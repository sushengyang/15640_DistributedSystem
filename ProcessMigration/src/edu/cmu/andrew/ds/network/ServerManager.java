package edu.cmu.andrew.ds.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import edu.cmu.andrew.ds.ps.MigratableProcess;

/**
 * ServerManager
 * 
 * Responsible for all the network communications in server side. 
 * 
 * In a new thread, it will run a loop accepting all the incoming clients 
 * and create a new instance of ServerHandler in a new thread reading incoming
 * message from connected clients. 
 * 
 * In the main thread, it provides a message handler handling all the incoming
 * messages. Also, it has interfaces serving ClusterManager.
 * 
 * @author KAIILANG CHEN(kailianc)
 * @author YANG PAN(yangpan)
 * @version 1.0
 */
public class ServerManager extends NetworkManager {
	
	/**
	 * MigrateTaks
	 * 
	 * A structure caching the migrate task info. Before requesting a client to
	 * emigrate a process, ServerManager will create a new instance of MigrateTask
	 * and cache it in _migrateTasks. After receiving the respond from the requested
	 * client, ServerManager will check _migrateTasks and find the destination and 
	 * send it there.
	 */
	public class MigrateTask {
		int _srcCid;
		int _srcPid;
		int _dstCid;
		MigrateTask(int srcCid, int srcPid, int dstCid) {
			_srcCid = srcCid;
			_srcPid = srcPid;
			_dstCid = dstCid;
		}
	}
	
	private ServerSocket _svrSocket = null;
	
	/* manage the increasing client id to assign a new client an id */
	private volatile AtomicInteger _cid = null;
	
	/* maintain the map between client id and socket of a client */
	private volatile Map<Integer, Socket> _clients = null;
	
	/* cache the migrate task info before receiving respond */
	ArrayList<MigrateTask> _migrateTasks = null;
	
	public ServerManager(int svrPort) {
		try {
			_clients = new ConcurrentSkipListMap<Integer, Socket>();
			_cid = new AtomicInteger(0);
			_migrateTasks = new ArrayList<MigrateTask>();
			
			_svrSocket = new ServerSocket(svrPort);
			
			System.out.println("Waiting for clients...");
			System.out.println("Please connect to " + InetAddress.getLocalHost() + ":" + svrPort + ".");
		} catch (IOException e) {
			println("ERROR: failed to listen on port " + svrPort);
			e.printStackTrace();
		}
	}
	
	/*
	 * Run a loop to accept incoming clients. Once a connection is established, 
	 * create a new instance of ServerHandler in a new thread to receive
	 * incoming messages by running a loop.
	 */
	@Override
	public void run() {
		while (true) {
			try {
				/* accepting new clients */
				Socket socket = _svrSocket.accept();
				addClient(socket);
				System.out.println("New client(cid is " + getCid(socket) + ") connected!");
				
				/* create a new instance of ServerHandler to receive messages */
				new ServerHandler(this, socket).start();
				/* send the client id to the new client */
				sendMsg(socket, new MessageStruct(5, Integer.valueOf(getCid(socket))));
			} catch (IOException e) {
				/* ServerSocket is closed */
				break;
			}
		}
	}
	
	public void clientDisconnected(Socket client) {
		int cid = getCid(client);
		System.out.println("Client " + cid + " has disconnected.");
		
		deleteClient(cid);
	}
	
/* ================== Message handlers begin ==================*/
	/*
	 * All messages coming from clients will be sent here. Currently server only needs
	 * to respond to two types of messages: type 1 and 3. For more details about the 
	 * message type, see MessageStruct.
	 * 
	 * @param msg	incoming message
	 * @param src	socket of client sending this message
	 */
	@Override
	public void msgHandler(MessageStruct msg, Socket src) {
		switch (msg._code) {
		case 0:
			/* message type sent from server to client */
			break;
		case 1:
			/* process info from clients */
			if (msg._content instanceof ArrayList<?>) {
				ArrayList<ArrayList<String>> proc = (ArrayList<ArrayList<String>>)msg._content;
				displayFromClient(proc, getCid(src));
			}
			break;
		case 2:
			/* message type sent from server to client */
			break;
		case 3:
			/* process from one client to be sent to another client */
			int cid = getCid(src);
			if (msg._content == null) {
				System.out.println("Client " + cid + " has no such process! Please check the pid again.");
				break;
			}
			if (msg._content instanceof MigratableProcess) {
				migrateToClient((MigratableProcess)msg._content, getCid(src));
			}
			break;
		case 4:
			/* message type sent from server to client */
			break;
		default:
			break;
		}
	}
	
	/*
	 * Receiving the process information of a client and printing it.
	 */
	private void displayFromClient(ArrayList<ArrayList<String>> proc, int srcCid) {
		if (proc.isEmpty()) {
			System.out.println("Client " + srcCid + " has no running process.");
			return;
		}
		for (ArrayList<String> p : proc) {
			System.out.println("\t" + srcCid + "\t" + p.get(0) + "\t" + p.get(1));
		}
	}
	
	/*
	 * Receiving a process from a client and migrating that to another client. The destination 
	 * can be found in _migrateTasks with the src_pic and src_cid.
	 */
	private void migrateToClient(MigratableProcess mp, int srcCid) {
		for (MigrateTask i: _migrateTasks) {
			if (i._srcCid==srcCid && i._srcPid==mp._pid) {
				try {
					Socket dst = getClient(i._dstCid);
					if (dst == null) {
						System.out.println("Connection to " + i._dstCid + " is broken! Cannot migrate process to it. Process lost.");
						return;
					}
					
					sendMsg(dst, new MessageStruct(4, mp));
				} catch (IOException e) {
					println("Connection to " + i._dstCid + " is broken! Cannot migrate process to it. Process lost.");
				}
				System.out.println("Migrate process successfully to client " + i._dstCid + ".");
				break;
			}
		}
	}
/* ================== Message handlers end ==================*/
	
/* ================== Client info manage methods begin ==================*/
	private void addClient(Socket socket) {
		_clients.put(Integer.valueOf(_cid.getAndIncrement()), socket);
	}
	
	private boolean deleteClient(int idx) {
		if (_clients.remove(Integer.valueOf(idx)) == null) {
			println("delete failed!");
			return false;
		}
		return true;
	}
	
	private Socket getClient(int cid) {
		return (Socket)_clients.get(Integer.valueOf(cid));
	}
	
	private int getCid(Socket socket) {
		for (Map.Entry<Integer, Socket> entry : _clients.entrySet()) {
		    if (entry.getValue() == socket) {
		    	return entry.getKey().intValue();
		    }
		}
		return -1;
	}
/* ================== Client info manage methods end ==================*/
	
/* ================== Interfaces for ClusterManager begin ==================*/
	/*
	 * Send request to all clients to send the process info to server.
	 */
	public void examClients() {
		System.out.println("Processes running on all clients: ");
		System.out.println("\tCID\tPID\tCLASSNAME");
		MessageStruct msg = new MessageStruct(0, null);
		
		for (Map.Entry<Integer, Socket> entry : _clients.entrySet()) {
		    try {
				sendMsg(entry.getValue(), msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * After receiving user input to migrate process, use this method to send request to 
	 * source client to suspend that process and send it to server. This method also
	 * caches this migration task info in _migrateTask for future use. 
	 */
	public void sendMigrateRequest(int srcCid, int srcPid, int dstCid) {
		Socket socket = getClient(srcCid);
		if (socket == null) {
			System.out.println("Cannot migrate. Client " + srcCid + " is not available!");
			return;
		}
		if (getClient(dstCid) == null) {
			System.out.println("Cannot migrate. Client " + dstCid + " is not available!");
			return;
		}
		
		/* cache the migrate task info for future use */
		_migrateTasks.add(new MigrateTask(srcCid, srcPid, dstCid));
		try {
			sendMsg(socket, new MessageStruct(2, Integer.valueOf(srcPid)));
		} catch (IOException e) {
			println("ERROR: Connection with " + srcCid + " is broken, message cannot be sent!");
			return;
		}
	}
	
	public void close() {
		System.out.println("Server is about to close. All connected clients will exit.");
		try {
			_svrSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Bye~");
	}
/* ================== Interfaces for ClusterManager end ==================*/
	
	/*
	 * Internal debug print method.
	 */
	private void println(String msg) {
		System.out.println("ServerManager: " + msg);
	}
}
