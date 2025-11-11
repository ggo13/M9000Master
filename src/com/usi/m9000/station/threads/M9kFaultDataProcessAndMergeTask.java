package com.usi.m9000.station.threads;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.station.M9kLED;
import com.usi.m9000.station.Exception.M9kThreadExceptionHandler;
import com.usi.m9000.station.dto.TriggersScheduleDTO;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.xml.util.M9kXMLUtils;



public class M9kFaultDataProcessAndMergeTask{

	M9kStationCommandClient commandClient;
	static HierarchicalINIConfiguration iniConf;
	private Connection mysqlConn;
	private int runFrequency = 1; // in seconds
	private ResourceBundle comtradeBundle;
//	private List<TriggersScheduleDTO> lstTriggersScheduleDTO;
	private TriggersScheduleDTO currentTriggersScheduleDTO;
	private static long currentStartTime = 0;
	private static long currentStopTime = 0;
	private static long lastTriggeredStopTime = 0;
	private int trackStartStopStatus = 0;
	private long irigTime;
	private static long deckTime;
	private static long triggerLimit;
	private int latencyDelay;
	private int triggerTimeLapse;
//	private int threadPoolSize;
	private static StationDTO stationDetails;
	private static boolean isWaitOver = false;
	ExecutorService executor;
	ExecutorService ltrExecutor;
	ExecutorService threadHandlerExecutor;
	StringBuffer strIdTriggersToRemove;
	
	private static long ltrStartTime = 0;
	private static long ltrStopTime = 0;
//	private long ltrIrigTime;
	private long ltrDeckTime = 0;
//	private int ltrTriggerLimit;
	// START: 18-Jun-2024 - Rename ltrTriggerLimit to ddrDeckLimit and read it from GUI
//	private int ddrDeckTimeLimit = 300; // in seconds
	// END: 18-Jun-2024
	
	private static long lastLtrStopTime = 0;
	
	private static Map<String, Integer> mapDfrStartStopStatus;
	 private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	 ScheduledFuture<?> scheduledTask = null;
	 Future<?> scheduledCrossRecoredTask = null;
	private static int waitStopTimeCounter = 0; 
	
	// START: 30-Apr-2024 : Implement decktime and trigger limit separately
	private int deckTimeLimit = 0;
	private int triggerTimeLimit = 0;
	// END: 30-Apr-2024
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kFaultDataProcessAndMergeTask.class);
	public M9kFaultDataProcessAndMergeTask() {
		super();
		initialize();
	}

	private void initialize()
	{
//		lstTriggersScheduleDTO = new ArrayList<TriggersScheduleDTO>();
		currentTriggersScheduleDTO = null;
		mapDfrStartStopStatus = new HashMap<String, Integer>(10);
		comtradeBundle = ResourceBundle.getBundle("M9K_COMTRADE");
		try {
			iniConf = new HierarchicalINIConfiguration("station.properties");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		try
		{
//			triggerLimit = Integer.parseInt(comtradeBundle.getString("trigger-limit").trim());
			triggerTimeLimit = M9kXMLUtils.getTheMaximumTriggerLimitFromConfig();
			logger.debug("triggerTimeLimit from config: " + triggerTimeLimit);
		}
		catch (Exception e) {
			e.printStackTrace();
			triggerTimeLimit = 15; // Default seconds
		}
		// START: 30-Apr-2024 : Implement decktime and trigger limit separately
		try
		{
			deckTimeLimit = Integer.parseInt(comtradeBundle.getString("decktime-limit").trim());
			if (triggerTimeLimit >0 && deckTimeLimit > triggerTimeLimit)
			{
				deckTimeLimit = triggerTimeLimit;
				logger.info("decktime-limit is greater than trigger-time-limit, so setting decktime-limit to trigger-time-limit. trigger-time-limit: "+triggerTimeLimit+" decktime-limit: "+deckTimeLimit);
			}
			logger.debug("deckTimeLimit from config: " + deckTimeLimit);
		}
		catch (Exception e) {
			e.printStackTrace();
			deckTimeLimit = 10; // Default seconds
		}
		// END: 30-Apr-2024

		try
		{
			latencyDelay = Integer.parseInt(comtradeBundle.getString("latency-delay".trim()));
		}
		catch (Exception e) {
			e.printStackTrace();
			latencyDelay = 1; // Default seconds
		}
		
		try
		{
			triggerTimeLapse = Integer.parseInt(comtradeBundle.getString("trigger-time-lapse").trim());
		}
		catch (Exception e) {
			e.printStackTrace();
			triggerTimeLapse = 5; // Default seconds
		}
//		try
//		{
//			threadPoolSize = Integer.parseInt(comtradeBundle.getString("thread-pool-size"));
//		}
//		catch (Exception e) {
//			threadPoolSize = 100; // Default seconds
//		}
//		START: 18-Jun-2024 - Rename ltrTriggerLimit to ddrDeckLimit and read it from GUI
//		try
//		{
////			ltrTriggerLimit = Integer.parseInt(comtradeBundle.getString("ltr-trigger-limit").trim());
//			ddrDeckTimeLimit = M9kStationXMLUtil.getDdrDeckTimeLimit();
//			// END: 18-Jun-2024
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			logger.warn("Unable to read ltr-trigger-limit value. default set to 300",e);
////			ltrTriggerLimit = 300; // Default seconds
//			ddrDeckTimeLimit = 300; // Default seconds
//		}
//		logger.debug("ddrDeckTimeLimit: "+ddrDeckTimeLimit);
		// END: 18-Jun-2024
		try
	  	  {
	 			  M9kStationUtil.petWatchDog();
	  	  }
	  	  catch(RuntimeException re)
	  	  {
	  		  logger.error("Error in petting watch dog ",re);
	  	  }
  	  catch(Exception e)
  	  {
  		  logger.error("Error in petting watch dog ",e);
  	  }
		cleanupTriggersUpdateTable();
//		stationDetails = M9kStationDBUtil.getStationDetails();
//		executor = Executors.newCachedThreadPool();
		// Saravanan test 06-July-2012
		executor = new ThreadPoolExecutor(2, 3, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),new ThreadPoolExecutor.DiscardPolicy());
//		executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
//                new LinkedBlockingQueue<Runnable>(),new ThreadPoolExecutor.DiscardPolicy());

		((ThreadPoolExecutor)executor).allowCoreThreadTimeOut(true);
		ltrExecutor = Executors.newCachedThreadPool();
		threadHandlerExecutor =  Executors.newCachedThreadPool();
	}
	
	
	public void scheduleFaultDataProcess()
	{
		  final Runnable taskPerformer = new Runnable() {
              public void run() {
            	  try
            	  {
           			  M9kStationUtil.petWatchDog();
            	  }
            	  catch(RuntimeException re)
            	  {
            		  logger.error("Error in petting watch dog ",re);
            	  }
            	  catch(Exception e)
            	  {
            		  logger.error("Error in petting watch dog ",e);
            	  }
            	  try
            	  {

            		  processFaultData();
            	  }
            	  catch(RuntimeException re)
            	  {
            		  logger.error("Error in processFaultData ",re);
//            		  try {
//  						logger.info("After each task execution "+scheduledTask.get());
//  					} catch (InterruptedException e) {
//  						// TODO Auto-generated catch block
//  						e.printStackTrace();
//  						logger.error("Exception during task execution ", e);
//  					} catch (ExecutionException e) {
//  						// TODO Auto-generated catch block
//  						e.printStackTrace();
//  						logger.error("Exception during task execution ", e);
//  					}
            	  }
            	  catch(Exception e)
            	  {
            		  logger.error("Error in processFaultData ",e);
//            		  try {
//  						logger.info("After each task execution "+scheduledTask.get());
//  					} catch (InterruptedException ie) {
//  						// TODO Auto-generated catch block
//  						e.printStackTrace();
//  						logger.error("Exception during task execution ", ie);
//  					} catch (ExecutionException ee) {
//  						// TODO Auto-generated catch block
//  						e.printStackTrace();
//  						logger.error("Exception during task execution ", ee);
//  					}
            	  }
              }
              
          };
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, getRunFrequency(), TimeUnit.SECONDS);
          threadHandlerExecutor.execute(new M9kThreadExceptionHandler(scheduledTask));
          // Add a shutdown hook to stop the thread pools gracefully when the application exits.  
          Runtime.getRuntime().addShutdownHook(new Thread("[ThreadPool-Shutdown]") {  
                
              @Override  
              public void run() {  
            	  logger.debug("Inside shutdown hook.");
                  if(scheduler != null && !scheduler.isShutdown()) {  
                	  scheduler.shutdownNow();  
                  }  
                    
              }  
          });  
	}

	public void processFaultData() {
		List<TriggersScheduleDTO> lstNewSetTriggersScheduleDTO;
		  stationDetails = M9kStationDBUtil.getStationDetails();
//		  logger.debug("Station Details during fault processing  "+stationDetails);
//			logger.debug("Sample Rate from local station details "+stationDetails.getSystemSampleRate());

//		TriggersScheduleDTO triggersScheduleDTO;
		trackStartStopStatus = 0;
		irigTime = -1;
		while(true)
		{
			try
			{
				M9kStationUtil.petWatchDog();
			}
			catch(RuntimeException re)
			{
				logger.error("Error in petting watch dog ",re);
			}
			catch(Exception e)
			{
				logger.error("Error in petting watch dog ",e);
			}
			try
			{
//				logger.debug("\t\t\t Last Trigger Stop time "+lastTriggeredStopTime);
				if (ltrDeckTime > 0 && stationDetails.getSystemLongTermSampleRate() > 0)
				{
					irigTime = M9kStationUtil.getIRIG();
					logger.debug("LTR IRIG Time "+irigTime+" LTR deck time "+ltrDeckTime+" Diff bet irig and deck time "+(ltrDeckTime - irigTime)+" ltr start time "+ltrStartTime+" ltr stop time "+ltrStopTime);
//					if (irigTime > ltrDeckTime)
					if (irigTime >= ltrDeckTime)
					{
						logger.debug("LTR Long term record hit the deck time. Invoking with start time "+ltrStartTime+" Stop Time "+ltrDeckTime);
						lastLtrStopTime = ltrDeckTime;
						// Check for disk space before recording
						if (M9kStationUtil.isDiskspaceAvailableToContinueRecording())
						{
							// Process LTR with startTime and deck time as stop time
							ltrExecutor.execute(new M9kLTRProcesssTask(ltrStartTime, ltrDeckTime));
							ltrExecutor.execute(new M9kLtrExportsProcesssTask(ltrStartTime, ltrDeckTime));
						}
						else
						{
							logger.warn("Recording is stopped as disk space is critical. LTR is not created for startTime "+ltrStartTime+" Endtime "+ltrDeckTime);
						}
						logger.debug("Old LTR start Time "+ltrStartTime);
						ltrStartTime = (long)(ltrDeckTime +((1/stationDetails.getSystemLongTermSampleRate())*1000000));
//						if (ltrStopTime <= ltrDeckTime)
						if (ltrStopTime <= ltrStartTime)
						{
//							ltrStopTime = (long)ltrStartTime+(stationDetails.getSystemLtrPrefaultTime()*1000000) - (ltrDeckTime-ltrStopTime);
							ltrStartTime = 0;
							ltrStopTime = 0;
							ltrDeckTime = 0;
						}
						else
						{
							// START: 18-Jun-2024 - Rename ltrTriggerLimit to ddrDeckLimit and read it from GUI
//							ltrDeckTime = ltrStartTime + (ltrTriggerLimit * 1000000);
							ltrDeckTime = ltrStartTime + (M9kStationXMLUtil.getDdrDeckTimeLimit() * 1000000);
							// END: 18-Jun-2024
						}
						logger.debug("New LTR start Time "+ltrStartTime);
						//					ltrStopTime = 0;
					}
//					else if (irigTime > (ltrStopTime+(stationDetails.getSystemLtrPrefaultTime()*1000000)))
					else if (irigTime >= ltrStopTime)
					{
						logger.debug("LTR Long term record hit the white space");
						// Process LTR with startTime and deck time as stop time
						lastLtrStopTime = ltrStopTime;
						if (M9kStationUtil.isDiskspaceAvailableToContinueRecording())
						{
							ltrExecutor.execute(new M9kLTRProcesssTask(ltrStartTime, ltrStopTime));
							ltrExecutor.execute(new M9kLtrExportsProcesssTask(ltrStartTime, ltrStopTime));
						}
						else
						{
							logger.warn("Recording is stopped as disk space is critical. LTR is not created for startTime "+ltrStartTime+" Endtime "+ltrStopTime);
						}
						ltrStartTime = 0;
						ltrStopTime = 0;
						ltrDeckTime = 0;
					}
				}
				lstNewSetTriggersScheduleDTO = getTriggersSchedule();
				//			if (lstNewSetTriggersScheduleDTO.isEmpty() && lstTriggersScheduleDTO.isEmpty())
				if (lstNewSetTriggersScheduleDTO.isEmpty() && currentTriggersScheduleDTO == null && trackStartStopStatus == 0)
				{
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					processSleep(5000);
					logger.debug("\t\t\t\t\t\t No record found. Continuing ");
//					continue;
					break;
				}
				
				logger.debug("\t\t\t\tRetrived data to process lstNewSetTriggersScheduleDTO "+lstNewSetTriggersScheduleDTO.size()+"  lstTriggersScheduleDTO "+currentTriggersScheduleDTO);
//				while (lstNewSetTriggersScheduleDTO.size() > 1) 
//				{
//					TriggersScheduleDTO triggersScheduleDTO = lstNewSetTriggersScheduleDTO.get(0);
//					logger.debug("\t\t\t\t\t\t Invoking cross record thread from a for loop");
//					lastTriggeredStopTime = triggersScheduleDTO.getStopTime()+(stationDetails.getSystemPostfaultTime()*1000);
//					//				executor.execute(new M9kFaultDataMergeTask(triggersScheduleDTO));
////					processSleep(50);				
//					spawnNewThreadToCrossRecordAndMerge(triggersScheduleDTO);
//					lstNewSetTriggersScheduleDTO.remove(0);
//				}
				logger.debug("\t\t\tIs wait over flag "+isWaitOver);
				if (isWaitOver) // True if it has waited for a latency time after hitting a white space
				{
					isWaitOver = false;
					if (lstNewSetTriggersScheduleDTO.isEmpty())
					{
						//					triggersScheduleDTO = lstTriggersScheduleDTO.get(0);
						//					currentStartTime-= stationDetails.getSystemPrefaultTime()*1000;
						//					currentStopTime+=stationDetails.getSystemPostfaultTime()*1000;
						//					triggersScheduleDTO.setStartTime(currentStartTime);
						//					triggersScheduleDTO.setStopTime(currentStopTime);
						logger.debug("\t\t\t\t\t\t Invoking cross record thread after hitting a white space "+currentTriggersScheduleDTO);
						waitStopTimeCounter = 0;
						lastTriggeredStopTime = currentTriggersScheduleDTO.getStopTime()+(stationDetails.getSystemPostfaultTime()*1000);
						//					executor.execute(new M9kFaultDataMergeTask(currentTriggersScheduleDTO));
						spawnNewThreadToCrossRecordAndMerge(currentTriggersScheduleDTO);
						deckTime = 0;
						triggerLimit = 0;

						currentTriggersScheduleDTO = null;
						//					lstTriggersScheduleDTO.clear();
						currentStartTime = 0;
						currentStopTime = 0;
						continue;
					}
					//				else if (lstNewSetTriggersScheduleDTO.get(0).getStartTime() > (currentTriggersScheduleDTO.getStartTime()+(stationDetails.getSystemPrefaultTime()+stationDetails.getSystemPostfaultTime())))
					//				{
					//					logger.debug("\t\t\t\t\t\t Invoking cross record thread after hitting a white space and the subsequent record received after latency wait did not overlap "+currentTriggersScheduleDTO);
					//					executor.execute(new M9kFaultDataMergeTask(currentTriggersScheduleDTO));
					//					currentStartTime
					////					lstTriggersScheduleDTO.clear();
					//				}
				}
				//			lstTriggersScheduleDTO.addAll(lstNewSetTriggersScheduleDTO);
				if (!lstNewSetTriggersScheduleDTO.isEmpty())
				{
					currentTriggersScheduleDTO = lstNewSetTriggersScheduleDTO.get(0);
				}
				logger.debug("\t\t\t\t\t\t ABout to request IRIG ");
				irigTime = M9kStationUtil.getIRIG();
				
				if (deckTime == 0)
				{
					// START: 13-May-2024 - Decktime and TriggerLimit time are two different options and trigger limit time are read from config
//					deckTime = irigTime + (triggerLimit*1000000);
					deckTime = irigTime + (deckTimeLimit*1000000);
					triggerLimit = irigTime + (triggerTimeLimit*1000000);
					// END: 13-May-2024
					logger.debug("Setting the new deck time from the IRIG "+deckTime+" and new trigger time limit "+triggerTimeLimit);
				}
				logger.debug("\t\t\t\t\tIRIG "+irigTime+" decktime "+deckTime+" current start time "+currentStartTime+" trigger limit "+triggerLimit+" latencyDelay "+latencyDelay+" decktimelimit "+deckTimeLimit);
				if (trackStartStopStatus == 0) // Received end time and hit the white slot
				{
					logger.debug("(irigTime + (latencyDelay*1000000)) "+(irigTime + (latencyDelay*1000000)));
					logger.debug("(deckTime-1000000) "+(deckTime-1000000));
					logger.debug("IF ((irigTime + (latencyDelay*1000000)) < (deckTime-1000000)) "+((irigTime + (latencyDelay*1000000)) < (deckTime-1000000)));
					waitStopTimeCounter = 0;
					if ((irigTime + (latencyDelay*1000000)) < (deckTime-1000000)) // Check to see if irig time is within deck time
					{
//						try {
							logger.debug("About to wait for the latency delay time "+(latencyDelay*1000));
//							Thread.sleep(latencyDelay*1000); // Wait for the latency time before reading again
							processSleep(latencyDelay*1000);
							isWaitOver=true;
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						continue;
					}
					else if (irigTime >= deckTime)
					{
						logger.debug("\t\t\t\t\t\t Invoking cross record thread after hitting the deck time. Stop waiting for any new triggers.");
						//					triggersScheduleDTO = lstTriggersScheduleDTO.get(0);
						lastTriggeredStopTime = currentTriggersScheduleDTO.getStopTime()+(stationDetails.getSystemPostfaultTime()*1000);
						//					executor.execute(new M9kFaultDataMergeTask(currentTriggersScheduleDTO));
						spawnNewThreadToCrossRecordAndMerge(currentTriggersScheduleDTO);
						currentTriggersScheduleDTO = null;
						//					lstTriggersScheduleDTO.clear();
						currentStartTime = 0;
						currentStopTime = 0;
						deckTime = 0;
						triggerLimit = 0;

					}
				}
				else // We haven't received stop yet. Should wait for stop
				{
					logger.debug("About to compare Irig time "+irigTime +" with the deckTime "+deckTime+" and trigger time limit "+triggerLimit);
					// START: 13-May-2024 - Check for trigger limit time along with deck time
//					if (irigTime >= deckTime) // Hit the deck time before getting stop
					if (irigTime >= deckTime || (triggerLimit > 0 && irigTime >= triggerLimit)) // Hit the deck time before getting stop
					{
						// START: 13-May-2024 - Wait until trigger limit time to create faults
//						if (waitStopTimeCounter < 2)
						if (irigTime <= triggerLimit || triggerLimit <= deckTime)
						{
							logger.debug("\t\t\t\t\t\t Invoking cross record thread after waiting for stop and hit a deck in mean time "+deckTime);
							//					triggersScheduleDTO = lstTriggersScheduleDTO.get(0);
							if (currentTriggersScheduleDTO == null)
							{
								currentTriggersScheduleDTO = new TriggersScheduleDTO();
								currentTriggersScheduleDTO.setStartTime(currentStartTime);
							}
							// After hitting deck time, stop time is calculated as deckTime-postfault time(dfr will add postfault data ) and DON'T FORGET to subtract the triggerTimelapse  
							currentTriggersScheduleDTO.setStopTime(deckTime - (triggerTimeLapse * 1000000) -(stationDetails.getSystemPostfaultTime()*1000));
							// last triggered time is the decktime - the trigger lapse time
							lastTriggeredStopTime = deckTime - (triggerTimeLapse * 1000000);
							logger.debug("lastTriggeredStopTime calculated after cross record "+lastTriggeredStopTime);
							//					executor.execute(new M9kFaultDataMergeTask(currentTriggersScheduleDTO));
							spawnNewThreadToCrossRecordAndMerge(currentTriggersScheduleDTO);
							waitStopTimeCounter++;
							logger.debug("Invoked data merge task with "+currentTriggersScheduleDTO);
							currentTriggersScheduleDTO = null;
							//					lstTriggersScheduleDTO.clear();
	
	//						// Start time is last request stop time + postfault time added by dfrs
	//						currentStartTime = lastTriggeredStopTime +(stationDetails.getSystemPostfaultTime()*1000);
							//					currentStartTime = deckTime;
							//					logger.debug("Sample Rate  "+stationDetails.getSystemSampleRate());
							logger.debug("Old requested current start Time "+currentStartTime);
							currentStartTime = (long)((lastTriggeredStopTime /1000000.0 +(1/stationDetails.getSystemSampleRate()))*1000000)+(stationDetails.getSystemPrefaultTime()*1000);
							logger.debug("New current start Time "+currentStartTime);
							currentStopTime = 0; 
							logger.debug("\t\t\t IRIG after waiting for stop and hitting the deck "+irigTime);
							// START: 13-May-2024 - Decktime and TriggerLimit time are two different options and trigger limit time are read from config
//							deckTime = irigTime + (triggerLimit*1000000);
							deckTime = irigTime + (deckTimeLimit*1000000);
							// END: 13-May-2024
							
							logger.debug("\t\t\t DECK time after waiting for stop and hitting the deck "+deckTime);
						}
						else
						{
							logger.warn("System is waiting for a trigger stop time which has not arrived. Quitting after hitting trigger limit of "+triggerLimit+" seconds");
							waitStopTimeCounter = 0;
							currentTriggersScheduleDTO = null;
							// START: 30-Jan-2013 - Reset both start and stop time. Typo resets stop time twice
//							currentStopTime = 0;
							currentStartTime = 0; 
							// END: 30-Jan-2013 - Done
							currentStopTime = 0;
							deckTime = 0;
							trackStartStopStatus = 0;
							triggerLimit = 0;

						}
					}
				}
			}
			catch (Exception e) {
				logger.error("Exception occured during the fault data processing ", e);
			}
			finally
			{
				logger.debug("Quitting the processing thread. ");
			}
		}
		
	}
	
	
	@SuppressWarnings({ "unused" })
	private long getIRIG()
	{
		long irig = 0L;
		logger.debug("About to get Get IRIG");
		
		String result;
		Set<String> dfrs = iniConf.getSections();
		String ipAddress = null;
		int port = 0;
		for (String dfr : dfrs) {
			if (!dfr.toUpperCase().startsWith("DFR"))
			{
				continue;
			}
//			System.out.println("["+dfr+"]");
			ipAddress = iniConf.getString(dfr+".ip-address");
			port = iniConf.getInt(dfr+".port");
//			System.out.println("Id "+iniConf.getString(dfr+".id"));
//			System.out.println("Ip address "+ipAddress);
//			System.out.println("Port "+port);

//			requestHealthStatus(ipAddress, port);
			logger.debug("About to invoke M9kStationCommandClient ");
			commandClient = new M9kStationCommandClient(ipAddress, port, "irig,user=machine");
			logger.debug("After invoking M9kStationCommandClient ");
			try {
				logger.debug("About to invoke sendAndReceive() ");
				result = commandClient.sendAndReceive();
				logger.debug("After invoking sendAndReceive() "+result);
				irig = Long.parseLong(result);
				logger.debug("REceived IRIG from "+ipAddress+" IRIG value: "+irig);
				break;
			}
			catch (M9000Exception e) {
				logger.error("Cannot get IRIG from "+dfr+" - "+ipAddress,e);
				irig = 0L;
			}
			catch (Exception e) {
				logger.error("Cannot get IRIG from "+dfr+" - "+ipAddress,e);
				irig = 0L;
			}
		}

		return irig;
	}
	
    private List<TriggersScheduleDTO> getTriggersSchedule()
    {
		PreparedStatement ps = null;
		ResultSet rs= null;
		TriggersScheduleDTO triggersScheduleDTO;
		List<TriggersScheduleDTO> lstProcessTriggersScheduleDTO = new ArrayList<TriggersScheduleDTO>();
		strIdTriggersToRemove = new StringBuffer();
		long start = 0;
		long stop = 0;
		int dfrId;
		String keyDfrName;
		Integer mapValue;
		long dfrTimeWithtimelapse;
		try {
			dfrTimeWithtimelapse = M9kStationUtil.getDfrTime() - (triggerTimeLapse * 1000000);
//			logger.debug("DB-POOL New connection from getTriggersSchedule ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
//			ps = mysqlConn.prepareStatement("select distinct a.idtriggers, a.dfrId,a.start,a.stop, a.updateTime from triggers_updates a " +
//					"inner join triggers_updates b  on (a.start <= (b.start+b.stop) and a.stop <= (b.start+b.stop)) " +
//					"where (a.start + a.stop) < ? ");
			ps = mysqlConn.prepareStatement("select * from triggers_updates where (start + stop) < ? order by (start + stop)");

//			logger.debug("Time lapse calculated and used in the query "+dfrTimeWithtimelapse+" from the property file "+triggerTimeLapse);
			ps.setLong(1, dfrTimeWithtimelapse);
			rs = ps.executeQuery();
			while (rs.next())
			{
//				try
//				{
//					if(isWatchDogEnabled())
//					{
//						M9kStationUtil.petWatchDog();
//					}
//				}
//				catch(RuntimeException re)
//				{
//					logger.error("Error in petting watch dog ",re);
//				}
//				catch(Exception e)
//				{
//					logger.error("Error in petting watch dog ",e);
//				}


				stop = rs.getLong("stop");
				start = rs.getLong("start");
				dfrId = rs.getInt("dfrId");
				keyDfrName = "DFR"+dfrId;
				if (strIdTriggersToRemove.length() == 0)
				{
					strIdTriggersToRemove.append(rs.getInt("idtriggers"));
				}
				else
				{
					strIdTriggersToRemove.append(", "+rs.getInt("idtriggers"));
				}
				if (start == 0 && stop == 0)
				{
					logger.debug("ZERO:  zero trigger Dfr start stops "+mapDfrStartStopStatus);
					mapValue = mapDfrStartStopStatus.get(keyDfrName);
					if (mapValue != null && mapValue > 0)
					{
						trackStartStopStatus--;
						mapDfrStartStopStatus.put(keyDfrName, --mapValue);
						if (trackStartStopStatus == 0)
						{
							trackStartStopStatus = 0;
							currentStartTime = 0;
							currentStopTime = 0;
							deckTime = 0;
							triggerLimit = 0;
						}
					}
					continue;
//					if (trackStartStopStatus == 0)
//					{
//					logger.debug("One of the DFR has restarted. Hence resetting all start and stop times.");
//						trackStartStopStatus = 0;
//						currentStartTime = 0;
//						currentStopTime = 0;
//						deckTime = 0;
//						continue;
//					}
//					else 
//					{
//						trackStartStopStatus = 1;
//						stop = M9kStationUtil.getIRIG();
//					} 
				}
//				// TODO: it has to start wobbling before requesting cross record
//				M9kLED.setTriggerLedWithRelay(LedState.WARBLE);
				logger.debug("\n\n\n\n\n\tTEST-CYCLE:Triggers table data read \n\t\t\t"+" idtriggers "+rs.getInt("idtriggers")+
						"\n\t\t\t dfrId "+rs.getInt("dfrId")+"\n\t\t\t start "+rs.getLong("start")+"\n\t\t\t stop "+rs.getLong("stop")+"\n\n");
				logger.debug("\n\t\t\t\t\t\tTEST-CYCLEStart from db "+start+" stop "+stop+" currentStartTime "+currentStartTime+" current stop time "+currentStopTime+"\n"+" trackStartStopStatus "+trackStartStopStatus+"\n\n");
//				logger.debug("TEST-CYCLE lstProcessTriggersScheduleDTO isempty? "+lstProcessTriggersScheduleDTO.isEmpty()+" value "+lstProcessTriggersScheduleDTO);
//				logger.debug("TEST-CYCLE stationDetails.getSystemPrefaultTime() "+stationDetails.getSystemPrefaultTime()+" post fault time "+stationDetails.getSystemPostfaultTime());
//				logger.debug("TEST-CYCLE test condition (start-((stationDetails.getSystemPrefaultTime()+stationDetails.getSystemPostfaultTime())*1000)) "+(start-((stationDetails.getSystemPrefaultTime()+stationDetails.getSystemPostfaultTime())*1000)));
//				logger.debug("TEST-CYCLE If((start-((stationDetails.getSystemPrefaultTime()+stationDetails.getSystemPostfaultTime())*1000)) > currentStopTime) "+((start-((stationDetails.getSystemPrefaultTime()+stationDetails.getSystemPostfaultTime())*1000)) > currentStopTime));
				if ((currentStartTime == 0 && start > lastTriggeredStopTime) || (start >0 && start < currentStartTime && start > lastTriggeredStopTime))
				{
					currentStartTime = start;
				}
				else if (start > 0 && currentStopTime > 0 && trackStartStopStatus == 0)
				{
					if ((start-((stationDetails.getSystemPrefaultTime()+stationDetails.getSystemPostfaultTime())*1000)) > currentStopTime)
					{
						if (lstProcessTriggersScheduleDTO.isEmpty())
						{
							TriggersScheduleDTO readyTriggersScheduleDTO = new TriggersScheduleDTO();
							readyTriggersScheduleDTO.setStartTime(currentStartTime);
							readyTriggersScheduleDTO.setStopTime(currentStopTime);
							logger.debug("\t\t\t\t New trigger. Invoke cross trigger ");
							spawnNewThreadToCrossRecordAndMerge(readyTriggersScheduleDTO);
//							lstProcessTriggersScheduleDTO.add(readyTriggersScheduleDTO);
						}
						else
						{
							triggersScheduleDTO = lstProcessTriggersScheduleDTO.get((lstProcessTriggersScheduleDTO.size()-1));
							triggersScheduleDTO.setStartTime(currentStartTime);
							triggersScheduleDTO.setStopTime(currentStopTime);
							logger.debug("\t\t\t\t Existing trigger. Invoke cross trigger ");
							spawnNewThreadToCrossRecordAndMerge(triggersScheduleDTO);
							lstProcessTriggersScheduleDTO.remove((lstProcessTriggersScheduleDTO.size()-1));
						}
						isWaitOver = false;
						lastTriggeredStopTime = currentStopTime+(stationDetails.getSystemPostfaultTime()*1000);
						currentStartTime = start;
						currentStopTime = 0;
						deckTime = 0;
						triggerLimit = 0;
						triggersScheduleDTO = new TriggersScheduleDTO();
						triggersScheduleDTO.setStartTime(currentStartTime);
						triggersScheduleDTO.setStopTime(currentStopTime);
						lstProcessTriggersScheduleDTO.add(triggersScheduleDTO);	
						logger.debug("\t\t\t\t About to add New one for the new start time "+lstProcessTriggersScheduleDTO);
					}
				}

				if (stop > currentStopTime && stop > lastTriggeredStopTime)
				{
					currentStopTime = stop;
				}
				mapValue = mapDfrStartStopStatus.get(keyDfrName);
				if (start > 0)
				{
					trackStartStopStatus++;
					if (mapValue == null)
					{
						mapDfrStartStopStatus.put(keyDfrName, 1);
					}
					else
					{
						mapDfrStartStopStatus.put(keyDfrName, ++mapValue);
					}
					
				}
				else if (trackStartStopStatus > 0) // TO avoid a condition where we just receive stop without a start at all
				{
					trackStartStopStatus--;
					if (mapValue == null)
					{
						mapDfrStartStopStatus.put(keyDfrName, 0);
					}
					else
					{
						mapDfrStartStopStatus.put(keyDfrName, --mapValue);
					}
					
				}
				else
				{
					logger.error("There was a stop time without a start time"+stop);
					stop = 0;
					currentStopTime = 0;
				}
				logger.debug("ZERO: Dfr start stops "+mapDfrStartStopStatus);
//				logger.debug("\n\t\t\t\t\t\ttrackStartStopStatus "+trackStartStopStatus+" currentStartTime "+currentStartTime+" current stop time "+currentStopTime+"\n");
//				triggersScheduleDTO.setIdTrigger(rs.getInt("idtriggers"));
//				triggersScheduleDTO.setDfrId(rs.getInt("dfrId"));
			}
//			logger.debug("\n\t\t\t\t\t\t After result set loop trackStartStopStatus "+trackStartStopStatus+" currentStartTime "+currentStartTime+" current stop time "+currentStopTime+"\n\n"+" \t\t\t\tstrIdTriggersToRemove "+strIdTriggersToRemove);
//			logger.debug("\t\t\t\t\t lstTriggersScheduleDTO "+lstProcessTriggersScheduleDTO+" size "+lstProcessTriggersScheduleDTO.size()+" is empty? "+lstProcessTriggersScheduleDTO.isEmpty()+" start "+start+" Stop "+stop);
			if (start > 0 || stop > 0)
			{
				if (lstProcessTriggersScheduleDTO.isEmpty())
				{
					triggersScheduleDTO = new TriggersScheduleDTO();
					triggersScheduleDTO.setStartTime(currentStartTime);
					triggersScheduleDTO.setStopTime(currentStopTime);
//					logger.debug("\t\t\t\t About to add to the list "+lstProcessTriggersScheduleDTO);
					lstProcessTriggersScheduleDTO.add(triggersScheduleDTO);	
				}
				else
				{
					triggersScheduleDTO = lstProcessTriggersScheduleDTO.get((lstProcessTriggersScheduleDTO.size()-1));
					triggersScheduleDTO.setStartTime(currentStartTime);
					triggersScheduleDTO.setStopTime(currentStopTime);
				}
			}
			
			rs.close();
			ps.close();
			if (strIdTriggersToRemove.length() > 0)
			{
				removeProcessedDatRecord(lstProcessTriggersScheduleDTO);
			}
		} catch (SQLException e) {
			logger.error("Error Occured in getTriggersSchedule ", e);
		} catch (Exception e) {
			logger.error("Error Occured in getTriggersSchedule ", e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs = null;
				}				
				
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (mysqlConn != null)
				{
//					logger.info("Closing the connection... "+mysqlConn);
//					logger.debug("DB-POOL Closing connection from getTriggersSchedule ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}

    	return lstProcessTriggersScheduleDTO;
    }
    
    private boolean removeProcessedDatRecord(List<TriggersScheduleDTO> lstTriggersScheduleDTO)
    {
    	boolean booConnection = true;
		PreparedStatement ps = null;
		
		try {
//			getLocalConnection();
			logger.debug("About to remove "+strIdTriggersToRemove);
			ps = mysqlConn.prepareStatement("delete from triggers_updates where idtriggers IN("+strIdTriggersToRemove+")");
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.warn("Station Master is not connected to master or Master Station MySql server is down"+e);
			booConnection = false;
		}
		finally
		{
			try {
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (mysqlConn != null)
				{
//					logger.info("Closing the connection... "+mysqlConn);
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}

    	return booConnection;
    }

    private void cleanupTriggersUpdateTable()
    {
		PreparedStatement ps = null;
		
		try {
			logger.debug("DB-POOL New connection from cleanupTriggersUpdateTable ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("About to truncate triggersUpdate table");
			ps = mysqlConn.prepareStatement("truncate triggers_updates");
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("Unable to cler triggers_update table. ",e);
		}
		catch (Exception e) {
			logger.error("Unable to cler triggers_update table. ",e);
		}
		finally
		{
			try {
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (mysqlConn != null)
				{
//					logger.info("Closing the connection... "+mysqlConn);
					logger.debug("DB-POOL Closing connection from cleanupTriggersUpdateTable ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}

    }
//    private StationDTO getLocalStationDetails()
//    {
//		PreparedStatement ps = null;
//		ResultSet rs= null;
//		StationDTO stationDTO = new StationDTO();
//		stationDTO.setPrefaultTime(0);
//		stationDTO.setPostfaultTime(0);
//		stationDTO.setLtrPrefaultTime(20);
//		stationDTO.setLtrPostfaultTime(20);
//		
//		try {
//			mysqlConn = M9kStationDBUtil.getLocalConnection();
//			ps = mysqlConn.prepareStatement("select preFaultTime, postFaultTime, ltrPreFaultTime, ltrPostFaultTime, sampleRate, longTermSampleRate from station_details");
//			rs = ps.executeQuery();
//			while (rs.next())
//			{
//				stationDTO.setPrefaultTime(rs.getLong("preFaultTime"));
//				stationDTO.setPostfaultTime(rs.getLong("postFaultTime"));
//				stationDTO.setLtrPrefaultTime(rs.getInt("ltrPreFaultTime"));
//				stationDTO.setLtrPostfaultTime(rs.getInt("ltrPostFaultTime"));
////				logger.debug("Sample Rate from database "+rs.getDouble("sampleRate"));
//				stationDTO.setSampleRate(rs.getDouble("sampleRate"));
//				stationDTO.setLtrSampleRate(rs.getDouble("longTermSampleRate"));
//			}
//		}catch (SQLException e) {
//			logger.info("Unable to get local station details from database");
//			
//		}
//		finally
//		{
//			try {
//				if (ps != null)
//				{
//					ps.close();
//					ps = null;
//				}
//				if (mysqlConn != null)
//				{
//					logger.info("Closing the connection... "+mysqlConn);
//					mysqlConn.close();
//					mysqlConn = null;
//				}
//			} catch (Exception e) {
//				logger.warn("Error in cleanup", e);
//			}
//		}
//		return stationDTO;
//    }
		
    private void spawnNewThreadToCrossRecordAndMerge(TriggersScheduleDTO triggersScheduleDTO)
    {
    	try {
    		if (!M9kStationUtil.isDiskspaceAvailableToContinueRecording())
			{
    			logger.warn("Recording is stopped as disk space is critical. Fault is not created for startTime "+triggersScheduleDTO.getStartTime()+" Endtime "+triggersScheduleDTO.getStopTime());
    			return;
			}
    		else
    		{
        		logger.debug("Is good for recording..Continuing with faults creation...");
    		}
    		if (triggersScheduleDTO.getStartTime() <= 0 || triggersScheduleDTO.getStopTime() <= 0 || triggersScheduleDTO.getStartTime() >= triggersScheduleDTO.getStopTime() )
    		{
    			logger.warn("Timelines are messed up. Stopped the invocation. "+triggersScheduleDTO);
//    			return;
    		}
    		else
    		{
	    		logger.debug("ES: Before total active threads "+((ThreadPoolExecutor)executor).getActiveCount());
	    		logger.debug("ES: Before Completed task count "+((ThreadPoolExecutor)executor).getCompletedTaskCount());
	    		logger.debug("ES: Before total task count "+((ThreadPoolExecutor)executor).getTaskCount());
	    		logger.debug("ES: Before Queue size "+((ThreadPoolExecutor)executor).getQueue().size());
	    		logger.debug("ES: Before Queue remaining capacity "+((ThreadPoolExecutor)executor).getQueue().remainingCapacity());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.warn("Thread sleep failed",e);
				}
				M9kFaultDataMergeTask m9kFaultDataMergeTask = new M9kFaultDataMergeTask(triggersScheduleDTO);
//				if ((triggersScheduleDTO.getStopTime() - triggersScheduleDTO.getStartTime()) == 1000000)
				if (M9kLED.isTestTrigger())
				{
					M9kLED.setDisableAlarm(true);
					m9kFaultDataMergeTask.setUserComments("This is a Test Trigger");
				}
//				else
//				{
//					M9kLED.disableAlarm = false;
//				}
//				M9kLED.setTriggerLedWithRelay(LedState.WARBLE);
//	    		scheduledCrossRecoredTask = executor.submit(new M9kFaultDataMergeTask(triggersScheduleDTO));
				scheduledCrossRecoredTask = executor.submit(m9kFaultDataMergeTask);
	    		threadHandlerExecutor.execute(new M9kThreadExceptionHandler(scheduledCrossRecoredTask));
	    		logger.debug("After  M9kFaultDataMergeTask thread invoked successfully.."+triggersScheduleDTO);
	    		logger.debug("ES: After total active threads "+((ThreadPoolExecutor)executor).getActiveCount());
	    		logger.debug("ES: After Completed task count "+((ThreadPoolExecutor)executor).getCompletedTaskCount());
	    		logger.debug("ES: After total task count "+((ThreadPoolExecutor)executor).getTaskCount());
	    		logger.debug("ES: After Queue size "+((ThreadPoolExecutor)executor).getQueue().size());
	    		logger.debug("ES: After Queue remaining capacity "+((ThreadPoolExecutor)executor).getQueue().remainingCapacity());
	    		if (ltrStartTime == 0)
	    		{
					irigTime = M9kStationUtil.getIRIG();
	
	    			logger.debug("LTR prefault "+stationDetails.getSystemLtrPrefaultTime()+" post fault "+stationDetails.getSystemLtrPostfaultTime());
	    			ltrStartTime = triggersScheduleDTO.getStartTime()-(stationDetails.getSystemLtrPrefaultTime()*1000000);
	    			if (lastLtrStopTime > ltrStartTime)
	    			{
	    				ltrStartTime = (long)(lastLtrStopTime +((1/stationDetails.getSystemLongTermSampleRate())*1000000));
	    			}
	    			ltrStopTime = triggersScheduleDTO.getStopTime()+(stationDetails.getSystemLtrPostfaultTime()*1000000);
	    			// START: 18-Jun-2024 - Rename ltrTriggerLimit to ddrDeckLimit and read it from GUI
	    			logger.debug("LTR start time calculated "+ltrStartTime+ " stop time "+ltrStopTime+" irig time"+irigTime+" ddrDeckTimeLimit "+M9kStationXMLUtil.getDdrDeckTimeLimit());
//	    			ltrDeckTime = ltrStartTime + (ltrTriggerLimit*1000000);
	    			ltrDeckTime = ltrStartTime + (M9kStationXMLUtil.getDdrDeckTimeLimit()*1000000);
	    			// END: 18-Jun-2024
	    			logger.debug("LTR Deck time calculated "+ltrDeckTime);
	    		}
	    		else 
	    		{
	    			logger.debug("LTR-DEBUG: New Fault record occured. calculate new stop time. Current Ltr deck time "+ltrDeckTime+" Current irig time "+irigTime);
	    			if (triggersScheduleDTO.getStartTime() > 0 && triggersScheduleDTO.getStopTime() > 0 && triggersScheduleDTO.getStartTime() < triggersScheduleDTO.getStopTime() )
	    			{
	    				ltrStopTime = triggersScheduleDTO.getStopTime()+(stationDetails.getSystemLtrPostfaultTime()*1000000);
	    				logger.debug("LTR-DEBUG: New stop calculated "+ltrStopTime);
	    			}
	    		}
    		}
    		
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error in spawning a new thread to merge fault records ",e);
		}
    }

	private void processSleep(long milliSeconds)
	{
		try {
//			logger.debug("Delay the spawning to avoid rush.");
			Thread.sleep(milliSeconds); // Wait for the latency time before reading again
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
    
	public int getRunFrequency() {
		return runFrequency;
	}

	public void setRunFrequency(int runFrequency) {
		this.runFrequency = runFrequency;
	}

	public int getDeckTimeLimit() {
		return deckTimeLimit;
	}

	public void setDeckTimeLimit(int deckTimeLimit) {
		this.deckTimeLimit = deckTimeLimit;
	}

}

