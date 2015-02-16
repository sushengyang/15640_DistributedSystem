JFLAGS = 
JC = javac

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	./hdfs/KPFile.java \
	./hdfs/KPFSException.java \
	./hdfs/KPFSFileInfo.java \
	./hdfs/KPFSMaster.java \
	./hdfs/KPFSMasterInterface.java \
	./hdfs/KPFSSlave.java \
	./hdfs/KPFSSlaveInterface.java \
	./jobcontrol/JobDispatcher.java \
	./jobcontrol/JobInfo.java \
	./jobcontrol/JobManager.java \
	./jobcontrol/Task.java \
	./mapreduce/GlobalInfo.java \
	./mapreduce/MapReduce.java \
	./mapreduce/Master.java \
	./mapreduce/MRBase.java \
	./mapreduce/Pair.java \
	./mapreduce/PairContainer.java \
	./mapreduce/Slave.java \
	./mapreduce/SlaveTracker.java \
	./mapreduce/SlaveWork.java \
	./network/Listen.java \
	./network/Message.java \
	./network/MsgHandler.java \
	./network/NetworkFailInterface.java \
	./network/NetworkHelper.java

MAIN = mapreduce.MapReduce
	
default: classes

classes: $(CLASSES:.java=.class)

run:
	java $(MAIN)

clean:	
	find . -name "*.class" -type f -delete
	
