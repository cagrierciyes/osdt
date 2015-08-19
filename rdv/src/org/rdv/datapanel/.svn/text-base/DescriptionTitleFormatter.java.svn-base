
package org.rdv.datapanel;

import org.rdv.DataViewer;

public class DescriptionTitleFormatter implements PanelTitleFormatter {
  
  @Override
  public String getTitle(DataPanel dataPanel) {
    
    StringBuffer sb=new StringBuffer();
    
    String description=dataPanel.getDescription();
    if (description != null) {
      sb.append(description);
      if(!dataPanel.localTimescaleUndefined()){
        sb.append(" (").append(DataViewer.formatSeconds(dataPanel.getLocalTimescale()));
        sb.append(")");
      }
      return sb.toString();
    }else{

      if(!dataPanel.localTimescaleUndefined()){
        sb.append(DataViewer.formatSeconds(dataPanel.getLocalTimescale()));
        //sb.append(": ");
      }
    } 
    return sb.toString();
  }
}
