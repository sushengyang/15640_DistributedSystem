/**
 * 
 */
package mapreduce;

import hdfs.KPFSException;
import hdfs.KPFSMaster;
import hdfs.KPFSMasterInterface;
import hdfs.KPFSSlave;
import hdfs.KPFSSlaveInterface;
import hdfs.KPFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import jobcontrol.JobInfo;
import jobcontrol.JobManager;
import jobcontrol.Task;
import network.Listen;
import network.Message;
import network.NetworkFailInterface;
import network.NetworkHelper;

/**
 * Master
 * 
 * The main thread of map-reduce master. Read in the user input and handle the commands.
 * Also provide the handlers for messages from slaves.
 * Maintain all the running tasks.
 * 
 */
public class Master implements NetworkFailInterface {
	private static Master _sharedMaster;

	public static Master sharedMaster() {
		if (_sharedMaster == null) {
			_sharedMaster = new Master();
		}
		return _sharedMaster;
	}

	/* map the sid to the socket corresponding to that slave */
	public HashMap<Integer, Socket> _slvSocket = new HashMap<Integer, Socket>();
	
	/* map the task name to the task */
	public volatile HashMap<String, Task> _tasks = new HashMap<String, Task>();
	
	/* map the sid to the tracker of that slave */
	private volatile HashMap<Integer, SlaveTracker> _slvTracker = new HashMap<Integer, SlaveTracker>();

	/* the data master service instance */
	private KPFSMasterInterface _kpfsMaster;
	
	/* the data node service instance */
	private KPFSSlaveInterface _kpfsSlave;

	private Master() {
		GlobalInfo.sharedInfo()._sid = 0;
		Listen l = new Listen(GlobalInfo.sharedInfo().MasterPort);
		l.start();
	}

	
	
	public void start() {
		/* start HDFS as master node*/
		try {
			_kpfsMaster = new KPFSMaster();
            KPFSMasterInterface stub = (KPFSMasterInterface) UnicastRemoteObject.exportObject(_kpfsMaster, 
            		GlobalInfo.sharedInfo().DataMasterPort);
            Registry registry = LocateRegistry.createRegistry(GlobalInfo.sharedInfo().DataMasterPort);;
            registry.rebind("DataMaster", stub);
            System.out.println("KPFS master ready");
        } catch (Exception e) {
            System.err.println("Failed to establish as HDFS master!");
            e.printStackTrace();
            System.exit(-1);
        }
		
		/* start HDFS as data node */
		try {
			_kpfsSlave = new KPFSSlave();
            KPFSSlaveInterface stub = (KPFSSlaveInterface) UnicastRemoteObject.exportObject(_kpfsSlave, 
            		GlobalInfo.sharedInfo().DataSlavePort);
            Registry registry = LocateRegistry.createRegistry(GlobalInfo.sharedInfo().getDataSlavePort(0));;
            registry.rebind("DataSlave", stub);
            System.out.println("KPFS data node ready");
        } catch (Exception e) {
            System.err.println("Failed to establish as HDFS master!");
            e.printStackTrace();
            System.exit(-1);
        }
		
		/* show the input rules */
		help();
		
		/* read in the user commands and handle them */
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String line = null;
			try {
				line = br.readLine();
				String[] cmd = line.split(" ");
				inputHandler(cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/* ============ Handlers for user commands BEGIN ============ */
	public void inputHandler(String[] cmd) {
		switch (cmd[0]) {
		case "new":
			newTask(cmd[1]);
			break;
		case "slavestatus":
			showSlave();
			break;
		case "alltasks":
			showAllTask();
			break;
		case "show":
			showTask(cmd[2]);
			break;
		case "aliveslaves":
			for (Integer sid: _slvSocket.keySet()) {
				System.out.println(sid);
			}
			break;
		case "allfiles":
			((KPFSMaster) _kpfsMaster).debug();
			break;
		case "end":
			terminateTask(cmd[1]);
			break;
		default:
			System.out.println("Invalid input. Please follow the command instructions.");
			help();
		}
		
	}
	
	private void showSlave() {
		System.out.println("The status of all slaves (including those who dies, for debug): ");
		for (SlaveTracker tracker: _slvTracker.values()) {
			System.out.println("\tSlave " + tracker._sid + ": waiting task(" + tracker._queueingCnt + "), working task(" + tracker._workingCnt + ")");
		}
	}
	
	private void showAllTask() {
		System.out.println("The status of all tasks: ");
		for (String taskName: _tasks.keySet()) {
			showTask(taskName);
		}
		if (_tasks.isEmpty()) {
			System.out.println("\tNo task is currently running.");
		}
	}
	
	private void help() {
		System.out.println("\tnew [taskname]\tStart a new task.");
		System.out.println("\tslavestatus\tShow the status of all slaves.");
		System.out.println("\talltasks\tShow the status of all tasks.");
		System.out.println("\tshow [taskname]\tShow the status of a specific task.");
		System.out.println("\taliveslaves\tShow the sid of all the living slaves.");
		System.out.println("\tallfiles\tShow all the files in KPFS.");
		System.out.println("\tend [taskname]\tTerminate a specific task.");
	}
	
	private void showTask(String taskName) {
		Task task = _tasks.get(taskName);
		if (task == null) {
			return;
		}
		System.out.println("\t" + task._taskName + " " + task._phase);
		if (task._phase == Task.TaskPhase.MAP) {
			System.out.println("\t\tJobs:");
			for (JobInfo job: task._mapJobs.values()) {
				System.out.print("\t\t\tJobId: " + job._jobId);
				System.out.print(", Type: " + job._type);
				System.out.println(", in slave: " + job._sid);
			}
		} else if (task._phase == Task.TaskPhase.REDUCE) {
			System.out.println("\t\tJobs:");
			for (JobInfo job: task._reduceJobs.values()) {
				System.out.print("\t\t\tJobId: " + job._jobId);
				System.out.print(", Type: " + job._type);
				System.out.println(", in slave: " + job._sid);
			}
		}
	}

	public void newTask(String taskName) {
		String rootDir = GlobalInfo.sharedInfo().MasterRootDir;
		String jarPath = rootDir + taskName + "/" + GlobalInfo.sharedInfo().UserDirName + "/" + taskName + ".jar";
		String inputPath = rootDir + taskName + "/" + GlobalInfo.sharedInfo().UserDirName + "/" + taskName + ".txt";
		File jarf = new File (jarPath);
		File inf = new File (inputPath);
		if (!jarf.exists() || !inf.exists()) {
			System.out.println("Please put " + taskName + ".txt and " + taskName + ".jar into the right directory and try again!" );
			return;
		}
		
		Task currentTask = new Task(taskName);
		_tasks.put(taskName, currentTask);

		
		String interDir = rootDir + taskName + "/" + GlobalInfo.sharedInfo().IntermediateDirName
				+ "/";
		String chunkDir = rootDir + taskName + "/" + GlobalInfo.sharedInfo().ChunkDirName + "/";
		String resultDir = rootDir + taskName + "/" + GlobalInfo.sharedInfo().ResultDirName
				+ "/";

		/* create the folder to store intermediate files */
		(new File(interDir)).mkdirs();
		(new File(chunkDir)).mkdirs();
		(new File(resultDir)).mkdirs();
		
		
		long jarSize = jarf.length();
		KPFile jarFile = new KPFile(taskName + "/" + GlobalInfo.sharedInfo().UserDirName + "/", taskName + ".jar");
		try {
			/* 0 is the id of master */
			_kpfsMaster.addFileLocation(jarFile.getRelPath(), 0, jarSize);
		} catch (RemoteException e) {
			System.err.println("Failed to add file!");
			e.printStackTrace();
		}
		
		currentTask._mrFile = jarFile;
		
		System.out.println("Spliting file for task " + taskName + ": " + inputPath);
		ArrayList<String> files = ((KPFSMaster) _kpfsMaster).splitFile(inputPath,
					GlobalInfo.sharedInfo().FileChunkSizeB, taskName + "/" + GlobalInfo.sharedInfo().ChunkDirName + "/", taskName);
		
		int jobId = 0;
		for (String fn : files) {
			JobInfo job = new JobInfo(++jobId, taskName);
			job._mrFile = jarFile;
			job._sid = getFreeSlave();
			job._type = JobInfo.JobType.MAP_READY;

			ArrayList<KPFile> list = new ArrayList<KPFile>();
			if(fn.contains("/")) {
				String[] parts = fn.split("/");
				KPFile kpfile = new KPFile(taskName + "/" + GlobalInfo.sharedInfo().ChunkDirName + "/", parts[parts.length - 1]);
				list.add(kpfile);
			}
			job._inputFile = list;
			
			/* duplicate the files to some other data nodes */
			try {
				((KPFSMaster) _kpfsMaster).duplicateFiles(list, _slvSocket.keySet().toArray());
			} catch (IOException | KPFSException e) {
				System.out.println("ERROR: failed to duplicate files!");
				e.printStackTrace();
			}

			currentTask._mapJobs.put(jobId, job);
		}
		
		System.out.println("Sending jobs of " + taskName + " out to slaves...");
		currentTask._phase = Task.TaskPhase.MAP;
		JobManager.sharedJobManager().sendJobs(currentTask._mapJobs.values());
		System.out.println("Job assignment of " + taskName + " finished.");
	}
	
	public void terminateTask(String taskName) {
		Task task = _tasks.get(taskName);
		if (task == null) {
			System.out.println(taskName + " does not exist.");
			return;
		}
		
		Collection<JobInfo> workingJobs = null;
		if (task._phase == Task.TaskPhase.MAP) {
			workingJobs = task._mapJobs.values();
		} else if (task._phase == Task.TaskPhase.REDUCE) {
			workingJobs = task._reduceJobs.values();
		}
		
		synchronized (_tasks) {
			_tasks.remove(taskName);
		}
		
		for (JobInfo job: workingJobs) {
			Message msg = new Message(job._sid, Message.MessageType.TERMINATE_JOB);
			msg._content = new Object[]{taskName, job._jobId};
			try {
				NetworkHelper.send(_slvSocket.get(job._sid), msg);
			} catch (IOException e) {
				networkFail(job._sid);
			}
		}
		System.out.println(taskName + " is terminated.");
	}
	
	/* ============ Handlers for user commands END ============ */
	
	/* ============ Handlers for network messages BEGIN ============ */
	
	public void slaveHeartbeat(int sid, SlaveTracker tracker) {
		synchronized (_slvTracker) {
			_slvTracker.put(sid, tracker);
		}
	}

	public void checkMapCompleted(JobInfo job) {
		Task task = _tasks.get(job._taskName);
		if (task == null) {
			System.out.println("WARNING [checkMapCompleted]: receiving a job that does not belong to any working task!");
			return;
		}
		JobInfo oldJob = task._mapJobs.get(job._jobId);
		if (oldJob == null) {
			return;
		}
		if (task._phase!=Task.TaskPhase.MAP || oldJob==null || oldJob._sid!=job._sid) {
			System.out.println("Getting an old finished job. Ignoring it. " + job._taskName + job._jobId + job._type + " from " + job._sid);
			if (oldJob != null) {
				System.out.println("\tOld job: " + oldJob._taskName + oldJob._jobId + oldJob._type + " from " + oldJob._sid);
			}
			return;
		}

		
		task._mapJobs.put(job._jobId, job);
		
//		System.out.println("Job finished!");
//		job.serialize();
		
		if (task.phaseComplete()) {
			HashMap<Integer, JobInfo> jobs = new HashMap<Integer, JobInfo>();
			
			HashMap<Integer, ArrayList<KPFile>> interFiles = new HashMap<Integer, ArrayList<KPFile>>();
			
			for (JobInfo ji: task._mapJobs.values()) {
				HashMap<Integer, KPFile> files = ji.getInterFilesWithIndex();
				for (Integer idx: files.keySet()) {
					ArrayList<KPFile> farr = interFiles.get(idx);
					if (farr==null) {
						farr = new ArrayList<KPFile>();
						interFiles.put(idx, farr);
					}
					farr.add(files.get(idx));
				}
			}
			
			for (Integer idx: interFiles.keySet()) {
				JobInfo reduceJob = new JobInfo(idx, task._taskName);
				reduceJob._mrFile = task._mrFile;
				reduceJob._sid = getFreeSlave();
				reduceJob._type = JobInfo.JobType.REDUCE_READY;
				reduceJob._inputFile = interFiles.get(idx);
				jobs.put(idx, reduceJob);
			}
			
			task._phase = Task.TaskPhase.REDUCE;
			task._reduceJobs = jobs;
			JobManager.sharedJobManager().sendJobs(task._reduceJobs.values());
		}
		
	}

	public void checkReduceCompleted(JobInfo job) {
		Task task = _tasks.get(job._taskName);
		if (task == null) {
			System.out.println("WARNING [checkReduceCompleted]: receiving a job that does not belong to any working task!");
			return;
		}
		
		JobInfo oldJob = task._reduceJobs.get(job._jobId);
		if (oldJob == null) {
			return;
		}
		if (task._phase!=Task.TaskPhase.REDUCE || oldJob==null || oldJob._sid!=job._sid) {
			System.out.println("Getting an old finished job. Ignoring it. " + job._taskName + job._jobId + job._type + " from " + job._sid);
			if (oldJob != null) {
				System.out.println("\tOld job: " + oldJob._taskName + oldJob._jobId + oldJob._type + " from " + oldJob._sid);
			}
			return;
		}
		
		task._reduceJobs.put(job._jobId, job);
		
		if (task.phaseComplete()) {
			task._phase = Task.TaskPhase.FINISH;
//			_tasks.remove(task._taskName);
			System.out.println("Task " + task._taskName + " is completed! The output files are at: ");
			for (JobInfo ji: task._reduceJobs.values()) {
				System.out.println("\tSlave " + ji._sid + ": " + ji._outputFile.get(0).getRelPath());
			}
		}
		
		
	}
	/* ============ Handlers for network messages BEGIN ============ */

	/* 
	 * Load balancer. Now only randomly choose a slave to do the job.
	 * 
	 * To be continued: use the information of _slvTracker to decide which slave 
	 * is the best one to receive a new job.
	 */
	public synchronized int getFreeSlave() {
		int slv = 0;
		synchronized (_slvSocket) {
			if ((slv = _slvSocket.size()) == 0) {
				System.out.println("Currently all slaves are down. Please restart this system!");
				System.exit(-1);
			}
		}
		Random rand = new Random();
		slv = rand.nextInt(slv);
		
		Integer sid = 0;
		synchronized (_slvSocket) {
			sid = (Integer) _slvSocket.keySet().toArray()[slv];
		}
		return sid.intValue();
	}

	/*
	 * One slave is down. 
	 * 1. Fix the location information of all the affected files and duplicate them into another data node. 
	 * 2. Reschedule all the running tasks.
	 */
	@Override
	public void networkFail(int sid) {
		System.out.println("networkFail: " + sid);
		if (_slvSocket.containsKey(sid) == false) {
			System.out.println("WARNING! no such sid.");
			return;
		}
		
		_slvSocket.remove(sid);
		
		/* remove the metadata of affected files and duplicate them */
		ArrayList<String> deletedFiles = ((KPFSMaster) _kpfsMaster).removeFileInSlave(sid);
		ArrayList<KPFile> toDup = new ArrayList<KPFile>();
		for (String filePath: deletedFiles) {
			String relDir = filePath.substring(0, filePath.lastIndexOf("/") + 1);
			String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
			toDup.add(new KPFile(relDir, fileName));
			System.out.println("File to duplicate: " + filePath);
		}
		try {
			((KPFSMaster) _kpfsMaster).duplicateFiles(toDup, _slvSocket.keySet().toArray());
		} catch (IOException | KPFSException e) {
			e.printStackTrace();
		}
		
		
		for (Task task: _tasks.values()) {
			if (task._phase == Task.TaskPhase.NONE || task._phase == Task.TaskPhase.FINISH) {
				continue;
			}
			
			task.reset();
			for (JobInfo job: task._mapJobs.values()) {
				job._sid = getFreeSlave();
			}
			
			task._phase = Task.TaskPhase.MAP;
			
			System.out.println("\tReschedule " + task._taskName);
		}
		if (_tasks.isEmpty()) {
			System.out.println("ATTENTION: slave " + sid + " is down!");
			return;
		}

		System.out.println("ATTENTION: slave " + sid + " is down! Reschedule all tasks after 5s ...");
		
		for (int i=5; i>=1; --i) {
			System.out.println(i+"...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Rescheduling...");
		boolean scheduled = false;
		for (Task task: _tasks.values()) {
			if (task._phase != Task.TaskPhase.NONE && task._phase != Task.TaskPhase.FINISH) {
				System.out.println("Task " + task._taskName + " is rescheduled.");
				JobManager.sharedJobManager().sendJobs(task._mapJobs.values());
				scheduled = true;
			}
		}
		if (scheduled == false) {
			System.out.println("All tasks are finished. No need to redo the work. If you have files stored "
					+ "on slave " + sid + ", please use 'new' command to start that task again!");
		}
	}
}
