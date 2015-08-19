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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/util/AudioPlayer.java $
 * $Revision: 1149 $
 * $Date: 2008-07-03 10:23:04 -0400 (Thu, 03 Jul 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * A class to play audio.
 * 
 * @author Jason P. Hanley
 */
public class AudioPlayer {
  /** the instance of the class */
  private static AudioPlayer instance;
  
  /** the URL of the current audio stream */
  private URL url;
  
  /** lock for the player */
  private ReentrantLock playerLock;
  
  /** property change help */
  private PropertyChangeSupport prop;
  
  /** a list of error listeners */
  private List<AudioErrorListener> errorListeners;
  
  /**
   * Creates an audio player.
   */
  protected AudioPlayer() {
    url = null;
    
    playerLock = new ReentrantLock();
    
    prop = new PropertyChangeSupport(this);
    
    errorListeners = new ArrayList<AudioErrorListener>();
  }
  
  /**
   * Gets the instance of the audio player.
   * 
   * @return  the audio player
   */
  public static AudioPlayer getInstance() {
    if (instance == null) {
      instance = new AudioPlayer();
    }
    
    return instance;
  }
  
  /**
   * Returns whether the audio player is playing a stream.
   * 
   * @return  true if playing, false otherwise
   */
  public boolean isPlaying() {
    return playerLock.isLocked();
  }
  
  /**
   * Set the player to playing or paused. If the current URL is not set (null)
   * then it is not possible to go to playing.
   * 
   * @param playing  set to playing or not
   */
  public void setPlaying(boolean playing) {
    boolean isPlaying = playerLock.isLocked();
    
    if (isPlaying == playing || url == null) {
      return;
    }
    
    if (isPlaying) {
      playerLock.lock();
      playerLock.unlock();
    } else {
      playStream();
    } 
  }
  
  /**
   * Get the URL of the current audio stream.
   * 
   * @return  the URL of the audio stream, or null if there is none
   */
  public URL getURL() {
    return url;
  }
  
  /**
   * Sets the URL of the audio stream to play. If player is not playing, it will
   * begin playing.
   * 
   * @param url  the URL to play
   */
  public void setURL(final URL url) {
    if (this.url == null && url == null) {
      return;
    }

    boolean playing = playerLock.isLocked();

    // stop player
    if (playing) {
      playerLock.lock();
      playerLock.unlock();
    }
    
    if (this.url == null || !this.url.equals(url)) {
      URL oldURL = this.url;
      this.url = url;
      prop.firePropertyChange("url", oldURL, url);
    }
    
    // play stream
    if (url != null) {
      playStream();
    }
  }
  
  /**
   * Play the stream.
   */
  private void playStream() {
    new Thread() {
      public void run() {
        prop.firePropertyChange("playing", false, true);
        
        prop.firePropertyChange("title", null, null);
        
        playerLock.lock();
        
        Exception exception = null;
        
        try {
          AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(url);
          Map<String,Object> properties = audioFileFormat.properties();
          if (properties.containsKey("title")) {
            prop.firePropertyChange("title", null, properties.get("title"));
          }
          
          AudioInputStream in = AudioSystem.getAudioInputStream(url);
          AudioFormat audioFormat = in.getFormat();
          
          AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
              audioFormat.getSampleRate(),
              16,
              audioFormat.getChannels(),
              audioFormat.getChannels() * 2,
              audioFormat.getSampleRate(),
              false);
          
          AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
          
          DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
          SourceDataLine line = (SourceDataLine)AudioSystem.getLine(info);
          line.open(decodedFormat);
          
          rawplay(din, line);
          
          in.close();
        } catch (Exception e) {
          exception = e;
        } finally {
          playerLock.unlock();
        }
        
        prop.firePropertyChange("playing", true, false);
        
        prop.firePropertyChange("title", null, null);
        
        if (exception != null) {
          fireAudioError(exception);
        }
      }
    }.start();
  }
  
  /**
   * Playback the input stream to the output device.
   * 
   * @param din           the input stream
   * @param line          the output device
   * @throws IOException  if an error occurs playing back the audio
   */
  private void rawplay(AudioInputStream din, SourceDataLine line) throws IOException {
    // Start
    line.start();
    
    byte[] data = new byte[4096];
    int nBytesRead = 0;
    while (playerLock.getQueueLength() == 0 && nBytesRead != -1) {
      nBytesRead = din.read(data, 0, data.length);
      if (nBytesRead != -1) {
        line.write(data, 0, nBytesRead);
      }
    }
    
    // Stop
    line.drain();
    line.stop();
    line.close();
    
    din.close();
  }

  /**
   * Adds a property change listener.
   * 
   * @param listener  the property change listener to add
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    prop.addPropertyChangeListener(listener);
  }
  
  /**
   * Removes the property change listener.
   * 
   * @param listener  the property change listener to remove.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    prop.removePropertyChangeListener(listener);
  }
  
  /**
   * Adds an audio error listener.
   * 
   * @param listener  the audio error listener to add
   */
  public void addAudioErrorListener(AudioErrorListener listener) {
    errorListeners.add(listener);
  }
  
  /**
   * Removes an audio error listener.
   * 
   * @param listener  the audio error listener to remove
   */
  public void removeAudioErrorListener(AudioErrorListener listener) {
    errorListeners.remove(listener);
  }
  
  /**
   * Invokes audio error listeners.
   * 
   * @param e  the exception to send to listeners
   */
  protected void fireAudioError(Exception e) {
    try {
      for (AudioErrorListener listener : errorListeners) {
        listener.audioError(e);
      }
    } catch (Exception ex) {}
  }
}