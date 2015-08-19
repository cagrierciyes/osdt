
package org.rdv.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class VizSeriesList extends CopyOnWriteArrayList<VisualizationSeries>{
  
  public VizSeriesList(){
    super();
  }
  
  public VizSeriesList(VizSeriesList l){
    super(l);
  }
  
  public VizSeriesList(VisualizationSeries series){
    super();
    add(series);
  }
  
  public boolean add(VisualizationSeries series){
    // must ensure series name is unique
//    for(VisualizationSeries s:series_){
//      if(s.getName().compareToIgnoreCase(series.getName())==0){
//        throw new IllegalArgumentException();
//      }
//    }
    if(!contains(series)) 
      return super.add(series);
    else
      return false;
  }
  
  /**
   * 
   * @param channelName
   * @return count of series containing the channel.
   */
  public int seriesContainerCount(String channelName){
    int count=0;
    for(VisualizationSeries vizSeries: this){
      if (vizSeries.containsChannel(channelName)){
        ++count;
      }
    }
    return count;
  }
  
  public boolean containsChannel(String channelName){
    for(VisualizationSeries vizSeries: this){
      if (vizSeries.containsChannel(channelName)){
        return true;
      }
    }
    return false;
  }
  
  public VisualizationSeries getByName(String seriesName){
    for(VisualizationSeries s:this){
      if(s.getName().compareToIgnoreCase(seriesName)==0){
        return s;
      }
    }
    return null;
  }
  
  public String[] getDisplayNames(){
    String ret[]=new String[size()];
    for(int i=0;i<ret.length;i++){
      ret[i]=get(i).getDisplayName();
    }
    return ret;
  }
  
  public List<String> getChannels(){
    List<String> ret = new ArrayList<String>();
    for(VisualizationSeries vizSeries: this){
      for(String channel: vizSeries.getChannels()){
        if(!ret.contains(channel)){
          ret.add(channel);
        }
      }
    }
    return ret;
  }
  
//  public List<String> getServerChannels(){
//    List<String> ret = new ArrayList<String>();
//    for(VisualizationSeries vizSeries: series_){
//      for(String channel: vizSeries.getServerChannels()){
//        if(!ret.contains(channel)){
//          ret.add(channel);
//        }
//      }
//    }
//    return ret;
//  }
}
