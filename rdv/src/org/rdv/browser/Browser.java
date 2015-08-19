
package org.rdv.browser;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.dialog.ErrorDialog;
import org.rdv.property.PropertyRepository;

public class Browser {

  private static final PropertyRepository PROPERTY_REPO
    = PropertyRepository.getInstance();
  private static final String SYSTEM_CONFIG_TITLE_KEY
    = "system.configuration.error";
  private static final String UNABLE_OPEN_BROWSER_KEY = "browser.error";
  private static final Log LOGGER = LogFactory.getLog(Browser.class);

  /**
   * Opens a browser pointing to the given URI.
   * @param parentComponent
   *    the parent component where we can show errors if they occur.
   * @param uri
   *    the URI to point the browser to
   */
  public static void open(Component parentComponent, URI uri) {
    if (uri == null) {
      throw new IllegalArgumentException("Browser URI not provided.");
    }
    Desktop desktop = Desktop.getDesktop();
    if (!Desktop.isDesktopSupported()
        || !desktop.isSupported(Desktop.Action.BROWSE)) {
      ErrorDialog.show(parentComponent,
          PROPERTY_REPO.getValue(UNABLE_OPEN_BROWSER_KEY),
          PROPERTY_REPO.getValue(SYSTEM_CONFIG_TITLE_KEY));
      return;
    }
    try {
      desktop.browse(uri);
    } catch (IOException e) {
      LOGGER.error(
          "Unable to open default browser.  Make sure it's installed properly.",
          e);
    }
  }
}
