/**
 * 
 */
package stub;

import rmi.RMIException;
import rmi.client.StubBase;

/**
 * @author PY
 *
 */
public class MyName_stub extends StubBase {
	public String getName() throws RMIException {
		Object[] content = {_ror, "getName"};
		Object retVal = invokeRemote(content, true);
		
		if (!(retVal instanceof String)) {
			throw new RMIException("Invalid return value type! String expected, but " + retVal.getClass().getName() + " received.");
		}
		return (String)retVal;
	}
	
	public void setName(String name) throws RMIException {
		Object[] content = {_ror, "setName", name};
		invokeRemote(content, false);
	}
}
