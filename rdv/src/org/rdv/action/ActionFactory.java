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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/action/ActionFactory.java $
 * $Revision: 1266 $
 * $Date: 2008-08-07 10:40:08 -0400 (Thu, 07 Aug 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.action;

import java.util.List;

import org.rdv.data.DataExporter;

/**
 * A factory class to manage actions.
 * 
 * @author Jason P. Hanley
 */
public class ActionFactory {
  /** the instance of this class */
  private static ActionFactory instance;
  
  /** the action to import data files */
  private DataImportAction dataImportAction;
  
  /** the action to export data file */
  private DataExportAction dataExportAction;

  /** the action to control offline mode */
  private OfflineAction offlineAction;
  
  /** the action to import OpenSees specific xml data file */
  private OpenSeesDataImportAction osDataImportAction;

  private NCExportAction2 ncExportAction;

  private DataViewerAction ncBrowseImportAction;
  
  private DataViewerAction ncWebImportAction;

  /**
   * Creates the action factory. 
   */
  protected ActionFactory() {
    super();
  }

  /**
   * Gets the instance of the action factory.
   * 
   * @return  the action factory
   */
  public static ActionFactory getInstance() {
    if (instance == null) {
      instance = new ActionFactory();
    }
    
    return instance;
  }
  
  /**
   * Gets the action for importing a data file.
   * 
   * @return  the data import action
   */
  public DataImportAction getDataImportAction() {
    if (dataImportAction == null) {
      dataImportAction = new DataImportAction();
    }
    
    return dataImportAction;
  }
  
  /**
   * Gets the action for exporting data to a file.
   * 
   * @return  the data export action
   */
  public DataExportAction getDataExportAction() {
    //if (dataExportAction == null) {
    //  dataExportAction = new DataExportAction();
    //}
    
    //return dataExportAction;
    
    //MainPanel mainPanel = RDV.getInstance(RDV.class).getMainPanel();
    return new DataExportAction();
  }
  
  public DataExportAction getDataExportAction(List<String> channels, 
                                        List<DataExporter> exporters) {
//    if (dataExportAction == null) {
//      dataExportAction = new DataExportAction();
//    }
    
    return new DataExportAction(channels, exporters);
  }
  
  public DataViewerAction getNCBrowseImportAction() {
    if (ncBrowseImportAction == null) {
      ncBrowseImportAction = new NCBrowseImportAction();
    }
    
    return ncBrowseImportAction;
  }

  public DataViewerAction getNCWebSvcImportAction() {
    if (ncWebImportAction == null) {
      ncWebImportAction = new NCWebSvcImportAction();
    }
    
    return ncWebImportAction;
  }
  
  /**
   * Gets the action to control offline mode.
   * 
   * @return  the offline action
   */
  public OfflineAction getOfflineAction() {
    if (offlineAction == null) {
      offlineAction = new OfflineAction();
    }
    
    return offlineAction;
  }
  
  /**
   * Gets the OpenSees DataImportAction
   * @return the OpenSeesDataImportAction
   */
  public OpenSeesDataImportAction getOpenSeesDataImportAction() {
    if (osDataImportAction == null) {
      osDataImportAction = new OpenSeesDataImportAction();
    }
    
    return osDataImportAction;
  }


  public NCExportAction2 getNCExportAction() {
    if (ncExportAction == null) {
      ncExportAction = new NCExportAction2();
    }
    
    return ncExportAction;
  }


}
