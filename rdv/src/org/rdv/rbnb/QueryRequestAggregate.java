
package org.rdv.rbnb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/***
 * A collection of data requests that are all serviced by one 
 * request for data from the server. Requests each are associated
 * with a data panel. 
 * 
 * @author Drew Daugherty
 *
 */
public abstract class QueryRequestAggregate {
  static Log log = LogFactory.getLog(QueryRequestAggregate.class.getName());

  protected List<SubscriptionRequest> requests_= 
                          new ArrayList<SubscriptionRequest>();
  protected TimeSeriesData resultData_ = null;
  protected List<DataListener> responses_=new ArrayList<DataListener>();
  protected boolean containsHistory_=false;
  
  public abstract boolean addSubscriptionRequest(SubscriptionRequest r);
  public abstract double getRange();
  //public abstract double getOffset();
  
  public boolean hasRequests(){return (!requests_.isEmpty());}
  
  public boolean contains(SubscriptionRequest r){
    return requests_.contains(r);
  }
  
  /***
   * For each requester, create a new response object and notify 
   * of request result.
   * @param timeSeries
   * @param dropData
   */
//  public void notifyRequestors(TimeSeriesData timeSeries, boolean dropData){
//    resultData_=timeSeries;
//    for (SubscriptionRequest r:requests_){
//      
//      /*boolean hasReqChannels=false;
//      Iterator<String> chanIt=r.getServerChannelNameIterator();
//      while (chanIt.hasNext()){
//        if(resultData_.hasChannel(chanIt.next())){
//          hasReqChannels=true;
//          break;
//        }
//      }*/
//      // only notify requestors if response contains channels they
//      // are interested in - added because monitoring queries may
//      // return data from different channels based on data rates
//      // etc.
//      /*if(hasReqChannels){*/
//        r.serviced();
//        QueryResponse resp=new QueryResponse(this,timeSeries,dropData);
//        r.notify(resp);
//      /*}*/
//    }
//    
//  }
  
  /***
   * Clear query data, if it hasn't already been cleared.
   */
//  protected void finalize() throws Throwable
//  {
//    if (responses_.size()==requests_.size()){
//      if(resultData_!=null && resultData_.isDone()){
//          log.info("freeing query data on finalize");
//          resultData_.clear();
//          resultData_=null;
//      }
//    }else{
//      log.warn("failed to free query data on finalize");
//    }
//    super.finalize(); //not necessary if extending Object.
//  }
  
  
  public boolean containsHistory(){
    return containsHistory_;
  }
  

  /***
   * Clear result data if requesting panels are done and if
   * there is no more data to be processed.
   * @param l
   */
  /*public synchronized void freeResult(DataListener l){
    if (!hasResponse(l) && hasRequest(l)){
        //log.debug("adding listener "+l);
        responses_.add(l);
    }
    //log.debug("responses: "+responses_.size()+" requests: "+requests_.size());
    if (responses_.size()==requests_.size()){
        if(resultData_!=null && resultData_.isDone()){
            log.info("freeing query data");
            resultData_.clear();
            resultData_=null;
        }
    }
  }

  private boolean hasResponse(DataListener l){
      for (DataListener resp:responses_){
        if (l==resp){
            return true;
        }
    }
    return false;
  }

  private boolean hasRequest(DataListener l){
      for (SubscriptionRequest r:requests_){
        if (l==r.getListener())return true;
      }
      return false;
  }
  */
  
  public List<String> getChannelNames(){
    List<String> names = new ArrayList<String>();
    for(SubscriptionRequest r:requests_){
        Iterator<String> it =r.getServerChannelNameList().iterator();
        while (it.hasNext()){
          String name = it.next();
          if (!names.contains(name)) names.add(name);
        }
    }
    return names;
  }
}
