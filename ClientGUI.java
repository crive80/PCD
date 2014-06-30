import javax.swing.*;

import java.awt.*;

import javax.swing.border.*;

import java.util.ArrayList;
import java.awt.event.*;
import java.awt.Font;
import java.rmi.*;
import java.io.IOException;


/**
 *
 * 
 */
public class ClientGUI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private String title; 
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int width;
    private int height;
    private JPanel contentPane;
    private JPanel topPanel;
    private JPanel centerWestPanel;
    private JPanel centerEastPanel;
    private JPanel centerPanel;
    private JPanel bottomPanel;
    private JTextArea log;
    private JTextField searchField;
    private JButton searchButton;
    private JList<String> serverList;
    private JList<String> downloadQueue;
    private ArrayList<String> downloadList;
    private JList<String> resourceList;
    private JScrollPane logScrollPane; 
    private JScrollPane servScrollPane; 
    private JScrollPane downScrollPane;
    private JScrollPane resScrollPane; 
    private ClientInterface clientReference;
    
    class WindowEventHandler extends WindowAdapter {
        public void windowClosing(WindowEvent evt) {
            System.out.println("Window closed");
            
        }
    }

    private void setMainPanel() {
        contentPane = new JPanel();
        contentPane.setOpaque(true);
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(5, 5));
    }
    
    private void setTopPanel() {
        topPanel = new JPanel();
        topPanel.setOpaque(true);
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK),
                "Cerca file",TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial",Font.PLAIN,10)));
        
        searchField = new JTextField(10);
        searchButton = new JButton("Cerca");
        searchButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		String s = searchField.getText();
        		//System.out.println(s); 
        		 if (s.length() == 0) {
                     popError("Stringa di ricerca errata!");
                     return;
                 }
        		 try {
        			 if(!clientReference.checkFile(s)){
        				 eraseDownloadList();
        				 ServerInterface s1 = clientReference.getConnected(); 
            			 ArrayList<ClientInterface> temp = s1.searchFile(s); 
            			 //System.out.println(temp); 
            			 clientReference.printSearchResults(temp, clientReference); 
                		 clientReference.newResource(temp, clientReference, s); 
        			 }
        			 else{
        				 popError("Risorsa già presente"); 
        			 }
        			
        			// }
        			 					
				} catch (RemoteException e1) {} catch (IOException e1) {
					appendLog("Errore in I/O"); 
				} 
        	}
        });
        
        topPanel.add(searchField,BorderLayout.WEST);
        topPanel.add(searchButton,BorderLayout.EAST);
        }
    
    private void setCenterPanel() {
        centerWestPanel = new JPanel();
        centerWestPanel.setOpaque(true);
        centerWestPanel.setBackground(Color.WHITE);
        centerWestPanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK),
                "Server disponibili",TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial",Font.PLAIN,10)));
        serverList = new JList<String>();
        serverList.setFont(new Font("Arial",Font.BOLD,10));
        centerWestPanel.setPreferredSize(new Dimension(width/4 -25, height/2));
        centerWestPanel.setLayout(new BoxLayout(centerWestPanel,BoxLayout.PAGE_AXIS));
        servScrollPane = new JScrollPane(serverList); 
        centerWestPanel.add(servScrollPane);

        centerEastPanel = new JPanel();
        centerEastPanel.setOpaque(true);
        centerEastPanel.setBackground(Color.WHITE);
        centerEastPanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK),
                "Risorse disponibili",TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial",Font.PLAIN,10)));
        centerEastPanel.setPreferredSize(new Dimension(width/4 -25, height/2));
        centerEastPanel.setLayout(new BoxLayout(centerEastPanel,BoxLayout.PAGE_AXIS));
        resourceList = new JList<String>();
        resourceList.setFont(new Font("Arial",Font.BOLD,10));
        resScrollPane = new JScrollPane(resourceList);
        resScrollPane.setPreferredSize(centerEastPanel.getSize());
        centerEastPanel.add(resScrollPane);

        centerPanel = new JPanel();
        centerPanel.setOpaque(true);
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK),
                "Coda download",TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial",Font.PLAIN,10)));
        downloadQueue = new JList<String>();
        downloadQueue.setFont(new Font("Arial",Font.BOLD,10));
        downloadList = new ArrayList<String>();
        downScrollPane = new JScrollPane(downloadQueue); 
        downScrollPane.setPreferredSize(centerPanel.getSize());
        centerPanel.add(downloadQueue);
      
        DefaultListModel<String> model = new DefaultListModel<String>();
        downloadQueue.setModel(model);
    }
    
    private void setBottomPanel() {
        bottomPanel = new JPanel();
        bottomPanel.setOpaque(true);
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK),
                "Log",TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial",Font.PLAIN,10)));
        bottomPanel.setLayout(new BorderLayout());
        log = new JTextArea();
        log.setFont(new Font("Arial",Font.PLAIN,12));
        log.setRows(10);
        log.setEditable(false);
        log.setBackground(Color.BLACK);
        log.setForeground(Color.GREEN);
        logScrollPane = new JScrollPane(log); 
        
        bottomPanel.add(logScrollPane, BorderLayout.CENTER);
    }
    
    public ClientGUI(String n, ClientInterface r) {
        super(n);
        clientReference = r;
        title = n;        
        width = (int) screenSize.getWidth()/2;
        height = (int) screenSize.getHeight() - 50;
        
        setMainPanel();
        setTopPanel();
        setCenterPanel();
        setBottomPanel();
        contentPane.add(topPanel,BorderLayout.PAGE_START);
        contentPane.add(centerWestPanel,BorderLayout.WEST);
        contentPane.add(centerEastPanel,BorderLayout.EAST);
        contentPane.add(centerPanel,BorderLayout.CENTER);
        contentPane.add(bottomPanel,BorderLayout.PAGE_END);
        setContentPane(contentPane);
        
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
        	   public void windowClosing(WindowEvent evt) {
        	     onExit();
        	   }
        	  });
        /*addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                try {
					clientReference.getConnected().removeClient(clientReference);
				} catch (RemoteException e1) {
					//appendLog("Client disconnesso"); 
					System.out.println("client sconnesso");
				}
                System.exit(0);//cierra aplicacion
            }
        });*/
        setSize(width, height);
        setVisible(true);
    }
    
    public void onExit() {
    	  System.err.println("Exit");
    	  System.exit(0);
    	}

    public void setServerList(String[] s) {
        DefaultListModel<String> model = new DefaultListModel<String>();
        for (int i=0; i<s.length; i++) { model.addElement(s[i]); }
        serverList.setModel(model);
    }

    public void setResourceList(ArrayList<String> l) {
        DefaultListModel<String> model = new DefaultListModel<String>();
        for (int i=0; i<l.size(); i++) { model.addElement(l.get(i)); }
        resourceList.setModel(model);
    }

    public void setDownloadQueue() {
        DefaultListModel<String> model = new DefaultListModel<String>();
        for (int i=0; i<downloadList.size(); i++) { model.addElement(downloadList.get(i)); }
        downloadQueue.setModel(model);
    }
   
    public void appendLog(String s) {
        log.append(s + "\n");
    }

    public void popError(String message) {
        JOptionPane.showMessageDialog(this,message);
    }

    public void addDownloadList(String l) {
        downloadList.add(l);
        setDownloadQueue();
    }

    public void popDownloadList(int n) {
        for (int i=downloadList.size()-1; i>= downloadList.size()-n; i++) {
            downloadList.remove(i);
        }
    }

    public int getDownloadListSize() {
        return downloadList.size();
    }
    
    public ArrayList<String> getDownloadList(){
    	return downloadList; 
    }

    public void eraseDownloadList() {
        downloadList.clear();
        setDownloadQueue();
    }

    public void modifyDownloadList(int index, String s) {
        downloadList.set(index,s);
        setDownloadQueue();
    }
}
