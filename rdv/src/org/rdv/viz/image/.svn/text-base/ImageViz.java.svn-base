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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/viz/image/ImageViz.java $
 * $Revision: 1307 $
 * $Date: 2008-12-01 12:54:10 -0500 (Mon, 01 Dec 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.viz.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;
import org.rdv.Extension;
import org.rdv.ExtensionManager;
import org.rdv.auth.Authentication;
import org.rdv.auth.AuthenticationEvent;
import org.rdv.auth.AuthenticationListener;
import org.rdv.auth.AuthenticationManager;
import org.rdv.data.Channel;
import org.rdv.data.ChannelSeries;
import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;
import org.rdv.datapanel.AbstractDataPanel;
import org.rdv.rbnb.Player;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBDataRequest;
import org.rdv.rbnb.RBNBTime;
import org.rdv.rbnb.SubscriptionRequest;
import org.rdv.rbnb.SubscriptionResponse;
import org.rdv.rbnb.TimeSeriesData;
import org.rdv.ui.UIUtilities;
import org.rdv.ui.RDVIcon;

import com.rbnb.sapi.ChannelMap;

/**
 * A visualization extension for viewing images.
 * 
 * @author Jason P. Hanley
 * @author Drew Daugherty
 */
public class ImageViz extends AbstractDataPanel implements AuthenticationListener {
  
  /** the logger for this class */
  static Log log = LogFactory.getLog(ImageViz.class.getName());
  
  /** the data panel property for the scale */
  private static final String DATA_PANEL_PROPERTY_SCALE = "scale";
  
  /** the data panel property for the origin */
  private static final String DATA_PANEL_PROPERTY_ORIGIN = "origin";
  
  /** the data panel property for the navigation panel visibility */
  private static final String DATA_PANEL_PROPERTY_SHOW_NAVIGATION_IMAGE = "showNavigationImage";
  
  /** the data panel property for filmstrip mode */
  private static final String DATA_PANEL_PROPERTY_FILMSTRIP_MODE = "filmstripMode";
  
  /** the data panel property for the maximum number of images to display in filmstrip mode */
  private static final String DATA_PANEL_PROPERTY_MAXIMUM_FILMSTRIP_IMAGES = "maximumFilmstripImages";
  
  /** the data panel property for the usage of the thumbnail images */
  private static final String DATA_PANEL_PROPERTY_USE_THUMBNAIL_IMAGE = "useThumbnailImage";
  
  /** the data panel property for the robotic controls visibility */
  private static final String DATA_PANEL_PROPERTY_HIDE_ROBOTIC_CONTROLS = "hideRoboticControls";

  /** the main panel */
  private JPanel panel;
  
  /** the panel to display the image */
  private ImagePanel imagePanel;
  
  /** the panel to display images as a filmstrip */
  private FilmstripPanel filmstripPanel;

  // the panels for the robotic controls
  private JPanel topControls;
  private JPanel irisControls;
  private JPanel focusControls;  
  private JPanel zoomControls;
  private JPanel tiltControls;
  private JPanel panControls;
  
  // menu items for the popup menu
  //private JCheckBoxMenuItem autoScaleMenuItem;
  //private JCheckBoxMenuItem showNavigationImageMenuItem;
  //private JCheckBoxMenuItem hideRoboticControlsMenuItem;
//  private boolean autoScale_=true;
//  private boolean showNavigation_=false;
  private boolean hideRoboticControls_=true;
  
  /** the group of radio buttons for the maximum filmstrip images */
  private ButtonGroup maximumFilmstripImagesButtonGroup;

  /** the mouse click listener for robotic control */
  MouseInputAdapter roboticMouseClickListener;
  
  /** a flag to enabled/disable filmstrip mode */
  private boolean filmstripMode;
  
  /** the values for the maximum number of filmstrip image */
  // 2, 3, 4, 5, 6, 8, 9, 10, 12, 15, 16, 18, 20
  private static final int[] MAXIMUM_FILMSTRIP_IMAGES_VALUES = new int[] { 2, 3, 4, 5, 6, 8, 9, 10, 12, 15, 16, 18, 20, 25};
  
  /** a flag to enable/disable usage of a thumbnail image */
  private boolean useThumbnailImage;
  
  /** the name of the thumbnail plugin */
  private static final String THUMBNAIL_PLUGIN_NAME = "_Thumbnails";
 	
  /** the time for the currently displayed image */
  private double displayedImageTime;
 	
  /** the data for the currently displayed image */
  private byte[] displayedImageData;
  
  /** the flexTPS stream object for the current channel */
  private FlexTPSStream flexTPSStream;
 	 	
  public ImageViz() {
    super();
    
    filmstripMode = false;
    useThumbnailImage = false;
    
    displayedImageTime = -1;
    
    initUI();
    
    setDataComponent(panel);
  }
  
  public SubscriptionRequest getSubscriptionRequest(){
    return new RBNBDataRequest(this,
        SubscriptionRequest.TYPE_IMAGE,0,getSeries());
  }
  
  
  @Override
  protected void setDataComponent(JComponent dataComponent) {
    super.setDataComponent(dataComponent);
//  public void openPanel(final DataPanelManager dataPanelManager) {
//    super.openPanel(dataPanelManager);
//    
    AuthenticationManager.getInstance().addAuthenticationListener(this);
  }
  
  public void closePanel() {
    super.closePanel();
    
    AuthenticationManager.getInstance().removeAuthenticationListener(this);
  }
  
  private void initUI() {
    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    
    initImagePanel();
    
    initFilmstripPanel();
    
    initTopControlsPanel();
    
    initIrisControls();   
    initFocusControls();
    initZoomControls();
    initTiltControls();
    initPanControls();
    
    initRoboticMouseClickListener();

    // set mouselisteners to trigger popup menu
    imagePanel.addMouseListener(getPopupMenuMouseListener());
    filmstripPanel.addMouseListener(getPopupMenuMouseListener());
    panel.addMouseListener(getPopupMenuMouseListener());
  }
			
  private void initImagePanel() {
    imagePanel = new ImagePanel();
    imagePanel.setBackground(Color.black);
    imagePanel.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
        String propertyName = pce.getPropertyName();
        if (propertyName.equals(ImagePanel.AUTO_SCALING_PROPERTY)) {
          boolean autoScale = (Boolean)pce.getNewValue();
          if (autoScale) {
            properties.remove(DATA_PANEL_PROPERTY_SCALE);
            properties.remove(DATA_PANEL_PROPERTY_ORIGIN);
          } else {
            properties.setProperty(DATA_PANEL_PROPERTY_SCALE, Double.toString(imagePanel.getScale()));
            properties.setProperty(DATA_PANEL_PROPERTY_ORIGIN, pointToString(imagePanel.getOrigin()));
          }
        } else if (propertyName.equals(ImagePanel.SCALE_PROPERTY) && !imagePanel.isAutoScaling()) {
          properties.setProperty(DATA_PANEL_PROPERTY_SCALE, pce.getNewValue().toString());
        } else if (propertyName.equals(ImagePanel.ORIGIN_PROPERTY) && !imagePanel.isAutoScaling()) {
          Point origin = (Point)pce.getNewValue();
          String originString = pointToString(origin);
          properties.setProperty(DATA_PANEL_PROPERTY_ORIGIN, originString);
        } else if (propertyName.equals(ImagePanel.NAVIGATION_IMAGE_ENABLED_PROPERTY)) {
          boolean showNavigation = (Boolean)pce.getNewValue();
          if (showNavigation) {
            properties.setProperty(DATA_PANEL_PROPERTY_SHOW_NAVIGATION_IMAGE, "true");
          } else {
            properties.remove(DATA_PANEL_PROPERTY_SHOW_NAVIGATION_IMAGE);
          }
        }
      }
    });
    
    panel.add(imagePanel, BorderLayout.CENTER);
  }
  
  private void initFilmstripPanel() {
    filmstripPanel = new FilmstripPanel();
    filmstripPanel.setBackground(Color.black);
    filmstripPanel.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
        String propertyName = pce.getPropertyName();
        if (propertyName.equals(FilmstripPanel.MAXIMUM_IMAGES_PROPERTY)) {
          if (filmstripPanel.getMaximumImages() != FilmstripPanel.MAXIMUM_IMAGES_DEFAULT) {
            properties.setProperty(DATA_PANEL_PROPERTY_MAXIMUM_FILMSTRIP_IMAGES, pce.getNewValue().toString());
          } else {
            properties.remove(DATA_PANEL_PROPERTY_MAXIMUM_FILMSTRIP_IMAGES);
          }
          updateMaximumFilmstripImagesRadioButtons();
        }
      }      
    });
    
    filmstripPanel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
          setFilmstripMode(false);
        }
      }
    });
  }
  
  private void initTopControlsPanel() {
    topControls = new JPanel();
    topControls.setLayout(new BorderLayout());
    panel.add(topControls, BorderLayout.NORTH);
  }
  
  private void initIrisControls() {
    irisControls = new JPanel();
    irisControls.setLayout(new BorderLayout());
    
    JButton closeIrisButton = new JButton(RDVIcon.roboticControlClose);
    closeIrisButton.setBorder(null);
    closeIrisButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        irisClose();
      }
    });
    irisControls.add(closeIrisButton, BorderLayout.WEST);
    
    JButton irisControlButton = new StrechIconButton(RDVIcon.roboticControlIrisbarMedium);
    irisControlButton.setBorder(null);
    irisControlButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        JButton button = (JButton)e.getComponent();
        int iris = Math.round(100f*e.getPoint().x/button.getWidth());
        iris(iris);
      }
    });    
    irisControls.add(irisControlButton, BorderLayout.CENTER);
    
    JPanel irisControlsRight = new JPanel();
    irisControlsRight.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    
    JButton openIrisButton = new JButton(RDVIcon.roboticControlOpen);
    openIrisButton.setBorder(null);
    openIrisButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        irisOpen();
      }
    });
    irisControlsRight.add(openIrisButton);
    
    JButton autoIrisButton = new JButton(RDVIcon.roboticControlAuto);
    autoIrisButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        irisAuto();
      }
    });
    autoIrisButton.setBorder(null);
    irisControlsRight.add(autoIrisButton);
    
    irisControls.add(irisControlsRight, BorderLayout.EAST);
  }
  
  private void initFocusControls() {
    focusControls = new JPanel();
    focusControls.setLayout(new BorderLayout());
    
    JButton nearFocusButton = new JButton(RDVIcon.roboticControlNear);
    nearFocusButton.setBorder(null);
    nearFocusButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        focusNear(); 
      }
    });
    focusControls.add(nearFocusButton, BorderLayout.WEST);
    
    JButton focusControlButton = new StrechIconButton(RDVIcon.roboticControlFocusbarMedium);
    focusControlButton.setBorder(null);
    focusControlButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        JButton button = (JButton)e.getComponent();
        int focus = Math.round(100f*e.getPoint().x/button.getWidth());
        focus(focus);
      }
    });

    focusControls.add(focusControlButton, BorderLayout.CENTER);
    
    JPanel focusControlsRight = new JPanel();
    focusControlsRight.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

    JButton farFocusButton = new JButton(RDVIcon.roboticControlFar);
    farFocusButton.setBorder(null);
    farFocusButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        focusFar();
      }
    });
    focusControlsRight.add(farFocusButton);    

    JButton autoFocusButton = new JButton(RDVIcon.roboticControlAuto);
    autoFocusButton.setBorder(null);
    autoFocusButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        focusAuto();
      }
    });
    focusControlsRight.add(autoFocusButton);
    
    focusControls.add(focusControlsRight, BorderLayout.EAST);
  }
  
  private void initZoomControls() {
    zoomControls = new JPanel();
    zoomControls.setLayout(new BorderLayout());
    
    JButton zoomOutButton = new JButton(RDVIcon.roboticControlMinus);
    zoomOutButton.setBorder(null);
    zoomOutButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        zoomOut();
      }
    });    
    zoomControls.add(zoomOutButton, BorderLayout.WEST);
        
    JButton zoomControlButton = new StrechIconButton(RDVIcon.roboticControlZoombarMedium);
    zoomControlButton.setBorder(null);
    zoomControlButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        JButton button = (JButton)e.getComponent();
        int zoom = Math.round(100f*e.getPoint().x/button.getWidth());
        zoom(zoom);
      }
    });
    zoomControls.add(zoomControlButton, BorderLayout.CENTER);

    JPanel topRightControls = new JPanel();
    topRightControls.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));        
    
    JButton zoomInButton = new JButton(RDVIcon.roboticControlPlus);
    zoomInButton.setBorder(null);
    zoomInButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        zoomIn();
      }
    });    
    topRightControls.add(zoomInButton);
    
    JButton topHomeButton = new JButton(RDVIcon.roboticControlHome);
    topHomeButton.setBorder(null);
    topHomeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        home();
      }
    });
    topRightControls.add(topHomeButton);    
    
    zoomControls.add(topRightControls, BorderLayout.EAST);    
  }
  
  private void initTiltControls() {
    tiltControls = new JPanel();
    tiltControls.setLayout(new BorderLayout());
    
    JButton tiltUpButton = new JButton(RDVIcon.roboticControlUp);
    tiltUpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        tiltUp();
      }      
    });
    tiltUpButton.setBorder(null);
    tiltControls.add(tiltUpButton, BorderLayout.NORTH);
    
    JButton tiltControlButton = new StrechIconButton(RDVIcon.roboticControlTiltbarMedium);
    tiltControlButton.setBorder(null);
    tiltControlButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        JButton button = (JButton)e.getComponent();
        int tilt = Math.round(100f*e.getPoint().y/button.getHeight());
        tilt(tilt);
      }
    });    
    tiltControls.add(tiltControlButton, BorderLayout.CENTER);
    
    JButton tiltDownButton = new JButton(RDVIcon.roboticControlDown);
    tiltDownButton.setBorder(null);
    tiltDownButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        tiltDown();
      }
    });
    tiltControls.add(tiltDownButton, BorderLayout.SOUTH);    
  }
  
  private void initPanControls() {
    panControls = new JPanel();
    panControls.setLayout(new BorderLayout());
    
    JButton panLeftButton = new JButton(RDVIcon.roboticControlLeft);
    panLeftButton.setBorder(null);
    panLeftButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        panLeft();
      }
    });
    panControls.add(panLeftButton, BorderLayout.WEST);
    
    JButton panControlButton = new StrechIconButton(RDVIcon.roboticControlPanbarMedium);
    panControlButton.setBorder(null);
    panControlButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        JButton button = (JButton)e.getComponent();
        int pan = Math.round(100f*e.getPoint().x/button.getWidth());
        pan(pan);
      }
    });    
    panControls.add(panControlButton, BorderLayout.CENTER);
    
    JPanel bottomRightControls = new JPanel();
    bottomRightControls.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    
    JButton panRightButton = new JButton(RDVIcon.roboticControlRight);
    panRightButton.setBorder(null);
    panRightButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        panRight();
      }
    });    
    bottomRightControls.add(panRightButton);
    
    JButton bottomHomeButton = new JButton(RDVIcon.roboticControlHome);
    bottomHomeButton.setBorder(null);
    bottomHomeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        home();
      }
    });    
    bottomRightControls.add(bottomHomeButton);    
    
    panControls.add(bottomRightControls, BorderLayout.EAST);    
  }

  private void initRoboticMouseClickListener() {
    roboticMouseClickListener = new MouseInputAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          Point imagePoint = imagePanel.panelToScaledImagePoint(e.getPoint());
          if (imagePoint != null) {
            center(imagePoint);
          }
        }
      }   
    };    
  }

  public void buildPopupMenu(JPopupMenu menu, MouseEvent e){
    super.buildPopupMenu(menu, e);
    
    // create a popup to copy an image to the clipboard
    final JMenuItem copyImageMenuItem = new JMenuItem("Copy");
    copyImageMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        copyImage();
      }
    });
    menu.add(copyImageMenuItem);

    menu.addSeparator();

    // create a popup to save an image
    final JMenuItem saveImageMenuItem = new JMenuItem("Save as...");
    saveImageMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        saveImage();
      }
    });
    menu.add(saveImageMenuItem);    

    menu.addSeparator();

    // create a popup to copy an image to the clipboard
    final JMenuItem printImageMenuItem = new JMenuItem("Print...");
    printImageMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        printImage();
      }
    });
    menu.add(printImageMenuItem);
    
    menu.addSeparator();
    
    final JMenuItem zoomInMenuItem = new JMenuItem("Zoom in");
    zoomInMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        imagePanel.zoomIn();
      }
    });
    menu.add(zoomInMenuItem);
    
    final JMenuItem zoomOutMenuItem = new JMenuItem("Zoom out");
    zoomOutMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        imagePanel.zoomOut();
      }
    });
    menu.add(zoomOutMenuItem);

    final JSeparator zoomMenuSeparator = new JPopupMenu.Separator();
    menu.add(zoomMenuSeparator);

    final JCheckBoxMenuItem autoScaleMenuItem = new  JCheckBoxMenuItem(
                                          "Auto scale", imagePanel.isAutoScaling());
    autoScaleMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setAutoScale(autoScaleMenuItem.isSelected());
      }      
    });
    menu.add(autoScaleMenuItem);
    
    final JMenuItem resetScaleMenuItem = new JMenuItem("Reset scale");
    resetScaleMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        imagePanel.setScale(1);
      }
    });
    menu.add(resetScaleMenuItem);
    
    final JCheckBoxMenuItem showNavigationImageMenuItem = new JCheckBoxMenuItem(
                            "Show navigation image", imagePanel.isNavigationImageEnabled());
    showNavigationImageMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        setShowNavigationImage(showNavigationImageMenuItem.isSelected());
      }
    });
    menu.add(showNavigationImageMenuItem);
    
    final JMenu maximumFilmstripImagesMenu = new JMenu("Maximum number of images");
    maximumFilmstripImagesButtonGroup = new ButtonGroup();
    for (final int maximumFilmstripImagesValue : MAXIMUM_FILMSTRIP_IMAGES_VALUES) {
      boolean selected = filmstripPanel.getMaximumImages() == maximumFilmstripImagesValue;
      JMenuItem maximumFilmStripImagesMenuItem = new JRadioButtonMenuItem(
                                    Integer.toString(maximumFilmstripImagesValue), selected);
      maximumFilmStripImagesMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          filmstripPanel.setMaximumImages(maximumFilmstripImagesValue);
        }        
      });
      maximumFilmstripImagesButtonGroup.add(maximumFilmStripImagesMenuItem);
      maximumFilmstripImagesMenu.add(maximumFilmStripImagesMenuItem);
    }
    menu.add(maximumFilmstripImagesMenu);
    
    menu.addSeparator();
    
    final JCheckBoxMenuItem showAsFilmstripMenuItem = new JCheckBoxMenuItem("Show as filmstrip", filmstripMode);
    showAsFilmstripMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        boolean filmstripMode = showAsFilmstripMenuItem.isSelected(); 
        setFilmstripMode(filmstripMode);
      }
    });
    menu.add(showAsFilmstripMenuItem);
    
    final JCheckBoxMenuItem useThumbnailImageMenuItem = new JCheckBoxMenuItem("Use thumbnail image", useThumbnailImage);
    useThumbnailImageMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setUseThumbnailImage(useThumbnailImageMenuItem.isSelected());
      }
    });
    menu.add(useThumbnailImageMenuItem);

    JCheckBoxMenuItem hideRoboticControlsMenuItem = new JCheckBoxMenuItem("Disable robotic controls", false);
    hideRoboticControlsMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        setRoboticControls();
      }
    });
    menu.add(hideRoboticControlsMenuItem);    
    
    //enable/disable menu items
    boolean hasImage = displayedImageData != null;
    saveImageMenuItem.setEnabled(hasImage);
    copyImageMenuItem.setEnabled(hasImage);
    printImageMenuItem.setEnabled(hasImage);
    
    boolean enableZoom = hasImage && ! filmstripMode;
    zoomInMenuItem.setEnabled(enableZoom);
    zoomInMenuItem.setVisible(!filmstripMode);
    zoomOutMenuItem.setEnabled(enableZoom);
    zoomOutMenuItem.setVisible(!filmstripMode);
    zoomMenuSeparator.setVisible(!filmstripMode);
    
    autoScaleMenuItem.setVisible(!filmstripMode);
    resetScaleMenuItem.setVisible(!filmstripMode);
    showNavigationImageMenuItem.setVisible(!filmstripMode);
    maximumFilmstripImagesMenu.setVisible(filmstripMode);
    
    showAsFilmstripMenuItem.setSelected(filmstripMode);

    useThumbnailImageMenuItem.setSelected(useThumbnailImage);
    useThumbnailImageMenuItem.setVisible(imageHasThumbnail());
    
    hideRoboticControlsMenuItem.setVisible(flexTPSStream != null);

  }
  
  /**
   * Sets filmstrip mode. If enabled, images will be displayed like a filmstrip,
   * if disbaled only the latest image will be displayed.
   * 
   * @param filmstripMode  if true, display images like a filmstrip
   */
  private void setFilmstripMode(boolean filmstripMode) {
    if (this.filmstripMode == filmstripMode) {
      return;
    }
    
    this.filmstripMode = filmstripMode;
    
    BufferedImage displayedImage = getDisplayedImage();
    
    if (filmstripMode) {
      panel.remove(imagePanel);
      imagePanel.setImage(null, -1);

      if (displayedImage != null) {
        filmstripPanel.addImage(displayedImage, displayedImageTime);
      }
      panel.add(filmstripPanel, BorderLayout.CENTER);
    } else {
      panel.remove(filmstripPanel);
      filmstripPanel.clearImages();
      
      if (displayedImage != null) {
        imagePanel.setImage(displayedImage, displayedImageTime);
      }
      panel.add(imagePanel, BorderLayout.CENTER);
    }
    
    setRoboticControls();
    
    panel.revalidate();
    panel.repaint();
    
    if (filmstripMode) {
      properties.setProperty(DATA_PANEL_PROPERTY_FILMSTRIP_MODE, "true");
    } else {
      properties.remove(DATA_PANEL_PROPERTY_FILMSTRIP_MODE);
    }
  }
  
  /**
   * Select the correct radio button for the maximum number of filmstrip images.
   */
  private void updateMaximumFilmstripImagesRadioButtons() {
    int maximumFilmstripImages = filmstripPanel.getMaximumImages();
    int index = Arrays.binarySearch(MAXIMUM_FILMSTRIP_IMAGES_VALUES, maximumFilmstripImages);
    if (index >= 0) {
      Enumeration<AbstractButton> elements = maximumFilmstripImagesButtonGroup.getElements();
      AbstractButton button = null;
      while (index-- >= 0) {
        button = elements.nextElement();
      }
      
      maximumFilmstripImagesButtonGroup.setSelected(button.getModel(), true);
    } else {
      ButtonModel buttonModel = maximumFilmstripImagesButtonGroup.getSelection();
      if (buttonModel != null) {
        buttonModel.setSelected(false);
      }
    }
    
  }
  
  /**
   * Controls the usage of thumbnail images.
   * 
   * @param useThumbnailImage  if true, use thumbnail images, otherwise don't
   */
  private void setUseThumbnailImage(boolean useThumbnailImage) {
    if (this.useThumbnailImage == useThumbnailImage) {
      return;
    }
    
    if (seriesList_.size() != 0 && useThumbnailImage && !imageHasThumbnail()) {
      return;
    }
    
    this.useThumbnailImage = useThumbnailImage;
    
    if (seriesList_.size() != 0) {
      clearImage();
      
      String channelName = (String)seriesList_.getChannels().iterator().next();
      String thumbnailChannelName = THUMBNAIL_PLUGIN_NAME + "/" + channelName;
      
      if (useThumbnailImage) {
        //RBNBController.getInstance().unsubscribe(channelName, this);
        //unsubscribe(channelName);
        unsubscribeAll();
        //RBNBController.getInstance().subscribe(thumbnailChannelName, this);
        subscribe(new ChannelSeries(thumbnailChannelName));
      } else {
        //RBNBController.getInstance().unsubscribe(thumbnailChannelName, this);
        //unsubscribe(thumbnailChannelName);
        //RBNBController.getInstance().subscribe(channelName, this);
        subscribe(new ChannelSeries(channelName));
      }
    }
    
    if (useThumbnailImage) {
      properties.setProperty(DATA_PANEL_PROPERTY_USE_THUMBNAIL_IMAGE, "true");
    } else {
      properties.remove(DATA_PANEL_PROPERTY_USE_THUMBNAIL_IMAGE);
    }
  }

  /**
   * Returns true if there is a thumbnail image channel available for this
   * image.
   * 
   * @return
   */
  private boolean imageHasThumbnail() {
    if (seriesList_.size() == 0) {
      return false;
    }
    
    String channelName = (String)seriesList_.getChannels().iterator().next();
    String thumbnailChannelName = THUMBNAIL_PLUGIN_NAME + "/" + channelName;
    Channel thumbnailChannel = RBNBController.getInstance().getChannel(thumbnailChannelName);
    
    return thumbnailChannel != null;
  }
  
  private void addRoboticControls() {
    if (flexTPSStream == null) {
      return;
    }
    
    if (flexTPSStream.canIris()) {
      topControls.add(irisControls, BorderLayout.NORTH);
    } else {
      topControls.remove(irisControls);
    }
    
    if (flexTPSStream.canFocus()) {
      topControls.add(focusControls, BorderLayout.CENTER);
    } else {
      topControls.remove(focusControls);
    }
    
    if (flexTPSStream.canZoom()) {
      topControls.add(zoomControls, BorderLayout.SOUTH);  
    } else {
      topControls.remove(zoomControls);
    }
    
    if (flexTPSStream.canTilt() && flexTPSStream.canPan()) {
      panel.add(tiltControls, BorderLayout.EAST);
      panel.add(panControls, BorderLayout.SOUTH);
    } else {
      panel.remove(tiltControls);
      panel.remove(panControls);      
    }
    
    panel.revalidate();
    
    imagePanel.removeMouseListener(roboticMouseClickListener);
    imagePanel.addMouseListener(roboticMouseClickListener);
  }
  
  private void removeRoboticControls() {
    topControls.remove(irisControls);
    topControls.remove(focusControls);
    topControls.remove(zoomControls);
    panel.remove(tiltControls);
    panel.remove(panControls);
    panel.revalidate();
    
    imagePanel.removeMouseListener(roboticMouseClickListener);
  }
  
  private void setRoboticControls() {
    if (filmstripMode) {
      removeRoboticControls();
      return;
    }
    
    if (hideRoboticControls_) {
      properties.setProperty(DATA_PANEL_PROPERTY_HIDE_ROBOTIC_CONTROLS, "true");
      removeRoboticControls();      
    } else {
      properties.remove(DATA_PANEL_PROPERTY_HIDE_ROBOTIC_CONTROLS);
      if (state == Player.STATE_MONITORING) {
        addRoboticControls();
      } else {
        removeRoboticControls();
      }      
    }
  }
  
  /**
   * Gets the displayed image. If no image is displayed or there is an error
   * decoding the displayed image, null is returned.
   * 
   * @return  the displayed image, or null if there is one or if there is an
   *          error decoding the image
   */
  private BufferedImage getDisplayedImage() {
    if (displayedImageData == null) {
      return null;
    }
    
    InputStream in = new ByteArrayInputStream(displayedImageData);
    BufferedImage bufferedImage = null;
    try {
      bufferedImage = ImageIO.read(in);
    } catch (IOException e) {
      log.error("Failed to decode displayed image.");
      e.printStackTrace();
    }
    return bufferedImage;
  }
  
  /**
   * If an image is currently being displayed, save it to a file. This will
   * bring up a file chooser to select the file.
   */
  private void saveImage() {
    saveImage(UIUtilities.getCurrentDirectory());
  }

  /**
   * If an image is currently being displayed, save it to a file. This will
   * bring up a file chooser to select the file.
   * 
   * @param directory the directory to start the file chooser in
   */
  private void saveImage(File directory) {
    if (displayedImageData != null) {
      // create default file name
      String channelName = (String)seriesList_.getChannels().iterator().next();
      String fileName = channelName.replace("/", " - ");
      if (!fileName.endsWith(".jpg")) {
        fileName += ".jpg";
      }
      File selectedFile = new File(directory, fileName);
      
      // create filter to only show files that end with '.jpg'
      FileFilter fileFilter = new FileFilter() {
        public boolean accept(File f) {
          return (f.isDirectory() || f.getName().toLowerCase().endsWith(".jpg"));
        }
        public String getDescription() {
          return "JPEG Image Files";
        }
      };
      
      // show dialog
      File outFile = UIUtilities.getFile(fileFilter, "Save", null, selectedFile);
      if (outFile != null) {
        // prompt for overwrite if file already exists
        if (outFile.exists()) {
          int overwriteReturn = JOptionPane.showConfirmDialog(null,
              outFile.getName() + " already exists. Do you want to replace it?",
              "Replace image?",
              JOptionPane.YES_NO_OPTION);
          if (overwriteReturn == JOptionPane.NO_OPTION) {
            saveImage(outFile.getParentFile());
            return;
          }
        }

        // write image file
        try {
          FileOutputStream out = new FileOutputStream(outFile);
          out.write(displayedImageData);
          out.close();
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null,
              "Filed to write image file.",
              "Save Image Error",
              JOptionPane.ERROR_MESSAGE);
          e.printStackTrace();
        }        
      }      
    }
  }

  /**
   * Copy the currently displayed image to the clipboard. If no image is being
   * displayed, nothing will be copied to the clipboard.
   */
  private void copyImage() {
    // get the displayed image
    Image displayedImage = getDisplayedImage();
    if (displayedImage == null) {
      return;
    }
    
    // get the system clipboard
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    // create the transferable to transfer an image
    ImageSelection contents = new ImageSelection(displayedImage);
    
    // set the clipboard contents to the image transferable
    clipboard.setContents(contents, null);
  }  

  /**
   * Print the displayed image. If no image is being displayed, this will method
   * will do nothing.
   */
  private void printImage() {
    // get the displayed image
    final Image displayedImage = getDisplayedImage();
    if (displayedImage == null) {
      return;
    }

    // setup a print job
    PrinterJob printJob = PrinterJob.getPrinterJob();
    
    // set the renderer for the image
    printJob.setPrintable(new Printable() {
      public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
        //we only have one page to print
        if (pageIndex != 0) {
          return Printable.NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D)g;
        
        // move to corner of imageable page
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        // get page dimensions
        double pageWidth = pageFormat.getImageableWidth();
        double pageHeight = pageFormat.getImageableHeight();
        
        // get image dimensions
        int imageWidth = displayedImage.getWidth(null);
        int imageHeight = displayedImage.getHeight(null);

        // get scale factor for image
        double widthScale = pageWidth/imageWidth;
        double heightScale = pageHeight/imageHeight;
        double scale = Math.min(widthScale, heightScale);
        
        // draw image with width and height scaled to page
        int scaledWidth = (int)(scale*imageWidth);
        int scaledHeight = (int)(scale*imageHeight);
        g2d.drawImage(displayedImage, 0, 0, scaledWidth, scaledHeight, null);
       
        return Printable.PAGE_EXISTS;
      }

    });
    
    // set the job name to the channel name (plus jpg extension)
    // this is used as a hint for a file name when printing to file
    String channelName = (String)seriesList_.getChannels().iterator().next();
    String jobName = channelName.replace("/", " - ");
    if (!jobName.endsWith(".jpg")) {
      jobName += ".jpg";
    }
    printJob.setJobName(jobName);
    
    // show the print dialog and print if ok clicked
    if (printJob.printDialog()) {
      try { 
        printJob.print();
      } catch(PrinterException pe) {
        JOptionPane.showMessageDialog(null,
            "Failed to print image.",
            "Print Image Error",
            JOptionPane.ERROR_MESSAGE);        
        pe.printStackTrace();
      }
    }
  }
  
  private void setShowNavigationImage(boolean showNavigation) {
    imagePanel.setNavigationImageEnabled(showNavigation);
  }
  
  private void setAutoScale(boolean autoScale) {
    imagePanel.setAutoScaling(autoScale);
  }

  private void panLeft() {
    if (flexTPSStream != null) {
      flexTPSStream.panLeft();
    }
  }
  
  private void panRight() {
    if (flexTPSStream != null) {
      flexTPSStream.panRight();
    }    
  }
  
  private void pan(int pan) {
    if (flexTPSStream != null) {
      flexTPSStream.pan(pan);
    }
  }

  private void tiltUp() {
    if (flexTPSStream != null) {
      flexTPSStream.tiltUp();
    }    
  }
  
  private void tiltDown() {
    if (flexTPSStream != null) {
      flexTPSStream.tiltDown();
    }        
  }
  
  private void tilt(int tilt) {
    if (flexTPSStream != null) {
      flexTPSStream.tilt(tilt);
    }
  }
  
  private void home() {
    if (flexTPSStream != null) {
      flexTPSStream.goHome();
    }
  }
  
  private void center(Point p) {
    if (flexTPSStream != null) {
      flexTPSStream.center(p, imagePanel.getScaledImageSize());
    }
  }
  
  private void zoomIn() {
    if (flexTPSStream != null) {
      flexTPSStream.zoomIn();
    }    
  }
  
  private void zoomOut() {
    if (flexTPSStream != null) {
      flexTPSStream.zoomOut();
    }        
  }
  
  private void zoom(int zoom) {
    if (flexTPSStream != null) {
      flexTPSStream.zoom(zoom);
    }
  }
  
  private void irisClose() {
    if (flexTPSStream != null) {
      flexTPSStream.irisClose();
    }
  }
  
  private void irisOpen() {
    if (flexTPSStream != null) {
      flexTPSStream.irisOpen();
    }  
  }
  
  private void iris(int iris) {
    if (flexTPSStream != null) {
      flexTPSStream.iris(iris);
    }
  }
  
  private void irisAuto() {
    if (flexTPSStream != null) {
      flexTPSStream.irisAuto();
    }    
  }
  
  private void focusNear() {
    if (flexTPSStream != null) {
      flexTPSStream.focusNear();
    }
  }
  
  private void focusFar() {
    if (flexTPSStream != null) {
      flexTPSStream.focusFar();
    }    
  }
  
  private void focus(int focus) {
    if (flexTPSStream != null) {
      flexTPSStream.focus(focus);
    }    
  }
  
  private void focusAuto() {
    if (flexTPSStream != null) {
      flexTPSStream.focusAuto();
    }
  }
  
  private void setupFlexTPSStream() {
    String channelName = (String)seriesList_.getChannels().iterator().next();

    Channel channel = RBNBController.getInstance().getChannel(channelName);
    if (channel == null) {
      return;
    }
    
    String host = channel.getMetadata("flexTPS_host");
    String feed = channel.getMetadata("flexTPS_feed");
    String stream = channel.getMetadata("flexTPS_stream");

    // If user successfully login, gaSession should be valid
    Authentication auth = AuthenticationManager.getInstance().getAuthentication();
    String gaSession = null;
    if (auth != null) {
      gaSession = auth.get("session");
    }
    
    if (host != null && feed != null && stream != null) {
      flexTPSStream = new FlexTPSStream(host, feed, stream, gaSession);
      
      if (flexTPSStream.canDoRobotic()) {
        setRoboticControls();
      } else {
        flexTPSStream = null;
        removeRoboticControls();
      }
    }
  }
  
	public boolean supportsMultipleChannels() {
		return false;
	}
	
	public boolean setChannel(String channelName) {
		if (!isChannelSupported(channelName)) {
			return false;
		}
		
		return super.setChannel(channelName);
	}
	
	private boolean isChannelSupported(String channelName) {
		Channel channel = RBNBController.getInstance().getChannel(channelName);
    if (channel == null) {
      return false;
    }
    
		String mimeType = channel.getMetadata("mime");
        
		Extension extension = ExtensionManager.getInstance().getExtension(this.getClass());
		if (extension != null) {
			List<String> mimeTypes = extension.getMimeTypes();
			for (int i=0; i<mimeTypes.size(); i++) {
				String mime = mimeTypes.get(i);
        if (mime.equals(mimeType)) {
					return true;
				}
			}
		}
		
		return false;
	}
  
  @Override
  protected void onSeriesAdded(VisualizationSeries vs) {
    String channelName = vs.getChannels().iterator().next();
    // subscribe to the thumbnail channel if it exists
    if (useThumbnailImage) {
      if (imageHasThumbnail()) {
        // TODO we are still subscribing to the channel initially and this will
        //      cause a fetch of 1 image, so we should suppress this somehow
        //RBNBController.getInstance().unsubscribe(channelName, this);
        //unsubscribe(channelName);
        unsubscribeAll();
        String thumbnailChannelName = THUMBNAIL_PLUGIN_NAME + "/" + channelName;
        //RBNBController.getInstance().subscribe(thumbnailChannelName, this);
        subscribe(new ChannelSeries(thumbnailChannelName));
      } else {
        setUseThumbnailImage(false);
      }
    }
    setupFlexTPSStream();
  }
  
//  @Override
//  protected void channelAdded(String channelName) {
//    setupFlexTPSStream();
//  }
  
  @Override
  protected void onSeriesRemoved(VisualizationSeries vs) {
    String channelName = vs.getChannels().iterator().next();
    if (useThumbnailImage) {
      String thumbnailChannelName = THUMBNAIL_PLUGIN_NAME + "/" + channelName;
      //RBNBController.getInstance().unsubscribe(thumbnailChannelName, this);
      //unsubscribe(thumbnailChannelName);
      unsubscribeAll();
    }
    
    clearImage();
    
    flexTPSStream = null;
    removeRoboticControls();
  }
	
  public void postTime(final double time) {
    super.postTime(time);
  }
  
  /***
   * Schedule display of new images and age old images (images outside
   * current display range will disappear).
   */
	public void postData(final SubscriptionResponse r) {
//		TimeSeriesData tsd=r.getTimeSeries();
//		Iterator<String> it = channels.iterator();
//    if (!it.hasNext()) {
//      //no channels to post to
//      return;
//    }
//    
//		String channelName;  
//    if (useThumbnailImage) {
//      channelName = THUMBNAIL_PLUGIN_NAME + "/" + it.next();
//    } else {
//      channelName = it.next();
//    }
		VizSeriesList vsl=getSeries();
    if(vsl.size()==0)return;
    
    VisualizationSeries s = vsl.get(0);
    String channelName=s.getChannels().get(0);
    TimeSeriesData tsd=r.getTimeSeries(s);
    if(tsd==null) return;
    
    int channelIndex = tsd.getChannelIndex(channelName);
    
    //See if there is an data for us in the channel map
    if (channelIndex < 0 || !tsd.hasData(channelIndex)) {
      log.debug("ImageViz panel got no data on channel "+channelName);
      return;
    }else{
    
  		final Double currTime=tsd.getMaxEndTime();
  		
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          filmstripPanel.setTime(currTime);
        }
      });
  		
      postImages(r,s);//channelIndex,channelName);
      
  //    //clear stale images
  //    if (displayedImageTime != -1 && (displayedImageTime <= currTime-timeScale || 
  //                                      displayedImageTime > currTime)) {
  //      SwingUtilities.invokeLater(new Runnable() {
  //        public void run() {
  //          clearImage();
  //          
  //        }
  //      });
    }
  }
	
  @Override
  public void globalTimeScaleChanged(final double timeScale) {
    super.globalTimeScaleChanged(timeScale);
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        filmstripPanel.setTimescale(timeScale);
      }
    });
  }

  /***
   * Schedule new image updates to be displayed by the AWT thread.
   * @param r
   */
  private void postImages(final SubscriptionResponse r,
       final VisualizationSeries s) {
		
    
    
		SwingUtilities.invokeLater(new Runnable() {
      public void run() {
      TimeSeriesData tsd=r.getTimeSeries(s);
      String channelName=s.getChannels().get(0);
      int channelIndex = tsd.getChannelIndex(channelName);  
      try {
  		  final Double currTime=tsd.getMaxEndTime();
  		  //clear stale images
  	    if (displayedImageTime != -1 && (displayedImageTime <= currTime-timeScale || 
  	                                      displayedImageTime > currTime)) {
  	          log.debug("clearing image at time: "+RBNBTime.formatISO(displayedImageTime)+
  	                  " scale start: "+RBNBTime.formatISO(currTime-timeScale)+" end: "+
  	                  RBNBTime.formatISO(currTime));
  	          clearImage();   
  	    }
  	    
  			if (tsd.getType(channelIndex) != ChannelMap.TYPE_BYTEARRAY) {
  				log.error("Expected byte array for JPEG data in channel " + channelName + ".");
  				return;
  			}
  			
  			List<Double> times=tsd.getTimes(channelIndex);
//    			if(times.size()<=0){
//    			  return;
//    			}
  			
  			if(r.dropData()){
  			  // only post last frame
  			  Double lastTime=times.get(times.size()-1);
  			  byte[] imageData = tsd.getDataAsByteArray(channelIndex,lastTime);
  			  postNewImage(imageData,lastTime,channelName);
  			}else{
    			Iterator<Double> timeIt=times.iterator();
    			while(timeIt.hasNext()){
    			
    			   Double nextTime= timeIt.next();
    			   final double imageTime=nextTime;
  //  			if (imageTime == displayedImageTime) {
  //  				//we are already displaying this image
  //  				return;
  //  			}
    			  
    			  final byte[] imageData = tsd.getDataAsByteArray(channelIndex,nextTime);
    			  postNewImage(imageData,imageTime,channelName);
    			}
  			}
    	} catch (Exception e) {
  			log.error("Failed to receive data for channel " + channelName + ".");
  			e.printStackTrace();
    	}finally{
    	  //tsd.free(ImageViz.this);
    	}
     }
    });

	}
  
  /***
   * Create an image from the byte array data and add it to the display panel.
   * @param imageData
   * @param imageTime
   * @param channelName
   */
  private void postNewImage(final byte[] imageData, final double imageTime,
                            final String channelName){
      // save raw image data
      displayedImageData = imageData;
      
      //draw image on screen
      final InputStream in = new ByteArrayInputStream(displayedImageData);
      
      try {
        BufferedImage bufferedImage = ImageIO.read(in);
        
        if (filmstripMode) {
          filmstripPanel.addImage(bufferedImage, imageTime);
        } else {
          imagePanel.setImage(bufferedImage, imageTime);
          imagePanel.setToolTipText(RBNBTime.formatISO(imageTime));
        }
        //update the image time currently displayed
        displayedImageTime = imageTime;
        
        log.debug("image posted at "+RBNBTime.formatISO(imageTime));
      } catch (IOException e) {
        log.error("Failed to decode image for " + channelName + ".");
        e.printStackTrace();
      }
  }
  
  public void postState(int newState, int oldState) {
    super.postState(newState, oldState);
    
    setRoboticControls();
  }
	
	private void clearImage() {
	  if (filmstripMode) {
	    filmstripPanel.clearImages();
	  } else {
	    imagePanel.setImage(null, -1);
	  }
	  
		displayedImageData = null;
		displayedImageTime = -1;
		log.debug("cleared latest image");
	}

  @Override
  public void setProperty(String key, String value) {
    super.setProperty(key, value);
    
    if (key == null) {
      return;
    }
    
    if (key.equals(DATA_PANEL_PROPERTY_SCALE)) {
      try {
        double scale = Double.parseDouble(value);
        imagePanel.setScale(scale);
      } catch (NumberFormatException e) {
        log.warn("Unable to set scale: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_ORIGIN)) {
      String[] pointComponents = value.split(",");
      if (pointComponents.length == 2) {
        try {
          int x = Integer.parseInt(pointComponents[0]);
          int y = Integer.parseInt(pointComponents[1]);
          imagePanel.setOrigin(x, y);
        } catch (NumberFormatException e) {
          log.warn("Unable to set origin: " + value + ".");
        }
      } else {
        log.warn("Unable to set origin: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_SHOW_NAVIGATION_IMAGE) && Boolean.parseBoolean(value)) {
      imagePanel.setNavigationImageEnabled(true);
    } else if (key.equals(DATA_PANEL_PROPERTY_FILMSTRIP_MODE) && Boolean.parseBoolean(value)) {
      setFilmstripMode(true);
    } else if (key.equals(DATA_PANEL_PROPERTY_MAXIMUM_FILMSTRIP_IMAGES)) {
      try {
        int maximumFilmstripImages = Integer.parseInt(value);
        filmstripPanel.setMaximumImages(maximumFilmstripImages);
      } catch (NumberFormatException e) {
        log.warn("Unable to set maximum filmstrip images: " + value + ".");
      }
    } else if (key.equals(DATA_PANEL_PROPERTY_USE_THUMBNAIL_IMAGE) && Boolean.parseBoolean(value)) {
      setUseThumbnailImage(true);
    } else if (key.equals(DATA_PANEL_PROPERTY_HIDE_ROBOTIC_CONTROLS) && Boolean.parseBoolean(value)) {
      hideRoboticControls_=true;
      setRoboticControls();
    }
  }
  
  /**
   * Converts the coordinates of a point to a string of the format "x,y".
   * 
   * @param p  the point
   * @return   the coordinates of the point as a string
   */
  private static String pointToString(Point p) {
    return p.x + "," + p.y;    
  }
  
  public void authenticationChanged(AuthenticationEvent event) {
    setupFlexTPSStream();
  }  
	
}