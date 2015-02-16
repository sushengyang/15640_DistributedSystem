/**
 * 
 */
package jobcontrol;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * JobManager
 * 
 * Encapsulate the operation to sending queue in master.
 *
 */
public class JobManager {
	private static JobManager _sharedJobManager = null;
	public static JobManager sharedJobManager() {
		if (_sharedJobManager == null) {
			_sharedJobManager = new JobManager();
		}
		return _sharedJobManager;
	}
	
	private JobDispatcher _dispatcher = null;
	private Queue<JobInfo> _sendingJobs = null;
	private JobManager() {
		_sendingJobs = new LinkedList<JobInfo>();
		_dispatcher = new JobDispatcher(this);
		_dispatcher.start();
	}
	
	public void sendJob(JobInfo job) {
		synchronized (_sendingJobs) {
			_sendingJobs.add(job);
		}
	}
	
	public void sendJobs(Collection<? extends JobInfo> jobs) {
		synchronized (_sendingJobs) {
			_sendingJobs.addAll(jobs);
		}
	}
	
	public boolean isSendingQueueEmpty() {
		synchronized (_sendingJobs) {
			return _sendingJobs.isEmpty();
		}
	}
	
	public JobInfo getNextJob() {
		synchronized (_sendingJobs) {
			return _sendingJobs.poll();	
		}
	}
	
	public void clearAllJobs() {
		synchronized (_sendingJobs) {
			_sendingJobs.clear();
		}
	}
}
