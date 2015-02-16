/**
 * 
 */
package mapreduce;

import java.io.Serializable;

/**
 * SlaveTracker
 * 
 * Store the status of a slave. Sent as the content of a heart beat from map-reduce slave to master
 *
 */
public class SlaveTracker implements Serializable {

	private static final long serialVersionUID = 2613707869955670269L;
	public int _sid = 0;
	public int _workingCnt = 0;
	public int _queueingCnt = 0;
	
	public SlaveTracker(int sid) {
		_sid = sid;
	}
}
