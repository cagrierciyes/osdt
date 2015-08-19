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
 * $URL: https://rdv.googlecode.com/svn/trunk/src/org/rdv/rbnb/RBNBController.java $
 * $Revision: 1331 $
 * $Date: 2008-12-08 11:38:36 -0500 (Mon, 08 Dec 2008) $
 * $Author: jason@paltasoftware.com $
 */

package org.rdv.rbnb;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rdv.DataViewer;
import org.rdv.TimeScale;
import org.rdv.data.Channel;
import org.rdv.data.LocalChannelManager;
import org.rdv.data.VisualizationSeries;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;

/**
 * A class to manage a connection to an RBNB server and to post channel data to
 * interested listeners.
 * 
 * @author Jason P. Hanley
 * @author Drew Daugherty
 */
public class RBNBController implements Player {
  
  private static Log log = LogFactory.getLog(RBNBController.class.getName());
  
  /** the single instance of this class */
  protected static RBNBController instance;

  //private String rbnbSinkName = "RDV";

  private Thread rbnbThread;

  private int state;

  private PrefetchExecutor prefetchExec_;
  
  //private ChannelMap requestedChannels;

  private ChannelManager channelManager;

  private MetadataManager metadataManager;

  private MarkerManager markerManager;

  private List<TimeListener> timeListeners;

  private List<StateListener> stateListeners;

  private List<SubscriptionListener> subscriptionListeners;

  private List<PlaybackRateListener> playbackRateListeners;

  private List<TimeScaleListener> timeScaleChangeListeners;

  private List<MessageListener> messageListeners;

  private List<ConnectionListener> connectionListeners;
  
  private RBNBQuery monitorQuery_;
  
  private double lastPlaySystemTime=-1.0;
  
  private RBNBQuery currPlayQuery_=null;
  
  /// Current time of latest data point being displayed, regardless of mode.
  private double location_;

  ///The virtual location during playback when no data is actually available.
  private double playbackLocation_;
  
  private double playbackRate;

  private double globalTimeScale;

  private double updateLocation = -1;

  private Object updateLocationLock = new Object();

  private double updateGlobalTimeScale = -1;

  private Object updateGlobalTimeScaleLock = new Object();

  private double updatePlaybackRate = -1;

  private Object updatePlaybackRateLock = new Object();

  private List<Integer> stateChangeRequests = new ArrayList<Integer>();

  private List<SubscriptionRequest> updateSubscriptionRequests = new ArrayList<SubscriptionRequest>();

  private boolean dropData_=false;

  /// Maximum rate (in hz) the cycle to fetch data and display can run at.
  public static double PLAYBACK_REFRESH_RATE = 3.0;
  
  /// Map to request handler for listeners such as data panels.
  private Map<DataListener, RequestHandler> subscribers_ = new HashMap<DataListener, RequestHandler>();
  
  /// RBNB connection for fetching data.
  private RBNBConnection rbnb_=new RBNBConnection();
  
  protected RBNBController() {

    state = STATE_DISCONNECTED;
    dropData_ = false;

    location_ = System.currentTimeMillis() / 1000d;
    playbackRate = 1;
    
    globalTimeScale = TimeScale.getDefaultTimeScale();

    channelManager = new ChannelManager();
    metadataManager = new MetadataManager(this);
    markerManager = new MarkerManager(this);

    timeListeners = new ArrayList<TimeListener>();
    stateListeners = new ArrayList<StateListener>();
    subscriptionListeners = new ArrayList<SubscriptionListener>();
    playbackRateListeners = new ArrayList<PlaybackRateListener>();
    timeScaleChangeListeners = new ArrayList<TimeScaleListener>();
    messageListeners = new ArrayList<MessageListener>();
    connectionListeners = new ArrayList<ConnectionListener>();

    prefetchExec_=new PrefetchExecutor();
    
    run();
  }

  /**
   * Get the single instance of this class.
   * 
   * @return  the instance of this class
   */
  public static RBNBController getInstance() {
    if (instance == null) {
      instance = new RBNBController();
    }
    
    return instance;
  }

  private void run() {
    rbnbThread = new Thread(new Runnable() {
      public void run() {
        runRBNB();
      }
    }, "RBNB");
    rbnbThread.start();
  }

  /***
   * Main control loop.  Fetch data and visualize it.
   */
  private void runRBNB() {
    log.info("RBNB data thread has started.");

    while (state != STATE_EXITING) {
      processSubscriptionRequests();
      processLocationUpdate();
      processGlobalTimeScaleUpdate();
      processPlaybackRateUpdate();
      processStateChangeRequests();

      switch (state) {
      case STATE_LOADING:
        loadData(true);
        log.warn("You must always manually transition from the loading state.");
        changeStateSafe(STATE_STOPPED);
        break;
      case STATE_PLAYING:
        loadData(true);
        updateDataPlaying();
        break;
      case STATE_MONITORING:
        loadData(true);
        updateDataMonitoring();
        //updateDataPlaying();
        break;
      case STATE_STOPPED:
      case STATE_DISCONNECTED:
        try {
          Thread.sleep(50);
        } catch (Exception e) {
        }
        break;
      }
    }

    rbnb_.close();
    metadataManager.stopUpdating();

    log.info("RBNB data thread is exiting.");
  }

  /**
   * Debugging routine to print state of subscriptions to log.
   */
  private void dumpSubscriptionRequests(){
    Iterator<RequestHandler>entries=subscribers_.values().iterator();
    while(entries.hasNext()){
      log.debug(entries.next());
    }
  }
  
  // State Processing Methods
 
  private void processSubscriptionRequests() {
    while (!updateSubscriptionRequests.isEmpty()) {
      SubscriptionRequest subscriptionRequest;
      synchronized (updateSubscriptionRequests) {
        subscriptionRequest = (SubscriptionRequest) updateSubscriptionRequests
            .remove(0);
      }
      
      RequestHandler reqHandler=subscribers_.get(subscriptionRequest.getListener());
      if(reqHandler==null){
        subscribers_.put(subscriptionRequest.getListener(),
                      new RequestHandler(subscriptionRequest));
      }else{
        reqHandler.updateRequest(subscriptionRequest);
        // all channels unsubbed
        if(!reqHandler.hasRequest()){
          subscribers_.remove(subscriptionRequest.getListener());
        }
      }
      
        
      if (subscriptionRequest.isSubscribe()){
        subscribeSafe(subscriptionRequest);
      }else{
        unsubscribeSafe(subscriptionRequest);
      }
      
      //dumpSubscriptionRequests();
    }
  }

  private void processLocationUpdate() {
    if (updateLocation != -1) {
      double oldLocation = location_;

      synchronized (updateLocationLock) {
        location_ = updateLocation;
        updateLocation = -1;
      }

      if (oldLocation == location_) {
        return;
      }

      log.info("Setting location to " + DataViewer.formatDate(location_) + ".");

      if(subscribers_.values().size()>0){
      
        loadData(location_, false);
        changeStateSafe(STATE_STOPPED);
      } else {
        updateTimeListeners(location_);
      }
    }
  }

  private void processGlobalTimeScaleUpdate() {
    if (updateGlobalTimeScale != -1) {
      synchronized (updateGlobalTimeScaleLock) {
        globalTimeScale = updateGlobalTimeScale;
        updateGlobalTimeScale = -1;
      }
      
      log.info("Setting global time scale to " + globalTimeScale + ".");

      fireTimeScaleChanged(globalTimeScale);
    }
  }

  /***
   * Queue a request to update the playback rate.
   */
  private void processPlaybackRateUpdate() {
    if (updatePlaybackRate != -1) {
      double oldPlaybackRate = playbackRate;

      synchronized (updatePlaybackRateLock) {
        playbackRate = updatePlaybackRate;
        updatePlaybackRate = -1;
      }

      if (playbackRate == oldPlaybackRate) {
        return;
      }

      log.info("Setting playback rate to " + playbackRate + " seconds.");

      //if (state == STATE_PLAYING) {
        //getPreFetchChannelMap();
        //waitOnPreFetchQuery();
        //preFetchData(location, playbackRate);
      //}

      firePlaybackRateChanged(playbackRate);
    }
  }

  private void processStateChangeRequests() {
    while (!stateChangeRequests.isEmpty()) {
      int updateState;
      synchronized (stateChangeRequests) {
        updateState = ((Integer) stateChangeRequests.remove(0)).intValue();
      }
      log.info("got state change request for "+getStateName(updateState));
      changeStateSafe(updateState);
    }
  }

  private boolean changeStateSafe(int newState) {
    
    if (state == newState) {
      log.info("Already in state " + getStateName(state) + ".");
      return true;
    } else if (state == STATE_PLAYING) {
      //getPreFetchChannelMap();
      //waitOnPreFetchQuery();
    } else if (state == STATE_EXITING) {
      log.error("Can not transition out of exiting state to "
          + getStateName(state) + " state.");
      return false;
    } else if (state == STATE_DISCONNECTED && newState != STATE_EXITING) {
      fireConnecting();

      try {
        rbnb_.init();
      } catch (SAPIException e) {
        rbnb_.close();
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String traceMsg=sw.toString();
        log.error(traceMsg);
        
        String nestedExceptionText="Nested exception:";
        int startIndex=0,endIndex=traceMsg.length()-1;
        int nestIndex=traceMsg.indexOf(nestedExceptionText);
        // parse nested exception
        if(nestIndex!=-1){
          startIndex=nestIndex+nestedExceptionText.length()+2;
        }
        endIndex=traceMsg.indexOf('\n', startIndex);
        
        fireErrorMessage("Failed to connect to the RBNB server at "
              +rbnb_.getHostName()+":"+rbnb_.getPortNumber()
              +": "+traceMsg.substring(startIndex, endIndex));

        fireConnectionFailed();
        return false;
      }

      metadataManager.startUpdating();
      fireConnected();
    }

    log.info("Changing from state "+getStateName(state)+" to "+getStateName(newState));
//    try{
//      throw new Exception("Changing from state "+getStateName(state)+" to "+getStateName(newState));
//    }catch(Exception e){
//      StringWriter sw = new StringWriter();
//      e.printStackTrace(new PrintWriter(sw));
//      String traceMsg=sw.toString();
//      log.debug(traceMsg);
//    }
    switch (newState) {
    case STATE_MONITORING:
      //if (!monitorData()) {
      //  fireErrorMessage("Stopping real time. Failed to load data from the server. Please try again later.");
      //  return false;
      //}
      //currPlayQuery_=null;
      //prefetchExec_.reset();
      //prefetchExec_.queue(getMonitorQuery());
      break;
    case STATE_PLAYING:
      if(state!=STATE_LOADING){
        // perform initial setup
        currPlayQuery_=null;
        prefetchExec_.reset();
        playbackLocation_=location_;
        prefetchExec_.queue(
            getPreFetchQuery(location_, playbackRate));
      }
      break;
    case STATE_LOADING:
    case STATE_STOPPED:
    case STATE_EXITING:
      break;
    case STATE_DISCONNECTED:
      //closeRBNB();
      rbnb_.close();
      metadataManager.stopUpdating();
      LocalChannelManager.getInstance().removeAllChannels();
      break;
    default:
      log.error("Unknown state: " + state + ".");
      return false;
    }

    int oldState = state;
    state = newState;

    notifyStateListeners(state, oldState);

    //log.info("Transitioned from state " + getStateName(oldState) + " to "
    //    + getStateName(state) + ".");

    return true;
  }
  
  // Subscription Methods
  private void subscribeSafe(SubscriptionRequest r) {
    // skip subscription if we are not connected
    if (state == STATE_DISCONNECTED) {
      return;
    }

    Iterator<String> it = r.getRDVChannelNameList().iterator();
    while (it.hasNext()){
      String channelName =it.next();
      // notify channel manager
      channelManager.subscribe(channelName, r.getListener());
    }
    
    int originalState = state;

    if (originalState == STATE_MONITORING) {
      changeStateSafe(STATE_MONITORING);
    } else if (originalState == STATE_PLAYING) {
      changeStateSafe(STATE_PLAYING);
    } else if (originalState == STATE_STOPPED) {
      // load data for newly subscribed channels only
      loadData(true);
    } else {
      changeStateSafe(STATE_STOPPED);
    }

    it = r.getRDVChannelNameList().iterator();
    while (it.hasNext()){
      String channelName =it.next();
      fireSubscriptionNotification(channelName);
    }
  }

  
  private void unsubscribeSafe(SubscriptionRequest r) {
    // skip unsubscription if we are not connected
    if (state == STATE_DISCONNECTED) {
      return;
    }

    Iterator<String> it = r.getRDVChannelNameList().iterator();
    while (it.hasNext()){
      String channelName =it.next();
      channelManager.unsubscribe(channelName, r.getListener());

      log.info("Unsubscribed from channel " + channelName + ".");
    }
    
    it = r.getRDVChannelNameList().iterator();
    while (it.hasNext()){
      String channelName =it.next();
      fireUnsubscriptionNotification(channelName);
    }
  }

  List<String> getServerChannels(List<String> subChannels){
    List<String> ret=new ArrayList<String>();
    //recreate the channel map with the subscribed channels
    for (String channelName : subChannels) {
      Channel channel = getChannel(channelName);
    
      // see if this is a local channel and subscribe to its server channels,
      // otherwise just subscribe to the channel
      if (channel != null){
        //LocalChannel localChannel = (LocalChannel) channel;
        List<String> serverChannels = channel.getServerChannels();
    
        for (String serverChannel : serverChannels) {
          ret.add(serverChannel); 
        }
      }
    }
    return ret;
  }
  // Load Methods

  /**
   * Load data for all channels.
   */
  private void loadData(boolean unservicedOnly) {
    loadData(location_, unservicedOnly);
  }

  /**
   * Load data for some set of requests.
   * 
   * @param location  the end time
   * @param unservicedOnly  true to load data for new or changed requests, 
   *                        false to load data for all requests.
   */
  private void loadData(double location, boolean unservicedOnly) {
    //String[] subscribedChannels = requestedChannels.GetChannelList();
    //loadData(Arrays.asList(subscribedChannels), location, duration);
    
    Collection<RBNBQuery> queries = buildQueries(location,unservicedOnly);
    if(!queries.isEmpty()){
      int prevState=state;
      changeStateSafe(STATE_LOADING);
      Collection<RBNBQuery> sortedQueries=prioritizeQueries(queries);
      executeMonitorQueries(sortedQueries);
      changeStateSafe(prevState);
    }
    //updateTimeListeners(location);
  }

  /**
   * Sort queries by display priority. Queries pertaining to panels
   * immediately visible will be at head of list.
   * 
   * @param currList the list to sort.
   * @return sorted list of queries.
   */
  private Collection<RBNBQuery> prioritizeQueries(Collection<RBNBQuery> currList){
    LinkedList<RBNBQuery> retList=new LinkedList<RBNBQuery>();

    Iterator<RBNBQuery>it=currList.iterator();
    
    while (it.hasNext()){
      RBNBQuery currQuery=it.next();
      int insPriority=currQuery.getDisplayPriority();
      
      Iterator<RBNBQuery> newIt=retList.iterator();
      int compareIndex=0,insertIndex=-1;
      while(newIt.hasNext()){
        RBNBQuery compareQuery=newIt.next();
        if(compareQuery.getDisplayPriority()>=insPriority){
            retList.add(compareIndex, currQuery);
            insertIndex=compareIndex;
            break;
        }
        ++compareIndex;
      }
      if(insertIndex==-1)
        retList.addLast(currQuery);
    }

    return retList;
  }
  
  /**
   * Convert the set of subscription requests received from panels
   * into a set of queries for archived data.
   * 
   * @param location  The end time of the data to be fetched.
   * @param unservicedOnly true if we should only create queries 
   *                      for unserviced requests.
   */
  private Collection<RBNBQuery> buildQueries(double location,
                                              boolean unservicedOnly){
    List <RBNBQuery> queries= new ArrayList<RBNBQuery>();
    QRAFixedTypeAndRange imageReqs = new QRAFixedTypeAndRange(
                                    SubscriptionRequest.TYPE_IMAGE,0.0);
    QRAFixedTypeAndRange tabularReqs = new QRAFixedTypeAndRange(
                                    SubscriptionRequest.TYPE_TABULAR,1.0);
    
    Collection<RequestHandler> allReqs=subscribers_.values();
    
    Iterator<RequestHandler>it=allReqs.iterator();
    RequestHandler testReq;
    while (it.hasNext()){
      testReq=it.next();
      if(testReq==null) continue;
      if(unservicedOnly && testReq.serviced())continue;

      if(!imageReqs.addSubscriptionRequest(testReq.getRequest()) && 
          !tabularReqs.addSubscriptionRequest(testReq.getRequest())){
        RBNBQuery historyQuery=testReq.getHistoryRequest(location);
        
        if(historyQuery!=null)
          queries.add(historyQuery);
      }
    }
    
    if(imageReqs.hasRequests()) 
      queries.add(RBNBQuery.createAbsoluteHistoryQuery(imageReqs,location));
    if(tabularReqs.hasRequests())
      queries.add(RBNBQuery.createAbsoluteHistoryQuery(tabularReqs,location));


    return queries;
  }
  
  /*private String[] getSubscribedChannelList(){
    List<String> ret=new ArrayList<String>();
    Collection<SubscriptionRequest> allReqs=subscribers_.values();
    Iterator<SubscriptionRequest>it=allReqs.iterator();
    SubscriptionRequest testReq;
    while (it.hasNext()){
      testReq=it.next();
      if(testReq==null)continue;
      Iterator<Channel> nameit=testReq.getRDVChannelIterator();
      while(nameit.hasNext()){
        String name=new String(nameit.next().getName());
        if (!ret.contains(name)) ret.add(name);
      }
    }
    String arr[]=new String[ret.size()];
    Iterator<String>chanIt=ret.iterator();
    int i=0;
    while (chanIt.hasNext()){
      arr[i++]=chanIt.next();
    }
    return arr;
  }*/
  
  /**
   * Queues requests for data from RBNB server, retrieves processed
   * requests and sends the data to panels for display.  Timing of data
   * display is controlled by playbackRate variable.  Designed to be 
   * called repeatedly, inserts delays to ensure data is never displayed
   * faster than the rate specified by PLAYBACK_REFRESH_RATE variable.
   * 
   */
  private synchronized void updateDataPlaying() {
    if (subscribers_.values().size() == 0) {
      fireStatusMessage("Stopping playback. No channels are selected.");
      changeStateSafe(STATE_STOPPED);
      return;
    }
    if(currPlayQuery_==null)
      currPlayQuery_=prefetchExec_.dequeue();
    
    // precompute timing and delay values
    long startPlayTime=System.currentTimeMillis();
    double newPlaySystemTime=startPlayTime/1000.0;
    double timeScale=(state==STATE_PLAYING)?playbackRate:1.0;
    
    long maxDelay=(long)(1000.0/PLAYBACK_REFRESH_RATE);
    // scale wall clock time to playback time
    double scaledElapsed=(lastPlaySystemTime<=0.0)?  
        0.0:(newPlaySystemTime-lastPlaySystemTime)*timeScale;
    double newLocation=playbackLocation_+scaledElapsed;
    boolean updatedListeners=false;
    
    if (currPlayQuery_ == null){
      //log.debug("no query received");
      //return;
      // do nothing
//    }else if(currPlayQuery_.timeout()){
//      String errMsg;
//      errMsg="Stopping "+getStateName(state)+
//                ". Timeout loading data from server. Please notify your Data Turbine administrator of this error.";
//      fireErrorMessage(errMsg);
//      changeStateSafe(STATE_STOPPED);
//      return;
    }else if(currPlayQuery_.error()){
      String errMsg;
      errMsg="Stopping "+getStateName(state)+
            ". Data load error: "+currPlayQuery_.getErrorMsg()+
            ". Please notify your Data Turbine administrator of this error.";
      fireErrorMessage(errMsg);
      changeStateSafe(STATE_STOPPED);
      return;
    }else if (!currPlayQuery_.gotData()){ 
      //log.debug("got no query data");
      // no data was received from last query
      if (currPlayQuery_.isFinal()){
        String errMsg;
        if(currPlayQuery_.timeout()){
          errMsg="Stopping "+getStateName(state)+
                ". Timeout loading data from server. Please notify your Data Turbine administrator of this error.";
        }else{
          errMsg="Stopping "+getStateName(state)+
                ". No more data is available.";
        }
        fireErrorMessage(errMsg);
        changeStateSafe(STATE_STOPPED);
        return;
      }else{
        // create a new query and submit it
        RBNBQuery newQuery;
        if(state==STATE_PLAYING){
          if(currPlayQuery_.getNoDataCount()>=3){
            //switch to fetch next record rather by than absolute time
            log.debug("no data-switching to fetch next record");
            newQuery= RBNBQuery.createNextQuery(currPlayQuery_);
          }else{
            //log.debug("no data-fetching absolute");
            //note: does not pick up changes to subscription list!!!
            newQuery= RBNBQuery.createNextAbsoluteQuery(currPlayQuery_, playbackRate);
          }
        }else{
          //state is monitoring
          newQuery=getMonitorQuery();
        }
        prefetchExec_.queue(newQuery);
      }  
      currPlayQuery_=null;    
   }else{
     // initial query processing
     if(!currPlayQuery_.initComplete()){
       //log.debug("query init");
       
       // fix up bad estimated location
       double start =currPlayQuery_.getResultStartTime();
       if(location_ > start || playbackLocation_>start){
         log.debug("fixing up bad time - "+(location_-start)+" secs diff");
         location_=start;
         newLocation=start+scaledElapsed;
         playbackLocation_=start;
       }
       
       //queue next query
       RBNBQuery newQuery;
       if(state==STATE_PLAYING){
         newQuery=getPreFetchQuery(currPlayQuery_.getRequestEndTime()
                           +RBNBTime.ONE_MICROSECOND,playbackRate);
       }else{
         newQuery=getMonitorQuery();
       }
       prefetchExec_.queue(newQuery);
       currPlayQuery_.setInitComplete();
     }
     
     currPlayQuery_.setTimeSeriesSlice(playbackLocation_,newLocation);
     
     TimeSeriesData tsSlice=currPlayQuery_.getTimeSeries();
     // grab this before timeseries can be freed
     boolean needMoreData=tsSlice.isDone();
     if(tsSlice.hasData()){
       double dataEnd=tsSlice.getMaxEndTime();
       updateTimeListeners(tsSlice.getMaxEndTime());
       location_=dataEnd+RBNBTime.ONE_MICROSECOND;
       log.info("updating time to: "+DataViewer.formatDate(location_));
       playbackLocation_=location_;
       //updatedListeners=true;
       notifyListeners(currPlayQuery_);
     }else{
       playbackLocation_=newLocation+RBNBTime.ONE_MICROSECOND;
       updateTimeListeners(playbackLocation_);
     }
     
     if(needMoreData){
       currPlayQuery_=null;
     }
   }

    //location=newLocation+RBNBTime.ONE_MICROSECOND;
    lastPlaySystemTime=newPlaySystemTime;
    //if(!updatedListeners){
      //update clock, panels
      //updateTimeListeners(location);
    //}
    
    if(state==STATE_MONITORING)return;
    
    //compute delay
    long elapsed =System.currentTimeMillis()-startPlayTime;
    long actualDelay=Math.max(maxDelay-elapsed, 0L);
    if(actualDelay>0L){
      try {
        Thread.sleep(actualDelay);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }else{
      //log.debug("no sleep - elapsed: "+elapsed+", max: "+maxDelay);
    }
  }
  
  /***
   * Create a query to prefetch data during playback.
   *  
   * @param location The start time of the query.
   * @param duration Data duration in seconds. 
   * @return A new RBNBQuery.
   */
  private RBNBQuery getPreFetchQuery(final double location, final double duration) {
   
    QueryRequestAggregate qra=new QRAFixedRange(duration);
    Iterator<RequestHandler>it=subscribers_.values().iterator();
    while (it.hasNext()){
      qra.addSubscriptionRequest(it.next().getRequest());
    }
    RBNBQuery newQuery= RBNBQuery.createAbsoluteQuery(qra, location);//new RBNBQuery(qra,new AbsoluteDataFetch(location,qra));
    //log.debug("Prefetching data from:"+DataViewer.formatDateSmart(location)+" to:"+DataViewer.formatDateSmart(location+duration));
    return newQuery;
  }

  private RBNBQuery getMonitorQuery() {
    
    QueryRequestAggregate qra=new QRAChart();
    Iterator<RequestHandler> it=subscribers_.values().iterator();
    while (it.hasNext()){
      qra.addSubscriptionRequest(it.next().getRequest());
    }
    RBNBQuery newQuery= RBNBQuery.createMonitorQuery(qra);
    log.debug("Prefetching monitor data");
    return newQuery;
  }
  
  // Monitor Methods
  private boolean monitorData() {
    return monitorData(true);
  }

  private boolean monitorData(boolean retry) {
    if(subscribers_.values().size()==0){
    //if (requestedChannels.NumberOfChannels() == 0) {
      return true;
    }

    log.debug("Monitoring data after location "
        + DataViewer.formatDate(location_) + ".");
    
    QueryRequestAggregate qra=new QRAFixedRange();
    Iterator<RequestHandler> hndIt=subscribers_.values().iterator();
    while (hndIt.hasNext()){
      qra.addSubscriptionRequest(hndIt.next().getRequest());
    }
    //monitorQuery_=new MonitorQuery(qra);
    monitorQuery_=RBNBQuery.createMonitorQuery(qra);
   
    //log.info("Monitoring selected data channels.");
    return true;
  }

  private void executeMonitorQueries(Collection<RBNBQuery> queries){
    boolean sawException=false;
    boolean sawTimeout=false;
    double newLocation=Double.MAX_VALUE;
    double lastTime=0.0;
    // stop monitoring if no channels selected
    //if (requestedChannels.NumberOfChannels() == 0) {
    //if (subscribers_.values().isEmpty()) {
    //  fireStatusMessage("Stopping real time. No channels are selected.");
    //  changeStateSafe(STATE_STOPPED);
    //  return;
    //}
    
    for (RBNBQuery q:queries){
      try{
        q.execute(rbnb_);
        if (q.timeout()){
          sawTimeout=true;
        }else if(q.error()){
          
        }else{
          lastTime = getLastTime(q.result());
          if(lastTime<newLocation) newLocation=lastTime;
        }
        
        notifyListeners(q);
        //q.notifyRequestors(false);
      }catch(Exception se){
        // must sapi as well as illegal argument exceptions
        sawException=true;
        se.printStackTrace();
      }
      updateTimeListeners(location_);
    }
    if(sawException){
      fireErrorMessage("Failed to load data from the server. Please try again later.");
      //e.printStackTrace();

      changeStateSafe(STATE_STOPPED);
      //requestIsMonitor = true;
      return;

    }else if(sawTimeout){
      if (state == STATE_MONITORING) {
        // no data was received, this is not an error and we should go on
        // to see if more data is recieved next time around
        // TODO see if we should sleep here
        log.debug("Fetch timed out for monitor.");
        return;

      } else {
        log.error("Failed to fetch data.");
        fireErrorMessage("Failed to load data from the server. Please try again later.");
        changeStateSafe(STATE_STOPPED);
        return;
      }
    }
    
    if (state == STATE_MONITORING) {
      // update current location
      if (newLocation != Double.MAX_VALUE && newLocation > location_) {
        location_ = newLocation;
      }
      updateTimeListeners(location_);
    }
     
  }
  
  private void updateDataMonitoring() {
    // stop monitoring if no channels selected
    if (subscribers_.values().size()==0){
    //if (requestedChannels.NumberOfChannels() == 0) {
      fireStatusMessage("Stopping real time. No channels are selected.");
      changeStateSafe(STATE_STOPPED);
      return;
    }
    // build monitoring query
    monitorData();
    
    try {
      monitorQuery_.execute(rbnb_);
      //sink.Fetch(timeout, getmap);
    } catch (Exception e) {
      fireErrorMessage("Failed to load data from the server: "+e.getMessage()+". Please try again later.");
      e.printStackTrace();

      changeStateSafe(STATE_STOPPED);
      //requestIsMonitor = true;
      return;
    }

    if (monitorQuery_.timeout()) {
    //if (getmap.GetIfFetchTimedOut()) {
      if (state == STATE_MONITORING) {
        // no data was received, this is not an error and we should go on
        // to see if more data is received next time around
        // TODO see if we should sleep here
        //fireErrorMessage("Failed to load data from the server: timeout error while monitoring. Please try again later.");
        //log.debug("Fetch timed out for monitor.");
        //return;

      } else {
        log.error("Failed to fetch data.");
        fireErrorMessage("Failed to load data from the server: timeout error. Please try again later.");
        changeStateSafe(STATE_STOPPED);
        return;
      }
    }

    // received no data
    if (!monitorQuery_.hasData()) {
    //if (getmap.NumberOfChannels() == 0) {
      return;
    }
    
    TimeSeriesData currTimeSeries=monitorQuery_.getTimeSeries();
    //double lastTimePosted=0.0;
    double newLocation=currTimeSeries.getMaxEndTime();
    if(newLocation>location_){
      location_=newLocation;
    }
    updateTimeListeners(location_);
    
    // post data to listeners
    notifyListeners(monitorQuery_);
    //monitorQuery_.notifyRequestors(dropData_);
//   
//    Collection<Double> tsTimes= currTimeSeries.getTimes();
//    Iterator<Double>timeIt=tsTimes.iterator();
//    while(state==STATE_MONITORING && 
//        timeIt.hasNext() && 
//        stateChangeRequests.size() == 0){
//      double tsTime=timeIt.next().doubleValue();
//      
//        //display everything 
//        //lastTimePosted=tsTime;
//        location=tsTime;
//        //newLocation=tsTime;
//        log.debug("updating time listeners to "+DataViewer.formatDateSmart(location));
//        updateTimeListeners(location);
//        //updatedListeners=true;
//      //}
//    }

//    if (state == STATE_MONITORING) {
//      
//      // update current location
//      double newLocation = getLastTime(monitorQuery_.result());//getmap);
//      if (newLocation > location) {
//        location = newLocation;
//      }
//      updateTimeListeners(location);
//    }
  }

  // Listener Methods
  private void notifyListeners(RBNBQuery q){
    List<SubscriptionResponse> responseSet=new ArrayList<SubscriptionResponse>();
    Iterator<RequestHandler> hndIt=subscribers_.values().iterator();
    while(hndIt.hasNext()){
      RequestHandler handler=hndIt.next();
      SubscriptionResponse resp=handler.getResponse(q);
      if(resp!=null)
        responseSet.add(resp);
    }
    Iterator<SubscriptionResponse>respIt=responseSet.iterator();
    while(respIt.hasNext()){
      respIt.next().notifyDataListener();
    }
  }
  
  private void updateTimeListeners(double location) {
    //log.debug("updating time listeners: "+DataViewer.formatDateSmart(location));
    for (int i = 0; i < timeListeners.size(); i++) {
      TimeListener timeListener = (TimeListener) timeListeners.get(i);
      try {
        timeListener.postTime(location);
      } catch (Exception e) {
        log.error("Failed to post time to " + timeListener + ".");
        e.printStackTrace();
      }
    }
  }

  private void notifyStateListeners(int state, int oldState) {
    for (int i = 0; i < stateListeners.size(); i++) {
      StateListener stateListener = (StateListener) stateListeners.get(i);
      stateListener.postState(state, oldState);
    }
  }

  private void fireSubscriptionNotification(String channelName) {
    SubscriptionListener subscriptionListener;
    for (int i = 0; i < subscriptionListeners.size(); i++) {
      subscriptionListener = (SubscriptionListener) subscriptionListeners
          .get(i);
      subscriptionListener.channelSubscribed(channelName);
    }
  }

  private void fireUnsubscriptionNotification(String channelName) {
    SubscriptionListener subscriptionListener;
    for (int i = 0; i < subscriptionListeners.size(); i++) {
      subscriptionListener = (SubscriptionListener) subscriptionListeners
          .get(i);
      subscriptionListener.channelUnsubscribed(channelName);
    }
  }

  private void firePlaybackRateChanged(double playbackRate) {
    PlaybackRateListener listener;
    for (int i = 0; i < playbackRateListeners.size(); i++) {
      listener = (PlaybackRateListener) playbackRateListeners.get(i);
      listener.playbackRateChanged(playbackRate);
    }
  }

  private void fireTimeScaleChanged(double timeScale) {
    TimeScaleListener listener;
    for (int i = 0; i < timeScaleChangeListeners.size(); i++) {
      listener = (TimeScaleListener) timeScaleChangeListeners.get(i);
      listener.globalTimeScaleChanged(timeScale);
    }
    // update all subscriptions to pick up new request times
    processSubscriptionRequests();
  }

  // Utility Methods

  /*private boolean moreData(String[] channels, double time) {
    double endTime = 0;

    for (String channelName : channels) {
      Channel channel = getChannel(channelName);
      if (channel == null) {
        continue;
      }

      double channelEndTime = channel.getStart() + channel.getDuration();
      endTime = Math.max(endTime, channelEndTime);
    }

    return time < endTime;
  }

  private boolean isVideo(String channelName) {
    Channel channel = getChannel(channelName);
    if (channel == null) {
      return false;
    }

    String mime = channel.getMetadata("mime");
    if (mime != null && mime.equals("image/jpeg")) {
      return true;
    } else {
      return false;
    }
  }*/

  private static double getLastTime(ChannelMap channelMap) {
    double lastTime = -1;

    String[] channels = channelMap.GetChannelList();
    for (int i = 0; i < channels.length; i++) {
      String channelName = channels[i];
      int channelIndex = channelMap.GetIndex(channelName);
      double[] times = channelMap.GetTimes(channelIndex);
      double endTime = times[times.length - 1];
      if (endTime > lastTime) {
        lastTime = endTime;
      }
    }

    return lastTime;
  }

  // Player Methods

  public int getState() {
    return state;
  }

  public void monitor() {
    if (state != STATE_MONITORING) {
      setLocation(System.currentTimeMillis() / 1000d);
    }
    setState(STATE_MONITORING);
  }

  public void play() {
    setState(STATE_PLAYING);
  }

  public void pause() {
    setState(STATE_STOPPED);
  }

  public void exit() {
    setState(STATE_EXITING);

    // wait for thread to finish
    int count = 0;
    //while (sink != null && count++ < 20) {
    while (rbnb_.isConnected() && count++ < 20) {
      try {
        Thread.sleep(50);
      } catch (Exception e) {
      }
    }
  }

  public void setState(int state) {
    log.debug("Setting state to "+getStateName(state));
//    try{
//      throw new Exception("Setting state to "+getStateName(state));
//    }catch(Exception e){
//      StringWriter sw = new StringWriter();
//      e.printStackTrace(new PrintWriter(sw));
//      String traceMsg=sw.toString();
//      log.debug(traceMsg);
//    }
    
    synchronized (stateChangeRequests) {
      stateChangeRequests.add(new Integer(state));
    }
  }

  public double getLocation() {
    return location_;
  }

  public void setLocation(final double location) {
    if (location < 0) {
      log.error("Location not set; location must be nonnegative.");
      return;
    }

    synchronized (updateLocationLock) {
      updateLocation = location;
    }
  }

  public double getPlaybackRate() {
    return playbackRate;
  }

  public void setPlaybackRate(final double playbackRate) {
    if (playbackRate <= 0) {
      log.error("Playback rate not set; playback rate must be positive.");
      return;
    }

    synchronized (updatePlaybackRateLock) {
      updatePlaybackRate = playbackRate;
    }
  }

  public double getTimeScale() {
    return globalTimeScale;
  }

  /***
   * Queue a global time scale update.
   * @param timeScale
   */
  public void setGlobalTimeScale(double timeScale) {
    if (timeScale <= 0) {
      log.error("Time scale not set; time scale must be positive.");
      return;
    }

    synchronized (updateGlobalTimeScaleLock) {
      updateGlobalTimeScale = timeScale;
    }
  }

  /**
   * Add a listener request to subscribe to channels to the 
   * list of updates to be made.
   * 
   * @param r The subscription request to add.
   */
  public boolean subscribe(SubscriptionRequest req){
    synchronized (updateSubscriptionRequests) {
      updateSubscriptionRequests.add(req);
    }
    return true;
  }
  
  /*public boolean subscribe(String channelName, DataListener listener) {
    synchronized (updateSubscriptionRequests) {
      updateSubscriptionRequests.add(new SubscriptionRequest(channelName,
          listener, true));
    }

    return true;
  }*/

  /**
   * Subscribe to the list of <code>channels</code> with the data
   * <code>listener</code>.
   * 
   * @param channels  the channels to subscribe to
   * @param listener  the data listener to post data to
   */
  /*public void subscribe(List<String> channels, DataListener listener) {
    synchronized (updateSubscriptionRequests) {
      updateSubscriptionRequests.add(new SubscriptionRequest(channels, listener, true));
    }
  }*/

  /**
   * Add a listener request to unsubscribe from channels to the 
   * list of updates to be made.
   * 
   * @param r The subscription request to add.
   */
  public boolean unsubscribe(SubscriptionRequest r) {
    synchronized (updateSubscriptionRequests) {
      updateSubscriptionRequests.add(r);
    } 
    return true;
  }
  
	/*public boolean unsubscribe(String channelName, DataListener listener) {
		synchronized (updateSubscriptionRequests) {
			updateSubscriptionRequests.add(new SubscriptionRequest(channelName, listener, false));
		}
		
		return true;	
	}*/

  /**
   * Unsubscribe from the list of <code>channels</code> for  the data
   * <code>listener</code>.
   * 
   * @param channels  the list of channels to unsubscribe from
   * @param listener  the data listener to unsubscribe
   */
	/*public void unsubscribe(List<String> channels, DataListener listener) {
	  synchronized (updateSubscriptionRequests) {
	    updateSubscriptionRequests.add(new SubscriptionRequest(channels, listener, false));
	  }
  }*/

	public boolean isSubscribed(String channelName) {
		return channelManager.isChannelSubscribed(channelName);
	}
  
  /**
   * Returns true if there is at least one listener subscribed to a channel.
   * 
   * @return  true if there are channel listener, false if there are none
   */
  public boolean hasSubscribedChannels() {
    return channelManager.hasSubscribedChannels();
  }

  public void addStateListener(StateListener stateListener) {
    stateListener.postState(state, state);
    stateListeners.add(stateListener);
  }

  public void removeStateListener(StateListener stateListener) {
    stateListeners.remove(stateListener);
  }

  public void addTimeListener(TimeListener timeListener) {
    timeListeners.add(timeListener);
    timeListener.postTime(location_);
  }

  public void removeTimeListener(TimeListener timeListener) {
    timeListeners.remove(timeListener);
  }

  public void addPlaybackRateListener(PlaybackRateListener listener) {
    listener.playbackRateChanged(playbackRate);
    playbackRateListeners.add(listener);
  }

  public void removePlaybackRateListener(PlaybackRateListener listener) {
    playbackRateListeners.remove(listener);
  }

  public void addTimeScaleListener(TimeScaleListener listener) {
    listener.globalTimeScaleChanged(globalTimeScale);
    timeScaleChangeListeners.add(listener);
  }

  public void removeTimeScaleListener(TimeScaleListener listener) {
    timeScaleChangeListeners.remove(listener);
  }

  // Public Methods

  public void setDropData(boolean dropData) {
    dropData_ = dropData;
  }
  
  public boolean getDropData(){
    return dropData_;
  }

  public String getRBNBHostName() {
    return rbnb_.getHostName();
    //return rbnbHostName;
  }

  public void setRBNBHostName(String rbnbHostName) {
    rbnb_.setHostName(rbnbHostName);
    //this.rbnbHostName = rbnbHostName;
  }

  public int getRBNBPortNumber() {
    return rbnb_.getPortNumber();
    //return rbnbPortNumber;
  }

  public void setRBNBPortNumber(int rbnbPortNumber) {
    rbnb_.setPortNumber(rbnbPortNumber);
    //this.rbnbPortNumber = rbnbPortNumber;
  }

  public String getRBNBConnectionString() {
    return rbnb_.getConnectionString();
    //return rbnbHostName + ":" + rbnbPortNumber;
  }

  /**
   * Gets the name of the server. If there is no active connection, null is
   * returned.
   * 
   * @return  the name of the server, or null if there is no connection
   */
  public String getServerName() {
    /*if (sink == null) {
      return null;
    }

    String serverName;

    try {
      serverName = sink.GetServerName();

      // strip out the leading slash that is there for some reason
      if (serverName.startsWith("/") && serverName.length() >= 2) {
        serverName = serverName.substring(1);
      }
    } catch (IllegalStateException e) {
      serverName = null;
    }

    return serverName;
    */
    return rbnb_.getServerName();
  }

  public boolean isConnected() {
    return rbnb_.isConnected();
    //return sink != null;
  }

  public void connect() {
    connect(false);
  }

  public boolean connect(boolean block) {
    if (isConnected()) {
      return true;
    }

    if (block) {
      final Thread object = Thread.currentThread();
      ConnectionListener listener = new ConnectionListener() {
        public void connecting() {}
        public void connected() {
          synchronized (object) {
            object.notify();
          }
        }
        public void connectionFailed() {
          object.interrupt();
        }
      };
      addConnectionListener(listener);

      synchronized (object) {
        setState(STATE_STOPPED);

        try {
          object.wait();
        } catch (InterruptedException e) {
          return false;
        }
      }

      removeConnectionListener(listener);
    } else {
      setState(STATE_STOPPED);
    }

    return true;
  }

  /**
   * Cancel a connection attempt.
   */
  public void cancelConnect() {
    if (rbnbThread != null) {
      rbnbThread.interrupt();
    }
  }

  /**
   * Disconnect from the RBNB server. This method will return immediately.
   */
  public void disconnect() {
    disconnect(false);
  }

  /**
   * Disconnect from the RBNB server. If block is set, this method will not
   * return until the server has disconnected.
   * 
   * @param block  if true, wait for the server to disconnect
   * @return       true if the server disconnected
   */
  public boolean disconnect(boolean block) {
    if (!isConnected()) {
      return true;
    }

    if (block) {
      final Thread object = Thread.currentThread();
      StateListener listener = new StateListener() {
        public void postState(int newState, int oldState) {
          if (newState == STATE_DISCONNECTED) {
            synchronized (object) {
              object.notify();
            }
          }
        }
      };
      addStateListener(listener);

      synchronized (object) {
        setState(STATE_DISCONNECTED);

        try {
          object.wait();
        } catch (InterruptedException e) {
          return false;
        }
      }

      removeStateListener(listener);

    } else {
      setState(STATE_DISCONNECTED);
    }

    return true;
  }

  public void reconnect() {
    setState(STATE_DISCONNECTED);
    setState(STATE_STOPPED);
  }

  public void addSubscriptionListener(SubscriptionListener subscriptionListener) {
    subscriptionListeners.add(subscriptionListener);
  }

  public void removeSubscriptionListener(
      SubscriptionListener subscriptionListener) {
    subscriptionListeners.remove(subscriptionListener);
  }

  // Message Methods

  private void fireErrorMessage(String errorMessage) {
    for (int i = 0; i < messageListeners.size(); i++) {
      MessageListener messageListener = (MessageListener) messageListeners
          .get(i);
      messageListener.postError(errorMessage);
    }
  }

  private void fireStatusMessage(String statusMessage) {
    for (int i = 0; i < messageListeners.size(); i++) {
      MessageListener messageListener = (MessageListener) messageListeners
          .get(i);
      messageListener.postStatus(statusMessage);
    }
  }

  public void addMessageListener(MessageListener messageListener) {
    messageListeners.add(messageListener);
  }

  public void removeMessageListener(MessageListener messageListener) {
    messageListeners.remove(messageListener);
  }

  // Connection Listener Methods

  private void fireConnecting() {
    for (int i = 0; i < connectionListeners.size(); i++) {
      ConnectionListener connectionListener = (ConnectionListener) connectionListeners
          .get(i);
      connectionListener.connecting();
    }
  }

  private void fireConnected() {
    for (int i = 0; i < connectionListeners.size(); i++) {
      ConnectionListener connectionListener = (ConnectionListener) connectionListeners
          .get(i);
      connectionListener.connected();
    }
  }

  private void fireConnectionFailed() {
    for (int i = 0; i < connectionListeners.size(); i++) {
      ConnectionListener connectionListener = (ConnectionListener) connectionListeners
          .get(i);
      connectionListener.connectionFailed();
    }
  }

  public void addConnectionListener(ConnectionListener connectionListener) {
    connectionListeners.add(connectionListener);
  }

  public void removeConnectionListener(ConnectionListener connectionListener) {
    connectionListeners.remove(connectionListener);
  }

  // Public Metadata Methods

  /**
   * Gets the <code>MetadataManager</code>.
   * 
   * @return the metadata manager
   */
  public MetadataManager getMetadataManager() {
    return metadataManager;
  }

  /**
   * Gets the <code>Channel</code> with this <code>channelName</code>. This is
   * a convenience method for the same method in <code>MetadataManager</code>.
   * 
   * @param channelName  the name of the channel
   * @return             the channel or null if it was not found
   * @see                MetadataManager#getChannel(String)
   */
  public Channel getChannel(String channelName) {
    return metadataManager.getChannel(channelName);
  }

  /**
   * Gets a list of <code>Channel<code>'s. This is a convenience method for the
   * same method in <code>MetadataManager</code>.
   * 
   * @return  a list of channels
   * @see     MetadataManager#getChannels()
   */
  public List<Channel> getChannels() {
    return metadataManager.getChannels();
  }

  /**
   * Gets a list of <code>Channel</code>'s with the <code>channelNames</code>.
   * This is a convenience method for the same method in
   * <code>MetadataManager</code>.
   * 
   * @param channelNames  the channel names to get
   * @return              a list of channels
   * @see                 MetadataManager#getChannels(List)
   */
  public List<Channel> getChannels(List<String> channelNames) {
    return metadataManager.getChannels(channelNames);
  }

  /**
   * Gets the <code>ChannelTree</code>. This is a convenience method for the
   * same method in <code>MetadataManager</code>.
   * 
   * @return  the channel tree
   * @see     MetadataManager#getChannelTree()
   */
  public ChannelTree getChannelTree() {
    return metadataManager.getChannelTree();
  }

  /**
   * Updates the metadata. This is a convenience method for the same method in
   * <code>MetadataManager</code>.
   * 
   * @see MetadataManager#updateMetadataBackground()
   */
  public void updateMetadata() {
    metadataManager.updateMetadataBackground();
  }

  // Public Marker Methods

  public MarkerManager getMarkerManager() {
    return markerManager;
  }

  // Public Static Methods

  public static String getStateName(int state) {
    String stateString;

    switch (state) {
    case STATE_LOADING:
      stateString = "loading";
      break;
    case STATE_PLAYING:
      stateString = "playback";
      break;
    case STATE_MONITORING:
      stateString = "real time";
      break;
    case STATE_STOPPED:
      stateString = "stopped";
      break;
    case STATE_EXITING:
      stateString = "exiting";
      break;
    case STATE_DISCONNECTED:
      stateString = "disconnected";
      break;
    default:
      stateString = "UNKNOWN";
    }

    return stateString;
  }

  /**
   * Returns the state code for a given state name
   * 
   * @param stateName  the state name
   * @return           the state code
   */
  public static int getState(String stateName) {
    int code;

    if (stateName.equals("loading")) {
      code = STATE_LOADING;
    } else if (stateName.equals("playback")) {
      code = STATE_PLAYING;
    } else if (stateName.equals("real time")) {
      code = STATE_MONITORING;
    } else if (stateName.equals("stopped")) {
      code = STATE_STOPPED;
    } else if (stateName.equals("exiting")) {
      code = STATE_EXITING;
    } else if (stateName.equals("disconnected")) {
      code = STATE_DISCONNECTED;
    } else {
      code = -1;
    }

    return code;
  }


  class RequestHandler {
    private SubscriptionRequest request_;
    private boolean requestServiced_=false;  ///< Have we gotten any history data for this request yet.
    private TimeSeriesComposite monitorPostedTimeSeries_=null;
    private List<TimeSeriesData> monitorTimeSeriesQueue_=new ArrayList<TimeSeriesData>();
    private List<TimeSeriesComposite> postedTimeSeriesQueue_=new ArrayList<TimeSeriesComposite>();
    private Map<VisualizationSeries, TimeSeriesComposite> seriesDataMap_=
                                new HashMap<VisualizationSeries, TimeSeriesComposite>();
    
    //private TimeRange hasDataRange=null;
    private TimeRange requestedDataRange=null;
    
    public RequestHandler (SubscriptionRequest r){
      request_=r;
    }
    
    public String toString(){
      StringBuffer retBuf=new StringBuffer();
      retBuf.append("svc: ").append(requestServiced_);
      retBuf.append(" range: ").append(requestedDataRange).append('\n');
      retBuf.append(request_);
      return retBuf.toString();
    }
    
    public boolean hasRequest(){
      return (request_!=null);
    }
    /***
     * Sub/unsub channels from new request.
     * @param r The new request to modify existing request.
     */
    public void updateRequest(SubscriptionRequest r){
      if(request_!=null){
        request_=request_.combineChannels(r);
      }else{
        request_=r;   
      }
      // reset data range if we get new channels
      requestedDataRange=null;
      log.debug("updating request");
      requestServiced_=false;
    }
   
    public SubscriptionRequest getRequest(){
      return request_;
    }
     
    public boolean serviced(){
      return requestServiced_;
    }
    
    public RBNBQuery getHistoryRequest(double end){
      double start = end-request_.getTimeRange();
      
      if(requestedDataRange!=null){
        if(requestedDataRange.contains(new TimeRange(start,end))){
          // we already have the data
          requestedDataRange=new TimeRange(start,end);
          log.warn("returning null history request");
          return null;
        }else if(end>requestedDataRange.end && 
            start>requestedDataRange.start && 
            start<requestedDataRange.end){
          // history we have received overlaps with new 
          // request...
          log.debug("shortening history request");
          QRAFixedRange qrafr=new QRAFixedRange(end-requestedDataRange.end-
                                                RBNBTime.ONE_MICROSECOND);
          qrafr.addSubscriptionRequest(request_);
          return RBNBQuery.createAbsoluteQuery(qrafr, requestedDataRange.end+
                                                RBNBTime.ONE_MICROSECOND);
        }
      }
      log.debug("normal history request");
      QRAChart chartReqs = new QRAChart(true);
      chartReqs.addSubscriptionRequest(request_);
      return RBNBQuery.createAbsoluteHistoryQuery(chartReqs,end);
    }
    
    public SubscriptionResponse getResponse(RBNBQuery q){ 
      switch(q.getContext()){
      case MONITOR:
        return getMonitoringResponse(q);
      case PLAYBACK:
        return getPlaybackResponse(q);
      default:
        return getHistoryResponse(q);
      }
    }
    
    private SubscriptionResponse getMonitoringResponse(RBNBQuery q){
      if(request_==null || !requestServiced_ || // !q.gotData() ||
          !q.containsRequest(request_)){
        return null;
      }
    
      // invalidate range of data we have received
      //hasDataRange=null;
      QueryResponse retResp=new QueryResponse(request_.getListener(),
                                                    q.getRequestEndTime(),
                                                    dropData_, false);
      TimeSeriesData currTimeSeries=q.getTimeSeries();
      
      for(VisualizationSeries s:request_.getSeriesList()){
        if(hasAllChannels(s,currTimeSeries.getChannelNames())){
          
          retResp.addTimeSeries(s,currTimeSeries);
        }else if(hasSomeChannels(s,currTimeSeries.getChannelNames())){
          TimeSeriesComposite currTsc = seriesDataMap_.get(s);
          if(currTsc==null){
            // store for later
            seriesDataMap_.put(s,new TimeSeriesComposite(currTimeSeries));
          }else{
            currTsc.addTimeSeries(currTimeSeries);
            if(hasAllChannels(s,currTsc.getChannelNames()) && currTsc.hasData()){
              // post to listener
              retResp.addTimeSeries(s, currTsc);
              
              // remove data from map
              seriesDataMap_.remove(s);
            }
          }
        }
      }
      
      return (retResp.getSeriesCount()>0)?
              (SubscriptionResponse)retResp:null;
    }
    
//    private SubscriptionResponse getMonitoringResponse(RBNBQuery q){
//      if(request_==null || !requestServiced_ || !q.gotData())
//        return null;
//      
//      // invalidate range of data we have received
//      //hasDataRange=null;
//      SubscriptionResponse retResp=null;
//      TimeSeriesData currTimeSeries=q.getTimeSeries();
//      
//      // only notify requestors if response contains channels they
//      // are interested in - added because monitoring queries may
//      // return data from different channels based on data rates
//      // etc.
//      if(hasAllChannels(currTimeSeries)){
//        currTimeSeries.addListener(request_.getListener());
//        retResp=new QueryResponse(request_.getListener(),
//                            currTimeSeries,dropData_, false);
//        //request_.notify(resp);
//        
//      }else if(hasSomeChannels(currTimeSeries)){
//        currTimeSeries.addListener(request_.getListener());
//        // start building composite timeseries
//        if(monitorPostedTimeSeries_==null){
//          log.debug("creating new series composite at "+
//              RBNBTime.formatISO(currTimeSeries.getMaxEndTime()));
//          monitorPostedTimeSeries_=new TimeSeriesComposite(currTimeSeries);
//          monitorTimeSeriesQueue_.add(currTimeSeries);
//        }else{
//          monitorPostedTimeSeries_.addTimeSeries(currTimeSeries);
//          monitorTimeSeriesQueue_.add(currTimeSeries);
//          // if new time series satisfies all channels
//          // and contains data that overlaps, post it
//          if(hasAllChannels(monitorPostedTimeSeries_) && 
//              monitorPostedTimeSeries_.hasData()){
//            log.debug("posting data between "+RBNBTime.formatISO(monitorPostedTimeSeries_.getMinStartTime())+
//                " and "+RBNBTime.formatISO(monitorPostedTimeSeries_.getMaxEndTime()));
//            retResp=new QueryResponse(request_.getListener(),
//              monitorPostedTimeSeries_,dropData_, false);
//            
//            postedTimeSeriesQueue_.add(monitorPostedTimeSeries_);
//            
//            //build new composite timeseries
//            log.debug("creating new series composite");
//            TimeSeriesComposite newTsc=new TimeSeriesComposite();
//            Iterator<TimeSeriesData>addIt=monitorTimeSeriesQueue_.iterator();
//            while (addIt.hasNext()){
//              TimeSeriesData addTsd=addIt.next();
//              if(addTsd.getMaxEndTime()>monitorPostedTimeSeries_.getMaxEndTime()){
//                log.debug("adding ts ending at "+
//                    RBNBTime.formatISO(addTsd.getMaxEndTime()));
//                newTsc.addTimeSeries(addTsd);
//              }
//            }
//            monitorPostedTimeSeries_=newTsc;
//            
//          }
//        }
//        
//        Iterator<TimeSeriesComposite> tscIt=postedTimeSeriesQueue_.iterator();
//        // remove old data
//        while(tscIt.hasNext()){
//          TimeSeriesComposite postedTsc=tscIt.next();
//          // harvest old time series
//          if(postedTsc.listenerDone()){
//            double postEnd=postedTsc.getMaxEndTime();
//
//            Iterator<TimeSeriesData>tsIt=monitorTimeSeriesQueue_.iterator();
//            while (tsIt.hasNext()){
//              TimeSeriesData nextTsd=tsIt.next();
//              // delete old time series
//              if(nextTsd.getMaxEndTime()<=postEnd){
//                log.debug("freeing ts ending at "+
//                    RBNBTime.formatISO(nextTsd.getMaxEndTime()));
//                nextTsd.free(request_.getListener());
//                tsIt.remove();
//              }
//            }
//            tscIt.remove();
//          }
//        }
//        log.debug("pts queue size: "+postedTimeSeriesQueue_.size()+", mts queue size: "+
//            monitorTimeSeriesQueue_.size());
//        
//      }//else{
////        retResp=new QueryResponse(request_.getListener(),
////            currTimeSeries,dropData_, false);
////      }
//      return retResp;
//    }
    
    private boolean hasSomeChannels(VisualizationSeries s, List<String> chanList){
      boolean hasSomeReqChannels=false;
      Iterator<String> chanIt=s.getServerChannels().iterator();
      while (chanIt.hasNext()){
        if(chanList.contains(chanIt.next())){
          hasSomeReqChannels=true;
          break;
        }
      }
      return hasSomeReqChannels;
    }
    
    private boolean hasAllChannels(VisualizationSeries s, List<String> chanList){
      boolean hasAllReqChannels=true;
      Iterator<String> chanIt=s.getServerChannels().iterator();
      while (chanIt.hasNext()){
        if(!chanList.contains(chanIt.next())){
          hasAllReqChannels=false;
          break;
        }
      }
      return hasAllReqChannels;
    }
    
    private SubscriptionResponse getHistoryResponse(RBNBQuery q){
      if(request_==null || !q.containsRequest(request_)) return null;
      
      //log.debug("request serviced");
      requestServiced_=true;
      QueryResponse retResp=new QueryResponse(request_.getListener(),
                                               q.getRequestEndTime(),
                                               dropData_,true);
      setRequestedDataRange(q);
      List<String> reqChannels = q.getRequestedChannels();
      
      TimeSeriesData currTimeSeries=q.getTimeSeries();
      
      for(VisualizationSeries s:request_.getSeriesList()){
        if(hasAllChannels(s,reqChannels)){
          
          retResp.addTimeSeries(s,currTimeSeries);
        }else if(hasSomeChannels(s,reqChannels)){
          
        }
      }
//      if(q.containsRequest(request_)){
//        if(reqHasAll(reqChannels)){
//          setRequestedDataRange(q);
//          //setHasDataRange(q.getTimeSeries());
//          q.getTimeSeries().addListener(request_.getListener());
//          retResp=new QueryResponse(request_.getListener(),
//                                  q.getTimeSeries(),dropData_,true);
//          //request_.notify(resp);
//        }else if(reqHasSome(reqChannels)){
//          // channels located on different servers???
//          // unsupported at this time
//          
//        }
//      }
      return (retResp.getSeriesCount()>0)?
          (SubscriptionResponse)retResp:null;
    }
    
    private void setRequestedDataRange(RBNBQuery query){
      double end=query.getRequestEndTime();
      double start=end-query.getRequestDuration();
      log.debug("Set request data range start: "+RBNBTime.formatISO(start)
          +" end: "+RBNBTime.formatISO(end));
      requestedDataRange=new TimeRange(start,end);
    }
    
    private void slideRequestedDataRange(RBNBQuery query){
      double end=query.getRequestEndTime();
      double start=end-query.getRequestDuration();
       
      if (requestedDataRange!=null){
         double oldDuration=requestedDataRange.length();
         if(Math.abs(start-requestedDataRange.end)<=2*RBNBTime.ONE_MICROSECOND){
           // shift window of data forward
           // TODO may not jive with panels that are lazy 
           // about aging data
           log.debug("Sliding request data range to end: "+RBNBTime.formatISO(end));
           requestedDataRange=new TimeRange(end-oldDuration,end);
         }else{
           log.debug("Reset request data range start: "+RBNBTime.formatISO(start)
               +" end: "+RBNBTime.formatISO(end));
           // reset data range
           requestedDataRange=new TimeRange(start,end);
         }
       }else{
         log.debug("Reset request data range start: "+RBNBTime.formatISO(start)
             +" end: "+RBNBTime.formatISO(end));
         // reset data range
         requestedDataRange=new TimeRange(start,end);
       }
    }
    
//    // set to min/max for my channels only
//    private void setHasDataRange(TimeSeriesData tsd){
//      double newStart=Double.MAX_VALUE,newEnd=0.0;
//      
//      Iterator<String> chanIt=request_.getServerChannelNameIterator();
//      while (chanIt.hasNext()){
//        int channelIndex=tsd.getChannelIndex(chanIt.next());
//        if(channelIndex>=0){
//          Double start=tsd.getStartTime(channelIndex);
//          Double end=tsd.getEndTime(channelIndex);
//          if(start!=null && start<newStart){
//            newStart=start.doubleValue();
//          }
//          if(end!=null && end>newEnd){
//            newEnd=end.doubleValue();
//          }
//        }
//      }
//      if(newStart!=Double.MAX_VALUE && newEnd != 0.0){
//        hasDataRange=new TimeRange(newStart,newEnd);
//      }
//    }
    
    private SubscriptionResponse getPlaybackResponse(RBNBQuery q){
      if(request_==null || !requestServiced_ || //!q.gotData() ||
          !q.containsRequest(request_)){
        return null;
      }
      // invalidate range of data we have received
      //hasDataRange=null;
      QueryResponse retResp=new QueryResponse(request_.getListener(),
                                        q.getRequestEndTime(),
                                        dropData_,false);
      
      List<String> reqChannels = q.getRequestedChannels();
      
      TimeSeriesData currTimeSeries=q.getTimeSeries();
      
      for(VisualizationSeries s:request_.getSeriesList()){
        if(hasAllChannels(s,reqChannels)){
          slideRequestedDataRange(q);
          retResp.addTimeSeries(s, currTimeSeries);
        }else{
          //???
        }
      }
      
//      if(reqHasAll(reqChannels)){
//        slideRequestedDataRange(q);
//        TimeSeriesData currTimeSeries=q.getTimeSeries();
//        //log.debug("creating slice from "+RBNBTime.formatISO(location)+" to "+RBNBTime.formatISO(newLocation));  
//        currTimeSeries.addListener(request_.getListener());
//        retResp=new QueryResponse(request_.getListener(),
//                                  currTimeSeries,dropData_,false);
//        //request_.notify(resp);
//      
//      }else if(reqHasSome(reqChannels)){
//          // channels located on different servers???
//          // unsupported at this time
//      }
      
      return (retResp.getSeriesCount()>0)?
          (SubscriptionResponse)retResp:null;
    }
    
//    private boolean reqHasAll(List<String> channels){
//      boolean ret=true;  
//      Iterator<String> chanIt=request_.getServerChannelNameList().iterator();
//      while (chanIt.hasNext()){
//        if(!channels.contains(chanIt.next())){
//          ret=false;
//          break;
//        }
//      }
//      return ret;
//    }
//    
//    private boolean reqHasSome(List<String> channels){
//      boolean all=true, one=false;  
//      Iterator<String> chanIt=request_.getServerChannelNameList().iterator();
//      while (chanIt.hasNext()){
//        if(!channels.contains(chanIt.next())){
//          all=false;
//        }else{
//          one=true;
//        }
//      }
//      return (one&&!all);
//    }
    
    
  }
  
  /***
   * Private thread to execute RBNB queries and retrieve data when 
   * in playback mode.
   */
  class PrefetchExecutor implements Runnable {
    Thread execThread_;
    List<RBNBQuery> pendingReqs_=new ArrayList<RBNBQuery>();
    List<RBNBQuery> completedReqs_=new ArrayList<RBNBQuery>();
    
    public PrefetchExecutor(){
     execThread_=new Thread(this,"Prefetch");
     execThread_.setPriority(Thread.MAX_PRIORITY);
     execThread_.start();
    
    }
    
    public void reset(){
      synchronized(pendingReqs_){
        pendingReqs_.clear();
      }
      synchronized(completedReqs_){
        completedReqs_.clear();
      }
    }
    
    public void queue(RBNBQuery r){
      synchronized(pendingReqs_){
        pendingReqs_.add(r);
        pendingReqs_.notify();
      }
    }
    
    public int pendingCount(){
      int ret;
      synchronized(pendingReqs_){
        ret=pendingReqs_.size();
      }
      return ret;
    }
    
    /***
     * Dequeue the next completed query. Does not wait if none is available.
     * @return RBNBQuery if available, null otherwise.
     */
    public RBNBQuery dequeue(){
      RBNBQuery r=null;
      //while(r==null){
        synchronized(completedReqs_){
          if (completedReqs_.size()>0){
            r=completedReqs_.remove(0);
          }/*else{
            try {
              completedReqs_.wait(500);
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }*/
        } 
      //}
      return r;
    }
    
    public void kill(){
      execThread_.interrupt();
    }
    
    /***
     * Main loop to execute queries. Blocks if no queries are 
     * pending.
     */
    public void run(){
      while (true){
        if(execThread_.isInterrupted())
          break;
        RBNBQuery r=null;
        synchronized(pendingReqs_){
          if(pendingReqs_.size()>0){
            r=pendingReqs_.remove(0);
          }else{
            try {
              pendingReqs_.wait();
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
        }
        
        if(r!=null){
          try {
            r.execute(rbnb_);
          }catch (Exception e){
            log.error("Failed to fetch data.");
            e.printStackTrace();
          }
          
          synchronized(completedReqs_){
            completedReqs_.add(r);
            completedReqs_.notify();
          }
        }
        
      }
    }
  }
 
}