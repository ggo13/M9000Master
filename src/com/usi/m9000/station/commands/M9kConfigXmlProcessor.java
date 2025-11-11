package com.usi.m9000.station.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.xmlbeans.XmlOptions;

import com.usi.AlarmDocument.Alarm;
import com.usi.AlarmsDocument.Alarms;
import com.usi.DFRDocument.DFR;
import com.usi.PdcDocument.Pdc;
import com.usi.SubStationDocument.SubStation;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.MySqlDfrDetailsDAO;
import com.usi.m9000.dao.MySqlStationDAO;
import com.usi.m9000.dao.MySqlSystemDAO;
import com.usi.m9000.dao.SystemDAO;
import com.usi.m9000.station.M9kLED;
import com.usi.m9000.station.action.M9kStationRequestProcessor;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.LedState;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kScp;
import com.usi.m9000.xml.M9000XmlConfig;

public class M9kConfigXmlProcessor {

private final static String command = "RESTART";
String configXml;
M9kStationCommandClient commandClient;
private static MySqlStationDAO mySqlStationDAO = null;
private MySqlDfrDetailsDAO mySqlDfrDetailsDAO = null;

SubStation substation;
M9000XmlConfig m9kConfig = null;
XmlOptions xmlOptions ;
Map<String, String> prefixes;
String pdcXmlFileWithPath;
static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kConfigXmlProcessor.class);

public M9kConfigXmlProcessor() {
	super();
	try {
		// Initialize xml options for xbeans
		xmlOptions = new XmlOptions();
		xmlOptions.setSaveOuter();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setUseDefaultNamespace();
		xmlOptions.setSaveNoXmlDecl();
		prefixes = new HashMap<String, String>();
		prefixes.put("", "http://www.usi.com");
		xmlOptions.setSaveImplicitNamespaces(prefixes);
		mySqlStationDAO = new MySqlStationDAO();
		mySqlDfrDetailsDAO = new MySqlDfrDetailsDAO();
		
	}catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

public StringBuffer applyConfig(String configXml) throws M9000Exception
{
//	String result = "SUCCESS";
	StringBuffer messageToMaster = null;
//	boolean booResult = false;
	if (configXml == null || configXml.isEmpty())
	{
		messageToMaster = new StringBuffer("ERROR: Config xml is invalid");
		return messageToMaster;
	}
//	String dfrId;
//	M9kDFRHealthStatusProcessor m9kDFRHealthStatusProcessor = new M9kDFRHealthStatusProcessor();
//	Map<String, Boolean> mapDfrsStatus = m9kDFRHealthStatusProcessor.getDFRsStatus();
//	if (mapDfrsStatus.containsValue(false))
//	{
//		messageToMaster.append("ERROR: Configuration could not be applied to atleast one of the chassis");
//		for (Iterator<String> iterator = mapDfrsStatus.keySet().iterator(); iterator.hasNext();) {
//			dfrId = iterator.next();
//			if (!mapDfrsStatus.get(dfrId))
//			{
//				messageToMaster.append(M9kStationConstants.NEWLINE);
//				messageToMaster.append(dfrId+" is down");
//			}
//			
//		}
//	}
	try {
		m9kConfig = new M9000XmlConfig();
		m9kConfig.loadStationXmlFile(new StringReader(configXml));
		
	} catch (M9000Exception e) {
		logger.error("Error in parsing config xml. config xml "+configXml,e);
//		return false;
		messageToMaster = new StringBuffer("ERROR: Config xml is invalid ");
		return messageToMaster;
	}	
	//TODO: PMU requirement that may be required in the future
	substation = m9kConfig.getSubstation();

	DFR[] dfrs = substation.getDFRs().getDFRArray();
	String configFile = "/tmp/newconfig.xml";
	File tempFile = new File (configFile);
	Path tmpPath = null;

	M9kLED.resetLed();
	M9kStationXMLUtil.initXml(configXml);
	
	// START: 27-May-2021 - Backup entire station configuration in each of the chassis
	// START: 23-Jul-2020 - BugFIX - to back up recently modified config
	M9kStationDBUtil.setStationDetails(null);
	// END: 23-Jul-2020
	// START: 09-Dec-2024 - Commented the block below and moved up the db code so that ACTIVE_CONFIG.sql is up-to-date instead of one version old 
			if (M9kStationUtil.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			{
				boolean status = mySqlStationDAO.updateLocalStationDetails(substation, configXml);
				logger.debug("Local config update status "+status);
			}
	// END: 09-Dec-2024 - Move up the db code so that ACTIVE_CONFIG.sql is up-to-date instead of one version old	
			
			// START: 23-Jan-2025 - Backup in station master software only if it is Remote architecture. As in local architecture, tomcat web application will take care of backup
			String backedUpStationConfigFileName = null;
			if (M9kStationUtil.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			{
					backedUpStationConfigFileName = M9kStationUtil.backUpConfig();
			}
			else
			{
				try
				{
					tmpPath = Files.createTempFile("tempConfigSQLFile", ".sql");
					Files.writeString(tmpPath, M9kStationDBUtil.getStationDetails().getStationDetailsAsSQLStmt());
					backedUpStationConfigFileName = tmpPath.toAbsolutePath().toString();
				}
				catch (IOException e)
				{
					logger.error("Error in creating temp file "+e);
				}
			}
			// END: 23-Jan-2025
//	Pattern fileDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
//	java.util.regex.Matcher matcher = fileDate.matcher(backedUpStationConfigFileName);
//	if (matcher.find()) {
//		backedUpStationConfigFileName = backedUpStationConfigFileName.substring(backedUpStationConfigFileName.lastIndexOf( matcher.group()));
//	}
//	else
//	{
//		backedUpStationConfigFileName = backedUpStationConfigFileName.substring(backedUpStationConfigFileName.lastIndexOf("/"));
//	}
	
	// END: 27-May-2021
	BufferedWriter bw = null;
	
	ExecutorService executor = Executors.newFixedThreadPool(dfrs.length);
	int port = M9kStationUtil.getPort();
	boolean booComplete = true;
	List<M9kScp> scpTasksList = new ArrayList<M9kScp>(); // <M9kScp>
	for (int i = 0; i < dfrs.length; i++) {
		logger.debug("queue name and Dfr id "+dfrs[i].getSystem().getDfrId());
		logger.debug("Dfr ip address "+dfrs[i].getIPAddress());
		logger.debug("XML text to be sent... "+dfrs[i].xmlText(xmlOptions));
//		M9kStationMasterPublisher.sendMessage(dfrs[i].getId(), dfrs[i].xmlText(xmlOptions));
		try
		{
			// Begin: 27-Jul-2020 - M2002_redux code 
//			bw = new BufferedWriter(new FileWriter(tempFile));
//			bw.write(dfrs[i].xmlText(xmlOptions));
//			bw.close();
//			M9kScp m9kScp = new M9kScp(dfrs[i].getIPAddress(),22,"root");
//			m9kScp.sendConfig(configFile, backedUpStationConfigFileName);
			M9kScp m9kScp = new M9kScp(dfrs[i].getIPAddress(),22,"root",dfrs[i].xmlText(xmlOptions), backedUpStationConfigFileName);
			scpTasksList.add(m9kScp);
			executor.execute(m9kScp);
//			logger.info("Successfully sent the files to "+dfrs[i].getIPAddress());
//			executor.execute(new M9kCommandThreads(dfrs[i].getIPAddress(), port, command));
			
//			executor.execute(new M9kCommandThreads(dfrs[i].getIPAddress(), port, "configWrite,"+dfrs[i].xmlText(xmlOptions)));
//			// START: 27-May-2021 - Backup entire station configuration in each of the chassis
//			if (backedUpStationConfigFileName != null)
//			{
//				logger.debug("backed up config "+backedUpStationConfigFileName+" in dfr "+dfrs[i].getIPAddress());
//				m9kScp.backupStationConfig(backedUpStationConfigFileName);
//			}
//			// END: 27-May-2021
			
			// End: 27-Jul-2020
		}
		catch (Exception e) {
//			booResult=false;
			if (messageToMaster == null)
			{
				messageToMaster = new StringBuffer();
				messageToMaster.append("ERROR: Configuration could not be applied to atleast one of the chassis. ");
			}
			messageToMaster.append(" DFR"+dfrs[i].getSystem().getDfrId());
			logger.error("Error occured while applying config file caused by "+e.getCause(),e);
		} 
		finally
		{
			if (bw != null)
			{
				try {
					
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.warn("Unable to close Buffered Writer ", e);
				}
			}
			tempFile.delete();
			try {
				if (tmpPath != null)
				{
					Files.deleteIfExists(tmpPath);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	executor.shutdown();
	logger.info("messageToMaster "+messageToMaster);
//	try
//	{
//		if (messageToMaster == null)
//		{
//			M9kScp m9kScp;
//			for (Iterator<M9kScp> iterator = scpTasksList.iterator(); iterator.hasNext();) {
//				m9kScp = iterator.next();
//				Thread thread = new Thread(m9kScp);
//		        thread.start();
//		        thread.join(); // Wait for the thread to complete
//				logger.info("DFR"+m9kScp.getHost()+" has failed "+m9kScp.isFailed());
//				if (m9kScp.isFailed())
//				{
//					if (messageToMaster == null)
//					{
//						messageToMaster = new StringBuffer();
//						messageToMaster.append("ERROR: Configuration could not be applied to atleast one of the chassis. ");
//					}
//					messageToMaster.append(" DFR"+m9kScp.getHost());			
//				}		
//			}
//		}	
//	}
//	catch (Exception e) {
//		logger.error("Error in waiting for thread ",e);
//	}
	logger.debug("messageToMaster after loop "+messageToMaster);
	try {
		booComplete = executor.awaitTermination(2, TimeUnit.MINUTES);
		logger.debug("After awaitTermination booComplete "+booComplete);
		if (messageToMaster == null)
		{
			M9kScp m9kScp;
			for (Iterator<M9kScp> iterator = scpTasksList.iterator(); iterator.hasNext();) {
				m9kScp = iterator.next();
				logger.debug("DFR"+m9kScp.getHost()+" has failed "+m9kScp.isFailed());
				if (m9kScp.isFailed())
				{
					if (messageToMaster == null)
					{
						messageToMaster = new StringBuffer();
						messageToMaster.append("ERROR: Configuration could not be applied to atleast one of the chassis. ");
					}
					messageToMaster.append(M9kStationUtil.getDFRNameFromIP(m9kScp.getHost())+" ");			
				}		
			}
		}	

		if (!booComplete)
		{
			logger.warn("DFR may be down as it takes too long to respond for applying config.");
			List<Runnable> listUnfinishedProcess = executor.shutdownNow();
			for (Iterator<Runnable> iterator = listUnfinishedProcess.iterator(); iterator
					.hasNext();) {
				M9kCommandThreads runnable = (M9kCommandThreads) iterator.next();
				logger.error("DFR "+runnable.getIpAddress()+" cannot be restarted or timed out");
				logger.debug("DFR pending restart "+runnable.getIpAddress());
				
			}
		}
// START: 09-Dec-2024 - Commented the code and Moved up the db code so that ACTIVE_CONFIG.sql is up-to-date instead of one version old 
//		if (M9kStationUtil.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
//		{
//			boolean status = mySqlStationDAO.updateLocalStationDetails(substation, configXml);
//			logger.debug("Local config update status "+status);
//		}
// END: 09-Dec-2024 - Move up the db code so that ACTIVE_CONFIG.sql is up-to-date instead of one version old	
	
		// START: 24-Jan-2025 - Station Master Software creates a backup only when it's in remote architecture
//		// START: 09-Dec-2024 - Remove the backup file if its local architecture to avoid duplicate file backup
//		if (!M9kStationUtil.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
//		{
//			try {
//				// Remove the backup file as it's already created by web application in local architecture
//				File newlyCreatedBackupFile = new File(backedUpStationConfigFileName);
//				newlyCreatedBackupFile.delete();
//			}
//				catch (Exception e) {
//					logger.error("Unable to delete the duplicate backup file",e);
//				}
//		}
//		// END: 09-Dec-2024 - Remove the backup file if its local architecture to avoid duplicate file backup
		// END: 24-Jan-2025 - Station Master Software creates a backup only when it's in remote architecture
		if (substation.getPdc() != null)
		{
			updateAndRestartPDC();
		}
		
//		scheduleMysqlEvents(configXml);
		M9kStationXMLUtil.initXml(configXml);
		M9kStationRequestProcessor.getInstance().reInitialize();
		M9kStationDBUtil.setStationDetails(null);
		M9kStationUtil.setLstHealthAttr(null);// Make it reread the health attribute for wetting voltage monitoring enable or disable
		mySqlDfrDetailsDAO.createOrUpdateDfrDetails();
		M9kStationUtil.insertUserActionToAlarmsLog("Config with serial number "+M9kStationXMLUtil.getConfigurationSerialNumber()+ " is applied to all the chassis. ", LedState.GREEN.name());
		// 17-Feb-2023 - Write Alarms info for m9kstatus command
		createOrUpdateAlarmsInfoFile(configXml);
	} catch (InterruptedException e) {
		logger.error("Error in applying config ",e);
	}
	// Reset(NuLL) the station details so that new updated station details are read fresh from the database
	logger.debug("messageToMaster to return "+messageToMaster);
	return messageToMaster;

}

public StringBuffer updateConfigInDBOnly(String configXml) throws M9000Exception
{
	StringBuffer messageToMaster = null;
	if (configXml == null || configXml.isEmpty())
	{
		messageToMaster = new StringBuffer("ERROR: Config xml is invalid");
		return messageToMaster;
	}
	
	if (M9kStationUtil.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
	{
		try {
			m9kConfig = new M9000XmlConfig();
			m9kConfig.loadStationXmlFile(new StringReader(configXml));
			
		} catch (M9000Exception e) {
			logger.error("Error in parsing while saving advanced settings. Received config xml "+configXml,e);
//			return false;
			messageToMaster = new StringBuffer("ERROR: Error in parsing while saving advanced settings. ");
			return messageToMaster;
		}	
		boolean status = mySqlStationDAO.updateLocalStationDetails(m9kConfig.getSubstation(), configXml);
		logger.debug("Local config update status "+status);
	}
	scheduleMysqlEvents(configXml);
	M9kLED.resetLed();
	M9kLED.resetRelay();
	M9kStationXMLUtil.initXml(configXml);
	M9kStationRequestProcessor.getInstance().reInitialize();
	M9kStationDBUtil.setStationDetails(null);
	M9kStationUtil.setLstHealthAttr(null);// Make it reread the health attribute for wetting voltage monitoring enable or disable

	messageToMaster = new StringBuffer("SUCCESS: Advanced system settings saved successfully");
	M9kStationUtil.insertUserActionToAlarmsLog("Advanced system settings modified in the config.", LedState.GREEN.name());

	// 17-Feb-2023 - Write Alarms info for m9kstatus command
	createOrUpdateAlarmsInfoFile(configXml);
	
	return messageToMaster;
}
/**
 * Mysql events to create and drop partitions and drop old data
 * @param configXml2
 */
private void scheduleMysqlEvents(String configXml) {
	
	SystemDAO systemDao = new MySqlSystemDAO();
	
	try {
		systemDao.createMySQLEvents(M9kStationConstants.MYSQL_EVENT_NAME_DROP_CONT_OSC);
		systemDao.createMySQLEvents(M9kStationConstants.MYSQL_EVENT_NAME_DROP_CONT_MEASUREMENTS);
		systemDao.createMySQLEvents(M9kStationConstants.MYSQL_EVENT_NAME_CREATE_CONT_OSC);
		systemDao.createMySQLEvents(M9kStationConstants.MYSQL_EVENT_NAME_CREATE_CONT_MEASUREMENTS);
		systemDao.createMySQLEvents(M9kStationConstants.MYSQL_EVENT_NAME_REMOVE_OLD_SER);
		systemDao.createMySQLEvents(M9kStationConstants.MYSQL_EVENT_NAME_REMOVE_OLD_DFRLOG);
	} catch (M9000Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

private void updateAndRestartPDC()
{
	BufferedWriter bwPdc = null;
	BufferedReader reader = null;
	substation = m9kConfig.getSubstation();
	Process p;
	try
	{
		// PMU config file write out
		Pdc pdc = substation.getPdc();
		File pdcConfigFile = new File(M9kStationUtil.getPdxXmlFileNameWithPath());
		bwPdc = new BufferedWriter(new FileWriter(pdcConfigFile));;
		bwPdc.write(pdc.xmlText(xmlOptions));
		bwPdc.close();

		p = Runtime.getRuntime().exec("killall -9 m9kpdc");
		p.waitFor();
		reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null)
		{
			sb.append(line);
			sb.append(M9kConstants.NEWLINE);
		}
		reader.close();
		logger.debug("\nResponse for restarting PDC "+sb.toString());
	}
	catch (Exception e) {
		logger.error("Error in updating PDC",e);
	}
	finally
	{
		
		try {
			if (bwPdc != null)
			{
				bwPdc.close();
				bwPdc = null;
			}
			if (reader != null)
			{
				reader.close();
				reader = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

public String reinitializeStation(String commandToReinitialize)
{
	String result = "SUCCESS";
	logger.debug("Command to reinitialize "+commandToReinitialize);
	BufferedReader reader = null;
	Process p;
	try
	{

		p = Runtime.getRuntime().exec(new String[]{"sh","-c",commandToReinitialize});
		p.waitFor();
		reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));

		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = reader.readLine()) != null)
		{
			sb.append(line);
			sb.append(M9kConstants.NEWLINE);
		}
		reader.close();
		logger.debug("\nResponse for restarting station master "+sb.toString());
	}
	catch (Exception e) {
		logger.error("Error in reinitializing m9k station software",e);
		result = "ERROR in reinitializing due to "+e.getMessage();
	}
	finally
	{
		
		try {
			if (reader != null)
			{
				reader.close();
				reader = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	return result;
}

public void reinitializeStation(File activeConfigFile) {
	BufferedReader bfrActiveConfig = null;
	String line;
	StringBuilder sbConfigXml = new StringBuilder();
	String configXml;
	logger.debug("Inside reinitializeStation...");
	try
	{
		bfrActiveConfig = new BufferedReader(new InputStreamReader(new FileInputStream(activeConfigFile)));
		while ((line = bfrActiveConfig.readLine()) != null)
		{
		    sbConfigXml.append(line);
		}
		bfrActiveConfig.close();
		configXml = M9kStationXMLUtil.retrieveConfigXmlFromSql(sbConfigXml.toString());
		M9kStationXMLUtil.initXml(configXml);
		M9kStationRequestProcessor.getInstance().reInitialize();
		M9kStationDBUtil.setStationDetails(null);
		M9kStationUtil.setLstHealthAttr(null);// Make it reread the health attribute for wetting voltage monitoring enable or disable
		logger.debug("reinitializeStation complete...");
	}
	catch (Exception e) {
		logger.error("Error occurred while reinitializeStation ",e);
	}
	
}

public String getConfigXml() {
	return configXml;
}

public void setConfigXml(String configXml) {
	this.configXml = configXml;
}

private void createOrUpdateAlarmsInfoFile(String configXml)
{
	BufferedWriter bwAlarmsMap = null;
	try
	{
		try {
			m9kConfig = new M9000XmlConfig();
			m9kConfig.loadStationXmlFile(new StringReader(configXml));
			
		} catch (M9000Exception e) {
			logger.error("Error in parsing while saving advanced settings. Received config xml "+configXml,e);
			return ;
		}	

		substation = m9kConfig.getSubstation();
		Map<String,String> mapAlarmsMap = new TreeMap<String, String>();
		// Alarms map info file write out
		Alarms alarms = substation.getStationProperties().getAlarms();
		Alarm[] alarmDetails = alarms.getAlarmArray();
		for (int i = 0; i < alarmDetails.length; i++) {
			if (mapAlarmsMap.get(alarmDetails[i].getRelay()) == null)
			{
				mapAlarmsMap.put(alarmDetails[i].getRelay(), alarmDetails[i].getAlarmName());
			}
			else
			{
				mapAlarmsMap.put(alarmDetails[i].getRelay(), mapAlarmsMap.get(alarmDetails[i].getRelay())+","+alarmDetails[i].getAlarmName());
			}
		}
		File pdcConfigFile = new File(M9kStationUtil.getAlarmsMapInfoFileNameWithPath());
		bwAlarmsMap = new BufferedWriter(new FileWriter(pdcConfigFile));
		String relayName;
		for (Iterator<String> iterator = mapAlarmsMap.keySet().iterator(); iterator.hasNext();) {
			relayName = iterator.next();
			
			bwAlarmsMap.write(relayName+"="+mapAlarmsMap.get(relayName));
			bwAlarmsMap.newLine();
		}
		bwAlarmsMap.close();

	}
	catch (Exception e) {
		logger.error("Error in updating Alarms Map Info",e);
	}
	finally
	{
		
		try {
			if (bwAlarmsMap != null)
			{
				bwAlarmsMap.close();
				bwAlarmsMap = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
}
