package org.rdv.criteria;

import java.util.Collection;

/**
 * Default criteria for the AbstractDataPanel class.  Duplicates
 * are rejected when adding new channels.
 * 
 * @author Josh J. Mattila
 */
public class RejectDuplicates<T> implements AdditionCriteria<T>,
    AdditionCriteriaFactory<T> {
  private Collection<T> collection_;

  public RejectDuplicates() {
    collection_ = null;
  }

  public RejectDuplicates(Collection<T> c) {
    collection_ = c;
  }

  public boolean canAdd(T object) {
    if(collection_ == null)
      return false;
    
    return !collection_.contains(object);
  }

  public AdditionCriteria<T> createCriteria(Collection<T> collection) {
    return new RejectDuplicates<T>(collection);
  }

}
