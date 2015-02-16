package main;

import sequential.DNASeq;


public class MainDNASeq {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
    	DNASeq dna = new DNASeq(3, "./DNA.csv", "./DNA_seq.csv");
    	dna.readData();
    	dna.launch();
    	dna.writeData();  
    	long estimatedTime = System.currentTimeMillis() - startTime;
    	System.out.println("Time elapsed : " + estimatedTime / 1000.0 + " sec");
    }
}
