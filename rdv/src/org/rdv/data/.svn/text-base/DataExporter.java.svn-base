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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/data/DataFileWriter.java $
 * $Revision: 1281 $
 * $Date: 2008-11-26 08:51:56 -0500 (Wed, 26 Nov 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * An interface used for exporting data to some format.
 * 
 * @author Jason P. Hanley
 * @author Drew Daugherty
 */
public interface DataExporter {
  public String getDescription();
  
  public String getDefaultFileExtension();
  
  /**
   * Initialize the writer. This should setup anything needed to export data 
   * file and be ready to write data. This typically involves writing a header.
   * 
   * @param file          the file or directory to write data to.
   * @param channels      the channels to be written
   * @param startTime     the earliest time for data
   * @param endTime       the latest time for data
   * @throws IOException  if there is an error initializing
   */
  public void init(File file, List<Channel> channels, 
      double startTime, double endTime) throws IOException;
  
  /**
   * Write a data sample to disk. The data sample contains a timestamp and an
   * array of data values. The array of data samples is indexed in the same
   * order as the channel list passed in initialization. If a data sample for a
   * particular sample is null, this means there was no data sample for this
   * timestamp.
   * 
   * @param sample        the sample to write
   * @throws IOException  if there is an error writing the data sample
   */
  public void writeSample(NumericDataSample sample) throws IOException;
  
  /**
   * @return a string summarizing current export status.
   */
  public String getStatus();
  
  /**
   * Close and cleanup any resources. No more data will be written
   * after this.
   * 
   * @throws IOException  if there is an error closing resources.
   */
  public void close() throws IOException;
  
  /**
   * Abort the export of data and cleanup any resources.
   */
  public void abort();
}