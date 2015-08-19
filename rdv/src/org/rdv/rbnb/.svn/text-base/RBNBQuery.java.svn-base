
package org.rdv.rbnb;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;
import org.rdv.data.LocalChannelManager;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.LocalChannelMap;
import com.rbnb.sapi.SAPIException;

/***
 * An RBNB server data query. The channels to fetch are 
 * determined by a QueryRequestAggregate member, while
 * The query type (absolute, next, etc.) is determined by 
 * a QueryDataFetch member.  See also the documentation for
 * the Sink class at dataturbine.org.
 * 
 * @see QueryRequestAggregate,QueryDataFetch
 * @author Drew Daugherty
 */
public class RBNBQuery {
    static Log log = LogFactory.getLog(RBNBQuery.class.getName());
    //private List<SubscriptionRequest> requests_= new ArrayList<SubscriptionRequest>();
    //private double location_;
    private QueryRequestAggregate qra_;
    private QueryDataFetch fetch_;
    //private double range_;
    //private int type_;
    //private ChannelMap result_=new ChannelMap();
    private ChannelMap request_=new ChannelMap();
    private LocalChannelMap result_;//=new LocalChannelMap();
    private TimeSeriesData resultTimeSeries_=null;
    //private long timeout_=LOADING_TIMEOUT;
    private String errorMessage_="";
    private boolean initComplete=false;
    private boolean gotData_=false;
    private List<String> requestedChans_;
    //private String reference_="absolute";
    /// the number of queries that have seen no data.
    //private int noDataCount_=0;
    private RBNBQueryContext context_;
    
   
    public static RBNBQuery createNextAbsoluteQuery(RBNBQuery prev, double range){
      return new RBNBQuery(RBNBQueryContext.PLAYBACK,
                        prev.qra_,new AbsoluteDataFetch(prev.fetch_,range));
    }
    
    public static RBNBQuery createMonitorQuery(QueryRequestAggregate qra){
      return new RBNBQuery(RBNBQueryContext.MONITOR,
                              qra,new MonitorDataFetch());
    }
    
    public static RBNBQuery createNextQuery(RBNBQuery prev){
      return new RBNBQuery(RBNBQueryContext.PLAYBACK,
                  prev.qra_,new NextDataFetch(prev.getRequestEndTime()));
    }
    
    public static RBNBQuery createAbsoluteQuery(QueryRequestAggregate qra, 
                                                double location){
      return new RBNBQuery(RBNBQueryContext.PLAYBACK,
                      qra,new AbsoluteDataFetch(location,qra.getRange()));
    }
    
    public static RBNBQuery createAbsoluteHistoryQuery(QueryRequestAggregate qra, 
                                                        double location){
      return new RBNBQuery(RBNBQueryContext.HISTORY,
                    qra,
                    new AbsoluteDataFetch(location-qra.getRange(),qra.getRange()));
    }
    
    public RBNBQuery(RBNBQueryContext context,
                  QueryRequestAggregate qra, QueryDataFetch fetch){
      context_=context;
      qra_=qra;
      fetch_=fetch;
      
      requestedChans_ = qra_.getChannelNames();
      for(String chanName:requestedChans_){
          try{
            request_.Add(chanName);
          }catch(SAPIException e){
            log.warn("Unable to add channel '"+chanName+"' to query");
          }
      }
      
      if (request_.NumberOfChannels()==0){
        log.debug("No channels to query!!");
      }
    }
    
    public RBNBQueryContext getContext(){
      return context_;
    }
    
    public List<String> getRequestedChannels(){
      return requestedChans_;
    }
    
    public boolean gotData(){
      return gotData_;
    }
    
    public boolean isFinal(){
      return fetch_.isFinal();
      //return (reference_.compareToIgnoreCase("absolute")==0);
    }
    
    public int getNoDataCount(){
      return fetch_.getNoDataCount();
    }
    
    public boolean containsRequest(SubscriptionRequest r){
      return qra_.contains(r);
    }
    
    /*public RBNBQuery(double location, SubscriptionRequest r){
      location_=location-r.getTimeOffset();
      range_=r.getTimeRange();
      //type_=r.getType();
      requests_.add(r);
    }*/
    
    /*public boolean addSubscriptionRequest(SubscriptionRequest r){
     
      if (r.getType() == SubscriptionRequest.TYPE_CHART ||
          r.getType() != type_ ||
          r.getTimeRange() != range_){
        // reject request aggregation
        return false;
      }
    
      requests_.add(r);
      return true;
    }*/
    public ChannelMap result(){
      return result_;
    }
    
    public TimeSeriesData getTimeSeries(){
      if(resultTimeSeries_==null){
        resultTimeSeries_=new RBNBTimeSeries(result_);
      }
      return resultTimeSeries_;
    }
    
    public void setTimeSeriesSlice(double start, double end){
      resultTimeSeries_= new TimeSeriesSlice(
                              new RBNBTimeSeries(result_),
                              start,end,fetch_.isFinal());
    }
    
    public boolean timeout(){
      return result_.GetIfFetchTimedOut();
    }
    
    public double getRequestDuration(){
      return fetch_.getRange();
    }
    
    public double getRequestEndTime(){
      // if dealing with a next query
      // return the start time of the 
      // result, else return the end
      // time requested
      return (fetch_.endTimeUnknown())?
          getResultStartTime():fetch_.getEndTime();
    }
    
    public double getResultDuration(){
      return getMaxDuration(result_);
    }
    
    public double getResultEndTime(){
      return getLastTime(result_);
    }
    
    public double getResultStartTime(){
      return getStartTime(result_);
    }
    
    private double getMaxDuration(ChannelMap m){
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
    
    private double getStartTime(ChannelMap m){
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
    }
    
    public boolean hasData(){
      return (result_!=null && result_.NumberOfChannels()>0);
    }
    
//    public void notifyRequestors(TimeSeriesData timeSeries,boolean dropData){
//      qra_.notifyRequestors(timeSeries,dropData);
//    }
//    
//    public void notifyRequestors(boolean dropData){
//      qra_.notifyRequestors(getTimeSeries(),dropData);
//    }
    
//    public void notifyRequestors(double start, double end){
//      qra_.notifyRequestors(getTimeSeries(), start, end);
//      //result_=null;
//      /*for (SubscriptionRequest r:requests_){
//        SubscriptionResponse resp=(errorMessage_.isEmpty())?
//                            new SubscriptionResponse(r,result_):
//                              new SubscriptionResponse(r,result_,errorMessage_);
//        resp.notifyRequestor();
//      }*/  
//    }
    
    public boolean initComplete(){
      return initComplete;
    }
    public void setInitComplete(){
      initComplete=true;
    }
    private String arrayToString(String arr[]){
      StringBuffer buffer=new StringBuffer();
      for(String chanName:arr){
        buffer.append(chanName);
        buffer.append(',');
      }
      return buffer.toString();
    }
    
    public String getErrorMsg(){return errorMessage_;}
    
    public boolean error(){return (!errorMessage_.isEmpty());}

    public int getDisplayPriority(){
        int retPriority=Integer.MAX_VALUE;
        Iterator<SubscriptionRequest> it=qra_.requests_.iterator();
        while (it.hasNext()){
            int priority=it.next().getDisplayPriority();
            retPriority=Math.min(retPriority, priority);
        }
        return (retPriority==Integer.MAX_VALUE)?-1:retPriority;
    }

    public void execute(RBNBConnection conn) throws SAPIException{
      
      try{
        //result_= fetchData(s,map);
        result_=new LocalChannelMap();
        if (request_.NumberOfChannels()>0){
          fetch_.fetch(conn,request_,result_);
        }else{
          log.warn("Empty query request executed");
        }
        if(hasData()) gotData_=true;
        LocalChannelManager.getInstance().updateData(result_);
      }catch(SAPIException e){
        errorMessage_=e.getMessage();
        log.error("query execute got exception: "+errorMessage_);
        //++noDataCount_;
        throw e;
      }
      logResult();
    }
    
    /*public void executeRetry(RBNBConnection conn, int tries) throws SAPIException{
      for (int i=0;i<tries;i++){
        //try{
          execute(conn);
          break;
        //}catch(SAPIException e){
          // re-throw if we are at end
         // if (i==tries-1) throw e;
        //}
      }
    }*/
    
    //private void fetchData(RBNBConnection conn, ChannelMap m) throws SAPIException{
      //log.debug("Requested data at "+location_+",range: "+range_+" on channels:"+arrayToString(m.GetChannelList()));
     // conn.request(m, location_, range_, reference_);
     // conn.fetch(timeout_,result_);
    //}
    
    private void logResult(){
      if(hasData()){
        int recs=getRecordCount(result_);
        String start=DataViewer.formatDate(getStartTime(result_));
        String end=DataViewer.formatDate(getLastTime(result_));
        double maxDur=getMaxDuration(result_);
        NumberFormat fmt=new DecimalFormat("0.0");
        log.info("Got "+recs+" records from: "+start+" to: "+end+" range: "+fmt.format(maxDur)+ " secs for "+result_.GetChannelList().length+" channels:"+arrayToString(result_.GetChannelList()));
      }else{
        log.debug("Got no data for "+result_.GetChannelList().length+" channels:"+arrayToString(result_.GetChannelList()));
      }
    }
}
