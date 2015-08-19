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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/viz/dial/DialViz.java $
 * $Revision: 1282 $
 * $Date: 2008-11-26 09:08:41 -0500 (Wed, 26 Nov 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.viz.dial;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import org.rdv.data.Channel;
import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;
import org.rdv.datapanel.AbstractDataPanel;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBDataRequest;
import org.rdv.rbnb.SubscriptionRequest;
import org.rdv.rbnb.SubscriptionResponse;
import org.rdv.rbnb.TimeSeriesData;

import com.rbnb.sapi.ChannelMap;

/**
 * A visualization extension to view a numeric data channel on a dial.
 * 
 * @author  Jason P. Hanley
 * @see     DialModel
 * @see     DialPanel
 */
public class DialViz extends AbstractDataPanel {

  /** the model for the dial */
  private DialModel model;
  
  private DialPanel panel_;
  
  /**
   * Creates a DialViz with no channels.
   */
  public DialViz() {
    super();
    
    model = new DialModel();
    panel_ = new DialPanel(this, model);

    setDataComponent(panel_);
  }

  public SubscriptionRequest getSubscriptionRequest(){
    return new RBNBDataRequest(this,
        SubscriptionRequest.TYPE_TABULAR,timeScale,getSeries());
  }
  
  public boolean supportsMultipleChannels() {
    return false;
  }
  
  @Override
  protected void onSeriesAdded(VisualizationSeries vs){
    String channelName = vs.getChannels().iterator().next();
    model.setName(channelName);
    
    Channel channel = RBNBController.getInstance().getChannel(channelName);
    String unit = (channel != null) ? channel.getUnit() : null;
    model.setUnit(unit);
  }
  
  @Override
  protected void onSeriesRemoved(VisualizationSeries vs){
    model.setValue(null);
    model.setName(null);
    model.setUnit(null);
  }
  
  @Override
  public void postData(final SubscriptionResponse r) {
    if (r == null) {
      return;
    }
    
    if (r.containsHistory()) {
      model.setValue(null);
    }
    
//    TimeSeriesData tsd=r.getTimeSeries();

//    Iterator<String> i = channels.iterator();
//    if (!i.hasNext()) {
//      return;
//    }
//
//    String channelName = i.next();
    VizSeriesList vsl=getSeries();
    if(vsl.size()==0)return;
    
    VisualizationSeries s = vsl.get(0);
    String channelName=s.getChannels().get(0);
    TimeSeriesData tsd=r.getTimeSeries(s);
    if(tsd==null) return;
    
    int channelIndex = tsd.getChannelIndex(channelName);

    // if there is data for channel, post it
    if (channelIndex >= 0) {
      postDataDial(tsd, channelName, channelIndex);
    }
  }
  
  /**
   * Posts data for the specified channel to the dial. This will get the data
   * point closest to the current timestamp and set value of the dial to this.
   * 
   * @param channelName   the name of the data channel
   * @param channelIndex  the index of the data channel
   */
  private void postDataDial(TimeSeriesData tsd, String channelName, 
                            int channelIndex) {
//    ChannelMap channelMap=subResponse_.getChannelMap();
//    double[] times = channelMap.GetTimes(channelIndex);
//    
//    int valueIndex = -1;
//    for (int i = times.length - 1; i >= 0; i--) {
//      if (times[i] <= time) {
//        valueIndex = i;
//        break;
//      }
//    }
//    
//    if (valueIndex == -1) {
//      return;
//    }
    Double lastTime=tsd.getEndTime(channelIndex);
    if (lastTime==null) return;
    
    double value;
    int typeID = tsd.getType(channelIndex);

    switch (typeID) {
    case ChannelMap.TYPE_FLOAT64:
      value = tsd.getDataAsFloat64(channelIndex,lastTime);
      break;
    case ChannelMap.TYPE_FLOAT32:
      value = tsd.getDataAsFloat32(channelIndex,lastTime);
      break;
    case ChannelMap.TYPE_INT64:
      value = tsd.getDataAsInt64(channelIndex,lastTime);
      break;
    case ChannelMap.TYPE_INT32:
      value = tsd.getDataAsInt32(channelIndex,lastTime);
      break;
    case ChannelMap.TYPE_INT16:
      value = tsd.getDataAsInt16(channelIndex,lastTime);
      break;
    case ChannelMap.TYPE_INT8:
      value = tsd.getDataAsInt8(channelIndex,lastTime);
      break;
    case ChannelMap.TYPE_STRING:
    case ChannelMap.TYPE_UNKNOWN:
    case ChannelMap.TYPE_BYTEARRAY:
    default:
      return;
    }
    
    model.setValue(value);
  }
  
  /**
   * Build dynamic popup menu for the chart. 
   */
  public void buildPopupMenu(JPopupMenu result, MouseEvent e) {
    super.buildPopupMenu(result, e);
    panel_.buildPopupMenu(result, e);
  }
  
}