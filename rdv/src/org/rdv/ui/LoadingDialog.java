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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/ui/LoadingDialog.java $
 * $Revision: 1149 $
 * $Date: 2008-07-03 10:23:04 -0400 (Thu, 03 Jul 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.ui;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * A dialog to to show that the application is loading with a progress bar.
 * 
 * @author  Jason P. Hanley
 * @since   1.2
 */
public class LoadingDialog extends JDialog {

  /** serialization version identifier */
  private static final long serialVersionUID = 234013397052656084L;

	/**
	 * The owner of the dialog.
	 * 
	 * @since  1.2
	 */
	JFrame frame;
	
	/**
	 * The progress bar to show the indeterminate state.
	 * 
	 * @since  1.2
	 */
	JProgressBar loadingProgressBar;
  
	/**
	 * Construct a dialog box showing a progress bar with no progress.
	 * 
	 * @param frame  the owner of the dialog
	 * @since        1.2
	 */
	public LoadingDialog(JFrame frame) {
		super(frame);
		this.frame = frame;
		
		initComponents();
	}
	
	/**
	 * Create the UI and display it centered on the owner.
	 * 
	 * @since  1.2
	 */
	private void initComponents() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		setTitle("Loading");
		
		Container container = getContentPane();
		container.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 0.5;
		c.weighty = 0.5;		
		c.gridheight = 1;
		c.ipadx = 0;
		c.ipady = 0;
        c.gridy = 0;
		c.gridx = 0;
		c.insets = new java.awt.Insets(5,5,5,5);		

		JLabel messageLabel = new JLabel("Loading data from the server...");
    c.gridwidth = 2;
		c.insets = new java.awt.Insets(5,5,5,5);
		container.add(messageLabel, c);
		
		loadingProgressBar = new JProgressBar(0, 1);
		loadingProgressBar.setPreferredSize(new Dimension(250, 17));
    c.gridwidth = 1;
    c.weightx = 1;
		c.gridy = 1;
		c.insets = new java.awt.Insets(0,5,5,5);
		container.add(loadingProgressBar, c);
	}
  
  /**
   * Control the visibility of the dialog.
   * 
   * @param visible  if true make the dialog visible, if false make it invisible
   */
  public void setVisible(boolean visible) {
    if (isVisible() == visible) {
      return;
    }
    
    if (visible) {
      pack();
      centerOnOwner();      
    }
    
    super.setVisible(visible);
  }
  
	/**
	 * Center the dialog box on the owner frame.
	 *
	 * @since  1.2
	 */
	private void centerOnOwner() {
		int frameX = frame.getX();
		int frameY = frame.getY();
		int frameWidth = frame.getWidth();
		int frameHeight = frame.getHeight();
		int dialogWidth = getWidth();
		int dialogHeight = getHeight();
		
		int dialogX = frameX + (frameWidth/2) - (dialogWidth/2);
		int dialogY = frameY + (frameHeight/2) - (dialogHeight/2);
		setLocation(dialogX, dialogY);
	}
	
	/**
	 * Set the progress bar to indeterminate.
	 * 
	 * @since  1.2
	 */
	public void start() {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		loadingProgressBar.setIndeterminate(true);
	}
	
	/**
	 * Set the progress bar to 0 progress.
	 *
	 * @since  1.2
	 */
	public void stop() {
    setCursor(null);
		loadingProgressBar.setIndeterminate(false);
	}
	
	/**
	 * Hide the dialog and dispose of it.
	 * 
	 * @since  1.2
	 */
	public void close() {
		setVisible(false);
    
    stop();
    
		dispose();
	}
}