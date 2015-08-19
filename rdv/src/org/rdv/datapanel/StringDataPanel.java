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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/datapanel/StringDataPanel.java $
 * $Revision: 1288 $
 * $Date: 2008-11-26 09:44:03 -0500 (Wed, 26 Nov 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.datapanel;

import java.awt.BorderLayout;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;
import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;
import org.rdv.rbnb.SubscriptionResponse;
import org.rdv.rbnb.TimeSeriesData;

import com.rbnb.sapi.ChannelMap;

/**
 * @author Jason P. Hanley
 */
public class StringDataPanel extends AbstractDataPanel {

	static Log log = LogFactory.getLog(StringDataPanel.class.getName());
		
	JPanel panel;
	JEditorPane messages;
	JScrollPane scrollPane;
  StringBuffer messageBuffer;
  
  String[] AVAILABLE_COLORS = {"blue", "green", "maroon", "purple", "red", "olive"};
  Hashtable<String, String> colors;
	
	double lastTimeDisplayed;
	
	public StringDataPanel() {
		super();
		
		lastTimeDisplayed = -1;
    messageBuffer = new StringBuffer();
    
    colors = new Hashtable<String, String>();
				
		initPanel();
		setDataComponent(panel);
		
		messages.addMouseListener(getPopupMenuMouseListener());
	}
  
	private void initPanel() {
		panel = new JPanel();
		
		panel.setLayout(new BorderLayout());
				
		messages = new JEditorPane();
		messages.setEditable(false);
    messages.setContentType("text/html");
		scrollPane = new JScrollPane(messages,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane, BorderLayout.CENTER);
	}
  
//	public boolean addChannels(Collection<String> channelNames) {
//	  int i=channels.size();
//	  
//	  if(super.addChannels(channelNames)){
//	    
//  	  for(String name:channelNames){
//  	    colors.put(name, AVAILABLE_COLORS[i%AVAILABLE_COLORS.length]);
//  	    ++i;
//  	  }
//	  }else{
//	    return false;
//	  }
//	  
//	  return true;
//	}
	
	protected void onSeriesAdded(VisualizationSeries vs){
	  colors.put(vs.getChannels().iterator().next(), 
	      AVAILABLE_COLORS[(seriesList_.size()-1)%AVAILABLE_COLORS.length]);
	}
  
	protected void onSeriesRemoved(VisualizationSeries vs){
	  colors.remove(vs.getChannels().iterator().next());
	}
  
	
	public boolean supportsMultipleChannels() {
		return true;
	}
	
	public void postTime(double time) {
	  super.postTime(time);
	}
	
	public void postData(final SubscriptionResponse r) {
	  if (r==null)return;
	  
    //ChannelMap channelMap=subResponse_.getChannelMap();
    //if (time < this.time) {
    if(r.containsHistory()){
      clearData();
    }
		
		//if (channelMap == null) {
			//no data to display yet
		//	return;
		//}
  //loop over all series and see if there is data for them
    VizSeriesList vsl = getSeries();
    for(VisualizationSeries s:vsl){
      TimeSeriesData tsd=r.getTimeSeries(s);
      
       String channelName = s.getChannels().get(0);
       int channelIndex = tsd.getChannelIndex(channelName);
        
       //if there is data for channel, post it
       if (channelIndex >= 0) {
           postDataText(tsd, channelName, channelIndex);
       }
    }
      
//    TimeSeriesData tsd=r.getTimeSeries();
//		//loop over all channels and see if there is data for them
//		Iterator<String> i = channels.iterator();
//		while (i.hasNext()) {
//			String channelName = i.next();
//			int channelIndex = tsd.getChannelIndex(channelName);
//			
//			//if there is data for channel, post it
//			if (channelIndex >= 0) {
//          postDataText(tsd, channelName, channelIndex);
//      }
//		}
    
    lastTimeDisplayed = time;
    
//    if(tsd.free(this)){
//      subResponse_=null;
//      //channelMap.Clear();
//      //channelMap = null;
//    }
	}

	private void postDataText(TimeSeriesData tsd, String channelName, 
	                          int channelIndex) {
            
    //We only know how to display strings
    if (tsd.getType(channelIndex) != ChannelMap.TYPE_STRING) {
      return;
    }

    String shortChannelName = channelName.substring(
                            channelName.lastIndexOf('/')+1);
    String channelColor = colors.get(channelName);
    
    Iterator<Double>timeIt=tsd.getTimes(channelIndex).iterator();
    while (timeIt.hasNext()){
      
    Double newTime=timeIt.next();
    String data = tsd.getDataAsString(channelIndex,newTime);
		//double[] times = channelMap.GetTimes(channelIndex);

//		int startIndex = -1;
//		
//		for (int i=0; i<times.length; i++) {
//			if (times[i] > lastTimeDisplayed && times[i] <= time) {
//				startIndex = i;
//				break;
//			}
//		}
//		
//		//see if there is no data in the time range we are loooking at
//		if (startIndex == -1) {
//			return;
//		}		
//
//		int endIndex = startIndex;
//		
//		for (int i=times.length-1; i>startIndex; i--) {
//			if (times[i] <= time) {
//				endIndex = i;
//				break;
//			}
//		}

//		for (int i=startIndex; i<=endIndex; i++) {
			messageBuffer.append("<strong style=\"color: " +
			                  channelColor + "\">" + shortChannelName + 
			                  "</strong> (<em>" + 
			                  DataViewer.formatDateSmart(newTime)+ 
			                  "</em>): " + data + "<br>");
		}
    messages.setText(messageBuffer.toString());

    int max = scrollPane.getVerticalScrollBar().getMaximum();
    scrollPane.getVerticalScrollBar().setValue(max);
	}
	
	void clearData() {
		messages.setText(null);
    messageBuffer.delete(0, messageBuffer.length());
		lastTimeDisplayed = -1;
	}
	
	public String toString() {
		return "Text Data Panel";
	}
}