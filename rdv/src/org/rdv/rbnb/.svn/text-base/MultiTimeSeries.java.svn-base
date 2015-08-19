
package org.rdv.rbnb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/***
 * Rolls multiple time series from one source into one, 
 * taking the union of all times. Time series are acquired
 * from the same source but at different times.  
 * 
 * It is assumed that all time series have the same
 * set of channel names.
 * @author Drew Daugherty
 *
 */
public class MultiTimeSeries implements TimeSeriesData {

  private List<MultiChannel> chanDataMap_=new ArrayList<MultiChannel>();
  private List<TimeSeriesData> tsList_=new ArrayList<TimeSeriesData>(); ///< Time series list, in ascending order.
  //private HashMap<Double,Integer> timeIndex_=new HashMap<Double,Integer>();
  private Double maxTime_=0.0;
  private Double minTime_=Double.MAX_VALUE;
  
  public MultiTimeSeries(TimeSeriesData tsd){
    for(int i=0;i<tsd.getChannelCount();i++){
      MultiChannel mapEntry=new MultiChannel(
                            tsd.getChannelName(i));
      mapEntry.addMapEntry(0, i);
      chanDataMap_.add(mapEntry);
      
      List<Double> times= tsd.getTimes(i);
      if(times.size()>0 && times.get(0)<minTime_)
        minTime_=times.get(0);
    }
    tsList_.add(tsd);
    
    updateMax(tsd);
    
  }
  
  @Override
  public List<String> getChannelNames(){
    if(tsList_.size()>0){
      return tsList_.get(0).getChannelNames();
    }
    List<String> retList=new ArrayList<String>();
    return retList;
  }
  
  /**
   * Add a new time series to end of list.  Assumes time
   * series has later start time than the max end time
   * previously in the list.
   * @param tsd
   * @return
   */
  public boolean addTimeSeries(TimeSeriesData tsd){
    if(tsd.hasData() && namesMatch(tsd)){
      int tsIndex=tsList_.size();
      tsList_.add(tsd);
      updateChanDataMap(tsd,tsIndex);
      updateMax(tsd);
      return true;
    }
    return false;
  }
  
  /***
   * @param tsd
   */
  private void updateMax(TimeSeriesData tsd){
    for(int i=0;i<tsd.getChannelCount();i++){
      List<Double> times= tsd.getTimes(i);
      if(times.size()>0){
        if(times.get(times.size()-1)>maxTime_){
          maxTime_=times.get(times.size()-1);
        }
      }
    }
  }
  
  /**
   * Updates the map from channel names to time series indexes.  
   * If channel name is not in map, create a new map entry.
   * @param tsd
   */
  private void updateChanDataMap(TimeSeriesData tsd, int tsIndex){
    Iterator<MultiChannel> chanIt=chanDataMap_.iterator();
    while(chanIt.hasNext()){
      MultiChannel myChan=chanIt.next();
      int chanIndex=tsd.getChannelIndex(myChan.name_);
      if(chanIndex>=0){
        myChan.addMapEntry(tsIndex, chanIndex);
      }
    }
    for(int i=0;i<tsd.getChannelCount();i++){
      if(!chanDataMap_.contains(tsd.getChannelName(i))){
        MultiChannel myChan=new MultiChannel(tsd.getChannelName(i));
        myChan.addMapEntry(tsIndex,i);
        chanDataMap_.add(myChan);
      }
    }
  }
  
  /***
   * 
   * @param tsd
   * @return true if at least one name matches.
   */
  private boolean namesMatch(TimeSeriesData tsd){  
    for(int i=0;i<tsd.getChannelCount();i++){
      for(MultiChannel mc:chanDataMap_){
        if(mc.nameMatches(tsd.getChannelName(i))){
          return true;
        }
      }
    }
    return false;
  }
  
//  @Override
//  public void addListener(DataListener l) {
//    // TODO Auto-generated method stub
//    
//  }

  @Override
  public void clear() {
    chanDataMap_.clear();
    for(TimeSeriesData tsd: tsList_){
      tsd.clear();
    }
    tsList_.clear();
    //timeIndex_.clear();
    maxTime_=0.0;
    minTime_=Double.MAX_VALUE;
  }

//  @Override
//  public boolean free(DataListener l) {
//    // TODO Auto-generated method stub
//    return false;
//  }

  @Override
  public int getChannelCount() {
    return chanDataMap_.size();
  }

  @Override
  public int getChannelIndex(String channelName) {
    for(int i=0;i<chanDataMap_.size();i++){
      if(chanDataMap_.get(i).nameMatches(channelName)){
        return i;
      }
    }
    return -1;
  }

  @Override
  public String getChannelName(int channelIndex) {
    return chanDataMap_.get(channelIndex).name_;
  }

  private MapEntry getMapEntry(int channelIndex, Double time){
    List<MapEntry> chanEntries=chanDataMap_.get(channelIndex).getDataMap();
    for(MapEntry me:chanEntries){
      TimeSeriesData tsd=tsList_.get(me.tsIndex_);
      if(time>=tsd.getMinStartTime()&&time<=tsd.getMaxEndTime()){
        return me;
      }
    }
    return null;
  }
  
  @Override
  public byte[] getDataAsByteArray(int channelIndex, Double time) {
     MapEntry me=getMapEntry(channelIndex,time);
     if(me!=null){
       TimeSeriesData tsd=tsList_.get(me.tsIndex_);
        return tsd.getDataAsByteArray(me.tsChanIndex_, time);
     }else{
       return null;
     }
  }

  @Override
  public float getDataAsFloat32(int channelIndex, Double time) {
    MapEntry me=getMapEntry(channelIndex,time);
    if(me!=null){
      TimeSeriesData tsd=tsList_.get(me.tsIndex_);
       return tsd.getDataAsFloat32(me.tsChanIndex_, time);
    }else{
      return 0.0f;
    }
  }

  @Override
  public double getDataAsFloat64(int channelIndex, Double time) {
    MapEntry me=getMapEntry(channelIndex,time);
    if(me!=null){
      TimeSeriesData tsd=tsList_.get(me.tsIndex_);
       return tsd.getDataAsFloat64(me.tsChanIndex_, time);
    }else{
      return 0.0;
    }
  }

  @Override
  public short getDataAsInt16(int channelIndex, Double time) {
    MapEntry me=getMapEntry(channelIndex,time);
    if(me!=null){
      TimeSeriesData tsd=tsList_.get(me.tsIndex_);
       return tsd.getDataAsInt16(me.tsChanIndex_, time);
    }else{
      return 0;
    }
  }

  @Override
  public int getDataAsInt32(int channelIndex, Double time) {
    MapEntry me=getMapEntry(channelIndex,time);
    if(me!=null){
      TimeSeriesData tsd=tsList_.get(me.tsIndex_);
       return tsd.getDataAsInt32(me.tsChanIndex_, time);
    }else{
      return 0;
    }
  }

  @Override
  public long getDataAsInt64(int channelIndex, Double time) {
    MapEntry me=getMapEntry(channelIndex,time);
    if(me!=null){
      TimeSeriesData tsd=tsList_.get(me.tsIndex_);
       return tsd.getDataAsInt64(me.tsChanIndex_, time);
    }else{
      return 0l;
    }
  }

  @Override
  public byte getDataAsInt8(int channelIndex, Double time) {
    MapEntry me=getMapEntry(channelIndex,time);
    if(me!=null){
      TimeSeriesData tsd=tsList_.get(me.tsIndex_);
       return tsd.getDataAsInt8(me.tsChanIndex_, time);
    }else{
      return 0;
    }
  }

  @Override
  public String getDataAsString(int channelIndex, Double time) {
    MapEntry me=getMapEntry(channelIndex,time);
    if(me!=null){
      TimeSeriesData tsd=tsList_.get(me.tsIndex_);
       return tsd.getDataAsString(me.tsChanIndex_, time);
    }else{
      return null;
    }
  }

  @Override
  public Double getStartTime(int channelIndex) {
    return getMinStartTime();
  }
  
  @Override
  public Double getEndTime(int channelIndex) {
    return getMaxEndTime();
  }

  @Override
  public double getMaxEndTime() {
    return maxTime_;
  }

  @Override
  public double getMinStartTime() {
    return minTime_;
  }

  @Override
  public List<Double> getTimes(int channelIndex) {
    List<Double> ret = new ArrayList<Double>(); 
    List<MapEntry> chanEntries=chanDataMap_.get(channelIndex).getDataMap();
    for(MapEntry me:chanEntries){
      TimeSeriesData tsd=tsList_.get(me.tsIndex_);
      ret.addAll(tsd.getTimes(me.tsChanIndex_));
    }
    return ret;
  }

  public Double getTimeNearest(int channelIndex, Double time){
    List<Double> times=getTimes(channelIndex);
    if(times.size()==0)return null;
    int dataIndex=Collections.binarySearch(times, time);
    
    if(dataIndex<0){
      int closestIndex=-(dataIndex+1);
      
      if(closestIndex+1<times.size()){
        if(Math.abs(times.get(closestIndex+1)-time)<
            Math.abs(times.get(closestIndex)-time))
          return times.get(closestIndex+1);
        else
          return times.get(closestIndex);
      }else{
        return times.get(closestIndex);
      }
    }else{
      return times.get(dataIndex);
    }
    
  }
  
  @Override
  public int getType(int channelIndex) {
    List<MapEntry> chanEntries=chanDataMap_.get(channelIndex).getDataMap();
    TimeSeriesData tsd=tsList_.get(chanEntries.get(0).tsIndex_);
    return tsd.getType(chanEntries.get(0).tsChanIndex_);
  }

  @Override
  public boolean hasChannel(String channelName) {
    return (getChannelIndex(channelName)>=0);
  }

  @Override
  public boolean hasData() {
    for(int i=0;i<chanDataMap_.size();i++){
      if(hasData(i)) return true;  
    }
    return false;
  }

  @Override
  public boolean hasData(int channelIndex) {
    return (getTimes(channelIndex).size()>0);
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public int size(int channelIndex) {
    return getTimes(channelIndex).size();
  }

  private class MultiChannel{
    private String name_;
    private List<MapEntry> dataMap_=new ArrayList<MapEntry>();
    
    public MultiChannel(String name){
      name_=name;
    }
    
    public boolean nameMatches(String name){
      return (name.compareToIgnoreCase(name_)==0);
    }
    
    public void addMapEntry(int tsIndex, int tsChanIndex){
      dataMap_.add(new MapEntry(tsIndex,tsChanIndex));
    }
    
    
    public List<MapEntry> getDataMap(){  
      return dataMap_;
    }
    
    
    public boolean equals(MultiChannel map){
      return(nameMatches(map.name_));
    }
    
    
  }
  private class MapEntry{
    int tsIndex_;
    int tsChanIndex_;
    public MapEntry(int tsIndex, int chanIndex){
      tsIndex_=tsIndex;
      tsChanIndex_=chanIndex;
    }
  }
}
