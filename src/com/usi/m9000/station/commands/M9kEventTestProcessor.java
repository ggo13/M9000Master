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
import com.usi.m9000.station.util.M9kKeyValuePair;
import com.usi.m9000.station.util.M9kSeverityLevels;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;

public class M9kEventTestProcessor {
M9kStationCommandClient commandClient;
//static HierarchicalINIConfiguration iniConf;

private StringBuffer errorMessage = null;
String eventtestReportPath = "/data/m9k/eventtest-report";
static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kEventTestProcessor.class);
private String strEventTestHeader = "";
private StringBuffer stationBuffer;
List<DfrDTO> lstDfrs;
CalibrationDTO calibrationDTO = null;
private static String reportFileNamePrefix;
private static String actionName;
MySqlDfrDetailsDAO mySqlDfrDetailsDAO;
MySqlReportDAO mySqlReportDAO;

public M9kEventTestProcessor() {
	super();
	try {
//		iniConf = new HierarchicalINIConfiguration("station.properties");
		stationBuffer = new StringBuffer();
		stationBuffer.append("Station Id: \t"+M9kStationDBUtil.getStationDetails().getSystemStationId()+M9kStationConstants.NEWLINE);
		stationBuffer.append("Station Name: \t"+M9kStationDBUtil.getStationDetails().getSystemStationName()+M9kStationConstants.NEWLINE+M9kStationConstants.NEWLINE);
		strEventTestHeader = stationBuffer.toString();
		try {
			strEventTestHeader += M9kStationUtil.getStringFromFile("eventtest-header-desc.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.warn("Trouble reading calibration header from txt file. Using default");
			strEventTestHeader += M9kStationUtil.getDefaultEventTestHeader();
		}
		strEventTestHeader+=M9kStationConstants.NEWLINE;
		strEventTestHeader+=M9kStationConstants.NEWLINE;
		logger.debug("strEventTestHeader to be updated "+strEventTestHeader);
		lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
		mySqlDfrDetailsDAO = new MySqlDfrDetailsDAO();
		mySqlReportDAO = new MySqlReportDAO();
		
	} catch (ConfigurationException e) {
		// TODO Auto-generated catch block
		logger.error("Exception in Event Test processor class",e);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		logger.error("Exception in Event Test processor class",e);
	}
}

public CalibrationDTO eventTestNow(String commandToSend) throws Exception
{
	logger.debug("EVENTTEST: Entered eventTestNow method");
	reportFileNamePrefix="EventTest-Report-";
	actionName="EVENTTEST ";
	boolean booComplete = true;
	errorMessage = null;
	int dfrCount = 0;
	List<DfrDTO> lstDfrs;
	DfrDTO dfrDTO = null;
	ExecutorService executor;
	List<Future<M9kKeyValuePair>> lstFutureResults;
	String ipAddress = null;
	int port = 0;
	
	lstDfrs = M9kStationXMLUtil.getListOfDFRs();
	executor = Executors.newFixedThreadPool(lstDfrs.size());
	lstFutureResults = new ArrayList<Future<M9kKeyValuePair>>(lstDfrs.size());
	eventtestReportPath = M9kStationUtil.getEventTestReportPath();
	logger.debug("EVENTTEST:"+eventtestReportPath);
	M9kStationUtil.setLedsForAction("All DFRs with events are offline temporarily to perform event test");
	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
		dfrDTO = iterator.next();
		dfrCount++;
		ipAddress = dfrDTO.getIpAddress();
		port = M9kStationUtil.getPort();
		logger.debug("EventTest: In eventTestNow method, invoking new thread for "+dfrDTO.getDfrName()+" ip "+ipAddress+" cmd "+commandToSend);
		lstFutureResults.add(executor.submit(new M9kCalibrationThreads(dfrDTO.getDfrName(), ipAddress, port, commandToSend)));
	}
	
	List<M9kKeyValuePair> lstCalibrationStatus= new ArrayList<M9kKeyValuePair>(lstFutureResults.size());
	for (Iterator<Future<M9kKeyValuePair>> iterator = lstFutureResults.iterator(); iterator
			.hasNext();) {
		Future<M9kKeyValuePair> future = iterator.next();
		try {
			logger.debug("EventTest: About to get from the future ");
			lstCalibrationStatus.add(future.get(3, TimeUnit.MINUTES));
			logger.debug("EventTest: Result from dfr added "+lstCalibrationStatus);
		} catch (InterruptedException e) {
			logger.error("Error waiting for the eventtest "+future,e);
		} catch (ExecutionException e) {
			logger.error("Error waiting for the eventtest "+future,e);
		} catch (TimeoutException e) {
			logger.error("Waited too long for the eventtest "+future,e);
		} catch (Exception e) {
				logger.error("Error in notification queue. Probably local JMS server is down.");
		}
		
		
	}
	executor.shutdown();

	try {
		booComplete = executor.awaitTermination(30, TimeUnit.SECONDS);
		if (!booComplete)
		{
			logger.warn("DFR may be down as it takes too long to respond for the EventTest request.");
			List<Runnable> listUnfinishedProcess = executor.shutdownNow();
			for (Iterator<Runnable> iterator = listUnfinishedProcess.iterator(); iterator
					.hasNext();) {
				M9kCommandThreads runnable = (M9kCommandThreads) iterator.next();
				logger.error("Unable to test the event boards in DFR "+runnable.getIpAddress()+" or it timed out");
//				System.out.println("DFR pending calibration "+runnable.getIpAddress());
				
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
	
	logger.debug("EventTest: DFR count "+dfrCount);
	calibrationDTO = getEventTestReport(lstCalibrationStatus);
	if (lstCalibrationStatus.size() != dfrCount && !calibrationDTO.getCalStatus().equalsIgnoreCase(M9kStationConstants.FAIL))
	{
		calibrationDTO.setCalStatus("FAIL");
		calibrationDTO.setCalStatusMsg("One of the chassis failed event test ");
		if (!calibrationDTO.getCalReport().toString().equalsIgnoreCase("No report generated"))
		{
			reportFileNamePrefix="EventTest-Error-Report-";
//			createEventTestReport(calibrationDTO);
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
			calibrationDTO.setCalStatusMsg("EventTest failed due to unexpected reason. Check station log for more details.");
		}
		if (!calibrationDTO.getCalReport().toString().equalsIgnoreCase("No report generated"))
		{
			reportFileNamePrefix="EventTest-Error-Report-";
//			createEventTestReport(calibrationDTO);
		}

	}
	else if (calibrationDTO.getCalStatus().equalsIgnoreCase(M9kStationConstants.PASS) )
	{
		calibrationDTO.setCalStatusMsg("Event Test Successful");
	}
	if (!calibrationDTO.getCalReport().toString().equalsIgnoreCase("No report generated"))
	{
		createEventTestReport(calibrationDTO);
	}
	return calibrationDTO;

}


private void createEventTestReport(CalibrationDTO calibrationDTO2) {
	logger.debug("EventTest: Entered createCalReportFile "+calibrationDTO2+" eventtestReportPath "+eventtestReportPath);
	M9kSeverityLevels severityLevel;
	String calFileName = getNewFileName(); //"calReport" + calibrationDTO2.getCalTime()+".txt";
	File calibrationDir = new File(eventtestReportPath);
	StringBuffer statusPrefix = new StringBuffer();
	if (!calibrationDir.exists())
	{
		calibrationDir.mkdirs();
		calibrationDir.setWritable(true, false);
		calibrationDir.setExecutable(true,false);
		calibrationDir.setReadable(true, false);

	}
	BufferedWriter bwCal = null;
	File eventTestReportFile = new File(eventtestReportPath+System.getProperty("file.separator")+calFileName);
	try {
		
		bwCal = new BufferedWriter(new FileWriter(eventTestReportFile));
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
		statusPrefix.append("You can view the report in "+eventTestReportFile.getAbsolutePath());
		statusPrefix.append(M9kStationConstants.NEWLINE);
		statusPrefix.append(M9kStationConstants.NEWLINE);
		calibrationDTO2.getCalReport().insert(0, statusPrefix);
		bwCal.write(Calendar.getInstance().getTime().toString());
		bwCal.write(M9kStationConstants.NEWLINE);
		bwCal.write(calibrationDTO2.getCalReport().toString());
		bwCal.close();
		M9kStationUtil.resetLeds(severityLevel, calibrationDTO2.getCalReport().toString());
	} catch (IOException e) {
		logger.error("Error in creating a EventTest report file ",e);
	} catch (Exception e) {
		logger.error("Error in creating a EventTest report file ",e);
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

private CalibrationDTO getEventTestReport(List<M9kKeyValuePair> lstCalibrationStatus) {
	CalibrationDTO calibrationDTO = new CalibrationDTO();
	calibrationDTO.setCalAction(M9kStationConstants.EVENTTEST);
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
	logger.debug("EventTest: lstCalibrationStatus "+lstCalibrationStatus);
	calibrationDTO.setCalStatus(M9kStationConstants.PASS);
	for (Iterator<M9kKeyValuePair> iterator = lstCalibrationStatus.iterator(); iterator
			.hasNext();) {
		M9kKeyValuePair m9kKeyValuePair =  iterator.next();
		logger.debug("EventTest: m9kKeyValuePair Key "+m9kKeyValuePair.getKey());
		logger.debug("EventTest: m9kKeyValuePair value "+m9kKeyValuePair.getValue());
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
			errorMessage.append("There was an issue in chassis: "+m9kKeyValuePair.getKey());
			errorMessage.append(M9kStationConstants.NEWLINE);
//			index = m9kKeyValuePair.getValue().indexOf(M9kStationConstants.FAIL); 
			if (calibrationDTO.getCalReport().length() == 0)
			{
				calibrationDTO.getCalReport().append(strEventTestHeader);
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
				calibrationDTO.getCalReport().append(strEventTestHeader);
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
		M9kStationUtil.resetLeds(M9kSeverityLevels.ERROR,calibrationDTO.getCalReport().toString()+M9kStationConstants.NEWLINE+errorMessage.toString());
	}
	return calibrationDTO;
}

//private void send(String destIp, int port, String command) throws M9000Exception {
//	
//	commandClient = new M9kStationCommandClient(destIp, port, command);
//	commandClient.sendAndReceive();
//}


public String getLastEventTestDate() {
	return mySqlReportDAO.getLastEventTestDate();
}

public StringBuffer getErrorMessage() {
	return errorMessage;
}

public void setErrorMessage(StringBuffer errorMessage) {
	this.errorMessage = errorMessage;
}

public String getStrCalHeader() {
	return strEventTestHeader;
}

public void setStrCalHeader(String strCalHeader) {
	this.strEventTestHeader = strCalHeader;
}


public List<DfrDTO> getLstDfrs() {
	return lstDfrs;
}


public void setLstDfrs(List<DfrDTO> lstDfrs) {
	this.lstDfrs = lstDfrs;
}


public CalibrationDTO getCalibrationDTO() {
	return calibrationDTO;
}

public void setCalibrationDTO(CalibrationDTO calibrationDTO) {
	this.calibrationDTO = calibrationDTO;
}

}
