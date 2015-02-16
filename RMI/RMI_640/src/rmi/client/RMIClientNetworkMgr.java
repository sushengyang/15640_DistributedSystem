/**
 * 
 */
package rmi.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import rmi.RMIMessage;

/**
 * RMIClientNetworkMgr
 * 
 * The network manager for a client. It only contains one static method to send and receive
 * message. It's enough in the RMI network communication.
 *  
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class RMIClientNetworkMgr extends Object {
	/*
	 * Send a message to server and wait for the response.
	 */
	public static RMIMessage sendAndReceive(String ipAddr, int port, RMIMessage msg) {
		RMIMessage rmiMsg = null;
		try {
			Socket socket = new Socket(ipAddr, port);
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(msg);
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			Object inMsg = in.readObject();
			if (inMsg instanceof RMIMessage) {
				rmiMsg = (RMIMessage) inMsg;
			}
			
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return rmiMsg;
		
	}
	
}
