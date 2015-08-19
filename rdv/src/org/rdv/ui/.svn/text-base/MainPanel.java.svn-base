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
 * $URL: http://rdv.googlecode.com/svn/trunk/src/org/rdv/ui/MainPanel.java $
 * $Revision: 1322 $
 * $Date: 2008-12-02 14:42:54 -0600 (Tue, 02 Dec 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.rdv.DataViewer;
import org.rdv.DockingDataPanelManager;
import org.rdv.Extension;
import org.rdv.ExtensionManager;
import org.rdv.ManagerStore;
import org.rdv.PanelManagerView;
import org.rdv.RDV;
import org.rdv.action.ActionFactory;
import org.rdv.action.Actions;
import org.rdv.action.DataViewerAction;
import org.rdv.auth.AuthenticationManager;
import org.rdv.data.Channel;
import org.rdv.data.LocalChannelManager;
import org.rdv.datapanel.DataPanel;
import org.rdv.property.PropertyRepository;
import org.rdv.rbnb.ConnectionListener;
import org.rdv.rbnb.LocalServer;
import org.rdv.rbnb.MessageListener;
import org.rdv.rbnb.Player;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBHelper;
import org.rdv.rbnb.StateListener;
import org.rdv.rbnb.TimeRange;
import org.rdv.ui.channel.LocalChannelDialog;
import org.rdv.ui.RDVIcon;

import com.jgoodies.uif_lite.component.Factory;

/**
 * Main frame for the application.  Creates built-in panels
 * and main menu. Delegates management of windows to PanelManager
 * and ManagerStore.
 * 
 * @author  Jason P. Hanley
 * @author  Lawrence J. Miller
 * @since   1.2
 */
public class MainPanel extends JPanel implements MessageListener, ConnectionListener, 
                                                  StateListener, ActionListener 
{

  /** serialization version identifier */
  private static final long serialVersionUID = -4692978463068122918L;
	
	static Log log = LogFactory.getLog(MainPanel.class.getName());
	
	private RBNBController rbnb;
	//private PanelManager dataPanelManager;
	
 	private BusyDialog busyDialog;
 	private LoadingDialog loadingDialog;
	
	private JFrame frame;
	//private GridBagConstraints c;
	private JMenuBar menuBar;
	private ChannelListPanel channelListPanel;
	private MetadataPanel metadataPanel;
  private JSplitPane leftPanel;
	private JPanel rightPanel;
	private ControlPanel controlPanel;
  private AudioPlayerPanel audioPlayerPanel;
  private MarkerSubmitPanel markerSubmitPanel;
 	
 	private OptionsDialog optionsDialog;
	private AboutDialog aboutDialog;
	private ConsoleDialog consoleDialog;
	private ConfigLoadRBNBDialog configLoadRBNBDialog;
	private ConfigLoadFileDialog configLoadFileDialog;
	private ConfigSaveRBNBDialog configSaveRBNBDialog;
	private ConfigSaveFileDialog configSaveFileDialog;
	private RBNBConnectionDialog rbnbConnectionDialog;

 	private Action fileAction;
 	private Action connectAction;
 	private Action disconnectAction;
 	private Action loginAction;
 	private Action logoutAction;
 	private Action loadAction;
 	private Action loadFileAction;
 	private Action loadRBNBAction;
 	private Action saveAction;
 	private Action saveFileAction;
 	private Action saveRBNBAction;
 	private Action importAction;
 	private Action exportAction; 
 	private JMenuItem exportMenuItem;
 	private Action exitAction;
 	private Action exportVideoAction;
  
 	private Action controlAction;
 	private DataViewerAction realTimeAction;
 	private DataViewerAction playAction;
 	private DataViewerAction pauseAction;
 	private Action beginningAction;
 	private Action endAction;
 	private Action gotoTimeAction;
 	private Action updateChannelListAction;
 	private Action optionsDialogAction;
 	private DataViewerAction dropDataAction;
 	private DataViewerAction disableLocalChannelsAction;
 	
 	private Action viewAction;
 	private DataViewerAction showChannelListAction;
 	private DataViewerAction showControlPanelAction;
 	private DataViewerAction showAudioPlayerPanelAction;
 	private DataViewerAction showMarkerPanelAction;

 	private Action showHiddenChannelsAction;
 	private Action hideEmptyTimeAction;
 	private Action fullScreenAction;
 	
 	private Action windowAction;
 	private Action setPanelManagerAction;
 	private Action basicPanelManagerAction;
 	private Action dockingPanelManagerAction;
  
 	private Action closeAllDataPanelsAction;
  
 	private Action helpAction;
 	private Action usersGuideAction;
 	private Action supportAction;
 	private Action releaseNotesAction;
 	private Action aboutAction;
  
 	private JLabel throbber;
 	private Icon throbberStop;
 	private Icon throbberAnim;
  
 	private final Object loadingMonitor = new Object();
  
  /** the key mask used for menus shortcuts */
  private final int menuShortcutKeyMask;
  private static final PropertyRepository PROPERTY_REPO
    = PropertyRepository.getInstance();

  private static final String ADD_CHANNEL_MENU_ITEM_KEY = "addChannelMenuItem.text";
	public MainPanel() {
		super();
		
		this.rbnb = RBNBController.getInstance();
		
		busyDialog = null;
		loadingDialog = null;
    
    // set the menu shortcut key mask in a platform independent way
    menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		
		initFrame();
	}
	
	public JFrame getFrame()
	{
		return frame;
	}

	private void initFrame() {
		frame = Application.getInstance(RDV.class).getMainFrame();
    
    setLayout(new BorderLayout());
		//c = new GridBagConstraints();

 		initActions();	
 		initMenuBar();
    
    channelListPanel = new ChannelListPanel();
    channelListPanel.setMinimumSize(new Dimension(0, 0));
    
    metadataPanel = new MetadataPanel(rbnb);
    
    leftPanel = Factory.createStrippedSplitPane(
        JSplitPane.VERTICAL_SPLIT,
        channelListPanel,
        metadataPanel,
        0.65f);
    leftPanel.setContinuousLayout(true);
    leftPanel.setBorder(new EmptyBorder(8, 8, 8, 0));
    log.info("Created left panel");
    
    controlPanel = new ControlPanel();
    
    audioPlayerPanel = new AudioPlayerPanel();
    //audioPlayerPanel.setVisible(false);
    
    markerSubmitPanel = new MarkerSubmitPanel(rbnb);
    
		//initRightPanel();
		//initControls();         
//		initTabbedPane();
//    initAudioPlayerPanel();
    //initMarkerSubmitPanel();    
    
//    dataPanelManager.addAudioPlayerComponent(audioPlayerPanel);
//    dataPanelManager.addChannelListComponent(leftPanel);
//    dataPanelManager.addControlComponent(controlPanel);
//    dataPanelManager.addMarkerSubmitComponent(markerSubmitPanel);
//    dataPanelManager.doInitialLayout();
//    
		//initSplitPane();
    
    channelListPanel.addChannelSelectionListener(metadataPanel);
		
		rbnb.addSubscriptionListener(controlPanel);
		
		rbnb.addTimeListener(controlPanel);
		
		rbnb.addStateListener(channelListPanel);
		rbnb.addStateListener(controlPanel);
    rbnb.addStateListener(this);

		rbnb.getMetadataManager().addMetadataListener(channelListPanel);
    rbnb.getMetadataManager().addMetadataListener(metadataPanel);
		rbnb.getMetadataManager().addMetadataListener(controlPanel);
		
		rbnb.addPlaybackRateListener(controlPanel);
    
    rbnb.addTimeScaleListener(controlPanel);
		
  	rbnb.addMessageListener(this);
  	
  	rbnb.addConnectionListener(this);
	}
  	
	public JComponent getChannelListComponent(){
	  return leftPanel;
	}
	
  public JComponent getControlComponent(){
    return controlPanel;
  }
  
  public JComponent getAudioPlayerComponent(){
    return audioPlayerPanel;
  }
  
  public JComponent getMarkerSubmitComponent(){
    return markerSubmitPanel;
  }
  
 	private void initActions() {
 		fileAction = new DataViewerAction("File", "File Menu", KeyEvent.VK_F);
 		
 		connectAction = new DataViewerAction("Connect", "Connect to RBNB server", KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcutKeyMask|ActionEvent.SHIFT_MASK)) {
      /** serialization version identifier */
      private static final long serialVersionUID = 5038790506859429244L;

      public void actionPerformed(ActionEvent ae) {
 				if (rbnbConnectionDialog == null) {
 					rbnbConnectionDialog = new RBNBConnectionDialog(frame, rbnb, ManagerStore.getPanelManager());
 				} else {
 					rbnbConnectionDialog.setVisible(true);
 				}			
 			}			
 		};
 		
 		disconnectAction = new DataViewerAction("Disconnect", "Disconnect from RBNB server", KeyEvent.VK_D, KeyStroke.getKeyStroke(KeyEvent.VK_D, menuShortcutKeyMask|ActionEvent.SHIFT_MASK)) {
      /** serialization version identifier */
      private static final long serialVersionUID = -1871076535376405181L;

      public void actionPerformed(ActionEvent ae) {
        ManagerStore.getPanelManager().closeAllDataPanels();
 				rbnb.disconnect();
 			}			
 		};


 		loadRBNBAction = new DataViewerAction("Load Server Setup", "Load data viewer setup from RBNB server") {
 		  /** serialization version identifier */
      private static final long serialVersionUID = 6021638195148556716L;

      public void actionPerformed(ActionEvent ae) {

        if(configLoadRBNBDialog != null) {
          configLoadRBNBDialog.setVisible(false);
          configLoadRBNBDialog.dispose();
        }
        //if(configLoadRBNBDialog == null) {
          configLoadRBNBDialog = new ConfigLoadRBNBDialog(frame, "Load Server Setup");
        //} else {
        //  configLoadRBNBDialog.setVisible(true);
        //}
      }     
    };
 		 		
    loadFileAction = new DataViewerAction("Load Setup File", "Load data viewer setup from file") {
      /** serialization version identifier */
      private static final long serialVersionUID = 7197815395398039821L;

      public void actionPerformed(ActionEvent ae) {

        if(configLoadFileDialog == null) {
          configLoadFileDialog = new ConfigLoadFileDialog(frame);
        } else {
          configLoadFileDialog.setVisible(true);
        }
      }     
    };

    saveRBNBAction = new DataViewerAction("Save Setup to Server", "Save data viewer setup to RBNB server") {
      /**
       * 
       */
      private static final long serialVersionUID = 7753400851956806374L;

      /** serialization version identifier */


      public void actionPerformed(ActionEvent ae) {
        
        if(configSaveRBNBDialog != null) {
          configSaveRBNBDialog.setVisible(false);
          configSaveRBNBDialog.dispose();
        }
        configSaveRBNBDialog=new ConfigSaveRBNBDialog(frame, "Save Server Setup");
        
//        if(configSaveRBNBDialog == null) {
//          configSaveRBNBDialog = new ConfigSaveRBNBDialog(frame);
//        } else {
//          configSaveRBNBDialog.setVisible(true);
//        }     
      }     
    };
    
    saveFileAction = new DataViewerAction("Save Setup to File", "Save data viewer setup to file") {
      /** serialization version identifier */
      private static final long serialVersionUID = -8259994975940624038L;

      public void actionPerformed(ActionEvent ae) {
        
        if(configSaveFileDialog == null) {
          configSaveFileDialog = new ConfigSaveFileDialog(frame);
        } else {
          configSaveFileDialog.setVisible(true);
        }     
      }     
    };    

    loadAction = new DataViewerAction("Load Setup", "Load Setup Menu", KeyEvent.VK_T);
    saveAction = new DataViewerAction("Save Setup", "Save Setup Menu", KeyEvent.VK_S);
    importAction = new DataViewerAction("Import", "Import Menu", KeyEvent.VK_I, RDVIcon.importData);
    
    exportAction = new DataViewerAction("Export", "Export Menu", KeyEvent.VK_E, RDVIcon.exportData);

    exportVideoAction = new DataViewerAction("Export RBNB video to Local", "Export video from the RBNB server to the local computer") {
      /** serialization version identifier */
      private static final long serialVersionUID = -6420430928972633313L;

      public void actionPerformed(ActionEvent ae) {
        showExportVideoDialog();
      }
    };


 
      exitAction = new DataViewerAction("Exit", "Exit RDV", KeyEvent.VK_X) {
      /** serialization version identifier */
      private static final long serialVersionUID = 3137490972014710133L;

      public void actionPerformed(ActionEvent ae) {
 				Application.getInstance().exit(ae);
 			}			
 		};
 		
 		controlAction = new DataViewerAction("Control", "Control Menu", KeyEvent.VK_C);
 
 		realTimeAction = new DataViewerAction("Real Time", "View data in real time", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_R, menuShortcutKeyMask), RDVIcon.rt) {
      /** serialization version identifier */
      private static final long serialVersionUID = -7564783609370910512L;

      public void actionPerformed(ActionEvent ae) {
 				rbnb.monitor();
 			}			
 		};
 		
 		playAction = new DataViewerAction("Play", "Playback data", KeyEvent.VK_P, KeyStroke.getKeyStroke(KeyEvent.VK_P, menuShortcutKeyMask), RDVIcon.play) {
      /** serialization version identifier */
      private static final long serialVersionUID = 5974457444931142938L;

      public void actionPerformed(ActionEvent ae) {
 				rbnb.play();
 			}			
 		};
 
 		pauseAction = new DataViewerAction("Pause", "Pause data display", KeyEvent.VK_A, KeyStroke.getKeyStroke(KeyEvent.VK_S, menuShortcutKeyMask), RDVIcon.pause) {
      /** serialization version identifier */
      private static final long serialVersionUID = -5297742186923194460L;

      public void actionPerformed(ActionEvent ae) {
 				rbnb.pause();
 			}			
 		};
 
 		beginningAction = new DataViewerAction("Go to beginning", "Move the location to the start of the data", KeyEvent.VK_B, KeyStroke.getKeyStroke(KeyEvent.VK_B, menuShortcutKeyMask), RDVIcon.begin) {
      /** serialization version identifier */
      private static final long serialVersionUID = 9171304956895497898L;

      public void actionPerformed(ActionEvent ae) {
				controlPanel.setLocationBegin();
 			}			
 		};
 
 		endAction = new DataViewerAction("Go to end", "Move the location to the end of the data", KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_E, menuShortcutKeyMask), RDVIcon.end) {
      /** serialization version identifier */
      private static final long serialVersionUID = 1798579248452726211L;

      public void actionPerformed(ActionEvent ae) {
 				controlPanel.setLocationEnd();
 			}			
 		};

 		gotoTimeAction = new DataViewerAction("Go to time", "Move the location to specific date time of the data", KeyEvent.VK_T, KeyStroke.getKeyStroke(KeyEvent.VK_T, menuShortcutKeyMask)) {
      /** serialization version identifier */
      private static final long serialVersionUID = -6411442297488926326L;

      public void actionPerformed(ActionEvent ae) {
        TimeRange timeRange = RBNBHelper.getChannelsTimeRange();
 			  double time = DateTimeDialog.showDialog(frame, rbnb.getLocation(), timeRange.start, timeRange.end);
        if (time >= 0) {
          rbnb.setLocation(time);
        }
 			}			
 		};
 
 		updateChannelListAction = new DataViewerAction("Update Channel List", "Update the channel list", KeyEvent.VK_U, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), RDVIcon.refresh) {
      /** serialization version identifier */
      private static final long serialVersionUID = -170096772973697277L;

      public void actionPerformed(ActionEvent ae) {
 				rbnb.updateMetadata();
 			}			
 		};
 
 		optionsDialogAction = new DataViewerAction("Options", "Display options dialog", KeyEvent.VK_O) {

      /**
       * serialization version identifier
       */
      private static final long serialVersionUID = 6753155740399556824L;

      public void actionPerformed(ActionEvent ae) {
        showOptionsDialog();
      }     
    };

 		dropDataAction = new DataViewerAction("Drop Data", "Drop data if playback can't keep up with data rate", KeyEvent.VK_D, RDVIcon.dropData) {
      /** serialization version identifier */
      private static final long serialVersionUID = 7079791364881120134L;

      public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 				rbnb.setDropData(menuItem.isSelected());		
 			}			
 		};
 		
 		disableLocalChannelsAction = new DataViewerAction("Disable Local Channels", "Disable support for local channels", KeyEvent.VK_D) {
      /** serialization version identifier */
      private static final long serialVersionUID = 7079791364881120134L;

      public void actionPerformed(ActionEvent ae) {
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
        LocalChannelManager.getInstance().setDoLocalMerge(!menuItem.isSelected()); 
        
      }     
    };
    
 		viewAction = new DataViewerAction("View", "View Menu", KeyEvent.VK_V);

 		showChannelListAction = new DataViewerAction("Show Channels", "", KeyEvent.VK_L, RDVIcon.channels) {
      /** serialization version identifier */
      private static final long serialVersionUID = 4982129759386009112L;

      public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
        //channelListPanel.setVisible(menuItem.isSelected());
        //layoutSplitPane();
        //leftPanel.resetToPreferredSizes();
 				ManagerStore.getPanelManager().setVisible(
 				                    PanelManagerView.CHANNEL_LIST_VIEW_ID,
 				                    menuItem.isSelected());
        //leftPanel.setVisible(menuItem.isSelected());
 			}			
 		};
    
//    showMetadataPanelAction = new DataViewerAction("Show Properties", "", KeyEvent.VK_P, RDVIcon.properties) {
//      /** serialization version identifier */
//      private static final long serialVersionUID = 430106771704397810L;
//
//      public void actionPerformed(ActionEvent ae) {
//        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
//        metadataPanel.setVisible(menuItem.isSelected());
//        //layoutSplitPane();
//        leftPanel.resetToPreferredSizes();
//      }     
//    };    
 
 		showControlPanelAction = new DataViewerAction("Show Control Panel", "", KeyEvent.VK_C, RDVIcon.control) {
      /** serialization version identifier */
      private static final long serialVersionUID = 6401715717710735485L;

      public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 				ManagerStore.getPanelManager().setVisible(
 				                    PanelManagerView.CONTROL_VIEW_ID,
 				                    menuItem.isSelected());
 				//controlPanel.setVisible(menuItem.isSelected());
 			}			
 		};

    showAudioPlayerPanelAction = new DataViewerAction ("Show Audio Player", "", KeyEvent.VK_A, RDVIcon.audio) {
      /** serialization version identifier */
      private static final long serialVersionUID = -4248275698973916287L;

      public void actionPerformed (ActionEvent ae) {
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource ();
        ManagerStore.getPanelManager().setVisible(
                            PanelManagerView.AUDIO_PLAYER_VIEW_ID,
                            menuItem.isSelected());
        //audioPlayerPanel.setVisible(menuItem.isSelected());
      }
    };    
    
    showMarkerPanelAction = new DataViewerAction ("Show Marker Panel", "", KeyEvent.VK_M, RDVIcon.info) {
      /** serialization version identifier */
      private static final long serialVersionUID = -5253555511660929640L;

      public void actionPerformed (ActionEvent ae) {
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource ();        
        //markerSubmitPanel.setVisible(menuItem.isSelected ());
        ManagerStore.getPanelManager().setVisible(
                            PanelManagerView.MARKER_VIEW_ID,
                            menuItem.isSelected());
      }
    };

      showHiddenChannelsAction = new DataViewerAction("Show Hidden Channels", "", KeyEvent.VK_H, KeyStroke.getKeyStroke(KeyEvent.VK_H, menuShortcutKeyMask), RDVIcon.hidden) {
      /** serialization version identifier */
      private static final long serialVersionUID = -2723464261568074033L;

      public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 				boolean selected = menuItem.isSelected();
 				channelListPanel.showHiddenChannels(selected);
 			}			
 		};
    
    hideEmptyTimeAction = new DataViewerAction("Hide time with no data", "", KeyEvent.VK_D) {
      /** serialization version identifier */
      private static final long serialVersionUID = -3123608144249355642L;

      public void actionPerformed(ActionEvent ae) {
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
        boolean selected = menuItem.isSelected();
        controlPanel.hideEmptyTime(selected);
      }     
    };    

 		fullScreenAction = new DataViewerAction("Full Screen", "", KeyEvent.VK_F, KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0)) {
      /** serialization version identifier */
      private static final long serialVersionUID = -6882310862616235602L;

      public void actionPerformed(ActionEvent ae) {
 				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)ae.getSource();
 		        if (menuItem.isSelected()) {
 		        	if (enterFullScreenMode()) {
 		        		menuItem.setSelected(true);
 		        	} else {
 		        		menuItem.setSelected(false);
 		        	}
 		        } else {
 		        	leaveFullScreenMode();
 		        	menuItem.setSelected(false);
 		        }
 			}			
 		};
 		
 		windowAction = new DataViewerAction("Window", "Window Menu", KeyEvent.VK_W);
 		
 		closeAllDataPanelsAction = new DataViewerAction("Close all data panels", "", KeyEvent.VK_C, RDVIcon.closeAll) {
      /** serialization version identifier */
      private static final long serialVersionUID = -8104876009869238037L;

      public void actionPerformed(ActionEvent ae) {
        ManagerStore.getPanelManager().closeAllDataPanels();				
 			}			
 		};

 		setPanelManagerAction = new DataViewerAction("Set panel manager", "", KeyEvent.VK_M);
 		
 		basicPanelManagerAction = new DataViewerAction("Basic", "", -1) {
 		 /** serialization version identifier */
      private static final long serialVersionUID = -5041880781915907401L;     

      public void actionPerformed(ActionEvent ae) {
        //PanelManager currMgr=ManagerStore.getPanelManager(); 
        //Collection<DataPanel> dataPanels = currMgr.getDataPanels();
        
        //currMgr.shutdown();
        ManagerStore.swapBasicPanelManager();
        
//        Iterator<DataPanel> panelIt=dataPanels.iterator();
//        while (panelIt.hasNext()){
//          newMgr.addDataPanel(panelIt.next());
//        }
//        newMgr.defaultLayout();
      }     
    };    
    
    dockingPanelManagerAction = new DataViewerAction("Docking", "", -1) {
      /** serialization version identifier */
      private static final long serialVersionUID = -3869194765239346883L;

      public void actionPerformed(ActionEvent ae) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //PanelManager currMgr=ManagerStore.getPanelManager(); 
//                Set<DataPanel> dataPanels = currMgr.getDataPanels();
//                
//                currMgr.shutdown();
                ManagerStore.swapDockingPanelManager();
                
//                Iterator<DataPanel> panelIt=dataPanels.iterator();
//                while (panelIt.hasNext()){
//                  newMgr.addDataPanel(panelIt.next());
//                }
//                newMgr.defaultLayout();
          }});
      }     
    };  
    
 		helpAction = new DataViewerAction("Help", "Help Menu", KeyEvent.VK_H);
    
    usersGuideAction = new DataViewerAction("RDV Help", "Open the RDV User's Guide", KeyEvent.VK_H, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)) {
      /** serialization version identifier */
      private static final long serialVersionUID = -2837190869008153291L;

      public void actionPerformed(ActionEvent ae) {
        try {
          URL usersGuideURL = new URL("http://nees.org/resources/1002/download/rdv2.0users_guide.pdf");
          //URL usersGuideURL = new URL("http://www.nees.org/research/dl_detail/rdv_2.1_users_guide/");
          DataViewer.browse(usersGuideURL);
        } catch (Exception e) {}        
      }
    };
    
    supportAction = new DataViewerAction("RDV Support", "Get support from NEES", KeyEvent.VK_S) {
      /** serialization version identifier */
      private static final long serialVersionUID = -6855670513381679226L;

      public void actionPerformed(ActionEvent ae) {
        try {
          URL supportURL = new URL("http://nees.org/tools/rdv/wiki");
          DataViewer.browse(supportURL);
        } catch (Exception e) {}        
      }
    };    
    
    /*
      releaseNotesAction = new DataViewerAction("Release Notes", "Open the RDV Release Notes", KeyEvent.VK_R) {
      // serialization version identifier 
      private static final long serialVersionUID = 7223639998298692494L;

      public void actionPerformed(ActionEvent ae) {
        try {
          URL releaseNotesURL = new URL("http://www.nees.org/research/dl_detail/rdv_release_notes/");
          DataViewer.browse(releaseNotesURL);
        } catch (Exception e) {}
      }
    };*/
    
 		aboutAction = new DataViewerAction("About RDV", "", KeyEvent.VK_A) {
      /** serialization version identifier */
      private static final long serialVersionUID = 3978467903181198979L;

      public void actionPerformed(ActionEvent ae) {
        showAboutDialog();
      }			
    };
 
  }
 	
  private void initMenuBar() {
    Application application = RDV.getInstance();
    ApplicationContext context = application.getContext();
    
    ResourceMap resourceMap = context.getResourceMap();
    String platform = resourceMap.getString("platform");
    boolean isMac = (platform != null  && platform.equals("osx"));
    
    ActionFactory actionFactory = ActionFactory.getInstance();
    
    Actions actions = Actions.getInstance();
    ActionMap actionMap = context.getActionMap(actions);
    
  	menuBar = new JMenuBar();
  
  	JMenuItem menuItem;
  		
 		JMenu fileMenu = new JMenu(fileAction);
  	fileMenu.getPopupMenu().setLightWeightPopupEnabled(false);
  	
 		menuItem = new JMenuItem(connectAction);
 		fileMenu.add(menuItem);
 
 		menuItem = new JMenuItem(disconnectAction);
 		fileMenu.add(menuItem);
 		
 		//fileMenu.addSeparator();
 		
 		//menuItem = new JMenuItem(loginAction);
 		//fileMenu.add(menuItem);
 		
 		//menuItem = new JMenuItem(logoutAction);
 		//fileMenu.add(menuItem);
 		
 		fileMenu.addSeparator();
    
 		JMenu loadSubMenu = new JMenu(loadAction);
 		loadSubMenu.getPopupMenu().setLightWeightPopupEnabled(false);
 		
    menuItem = new JMenuItem(loadFileAction);
    loadSubMenu.add(menuItem);
    
    menuItem = new JMenuItem(loadRBNBAction);
    loadSubMenu.add(menuItem);
    
    fileMenu.add(loadSubMenu);
    
    JMenu saveSubMenu = new JMenu(saveAction);
    saveSubMenu.getPopupMenu().setLightWeightPopupEnabled(false);
    
    menuItem = new JMenuItem(saveFileAction);
    saveSubMenu.add(menuItem);
    
    menuItem = new JMenuItem(saveRBNBAction);
    saveSubMenu.add(menuItem);
    
    fileMenu.add(saveSubMenu);
    
    fileMenu.addSeparator();

    final JMenuItem addLocalChanMenuItem = new JMenuItem(
        PROPERTY_REPO.getValue(ADD_CHANNEL_MENU_ITEM_KEY));
    addLocalChanMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new LocalChannelDialog(UIUtilities.getMainFrame());
      }
    });
    fileMenu.add(addLocalChanMenuItem);

    fileMenu.addSeparator();

    JMenu importSubMenu = new JMenu(importAction);
 		importSubMenu.getPopupMenu().setLightWeightPopupEnabled(false);
 		
    menuItem = new JMenuItem(actionFactory.getDataImportAction());
    importSubMenu.add(menuItem);
    
    menuItem = new JMenuItem(actionFactory.getOpenSeesDataImportAction());
    importSubMenu.add(menuItem);

    importSubMenu.addSeparator();
    
    importSubMenu.add(new JMenuItem(actionMap.get("importJPEGs")));

	  importSubMenu.addSeparator();
	  
	  menuItem = new JMenuItem(actionFactory.getNCBrowseImportAction()); //"Import NEESCentral data to Local");
	  importSubMenu.add(menuItem);
	  
	  menuItem = new JMenuItem(actionFactory.getNCWebSvcImportAction()); //"Import NEESCentral data to Local");
    importSubMenu.add(menuItem);

    fileMenu.add(importSubMenu);

    JMenu exportSubMenu = new JMenu(exportAction);
    exportSubMenu.getPopupMenu().setLightWeightPopupEnabled(false);
    
    exportMenuItem = new JMenuItem("Export data channels");//,
    exportMenuItem.addActionListener(this);
    
        //"Export data on the server to the local computer");
    exportSubMenu.add(exportMenuItem);
    
    menuItem = new JMenuItem(exportVideoAction);
    exportSubMenu.add(menuItem);
	  
	  exportSubMenu.addSeparator(); 
	  
	  menuItem = new JMenuItem(actionFactory.getNCExportAction());
	  exportSubMenu.add(menuItem);
    
    fileMenu.add(exportSubMenu);

 		fileMenu.addSeparator();
 		
    menuItem = new DataViewerCheckBoxMenuItem(actionFactory.getOfflineAction());
    fileMenu.add(menuItem);    
    
    if (!isMac) {
      menuItem = new JMenuItem(exitAction);
      fileMenu.add(menuItem);
    }
    
    fileMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent arg0) {}
      public void menuDeselected(MenuEvent arg0) {}
      public void menuSelected(MenuEvent arg0) {
        addLocalChanMenuItem.setEnabled(LocalChannelManager.
                              getInstance().getDoLocalMerge());
        
      }      
    }); 
    
  	menuBar.add(fileMenu);
  		
 		JMenu controlMenu = new JMenu(controlAction);
 		controlMenu.getPopupMenu().setLightWeightPopupEnabled(false);
 		
 		menuItem = new SelectableCheckBoxMenuItem(realTimeAction);
 		controlMenu.add(menuItem);
 		
 		menuItem = new SelectableCheckBoxMenuItem(playAction);
 		controlMenu.add(menuItem);
 		
 		menuItem = new SelectableCheckBoxMenuItem(pauseAction);
 		controlMenu.add(menuItem);   
 		
 		controlMenu.addSeparator();
 
 		menuItem = new JMenuItem(beginningAction);
 		controlMenu.add(menuItem);
 
 		menuItem = new JMenuItem(endAction);
 		controlMenu.add(menuItem);
 		
 		menuItem = new JMenuItem(gotoTimeAction);
 		controlMenu.add(menuItem);
 		
 		menuBar.add(controlMenu);
 		
 		controlMenu.addSeparator();
 		
 		menuItem = new JMenuItem(updateChannelListAction);
 		controlMenu.add(menuItem);
 		
 		controlMenu.addSeparator();
 		menuItem = new JMenuItem(optionsDialogAction);
 		controlMenu.add(menuItem);
 		
 		menuItem = new SelectableCheckBoxMenuItem(dropDataAction);
 		controlMenu.add(menuItem);
 		
 		menuItem = new SelectableCheckBoxMenuItem(disableLocalChannelsAction);
 		controlMenu.add(menuItem);
 		
 		controlMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent arg0) {}
      public void menuDeselected(MenuEvent arg0) {}
      public void menuSelected(MenuEvent arg0) {
        int state = rbnb.getState();
        realTimeAction.setSelected(state == Player.STATE_MONITORING);
        playAction.setSelected(state == Player.STATE_PLAYING);
        pauseAction.setSelected(state == Player.STATE_STOPPED);
        dropDataAction.setSelected(rbnb.getDropData());
        disableLocalChannelsAction.setSelected(
            !LocalChannelManager.getInstance().getDoLocalMerge());
      }      
    }); 
 		
 		JMenu viewMenu = new JMenu(viewAction);
 		viewMenu.getPopupMenu().setLightWeightPopupEnabled(false);
 		
 		final JCheckBoxMenuItem showChanListMenuItem = 
 		          new JCheckBoxMenuItem(showChannelListAction);
 		//menuItem.setSelected(true);
 		viewMenu.add(showChanListMenuItem);
    
//    menuItem = new JCheckBoxMenuItem(showMetadataPanelAction);
//    menuItem.setSelected(true);
//    viewMenu.add(menuItem);   
 		
 		final JCheckBoxMenuItem showControlMenuItem = 
 		            new JCheckBoxMenuItem(showControlPanelAction);
 		//menuItem.setSelected(true);
 		viewMenu.add(showControlMenuItem);
    
    final JCheckBoxMenuItem showAudioMenuItem = 
            new JCheckBoxMenuItem(showAudioPlayerPanelAction);
    //menuItem.setSelected(false);
    viewMenu.add(showAudioMenuItem);
 		
    final JCheckBoxMenuItem showMarkerMenuItem = 
                    new JCheckBoxMenuItem (showMarkerPanelAction);
    //menuItem.setSelected(true);
    viewMenu.add(showMarkerMenuItem);
 		
 		viewMenu.addSeparator();
    
    menuItem = new JCheckBoxMenuItem(showHiddenChannelsAction);
 		menuItem.setSelected(false);
 		viewMenu.add(menuItem);
    
    menuItem = new JCheckBoxMenuItem(hideEmptyTimeAction);
    menuItem.setSelected(false);
    viewMenu.add(menuItem);    

 		viewMenu.addSeparator();
 		
 		menuItem = new JCheckBoxMenuItem(fullScreenAction);
 		menuItem.setSelected(false);
 		viewMenu.add(menuItem);
 		
 		viewMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent arg0) {}
      public void menuDeselected(MenuEvent arg0) {}
      public void menuSelected(MenuEvent arg0) {
        showChanListMenuItem.setSelected(ManagerStore.getPanelManager().isVisible(
                                        PanelManagerView.CHANNEL_LIST_VIEW_ID));
        showAudioMenuItem.setSelected(ManagerStore.getPanelManager().isVisible(
                                        PanelManagerView.AUDIO_PLAYER_VIEW_ID));
        showControlMenuItem.setSelected(ManagerStore.getPanelManager().isVisible(
                                        PanelManagerView.CONTROL_VIEW_ID));
        showMarkerMenuItem.setSelected(ManagerStore.getPanelManager().isVisible(
                                        PanelManagerView.MARKER_VIEW_ID));
      }      
    }); 
 		
 		menuBar.add(viewMenu);
 		
 		final JMenu windowMenu = new JMenu(windowAction);
 		windowMenu.getPopupMenu().setLightWeightPopupEnabled(false);
 		
    menuBar.add(windowMenu);
    
 		JMenu helpMenu = new JMenu(helpAction);
    helpMenu.getPopupMenu().setLightWeightPopupEnabled(false);
    
    menuItem = new JMenuItem(usersGuideAction);
    helpMenu.add(menuItem);
    
    menuItem = new JMenuItem(supportAction);
    helpMenu.add(menuItem);    

    //menuItem = new JMenuItem(releaseNotesAction);
    //helpMenu.add(menuItem);
    
    if (!isMac) {
      helpMenu.addSeparator();
      
      menuItem = new JMenuItem(aboutAction);
      helpMenu.add(menuItem);
    }
    
  	menuBar.add(helpMenu);

    menuBar.add(Box.createHorizontalGlue());
    throbberStop = RDVIcon.throbber;
    throbberAnim = RDVIcon.throbberAnim;
    throbber = new JLabel(throbberStop);
    throbber.setBorder(new EmptyBorder(0,0,0,4));
    menuBar.add(throbber, BorderLayout.EAST);
    
    if (isMac) {
      registerMacOSXEvents();
    }
		
		frame.setJMenuBar(menuBar);
		
		// window menu is dynamically created now
		windowMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent arg0) {}
      public void menuDeselected(MenuEvent arg0) {}
      public void menuSelected(MenuEvent arg0) {
        JMenuItem menuItem;
        windowMenu.removeAll();
        JMenu addPanelMenu = new JMenu("Add panel");
        addPanelMenu.getPopupMenu().setLightWeightPopupEnabled(false);
        
        List<Extension> extensions = ExtensionManager.getInstance().getExtensions();
        for (final Extension extension : extensions) {
          Action action = new DataViewerAction(extension.getName(), "", KeyEvent.VK_J) {
            /** serialization version identifier */
            private static final long serialVersionUID = 36998228704476723L;

            public void actionPerformed(ActionEvent ae) {
              try {
                DataPanel panel = ExtensionManager.getInstance()
                                          .createDataPanel(extension);
                int viewId=ManagerStore.getPanelManager().addDataPanel(panel);
                ManagerStore.getPanelManager().showView(viewId);
              } catch (Exception e) {
                log.error("Unable to open data panel provided by extension " + extension.getName() + " (" + extension.getID() + ").");
                e.printStackTrace();
              }
            }     
          };
          
            menuItem = new JMenuItem(action);
            addPanelMenu.add(menuItem);
        }
        windowMenu.add(addPanelMenu);
        windowMenu.addSeparator();
        
        menuItem = new JMenuItem(closeAllDataPanelsAction);
        windowMenu.add(menuItem);
          
        windowMenu.addSeparator(); 
        JMenu panelManagerSubMenu = new JMenu(setPanelManagerAction);
        panelManagerSubMenu.getPopupMenu().setLightWeightPopupEnabled(false);
        
        ButtonGroup panelManagerGroup = new ButtonGroup();
        
        final JMenuItem basicPanelManagerMenuItem = new JRadioButtonMenuItem(basicPanelManagerAction);
        basicPanelManagerMenuItem.setSelected(true);
        panelManagerSubMenu.add(basicPanelManagerMenuItem);
        panelManagerGroup.add(basicPanelManagerMenuItem);
        
        final JMenuItem dockingPanelManagerMenuItem = new JRadioButtonMenuItem(
                                                          dockingPanelManagerAction);
        dockingPanelManagerMenuItem.setSelected(true);
        panelManagerSubMenu.add(dockingPanelManagerMenuItem);
        panelManagerGroup.add(dockingPanelManagerMenuItem);
        windowMenu.add(panelManagerSubMenu);

        panelManagerSubMenu.addMenuListener(new MenuListener() {
          public void menuCanceled(MenuEvent arg0) {}
          public void menuDeselected(MenuEvent arg0) {}
          public void menuSelected(MenuEvent arg0) {
            if (ManagerStore.getPanelManager() instanceof DockingDataPanelManager){
              dockingPanelManagerMenuItem.setSelected(true);
            } else {
              basicPanelManagerMenuItem.setSelected(true);
            }
          }      
        });
        
        ManagerStore.getPanelManager().buildMenu(windowMenu);
      }      
    });
		
		
		fileMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent arg0) {}
      public void menuDeselected(MenuEvent arg0) {}
      public void menuSelected(MenuEvent arg0) {
        if (AuthenticationManager.getInstance().getAuthentication() == null) {
          
          //loginAction.setEnabled(true);
          //logoutAction.setEnabled(false);
        } else {
          //loginAction.setEnabled(false);
          //logoutAction.setEnabled(true);
        }
      }      
    });
	}
  	
  public void actionPerformed(ActionEvent e)
  {
    if(e.getSource()==exportMenuItem){
      Action a=ActionFactory.getInstance().getDataExportAction();
      a.actionPerformed(e);
    }
    
  }
  
	/**
	 * Register event handlers for Mac OS X specific menu items.
	 */
  private void registerMacOSXEvents() {
    try {
      Application rdv = RDV.getInstance();
      OSXAdapter.setQuitHandler(rdv, Application.class.getDeclaredMethod("exit", (Class[])null));
      OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("showAboutDialog", (Class[])null));
    } catch (Exception e) {
      log.warn("Unable to register Mac OS X events.");
      e.printStackTrace();
      return;
    }
    
    log.info("Registered Mac OS X events.");
  }
  
//  /**
//   * Hide the left part of the main split pane if both it's components are
//   * visible. If either of them are visible, show it.
//   */
//  private void layoutSplitPane() {
//    boolean visible = channelListPanel.isVisible() || metadataPanel.isVisible();
//    
//    if (leftPanel.isVisible() != visible) {
//      leftPanel.setVisible(visible);
//      splitPane.resetToPreferredSizes();
//    }
//  }
	
 	private boolean enterFullScreenMode() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();
		for (int i=0; i<devices.length; i++) {
			GraphicsDevice device = devices[i];
			if (device.isFullScreenSupported() && device.getFullScreenWindow() == null) {
				log.info("Switching to full screen mode.");
		
				frame.setVisible(false);
		
				try {
					device.setFullScreenWindow(frame);
				} catch (InternalError e) {
					log.error("Failed to switch to full screen exclusive mode.");
					e.printStackTrace();
					
					frame.setVisible(true);
					return false;
				}
		
				frame.dispose();
				frame.setUndecorated(true);
				frame.setVisible(true);
				frame.requestFocusInWindow();
				
				return true;
			}
		}
		
		log.warn("No screens available or full screen exclusive mode is unsupported on your platform.");
    
    postError("Full screen mode is not supported on your platform.");
		
		return false;
 	}
 	
 	private void leaveFullScreenMode() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();
		for (int i=0; i<devices.length; i++) {
			GraphicsDevice device = devices[i];
			if (device.isFullScreenSupported() && device.getFullScreenWindow() == frame) {
				log.info("Leaving full screen mode.");
	
				frame.setVisible(false);
				device.setFullScreenWindow(null);
				frame.dispose();
				frame.setUndecorated(false);
				frame.setVisible(true);
				
				break;
			}
		}
	}
 	
	public void postError(String errorMessage) {
		JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void postStatus(String statusMessage) {
		JOptionPane.showMessageDialog(this, statusMessage, "Status", JOptionPane.INFORMATION_MESSAGE);
	}
	
  /**
   * Displays the about dialog
   */
  public void showAboutDialog() {
    if (aboutDialog == null) {
      aboutDialog = new AboutDialog(frame);
      aboutDialog.setConsole(consoleDialog);
    } else {
      aboutDialog.setVisible(true);
    }
  }

  /**
   * Displays the options dialog
   */
  public void showOptionsDialog() {
    if (optionsDialog == null) {
      optionsDialog = new OptionsDialog(frame);
    }
   
    optionsDialog.setVisible(true);
  }
  
  public void showExportVideoDialog() {
    List<String> channels = channelListPanel.getSelectedChannels();
    
    // remove non-data channels
    for (int i=channels.size()-1; i>=0; i--) {
      Channel channel = RBNBController.getInstance().getChannel(channels.get(i));
      String mime = channel.getMetadata("mime");
      if (!mime.equals("image/jpeg")) {
        channels.remove(i);
      }
    }
    
    // don't bring up the dialog if there are no channels specified
    if (channels.isEmpty()) {
      JOptionPane.showMessageDialog(null,
          "There are no video channels selected.", "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    new ExportVideoDialog(frame, rbnb, channels);
  }
  
  /**
   * Gets a list of channels selected in the channel list.
   * 
   * @return  a list of selected channels
   */
  public List<String> getSelectedChannels() {
    return channelListPanel.getSelectedChannels();
  }
  
  /**
   * Gets if hidden channels are visible in the UI.
   * 
   * @return  true if hidden channels are visible, false otherwise
   */
  public boolean isHiddenChannelsVisible() {
    return channelListPanel.isShowingHiddenChannels();
  }

 	public void connecting() {
   setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
   
   if (busyDialog != null) {
    busyDialog.close();
    busyDialog = null;
   }   
    
 	 busyDialog = new BusyDialog(frame);
   busyDialog.setCancelActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent arg0) {
       rbnb.cancelConnect();
     }
   });
   busyDialog.start();

   startThrobber();
	}

	public void connected() {
    setCursor(null);
    
    if (busyDialog != null) {
      busyDialog.close();
      busyDialog = null;
    }

    stopThrobber();
	}

	public void connectionFailed() {
    setCursor(null);

    if (busyDialog != null) {
      busyDialog.close();
      busyDialog = null;
    }

    stopThrobber();
	}
    
  public void postState(int newState, int oldState) {
    if (newState == Player.STATE_DISCONNECTED) {
      frame.setTitle("RDV");
      
      controlAction.setEnabled(false);
      disconnectAction.setEnabled(false);
      saveAction.setEnabled(false);
      importAction.setEnabled(false);
      exportAction.setEnabled(false);

      controlPanel.setEnabled(false);
      markerSubmitPanel.setEnabled(false);
      
      ActionFactory.getInstance().getOfflineAction().setSelected(false);
    } else if (oldState == Player.STATE_DISCONNECTED) {
      frame.setTitle(rbnb.getServerName() + " - RDV");
      
      controlAction.setEnabled(true);
      disconnectAction.setEnabled(true);
      
      LocalServer localServer = LocalServer.getInstance();
      boolean offline = rbnb.getRBNBHostName().equals(localServer.getHost()) &&
                        rbnb.getRBNBPortNumber() == localServer.getPort();
      saveAction.setEnabled(!offline);
      importAction.setEnabled(offline);
      exportAction.setEnabled(true);
 
      controlPanel.setEnabled(true);
      markerSubmitPanel.setEnabled(true);
    }

    if (newState == Player.STATE_LOADING || newState == Player.STATE_PLAYING || newState == Player.STATE_MONITORING) {
      startThrobber(); 
    } else if (oldState == Player.STATE_LOADING || oldState == Player.STATE_PLAYING || oldState == Player.STATE_MONITORING){
      stopThrobber();
    }

    if (newState == Player.STATE_LOADING) {
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      loadingDialog = new LoadingDialog(frame);
      
      new Thread() {
        public void run() {
          synchronized (loadingMonitor) {
            if (loadingDialog == null) {
              return;
            }
            
            try { loadingMonitor.wait(1000); } catch (InterruptedException e) {}

            if (loadingDialog != null) {
              loadingDialog.setVisible(true);
              loadingDialog.start();
            }
          }
        }
      }.start();
    } else if (oldState == Player.STATE_LOADING) {
      setCursor(null);

      synchronized (loadingMonitor) {
        loadingMonitor.notify();

        if (loadingDialog != null) {
          loadingDialog.close();
          loadingDialog = null;
        }
      }
    }
  }
  
  private void startThrobber() {
    throbber.setIcon(throbberAnim);
  }
  
  private void stopThrobber() {
    throbber.setIcon(throbberStop);
  };

}
