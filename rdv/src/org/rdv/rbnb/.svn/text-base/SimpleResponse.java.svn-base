/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rdv.rbnb;

import org.rdv.data.VisualizationSeries;

import com.rbnb.sapi.ChannelMap;
/**
 *
 * @author Drew Daugherty
 */
public class SimpleResponse implements SubscriptionResponse {

    //private String errorMessage_;
    private ChannelMap map_;

//    public SimpleResponse(ChannelMap map,
//                          String errorMsg){
//      map_=map;
//      errorMessage_=errorMsg;
//    }

    public SimpleResponse(ChannelMap map){
      map_=map;
    }

    //public ChannelMap getChannelMap(){return map_;}
    //public SubscriptionRequest getSubscriptionRequest(){return req_;}
    //public void setSubscriptionRequest(SubscriptionRequest r);
//    public boolean fail(){return !errorMessage_.isEmpty();}
//    public String getErrorMessage(){return errorMessage_;}

//    public boolean freeResult(DataListener l){
//        map_.Clear();
//        map_=null;
//        return true;
//    }
    //public void notifyRequestor(){
    //    req_.notify(this);

    //}
    public boolean containsHistory(){return false;}

    @Override
    public TimeSeriesData getTimeSeries(VisualizationSeries s) {
      // TODO Auto-generated method stub
      return new RBNBTimeSeries(map_);
    }

    
    @Override
    public boolean dropData() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void notifyDataListener() {
      // TODO Auto-generated method stub
      
    }

    @Override
    public double getRequestEndTime() {
      // TODO Auto-generated method stub
      return 0;
    }
}
