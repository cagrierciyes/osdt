
package org.rdv.rbnb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
/***
 * Time series data based on data from RBNB server (ChannelMap).  
 * Contains convenience methods for determine start, end, duration, etc.   
 * @author Drew Daugherty
 *
 */
public class RBNBTimeSeries implements TimeSeriesData {
    /**
     * The logger for this class.
     */
    static Log log = LogFactory.getLog(RBNBTimeSeries.class.getName());
    
    //protected List<DataListener> listeners_=new ArrayList<DataListener>();
    
    private ChannelMap map_;
    private List<Double> sortedTimes_=new ArrayList<Double>();
    //private double lastUsefulTime_=0.0;
    private double startTime_=Double.MAX_VALUE;
    private double endTime_=0.0;
    
    
    public RBNBTimeSeries(ChannelMap map){
      map_=map;
      //fetchType_=fetchType;
//      if(map_==null){
//        try{
//          throw new Exception("warning- rbnb time series got null channel map!");
//        }catch(Exception e){
//          e.printStackTrace();
//        }
//      }
      precalcMapTimes(map_);
    }
    
    /*public List<Double> getTimes(){
      return sortedTimes_;
    }*/
    
    /* (non-Javadoc)
     * @see org.rdv.rbnb.TimeSeriesData#getChannelMap()
     */
//    public ChannelMap getChannelMap(){
//      return map_;
//    }
    
    /* (non-Javadoc)
     * @see org.rdv.rbnb.TimeSeriesData#getType(java.lang.String)
     */
    public int getType(int channelIndex){
      return map_.GetType(channelIndex);
    }
    
    /* (non-Javadoc)
     * @see org.rdv.rbnb.TimeSeriesData#hasChannel(java.lang.String)
     */
    public boolean hasChannel(String channelName){
      return (getChannelIndex(channelName)>=0);
    }
    
    
//    public List<Double> getTimes(String channelName){
//      int index=getChannelIndex(channelName);
//      return getTimes(index);
//    }
    
    public List<Double> getTimes(int channelIndex){
      List<Double> ret=new ArrayList<Double>(); 
      double times[]=map_.GetTimes(channelIndex);
      for(int i=0;i<times.length;i++){
        ret.add(new Double(times[i]));
      }
      return ret;
    }
    
    /*protected TimeSeriesIterator iterator(String channelName, double respStart, double respEnd){
      int chanIndex=getChannelIndex(channelName);
      int endIndex=getEndIndex(chanIndex,respEnd);
      int startIndex=getStartIndex(chanIndex,respStart);
      if(chanIndex<0 || endIndex<0 || startIndex<0 || startIndex > endIndex)
        return null;
      else
        return new TimeSeriesIterator(this,chanIndex,startIndex,endIndex);
    }*/
    
    /*protected int getEndIndex(int channelIndex,double end){
      double times[]=map_.GetTimes(channelIndex);
      for(int i=times.length-1;i>=0;i--){
        if(times[i]<=end){
          return i;
        }
      }
      log.warn("end of "+RBNBTime.formatISO(end)+
          " less than data start: "+
          RBNBTime.formatISO(times[0]));
      return -1;
    }
    
    protected int getStartIndex(int channelIndex,double start){
      double times[]=map_.GetTimes(channelIndex);
      for(int i=0;i<times.length-1;i++){
        if(times[i]>=start){
          return i;
        }
      }
      log.warn("start of "+RBNBTime.formatISO(start)+
          " greater than data end: "+
          RBNBTime.formatISO(times[times.length-1]));
      return -1;
    }*/
    
    /*public List<Double> getTimes(String channelName,double start,double end){
      int index=getChannelIndex(channelName);
      return getTimes(index,start,end);
    }
    
    public List<Double> getTimes(int channelIndex,double start,double end){
      List<Double> ret=new ArrayList<Double>(); 
      double times[]=map_.GetTimes(channelIndex);
      for(int i=0;i<times.length;i++){
        if(times[i]>=start && times[i]<=end)
          ret.add(new Double(times[i]));
      }
      return ret;
    }*/

    /* (non-Javadoc)
     * @see org.rdv.rbnb.TimeSeriesData#getTime(int, int)
     */
    public double getTime(int channelIndex, int iteratorIndex){
      return map_.GetTimes(channelIndex)[iteratorIndex];
    }
    
    /* (non-Javadoc)
     * @see org.rdv.rbnb.TimeSeriesData#getChannelIndex(java.lang.String)
     */
    public int getChannelIndex(String channelName){
      return (map_==null)?-1:map_.GetIndex(channelName);
    }

    /// Return the intersection of times for the channel names.
    /*public List<Double> getTimes(Collection<String> channelNames){
      
      List<List<Double>> times=new ArrayList<List<Double>>();
      Iterator<String>nameIt=channelNames.iterator();
      while (nameIt.hasNext()){
        String name=nameIt.next();
        times.add(getTimes(name));
      }
      Iterator<List<Double>> timeIt=times.iterator();
      List<Double>first;
      if(timeIt.hasNext())
        first=timeIt.next();
      else
        first=new ArrayList<Double>();
      while(timeIt.hasNext()){
        // intersection of times
        first.retainAll(timeIt.next());
      }
     return first;
    }*/

    /*public List<Double> getTimes(Collection<String> channelNames, double start, double end){
      // note: needs replacing
      List<List<Double>> times=new ArrayList<List<Double>>();
      Iterator<String>nameIt=channelNames.iterator();
      while (nameIt.hasNext()){
        String name=nameIt.next();
        times.add(getTimes(name,start,end));
      }
      Iterator<List<Double>> timeIt=times.iterator();
      List<Double>first;
      if(timeIt.hasNext())
        first=timeIt.next();
      else
        first=new ArrayList<Double>();
      while(timeIt.hasNext()){
        // intersection of times
        first.retainAll(timeIt.next());
      }
     return first;
    }*/
    
    
    protected double getDataAsFloat64(int channelIndex, int iteratorIndex){
      return map_.GetDataAsFloat64(channelIndex)[iteratorIndex];
    }
    
    protected float getDataAsFloat32(int channelIndex, int iteratorIndex){
      return map_.GetDataAsFloat32(channelIndex)[iteratorIndex];
    }
    
    protected long getDataAsInt64(int channelIndex, int iteratorIndex){
      return map_.GetDataAsInt64(channelIndex)[iteratorIndex];
    }
    
    protected int getDataAsInt32(int channelIndex, int iteratorIndex){
      return map_.GetDataAsInt32(channelIndex)[iteratorIndex];
    }
    
    protected short getDataAsInt16(int channelIndex, int iteratorIndex){
      return map_.GetDataAsInt16(channelIndex)[iteratorIndex];
    }
    
    protected byte getDataAsInt8(int channelIndex, int iteratorIndex){
      return map_.GetDataAsInt8(channelIndex)[iteratorIndex];
    }
    
    protected String getDataAsString(int channelIndex, int iteratorIndex){
      return map_.GetDataAsString(channelIndex)[iteratorIndex];
    }
    
    protected byte[] getDataAsByteArray(int channelIndex, int iteratorIndex){
      return map_.GetDataAsByteArray(channelIndex)[iteratorIndex];
    }
    
    public double getDataAsFloat64(int channelIndex,Double time){
      int dataIndex=getIndexForTime(time,channelIndex);
      if(dataIndex<0){
        // missing data
        return 0.0;
        //return null;
      }else{
        return map_.GetDataAsFloat64(channelIndex)[dataIndex];
      }
    }
    
    public float getDataAsFloat32(int channelIndex,Double time){
      int dataIndex=getIndexForTime(time,channelIndex);
      if(dataIndex<0){
        // missing data
        return 0.0f;
        //return null;
      }else{
        return map_.GetDataAsFloat32(channelIndex)[dataIndex];
      }
    }
    
    public long getDataAsInt64(int channelIndex,Double time){
      int dataIndex=getIndexForTime(time,channelIndex);
      if(dataIndex<0){
        // missing data
        return 0l;
        //return null;
      }else{
        return map_.GetDataAsInt64(channelIndex)[dataIndex];
      }
    }
    
    public int getDataAsInt32(int channelIndex,Double time){
      int dataIndex=getIndexForTime(time,channelIndex);
      if(dataIndex<0){
        // missing data
        return 0;
        //return null;
      }else{
        return map_.GetDataAsInt32(channelIndex)[dataIndex];
      }
    }
    
    @Override
    public short getDataAsInt16(int channelIndex, Double time) {
      int dataIndex=getIndexForTime(time,channelIndex);
      if(dataIndex<0){
        // missing data
        return 0;
        //return null;
      }else{
        return map_.GetDataAsInt16(channelIndex)[dataIndex];
      }
    }
    
    public byte getDataAsInt8(int channelIndex,Double time){
      int dataIndex=getIndexForTime(time,channelIndex);
      if(dataIndex<0){
        // missing data
        return 0;
        //return null;
      }else{
        return map_.GetDataAsInt8(channelIndex)[dataIndex];
      }
    }
    
    public String getDataAsString(int channelIndex,Double time){
      int dataIndex=getIndexForTime(time,channelIndex);
      if(dataIndex<0){
        // missing data
        return "";
      }else{
        return map_.GetDataAsString(channelIndex)[dataIndex];
      }
    }
    
    public byte[] getDataAsByteArray(int channelIndex,Double time){
      int dataIndex=getIndexForTime(time,channelIndex);
      if(dataIndex<0){
        // missing data
        return null;
      }else{
        return map_.GetDataAsByteArray(channelIndex)[dataIndex];
      }
    }
    
    private int getIndexForTime(Double time,int channelIndex){
      //log.debug("getting time index for channel "+channelIndex+ " at "+RBNBTime.formatISO(time));
      double times[]=map_.GetTimes(channelIndex);
      if(times==null)log.debug("null time array!");
      return Arrays.binarySearch(times, time);
    }
    
    public Double getStartTime(int channelIndex){
      double times[]=map_.GetTimes(channelIndex);
      if(times.length>0){
        return times[0];
      }else{
        return null;
      }
    }
    
    public Double getEndTime(int channelIndex){
      double times[]=map_.GetTimes(channelIndex);
      if(times.length>0){
        return times[times.length-1];
      }else{
        return null;
      }
    }
    
    public Double getTimeNearest(int channelIndex, Double time){
      double times[]=map_.GetTimes(channelIndex);
      Double ret;
      if(times==null || times.length==0)return null;
      int dataIndex=Arrays.binarySearch(times, time);
      
      if(dataIndex<0){
        //no exact match, find closest
        int closestIndex=-(dataIndex+1);
        if (closestIndex>=times.length){
          // return last item
          ret=times[times.length-1];
        }else{
          // see if next time is closer
          if(closestIndex+1<times.length && 
              (Math.abs(times[closestIndex+1]-time)<
                Math.abs(times[closestIndex]-time))){
            ret= times[closestIndex+1];
          }else{
            ret= times[closestIndex];
          }
        }
      }else{
        ret= times[dataIndex];
      }
      double diff=Math.abs(ret-time);
      if(Math.abs(diff-0.0)>1E-6){
        log.debug("returning time nearest, difference is "+diff);
      }
      return ret;
    }
    
    private void precalcMapTimes(ChannelMap map){
      /*if(fetchType_.compareToIgnoreCase(QueryDataFetch.FETCH_TYPE_NEXT)==0){
        double seriesStart=getstartTime_(map_);
        if(seriesStart>=start && seriesStart<=end){
          ret.add(new Double(seriesStart));
        }
        
      }else{*/
        for(int i=0;i<getChannelCount();i++){
          double start=map_.GetTimeStart(i);
          if(start<startTime_){
            startTime_=start;
          }
          double duration=map_.GetTimeDuration(i);
          if(start+duration>endTime_){
            endTime_=start+duration;
          }
        }
        
        /*if(fetchType_.compareToIgnoreCase(QueryDataFetch.FETCH_TYPE_NEXT)==0){
          lastUsefulTime_=getStartTime();
        }else{
          lastUsefulTime_=getEndTime();
        }*/
      //}
      
    }
    
    /* (non-Javadoc)
     * @see org.rdv.rbnb.TimeSeriesData#isDoneAt(double)
     */
    public boolean isDone(){
      return true;
    }
    
    public double getTimeAfter(double time){
      double ret=-1.0;
      Iterator<Double>it=sortedTimes_.iterator();
      while(it.hasNext()){
        double next=it.next().doubleValue();
        if(next>time) {
          ret=next;
          break;
        }
      }
      return ret;
    }
    
    /* (non-Javadoc)
     * @see org.rdv.rbnb.TimeSeriesData#getEndTime()
     */
    public double getEndTime(){
      return endTime_;
    }
    
    /* (non-Javadoc)
     * @see org.rdv.rbnb.TimeSeriesData#getStartTime()
     */
    public double getStartTime(){
      //return getstartTime_(map_);
      return startTime_;
    }
    
    /* (non-Javadoc)
     * @see org.rdv.rbnb.TimeSeriesData#hasData()
     */
    public boolean hasData(){
      return (getChannelCount()>0);
    }
    
    /* (non-Javadoc)
     * @see org.rdv.rbnb.TimeSeriesData#clear()
     */
    public void clear(){
      //log.info("clear data start");
      map_.Clear();
      //log.info("clear data end");
    }

    @Override
    public int getChannelCount() {
      return (map_==null)?0:map_.NumberOfChannels();
    }

    @Override
    public double getMaxEndTime() {
      return endTime_;
    }

    @Override
    public double getMinStartTime() {
      return startTime_;
    }

    @Override
    public boolean hasData(int channelIndex){
      return (size(channelIndex)>0);
    }
    
    @Override
    public int size(int channelIndex) {
      if(channelIndex>=0 && channelIndex<getChannelCount()){
        return (map_==null)?0:map_.GetTimes(channelIndex).length;
      }else{
        return 0;
      }
    }

//    @Override
//    public void addListener(DataListener l) {
//      boolean containsRef=false;
//      for(DataListener dl:listeners_){
//        if(dl==l){
//          containsRef=true;
//          break;
//        }
//      }
//      if(!containsRef) listeners_.add(l);
//    }

//    @Override
//    public synchronized boolean free(DataListener l) {
//      Iterator<DataListener> dlIt=listeners_.iterator();
//      while(dlIt.hasNext()){
//        if(dlIt.next()==l){
//          dlIt.remove();
//          break;
//        }
//      }
//      if(listeners_.size()==0 && map_!=null){
//        log.debug("freeing query data");
////        try{
////          throw new Exception("freeing query data");
////        }catch(Exception e){
////          e.printStackTrace();
////        }
//        map_.Clear();
//        map_=null;
//      }
//      return isDone();
//      return true;
//    }
    
    /*
     * Clear data frem the channel map.
     * Should supplant old reference counting free() mechanism.
     * @see java.lang.Object#finalize()
     */
//    @Override
//    protected void finalize() throws Throwable{
//      try{
//        if(map_!=null) map_.Clear();
//        log.info("freed query data");
//      }finally{
//        super.finalize();
//      }
//    }

    @Override
    public String getChannelName(int channelIndex) {
      return (map_==null)?null:(map_.GetName(channelIndex));
    }
    
    @Override
    public List<String> getChannelNames(){
      List<String> retList=new ArrayList<String>();
      if(map_!=null){
        for(int i=0;i<map_.NumberOfChannels();i++){  
          retList.add(map_.GetName(i));
        }
      }
      return retList;
    }
    
    /*private double getMaxDuration(ChannelMap m){
      double duration=0.0;
      for(int i=0;i<m.NumberOfChannels();i++){
        duration= Math.max(m.GetTimeDuration(i),duration);
      }
      return duration;
    }
    
    private double getLastTime(ChannelMap m){
        double end=0.0;
        for(int i=0;i<m.NumberOfChannels();i++){
          end= Math.max(m.GetTimeStart(i)+m.GetTimeDuration(i),end);
        }
       return end;
    }
    
    private double getstartTime_(ChannelMap m){
      double start=Double.MAX_VALUE;
      for(int i=0;i<m.NumberOfChannels();i++){
        double resultStart=m.GetTimeStart(i);
        //log.debug("chan "+m.GetName(i)+" start: "+DataViewer.formatDate(resultStart));
        start= Math.min(resultStart,start);
      }
     return start;
    }
    
    private int getRecordCount(ChannelMap m){
      int count=0;
      for(int i=0;i<m.NumberOfChannels();i++){
        count= Math.max(m.GetTimes(i).length,count);
      }
     return count;
    }*/
}
