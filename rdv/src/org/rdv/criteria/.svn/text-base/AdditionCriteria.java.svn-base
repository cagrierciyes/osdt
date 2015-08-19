/*
 * RDV
 * Real-time Data Viewer
 * http://it.nees.org/software/rdv/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * $URL$
 * $Revision$
 * $Date$
 * $Author$
 */

package org.rdv.criteria;


/**
 * An interface for adding objects to collections. If the addition criteria is
 * not met, canAdd() will return false. <br>
 * <br>
 * To instantiate this object, you will most likely need to pass in a List or
 * Iterator type for which to check certain types of AdditionCriteria (such as
 * rejecting duplicate entries).
 * 
 * @author Josh J. Mattila
 * 
 */
public interface AdditionCriteria<T> {

  /**
   * Returns true if the object sufficiently meets the addition criteria set by
   * the implementation.
   * 
   * @return True if addition of the object is allowed, false otherwise.
   */
  boolean canAdd(T object);

}
