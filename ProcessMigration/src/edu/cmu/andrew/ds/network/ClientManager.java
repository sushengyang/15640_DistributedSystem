package edu.cmu.andrew.ds.network;

import java.io.IOException;
import java.net.Socket;

import edu.cmu.andrew.ds.ps.MigratableProcess;
import edu.cmu.andrew.ds.ps.ProcessManager;

/**
 * ClientManager
 * 
 * Responsible for all the network communications in client side.
 * 
 * In a new thread, it will run a loop receiving messages sent from server and 
 * dispatch it to the main thread to handle.
 * 
 * In the main thread, it provides a message handler handling all the incoming
 * messages. Also, it has interfaces serving ProcessManager.
 * 
 * @author KAIILANG CHEN(kailianc)
 * @author YANG PAN(yangpan)
 * @version 1.0
 * 
 */
public class ClientManager extends NetworkManager {
	
	/* the socket communicating with server */
	Socket _socket = null;
	/* instance of ProcessManager to callback */
	public ProcessManager _procMgr = null;
	
	public ClientManager(String addr, int port) {
		try {
			_socket = new Socket(addr, port);
			System.out.println("Connected to server: " + addr + ":" + port);
		} catch (IOException e) {
			System.out.println("Cannot connect to server " + addr + ":" + port);
			e.setStackTrace(e.getStackTrace());
			System.exit(0);
		}
	}
	
/* ================== Interfaces for ProcessManager begin ==================*/
	public void sendMsg(MessageStruct msg) throws IOException {
		sendMsg(_socket, msg);
	}
	
	/*
	 * Close the socket to exit.
	 */
	public void close() {
		try {
			_socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
/* ================== Interfaces for ProcessManager end ==================*/
	
	/*
	 * All messages coming from server will be sent here. Currently client needs to respond 
	 * to three types of messages: type 0, 2, 4 and 5. For more details about the message 
	 * type, see MessageStruct.
	 * 
	 * @param msg	incoming message
	 * @param src	not used in client
	 */
	@Override
	public void msgHandler(MessageStruct msg, Socket src) {
		switch (msg._code) {
		case 0:
			/* gather process info and send to server */
			_procMgr.displayToServer();
			break;
		case 1:
			/* message type sent from client to server */
			break;
		case 2:
			/* request from server to migrate a process */
			if (msg._content instanceof Integer) {
				int pid = ((Integer)msg._content).intValue();
				System.out.println("Request from server to emmigrate process " + pid);
				_procMgr.emmigrateToServer(pid);
			}
			
			break;
		case 3:
			/* message type sent from client to server */
			break;
		case 4:
			/* immigrating process sent from server */
			if (msg._content instanceof MigratableProcess) {
				_procMgr.immigrateFromServer((MigratableProcess)msg._content);
			}
			break;
		case 5:
			/* get client id from server */
			if (msg._content instanceof Integer) {
				_procMgr._prompt = "#" + ((Integer)msg._content).intValue() + " > ";
			}
		default:
			break;
		}
	}
	
	/*
	 * Running a loop to receive messages from server. If it fails when receiving, the 
	 * connections is broken. Close the socket and exit with -1.
	 */
	@Override
	public void run() {
		while(true) {
			try {
				receiveMsg(_socket);
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Connection to server is broken. Please restart client.");
				close(_socket);
				System.exit(-1);
			}
		}
	}
}
