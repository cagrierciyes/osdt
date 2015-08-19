
package org.rdv.rbnb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/***
 * Composites time series from multiple sources together.
 * @author Drew Daugherty
 *
 */
public class TimeSeriesComposite implements TimeSeriesData {
 
  /**
   * The logger for this class.
   */
  private static final Log log = LogFactory.getLog(TimeSeriesComposite.class.getName());
  
  public static final double JITTER_FACTOR_MS=(300.0/1000.0);
  
  private HashMap<Integer,ChannelIndex> channelIndexCache_=new HashMap<Integer,ChannelIndex>(); 
  private List<MultiTimeSeries> timeSeries_ = new ArrayList<MultiTimeSeries>();
  private boolean listenerDone_=false;
  private double maxEndTime_=0.0;
  
  public TimeSeriesComposite(){}
  public TimeSeriesComposite(TimeSeriesData tsd){
    addTimeSeries(tsd);
  }
  
//  @Override
//  public void addListener(DataListener l) {
//    // TODO Auto-generated method stub
//    
//  }

  @Override
  public void clear() {
    // TODO Auto-generated method stub

  }

  @Override
  public List<String> getChannelNames(){
    List<String> retList=new ArrayList<String>();
    for(MultiTimeSeries s:timeSeries_){
      for (String chanName:s.getChannelNames()){
        if(!retList.contains(chanName))
          retList.add(chanName);
      }
    }
    return retList;
  }
  
//  @Override
//  public boolean free(DataListener l) {
//    listenerDone_=true;
//    return true;
//  }
  
//  public boolean listenerDone(){
//    return listenerDone_;
//  }
  
  public void addTimeSeries(TimeSeriesData tsd){
    if(tsd.hasData()){
      Iterator<MultiTimeSeries> tsIt=timeSeries_.iterator();
      while (tsIt.hasNext()){
        // add times series to multi series if channel names match
        if(tsIt.next().addTimeSeries(tsd)) {
          log.debug("added tsd at "+RBNBTime.formatISO(tsd.getMaxEndTime())+
                      "to multi series");
          updateMaxEndTime();
          return;
        }
      }
      // create new time series wrapped in multi
      log.debug("created new multi from tsd at "+
                  RBNBTime.formatISO(tsd.getMaxEndTime()));
      timeSeries_.add(new MultiTimeSeries(tsd));
      updateMaxEndTime();
    }
  }

  @Override
  public int getChannelCount() {
    int i=0;
    int size=0;
    for(i=0;i<timeSeries_.size();i++){
      size+=timeSeries_.get(i).getChannelCount();
    }
    return size;
  }

  /***
   * Assumes that there are no two timeseries with the 
   * same channel name.
   */
  @Override
  public int getChannelIndex(String channelName) {
    int i=0;
    int indexBase=0;
    for(i=0;i<timeSeries_.size();i++){
      int chanIndex= timeSeries_.get(i).getChannelIndex(channelName);
      if(chanIndex>=0){
        int assignedIndex=indexBase+chanIndex;
        return assignedIndex;
      }
      indexBase += timeSeries_.get(i).getChannelCount();
    }
    return -1;
  }
  
  public Double getTimeNearest(int channelIndex, Double time){
    ChannelIndex c=getCacheEntry(channelIndex);
    if(c==null)return null;
    return timeSeries_.get(c.tsIndex_).
                getTimeNearest(c.tsChannelIndex_, time);
  }
  
  private ChannelIndex getCacheEntry(int channelIndex){
    ChannelIndex c=channelIndexCache_.get(channelIndex);
    if (c!=null){
      return c;
    }else{
      int i=0;
      int indexBase=0;
      for(i=0;i<timeSeries_.size();i++){
        int seriesCount=timeSeries_.get(i).getChannelCount();
        if(indexBase+seriesCount>channelIndex){
          ChannelIndex ret =new ChannelIndex(i,channelIndex-indexBase);
          channelIndexCache_.put(channelIndex,ret);
          return ret;
        }else{
          indexBase += seriesCount;
        }
      }
    }
    return null;
  }
  

  @Override
  public byte[] getDataAsByteArray(int channelIndex, Double time) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).
                  getDataAsByteArray(c.tsChannelIndex_, time);
  }

  @Override
  public float getDataAsFloat32(int channelIndex, Double time) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).
                  getDataAsFloat32(c.tsChannelIndex_, time);
  }

  @Override
  public double getDataAsFloat64(int channelIndex, Double time) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).
                  getDataAsFloat64(c.tsChannelIndex_, time);
  }

  @Override
  public short getDataAsInt16(int channelIndex, Double time) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).
                  getDataAsInt16(c.tsChannelIndex_, time);
  }

  @Override
  public int getDataAsInt32(int channelIndex, Double time) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).
                  getDataAsInt32(c.tsChannelIndex_, time);
  }

  @Override
  public long getDataAsInt64(int channelIndex, Double time) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).
                  getDataAsInt64(c.tsChannelIndex_, time);
  }

  @Override
  public byte getDataAsInt8(int channelIndex, Double time) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).
                  getDataAsInt8(c.tsChannelIndex_, time);
  }

  @Override
  public String getDataAsString(int channelIndex, Double time) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).
                  getDataAsString(c.tsChannelIndex_, time);
  }

  @Override
  public Double getEndTime(int channelIndex) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).getEndTime(c.tsChannelIndex_);
  }

  @Override
  public Double getStartTime(int channelIndex) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).getStartTime(c.tsChannelIndex_);
  }
  
  private void updateMaxEndTime(){
    double minEnd=Double.MAX_VALUE;
    double maxWithinJitter=0.0;
    int i=0;
    
    for(i=0;i<timeSeries_.size();i++){
      if(timeSeries_.get(i).getMaxEndTime()<minEnd){
        minEnd=timeSeries_.get(i).getMaxEndTime();
      }
    }
    
    for(i=0;i<timeSeries_.size();i++){
      // this needs to be replaced with getNearestTime()??
      TimeSeriesData nonMinTsd=timeSeries_.get(i);
      // see if there are any data points within jitter time period
      for(int j=0;j<nonMinTsd.getChannelCount();j++){
        List<Double> tsdTimes=nonMinTsd.getTimes(j);
        for(int k=tsdTimes.size()-1;k>=0;k--){
           Double d=tsdTimes.get(k);
           if(d>minEnd && d<=minEnd+JITTER_FACTOR_MS && d>maxWithinJitter){
             maxWithinJitter=d;
             break;
           }
           if(d<=minEnd)break;
        }
      }
    }
    maxEndTime_= Math.max(minEnd,maxWithinJitter);
  }
  /**
   * return max time of overlap including jitter.
   */
  @Override
  public double getMaxEndTime() {
    return maxEndTime_;
  }

  /***
   * return min time of overlap including jitter.
   */
  @Override
  public double getMinStartTime() {
    double minWithinJitter=Double.MAX_VALUE;
    double maxStart=0.0;
    int i;
    
    for(i=0;i<timeSeries_.size();i++){
      double tsMinStart=timeSeries_.get(i).getMinStartTime();
      if(tsMinStart>maxStart)
        maxStart=tsMinStart;
    }
    
    for(i=0;i<timeSeries_.size();i++){
      // this needs to be replaced with getNearestTime()??
      TimeSeriesData tsd=timeSeries_.get(i);
      // see if there are any data points within jitter time period
      for(int j=0;j<tsd.getChannelCount();j++){
        List<Double> tsdTimes=tsd.getTimes(j);
        for(int k=0;k<tsdTimes.size();k++){
           Double d=tsdTimes.get(k);
           if(d<maxStart && d>=maxStart-JITTER_FACTOR_MS && d<minWithinJitter){
             minWithinJitter=d;
             break;
           }
           if(d>=maxStart)break;
        }
      }
    }
    /*for(i=0;i<timeSeries_.size();i++){
      // needs to be replaced with getNearestTime()
      double tsMinStart =timeSeries_.get(i).getMinStartTime();
      if(tsMinStart<ret && tsMinStart>=maxStart-JITTER_FACTOR_MS){
        ret=tsMinStart;
      }
    }*/
    return Math.min(maxStart, minWithinJitter);
  }

  /**
   * returns only the overlap times.
   */
  @Override
  public List<Double> getTimes(int channelIndex) {
    double overlapMax=getMaxEndTime();
    double overlapMin=getMinStartTime();
    
    ChannelIndex c=getCacheEntry(channelIndex);
    List<Double>ret=new ArrayList<Double>();
    if(c!=null){
      ret.addAll(timeSeries_.get(c.tsIndex_).getTimes(c.tsChannelIndex_));
      Iterator<Double> timeIt=ret.iterator();
      while (timeIt.hasNext()){
        Double time=timeIt.next();
        if(time<overlapMin || time>overlapMax){
          timeIt.remove();
        }
      }
    }
    return ret;
  }

  @Override
  public int getType(int channelIndex) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).getType(c.tsChannelIndex_);
  }

  @Override
  public boolean hasChannel(String channelName) {
    return (getChannelIndex(channelName)>=0);
  }

  @Override
  public boolean hasData() {
    return (timeSeries_.size()>1 && 
          getMaxEndTime()-getMinStartTime()>=0);
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

  @Override
  public String getChannelName(int channelIndex) {
    ChannelIndex c=getCacheEntry(channelIndex);
    return timeSeries_.get(c.tsIndex_).getChannelName(c.tsChannelIndex_);
  }


  private class ChannelIndex{
    int tsIndex_;
    int tsChannelIndex_;
    //String name_;
    
    public ChannelIndex(int tsIndex, int channelIndex){
      //name_=name;
      tsIndex_=tsIndex;
      tsChannelIndex_=channelIndex;
    }
  }
}
