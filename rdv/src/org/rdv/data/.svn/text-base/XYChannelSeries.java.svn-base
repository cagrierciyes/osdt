
package org.rdv.data;

public class XYChannelSeries extends ChannelSeries {
  public XYChannelSeries(String channelName){
    // build a new series
    super(channelName);
    formatter_=new XYSeriesFormatter();
  }
  
  public boolean addChannel(String channelName){
    if(channels_.size() > 1){
      return false;
    }
    return super.addChannel(channelName); 
  }
  
  public boolean hasAllChannels(){
    return (channels_.size() == 2);
  }
  
}
