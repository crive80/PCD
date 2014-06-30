import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("serial")
public class Client extends UnicastRemoteObject implements ClientInterface{
	private String name; 
	private int download; 
	private CheckServer sc = null; 
	private ArrayList<Resource> files; 
	//private ArrayList<Resource> files2; 
	private ServerInterface connected; 
	private ArrayList<String> activeClients = new ArrayList<String>(); 
	private static final String HOST="localhost";
	private ClientGUI clientGUI; 
	private int DELAY = 1000; 
	private Object lock = new Object(); 
	private DownloadScheduler scheduler; 
	
	
	public Client(String n, String s, int c, ArrayList<Resource> r) throws RemoteException{
		name = n; 
		download = c; 
		files = r; 
		clientGUI = new ClientGUI(name,this);
		connect(s); 
		//System.out.println(s); 
		printLocalFiles(this); 		
		//connected.printServers();
	}
	
	public void connect(String s) throws RemoteException{ 
		try {
			connected = (ServerInterface)Naming.lookup("rmi://" + HOST + "/Server/" + s);
			//System.out.println("connesso al server: " + connected.getName()); 
			connected.addClient(this);
			sc = new CheckServer(); sc.start();
			
		} catch (MalformedURLException e) {
			clientGUI.appendLog("Errore di connessione");
		} catch (NotBoundException e) {
			clientGUI.appendLog("Errore nel binding");
		} 
	}
	public void addActiveClient(String c)throws RemoteException{
		activeClients.add(c); 
	}
	public void rmActiveClient(String c)throws RemoteException{
		activeClients.remove(c); 
	}
	public ArrayList<String> getActiveClients()throws RemoteException{
		return activeClients; 
	}
	public boolean checkActiveClients(String Name) throws RemoteException{
		boolean trovato = false; 
		for(int i = 0; i < activeClients.size(); i++){
			if(activeClients.get(i).equals(name))
				trovato = true; 
		}
		return trovato; 
	}
	public ArrayList<Resource> getFilesFromServer() throws RemoteException{
		ArrayList<Resource> temp = connected.getFiles(); 
		return temp; 
	}
	
	public void printLocalFiles(Client c) throws RemoteException{
		ArrayList<String> temp = new ArrayList<String>();
		ArrayList<Resource> risorse = c.getFiles(); 
		for(int i = 0; i < risorse.size(); i++){
			temp.add(risorse.get(i).getName());
			//System.out.print(i); 
		}
		clientGUI.setResourceList(temp); 
	}
	
	public void printSearchResults(ArrayList<ClientInterface> c, ClientInterface owner) throws RemoteException{
    	Iterator<ClientInterface> iter = c.iterator();
    	while(iter.hasNext()) {
    	    ClientInterface key = iter.next();
    	    if(key.getName().equals(owner.getName()))
    	    	clientGUI.appendLog("trovato uguale a " + owner.getName()); 
    	    else{
    	    	clientGUI.appendLog("ricevuta lista da " + key.getName()); 
    	    }
    	}
    }
	public Resource getResource(String s) throws RemoteException{
		int index = 0; 
		for(int i = 0; i < files.size(); i++){
			if(files.get(i).getName().equals(s))
				index = i; 
		}
		return files.get(index); 
	}

	public boolean checkFile(String r) throws RemoteException{
		boolean found = false; 
		for(int i = 0; i < files.size(); i++){
			if(files.get(i).getName().equals(r))
				found = true; 
		}
		return found; 
	}

	public void newResource(ArrayList<ClientInterface> c, ClientInterface owner, String r) 
	throws IOException{
		c.remove(owner);
		if(c.size() > 0){
			Resource file = c.get(0).getResource(r);  
			scheduler = new DownloadScheduler(c, file); 
			scheduler.start(); 
		}
		
	}
	
	public ArrayList<Resource> getFiles() throws RemoteException{
		return files; 
	}
	
	public String getName() throws RemoteException{
		return name; 
	}
	public int getDownload() throws RemoteException{
		return download; 
	}
	public ServerInterface getConnected() throws RemoteException{
		return connected; 
	}
	
	public byte[] fileToByteArray(Resource r) throws IOException, RemoteException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(r);
		byte[] b = baos.toByteArray(); 
		return b; 
	}
	
	public Resource byteArrayToFile(byte[] b) throws IOException, ClassNotFoundException{
		ByteArrayInputStream bis = new ByteArrayInputStream(b);
	    ObjectInput in = new ObjectInputStream(bis);
	    Resource r2 = (Resource) in.readObject();
	    
	    return r2; 
	}
	
	public int requestPart(Resource r, int part, String clientName, ClientInterface client)throws RemoteException{
		int temp; 
		clientGUI.addDownloadList("richiesta parte #"+part+"al Client "+clientName);
		client.addActiveClient(clientName); 
		temp = client.uploadPart(part); 
		clientGUI.addDownloadList("ottenuta parte #"+part+"dal Client "+clientName);
		return temp; 
	}
	
	public int uploadPart(int p)throws RemoteException{
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
			clientGUI.appendLog("Download Interrotto");
		} 
		return p; 
	}
	
	public class CheckServer extends Thread{
		CheckServer(){ setDaemon(true); }
		public void run(){
			while(true){
				try{
					String[] list = Naming.list("rmi://" + HOST + "/Server/"); 
					String[] names = new String[list.length];  
					for(int i = 0; i<list.length; i++){
							 ServerInterface t = (ServerInterface)Naming.lookup(list[i]); 
                             names[i] = t.getName(); 
						}
					clientGUI.setServerList(names);
				}catch (Exception e) {  
					clientGUI.appendLog("Server non più presente nel sistema"); 
                return;
            }
				try {
					sleep(10);
				} catch (InterruptedException e) {
					clientGUI.appendLog("Daemon di controllo server interrotto"); 
					return; 
				} 
			}
		}
	}
	
	class DownloadScheduler extends Thread{
		ArrayList<ClientInterface> clients; 
		Resource r; 
		int voidParts; // numero di parti rimanenti
		int[] check; // array per controllare la parti scaricate
		int limit;
		boolean[] cBool; 
		
		DownloadScheduler(ArrayList<ClientInterface> clients, Resource r){
			this.clients = clients; 
			this.r = r; 
			voidParts = r.getParts();
			check = new int[r.getParts()];
			limit = Client.this.download;
			cBool = new boolean[clients.size()]; 
			for(int i = 0; i<cBool.length; i++)
				cBool[i] = false; 
			for(int i = 0; i < check.length; i++){
				check[i] = 0; 
			}
			
		}
		public void modCBool(int i, boolean b){
			cBool[i] = b; 
		}
		public boolean[] getBool(){ return cBool; }
		
		public void run(){
			synchronized(lock){
				DlThread[] T = new DlThread[r.getParts()]; 
				int n = 0; 
				while(!checkCompleted(check, r)){
					if(limit > 0){
						n = connections(limit, voidParts, clients.size()); 
						int j = 0; 
						for(int i = 0; i<n; i++){
							int nvp = getNextVoidPart(check); 
							T[nvp-1] = new DlThread(r, clients.get(j), nvp, check); 
							T[nvp-1].start(); 
							try {
								if(clients.get(j).checkActiveClients(Client.this.getName())){
									T[nvp-1].join();
								}
									
							} catch (RemoteException e) {
								clientGUI.appendLog("errore di connessione");
							} catch (InterruptedException e) {
								clientGUI.appendLog("thread interrotto");
							} 
							limit--; 
							j++; 
							if(j>clients.size())
								j = 0; 
							}
						}
						voidParts = getNVoidParts(check);
					    limit = limit+n; 
					}					
				}
			}
		
		
		public void printList(Resource r, ClientInterface c, int i, int l) throws RemoteException{
			ArrayList<String> temp = clientGUI.getDownloadList(); 
			String s = r.getName() + "[" + (i+1) + "] " + "from " + c.getName() + "{waiting}";
			if(!temp.contains(s))
				temp.add(s); 
			for(int j = 0; j<temp.size(); j++){
				clientGUI.addDownloadList(temp.get(j));
			}
			
		}
		
		public int connections(int limit, int parts, int clients){
			int temp = limit; 
			if(parts < temp) temp = parts; 
			if(clients < temp) temp = clients; 
			return temp; 				
		}
			
		public boolean checkCompleted(int[] b, Resource r){
			boolean completed = true; 
			for(int i = 0; i <b.length; i++){
				if(b[i] == 0) completed = false; 
			}
			if(completed){
				byte[] temp;
				try {
					temp = clients.get(0).fileToByteArray(r);
					sleep(100); 
					files.add(Client.this.byteArrayToFile(temp)); 
					Client.this.printLocalFiles(Client.this); 
				} catch (RemoteException e) {
					clientGUI.appendLog("Errore di connessione");
				} catch (IOException e) {
					clientGUI.appendLog("Errore in I/O");
				} catch (InterruptedException e) {
					clientGUI.appendLog("interruzione");
				} catch (ClassNotFoundException e) {
					clientGUI.appendLog("ClassNotFound exception");
				} 
			}
			 return completed; 
		}
		
		public synchronized int getNVoidParts(int[] c){
			int t = 0; 
			boolean trovata = false; 
			while(!trovata && t<c.length){
				if(c[t] == 0) trovata = true; 
				else t++; 
			} 
			return t; 
		}
		public int getNextVoidPart(int[] c){// DA RIFARE CON WHILE
			int index = 0; 
			for(int i = 0; i<c.length; i++)
				if(c[i]==0) {
					index = i; 
					break; 
				}
			return index + 1; 
			}
	}
	
	public class DlThread extends Thread{
		Resource r; 
		ClientInterface c; 
		int part; 
		int[] check; 

		DlThread(Resource r, ClientInterface c, int part, int[] check){
			this.r = r;
			this.c = c; 
			this.part = part; 
			this.check = check; 
		}
		
		public ClientInterface getClient() {
			return c; 
		}
		
		public void run(){
				try {
					if(!checkPart(check, part)){
						//int index = clientGUI.getDownloadListSize() - r.getParts() + part - 1;				                
						this.modifyCheck(part, check); 
					    requestPart(r, part, c.getName(), c);
					    c.rmActiveClient(Client.this.getName()); 
					}
				} catch (RemoteException e) {
					clientGUI.appendLog("Problemi di connessione durante il download.");
					scheduler.interrupt(); 
				}
				}
		public void modifyCheck(int part, int[] c){
			c[part-1] = part; 
		}
		public boolean checkPart(int[] c, int i){
			if(c[i-1]==0) return false; 
			return true; 
		}
	}
	
	public static void main(String[] args) {
	     try {
	        ArrayList<Resource> aux = new ArrayList<Resource>();
	        for (int i=3; i<args.length; i+=2) {
	            aux.add(new Resource(args[i],Integer.parseInt(args[i+1])));
	        }
	        new Client(args[0],args[1],Integer.parseInt(args[2]),aux);	        
	        } catch (Exception e) {
	            System.err.println("errore nella creazione del client");
	        }
	        
	}
}
