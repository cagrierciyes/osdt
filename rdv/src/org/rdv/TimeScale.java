
package org.rdv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/***
 * Static methods to manage the set of global time scales. Note 
 * this class is not thread safe.
 * @author drewid
 *
 */
public class TimeScale {
  
  /** Default time scale values in seconds */
  private static Double[] globalTimeScales_ = {0.001, 0.002, 0.005, 0.01, 0.02,
    0.05, 0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 30.0, 60.0, 120.0, 300.0,
    600.0, 1200.0, 1800.0, 3600.0, 7200.0, 14400.0, 28800.0, 57600.0, 86400.0,
    172800.0, 432000.0, 604800.0};
  
  /** Default time scale value (should be a member of the global timescale set) **/
  private static double defaultTimeScale_ = 1800.0;
  
  /** Clients who should be notified if the set of time scales changes. */
  private static List<TimeScalesChangeListener> timeScaleListeners_= 
                                  new ArrayList<TimeScalesChangeListener>();
  
  public static final double TIME_SCALE_UNDEFINED=-1.0;
  public static final String TIME_SCALE_UNDEFINED_DESC="Global";
  
  private TimeScale(){}
  
  public static int getGlobalTimeScaleIndex(double timeScale){
    return Arrays.binarySearch(globalTimeScales_, timeScale);
  }
  
  public static double getGlobalTimeScaleAt(int index){
    return globalTimeScales_[index];
  }
  /**
   * True if the timescale value is within the global set.
   * @param timeScale
   * @return
   */
  public static boolean hasGlobalTimeScale(double timeScale){
    int index = Arrays.binarySearch(globalTimeScales_, timeScale);
    return(index >= 0);
  }
  
  public static void addListener(TimeScalesChangeListener listener){
    if (!timeScaleListeners_.contains(listener))
      timeScaleListeners_.add(listener);
  }
  
  public static void removeListener(TimeScalesChangeListener listener){
    timeScaleListeners_.remove(listener);
  }
  
  private static void notifyListenersAdded(int index){
    for(TimeScalesChangeListener listener: timeScaleListeners_){
      listener.timeScaleAdded(index, globalTimeScales_[index]);
    }
  }
  
  /**
   * Add a time scale to the global set. Inserts new entry into the 
   * combo box.
   */
  public static int addGlobalTimeScale(double timeScale){
    int index = Arrays.binarySearch(globalTimeScales_, timeScale);
    if(index<0){
      int insertIndex=-index-1;
      Double newScales[] = new Double[globalTimeScales_.length+1];
      System.arraycopy(globalTimeScales_, 0, newScales, 0, insertIndex);
      newScales[insertIndex]=timeScale;
      System.arraycopy(globalTimeScales_, insertIndex, newScales, 
                      insertIndex+1, globalTimeScales_.length-insertIndex);
      globalTimeScales_=newScales;
      index=insertIndex;
      notifyListenersAdded(index);
    }
    return index;
  }
  
  /**
   * @return default time scale value in seconds.
   */
  public static double getDefaultTimeScale(){
    return defaultTimeScale_;
  }
  
  /**
   * Set the timescale that first appears when RDV starts. 
   * @param timeScale
   */
  public static void setDefaultTimeScale(double timeScale){
    defaultTimeScale_=timeScale;
  }
  
  /**
   * Set the list of timescales available to panels.  Ensure
   * we have a sorted list so binary search works.
   * @return
   */
  public static void setGlobalTimeScales(Double timeScales[]){
    Arrays.sort(timeScales);
    globalTimeScales_=timeScales;
  }
  
  /**
   * Get the list of timescales available to panels.
   * @return
   */
  public static Double[] getGlobalTimeScales(){
    return globalTimeScales_;
  }
}
