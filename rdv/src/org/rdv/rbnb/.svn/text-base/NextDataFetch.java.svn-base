
package org.rdv.rbnb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

public class NextDataFetch extends QueryDataFetch {
  /**
   * The logger for this class.
   */
  private static Log log = LogFactory.getLog(NextDataFetch.class.getName());
  
  private double location_;
  
//  public NextDataFetch(QueryDataFetch f){
//    location_=f.getEndTime();
//  }
  
  public NextDataFetch(double location){
    location_=location;
  }
  
  @Override
  protected void request(RBNBConnection conn, ChannelMap request) 
        throws SAPIException{
    log.info("Executing next fetch starting: "+DataViewer.formatDate(location_));
    conn.request(request, location_, 0.0, "next");
  }
  
  
  @Override
  public boolean isFinal(){return true;}
  
  public String getType(){return FETCH_TYPE_NEXT;}
}
