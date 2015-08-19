
package org.rdv.rbnb;

import java.util.HashMap;
import java.util.Map;

import org.rdv.data.VisualizationSeries;


/***
 * A response to a SubscriptionRequest.  Responses contain
 * data and/or an error message indicating why the request
 * could not be fulfilled.
 * @author Drew Daugherty
 *
 */
public class QueryResponse implements SubscriptionResponse {
  
    private Map<VisualizationSeries, TimeSeriesData> timeSeriesMap_=
          new HashMap<VisualizationSeries, TimeSeriesData>();
    //private QueryRequestAggregate qra_;
    private boolean containsHistory_=false;
    private boolean dropData_=false;
    //private static Log log = LogFactory.getLog(QueryResponse.class.getName());
    private DataListener listener_;
    private double requestEndTime_=0.0;
    
    /// start assumed to be start/end of map data.
    public QueryResponse(DataListener l){
      listener_=l;
      //timeSeries_=timeSeries;
    }
   
    public QueryResponse(DataListener l, double requestEndTime, //TimeSeriesData timeSeries, 
                        boolean dropData, boolean containsHistory){
      this(l);
      dropData_=dropData;
      requestEndTime_=requestEndTime;
      containsHistory_=containsHistory;
    }
    
    public double getRequestEndTime(){
      return requestEndTime_;
    }
    
    public boolean dropData(){
      return dropData_;
    }
    
    public boolean containsHistory(){
      return containsHistory_;
    }
    
    public TimeSeriesData getTimeSeries(VisualizationSeries s){
      return timeSeriesMap_.get(s);
    }

    public void addTimeSeries(VisualizationSeries s, TimeSeriesData tsd){
      timeSeriesMap_.put(s, tsd);
    }
    
    @Override
    public void notifyDataListener() {
      listener_.postData(this); 
    }
    
    public int getSeriesCount(){
      return timeSeriesMap_.keySet().size();
    }
    
//    public boolean freeResult(DataListener l){
////        if(timeSeries_.hasData()){
////            //log.debug("comparing "+DataViewer.formatDateSmart(time)+
////            //        " to "+DataViewer.formatDateSmart(map_.GetTimeStart(0)+map_.GetTimeDuration(0)));
//            if(timeSeries_.isDone()){
//                qra_.freeResult(l);
//                return true;
//            }
////        }
//        return false;
//    }
    
}
