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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/viz/chart/ChartViz.java $
 * $Revision: 1307 $
 * $Date: 2008-12-01 12:54:10 -0500 (Mon, 01 Dec 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.viz.chart;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.rdv.DataViewer;
import org.rdv.TimeScale;
import org.rdv.data.Channel;
import org.rdv.data.ChartLocalSeries;
import org.rdv.data.DataFileReader;
import org.rdv.data.NumericDataSample;
import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;
import org.rdv.data.XYChannelSeries;
import org.rdv.datapanel.AbstractDataPanel;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBTime;
import org.rdv.rbnb.SubscriptionResponse;
import org.rdv.rbnb.TimeSeriesData;
import org.rdv.ui.UIUtilities;

import com.rbnb.sapi.ChannelMap;

/**
 * A data panel to plot data time series and xy charts. 
 * 
 * @author Jason P. Hanley
 * @author Drew Daugherty
 */
public abstract class ChartViz extends AbstractDataPanel 
{
  /**
   * The logger for this class.
   */
	static Log log = LogFactory.getLog(ChartViz.class.getName());
	
  /** the data panel property to control the legend visibility */
  private static final String DATA_PANEL_PROPERTY_SHOW_LEGEND = "showLegend";
  
  private static final double ADJUST_TIME_SCALE_TIMEOUT_SEC=2.0;
  
  /**
   * The chart.
   */
	JFreeChart chart;
  
  /**
   * The xy plot for this chart.
   */
  XYPlot xyPlot;
  
  /**
   * The domain (horizontal) axis that contains a value. This will be a number
   * axis for an xy plot or a date axis for a timeseries plot.
   */
  ValueAxis domainAxis;
  
  /**
   * The range (vertical) axis that contains a number.
   */
  NumberAxis rangeAxis;
  
  /**
   * The component that renderers the chart.
   */
	ChartPanel chartPanel;
  
  /**
   * The data set for the chart.
   */
	XYDataset dataCollection;
  
  /**
   * The legend for the series in the chart.
   */
  LegendTitle seriesLegend;
	
  /**
   * The container for the chart component.
   */
  JPanel chartPanelPanel;
  
  /**
   * A bit to indicate if we are plotting time series charts of x vs. y charts.
   */
	final boolean xyMode;
  
	/**
   * The timestamp for the last piece if data displayed.
   */
  long lastDataPostSysTime=-1;
  
  /**
   * The number of local data series.
   */
  int localSeries;
  
  /**
   * Data start time for the current post operation.
   */
  double postDataStartTime=Double.MAX_VALUE;
  
  /**
   * Data end time for the current post operation.
   */
  double postDataEndTime=0.0;
  
  /**
   * Data record count for the current post operation.
   */
  long postDataCount=0L;
  
  /**
   * Plot colors for each series.
   */
  HashMap<String,Color> colors;
  
  
  /**
   * Colors used for the series.
   */
  final static Color[] seriesColors = {Color.decode("#FF0000"), Color.decode("#0000FF"),
                    Color.decode("#009900"), Color.decode("#FF9900"),
                    Color.decode("#9900FF"), Color.decode("#FF0099"),
                    Color.decode("#0099FF"), Color.decode("#990000"),
                    Color.decode("#000099"), Color.black};

  /** a flag to control the legend visibility, defaults to true */
  private boolean showLegend;
  
  /**
   * Constructs a chart data panel in time series mode.
   */
	public ChartViz() {
		this(false);
	}

  /**
   * Constructs a chart data panel.
   * 
   * @param xyMode  if true in x vs. y mode, otherwise in time series mode
   */
	public ChartViz(boolean xyMode) {
		super();
		
		this.xyMode = xyMode;
		
    //lastTimeDisplayed = -1;
    
    colors = new HashMap<String,Color>();

    // show the legend by default    
    showLegend = true;
		
		initChart();
		
		setDataComponent(chartPanelPanel);
	}
  
  /**
   * Create the chart and setup it's UI.
   */
	private void initChart() {
    XYToolTipGenerator toolTipGenerator;
    
		if (xyMode) {
			dataCollection = new XYTimeSeriesCollection();
      
      NumberAxis domainAxis = new NumberAxis();
      domainAxis.setAutoRangeIncludesZero(false);
      domainAxis.addChangeListener(new AxisChangeListener() {
        public void axisChanged(AxisChangeEvent ace) {
          boundsChanged();
        }        
      });
      this.domainAxis = domainAxis;
      
      toolTipGenerator = new StandardXYToolTipGenerator("{0}: {1} , {2}",
          new DecimalFormat(),
          new DecimalFormat());
		} else {
			dataCollection = new TimeSeriesCollection();
      
      domainAxis = new FixedAutoAdjustRangeDateAxis();
      domainAxis.setLabel("Time");
      domainAxis.setAutoRange(false);
            
      toolTipGenerator = new StandardXYToolTipGenerator("{0}: {1} , {2}",
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"),
          new DecimalFormat());
    }
    
    rangeAxis = new NumberAxis();
    rangeAxis.setAutoRangeIncludesZero(false);    
    rangeAxis.addChangeListener(new AxisChangeListener() {
      public void axisChanged(AxisChangeEvent ace) {
        boundsChanged();
      }        
    });    
    
    FastXYItemRenderer renderer = new FastXYItemRenderer(StandardXYItemRenderer.LINES,
        toolTipGenerator);
    //SamplingXYLineRenderer renderer = new SamplingXYLineRenderer();
    //renderer.setToolTipGenerator(toolTipGenerator);
    //renderer.setShapeSize(0);
    renderer.setBaseCreateEntities(false);
    renderer.setBaseStroke(new BasicStroke(0.5f));
    Rectangle rect = new Rectangle(0,0,5,5);
    renderer.setLegendLine(rect);
    if (xyMode) {
      renderer.setCursorVisible(true);
    }
    
    xyPlot = new XYPlot(dataCollection, domainAxis, rangeAxis, renderer);
    
    chart = new JFreeChart(xyPlot);    
		chart.setAntiAlias(false);
    //chart.setAntiAlias(true);
    
    seriesLegend = chart.getLegend();
    chart.removeLegend();    

		chartPanel = new ChartPanel(chart, true, this);
    chartPanel.setInitialDelay(0);

		chartPanelPanel = new JPanel();
		chartPanelPanel.setLayout(new BorderLayout());
		chartPanelPanel.add(chartPanel, BorderLayout.CENTER);
	}
  
	@Override
	public void buildPopupMenu(JPopupMenu menu, MouseEvent e){
	  super.buildPopupMenu(menu, e);
	  
	  ActionListener setTimerangeAction = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        double newScale =Double.parseDouble(ae.getActionCommand());
        setLocalTimescale(newScale);
      }
    };
	  
	  // regenerate new menu
    JMenu timeRangeMenu = new JMenu("Set Timerange");
    ButtonGroup timeRangeGroup = new ButtonGroup();
    JRadioButtonMenuItem range = new JRadioButtonMenuItem(
                              TimeScale.TIME_SCALE_UNDEFINED_DESC);
    range.setActionCommand(String.valueOf(TimeScale.TIME_SCALE_UNDEFINED));
    range.addActionListener(setTimerangeAction);
    timeRangeGroup.add(range);
    timeRangeMenu.add(range);
    if (localTimeScale_ == TimeScale.TIME_SCALE_UNDEFINED)
      range.setSelected(true);
    
    Double timeScales[]=TimeScale.getGlobalTimeScales();
    for (int i=0;i<timeScales.length;i++) {
      range = new JRadioButtonMenuItem(
                  DataViewer.formatSeconds(timeScales[i]));
      range.setActionCommand(String.valueOf(timeScales[i]));
      range.addActionListener(setTimerangeAction);
      timeRangeGroup.add(range);
      timeRangeMenu.add(range);
      if (localTimeScale_ == timeScales[i])
        range.setSelected(true);
    }
    
    menu.add(timeRangeMenu);
    
 // create a popup menu item to copy an image to the clipboard
    final JMenuItem copyChartMenuItem = new JMenuItem("Copy");
    copyChartMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        copyChart();
      }
    });
    menu.add(copyChartMenuItem);

    //menu.insert(new JPopupMenu.Separator(), 3);
    
    menu.add(new JPopupMenu.Separator());
    
    // Clear Data from the chart - Thomas Marullo 9/24/09
    final JMenuItem clearChartMenuItem = new JMenuItem("Clear Data");
        clearChartMenuItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                clearData();
              }
            }); 
          }
        });
        menu.add(clearChartMenuItem, 2);
    // End Clear Data
    
    JMenuItem showLegendMenuItem = new JCheckBoxMenuItem("Show Legend", showLegend);
    showLegendMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setShowLegend(!showLegend);
      }
    });
    menu.add(showLegendMenuItem);

    if (xyMode) {
      menu.add(new JPopupMenu.Separator());
      
      JMenuItem addLocalSeriesMenuItem = new JMenuItem("Add local series...");
      addLocalSeriesMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          addLocalSeries();
        }        
      });
      
      menu.add(addLocalSeriesMenuItem);
    }
    
    chartPanel.buildPopupMenu(menu,e);
	}
	
  /**
   * Takes the chart and puts it on the clipboard as an image.
   */
  private void copyChart() {
    // get the system clipboard
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    // create an image of the chart with the preferred dimensions
    Dimension preferredDimension = chartPanel.getPreferredSize();
    Image image = chart.createBufferedImage((int)preferredDimension.getWidth(), (int)preferredDimension.getHeight());
    
    // wrap image in the transferable and put on the clipboard
    ImageSelection contents = new ImageSelection(image);
    clipboard.setContents(contents, null);
  }
  
  /**
   * Add data from a local file as a series to this chart. This will ask the
   * user for the file name, and which channels to use.
   */
  private void addLocalSeries() {
    File file = UIUtilities.openFile();
    if (file == null || !file.isFile() || !file.exists()) {
      return;
    }
    
    DataFileReader reader;
    try {
      reader = new DataFileReader(file);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(getDataComponent(),
          e.getMessage(),
          "Problem reading data file",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    
    List<Channel> channels = reader.getChannels();
    if (channels.size() < 2) {
      JOptionPane.showMessageDialog(getDataComponent(),
          "There must be at least 2 channels in the data file",
          "Problem with data file",
          JOptionPane.ERROR_MESSAGE);      
      return;
    }
    
    Channel xChannel;
    Channel yChannel;
    
    if (channels.size() == 2) {
      xChannel = channels.get(0);
      yChannel = channels.get(1);
    } else {    
      xChannel = (Channel)JOptionPane.showInputDialog(
          getDataComponent(),
          "Select the x channel:",
          "Add local channel",
          JOptionPane.PLAIN_MESSAGE,
          null,
          channels.toArray(),
          null);
      
      if (xChannel == null) {
        return;
      }
      
      yChannel = (Channel)JOptionPane.showInputDialog(
          getDataComponent(),
          "Select the y channel:",
          "Add local channel",
          JOptionPane.PLAIN_MESSAGE,
          null,
          channels.toArray(),
          null);
      
      if (yChannel == null) {
        return;
      }
    }

    String xChannelName = xChannel.getName();
    if (xChannel.getUnit() != null) {
      xChannelName += " (" + xChannel.getUnit() + ")";
    }
    int xChannelIndex = channels.indexOf(xChannel);
    
    String yChannelName = yChannel.getName();
    if (yChannel.getUnit() != null) {
      yChannelName += " (" + yChannel.getUnit() + ")";
    }
    int yChannelIndex = channels.indexOf(yChannel);
    
    String seriesName = xChannelName + " vs. " + yChannelName;
    
    XYTimeSeries data = new XYTimeSeries(seriesName, FixedMillisecond.class);
    
    try {
      NumericDataSample sample;
      while ((sample = reader.readSample()) != null) {
        double timestamp = sample.getTimestamp();
        Number[] values = sample.getValues();
        
        FixedMillisecond time = new FixedMillisecond((long)(timestamp*1000));
        XYTimeSeriesDataItem dataItem = new XYTimeSeriesDataItem(time);
        
        if (values[xChannelIndex] != null && values[yChannelIndex] != null) {
          dataItem.setX(values[xChannelIndex]);
          dataItem.setY(values[yChannelIndex]);
        }
        
        data.add(dataItem, false);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    
    Color color = getLeastUsedColor();
    colors.put(seriesName, color);      
    
    ((XYTimeSeriesCollection)dataCollection).addSeries(data);
    localSeries++;
    
    setSeriesColors();
    
    updateTitle();
    
    updateLegend();
  }
  
  /**
   * Remove the local series from the chart.
   * 
   * @param seriesName  the name of the local series.
   */
  private void removeLocalSeries(String seriesName) {
    XYTimeSeries series = ((XYTimeSeriesCollection)dataCollection).getSeries(seriesName);
    
    if (series == null) {
      return;
    }
    
    localSeries--;
    ((XYTimeSeriesCollection)dataCollection).removeSeries(series);
    
    colors.remove(seriesName);
    setSeriesColors();
    
    updateTitle();
    
    updateLegend();
  }
  
  /**
   * Called when the bounds of an axis are changed. This updates the data panel
   * properties for these values.
   */
  private void boundsChanged() {
    if (xyMode) {
      if (domainAxis.isAutoRange()) {
        properties.remove("domainLowerBound");
        properties.remove("domainUpperBound");        
      } else {
        log.debug("setting domain bound to lower:"+domainAxis.getLowerBound()+
                  " upper:"+ domainAxis.getUpperBound());
        properties.setProperty("domainLowerBound", Double.toString(domainAxis.getLowerBound()));
        properties.setProperty("domainUpperBound", Double.toString(domainAxis.getUpperBound()));        
      }
    }
    
    if (rangeAxis.isAutoRange()) {
      log.debug("range is auto - lower:"+rangeAxis.getLowerBound()+
          " upper:"+ rangeAxis.getUpperBound());
      properties.remove("rangeLowerBound");
      properties.remove("rangeUpperBound");
    } else {
      log.debug("setting range bound to lower:"+rangeAxis.getLowerBound()+
          " upper:"+ rangeAxis.getUpperBound());
      properties.setProperty("rangeLowerBound", Double.toString(rangeAxis.getLowerBound()));
      properties.setProperty("rangeUpperBound", Double.toString(rangeAxis.getUpperBound()));
    }
  }
		
  /**
   * Indicates that this data panel can support multiple channels. This always
   * returns true.
   * 
   * @return  always true
   */
	public boolean supportsMultipleChannels() {
		return true;
	}
	
	protected VisualizationSeries getSeriesForChannel(String channelName){
	  if (xyMode) {
	    return new XYChannelSeries(channelName);
	  }else{
	    return super.getSeriesForChannel(channelName);
	  }
  }
	
	protected void onSeriesAdded(VisualizationSeries vs){
//	  String channelDisplay = getChannelDisplay(channelName);
//    String seriesName = null;
    Color color = getLeastUsedColor();
    
    if (xyMode) {
//        Iterator<String> chanIt = vs.getChannels().iterator();
//        String firstChannelName = (String)chanIt.next();
//        String firstChannelDisplay = getChannelDisplay(firstChannelName);
//        seriesName = firstChannelDisplay + " vs. " + channelDisplay;
//        
        //color = getLeastUsedColor();
        
        XYTimeSeries data = new XYTimeSeries(vs.getName(), FixedMillisecond.class);
        data.setMaximumItemAge((long)(timeScale*1000), (long)(time*1000));
        
        int position = dataCollection.getSeriesCount() - localSeries;
        ((XYTimeSeriesCollection)dataCollection).addSeries(position, data);
      
    } else {
      //seriesName = channelDisplay;
      FastTimeSeries data = new FastTimeSeries(vs.getName(), FixedMillisecond.class);
      data.setMaximumItemAge((long)(timeScale*1000), (long)(time*1000));
      ((TimeSeriesCollection)dataCollection).addSeries(data);
    }
      
    // find the least used color and set it
    colors.put(vs.getName(), color);
    setSeriesColors();
    updateLegend();
    System.out.println("adding series "+vs.getName());
	}
	
  /**
   * Called when a channel has been added.
   * 
   * @param channelName  the new channel
   */
//  protected void channelAdded(String channelName) {
//    
//    updateTitle();    
//        
//  }
	
  /**
   * Update legend and color allocations.
   * @param  vs the series that was removed.
   */
  protected void onSeriesRemoved(VisualizationSeries vs) {
    setSeriesColors();
    //updateTitle();
    updateLegend();
  }
  
  /**
   * Return a color that is least used.
   * 
   * @return            the color
   */
  private Color getLeastUsedColor() {
    int usage = -1;
    Color color = null;
    for (int i=0; i<seriesColors.length; i++) {
      int seriesUsingColor = getSeriesUsingColor(seriesColors[i]);
      if (usage == -1 || seriesUsingColor < usage) {
        usage = seriesUsingColor;
        color = seriesColors[i]; 
      }
    }
    return color;
  }
  
  /**
   * Count the number of series using the specified color for their series
   * plot.
   * 
   * @param color          the color to find
   * @return               the number of series using this color
   */
  private int getSeriesUsingColor(Color color) {
    if (color == null) {
      return 0;
    }
    
    int count = 0;
    
    for (int i=0; i<dataCollection.getSeriesCount(); i++) {
      Paint p = xyPlot.getRenderer().getSeriesPaint(i);
      if (p.equals(color)) {
        count++;
      }
    }
    
    return count;
  }
  
  /**
   * Set the color for all the series.
   */
  private void setSeriesColors() {
    for (int i=0; i<dataCollection.getSeriesCount(); i++) {
      String series = (String)dataCollection.getSeriesKey(i);
      xyPlot.getRenderer().setSeriesPaint(i, colors.get(series));
    }
  }

  /**
   * Set local time scale.
   */
  protected void setLocalTimescale(double timeScale) {
    super.setLocalTimescale(timeScale);
    ageDataOnTimeScaleChange();
  }

  /**
   * Shows or hides the legend.
   * 
   * @param showLegend  if true, the legend will show, otherwise it will not
   */
  private void setShowLegend(boolean showLegend) {
    if (this.showLegend == showLegend) {
      return;
    }
    
    this.showLegend = showLegend;
    
    //showLegendMenuItem.setSelected(showLegend);
    
    if (showLegend) {
      properties.remove(DATA_PANEL_PROPERTY_SHOW_LEGEND);
    } else {
      properties.setProperty(DATA_PANEL_PROPERTY_SHOW_LEGEND, "false");
    }
    
    updateLegend();
  }
  
  /**
   * Update the legend and axis labels based on the series being viewed.
   */
  private void updateLegend() {
    int series = dataCollection.getSeriesCount();
    //int chans = channels.size();
    VisualizationSeries firstSeries = null;
    if(seriesList_.size()>0){
      firstSeries = seriesList_.get(0);
    }
    
    if (xyMode) {
      if(firstSeries==null){
        domainAxis.setLabel(null);
        rangeAxis.setLabel(null);
      }else{
        String channelDisplay = getChannelDisplay(firstSeries.getChannels().get(0));
        domainAxis.setLabel(channelDisplay);
        
        if(firstSeries.getChannels().size()>1){
          String channel2 = firstSeries.getChannels().get(1);
          rangeAxis.setLabel(getChannelDisplay(channel2));
        }else{
          rangeAxis.setLabel(null);
        }
      }
//      if (series == 0 && chans == 1) {
//        String channelDisplay = getChannelDisplay((String)channels.get(0));
//        domainAxis.setLabel(channelDisplay);
//        rangeAxis.setLabel(null);
//      } else if (series == 1 && chans == 0) {
//        XYTimeSeries xySeries = ((XYTimeSeriesCollection)dataCollection).getSeries(0); 
//        String seriesName = (String)xySeries.getKey();
//        String[] channelNames = seriesName.split(" vs. ");
//        if (channelNames.length == 2) {
//          domainAxis.setLabel(channelNames[0]);
//          rangeAxis.setLabel(channelNames[1]);
//        }
//      } else if (series == 1 && chans == 2) {
//        String channelDisplay1 = getChannelDisplay((String)channels.get(0));
//        domainAxis.setLabel(channelDisplay1);
//        String channelDisplay2 = getChannelDisplay((String)channels.get(1));
//        rangeAxis.setLabel(channelDisplay2);
//      }      
    } else {
      if(firstSeries==null){
        rangeAxis.setLabel(null);
      }else{
        String channelDisplay = getChannelDisplay(firstSeries.getChannels().get(0));
        rangeAxis.setLabel(channelDisplay);
      }
    }
    
    // show the legend if it is enabled and there are at least 2 series
    if (showLegend && series >= 2) {
      if (chart.getLegend() == null) {
        chart.addLegend(seriesLegend);
      }
    } else {
      if (chart.getLegend() != null) {
        seriesLegend = chart.getLegend();
      }
      chart.removeLegend();      
    }    
  }
		
//  @Override
//  public VizSeriesList getSeries(){
//    
//    if (xyMode) {
//      VizSeriesList ret = new VizSeriesList();
//      Iterator<String> i = channels.iterator();
//      while (i.hasNext()) {
//        String firstChannel = i.next();
//        if (i.hasNext()) {
//          String secondChannel = i.next();
//          List<String> channels=new ArrayList<String>();
//          channels.add(firstChannel);
//          channels.add(secondChannel);
//          ChannelSeries newSeries=new ChannelSeries(channels, new XYSeriesFormatter());
//          ret.add(newSeries);
//        }else{
//          ChannelSeries newSeries=new ChannelSeries(firstChannel, new XYSeriesFormatter());
//          ret.add(newSeries);
//        }
//      }
//
//      // add opaque series with just name placeholder???
//      int remoteSeries = dataCollection.getSeriesCount()-localSeries;
//      for (int j=remoteSeries; j<remoteSeries+localSeries; j++) {
//        String seriesName = (String)dataCollection.getSeriesKey(j);
//        new ChartLocalSeries(seriesName);
//      }
//      return ret;
//    }else{
//      return super.getSeries();
//    }
//  }
  
  @Override
  public boolean removeSeries(String seriesName){
    boolean ret=true;
    VizSeriesList slist = getSeries();
    for(int i=0;i<slist.size();i++){
      VisualizationSeries series=slist.get(i);
      
      if(series.getName().compareToIgnoreCase(seriesName)==0){
        if(series instanceof ChartLocalSeries){
          removeLocalSeries(series.getName());
        }else{
          if (xyMode) {
            XYTimeSeriesCollection dataCollection = (XYTimeSeriesCollection)this.dataCollection;
            XYTimeSeries data = dataCollection.getSeries(series.getName());
            dataCollection.removeSeries(data);
            
            colors.remove(series.getName());
            ret= super.removeSeries(series);
          } else {
            
            TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
            TimeSeries data = dataCollection.getSeries(series.getName());
            if(data!=null){
              dataCollection.removeSeries(data);
            }
            colors.remove(series.getName());
            
            ret= super.removeSeries(series);
          }
          
        }
      }
    }
    
    setSeriesColors();
    updateTitle();
    updateLegend();
    return ret;
  }
  
	
  /**
   * Get the string for this channel to display in the UI. This will show the
   * channel units if there are any.
   *  
   * @param channelName  the name of the channel
   * @return             the string to display the channel in the UI
   */
  private String getChannelDisplay(String channelName) {
    String seriesName = channelName;
    Channel channel = RBNBController.getInstance().getChannel(channelName);
    if (channel != null) {
      String unit = channel.getUnit();
      if (unit != null) {
        seriesName += " (" + unit + ")";
      }
    }
    return seriesName;    
  }
		
  /**
   * Called when the global time scale changes. This updates the maximum age of the
   * dataset.
   * 
   * @param newTimeScale  the new time scale
   */
  public void globalTimeScaleChanged(double timeScale) {
    super.globalTimeScaleChanged(timeScale);
    if(localTimeScale_==TimeScale.TIME_SCALE_UNDEFINED){
      ageDataOnTimeScaleChange();
    }
  }  
    
  private void ageDataOnTimeScaleChange(){
    
    SwingUtilities.invokeLater(new Runnable() {
      final double localScale = localTimeScale_;
      public void run() {

//        /* update UI to reflect changing timescale */
//        Enumeration<AbstractButton> iter = timeRangeGroup.getElements();
//        while (timeRangeGroup.getElements().hasMoreElements()) {
//          AbstractButton b = iter.nextElement();
//          double btnValue = Double.parseDouble(b.getActionCommand());
//          if(localScale==btnValue){
//            timeRangeGroup.setSelected(b.getModel(), true);
//            break;
//          }
//        }
        
        int series = dataCollection.getSeriesCount();
        if (xyMode) {
          series -= localSeries;
        }
        
    		for (int i=0; i<series; i++) {
    			if (xyMode) {
    				XYTimeSeriesCollection xyTimeSeriesCollection = (XYTimeSeriesCollection)dataCollection;
    				XYTimeSeries data = xyTimeSeriesCollection.getSeries(i);
            data.setMaximumItemAge((long)(timeScale*1000), (long)(time*1000));
    			} else {
    				TimeSeriesCollection timeSeriesCollection = (TimeSeriesCollection)dataCollection;
    				FastTimeSeries data = (FastTimeSeries)timeSeriesCollection.getSeries(i);
    				data.setMaximumItemAge((long)(timeScale*1000), (long)(time*1000));
    			}
    		}
        
        if (!xyMode) {
          domainAxis.setRange((time-timeScale)*1000, time*1000);
          ((FixedAutoAdjustRangeDateAxis)domainAxis).setAutoAdjustRange((time-timeScale)*1000, time*1000);
        }        
      }
    });		
	}
  
  /**
   * Posts new data to the data panel.
   * 
   * @param channelMap  the channel map with the new data
   */
	public void postData(final SubscriptionResponse r){
	    if(r==null) return;
	    final double now =RBNBController.getInstance().getLocation();
	    this.time=now;
	    lastDataPostSysTime=System.currentTimeMillis();
	    
      Runnable run = new Runnable() {
            //private double time=ChartViz.this.time;
            private SubscriptionResponse resp=r;
            //SubscriptionResponse resp=r;
            public void run() {
              //ChartViz.this.subResponse_=resp;
              if(r.containsHistory()){
                //log.debug("received "+DataViewer.formatDate(time)+" before "+DataViewer.formatDate(this.time));
                clearData();
              }
              
              // must set time axis before adding data to ensure
              // auto ranging is properly calculated
              setTimeAxis(now);
              
              if (xyMode) {
                postDataXY(resp); 
              } else {
                postDataTimeSeries(resp);              
              }
              
              //resp.getTimeSeries().free(ChartViz.this);        
            //if(resp.freeResult(ChartViz.this)){
              //ChartViz.this.subResponse_=null;
            //}
         }
      };

      SwingUtilities.invokeLater(run);
	}

  /**
   * Posts the data in the channel map when in time series mode.
   * 
   * @param resp the response with the new data
   */
	private void postDataTimeSeries(SubscriptionResponse resp) {
	  postDataCount=0L;
	  postDataStartTime=Double.MAX_VALUE;
	  postDataEndTime=0.0;
	  long startTimeMillis=System.currentTimeMillis();
	  
	  chart.setNotify(false);
	  VizSeriesList vsl = getSeries();
	  
		//loop over all channels and see if there is data for them
		for (VisualizationSeries s : vsl) {
			TimeSeriesData tsd = resp.getTimeSeries(s);
			if (tsd == null) continue;
			
			//if there is data for channel, post it
		  //if(tsd.hasChannel(channelName)){
				postDataTimeSeries(tsd, s);
			//}
		}
		log.debug("copied "+postDataCount+" series recs from: "
		          +RBNBTime.formatISO(postDataStartTime)+" to: "
		          +RBNBTime.formatISO(postDataEndTime)+" in "
		          +(System.currentTimeMillis()-startTimeMillis)
		          +" ms");
		startTimeMillis=System.currentTimeMillis();
		chart.setNotify(true);
    chart.fireChartChanged();
    // always return 0
    //log.info("rendered plot in "
    //    +(System.currentTimeMillis()-startTimeMillis)
    //    +" ms");
	}
	
  /**
   * Posts the data in the channel map to the specified channel when in time
   * series mode.
   * 
   * @param channelMap    the channel map containing the new data
   * @param channelName   the name of the channel to post data to
   */
	private void postDataTimeSeries(TimeSeriesData tsd, VisualizationSeries s) {
	  String channelName = s.getChannels().get(0);
	  if(!tsd.hasChannel(channelName)) return;
	  
		TimeSeriesCollection dataCollection = (TimeSeriesCollection)this.dataCollection;
    FastTimeSeries timeSeriesData = (FastTimeSeries)dataCollection.getSeries(channelName);
    if (timeSeriesData == null) {
      log.warn("No chartviz data series with channel name "+channelName);
      return;
    }
		
    //TimeSeriesData respTs=resp.getTimeSeries();
		try {
			//double[] times = channelMap.GetTimes(channelIndex);
			int channelIndex=tsd.getChannelIndex(channelName);
			if(channelIndex<0){
			  //log.debug("time series got null iterator for channel "+channelName);
			}else{
			  List<Double> times=tsd.getTimes(channelIndex);
        Iterator<Double>it=times.iterator();
        int typeID = tsd.getType(channelIndex);
        
			  //chart.setNotify(false);
	      timeSeriesData.startAdd(times.size());
	      
  			while (it.hasNext()){
  			  Double newTime=it.next();
  			  
  			  RBNBTime recTime=new RBNBTime(newTime);
  			  FixedMillisecond time = new FixedMillisecond(recTime.getMillis());
    			switch (typeID) {
    				case ChannelMap.TYPE_FLOAT64:					
    					timeSeriesData.add(time, tsd.getDataAsFloat64(channelIndex,newTime));
    					break;
    				case ChannelMap.TYPE_FLOAT32:
    				  timeSeriesData.add(time, tsd.getDataAsFloat32(channelIndex,newTime));
    				  break;					
    				case ChannelMap.TYPE_INT64:
    				  timeSeriesData.add(time, tsd.getDataAsInt64(channelIndex,newTime));
    					break;
    				case ChannelMap.TYPE_INT32:
    				  timeSeriesData.add(time, tsd.getDataAsInt32(channelIndex,newTime));
              break;
    				case ChannelMap.TYPE_INT16:
    				  timeSeriesData.add(time, tsd.getDataAsInt16(channelIndex,newTime));
              break;					
    				case ChannelMap.TYPE_INT8:					
    				  timeSeriesData.add(time, tsd.getDataAsInt8(channelIndex,newTime));
    					break;					
    				case ChannelMap.TYPE_STRING:
    				case ChannelMap.TYPE_UNKNOWN:
    				case ChannelMap.TYPE_BYTEARRAY:
    					log.error("Got byte array type for channel " + channelName + ". Don't know how to handle.");
    					break;
    			}
    			++postDataCount;
  			}
  			
  			if(times.size()>0){
  			  if(times.get(0)<postDataStartTime)
  			    postDataStartTime=times.get(0);
  			  
  			  if(times.get(times.size()-1)>postDataEndTime)
  			    postDataEndTime=times.get(times.size()-1);
  			}
			}
      timeSeriesData.fireSeriesChanged();
      
			//chart.setNotify(true);
			//chart.fireChartChanged();
		} catch (Exception e) {
			log.error("Problem plotting data for channel " + channelName + ".");
			e.printStackTrace();
		}
	}
	
  /**
   * Posts a new time with no data. Updates time scale of graph if
   * no data has been posted to this chart within the fixed timeout.  
   * This was added for the case where we are monitoring data and
   * only receiving from some sources.  Plots dependent on sources 
   * not providing data should still adjust to show time passing.   
   * 
   * @param time  the new time
   */
	public void postTime(double time) {
	  if ((time>this.time) && 
	      (System.currentTimeMillis()-lastDataPostSysTime)
	          >(ADJUST_TIME_SCALE_TIMEOUT_SEC*1000.0)){
	    log.debug("adjusting chart axis on postTime()");
  		super.postTime(time);
  		final double myTime = time;
  		SwingUtilities.invokeLater(new Runnable() {
  		  public void run(){
  		    setTimeAxis(myTime);
  		  }
  		});
	  }
  }
  
  /**
   * Posts the data in the channel map when in x vs. y mode.
   * 
   * @param resp        new data response
   * @param cachedChannelMap  the cached channel map
   */
  private void postDataXY(SubscriptionResponse resp) {
    postDataCount=0L;
    postDataStartTime=Double.MAX_VALUE;
    postDataEndTime=0.0;
    VizSeriesList vsl = getSeries();
    long startTimeMillis=System.currentTimeMillis();
    
    chart.setNotify(false);
    for (int i=0;i<vsl.size();i++){
      VisualizationSeries s=vsl.get(i);
      postDataXY(resp.getTimeSeries(s),s,i);
    }
    
//    //loop over all channels and see if there is data for them
//    int seriesCount = dataCollection.getSeriesCount()-localSeries;
//    
//    chart.setNotify(false);
//    
//    for (int i=0; i<seriesCount; i++) {
//      postDataXY(resp, i);
//    }
    log.debug("copied "+postDataCount+" xy recs from: "
        +RBNBTime.formatISO(postDataStartTime)+" to: "
        +RBNBTime.formatISO(postDataEndTime)+" in "
        +(System.currentTimeMillis()-startTimeMillis)
        +" ms");
    startTimeMillis=System.currentTimeMillis();
    
    chart.setNotify(true);
    //xySeriesData.fireSeriesChanged();
    chart.fireChartChanged();
    //lastTimeDisplayed = time;
    
    // always returns 0
    //log.info("rendered plot in "
    //    +(System.currentTimeMillis()-startTimeMillis)
    //    +" ms");
  }  

  /**
   * Posts the data in the channel map to the specified channel when in x vs. y
   * mode.
   * 
   * @param tsd        new data response
   * @param series    data series
   * @param seriesIndex            the index of the series
   */
	private void postDataXY(TimeSeriesData tsd, VisualizationSeries series, int seriesIndex) {
		if (!xyMode) {
			log.error("Tried to post X vs. Y data when not in xy mode.");
			return;
		}
		
		
//		if (resp == null) {
//      log.debug("postXY channel map null");
//			//no data to display yet
//			return;
//		}
//		TimeSeriesData tsData=resp.getTimeSeries();
		
		if (tsd==null || !tsd.hasData()) {
      log.debug("postXY no data to graph");
      //no data to display yet
      return;
    }
		
		//System.out.print("series:"+series+", chans:");
		//for(String chanName: channels){
		//  System.out.print(chanName+",");
		//}
		//log.debug();
		
		// not enuff channels in series yet
		if(series.getChannels().size()<2) return;
		
		//Object[] channelsArray = channels.toArray();
		String xChannelName = series.getChannels().get(0);//(String)channelsArray[series*2];
		String yChannelName = series.getChannels().get(1);//(String)channelsArray[series*2+1];
		
		//get the channel indexes for the  x and y channels
    int xChannelIndex = tsd.getChannelIndex(xChannelName);
    int yChannelIndex = tsd.getChannelIndex(yChannelName);
    
    if(xChannelIndex<0 || yChannelIndex<0) {
      log.warn("postXY channels not found in time series - post data cancelled");
      return;
    }
    
    List<Double> xTimes=tsd.getTimes(xChannelIndex);
    List<Double> yTimes=tsd.getTimes(yChannelIndex);
    
    if(xTimes.size()==0 || yTimes.size()==0){
      log.warn("postXY channels have no time series data - post data cancelled");
      return;
    }
		boolean xIsDriver=false;
    List<Double> driverTimes;
    if(xTimes.size()<yTimes.size()){
      driverTimes=xTimes;
      xIsDriver=true;
    }else{
      driverTimes=yTimes;
    }
		
		try {      
		  //log.debug("postXY graphing from  "+DataViewer.formatDateSmart(dataStartTime)+" to "+DataViewer.formatDateSmart(times[endIndex]));
			XYTimeSeriesCollection dataCollection = (XYTimeSeriesCollection)this.dataCollection;
      XYTimeSeries xySeriesData = (XYTimeSeries)dataCollection.getSeries(seriesIndex);
      
			int yTypeID = tsd.getType(yChannelIndex);
			int xTypeID = tsd.getType(xChannelIndex);
			
			//chart.setNotify(false);
//			if(tsd.size(yChannelIndex) != tsd.size(xChannelIndex)){
//			  log.warn("xy chart data size mismatch!");
//			}
			log.debug("start graphing "+(tsd.size(yChannelIndex)+" points on series "+series));
      xySeriesData.startAdd(driverTimes.size());
      
      //List<Double> yTimes=tsd.getTimes(yChannelIndex);
      Iterator<Double> drvTimeIt=driverTimes.iterator();
      
			while (drvTimeIt.hasNext()){
			  Double drvTime=drvTimeIt.next();
			  //TimeSeriesRecord xRec=xIt.next();
			  //if(xRec.getTime()!=yRec.getTime()){
			  //  log.warn("xy plot time out of sync!");
			  //}
			  Double xTime,yTime;
			  if(xIsDriver){
			    xTime=drvTime;
			    yTime=tsd.getTimeNearest(yChannelIndex,xTime);
			  }else{
			    yTime=drvTime;
          xTime=tsd.getTimeNearest(xChannelIndex,yTime);
			  }
			  //log.info("xt: "+DataViewer.formatDate(xTime)+", yt: "+DataViewer.formatDate(yTime));
			  
			  if(xTime==null || yTime==null){
			    log.warn("postXY no time for one or more channels");
			    continue;
			  }
			  
			  RBNBTime rbnbTime=new RBNBTime(drvTime);
        FixedMillisecond time=new FixedMillisecond(rbnbTime.getMillis());
  			Number xData=null,yData=null;		
  			switch (xTypeID) {
  				case ChannelMap.TYPE_FLOAT64:
  					xData = tsd.getDataAsFloat64(xChannelIndex,xTime);
  					break;
  				case ChannelMap.TYPE_FLOAT32:
  					xData = tsd.getDataAsFloat32(xChannelIndex,xTime); 
  					break;					
  				case ChannelMap.TYPE_INT64:
  					xData = tsd.getDataAsInt64(xChannelIndex,xTime); 
  					break;
  				case ChannelMap.TYPE_INT32:
  					xData = tsd.getDataAsInt32(xChannelIndex,xTime); 
  					break;
  				case ChannelMap.TYPE_INT16:
  					xData = tsd.getDataAsInt16(xChannelIndex,xTime);  
  					break;					
  				case ChannelMap.TYPE_INT8:
  					xData = tsd.getDataAsInt8(xChannelIndex,xTime); 
  					break;					
  				case ChannelMap.TYPE_BYTEARRAY:					
  				case ChannelMap.TYPE_STRING:
  				case ChannelMap.TYPE_UNKNOWN:
  					log.error("Don't know how to handle data type for " + xChannelName + ".");
  					break;
  			}
  			switch (yTypeID) {
        case ChannelMap.TYPE_FLOAT64:
          yData = tsd.getDataAsFloat64(yChannelIndex,yTime);
          break;
        case ChannelMap.TYPE_FLOAT32: 
          yData = tsd.getDataAsFloat32(yChannelIndex,yTime);
          break;          
        case ChannelMap.TYPE_INT64: 
          yData = tsd.getDataAsInt64(yChannelIndex,yTime);
          break;
        case ChannelMap.TYPE_INT32: 
          yData = tsd.getDataAsInt32(yChannelIndex,yTime);
          break;
        case ChannelMap.TYPE_INT16:  
          yData = tsd.getDataAsInt16(yChannelIndex,yTime);
          break;          
        case ChannelMap.TYPE_INT8: 
          yData = tsd.getDataAsInt8(yChannelIndex,yTime);
          break;          
        case ChannelMap.TYPE_BYTEARRAY:         
        case ChannelMap.TYPE_STRING:
        case ChannelMap.TYPE_UNKNOWN:
          log.error("Don't know how to handle data type for " + yChannelName + ".");
          break;
  			}
  			//FIXME null is not a valid return value for the getData methods now
  			// need some way to verify both data channels have data at same time
  			// and perhaps provide a way to match closest data point if they don't
  			if(xData!=null && yData !=null){
  			  xySeriesData.add(time, xData, yData, false);
  			  ++postDataCount;
  			}else{
  			  log.warn("data not synced for plot x: "+xChannelName+" y: "+yChannelName+" at:"+rbnbTime.formatISO());
  			}
			}
      xySeriesData.fireSeriesChanged();
//			
//			chart.setNotify(true);
//			//xySeriesData.fireSeriesChanged();
//			chart.fireChartChanged();
      if(driverTimes.size()>0){
        if(driverTimes.get(0)<postDataStartTime)
          postDataStartTime=driverTimes.get(0);
        
        if(driverTimes.get(driverTimes.size()-1)>postDataEndTime)
          postDataEndTime=driverTimes.get(driverTimes.size()-1);
      }
      
//			if(driverTimes.size()>0){
//		    log.debug("xy chart graphed "+driverTimes.size()+" recs between "+
//		    RBNBTime.formatISO(driverTimes.get(0))+" and "+
//		    RBNBTime.formatISO(driverTimes.get(driverTimes.size()-1)));
//			}else{
//			  log.warn("xy chart graphed 0 recs");
//			}
      
		} catch (Exception e) {
			log.error("Problem plotting data for channels " + xChannelName + " and " + yChannelName + ".");
			e.printStackTrace();
		}

	}
	
	/**
   * Sets the time axis to display within the current time and time scale. This
   * assumes it is called in the event dispatch thread.
   */
  private void setTimeAxis(double end) {
    if (chart == null) {
      log.warn("Chart object is null. This shouldn't happen.");
      return;
    }
    
    //Object[] channelsArray = channels.toArray();
    RBNBTime endTime=new RBNBTime(end);
    RBNBTime startTime=new RBNBTime(end-timeScale);
    //log.debug("time axis start: "+startTime.formatISO()+" end:"+endTime.formatISO());
    
    int series = dataCollection.getSeriesCount();
    if (xyMode) {
      series -= localSeries;
    }
    for (int i=0; i<series; i++) {
      
//      //FIXME access to channelsArray causes an array index error on AWT thread
//      String xChannelName = (String)channelsArray[i*2];
//      String yChannelName = (String)channelsArray[i*2+1];
//      int xIndex=resp.getTimeSeries().getChannelIndex(xChannelName);
//      int yIndex=resp.getTimeSeries().getChannelIndex(yChannelName);
//      
      //RBNBTime maxLast=new RBNBTime(Math.max(resp.getTimeSeries().getEndTime(xIndex),
      //                      resp.getTimeSeries().getEndTime(yIndex)));
      
      if (xyMode) {
        XYTimeSeriesCollection xyTimeSeriesDataCollection = (XYTimeSeriesCollection)dataCollection;
        XYTimeSeries data = xyTimeSeriesDataCollection.getSeries(i);
        //log.debug("aging graph items at "+endTime.formatISO());
        data.removeAgedItems(endTime.getMillis());        
      } else {
        TimeSeriesCollection timeSeriesDataCollection = (TimeSeriesCollection)dataCollection;
        TimeSeries data = timeSeriesDataCollection.getSeries(i);
        //log.debug("aging graph items at "+endTime.formatISO());
        data.removeAgedItems(endTime.getMillis(), true);
      }
    }
    
    if (!xyMode) {
      domainAxis.setRange(startTime.getMillis(), endTime.getMillis());
      ((FixedAutoAdjustRangeDateAxis)domainAxis).setAutoAdjustRange(startTime.getMillis(), 
                                                                     endTime.getMillis());
    }
  } 
  
  /**
   * Sets the time axis to display within the current time and time scale. This
   * assumes it is called in the event dispatch thread.
   */
//	private void setTimeAxis(SubscriptionResponse resp) {
//		
//    TimeSeriesData tsd=resp.getTimeSeries();
//    if(!tsd.hasData())return;
//    setTimeAxis(tsd.getMaxEndTime());
//	}	
	
  /**
   * Removes all data from all the series.
   */
	void clearData() {
		if (chart == null) return;

		//lastTimeDisplayed = -1;
    
    int series = dataCollection.getSeriesCount();
    if (xyMode) {
      series -= localSeries;
    }          

	  for (int i=0; i<series; i++) {
		  if (xyMode) {
			  XYTimeSeriesCollection xyTimeSeriesDataCollection = (XYTimeSeriesCollection)dataCollection;
			  XYTimeSeries data = xyTimeSeriesDataCollection.getSeries(i);
			  data.clear();
		  } else {
			  TimeSeriesCollection timeSeriesDataCollection = (TimeSeriesCollection)dataCollection;
			  TimeSeries data = timeSeriesDataCollection.getSeries(i);
			  data.clear();				
		  }
	  }
	  
		log.info("Cleared data display.");
	}
  
  /**
   * Sets properties for the data panel.
   * 
   * @param key    the key for the property
   * @param value  the value for the property
   */
  public void setProperty(String key, String value) {
    super.setProperty(key, value);
    
    if (key != null && value != null) {
      if (key.equals("domainLowerBound")) {
        domainAxis.setLowerBound(Double.parseDouble(value));
      } else if (key.equals("domainUpperBound")) {
        domainAxis.setUpperBound(Double.parseDouble(value));
      } else if (key.equals("rangeLowerBound")) {
        log.debug("setting range lower bound: "+value);
        rangeAxis.setLowerBound(Double.parseDouble(value));
      } else if (key.equals("rangeUpperBound")) {
        log.debug("setting range upper bound: "+value);
        rangeAxis.setUpperBound(Double.parseDouble(value));
      } else if (key.equals(DATA_PANEL_PROPERTY_SHOW_LEGEND) && !Boolean.parseBoolean(value)) {
        setShowLegend(false);
      }
    }
  }
	
  /**
   * Get the name of this data panel.
   */
	public String toString() {
		return "JFreeChart Data Panel";
	}
}