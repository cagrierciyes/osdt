/*
 RDV 2.0 NCGetFileDialog.java
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
import java.io.File;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

public class NCGetFileDialog2 extends JDialog implements ActionListener
{
    private JButton yesButton = null;
    private JButton noButton = null;
    private boolean success = false;
    public boolean isSuccess() { return success; }
	
	public String path;
	public File localFile;
	static MyOwnFocusTraversalPolicy newPolicy;
	
	JTextField field;
	
	public static class MyOwnFocusTraversalPolicy extends FocusTraversalPolicy
    {
        Vector<Component> order;
		
        public MyOwnFocusTraversalPolicy(Vector<Component> order) {
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
	
    public NCGetFileDialog2(JFrame frame)
	{
        super(frame, "Import NEESCentral Data", true);
	
		this.setResizable(false);
		
		yesButton = new JButton("OK");
		yesButton.addActionListener(this);
		
		noButton = new JButton("Cancel");
		noButton.addActionListener(this);
		
		field = new JTextField("", 42);
		
		Vector<Component> order = new Vector<Component>(3);
        order.add(field);
        order.add(yesButton);
        order.add(noButton);
		
		newPolicy = new MyOwnFocusTraversalPolicy(order);
		this.setFocusTraversalPolicy(newPolicy);
		
		field.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pressed");
		
		Action foobar1 = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				yesButton.requestFocus();
			}
		};
		
		field.getActionMap().put("pressed", foobar1);
				
		yesButton.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pressed");
		
		Action foobar3 = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				success = true;
				
				localFile = UIUtilities.saveFile();
				
				if(localFile == null)
				{
					success = false;
					setVisible(false);
					return;
				}
				
				path = field.getText();
				
				setVisible(false);
			}
		};
		
		yesButton.getActionMap().put("pressed", foobar3);
		
		noButton.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pressed");
		
		Action foobar4 = new AbstractAction(){
			public void actionPerformed(ActionEvent e){
				success = false;
				setVisible(false);
			}
		};
		
		noButton.getActionMap().put("pressed", foobar4);
		
		JPanel panel = new JPanel();

		panel.add(field);
		
		panel.setBorder(new TitledBorder("Enter URL or path of file from NEESCentral"));	  
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2, 2, 2));
		buttonPanel.add(yesButton);
		buttonPanel.add(noButton);
		
		JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout());
		panel2.add(panel, BorderLayout.NORTH);
		panel2.add(buttonPanel, BorderLayout.SOUTH);
		
		Container contentPane = getContentPane();
	
		contentPane.add(panel2);
		
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }
	
	
	
    public void actionPerformed(ActionEvent e)
	{
        if(yesButton == e.getSource())
		{
            success = true;
			
			localFile = UIUtilities.saveFile();
			
			if(localFile == null)
			{
				success = false;
				setVisible(false);
				return;
			}
			
			path = field.getText();

            setVisible(false);
        }
        else if(noButton == e.getSource())
		{
            success = false;
            setVisible(false);
        }
    }
}
