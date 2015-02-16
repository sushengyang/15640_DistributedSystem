CLASSES = \
	./main/MainHello.java \
	./main/MainDNAPar.java \
	./main/MainKMeansPar.java \
	./main/MainDNASeq.java \
	./main/MainKMeansSeq.java \
	./parallel/DNAPar.java \
	./parallel/KMeansPar.java \
	./sequential/DNASeq.java \
	./sequential/KMeansSeq.java \
	./utility/Point2D.java \
	./utility/Generator.java \
	./utility/DNAGenerator.java \
	./utility/Point2DGenerator.java \

JC = javac
MPIJC = mpijavac
	
.SUFFIXES: .java .class
.java.class:
	$(JC) $*.java

default: classes

classes: $(CLASSES:.java=.class)

compile:
	$(MPIJC) $(CLASSES)

generate_kmeans_data:
	java utility.Point2DGenerator 5 50000 kmeans 100.0 10.0

generate_dna_data:
	java utility.DNAGenerator 5 50000 20 DNA 8

run_kmeans_seq:
	java main.MainKMeansSeq

run_dna_seq:
	java main.MainDNASeq

run_kmeans_par:
	echo "Number of process: 2"
	mpirun -np 2 -machinefile ./hostfile.txt java main.MainKMeansPar 5
	mpirun -np 4 -machinefile ./hostfile.txt java main.MainKMeansPar 5
	mpirun -np 8 -machinefile ./hostfile.txt java main.MainKMeansPar 5
	mpirun -np 12 -machinefile ./hostfile.txt java main.MainKMeansPar 5
	mpirun -np 16 -machinefile ./hostfile.txt java main.MainKMeansPar 5
	mpirun -np 20 -machinefile ./hostfile.txt java main.MainKMeansPar 5
	
run_dna_par:
	mpirun -np 2 -machinefile ./hostfile.txt java main.MainDNAPar 5
	mpirun -np 4 -machinefile ./hostfile.txt java main.MainDNAPar 5
	mpirun -np 8 -machinefile ./hostfile.txt java main.MainDNAPar 5
	mpirun -np 12 -machinefile ./hostfile.txt java main.MainDNAPar 5
	mpirun -np 16 -machinefile ./hostfile.txt java main.MainDNAPar 5
	mpirun -np 20 -machinefile ./hostfile.txt java main.MainDNAPar 5

test: 
	mpirun -np 3 -machinefile ./hostfile.txt java main.MainHello 3
	
clean:	
	find . -name "*.class" -type f -delete
	
