
package org.rdv.rbnb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/***
 * Implements a slice overlay on top of another time series data object.
 * Only the data corresponding to the times that fall within the slice
 * start and end times will be reported by getTimes().  
 * @author Drew Daugherty
 *
 */
public class TimeSeriesSlice implements TimeSeriesData{

  private static Log log = LogFactory.getLog(TimeSeriesSlice.class.getName());
  
  private TimeSeriesData tsd_;
  private double start_;
  private double end_;
  private double minSeriesStartTime_ = Double.MAX_VALUE;
  private double maxSeriesEndTime_=0.0;
  private double minSliceStartTime_ = Double.MAX_VALUE;
  private double maxSliceEndTime_=0.0;
  private boolean sliceHasData_=false;
  private boolean seriesHasData_=false;
  private boolean doneIfHasData_=false;
  private HashMap<Integer,List<Double>> timeMap_=new HashMap<Integer,List<Double>>();
  
  
  public TimeSeriesSlice(TimeSeriesData tsd, double start, double end, 
                boolean doneIfHasData){
    tsd_=tsd;
    start_=start;
    end_=end;
    doneIfHasData_=doneIfHasData;
    precalcTimes();
  }

  private void precalcTimes(){
    for(int i=0;i<tsd_.getChannelCount();i++){
      
      List<Double> sliceTimes=new ArrayList<Double>();
      List<Double>rawTimes=tsd_.getTimes(i);
      Iterator<Double> rawIt=rawTimes.iterator();
      while (rawIt.hasNext()){
        Double next=rawIt.next();
        if(next>=start_ && next<=end_)
          sliceTimes.add(next);
      }
      
      timeMap_.put(i,sliceTimes);
      if (sliceTimes.size()>0){
        sliceHasData_=true;
        if(sliceTimes.get(0)<minSliceStartTime_)
          minSliceStartTime_=sliceTimes.get(0);
        
        if(sliceTimes.get(sliceTimes.size()-1)>maxSliceEndTime_)
          maxSliceEndTime_=sliceTimes.get(sliceTimes.size()-1);
      }
      
      if(rawTimes.size()>0){
        seriesHasData_=true;
        if(rawTimes.get(0)<minSeriesStartTime_)
          minSeriesStartTime_=rawTimes.get(0);
        
        if(rawTimes.get(rawTimes.size()-1)>maxSeriesEndTime_)
          maxSeriesEndTime_=rawTimes.get(rawTimes.size()-1);
      }
    }
    log.debug("*slice start:"+RBNBTime.formatISO(start_)+
        " end:"+RBNBTime.formatISO(end_)+" data start: "+
        RBNBTime.formatISO(minSeriesStartTime_)+" end:"+
        RBNBTime.formatISO(maxSeriesEndTime_));
  }
  
  @Override
  public void clear() {
    tsd_.clear();
  }

  @Override
  public List<String> getChannelNames(){
    return tsd_.getChannelNames();
  }
  
  @Override
  public int getChannelIndex(String channelName) {
    return tsd_.getChannelIndex(channelName);
  }

  @Override
  public double getMaxEndTime() {
    return maxSliceEndTime_;
  }

  @Override
  public double getMinStartTime() {
    return minSliceStartTime_;
  }

  public Double getStartTime(int channelIndex){
    List<Double> times=timeMap_.get(channelIndex);
    if(times.size()>0){
      return times.get(0);
    }else{
      return null;
    }
  }
  
  public Double getEndTime(int channelIndex){
    List<Double> times=timeMap_.get(channelIndex);
    if(times.size()>0){
      return times.get(times.size()-1);
    }else{
      return null;
    }
  }
  
  public Double getTimeNearest(int channelIndex, Double time){
    return tsd_.getTimeNearest(channelIndex, time);
  }
  
//  @Override
//  public List<Double> getTimes(String channelName) {
//    int chanIndex=tsd_.getChannelIndex(channelName);
//    return timeMap_.get(new Integer(chanIndex));
//  }

  @Override
  public List<Double> getTimes(int channelIndex) {
    return timeMap_.get(new Integer(channelIndex));
  }

  @Override
  public int getType(int channelIndex) {
    return tsd_.getType(channelIndex);
  }

  @Override
  public boolean hasChannel(String channelName) {
    return tsd_.hasChannel(channelName);
  }

  @Override
  public boolean hasData() {
    return sliceHasData_;
  }

  @Override
  public boolean isDone() {
    if(seriesHasData_){
      // slice is premature
      if (end_<minSeriesStartTime_)
        return false;
      else if (sliceHasData_ && doneIfHasData_){
        return true;
      }else{
        return (end_>=maxSeriesEndTime_);
      }
    }else{
      // no data in series
      return true;
    }
  }

  public boolean hasData(int channelIndex){
    return (size(channelIndex)>0);
  }
  
  @Override
  public int size(int channelIndex) {
    return timeMap_.get(new Integer(channelIndex)).size();
  }

  @Override
  public int getChannelCount() {
    return tsd_.getChannelCount();
  }

  @Override
  public byte[] getDataAsByteArray(int channelIndex, Double time) {
    return tsd_.getDataAsByteArray(channelIndex,time);
  }

  @Override
  public float getDataAsFloat32(int channelIndex, Double time) {
    return tsd_.getDataAsFloat32(channelIndex,time);
  }

  @Override
  public double getDataAsFloat64(int channelIndex, Double time) {
    return tsd_.getDataAsFloat64(channelIndex, time);
  }

  @Override
  public short getDataAsInt16(int channelIndex, Double time) {
    return tsd_.getDataAsInt16(channelIndex, time);
  }

  @Override
  public int getDataAsInt32(int channelIndex, Double time) {
    return tsd_.getDataAsInt32(channelIndex, time);
  }

  @Override
  public long getDataAsInt64(int channelIndex, Double time) {
    return tsd_.getDataAsInt64(channelIndex, time);
  }

  @Override
  public byte getDataAsInt8(int channelIndex, Double time) {
    return tsd_.getDataAsInt8(channelIndex, time);
  }

  @Override
  public String getDataAsString(int channelIndex, Double time) {
    return tsd_.getDataAsString(channelIndex, time);
  }

//  @Override
//  public void addListener(DataListener l) {
//    tsd_.addListener(l);
//  }

//  @Override
//  public boolean free(DataListener l) {
//    if(isDone()){
//      return tsd_.free(l);
//    }
//    return false;
//  }

  @Override
  public String getChannelName(int channelIndex) {
    return tsd_.getChannelName(channelIndex);
  }
  
}
