/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.sourceforge.net/
 * 
 * Copyright (c) 2008 Palta Software
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/RDV.java $
 * $Revision: 1322 $
 * $Date: 2008-12-02 15:42:54 -0500 (Tue, 02 Dec 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.rdv.action.ActionFactory;
import org.rdv.config.ConfigFile;
import org.rdv.data.LocalChannelManager;
import org.rdv.datapanel.DataPanel;
import org.rdv.rbnb.LocalServer;
import org.rdv.rbnb.RBNBController;
import org.rdv.ui.MainPanel;
import org.rdv.util.PortKnock;

/**
 * The RDV application class.
 * 
 * @author Jason P. Hanley
 */
public class RDV extends SingleFrameApplication {
  
  /** the logger */
  private static Log log = LogFactory.getLog(RDV.class.getName());
  
  /** the command line arguments */
  private String[] args;
  
  /** the main panel */
  private MainPanel mainPanel;

  /**
   * Initializes the application before startup.
   * @param args  the command line arguments
   */
  @Override
  protected void initialize(String[] args) {
    super.initialize(args);
    
    this.args = args;

    //enable dynamic layout during application resize
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    if (!toolkit.isDynamicLayoutActive()) {
      toolkit.setDynamicLayout(true);
    }
    
    //set the logging system to use the internal console
    System.setProperty("org.apache.commons.logging.Log",
        "org.rdv.ui.ConsoleDialog$ConsoleLogger");

    //disable default drop target for swing components
    System.setProperty("suppressSwingDropSupport", "true");
  }

  /**
   * Starts up the application and shows the UI.
   */
  @Override
  protected void startup() {
    mainPanel = new MainPanel();
    ManagerStore.createDefaultPanelManager();
    show(mainPanel);
  }
  
  /**
   * Processes any command line arguments after the application has started and
   * the UI is visible.
   */
  @Override
  protected void ready() {
    super.ready();
    
    ProcessArgsTask task = new ProcessArgsTask(this, args);
    task.execute();
  }
  
  /**
   * Gets the main panel.
   * 
   * @return  the main panel
   */
  public MainPanel getMainPanel() {
    return mainPanel;
  }
  
  /**
   * A task to process command line arguments.
   * 
   * @author Jason P. Hanley
   */
  private class ProcessArgsTask extends Task<Void, Void> {
    private final String[] args;
    
    public ProcessArgsTask(Application application, String[] args) {
      super(application);
      
      this.args = args;
    }

    @Override
    protected Void doInBackground() throws Exception {
      parseArgs(args, true);
      return null;
    }
  }

  /**
   * Shuts down the application.
   */
  @Override
  protected void shutdown() {
    super.shutdown();

    RBNBController.getInstance().exit();

    try {
      LocalServer.getInstance().stopServer();
    } catch (Exception e) {
    }

    waitForThreads();
  }

  /**
   * Solves a bug (ticket #8) that only appears when certain threads are still running after
   * the shutdown() method exits. Any future threads that might cause this to
   * happen as well can be added to the cases below.
   */
  private void waitForThreads() {
    List<Thread> offendingThreads = new ArrayList<Thread>(2);
    Thread[] list1 = new Thread[Thread.activeCount()];
    Thread.currentThread().getThreadGroup().enumerate(list1);
    for (Thread t : list1) {
      if (t == null)
        break;
      if (t.getName().equals("RCO._Terminator"))
        offendingThreads.add(t);
      else if (t.getName().equals("Working Offline"))
        offendingThreads.add(t);
    }

    // Process threads that must exit before shutdown() finishes
    while (offendingThreads.size() > 0) {
      try {

        Thread currentThread = offendingThreads.get(0);
        if (currentThread.isAlive()) {
          // Joining the thread in this spot doesn't work
          // Sleep the current thread to wait
          Thread.sleep(30);
        } else {
          offendingThreads.remove(currentThread);
        }
        
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Creates the command line options.
   * 
   * @return  the command line options.
   */
  @SuppressWarnings("static-access")
  private static Options createOptions() {
    Options options = new Options();

    Option hostNameOption = OptionBuilder.withArgName("host name").hasArg()
        .withDescription("The host name of the Data Turbine server")
        .withLongOpt("host").create('h');

    Option portNumberOption = OptionBuilder.withArgName("port number").hasArg()
        .withDescription("The port number of the Data Turbine server")
        .withLongOpt("port").create('p');

    Option channelsOption = OptionBuilder.withArgName("channels").hasArgs()
        .withDescription("A list of channels to subscribe to")
        .withLongOpt("channels").create('c');

    Option playbackRateOption = OptionBuilder.withArgName("playback rate")
        .hasArg().withDescription("The playback rate")
        .withLongOpt("playback-rate").create('r');

    Option timeScaleOption = OptionBuilder.withArgName("time scale").hasArg()
        .withDescription("The default time scale, in seconds or unit-specified (m=mins,h=hours,etc.)")
        .withLongOpt("scale").create('s');
    
    Option locationOption = OptionBuilder.withArgName("time").hasArg()
        .withDescription("The time in ISO8601 format (e.g. 2008-07-15T14:13:54.735EDT)")
        .withLongOpt("time").create('t');

    Option timeRangeOption = OptionBuilder.withArgName("time scales")
    .withDescription("Comma-delimited list of time scales to replace the default")
    .withLongOpt("scale-list")
    .hasArg().create();
    
    Option playOption = OptionBuilder
        .withDescription("Start playing back data").withLongOpt("play")
        .create();

    Option realTimeOption = OptionBuilder.withDescription(
        "Start viewing data in real time").withLongOpt("real-time").create();

    Option portKnockOption = OptionBuilder.withArgName("ports").hasArgs()
        .withDescription("A list of ports to knock before connecting to the server")
        .withLongOpt("knock").create('k');
    
    Option updateRateOption = OptionBuilder.withArgName("rate").hasArg()
    .withDescription("Display update rate, in Hz")
    .withLongOpt("update-rate").create('u');
    
    Option helpOption = new Option("?", "help", false, "Display usage");

    Option dropDataOption = OptionBuilder.withDescription("Drop video frames at high rates")
                                .withLongOpt("drop-data").create('d');
    
    Option disableLocalChansOption = OptionBuilder.withDescription("Disable local data channels")
                                        .withLongOpt("no-local").create('l');
    
    Option windowLayoutOption = OptionBuilder.withDescription("Set window layout manager to use (basic, docking)")
                          .withLongOpt("window-layout").hasArg().create();
    
    options.addOption(timeRangeOption);
    options.addOption(hostNameOption);
    options.addOption(portNumberOption);
    options.addOption(channelsOption);
    options.addOption(playbackRateOption);
    options.addOption(timeScaleOption);
    options.addOption(locationOption);
    options.addOption(playOption);
    options.addOption(realTimeOption);
    options.addOption(portKnockOption);
    options.addOption(helpOption);
    options.addOption(dropDataOption);
    options.addOption(updateRateOption);
    options.addOption(disableLocalChansOption);
    options.addOption(windowLayoutOption);
    
    return options;
  }

  /**
   * Parses the command line arguments. If there is an error parsing the
   * arguments, an error will be printed to stderr and false will be returned.
   * 
   * @param args     the command line arguments
   * @param setArgs  if true set the options, if false just try and parse them
   * @return         true if the options parsed correctly, false otherwise
   */
  public static boolean parseArgs(String[] args, boolean setArgs) {
    Options options = createOptions();

    URL configURL = null;
    String hostName = null;
    int portNumber = -1;
    String[] channels = null;
    double playbackRate = -1;
    double timeScale = -1;
    double location = -1;
    boolean play = false;
    boolean dropData=false;
    boolean disableLocalChans=false;
    boolean realTime = false;
    int[] knockPorts = null;

    CommandLineParser parser = new PosixParser();

    try {
      CommandLine line = parser.parse(options, args);

      if (line.hasOption('?')) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar rdv.jar", options, true);
        return false;
      }

      String[] leftOverOptions = line.getArgs();
      if (leftOverOptions != null && leftOverOptions.length > 0) {
        String configFile = leftOverOptions[0];
        
        try {
          if (configFile.matches("^[a-zA-Z]+://.*")) {
            configURL = new URL(configFile);
          } else {
            configURL = new File(configFile).toURI().toURL();
          }
        } catch (MalformedURLException e) {
          log.error("\"" + configFile + "\" is not a valid configuration file URL.");
          return false;
        }
      }

      if (line.hasOption('d')) {
        dropData=true;
      }
      
      if (line.hasOption('h')) {
        hostName = line.getOptionValue('h');
      }

      if (line.hasOption('p')) {
        String value = line.getOptionValue('p');
        portNumber = Integer.parseInt(value);
      }

      if (line.hasOption('c')) {
        channels = line.getOptionValues('c');
      }

      if (line.hasOption('r')) {
        String value = line.getOptionValue('r');
        playbackRate = Double.parseDouble(value);
      }

      if (line.hasOption('s')) {
        String value = line.getOptionValue('s');
        try{
          timeScale = DataViewer.parseTime(value);
        }catch(Exception e){
          log.error("Invalid time scale: " + value);
          return false;
        }
      }
      
      if (line.hasOption('t')) {
        String value = line.getOptionValue('t');
        try {
          location = DataViewer.parseTimestamp(value);
        } catch (java.text.ParseException e) {
          log.error("Invalid time: " + value);
          return false;
        }
      }

      if (line.hasOption('u')) {
        String value = line.getOptionValue('u');
        try {
          RBNBController.PLAYBACK_REFRESH_RATE = Double.parseDouble(value);
        } catch (NumberFormatException e) {
          log.warn("Invalid update rate: " + value+". Value not set.");
        }
      }
      
      if (line.hasOption("l")) {
        disableLocalChans=true;
      }
      
      if (line.hasOption("window-layout")) {
        String value = line.getOptionValue("window-layout");
        if(value.toLowerCase().startsWith("d")){
          ManagerStore.defaultIsBasic=false;
        }
      }
      
      if (line.hasOption("scale-list")) {
        String timeRangeList = line.getOptionValue("scale-list");
        String timeRangeStrings[] =timeRangeList.split(",");
        double newDefault=-1.0;
        List<Double> validRanges=new ArrayList<Double>();
        for (String timeRangeString : timeRangeStrings){
          try{
            Double range;
            // logic for starring default range
            if(timeRangeString.startsWith("*") || timeRangeString.endsWith("*")){
              range=DataViewer.parseTime(timeRangeString.replace("*", ""));
              newDefault=range;
            }else{
              range=DataViewer.parseTime(timeRangeString);
            }
            validRanges.add(range);
          }catch(NumberFormatException e){
            log.error("Invalid list time scale: " + timeRangeString);
            return false;
          }
        }
        if(validRanges.size()>0){
          TimeScale.setGlobalTimeScales((Double[])validRanges
                              .toArray(new Double[validRanges.size()]));
          TimeScale.setDefaultTimeScale((newDefault<0.0)?
              validRanges.get(0):newDefault);
        }
      }
      
      if (line.hasOption("play")) {
        play = true;
      } else if (line.hasOption("real-time")) {
        realTime = true;
      }

      if (line.hasOption('k')) {
        String[] knockPortStrings = line.getOptionValues('k');
        knockPorts = new int[knockPortStrings.length];
        try {
          for (int i = 0; i < knockPortStrings.length; i++) {
            knockPorts[i] = Integer.parseInt(knockPortStrings[i]);
            if (knockPorts[i] < 1 || knockPorts[i] > 65535) {
              throw new NumberFormatException("Invalid port range: "
                  + knockPorts[i]);
            }
          }
        } catch (NumberFormatException e) {
          log.error("Invalid port knocking option: " + e.getMessage()
              + ".");
          return false;
        }
      }
    } catch (ParseException e) {
      log.error("Command line arguments invalid: " + e.getMessage()
          + ".");
      return false;
    }
    
    if (!setArgs) {
      return true;
    }
    
    if (configURL != null) {
      ConfigFile c = new ConfigFile();
      c.loadConfiguration(configURL.getFile());
      return true;
    }
    
    RBNBController rbnbController = RBNBController.getInstance();

    if (playbackRate != -1) {
      rbnbController.setPlaybackRate(playbackRate);
    }

    if (timeScale != -1) {
      rbnbController.setGlobalTimeScale(timeScale);
    }

    if(disableLocalChans){
      LocalChannelManager.getInstance().setDoLocalMerge(false);
    }
    
    if(dropData){
      rbnbController.setDropData(true);
    }
    
    if (hostName == null && portNumber == -1) {
      ActionFactory.getInstance().getOfflineAction().goOffline();
      return true;
    }
    
    if (knockPorts != null) {
      try {
        PortKnock.knock(hostName, knockPorts);
      } catch (IOException e) {
        log.warn("Error port knocking: " + e.getMessage() + ".");
        e.printStackTrace();
      }
    }

    if (hostName != null) {
      rbnbController.setRBNBHostName(hostName);
    }

    if (portNumber != -1) {
      rbnbController.setRBNBPortNumber(portNumber);
    }
    
    if (!rbnbController.connect(true)) {
      return true;
    }
    
    // set location before we add channels so they load the correct data, but
    // this can get reset, so it is set again after all the channels are added
    if (location >= 0) {
      rbnbController.setLocation(location);
    }
    
    if (channels != null) {
      for (int i = 0; i < channels.length; i++) {
        String channel = channels[i];
        log.info("Viewing channel " + channel + ".");
        DataPanel panel = 
            ExtensionManager.getInstance().createDataPanelFromChannel(channel);
        int viewId =ManagerStore.getPanelManager().addDataPanel(panel);
        ManagerStore.getPanelManager().showView(viewId);
      }
    }

    if (play) {
      log.info("Starting data playback.");
      rbnbController.play();
    } else if (realTime) {
      log.info("Viewing data in real time.");
      rbnbController.monitor();
    } else if (location >= 0) {
      // make sure the location is set since it can be reset when channel's are
      // added and they have no data at the current location
      rbnbController.setLocation(location);
    }
    
    return true;
  }
  
  /**
   * Set properties to make the application integrate better with the Mac UI.
   */
  public static void setupForMac() {
    // when on mac, use the mac menu bar
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    
    // set the application name for mac
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RDV");    
  }

  /**
   * The main method for the application.
   * 
   * @param args  the command line arguments
   */
  public static void main(String[] args) {
    
    setupForMac();
    
    if (!parseArgs(args, false)) {
      return;
    }
    
    launch(RDV.class, args);
  }

}