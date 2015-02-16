package main;
import java.awt.Point;
import java.util.Arrays;

import mpi.MPI;
import mpi.MPIException;

public class MPIHello {
	
	public static void main(String[] args) throws MPIException{
		MPI.Init(args);
		
		//myRank : current process id;
		int myRank = MPI.COMM_WORLD.Rank();
		System.out.println("My rank is : " + myRank);
		
		//MPI.COMM_WORLD.Size : total amount of processes
		int size = MPI.COMM_WORLD.Size();
		System.out.println("Size is" + MPI.COMM_WORLD.Size());
		
		//master here
		if(myRank == 0) {
			for(int i = 1; i < size; i++) {
				Point [] p = new Point[1];
				p[0] = new Point(i, i);
				//send point to each processor
				//sentObj,offset,item to be sent,data type,receiver id,tag
				MPI.COMM_WORLD.Send(p, 0, p.length, MPI.OBJECT, i, 99);
				System.out.println("send "+ Arrays.toString(p) + " to " + i);
			}
			
			
		}
		//slave here
		else {
			//message : buf | 0 : offset | 20 : count | MPI.CHAR : datatype | 0 : source | 99 : tag 
			Point[] p = new Point[1];
			MPI.COMM_WORLD.Recv(p, 0, 1, MPI.OBJECT, 0, 99);
			System.out.println("recv "+ Arrays.toString(p) + " from 0");
	
		}
		
//		int[]xSum = { 1, 2, 3 },xSumNew = new int[3];
//		MPI.COMM_WORLD.Allreduce(xSum, 0, xSumNew, 0, xSum.length, MPI.INT, MPI.SUM);
//		System.out.println(Arrays.toString(xSum));

	

		
		MPI.Finalize();
	
	}
}




