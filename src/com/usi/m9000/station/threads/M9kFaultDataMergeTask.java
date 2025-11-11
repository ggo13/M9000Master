package com.usi.m9000.station.threads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.DigitalInfo;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.M9kLED;
import com.usi.m9000.station.commands.M9kDFRHealthStatusProcessor;
import com.usi.m9000.station.commands.M9kTriggerProcessor;
import com.usi.m9000.station.dto.ProcessDatDTO;
import com.usi.m9000.station.dto.TriggersScheduleDTO;
import com.usi.m9000.station.faultLocation.M9kFaultLocation;
import com.usi.m9000.station.faultLocation.M9kFaultReport;
import com.usi.m9000.station.util.LedState;
import com.usi.m9000.station.util.M9kSeverityLevels;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationSignatureServer;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;


public class M9kFaultDataMergeTask implements Runnable{
//	static boolean booStatus = true;
	private Connection mysqlConn;
	TriggersScheduleDTO triggersScheduleDTO;
	private ResourceBundle comtradeBundle;
//	 static HierarchicalINIConfiguration iniConf;
	org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kFaultDataMergeTask.class);
//	Map<Integer, List<ProcessDatDTO>> mapDfrs ;
//	Map<Integer, String> mapDfrIPAddress ;
//	Map<Integer, Integer> mapDfrPorts;
	M9kTriggerProcessor m9kTriggerProcessor;
	boolean flagProcess = true;
	
	M9kDFRHealthStatusProcessor m9kDFRHealthStatusProcessor;
	Map<String, Boolean> mapDfrsStatus = null;
	private long startTimer;
	DfrDTO currentDfr; 
	static List<DfrDTO> lstDfrs = null;
	int analogOffset = 1;
	int eventOffset = 1;
	int eventChannelIndex = 1;
	long crossReqWaitTime = 30000;
	private int cfgChnlIndexNumber = 1;
	private M9kStationComtradeUtil m9kStationComtradeUtil;
	private String userComments = "";
	final static String DUMMY_ANALOG_CFG = ",V,1,0,0,-32768,32767,1,10,P";
	static
	{
		try {
			lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
			// Sort ascending order of the dfr id
			Collections.sort(lstDfrs, new Comparator<DfrDTO>() {

				@Override
				public int compare(DfrDTO o1, DfrDTO o2) {
//					return ((o1.getDfrId() < o2.getDfrId())?0:1);
					return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
				}
			});
//			logger.debug("MERGE: lst pf dfr after sort "+lstDfrs);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			org.apache.logging.log4j.LogManager.getLogger(M9kFaultDataMergeTask.class).error("Error in get the list of dfrs from station config",e);
		}
		org.apache.logging.log4j.LogManager.getLogger(M9kFaultDataMergeTask.class).debug("MERGE: lst of dfrs " + lstDfrs);
	}
	public M9kFaultDataMergeTask(TriggersScheduleDTO triggersScheduleDTO)
	{
		logger.debug("Entered M9kFaultDataProcessTask constructor");
		if (triggersScheduleDTO.getStartTime() <= 0 || triggersScheduleDTO.getStopTime() <= 0 || triggersScheduleDTO.getStartTime() >= triggersScheduleDTO.getStopTime() )
		{
			logger.warn("Timelines are messed up. Stopping the thread"+triggersScheduleDTO);
			flagProcess = false;
		}
		comtradeBundle = ResourceBundle.getBundle("M9K_COMTRADE");
		try
		{
			crossReqWaitTime = Integer.parseInt(comtradeBundle.getString("fault-cross-req-wait-time"));
		}
		catch (Exception e) {
			crossReqWaitTime = 30000; // Default milli seconds
		}
		this.triggersScheduleDTO = triggersScheduleDTO;
		  initialize();

	}
	
	private void initialize()
	{
//		mapDfrs = new HashMap<Integer, List<ProcessDatDTO>>();
//		mapDfrIPAddress = new HashMap<Integer, String>();
//		mapDfrPorts = new HashMap<Integer, Integer>();
		
		m9kTriggerProcessor = new M9kTriggerProcessor();
//		Set<String> dfrs = iniConf.getSections();
//		int dfrId = 0;
//		for (String dfr : dfrs) {
//			if (dfr.equalsIgnoreCase("station"))
//			{
//				continue;
//			}
//			dfrId = iniConf.getInt(dfr+".id");
//			mapDfrs.put(dfrId, new ArrayList<ProcessDatDTO>());
//			mapDfrIPAddress.put(dfrId, iniConf.getString(dfr+".ip-address"));
//			mapDfrPorts.put(dfrId,iniConf.getInt(dfr+".port"));
//		}
		triggersScheduleDTO.setSignature(M9kStationSignatureServer.getInstance().getNewSignature());
		m9kDFRHealthStatusProcessor = new M9kDFRHealthStatusProcessor();
		m9kStationComtradeUtil = new M9kStationComtradeUtil();
	}
	public void run()
	{
		if (flagProcess)
		{
			processFaultRecordsFromDat();
		}
		else
		{
    		M9kLED.setTriggerLedWithRelay(LedState.YELLOW);
		}
	}
	
	private int processFaultRecordsFromDat()
	{
		int totalRecordsInserted = 0;
		long start;
		long end;
		List<ProcessDatDTO> lstProcessDatDTO = new ArrayList<ProcessDatDTO>();
		try {
			start = System.currentTimeMillis();
    		// TODO: it has to start wobbling before requesting cross record
//			M9kLED.setTriggerLedWithRelay(LedState.GREEN);


			m9kTriggerProcessor.crossTriggerAll(triggersScheduleDTO);
			lstProcessDatDTO = getDatRecordToProcess();
			end = System.currentTimeMillis();
			logger.debug("Time to execute getDatRecordToProcess "+(end-start));
			
			logger.debug("Total number of dat records to process "+lstProcessDatDTO.size());
//			stmt.setFetchSize(Integer.MIN_VALUE);
			if (lstProcessDatDTO.size() > 0)
			{
				logger.debug("About to process the dat records"+lstProcessDatDTO);
//				verifyIntegrity(lstProcessDatDTO);
				pushProcessedDatToStaging(lstProcessDatDTO);
				
			}
			else
			{
//	    		M9kLED.setLed(LedName.TRIGGER, LedState.YELLOW);
			}
			
		} catch (Exception e) {
//			e.printStackTrace();
			logger.error("Exception occured in processFaultRecordsFromDat ", e.getCause());
		}
		return totalRecordsInserted;
	}
	
//	private void verifyIntegrity(List<ProcessDatDTO> lstProcessDatDTO) {
//		long expectedDataSize;
//		long actualDataSize;
//		for (Iterator<ProcessDatDTO> iterator = lstProcessDatDTO.iterator(); iterator
//				.hasNext();) {
//			ProcessDatDTO processDatDTO = iterator.next();
//			processDatDTO.getLstAnalogInfo();
//		}
//		
//	}

	private void pushProcessedDatToStaging(List<ProcessDatDTO> lstReadyToPushDat)
	{
		PreparedStatement psInsert = null;
		int result = -1;
		ProcessDatDTO mergedProcessDatDto = null;
		ByteArrayInputStream binaryDataInputStream =  null;
		boolean isDisturbanceAlarmOnly = true;
		// START: 15-May-2024 - Change it to set alarms whenever disturbance fault is detected
		boolean isAnyDisturbanceFaultDetected = false;
		// END: 15-May-2024
//		PreparedStatement psDelete = null;
		try {
			logger.debug("DB-POOL New connection from pushProcessedDatToStaging ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			mergedProcessDatDto = processAndMergeFaults(lstReadyToPushDat);
			logger.debug("Final merged data "+mergedProcessDatDto);
			String faultLocationDetails = getFaultLocationDetails(mergedProcessDatDto);
			
			Map<String, DigitalInfo> abnormalEvents = m9kStationComtradeUtil.getActiveEventsId(mergedProcessDatDto);
//			logger.debug("Abnormal events "+abnormalEvents.keySet());
			if (abnormalEvents == null || abnormalEvents.isEmpty())
			{
				logger.info("No abnormal events. Fault location cannot be calculated");
				isDisturbanceAlarmOnly = false;
				// START: 15-May-2024 - Change it to set alarms whenever disturbance fault is detected
				isAnyDisturbanceFaultDetected = false;
				// END: 15-May-2024
			}
			else
			{
				// Check for disturbance alarms list
				isDisturbanceAlarmOnly = M9kStationXMLUtil.isDisturbanceOnlyFault(abnormalEvents);
				// START: 15-May-2024 - Change it to set alarms whenever disturbance fault is detected
				isAnyDisturbanceFaultDetected = M9kStationXMLUtil.isAnyDisturbanceFaultDetected(abnormalEvents);
				// END: 15-May-2024
			}
			
			String associatedLineGroups = m9kStationComtradeUtil.getFaultLogicStatus(mergedProcessDatDto); 
			
			boolean booFautLogic = ((associatedLineGroups == null)?false:true); 
			if (booFautLogic) // IF boolean logic is TRUE enable alarm otherwise disable alarm
			{
				M9kLED.setDisableAlarm(false);
			}
			else
			{
				M9kLED.setDisableAlarm(true);
			}
			if (!isDisturbanceAlarmOnly)
			{
				M9kLED.setTriggerLedWithRelay(LedState.WARBLE);
				// START: 15-May-2024 - Change it to set alarms whenever disturbance fault is detected
				if (isAnyDisturbanceFaultDetected)
				{
					M9kLED.setDisturbanceTriggerLedWithRelay(LedState.WARBLE);
				}
				// END: 15-May-2024	
			}
			else
			{
				M9kLED.setDisturbanceTriggerLedWithRelay(LedState.WARBLE);
			}
			if (mergedProcessDatDto.getData() != null)
			{
				psInsert = mysqlConn.prepareStatement("insert into dat_staging(stationId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, data, faultLocation, faultLogic, comments, lineGroups) " +
	//					"select dfrId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, data from dat a where a.id IN ("+getCommaSeperatedIds(lstReadyToPushDat)+")");
						"values( ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" );
			}
			else
			{
				psInsert = mysqlConn.prepareStatement("insert into dat_staging(stationId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, dataBlob, faultLocation, faultLogic, comments, lineGroups) " +
						//					"select dfrId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, data from dat a where a.id IN ("+getCommaSeperatedIds(lstReadyToPushDat)+")");
											"values( ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" );				
			}
			int iIndex = 1;
//			psInsert.setInt(iIndex++, mergedProcessDatDto.getDfrId());
			psInsert.setInt(iIndex++, M9kStationDBUtil.getStationDetails().getSystemStationId());
			psInsert.setString(iIndex++, mergedProcessDatDto.getType());
			psInsert.setLong(iIndex++, mergedProcessDatDto.getTsPrefault());
			psInsert.setLong(iIndex++, mergedProcessDatDto.getTsTrigger());
			psInsert.setDouble(iIndex++, mergedProcessDatDto.getLineFreq());
			psInsert.setDouble(iIndex++, mergedProcessDatDto.getSampleRate());
			psInsert.setInt(iIndex++, mergedProcessDatDto.getSampleCnt());
			if (mergedProcessDatDto.getAnalogs() != null)
			{
				psInsert.setString(iIndex++, mergedProcessDatDto.getAnalogs().toString());
			}
			else
			{
				psInsert.setString(iIndex++, "");
			}
			
			if (mergedProcessDatDto.getEvents() != null)
			{
				psInsert.setString(iIndex++, mergedProcessDatDto.getEvents().toString());
			}
			else
			{
				psInsert.setString(iIndex++, "");
			}
			if(mergedProcessDatDto.getData() != null)
			{
				psInsert.setString(iIndex++, mergedProcessDatDto.getData().toString());
			}
			else
			{
				binaryDataInputStream = new ByteArrayInputStream(mergedProcessDatDto.getBinaryData().toByteArray());
				psInsert.setBinaryStream(iIndex++, binaryDataInputStream);
			}
			psInsert.setString(iIndex++, faultLocationDetails);
			psInsert.setBoolean(iIndex++, booFautLogic);
			psInsert.setString(iIndex++, getUserComments());
			psInsert.setString(iIndex++, associatedLineGroups);
			result = psInsert.executeUpdate();
				// Remove the ids from the process dat
				
			psInsert.close();
			if (M9kLED.getTriggerDuration() == 0)
			{
				logger.debug("No trigger timer. Manual reset");
				if (!isDisturbanceAlarmOnly)
				{
					M9kLED.resetTriggerStatus();
					// START: 15-May-2024 - Set alarms whenever disturbance fault is detected
					if (isAnyDisturbanceFaultDetected)
					{
						M9kLED.resetDisturbanceAlarmStatus();
					}
					// END: 15-May-2024
				}
				else
				{
					M9kLED.resetDisturbanceAlarmStatus();
				}
			}
//			logger.info("ids from dat "+ getCommaSeperatedIds(lstReadyToPushDat)+" have been pushed to staging");
			if (mergedProcessDatDto != null)
			{
				mergedProcessDatDto.setData(null);
				mergedProcessDatDto = null;
			}			
//			if (result > 0)
//			{
//				psDelete = mysqlConn.prepareStatement("delete from process_dat where datid IN("+getCommaSeperatedIds(lstReadyToPushDat)+")");
//				psDelete.executeUpdate();
//				logger.info("ids from dat "+ getCommaSeperatedIds(lstReadyToPushDat)+" have been deleted from process_dat table");
//				psDelete.close();
//				removeProcessedDatRecord();
//			}
		} catch (SQLException sqle) {
			logger.error("SQl Exception occured in pushProcessedDatToStaging. Stopping the process ", sqle);
			result = -1;
			if (!isDisturbanceAlarmOnly)
			{
				M9kLED.setTriggerLedWithRelay(LedState.YELLOW);
				// START: 15-May-2024 - Set alarms whenever disturbance fault is detected
				if (isAnyDisturbanceFaultDetected)
				{
					M9kLED.setDisturbanceTriggerLedWithRelay(LedState.YELLOW);
				}
				// END: 15-May-2024
			}
			else
			{
				M9kLED.setDisturbanceTriggerLedWithRelay(LedState.YELLOW);
			}
		}
		catch (Exception e) {
			logger.error("Exception occured in pushProcessedDatToStaging. Stopping the proces ", e);
			result = -1;
			if (!isDisturbanceAlarmOnly)
			{
				M9kLED.setTriggerLedWithRelay(LedState.YELLOW);
				// START: 15-May-2024 - Set alarms whenever disturbance fault is detected
				if (isAnyDisturbanceFaultDetected)
				{
					M9kLED.setDisturbanceTriggerLedWithRelay(LedState.YELLOW);
				}
				// END: 15-May-2024
			}
			else
			{
				M9kLED.setDisturbanceTriggerLedWithRelay(LedState.YELLOW);
			}
			
		}
		finally
		{
			if (mergedProcessDatDto != null)
			{
				mergedProcessDatDto.setData(null);
				mergedProcessDatDto = null;
			}
			try {
				System.gc();
				if (binaryDataInputStream != null)
				{
					binaryDataInputStream.close();
					binaryDataInputStream = null;
				}
				if (psInsert != null)
				{
					psInsert.close();
					psInsert= null;
				}
//				if (psDelete != null)
//				{
//					psDelete.close();
//					psDelete= null;
//				}
				if (mysqlConn != null)
				{
					logger.debug("DB-POOL Closing connection from pushProcessedDatToStaging ");
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in cleanup", e);
			} catch (IOException e) {
				logger.warn("Error in cleanup", e);
			}
			if (result == -1)
			{
				moveToErrorDat(getCommaSeperatedIds(lstReadyToPushDat));
			}
			else if (result > 0)
			{
				removeProcessedDatRecords(getCommaSeperatedIds(lstReadyToPushDat));
			}

		}
		
	}
	

	private ProcessDatDTO processAndMergeFaults(List<ProcessDatDTO> lstReadyToPushDat) throws Exception
	{
//		logger.debug("MERGE: lstReadyToPushDat - lst of processDat before sort "+lstReadyToPushDat);
		// Sort ascending order of the dfr id
		Collections.sort(lstReadyToPushDat, new Comparator<ProcessDatDTO>() {

			@Override
			public int compare(ProcessDatDTO o1, ProcessDatDTO o2) {
//				return ((o1.getDfrId() < o2.getDfrId())?0:1);
				return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
			}
		});
//		logger.debug("MERGE: lstReadyToPushDat - lst of processDat after sort "+lstReadyToPushDat);
//		logger.debug("MERGE: lst pf dfr before sort "+lstDfrs);
		String analogs[];
		String events[];
		int i = 1;
		ProcessDatDTO mergedProcessDatDto = null;
		ProcessDatDTO processDatDTO = null;
		String updatedAnalogData;
		String updatedEventData;
		int iActiveDfrIndex = 0;
		for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator
				.hasNext();) {
			currentDfr = iterator.next();
			logger.debug(" mapDfrsStatus "+mapDfrsStatus+" currentDfr "+currentDfr +" DFR DTO "+currentDfr.getDfrId());
//			System.out.println(" mapDfrsStatus "+mapDfrsStatus.get(("DFR"+currentDfr.getDfrId()))+" DFR DTO "+currentDfr.getDfrId());
//			System.out.println("i = "+i+" Is (!mapDfrsStatus.get(dfrDto.getDfrId()) && i==1) "+(!mapDfrsStatus.get(("DFR"+currentDfr.getDfrId())) && i==1));
			
//			if (mergedProcessDatDto != null)
//			{
//				logger.debug("MERGE: the MergedAnalogs at the start "+mergedProcessDatDto.getAnalogs());
//			}
			if (!mapDfrsStatus.get("DFR"+currentDfr.getDfrId())) // current dfr is down
			{
				if (i == 1)// DFR1 is down
				{
//				logger.debug("MERGE: Dummy data for "+currentDfr+" \n value of i: "+i+" active dfr index "+iActiveDfrIndex);
				mergedProcessDatDto = new ProcessDatDTO();
				mergedProcessDatDto.setType(lstReadyToPushDat.get(0).getType());
				mergedProcessDatDto.setTsPrefault(lstReadyToPushDat.get(0).getTsPrefault());
				mergedProcessDatDto.setTsTrigger(lstReadyToPushDat.get(0).getTsTrigger());
				mergedProcessDatDto.setLineFreq(lstReadyToPushDat.get(0).getLineFreq());
				mergedProcessDatDto.setSampleRate(lstReadyToPushDat.get(0).getSampleRate());
				mergedProcessDatDto.setSampleCnt(lstReadyToPushDat.get(0).getSampleCnt());
				mergedProcessDatDto.setAnalogs(new StringBuffer());
				mergedProcessDatDto.setEvents(new StringBuffer());
				if (lstReadyToPushDat.get(0).getType() != null && lstReadyToPushDat.get(0).getType().equalsIgnoreCase(M9kStationConstants.ASCII))
				{
					updateDummyData(mergedProcessDatDto);
				}
				else
				{
					updateDummyBinaryData(mergedProcessDatDto);
				}
//				if (currentDfr.getAnalogChnlCnt() > 0)
//				{
//					updateDummyAnalogDetails(mergedProcessDatDto);
//				}
//				if (currentDfr.getDigitalChnlCnt() > 0)
//				{
//					updateDummyDigitalDetails(mergedProcessDatDto);
//				}
//				mergedProcessDatDto.setData(processDatDTO.getData());
//				processDatDTO.setAnalogOffset(analogOffset);
//				processDatDTO.setEventsOffset(eventOffset);
				}
				else  // DFR other than DFR1 is down
				{
//					logger.debug("MERGE: Dummy data for "+processDatDTO+" \n value of i: "+i+" active dfr index "+iActiveDfrIndex);
					if (mergedProcessDatDto.getType() != null && mergedProcessDatDto.getType().equalsIgnoreCase(M9kStationConstants.ASCII))
					{
						updateDummyData(mergedProcessDatDto);
					}
					else
					{
						updateDummyBinaryData(mergedProcessDatDto);
					}
	//				updateDummyAnalogDetails(mergedProcessDatDto);
	//				updateDummyDigitalDetails(mergedProcessDatDto);
				}
				eventChannelIndex+=currentDfr.getDigitalChnlCnt();
			}
			else
			{
//				logger.debug("MERGE: iActiveDfrIndex "+iActiveDfrIndex+" lstReadyToPushDat "+lstReadyToPushDat);
				processDatDTO = lstReadyToPushDat.get(iActiveDfrIndex++);
//				logger.debug("MERGE: the Analogs at the start "+processDatDTO.getAnalogs());
				if (i == 1)
				{
//					logger.debug("MERGE: Real data for "+processDatDTO+" \n value of i: "+i+" active dfr index "+iActiveDfrIndex);
					mergedProcessDatDto = new ProcessDatDTO();
					mergedProcessDatDto.setType(processDatDTO.getType());
					mergedProcessDatDto.setTsPrefault(processDatDTO.getTsPrefault());
					mergedProcessDatDto.setTsTrigger(processDatDTO.getTsTrigger());
					mergedProcessDatDto.setLineFreq(processDatDTO.getLineFreq());
					mergedProcessDatDto.setSampleRate(processDatDTO.getSampleRate());
					mergedProcessDatDto.setSampleCnt(processDatDTO.getSampleCnt());
					// Start: 15-MAY-2017 - Prefix A was missing for DFR1 channels 
					mergedProcessDatDto.setAnalogs(new StringBuffer());
					processDatDTO.setAnalogOffset(analogOffset);
					processDatDTO.setEventsOffset(eventOffset);
					if(processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
					{
//						mergedProcessDatDto.setAnalogs(processDatDTO.getAnalogs());
						mergedProcessDatDto.getAnalogs().append(updateAnalogOffset(processDatDTO));
					}
//					else
//					{
//						mergedProcessDatDto.setAnalogs(new StringBuffer());
//					}
					// END: 15-MAY-2017
					if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
					{
						updatedEventData = updateEventOffset(processDatDTO);
						mergedProcessDatDto.setEvents(new StringBuffer(updatedEventData));
					}
					else
					{
						mergedProcessDatDto.setEvents(new StringBuffer());
					}
					if (processDatDTO.getType() != null && processDatDTO.getType().equalsIgnoreCase(M9kStationConstants.ASCII))
					{
						updateData(mergedProcessDatDto, processDatDTO);
					}
					else
					{
						updateBinaryData(mergedProcessDatDto, processDatDTO);
					}
				}
				else
				{
//					logger.debug("MERGE: Real data for "+processDatDTO+" \n value of i: "+i+" active dfr index "+iActiveDfrIndex);
					processDatDTO.setAnalogOffset(analogOffset);
					processDatDTO.setEventsOffset(eventOffset);
	//				mergedProcessDatDto.setSampleCnt(mergedProcessDatDto.getSampleCnt()+processDatDTO.getSampleCnt());
	//				logger.debug("Samples count "+mergedProcessDatDto.getSampleCnt());
	
//					logger.debug("Beforeeeee updateData the object "+processDatDTO);
//					logger.debug("MERGE:Before updateData the Analogs "+processDatDTO.getAnalogs());
					if ((processDatDTO.getType() != null && processDatDTO.getType().equalsIgnoreCase(M9kStationConstants.ASCII)))
					{
						updateData(mergedProcessDatDto, processDatDTO);
					}
					else
					{
						updateBinaryData(mergedProcessDatDto, processDatDTO);
					}
					
					try
					{
						if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
						{
							updatedAnalogData = updateAnalogOffset(processDatDTO);
		//					mergedProcessDatDto.getAnalogs().append(M9kStationConstants.NEWLINE);
							mergedProcessDatDto.getAnalogs().append(updatedAnalogData);
						}
		
						if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
						{
							updatedEventData = updateEventOffset(processDatDTO);
		//					mergedProcessDatDto.getEvents().append(M9kStationConstants.NEWLINE);
							if (mergedProcessDatDto.getEvents() == null)
							{
								mergedProcessDatDto.setEvents(new StringBuffer(updatedEventData));
							}
							else
							{
								mergedProcessDatDto.getEvents().append(updatedEventData);
							}
						}
					}
					catch (Exception e) {
//						e.printStackTrace();
						logger.error("Error occured while updating offsets ",e);
						throw e;
					}
					logger.debug("Offsets updated...");
				}
				logger.debug("About to split analogs and events and store it in a string array");
				try
				{
					if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
					{
						analogs = processDatDTO.getAnalogs().toString().split(M9kStationConstants.NEWLINE);
						// START: 11-Jun-2020 - Mini implementation
						analogOffset += analogs.length;
//						if (currentDfr.isMiniChassis())
//						{
//							analogOffset += (analogs.length-(analogs.length % M9kConstants.NO_OF_ANALOG_CHNLS_PER_ANEV_BOARD));
//						}
//						else
//						{
//							analogOffset += (analogs.length-(analogs.length % 8));
//						}
						// END: 11-Jun-2020
					}
					if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
					{
						events = processDatDTO.getEvents().toString().split(M9kStationConstants.NEWLINE); 
						eventOffset += events.length;
					}
				}
				catch (Exception e) {
//					e.printStackTrace();
					logger.error("Error occured while updating analogs and events string array ",e);
					throw e;
				}
			}
			i++;

		}
//		logger.debug("Before set Merged data th object "+mergedProcessDatDto);
//		logger.debug("Before calling setMergedData mergeEventData "+mergedProcessDatDto.getMergedEventData());
		logger.debug("About to set merged data");
		try
		{
			if (mergedProcessDatDto.getMergedAnalogData() != null || mergedProcessDatDto.getMergedEventData() != null)
			{
				setMergedData(mergedProcessDatDto);
			}
			else
			{
				setMergedBinaryData(mergedProcessDatDto);
			}
		}
		catch (Exception e) {
//			e.printStackTrace();
			logger.error("Error occured in setMergedData ",e);
			throw e;
		}
//		logger.debug("After set merge Data returned, data size "+mergedProcessDatDto.getData().length());
		
//		logger.debug("Merged process dat dto "+mergedProcessDatDto);
		return mergedProcessDatDto;
	}

//	private ProcessDatDTO processAndMergeFaultsBackUp(List<ProcessDatDTO> lstReadyToPushDat)
//	{
//		// Sort ascending order of the dfr id
//		Collections.sort(lstReadyToPushDat, new Comparator<ProcessDatDTO>() {
//
//			@Override
//			public int compare(ProcessDatDTO o1, ProcessDatDTO o2) {
//				return ((o1.getDfrId() < o2.getDfrId())?0:1);
//			}
//		});
//		int analogOffset = 1;
//		int eventOffset = 1;
//		String analogs[];
//		String events[];
//		int i = 1;
//		ProcessDatDTO mergedProcessDatDto = null;
//		String updatedAnalogData;
//		String updatedEventData;
//		iCnt = 0;
//
//		for (Iterator<ProcessDatDTO> iterator = lstReadyToPushDat.iterator(); iterator
//				.hasNext();) {
//			ProcessDatDTO processDatDTO = iterator.next();
//			logger.debug("MERGE: the Analogs at the start "+processDatDTO.getAnalogs());
//			if (mergedProcessDatDto != null)
//			{
//				logger.debug("MERGE: the MergedAnalogs at the start "+mergedProcessDatDto.getAnalogs());
//			}
//			if (processDatDTO.getDfrId() != i && i==1) // DFR1 is down
//			{
//				mergedProcessDatDto = new ProcessDatDTO();
//				mergedProcessDatDto.setType(processDatDTO.getType());
//				mergedProcessDatDto.setTsPrefault(processDatDTO.getTsPrefault());
//				mergedProcessDatDto.setTsTrigger(processDatDTO.getTsTrigger());
//				mergedProcessDatDto.setLineFreq(processDatDTO.getLineFreq());
//				mergedProcessDatDto.setSampleRate(processDatDTO.getSampleRate());
//				mergedProcessDatDto.setSampleCnt(processDatDTO.getSampleCnt());
//				mergedProcessDatDto.setAnalogs(processDatDTO.getAnalogs());
//				mergedProcessDatDto.setEvents(processDatDTO.getEvents());
//				updateDummyData(mergedProcessDatDto, i);
//				mergedProcessDatDto.setData(processDatDTO.getData());
//				processDatDTO.setAnalogOffset(analogOffset);
//				processDatDTO.setEventsOffset(eventOffset);
//				i++;
//				continue;
//			}
//			else if (i > 1) // DFR other than DFR1 is down
//			{
////				processDatDTO.setAnalogOffset(analogOffset);
////				processDatDTO.setEventsOffset(eventOffset);
////				mergedProcessDatDto.setSampleCnt(mergedProcessDatDto.getSampleCnt()+processDatDTO.getSampleCnt());
////				logger.debug("Samples count "+mergedProcessDatDto.getSampleCnt());
//
////				logger.debug("Before updateData the object "+processDatDTO);
//				updateDummyData(mergedProcessDatDto, i);
//				
//
//				if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
//				{
//					updatedAnalogData = updateAnalogOffset(processDatDTO);
////					mergedProcessDatDto.getAnalogs().append(M9kStationConstants.NEWLINE);
//					mergedProcessDatDto.getAnalogs().append(updatedAnalogData);
//				}
//
//				if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
//				{
//					updatedEventData = updateEventOffset(processDatDTO);
////					mergedProcessDatDto.getEvents().append(M9kStationConstants.NEWLINE);
//					mergedProcessDatDto.getEvents().append(updatedEventData);
//				}
//				i++;
//				continue;
//			}
//			if (i == 1)
//			{
//				mergedProcessDatDto = new ProcessDatDTO();
//				mergedProcessDatDto.setType(processDatDTO.getType());
//				mergedProcessDatDto.setTsPrefault(processDatDTO.getTsPrefault());
//				mergedProcessDatDto.setTsTrigger(processDatDTO.getTsTrigger());
//				mergedProcessDatDto.setLineFreq(processDatDTO.getLineFreq());
//				mergedProcessDatDto.setSampleRate(processDatDTO.getSampleRate());
//				mergedProcessDatDto.setSampleCnt(processDatDTO.getSampleCnt());
//				mergedProcessDatDto.setAnalogs(processDatDTO.getAnalogs());
//				mergedProcessDatDto.setEvents(processDatDTO.getEvents());
//				updateData(mergedProcessDatDto, processDatDTO);
////				mergedProcessDatDto.setData(processDatDTO.getData());
//				processDatDTO.setAnalogOffset(analogOffset);
//				processDatDTO.setEventsOffset(eventOffset);
//			}
//			else
//			{
//				processDatDTO.setAnalogOffset(analogOffset);
//				processDatDTO.setEventsOffset(eventOffset);
////				mergedProcessDatDto.setSampleCnt(mergedProcessDatDto.getSampleCnt()+processDatDTO.getSampleCnt());
////				logger.debug("Samples count "+mergedProcessDatDto.getSampleCnt());
//
//				logger.debug("Beforeeeee updateData the object "+processDatDTO);
//				logger.debug("MERGE:Before updateData the Analogs "+processDatDTO.getAnalogs());
//				updateData(mergedProcessDatDto, processDatDTO);
//				
//
//				if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
//				{
//					updatedAnalogData = updateAnalogOffset(processDatDTO);
////					mergedProcessDatDto.getAnalogs().append(M9kStationConstants.NEWLINE);
//					mergedProcessDatDto.getAnalogs().append(updatedAnalogData);
//				}
//
//				if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
//				{
//					updatedEventData = updateEventOffset(processDatDTO);
////					mergedProcessDatDto.getEvents().append(M9kStationConstants.NEWLINE);
//					mergedProcessDatDto.getEvents().append(updatedEventData);
//				}
//
//			}
//			i++;
//			
//			if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
//			{
//				analogs = processDatDTO.getAnalogs().toString().split(M9kStationConstants.NEWLINE); 
//				analogOffset += analogs.length;
//			}
//			if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
//			{
//				events = processDatDTO.getEvents().toString().split(M9kStationConstants.NEWLINE); 
//				eventOffset += events.length;
//			}
//			
//			
//		}
////		logger.debug("Before set Merged data th object "+mergedProcessDatDto);
////		logger.debug("Before calling setMergedData mergeEventData "+mergedProcessDatDto.getMergedEventData());
//		setMergedData(mergedProcessDatDto);
//		logger.debug("After set merge Data returned, data size "+mergedProcessDatDto.getData().length());
//		
//		logger.debug("Merged process dat dto "+mergedProcessDatDto);
//		return mergedProcessDatDto;
//	}
	
	private String updateAnalogOffset(ProcessDatDTO processDatDTO) throws Exception
	{
		String analogs[];
		StringBuffer newAnalogBuffer = new StringBuffer();
		StringBuffer analogBuffer = processDatDTO.getAnalogs();
		analogs = analogBuffer.toString().split(M9kStationConstants.NEWLINE);
//		int aOffset = processDatDTO.getAnalogOffset();
		int aOffset = currentDfr.getAnalogChannelStart();
		
		logger.debug("OFFSET analogs offset "+aOffset+" cfgChnlIndexNumber "+cfgChnlIndexNumber);
		String newPrefixStr;
		int startIndex;
		for (int i = 0; i < analogs.length; i++) {
			logger.debug("Analog loop i-> "+i+" - "+analogs[i]+" currentDfr "+currentDfr+" currentDfr physical analog chnl count "+currentDfr.getPhysicalAnalogsCount() );
			startIndex = findNthIndexOf(analogs[i], ",", 2);
			if (i < currentDfr.getPhysicalAnalogsCount())
			{
				newPrefixStr = cfgChnlIndexNumber+","+"A"+aOffset;
			}
			else
			{
				newPrefixStr = cfgChnlIndexNumber+","+M9kStationConstants.VIRTUAL_PREFIX+aOffset;
			}
//			logger.debug("OFFSET analogs prefix offset "+newPrefixStr);
			newAnalogBuffer.append(newPrefixStr);
			newAnalogBuffer.append(analogs[i].substring(startIndex));
			if (i < analogs.length)
			{
				newAnalogBuffer.append(M9kStationConstants.NEWLINE);
			}
			aOffset++;
			cfgChnlIndexNumber ++;
		}
		return newAnalogBuffer.toString();
		
	}
	
	private String updateEventOffset(ProcessDatDTO processDatDTO) throws Exception
	{
		String events[];
		StringBuffer newEventBuffer = new StringBuffer();
		StringBuffer eventBuffer = processDatDTO.getEvents();
		events = eventBuffer.toString().split(M9kStationConstants.NEWLINE);
		int eOffset = processDatDTO.getEventsOffset();
//		logger.debug("OFFSET events offset "+eOffset);
		String newPrefixStr = eOffset+","+"E";
		int startIndex ;
		for (int i = 0; i < events.length; i++) {
			if (!isTriggerEvent(events[i]))
			{
				startIndex = findNthIndexOf(events[i],",",2);
//				newPrefixStr = eOffset+","+"E"+eOffset;
				newPrefixStr = eOffset+","+"E"+eventChannelIndex++;
			}
			else
			{
				startIndex = findNthIndexOf(events[i],",",1);
				newPrefixStr = eOffset+"";
			}
//			logger.debug("SANDY-BOG: OFFSET events prefix offset "+newPrefixStr);
			newEventBuffer.append(newPrefixStr);
			newEventBuffer.append(events[i].substring(startIndex));
			if (i < events.length)
			{
				newEventBuffer.append(M9kStationConstants.NEWLINE);
			}
			eOffset++;
		}
//		logger.debug("SANDY-BOG: Events string "+newEventBuffer.toString());
		return newEventBuffer.toString();
		
	}

	
	private boolean isTriggerEvent(String eventInfo) {
		boolean isTriggerEvent = true;
		String[] strTokens = eventInfo.split(",");
		if (strTokens[1].toUpperCase().startsWith("E"))
		{
			isTriggerEvent = false;
		}
		return isTriggerEvent;
	}

	private void updateData (ProcessDatDTO mergedProcessDatDto, ProcessDatDTO processDatDTO) throws Exception
	{
		StringBuffer mergedAnalogDataBuffer = mergedProcessDatDto.getMergedAnalogData();
		StringBuffer mergedEventDataBuffer = mergedProcessDatDto.getMergedEventData();
//		logger.debug("Event data merged "+mergedEventDataBuffer);
		String data[] = processDatDTO.getData().toString().split(M9kStationConstants.NEWLINE);
		logger.debug("SHOW: total data count to update "+data.length);
		String mergedAnalog[] = null;
		String mergedEvent[] = null;
		if (mergedAnalogDataBuffer != null && mergedAnalogDataBuffer.length() > 0)
		{
			mergedAnalog = mergedAnalogDataBuffer.toString().split(M9kStationConstants.NEWLINE);
		}
		if (mergedEventDataBuffer != null && mergedEventDataBuffer.length() > 0)
		{
			mergedEvent =  mergedEventDataBuffer.toString().split(M9kStationConstants.NEWLINE);
		}

		String dataSplitArray[];
		StringBuffer newMergedAnalogDataBuffer = new StringBuffer();
		StringBuffer newMergedEventDataBuffer = new StringBuffer();

//		logger.debug("Analog String to start with "+data[0]);
		int analogsCnt = 0;
		int startIndex = 2;
//		startIndex = findNthIndexOf(data[0], ",", 2)+1;
		int endIndex = 0;
		int eventStartIndex = 0;
		boolean isAnalog = false;
		boolean isEvents = false;
		if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
		{
				analogsCnt = processDatDTO.getAnalogs().toString().split(M9kStationConstants.NEWLINE).length;
				endIndex =  analogsCnt+1;
				if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
				{
					eventStartIndex = endIndex+1;
				}
			logger.debug("Total analogsCnt "+analogsCnt);
			logger.debug("Start Index "+startIndex);
			
			logger.debug("End Index in analog box "+endIndex);
			isAnalog = true;
		}
		else
		{
			eventStartIndex = startIndex;
			logger.debug("End Index in events "+endIndex);
		}

		if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
		{
			isEvents = true;	
		}
		logger.debug("SHOW: Event start index "+eventStartIndex+" Do we have the events this time? "+isEvents);
		
		try
		{
			if (isAnalog)
			{
				if (mergedAnalog != null)
				{
	//				logger.debug("SHOW: (Merged Analog not null) Total data sample "+data.length);
					for (int i = 0; i < data.length; i++) {
	//					logger.debug("SHOW: In Analog sample Cnt "+i+" Data: "+data[i]);
						dataSplitArray = data[i].split(",");
						newMergedAnalogDataBuffer.append(mergedAnalog[i]);
						for (int j = startIndex; j <= endIndex; j++) {
							newMergedAnalogDataBuffer.append(","+dataSplitArray[j]);
						}
							newMergedAnalogDataBuffer.append(M9kStationConstants.NEWLINE);
					}
				}
				else
				{
	//				logger.debug("SHOW: (No Merged event ) Total data sample "+data.length);
					for (int i = 0; i < data.length; i++) {
	//					logger.debug("SHOW: In Analog sample Cnt "+i+" Data: "+data[i]);
						
						dataSplitArray = data[i].split(",");
	//					timestamp = (long)((iCnt/processDatDTO.getSampleRate())*1000000);
						newMergedAnalogDataBuffer.append(dataSplitArray[0] +","+dataSplitArray[1]);
						for (int j = startIndex; j <= endIndex; j++) {
								newMergedAnalogDataBuffer.append(","+dataSplitArray[j]);
						}
							newMergedAnalogDataBuffer.append(M9kStationConstants.NEWLINE);
					}
					
				}
			}
		}
		catch (Exception e) {
//			e.printStackTrace();
			logger.error("Error occured during analog data update",e);
			throw e;
		}
		
		try
		{
			if (isEvents)
			{
				if (mergedEvent != null)
				{
	//				logger.debug("SHOW: (Merged event not null) Total data sample "+data.length);
					for (int i = 0; i < data.length; i++) {
	//					logger.debug("SHOW: In Events sample Cnt "+i+" Data: "+data[i]);
						
						dataSplitArray = data[i].split(",");
						newMergedEventDataBuffer.append(mergedEvent[i]);
						for (int j = eventStartIndex; j < dataSplitArray.length; j++) {
								newMergedEventDataBuffer.append(","+dataSplitArray[j]);
						}
						if (i==0)
						{
							logger.debug("MERGE: ACTUAL events added when megedEvent != null "+newMergedEventDataBuffer);
						}
						newMergedEventDataBuffer.append(M9kStationConstants.NEWLINE);
					}
				}
				else
				{
	//				logger.debug("SHOW:(No merged event) Total data sample "+data.length);
					for (int i = 0; i < data.length; i++) {
	//					logger.debug("SHOW: In Events sample Cnt "+i+" Data: "+data[i]);
						dataSplitArray = data[i].split(",");
						for (int j = eventStartIndex; j < dataSplitArray.length; j++) {
							if (j == eventStartIndex)
							{
								newMergedEventDataBuffer.append(dataSplitArray[j]);
	//							logger.debug("Total Events "+dataSplitArray.length);
							}
							else
							{
								newMergedEventDataBuffer.append(","+dataSplitArray[j]);
							}
						}
						if (i==0)
						{
							logger.debug("MERGE: ACTUAL events added when megedEvent == null "+newMergedEventDataBuffer);
						}
	
						newMergedEventDataBuffer.append(M9kStationConstants.NEWLINE);
					}
					
					
				}
			}
		}
		catch (Exception e) {
//			e.printStackTrace();
			logger.error("Error occured during events data update",e);
			throw e;
		}		
		
		if (newMergedAnalogDataBuffer != null && newMergedAnalogDataBuffer.length()  > 0)
		{
				mergedProcessDatDto.setMergedAnalogData(newMergedAnalogDataBuffer);
		}
		if (newMergedEventDataBuffer != null && newMergedEventDataBuffer.length()  > 0)
		{
				mergedProcessDatDto.setMergedEventData(newMergedEventDataBuffer);
		}
		logger.debug("Returning from updateData");
	}

	private void updateDummyData (ProcessDatDTO mergedProcessDatDto)
	{
		int totalAnalogs = 0;
		int totalDigitals = 0;
		int iCnt = 0;

		logger.debug("Current dfr "+currentDfr.getDfrId());
//		for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
//			currentDfr = iterator.next();
//			if (currentDfr.getDfrId() == dfrId)
//			{
		totalAnalogs = currentDfr.getAnalogChnlCnt();
		totalDigitals = currentDfr.getDigitalChnlCnt();
//				break;
//			}
//			
//		}
		StringBuffer mergedAnalogDataBuffer = mergedProcessDatDto.getMergedAnalogData();
		StringBuffer mergedEventDataBuffer = mergedProcessDatDto.getMergedEventData();
//		logger.debug("Event data merged "+mergedEventDataBuffer);
//		String data[] = processDatDTO.getData().toString().split(M9kStationConstants.NEWLINE);
//		logger.debug("SHOW: total data count to update "+data.length);
		String mergedAnalog[] = null;
		String mergedEvent[] = null;
		if (mergedAnalogDataBuffer != null && mergedAnalogDataBuffer.length() > 0)
		{
			mergedAnalog = mergedAnalogDataBuffer.toString().split(M9kStationConstants.NEWLINE);
		}
		if (mergedEventDataBuffer != null && mergedEventDataBuffer.length() > 0)
		{
			mergedEvent =  mergedEventDataBuffer.toString().split(M9kStationConstants.NEWLINE);
		}

//		String dataSplitArray[];
		StringBuffer newMergedAnalogDataBuffer = new StringBuffer();
		StringBuffer newMergedEventDataBuffer = new StringBuffer();

//		logger.debug("Analog String to start with "+data[0]);
//		int analogsCnt = 0;
		int startIndex = 2;
//		startIndex = findNthIndexOf(data[0], ",", 2)+1;
		int endIndex = 0;
		int eventStartIndex = 0;
		boolean isAnalog = false;
		boolean isEvents = false;
		long timestamp;
		if (totalAnalogs > 0)
		{
//				analogsCnt = processDatDTO.getAnalogs().toString().split(M9kStationConstants.NEWLINE).length;
				endIndex =  totalAnalogs+1;
				if (totalDigitals > 0)
				{
					eventStartIndex = endIndex+1;
				}
			logger.debug("Total analogsCnt "+totalAnalogs+" Total digitals "+totalDigitals);
			logger.debug("Start Index "+startIndex);
			
			logger.debug("End Index in analog box "+endIndex);
			isAnalog = true;
		}
		else
		{
			eventStartIndex = startIndex;
			logger.debug("End Index in events "+endIndex);
		}

		if (totalDigitals > 0)
		{
			isEvents = true;	
		}
		logger.debug("SHOW: Event start index "+eventStartIndex+" Do we have the events this time? "+isEvents);
		
		
		if (isAnalog)
		{
			if (mergedAnalog != null)
			{
//				logger.debug("SHOW: (Merged Analog not null) Total data sample "+data.length);
				for (int i = 0; i < mergedProcessDatDto.getSampleCnt(); i++) {
//					logger.debug("SHOW: In Analog sample Cnt "+i+" Data: "+data[i]);
//					dataSplitArray = data[i].split(",");
					newMergedAnalogDataBuffer.append(mergedAnalog[i]);
					for (int j = startIndex; j <= endIndex; j++) {
						newMergedAnalogDataBuffer.append(","+"99999");
					}
						newMergedAnalogDataBuffer.append(M9kStationConstants.NEWLINE);
				}
			}
			else
			{
//				logger.debug("SHOW: (No Merged event ) Total data sample "+data.length);
				for (int i = 0; i < mergedProcessDatDto.getSampleCnt(); i++) {
//					logger.debug("SHOW: In Analog sample Cnt "+i+" Data: "+data[i]);
					
//					dataSplitArray = data[i].split(",");
					timestamp = (long)((iCnt++ /mergedProcessDatDto.getSampleRate())*1000000);
					newMergedAnalogDataBuffer.append(iCnt +","+timestamp);
					for (int j = startIndex; j <= endIndex; j++) {
							newMergedAnalogDataBuffer.append(","+"99999");
					}
						newMergedAnalogDataBuffer.append(M9kStationConstants.NEWLINE);
				}
				
			}
			updateDummyAnalogDetails(mergedProcessDatDto);

		}
		
		if (isEvents)
		{
			if (mergedEvent != null)
			{
//				logger.debug("SHOW: (Merged event not null) Total data sample "+data.length);
				for (int i = 0; i < mergedProcessDatDto.getSampleCnt(); i++) {
//					logger.debug("SHOW: In Events sample Cnt "+i+" Data: "+data[i]);
					
//					dataSplitArray = data[i].split(",");
					newMergedEventDataBuffer.append(mergedEvent[i]);
					for (int j = eventStartIndex; j < eventStartIndex+totalDigitals; j++) {
							newMergedEventDataBuffer.append(","+"0");
					}
					if (i==0)
					{
						logger.debug("MERGE: DUMMY events added when megedEvent != null "+newMergedEventDataBuffer);
					}
						newMergedEventDataBuffer.append(M9kStationConstants.NEWLINE);
				}
			}
			else
			{
//				logger.debug("SHOW:(No merged event) Total data sample "+data.length);
				for (int i = 0; i < mergedProcessDatDto.getSampleCnt(); i++) {
//					logger.debug("SHOW: In Events sample Cnt "+i+" Data: "+data[i]);
//					dataSplitArray = data[i].split(",");
					for (int j = eventStartIndex; j < eventStartIndex+totalDigitals; j++) {
						if (j == eventStartIndex)
						{
							newMergedEventDataBuffer.append("0");
//							logger.debug("Total Events "+dataSplitArray.length);
						}
						else
						{
							newMergedEventDataBuffer.append(","+"0");
						}
					}
					if (i==0)
					{
						logger.debug("MERGE: DUMMY events added when megedEvent == null "+newMergedEventDataBuffer);
					}
						newMergedEventDataBuffer.append(M9kStationConstants.NEWLINE);
				}
				
				
			}
				updateDummyEventDetails(mergedProcessDatDto);

		}
		
		
		if (newMergedAnalogDataBuffer != null && newMergedAnalogDataBuffer.length()  > 0)
		{
				mergedProcessDatDto.setMergedAnalogData(newMergedAnalogDataBuffer);
		}
		if (newMergedEventDataBuffer != null && newMergedEventDataBuffer.length()  > 0)
		{
				mergedProcessDatDto.setMergedEventData(newMergedEventDataBuffer);
		}
		
	}
	
	/**
	 * Creates dummy data for analog channels
	 * START: 08-Dec-2020 - Dummy data creation updated
	 * END: 14-Dec-2020
	 */
	private void updateDummyAnalogDetails(ProcessDatDTO mergedProcessDatDto)
	{
		StringBuffer dummyAnalogCfg = new StringBuffer();
		int chnlCnt = 0;
////		int diff = currentDfr.getAnalogChannelStart() - analogOffset ;
////		logger.debug("MERGE: current analog offset "+analogOffset+" dfrs analog start chnl no "+currentDfr.getAnalogChannelStart());
////		int actualOffsetStart = currentDfr.getAnalogChannelStart();
////		int actualOffsetEnd = currentDfr.getAnalogChannelEnd();
////		if (diff < 0)
////		{
////			actualOffsetStart+=Math.abs(diff);
////			actualOffsetEnd+=Math.abs(diff);
////		}
//		logger.debug("MERGE: Actual Analog Start "+actualOffsetStart+" Actual Analog End "+actualOffsetEnd);
		AnalogChannelDTO dummyAnalogDto;
		String inputUnits;
//		for (int i = actualOffsetStart,j = analogOffset + 1; i <= actualOffsetEnd; i++,j++) {
		for (int i = currentDfr.getAnalogChannelStart(); i < (currentDfr.getAnalogChannelStart()+currentDfr.getAnalogChnlCnt()); i++) {
			// ,V,1,0,0,-32768,32767,1,10,P
			dummyAnalogDto = currentDfr.getLstAnalogChannels().get(chnlCnt++);
			if (!dummyAnalogDto.isTransducer())
			{
				if (dummyAnalogDto.getInputType().toLowerCase().indexOf("current") != -1)
				{
					inputUnits = "A";
				}
				else
				{
					inputUnits = "V";
				}
			}
			else
			{
				inputUnits = dummyAnalogDto.getTransducerUnits();
			}
//			if (analogOffset > actualOffsetStart)// Virtual channels affects the index
			if (dummyAnalogDto.isVirtual())
			{
//				dummyAnalogCfg.append(j+",A"+i+",A"+","+currentDfr.getLstAnalogChannelNames().get(chnlCnt++)+DUMMY_ANALOG_CFG);
				dummyAnalogCfg.append((analogOffset++)+",SUM"+i);
			}
			else
			{
//				dummyAnalogCfg.append(i+",A"+i+",A"+","+currentDfr.getLstAnalogChannelNames().get(chnlCnt++)+DUMMY_ANALOG_CFG);
				dummyAnalogCfg.append((analogOffset++)+",A"+i);
			}
			dummyAnalogCfg.append(","+dummyAnalogDto.getPhase()); // Phase
			dummyAnalogCfg.append(","+dummyAnalogDto.getCircuitName()); // Channel description
			dummyAnalogCfg.append(","+inputUnits); // Units
			dummyAnalogCfg.append(",1,0,"+M9kConstants.COMTRADE_ANALOG_CHNL_SKEW); // Scale, Offset and Skew factor
			dummyAnalogCfg.append(","+M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MIN+","+M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MAX); // min and max range
			dummyAnalogCfg.append(","+dummyAnalogDto.getPrimaryRatio()+","+dummyAnalogDto.getSecondaryRatio()+","+M9kConstants.COMTRADE_PRIMARY); // Primary and Secondary ratio amd Primary or secondary
			dummyAnalogCfg.append(M9kStationConstants.NEWLINE);
			cfgChnlIndexNumber ++; // Keep track of index in case of dummy channels
		}
//		analogOffset = currentDfr.getAnalogChannelEnd()+1;
		if (mergedProcessDatDto.getAnalogs() == null)
		{
			mergedProcessDatDto.setAnalogs(dummyAnalogCfg);
		}
		else
		{
			mergedProcessDatDto.getAnalogs().append(dummyAnalogCfg);
		}
	}
	
	private void updateDummyEventDetails(ProcessDatDTO mergedProcessDatDto)
	{
		StringBuffer dummyDigitalCfg = new StringBuffer();
		int ichnlCnt = 0;
		logger.debug("currentDfr "+currentDfr+" lst Evnt chnl names "+currentDfr.getLstEventChannelNames()+" length "+currentDfr.getLstEventChannelNames().size());
		logger.debug("currentDfr.getDigitalChannelStart() "+currentDfr.getDigitalChannelStart()+" currentDfr.getDigitalChannelEnd() "+currentDfr.getDigitalChannelEnd());
//		int diff = currentDfr.getDigitalChannelStart() - eventOffset ;
		logger.debug("MERGE: current digital offset "+eventOffset+" dfrs digital start chnl no "+currentDfr.getDigitalChannelStart());
		int actualOffsetStart = currentDfr.getDigitalChannelStart();
		int actualOffsetEnd = currentDfr.getDigitalChannelEnd();
//		if (diff < 0)
//		{
//			actualOffsetStart+=Math.abs(diff);
//			actualOffsetEnd+=Math.abs(diff);
//		}
		logger.debug("MERGE: Actual Digital Start "+actualOffsetStart+" Actual Digital End "+actualOffsetEnd);
		
		for (int i = actualOffsetStart; i <= actualOffsetEnd; i++) {
			// START: 08-Dec-2020 - Get normal status for events for dummy records
//			dummyDigitalCfg.append(i+",E"+i+",,"+currentDfr.getLstEventChannelNames().get(ichnlCnt++)+",0");
			dummyDigitalCfg.append((eventOffset++) +",E"+i+",,"+currentDfr.getLstEventChannelNames().get(ichnlCnt)+","+currentDfr.getLstEventChannels().get(ichnlCnt).getNormalState());
			ichnlCnt++;
			// END: 08-Dec-2020
			dummyDigitalCfg.append(M9kStationConstants.NEWLINE);
		}
//		eventOffset = currentDfr.getDigitalChannelEnd()+1;
		if (mergedProcessDatDto.getEvents() == null)
		{
			mergedProcessDatDto.setEvents(dummyDigitalCfg);
		}
		else
		{
			mergedProcessDatDto.getEvents().append(dummyDigitalCfg);
		}
	}
	private void setMergedData(ProcessDatDTO mergedProcessDatDto) throws Exception
	{
		logger.debug("in set merge data ");
//		logger.debug("In setMergedData mergeAnalogData "+mergedProcessDatDto.getMergedAnalogData());
//		logger.debug("In setMergedData mergeEventData "+mergedProcessDatDto.getMergedEventData());
		String analogData[] = null;
		String eventData[] = null;
		if (mergedProcessDatDto.getMergedAnalogData() != null && mergedProcessDatDto.getMergedAnalogData().length() > 0)
		{
			analogData =  mergedProcessDatDto.getMergedAnalogData().toString().split(M9kStationConstants.NEWLINE);
		}
		if (mergedProcessDatDto.getMergedEventData() != null && mergedProcessDatDto.getMergedEventData().length() > 0 )
		{
			eventData = mergedProcessDatDto.getMergedEventData().toString().split(M9kStationConstants.NEWLINE);
		}
		StringBuffer finalData = new StringBuffer();
		int i = 0;
		try
		{
			if (analogData != null && eventData != null)
			{
				for (i = 0; i < analogData.length; i++) {
					finalData.append(analogData[i]);
					finalData.append(","+eventData[i]);
					if (i < analogData.length)
					{
						finalData.append(M9kStationConstants.NEWLINE);
					}
				}
			}
			else if (analogData != null)
			{
				for (i = 0; i < analogData.length; i++) {
					finalData.append(analogData[i]);
					if (i < analogData.length)
					{
						finalData.append(M9kStationConstants.NEWLINE);
					}
				}
			}
			else if (eventData != null)
			{
				long timestamp = 0;
				for (i = 0; i < eventData.length; i++) {
					timestamp = (long)((i /mergedProcessDatDto.getSampleRate())*1000000);
					finalData.append(i+","+timestamp+","+eventData[i]);
					if (i < eventData.length)
					{
						finalData.append(M9kStationConstants.NEWLINE);
					}
				}				
			}
			mergedProcessDatDto.setData(finalData);
			finalData = null;
			analogData = null;
			eventData = null;
			mergedProcessDatDto.setMergedAnalogData(null);
			mergedProcessDatDto.setMergedEventData(null);
			logger.debug("After setData in setMergedData "+i);
		}
		catch (Exception e) {
//			e.printStackTrace();
			finalData = null;
			analogData = null;
			eventData = null;
			mergedProcessDatDto.setMergedAnalogData(null);
			mergedProcessDatDto.setMergedEventData(null);
			logger.error("Error occured in setMergedData "+i,e);
			throw e;
		}
		logger.debug("Merging final data done. Returning.");;
	}

	private void setMergedBinaryData(ProcessDatDTO mergedProcessDatDto) throws Exception
	{
		DataInputStream binMergedAnalogInputStream = null;
		DataInputStream binMergedEventsInputStream  = null;
		ByteArrayOutputStream binaryData = new ByteArrayOutputStream();
		DataOutputStream binaryOutStream = new DataOutputStream(binaryData);
		boolean flgAnalog = false;
		boolean flgEvent = false;
		if (mergedProcessDatDto.getMergedBinaryAnalogData() != null)
		{
			flgAnalog = true;
			logger.debug("BINARY: Analog data size to be merged "+mergedProcessDatDto.getMergedBinaryAnalogData().size());
			binMergedAnalogInputStream = new DataInputStream(new ByteArrayInputStream(mergedProcessDatDto.getMergedBinaryAnalogData().toByteArray()));
		}
		if (mergedProcessDatDto.getMergedBinaryEventData() != null)
		{
			flgEvent = true;
			logger.debug("BINARY: Event data size to be merged "+mergedProcessDatDto.getMergedBinaryEventData().size());
			binMergedEventsInputStream = new DataInputStream(new ByteArrayInputStream( mergedProcessDatDto.getMergedBinaryEventData().toByteArray()));
		}
		logger.debug("BINARY: Total Analog cnt to be merged "+mergedProcessDatDto.getMergedAnalogsCount());
		logger.debug("BINARY: Total Event cnt to be merged "+mergedProcessDatDto.getMergedEventsCount());
		int eventBytesToRead = (int)(2 * Math.ceil((float)mergedProcessDatDto.getMergedEventsCount()/16));
		byte[] mergedAnalogBytes = new byte[mergedProcessDatDto.getMergedAnalogsCount()];
		byte[] mergedEventBytes = new byte[eventBytesToRead];
		byte[] finalMergedData = new byte[mergedProcessDatDto.getMergedAnalogsCount()+eventBytesToRead];
		logger.debug("BINARY: in set merge data: Total sample Cnt "+mergedProcessDatDto.getSampleCnt());
		int i = 0;
		try
		{
			if (flgAnalog && flgEvent)
			{
				while (i++ < mergedProcessDatDto.getSampleCnt())
				{
					binMergedAnalogInputStream.read(mergedAnalogBytes);
					binMergedEventsInputStream.read(mergedEventBytes);
					
					// Merge the 2 byte arrays, the merged analog data with merged event data to form a record
					System.arraycopy(mergedAnalogBytes, 0, finalMergedData, 0, mergedAnalogBytes.length);
					if (i==1)
					{
						logger.debug("BINARY: Final merged data Size after analog data "+finalMergedData.length+" after reading analog data size "+mergedAnalogBytes.length);
					}
					System.arraycopy(mergedEventBytes, 0, finalMergedData, mergedAnalogBytes.length, mergedEventBytes.length);
					if (i==1)
					{
						logger.debug("BINARY: Final merged data Size after event data and before writing "+finalMergedData.length+" after reading event data size "+mergedEventBytes.length);
					}
					binaryOutStream.write(finalMergedData);
	
				}
			}
			else if (flgAnalog)
			{
					binaryOutStream.write(mergedProcessDatDto.getMergedBinaryAnalogData().toByteArray());
			}
			else if (flgEvent)
			{
				long timestamp = 0;
				Integer dummyTimestamp = 0xFFFFFFFF;
				while (i < mergedProcessDatDto.getSampleCnt())
				{
					timestamp = (long)((i /mergedProcessDatDto.getSampleRate())*1000000);
					binaryOutStream.writeInt(Integer.reverseBytes(i));
					i++;
					if (timestamp <= (long)Integer.MAX_VALUE)
					{
						binaryOutStream.writeInt(Integer.reverseBytes((int)timestamp));
					}
					else
					{
						binaryOutStream.writeInt(Integer.reverseBytes(dummyTimestamp));
					}
					binMergedEventsInputStream.read(mergedEventBytes);
					binaryOutStream.write(mergedEventBytes);
				}				
			}
			mergedProcessDatDto.setBinaryData(binaryData);
		}
		catch (Exception e) {
//			e.printStackTrace();
			mergedProcessDatDto.setMergedAnalogData(null);
			mergedProcessDatDto.setMergedEventData(null);
			logger.error("Error occured in setMergedData "+i,e);
			throw e;
		}
		finally
		{
			try
			{
				mergedAnalogBytes = null;
				mergedEventBytes = null;
				finalMergedData = null;
				
				if (binaryData != null)
				{
					binaryData.close();
					binaryData = null;
				}
				if (binaryOutStream != null)
				{
					binaryOutStream.close();
					binaryOutStream = null;
				}
				if (binMergedAnalogInputStream != null)
				{
					binMergedAnalogInputStream.close();
					binMergedAnalogInputStream = null;
				}
				if (binMergedEventsInputStream != null)
				{
					binMergedEventsInputStream.close();
					binMergedEventsInputStream = null;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		logger.debug("Merging final data done. Returning.");;
	}

    private List<ProcessDatDTO> getDatRecordToProcess()
    {
    	long endTimer = 0;
		PreparedStatement ps = null;
		ResultSet rs= null;
		ProcessDatDTO processDatDTO;
		List<ProcessDatDTO> lstProcessDatDTO = new ArrayList<ProcessDatDTO>();
		mapDfrsStatus = m9kDFRHealthStatusProcessor.getDFRsStatus();
		logger.debug(" mapDfrsStatus "+mapDfrsStatus);
		int activeDfrs = getActiveDfrsCount(mapDfrsStatus);
		logger.debug("Active dfrs at the start "+activeDfrs);
		InputStream binaryDataStream;
		String asciiData;
		boolean booContinue = true;
		boolean booStatusChange = false;
		try {
			logger.debug("DB-POOL New connection from getDatRecordToProcess ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
				ps = mysqlConn.prepareStatement("select a.* from dat a where a.signature = ? order by a.dfrId");
				startTimer = -1;

				while (booContinue)
				{
				
					ps.setInt(1, triggersScheduleDTO.getSignature());
					rs = ps.executeQuery();
					if (startTimer == -1)
					{
						startTimer = System.currentTimeMillis();
						logger.debug("Start timer set "+startTimer+" for signature="+triggersScheduleDTO.getSignature());
						
					}
					while (rs.next())
					{
						processDatDTO = new ProcessDatDTO();
						processDatDTO.setDatId(rs.getInt("id"));
						processDatDTO.setDfrId(rs.getInt("dfrId"));
						processDatDTO.setType(rs.getString("type"));
						processDatDTO.setTsPrefault(rs.getLong("tsPrefault"));
						processDatDTO.setTsTrigger(rs.getLong("tsTrigger"));
						processDatDTO.setLineFreq(rs.getDouble("lineFreq"));
						processDatDTO.setSampleRate(rs.getDouble("sampleRate"));
						processDatDTO.setSampleCnt(rs.getInt("sampleCnt"));
						processDatDTO.setAnalogs(new StringBuffer(rs.getString("analogs")));
						processDatDTO.setEvents(new StringBuffer(rs.getString("events")));
						if ((asciiData = rs.getString("data")) != null)
						{
							processDatDTO.setData(new StringBuffer(asciiData));
						}
						else if ((binaryDataStream = rs.getBinaryStream("dataBlob")) != null)
						{
							processDatDTO.setBinaryDataStream(binaryDataStream);
						}
						lstProcessDatDTO.add(processDatDTO);
//						logger.debug(" mapDfrsStatus "+mapDfrsStatus+" processDatDTO "+processDatDTO +" DFR DTO "+processDatDTO.getDfrId());
						if (!mapDfrsStatus.get("DFR"+processDatDTO.getDfrId()))
						{
							logger.debug("DFR may be down but we have record for that");
							booStatusChange = true;
							mapDfrsStatus.put("DFR"+processDatDTO.getDfrId(),true);
						}
					}
					if (booStatusChange)
					{
						activeDfrs = getActiveDfrsCount(mapDfrsStatus);
						logger.debug("Recalculating Active dfrs "+activeDfrs);
					}

				rs.close();
				endTimer = System.currentTimeMillis();
				if (lstProcessDatDTO.size() == activeDfrs || (endTimer - startTimer) > crossReqWaitTime)
				{
					booContinue = false;
					if (lstProcessDatDTO.size() != activeDfrs) // if any dfrs takes too long to respond 
					{
						updateMissingDFRStatus(lstProcessDatDTO);
//				    		M9kLED.setLed(LedName.TRIGGER, LedState.YELLOW);
					}
					break;
				}
				else
				{
					lstProcessDatDTO.clear();
				}
			}
				logger.debug("end timer"+ endTimer+" wait time "+crossReqWaitTime);
				logger.debug("Total time waited "+(endTimer-startTimer)+" for signature="+triggersScheduleDTO.getSignature());
				ps.close();
//				if (lstProcessDatDTO.size() > 0)
//				{
//					removeProcessedDatRecord(lstProcessDatDTO);
//				}
		} catch (SQLException e) {
			logger.error("Error reading data from dat table ", e);
		} catch (Exception e) {
			logger.error("Error reading data from dat table ", e);
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
					logger.debug("DB-POOL Closing connection from getDatRecordToProcess ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}

    	return lstProcessDatDTO;
    }
    
    private void updateMissingDFRStatus(List<ProcessDatDTO>  lstProcessDatDTO)
    {
    	boolean booFound = false;
    	logger.debug("MERGE: Some DFR is not responding or responding too late");
    	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator
				.hasNext();) {
    		DfrDTO dfrDTO =  iterator.next();
			logger.debug("MERGE: about to check for Dfr "+dfrDTO.getDfrId());

    		for (Iterator<ProcessDatDTO> iterator2 = lstProcessDatDTO.iterator(); iterator2
					.hasNext();) {
				ProcessDatDTO processDatDTO = iterator2.next();
				if (processDatDTO.getDfrId() == dfrDTO.getDfrId())
				{
					booFound = true;
					break;
				}
			}
    		if (!booFound)
    		{
    			mapDfrsStatus.put("DFR"+dfrDTO.getDfrId(), false);
				logger.warn("The dfr "+"DFR"+dfrDTO.getDfrId()+" hasn't responded for long time. ");
    		}
    		else
    		{
    			logger.debug("MERGE: dfr id "+dfrDTO.getDfrId()+" is not missing");
    		}
    		booFound = false;
		}
    }
    
    @SuppressWarnings("unused")
	private boolean removeProcessedDatRecord()
    {
    	boolean booConnection = true;
		PreparedStatement ps = null;
		
		try {
//			getLocalConnection();
			logger.debug("About to delete from process-dat table for signature="+triggersScheduleDTO.getSignature());
			ps = mysqlConn.prepareStatement("delete from process_dat where datId IN (select id from dat a where (a.id = datId and a.signature = ?) or a.updated < DATE_SUB(now(), INTERVAL 10 minute))");
			ps.setInt(1, triggersScheduleDTO.getSignature());
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.warn("Station Master is not connected to master or Master Station MySql server is down");
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
					logger.debug("Closing the connection... "+mysqlConn);
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}

    	return booConnection;
    }
    
    private void moveToErrorDat (String errorIds)
    {
		PreparedStatement ps = null;
		PreparedStatement psDelete = null;
		logger.warn("Error ids "+errorIds+ " are being moved to error table error_dat");
		try {
			logger.debug("DB-POOL New  connection from moveToErrorDat ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			ps = mysqlConn.prepareStatement("insert into error_dat select * from dat where id in ("+errorIds+")");
			ps.executeUpdate();
			
			psDelete = mysqlConn.prepareStatement("delete from dat where id in ("+errorIds+")");
			psDelete.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.warn("Error Ids "+errorIds+" weren't moved to error_dat. ",e);
		} catch (Exception e) {
			logger.warn("Error Ids "+errorIds+" weren't moved to error_dat. ",e);
		}
		finally
		{
			try {
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (psDelete != null)
				{
					psDelete.close();
					psDelete = null;
				}
				if (mysqlConn != null)
				{
					logger.debug("DB-POOL Closing connection from moveToErrorDat ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}

    }
    
    private void removeProcessedDatRecords (String recordIds)
    {
		PreparedStatement psDelete = null;
		try {
			logger.debug("DB-POOL New  connection from moveToErrorDat ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			
			psDelete = mysqlConn.prepareStatement("delete from dat where id in ("+recordIds+") or updated < DATE_SUB(now(), INTERVAL 10 minute)" );
			psDelete.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.warn("Dat Ids "+recordIds+" Could not be deleted. ",e);
		} catch (Exception e) {
			logger.warn("Dat Ids "+recordIds+" Could not be deleted.  ",e);
		}
		finally
		{
			try {
				if (psDelete != null)
				{
					psDelete.close();
					psDelete = null;
				}
				if (mysqlConn != null)
				{
					logger.debug("DB-POOL Closing connection from moveToErrorDat ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}

    }

    private int getActiveDfrsCount(Map<String, Boolean> mapDFrs)
    {
    	int activeCount = 0;
    	for (Iterator<Boolean> iterator = mapDFrs.values().iterator(); iterator.hasNext();) {
    		Boolean isActive = iterator.next();
			if (isActive)
			{
				activeCount++;
			}
		}
    	return activeCount;
    }
    private String getCommaSeperatedIds(List<ProcessDatDTO> lstProcessDatDTO)
    {
    	StringBuffer processedFaultsId =  new StringBuffer();
    	ProcessDatDTO processDatDTO;
    	for (Iterator<ProcessDatDTO> iterator = lstProcessDatDTO.iterator(); iterator
				.hasNext();) {
			processDatDTO = iterator.next();
			if (processedFaultsId.length() == 0)
			{
				processedFaultsId.append(processDatDTO.getDatId());
			}
			else
			{
				processedFaultsId.append(","+processDatDTO.getDatId());
			}
			
		}
    	return processedFaultsId.toString();
    }
    
	public static int findNthIndexOf (String str, String needle, int occurence)
    throws IndexOutOfBoundsException {
		int index = -1;
		Pattern p = Pattern.compile(needle, Pattern.MULTILINE);
		Matcher m = p.matcher(str);
		while(m.find()) {
		if (--occurence == 0) {
		    index = m.start();
		    break;
		}
		}
		if (index < 0) throw new IndexOutOfBoundsException();
		return index;
	}

// Binary processing
	

	private void updateBinaryData (ProcessDatDTO mergedProcessDatDto, ProcessDatDTO processDatDTO) throws Exception
	{
		DataInputStream binMergedAnalogInputStream = null;
		DataInputStream binMergedEventsInputStream  = null;
		
		ByteArrayOutputStream binaryAnalogData = new ByteArrayOutputStream();
		ByteArrayOutputStream binaryEventData = new ByteArrayOutputStream();
		DataOutputStream binaryAnalogOutStream = new DataOutputStream(binaryAnalogData);
		DataOutputStream binaryEventsOutStream = new DataOutputStream(binaryEventData);

		if (mergedProcessDatDto.getMergedBinaryAnalogData() != null)
		{
			logger.debug("Already merged analog data length? "+mergedProcessDatDto.getMergedBinaryAnalogData().size());
			binMergedAnalogInputStream = new DataInputStream(new ByteArrayInputStream(mergedProcessDatDto.getMergedBinaryAnalogData().toByteArray()));
		}
		if (mergedProcessDatDto.getMergedBinaryEventData() != null)
		{
			logger.debug("Already merged events data length? "+mergedProcessDatDto.getMergedBinaryEventData().size());
			binMergedEventsInputStream = new DataInputStream(new ByteArrayInputStream( mergedProcessDatDto.getMergedBinaryEventData().toByteArray()));
		}
		DataInputStream binaryDataStream = new DataInputStream(processDatDTO.getBinaryDataStream());
		
		int analogsCnt = 0;
		int eventsCnt = 0;
		int startIndex = 2;
		int endIndex = 0;
		int eventStartIndex = 0;
		boolean isAnalog = false;
		boolean isEvents = false;
		String zeros = "0000000000000000";
		String leadZero;
		short eventHexValue;
		if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
		{
				analogsCnt = processDatDTO.getAnalogs().toString().split(M9kStationConstants.NEWLINE).length;
				endIndex =  analogsCnt+1;
				if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
				{
					eventStartIndex = endIndex+1;
				}
			logger.debug("Total analogsCnt "+analogsCnt);
			logger.debug("Start Index "+startIndex);
			
			logger.debug("End Index in analog box "+endIndex);
			isAnalog = true;
		}
		else
		{
			eventStartIndex = startIndex;
			logger.debug("End Index in events "+endIndex);
		}

		if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
		{
			isEvents = true;	
			eventsCnt = processDatDTO.getEvents().toString().split(M9kStationConstants.NEWLINE).length;
		}
		logger.debug("SHOW: Event start index "+eventStartIndex+" Do we have the events this time? "+isEvents+" Total events count.... "+eventsCnt);

		
		try
		{
			int i = 0;
			int j = 0;
			int analogBytesToRead = (analogsCnt * 2);
			int initialAnalogBytes = analogBytesToRead+4+4; // (4+4)=8 bytes for the first 2 columns for the first dfr has to be included
			int eventBytesToRead = (int)(2 * Math.ceil((float)eventsCnt/16));
			byte[] analogBytes;
			byte[] eventBytes ;
			byte[] mergedAnalogBytes;
			byte[] newMergedAnalogData;
			int mergedEventBytesToRead = 0;
			int eventsSpilledOver = 0;
			byte[] mergedEventBytes;
			short eventData;
			String eventBits = "";
			String newEvents = "";
			int eventByteIndex = 0;

			while (binaryDataStream.available() > 0 && i++ < processDatDTO.getSampleCnt())
			{
//				logger.debug("BINARY: Samples read "+i);
				if (isAnalog)
				{
					if(binMergedAnalogInputStream == null || binMergedAnalogInputStream.available() <= 0)
					{
//						logger.debug("No analog data available to merge. AnalogByes to read "+initialAnalogBytes);
						analogBytes = new byte[initialAnalogBytes];
						binaryDataStream.read(analogBytes);
						binaryAnalogOutStream.write(analogBytes);
					}
					else
					{
//						logger.debug("Analog data available to merge. New AnalogByes to read "+analogBytesToRead);
						analogBytes = new byte[analogBytesToRead];

//						logger.debug("Already available merge data "+mergedProcessDatDto.getMergedAnalogsCount());
						mergedAnalogBytes = new byte[mergedProcessDatDto.getMergedAnalogsCount()];
						newMergedAnalogData = new byte[mergedProcessDatDto.getMergedAnalogsCount() + analogBytesToRead];

						// Read the already merged data
						binMergedAnalogInputStream.read(mergedAnalogBytes);

						// to skip initial 8 bytes as it is already merged
						binaryDataStream.skipBytes(8);
						binaryDataStream.read(analogBytes);

						// Merge the 2 byte arrays, the old merged analog data with newly read analog data
						System.arraycopy(mergedAnalogBytes, 0, newMergedAnalogData, 0, mergedAnalogBytes.length);
						System.arraycopy(analogBytes, 0, newMergedAnalogData, mergedAnalogBytes.length, analogBytes.length);

						binaryAnalogOutStream.write(newMergedAnalogData);		
					}
				}
				if(isEvents)
				{
					if(binMergedEventsInputStream == null || binMergedEventsInputStream.available() <= 0)
					{
//						logger.debug("No already merged events. Events bytes read per samples " + eventBytesToRead);
						if (!isAnalog)
						{
							binaryDataStream.skipBytes(8);
						}
						eventBytes = new byte[eventBytesToRead];
						binaryDataStream.read(eventBytes);
						binaryEventsOutStream.write(eventBytes);
					}
					else
					{
						mergedEventBytesToRead = (int)(2 * Math.floor((float)mergedProcessDatDto.getMergedEventsCount()/16));
//						logger.debug("Already merged events available. Merged Events bytes read per samples " + mergedEventBytesToRead);
						eventsSpilledOver = (int)(mergedProcessDatDto.getMergedEventsCount() % 16);
//						logger.debug("Odd events to be handled "+eventsSpilledOver);
						mergedEventBytes = new byte[mergedEventBytesToRead];
						binMergedEventsInputStream.read(mergedEventBytes);
						binaryEventsOutStream.write(mergedEventBytes);
						if (!isAnalog) // if All events box skip the header index and timestamp  
						{
							binaryDataStream.skipBytes(8); 
						}
						if (eventsSpilledOver > 0) // Events was not divisible by 16 so the newly read events has to be appended to the left over events
						{
							int nofTimesToRead = (int)(Math.ceil((float)eventsCnt/16));							
							eventData = binMergedEventsInputStream.readShort();//Short.reverseBytes(roBuff.getShort());
							eventData = Short.reverseBytes(eventData);
							eventBits = Integer.toBinaryString(eventData);
//							eventBits = new StringBuffer(eventBits).reverse().toString();
//							logger.debug("BIG-BINARY: BEFORE stuffing zeroes: eventBits with spilled over events to be merged with about to read events "+eventBits);
							if (eventBits.length() > eventsSpilledOver)
							{
								eventBits = eventBits.substring(eventBits.length() - eventsSpilledOver);
							}
							else // The extra events will be 0 during the prefault and postfault time and hence it'll be just 0
							{
								leadZero = zeros.substring(0,eventsSpilledOver-eventBits.length());
								eventBits=leadZero.concat(eventBits);
							}
//							logger.debug("BIG-BINARY: AFTER stuffing zeroes eventBits with spilled over events to be merged with about to read events "+eventBits);
							//								leadZero = zeros.substring(0,16-eventBits.length());
							//								eventBits=leadZero.concat(eventBits);
							eventByteIndex = 0;
//							logger.debug("BINARY: nofTimesToRead "+nofTimesToRead+" eventByteIndx "+eventByteIndex);
							while (eventByteIndex++ < nofTimesToRead) // Number of bytes to read from the event stream
							{
//								logger.debug("BINARY: eventByteIndx "+eventByteIndex);
								eventData = binaryDataStream.readShort(); // A short consist of 16 events
								eventData = Short.reverseBytes(eventData);
								newEvents = Integer.toBinaryString(eventData);
//								newEvents = new StringBuffer(newEvents).reverse().toString();
//								logger.debug("BIG-BINARY: newEvents read before "+newEvents);
								if (newEvents.length() > 16)
								{
									newEvents = newEvents.substring(newEvents.length() - 16);
								}
								if (eventByteIndex < nofTimesToRead)
								{
									leadZero = zeros.substring(0,16-newEvents.length());
									newEvents=leadZero.concat(newEvents);
								}
								else
								{
									if (newEvents.length() > (eventsCnt-(16*(nofTimesToRead-1))))
									{
										newEvents = newEvents.substring(newEvents.length()-(eventsCnt-(16*(nofTimesToRead-1))));
									}
									else
									{
										leadZero = zeros.substring(0,(eventsCnt-(16*(nofTimesToRead-1)))-newEvents.length()); // calculate final events left to read
										newEvents=leadZero.concat(newEvents);																			
									}
								}

//								logger.debug("BIG-BINARY: newEvents read after "+newEvents);
//								j = 0;
								j=newEvents.length();
//								logger.debug("BINARY: eventsIndexCnt "+eventsIndexCnt +" eventsCnt "+eventsCnt);
//								while(j < newEvents.length()) // Total Number of events in this particular DFR
								while(j > 0) // Total Number of events in this particular DFR
								{
//									logger.debug("BIG-BINARY: eventsBits "+eventBits +" j "+j+" NewEvents "+newEvents);
//									eventBits = newEvents.substring(j++, j) + eventBits; // Append to the already available events, if any.
									eventBits = newEvents.substring((j-1), j--) + eventBits; // Append to the already available events, if any.
									if (eventBits.length() == 16)
									{
//										logger.debug("BIG-BINARY: eventsBits "+eventBits);
										eventHexValue = (short)convertEventToHex(eventBits);
//										logger.debug("BIG-BINARY: eventsBits hex value "+eventHexValue);
										binaryEventsOutStream.writeShort(Short.reverseBytes(eventHexValue));
										eventBits = "";
									}
								}
//								logger.debug("BIG-BINARY: Evnets left to be written "+eventBits);
								if (!eventBits.isEmpty() && eventByteIndex == nofTimesToRead)
								{
										leadZero = zeros.substring(0,16-eventBits.length());
										eventBits=leadZero.concat(eventBits);
										eventHexValue = (short)convertEventToHex(eventBits);
										binaryEventsOutStream.writeShort(Short.reverseBytes(eventHexValue));
										break;
								}
							}
						}
						else // No adjustments needed as the events count was divisible by 16
						{
							eventBytesToRead = (int)(2 * Math.ceil((float)eventsCnt/16));
//							binaryDataStream.skipBytes(8);
//							logger.debug("Already merged events available but no spill overs. Events bytes to be read per samples " + eventBytesToRead);
							eventBytes = new byte[eventBytesToRead];
	
							// Read the events as it is and write in to the resultant stream
							binaryDataStream.read(eventBytes);
							binaryEventsOutStream.write(eventBytes);
						}
	
					}
				}
			}
			if (i < processDatDTO.getSampleCnt())
			{
				logger.error("ERROR: Incorrect Data format. Quitting the merge process.");
				throw new M9000Exception("Incorrect Data format.");
			}
			if (isAnalog)
			{
				if(mergedProcessDatDto.getMergedAnalogsCount() <= 0)
				{
					logger.debug("BINARY: initialAnalogBytes "+initialAnalogBytes);
					mergedProcessDatDto.setMergedAnalogsCount(initialAnalogBytes);
				}
				else
				{
					logger.debug("BINARY: already available analog cnt "+mergedProcessDatDto.getMergedAnalogsCount()+" Plus the new analog cnt "+analogBytesToRead);
					mergedProcessDatDto.setMergedAnalogsCount(mergedProcessDatDto.getMergedAnalogsCount() + analogBytesToRead);
				}
				mergedProcessDatDto.setMergedBinaryAnalogData(binaryAnalogData);
				logger.debug("BINARY: Merged binary analog cnt "+mergedProcessDatDto.getMergedAnalogsCount());
				logger.debug("BINARY: Merged binary analog size "+binaryAnalogData.size());
			}
			if (isEvents)
			{
				if(mergedProcessDatDto.getMergedEventsCount() <= 0)
				{
					logger.debug("BINARY: initial Event count "+eventsCnt);
					mergedProcessDatDto.setMergedEventsCount(eventsCnt);
				}
				else
				{
					logger.debug("BINARY: already available events cnt "+mergedProcessDatDto.getMergedEventsCount()+" Plus the new events cnt "+eventsCnt);
					mergedProcessDatDto.setMergedEventsCount(mergedProcessDatDto.getMergedEventsCount()+eventsCnt);
				}
				mergedProcessDatDto.setMergedBinaryEventData(binaryEventData);
				logger.debug("BINARY: Merged binary events cnt "+mergedProcessDatDto.getMergedEventsCount());
				logger.debug("BINARY: Merged binary events size "+binaryEventData.size());
			}
		}
		catch (Exception e) {
//			e.printStackTrace();
			logger.error("Error occured during binary data update for DFRId "+processDatDTO.getDfrId(),e);
			try {
				if (M9kStationUtil.getTriggerNotify().equalsIgnoreCase(M9kStationConstants.ENABLE))
				{
					M9kStationUtil.sendNotification(M9kSeverityLevels.ERROR.name(), "There was a fault at this station "+M9kStationDBUtil.getStationTitle()+" Due to issue with the data format in chassis DFR"+processDatDTO.getDfrId()+" couldn't create COMTRADE fault. Moved the raw data to error database: error_dat");
				}
			} catch (M9000Exception e1) {
				logger.error("Unable to notify trigger event to master",e1);
			}

			throw e;
		}
		finally
		{
			try
			{
				if (binaryAnalogData != null)
				{
					binaryAnalogData.close();
					binaryAnalogData = null;
				}
				if (binaryAnalogOutStream != null)
				{
					binaryAnalogOutStream.close();
					binaryAnalogOutStream = null;
				}
				if (binaryEventData != null)
				{
					binaryEventData.close();
					binaryEventData = null;
				}
				if (binaryEventsOutStream != null)
				{
					binaryEventsOutStream.close();
					binaryEventsOutStream = null;
				}
				if (binMergedAnalogInputStream != null)
				{
					binMergedAnalogInputStream.close();
					binMergedAnalogInputStream = null;
				}
				if (binMergedEventsInputStream != null)
				{
					binMergedEventsInputStream.close();
					binMergedEventsInputStream = null;
				}
				if (binaryDataStream != null)
				{
					binaryDataStream.close();
					binaryDataStream = null;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		logger.debug("Returning from updateBinaryData");
	}

	private void updateDummyBinaryData (ProcessDatDTO mergedProcessDatDto) throws Exception
	{
		DataInputStream binMergedAnalogInputStream = null;
		DataInputStream binMergedEventsInputStream = null;
		
		ByteArrayOutputStream binaryAnalogData = new ByteArrayOutputStream();
		ByteArrayOutputStream binaryEventData = new ByteArrayOutputStream();
		DataOutputStream binaryAnalogOutStream = new DataOutputStream(binaryAnalogData);
		DataOutputStream binaryEventsOutStream = new DataOutputStream(binaryEventData);
//		logger.debug("Already mrged analog data avail? "+mergedProcessDatDto.getMergedBinaryAnalogData().size());
//		logger.debug("Already mrged events data avail? "+mergedProcessDatDto.getMergedBinaryEventData().size());
		if (mergedProcessDatDto.getMergedBinaryAnalogData() != null)
		{
			binMergedAnalogInputStream = new DataInputStream(new ByteArrayInputStream(mergedProcessDatDto.getMergedBinaryAnalogData().toByteArray()));
		}
		if (mergedProcessDatDto.getMergedBinaryEventData() != null)
		{
			binMergedEventsInputStream = new DataInputStream(new ByteArrayInputStream( mergedProcessDatDto.getMergedBinaryEventData().toByteArray()));
		}
		
		int iCnt = 0;
		int analogsCnt = 0;
		int eventsCnt = 0;
		int startIndex = 2;
		int endIndex = 0;
		int eventStartIndex = 0;
		boolean isAnalog = false;
		boolean isEvents = false;
		String zeros = "0000000000000000";
		String leadZero;
		short eventHexValue;
		int totalAnalogs = 0;
		int totalEvents = 0;
		logger.debug("Current dfr "+currentDfr.getDfrId());
		totalAnalogs = currentDfr.getAnalogChnlCnt();
		totalEvents = currentDfr.getDigitalChnlCnt();
		long timestamp;
		
		if (totalAnalogs > 0)
		{
				analogsCnt = totalAnalogs;
				endIndex =  analogsCnt+1;
				if (totalEvents > 0)
				{
					eventStartIndex = endIndex+1;
				}
			logger.debug("Total analogsCnt "+analogsCnt);
			logger.debug("Start Index "+startIndex);
			
			logger.debug("End Index in analog box "+endIndex);
			isAnalog = true;
		}
		else
		{
			eventStartIndex = startIndex;
			logger.debug("End Index in events "+endIndex);
		}

		if (totalEvents > 0)
		{
			isEvents = true;	
			eventsCnt = totalEvents;
		}
		logger.debug("SHOW: Event start index "+eventStartIndex+" Do we have the events this time? "+isEvents+" Events count "+eventsCnt);

		
		try
		{
			int i = 0;
			int analogBytesToRead = (analogsCnt * 2);
			int initialAnalogBytes = analogBytesToRead+4+4;  // (4+4)=8 bytes for the first 2 columns for the first dfr has to be included 
			int eventShortsToWrite = (int)(Math.ceil((float)eventsCnt/16));
			byte[] mergedAnalogBytes;
			int mergedEventBytesToRead = 0;
			int eventsSpilledOver = 0;
			byte[] mergedEventBytes;
			short eventData;
			String eventBits = "";
			Integer dummyTimestamp = 0xFFFFFFFF;
			Integer dummyAnalog = 0x8000;
			Short dummyEvents = 0x0000000000000000;
			short shortAnalog;
			while (i++ < mergedProcessDatDto.getSampleCnt())
			{
				if (isAnalog)
				{
					if(binMergedAnalogInputStream == null || binMergedAnalogInputStream.available() <= 0)
					{
						timestamp = (long)((iCnt++ /mergedProcessDatDto.getSampleRate())*1000000);
						binaryAnalogOutStream.writeInt(Integer.reverseBytes(iCnt));
						if (timestamp <= (long)Integer.MAX_VALUE)
						{
							binaryAnalogOutStream.writeInt(Integer.reverseBytes((int)timestamp));
						}
						else
						{
							binaryAnalogOutStream.writeInt(Integer.reverseBytes(dummyTimestamp));
						}
						for (int k = 0; k < totalAnalogs; k++) {
							shortAnalog = dummyAnalog.shortValue();
							binaryAnalogOutStream.writeShort(Short.reverseBytes(shortAnalog));							
						}
//						mergedAnalogsCount = analogBytesToRead;
					}
					else
					{

						mergedAnalogBytes = new byte[mergedProcessDatDto.getMergedAnalogsCount()];

						// Read the already merged data
						binMergedAnalogInputStream.read(mergedAnalogBytes);
						binaryAnalogOutStream.write(mergedAnalogBytes);		

						for (int k = 0; k < totalAnalogs; k++) {
							shortAnalog = dummyAnalog.shortValue();
							binaryAnalogOutStream.writeShort(Short.reverseBytes(shortAnalog));							
						}


//						mergedAnalogsCount = mergedProcessDatDto.getMergedAnalogsCount() + analogBytesToRead;
					}
					

				}
				if(isEvents)
				{
					if(binMergedEventsInputStream == null || binMergedEventsInputStream.available() <= 0)
					{
						for (int k = 0; k < eventShortsToWrite; k++) {
							// START:14-Dec-2020 - Fixed Dummy events issues 
//							binaryEventsOutStream.writeShort(dummyEvents);
							binaryEventsOutStream.writeShort(Short.reverseBytes(dummyEvents));
							// END: 14-Dec-2020
						}
						
					}
					else
					{
						mergedEventBytesToRead = (int)(2 * Math.floor((float)mergedProcessDatDto.getMergedEventsCount()/16));
						eventsSpilledOver = (int)(mergedProcessDatDto.getMergedEventsCount() % 16);
						mergedEventBytes = new byte[mergedEventBytesToRead];
						binMergedEventsInputStream.read(mergedEventBytes);
						binaryEventsOutStream.write(mergedEventBytes);
						if (eventsSpilledOver > 0) // Events was not divisible by 16 so the newly read events has to be appended to the left over events
						{
							eventData = binMergedEventsInputStream.readShort();//Short.reverseBytes(roBuff.getShort());
							eventData = Short.reverseBytes(eventData);
							eventBits = Integer.toBinaryString(eventData);
//							logger.debug("DUMMY-BINARY: eventBits spilled over "+eventBits);
							if (eventBits.length() > eventsSpilledOver)
							{
								eventBits = eventBits.substring(eventBits.length() - eventsSpilledOver);
							}
							else // The extra events will be 0 during the prefault and postfault time and hence it'll be just 0
							{
								leadZero = zeros.substring(0,eventsSpilledOver-eventBits.length());
								eventBits=leadZero.concat(eventBits);
							}
//							logger.debug("DUMMY-BINARY: After padding eventBits spilled over "+eventBits);
							int nofTimesToWrite = (int)(Math.ceil((float)eventsCnt/16));
							while(nofTimesToWrite-- > 0) // Total Number of events in this particular DFR
							{
								if (eventBits.length() > 0)
								{
//									eventBits = "0" + eventBits; // Append to the already available events, if any.
									leadZero = zeros.substring(0,eventBits.length());
									eventBits=leadZero.concat(eventBits);
//									logger.debug("DUMMY-BINARY:Before sending to  "+eventBits);
									eventHexValue = (short) convertEventToHex(eventBits);
//									logger.debug("DUMMY-BINARY:Hex value returned  "+eventHexValue);
									binaryEventsOutStream.writeShort(Short.reverseBytes(eventHexValue));
									eventBits = "";
									// START:14-Dec-2020 - Fixed Dummy events issues
//									binaryEventsOutStream.writeShort(dummyEvents);
									binaryEventsOutStream.writeShort(Short.reverseBytes(dummyEvents));
									// END: 14-Dec-2020
								}
								else
								{
									// START:14-Dec-2020 - Fixed Dummy events issues
//									binaryEventsOutStream.writeShort(dummyEvents);
									binaryEventsOutStream.writeShort(Short.reverseBytes(dummyEvents));
									// END: 14-Dec-2020
								}
							}
						}
						else // No adjustments needed as the events count was divisible by 16
						{
							for (int k = 0; k < eventShortsToWrite; k++) {
								// START:14-Dec-2020 - Fixed Dummy events issues
//								binaryEventsOutStream.writeShort(dummyEvents);
								binaryEventsOutStream.writeShort(Short.reverseBytes(dummyEvents));
								// END: 14-Dec-2020
							}
						}
	
					}

				}
			}
			if (isAnalog)
			{
				updateDummyAnalogDetails(mergedProcessDatDto);
				
				if(mergedProcessDatDto.getMergedAnalogsCount() <= 0)
				{
					logger.debug("DUMMY-BINARY: initialAnalogBytes "+initialAnalogBytes);
					mergedProcessDatDto.setMergedAnalogsCount(initialAnalogBytes);
				}
				else
				{
					logger.debug("DUMMY-BINARY: already available analog cnt "+mergedProcessDatDto.getMergedAnalogsCount()+" Plus the new analog cnt "+analogBytesToRead);
					mergedProcessDatDto.setMergedAnalogsCount(mergedProcessDatDto.getMergedAnalogsCount() + analogBytesToRead);
				}
				mergedProcessDatDto.setMergedBinaryAnalogData(binaryAnalogData);
			}
			if (isEvents)
			{
				updateDummyEventDetails(mergedProcessDatDto);

				if(mergedProcessDatDto.getMergedEventsCount() <= 0)
				{
					logger.debug("DUMMY-BINARY: initial Event count "+eventsCnt);
					mergedProcessDatDto.setMergedEventsCount(eventsCnt);
				}
				else
				{
					logger.debug("DUMMY-BINARY: already available events cnt "+mergedProcessDatDto.getMergedEventsCount()+" Plus the new events cnt "+eventsCnt);
					mergedProcessDatDto.setMergedEventsCount(mergedProcessDatDto.getMergedEventsCount()+eventsCnt);
				}
				mergedProcessDatDto.setMergedBinaryEventData(binaryEventData);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Error occured during analog data update",e);
			throw e;
		}
		finally
		{
			try
			{
				if (binaryAnalogData != null)
				{
					binaryAnalogData.close();
					binaryAnalogData = null;
				}
				if (binaryAnalogOutStream != null)
				{
					binaryAnalogOutStream.close();
					binaryAnalogOutStream = null;
				}
				if (binaryEventData != null)
				{
					binaryEventData.close();
					binaryEventData = null;
				}
				if (binaryEventsOutStream != null)
				{
					binaryEventsOutStream.close();
					binaryEventsOutStream = null;
				}
				if (binMergedAnalogInputStream != null)
				{
					binMergedAnalogInputStream.close();
					binMergedAnalogInputStream = null;
				}
				if (binMergedEventsInputStream != null)
				{
					binMergedEventsInputStream.close();
					binMergedEventsInputStream = null;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		logger.debug("Returning from DummyupdateData");
	}
	
	private String getFaultLocationDetails(ProcessDatDTO mergedProcessDatDto) {
		logger.debug("Entered getFaultLocationDetails");
		// Because merged analog count is the total analog bytes to read, we'll over write here to give total analog counts reverse engineering ((analogsCnt *2)+4+4)
		mergedProcessDatDto.setMergedAnalogsCount((mergedProcessDatDto.getMergedAnalogsCount()-4-4)/2);
		logger.debug("Merged analog count "+mergedProcessDatDto.getMergedAnalogsCount());
		logger.debug("Merged events count "+mergedProcessDatDto.getMergedEventsCount());
		
		StringBuffer faultLocationDetails = new StringBuffer();
		Map<LineGroupsAlgorithm, M9kFaultReport> mapFaultReport = null;
		M9kFaultReport faultReport;
		
		if (mergedProcessDatDto.getMergedEventsCount() > 0)
		{
			M9kFaultLocation m9kFaultLocation = new M9kFaultLocation(mergedProcessDatDto);
			if (m9kFaultLocation.isContinueProcessing())
			{
				try {
					mapFaultReport = m9kFaultLocation.findFaultLocation(false);
					logger.debug("Returned from findFaultLocation "+mapFaultReport);
					if (mapFaultReport != null)
					{
						for (LineGroupsAlgorithm lineIterator : mapFaultReport.keySet()) {
							faultReport = mapFaultReport.get(lineIterator);
							if (faultReport != null)
							{
								if (faultLocationDetails.length() > 0)
								{
									faultLocationDetails.append(M9kStationConstants.NEWLINE);
								}
								if (lineIterator.getLineGroupName() != null || !lineIterator.getLineGroupName().isEmpty())
								{
									faultLocationDetails.append("Line Name: "+lineIterator.getLineGroupName()+M9kStationConstants.NEWLINE);
								}
								else
								{
									faultLocationDetails.append("Line Name: "+lineIterator.getName()+M9kStationConstants.NEWLINE);
								}
								faultLocationDetails.append(faultReport.getFaultReportDetails());
							}
						}
					}
				} catch (M9000Exception e) {
					logger.error("There is an issue with fault location calculation. ",e);
				} 
				catch (Exception e) {
					logger.error("There is an issue with fault location calculation. "+e);
				}
			}
		}
		else
		{
			logger.info("No events. Fault location cannot be calculated");
		}

		return faultLocationDetails.toString();
	}

	
	private int convertEventToHex(String events)
	{
		int hexVal;
//		logger.debug("BINARY-HEX: Events string to be converted "+events);
		Integer val = Integer.parseInt(events,2);
//		logger.debug("BINARY-HEX: Long value of Events string to be converted "+val);
		String zeros = "0000";
		String hexString = Integer.toHexString(val);
//		logger.debug("BINARY-HEX: before padding Hex value of converted "+hexString);
		if (hexString.length() > 4)
		{
			hexString = hexString.substring(0,4);
		}
		else
		{
			String leadZero = zeros.substring(0,4-hexString.length());
			hexString=leadZero.concat(hexString);			
		}
//		hexString = new StringBuffer(hexString).reverse().toString();
//		logger.debug("BINARY-HEX: After padding Hex value converted "+hexString);
		hexVal = Integer.parseInt(hexString,16);
//		logger.debug("BINARY-HEX: Integer Hex value aafter conversion to be returned "+hexVal);
		return hexVal;
	}

	/**
	 * @return the userComments
	 */
	public String getUserComments() {
		return userComments;
	}

	/**
	 * @param userComments the userComments to set
	 */
	public void setUserComments(String userComments) {
		this.userComments = userComments;
	}
}
