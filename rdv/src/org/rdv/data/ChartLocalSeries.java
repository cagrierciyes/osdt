
package org.rdv.data;

import java.util.List;

public class ChartLocalSeries implements VisualizationSeries {

  private String name_;
  
  public ChartLocalSeries (String name){
    name_=name;
  }
  
  @Override
  public boolean containsChannel(String channelName) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public List<String> getChannels() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getDisplayName() {
    
    return name_;
  }

  @Override
  public String getName() {
    
    return name_;
  }

  @Override
  public List<String> getServerChannels() {
    return null;
  }

  @Override
  public boolean hasAllChannels() {
    return true;
  }

  @Override
  public boolean addChannel(String channelName) {
    return false;
  }

}
