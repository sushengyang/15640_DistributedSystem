/**
 * 
 */
package stub;

import rmi.RMIException;
import rmi.RMIObjectReference;
import rmi.client.StubBase;
import stub.Student_stub;

/**
 * StudentList_stub
 * 
 * a service stub class provides register student's name and score and get first student's info
 * 
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class StudentList_stub extends StubBase {
	
	public void generateNewStudent(String name, int score) throws RMIException {
		Object[] content = {_ror, "generateNewStudent", name, score};
		invokeRemote(content, false);
	}
	
	public Student_stub getFirstStudent() throws RMIException {
		Object[] content = {_ror, "getFirstStudent"};
		Object retVal = invokeRemote(content, true);
		
		if (!(retVal instanceof RMIObjectReference)) {
			throw new RMIException("Invalid return value type! RMIObjectReference expected, but " 
					+ retVal.getClass().getName() + " received.");
		}
		
		Student_stub student = (Student_stub)(((RMIObjectReference)retVal).localize());
		return student;
	}
}
