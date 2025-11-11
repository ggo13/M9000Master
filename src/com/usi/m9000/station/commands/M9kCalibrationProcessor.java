package com.usi.m9000.station.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.configuration.ConfigurationException;

import com.usi.m9000.dao.MySqlDfrDetailsDAO;
import com.usi.m9000.dao.MySqlReportDAO;
import com.usi.m9000.dto.CalibrationDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.M9kLED;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.threads.M9kCalibrationThreads;
import com.usi.m9000.station.util.LedState;
import com.usi.m9000.station.util.M9kKeyValuePair;
import com.usi.m9000.station.util.M9kSeverityLevels;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;

public class M9kCalibrationProcessor {
//private int noOfTimes = 1;
M9kStationCommandClient commandClient;
//static HierarchicalINIConfiguration iniConf;
private final static String command = "CALIBRATE";

private StringBuffer errorMessage = null;
String calReportPath = "/data/m9k/calibration-report";
static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kCalibrationProcessor.class);
private String strCalHeader = "";
private String strCalVerifyHeader = "";
private StringBuffer stationBuffer;
List<DfrDTO> lstDfrs;
CalibrationDTO calibrationDTO = null;
private static String reportFileNamePrefix;
private static String actionName;
MySqlDfrDetailsDAO mySqlDfrDetailsDAO;
MySqlReportDAO mySqlReportDAO;
double gainTolerance = 0.25; // In terms of percentage
int ofsTolerance = 40; // In terms of integer

public M9kCalibrationProcessor() {
	super();
	try {
//		iniConf = new HierarchicalINIConfiguration("station.properties");
		stationBuffer = new StringBuffer();
		stationBuffer.append("Station Id: \t"+M9kStationDBUtil.getStationDetails().getSystemStationId()+M9kStationConstants.NEWLINE);
		stationBuffer.append("Station Name: \t"+M9kStationDBUtil.getStationDetails().getSystemStationName()+M9kStationConstants.NEWLINE+M9kStationConstants.NEWLINE);
		strCalHeader = stationBuffer.toString();
		try {
			strCalHeader += M9kStationUtil.getStringFromFile("calibration-header-desc.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.warn("Trouble reading calibration header from txt file. Using default");
			strCalHeader += M9kStationUtil.getDefaultCalibrationHeader();
		}
		strCalHeader+=M9kStationConstants.NEWLINE;
		strCalHeader+=M9kStationConstants.NEWLINE;
		strCalVerifyHeader = stationBuffer.toString();
		try {
			strCalVerifyHeader += M9kStationUtil.getStringFromFile("calibration-verify-desc.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.warn("Trouble reading calibration header from txt file. Using default");
			strCalVerifyHeader += M9kStationUtil.getDefaultCalVerificationHeader();
		}
		strCalVerifyHeader+=M9kStationConstants.NEWLINE;
		strCalVerifyHeader+=M9kStationConstants.NEWLINE;
		logger.debug("strCalHeader to be updated "+strCalHeader);
		lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
		mySqlDfrDetailsDAO = new MySqlDfrDetailsDAO();
		mySqlReportDAO = new MySqlReportDAO();
		
	} catch (ConfigurationException e) {
		// TODO Auto-generated catch block
		logger.error("Exception in calibration processor class",e);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		logger.error("Exception in calibration processor class",e);
	}
}

public CalibrationDTO calibrateNow() throws Exception
{
	logger.debug("CALIBRATE: Entered calibrateNow method");
	reportFileNamePrefix="Cal-Report-";
	actionName="CALIBRATION ";
	boolean booComplete = true;
	errorMessage = null;
	int dfrCount = 0;
	List<DfrDTO> lstDfrs;
	DfrDTO dfrDTO = null;
//	Set<String> dfrs = iniConf.getSections();
	ExecutorService executor;
	List<Future<M9kKeyValuePair>> lstFutureResults;
	String ipAddress = null;
	String commandToSend;
	int port = 0;
	
	lstDfrs = M9kStationXMLUtil.getListOfDFRs();
	executor = Executors.newFixedThreadPool(lstDfrs.size());
	lstFutureResults = new ArrayList<Future<M9kKeyValuePair>>(lstDfrs.size());
	calReportPath = M9kStationUtil.getCalReportPath();
	M9kStationUtil.setLedsForAction("All DFRs are temporarily offline for calibration");
//	for (String dfr : dfrs) {
	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
		dfrDTO = iterator.next();
//		if (!dfr.toUpperCase().startsWith("DFR"))
//		{
//			if (dfr.equalsIgnoreCase("calibration"))
//			{
//				try
//				{
//					calReportPath = iniConf.getString(dfr + ".cal-report-path");
//				}
//				catch (Exception e) {
//					calReportPath = "/data/M9K/calibration-report";
//					logger.warn("Trouble in reading calibration report file path from properties file. Creating default at /data/M9K/calibration-report ",e);
//				}
//			}
//			continue;
//		}
		dfrCount++;
//		System.out.println("[" + dfr + "]");
//		ipAddress = iniConf.getString(dfr + ".ip-address");
		ipAddress = dfrDTO.getIpAddress();
//		port = iniConf.getInt(dfr + ".port");
		port = M9kStationUtil.getPort();
//		System.out.println("Id " + iniConf.getString(dfr + ".id"));
//		System.out.println("Ip address " + ipAddress);
//		System.out.println("Port " + port);

//		commandToSend = command + ", Times=" +getNoOfTimes();
		commandToSend = command ;
//		executor.execute(new M9kCommandThreads(ipAddress, port, commandToSend));
		logger.debug("CALIBRATE: In calibrateNow method, invoking new thread for "+dfrDTO.getDfrName()+" ip "+ipAddress+" cmd "+commandToSend);
		lstFutureResults.add(executor.submit(new M9kCalibrationThreads(dfrDTO.getDfrName(), ipAddress, port, commandToSend)));
	}
	
	List<M9kKeyValuePair> lstCalibrationStatus= new ArrayList<M9kKeyValuePair>(lstFutureResults.size());
	for (Iterator<Future<M9kKeyValuePair>> iterator = lstFutureResults.iterator(); iterator
			.hasNext();) {
		Future<M9kKeyValuePair> future = iterator.next();
		try {
			logger.debug("CALIBRATE: About to get from the future ");
			lstCalibrationStatus.add(future.get(3, TimeUnit.MINUTES));
			logger.debug("CALIBRATE: Result from dfr added "+lstCalibrationStatus);
		} catch (InterruptedException e) {
			logger.error("Error waiting for the calibration "+future,e);
		} catch (ExecutionException e) {
			logger.error("Error waiting for the calibration "+future,e);
		} catch (TimeoutException e) {
			logger.error("Waited too long for the calibration "+future,e);
		} catch (Exception e) {
				logger.error("Error in notification queue. Probably local JMS server is down.");
		}
		
		
	}
	executor.shutdown();

	try {
		booComplete = executor.awaitTermination(4, TimeUnit.MINUTES);
		if (!booComplete)
		{
			logger.warn("DFR may be down as it takes too long to respond for the caliberation request.");
			List<Runnable> listUnfinishedProcess = executor.shutdownNow();
			for (Iterator<Runnable> iterator = listUnfinishedProcess.iterator(); iterator
					.hasNext();) {
				M9kCalibrationThreads runnable = (M9kCalibrationThreads) iterator.next();
				logger.error("DFR "+runnable.getIpAddress()+" cannot be calibrated or timed out");
				logger.debug("DFR pending calibration "+runnable.getIpAddress());
				
			}
		}
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		logger.error("Error in calibration ",e);
	}
//		while(!executor.isTerminated())
//		{
//			logger.debug("Awaiting calibration for all dfrs");
//		}
	
	logger.debug("CALIBRATE: DFR count "+dfrCount);
	M9kLED.setCalibrating(false);
	calibrationDTO = getCalibrationReport(lstCalibrationStatus);
	if (lstCalibrationStatus.size() != dfrCount && !calibrationDTO.getCalStatus().equalsIgnoreCase(M9kStationConstants.FAIL))
	{
		calibrationDTO.setCalStatus("FAIL");
		calibrationDTO.setCalStatusMsg("One of the chassis did not calibrate successfully ");
		if (!calibrationDTO.getCalReport().toString().equalsIgnoreCase("No report generated"))
		{
			reportFileNamePrefix="Cal-Error-Report-";
			createCalReport(calibrationDTO);
		}

	}
	else if (calibrationDTO.getCalStatus().equalsIgnoreCase(M9kStationConstants.FAIL) )
	{
		if (getErrorMessage() != null)
		{
			calibrationDTO.setCalStatusMsg(getErrorMessage().toString());
		}
		else
		{
			calibrationDTO.setCalStatusMsg("Calibration failed due to unexpected reason. Check station log for more details.");
		}
		if (!calibrationDTO.getCalReport().toString().equalsIgnoreCase("No report generated"))
		{
			reportFileNamePrefix="Cal-Error-Report-";
			createCalReport(calibrationDTO);
		}

	}
	else if (calibrationDTO.getCalStatus().equalsIgnoreCase(M9kStationConstants.PASS) )
	{
		calibrationDTO.setCalStatusMsg("Calibration Successful");
	}
//	if (!calibrationDTO.getCalReport().toString().equalsIgnoreCase("No report generated"))
//	{
//		createCalReport(calibrationDTO);
//	}
	return calibrationDTO;

}



private void createCalReport(CalibrationDTO calibrationDTO2) {
	logger.debug("CALIBRATE: Entered createCalReportFile "+calibrationDTO2);
	M9kSeverityLevels severityLevel;
	String calFileName = getNewFileName(); //"calReport" + calibrationDTO2.getCalTime()+".txt";
	File calibrationDir = new File(calReportPath);
	StringBuffer statusPrefix = new StringBuffer();
	if (!calibrationDir.exists())
	{
		calibrationDir.mkdirs();
	}
	calibrationDir.setWritable(true, false);
	calibrationDir.setExecutable(true,false);
	calibrationDir.setReadable(true, false);

	BufferedWriter bwCal = null;
	File calibrationFile = new File(calReportPath+System.getProperty("file.separator")+calFileName);
	try {
		
		bwCal = new BufferedWriter(new FileWriter(calibrationFile));
//		if (calibrationDTO2.getCalStatus().toUpperCase().equalsIgnoreCase("PASS"))
//		{
//			bwCal.write(actionName+" Successful.");
//		}
//		else
//		{
//			bwCal.write(actionName+" Failed. "+calibrationDTO2.getCalStatusMsg().toString());
//		}
//		bwCal.write(M9kStationConstants.NEWLINE);
//		bwCal.write("You can view the report in "+calibrationFile.getAbsolutePath()+ " created at "+getCurrentTime());
//		bwCal.write(M9kStationConstants.NEWLINE);
		if (calibrationDTO2.getCalStatus().toUpperCase().equalsIgnoreCase("PASS"))
		{
			statusPrefix.append(actionName+" Successful.");
			severityLevel=M9kSeverityLevels.INFO;
		}
		else
		{
			statusPrefix.append(actionName+" Failed. "+calibrationDTO2.getCalStatusMsg().toString());
			severityLevel=M9kSeverityLevels.ERROR;
		}
		statusPrefix.append(M9kStationConstants.NEWLINE);
		statusPrefix.append(M9kStationConstants.NEWLINE);
		statusPrefix.append("You can view the report in "+calibrationFile.getAbsolutePath());
		statusPrefix.append(M9kStationConstants.NEWLINE);
		statusPrefix.append(M9kStationConstants.NEWLINE);
		calibrationDTO2.getCalReport().insert(0, statusPrefix);
		bwCal.write(Calendar.getInstance().getTime().toString());
		bwCal.write(M9kStationConstants.NEWLINE);
		bwCal.write(calibrationDTO2.getCalReport().toString());
		bwCal.close();
		M9kStationUtil.resetLeds(severityLevel,calibrationDTO2.getCalReport().toString());
	} catch (IOException e) {
		logger.error("Error in creating a calibration report file ",e);
	} catch (Exception e) {
		logger.error("Error in creating a calibration report file ",e);
	}
	finally
	{
		M9kLED.setCalibrating(false);
		if (bwCal != null)
		{
			try {
				bwCal.close();
			} catch (IOException e) {
				
			}
		}
	}
	
	
}

public void insertIntoCalReportTable(CalibrationDTO calibrationDTO) {
	mySqlReportDAO.insertIntoReportTable(calibrationDTO);	
}

public String getLastVerifiedDate() {
	return mySqlReportDAO.getLastVerifedDate();
}
public String getLastCalDate() {
	return mySqlReportDAO.getLastCalibratedDate();
}

private String getNewFileName() {
	String calFile;
	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd-yyyy_HH_mm_ss");
	Calendar cal = Calendar.getInstance();
	calFile =reportFileNamePrefix +  sdf.format(cal.getTime())+".txt";
	return calFile;
}

private CalibrationDTO getCalibrationReport(List<M9kKeyValuePair> lstCalibrationStatus) {
	CalibrationDTO calibrationDTO = new CalibrationDTO();
	calibrationDTO.setCalAction(M9kStationConstants.CALIBRATE);
//	int index = 0;
	Collections.sort(lstCalibrationStatus, new Comparator<M9kKeyValuePair>() {
		@Override
		public int compare(M9kKeyValuePair arg0, M9kKeyValuePair arg1) {
			String key1 = arg0.getKey();
			String key2 = arg1.getKey();
			int lhs = Integer.parseInt(key1.substring(key1.indexOf("DFR")+3));
			int rhs = Integer.parseInt(key2.substring(key2.indexOf("DFR")+3));
//			return ((lhs < rhs)?0:1) ;
			return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
		}
	});
	logger.debug("CALIBRATE: lstCalibrationStatus "+lstCalibrationStatus);
	calibrationDTO.setCalStatus(M9kStationConstants.PASS);
	for (Iterator<M9kKeyValuePair> iterator = lstCalibrationStatus.iterator(); iterator
			.hasNext();) {
		M9kKeyValuePair m9kKeyValuePair =  iterator.next();
		logger.debug("CALIBRATE: m9kKeyValuePair Key "+m9kKeyValuePair.getKey());
		logger.debug("CALIBRATE: m9kKeyValuePair value "+m9kKeyValuePair.getValue());
		if (m9kKeyValuePair.getValue().startsWith(M9kStationConstants.FAIL))
		{
			calibrationDTO.setCalStatus(M9kStationConstants.FAIL);
			if (errorMessage == null)
			{
				errorMessage = new StringBuffer();
			}
//			else
//			{
//				errorMessage.append(", "+m9kKeyValuePair.getKey());
//			}
			errorMessage.append("There was an issue in calibrating chassis: "+m9kKeyValuePair.getKey());
			errorMessage.append(M9kStationConstants.NEWLINE);
//			index = m9kKeyValuePair.getValue().indexOf(M9kStationConstants.FAIL); 
			if (calibrationDTO.getCalReport().length() == 0)
			{
				calibrationDTO.getCalReport().append(strCalHeader);
			}
//			if (index != -1)
//			{
			calibrationDTO.getCalReport().append(M9kStationConstants.NEWLINE);
				calibrationDTO.getCalReport().append(m9kKeyValuePair.getKey()+" Status "+m9kKeyValuePair.getValue());
				calibrationDTO.getCalReport().append(M9kStationConstants.NEWLINE);
//			}
//			else
//			{
//				calibrationDTO.getCalReport().append(m9kKeyValuePair.getValue());
//			}
		}
		else if (m9kKeyValuePair.getValue().startsWith(M9kStationConstants.PASS) || m9kKeyValuePair.getValue().toUpperCase().indexOf("SER ONLY") != -1)
		{
//			calibrationDTO.setCalStatus(M9kStationConstants.PASS);
			if (calibrationDTO.getCalReport().length() == 0)
			{
				calibrationDTO.getCalReport().append(strCalHeader);
			}
			calibrationDTO.getCalReport().append(M9kStationConstants.NEWLINE);
			calibrationDTO.getCalReport().append(m9kKeyValuePair.getKey()+" Status "+m9kKeyValuePair.getValue());
			calibrationDTO.getCalReport().append(M9kStationConstants.NEWLINE);
		}
		else
		{
			calibrationDTO.setCalStatus(M9kStationConstants.FAIL);
			if (errorMessage == null)
			{
				errorMessage = new StringBuffer();
			}
			else
			{
				errorMessage.append(M9kStationConstants.NEWLINE);
			}
			errorMessage.append("Chassis "+m9kKeyValuePair.getKey()+" failed. Reason: "+m9kKeyValuePair.getValue());

		}
	}
	if (calibrationDTO.getCalReport().length() == 0)
	{
		calibrationDTO.getCalReport().append("No report generated");
	}
	return calibrationDTO;
}

//private void send(String destIp, int port, String command) throws M9000Exception {
//	
//	commandClient = new M9kStationCommandClient(destIp, port, command);
//	commandClient.sendAndReceive();
//}


public StringBuffer getErrorMessage() {
	return errorMessage;
}

public void setErrorMessage(StringBuffer errorMessage) {
	this.errorMessage = errorMessage;
}

public String getStrCalHeader() {
	return strCalHeader;
}

public void setStrCalHeader(String strCalHeader) {
	this.strCalHeader = strCalHeader;
}

public String processExternalCalibration(String extCalCommand)
{
	String extCalStatusMessage = "External Cal factors applied";
	String ipAddress;
	int dfrId;
	StringBuffer calibResult = new StringBuffer();
	String processedCommand = null;
	logger.debug("EXTERNAL-CALIBRATE: entered processExternalCalibration "+extCalCommand);
	ipAddress = extCalCommand.substring(0,extCalCommand.indexOf(","));
	logger.debug("Ip address derived "+ipAddress);
	dfrId = Integer.parseInt(ipAddress.substring(ipAddress.lastIndexOf(".")+2));
	processedCommand = getDfrExternalCalibrationCommands(dfrId, extCalCommand);
	logger.debug("Processed extcal comand "+processedCommand);
	commandClient = new M9kStationCommandClient(ipAddress, M9kStationUtil.getPort(), processedCommand);
		try {
			logger.debug("Command "+processedCommand+" sent to "+ipAddress);
			calibResult.append(commandClient.sendAndReceive());
		} catch (Exception e) {
			logger.error("Error in external calibration process . Command sent "+processedCommand,e);
			try
			{
				calibResult.append("There has been a error in DFR"+Integer.parseInt(ipAddress.substring(11))+"Command sent "+processedCommand);
			} catch (Exception e1) {
				calibResult.append("There has been a error in DFR"+ipAddress+"Command sent "+processedCommand+ " .Reason: "+e1.getMessage());
			}
		
		}

		extCalStatusMessage = "User Applied External Cal factors. Command sent "+processedCommand;
		M9kStationUtil.insertUserActionToAlarmsLog(extCalStatusMessage, LedState.GREEN.name());

		return calibResult.toString();
}

private String getDfrExternalCalibrationCommands(int dfrId, String command)
{
	Integer currentChannel=0;
	String[] commandParams;
	String channels[]= null;
	StringBuffer prefixCommand = null;
	StringBuffer channelParams = null;
	StringBuffer processedCommand = null;
	int newChannelIndex;

	command = command.substring(command.indexOf(",")+1); // Remove IPaddress prefix
	
	commandParams = command.split(",");
	prefixCommand = new StringBuffer(commandParams[0]);
	for (int i = 1; i < commandParams.length; i++) {
		if (commandParams[i].toLowerCase().startsWith("ch"))
		{
			if (channelParams == null)
			{
				channelParams = new StringBuffer(commandParams[i]);
			}
			else
			{
				channelParams.append(","+commandParams[i]);
			}
		}
		else
		{
			prefixCommand.append(","+commandParams[i]);
		}
	}
	channels =channelParams.toString().split(",");
//	int index = command.indexOf("ch");
//	if (index != -1)
//	{
//		channels =command.substring(index).split(",");
//		prefixCommand=command.substring(0, command.indexOf("ch=")+3);
//		logger.debug("Total Channels requested "+channels.length);
//	}
//	else
//	{
//		dfrNameToSend = command.substring(command.indexOf(",")+1);
//	}
	logger.debug("EXTERNAL-CALIBRATE:Actual command "+command );
	logger.debug("EXTERNAL-CALIBRATE:t\t%&^******************************** Channel no from substring "+command+" channel "+currentChannel);
	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
		DfrDTO dfrDTO = iterator.next();
		if (dfrDTO.getDfrId() == dfrId)
		{
			if (channels != null)
			{
				for (int i = 0; i < channels.length; i++) {
					currentChannel = Integer.parseInt(channels[i].substring(channels[i].indexOf("=")+1));
					if (dfrDTO.getAnalogChannelStart() <= currentChannel && dfrDTO.getAnalogChannelEnd() >= currentChannel)
					{
						newChannelIndex = currentChannel - dfrDTO.getAnalogChannelStart()+1;
						if (processedCommand != null)
						{
							processedCommand.append(",ch="+newChannelIndex);
						}
						else
						{
							processedCommand = new StringBuffer(prefixCommand.toString() + ",ch="+newChannelIndex);
						}
					}
					else
					{
						if (processedCommand != null)
						{
							processedCommand.append(",ch="+currentChannel);
						}
						else
						{
							processedCommand = new StringBuffer(prefixCommand.toString() + ",ch=" + currentChannel);
						}
					}				
				}
			}
		}
	}
	logger.debug("EXTERNAL-CALIBRATE:Map of results to be returned "+processedCommand);
	return processedCommand.toString();
}

public List<DfrDTO> getLstDfrs() {
	return lstDfrs;
}


public void setLstDfrs(List<DfrDTO> lstDfrs) {
	this.lstDfrs = lstDfrs;
}

public String applyCalFactors(String command) {
	String ipAddress="";
	boolean booCreateReport = true;
	String result;
	StringBuffer calApplyResult = new StringBuffer();
	calibrationDTO.setCalAction(M9kStationConstants.CALIBRATE_APPLY);
	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
		try
		{
		DfrDTO dfrDTO = iterator.next();
		ipAddress = dfrDTO.getIpAddress().trim();
		commandClient = new M9kStationCommandClient(ipAddress, M9kStationUtil.getPort(), command);
		result = commandClient.sendAndReceive();
		calApplyResult.append(result);
		calApplyResult.append(M9kStationConstants.NEWLINE);
		if (result.toUpperCase().equalsIgnoreCase(M9kStationConstants.FAIL))
		{
			booCreateReport = false;
		}
		}
		catch (Exception e) {
			logger.error("Error in sending external calibration ",e);
			if (calApplyResult.length() > 0)
			{
				calApplyResult.append(M9kStationConstants.NEWLINE);
			}
			try
			{
				calApplyResult.append("FAIL: There has been a error in DFR"+Integer.parseInt(ipAddress.substring(11))+"Command sent "+command);
			} catch (Exception e1) {
				calApplyResult.append("FAIL: There has been a error in DFR"+ipAddress+"Command sent "+command);
			}
		
		}
	}
	if (booCreateReport && calibrationDTO != null)
	{
		createCalReport(calibrationDTO);
	}
	insertIntoCalReportTable(calibrationDTO);
	return calApplyResult.toString();
}


public CalibrationDTO verifyCalibration(String commandToSend) 
{
	logger.debug("VERIFY: Entered verifyCalibration method");
	reportFileNamePrefix="Cal-Verify-Report-";
	actionName="CALIBRATION Verification ";
	boolean booComplete = true;
	errorMessage = null;
	int dfrCount = 0;
	List<DfrDTO> lstDfrs;
	DfrDTO dfrDTO = null;
//	Set<String> dfrs = iniConf.getSections();
	ExecutorService executor;
	List<Future<M9kKeyValuePair>> lstFutureResults;

	String ipAddress = null;
	int port = 0;
	Map<String, Integer> mapDfrsAnalogOffset = mySqlDfrDetailsDAO.getDfrAnalogsOffset();
	Integer analogOffset;
	String cmdChOfs = "";
	String limits="";
	lstDfrs = M9kStationXMLUtil.getListOfDFRs();
	executor = Executors.newFixedThreadPool(lstDfrs.size());
	lstFutureResults = new ArrayList<Future<M9kKeyValuePair>>(lstDfrs.size());
	calReportPath = M9kStationUtil.getCalReportPath();
	gainTolerance = M9kStationUtil.getGainTolerance();
	limits=", gainTolerance="+gainTolerance;
	ofsTolerance = M9kStationUtil.getOfsTolerance();
	limits+=", ofsTolerance="+ofsTolerance;
	M9kStationUtil.setLedsForAction("All DFRs are offline temporarily to verify calibration");
//	for (String dfr : dfrs) {
	try
	{
		for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
			dfrDTO = iterator.next();
	//		if (!dfr.toUpperCase().startsWith("DFR"))
	//		{
	//			if (dfr.equalsIgnoreCase("calibration"))
	//			{
	//				try
	//				{
	//					calReportPath = iniConf.getString(dfr + ".cal-report-path");
	//				}
	//				catch (Exception e) {
	//					calReportPath = "/data/M9K/calibration-report";
	//					logger.warn("Trouble in reading calibration report file path from properties file. Creating default at /data/M9K/calibration-report ",e);
	//				}
	//				try
	//				{
	//					gainTolerance= iniConf.getDouble(dfr + ".gainTolerance");
	//				}
	//				catch (Exception e) {
	//					gainTolerance = 0.25;
	//					logger.warn("Trouble in reading gainTolerance from properties file. Using default value as 0.25 ",e);
	//				}
	//				limits=", gainTolerance="+gainTolerance;
	//
	//				try
	//				{
	//					ofsTolerance= iniConf.getInt(dfr + ".ofsTolerance");
	//				}
	//				catch (Exception e) {
	//					ofsTolerance = 40;
	//					logger.warn("Trouble in reading ofsTolerance from properties file. Using default value as 40 ",e);
	//				}
	//				limits+=", ofsTolerance="+ofsTolerance;
	//				
	//			}
	//			continue;
	//		}
			dfrCount++;
	//		System.out.println("[" + dfr + "]");
	//		ipAddress = iniConf.getString(dfr + ".ip-address");
			ipAddress = dfrDTO.getIpAddress();
	//		port = iniConf.getInt(dfr + ".port");
			port = M9kStationUtil.getPort();
	//		System.out.println("Id " + iniConf.getString(dfr + ".id"));
	//		System.out.println("Ip address " + ipAddress);
	//		System.out.println("Port " + port);
			analogOffset = mapDfrsAnalogOffset.get(dfrDTO.getDfrName());
			logger.debug("Analog offset for dfr "+dfrDTO.getDfrName()+ " is "+analogOffset+" limits to be compared "+limits);
			cmdChOfs = "";
			if (analogOffset != null)
			{
				cmdChOfs=", chOfs="+analogOffset;
			}
	//		executor.execute(new M9kCommandThreads(ipAddress, port, commandToSend));
			logger.debug("VERIFY: In verifyCalibration method, invoking new thread for "+dfrDTO.getDfrName()+" ip "+ipAddress+" cmd "+(commandToSend+limits+cmdChOfs));
			lstFutureResults.add(executor.submit(new M9kCalibrationThreads(dfrDTO.getDfrName(), ipAddress, port, (commandToSend+limits+cmdChOfs))));
		}
		
		List<M9kKeyValuePair> lstCalibrationStatus= new ArrayList<M9kKeyValuePair>(lstFutureResults.size());
		for (Iterator<Future<M9kKeyValuePair>> iterator = lstFutureResults.iterator(); iterator
				.hasNext();) {
			Future<M9kKeyValuePair> future = iterator.next();
			try {
				logger.debug("VERIFY: About to get from the future ");
				lstCalibrationStatus.add(future.get(10, TimeUnit.SECONDS));
				logger.debug("VERIFY: Result from dfr added "+lstCalibrationStatus);
			} catch (InterruptedException e) {
				logger.error("Error waiting for the calibration "+future,e);
			} catch (ExecutionException e) {
				logger.error("Error waiting for the calibration "+future,e);
			} catch (TimeoutException e) {
				logger.error("Waited too long for the calibration "+future,e);
			} catch (Exception e) {
					logger.error("Error in notification queue. Probably local JMS server is down.");
			}
			
			
		}
		executor.shutdown();
	
		try {
			booComplete = executor.awaitTermination(1, TimeUnit.MINUTES);
			if (!booComplete)
			{
				logger.warn("DFR may be down as it takes too long to respond for the caliberation request.");
				List<Runnable> listUnfinishedProcess = executor.shutdownNow();
				for (Iterator<Runnable> iterator = listUnfinishedProcess.iterator(); iterator
						.hasNext();) {
					M9kCommandThreads runnable = (M9kCommandThreads) iterator.next();
					logger.error("DFR "+runnable.getIpAddress()+" cannot be calibrated or timed out");
					logger.debug("DFR pending calibration "+runnable.getIpAddress());
					
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in calibration ",e);
		}
	//		while(!executor.isTerminated())
	//		{
	//			logger.debug("Awaiting calibration for all dfrs");
	//		}
		
		logger.debug("Verify: DFR count "+dfrCount);
		calibrationDTO = getCalVerificationReport(lstCalibrationStatus);
		if (lstCalibrationStatus.size() != dfrCount && !calibrationDTO.getCalStatus().equalsIgnoreCase(M9kStationConstants.FAIL))
		{
			calibrationDTO.setCalStatus("FAIL");
			calibrationDTO.setCalStatusMsg("Could not verify one of the chassis");
			reportFileNamePrefix="Cal-Verify-Error-Report-";
			logger.error("Calibration Verification failed. One of the chassis could not be verified");
		}
		else if (calibrationDTO.getCalStatus().equalsIgnoreCase(M9kStationConstants.FAIL) )
		{
			if (getErrorMessage() != null)
			{
				calibrationDTO.setCalStatusMsg(getErrorMessage().toString());
				logger.error("Calibration Verification failed. "+getErrorMessage() );
			}
			else
			{
				calibrationDTO.setCalStatusMsg("Calibration verification failed due to unexpected reason. Check station log for more details.");
				logger.error("Calibration Verification failed due to unexpected reason.Check station log for more details.");
			}
			reportFileNamePrefix="Cal-Verify-Error-Report-";
	
		}
		else if (calibrationDTO.getCalStatus().equalsIgnoreCase(M9kStationConstants.PASS) )
		{
			calibrationDTO.setCalStatusMsg("Calibration Verification Successful");
			logger.info("Calibration Verification Successful.");
			
		}
		if (!calibrationDTO.getCalReport().toString().equalsIgnoreCase("No report generated"))
		{
			createCalReport(calibrationDTO);
		}
		logger.info("Calibration Verification report"+M9kStationConstants.NEWLINE+ calibrationDTO.getCalReport());

	}
	catch (Exception e) {
		logger.error("Exception occured during verification process",e);
	}
	finally
	{
		M9kLED.setCalibrating(false);
	}
	return calibrationDTO;

}

private CalibrationDTO getCalVerificationReport(List<M9kKeyValuePair> lstCalibrationStatus) {
	CalibrationDTO calibrationDTO = new CalibrationDTO();
	calibrationDTO.setCalAction(M9kStationConstants.CALIBRATE_VERIFY);
//	int index = 0;
	Collections.sort(lstCalibrationStatus, new Comparator<M9kKeyValuePair>() {
		@Override
		public int compare(M9kKeyValuePair arg0, M9kKeyValuePair arg1) {
			String key1 = arg0.getKey();
			String key2 = arg1.getKey();
			int lhs = Integer.parseInt(key1.substring(key1.indexOf("DFR")+3));
			int rhs = Integer.parseInt(key2.substring(key2.indexOf("DFR")+3));
//			return ((lhs < rhs)?0:1) ;
			return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
		}
	});
	logger.debug("VERIFY: lstCalibrationStatus "+lstCalibrationStatus);
	calibrationDTO.setCalStatus(M9kStationConstants.PASS);
	for (Iterator<M9kKeyValuePair> iterator = lstCalibrationStatus.iterator(); iterator
			.hasNext();) {
		M9kKeyValuePair m9kKeyValuePair =  iterator.next();
		logger.debug("VERIFY: m9kKeyValuePair Key "+m9kKeyValuePair.getKey());
		logger.debug("VERIFY: m9kKeyValuePair value "+m9kKeyValuePair.getValue());
		if (m9kKeyValuePair.getValue().startsWith(M9kStationConstants.FAIL))
		{
			calibrationDTO.setCalStatus(M9kStationConstants.FAIL);
			if (errorMessage == null)
			{
				errorMessage = new StringBuffer();
			}
//			else
//			{
//				errorMessage.append(", "+m9kKeyValuePair.getKey());
//			}
			errorMessage.append("There was an issue in cal varification process in chassis: "+m9kKeyValuePair.getKey());
			errorMessage.append(M9kStationConstants.NEWLINE);
//			index = m9kKeyValuePair.getValue().indexOf(M9kStationConstants.FAIL); 
			if (calibrationDTO.getCalReport().length() == 0)
			{
				calibrationDTO.getCalReport().append(strCalVerifyHeader);
			}
//			if (index != -1)
//			{
			calibrationDTO.getCalReport().append(M9kStationConstants.NEWLINE);
				calibrationDTO.getCalReport().append(m9kKeyValuePair.getKey()+" Status "+m9kKeyValuePair.getValue());
				calibrationDTO.getCalReport().append(M9kStationConstants.NEWLINE);
//			}
//			else
//			{
//				calibrationDTO.getCalReport().append(m9kKeyValuePair.getValue());
//			}
		}
		else if (m9kKeyValuePair.getValue().startsWith(M9kStationConstants.PASS))
		{
//			calibrationDTO.setCalStatus(M9kStationConstants.PASS);
			if (calibrationDTO.getCalReport().length() == 0)
			{
				calibrationDTO.getCalReport().append(strCalVerifyHeader);
			}
			calibrationDTO.getCalReport().append(M9kStationConstants.NEWLINE);
			calibrationDTO.getCalReport().append(m9kKeyValuePair.getKey()+" Status "+m9kKeyValuePair.getValue());
			calibrationDTO.getCalReport().append(M9kStationConstants.NEWLINE);
		}
		else
		{
			calibrationDTO.setCalStatus(M9kStationConstants.FAIL);
			if (errorMessage == null)
			{
				errorMessage = new StringBuffer();
			}
			else
			{
				errorMessage.append(M9kStationConstants.NEWLINE);
			}
			errorMessage.append("Chassis "+m9kKeyValuePair.getKey()+" failed. Reason: "+m9kKeyValuePair.getValue());

		}
	}
	if (calibrationDTO.getCalReport().length() == 0)
	{
		calibrationDTO.getCalReport().append("No report generated");
	}
	return calibrationDTO;
}


public CalibrationDTO getCalibrationDTO() {
	return calibrationDTO;
}

public void setCalibrationDTO(CalibrationDTO calibrationDTO) {
	this.calibrationDTO = calibrationDTO;
}

}
