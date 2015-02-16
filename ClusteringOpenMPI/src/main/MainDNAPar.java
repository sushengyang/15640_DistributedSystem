package main;

import mpi.MPI;
import mpi.MPIException;
import parallel.DNAPar;



public class MainDNAPar {
	public static void main(String[] args) throws MPIException {
		long startTime = System.currentTimeMillis();
		MPI.Init(args);
		DNAPar dna = new DNAPar(3, "./DNA.csv", "./DNA_par.csv");
		dna.readData();
		dna.launch();
		int rank = MPI.COMM_WORLD.Rank();
		MPI.Finalize();
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("Rank " + rank + ": used " + estimatedTime / 1000.0 + " sec");
    }
}
