
package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.jdesktop.application.ResourceMap;
import org.rdv.ConfigurationManager;
import org.rdv.RDV;
import org.rdv.config.ConfigRBNB;

/**
 * A class which represents the loading dialog for configuration files
 * 
 * @author Andrew Moraczewski
 *
 */
public class ConfigLoadRBNBDialog extends JDialog {

  /** serialized version identifier */
  private static final long serialVersionUID = -216251208791107362L;
  
  /** panel which holds the tabbed pane */
  JPanel panel;
  
  /** tabbed pane */
  //JTabbedPane tabbedPane;
  
  /** file loading file chooser */
  //JFileChooser fileChooser;
  
  /** panel which holds the file chooser */
  //JPanel loadFilePanel;
  
  /** panel which holds the rbnb chooser */
  JPanel loadRBNBPanel;
  
  JScrollPane rbnbScrollPane;
  
  JList fileList;
  
  //JTextField configName_;
  
  ConfigRBNB configRBNB_ = new ConfigRBNB();
  
  public ConfigLoadRBNBDialog(JFrame owner, String title) {
    super(owner,title);
    init(owner);
  }
  public ConfigLoadRBNBDialog(JFrame owner) {
    super(owner);
    init(owner);
  }
  
  private void init(JFrame owner){
    configRBNB_.load();
    Iterator<String> nameIt=configRBNB_.getNames().iterator();
    
    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    //tabbedPane = new JTabbedPane();
    
    // setup the normal loading from a file tab
    //loadFilePanel = new JPanel();
//    fileChooser = new JFileChooser();
//    fileChooser.addActionListener(this);
//    fileChooser.setFileFilter(new RDVConfigurationFileFilter());
//    loadFilePanel.add(fileChooser);
//    tabbedPane.addTab(" File ", null, loadFilePanel, "Load Configuration from a File");
    
    // setup the new load from RBNB tab
    loadRBNBPanel = new JPanel();
    loadRBNBPanel.setLayout(new BorderLayout());
    
    JPanel upperPanel = new JPanel();
    upperPanel.setLayout(new BorderLayout());
    upperPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    
    DefaultListModel listModel = new DefaultListModel();
    while(nameIt.hasNext()){
      listModel.addElement(nameIt.next());
    }
    fileList = new JList(listModel);
    fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fileList.setLayoutOrientation(JList.VERTICAL);
    fileList.addMouseListener(new ListMouseAdapter());
    rbnbScrollPane = new JScrollPane(fileList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    rbnbScrollPane.setPreferredSize(new Dimension(300, 200));
    
    upperPanel.add(rbnbScrollPane);
    loadRBNBPanel.add(upperPanel, BorderLayout.CENTER);
    
    JPanel lowerPanel = new JPanel();
    GroupLayout lowerPanelLayout = new GroupLayout(lowerPanel);
    lowerPanel.setLayout(lowerPanelLayout);
    lowerPanelLayout.setAutoCreateGaps(true);
    lowerPanelLayout.setAutoCreateContainerGaps(true);
   
    JButton loadButton = new JButton("Load");
    loadButton.addActionListener(new AbstractAction() {

      /** serialized version identifier */
      private static final long serialVersionUID = 4184522488722073898L;

      public void actionPerformed(ActionEvent e) {
        loadConfig();
      }
    });
    
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new AbstractAction() {
      
      /** serialized version identifier */
      private static final long serialVersionUID = -8202252545963083385L;      

      public void actionPerformed(ActionEvent e) {
        dispose();
      } 
    });
    
    //configName_ = new JTextField();
    //configName_.setPreferredSize(new Dimension(300, 20));
    
    lowerPanelLayout.setHorizontalGroup(
        //lowerPanelLayout.createSequentialGroup()
        lowerPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
        //.addComponent(configName_)
        //.addGroup(lowerPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
        .addGroup(lowerPanelLayout.createSequentialGroup()
            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cancelButton)
            .addComponent(loadButton)
        )
    );
    
    lowerPanelLayout.setVerticalGroup(
        lowerPanelLayout.createSequentialGroup()
        //.addComponent(configName_)
        .addGroup(lowerPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(loadButton)
            .addComponent(cancelButton)
        )
    );
    
    loadRBNBPanel.add(lowerPanel, BorderLayout.SOUTH);
    //tabbedPane.addTab(" RBNB ", null, loadRBNBPanel, "Load Configuration from the RBNB server");
    
    //panel.add(tabbedPane, BorderLayout.CENTER);
    panel.add(loadRBNBPanel, BorderLayout.CENTER);
    add(panel);
    
    // inject resources from the properties for this component
    ResourceMap resourceMap = RDV.getInstance().getContext().getResourceMap(getClass());
    resourceMap.injectComponents(this);
    
    pack();
    
    setResizable(false);
    setLocationByPlatform(true);
    setVisible(true);
  }
  
  private void loadConfig() {
    System.out.println("Attempting to load File");
    //int index = fileList.locationToIndex(e.getPoint());
    int index = fileList.getSelectedIndex();
    if (index < 0) return;
    
    //if (e.getClickCount() == 2) {
      //setButton.doClick(); // emulate button click  
    fileList.ensureIndexIsVisible(index);
    
    ListModel dlm = fileList.getModel();
    String configName = (String)dlm.getElementAt(index);
    String config=configRBNB_.getConfiguration(configName);
    if (config!=null)
      ConfigurationManager.applyConfig(config);
    // load selected config
    //c.getConfiguration("Test");
  }
  
private class ListMouseAdapter extends MouseAdapter {
    
    public void mouseClicked(MouseEvent e) {
      
//      int index = fileList.locationToIndex(e.getPoint());
//      if (index < 0) return;
//      
//      //if (e.getClickCount() == 2) {
//        //setButton.doClick(); // emulate button click  
//        fileList.ensureIndexIsVisible(index);
//        
//        ListModel dlm = fileList.getModel();
//        String configName = (String)dlm.getElementAt(index);
//        configName_.setText(configName);
//      //}
    }
  }
//  public void actionPerformed(ActionEvent ae) {
//    if(ae.getActionCommand() == JFileChooser.CANCEL_SELECTION) {
//      setVisible(false);
//      dispose();
//    } else if (ae.getActionCommand() == JFileChooser.APPROVE_SELECTION) {
//      
//      /*File configFile = fileChooser.getSelectedFile();
//      if (configFile != null) {
//        try {
//          URL configURL = configFile.toURI().toURL();
//          ConfigFile c = new ConfigFile();
//          c.loadConfiguration(configURL.getFile());
//        } catch (MalformedURLException e) {
//          DataViewer.alertError("\"" + configFile
//              + "\" is not a valid configuration file URL.");
//        }
//      }
//      setVisible(false);
//      dispose();
//      */
//    }
//  }
}
