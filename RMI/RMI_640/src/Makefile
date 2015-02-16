JFLAGS = 
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	./main/RMIServer.java \
	./main/RMISvrHandler.java \
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
	./services/Student.java \
	./services/StudentList.java \

MAIN = main/RMIServer
	
default: classes

classes: $(CLASSES:.java=.class)

run:
	java $(MAIN)

clean:	
	find . -name "*.class" -type f -delete
	
