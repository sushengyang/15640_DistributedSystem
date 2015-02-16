/**
 * 
 */
package services;

import rmi.server.RMIService;

/**
 * @author PY
 *
 */
public class MyName extends RMIService {
	String _name;
	public MyName()	 {
		_name = "PY";
	}
	
	public String getName() {
		return _name;
	}
	
	public void setName(String name) {
		_name = name;
	}
}
