
package org.rdv.rbnb;

public class QRAFixedRange extends QueryRequestAggregate {

  private double  range_;
  
  public QRAFixedRange(){
    range_=1.0;
  }
  
  public QRAFixedRange(double range){
    range_=range;
  }
  
  @Override
  public boolean addSubscriptionRequest(SubscriptionRequest r) {
    requests_.add(r);
    return true;
  }

  /*
  @Override
  public double getOffset() {
    return 0;
  }
  */
  @Override
  public double getRange() {
    return range_;
  }

}
