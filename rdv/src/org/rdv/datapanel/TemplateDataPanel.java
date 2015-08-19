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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/datapanel/TemplateDataPanel.java $
 * $Revision: 1213 $
 * $Date: 2008-07-14 16:09:03 -0400 (Mon, 14 Jul 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.datapanel;

import java.util.Iterator;

import javax.swing.JComponent;

import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;
import org.rdv.rbnb.SubscriptionResponse;
import org.rdv.rbnb.TimeSeriesData;

import com.rbnb.sapi.ChannelMap;

/**
 * A template for creating a data panel extension. This is the bare minumum
 * needed to get a working data panel (that does nothing).
 * 
 * @author Jason P. Hanley
 */
public class TemplateDataPanel extends AbstractDataPanel {

  /** the last time displayed */
  private double lastTimeDisplayed;

  /**
   * Create the data panel.
   */
  public TemplateDataPanel() {
    super();

    initDataComponent();
  }

  /**
   * Initialize the UI component and pass it too the abstract class.
   */
  private void initDataComponent() {
    // TODO create data component
    JComponent myComponent = null;
    setDataComponent(myComponent);
  }

  public boolean supportsMultipleChannels() {
    // TODO change if this data panel supports multiple channels
    return false;
  }

  public void postData(final SubscriptionResponse r) {
    if (r.containsHistory()) {
      lastTimeDisplayed = -1;

      // TODO clear data in your data component
    }
    
    VizSeriesList vsl = getSeries();
    for(VisualizationSeries s:vsl){
      TimeSeriesData tsd=r.getTimeSeries(s);
      if (tsd==null || !tsd.hasData()) {
        //no data to display yet
        return;
      }
  
      //loop over all channels and see if there is data for them
      for (String channelName : s.getChannels()) {
        int channelIndex = tsd.getChannelIndex(channelName);
  
        //if there is data for channel, post it
        if (channelIndex >= 0) {
          displayData(tsd, channelName, channelIndex);
        }
      }
    }
   
  }

  private void displayData(TimeSeriesData tsd,
                      String channelName, int channelIndex) {
      //ChannelMap channelMap=subResponse_.getChannelMap();
    if (tsd.getType(channelIndex) != ChannelMap.TYPE_FLOAT64) {
      return;
    }
    
    Iterator<Double> times = tsd.getTimes(channelIndex).iterator();
    while (times.hasNext()){
      Double currTime=times.next();
      double data = tsd.getDataAsFloat64(channelIndex,currTime);
      if(!times.hasNext()){
        lastTimeDisplayed = currTime;
      }
      // TODO display the data at this timestamp
    }
   
  }

  @Override
  protected void onSeriesAdded(VisualizationSeries vs) {
    // do series setup here
    
  }

  @Override
  protected void onSeriesRemoved(VisualizationSeries vs) {
    // do series teardown here
    
  }
}