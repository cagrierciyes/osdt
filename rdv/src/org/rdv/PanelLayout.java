
package org.rdv;

public enum PanelLayout {
  HORIZONTAL_LAYOUT {
    public String toString(){
      return "horizontal";
    }
  }, VERTICAL_LAYOUT{
    public String toString(){
      return "vertical";
    }
  }, TILED_LAYOUT{
    public String toString(){
      return "tiled";
    }
  };
  
  public static PanelLayout fromString(String s){
    if(s.compareToIgnoreCase("horizontal")==0){
      return HORIZONTAL_LAYOUT;
    }else if(s.compareToIgnoreCase("vertical")==0){
      return VERTICAL_LAYOUT;
    }else{
      return TILED_LAYOUT;
    }
  }
  
}
