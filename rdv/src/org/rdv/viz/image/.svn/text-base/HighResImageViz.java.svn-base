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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
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
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;
import org.rdv.Extension;
import org.rdv.ExtensionManager;
import org.rdv.TimeScale;
import org.rdv.auth.AuthenticationEvent;
import org.rdv.auth.AuthenticationListener;
import org.rdv.auth.AuthenticationManager;
import org.rdv.data.Channel;
import org.rdv.data.ChannelSeries;
import org.rdv.data.VisualizationSeries;
import org.rdv.data.VizSeriesList;
import org.rdv.datapanel.AbstractDataPanel;
import org.rdv.rbnb.RBNBController;
import org.rdv.rbnb.RBNBDataRequest;
import org.rdv.rbnb.RBNBTime;
import org.rdv.rbnb.SubscriptionRequest;
import org.rdv.rbnb.SubscriptionResponse;
import org.rdv.rbnb.TimeSeriesData;
import org.rdv.ui.UIUtilities;

import com.rbnb.sapi.ChannelMap;

/**
 * A visualization extension for viewing high resolution images.
 * Modified from ImageViz
 * @author Andy Moraczewski
 */
public class HighResImageViz extends AbstractDataPanel implements AuthenticationListener,ImageDisplay {
  
  /** the logger for this class */
  static Log log = LogFactory.getLog(HighResImageViz.class.getName());
  
  /** the data panel autoscale property */
  private static final String DATA_PANEL_PROPERTY_AUTOSCALE = "autoscale";
  
  /** the data panel property for the scale */
  private static final String DATA_PANEL_PROPERTY_SCALE = "scale";
  
  /** the data panel property for the origin */
  private static final String DATA_PANEL_PROPERTY_ORIGIN = "origin";
  
  /** the data panel property for the navigation panel visibility */
  private static final String DATA_PANEL_PROPERTY_SHOW_NAVIGATION_IMAGE = "showNavigationImage";
  
  /** the data panel property for the usage of the thumbnail images */
  private static final String DATA_PANEL_PROPERTY_USE_THUMBNAIL_IMAGE = "useThumbnailImage";

  /** the main panel */
  private JPanel panel;
  
  /** the panel to display the image */
  private ImagePanel imagePanel;
  
  /** the panel to display images for the image selector panel */
  private ImageSelectorPanel imageSelectorPanel;
  
  // menu items for the popup menu
  //private JCheckBoxMenuItem autoScaleMenuItem;
  //private JCheckBoxMenuItem showNavigationImageMenuItem;
  
  /** a flag to enable/disable usage of a thumbnail image */
  private boolean useThumbnailImage;
  
  /** the name of the thumbnail plugin */
  private static final String THUMBNAIL_PLUGIN_NAME = "_Thumbnails";
 	
  /** the time for the currently displayed image */
  private double displayedImageTime;
 	
  /** the data for the currently displayed image */
  private byte[] displayedImageData;
 	 	
  public HighResImageViz() {
    super();
    
    useThumbnailImage = false;
    
    displayedImageTime = -1;
    
    // default to 30 minutes fixed to avoid using too much memory 
    localTimeScale_=1800;
    
    initUI();
    
    setDataComponent(panel);
  }
  
  public SubscriptionRequest getSubscriptionRequest(){
    return new RBNBDataRequest(this,
        SubscriptionRequest.TYPE_CHART,timeScale,getSeries()); //0);
  }
  
  @Override
  protected void setDataComponent(JComponent dataComponent) {
    super.setDataComponent(dataComponent);
//  public void openPanel(final DataPanelManager dataPanelManager) {
//    super.openPanel(dataPanelManager);
    
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
    initImageSelectorPanel();
    
    // set mouselistener to trigger popup menu
    imagePanel.addMouseListener(getPopupMenuMouseListener());
    //imageSelectorPanel.setComponentPopupMenu(popupMenu);
    panel.addMouseListener(getPopupMenuMouseListener()); 
    
  }
			
  private void initImagePanel() {
    imagePanel = new ImagePanel();
    imagePanel.setBackground(Color.black);
    imagePanel.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
        String propertyName = pce.getPropertyName();
        if (propertyName.equals(ImagePanel.AUTO_SCALING_PROPERTY)) {
          boolean autoScaling = (Boolean)pce.getNewValue();
          //autoScaleMenuItem.setSelected(autoScaling);
          if (autoScaling) {
            properties.setProperty(DATA_PANEL_PROPERTY_AUTOSCALE, "true");
            properties.remove(DATA_PANEL_PROPERTY_SCALE);
            properties.remove(DATA_PANEL_PROPERTY_ORIGIN);
          } else {
            properties.setProperty(DATA_PANEL_PROPERTY_SCALE, Double.toString(imagePanel.getScale()));
            properties.setProperty(DATA_PANEL_PROPERTY_ORIGIN, pointToString(imagePanel.getOrigin()));
            properties.remove(DATA_PANEL_PROPERTY_AUTOSCALE);
          }
        } else if (propertyName.equals(ImagePanel.SCALE_PROPERTY) && !imagePanel.isAutoScaling()) {
          properties.setProperty(DATA_PANEL_PROPERTY_SCALE, pce.getNewValue().toString());
          properties.remove(DATA_PANEL_PROPERTY_AUTOSCALE);
        } else if (propertyName.equals(ImagePanel.ORIGIN_PROPERTY) && !imagePanel.isAutoScaling()) {
          Point origin = (Point)pce.getNewValue();
          String originString = pointToString(origin);
          properties.setProperty(DATA_PANEL_PROPERTY_ORIGIN, originString);
          properties.remove(DATA_PANEL_PROPERTY_AUTOSCALE);
        } else if (propertyName.equals(ImagePanel.NAVIGATION_IMAGE_ENABLED_PROPERTY)) {
          boolean showNavigationImage = (Boolean)pce.getNewValue();
          //showNavigationImageMenuItem.setSelected(showNavigationImage);
          if (showNavigationImage) {
            properties.setProperty(DATA_PANEL_PROPERTY_SHOW_NAVIGATION_IMAGE, "true");
          } else {
            properties.remove(DATA_PANEL_PROPERTY_SHOW_NAVIGATION_IMAGE);
          }
        }
      }
    });
    
    panel.add(imagePanel, BorderLayout.CENTER);
  }
  
  private void initImageSelectorPanel() {
    imageSelectorPanel = new ImageSelectorPanel(this);
    imageSelectorPanel.setBackground(Color.LIGHT_GRAY);
    imageSelectorPanel.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent pce) {
      }
    });
    
    panel.add(imageSelectorPanel, BorderLayout.SOUTH);
  }

  @Override
  protected void setLocalTimescale(final double timeScale) {
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        imageSelectorPanel.setTimescale(timeScale);
      }
    });
    
    super.setLocalTimescale(timeScale);
  }
  
  public void buildPopupMenu(JPopupMenu menu, MouseEvent e){
    super.buildPopupMenu(menu, e);
    
    ActionListener setTimerangeAction = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        double newScale =Double.parseDouble(ae.getActionCommand());
        setLocalTimescale(newScale);
      }
    };
    
    // regenerate new menu
    JMenu timeRangeMenu = new JMenu("Set Timerange");
    ButtonGroup timeRangeGroup = new ButtonGroup();
    JRadioButtonMenuItem range = new JRadioButtonMenuItem(
                              TimeScale.TIME_SCALE_UNDEFINED_DESC);
    range.setActionCommand(String.valueOf(TimeScale.TIME_SCALE_UNDEFINED));
    range.addActionListener(setTimerangeAction);
    timeRangeGroup.add(range);
    timeRangeMenu.add(range);
    if (localTimeScale_ == TimeScale.TIME_SCALE_UNDEFINED)
      range.setSelected(true);
    
    Double timeScales[]=TimeScale.getGlobalTimeScales();
    for (int i=0;i<timeScales.length;i++) {
      range = new JRadioButtonMenuItem(
                  DataViewer.formatSeconds(timeScales[i]));
      range.setActionCommand(String.valueOf(timeScales[i]));
      range.addActionListener(setTimerangeAction);
      timeRangeGroup.add(range);
      timeRangeMenu.add(range);
      if (localTimeScale_ == timeScales[i])
        range.setSelected(true);
    }
    
    menu.add(timeRangeMenu);
    menu.addSeparator();
    
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
    
    //menu.addSeparator();
    
    final JCheckBoxMenuItem useThumbnailImageMenuItem = new JCheckBoxMenuItem("Use thumbnail image", useThumbnailImage);
    useThumbnailImageMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setUseThumbnailImage(useThumbnailImageMenuItem.isSelected());
      }
    });
    menu.add(useThumbnailImageMenuItem);
    
   
        boolean hasImage = displayedImageData != null;
        saveImageMenuItem.setEnabled(hasImage);
        copyImageMenuItem.setEnabled(hasImage);
        printImageMenuItem.setEnabled(hasImage);
        
        boolean enableZoom = hasImage;
        zoomInMenuItem.setEnabled(enableZoom);
        zoomInMenuItem.setVisible(true);
        zoomOutMenuItem.setEnabled(enableZoom);
        zoomOutMenuItem.setVisible(true);
        zoomMenuSeparator.setVisible(true);
        
        autoScaleMenuItem.setVisible(true);
        resetScaleMenuItem.setVisible(true);
        showNavigationImageMenuItem.setVisible(true);

        useThumbnailImageMenuItem.setSelected(useThumbnailImage);
        useThumbnailImageMenuItem.setVisible(imageHasThumbnail());
     
    
     
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
        subscribe(new ChannelSeries(thumbnailChannelName));
        //RBNBController.getInstance().subscribe(thumbnailChannelName, this);
      } else {
        //unsubscribe(thumbnailChannelName);
        unsubscribeAll();
        //RBNBController.getInstance().unsubscribe(thumbnailChannelName, this);
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
    if(!autoScale){
      //reset scale
      imagePanel.setScale(1);
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
        
        //unsubscribe(channelName);
        unsubscribeAll();
        //RBNBController.getInstance().unsubscribe(channelName, this);
        
        String thumbnailChannelName = THUMBNAIL_PLUGIN_NAME + "/" + channelName;
        //RBNBController.getInstance().subscribe(thumbnailChannelName, this);
        subscribe(new ChannelSeries(thumbnailChannelName));
      } else {
        setUseThumbnailImage(false);
      }
    }
  }
  
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
  }
	
  public void postTime(final double time) {
    super.postTime(time);
    System.out.println("posted time: "+DataViewer.formatDate(time));
     
//    // age images off panel
//    final double lastTime=time;
//    
//    SwingUtilities.invokeLater(new Runnable() {
//      public void run() {
//        imageSelectorPanel.setTime(lastTime);
//      }
//    });
  }
  
	public void postData(final SubscriptionResponse r) {
		String channelName;
		
//		Iterator<String> it = channels.iterator();
//    if (!it.hasNext()) {
//      // not subbed to any channels
//      return;
//    }else{
//      if (useThumbnailImage) {
//        channelName = THUMBNAIL_PLUGIN_NAME + "/" + it.next();
//      } else {
//        channelName = it.next();
//      }
//    }
    VizSeriesList seriesList = getSeries();
    for (VisualizationSeries s : seriesList){
      TimeSeriesData tsd=r.getTimeSeries(s);
      if(tsd==null) continue;
      
      final int channelIndex=tsd.getChannelIndex(s.getChannels().get(0));
      if(channelIndex<0 || !tsd.hasData(channelIndex)){
        // no data here
        continue;
      }
      
      final double lastTime=tsd.getEndTime(channelIndex);
      
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          imageSelectorPanel.setTime(lastTime);
        }
      });
  		
      postImage(tsd,channelIndex,s.getChannels().get(0));
      
    }
    
  //age images off panel
  final double lastTime=r.getRequestEndTime();
  
  SwingUtilities.invokeLater(new Runnable() {
    public void run() {
      imageSelectorPanel.setTime(lastTime);
    }
  });
    
//    if(tsd.free(this)){
//      subResponse_=null;
//    }
    
    //clear stale images
//    if (displayedImageTime != -1 && (displayedImageTime <= time-timeScale || displayedImageTime > time)) {
//      SwingUtilities.invokeLater(new Runnable() {
//        public void run() {
//          //clearImage();
//        }
//      });
//    }
  }
	
  @Override
  public void globalTimeScaleChanged(final double timeScale) {
    if(localTimeScale_==TimeScale.TIME_SCALE_UNDEFINED){
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          imageSelectorPanel.setTimescale(timeScale);
        }
      });
      
      super.globalTimeScaleChanged(timeScale);
    }
  }

  private void postImage(final TimeSeriesData tsd, final int channelIndex,
                          final String channelName) {
			if (tsd.getType(channelIndex) != ChannelMap.TYPE_BYTEARRAY) {
				log.error("Expected byte array for JPEG data in channel " + channelName + ".");
				return;
			}
			
		  SwingUtilities.invokeLater(new Runnable() {
        public void run() {
      			Iterator<Double> timeIt=tsd.getTimes(channelIndex).iterator();
      			while (timeIt.hasNext()){
      			  final Double imageTime=timeIt.next();
      			  
      			  final byte[] imageData = tsd.getDataAsByteArray(channelIndex,imageTime);
        			final boolean isCurrent = !timeIt.hasNext();
        			
        			if (imageData.length > 0) {  
        			  //draw image on screen                
                try{
                  InputStream in = new ByteArrayInputStream(imageData);
                  BufferedImage bufferedImage = ImageIO.read(in);
                  imageSelectorPanel.addImage(bufferedImage, imageTime, isCurrent);
                  if(isCurrent) imagePanel.setImage(bufferedImage, imageTime); 
                  HighResImageViz.this.getDataComponent().validate();
                  
                  //update the image data and time currently displayed
                  displayedImageData = imageData.clone();
                  displayedImageTime = imageTime;
                  
                } catch (IOException e) {
                  log.error("Failed to decode image for " + channelName + " at "+RBNBTime.formatISO(imageTime));
                  e.printStackTrace();
                }
                  
        			}else{
                log.error("Data array empty for channel " + channelName + " at "+RBNBTime.formatISO(imageTime));
              }
      			}
            //tsd.free(HighResImageViz.this); 
        }});
	}
  
  public double getTime(){
    return displayedImageTime;
  }
  
  public void setViewImage(BufferedImage image, double time){
    
    imagePanel.setImage(image, time);
    imageSelectorPanel.setThumb(time);
    displayedImageTime=time;
    
  }
  
  public void postState(int newState, int oldState) {
    super.postState(newState, oldState);
  }
	
	public void clearImage() {
	  imagePanel.setImage(null, -1);
	  imageSelectorPanel.clearImages();
	  
		displayedImageData = null;
		displayedImageTime = -1;
	}

  @Override
  public void setProperty(String key, String value) {
    super.setProperty(key, value);
    
    if (key == null) {
      return;
    }
    
    if (key.equals(DATA_PANEL_PROPERTY_AUTOSCALE)) {
      imagePanel.setAutoScaling(true);
    }else if (key.equals(DATA_PANEL_PROPERTY_SCALE)) {
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
    } else if (key.equals(DATA_PANEL_PROPERTY_USE_THUMBNAIL_IMAGE) && Boolean.parseBoolean(value)) {
      setUseThumbnailImage(true);
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
  }  
	
}