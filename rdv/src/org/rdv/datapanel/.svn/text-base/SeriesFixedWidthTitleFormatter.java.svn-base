
package org.rdv.datapanel;

import java.util.Iterator;

import org.rdv.DataViewer;
import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;

public class SeriesFixedWidthTitleFormatter implements PanelTitleFormatter {

  int maxWidth_=50;
  
  public SeriesFixedWidthTitleFormatter(){}
  
  public SeriesFixedWidthTitleFormatter(int maxWidth){
    maxWidth_=maxWidth;
  }
  
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
    VizSeriesList seriesList=dataPanel.getSeries();
    if(seriesList.size()>0){
      if(sb.length()>0) sb.append(": ");
      
      Iterator<VisualizationSeries> i = seriesList.iterator();
      while (i.hasNext()) {
        sb.append(i.next().getDisplayName());
        if (i.hasNext()) {
          sb.append(", ");
        }
      }
    }
    
    String panelTitle=sb.toString();
    return (panelTitle.length()>=maxWidth_)?
        panelTitle.substring(0, maxWidth_-4)+"...":panelTitle;
  }

}
