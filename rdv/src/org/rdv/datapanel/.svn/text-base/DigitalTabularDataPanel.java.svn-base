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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/datapanel/DigitalTabularDataPanel.java $
 * $Revision: 1287 $
 * $Date: 2008-11-26 09:42:11 -0500 (Wed, 26 Nov 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.datapanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;
import org.rdv.data.Channel;
import org.rdv.data.TabularChannelSeries;
import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;
import org.rdv.rbnb.Player;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBDataRequest;
import org.rdv.rbnb.SubscriptionRequest;
import org.rdv.rbnb.SubscriptionResponse;
import org.rdv.rbnb.TimeSeriesData;
import org.rdv.ui.AdjustThresholdDialog;
import org.rdv.ui.ChannelListDataFlavor;
import org.rdv.ui.FontChooserDialog;
import org.rdv.ui.UIUtilities;

import com.rbnb.sapi.ChannelMap;

/**
 * A Data Panel extension to display numeric data in a tabular form. Maximum and
 * minimum values are also displayed.
 * 
 * @author Jason P. Hanley
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author Drew Daugherty
 * @since 1.3
 */
public class DigitalTabularDataPanel extends AbstractDataPanel {

	static Log log = LogFactory.getLog(DigitalTabularDataPanel.class.getName());

	private static final String SENSOR_COL_NAME = "Name";
	private static final String VALUE_COL_NAME = "Value";
	private static final String UNIT_COL_NAME = "Unit";
	private static final String MIN_COL_NAME = "Min";
	private static final String MAX_COL_NAME = "Max";
	private static final String MIN_THRESH_COL_NAME = "Min Thresh";
  private static final String MAX_THRESH_COL_NAME = "Max Thresh";
       
	/**
	 * The main panel
	 * 
	 * @since 1.3
	 */
	private JPanel mainPanel;

	/**
	 * The container for the tables
	 */
	private Box panelBox;

	/**
	 * The maximum number of column groups
	 */
	private static final int MAX_COLUMN_GROUP_COUNT = 10;

	 /**
   * The maximum precision. 
   */
  private static final int MAX_PRECISION = 12;
  
	/**
	 * The current number of column groups
	 */
	private int columnGroupCount;

	/**
	 * The data models for the table
	 */
	private List<DataTableModel> tableModels;

	/**
	 * The tables
	 */
	private List<TabularTable> tables;

	 /**
   * A map from channel names to data (units, min/max thresholds, etc.)
   */
  private Map<String, DataChannel> channelMap;

	/**
	 * The cell renderer for sensor data
	 */
	private DoubleTableCellRenderer doubleCellRenderer;

	/**
	 * Cell renderer for other numeric data (min/max, etc.)
	 */
	private DataTableCellRenderer dataCellRenderer;

	 /**
   * Cell renderer for labels (name, unit, etc.)
   */
	private TextTableCellRenderer textCellRenderer;
	
	/**
	 * Table cell text font.
	 */
	private Font font_=new DefaultTableCellRenderer().getFont();
	
	/**
   * Number of significant digits used in formatting double precision numbers.
   */
	private int precision_ = 9;
	
	/**
	 * Format string for data significant digits.
	 */
	private String precisionZeros_ = "000000000";
	
	/**
	 * The button to set decimal data rendering
	 */
	private JToggleButton decimalButton;

	/**
	 * The button to set engineering data rendering
	 */
	private JToggleButton engineeringButton;

	/**
	 * The group of check box components to control the the min/max columns
	 * visibility
	 */
	private CheckBoxGroup showMinMaxCheckBoxGroup;

	/**
	 * The group of check box components to control the the threshold columns
	 * visibility
	 */
	private CheckBoxGroup showThresholdCheckBoxGroup;

	/**
	 * The last time data was displayed in the UI
	 * 
	 * @since 1.2
	 */
	double lastTimeDisplayed;

	/**
	 * The maximum number of channels allowed in this data panel;
	 */
	private static final int MAX_CHANNELS_PER_COLUMN_GROUP = 40;

	/**
	 * The percentage of the warning value, when a value gets within this much
	 * of a threshold, a yellow background will be shown.
	 */
	private static final double WARNING_PERCENTAGE = 0.25;

	/**
	 * The percentage of the critical value, when a value gets within this much
	 * of a threshold, a red background will be shown.
	 */
	private static final double CRITICAL_PERCENTAGE = 0.1;

	private JCheckBox useOffsetsCheckBox;

	/**
	 * @param len
	 * @return a string containing len zero characters.
	 */
	private String zeros(final int len) {
	  if (len < 0) {
	    throw new IllegalArgumentException();
	  }
	  for (;;) {
  	  final String zeros = precisionZeros_;
  	  if (zeros.length() >= len) {
  	    return zeros.substring(0, len);
  	  }
  	  precisionZeros_ = zeros+zeros;
	  }
	}
	
	private class TableScrollPane extends JScrollPane{
	  
	  /**
     * 
     */
    private static final long serialVersionUID = -338234108145035149L;
    
    private TabularTable table_;
    
    public TableScrollPane(TabularTable table){
      super(table);
      table_=table;
    }
    
    public TabularTable  getTable(){return table_;}
	}
	
	private class TabularTable extends JTable{
	  
	  /**
     * 
     */
    private static final long serialVersionUID = -3588822410747957846L;

    private int column_;
	  
	  private DataTableModel model_;
	  
	  public TabularTable(DataTableModel tm, int column){
	    super(tm);
	    model_=tm;
	    column_=column;
	  }
	  
	  public int getColumn(){return column_;}
	  	  
	  private void fireDataChanged(){
	    
	    // TODO save selected rows
	    int[] rowIndices = getSelectedRows();

	    model_.fireTableDataChanged();
	    
	    // TODO restore selections
	    for (int i=0;i<rowIndices.length;i++){
	      addRowSelectionInterval(rowIndices[i], rowIndices[i]);
	    }
	  }
	  
	  public DataTableModel getTableModel(){
	    return model_;
	  }
	 
	  public boolean hasChannel(String name){
	    return model_.hasChannel(name);
	  }
	  
	  public void moveRowUp(int rowNum){
	    if(rowNum>0){
	      model_.reorder(rowNum, rowNum-1);
	    }
	  }
	  
	  public void moveRowDown(int rowNum){
	    if(rowNum<model_.getRowCount()-1){
        model_.reorder(rowNum, rowNum+1);
      }
	  }
	  
	  public void addRow(String name, int rowNum){
	    model_.addRow(name, rowNum);
	  }
	  
	  public void appendRow(String name){
      model_.appendRow(name);
    }
	  
	  public List<Integer> getChannelRows(String name){
	    return model_.getChannelRows(name);
	  }
	  
	  public List<Integer> getBlankRows(){
      return model_.getBlankRows();
    }
	  
	  public void setCellRenderers(){
  	  getColumn(VALUE_COL_NAME).setCellRenderer(dataCellRenderer);
  	  getColumn(UNIT_COL_NAME).setCellRenderer(textCellRenderer);
      getColumn(SENSOR_COL_NAME).setCellRenderer(textCellRenderer);
      
      if (model_.maxMinVisible) {
        getColumn(MIN_COL_NAME).setCellRenderer(doubleCellRenderer);
        getColumn(MAX_COL_NAME).setCellRenderer(doubleCellRenderer);
      }
  
      if (model_.thresholdVisible) {
        getColumn(MIN_THRESH_COL_NAME).setCellRenderer(
            doubleCellRenderer);
        getColumn(MAX_THRESH_COL_NAME).setCellRenderer(
            doubleCellRenderer);
      }
    }
	  
	  /**
	   * Remove all the rows that are selected. If a
	   * selected row is not blank, the channel in that
	   * row will be removed from the panel.
	   */
	  private void removeSelectedRows() {
	    List<String> selectedChannels = new ArrayList<String>();
	    int[] selectedRows = getSelectedRows();
	    clearSelection();

	    for (int j = selectedRows.length - 1; j >= 0; j--) {
	      int row = selectedRows[j];
	      if (model_.isBlankRow(row)) {
	        model_.deleteBlankRow(row);
	      } else {
	        DataRow dataRow = model_.getRowAt(row);
	        selectedChannels.add(dataRow.getName());
	      }
	    }

	    for (String channelName : selectedChannels) {
	      removeChannel(channelName);
	    }
	  }
	  
    public List<DataChannel> getSelectedDataChannels(){
      List<DataChannel> dcList = new ArrayList<DataChannel>();
      for(int i = 0; i < getSelectedRows().length; i++){
        dcList.add(channelMap.get(model_.getRowAt(getSelectedRows()[i]).getName()));
      }
      return dcList;
    }
	  
	  
	}
	
	/**
	 * Initialize the data panel.
	 * 
	 * @since 1.2
	 */
	public DigitalTabularDataPanel() {
		super();

		tableModels = new ArrayList<DataTableModel>();
		tables = new ArrayList<TabularTable>();
		channelMap = new HashMap<String, DataChannel>();

		lastTimeDisplayed = -1;

		initComponents();

		setDataComponent(mainPanel);
	}
	
	public SubscriptionRequest getSubscriptionRequest(){
    return new RBNBDataRequest(this,
        SubscriptionRequest.TYPE_TABULAR,timeScale,getSeries());
  }
	
	/**
	 * Initialize the container and add the header.
	 * 
	 * @since 1.2
	 */
	private void initComponents() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		panelBox = Box.createHorizontalBox();

		dataCellRenderer = new DataTableCellRenderer();
		doubleCellRenderer = new DoubleTableCellRenderer();
		textCellRenderer = new TextTableCellRenderer();
		
		showMinMaxCheckBoxGroup = new CheckBoxGroup();
		showThresholdCheckBoxGroup = new CheckBoxGroup();

		addColumn();

		mainPanel.add(panelBox, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JPanel offsetsPanel = new JPanel();
		offsetsPanel.setLayout(new GridLayout());
		useOffsetsCheckBox = new JCheckBox("Use Offsets");
		useOffsetsCheckBox.setToolTipText("Use Offsets");
		useOffsetsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				boolean checked = ((JCheckBox) ae.getSource()).isSelected();

				useOffsetRenderer(checked);
			}
		});
		offsetsPanel.add(useOffsetsCheckBox);

		JButton takeOffsetsButton = new JButton("Take Offsets");
		takeOffsetsButton.setToolTipText("Take Offsets");
		takeOffsetsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				useOffsetsCheckBox.setSelected(true); // this moved from above
														// repaint
				for (int i = 0; i < columnGroupCount; i++) {
					((DataTableModel) tableModels.get(i)).takeOffsets();
					((DataTableModel) tableModels.get(i)).useOffsets(true);
					((JTable) tables.get(i)).repaint();
				} // for
			} // actionPerformed ()
		}); // actionlistener
		offsetsPanel.add(takeOffsetsButton);
 
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 0;
		c.ipadx=5;
		c.anchor=GridBagConstraints.WEST;
		buttonPanel.add(offsetsPanel, c);

		JPanel formatPanel = new JPanel();
		formatPanel.setLayout(new GridLayout());
		decimalButton = new JToggleButton("Decimal", true);
		decimalButton.setToolTipText("Decimal");
		decimalButton.setSelected(true);
		decimalButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				useEngineeringRenderer(false);
			}
		});
		formatPanel.add(decimalButton);

		engineeringButton = new JToggleButton("Engineering");
		engineeringButton.setToolTipText("Engineering");
		engineeringButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				useEngineeringRenderer(true);
			}
		});
		formatPanel.add(engineeringButton);

		ButtonGroup formatButtonGroup = new ButtonGroup();
		formatButtonGroup.add(decimalButton);
		formatButtonGroup.add(engineeringButton);

		c.weightx = 0.1;
		c.gridx = 1;
		c.gridy = 0;
		c.ipadx = 5;
		c.anchor=GridBagConstraints.EAST;
		buttonPanel.add(formatPanel, c);
		
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		//mainPanel.addMouseListener(new MouseInputAdapter() {
		//});

	}

	private void addColumn() {
		if (columnGroupCount == MAX_COLUMN_GROUP_COUNT) {
			return;
		}

		if (columnGroupCount != 0) {
			panelBox.add(Box.createHorizontalStrut(7));
		}

		final int tableIndex = columnGroupCount;

		final DataTableModel tableModel = new DataTableModel();
		final TabularTable table = new TabularTable(tableModel, tableIndex);

		// TODO implement swing drag and drop
		// may not be able to mix and match swing and awt...
//		table.setDragEnabled(true);
//    table.setDropMode(DropMode.INSERT_ROWS);
//		table.setTransferHandler(new MyTableRowTransferHandler(table));

		table.getTableHeader().setReorderingAllowed(false);
		//table.setSelectionMode(0);	
		//table.setFillsViewportHeight(true);

		table.setName(DigitalTabularDataPanel.class.getName() + " JTable #"
				+ Integer.toString(columnGroupCount));

		if (showThresholdCheckBoxGroup.isSelected()) {
			tableModel.setThresholdVisible(true);
		}

		if (showMinMaxCheckBoxGroup.isSelected()) {
			tableModel.setMaxMinVisible(true);
		}
		table.setCellRenderers();
		
		tables.add(table);
		tableModels.add(tableModel);

		final TableScrollPane tableScrollPane = new TableScrollPane(table);
		panelBox.add(tableScrollPane);
		
		// add mouse listeners for popup menu
		table.addMouseListener(getPopupMenuMouseListener());
		table.addKeyListener(new TableKeyListener(table));
		tableScrollPane.addMouseListener(getPopupMenuMouseListener());
		
		panelBox.revalidate();

		columnGroupCount++;

		properties.setProperty("numberOfColumns", Integer
				.toString(columnGroupCount));
	}

	/**
	 * Dynamically add items to right-click popup menu before
	 * display. 
	 */
	public void buildPopupMenu(JPopupMenu menu, MouseEvent e){
	   super.buildPopupMenu(menu,e);
	   
	   final TabularTable table;
	   
	   if(e.getComponent() instanceof TabularTable){
	     table=(TabularTable)e.getComponent();
	   }else if(e.getComponent() instanceof TableScrollPane){
	     table=((TableScrollPane)e.getComponent()).getTable();
	   }else if(tables.size()>0){
	     table=tables.get(0);
	   }else{
	     // no tables
	     return;
	   }
	   
	   final JMenuItem copyMenuItem = new JMenuItem("Copy");
	   if(table!=null){
  	    copyMenuItem.addActionListener(new ActionListener() {
  	      public void actionPerformed(ActionEvent ae) {
  	        TransferHandler.getCopyAction().actionPerformed(
  	            new ActionEvent(table, ae.getID(), ae
  	                .getActionCommand(), ae.getWhen(), ae
  	                .getModifiers()));
  	      }
  	    });
	   }else{
	     
	   }
	    menu.add(copyMenuItem);

	    menu.addSeparator();

	    JMenuItem printMenuItem = new JMenuItem("Print...");
	    printMenuItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        try {
	          table.print(JTable.PrintMode.FIT_WIDTH);
	        } catch (PrinterException pe) {
	        }
	      }
	    });
	    menu.add(printMenuItem);
	    menu.addSeparator();

	    final JCheckBoxMenuItem showMaxMinMenuItem = new JCheckBoxMenuItem(
	        "Show min/max columns", false);
	    showMaxMinMenuItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent ae) {
	        setMaxMinVisible(showMaxMinMenuItem.isSelected());
	      }
	    });
	    showMinMaxCheckBoxGroup.addCheckBox(showMaxMinMenuItem);
	    menu.add(showMaxMinMenuItem);

	    final JCheckBoxMenuItem showThresholdMenuItem = new JCheckBoxMenuItem(
	        "Show threshold columns", false);
	    showThresholdMenuItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent ae) {
	        setThresholdVisible(showThresholdMenuItem.isSelected());
	      }
	    });
	    showThresholdCheckBoxGroup.addCheckBox(showThresholdMenuItem);
	    menu.add(showThresholdMenuItem);

	    menu.addSeparator();

	    JMenuItem blankRowMenuItem = new JMenuItem("Insert blank row");
	    blankRowMenuItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        table.getTableModel().appendBlankRow();
	      }
	    });
	    menu.add(blankRowMenuItem);

	    JMenuItem removeSelectedRowsMenuItem = new JMenuItem(
	        "Remove selected rows");
	    removeSelectedRowsMenuItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        table.removeSelectedRows();
	      }
	    });
	    menu.add(removeSelectedRowsMenuItem);

	    menu.addSeparator();

	    JMenu numberOfColumnsMenu = new JMenu("Number of columns");
	    numberOfColumnsMenu.addMenuListener(new MenuListener() {
	      public void menuSelected(MenuEvent me) {
	        JMenu menu = (JMenu) me.getSource();
	        for (int j = 0; j < MAX_COLUMN_GROUP_COUNT; j++) {
	          JMenuItem menuItem = menu.getItem(j);
	          boolean selected = (j == (columnGroupCount - 1));
	          menuItem.setSelected(selected);
	        }
	      }

	      public void menuDeselected(MenuEvent me) {
	      }

	      public void menuCanceled(MenuEvent me) {
	      }
	    });

	    for (int i = 0; i < MAX_COLUMN_GROUP_COUNT; i++) {
	      final int number = i + 1;
	      JRadioButtonMenuItem item = new JRadioButtonMenuItem(Integer
	          .toString(number));
	      item.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent ae) {
	          setNumberOfColumns(number);
	        }
	      });
	      numberOfColumnsMenu.add(item);
	    }
	    menu.add(numberOfColumnsMenu);

	    final JMenuItem changeFontMenuItem = new JMenuItem("Select Font...");
	    changeFontMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // open font selection dialog
          FontChooserDialog dlg = new FontChooserDialog(UIUtilities.getMainFrame());
          dlg.setVisible(true);
          Font selectedFont = dlg.getSelectedFont();
          if (selectedFont!=null){
            setFont(selectedFont);
          }  
        }
      });
	    menu.add(changeFontMenuItem);
	    
	    JMenu precisionMenu = new JMenu("Precision");
	    precisionMenu.addMenuListener(new MenuListener() {
        public void menuSelected(MenuEvent me) {
          JMenu menu = (JMenu) me.getSource();
          for (int j = 0; j < MAX_PRECISION; j++) {
            JMenuItem menuItem = menu.getItem(j);
            boolean selected = (j+1 == precision_);
            menuItem.setSelected(selected);
          }
        }
        public void menuDeselected(MenuEvent me) {}
        public void menuCanceled(MenuEvent me) {}
      });

      for (int i = 0; i < MAX_PRECISION; i++) {
        final int number = i+1;
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(Integer
            .toString(number));
        item.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            setPrecision(number);
          }
        });
        precisionMenu.add(item);
      }
      menu.add(precisionMenu);
      
      final JMenuItem adjustMaxThresholdMenuItem = new JMenuItem("Adjust maximum threshold for selected channels...");
      adjustMaxThresholdMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          AdjustThresholdDialog dlg = new AdjustThresholdDialog(UIUtilities.getMainFrame());
          dlg.setVisible(true);
          Double adjustedThreshold = dlg.getAdjustedThreshold();
          if(adjustedThreshold == null) return;
          setMaxThresholdMultiple(table.getSelectedDataChannels(), adjustedThreshold);
          table.repaint();
        }
      });
      menu.add(adjustMaxThresholdMenuItem);
      
      final JMenuItem adjustMinThresholdMenuItem = new JMenuItem("Adjust minimum threshold for selected channels...");
      adjustMinThresholdMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          AdjustThresholdDialog dlg = new AdjustThresholdDialog(UIUtilities.getMainFrame());
          dlg.setVisible(true);
          Double adjustedThreshold = dlg.getAdjustedThreshold();
          if(adjustedThreshold == null) return;
          setMinThresholdMultiple(table.getSelectedDataChannels(), adjustedThreshold);
          table.repaint();
        }
      });
      menu.add(adjustMinThresholdMenuItem);

	    boolean anyRowsSelected = table.getSelectedRowCount() > 0;
	    copyMenuItem.setEnabled(anyRowsSelected);
	    removeSelectedRowsMenuItem.setEnabled(anyRowsSelected);
	}
	
	/**
	 * Delete rightmost table.
	 * TODO arbitrary table delete. 
	 */
	private void removeColumn() {
		if (columnGroupCount == 1) {
			return;
		}

		columnGroupCount--;

		tables.remove(columnGroupCount);
		tableModels.remove(columnGroupCount);

		removeDeadSeries();
    
		panelBox.remove(columnGroupCount * 2);
		panelBox.remove(columnGroupCount * 2 - 1);
		panelBox.revalidate();
	}

	/**
	 * 
	 * @param name Channel name to test.
	 * @return true if channel is displayed, false otherwise.
	 */
	private boolean displayingChannel(String name){
	  boolean displaying=false;
    for(TabularTable table : tables){
      if(table.hasChannel(name)){
        displaying=true;
        break;
      }
    }
    return displaying;
	}
	
	/**
	 * Unsub channels if they are no longer being
	 * displayed.
	 * @param name Channel name to unsub.
	 */
	private void removeDeadSeries(){
	  for (VisualizationSeries vs : seriesList_){
	    String channel = vs.getChannels().iterator().next();
  	  if(!displayingChannel(channel)){
  	    removeSeries(vs);
  	  }
	  }
	}

	/**
	 * Size the tables to have the specifies number of
	 * columns, adding or deleting as necessary. 
	 * @param columns  The number of columns to display. 
	 */
	private void setNumberOfColumns(int columns) {
		if (columnGroupCount < columns) {
			for (int i = columnGroupCount; i < columns; i++) {
				addColumn();
			}
		} else if (columnGroupCount > columns) {
			for (int i = columnGroupCount; i > columns; i--) {
				removeColumn();
			}
		}
	}

	/**
	 * Change the font for all tables.
	 * @param font the new table font.
	 */
	private void setFont(Font font){
	  font_=font;
	  Iterator<TabularTable> it=tables.iterator();
	  while(it.hasNext()){
	    TabularTable table=it.next();
  	  FontMetrics fm = table.getFontMetrics(font_);
      table.setRowHeight(fm.getMaxAscent()+fm.getMaxDescent());
      // invalidate to show changes
      table.invalidate();
      table.repaint();
	  }
	  String value=font.getFamily()+","+font.getStyle()+","+font.getSize();
 	  properties.setProperty("font", value);
	}
	
	/**
	 * Set the number of decimal places to display. Does nothing
	 * if precision less than 1.
	 * @param precision New display precision.
	 */
	private void setPrecision(int precision){
	  if(precision<1)return;
	  
	  precision_=precision;
	  Iterator<TabularTable> it=tables.iterator();
    while(it.hasNext()){
      TabularTable table=it.next();
      // invalidate to show changes
      table.invalidate();
      table.repaint();
    }
    properties.setProperty("precision", Integer.toString(precision_));
	}
	
	/**
	 * Display numbers with offsets.
	 * @param useOffset
	 */
	private void useOffsetRenderer(boolean useOffset) {

		useOffsetsCheckBox.setSelected(useOffset);
		for (int i = 0; i < columnGroupCount; i++) {
			((DataTableModel) tableModels.get(i)).useOffsets(useOffset);
			((JTable) tables.get(i)).repaint();
		}

		if (useOffset) {
			properties.setProperty("useoffset", "true");
		} else
			properties.setProperty("useoffset", "false");
	}

	private void useEngineeringRenderer(boolean useEngineeringRenderer) {
		dataCellRenderer.setShowEngineeringFormat(useEngineeringRenderer);
		doubleCellRenderer.setShowEngineeringFormat(useEngineeringRenderer);

		for (JTable table : tables) {
			table.repaint();
		}

		if (useEngineeringRenderer) {
			engineeringButton.setSelected(true);
			properties.setProperty("renderer", "engineering");
		} else {
			decimalButton.setSelected(true);
			properties.remove("renderer");
		}
	}

	private void setMaxMinVisible(boolean maxMinVisible) {
		if (showMinMaxCheckBoxGroup.isSelected() != maxMinVisible) {
			for (int i = 0; i < this.columnGroupCount; i++) {
				DataTableModel tableModel = tableModels.get(i);
				TabularTable table = tables.get(i);

				tableModel.setMaxMinVisible(maxMinVisible);
				table.setCellRenderers();
			}

			showMinMaxCheckBoxGroup.setSelected(maxMinVisible);

			if (maxMinVisible) {
				properties.setProperty("maxMinVisible", "true");
			} else {
				properties.remove("maxMinVisible");
			}
		}
	}

	private void setThresholdVisible(boolean thresholdVisible) {
		if (showThresholdCheckBoxGroup.isSelected() != thresholdVisible) {
			for (int i = 0; i < columnGroupCount; i++) {
				DataTableModel tableModel = tableModels.get(i);
				TabularTable table = tables.get(i);

				tableModel.setThresholdVisible(thresholdVisible);
				table.setCellRenderers();
			}

			showThresholdCheckBoxGroup.setSelected(thresholdVisible);

			if (thresholdVisible) {
				properties.setProperty("thresholdVisible", "true");
			} else {
				properties.remove("thresholdVisible");
			}
		}
	}

	void clearData() {

		for (int i = 0; i < columnGroupCount; i++) {
			((DataTableModel) tableModels.get(i)).clearData();
		}

		lastTimeDisplayed = -1;
	}

	public boolean supportsMultipleChannels() {
		return true;
	}

	
	/**
	 * an overridden method from
	 * 
	 * @see org.rdv.datapanel.AbstractDataPanel for the
	 * @see DropTargetListener interface
	 */
  @SuppressWarnings("unchecked")
  public void drop(DropTargetDropEvent e) {
		try {
			int dropAction = e.getDropAction();
			if (dropAction == DnDConstants.ACTION_COPY){
			  // TODO add support for drag and drop row reorder here?
			  //System.out.println(e.getSource()+" "+e.getTransferable());
			  
			}else if (dropAction == DnDConstants.ACTION_LINK) {
        DataFlavor channelListDataFlavor = new ChannelListDataFlavor();
				Transferable tr = e.getTransferable();
				if (e.isDataFlavorSupported(channelListDataFlavor)) {
					e.acceptDrop(DnDConstants.ACTION_LINK);
					e.dropComplete(true);

					// calculate which table the x coordinate of the mouse
					// corresponds to
					double clickX = e.getLocation().getX(); // get the mouse x
															// coordinate
					int compWidth = dataComponent.getWidth(); // gets the
																// width of this
																// component
					final int tableNum = (int) (clickX * columnGroupCount / compWidth);

					final List<String> channels = (List)tr.getTransferData(channelListDataFlavor);

					new Thread() {
						public void run() {
						  startAddingChannels();
							for (int i=0; i<channels.size(); i++) {
                String channel = channels.get(i);
								boolean status;
								//if (supportsMultipleChannels()) {
								  TabularChannelSeries tcs = new TabularChannelSeries(channel,tableNum);
									status = addSeries(tcs);
									//tables.get(tableNum).appendRow(channel);
//								} else {
//									status = setChannel(channel);
//									//tables.get(tableNum).appendRow(channel);
//								}

								if (!status) {
									// TODO display an error in the UI
								}
							}
							stopAddingChannels();
						}
					}.start();
				} else {
					e.rejectDrop();
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (UnsupportedFlavorException ufe) {
			ufe.printStackTrace();
		}
	} // drop ()

  @Override
  public Properties getProperties() {
    for(String chanName : channelMap.keySet()){
      //DataChannel chan = channelMap.get(chanName);
      updateChannelProperties(chanName);
    }
    updateBlankProperties();
    return properties;
  }
  
//  @Override
//  public String getTitle() {
//    return "Tabular "+super.getTitle();
//  }
  
//  @Override
//  public boolean addChannels(Collection<String> channelNames) {
//    
//    for(String name:channelNames){
//      addChannel(name);
//    }
//    
//    for(String name:channelMap.keySet()){
//      updateChannelProperties(name);
//    }
//    return true;
//  }
  protected VisualizationSeries getSeriesForChannel(String channelName){
    return new TabularChannelSeries(channelName,0);
  }
  
  @Override
  protected void onSeriesAdded(VisualizationSeries vs){
    int colId = ((TabularChannelSeries)vs).getColumnId();
    addChannel(vs.getChannels().iterator().next(), colId);
  }
  
//	public boolean addChannel(String channelName) {
//	  // add to table 0 by default
//	  return addChannel(channelName, 0);
//	}

	private boolean addChannel(String channelName, int tableNum) {
	  if (channelName.startsWith("BLANK ")) {
      return false;
    }
    
    if(channelMap.get(channelName) == null)
      channelMap.put(channelName,new DataChannel(channelName));
    
    //if (super.addChannel(channelName)) {  
      // ensure we have a row to display this channel in
      if(!displayingChannel(channelName)){
        tables.get(tableNum).appendRow(channelName);
      }
      
      return true;
    //}else{
    //  return false;
    //}
	}

	@Override
	protected void channelAdded(String channelName) {
		String labelText = channelName;

		Channel channel = RBNBController.getInstance().getChannel(channelName);
		String unit = null;
		if (channel != null) {
			unit = channel.getUnit();
			if (unit != null) {
				labelText += "(" + unit + ")";
			}
		}

		String lowerThresholdString = (String) (lowerThresholds
				.get(channelName));
		String upperThresholdString = (String) (upperThresholds
				.get(channelName));

		// Double.NEGATIVE_INFINITY and Double.POSITIVE_INFINITY represent empty
		// thresholds
		double lowerThreshold = Double.NEGATIVE_INFINITY;
		double upperThreshold = Double.POSITIVE_INFINITY;

		// handle errors generated by daq - "Unknown command 'list-lowerbounds'"
		if (lowerThresholdString != null) {
			try {
				lowerThreshold = Double.parseDouble(lowerThresholdString);
			} catch (java.lang.NumberFormatException nfe) {
				log.warn("Non-numeric lower threshold in metadata: "
						+ lowerThresholdString);
			}
		} // if
		if (upperThresholdString != null) {
			try {
				upperThreshold = Double.parseDouble(upperThresholdString);
			} catch (java.lang.NumberFormatException nfe) {
				log.warn("Non-numeric upper threshold in metadata: "
						+ upperThresholdString);
			}
		} // if

		DataChannel chan = channelMap.get(channelName);
		chan.setMinThreshold(lowerThreshold);
		chan.setMaxThreshold(upperThreshold);
		chan.setUnit(unit);
	
		
//		int tableNumber = channelTableMap.get(channelName);
//		tableModels.get(tableNumber).addRow(channelName, unit, lowerThreshold,
//				upperThreshold);
//		properties.setProperty("channelTable_" + channelName, Integer
//				.toString(tableNumber));
	}

	public void setMinThresholdMultiple(List<DataChannel> channels, double minThreshold){
	  for(DataChannel channel : channels){
	    channel.setMinThreshold(minThreshold);
	  }
	}
	
  public void setMaxThresholdMultiple(List<DataChannel> channels, double maxThreshold){
    for(DataChannel channel : channels){
      channel.setMaxThreshold(maxThreshold);
    }
  }
	
	private void updateBlankProperties(){
    StringBuilder tableValue = new StringBuilder();
    for(TabularTable table: tables){
      Iterator<Integer> rowIt = table.getBlankRows().iterator();
      while (rowIt.hasNext()){
        if (tableValue.length()>0 && 
            tableValue.charAt(tableValue.length()-1) != ','){
          tableValue.append(',');
        }
        tableValue.append(table.getColumn()).append(',');
        tableValue.append(rowIt.next());
      }
    }
    
    properties.setProperty("channelTable_BLANK", 
                            tableValue.toString());  
  }
	
	private void updateChannelProperties(String chanName){
	  StringBuilder tableValue = new StringBuilder();
	  for(TabularTable table: tables){
	    Iterator<Integer> rowIt = table.getChannelRows(chanName).iterator();
	    while (rowIt.hasNext()){
	      if (tableValue.length()>0 && 
	          tableValue.charAt(tableValue.length()-1) != ','){
	        tableValue.append(',');
	      }
	      tableValue.append(table.getColumn()).append(',');
	      tableValue.append(rowIt.next());
	    }
	  }
	  
    properties.setProperty("channelTable_" + chanName, 
                            tableValue.toString());
	}
	
	@Override
	protected void onSeriesRemoved(VisualizationSeries vs){
	//int tableNumber = channelTableMap.get(channelName);
    Iterator<DataTableModel> it = tableModels.iterator();
    while(it.hasNext()){
      DataTableModel dtm = it.next();
      dtm.deleteRow(vs.getName());
    }
    //tableModels.get(tableNumber).deleteRow(channelName);
    channelMap.remove(vs.getName());
    properties.remove("channelTable_" + vs.getName());
    properties.remove("minThreshold_" + vs.getName());
    properties.remove("maxThreshold_" + vs.getName());
    
	  //return super.removeSeries(vs);
	}
	
	

	@Override
	public void postTime(double time) {
    //super.postTime(time);
    
	}
	
	private void ageData(){
	  double currTime=RBNBController.getInstance().getLocation();
	  
	  // age (clear) rows that haven't seen data in a while
    for (int j = 0; j < columnGroupCount; j++) {
      boolean rowCleared = false;

      DataTableModel tableModel = (DataTableModel) tableModels.get(j);
      for (int k = 0; k < tableModel.getRowCount(); k++) {
        DataRow row = tableModel.getRowAt(k);
        if (row.isCleared()) {
          continue;
        }
        double timestamp = row.getTime();
        if (timestamp <= currTime - timeScale || timestamp > currTime) {
          log.info("tabular clearing data - row: "+ DataViewer.formatDate(timestamp)+", currTime: "+
                                                    DataViewer.formatDate(currTime));
          row.clearData();
          rowCleared = true;
        }
      }

      if (rowCleared) {
        updateAllTables();
        //TODO replace all fireTableDataChanged() with updateAllTables()
        //tableModel.fireTableDataChanged();
      }
    }
	}
	
	@Override
	public void postData(final SubscriptionResponse r) {
    //double currTime=0;
    
    VizSeriesList vsl = getSeries();
    for(VisualizationSeries s:vsl){
      TimeSeriesData tsd=r.getTimeSeries(s);
      if (tsd==null || !tsd.hasData()) {
        //if (subResponse_ == null) {
          //log.debug("Digital panel exiting on null response");
          // no data to display yet
          continue;
      }
      
  		postDataTabular(r,s);
  		this.time=Math.max(tsd.getMaxEndTime(),this.time);
    }
    
    if (state != Player.STATE_MONITORING) {
      lastTimeDisplayed = this.time;
    }
    
    //ageData(this.time);
    ageData();
    
    //for(DataTableModel tableModel : tableModels){
        //tableDataChanged(tableModel);
        updateAllTables();
    //}	
    
    //if(subResponse_!=null && 
    //    tsd.free(this);//{
    //  subResponse_=null;
    //}
	}
	
	private void updateAllTables(){
	  for(TabularTable table : tables){
	    table.fireDataChanged();
	  }
	}

//	private void printChannelMap(ChannelMap m){
//	  double start=System.currentTimeMillis()/1000.0,end=0.0;
//	  for(int i=0;i<m.NumberOfChannels();i++){
//	    double chanStart=m.GetTimeStart(i);
//	    start = Math.min(chanStart,start);
//	    end= Math.max(chanStart+m.GetTimeDuration(i),end);
//	  }
//	  //log.debug("Digital channel map start:"+DataViewer.formatDateSmart(start)+", end:"+DataViewer.formatDateSmart(end));
//	  
//	}
	
	private void postDataTabular(SubscriptionResponse r, VisualizationSeries s) {
	  TimeSeriesData tsd=r.getTimeSeries(s);
	  String channelName = s.getChannels().get(0);
	  int channelIndex = tsd.getChannelIndex(channelName);
	  if (channelIndex < 0) {
	    return;
	  }
	  
		List<Double> times = tsd.getTimes(channelIndex);
		if(times.size()<=0){
		  log.debug("tabular display postData exiting - no data");
		  return;
		}
		
//		// determine what time we should load data from
//		double dataStartTime;
//		if (lastTimeDisplayed == time) {
//			dataStartTime = time - timeScale;
//		} else {
//			dataStartTime = lastTimeDisplayed;
//		}
//
//		for (int i = 0; i < times.length; i++) {
//			if (times[i] > dataStartTime && times[i] <= time) {
//				startIndex = i;
//				break;
//			}
//		}
//
//		// see if there is no data in the time range we are looking at
//		if (startIndex == -1) {
//		  //if(channelIndex==0)
//		    //log.debug("Digital panel exiting - no data in time range");
//			return;
//		}
//
//		int endIndex = startIndex;
//
//		for (int i = times.length - 1; i > startIndex; i--) {
//			if (times[i] <= time) {
//				endIndex = i;
//				break;
//			}
//		}

		DataChannel chan = channelMap.get(channelName);
    if (chan==null){
      log.warn("unable to find channel "+channelName+" in map");
      //System.out.println("unable to find channel "+channelName);
      return;
    }
		
		// do we really need to recompute start and end for each channel?
		// NO! :)
		//if(channelIndex==0)
		  //log.debug("Digital panel start index: "+startIndex+", end:" + endIndex);
		if(r.dropData()){
		  // only show last record
		  int typeID = tsd.getType(channelIndex);
		  Double lastTime=times.get(times.size()-1);
      Number data;

      switch (typeID) {
      case ChannelMap.TYPE_FLOAT64:
        data = tsd.getDataAsFloat64(channelIndex,lastTime);
        break;
      case ChannelMap.TYPE_FLOAT32:
        data = tsd.getDataAsFloat32(channelIndex,lastTime);
        break;
      case ChannelMap.TYPE_INT64:
        data = tsd.getDataAsInt64(channelIndex,lastTime);
        break;
      case ChannelMap.TYPE_INT32:
        data = tsd.getDataAsInt32(channelIndex,lastTime);
        break;
      case ChannelMap.TYPE_INT16:
        data = tsd.getDataAsInt16(channelIndex,lastTime);
        break;
      case ChannelMap.TYPE_INT8:
        data = tsd.getDataAsInt8(channelIndex,lastTime);
        break;
      case ChannelMap.TYPE_STRING:
      case ChannelMap.TYPE_UNKNOWN:
      case ChannelMap.TYPE_BYTEARRAY:
      default:
        return;
      } // switch
       
//      DataChannel chan = channelMap.get(channelName);
      chan.updateData(lastTime, data.doubleValue());
      
//      for (int ii = 0; ii < columnGroupCount; ii++) {
//        ((DataTableModel) tableModels.get(ii)).updateData(channelName,
//            lastTime, data.doubleValue());
//      } // for

		}else{
		  Iterator<Double> it=times.iterator();
		  int typeID = tsd.getType(channelIndex);
		  
  		while(it.hasNext()) {
  		  Double currTime=it.next();
  			Number data;
  
  			switch (typeID) {
  			case ChannelMap.TYPE_FLOAT64:
  				data = tsd.getDataAsFloat64(channelIndex,currTime);
  				break;
  			case ChannelMap.TYPE_FLOAT32:
  				data = tsd.getDataAsFloat32(channelIndex,currTime);
  				break;
  			case ChannelMap.TYPE_INT64:
  				data = tsd.getDataAsInt64(channelIndex,currTime);
  				break;
  			case ChannelMap.TYPE_INT32:
  				data = tsd.getDataAsInt32(channelIndex,currTime);
  				break;
  			case ChannelMap.TYPE_INT16:
  				data = tsd.getDataAsInt16(channelIndex,currTime);
  				break;
  			case ChannelMap.TYPE_INT8:
  				data = tsd.getDataAsInt8(channelIndex,currTime);
  				break;
  			case ChannelMap.TYPE_STRING:
  			case ChannelMap.TYPE_UNKNOWN:
  			case ChannelMap.TYPE_BYTEARRAY:
  			default:
  				return;
  			} // switch
  			
  			chan.updateData(currTime, data.doubleValue());
//  			for (int ii = 0; ii < columnGroupCount; ii++) {
//  				((DataTableModel) tableModels.get(ii)).updateData(channelName,
//  						currTime, data.doubleValue());
//  			} // for
  
  		} // for
	  }
	} // postDataTabular ()

	public List<String> subscribedChannels() {
		List<String> allChannels = new ArrayList<String>();

		for (int i = 0; i < tableModels.size(); i++) {
			DataTableModel tableModel = tableModels.get(i);
			for (int j = 0; j < tableModel.getRowCount(); j++) {
				DataRow dataRow = tableModel.getRowAt(j);
				String name = dataRow.getName();
				if (name.length() == 0) {
				  continue;
					//name = "BLANK " + i;
				}
				allChannels.add(name);
			}
		}

		return allChannels;
	}

	public void setProperty(String key, String value) {
		super.setProperty(key, value);

		if (key != null && value != null) {
			if (key.equals("font")){
			  StringTokenizer st = new StringTokenizer(value,",");
			  String name=st.nextToken();
			  int style=Integer.parseInt(st.nextToken());
			  int size=Integer.parseInt(st.nextToken());
			  setFont(new Font(name,style,size));
			} else if (key.equals("precision")) {
			  try{
  			  setPrecision(Integer.parseInt(value));
			  }catch ( NumberFormatException e){
			    // ignore
			  }
			} else if (key.equals("renderer") && value.equals("engineering")) {
				useEngineeringRenderer(true);
			} else if (key.equals("maxMinVisible") && value.equals("true")) {
				setMaxMinVisible(true);
			} else if (key.equals("thresholdVisible") && value.equals("true")) {
				setThresholdVisible(true);
			} else if (key.equals("numberOfColumns")) {
				int columns = Integer.parseInt(value);
				setNumberOfColumns(columns);
			} else if (key.startsWith("channelTable_")) {
			  String channelName = key.substring(13);
			  StringTokenizer st = new StringTokenizer(value,",");
			  
			  while (st.hasMoreTokens()){
			    int tableIndex=0,row=0;
			    try{
			      tableIndex=Integer.parseInt(st.nextToken());
			    }catch(NumberFormatException nfe){
			      // ignore
			    }
			    try{
			      if(st.hasMoreTokens())
			        row=Integer.parseInt(st.nextToken());
			    }catch(NumberFormatException nfe){
			      // ignore
			    }
			    
			    if (tableIndex >= columnGroupCount) {
			      setNumberOfColumns(tableIndex+ 1);
			    }

			    TabularTable table = tables.get(tableIndex);
			    if(table != null) table.addRow(channelName,row);
			  }
				//int tableIndex = Integer.parseInt(value);
				//channelTableMap.put(channelName, tableIndex);
			} else if (key.startsWith("minThreshold_")) {
				String channelName = key.substring(13);
				try {
					Double.parseDouble(value);
					lowerThresholds.put(channelName, value);
					properties.put(key, value);
				} catch (NumberFormatException e) {
				}
			} else if (key.startsWith("maxThreshold_")) {
				String channelName = key.substring(13);
				try {
					Double.parseDouble(value);
					upperThresholds.put(channelName, value);
					properties.put(key, value);
				} catch (NumberFormatException e) {
				}
			} else if (key.startsWith("useoffset") && value.equals("true")) {
				useOffsetRenderer(true);
			}
		} // if
	} // setProperty ()

	public String toString() {
		return "Tabular Data Panel";
	}

	/** an inner class to implement the data model in this design pattern */
	private class DataTableModel extends AbstractTableModel {
	
	    /** serialization version identifier */
        private static final long serialVersionUID = 1706987603986659555L;
	
		private String[] columnNames = { SENSOR_COL_NAME, VALUE_COL_NAME,
		    UNIT_COL_NAME, MIN_COL_NAME, MAX_COL_NAME, MIN_THRESH_COL_NAME,
		    MAX_THRESH_COL_NAME };

		private ArrayList<DataRow> rows;

		//private boolean cleared;

		private boolean maxMinVisible;

		private boolean thresholdVisible;

		private boolean useOffsets;

		//private final DataRow BLANK_ROW = new DataRow(new String(), null,
		//		(Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY);

		public DataTableModel() {
			super();
			rows = new ArrayList<DataRow>();
//		cleared = true;
			maxMinVisible = false;
			thresholdVisible = false;
			useOffsets = false;
		}

		/**
		 * Moves row at fromIndex to new index, shifting other rows 
		 * around.
		 * @param fromIndex
		 * @param toIndex
		 */
		public void reorder(int fromIndex, int toIndex){
		  int currIndex = fromIndex;
		  while(currIndex != toIndex){
		    int nextIndex = (toIndex > currIndex)?currIndex+1:currIndex-1;
		    Collections.swap(rows, currIndex, nextIndex);
		    currIndex=nextIndex;
		  }
		}
		
		public boolean isCellEditable(int row, int col) {
			return (col == this.findColumn(MIN_THRESH_COL_NAME) || col == this
					.findColumn(MAX_THRESH_COL_NAME));
		} // isCellEditable ()

		public void appendBlankRow(){
		  int rowNum=rows.size();
		  addBlankRow(rowNum);
		}
		
		public void appendRow(String name) {
		  int rowNum=rows.size();
      if(name.isEmpty() || name.toUpperCase().startsWith("BLANK")){
        addBlankRow(rowNum);
      }else{
        addRow(new DataRow(name, rowNum));
      }
    }
		
		public void addRow(String name, int rowNum) {
		  if(name.isEmpty() || name.toUpperCase().startsWith("BLANK")){
		    addBlankRow(rowNum);
		  }else{
		    addRow(new DataRow(name, rowNum));
		  }
		}
		
		public boolean hasChannel(String name){
		  boolean found=false;
      for(int i=0;i<rows.size();i++){
        if(rows.get(i).getName().compareToIgnoreCase(name)==0){
         found=true; 
        }
      }
	   return found; 
		}
		
		public List<Integer> getBlankRows(){
      List<Integer> rowIndexes= new ArrayList<Integer>();
      for(int i=0;i<rows.size();i++){
        if(rows.get(i).isBlank()){
         rowIndexes.add(i); 
        }
      }
      return rowIndexes;
    }
    
		public List<Integer> getChannelRows(String name){
		  List<Integer> rowIndexes= new ArrayList<Integer>();
		  for(int i=0;i<rows.size();i++){
		    if(rows.get(i).getName().compareToIgnoreCase(name)==0){
		     rowIndexes.add(i); 
		    }
		  }
		  return rowIndexes;
		}
		
		public void addBlankRow(int rowNum) {
			addRow(new DataRow("",rowNum));
		}

		public boolean isBlankRow(int row) {
		  DataRow dr = rows.get(row);
		  if(dr == null) return true;
			return (dr.name.isEmpty());
		}

		public void deleteBlankRow(int row) {
			if (isBlankRow(row)) {
				rows.remove(row);
				fireTableRowsDeleted(row, row);
			}
		}

		public void addRow(DataRow dataRow) {
			boolean inserted = false;
		  int row=0;
			for(int i=0;i<rows.size();i++){
		    if(dataRow.compareTo(rows.get(i))<0){
		      rows.add(i, dataRow);
		      row=i;
		      inserted=true;
		      break;
		    }
			}
		  if(!inserted){
		    rows.add(dataRow);
		    row = rows.size() - 1;
		  }
			fireTableRowsInserted(row, row);
		}

		public void deleteRow(String name) {
			for (int i = 0; i < rows.size(); i++) {
				DataRow dataRow = (DataRow) rows.get(i);
				if (dataRow.name.equals(name)) {
					rows.remove(dataRow);
					this.fireTableRowsDeleted(i, i);
					break;
				}
			}
		}

		public void clearData() {
			//cleared = true;
			for (int i = 0; i < rows.size(); i++) {
				DataRow dataRow = (DataRow) rows.get(i);
				dataRow.clearData();
			}
			fireTableDataChanged();
		}

		public void takeOffsets() {
			for (int i = 0; i < rows.size(); i++) {
				DataRow dataRow = (DataRow) rows.get(i);
				dataRow.setOffset(dataRow.getData());
			}
			fireTableDataChanged();
		}

		public void useOffsets(boolean useOffsets) {
			if (this.useOffsets != useOffsets) {
				this.useOffsets = useOffsets;
				fireTableDataChanged();
			}
		}

		/** a method that will get a data row from its index */
		public DataRow getRowAt(int rowdex) {
			return ((DataRow) rows.get(rowdex));
		}

		public int getRowCount() {
			return rows.size();
		}

		public int getColumnCount() {
			int retval = columnNames.length;
			retval -= (maxMinVisible) ? 0 : 2;
			retval -= (thresholdVisible) ? 0 : 2;
			return retval;
		}

		public String getColumnName(int col) {
			if (col < 3 || maxMinVisible) {
				return columnNames[col];
			} else {
				return columnNames[col + 2];
			}
		}

		public Object getValueAt(int row, int col) {
//			if (col != 0 && cleared) {
//				return new String();
//			}

			if (row == -1 || row >=rows.size()) {
				return null;
			}
			
			DataRow dataRow = (DataRow) rows.get(row);

			if (dataRow.isBlank()) {
				return null;
			}

			DataChannel chan=dataRow.getDataChannel();
			if(chan==null)return null;
			
			String[] nameSplit = dataRow.getName().split("/");

			if (col > 2 && !maxMinVisible) {
				col += 2;
			}

			switch (col) {
			case 0:
				return (nameSplit[nameSplit.length - 1]);
			case 1:
				return dataRow.isCleared() ? null : new Double(dataRow
						.getData()
						- (useOffsets ? dataRow.getOffset() : 0));
			case 2:
				return dataRow.getUnit();
			case 3:
				return dataRow.isCleared() ? null : new Double(dataRow
						.getMinimum()
						- (useOffsets ? dataRow.getOffset() : 0));
			case 4:
				return dataRow.isCleared() ? null : new Double(dataRow
						.getMaximum()
						- (useOffsets ? dataRow.getOffset() : 0));
			case 5:
				return (chan.getMinThreshold() == Double.NEGATIVE_INFINITY) ? null
						: new Double(chan.getMinThreshold());
			case 6:
				return (chan.getMaxThreshold() == Double.POSITIVE_INFINITY) ? null
						: new Double(chan.getMaxThreshold());
			default:
				return null;
			}
		}

		/**
		 * Called when the user updates a cell in the table. This is used to let
		 * the user set the thresholds.
		 * 
		 * @param o
		 *            the value entered by the user
		 * @param row
		 *            the row of the cell
		 * @param col
		 *            the column of the cell
		 */
		public void setValueAt(Object o, int row, int col) {
			DataRow dataRow = (DataRow) rows.get(row);
			if (dataRow.isBlank()) {
				return;
			}

			if (o == null) {
				return;
			}
			DataChannel dataChan=dataRow.getDataChannel();
			if(dataChan==null) return;
			
			// adjust the col for optional columns
			if (col > 2 && !maxMinVisible) {
				col += 2;
			}

			String value = o.toString().trim();

			switch (col) {
			case 5: // "Min Thresh"
				try {
					double minThreshold;
					if (value.length() == 0) {
						minThreshold = Double.NEGATIVE_INFINITY;
					} else {
						minThreshold = Double.parseDouble(value);
					}

					if (minThreshold <= dataChan.getMaxThreshold()) {
						dataChan.setMinThreshold(minThreshold);
						properties.setProperty("minThreshold_"
								+ dataRow.getName(), Double
								.toString(minThreshold));
						fireTableCellUpdated(row, col);
					}
				} catch (Throwable e) {
				}
				break;
			case 6: // "Max Thresh"
				try {
					double maxThreshold;
					if (value.length() == 0) {
						maxThreshold = Double.POSITIVE_INFINITY;
					} else {
						maxThreshold = Double.parseDouble(value);
					}

					if (maxThreshold >= dataChan.getMinThreshold()) {
						dataChan.setMaxThreshold(maxThreshold);
						properties.setProperty("maxThreshold_"
								+ dataRow.getName(), Double
								.toString(maxThreshold));
						fireTableCellUpdated(row, col);
					}
				} catch (Throwable e) {
				}
				break;
			}
		}

		public void setValueAtMultiple(Object o, int[] row, int column){
		  for(int i = 0; i < row.length; i++){
		    setValueAt(o, row[i], column);
		  }
		}

//		public boolean getMaxMinVisibile() {
//			return maxMinVisible;
//		}
//
//		public boolean getThresholdVisible() {
//			return thresholdVisible;
//		} // getThresholdVisible ()

		public void setMaxMinVisible(boolean maxMinVisible) {
			if (this.maxMinVisible != maxMinVisible) {
				this.maxMinVisible = maxMinVisible;
				fireTableStructureChanged();
			}
		}

		public void setThresholdVisible(boolean thresholdVisible) {
			if (this.thresholdVisible != thresholdVisible) {
				this.thresholdVisible = thresholdVisible;
				fireTableStructureChanged();
			}
		} // setThresholdVisible ()

	}

	private class DataChannel {
	  private String name;

    private String unit;
    
    private double min;

    private double max;

    private double time;

    private double value;

    private double offset;

    private boolean cleared;

    private double minThresh;

    private double maxThresh;
    
    public DataChannel(String name){
      this.name=name;
      cleared=true;
    }
    
    public void setData(double timestamp, double data, String unit) {
      time = timestamp;
      value = data;
      this.unit = unit;
      min = cleared ? data : Math.min(min, data);
      max = cleared ? data : Math.max(max, data);
      cleared = false;
    }
    
    public void setUnit(String unit){
      this.unit=unit;
    }
    
    public double getValue(){
      return value;
    }
    
    public void setMinThreshold(double thresh){
      minThresh= thresh;
    }
    
    public void setMaxThreshold(double thresh){
      maxThresh= thresh;
    }
    
    public double getMinThreshold(){
      return minThresh;
    }
    
    public double getMaxThreshold(){
      return maxThresh;
    }
    
    public void clearData() {
      cleared = true;
    } 
    
    public boolean isCleared() {
      return cleared;
    }
    
    public double getTime() {
      return time;
    }

    public double getMinimum() {
      return min;
    }

    public double getMaximum() {
      return max;
    }

    public double getOffset() {
      return offset;
    }

    public void setOffset(double offset) {
      this.offset = offset;
    }
    
    public void updateData(double newTime, double newValue){
      String chanUnit=null;  
      Channel channel = RBNBController.getInstance().getChannel(name);
      if (channel != null)
        chanUnit = channel.getUnit();
      setData(newTime,newValue,chanUnit);
    }
	}
	
	private class DataRow implements Comparable {
		String name;

		private int rowNum=0;
		
		//double minThresh;

		//double maxThresh;

		
		public DataRow(String name, int num){
		  this.name=name;
		  this.rowNum=num;
		  //clearData();
		}
		
		public DataChannel getDataChannel(){
		  return channelMap.get(name);
		}
//		public DataRow(String name, String unit, double lowerThresh,
//				double upperThresh) {
//			this.name = name;
////			this.unit = unit;
////			this.minThresh = lowerThresh;
////			this.maxThresh = upperThresh;
//			//clearData();
//		}
		public boolean isBlank(){
		  return (name.isEmpty());
		}
		
		public String getName() {
			return name;
		}

		public String getUnit() {
		  DataChannel chan = channelMap.get(name);
      return (chan != null)?chan.unit:"";
		}

		public double getData() {
		  DataChannel chan = channelMap.get(name);
      return (chan != null)?chan.getValue():0.0;
		}

//		public void setData(double timestamp, double data, String unit) {
//		  DataChannel chan = channelMap.get(name);
//      if(chan != null){
//        chan.setData(timestamp,data,unit);
//      }
//		}

		public void clearData() {
		  DataChannel chan = channelMap.get(name);
      if(chan != null){
        chan.clearData();
      }
    } 

    public boolean isCleared() {
      DataChannel chan = channelMap.get(name);
      return (chan != null)?chan.isCleared():false;
    }
    
    public double getTime() {
      DataChannel chan = channelMap.get(name);
      return (chan != null)?chan.getTime():0.0;
    }

    public double getMinThreshold(){
      DataChannel chan = channelMap.get(name);
      return (chan != null)?chan.getMinThreshold():0.0;
    }
    
    public double getMinimum() {
      DataChannel chan = channelMap.get(name);
      return (chan != null)?chan.getMinimum():0.0;
    }

    public double getMaximum() {
      DataChannel chan = channelMap.get(name);
      return (chan != null)?chan.getMaximum():0.0;
    }

    public double getOffset() {
      DataChannel chan = channelMap.get(name);
      return (chan != null)?chan.getOffset():0.0;
    }

    public void setOffset(double offset) {
      DataChannel chan = channelMap.get(name);
      if(chan != null){
        chan.setOffset(offset);
      }
    }

    @Override
    public int compareTo(Object arg0) {
      if(arg0 instanceof DataRow){
        DataRow otherRow = (DataRow)arg0;
        if(rowNum < otherRow.rowNum ){
          return -1;
        }else if (rowNum > otherRow.rowNum){
          return 1;
        }else{
          return 0;
        }
      }else{
        throw new IllegalArgumentException();
      }
    }

	} // inner class DataRow

	/**
	 * Renderer for non-numeric cells.
	 */
	private class TextTableCellRenderer extends DefaultTableCellRenderer {
    /**  serialization version identifier */
    private static final long serialVersionUID = -5902535152918539920L;

    public TextTableCellRenderer() {
      setHorizontalAlignment(SwingConstants.LEFT);
      setVerticalAlignment(SwingConstants.CENTER);
    }
    
    public Font getFont(){
      return font_;
    }
  } // inner class TextTableCellRenderer

	/**
	 * Renderer for non-sensor data numeric columns (min/max, etc.)
	 */
	private class DoubleTableCellRenderer extends DefaultTableCellRenderer {

    /** serialization version identifier */
    private static final long serialVersionUID = 1835123361189568703L;

		private boolean showEngineeringFormat;

		public DoubleTableCellRenderer() {
			this(false);
		}

		public Font getFont(){
      return font_;
    }
		
		public DoubleTableCellRenderer(boolean showEngineeringFormat) {
			super();

			this.showEngineeringFormat = showEngineeringFormat;
		
			setHorizontalAlignment(SwingConstants.RIGHT);
			setVerticalAlignment(SwingConstants.CENTER);
		}

		public void setValue(Object value) {
			if (value != null && value instanceof Number) {
				if (showEngineeringFormat) {
				  String engFmt = "##0."+zeros(precision_)+"E0";
				  DecimalFormat engineeringFormatter = new DecimalFormat(engFmt);
					setText(engineeringFormatter.format(value));
				} else {
				  String decFmt = "0."+zeros(precision_);
				  DecimalFormat decimalFormatter=new DecimalFormat(decFmt);
					setText(decimalFormatter.format(value));
				}
			} else {
				super.setValue(value);
			}
		}

		public void setShowEngineeringFormat(boolean showEngineeringFormat) {
			this.showEngineeringFormat = showEngineeringFormat;
		}
	} // inner class DoubleTableCellRenderer

	/**
	 * An inner class that renders the sensor data value cell by coloring it when it is
	 * outside or approaching the threshold intervals.
	 */
	private class DataTableCellRenderer extends DoubleTableCellRenderer {

        /** serialization version identifier */
        private static final long serialVersionUID = -2910432979644162805L;

		public DataTableCellRenderer() {
			super();
		}
		

		/**
		 * a method that will color the data value cell based on it's proximity
		 * to some threshold
		 */
		public Component getTableCellRendererComponent(JTable aTable,
				Object aNumberValue, boolean aIsSelected, boolean aHasFocus,
				int aRow, int aColumn) {

			Component renderer = super.getTableCellRendererComponent(aTable,
					aNumberValue, aIsSelected, aHasFocus, aRow, aColumn);

			if (aIsSelected) {
				return renderer;
			} else if (aNumberValue == null
					|| !(aNumberValue instanceof Number)) {
				renderer.setBackground(Color.white);
				renderer.setForeground(Color.black);
				return renderer;
			}

			double numberValue = ((Number) aNumberValue).doubleValue();

			DataRow dataRow = ((DataTableModel) aTable.getModel())
					.getRowAt(aRow);
			DataChannel chan = dataRow.getDataChannel();
			double minThresh = chan.getMinThreshold();
			double maxThresh = chan.getMaxThreshold();

			// if either max or min threshold not set to a value (default
			// infinity), show only critical thresholds (do not show warnings!)
			boolean criticalOnly = (minThresh == Double.NEGATIVE_INFINITY)
					|| (maxThresh == Double.POSITIVE_INFINITY);

			if (criticalOnly) {
				if (numberValue < minThresh || numberValue > maxThresh) {
					renderer.setBackground(Color.red);
					renderer.setForeground(Color.white);
				} else {
					renderer.setBackground(Color.white);
					renderer.setForeground(Color.black);
				}
				return renderer;
			}

			double warningThresh = WARNING_PERCENTAGE * (maxThresh - minThresh);
			double criticalThresh = CRITICAL_PERCENTAGE
					* (maxThresh - minThresh);

			double warningMinThresh = minThresh + warningThresh;
			double criticalMinThresh = minThresh + criticalThresh;

			double warningMaxThresh = maxThresh - warningThresh;
			double criticalMaxThresh = maxThresh - criticalThresh;

			if (numberValue < criticalMinThresh
					|| numberValue > criticalMaxThresh) {
				renderer.setBackground(Color.red);
				renderer.setForeground(Color.white);
			} else if (numberValue < warningMinThresh
					|| numberValue > warningMaxThresh) {
				renderer.setBackground(Color.yellow);
				renderer.setForeground(Color.black);
			} else {
				renderer.setBackground(Color.white);
				renderer.setForeground(Color.black);
			}

			return renderer;
		}
	} // getTableCellRendererComponent ()

	class CheckBoxGroup {
		boolean selected;

		List<AbstractButton> checkBoxes;

		public CheckBoxGroup() {
			this(false);
		}

		public CheckBoxGroup(boolean selected) {
			this.selected = selected;

			checkBoxes = new ArrayList<AbstractButton>();
		}

		public void addCheckBox(AbstractButton checkBox) {
			checkBoxes.add(checkBox);
			checkBox.setSelected(selected);
		}

		public void removeCheckBox(AbstractButton checkBox) {
			checkBoxes.remove(checkBox);
		}

		public void setSelected(boolean selected) {
			this.selected = selected;

			for (AbstractButton checkBox : checkBoxes) {
				checkBox.setSelected(selected);
			}
		}

		public boolean isSelected() {
			return selected;
		}
	}
	
	/**
	 * Key listener moves rows up or down when arrow keys
	 * are pressed.
	 */
	private class TableKeyListener implements KeyListener{
	  private TabularTable table_;
	  public TableKeyListener(TabularTable t){
	    table_=t;
	  }
	  
    @Override
    public void keyPressed(KeyEvent arg0) {
      int selRows[] = table_.getSelectedRows();
      
      if((arg0.getKeyCode()==KeyEvent.VK_DOWN || 
          arg0.getKeyCode()==KeyEvent.VK_UP) &&
          !arg0.isShiftDown() &&
          !arg0.isControlDown() &&
          selRows.length == 1){
        
        if (arg0.getKeyCode()==KeyEvent.VK_DOWN){
          table_.moveRowDown(selRows[0]);
        }else if (arg0.getKeyCode()==KeyEvent.VK_UP){
          table_.moveRowUp(selRows[0]);
        }
      }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {}

    @Override
    public void keyTyped(KeyEvent arg0) {}
	}
    
	/**
	 * Handles drag & drop row reordering via swing. Not being used
	 * because table doesn't seem to act as a drop target - canImport()
	 * not being called.  Cannot mix and match awt and swing drag & drop?
	 */
	private class MyTableRowTransferHandler extends TransferHandler {
	   private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class, 
	                                                     DataFlavor.javaJVMLocalObjectMimeType, 
	                                                     "Integer Row Index");
	   private JTable table = null;

	   public MyTableRowTransferHandler(JTable table) {
	      this.table = table;
	   }

	   @Override
	   protected Transferable createTransferable(JComponent c) {
	      assert (c == table);
	      //System.out.println("selected row: "+table.getSelectedRow());
	      return new DataHandler(new Integer(table.getSelectedRow()), localObjectFlavor.getMimeType());
	   }

	   @Override
	   public boolean canImport(TransferHandler.TransferSupport info) {
	      //System.out.println(info);
	      boolean b = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
	      table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
	      info.setShowDropLocation(b);
	      return b;
	   }

	   @Override
	   public int getSourceActions(JComponent c) {
	      return TransferHandler.COPY_OR_MOVE;
	   }

	   @Override
	   public boolean importData(TransferHandler.TransferSupport info) {
	      JTable target = (JTable) info.getComponent();
	      JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
	      int index = dl.getRow();
	      int max = table.getModel().getRowCount();
	      if (index < 0 || index > max)
	         index = max;
	      
	      target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	      try {
	         Integer rowFrom = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
	         
	         if (rowFrom != -1 && rowFrom != index) {
	            ((DataTableModel)table.getModel()).reorder(rowFrom, index);
	            if (index > rowFrom)
	               index--;
	            target.getSelectionModel().addSelectionInterval(index, index);
	            return true;
	         }
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	      return false;
	   }

	   @Override
	   protected void exportDone(JComponent c, Transferable t, int act) {
	      if (act == TransferHandler.MOVE) {
	         table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	      }
	   }

	}

//	// quick and dirty row drag & drop snippet from 
//	// http://www.velocityreviews.com/forums/t147778-jtable-drag-and-drop-a-row.html
//	private class DragDropRowTableUI extends BasicTableUI {
//	  
//	  private boolean draggingRow = false;
//	  private int startDragPoint;
//	  private int dyOffset;
//	  
//	    protected MouseInputListener createMouseInputListener() {
//	        return new DragDropRowMouseInputHandler();
//	    }
//	    
//	    public void paint(Graphics g, JComponent c) {
//	      super.paint(g, c);
//	      
//	      if (draggingRow) {
//	        g.setColor(table.getParent().getBackground());
//	      Rectangle cellRect = table.getCellRect(table.getSelectedRow(), 0, false);
//	        g.copyArea(cellRect.x, cellRect.y, table.getWidth(), table.getRowHeight(), cellRect.x, dyOffset);
//	        
//	        if (dyOffset < 0) {
//	          g.fillRect(cellRect.x, cellRect.y + (table.getRowHeight() + dyOffset), table.getWidth(), (dyOffset * -1));
//	        } else {
//	          g.fillRect(cellRect.x, cellRect.y, table.getWidth(), dyOffset);
//	        }
//	      }
//	    }
//
//	
//    	private class DragDropRowMouseInputHandler extends MouseInputHandler {
//        
//        public void mousePressed(MouseEvent e) {
//          super.mousePressed(e);
//          startDragPoint = (int)e.getPoint().getY();
//        }
//        
//        public void mouseDragged(MouseEvent e) {
//          int fromRow = table.getSelectedRow();
//          
//          if (fromRow >= 0) {
//            draggingRow = true;
//                      
//            int rowHeight = table.getRowHeight();
//            int middleOfSelectedRow = (rowHeight * fromRow) + (rowHeight / 2);
//            
//            int toRow = -1;
//            int yMousePoint = (int)e.getPoint().getY();
//                      
//            if (yMousePoint < (middleOfSelectedRow - rowHeight)) {
//              // Move row up
//              toRow = fromRow - 1;
//            } else if (yMousePoint > (middleOfSelectedRow + rowHeight)) {
//              // Move row down
//              toRow = fromRow + 1;
//            }
//            
//            if (toRow >= 0 && toRow < table.getRowCount()) {
//              TableModel model = table.getModel();
//              
//            for (int i = 0; i < model.getColumnCount(); i++) {
//              Object fromValue = model.getValueAt(fromRow, i);
//              Object toValue = model.getValueAt(toRow, i);
//              
//              model.setValueAt(toValue, fromRow, i);
//              model.setValueAt(fromValue, toRow, i);
//            }
//            table.setRowSelectionInterval(toRow, toRow);
//            startDragPoint = yMousePoint;
//            }
//            
//            dyOffset = (startDragPoint - yMousePoint) * -1;
//            table.repaint();
//          }
//        }
//        
//        public void mouseReleased(MouseEvent e){
//          super.mouseReleased(e);
//          
//          draggingRow = false;
//          table.repaint();
//        }
//    }
//	}
	
} // class


