/**
 * 
 */
package rmi;

/**
 * RMIException
 * 
 * The exception type only thrown in remote invocation.
 * 
 * @author Yang Pan (yangpan)
 * @author Kailiang Chen (kailianc)
 *
 */
public class RMIException extends Exception {

	private static final long serialVersionUID = -7201124140957888692L;

	public RMIException(String msg) {
		super(msg);
	}

}
