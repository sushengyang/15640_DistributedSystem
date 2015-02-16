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

import utility.Point2D;

public class KMeansSeq {
	private int numOfCluster;
	private String ifileName;
	private String ofileName;
	
	private int errorCnt;
	
	private ArrayList<Point2D> points;
	private int[] nearestCentroid;
	
	private HashMap<Point2D, Integer> map;
	
	private Point2D[] curCentroid;
	private Point2D[] newCentroid;
	
	
    public KMeansSeq(int numOfCluster, String ifileName, String ofileName) {
    	this.numOfCluster = numOfCluster;
    	this.ifileName = ifileName;
    	this.ofileName = ofileName;
    	
    	this.points = new ArrayList<Point2D>();
    	this.curCentroid = new Point2D[numOfCluster];
    	this.newCentroid = new Point2D[numOfCluster];
    	
    	this.map = new HashMap<Point2D, Integer>();
    	this.errorCnt = 0;
    }
    
    public void readData() {
    	try {
			BufferedReader br = new BufferedReader(new FileReader(ifileName));
			String line = null;
			while((line = br.readLine()) != null) {
				if(line.isEmpty()) {
					continue;
				}
				String[] parts = line.split(",");
				Point2D p = new Point2D(Double.parseDouble(parts[0]),Double.parseDouble(parts[1]));
				points.add(p);
			}
			br.close();			
			this.nearestCentroid = new int[points.size()];
		} catch (NumberFormatException | IOException e) {
		}  
    }
    
    public void writeData() {
    	try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(ofileName));
			for(int i = 0; i < points.size(); i++) {
				map.put(points.get(i), nearestCentroid[i]);
			}
			
			LinkedHashMap<Point2D, Integer> sortedMap = (LinkedHashMap<Point2D, Integer>) sortByValue(map);			
			for(Entry<Point2D, Integer> entry : sortedMap.entrySet()) {
				bw.write(entry.getKey().getX() + "," + entry.getKey().getY() + "," + entry.getValue());
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
			//Point2D p2d = points.get(r.nextInt(points.size()));
			Point2D p2d = points.get(i);
			curCentroid[i] = p2d;
		}
		
		// Step 2. iterate to converge
//		System.out.println("Step 2. iterate to converge");
		int curStep = 0;
		int maxStep = 100;
		double minError = 0.0;
		while(curStep < maxStep) {
			
			// Step 2.1 classify points (find nearest centroid for every point)
//			System.out.println("Step 2.1 classify points");
			for (int i = 0; i < points.size(); i++) {
				nearestCentroid[i] = classify(points.get(i));
			}
			
			// Step 2.2 compute new centroid for every class
//			System.out.println("Step 2.2 compute new centroid for every class");
			double[] sumX = new double[numOfCluster];
			double[] sumY = new double[numOfCluster];
			int[] numOfPoints = new int[numOfCluster];
			for (int i = 0; i < numOfCluster; i++) {
				sumX[i] = 0.0;
				sumY[i] = 0.0;
				numOfPoints[i] = 0;
			}
			
			for(int i = 0; i < points.size(); i++) {
			    sumX[nearestCentroid[i]] += points.get(i).getX(); 	
			    sumY[nearestCentroid[i]] += points.get(i).getY();
			    numOfPoints[nearestCentroid[i]]++;
			}
			
			for(int i = 0; i < numOfCluster; i++) {
			    if(numOfPoints[i] == 0) {
			    	newCentroid[i] = new Point2D(0.0, 0.0);
			    } else {
			    	newCentroid[i] = new Point2D(sumX[i] / (double)numOfPoints[i], sumY[i] / (double)numOfPoints[i]);
			    }
			}
			
			// Step 2.3 compute new errors
//			System.out.println("Step 2.3 check new errors");
//		    if(check(curCentroid, newCentroid, minError)) {
//		    	errorCnt++;
//		    	System.out.println("error converge");
//		    	break;
//		    }
		    
//		    if(errorCnt == 3) {
//		    	System.out.println("error converge");
		    	//break;
//		    }
		    
		    for(int i = 0; i < numOfCluster; i++) {
		    	curCentroid[i] = newCentroid[i];
		    }
		    curStep++;
//		    System.out.println("curStep = " + curStep);
		}
		
//		System.out.println("Centroid for clusters are : ");
//		for(int i = 0; i < numOfCluster; i++) {
//	    	System.out.println(curCentroid[i].getX() + "," + curCentroid[i].getY());
//		}
    }
    
    private boolean check(Point2D[] curCentroid, Point2D[] newCentroid, double minError) {
    	boolean ret = true;
    	for(int i = 0; i < numOfCluster; i++) {
    		double diff = Math.pow(Math.abs(curCentroid[i].getX() - newCentroid[i].getX()), 2.0) + 
    				Math.pow(Math.abs(curCentroid[i].getX() - newCentroid[i].getX()), 2.0);
    		if(diff > minError) {
    			ret = false;
    			break;
    		}
    	}
    	return ret;
    } 
     
    private int classify(Point2D p2d) {
    	double minDist = Double.MAX_VALUE;
    	double curDist = 0.0;
    	
    	int index = -1;
    	for(int i = 0; i < curCentroid.length; i++) {
    		Point2D c = curCentroid[i];
    		curDist = Math.pow(Math.abs(p2d.getX() - c.getX()), 2.0) 
    				+ Math.pow(Math.abs(p2d.getY() - c.getY()), 2.0);
    		if(curDist < minDist) {
    			index = i;
    			minDist = curDist;
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
