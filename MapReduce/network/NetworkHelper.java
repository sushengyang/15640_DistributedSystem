/**
 * 
 */
package network;

import hdfs.KPFSMasterInterface;
import hdfs.KPFSSlaveInterface;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import mapreduce.GlobalInfo;

/**
 * NetworkHelper
 * 
 * Some helper functions 
 * 1. Sending and receiving message with socket (map-reduce)
 * 2. Get the service of RMI (KPFS)
 *
 */
public class NetworkHelper {
	public static Message receive(Socket socket) throws IOException, ClassNotFoundException {
		ObjectInputStream inStream;
		Object inObj;
		
		inStream = new ObjectInputStream(socket.getInputStream());
		inObj = inStream.readObject();
		if (inObj instanceof Message) {
			Message msg = (Message) inObj;
			return msg;
		}
		
		return null;
	}
	
	public synchronized static void send(Socket socket, Message msg) throws IOException {
		if (!(msg instanceof Message)) {
			return;
		}
		
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		out.writeObject(msg);
		
	}
	
	/*
	 * ATTENTION: before using this function, make sure you will get the response right away, 
	 * otherwise you will get stuck here. It's a block function.
	 */
	public static synchronized Message sendAndReceive(Socket socket, Message msg) throws IOException, ClassNotFoundException {
		/* send */
		if (!(msg instanceof Message)) {
			return null;
		}
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		out.writeObject(msg);
		
		/* receive */
		ObjectInputStream inStream;
		Object inObj;
		
		inStream = new ObjectInputStream(socket.getInputStream());
		inObj = inStream.readObject();
		if (inObj instanceof Message) {
			Message inMsg = (Message) inObj;
			return inMsg;
		}
		
		return null;
	}
	
	/* ============= RMI helper functions ============= */
	public static synchronized KPFSMasterInterface getMasterService() {
		Registry registry = null;
		KPFSMasterInterface masterService = null;
		try {
			registry = LocateRegistry.getRegistry(
					GlobalInfo.sharedInfo().DataMasterHost,
					GlobalInfo.sharedInfo().DataMasterPort);
			masterService = (KPFSMasterInterface) registry
					.lookup("DataMaster");
		} catch (RemoteException | NotBoundException e) {
			System.out.println("Data master is down. Failed to get KPFS service.");
		}
		return masterService;
	}

	public static synchronized KPFSSlaveInterface getSlaveService(int sid) {
		Registry registry = null;
		KPFSSlaveInterface slaveService = null;
		try {
			registry = LocateRegistry.getRegistry(GlobalInfo.sharedInfo().getSlaveHostBySID(sid),
					GlobalInfo.sharedInfo().getDataSlavePort(sid));
			slaveService = (KPFSSlaveInterface) registry
					.lookup("DataSlave");
		} catch (RemoteException | NotBoundException e) {
			System.out.println("Data node " + sid + " is down. Failed to get KPFS service.");
		}
		return slaveService;
	}
}
