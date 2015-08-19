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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/ui/ApplicationApplet.java $
 * $Revision: 1281 $
 * $Date: 2008-11-26 08:51:56 -0500 (Wed, 26 Nov 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.ui;

import javax.swing.JApplet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;
import org.rdv.ExtensionManager;
import org.rdv.ManagerStore;
import org.rdv.data.Channel;
import org.rdv.datapanel.DataPanel;
import org.rdv.rbnb.RBNBController;

/**
 * @author jphanley, bkirschner
 */
public class ApplicationApplet extends JApplet {

    /** serialization version identifier */
    private static final long serialVersionUID = 3893529815569317251L;

	public static String CHANNEL_SPLIT_CHAR = "&";
	protected DataViewer dataViewer;
	private static Log log=LogFactory.getLog(ApplicationApplet.class.getName());

	public void init() {
		System.setProperty("sun.java2d.ddscale","true");	

		RBNBController rbnbController = RBNBController.getInstance();

		String hostName = getParameter("host");
		String portString = getParameter("port");
		String channelsString = getParameter("channels");
		String playbackRateString = getParameter("playback-rate");
		String timeScaleString = getParameter("time-scale");
		boolean play = Boolean.parseBoolean(getParameter("play"));
		boolean realTime = Boolean.parseBoolean(getParameter("real-time"));

		if (playbackRateString != null && !playbackRateString.equals("")) {
      double playbackRate = Double.parseDouble(playbackRateString);
      rbnbController.setPlaybackRate(playbackRate);
		}

		if (timeScaleString != null && !timeScaleString.equals("")) {
      double timeScale = Double.parseDouble(timeScaleString);
      rbnbController.setGlobalTimeScale(timeScale);
    }

		if (portString != null && !portString.equals("")) {
			rbnbController.setRBNBPortNumber(Integer.parseInt(portString));
    }

		String[] channels = null;
		if (channelsString != null && !channelsString.equals("")) {
			channels = channelsString.split(CHANNEL_SPLIT_CHAR);
    }

		if (hostName != null && !hostName.equals("")) {
			rbnbController.setRBNBHostName(hostName);

      if (rbnbController.connect(true)) {
  			if (channels != null) {
  				for (int i=0; i<channels.length; i++) {
  					String channel = channels[i];
  					log.debug("Viewing channel " + channel + ".");
  					Channel channelTest = rbnbController.getChannel(channel);
  					if ( channelTest == null )
  						log.debug("No such channel: " + channel );
  					
  					DataPanel panel = ExtensionManager.getInstance()
  					                          .createDataPanelFromChannel(channel);
  					int viewId=ManagerStore.getPanelManager().addDataPanel(panel);
  					ManagerStore.getPanelManager().showView(viewId);
  				}
  			}
  
  			if (play) {
  				log.debug("Starting data playback.");
  				rbnbController.play();
  			} else if (realTime) {
  				log.debug("Viewing data in real time.");
  				rbnbController.monitor();
  			}
      }
		}

		//this.setContentPane(dataViewer.getApplicationFrame().getContentPane());
	}
	
	public void start() {
		
	}
	
	public void stop() {
		
	}

	public void destroy() {
		//dataViewer.exit();
	}
}
