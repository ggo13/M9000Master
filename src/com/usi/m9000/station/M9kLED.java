package com.usi.m9000.station;

import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.xmlbeans.XmlOptions;

import com.usi.health.AttributeDocument.Attribute;
import com.usi.health.DFRDocument.DFR;
import com.usi.health.SubStationDocument.SubStation;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.MySqlAlarmLogsDAO;
import com.usi.m9000.dto.AlarmLogsDTO;
import com.usi.m9000.station.Exception.M9kThreadExceptionHandler;
import com.usi.m9000.station.commands.M9kCommandThreads;
import com.usi.m9000.station.commands.M9kDFRHealthStatusProcessor;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.LedName;
import com.usi.m9000.station.util.LedState;
import com.usi.m9000.station.util.M9kSeverityLevels;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.station.util.RelayName;

/**
 * 
 * @author sramasamy
 * Release v2.0 - 31-May-2022 - Relay Mappings are read from config in xml util 
 */

public class M9kLED {
	private M9kDFRHealthStatusProcessor m9kDFRHealthStatusProcessor;
	private SubStation substationHlth;
	private static int LED_VALUE = 0;
// to initialize Relay to not actice state
	private static int RELAY_VALUE = 255;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> scheduledTask = null;
	private static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kLED.class);
	private ExecutorService threadHandlerExecutor;
	private int runFrequency = 10; // in seconds
	private static HierarchicalINIConfiguration iniConf;
	private static double v5Low=4.8;
	private static double v5High=5.2;
	private static double v3_3Low = 3.2;
	private static double v3_3High = 3.4;
	private static double v2_5Low=2.4;
	private static double v2_5High=2.6;
	private static double v1_5Low=1.4;
	private static double v1_5High=1.6;
	private static double v1_2Low=1.1;
	private static double v1_2High=1.3;
	private static double v1_8Low=1.7;
	private static double v1_8High=1.9;
	private static double tempLow=1;
	private static double tempHigh=50;
	private static double tempWarnLimit=3;
	private static double fanRPMLow=2000;
	private static double fanRPMHigh=5000;
	private static int diskSafetyLimit = 80; // In percentage
	public static int triggerDuration = 10;// in seconds
	public static int dfrTimeDiffLimit = 30;// in seconds. This is the maximum limit in seconds can a DFR time can have for each DFRs. It it exceeds this limit then thrown an alarm
	// 31-May-2022 - Relay Mappings are read from config in xml util 
//	public static Map<LedName, List<Object>> mapLedToRelay;
	private XmlOptions xmlOptions;
	private static String ipAddress = M9kStationUtil.getPowerSupply();
	private static int port = M9kStationUtil.getPort();
	private static Timer timer;
	private static boolean alreadyTriggering = false;
	private static int iCount = 0;
	private static MySqlAlarmLogsDAO mySqlAlarmLogsDAO;
	private static AlarmLogsDTO triggerAlarmLogsDTO;
	private String communicationDesc;
	private static boolean booStationActive = true;
	private static StringBuffer notifyReport;
	private static M9kSeverityLevels msgSeverity;
	private static boolean isCalibrating = false;
	private static M9kLED thisInstance = null;
	public static boolean isPowerSupplyDownReported=false;
	public static LedState currentWettVState = LedState.GREEN;
	public static Map<LedName, LedState> mapCurrentLEDState;
	private static boolean disableAlarm = false;
	private static boolean testTrigger = false;

	// 11-DEC-2014 to keep track of temperature readings to warn user
	private double temperatureVal;
	
	// 18-June-2015 To use IRIG as updated in alarms_log time
	private static String irigDateToMysqlDSate;
	
	private static boolean isPowerSupplyErrorLogged= false;  // Report power supply unavailability only once
	
	private static boolean diskUsageAlmostFull=false; // To handle disk usage full scenario 
	
	private static boolean alreadyDisturbanceAlarm = false;
	private static Timer disturbanceAlarmTimer;
	
	static 
	{
		init();
	}
	private M9kLED()
	{
		m9kDFRHealthStatusProcessor = new M9kDFRHealthStatusProcessor();
		threadHandlerExecutor = Executors.newSingleThreadExecutor();
		mySqlAlarmLogsDAO = new MySqlAlarmLogsDAO();
		xmlOptions = new XmlOptions();
		xmlOptions.setSaveOuter();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setUseDefaultNamespace();
		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("", "http://www.usi.com/health");
		xmlOptions.setSaveImplicitNamespaces(prefixes);
//		init();
		
	}
	public static M9kLED getInstance()
	{
		if (thisInstance == null)
		{
			thisInstance = new M9kLED();
		}
		return thisInstance;
	}
	public static void init()
	{
		try
		{
//			mapLedToRelay = new HashMap<LedName, List<Object>>(10);
			mapCurrentLEDState = new HashMap<LedName, LedState>(10);
			ipAddress = M9kStationUtil.getPowerSupply();
			if (!M9kStationUtil.isPowerSupplyAvailable())
			{
				if (!isPowerSupplyErrorLogged)
				{
					isPowerSupplyErrorLogged = true;
					logger.info("Alarm Module is either not available or not reachable");
					M9kStationUtil.insertUserActionToAlarmsLog("Alarm Module is either not available or not reachable", LedState.RED.name());
				}
			}
			port = M9kStationUtil.getPort();
			
			iniConf = new HierarchicalINIConfiguration();
			iniConf.setDelimiterParsingDisabled(false);
			logger.debug("Default list delimeiter "+HierarchicalINIConfiguration.getDefaultListDelimiter());
			iniConf.load("station.properties");
			Set<String> sections = iniConf.getSections();
			logger.debug("Entered Init: sections  "+sections);
			
/**			
			// START: 10-Jan-2017 - loading properties without loop 
			// Generic loading of relayInfo section from station.properties
			List<Object> lstRelayName = null;
			SubnodeConfiguration subSection = iniConf.getSection("RelayInfo");
			if (subSection != null)
			{
				for (Iterator<String> iterateRelayKeys = subSection.getKeys(); iterateRelayKeys
						.hasNext();) {
					String relayKey = iterateRelayKeys.next();
					lstRelayName = subSection.getList(relayKey);
					if (lstRelayName != null && !lstRelayName.isEmpty())
					{
						mapLedToRelay.put(LedName.valueOf(relayKey), lstRelayName);
					}
					
				}
			}
			else
			{
				lstRelayName = new ArrayList<Object>();
				lstRelayName.add("Relay_1");
				mapLedToRelay.put(LedName.valueOf("ONLINE"), lstRelayName);
				
				lstRelayName = new ArrayList<Object>();
				lstRelayName.add("Relay_2");
				mapLedToRelay.put(LedName.valueOf("TRIGGER"), lstRelayName);

				lstRelayName = new ArrayList<Object>();
				lstRelayName.add("Relay_3");
				mapLedToRelay.put(LedName.valueOf("CLOCK_SYNC"), lstRelayName);
				
				lstRelayName = new ArrayList<Object>();
				lstRelayName.add("Relay_4");
				mapLedToRelay.put(LedName.valueOf("COMMUNICATION"), lstRelayName);
				
				lstRelayName = new ArrayList<Object>();
				lstRelayName.add("Relay_5");
				mapLedToRelay.put(LedName.valueOf("POWER"), lstRelayName);
				
				lstRelayName = new ArrayList<Object>();
				lstRelayName.add("Relay_6");
				mapLedToRelay.put(LedName.valueOf("DISK"), lstRelayName);
				
				lstRelayName = new ArrayList<Object>();
				lstRelayName.add("Relay_7");
				mapLedToRelay.put(LedName.valueOf("TEMPERATURE"), lstRelayName);
				
				logger.error("Unable to load Subsection [RelayInfo] from M9k_COMTRADE.properties. Using default values");
			}
**/			
			// Read Health attributes
			SubnodeConfiguration subSection = iniConf.getSection("health");
			if (subSection != null)
			{
				v5Low = subSection.getDouble("v5-low",4.8);
				v5High = subSection.getDouble("v5-high",5.2);
				v3_3Low = subSection.getDouble("v3_3-low",3.2);
				v3_3High = subSection.getDouble("v3_3-high",3.4);
				v2_5Low = subSection.getDouble("v2_5-low",2.4);
				v2_5High = subSection.getDouble("v2_5-high",2.6);
				v1_5Low = subSection.getDouble("v1_5-low",1.4);
				v1_5High = subSection.getDouble("v1_5-high",1.6);
				v1_2Low = subSection.getDouble("v1_2-low",1.1);
				v1_2High = subSection.getDouble("v1_2-high",1.3);
				v1_8Low = subSection.getDouble("v1_8-low",1.7);
				v1_8High = subSection.getDouble("v1_8-high",1.9);
				tempLow = subSection.getDouble("temp-low",1);
				tempHigh = subSection.getDouble("temp-high",70);
				tempWarnLimit = subSection.getDouble("temp-warn-limit",3);
				fanRPMLow = subSection.getDouble("fanRPM-low",2000);
				fanRPMHigh = subSection.getDouble("fanRPM-high",5000);
				diskSafetyLimit = subSection.getInt("diskSafetyLimit",80);
				dfrTimeDiffLimit= subSection.getInt("dfrTimeDiffLimit",30);
				logger.debug("diskSafetyLimit after read from file "+diskSafetyLimit);
			}
			else
			{
				logger.error("Unable to load Subsection [health] from M9k_COMTRADE.properties. Using default values");
			}
			
// START: 05-July-2022 - Moved the Trigger Duration to GUI			
//			// Load TriggerLED attributes
//			subSection = iniConf.getSection("TriggerLed");
//			if (subSection != null)
//			{
//				triggerDuration = subSection.getInt("duration",10);
//				logger.debug("TRIGGER: duration " +triggerDuration);
//			}
//			else
//			{
//				logger.error("Unable to load Subsection [TriggerLed] from M9k_COMTRADE.properties. Using default values");
//			}
// END: 05-July-2022
			
//			for (String section : sections) {
//				logger.debug("section "+section);
//				if (section.equalsIgnoreCase("health"))
//				{
//					logger.debug("[" + section+ "]");
//					v5Low = iniConf.getDouble(section + ".v5-low",4.8);
//					v5High = iniConf.getDouble(section + ".v5-high",5.2);
//					v3_3Low = iniConf.getDouble(section + ".v3_3-low",3.2);
//					v3_3High = iniConf.getDouble(section + ".v3_3-high",3.4);
//					v2_5Low = iniConf.getDouble(section + ".v2_5-low",2.4);
//					v2_5High = iniConf.getDouble(section + ".v2_5-high",2.6);
//					v1_5Low = iniConf.getDouble(section + ".v1_5-low",1.4);
//					v1_5High = iniConf.getDouble(section + ".v1_5-high",1.6);
//					v1_2Low = iniConf.getDouble(section + ".v1_2-low",1.1);
//					v1_2High = iniConf.getDouble(section + ".v1_2-high",1.3);
//					v1_8Low = iniConf.getDouble(section + ".v1_8-low",1.7);
//					v1_8High = iniConf.getDouble(section + ".v1_8-high",1.9);
//					tempLow = iniConf.getDouble(section + ".temp-low",1);
//					tempHigh = iniConf.getDouble(section + ".temp-high",70);
//					tempWarnLimit = iniConf.getDouble(section + ".temp-warn-limit",3);
//					fanRPMLow = iniConf.getDouble(section + ".fanRPM-low",2000);
//					fanRPMHigh = iniConf.getDouble(section + ".fanRPM-high",5000);
//					diskSafetyLimit = iniConf.getInt(section + ".diskSafetyLimit",80);
//					logger.debug("diskSafetyLimit after read from file "+diskSafetyLimit);
//				}
//				else if (section.equalsIgnoreCase("PowerSupply"))
//				{
//					ipAddress = iniConf.getString(section +".ip-address","192.168.1.100");
//					port = iniConf.getInt(section +".port",9978);
//				}
//				else if (section.equalsIgnoreCase("TriggerLed"))
//				{
//					triggerDuration = iniConf.getInt(section +".duration",0);
//					logger.debug("TRIGGER: duration " +triggerDuration);
//				}
//				else if (section.equalsIgnoreCase("RelayInfo"))
//				{
//					List<Object> lstRelayName = null;
//					mapLedToRelay = new HashMap<LedName, List<Object>>();
//					lstRelayName = iniConf.getList(section +"."+LedName.ONLINE.name());
//					logger.debug("\t\t Led Name "+LedName.ONLINE+" \t lst of relay names "+lstRelayName);
//					if (lstRelayName != null && !lstRelayName.isEmpty())
//					{
//						mapLedToRelay.put(LedName.ONLINE, lstRelayName);
//					}
//					lstRelayName = iniConf.getList(section +"."+LedName.TRIGGER.name());
//					if (lstRelayName != null && !lstRelayName.isEmpty())
//					{
//						mapLedToRelay.put(LedName.TRIGGER, lstRelayName);
//					}
//					lstRelayName = iniConf.getList(section +"."+LedName.CLOCK_SYNC.name());
//					if (lstRelayName != null && !lstRelayName.isEmpty())
//					{
//						mapLedToRelay.put(LedName.CLOCK_SYNC, lstRelayName);
//					}
//					lstRelayName = iniConf.getList(section +"."+LedName.COMMUNICATION.name());
//					if (lstRelayName != null && !lstRelayName.isEmpty())
//					{
//						mapLedToRelay.put(LedName.COMMUNICATION, lstRelayName);
//					}
//					lstRelayName = iniConf.getList(section +"."+LedName.POWER.name());
//					if (lstRelayName != null && !lstRelayName.isEmpty())
//					{
//						mapLedToRelay.put(LedName.POWER, lstRelayName);
//					}
//					lstRelayName = iniConf.getList(section +"."+LedName.DISK.name());
//					if (lstRelayName != null && !lstRelayName.isEmpty())
//					{
//						mapLedToRelay.put(LedName.DISK, lstRelayName);
//					}
//					lstRelayName = iniConf.getList(section +"."+LedName.TEMPERATURE.name());
//					if (lstRelayName != null && !lstRelayName.isEmpty())
//					{
//						mapLedToRelay.put(LedName.TEMPERATURE, lstRelayName);
//					}
//					lstRelayName = iniConf.getList(section +"."+LedName.FAN.name());
//					if (lstRelayName != null && !lstRelayName.isEmpty())
//					{
//						mapLedToRelay.put(LedName.FAN, lstRelayName);
//					}
//				}
//			}

			// END: 10-Jan-2017 -- commented code and implemented new logic
			
		}
		catch (Exception e) {
			logger.error("Error in reading configuration. Using local values.",e);
		}
	}
	public static void setTriggerLedWithRelay(LedState ledState)
	{
		irigDateToMysqlDSate = M9kStationUtil.getDateFormat("yyyy-MM-dd HH:mm:ss").format(M9kStationUtil.getIRIG()/1000);
		ExecutorService  executor = Executors.newFixedThreadPool(1);

		logger.debug("\n\n\t\tTRIGGER: Set trigger to green was called "+ (++iCount)+" \n\n" );
//		M9kStationCommandClient commandClient;
		String commandToSend;
		LedName ledName = LedName.TRIGGER;
//        switch(ledState)
//        {
//		case OFF:
//			LED_VALUE &= ~(1 << ledName.ordinal());
//			LED_VALUE &= ~(1 << (ledName.ordinal() + 8));
//			break;
//		case RED:
//			LED_VALUE &= ~(1 << ledName.ordinal());
//			LED_VALUE |= (1 << (ledName.ordinal() + 8));
//			break;
//		case GREEN:
//			if (getTriggerDuration() > 0 && !isAlreadyTriggering())
//			{
//				triggerAlarmLogsDTO = new AlarmLogsDTO();
//				triggerAlarmLogsDTO.setLedName(LedName.TRIGGER.name());
//				triggerAlarmLogsDTO.setLedStatus(ledState.name());
//				LED_VALUE |= (1 << ledName.ordinal());
//				LED_VALUE &= ~(1 << (ledName.ordinal() + 8));
//				logger.debug("Trigger duration "+getTriggerDuration());
//				logger.debug("Timer "+timer);
//			}
//			break;
//		case YELLOW:
//			LED_VALUE |= (1 << ledName.ordinal());
//			LED_VALUE |= (1 << (ledName.ordinal() + 8));
//			break;
//        }

		if (getTriggerDuration() > 0 && !isAlreadyTriggering())
		{
			triggerAlarmLogsDTO = new AlarmLogsDTO();
			triggerAlarmLogsDTO.setLedName(LedName.TRIGGER.name());
			triggerAlarmLogsDTO.setLedStatus(ledState.name());
//			LED_VALUE |= (1 << ledName.ordinal());
//			LED_VALUE &= ~(1 << (ledName.ordinal() + 8));
			logger.debug("Trigger duration "+getTriggerDuration());
			logger.debug("Timer "+timer);
		}

		RelayName relayName = M9kStationXMLUtil.getRelayMappings().get(ledName);
		logger.debug("TRIGGER: Already triggering? "+isAlreadyTriggering());
		StringBuffer relaysInfo = new StringBuffer();
		if (!alreadyTriggering) // If the timer is active do nothing, set values only when timer is null
		{
			
			if (relayName!= null && ledState != LedState.OFF && ledState != LedState.YELLOW)
			{
					relaysInfo.append(relayName.name());
					switch (ledState) {
					case WARBLE:
						RELAY_VALUE &= ~(1 << (relayName.ordinal()));
						break;
	//				case RED:
	//					RELAY_VALUE |= (1 << (relays.ordinal()));
	//					break;
					default:
						break;
					}
//				triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("System triggered. Relay(s) "+relaysInfo+" set to alarm"));
				if (!disableAlarm && !isTestTrigger())
				{
					triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("System triggered."));
					commandToSend="set,led="+(LedName.TRIGGER.ordinal()+1)+",ledAction="+ledState.name()+",relays="+RELAY_VALUE;
					disableAlarm = false;
					testTrigger = false;
				}
				else
				{
					triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("Test Trigger using Trigger Now "));

					commandToSend="set,led="+(LedName.TRIGGER.ordinal()+1)+",ledAction="+ledState.name();
				}
//				commandClient = new M9kStationCommandClient(ipAddress, port, "set,led="+(LedName.TRIGGER.ordinal()+1)+",ledAction="+ledState.name()+",relays="+RELAY_VALUE);
	
			}
			else
			{
				relaysInfo.append("No relays set or reset");
				triggerAlarmLogsDTO.setLedStatus(ledState.name());
				if(ledState == LedState.YELLOW)
				{
					triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("Warn: There might be a problem in trigger data"+ relaysInfo));
				}
				else
				{
					triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer(relaysInfo));
					
				}
				commandToSend = "set,led="+(LedName.TRIGGER.ordinal()+1)+",ledAction="+ledState.name();
//				commandClient = new M9kStationCommandClient(ipAddress, port, "set,led="+(LedName.TRIGGER.ordinal()+1)+",ledAction="+ledState.name());			
			}
			
			logger.debug("Led value to be set "+LED_VALUE+" for led "+ledName);
	        try {
					logger.debug("About to schedule timer ");

				alreadyTriggering = true;
				if (M9kStationUtil.isPowerSupplyAvailable())
				{
					if (isPowerSupplyErrorLogged)
					{
						reinitializeAlarmModule();
					}
					if ( getTriggerDuration() > 0)
					{
						scheduleTriggerLedTimer();
					}
	//				commandClient.sendAndReceive();
					executor.execute(new M9kCommandThreads(ipAddress, port, commandToSend));
					// START: 24-Jan-2023 - Changed from Shutdown to ShutdownNow as LED was still warbling even after turning it off. Thanks to Todd Sampson for fixing it.
					executor.shutdownNow();
				}
				else
				{
					alreadyTriggering = false;
				}
			} catch (Exception e) {
				// Warbling will not return any value,  hence there will be socket timeout exception, which we can ignore
				if (!(e.getCause() instanceof SocketTimeoutException))
				{
					logger.error("Error in connecting to power supply ",e);
				}
				else
				{
					logger.debug("It's wobbling!");
				}
			}
	        finally
	        {
	        	disableAlarm = false;
	        	testTrigger = false;
	        }
	        try
	        {
				triggerAlarmLogsDTO.setRelays(relaysInfo.toString());
				if (mySqlAlarmLogsDAO == null)
				{
					mySqlAlarmLogsDAO = new MySqlAlarmLogsDAO();
				}
				triggerAlarmLogsDTO.setAlarmTime(irigDateToMysqlDSate);
				mySqlAlarmLogsDAO.insertAlarmDetails(triggerAlarmLogsDTO);

	        }
	        catch (Exception e) {
	        	logger.error("Error in inserting into alarms log "+triggerAlarmLogsDTO,e);
	        }

		}
		
	}
	
	// Alarm Module is back after it was unavaialable
	private static void reinitializeAlarmModule() {
		isPowerSupplyErrorLogged = false;
		logger.info("Alarm Module is reachable now");
		M9kStationUtil.insertUserActionToAlarmsLog("Alarm Module is reachable now", LedState.GREEN.name());
		M9kStationUtil.setWatchDogEnabled(M9kStationUtil.isWatchDogEnabled());
	}
	private synchronized static void scheduleTriggerLedTimer() {
		timer = new Timer();
		logger.debug("TRIGGER: About to schedule timer for duration "+getTriggerDuration());
	    timer.schedule(new TimerTask(){
		    public void run() {
			      logger.debug("Time's up! Trigger is reset and timer cancelled");
			      resetTriggerStatus();
			      if (timer != null)
			      {
			    	  timer.cancel(); 
			    	  timer = null;
			      }
			      alreadyTriggering = false;
			    }
	    }, getTriggerDuration() * 1000);
		
	}
	public static void resetTriggerStatus()
	{
		M9kStationCommandClient commandClient;
		RelayName relayName = M9kStationXMLUtil.getRelayMappings().get(LedName.TRIGGER);
		irigDateToMysqlDSate = M9kStationUtil.getDateFormat("yyyy-MM-dd HH:mm:ss").format(M9kStationUtil.getIRIG()/1000);

//		// Set LED status to OFF
//		LED_VALUE &= ~(1 << LedName.TRIGGER.ordinal());
//		LED_VALUE &= ~(1 << (LedName.TRIGGER.ordinal() + 8));
		StringBuffer relaysInfo = new StringBuffer();
		if (relayName!= null )
		{
			relaysInfo.append(relayName.name());
			
			if (triggerAlarmLogsDTO != null){
//				triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("Trigger time complete. Relay(s) "+relaysInfo+" reset"));
				triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("Trigger time complete."));
			}
			if (!isRelaySharedWithOtherLEDs(LedName.TRIGGER, relayName))
			{
				RELAY_VALUE |= (1 << (relayName.ordinal()));
				logger.debug("Resetting trigger led and relay together");
			}
			else
			{
				// Some LED is set to RED that needs this relay to be set
				RELAY_VALUE &= ~(1 << (relayName.ordinal()));
			}
			commandClient = new M9kStationCommandClient(ipAddress, port, "set,led="+(LedName.TRIGGER.ordinal()+1)+",ledAction="+LedState.OFF.name()+",relays="+RELAY_VALUE);
		}
		else
		{
			logger.debug("Resetting trigger led alone ");
			relaysInfo.append("No relays reset");
			commandClient = new M9kStationCommandClient(ipAddress, port, "set,led="+(LedName.TRIGGER.ordinal()+1)+",ledAction="+LedState.OFF.name());			
		}
		logger.debug("\n\n\tTRIGGER: Reset trigger to OFF "+iCount +"\n\n");
        logger.debug("Trigger Led value to be reset "+LED_VALUE+" Relay value "+RELAY_VALUE);
        if (M9kStationUtil.isPowerSupplyAvailable())
		{
        	if (isPowerSupplyErrorLogged)
    		{
        		reinitializeAlarmModule();
    		}
	        try {
				commandClient.sendAndReceive();
			} catch (M9000Exception e) {
				logger.error("Error in resetting trigger led with value "+LED_VALUE,e);
			}
			catch (Exception e) {
				logger.error("Error in resetting trigger led with value "+LED_VALUE,e);
			}
		}
        try
        {
			if (triggerAlarmLogsDTO != null){
				triggerAlarmLogsDTO.setLedStatus(LedState.OFF.name());
				triggerAlarmLogsDTO.setRelays(relaysInfo.toString());
				triggerAlarmLogsDTO.setAlarmTime(irigDateToMysqlDSate);
				mySqlAlarmLogsDAO.insertAlarmDetails(triggerAlarmLogsDTO);
			}

        }
        catch (Exception e) {
			logger.error("Error in inserting trigger alarms into database "+LED_VALUE,e);
		} 
		finally
		{
			
			triggerAlarmLogsDTO = null;
		}
	
	}
	public static void setLedWithRelay(LedName ledName, LedState ledState, String reason)
	{
//		logger.debug("TRIGGER: LED_VALUE before setting new one "+LED_VALUE);
//		logger.debug("TRIGGER: RELAY_VALUE before setting new one "+RELAY_VALUE);
		M9kStationCommandClient commandClient;
//		switch (ledState) {
//		case OFF:
//			LED_VALUE &= ~(1 << ledName.ordinal()); // GREEN (OFF)
//			LED_VALUE &= ~(1 << (ledName.ordinal() + 8)); // RED (OFF)
//			break;
//		case RED:
//			LED_VALUE &= ~(1 << ledName.ordinal()); // GREEN (OFF)
//			LED_VALUE |= (1 << (ledName.ordinal() + 8)); // RED (ON)
//			break;
//		case GREEN:
//			LED_VALUE |= (1 << ledName.ordinal()); // GREEN (ON)
//			LED_VALUE &= ~(1 << (ledName.ordinal() + 8)); // RED (OFF)
//			break;
//		case YELLOW:
//			LED_VALUE |= (1 << ledName.ordinal());// GREEN (ON)
//			LED_VALUE |= (1 << (ledName.ordinal() + 8));// RED (ON)
//			break;
//		}
		RelayName relayName = M9kStationXMLUtil.getRelayMappings().get(ledName);
		logger.debug("RELAY_MAPPING: List of relays for ledName "+ledName+" led state "+ledState+" relayNames "+relayName+" RELAY_VALUE "+RELAY_VALUE);
		StringBuffer relaysInfo = new StringBuffer();
		StringBuffer alarmDesc = new StringBuffer(reason);
		long irigTime ;
		LedState currentLedState = getCurrentLedState(ledName);
		logger.debug("Current state of "+ledName+" is "+currentLedState+" to be compared with new state "+ledState);
		if (currentLedState == null || !currentLedState.equals(ledState))
		{
			irigTime = M9kStationUtil.getIRIG();
			irigDateToMysqlDSate = M9kStationUtil.getDateFormat("yyyy-MM-dd HH:mm:ss").format(irigTime/1000);
			logger.debug("IRIG-TEST: irigTime "+irigTime);
			
			if (relayName!= null && ledState != LedState.OFF && ledState != LedState.YELLOW)
			{
				if (ledState.equals(LedState.RED) || !isRelaySharedWithOtherLEDs(ledName, relayName))
				{
					relaysInfo.append(relayName.name());
					switch (ledState) {
					case RED:
						RELAY_VALUE &= ~(1 << (relayName.ordinal()));
						break;
					case GREEN:
						RELAY_VALUE |= (1 << (relayName.ordinal()));
						break;
					default:
						break;
					}
					logger.debug("TRIGGER: Relay value to be set for led "+relayName+" color "+ledState+" is "+RELAY_VALUE+" Reason "+reason+" ledName "+ledName);
					if (ledState.equals(LedState.RED))
					{
	//					alarmDesc.append(" Relay(s) "+relaysInfo+" set to alarm");
						logger.debug(" Relay(s) "+relaysInfo+" set to alarm");
					}
					else
					{
	//					alarmDesc.append(" Relay(s) "+relaysInfo+" reset");
						logger.debug(" Relay(s) "+relaysInfo+" reset");
					}
					logger.debug("Command to be sent "+("set,led="+(ledName.ordinal()+1)+",ledAction="+ledState.name()+",relays="+RELAY_VALUE));
					commandClient = new M9kStationCommandClient(ipAddress, port, "set,led="+(ledName.ordinal()+1)+",ledAction="+ledState.name()+",relays="+RELAY_VALUE);
				}
				else
				{
					relaysInfo.append("Relay "+relayName+" is already set or reset");
					commandClient = new M9kStationCommandClient(ipAddress, port, "set,led="+(ledName.ordinal()+1)+",ledAction="+ledState.name());			
				}
	
			}
			else
			{
				relaysInfo.append("No relays set or reset");
				commandClient = new M9kStationCommandClient(ipAddress, port, "set,led="+(ledName.ordinal()+1)+",ledAction="+ledState.name());			
			}
			if (M9kStationUtil.isPowerSupplyAvailable())
			{
				if (isPowerSupplyErrorLogged)
				{
					reinitializeAlarmModule();
				}
	
		        logger.debug("Led value to be set "+LED_VALUE+" for led "+ledName);
		        try {
					commandClient.sendAndReceive();
				} catch (Exception e) {
	
					logger.error("Error in setting led "+ledName,e);
				}
			}
			try
			{
				logger.debug("About to insert into the database "+alarmDesc);
				AlarmLogsDTO alarmLogsDTO = new AlarmLogsDTO();
				alarmLogsDTO.setAlarmDescription(alarmDesc);
				alarmLogsDTO.setLedName(ledName.name());
				alarmLogsDTO.setLedStatus(ledState.name());
				alarmLogsDTO.setRelays(relaysInfo.toString());
				alarmLogsDTO.setAlarmTime(irigDateToMysqlDSate);
				mySqlAlarmLogsDAO.insertAlarmDetails(alarmLogsDTO);
				alarmLogsDTO = null;
				logger.debug("Done inserting into the database");
			} catch (Exception e) {
				logger.error("Error in inserting into alarms logs "+ledName,e);
			}

		}

	}
	
	/**
	 * Checks with whether relay is shared with other LEDs and needs to be set for RED
	 * @param srcLedName 
	 * @param relayName
	 * @return
	 */
	private static boolean isRelaySharedWithOtherLEDs(LedName srcLedName, RelayName relayName) {
		boolean booIsrelayShared = false;
		logger.debug("Check for Ledname "+srcLedName+" and relay "+relayName);
		List<LedName> lstLedNamesForRelay = M9kStationXMLUtil.getMapRelayToLeds().get(relayName);
		LedName ledName;
		for (Iterator<LedName> iterator = lstLedNamesForRelay.iterator(); iterator.hasNext();) {
			ledName = iterator.next();
			if (!ledName.equals(srcLedName) && getCurrentLedState(ledName) != null && getCurrentLedState(ledName).equals(LedState.RED))
			{
				logger.info(ledName.name() + " shares this relay "+relayName+" With this LED "+srcLedName);
				booIsrelayShared = true;
				break;
			}
		}
		logger.debug("IS relay shared?? "+booIsrelayShared);
		return booIsrelayShared;
	}
	public static void resetLed()
	{
		logger.info("Resetting all LEDS");
		M9kStationCommandClient commandClient = new M9kStationCommandClient(ipAddress, port, "set,leds=0");
        try {
    		if (M9kStationUtil.isPowerSupplyAvailable())
    		{
    			commandClient.sendAndReceive();
    		}
			if (M9kStationUtil.getMapDfrMonitorStatus().get(ipAddress) != null && !M9kStationUtil.getMapDfrMonitorStatus().get(ipAddress))
	    	 {
				logger.error("Alarm module is reachable now");
				if (isPowerSupplyErrorLogged)
				{
					reinitializeAlarmModule();
				}
	    	 }
				M9kStationUtil.getMapDfrMonitorStatus().put(ipAddress, true);
			LED_VALUE = 0;
		} 
		catch (Exception e) {
			if (M9kStationUtil.getMapDfrMonitorStatus().get(ipAddress) == null || M9kStationUtil.getMapDfrMonitorStatus().get(ipAddress))
	    	 {
				logger.error("Error in resetting led. Alarm Module is not reachable ",e);
	    	 }
			M9kStationUtil.getMapDfrMonitorStatus().put(ipAddress, false);
		}
	}

	public static void resetRelay()
	{
		logger.info("Resetting all RELAYS");
		RELAY_VALUE = 255;
		M9kStationCommandClient commandClient = new M9kStationCommandClient(ipAddress, port, "set,relays="+RELAY_VALUE);
        try {
    		if (M9kStationUtil.isPowerSupplyAvailable())
    		{
    			commandClient.sendAndReceive();
    		}
			if (M9kStationUtil.getMapDfrMonitorStatus().get(ipAddress) != null && !M9kStationUtil.getMapDfrMonitorStatus().get(ipAddress))
	    	 {
				logger.error("Alarm module is reachable now");
				if (isPowerSupplyErrorLogged)
				{
					reinitializeAlarmModule();
				}
	    	 }
				M9kStationUtil.getMapDfrMonitorStatus().put(ipAddress, true);
		} 
		catch (Exception e) {
			if (M9kStationUtil.getMapDfrMonitorStatus().get(ipAddress) == null || M9kStationUtil.getMapDfrMonitorStatus().get(ipAddress))
	    	 {
				logger.error("Error in resetting led. Alarm Module is not reachable ",e);
	    	 }
			M9kStationUtil.getMapDfrMonitorStatus().put(ipAddress, false);
		}
	}
	public void schedulePolling()
	{
		  final Runnable taskPerformer = new Runnable() {
              public void run() {
            	  if (!isCalibrating()) // Don't update the LEDs during calibration/Verification process
            	  {
					  if (M9kStationUtil.isPowerSupplyAvailable())
						{
							if (isPowerSupplyErrorLogged)
							{
								reinitializeAlarmModule();
							}
		            		  updateLeds();
						}
					  else
					  {
						  if (!isPowerSupplyErrorLogged)
							{
							  isPowerSupplyErrorLogged = true;
								logger.info("Alarm Module is either not available or not reachable");
								M9kStationUtil.insertUserActionToAlarmsLog("Alarm Module is either not available or not reachable", LedState.RED.name());
							}
					  }
            	  }
              }
          };
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, getRunFrequency(), TimeUnit.SECONDS);
          threadHandlerExecutor.execute(new M9kThreadExceptionHandler(scheduledTask));
	}
	
	public void updateLeds() {
		substationHlth = m9kDFRHealthStatusProcessor.getHealthStatus();
		notifyReport = new StringBuffer();
		msgSeverity = M9kSeverityLevels.INFO;
//		logger.debug("Substation health xml "+substationHlth.xmlText(xmlOptions));
		logger.debug("Substation health status "+substationHlth.getStatus());
		if (substationHlth.getStatus().equalsIgnoreCase(M9kStationConstants.INACTIVE_STATUS))
		{
			setLedWithRelay(LedName.ONLINE, LedState.RED,"All DFRs are down");
			setLedWithRelay(LedName.COMMUNICATION, LedState.OFF, "All DFRs are down");
			setLedWithRelay(LedName.CLOCK_SYNC, LedState.OFF, "Warning: communication down");
			setLedWithRelay(LedName.POWER, LedState.OFF, "Warning: communication down");
			setLedWithRelay(LedName.DISK, LedState.OFF, "Warning: communication down");
			setLedWithRelay(LedName.TEMPERATURE, LedState.OFF, "Warning: communication down");
			setLedWithRelay(LedName.DISTURBANCE, LedState.OFF, "Warning: communication down");
			booStationActive = false;
			if (!isAlreadyReported(LedName.ONLINE, LedState.RED))
			{
				notifyReport.append("All chassis are down. The Station is offline.");
				msgSeverity = M9kSeverityLevels.FATAL; 
			}
		}
		else
		{
			booStationActive = true;
			processHealthStatus();
		}
		if (notifyReport.length() > 0)
		{
			// Notify Web Master
			try {
				M9kStationUtil.sendNotification(msgSeverity.name(), notifyReport.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("Error in notifying "+notifyReport.toString()+ " to Master station.",e);
			}
			
		}
	}

	private void processHealthStatus() {
		DFR[] dfrs = substationHlth.getDFRArray();
		int diskUsagePercentage;
		boolean booPower = true;
		boolean booTemp = true;
//		boolean booFan = true; // Disabled as it is going to be fanless
		boolean booClockSync = true;
		StringBuffer powerDesc = new StringBuffer();
		StringBuffer tempDesc = new StringBuffer();
//		StringBuffer fanDesc = new StringBuffer();
		StringBuffer clockSyncDesc = new StringBuffer();
		double val;
		int diskWarningLimit = 0;
		Attribute[] healthAttr;
		boolean booCommunicationUp = isCommunicationUp(dfrs);
		SimpleDateFormat sdfDfrTime = new SimpleDateFormat("MMM d yyyy - HH:mm:ss");
		Date minDateDfrTime = null;
		Date maxDateDfrTime = null;
		Date currDateDfrTime = null;
		StringBuffer strbuffDFRTimes = null;
//		logger.debug("\n\n\t\t"+"New Cycle of polling relay value "+RELAY_VALUE+" led value "+LED_VALUE+" \n\n");
//		setLedWithRelay(LedName.ONLINE, LedState.GREEN, "Station Master Started");
		if (!M9kStationUtil.isDiskspaceAvailableToContinueRecording())
		{
			if (!isAlreadyReported(LedName.ONLINE, LedState.RED))
			{
				notifyReport.append("Recording is stopped as disk space is critical. No new faults will be recorded");
				if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.FATAL.ordinal())
				{
					msgSeverity = M9kSeverityLevels.FATAL;
				}
			}
			setLedWithRelay(LedName.ONLINE, LedState.RED,"Disk space is critical. Stopped recording until disk space is cleared. Current disk space "+M9kStationUtil.getPercentageOfDiskUsed());
			setLedWithRelay(LedName.COMMUNICATION, LedState.OFF, communicationDesc);
			if (isAlreadyReported(LedName.CLOCK_SYNC, LedState.GREEN))
			{
				setLedWithRelay(LedName.CLOCK_SYNC, LedState.OFF, "Warning: Recording is stopped as disk space is critical");
			}
			if (isAlreadyReported(LedName.POWER, LedState.GREEN))
			{
				setLedWithRelay(LedName.POWER, LedState.OFF, "Warning: Recording is stopped as disk space is critical");
			}
			if (isAlreadyReported(LedName.DISK, LedState.GREEN))
			{
				setLedWithRelay(LedName.DISK, LedState.OFF, "Warning: Recording is stopped as disk space is critical");
			}
			if (isAlreadyReported(LedName.TEMPERATURE, LedState.GREEN))
			{
				setLedWithRelay(LedName.TEMPERATURE, LedState.OFF, "Warning: Recording is stopped as disk space is critical");
			}
			if (isAlreadyReported(LedName.DISTURBANCE, LedState.GREEN))
			{
				setLedWithRelay(LedName.DISTURBANCE, LedState.OFF, "Warning: Recording is stopped as disk space is critical");
			}
			
			try {
				logger.info("About to perform disk clean up as the disk is already full and recording has stopped");
				M9kStationUtil.performDiskCleanup();
				logger.info("Done.");
			} catch (M9000Exception e) {
				logger.error("Disk is almost full. Cleanup process failed due to these errors.",e);
			}

			return;
		}
		if(!booCommunicationUp)
		{
			if(!booStationActive)
			{
				if (!isAlreadyReported(LedName.ONLINE, LedState.RED))
				{
					notifyReport.append("All DFRs are down. The Station is offline.");
					if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.FATAL.ordinal())
					{
						msgSeverity = M9kSeverityLevels.FATAL;
					}
				}
				setLedWithRelay(LedName.ONLINE, LedState.RED,"All DFRs are down");
				setLedWithRelay(LedName.COMMUNICATION, LedState.OFF, communicationDesc);
			}
			else
			{
				M9kStationUtil.setAtleastOneDFRsUp(true);
				if (!isAlreadyReported(LedName.COMMUNICATION, LedState.RED) && !isAlreadyReported(LedName.ONLINE, LedState.YELLOW))
				{
					notifyReport.append(communicationDesc);
					if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.ERROR.ordinal())
					{
						msgSeverity = M9kSeverityLevels.ERROR;
					}
				}
				setLedWithRelay(LedName.COMMUNICATION, LedState.RED, communicationDesc);
				setLedWithRelay(LedName.ONLINE, LedState.YELLOW, "Warning: One of the chassis is unreachable.");
			}
			if (isAlreadyReported(LedName.CLOCK_SYNC, LedState.GREEN))
			{
				setLedWithRelay(LedName.CLOCK_SYNC, LedState.OFF, "Warning: communication down");
			}
			if (isAlreadyReported(LedName.POWER, LedState.GREEN))
			{
				setLedWithRelay(LedName.POWER, LedState.OFF, "Warning: communication down");
			}
			if (isAlreadyReported(LedName.DISK, LedState.GREEN))
			{
				setLedWithRelay(LedName.DISK, LedState.OFF, "Warning: communication down");
			}
			if (isAlreadyReported(LedName.TEMPERATURE, LedState.GREEN))
			{
				setLedWithRelay(LedName.TEMPERATURE, LedState.OFF, "Warning: communication down");
			}
			if (isAlreadyReported(LedName.DISTURBANCE, LedState.GREEN))
			{
				setLedWithRelay(LedName.DISTURBANCE, LedState.OFF, "Warning: communication down");
			}

		}
		else
		{
			if (!isAlreadyReported(LedName.COMMUNICATION, LedState.GREEN))
			{
				notifyReport.append(communicationDesc);
				notifyReport.append(M9kStationConstants.NEWLINE);
				msgSeverity = M9kSeverityLevels.INFO;
			}
			setLedWithRelay(LedName.COMMUNICATION, LedState.GREEN, communicationDesc);
			setLedWithRelay(LedName.ONLINE, LedState.GREEN,"Station Master Started");
		}
		diskUsagePercentage = M9kStationUtil.getPercentageOfDiskUsed();
		diskWarningLimit = (int)Math.floor((95.0/100)*getDiskSafetyLimit());
		logger.debug("diskUsagePercentage "+diskUsagePercentage+" safety limit "+getDiskSafetyLimit());
		// Check For Disk
		if ( diskUsagePercentage > diskWarningLimit)
		{
			if (diskUsagePercentage < getDiskSafetyLimit())
			{
				if (diskUsageAlmostFull) // Check before logging in information for user
				{
//					logger.info("Disk cleanup process stopped as the usage reached non-critical limit");
					try {
						logger.info("Continuing disk clean up after being full as the disk space is still close to critical limit. Current status "+diskUsagePercentage+" Safety limit set "+getDiskSafetyLimit());
						M9kStationUtil.performDiskCleanup();
						logger.info("Done.");
					} catch (M9000Exception e) {
						logger.error("Disk is reaching critical limits. Cleanup process failed due to these errors.",e);
					}
				}
				if (!isAlreadyReported(LedName.DISK, LedState.YELLOW))
				{
					notifyReport.append("Warning: Disk is reaching close to the safety limit. Current status "+diskUsagePercentage+" Safety limit set "+getDiskSafetyLimit());
					notifyReport.append(M9kStationConstants.NEWLINE);
					if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.WARNING.ordinal())
					{
						msgSeverity = M9kSeverityLevels.WARNING;
					}
				}
				setLedWithRelay(LedName.DISK, LedState.YELLOW, "Warning: Disk is reaching close to the safety limit. Current status "+diskUsagePercentage+" Safety limit set "+getDiskSafetyLimit());
			}
			else
			{
				if (!isAlreadyReported(LedName.DISK, LedState.RED))
				{
					notifyReport.append("CRITICAL: Disk usage exceeded its safety limit. Current status "+diskUsagePercentage+" Safety limit set "+getDiskSafetyLimit());
					notifyReport.append(M9kStationConstants.NEWLINE);
					if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.WARNING.ordinal())
					{
						msgSeverity = M9kSeverityLevels.WARNING;
					}
				}
				setLedWithRelay(LedName.DISK, LedState.RED, "CRITICAL: Disk usage exceeded its safety limit. Current status "+diskUsagePercentage+" Safety limit set "+getDiskSafetyLimit());
				// START: 21-Dec-2020 - Start cleanup if disk is more than 95% full
				if (diskUsagePercentage >= M9kStationUtil.getDiskCleanupLimit())
				{
					logger.info("Disk is almost full with usage percentage of "+diskUsagePercentage+"%. Disk cleanup process will kick in installments until it reaches safe limit of "+getDiskSafetyLimit()+"%");
					diskUsageAlmostFull = true;
				}
				if (diskUsageAlmostFull)
				{
					try {
						logger.info("About to perform disk clean up");
						M9kStationUtil.performDiskCleanup();
						logger.info("Done.");
					} catch (M9000Exception e) {
						logger.error("Disk is almost full. Cleanup process failed due to these errors.",e);
					}
				}
			}
		}
		else 
		{
			diskUsageAlmostFull = false;
			if (booCommunicationUp)
			{
				if (!isAlreadyReported(LedName.DISK, LedState.GREEN))
				{
					notifyReport.append("Disk space ok");
					notifyReport.append(M9kStationConstants.NEWLINE);
					if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.INFO.ordinal())
					{
						msgSeverity = M9kSeverityLevels.INFO;
					}
				}
				setLedWithRelay(LedName.DISK, LedState.GREEN,"Disk space ok");
			}
		}
		
		for (int i = 0; i < dfrs.length; i++) {
			healthAttr = dfrs[i].getAttributeArray();
			for (int j = 0; j < healthAttr.length; j++) {
				
				// Power
//				if (booPower)
//				{
					if (healthAttr[j].getName().equalsIgnoreCase("V5"))
					{
						val = Double.parseDouble(healthAttr[j].getValue());
						if (val < getV5Low() || val > getV5High())
						{
							booPower = false;
							powerDesc.append(dfrs[i].getName()+". V5 was out of limit with value "+val);
							powerDesc.append(M9kStationConstants.NEWLINE);
						}
					}
					else if (healthAttr[j].getName().equalsIgnoreCase("V3_3"))
					{
						val = Double.parseDouble(healthAttr[j].getValue());
						if (val < getV3_3Low() || val > getV3_3High())
						{
							booPower = false;
							powerDesc.append(dfrs[i].getName()+". V3.3 was out of limit with value "+val);
							powerDesc.append(M9kStationConstants.NEWLINE);
						}
					}
					else if (healthAttr[j].getName().equalsIgnoreCase("V2_5"))
					{
						val = Double.parseDouble(healthAttr[j].getValue());
						if (val < getV2_5Low() || val > getV2_5High())
						{
							booPower = false;
							powerDesc.append(dfrs[i].getName()+". V2.5 was out of limit with value "+val);
							powerDesc.append(M9kStationConstants.NEWLINE);
						}
					}
					else if (healthAttr[j].getName().equalsIgnoreCase("V1_8"))
					{
						val = Double.parseDouble(healthAttr[j].getValue());
						if (val < getV1_8Low() || val > getV1_8High())
						{
							booPower = false;
							powerDesc.append(dfrs[i].getName()+". V1.8 was out of limit with value "+val);
							powerDesc.append(M9kStationConstants.NEWLINE);
						}
					}
					else if (healthAttr[j].getName().equalsIgnoreCase("V1_5"))
					{
						val = Double.parseDouble(healthAttr[j].getValue());
						if (val < getV1_5Low() || val > getV1_5High())
						{
							booPower = false;
							powerDesc.append(dfrs[i].getName()+". V1.5 was out of limit with value "+val);
							powerDesc.append(M9kStationConstants.NEWLINE);
						}
					}
					else if (healthAttr[j].getName().equalsIgnoreCase("V1_2"))
					{
						val = Double.parseDouble(healthAttr[j].getValue());
						if (val < getV1_2Low() || val > getV1_2High())
						{
							booPower = false;
							powerDesc.append(dfrs[i].getName()+". V1.2 was out of limit with value "+val);
							powerDesc.append(M9kStationConstants.NEWLINE);
						}
					}

//				}
				// Wetting voltage -- Should be combined with Power in the future
//				if (booWettingVoltage)
//				{
				 else if (healthAttr[j].getName().equalsIgnoreCase("WettingVoltage"))
					{
						if (healthAttr[j].getValue().equalsIgnoreCase("Not Present"))
						{
							booPower = false;
							powerDesc.append(dfrs[i].getName()+". Wetting Voltage not present");
							powerDesc.append(M9kStationConstants.NEWLINE);
						}
					}
					
//				}
				
				// Temperature
//				if (booTemp)
//				{
				 else if (healthAttr[j].getName().equalsIgnoreCase("Temp"))
					{
						temperatureVal = Double.parseDouble(healthAttr[j].getValue());
						// START Modified to include warning level for tempearture 11-Dec-2014
						// 14-July-2021 - Included lower limit for warning
						//if (val < getTempLow() || val > getTempHigh())
//						if (temperatureVal < getTempLow() || (temperatureVal > getTempHigh()-getTempWarnLimit()))
						if (temperatureVal < (getTempLow()+getTempWarnLimit()) || temperatureVal > (getTempHigh()-getTempWarnLimit()))
						{
							booTemp = false;
//								tempDesc.append("Source Chassis: "+dfrs[i].getName()+". Temperature is out of limits with value "+temperatureVal);
							if (temperatureVal > (getTempHigh()-getTempWarnLimit()) && temperatureVal < getTempHigh()
									|| (temperatureVal < (getTempLow()+getTempWarnLimit()) && temperatureVal > getTempLow()))
							{
								tempDesc.append(dfrs[i].getName()+". Temperature is about to reach critical level with value "+temperatureVal);
							}
							else
							{
								tempDesc.append(dfrs[i].getName()+". Temperature is out of limits with value "+temperatureVal);
							}
							tempDesc.append(M9kStationConstants.NEWLINE);
						}
						// END 11-Dec-2014
					}
//				}
				// Fan RPM Disabled as it is going to be fanless
//					if (booFan)
//					{
//						logger.debug("Name: "+healthAttr[j].getName());
//						logger.debug("Value: "+healthAttr[j].getValue());
//
//						if (healthAttr[j].getName().equalsIgnoreCase("FanRpm"))
//						{
//							val = Double.parseDouble(healthAttr[j].getValue());
//							logger.debug("Fan value "+val+" fan low limit "+getFanRPMLow()+" fan high limit "+getFanRPMHigh());
//							if (val < getFanRPMLow() || val > getFanRPMHigh())
//							{
//								booFan = false;
//								fanDesc.append("Source Chassis: "+dfrs[i].getName()+". Fan RPM is out of limits with val "+val);
//							}
//						}
//					}
				// Clock sync 
//				if (booClockSync)
//				{
					else if (healthAttr[j].getName().equalsIgnoreCase("Locked"))
					{
						val = Double.parseDouble(healthAttr[j].getValue());
						if (val == 0.0)
						{
							booClockSync = false;
							clockSyncDesc.append(dfrs[i].getName()+". Clock is not synchronized.");
							clockSyncDesc.append(M9kStationConstants.NEWLINE);
						}
					}
//				}
					// START: 06-Feb-2018 - To verify dfrtime on all chassis, if not set clock sync alarm
					
					else if (healthAttr[j].getName().equalsIgnoreCase("DfrTime"))
					{
						try
						{
							currDateDfrTime = sdfDfrTime.parse(healthAttr[j].getValue());
							logger.debug("DFRTIME:Current Dfrtime date "+currDateDfrTime+ "Previous dfrTime "+minDateDfrTime);
							if (strbuffDFRTimes == null)
							{
								strbuffDFRTimes = new StringBuffer();
							}
							strbuffDFRTimes.append(dfrs[i].getName()+"\t:"+currDateDfrTime.toString());
							strbuffDFRTimes.append(M9kStationConstants.NEWLINE);
							if (minDateDfrTime == null || (minDateDfrTime.compareTo(currDateDfrTime) > 0 ))
							{
								minDateDfrTime = currDateDfrTime;
								logger.debug("Minimum dfrtime set as "+minDateDfrTime);
							}
								
							if (maxDateDfrTime == null || (maxDateDfrTime.compareTo(currDateDfrTime) < 0 ))
							{
								maxDateDfrTime = currDateDfrTime;
								logger.debug("Maximum dfrtime set as "+maxDateDfrTime);
							}
						}
						catch (Exception e) {
							logger.error("DFRTIME:Exception occure while parsing DFRTIME as date",e);
						}
//						if (!minDateDfrTime.equals(currDateDfrTime))
//						{
//							logger.debug("DFRTIME:False. Dates are different");
//							booClockSync = false;
//							booDFRTimeSync = false;
//						}
//						else
//						{
//							logger.debug("DFRTIME:True. Dates are the same");
//						}
					}
					// END: 06-Feb-2018 

			}
////				if (!booPower && !booTemp && !booFan && !booClockSync) // Fan RPM Disabled as it is going to be fanless
//			if (!booPower && !booTemp && !booClockSync && !booWettingVoltage)
//			{
//				break;
//			}
		}
		
		// Power
		if (booPower)
		{
			if (booCommunicationUp)
			{
				if (!isAlreadyReported(LedName.POWER, LedState.GREEN))
				{
					notifyReport.append("Power and Wetting Voltage is good");
					notifyReport.append(M9kStationConstants.NEWLINE);
					if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.INFO.ordinal())
					{
						msgSeverity = M9kSeverityLevels.INFO;
					}
				}
				setLedWithRelay(LedName.POWER, LedState.GREEN, "Power is ok");
			}
		}
		else
		{
			if (!isAlreadyReported(LedName.POWER, LedState.RED))
			{
				notifyReport.append(powerDesc.toString());
				notifyReport.append(M9kStationConstants.NEWLINE);
				if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.ERROR.ordinal())
				{
					msgSeverity = M9kSeverityLevels.ERROR;
				}
			}
			setLedWithRelay(LedName.POWER, LedState.RED, powerDesc.toString());
		}

		// Temperature
		if (booTemp)
		{
			if (booCommunicationUp)
			{
				if (!isAlreadyReported(LedName.TEMPERATURE, LedState.GREEN))
				{
					notifyReport.append("Temperature is ok");
					notifyReport.append(M9kStationConstants.NEWLINE);
					if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.INFO.ordinal())
					{
						msgSeverity = M9kSeverityLevels.INFO;
					}
	
				}
				setLedWithRelay(LedName.TEMPERATURE, LedState.GREEN, "Temperature is ok");
			}
		}
		else
		{
			// START: 11-DEC-2014 Modified to show warning for temperatures before turning red
			if (temperatureVal > (getTempHigh()-getTempWarnLimit()) && temperatureVal < getTempHigh())
			{
				if (!isAlreadyReported(LedName.TEMPERATURE, LedState.YELLOW))
				{
					notifyReport.append(tempDesc.toString());
					notifyReport.append(M9kStationConstants.NEWLINE);
					if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.ERROR.ordinal())
					{
						msgSeverity = M9kSeverityLevels.ERROR;
					}

				}
				setLedWithRelay(LedName.TEMPERATURE, LedState.YELLOW, tempDesc.toString());
			}
			else
			{
				if (!isAlreadyReported(LedName.TEMPERATURE, LedState.RED))
				{
					notifyReport.append(tempDesc.toString());
					notifyReport.append(M9kStationConstants.NEWLINE);
					if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.ERROR.ordinal())
					{
						msgSeverity = M9kSeverityLevels.ERROR;
					}

				}
				setLedWithRelay(LedName.TEMPERATURE, LedState.RED, tempDesc.toString());

			}
			// END: 11-DEC-2014
		}
		// Fan Disabled as it is going to be fanless
//			logger.debug("booFan status "+booFan);
//			if (booFan)
//			{
//				setLedWithRelay(LedName.FAN, LedState.GREEN, "Fan is ok");
//			}
//			else
//			{
//				setLedWithRelay(LedName.FAN, LedState.RED, fanDesc.toString());
//			}
		// Clock Sync
		if ((maxDateDfrTime != null && minDateDfrTime !=null) && (Math.abs(maxDateDfrTime.getTime() - minDateDfrTime.getTime())/1000) > dfrTimeDiffLimit)
		{
			logger.debug("(Math.abs(maxDateDfrTime.getTime() - minDateDfrTime.getTime())/1000) "+(Math.abs(maxDateDfrTime.getTime() - minDateDfrTime.getTime())/1000)+" dfrTimeDfiffLimit "+dfrTimeDiffLimit);
			booClockSync = false;
			clockSyncDesc.append("DFR chassis times do not match. Please check the Health Monitor screen.");
			clockSyncDesc.append(M9kStationConstants.NEWLINE);
			clockSyncDesc.append(strbuffDFRTimes);
			clockSyncDesc.append(M9kStationConstants.NEWLINE);
		}
		if (booClockSync)
		{
			if (booCommunicationUp)
			{
				if (!isAlreadyReported(LedName.CLOCK_SYNC, LedState.GREEN))
				{
					notifyReport.append("Clock is synchronized");
					notifyReport.append(M9kStationConstants.NEWLINE);
					if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.INFO.ordinal())
					{
						msgSeverity = M9kSeverityLevels.INFO;
					}
	
				}
				setLedWithRelay(LedName.CLOCK_SYNC, LedState.GREEN, "Clock is synchronized");
			}
		}
		else
		{
			if (!isAlreadyReported(LedName.CLOCK_SYNC, LedState.RED))
			{
				notifyReport.append(clockSyncDesc.toString());
				notifyReport.append(M9kStationConstants.NEWLINE);
				if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.ERROR.ordinal())
				{
					msgSeverity = M9kSeverityLevels.ERROR;
				}
			}
			setLedWithRelay(LedName.CLOCK_SYNC, LedState.RED, clockSyncDesc.toString());
		}

		// Wetting Voltage check -- Combne the logic with power in the future
//		if (booWettingVoltage)
//		{
//			if (booCommunicationUp)
//			{
//				if (!currentWettVState.equals(LedState.GREEN))
//				{
//					booWettAlarmsEntry = true;
//					currentWettVState = LedState.GREEN;
//					wettingVoltageDesc.append("Wetting Voltage is good");
//					notifyReport.append(wettingVoltageDesc);
//					notifyReport.append(M9kStationConstants.NEWLINE);
//					if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.INFO.ordinal())
//					{
//						msgSeverity = M9kSeverityLevels.INFO;
//					}
//	
//				}
//				
//			}
//		}
//		else
//		{
//			if (!currentWettVState.equals(LedState.RED))
//			{
//				booWettAlarmsEntry = true;
//				currentWettVState = LedState.RED;
//				notifyReport.append(wettingVoltageDesc.toString());
//				notifyReport.append(M9kStationConstants.NEWLINE);
//				if (msgSeverity == null || msgSeverity.ordinal() > M9kSeverityLevels.ERROR.ordinal())
//				{
//					msgSeverity = M9kSeverityLevels.ERROR;
//				}
//			}
//		}
//		if (booWettAlarmsEntry)
//		{
//			try
//			{
//				AlarmLogsDTO alarmLogsDTO = new AlarmLogsDTO();
//				alarmLogsDTO.setAlarmDescription(wettingVoltageDesc);
//				alarmLogsDTO.setLedName(LedName.WETTING_VOLTAGE.name());
//				alarmLogsDTO.setLedStatus(currentWettVState.name());
//				alarmLogsDTO.setRelays("No relays are set");
//				alarmLogsDTO.setAlarmTime(irigDateToMysqlDSate);
//				mySqlAlarmLogsDAO.insertAlarmDetails(alarmLogsDTO);
//				alarmLogsDTO = null;
//				logger.debug("Done inserting into the database");
//			} catch (Exception e) {
//				logger.error("Error in inserting into alarms logs "+LedName.WETTING_VOLTAGE.name(),e);
//			}
//		}
	}
	
	/**
	 * Check Communication
	 * @param dfrs
	 * @return
	 */
	private boolean isCommunicationUp(DFR[] dfrs) {
		boolean booComm = true;
		booStationActive = true;
		int inactiveDfrindex = 0;
		int i = 0;
		int inactiveDfrCount = 0;
		communicationDesc = "";
		for (i = 0; i < dfrs.length; i++) {
			if (dfrs[i].getStatus().equalsIgnoreCase(M9kStationConstants.INACTIVE_STATUS))
			{
				booComm = false;
				inactiveDfrindex = i;
				inactiveDfrCount++;
				communicationDesc += dfrs[inactiveDfrindex].getName()+" is down. "+M9kStationConstants.NEWLINE;
			}
		}
		if (inactiveDfrCount == dfrs.length)
		{
			booStationActive = false;
			M9kStationUtil.setAtleastOneDFRsUp(false);
		}
		else
		{
			M9kStationUtil.setAtleastOneDFRsUp(true);
		}
		if (booComm)
		{
			communicationDesc =  "All chassis are up and running";
			M9kStationUtil.setAtleastOneDFRsUp(true);
		}
		else
		{
			if (booStationActive)
			{
//				communicationDesc = dfrs[inactiveDfrindex].getName()+" is down. ";
				M9kStationUtil.setAtleastOneDFRsUp(true);
			}
			else
			{
				M9kStationUtil.setAtleastOneDFRsUp(false);
				communicationDesc = "All chassis are down. Station is considered inactive";
			}
		}
		return booComm;
	}

	private static LedState getCurrentLedState(LedName ledName)
	{
		LedState currentLedState = null;
		if (M9kStationUtil.isPowerSupplyAvailable())
		{
			if (isPowerSupplyErrorLogged)
			{
				reinitializeAlarmModule();
			}
			M9kStationCommandClient commandClient = new M9kStationCommandClient(ipAddress, port, "led,led="+(ledName.ordinal()+1));
			try {
				String output = commandClient.sendAndReceive();
				int ledState = Integer.parseInt(output.trim());
	//			logger.debug("Converted led state return "+ledState);
				currentLedState = LedState.values()[ledState];
			} catch (NumberFormatException e) {
				logger.error("Error in getting current state of the led ",e);
			} catch (M9000Exception e) {
				logger.error("Error in getting current state of the led ",e);
			}
			catch (Exception e) {
				logger.error("Error in getting current state of the led ",e);
			}
		}
		return currentLedState;
	}

	public static boolean isAlreadyReported(LedName ledName, LedState ledState)
	{
		boolean isReported = true;
		LedState currentLedState = getCurrentLedState(ledName);
		logger.debug("Current state of "+ledName+" With ordinal "+(ledName.ordinal()+1)+" is "+currentLedState+" to be compared with new state "+ledState);
		if (currentLedState == null && !isPowerSupplyDownReported )
		{
			// Notify Web Master
			try {
				M9kStationUtil.sendNotification(M9kSeverityLevels.ERROR.name(), "Power Supply is not reachable");
				isPowerSupplyDownReported = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("Error in notifying Master station about Power Supply not reachable.",e);
			}
		}
		else if (currentLedState != null && isPowerSupplyDownReported)
		{
			// Notify Web Master
			try {
				M9kStationUtil.sendNotification(M9kSeverityLevels.INFO.name(), "Power Supply is Ok");
				isPowerSupplyDownReported = false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("Error in notifying Master station about Power Supply is up and ok.",e);
			}
		}

		if (currentLedState != null && !currentLedState.equals(ledState))
		{
			isReported = false;
		}
		return isReported;
	}
	public static void main(String args[])
	{
	}

	public int getRunFrequency() {
		return runFrequency;
	}

	public void setRunFrequency(int runFrequency) {
		this.runFrequency = runFrequency;
	}
	public double getV5Low() {
		return v5Low;
	}
	public double getV5High() {
		return v5High;
	}
	public double getV3_3Low() {
		return v3_3Low;
	}
	public double getV3_3High() {
		return v3_3High;
	}
	public double getV2_5Low() {
		return v2_5Low;
	}
	public double getV2_5High() {
		return v2_5High;
	}
	public double getV1_5Low() {
		return v1_5Low;
	}
	public double getV1_5High() {
		return v1_5High;
	}
	public double getV1_2Low() {
		return v1_2Low;
	}
	public double getV1_2High() {
		return v1_2High;
	}
	public double getV1_8Low() {
		return v1_8Low;
	}
	public double getV1_8High() {
		return v1_8High;
	}
	public double getTempLow() {
		return tempLow;
	}
	public double getTempHigh() {
		return tempHigh;
	}
	/**
	 * @return the tempWarnLimit
	 */
	public static double getTempWarnLimit() {
		return tempWarnLimit;
	}
	/**
	 * @param tempWarnLimit the tempWarnLimit to set
	 */
	public static void setTempWarnLimit(double tempWarnLimit) {
		M9kLED.tempWarnLimit = tempWarnLimit;
	}
	public double getFanRPMLow() {
		return fanRPMLow;
	}
	public double getFanRPMHigh() {
		return fanRPMHigh;
	}
	public int getDiskSafetyLimit() {
		return diskSafetyLimit;
	}
	public static int getTriggerDuration() {
		triggerDuration=M9kStationXMLUtil.getTriggerDuration();
		return triggerDuration;
	}
	

	public static boolean isAlreadyTriggering() {
		return alreadyTriggering;
	}
	
	/**
	 * Track disturbance alarm triggering
	 * @return
	 */
	public static boolean isAlreadyDisturbanceAlarm() {
		return alreadyDisturbanceAlarm;
	}
	
	public static boolean isCalibrating() {
		return isCalibrating;
	}
	public static void setCalibrating(boolean isCalibrated) {
		M9kLED.isCalibrating = isCalibrated;
	}
	public void resetCalibrationLeds() {
		updateLeds();
		
	}
	/**
	 * @return the disableAlarm
	 */
	public static boolean isDisableAlarm() {
		return disableAlarm;
	}
	/**
	 * @param disableAlarm the disableAlarm to set
	 */
	public static void setDisableAlarm(boolean disableAlarm) {
		if (M9kStationUtil.getUserDisableAlarms().toUpperCase().equalsIgnoreCase(M9kStationConstants.ENABLE))
		{
			M9kLED.disableAlarm = disableAlarm;
		}
		else
		{
			M9kLED.disableAlarm = false;
		}
	}
	/**
	 * @return the testTrigger
	 */
	public static boolean isTestTrigger() {
		return testTrigger;
	}
	/**
	 * @param testTrigger the testTrigger to set
	 */
	public static void setTestTrigger(boolean testTrigger) {
		M9kLED.testTrigger = testTrigger;
	}

	public static void setDisturbanceTriggerLedWithRelay(LedState ledState)
	{
		irigDateToMysqlDSate = M9kStationUtil.getDateFormat("yyyy-MM-dd HH:mm:ss").format(M9kStationUtil.getIRIG()/1000);
		ExecutorService  executor = Executors.newFixedThreadPool(1);

		String commandToSend;
		LedName ledName = LedName.DISTURBANCE;

		if (getTriggerDuration() > 0 && !isAlreadyDisturbanceAlarm())
		{
			triggerAlarmLogsDTO = new AlarmLogsDTO();
			triggerAlarmLogsDTO.setLedName(LedName.DISTURBANCE.name());
			triggerAlarmLogsDTO.setLedStatus(ledState.name());
//			LED_VALUE |= (1 << ledName.ordinal());
//			LED_VALUE &= ~(1 << (ledName.ordinal() + 8));
			logger.debug("Trigger duration "+getTriggerDuration());
			logger.debug("Timer "+disturbanceAlarmTimer);
		}

		RelayName relayName = M9kStationXMLUtil.getRelayMappings().get(ledName);
		logger.debug("TRIGGER: Already disturbance alarm triggering? "+isAlreadyDisturbanceAlarm());
		StringBuffer relaysInfo = new StringBuffer();
		if (!alreadyDisturbanceAlarm) // If the timer is active do nothing, set values only when timer is null
		{
			
			if (relayName!= null && ledState != LedState.OFF && ledState != LedState.YELLOW)
			{
					relaysInfo.append(relayName.name());
					switch (ledState) {
					case WARBLE:
						RELAY_VALUE &= ~(1 << (relayName.ordinal()));
						break;
	//				case RED:
	//					RELAY_VALUE |= (1 << (relays.ordinal()));
	//					break;
					default:
						break;
					}
//				triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("System triggered. Relay(s) "+relaysInfo+" set to alarm"));
				if (!disableAlarm && !isTestTrigger())
				{
					triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("System triggered for Disturbance Alarm"));
					commandToSend="set,led="+(ledName.ordinal()+1)+",ledAction="+ledState.name()+",relays="+RELAY_VALUE;
					logger.info("COMMAND to set disturbance alarm "+commandToSend);
					disableAlarm = false;
					testTrigger = false;
				}
				else
				{
					triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("Test Trigger using Trigger Now "));

					commandToSend="set,led="+(ledName.ordinal()+1)+",ledAction="+ledState.name();
				}
//				commandClient = new M9kStationCommandClient(ipAddress, port, "set,led="+(LedName.TRIGGER.ordinal()+1)+",ledAction="+ledState.name()+",relays="+RELAY_VALUE);
	
			}
			else
			{
				relaysInfo.append("No relays set or reset");
				triggerAlarmLogsDTO.setLedStatus(ledState.name());
				if(ledState == LedState.YELLOW)
				{
					triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("Warn: There might be a problem in trigger data"+ relaysInfo));
				}
				else
				{
					triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer(relaysInfo));
					
				}
				commandToSend = "set,led="+(ledName.ordinal()+1)+",ledAction="+ledState.name();
//				commandClient = new M9kStationCommandClient(ipAddress, port, "set,led="+(LedName.TRIGGER.ordinal()+1)+",ledAction="+ledState.name());			
			}
			
			logger.debug("Led value to be set "+LED_VALUE+" for led "+ledName);
	        try {
					logger.debug("About to schedule disturbanceAlarmTimer ");

				alreadyDisturbanceAlarm = true;
				if (M9kStationUtil.isPowerSupplyAvailable())
				{
					if (isPowerSupplyErrorLogged)
					{
						reinitializeAlarmModule();
					}
					if ( getTriggerDuration() > 0)
					{
						scheduleDisturbanceAlarmLedTimer();
					}
	//				commandClient.sendAndReceive();
					executor.execute(new M9kCommandThreads(ipAddress, port, commandToSend));
					// START: 24-Jan-2023 - Changed from Shutdown to ShutdownNow as LED was still warbling even after turning it off. Thanks to Todd Sampson for fixing it.
//					executor.shutdown();
					executor.shutdownNow();
				}
				else
				{
					alreadyDisturbanceAlarm = false;
				}
			} catch (Exception e) {
				// Warbling will not return any value,  hence there will be socket timeout exception, which we can ignore
				if (!(e.getCause() instanceof SocketTimeoutException))
				{
					logger.error("Error in connecting to power supply ",e);
				}
				else
				{
					logger.debug("It's warbling!");
				}
			}
	        finally
	        {
	        	disableAlarm = false;
	        	testTrigger = false;
	        }
	        try
	        {
				triggerAlarmLogsDTO.setRelays(relaysInfo.toString());
				if (mySqlAlarmLogsDAO == null)
				{
					mySqlAlarmLogsDAO = new MySqlAlarmLogsDAO();
				}
				triggerAlarmLogsDTO.setAlarmTime(irigDateToMysqlDSate);
				mySqlAlarmLogsDAO.insertAlarmDetails(triggerAlarmLogsDTO);

	        }
	        catch (Exception e) {
	        	logger.error("Error in inserting into alarms log "+triggerAlarmLogsDTO,e);
	        }

		}
		
	}
	
	private synchronized static void scheduleDisturbanceAlarmLedTimer() {
		disturbanceAlarmTimer = new Timer();
		logger.debug("DISTURBANCE: About to schedule timer for duration "+getTriggerDuration());
	    disturbanceAlarmTimer.schedule(new TimerTask(){
		    public void run() {
			      logger.debug("Time's up! disturbanceAlarmTimer is reset and disturbanceAlarmTimer cancelled");
			      resetDisturbanceAlarmStatus();
			      if (disturbanceAlarmTimer != null)
			      {
			    	  disturbanceAlarmTimer.cancel(); 
			    	  disturbanceAlarmTimer = null;
			      }
			      alreadyDisturbanceAlarm = false;
			    }
	    }, getTriggerDuration() * 1000);
		
	}
	public static void resetDisturbanceAlarmStatus()
	{
		LedName disturbanceAlarmLedName = LedName.DISTURBANCE;
		M9kStationCommandClient commandClient;
		RelayName relayName = M9kStationXMLUtil.getRelayMappings().get(disturbanceAlarmLedName);
		irigDateToMysqlDSate = M9kStationUtil.getDateFormat("yyyy-MM-dd HH:mm:ss").format(M9kStationUtil.getIRIG()/1000);

//		// Set LED status to OFF
//		LED_VALUE &= ~(1 << LedName.TRIGGER.ordinal());
//		LED_VALUE &= ~(1 << (LedName.TRIGGER.ordinal() + 8));
		StringBuffer relaysInfo = new StringBuffer();
		if (relayName!= null )
		{
			relaysInfo.append(relayName.name());
			
			if (triggerAlarmLogsDTO != null){
//				triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("Trigger time complete. Relay(s) "+relaysInfo+" reset"));
				triggerAlarmLogsDTO.setAlarmDescription(new StringBuffer("Disturbance Alarm time complete."));
			}
			if (!isRelaySharedWithOtherLEDs(disturbanceAlarmLedName, relayName))
			{
				RELAY_VALUE |= (1 << (relayName.ordinal()));
				logger.debug("Resetting Disturbance Alarm trigger led and relay together");
			}
			else
			{
				// Some LED is set to RED that needs this relay to be set
				RELAY_VALUE &= ~(1 << (relayName.ordinal()));
			}
			commandClient = new M9kStationCommandClient(ipAddress, port, "set,led="+(disturbanceAlarmLedName.ordinal()+1)+",ledAction="+LedState.OFF.name()+",relays="+RELAY_VALUE);
		}
		else
		{
			logger.debug("Resetting Disturbance Alarm led alone ");
			relaysInfo.append("No relays reset");
			commandClient = new M9kStationCommandClient(ipAddress, port, "set,led="+(disturbanceAlarmLedName.ordinal()+1)+",ledAction="+LedState.OFF.name());			
		}
		logger.debug("\n\n\tTRIGGER: Reset trigger to OFF "+iCount +"\n\n");
        logger.debug("Trigger Led value to be reset "+LED_VALUE+" Relay value "+RELAY_VALUE);
        if (M9kStationUtil.isPowerSupplyAvailable())
		{
        	if (isPowerSupplyErrorLogged)
    		{
        		reinitializeAlarmModule();
    		}
	        try {
				commandClient.sendAndReceive();
			} catch (M9000Exception e) {
				logger.error("Error in resetting trigger led with value "+LED_VALUE,e);
			}
			catch (Exception e) {
				logger.error("Error in resetting trigger led with value "+LED_VALUE,e);
			}
		}
        try
        {
			if (triggerAlarmLogsDTO != null){
				triggerAlarmLogsDTO.setLedStatus(LedState.OFF.name());
				triggerAlarmLogsDTO.setRelays(relaysInfo.toString());
				triggerAlarmLogsDTO.setAlarmTime(irigDateToMysqlDSate);
				mySqlAlarmLogsDAO.insertAlarmDetails(triggerAlarmLogsDTO);
			}

        }
        catch (Exception e) {
			logger.error("Error in inserting trigger alarms into database "+LED_VALUE,e);
		} 
		finally
		{
			
			triggerAlarmLogsDTO = null;
		}
	
	}

}
