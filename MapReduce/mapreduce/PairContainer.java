package mapreduce;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author PY
 *
 */
public class PairContainer implements Serializable {
	private static final long serialVersionUID = 4824504881487447089L;
	
	public ArrayList<Pair> _list = new ArrayList<Pair>();

	
	public PairContainer() {
	}
	
	public PairContainer(Iterator<Pair> itor) {
		while(itor.hasNext()) {
			_list.add(itor.next());
		}
	}

	public void emit(Pair pair) {
		_list.add(pair);
	}
	
	public void emit(String key, String val) {
		Pair pair = new Pair(key, val);		
		emit(pair);
	}
	
	/*
	 * Merge all the pairs with the same key into a pair (value is an array).
	 */
	@SuppressWarnings("unchecked")
	public void mergeSameKey() {
		String currentKey = null;
		
		ArrayList<String> list = null;
		ArrayList<Pair> newList = new ArrayList<Pair>();
		
		Collections.sort(_list);
		
		for(Pair pair : _list) {
			String key = pair.getFirst();
			
			if(key.equals(currentKey)) {
				Iterator<String> val = pair.getSecond();
				while(val.hasNext()) {
					list.add(val.next());
				}
			} else {
				if(currentKey != null) {
					Pair newPair = new Pair(currentKey, list.iterator());			
					newList.add(newPair);
				}
				list = new ArrayList<String>();
				Iterator<String> val = pair.getSecond();
				while(val.hasNext()) {
					list.add(val.next());
				}
				currentKey = key;
			}
		}
		if(currentKey != null) {
			Pair newPair = new Pair(currentKey, list.iterator());			
			newList.add(newPair);
		}
		
		Collections.sort(newList);
		_list = newList;
	}
	
	public Iterator<Pair> getInitialIterator() {
		return _list.iterator() ;
	}
	
	/*
	 * Convert all the pairs to a string so that it can be saved in a file.
	 * 
	 * PairContainer => key1:value1,value2,value3;key2:value1,value2,value3;
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<Pair> itor = _list.iterator();
		int i = 0;
		while(itor.hasNext()) {
			Pair pair = itor.next();
			if(i > 0) {
				sb.append("\n");
			}
			sb.append(pair.toString());
			i++;
		}
		return sb.toString();
	}
	
	/*
	 * Restore from a string read in from a file into this container.
	 * 
	 * key1:value1,value2,value3;key2:value1,value2,value3; => PairContainer 
	 */
	public void restoreFromString(String str) {
		if(str == null) {
			return;
		}
		String[] pairStrs = str.split("\n");
		for(String pairStr : pairStrs) { 
			String[] parts = pairStr.split(":");
			String key = parts[0];
			String valueList = parts[1];
			if(valueList != null) {
				String[] values = parts[1].split(",");
				for(String value : values) {
					Pair pair = new Pair(key, value);
					_list.add(pair);
				}
			}
		}
		mergeSameKey();
	}
}
