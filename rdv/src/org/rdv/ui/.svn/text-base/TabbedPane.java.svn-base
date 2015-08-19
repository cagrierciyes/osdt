package org.rdv.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * A modified JTabbedPane class that has a ghost pane functionality built in.
 * Original class was found at the <a href="http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html"
 * >Java Swing Tips Blog</a>.
 * 
 * @author Unknown, Josh Mattila, Andy Moraczewski
 * 
 */
public class TabbedPane extends JTabbedPane {
  private static final long serialVersionUID = -3087657072850744398L;

  /**
   * Name of the transferable type of a tab.
   */
  private static final String TRANSFERABLE_TYPE = "RDV Tab";

  /**
   * Width of the line that is drawn (while dragging) to show where tabs will be
   * placed when dropped.
   */
  private static final int LINEWIDTH = 3;

  /**
   * Color of the line that is drawn (while dragging) to show where tabs will be
   * placed when dropped.
   */
  private final Color lineColor = new Color(0, 100, 255);

  /**
   * The bounding box of the line that is drawn (while dragging) to show where
   * tabs will be placed when dropped.
   */
  private final Rectangle lineRect = new Rectangle();

  private static int rwh = 20;

  private static int buttonsize = 30; // xxx magic number of scroll

  /**
   * Rectangle that represents the backward button added to JTabbedPane when
   * there are too many tabs to display in the current window.
   */
  private static Rectangle rBackward = new Rectangle();

  /**
   * Rectangle that represents the forward button added to JTabbedPane when
   * there are too many tabs to display in the current window.
   */
  private static Rectangle rForward = new Rectangle();

  /**
   * The glass pane added to allow the ghost tab to show while being dragged.
   */
  private final GhostGlassPane glassPane = new GhostGlassPane();

  /**
   * The index of the tab that is currently being dragged.
   */
  private int dragTabIndex = -1;

  /**
   * If set to true, then a ghosted image of the tab is displayed when dragging
   * and dropping.
   */
  private boolean hasGhost = true;

  /**
   * Popup menu object for all tabs.
   */
  private JPopupMenu popupMenu;

  /**
   * The new tab menu item. When clicked, a new tab should be created.
   */
  private JMenuItem newTab;

  /**
   * The rename tab menu item. When clicked, the tab should allow the user to
   * rename the tab.
   */
  private JMenuItem renameTab;

  /**
   * The close tab menu item. When clicked, the tab should be closed.
   */
  private JMenuItem closeTab;

  /**
   * A list of TabListener objects to notify when tab operations occur (user
   * requests new tab, user renames tab, user closes a tab).
   */
  private List<TabListener> tabListeners;

  public TabbedPane() {
    super();
    tabListeners = new ArrayList<TabListener>();

    final DragSourceListener dsl = new DragSourceListener() {
      public void dragEnter(DragSourceDragEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
      }

      public void dragExit(DragSourceEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
        lineRect.setRect(0, 0, 0, 0);
        glassPane.setPoint(new Point(-1000, -1000));
        glassPane.repaint();
      }

      public void dragOver(DragSourceDragEvent e) {
        Point glassPt = e.getLocation();
        SwingUtilities.convertPointFromScreen(glassPt, glassPane);
        int targetIdx = getTargetTabFromGlass(glassPt);

        // cannot compare glassPane coords to tabbedPane coords
        Rectangle tabAreaBounds = getTabAreaBounds();
        Point tabPt = e.getLocation();
        SwingUtilities.convertPointFromScreen(tabPt, TabbedPane.this);

        if (tabAreaBounds.contains(tabPt) && targetIdx >= 0
            && targetIdx != dragTabIndex && targetIdx != dragTabIndex + 1) {
          e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
          glassPane.setCursor(DragSource.DefaultMoveDrop);
        } else {
          e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
          glassPane.setCursor(DragSource.DefaultMoveNoDrop);
        }
      }

      public void dragDropEnd(DragSourceDropEvent e) {
        lineRect.setRect(0, 0, 0, 0);
        dragTabIndex = -1;
        glassPane.setVisible(false);
        if (hasGhost()) {
          glassPane.setVisible(false);
          glassPane.setImage(null);
        }
      }

      public void dropActionChanged(DragSourceDragEvent e) {
      }
    };

    final Transferable t = new Transferable() {
      private final DataFlavor FLAVOR = new DataFlavor(
          DataFlavor.javaJVMLocalObjectMimeType, TRANSFERABLE_TYPE);

      public Object getTransferData(DataFlavor flavor) {
        return TabbedPane.this;
      }

      public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] f = new DataFlavor[1];
        f[0] = this.FLAVOR;
        return f;
      }

      public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.getHumanPresentableName().equals(TRANSFERABLE_TYPE);
      }
    };

    final DragGestureListener dgl = new DragGestureListener() {
      public void dragGestureRecognized(DragGestureEvent e) {
        if (getTabCount() <= 1)
          return;
        Point tabPt = e.getDragOrigin();
        dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
        // "disabled tab problem".
        if (dragTabIndex < 0 || !isEnabledAt(dragTabIndex))
          return;
        initGlassPane(e.getComponent(), e.getDragOrigin());
        try {
          e.startDrag(DragSource.DefaultMoveDrop, t, dsl);
        } catch (InvalidDnDOperationException idoe) {
          idoe.printStackTrace();
        }
      }
    };

    new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE,
        new CDropTargetListener(), true);
    new DragSource().createDefaultDragGestureRecognizer(this,
        DnDConstants.ACTION_COPY_OR_MOVE, dgl);

    initPopupMenu();
  }

  /**
   * Builds the popup menu object so that it can be linked to the mouse event of
   * right clicking on a tab.
   */
  private void initPopupMenu() {
    this.addMouseListener(new MouseListener() {

      public void mouseClicked(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }

      public void mousePressed(MouseEvent e) {
        // The popup trigger is potentially different on different platforms, so it must be checked
        // on both the mouse press (OS X) and the mouse release (WIN)
        if (e.isPopupTrigger())
          showPopupMenu(e.getX(), e.getY());
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger())
          showPopupMenu(e.getX(), e.getY());
      }
    });

    this.popupMenu = new JPopupMenu();

    this.newTab = new JMenuItem("New Tab");
    newTab.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        addTab(" Tab" + ae.getActionCommand() + " ", null);
        fireAddTabEvent(getTabCount() - 1);
      }
    });
    popupMenu.add(newTab);
    this.renameTab = new JMenuItem("Rename Tab");
    renameTab.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = Integer.parseInt(e.getActionCommand());
        Rectangle r = getBoundsAt(index);

        JPopupMenu rename = new JPopupMenu();
        rename.setOpaque(false);

        JTextField nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(r.width + 10, r.height));
        nameField.setActionCommand(e.getActionCommand());
        nameField.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            JTextField f = (JTextField) e.getSource();
            setTitleAt(Integer.parseInt(e.getActionCommand()), (" " + f.getText() + " "));

            // closes menu
            f.getParent().setVisible(false);
          }
        });

        rename.add(nameField);
        rename.show(TabbedPane.this, r.x, r.y + r.height);
        nameField.requestFocusInWindow();

        fireRenameTabEvent(index);
      }
    });

    popupMenu.add(renameTab);

    popupMenu.addSeparator();
    this.closeTab = new JMenuItem("Close Tab");
    closeTab.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Component c = getComponentAt(Integer.parseInt(e.getActionCommand()));
          removeTabAt(Integer.parseInt(e.getActionCommand()));
          fireRemoveTabEvent(c);
        } catch (NumberFormatException nfe) {
          // Nothin
        }
      }
    });
    popupMenu.add(closeTab);
  }

  /**
   * Called when the right click popup menu should be shown.
   * 
   * @param x
   *          The X location of where to open the menu.
   * @param y
   *          The Y location of where to open the menu.
   */
  private void showPopupMenu(int x, int y) {
    int tab = indexAtLocation(x, y);

    if (tab == -1) {
      renameTab.setEnabled(false);
      closeTab.setEnabled(false);
    } else if (this.getTabCount() == 1) {
      renameTab.setEnabled(true);
      closeTab.setEnabled(false);
    } else {
      closeTab.setEnabled(true);
      renameTab.setEnabled(true);
    }

    closeTab.setActionCommand(String.valueOf(tab));
    renameTab.setActionCommand(String.valueOf(tab));
    newTab.setActionCommand(String.valueOf(this.getTabCount() + 1));
    popupMenu.show(this, x, y);
  }

  /**
   * Generates a click event on the navigation arrows that are usually handled
   * by JTabbedPane.
   * 
   * @param actionKey
   */
  private void clickArrowButton(String actionKey) {
    ActionMap map = getActionMap();
    if (map != null) {
      Action action = map.get(actionKey);
      if (action != null && action.isEnabled()) {
        action.actionPerformed(new ActionEvent(this,
            ActionEvent.ACTION_PERFORMED, null, 0, 0));
      }
    }
  }

  /**
   * Called when dragging a tab to the end of visible tabs. This allows the user
   * to press the tab arrows even when dragging a tab.
   * 
   * @param actionKey
   */
  private void autoScrollTest(Point glassPt) {
    Rectangle r = getTabAreaBounds();
    int tabPlacement = getTabPlacement();
    if (tabPlacement == TOP || tabPlacement == BOTTOM) {
      rBackward.setBounds(r.x, r.y, rwh, r.height);
      rForward.setBounds(r.x + r.width - rwh - buttonsize, r.y, rwh
          + buttonsize, r.height);
    } else if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      rBackward.setBounds(r.x, r.y, r.width, rwh);
      rForward.setBounds(r.x, r.y + r.height - rwh - buttonsize, r.width, rwh
          + buttonsize);
    }
    if (rBackward.contains(glassPt)) {
      // log.debug(new java.util.Date() + "Backward");
      clickArrowButton("scrollTabsBackwardAction");
    } else if (rForward.contains(glassPt)) {
      // log.debug(new java.util.Date() + "Forward");
      clickArrowButton("scrollTabsForwardAction");
    }
  }

  public void setPaintGhost(boolean flag) {
    hasGhost = flag;
  }

  public boolean hasGhost() {
    return hasGhost;
  }

  public boolean addTabListener(TabListener t) {
    return tabListeners.add(t);
  }

  public boolean removeTabListener(TabListener t) {
    return tabListeners.remove(t);
  }

  public void fireAddTabEvent(int index) {
    for (TabListener t : tabListeners) {
      t.tabAdded(index);
    }
  }

  public void fireRenameTabEvent(int index) {
    for (TabListener t : tabListeners) {
      t.tabRenamed(index);
    }
  }

  public void fireRemoveTabEvent(Component c) {
    for (TabListener t : tabListeners) {
      t.tabRemoved(c);
    }
  }

  private int getTargetTabFromGlass(Point glassPt) {
    Point tabPt = SwingUtilities.convertPoint(glassPane, glassPt,
        TabbedPane.this);

    boolean isTB = getTabPlacement() == JTabbedPane.TOP
        || getTabPlacement() == JTabbedPane.BOTTOM;

    for (int i = 0; i < getTabCount(); i++) {
      Rectangle r = getBoundsAt(i);

      if (isTB) {
        r.setRect(r.x - r.width / 2, r.y, r.width, r.height);
      } else {
        r.setRect(r.x, r.y - r.height / 2, r.width, r.height);
      }

      if (r.contains(tabPt))
        return i;
    }

    Rectangle r = getBoundsAt(getTabCount() - 1);
    if (isTB)
      r.setRect(r.x + r.width / 2, r.y, r.width, r.height);
    else
      r.setRect(r.x, r.y + r.height / 2, r.width, r.height);

    return r.contains(tabPt) ? getTabCount() : -1;
  }

  /**
   * Swaps all tab information between two tabs.
   * @param prev
   * @param next
   */
  private void convertTab(int prev, int next) {
    if (next < 0 || prev == next) {
      return;
    }

    Component cmp = getComponentAt(prev);
    Component tab = getTabComponentAt(prev);
    String str = getTitleAt(prev);
    Icon icon = getIconAt(prev);
    String tip = getToolTipTextAt(prev);
    boolean flg = isEnabledAt(prev);
    int tgtindex = prev > next ? next : next - 1;
    remove(prev);

    insertTab(str, icon, cmp, tip, tgtindex);
    setEnabledAt(tgtindex, flg);

    // When you drag'n'drop a disabled tab, it finishes enabled
    // and selected.
    // pointed out by dlorde
    if (flg)
      setSelectedIndex(tgtindex);

    // I have a component in all tabs (jlabel with an X to close
    // the tab) and when i move a tab the component disappear.
    // pointed out by Daniel Dario Morales Salas
    setTabComponentAt(tgtindex, tab);
  }

  private void initTargetLeftRightLine(int next) {
    if (next < 0 || dragTabIndex == next || next - dragTabIndex == 1) {
      lineRect.setRect(0, 0, 0, 0);
    } else if (next == 0) {
      Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0),
          glassPane);
      lineRect.setRect(r.x - LINEWIDTH / 2, r.y, LINEWIDTH, r.height);
    } else {
      Rectangle r = SwingUtilities.convertRectangle(this,
          getBoundsAt(next - 1), glassPane);
      lineRect.setRect(r.x + r.width - LINEWIDTH / 2, r.y, LINEWIDTH, r.height);
    }
  }

  private void initTargetTopBottomLine(int next) {
    if (next < 0 || dragTabIndex == next || next - dragTabIndex == 1) {
      lineRect.setRect(0, 0, 0, 0);
    } else if (next == 0) {
      Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0),
          glassPane);
      lineRect.setRect(r.x, r.y - LINEWIDTH / 2, r.width, LINEWIDTH);
    } else {
      Rectangle r = SwingUtilities.convertRectangle(this,
          getBoundsAt(next - 1), glassPane);
      lineRect.setRect(r.x, r.y + r.height - LINEWIDTH / 2, r.width, LINEWIDTH);
    }
  }

  private void initGlassPane(Component c, Point tabPt) {
    getRootPane().setGlassPane(glassPane);
    if (hasGhost()) {
      Rectangle rect = getBoundsAt(dragTabIndex);
      BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(),
          BufferedImage.TYPE_INT_ARGB);
      Graphics g = image.getGraphics();
      c.paint(g);
      rect.x = rect.x < 0 ? 0 : rect.x;
      rect.y = rect.y < 0 ? 0 : rect.y;
      image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
      glassPane.setImage(image);
    }
    Point glassPt = SwingUtilities.convertPoint(c, tabPt, glassPane);
    glassPane.setPoint(glassPt);
    glassPane.setVisible(true);
  }

  private Rectangle getTabAreaBounds() {
    Rectangle tabbedRect = getBounds();
    Component comp = getSelectedComponent();
    int idx = 0;

    while (comp == null && idx < getTabCount())
      comp = getComponentAt(idx++);

    Rectangle compRect = (comp == null) ? new Rectangle() : comp.getBounds();

    int tabPlacement = getTabPlacement();

    if (tabPlacement == TOP) {
      tabbedRect.height = tabbedRect.height - compRect.height;
    } else if (tabPlacement == BOTTOM) {
      tabbedRect.y = tabbedRect.y + compRect.y + compRect.height;
      tabbedRect.height = tabbedRect.height - compRect.height;
    } else if (tabPlacement == LEFT) {
      tabbedRect.width = tabbedRect.width - compRect.width;
    } else if (tabPlacement == RIGHT) {
      tabbedRect.x = tabbedRect.x + compRect.x + compRect.width;
      tabbedRect.width = tabbedRect.width - compRect.width;
    }

    tabbedRect.grow(2, 2);

    return tabbedRect;
  }

  public class GhostGlassPane extends JPanel {
    private static final long serialVersionUID = 870778099495241294L;

    private final AlphaComposite composite;

    private Point location = new Point(0, 0);

    private BufferedImage draggingGhost = null;

    public GhostGlassPane() {
      setOpaque(false);
      composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
      // http://bugs.sun.com/view_bug.do?bug_id=6700748
      // setCursor(null);
    }

    public void setImage(BufferedImage draggingGhost) {
      this.draggingGhost = draggingGhost;
    }

    public void setPoint(Point location) {
      this.location = location;
    }

    public void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setComposite(composite);

      if (draggingGhost != null) {
        double xx = location.getX() - (draggingGhost.getWidth(this) / 2d);
        double yy = location.getY() - (draggingGhost.getHeight(this) / 2d);
        g2.drawImage(draggingGhost, (int) xx, (int) yy, null);
      }
      if (dragTabIndex >= 0) {
        g2.setPaint(lineColor);
        g2.fill(lineRect);
      }
    }
  }

  class CDropTargetListener implements DropTargetListener {
    public void dragEnter(DropTargetDragEvent e) {
      if (isDragAcceptable(e))
        e.acceptDrag(e.getDropAction());
      else
        e.rejectDrag();
    }

    public void dragExit(DropTargetEvent e) {
    }

    public void dropActionChanged(DropTargetDragEvent e) {
    }

    private Point pt_ = new Point();

    public void dragOver(final DropTargetDragEvent e) {
      Point pt = e.getLocation();
      if (getTabPlacement() == JTabbedPane.TOP
          || getTabPlacement() == JTabbedPane.BOTTOM) {
        initTargetLeftRightLine(getTargetTabFromGlass(pt));
      } else {
        initTargetTopBottomLine(getTargetTabFromGlass(pt));
      }
      if (hasGhost()) {
        glassPane.setPoint(pt);
      }
      if (!pt_.equals(pt))
        glassPane.repaint();
      pt_ = pt;
      autoScrollTest(pt);
    }

    public void drop(DropTargetDropEvent e) {
      if (isDropAcceptable(e)) {
        convertTab(dragTabIndex, getTargetTabFromGlass(e.getLocation()));
        e.dropComplete(true);
      } else {
        e.dropComplete(false);
      }
      repaint();
    }

    public boolean isDragAcceptable(DropTargetDragEvent e) {
      Transferable t = e.getTransferable();
      if (t == null)
        return false;
      DataFlavor[] f = e.getCurrentDataFlavors();
      if (t.isDataFlavorSupported(f[0]) && dragTabIndex >= 0) {
        return true;
      }
      return false;
    }

    public boolean isDropAcceptable(DropTargetDropEvent e) {
      Transferable t = e.getTransferable();
      if (t == null)
        return false;
      DataFlavor[] f = t.getTransferDataFlavors();
      if (t.isDataFlavorSupported(f[0]) && dragTabIndex >= 0) {
        return true;
      }
      return false;
    }
  }
}
