package main;

import sequential.DNASeq;

public class MainDNA {
	public static void main(String[] args) {
    	DNASeq dna = new DNASeq(3, "./DNA.csv", "./DNA_seq.csv");
    	dna.readData();
    	dna.launch();
    	dna.writeData();    
    }
}
