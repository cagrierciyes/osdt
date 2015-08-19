/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rdv.rbnb;

import org.rdv.data.VisualizationSeries;

/**
 * A response containing data corresponding to a 
 * SubscriptionRequest.
 * 
 * @author Drew Daugherty
 */
public interface SubscriptionResponse {

    //boolean freeResult(DataListener l);

    TimeSeriesData getTimeSeries(VisualizationSeries s);
    
    //ChannelMap getChannelMap();

    double getRequestEndTime();
    
    void notifyDataListener();
    
    boolean dropData();

    boolean containsHistory();
}
