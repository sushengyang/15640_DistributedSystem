/**
 * 
 */
package mapreduce;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * GlobalInfo
 * 
 * Store all the information in "config.txt". And provide some convenient methods to 
 * get the information. Served for both map-reduce system and KPFS.
 *
 */
public class GlobalInfo {
	/* ======== For map-reduce ======== */
	public int MasterPort = 7888;
	public int SlavePort = 8999;
	public String MasterHost = "73.52.255.101";
	public HashMap<Integer, String> SID2Host = new HashMap<Integer, String>();
	public HashMap<Integer, Integer> SID2Capacity = new HashMap<Integer, Integer>(); 
	
	/* ======== For KPFS ======== */
	public int FileChunkSizeB = 27;
	public int NumberOfReducer = 3;
	
	public String MasterRootDir = "/tmp/master";
	
	public String IntermediateDirName = "IntermediateFiles";
	public String ChunkDirName = "ChunkInputFiles";
	public String ResultDirName = "ResultFiles";
	public String UserDirName = "UserFiles";
	
	
	public String DataMasterHost = "73.52.255.101";
	public int DataMasterPort = 9980;
	public int DataSlavePort = 9990;
	
	/* actually should be ID2RootDir, translate sid to the root directory of that machine (including master) */
	public HashMap<Integer, String> Host2RootDir = new HashMap<Integer, String>();
	
	/* ======== set by every node ======== */
	public int _sid = -1;
	
	
	public static GlobalInfo _sharedInfo = null;
	public static GlobalInfo sharedInfo() {
		if (_sharedInfo == null) {
			_sharedInfo = new GlobalInfo();
		}
		return _sharedInfo;
	}
	
	/* Make constructor private to ensure its singleton */
	private GlobalInfo() {
		
	}
	
	public String getSlaveHostBySID(int sid) {
		String ret = SID2Host.get(sid);
		if(ret == null) {
			return "";
		}
		return ret;
	}
	public String getRootDirByHost(String host) {
		String ret = Host2RootDir.get(host);
		if(ret == null) {
			return "";
		}
		return ret;
	}
	
	
	public String getLocalRootDir() {
		return Host2RootDir.get(_sid);
	}
	
	public String getLocalHost() {
		String localhost = "";
		try {
			localhost = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.err.println("Network error!");
			e.printStackTrace();
		}
		return localhost;
	}
	
	public int getDataSlavePort(int sid) {
		return DataSlavePort + sid;
	}
}
