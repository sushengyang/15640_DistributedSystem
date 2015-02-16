/**
 * 
 */
package network;

import java.io.Serializable;

/**
 * Message
 * 
 * The message structure used in communication between map-reduce master and slaves.
 * Every message has a type indicating its purposes.
 * 
 * 	type				direction	content				remark
 * 	NEW_JOB				m -> s		JobInfo				assign a new job to slave
 * 	MAP_COMPLETE		s -> m		JobInfo				a map job is completed
 * 	REDUCE_COMPLETE		s -> m		JobInfo				a reduce job is completed
 * 	SLAVE_HEARTBEAT		s -> m		SlaveTracker		send the status of a slave to master				
 *
 */
public class Message implements Serializable {
	private static final long serialVersionUID = 1114115388371865795L;
	
	public enum MessageType {
		NEW_JOB,
		MAP_COMPLETE,
		REDUCE_COMPLETE,
		SLAVE_HEARTBEAT,
		TERMINATE_JOB
	};
	
	public MessageType _type;
	public int _source;
	public Object _content;
	
	public Message() {
		
	}
	public Message(int sid, MessageType type) {
		_type = type;
		_source = sid;
	}
}
