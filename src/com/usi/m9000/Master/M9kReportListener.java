package com.usi.m9000.Master;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.struts2.interceptor.SessionAware;

import com.usi.m9000.dao.MySqlAlarmLogsDAO;
import com.usi.m9000.dao.MySqlReportDAO;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.AlarmLogsDTO;
import com.usi.m9000.dto.EmailReportsSettingsDTO;
import com.usi.m9000.dto.EmailSettingsDTO;
import com.usi.m9000.dto.M9kStatusReportDTO;
import com.usi.m9000.dto.ReportsDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.StationReportDTO;
import com.usi.m9000.reports.M9kDailyStatusReportPDF;
import com.usi.m9000.reports.M9kStationSpecificDailyStatusReportPDF;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kReportUtil;
import com.usi.m9000.util.M9kUtils;

public class M9kReportListener implements SessionAware, ServletContextListener{
	/**
	 * 
	 */
	Map<String, Object> session;
//	XmlOptions xmlOptions;
//	com.usi.health.SubStationDocument.SubStation substationHlth;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	// START: 22-July-2024 - made it static to reschedule daily status report
	private static ScheduledFuture<?> scheduledTask = null;
	private int runAtHour = 0; // Integer value representing hours of the day 0 - 23 hrs
	private int runAtMin = 0; // Integer value representing hours of the day 0 - 59 hrs
	private int runAtInterval = (24*60); // Integer value representing hours of the day 0 - 59 hrs
	M9kDAOFactory m9kDAOFactory;
	StationDAO stationDao;
	List<StationDTO> lstManagedStations;
	PropertiesConfiguration m9k_master_config = null;
	
	// START: 10-May-2021 - Email reporting Properties moved to database from m9k-master property file
//	private String dailyStatusReportGeneration=M9kConstants.ENABLE;
	private boolean dailyStatusReportGeneration=false;
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kReportListener.class);
	static boolean isAlertSent = false;
	protected Runnable taskPerformer = null;
	private MySqlReportDAO mySqlReportDAO;
	private MySqlAlarmLogsDAO mySqlAlarmLogsDAO;
	private String dailyStatusReportTitle="DME Daily Health Report";
	private String stationSpecificReportTitle="Station Specific DME Health Report";
	private String reportsDir = "/data/m9k/status-reports/";
	private int noOfStationsFailed=0;
	private List<StationReportDTO> lstOfFailedStations = null;
	M9kHeartBeatListener heartBeatListener;
	private EmailSettingsDTO emailSettingsDTO;
	private EmailReportsSettingsDTO emailReportsSettingsDTO;
	/**
	 * 
	 */
	public M9kReportListener()
	{
		m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		stationDao = m9kDAOFactory.getStationDAO();
		mySqlReportDAO = new MySqlReportDAO();
		mySqlAlarmLogsDAO = new MySqlAlarmLogsDAO();
		lstOfFailedStations = new ArrayList<StationReportDTO>();
		emailSettingsDTO = M9kReportUtil.getEmailSettingsDTO();
		emailReportsSettingsDTO = M9kReportUtil.getEmailReportsSettingsDTO();
		heartBeatListener = new M9kHeartBeatListener();
		try {
			m9k_master_config = new PropertiesConfiguration("/m9k-master.properties");
			if (getRunAtInterval() > 60) // Check whether the interval is at least 1 hour 
			{
				mySqlAlarmLogsDAO.setReportFreq(getRunAtInterval()/60); // convert to hours
			}
			else
			{
				mySqlAlarmLogsDAO.setReportFreq(1); // convert to hours
			}
		} catch (ConfigurationException e) {
			logger.error("Error in reading m9k-master.properties files. Enabling health poll by default and runs at frequency of 60 seconds",e);
			m9k_master_config=null;
//			dailyStatusReportGeneration=false;
			runAtHour=1;
		}
		catch (Exception e) {
			logger.error("Error in reading m9k-master.properties files. Enabling health poll by default and runs at frequency of 60 seconds",e);
			m9k_master_config=null;
			dailyStatusReportGeneration=false;
			runAtHour=1;
		}

	}

	private M9kStatusReportDTO populateDailyStatusReport()
	{
		M9kStatusReportDTO m9kStatusReportDTO = null;
		try {
			m9kStatusReportDTO = new M9kStatusReportDTO();
			m9kStatusReportDTO.setReportId(M9kUtils.getNextReportId());
			m9kStatusReportDTO.setReportTitle(getDailyStatusReportTitle());
			m9kStatusReportDTO.setReportsDir(getReportsDir());
			Date currDate = new Date();
			m9kStatusReportDTO.setReportDate( new java.text.SimpleDateFormat("yyyy-MM-dd").format(currDate));
			m9kStatusReportDTO.setReportTime( new java.text.SimpleDateFormat("HH:mm:ss").format(currDate));
			try
			{
				m9kStatusReportDTO.setReportHeader(M9kUtils.getStringFromFile("daily-status-report-header.txt"));
			}
			catch (Exception e) {
				logger.error("Unable to read 'daily-status-report-header.txt' file for header info. Using default.",e);
				m9kStatusReportDTO.setReportHeader(M9kUtils.getDefaultDailyReportHeader());
			}
			m9kStatusReportDTO.getReportBody().append(String.format("%1$-30s %2$-30s%n", "Substation", "Health Check Result"));
			ReportsDTO analogsSelfTestReportsDto = null;
			ReportsDTO eventsSelfTestReportsDto = null;

			Map<String, StationReportDTO> mapStationAlarmsDetails = mySqlAlarmLogsDAO.getStationAlarmsDetails();
			List<StationReportDTO> lstStationForReports = stationDao.getLstStationForReports();
			
//			Map<String, List<AlarmLogsDTO>> mapAbnormalAlarmsDetails = mySqlAlarmLogsDAO.getAbnormalStationAlarmsDetails();
			String stationKey;
			StationReportDTO stationReportDTO;
//			List<String> lstStationNames = new ArrayList<String>(mapStationAlarmsDetails.keySet());
//			Collections.sort(lstStationNames, new Comparator<String>() {
//
//				@Override
//				public int compare(String o1, String o2) {
//					int lhs = Integer.parseInt(o1.substring(0, o1.indexOf("-")).trim());
//					int rhs = Integer.parseInt(o2.substring(0, o2.indexOf("-")).trim());
////					return ((lhs < rhs)?0:1);
//					return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
//				}
//			});
			boolean booPass = true;
//			ReportsDTO stationReportDetails;
			StringBuffer stationStatusBuffer;
			Map<String, ReportsDTO> mapAnalogSelfTestStatus = mySqlReportDAO.getMostRecentReport(M9kConstants.CALIBRATE_VERIFY);
			Map<String, ReportsDTO> mapEventSelfTestStatus = mySqlReportDAO.getMostRecentReport(M9kConstants.EVENTTEST);
			for (Iterator<StationReportDTO> iterator = lstStationForReports.iterator(); iterator.hasNext();) {
				booPass = true;
				stationReportDTO = iterator.next(); 
				stationKey = stationReportDTO.getStationKey();
				if (mapStationAlarmsDetails.get(stationKey) != null)
				{
					stationReportDTO = mapStationAlarmsDetails.get(stationKey);
				}
				if (stationReportDTO.getAnalogCount() > 0)
				{
					analogsSelfTestReportsDto = mapAnalogSelfTestStatus.get(stationKey);
				}
				if (stationReportDTO.getDigitalCount() > 0 && isEventTestEnabled())
				{
					eventsSelfTestReportsDto = mapEventSelfTestStatus.get(stationKey);
				}

				
//				stationReportDetails = new ReportsDTO();
//				stationReportDetails.setStationNameForReports(stationKey);
				stationStatusBuffer = new StringBuffer();
				AlarmLogsDTO analogAlarmLogsDTO = null;
				AlarmLogsDTO digitalAlarmLogsDTO = null;

//				if (mapAbnormalAlarmsDetails.get(stationKey) != null && mapAbnormalAlarmsDetails.get(stationKey).size() > 0)
//				{
//					booPass=false;
//					if (stationReportDTO.getLstOfStationAlarms() == null)
//					{
//						stationReportDTO.setLstOfStationAlarms(mapAbnormalAlarmsDetails.get(stationKey));
//					}
//					else
//					{
//						stationReportDTO.getLstOfStationAlarms().addAll(mapAbnormalAlarmsDetails.get(stationKey));
//					}
//				}
				if (stationReportDTO.getLstOfStationAlarms() != null ) // Alarms detected in last 24 hours
				{
					booPass=false;
//					stationReportDetails.setStatus(M9kConstants.FAIL);
				}
				if (stationReportDTO.getAnalogCount() > 0)
				{
					if (analogsSelfTestReportsDto== null || analogsSelfTestReportsDto.getStatus()==null) // Has analog self test (verify calibration) ever run?
					{
						analogAlarmLogsDTO = new AlarmLogsDTO();
						booPass=false;
	//					m9kStatusReportDTO.getReportBody().append(String.format("%1$25s%n"," Analog Channel Self Test run not found. "));
						stationReportDTO.getAnalogSelfTestDetails().append(String.format("%1$-25s %2$-25s %3$-25s %4$-25s%n","No Date Info", "Analog Channels Self Test",M9kConstants.FAIL," Calibration/Verification record not found. "));
						analogAlarmLogsDTO.setAlarmTime("No Date Info");
						analogAlarmLogsDTO.setLedName("Analog Channels Self Test");
						analogAlarmLogsDTO.setLedStatus(M9kConstants.FAIL);
						analogAlarmLogsDTO.setAlarmDescription(new StringBuffer("Calibration/Verification information not found."));
						analogAlarmLogsDTO.setCurrentStatus(M9kConstants.UNRESOLVED+". Calibration/Verification need to be performed");
						stationStatusBuffer.append("Analog Channel Self Test run not found.");
						stationStatusBuffer.append(M9kConstants.NEWLINE);
					}
					else if (analogsSelfTestReportsDto.getStatus().equalsIgnoreCase(M9kConstants.FAIL)) // Analog self test failed
					{
						analogAlarmLogsDTO = new AlarmLogsDTO();
						booPass=false;
						stationReportDTO.getAnalogSelfTestDetails().append(String.format("%1$25s %2$-30s %3$-25s %4$-25s%n",analogsSelfTestReportsDto.getReport_time(),"Analog Channels Self Test",M9kConstants.FAIL,analogsSelfTestReportsDto.getStatus_msg()));
						analogAlarmLogsDTO.setAlarmTime(analogsSelfTestReportsDto.getReport_time());
						analogAlarmLogsDTO.setLedName("Analog Channels Self Test");
						analogAlarmLogsDTO.setLedStatus(M9kConstants.FAIL);
						analogAlarmLogsDTO.setAlarmDescription(new StringBuffer(analogsSelfTestReportsDto.getStatus_msg()));
						stationStatusBuffer.append("Calibration/Verification information not found.");
						analogAlarmLogsDTO.setCurrentStatus(M9kConstants.UNRESOLVED);
						stationStatusBuffer.append(" Analog Channel Self Test failed due to "+eventsSelfTestReportsDto.getStatus_msg()+". Last run at "+eventsSelfTestReportsDto.getReport_time());
						stationStatusBuffer.append(M9kConstants.NEWLINE);
					}
				}
				if (stationReportDTO.getDigitalCount() > 0 && isEventTestEnabled())
				{
					if (eventsSelfTestReportsDto == null || eventsSelfTestReportsDto.getStatus()==null) // Has digital self test ever run?
					{
						if (digitalAlarmLogsDTO == null)
						{
							digitalAlarmLogsDTO = new AlarmLogsDTO();
						}
						booPass=false;
						stationReportDTO.getDigitalsSelfTestDetails().append(String.format("%1$-25s %2$-25s %3$-25s %4$-25s%n","No Date Info", "Digital Channels Self Test",M9kConstants.FAIL," Self Test run record not found. "));
						digitalAlarmLogsDTO.setAlarmTime("No Date Info");
						digitalAlarmLogsDTO.setLedName("Digital Channels Self Test");
						digitalAlarmLogsDTO.setLedStatus(M9kConstants.FAIL);
						digitalAlarmLogsDTO.setAlarmDescription(new StringBuffer("Self Test run information not found."));
						digitalAlarmLogsDTO.setCurrentStatus(M9kConstants.UNRESOLVED+". Event test needs to be performed");
						stationStatusBuffer.append("Digital Channel Self Test run not found.");
	
						stationStatusBuffer.append("Digital Channel Self Test run not found.");
						stationStatusBuffer.append(M9kConstants.NEWLINE);
					}
					else  if(eventsSelfTestReportsDto.getStatus().equalsIgnoreCase(M9kConstants.FAIL)) // digital self test ran and failed
					{
						if (digitalAlarmLogsDTO == null)
						{
							digitalAlarmLogsDTO = new AlarmLogsDTO();
						}
						booPass=false;
						stationReportDTO.getDigitalsSelfTestDetails().append(String.format("%1$-25s %2$-30s %3$-25s %4$-25s%n",eventsSelfTestReportsDto.getReport_time(),"Digital Channels Self Test",M9kConstants.FAIL,eventsSelfTestReportsDto.getStatus_msg()));
						digitalAlarmLogsDTO.setAlarmTime(eventsSelfTestReportsDto.getReport_time());
						digitalAlarmLogsDTO.setLedName("Digital Channels Self Test");
						digitalAlarmLogsDTO.setLedStatus(M9kConstants.FAIL);
						digitalAlarmLogsDTO.setAlarmDescription(new StringBuffer(eventsSelfTestReportsDto.getStatus_msg()));
						digitalAlarmLogsDTO.setCurrentStatus(M9kConstants.UNRESOLVED);
						stationStatusBuffer.append(" Digital Channel Self Test failed due to "+eventsSelfTestReportsDto.getStatus_msg()+". Last run at "+eventsSelfTestReportsDto.getReport_time());
//						digitalAlarmLogsDTO.setCurrentStatus("Digital event test needs to be performed");
						stationStatusBuffer.append(M9kConstants.NEWLINE);
					}
				}
				m9kStatusReportDTO.getReportBody().append(M9kConstants.NEWLINE);
//				stationReportDetails.setStatus_msg(stationStatusBuffer.toString());
				if (!booPass)
				{
					noOfStationsFailed++;
//					stationReportDetails.setStatus(M9kConstants.FAIL);
					m9kStatusReportDTO.getReportBody().append(String.format("%1$-30s %2$-30s %3$-30s%n%n",stationKey,"",M9kConstants.FAIL));
					m9kStatusReportDTO.getDailyStatusdataSource().add(stationKey, M9kConstants.FAIL);
					if (analogAlarmLogsDTO != null)
					{
						if (stationReportDTO.getLstOfStationAlarms() == null)
						{
							stationReportDTO.setLstOfStationAlarms(new ArrayList<AlarmLogsDTO>());
						}
						logger.debug("Analog ALarms log to be added for event test and calibration"+analogAlarmLogsDTO);
						stationReportDTO.getLstOfStationAlarms().add(analogAlarmLogsDTO);
					}
					if (digitalAlarmLogsDTO != null)
					{
						if (stationReportDTO.getLstOfStationAlarms() == null)
						{
							stationReportDTO.setLstOfStationAlarms(new ArrayList<AlarmLogsDTO>());
						}
						logger.debug("Events ALarms log to be added for event test and calibration"+digitalAlarmLogsDTO);
						stationReportDTO.getLstOfStationAlarms().add(digitalAlarmLogsDTO);
					}
					getLstOfFailedStations().add(stationReportDTO);
				}
				else
				{
					m9kStatusReportDTO.getReportBody().append(String.format("%1$-30s %2$-30s %3$-30s%n",stationKey,"",M9kConstants.PASS));
					m9kStatusReportDTO.getDailyStatusdataSource().add(stationKey, M9kConstants.PASS);
//					stationReportDetails.setStatus(M9kConstants.PASS);
				}
				
			}
			
			m9kStatusReportDTO.getReportBody().append(String.format("%n%n%1$s","* In the event of a \"Fail\" Health Status, please reference station specific DME Alarms Report"));
			m9kStatusReportDTO.setDailyStatusSummaryText("* In the event of a \"Fail\" Health Status, please reference station specific DME Alarms Report");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in daily status reports generation. ",e);
			m9kStatusReportDTO = null;
		}
		logger.debug("Returning status report dto "+m9kStatusReportDTO);
		return m9kStatusReportDTO;
	}

	public void createDailyStatusReport()
	{
		emailSettingsDTO = M9kReportUtil.getEmailSettingsDTO();
		emailReportsSettingsDTO = M9kReportUtil.getEmailReportsSettingsDTO();

		logger.debug("daily status report enabled? "+emailSettingsDTO.isEnableEmail());
		// START: 22-Jul-2024 - Check for daily status email settings to be enabled
//		if (!emailSettingsDTO.isEnableEmail())
		if (!emailSettingsDTO.isEnableEmail() || !emailReportsSettingsDTO.isEnableDailyStatusEmails())
		// END: 22-Jul-2024 - Check for daily status email settings to be enabled
		{
			return;
		}
	      logger.debug("Master health status polling started and to run at interval of "+getRunAtInterval()+" minutes and runs at "+getRunAtHour()+" Hours and "+getRunAtMin()+" Minutes.");

		StringBuffer m9kReportContent;
		heartBeatListener.getHealthStatus();
		M9kStatusReportDTO m9kStatusReportDTO = populateDailyStatusReport();
		if (m9kStatusReportDTO != null)
		{
			try {
				m9kReportContent = M9kReportUtil.generateDailyStatusReport(m9kStatusReportDTO);
//				
//				logger.debug("Report Generated \n"+m9kReportContent);
//				try
//				{
//				File reportsFile = new File(m9kStatusReportDTO.getReportsDir());
//				boolean booCreated  = reportsFile.setWritable(true);
//				logger.debug("FILE: setting permission "+booCreated);
//				if (!reportsFile.exists())
//				{
//					booCreated = reportsFile.mkdirs();
//					logger.debug("FILE: Create all dirs "+booCreated);
//				}
//				reportsFile = new File(m9kStatusReportDTO.getReportsDir()+m9kStatusReportDTO.createReportFileName("daily-status-report"));
//				bw = new BufferedWriter(new FileWriter(reportsFile));
//				bw.write(m9kReportContent.toString());
//				bw.close();
//				}
//				catch (Exception e) {
//					logger.error("Exception in creating report file ",e);
//				}
//				finally
//				{
//					if (bw!=null)
//					{
//						try {
//							bw.close();
//							bw = null;
//						} catch (IOException e) {
//						}
//					}
//				}
//				
//				SendMailUSI.sendEmail("DME Daily Status Report "+m9kStatusReportDTO.getReportDate()+" "+m9kStatusReportDTO.getReportTime(), m9kReportContent.toString());

				try
				{
					M9kDailyStatusReportPDF m9kDailyStatusReportPDF = new M9kDailyStatusReportPDF(m9kStatusReportDTO);
					m9kDailyStatusReportPDF.buildPDF();
					m9kDailyStatusReportPDF.sendEmail();
				}
				catch(Exception e)
				{
					logger.error("Unable to create PDF report ",e);
				}

				ReportsDTO reportsDTO = new ReportsDTO();
				reportsDTO.setStationId(0);
				reportsDTO.setReportFileName(m9kStatusReportDTO.getPDFReportFileName());
				reportsDTO.setReport_action("DAILY-STATUS");
				reportsDTO.setReport_time(m9kStatusReportDTO.getReportDate()+" "+m9kStatusReportDTO.getReportTime());
				reportsDTO.setReport(m9kReportContent.toString());
				if (noOfStationsFailed == 0)
				{
					reportsDTO.setStatus(M9kConstants.PASS);
				}
				else
				{
					reportsDTO.setStatus(M9kConstants.FAIL);
					reportsDTO.setStatus_msg("At least "+noOfStationsFailed+" of the station(s) have failed.Please look into report for more details.");
				}
				mySqlReportDAO.insertIntoReportTable(reportsDTO);
				StationReportDTO stationReportDTO;
				if (!getLstOfFailedStations().isEmpty())
				{
					for (Iterator<StationReportDTO> iterator = getLstOfFailedStations().iterator(); iterator
							.hasNext();) {
						stationReportDTO = iterator.next();
						createStationSpecificReport(stationReportDTO);
					}
				}
				getLstOfFailedStations().clear();
				noOfStationsFailed = 0;
				
			} catch (Exception e) {
				logger.error("Unable to generate daily status report",e);
				noOfStationsFailed = 0;
			}
			
		}
		
	}

	public void createStationSpecificReport(StationReportDTO stationReportDTO)
	{
		StringBuffer m9kReportContent;
		M9kStatusReportDTO m9kStatusReportDTO = populateStationSpecificStatusReport(stationReportDTO);
		if (m9kStatusReportDTO != null)
		{
			try {
				m9kReportContent = M9kReportUtil.generateStationSpecificReport(m9kStatusReportDTO, stationReportDTO);
//				logger.debug("Report Generated \n"+m9kReportContent);
//				try
//				{
//				File reportsFile = new File(m9kStatusReportDTO.getReportsDir());
//				boolean booCreated  = reportsFile.setWritable(true);
//				logger.debug("FILE: setting permission "+booCreated);
//				if (!reportsFile.exists())
//				{
//					booCreated = reportsFile.mkdirs();
//					logger.debug("FILE: Create all dirs "+booCreated);
//				}
//				reportsFile = new File(m9kStatusReportDTO.getReportsDir()+m9kStatusReportDTO.createReportFileName(stationReportDTO.getStationKey()+"-report"));
//				bw = new BufferedWriter(new FileWriter(reportsFile));
//				bw.write(m9kReportContent.toString());
//				bw.close();
//				}
//				catch (Exception e) {
//					logger.error("Exception in creating report file ",e);
//				}
//				finally
//				{
//					if (bw!=null)
//					{
//						try {
//							bw.close();
//							bw = null;
//						} catch (IOException e) {
//						}
//					}
//				}
//				SendMailUSI.sendEmail("DME "+stationReportDTO.getStationKey()+" Report "+m9kStatusReportDTO.getReportDate()+" "+m9kStatusReportDTO.getReportTime(), m9kReportContent.toString());
	

			try
			{
				M9kStationSpecificDailyStatusReportPDF m9kStationSpecificDailyStatusReportPDF = new M9kStationSpecificDailyStatusReportPDF(m9kStatusReportDTO);
				m9kStationSpecificDailyStatusReportPDF.buildPDF();
				m9kStationSpecificDailyStatusReportPDF.sendEmail();
			}
			catch(Exception e)
			{
				logger.error("Unable to create PDF report ",e);
			}			
				ReportsDTO reportsDTO = new ReportsDTO();
				reportsDTO.setStationId(stationReportDTO.getStationId());
				reportsDTO.setReportFileName(m9kStatusReportDTO.getPDFReportFileName());
				reportsDTO.setReport_action(stationReportDTO.getStationKey()+"-STATUS");
				reportsDTO.setReport_time(m9kStatusReportDTO.getReportDate()+" "+m9kStatusReportDTO.getReportTime());
				reportsDTO.setReport(m9kReportContent.toString());
				reportsDTO.setStatus(M9kConstants.FAIL);
				reportsDTO.setStatus_msg(stationReportDTO.getStationKey()+" has issues. Please look into report for more details.");
				mySqlReportDAO.insertIntoReportTable(reportsDTO);
				
			} catch (Exception e) {
				logger.error("Unable to generate station specific status report for "+stationReportDTO.getStationKey(),e);
				noOfStationsFailed = 0;
			}
		}
		
	}

	private M9kStatusReportDTO populateStationSpecificStatusReport(StationReportDTO stationReportDTO)
	{
		M9kStatusReportDTO m9kStatusReportDTO = null;
		AlarmLogsDTO alarmsLogDto;
		try {
			m9kStatusReportDTO = new M9kStatusReportDTO();
			m9kStatusReportDTO.setReportTitle(getStationSpecificReportTitle());
			m9kStatusReportDTO.setReportsDir(getReportsDir());
			m9kStatusReportDTO.setReportId(M9kUtils.getNextReportId());
			Date currDate = new Date();
			m9kStatusReportDTO.setReportDate( new java.text.SimpleDateFormat("yyyy-MM-dd").format(currDate));
			m9kStatusReportDTO.setReportTime( new java.text.SimpleDateFormat("HH:mm:ss").format(currDate));
			m9kStatusReportDTO.setStationSpecificDetailsDTO(stationReportDTO);
			m9kStatusReportDTO.getReportBody().append(String.format("%n%n%1$-30s %2$-30s %3$-30s %4$-30s %n%n", "Date/Time", "Action", "Status", "Description"));
			if (stationReportDTO.getLstOfStationAlarms() != null && !stationReportDTO.getLstOfStationAlarms().isEmpty())
			{
				for (Iterator<AlarmLogsDTO> iterator = stationReportDTO.getLstOfStationAlarms().iterator(); iterator.hasNext();) {
					alarmsLogDto =  iterator.next();
					m9kStatusReportDTO.getReportBody().append(String.format("%1$-30s %2$-30s %3$-30s %4$-30s %n%n", alarmsLogDto.getAlarmTime(), alarmsLogDto.getLedName(), alarmsLogDto.getLedStatus(), alarmsLogDto.getAlarmDescription()));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in station specific reports generation. ",e);
			m9kStatusReportDTO = null;
		}
		return m9kStatusReportDTO;
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
			logger.warn("Error occured in ContextDestroyed method in M9kReportListener ",e);
		}	
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		getTaskPerformer(); 
      scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, getInitDelay(), getRunAtInterval(), TimeUnit.MINUTES);
		
	}

	private Runnable getTaskPerformer() {
		taskPerformer = new Runnable() {
	          public void run() {
	        	  createDailyStatusReport();
	          }
	      };
	      return taskPerformer;
	}
	public void reScheduleRunAtHour()
	{
		try
		{
			scheduledTask.cancel(true);
	//		m9k_master_config.setProperty("daily-report-run-hour", 1);
			logger.info("Re-scheduling run at hour "+getRunAtHour()+ " at regular interval "+getRunAtInterval()+" minutes");
			logger.info("Before reschduling "+scheduledTask.isCancelled());
			scheduledTask = scheduler.scheduleWithFixedDelay(getTaskPerformer(), getInitDelay(), getRunAtInterval(), TimeUnit.MINUTES);
			logger.info("after reschduling "+scheduledTask.isCancelled());
		}
		catch (Exception e) {
			logger.error("Error occured in reScheduleRunAtHour method in M9kReportListener ",e);
		}
	}

	/**
	 *   10-May-2021 - Email reporting Properties moved to database from m9k-master property file
	 * @return
	 */
	public int getRunAtHour() {
//		if (m9k_master_config != null)
//		{
//			runAtHour=m9k_master_config.getInt("daily-report-run-at-hour", 0);
//		}
//		if (runAtHour < 0 && runAtHour > 23)
//		{
//			logger.error("The hour to run daily report specified in m9k-master.properties is invalid. Using 0 (midnight) as default");
//			runAtHour=0;
//		}
		// START: 28-June-2021 - Read from database
		try
		{
			runAtHour=Integer.parseInt(emailReportsSettingsDTO.getDailyStatusReportTime().substring(0, emailReportsSettingsDTO.getDailyStatusReportTime().indexOf(":")));
		}
		catch (Exception e) {
			runAtHour = 0;
			logger.warn("Unable to get time to run from database. Using default 00 hour",e); 
		}
		return runAtHour;
	}

	public void setRunAtHour(int runAtHour) {
		this.runAtHour = runAtHour;
	}

	/**
	 *   10-May-2021 - Email reporting Properties moved to database from m9k-master property file
	 * @return
	 */	
	public boolean getDailyStatusReport() {
//		if (m9k_master_config != null)
//		{
//			dailyStatusReportGeneration=m9k_master_config.getString("daily-status-report-generation", M9kConstants.ENABLE);
//		}
		dailyStatusReportGeneration = emailReportsSettingsDTO.isEnableDailyStatusEmails();
		return dailyStatusReportGeneration;
	}

	public void setDailyStatusReport(boolean dailyStatusReportGeneration) {
		this.dailyStatusReportGeneration = dailyStatusReportGeneration;
	}

	private long getInitDelay()
	{
		long initDelay = 0;
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		  if (getRunAtHour() < cal.get(Calendar.HOUR_OF_DAY)) // if the run at hour is already past the current time then schedule it next day else today
		  {
			  cal.add(Calendar.DAY_OF_MONTH, 1);
		  }
		  else if (getRunAtHour() == cal.get(Calendar.HOUR_OF_DAY) && getRunAtMin() < cal.get(Calendar.MINUTE))
		  {
			  cal.add(Calendar.DAY_OF_MONTH, 1);
		  }
//		  cal.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,hour,0, 0);
		  cal.set(Calendar.HOUR_OF_DAY,getRunAtHour());
		  cal.set(Calendar.MINUTE,getRunAtMin());
		  cal.set(Calendar.SECOND,0);
		  initDelay = ((cal.getTimeInMillis()-new Date().getTime())/1000/60)+1 ;
		  if (initDelay > getRunAtInterval())
		  {
			  initDelay = 0;
		  }
		  logger.info("About to schedule the report generator after this init delay "+initDelay);
		return initDelay;
	}

	public String getDailyStatusReportTitle() {
//		logger.debug("In getDailyStatusReportTitle m9k_master_config is Not Null? "+(m9k_master_config != null));
//		if (m9k_master_config != null)
//		{
//			dailyStatusReportTitle=m9k_master_config.getString("daily-status-report-title", "DME Daily Health Report");
//		}
//		else
//		{
//			logger.debug("In getDailyStatusReportTitle m9k_master_config is null using default title "+dailyStatusReportTitle);
//		}
		dailyStatusReportTitle = emailReportsSettingsDTO.getDailyStatusReportTitle();
		logger.debug("In getDailyStatusReportTitle About to return title "+dailyStatusReportTitle);
		return dailyStatusReportTitle;
	}
	
	public void setDailyStatusReportTitle(String dailyStatusReportTitle) {
		this.dailyStatusReportTitle = dailyStatusReportTitle;
	}
	
	public String getStationSpecificReportTitle() {
//		if (m9k_master_config != null)
//		{
//			stationSpecificReportTitle=m9k_master_config.getString("station-specific-status-report-title", "DME Alarms Report");
//		}
		stationSpecificReportTitle = emailReportsSettingsDTO.getStationSpecificStatusReportTitle();
		logger.debug("station specific report title "+stationSpecificReportTitle);
		return stationSpecificReportTitle;
	}

	public void setStationSpecificReportTitle(String stationSpecificReportTitle) {
		this.stationSpecificReportTitle = stationSpecificReportTitle;
	}

	public String getReportsDir() {
//		if (m9k_master_config != null)
//		{
//			reportsDir=m9k_master_config.getString("report-files-dir", "/data/m9k/status-reports/");
//			if (!reportsDir.endsWith("/"))
//			{
//				reportsDir+="/";
//			}
//		}
		reportsDir = emailReportsSettingsDTO.getDailyStatusReportDir();
		logger.debug("Reports Dir "+reportsDir);
		return reportsDir;
	}

	public void setReportsDir(String reportsDir) {
		this.reportsDir = reportsDir;
	}

	public int getRunAtMin() {
//		if (m9k_master_config != null)
//		{
//			runAtMin=m9k_master_config.getInt("daily-report-run-at-minute", 0);
//		}
//		if (runAtMin < 0 && runAtMin > 59)
//		{
//			logger.error("The minute to run daily report specified in m9k-master.properties is invalid. Using 0 as default");
//			runAtMin=0;
//		}
		// START: 28-June-2021 - Read from database
		try
		{
			runAtMin=Integer.parseInt(emailReportsSettingsDTO.getDailyStatusReportTime().substring(emailReportsSettingsDTO.getDailyStatusReportTime().indexOf(":")+1));
		}
		catch (Exception e) {
			runAtMin = 0;
			logger.warn("Unable to get time to run from database. Using default 00 minute",e); 
		}
		// END: 28-June-2021
		return runAtMin;
	}

	public void setRunAtMin(int runAtMin) {
		this.runAtMin = runAtMin;
	}

	public int getRunAtInterval() {
//		if (m9k_master_config != null)
//		{
//			runAtInterval=m9k_master_config.getInt("daily-report-repeat-interval", (24*60));
//		}
		// START: 28-June-2021 - Read from database
		runAtInterval = emailReportsSettingsDTO.getDailyStatusRepeatInterval();
		// END: 28-June-2021
		logger.debug("Report run at interval "+runAtInterval);
		return runAtInterval;
	}

	public void setRunAtInterval(int runAtInterval) {
		this.runAtInterval = runAtInterval;
	}

	public List<StationReportDTO> getLstOfFailedStations() {
		return lstOfFailedStations;
	}

	public void setLstOfFailedStations(List<StationReportDTO> lstOfFailedStations) {
		this.lstOfFailedStations = lstOfFailedStations;
	}
	
	public boolean isEventTestEnabled() {
		boolean booEventTest = false;
		String eventTest; 
		if (m9k_master_config != null)
		{
			eventTest=m9k_master_config.getString("event-board-test", "enable");
			if (eventTest.equalsIgnoreCase(M9kConstants.ENABLE))
			{
				booEventTest = true;
			}
			else
			{
				booEventTest = false;
			}
		}
		return booEventTest;
	}
	
}
