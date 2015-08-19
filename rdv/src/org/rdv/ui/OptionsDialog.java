package org.rdv.ui;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jdesktop.application.Application;
import org.rdv.rbnb.RBNBController;

import com.cloudgarden.layout.AnchorConstraint;
import com.cloudgarden.layout.AnchorLayout;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/

/**
 * Dialog to set RDV GUI options such as the display refresh 
 * rate.
 */
public class OptionsDialog extends javax.swing.JDialog implements WindowListener {
	private JTextField updateRateTxt;
	private JPanel updateRatePanel;
	private JTextArea jTextArea2;
	private JTextArea jTextArea1;
	private int displayUpdateRate;

	/**
	* Auto-generated main method to display this JDialog
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame();
				OptionsDialog inst = new OptionsDialog(frame);
				inst.setVisible(true);
			}
		});
	}
	
	/**
	 * Create the dialog window.
	 * @param frame Parent window frame.
	 */
	public OptionsDialog(JFrame frame) {
		super(frame,"Options");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initGUI();
		addWindowListener(this);
	}
	
	private void initGUI() {
		try {
			AnchorLayout thisLayout = new AnchorLayout();
			getContentPane().setLayout(thisLayout);
			this.setName("Options");
			{
				updateRatePanel = new JPanel();
				getContentPane().add(updateRatePanel, new AnchorConstraint(337, 734, 615, 125, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
				updateRatePanel.setPreferredSize(new java.awt.Dimension(187, 30));
				updateRatePanel.setLayout(null);
				updateRatePanel.setName("updateRatePanel");
				{
					updateRateTxt = new JTextField();
					updateRatePanel.add(updateRateTxt);
					updateRateTxt.setName("updateRateTxt");
					updateRateTxt.setBounds(118, 4, 35, 21);
				}
				{
					jTextArea2 = new JTextArea();
					updateRatePanel.add(jTextArea2);
					jTextArea2.setName("jTextArea2");
					jTextArea2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
					jTextArea2.setBounds(160, 6, 14, 18);
				}
				{
					jTextArea1 = new JTextArea();
					updateRatePanel.add(jTextArea1);
					jTextArea1.setName("jTextArea1");
					jTextArea1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
					jTextArea1.setBounds(7, 6, 105, 18);
				}
			}
			this.setSize(301, 135);
			Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(getContentPane());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

  @Override
  public void windowActivated(WindowEvent e) {
    // TODO Auto-generated method stub
    
  }

  @Override
  /**
   * Set the program options from the values in the dialog.
   */
  public void windowClosed(WindowEvent e) {
    RBNBController.PLAYBACK_REFRESH_RATE=Double.parseDouble(updateRateTxt.getText());
  }

  @Override
  public void windowClosing(WindowEvent e) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void windowDeactivated(WindowEvent e) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void windowDeiconified(WindowEvent e) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void windowIconified(WindowEvent e) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void windowOpened(WindowEvent e) {
    updateRateTxt.setText(Double.toString(RBNBController.PLAYBACK_REFRESH_RATE));
  }

}
