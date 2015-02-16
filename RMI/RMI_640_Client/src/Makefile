JFLAGS = 
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	./main/RMIClient.java \
	./rmi/RMIObjectReference.java \
	./rmi/RMIMessage.java \
	./rmi/RMIException.java \
	./rmi/server/ServerConst.java \
	./rmi/server/RMIService.java \
	./rmi/server/RMIServerRegistry.java \
	./rmi/server/RMIServerNetworkMgr.java \
	./rmi/client/ClientConst.java \
	./rmi/client/RMIClientRegistry.java \
	./rmi/client/RMIClientNetworkMgr.java \
	./rmi/client/ClientConst.java \
	./stub/Student_stub.java \
	./stub/StudentList_stub.java \

MAIN = main/RMIClient
	
default: classes

classes: $(CLASSES:.java=.class)

run:
	java $(MAIN)

clean:	
	find . -name "*.class" -type f -delete
	
