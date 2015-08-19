
package org.rdv.ui;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
/**
 * Interface for panels to implement if they are building popup
 * menus dynamically.
 * 
 * @author drewid
 *
 */
public interface PopupMenuBuilder {
  /// Insert items into menu, return index of next item in menu.
  void buildPopupMenu(JPopupMenu menu, MouseEvent e);
}
