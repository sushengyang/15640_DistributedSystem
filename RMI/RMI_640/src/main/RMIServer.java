/**
 * 
 */
package main;

import java.net.Socket;

import rmi.RMIException;
import rmi.server.RMIServerNetworkMgr;
import rmi.server.RMIServerRegistry;
import services.StudentList;

/**
 * RMIServer
 * 
 * The main entrance of the server program. 
 * 
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class RMIServer {
	
	/**
	 * Everything in a server starts from here.
	 * 
	 * @param args No use.
	 */
	public static void main(String[] args) {
		StudentList studentList = new StudentList();
		try {
			RMIServerRegistry.sharedRegistry().bind("StudentList", studentList);
		} catch (RMIException e) {
			e.printStackTrace();
		}
		
		RMIServerNetworkMgr netmgr = RMIServerNetworkMgr.sharedNetworkMgr();
		
		while (true) {
			Socket client = netmgr.waitForClient();
			new RMISvrHandler(client).start();
		}
	}

}
