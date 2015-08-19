/*
 RDV 2.0 NCImportAction.java
 Author: Charles Cowart
 Date: 02/2010
 */

package org.rdv.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.rdv.RDV;
import org.rdv.ui.LoginDialog;
import org.rdv.ui.NCGetFileDialog2;

public class NCWebSvcImportAction extends DataViewerAction
{
	public NCWebSvcImportAction()
	{
		super("Import NEES Data (Web Svcs)", "Import data from NEES server to the local computer");
		
		ncGetFile = null;
	}
	
	NCGetFile ncGetFile;

	public void actionPerformed(ActionEvent ae)
	{
		LoginDialog myLoginDialog = null;
		
		try
		{
			JFrame frame = RDV.getInstance(RDV.class).getMainPanel().getFrame();
	  
			if(ncGetFile == null)
			{
				myLoginDialog = new LoginDialog(frame);
				if(myLoginDialog.isSuccess())
				{
					ncGetFile = new NCGetFile(myLoginDialog.name, myLoginDialog.password);
									
					myLoginDialog = null;
				}else
				{
					myLoginDialog = null;
					//user cancelled
					return;
				}
			}

			NCGetFileDialog2 getFileDialog = new NCGetFileDialog2(frame);
			
			String project, experiment, trial, repetition, relativePath;
			
			File localPath;
	  
			if(getFileDialog.isSuccess())
			{
				//Ready to Download
				String tmp = getFileDialog.path;
				
				if(tmp.startsWith("https://", 0) || tmp.startsWith("HTTPS://", 0))
				{
					//assume NEESCentral URL for file
					localPath = getFileDialog.localFile;
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					ncGetFile.writeFileToLocal(tmp, localPath);
					frame.setCursor(null); //turn off the wait cursor
					JOptionPane.showMessageDialog(frame, "Import Successful");
				}else
				{
					//assume NEESCentral full folder path for file
					if(tmp.startsWith("/", 0))
					{
						//remove any leading slash
						tmp = tmp.substring(1);
					}
					
					String[] tmp2 = tmp.split("/");
		
					
					if(tmp2.length < 5)
					{
						//assume path has at least 5 components (project, exp, trial, repetition, and name)
						//can be more if after repetition and before name there is an aribitrarily long set
						//of file paths.
						throw new Exception("Not a valid NEES file path");
					}	
						relativePath = "";
						project = tmp2[0];
						experiment = tmp2[1];
						trial = tmp2[2];
						repetition = tmp2[3];
						for(int i = 4; i < tmp2.length; i++)
						{
							relativePath += "/" + tmp2[i];
						}
						
						relativePath = relativePath.substring(1);
					
					
					localPath = getFileDialog.localFile;
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					int projectID = ncGetFile.FindProjectID(project);
					
					//writeFileToLocal requires a NEESCentral download URL for the file as well as a local path.
					//the URL is usually only given to users who right click on a file from NEESCentral.
					//thus, if we are given the file path, often from the user's memory, we need to find the URL.
					//if however the user gives us a valid URL from neescentral, then we can use the url.
					ncGetFile.writeFileToLocal(ncGetFile.getUserFileURI(projectID, experiment, trial, repetition, relativePath), localPath);
					frame.setCursor(null); //turn off the wait cursor
					JOptionPane.showMessageDialog(frame, "Import Successful");
				}

			}
		    
		}catch(Exception e)
		{
			ncGetFile = null;
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
		}
  }
}






