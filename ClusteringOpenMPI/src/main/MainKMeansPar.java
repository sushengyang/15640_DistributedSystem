package main;

import mpi.MPI;
import mpi.MPIException;
import parallel.KMeansPar;

public class MainKMeansPar {
    public static void main(String[] args) throws MPIException {
    	long startTime = System.currentTimeMillis();
		MPI.Init(args);
		KMeansPar kms = new KMeansPar(3, "./kmeans.csv", "./kmeans_par.csv");
    	kms.readData();
    	kms.launch();
    	kms.writeData();  
		int rank = MPI.COMM_WORLD.Rank();
		MPI.Finalize();
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("Rank " + rank + ": used " + estimatedTime / 1000.0 + " sec");
    }
}
