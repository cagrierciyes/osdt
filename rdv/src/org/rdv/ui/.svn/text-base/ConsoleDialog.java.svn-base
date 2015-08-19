/*  ConsoleDialog.java
 *  Subclass of JDialog
 *
 *  Copyright (c) 2010 Andrew Moraczewski
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *  Author: Andrew Moraczewski
 *  Date: June 23, 2010
 *  
 *  Source Code based on code from http://www.exampledepot.com/egs/javax.swing.text/ta_Console.html
 */

package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.ResourceMap;
import org.rdv.RDV;

public class ConsoleDialog extends JDialog implements java.util.Observer {
  /** serialization version identifier */
  private static final long serialVersionUID = -8875776463661389468L;
  private static final int messageLimit=2048;
  
  static MessageBuffer messageBuffer = new MessageBuffer();
  
  /** text area in which to display the console information */
  JTextArea textArea;
  private boolean scrollLock_=false;
  
  public ConsoleDialog(JDialog owner) {
    super(owner);
    
    setName("consoleDialog");
    
    setDefaultCloseOperation(AboutDialog.DISPOSE_ON_CLOSE);
    
    JPanel container = new JPanel();
    container.setLayout(new BorderLayout());
    container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setContentPane(container);
    
    InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = container.getActionMap();
    
    Action disposeAction = new AbstractAction() {
      /** serialized version identifier */
      private static final long serialVersionUID = 4380189911762232261L;

      public void actionPerformed(ActionEvent ae) {
        dispose();
      }
    };
    
    Action copyAction = new AbstractAction() {

      /** serialized version identifier */
      private static final long serialVersionUID = 2596081241883913660L;

      public void actionPerformed(ActionEvent e) {
        textArea.selectAll();
        textArea.copy();
      }
    };
    
//    Action scrollLockAction = new AbstractAction() {
//      /** serialized version identifier */
//      private static final long serialVersionUID = -8089076016097529064L;
//
//      public void actionPerformed(ActionEvent e) {
//        //toggle scroll lock
//        scrollLock_=!scrollLock_;
//      }
//    };
    
    disposeAction.putValue(Action.NAME, "OK");
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "dispose");
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "dispose");
    actionMap.put("dispose", disposeAction); 
    copyAction.putValue(Action.NAME, "Copy");
    actionMap.put("copy", copyAction);
//    actionMap.put("scroll lock", scrollLockAction);
//    scrollLockAction.putValue(Action.NAME, "Scroll Lock");
    
    textArea = new JTextArea();
    textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    textArea.setBackground(Color.WHITE);
    textArea.setForeground(Color.BLACK);
    textArea.setEditable(false);
    textArea.setLineWrap(false);
  
    Iterator<String> msgIt = messageBuffer.getMessages().iterator();
    
    //add all the messages in the buffer to this point
    while(msgIt.hasNext()){
      addMessage(msgIt.next());
    }
      
    
    JScrollPane scrollPane = 
        new JScrollPane(textArea,
                        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollPane.setPreferredSize(new Dimension(640, 480));
    container.add(scrollPane, BorderLayout.CENTER);
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BorderLayout());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    JButton okButton = new JButton(disposeAction);
    buttonPanel.add(okButton, BorderLayout.EAST);
    
    JPanel leftBtnsPanel=new JPanel();
    leftBtnsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.add(leftBtnsPanel,BorderLayout.WEST);
    
    JButton copyButton = new JButton(copyAction);
    leftBtnsPanel.add(copyButton);
    
//    JButton scrollLockButton = new JButton(scrollLockAction);
//    leftBtnsPanel.add(scrollLockButton);
    
    container.add(buttonPanel, BorderLayout.SOUTH);
    
    // inject resources from the properties for this component
    ResourceMap resourceMap = RDV.getInstance().getContext().getResourceMap(getClass());
    resourceMap.injectComponents(this);
    
    pack();
    okButton.requestFocusInWindow();
    setLocationByPlatform(true);
    setVisible(true);
    
    messageBuffer.addObserver(this);
  }
  
  public void update(java.util.Observable o, Object arg)
  {
    
    addBufferedMessages();
    
//    // Keep the text area down to a certain character size
//    int idealSize = 10000;
//    int maxExcess = 500;
//    int excess = textArea.getDocument().getLength() - idealSize;
//    if (excess >= maxExcess) {
//        textArea.replaceRange("", 0, excess);
//    }
  }
  
  private void addBufferedMessages(){
    Iterator<String> msgIt = messageBuffer.getMessages().iterator();
    
    //add all the messages in the buffer to this point
    while(msgIt.hasNext()){
      addMessage(msgIt.next());
    }
    
    pruneOldMessages();
    
    if(!scrollLock_)
      textArea.setCaretPosition(textArea.getDocument().getLength());
  }
  
  private void addMessage(String message){
    textArea.append(message + "\n"); 
  }
  
  private void pruneOldMessages(){
    // prune old messages
    try{
      String textString= textArea.getDocument().getText(0, 
                        textArea.getDocument().getLength());
      int lineCount=0;
      for(int i=textString.length()-1;i>=0;i--){
        if(textString.charAt(i)=='\n'){
          if(++lineCount>messageLimit){
            // delete old lines
            textArea.replaceRange("", 0, i+1);
            break;
          }
        }
      }
    }catch(BadLocationException ble){
      ble.printStackTrace();
    }
  }
  
  public static class ConsoleLogger implements Log
  {
    public enum LogLevel { TRACE, DEBUG, INFO, WARN, ERROR, FATAL }
    
    private boolean traceEnabled, debugEnabled, infoEnabled, warnEnabled, errorEnabled, fatalEnabled;
    
    private DateFormat m_dateFormat = new SimpleDateFormat("KK:mm:ss a");
    
    public ConsoleLogger(String msg) {
      
      String logLevel = (String) LogFactory.getFactory()
              .getAttribute("org.apache.commons.logging.Log.defaultlog");
      if(logLevel==null || logLevel.isEmpty()){
        logLevel="info";
      }
      
      //set logging system level
      traceEnabled = (logLevel.compareToIgnoreCase("trace")==0);
      debugEnabled = traceEnabled ||
                      (logLevel.compareToIgnoreCase("debug")==0);
      infoEnabled = traceEnabled || debugEnabled ||
                    (logLevel.compareToIgnoreCase("info")==0);
      warnEnabled = traceEnabled || debugEnabled || infoEnabled ||
                    (logLevel.compareToIgnoreCase("warn")==0);
      errorEnabled = true;
      fatalEnabled = true;
    }
    
    protected void addMessage(LogLevel level, String msg)
    {
      String logMessage = String.format("[%s] %s - %s", level.toString(), m_dateFormat.format(new Date(System.currentTimeMillis())), msg);
      
      messageBuffer.addMessage(logMessage);
      
      //reroute logging information to STDOUT and STDERR depending on severity
      switch(level) {
        case TRACE:
        case DEBUG:
        case INFO:
        case WARN:
          System.out.println(logMessage);
          break;
        case ERROR:
        case FATAL:
          System.err.println(logMessage);
          break;
        default:
          break;
      }
    }

    @Override
    public void debug(Object message) {
      if(isDebugEnabled()) {
        addMessage(LogLevel.DEBUG, message.toString());
      }
    }

    @Override
    public void debug(Object arg0, Throwable arg1) {
      if(isDebugEnabled()) {
        addMessage(LogLevel.DEBUG, arg1.getMessage());
      }
    }

    @Override
    public void error(Object arg0) {
      if(isErrorEnabled()) {
        addMessage(LogLevel.ERROR, arg0.toString());
      }
    }

    @Override
    public void error(Object arg0, Throwable arg1) {
      if(isErrorEnabled()) {
        addMessage(LogLevel.ERROR, arg1.getMessage());
      }
    }

    @Override
    public void fatal(Object arg0) {
      if(isFatalEnabled()) {
        addMessage(LogLevel.FATAL, arg0.toString());
      }
    }

    @Override
    public void fatal(Object arg0, Throwable arg1) {
      if(isFatalEnabled()) {
        addMessage(LogLevel.FATAL, arg0.toString());
      }
    }

    @Override
    public void info(Object message) {
      if(isInfoEnabled()) {
        addMessage(LogLevel.INFO, message.toString());
      }
    }
    
    @Override
    public void info(Object arg0, Throwable arg1) {
      if(isInfoEnabled()) {
        addMessage(LogLevel.INFO, arg1.getMessage());
      }
    }
    
    @Override
    public void trace(Object arg0) {
      if(isTraceEnabled()) {
        addMessage(LogLevel.TRACE, arg0.toString());
      }
    }

    @Override
    public void trace(Object arg0, Throwable arg1) {
      if(isTraceEnabled()) {
        addMessage(LogLevel.TRACE, arg1.getMessage());
      }
    }

    @Override
    public void warn(Object arg0) {
      if(isWarnEnabled()) {
        addMessage(LogLevel.WARN, arg0.toString());
      }
    }

    @Override
    public void warn(Object arg0, Throwable arg1) {
      if(isWarnEnabled()) {
        addMessage(LogLevel.WARN, arg1.getMessage());
      }
    }

    @Override
    public boolean isTraceEnabled() {
      return traceEnabled;
    }

    @Override
    public boolean isDebugEnabled() {
      return (debugEnabled || traceEnabled);
    }

    @Override
    public boolean isInfoEnabled() {
      return (infoEnabled || debugEnabled || traceEnabled);
    }
    
    @Override
    public boolean isWarnEnabled() {
      return (warnEnabled || infoEnabled || debugEnabled || traceEnabled);
    }

    @Override
    public boolean isErrorEnabled() {
      return (errorEnabled || warnEnabled || infoEnabled || debugEnabled || traceEnabled);
    }

    @Override
    public boolean isFatalEnabled() {
      return (fatalEnabled || errorEnabled || warnEnabled || infoEnabled || debugEnabled || traceEnabled);
    }
  }
  protected static class MessageBuffer extends java.util.Observable
  {
    private List<String> m_buffer = new ArrayList<String>();
    
    public synchronized void addMessage(String message)
    {
      m_buffer.add(message);
      if(m_buffer.size()>messageLimit){
        m_buffer.remove(0);
      }
      setChanged();
      notifyObservers(message);
    }
    
    public synchronized List<String> getMessages()
    {
      List<String>ret=new ArrayList<String>(m_buffer);
      m_buffer.clear();
      return ret;
    }
    
    
  }

  
  
}