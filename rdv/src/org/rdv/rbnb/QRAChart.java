
package org.rdv.rbnb;

/***
 * Query request to retrieve history data on channels.  Range is
 * simply the maximum for all added SubscriptionRequests.  
 * @author Drew Daugherty
 */
public class QRAChart extends QueryRequestAggregate {
  double range_=0.0;
  
  public QRAChart(){}
  
  public QRAChart(boolean containsHistory){
    containsHistory_=containsHistory;
  }
  
  @Override
  public boolean addSubscriptionRequest(SubscriptionRequest r) {
    // only combine chart type
//    if (r.getType() != SubscriptionRequest.TYPE_CHART){
//      return false;
//    }
    //if (requests_.isEmpty()){
      requests_.add(r);
      range_=Math.max(r.getTimeRange(),range_);
    //}else{
    //  return false;
    //}
    return true;
  }

  /*@Override
  public double getOffset() {
    return range_;
    
  }
  */
  
  @Override
  public double getRange() {
    return range_;
  }
}
