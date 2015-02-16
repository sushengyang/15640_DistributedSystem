/**
 * 
 */
package mapreduce;

import hdfs.KPFSMaster;
import hdfs.KPFSMasterInterface;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

/**
 * MapReduce
 * 
 * The main entrance of the entire system. 
 * 
 */
public class MapReduce {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("(m)aster or (s)lave? If choose to be slave, please provide the sid.");
		Scanner in = new Scanner(System.in);
		String line = in.nextLine();
		String[] cmd = line.split("\\s+");
		String opt = "";

		String role = null;
		if (cmd[0].contains("m")) {
			role = "m";
		} else if (cmd[0].contains("s")) {
			role = "s";
			opt = cmd[1];
		} else {
			System.out.println("Invalid input. Please restart.");
			System.exit(-1);
		}
		start("config.txt", role, opt);
	}

	private static void start(String confFileName, String mstOrSlv, String opt) {
		Properties prop = new Properties();
		try {
			InputStream propInput = new FileInputStream(confFileName);
			prop.load(propInput);
		} catch (FileNotFoundException e) {
			System.out.println("Config file " + confFileName + " not found!");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Invalid config file " + confFileName
					+ ". Please check again.");
			System.exit(-1);
		}

		loadConfig(prop);

		if (mstOrSlv.contains("m")) {
			
			System.out.println("Master");
			Master.sharedMaster().start();
		} else if (mstOrSlv.contains("s")) {
			
			System.out.println("Slave");
			int sid = Integer.parseInt(opt);
			String confIP = GlobalInfo.sharedInfo().SID2Host.get(sid);
			if (confIP==null || (confIP.equals(GlobalInfo.sharedInfo().getLocalHost()) == false)) {
				System.err.println("This host is not configured as a slave of sid " + sid + " in config.txt!" );
				System.exit(-1);
			}
			GlobalInfo.sharedInfo()._sid = sid;
			Slave.sharedSlave().start();
		} else {
			System.out.println("This machine is not included in config file!");
		}
	}

	private static void loadConfig(Properties prop) {
		GlobalInfo.sharedInfo().MasterPort = Integer.parseInt(prop
				.getProperty("MasterPort"));
		GlobalInfo.sharedInfo().SlavePort = Integer.parseInt(prop
				.getProperty("SlavePort"));
		GlobalInfo.sharedInfo().MasterHost = prop.getProperty("MasterHost");
		GlobalInfo.sharedInfo().SID2Host.put(0, GlobalInfo.sharedInfo().MasterHost);

		String[] slaves = prop.getProperty("SlaveHosts").split(",");
		for (int i = 0; i < slaves.length; ++i) {
			GlobalInfo.sharedInfo().SID2Host.put(i + 1, slaves[i].trim());
		}

		String[] slaverootdir = prop.getProperty("SlaveRootDir").split(",");
		for (int i = 0; i < slaverootdir.length; ++i) {
			GlobalInfo.sharedInfo().Host2RootDir.put(i + 1, slaverootdir[i].trim());
		}
		
		String[] slavecapacity = prop.getProperty("SlaveCapacity").split(",");
		for (int i=0; i<slavecapacity.length; ++i) {
			GlobalInfo.sharedInfo().SID2Capacity.put(i+1, Integer.parseInt(slavecapacity[i].trim()));
		}

		GlobalInfo.sharedInfo().FileChunkSizeB = Integer.parseInt(prop
				.getProperty("FileChunkSizeB"));
		GlobalInfo.sharedInfo().NumberOfReducer = Integer.parseInt(prop
				.getProperty("NumberOfReducer"));
		GlobalInfo.sharedInfo().MasterRootDir = prop
				.getProperty("MasterRootDir");
		GlobalInfo.sharedInfo().Host2RootDir.put(0, GlobalInfo.sharedInfo().MasterRootDir);

		GlobalInfo.sharedInfo().IntermediateDirName = prop
				.getProperty("IntermediateDirName");
		GlobalInfo.sharedInfo().ChunkDirName = prop.getProperty("ChunkDirName");
		GlobalInfo.sharedInfo().ResultDirName = prop
				.getProperty("ResultDirName");
		GlobalInfo.sharedInfo().UserDirName = prop
				.getProperty("UserDirName");

		GlobalInfo.sharedInfo().DataMasterHost = prop
				.getProperty("DataMasterHost");
		GlobalInfo.sharedInfo().DataMasterPort = Integer.parseInt(prop
				.getProperty("DataMasterPort"));
		GlobalInfo.sharedInfo().DataSlavePort = Integer.parseInt(prop
				.getProperty("DataSlavePort"));

	}

	// ====== test begin, for debug ======
	private static void testSplit() throws RemoteException {
		File file = new File("words.txt");
		String dir = file.getAbsolutePath().substring(0,
				file.getAbsolutePath().lastIndexOf('/') + 1);
		String fileName = file.getAbsolutePath().substring(
				file.getAbsolutePath().lastIndexOf("/") + 1);
		System.out.println(dir);
		System.out.println(fileName);

		Registry registry = null;
		KPFSMasterInterface _kpfsMaster = null;

		try {
			registry = LocateRegistry.getRegistry(
					GlobalInfo.sharedInfo().DataMasterHost,
					GlobalInfo.sharedInfo().DataMasterPort);
		} catch (RemoteException e) {
			registry = LocateRegistry.createRegistry(GlobalInfo.sharedInfo().DataMasterPort);
		}
		
		try {
			_kpfsMaster = (KPFSMasterInterface) registry
					.lookup("KPFSMasterInterface");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		ArrayList<String> files = ((KPFSMaster) _kpfsMaster).splitFile(file.getAbsolutePath(),
				20, dir, fileName);
		for (String fn : files) {
			System.out.println(fn);
		}
	}

	private static void testMkdir() {
		File aa = new File("/Users/PY/Desktop/pypy");
		if (aa.mkdirs() == true) {
			System.out.println("mkdir successfully!");
		} else {
			System.out.println("failed to mkdir!");
		}
	}

	private static void testKPFSMaster() {
		KPFSMaster master = new KPFSMaster();
		master.addFileLocation("a", 1, 14);
		master.addFileLocation("a", 2, 15);
		master.addFileLocation("b", 3, 16);
		master.addFileLocation("c", 4, 17);
		System.out.println(master.getFileLocation("a"));
		System.out.println(master.getFileLocation("d"));
		System.out.println(master.getFileLocation("c"));
		master.removeFileLocation("a", 1);
		System.out.println(master.getFileLocation("a"));
	}

	private static void testLoadJar() {
		File jarFile = new File("jar/WordCounter.jar");
		byte[] byteArr = new byte[(int) jarFile.length()];

		try {
			FileInputStream fin = new FileInputStream(jarFile);
			BufferedInputStream bin = new BufferedInputStream(fin);
			bin.read(byteArr, 0, byteArr.length);
			bin.close();
			fin.close();

			File file = File.createTempFile("WordCounter", null);
			file.deleteOnExit();
			FileOutputStream bout = new FileOutputStream(file);
			bout.write(byteArr);
			bout.close();

			// File file = new File("jar/WordCounter.jar");

			System.out.println(file.getAbsolutePath());
			System.out.println(file.exists());

			URL[] urls = { file.toURI().toURL() };
			Class cls = (new URLClassLoader(urls)).loadClass("WordCounter");

			Constructor mapConstr = cls.getConstructor();
			MRBase mapper = (MRBase) mapConstr.newInstance();
			mapper.map("xxx", "b", new PairContainer());

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
