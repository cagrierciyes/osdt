/*
 RDV 2.0 NCExportAction.java
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
import org.rdv.ui.NCPutFileDialog2;


public class NCExportAction2 extends DataViewerAction
{
	public NCExportAction2()
	{
		super("Export data file to NEESCentral",
			  "Export local data file to NEESCentral server");
		ncPutFile = null;
	}
	
	NCPutFile ncPutFile;
	
	public void actionPerformed(ActionEvent ae)
	{
		LoginDialog myLoginDialog = null;
		
		try
		{
			JFrame frame = RDV.getInstance(RDV.class).getMainPanel().getFrame();
			
			if(ncPutFile == null)
			{
				myLoginDialog = new LoginDialog(frame);
				if(myLoginDialog.isSuccess())
				{
					ncPutFile = new NCPutFile(myLoginDialog.name, myLoginDialog.password);
					
					myLoginDialog = null;
				}else
				{
					myLoginDialog = null;
					//user cancelled
					return;
				}
			}
			
			NCPutFileDialog2 putFileDialog = new NCPutFileDialog2(frame);
			
			
			String project, experiment, trial, repetition, relativePath;
			File localPath;
			
			if(putFileDialog.isSuccess())
			{
				//Ready to Upload
				localPath = putFileDialog.localFile;
				String tmp = putFileDialog.path;
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
					throw new Exception("Not a valid NEESCentral file path");
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

				String ncPath = "/nees/home/" + project + ".groups/" + experiment + "/" + trial + "/" + repetition;
				
				String[] tmp3 = relativePath.split("/");
				
				String ncName;
				
				if(tmp3.length == 1)
				{
					//User has specified 'filename', and not 'Converted_Data/filename', 'Unprocessed_Data/filename', etc.
					ncName = tmp3[0];
				}else
				{
					//User has specified 'Converted_Data/filename', 'Unprocessed_Data/filename', etc.
					for(int i = 0; i < tmp3.length - 1; i++)
					{
						ncPath += "/" + tmp3[i];
					}
					ncName = tmp3[tmp3.length-1];
				}
				
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				ncPutFile.writeFileToNEESCentral(localPath, ncName, ncPath);
				frame.setCursor(null);
				JOptionPane.showMessageDialog(frame, "Export Successful");

			}
			
		}catch(Exception e)
		{
			ncPutFile = null;
			JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
		}
	}

  
}