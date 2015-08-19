
package org.rdv.data;

import java.util.Iterator;

public class BasicSeriesFormatter implements SeriesFormatter {

  @Override
  public String formatDisplayName(ChannelSeries channelSeries) {
    StringBuilder sb = new StringBuilder();
    Iterator<String> chanIt = channelSeries.getChannels().iterator();
    while(chanIt.hasNext()){
      sb.append(chanIt.next());
      if(chanIt.hasNext()){
        sb.append('/');
      }
    }
    
    return sb.toString();
  }

  public String formatName(ChannelSeries channelSeries) {
    return formatDisplayName(channelSeries);
  }
}
