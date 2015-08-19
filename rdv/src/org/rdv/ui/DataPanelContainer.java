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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/ui/DataPanelContainer.java $
 * $Revision: 1149 $
 * $Date: 2008-07-03 10:23:04 -0400 (Thu, 03 Jul 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataPanelManager;
import org.rdv.DataViewer;
import org.rdv.PanelLayout;
import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;
import org.rdv.datapanel.DataPanel;
import org.rdv.datapanel.DescriptionTitleFormatter;
import org.rdv.datapanel.SeriesFixedWidthTitleFormatter;
import org.rdv.ui.RDVIcon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jgoodies.uif_lite.panel.SimpleInternalFrame;

/**
 * A container to hold the UI components for the data panels. They may add and
 * remove UI components as needed.
 * 
 * @author  Jason P. Hanley
 * @since   1.1
 */
public class DataPanelContainer extends JPanel 
          implements DragGestureListener, DragSourceListener 
{

  /** serialization version identifier */
  private static final long serialVersionUID = -2496258563984574021L;
  
	/**
	 * The logger for this class.
	 * 
	 * @since  1.1
	 */
	static Log log = LogFactory.getLog(DataPanelContainer.class.getName());
	
	/**
	 * A list of docked data panels.
	 * 
	 * @since  1.1
	 */
	List<DecoratedDataPanel> dataPanels;
	
	/**
	 * The current layout.
	 * 
	 * @since  1.1
	 */
	private PanelLayout layout;
  
  /**
   * The layout manager.
   * 
   * @since  1.3
   */
  private GridLayout gridLayout;
  
  /**
   * The drag gesture recognizers for the components.
   * 
   * @since  1.3
   */
  private HashMap<Component,DragGestureRecognizer> dragGestures;
  
  private DataPanelManager panelManager_;
  /**
   * The position of components that were in this container.
   */
  //private HashMap<DataPanel, Integer> previousPositions;
	
	/** 
	 * Create the container and set the default layout to horizontal.
	 * 
	 * @since  1.1
	 */
	public DataPanelContainer(DataPanelManager panelManager) {
    super();
    panelManager_=panelManager;
    
    setBorder(null);
    
    gridLayout = new GridLayout(1, 1, 8, 8);
    setLayout(gridLayout);
    
    //removed because the University of Buffalo no longer maintains the Real-time Data Viewer
    //initLogo();
    
    dataPanels = new CopyOnWriteArrayList<DecoratedDataPanel>();
    
    dragGestures =  new HashMap<Component,DragGestureRecognizer>();
		
		layout = PanelLayout.TILED_LAYOUT;
    
    //previousPositions = new HashMap<DataPanel, Integer>();
	}
 
  /**
   * Add the NEESit and UB logo as the initial background.
   */
  private void initLogo() {
    JLabel backgroundImage = new JLabel(RDVIcon.neesITandUB);
    backgroundImage.setPreferredSize(new Dimension(1,1));
    backgroundImage.setMinimumSize(new Dimension(1,1));
    add(backgroundImage);    
  }
	
//  public void setAttached(DataPanel dataPanel, boolean attached) {
//    for(DecoratedDataPanel panel: dataPanels){
//      if(panel.equals(dataPanel)){
//        panelStatusMap_.get(dataPanel).detached_=!attached;
//      }
//    }
//    layoutDataPanels();
//  }
//  
//  public void setMaximized(DataPanel dataPanel, boolean maximized) {
//    for(DataPanel panel: dataPanels){
//      if(panel.equals(dataPanel)){
//        panelStatusMap_.get(dataPanel).maximized_=maximized;
//      }
//    }
//    layoutDataPanels();
//  }
  
  public List<DataPanel> getDataPanels(){
    List<DataPanel> retList=new ArrayList<DataPanel>();
    for(DecoratedDataPanel d : dataPanels){
      retList.add(d.panel_);
    }
    return retList;
  }
  
  
  public boolean contains(DataPanel dataPanel){
    for(DecoratedDataPanel d : dataPanels){
      if(d.panel_==dataPanel){
        return true;
      }
    }
    return false;
  }
  
  public void updateTitle(DataPanel dataPanel){
    DecoratedDataPanel decPanel = getDecoratedPanel(dataPanel);
    if(decPanel != null){
      decPanel.updateTitle();
    }
  }
  
  private DecoratedDataPanel getDecoratedPanel(DataPanel dataPanel){
    for(DecoratedDataPanel d : dataPanels){
      if(d.panel_==dataPanel){
        return d;
      }
    }
    return null;
  }
  
  
	/**
	 * Add a data panel UI component to this container.
	 * 
	 * @param component  the UI component to add
	 * @since            1.1
	 */
	public void addDataPanel(DataPanel dataPanel) {
	  if(!contains(dataPanel)){
	    DecoratedDataPanel decoratedPanel = new DecoratedDataPanel(dataPanel);
      addDecoratedPanel(decoratedPanel);
  		layoutDataPanels();
  		
  		log.info("Added data panel to container (total=" + dataPanels.size() + ").");
	  }
	}
	
	private void addDecoratedPanel(DecoratedDataPanel decoratedPanel){
	  
	  dataPanels.add(decoratedPanel);
    //panelStatusMap_.put(dataPanel, new PanelStatus());
    
    DragSource dragSource = DragSource.getDefaultDragSource();
    DragGestureRecognizer dragGesture = 
                              dragSource.createDefaultDragGestureRecognizer(
                                  decoratedPanel.getTitleComponent(), DnDConstants.ACTION_MOVE, this);
    dragGestures.put(decoratedPanel.getTitleComponent(), dragGesture);
	}
	
	/**
	 * Remove the data panel UI component from this container.
	 * 
	 * @param component  the UI component to remove.
	 * @since            1.1
	 */
	public void removeDataPanel(DataPanel dataPanel) {
	    DecoratedDataPanel decPanel = getDecoratedPanel(dataPanel);
	    if (decPanel==null) return;
	    
	    if (decPanel.maximized_) {
	      decPanel.restorePanel(false);
	    } else if (!decPanel.attached_) {
	      decPanel.attachPanel(false);         
	    } 
	    
  	  DragGestureRecognizer dragGesture = (DragGestureRecognizer)
                                        dragGestures.remove(decPanel.getTitleComponent());
      dragGesture.setComponent(null);
  
      //panelStatusMap_.remove(dataPanel);
  		dataPanels.remove(decPanel);
  		layoutDataPanels();
  		
  		log.debug("Removed data panel container (total=" + dataPanels.size() + ").");
	}
	
	/**
	 * Set the layout for the data panels.
	 * 
	 * @param layout  the layout to use
	 * @since         1.1
	 */
	public void setPanelLayout(PanelLayout layout) {
    //if (this.layout != layout) {
      this.layout = layout;
      layoutDataPanels();
    //}
	}
	
	public PanelLayout getPanelLayout() {
      return this.layout;
  }
	
	/**
	 * Layout the data panel according the layout setting and in the order in which
	 * they were added to the container.
	 * 
	 * @since  1.1
	 */
	private void layoutDataPanels() {
	  javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
      		int numberOfDataPanels = dataPanels.size();
      		if (numberOfDataPanels > 0) {
      			if (layout == PanelLayout.TILED_LAYOUT) {
      			  int gridDimension = (int)Math.ceil(Math.sqrt(numberOfDataPanels));
      	      int rows = gridDimension;
      	      
      	      int columns;
      	      if (numberOfDataPanels > Math.pow(gridDimension, 2)*(gridDimension-1)/gridDimension) {
      	        columns = gridDimension;
      	      } else {
      	        columns = gridDimension-1;
      	      }
              gridLayout.setRows(columns);
              gridLayout.setColumns(rows);
      			} else if (layout == PanelLayout.HORIZONTAL_LAYOUT){
      			  gridLayout.setRows(numberOfDataPanels);
      			  gridLayout.setColumns(1);
      			} else {
      			  gridLayout.setColumns(numberOfDataPanels);
              gridLayout.setRows(1);
              
              //gridLayout.setRows(rows);
              //gridLayout.setColumns(columns);
            }
          }
         
          removeAll();
      		//JComponent component;
      		for(DecoratedDataPanel panel: dataPanels){
      //		  PanelStatus ps=panelStatusMap_.get(panel);
      		  if(!panel.maximized_ && panel.attached_){
      		    add(panel.getComponent());
      		  }
      		}
      		
      		validate();
      		repaint();
      }
  });
	}
  
  private void moveBefore(DecoratedDataPanel movePanel, 
                          DecoratedDataPanel beforePanel) {
    if (beforePanel != null && movePanel != null) {
      int beforeIndex = getPanelIndex(beforePanel);
      int moveIndex = getPanelIndex(movePanel);
      //System.out.println("Dragging panel "+moveIndex+" to "+beforeIndex);
      for(int i=moveIndex;i!=beforeIndex;){
        int nextIndex;
        if(i<beforeIndex)
          nextIndex=i+1;
        else
          nextIndex=i-1;
        
        Collections.swap(dataPanels, i, nextIndex);
        i=nextIndex;
      } 
      layoutDataPanels();
    }
  }
  
  private int getPanelIndex(DecoratedDataPanel dataPanel) {
    for (int i=0; i<dataPanels.size(); i++) {
      if (dataPanel.equals(dataPanels.get(i))) {
        return i;
      }
    }    
    return -1;
  }

  private DecoratedDataPanel getPanelForComponent(Component component) {
    for (int i=0; i<dataPanels.size(); i++) {
      if (dataPanels.get(i).getComponent()==component) {
        return dataPanels.get(i);
      }else if(dataPanels.get(i).getTitleComponent()==component) {
        return dataPanels.get(i);
      }
    }    
    return null;
  }
  
  public Document getConfigurationXML() throws Exception{
    Document ret= DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element viewsEl=ret.createElement("views");
    ret.appendChild(viewsEl);
    
    for (DecoratedDataPanel panel: dataPanels){
      Element viewEl=ret.createElement("view");
      viewsEl.appendChild(viewEl);
      
      Element idEl=ret.createElement("id");
      viewEl.appendChild(idEl);
      int viewId=panelManager_.getViewId(panel.panel_);
      idEl.setTextContent(Integer.toString(viewId));
      
      Element attachEl=ret.createElement("attach");
      viewEl.appendChild(attachEl);
      attachEl.setTextContent(Boolean.toString(panel.attached_));
      
      Element maxEl=ret.createElement("maximize");
      viewEl.appendChild(maxEl);
      maxEl.setTextContent(Boolean.toString(panel.maximized_));
      
      if(panel.panel_.getDescription()!=null){
        Element descEl=ret.createElement("description");
        viewEl.appendChild(descEl);
        descEl.setTextContent(panel.panel_.getDescription());
      }
      
      Element showChanEl=ret.createElement("showChannelsInTitle");
      viewEl.appendChild(showChanEl);
      showChanEl.setTextContent(Boolean.toString(panel.showChannelsInTitle));
      
      if(panel.bounds_!=null){
        Element boundEl=ret.createElement("frameBounds");
        viewEl.appendChild(boundEl);
        boundEl.setTextContent(panel.bounds_.x+","+panel.bounds_.y+","+
                                panel.bounds_.height+","+panel.bounds_.width);
      }
    }
    
    return ret;
  }
  
  public void setConfigurationXML(Document d) throws Exception{
    XPath xp = XPathFactory.newInstance().newXPath();
    
    NodeList viewNodes = (NodeList)xp.evaluate("views/view", d, 
                                        XPathConstants.NODESET);
    for (int j=0; j<viewNodes.getLength(); j++) {
      Node viewNode = viewNodes.item(j);
      
      Node idNode = (Node)xp.evaluate("id", viewNode, 
                                  XPathConstants.NODE);
      int viewId = Integer.parseInt(idNode.getTextContent());
      DataPanel panel = panelManager_.getDataPanel(viewId);
      DecoratedDataPanel decPanel=new DecoratedDataPanel(panel);
      
      Node boundNode = (Node)xp.evaluate("frameBounds", viewNode, 
          XPathConstants.NODE);
      if(boundNode!=null){
        String rectDim = boundNode.getTextContent();
        String dimArr[] = rectDim.split(",");
        Rectangle newBounds = new Rectangle();
        newBounds.x=Integer.parseInt(dimArr[0]);
        newBounds.y=Integer.parseInt(dimArr[1]);
        newBounds.height=Integer.parseInt(dimArr[2]);
        newBounds.width=Integer.parseInt(dimArr[3]);
        //System.out.println(newBounds.x+","+newBounds.y+","+newBounds.height+","+newBounds.width);
        decPanel.bounds_=newBounds;
      }

      Node descNode = (Node)xp.evaluate("description", viewNode, 
          XPathConstants.NODE);
      if(descNode!=null){
        decPanel.panel_.setDescription(descNode.getTextContent());
      }
      
      Node showChanNode = (Node)xp.evaluate("showChannelsInTitle", viewNode, 
                                          XPathConstants.NODE);
      decPanel.showChannelsInTitle=Boolean.parseBoolean(showChanNode.getTextContent());
      
      Node attachNode = (Node)xp.evaluate("attach", viewNode, 
                                    XPathConstants.NODE);
      decPanel.attached_=Boolean.parseBoolean(attachNode.getTextContent());
      
      Node maxNode = (Node)xp.evaluate("maximize", viewNode, 
                                        XPathConstants.NODE);
      decPanel.maximized_=Boolean.parseBoolean(maxNode.getTextContent());      
      
      decPanel.updateTitle();
      if(decPanel.maximized_){
        decPanel.maximizePanel();
      }else if (!decPanel.attached_){
        decPanel.detachPanel();
      }
      // prevent re-adding panel
      panelManager_.deleteDataPanelFromContainers(decPanel.panel_);
      addDecoratedPanel(decPanel);
    }
    
    layoutDataPanels();
  }
  
  public void dragGestureRecognized(DragGestureEvent e) {
    e.startDrag(DragSource.DefaultMoveDrop, new StringSelection(""), this);    
  }

  public void dragEnter(DragSourceDragEvent e) {}

  public void dragOver(DragSourceDragEvent e) {
    
  }

  public void dropActionChanged(DragSourceDragEvent dsde) {}

  public void dragExit(DragSourceEvent dse) {}

  public void dragDropEnd(DragSourceDropEvent dsde) {
    Point dragPoint = dsde.getLocation();
    Point containerLocation = getLocationOnScreen();
    dragPoint.translate(-containerLocation.x, -containerLocation.y);

    Component overComponent = getComponentAt(dragPoint);
    Component dragComponent = dsde.getDragSourceContext().getComponent();
    DecoratedDataPanel overPanel = getPanelForComponent(overComponent);
    DecoratedDataPanel dragPanel = getPanelForComponent(dragComponent);
    
    if (overPanel != null && overPanel != dragPanel) {
      //System.out.println("dragging "+dragPanel+" to "+overPanel);
      moveBefore(dragPanel, overPanel);
    }
    
//    Point dragPoint = dsde.getLocation();
//    Point containerLocation = getLocationOnScreen();
//    dragPoint.translate(-containerLocation.x, -containerLocation.y);
//
//    Component overComponent = getComponentAt(dragPoint);
//    Component dragComponent = dsde.getDragSourceContext().getComponent();
//    if (overComponent != null && overComponent != dragComponent) {
//        moveBefore(dragComponent, overComponent);
//    }
    
  }
  
//  private class PanelStatus
//  {
//    boolean maximized_=false;
//    boolean detached_=false;
//    
//  }
  
  private class DecoratedDataPanel{
    private DataPanel panel_;
    
    /**
     * The top-level UI component with toolbar.
     * 
     * @since  1.1
     */
    JComponent component_;
    
    JPanel titlePanel_;
    
    /**
     * The attach/detach button.
     * 
     * @since 1.3
     */
    ToolBarButton achButton;
    
    /**
     * The frame used when the UI component is detached or full screen.
     * 
     * @since  1.1
     */
    JFrame frame;
    
    /**
     * Indicating if the UI component is docked.
     * 
     * @since  1.1
     */
    boolean attached_=true;
    
    /**
     * Indicating if the UI component is in fullscreen mode.
     * 
     * @since  1.1
     */
    boolean maximized_=false;
    
    /**
     * Indicating if the data panel has been paused by the snaphsot button in the
     * toolbar.
     * 
     * @since  1.1
     */
    boolean paused=false;
    
    
    /**
     * Whether to show the channel names in the title;
     */
    boolean showChannelsInTitle=true;
    
    /**
     * bounding rectangle for detached frame.
     */
    Rectangle bounds_=null;
    
    
    public DecoratedDataPanel(DataPanel panel){
      panel_=panel;
      frame = new JFrame(new SeriesFixedWidthTitleFormatter().getTitle(panel_));
      component_ = new SimpleInternalFrame(createTitleComponent(), createToolBar(),
          panel_.getComponent());
    }
    
    JComponent getTitleComponent(){
      if(titlePanel_==null)
        createTitleComponent();
      
      return titlePanel_;
    }
    
    JComponent getComponent(){
      return component_;
    }
    
    protected JComponent getChannelComponent() {
      VizSeriesList channelSeries=panel_.getSeries();//subscribedChannels();
      if (channelSeries.size() == 0) {
        return null;
      }
      JPanel channelBar = new JPanel();
      channelBar.setOpaque(false);
      channelBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      
      Iterator<VisualizationSeries> i = channelSeries.iterator();
      while (i.hasNext()) {
        VisualizationSeries series = i.next();
        
        if (showChannelsInTitle) {
          channelBar.add(new ChannelTitle(series,panel_));
        }      
      }
      
      return channelBar;
    }
    
    /**
     * Get a component for displaying the title in top bar. This implementation
     * includes a button to remove a specific channel.
     * 
     * Subclasses should overide this method if they don't want the default
     * implementation.
     * 
     * @return  A component for the top bar
     * @since   1.3
     */
    JComponent createTitleComponent() {
      JPanel titleBar;
      if(titlePanel_==null){
        titleBar = new JPanel();
        titlePanel_=titleBar;
      }else{
        titleBar=titlePanel_;
        titleBar.removeAll();
      }
      titleBar.setOpaque(false);
      titleBar.setLayout(new BorderLayout());

      JPopupMenu popupMenu = new ScrollablePopupMenu();

      final String title = "Edit description";
//      if (panel_.getDescription() != null) {
//        title = "Edit description";
//      } else {
//        title = "Add description";
//      }    
      
      JMenuItem addDescriptionMenuItem = new JMenuItem(title);
      addDescriptionMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          String response = (String)JOptionPane.showInputDialog(null, "Enter a description",
              title, JOptionPane.QUESTION_MESSAGE, null, null, panel_.getDescription());
          if (response == null) {
            return;
          } else if (response.length() == 0) {
            panel_.setDescription(null);
          } else {
            panel_.setDescription(response);
          }
        }
      });
      popupMenu.add(addDescriptionMenuItem);
      
      if (panel_.getDescription() != null) {
        JMenuItem removeDescriptionMenuItem = new JMenuItem("Remove description");
        removeDescriptionMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            setDescription(null);
          }
        });
        popupMenu.add(removeDescriptionMenuItem);
      }
      
      popupMenu.addSeparator();
      
      final JCheckBoxMenuItem showChannelsInTitleMenuItem = new JCheckBoxMenuItem("Show channels in title", showChannelsInTitle);
      showChannelsInTitleMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          setShowChannelsInTitle(showChannelsInTitleMenuItem.isSelected());
        }
      });
      popupMenu.add(showChannelsInTitleMenuItem);    
      
      VizSeriesList channels=panel_.getSeries();//subscribedChannels();
      if (channels.size() > 0) {
        popupMenu.addSeparator();
        
        Iterator<VisualizationSeries> i = channels.iterator();
        while (i.hasNext()) {
          final VisualizationSeries channelSeries = i.next();
          
          JMenuItem unsubscribeChannelMenuItem = new JMenuItem("Unsubscribe from " + 
                                                        channelSeries.getDisplayName());
          unsubscribeChannelMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              panel_.removeSeries(channelSeries.getName());
            }
          });
          popupMenu.add(unsubscribeChannelMenuItem);      
        }
      }

      // set component popup and mouselistener to trigger it
      titleBar.setComponentPopupMenu(popupMenu);
      titleBar.addMouseListener(new MouseInputAdapter() {});

      if (panel_.getDescription() != null || !panel_.localTimescaleUndefined()) {  
        titleBar.add(getDescriptionComponent(), BorderLayout.WEST);
        
      }
      
      JComponent titleComponent = getChannelComponent();
      if (titleComponent != null) {
        titleBar.add(titleComponent, BorderLayout.CENTER);
      }
      
      return titleBar;
    }
    
    JComponent getDescriptionComponent() {
      String label=new DescriptionTitleFormatter().getTitle(panel_);
      
//      if (panel_.getDescription() != null){
//        if(!panel_.localTimescaleUndefined()){
//          label = panel_.getDescription() + " ("+DataViewer.formatSeconds(panel_.getLocalTimescale())+")";
//        }else{
//          label = panel_.getDescription();
//        }
//      }else{
//        if(!panel_.localTimescaleUndefined()){
//          label = DataViewer.formatSeconds(panel_.getLocalTimescale());
//        }
//      }
      if(!label.isEmpty() && showChannelsInTitle){
        label = label +":";
      }
      
      JLabel descriptionLabel = new JLabel(label);
      descriptionLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
      descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.BOLD));
      descriptionLabel.setForeground(Color.white);
      return descriptionLabel;
    }
    
    JToolBar createToolBar() {
      JToolBar toolBar = new JToolBar();
      toolBar.setFloatable(false);
      
      //final DataPanel dataPanel = this;
      
      achButton = new ToolBarButton(RDVIcon.detach, "Detach data panel");
      achButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          toggleDetach();
        }
      });
      toolBar.add(achButton);
      JButton button = new ToolBarButton(RDVIcon.close, "Close data panel");
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          panel_.closePanel();
          removeDataPanel(panel_);
        }
      });    
      toolBar.add(button);    

      return toolBar;
    }
    
    void setShowChannelsInTitle(boolean showChannelsInTitle) {
      if (this.showChannelsInTitle != showChannelsInTitle) {
        this.showChannelsInTitle = showChannelsInTitle;
        //properties.setProperty("showChannelsInTitle", Boolean.toString(showChannelsInTitle));
        updateTitle();
      }
    }
    
    void setDescription(String description) {
      if (panel_.getDescription() != description) {
        panel_.setDescription(description);
//        if (description != null) {
//          properties.setProperty("description", description);
//        } else {
//          properties.remove("description");
//        }
        updateTitle();
      }
    }
    
    void updateTitle(){
      if(component_ instanceof SimpleInternalFrame){
        ((SimpleInternalFrame)component_).setTitle(createTitleComponent());
      }
      
      if (!attached_) {
        frame.setTitle(new SeriesFixedWidthTitleFormatter().getTitle(panel_));
      }
    }
    /**
     * Toggle maximizing the data panel UI component to fullscreen.
     * 
     * @since  1.1
     */
    void toggleMaximize() { 
      if (maximized_) {
        restorePanel(attached_);
        if (!attached_) {
          detachPanel();
        }
      } else {
        if (!attached_) {
          attachPanel(false);
        }
        maximizePanel();
      }
    }
    
    /**
     * Toggle detaching the UI component from the data panel container.
     * 
     * @since  1.1
     */
    void toggleDetach() {
      if (maximized_) {
        restorePanel(false);
      }
      
      if (attached_) {
        detachPanel();
      } else {
        attachPanel(true);
      }
    }
    
    /**
     * Detach the UI component from the data panel container.
     * 
     * @since  1.1
     */
    void detachPanel() {
      attached_ = false;
      //properties.setProperty("attached", "false");
      //setAttached(panel_);
      //dataPanelContainer.removeDataPanel(component);

      achButton.setIcon(RDVIcon.attach);

      frame = new JFrame(new SeriesFixedWidthTitleFormatter().getTitle(panel_));
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          panel_.closePanel();
        }
      });
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      frame.getContentPane().add(component_);

      
      //String bounds = properties.getProperty("bounds");
      if (bounds_ != null) {
        loadBounds();
      } else {
        frame.pack();
      }

      frame.addComponentListener(new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
          storeBounds();
        }

        public void componentMoved(ComponentEvent e) {
          storeBounds();
        }

        public void componentShown(ComponentEvent e) {
          storeBounds();
        }
      });

      frame.setVisible(true);
      layoutDataPanels();
    }
    
    void loadBounds() {
      if (bounds_ != null) {
//        properties.setProperty("bounds", bounds);

        if (frame == null) {
          return;
        }
//        String[] boundsElements = bounds.split(",");
//        int x = Integer.parseInt(boundsElements[0]);
//        int y = Integer.parseInt(boundsElements[1]);
//        int width = Integer.parseInt(boundsElements[2]);
//        int height = Integer.parseInt(boundsElements[3]);
        
        frame.setBounds(bounds_.x,bounds_.y,
                    bounds_.width,bounds_.height);
      }
    }

    void storeBounds() {
      if (frame != null) {
        bounds_=new Rectangle(frame.getX(),frame.getY(),
              frame.getWidth() ,frame.getHeight());
      }
    }
    
    /**
     * Undock the UI component and display fullscreen.
     * 
     * @since  1.1
     */
    void maximizePanel() {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] devices = ge.getScreenDevices();
      for (int i=0; i<devices.length; i++) {
        GraphicsDevice device = devices[i];
        if (device.isFullScreenSupported() && device.getFullScreenWindow() == null) {     
          maximized_ = true;
          //maximizeDataPanel(panel_);
          //dataPanelContainer.removeDataPanel(component);

          frame = new JFrame(new SeriesFixedWidthTitleFormatter().getTitle(panel_));
          frame.setUndecorated(true);       
          frame.getContentPane().add(component_);
          
          try {
            device.setFullScreenWindow(frame);
          } catch (InternalError e) {
            log.error("Failed to switch to full screen mode: " + e.getMessage() + ".");
            restorePanel(true);
            return;
          }
          
          frame.setVisible(true);
          frame.requestFocus();
                  
          return;
        }
      }
      
      log.warn("No screens available or full screen exclusive mode is unsupported on your platform.");
    }
    
    /**
     * Leave fullscreen mode and dock the UI component if addToContainer is true.
     * 
     * @param addToContainer  whether to dock the UI component
     * @since                 1.1
     */
    void restorePanel(boolean addToContainer) {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] devices = ge.getScreenDevices();
      for (int i=0; i<devices.length; i++) {
        GraphicsDevice device = devices[i];
        if (device.isFullScreenSupported() && device.getFullScreenWindow() == frame) {
          if (frame != null) {
            frame.setVisible(false);
            device.setFullScreenWindow(null);
            frame.getContentPane().remove(component_);
            frame.dispose();
            frame = null;
          }
      
          maximized_ = false;
          
          if (addToContainer) {
            layoutDataPanels();
            //restoreDataPanel(this);
            //dataPanelContainer.addDataPanel(component);
          }
          
          break;
        }
      }
    } 
    
    /** Dispose of the frame for the UI component. Dock the UI component if
     * addToContainer is true.
     * 
     * @param addToContainer  whether to dock the UI component
     * @since                 1.1
     */
    void attachPanel(boolean addToContainer) {
      if (frame != null) {
        frame.setVisible(false);
        frame.getContentPane().remove(component_);
        frame.dispose();
        frame = null;
      }
      
      if (addToContainer) {
        attached_ = true;
        //properties.remove("attached");
        
        achButton.setIcon(RDVIcon.detach);
        layoutDataPanels();
        //attachDataPanel(this);
        //dataPanelContainer.addDataPanel(component);
      }
    }
  }
  
  private class ChannelTitle extends JPanel {

    /** serialization version identifier */
    private static final long serialVersionUID = -7191565876111378704L;

    private DataPanel panel_;
    
    private VisualizationSeries vizSeries_;
    
    public ChannelTitle(VisualizationSeries vizSeries, DataPanel dataPanel) {
      
      vizSeries_=vizSeries;
      panel_=dataPanel;
    
      setLayout(new BorderLayout());
      setBorder(new EmptyBorder(0, 0, 0, 5));
      setOpaque(false);
      
      JLabel text = new JLabel(vizSeries_.getDisplayName());
      text.setForeground(SimpleInternalFrame.getTextForeground(true));
      add(text, BorderLayout.CENTER);
      
      JButton closeButton = new JButton(RDVIcon.closeChannel);
      closeButton.setToolTipText("Remove channel");
      closeButton.setBorder(null);
      closeButton.setOpaque(false);
      closeButton.addActionListener(getActionListener());
      add(closeButton, BorderLayout.EAST);
    }
    
    protected ActionListener getActionListener() {
      return new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          panel_.removeSeries(vizSeries_.getName());
        }
      };
    }
  }
}



