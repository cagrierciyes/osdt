
package org.rdv;

import java.util.Collection;
import java.util.HashSet;

import org.rdv.datapanel.DataPanel;
import org.rdv.ui.UIUtilities;

/**
 * Manage access to the PanelManager instance and swapping 
 * between panel managers.
 * @author Drew Daugherty
 *
 */
public class ManagerStore {
  /** the single instance of this class */
  private static PanelManager panelManager_=null;
  public static boolean defaultIsBasic=true;
  
  public static synchronized PanelManager swapBasicPanelManager(){
    Collection<DataPanel> dataPanels = new HashSet<DataPanel>();
    
    if (panelManager_ != null) {
      if(panelManager_ instanceof DataPanelManager){
        return panelManager_;
      }
      dataPanels=panelManager_.getDataPanels();
      // release views but don't close
      panelManager_.releaseAllViews();
    }
    panelManager_ = new DataPanelManager(UIUtilities.getMainPanel());
    for(DataPanel panel: dataPanels){
      //System.out.println("adding panel "+
      //    new SeriesFixedWidthTitleFormatter().getTitle(panel));
      panelManager_.addDataPanel(panel);
    }
    panelManager_.defaultLayout();
    return panelManager_;
  }
  
  public static synchronized PanelManager swapDockingPanelManager(){
    Collection<DataPanel> dataPanels = new HashSet<DataPanel>();
    
    if (panelManager_ != null) {
      if(panelManager_ instanceof DockingDataPanelManager){
        return panelManager_;
      }
      dataPanels=panelManager_.getDataPanels();
      panelManager_.releaseAllViews();
    }
    panelManager_ = new DockingDataPanelManager(UIUtilities.getMainPanel());
    for(DataPanel panel: dataPanels){
      //System.out.println("adding panel "+
      //    new SeriesFixedWidthTitleFormatter().getTitle(panel));
      panelManager_.addDataPanel(panel);
    }
    panelManager_.defaultLayout();
    return panelManager_;
  }
  
  public static synchronized void createDefaultPanelManager(){
    if(defaultIsBasic)
      swapBasicPanelManager();
    else
      swapDockingPanelManager();
  }
  /**
  * Gets the single panelManager_ of this class.
  * 
  * @return  the single panelManager_ of this class
  */
  public static synchronized PanelManager getPanelManager() {
    if (panelManager_ == null) {
      createDefaultPanelManager();
    }
    
    return panelManager_;
  }
}
