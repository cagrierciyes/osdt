
package org.rdv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.data.Channel;
import org.rdv.datapanel.DataPanel;
import org.rdv.rbnb.RBNBController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExtensionManager {

  /**
   * The logger for this class.
   */
  static Log log = LogFactory.getLog(ExtensionManager.class.getName());
  
  /** the single instance of this class */
  private static ExtensionManager INSTANCE;
  
  /**
   * A list of registered extensions
   */
  private List<Extension> extensions;
  
  /**
   * The name of the extensions configuration file
   */
  private static final String EXTENSIONS_CONFIG_FILENAME = "config/extensions.xml";

  private List<String> dependenciesLoaded_=new ArrayList<String>();
  
  public static ExtensionManager getInstance(){
    if (INSTANCE == null){
      INSTANCE = new ExtensionManager();
    }
    return INSTANCE;
  }

  private ExtensionManager(){
    extensions = new ArrayList<Extension>();
    loadExtensionDepends();
    loadExtensionManifest();
  }

  private void loadExtensionDepends(){
    try{
      Class.forName("javax.media.j3d.J3DBuffer");
      dependenciesLoaded_.add("java3d");
      log.info("Java3D library found.");
    }catch(ClassNotFoundException e){
      log.warn("Java3D library not found.");
    }catch(NoClassDefFoundError err){
      log.warn("Java3D library not found.");
    }
      
  }

  /**
   * Load the the extension manifest file describing all the extensions and
   * their properties.
   */
  private void loadExtensionManifest() {
    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder();
      Document xmlDocument = documentBuilder.parse(DataViewer
          .getResourceAsStream(EXTENSIONS_CONFIG_FILENAME));
      NodeList extensionNodes = xmlDocument.getElementsByTagName("extension");
      for (int i = 0; i < extensionNodes.getLength(); i++) {
        Node extensionNode = extensionNodes.item(i);
        String dependencies[]=null;
        boolean dependsFound=true;
        String extensionID = null;
        String extensionName = null;
        List<String> mimeTypes = new ArrayList<String>();
        NodeList parameters = extensionNode.getChildNodes();
        for (int j = 0; j < parameters.getLength(); j++) {
          Node parameterNode = parameters.item(j);
          String parameterName = parameterNode.getNodeName();
          if (parameterName.equals("id")) {
            extensionID = parameterNode.getFirstChild().getNodeValue();
          } else if (parameterName.equals("name")) {
            extensionName = parameterNode.getFirstChild().getNodeValue();
          } else if (parameterName.equals("depends")) {
            String nodeList=parameterNode.getFirstChild().getNodeValue();
            dependencies=nodeList.split(" ,;");
          } else if (parameterName.equals("mimeTypes")) {
            NodeList mimeTypeNodes = parameterNode.getChildNodes();
            for (int k = 0; k < mimeTypeNodes.getLength(); k++) {
              Node mimeTypeNode = mimeTypeNodes.item(k);
              if (mimeTypeNode.getNodeName().equals("mimeType")) {
                mimeTypes.add(mimeTypeNode.getFirstChild().getNodeValue());
              }
            }
          }
        }
        
        if(dependencies!= null){
          for(String dependency:dependencies){
            //log.info("Checking dependency "+dependency);
            boolean found=false;
            for(String loadedDepend:dependenciesLoaded_){
              if(loadedDepend.compareToIgnoreCase(dependency)==0){
                found=true;
                break;
              }
            }
            if(!found){
              dependsFound=false;
              log.warn("Unable to register extension " + extensionName + " (" + extensionID
                  + "): dependency "+dependency+" not found.");
              break;
            }
          }
        }
        
        if(dependsFound){
          try{
            // try to locate the necessary class
            Class.forName(extensionID);
            Extension extension = new Extension(extensionID, extensionName,
                mimeTypes);
            extensions.add(extension);
            log.info("Registered extension " + extensionName + " (" + extensionID
                + ").");
          }catch(ClassNotFoundException e){
            log.warn("Unable to register extension " + extensionName + " (" + extensionID
                + "): class not found.");
          }catch(NoClassDefFoundError err){
            log.warn("Unable to register extension " + extensionName + " (" + extensionID
                + "): class not found.");
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to fully load the extension manifest.");
      e.printStackTrace();
    }
  }

  /**
   * View the list of channels with the specified extension. If the channel
   * supports multiple channels, all the channels will be viewed in the same
   * instance. Otherwise a new instance will be created for each channel.
   * 
   * If adding a channel to the same instance of an extension fails, a new
   * instance of the extension will be created and the channel will be added
   * this this.
   * 
   * @param channels   the list of channels to view
   * @param extension  the extension to view these channels with
   */
  public DataPanel createDataPanel(List<String> channels, Extension extension) {
    DataPanel dataPanel = null;
    try {
      dataPanel = createDataPanel(extension);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    
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
      log.warn("problem adding channels to panel");
    }
  
    return dataPanel;
  }
  
  
  /**
   * Creates the data panel referenced by the supplied extension ID. This ID is
   * by convention, the name of the class implementing this extension.
   * 
   * @param extension
   *          the extension to create
   * @throws Exception
   * @return the newly created data panel
   */
  public DataPanel createDataPanel(Extension extension) throws Exception {
    Class<?> dataPanelClass;
    try {
      dataPanelClass = Class.forName(extension.getID());
    } catch (ClassNotFoundException e) {
      throw new Exception("Unable to find extension " + extension.getName());
    }

    DataPanel dataPanel = (DataPanel) dataPanelClass.newInstance();

    //dataPanel.setPanelManager(this);
    //container.addDataPanel(dataPanel);
    //Set<DataPanel> set = dataPanels.get(container);
    //set.add(dataPanel);
    
    return dataPanel;
  }
  
  /**
   * View the channel specified by the given channel name. This will cause the
   * data panel manager to look for the default viewer for the mime type of the
   * viewer.
   * 
   * If the channel has no mime type, it is assumed to be numeric data unless
   * the channel ends with .jpg, in which case it is assumed to be image or
   * video data.
   * 
   * @param channelName
   *          the name of the channel to view
   * @throws Exception 
   * @throws ClassNotFoundException 
   */
  public DataPanel createDataPanelFromExtension(String extensionName) 
                                  throws Exception  
  {
    return createDataPanel(getExtension(extensionName));
  }
  
  /**
   * View the channel specified by the given channel name. This will cause the
   * data panel manager to look for the default viewer for the mime type of the
   * viewer.
   * 
   * If the channel has no mime type, it is assumed to be numeric data unless
   * the channel ends with .jpg, in which case it is assumed to be image or
   * video data.
   * 
   * @param channelName
   *          the name of the channel to view
   */
  public DataPanel createDataPanelFromChannel(String channelName) {
    return createDataPanel(channelName, getDefaultExtension(channelName));
  }
  
  /**
   * View the channel given by the channel name with the specified extension.
   * 
   * @param channelName  the name of the channel to view
   * @param extension    the extension to view the channel with
   */
  public DataPanel createDataPanel(String channelName, Extension extension) {
    DataPanel dataPanel=null;
    
    if (extension != null) {
      log.info("Creating data panel for channel " + channelName + " with extension " + extension.getName() + ".");
      try {
        dataPanel = createDataPanel(extension);
        if (!dataPanel.setChannel(channelName)) {
          log.error("Failed to add channel " + channelName + ".");
          //closeDataPanel(dataPanel);
          return null;
        }
      } catch (Exception e) {
        log.error("Failed to create data panel and add channel.");
        e.printStackTrace();
        return null;
      }     
    } else {
      log.warn("Failed to find data panel extension for channel " + channelName + ".");
    }    
    return dataPanel;
  }
  
  /**
   * Return the registered extension specified by the given class.
   * 
   * @param extensionClass
   *          the class of the desired extension
   * @return the extension, or null if the class wasn't found
   */
  public Extension getExtension(Class<?> extensionClass) {
    for (int i = 0; i < extensions.size(); i++) {
      Extension extension = extensions.get(i);
      if (extension.getID().equals(extensionClass.getName())) {
        return extension;
      }
    }

    return null;
  }

  /**
   * Return the registered extension specified by the given class name.
   * 
   * @param extensionID              the class name of the desired extension
   * @return                         the extension, or null if the class wasn't found
   * @throws ClassNotFoundException
   */
  public Extension getExtension(String extensionID) throws ClassNotFoundException {
    Class<?> extensionClass = Class.forName(extensionID);
    return getExtension(extensionClass);
  }
  
  /**
   * Return a list of registered extensions.
   * 
   * @return  an array list of extensions
   * @see     Extension
   */
  public List<Extension> getExtensions() {
    return extensions;
  }
  

  /**
   * Returns a list of extensions that support the channels. An extension must
   * support each channel to be included on this list.
   * 
   * @param channels  the channels to support
   * @return          a list of supported extensions
   */
  public List<Extension> getExtensions(List<String> channels) {
    if (channels == null || channels.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<Extension> extensions = getExtensions(channels.get(0));
    
    for (int i=1; i<channels.size(); i++) {
      String channel = channels.get(i);
      List<Extension> channelExtensions = getExtensions(channel);
      for (int j=extensions.size()-1; j>=0; j--) {
        Extension extension = extensions.get(j);
        if (!channelExtensions.contains(extension)) {
          extensions.remove(extension);
        }
      }
    }

    return extensions;
  }

  /**
   * Return the default extension for the channel or null if there is none.
   * 
   * @param channelName  the channel to use 
   * @return             the default extension or null if there is none
   */
  public Extension getDefaultExtension(String channelName) {
    Channel channel = RBNBController.getInstance().getChannel(channelName);
    String mime = null;
    if (channel != null) {
      mime = channel.getMetadata("mime");
    }

    return findExtension(mime);
  }

  /**
   * Return a list of extension that support the channel.
   * 
   * @param channelName  the channel to support
   * @return             a list of supported extensions
   */
  public List<Extension> getExtensions(String channelName) {
    Channel channel = RBNBController.getInstance().getChannel(channelName);
    
    String mime = null;
    if (channel != null) {
      mime = channel.getMetadata("mime");
    }
    
    List<Extension> usefulExtensions = new ArrayList<Extension>();
    for (int i=0; i<extensions.size(); i++) {
      Extension extension = extensions.get(i);
      List<String> mimeTypes = extension.getMimeTypes();
      for (int j=0; j<mimeTypes.size(); j++) {
        String mimeType = mimeTypes.get(j);
        if (mimeType.equals(mime)) {
          usefulExtensions.add(extension);
        }
      }
    }
    
    return usefulExtensions;
  }
  
  /**
   * Return an extension that is capable of viewing the
   * provided mime type.
   * 
   * @param mime  the mime type that the extension must support
   * @return      an extension to view this mime type, or null if none is found
   */
  private Extension findExtension(String mime) {
    for (int i=0; i<extensions.size(); i++) {
      Extension extension = extensions.get(i);
      List<String> mimeTypes = extension.getMimeTypes();
      for (int j=0; j<mimeTypes.size(); j++) {
        String mimeType = mimeTypes.get(j);
        if (mimeType.equals(mime)) {
          return extension;
        }
      }
    }
    
    return null;
  }

}
