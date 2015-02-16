/**
 * 
 */
package rmi.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import rmi.RMIException;
import rmi.RMIObjectReference;

/**
 * RMIServerRegistry
 * 
 * The registry in server. Responsible for the maintenance of the running remote instance.
 * It is singleton and can be instantiated and got by invoking `sharedRegistry`.
 *  
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class RMIServerRegistry {
	/* singleton */
	private static RMIServerRegistry _sharedRegistry = null;
	
	/* hash map storing the registered services */
	private HashMap <String, RMIService> _registeredServices = null;
	
	/* hash map storing the services generated in remote invocation */
	private HashMap <Integer, RMIService> _referencedServices = null;
	
	public static RMIServerRegistry sharedRegistry() {
		if (_sharedRegistry == null) {
			_sharedRegistry = new RMIServerRegistry();
		}
		return _sharedRegistry;
	}
	
	/* 
	 * Set private to avoid being instantiated by mistake.
	 */
	private RMIServerRegistry() {
		_registeredServices = new HashMap<String, RMIService>();
		_referencedServices = new HashMap<Integer, RMIService>();
	}
	
	/* 
	 * Called at the start of server program.
	 */
	public void bind(String name, RMIService obj) throws RMIException {
		_registeredServices.put(name, obj);
	}
	
	/*
	 * Get the service from registered services
	 */
	public RMIService getObj(String name) {
		return _registeredServices.get(name);
	}

	public String[] getBindedList() {
		
		ArrayList<String> names = new ArrayList<String>();
		Iterator<String> kit = _registeredServices.keySet().iterator();
		while (kit.hasNext()) {
			String key = kit.next();
			names.add(key);
		}
		return (String[]) names.toArray();
	}
	
	/*
	 * Add a new service generated in the remote invocation. Store it in hash map 
	 * for future invocation.
	 */
	public void addReferencedService(RMIService refSvc) {
		_referencedServices.put(Integer.valueOf(refSvc._rorID), refSvc);
	}
	
	/*
	 * Get a referenced service
	 */
	public RMIService getReferencedService(int rorID) {
		return _referencedServices.get(Integer.valueOf(rorID));
	}
	
	/*
	 * When invoking a remote method, search it in referenced services first to see if it's 
	 * a referenced object. If not, search it in registered services.
	 */
	public RMIService getObjByROR(RMIObjectReference ror) {
		RMIService ret = null;
		if ((ret = _referencedServices.get(ror._remoteID)) == null) {
			ret = _registeredServices.get(ror._objName);
		}
		return ret;
	}
}
