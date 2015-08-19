package org.rdv.ui;
/**
@author Drew Daugherty
*/

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataListener;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.rdv.DataViewer;
import org.rdv.action.DataImportAction;
import org.rdv.net.FTPAuthentication;
import org.rdv.ui.RDVIcon;

public class RemoteFilesListDialog extends JDialog implements ActionListener {
	//private static RemoteFilesListDialog dialog;
  private static String FILE_SEPARATOR="/";
	//private static String value = "";
 
	private JList list;
 
	FileSystemListModel model = new FileSystemListModel();
 
	String server, username, password;
 
	FTPClient ftp;
	
	JTextField fileName = new JTextField(15);
	
	ProgressWindowModal progressDlg_;
	
	FileByteTransfer byteTransfer_;
	
	final JButton importButton;
	
	JLabel pathLabel = new JLabel(FILE_SEPARATOR);
	
	CursorToolkit ctoolkit=new CursorToolkit();
	/**
	 * Set up and show the dialog. The first Component argument determines which
	 * frame the dialog depends on; it should be a component in the dialog's
	 * controlling frame. The second Component argument should be null if you
	 * want the dialog to come up with its left corner in the center of the
	 * screen; otherwise, it should be the component on top of which the dialog
	 * should appear.
	 * @param function TODO
	 * @throws IOException 
	 */
	public static void showDialog(Component frameComp, String title, FTPAuthentication auth) {
		Frame frame = JOptionPane.getFrameForComponent(frameComp);
		
		//if(JOptionPane.showConfirmDialog(frameComp, 
		//		"Press OK to Browse for it at " + server + "\nIt may take up to 15 seconds to connect to the server.", "Selecting", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
		//			RemoteFilesListDialog.connect(server,user,password);
		//} else 
		//	return null;
		//if(dialog == null) {
		  RemoteFilesListDialog	dialog = new RemoteFilesListDialog(frame, title, auth);
		//}
		//dialog.connect(server,user,password);
		//RemoteFilesListDialog.value = null;
		dialog.repopulateList();
		dialog.setVisible(true);
		//return value;
	}
	
 
	public void setProgress(float pctComplete)
	{
	  progressDlg_.setProgress(pctComplete);
	}
	
	public void retrieveOperationComplete()
	{ 
	  if(progressDlg_!=null){
	    //progressDlg_.setCursor(null);
	    ctoolkit.stopWaitCursor(progressDlg_.getRootPane());
      progressDlg_.dispose();
      progressDlg_=null;
    }
	  
	  if(byteTransfer_!=null){
	    if(byteTransfer_.error()){
	      JOptionPane.showMessageDialog(this, "Import failed.");
	    }else{
	      DataImportAction impData = new DataImportAction(this);
	      impData.importData(byteTransfer_.getFile(),byteTransfer_.getNEESFileName());
	      //JOptionPane.showMessageDialog(this, "Import succeeded.");
	    }
	    byteTransfer_=null;
	  }
	  
	  try {
      ftp.completePendingCommand();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
	}
	
	public void retrieveFile(FileSystemItem fsItem){
	  // confirm file name,
    // download file to temp file, launch file load
	  
	  progressDlg_= new ProgressWindowModal(this,"File Import Progress");
	  ctoolkit.startWaitCursor(progressDlg_.getRootPane());
	  //progressDlg_.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	  progressDlg_.setStatus("Importing NEEShub file " + fsItem.getName());
  
    try {
      
      if(fsItem.getName().endsWith(".txt") || fsItem.getName().endsWith(".TXT")){
        ftp.setFileType(FTP.ASCII_FILE_TYPE);
      }else{
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
      }
        
      InputStream inStream = ftp.retrieveFileStream(fsItem.getName());
      
      byteTransfer_=new FileByteTransfer(inStream,fsItem.getSize(),fsItem.getName(),this);
      Thread t = new Thread(byteTransfer_);
      t.start();
  
      //System.out.println("transferred "+trans.getTransferred()+"/"+fsItem.getSize()+" bytes to "+outputDataFile.getCanonicalPath());
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    
    // transfer control to modal dialog
    progressDlg_.setVisible(true);
    
	}
	
	public void repopulateList(String newDir) {
	  try {
	    ftp.changeWorkingDirectory(newDir);
	    pathLabel.setText(ftp.printWorkingDirectory());
	    pathLabel.setToolTipText(ftp.printWorkingDirectory());
	    repopulateList();
	  } catch (IOException e) {
	    e.printStackTrace(); 
	  }
	}
	
	protected void setImportBtnState(int index){   
    if (index < 0) return;
    list.ensureIndexIsVisible(index);
    
    ListModel dlm = list.getModel();
    if(index >= dlm.getSize()) return;
    FileSystemItem item = (FileSystemItem)dlm.getElementAt(index);
    
    if(item!=null){
      importButton.setEnabled(!item.isDir()); 
    }
	}
	
	private void repopulateList() {
	  setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		FTPFile[] files=null;
		try {
			model.clear();
			files = ftp.listFiles();
			int i = 0;
			// list directories before everything else
			for (i = 0; i < files.length; i++) {
			  //System.out.println(files[i].getName());
			  if(files[i].isDirectory())
					model.addElement(new FileSystemItem(files[i]));
			}
			for (i = 0; i < files.length; i++) {
        //System.out.println(files[i].getName());
        if(!files[i].isDirectory())
          model.addElement(new FileSystemItem(files[i]));
      }
		} catch (IOException e) {
		JOptionPane.showMessageDialog(null, e);
			e.printStackTrace();
		}
		if(files!=null){
		  list.setVisibleRowCount(files.length);
		}else{
		  list.setVisibleRowCount(-1);
		}
		setCursor(null);
		list.repaint();
		// tell scroll pane to refresh
		list.revalidate();
		setImportBtnState(list.getSelectedIndex());
	}
 
	private RemoteFilesListDialog(Frame frame, String title, FTPAuthentication auth) {
		super(frame, title, true);

		ftp=auth.getClient();
		ftp.addProtocolCommandListener(new PrintingCommandListener(
        new PrintWriter(System.out)));
		// Create and initialize the buttons.
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this);
 
		//
		importButton = new JButton("Import");
		importButton.setActionCommand("Import");
		importButton.addActionListener(this);
		importButton.setEnabled(false);
		getRootPane().setDefaultButton(importButton);
 
		JButton upButton = new JButton(RDVIcon.folderUp);
		upButton.setActionCommand("Up");
		upButton.addActionListener(this);
		
		// main part of the dialog
		list = new JList(model) {
			// Subclass JList to workaround bug 4832765, which can cause the
			// scroll pane to not let the user easily scroll up to the beginning
			// of the list. An alternative would be to set the unitIncrement
			// of the JScrollBar to a fixed value. You wouldn't get the nice
			// aligned scrolling, but it should work.
			public int getScrollableUnitIncrement(Rectangle visibleRect,
					int orientation, int direction) {
				int row;
				if (orientation == SwingConstants.VERTICAL && direction < 0
						&& (row = getFirstVisibleIndex()) != -1) {
					Rectangle r = getCellBounds(row, row);
					if ((r.y == visibleRect.y) && (row != 0)) {
						Point loc = r.getLocation();
						loc.y--;
						int prevIndex = locationToIndex(loc);
						Rectangle prevR = getCellBounds(prevIndex, prevIndex);
 
						if (prevR == null || prevR.y >= r.y) {
							return 0;
						}
						return prevR.height;
					}
				}
				return super.getScrollableUnitIncrement(visibleRect,
						orientation, direction);
			}
		};
 
		list.setCellRenderer(new FileSystemRenderer());
		//list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setVisibleRowCount(-1);
		list.addMouseListener(new FileSystemListMouseAdapter());
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(325, 250));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		
		// Create a container so that we can add a title around
		// the scroll pane. Can't add a title directly to the
		// scroll pane because its background would be white.
		// Lay out the label and scroll pane from top to bottom.
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		
		pathLabel.setLabelFor(list);
		listPane.add(upButton);
		listPane.add(Box.createRigidArea(new Dimension(0, 5)));
		listPane.add(pathLabel);
		listPane.add(Box.createRigidArea(new Dimension(0, 2)));
		listPane.add(listScroller);
		listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
		//if(function.equals("Save"))
		//	listPane.add(fileName);
		
		// Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(importButton);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(closeButton);
 
		// Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();
		contentPane.add(listPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
 
		pack();
		setLocationRelativeTo(frame);
	}
 
	// Handle clicks on the Select and Cancel buttons.
	public void actionPerformed(ActionEvent e) {
		if ("Import".equals(e.getActionCommand())) {
			FileSystemItem selected = (FileSystemItem) (list.getSelectedValue());
			if(selected != null && !selected.isDir()) {
			  retrieveFile(selected);  
			}
			//closeDialog();
			
		}else if ("Up".equals(e.getActionCommand())) {
		  System.out.println("go up");
		  try{
		    ftp.changeToParentDirectory();
		    pathLabel.setText(ftp.printWorkingDirectory());
	      pathLabel.setToolTipText(ftp.printWorkingDirectory());
		    repopulateList();
		  }catch(IOException ioe){}
		}else{
		  //cancel
		  closeDialog();
		}
		
	}
 
	private void closeDialog(){
	  setVisible(false);
    try {
      if (ftp.isConnected())
        ftp.disconnect();
    } catch (IOException e1) {
      JOptionPane.showMessageDialog(null, e1);
      e1.printStackTrace();
    }
    dispose();
	}
	
	/*public void connect(String server, String username, String password) throws IOException {
		ftp = new FTPClient();
		ftp.addProtocolCommandListener(new PrintingCommandListener(
				new PrintWriter(System.out)));
 
		try {
			int reply;
			ftp.connect(server);
			System.out.println("Connected to " + server + ".");
 
			// After connection attempt, you should check the reply code to
			// verify success.
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				System.err.println("FTP server refused connection.");
				JOptionPane.showMessageDialog(null,"FTP server refused connection.");
			}
		} catch (IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException e1) {
					throw e1;
				}
			}
			System.err.println("Could not connect to server.");
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e);
		}
		try {
			if (!ftp.login(username, password)) {
				ftp.logout();
			}
 
			System.out.println("Remote system is " + ftp.getSystemName());
			System.out.println("My directory is " + ftp.printWorkingDirectory());
 
			// Use passive mode as default because most of us are
			// behind firewalls these days.
			// ftp.enterLocalPassiveMode();
		} catch (FTPConnectionClosedException e) {
			System.err.println("Server closed connection.");
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
 
		
	} // end main*/


	private class FileSystemListMouseAdapter extends MouseAdapter {
	  
	  public void mouseClicked(MouseEvent e) {
	    
	    int index = list.locationToIndex(e.getPoint());
	    if (index < 0) return;
	    
	    setImportBtnState(index);
	    
	    if (e.getClickCount() == 2) {
	      //setButton.doClick(); // emulate button click  
	      list.ensureIndexIsVisible(index);
	      
	      ListModel dlm = list.getModel();
	      FileSystemItem item = (FileSystemItem)dlm.getElementAt(index);
	      
	      if(item!=null){
	        if(item.isDir()){  
  	        RemoteFilesListDialog.this.repopulateList(item.toString());
  	      }else{
  	        RemoteFilesListDialog.this.retrieveFile(item);
  	      }
	      }
	    }
	  }
	}
	
	private class PrintingCommandListener implements ProtocolCommandListener {
	  private PrintWriter __writer;
	 
	  public PrintingCommandListener(PrintWriter writer) {
	    __writer = writer;
	  }
	 
	  public void protocolCommandSent(ProtocolCommandEvent event) {
	    __writer.print(event.getMessage());
	    __writer.flush();
	  }
	 
	  public void protocolReplyReceived(ProtocolCommandEvent event) {
	    __writer.print(event.getMessage());
	    __writer.flush();
	  }
	}


	private class FileSystemItem{
	  private String name_;
	  private boolean isDir_ = false;
	  private long size_;
	  public FileSystemItem(FTPFile f){
	    
	    name_=f.getName();
	    isDir_=f.isDirectory();
	    size_=f.getSize();
	  }
	  
	  /*public FileSystemItem(String n, boolean dir){
	    name=n;
	    isDir=dir;
	  }*/
	  public String toString(){ return name_; }
	  public String getName(){ return name_; }
	  public boolean isDir(){return isDir_;}
	  public long getSize(){return size_;}
	}


	private class FileSystemListModel implements ListModel {
	  private Collection<FileSystemItem> collection=new ArrayList<FileSystemItem>();
	  
	  
	  public void clear(){
	    collection.clear();
	  }
	  
	  public void addElement(FileSystemItem i){
	    collection.add(i); 
	  }
	  
	  public FileSystemListModel(){
	    
	  }
	  
	  public FileSystemListModel(Collection<FileSystemItem> c) {
	    this.collection = c;
	  }

	  public Object getElementAt(int index) {
	    return(collection.toArray()[index]);
	  }

	  public int getSize() {
	    return(collection.size());
	  }

	  public void addListDataListener(ListDataListener l) {}

	  public void removeListDataListener(ListDataListener l) {}
	}

	private class FileSystemRenderer extends DefaultListCellRenderer {
	  private Icon dirIcon = RDVIcon.folder;
	  private Icon fileIcon = RDVIcon.data;
	  
	  public Component getListCellRendererComponent(JList list,
	                                                Object value,
	                                                int index,
	                                                boolean isSelected,
	                                                boolean hasFocus) {
	    JLabel label =
	      (JLabel)super.getListCellRendererComponent(list,
	                                                 value,
	                                                 index,
	                                                 isSelected,
	                                                 hasFocus);
	    if (value instanceof FileSystemItem) {
	      if(((FileSystemItem)value).isDir()){
	        label.setIcon(dirIcon);
	      } else {
	        // Clear old icon; needed in 1st release of JDK 1.2
	        label.setIcon(fileIcon); 
	      }
	    }
	    return(label);
	  }
	}

	/** Basic CursorToolkit that swallows mouseclicks */
	private class CursorToolkit {
	  private final MouseAdapter mouseAdapter =
	    new MouseAdapter() {};

	  private CursorToolkit() {}

	  /** Sets cursor for specified component to Wait cursor */
	  public void startWaitCursor(JComponent component) {
	    RootPaneContainer root =
	      ((RootPaneContainer) component.getTopLevelAncestor());
	    root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    root.getGlassPane().addMouseListener(mouseAdapter);
	    root.getGlassPane().setVisible(true);
	  }

	  /** Sets cursor for specified component to normal cursor */
	  public void stopWaitCursor(JComponent component) {
	    RootPaneContainer root =
	      ((RootPaneContainer) component.getTopLevelAncestor());
	    root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	    root.getGlassPane().removeMouseListener(mouseAdapter);
	    root.getGlassPane().setVisible(false);
	  }

	  /*public static void main(String[] args) {
	    final JFrame frame = new JFrame("Test App");
	    frame.getContentPane().add(
	      new JLabel("I'm a Frame"), BorderLayout.NORTH);
	    frame.getContentPane().add(
	      new JButton(new AbstractAction("Wait Cursor") {
	        public void actionPerformed(ActionEvent event) {
	          System.out.println("Setting Wait cursor on frame");
	          startWaitCursor(frame.getRootPane());
	        }
	      }));
	    frame.setSize(800, 600);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.show();
	  }*/
	}

	private class FileByteTransfer implements Runnable
	{
	  InputStream in_;
	  File outputDataFile_;
	  long totalBytes_=0, transferred_=0;
	  RemoteFilesListDialog dlg_;
	  String neesFileName_;
	  boolean hasError_=false;
	  
	  public FileByteTransfer(InputStream in, long totalBytes, String neesFileName, RemoteFilesListDialog dlg) throws IOException{
	    in_=in;
	    totalBytes_=totalBytes;
	    neesFileName_=neesFileName;
	    dlg_=dlg;
	    outputDataFile_ = File.createTempFile("NEEShub", ".dat");
	  }
	  
	  public void run() {
	    byte buffer[]=new byte[1024];
	    OutputStream outStream=null;
	    try{
	      outStream = new BufferedOutputStream(new FileOutputStream(outputDataFile_));  
	      
	      while (transferred_<totalBytes_){
	        int readCount = in_.read(buffer);
	        if(readCount<0) break;
	        outStream.write(buffer,0,readCount);
	        transferred_+=readCount;
	        dlg_.setProgress((float)transferred_/(float)totalBytes_);
	      }
	    }catch(IOException ioe){
	      hasError_=true;
	      ioe.printStackTrace();
	    }finally{
	      try{
	        if(outStream!=null) outStream.close();
	      }catch(IOException ignore1){}
	      try{
	        in_.close();
	      }catch(IOException ignore2){}
	      dlg_.retrieveOperationComplete();
	    }
	  }
	  
	  public String getNEESFileName(){return neesFileName_;}
	  
	  public File getFile(){
        return outputDataFile_;
	  }
	  
	  public long getTransferred(){
	    return transferred_;
	  }
	  
	  public boolean error(){
	    return hasError_;
	  }
	}
}



