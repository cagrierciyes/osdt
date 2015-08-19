/*
 RDV 2.0 LoginDialog.java
 Author: Charles Cowart
 Date: 02/2010
 */

package org.rdv.ui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

public class LoginDialog extends JDialog implements ActionListener
{
    private JPanel myPanel = null;
    private JButton yesButton = null;
    private JButton noButton = null;

	public String name, password;
	
	private JTextField f1;
	private JPasswordField f2;
	static MyFocusTraversalPolicy newPolicy;
	
	public static class MyFocusTraversalPolicy extends FocusTraversalPolicy
    {
        Vector<Component> order;
		
        public MyFocusTraversalPolicy(Vector<Component> order) {
            this.order = new Vector<Component>(order.size());
            this.order.addAll(order);
        }
        public Component getComponentAfter(Container focusCycleRoot,
                                           Component aComponent)
        {
            int idx = (order.indexOf(aComponent) + 1) % order.size();
            return order.get(idx);
        }
		
        public Component getComponentBefore(Container focusCycleRoot,
                                            Component aComponent)
        {
            int idx = order.indexOf(aComponent) - 1;
            if (idx < 0) {
                idx = order.size() - 1;
            }
            return order.get(idx);
        }
		
        public Component getDefaultComponent(Container focusCycleRoot) {
            return order.get(0);
        }
		
        public Component getLastComponent(Container focusCycleRoot) {
            return order.lastElement();
        }
		
        public Component getFirstComponent(Container focusCycleRoot) {
            return order.get(0);
        }
    }

	
    public LoginDialog(JFrame frame)
	{
        super(frame, "Enter Login Information", true);
		

	
		this.setResizable(false);
		
		yesButton = new JButton("OK");
		yesButton.addActionListener(this);
		
		noButton = new JButton("Cancel");
		noButton.addActionListener(this);
		
		 f1 = new JTextField("", 20);
		 f2 = new JPasswordField("", 20);
		
		Vector<Component> order = new Vector<Component>(4);
        order.add(f1);
        order.add(f2);
        order.add(yesButton);
        order.add(noButton);
		
        newPolicy = new MyFocusTraversalPolicy(order);
		this.setFocusTraversalPolicy(newPolicy);
		
		f1.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pressed");
		
		Action foobar1 = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				f2.requestFocus();
			}
		};
		
		f1.getActionMap().put("pressed", foobar1);
		
		f2.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pressed");
		
		Action foobar2 = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				yesButton.requestFocus();
			}
		};
		
		f2.getActionMap().put("pressed", foobar2);
		
		yesButton.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pressed");
		
		Action foobar3 = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				name = f1.getText();
				password = new String(f2.getPassword());
				
				setVisible(false);
			}
		};
		
		yesButton.getActionMap().put("pressed", foobar3);
		
		noButton.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pressed");
		
		Action foobar4 = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				name = null;
				password = null;
				
				setVisible(false);
			}
		};
		
		noButton.getActionMap().put("pressed", foobar4);
		

		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		
		panel1.add(f1);
		panel2.add(f2);
		
		panel1.setBorder(new TitledBorder("User Name"));
		panel2.setBorder(new TitledBorder("Password"));
	  
		JPanel panel0 = new JPanel();
		panel0.setLayout(new BoxLayout(panel0, BoxLayout.Y_AXIS));
		
		panel0.add(panel1);
		panel0.add(panel2);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2, 2, 2));
		buttonPanel.add(yesButton);
		buttonPanel.add(noButton);
		
		
		JPanel panelX = new JPanel();
        panelX.setLayout(new BorderLayout());
        panelX.add(panel0, BorderLayout.NORTH);
        panelX.add(buttonPanel, BorderLayout.SOUTH);
		
		Container contentPane = getContentPane();
		contentPane.add(panelX);
		
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }
	
	public boolean isSuccess()
	{
		if(name == null)
		{
			return false;
		}
		
		return true;
	}
	
    public void actionPerformed(ActionEvent e)
	{
        if(yesButton == e.getSource()) 
		{
			name = f1.getText();
			password = new String(f2.getPassword());
			
            setVisible(false);
        }
        else if(noButton == e.getSource())
		{
			name = null;
			password = null;
			
            setVisible(false);
        }
    }
}
