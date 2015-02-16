/**
 * 
 */
package rmi;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import rmi.client.StubBase;

/**
 * RMIObjectReference
 * 
 * The class of a remote object reference. Any client hold this can invoke a method remotely.
 * A client can get a ROR by either calling `lookup` of RMIClientRegistry or invoking a remote method
 * with the return value of RMIService type.
 *  
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class RMIObjectReference implements Serializable {
	private static final long serialVersionUID = 917239297620546165L;
	public String _svrIP;	/* the server in which this remote object is located in */
	public int _svrPort;	/* listening port of the server */
	public String _objName;	/* the object name of this remote object, package name excluded */
	public int _remoteID;	/* used in server to search for this object */
	
	public RMIObjectReference() {
		this._svrIP = "";
		this._svrPort = 0;
		this._objName = "";
		this._remoteID = -1;
	}
	
	/* 
	 * After receiving a ROR from server, use this method to get an instance of local stub 
	 * corresponding to that object. 
	 */
	public StubBase localize() throws RMIException {
		try {
			Class<?> clazz = Class.forName("stub."+_objName+"_stub");
			Constructor<?> cons = clazz.getConstructor();
			StubBase stub = (StubBase)cons.newInstance(new Object[] {});
			
			/* attach this ROR to the stub */
			stub._ror = this;
			
			return stub;
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException 
				| NoSuchMethodException | SecurityException 
				| ClassNotFoundException e) {
			throw new RMIException("Failed to localize for " + "stub."+_objName+"_stub");
		}
	}
}
