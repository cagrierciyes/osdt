
package org.rdv.data;

import java.util.List;

public interface VisualizationSeries {
  //public void removeFromPanel(DataPanel dataPanel);
  public String getName();
  public String getDisplayName();
  
  public boolean containsChannel(String channelName);
  //public boolean equals(Object o);
  
  /**
   * Add a channel.
   * @return true if channel added, false otherwise.
   */
  public boolean hasAllChannels();
  public boolean addChannel(String channelName);
  
  public List<String> getChannels();
  public List<String> getServerChannels();
  
}
