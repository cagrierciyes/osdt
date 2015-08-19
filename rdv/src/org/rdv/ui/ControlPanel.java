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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/ui/ControlPanel.java $
 * $Revision: 1293 $
 * $Date: 2008-11-26 10:14:53 -0500 (Wed, 26 Nov 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Resource;
import org.jdesktop.application.ResourceMap;
import org.rdv.DataViewer;
import org.rdv.RDV;
import org.rdv.TimeScale;
import org.rdv.TimeScalesChangeListener;
import org.rdv.rbnb.EventMarker;
import org.rdv.rbnb.EventMarkerListener;
import org.rdv.rbnb.MetadataListener;
import org.rdv.rbnb.PlaybackRateListener;
import org.rdv.rbnb.Player;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBHelper;
import org.rdv.rbnb.RBNBTime;
import org.rdv.rbnb.StateListener;
import org.rdv.rbnb.SubscriptionListener;
import org.rdv.rbnb.TimeListener;
import org.rdv.rbnb.TimeRange;
import org.rdv.rbnb.TimeScaleListener;

import com.rbnb.sapi.ChannelTree;

/**
 * The UI to act as the control panel for data playback.
 * 
 * @author Jason P. Hanley
 * @author Lawrence J. Miller
 */
public class ControlPanel extends JPanel implements TimeListener, StateListener, 
  SubscriptionListener, MetadataListener, PlaybackRateListener, TimeScaleListener,
  TimeAdjustmentListener, EventMarkerListener, TimeScalesChangeListener
{

  /** serialization version identifier */
  private static final long serialVersionUID = 2727527118691092710L;

  /** the logger for this class */
  private static Log log = LogFactory.getLog(ControlPanel.class.getName());

  /** the RBNB controller */
  private final RBNBController rbnbController;
  
  /** Indicate if we are hiding empty time regions */
  private boolean hideEmptyTime;

  /** the button to go to the beginning of data */
  private JButton beginButton;
  
  /** the button to pause real time and play modes */
  private JButton pauseButton;
  
  /** the button to enter real time mode */
  private JButton rtButton;
  
  /** the play button */
  private JButton playButton;
  
  /** the button to go to the end of data */
  private JButton endButton;
	
  /** the spinner to select the playback rate */
  private JSpinner playbackRateSpinner;	
  
  /** the combo box to select the time scale */
  private JComboBox timeScaleComboBox;
  
  /** the button to display and update the location */
  private JButton locationButton;
  
  /** the zoomed time slider */
  private TimeSlider zoomTimeSlider;
  
  /** the label displaying the minimum of the zoomed time slider */
  private JLabel zoomMinimumLabel;
  
  /** the label displaying the range of the zoomed time slider */
  private JLabel zoomRangeLabel;
  
  /** the label displaying the maximum of the zoomed time slider */
  private JLabel zoomMaximumLabel;
  
  /** the global time slider */
  private TimeSlider globalTimeSlider;
  
  /** the title for the invalid time scale error dialog */
  @Resource private String timeScaleErrorTitle;
  
  /** the message for the time scale parse error dialog */
  @Resource private String timeScaleParseErrorMessage;
  
  /** the message for the non positive time scale error dialog */
  @Resource private String timeScaleNonPositiveErrorMessage;

 	
  /** Predefined playback rates */
 	private static final Double playbackRates[] = {1e-3, 2e-3, 5e-3, 1e-2, 2e-2,
    5e-2, 1e-1, 2e-1, 5e-1, 1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0, 200.0,
    500.0, 1000.0}; 

  /** the latest channel tree */
  private ChannelTree ctree;

  
  /**
   * Construct a control panel to control data playback.
   */
  public ControlPanel() {
    super();
    
    this.rbnbController = RBNBController.getInstance();
    
    hideEmptyTime = false;
    
    initPanel();
    TimeScale.addListener(this);
    // inject fields from properties file
    RDV.getInstance().getContext().getResourceMap().injectFields(this);
    
    double location = rbnbController.getLocation();
    globalTimeSlider.setValues(location, 0, location, 0, location);
    
    rbnbController.getMarkerManager().addMarkerListener(this);
	}
  
  /**
   * Setup the UI.
   */
	private void initPanel() {
    setLayout(new BorderLayout());
    
		GridBagConstraints c = new GridBagConstraints();
    
    JPanel container = new JPanel();
    container.setLayout(new GridBagLayout());
    
    Box firstRowPanel = new Box(BoxLayout.LINE_AXIS);
    
    beginButton = new JButton();
    beginButton.setName("beginButton");
    beginButton.setIcon(RDVIcon.begin);
    beginButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setLocationBegin();
      }
    });
    firstRowPanel.add(beginButton);    
    
    pauseButton = new JButton();
    pauseButton.setName("pauseButton");
    pauseButton.setIcon(RDVIcon.pause);
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          rbnbController.pause();
      }
    });
    firstRowPanel.add(pauseButton);
    
    rtButton = new JButton();
    rtButton.setName("rtButton");
    rtButton.setIcon(RDVIcon.rt);
    rtButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          rbnbController.monitor();
      }
    });
    firstRowPanel.add(rtButton);
    
    playButton = new JButton();
    playButton.setName("playButton");
    playButton.setIcon(RDVIcon.play);
    playButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          rbnbController.play();
      }
    });
    firstRowPanel.add(playButton);
    
    endButton = new JButton();
    endButton.setName("endButton");
    endButton.setIcon(RDVIcon.end);
    endButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setLocationEnd();
      }
    });
    firstRowPanel.add(endButton);    
    
    firstRowPanel.add(Box.createHorizontalStrut(8));
    
    SpinnerListModel playbackRateModel = new SpinnerListModel(playbackRates);
    playbackRateSpinner = new JSpinner(playbackRateModel);
    playbackRateSpinner.setName("playbackRateSpinner");
    JSpinner.ListEditor playbackRateEditor = new JSpinner.ListEditor(playbackRateSpinner);
    playbackRateEditor.getTextField().setEditable(false);
    playbackRateSpinner.setEditor(playbackRateEditor);
    playbackRateSpinner.setPreferredSize(new Dimension(80, playbackRateSpinner.getPreferredSize().height));
    playbackRateSpinner.setMinimumSize(playbackRateSpinner.getPreferredSize());
    playbackRateSpinner.setMaximumSize(playbackRateSpinner.getPreferredSize());
    playbackRateSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        playbackRateChanged();
      }      
    });
    firstRowPanel.add(playbackRateSpinner);

    firstRowPanel.add(Box.createHorizontalStrut(8));
    
    timeScaleComboBox = new JComboBox();
    timeScaleComboBox.setName("timeScaleComboBox");
    timeScaleComboBox.setEditable(true);
    timeScaleComboBox.setPreferredSize(new Dimension(96, timeScaleComboBox.getPreferredSize().height));
    timeScaleComboBox.setMinimumSize(timeScaleComboBox.getPreferredSize());
    timeScaleComboBox.setMaximumSize(timeScaleComboBox.getPreferredSize());
    
    Double[] globalTimeScales=TimeScale.getGlobalTimeScales();
    for (int i=0; i<globalTimeScales.length; i++) {
      timeScaleComboBox.addItem(DataViewer.formatSeconds(globalTimeScales[i]));
    }
    
    timeScaleComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          timeScaleChange();
        }
      }
    });
    
    firstRowPanel.add(timeScaleComboBox);

    firstRowPanel.add(Box.createHorizontalGlue());
    
    locationButton = new JButton();
    locationButton.setName("locationButton");
    locationButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        TimeRange timeRange = RBNBHelper.getChannelsTimeRange();
        double time = DateTimeDialog.showDialog(ControlPanel.this, rbnbController.getLocation(), timeRange.start, timeRange.end);
        if (time >= 0) {
          rbnbController.setLocation(time);
        }
      }
    });
    firstRowPanel.add(locationButton);
    
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 1;
    c.ipadx = 0;
    c.ipady = 0;
    c.insets = new java.awt.Insets(8,8,8,8);
    c.anchor = GridBagConstraints.NORTHWEST;    
    container.add(firstRowPanel, c);    
		
    zoomTimeSlider = new TimeSlider();
    zoomTimeSlider.setRangeChangeable(false);
    zoomTimeSlider.addTimeAdjustmentListener(this);
    c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
		c.insets = new java.awt.Insets(0,8,0,8);
		c.anchor = GridBagConstraints.NORTHWEST;
    container.add(zoomTimeSlider, c);
    
    JPanel zoomTimePanel = new JPanel();
    zoomTimePanel.setLayout(new BorderLayout());
    
    zoomMinimumLabel = new JLabel();
    zoomMinimumLabel.setName("zoomMinimumLabel");
    zoomTimePanel.add(zoomMinimumLabel, BorderLayout.WEST);
    
    zoomRangeLabel = new JLabel();
    zoomRangeLabel.setName("zoomRangeLabel");
    zoomRangeLabel.setHorizontalAlignment(JLabel.CENTER);
    zoomTimePanel.add(zoomRangeLabel, BorderLayout.CENTER);
    
    zoomMaximumLabel = new JLabel();
    zoomMaximumLabel.setName("zoomMaximumLabel");
    zoomTimePanel.add(zoomMaximumLabel, BorderLayout.EAST);

    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 1;
    c.ipadx = 0;
    c.ipady = 0;
    c.insets = new java.awt.Insets(8,8,0,8);
    c.anchor = GridBagConstraints.NORTHWEST;
    container.add(zoomTimePanel, c);    
    
    globalTimeSlider = new TimeSlider();
    globalTimeSlider.setValueChangeable(false);
    globalTimeSlider.addTimeAdjustmentListener(this);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.weighty = 1;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 1;
    c.ipadx = 0;
    c.ipady = 0;
    c.insets = new java.awt.Insets(8,8,8,8);
    c.anchor = GridBagConstraints.NORTHWEST;
    container.add(globalTimeSlider, c);
    
    add(container, BorderLayout.CENTER);        
		
		log.info("Initialized control panel.");
	}
	
  /**
   * Called when the channel metadata is updated.
   */
	public void channelTreeUpdated(ChannelTree ctree) {
		this.ctree = ctree;
		
		updateTimeBoundaries();
	}
	
  /**
   * Update the boundaries of the time sliders based on the subscribed channel
   * bounds and the event marker bounds.
   */
	private void updateTimeBoundaries() {
    // We haven't got the metadata channel tree yet
    if (ctree == null) {
      return;
    }
    
    if (!SwingUtilities.isEventDispatchThread()) {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            updateTimeBoundaries();
          }
        });
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
      return;
    }

    double startTime = -1;
    double endTime = -1;
    
    boolean hasSubscribedChannels = rbnbController.hasSubscribedChannels();

    // get the time bounds for all channels
    Iterator<?> it = ctree.iterator();
    while (it.hasNext()) {
      ChannelTree.Node node = (ChannelTree.Node)it.next();
      ChannelTree.NodeTypeEnum type = node.getType();
      if (type != ChannelTree.CHANNEL) {
        continue;
      }
      
      String channelName = node.getFullName();
      if (rbnbController.isSubscribed(channelName) || !hasSubscribedChannels) {
        double channelStart = node.getStart();
        double channelDuration = node.getDuration();
        double channelEnd = channelStart+channelDuration;
        //log.debug("chan: "+channelName+" start: "+DataViewer.formatDate(channelStart)+
        //          " stop: "+DataViewer.formatDate(channelEnd));
        if (startTime == -1 || channelStart < startTime) {
          startTime = channelStart;
        }
        if (endTime == -1 || channelEnd > endTime) {
          endTime = channelEnd;
        }
      }
    }
    
    if (hideEmptyTime) {
      List<TimeRange> timeRanges = new ArrayList<TimeRange>();
      
      double markerStartTime = -1;
  
      // get the time bounds for all markers
      List<EventMarker> markers = rbnbController.getMarkerManager().getMarkers();
      for (EventMarker marker : markers) {
        double markerTime = Double.parseDouble(marker.getProperty("timestamp"));
        if (startTime == -1 || markerTime < startTime) {
          startTime = markerTime;
        }
        if (endTime == -1 || markerTime > endTime) {
          endTime = markerTime;
        }
        
        String type = marker.getProperty("type");
        if (type.compareToIgnoreCase("start") == 0 && markerStartTime == -1) {
          markerStartTime = markerTime;
        } else if (type.compareToIgnoreCase("stop") == 0 && markerStartTime != -1) {
          timeRanges.add(new TimeRange(markerStartTime, markerTime));
          markerStartTime = -1;
        }
      }
      
      // add time range for ongoing event
      if (markerStartTime != -1) {
        timeRanges.add(new TimeRange(markerStartTime, Double.MAX_VALUE));
      }
      
      zoomTimeSlider.setTimeRanges(timeRanges);
      globalTimeSlider.setTimeRanges(timeRanges);
    }

    if (startTime == -1) {
      return;
    }
    
    double state = rbnbController.getState();
    double location = rbnbController.getLocation();
    if (state == Player.STATE_MONITORING && location > endTime) {
      endTime = location;
    }

    globalTimeSlider.setValues(startTime, endTime);
    
    // reset the selected time range if it gets stuck together
    double start = globalTimeSlider.getStart();
    double end = globalTimeSlider.getEnd();
    if (start == end) {
      if (start == globalTimeSlider.getMinimum()) {
        globalTimeSlider.setEnd(globalTimeSlider.getMaximum());
      } else if (start == globalTimeSlider.getMaximum()) {
        globalTimeSlider.setStart(globalTimeSlider.getMinimum());
      }
    }
  }
		
  /**
   * Set the time to the minimum of the zoom time slider.
   */
	public void setLocationBegin() {
		rbnbController.setLocation(zoomTimeSlider.getMinimum());
	}
	
  /**
   * Set the time to the maximum of the zoom time sldier.
   */
	public void setLocationEnd() {
		rbnbController.setLocation(zoomTimeSlider.getMaximum());
	}

  /**
   * Called when the playback rate changes. Update the UI display. 
   * 
   * @param playbackRate  the current playback rate
   */
  public void playbackRateChanged(double playbackRate) {
    playbackRateSpinner.setValue(playbackRate);
  }    
  
  /**
   * Called when the playback rate is changed in the UI.
   */
  private void playbackRateChanged() {
    double playbackRate = (Double)playbackRateSpinner.getValue();
    rbnbController.setPlaybackRate(playbackRate);
  }
  
  /**
   * Called when the time scale changes. Sets the displayed time scale in the
   * UI.
   * 
   * @param timeScale  the time scale
   */
  public void globalTimeScaleChanged(double timeScale) {
    int index = TimeScale.getGlobalTimeScaleIndex(timeScale);
    if (index<0.0) {
      // time scale not found
      index = TimeScale.addGlobalTimeScale(timeScale);
      
      //timeScaleComboBox.setSelectedItem(DataViewer.formatSeconds(timeScale));
    } 
    timeScaleComboBox.setSelectedIndex(index);
  }
  
  /**
   * Called when the time scale changes in the UI combo box. This sets the
   * time scale in the rbnb controller.
   */
  private void timeScaleChange() {
    double newTimeScale;
    
    int value = timeScaleComboBox.getSelectedIndex();
    if (value == -1) {
      String timeScaleString = (String)timeScaleComboBox.getSelectedItem();
      
      try {
        newTimeScale = DataViewer.parseTime(timeScaleString);
      } catch (IllegalArgumentException e) {
        JOptionPane.showMessageDialog(this,
            timeScaleParseErrorMessage,
            timeScaleErrorTitle,
            JOptionPane.ERROR_MESSAGE);
        timeScaleComboBox.setSelectedItem(DataViewer.formatSeconds(
            rbnbController.getTimeScale()));
        return;
      }
      
      if (newTimeScale <= 0) {
        JOptionPane.showMessageDialog(this,
            timeScaleNonPositiveErrorMessage,
            timeScaleErrorTitle,
            JOptionPane.ERROR_MESSAGE);
        timeScaleComboBox.setSelectedItem(DataViewer.formatSeconds(
            rbnbController.getTimeScale()));
        return;
      }
      
      // we may already have this time scale
      int timeScaleIndex = TimeScale.getGlobalTimeScaleIndex(newTimeScale);      
      if (timeScaleIndex >= 0) {
        timeScaleComboBox.setSelectedIndex(timeScaleIndex);
      } //else {
//        
//        timeScaleComboBox.setSelectedItem(DataViewer.formatSeconds(timeScale));
//      }
    } else {
      newTimeScale = TimeScale.getGlobalTimeScaleAt(value);
    }

    if(newTimeScale != rbnbController.getTimeScale()){
      rbnbController.setGlobalTimeScale(newTimeScale);
    }
  }
  
  /**
   * Called when the time changes in the UI. This sets the time in the rbnb
   * controller.
   * 
   * @param event  the time event
   */
  public void timeChanged(TimeEvent event) {
    if (event.getSource() == zoomTimeSlider && !zoomTimeSlider.isAdjusting()) {
      rbnbController.setLocation(zoomTimeSlider.getValue());
    }
  }

  /**
   * Called when the time range changes in the UI. This sets up the bounds for
   * the zoom time slider and updates UI displays. If the current time is
   * outside the new range, the time will be changes via the rbnb controller to
   * be within this range.
   * 
   * @param event  the time event
   */
  public void rangeChanged(TimeEvent event) {
    if (event.getSource() == globalTimeSlider) {
      double start = globalTimeSlider.getStart();
      double end = globalTimeSlider.getEnd();
      
      zoomTimeSlider.setValues(start, end);
      
      zoomMinimumLabel.setText(DataViewer.formatDate(start));
      zoomRangeLabel.setText(DataViewer.formatSeconds(end-start));
      zoomMaximumLabel.setText(DataViewer.formatDate(end));
      
      double location = rbnbController.getLocation();
      double state = rbnbController.getState();
      log.debug("setting range to start: "+RBNBTime.formatISO(start)+" end: "+
          RBNBTime.formatISO(end)+" location: "+RBNBTime.formatISO(location));
      if (state != Player.STATE_MONITORING && 
          state != Player.STATE_PLAYING) {
        if (location < start) {
          log.debug("out of range - setting to start");
          rbnbController.setLocation(start);
        } else if (location > end) {
          log.debug("out of range - setting to end");
          rbnbController.setLocation(end);
        }
      }
    }
  }  

  /**
   * Called when the bounds change in the UI.
   * 
   * @param event  the time event
   */
  public void boundsChanged(TimeEvent event) {}

	
	// Player Time Methods
	
  /**
   * Called when the time changes. This updates the slider and display
   * components in the UI. It will also adjust the bounds and range if needed.
   * 
   * @param time  the new time
   */
  public void postTime(final double time) {
    if (!SwingUtilities.isEventDispatchThread()) {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            postTime(time);
          }
        });
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
      return;
    }    
    log.debug("control panel posting time to: "+RBNBTime.formatISO(time));
    if (time < globalTimeSlider.getMinimum()) {
      globalTimeSlider.setMinimum(time);
    } else if (time > globalTimeSlider.getMaximum()) {
      globalTimeSlider.setMaximum(time);
    }

    if (time < globalTimeSlider.getStart()) {
      globalTimeSlider.setStart(time);
    } else if (time > globalTimeSlider.getEnd()) {
      globalTimeSlider.setEnd(time);
    }
    
    if (!zoomTimeSlider.isValueAdjusting()) {
      zoomTimeSlider.removeTimeAdjustmentListener(this);
      zoomTimeSlider.setValue(time);
      zoomTimeSlider.addTimeAdjustmentListener(this);
    }
    
    if (rbnbController.getState() == Player.STATE_PLAYING && !globalTimeSlider.isTimeValid(time)) {
      double newTime = globalTimeSlider.getNextValidTime(time);
      if (newTime == -1) {
        newTime = globalTimeSlider.getActualMaximum();
      }
      zoomTimeSlider.setValue(newTime);
      if (newTime > time) {
        rbnbController.play();
      }
    }
    
    globalTimeSlider.setValue(time);

    locationButton.setText(DataViewer.formatDate(time));    
	}

	
	// Player State Methods

  /**
   * Called when the state of the rbnb controller changes. This updates various
   * UI elements depending on the state.
   * 
   * @param newState  the new controller state
   * @param oldState  the old controller state
   */
  public void postState(int newState, int oldState) {
    ResourceMap resourceMap = RDV.getInstance().getContext().getResourceMap();
      
    if (newState == Player.STATE_MONITORING) {
      //rtButton.setName("pauseButton");
      //resourceMap.injectComponent(rtButton);
      rtButton.setSelected(true);
      
      playbackRateSpinner.setEnabled(false);
    } else if (oldState == Player.STATE_MONITORING) {
      //rtButton.setName("rtButton");
      //resourceMap.injectComponent(rtButton);
      rtButton.setSelected(false);
      
      playbackRateSpinner.setEnabled(true);
    }
    
    if (newState == Player.STATE_PLAYING) {
      //playButton.setName("pauseButton");
      //resourceMap.injectComponent(playButton);
      playButton.setSelected(true);
      playbackRateSpinner.setEnabled(true);
    } else if (oldState == Player.STATE_PLAYING) {
      //playButton.setName("playButton");
      //resourceMap.injectComponent(playButton);
      playButton.setSelected(false);
    }    

    if(newState == Player.STATE_STOPPED){
      pauseButton.setSelected(true);
      playbackRateSpinner.setEnabled(true);
    }else if (oldState == Player.STATE_STOPPED) {
      pauseButton.setSelected(false);
    }
    
    if (oldState == Player.STATE_DISCONNECTED) {
      updateTimeBoundaries();
    }
  }
  
  /**
   * Set whether or not this component is enabled. If it is disabled, the UI
   * will not respond to user input. 
   * 
   * @param enabled  true if the component should be enabled, false otherwise
   */
  public void setEnabled(boolean enabled) {
    if (isEnabled() == enabled) {
      return;
    }
    
    super.setEnabled(enabled);
    
    beginButton.setEnabled(enabled);
    pauseButton.setEnabled(enabled);
    rtButton.setEnabled(enabled);
    playButton.setEnabled(enabled);
    endButton.setEnabled(enabled);
    
    if (enabled && rbnbController.getState() != Player.STATE_MONITORING) {
      playbackRateSpinner.setEnabled(true);
    } else {
      playbackRateSpinner.setEnabled(false);
    }

    timeScaleComboBox.setEnabled(enabled);
    
    locationButton.setEnabled(enabled);
    
    zoomTimeSlider.setEnabled(enabled);
    zoomMinimumLabel.setEnabled(enabled);
    zoomRangeLabel.setEnabled(enabled);
    zoomMaximumLabel.setEnabled(enabled);
    
    globalTimeSlider.setEnabled(enabled);
  }


	// Player Subscription Methods
	
  /**
   * Called when a channel is subscribed to. This updates the time bounds.
   * 
   * @param channelName  the channel being subscribed to
   */
	public void channelSubscribed(String channelName) {
		updateTimeBoundaries();
	}

  /**
   * Called when a channel is unsubscribed to.
   * 
   * @param channelName  the channel being unsubscribed from
   */
	public void channelUnsubscribed(String channelName) {
		updateTimeBoundaries();
	}

  
  // Marker methods
  
  /**
   * Called when there is a new event marker. This adds the event marker to the
   * UI and updates the time bounds.
   * 
   * @param marker  the new event marker
   */
  public void eventMarkerAdded(EventMarker marker) {
    zoomTimeSlider.addMarker(marker);
    globalTimeSlider.addMarker(marker);
    
    updateTimeBoundaries();
  }
  
  /**
   * Called when all the event markers are removed 
   */
  public void eventMarkersCleared() {
    zoomTimeSlider.cleareMarkers();
    globalTimeSlider.cleareMarkers();
    
    updateTimeBoundaries();
  }

  /**
   * Toggle the hiding or showing of time regions with no data. This is
   * determined by start and stop event markers.
   * 
   * @param hideEmptyTime  if true, only show time with data, otherwise show all
   *                       time within the bounds of the current channels
   */
  public void hideEmptyTime(boolean hideEmptyTime) {
    if (this.hideEmptyTime != hideEmptyTime) {
      this.hideEmptyTime = hideEmptyTime;
      
      if (!hideEmptyTime) {
        zoomTimeSlider.clearTimeRanges();
        globalTimeSlider.clearTimeRanges();
      }
      
      updateTimeBoundaries();
    }
  }

  @Override
  public void timeScaleAdded(int index, double timeScale) {
    timeScaleComboBox.insertItemAt(DataViewer.formatSeconds(timeScale), index);
    
  }

  @Override
  public void timeScaleRemoved(int index, double timeScale) {
    // shouldn't happen yet...
    
  }
}
