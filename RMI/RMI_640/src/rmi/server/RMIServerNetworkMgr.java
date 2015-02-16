/**
 * 
 */
package rmi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import rmi.RMIMessage;
import rmi.RMIObjectReference;
import rmi.RMIMessage.RMIMsgType;

/**
 * RMIServerNetworkMgr
 * 
 * A network manager in server side. It is singleton and can be instantiated and got by 
 * invoking `sharedNetworkMgr()`. 
 *  
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class RMIServerNetworkMgr {
	private ServerSocket _svrSocket;
	
	/* singleton */
	private static RMIServerNetworkMgr _sharedNetworkMgr = null;
	
	/* 
	 * Set private to avoid being instantiated by mistake.
	 */
	private RMIServerNetworkMgr(int port) {
		try {
			_svrSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Port " + ServerConst.ListenPort + " is being used. Please change to"
					+ " another port in ServerConst.java and ClientConst.java in client.");
			System.exit(-1);
		}
	}
	
	/*
	 * Singleton method
	 */
	public static RMIServerNetworkMgr sharedNetworkMgr() {
		if (_sharedNetworkMgr == null) {
			_sharedNetworkMgr = new RMIServerNetworkMgr(ServerConst.ListenPort);
		}
		return _sharedNetworkMgr;
	}
	
	/* 
	 * Get the IP address of this machine to fill in the ROR
	 */
	public static String getLocalIP() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		return null;
	}
	
	/*
	 * Wait to receive a message from a client. After receiving one, handle it with 
	 * different response.
	 * 
	 * Return true if the connection is good. 
	 * Return false otherwise. 
	 * 
	 */
	public boolean msgReceiveAndHandler(Socket _socket) {
		RMIMessage msg = null;
		try {
			msg = receiveMsg(_socket);
		} catch (ClassNotFoundException | IOException e1) {
			/* the connection to this client is broken, close this socket */
			try {
				close(_socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		
		if (msg._type == RMIMsgType.LOOKUP) {
			/* lookup request from client */
			String name = (String)msg._content;
			RMIService obj = RMIServerRegistry.sharedRegistry().getObj(name);
			
			RMIMessage ret = new RMIMessage();
			ret._type = RMIMsgType.LOOKUP_RESPOND;
			
			if (obj == null) {
				/* content is null if the corresponding service is not established I*/
				ret._content = null;
			} else {
				/* set content to be the RMIObjectReference */
				RMIObjectReference ror = new RMIObjectReference();
				ror._objName = name;
				ror._remoteID = obj._rorID;
				ror._svrIP = RMIServerNetworkMgr.getLocalIP();
				ror._svrPort = ServerConst.ListenPort;
				
				ret._content = ror;
			}
			sendMsg(_socket, ret);
		}
		else if (msg._type == RMIMsgType.LIST) {
			String[] names = RMIServerRegistry.sharedRegistry().getBindedList();
			
			RMIMessage ret = new RMIMessage();
			ret._type = RMIMsgType.LIST_RESPOND;
			ret._content = names;
			sendMsg(_socket, ret);
		}
		else if (msg._type == RMIMsgType.CALL) {
			/* get the object by ror, if the ror is got from lookup, search in _registeredServices,
			 * or got from method invoke, search in _referencedServices (reference by value).
			 */
			RMIService obj = RMIServerRegistry.sharedRegistry().getObjByROR(msg.getROR());
			
			RMIMessage retMsg = new RMIMessage();
			retMsg._type = RMIMsgType.CALL_RESPOND;
			
			if (obj == null) {
				retMsg._content = null;
				sendMsg(_socket, retMsg);
			} else {
				String funName = msg.getMethodName();
				
				Object[] arg = msg.getArguments();
				Class<?>[] argType = msg.getArgType();
				
				try {
					Method m = obj.getClass().getMethod(funName, argType);
					
					/* invoke the method and get the return value */
					Object retVal = m.invoke(obj, arg);
					
					if (retVal instanceof RMIService) {
						/* if return value is subclass of RMIService, return ROR */
						RMIServerRegistry.sharedRegistry().addReferencedService((RMIService) retVal);
						RMIObjectReference ror = new RMIObjectReference();
						ror._objName = retVal.getClass().getSimpleName();
						ror._remoteID = ((RMIService) retVal)._rorID;
						ror._svrIP = RMIServerNetworkMgr.getLocalIP();
						ror._svrPort = ServerConst.ListenPort;
						retMsg._content = ror;
					} else {
						retMsg._content = retVal;
					}
					sendMsg(_socket, retMsg);
					
				} catch (NoSuchMethodException e) {
					System.out.println("No such method " + funName + ": ");
					for (Class<?> type: argType) {
						System.out.print(type.getName() + " ");
					}
					RMIMessage retMsgEx = new RMIMessage();
					retMsgEx._type = RMIMsgType.EXCEPTION;
					retMsgEx._content = e;
					sendMsg(_socket, retMsgEx);
				} catch (SecurityException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					RMIMessage retMsgEx = new RMIMessage();
					retMsgEx._type = RMIMsgType.EXCEPTION;
					retMsgEx._content = e;
					sendMsg(_socket, retMsgEx);
					e.printStackTrace();
				}
			}
			
		}
		return true;
	}
	
	
	/*
	 * You should have a loop to call this method to receive incoming clients.
	 */
	public Socket waitForClient() {
		try {
			Socket socket = _svrSocket.accept();
			return socket;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * Receive a message from client socket. Return null if the incoming message is not RMIMessage.
	 */
	public RMIMessage receiveMsg(Socket socket) throws IOException, ClassNotFoundException {
		ObjectInputStream inStream;
		Object inObj;
		
		inStream = new ObjectInputStream(socket.getInputStream());
		inObj = inStream.readObject();
		if (inObj instanceof RMIMessage) {
			RMIMessage msg = (RMIMessage) inObj;
			return msg;
		}
		
		return null;
	}
	
	/*
	 * Send msg to the client socket.
	 */
	public void sendMsg(Socket socket, Object msg) {
		if (!(msg instanceof RMIMessage)) {
			return;
		}
		ObjectOutputStream out;
		
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Close the socket.
	 */
	public void close (Socket socket) throws IOException {
		socket.close();
	}
}
