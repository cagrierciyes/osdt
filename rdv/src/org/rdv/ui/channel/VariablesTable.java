
package org.rdv.ui.channel;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.RDV;
import org.rdv.data.LocalChannel;
import org.rdv.data.LocalChannelManager;
import org.rdv.formatter.FormatterService;
import org.rdv.property.PropertyRepository;
import org.rdv.rbnb.RBNBUtilities;

public class VariablesTable {

  private static final Log LOGGER = LogFactory.getLog(VariablesTable.class);
  private static final RDV REAL_TIME_DATA_VIEWER = RDV.getInstance(RDV.class);
  private DefaultTableModel tableModel;
  private JTable variablesTable;
  private static final String VARIABLES_TABLE_CHANNEL_COLUMN_TEXT
    = "variablesTableChannelColumnText";

  private static final String VARIABLES_TABLE_NAME_COLUMN_TEXT
    = "variablesTableNameColumnText";

  private static final PropertyRepository PROPERTY_REPO
    = PropertyRepository.getInstance();
  private final JButton removeVariableButton;

  public VariablesTable(JButton removeVariableButton) {
    this.removeVariableButton = removeVariableButton;
    tableModel = new DefaultTableModel(new Object[][] {},
        new String[] {
        PROPERTY_REPO.getValue(VARIABLES_TABLE_CHANNEL_COLUMN_TEXT),
        PROPERTY_REPO.getValue(VARIABLES_TABLE_NAME_COLUMN_TEXT)});
    tableModel.addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        variablesTableChanged(e);
      }
    });
    variablesTable = new JTable(tableModel);
    variablesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    variablesTable.setPreferredScrollableViewportSize(new Dimension(200,100));
    // variablesTable.setFillsViewportHeight(true);
    variablesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        variablesTableSectionChanged();
      }
    });
    boolean showHidden = REAL_TIME_DATA_VIEWER.getMainPanel().isHiddenChannelsVisible();
    List<String> serverChannels = RBNBUtilities.getServerChannels(
        "application/octet-stream", showHidden);
    // set the channels column editor to a combo box filled with the server channels
    JComboBox channelComboBox = new JComboBox(serverChannels.toArray());
    TableColumn channelColumn = variablesTable.getColumnModel().getColumn(0);
    channelColumn.setCellEditor(new DefaultCellEditor(channelComboBox));
    variablesTable.setRowHeight(channelComboBox.getPreferredSize().height);
  }

  /**
   * Checks to see if the current variables are valid
   * @param errorBuffer
   *    the buffer that contains the list of validation error messages
   * @return
   *    true if the variables are all valid, otherwise false
   */
  public boolean variablesValid(StringBuffer errorBuffer) {
    List<String> variables = new ArrayList<String>();
    for (int row=0; row < getRowCount(); row++) {
      String variableChannel = (String) tableModel.getValueAt(row, 0);
      String variableName = (String) tableModel.getValueAt(row, 1);

      if (variableName == null || variableName.isEmpty()
          || variableChannel == null || variableChannel.isEmpty()) {
        return false;
      }

     // check for invalid variable name
      if (!variableName.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
        String message = FormatterService.format(
            PROPERTY_REPO.getValue("invalidVariableError"), variableName);
        LOGGER.error(message);
        errorBuffer.append(message);
        return false;
      }

     // check for duplicate variable name
      if (variables.contains(variableName)) {
        String message = FormatterService.format(
            PROPERTY_REPO.getValue("duplicateVariableNameError"), variableName);
        LOGGER.error(message);
        errorBuffer.append(message);
        return false;
      }
      variables.add(variableName);
    }

    // no variables specified
    if (variables.isEmpty()) {
      String message = PROPERTY_REPO.getValue("variableNotSpecified");
      LOGGER.error(message);
      errorBuffer.append(message);
      return false;
    }
    return true;
  }

  public List<String> getVariableNameList() {
    List<String> variableNameList = new ArrayList<String>();
    for (int row=0; row < getRowCount(); row++) {
      String variableName = (String) tableModel.getValueAt(row, 1);
      variableNameList.add(variableName);
    }
    return variableNameList;
  }

  public boolean formulaContainsAllVariables(String formula) {
    if (formula == null || formula.isEmpty()) {
      return false;
    }
    // check if all the variables are in the formula
    for (String variable : getVariableNameList()) {
      if (!formula.contains(variable)) {
        return false;
      }
    }
    return true;
  }

  public int getRowCount() {
    return tableModel.getRowCount();
  }

  public void setValueAt(Object aValue, int row, int column) {
    tableModel.setValueAt(aValue, row, column);
  }
  
  /**
   * Called when the variables table has a change. This will try to fill in the
   * name if a new channel is added.
   * 
   * @param e  the table model event
   */
  private void variablesTableChanged(TableModelEvent e) {
    // channel column
    if (e.getColumn() == 0) {
      for (int row=e.getFirstRow(); row<=e.getLastRow(); row++) {
        String channel = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        
        if (channel != null && (name == null || name.length() == 0)) {
          // create variable name from the channel name
          int index = channel.lastIndexOf('/');
          if (index != -1 && channel.length() > index+1) {
            name = channel.substring(index+1);
            
            // remove invalid characters
            name = name.replaceAll("[^a-zA-Z0-9]", "");
            
            // make sure it starts with a letter
            if (name.matches("^\\d")) {
              name = "x" + name;
            }
            
            // set the name if any characters are left
            if (name.length() > 0) {
              tableModel.setValueAt(name, row, 1);
            }
          }
        }
      }
    }
  }

  /**
   * Called when the selection changes in the variables table. This enables or
   * disables the remove variable button.
   */
  private void variablesTableSectionChanged() {
    boolean hasSelectedRows = variablesTable.getSelectedRows().length > 0;
    removeVariableButton.setEnabled(hasSelectedRows);
  }

  /**
   * Adds a row the variables table and pops up the channel selector combo box.
   */
    public void addEmptyRow() {
      int row = tableModel.getRowCount();
      tableModel.insertRow(row, (Object[])null);

      if (variablesTable.editCellAt(row, 0)) {
        variablesTable.scrollRectToVisible(variablesTable.getCellRect(row, 0, true));

//        DefaultCellEditor chanelCellEditor = (DefaultCellEditor) variablesTable.getCellEditor(row, 0);
//        JComboBox channelComboBox = (JComboBox) chanelCellEditor.getComponent();
//        channelComboBox.requestFocusInWindow();
//        try { channelComboBox.showPopup(); } catch (IllegalComponentStateException e) {}
      }
    }

    /**
     * Adds an entry to the variables table.
     * @param channelName
     *    the name of the channel to add
     * @param variableName
     *    the name of the variable to add
     */
    public void addVariable(String channelName, String variableName) {
      tableModel.addRow(new String[] {channelName, variableName});
    }

    /**
     * Removes the select rows from the variables table.
     */
    public void removeVariableRow() {
      int[] rows = variablesTable.getSelectedRows();
      if (rows.length == 0) {
        return;
      }

      Arrays.sort(rows);

      for (int i=rows.length-1; i>=0; i--) {
        tableModel.removeRow(rows[i]);
      }
    }

  /**
   * Creates and adds the channel. If there is an error doing this, an error
   * message will be displayed.
   * @param name
   * @param unit
   * @param formula
   */
  public void createChannel(String name, String unit, String formula) {
    // set the unit to null if it is empty
    if (unit != null && unit.length() == 0) {
      unit = null;
    }

    // get variables from the rows that aren't empty
    Map<String, String> variables = new HashMap<String, String>();
    for (int row = 0; row < tableModel.getRowCount(); row++) {
      String variableChannel = (String) tableModel.getValueAt(row, 0);
      String variableName = (String) tableModel.getValueAt(row, 1);
      if (variableChannel != null && variableChannel.length() > 0
          && variableName != null && variableName.length() > 0) {
        variables.put(variableName, variableChannel);
      }
    }

    LocalChannel channel = new LocalChannel(name, unit, variables, formula);
    LocalChannelManager.getInstance().setChannel(channel);
  }

  public Map<String, String> getMap() {
    Map<String, String> map = new HashMap<String, String>();
    for (int row = 0; row < tableModel.getRowCount(); row++) {
      String variableChannel = (String) tableModel.getValueAt(row, 0);
      String variableName = (String) tableModel.getValueAt(row, 1);
      map.put(variableName, variableChannel);
    }
    return map;
  }

  public Component getComponent() {
    return variablesTable;
  }

  public int[] getSelectedRows() {
    return variablesTable.getSelectedRows();
  }

  public void insertRow(int row, Vector rowData) {
    tableModel.insertRow(row, rowData);
  }

  public void insertRow(int row, Object[] rowData) {
    tableModel.insertRow(row, rowData);
  }

  public boolean editCellAt(int row, int column) {
    return variablesTable.editCellAt(row, column);
  }

  public void addTableModelListener(TableModelListener listener) {
    tableModel.addTableModelListener(listener);
  }
}
