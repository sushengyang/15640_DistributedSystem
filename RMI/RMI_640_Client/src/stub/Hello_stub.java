/**
 * 
 */
package stub;

import rmi.RMIException;
import rmi.RMIMessage;
import rmi.RMIObjectReference;
import rmi.client.StubBase;

/**
 * @author PY
 *
 */
public class Hello_stub extends StubBase {
	
	public String haha(String a) throws RMIException {
		Object[] content = {_ror, "haha", a};
		Object retVal = invokeRemote(content, true);
		
		if (!(retVal instanceof String)) {
			throw new RMIException("Invalid return value type! String expected, but " + retVal.getClass().getName() + " received.");
		}
		return (String)retVal;
	}
	
	public int test(String a, String b, int c, double[] d) throws RMIException {
		Object[] content = {_ror, "test", a, b, c, d};
		Object retVal = invokeRemote(content, true);
		if (!(retVal instanceof Integer)) {
			throw new RMIException("Invalid return value type! int expected, but " + retVal.getClass().getName() + " received.");
		}
		return ((Integer)retVal).intValue();
	}
	
	public MyName_stub whatsMyName() throws RMIException {
		Object[] content = {_ror, "whatsMyName"};
		Object retVal = invokeRemote(content, true);
		
		if (!(retVal instanceof RMIObjectReference)) {
			throw new RMIException("Invalid return value type! RMIObjectReference expected, but " 
					+ retVal.getClass().getName() + " received.");
		}
		
		MyName_stub myname = (MyName_stub)(((RMIObjectReference)retVal).localize());
		return myname;
	}
}
