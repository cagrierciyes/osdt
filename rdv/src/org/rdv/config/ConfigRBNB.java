
package org.rdv.config;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.ConfigurationManager;
import org.rdv.rbnb.RBNBController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
/***
 * Manages RDV configurations stored an an RDV server. All stored configurations
 * are saved to one xml document in a hidden channel on the server. No
 * connection with the server is maintained.  
 * @author Drew Daugherty
 *
 */
public class ConfigRBNB {
  private static Log log = LogFactory.getLog(ConfigRBNB.class.getName());
  /// RBNB source name.
  public static final String SOURCE_NAME = "_CONFIGS";
  public static final String CHANNEL_NAME = "config.xml";

  /// configuration name to xml document map.
  private HashMap<String,String> configMap_=new HashMap<String,String>();
  
  /// Save all configurations back to RBNB server.
  public void save() {
    Source src = new Source(1, "append", 100); // 100 revisions allowed
    ChannelMap cMap = new ChannelMap();
    
    try {
      cMap.Add(CHANNEL_NAME);
      cMap.PutMime(0, "application/octet-stream");
      
      src.OpenRBNBConnection(RBNBController.getInstance().getRBNBHostName() + 
          ":" + RBNBController.getInstance().getRBNBPortNumber(), SOURCE_NAME);
      src.Register(cMap);
      
    } catch(SAPIException e) {
      log.error(e.getMessage());
      return;
    }
    
    boolean error = false;
    try {
      String out=serializeToXML();
      cMap.PutTimeAuto("server");
      byte[] byteArray = out.getBytes();
      //cMap.PutTime(0, 0);
      cMap.PutDataAsByteArray(0, byteArray);
      src.Flush(cMap);
    } catch(Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      error = true;
    }
    
    src.Detach();
    src.CloseRBNBConnection();
  }

  private void test(){
    try{
      StringWriter currCfgWriter=new StringWriter();
      ConfigurationManager.printConfigXml(
                    new PrintWriter(new BufferedWriter(currCfgWriter)));
      System.out.println("Curr Config\n============");
      System.out.println(currCfgWriter);
      addConfiguration("test",currCfgWriter.toString());
      System.out.println("Serialized Config\n============");
      String xml=serializeToXML();
      System.out.println(xml);
      deserializeFromXML(xml);
      System.out.println("Test Config\n============");
      System.out.println(configMap_.get("test"));
      
    }catch(Exception pce){
      pce.printStackTrace();
    }
    //String moddedConfig = ConfigurationManager.modifyExistingConfig(testConfig,"test2",true);
    //System.out.println("Modded Config\n============");
    //System.out.println(moddedConfig);
  }
  
  /// Add or replace a configuration.  
  public void addConfiguration(String name, String config){
     configMap_.put(name,config);
  }
  
  /// @return An xml configuration for the given name.
  public String getConfiguration(String name){
    return configMap_.get(name);
  }
  
  /// Return a list of configuration names.
  public Collection<String> getNames(){
    List<String> ret=new ArrayList<String>();
    ret.addAll(configMap_.keySet());
    Collections.sort(ret);
    return ret;
  }
  
  /**
   * Convert the stored xml to a set of named configs.
   * @param xml
   * @throws Exception
   */
  private void deserializeFromXML(String xml) throws Exception{
    configMap_.clear();
    DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    InputStream is = new ByteArrayInputStream(xml.getBytes());
    Document document = documentBuilder.parse(is);
    XPath xp = XPathFactory.newInstance().newXPath();
    NodeList configNodes = (NodeList)xp.evaluate("ConfigRBNB/Configs/Config", document, XPathConstants.NODESET);
    
    for (int i=0; i<configNodes.getLength(); i++) {
      Node configNode = configNodes.item(i);
      String name = configNode.getAttributes().getNamedItem("id").getNodeValue();
      Node rdvNode = (Node)xp.evaluate("rdv", configNode, XPathConstants.NODE);
     
      Document newRdvDoc = 
        DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Node newRdvRoot=newRdvDoc.importNode(rdvNode, true);
      newRdvDoc.appendChild(newRdvRoot);
      StringWriter xmlWriter=new StringWriter();
      OutputFormat format = new OutputFormat(newRdvDoc);
      format.setLineWidth(65);
      format.setIndenting(true);
      format.setIndent(2);
      XMLSerializer serializer = new XMLSerializer(xmlWriter, format);
      serializer.serialize(newRdvDoc);
      
      String configString=xmlWriter.toString();
      configMap_.put(name,configString);
    }
  
  }
  
  /**
   * Convert the set of named configs to an xml document.
   */
  private String serializeToXML() throws ParserConfigurationException,IOException{
    Iterator<String> cfgIt=configMap_.keySet().iterator();
  
    DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
    DocumentBuilder bd = fact.newDocumentBuilder();
    Document doc = bd.newDocument(); 
    Element root = (Element) doc.createElement("ConfigRBNB");
    Element configs = (Element) doc.createElement("Configs");
    root.appendChild(configs);
    while(cfgIt.hasNext()){
      String key=cfgIt.next();
      String config=configMap_.get(key);
      if (config!=null&&!config.isEmpty()){
        try{
            Element newConfig = (Element) doc.createElement("Config");
            newConfig.setAttribute("id", key);
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(config.getBytes());
            Document document = documentBuilder.parse(is);
            Node storeCfgNode = doc.importNode( document.getFirstChild(), true );
            newConfig.appendChild(storeCfgNode);
            // insert config XML
            configs.appendChild(newConfig);
          
        }catch(Exception e){
          e.printStackTrace();
        }
      }
    }
    
    doc.appendChild(root);
    StringWriter xmlWriter=new StringWriter();
    OutputFormat format = new OutputFormat(doc);
    format.setLineWidth(65);
    format.setIndenting(true);
    format.setIndent(2);
    XMLSerializer serializer = new XMLSerializer(xmlWriter, format);
    serializer.serialize(doc);
    
    return xmlWriter.toString();
  }
  
  /**
   * Retrieve all configurations from the server and replace
   * the local set.
   */
  public void load() {
    Sink sink = new Sink();
    try {
      sink.OpenRBNBConnection(RBNBController.getInstance().getRBNBHostName() + ":" + RBNBController.getInstance().getRBNBPortNumber(), SOURCE_NAME);
      ChannelMap cMap = new ChannelMap();
      cMap.Add(SOURCE_NAME + "/" + CHANNEL_NAME);
      sink.RequestRegistration(cMap);
      
      sink.Request(cMap, 0, 0, "newest");
      ChannelMap dataMap = sink.Fetch(-1);
      
      int index = dataMap.GetIndex(SOURCE_NAME + "/" + CHANNEL_NAME);
      if(index != -1) {
        byte[][] array = dataMap.GetDataAsByteArray(index);
        String serializedXml= new String(array[0]);
        deserializeFromXML(serializedXml);
        //ConfigurationManager.applyConfig(new String(array[0]), configName);
      }
    } catch(SAPIException e) {
      log.error(e.getMessage());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      sink.CloseRBNBConnection();
    }
  }
}
