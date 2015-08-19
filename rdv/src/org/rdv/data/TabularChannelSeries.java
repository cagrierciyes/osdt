
package org.rdv.data;

public class TabularChannelSeries extends ChannelSeries {
  private int columnId_=0;
  
  public TabularChannelSeries(String channelName, int columnId){
    super(channelName);
    columnId_=columnId;
  }
  
  public int getColumnId(){
    return columnId_;
  }
}
