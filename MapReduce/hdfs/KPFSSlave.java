/**
 * 
 */
package hdfs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import mapreduce.GlobalInfo;

/**
 * KPFSSlave
 * 
 * KPFS communicates with RMI. This is the service class in data node.
 * Used to provide the content of a file node and store a new file in this data.
 * 
 * Note: the data master can also be a data node.
 *
 */
public class KPFSSlave implements KPFSSlaveInterface {
    public KPFSSlave() {}
	
    /*
     * Provide the content as string of the file in relPath in this data node.
     */
	@Override
	public synchronized String getFileString(String relPath) throws KPFSException {
		File file = new File(GlobalInfo.sharedInfo().getLocalRootDir() + relPath);
		Scanner exp = null;
		try {
			exp = new Scanner(file);
		} catch (FileNotFoundException e) {
			throw new KPFSException(GlobalInfo.sharedInfo()._sid + ": File " + relPath + " is not found!");
		}
		
		String str = "";
		while (exp.hasNextLine()) {
			str += exp.nextLine() + '\n';
		}
		exp.close();
		return str;
	}

	/*
     * Provide the content as byte[] of the file in relPath in this data node.
     */
	@Override
	public synchronized byte[] getFileBytes(String relPath) throws KPFSException {
		File file = new File(GlobalInfo.sharedInfo().getLocalRootDir() + relPath); 
		byte[] byteArr = new byte[(int)file.length()];
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new KPFSException("File " + relPath + " is not found!");
		}
		
		BufferedInputStream bin = new BufferedInputStream(fin);
		
		try {
			bin.read(byteArr, 0, byteArr.length);
			bin.close();
			fin.close();
		} catch (IOException e) {
			throw new KPFSException("Internal error occurs when reading the file " + relPath);
		}
		
		return byteArr;
	}
	
	/*
     * Store the content as a file in relPath in this data node. 
     * Used to receive a copy of input file from data master.
     */
	@Override
	public synchronized void storeFile(String relPath, byte[] content) throws KPFSException {
		File file = new File(GlobalInfo.sharedInfo().getLocalRootDir() + relPath); 
		file.getParentFile().mkdirs();
		
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(file);
			outStream.write(content);
			outStream.close();
		} catch (IOException e) {
			throw new KPFSException("Internal error occurs when storing the file " + relPath);
		}
	}

	

}
