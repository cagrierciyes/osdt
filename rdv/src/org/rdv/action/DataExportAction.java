/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/action/DataExportAction.java $
 * $Revision: 1330 $
 * $Date: 2008-12-08 11:13:36 -0500 (Mon, 08 Dec 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.rdv.RDV;
import org.rdv.data.ASCIIDataFileWriter;
import org.rdv.data.Channel;
import org.rdv.data.DataExporter;
import org.rdv.data.MATFileWriter;
import org.rdv.data.NumericDataSample;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBReader;
import org.rdv.ui.ExportDialog;
import org.rdv.ui.MainPanel;
import org.rdv.ui.ProgressWindow;
import org.rdv.ui.UIUtilities;

import com.rbnb.sapi.SAPIException;

/**
 * An action to export data from a RBNB server to disk
 * 
 * @author Jason P. Hanley
 * @see RBNBReader
 * @see DataExporter
 */
public class DataExportAction extends DataViewerAction {

  /** serialization version identifier */
  private static final long serialVersionUID = 8366769228683976744L;

  private DataExporter exporter_;
  private List<String> channels_;

  // the names of the data formats we can export to
  List<DataExporter> fileExporters_ = Arrays
      .asList(new DataExporter[] { 
           new ASCIIDataFileWriter(), new MATFileWriter() });

  
  public DataExportAction() {
    super("Export data channels",
        "Export data on the server to the local computer");
    //MainPanel mainPanel = RDV.getInstance(RDV.class).getMainPanel();
    //channels_ = channels;//mainPanel.getSelectedChannels();
  }

//  public DataExportAction(List<DataExporter> exporters){
//    this();
//    fileExporters_= exporters;
//  }
  
  public DataExportAction(List<String> channels, 
            List<DataExporter> exporters){
    this();
    fileExporters_= exporters;
    channels_ = channels;
  }
  
  /**
   * Show the data export dialog with all channels.
   */
  public void actionPerformed(ActionEvent ae) {
    if(channels_==null){
      MainPanel mainPanel = RDV.getInstance(RDV.class).getMainPanel();
      channels_ = mainPanel.getSelectedChannels();
    }
    exportData(channels_);
  }

  /**
   * Show the data export dialog with the specified channels.
   * 
   * @param channels
   *          the channels to export
   */
  public void exportData(List<String> channels) {
    // remove non-data channels
    for (int i=channels.size()-1; i>=0; i--) {
      Channel channel = RBNBController.getInstance().getChannel(channels.get(i));
      String mime = channel.getMetadata("mime");
      if (!mime.equals("application/octet-stream")) {
        channels.remove(i);
      }
    }

    // don't bring up the dialog if there are no channels specified
    if (channels == null || channels.isEmpty()) {
      JOptionPane.showMessageDialog(null,
          "There are no data channels selected.", "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    // show the export dialog
    ExportDialog dialog = ExportDialog.showDialog(channels, fileExporters_);
    if (dialog == null) {
      return;
    }

    exporter_=dialog.getExporter();
    if(exporter_==null){
      JOptionPane.showMessageDialog(null,
          "No export format selected.", "Error",
          JOptionPane.ERROR_MESSAGE);
    }else{
      exportData(dialog.getFile(), dialog.getSelectedChannels(), 
        dialog.getStartTime(), dialog.getEndTime());
    }
  }

  /**
   * Export the data contained in the channels, bounded by the start and end
   * time, to the file using the specified writer. This will spawn a thread to
   * do the actual exporting.
   * 
   * @param channels
   *          the channels to export
   * @param startTime
   *          the start time for the data
   * @param endTime
   *          the end time for the data
   * @see #exportDataThread(String, List, double, double, ProgressWindow)
   */
  private void exportData(final File file, final List<String> channels, final double startTime,
      final double endTime) {
    // create a window for progress
    final ProgressWindow progressWindow = new ProgressWindow(
        UIUtilities.getMainFrame(),
        "Exporting data...");
    progressWindow.setStatus(exporter_.getStatus()); //"Exporting data to " + file.getName());
    progressWindow.setVisible(true);

    // the server to get data from
    final String rbnbServer = RBNBController.getInstance()
        .getRBNBConnectionString();

    // spawn a thread to do the actual exporting
    new Thread("Export") {
      public void run() {
        // export the data and catch any exception
        boolean error = false;
        try {
          exportDataThread(rbnbServer, file, channels, 
                        startTime, endTime, progressWindow);
        } catch (SAPIException e) {
          error = true;
          e.printStackTrace();
        } catch (IOException e) {
          error = true;
          e.printStackTrace();
        }

        progressWindow.dispose();

        // show a completion or error message
        if (!error) {
          JOptionPane.showMessageDialog(UIUtilities.getMainFrame(),
              "Export complete.",
              "Export complete",
              JOptionPane.INFORMATION_MESSAGE);
        } else {
          JOptionPane.showMessageDialog(UIUtilities.getMainFrame(),
              "There was an error exporting the data.",
              "Export failed",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    }.start();
  }

  /**
   * Export the data.
   * 
   * @param rbnbServer
   *          the rbnb server to export from
   * @param channels
   *          the channels to export
   * @param startTime
   *          the start time for the data
   * @param endTime
   *          the end time for the data
   * @param progressWindow
   *          the windows to post progress
   * @throws SAPIException
   *           if there is an error communicating with the server
   * @throws IOException
   *           if there is an error writing the data file
   */
  public void exportDataThread(String rbnbServer, File file, 
      List<String> channels, double startTime, double endTime, 
      ProgressWindow progressWindow) 
    throws SAPIException, IOException {

    RBNBReader reader = null;

    try {
      RBNBController rbnb = RBNBController.getInstance();
      List<Channel> channelMetadata = rbnb.getChannels(channels);

      reader = new RBNBReader(rbnbServer, channelMetadata, startTime, endTime);
      
      exporter_.init(file, channelMetadata, startTime, endTime);

      progressWindow.setProgress(0);

      NumericDataSample sample;
      while ((sample = reader.readSample()) != null) {
        exporter_.writeSample(sample);

        float progress = (float) ((sample.getTimestamp() - startTime) / (endTime - startTime));
        progressWindow.setProgress(progress);

        progressWindow.setStatus(exporter_.getStatus());//"Exporting data to " + file.getName() + " ("
            //+ DataViewer.formatDate(sample.getTimestamp()) + ")");
      }
    } catch(Exception e){
      e.printStackTrace();
    }finally {
    
      if (reader != null) {
        reader.close();
      }

      exporter_.close();
    }

  }

}