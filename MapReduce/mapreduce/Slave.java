/**
 * 
 */
package mapreduce;

import hdfs.KPFSSlave;
import hdfs.KPFSSlaveInterface;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import jobcontrol.JobInfo;
import network.Message;
import network.MsgHandler;
import network.NetworkFailInterface;
import network.NetworkHelper;

/**
 * Slave
 * 
 * The main thread of a map-reduce slave.
 * 
 */
public class Slave implements NetworkFailInterface {
	private static Slave _sharedSlave;

	public static Slave sharedSlave() {
		if (_sharedSlave == null) {
			_sharedSlave = new Slave();
		}
		return _sharedSlave;
	}

	/* The socket to map-reduce master */
	public Socket _socket;
	
	/* All the queueing jobs */
	public ArrayList<JobInfo> _waitingJob = new ArrayList<JobInfo>();
	
	/* All the working jobs */
	public ArrayList<JobInfo> _workingJob = new ArrayList<JobInfo>();
	
	/* Ready to receive job and report the status to master */
	private boolean _ready = false;

	private Slave() {
		try {
			_socket = new Socket(GlobalInfo.sharedInfo().MasterHost,
					GlobalInfo.sharedInfo().MasterPort);
			
			/* tell master my sid */
			ObjectOutputStream out = new ObjectOutputStream(_socket.getOutputStream());
			out.writeObject(GlobalInfo.sharedInfo()._sid);
			
			/* check if this sid is valid from master */
			ObjectInputStream inStream = new ObjectInputStream(_socket.getInputStream());
			String res = (String) inStream.readObject();
			if (res.equals("no")) {
				System.err.println("Slave with sid " + GlobalInfo.sharedInfo()._sid + " is already connected to master!");
				System.exit(-1);
			}
			System.out.println("Connected to master");
			
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Connection failed!");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void start() {
		/* start receiving messages from master in another thread */
		MsgHandler handler = new MsgHandler(GlobalInfo.sharedInfo()._sid, _socket, this);
		Thread t = new Thread(handler);
		t.start();
		
		
		/* start HDFS as data node */
		try {
			KPFSSlave obj = new KPFSSlave();
			KPFSSlaveInterface stub = (KPFSSlaveInterface) UnicastRemoteObject
					.exportObject(obj, 0);

			Registry _registry = null;
			try {
				_registry = LocateRegistry
						.getRegistry(GlobalInfo.sharedInfo().getDataSlavePort(GlobalInfo.sharedInfo()._sid));
				_registry.list();
			} catch (RemoteException e) {
				_registry = LocateRegistry.createRegistry(GlobalInfo.sharedInfo().getDataSlavePort(GlobalInfo.sharedInfo()._sid));
			}
				
			_registry.bind("DataSlave", stub);

			System.out.println("HDFS ready");
		} catch (Exception e) {
			System.err.println("Failed to open HDFS service!");
			e.printStackTrace();
		}
		
		/* start the coordinator of workers */
		SlaveWork work = new SlaveWork(null, false);
		work.start();
		
		/* ready to work */
		_ready = true;

		/* sending heart beat to master periodically */
		while (true) {
			if (!_ready) {
				continue;
			}
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			SlaveTracker slvTracker = new SlaveTracker(GlobalInfo.sharedInfo()._sid);
			synchronized (_waitingJob) {
				slvTracker._queueingCnt = _waitingJob.size();
			}
			synchronized (_workingJob) {
				slvTracker._workingCnt = _workingJob.size();
			}
			
			Message heartbeat = new Message(GlobalInfo.sharedInfo()._sid, Message.MessageType.SLAVE_HEARTBEAT);
			heartbeat._content = slvTracker;
			
			try {
				NetworkHelper.send(_socket, heartbeat);
			} catch (IOException e) {
				System.err.println("Connection broken. Please restart this slave!");
				System.exit(-1);
			}
		}
	}
	
	/*
	 * Receive a new job from master. Put it into the queue. The coordinator will set them work.
	 */
	public void newJob(JobInfo job) {
//		System.out.println("get a new job: " + job._jobId + " " + job._taskName
//				+ " " + job._type);

		/* update phase */
		if (job._type == JobInfo.JobType.MAP_READY) {
			job._type = JobInfo.JobType.MAP_QUEUE;
		} else if (job._type == JobInfo.JobType.REDUCE_READY) {
			job._type = JobInfo.JobType.REDUCE_QUEUE;
		} else {
			System.out.println("WARNING: try to queue a job that is not in ready phase! (" + job._type + ")");
			return;
		}
		
		synchronized (_waitingJob) {
			_waitingJob.add(job);
		}
		
	}
	
	public void terminateJob(String taskName, Integer jobId) {
		synchronized (_waitingJob) {
			for (JobInfo job: _waitingJob) {
				if (job._jobId == jobId) {
					job._type = JobInfo.JobType.TERMINATED;
					System.out.println(taskName + " (" + jobId + ") terminated." );
					break;
				}
			}
		}
	}
	
	/*
	 * Report to the master that the status of a job is changed.
	 */
	public synchronized void updateJobInfo(JobInfo job, Message.MessageType type) {
		Message msg = new Message(GlobalInfo.sharedInfo()._sid, type);
		msg._content = job;
		synchronized (job) {
			try {
				NetworkHelper.send(_socket, msg);
			} catch (IOException e) {
				System.err.println("Connection broken. Please restart this slave!");
				System.exit(-1);
			}
		}
		
		if (job._type==JobInfo.JobType.MAP_COMPLETE || job._type==JobInfo.JobType.REDUCE_COMPLETE) {
			synchronized(_workingJob) {
				_workingJob.remove(job);
			}
			
			System.out.println("finish job: " + job._jobId + " " + job._taskName
					+ " " + job._type);
		}
	}

	/*
	 * Map-reduce master is down.
	 */
	@Override
	public void networkFail(int sid) {
		System.err.println("Connection broken. Please restart this slave!");
		System.exit(-1);
	}
}
