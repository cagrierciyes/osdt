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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/DataPanelManager.java $
 * $Revision: 1292 $
 * $Date: 2008-11-26 10:13:10 -0500 (Wed, 26 Nov 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.action.DataViewerAction;
import org.rdv.datapanel.DataPanel;
import org.rdv.datapanel.SeriesFixedWidthTitleFormatter;
import org.rdv.ui.DataPanelContainer;
import org.rdv.ui.MainPanel;
import org.rdv.ui.TabListener;
import org.rdv.ui.TabbedPane;
import org.rdv.ui.RDVIcon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jgoodies.uif_lite.component.Factory;

/**
 * A class to manage data panels with a simple tabbed interface.
 * 
 * @author  Jason P. Hanley
 * @author  Wei Deng
 * @author  Drew Daugherty
 */
public class DataPanelManager implements PanelManager, TabListener {

  /**
   * The logger for this class.
   */
  static Log log = LogFactory.getLog(DataPanelManager.class.getName());
  
  private int currPanelId_=PanelManagerView.BASE_VIEW_ID;
  
  /**
   * Tabbed interface object.
   */
  private TabbedPane tabbedInterface;

  private JSplitPane splitPane_;
  
  /**
   * Channel list plus metadata pane.  
   */
  private JComponent channelListComponent_;
  
  private JComponent controlComponent_;

  private JComponent audioComponent_; 

  private JComponent markerComponent_; 
 
  private MainPanel mainPanel_;
  
  private boolean manualPanelClose_=false;
  
  /**
   * A reference to the data panel container for the data panels to add their ui
   * component too.
   */
  //private List<DataPanelContainer> containers;

  /**
   * A list of all the data panels.
   */
  //private Map<DataPanelContainer, Set<DataPanel>> dataPanels;
  /**
   * map from id # to data panel.
   */
  private Map<Integer, DataPanel> panelIdMap_ = new HashMap<Integer,DataPanel>();
  
  /**
   * The constructor for the data panel manager. This initializes the data panel
   * container and the list of registered data panels.
   */
  DataPanelManager(MainPanel mainPanel) {
    mainPanel_=mainPanel;
    //containers = new ArrayList<DataPanelContainer>();
    //dataPanels = new HashMap<DataPanelContainer, Set<DataPanel>>();
    
    tabbedInterface = new TabbedPane();

    /* adds scroll buttons to the tabbed interface */
    tabbedInterface.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    DataPanelContainer c = new DataPanelContainer(this);
    //containers.add(0, c);

    tabbedInterface.insertTab(" Default ", null, c, "", 0);
    //dataPanels.put(c, new LinkedHashSet<DataPanel>());
    tabbedInterface.addTabListener(this);
    
    //defaultLayout();
  }

  public void releaseAllViews(){
    releaseAllDataPanels();
    mainPanel_.removeAll();
  }
  
  public void defaultLayout(){
    manualPanelClose_=false;
    audioComponent_=mainPanel_.getAudioPlayerComponent();
    channelListComponent_=mainPanel_.getChannelListComponent(); 
    controlComponent_=mainPanel_.getControlComponent(); 
    markerComponent_=mainPanel_.getMarkerSubmitComponent(); 
    mainPanel_.removeAll();
    mainPanel_.setLayout(new BorderLayout());
    
    JToolBar controlBar = new JToolBar();
    controlBar.setLayout(new BorderLayout());
    controlBar.add(controlComponent_,BorderLayout.CENTER);
    mainPanel_.add(controlBar, BorderLayout.NORTH);
    log.info("Added control panel.");    
    
    JPanel  rightPanel = new JPanel();
      rightPanel.setMinimumSize(new Dimension(0, 0));
      rightPanel.setLayout(new GridBagLayout());
    

       GridBagConstraints c= new GridBagConstraints();
       c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.ipadx = 0;
        c.ipady = 0;
        c.insets = new java.awt.Insets(8,0,8,6);
        c.anchor = GridBagConstraints.NORTHWEST;
        rightPanel.add(tabbedInterface, c);
        log.info("Added data panel container.");    
    
      audioComponent_.setVisible(false);
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0;
      c.weighty = 0;
      c.gridx = 0;
      c.gridy = 2;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.ipadx = 0;
      c.ipady = 0;
      c.insets = new java.awt.Insets (0, 0, 8, 6);
      c.anchor = GridBagConstraints.SOUTHWEST;        
      rightPanel.add (audioComponent_, c);
      log.info ("Added Audio Player Panel.");
      
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0;
      c.weighty = 0;
      c.gridx = 0;
      c.gridy = 3;
      c.gridwidth = 2;
      c.gridheight = 1;
      c.ipadx = 0;
      c.ipady = 0;
      c.insets = new java.awt.Insets (0, 0, 8, 6);
      c.anchor = GridBagConstraints.SOUTHWEST;        
      rightPanel.add (markerComponent_, c);
      log.info ("Added Marker Submission Panel.");
    
   splitPane_ = Factory.createStrippedSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        channelListComponent_,
        rightPanel,
        0.2f);
    splitPane_.setContinuousLayout(true);
    mainPanel_.add(splitPane_, BorderLayout.CENTER);
    
    
    for(DataPanel p : panelIdMap_.values()){
      addDataPanelToContainer(p, 
          new SeriesFixedWidthTitleFormatter().getTitle(p));
    }
    
    mainPanel_.validate();
    manualPanelClose_=true;
  }
  

  /**
   * Returns the data panel container where data panels can add their ui
   * components too.
   * 
   * @return the data panel container
   */
  public DataPanelContainer getDataPanelContainer() {
    return (DataPanelContainer) tabbedInterface.getSelectedComponent();
  }

  /**
   * Returns the tabbed pane where data panels containers are added to.
   * 
   * @return the data panel container
   */
  public TabbedPane getTabbedDataPane() {
    return tabbedInterface;
  }

  /**
   * Adds panel to appropriate container.
   * 
   * @param dataPanel The data panel to be added.
   */
  public int addDataPanelToContainer(DataPanel dataPanel, String containerName) {
    int viewId=getViewId(dataPanel);
    if(viewId<0){
      viewId=getNewPanelId(dataPanel);
      panelIdMap_.put(viewId, dataPanel);
      dataPanel.setPanelManager(this);
    }
    deleteDataPanelFromContainers(dataPanel);
    
    DataPanelContainer c = findContainer(containerName);
    if(c==null){
      c = addContainer(containerName); 
    }
    
    //int viewId=getViewId(dataPanel);
    c.addDataPanel(dataPanel);
    return viewId;
  }
  
  public void removeDataPanel(DataPanel dataPanel){
    deleteDataPanelFromContainers(dataPanel);
    
    int viewId = getViewId(dataPanel);
    panelIdMap_.remove(viewId);
  }
  
  public void deleteDataPanelFromContainers(DataPanel dataPanel){
    int containerIndex = getContainerIndex(dataPanel);
    if(containerIndex>=0){
      DataPanelContainer container = (DataPanelContainer) 
                tabbedInterface.getComponentAt(containerIndex);
      container.removeDataPanel(dataPanel); 
    }
  }
  
  /**
   * Adds panel but doesn't display it.
   * 
   * @param dataPanel The data panel to be added.
   */
  public int addDataPanel(DataPanel dataPanel) {
    if(!panelIdMap_.values().contains(dataPanel)){
      int viewId=getNewPanelId(dataPanel);
      addDataPanel(viewId, dataPanel);
      return viewId;
    }else{
      return getViewId(dataPanel);
    }
  }
  
  private int getNewPanelId(DataPanel dataPanel){
    return currPanelId_++;
  }
  
  /**
   * Adds panel with specified view id to currently displayed
   * tab container.
   * 
   * @param dataPanel The data panel to be added.
   */
  @Override
  public void addDataPanel(int viewId, DataPanel dataPanel) {
    panelIdMap_.put(viewId, dataPanel);
    dataPanel.setPanelManager(this);
    getDataPanelContainer().addDataPanel(dataPanel); 
  }
  
  /**
   * Ensures the view is visible.
   * 
   * @param dataPanel The data panel to be added.
   */
  public void showView(int viewId) {
   
    if(viewId<0){
      setVisible(viewId,true);
    }else{
      DataPanel dataPanel = panelIdMap_.get(viewId);
      if(dataPanel != null){
        int containerIndex = getContainerIndex(dataPanel);
        tabbedInterface.setSelectedIndex(containerIndex);
        
      }
    }
  }
  
  public boolean isVisible(int viewId){
    if(viewId<0){
      switch(viewId){
      case PanelManagerView.AUDIO_PLAYER_VIEW_ID:
        return audioComponent_.isVisible();
      case PanelManagerView.CHANNEL_LIST_VIEW_ID:
        return channelListComponent_.isVisible();
      case PanelManagerView.CONTROL_VIEW_ID:
        return controlComponent_.isVisible();
      case PanelManagerView.MARKER_VIEW_ID:
        return markerComponent_.isVisible();  
      }
      
    }else{
      DataPanel dataPanel = panelIdMap_.get(viewId);
      DataPanelContainer currContainer =(DataPanelContainer) 
                                        tabbedInterface.getComponentAt(
                                                    tabbedInterface.getSelectedIndex());
      return (currContainer!=null && dataPanel!=null && 
          currContainer.contains(dataPanel));
    }
    return false;
  }
  
  @Override
  public void setVisible(int viewId, boolean visible){
    if(viewId<0){
      switch(viewId){
      case PanelManagerView.AUDIO_PLAYER_VIEW_ID:
        audioComponent_.setVisible(visible);
        break;
      case PanelManagerView.CHANNEL_LIST_VIEW_ID:
        channelListComponent_.setVisible(visible);
        splitPane_.resetToPreferredSizes();
        break;
      case PanelManagerView.CONTROL_VIEW_ID:
        controlComponent_.setVisible(visible);
        break;
      case PanelManagerView.MARKER_VIEW_ID:
        markerComponent_.setVisible(visible);  
      }
      
    }else{
      if(visible){
        showView(viewId);
      }
    }
  }
  
  private int getContainerIndex(DataPanel dataPanel){
    for (int i = 0; i < tabbedInterface.getTabCount(); i++) {
      DataPanelContainer container = (DataPanelContainer) tabbedInterface
          .getComponentAt(i);
      if(container.contains(dataPanel)){
        return i;
      }
    }
    return -1;
  }
  
  /**
   * Calls the closePanel method on the specified data panel.
   * 
   * After this method has been called this data panel instance has reached the
   * end of its life cycle.
   * 
   * @param dataPanel
   *          the data panel to be closed
   * @see DataPanel#closePanel()
   */
  public void closeDataPanel(DataPanel dataPanel) {
//    for (DataPanelContainer container : containers) {
//      container.removeDataPanel(dataPanel); 
//    }
    if(manualPanelClose_){
      for(Integer viewId: panelIdMap_.keySet()){
        DataPanel panel = panelIdMap_.get(viewId);
        if(panel==dataPanel){
          panelIdMap_.remove(viewId);
          break;
        }
      }
    }
  }
  
//  public void detachDataPanel(DataPanel dataPanel) {  
//    for (DataPanelContainer container : containers) {
//      container.setAttached(dataPanel, false);
//    }
//  }
//  
//  public void maximizeDataPanel(DataPanel dataPanel) {    
//    for (DataPanelContainer container : containers) {
//      container.setMaximized(dataPanel, true); 
//    }
//  }
//
//  public void restoreDataPanel(DataPanel dataPanel) {    
//    for (DataPanelContainer container : containers) {
//      container.setMaximized(dataPanel, false); 
//    }
//  }
//
//  public void attachDataPanel(DataPanel dataPanel) {
//    for (DataPanelContainer container : containers) {
//      container.setAttached(dataPanel, true);
//    }
//  }

  /**
   * Release datapanels but dont call <code>closePanel</code>.
   * 
   * @see DataPanel#closePanel()
   */
  private void releaseAllDataPanels() {
    //int count = 0;
    int i=0;
    
    for (i = 0; i < tabbedInterface.getTabCount(); i++) {
      DataPanelContainer container = (DataPanelContainer) tabbedInterface
          .getComponentAt(i);

      log.debug("Closing tab "+i+" data panels.");
      List<DataPanel> panels = container.getDataPanels();// dataPanels.get(container);
      Iterator<DataPanel> iter = panels.iterator();
      while (iter.hasNext()) {
        DataPanel p = iter.next();
        //p.closePanel();
        container.removeDataPanel(p);
        closeDataPanel(p);  
        //iter.remove();
      }
    }
    
    tabbedInterface.removeAll();
  }
  
  /**
   * Calls the <code>closePanel</code> method on each registered data panel.
   * 
   * @see DataPanel#closePanel()
   */
  public void closeAllDataPanels() {
    //int count = 0;
    int i=0;
    
    for (i = 0; i < tabbedInterface.getTabCount(); i++) {
      DataPanelContainer container = (DataPanelContainer) tabbedInterface
          .getComponentAt(i);

      log.debug("Closing tab "+i+" data panels.");
      List<DataPanel> panels = container.getDataPanels();// dataPanels.get(container);
      Iterator<DataPanel> iter = panels.iterator();
      while (iter.hasNext()) {
        DataPanel p = iter.next();
        p.closePanel();
        container.removeDataPanel(p);
        closeDataPanel(p);  
        //iter.remove();
      }
      //set = null;
      //count++;
    }
    
    int count = tabbedInterface.getTabCount()-1;
    // Don't delete the first tab in the list
    //if (count > 0) {
      for (i = 0; i < count; i++) {
        // Don't delete the first tab in the list
        
            tabbedInterface.removeTabAt(1);
//            break;
          //}
        //}
      }
      //iterContainers.remove();
    //}

    
    // TODO: need some way to turn off tabs when there is only one...
    // this produces null ptr exception
    //tabbedInterface.getTabComponentAt(0).setVisible(false);
  }

//  /**
//   * View the channel specified by the given channel name. This will cause the
//   * data panel manager to look for the default viewer for the mime type of the
//   * viewer.
//   * 
//   * If the channel has no mime type, it is assumed to be numeric data unless
//   * the channel ends with .jpg, in which case it is assumed to be image or
//   * video data.
//   * 
//   * @param channelName
//   *          the name of the channel to view
//   */
//  public void viewChannel(String channelName) {
//    viewChannel(channelName, getDefaultExtension(channelName));
//	}
//  
//  /**
//   * View the channel given by the channel name with the specified extension.
//   * 
//   * @param channelName  the name of the channel to view
//   * @param extension    the extension to view the channel with
//   */
//  public void viewChannel(String channelName, Extension extension) {
//    if (extension != null) {
//      log.info("Creating data panel for channel " + channelName + " with extension " + extension.getName() + ".");
//      try {
//        DataPanel dataPanel = createDataPanel(extension,
//            getDataPanelContainer());
//        if (!dataPanel.setChannel(channelName)) {
//          log.error("Failed to add channel " + channelName + ".");
//          closeDataPanel(dataPanel);
//        }
//      } catch (Exception e) {
//        log.error("Failed to create data panel and add channel.");
//        e.printStackTrace();
//        return;
//      }     
//    } else {
//      log.warn("Failed to find data panel extension for channel " + channelName + ".");
//    }    
//  }
  
//  /**
//   * View the list of channels with the specified extension. If the channel
//   * supports multiple channels, all the channels will be viewed in the same
//   * instance. Otherwise a new instance will be created for each channel.
//   * 
//   * If adding a channel to the same instance of an extension fails, a new
//   * instance of the extension will be created and the channel will be added
//   * this this.
//   * 
//   * @param channels   the list of channels to view
//   * @param extension  the extension to view these channels with
//   */
//  public void viewChannels(List<String> channels, Extension extension) {
//    DataPanel dataPanel = null;
//    try {
//      dataPanel = createDataPanel(extension, getDataPanelContainer());
//    } catch (Exception e) {
//      e.printStackTrace();
//      return;
//    }
//    boolean supportsMultipleChannels = dataPanel.supportsMultipleChannels();
//    
//    //for (String channel : channels) {
//      if (supportsMultipleChannels) {
//        boolean subscribed = dataPanel.addChannels(channels);
//
//        if (!subscribed) {
//          try {
//            dataPanel = createDataPanel(extension, getDataPanelContainer());
//          } catch (Exception e) {
//            e.printStackTrace();
//            return;
//          }
//          
//          dataPanel.addChannels(channels);
//        }
//      } else {
//        if (dataPanel == null) {
//          try {
//            dataPanel = createDataPanel(extension, getDataPanelContainer());
//          } catch (Exception e) {
//            e.printStackTrace();
//            return;
//          }
//        }
//        
//        dataPanel.setChannel(channels.iterator().next());
//        dataPanel = null;
//      }
//    //}    
//  }
	

  private DataPanelContainer addContainer(String name){
    DataPanelContainer c = new DataPanelContainer(this);
    //containers.add(c);

    String tabName=" "+name+" ";
    tabbedInterface.addTab(tabName, c);
    return c;
    //dataPanels.put(c, new LinkedHashSet<DataPanel>());
  }
  
  private DataPanelContainer findContainer(String name){
    DataPanelContainer container = null;
  
    for (int i = 0; i < tabbedInterface.getTabCount(); i++) {
      String tabTitle = tabbedInterface.getTitleAt(i).trim();
      if (tabTitle.equals(name.trim())) {
        container = (DataPanelContainer) tabbedInterface.getComponentAt(i);
      }
    }
    return container; 
  }
  
  public DataPanel getDataPanel(int viewId){
    return panelIdMap_.get(viewId);
  }
  
//  /**
//   * Creates the data panel referenced by the supplied extension ID. This first
//   * finds the extension onbject then calls createDataPanel with this.
//   * 
//   * @param extensionID  the ID of the extension to create
//   * @throws Exception
//   * @return             the newly created data panel
//   */
//  public DataPanel createDataPanel(String extensionID, String tabName)
//      throws Exception {
//
//    DataPanelContainer container = null;
//    for (int i = 0; i < tabbedInterface.getTabCount(); i++) {
//      if (tabbedInterface.getTitleAt(i).equals(tabName)) {
//        container = (DataPanelContainer) tabbedInterface.getComponentAt(i);
//      }
//    }
//
//    if (container == null) {
//      DataPanelContainer c = new DataPanelContainer();
//      containers.add(c);
//
//      tabbedInterface.addTab(tabName, c);
//      //dataPanels.put(c, new LinkedHashSet<DataPanel>());
//
//      container = c;
//    }
//
//    return createDataPanel(getExtension(extensionID), container);
//  }

//  /**
//   * Creates the data panel referenced by the supplied extension ID. This ID is
//   * by convention, the name of the class implementing this extension. It then
//   * places the newly created data panel into the current open tab.
//   * 
//   * @param extension
//   *          the extension to create
//   * @throws Exception
//   * @return the newly created data panel
//   */
//  public DataPanel createDataPanel(Extension extension) throws Exception {
//    return createDataPanel(extension, this.getDataPanelContainer());
//  }
//
//  /**
//   * Creates the data panel referenced by the supplied extension ID. This ID is
//   * by convention, the name of the class implementing this extension.
//   * 
//   * @param extension
//   *          the extension to create
//   * @throws Exception
//   * @return the newly created data panel
//   */
//  private DataPanel createDataPanel(Extension extension,
//      DataPanelContainer container) throws Exception {
//    Class<?> dataPanelClass;
//    try {
//      dataPanelClass = Class.forName(extension.getID());
//    } catch (ClassNotFoundException e) {
//      throw new Exception("Unable to find extension " + extension.getName());
//    }
//
//    DataPanel dataPanel = (DataPanel) dataPanelClass.newInstance();
//
//    dataPanel.setPanelManager(this);
//    container.addDataPanel(dataPanel);
//    //Set<DataPanel> set = dataPanels.get(container);
//    //set.add(dataPanel);
//    
//    return dataPanel;
//  }

  /**
   * See if any data panel are subscribed to at least one of these channels.
   * 
   * @param channels  the names of the channels to check
   * @return             true if any data panel is subscribed to the channels,
   *                     false otherwise
   */
  public boolean isAnyChannelSubscribed(List<String> channels) {

    for (int i = 0; i < tabbedInterface.getTabCount(); i++) {
      DataPanelContainer container = (DataPanelContainer) 
                              tabbedInterface.getComponentAt(i);
      for(DataPanel panel: container.getDataPanels()){
        for (String channel : channels) {
          if(panel.isChannelSubscribed(channel)){
            return true;
          }
        }
      }
    }
//    for (Set<DataPanel> set : dataPanels.values()) {
//      for (DataPanel dataPanel : set) {
//        for (String channel : channels) {
//          if (dataPanel.isChannelSubscribed(channel))
//            return true;
//        }
//      }
//    }
    
    return false;
  }
  
  /**
   * Unsubscribe all data panels from these channels. Data panels will be
   * closed if they are subscribed to no additional channels.
   * 
   * @param channel   the channel to unsubscribe
   */
  @Override
  public void unsubscribeChannels(List<String> channels) {

    for (int i = 0; i < tabbedInterface.getTabCount(); i++) {
      DataPanelContainer container = (DataPanelContainer) 
                              tabbedInterface.getComponentAt(i);
      for(DataPanel panel: container.getDataPanels()){
        for (String channel : channels) {
          panel.removeChannel(channel);
        }
      }
    }
    closeEmptyDataPanels();
  }
  
  /**
   * Close data panels that aren't subscribed to any channels.
   */
  private void closeEmptyDataPanels() {
    for (int i = 0; i < tabbedInterface.getTabCount(); i++) {
      DataPanelContainer container = (DataPanelContainer) 
                              tabbedInterface.getComponentAt(i);
      for(DataPanel panel: container.getDataPanels()){
        if (panel.subscribedChannelCount() == 0) {
          panel.closePanel();
          container.removeDataPanel(panel);
          closeDataPanel(panel);
          //iter.remove();
        }
      }
    }
    
//    for (Set<DataPanel> set : dataPanels.values()) {
//      Iterator<DataPanel> iter = set.iterator();
//      while (iter.hasNext()) {
//        DataPanel dataPanel = iter.next();
//        if (dataPanel.subscribedChannelCount() == 0) {
//          dataPanel.closePanel();
//          iter.remove();
//        }
//      }
//    }
  }
  
  /**
   * Return a map of DataPanels to names of tabs.
   * 
   * @return A map of data panels -> tab names.
   */
  public Collection<DataPanel> getDataPanels() {
    // LinkedHashMaps maintain order of elements as their inserted into a map
    Collection<DataPanel> panelSet = new HashSet<DataPanel>();

    for (int i = 0; i < tabbedInterface.getTabCount(); i++) {
      String tabName = tabbedInterface.getTitleAt(i);
      DataPanelContainer container = (DataPanelContainer) tabbedInterface
          .getComponentAt(i);

      //Set<DataPanel> set = dataPanels.get(container);

      for (DataPanel c : container.getDataPanels()) {
        panelSet.add((DataPanel) c);
      }
    }

    return panelSet;
  }

  public int getDisplayPriority(DataPanel p){
    for (int i = 0; i < tabbedInterface.getTabCount(); i++) {
      DataPanelContainer container = (DataPanelContainer) tabbedInterface
          .getComponentAt(i);
       
      if(container.contains(p)){
        return Math.abs(i-tabbedInterface.getSelectedIndex());
      }
//      Set<DataPanel> set = dataPanels.get(container);
//
//      for (DataPanel c : set) {
//        if(c==p)return i;
//      }
    }
    return -1;
  }
  
  public void buildMenu(JMenu menu){
    Action dataPanelAction;
    Action dataPanelHorizontalLayoutAction;
    Action dataPanelVerticalLayoutAction;
    Action dataPanelTiledLayoutAction;
    
    dataPanelAction = new DataViewerAction("Arrange", "Arrange Data Panel Orientation", KeyEvent.VK_D);
    
    dataPanelHorizontalLayoutAction = new DataViewerAction("Horizontal Data Panel Orientation", "", -1, RDVIcon.vertical) {
      /** serialization version identifier */
      private static final long serialVersionUID = 3356151813557187908L;

      public void actionPerformed(ActionEvent ae) {
        setPanelLayout(PanelLayout.HORIZONTAL_LAYOUT);
      }     
    };    
    
    dataPanelVerticalLayoutAction = new DataViewerAction("Vertical Data Panel Orientation", "", -1, RDVIcon.horizontal) {
      /** serialization version identifier */
      private static final long serialVersionUID = -4629920180285927138L;

      public void actionPerformed(ActionEvent ae) {
        setPanelLayout(PanelLayout.VERTICAL_LAYOUT);
      }     
    };    
    
    dataPanelTiledLayoutAction = new DataViewerAction("Tiled Data Panel Orientation", "", -1, RDVIcon.tiled) {
     /** serialization version identifier */
      private static final long serialVersionUID = 7918194370046044968L;

      public void actionPerformed(ActionEvent ae) {
        setPanelLayout(PanelLayout.TILED_LAYOUT);
      }     
    }; 
    
    JMenu dataPanelSubMenu = new JMenu(dataPanelAction);
    
    ButtonGroup dataPanelLayoutGroup = new ButtonGroup();
    
    final JMenuItem tiledMenuItem = new JRadioButtonMenuItem(dataPanelTiledLayoutAction);
    tiledMenuItem.setSelected(true);
    dataPanelSubMenu.add(tiledMenuItem);
    dataPanelLayoutGroup.add(tiledMenuItem);
   
    final JMenuItem horizMenuItem = new JRadioButtonMenuItem(dataPanelHorizontalLayoutAction);
    dataPanelSubMenu.add(horizMenuItem);
    dataPanelLayoutGroup.add(horizMenuItem);
    
    final JMenuItem vertMenuItem = new JRadioButtonMenuItem(dataPanelVerticalLayoutAction);
    dataPanelSubMenu.add(vertMenuItem);
    dataPanelLayoutGroup.add(vertMenuItem);
    
    dataPanelSubMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent arg0) {}
      public void menuDeselected(MenuEvent arg0) {}
      public void menuSelected(MenuEvent arg0) {
        if (getPanelLayout()==PanelLayout.TILED_LAYOUT) {
          tiledMenuItem.setSelected(true);
        } else if (getPanelLayout()==PanelLayout.HORIZONTAL_LAYOUT){
          horizMenuItem.setSelected(true);
        }else{
          vertMenuItem.setSelected(true);
        }
      }      
    });
    
    menu.add(dataPanelSubMenu);
  }

//  public int getCurrentTab(){
//    return tabbedInterface.getSelectedIndex();
//  }

  public void tabAdded(int tabIndex) {
    DataPanelContainer newCont = new DataPanelContainer(this);

    //containers.add(newCont);

    tabbedInterface.setComponentAt(tabIndex, newCont);
//    if (!dataPanels.containsKey(newCont)) {
//      dataPanels.put(newCont, new LinkedHashSet<DataPanel>());
//    }
  }

  public void tabRemoved(Component c) {
    DataPanelContainer container = (DataPanelContainer) c;

    Iterator<DataPanel> panelIt = container.getDataPanels().iterator();
    while(panelIt.hasNext()){
      DataPanel p = panelIt.next();
      //container.removeDataPanel(p);
      p.closePanel();
      container.removeDataPanel(p);
      closeDataPanel(p);
    }
    
//    if (dataPanels.containsKey(container)) {
//      Set<DataPanel> set = dataPanels.get(container);
//      synchronized (set) {
//        Iterator<DataPanel> iter = set.iterator();
//        while (iter.hasNext()) {
//          DataPanel p = iter.next();
//          p.closePanel();
//          iter.remove();
//        }
//      }
//      dataPanels.remove(container);
//    }

//    if (containers.contains(container)) {
//      containers.remove(container);
//    }
  }

  public void tabRenamed(int tabIndex) {
  }
  
  @Override
  public void setPanelLayout(PanelLayout layout) {
    getDataPanelContainer().setPanelLayout(layout);
  }
  
  @Override
  public PanelLayout getPanelLayout() {
    return getDataPanelContainer().getPanelLayout();
  }
  
  public int getViewId(DataPanel dataPanel){
    Iterator<Entry<Integer,DataPanel>> it=panelIdMap_.entrySet().iterator();
    while(it.hasNext()){
      Entry<Integer,DataPanel> entry=it.next();
      //System.out.println("evaluating "+dataPanel+" against "+entry.getKey()+": "+entry.getValue());
      if(entry.getValue()==dataPanel)
        return entry.getKey();
    }
    log.warn("panel manager getViewId() returning -1");
    return -1;
  }
  
  public Document getConfigurationXML() throws Exception{
    Document ret= DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    int newViewId=0;
    Element tabsEl=ret.createElement("tabs");
    ret.appendChild(tabsEl);
    for (int i = 0; i < tabbedInterface.getTabCount(); i++) {
      DataPanelContainer comp = (DataPanelContainer) tabbedInterface
          .getComponentAt(i);
      
      Element tabEl=ret.createElement("tab");
      tabsEl.appendChild(tabEl);
      
      Element nameEl=ret.createElement("title");
      tabEl.appendChild(nameEl);
      nameEl.setTextContent(tabbedInterface.getTitleAt(i).trim());
      
      Element layoutEl=ret.createElement("layout");
      tabEl.appendChild(layoutEl);
      layoutEl.setTextContent(comp.getPanelLayout().toString());
      
      Document d = comp.getConfigurationXML();
      Node containerNode=ret.importNode(d.getFirstChild(), true);
      tabEl.appendChild(containerNode);
      
//      Element panelsEl=ret.createElement("views");
//      tabEl.appendChild(panelsEl);
//      Iterator<DataPanel> panelIt = comp.getDataPanels().iterator();
//      while(panelIt.hasNext()){ 
//        DataPanel p = panelIt.next();
//        //container.removeDataPanel(p);
//        int panelId=getViewId(p);
//        if(panelId >= 0){
//          Element panelEl=ret.createElement("view");
//          panelsEl.appendChild(panelEl);
//          panelEl.setTextContent(Integer.toString(panelId));
//        }
//      }
    }
    return ret;
  }
  
  public void setConfigurationXML(Document layout) 
                                throws Exception{
    XPath xp = XPathFactory.newInstance().newXPath();
  
    NodeList tabNodes = (NodeList)xp.evaluate("windowLayout/tabs/tab", layout, 
                                        XPathConstants.NODESET);
    for (int j=0; j<tabNodes.getLength(); j++) {
      Node tabNode = tabNodes.item(j);
      
      Node titleNode = (Node)xp.evaluate("title", tabNode, 
                              XPathConstants.NODE);
      String containerName = titleNode.getTextContent();
      DataPanelContainer c = findContainer(containerName);
      if(c==null){
        c = addContainer(containerName); 
      }
      
      Node layoutNode = (Node)xp.evaluate("layout", tabNode, 
                              XPathConstants.NODE);
      c.setPanelLayout(PanelLayout.fromString(layoutNode.getTextContent()));
      
      
      Node viewsNode = (Node)xp.evaluate("views", tabNode, 
                              XPathConstants.NODE);
      Document d = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().newDocument();
      Node importNode = d.importNode(viewsNode, true);
      d.appendChild(importNode);
      
      c.setConfigurationXML(d);
      
//      NodeList panelNodes = (NodeList)xp.evaluate("views/view", tabNode, 
//                              XPathConstants.NODESET);
//      for (int i=0; i<panelNodes.getLength(); i++) {
//        Node panelNode = panelNodes.item(i);
//        int panelId = Integer.parseInt(panelNode.getTextContent());
//        DataPanel panel = getDataPanel(panelId);
//        //addDataPanel(panelId,panel);
//      }
    }
  }

  @Override
  public void updateViewTitle(int viewId) {
    DataPanel panel = panelIdMap_.get(viewId);
    if (panel!=null){
      for (int i = 0; i < tabbedInterface.getTabCount(); i++) {
        DataPanelContainer container = (DataPanelContainer) tabbedInterface
                                                        .getComponentAt(i);
        container.updateTitle(panel);
      }
    }
  }
  
}