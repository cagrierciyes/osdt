/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2008 Palta Software
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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/data/LocalChannelException.java $
 * $Revision: 1280 $
 * $Date: 2008-11-25 16:13:10 -0500 (Tue, 25 Nov 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.data;

/**
 * An exception thrown by a <code>LocalChannel</code>.
 * 
 * @author Jason P. Hanley
 * @see    LocalChannel
 */
public class LocalChannelException extends Exception {

  /** serialization version identifier */
  private static final long serialVersionUID = 7815808331057742190L;

  /**
   * Creates a LocalChannelException with the given message.
   * 
   * @param message  the message
   */
  public LocalChannelException(String message) {
    super(message);
  }

}