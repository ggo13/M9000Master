package com.usi.m9000.station.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kComtradeParser;
import com.usi.m9000.dao.ComtradeDataDAO;
import com.usi.m9000.dao.ContinuousComtradeDataDAO;
import com.usi.m9000.dao.LongTermDataDAO;
import com.usi.m9000.dao.MySqlDatDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.CalibrationDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.M9kLED;
import com.usi.m9000.station.commands.M9kCalibrationProcessor;
import com.usi.m9000.station.commands.M9kConfigXmlProcessor;
import com.usi.m9000.station.commands.M9kDFRHealthStatusProcessor;
import com.usi.m9000.station.commands.M9kDfrBoardDetectProcessor;
import com.usi.m9000.station.commands.M9kDfrsInfoProcessor;
import com.usi.m9000.station.commands.M9kEventTestProcessor;
import com.usi.m9000.station.commands.M9kScopeProcessor;
import com.usi.m9000.station.commands.M9kTriggerProcessor;
import com.usi.m9000.station.faultLocation.M9kFaultLocation;
import com.usi.m9000.station.faultLocation.M9kFaultReport;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kSeverityLevels;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;

public class M9kStationRequestProcessor{
	Map<String, String> mapDfrs;	
	static HierarchicalINIConfiguration iniConf;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationRequestProcessor.class);
	private static M9kStationRequestProcessor m9kStationRequestProcessor = null;
	List<DfrDTO> lstDfrs;
//	private static MySqlStationDAO mySqlStationDAO = null;
//	static String stationConfigXml;

	private M9kConfigXmlProcessor configXmlProcessor;
	private M9kTriggerProcessor triggerHandler;
	private M9kCalibrationProcessor calibrate;
	private M9kDfrsInfoProcessor dfrInfo;
	private M9kDfrBoardDetectProcessor dfrBoardDetect;
	private M9kDFRHealthStatusProcessor stationHealthStatus;
	private M9kScopeProcessor scopeProcessor;
	private M9kEventTestProcessor eventTest;
	private static MySqlDatDAO mySqlDataDAO = null;
	private ComtradeDataDAO comtradeDetailsDao ;
	private LongTermDataDAO longTermDataDAO;
	private ContinuousComtradeDataDAO mysqlContinuousComtradeDAO;
	
	private M9kStationRequestProcessor()
	{
		try {
			M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
			iniConf = new HierarchicalINIConfiguration("station.properties");
			mapDfrs = new HashMap<String, String>();
			lstDfrs = new ArrayList<DfrDTO>();
			initializeRequestProcessors();
			comtradeDetailsDao = m9kDAOFactory.getComtradeDataDAO();
			mySqlDataDAO =new MySqlDatDAO();
			longTermDataDAO = m9kDAOFactory.getLongTermDataDAO();
			mysqlContinuousComtradeDAO = m9kDAOFactory.getContinuousComtradeDataDAO();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void initializeRequestProcessors()
	{
		configXmlProcessor = new M9kConfigXmlProcessor();
		triggerHandler = new M9kTriggerProcessor();
		calibrate = new M9kCalibrationProcessor();
		dfrInfo = new M9kDfrsInfoProcessor();
		dfrBoardDetect = new M9kDfrBoardDetectProcessor();
		stationHealthStatus = new M9kDFRHealthStatusProcessor();
		scopeProcessor = new M9kScopeProcessor();
		eventTest = new M9kEventTestProcessor();
	}
	
	public void reInitialize()
	{
		scopeProcessor = new M9kScopeProcessor();
	}
	public static M9kStationRequestProcessor getInstance()
	{
		if (m9kStationRequestProcessor == null)
		{
			m9kStationRequestProcessor = new M9kStationRequestProcessor();

		}
		
		return m9kStationRequestProcessor;
	}
	public StringBuffer processAndSendConfig(String configXml) throws M9000Exception
	{
//			boolean booResult = configXmlProcessor.applyConfig(configXml);
			StringBuffer applyConfigResult = configXmlProcessor.applyConfig(configXml);
			if (applyConfigResult == null)
			{
				M9kStationUtil.sendNotification(M9kSeverityLevels.INFO.name(), "Station Configuration was modified and sent successfully.");
			}
			else
			{
				M9kStationUtil.sendNotification(M9kSeverityLevels.ERROR.name(), applyConfigResult.toString());
			}
			return applyConfigResult;
	}
	
	/**
	 * 28-Dec-2022 - Advanced settings update to station master db
	 * @param configXml
	 * @return
	 * @throws M9000Exception
	 */
	public StringBuffer updateConfigInDB(String configXml) throws M9000Exception
	{
			StringBuffer applyConfigResult = configXmlProcessor.updateConfigInDBOnly(configXml);
			if (applyConfigResult.toString().toUpperCase().startsWith("SUCCESS"))
			{
				M9kStationUtil.sendNotification(M9kSeverityLevels.INFO.name(), "SUCCESSFUl:Advanced Settings updated for the Station");
			}
			else
			{
				M9kStationUtil.sendNotification(M9kSeverityLevels.ERROR.name(), applyConfigResult.toString());
			}
			return applyConfigResult;
	}

	public String reinitializeStation(String commandToReinitialize) throws M9000Exception
	{
		return configXmlProcessor.reinitializeStation(commandToReinitialize);
	}

	public boolean processTriggerNow() throws M9000Exception
	{
		boolean booResult = triggerHandler.triggerNow();
		return booResult;
	}
	
	public CalibrationDTO processCalibration() throws Exception
	{
//		calibrate.setNoOfTimes(noOfTimes);
//		logger.debug("CALIBRATE: No of times to calibrate "+noOfTimes);
		logger.debug("CALIBRATE: About to call calibrateNow method "+calibrate);
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		CalibrationDTO calibrationDTO = calibrate.calibrateNow();
		calibrationDTO.setCalTime(sdf.format(cal.getTime()));
		calibrate.insertIntoCalReportTable(calibrationDTO);
		logger.debug("CALIBRATE: After inserting into db "+calibrationDTO);
		return calibrationDTO;
	}
	
	public CalibrationDTO processEventTest(String messageToSend) throws M9000Exception
	{
		CalibrationDTO calibrationDTO = null;
		try
		{
		logger.debug("EventTest: About to call calibrateNow method "+calibrate);
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		calibrationDTO = eventTest.eventTestNow(messageToSend);
		calibrationDTO.setCalTime(sdf.format(cal.getTime()));
		calibrate.insertIntoCalReportTable(calibrationDTO);
		logger.debug("CALIBRATE: After inserting into db "+calibrationDTO);
		}
		catch (Exception e) {
			throw new M9000Exception("Error in Event Test process.",e);
		}
		finally
		{
			M9kLED.setCalibrating(false);
		}
		return calibrationDTO;
	}
	public String processExternalCalibration(String command) throws M9000Exception
	{
		logger.debug("EXTERNAL-CALIBRATE: About to call processExternalCalibration method "+command);
		String extCalStatus = calibrate.processExternalCalibration(command);
		return extCalStatus;
	}
	
	public String processAndApplyCalFactors(String command) throws M9000Exception
	{
		String calApplyResult  = calibrate.applyCalFactors(command);
			return calApplyResult;
	}
	
	public CalibrationDTO verifyCalibration(String command) throws M9000Exception
	{
		CalibrationDTO calibrationDTO = calibrate.verifyCalibration(command);
		M9kLED.setCalibrating(false);
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		calibrationDTO.setCalTime(sdf.format(cal.getTime()));
		calibrate.insertIntoCalReportTable(calibrationDTO);
		return calibrationDTO;
	}
	public void processDfrInfo() throws M9000Exception
	{
		dfrInfo.getDfrInfo();
	}
	public void processBoardDetect() throws M9000Exception
	{
		dfrBoardDetect.detectBoards();
	}
	
	public String processHealthRequest()
	{
		String healthStatus = stationHealthStatus.getHealthStatusXml();
		return healthStatus;
	}
	
	public byte[] getScopeData(String command)
	{
		return scopeProcessor.processScope(command);
	}
	public Map<String, String> getScopeConfig()
	{
		return scopeProcessor.getScopeConfig();
	}
	
	/**
	 * START: 13-Jul-2020 - Hall Effect implementation - Config request for specific dfr
	 * @param dfrId
	 * @return
	 */
	public String getDFRSpecificScopeConfig(int dfrId)
	{
		return scopeProcessor.getDFRSpecificScopeConfig(dfrId);
	}
	
	public String getLastCalVerifiedDate()
	{
		return calibrate.getLastVerifiedDate();
	}
	
	public String getLastCalDate()
	{
		return calibrate.getLastCalDate();
	}
	
	public String getLastEventTestDate()
	{
		return eventTest.getLastEventTestDate();
	}
	public String calculateFaultLocation(String faultFileName) throws M9000Exception
	{
		StringBuffer strFaultLocationDetails=null;
		String dataDir = M9kStationUtil.getDataDir();
		logger.info("Fault file name received "+faultFileName+" dataDir "+dataDir);
		M9kComtradeParser m9kComtradeParser = new M9kComtradeParser(new File(dataDir+faultFileName+M9kConstants.DAT_FILE_EXTN));
		M9kFaultLocation m9kFaultLocation = new M9kFaultLocation(m9kComtradeParser.getProcessData());
		Map<LineGroupsAlgorithm, M9kFaultReport> mapFaultReport = m9kFaultLocation.findFaultLocation(true);
		M9kFaultReport faultReport;
		for (LineGroupsAlgorithm lineGroup : mapFaultReport.keySet()) {
			logger.debug("Line group Name "+lineGroup.getLineGroupName());
			faultReport = mapFaultReport.get(lineGroup);
			if (faultReport != null)
			{
				if (strFaultLocationDetails == null)
				{
					strFaultLocationDetails = new StringBuffer();
				}
				strFaultLocationDetails.append("Line Name: "+lineGroup.getLineGroupName()+M9kConstants.NEWLINE);
				strFaultLocationDetails.append(faultReport.getFaultReportDetails()+M9kConstants.NEWLINE);
				System.out.println("Details "+faultReport.getFaultReportDetails());
			}
		}
		System.out.println("Fault Details "+mapFaultReport);

		return strFaultLocationDetails.toString();
	}
	
	public boolean deleteFaultFiles(String faultFileNames, String dataType) throws M9000Exception
	{
		boolean booResult = true;
		String dataDir = M9kStationUtil.getDataDir();

		String[] arrFileNames = faultFileNames.split(";;");
		String faultFileName;
		File fileToBeDeleted;
		StringBuffer strbufFaultIds = new StringBuffer(); 
		int resultFaultid = -1;
		for (int i = 0; i < arrFileNames.length; i++) {
			faultFileName = arrFileNames[i];
			if (dataType != null && !dataType.isEmpty() && dataType.equalsIgnoreCase(M9kConstants.FAULT_TYPE))
			{
				try {
					resultFaultid = M9kStationUtil.getFaultIdFromFileName(faultFileName);
				} catch (Exception e) {
					logger.error("Exception with parsing for faultId from filename "+faultFileName+" caused by "+e.getMessage());
					resultFaultid = -1;
				}
				if (resultFaultid > -1)
				{
					if (i == 0)
					{
						strbufFaultIds.append(resultFaultid);
					}
					else
					{
						strbufFaultIds.append(","+resultFaultid);
					}
				}
			}
			// Delete DAT
			fileToBeDeleted = new File(dataDir+faultFileName+M9kConstants.DAT_FILE_EXTN);
			logger.debug("Fault file to be deleted "+fileToBeDeleted.getPath()+" - "+fileToBeDeleted.getName());
			if (fileToBeDeleted.exists())
			{
				fileToBeDeleted.delete();
			}
			// Delete CFG
			fileToBeDeleted = new File(dataDir+faultFileName+M9kConstants.CFG_FILE_EXTN);
			logger.debug("Fault file to be deleted "+fileToBeDeleted.getPath()+" - "+fileToBeDeleted.getName());
			if (fileToBeDeleted.exists())
			{
				fileToBeDeleted.delete();
			}
			// Delete INF
			fileToBeDeleted = new File(dataDir+faultFileName+M9kConstants.INF_FILE_EXTN);
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
				if (strbufFaultIds.length() > 0 )
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
			else if(dataType.equalsIgnoreCase(M9kConstants.LTR_ANALOG_TYPE) || dataType.equalsIgnoreCase(M9kConstants.LTR_MEASUREMENT_TYPE))
			{
				longTermDataDAO.deleteLTRRecordsWithFileNames(faultFileNames);
			}
			else if(dataType.equalsIgnoreCase(M9kConstants.CONTINUOUS_ANALOG_DATA) || dataType.equalsIgnoreCase(M9kConstants.CONTINUOUS_DATA))
			{
				mysqlContinuousComtradeDAO.deleteRecordsWithFileNames(faultFileNames);
			}
		}		
		return booResult;
	}
	// TODO: Since scope is requested one dfr at a time there isn't a seperate class to handle. We should create new class to handle scope like we did for other requests.
	public byte[] processScopeOld(String command)
	{
		M9kStationCommandClient commandClient = null;
		Map<String,StringBuffer> mapDfrCommands = null;
		byte[] scopeData = null;
		byte[][] stationScope=null;
			try
			{
				String ipAddress;
				StringBuffer dfrCommand;
				int i =0;
				if (getLstDfrs() == null || getLstDfrs().isEmpty())
				{
					lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
				}
				mapDfrCommands = getDfrScopeCommands(command,getLstDfrs());
				stationScope = new byte[mapDfrCommands.size()][];
				for (Iterator<String> dfrIterator = mapDfrCommands.keySet().iterator(); dfrIterator.hasNext();) {
					ipAddress = dfrIterator.next();
					dfrCommand = mapDfrCommands.get(ipAddress);
					commandClient = new M9kStationCommandClient(ipAddress, 9978, dfrCommand.toString());
					scopeData = commandClient.sendAndReceiveBinary();
					// TODO: When we need scope data from more than one DFR we need to merge these data in stationScope variable and return the merged data
					stationScope[i++]=scopeData;
				}
				logger.debug("Result after data fetch "+scopeData.length);
			}
			catch (M9000Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return scopeData;
	}
	
	private Map<String,StringBuffer> getDfrScopeCommands(String command, List<DfrDTO>lstDfrs)
	{
		String ipAddress="";
		Integer currentChannel=0;
		Map<String,StringBuffer> mapScopeCmd = new HashMap<String, StringBuffer>();
		String channels[]=command.substring(command.indexOf("ch")).split(",");
		String prefixCommand=command.substring(0, command.indexOf("ch=")+3);
		logger.debug("Total Channels requested "+channels.length);
		int newChannelIndex;
		logger.debug("\t\t\t\t\t%&^******************************** Channel no from substring "+command+" channel "+currentChannel);
		for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
			DfrDTO dfrDTO = iterator.next();
			ipAddress = dfrDTO.getIpAddress().trim();
			for (int i = 0; i < channels.length; i++) {
				currentChannel = Integer.parseInt(channels[i].substring(channels[i].indexOf("=")+1));
				if (dfrDTO.getAnalogChannelStart() <= currentChannel && dfrDTO.getAnalogChannelEnd() >= currentChannel)
				{
					newChannelIndex = currentChannel - dfrDTO.getAnalogChannelStart()+1;
					if (mapScopeCmd.containsKey(ipAddress))
					{
						mapScopeCmd.get(ipAddress).append(",ch="+newChannelIndex);
					}
					else
					{
						mapScopeCmd.put(ipAddress, new StringBuffer(prefixCommand + newChannelIndex));
					}
				}				
			}
		}
		return mapScopeCmd;
	}

	public List<DfrDTO> getLstDfrs() {
		return lstDfrs;
	}

	public void setLstDfrs(List<DfrDTO> lstDfrs) {
		this.lstDfrs = lstDfrs;
	}

	/**
	 * 
	 * @return
	 */
	public String getStationMasterComputerType() {
		
		return M9kStationUtil.getStationMasterComputerType();
	}
	
	

}
