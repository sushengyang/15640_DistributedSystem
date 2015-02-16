/**
 * 
 */
package main;

import java.net.Socket;

import rmi.server.RMIServerNetworkMgr;


/**
 * RMISvrHandler
 * 
 * A handler to receive a message from a client. One handler is responsible for one client. 
 * You should implement your own handler if you use our RMI package. 
 * 
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class RMISvrHandler extends Thread {
	
	private Socket _socket;
	
	public RMISvrHandler(Socket socket) {
		_socket = socket;
	}

	@Override
	public void run() {
		/* if msgReceiveAndHandler returns false (the connection is broken), end the loop */
		while (RMIServerNetworkMgr.sharedNetworkMgr().msgReceiveAndHandler(_socket)) {
			;
		}
		
	}

}
