
package org.rdv.rbnb;
import java.util.List;

import org.rdv.data.VizSeriesList;

/***
 * A request to receive RBNB data.  Requests are made by 
 * a DataListener who receives notification when data is 
 * available.   Requests shall be made on one type of data
 * (image, numeric, etc.) for one time range only.  
 *
 * @author Drew Daugherty
 *
 */
public interface SubscriptionRequest {
    public static int TYPE_IMAGE=0;
    public static int TYPE_TABULAR=1;
    public static int TYPE_CHART=2;
    //public static int TYPE_UNSUBSCRIBE=3;
    
    //public void notify(QueryResponse r);
    //public void addChannel(String name, int type);
    public List<String> getServerChannelNameList();
    public List<String> getRDVChannelNameList();
    public int getType();
    //public void setTimeRange(double range);
    public double getTimeRange();
    //public void serviced();
    //public boolean isServiced();
    
    //public void setTimeOffset(double offset);
    public double getTimeOffset();
    public void setUnsubscribe();
    public boolean isSubscribe();
    public DataListener getListener();
    //public DataPanel getPanel();
    public SubscriptionRequest combineChannels(SubscriptionRequest r);
    public VizSeriesList getSeriesList();
    //public void addChannel(String name);
    //public void addChannels(List<String> names);
    
    public boolean hasChannel(String name);
    public int getDisplayPriority();
    public boolean equals(Object o);
    public String toString();
}
