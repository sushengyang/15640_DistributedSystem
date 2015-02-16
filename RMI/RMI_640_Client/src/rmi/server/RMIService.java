/**
 * 
 */
package rmi.server;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * RMIService
 * 
 * The base class of all classes that can be remotely invoked. If you want to add a new
 * service, your service class should inherit this base class.
 *  
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public abstract class RMIService {
	private static volatile AtomicInteger RORID = new AtomicInteger(0);
	public int _rorID;
	
	public RMIService() {
		this._rorID = RORID.getAndDecrement();
	}
}
