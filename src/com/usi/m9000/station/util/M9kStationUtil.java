/**
 * 
 */
package com.usi.m9000.station.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.ComtradeDataDAO;
import com.usi.m9000.dao.LongTermDataDAO;
import com.usi.m9000.dao.MySqlAlarmLogsDAO;
import com.usi.m9000.dao.MySqlDatDAO;
import com.usi.m9000.dao.MySqlReportDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.AlarmLogsDTO;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.M9kLED;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

/**
 * @author sramasamy
 *
 */
public class M9kStationUtil {
	private static HierarchicalINIConfiguration iniConf;
	private static PropertiesConfiguration m9000Config;
	
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationUtil.class);
//	private static StationDTO stationDetail;
	private static long currentSysTime = 0L; 
	private static long irig  = 0L;
	private static long dfrTime  = 0L;
	private static long currentReferenceTime = 0L;
	private static String masterHost = null;
	private static String masterType = null; // local or remote
	private static long watchDogTimer = 0L;
	private static int port = -1;
	private static String powerSupply = "powersupply";
	private static String calReportPath = null;
	private static String eventTestReportPath = null;
	private static double gainTolerance=-1;
	private static int ofsTolerance=-1;
	private static MySqlReportDAO mySqlReportDAO;
	private final static long BYTES_IN_GB = 1073741824L; // 1024*1024*1024
	private static boolean watchDogEnabled;
	private static String irigTimezone = null;
	private static String userPreferredTimezone = null; 
	private static int socketReadTimeout = 0;// In ms
	private static int socketConnectionTimeout = 0;// In ms
	private static int calibrateSocketReadTimeout = 0;// In ms
	private static int socketReadWarnTime = 0;// In ms
	
	private static String masterDbUser = null;
	private static String masterDbPwd = null;

	private static ResourceBundle bundle;
	private static String dataDir = null;
	private static Map<String,Boolean> mapDfrMonitorStatus = null;

	private static String masterNotifyQ = null;
	private static String triggerNotify = null;
	private static String masterNotification = null;
	private static String reportsDataTransfer = null;
	private static String pdcXmlFileWithPath = null;
	private static Map<String, String> mapUnitsToDataType;
	private static String serNotify = null;
	
	private static boolean atleastOneDFRsUp = true;
	
	private static String userDisableAlarms = null;
	
	private static MySqlAlarmLogsDAO mySqlAlarmLogsDAO;
	
	private static List<Object> lstHealthAttr = null;
	
	private static Boolean ledPolling = null; // To perform without power supply
	
	private static int totalOldestFaultsToDelete = 10; // Total oldest faults to delete as a part of disl cleanup
	private static ComtradeDataDAO comtradeDetailsDao = null;
	private static MySqlDatDAO mySqlDataDAO = null;
	private static LongTermDataDAO longTermDataDAO;
	private static int diskCleanupLimit = 95; //Start disk cleanup when usage percentage hits the below value. Default 95%
	private static int minDaysOfFaultsToRetain = 30;// default 30 days worth of faults to be retained in case of cleanup
	private static boolean pauseRecording = false; // Option to stop recording for reasons like disk full or maintenance
	private static int diskSafetyLimit = 80; // In percentage
	private static int stopRecordingAtDiskLimitPercentage = 98;
	
	static
	{
		try {
			iniConf = new HierarchicalINIConfiguration("station.properties");
			bundle = ResourceBundle.getBundle("M9K_COMTRADE");
			m9000Config = new PropertiesConfiguration("M9000.properties");
			mySqlReportDAO = new MySqlReportDAO();
			mySqlAlarmLogsDAO = new MySqlAlarmLogsDAO();
			M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
			comtradeDetailsDao = m9kDAOFactory.getComtradeDataDAO();
			mySqlDataDAO =new MySqlDatDAO();
			longTermDataDAO = m9kDAOFactory.getLongTermDataDAO();
		} catch (ConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.error("Unable to read cofiguration", e1);
		}
	}
	
	public synchronized static long getIRIG()
	{		
		M9kStationCommandClient commandClient;
//		long irig = 0L;
		long timeNow = System.currentTimeMillis();
		logger.debug("IRIG: Time elapsed since last request: "+(timeNow - currentSysTime));
		if (irig > 0&& (timeNow - currentSysTime) <= 500)
		{
			return irig;
		}
		else
		{
			currentSysTime = timeNow;
		}
		// Check if atleast one chassis is up to request IRIG from
		if (!isAtleastOneDFRsUp())
		{
			irig = timeNow*1000;
			logger.error("Cannot get IRIG from any of dfrs. Using station master computer time "+irig);
			return irig;
		}
		String result;
//		Set<String> dfrs = iniConf.getSections();
		String ipAddress = null;
		List<DfrDTO> lstDfrs;
		DfrDTO dfrDTO = null;
		try
		{
			lstDfrs = M9kStationXMLUtil.getListOfDFRs();
	//		for (String dfr : dfrs) {
			for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
				dfrDTO = iterator.next();
	//			if (!dfr.toUpperCase().startsWith("DFR"))
	//			{
	//				continue;
	//			}
				ipAddress = dfrDTO.getIpAddress();
				try {
	//				commandClient = new M9kStationCommandClient(ipAddress, port, "irig,user=machine");
					commandClient = new M9kStationCommandClient(ipAddress, getPort(), "dfrtime,user=machine");
					logger.debug("About to request IRIG from "+ipAddress);
					result = commandClient.sendAndReceive();
					logger.debug("IRIG recieved from "+ipAddress+" is "+result);
					irig = Long.parseLong(result);
					break;
				}
				catch (M9000Exception me)
				{
					logger.warn("Cannot get IRIG from "+dfrDTO.getDfrName()+" - "+ipAddress,me);
					irig = 0L;
				}
				catch (Exception e) {
					logger.warn("Cannot get IRIG from "+dfrDTO.getDfrName()+" - "+ipAddress,e);
					irig = 0L;
				}
			}
			currentSysTime = System.currentTimeMillis();
		}
		catch (Exception e) {
			logger.error("Cannot get IRIG from any of dfrs",e);
			irig = 0L;
		}
		if (irig == 0L)
		{
			irig = System.currentTimeMillis()*1000;
			logger.error("Cannot get IRIG from any of dfrs. Using station master computer time "+irig);
		}
		return irig;
	}

	public static long getDfrIRIGTime(String dfrName)
	{
		logger.debug("About to fetch IRIG for dfr "+dfrName);
		M9kStationCommandClient commandClient;
		String result;
		String ipAddress = null;
		
		
		try
		{
			if (dfrName != null)
			{
				String ipAddressPrefix = "192.168.1.";
				int ipAddressStartBase = 100;
				try
				{
					ipAddressPrefix = getPowerSupply().substring(0, getPowerSupply().lastIndexOf(".")+1);
					ipAddressStartBase = Integer.parseInt(getPowerSupply().substring(getPowerSupply().lastIndexOf(".")+1));
				}
				catch (Exception e)
				{
					ipAddressPrefix = "192.168.1.";
					ipAddressStartBase = 100;
				}
				int dfrId = Integer.parseInt(dfrName.substring(3)); // SUBSTRING of DFR1 will be 1 
				ipAddress = ipAddressPrefix+(ipAddressStartBase+dfrId);
				logger.debug("About to get dfrtime for ipaddress "+ipAddress);
				try {
	//				commandClient = new M9kStationCommandClient(ipAddress, port, "irig,user=machine");
					commandClient = new M9kStationCommandClient(ipAddress, getPort(), "dfrtime,user=machine");
					logger.debug("About to request IRIG from "+ipAddress);
					result = commandClient.sendAndReceive();
					logger.debug("IRIG recieved from "+ipAddress+" is "+result);
					irig = Long.parseLong(result);
				}
				catch (M9000Exception me)
				{
					logger.error("Cannot get IRIG from "+dfrName+" - "+ipAddress,me);
					irig = 0L;
				}
				catch (Exception e) {
					logger.error("Cannot get IRIG from "+dfrName+" - "+ipAddress,e);
					irig = 0L;
				}
			}
		}
		catch (Exception e) {
			logger.error("Cannot get IRIG from dfr "+dfrName,e);
			irig = 0L;
		}
		return irig;
	}

	public synchronized static long getDfrTime()
	{		
		M9kStationCommandClient commandClient;
//		long irig = 0L;
		long timeNow = System.currentTimeMillis();
		long timeElapsedInSeconds = (timeNow - currentReferenceTime);  
		logger.debug("IRIG: Time elapsed since last request: "+timeElapsedInSeconds);
		if (dfrTime > 0&& timeElapsedInSeconds <= 10000)
		{
			return (dfrTime+(timeElapsedInSeconds*1000));
		}
		else
		{
			currentReferenceTime = timeNow;
		}
		if (!isAtleastOneDFRsUp())
		{
			irig = timeNow*1000;
			logger.error("Cannot get IRIG from any of dfrs. Using station master computer time "+irig);
			return irig;
		}
		String result;
//		Set<String> dfrs = iniConf.getSections();
		List<DfrDTO> lstDfrs;
		DfrDTO dfrDTO = null;
		try
		{
			lstDfrs = M9kStationXMLUtil.getListOfDFRs();
			String ipAddress = null;
	//		for (String dfr : dfrs) {
			for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
				dfrDTO = iterator.next();
	//			if (!dfr.toUpperCase().startsWith("DFR"))
	//			{
	//				continue;
	//			}
				ipAddress = dfrDTO.getIpAddress();
				try {
	//				commandClient = new M9kStationCommandClient(ipAddress, port, "dfrTime,user=machine");
					if(isWatchDogEnabled() && isPowerSupplyAvailable())
		      		  {
		      			  petWatchDog();
		      		  }
					commandClient = new M9kStationCommandClient(ipAddress, getPort(), "dfrtime,user=machine");
					logger.debug("About to request dfrTime from "+ipAddress);
					result = commandClient.sendAndReceive();
					logger.debug("dfrTime recieved from "+ipAddress+" is "+result);
					dfrTime = Long.parseLong(result);
					break;
				}
				catch (M9000Exception me)
				{
					logger.error("Cannot get dfrTime from "+dfrDTO.getDfrName()+" - "+ipAddress,me);
					dfrTime = 0L;
				}
				catch (Exception e) {
					logger.error("Cannot get dfrTime from "+dfrDTO.getDfrName()+" - "+ipAddress,e);
					dfrTime = 0L;
				}
			}
			currentReferenceTime = System.currentTimeMillis();
		}
		catch (Exception e) {
			logger.error("Cannot get dfrTime from any of dfrss",e);
			dfrTime = 0L;
		}
		if (dfrTime == 0L)
		{
			dfrTime = System.currentTimeMillis() * 1000;
			logger.error("Cannot get dfrtime from any of dfrs. Using station master computer time "+dfrTime);
		}
		return dfrTime;
	}
	public static void petWatchDog()
	{		
		M9kStationCommandClient commandClient;
		String result;
		String ipAddress = getPowerSupply();
		long timeNow = System.currentTimeMillis();
//		logger.debug("WATCHDOG: watchDogTimer "+watchDogTimer);
		if (watchDogTimer == 0)
		{
			watchDogTimer = System.currentTimeMillis();
//			logger.debug("Initializing watchDogTimer as it was zero with : "+watchDogTimer);
		}
		long timeElapsedInSeconds = (timeNow - watchDogTimer);
		if (timeElapsedInSeconds <= 30000)
		{
//			logger.debug("timeElapsedInSeconds is not 30 seconds yet. so wait before petting "+timeElapsedInSeconds);
			return;
		}
		else
		{
			logger.debug("timeElapsedInSeconds is greater than 30 seconds . Reset ");
			watchDogTimer = 0;
		}
		
			try {
				commandClient = new M9kStationCommandClient(ipAddress, getPort(), "set");
				logger.debug("WATCHDOG-PET: About to pet watchdog in "+ipAddress);
				result = commandClient.sendAndReceive();
				logger.debug("watchdog response from "+ipAddress+" is "+result);
			}
			catch (M9000Exception me)
			{
				if (isPowerSupplyAvailable())
				{
					logger.error("Unable to pet the watchdog",me);
				}
			}
			catch (Exception e) {
				if (isPowerSupplyAvailable())
				{
					logger.error("Unable to pet the watchdog",e);
				}
			}
	}

	public static void enableWatchDog()
	{		
		M9kStationCommandClient commandClient;
		String result;
		String ipAddress = getPowerSupply();
			try {
				commandClient = new M9kStationCommandClient(ipAddress, getPort(), "set, LED=190,command=1");
				logger.debug("About to enable watchdog in "+ipAddress);
				result = commandClient.sendAndReceive();
				logger.debug("watchdog response from "+ipAddress+" is "+result);
			}
			catch (M9000Exception me)
			{
				logger.error("Unable to enable the watchdog",me);
			}
			catch (Exception e) {
				logger.error("Unable to enable the watchdog",e);
			}
	}

	public static void disableWatchDog()
	{		
		M9kStationCommandClient commandClient;
		String result;
		String ipAddress = getPowerSupply();
			try {
				commandClient = new M9kStationCommandClient(ipAddress, getPort(), "set, LED=190,command=0");
				logger.debug("About to disable watchdog in "+ipAddress);
				result = commandClient.sendAndReceive();
				logger.debug("watchdog response from "+ipAddress+" is "+result);
			}
			catch (M9000Exception me)
			{
				logger.error("Unable to disable the watchdog",me);
			}
			catch (Exception e) {
				logger.error("Unable to disable the watchdog",e);
			}
	}

	public static int getPercentageOfDiskUsed()
	{
		File root = new File("/");
		File home = new File("/home");
		File data = new File("/data");
		File dataDb = new File("/data-db");
		int highestPercentage;
		int percentage;
		
		float totalSpace = Math.round((float)((root.getTotalSpace()/ BYTES_IN_GB)+0.5));
//		logger.debug("DISK: Total space "+totalSpace);
//		logger.debug("DISK: Free space "+file.getFreeSpace());
		float usedSpace = Math.round((float)(((root.getTotalSpace()-root.getFreeSpace())/ BYTES_IN_GB)+0.5));
//		logger.debug("DISK: Used space "+usedSpace);
		highestPercentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
		
		totalSpace = Math.round((float)((home.getTotalSpace()/ BYTES_IN_GB)+0.5));
		usedSpace = Math.round((float)(((home.getTotalSpace()-home.getFreeSpace())/ BYTES_IN_GB)+0.5));
		percentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
		if (percentage > highestPercentage)
		{
			highestPercentage = percentage;
		}

		totalSpace = Math.round((float)((data.getTotalSpace()/ BYTES_IN_GB)+0.5));
		logger.debug("DATA: Total space "+totalSpace);
		usedSpace = Math.round((float)(((data.getTotalSpace()-data.getFreeSpace())/ BYTES_IN_GB)+0.5));
		logger.debug("DATA: usedSpace "+usedSpace);
		percentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
		logger.debug("DATA: percentage "+percentage);
		if (percentage > highestPercentage)
		{
			highestPercentage = percentage;
		}

		if (dataDb.exists()) // If data-db partition exists i.e. in case of dual drives
		{
			totalSpace = Math.round((float)((dataDb.getTotalSpace()/ BYTES_IN_GB)+0.5));
			usedSpace = Math.round((float)(((dataDb.getTotalSpace()-dataDb.getFreeSpace())/ BYTES_IN_GB)+0.5));
			percentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
			if (percentage > highestPercentage)
			{
				highestPercentage = percentage;
			}
		}

		logger.debug("Returning disk percentage used "+highestPercentage);
		return highestPercentage;
	}

	public static String getDiskUsageInformation()
	{
		
		StringBuffer diskUsageInfo = new StringBuffer(100);
		File root = new File("/");
		File home = new File("/home");
		File data = new File("/data");
		File data_db = new File("/data-db");
		
		diskUsageInfo.append("<table class=\"configureTable\"><th>Partitions</th><th>Total</th><th>Used</th><th>Used %</th>");
		float totalSpace = Math.round((float)((root.getTotalSpace()/ BYTES_IN_GB)+1.5));
//		logger.debug("DISK: Total space "+totalSpace);
//		logger.debug("DISK: Free space "+file.getFreeSpace());
		float usedSpace = Math.round((float)(((root.getTotalSpace()-root.getFreeSpace())/BYTES_IN_GB)+0.5));
//		logger.debug("DISK: Used space "+usedSpace);
		int percentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
		diskUsageInfo.append("<tr><td>"+"/"+"</td><td>"+totalSpace+"G</td><td>"+usedSpace+"G</td><td>"+percentage+"</td></tr>");
		
		totalSpace = Math.round((float)((home.getTotalSpace()/ BYTES_IN_GB)+0.5));
		usedSpace = Math.round((float)(((home.getTotalSpace()-home.getFreeSpace())/ BYTES_IN_GB)+0.5));
		percentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
		diskUsageInfo.append("<tr><td>"+"/home"+"</td><td>"+totalSpace+"G</td><td>"+usedSpace+"G</td><td>"+percentage+"</td></tr>");
		
		totalSpace = Math.round((float)((data.getTotalSpace()/ BYTES_IN_GB)+0.5));
		usedSpace = Math.round((float)(((data.getTotalSpace()-data.getFreeSpace())/ BYTES_IN_GB)+0.5));
		percentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
		diskUsageInfo.append("<tr><td>"+"/data"+"</td><td>"+totalSpace+"G</td><td>"+usedSpace+"G</td><td>"+percentage+"</td></tr>");
		
		if (data_db.exists())
		{
			totalSpace = Math.round((float)((data.getTotalSpace()/ BYTES_IN_GB)+0.5));
			usedSpace = Math.round((float)(((data.getTotalSpace()-data.getFreeSpace())/ BYTES_IN_GB)+0.5));
			percentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
			diskUsageInfo.append("<tr><td>"+"/data-db"+"</td><td>"+totalSpace+"G</td><td>"+usedSpace+"G</td><td>"+percentage+"</td></tr>");
		}
		
		diskUsageInfo.append("</table>");
		
		return diskUsageInfo.toString();
	}

	public static String getStringFromFile(String file) throws IOException 
	{
		InputStream inputStream = 
		    Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
		return convertStreamToString(inputStream);
	}
	private static String convertStreamToString(InputStream is) throws IOException {
	    if (is != null) {
	        Writer writer = new StringWriter();

	        char[] buffer = new char[1024];
	        try {
	            Reader reader = new BufferedReader(
	                    new InputStreamReader(is, "UTF-8"));
	            int n;
	            while ((n = reader.read(buffer)) != -1) {
	                writer.write(buffer, 0, n);
	            }
	        } finally {
	            is.close();
	        }
	        return writer.toString();
	    } else {        
	        return "";
	    }
	}
	
	public static String getDefaultCalibrationHeader()
	{
		StringBuffer strCalHeader = new StringBuffer();
		strCalHeader.append("DESCRIPTION");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("Each Analog channel can use any of four Analog gains; X1, X3, X6, & X8.");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("This calibration process measures Ground and an internal 1.25V Reference voltage"); 
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("using all four Analog gains for each Analog channel. Compensation values for");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("both Offset & Gain are then determined for each Analog channel for each Gain.");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("The 'Status' results reported below indicate whether the Analog channel calibrated");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("correctly or had an error. The Compensation values are stored in the");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("Station Master Database and are used for all subsequent measurements.");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("Successful completion of Internal Calibration assures published accuracy");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("at the V1, V2, & I terminals for all Analog channels.");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		strCalHeader.append("Refer to the system manual for specifications and recommendations.");
		strCalHeader.append(M9kStationConstants.NEWLINE);
		return strCalHeader.toString();
	}
	
	public static String getDefaultCalVerificationHeader()
	{
		StringBuffer calVerifyHeader = new StringBuffer();
		calVerifyHeader.append("Calibration VERIFICATION Report");
		calVerifyHeader.append(M9kStationConstants.NEWLINE);
		calVerifyHeader.append("Summary:   A self-calibration is run to produce Calibration Factors required to");
		calVerifyHeader.append(M9kStationConstants.NEWLINE);
		calVerifyHeader.append("bring the DFR into calibration. These Calibration Factors are compared");
		calVerifyHeader.append(M9kStationConstants.NEWLINE);
		calVerifyHeader.append("to the Calibration Factors that have been applied. The purpose is to");
		calVerifyHeader.append(M9kStationConstants.NEWLINE);
		calVerifyHeader.append("determine whether the applied Calibration Factors are still valid.");
		calVerifyHeader.append(M9kStationConstants.NEWLINE);
		calVerifyHeader.append(M9kStationConstants.NEWLINE);
		calVerifyHeader.append("(The Calibration Factors that are produced during the self-calibration");
		calVerifyHeader.append(M9kStationConstants.NEWLINE);
		calVerifyHeader.append("are not applied to the DFR.)");
		calVerifyHeader.append(M9kStationConstants.NEWLINE);
		calVerifyHeader.append(M9kStationConstants.NEWLINE);
		calVerifyHeader.append("There are four possible gains available: x1, x3, x6, & x8. A gain");
		calVerifyHeader.append(M9kStationConstants.NEWLINE);
		calVerifyHeader.append("scale factor and offset factor are determined and verified for all four gains.");
		calVerifyHeader.append(M9kStationConstants.NEWLINE);
		
		return calVerifyHeader.toString();
	}
	
	public static String getDefaultEventTestHeader()
	{
		StringBuffer strEventTestHeader = new StringBuffer();
		strEventTestHeader.append("TEST DESCRIPTION");
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		strEventTestHeader.append("==================");
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		strEventTestHeader.append("The DFR hardware provides a self-test mechanism to generate an Event on any");
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		strEventTestHeader.append("channel of all the Event cards. This test walks an Event through all of the"); 
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		strEventTestHeader.append("Event channels and automatically verifies that each channel is functional.");
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		strEventTestHeader.append("Test Results");
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		strEventTestHeader.append("==================");
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		strEventTestHeader.append("All of the Event channels in the DFR are shown below. A '.' indicates a Normal");
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		strEventTestHeader.append("state. A '*' indicates the Event channel has been activated. The correct response");
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		strEventTestHeader.append("should show an '*' in channel 1, then channel 2, and so on, for every Event");
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		strEventTestHeader.append("channel in the DFR. Only one Event channel should be active at a time.");
		strEventTestHeader.append(M9kStationConstants.NEWLINE);
		return strEventTestHeader.toString();
	}
	
	public static String getMasterHost()
	{
		if (masterHost == null)
		{
			masterHost = iniConf.getString("station.master_host", "localhost");
		}
		return masterHost;
	}

	public static String getMasterType()
	{
		if (masterType == null)
		{
			masterType = iniConf.getString("station.master-type", "local");
		}
		return masterType;
	}
	
	public static String getLastVerifiedDate() {
		return mySqlReportDAO.getLastVerifedDate();
	}
	public static String getLastCalDate() {
		return mySqlReportDAO.getLastCalibratedDate();
	}

	public static String getLastEventTestDate() {
		return mySqlReportDAO.getLastEventTestDate();
	}
	public static boolean isWatchDogEnabled() {
		String status = iniConf.getString("station.watch-dog", "DISABLE");
		if (status.equalsIgnoreCase(M9kStationConstants.ENABLE))
		{
			watchDogEnabled = true;
		}
		else
		{
			watchDogEnabled = false;
		}
		return watchDogEnabled;
	}

	public static void setWatchDogEnabled(boolean watchDogEnabled) {
		if (isPowerSupplyAvailable())
		{
			if (watchDogEnabled)
			{
				enableWatchDog();
			}
			else
			{
				disableWatchDog();
			}
		}
		else
		{
			logger.info("Alarm Module is either not available or not reachable");
		}
	}


	public static int getPort() {
		if (port == -1)
		{
			try
			{
				port = iniConf.getInt("station.port",9978);
			}
			catch (Exception e) {
				port = 9978;
				logger.warn("Trouble in reading port from properties file. Using default value as 9978 ",e);
			}
		}
		return port;
	}

	public static String getPowerSupply() {
//		if (powerSupply == null)
//		{
//			try
//			{
//				powerSupply = iniConf.getString("station.power-supply","192.168.1.100");
//			}
//			catch (Exception e) {
//				powerSupply = "192.168.1.100";
//				logger.warn("Trouble in reading powerSupply from properties file. Using default value as 192.168.1.100 ",e);
//			}
//		}
		return powerSupply;
	}

	public static String getCalReportPath() {
		if (calReportPath == null)
		{
			try
			{
				calReportPath = iniConf.getString("calibration.cal-report-path","/data/m9k/calibration-report");
			}
			catch (Exception e) {
				calReportPath = "/data/m9k/calibration-report";
				logger.warn("Trouble in reading calReportPath from properties file. Using default value as /data/m9k/calibration-report ",e);
			}
		}
		return calReportPath;
	}

	public static String getEventTestReportPath() {
		if (eventTestReportPath == null)
		{
			try
			{
				eventTestReportPath = iniConf.getString("eventtest.eventtest-report-path","/data/m9k/eventtest-report");
			}
			catch (Exception e) {
				eventTestReportPath = "/data/m9k/eventtest-report";
				logger.warn("Trouble in reading calReportPath from properties file. Using default value as /data/m9k/eventtest-report ",e);
			}
		}
		return eventTestReportPath;
	}
	public static double getGainTolerance() {
		if (gainTolerance == -1)
		{
			try
			{
				gainTolerance= iniConf.getDouble("calibration.gainTolerance",0.25);
			}
			catch (Exception e) {
				gainTolerance = 0.25;
				logger.warn("Trouble in reading gainTolerance from properties file. Using default value as 0.25 ",e);
			}
		}
		return gainTolerance;
	}

	public static int getOfsTolerance() {
		if (ofsTolerance == -1)
		{
			try
			{
				ofsTolerance= iniConf.getInt("calibration..ofsTolerance",40);
			}
			catch (Exception e) {
				ofsTolerance = 40;
				logger.warn("Trouble in reading ofsTolerance from properties file. Using default value as 40 ",e);
			}
		}
		return ofsTolerance;
	}
	
	public static java.text.SimpleDateFormat getDateFormat(String strFormat)
	{
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat (strFormat);
		if (isAtleastOneDFRsUp() && (getIrigTimezone() == null || getUserPreferredTimezone() == null || !getIrigTimezone().trim().equalsIgnoreCase("UTC") || getUserPreferredTimezone().trim().equalsIgnoreCase("UTC")))
	    {
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	    }
		return sdf;
	}

	public static String getIrigTimezone() {
		if (irigTimezone == null)
		{
			irigTimezone = iniConf.getString("station.irig-timezone", "local");
		}
		return irigTimezone;
	}

	public static void setIrigTimezone(String irigTimezone) {
		M9kStationUtil.irigTimezone = irigTimezone;
	}

	public static String getUserPreferredTimezone() {
		if (userPreferredTimezone == null)
		{
			userPreferredTimezone = iniConf.getString("station.user-preferred-timezone", "local");
		}
		return userPreferredTimezone;
	}

	public static void setUserPreferredTimezone(String userPreferredTimezone) {
		M9kStationUtil.userPreferredTimezone = userPreferredTimezone;
	}

	public static int getSocketReadTimeout() {
		if (socketReadTimeout == 0)
		{
			socketReadTimeout = iniConf.getInteger("network.socket-read-time-out", 30000);
		}
		return socketReadTimeout;
	}

	public static int getSocketConnectionTimeout() {
		if (socketConnectionTimeout == 0)
		{
			socketConnectionTimeout = iniConf.getInteger("network.socket-connection-time-out", 30000);
		}
		return socketConnectionTimeout;
	}

	public static int getCalibrateSocketReadTimeout() {
		if (calibrateSocketReadTimeout == 0)
		{
			calibrateSocketReadTimeout = iniConf.getInteger("network.calibrate-socket-read-time-out", 180000);
		}
		return calibrateSocketReadTimeout;
	}

	public static int getSocketReadWarnTime() {
		if (socketReadWarnTime == 0)
		{
			socketReadWarnTime = iniConf.getInteger("network.socket-read-warning-time", 1000);
		}
		return socketReadWarnTime;
	}
	
	public static String convertEpochToComtradeDisplayDate(long epochTime) throws M9000Exception
	{
	     SimpleDateFormat sdfUTC = new SimpleDateFormat("dd/MM/yyyy,HH:mm:ss.SSS");
	     Date inputDate = new Date(epochTime/1000);
	     if (!getIrigTimezone().trim().equalsIgnoreCase("UTC") || getUserPreferredTimezone().trim().equalsIgnoreCase("UTC"))
	     {
	    	 sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	     }
	     return sdfUTC.format(inputDate);
	}
	
	public static String getDataDir()
	{
		if (dataDir == null)
		{
			try
			{
				dataDir = bundle.getString("dataDir");
				if (dataDir != null)
				{
					if (!dataDir.endsWith(File.separator))
					{
						dataDir = dataDir+File.separator;
					}
				}
				else
				{
					logger.warn("dataDir from M9K_COMTRADE is null. Using default path /data/m9k/faults");
					dataDir = "/data/m9k/faults/";
				}
			}
			catch (Exception e)
			{
				logger.warn("There was trouble in reading dataDir from M9K_COMTRADE. Using default path /data/m9k/faults",e);
				dataDir = "/data/m9k/faults/";
			}
		}
		return dataDir;
	}
	
	public static String getMasterDbUser() {
		if (masterDbUser == null)
		{
			masterDbUser = iniConf.getString("station.master_db_user", "dfr");
		}
		return masterDbUser;
	}

	public static String getMasterDbPwd() {
		if (masterDbPwd == null)
		{
			masterDbPwd = iniConf.getString("station.master_db_pwd", "usi");
		}
		return masterDbPwd;
	}

	public static Map<String, Boolean> getMapDfrMonitorStatus() {
		if (mapDfrMonitorStatus == null)
		{
			mapDfrMonitorStatus = new HashMap<String, Boolean>(M9kStationXMLUtil.getListOfDFRs().size());
		}
		
		return mapDfrMonitorStatus;
	}

	public static String getMasterNotifyQ() {
		if (masterNotifyQ == null)
		{
			masterNotifyQ = iniConf.getString("master-notify.master-notify-queue", "Master.NotificationQ");
		}
		return masterNotifyQ;
	}
	
	public static String getCurrentDate()
	{
		String currDate="";
		try
		{
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		currDate = dateFormat.format(date); 
		}
		catch (Exception e) {
			logger.warn("Error in getting current date. Using long ",e);
			currDate = ""+System.currentTimeMillis();
		}
		return currDate;
	}
	public synchronized static void sendNotification(String severity, String messageToSend) throws M9000Exception
	{
		if (getMasterNotification().equalsIgnoreCase(M9kStationConstants.DISABLE))
		{
			return;
		}
		QueueConnection connection = null;
		QueueSession qSession = null;
		try
		{
			connection = getQConnection();
			qSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
	//       logger.debug("After session create ");
	       // Create the destination (Topic or Queue)
	       Queue destination = qSession.createQueue(getMasterNotifyQ());
	
	       QueueSender  sender  = qSession.createSender(destination);
	       sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	       javax.jms.TextMessage message = qSession.createTextMessage();
	       message.setText( messageToSend );
	       message.setStringProperty("STATION", M9kStationDBUtil.getStationTitle());
	       message.setStringProperty("DATE_OF_OCCURENCE", getCurrentDate());
	       message.setStringProperty("SEVERITY",severity);
	       logger.debug("Before sending message "+messageToSend);
	       sender.send(message);
	       qSession.close();
	       connection.close();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new M9000Exception("Unable to send notification", e);
		}
		finally {
		       try {
		    	   if (qSession != null)
		    	   {
		    		   qSession.close();
		    	   }
		    	   if (connection != null)
		    	   {
		    		   connection.close();
		    	   }
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
       
	}
	 private static QueueConnection getQConnection() throws JMSException
	    {
	    	QueueConnection connection=null;
	    	QueueConnectionFactory qConnectionFactory = (QueueConnectionFactory)new ActiveMQConnectionFactory("tcp://"+M9kStationUtil.getMasterHost()+":61616");

	        // Create a Connection
	    	connection = qConnectionFactory.createQueueConnection();
	        connection.start();
	        return connection;

	    }
	 
	 public static void setLedsForAction(String actionDetails) {
			M9kLED.setCalibrating(true);
			// Notify Web Master
			try {
				sendNotification(M9kSeverityLevels.INFO.name(), actionDetails);
			} catch (Exception e) {
				logger.debug("Error in notifying Calibration start event to Master station");			}
			M9kLED.setLedWithRelay(LedName.ONLINE, LedState.YELLOW,actionDetails);
//			M9kLED.setLedWithRelay(LedName.COMMUNICATION, LedState.RED, "DFRs taken offline for calibration");
//			M9kLED.setLedWithRelay(LedName.CLOCK_SYNC, LedState.OFF, "Warning: communication down");
//			M9kLED.setLedWithRelay(LedName.POWER, LedState.OFF, "Warning: communication down");
//			M9kLED.setLedWithRelay(LedName.DISK, LedState.OFF, "Warning: communication down");
//			M9kLED.setLedWithRelay(LedName.TEMPERATURE, LedState.OFF, "Warning: communication down");
			
		}

		public static void resetLeds(M9kSeverityLevels severityLevel, String calibInfo) {
			M9kLED.setCalibrating(false);
			// Notify Web Master
			try {
				sendNotification(severityLevel.name(), calibInfo);
			} catch (Exception e) {
				logger.debug("Error in notifying completion of Calibration to Master station");
			}
			if (isPowerSupplyAvailable())
			{
				M9kLED.getInstance().resetCalibrationLeds();
			}
			else
			{
				if (calibInfo.indexOf("EVENTTEST") != -1)
				{
					M9kLED.setLedWithRelay(LedName.ONLINE, LedState.GREEN,"EventTest complete");
				}
				else
				{
					M9kLED.setLedWithRelay(LedName.ONLINE, LedState.GREEN,"Calibration complete");
				}
			}
			
		}

		public static String getTriggerNotify() {
			if (triggerNotify == null)
			{
				triggerNotify = iniConf.getString("station.trigger-notify", "enable");
			}
			return triggerNotify;
		}

		public static void setTriggerNotify(String triggerNotify) {
			M9kStationUtil.triggerNotify = triggerNotify;
		}

		/**
		 * @return the serNotify
		 */
		public static String getSerNotify() {
			if (serNotify == null)
			{
				serNotify = iniConf.getString("station.ser-notify", "enable");
			}
			return serNotify;
		}

		/**
		 * @param serNotify the serNotify to set
		 */
		public static void setSerNotify(String serNotify) {
			M9kStationUtil.serNotify = serNotify;
		}

		public static String getUserDisableAlarms() {
			if (userDisableAlarms == null)
			{
				userDisableAlarms = iniConf.getString("station.user-disable-alarms", "disable");
			}
			return userDisableAlarms;
		}

		public static void setUserDisableAlarms(String userDisableAlarms) {
			M9kStationUtil.userDisableAlarms = userDisableAlarms;
		}

		public static String getMasterNotification() {
			if (masterNotification == null)
			{
				masterNotification = iniConf.getString("station.master-notification","enable");
			}
			return masterNotification;
		}

		public static void setMasterNotification(String masterNotification) {
			M9kStationUtil.masterNotification = masterNotification;
		}
		
		public static String getReportsDataTransfer() {
			if (reportsDataTransfer == null)
			{
				reportsDataTransfer = iniConf.getString("station.reports-data-transfer", "enable");
			}
			return reportsDataTransfer;
		}
		
		public static String getPdxXmlFileNameWithPath() {
			if (pdcXmlFileWithPath == null)
			{
				try
				{
					pdcXmlFileWithPath = iniConf.getString("station.pdc-xml-file-name-with-path","/home/dfr/.m9kpdc.xml");
				}
				catch (Exception e) {
					pdcXmlFileWithPath = "/home/dfr/.m9kpdc.xml";
					logger.warn("Trouble in reading pdc xml file path from properties file. Using default value as /home/dfr/.m9kpdc.xml ",e);
				}
			}
			return pdcXmlFileWithPath;
		}
		
		/**
		 * 17-Feb-2023 - New Alarms map Info into a file for m9kstatus command
		 * @return
		 */
		public static String getAlarmsMapInfoFileNameWithPath() {
			String alarmsMapInfoFileWithPath = "/home/dfr/.m9kalarmsmap";
				try
				{
					alarmsMapInfoFileWithPath = iniConf.getString("station.alarms-map-file-name-with-path","/home/dfr/.m9kalarmsmap");
				}
				catch (Exception e) {
					alarmsMapInfoFileWithPath = "/home/dfr/.m9kalarmsmap";
					logger.warn("Trouble in reading alarms map info file path from properties file. Using default value as /home/dfr/.m9kalarmsmap ",e);
				}
			return alarmsMapInfoFileWithPath;
		}
		
		public static String getContDataUnits(String dataType)
		{
			String dataUnit;
			if (mapUnitsToDataType == null)
			{
				populateContDataUnits();
			}
			dataUnit = mapUnitsToDataType.get(dataType.toUpperCase());
			if (dataUnit == null || dataUnit.isEmpty())
			{
				dataUnit = "%";
			}
			return dataUnit;
		}

		/**
		 * 
		 */
		private static void populateContDataUnits() {
			mapUnitsToDataType = new HashMap<String, String>();
			Iterator<String> iteratorUnits;
			String key;
			iteratorUnits = iniConf.getSection("cont-data-units").getKeys();
			while (iteratorUnits.hasNext())
			{
				key = iteratorUnits.next();
//				System.out.println("key "+key);
//				System.out.println("Value "+iniConf.getSection("cont-data-units").getString(key));
				mapUnitsToDataType.put(key.toUpperCase(), iniConf.getSection("cont-data-units").getString(key));
			}
		}
		public static void main(String[] args)
		{
			System.out.println(M9kStationUtil.getContDataUnits("Phase"));
			System.out.println("Disk percentage.."+M9kStationUtil.getPercentageOfDiskUsed());
			System.out.println("Orig; "+M9kStationUtil.isPauseRecording());
			M9kStationUtil.setPauseRecording(true);
			System.out.println("Set to true "+M9kStationUtil.isPauseRecording());
			M9kStationUtil.setPauseRecording(false);
			System.out.println("Set to false "+M9kStationUtil.isPauseRecording());
			System.out.println("Data dir "+getDataDir());
			String[] oldFaultFileNames = new File(getDataDir()+"R01-Sundowner").list();
			String fileDate;
			for (int i = 0; i < oldFaultFileNames.length; i++) {
				System.out.println("file name "+oldFaultFileNames[i]);
				fileDate = oldFaultFileNames[i].substring(0, oldFaultFileNames[i].indexOf(",", 8));
				System.out.println("File date "+fileDate);
				System.out.println(isFileOldEnoughToDelete(oldFaultFileNames[i]));
			}

		}

		/**
		 * @return the atleastOneDFRsUp
		 */
		public static boolean isAtleastOneDFRsUp() {
			return atleastOneDFRsUp;
		}

		/**
		 * @param atleastOneDFRsUp the atleastOneDFRsUp to set
		 */
		public static void setAtleastOneDFRsUp(boolean atleastOneDFRsUp) {
			M9kStationUtil.atleastOneDFRsUp = atleastOneDFRsUp;
		}
		
		// Gets the faultId from the given filename
		public static int getFaultIdFromFileName(String fileName) throws Exception
		{
			int faultId = -1;
			String strPatt = "R[0-9]+F[0-9]+";
			String strSubPatt = "F[0-9]+";
			String matchedString;
			Pattern searchPattern = Pattern.compile(strPatt);
			Matcher m = searchPattern.matcher(fileName);
			if(m.find()) {
				System.out.println("MAtch found. Start "+m.start() +" end "+m.end()+" toString "+m.group());
				matchedString = m.group();
				System.out.println("Matched String "+matchedString);
				searchPattern = Pattern.compile(strSubPatt);
				m = searchPattern.matcher(matchedString);
				m.find();
				faultId = Integer.parseInt(m.group().substring(1));
				System.out.println("Fault id "+faultId);
					
			}
			else
			{
				System.out.println("No Match Found :(");
			}
			
			return faultId;
		}
		
		public static void insertUserActionToAlarmsLog(String alarmDesc, String ledStatus)
		{
			AlarmLogsDTO alarmLogsDTO = new AlarmLogsDTO();
			alarmLogsDTO.setAlarmDescription(new StringBuffer(alarmDesc));
			alarmLogsDTO.setLedName(LedName.USER_ACTION.name());
			alarmLogsDTO.setLedStatus(ledStatus);
			alarmLogsDTO.setRelays("No Relays set or reset");
			alarmLogsDTO.setAlarmTime(getDateFormat("yyyy-MM-dd HH:mm:ss").format(M9kStationUtil.getIRIG()/1000));
			mySqlAlarmLogsDAO.insertAlarmDetails(alarmLogsDTO);
		}

		public static List<Object> getLstHealthAttr() {
			if (lstHealthAttr == null)
			{
				lstHealthAttr = m9000Config.getList("health-attributes");
				logger.debug("Before wett check lstHealthAttr values "+lstHealthAttr);
				if (M9kStationXMLUtil.getMonitorWettingVoltage() == 1)
				{
					if (!lstHealthAttr.contains(M9kStationConstants.WETTING_VOLTAGE))
					{
						lstHealthAttr.add(M9kStationConstants.WETTING_VOLTAGE);
					}
				}
				else
				{
					if (lstHealthAttr.contains(M9kStationConstants.WETTING_VOLTAGE))
					{
						lstHealthAttr.remove(M9kStationConstants.WETTING_VOLTAGE);
					}
				}
			}
			logger.debug("lstHealthAttr values "+lstHealthAttr);

			return lstHealthAttr;
		}

		public static void setLstHealthAttr(List<Object> lstHealthAttr) {
			M9kStationUtil.lstHealthAttr = lstHealthAttr;
		}

		public static boolean isLedPolling() {
			String ledPollingStatus;
			if (ledPolling == null)
			{
				ledPollingStatus = iniConf.getString("station.led-polling", "ENABLE");
				if (ledPollingStatus.equalsIgnoreCase(M9kStationConstants.DISABLE))
				{
					ledPolling = false;
				}
				else
				{
					ledPolling = true;
				}
			}
			return ledPolling;
		}

		public static boolean isDestinationReachable(String ip, int timeout){
		    boolean state = false;

		    try {
		        state = InetAddress.getByName(ip).isReachable(timeout);
		    } catch (IOException e) {
		        logger.error("Unable to ping the destination "+ip,e);
		    }

		    return state;
		}
		public static boolean isPowerSupplyAvailable() {
			return (isLedPolling() && isDestinationReachable(getPowerSupply(), 1000));
		}

		public static String backUpConfig() {
			BufferedWriter bwConfig = null;
			String backedUpFileName = null;
			try
			{
				Set<PosixFilePermission> configDirpermissions = PosixFilePermissions.fromString("rwxrwxr--");
				Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("r--r--r--");
				String dateTime = new java.text.SimpleDateFormat("MM-dd-yyyy,HH_mm_ss").format(System.currentTimeMillis());
				String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
				String dir = "/data/m9k/config-files/"+date;
				File configDir = new File(dir); 
				configDir.mkdirs();
				configDir.setWritable(true, false);
				configDir.setExecutable(true,false);
				configDir.setReadable(true, false);
				Path path = Paths.get(configDir.getPath());
				try
				{
				   Files.setPosixFilePermissions(path, configDirpermissions);
				}
				catch(Exception e)
				{
					logger.error("Unable to set permissions on config dir "+configDir.getPath(),e);
				}
	            
				backedUpFileName = dir+"/"+dateTime+","+getLocalTimeCode()+",R"+String.format("%02d", M9kStationDBUtil.getStationDetails().getSystemStationId())+"-"+ M9kStationDBUtil.getStationDetails().getSystemStationName()+"-config"+getM9kConfigFileExtension();
				logger.debug("File name to be created "+backedUpFileName);
				File configFile = new File(backedUpFileName);
				bwConfig = new BufferedWriter(new FileWriter(configFile));;
				bwConfig.write(M9kStationDBUtil.getStationDetails().getStationDetailsAsSQLStmt());
				bwConfig.close();
				bwConfig = null;
				try
				{
					Files.setPosixFilePermissions(Paths.get(backedUpFileName), permissions);
				}
				catch(Exception e)
				{
					logger.error("Unable to set permissions on config file "+backedUpFileName,e);
				}
				// START: 25-May-2016 Save the config file to CFast drive for backup
				String userHome = "/home/dfr/";
				try
				{
					 userHome = System.getProperty( "user.home" );
					 if (!userHome.endsWith("/"))
					 {
						 userHome+="/";
					 }
				}
				catch(Exception e)
				{
					userHome = "/home/dfr/";
					logger.warn("Unable to read user.home. Using default /home/dfr ",e);
				}
				path = Paths.get(userHome+"/m9k/config-files/");
				try
				{
					Files.setPosixFilePermissions(path, configDirpermissions);
				}
				catch(Exception e)
				{
					logger.error("Unable to set permissions on config dir "+userHome+"/m9k/config-files/",e);
				}
	            
				String secondaryDir = userHome+"/m9k/config-files/"+date;
				File secondaryConfigDir = new File(secondaryDir);
				secondaryConfigDir.mkdirs();
				path = Paths.get(secondaryDir);
				try
				{
					Files.setPosixFilePermissions(path, configDirpermissions);
				}
				catch(Exception e)
				{
					logger.error("Unable to set permissions on config dir "+secondaryDir,e);
				}
	            
				String secondaryFileName = secondaryDir+"/"+"R"+String.format("%02d", M9kStationDBUtil.getStationDetails().getSystemStationId())+"-"+ M9kStationDBUtil.getStationDetails().getSystemStationName()+"-config_"+dateTime+getM9kConfigFileExtension();
				logger.debug("Secondary File name to be created "+secondaryFileName);
				configFile = new File(secondaryFileName);
				bwConfig = new BufferedWriter(new FileWriter(configFile));;
				bwConfig.write(M9kStationDBUtil.getStationDetails().getStationDetailsAsSQLStmt());
				bwConfig.close();
				bwConfig = null;
				try
				{
					Files.setPosixFilePermissions(Paths.get(secondaryFileName), permissions);
				}
				catch(Exception e)
				{
					logger.error("Unable to set permissions on config file "+secondaryFileName,e);
				}
				// STOP: 25-May-2016
			}
			catch (Exception e) {
				logger.error("Error in writing out config file for backup",e);
			}
			finally
			{
				
				try {
					if (bwConfig != null)
					{
						bwConfig.close();
						bwConfig = null;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			return backedUpFileName;
		}
		
		public static int toIndex(double delta, double tus, int sampleCnt)
		{
			double raw = delta / tus;								// Calculate raw index distance
			if(raw < 0) return 0;									// If first sample or to the left, return 0
			int result = (int)Math.round(raw + 0.5);				// Round result to int
			return result >= sampleCnt ? sampleCnt - 1 : result;	// Limit result to sampleCnt - 1
		}
		
		/**
		 * This is set by user in the station.properties file
		 * @return
		 */
		public static int getDiskSafetyLimit() {
				try
				{
					diskSafetyLimit = iniConf.getInt("health.diskSafetyLimit",80);
				}
				catch (Exception e) {
					diskSafetyLimit = 95;
					logger.warn("Trouble in reading disk safety limit from properties file. Using default value as 80% ",e);
				}
			return diskSafetyLimit;
		}
		public static void setDiskSafetyLimit(int diskSafetyLimit) {
			M9kStationUtil.diskSafetyLimit = diskSafetyLimit;
		}
		public static int getDiskCleanupLimit() {
			try
			{
				diskCleanupLimit = iniConf.getInt("health.diskCleanupLimitPercentage",95);
			}
			catch (Exception e) {
				diskCleanupLimit = 95;
				logger.warn("Trouble in reading disk cleanup limit from properties file. Using default value as 95% ",e);
			}
		return diskCleanupLimit;
		}
		public static void setDiskCleanupLimit(int diskCleanupLimit) {
			M9kStationUtil.diskCleanupLimit = diskCleanupLimit;
		}
		
		/**
		 * DFR stops recording if the disk use percentage reaches this user specified level. Default 98%
		 * @return
		 */
		public static int getDiskLimitToStopRecording()
		{
			try
			{
				stopRecordingAtDiskLimitPercentage = iniConf.getInt("health.stopRecordingAtDiskLimitPercentage",98);
			}
			catch (Exception e) {
				stopRecordingAtDiskLimitPercentage = 98;
				logger.warn("Trouble in reading stopRecordingAtDiskLimitPercentage from properties file. Using default value as 98% ",e);
			}
			logger.debug("Stop recording if disk space reaches "+stopRecordingAtDiskLimitPercentage+"%");
			return stopRecordingAtDiskLimitPercentage;
		}
		
		public static void setDiskLimitToStopRecording(int stopRecordingAtDiskLimitPercentage)
		{
			M9kStationUtil.stopRecordingAtDiskLimitPercentage = stopRecordingAtDiskLimitPercentage;
		}
		/**
		 * Checks for the disk space availability. IF it reaches close to 100% or user set value it stops recording 
		 * @return
		 */
		public static boolean isDiskspaceAvailableToContinueRecording() {
			boolean isDiskspaceAvaialbleToRecord = true;
			logger.debug("");
			if (getPercentageOfDiskUsed() >= getDiskLimitToStopRecording())
			{
				isDiskspaceAvaialbleToRecord = false; // Stop recording if it reaches more than 98% of used space
			}
			return isDiskspaceAvaialbleToRecord;
		}
		
		public static int getMinDaysOfFaultsToRetain() {
			try
			{
				minDaysOfFaultsToRetain = iniConf.getInt("health.minDaysOfFaultsToRetain",30);
			}
			catch (Exception e) {
				minDaysOfFaultsToRetain = 30;
				logger.warn("Trouble in reading Minimum faults limit from properties file. Using default value as 30 days ",e);
			}
			return minDaysOfFaultsToRetain;
		}

		public static void setMinDaysOfFaultsToRetain(int minDaysOfFaultsToRetain) {
				M9kStationUtil.minDaysOfFaultsToRetain = minDaysOfFaultsToRetain;
		}
		public static void performDiskCleanup() throws M9000Exception
		{
			List<ComtradeDataDTO> lstComtradeDetails= comtradeDetailsDao.getLstOfOldestOfFaultsToDelete(M9kStationDBUtil.getStationDetails().getSystemStationId(), getTotalOldestFaultsToDelete(), getMinDaysOfFaultsToRetain());
			logger.info("About to delete "+lstComtradeDetails.size()+" total faults settings from properties file "+getTotalOldestFaultsToDelete()+" faults");
			logger.debug("Size of oldest faults retrieved "+lstComtradeDetails.size());
			if (lstComtradeDetails.size() > 0)
			{
				performDeleteForDiskCleanup(lstComtradeDetails, M9kConstants.FAULT_TYPE);
			}
			else
			{
				logger.info("No Faults to delete. Attempting to delete any old files laying around");
				try
				{
					String srcFolder = getDataDir()+M9kStationDBUtil.getStationFolderName()+File.separator;	
					// Remove unwanted files to clear space
					File[] oldFaultFiles = new File(srcFolder).listFiles();
					if (oldFaultFiles.length > 0)
					{
						logger.debug("first Faults file before sort "+oldFaultFiles[0].getName());
						sortDir(oldFaultFiles);
						logger.debug("first Faults file after sort "+oldFaultFiles[0].getName());
						logger.debug("Length of old Faults files "+oldFaultFiles.length);
						for (int i = 0; i < oldFaultFiles.length && i < getTotalOldestFaultsToDelete(); i++) {
							logger.debug("Faults file absolute path "+oldFaultFiles[i].getAbsolutePath()+"Faults file name "+oldFaultFiles[i].getName()+" is it a file? "+oldFaultFiles[i].isFile());
							if (oldFaultFiles[i].isFile() && isFileOldEnoughToDelete(oldFaultFiles[i].getName()))
							{
								oldFaultFiles[i].delete();
								logger.info("Deleted file "+oldFaultFiles[i]);
							}
						}
					}
				}
				catch (Exception e) {
					logger.warn("Attempt to delete any old files if any failed.",e);
				}
			}
			lstComtradeDetails= longTermDataDAO.getLstOfOldestLongTermDataDetails(M9kStationDBUtil.getStationDetails().getSystemStationId(), getTotalOldestFaultsToDelete(), getMinDaysOfFaultsToRetain());
			logger.info("About to delete "+getTotalOldestFaultsToDelete()+" DDR records");
			logger.debug("Size of oldest DDR records retrieved "+lstComtradeDetails.size());
			if (lstComtradeDetails.size() > 0)
			{
				performDeleteForDiskCleanup(lstComtradeDetails, M9kConstants.LTR_DATA);
			}
			else
			{
					logger.info("No DDR records to delete. Attempting to delete any old files laying around");			
				try
				{
					String srcFolder = getDataDir()+M9kStationDBUtil.getStationFolderName()+File.separator+M9kConstants.LTR_DIR+File.separator;	
					// Remove unwanted files to clear space
					File[] oldDDRFiles = new File(srcFolder).listFiles();
					if (oldDDRFiles.length > 0)
					{
						logger.debug("first DDR file before sort "+oldDDRFiles[0].getName());
						sortDir(oldDDRFiles);
						logger.debug("first DDR file after sort "+oldDDRFiles[0].getName());
						logger.debug("Length of old DDR files "+oldDDRFiles.length);
						for (int i = 0; i < oldDDRFiles.length && i < getTotalOldestFaultsToDelete(); i++) {
							logger.debug("DDR file absolute path "+oldDDRFiles[i].getAbsolutePath()+"DDR file name "+oldDDRFiles[i].getName()+" is it a file? "+oldDDRFiles[i].isFile());
							if (oldDDRFiles[i].isFile() && isFileOldEnoughToDelete(oldDDRFiles[i].getName()))
							{
								oldDDRFiles[i].delete();
								logger.info("Deleted file "+oldDDRFiles[i]);
							}
						}
					}
				}
				catch (Exception e) {
					logger.warn("Attempt to delete any old files if any failed.",e);
				}
			}
			
		}

		public static int getTotalOldestFaultsToDelete() {
			try
			{
				totalOldestFaultsToDelete = Integer.parseInt(bundle.getString("total-faults-to-delete").trim());
			}
			catch (Exception e) {
				logger.error("Unable to read 'total-faults-to-delete' from M9K_COMTRADE properties file. Default 10 faults will be deleted.",e);
				totalOldestFaultsToDelete = 10; // Default 10 faults will be deleted
			}
			return totalOldestFaultsToDelete;
		}
		
		public static void setTotalOldestFaultsToDelete(int totalOldestFaultsToDelete) {
			M9kStationUtil.totalOldestFaultsToDelete = totalOldestFaultsToDelete;
		}
		private static void performDeleteForDiskCleanup(List<ComtradeDataDTO> lstComtradeDetails, String dataType) throws M9000Exception
		{
			logger.info("About to delete files for dataType "+dataType);
			String srcDir = getDataDir();

			logger.debug("src dir "+srcDir);
			File fileToBeDeleted;
			StringBuffer strbufFaultIds = null; 
			ComtradeDataDTO comtradeDataDTO = null;
			for (Iterator<ComtradeDataDTO> iterator = lstComtradeDetails.iterator(); iterator.hasNext();) {
				comtradeDataDTO = iterator.next();
					if (strbufFaultIds == null)
					{
						strbufFaultIds = new StringBuffer(); 
						strbufFaultIds.append(comtradeDataDTO.getFaultId());
					}
					else
					{
						strbufFaultIds.append(","+comtradeDataDTO.getFaultId());
					}
					// Delete DAT
					fileToBeDeleted = new File(srcDir+comtradeDataDTO.getFileName()+M9kConstants.DAT_FILE_EXTN);
					logger.debug("Fault file to be deleted "+fileToBeDeleted.getPath()+" - "+fileToBeDeleted.getName());
					if (fileToBeDeleted.exists())
					{
						fileToBeDeleted.delete();
					}
					// Delete CFG
					fileToBeDeleted = new File(srcDir+comtradeDataDTO.getFileName()+M9kConstants.CFG_FILE_EXTN);
					logger.debug("Fault file to be deleted "+fileToBeDeleted.getPath()+" - "+fileToBeDeleted.getName());
					if (fileToBeDeleted.exists())
					{
						fileToBeDeleted.delete();
					}
					// Delete INF
					fileToBeDeleted = new File(srcDir+comtradeDataDTO.getFileName()+M9kConstants.INF_FILE_EXTN);
					logger.debug("Fault file to be deleted "+fileToBeDeleted.getPath()+" - "+fileToBeDeleted.getName());
					if (fileToBeDeleted.exists())
					{
						fileToBeDeleted.delete();
					}
			}
			if (dataType != null && !dataType.isEmpty())
			{
				if(dataType.equalsIgnoreCase(M9kConstants.FAULT_TYPE))
				{
					if (strbufFaultIds != null && strbufFaultIds.length() > 0 )
					{
						try
						{
							comtradeDetailsDao.deleteFaults(M9kStationDBUtil.getStationDetails().getSystemStationId(), strbufFaultIds.toString());
							mySqlDataDAO.deleteFaultsFromDatStaging(strbufFaultIds.toString());
						}
						catch (Exception e) {
							logger.error("Error in deleting from dat_staging table for remote architecture for faults "+strbufFaultIds,e);
						}
					}
				}
				else // LTR Type data
				{
					longTermDataDAO.deleteLTRRecords(M9kStationDBUtil.getStationDetails().getSystemStationId(), strbufFaultIds.toString());
				}
			}		

		}

		/**
		 * @return the pauseRecording
		 */
		public static boolean isPauseRecording() {
			pauseRecording = iniConf.getBoolean("health.pauseRecording",false);
			return pauseRecording;
		}

		/**
		 * @param pauseRecording the pauseRecording to set
		 */
		public static void setPauseRecording(boolean pauseRecording) {
			M9kStationUtil.pauseRecording = pauseRecording;
			iniConf.setProperty("health.pauseRecording", "true");
		}
		
		
		public static boolean isFileOldEnoughToDelete(String fileName)
		{
			boolean fileIsOldEnoughToDelete = false;
//			DateFormat dateFileFormat = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat sdf = new SimpleDateFormat(M9kStationConstants.COMTRADE_FILE_DATE_FORMAT);
			try
			{
				Pattern matchTimestampPattern = Pattern.compile("\\d{6},\\d{9}",Pattern.DOTALL); // Matching the timestamp yyMMdd,HHmmss in the file name
				Matcher timeStampMatched = matchTimestampPattern.matcher(fileName);
				if (timeStampMatched.find())
				{
					String fileTimeStamp = timeStampMatched.group(0);
					logger.debug("Matched time stamp "+fileTimeStamp);
					System.out.println("Matched time stamp "+fileTimeStamp);
//					Date fileDate= sdf.parse(fileName.substring(0, fileName.indexOf(",",fileName.indexOf(",")+1)));
					Date fileDate= sdf.parse(fileTimeStamp);
					Date today = new Date();
					int noDaysOld = (int)((today.getTime() - fileDate.getTime())/(1000 * 60 * 60 * 24));
					if (noDaysOld > getMinDaysOfFaultsToRetain())
					{
						logger.debug("It is older than the number of files to retain."+noDaysOld +" > "+ getMinDaysOfFaultsToRetain());
						fileIsOldEnoughToDelete = true;
					}
				}
				else
				{
					fileIsOldEnoughToDelete = true;
				}
			}
			catch (Exception e) {
				logger.error("Error occured when extracting date from file name "+fileName);
			}
			logger.debug("Is old enough to delete? "+fileIsOldEnoughToDelete);
			
			return fileIsOldEnoughToDelete;
		}
		
		public static String getM9kConfigFileExtension()
		{
			String cfgExtn = ".sql";
			try
			{
				cfgExtn = bundle.getString("m9000-config-file-extn");
				if (cfgExtn != null && !cfgExtn.isEmpty())
				{
					if (!cfgExtn.startsWith("."))
					{
						cfgExtn="."+cfgExtn;
					}
				}
			}
			catch (Exception e) {
				logger.error("Unable to get m9k config file extension. Using default sql.",e);
				cfgExtn = ".sql";
			}
			return cfgExtn;
		}

		public static String getLocalTimeCode()
		{
			String timeCode;
			try
			{
				// Start: Modified the timezone calculation logic 02-Dec-2013
//				TimeZone tz = Calendar.getInstance().getTimeZone();
//				timeCode = (tz.getRawOffset()/1000/3600)+"t";
				Calendar cal = Calendar.getInstance();
				timeCode = (cal.get(Calendar.ZONE_OFFSET)+cal.get(Calendar.DST_OFFSET))/1000/3600 +"t";
				// End
			}
			catch (Exception e) {
				logger.error("Cannot get TimeZone and rawoffset for timecode. Attempting to read from M9K_COMTRADE",e);
				try
				{
					timeCode = bundle.getString("timeCode");
				}
				catch (Exception e1) {
					logger.error("Cannot get TimeZone from M9K_COMTRADE either. Using default -5t",e1);
					timeCode = "-5t";
				}
			}
			return timeCode;
		}
		
		public static boolean isDiskSpaceAvailableToRecord()
		{
			boolean diskSpaceAvailable = true;
			if (getPercentageOfDiskUsed() > getDiskCleanupLimit())
			{
				
			}
			return diskSpaceAvailable;
		}

		/**
		 * To sort the list of files to be deleted based on last modified
		 */
		static class Pair implements Comparable<Object> {
		    public long t;
		    public File f;

		    public Pair(File file) {
		        f = file;
		        t = file.lastModified();
		    }

		    public int compareTo(Object o) {
		        long u = ((Pair) o).t;
		        return t < u ? -1 : t == u ? 0 : 1;
		    }
		};

		public static void sortDir(File[] srcFiles)
		{
			Pair[] pairs = new Pair[srcFiles.length];
			for (int i = 0; i < srcFiles.length; i++)
			    pairs[i] = new Pair(srcFiles[i]);
	
			// Sort them by timestamp.
			Arrays.sort(pairs);
	
			// Take the sorted pairs and extract only the file part, discarding the timestamp.
			for (int i = 0; i < srcFiles.length; i++)
				srcFiles[i] = pairs[i].f;
		}
		
		public static Map<String,String> getRelevantIpAddresses()
		{
			Enumeration<InetAddress> inetAddresses;
			Map<String,String> mapIpaddresses = new HashMap<String, String>();
			try
			{
				Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		        for (NetworkInterface netint : Collections.list(nets))
		        {
		        	inetAddresses = netint.getInetAddresses();
			        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
			            if (!inetAddress.getHostAddress().equalsIgnoreCase("127.0.0.1") && 
			            		!inetAddress.getHostAddress().equalsIgnoreCase("192.168.1.1") &&
			            		isValidIPAddress(inetAddress.getHostAddress()))
			            {
			            	mapIpaddresses.put(netint.getName(), inetAddress.getHostAddress());
			            }
			        }
		        }
			}
			catch(Exception e)
			{
				logger.warn("Issue with getting local ip addresses ",e);
			}
			return mapIpaddresses;
	            
		}
		
		// From https://www.geeksforgeeks.org/how-to-validate-an-ip-address-using-regular-expressions-in-java/
		public static boolean isValidIPAddress(String ip)
	    {
	 
	        // Regex for digit from 0 to 255.
	        String zeroTo255
	            = "(\\d{1,2}|(0|1)\\"
	              + "d{2}|2[0-4]\\d|25[0-5])";
	 
	        // Regex for a digit from 0 to 255 and
	        // followed by a dot, repeat 4 times.
	        // this is the regex to validate an IP address.
	        String regex
	            = zeroTo255 + "\\."
	              + zeroTo255 + "\\."
	              + zeroTo255 + "\\."
	              + zeroTo255;
	 
	        // Compile the ReGex
	        Pattern p = Pattern.compile(regex);
	 
	        // If the IP address is empty
	        // return false
	        if (ip == null) {
	            return false;
	        }
	 
	        // Pattern class contains matcher() method
	        // to find matching between given IP address
	        // and regular expression.
	        Matcher m = p.matcher(ip);
	 
	        // Return if the IP address
	        // matched the ReGex
	        return m.matches();
	    }
		
		/**
		 * get config backup folder  config-file path
		 */
		public static void getConfigBackupPath()
		{
			File configDir = new File(dataDir);
			configDir = new File(configDir.getParent()+File.separator+"config-files");
		}
		/**
		 * get config backup folder  config-file path
		 */
		public static void getlogFilePath()
		{
			File configDir = new File(dataDir);
			configDir = new File(configDir.getParent()+File.separator+"log");
		}
		/**
		 * Data cleanup for import config and remove stations
		 */
		public static void m9kCleanup()  throws Exception
		{
			// Cleanup database tables
			logger.info("Clearing database data");
			M9kStationDBUtil.m9kStationDbCleanup();
			// Faults Data dir
			logger.info("Removing Data dir..."+getDataDir());
			deleteFilesFromDirectory(getDataDir());
			// Calibration report Directory
			logger.info("Removing Cal report dir..."+getCalReportPath());
			deleteFilesFromDirectory(getCalReportPath());
			// Event test report Directory
			logger.info("Removing Event test report dir..."+getEventTestReportPath());
			deleteFilesFromDirectory(getEventTestReportPath());
			// SER Email directory
			logger.info("Removing SER Email dir..."+M9kUtils.getSEREmailDir());
			deleteFilesFromDirectory(M9kUtils.getSEREmailDir());
			// Email Reports Email directory
			logger.info("Removing Reports dir..."+M9kUtils.getReportsDir());
			deleteFilesFromDirectory(M9kUtils.getReportsDir());				
		}
		
		private static void deleteFilesFromDirectory(String dirPath)  throws Exception
		{
			File sourceDir = new File(dirPath);
			if (sourceDir.exists())
			{
				for(File file: sourceDir.listFiles()) 
				{
					if (file.isDirectory())
					{
						deleteFilesFromDirectory(file.getAbsolutePath());
					}
					file.delete();
				}
			}
		}
		
		public static String getTimeToRunDropContOscEvent()
		{
			return iniConf.getString("station.drop-cont-osc-event-time", "0510");
		}
		
		public static String getTimeToRunDropContMeasurementEvent()
		{
			return iniConf.getString("station.drop-cont-measurements-event-run-time", "0520");
		}
		
		public static String getTimeToRunCreateContOscEvent()
		{
			return iniConf.getString("station.create-cont-osc-event-run-time", "0610");
		}
		
		public static String getTimeToRunCreateContMeasurementEvent()
		{
			return iniConf.getString("station.create-cont-measurements-event-run-time", "0620");
		}
		
		public static String getTimeToRunRemoveOldSerEvent()
		{
			return iniConf.getString("station.remove-old-ser-event-run-time", "0430");
		}

		public static String getTimeToRunRemoveOldDfrlogEvent()
		{
			return iniConf.getString("station.remove-old-dfrlog-event-run-time", "0410");
		}
		
		public static int getNoOfYearsToRetainSer()
		{
			return iniConf.getInt("station.no-of-years-to-retain-ser", 5);
		}
		
		public static int getNoOfYearsToRetainDfrlog()
		{
			return iniConf.getInt("station.no-of-years-to-retain-dfrlog", 1);
		}
		
		public static String getStationMasterComputerType()
		{

			    String computerType = "MOXA";
			    	logger.debug("In getStationMasterComputerType() ");
			    try {
			      Process process = Runtime.getRuntime().exec("systemctl status m9ksysio.service");
			      process.waitFor();
			      int exitValue = process.exitValue();
			      logger.debug("exit code ");
			      if (exitValue == 4) {
			    	  computerType = "ARBOR";
			      } else {
			    	  computerType = "MOXA";
			      }

			    } catch (Exception e) {
			    	computerType = "MOXA"; // Defaults to Moxa
			      logger.error("Error executing the process: " + e.getMessage());
			    }
			   return computerType;
		}
		
		 public static String getDFRNameFromIP(String ipAddress) {
		        // Split the IP address into octets using dot as delimiter
			 String dfrName="DFR";
		        String[] octets = ipAddress.split("\\.");

		        if (octets.length != 4) {
		            logger.error("Invalid IP address format"+ipAddress);
		            // returning ? as we don't know the DFR name
		            dfrName+="?";
		            return dfrName;
		        }

		        // Extract the fourth octet
		        String fourthOctetStr = octets[3];

		        // Ensure the fourth octet is a valid integer
		        try {
		            int fourthOctet = Integer.parseInt(fourthOctetStr);

		            // Extract the last two digits of the fourth octet
		            int lastTwoDigits = fourthOctet % 100; // Modulus operation to get last two digits

		            dfrName+= lastTwoDigits;
		        } catch (NumberFormatException e) {
		        	 logger.error("Invalid IP address format"+ipAddress);
			            // returning ? as we don't know the DFR name
			         dfrName+="?";
			         return dfrName;
		        }
		        return dfrName;
		    }

}
