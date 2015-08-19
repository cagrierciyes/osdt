
package org.rdv.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.rdv.ConfigurationManager;

public class ConfigFile extends ConfigStore {
  
  @Override
  public void saveConfiguration(String configName) {
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(configName)));
      ConfigurationManager.printConfigXml(out);
      out.close();
    } catch(Exception e) {
      StringWriter s = new StringWriter();
      PrintWriter pw = new PrintWriter(new BufferedWriter(s));
      e.printStackTrace(pw);
      pw.flush();
      log.error(s.toString());
    }
  }

  @Override
  public void loadConfiguration(String configName) {
    String config = "";
    try {
      //URL url=new URL(configName);
      
      BufferedReader in = new BufferedReader(new FileReader(configName));
      String temp;
      while((temp = in.readLine()) != null) {
        config += temp;
      }
      in.close();
    } catch(IOException e) {
      log.error(e.getMessage());
    }
    System.out.println("Loading Config\n=========\n"+config);
    ConfigurationManager.applyConfig(config);
  }

  public void loadConfiguration(URL url) {
    //String config = "";
   // try {
      //System.out.println("Loading Config\n=========\n"+config);
      ConfigurationManager.applyConfig(url);
      
      //URL url=new URL(configName);
      
//      BufferedReader in = new BufferedReader(url.openStream());
//      String temp;
//      while((temp = in.readLine()) != null) {
//        config += temp;
//      }
//      in.close();
//    } catch(IOException e) {
//      log.error(e.getMessage());
//    }
    
  }
}
