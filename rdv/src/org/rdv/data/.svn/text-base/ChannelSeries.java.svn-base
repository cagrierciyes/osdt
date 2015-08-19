
package org.rdv.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rdv.rbnb.RBNBController;

public class ChannelSeries implements VisualizationSeries{
  protected List<String> channels_=new ArrayList<String>();
  protected SeriesFormatter formatter_=new BasicSeriesFormatter();
  
  public ChannelSeries(String channelName, ChannelSeries series){
    // build a new series
    channels_.addAll(series.channels_);
    addChannel(channelName);
    formatter_=series.formatter_;
  }
  
  public ChannelSeries(String channelName, SeriesFormatter formatter){
    // build a new series
    channels_.add(channelName);
    formatter_=formatter;
  }
  
  public ChannelSeries(String channelName){
    // build a new series
    channels_.add(channelName);
  }
  
  public ChannelSeries(List<String> channelNames, SeriesFormatter formatter){
    // build a new series
    channels_.addAll(channelNames);
    formatter_=formatter;
  }
  
  public List<String> getServerChannels(){
    List<String> ret = new ArrayList<String>();
    
    for(String chanName:channels_){
      Channel c = RBNBController.getInstance().getChannel(chanName);
      if(c==null) continue;
      Iterator<String> schanIt = c.getServerChannels().iterator();
      while(schanIt.hasNext()){
        String schanName = schanIt.next();
        if(!ret.contains(schanName)){
          ret.add(schanName);
        }
      }
    }
    return ret;
  }
  
  public boolean containsChannel(String channelName){
    Iterator<String> chanIt=channels_.iterator();
    while(chanIt.hasNext()){
      if(chanIt.next().equalsIgnoreCase(channelName))return true;
    }
    return false;
  }
  
  public List<String> getChannels(){
    return channels_;
  }

  public boolean addChannel(String channelName){
    if(!channels_.contains(channelName)){
      channels_.add(channelName);
      return true;
    }
    return false;
  }
  
  public boolean hasAllChannels(){
    return true;
  }
  
  public int size(){
    return channels_.size();
  }
  
//  public void removeFromPanel(DataPanel dataPanel){
//    Iterator<String> channelIt = getChannels().iterator();
//    while(channelIt.hasNext()){
//       dataPanel.removeChannel(channelIt.next());
//    }
//  }
  
  public String getDisplayName(){
    return formatter_.formatDisplayName(this);
  }
  
  public String getName(){
    return formatter_.formatName(this);
    
//    StringBuilder sb = new StringBuilder();
//    Iterator<String> it = channels_.iterator();
//    while (it.hasNext()){
//      String channelName = it.next();
//      Channel c = RBNBController.getInstance().getChannel(channelName);
//      sb.append(c.getName());
//      if(it.hasNext())
//        sb.append(',');
//    }
//    return sb.toString();
  }
  
  @Override public int hashCode() {
    StringBuilder b = new StringBuilder();
    for(int i=0;i<channels_.size();i++){
      b.append(channels_.get(i).toLowerCase());
    }
    return b.toString().hashCode();
  }
  
  @Override
  public boolean equals(Object o){
    if(o instanceof VisualizationSeries){
      VisualizationSeries vs = (VisualizationSeries)o;
      List<String>otherChannels = vs.getChannels();
      
      if(channels_.size()!=otherChannels.size()) 
        return false;
      
      for(int i=0;i<channels_.size();i++){
        if(!channels_.get(i).equalsIgnoreCase(otherChannels.get(i))){
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
