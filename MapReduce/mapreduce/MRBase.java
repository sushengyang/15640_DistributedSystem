/**
 * 
 */
package mapreduce;

import java.util.Iterator;

/**
 * MRBase
 * 
 * The interface for user-defined map-reduce class. All developers should implement this interface 
 * to write their map and reduce methods.
 *
 */

public interface MRBase {
	/**
	 * The map method.
	 * 
	 * @param key The key of this input pair.
	 * @param value The value of this input pair.
	 * @param output The container that the output pair should be put in.
	 */
	public void map(String key, String value, PairContainer output);
	
	/**
	 * The reduce method.
	 * 
	 * @param key The key of this input pair.
	 * @param value The value of this input pair.
	 * @param output The container that the output pair should be put in.
	 */
	public void reduce(String key, Iterator<String> values, PairContainer output);
}

