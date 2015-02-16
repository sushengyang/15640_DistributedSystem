package edu.cmu.andrew.ds.ps;

import java.io.Serializable;

/**
 * MigratableProcess
 * 
 * An abstract class for all the migratable processes, in which suspend and resume 
 * functions before/after migration are provided.
 * 
 *
 * @author KAIILANG CHEN(kailianc)
 * @author YANG PAN(yangpan)
 * @version 1.0
 * 
 */

public abstract class MigratableProcess implements Runnable, Serializable {
	private static final long serialVersionUID = -7112741340544311283L;
	public int _pid = -1;
	/*
	 *  This method will be called before the object is serialized. 
	 *  It affords an opportunity for the process to enter a known safe state.
	 */
	public abstract void suspend();
	
	/*
	 * This method will be called after migration. Resume all the work that was 
	 * suspended.
	 */
	public abstract void resume();
	
	
	/*
	 *  This method is used for debugging. It can print the class name of the 
	 *  process as well as the original set of arguments with which it was called. 
	 */
	public abstract String toString();
	
}
