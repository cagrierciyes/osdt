package org.rdv.rbnb;

import java.util.List;

public interface TimeSeriesData {
  //@Deprecated
  //public abstract ChannelMap getChannelMap();

  public abstract int getType(int channelIndex);

  public abstract boolean hasChannel(String channelName);

  public abstract double getDataAsFloat64(int channelIndex, Double time);
  
  public abstract float getDataAsFloat32(int channelIndex, Double time);
  
  public abstract long getDataAsInt64(int channelIndex, Double time);
  
  public abstract int getDataAsInt32(int channelIndex, Double time);
  
  public abstract short getDataAsInt16(int channelIndex, Double time);
  
  public abstract byte getDataAsInt8(int channelIndex, Double time);
  
  public abstract String getDataAsString(int channelIndex, Double time);
  
  public abstract byte[] getDataAsByteArray(int channelIndex, Double time);
  
  public abstract int getChannelIndex(String channelName);

  public abstract int getChannelCount();
  
  public abstract boolean isDone();
  
  public abstract String getChannelName(int channelIndex);

  public abstract Double getTimeNearest(int channelIndex, Double time);
  
  //public abstract Double getNearestTime(long tolerenceMillis);
  
  public abstract double getMaxEndTime();

  public abstract double getMinStartTime();

  public abstract Double getStartTime(int channelIndex);
  
  public abstract Double getEndTime(int channelIndex);
  
  public abstract boolean hasData();
  
  public abstract boolean hasData(int channelIndex);

  public abstract void clear();
  
  public abstract int size(int channelIndex);
  
  public abstract List<String> getChannelNames();
  
  public abstract List<Double> getTimes(int channelIndex);
  
  //public abstract void addListener(DataListener l);
  
  //public abstract boolean free(DataListener l);
}