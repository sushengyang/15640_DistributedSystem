package main;

import sequential.KMeansSeq;

public class MainKMeansSeq {
    public static void main(String[] args) {
    	long startTime = System.currentTimeMillis();
    	KMeansSeq kms = new KMeansSeq(3, "./kmeans.csv", "./kmeans_seq.csv");
    	kms.readData();
    	kms.launch();
    	kms.writeData();  
    	long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("Time elapsed : " + estimatedTime / 1000.0 + " sec");
    }
}
