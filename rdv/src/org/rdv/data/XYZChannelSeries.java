
package org.rdv.data;

public class XYZChannelSeries extends ChannelSeries {
  public XYZChannelSeries(String channelName){
    // build a new series
    super(channelName);
    formatter_=new XYSeriesFormatter();
  }
  
  private void getChannelComponents(String channelName, 
            String sensorName, String dimensionName){
    
    int lastIndex = channelName.lastIndexOf('_');
    if(lastIndex>=0){
      sensorName=channelName.substring(0, lastIndex-1);
      dimensionName=channelName.substring(lastIndex+1).toLowerCase();
    }else{
      sensorName=channelName;
      dimensionName="x";
    }
  }
  
  public boolean addChannel(String channelName){
    if(hasAllChannels()){
      return false;
    }
    
    String newSensor="", newDimension="";
    getChannelComponents(channelName,newSensor,newDimension);
    
    boolean add = true;
    String sensorName="", dimensionName="";
    for(String channel : channels_){
      getChannelComponents(channel,sensorName,dimensionName);
      if(!sensorName.equalsIgnoreCase(newSensor) || 
        newDimension.equalsIgnoreCase(dimensionName)){
        add=false;
        break;
      }
    }
    
    return (add)?super.addChannel(channelName):false; 
  }
  
  public boolean hasAllChannels(){
    return (channels_.size() == 3);
  }
}
