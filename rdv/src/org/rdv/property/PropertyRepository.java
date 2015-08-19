
package org.rdv.property;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyRepository {

  private static final PropertyRepository INSTANCE
    = new PropertyRepository();
  private static final String PROPERTY_FILENAME = "rdv.properties";
  private Properties properties = new Properties();

  private PropertyRepository() {
    readPropertiesFile();
  }

  private void readPropertiesFile() {
    InputStream inputStream = getClass().getResourceAsStream(PROPERTY_FILENAME);
    try {
      properties.load(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static PropertyRepository getInstance() {
    return INSTANCE;
  }

  /**
   * Gets the value from the properties
   * @param key
   *    the key
   * @return
   *    the value
   */
  public String getValue(String key) {
    return properties.getProperty(key);
  }
}
