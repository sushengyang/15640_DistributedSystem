/**
 * 
 */
package edu.cmu.andrew.ds.network;

import java.io.Serializable;

/**
 * MessageStruct
 * 
 * A structure for communicating between server and client. Two fields indicate 
 * the message type(_code) and message body(_content).
 * 
 * Message types and description: 
 * 		type	description						direction
 * 		0		request process info			server -> client
 * 		1		respond process info			client -> server
 * 		2		request to migrate a process	server -> client
 * 		3		emigrate a process				client -> server
 * 		4		immigrate a process				server -> client
 * 		5		set client id					server -> client
 * 
 * Examples:
 * 		1. display the info of all the running processes on all clients
 * 				server			client
 * 				0		->
 * 						<-		1
 * 		2. migrate a process
 * 				server			client
 * 				2		->
 * 						<-		3
 * 				4		-> 
 * 
 * @author KAIILANG CHEN(kailianc)
 * @author YANG PAN(yangpan)
 * @version 1.0
 *
 */
public class MessageStruct extends Object implements Serializable {
	private static final long serialVersionUID = 3532734764930998421L;
	public int _code;
	public Object _content;
	
	public MessageStruct() {
		this._code = 0;
		this._content = null;
	}
	
	public MessageStruct(int code, Object content) {
		this._code = code;
		this._content = content;
	}
}
