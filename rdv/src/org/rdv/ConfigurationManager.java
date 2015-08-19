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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/ConfigurationManager.java $
 * $Revision: 1304 $
 * $Date: 2008-12-01 11:14:37 -0500 (Mon, 01 Dec 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.data.LocalChannel;
import org.rdv.data.LocalChannelManager;
import org.rdv.datapanel.DataPanel;
import org.rdv.rbnb.RBNBController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * A class to manage the application configuration.
 * 
 * @author  Jason P. Hanley
 * @since   1.3
 */
public class ConfigurationManager {
  /**
   * The logger for this class.
   * 
   * @since  1.3
   */
  static Log log = LogFactory.getLog(ConfigurationManager.class.getName());

  public static void applyConfig(String s) {
    InputStream is = new ByteArrayInputStream(s.getBytes());
    applyConfig(is);
  }
  
  public static void applyConfig(URL url) {
    try {
      applyConfig(url.openStream());
    } catch (Exception e) {
      DataViewer.alertError("Unable to load configuration file.");
      e.printStackTrace();
      return;
    }
  }
  
  /**
  * Load the configuration file from the specified URL and configure the
  * application. This spawns a new thread to do this in the background.
  *
  * @param in  the input stream to load the configuration from
  */
  public static void applyConfig(final InputStream in) {
    new Thread() {
           public void run() {
               
               Document document;
               try {
                 DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();    
                 document = documentBuilder.parse(in);
                 applyConfigWorker(document);
               } catch (Exception e) {
                 e.printStackTrace();
                 DataViewer.alertError("The configuration file is corrupt.");
                 return;
               }
             }
    }.start();
  }
 
  private static void applyConfigWorker(Document document) throws Exception{
    XPath xp = XPathFactory.newInstance().newXPath();
    
    Boolean foundPanelManagerSection= (Boolean)xp.evaluate("rdv/panelManager", document, XPathConstants.BOOLEAN);
    if(foundPanelManagerSection){
      applyConfigVer2(document);
    }else{
      applyConfigVer1(document);
    }
    
  }
  
  private static void applyRBNBStateConfig(Document document) throws XPathExpressionException{
    RBNBController rbnb = RBNBController.getInstance();
    XPath xp = XPathFactory.newInstance().newXPath();
    
    Node rbnbNode= (Node)xp.evaluate("rdv/rbnb/state[1]", document, XPathConstants.NODE);
    int state = RBNBController.getState(rbnbNode.getTextContent());
    rbnb.setState(state);
  }
  
  private static void applyRBNBConfig(Document document) throws XPathExpressionException{

    RBNBController rbnb = RBNBController.getInstance();
    XPath xp = XPathFactory.newInstance().newXPath();
  
    if (rbnb.getState() != RBNBController.STATE_DISCONNECTED) {
      rbnb.pause();
    }
    
    Node rbnbNode= (Node)xp.evaluate("rdv/rbnb[1]", document, XPathConstants.NODE);
    NodeList rbnbNodes = rbnbNode.getChildNodes();

    String host = findChildNodeText(rbnbNodes, "host");
    int port = Integer.parseInt(findChildNodeText(rbnbNodes, "port"));
    if (!rbnb.getRBNBHostName().equals(host) || rbnb.getRBNBPortNumber() != port) {
      rbnb.disconnect();

      int RETRY_LIMIT = 240;
      int tries = 0;
      while (tries++ < RETRY_LIMIT && rbnb.getState() != RBNBController.STATE_DISCONNECTED) {
        try { Thread.sleep(250);  } catch (InterruptedException e) {}
      }

      if (tries >= RETRY_LIMIT) {
        return;
      }

      rbnb.setRBNBHostName(host);
      rbnb.setRBNBPortNumber(port);
    }
//    } else {
//      localChannelManager.removeAllChannels();
//    }

    double timeScale = Double.parseDouble(findChildNodeText(rbnbNodes, "timeScale"));
    rbnb.setGlobalTimeScale(timeScale);

    double playbackRate = Double.parseDouble(findChildNodeText(rbnbNodes, "playbackRate"));
    rbnb.setPlaybackRate(playbackRate);

    int state = RBNBController.getState(findChildNodeText(rbnbNodes, "state"));
    if (state != RBNBController.STATE_DISCONNECTED) {
      if (!rbnb.connect(true)) {
        return;
      }
    }

  }
  
  private static void applyLocalChannelConfig(Document document) throws XPathExpressionException{
    XPath xp = XPathFactory.newInstance().newXPath();
    LocalChannelManager localChannelManager = LocalChannelManager.getInstance();
    localChannelManager.removeAllChannels();
    
    NodeList localChannelNodes = (NodeList)xp.evaluate("rdv/localChannels/localChannel", document, XPathConstants.NODESET);
    for (int i=0; i<localChannelNodes.getLength(); i++) {
      Node localChannelNode = localChannelNodes.item(i);
      String name = localChannelNode.getAttributes().getNamedItem("name").getNodeValue();
      Node unitNode = localChannelNode.getAttributes().getNamedItem("unit");
      String unit = (unitNode != null) ? unitNode.getNodeValue() : null;
      String formula = localChannelNode.getAttributes().getNamedItem("formula").getNodeValue();

      Map<String,String> variables = new HashMap<String,String>();

      NodeList variableNodes = (NodeList)xp.evaluate("variable", localChannelNode, XPathConstants.NODESET);
      for (int j=0; j<variableNodes.getLength(); j++) {
        Node variableNode = variableNodes.item(j);
        String variableName = variableNode.getAttributes().getNamedItem("name").getNodeValue();
        String variableChannel = variableNode.getAttributes().getNamedItem("channel").getNodeValue();
        variables.put(variableName, variableChannel);
      }

      try {
        LocalChannel localChannel = new LocalChannel(name, unit, variables, formula);
        localChannelManager.setChannel(localChannel);
      } catch (Exception e) {
        log.error("Failed to add local channel: " + name + ".");
      }
    }

  }
  
  private static void applyDataPanelConfigVer1(Document document) throws XPathExpressionException{
    XPath xp = XPathFactory.newInstance().newXPath();
    PanelManager dataPanelManager = ManagerStore.getPanelManager();
    
    NodeList dataPanelNodes = (NodeList)xp.evaluate("rdv/dataPanel", document, XPathConstants.NODESET);
    for (int i=0; i<dataPanelNodes.getLength(); i++) {
      Node dataPanelNode = dataPanelNodes.item(i);
      String id = dataPanelNode.getAttributes().getNamedItem("id").getNodeValue();

      /* extract the tab name field from xml */
      Node tabNode = (Node)xp.evaluate("tab", dataPanelNode, XPathConstants.NODE);
      String tabName = tabNode.getTextContent();
      
      DataPanel dataPanel;
      try {
        dataPanel = ExtensionManager.getInstance()
                                       .createDataPanelFromExtension(id);          
        int viewId=dataPanelManager.addDataPanelToContainer(dataPanel, tabName);
        dataPanelManager.showView(viewId);
      } catch (Exception e) {
        continue;
      }

      NodeList entryNodes = (NodeList)xp.evaluate("properties/entry", dataPanelNode, XPathConstants.NODESET);
      for (int j=0; j<entryNodes.getLength(); j++) {
        String key = entryNodes.item(j).getAttributes().getNamedItem("key").getNodeValue();
        String value = entryNodes.item(j).getTextContent();
        dataPanel.setProperty(key, value);
      }        

      NodeList channelNodes = (NodeList)xp.evaluate("channels/channel", dataPanelNode, XPathConstants.NODESET);
      if(channelNodes.getLength()>0){
        Collection<String> c=new ArrayList<String>();
        for (int j=0; j<channelNodes.getLength(); j++) {
          String channel = channelNodes.item(j).getTextContent();
          c.add(channel);
        }
      
        addChannelsToPanel(dataPanel, c);
      }
    }

  }

  /**
   * Subscribes the data panel to all channels in the list.  Prints 
   * a log error message if one or more subscription fails.  
   * @param dataPanel
   * @param channels
   */
  private static void addChannelsToPanel(DataPanel dataPanel, Collection<String> channels)
  {
    boolean added;
    List<String> addFailChans = new ArrayList<String>();
    
    dataPanel.startAddingChannels();
    Iterator<String> chanIt=channels.iterator();
    while(chanIt.hasNext()){
      String chanName=chanIt.next();
      if (dataPanel.supportsMultipleChannels()) {
        added = dataPanel.addChannel(chanName);
      }else{
        added = dataPanel.setChannel(chanName);
      }
      if(!added){
         addFailChans.add(chanName);
      }
    }
    dataPanel.stopAddingChannels();
  
    if (addFailChans.size()>0) {
      StringBuilder channelMsg = new StringBuilder();
      Iterator<String> it=addFailChans.iterator();
      while(it.hasNext()){
        channelMsg.append(it.next());
        if (it.hasNext()){
          channelMsg.append(", ");
        }
      }
      log.error("Failed to add channels " + channelMsg + ".");
    } 
  }
  
  private static void applyDataPanelConfigVer2(Document document) throws XPathExpressionException{
    XPath xp = XPathFactory.newInstance().newXPath();
    PanelManager dataPanelManager = ManagerStore.getPanelManager();
    
    NodeList dataPanelNodes = (NodeList)xp.evaluate("rdv/dataPanel", document, XPathConstants.NODESET);
    for (int i=0; i<dataPanelNodes.getLength(); i++) {
      Node dataPanelNode = dataPanelNodes.item(i);
      String id = dataPanelNode.getAttributes().getNamedItem("id").getNodeValue();

      /* extract the view id field from xml */
      Node viewIdNode = (Node)xp.evaluate("viewId", dataPanelNode, XPathConstants.NODE);
      int viewId = Integer.parseInt(viewIdNode.getTextContent());
      
      DataPanel dataPanel;
      try {
        dataPanel = ExtensionManager.getInstance()
                                       .createDataPanelFromExtension(id);          
        dataPanelManager.addDataPanel(viewId,dataPanel);
      } catch (Exception e) {
        continue;
      }

      NodeList entryNodes = (NodeList)xp.evaluate("properties/entry", dataPanelNode, XPathConstants.NODESET);
      for (int j=0; j<entryNodes.getLength(); j++) {
        String key = entryNodes.item(j).getAttributes().getNamedItem("key").getNodeValue();
        String value = entryNodes.item(j).getTextContent();
        dataPanel.setProperty(key, value);
      }        

      NodeList channelNodes = (NodeList)xp.evaluate("channels/channel", dataPanelNode, XPathConstants.NODESET);
      if(channelNodes.getLength()>0){
        Collection<String> c=new ArrayList<String>();
        for (int j=0; j<channelNodes.getLength(); j++) {
          String channel = channelNodes.item(j).getTextContent();
          c.add(channel);
        }
      
        addChannelsToPanel(dataPanel,c);       
      }
    }

  }

  private static void initPanelManagerVer2(Document document) 
    throws XPathExpressionException, ParserConfigurationException {
    XPath xp = XPathFactory.newInstance().newXPath();
    ManagerStore.getPanelManager().closeAllDataPanels();
    
    Node typeNode = (Node)xp.evaluate("rdv/panelManager/type", 
                                  document, XPathConstants.NODE);
    
    if(typeNode!=null && 
        typeNode.getTextContent().compareToIgnoreCase("docking")==0){
      ManagerStore.swapDockingPanelManager();
    }else{
      ManagerStore.swapBasicPanelManager();
    }
    
//    Node layoutNode = (Node)xp.evaluate("rdv/panelManager/windowLayout", 
//                                        document, XPathConstants.NODE);
//    
//    DocumentBuilder documentBuilder = DocumentBuilderFactory.
//                              newInstance().newDocumentBuilder();
//    
//    Document layoutDoc = documentBuilder.newDocument();
//    layoutDoc.importNode(layoutNode, true);
//    
//    ManagerStore.getPanelManager().setConfigurationXML(layoutDoc);    
  }
  
  private static void initPanelManagerVer1(Document document) {
    PanelManager dataPanelManager = ManagerStore.getPanelManager();
    dataPanelManager.closeAllDataPanels();
    ManagerStore.swapBasicPanelManager(); 
  }
  
  private static void applyWindowLayoutConfig(Document document) 
                                          throws Exception{
    XPath xp = XPathFactory.newInstance().newXPath();
    PanelManager dataPanelManager = ManagerStore.getPanelManager();
    
    Node layoutNode = (Node)xp.evaluate("rdv/panelManager/windowLayout",
                                        document, XPathConstants.NODE);
    
    DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().
                                            newDocumentBuilder();
    
    Document layoutDoc = documentBuilder.newDocument();
    Node importNode=layoutDoc.importNode(layoutNode, true);
    layoutDoc.appendChild(importNode);

    dataPanelManager.setConfigurationXML(layoutDoc);
  }
  
  
  private static void applyConfigVer1(Document document) throws XPathExpressionException{
    initPanelManagerVer1(document);
    applyRBNBConfig(document);
    applyLocalChannelConfig(document);
    applyDataPanelConfigVer1(document);
    applyRBNBStateConfig(document);
  }

  private static void applyConfigVer2(Document document) throws Exception{
    initPanelManagerVer2(document);
    applyRBNBConfig(document);
    applyLocalChannelConfig(document);
    applyDataPanelConfigVer2(document);
    applyWindowLayoutConfig(document);
    applyRBNBStateConfig(document);
  }
  
//  public static void applyConfig(String xmlString, String configName) {
//    System.out.println("CONFIGSTRING\n\n" + xmlString);
//    RBNBController rbnb = RBNBController.getInstance();
//    LocalChannelManager localChannelManager = LocalChannelManager.getInstance();
//    DataPanelManager dataPanelManager = DataPanelManager.getInstance();
//
//    if (rbnb.getState() != RBNBController.STATE_DISCONNECTED) {
//      rbnb.pause();
//    }
//    dataPanelManager.closeAllDataPanels();
//
//    Document document;
//    try {
//      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//      InputStream is = new ByteArrayInputStream(xmlString.getBytes());
//      document = documentBuilder.parse(is);
//    } catch (Exception e) {
//      DataViewer.alertError("The configuration file is corrupt.");
//      return;
//    }
//
//    XPath xp = XPathFactory.newInstance().newXPath();
//
//    try {
//      Node rbnbNode= (Node)xp.evaluate("configName/rdv/rbnb[1]", document, XPathConstants.NODE);
//      NodeList rbnbNodes = rbnbNode.getChildNodes();
//
//      String configNodeName = findChildNodeValue(rbnbNodes, "configName", configName);
//      if(configNodeName != null) {
//        String host = findChildNodeText(rbnbNodes, "host");
//        int port = Integer.parseInt(findChildNodeText(rbnbNodes, "port"));
//        if (!rbnb.getRBNBHostName().equals(host) || rbnb.getRBNBPortNumber() != port) {
//          rbnb.disconnect();
//
//          int RETRY_LIMIT = 240;
//          int tries = 0;
//          while (tries++ < RETRY_LIMIT && rbnb.getState() != RBNBController.STATE_DISCONNECTED) {
//            try { Thread.sleep(250);  } catch (InterruptedException e) {}
//          }
//
//          if (tries >= RETRY_LIMIT) {
//            return;
//          }
//
//          rbnb.setRBNBHostName(host);
//          rbnb.setRBNBPortNumber(port);
//        } else {
//          localChannelManager.removeAllChannels();
//        }
//
//        double timeScale = Double.parseDouble(findChildNodeText(rbnbNodes, "timeScale"));
//        rbnb.setGlobalTimeScale(timeScale);
//
//        double playbackRate = Double.parseDouble(findChildNodeText(rbnbNodes, "playbackRate"));
//        rbnb.setPlaybackRate(playbackRate);
//
//        int state = RBNBController.getState(findChildNodeText(rbnbNodes, "state"));
//        if (state != RBNBController.STATE_DISCONNECTED) {
//          if (!rbnb.connect(true)) {
//            return;
//          }
//        }
//
//        NodeList localChannelNodes = (NodeList)xp.evaluate("configName/rdv/localChannel", document, XPathConstants.NODESET);
//        for (int i=0; i<localChannelNodes.getLength(); i++) {
//          Node localChannelNode = localChannelNodes.item(i);
//          String name = localChannelNode.getAttributes().getNamedItem("name").getNodeValue();
//          Node unitNode = localChannelNode.getAttributes().getNamedItem("unit");
//          String unit = (unitNode != null) ? unitNode.getNodeValue() : null;
//          String formula = localChannelNode.getAttributes().getNamedItem("formula").getNodeValue();
//
//          Map<String,String> variables = new HashMap<String,String>();
//
//          NodeList variableNodes = (NodeList)xp.evaluate("variable", localChannelNode, XPathConstants.NODESET);
//          for (int j=0; j<variableNodes.getLength(); j++) {
//            Node variableNode = variableNodes.item(j);
//            String variableName = variableNode.getAttributes().getNamedItem("name").getNodeValue();
//            String variableChannel = variableNode.getAttributes().getNamedItem("channel").getNodeValue();
//            variables.put(variableName, variableChannel);
//          }
//
//          try {
//            LocalChannel localChannel = new LocalChannel(name, unit, variables, formula);
//            LocalChannelManager.getInstance().addChannel(localChannel);
//          } catch (Exception e) {
//            log.error("Failed to add local channel: " + name + ".");
//          }
//        }
//
//        NodeList dataPanelNodes = (NodeList)xp.evaluate("configName/rdv/dataPanel", document, XPathConstants.NODESET);
//        for (int i=0; i<dataPanelNodes.getLength(); i++) {
//          Node dataPanelNode = dataPanelNodes.item(i);
//          String id = dataPanelNode.getAttributes().getNamedItem("id").getNodeValue();
//
//          /* extract the tab name field from xml */
//          Node tabNode = (Node)xp.evaluate("tab", dataPanelNode, XPathConstants.NODE);
//          String tabName = tabNode.getTextContent();
//
//          DataPanel dataPanel;
//          try {
//            dataPanel = dataPanelManager.createDataPanel(id, tabName);
//          } catch (Exception e) {
//            continue;
//          }
//
//          NodeList entryNodes = (NodeList)xp.evaluate("properties/entry", dataPanelNode, XPathConstants.NODESET);
//          for (int j=0; j<entryNodes.getLength(); j++) {
//            String key = entryNodes.item(j).getAttributes().getNamedItem("key").getNodeValue();
//            String value = entryNodes.item(j).getTextContent();
//            dataPanel.setProperty(key, value);
//          }        
//
//          NodeList channelNodes = (NodeList)xp.evaluate("channels/channel", dataPanelNode, XPathConstants.NODESET);
//          Collection<String> c=new ArrayList<String>();
//          for (int j=0; j<channelNodes.getLength(); j++) {
//            String channel = channelNodes.item(j).getTextContent();
//            c.add(channel);
//          }
//          boolean added;
//          if (dataPanel.supportsMultipleChannels()) {
//            added = dataPanel.addChannels(c);
//          } else {
//            added = dataPanel.setChannel(c.iterator().next());
//          }
//
//          if (!added) {
//            StringBuffer channelMsg=new StringBuffer();
//            Iterator<String> it=c.iterator();
//            while(it.hasNext()){
//              channelMsg.append(it.next());
//              if (it.hasNext()){
//                channelMsg.append(", ");
//              }
//            }
//            log.error("Failed to add channels " + channelMsg + ".");
//          }       
//        }
//
//        rbnb.setState(state);
//      } else {
//        log.error("Failed to find config " + configName + ".");
//      }
//
//    } catch (XPathExpressionException e) {
//      e.printStackTrace();
//    }
//  }
  
  /**
   * Prints the current program state to an output stream.
   */
  public static void printConfigXml(PrintWriter out) throws ParserConfigurationException {
    out.print(getConfigurationXml());
    out.flush();
  }
  
//  public static void printConfig(PrintWriter out) {
//    
//    out.println("<?xml version=\"1.0\"?>");
//    out.println("<rdv>");
//    
//    RBNBController rbnb = RBNBController.getInstance();
//    out.println("  <rbnb>");
//    out.print("    <host>" + rbnb.getRBNBHostName() + "</host>\n");
//    out.print("    <port>" + rbnb.getRBNBPortNumber() + "</port>\n");
//    out.print("    <state>" + RBNBController.getStateName(rbnb.getState()) + "</state>\n");
//    out.print("    <timeScale>" + rbnb.getTimeScale() + "</timeScale>\n");
//    out.print("    <playbackRate>" + rbnb.getPlaybackRate() + "</playbackRate>\n");
//    out.print("  </rbnb>\n");
//    
//    LocalChannelManager localChannelManager = LocalChannelManager.getInstance();
//    if (localChannelManager.hasChannels()) {
//      for (LocalChannel localChannel : localChannelManager.getChannels()) {
//        out.print("  <localChannel name=\"" + localChannel.getName() + "\"");
//        if (localChannel.getUnit() != null) {
//          out.print(" unit=\"" + localChannel.getUnit() + "\"");
//        }
//        out.print(" formula=\"" + localChannel.getFormula() + "\">\n");
//        for (Entry<String, String> variable : localChannel.getVariables().entrySet()) {
//          out.print("    <variable name=\"" + variable.getKey() + "\" channel=\"" + variable.getValue() + "\"/>\n");
//        }
//        out.print("  </localChannel>\n");
//      }
//    }
//    
//    Map<DataPanel, String> dataPanelMap = ManagerStore.getPanelManager().getDataPanels();
//    Iterator<DataPanel> it = dataPanelMap.keySet().iterator();
//    while (it.hasNext()) {
//      DataPanel dataPanel = it.next();
//      Properties properties = dataPanel.getProperties();
//      
//      if (isPanelDetached(dataPanel, properties)) {
//        if (dataPanel.subscribedChannelCount() == 0)  // A detached panel with no channels subscribed, or a non-existing detached panel 
//          continue; // don't add to configuration
//      }
//      
//      out.print("  <dataPanel id=\"" + dataPanel.getClass().getName() + "\">\n");
//      
//      if (dataPanel.subscribedChannelCount() > 0) {
//        out.print("    <channels>\n");
//        Iterator<String> channels = dataPanel.subscribedChannels().iterator();
//        while (channels.hasNext()) {
//          String channel = channels.next();
//          out.print("      <channel>" + channel + "</channel>\n");
//        }     
//        out.print("    </channels>\n");
//      }
//
//
//      if (properties.size() > 0) {
//        out.print("    <properties>\n");
//        for (Enumeration<?> keys = properties.propertyNames(); keys.hasMoreElements() ;) {
//           String key = (String)keys.nextElement();
//           String value = properties.getProperty(key);
//           out.print("      <entry key=\"" + key + "\">" + value + "</entry>\n");
//        }
//        out.print("    </properties>\n");
//      }
//      
//      String tabName = dataPanelMap.get(dataPanel);
//      out.print("    <tab>" + tabName + "</tab>\n");
//      
//      out.print("  </dataPanel>\n");
//    }
//    
//    dataPanelMap = null;
//    it = null;
//    
//    out.print("</rdv>");
//    out.flush();
//  }
  
  /**
   * Inspect if the given DataPanel is detached from the DataPanelContainer
   * @param dataPanel to check
   * @param properties properties for the DataPanel
   * @return flag indicating detached
   */
  private static boolean isPanelDetached(DataPanel dataPanel, Properties properties) {
    if (properties.size() == 0) {
      return false;
    }
    
    String key, value;
    for (Enumeration<?> keys = properties.propertyNames(); keys.hasMoreElements() ;) {
      key = (String)keys.nextElement();
      if (key == "attached") {
        value = properties.getProperty(key);
        if (value == "false")
          return true;          
      }
    }
    
    return false;
  }
  
  /**
   * This function takes a config name and returns an xml string which can then be saved
   * @param configName
   * @return
   */
  /*public static String createNewConfig(String configName) {
    StringBuffer out = new StringBuffer();
    out.append("<?xml version=\"1.0\"?>");
    out.append(getCurrentConfig(configName));
    return out.toString();
  }*/
  
  /**
   * This function takes a config xml string and adds or removes the desired config
   * @param xmlString the existing xml string
   * @param configName the xml string to add or remove
   * @param add add this is true, remove this if false
   * @return the new modified String
   */
  /*public static String modifyExistingConfig(String xmlString, String configName, boolean add) {
    Document document;
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      InputStream is = new ByteArrayInputStream(xmlString.getBytes());
      document = documentBuilder.parse(is);
    } catch (Exception e) {
      DataViewer.alertError("The configuration file is corrupt.");
      return xmlString;
    }

    XPath xp = XPathFactory.newInstance().newXPath();
    
    try {
      NodeList configNodes = (NodeList)xp.evaluate("configName", document, XPathConstants.NODESET);
      if(configNodes != null) {
        boolean nodeFound = false;
        for(int i=0; i < configNodes.getLength(); i++) {
          Node node = configNodes.item(i);
          if(node.getAttributes().getNamedItem("id").getNodeValue().equals(configName)) {
            nodeFound = true;
            if(add == false) {
              //remove the node
              document.removeChild(node);
            } else {
              //overwrite the node
              Node newNode = document.createTextNode(getCurrentConfig(configName));
              document.appendChild(newNode);
            }
          }
        }
        try {
          if(nodeFound == false) {
            // node not found, so get the current configuration and turn it into xml, adding it into the document
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(getCurrentConfig(configName).getBytes());
            Node newNode = documentBuilder.parse(is);
            document.importNode(newNode, true);
          } else {
            //node already modified correctly
          }
        } catch(Exception e) {
          log.debug(getCurrentConfig(configName));
          DataViewer.alertError("New created configuration is corrupt.");
        }
      } else {
        log.error("No configName nodes exist");
      }
    } catch (XPathExpressionException e) {
      log.error("Failed to modify config xml string");
      e.printStackTrace();
    }
    
    return document.getTextContent();
  }
  
  private static String findChildNodeValue(NodeList nodes, String nodeName, String value) {
    for(int i=0; i <nodes.getLength(); i++) {
      Node node = (Node)nodes.item(i);
      if(node.getNodeName().equals(nodeName) && node.getAttributes().getNamedItem("id").getNodeValue().equals(value)) {
        return node.getTextContent();
      }
    }
    return null;
  }
  */
  private static String findChildNodeText(NodeList nodes, String nodeName) {
    for (int i=0; i<nodes.getLength(); i++) {
      Node node = (Node)nodes.item(i);
      if (node.getNodeName().equals(nodeName)) {
        return node.getTextContent();
      }
    }
    return null;
  }
  
  /**
   * Serialize program state to an xml document using the DOM.
   * @return xml document as string.
   * @throws ParserConfigurationException
   */
  private static String getConfigurationXml() throws ParserConfigurationException {
    RBNBController rbnb = RBNBController.getInstance();
    DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
  
    Document document = documentBuilder.newDocument();
    
    Element rdvEl = document.createElement("rdv");
    document.appendChild(rdvEl);
    Element rbnbEl = document.createElement("rbnb");
    
    Element el = document.createElement("host");
    el.setTextContent(rbnb.getRBNBHostName());
    rbnbEl.appendChild(el);
    
    el = document.createElement("port");
    el.setTextContent(Integer.toString(rbnb.getRBNBPortNumber()));
    rbnbEl.appendChild(el);
    
    el = document.createElement("state");
    el.setTextContent(RBNBController.getStateName(rbnb.getState()));
    rbnbEl.appendChild(el);
    
    el = document.createElement("timeScale");
    el.setTextContent(Double.toString(rbnb.getTimeScale()));
    rbnbEl.appendChild(el);
    
    el = document.createElement("playbackRate");
    el.setTextContent(Double.toString(rbnb.getPlaybackRate()));
    rbnbEl.appendChild(el);

    rdvEl.appendChild(rbnbEl);
    
    Element localChansEl = document.createElement("localChannels");
    
    LocalChannelManager localChannelManager = LocalChannelManager.getInstance();
    if (localChannelManager.hasChannels()) {
      for (LocalChannel localChannel : localChannelManager.getChannels()) {
        Element localChanEl = document.createElement("localChannel");
        localChanEl.setAttribute("name", localChannel.getName());
        
        if (localChannel.getUnit() != null) {
          localChanEl.setAttribute("unit", localChannel.getUnit());
        }
        localChanEl.setAttribute("formula",localChannel.getFormula());
        for (Entry<String, String> variable : localChannel.getVariables().entrySet()) {
          Element varEl = document.createElement("variable");
          varEl.setAttribute("name", variable.getKey());
          varEl.setAttribute("channel", variable.getValue());
          localChanEl.appendChild(varEl);
        }
        localChansEl.appendChild(localChanEl);
      }
    }
    rdvEl.appendChild(localChansEl);
    
    Collection<DataPanel> dataPanelSet = ManagerStore.getPanelManager().getDataPanels();
    Iterator<DataPanel> it = dataPanelSet.iterator();
    while (it.hasNext()) {
      DataPanel dataPanel = it.next();
      Properties properties = dataPanel.getProperties();
      
      if (isPanelDetached(dataPanel, properties)) {
        if (dataPanel.subscribedChannelCount() == 0)  // A detached panel with no channels subscribed, or a non-existing detached panel 
          continue; // don't add to configuration
      }
    
      Element panelEl = document.createElement("dataPanel");
      rdvEl.appendChild(panelEl);
      panelEl.setAttribute("id", dataPanel.getClass().getName());
      
      Element viewIdEl=document.createElement("viewId");
      panelEl.appendChild(viewIdEl);
      viewIdEl.setTextContent(Integer.toString(
                  ManagerStore.getPanelManager().getViewId(dataPanel)));
      
      if (dataPanel.subscribedChannelCount() > 0) {
        Element chansEl=document.createElement("channels");
        Iterator<String> channels = dataPanel.subscribedChannels().iterator();
        while (channels.hasNext()) {
          String channel = channels.next();
          Node chanEl = document.createElement("channel");
          chanEl.setTextContent(channel);
          chansEl.appendChild(chanEl);
        }
        panelEl.appendChild(chansEl);
      }

      if (properties.size() > 0) {
        Element propsEl=document.createElement("properties");
        for (Enumeration<?> keys = properties.propertyNames(); keys.hasMoreElements() ;) {
          String key = (String)keys.nextElement();
          String value = properties.getProperty(key);
          Element entryEl = document.createElement("entry");
          entryEl.setAttribute("key", key);
          entryEl.setTextContent(value);
          propsEl.appendChild(entryEl);
        }
        panelEl.appendChild(propsEl);
      }
    }
    
    // write panel fragment here
    PanelManager dataPanelManager = ManagerStore.getPanelManager();
    
    Element pmEl =document.createElement("panelManager");
    rdvEl.appendChild(pmEl);
    
    Element typeEl = document.createElement("type");
    pmEl.appendChild(typeEl);
    if(dataPanelManager instanceof DockingDataPanelManager){
      typeEl.setTextContent("docking");
    }else{
      typeEl.setTextContent("basic");
    }
    
    Element layoutEl = document.createElement("windowLayout");
    pmEl.appendChild(layoutEl);
    try{
      Document panelMgrConfig= dataPanelManager.getConfigurationXML();
      Node layoutFragment = document.importNode(panelMgrConfig.getDocumentElement(), true);
      layoutEl.appendChild(layoutFragment);
    }catch(Exception e){
      
      e.printStackTrace();
    }
 
    return format(document);
  }
  
  /**
   * Pretty-print xml document, indenting elements. 
   * @param d document to format.
   * @return formatted xml.
   */
  private static String format(Document d) {
    try {
        OutputFormat format = new OutputFormat(d);
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(2);
        Writer out = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.serialize(d);
        return out.toString();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

}
