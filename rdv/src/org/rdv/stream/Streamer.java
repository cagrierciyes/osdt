
package org.rdv.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Streamer {

  private static final int CACHE_BYTE_COUNT = 10000;
  private static final int END_OF_STREAM = -1;

  private static final Log LOGGER = LogFactory.getLog(Streamer.class);

  /**
   * Streams data from the given <tt>InputStream</tt> to the
   * <tt>OutputStream</tt>.
   * @param inputStream
   * @param outputStream
   * @return
   *    true if the operation was successful, otherwise false
   */
  public static boolean stream(InputStream inputStream,
      OutputStream outputStream) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Streaming data from input stream to output stream.");
    }
    byte[] cache = new byte[CACHE_BYTE_COUNT];
    int result = 0;
    while (result != END_OF_STREAM) {
      try {
        result = inputStream.read(cache);
      } catch (IOException e) {
        LOGGER.error("Error occurred while reading from stream.", e);
        return false;
      }
      if (result != END_OF_STREAM) {
        try {
          outputStream.write(cache, 0, result);
        } catch (IOException e) {
          LOGGER.error("Error occurred while writing to stream.", e);
          return false;
        }
      }
    }
    return true;
  }
}
