
package org.rdv.rbnb;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RBNBTime {
  public static final double ONE_MILLISECOND = 1.0E-3;
  public static final double ONE_MICROSECOND = 1.0E-6;
  
  private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
  private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("EEEE, MMMM d, yyyy h:mm.ss.SSS a");
  private static final SimpleDateFormat DAY_DATE_FORMAT = new SimpleDateFormat("EEEE h:mm.ss.SSS a");
  private static final SimpleDateFormat TIME_DATE_FORMAT = new SimpleDateFormat("h:mm:ss.SSS a");
  
  public static long convertToMillis(double time){
    return (long)(time*1000.0);
  }
  
  public static double convertFromMillis(long millis){
    return millis/1000.0;
  }
  
  public static String formatISO(double time){
      return ISO_DATE_FORMAT.format(new Date(convertToMillis(time)));
  }
  
  private double time_;
  
  public RBNBTime(double time){
    time_=time;
  }
  
  public long getMillis(){
    return convertToMillis(time_);
  }
  
  public String formatISO(){
    return ISO_DATE_FORMAT.format(new Date(getMillis()));
}
}
