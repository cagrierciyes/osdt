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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/viz/spectrum/SpectrumViz.java $
 * $Revision: 1329 $
 * $Date: 2008-12-08 11:07:06 -0500 (Mon, 08 Dec 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.viz.spectrum;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumSet;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.data.Channel;
import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;
import org.rdv.datapanel.AbstractDataPanel;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.SubscriptionResponse;
import org.rdv.rbnb.TimeSeriesData;
import org.rdv.ui.SpringUtilities;

import com.rbnb.sapi.ChannelMap;

/**
 * A spectrum analyzer.
 * 
 * @author Jason P. Hanley
 */
public class SpectrumViz extends AbstractDataPanel {

  /** the logger for this class */
  static Log log = LogFactory.getLog(SpectrumViz.class.getName());

  /** the data panel property for the sample rate */
  private static final String DATA_PANEL_PROPERTY_SAMPLE_RATE = "sampleRate";

  /** the data panel property for the number of samples */
  private static final String DATA_PANEL_PROPERTY_NUMBER_OF_SAMPLES = "numberOfSamples";

  /** the data panel property for the window function */
  private static final String DATA_PANEL_PROPERTY_WINDOW_FUNCTION = "windowFunction";

  /** the data panel property for the segment size */
  private static final String DATA_PANEL_PROPERTY_SEGMENT_SIZE = "segmentSize";

  /** the data panel property for the overlap */
  private static final String DATA_PANEL_PROPERTY_OVERLAP = "overlap";

  /** the data panel property to control the visiblity of the properties panel */
  private static final String DATA_PANEL_PROPERTY_SHOW_PROPERTIES = "showProperties";

  /** the data panel property for the selector function */
  private static final String DATA_PANEL_PROPERTY_SELECTOR_FUNCTION = "SelectorFunction";

  /** the time for the last data point displayed */
  private double lastTimeDisplayed;

  /** the main panel */
  private JPanel panel;

  /** the spectrum analyzer panel */
  private SpectrumAnalyzerPanel spectrumAnalyzerPanel;

  /** the properties panel */
  private JPanel propertiesPanel;

  /** the sample rate text field */
  private JTextField sampleRateTextField;

  /** the number of samples text field */
  private JTextField numberOfSamplesTextField;

  /** the window function combo box */
  private JComboBox windowFunctionComboBox;
  
  /** the selector function combo box */
  private JComboBox selectorFunctionComboBox;

  /** the segment size text field */
  private JTextField segmentSizeTextField;;

  /** the overlap text field */
  private JTextField overlapTextField;

  /** the show properties menu item */
  private JCheckBoxMenuItem showPropertiesMenuItem;
	
  /** Show or hide the properties panel */
  private boolean propertiesVisible_=true;
  
  /**
   * Creates the spectrum analyzer data panel.
   */
	public SpectrumViz() {
		super();
				
		lastTimeDisplayed = -1;
		
		initComponents();
		
		setDataComponent(panel);
	}
	
	/**
	 * Initializes the UI components.
	 */
	private void initComponents() {
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		initSpectrumAnalyzerPanel();
		initPropertiesPanel();
	}
	
	/**
	 * Initializes the spectrum analyzer panel.
	 */
	private void initSpectrumAnalyzerPanel() {
	  spectrumAnalyzerPanel = new SpectrumAnalyzerPanel(this);
    spectrumAnalyzerPanel.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
        hanelSpectrumAnalyzerPanelPropertyChanges(pce);
      }     
    });
	    
    panel.add(spectrumAnalyzerPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Initializes the properties panel.
	 */
	private void initPropertiesPanel() {
    propertiesPanel = new JPanel();
    propertiesPanel.setLayout(new SpringLayout());
    propertiesPanel.setBorder(BorderFactory.createTitledBorder("Properties"));
    propertiesPanel.addMouseListener(getPopupMenuMouseListener());
    
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        handlePropertiesUpdate((Component) ae.getSource());
      }
    };
    
    FocusAdapter focusListener = new FocusAdapter() {
      public void focusLost(FocusEvent fe) {
        handlePropertiesUpdate(fe.getComponent());
      }
    };
    
    propertiesPanel.add(new JLabel("Sample rate: "));
    sampleRateTextField = new JTextField(Double.toString(spectrumAnalyzerPanel.getSampleRate()));
    sampleRateTextField.addActionListener(actionListener);
    sampleRateTextField.addFocusListener(focusListener);
    propertiesPanel.add(sampleRateTextField);
    
    propertiesPanel.add(new JLabel("Number of points: "));
    numberOfSamplesTextField = new JTextField(Integer.toString(spectrumAnalyzerPanel.getNumberOfSamples()));
    numberOfSamplesTextField.addActionListener(actionListener);
    numberOfSamplesTextField.addFocusListener(focusListener);
    propertiesPanel.add(numberOfSamplesTextField);
    
    propertiesPanel.add(new JLabel("Window: "));
    Object[] windowTypes = EnumSet.allOf(WindowFunction.class).toArray();
    windowFunctionComboBox = new JComboBox(windowTypes);
    windowFunctionComboBox.setSelectedItem(spectrumAnalyzerPanel.getWindowFunction());
    windowFunctionComboBox.addActionListener(actionListener);
    propertiesPanel.add(windowFunctionComboBox);
	   
    propertiesPanel.add(new JLabel("Size: "));
    segmentSizeTextField = new JTextField(Integer.toString(spectrumAnalyzerPanel.getSegmentSize()));
    segmentSizeTextField.addActionListener(actionListener);
    segmentSizeTextField.addFocusListener(focusListener);
    propertiesPanel.add(segmentSizeTextField);
    
    propertiesPanel.add(new JLabel("Overlap: "));
    overlapTextField = new JTextField(Integer.toString(spectrumAnalyzerPanel.getOverlap()));
    overlapTextField.addActionListener(actionListener);
    overlapTextField.addFocusListener(focusListener);
    propertiesPanel.add(overlapTextField);
 
	propertiesPanel.add(new JLabel("Selector: "));
    Object[] selectorTypes = EnumSet.allOf(SelectorFunction.class).toArray();
    selectorFunctionComboBox = new JComboBox(selectorTypes);
    selectorFunctionComboBox.setSelectedItem(spectrumAnalyzerPanel.getSelectorFunction());
    selectorFunctionComboBox.addActionListener(actionListener);
    propertiesPanel.add(selectorFunctionComboBox);
	      
    SpringUtilities.makeCompactGrid(propertiesPanel, 6, 2, 5, 5, 5, 5);
    
    panel.add(propertiesPanel, BorderLayout.EAST);
	}
	
	/**
	 * Initializes the popup menu. Adds items from panel superclass, chart and
	 * tweak for this panel.
	 */
	public void buildPopupMenu(JPopupMenu menu, MouseEvent e){
	  super.buildPopupMenu(menu,e);
	  spectrumAnalyzerPanel.buildPopupMenu(menu,e);
	  
	  for(int i=0;i<menu.getComponentCount();i++){
	    Component c = menu.getComponent(i);
	    if (c instanceof JMenuItem){
	      JMenuItem item = (JMenuItem)c;
	      // rename to avoid confusion
	      if (item.getText().compareToIgnoreCase("Properties...")==0){
	        item.setText("Chart Properties...");
	      }
	    }
	  }
	  
	  menu.add(new JPopupMenu.Separator());
    
    showPropertiesMenuItem = new JCheckBoxMenuItem("Show properties", propertiesVisible_);
    showPropertiesMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        propertiesVisible_=showPropertiesMenuItem.isSelected();
        setPropertiesVisible(propertiesVisible_);
      }
    });
    menu.add(showPropertiesMenuItem);
	}
	
	/**
	 * Handle events from the properties panel.
	 * 
	 * @param source  the source of the event
	 */
	private void handlePropertiesUpdate(Component source) {
	  if (source == null) return;
	  
	  if (source.equals(sampleRateTextField)) {
	    handleSampleRateUpdate();
	  } else if (source.equals(numberOfSamplesTextField)) {
	    handleNumberOfSamplesUpdate();
	  }
	   else if (source.equals(windowFunctionComboBox)) {
	    handleWindowFunctionUpdate();
	  }
	   else if (source.equals(selectorFunctionComboBox)) {
	    handleSelectorFunctionUpdate();
	  }	  
	    else if (source.equals(segmentSizeTextField)) {
	    handleSegmentSizeUpdate();
	  } else if (source.equals(overlapTextField)) {
	    handleOverlapUpdate();
	  }
	}
	
	/**
	 * Handles events from the sample rate component.
	 */
	private void handleSampleRateUpdate() {
    try {
      double sampleRate = Double.parseDouble(sampleRateTextField.getText());
      spectrumAnalyzerPanel.setSampleRate(sampleRate);
    } catch (Exception e) {
      sampleRateTextField.setText(Double.toString(spectrumAnalyzerPanel.getSampleRate()));
      return;
    }
	}
	
	/**
	 * Handles events from the number of samples component.
	 */
	private void handleNumberOfSamplesUpdate() {
    try {
      int numberOfSamples = Integer.parseInt(numberOfSamplesTextField.getText());
      spectrumAnalyzerPanel.setNumberOfSamples(numberOfSamples);
    } catch (Exception e) {
      numberOfSamplesTextField.setText(Integer.toString(spectrumAnalyzerPanel.getNumberOfSamples()));
      return;
    }
	}
	
	/**
	 * Handles events from the window function component.
	 */
	private void handleWindowFunctionUpdate() {
	  WindowFunction windowFunction = (WindowFunction)windowFunctionComboBox.getSelectedItem();
	  spectrumAnalyzerPanel.setWindowFunction(windowFunction);
	}
	
	private void handleSelectorFunctionUpdate() {
	  SelectorFunction selectorFunction = (SelectorFunction)selectorFunctionComboBox.getSelectedItem();
	  spectrumAnalyzerPanel.setSelectorFunction(selectorFunction);
	}
	
	/**
	 * Handles events from the segment size component.
	 */
	private void handleSegmentSizeUpdate() {
    try {
      int segmentSize = Integer.parseInt(segmentSizeTextField.getText());
      spectrumAnalyzerPanel.setSegmentSize(segmentSize);
    } catch (Exception e) {
      segmentSizeTextField.setText(Integer.toString(spectrumAnalyzerPanel.getSegmentSize()));
      return;
    }
	}
	
	/**
	 * Handles events from the overlap component.
	 */
	private void handleOverlapUpdate() {
    try {
      int overlap = Integer.parseInt(overlapTextField.getText());
      spectrumAnalyzerPanel.setOverlap(overlap);
    } catch (Exception e) {
      overlapTextField.setText(Integer.toString(spectrumAnalyzerPanel.getOverlap()));
      return;
    }
	}
	
	/**
	 * Hanles property change events from the spectrum analyzer panel.
	 * 
	 * @param pce  the property change event
	 */
	private void hanelSpectrumAnalyzerPanelPropertyChanges(PropertyChangeEvent pce) {
    String propertyName = pce.getPropertyName();
    if (propertyName.equals(SpectrumAnalyzerPanel.SAMPLE_RATE_PROPERTY)) {
      String sampleRate = Double.toString(spectrumAnalyzerPanel.getSampleRate());
      sampleRateTextField.setText(sampleRate);
      properties.setProperty(DATA_PANEL_PROPERTY_SAMPLE_RATE, sampleRate);
    } else if (propertyName.equals(SpectrumAnalyzerPanel.NUMBER_OF_SAMPLES_PROPERTY)) {
      String numberOfSamples = Integer.toString(spectrumAnalyzerPanel.getNumberOfSamples());
      numberOfSamplesTextField.setText(numberOfSamples);
      properties.setProperty(DATA_PANEL_PROPERTY_NUMBER_OF_SAMPLES, numberOfSamples);
    } else if (propertyName.equals(SpectrumAnalyzerPanel.WINDOW_FUNCTION_PROPERTY)) {
      WindowFunction windowFunction = spectrumAnalyzerPanel.getWindowFunction();
      windowFunctionComboBox.setSelectedItem(windowFunction);
      String windowFunctionString = windowFunction.toString();
      properties.setProperty(DATA_PANEL_PROPERTY_WINDOW_FUNCTION, windowFunctionString);
    } else if (propertyName.equals(SpectrumAnalyzerPanel.SELECTOR_FUNCTION_PROPERTY)) {
      SelectorFunction selectorFunction = spectrumAnalyzerPanel.getSelectorFunction();
      selectorFunctionComboBox.setSelectedItem(selectorFunction);
      String selectorFunctionString = selectorFunction.toString();
      properties.setProperty(DATA_PANEL_PROPERTY_SELECTOR_FUNCTION, selectorFunctionString);
    } else if (propertyName.equals(SpectrumAnalyzerPanel.SEGMENT_SIZE_PROPERTY)) {
      String segmentSize = Integer.toString(spectrumAnalyzerPanel.getSegmentSize());
      segmentSizeTextField.setText(segmentSize);
      properties.setProperty(DATA_PANEL_PROPERTY_SEGMENT_SIZE, segmentSize);
    } else if (propertyName.equals(SpectrumAnalyzerPanel.OVERLAP_PROPERTY)) {
      String overlap = Integer.toString(spectrumAnalyzerPanel.getOverlap());
      overlapTextField.setText(overlap);
      properties.setProperty(DATA_PANEL_PROPERTY_OVERLAP, overlap);
    }
	}
	
	/**
	 * Gets the properties panel visibility.
	 * 
	 * @return  true if visible, false otherwise
	 */
  public boolean isPropertiesVisible() {
    return propertiesPanel.isVisible();
  }
  
  /**
   * Sets the properties panel visibility.
   * 
   * @param visible  true to make the panel visible, false to make it invisible
   */
  public void setPropertiesVisible(boolean visible) {
    propertiesPanel.setVisible(visible);
    //showPropertiesMenuItem.setSelected(visible);
    
    if (visible) {
      properties.remove(DATA_PANEL_PROPERTY_SHOW_PROPERTIES);
    } else {
      properties.setProperty(DATA_PANEL_PROPERTY_SHOW_PROPERTIES, "false");
    }
  }

	public boolean supportsMultipleChannels() {
		return false;
	}

	@Override
  protected void onSeriesAdded(VisualizationSeries vs) {
	  String channelName = vs.getChannels().iterator().next();
	  Channel channel = RBNBController.getInstance().getChannel(channelName);
	  if (channel == null) {
	    return;
	  }
	  
	  // get the sample rate and send it to the spectrum analzyer panel
	  String sampleRateString = channel.getMetadata("sampleRate");
	  if (sampleRateString == null) {
	    return;
	  }
	  
	  double sampleRate;
	  try {
	    sampleRate = Double.parseDouble(sampleRateString);
	  } catch (NumberFormatException e) {
	    return;
	  }
	  
	  if (sampleRate > 0) {
	    spectrumAnalyzerPanel.setSampleRate(sampleRate);
	    
	    sampleRateTextField.setEnabled(false);
	  }
  }

  @Override
  protected void onSeriesRemoved(VisualizationSeries vs) {
    
    clearData();
    
    sampleRateTextField.setEnabled(true);
  }

  @Override
  public void postTime(double time) {
    super.postTime(time);
  }
  
  @Override
	public void postData(final SubscriptionResponse r) {
    if(r==null)return;
            //ChannelMap channelMap=subResponse_.getChannelMap();
    if (r.containsHistory()) {
      clearData();
    }
    //TimeSeriesData tsd=r.getTimeSeries();
		//super.postTime(time);

//		if (channelMap == null) {
//			//no data to display yet
//			return;
//		}
		
    VizSeriesList vsl=getSeries();
    if(vsl.size()==0)return;
    
    for(VisualizationSeries s:vsl){
      String channelName=s.getChannels().get(0);
      TimeSeriesData tsd=r.getTimeSeries(s);
      if(tsd==null) continue;
      
      int channelIndex = tsd.getChannelIndex(channelName);
      
      //if there is data for channel, post it
      if (channelIndex >= 0 && tsd.hasData(channelIndex)) {
        postData(tsd, channelIndex);
      }
    }
    //loop over all channels and see if there is data for them
//    for (String channelName : channels) {
//      int channelIndex = tsd.getChannelIndex(channelName);
//      
//      //if there is data for channel, post it
//      if (channelIndex >= 0 && tsd.hasData(channelIndex)) {
//        postData(tsd, channelIndex);
//      }
//    }
  }

  /**
   * Looks through the data for the current time range and post it if any is
   * found.
   * 
   * @param tsd           the time series data
   * @param channelIndex  the index for the channel
   */
  private void postData(final TimeSeriesData tsd, 
                  final int channelIndex) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        int typeID = tsd.getType(channelIndex);
        Iterator<Double>timeIt=tsd.getTimes(channelIndex).iterator();
        while(timeIt.hasNext()){
          Double currTime=timeIt.next();
          double currData=0.0;
          switch (typeID) {
          case ChannelMap.TYPE_FLOAT64:
            currData = tsd.getDataAsFloat64(channelIndex,currTime);
            break;
          case ChannelMap.TYPE_FLOAT32:
            currData = tsd.getDataAsFloat32(channelIndex,currTime);
            break;
          case ChannelMap.TYPE_INT64:
            currData = tsd.getDataAsInt64(channelIndex,currTime);
            break;
          case ChannelMap.TYPE_INT32:
            currData = tsd.getDataAsInt32(channelIndex,currTime);
            break;
          case ChannelMap.TYPE_INT16:
            currData= tsd.getDataAsInt16(channelIndex,currTime);
            break;
          case ChannelMap.TYPE_INT8:
            currData = tsd.getDataAsInt8(channelIndex,currTime);
            break;
          default:
            // wrong type???
            return;
          }
          spectrumAnalyzerPanel.addData(currData);
          if(!timeIt.hasNext()){
            lastTimeDisplayed = currTime;
          }
        }
        spectrumAnalyzerPanel.finishedAddingData();
      }  
    });
    

    
  }

  /**
   * Post the data to the spectrum analyzer. This will method will queue the
   * post on the EDT thread and return immediately.
   * 
   * @param channelMap    the channel map with the data
   * @param channelIndex  the index to the channel
   * @param startIndex    the start index of the data
   * @param endIndex      the end index of the data
   */
  private void postData(final ChannelMap channelMap, 
                        final int channelIndex, final int startIndex, 
                        final int endIndex) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        int typeID = channelMap.GetType(channelIndex);

        switch (typeID) {
        case ChannelMap.TYPE_FLOAT64:
          double[] doubleData = channelMap.GetDataAsFloat64(channelIndex);
          spectrumAnalyzerPanel.addData(doubleData, startIndex, endIndex);
          break;
        case ChannelMap.TYPE_FLOAT32:
          float[] floatData = channelMap.GetDataAsFloat32(channelIndex);
          spectrumAnalyzerPanel.addData(floatData, startIndex, endIndex);
          break;
        case ChannelMap.TYPE_INT64:
          long[] longData = channelMap.GetDataAsInt64(channelIndex);
          for (int i = startIndex; i <= endIndex; i++) {
            spectrumAnalyzerPanel.addData(longData[i]);
          }
          spectrumAnalyzerPanel.finishedAddingData();
          break;
        case ChannelMap.TYPE_INT32:
          int[] intData = channelMap.GetDataAsInt32(channelIndex);
          for (int i = startIndex; i <= endIndex; i++) {
            spectrumAnalyzerPanel.addData(intData[i]);
          }
          spectrumAnalyzerPanel.finishedAddingData();
          break;
        case ChannelMap.TYPE_INT16:
          short[] shortData = channelMap.GetDataAsInt16(channelIndex);
          for (int i = startIndex; i <= endIndex; i++) {
            spectrumAnalyzerPanel.addData(shortData[i]);
          }
          spectrumAnalyzerPanel.finishedAddingData();
          break;
        case ChannelMap.TYPE_INT8:
          byte[] byteData = channelMap.GetDataAsInt8(channelIndex);
          for (int i = startIndex; i <= endIndex; i++) {
            spectrumAnalyzerPanel.addData(byteData[i]);
          }
          spectrumAnalyzerPanel.finishedAddingData();
          break;
        }
      }
    });
  }

  /**
   * Clear the current stored data. This will queue a call on the EDT to clear
   * the data in the spectrum analyzer and return immediatly.
   */
  private void clearData() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        spectrumAnalyzerPanel.clearData();
      }
    });
	  
    lastTimeDisplayed = -1;
  }

  @Override
  public void setProperty(String key, String value) {
    super.setProperty(key, value);
    
    if (key == null) {
      return;
    }
    
    properties.setProperty(key, value);
    
    if (key.equals(DATA_PANEL_PROPERTY_SAMPLE_RATE)) {
      try {
        double sampleRate = Double.parseDouble(value);
        spectrumAnalyzerPanel.setSampleRate(sampleRate);
      } catch (Exception e) {
        log.warn("Unable to set sample rate: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_NUMBER_OF_SAMPLES)) {
      try {
        int numberOfSamples = Integer.parseInt(value);
        spectrumAnalyzerPanel.setNumberOfSamples(numberOfSamples);
      } catch (Exception e) {
        log.warn("Unable to set number of samples: " + value + ".");
      }
    }

else if (key.equals(DATA_PANEL_PROPERTY_SELECTOR_FUNCTION)) {
      try {
	SelectorFunction selectorFunction = SelectorFunction.valueOf(value);
        spectrumAnalyzerPanel.setSelectorFunction(selectorFunction);
      } catch (Exception e) {
        log.warn("Unable to set selector function: " + value + ".");
      }
    }

else if (key.equals(DATA_PANEL_PROPERTY_WINDOW_FUNCTION)) {
      try {
        WindowFunction windowFunction = WindowFunction.valueOf(value);
        spectrumAnalyzerPanel.setWindowFunction(windowFunction);
      } catch (Exception e) {
        log.warn("Unable to set window function: " + value + ".");
      }
    }

else if (key.equals(DATA_PANEL_PROPERTY_SEGMENT_SIZE)) {
      try {
        int segmentSize = Integer.parseInt(value);
        spectrumAnalyzerPanel.setSegmentSize(segmentSize);
      } catch (Exception e) {
        log.warn("Unable to set segment size: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_OVERLAP)) {
      try {
        int overlap = Integer.parseInt(value);
        spectrumAnalyzerPanel.setOverlap(overlap);
      } catch (Exception e) {
        log.warn("Unable to set overlap: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_SHOW_PROPERTIES) && !Boolean.parseBoolean(value)) {
      setPropertiesVisible(false);
    }
  }

  @Override
  public String toString() {
    return "Spectrum Analyzer";
  }

}
