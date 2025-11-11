package com.usi.m9000.Master;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.struts2.interceptor.SessionAware;

import com.usi.m9000.Master.threads.M9kHeartBeatThreads;
import com.usi.m9000.common.email.SendMailUSI;
import com.usi.m9000.dao.MySqlAlarmLogsDAO;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.AlarmLogsDTO;
import com.usi.m9000.dto.EmailReportsSettingsDTO;
import com.usi.m9000.dto.EmailSettingsDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.station.util.LedName;
import com.usi.m9000.station.util.LedState;
import com.usi.m9000.station.util.M9kKeyValuePair;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kMessagesUtil;
import com.usi.m9000.util.M9kReportUtil;
import com.usi.m9000.util.M9kUtils;

/**
 * 10-May-2021 - Email/report settings moved to database from m9k-master.properties file
 * @author sramasamy
 *
 */
public class M9kHeartBeatListener implements SessionAware, ServletContextListener{
	/**
	 * 
	 */
	Map<String, Object> session;
	String stationHealthStatus;
//	XmlOptions xmlOptions;
//	com.usi.health.SubStationDocument.SubStation substationHlth;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> scheduledTask = null;
	private int runFrequency = 30; // in seconds
	M9kDAOFactory m9kDAOFactory;
	StationDAO stationDao;
	List<StationDTO> lstManagedStations;
	static StringBuffer stationsBackUp = new StringBuffer();
	private static List<String> lstDownStaionsList = null;
	PropertiesConfiguration config = null;
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kHeartBeatListener.class);
	static boolean isAlertSent = false;
	protected Runnable taskPerformer = null;
	private MySqlAlarmLogsDAO mySqlAlarmLogsDAO;
	private EmailSettingsDTO emailSettingsDTO;
	private EmailReportsSettingsDTO emailReportsSettingsDTO;

	/**
	 * 
	 */
	public M9kHeartBeatListener()
	{
		m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		stationDao = m9kDAOFactory.getStationDAO();
		mySqlAlarmLogsDAO = new MySqlAlarmLogsDAO();
		try {
			config = new PropertiesConfiguration("/m9k-master.properties");
		} catch (ConfigurationException e) {
			logger.error("Error in reading m9k-master.properties files. Enabling health poll by default and runs at frequency of 60 seconds",e);
			config=null;
			runFrequency=60;
		}

	}

	public void getHealthStatus()
	  {
		emailSettingsDTO = M9kReportUtil.getEmailSettingsDTO();
		emailReportsSettingsDTO = M9kReportUtil.getEmailReportsSettingsDTO();
//		if (!getMasterHealthPoll().equalsIgnoreCase(M9kConstants.ENABLE))
		if (!emailReportsSettingsDTO.isMasterHealthStatusPoll())
		{
			return;
		}

		if (lstDownStaionsList == null)
		{
			try
			{
				initialize();
			}
			catch(Exception e)
			{
				logger.error("Error in initializing with the station access status",e);
				lstDownStaionsList = new ArrayList<String>();
			}
		}

		ExecutorService executor = null;
		List<Future<M9kKeyValuePair>> lstFutureResults = null;
		boolean booComplete = true;
//		String substationHlth = "";
	  try {
				lstManagedStations = stationDao.getManagedStations();
				executor = Executors.newFixedThreadPool(lstManagedStations.size());
				lstFutureResults = new ArrayList<Future<M9kKeyValuePair>>(lstManagedStations.size());
//				  stationHealthStatus = M9kMessagesUtil.sendSynchMessage(""+stationDetails.getSystemStationId(), "MONITOR", "MONITOR");
				StationDTO stationDto;
				for (Iterator<StationDTO> iterator = lstManagedStations.iterator(); iterator
						.hasNext();) {
					stationDto =  iterator.next();
					lstFutureResults.add(executor.submit(new M9kHeartBeatThreads(stationDto.getSystemStationId(), stationDto.getSystemStationName())));
				}
				
			
				List<M9kKeyValuePair> lstHealthStatus= new ArrayList<M9kKeyValuePair>(lstFutureResults.size());
				for (Iterator<Future<M9kKeyValuePair>> iterator = lstFutureResults.iterator(); iterator
						.hasNext();) {
					Future<M9kKeyValuePair> future = iterator.next();
					try {
						logger.debug("HEARTBEAT: About to get from the future waiting for "+getRunFrequency()+" seconds.");
						lstHealthStatus.add(future.get(M9kMessagesUtil.getMqResponseWaitTime()+10, TimeUnit.SECONDS));
						logger.debug("HEARTBEAT: Result from Station added "+lstHealthStatus);
					} catch (InterruptedException e) {
						logger.error("Error waiting for the HEARTBEAT "+future,e);
					} catch (ExecutionException e) {
						logger.error("Error waiting for the HEARTBEAT "+future,e);
					} catch (TimeoutException e) {
						logger.error("Waited too long for the HEARTBEAT "+future,e);
					} catch (Exception e) {
							logger.error("Error in notification queue. Probably local JMS server is down.");
					}
					
					
				}
				executor.shutdown();

				try {
					booComplete = executor.awaitTermination(5, TimeUnit.SECONDS);
					if (!booComplete)
					{
						logger.warn("Station may be down as it takes too long to respond for the health request.");
						List<Runnable> listUnfinishedProcess = executor.shutdownNow();
						for (Iterator<Runnable> iterator = listUnfinishedProcess.iterator(); iterator
								.hasNext();) {
							M9kHeartBeatThreads runnable = (M9kHeartBeatThreads) iterator.next();
							logger.error("Station "+runnable.getStationId()+" - "+runnable.getStationName()+" timed out for health request");
							
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error("Error in calibration ",e);
				}
				
			processHeartBeat(lstHealthStatus);
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

	private void processHeartBeat(List<M9kKeyValuePair> lstHealthStatus) {
		
		StringBuffer emailText = new StringBuffer();
		M9kKeyValuePair m9kKeyValuePair;
		String stationHealthStatus;
		Collections.sort(lstHealthStatus, new Comparator<M9kKeyValuePair>() {
			@Override
			public int compare(M9kKeyValuePair arg0, M9kKeyValuePair arg1) {
				int lhsKey = Integer.parseInt(arg0.getKey().substring(0, arg0.getKey().indexOf("-")).trim());
				int rhsKey = Integer.parseInt(arg1.getKey().substring(0, arg1.getKey().indexOf("-")).trim());;
				return ((lhsKey < rhsKey)? -1 : (lhsKey == rhsKey)?0:1);
			}
		});
		List<AlarmLogsDTO> lstAlarmsLogDto = new ArrayList<AlarmLogsDTO>(lstHealthStatus.size());
		for (Iterator<M9kKeyValuePair> iterator = lstHealthStatus.iterator(); iterator.hasNext();) {
			m9kKeyValuePair = (M9kKeyValuePair) iterator.next();
			stationHealthStatus = m9kKeyValuePair.getValue();
			AlarmLogsDTO alarmLogsDTO = null;
			logger.debug("Station "+m9kKeyValuePair.getKey()+" Message "+stationHealthStatus);
			if (stationHealthStatus.toUpperCase().startsWith("ERROR"))
			  {
				logger.debug("lst of stations down "+lstDownStaionsList);
				if (!lstDownStaionsList.contains(m9kKeyValuePair.getKey()))
				{
					lstDownStaionsList.add(m9kKeyValuePair.getKey());
					isAlertSent = false;
				}
				emailText.append(m9kKeyValuePair.getKey()+ " is unreachable from remote master. ");
				emailText.append(M9kConstants.NEWLINE);
//				emailText.append(stationHealthStatus);
//				emailText.append(M9kConstants.NEWLINE);
//				emailText.append(M9kConstants.NEWLINE);
				emailText.append(M9kConstants.NEWLINE);
//				SubStationDocument subStationDoc = SubStationDocument.Factory.parse(stationHealthStatus, xmlOptions);
//				substationHlth = subStationDoc.getSubStation();
				alarmLogsDTO = new AlarmLogsDTO();
				alarmLogsDTO.setStationId(Integer.parseInt(m9kKeyValuePair.getKey().substring(0, m9kKeyValuePair.getKey().indexOf("-")).trim()));
				alarmLogsDTO.setAlarmDescription(new StringBuffer("DME unreachable from remote master. "));
				alarmLogsDTO.setLedName(LedName.STATION_HEALTH.name());
				alarmLogsDTO.setLedStatus(LedState.RED.name());
				alarmLogsDTO.setRelays("");
				alarmLogsDTO.setAlarmTime(new java.text.SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").format(new Date()));

			  }
			  else
			  {
//				  substationHlth = null;
				  logger.debug("ELSE: Lst of down stations "+lstDownStaionsList);
				  if (lstDownStaionsList.contains(m9kKeyValuePair.getKey()))
					{
					  logger.debug("About to remove station as it is back up"+m9kKeyValuePair.getKey());
					  lstDownStaionsList.remove(m9kKeyValuePair.getKey());
						stationsBackUp.append(m9kKeyValuePair.getKey()+" is now reachable");
						stationsBackUp.append(M9kConstants.NEWLINE);
						stationsBackUp.append(M9kConstants.NEWLINE);
						alarmLogsDTO = new AlarmLogsDTO();
						alarmLogsDTO.setStationId(Integer.parseInt(m9kKeyValuePair.getKey().substring(0, m9kKeyValuePair.getKey().indexOf("-")).trim()));
						alarmLogsDTO.setAlarmDescription(new StringBuffer("DME is now reachable from remote master. "));
						alarmLogsDTO.setLedName(LedName.STATION_HEALTH.name());
						alarmLogsDTO.setLedStatus(LedState.GREEN.name());
						alarmLogsDTO.setRelays("");
						alarmLogsDTO.setAlarmTime(new java.text.SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").format(new Date()));
					}
				  logger.debug("Station Id "+m9kKeyValuePair.getKey()+ " is healthy. "+stationHealthStatus);
			  }
			if (alarmLogsDTO != null)
			{
				lstAlarmsLogDto.add(alarmLogsDTO);
			}
			
		}
		logger.debug("HEARTBEAT: result if any error? "+emailText.toString()+" isAlertSent "+isAlertSent);
		if (!isAlertSent && emailText.length() > 0)
		{
			try
			{
//				if (M9kUtils.isEmailNotificationEnabled())
				if (emailSettingsDTO.isEnableEmail())
				{
					SendMailUSI.sendEmail("IMPORTANT: One or more DMEs unreachable ", emailText.toString());
				}
				else
				{
					logger.info("IMPORTANT: One or more DMEs unreachable "+emailText.toString());
				}
				if (stationsBackUp.length() > 0)
				{
//					if (M9kUtils.isEmailNotificationEnabled())
					if (emailSettingsDTO.isEnableEmail())
					{
						SendMailUSI.sendEmail("INFO: One or more DMEs are now reachable", stationsBackUp.toString());
					}
					else
					{
						logger.info("INFO: One or more DMEs are now reachable" + stationsBackUp.toString());
					}
					stationsBackUp.setLength(0);
				}
				if (lstAlarmsLogDto.size() > 0)
				{
					for (Iterator<AlarmLogsDTO> iterator = lstAlarmsLogDto.iterator(); iterator
							.hasNext();) {
						mySqlAlarmLogsDAO.insertIntoMastersAlarmDetails(iterator.next());
						
					}
				}
				isAlertSent = true;
			}
			catch (Exception e) {
				logger.error("Unable to email",e);
				isAlertSent = false;
			}
		}
		else //if (isAlertSent && emailText.length() == 0)
		{
			try
			{
				if (stationsBackUp.length() > 0)
				{
					if (M9kUtils.isEmailNotificationEnabled())
					{
						SendMailUSI.sendEmail("INFO: One or more DMEs are now reachable", stationsBackUp.toString());
					}
					else
					{
						logger.info("INFO: One or more DMEs are now reachable" + stationsBackUp.toString());
					}
					stationsBackUp.setLength(0);
					for (Iterator<AlarmLogsDTO> iterator = lstAlarmsLogDto.iterator(); iterator
							.hasNext();) {
						mySqlAlarmLogsDAO.insertIntoMastersAlarmDetails(iterator.next());
						
					}
				}
			}
			catch (Exception e) {
				logger.error("Unable to email about the stations that are back up. "+stationsBackUp.toString(),e);
//				isAlertSent = false;
			}
			
		}
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
//		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//	    LogFactory.release(contextClassLoader);
		try
		{
			if (scheduledTask != null)
			{
				scheduledTask.cancel(true);
			}
			if (scheduler != null)
			{
				scheduler.shutdownNow();
			}
		} catch (Exception e) {
			logger.warn("Error occured in ContextDestroyed method in M9kHeartBeatListener ",e);
		}		
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
//		 System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		  taskPerformer = new Runnable() {
              public void run() {
            	  getHealthStatus();
              }
          };
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, getRunFrequency(), TimeUnit.SECONDS);
	}

	public void reScheduleRunFrequency(int runFrequencyInSeconds)
	{
		scheduledTask.cancel(true);
		config.setProperty("master-health-status-polling-frequency", runFrequencyInSeconds);
		scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, runFrequencyInSeconds, TimeUnit.SECONDS);
	}
	public String getStationHealthStatus() {
		return stationHealthStatus;
	}

	public void setStationHealthStatus(String stationHealthStatus) {
		this.stationHealthStatus = stationHealthStatus;
	}


	public int getRunFrequency() {
		if (config != null)
		{
			runFrequency=config.getInt("master-health-status-polling-frequency", 300);
			M9kMessagesUtil.setHeartBeatCheckFrequency(runFrequency);
		}
//		runFrequency = M9kReportUtil.getEmailReportsSettingsDTO().getMasterHealthStatusPollingFrequency();
		return runFrequency;
	}

	public void setRunFrequency(int runFrequency) {
		this.runFrequency = runFrequency;
	}

//	public String getMasterHealthPoll() {
//		if (config != null)
//		{
//			masterHealthPoll=config.getString("master-health-status-poll", M9kConstants.ENABLE);
//		}
//
//		return masterHealthPoll;
//	}

//	public void setMasterHealthPoll(String masterHealthPoll) {
//		this.masterHealthPoll = masterHealthPoll;
//	}


	private void initialize()
	{
		Map<String, List<AlarmLogsDTO>> mapLstOfAbnormalAlarms = mySqlAlarmLogsDAO.getAbnormalStationAlarmsDetails();
		AlarmLogsDTO alarmLogsDTO;
		lstDownStaionsList = new ArrayList<String>();
		for (Iterator<String> iterator = mapLstOfAbnormalAlarms.keySet().iterator(); iterator
				.hasNext();) {
			String key = iterator.next();
			logger.debug("Station to search alarms for "+key);
			for (Iterator<AlarmLogsDTO> iterator2 = mapLstOfAbnormalAlarms.get(key).iterator(); iterator2
					.hasNext();) {
				alarmLogsDTO = iterator2.next();
				if (alarmLogsDTO.getLedName().equalsIgnoreCase(LedName.STATION_HEALTH.name()) && alarmLogsDTO.getLedStatus().equalsIgnoreCase(LedState.RED.name()))
				{
					logger.debug("Station added to down list "+key);
					lstDownStaionsList.add(key);
					break;
				}
				
			}
			
		}
	}
}
