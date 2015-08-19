
package org.rdv.formatter;

import java.util.Formatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FormatterService {

  private static final Log LOGGER = LogFactory.getLog(FormatterService.class);

  /**
   * Wraps the calls to the string formatter so they are thread safe and
   * require less boilerplate code.
   * @param format
   *    the format string
   * @param args
   *    the arguments to pass into the format string
   * @return
   *    the formatted string or null if it failed
   */
  public static String format(String format, Object... args) {
    Formatter formatter = new Formatter();
    Formatter result = formatter.format(format, args);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Finished formatting string: " + result);
    }
    if (result == null) {
      return null;
    } else {
      return result.toString();
    }
  }
}
