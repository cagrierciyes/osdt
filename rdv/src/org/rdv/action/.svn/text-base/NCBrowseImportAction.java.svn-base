/*
 RDV 2.0 NCImportAction.java
 Author: Charles Cowart
 Date: 02/2010
 */

package org.rdv.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.rdv.RDV;
import org.rdv.net.FTPAuthentication;
import org.rdv.ui.LoginDialog;
import org.rdv.ui.RemoteFilesListDialog;

public class NCBrowseImportAction extends DataViewerAction
{
	public NCBrowseImportAction()
	{
		super("Import NEES Data (Browser)", "Import data from NEES server using file browser");
		
	}


	public void actionPerformed(ActionEvent ae)
	{
		LoginDialog myLoginDialog = null;
		JFrame frame = RDV.getInstance(RDV.class).getMainPanel().getFrame();
		while(true){
  		myLoginDialog = new LoginDialog(frame);
  		if(myLoginDialog.isSuccess())
  		{
  		  FTPAuthentication auth = new FTPAuthentication(myLoginDialog.name, myLoginDialog.password,"neesws.neeshub.org");  
  		  if (auth.authenticate()){
          RemoteFilesListDialog.showDialog(frame,"Import NEES Data",auth);
          return;
  		  }else{
  		    JOptionPane.showMessageDialog(frame, auth.getErrorMessage());
  		  }
  							
  			myLoginDialog = null;
  		}else {
  			myLoginDialog = null;
  			//user cancelled
  			return;
  		}
		}
	}
  
}






