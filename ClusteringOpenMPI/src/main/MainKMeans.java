package main;

import sequential.KMeansSeq;

public class MainKMeans {
    public static void main(String[] args) {
    	KMeansSeq kms = new KMeansSeq(3, "./kmeans.csv", "./kmeans_seq.csv");
    	kms.readData();
    	kms.launch();
    	kms.writeData();    
    }
}
