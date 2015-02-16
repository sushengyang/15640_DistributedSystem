/**
 * 
 */
package hdfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;

import network.NetworkHelper;
import mapreduce.GlobalInfo;

/**
 * KPFile
 * 
 * Encapsulates all the file operation in KPFS. Every file is identified by relative directory and
 * it's file name. Provides two methods to get the content:  getFileString() and getFileBytes(). These 
 * methods will fetch the location of this file from data master, and then fetch the content from 
 * the corresponding data node.
 * 
 */
public class KPFile implements Serializable {

	private static final long serialVersionUID = 225432063820226038L;
	
	/* the  directory of this file relative to the root directory. 
	 * all the directory inside the program end with "/" 
	 */
	public String _relDir = null; /* "taskName/*Files/" */
	
	public String _fileName = null; /* "taskName.part001" */

	public KPFile(String relDir, String fileName) {
		_relDir = relDir;
		_fileName = fileName;
	}

	/*
	 * Get the content as string of this file from data node. The location of this file will be 
	 * first fetched from data master.
	 */
	public String getFileString() throws RemoteException {
		/* retrieve location information from data master */
		KPFSMasterInterface masterService = NetworkHelper.getMasterService();
		if (masterService == null) {
			return null;
		}
		KPFSFileInfo info = masterService.getFileLocation(getRelPath());
		
		if (info == null) {
			throw new RemoteException("Cannot find the location of the file: " + getRelPath());
		}

		/* retrieve file content from actual data node */
		KPFSSlaveInterface slaveService = NetworkHelper.getSlaveService(info._sid);
		if (slaveService == null) {
			return null;
		}
		String content = null;
		try {
			content = slaveService.getFileString(getRelPath());
		} catch (KPFSException e) {
			System.out.println("Failed to read content from KPFile!");
			e.printStackTrace();
		}

		return content;
	}

	/*
	 * Get the content as byte[] of this file from data node. The location of this file will be 
	 * first fetched from data master.
	 */
	public byte[] getFileBytes() throws RemoteException {
		KPFSMasterInterface masterService = NetworkHelper.getMasterService();
		if (masterService == null) {
			return null;
		}
		KPFSFileInfo info = masterService.getFileLocation(getRelPath());

		/* retrieve file content from actual data node */
		KPFSSlaveInterface slaveService = NetworkHelper.getSlaveService(info._sid);
		if (slaveService == null) {
			return null;
		}
		byte[] content = null;
		try {
			content = slaveService.getFileBytes(getRelPath());
		} catch (KPFSException e) {
			System.out.println("Failed to read content from KPFile!");
			e.printStackTrace();
		}

		return content;
	}

	/*
	 * Save this file in the local data node and notify data master the location information
	 */
	public void saveFileLocally(byte[] byteArr)
			throws IOException {
		File file = new File(getLocalAbsPath());
		file.getParentFile().mkdirs();

		FileOutputStream outStream = new FileOutputStream(file, false);
		outStream.write(byteArr);
		outStream.write("\n".getBytes());
		outStream.close();
		
		/* save the metadata of this file to master */
		KPFSMasterInterface masterService = NetworkHelper.getMasterService();
		masterService.addFileLocation(getRelPath(), GlobalInfo.sharedInfo()._sid,
				(int) file.length());
		
	}
	
	public String getRelPath() {
		return _relDir + _fileName;
	}

	public String getLocalAbsPath() {
		return GlobalInfo.sharedInfo().getLocalRootDir() + getRelPath();
	}
}
