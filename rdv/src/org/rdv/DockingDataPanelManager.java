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

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.ViewSerializer;
import net.infonode.docking.WindowPopupMenuFactory;
import net.infonode.docking.mouse.DockingWindowActionMouseButtonListener;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.BlueHighlightDockingTheme;
import net.infonode.docking.theme.ClassicDockingTheme;
import net.infonode.docking.theme.DefaultDockingTheme;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.GradientDockingTheme;
import net.infonode.docking.theme.LookAndFeelDockingTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.theme.SlimFlatDockingTheme;
import net.infonode.docking.theme.SoftBlueIceDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.MixedViewHandler;
import net.infonode.docking.util.PropertiesUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.datapanel.DataPanel;
import org.rdv.datapanel.PanelTitleFormatter;
import org.rdv.datapanel.SeriesFixedWidthTitleFormatter;
import org.rdv.ui.MainPanel;
import org.rdv.ui.UIUtilities;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A class to manage data panels with a docking framework.
 * 
 * @author  Drew Daugherty
 */
public class DockingDataPanelManager implements PanelManager {

  private static final RootWindowProperties titleBarStyleProperties = 
                PropertiesUtil.createTitleBarStyleRootWindowProperties();
  
  /**
   * The logger for this class.
   */
  static Log log = LogFactory.getLog(DockingDataPanelManager.class.getName());
  
  /**
   * The currently applied docking windows theme
   */
  private DockingWindowsTheme currentTheme_ = new ShapedGradientDockingTheme();
  
  /**
   * Show title bars.
   */
  private boolean titleBarTheme_ = false;
  
  /**
   * map containing all views.
   */
  private ViewMap viewMap_=new ViewMap();
  
  
  /**
   * map from view id # to data panel.
   */
  private Map<DataPanel,Integer> panelIdMap_ = new HashMap<DataPanel,Integer>();
  
  /**
   * The one and only root window
   */
  private RootWindow rootWindow_;
  
  /**
   * In this properties object the modified property values for close buttons etc. are stored. This object is cleared
   * when the theme is changed.
   */
  private RootWindowProperties properties_ = new RootWindowProperties();
  
  private MainPanel mainPanel_;
  
  /**
   * Set to true when closing panels manually should trigger 
   * a delete.  Set to false when programmatically manipulating
   * panels.
   */
  private boolean manualPanelClose_=true;
  
  /**
   * Tab window to add panels to by default.
   */
  TabWindow tabWindow_;
  
  DockingDataPanelManager(MainPanel mainPanel){
    mainPanel_=mainPanel;
    //defaultLayout();
  }
  
  /**
   * Creates the root window and the views.
   */
  private void createRootWindow() {

    // The mixed view map makes it easy to mix static and dynamic views inside the same root window
    MixedViewHandler handler = new MixedViewHandler(viewMap_, new ViewSerializer() {
      public void writeView(View view, ObjectOutputStream out) throws IOException {
        out.writeInt(((DockingView) view).getId());
      }

      public View readView(ObjectInputStream in) throws IOException {
        return viewMap_.getView(in.readInt());
      }
    });

    rootWindow_ = DockingUtil.createRootWindow(viewMap_, handler, true);
    
    // turn off tab window buttons
    rootWindow_.getRootWindowProperties().getTabWindowProperties().
                                getCloseButtonProperties().setVisible(false);//getTabProperties().getFocusedButtonProperties().getCloseButtonProperties().setVisible(false);
    //rootWindow_.getRootWindowProperties().getTabWindowProperties().getCloseButtonProperties().setVisible(false);
    rootWindow_.getRootWindowProperties().setRecursiveTabsEnabled(true);
    
    rootWindow_.getRootWindowProperties().getFloatingWindowProperties().setUseFrame(true);
    
    // disable per-tab buttons like undock/minimize
    rootWindow_.getRootWindowProperties().getTabWindowProperties().
                                getTabProperties().getHighlightedButtonProperties().
                                getUndockButtonProperties().setVisible(false);
    rootWindow_.getRootWindowProperties().getTabWindowProperties().getTabProperties().
                                getHighlightedButtonProperties().getDockButtonProperties().
                                setVisible(false);
    rootWindow_.getRootWindowProperties().getTabWindowProperties().
      getTabProperties().getHighlightedButtonProperties().
      getMinimizeButtonProperties().setVisible(false);
    
    final Icon closeIcon = rootWindow_.getRootWindowProperties().getTabWindowProperties().
                                              getCloseButtonProperties().getIcon();
    final Icon undockIcon = rootWindow_.getRootWindowProperties().getTabWindowProperties().
                                              getUndockButtonProperties().getIcon();    
    final Icon dockIcon = rootWindow_.getRootWindowProperties().getTabWindowProperties().
                                              getDockButtonProperties().getIcon();

    rootWindow_.setPopupMenuFactory(new WindowPopupMenuFactory() {
      public JPopupMenu createPopupMenu(final DockingWindow window) {
      
      JPopupMenu menu = new JPopupMenu();
      // Check that the window is a View
      if (window instanceof View) {
        final View v = (View) window;
        
        if(window instanceof DockingPanelView){
          final DockingPanelView dpv = (DockingPanelView)window;
          
          JMenuItem renameItem=menu.add("Set Description");
          renameItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        
                        String s = (String)JOptionPane.showInputDialog(
                                    window,
                                    "Enter new description:\n",
                                    "Set Description",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    null,
                                    dpv.getDataPanel().getDescription());
                        
                        if(s!=null){
                          //v.setName(s); 
                          //v.getViewProperties().setTitle(s);
                          dpv.getDataPanel().setDescription((s.isEmpty())?null:s);
                        }
                    }
                    });
        }
        
        JMenuItem closeItem=menu.add("Close");
        closeItem.setIcon(closeIcon);
        closeItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          try {
          window.closeWithAbort();
        } catch (OperationAbortedException e1) {
          //e1.printStackTrace();
        }
        }
        });
      
        if(v.isUndocked()){
          JMenuItem dockItem=menu.add("Dock");
          dockItem.setIcon(dockIcon);
          dockItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            try {
            window.dock();
          } catch (Exception e1) {
            //e1.printStackTrace();
          }
          }
          });
        }else{
          JMenuItem undockItem=menu.add("Undock");
          undockItem.setIcon(undockIcon);
          undockItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            try {
            window.undock(new Point(0,0));
          } catch (Exception e1) {
            //e1.printStackTrace();
          }
          }
          });
        }
      }
      return menu;
      }
      });
    
    properties_.addSuperObject(titleBarStyleProperties);
    titleBarTheme_=true;
    
    // Set gradient theme. The theme properties object is the super object of our properties object, which
    // means our property value settings will override the theme values
    properties_.addSuperObject(currentTheme_.getRootWindowProperties());

    // Our properties object is the super object of the root window properties object, so all property values of the
    // theme and in our property object will be used by the root window
    rootWindow_.getRootWindowProperties().addSuperObject(properties_);

    // Enable the bottom window bar
    rootWindow_.getWindowBar(Direction.DOWN).setEnabled(true);

    // Add a listener which shows dialogs when a window is closing or closed.
    rootWindow_.addListener(new DockingWindowAdapter() {
      public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
        updateViews(addedWindow, true);

        // If the added window is a floating window, then update it
        if (addedWindow instanceof FloatingWindow)
          updateFloatingWindow((FloatingWindow) addedWindow);
      }

      public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
        //updateViews(removedWindow, false);
      }

      public void windowClosing(DockingWindow window) throws OperationAbortedException {
        // Confirm close operation
        if (JOptionPane.showConfirmDialog(UIUtilities.getMainFrame(), 
                      "Really close data panel '" + window + "'?") != JOptionPane.YES_OPTION)
          throw new OperationAbortedException("Data panel close was aborted!");
        updateViews(window, false);
      }

      public void windowDocking(DockingWindow window) throws OperationAbortedException {
        // Confirm dock operation
        if (JOptionPane.showConfirmDialog(UIUtilities.getMainFrame(), 
                      "Really dock data panel '" + window + "'?") != JOptionPane.YES_OPTION)
          throw new OperationAbortedException("Data panel dock was aborted!");
      }

      public void windowUndocking(DockingWindow window) throws OperationAbortedException {
        // Confirm undock operation 
        if (JOptionPane.showConfirmDialog(UIUtilities.getMainFrame(), 
                      "Really undock data panel '" + window + "'?") != JOptionPane.YES_OPTION)
          throw new OperationAbortedException("Data panel undock was aborted!");
      }

    });

    // Add a mouse button listener that closes a window when it's clicked with the middle mouse button.
    rootWindow_.addTabMouseButtonListener(
            DockingWindowActionMouseButtonListener.MIDDLE_BUTTON_CLOSE_LISTENER);
    
    mainPanel_.add(rootWindow_);
  }
  
  
  /**
   * Update view menu items and dynamic view map.
   *
   * @param window the window in which to search for views
   * @param added  if true the window was added
   */
  private void updateViews(DockingWindow window, boolean added) {
    if(!manualPanelClose_){
      return;
    }
    
    if (window instanceof DockingView) {
      if (window instanceof DockingPanelView) {
        
        DockingPanelView dpv = (DockingPanelView) window;
        if (added){
          // should already be added but doesn't hurt
          panelIdMap_.put(dpv.getDataPanel(),new Integer(dpv.getId()));
        }else{
          // avoid concurrent modification exceptions when
          // programmatically deleting
          //if(manualPanelClose_){
            DataPanel panel = dpv.getDataPanel();
            log.info("Manually closed docking panel "+
                    new SeriesFixedWidthTitleFormatter().getTitle(panel));
            panel.closePanel();
            panelIdMap_.remove(dpv.getDataPanel());
          //}
        }
      }
      if(!added){
        viewMap_.removeView(((DockingView)window).getId());
      }
      
    } else {
      //recursively descend into window hierarchy
      for (int i = 0; i < window.getChildWindowCount(); i++)
        updateViews(window.getChildWindow(i), added);
    }
  }
  
  /**
   * Update the floating window by adding a menu bar and a status label if menu option is chosen.
   *
   * @param fw the floating window
   */
  private void updateFloatingWindow(FloatingWindow fw) {
  
    // Only update with if menu is selected
//    if (enableMenuAndStatusLabelMenuItem.isSelected()) {
//      // Create a dummy menu bar as example
//      JMenuBar bar = new JMenuBar();
//      bar.add(new JMenu("Menu 1")).add(new JMenuItem("Menu 1 Item 1"));
//      bar.add(new JMenu("Menu 2")).add(new JMenuItem("Menu 2 Item 1"));
//
//      // Set it in the root pane of the floating window
//      fw.getRootPane().setJMenuBar(bar);
//
//      // Create and add a status label
//      JLabel statusLabel = new JLabel("I'm a status label!");
//
//      // Add it as the SOUTH component to the root pane's content pane. Note that the actual floating
//      // window is placed in the CENTER position and must not be removed.
//      fw.getRootPane().getContentPane().add(statusLabel, BorderLayout.SOUTH);
//    }
  }

  
  /**
   * Sets the default window layout.
   */
  private void setDefaultLayout() {
    tabWindow_ = new TabWindow();
    
    DockingView channelListView = generateStaticView(
                          PanelManagerView.CHANNEL_LIST_VIEW_ID);
    DockingView controlView = generateStaticView(
                          PanelManagerView.CONTROL_VIEW_ID);
    DockingView markerView = generateStaticView(
                            PanelManagerView.MARKER_VIEW_ID);
    
    rootWindow_.setWindow(new SplitWindow(false,
                             0.12f, controlView,
                             new SplitWindow(true,
                                   0.2f, channelListView,
                                   new SplitWindow(false,
                                       0.9f,tabWindow_, markerView))));

    for(DataPanel panel:panelIdMap_.keySet()){
      int viewId=panelIdMap_.get(panel);
//      log.info("Adding panel "+
//          new SeriesFixedWidthTitleFormatter().getTitle(panel)+
//          " (view id "+viewId+")");
      tabWindow_.addTab(viewMap_.getView(viewId));
    }
    
//    WindowBar windowBar = rootWindow_.getWindowBar(Direction.DOWN);
//
//    while (windowBar.getChildWindowCount() > 0)
//      windowBar.getChildWindow(0).close();
//
//    windowBar.addTab(views[3]);
  }
  
//  @Override
//  public void addAudioPlayerComponent(JComponent audioComp) {
//    DockingView dv = new DockingView(AUDIO_PLAYER_VIEW_ID, "Audio Player", null, 
//        audioComp);
//    
//    viewMap_.addView(AUDIO_PLAYER_VIEW_ID, dv);
//  }
//
//  @Override
//  public void addChannelListComponent(JComponent clComp) {
//    DockingView dv = new DockingView(CHANNEL_LIST_VIEW_ID, "Channel List", null, 
//        clComp); 
//    viewMap_.addView(CHANNEL_LIST_VIEW_ID, dv);
//  }
//
//  @Override
//  public void addControlComponent(JComponent ctrlComp) {
//    DockingView dv = new DockingView(CONTROL_VIEW_ID, "Control", null, 
//        ctrlComp); 
//    viewMap_.addView(CONTROL_VIEW_ID, dv);
//  }

  private int getNextViewId(){
    int ret=0;
    // iterate through data panel view ids
    for(int viewId: panelIdMap_.values()){
      ret=Math.max(viewId+1, ret);
    }
    return ret;
  }
  
  @Override
  public int addDataPanel(DataPanel dataPanel) {
    if(!panelIdMap_.keySet().contains(dataPanel)){
      int newId=getNextViewId();
      addDataPanel(newId,dataPanel);
      return newId;
    }else{
      return panelIdMap_.get(dataPanel);
    }
  }

  
  private void showView(View v){
    if (v.getRootWindow() != null){
      v.restoreFocus();
    }else{ 
      if(tabWindow_!=null && tabWindow_.isShowing()){
        tabWindow_.addTab(v);
      }else{
        //TODO add logic to find first tab window containing a data panel
        // rather than split root window?
        tabWindow_=new TabWindow(v);
        
        DockingWindow dockRoot=rootWindow_.getWindow();
        if (dockRoot instanceof SplitWindow){
          SplitWindow splitRoot =(SplitWindow)dockRoot;
          //System.out.println(((splitRoot.isHorizontal())?"Horizontal":"Vertical")+
           //   ": "+splitRoot.getDividerLocation()+", count: "+splitRoot.getChildWindowCount());
          dockRoot.split(tabWindow_, Direction.RIGHT, 0.66f);
        }else{
          DockingUtil.addWindow(tabWindow_, rootWindow_);
        }
      }
    }
  }
  
  public boolean isVisible(int viewId){
    View v = viewMap_.getView(viewId);
    if ((v!=null) && (v.getRootWindow()!=null)){
      return true;
    }else{
      return false;
    }
  }
  
  public void setVisible(int viewId, boolean visible){
    View v = viewMap_.getView(viewId);
    if (v==null){
      if(visible){
        // view was closed, regenerate
        DockingView dv = generateStaticView(viewId);
        showView(dv);
      }
    }else{
      if(visible){
        showView(v);
      }else{
        v.minimize();
      }
    }
  }
  
  private DockingView generateStaticView (int viewId){
    
    DockingView dv = (DockingView)viewMap_.getView(viewId);
    if(dv!=null){
      return dv;
    }else{
      switch(viewId){
      case PanelManagerView.AUDIO_PLAYER_VIEW_ID:
        JComponent audioPlayerComp = mainPanel_.getAudioPlayerComponent();
        audioPlayerComp.setVisible(true);
        dv = new DockingView(PanelManagerView.AUDIO_PLAYER_VIEW_ID, "Audio Player", 
                    null, audioPlayerComp);
        break;
      case PanelManagerView.CHANNEL_LIST_VIEW_ID:
        dv = new DockingView(PanelManagerView.CHANNEL_LIST_VIEW_ID, "Channel List", 
                    null, mainPanel_.getChannelListComponent());
        break;
      case PanelManagerView.CONTROL_VIEW_ID:      
        dv = new DockingView(PanelManagerView.CONTROL_VIEW_ID, "Control", null, 
             mainPanel_.getControlComponent());
        break;
      default:
        dv = new DockingView(PanelManagerView.MARKER_VIEW_ID, "Event Marker", null, 
             mainPanel_.getMarkerSubmitComponent());
        break;
      }
      viewMap_.addView(viewId, dv);
      return dv;
    }
  }
  
  public void removeDataPanel(DataPanel dataPanel) {
    //dataPanel.setPanelManager(this);
    //dataPanel.setHideTitle(true);
    int viewId = getViewId(dataPanel);
    
    viewMap_.removeView(viewId);
    panelIdMap_.remove(dataPanel);
  }
  
  @Override
  public void addDataPanel(int viewId, DataPanel dataPanel) {
    dataPanel.setPanelManager(this);
    //dataPanel.setHideTitle(true);
    log.info("Adding panel "+
        new SeriesFixedWidthTitleFormatter().getTitle(dataPanel)+
        " (view id "+viewId+")");
    DockingView dv = new DockingPanelView(viewId, dataPanel);
    
    viewMap_.addView(viewId, dv);
    panelIdMap_.put(dataPanel,viewId);
  }

//  @Override
//  public void addMarkerSubmitComponent(JComponent markerComp) {
//    DockingView dv = new DockingView(MARKER_VIEW_ID, "Event Marker", null, 
//        markerComp); 
//    viewMap_.addView(MARKER_VIEW_ID, dv);
//  }

//  @Override
//  public void attachDataPanel(DataPanel dataPanel) {
//    // TODO Auto-generated method stub
//    
//  }

  public void buildMenu(JMenu menu){
    // add lots of menus here
    menu.add(createFocusViewMenu());
    menu.add(createThemesMenu());
    menu.add(createPropertiesMenu());
    menu.add(createWindowBarsMenu());

  }

  /**
   * Creates the menu where individual window bars can be enabled and disabled.
   *
   * @return the window bar menu
   */
  private JMenu createWindowBarsMenu() {
    JMenu barsMenu = new JMenu("Window Bars");

    for (int i = 0; i < 4; i++) {
      final Direction d = Direction.getDirections()[i];
      JCheckBoxMenuItem item = new JCheckBoxMenuItem("Toggle " + d);
      item.setSelected(rootWindow_.getWindowBar(d).isEnabled());
      barsMenu.add(item).addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // Enable/disable the window bar
          rootWindow_.getWindowBar(d).setEnabled(
                !rootWindow_.getWindowBar(d).isEnabled());
        }
      });
    }

    return barsMenu;
  }
  
  /**
   * Creates the menu where different property values can be modified.
   *
   * @return the properties menu
   */
  private JMenu createPropertiesMenu() {
    JMenu buttonsMenu = new JMenu("Properties");

    buttonsMenu.add("Enable Close").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        properties_.getDockingWindowProperties().setCloseEnabled(true);
      }
    });

    buttonsMenu.add("Hide Close Buttons").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        properties_.getDockingWindowProperties().setCloseEnabled(false);
      }
    });

    buttonsMenu.add("Freeze Layout").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        freezeLayout(true);
      }
    });

    buttonsMenu.add("Unfreeze Layout").addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        freezeLayout(false);
      }
    });

    return buttonsMenu;
  }
  

  /**
   * Freezes or unfreezes the window layout and window operations.
   *
   * @param freeze true for freeze, otherwise false
   */
  private void freezeLayout(boolean freeze) {
    // Freeze window operations
    properties_.getDockingWindowProperties().setDragEnabled(!freeze);
    properties_.getDockingWindowProperties().setCloseEnabled(!freeze);
    properties_.getDockingWindowProperties().setMinimizeEnabled(!freeze);
    properties_.getDockingWindowProperties().setRestoreEnabled(!freeze);
    properties_.getDockingWindowProperties().setMaximizeEnabled(!freeze);
    properties_.getDockingWindowProperties().setUndockEnabled(!freeze);
    properties_.getDockingWindowProperties().setDockEnabled(!freeze);

    // Freeze tab reordering inside tabbed panel
    properties_.getTabWindowProperties().
          getTabbedPanelProperties().setTabReorderEnabled(!freeze);
  }
  
  /**
   * Creates the menu where views can be shown and focused.
   *
   * @return the focus view menu
   */
  private JMenu createFocusViewMenu() {
    JMenu viewsMenu = new JMenu("Focus View");

    for (int i = 0; i < viewMap_.getViewCount(); i++) {
      final View view = viewMap_.getView(i);
      if (view==null) continue;
      viewsMenu.add("Focus " + view.getTitle()).addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              // Ensure the view is shown in the root window
              DockingUtil.addWindow(view, rootWindow_);

              // Transfer focus to the view
              view.restoreFocus();
            }
          });
        }
      });
    }

    return viewsMenu;
  }
  
  /**
   * Creates the menu where the theme can be changed.
   *
   * @return the theme menu
   */
  private JMenu createThemesMenu() {
    JMenu themesMenu = new JMenu("Themes");

// don't get the title bar window thing
    
    
    final JCheckBoxMenuItem titleBarStyleItem = new JCheckBoxMenuItem("Title Bar Style Theme");
    titleBarStyleItem.setSelected(titleBarTheme_);
    titleBarStyleItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!titleBarTheme_)
          properties_.addSuperObject(titleBarStyleProperties);
        else{
          // TODO this does not work to remove title bars from windows 
          properties_.removeSuperObject(titleBarStyleProperties);
        }
        titleBarTheme_=!titleBarTheme_;
      }
    });

    themesMenu.add(titleBarStyleItem);
    themesMenu.add(new JSeparator());

    DockingWindowsTheme[] themes = {new DefaultDockingTheme(),
                                    new LookAndFeelDockingTheme(),
                                    new BlueHighlightDockingTheme(),
                                    new SlimFlatDockingTheme(),
                                    new GradientDockingTheme(),
                                    new ShapedGradientDockingTheme(),
                                    new SoftBlueIceDockingTheme(),
                                    new ClassicDockingTheme()};

    ButtonGroup group = new ButtonGroup();

    for (int i = 0; i < themes.length; i++) {
      final DockingWindowsTheme theme = themes[i];

      JRadioButtonMenuItem item = new JRadioButtonMenuItem(theme.getName());
      item.setSelected(themes[i].getClass()==currentTheme_.getClass());
      
      group.add(item);

      themesMenu.add(item).addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // Clear the modified properties values
          properties_.getMap().clear(true);

          setTheme(theme);
        }
      });
    }
    return themesMenu;
  }

  /**
   * Sets the docking windows theme.
   *
   * @param theme the docking windows theme
   */
  private void setTheme(DockingWindowsTheme theme) {
    properties_.replaceSuperObject(currentTheme_.getRootWindowProperties(),
                                  theme.getRootWindowProperties());
    currentTheme_ = theme;
  }
  
  @Override
  public void closeAllDataPanels() {
    // this works but we cannot modify panelIdMap_ 
    // on window closed event...
    manualPanelClose_=false;
    
    for(DataPanel panel: panelIdMap_.keySet()){
      Integer viewId=panelIdMap_.get(panel);
      View v=viewMap_.getView(viewId);
      if(v!=null){
        v.dock();
        v.close();
        viewMap_.removeView(viewId);
      }else{
        log.warn("view "+viewId+" not found");
      }
      log.info("Closed docking panel "+
          new SeriesFixedWidthTitleFormatter().getTitle(panel));
      panel.closePanel();
    }
    System.out.println(viewMap_.getViewCount()+" views remain");
    panelIdMap_.clear();
    manualPanelClose_=true;
  }

  //@Override
  //private void closeDataPanel(DataPanel dataPanel) {
    // already handled by window close event
//    Integer viewId=panelIdMap_.get(dataPanel);
//    viewMap_.removeView(viewId);
//    panelIdMap_.remove(dataPanel);
//  }

//  @Override
//  public void detachDataPanel(DataPanel dataPanel) {
//    // TODO Auto-generated method stub
//    
//  }
  
  public void releaseAllViews() {
    // this works but we cannot modify panelIdMap_ 
    // on window closed event...
    manualPanelClose_=false;
    
    for(int i=0;i<viewMap_.getViewCount();i++){
      DockingView v=(DockingView)viewMap_.getViewAtIndex(i);
//      if(v instanceof DockingPanelView){
//        DataPanel panel = ((DockingPanelView)v).getDataPanel();
//        panel.closePanel();
//        log.info("Closed docking panel "+
//            new SeriesFixedWidthTitleFormatter().getTitle(panel)+
//            " with id "+v.getId());
//      }else{
//        System.out.println("Closing view with id "+v.getId());
//      }
      v.dock();
      viewMap_.removeView(v.getId());
      v.close();
    }
    
    panelIdMap_.clear();
    mainPanel_.removeAll();
    manualPanelClose_=true;
  }

  
  @Override
  public void defaultLayout() {  
    mainPanel_.removeAll();
    createRootWindow();
    setDefaultLayout();
    mainPanel_.validate();
    manualPanelClose_=true;
  }
  
  @Override
  public DataPanel getDataPanel(int viewId) {
    DockingView v=(DockingView)viewMap_.getViewAtIndex(viewId);
    if(v instanceof DockingPanelView){
      return ((DockingPanelView)v).getDataPanel();
    }else{
      return null;
    }
  }

  @Override
  public PanelLayout getPanelLayout() {
    // this is not meaningful
    return PanelLayout.TILED_LAYOUT;
  }

  @Override
  public boolean isAnyChannelSubscribed(List<String> channels) {
    for (DataPanel panel : panelIdMap_.keySet()){
      for(String channel: channels){
        if(panel.isChannelSubscribed(channel)){
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void setPanelLayout(PanelLayout layout) {
    // TODO Auto-generated method stub
    // ???
  }

 // @Override
//  public void showDataPanel(DataPanel dataPanel) {
//    addDataPanel(dataPanel);
//    Integer viewId=panelIdMap_.get(dataPanel);
//    if(viewId!=null){
//      showView(viewMap_.getView(viewId));
//    }
//  }
  @Override
  public void showView(int viewId){
    showView(viewMap_.getView(viewId));
  }

  @Override
  public int addDataPanelToContainer(DataPanel dataPanel, String container) {
    return addDataPanel(dataPanel);
  }

  @Override
  public void unsubscribeChannels(List<String> channels) {
    for (DataPanel panel : panelIdMap_.keySet()){
      for(String channel: channels){
//        if(panel.isChannelSubscribed(channel)){
          panel.removeChannel(channel);
          if(panel.subscribedChannelCount()==0){
            panel.closePanel();
            int viewId=panelIdMap_.get(panel);
            View v=viewMap_.getView(viewId);
            v.close();
            // these should be handled by updateViews
          //viewMap_.removeView(viewId);
            //panelIdMap_.remove(panel);
          }
//        }
      }
    }
  }

  @Override
  public Collection<DataPanel> getDataPanels() {
    Collection<DataPanel> dataPanels = new HashSet<DataPanel>();
    dataPanels.addAll(panelIdMap_.keySet());
    return dataPanels;
  }

  @Override
  public int getDisplayPriority(DataPanel dataPanel) {
    int viewId=getViewId(dataPanel);
    return isVisible(viewId)?0:1;
  }

  @Override
  public Document getConfigurationXML() throws Exception {
    Document ret= DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element stateEl=ret.createElement("serialState");
    ret.appendChild(stateEl);
    
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(bos);
    rootWindow_.write(out, false);
    out.close();
    String data = new sun.misc.BASE64Encoder().encode(bos.toByteArray());
    stateEl.appendChild(ret.createCDATASection(data));
    
    return ret;
  }

  @Override
  public void setConfigurationXML(Document document) throws Exception {
    XPath xp = XPathFactory.newInstance().newXPath();
    
    Node layoutNode = (Node)xp.evaluate("windowLayout/serialState", document, 
                                        XPathConstants.NODE);
    
    final CDATASection cdata = (CDATASection)layoutNode.getFirstChild();

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try{
          byte layoutData[] = new sun.misc.BASE64Decoder().decodeBuffer(cdata.getData());
          ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(layoutData));
          rootWindow_.read(in, true);
          in.close();
        }catch(Exception e){
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  public int getViewId(DataPanel dataPanel) {
    Integer i=panelIdMap_.get(dataPanel);
    return (i==null)?-1:i;
  }

  @Override
  public void updateViewTitle(int viewId) {
    View v = viewMap_.getView(viewId);
    if(v!=null && v instanceof DockingPanelView){
      DockingPanelView dpv = (DockingPanelView)v;
      PanelTitleFormatter ptf = new SeriesFixedWidthTitleFormatter();
      String panelTitle = ptf.getTitle(dpv.panel_);
      
      v.setName(panelTitle); 
      v.getViewProperties().setTitle(panelTitle);
    }
  }
  
  private class DockingView extends View{
    /**
     * Unique id for all views.
     */
    int id_;
    
    public DockingView(int id, String title, Icon icon, 
                         Component component) {
      super(title, icon, component);
      id_=id;
    }

    public int getId(){
      return id_;
    }
  }
  
  private class DockingPanelView extends DockingView{
    DataPanel panel_;
    
    public DockingPanelView(int id, DataPanel panel) {
      super(id, new SeriesFixedWidthTitleFormatter().getTitle(panel),
           null, panel.getComponent());
      
      panel_=panel;
      
    }
    
    public DataPanel getDataPanel(){
      return panel_;
    }
  }
}