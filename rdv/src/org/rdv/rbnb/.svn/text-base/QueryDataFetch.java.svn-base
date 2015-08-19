
package org.rdv.rbnb;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

public abstract class QueryDataFetch {
  private final static long LOADING_TIMEOUT = 600000L;  ///5 minute timeout.
  private final static double END_TIME_UNKNOWN=0.0;
  public final static String FETCH_TYPE_NEXT="next";
  public final static String FETCH_TYPE_ABSOLUTE="absolute";
  public final static String FETCH_TYPE_MONITOR="monitor";
  
  protected long timeout_=LOADING_TIMEOUT;
  private int tries_=0;
  protected int noDataCount_=0;
  
  public void fetch(RBNBConnection conn, ChannelMap request, 
      ChannelMap result) throws SAPIException{
    //ChannelMap result;
    try{
      request(conn,request);
      conn.fetch(timeout_,result);
      //if(result==request){
        //log.debug("");
        //System.out.println("same result/request refs!!!");
      //}
      if(result.NumberOfChannels()<=0)
        ++noDataCount_;  
      else
        noDataCount_=0;
    }catch(SAPIException e){
      tries_++;
      noDataCount_++;
      //log error?
      throw e;
    }
    //System.out.println("Curr no data count: "+noDataCount_);
    //return result;
  }
  
  protected abstract void request(RBNBConnection conn, ChannelMap request) throws SAPIException;
  public abstract String getType();
  
  public void setTimeout(long timeout) {timeout_=timeout;}
  public int getNoDataCount(){return noDataCount_;}
  public boolean isFinal(){return false;}
  public double getEndTime(){return END_TIME_UNKNOWN;}
  public boolean endTimeUnknown(){
    return (getEndTime()==END_TIME_UNKNOWN);
  }
  public double getRange(){return 0.0;}
  
}
