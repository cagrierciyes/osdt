
package org.rdv.net;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

public class FTPAuthentication implements NetworkAuthentication {
  
  private FTPClient client_;
   
  private String user_;
  private String password_;
  private String host_;
  private String error_="";
  
  public FTPAuthentication(String username, String password, String host){
    //client_=new FTPClient();
    user_=username;
    password_=password;
    host_=host;
  }
  
  public boolean authenticate() {
    client_ = new FTPClient();
    //client_.addProtocolCommandListener(new PrintingCommandListener(
    //    new PrintWriter(System.out)));
 
    try {
      client_.connect(host_);
      System.out.println("Connected to " + host_ + ".");
 
      // After connection attempt, you should check the reply code to
      // verify success.
      int reply = client_.getReplyCode();
      if (!FTPReply.isPositiveCompletion(reply)) {
        error_=client_.getReplyString();
        disconnect();
        System.err.println(error_);
        return false;
      }
       
      if (!client_.login(user_, password_)) {
        client_.logout();
        error_="Authentication failed.";
        disconnect();
        return false;
      }
 
      System.out.println("Remote system is " + client_.getSystemName());
      System.out.println("My directory is " + client_.printWorkingDirectory());
 
      // Use passive mode as default because most of us are
      // behind firewalls these days.
      // client_.enterLocalPassiveMode();
    } catch (FTPConnectionClosedException cce) {
      System.err.println("Server closed connection.");
      cce.printStackTrace();
      error_="An FTP server connection error occurred: "+cce.getMessage();
      disconnect();
      return false;
    } catch (UnknownHostException uhe) {
      uhe.printStackTrace();
      error_="Unable to resolve host name "+uhe.getMessage();
      disconnect();
      return false;
    } catch (IOException ioe) {
      ioe.printStackTrace();
      error_="An IO error occurred: "+ioe.getMessage();
      disconnect();
      return false;
    }
    
    return true;
  } 
  
  private void disconnect(){ 
     if(client_!=null && client_.isConnected()) {
        try {
          client_.disconnect();
        } catch (IOException e1) {
          //ignore
        }
     }
  }
  
  public String getUserName(){return user_;};
  public String getPassword(){return password_;};
  
  public String getErrorMessage(){return error_;};
  
  public FTPClient getClient(){return client_;};
}
