
package org.rdv.rbnb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.ManagerStore;
import org.rdv.data.Channel;
import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;
import org.rdv.datapanel.DataPanel;

/***
 * A request to subscribe or unsubscribe from a set of channels.
 * Each request has one DataListener that is typically a DataPanel
 * interested in receiving data for visualization.
 * @author Drew Daugherty
 */
public class RBNBDataRequest implements SubscriptionRequest {
  static Log log = LogFactory.getLog(RBNBDataRequest.class.getName());
  private int type_;
  private double range_;
  private double offset_;
  //private List<String> chans_=new ArrayList<String>();
  private VizSeriesList series_;
  private DataListener requestor_;
  //private boolean serviced_=false;
  private boolean subscribe_=true;
  
  public static RBNBDataRequest createImageRequest(DataListener l, 
                                                  VizSeriesList seriesList){
    RBNBDataRequest r = new RBNBDataRequest(l,TYPE_IMAGE,0,seriesList);
//    Iterator<String> it=chans.iterator();
//    while(it.hasNext()){
//      r.addChannel(it.next());
//    }
    return r;
  }
  
  public static RBNBDataRequest createTabularRequest(DataListener l, 
                                                  VizSeriesList seriesList){
    RBNBDataRequest r = new RBNBDataRequest(l,TYPE_TABULAR,1,seriesList);
//    Iterator<String> it=chans.iterator();
//    while(it.hasNext()){
//      r.addChannel(it.next());
//    }
    return r;
  }
  
  public static RBNBDataRequest createChartRequest(DataListener l, double range, 
                                                    VizSeriesList seriesList){
    RBNBDataRequest r = new RBNBDataRequest(l,TYPE_CHART,range,seriesList);
//    Iterator<String> it=chans.iterator();
//    while(it.hasNext()){
//      r.addChannel(it.next());
//    }
    return r;
  }
  
  /*public static RBNBDataRequest createUnsubscribeRequest(DataListener l,
                                                  Collection<String> chans){
    RBNBDataRequest r = new RBNBDataRequest(l,TYPE_UNSUBSCRIBE,0);
    Iterator<String> it=chans.iterator();
    while(it.hasNext()){
      r.addChannel(it.next());
    }
    return r;
  }*/
  
  public RBNBDataRequest(SubscriptionRequest r, VizSeriesList seriesList){
    this(r.getListener(),r.getType(),r.getTimeRange(),seriesList);
    //MetadataManager mm = RBNBController.getInstance().getMetadataManager();
//    Iterator<String> it = newChans.iterator();
//    while(it.hasNext()){
//      chans_.add(it.next());
//    }
  }
  
  public RBNBDataRequest(DataListener l,int type,double range, 
                        VizSeriesList seriesList){
    requestor_=l;
    type_=type;
    range_=range;
    series_=seriesList;
    
    switch (type_){
      case TYPE_IMAGE:
        offset_=0;
        range_=0;
        break;
      case TYPE_TABULAR:
        offset_=1;
        range_=1;
        break;
      default:
        offset_=range_;
    }
  }
  
  //public void serviced(){serviced_=true;}
  //public boolean isServiced(){return serviced_;}

  public void setUnsubscribe(){
      subscribe_=false;
  }

  public List<String> getRDVChannelNameList() {
    return series_.getChannels();
  }
  
  public String toString(){
    StringBuffer retBuf=new StringBuffer();
    
    retBuf.append((subscribe_)?"Sub ":"Unsub ");
    retBuf.append(" from: ").append(requestor_);
    retBuf.append(" type: ").append(type_);
    retBuf.append(" range: ").append(range_);
    retBuf.append(" chans: ");
    Iterator<String> chanIt=getServerChannelNameList().iterator();
    while (chanIt.hasNext()){
      retBuf.append(chanIt.next());
      if(chanIt.hasNext())retBuf.append(",");
    }
    return retBuf.toString();
  }
  
  public List<String> getServerChannelNameList() {
    List<String> l=new ArrayList<String>();
    Iterator<String> it = getRDVChannelNameList().iterator();
    MetadataManager mm = RBNBController.getInstance().getMetadataManager();
    
    while (it.hasNext()){
      String channelName=it.next();
      Channel c=mm.getChannel(channelName);
      if(c!=null){
        Iterator<String> nameIt= c.getServerChannels().iterator();
        while(nameIt.hasNext()){
          String name=nameIt.next();
          //log.debug("adding server channel "+name+" to request");
          if (!l.contains(name)) l.add(name);
        }
      }
    }
    return l;
  }

  public DataListener getListener(){
    return requestor_;
  }

  public int getDisplayPriority(){
      int ret=-1;
      DataPanel panel;

      try{
          panel=(DataPanel)requestor_;
          if(panel != null)
            ret=ManagerStore.getPanelManager().getDisplayPriority(panel);
      }catch(ClassCastException e){}
      
      return ret;
  }

//  public boolean containsChannel(String chanName){
//    return (series_.containsChannel(chanName));
//  }
  
  public double getTimeOffset() {
    return offset_;
  }

  public double getTimeRange() {
    return range_;
  }

  public int getType() {
    return type_;
  }

//  public void notify(QueryResponse r) {
//    requestor_.postData(r);
//  }
  
//  public void addChannel(String name){
//    if(!chans_.contains(name)) chans_.add(name);
//  }
  
//  public void addChannels(List<String> names){
//    Iterator<String> it= names.iterator();
//    while(it.hasNext()){
//      addChannel(it.next());
//    }
//  }
  
  public boolean isSubscribe(){
    return subscribe_;
  }
  
  public boolean hasChannel(String name){
    return series_.containsChannel(name);
//    Iterator<String> it=chans_.iterator();
//    while (it.hasNext()){
//      if(name.compareToIgnoreCase(it.next())==0)
//        return true;
//    }
//    return false;
  }
  
  public SubscriptionRequest combineChannels(SubscriptionRequest r){
    VizSeriesList newList = new VizSeriesList(series_);
    
    Iterator<VisualizationSeries> seriesIt = r.getSeriesList().iterator();
    while(seriesIt.hasNext()){
      VisualizationSeries s = seriesIt.next();
      if(r.isSubscribe()){
        newList.add(s);
      }else{
        newList.remove(s);
      }
    }
        
//    List<String> newChanList = new ArrayList<String>();
//    Iterator<String> mychanit = chans_.iterator();
//    while(mychanit.hasNext()){
//      newChanList.add(mychanit.next());
//    }
//      
//    Iterator<String> it=r.getRDVChannelNameList().iterator();
//    while(it.hasNext()){
//      String combineChan=it.next();
//      if(combineChan==null)continue;
//      
//      if(r.isSubscribe()){
//        boolean found=false;
//        // see if we are already subbed to this channel
//        for(String s:newChanList){
//            if (s.compareToIgnoreCase(combineChan)==0){
//                found=true;
//                break;
//            }
//        }
//        if(!found) newChanList.add(combineChan);
//      }else{
//        newChanList.remove(combineChan);
//      }
//    }

    if(newList.size()==0){
      return null;
    }else{
      return new RBNBDataRequest(r,newList);
    }
  }
  
  @Override
  public boolean equals(Object o){
    
    if(o instanceof RBNBDataRequest){ 
      RBNBDataRequest r = (RBNBDataRequest)o;
      if(r==this){
        return true;
      }else{
        if(r.series_.size() != series_.size()) return false;
        
        Iterator<VisualizationSeries> seriesIt = r.series_.iterator();
        while(seriesIt.hasNext()){
          VisualizationSeries series = seriesIt.next();
          if(!series_.contains(series)){
            return false;
          }
        }
        return ((type_==r.getType())&&
                (range_==r.getTimeRange()) && 
                (offset_==r.getTimeOffset()) &&
                (subscribe_==r.isSubscribe()) &&
                (requestor_==r.getListener()));
      }
    }
    return false;
  }

  @Override
  public VizSeriesList getSeriesList() {
    return series_;
  }

  
}
