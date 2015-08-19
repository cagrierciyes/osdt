
package org.rdv.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.text.NumberFormatter;

public class AdjustThresholdDialog extends JDialog {
  public int maxFractionDigits = 6;
  protected Double adjustedThreshold = null;
  protected JFormattedTextField thresholdField;
  protected DecimalFormat floatFormat;
  
  public AdjustThresholdDialog(JFrame mainFrame){
    super(mainFrame, "Threshold Adjustment", true);
    Container cp = getContentPane();
    
    Panel top = new Panel();
    top.setLayout(new FlowLayout());
    
    floatFormat = new DecimalFormat();
    floatFormat.setMaximumFractionDigits(maxFractionDigits);
    thresholdField = new JFormattedTextField(floatFormat);
    thresholdField.setValue(null);
    thresholdField.setColumns(10);
    thresholdField.addKeyListener(new KeyListener(){
      @Override
      public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
        case KeyEvent.VK_ESCAPE:
          dispose();
          setVisible(false);
          break;
        case KeyEvent.VK_ENTER:
          try {
            thresholdField.commitEdit();
            adjustedThreshold = ((Number)thresholdField.getValue()).doubleValue();
            dispose();
            setVisible(false);
          }
          catch (ParseException ex) {ex.printStackTrace();}
          break;
        default:
          break;
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {}
      @Override
      public void keyTyped(KeyEvent e) {}
    });
        
    top.add(thresholdField);
    cp.add(top, BorderLayout.NORTH);
    
    Panel bot = new Panel();
    bot.setLayout(new FlowLayout());
    
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        if(thresholdField.getValue() == null) return;
        adjustedThreshold = ((Number)thresholdField.getValue()).doubleValue();
        dispose();
        setVisible(false);
      }
    });
    
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        dispose();
        setVisible(false);
      }
    });
    

    bot.add(okButton);
    bot.add(cancelButton);
    cp.add(bot, BorderLayout.SOUTH);
    
    pack();
    setLocation(100,100);
  }
  
  public Double getAdjustedThreshold(){
    return adjustedThreshold;
  }
}
