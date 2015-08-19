/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
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
 * $Date: 2008-12-02 14:43:15 -0500 (Tue, 02 Dec 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.ui.channel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.matheclipse.parser.client.SyntaxError;
import org.rdv.RDV;
import org.rdv.browser.Browser;
import org.rdv.data.Channel;
import org.rdv.data.LocalChannel;
import org.rdv.data.LocalChannelManager;
import org.rdv.dialog.ErrorDialog;
import org.rdv.formatter.FormatterService;
import org.rdv.property.PropertyRepository;
import org.rdv.rbnb.RBNBController;

/**
 * A dialog to collect the information necessary to create or edit a local channel.
 * 
 * @author Jason P. Hanley
 * @author Michael Boldischar - Updated for editing
 */
public class LocalChannelDialog extends JDialog {

  private static final Log LOGGER = LogFactory.getLog(LocalChannelDialog.class);
  /** serialization version identifier */
  private static final long serialVersionUID = -7945306695595024914L;

  private static final PropertyRepository PROPERTY_REPO
    = PropertyRepository.getInstance();

  /** the name text field */
  private final JTextField nameTextField = new JTextField();

  /** the unit text field */
  private final JTextField unitTextField = new JTextField();

  /** the remove variable button */
  private final JButton removeVariableButton = new JButton();

  /** the formula text area */
  private final JTextArea formulaTextArea = new JTextArea(5, 50);

  /** the add channel button */
  private final JButton channelActionButton = new JButton();

  private LocalChannel channel;

  private final JButton helpButton = new JButton();

  private final VariablesTable variablesTable
    = new VariablesTable(removeVariableButton);

  private static final String TITLE_KEY
    = "addLocalChannelDialog.title";
  private static final String NAME_KEY = "nameLabel.text";
  private static final String UNIT_KEY= "unitLabel.text";
  private static final String VARIABLES_KEY= "variablesLabel.text";
  private static final String ADD_VARIABLE_BUTTON_KEY = "addVariableButton.text";
  private static final String FORMULA_KEY = "formulaLabel.text";
  private static final String ADD_CHANNEL_BUTTON_KEY = "addChannelButton.text";
  private static final String SAVE_CHANNEL_BUTTON_KEY ="saveChannelButton.text";
  private static final String CANCEL_BUTTON_KEY = "cancelButton.text";
  private static final String FORMULA_ERROR_TITLE_KEY = "formulaErrorTitle";
  private static final String FORMULA_ERROR_MESSAGE_KEY = "formulaErrorMessage";
  private static final String ARITHMETIC_ERROR_MESSAGE_KEY = "arithmeticErrorMessage";
  private static final String SYNTAX_ERROR_MESSAGE_KEY = "syntaxErrorMessage";
  private static final String REMOVE_VARIABLE_BUTTON_KEY = "removeVariableButton.text";

  private static final String HELP_BUTTON_KEY = "help.button.text";
  private static final String ADD_CHANNEL_DOC_KEY = "channel.add.documentation";

  private final Formatter formatter = new Formatter();

  /**
   * Creates a add local channel dialog.
   * 
   * @param owner  the dialog's owner frame
   */
  public LocalChannelDialog(JFrame owner) {
    this(null, owner);
  }

  public LocalChannelDialog(LocalChannel channel, JFrame owner) {
    super(owner);
    this.channel = channel;
    setName("addLocalChannelDialog");
    setTitle(PROPERTY_REPO.getValue(TITLE_KEY));
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    initComponents();
    populateDialog();
  }

  /**
   * Populates dialog with existing channel value
   */
  private void populateDialog() {
    if (channel != null) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Populating dialog with existing channel: " + channel);
      }
      nameTextField.setText(channel.getName());
      unitTextField.setText(channel.getUnit());
      Map<String, String> variables = channel.getVariables();
      Iterator<Map.Entry<String, String>> iterator
        = variables.entrySet().iterator();
      while (iterator.hasNext()) {
          Map.Entry<String, String> pair
            = (Map.Entry<String, String>) iterator.next();
          variablesTable.addVariable(pair.getValue() ,pair.getKey());
      }
      formulaTextArea.setText(channel.getFormula());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Finished populating dialog.");
      }
    }
  }

  /**
   * Creates default constraints for a UI component.
   * @return
   *    the default constraints
   */
  private GridBagConstraints createDefaultGridBagConstraints() {
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 0;
    c.weighty = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.ipadx = 0;
    c.ipady = 0;
    c.weightx = 0;
    c.weighty = 0;
    return c;
  }

  /**
   * Initialize the UI components.
   */
  private void initComponents() {
    RDV rdv = RDV.getInstance(RDV.class);

    JPanel container = new JPanel();
    setContentPane(container);

    container.setLayout(new GridBagLayout());
    GridBagConstraints c = createDefaultGridBagConstraints();

    JLabel nameLabel = new JLabel();
    nameLabel.setName("nameLabel");
    nameLabel.setText(PROPERTY_REPO.getValue(NAME_KEY));

    c = createDefaultGridBagConstraints();
    c.fill = GridBagConstraints.NONE;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(10,10,10,5);
    container.add(nameLabel, c);

    c = createDefaultGridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(10,0,10,10);
    container.add(nameTextField, c);
    if (isChannelUpdateOperation()) {
      nameTextField.setEnabled(false);
    }
    JLabel unitLabel = new JLabel();
    unitLabel.setName("unitLabel");
    unitLabel.setText(PROPERTY_REPO.getValue(UNIT_KEY));
    c = createDefaultGridBagConstraints();
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,5);
    container.add(unitLabel, c);

    c = createDefaultGridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,0,10,10);
    container.add(unitTextField, c);

    JLabel variablesLabel = new JLabel();
    variablesLabel.setName("variablesLabel");
    variablesLabel.setText(PROPERTY_REPO.getValue(VARIABLES_KEY));
    c = createDefaultGridBagConstraints();
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth =1;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,10);
    container.add(variablesLabel, c);

    JPanel variablesPanel = buildVariablesPanel();

    c = createDefaultGridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,10);
    c.weightx = 1;
    c.weighty = 1;
    container.add(variablesPanel, c);
    
    JLabel formulaLabel = new JLabel();
    formulaLabel.setName("formulaLabel");
    formulaLabel.setText(PROPERTY_REPO.getValue(FORMULA_KEY));
    c = createDefaultGridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 0;
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,10);
    container.add(formulaLabel, c);

    JScrollPane formulaScrollPane = new JScrollPane(formulaTextArea);
    c = createDefaultGridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1;
    c.weighty = 1;
    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.insets = new java.awt.Insets(0,10,10,10);
    container.add(formulaScrollPane, c);

    JPanel footerPanel = buildFooterPanel();

    c = createDefaultGridBagConstraints();
    c.fill = GridBagConstraints.NONE;
    c.weightx = 0.5;
    c.weighty = 0;
    c.gridx = 0;
    c.gridy = 7;
    c.gridwidth = GridBagConstraints.REMAINDER;;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new java.awt.Insets(0,0,10,5);
    container.add(footerPanel, c);

    // bind keystrokes
    InputMap inputMap = container.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "addChannel");
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");

    // initially disable buttons
    removeVariableButton.setEnabled(false);

    pack();

    setLocationByPlatform(true);
    setVisible(true);
  }

  /**
   * Checks to see if the information in this window is valid.  If it isn't
   * a dialog is displayed that explains what is invalid.
   * @param
   *    operationErrorString
   *      an error message to display if the data is invalid
   * @return
   *    true if the information in this window is valid, otherwise false
   */
  private boolean isChannelValid(String operationErrorString) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Checking to see if it is valid to add the current channel.");
    }
    StringBuffer errorBuffer = new StringBuffer();
    errorBuffer.append("<html><body>");
    errorBuffer.append(operationErrorString);
    errorBuffer.append("<br/><ul>");
    /*
     * Making each of these checks run separately so we have a list of problems,
     * instead of just one.
     */
    boolean valid = isChannelNameValid(errorBuffer);
    if (isChannelAddOperation()) {
      valid = isChannelNameAvailable(errorBuffer) && valid;
    }
    valid = isUnitValid(errorBuffer) && valid;
    valid = variablesTable.variablesValid(errorBuffer) && valid;
    valid = isFormulaValid(errorBuffer) && valid;
    errorBuffer.append("</ul></body></html>");
    if (valid) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Channel is valid.");
      }
    } else {
      LOGGER.error(errorBuffer.toString());
      ErrorDialog.show(this, errorBuffer.toString(),
          PROPERTY_REPO.getValue("invalidDataError"));
    }
    return valid;
  }

  /**
   * Verifies the data and tries to add the data as a new channel
   */
  private void addChannel() {
    if (isChannelValid(PROPERTY_REPO.getValue("addChannelInvalidData.text"))) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Channel information is valid.");
      }
      String name = nameTextField.getText();

      // get the unit
      String unit = unitTextField.getText();
      try {
        variablesTable.createChannel(name, unit, formulaTextArea.getText());
      } catch (Exception e) {
        handleLocalChannelException(e);
        LOGGER.error("There was an error sending the channel data to the server.", e);
      }
      dispose();
    } else {
      LOGGER.error("Channel information is not valid, unable to process request.");
    }
  }

  /**
   * Verifies the data and tries to update the channel data.
   */
  private void updateChannel() {
    if (channel != null && isChannelValid(PROPERTY_REPO.getValue("addChannelInvalidData.text"))) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Channel information is valid.");
      }
      channel.setFormula(formulaTextArea.getText(), variablesTable.getMap());
      LocalChannelManager.getInstance().setChannel(channel);
      dispose();
    } else {
      LOGGER.error("Channel information is not valid, unable to process request.");
    }
  }

  private JPanel buildFooterPanel() {
    JPanel footerPanel = new JPanel();

    if (isChannelAddOperation()) {
      channelActionButton.setName("addChannelButton");
      channelActionButton.setText(PROPERTY_REPO.getValue(ADD_CHANNEL_BUTTON_KEY));
    } else { // channel exists
      channelActionButton.setName("saveChannelButton");
      channelActionButton.setText(PROPERTY_REPO.getValue(SAVE_CHANNEL_BUTTON_KEY));
    }
    channelActionButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (isChannelAddOperation()) {
          addChannel();
        } else {
          updateChannel();
        }
      }
    });
    footerPanel.add(channelActionButton);

    JButton cancelButton = new JButton();
    cancelButton.setName("cancelButton");
    cancelButton.setText(PROPERTY_REPO.getValue(CANCEL_BUTTON_KEY));
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    });
    footerPanel.add(cancelButton);
    helpButton.setText(PROPERTY_REPO.getValue(HELP_BUTTON_KEY));
    helpButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          Browser.open(getOwner(),
              new URI(PROPERTY_REPO.getValue(ADD_CHANNEL_DOC_KEY)));
        } catch (URISyntaxException e1) {
          LOGGER.error("", e1);
          e1.printStackTrace();
        }
      }
    });
    footerPanel.add(helpButton);
    return footerPanel;
  }

  private JPanel buildVariablesPanel() {
    JPanel variablesPanel = new JPanel();
    variablesPanel.setLayout(new BorderLayout());
    
    JScrollPane variablesScrollPane = new JScrollPane(variablesTable.getComponent());
    variablesPanel.add(variablesScrollPane, BorderLayout.CENTER);

    Box variablesButtonsPanel = Box.createVerticalBox();
    variablesButtonsPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));

    JButton addVariableButton = new JButton();
    addVariableButton.setName("addVariableButton");
    addVariableButton.setText(PROPERTY_REPO.getValue(ADD_VARIABLE_BUTTON_KEY));
    addVariableButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        addVariable();
      }
    });
    variablesButtonsPanel.add(addVariableButton);

    variablesButtonsPanel.add(Box.createVerticalStrut(5));

    removeVariableButton.setName("removeVariableButton");
    removeVariableButton.setText(PROPERTY_REPO.getValue(REMOVE_VARIABLE_BUTTON_KEY));
    removeVariableButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        removeVariable();
      }
    });
    variablesButtonsPanel.add(removeVariableButton);

    variablesPanel.add(variablesButtonsPanel, BorderLayout.EAST);
    return variablesPanel;
  }

  /**
   * Check to see if the channel name is valid
   * @param
   *    errorBuffer
   *      the buffer to place error messages in when the name is invalid
   * @return
   *    true if the channel name is valid, otherwise false
   */
  private boolean isChannelNameValid(StringBuffer errorBuffer) {
    String name = nameTextField.getText();
    boolean result =  name.matches(
        "^[[a-zA-Z0-9\\.\\(\\)\\- ]+/]*[a-zA-Z0-9\\.\\(\\)\\- ]+$");
    if (!result) {
      String message = FormatterService.format(
          PROPERTY_REPO.getValue("invalidChannelName"), name);
      errorBuffer.append(message);
      LOGGER.error(message);
    }
    return result;
  }

  /**
   * Check to see if the channel name is available
   * @param
   *    errorBuffer
   *      the buffer to put error messages into when the data is invalid
   * @return
   *    true if the channel name is available, or we are editing an existing
   *    channel, otherwise false
   */
  private boolean isChannelNameAvailable(StringBuffer errorBuffer) {
    boolean nameFree = false;
    if (channel == null) {
      String name = nameTextField.getText();
      // check for existing channel
      Channel channel = RBNBController.getInstance().getChannel(name);
      nameFree = channel == null;
      if (!nameFree) {
        String errorMessage = FormatterService.format(
            PROPERTY_REPO.getValue("channelNameNotAvailable"), name);
        LOGGER.error(errorMessage);
        errorBuffer.append(errorBuffer);
      }
    }
    return nameFree;
  }

  /**
   * Check to see if the unit is valid
   * @param
   *    errorBuffer
   *      the error buffer that contains the list of validation errors.
   * @return
   *    true if the unit is valid, otherwise false
   */
  private boolean isUnitValid(StringBuffer errorBuffer) {
    String unit = unitTextField.getText();
    // check for invalid unit
    boolean unitValid = unit != null && !unit.isEmpty() && !unit.contains("=");
    if (!unitValid) {
      String message = PROPERTY_REPO.getValue("invalidUnitsError") + unit;
      errorBuffer.append(message);
      LOGGER.error(message);
    }
    return unitValid;
  }

  /**
   * Check to see if the formula is valid.
   * @return
   *    true if the formula is valid, otherwise false
   */
  private boolean isFormulaValid(StringBuffer stringBuffer) {
    String formula = formulaTextArea.getText();
    boolean formulaHasAllVariables
      = variablesTable.formulaContainsAllVariables(formula);
    boolean formulaValid = formula != null && !formula.isEmpty()
        && formulaHasAllVariables;
    if (!formulaValid) {
      String message = PROPERTY_REPO.getValue("formulaInvalid");
      LOGGER.error(message);
      stringBuffer.append(message);
    }
    return formulaValid;
  }

  /**
   * Adds a row the variables table and pops up the channel selector combo box.
   */
  public void addVariable() {
    variablesTable.addEmptyRow();
  }

  /**
   * Removes the select rows from the variables table.
   */
  public void removeVariable() {
    variablesTable.removeVariableRow();
  }

  /**
   * Handles an exception thrown when creating a local channel. An error dialog
   * will display with the nature of the error.
   * 
   * @param e  the exception
   */
  private void handleLocalChannelException(Exception e) {
    String errorTitle = PROPERTY_REPO.getValue(FORMULA_ERROR_TITLE_KEY);

    String errorMessage = "<html><body><font size=+1><b>" +
            PROPERTY_REPO.getValue(FORMULA_ERROR_MESSAGE_KEY) +
                          "</b></font><br><br>";
    
    if (e instanceof ArithmeticException) {
      ArithmeticException ae = (ArithmeticException)e;
      
      String message = ae.getMessage();
      if (message.contains(": ")) {
        message = message.substring(message.indexOf(": ")+2);
      }

      errorMessage += formatter.format(
          PROPERTY_REPO.getValue(ARITHMETIC_ERROR_MESSAGE_KEY),
          message);
    } else if (e instanceof SyntaxError) {
      SyntaxError se = (SyntaxError)e;
      
      int row = se.getRowIndex()+1;
      int column = se.getColumnIndex();
      String line = se.getCurrentLine();
      if (column > line.length()) column = line.length();
      
      errorMessage += 
          formatter.format(PROPERTY_REPO.getValue(SYNTAX_ERROR_MESSAGE_KEY),
              row, column) + ":<br><pre>" +
                      line + "<br>";
      
      for (int i=0; i<column-1; i++) {
        errorMessage += "&nbsp;";
      }
      errorMessage += "^</pre>";
    } else {
      errorMessage += e.getMessage();
    }
    
    errorMessage += "</body></html>";

    JOptionPane.showMessageDialog(this,
        errorMessage,
        errorTitle,
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Disposes the dialog.
   */
  public void cancel() {
    dispose();
  }

  /**
   * Checks to see if this dialog is for a channel update operation.
   * @return
   *    true if it is an update operation, otherwise false
   */
  public boolean isChannelUpdateOperation() {
    return channel != null;
  }

  /**
   * Checks to see if this dialog is for a new add operation.
   * @return
   *    true if it is a new channel add operation, otherwise false
   */
  public boolean isChannelAddOperation() {
    return channel == null;
  }
}
