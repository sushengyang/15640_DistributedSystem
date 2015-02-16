package parallel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

import mpi.MPI;
import mpi.MPIException;
import utility.Point2D;

public class KMeansPar {
	private int numOfCluster;
	private String ifileName;
	private String ofileName;
	
	private int errorCnt;
	
	private ArrayList<Point2D> points;
	private int[] nearestCentroid;
	
	private HashMap<Point2D, Integer> map;
	
	private Point2D[] curCentroid;
	private Point2D[] newCentroid;
	
	private int rank;
	private int size;
	
	private int[] slavest;
	private int[] slaveed;
	
	public KMeansPar(int numOfCluster, String ifileName, String ofileName) {
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
    
    public void launch() {
    	try {
			rank = MPI.COMM_WORLD.Rank();
			size = MPI.COMM_WORLD.Size() - 1;
			
			slavest = new int[size + 1];
			slaveed = new int[size + 1];
			for (int i=1; i<=size; ++i) {
				slavest[i] = (i-1) * points.size() / size;
				slaveed[i] = min(points.size()-1, i*points.size()/size - 1);
				//System.out.println("slave " + i + " st(" + slavest[i] + "), ed(" + slaveed[i] + ")");
			}
		
			// Step 1. random centroid
//	    	System.out.println("Step 1. random centroid");
	    	Random r = new Random();
			for (int i = 0; i < numOfCluster; i++) {
				Point2D p2d = points.get(r.nextInt(points.size()));
				//Point2D p2d = points.get(i);
				curCentroid[i] = p2d;
				//System.out.println(i + " centoid: " + p2d.getX() + " " + p2d.getY());
			}
			
			// Step 2. iterate to converge
//			System.out.println("Step 2. iterate to converge");
			int curStep = 0;
			int maxStep = 100;
			double minError = 0.0;
			while(curStep < maxStep) {
				
				if (rank == 0) {
					for (int i = 0; i < numOfCluster; i++) {
						Point2D p2d = curCentroid[i];
						//System.out.println(i + " centoid: " + p2d.getX() + " " + p2d.getY());
					}
					// Master send out the tasks
					for (int slave = 1; slave <= size ; slave++) {
						Point2D[] msg = new Point2D[1 + numOfCluster];
						msg[0] = new Point2D((double)slavest[slave], (double)slaveed[slave]);
						for (int i=0; i<numOfCluster; ++i) {
							msg[i+1] = curCentroid[i];
						}
						MPI.COMM_WORLD.Send(msg, 0, msg.length, MPI.OBJECT, slave, 1);
					}
				} else {
					// Slave working...
					Point2D[] msg = new Point2D[1 + numOfCluster];
					MPI.COMM_WORLD.Recv(msg, 0, msg.length, MPI.OBJECT, 0, 1);
					int st = (int)msg[0].getX();
					int ed = (int)msg[0].getY();
					for (int i=0; i<numOfCluster; ++i) {
						curCentroid[i] = msg[i+1];
					}
					Integer[] res = new Integer[ed-st+1];
					
					// Step 2.1 classify points (find nearest centroid for every point)
//					System.out.println("Step 2.1 classify points " + MPI.COMM_WORLD.Rank());
					
					for (int i = st; i <= ed; i++) {
						res[i-st] = classify(points.get(i));
						//System.out.println("Slave " + rank + ": " + i + ": " + res[i-st]);
					}
					MPI.COMM_WORLD.Send(res, 0, res.length, MPI.OBJECT, 0, 2);
//					System.out.println("FINISHED Step 2.1 classify points " + MPI.COMM_WORLD.Rank());
				}
				
				if (rank == 0) {
					// Receive the results from slaves 
					for (int slave = 1; slave <= size; ++slave) {
						//System.out.println("phase " + 2 + " receving msg from slave " + slave);
						Integer[] partRes = new Integer[slaveed[slave]-slavest[slave]+1];
//						System.out.println("\trecevied");
						MPI.COMM_WORLD.Recv(partRes, 0, partRes.length, MPI.OBJECT, slave, 2);
						int st = slavest[slave];
						for (int i=0; i<partRes.length; ++i) {
							nearestCentroid[i+st] = partRes[i];
						}
					}
					
					// Assign the statistics tasks to slaves
					for (int slave = 1; slave <= size; ++slave) {
						int st = getSlaveStartIndex(slave);
						int ed = getSlaveEndIndex(slave);
						Integer[] msg = new Integer[ed-st+1];
						for (int i = st; i <= ed; ++i) {
							msg[i-st] = nearestCentroid[i];
						}
						MPI.COMM_WORLD.Send(msg, 0, msg.length, MPI.OBJECT, slave, 3);
					}
				} else {
					int slv = MPI.COMM_WORLD.Rank();
					int st = slavest[slv];
					int ed = slaveed[slv];
					Integer partCluster[] = new Integer[ed-st+1];
					MPI.COMM_WORLD.Recv(partCluster, 0, partCluster.length, MPI.OBJECT, 0, 3);
					
					// Step 2.2 compute new centroid for every class
//					System.out.println("Step 2.2 compute new centroid for every class");
					Map<Integer, int[]> res = new HashMap<Integer, int[]>();
					for (int i=0; i<partCluster.length; ++i) {
						int[] rr = res.get(partCluster[i]);
						if (rr == null) {
							rr = new int[3];
							res.put(partCluster[i], rr);
						}
						rr[0] += points.get(i+st).getX();	// sumX
						rr[1] += points.get(i+st).getY();	// sumY
						++rr[2];	// numOfPoints
					}
					for (int i=0; i<partCluster.length; ++i) { 
						int[] rr = res.get(partCluster[i]);
					}
					
					// send the result back
					Map[] msg = new Map[1];
					msg[0] = res;
					MPI.COMM_WORLD.Send(msg, 0, 1, MPI.OBJECT, 0, 4);
				}
				
				if (rank == 0) {
					Map<Integer, int[]> allStat = new HashMap<Integer, int[]>();
					
					for (int slave = 1; slave <= size; ++slave) {
						Map[] msg = new Map[1];
						MPI.COMM_WORLD.Recv(msg, 0, 1, MPI.OBJECT, slave, 4);
						Map<Integer, int[]> partStat = msg[0];
						
						for (Integer cluster: partStat.keySet()) {
							int allr[] = allStat.get(cluster);
							if (allr == null) {
								allr = new int[3];
								allStat.put(cluster, allr);
							}
							
							int partr[] = partStat.get(cluster);
							allr[0] += partr[0];	// sumX
							allr[1] += partr[1];	// sumY
							allr[2] += partr[2];	// numOfPoints
						}
					}
					
					for(int i = 0; i < numOfCluster; i++) {
						int rr[] = allStat.get(i);
						if (rr == null) {
							newCentroid[i] = new Point2D(0.0, 0.0);
						} else {
							newCentroid[i] = new Point2D(rr[0] / (double)rr[2], rr[1] / (double)rr[2]);
					    }
					}
					
					// Step 2.3 compute new errors
//					System.out.println("Step 2.3 check new errors");
//				    if(check(curCentroid, newCentroid, minError)) {
//				    	errorCnt++;
//				    }
				    
//				    if(errorCnt == 3) {
				    	//System.out.println("error converge");
				    	//break;
//				    }
				    
				    for(int i = 0; i < numOfCluster; i++) {
				    	curCentroid[i] = newCentroid[i];
				    }
				} 
				
				curStep++;
//				System.out.println("curStep = " + curStep); 	
			}
			
//			if (rank == 0) {	
//				System.out.println("Centroid for clusters are : ");
//				for(int i = 0; i < numOfCluster; i++) {
//			    	System.out.println(curCentroid[i].getX() + "," + curCentroid[i].getY());
//				}
//			}
    	} catch (MPIException e) {
		}
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
    
    private int min(int a, int b) {
    	return a<b?a:b;
    }
    
    private int getSlaveStartIndex(int slv) {
    	return (slv-1) * points.size() / size;
    }
    private int getSlaveEndIndex(int slv) {
    	return min(points.size()-1, slv*points.size()/size - 1);
    }
    private int getSlaveLength() {
    	return points.size()/size + 1;
    }
}
