
package org.rdv.dialog;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ErrorDialog {

  private static final Log LOGGER = LogFactory.getLog(ErrorDialog.class);

  /**
   * Shows an error dialog with the given message
   * @param parentComponent
   *    the parent component
   * @param message
   *    the message to show
   * @param title
   *    the title of the window
   */
  public static void show(Component parentComponent, String message,
      String title) {
    LOGGER.error(message);
    JOptionPane.showMessageDialog(parentComponent, message, title,
        JOptionPane.ERROR_MESSAGE);
  }
}
