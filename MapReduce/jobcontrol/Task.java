package jobcontrol;

import hdfs.KPFile;

import java.util.HashMap;

/**
 * Task
 * 
 * Store all the information of a task and provide some convenient methods to get the information of 
 * this task.
 * 
 * Note: we use a different description from Hadoop. Task is larger work and the job is smaller work.
 */
public class Task {
	public enum TaskPhase{
		NONE,
		MAP_READY,
		MAP,
		REDUCE_READY,
		REDUCE,
		FINISH
	}
	public String _taskName;
	public KPFile _mrFile;
	public HashMap<Integer, JobInfo> _mapJobs = new HashMap<Integer, JobInfo>();
	public HashMap<Integer, JobInfo> _reduceJobs = new HashMap<Integer, JobInfo>();
	public TaskPhase _phase = TaskPhase.NONE;
	
	public Task(String taskName) {
		_taskName = taskName;
	}	
	
	/*
	 * Check if this task is finished, no matter it's map or reduce.
	 */
	public boolean phaseComplete() {
		if (_phase == TaskPhase.MAP) {
			for (Integer jobId: _mapJobs.keySet()) {
				JobInfo job = _mapJobs.get(jobId);
				if (job._type != JobInfo.JobType.MAP_COMPLETE) {
					return false;
				}
			}
			return true;
		}
		if (_phase == TaskPhase.REDUCE) {
			for (Integer jobId: _reduceJobs.keySet()) {
				JobInfo job = _reduceJobs.get(jobId);
				if (job._type != JobInfo.JobType.REDUCE_COMPLETE) {
					return false;
				}
			}
			return true;
		}
		System.out.println(_taskName + " is not a task in map or reduce phase!");
		return false;
	}
	
	/* 
	 * Clear the task. Used when a slave is down and reschedule this task. 
	 */
	public void reset() {
		synchronized (this) {
			_phase = TaskPhase.MAP_READY;
		}
		
		for (JobInfo job: _mapJobs.values()) {
			synchronized (job) {
				job._type = JobInfo.JobType.MAP_READY;
				job._sid = -1;
				job._outputFile.clear();
			}
		}
		
	}
}
