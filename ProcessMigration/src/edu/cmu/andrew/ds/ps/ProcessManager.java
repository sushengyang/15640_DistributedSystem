package edu.cmu.andrew.ds.ps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import edu.cmu.andrew.ds.network.ClientManager;
import edu.cmu.andrew.ds.network.MessageStruct;


/**
 * ProcessManager
 * 
 * When client as server, ProcessManager is the main control class of the program.
 * It start a new thread running ClientManager to receive messages. In the main thread,
 * a loop is running to receive and handle user input. Also provides some interfaces for 
 * ClientManager to call to handle the request from server.
 * 
 * Support input command:
 * 		create PROC_NAME [OPTIONAL_ARG]: 
 * 			Craete a new instance of class PROC_NAME inherited MigratableProcess.
 * 			All the arguments following PROC_NAME will be passed to the constructor
 * 			of that instance.
 * 		call PID METHOD_NAME [OPTIONAL_ARG]: 
 * 			Call a method METHOD_NAME of process PID with optional arguments 
 * 			OPTIONAL_ARG. 
 * 		ps:
 * 			Show the info of all the running processes on all clients.
 * 		help: 
 * 			Show all the accepted input commands.
 * 		exit: 
 * 			Close the client program. 
 * 
 * @author KAIILANG CHEN(kailianc)
 * @author YANG PAN(yangpan)
 * @version 1.0
 *
 */

public class ProcessManager {
	
	private String _packageName;
	private ClientManager _cltMgr = null;
	
	/* 
	 * a prompt before user input, after connection is established, server will send 
	 * the client id back, and the prompt will be changed to "#ID > ", which is 
	 * convenient for distinguish different client. 
	 */
	public String _prompt = "> ";

	/*
	 * maintain a map between pid and process.
	 */
	private volatile Map<Integer, MigratableProcess> _pmap = new ConcurrentSkipListMap<Integer, MigratableProcess>();
	
	/* manage the increasing pid to assign a new process an id */
	private volatile AtomicInteger _pidCnt; 	
	
	public ProcessManager(String svrAddr, int port) {
		_pidCnt = new AtomicInteger(0);
		_packageName = this.getClass().getPackage().getName();
		_cltMgr = new ClientManager(svrAddr, port);
		_cltMgr._procMgr = this;
		
		/* new thread to receive msg */
		new Thread(_cltMgr).start();
	}
		
	/*
	 * Accepting user input and handling them.
	 */
	public void startClient() {
		System.out.println("Type 'help' for more information");
		
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(_prompt);
        while (true) {
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException e) {
            	println("ERROR: read line failed!");
            	return;
            }
            execCmd(line.split("\\s+"));
            System.out.print(_prompt);
        }
	}
	
/* ================== Input handlers begin ==================*/
	/*
	 * Support input command:
	 * 		create PROC_NAME [OPTIONAL_ARG]: 
	 * 			Craete a new instance of class PROC_NAME inherited MigratableProcess.
	 * 			All the arguments following PROC_NAME will be passed to the constructor
	 * 			of that instance.
	 * 		call PID METHOD_NAME [OPTIONAL_ARG]: 
	 * 			Call a method METHOD_NAME of process PID with optional arguments 
	 * 			OPTIONAL_ARG. 
	 * 		ps:
	 * 			Show the info of all the running processes on all clients.
	 * 		help: 
	 * 			Show all the accepted input commands.
	 * 		exit: 
	 * 			Close the client program. 
	 */
	private void execCmd(String[] arg) {
		switch(arg[0]) {
		case "create":
		case "ct":
			if (arg.length < 2) {
				System.out.println("Invalid command.");
				break;
			}
			create(arg);
			break;
		case "call":
			if (arg.length < 3) {
				System.out.println("Invalid command.");
				break;
			}
			callMethod(arg);
		case "ps":
			display();
			break;
		case "exit":
		case "st":
			exit();
			break;
		case "help":
		case "hp":
			help();
			break;
		default:
			break;	
		}	
	}
	
    private void create(String[] str) {
    	String psName = str[1];
    	MigratableProcess ps = null;
		try {
			String[] s = Arrays.copyOfRange(str, 2, str.length);
			Class<?> cls = Class.forName(_packageName+ "." + psName);
			Constructor<?> ctor = cls.getConstructor(String[].class);
			
			ps = (MigratableProcess)ctor.newInstance((Object)s);
			
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			println("Class " + psName + " not found.");
			return;
		} catch (NoSuchMethodException e) {
			println(psName + " should have a constructor with prototype " + psName + "(String[]);");
			return;
		} catch (InvocationTargetException e) {
			println("Please provide appropriate arguments to the constructor of " + psName + "!");
			return;
		}
		
		addProcess(ps);
		ps._pid = getPid(ps);
		System.out.println(psName + " class has been created, pid: " + getPid(ps));
    	Thread thread = new Thread(ps);
        thread.start();
        display();
	}
	
	private void callMethod(String[] argv) {
		int pid = 0;
		try {
			pid = Integer.parseInt(argv[1]);
		} catch (NumberFormatException e) {
			println("Pid is not a number!");
			return;
		}
		MigratableProcess ps = getProcess(pid);
		if (ps == null) {
			println("Invalid pid!");
			return;
		}
		
		Method method = null;
		try {
			method = ps.getClass().getMethod(argv[2], new Class[]{String[].class});
		} catch (NoSuchMethodException e) {
			println("No such method named " + argv[2] + " found");
			return;
		}catch (SecurityException e) {
			e.printStackTrace();
		}
		try {
			method.invoke(ps, (Object)Arrays.copyOfRange(argv, 3, argv.length));
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			println("Illegal argument!");
			return;
		}
	}
	
	/*
	 * 
	 */
	private void display() {
		if (_pmap.size() == 0) {
			System.out.println("\tNo process is currently running.");
			return;
		}
		System.out.println("\tpid\tClass Name");
		for (Map.Entry<Integer, MigratableProcess> entry : _pmap.entrySet()) {
		    System.out.println("\t" + entry.getKey() + "\t" + entry.getValue().getClass().getName());
		}
	}
	
	public void help() {
		System.out.println("Here are the commands:");
		System.out.println("\tcommand\t\t\tdescription");
		System.out.println("\tcreate CLASSNAME\tcreate a new instance of CLASSNAME");
		System.out.println("\tmigrate PID\t\tmigrate a process with PID to another computer");
		System.out.println("\tps\t\t\tdisplay all the running processes");
		System.out.println("\texit\t\t\texit the program");
	}
	
	private void exit() {
		_cltMgr.close();
		System.exit(0);
	}
/* ================== Input handlers end ==================*/
	
/* ================== Interfaces for ClientManager begin ==================*/
	public void displayToServer() {
		ArrayList<ArrayList<String>> content = new ArrayList<ArrayList<String>>();
		for (Map.Entry<Integer, MigratableProcess> entry : _pmap.entrySet()) {
			ArrayList<String> cur = new ArrayList<String>();
			cur.add(String.valueOf(entry.getKey()));
			cur.add(entry.getValue().getClass().getName());
			content.add(cur);
		}
		
		MessageStruct msg = new MessageStruct(1, content);
		try {
			_cltMgr.sendMsg(msg);
		} catch (IOException e) {
			System.out.println("Connection to server is broken. Please restart client.");
			_cltMgr.close();
			System.exit(-1);
		}
	}
	
	public void emmigrateToServer(int idx) {
		
		MigratableProcess ps = (MigratableProcess)_pmap.get(idx);
		
		if (ps == null) {
			println("WARNING: try to migrate a non-existing pid(" + idx + ")!");
		} else {
			ps.suspend();
		}
		
		MessageStruct msg  = new MessageStruct(3, ps);
		try {
			_cltMgr.sendMsg(msg);
		} catch (IOException e) {
			System.out.println("Network problem. Cannot migrate now.");
			if (ps != null) {
				ps.resume();
			}
			return;
		} 
		
		if (ps != null) {
			deleteProcess(idx);
			System.out.println("Process " + idx + " has been emmigrated to server successfully!");
			display();
		}
		
	}
	
	public void immigrateFromServer(MigratableProcess proc) {
		proc.resume();
		new Thread(proc).start();
		
		addProcess(proc);
		proc._pid = getPid(proc);
		System.out.println("New process immigrated! PID: " + getPid(proc));
	}
/* ================== Interfaces for ClientManager end ==================*/
	
/* ================== Process info manage methods begin ==================*/
	private void addProcess(MigratableProcess ps) {
		_pmap.put(Integer.valueOf(_pidCnt.getAndIncrement()), ps);
	}
	
	private boolean deleteProcess(int idx) {
		if (_pmap.remove(Integer.valueOf(idx)) == null) {
			println("delete failed!");
			return false;
		}
		return true;
	}
	
	private int getPid(MigratableProcess ps) {
		for (Map.Entry<Integer, MigratableProcess> entry : _pmap.entrySet()) {
		    if (entry.getValue() == ps) {
		    	return entry.getKey().intValue();
		    }
		}
		return -1;
	}
	
	private MigratableProcess getProcess(int pid) {
		return (MigratableProcess)_pmap.get(Integer.valueOf(pid));
	}
/* ================== Process info manage methods end ==================*/
	
	/*
	 * Internal debug print method.
	 */
	private void println(String msg) {
		System.out.println("ProcessManager: " + msg);
	}
}
