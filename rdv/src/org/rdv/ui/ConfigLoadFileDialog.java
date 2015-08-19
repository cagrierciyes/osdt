
package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.application.ResourceMap;
import org.rdv.DataViewer;
import org.rdv.RDV;
import org.rdv.config.ConfigFile;
import org.rdv.util.RDVConfigurationFileFilter;

/**
 * A class which represents the loading dialog for configuration files
 * 
 * @author Andrew Moraczewski
 *
 */
public class ConfigLoadFileDialog extends JDialog implements ActionListener {

  /** serialized version identifier */
  private static final long serialVersionUID = 6298045276478657400L;

  /** panel which holds the tabbed pane */
  JPanel panel;
  
  /** tabbed pane */
  //JTabbedPane tabbedPane;
  
  /** file loading file chooser */
  JFileChooser fileChooser;
  
  /** panel which holds the file chooser */
  JPanel loadFilePanel;
  
  /** panel which holds the rbnb chooser */
  //JPanel loadRBNBPanel;
  
  JScrollPane rbnbScrollPane;
  
  JList fileList;
  
  
  public ConfigLoadFileDialog(JFrame owner) {
    super(owner);
    
    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    //tabbedPane = new JTabbedPane();
    
    // setup the normal loading from a file tab
    loadFilePanel = new JPanel();
    fileChooser = new JFileChooser();
    fileChooser.addActionListener(this);
    fileChooser.setFileFilter(new RDVConfigurationFileFilter());
    loadFilePanel.add(fileChooser);
    //tabbedPane.addTab(" File ", null, loadFilePanel, "Load Configuration from a File");
    
    // setup the new load from RBNB tab
//    loadRBNBPanel = new JPanel();
//    loadRBNBPanel.setLayout(new BorderLayout());
//    
//    JPanel upperPanel = new JPanel();
//    upperPanel.setLayout(new BorderLayout());
//    upperPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
//    
//    fileList = new JList();
//    fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//    fileList.setLayoutOrientation(JList.VERTICAL);
//    rbnbScrollPane = new JScrollPane(fileList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//    rbnbScrollPane.setPreferredSize(new Dimension(300, 300));
//    
//    upperPanel.add(rbnbScrollPane);
//    loadRBNBPanel.add(upperPanel, BorderLayout.CENTER);
//    
//    JPanel lowerPanel = new JPanel();
//    GroupLayout lowerPanelLayout = new GroupLayout(lowerPanel);
//    lowerPanel.setLayout(lowerPanelLayout);
//    lowerPanelLayout.setAutoCreateGaps(true);
//    lowerPanelLayout.setAutoCreateContainerGaps(true);
//    
//    JButton loadButton = new JButton("Open");
//    loadButton.addActionListener(new AbstractAction() {
//
//      /** serialized version identifier */
//      private static final long serialVersionUID = -3658208960047309506L;
//
//      public void actionPerformed(ActionEvent e) {
//        loadConfig();
//      }
//    });
//    
//    JButton cancelButton = new JButton("Cancel");
//    cancelButton.addActionListener(new AbstractAction() {
//
//      /** serialized version identifier */
//      private static final long serialVersionUID = -3658208960047309506L;
//
//      public void actionPerformed(ActionEvent e) {
//        dispose();
//      } 
//    });
//    
//    JTextField textField = new JTextField();
//    textField.setPreferredSize(new Dimension(300, 30));
//    
//    lowerPanelLayout.setHorizontalGroup(
//        lowerPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//        .addComponent(textField)
//        .addGroup(lowerPanelLayout.createSequentialGroup()
//            .addComponent(cancelButton)
//            .addComponent(loadButton)
//        )
//    );
//    
//    lowerPanelLayout.setVerticalGroup(
//        lowerPanelLayout.createSequentialGroup()
//        .addComponent(textField)
//        .addGroup(lowerPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//            .addComponent(loadButton)
//            .addComponent(cancelButton)
//        )
//    );
//    
//    loadRBNBPanel.add(lowerPanel, BorderLayout.SOUTH);
//    tabbedPane.addTab(" RBNB ", null, loadRBNBPanel, "Load Configuration from the RBNB server");
//    
//    panel.add(tabbedPane, BorderLayout.CENTER);
//    
    panel.add(loadFilePanel,BorderLayout.CENTER);
    add(panel);
    
    // inject resources from the properties for this component
    ResourceMap resourceMap = RDV.getInstance().getContext().getResourceMap(getClass());
    resourceMap.injectComponents(this);
    
    pack();
    
    setResizable(false);
    setLocationByPlatform(true);
    setVisible(true);
  }
  
  
  public void actionPerformed(ActionEvent ae) {
    if(ae.getActionCommand() == JFileChooser.CANCEL_SELECTION) {
      setVisible(false);
      dispose();
    } else if (ae.getActionCommand() == JFileChooser.APPROVE_SELECTION) {
      
      File configFile = fileChooser.getSelectedFile();
      if (configFile != null) {
        try {
          URL configURL = configFile.toURI().toURL();
          ConfigFile c = new ConfigFile();
          c.loadConfiguration(configURL);
        } catch (MalformedURLException e) {
          DataViewer.alertError("\"" + configFile
              + "\" is not a valid configuration file URL.");
        }
      }
      setVisible(false);
      dispose();
    }
  }
}
