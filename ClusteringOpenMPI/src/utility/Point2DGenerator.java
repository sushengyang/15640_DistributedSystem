package utility;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

public class Point2DGenerator extends Generator {
	private static final int FIVE_SEC = 50000;
	private static final int ARGS_LEN = 5;
	
	private int numOfCluster = 5;
	private int numOfPoints = 20;
	private String fileName = "kmeans.csv";
	private double maxMean = 100.0;
	private double maxVar = 5.0;
	
	private TreeSet<Double> centroid = new TreeSet<Double>();
	private HashSet<String> point2D = new HashSet<String>();
	
	@Override
	protected void printUsage() {
		System.out.println("Default value used.");
		System.out.println("Usage : Point2DGenerator [numOfClusters] [numOfPoints/cluster] [output filename] [max mean] [max variance]");
		System.out.println("e.g.    Point2DGenerator 3 20 kmeans 100.0 5.0");
		System.out.println("        3 clusters, 20 points per cluster, max mean 100.0, max var 50.0 data generated in kmeans.csv");
	}
	
	@Override
	protected void parseArgs(String[] args) {
		numOfCluster = Integer.parseInt(args[0]);
    	numOfPoints = Integer.parseInt(args[1]);
    	fileName = args[2] + ".csv";
    	maxMean = Double.parseDouble(args[3]);
    	maxVar = Double.parseDouble(args[4]);
	}
	
	@Override
	protected void generateCentroid() {
    	int count = 0;
    	while(count < numOfCluster) {
    		double randCentroid = getUniform(0, maxMean);
    		if(!centroid.contains(randCentroid)) {
    			centroid.add(randCentroid);
    			count++;
    		}
    	}
	}
	
	@Override
	protected void generateData() {
		try {
	    	FileOutputStream fos = new FileOutputStream(fileName);
	    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	    	
	    	Iterator<Double> itor = centroid.iterator();
	    	while(itor.hasNext()) {
	    		int count = 0;
	    		double mean = itor.next();
	    		double variance = getUniform(0, maxVar);
	    		
	        	while(count < numOfPoints) {
	        		double x = getGaussian(mean, variance);
	        		double y = getGaussian(mean, variance);
	        	    
	        	    if(x > 0 && y > 0) {
		        	    String xy = x + "," + y;
		        	    if(!point2D.contains(xy)) {
		        	    	point2D.add(xy);
		        	    	count++;
		        	    	bw.write(x + "," + y);
		        	    	bw.newLine();
		        	    }
	        	    }
	        	}
	        }
	    	
	    	bw.close();
	    	fos.close();
		} catch(IOException e) {			
		}
	}
	
    public static void main(String[] args) throws IOException {
    	Reminder rmd = new Reminder(FIVE_SEC);
    	Point2DGenerator p2d = new Point2DGenerator();
    	
    	if(args.length != ARGS_LEN) {
    		p2d.printUsage();
    	} else {
    		p2d.parseArgs(args);
    	}
    	
    	p2d.generateCentroid();
    	p2d.generateData();
    	rmd.cancel();
    	System.out.println("Data Generation Succeed!");
    }
}
