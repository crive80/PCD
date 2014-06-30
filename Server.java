import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
@SuppressWarnings("serial")
public class Server extends UnicastRemoteObject implements ServerInterface{
	private ServerGUI serverGui;
	private String name; 
	private Map<ClientInterface, ArrayList<Resource>> clientMap = new HashMap<ClientInterface, ArrayList<Resource>>(); 
	private static ArrayList<ServerInterface> allServers = new ArrayList<ServerInterface>(); // all system's servers
	private static ArrayList<String> allServersList = new ArrayList<String>(); 
	private static final String HOST = "localhost";
	private Object lock = new Object(); 
	
	public Server(String n) throws RemoteException{
		name = n; 
		serverGui = new ServerGUI(n,this);
        ServerCheckDaemon c1 = new ServerCheckDaemon(); 
        c1.start();
        ClientCheckDaemon c2 = new ClientCheckDaemon(); // da spostare alla creazione di client
        c2.start(); 
        
	}
	public String getName() throws RemoteException{
		return name; 
	}
	public ServerGUI getServerGUI() throws RemoteException{
		return serverGui;
	}
	
	public synchronized ArrayList<ClientInterface> getClients(){
		ArrayList<ClientInterface> temp = new ArrayList<ClientInterface>(); 
		Set<ClientInterface> list  = clientMap.keySet();
		Iterator<ClientInterface> it = list.iterator();
		while(it.hasNext()){
			ClientInterface key = it.next(); 
			temp.add(key); 
		}
		return temp; 
	}
	
	public ArrayList<Resource> getFiles() throws RemoteException{
		ArrayList<Resource> temp = new ArrayList<Resource>(); 
		Set<ClientInterface> list  = clientMap.keySet();
		Iterator<ClientInterface> it = list.iterator(); 
		while(it.hasNext()){
			ArrayList<Resource> vr = it.next().getFiles(); 
			temp.addAll(vr);
		}
		return temp; 
	}

	public Map<ClientInterface, ArrayList<Resource>> getClientsMap() throws RemoteException{ 
		return clientMap;
		}; 
	
	public ArrayList<ClientInterface> searchFile(String name) throws RemoteException{
		ArrayList<ClientInterface> temp = new ArrayList<ClientInterface>(); 
		try{
			synchronized (clientMap){
				for(int i = 0; i<allServersList.size(); i++){
					ServerInterface in = (ServerInterface) Naming.lookup(allServersList.get(i));
					Set<ClientInterface> list  = in.getClientsMap().keySet();
					Iterator<ClientInterface> it = list.iterator(); 
					while(it.hasNext()){
						ClientInterface c = it.next(); 
						//System.out.println(c.getName()); 
						if(searchFileFromClient(c, name)){
							temp.add(c); 
						}
					}
				}
			}
		}catch(Exception e){ serverGui.appendLog("errore durante la ricerca"); }
		
		return temp; 
	}

	public synchronized boolean searchFileFromClient(ClientInterface c, String r) throws RemoteException{
		
		boolean found = false; 
		Iterator<Resource> it = c.getFiles().iterator(); 
			while(it.hasNext()){
				Resource t = it.next(); 
				if(t.getName().equals(r)){
					found = true; 
				}
			}
		return found; 
	}
	
	public boolean addClient(ClientInterface c) throws RemoteException{
		boolean aggiunto = false; 
		synchronized(clientMap){
			if(!clientMap.containsKey(c)){
				clientMap.put(c, c.getFiles()); 
				aggiunto = true; 
			}
		}
		return aggiunto;  
	}
	public boolean removeClient(ClientInterface c) throws RemoteException{
		boolean rimosso = false; 
		synchronized(clientMap){
			if(clientMap.containsKey(c)){
				clientMap.remove(c); 
				rimosso = true; 
			}
		}
		return rimosso;  
	}
	
	public void disconnect() throws NotBoundException, RemoteException, MalformedURLException {
       synchronized (lock) {
            Naming.unbind("rmi://" + HOST + "/Server/" + name);
       }
    }
	
	class ServerCheckDaemon extends Thread{
		ServerCheckDaemon(){ setDaemon(true); }
		public void run(){
			while(true){
					try{ 
						String[] list = Naming.list("rmi://" + HOST + "/Server/");
						String[] names = new String[list.length]; 
						for(int i = 0; i<list.length; i++){
							 ServerInterface t = (ServerInterface)Naming.lookup(list[i]); 
                             names[i] = t.getName(); 
						}
						   
						serverGui.setServerList(names);
						
		                allServersList.clear();
		                for (int i=0; i<list.length; i++){
		                	allServersList.add(list[i]); 
		                	ServerInterface t = (ServerInterface) Naming.lookup(list[i]); 
		                	allServers.add(t); 
		                }
		                } catch (RemoteException | MalformedURLException | NotBoundException e) {
		            	serverGui.appendLog("Errore di connessione");
		                }
					try {
						sleep(20);
					} catch (InterruptedException e) {
						serverGui.appendLog("thread interrotto");
					} 
				
			}
		}
	}
	
	class ClientCheckDaemon extends Thread{
		ClientCheckDaemon(){ setDaemon(true); }
		public void run(){
			while(true){
				synchronized(clientMap){
				    	ArrayList<ClientInterface> temp = Server.this.getClients(); 
				    	String[] list = new String[temp.size()]; 
				    	for(int i = 0; i < list.length; i++){
				    		try {
								list[i] = temp.get(i).getName();
							} catch (RemoteException e) {
								 serverGui.appendLog("Client disconnesso\n"); 
								 clientMap.remove(temp.get(i)); 
							} 
				    	}
				    }
					try {
						sleep(20);
					} catch (InterruptedException e) {
						serverGui.appendLog("CheckDaemon interrotto"); 
					} 
			
				try {
					ArrayList<String> allClients = new ArrayList<String>();
					for(int i = 0; i<allServersList.size(); i++){
						ServerInterface s = (ServerInterface)Naming.lookup(allServersList.get(i));
						ArrayList<ClientInterface> temp = s.getClients(); 
						//System.out.println(allServersList.size()); 
						for (int j=0; j<temp.size(); j++) {
							allClients.add(temp.get(j).getName()); 
							//clientMap.put(temp.get(j), temp.get(j).getFiles()); 
						}
					}
					ArrayList<String> localClients = new ArrayList<String>(); 
					ArrayList<ClientInterface> temp = Server.this.getClients(); 
					for (int j=0; j<temp.size(); j++) 
						localClients.add(temp.get(j).getName()); 
						
					serverGui.setClientList(localClients); 
					serverGui.setGlobalClientList(allClients); 
					
					} catch (Exception e) {
					}
				} 
			}
	}
	
	public void printServers() throws RemoteException{
		Iterator<String> it = allServersList.iterator(); 
		while(it.hasNext())
			System.out.println(it.next()); 
	}
	public ArrayList<String> getAllServers() throws RemoteException{
		return allServersList; 
	}
	
	public void test() throws RemoteException{
		ArrayList<ClientInterface> c = this.getClients(); 
		for(int i = 0; i < c.size(); i++)
			System.out.println(c.get(i)); 
	}	
	
	 public static void main(String[] args) throws Exception {
	    try {
	    	for(int i = 0; i<args.length; i++){
	    		Server s = new Server(args[i]);
	    		String serverName = "rmi://" + HOST + "/Server/" + s.getName();
		 	    Naming.rebind(serverName,s);
		 	     /* CONTROLLARE SE SERVE */
		 	    Server.allServersList.add(s.getName());
		 	    Server.allServers.add(s); 
	    	}
	    	
	    } catch (RemoteException e) {
	        System.err.println("Errore di connessione.");
	        System.exit(1);
	    }
	 }	 
}
v
