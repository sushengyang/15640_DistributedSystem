JFLAGS = 
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	./edu/cmu/andrew/ds/main/PMMain.java \
	./edu/cmu/andrew/ds/main/ClusterManager.java \
	./edu/cmu/andrew/ds/io/TransactionalFileInputStream.java \
	./edu/cmu/andrew/ds/io/TransactionalFileOutputStream.java \
	./edu/cmu/andrew/ds/network/ClientManager.java \
	./edu/cmu/andrew/ds/network/NetworkManager.java \
	./edu/cmu/andrew/ds/network/MessageStruct.java \
	./edu/cmu/andrew/ds/network/ServerHandler.java \
	./edu/cmu/andrew/ds/network/ServerManager.java \
	./edu/cmu/andrew/ds/ps/IOProcess.java \
	./edu/cmu/andrew/ds/ps/NonIOProcess.java \
	./edu/cmu/andrew/ds/ps/GrepProcess.java \
	./edu/cmu/andrew/ds/ps/MigratableProcess.java \
	./edu/cmu/andrew/ds/ps/ProcessManager.java \

MAIN = edu/cmu/andrew/ds/main/PMMain
	
default: classes

classes: $(CLASSES:.java=.class)

run:
	java $(MAIN)

clean:	
	find . -name "*.class" -type f -delete
	
