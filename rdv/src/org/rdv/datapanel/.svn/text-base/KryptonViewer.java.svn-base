/*  KryptonViewer.java
 *  Subclass of AbstractDataPanel
 *
 *  Copyright (c) 2007 Phillip A. Stewart
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *  Author: Phillip A. Stewart
 *  Date: 
 *
 *   KryptonViewer funded by University of Minnesota
 *   For more information please contact Phillip Stewart (tub45917@temple.edu)
 */

package org.rdv.datapanel;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.media.j3d.Appearance;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.LineArray;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import org.rdv.action.ActionFactory;
import org.rdv.data.DataExporter;
import org.rdv.data.JPEGConverter;
import org.rdv.data.JPEGZipDataExporter;
import org.rdv.data.NumericDataSample;
import org.rdv.data.VisualizationSeries;
import org.rdv.rbnb.RBNBDataRequest;
import org.rdv.rbnb.SubscriptionRequest;
import org.rdv.rbnb.SubscriptionResponse;
import org.rdv.rbnb.TimeSeriesData;
import org.rdv.ui.MessagePopup;

import com.rbnb.sapi.ChannelMap;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * A data panel to display a 3D model of a structure using data from the Krypton
 * system.
 * 
 * @author Phillip A. Stewart
 * 
 */
public class KryptonViewer extends AbstractDataPanel implements JPEGConverter {
  /** The parent KryptonViewer **/
  private KryptonViewer mpanel = this;

  /** The representation of the structure and current displacements */
  private StructureModel model;

  /** Component that renders the 3D model. */
  private ModelViewer mview;

  /** Component that contains the 3D pane and the toolbars. */
  private ViewPanel viewpanel;

  /** Editor to modify live models */
  private ModelEditor editor;
  
  /** Mouse event handler for interacting with the 3D model. */
  private MousePicker mpicker;
  /** Last posted time */
  //private double lastPostedTime;

  /** actionLinks */
  // matches channel names to a set of NodeAxisPairs
  private HashMap<String, HashSet<NodeAxisPair>> actionLinks;

  private static DateFormat EXPORT_DATE_FORMAT = 
    new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss-SSS");
  
  /* Java 3D will render the scene into a Canvas3D component.
   * To perform frame captures and save the frames to disk, we keep
   * a reference to an offscreen (nonvisible) Canvas3D component that
   * can be explicitly called to render individual frames.
   */
  private Canvas3D     offScreenCanvas3D = null;
  
  /*
   * The image that is attached to the off-screen Canvas3D and
   * contains the results of screen captures
   */
  private ImageComponent2D   imageComponent = null;
  
  
  // constants for axes numbers in NodeAxisPairs
  static final int X_AXIS = 0;
  static final int Y_AXIS = 1;
  static final int Z_AXIS = 2;
  static final int X_ROT = 3;
  static final int Y_ROT = 4;
  static final int Z_ROT = 5;
  static final int STR = 6; // stress/strain "axis"
  // constants for bound edges
  static final int MIN = 0;
  static final int MAX = 1;
  
  // Value for invisible nodes in the data
  static final double INV_DATA = 1.0E-128;
  
  private LinkedHashMap<String, boolean[]> nodesXYZ = new LinkedHashMap<String, boolean[]>();
  private LinkedHashMap<String, double[]> nodesPos = new LinkedHashMap<String, double[]>();
  
  /**
   * Initializes the Model Panel. Sets up the 3D environment and controls.
   */
  public KryptonViewer() {
    super();
    
    actionLinks = new HashMap<String,HashSet<NodeAxisPair>>();
    model = new StructureModel();
    mview = new ModelViewer();
    editor = new ModelEditor();
    viewpanel = new ViewPanel();
    
    setDataComponent((JPanel) (viewpanel));
  }

  public SubscriptionRequest getSubscriptionRequest(){
    return new RBNBDataRequest(this,
        SubscriptionRequest.TYPE_TABULAR,timeScale,getSeries());
  }
  
//  public boolean addChannels(Collection<String> channelNames){
//    Iterator<String> it= channelNames.iterator();
//    while (it.hasNext()){
//      if(!addChannel(it.next())) return false;
//    }
//    return true; 
//  }
  
  /**
   * Links the channel to the currently selected node and axis from channel
   * linker. Defers to addChannel(String channelname, String node, int axis)
   * 
   * @param channelname
   *          name of the channel to add
   */

//  public boolean addChannel(String channelname) {
//    
//    return super.addChannel(channelname);
//  }
  @Override
  protected void onSeriesAdded(VisualizationSeries vs){}
  
  /**
   * Unlinks a channel from all node and axes with which it may be associated.
   */
  @Override
  protected void onSeriesRemoved(VisualizationSeries vs){
    String channelname = vs.getChannels().iterator().next();
    actionLinks.remove(channelname);
    String[] nodeName = channelname.split("/");   
    String node = nodeName[nodeName.length-1].substring(0, nodeName[nodeName.length-1].length()-2);
    //System.out.println(node);
    if(model.containsNode(node));
      model.removeNode(node);
    mview.sync();
    editor.updatescreen();
  }
  
  /**
   * Overridden from AbstractDataPanel to do extra work. Unlinks a channel from
   * all node and axes with which it may be associated.
   */
//  protected void channelRemoved(String channelname) {
//    actionLinks.remove(channelname);
//    String[] nodeName = channelname.split("/");   
//    String node = nodeName[nodeName.length-1].substring(0, nodeName[nodeName.length-1].length()-2);
//    //System.out.println(node);
//    if(model.containsNode(node));
//      model.removeNode(node);
//    mview.sync();
//    editor.updatescreen();
//  }

  /**
   * Returns whether or not multiple channels are supported. Always
   * <code>true</code> for KryptonViewer
   */
  public boolean supportsMultipleChannels() {
    return true;
  }
  
  /** Returns true if each node has a corresponding x, y, and z channel */
  public boolean hasXYZ(boolean[] b){
    if(b[0] && b[1] && b[2])
      return true;
      
    return false;
  }
  
  /**
   * @param time
   *          current time
   * postTime does nothing.
   */
  @Deprecated
  public void postTime(double time) {
    super.postTime(time);
  }
  
  /**
   * @param r Response containing data.
   * postData retrieves data from the current subscribed channels and gets new data based on the time.
   */
  public void postData(final SubscriptionResponse r) {
    //this.subResponse_ = r;
  
//    if (subResponse_==null){
//      return;
//    }
    
//    ChannelMap channelMap=subResponse_.getChannelMap();
    //TimeSeriesData tsd=r.getTimeSeries();
    // PostData drops in new data, postTime is called
    // repeatedly on those data points, iterate through them

    HashMap<String, Point3d> disp_updates = new HashMap<String, Point3d>();
    
    // iterate over subscribed channels
    for(VisualizationSeries s: getSeries()){
//    Iterator<String> i = channels.iterator();
//    while (i.hasNext()){// && channelMap != null) {
      TimeSeriesData tsd=r.getTimeSeries(s);
      if(tsd==null || !tsd.hasData()) continue;
      
      String channelName = s.getChannels().get(0);
      int channelIndex = tsd.getChannelIndex(channelName);
      if (channelIndex >= 0 && tsd.hasData(channelIndex)) // if this channel has new data
      {
        Double endTime = tsd.getEndTime(channelIndex);

//        int timeIndex = 0;
//        if (times.length > 0 && time > mpanel.time) {
//          for (timeIndex = 0; times[timeIndex] < time
//              && timeIndex < times.length - 1; timeIndex++) {}
//        }
        
        int typeID = tsd.getType(channelIndex);
        double data = 0.0;
        double x = Double.NEGATIVE_INFINITY;
        double y = Double.NEGATIVE_INFINITY;
        double z = Double.NEGATIVE_INFINITY;
        String[] names = channelName.split("/");
        int len = names.length - 1;
        String nodeName = names[len];
        int nameLen = nodeName.length();
        String chan = nodeName.substring(0, nameLen-2);
        chan=chan.replace(' ', '_');
        
        switch (typeID) {
        
        case ChannelMap.TYPE_FLOAT64:
          data = tsd.getDataAsFloat64(channelIndex,endTime);
          break;
        case ChannelMap.TYPE_FLOAT32:
          data = tsd.getDataAsFloat32(channelIndex,endTime);
          break;
        case ChannelMap.TYPE_INT64:
          data = tsd.getDataAsInt64(channelIndex,endTime);
          break;
        case ChannelMap.TYPE_INT32:
          data = tsd.getDataAsInt32(channelIndex,endTime);
          break;
        case ChannelMap.TYPE_INT16:
          data = tsd.getDataAsInt16(channelIndex,endTime);
          break;
        case ChannelMap.TYPE_INT8:
          data = tsd.getDataAsInt8(channelIndex,endTime);
          break;
        case ChannelMap.TYPE_STRING:
        case ChannelMap.TYPE_UNKNOWN:
        case ChannelMap.TYPE_BYTEARRAY:
        default:
          return;
        } // switch
        
        char c = nodeName.toLowerCase().charAt(nameLen-1);
        if(!nodesPos.containsKey(chan)){
          boolean[] temp = {false, false, false};
          nodesXYZ.put(chan, temp);
          double[] tempInt = {0.0,0.0,0.0};
          nodesPos.put(chan, tempInt);
        }
        
        
        if(data == INV_DATA){
          log.debug("got invisible point");
        }else{
          switch(c){
            case 'x':
              nodesXYZ.get(chan)[0]=true;
              nodesPos.get(chan)[0]=data;
              break;
            case 'y':
              nodesXYZ.get(chan)[1]=true;
              nodesPos.get(chan)[1]=data;
              break;
            case 'z':
              nodesXYZ.get(chan)[2]=true;
              nodesPos.get(chan)[2]=data;
              break;
          }
        }
        
        if (model.nodeCount() == 0 && hasXYZ(nodesXYZ.get(chan))) {
          model = new StructureModel();
          try {
            double[] position = nodesPos.get(chan);
            model.addNode(chan, new Point3d(position[0], position[1], position[2]));
            mview.sync();
            mview.resetScene();

          } catch (StructureModel.ModelException e) {
            log.debug(e);
          }
        }
        
        if ( hasXYZ(nodesXYZ.get(chan)) ) {
          double[] position = nodesPos.get(chan);
          disp_updates.put(chan, new Point3d(position[0], position[1], position[2]));
          //nodesXYZ.remove(chan);
          nodesPos.remove(chan);
          if (model.nodes.containsKey(chan)) {
            model.nodes.get(chan).setAlive(true);
          }

        } else if(data == INV_DATA){
          if (model.nodes.containsKey(chan)) {
            model.nodes.get(chan).setAlive(false);
          }

        }
        
        
      } // end if channelIndex !=1
    } // while channel.hasNext() (subscribed channels)

    // now walk updates and push to model and then call mview.update()
    
    Iterator<Entry<String, Point3d>> nodeIter = disp_updates.entrySet()
        .iterator();

    while (nodeIter.hasNext()) {
      Entry<String, Point3d> temp = nodeIter.next();
      String str = temp.getKey();
      Point3d pt = temp.getValue();
      
      if (model.containsNode(str) && !model.containsNode(pt)) {
        
        model.moveNode(str, pt, false);

      } else if (!model.containsNode(str) && !model.containsNode(pt)) {
        try {
          model.addNode(str, pt);
          mview.sync();
          mview.viewSync();
        } catch (StructureModel.ModelException e) {
          log.error(e);
        }
      }
      
    }
    
    mview.update();
    editor.updatescreen();
    //this.time = time;
   
//    if(tsd.free(this)){
//      subResponse_=null;
//    }
  }
  
  
  // return Color3f as #xxxxxx hex string
  private String colortohex(Color3f c) {
    String color = String.format("#%1$02x%2$02x%3$02x", c.get().getRed(), c
        .get().getGreen(), c.get().getBlue());
    return color;
  }
  /**
   * Destroys the current model definition and unlinks all channels.
   */
   public void destroyModel()
   {
     
     mview.destroyModelBranch();
     editor.deselectAll(); // deselect all
     actionLinks.clear();
     model.nodes.clear();
     removeAllChannels();
   }
   
   
  /**
   * Displays an error message in a popup window. Also echo error message to
   * stdout.
   * 
   * @param msg
   *          The error message to display.
   */
  public void popupError(String msg) {
    MessagePopup popup = MessagePopup.getInstance();
    popup.showError(msg);
    log.info(msg);
  }

  // ---------------------------- //
  // --- Begin Nested Classes --- //
  // ---------------------------- //
  
  // StructureModel represents the model: nodes and members
  /**
   * Encapsulates the definition of a model. Nodes and their positions and
   * displacements, members and their endpoints and deformation types.
   */
  class StructureModel {
    /** The list of nodes in the <code>StructureModel</code>. */
    private LinkedHashMap<String, SNode> nodes;

    /** The center point of the model */
    private Point3d centerpoint = new Point3d(0.0, 0.0, 0.0);

    /** Did the model define the center or are we calculating it */
    private boolean center_defined = false;
    
    /** Number used to Scale Model Movement */
    private double modelScale = 1.0;
    
    // Movement scale: 1.0 is Normal Movement, 5.0 is 5x normal movement
    static final double MIN_MOV_SCALE = 1.0;
    static final double MAX_MOV_SCALE = 5.0;
    /**
     * Constructs an empty <code>StructureModel</code>.
     */
    public StructureModel() {
      nodes = new LinkedHashMap<String, SNode>();
    }

    /**
     * Adds a node to the <code>StructureModel</code>. Every node must have a
     * unique name, and are required to be at unique locations. Throws
     * ModelException
     * 
     * @param s
     *          the name of the node being added
     * @param p
     *          the location of the node being added
     * @param c
     *          the color of the node being added
     * @see #addNode(String,Point3d)
     */

    public void addNode(String s, Point3d p) throws ModelException {
      if (this.containsNode(s)) {
        throw new ModelException("Node with name: " + s
            + " already exists in model.");
      } else if (this.containsNode(p)) {
        throw new ModelException("Node exists at point: " + p);
      } else if (!s.matches("[\\w_]+")) {
        throw new ModelException("Invalid characters in node name: " + s);
      } else {
        nodes.put(s, new SNode(s, p, true));
        
      }
    }

    /**
     * Remove the node <code>name</code> from the model. Also remove any members
     * that are connected to <code>name</code>.
     * 
     * @param name
     *          the name of the node to remove
     */
    public void removeNode(String name) {
      nodes.remove(name);
      
    }
    /** Returns the model scale */
    public double getScale(){
      return modelScale;
    }
    /** Sets each node's drawn position back to their original position **/
    public void reSync(){
      Iterator<String> iter = nodes.keySet().iterator();
      while(iter.hasNext()){
        String nextnode = iter.next();
        nodes.get(nextnode).drawnPosition=new Point3d(nodes.get(nextnode).position);
        
      }
      mview.update();
    }
    /** Sets the model scale */
    public void changeScale(boolean s){
      if(s && modelScale>MIN_MOV_SCALE){
        modelScale--;
      }
      else if(!s && modelScale< MAX_MOV_SCALE)
        modelScale++;
    }
    /**
     * Find the maximum or minimum values in the set of nodes along an axis.
     * 
     * @param extreme
     *          either KryptonViewer.MAX or .MIN
     * @param axis
     *          one of KryptonViewer.X_AXIS, .Y_AXIS or .Z_AXIS
     * @return a double of the extreme value for the specified axis
     */
    public double getAxisExtreme(int extreme, int axis) {
      TreeSet<Double> sort = new TreeSet<Double>();
      Iterator<String> nodeiter = model.nodes.keySet().iterator();
      while (nodeiter.hasNext()) {
        double values[] = new double[3];
        nodes.get(nodeiter.next()).getBasePosition().get(values);
        sort.add(values[axis]);
      }
      return (sort.isEmpty() ? 0
          : (extreme == MAX ? sort.last() : sort.first()));

    }

    /**
     * Returns the maximum height (z-axis parallel distance) of the model.
     * 
     * @return the maximum height (z-axis parallel distance) in the model as a
     *         <code>double</code>
     */
    public double getHeight() {
      return getAxisExtreme(MAX, Z_AXIS) - getAxisExtreme(MIN, Z_AXIS);
    }

    /**
     * Returns the maximum depth (y-axis parallel distance) of the model.
     * 
     * @return the maximum depth (y-axis parallel distance) in the model as a
     *         <code>double</code>
     */
    public double getDepth() {
      return getAxisExtreme(MAX, Y_AXIS) - getAxisExtreme(MIN, Y_AXIS);
    }

    /**
     * Returns the length of the longest diagonal in the model.
     * 
     * @return the length of the longest diagonal in the model as a
     *         <code>double</code>
     */
    public double getMaximumSize() {
      double maxDistance = 0.0;
      Object[] nodes = model.nodes.values().toArray();
      
      for (int i = 0; i < nodes.length; i++) {
        StructureModel.SNode node = (StructureModel.SNode) nodes[i];
        for (int j = i; j < nodes.length; j++) {
          maxDistance = Math.max(maxDistance, node.position
              .distance(((StructureModel.SNode) nodes[j]).position));
        }
      }
      return maxDistance;
    }

    /**
     * Returns the center point of the model. This is either set in the model
     * file or otherwise computed from the model's dimensions.
     * 
     * @return the center point of the model as a <code>Point3d</code>
     */
    public Point3d getCenter() {
      Point3d center;
      if (center_defined) {
        center = new Point3d();
        center.set(centerpoint);
      } else {
        double x = getAxisExtreme(MIN, X_AXIS)
            + (getAxisExtreme(MAX, X_AXIS) - getAxisExtreme(MIN, X_AXIS)) / 2;
        double y = getAxisExtreme(MIN, Y_AXIS)
            + (getAxisExtreme(MAX, Y_AXIS) - getAxisExtreme(MIN, Y_AXIS)) / 2;
        double z = getAxisExtreme(MIN, Z_AXIS)
            + (getAxisExtreme(MAX, Z_AXIS) - getAxisExtreme(MIN, Z_AXIS)) / 2;
        center = new Point3d(x, y, z);
      }

      return center;

    }
    /** Sets the centerpoint of the model **/
    public void setCenter(Point3d p) {
      this.centerpoint = new Point3d(p);
      center_defined = true;
    }

    /**
     * Returns <code>true</code> if the model contains a node by this name.
     * 
     * @param name
     *          the name to check for existence
     * @return <code>true</code> if the model contains a node by this name
     */
    public boolean containsNode(String name) {
      return nodes.containsKey(name);
    }

    /**
     * Returns <code>true</code> if the model contains a node at this location.
     * 
     * @param p
     *          the Point3d to check for the presence of a node
     * @return <code>true</code> if the model contains a node at this point
     */
    public boolean containsNode(Point3d p) {
      boolean found = false;
      Iterator<String> names = nodes.keySet().iterator();
      while (!found && names.hasNext()) {
        if (nodes.get(names.next()).position.equals(p)) {
          found = true;
        }
      }
      return found;
    }

    /**
     * Reports how many nodes are in the <code>StructureModel</code>
     * 
     * @return the number of nodes in the <code>StructureModel</code> as an
     *         <code>int</code>
     */
    public int nodeCount() {
      return nodes.size();
    }
    /** Reports how many nodes are alive */
    public int liveNodeCount() {
      int c=0;
      Iterator<String> iter = nodes.keySet().iterator();
      while(iter.hasNext()){
        String nextnode=iter.next();
        if(nodes.get(nextnode).isAlive()) 
          c++;
      }
      return c;
    }
    /**
     * Moves the referenced node. A relative movement means that the supplied
     * Point3d should be treated as a displacement from the node's base position
     * (not the node's currently displaced position). An absolute movement
     * changes the node's base position to the <code>Point3d</code> supplied and
     * zeroes its displacement.
     * 
     * Uses the change in x,y,z to amplify movement for a more noticable change.
     * 
     * @param name
     *          the name of the node to move
     * @param p
     *          the Point3d to move the node to or displace it by
     * @param relative
     *          <code>true</code> if this is a relative movement,
     *          <code>false</code> if this an absolute movement
     */
    public void moveNode(String name, Point3d p, boolean relative) {
      SNode node = nodes.get(name);
      Point3d oldp = node.position;
      Point3d oldDp = node.drawnPosition;
      double deltax = 0.0, deltay = 0.0, deltaz = 0.0;
      
      if(node.isAlive()){
        deltax = ((p.x)-(oldp.x)) * modelScale;
        deltay = ((p.y)-(oldp.y)) * modelScale;
        deltaz = ((p.z)-(oldp.z)) * modelScale;
      }
      if (relative) {
        node.displacement.set(p);
        node.setDrawPosition(new Point3d(oldDp.x+deltax, oldDp.y+deltay, oldDp.z+deltaz));
      } else {
        node.position.set(p);
        node.setDrawPosition(p);
        //node.setDrawPosition(new Point3d(oldDp.x+deltax, oldDp.y+deltay, oldDp.z+deltaz));
      }
    }

    /**
     * Rotates the referenced node. Rotations should be kept between -180 and
     * 180 on each axis.
     * 
     * @param name
     *          the name of the node to rotate
     * @param p
     *          the rotation to apply, around each axis
     */
    public void rotateNode(String name, Point3d p) {
      SNode node = nodes.get(name);
      node.rotation.set(p);
    }
    /**
     * Sets the initial position to the current position of the nodes
     *
     */
    public void setInitialPosition(){
      Iterator<String> names = nodes.keySet().iterator();
      while (names.hasNext()) {
        String nextnode = names.next();
        model.nodes.get(nextnode).setInitPosition();
      }
      
    }

    // ----- Begin Nested Classes within StructureModel ---//
    class SNode {
      String name;

      SNode partner;
      
      Point3d position, displacement = new Point3d();
      Point3d initPosition;
      Point3d drawnPosition;
      Point3d rotation = new Point3d();

      boolean alive;

      public SNode(String s, Point3d p, boolean a) {
        position = p;
        drawnPosition = new Point3d(p);
        alive = a;

        name = s;
      }
      
      public void setDrawPosition(Point3d p){
        drawnPosition = p;
      }
      public Point3d getPosition() {
        return position;
      }
      public void setInitPosition(){
        initPosition = new Point3d(this.position);
      }

      public Point3d getRotation() {
        return rotation;
      }

      public Point3d getBasePosition() {
        return position;
      }

      public boolean isAlive() {
        return this.alive;
      }

      public void setAlive(boolean b) {
        alive = b;
      }

      public String toString() {
        return position.toString();
      }

    }
  
  
    class ModelException extends Exception {
      String errormsg;

      ModelException(String msg) {
        errormsg = msg;
      }
    }
    // ---- End Nested Classes within StructureModel---//
  }// end of StructureModel
  /**
   * ModelViewer is responsible for drawing the model of the
   * associated {@link StructureModel} and rendering it into a canvas on screen.
   * It is call driven, so must be told to update itself, which causes it to
   * re-read values from the model and redraw the canvas.
   */
  class ModelViewer extends JPanel {

    private SimpleUniverse suni;

    // branches for sync control
    private BranchGroup modelBranch; // holds top-level TGs --> bridgeBranch

    private BranchGroup bridgeBranch; // holds node, member, and path branches

    private BranchGroup axesBranch; // holds axes

    private BranchGroup pathBranch; // holds paths of the nodes 
    // for setting up the initial view
    private Transform3D cameraTrans = new Transform3D();

    private Transform3D cameraorig;

    private Transform3D modelTrans = new Transform3D();

    private Transform3D modelorig;

    private Transform3D tiltzoomorig = new Transform3D();

    private Transform3D rotatetranslateorig = new Transform3D();

    private TransformGroup modelPlacementTG = new TransformGroup(); // used for
                                                                    // initial
                                                                    // positioning

    private TransformGroup rotatetranslateTG = new TransformGroup(); // used for
                                                                     // tilt
                                                                     // control
                                                                     // via API
                                                                     // methods
                                                                     // (screen
                                                                     // oriented)

    private TransformGroup tiltzoomTG = new TransformGroup(); // used for mouse
                                                              // and most
                                                              // transforms via
                                                              // API methods
                                                              // (model-centered)

    private MouseRotate mouseSpin = new MouseRotate();

    private MouseZoom mouseSize = new MouseZoom();

    private MouseTranslate mouseMove = new MouseTranslate();

    // where the nodes live
    // mappings of node names to nodetransforms and nodeshapes
    private HashMap<String, TransformGroup> nodetransformmap;

    private HashMap<String, Sphere> nodeshapemap;
    
    // BranchGroups for connecting nodes and members during sync.
    private BranchGroup nodebranch;

    private Color3f highlightcolor = new Color3f(1.0f, 0.7f, 0.2f);
    
    // When a node is dead, there is no data and will turn red 
    private Color3f deadColor = new Color3f(Color.RED);
    private Color3f liveColor = new Color3f(Color.GREEN);

    private String highlightnode = "";

    private Canvas3D canvas;

    private PickCanvas pcanvas;
    
    /**
     * Constructs a new <code>ModelViewer</code>
     */
    public ModelViewer() {
      nodetransformmap = new HashMap<String, TransformGroup>();
      nodeshapemap = new HashMap<String, Sphere>();
      
      setLayout(new BorderLayout());
      modelBranch = new BranchGroup();
      bridgeBranch = new BranchGroup();
      bridgeBranch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
      bridgeBranch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
      bridgeBranch.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
      
      bridgeBranch.setBoundsAutoCompute(true);

      tiltzoomTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      tiltzoomTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

      rotatetranslateTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      rotatetranslateTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

      modelPlacementTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
      modelPlacementTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

      mouseSpin.setCapability(Behavior.ALLOW_BOUNDS_WRITE);
      mouseSize.setCapability(Behavior.ALLOW_BOUNDS_WRITE);
      mouseMove.setCapability(Behavior.ALLOW_BOUNDS_WRITE);
      
      MouseWheelListener wheel = new MouseWheelListener() {

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
          int direction = e.getWheelRotation();
          direction *= -1.0;
          float f = (float) (1.0 + (float)direction * 0.1);
          mview.zoomCamera(f, true);
        }
        
      };
      
      // from java's ColorCube tutorial
      // set up Mouse Controls for zoom/spin/rotate

      // bounds were 10000.0 ... check how big they actually need to be....?
      // pixels?
      BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
          1000.0);

      // Setup and apply rotation behavior
      mouseSpin.setFactor(mouseSpin.getXFactor() * 0.6);
      mouseSpin.setTransformGroup(rotatetranslateTG);
      modelBranch.addChild(mouseSpin);
      mouseSpin.setSchedulingBounds(bounds);
      
      // Setup and apply zoom behavior
      mouseSize.setTransformGroup(tiltzoomTG);
      modelBranch.addChild(mouseSize);
      mouseSize.setSchedulingBounds(bounds);
      // Setup and apply translate behavior
      mouseMove.setTransformGroup(rotatetranslateTG);
      modelBranch.addChild(mouseMove);
      mouseMove.setSchedulingBounds(bounds);
      
      // nest view control groups.
      // tiltzoom --> rotate --> local placement and orientation
      modelBranch.addChild(tiltzoomTG);
      tiltzoomTG.addChild(rotatetranslateTG);
      rotatetranslateTG.addChild(modelPlacementTG);
      modelPlacementTG.addChild(bridgeBranch);

      canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
      
      add(canvas, BorderLayout.CENTER);
      canvas.addMouseWheelListener(wheel);
      canvas.setDoubleBufferEnable(true);
      canvas.setStereoEnable(true);
      suni = new SimpleUniverse(canvas);
      suni.getViewingPlatform().setNominalViewingTransform();

      /*
       * Following DepthBufferFreeze is magic to make lines render with correct
       * occlusion. Actually, due to the way the underlying OpenGL/hardware
       * processes decide to render things, opaque and antialiased objects are
       * rendered on different passes and the hardware buffered is updated in
       * specific ways. In general, this causes the Z-order of antialiased
       * objects to effectively be ignored, the following freeze prevents this,
       * probably sacrificing some amount of computation overhead to do so, but
       * it gives us what we want here (correctly rendered member depth) and I
       * don't see the potential performance hit causing a problem
       */
      suni.getViewer().getView().setDepthBufferFreezeTransparent(false);
      suni.addBranchGraph(modelBranch);
      
      // setup mouse picker
      pcanvas = new PickCanvas(canvas, bridgeBranch);
      pcanvas.setMode(PickTool.BOUNDS);
      mpicker = new MousePicker(pcanvas);
      
      nodebranch = new BranchGroup();
      pathBranch = new BranchGroup();
      
      nodebranch.setCapability(BranchGroup.ALLOW_PICKABLE_READ);
      nodebranch.setCapability(BranchGroup.ALLOW_AUTO_COMPUTE_BOUNDS_READ);
      nodebranch.setCapability(BranchGroup.ENABLE_PICK_REPORTING);
      
      nodebranch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
      nodebranch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
      nodebranch.setCapability(BranchGroup.ALLOW_DETACH);
      
      pathBranch.setCapability(BranchGroup.ALLOW_DETACH);
      pathBranch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
      pathBranch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
      
      axesBranch = createAxes();

      bridgeBranch.addChild(nodebranch);
      bridgeBranch.addChild(axesBranch);
      bridgeBranch.addChild(pathBranch);
      
    } // end constructor
    
    public BufferedImage saveImage() throws AWTException{
      Point temp = canvas.getLocationOnScreen();
      return new Robot().createScreenCapture(
          new Rectangle((int)temp.getX(),(int)temp.getY(),
                        canvas.getWidth(),canvas.getHeight()));
    }
    
    /**
     * This synchronizes the <code>ModelViewer</code> scenegraph object with the
     * <code>StructureModel</code>
     */
    public void sync() {

      // detach for editing
      nodebranch.detach();
      pathBranch.detach();
      
      Iterator<String> modelnodes = model.nodes.keySet().iterator();
      String nextnode;
      while (modelnodes.hasNext()) {
        nextnode = modelnodes.next();
        
        if (!nodeshapemap.containsKey(nextnode)) // can check either, sync must
                                                 // keep both maps synced in
                                                 // tandem
        {
          float radius = 0.4f;//(float) Math.max(model.getMaximumSize() / 75.0, 0.01);
          Sphere sphere = new Sphere(radius);
          
          sphere.getShape().setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
          sphere.getShape().setCapability(Shape3D.ALLOW_GEOMETRY_READ);
          sphere.getShape().setCapability(Shape3D.ALLOW_APPEARANCE_READ);
          sphere.getShape().setCapability(Shape3D.ALLOW_BOUNDS_READ);// JLM
          
          sphere.getShape().getGeometry().setCapability(Geometry.ALLOW_INTERSECT);
          
          sphere.getShape().setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
          
          sphere.getShape().setCapability(Shape3D.ALLOW_PICKABLE_READ);
          sphere.getShape().setCapability(Shape3D.ALLOW_PICKABLE_WRITE);
          sphere.getShape().setCapability(Shape3D.ALLOW_COLLISION_BOUNDS_READ);
          
          sphere.getShape().setCapability(Shape3D.ALLOW_COLLISION_BOUNDS_WRITE);
          sphere.getShape().setCapability(Shape3D.ALLOW_APPEARANCE_READ);
          
          sphere.setCapability(Geometry.ALLOW_INTERSECT);
          
          PickTool.setCapabilities(sphere.getShape(), PickTool.INTERSECT_FULL);

          Appearance ap = new Appearance();

          ap.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
          ap.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
          
          ap.setColoringAttributes(new ColoringAttributes(liveColor, ColoringAttributes.SHADE_FLAT));
          sphere.setAppearance(ap);
          
          TransformGroup tg = new TransformGroup();
          tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
          tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
          
          tg.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
          tg.setCapability(TransformGroup.ALLOW_PICKABLE_READ);
          tg.setCapability(TransformGroup.ALLOW_PICKABLE_WRITE);
          
          Transform3D tr = new Transform3D();
          tr.set(new Vector3d(model.nodes.get(nextnode).position));
          
          tg.setTransform(tr);
          tg.addChild(sphere);
          
          
          nodebranch.addChild(tg);
          nodetransformmap.put(nextnode, tg);
          nodeshapemap.put(nextnode, sphere);
          
        }
      }
      // now remove anything from the mview maps that is no longer in the model
      Iterator<String> mviewnodes = nodeshapemap.keySet().iterator();
      while (mviewnodes.hasNext()) {
        nextnode = mviewnodes.next();
        if (!model.containsNode(nextnode)) {
          // remove transformgroup from branch
          nodebranch.removeChild(nodetransformmap.get(nextnode));
          // remove from mview maps
          nodetransformmap.remove(nextnode);
          mviewnodes.remove(); // remove last pop from nodeshapemap via iterator
          
        }
      }
      bridgeBranch.addChild(nodebranch);
      bridgeBranch.addChild(pathBranch);
      update();
    }

    /**
     * Update the viewing platform and transform to reflect the model's new size
     * and shape for proper viewing and scaling. This method adjusts center and
     * scaling, but does not change the model's orientation onscreen. See
     * resetScene() for that.
     */
    public void viewSync() {
      // update mouse action bounds
      double scale = model.getMaximumSize();
      BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
          10.0 * scale);
      mouseSize.setBounds(bounds);
      mouseSize.setSchedulingBounds(bounds);
      mouseSize.setFactor(scale / 75);
      mouseMove.setBounds(bounds);
      mouseMove.setSchedulingBounds(bounds);
      mouseMove.setFactor(scale / 100);
      mouseSpin.setBounds(bounds);
      mouseSpin.setSchedulingBounds(bounds);

      // update view controls rotation bases
      // model is now upright vs. toward screen
      modelTrans.rotX(-Math.PI * 1 / 2);
      /*
       * Translate to the center of the model. Orientation is rotated versus
       * screen. y <=> z
       */
      // update model location
      
        Point3d modelcenter = model.getCenter();
        modelTrans.setTranslation(new Vector3d(-modelcenter.x, -modelcenter.z,
            modelcenter.y));

        modelPlacementTG.setTransform(modelTrans);
        modelorig = new Transform3D(modelTrans); // save for resetScene

        // rotate the initial viewpoint based on height vs. depth
        // tilt a low height, high depth model towards camera for better viewing
        double height = model.getHeight();
        double depth = model.getDepth();
        tiltzoomorig.rotX((1.0 - (depth > height ? height / depth : 1.0))
            * Math.PI / 4.0); // prep for resetScene
        tiltzoomTG.setTransform(tiltzoomorig);

        // update viewingplatform location
        // zero nodes, one node, no height, large
        Point3d eye = new Point3d();
        double maxsize = model.getMaximumSize();
        if (maxsize > 0) {
          Iterator<String> points = model.nodes.keySet().iterator();
          double sw = (double) canvas.getWidth(); // fov is based on sw, need to
                                                  // scale for fov relative to
                                                  // screen height
          double sh = (double) canvas.getHeight();
          double borderFactor = 1.1;
          double screenRatioAdjust = Math.max(1.0, sw / sh);
          BoundingSphere b = new BoundingSphere(modelcenter, maxsize / 10.0);
          while (points.hasNext()) {
            Point3d np = model.nodes.get(points.next()).getBasePosition();
            b.combine(np);
          }
          eye.set(0, 0, b.getRadius() * borderFactor * screenRatioAdjust
              / Math.tan(suni.getViewer().getView().getFieldOfView() / 2));
        } else // for zero or one nodes
        {
          eye.set(0.0, 0, 1.75);
        }

        // set clipping distances
        suni.getViewer().getView().setBackClipDistance(eye.z * 2);
        suni.getViewer().getView().setFrontClipDistance(eye.z * 2 / 500.0);

        cameraTrans.lookAt(eye, new Point3d(), new Vector3d(0, 1, 0));
        cameraTrans.invert();
        cameraorig = new Transform3D(cameraTrans);
        suni.getViewingPlatform().getViewPlatformTransform().setTransform(
            cameraTrans);
        // also take the mouse TG back to regular zoom (but leave
        // translation/rotation as-is)
        Transform3D delta = new Transform3D();
        tiltzoomTG.getTransform(delta);
        delta.setScale(1.0);
        tiltzoomTG.setTransform(delta);
      
      // update axes scale

      axesBranch.detach();
      axesBranch = createAxes();
      bridgeBranch.addChild(axesBranch);

    }

    /**
     * Update the canvas with new values from the model This is not optimized at
     * all, so should only be called when there are new values in the model to
     * avoid a performance penalty.
     */
    public void update() {
      updateNodes();
    }
    
    public void addCanvas3D(Canvas3D offScreenCanvas3D){
      suni.getViewer().getView().addCanvas3D(offScreenCanvas3D);
    }
    
//    public void renderOffscreen(ImageComponent2D imageComponent){
//      //update();
//      canvas.setOffScreenBuffer( imageComponent );
//      canvas.renderOffScreenBuffer();
//      canvas.waitForOffScreenRendering();
//      
//    }

    /**
     * Destroys the model currently being displayed
     */
    public void destroyModelBranch() {
      bridgeBranch.removeAllChildren();
      nodebranch.removeAllChildren();

      if (!(nodetransformmap == null)) {
        nodetransformmap.clear();
      }
      if (!(nodeshapemap == null)) {
        nodeshapemap.clear();
      }
      resetScene();
    }

    // axes scaled by (1/32) of max_size
    private BranchGroup createAxes() {
      double scale = (16.0 / 32.0) * model.getMaximumSize();// Changed to 32/32
                                                            // for larger axis
      float label_scale = (float) ((1.0 / 16.0) * model.getMaximumSize());
      Matrix3f matrix = new Matrix3f(1, 0, 0, 0, 1, 0, 0, 0, 1);

      BranchGroup axes = new BranchGroup();
      Point3d mc = new Point3d(0.0,0.0,0.0);//model.getCenter();

      TransformGroup tg = new TransformGroup(new Transform3D(matrix,
          new Vector3d(mc.x, mc.y, mc.z), 1.0));
      // model.getAxisExtreme(KryptonViewer.MIN, KryptonViewer.Z_AXIS)
      axes.addChild(tg);
      axes.setCapability(BranchGroup.ALLOW_DETACH);
      axes.setCapability(Group.ALLOW_CHILDREN_WRITE);
      axes.setCapability(Group.ALLOW_CHILDREN_READ);
      axes.setCapability(Group.ALLOW_CHILDREN_EXTEND);
      Color3f blue = new Color3f(0.0f, 0.0f, 1.0f);

      LineArray x_axis = new LineArray(2, LineArray.COORDINATES
          | LineArray.COLOR_3);
      x_axis.setCoordinate(0, new Point3f((float) (-scale), 0.0f, 0.0f));
      x_axis.setCoordinate(1, new Point3f((float) (scale), 0.0f, 0.0f));
      x_axis.setColor(0, blue);
      x_axis.setColor(1, blue);
      tg.addChild(new Shape3D(x_axis));

      LineArray y_axis = new LineArray(2, LineArray.COORDINATES
          | LineArray.COLOR_3);
      y_axis.setCoordinate(0, new Point3f(0.0f, (float) (-scale), 0.0f));
      y_axis.setCoordinate(1, new Point3f(0.0f, (float) (scale), 0.0f));
      y_axis.setColor(0, blue);
      y_axis.setColor(1, blue);
      tg.addChild(new Shape3D(y_axis));

      LineArray z_axis = new LineArray(2, LineArray.COORDINATES
          | LineArray.COLOR_3);
      z_axis.setCoordinate(0, new Point3f(0.0f, 0.0f, (float) (-scale)));
      z_axis.setCoordinate(1, new Point3f(0.0f, 0.0f, (float) (scale)));
      z_axis.setColor(0, blue);
      z_axis.setColor(1, blue);
      tg.addChild(new Shape3D(z_axis));

      // labels
      Text2D x_label = new Text2D("x", new Color3f(1.0f, 0.0f, 0.0f),
          "SansSerif", 150, java.awt.Font.BOLD);
      x_label.setRectangleScaleFactor(x_label.getRectangleScaleFactor()
          * label_scale);
      // was scale, -0.25,
      TransformGroup xtg = new TransformGroup(new Transform3D(matrix,
          new Vector3d(scale / 16.0, 0, 0), 1.0));
      xtg.addChild(x_label);
      tg.addChild(xtg);
      Text2D y_label = new Text2D("y", new Color3f(1.0f, 0.0f, 0.0f),
          "SansSerif", 150, java.awt.Font.BOLD);
      y_label.setRectangleScaleFactor(y_label.getRectangleScaleFactor()
          * label_scale);
      // was -0.25,scale,
      TransformGroup ytg = new TransformGroup(new Transform3D(matrix,
          new Vector3d(0, scale / 16.0, 0), 1.0));
      ytg.addChild(y_label);
      tg.addChild(ytg);
      Text2D z_label = new Text2D("z", new Color3f(1.0f, 0.0f, 0.0f),
          "SansSerif", 150, java.awt.Font.BOLD);
      z_label.setRectangleScaleFactor(z_label.getRectangleScaleFactor()
          * label_scale);
      TransformGroup ztg = new TransformGroup();
      Transform3D z_trans = new Transform3D(new Matrix3f(-1, 0, 0, 0, 0, -1, 0,
          -1, 0), new Vector3d(0, 0, 1.5 * scale / 16.0), 1.0);
      ztg.setTransform(z_trans);
      ztg.addChild(z_label);
      tg.addChild(ztg);
      // turn off picking for all axes shapes
      for (int i = 0; i < axes.numChildren(); i++) {
        axes.getChild(i).setPickable(false);
      }
      return axes;
    }
/**
 * Updates the position of nodes and sets the colors.
 * 
 */
    private void updateNodes() {
      Sphere s = new Sphere((float) Math.max(model.getMaximumSize() / 100.0,0.01));
      Transform3D tr;
      Iterator<String> nodes = model.nodes.keySet().iterator();
      
      while (nodes.hasNext()) {
        String nextnode = nodes.next();
        Appearance ap = new Appearance();
        
        tr = new Transform3D();
        Point3d newloc;
        if(model.nodes.get(nextnode).drawnPosition!=null)
          newloc = new Point3d(model.nodes.get(nextnode).drawnPosition);
        else
          newloc = new Point3d(model.nodes.get(nextnode).position);
        
        
        tr.set(new Vector3d(newloc));
        nodetransformmap.get(nextnode).setTransform(tr);
        if ( nextnode.equals(highlightnode) ) {
          ColoringAttributes c = new ColoringAttributes(highlightcolor, ColoringAttributes.SHADE_FLAT);
          ap.setColoringAttributes(c);
          nodeshapemap.get(nextnode).setAppearance(ap);
          ap=null;
        } else{
          
          if (model.nodes.get(nextnode).isAlive()) {
            ColoringAttributes c = new ColoringAttributes(liveColor, ColoringAttributes.SHADE_FLAT);
            ap.setColoringAttributes(c);
            nodeshapemap.get(nextnode).setAppearance(ap);

          }else {
            ColoringAttributes c = new ColoringAttributes(deadColor, ColoringAttributes.SHADE_FLAT);
            ap.setColoringAttributes(c);
            nodeshapemap.get(nextnode).setAppearance(ap);
          } 
        }
        
        
        ap=null;
      }
      
    }
    
    
    /**
     * Get the <code>String</code> name for the <code>Node</code> specified
     * 
     * @param s
     *          the <code>Node</code> to find the name of
     * @return the name of the node as a <code>String</code>
     */
    public String getNodeName(Node s) {
      Iterator<String> names = nodeshapemap.keySet().iterator();
      boolean found = false;
      String next = "";
      while (names.hasNext() && !found) {
        next = names.next();
        if ((Node) nodeshapemap.get(next) == (Node) s) {
          found = true;
        }
      }
      return next;
    }


    /**
     * Rotates the displayed model around its own z-axis
     * 
     * @param degrees
     *          the number of degrees to rotate the model
     * @param relative
     *          whether this rotation is relative to current position or
     *          absolute (relative to initial position)
     */
    public void rotateModel(float degrees, boolean relative) {
      Transform3D current = new Transform3D();
      Transform3D delta = new Transform3D();
      rotatetranslateTG.getTransform(current);
      if (!relative) // if not relative, reset rotation to zero before multiply
      {
        current.rotY(0);
      }
      delta.rotY(degrees / 180 * Math.PI);
      current.mul(delta);
      rotatetranslateTG.setTransform(current);
    }

    /**
     * Tilts the displayed model around its own y-axis towards the viewer
     * 
     * @param degrees
     *          the number of degrees to rotate the model
     * @param relative
     *          whether this tilt is relative to current position or absolute
     *          (relative to initial position)
     */
    public void tiltModel(float degrees, boolean relative) {
      Transform3D delta = new Transform3D();
      Transform3D current = new Transform3D();
      tiltzoomTG.getTransform(current);
      if (!relative) {
        current.rotX(0);
      }
      delta.rotX(degrees / 180f * Math.PI);
      current.mul(delta);
      tiltzoomTG.setTransform(current);
    }

    /**
     * Zooms the camera in or out
     * 
     * @param factor
     *          scale percentage (>1.0 to zoom in, <1.0 to zoom out)
     * @param relative
     *          whether this zoom is relative to current position or absolute
     *          (relative to initial position)
     */
    public void zoomCamera(float factor, boolean relative) {
      Transform3D delta = new Transform3D();
      tiltzoomTG.getTransform(delta);
      if (relative) {
        delta.setScale(delta.getScale() * factor);
      } else {
        delta.setScale(factor);
      }
      tiltzoomTG.setTransform(delta);
    }

    /**
     * Resets the camera to its initial viewpoint.
     */
    public void resetScene() {
      model.center_defined=false;
      viewSync();
      tiltzoomTG.setTransform(tiltzoomorig);
      modelPlacementTG.setTransform(modelorig);
      rotatetranslateTG.setTransform(rotatetranslateorig);
      suni.getViewingPlatform().getViewPlatformTransform().setTransform(
          cameraorig);
    }

    /**
     * Highlights the specified node in the model
     * 
     * @param nodename
     *          the name of the node to highlight
     */
    public void highlightNode(String nodename) {
      highlightnode = nodename;
      updateNodes();
    }

    /**
     * Set the color used to highlight nodes or members.
     * 
     * @param c
     *          the <code>Color3f</code> to user for highlighting
     */
    public void setHighlightColor(Color3f c) {
      highlightcolor = c;
    }
      
    
  }// end ModelViewer

  
  
  /**
   * This class groups the model canvas and the toolbars together into one
   * component that can be used as the <code>dataComponent</code> for the
   * {@link DataPanel} interface.
   */
  class ViewPanel extends JPanel implements ActionListener {

    private JMenuBar menubar = new JMenuBar();
    
    JMenu viewmenu = new JMenu("View");
    JMenu exportMenu = new JMenu("Export");
    JMenu toolmenu = new JMenu("Tools");
    
    private JMenuItem resetView = new JMenuItem("Reset View");
    private JMenuItem syncNodes = new JMenuItem("Re-Sync Nodes");
    
    private JMenuItem exportJPEGRange = new JMenuItem("JPEG Range");
    //private JMenuItem exportJPEG = new JMenuItem("Export JPEG");

    private JMenuItem frontview = new JMenuItem("Front View");

    private JMenuItem topview = new JMenuItem("Top View");

    private JMenuItem cornerview = new JMenuItem("Corner View");

    private JMenuItem ccw15 = new JMenuItem("Rotate CCW 15°");

    private JMenuItem cw15 = new JMenuItem("Rotate CW 15°");

    private JMenuItem ccw90 = new JMenuItem("Rotate CCW 90°");

    private JMenuItem cw90 = new JMenuItem("Rotate CW 90°");

    private JMenuItem deselect = new JMenuItem("Deselect All");

    private JMenuItem zoomin = new JMenuItem("Zoom In");

    private JMenuItem zoomout = new JMenuItem("Zoom Out");

    private JButton zoominbutton = new JButton("+");

    private JButton zoomoutbutton = new JButton("-");

    private JButton ccw90b = new JButton("CCW");

    private JButton ccw15b = new JButton("CCW");

    private JButton cw90b = new JButton("CW");

    private JButton cw15b = new JButton("CW");

    private JLabel fifteen = new JLabel("15°");

    private JLabel ninety = new JLabel("90°");

    private JPanel bottompane = new JPanel(new BorderLayout());

    private JPanel viewcontrols = new JPanel(new GridBagLayout());

    /** reference for edit pane */
    // should look into rebuilding this so that axis selection is replace by
    // edit tools
    private JPanel editpanel;

    /**
     * Constructs a new <code>ViewPanel</code>.
     */
    public ViewPanel() {
      setLayout(new BorderLayout());
      
      toolmenu.add(resetView);
      toolmenu.add(syncNodes);
      
      //recordMenu.add(exportJPEG);
      exportMenu.add(exportJPEGRange);
      viewmenu.add(frontview);
      viewmenu.add(topview);
      viewmenu.add(cornerview);
      viewmenu.addSeparator();
      viewmenu.add(deselect);
      viewmenu.addSeparator();
      viewmenu.add(zoomin);
      zoomin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
          KeyEvent.CTRL_DOWN_MASK));
      viewmenu.add(zoomout);
      zoomout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
          KeyEvent.CTRL_DOWN_MASK));
      viewmenu.add(ccw15);
      ccw15.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET,
          KeyEvent.CTRL_DOWN_MASK));
      viewmenu.add(cw15);
      cw15.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET,
          KeyEvent.CTRL_DOWN_MASK));
      viewmenu.add(ccw90);
      ccw90.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET,
          KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
      viewmenu.add(cw90);
      cw90.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET,
          KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
      
      // must disable lightweight popup to get menus to appear 
      // over 3d display panel
      viewmenu.getPopupMenu().setLightWeightPopupEnabled(false);
      exportMenu.getPopupMenu().setLightWeightPopupEnabled(false);
      toolmenu.getPopupMenu().setLightWeightPopupEnabled(false);
      
      menubar.add(viewmenu);
      menubar.add(toolmenu);
      menubar.add(exportMenu);
      
      // configure the buttons
      Insets inset = new Insets(1, 3, 1, 3);
      zoominbutton.setFocusPainted(false);
      zoominbutton.setMargin(inset);
      zoomoutbutton.setFocusPainted(false);
      zoomoutbutton.setMargin(inset);
      ccw15b.setFocusPainted(false);
      ccw15b.setMargin(inset);
      cw15b.setFocusPainted(false);
      cw15b.setMargin(inset);
      ccw90b.setFocusPainted(false);
      ccw90b.setMargin(inset);
      cw90b.setFocusPainted(false);
      cw90b.setMargin(inset);
      // creating the view control button panel
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 0;
      viewcontrols.add(zoominbutton, c);
      c.gridx = 2;
      viewcontrols.add(zoomoutbutton, c);
      c.gridy = 1;
      c.gridx = 0;
      viewcontrols.add(ccw15b, c);
      c.gridx = 1;
      viewcontrols.add(fifteen, c);
      c.gridx = 2;
      viewcontrols.add(cw15b, c);
      c.gridx = 0;
      c.gridy = 2;
      viewcontrols.add(ccw90b, c);
      c.gridx = 1;
      viewcontrols.add(ninety, c);
      c.gridx = 2;
      viewcontrols.add(cw90b, c);

      menubar.addMouseListener(getPopupMenuMouseListener());
      add(menubar, BorderLayout.NORTH);
      add(mview, BorderLayout.CENTER);
      editpanel = editor.getAxisPanel();
      add(bottompane, BorderLayout.SOUTH);
      viewcontrols.setBorder(BorderFactory.createCompoundBorder(BorderFactory
          .createMatteBorder(0, 2, 0, 0, Color.DARK_GRAY), BorderFactory
          .createEmptyBorder(1, 2, 1, 2)));
      viewcontrols.addMouseListener(getPopupMenuMouseListener());
      bottompane.add(editpanel, BorderLayout.CENTER);
      bottompane.add(viewcontrols, BorderLayout.EAST);

      // adding listeners
      syncNodes.addActionListener(this);
      resetView.addActionListener(this);
      //exportJPEG.addActionListener(this);
      exportJPEGRange.addActionListener(this);
      zoomin.addActionListener(this);
      zoomout.addActionListener(this);
      frontview.addActionListener(this);
      topview.addActionListener(this);
      cornerview.addActionListener(this);
      deselect.addActionListener(this);
      zoominbutton.addActionListener(this);
      zoomoutbutton.addActionListener(this);
      cw15b.addActionListener(this);
      ccw15b.addActionListener(this);
      cw90b.addActionListener(this);
      ccw90b.addActionListener(this);
      ccw15.addActionListener(this);
      cw15.addActionListener(this);
      ccw90.addActionListener(this);
      cw90.addActionListener(this);

    }// end constructor

    //Utility method to create a Canvas3D GUI component. The Canvas3D
    //is used by Java 3D to output rendered frames.
    protected Canvas3D createCanvas3D( boolean offscreen )
    {
     /*
      * First we query Java 3D for the available device information.
      * We set up a GraphicsConfigTemplate3D and specify that we would
      * prefer a device configuration that supports antialiased output.
      */
     GraphicsConfigTemplate3D gc3D = new GraphicsConfigTemplate3D();
     gc3D.setSceneAntialiasing( GraphicsConfigTemplate.PREFERRED );

     //We then get a list of all the screen devices for the
     //local graphics environment
     GraphicsDevice gd[] = GraphicsEnvironment.
                            getLocalGraphicsEnvironment().
                             getScreenDevices();

     //We select the best configuration supported by the first screen
     //device, and specify whether we are creating an onscreen or
     //an offscreen Canvas3D.
     Canvas3D c3d = new Canvas3D(gd[0].getBestConfiguration(gc3D), 
                   offscreen );

    /*
     * Here we have hard-coded the initial size of the Canvas3D.
     * However, because we have used a BorderLayout layout algorithm,
     * this will be automatically resized to fit�-as the parent JFrame
     * is resized.
     */
     //c3d.setSize( 500, 500 );

     return c3d;
    }
    
    //Utility method that performs some additional initialization
    //for an offscreen Canvas3D.
    protected Canvas3D createOffscreenCanvas3D(int offScreenWidth, int offScreenHeight)
    {
     //First we create a Canvas3D and specify that it is to be used
     //for offscreen rendering.
     
     if(offScreenCanvas3D == null){   
       offScreenCanvas3D = createCanvas3D(true);
       // add to View so we can get the scene rendered
       mview.addCanvas3D(offScreenCanvas3D);
       
     }
     
     //We then need to explicitly set the size of the off screen
     //Canvas3D.
     offScreenCanvas3D.getScreen3D().setSize( offScreenWidth,
                                              offScreenHeight );

     //This calculation returns the physical size of the screen and
     //is based on 90 display pixels per inch
     offScreenCanvas3D.getScreen3D().
      setPhysicalScreenHeight( 0.0254/90 * offScreenHeight );
     offScreenCanvas3D.getScreen3D().
      setPhysicalScreenWidth( 0.0254/90 * offScreenWidth );

     //We then create an AWT RenderedImage that the Canvas3D will
     //render into. We create a simple 3 Byte RGB format image.
     RenderedImage renderedImage =
      new BufferedImage( offScreenWidth, offScreenHeight, 
                         BufferedImage.TYPE_3BYTE_BGR );

     //The AWT RenderedImage needs to be wrapped in a Java 3D
     //ImageComponent2D before it can be assigned to the
     //Canvas3D for rendering
     imageComponent =
      new ImageComponent2D( ImageComponent.FORMAT_RGB8,
                            renderedImage );

     //This call notifies Java 3D that we require read-access to the
     //ImageComponent2D. We will be reading the pixels in the image
     //when we output it to disk.
     imageComponent.setCapability( ImageComponent2D.ALLOW_IMAGE_READ );

     //Finally, we assign the ImageComponent2D to the offscreen
     //Canvas3D for rendering
     offScreenCanvas3D.setOffScreenBuffer( imageComponent );
     
     return offScreenCanvas3D;
    }
    
//    public void saveImage(double imageTime){
//      
//      createOffscreenCanvas3D(mview.getWidth(),mview.getHeight());
//      //Called to render the scene into the offscreen Canvas3D and
//      //save the image (as a JPEG) to disk.
//      
//       offScreenCanvas3D.renderOffScreenBuffer();
//       offScreenCanvas3D.waitForOffScreenRendering();
//       //System.out.println( "Rendered to offscreen" );
//       
//       try
//       {
//         Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
//         ImageWriter writer=(ImageWriter)iter.next();
//         
//         // instantiate an ImageWriteParam object with default compression options
//         ImageWriteParam iwp = writer.getDefaultWriteParam();
//         
//         iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//         iwp.setCompressionQuality(1.0f);   // an integer between 0 and 1
//         // 1 specifies minimum compression and maximum quality
//         
//         FileUtils.forceMkdir(new File(exportDir));
//         File file = new File(exportDir+File.separatorChar+
//             EXPORT_DATE_FORMAT.format(new Date(((long)(imageTime*1000))))+".jpg");
//         //int i=0;
//         //while(file.isFile()){
//         //  file = new File("krypton3d_"+(i++)+".jpg");
//         //}
//         FileImageOutputStream output = new FileImageOutputStream(file);
//         writer.setOutput(output);
//         IIOImage image = new IIOImage(imageComponent.getImage(), null, null);
//         writer.write(null, image, iwp);
//         //writer.endWriteSequence();
//         writer.dispose();
//         output.flush();
//         output.close();
//         
//         //log.info( "Saved image at "+file.getCanonicalPath());
////        FileOutputStream fileOut = new FileOutputStream( "krypton3d.jpg" );
////
////        JPEGImageEncoder encoder =
////         JPEGCodec.createJPEGEncoder( fileOut );
////        encoder.setJPEGEncodeParam(arg0);
////        
////        encoder.encode( imageComponent.getImage() );
////        
////        fileOut.flush();
////        fileOut.close();
//       }catch( Exception e ){
//         e.printStackTrace();
//         System.err.println( "Failed to save image!");
//       }
//      
//    }

    public void writeJPEG(ImageOutputStream out) throws IOException{
      
      createOffscreenCanvas3D(mview.getWidth(),mview.getHeight());
      //Called to render the scene into the offscreen Canvas3D and
      //save the image (as a JPEG) to disk.
      
       offScreenCanvas3D.renderOffScreenBuffer();
       offScreenCanvas3D.waitForOffScreenRendering();
       //System.out.println( "Rendered to offscreen" );
       
       //try
       //{
         Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
         ImageWriter writer=(ImageWriter)iter.next();
         
         // instantiate an ImageWriteParam object with default compression options
         ImageWriteParam iwp = writer.getDefaultWriteParam();
         
         iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
         iwp.setCompressionQuality(1.0f);   // an integer between 0 and 1
         // 1 specifies minimum compression and maximum quality
         
         //FileUtils.forceMkdir(new File(exportDir));
         //File file = new File(exportDir+File.separatorChar+
         //    EXPORT_DATE_FORMAT.format(new Date(((long)(imageTime*1000))))+".jpg");
         //int i=0;
         //while(file.isFile()){
         //  file = new File("krypton3d_"+(i++)+".jpg");
         //}
         //FileImageOutputStream output = new FileImageOutputStream(file);
         writer.setOutput(out);
         IIOImage image = new IIOImage(imageComponent.getImage(), null, null);
         writer.write(null, image, iwp);
         //writer.endWriteSequence();
         writer.dispose();
         out.flush();
         //output.close();
         
         //log.info( "Saved image at "+file.getCanonicalPath());
//        FileOutputStream fileOut = new FileOutputStream( "krypton3d.jpg" );
//
//        JPEGImageEncoder encoder =
//         JPEGCodec.createJPEGEncoder( fileOut );
//        encoder.setJPEGEncodeParam(arg0);
//        
//        encoder.encode( imageComponent.getImage() );
//        
//        fileOut.flush();
//        fileOut.close();
//       }catch( Exception e ){
//         e.printStackTrace();
//         System.err.println( "Failed to save image!");
//       }
       
      
    }
    /**
     * Handles the button presses and notifies the <code>ModelViewer</code> of
     * what to do
     * 
     * @param e
     *          the action event that occurred
     */
    public void actionPerformed(ActionEvent e) {
      // check view buttons first
      Object source = e.getSource();
//      if (source == exportJPEG){
//        // Could not complete in REU Schedule
//        saveImage(time);
//      } else if (source == exportJPEGRange){
      if (source == exportJPEGRange){
        // Could not complete in REU Schedule
        AbstractAction a=ActionFactory.getInstance().getDataExportAction(
                    seriesList_.getChannels(), Arrays.asList(new DataExporter[]{
                        new JPEGZipDataExporter(KryptonViewer.this)}));
        a.actionPerformed(null);
      } else if (source == frontview) {
        mview.resetScene();
      } else if (source == topview) {
        mview.resetScene();
        mview.tiltModel(90, false);
      } else if (source == cornerview) {
        mview.resetScene();
        mview.rotateModel(-35, false); // reset and initial modification
        mview.tiltModel(20, true); // second modification must be relative
        mview.zoomCamera(0.9f, true);
      } else if (source == zoomin || source == zoominbutton) {
        mview.zoomCamera(1.1f, false);
      } else if (source == zoomout || source == zoomoutbutton) {
        mview.zoomCamera(0.9f, false);
      } else if (source == cw15b || source == cw15) {
        mview.rotateModel(-15, true);
      } else if (source == ccw15b || source == ccw15) {
        mview.rotateModel(15, true);
      } else if (source == ccw90b || source == ccw90) {
        mview.rotateModel(90, true);
      } else if (source == cw90b || source == cw90) {
        mview.rotateModel(-90, true);
      } else if (source == deselect) {
        editor.deselectAll(); // deselect all
      } else if (source == resetView) {
        mview.resetScene();
      } else if (source == syncNodes) {
        model.reSync();
      }
    } // end actionPerformed()
  }

  /**
   * Responsible for controlling the listeners in the view area for mouse
   * selection of the model elements.
   */
  class MousePicker{
    PickCanvas pcanvas;

    JPopupMenu axismenu, nodemenu, membermenu, nconnmenu;

    JLabel memberlabel, nodelabel, nconnlabel;

    MouseAdapter currentpicker;

    
    // node picker
    MouseAdapter npick = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        Node s = getFinalPick(e, PickResult.PRIMITIVE);       
        if (s != null) {
          
          String name = mview.getNodeName(s);
          editor.selectNode(name);
          if (e.getButton() == MouseEvent.BUTTON3) {
            nodelabel.setText(name + ": "
                + model.nodes.get(name).getBasePosition());
            nodemenu.show(pcanvas.getCanvas(), e.getX() + 8, e.getY() + 8);
          }
          
        } else { // picked nothing --> deselect
          //editor.deselectAll();
        }
        editor.updatescreen();
      }
   
    };
    
    // axis menu listener
    MouseAdapter amlisten = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        editor.selectedaxis = axismenu.getComponentIndex((Component) e
            .getSource());
        editor.updatescreen();
      }
    };

    // node menu listener
    MouseAdapter nmlisten = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        model.removeNode(editor.selectednode);
        mview.sync();
        mview.viewSync();
        editor.updatescreen();
      }
    };
    
   
      
   
    /**
     * Creates a new <code>MousePicker</code> for controlling picking on
     * <code>pc</code>
     * 
     * @param pc
     *          the <code>PickCanvas</code> that this <code>MousePicker</code>
     *          manages picking for.
     */
    public MousePicker(PickCanvas pc) {
      this.pcanvas = pc;
      
      pcanvas.setTolerance(6.0f); // 6 pixel tolerance
      axismenu = new JPopupMenu();
      axismenu.setLightWeightPopupEnabled(false);
      axismenu.add("X-Axis");
      axismenu.add("Y-Axis");
      axismenu.add("Z-Axis");
      axismenu.add("X Rotation Axis");
      axismenu.add("Y Rotation Axis");
      axismenu.add("Z Rotation Axis");
      axismenu.add("Stress/Strain");
      axismenu.getComponent(6).setEnabled(false);
      for (int i = 0; i < axismenu.getComponentCount(); i++) {
        axismenu.getComponent(i).addMouseListener(amlisten);
      }

      nodelabel = new JLabel("<nodename> <pos>");
      nodelabel.setFont(new Font("Sans Serif", Font.BOLD, 14));
      nodemenu = new JPopupMenu();
      nodemenu.setLightWeightPopupEnabled(false);
      nodemenu.add(nodelabel);
      nodemenu.add("Remove");
      for (int i = 1; i < nodemenu.getComponentCount(); i++) {
        nodemenu.getComponent(i).addMouseListener(nmlisten);
      }
      
    }

    /**
     * Update the picking mode based on the new mode
     * 
     * @param mode
     *          the new mode that picking should be handled for, should be one
     *          of <code>ModelEditor.AXIS, .NODE, .MEMBER, .SNODE</code>
     */
    public void updatePickMode(int mode) {
      pcanvas.getCanvas().removeMouseListener(currentpicker);      
      currentpicker = npick;       
      pcanvas.getCanvas().addMouseListener(currentpicker);
    }

    // deduce the result of picking based on the type of Node requested and the
    // mouse location
    // from the pickAllSorted
    private Node getFinalPick(MouseEvent e, int type) {
      
      pcanvas.setShapeLocation(e);
      
      PickResult[] picks = pcanvas.pickAllSorted();
      Node p = null;
      
      
      if (picks != null) {
        int index = 1;
       
        p = picks[0].getNode(type);
        while (p == null && index < picks.length) { // find the closest Node of
                                                  // requested type
          p = picks[index].getNode(type);
          index++;
        }
      }
      
      return p;
    }

    
  }

  /**
   * This class manages the editing of live models in the panel.
   */
  class ModelEditor implements ActionListener {
    /*
     * Currently selected node index, <code>-1</code> if no node is currently
     * selected, otherwise between -1 and totalnodes.
     */
    String selectednode = "";

    int selectedmember = 0;

    /*
     * Currently selected axis, should be one of
     * <code>KryptonViewer.X_AXIS</code>, <code>KryptonViewer.Y_AXIS</code>,
     * <code>KryptonViewer.Z_AXIS</code>, <code>KryptonViewer.X_ROT</code>,
     * <code>KryptonViewer.Y_ROT</code>, <code>KryptonViewer.Z_ROT</code>,
     * <code>KryptonViewer.STR</code> or -1 if none selected
     */
    int selectedaxis = -1;

    // mode indices
    public static final int AXIS = 0, NODE = 1, MEMBER = 2, SNODE = 3;

    private int mode = AXIS;

    private boolean viewLock = false;

    private JPanel axispane;

    private JButton addnode;

    private JButton next_node = new JButton(">");
    private JButton prev_node = new JButton("<");
    private JButton next_view = new JButton(">");
    private JButton prev_view = new JButton("<");

    private JLabel scaleLabel = new JLabel("Scale: "+ model.getScale()+"x");
 
    private JLabel apnodename = new JLabel(" - ");

    private JLabel apchanname = new JLabel("--");

    private JLabel nodeInfo = new JLabel("--  --  --");

    private Vector<String> basecolors;

    public ModelEditor() {
      GridBagLayout grid = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();
      basecolors = new Vector<String>();
      basecolors.add("blue");
      basecolors.add("green");
      basecolors.add("red");
      basecolors.add("yellow");
      basecolors.add("cyan");
      basecolors.add("pink");
      basecolors.add("magenta");
      basecolors.add("white");
      basecolors.add("gray");

      // axispane
      JLabel nodelabel = new JLabel("Node: ");
      next_node.setFocusPainted(false);
      prev_node.setFocusPainted(false);
      
      JLabel spacer = new JLabel();

      axispane = new JPanel(grid);
      axispane.addMouseListener(getPopupMenuMouseListener());
      c.anchor = GridBagConstraints.LINE_START;
      c.gridx = 0;
      c.gridy = 0;
      c.insets = new Insets(2, 2, 2, 2);
      c.gridwidth = 8;
      c.fill = GridBagConstraints.BOTH;
      c.gridy = 1;
      c.gridwidth = 1;
      c.fill = GridBagConstraints.NONE;
      axispane.add(prev_node, c);
      c.gridx = 1;
      axispane.add(next_node, c);

      c.gridx = 4;
      axispane.add(nodeInfo, c);

      c.gridx = 2;
      axispane.add(nodelabel, c);
      c.gridx = 3;
      axispane.add(apnodename, c);
      c.gridx = 4;
      c.weightx = 1.0;
      axispane.add(spacer, c);
      c.gridx = 5;
      c.weightx = 0;
      c.gridx = 5;
      //c.gridy = 1;
      
      // second row
      c.gridy = 2;
      c.gridx = 0;
      axispane.add(prev_view, c);
      c.gridx = 1;
      axispane.add(next_view, c);
      c.gridx = 2;
      axispane.add(scaleLabel, c);
      
      prev_node.addActionListener(this);
      next_node.addActionListener(this);
      prev_view.addActionListener(this);
      next_view.addActionListener(this);

    }

    /**
     * Gets the <code>JPanel</code> for axis linking
     * 
     * @return a <code>JPanel</code> for axis linking
     */
    public JPanel getAxisPanel() {
      setMode(AXIS);
      editor.updatescreen();
      return axispane;
    }

    /**
     * Selects a node in the model for actions
     * 
     * @param name
     *          the name of the node to select
     */
    public void selectNode(String name) {
      selectedmember = 0;
      selectednode = name;
    }

    /**
     * Deselects any selected nodes, members or axes in the model.
     */
    public void deselectAll() {
      mview.highlightNode("");
      selectednode = "";
      selectedaxis = -1;
      selectedmember = 0;
    }

    /**
     * Sets the editing mode.
     * 
     * @param mode
     *          Should be one of
     *          <code>ModelEditor.AXIS, .NODE, .MEMBER, .SNODE</code>.
     */
    public void setMode(int mode) {
      
      mpicker.updatePickMode(mode);
      deselectAll(); // deselect everything
      mview.sync(); // update model for highlighting
      updatescreen(); // update text in panels
      
      this.mode = mode;
    }

    /**
     * Returns the current editing mode.
     * 
     * @return an <code>int</code> representing the current editing mode.
     */
    public int getMode() {
      return mode;
    }
    
    /**
     * Sets the axis linking panel's channel name
     * 
     * @param chan
     *          the name of the channel to set
     */
    public void setChanName(String chan) {
      apchanname.setText(chan);
    }

    /**
     * Updates the editing panel with most current selections/actions.
     */
    public void updatescreen() {
     
      if (selectednode == "") {
        apnodename.setText(" - ");
      } else {
        apnodename.setText(selectednode);
        if(model.containsNode(selectednode)){
          nodeInfo.setText("     X: " + model.nodes.get(selectednode).position.x
              + ", Y: " + model.nodes.get(selectednode).position.y + ", Z: "
              + model.nodes.get(selectednode).position.z);
        }
      }
      if(model.containsNode(selectednode))
        mview.highlightNode(selectednode);
      scaleLabel.setText("Scale: " + (int)Math.floor(model.getScale())+"x");
    }


    // override for implementing ActionListener
    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      mview.update();
      if (source == addnode) {
        
      } else if (source == prev_view) {
        model.changeScale(true);
        
      } else if (source == next_view) {
        model.changeScale(false);
        
      }

      // and then linking buttons
      else if (model.nodeCount() > 0) {
        if (source == next_node) {
          if (selectednode == "") {
            selectednode = (String) model.nodes.keySet().toArray()[0];
          } else {
            // select next, loop to beginning
            Object[] nodearray = model.nodes.keySet().toArray();
            boolean found = false;
            int index;
            for (index = 0; index < nodearray.length && !found; index++) {
              if (nodearray[index] == selectednode) {
                found = true;
              }
            }
            if (found) {
              // leaving for loop advances one further, now ensure wrap
              index = (index) % nodearray.length;
              selectednode = (String) nodearray[index];

            } else {
              selectednode = "";
              selectedaxis = -1;
            }
          }
        } else if (source == prev_node) {
          if (selectednode == "") {
            selectednode = (String) model.nodes.keySet().toArray()[model.nodes
                .keySet().size() - 1];
          } else {
            Object[] nodearray = model.nodes.keySet().toArray();
            boolean found = false;
            int index;
            for (index = 0; index < nodearray.length && !found; index++) {
              if (nodearray[index] == selectednode) {
                found = true;
              }
            }
            if (found) {
              index = (index + nodearray.length - 2) % nodearray.length;
              selectednode = (String) nodearray[index];
              
            } else {
              selectednode = "";
              selectedaxis = -1;
            }
          }
        } 
        
      }
      updatescreen();
    }
  }

  // helper class for realtime data mapping
  /**
   * Helper class for channel subscriptions.
   */
  class NodeAxisPair {
    String nodename;

    int axis;

    NodeAxisPair(String n, int a) {
      nodename = n;
      axis = a;
    }
  }

  @Override
  public void writeJPEG(NumericDataSample sample, ImageOutputStream out)
      throws IOException {
    
    // PostData drops in new data, postTime is called
    // repeatedly on those data points, iterate through them

    HashMap<String, Point3d> disp_updates = new HashMap<String, Point3d>();
    
    Number[] values = sample.getValues();
    //    is this check necessary?
    if(seriesList_.size() != values.length){
      log.warn("export sample size does not match channels, exiting");
      return;
    }
    
    // i iterates over subscribed channels
    for(int i=0;i<values.length;i++){
      String channelName = sample.getChannelNameAt(i);
      //int channelIndex = tsd.getChannelIndex(channelName);
      Number data = values[i];
      
      if (data != null) // if this channel has new data
      {
//        int timeIndex = 0;
//        if (times.length > 0 && time > mpanel.time) {
//          for (timeIndex = 0; times[timeIndex] < time
//              && timeIndex < times.length - 1; timeIndex++) {}
//        }
        
        double x = Double.NEGATIVE_INFINITY;
        double y = Double.NEGATIVE_INFINITY;
        double z = Double.NEGATIVE_INFINITY;
        String[] names = channelName.split("/");
        int len = names.length - 1;
        String nodeName = names[len];
        int nameLen = nodeName.length();
        String chan = nodeName.substring(0, nameLen-2);
        chan=chan.replace(' ', '_');
        
        char c = nodeName.toLowerCase().charAt(nameLen-1);
        if(!nodesPos.containsKey(chan)){
          boolean[] temp = {false, false, false};
          nodesXYZ.put(chan, temp);
          double[] tempInt = {0.0,0.0,0.0};
          nodesPos.put(chan, tempInt);
        }
        
        
        if(data.doubleValue() == INV_DATA){
          log.debug("got invisible point");
        }else{
          switch(c){
            case 'x':
              nodesXYZ.get(chan)[0]=true;
              nodesPos.get(chan)[0]=data.doubleValue();
              break;
            case 'y':
              nodesXYZ.get(chan)[1]=true;
              nodesPos.get(chan)[1]=data.doubleValue();
              break;
            case 'z':
              nodesXYZ.get(chan)[2]=true;
              nodesPos.get(chan)[2]=data.doubleValue();
              break;
          }
        }
        
        if (model.nodeCount() == 0 && hasXYZ(nodesXYZ.get(chan))) {
          model = new StructureModel();
          try {
            double[] position = nodesPos.get(chan);
            model.addNode(chan, new Point3d(position[0], position[1], position[2]));
            mview.sync();
            mview.resetScene();

          } catch (StructureModel.ModelException e) {
            log.debug(e);
          }
        }
        
        if ( hasXYZ(nodesXYZ.get(chan)) ) {
          double[] position = nodesPos.get(chan);
          disp_updates.put(chan, new Point3d(position[0], position[1], position[2]));
          //nodesXYZ.remove(chan);
          nodesPos.remove(chan);
          if (model.nodes.containsKey(chan)) {
            model.nodes.get(chan).setAlive(true);
          }

        } else if(data.doubleValue() == INV_DATA){
          if (model.nodes.containsKey(chan)) {
            model.nodes.get(chan).setAlive(false);
          }

        }
        
        
      } // end if channelIndex !=1
    } // while channel.hasNext() (subscribed channels)

    // now walk updates and push to model and then call mview.update()
    
    Iterator<Entry<String, Point3d>> nodeIter = disp_updates.entrySet()
        .iterator();

    while (nodeIter.hasNext()) {
      Entry<String, Point3d> temp = nodeIter.next();
      String str = temp.getKey();
      Point3d pt = temp.getValue();
      
      if (model.containsNode(str) && !model.containsNode(pt)) {
        
        model.moveNode(str, pt, false);

      } else if (!model.containsNode(str) && !model.containsNode(pt)) {
        try {
          model.addNode(str, pt);
          mview.sync();
          mview.viewSync();
        } catch (StructureModel.ModelException e) {
          log.error(e);
        }
      }
      
    }
    
    mview.update();
    editor.updatescreen();
    //this.time = time;
   
    viewpanel.writeJPEG(out);
    //exportTime=sample.getTimestamp();
  }


}
