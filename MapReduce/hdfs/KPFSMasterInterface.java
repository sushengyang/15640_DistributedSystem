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
public interface KPFSMasterInterface extends Remote {
	/*
	 * Return the location information of a file with the in the relative path.
	 * The relative path of a file is its identification.
	 */
	public KPFSFileInfo getFileLocation(String relPath) throws RemoteException;
	
	/*
	 * Add the metadata of a file. Used when a slave produces a new file (intermediate file
	 * or result file).
	 */
	public boolean addFileLocation(String relPath, int sid, long size) throws RemoteException;
	
	/*
	 * Remove the location information in sid of the file in the relPath.
	 * Used when sid is down.
	 */
	public void removeFileLocation(String relPath, int sid) throws RemoteException;
}
