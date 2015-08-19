
package org.rdv.rbnb;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

public class MonitorDataFetch extends QueryDataFetch {
  public static int DEFAULT_GAP_CONTROL=5;
  private int gapControl_=DEFAULT_GAP_CONTROL;
  
  public MonitorDataFetch(){timeout_=500;}
 
  @Override
  protected void request(RBNBConnection conn, ChannelMap request)
      throws SAPIException {
    
    if(!conn.monitoring()){
      conn.monitor(request, gapControl_);
    }
  }

  public String getType(){return FETCH_TYPE_MONITOR;}
}
