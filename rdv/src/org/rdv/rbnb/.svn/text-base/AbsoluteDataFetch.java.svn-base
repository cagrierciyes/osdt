
package org.rdv.rbnb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

public class AbsoluteDataFetch extends QueryDataFetch {
  private static Log log = LogFactory.getLog(AbsoluteDataFetch.class.getName());
  private double location_;
  private double range_;
  
  public AbsoluteDataFetch(double location, double range){
    location_=location;
    range_=range;
  }
  
  public AbsoluteDataFetch(QueryDataFetch f, double range){
    noDataCount_=f.noDataCount_;
    location_=f.getEndTime();
    range_=range;
  }
  
  @Override
  protected void request(RBNBConnection conn, ChannelMap request) 
        throws SAPIException{
    log.debug("Absolute fetch from: "+DataViewer.formatDate(location_)+" to:"+
        DataViewer.formatDate(location_+range_));
    conn.request(request, location_, range_, "absolute");
  }
  
  @Override
  public double getEndTime(){return location_+range_;}
  
  @Override
  public double getRange(){return range_;}
  
  public String getType(){return FETCH_TYPE_ABSOLUTE;}
}
