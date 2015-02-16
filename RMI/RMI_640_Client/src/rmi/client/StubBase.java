/**
 * 
 */
package rmi.client;

import rmi.RMIException;
import rmi.RMIMessage;
import rmi.RMIObjectReference;
import rmi.RMIMessage.RMIMsgType;

/**
 * StubBase
 * 
 * The base class of all the stubs in client. All stubs should inherit from this class.
 * Examples are in stub package.
 * 
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class StubBase {
	public RMIObjectReference _ror;
	
	/**
	 * Wrap the invocation of a remote method for stub. All stubs can wrap the content and call this method
	 * to invoke remotely. Also provides some preliminary check for the return message.
	 * 
	 *   @param content {ROR, method name, args...}
	 *   @param hasRetVal Set to true if this method has return value (non-void). Set to false otherwise.
	 *   @return the return value (if any) or null 
	 */
	public Object invokeRemote(Object[] content, boolean hasRetVal) throws RMIException {
		RMIMessage outMsg = new RMIMessage();
		outMsg._type = RMIMsgType.CALL;
		outMsg._content = content;
		RMIMessage inMsg = RMIClientNetworkMgr.sendAndReceive(_ror._svrIP, _ror._svrPort, outMsg);
		
		if (hasRetVal == true) {
			if (inMsg == null) {
				throw new RMIException("Invalid return value (null)");
			}
		}
		
		if (inMsg._type == RMIMsgType.EXCEPTION) {
			throw new RMIException("Server internal error");
		}
		return inMsg._content;
	}
}
