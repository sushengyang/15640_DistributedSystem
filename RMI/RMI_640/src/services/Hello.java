/**
 * 
 */
package services;

import rmi.server.RMIService;

/**
 * @author PY
 *
 */
public class Hello extends RMIService {
	public String haha(String a) {
		return "yohaha: " + a;
	}
	
	public int test(String a, String b, int c, double[] d) {
		System.out.println("in test~: " + a+b);
		for (double ch: d) {
			System.out.println(ch);
		}
		return c+10;
	}
	
	public MyName whatsMyName() {
		MyName name = new MyName();
		return name;
	}
}
