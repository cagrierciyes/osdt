
package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jdesktop.application.ResourceMap;
import org.rdv.RDV;
import org.rdv.config.ConfigFile;
import org.rdv.util.RDVConfigurationFileFilter;

/**
 * A class which represents the saving dialog for configuration files
 * 
 * @author Andrew Moraczewski
 *
 */
public class ConfigSaveFileDialog extends JDialog implements ActionListener {

  /** serialized version identifier */
  private static final long serialVersionUID = 859415261500962924L;
  
  /** panel which holds the tabbed pane */
  JPanel panel;
  
  /** tabbed pane with tabs for saving to a file or the RBNB server */
  //JTabbedPane tabbedPane;
  
  /** file chooser for saving files*/
  JFileChooser fileChooser;
  
  /** panel for saving to a file */
  JPanel saveFilePanel;
  
  /** panel for saving to the RBNB server */
  //JPanel saveRBNBPanel;
  
  //JScrollPane rbnbScrollPane;
  
  //JList fileList;
  
  public ConfigSaveFileDialog(JFrame owner) {
    super(owner);
    
    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    //tabbedPane = new JTabbedPane();
    
    // setup the normal saving to a file tab
    saveFilePanel = new JPanel();
    fileChooser = new JFileChooser();
    fileChooser.addActionListener(this);
    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    fileChooser.setFileFilter(new RDVConfigurationFileFilter());
    saveFilePanel.add(fileChooser);
    //tabbedPane.addTab(" File ", null, saveFilePanel, "Save Configuration to a File");
    
    // setup the new save to RBNB tab
//    saveRBNBPanel = new JPanel();
//    saveRBNBPanel.setLayout(new BorderLayout());
//    
//    JPanel upperPanel = new JPanel();
//    upperPanel.setLayout(new BorderLayout());
//    upperPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
//    
//    fileList = new JList();
//    fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//    fileList.setLayoutOrientation(JList.VERTICAL);
//
//    rbnbScrollPane = new JScrollPane(fileList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//    rbnbScrollPane.setPreferredSize(new Dimension(300, 300));
//    
//    upperPanel.add(rbnbScrollPane);
//    saveRBNBPanel.add(upperPanel, BorderLayout.CENTER);
//    
//    JPanel lowerPanel = new JPanel();
//    GroupLayout lowerPanelLayout = new GroupLayout(lowerPanel);
//    lowerPanel.setLayout(lowerPanelLayout);
//    lowerPanelLayout.setAutoCreateGaps(true);
//    lowerPanelLayout.setAutoCreateContainerGaps(true);
//    
//    JButton saveButton = new JButton("Save");
//    saveButton.addActionListener(new AbstractAction() {
//
//      /** serialized version identifier */
//      private static final long serialVersionUID = -3658208960047309506L;
//
//      public void actionPerformed(ActionEvent e) {
//        saveConfig();
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
//            .addComponent(saveButton)
//        )
//    );
//    
//    lowerPanelLayout.setVerticalGroup(
//        lowerPanelLayout.createSequentialGroup()
//        .addComponent(textField)
//        .addGroup(lowerPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//            .addComponent(saveButton)
//            .addComponent(cancelButton)
//        )
//    );
//    
//    saveRBNBPanel.add(lowerPanel, BorderLayout.SOUTH);
//    tabbedPane.addTab(" RBNB ", null, saveRBNBPanel, "Save Configuration from the RBNB server");
    
    panel.add(saveFilePanel, BorderLayout.CENTER);
    //panel.add(tabbedPane, BorderLayout.CENTER);
    
    add(panel);
    
    // inject resources from the properties for this component
    ResourceMap resourceMap = RDV.getInstance().getContext().getResourceMap(getClass());
    resourceMap.injectComponents(this);
    
    pack();

    setResizable(false);
    setLocationByPlatform(true);
    setVisible(true);
  }
  
  /*private void saveConfig() {
    System.out.println("Attempting to save file");
    ConfigRBNB c = new ConfigRBNB();
    c.saveConfiguration(ConfigurationManager.getConfig());
  }*/

  public void actionPerformed(ActionEvent ae) {
    
    if(ae.getActionCommand() == JFileChooser.CANCEL_SELECTION) {
      setVisible(false);
      dispose();
    } else if(ae.getActionCommand() == JFileChooser.APPROVE_SELECTION) {
      
      File file = fileChooser.getSelectedFile();
      if (file != null) {
        if (file.getName().indexOf(".") == -1) {
          file = new File(file.getAbsolutePath() + ".rdv");
        }

        // prompt for overwrite if file already exists
        if (file.exists()) {
          int overwriteReturn = JOptionPane.showConfirmDialog(null,
              file.getName() + " already exists. Do you want to overwrite it?",
              "Overwrite file?",
              JOptionPane.YES_NO_OPTION);
          if (overwriteReturn == JOptionPane.NO_OPTION) {
            return;
          }
        }
        ConfigFile c = new ConfigFile();
        c.saveConfiguration(file.getAbsolutePath());
      }
      
      setVisible(false);
      dispose();
    }
  }
}
