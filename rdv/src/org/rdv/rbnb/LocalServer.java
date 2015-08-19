/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
 * Copyright (c) 2005-2007 University at Buffalo
 * Copyright (c) 2005-2007 NEES Cyberinfrastructure Center
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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/rbnb/LocalServer.java $
 * $Revision: 1321 $
 * $Date: 2008-12-02 15:32:22 -0500 (Tue, 02 Dec 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.rbnb;

import java.io.File;

import com.rbnb.api.Server;

/**
 * A singleton class to manage a local RBNB server.
 * 
 * @author Jason P. Hanley
 */
public class LocalServer {
  /** the instance of the class */
  protected static LocalServer instance;
  
  /** the RBNB server */
  private Server server;
  
  /** the host for the local server */
  private final String host;
  
  /** the port for the local server */
  private final int port;
  
  /** the archive directory */
  private File archiveDirectory;
  
  /**
   * The default constructor for this singleton class. Declared so constructor
   * is protected.
   */
  protected LocalServer() {
    super();
    
    host = "127.0.0.1";
    port = getRandomPort();
    
    int i=0;
    do {
      i++;
      archiveDirectory = new File(System.getProperty("java.io.tmpdir"), "RDV_archive-" + i);
    } while (archiveDirectory.exists());
    
    archiveDirectory.deleteOnExit();
  }
  
  /**
   * Get the single instance of this class.
   * 
   * @return  the instance of the LocalServer
   */
  public static LocalServer getInstance() {
    if (instance == null) {
      instance = new LocalServer();
    }
    
    return instance;
  }
  
  /**
   * Gets the host name for the server
   * 
   * @return  the host name the server is on
   */
  public String getHost() {
    return host;
  }
  
  /**
   * Gets the port number for the server.
   * 
   * @return  the port number the server is on
   */
  public int getPort() {
    return port;    
  }
  
  /**
   * Starts the local RBNB server. If one is already started, this does nothing.
   * 
   * @throws Exception  if there is an error starting the server
   */
  public void startServer() throws Exception {
    if (isServerRunning()) {
      return;
    }
    
    if (!archiveDirectory.exists()) {
      archiveDirectory.mkdir();
    }
    
    // lock down access to localhost only, set the archive directory, and turn
    // off logging and metrics
    String[] args = { "-n", "Working Offline",
                      "-a", host + ":" + port,
                      "-L",
                      "-H", archiveDirectory.getCanonicalPath(),
                      "-l", "OFF",
                      "-m", "OFF"};
    
    // start the server
    server = Server.launchNewServer(args);
  }
  
  /**
   * Stops the local RBNB server. If no server is runnung, this does nothing.
   * 
   * @throws Exception  if there is an error stopping the server
   */
  public void stopServer() throws Exception {
    if (!isServerRunning()) {
      return;
    }
    
    server.stop();
    server = null;
    
    removeDirectory(archiveDirectory);
  }
  
  /**
   * See if the local RBNB server is running.
   * 
   * @return  true if the server is running, false otherwise
   */
  public boolean isServerRunning() {
    return server != null && server.isRunning();
  }
  
  /**
   * Gets a random port number in the range of 49152 to 65535.
   * 
   * @return  a port number
   */
  private static int getRandomPort() {
    return (int)(Math.random()*(65535-49152)) + 49152;
  }
  
  /**
   * Remove the directory. If the directory contains any files or directories
   * they will be removed also.
   * 
   * @param directory  the directory to be removed
   */
  private static void removeDirectory(File directory) {
      directory.deleteOnExit();
      
      for (File file: directory.listFiles()) {
        if (file.isDirectory()) {
          removeDirectory(file);
        } else {
          file.deleteOnExit();
        }
      }
  }
}