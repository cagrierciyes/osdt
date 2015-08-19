/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
 * Copyright (c) 2008 Palta Software
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/action/DataImportAction.java $
 * $Revision: 1281 $
 * $Date: 2008-11-26 08:51:56 -0500 (Wed, 26 Nov 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.action;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.rdv.data.Channel;
import org.rdv.data.DataFileReader;
import org.rdv.data.NumericDataSample;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBException;
import org.rdv.rbnb.RBNBSource;
import org.rdv.ui.ProgressWindow;
import org.rdv.ui.UIUtilities;

/**
 * An action to import data files.
 * 
 * @author Jason P. Hanley
 *
 */
public class DataImportAction extends DataViewerAction {

  /** serialization version identifier */
  private static final long serialVersionUID = 4075250858285498451L;

  /** the window to show the progress of the import */
  private ProgressWindow progressWindow;
  
  /** the number of samples to collect before sending the data to the server */
  private static int SAMPLES_PER_FLUSH = 50;
  
  private Dialog dlg_=null;
  
  public DataImportAction(Dialog dlg){
    super("Import data file",
        "Import local data to RBNB server");
    dlg_=dlg;
  }
  
  /** constructor with generic parameters */
  public DataImportAction(String text, String desc, Dialog dlg) {
    super(text, desc);
  }
  
  public DataImportAction() {
    super("Import data file",
          "Import local data to RBNB server");
  }
  
  /** constructor with generic parameters */
  public DataImportAction(String text, String desc) {
    super(text, desc);
  }
  
  /**
   * Prompts the user for the data file and uploads the data to the RBNB server.
   */
  public void actionPerformed(ActionEvent ae) {
    importData();
  }
  
  /**
   * Prompts the user for the data file and uploads the data to the RBNB server.
   */
  public void importData() {
    File dataFile = UIUtilities.getFile("Import");
    
    if (dataFile == null) {
      return;
    }
    
    if (!dataFile.exists()) {
      return;
    }
    
    if (!dataFile.isFile()) {
      return;
    }
    
    importData(dataFile);
  }
  
  /**
   * Uploads the data to the RBNB server.
   * 
   * @param dataFile  the data file
   */
  public void importData(File dataFile) {
    importData(dataFile, null);
  }
  
  /**
   * Uploads the data to the RBNB server and optionally deletes the data file
   * after import.
   * 
   * @param dataFile        the data file
   * @param deleteDataFile  if true delete the data file after import
   */
  public void importData(File dataFile, boolean deleteDataFile) {
    importData(dataFile, null, deleteDataFile);
  }
  
  /**
   * Uploads the data to the RBNB server using the given source name.
   * 
   * @param dataFile    the data file
   * @param sourceName  the source name
   */
  public void importData(File dataFile, String sourceName) {
    importData(dataFile, sourceName, false);
  }
  
  /**
   * Uploads the data to the RBNB server using the given source name and
   * optionally deletes the data file after import.
   * 
   * @param dataFile        the data file
   * @param sourceName      the source name
   * @param deleteDataFile  if true delete the data file after import
   */
  public void importData(File dataFile, String sourceName, boolean deleteDataFile) {
    URL dataFileURL = null;
    try {
      dataFileURL = dataFile.toURI().toURL();
    } catch (MalformedURLException e) {}
    
    importData(dataFileURL, sourceName, deleteDataFile);
  }
  
  /**
   * Uploads the data to the RBNB server.
   * 
   * @param dataFile  the data file URL
   */
  public void importData(URL dataFile) {
    importData(dataFile, null);
  }
  
  /**
   * Uploads the data to the RBNB server using the given source name.
   * 
   * @param dataFile    the data file URL
   * @param sourceName  the source name
   */
  public void importData(URL dataFile, String sourceName) {
    importData(dataFile, sourceName, false);
  }

  /**
   * Uploads the data to the RBNB server using the given source name and
   * optionally deletes the data file after import.
   * 
   * @param dataFile        the data file URL
   * @param sourceName      the source name
   * @param deleteDataFile  if true delete the data file after import
   */
  private void importData(URL dataFile, String sourceName, boolean deleteDataFile) {
    if (sourceName == null || sourceName.length() == 0) {
      sourceName = getDefaultSourceName(dataFile);
    }    
    
    importData(Collections.singletonList(dataFile),
               Collections.singletonList(sourceName),
               deleteDataFile);
  }
  
  /**
   * Uploads the data to the RBNB server.
   * 
   * @param dataFiles  the data files
   */
  public void importData(List<URL> dataFiles) {
    List<String> sourceNames = new ArrayList<String>();
    for (URL dataFile : dataFiles) {
      sourceNames.add(getDefaultSourceName(dataFile));
    }
    
    importData(dataFiles, sourceNames, false);
  }  
  
  /**
   * Uploads the data to the RBNB using the given source names. This will happen
   * in a separate thread. The data files will optionally be deleted after the
   * import is complete (file URL's only).
   * 
   * @param dataFiles        the data files
   * @param sourceNames      the source names
   * @param deleteDataFiles  if true delete the data files after import
   */
  public void importData(final List<URL> dataFiles, final List<String> sourceNames, 
                        final boolean deleteDataFiles) {
    if(dlg_!=null)
      progressWindow = new ProgressWindow(dlg_, "Importing data...");
    else
      progressWindow = new ProgressWindow(UIUtilities.getMainFrame(),
      "Importing data...");
      
    progressWindow.setVisible(true);
    
    new Thread() {
      public void run() {
        boolean error = false;
        try {
          importDataThread(dataFiles, sourceNames);
        } catch (Exception e) {
          error = true;
          e.printStackTrace();
        }
        
        if (deleteDataFiles) {
          for (URL dataFile : dataFiles) {
            if (dataFile.getProtocol().equals("file")) {
              try {
                File file = new File(dataFile.toURI());
                file.delete();
              } catch (URISyntaxException e) {
                e.printStackTrace();
              }
            }
          }
        }
        
        progressWindow.dispose();
        
        RBNBController.getInstance().updateMetadata();        

        if (!error) {
          if(dlg_!=null)
            JOptionPane.showMessageDialog(dlg_,
                "Import complete.",
                "Import complete",
                JOptionPane.INFORMATION_MESSAGE);
          else
            JOptionPane.showMessageDialog(UIUtilities.getMainFrame(),
              "Import complete.",
              "Import complete",
              JOptionPane.INFORMATION_MESSAGE);        
          
        } else {
          if(dlg_!=null)
            JOptionPane.showMessageDialog(dlg_,
                "There was an error importing the data file.",
                "Import failed",
                JOptionPane.ERROR_MESSAGE);
          else
            JOptionPane.showMessageDialog(UIUtilities.getMainFrame(),
              "There was an error importing the data file.",
              "Import failed",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    }.start();
  }
  
  /**
   * The thread that does the importing.
   * 
   * @param dataFiles       the list of data files to import
   * @param sourceNames     the names of the sources for the data files
   * @throws IOException    if there is an error reading the data files
   * @throws RBNBException  if there is an error importing the data
   */
  private void importDataThread(List<URL> dataFiles, List<String> sourceNames) throws IOException, RBNBException {
    for (int i=0; i<dataFiles.size(); i++) {
      URL dataFile = dataFiles.get(i);
      String sourceName = sourceNames.get(i);
      
      progressWindow.setStatus("Importing data file " + getFileName(dataFile));
      float minProgress = (float)(i)/dataFiles.size();
      float maxProgress = (float)(i+1)/dataFiles.size();
      
      DataFileReader reader = new DataFileReader(dataFile);
      
      if (reader.getProperty("samples") == null) {
        throw new IOException("Unable to determine the number of data samples.");
      }

      int samples = Integer.parseInt(reader.getProperty("samples"));
      int archiveSize = (int)Math.ceil((double)samples / SAMPLES_PER_FLUSH);
      
      RBNBController rbnb = RBNBController.getInstance();
      RBNBSource source = new RBNBSource(sourceName, archiveSize,
          rbnb.getRBNBHostName(), rbnb.getRBNBPortNumber());      
      
      List<Channel> channels = reader.getChannels();
      for (Channel channel : channels) {
        source.addChannel(channel.getName(),
                          "application/octet-stream",
                          channel.getUnit());        
      }      

      int currentSample = 0;
      
      NumericDataSample sample;
      while ((sample = reader.readSample()) != null) {
        double timestamp = sample.getTimestamp();
        Number[] values = sample.getValues();
        for (int j=0; j<values.length; j++) {
          if (values[j] == null) {
            continue;
          }
          source.putData(channels.get(j).getName(), timestamp, values[j].doubleValue());
        }
        
        currentSample++;
        if (currentSample % 50 == 0) {
          source.flush();
        }
        
        if (samples != -1) {
          progressWindow.setProgress(minProgress + maxProgress * currentSample/samples);
        }
      }
      
      source.flush();
      source.close();
      
      progressWindow.setProgress(maxProgress);
    }
  }
  
  /**
   * Gets the name of the file from the URL.
   * 
   * @param file  the file URL
   * @return      the name of the file
   */
  private static String getFileName(URL file) {
    String fileName = file.getPath();

    int lastPathIndex = fileName.lastIndexOf('/');
    if (fileName.length() > lastPathIndex+1) {
      fileName = fileName.substring(lastPathIndex+1);
    }
    
    // fix for possible non-ascii characters encoded in file name
    try {
      fileName = URLDecoder.decode(fileName, "UTF-8");
    } catch (UnsupportedEncodingException ue) {  }
    
    return fileName;    
  }
  
  /**
   * Gets the default name of the source for the given the file. This will be
   * the name of the file without the extension.
   * 
   * @param dataFile  the data file
   * @return          the source name
   */
  private static String getDefaultSourceName(URL dataFile) {
    String sourceName = getFileName(dataFile);
    
    int dotIndex = sourceName.lastIndexOf('.');
    if (dotIndex != -1) {
      sourceName = sourceName.substring(0, dotIndex);
    }
    
    return sourceName;
  }
  
}