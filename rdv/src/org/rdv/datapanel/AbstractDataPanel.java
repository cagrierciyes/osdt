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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/datapanel/AbstractDataPanel.java $
 * $Revision: 1289 $
 * $Date: 2008-11-26 09:45:20 -0500 (Wed, 26 Nov 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.datapanel;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;
import org.rdv.PanelManager;
import org.rdv.TimeScale;
import org.rdv.criteria.AdditionCriteria;
import org.rdv.criteria.AdditionCriteriaFactory;
import org.rdv.criteria.RejectDuplicates;
import org.rdv.data.BasicSeriesFormatter;
import org.rdv.data.Channel;
import org.rdv.data.ChannelSeries;
import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;
import org.rdv.rbnb.DataListener;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBDataRequest;
import org.rdv.rbnb.StateListener;
import org.rdv.rbnb.SubscriptionRequest;
import org.rdv.rbnb.SubscriptionResponse;
import org.rdv.rbnb.TimeListener;
import org.rdv.rbnb.TimeScaleListener;
import org.rdv.ui.ChannelListDataFlavor;
import org.rdv.ui.ChannelSelectDialog;
import org.rdv.ui.PopupMenuBuilder;
import org.rdv.ui.UIUtilities;
import org.rdv.ui.RDVIcon;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;

/**
 * A default implementation of the DataPanel interface. This class manages
 * add and remove channel requests, and handles subscription to the
 * RBNBController for time, data, state, and posting. It also provides a toolbar
 * placed at the top of the UI component to enable the detach and fullscreen
 * features along with a close button.
 * <p>
 * Data panels extending this class must have a no argument constructor and call
 * setDataComponent with their UI component in this constructor. 
 * 
 * @since   1.1
 * @author  Jason P. Hanley
 */
public abstract class AbstractDataPanel implements DataPanel, DataListener, 
      TimeListener, TimeScaleListener, StateListener, DropTargetListener, 
      PopupMenuBuilder
{
  
	/**
	 * The logger for this class.
	 * 
	 * @since  1.1
	 */
	static Log log = LogFactory.getLog(AbstractDataPanel.class.getName());
	
	/**
	 * The data panel manager for callbacks to the main application.
	 * 
	 * @since  1.2
	 */
	protected PanelManager dataPanelManager;

  /**
   * The list of subscribed channels, sorted into series.
   */
  protected VizSeriesList seriesList_=new VizSeriesList();
  
  /**
   * A list of lower threshold values for channels.
   * 
   * @since 1.3
   */
  Hashtable<String, String> lowerThresholds;

  /**
   * A list of upper threshold values for channels.
   * 
   * @since 1.3
   */
  Hashtable<String, String> upperThresholds;
	
	/**
	 * The last posted time.
	 * 
	 * @since  1.1
	 */
	protected double time;
	
	/**
	 * The current time scale in use.
	 * @since  1.1
	 */
	protected double timeScale;

	/**
   * The local time scale to use if not undefined.
   * @since  2.2
   */
	protected double localTimeScale_=TimeScale.TIME_SCALE_UNDEFINED;
  
  /**
   * The last posted state.
   * 
   * @since  1.3
   */
  protected double state;

  /**
   * A description of the data panel.
   * 
   * @since  1.4
   */
  String description_=null;
  
	/**
	 * The subclass UI component.
	 * 
	 * @since  1.1
	 */
	JComponent dataComponent;
		
 	/**
 	 * Indicating if the data panel has been paused by the snaphsot button in the
 	 * toolbar.
 	 * 
 	 * @since  1.1
 	 */
 	//boolean paused;

	/**
   * Indicates whether or not to expect more channel adds.
   */
  boolean addChannels_=false;
	
  /**
   * Properties list for data panel.
   */
  protected Properties properties;

  /**
   * Creates criteria for adding new elements to the channel list
   */
  protected AdditionCriteriaFactory<String> additionCriteria;
  
  
  /**
   * Initialize the list of channels and units. Set parameters to defaults.
   * 
   * @since 1.1
   */
  public AbstractDataPanel() {
    /*
     * We use a copy on write array list so traversals are thread safe. This
     * allows the iterators used for posting of data to be inherently thread
     * safe at the cost of the time taken to add/remove channels.
     */
    //channels = new CopyOnWriteArrayList<String>();

    lowerThresholds = new Hashtable<String, String>();
    upperThresholds = new Hashtable<String, String>();

    time = 0;
    timeScale = TimeScale.getDefaultTimeScale();
    state = RBNBController.STATE_DISCONNECTED;

    //paused = false;

    //showChannelsInTitle = true;
    
    properties = new Properties();
    //this.properties.put("timescale", String.valueOf(timeScale));

    additionCriteria = new RejectDuplicates<String>();
  }

  public void setPanelManager(PanelManager dataPanelManager) {
    this.dataPanelManager = dataPanelManager;
  }

  /**
   * Return the GUI component responsible for the data visualization.
   */
  public JComponent getComponent(){
    return dataComponent;
  }

  /**
   * Clears data panel and adds the specified channel. 
   */
	public boolean setChannel(String channelName) {
		if (seriesList_.containsChannel(channelName)) {
			return false;
		}    
		
		removeAllChannels();
		return addChannel(channelName);
	}
	
	/**
	 * Panels override this method to use data series 
	 * with more than one channel. For example, an XY plot
	 * or XYZ displacement.
	 * @param channelName First channel in series.
	 * @return New data series.
	 */
	protected VisualizationSeries getSeriesForChannel(String channelName){
	  return new ChannelSeries(channelName,new BasicSeriesFormatter());
	}
	
	/**
	 * Perform additional configuration when series are added.
	 * @param vs
	 */
	protected abstract void onSeriesAdded(VisualizationSeries vs);
	
	/**
   * Perform additional configuration when series are removed.
   * @param vs
   */
	protected abstract void onSeriesRemoved(VisualizationSeries vs);
  
  /**
   * For use by subclasses to do any initialization needed when a channel has
   * been added. Should use onSeriesAdded() instead. 
   * @deprecated
   * @param channelName  the name of the channel being added
   */
  protected void channelAdded(String channelName) {}

  /**
   * Override to visualize data.
   */
  public abstract void postData(final SubscriptionResponse r); 

  /**
   * Override to update panel as time passes but no data is received. 
   * Note that panels should in general call 
   * RBNBController.getInstance().getLocation()
   * to get the current time rather than trying 
   * to track this information locally.
   */
  public void postTime(double time) {
    this.time = time;
  }

	/**
	 * Adds a channel to the panel, trying to find an appropriate
	 * series or creating a new series if none is found. Updates
	 * title and subs channel if addChannels_ flag is not set 
	 * indicating more channels are going to be added. 
	 */
	public boolean addChannel(String channelName) {
    if (!addChannelToViewList(channelName)) {
      return false;
    }
    
    VisualizationSeries newSeries = null;
    for(VisualizationSeries vs:seriesList_){
      // add channel to series if the series allows it 
      if(!vs.hasAllChannels() && vs.addChannel(channelName)){
        newSeries=vs;
        break;
      }
    }
    if(newSeries==null){
      newSeries = getSeriesForChannel(channelName);
      seriesList_.add(newSeries);
    }
   
    if(newSeries.hasAllChannels()) onSeriesAdded(newSeries);
    
    channelAdded(channelName);
    // more channels are to be added?
    if(!addChannels_){
      updateTitle();
      subscribe(newSeries);
    }
    return true;
  }

	/**
   * Adds a series to the panel. Updates
   * title and subs channel if addChannels_ flag is not set 
   * indicating more channels are going to be added. 
   */
  public boolean addSeries(VisualizationSeries vs) {
    if(!vs.hasAllChannels()){
      // deny add
      return false;
    }
    
    for(String channel : vs.getChannels()){
      addChannelToViewList(channel);
    }
    
    seriesList_.add(vs);
    onSeriesAdded(vs);
    
    for(String channel : vs.getChannels()){
      channelAdded(channel);
    }
    
    // more channels are to be added?
    if(!addChannels_){
      updateTitle();
      subscribe(vs);
    }
    return true;
  }
  
  private boolean addChannelToViewList(String channelName){
  //see if channel exists
    Channel channel = RBNBController.getInstance().getChannel(channelName);
    if (channel == null) {
      return false;
    }
    
    AdditionCriteria<String> newCriteria = additionCriteria
                                    .createCriteria(seriesList_.getChannels());

    if (!newCriteria.canAdd(channelName)) {
      return false;
    }

    //channels.add(channelName);

    /**
     * these parameters are defined in the NEESit DAQ protocol.
     * 
     * @see org.nees.rbnb.DaqToRbnb see line 495 of
     *      https://svn.nees.org/svn/telepresence/fake_daq/fake_daq.c
     */
    String lowerThreshold = channel.getMetadata("lowerbound");
    String upperThreshold = channel.getMetadata("upperbound");
    // handle errors generated by daq - Unknown command 'list-lowerbounds'
    if (lowerThreshold != null && !lowerThresholds.containsKey(channelName)) {
      try {
        Double.parseDouble(lowerThreshold);
        lowerThresholds.put(channelName, lowerThreshold);
      } catch (java.lang.NumberFormatException nfe) {
        log.warn("Non-numeric lower threshold in metadata: \"" + lowerThreshold
            + "\"");
      }
    } // ! null
    if (upperThreshold != null && !upperThresholds.containsKey(channelName)) {
      try {
        Double.parseDouble(upperThreshold);
        upperThresholds.put(channelName, upperThreshold);
      } catch (java.lang.NumberFormatException nfe) {
        log.warn("Non-numeric upper threshold in metadata: \"" + upperThreshold
            + "\"");
      }
    } // ! null
    //log.debug("&&& LOWER: " + lowerThreshold + " UPPER: " + upperThreshold);
    
    return true;
  }
  
	
  public boolean removeSeries(VisualizationSeries vs){
    seriesList_.remove(vs);
    unsubscribe(vs);
    
    for(String chanName : vs.getChannels()){
      lowerThresholds.remove(chanName);
      upperThresholds.remove(chanName);
    
      channelRemoved(chanName);
    }
    updateTitle();
    onSeriesRemoved(vs);
    return true;
  }
  
  public boolean removeSeries(String seriesName){
    boolean ret=false;
    
    for(VisualizationSeries vs : seriesList_){
      if (vs.getName().equalsIgnoreCase(seriesName)){
        removeSeries(vs);
        ret=true;
      }
   }
    return ret;
  }
  
	public boolean removeChannel(String channelName) {
	  for(VisualizationSeries vs : seriesList_){
	     if (vs.containsChannel(channelName)){
	       removeSeries(vs);
	     }
	  }
	  
//		if (!channels.contains(channelName)) {
//			return false;
//		}
//    unsubscribe(new ChannelSeries(channelName));
    //RBNBController.getInstance().unsubscribe(channelName, this);
    
  	//channels.remove(channelName);
//    lowerThresholds.remove(channelName);
//    upperThresholds.remove(channelName);
//	
//    channelRemoved(channelName);
//    
//    updateTitle();
    return true;
  }

  /**
   * For use by subclasses to do any cleanup needed when a channel has been
   * removed.
   * 
   * @param channelName
   *          the name of the channel being removed
   */
  protected void channelRemoved(String channelName) {
  }

  /**
   * Calls removeChannel for each subscribed channel.
   * 
   * @see removeChannel(String)
   * @since 1.1
   * 
   */
  void removeAllChannels() {
    for(VisualizationSeries vs : seriesList_){
          removeSeries(vs);
     }
    
//    Iterator<String> i = seriesList_.getChannels().iterator();
//    while (i.hasNext()) {
//      removeChannel(i.next());
//    }
    
    // remove all series
    seriesList_.clear();
  }

  /**
   * Gets the UI component used for displaying data.
   * 
   * @return the UI component used to display data
   */
  protected JComponent getDataComponent() {
    return dataComponent;
  }

  /**
   * Return a MouseListener to show the popup menu.
   */
  public MouseListener getPopupMenuMouseListener(){
    return new MouseInputAdapter(){
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          displayPopupMenu(e);
        }
      }
      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          displayPopupMenu(e);
        }
      }
    };
  }
  
  /**
   * Set the UI component to be used for displaying data. This method must be
   * called from the constructor of the subclass.
   * 
   * @param dataComponent
   *          the UI component
   * @since 1.1
   */
  protected void setDataComponent(JComponent dataComponent) {
    this.dataComponent = dataComponent;
    
//    component = new SimpleInternalFrame(createTitleComponent(), createToolBar(),
//        dataComponent);

    //dataPanelContainer.addDataPanel(component);

    initDropTarget();

    RBNBController.getInstance().addTimeListener(this);
    RBNBController.getInstance().addStateListener(this);
    RBNBController.getInstance().addTimeScaleListener(this);   
  }

  
  public VizSeriesList getSeries(){  
    return seriesList_;
    
  }
  

  /**
   * Build a new popup menu dynamically and display it
   * over the source component.
   */
  protected void displayPopupMenu(MouseEvent e) {
    JPopupMenu menu = new JPopupMenu();
    buildPopupMenu(menu, e);
    menu.show(e.getComponent(), e.getX(), 
                                e.getY());
  }
  
  /**
   * Add menu items common to all panels.
   */
  public void buildPopupMenu(JPopupMenu menu, MouseEvent e){
    if (seriesList_.size() > 0) {
      
      JMenuItem unsubMenuItem = new JMenuItem("Unsubscribe series...");
      unsubMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          
          String seriesArr[]=seriesList_.getDisplayNames();
           
          String seriesNameArr[] = ChannelSelectDialog.showDialog(UIUtilities.getMainFrame(),
              null,
              "Select series to unsubscribe:",
              "Unsubscribe Series",
              seriesArr,
              seriesArr[0]);
  
          if(seriesNameArr!=null && seriesNameArr.length>0){
            for(int i=0;i<seriesNameArr.length;i++){
              String seriesName=seriesNameArr[i];
              for(int j=0;j<seriesArr.length;j++){
                if(seriesArr[j].compareToIgnoreCase(seriesName)==0){
                  VisualizationSeries vs=seriesList_.get(j);
                  AbstractDataPanel.this.removeSeries(vs.getName());
                  break;
                }
              }
            }
          }
        }
      });
      
      menu.insert(unsubMenuItem, 0);
      menu.insert(new JSeparator(),1);
    }    
  }
  
  
  public String getDescription(){
    return description_;
  }
 
  
  /**
   * Update the title of the data panel. This includes the text displayed in the
   * header panel and the text displayed in the frame if the data panel is
   * detached.
   */
  protected void updateTitle() {
    
    if (dataPanelManager!=null){
      
      int viewId = dataPanelManager.getViewId(this);
      if(viewId >=0){
        
        dataPanelManager.updateViewTitle(viewId);
      }else{
        log.warn("Not updating title for "+getClass().toString()+" (id "+viewId+")");
      }
      
    }else{
      log.debug("No panel manager for "+getClass().toString());
    }
    //if(titleHidden_)return;
    
  }
  
//  protected boolean isShowChannelsInTitle() {
//    return showChannelsInTitle;
//  }
//  
//  void setShowChannelsInTitle(boolean showChannelsInTitle) {
//    if (this.showChannelsInTitle != showChannelsInTitle) {
//      this.showChannelsInTitle = showChannelsInTitle;
//      properties.setProperty("showChannelsInTitle", Boolean.toString(showChannelsInTitle));
//      updateTitle();
//    }
//  }
  
  public void setDescription(String description) {
    if (description_ != description) {
      description_ = description;
      if (description != null) {
        properties.setProperty("description", description);
      } else {
        properties.remove("description");
      }
      updateTitle();
    }
  }

  /**
   * update timescale if no local scale is set.
   */
  public void globalTimeScaleChanged(double timeScale) {
    if(localTimeScale_==TimeScale.TIME_SCALE_UNDEFINED){
      myTimeScaleChanged(timeScale);
    }
  }

  protected void myTimeScaleChanged(double timeScale){
    this.timeScale = timeScale;
    subscribeAll();
    updateTitle();
  }
  
  /**
   * Set local time scale.
   */
  protected void setLocalTimescale(double timeScale) {
    localTimeScale_=timeScale;
    double newScale=timeScale;
    if(localTimeScale_==TimeScale.TIME_SCALE_UNDEFINED){
      // grab global scale
      newScale=RBNBController.getInstance().getTimeScale();
      this.properties.remove("timescale");
    }else{
      if(!TimeScale.hasGlobalTimeScale(timeScale)){
        TimeScale.addGlobalTimeScale(timeScale);
      }
      this.properties.put("timescale", String.valueOf(localTimeScale_));
    }
    
    myTimeScaleChanged(newScale);
  }
  
  public boolean localTimescaleUndefined(){
    return (localTimeScale_==TimeScale.TIME_SCALE_UNDEFINED);
  }
  
  public double getLocalTimescale(){
    return localTimeScale_;
  }
  
  public void postState(int newState, int oldState) {
    state = newState;
	}
	
//	/**
//	 * Toggle pausing of the data panel. Pausing is freezing the data display and
//	 * stopping listeners for data. When a channel is unpaused it will again
//	 * subscribe to data for the subscribed channels.
//	 *
//	 * @since  1.1
//	 */
//	void togglePause() {
////		Iterator<String> i = channels.iterator();
////		while (i.hasNext()) {
////			String channelName = i.next();
////			
//			if (paused) {
//				//RBNBController.getInstance().subscribe(channelName, this);
//			  subscribeAll();
//			} else {
//			  unsubscribeAll();
//				//RBNBController.getInstance().unsubscribe(channelName, this);
//			}
////	}
////		
//		paused = !paused;
//	}
	
  public void startAddingChannels(){
    addChannels_=true;
  }
  
  public void stopAddingChannels(){
    addChannels_=false;
    updateTitle();
    subscribeAll();
  }
  
	protected SubscriptionRequest getSubscriptionRequest(){
    return new RBNBDataRequest(this,
        SubscriptionRequest.TYPE_CHART,timeScale,seriesList_);
  }
	
	protected SubscriptionRequest getSubscriptionRequest(
	                              VisualizationSeries series){
	  VizSeriesList vsl=new VizSeriesList(series);
	  
    return new RBNBDataRequest(this,
        SubscriptionRequest.TYPE_CHART,timeScale,vsl);
  }
	
	protected void subscribe(VisualizationSeries series){
//    SubscriptionRequest req=getSubscriptionRequest();
//    req.addChannel(name);
//    RBNBController.getInstance().subscribe(req);
    RBNBController.getInstance().subscribe(
        getSubscriptionRequest());
  }
  
  protected void subscribeAll(){
//    if(channels.size()>0){
//      SubscriptionRequest req=getSubscriptionRequest();
//      
//      for(String channel:channels){
//        req.addChannel(channel);
//      }
//      
//      RBNBController.getInstance().subscribe(req);
//    }
    RBNBController.getInstance().subscribe(
        getSubscriptionRequest());
  }
  
  public void unsubscribe(VisualizationSeries series){
//    List<String>l=new ArrayList<String>();
//    l.add(name);
    SubscriptionRequest req=getSubscriptionRequest(series);//RBNBDataRequest.createUnsubscribeRequest(this, l);
    req.setUnsubscribe();
    RBNBController.getInstance().unsubscribe(req);
  }
  
  public void unsubscribeAll(){
    //RBNBDataRequest req=RBNBDataRequest.createUnsubscribeRequest(this,channels);
    SubscriptionRequest req=getSubscriptionRequest();//RBNBDataRequest.createUnsubscribeRequest(this, l);
    req.setUnsubscribe();
//    
//    for(String s:channels){
//        req.addChannel(s);
//    }
    RBNBController.getInstance().unsubscribe(req);
  }
	
	public void closePanel() {
		removeAllChannels();

// 		if (maximized) {
// 			restorePanel(false);
//		} else if (!attached) {
//			attachPanel(false);		 			
// 		} 
// 		  
		//dataPanelManager.closeDataPanel(this);
 		  
		//dataPanelContainer.removeDataPanel(component);
 		
 		RBNBController.getInstance().removeStateListener(this);
 		RBNBController.getInstance().removeTimeListener(this);
 		RBNBController.getInstance().removeTimeScaleListener(this);
	}
  
  public int subscribedChannelCount() {
    return seriesList_.getChannels().size();
  }
  
  public List<String> subscribedChannels() {
    return seriesList_.getChannels();
  }
  
  public boolean isChannelSubscribed(String channelName) {
    return seriesList_.containsChannel(channelName);
  }

  public Properties getProperties() {
    return properties;
  }
  
  public void setProperty(String key, String value) {
//    if (key.equals("showChannelsInTitle")) {
//      setShowChannelsInTitle(Boolean.parseBoolean(value));
//    } else 
    if (key.equals("description")) {
      setDescription(value);
    } //else if (key.equals("attached") && Boolean.parseBoolean(value) == false) {
//      detachPanel();
//    } else if (key.equals("bounds")) {
//      loadBounds(value);
//    } else 
    else if (key.equals("timescale")) {
      //timeScaleChanged(Double.parseDouble(value));
      setLocalTimescale(Double.parseDouble(value));
    }
  }
		
	/**
	 * Setup the drop target for channel subscription via drag-and-drop.
	 *
	 * @since  1.2
	 */
	void initDropTarget() {
		new DropTarget(dataComponent, DnDConstants.ACTION_LINK, this);
	}

	public void dragEnter(DropTargetDragEvent e) {}
	
	public void dragOver(DropTargetDragEvent e) {}
	
	public void dropActionChanged(DropTargetDragEvent e) {}
	
	@SuppressWarnings("unchecked")
  public void drop(DropTargetDropEvent e) {
    try {
      int dropAction = e.getDropAction();
      if (dropAction == DnDConstants.ACTION_LINK) {
        DataFlavor channelListDataFlavor = new ChannelListDataFlavor();
        Transferable tr = e.getTransferable();
        if (e.isDataFlavorSupported(channelListDataFlavor)) {
          e.acceptDrop(DnDConstants.ACTION_LINK);
          e.dropComplete(true);

  				final List<String> channels = (List)tr.getTransferData(channelListDataFlavor);
          
          new Thread() {
            public void run() {
              startAddingChannels();
              for (int i=0; i<channels.size(); i++) {
                String channel = channels.get(i);
                boolean status;
                if (supportsMultipleChannels()) {
                  status = addChannel(channel);
                } else {
                  status = setChannel(channel);
                }

                if (!status) {
                  // TODO display an error in the UI
                }
              }
              stopAddingChannels();
            }
          }.start();
  			} else {
  				e.rejectDrop();
  			}
      }
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
		}	
	}
	
	public void dragExit(DropTargetEvent e) {}
  
  public class ChannelTitle extends JPanel {

    /** serialization version identifier */
    private static final long serialVersionUID = -7191565876111378704L;

    public ChannelTitle(String channelName) {
      this(channelName, channelName);
    }
    
    public ChannelTitle(String seriesName, String channelName) {
      setLayout(new BorderLayout());
      setBorder(new EmptyBorder(0, 0, 0, 5));
      setOpaque(false);
      
      JLabel text = new JLabel(seriesName);
      text.setForeground(SimpleInternalFrame.getTextForeground(true));
      add(text, BorderLayout.CENTER);
      
      JButton closeButton = new JButton(RDVIcon.closeChannel);
      closeButton.setToolTipText("Remove channel");
      closeButton.setBorder(null);
      closeButton.setOpaque(false);
      closeButton.addActionListener(getActionListener(seriesName, channelName));
      add(closeButton, BorderLayout.EAST);
    }
    
    protected ActionListener getActionListener(final String seriesName, final String channelName) {
      return new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          removeChannel(channelName);
        }
      };
    }
  }
  
  /**
   * A class to transfer image data.
   * 
   * @since  1.3
   */
  public class ImageSelection implements Transferable {
    /**
     * The image to transfer.
     */
    Image image;
    
    /**
     * Creates this class to transfer the <code>image</code>.
     * 
     * @param image the image to transfer
     */
    public ImageSelection(Image image) {
      this.image = image;
    }

    /**
     * Returns the transfer data flavors support. This returns an array with one
     * element representing the {@link DataFlavor#imageFlavor}.
     * 
     * @return an array of transfer data flavors supported
     */
    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] {DataFlavor.imageFlavor};
    }

    /**
     * Returns true if the specified data flavor is supported. The only data
     * flavor supported by this class is the {@link DataFlavor#imageFlavor}.
     * 
     * @return true if the transfer data flavor is supported
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return DataFlavor.imageFlavor.equals(flavor);      
    }

    /**
     * Returns the {@link Image} object to be transfered if the flavor is
     * {@link DataFlavor#imageFlavor}. Otherwise it throws an
     * {@link UnsupportedFlavorException}.
     * 
     * @return the image object transfered by this class
     * @throws UnsupportedFlavorException
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
      if (!DataFlavor.imageFlavor.equals(flavor)) 
      {
        throw new UnsupportedFlavorException(flavor);
      }

      return image;
    }
  }  
}