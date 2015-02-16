/**
 * 
 */
package main;

import rmi.RMIException;
import rmi.RMIObjectReference;
import rmi.client.RMIClientRegistry;
import stub.StudentList_stub;
import stub.Student_stub;

/**
 * RMIClient
 * 
 * The main entrance of the client program. 
 * 
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class RMIClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RMIClientRegistry registry = new RMIClientRegistry();
		
		try {
			RMIObjectReference ror = null;
			
			if(args.length > 1) {
				ror = registry.lookup("StudentList", args[0], args[1]);
			} else {
				ror = registry.lookup("StudentList", null, null);
			}
			StudentList_stub stuList = (StudentList_stub)ror.localize();
			
			/* remotely invoke method to generate new student in StudentList */
			stuList.generateNewStudent("Yan Pan", 100);
			stuList.generateNewStudent("Kailiang Chen", 100);
			stuList.generateNewStudent("Kasden Greg", 59);
			
			/* Get the first student in the list, an example of pass by reference (because Student is subclass 
			 * of RMIService.
			 * 
			 * Then we remotely invoke two methods to modify it and two to get the value of it. The "getName"
			 * and "getScore" methods are the examples of pass by value (because String and int are not subclass
			 * of RMIService.
			 */
			Student_stub yp = stuList.getFirstStudent();
			System.out.println("Before name modify: " + yp.getName() + " has score " + yp.getScore());
			yp.setName("Yang Pan");
			System.out.println("After name modify: " + yp.getName() + " has score " + yp.getScore());
			yp.setScore(80); /* :( */
			System.out.println("After score modify: " + yp.getName() + " has score " + yp.getScore());
			
			/* to illustrate the student is really passed by reference, we get a new remote object reference
			 * here to see its value
			 */
			Student_stub new_yp = stuList.getFirstStudent();
			System.out.println("Finally: " + new_yp.getName() + " has score " + new_yp.getScore());
		} catch (RMIException e1) {
			e1.printStackTrace();
		}
	}

}
