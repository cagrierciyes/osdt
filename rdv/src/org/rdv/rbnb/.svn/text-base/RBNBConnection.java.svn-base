
package org.rdv.rbnb;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

public class RBNBConnection {
  //private static int STATE_DISCONNECT=0;
  private static int STATE_REQUEST=1;
  private static int STATE_MONITOR=2;
  
  private String sinkName_="RDV";
  private Sink sink_;
  //Sink requestSink_;
  private int state_=STATE_REQUEST;
  private String host_= "localhost";
  private int port_= 3333;
  private long profileBytes_=0;
  private long profileStartMillis_=0;
  static Log log = LogFactory.getLog(RBNBConnection.class.getName());
  
  public RBNBConnection(){
    sinkName_=getSinkName();
  }
  
  public RBNBConnection(String server, int port){
    host_=server;
    port_=port;
    sinkName_=getSinkName();
  }
  
  private String getSinkName(){
    String name="RDV";
    // get the system host name and append it to the sink name
    try {
      InetAddress addr = InetAddress.getLocalHost();
      String hostname = addr.getHostName();
      name += "@" + hostname;
    } catch (UnknownHostException e) {
    }
    return name;
  }
  
  public String getHostName() {
    return host_;
  }

  public void setHostName(String rbnbHostName) {
    host_ = rbnbHostName;
  }

  public int getPortNumber() {
    return port_;
  }

  public void setPortNumber(int rbnbPortNumber) {
    port_ = rbnbPortNumber;
  }

  public String getConnectionString() {
    return host_ + ":" + port_;
  }

  public boolean isConnected() {
    return (sink_ != null); 
  }
  /**
   * Gets the name of the server. If there is no active connection, null is
   * returned.
   * 
   * @return the name of the server, or null if there is no connection
   */
  public String getServerName() {
    if (sink_ == null) {
      return null;
    }

    String serverName;

    try {
      serverName = sink_.GetServerName();

      // strip out the leading slash that is there for some reason
      if (serverName.startsWith("/") && serverName.length() >= 2) {
        serverName = serverName.substring(1);
      }
    } catch (IllegalStateException e) {
      serverName = null;
    }

    return serverName;
  }
  
  public void init() throws SAPIException {
    if (sink_ == null) {
      sink_ = new Sink();
    } else {
      return;
    }
    
    /*if (requestSink_ == null) {
      requestSink_ = new Sink();
    } else {
      return;
    }

    monitorSink_.OpenRBNBConnection(host_ + ":" + port_, "Mon "+sinkName_);*/
    sink_.OpenRBNBConnection(host_ + ":" + port_, sinkName_);
    
    //log.info("Connected to RBNB server.");
  }

  public void close() {
    /*if (monitorSink_!= null){
      monitorSink_.CloseRBNBConnection();
      monitorSink_ = null;
    }*/

    if (sink_!= null){
      sink_.CloseRBNBConnection();
      sink_ = null;
    }
    //log.info("Connection to RBNB server closed.");
  }

  private void reInit() throws SAPIException {
    close();
    init();
  }

  private void profileStart(){
    profileBytes_=sink_.BytesTransferred();
    profileStartMillis_=System.currentTimeMillis();
  }
  
  private void profileEnd(){
    long transferred=sink_.BytesTransferred()-profileBytes_;
    double elapsed=(System.currentTimeMillis()-profileStartMillis_)/1000.0;
    if(transferred>0L)
      log.info("transferred "+transferred+" bytes in "+elapsed+
                    " secs, "+(transferred/elapsed)+" bytes/sec");
    else
      log.debug("transferred "+transferred+" bytes in "+elapsed+
                    " secs, "+(transferred/elapsed)+" bytes/sec");
  }
  
  public void request(ChannelMap cm, double start, double duration, 
                      String reference) throws SAPIException{
    if(state_!=STATE_REQUEST){
      //log.debug("reinit to request state");
      reInit();
      state_=STATE_REQUEST;
    }
    if(!isConnected()){
      init();
    }
    //log.debug("request call");
    //profileStart();
    try{
        sink_.Request(cm,start,duration,reference);
    }catch(SAPIException e){
        if (e.getMessage().indexOf("Socket")!=-1){
            reInit();
            sink_.Request(cm,start,duration,reference);
        }else{
            throw e;
        }
    }
    //profileEnd();
  }
  
  public ChannelMap fetch(long arg0, ChannelMap arg1) throws SAPIException{
    if(!isConnected()){
      init();
    }
    ChannelMap ret;
    profileStart();
    ret= sink_.Fetch(arg0,arg1);
    profileEnd();
    return ret;
  }
  
  public void monitor(ChannelMap cm, int gapControl) throws SAPIException{
    //if(state_!=STATE_MONITOR){
      //log.debug("reinit to monitor state");
      //reInit();
      state_=STATE_MONITOR;
    //}
    if(!isConnected()){
      init();
    }
    //log.debug("monitor call");
    //profileStart();
    //sink_.Monitor(cm,gapControl);
    sink_.Subscribe(cm,0.0,0.5,"newest");
    //sink_.Subscribe(cm);
    //profileEnd();
  }
  
  public boolean monitoring(){
    return (state_==STATE_MONITOR);
  }
  
}
