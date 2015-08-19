/*
 * RDV
 * Real-time Data Viewer
 * http://rdv.googlecode.com/
 * 
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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/viz/image/FilmstripPanel.java $
 * $Revision: 1119 $
 * $Date: 2008-07-01 10:21:41 -0400 (Tue, 01 Jul 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.viz.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.rdv.DataViewer;
import org.rdv.rbnb.RBNBController;

/**
 * A class to display images like a filmstrip.
 * 
 * @author Jason P. Hanley
 */
public class FilmstripPanel extends JPanel {

  /** serialization version identifier */
  private static final long serialVersionUID = 6924475674504241229L;
  
  /** the maximum images property */
  public static final String MAXIMUM_IMAGES_PROPERTY = "maximumImages";
  
  /** the default value for the maximum images */
  public static final int MAXIMUM_IMAGES_DEFAULT = 4;
  
  /** the maximum number of images that can be displayed at one time */
  private int maximumImages; 
  
  /** the current time */
  private double time;
  
  /** the time scale */
  private double timescale;
  
  /** the mouse listener for clicks on an image */
  private final MouseListener mouseListener;
  
  /** a image panel cached for later use */
  private ImagePanel cachedImagePanel;
  
  /**
   * Creates a filmstrip panel with a time scale of 1.
   */
  public FilmstripPanel() {
    super();
    
    maximumImages = MAXIMUM_IMAGES_DEFAULT;
    
    timescale = 1;
    
    setLayout(new FilmstripLayout());
    
    mouseListener = new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
          ImagePanel imagePanel = (ImagePanel) e.getComponent();
          double timestamp = imagePanel.getTimestamp();
          if (timestamp >= 0) {
            RBNBController.getInstance().setLocation(timestamp);
          }
        }
      }
    };
  }
  
  /**
   * Adds an image to the filmstrip.
   * 
   * @param image      the image to add
   * @param timestamp  the timestamp for the image
   */
  public void addImage(BufferedImage image, double timestamp) {
    int componentCount = getComponentCount();
    if (componentCount > 0) {
      ImagePanel imagePanel = (ImagePanel)getComponent(componentCount-1);
      if (imagePanel.getTimestamp() == timestamp) {
        return;
      }
    }
    
    ImagePanel imagePanel;
    if (cachedImagePanel != null) {
      imagePanel = cachedImagePanel;
      cachedImagePanel = null;
    } else {
      imagePanel = new ImagePanel(false);
      imagePanel.setBackground(Color.black);
      imagePanel.setComponentPopupMenu(getComponentPopupMenu());
      imagePanel.addMouseListener(mouseListener);
      
      for (MouseListener listener : getMouseListeners()) {
        imagePanel.addMouseListener(listener);
      }
    }
        
    imagePanel.setImage(image, timestamp);
    imagePanel.setToolTipText(DataViewer.formatDateSmart(timestamp));
    
    add(imagePanel);
    
    purgeImages();
  }
  
  /**
   * Removes all the images from the filmstrip.
   */
  public void clearImages() {
    removeAll();
  }
  
  /**
   * Gets the maximum number of images displayed in the filmstrip.
   * 
   * @return  the maximum number of images displayed
   */
  public int getMaximumImages() {
    return maximumImages;
  }
  
  /**
   * Sets the maximum number of images displayed in the filmstrip.
   * 
   * @param maximumImages  the maximum number of images to display
   */
  public void setMaximumImages(int maximumImages) {
    if (this.maximumImages == maximumImages || maximumImages < 2) {
      return;
    }
    
    int oldMaximumImages = this.maximumImages;
    this.maximumImages = maximumImages;
    
    firePropertyChange(MAXIMUM_IMAGES_PROPERTY, oldMaximumImages, maximumImages);
    
    purgeImages();
  }
  
  /**
   * Sets the time for the filmstrip. Images older than the time scale will be
   * removed.
   * 
   * @param time  the new time
   */
  public void setTime(double time) {
    this.time = time;
    
    purgeImages();
  }
  
  /**
   * Sets the time scale. Images older than the time scale will be removed.
   * 
   * @param timescale  the new time scale
   */
  public void setTimescale(double timescale) {
    this.timescale = timescale;
    
    purgeImages();
  }
  
  /**
   * Purges images that shouldm't be displayed anymore. This includes enforcing
   * the maximum number of images and aging images older than the time scale,
   * relative to the current time.
   */
  private void purgeImages() {
    if (getComponentCount() == 0) {
      return;
    }
    
    boolean revalidate = false;
    
    while (getComponentCount() > maximumImages) {
      cacheImagePanel((ImagePanel)getComponent(0));
      
      remove(0);
      revalidate = true;
    }
    
    for (int i=getComponentCount()-1; i>=0 ; i--) {
      ImagePanel imagePanel = (ImagePanel)getComponent(i);
      if (imagePanel.getTimestamp() <= time-timescale || imagePanel.getTimestamp() > time) {
        cacheImagePanel((ImagePanel)getComponent(i));
        
        remove(i);
        revalidate = true; 
      }
    }
    
    if (revalidate) {
      revalidate();
    }
  }
  
  /**
   * Caches the image panel if one isn't cached already. The image panel will
   * be cleared of any image it contains.
   * 
   * @param imagePanel  the image panel to cache
   */
  private void cacheImagePanel(ImagePanel imagePanel) {
    if (cachedImagePanel != null) {
      return;
    }
    
    imagePanel.setImage(null, -1);
    cachedImagePanel = imagePanel;
  }
  
  /**
   * A simple layout manager that's layouts the components horizontally. The
   * components width will be the same based on the widht of the container.
   * Their height will be the height on the container.
   * 
   * @author Jason P. Hanley
   */
  class FilmstripLayout implements LayoutManager {

    public void addLayoutComponent(String name, Component component) {}
    
    public void removeLayoutComponent(Component component) {}

    /**
     * Lays out the container like a filmstrip. Each component will be of equal
     * size and layed out next to each other left-to-right.
     */
    public void layoutContainer(Container container) {
      int componentCount = container.getComponentCount();
      if (componentCount == 0) {
        return;
      }
      
      Dimension dimensions = container.getSize();
      Insets insets = container.getInsets();
      dimensions.width -= insets.left + insets.right;
      dimensions.height -= insets.top + insets.bottom;
      
      //if(componentCount != 4 && componentCount != 9 && componentCount != 16 && componentCount != 25) {
        /*int width = dimensions.width / componentCount;
      
        for (int i = 0; i < componentCount; i++) {
          Component component = container.getComponent(i);
          component.setBounds(i*width, 0, width, dimensions.height);
        }*/
        
        int numRows = 0;
        int numCols = 0;
        
        while(numCols * numRows < componentCount) {
          if(numCols > numRows)
            numRows++;
          else
            numCols++;
        }
        int width = dimensions.width / numCols;
        int height = dimensions.height / numRows;
      
        int count = componentCount - 1;
        for(int i = 0; i < numRows; i++) {
          for(int j = 0; j < numCols; j++) {
            if(count >= 0) {
              Component component = container.getComponent(count);
              component.setBounds(j*width, i*height, width, height);
            }
            count--;
          }
        }
    }

    /**
     * Gets the minimum layout size which is 0,0.
     */
    public Dimension minimumLayoutSize(Container container) {
      return new Dimension(0, 0);
    }

    /**
     * Gets the preferred layout size based on the preferred size of the first
     * component.
     */
    public Dimension preferredLayoutSize(Container container) {
      int componentCount = container.getComponentCount();
      if (componentCount == 0) {
        return new Dimension(0, 0);
      }
      
      Dimension dimension = container.getComponent(0).getPreferredSize();
      return new Dimension(componentCount*dimension.width, dimension.height);
    }

  }

}