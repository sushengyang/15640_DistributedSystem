/**
 * 
 */
package hdfs;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author PY
 *
 */
public interface KPFSSlaveInterface extends Remote {
	/*
     * Provide the content as string of the file in relPath in this data node.
     */
	public String getFileString(String relPath) throws RemoteException, KPFSException;
	
	/*
     * Provide the content as byte[] of the file in relPath in this data node.
     */
	public byte[] getFileBytes(String relPath) throws RemoteException, KPFSException;
	
	/*
     * Store the content as a file in relPath in this data node. 
     * Used to receive a copy of input file from data master.
     */
	public void storeFile(String relPath, byte[] content) throws RemoteException, KPFSException;
}
