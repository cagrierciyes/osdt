
package org.rdv.rbnb;

public class QRAFixedTypeAndRange extends QueryRequestAggregate {

  int type_;
  double range_;
  
  public QRAFixedTypeAndRange(int type, double range){
    type_=type;
    range_=range;
    
  }
  
  public QRAFixedTypeAndRange(int type, double range, boolean containsHistory){
    this(type,range);
    containsHistory_=containsHistory;
  }
  
  @Override
  public boolean addSubscriptionRequest(SubscriptionRequest r) {
    if (r.getType() != type_){
      // reject request aggregation
      return false;
    }
  
    requests_.add(r);
    return true;
  }

  public int getType(){
    return type_;
  }
  
  /*@Override
  public double getOffset() {
    switch (type_){
    case SubscriptionRequest.TYPE_IMAGE:
      return 0.0;
    case SubscriptionRequest.TYPE_TABULAR:
      return 1.0;
    default:
      return 0.0;
    }
  }
  */
  @Override
  public double getRange() {
    return range_;
  }

}
