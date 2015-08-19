
package org.rdv.data;

import java.util.Iterator;

import org.rdv.rbnb.RBNBController;

public class XYSeriesFormatter implements SeriesFormatter {

  @Override
  public String formatName(ChannelSeries channelSeries) {
    StringBuilder sb = new StringBuilder();
    Iterator<String> chanIt = channelSeries.getChannels().iterator();
    while(chanIt.hasNext()){
      String channelName = chanIt.next();
      sb.append(channelName);
      Channel channel = RBNBController.getInstance().getChannel(channelName);
      if (channel != null) {
        String unit = channel.getUnit();
        if (unit != null) {
          sb.append(" (").append(unit).append(")");
        }
      }
      if(chanIt.hasNext()){
          sb.append(" vs. ");
      }
    }
    return sb.toString();
  }
  
  @Override
  public String formatDisplayName(ChannelSeries channelSeries) {
    StringBuilder sb = new StringBuilder();
    Iterator<String> chanIt = channelSeries.getChannels().iterator();
    while(chanIt.hasNext()){
      sb.append(chanIt.next());
      if(chanIt.hasNext()){
          sb.append(" vs. ");
      }
    }
    return sb.toString();
  }

}
