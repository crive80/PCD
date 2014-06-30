import java.net.MalformedURLException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.Map;

public interface ServerInterface extends Remote {
	
	public String getName() throws RemoteException; 
	public ServerGUI getServerGUI() throws RemoteException;
	public ArrayList<Resource> getFiles() throws RemoteException;
	public void printServers() throws RemoteException; 
	public ArrayList<String> getAllServers() throws RemoteException; 
	//public void addFilesFromClient(ArrayList<Resource> r) throws RemoteException; 
	public boolean addClient(ClientInterface c) throws RemoteException; 
	public boolean removeClient(ClientInterface c) throws RemoteException;
	public void disconnect() throws RemoteException, NotBoundException, MalformedURLException; 
	public ArrayList<ClientInterface> getClients() throws RemoteException; 
	public boolean searchFileFromClient(ClientInterface c, String r) throws RemoteException; 
	public ArrayList<ClientInterface> searchFile(String name) throws RemoteException; 
	//public ClientsMap getClientsMap() throws RemoteException; 
	public Map<ClientInterface, ArrayList<Resource>> getClientsMap() throws RemoteException;

}
