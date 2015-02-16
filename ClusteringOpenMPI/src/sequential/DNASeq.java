package sequential;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class DNASeq {
	private static final String DNA_SEQ = "ACGT";
	
	private int numOfCluster;
	private String ifileName;
	private String ofileName;
	
	private ArrayList<String> dnaList;
	private int[] nearestCentroid;
	
	private HashMap<String, Integer> map;
	
	private String[] curCentroid;
	private String[] newCentroid;
	
	public DNASeq(int numOfCluster, String ifileName, String ofileName) {
    	this.numOfCluster = numOfCluster;
    	this.ifileName = ifileName;
    	this.ofileName = ofileName;
    	
    	this.dnaList = new ArrayList<String>();
    	this.curCentroid = new String[numOfCluster];
    	this.newCentroid = new String[numOfCluster];
    	
    	this.map = new HashMap<String, Integer>();
    }
	
    public void readData() {
    	try {
			BufferedReader br = new BufferedReader(new FileReader(ifileName));
			String line = null;
			while((line = br.readLine()) != null) {
				if(line.isEmpty()) {
					continue;
				}
				dnaList.add(line);
			}
			br.close();			
			this.nearestCentroid = new int[dnaList.size()];
		} catch (NumberFormatException | IOException e) {
		}  
    }
    
    public void writeData() {
    	try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(ofileName));
			for(int i = 0; i < dnaList.size(); i++) {
				map.put(dnaList.get(i), nearestCentroid[i]);
			}
			
			LinkedHashMap<String, Integer> sortedMap = (LinkedHashMap<String, Integer>) sortByValue(map);			
			for(Entry<String, Integer> entry : sortedMap.entrySet()) {
				bw.write(entry.getKey() + "," + entry.getValue());
				bw.newLine();
			}
			
			bw.close();
		} catch (NumberFormatException | IOException e) {
		} 
    }
    
    public void launch() {
    	// Step 1. random centroid
//    	System.out.println("Step 1. random centroid");
    	Random r = new Random();
		for (int i = 0; i < numOfCluster; i++) {
			String dna = dnaList.get(r.nextInt(dnaList.size()));
			//String dna = dnaList.get(i);
			curCentroid[i] = dna;
		}
		
		// Step 2. iterate to converge
//		System.out.println("Step 2. iterate to converge");
		int curStep = 0;
		int maxStep = 500;
		int minError = 0;
		while(curStep < maxStep) {
			
			// Step 2.1 classify points (find nearest centroid for every point)
//			System.out.println("Step 2.1 classify points");
			for (int i = 0; i < dnaList.size(); i++) {
				nearestCentroid[i] = classify(dnaList.get(i));
			}
			
			// Step 2.2 compute new centroid for every class
//			System.out.println("Step 2.2 compute new centroid for every class");
			HashMap<Integer, ArrayList<String>> map = new HashMap<Integer, ArrayList<String>>();
			
			for (int i = 0; i < dnaList.size(); i++) {
				ArrayList<String> list = null;
				if(map.containsKey(nearestCentroid[i])) {
					list = map.get(nearestCentroid[i]);
				} else {
					list = new ArrayList<String>();
				}
				list.add(dnaList.get(i));
				map.put(nearestCentroid[i], list);
			}
			
			for(int i = 0; i < numOfCluster; i++) {
				if (map.containsKey(i) == false) {
					continue;
				}
				newCentroid[i] = adjust(map.get(i));
			}			
			
			// Step 2.3 compute new errors
//			System.out.println("Step 2.3 check new errors");
//		    if(check(curCentroid, newCentroid, minError)) {
//		    	System.out.println("error converge");
//		    	break;
//		    }
		    
		    for(int i = 0; i < numOfCluster; i++) {
		    	curCentroid[i] = newCentroid[i];
		    }
		    curStep++;
//		    System.out.println("curStep = " + curStep);
		}
		
//		System.out.println("Centroid for clusters are : ");
//		for(int i = 0; i < numOfCluster; i++) {
//	    	System.out.println(newCentroid[i]);
//		}
    }
    
    private boolean check(String[] curCentroid, String[] newCentroid, int minError) {
    	boolean ret = true;
    	for(int i = 0; i < numOfCluster; i++) {
    		int error = diff(curCentroid[i], newCentroid[i]);
    		if(error > minError) {
    			ret = false;
    			break;
    		}
    	}
    	return ret;
    }
    
    private int diff(String s1, String s2) {
    	int numOfDiff = 0;
    	for(int i = 0; i < s1.length(); i++) {
    		if(s1.charAt(i) == s2.charAt(i)) {
    			numOfDiff++;
    		}
    	}
    	return numOfDiff;
    }
    
    private String adjust(ArrayList<String> list) {
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0; i < list.get(0).length(); i++) {
    		int[] freqOfDNA = new int[DNA_SEQ.length()];
    		for(String str : list) {    			
    			char c = str.charAt(i);
    			freqOfDNA[DNA_SEQ.indexOf(c)]++;    			
    		}
    		
    		int freqMax = 0;
    		int indexMax = 0;
    		for(int j = 0; j < DNA_SEQ.length(); j++) {
    			if(freqOfDNA[j] > freqMax) {
    				indexMax = j;
    				freqMax = freqOfDNA[j];
    			}
    		}
    		sb.append(DNA_SEQ.charAt(indexMax));
    	}
    	return sb.toString();
    }
    
    private int classify(String dna) {
    	int index = -1;
    	int minDist = Integer.MAX_VALUE;    	
    	
    	for(int i = 0; i < curCentroid.length; i++) {
    		String str = curCentroid[i];
    		int diff = 0;
    		for(int j = 0; j < str.length(); j++) {
    			if(str.charAt(j) != dna.charAt(j)) {
    				diff++;
    			}
    		}
    		
    		if(diff < minDist) {
    			index = i;
    			minDist = diff;
    		}
    	}
    	return index;
    }
    
    private Map sortByValue(Map unsortedMap) {
    	List list = new LinkedList(unsortedMap.entrySet());
    	 
    	Collections.sort(list, new Comparator() {
    		public int compare(Object o1, Object o2) {
    			return ((Comparable) ((Map.Entry) (o1)).getValue())
    						.compareTo(((Map.Entry) (o2)).getValue());
    		}
    	});
     
    	Map sortedMap = new LinkedHashMap();
    	for (Iterator it = list.iterator(); it.hasNext();) {
    		Map.Entry entry = (Map.Entry) it.next();
    		sortedMap.put(entry.getKey(), entry.getValue());
    	}
    	return sortedMap;
	}
}
