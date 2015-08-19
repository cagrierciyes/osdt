package org.rdv.criteria;

import java.util.Collection;

/**
 * 
 * Creates instances of AdditionCriteria and returns them. This interface is
 * used to tie run-time values to AdditionCriteria objects.
 * 
 * @author Josh J. Mattila
 * 
 */
public interface AdditionCriteriaFactory<T> {

  /**
   * Creates an addition criteria object given a collection.
   * 
   * @param collection
   *          A non-null Collection object.
   * @return A valid AdditionCriteria object.
   */
  AdditionCriteria<T> createCriteria(Collection<T> collection);
}
