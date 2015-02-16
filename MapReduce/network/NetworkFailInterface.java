/**
 * 
 */
package network;

/**
 * NetworkFailInterface
 * 
 * Implement this interface to handle the situation where a connection to sid is down.
 *
 */
public interface NetworkFailInterface {
	/*
	 * For map-reduce master: one slave is down. 
	 * 1. Fix the location information of all the affected files and duplicate them into another data node. 
	 * 2. Reschedule all the running tasks.
	 * 
	 * For map-reduce slave: master is down.
	 * 1. Terminate self.
	 */
	public void networkFail(int sid);
}
