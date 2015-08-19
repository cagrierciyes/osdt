
package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.ResourceMap;
import org.rdv.ConfigurationManager;
import org.rdv.RDV;
import org.rdv.config.ConfigRBNB;

/**
 * A class which represents the saving dialog for configuration files
 * 
 * @author Andrew Moraczewski
 *
 */
public class ConfigSaveRBNBDialog extends JDialog {

  /** serialized version identifier */
  private static final long serialVersionUID = -7017874411811983013L;

  private Log log = LogFactory.getLog(ConfigSaveRBNBDialog.class.getName());
  
  /** panel which holds the tabbed pane */
  JPanel panel;
  
  /** tabbed pane with tabs for saving to a file or the RBNB server */
  //JTabbedPane tabbedPane;
  
  /** file chooser for saving files*/
  //JFileChooser fileChooser;
  
  /** panel for saving to a file */
  //JPanel saveFilePanel;
  
  /** panel for saving to the RBNB server */
  JPanel saveRBNBPanel;
  
  JScrollPane rbnbScrollPane;
  
  JList fileList;
  
  JTextField configName_;
  
  ConfigRBNB configRBNB_ = new ConfigRBNB();
  
  public ConfigSaveRBNBDialog(JFrame owner,String title) {
    super(owner,title);
    init(owner);
  }
  
  public ConfigSaveRBNBDialog(JFrame owner) {
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
    
    // setup the normal saving to a file tab
//    saveFilePanel = new JPanel();
//    fileChooser = new JFileChooser();
//    fileChooser.addActionListener(this);
//    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
//    fileChooser.setFileFilter(new RDVConfigurationFileFilter());
//    saveFilePanel.add(fileChooser);
//    tabbedPane.addTab(" File ", null, saveFilePanel, "Save Configuration to a File");
    
    // setup the new save to RBNB tab
    saveRBNBPanel = new JPanel();
    saveRBNBPanel.setLayout(new BorderLayout());
    
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
    saveRBNBPanel.add(upperPanel, BorderLayout.CENTER);
    
    JPanel lowerPanel = new JPanel();
    GroupLayout lowerPanelLayout = new GroupLayout(lowerPanel);
    lowerPanel.setLayout(lowerPanelLayout);
    lowerPanelLayout.setAutoCreateGaps(true);
    lowerPanelLayout.setAutoCreateContainerGaps(true);
    
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(new AbstractAction() {

      /** serialized version identifier */
      private static final long serialVersionUID = -3658208960047309506L;

      public void actionPerformed(ActionEvent e) {
        saveConfig();
      }
    });
    
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new AbstractAction() {

      /** serialized version identifier */
      private static final long serialVersionUID = -3658208960047309506L;

      public void actionPerformed(ActionEvent e) {
        dispose();
      } 
    });
    
    configName_ = new JTextField();
    configName_.setPreferredSize(new Dimension(300, 20));
    
    lowerPanelLayout.setHorizontalGroup(
        lowerPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
        .addComponent(configName_)
        .addGroup(lowerPanelLayout.createSequentialGroup()
            .addComponent(cancelButton)
            .addComponent(saveButton)
        )
    );
    
    lowerPanelLayout.setVerticalGroup(
        lowerPanelLayout.createSequentialGroup()
        .addComponent(configName_)
        .addGroup(lowerPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
            .addComponent(saveButton)
            .addComponent(cancelButton)
        )
    );
    
    saveRBNBPanel.add(lowerPanel, BorderLayout.SOUTH);
    //tabbedPane.addTab(" RBNB ", null, saveRBNBPanel, "Save Configuration from the RBNB server");
    
    //panel.add(tabbedPane, BorderLayout.CENTER);
    panel.add(saveRBNBPanel, BorderLayout.CENTER);
    
    add(panel);
    
    // inject resources from the properties for this component
    ResourceMap resourceMap = RDV.getInstance().getContext().getResourceMap(getClass());
    resourceMap.injectComponents(this);
    
    pack();

    setResizable(false);
    setLocationByPlatform(true);
    setVisible(true);
  }
  
  private void saveConfig() {
    System.out.println("Attempting to save file");
    //ConfigRBNB c = new ConfigRBNB();
    //c.saveConfiguration("Test");
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(new BufferedWriter(sw));
    try{
      ConfigurationManager.printConfigXml(out);
    }catch(Exception e){
      // also open dialog
      StringWriter s = new StringWriter();
      PrintWriter pw = new PrintWriter(new BufferedWriter(s));
      e.printStackTrace(pw);
      pw.flush();
      log.error(s.toString());
    }
    
    configRBNB_.addConfiguration(configName_.getText(), sw.toString());
    configRBNB_.save();
    //c.test();
  }

  private class ListMouseAdapter extends MouseAdapter {
    
    public void mouseClicked(MouseEvent e) {
      
      int index = fileList.locationToIndex(e.getPoint());
      if (index < 0) return;
      
      //if (e.getClickCount() == 2) {
        //setButton.doClick(); // emulate button click  
        fileList.ensureIndexIsVisible(index);
        
        ListModel dlm = fileList.getModel();
        String configName = (String)dlm.getElementAt(index);
        configName_.setText(configName);
      //}
    }
  }
  /*public void actionPerformed(ActionEvent ae) {
    
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
  }*/
}
