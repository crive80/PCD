import java.io.IOException;
import java.rmi.*;
import java.util.ArrayList;

public interface ClientInterface extends Remote{

	public String getName() throws RemoteException; 
	public int getDownload() throws RemoteException;
	public ArrayList<Resource> getFiles() throws RemoteException;
	public Resource getResource(String s) throws RemoteException;
	public ArrayList<Resource> getFilesFromServer() throws RemoteException; 
	public byte[] fileToByteArray(Resource r) throws RemoteException, IOException; 
	public Resource byteArrayToFile(byte[] b) throws IOException, ClassNotFoundException; 
	public boolean checkFile(String r) throws RemoteException; 
	public int requestPart(Resource r, int part, String client, ClientInterface clientName)throws RemoteException; 
	public int uploadPart(int p) throws RemoteException; 
	public void addActiveClient(String c)throws RemoteException;  
	public void rmActiveClient(String c)throws RemoteException; 
	public ArrayList<String> getActiveClients()throws RemoteException; 
	public boolean checkActiveClients(String Name) throws RemoteException; 
	public ServerInterface getConnected() throws RemoteException;
	public void printSearchResults(ArrayList<ClientInterface> temp, ClientInterface c) throws IOException;
	public void newResource(ArrayList<ClientInterface> temp,
			ClientInterface clientReference, String s) throws IOException;
}
