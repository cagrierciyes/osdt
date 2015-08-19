
package org.rdv;

import java.util.Collection;
import java.util.List;

import javax.swing.JMenu;

import org.rdv.datapanel.DataPanel;
import org.w3c.dom.Document;

/***
 * Interface for managing main RDV window.  All layout and
 * window decoration of data panels and built-in panels (collectively 
 * referred to as views) are delegated to PanelManager.  Built-in
 * panels refer to control, channel, event marker, etc. Data panels
 * are user-created plots, tabular displays, etc.  PanelManager may
 * also handle drag-and-drop placement of windows.   
 * 
 * @author Drew Daugherty
 *
 */
public interface PanelManager {
  public void defaultLayout();
  
  public void closeAllDataPanels();
  public void setPanelLayout(PanelLayout layout);
  public PanelLayout getPanelLayout();
  
  public int addDataPanel(DataPanel dataPanel);
  public int addDataPanelToContainer(DataPanel dataPanel, String container);
  public void addDataPanel(int viewId,DataPanel dataPanel);
  //public void removeDataPanel(DataPanel dataPanel);
  
  public DataPanel getDataPanel(int viewId);
  public Collection<DataPanel> getDataPanels();
  
  public int getViewId(DataPanel dataPanel);
  public void updateViewTitle(int viewId);
  public void setVisible(int viewId, boolean visible);
  public boolean isVisible(int viewId);
  public void showView(int viewId);

  public int getDisplayPriority(DataPanel dataPanel);
  
  public boolean isAnyChannelSubscribed(List<String> channels);
  public void unsubscribeChannels(List<String> channels);
  
  public Document getConfigurationXML() 
                    throws Exception;
  public void setConfigurationXML(Document document)
                    throws Exception;
  
  public void releaseAllViews();
  public void buildMenu(JMenu menu);
}
