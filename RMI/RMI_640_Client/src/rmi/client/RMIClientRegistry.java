/**
 * 
 */
package rmi.client;

import rmi.RMIException;
import rmi.RMIMessage;
import rmi.RMIObjectReference;
import rmi.RMIMessage.RMIMsgType;

/**
 * RMIClientRegistry
 * 
 * Registry class in client. Wraps the lookup method in client.
 * 
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class RMIClientRegistry {
	public String _svrIP = ClientConst.SvrIP;
	public int _svrPort = ClientConst.SvrPort;
	
	/*
	 * Look up a ROR of objName from server.
	 */
	public RMIObjectReference lookup (String objName, String ipAddr, String port) throws RMIException {
		RMIMessage msg = new RMIMessage();
		msg._type = RMIMsgType.LOOKUP;
		msg._content = objName;
		RMIMessage inMsg = null;
		
		if(ipAddr == null && port == null) {
			inMsg = RMIClientNetworkMgr.sendAndReceive(_svrIP, _svrPort, msg);
		} else {
			inMsg = RMIClientNetworkMgr.sendAndReceive(ipAddr, Integer.valueOf(port), msg);
		}	
			
		if (inMsg._content == null || !(inMsg._content instanceof RMIObjectReference)) {
			throw new RMIException("Invalid respond from server");
		}
		RMIObjectReference ror = (RMIObjectReference) inMsg._content;
	
		return ror;
	}
}
