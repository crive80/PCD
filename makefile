Progetto : ClientGUI.class ServerGUI.class Resource.class ServerInterface.class Server.class ClientInterface.class Client.class

ClientGUI.class : ClientGUI.java
	javac ClientGUI.java -d ./

ServerGUI.class : ServerGUI.java
	javac ServerGUI.java -d ./

Resource.class : Resource.java
	javac Resource.java -d ./

ServerInterface.class : ServerInterface.java
	javac ServerInterface.java -d ./

Server.class : Server.java
	javac Server.java -d ./

ClientInterface.class : ClientInterface.java
	javac ClientInterface.java -d ./

Client.class : Client.java
	javac Client.java -d ./

clean: 
	rm -f *.class

start: 
	killall -q rmiregistry &
	sleep 10
	rmiregistry &
	sleep 2
	java Server server1 &
	java Server server2 &
	sleep 2
	java Client client1 server1 3 r1 1 &
	java Client client2 server2 5 r4 2 r2 7 g 8 &
	java Client client3 server2 5 r4 2 r2 7 g 8 &

stop:
	killall -q rmiregistry &



    
