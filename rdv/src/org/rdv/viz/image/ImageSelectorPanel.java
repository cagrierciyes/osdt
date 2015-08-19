
package org.rdv.viz.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;
import org.rdv.rbnb.RBNBController;

/** 
 * A panel that is part of the high resolution image viewer which is used
 * for selecting a desired image to view in a higher resolution
 * @author Andrew Moraczewski
 */
public class ImageSelectorPanel extends JPanel {
  
  Log log = LogFactory.getLog(ImageSelectorPanel.class.getName());

  private static final long serialVersionUID = 8901808305980698355L;
  
  /** the current time */
  //private double time;
  
  /** the time scale */
  private double timescale;
  
  /** a canvas to store all the different images */
  private JPanel scrollingViewArea;
  
  /** the scroll pane */
  private JScrollPane scrollPane;
  
  /** a image panel cached for later use */
  private ImagePanel cachedImagePanel;
  
  /** the mouse listener for clicks on an image */
  private final MouseListener mouseListener;
  
  private HashMap<Double,BufferedImage> fullResImageMap_=new HashMap<Double,BufferedImage>();
  
  private ImageDisplay imageDisplay_;
  
  private double currThumbTime_=-1.0;
  
  public ImageSelectorPanel(ImageDisplay imgDisp) {
    super(new BorderLayout());
    
    imageDisplay_=imgDisp;
    scrollingViewArea = new JPanel();
    scrollingViewArea.setBackground(Color.black);
    scrollingViewArea.setOpaque(true);
    scrollingViewArea.setLayout(new ImageSelectorLayout());
    
    timescale = 1;
    
    scrollPane = new JScrollPane(scrollingViewArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollPane.addMouseListener(new MouseAdapter(){});
    
    mouseListener = new MouseAdapter() {
      
      private void showImage(MouseEvent e){
        if (SwingUtilities.isLeftMouseButton(e)){ //&& e.getClickCount() == 1) {
          if(e.getComponent() instanceof ImagePanel){
            final ImagePanel imagePanel = (ImagePanel) e.getComponent();
            
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                double timestamp = imagePanel.getTimestamp();
                if(imageDisplay_.getTime()!=timestamp){
                  
                  BufferedImage fullResImage=fullResImageMap_.get(new Double(timestamp));
                  if(fullResImage!=null){
                    imageDisplay_.setViewImage(fullResImage, timestamp);
                  }
                }
              }
            });
          }
        } 
      }
      public void mousePressed(MouseEvent e) {
        //System.out.println("got mouse pressed event in image selector");
        showImage(e);
      }
      public void mouseReleased(MouseEvent e){
        //System.out.println("got mouse released event in image selector");
        //showImage(e);
      }
    };
    
    add(scrollPane, BorderLayout.CENTER);
  }
  
  
  /// Highlight the correct thumbnail image.
  public void setThumb(double time){
    if (currThumbTime_!=time){
      resetBorders();
      
      for (int i=0;i<scrollingViewArea.getComponentCount();++i){
        
          ImagePanel currPanel = (ImagePanel)scrollingViewArea.getComponent(i);
          if (time == currPanel.getTimestamp()) {
            currPanel.setBorder( BorderFactory.createBevelBorder
                ( BevelBorder.LOWERED,Color.red,Color.red ) );
            break;
          }
      }
      
      currThumbTime_=time;
    }
  }
  
  private void resetBorders(){
    for(int i=0;i<scrollingViewArea.getComponentCount();i++){
     ImagePanel imagePanel = (ImagePanel)scrollingViewArea.getComponent(i);
     imagePanel.setBorder(null);
    }
  }
  
  /**
   * Adds an image to the ImageSelectorPanel.
   * 
   * @param image      the image to add
   * @param timestamp  the timestamp for the image
   */
  public void addImage(BufferedImage image, double timestamp, boolean setThumb) {
    int componentCount = scrollingViewArea.getComponentCount();
    if (componentCount > 0) {
      ImagePanel imagePanel = (ImagePanel)scrollingViewArea.getComponent(componentCount-1);
      if (imagePanel.getTimestamp() == timestamp) {
        return;
      }
    }
    
    if(fullResImageMap_.get(new Double(timestamp))!=null){
      // prevent adding dupes
      return;
    }
    
    fullResImageMap_.put(new Double(timestamp),image);
    
    // rescale the image to have a height of 80
    // and rescale the width to keep the same ratio
    int targetHeight = 80;
    double thumbRatio = (double)(targetHeight) / image.getHeight();
    int thumbHeight = (int)(image.getHeight() * thumbRatio);
    int thumbWidth = (int)(image.getWidth() * thumbRatio);
    BufferedImage scaledImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = scaledImage.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
    
    
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
    
    // no need for a high quality rendering for thumbnails
    imagePanel.setImage(scaledImage, timestamp);
    imagePanel.setHighQualityRenderingEnabled(false);
    imagePanel.setToolTipText(DataViewer.formatDate(timestamp));
    
    int addIndex=0;
    //sort images by timestamp ascending, left to right
    for (addIndex=0;addIndex<scrollingViewArea.getComponentCount();++addIndex){
      
        ImagePanel currPanel = (ImagePanel)scrollingViewArea.getComponent(addIndex);
        if (timestamp >= currPanel.getTimestamp()) {
           break;
        }
    }
    
    scrollingViewArea.add(imagePanel,addIndex);
    
    purgeImages();
    
    if(setThumb){
      setThumb(timestamp);
      
      scrollingViewArea.doLayout();
      Rectangle newImageBounds=imagePanel.getBounds();
      newImageBounds.width+=5;
      System.out.println(DataViewer.formatDate(timestamp)+" added at index:"+addIndex+
                                ", x:"+newImageBounds.x+", width:"+newImageBounds.width);
      scrollingViewArea.scrollRectToVisible(newImageBounds);
    }
    
    scrollingViewArea.revalidate();
    scrollingViewArea.repaint();
  }
  
  /**
   * Removes all the images from the image selection panel.
   */
  public void clearImages() {
    scrollingViewArea.removeAll();
    fullResImageMap_.clear();
    // revalidate the view area in order to make sure it appears empty when loading a new channel
    scrollingViewArea.revalidate();
    scrollingViewArea.repaint();
    imageDisplay_.setViewImage(null, -1); 
  }
  
  /**
   * Sets the time for the image selection panel. Images older than the time scale will be
   * removed.
   * 
   * @param time  the new time
   */
  public void setTime(double time) {
    //this.time = time;
    
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
   * Purges images that shouldn't be displayed anymore. This includes enforcing
   * the maximum number of images and aging images older than the time scale,
   * relative to the current time.
   */
  private void purgeImages() {
    if (scrollingViewArea.getComponentCount() == 0) {
      return;
    }
    
    boolean revalidate = false;
    
    while (scrollingViewArea.getComponentCount() > 1000) {
      cacheImagePanel((ImagePanel)scrollingViewArea.getComponent(0));
      
      scrollingViewArea.remove(0);
      revalidate = true;
    }
    
    double time = RBNBController.getInstance().getLocation();
    Iterator<Double> imageTimeIt=fullResImageMap_.keySet().iterator();
    while(imageTimeIt.hasNext()){
      double imageTime=imageTimeIt.next().doubleValue();
      if (imageTime <= time-timescale || imageTime > time) {
        imageTimeIt.remove();
      }
    }
    
    for (int i=scrollingViewArea.getComponentCount()-1; i>=0 ; i--) {
      ImagePanel imagePanel = (ImagePanel)scrollingViewArea.getComponent(i);
      double panelTimestamp=imagePanel.getTimestamp();
      if (panelTimestamp <= time-timescale || panelTimestamp > time) {
        System.out.println("panel: "+DataViewer.formatDate(panelTimestamp)+", start: "
            +DataViewer.formatDate(time-timescale)+
            ", end: "+DataViewer.formatDate(time));
        cacheImagePanel((ImagePanel)scrollingViewArea.getComponent(i));
        
        scrollingViewArea.remove(i);
        revalidate = true; 
      }
    }
    
    // removed check to make changing channels appear correctly
    //if (revalidate) {
      //scrollingViewArea.revalidate();
      scrollingViewArea.revalidate();
      scrollingViewArea.repaint();
      
      if (scrollingViewArea.getComponentCount() == 0) {
        imageDisplay_.setViewImage(null,-1);
      }
    //}
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
  
  class ImageSelectorLayout implements LayoutManager {

    public void addLayoutComponent(String name, Component comp) {}

    public void removeLayoutComponent(Component comp) {}
    
    public void layoutContainer(Container container) {
      int componentCount = container.getComponentCount();
      if (componentCount == 0) {
        return;
      }
      
      Dimension dimensions = container.getSize();
      Insets insets = container.getInsets();
      dimensions.width -= insets.left + insets.right;
      dimensions.height -= insets.top + insets.bottom;
      
      int width = 110;
      
      //give a border of 10 between each image
      for (int i = 0; i < componentCount; i++) {
        Component component = container.getComponent(componentCount - i - 1);
        component.setBounds(i*120, 0, width, dimensions.height);
      }
      
    }

    public Dimension minimumLayoutSize(Container container) {
      return new Dimension(0,0);
    }

    public Dimension preferredLayoutSize(Container container) {
      int componentCount = container.getComponentCount();
      if (componentCount == 0) {
        return new Dimension(0,0);
      }
      
      Dimension dimension = container.getComponent(0).getPreferredSize();
      return new Dimension(componentCount*120, 100);
    }
    
  }
}