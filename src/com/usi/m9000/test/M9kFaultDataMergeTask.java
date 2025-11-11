package com.usi.m9000.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.usi.AnalogsDocument.Analogs;
import com.usi.DFRDocument.DFR;
import com.usi.EventsDocument.Events;
import com.usi.SubStationDocument.SubStation;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.dto.ProcessDatDTO;
import com.usi.m9000.station.faultLocation.M9kFaultLocation;
import com.usi.m9000.station.faultLocation.M9kFaultReport;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.xml.M9000XmlConfig;


public class M9kFaultDataMergeTask{
	static boolean booStatus = true;
	private int signature;
	private int stationId;
	private String stationConfigXml;
	private SubStation subStation;
	private M9000XmlConfig m9kConfig = null;
	private Connection mysqlConn;
	boolean flagProcess = true;
	
	Map<String, Boolean> mapDfrsStatus = null;
	DfrDTO currentDfr; 
	static List<DfrDTO> lstDfrs = null;
	int analogOffset = 1;
	int eventOffset = 1;


	final static String DUMMY_ANALOG_CFG = ",V,1,0,0,-32768,32767,1,10,P";

	public M9kFaultDataMergeTask(int stationId, int signature)
	{
		this.stationId = stationId;
		this.signature = signature;
		System.out.println("Entered M9kFaultDataProcessTask constructor");
		try {
			stationConfigXml = getLocalConfigXml();
			m9kConfig = new M9000XmlConfig();
			m9kConfig.loadStationXmlFile(new StringReader(stationConfigXml));
			subStation = m9kConfig.getSubstation();
			lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
		} catch (M9000Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		int totalNoOfDfrs =  subStation.getDFRs().sizeOfDFRArray();
		mapDfrsStatus = new HashMap<String, Boolean>(totalNoOfDfrs);
		for (int i = 1; i <= totalNoOfDfrs; i++) {
			mapDfrsStatus.put("DFR"+i,false);
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


//			m9kTriggerProcessor.crossTriggerAll(triggersScheduleDTO);
			lstProcessDatDTO = getDatRecordToProcess();
			end = System.currentTimeMillis();
			System.out.println("Time to execute getDatRecordToProcess "+(end-start));
			
			System.out.println("Total number of dat records to process "+lstProcessDatDTO.size());
//			stmt.setFetchSize(Integer.MIN_VALUE);
			if (lstProcessDatDTO.size() > 0)
			{
				System.out.println("About to process the dat records"+lstProcessDatDTO);
				pushProcessedDatToStaging(lstProcessDatDTO);
				
			}
			else
			{
//	    		M9kLED.setLed(LedName.TRIGGER, LedState.YELLOW);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception occured in processFaultRecordsFromDat "+ e.getCause());
		}
		return totalRecordsInserted;
	}
	
	private void pushProcessedDatToStaging(List<ProcessDatDTO> lstReadyToPushDat)
	{
		PreparedStatement psInsert = null;
		int result = -1;
		ProcessDatDTO mergedProcessDatDto = null;
		ByteArrayInputStream binaryDataInputStream =  null;
//		PreparedStatement psDelete = null;
		try {
			System.out.println("DB-POOL New connection from pushProcessedDatToStaging ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			mergedProcessDatDto = processAndMergeFaults(lstReadyToPushDat);
			System.out.println("Final merged data "+mergedProcessDatDto);
			String faultLocationDetails = getFaultLocationDetails(mergedProcessDatDto);
			
			if (mergedProcessDatDto.getData() != null)
			{
				psInsert = mysqlConn.prepareStatement("insert into dat_staging(stationId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, data, faultLocation) " +
	//					"select dfrId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, data from dat a where a.id IN ("+getCommaSeperatedIds(lstReadyToPushDat)+")");
						"values( ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" );
			}
			else
			{
				psInsert = mysqlConn.prepareStatement("insert into dat_staging(stationId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, dataBlob, faultLocation) " +
						//					"select dfrId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, data from dat a where a.id IN ("+getCommaSeperatedIds(lstReadyToPushDat)+")");
											"values( ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" );				
			}
			int iIndex = 1;
//			psInsert.setInt(iIndex++, mergedProcessDatDto.getDfrId());
			psInsert.setInt(iIndex++,getStationId());
			psInsert.setString(iIndex++, mergedProcessDatDto.getType());
			psInsert.setLong(iIndex++, mergedProcessDatDto.getTsPrefault());
			psInsert.setLong(iIndex++, mergedProcessDatDto.getTsTrigger());
			psInsert.setDouble(iIndex++, mergedProcessDatDto.getLineFreq());
			psInsert.setDouble(iIndex++, mergedProcessDatDto.getSampleRate());
			psInsert.setInt(iIndex++, mergedProcessDatDto.getSampleCnt());
			psInsert.setString(iIndex++, mergedProcessDatDto.getAnalogs().toString());
			psInsert.setString(iIndex++, mergedProcessDatDto.getEvents().toString());
			if(mergedProcessDatDto.getData() != null)
			{
				psInsert.setString(iIndex++, mergedProcessDatDto.getData().toString());
			}
			else
			{
				binaryDataInputStream = new ByteArrayInputStream(mergedProcessDatDto.getBinaryData().toByteArray());
				psInsert.setBinaryStream(iIndex++, binaryDataInputStream, mergedProcessDatDto.getBinaryData().toByteArray().length);
			}
			psInsert.setString(iIndex++, faultLocationDetails);
			result = psInsert.executeUpdate();
				// Remove the ids from the process dat
				
			psInsert.close();
//			System.out.println("ids from dat "+ getCommaSeperatedIds(lstReadyToPushDat)+" have been pushed to staging");
			if (mergedProcessDatDto != null)
			{
				mergedProcessDatDto.setData(null);
				mergedProcessDatDto = null;
			}			
		} catch (SQLException sqle) {
			System.out.println("SQl Exception occured in pushProcessedDatToStaging. Stopping the proces "+ sqle);
			result = -1;
		}
		catch (Exception e) {
			System.out.println("Exception occured in pushProcessedDatToStaging. Stopping the proces "+ e);
			result = -1;
			
		}
		finally
		{
			if (mergedProcessDatDto != null)
			{
				mergedProcessDatDto.setData(null);
				mergedProcessDatDto = null;
			}
			if (result == -1)
			{
//				moveToErrorDat(getCommaSeperatedIds(lstReadyToPushDat));
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
					System.out.println("DB-POOL Closing connection from pushProcessedDatToStaging ");
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				System.out.println("Error in cleanup"+ e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	

	private ProcessDatDTO processAndMergeFaults(List<ProcessDatDTO> lstReadyToPushDat) throws Exception
	{
//		System.out.println("MERGE: lstReadyToPushDat - lst of processDat before sort "+lstReadyToPushDat);
		// Sort ascending order of the dfr id
		Collections.sort(lstReadyToPushDat, new Comparator<ProcessDatDTO>() {

			@Override
			public int compare(ProcessDatDTO o1, ProcessDatDTO o2) {
//				return ((o1.getDfrId() < o2.getDfrId())?0:1);
				return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
			}
		});
//		System.out.println("MERGE: lstReadyToPushDat - lst of processDat after sort "+lstReadyToPushDat);
//		System.out.println("MERGE: lst pf dfr before sort "+lstDfrs);
		// Sort ascending order of the dfr id
		Collections.sort(lstDfrs, new Comparator<DfrDTO>() {

			@Override
			public int compare(DfrDTO o1, DfrDTO o2) {
//				return ((o1.getDfrId() < o2.getDfrId())?0:1);
				return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
			}
		});
//		System.out.println("MERGE: lst pf dfr after sort "+lstDfrs);
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
//			System.out.println(" mapDfrsStatus "+mapDfrsStatus+" DFR DTO "+currentDfr.getDfrId());
//			System.out.println(" mapDfrsStatus "+mapDfrsStatus.get(("DFR"+currentDfr.getDfrId()))+" DFR DTO "+currentDfr.getDfrId());
//			System.out.println("i = "+i+" Is (!mapDfrsStatus.get(dfrDto.getDfrId()) && i==1) "+(!mapDfrsStatus.get(("DFR"+currentDfr.getDfrId())) && i==1));
			
//			if (mergedProcessDatDto != null)
//			{
//				System.out.println("MERGE: the MergedAnalogs at the start "+mergedProcessDatDto.getAnalogs());
//			}
			if (!mapDfrsStatus.get("DFR"+currentDfr.getDfrId())) // current dfr is down
			{
				if (i == 1)// DFR1 is down
				{
//				System.out.println("MERGE: Dummy data for "+currentDfr+" \n value of i: "+i+" active dfr index "+iActiveDfrIndex);
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
//					System.out.println("MERGE: Dummy data for "+processDatDTO+" \n value of i: "+i+" active dfr index "+iActiveDfrIndex);
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
			}
			else
			{
//				System.out.println("MERGE: iActiveDfrIndex "+iActiveDfrIndex+" lstReadyToPushDat "+lstReadyToPushDat);
				processDatDTO = lstReadyToPushDat.get(iActiveDfrIndex++);
//				System.out.println("MERGE: the Analogs at the start "+processDatDTO.getAnalogs());
				if (i == 1)
				{
//					System.out.println("MERGE: Real data for "+processDatDTO+" \n value of i: "+i+" active dfr index "+iActiveDfrIndex);
					mergedProcessDatDto = new ProcessDatDTO();
					mergedProcessDatDto.setType(processDatDTO.getType());
					mergedProcessDatDto.setTsPrefault(processDatDTO.getTsPrefault());
					mergedProcessDatDto.setTsTrigger(processDatDTO.getTsTrigger());
					mergedProcessDatDto.setLineFreq(processDatDTO.getLineFreq());
					mergedProcessDatDto.setSampleRate(processDatDTO.getSampleRate());
					mergedProcessDatDto.setSampleCnt(processDatDTO.getSampleCnt());
					mergedProcessDatDto.setAnalogs(processDatDTO.getAnalogs());
					processDatDTO.setAnalogOffset(analogOffset);
					processDatDTO.setEventsOffset(eventOffset);
					if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
					{
						updatedEventData = updateEventOffset(processDatDTO);
						mergedProcessDatDto.setEvents(new StringBuffer(updatedEventData));
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
//					System.out.println("MERGE: Real data for "+processDatDTO+" \n value of i: "+i+" active dfr index "+iActiveDfrIndex);
					processDatDTO.setAnalogOffset(analogOffset);
					processDatDTO.setEventsOffset(eventOffset);
	//				mergedProcessDatDto.setSampleCnt(mergedProcessDatDto.getSampleCnt()+processDatDTO.getSampleCnt());
	//				System.out.println("Samples count "+mergedProcessDatDto.getSampleCnt());
	
//					System.out.println("Beforeeeee updateData the object "+processDatDTO);
//					System.out.println("MERGE:Before updateData the Analogs "+processDatDTO.getAnalogs());
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
		//					mergedProcessDatDto.getAnalogs().append("\n");
							mergedProcessDatDto.getAnalogs().append(updatedAnalogData);
						}
		
						if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
						{
							updatedEventData = updateEventOffset(processDatDTO);
		//					mergedProcessDatDto.getEvents().append("\n");
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
						e.printStackTrace();
						System.out.println("Error occured while updating offsets "+e);
						throw e;
					}
					System.out.println("Offsets updated...");
				}
				System.out.println("About to split analogs and events and store it in a string array");
				try
				{
					if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
					{
						analogs = processDatDTO.getAnalogs().toString().split("\n"); 
						analogOffset += analogs.length;
					}
					if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
					{
						events = processDatDTO.getEvents().toString().split("\n"); 
						eventOffset += events.length;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error occured while updating analogs and events string array "+e);
					throw e;
				}
			}
			i++;

		}
//		System.out.println("Before set Merged data th object "+mergedProcessDatDto);
//		System.out.println("Before calling setMergedData mergeEventData "+mergedProcessDatDto.getMergedEventData());
		System.out.println("About to set merged data");
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
			e.printStackTrace();
			System.out.println("Error occured in setMergedData "+e);
			throw e;
		}
//		System.out.println("After set merge Data returned, data size "+mergedProcessDatDto.getData().length());
		
//		System.out.println("Merged process dat dto "+mergedProcessDatDto);
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
//			System.out.println("MERGE: the Analogs at the start "+processDatDTO.getAnalogs());
//			if (mergedProcessDatDto != null)
//			{
//				System.out.println("MERGE: the MergedAnalogs at the start "+mergedProcessDatDto.getAnalogs());
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
////				System.out.println("Samples count "+mergedProcessDatDto.getSampleCnt());
//
////				System.out.println("Before updateData the object "+processDatDTO);
//				updateDummyData(mergedProcessDatDto, i);
//				
//
//				if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
//				{
//					updatedAnalogData = updateAnalogOffset(processDatDTO);
////					mergedProcessDatDto.getAnalogs().append("\n");
//					mergedProcessDatDto.getAnalogs().append(updatedAnalogData);
//				}
//
//				if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
//				{
//					updatedEventData = updateEventOffset(processDatDTO);
////					mergedProcessDatDto.getEvents().append("\n");
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
////				System.out.println("Samples count "+mergedProcessDatDto.getSampleCnt());
//
//				System.out.println("Beforeeeee updateData the object "+processDatDTO);
//				System.out.println("MERGE:Before updateData the Analogs "+processDatDTO.getAnalogs());
//				updateData(mergedProcessDatDto, processDatDTO);
//				
//
//				if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
//				{
//					updatedAnalogData = updateAnalogOffset(processDatDTO);
////					mergedProcessDatDto.getAnalogs().append("\n");
//					mergedProcessDatDto.getAnalogs().append(updatedAnalogData);
//				}
//
//				if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
//				{
//					updatedEventData = updateEventOffset(processDatDTO);
////					mergedProcessDatDto.getEvents().append("\n");
//					mergedProcessDatDto.getEvents().append(updatedEventData);
//				}
//
//			}
//			i++;
//			
//			if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
//			{
//				analogs = processDatDTO.getAnalogs().toString().split("\n"); 
//				analogOffset += analogs.length;
//			}
//			if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
//			{
//				events = processDatDTO.getEvents().toString().split("\n"); 
//				eventOffset += events.length;
//			}
//			
//			
//		}
////		System.out.println("Before set Merged data th object "+mergedProcessDatDto);
////		System.out.println("Before calling setMergedData mergeEventData "+mergedProcessDatDto.getMergedEventData());
//		setMergedData(mergedProcessDatDto);
//		System.out.println("After set merge Data returned, data size "+mergedProcessDatDto.getData().length());
//		
//		System.out.println("Merged process dat dto "+mergedProcessDatDto);
//		return mergedProcessDatDto;
//	}
	
	private String updateAnalogOffset(ProcessDatDTO processDatDTO) throws Exception
	{
		String analogs[];
		StringBuffer newAnalogBuffer = new StringBuffer();
		StringBuffer analogBuffer = processDatDTO.getAnalogs();
		analogs = analogBuffer.toString().split("\n");
		int aOffset = processDatDTO.getAnalogOffset();
		System.out.println("OFFSET analogs offset "+aOffset);
		String newPrefixStr;
		int startIndex;
		for (int i = 0; i < analogs.length; i++) {
			startIndex = findNthIndexOf(analogs[i], ",", 2);
			newPrefixStr = aOffset+","+"A"+aOffset;
//			System.out.println("OFFSET analogs prefix offset "+newPrefixStr);
			newAnalogBuffer.append(newPrefixStr);
			newAnalogBuffer.append(analogs[i].substring(startIndex));
			if (i < analogs.length)
			{
				newAnalogBuffer.append("\n");
			}
			aOffset++;
		}
		return newAnalogBuffer.toString();
		
	}
	
	private String updateEventOffset(ProcessDatDTO processDatDTO) throws Exception
	{
		String events[];
		StringBuffer newEventBuffer = new StringBuffer();
		StringBuffer eventBuffer = processDatDTO.getEvents();
		events = eventBuffer.toString().split("\n");
		int eOffset = processDatDTO.getEventsOffset();
		System.out.println("OFFSET events offset "+eOffset);
		String newPrefixStr = eOffset+","+"E"+eOffset;
		int startIndex ;
		for (int i = 0; i < events.length; i++) {
			if (!isTriggerEvent(events[i]))
			{
				startIndex = findNthIndexOf(events[i],",",2);
				newPrefixStr = eOffset+","+"E"+eOffset;
			}
			else
			{
				startIndex = findNthIndexOf(events[i],",",1);
				newPrefixStr = eOffset+"";
			}
//			System.out.println("SANDY-BOG: OFFSET events prefix offset "+newPrefixStr);
			newEventBuffer.append(newPrefixStr);
			newEventBuffer.append(events[i].substring(startIndex));
			if (i < events.length)
			{
				newEventBuffer.append("\n");
			}
			eOffset++;
		}
//		System.out.println("SANDY-BOG: Events string "+newEventBuffer.toString());
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
//		System.out.println("Event data merged "+mergedEventDataBuffer);
		String data[] = processDatDTO.getData().toString().split("\n");
		System.out.println("SHOW: total data count to update "+data.length);
		String mergedAnalog[] = null;
		String mergedEvent[] = null;
		if (mergedAnalogDataBuffer != null && mergedAnalogDataBuffer.length() > 0)
		{
			mergedAnalog = mergedAnalogDataBuffer.toString().split("\n");
		}
		if (mergedEventDataBuffer != null && mergedEventDataBuffer.length() > 0)
		{
			mergedEvent =  mergedEventDataBuffer.toString().split("\n");
		}

		String dataSplitArray[];
		StringBuffer newMergedAnalogDataBuffer = new StringBuffer();
		StringBuffer newMergedEventDataBuffer = new StringBuffer();

//		System.out.println("Analog String to start with "+data[0]);
		int analogsCnt = 0;
		int startIndex = 2;
//		startIndex = findNthIndexOf(data[0], ",", 2)+1;
		int endIndex = 0;
		int eventStartIndex = 0;
		boolean isAnalog = false;
		boolean isEvents = false;
		if (processDatDTO.getAnalogs() != null && processDatDTO.getAnalogs().length() > 0)
		{
				analogsCnt = processDatDTO.getAnalogs().toString().split("\n").length;
				endIndex =  analogsCnt+1;
				if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
				{
					eventStartIndex = endIndex+1;
				}
			System.out.println("Total analogsCnt "+analogsCnt);
			System.out.println("Start Index "+startIndex);
			
			System.out.println("End Index in analog box "+endIndex);
			isAnalog = true;
		}
		else
		{
			eventStartIndex = startIndex;
			System.out.println("End Index in events "+endIndex);
		}

		if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
		{
			isEvents = true;	
		}
		System.out.println("SHOW: Event start index "+eventStartIndex+" Do we have the events this time? "+isEvents);
		
		try
		{
			if (isAnalog)
			{
				if (mergedAnalog != null)
				{
	//				System.out.println("SHOW: (Merged Analog not null) Total data sample "+data.length);
					for (int i = 0; i < data.length; i++) {
	//					System.out.println("SHOW: In Analog sample Cnt "+i+" Data: "+data[i]);
						dataSplitArray = data[i].split(",");
						newMergedAnalogDataBuffer.append(mergedAnalog[i]);
						for (int j = startIndex; j <= endIndex; j++) {
							newMergedAnalogDataBuffer.append(","+dataSplitArray[j]);
						}
							newMergedAnalogDataBuffer.append("\n");
					}
				}
				else
				{
	//				System.out.println("SHOW: (No Merged event ) Total data sample "+data.length);
					for (int i = 0; i < data.length; i++) {
	//					System.out.println("SHOW: In Analog sample Cnt "+i+" Data: "+data[i]);
						
						dataSplitArray = data[i].split(",");
	//					timestamp = (long)((iCnt/processDatDTO.getSampleRate())*1000000);
						newMergedAnalogDataBuffer.append(dataSplitArray[0] +","+dataSplitArray[1]);
						for (int j = startIndex; j <= endIndex; j++) {
								newMergedAnalogDataBuffer.append(","+dataSplitArray[j]);
						}
							newMergedAnalogDataBuffer.append("\n");
					}
					
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occured during analog data update"+e);
			throw e;
		}
		
		try
		{
			if (isEvents)
			{
				if (mergedEvent != null)
				{
	//				System.out.println("SHOW: (Merged event not null) Total data sample "+data.length);
					for (int i = 0; i < data.length; i++) {
	//					System.out.println("SHOW: In Events sample Cnt "+i+" Data: "+data[i]);
						
						dataSplitArray = data[i].split(",");
						newMergedEventDataBuffer.append(mergedEvent[i]);
						for (int j = eventStartIndex; j < dataSplitArray.length; j++) {
								newMergedEventDataBuffer.append(","+dataSplitArray[j]);
						}
						if (i==0)
						{
							System.out.println("MERGE: ACTUAL events added when megedEvent != null "+newMergedEventDataBuffer);
						}
						newMergedEventDataBuffer.append("\n");
					}
				}
				else
				{
	//				System.out.println("SHOW:(No merged event) Total data sample "+data.length);
					for (int i = 0; i < data.length; i++) {
	//					System.out.println("SHOW: In Events sample Cnt "+i+" Data: "+data[i]);
						dataSplitArray = data[i].split(",");
						for (int j = eventStartIndex; j < dataSplitArray.length; j++) {
							if (j == eventStartIndex)
							{
								newMergedEventDataBuffer.append(dataSplitArray[j]);
	//							System.out.println("Total Events "+dataSplitArray.length);
							}
							else
							{
								newMergedEventDataBuffer.append(","+dataSplitArray[j]);
							}
						}
						if (i==0)
						{
							System.out.println("MERGE: ACTUAL events added when megedEvent == null "+newMergedEventDataBuffer);
						}
	
						newMergedEventDataBuffer.append("\n");
					}
					
					
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occured during events data update"+e);
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
		System.out.println("Returning from updateData");
	}

	private void updateDummyData (ProcessDatDTO mergedProcessDatDto)
	{
		int totalAnalogs = 0;
		int totalDigitals = 0;
		int iCnt = 0;

		System.out.println("Current dfr "+currentDfr.getDfrId());
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
//		System.out.println("Event data merged "+mergedEventDataBuffer);
//		String data[] = processDatDTO.getData().toString().split("\n");
//		System.out.println("SHOW: total data count to update "+data.length);
		String mergedAnalog[] = null;
		String mergedEvent[] = null;
		if (mergedAnalogDataBuffer != null && mergedAnalogDataBuffer.length() > 0)
		{
			mergedAnalog = mergedAnalogDataBuffer.toString().split("\n");
		}
		if (mergedEventDataBuffer != null && mergedEventDataBuffer.length() > 0)
		{
			mergedEvent =  mergedEventDataBuffer.toString().split("\n");
		}

//		String dataSplitArray[];
		StringBuffer newMergedAnalogDataBuffer = new StringBuffer();
		StringBuffer newMergedEventDataBuffer = new StringBuffer();

//		System.out.println("Analog String to start with "+data[0]);
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
//				analogsCnt = processDatDTO.getAnalogs().toString().split("\n").length;
				endIndex =  totalAnalogs+1;
				if (totalDigitals > 0)
				{
					eventStartIndex = endIndex+1;
				}
			System.out.println("Total analogsCnt "+totalAnalogs+" Total digitals "+totalDigitals);
			System.out.println("Start Index "+startIndex);
			
			System.out.println("End Index in analog box "+endIndex);
			isAnalog = true;
		}
		else
		{
			eventStartIndex = startIndex;
			System.out.println("End Index in events "+endIndex);
		}

		if (totalDigitals > 0)
		{
			isEvents = true;	
		}
		System.out.println("SHOW: Event start index "+eventStartIndex+" Do we have the events this time? "+isEvents);
		
		
		if (isAnalog)
		{
			if (mergedAnalog != null)
			{
//				System.out.println("SHOW: (Merged Analog not null) Total data sample "+data.length);
				for (int i = 0; i < mergedProcessDatDto.getSampleCnt(); i++) {
//					System.out.println("SHOW: In Analog sample Cnt "+i+" Data: "+data[i]);
//					dataSplitArray = data[i].split(",");
					newMergedAnalogDataBuffer.append(mergedAnalog[i]);
					for (int j = startIndex; j <= endIndex; j++) {
						newMergedAnalogDataBuffer.append(","+"99999");
					}
						newMergedAnalogDataBuffer.append("\n");
				}
			}
			else
			{
//				System.out.println("SHOW: (No Merged event ) Total data sample "+data.length);
				for (int i = 0; i < mergedProcessDatDto.getSampleCnt(); i++) {
//					System.out.println("SHOW: In Analog sample Cnt "+i+" Data: "+data[i]);
					
//					dataSplitArray = data[i].split(",");
					timestamp = (long)((iCnt++ /mergedProcessDatDto.getSampleRate())*1000000);
					newMergedAnalogDataBuffer.append(iCnt +","+timestamp);
					for (int j = startIndex; j <= endIndex; j++) {
							newMergedAnalogDataBuffer.append(","+"99999");
					}
						newMergedAnalogDataBuffer.append("\n");
				}
				
			}
			updateDummyAnalogDetails(mergedProcessDatDto);

		}
		
		if (isEvents)
		{
			if (mergedEvent != null)
			{
//				System.out.println("SHOW: (Merged event not null) Total data sample "+data.length);
				for (int i = 0; i < mergedProcessDatDto.getSampleCnt(); i++) {
//					System.out.println("SHOW: In Events sample Cnt "+i+" Data: "+data[i]);
					
//					dataSplitArray = data[i].split(",");
					newMergedEventDataBuffer.append(mergedEvent[i]);
					for (int j = eventStartIndex; j < eventStartIndex+totalDigitals; j++) {
							newMergedEventDataBuffer.append(","+"0");
					}
					if (i==0)
					{
						System.out.println("MERGE: DUMMY events added when megedEvent != null "+newMergedEventDataBuffer);
					}
						newMergedEventDataBuffer.append("\n");
				}
			}
			else
			{
//				System.out.println("SHOW:(No merged event) Total data sample "+data.length);
				for (int i = 0; i < mergedProcessDatDto.getSampleCnt(); i++) {
//					System.out.println("SHOW: In Events sample Cnt "+i+" Data: "+data[i]);
//					dataSplitArray = data[i].split(",");
					for (int j = eventStartIndex; j < eventStartIndex+totalDigitals; j++) {
						if (j == eventStartIndex)
						{
							newMergedEventDataBuffer.append("0");
//							System.out.println("Total Events "+dataSplitArray.length);
						}
						else
						{
							newMergedEventDataBuffer.append(","+"0");
						}
					}
					if (i==0)
					{
						System.out.println("MERGE: DUMMY events added when megedEvent == null "+newMergedEventDataBuffer);
					}
						newMergedEventDataBuffer.append("\n");
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
	
	private void updateDummyAnalogDetails(ProcessDatDTO mergedProcessDatDto)
	{
		StringBuffer dummyAnalogCfg = new StringBuffer();
		int chnlCnt = 0;
		int diff = currentDfr.getAnalogChannelStart() - analogOffset ;
		System.out.println("MERGE: current analog offset "+analogOffset+" dfrs analog start chnl no "+currentDfr.getAnalogChannelStart());
		int actualOffsetStart = currentDfr.getAnalogChannelStart();
		int actualOffsetEnd = currentDfr.getAnalogChannelEnd();
		if (diff < 0)
		{
			actualOffsetStart+=Math.abs(diff);
			actualOffsetEnd+=Math.abs(diff);
		}
		System.out.println("MERGE: Actual Analog Start "+actualOffsetStart+" Actual Analog End "+actualOffsetEnd);
		for (int i = actualOffsetStart; i <= actualOffsetEnd; i++) {
			dummyAnalogCfg.append(i+",A"+i+",A"+","+currentDfr.getLstAnalogChannelNames().get(chnlCnt++)+DUMMY_ANALOG_CFG);
			dummyAnalogCfg.append("\n");
		}
		analogOffset = currentDfr.getAnalogChannelEnd()+1;
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
		System.out.println("currentDfr "+currentDfr+" lst Evnt chnl names "+currentDfr.getLstEventChannelNames()+" length "+currentDfr.getLstEventChannelNames().size());
		System.out.println("currentDfr.getDigitalChannelStart() "+currentDfr.getDigitalChannelStart()+" currentDfr.getDigitalChannelEnd() "+currentDfr.getDigitalChannelEnd());
		int diff = currentDfr.getDigitalChannelStart() - eventOffset ;
		System.out.println("MERGE: current digital offset "+eventOffset+" dfrs digital start chnl no "+currentDfr.getDigitalChannelStart());
		int actualOffsetStart = currentDfr.getDigitalChannelStart();
		int actualOffsetEnd = currentDfr.getDigitalChannelEnd();
		if (diff < 0)
		{
			actualOffsetStart+=Math.abs(diff);
			actualOffsetEnd+=Math.abs(diff);
		}
		System.out.println("MERGE: Actual Digital Start "+actualOffsetStart+" Actual Digital End "+actualOffsetEnd);
		
		for (int i = actualOffsetStart; i <= actualOffsetEnd; i++) {
			dummyDigitalCfg.append(i+",E"+i+",,"+currentDfr.getLstEventChannelNames().get(ichnlCnt++)+",0");
			dummyDigitalCfg.append("\n");
		}
		eventOffset = currentDfr.getDigitalChannelEnd()+1;
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
		System.out.println("in set merge data ");
//		System.out.println("In setMergedData mergeAnalogData "+mergedProcessDatDto.getMergedAnalogData());
//		System.out.println("In setMergedData mergeEventData "+mergedProcessDatDto.getMergedEventData());
		String analogData[] = null;
		String eventData[] = null;
		if (mergedProcessDatDto.getMergedAnalogData() != null && mergedProcessDatDto.getMergedAnalogData().length() > 0)
		{
			analogData =  mergedProcessDatDto.getMergedAnalogData().toString().split("\n");
		}
		if (mergedProcessDatDto.getMergedEventData() != null && mergedProcessDatDto.getMergedEventData().length() > 0 )
		{
			eventData = mergedProcessDatDto.getMergedEventData().toString().split("\n");
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
						finalData.append("\n");
					}
				}
			}
			else if (analogData != null)
			{
				for (i = 0; i < analogData.length; i++) {
					finalData.append(analogData[i]);
					if (i < analogData.length)
					{
						finalData.append("\n");
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
						finalData.append("\n");
					}
				}				
			}
			mergedProcessDatDto.setData(finalData);
			finalData = null;
			analogData = null;
			eventData = null;
			mergedProcessDatDto.setMergedAnalogData(null);
			mergedProcessDatDto.setMergedEventData(null);
			System.out.println("After setData in setMergedData "+i);
		}
		catch (Exception e) {
			e.printStackTrace();
			finalData = null;
			analogData = null;
			eventData = null;
			mergedProcessDatDto.setMergedAnalogData(null);
			mergedProcessDatDto.setMergedEventData(null);
			System.out.println("Error occured in setMergedData "+i+e);
			throw e;
		}
		System.out.println("Merging final data done. Returning.");;
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
			System.out.println("BINARY: Analog data size to be merged "+mergedProcessDatDto.getMergedBinaryAnalogData().size());
			binMergedAnalogInputStream = new DataInputStream(new ByteArrayInputStream(mergedProcessDatDto.getMergedBinaryAnalogData().toByteArray()));
		}
		if (mergedProcessDatDto.getMergedBinaryEventData() != null)
		{
			flgEvent = true;
			System.out.println("BINARY: Event data size to be merged "+mergedProcessDatDto.getMergedBinaryEventData().size());
			binMergedEventsInputStream = new DataInputStream(new ByteArrayInputStream( mergedProcessDatDto.getMergedBinaryEventData().toByteArray()));
		}
		System.out.println("BINARY: Total Analog cnt to be merged "+mergedProcessDatDto.getMergedAnalogsCount());
		System.out.println("BINARY: Total Event cnt to be merged "+mergedProcessDatDto.getMergedEventsCount());
		int eventBytesToRead = (int)(2 * Math.ceil((float)mergedProcessDatDto.getMergedEventsCount()/16));
		byte[] mergedAnalogBytes = new byte[mergedProcessDatDto.getMergedAnalogsCount()];
		byte[] mergedEventBytes = new byte[eventBytesToRead];
		byte[] finalMergedData = new byte[mergedProcessDatDto.getMergedAnalogsCount()+eventBytesToRead];
		System.out.println("BINARY: in set merge data: Total sample Cnt "+mergedProcessDatDto.getSampleCnt());
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
						System.out.println("BINARY: Final merged data Size after analog data "+finalMergedData.length+" after reading analog data size "+mergedAnalogBytes.length);
					}
					System.arraycopy(mergedEventBytes, 0, finalMergedData, mergedAnalogBytes.length, mergedEventBytes.length);
					if (i==1)
					{
						System.out.println("BINARY: Final merged data Size after event data and before writing "+finalMergedData.length+" after reading event data size "+mergedEventBytes.length);
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
			e.printStackTrace();
			mergedProcessDatDto.setMergedAnalogData(null);
			mergedProcessDatDto.setMergedEventData(null);
			System.out.println("Error occured in setMergedData "+i+e);
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
		System.out.println("Merging final data done. Returning.");;
	}

    private List<ProcessDatDTO> getDatRecordToProcess()
    {
		PreparedStatement ps = null;
		ResultSet rs= null;
		ProcessDatDTO processDatDTO;
		List<ProcessDatDTO> lstProcessDatDTO = new ArrayList<ProcessDatDTO>();
		System.out.println(" mapDfrsStatus "+mapDfrsStatus);
		int activeDfrs = getActiveDfrsCount(mapDfrsStatus);
		System.out.println("Active dfrs at the start "+activeDfrs);
		InputStream binaryDataStream;
		String asciiData;
		try {
			System.out.println("DB-POOL New connection from getDatRecordToProcess ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
				ps = mysqlConn.prepareStatement("select a.* from error_dat a where a.signature = ? order by a.dfrId");

					ps.setInt(1, getSignature());
					rs = ps.executeQuery();
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
						mapDfrsStatus.put("DFR"+processDatDTO.getDfrId(),true);
					}
					activeDfrs = getActiveDfrsCount(mapDfrsStatus);
					System.out.println("Recalculating Active dfrs "+activeDfrs);

					rs.close();
					if (lstProcessDatDTO.size() != activeDfrs) // if any dfrs takes too long to respond 
					{
						updateMissingDFRStatus(lstProcessDatDTO);
//				    		M9kLED.setLed(LedName.TRIGGER, LedState.YELLOW);
					}
				ps.close();
		} catch (SQLException e) {
			System.out.println("Error reading data from process_dat table "+e);
		} catch (Exception e) {
			System.out.println("Error reading data from process_dat table "+e);
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
//					System.out.println("Closing the connection... "+mysqlConn);
					System.out.println("DB-POOL Closing connection from getDatRecordToProcess ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				System.out.println("Error in cleanup"+e);
			}
		}

    	return lstProcessDatDTO;
    }
    
    private void updateMissingDFRStatus(List<ProcessDatDTO>  lstProcessDatDTO)
    {
    	boolean booFound = false;
    	System.out.println("MERGE: Some DFR is not responding or responding too late");
    	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator
				.hasNext();) {
    		DfrDTO dfrDTO =  iterator.next();
			System.out.println("MERGE: about to check for Dfr "+dfrDTO.getDfrId());

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
				System.out.println("The dfr "+"DFR"+dfrDTO.getDfrId()+" hasn't responded for long time. ");
    		}
    		else
    		{
    			System.out.println("MERGE: dfr id "+dfrDTO.getDfrId()+" is not missing");
    		}
    		booFound = false;
		}
    }
    
    private boolean removeProcessedDatRecord()
    {
    	boolean booConnection = true;
		PreparedStatement ps = null;
		
		try {
//			getLocalConnection();
			System.out.println("About to delete from process-dat table for signature="+getSignature());
			ps = mysqlConn.prepareStatement("delete from process_dat where datId IN (select id from dat a where (a.id = datId and a.signature = ?) or a.updated < DATE_SUB(now(), INTERVAL 10 minute))");
			ps.setInt(1, getSignature());
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Station Master is not connected to master or Master Station MySql server is down");
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
					System.out.println("Closing the connection... "+mysqlConn);
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				System.out.println("Error in cleanup"+e);
			}
		}

    	return booConnection;
    }
    
    private void moveToErrorDat (String errorIds)
    {
		PreparedStatement ps = null;
		PreparedStatement psDelete = null;
		System.out.println("Error ids "+errorIds+ " are being moved to error table error_dat");
		try {
			System.out.println("DB-POOL New  connection from moveToErrorDat ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			ps = mysqlConn.prepareStatement("insert into error_dat select * from dat where id in ("+errorIds+")");
			ps.executeUpdate();
			
			psDelete = mysqlConn.prepareStatement("delete from dat where id in ("+errorIds+")");
			psDelete.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Error Ids "+errorIds+" weren't moved to error_dat. "+e);
		} catch (Exception e) {
			System.out.println("Error Ids "+errorIds+" weren't moved to error_dat. "+e);
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
					System.out.println("DB-POOL Closing connection from moveToErrorDat ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				System.out.println("Error in cleanup"+e);
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
			System.out.println("Already merged analog data length? "+mergedProcessDatDto.getMergedBinaryAnalogData().size());
			binMergedAnalogInputStream = new DataInputStream(new ByteArrayInputStream(mergedProcessDatDto.getMergedBinaryAnalogData().toByteArray()));
		}
		if (mergedProcessDatDto.getMergedBinaryEventData() != null)
		{
			System.out.println("Already merged events data length? "+mergedProcessDatDto.getMergedBinaryEventData().size());
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
				analogsCnt = processDatDTO.getAnalogs().toString().split("\n").length;
				endIndex =  analogsCnt+1;
				if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
				{
					eventStartIndex = endIndex+1;
				}
			System.out.println("Total analogsCnt "+analogsCnt);
			System.out.println("Start Index "+startIndex);
			
			System.out.println("End Index in analog box "+endIndex);
			isAnalog = true;
		}
		else
		{
			eventStartIndex = startIndex;
			System.out.println("End Index in events "+endIndex);
		}

		if (processDatDTO.getEvents() != null && processDatDTO.getEvents().length() > 0)
		{
			isEvents = true;	
			eventsCnt = processDatDTO.getEvents().toString().split("\n").length;
		}
		System.out.println("SHOW: Event start index "+eventStartIndex+" Do we have the events this time? "+isEvents+" Total events count.... "+eventsCnt);

		
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
//				System.out.println("BINARY: Samples read "+i);
				if (isAnalog)
				{
					if(binMergedAnalogInputStream == null || binMergedAnalogInputStream.available() <= 0)
					{
//						System.out.println("No analog data available to merge. AnalogByes to read "+initialAnalogBytes);
						analogBytes = new byte[initialAnalogBytes];
						binaryDataStream.read(analogBytes);
						binaryAnalogOutStream.write(analogBytes);
					}
					else
					{
//						System.out.println("Analog data available to merge. New AnalogByes to read "+analogBytesToRead);
						analogBytes = new byte[analogBytesToRead];

//						System.out.println("Already available merge data "+mergedProcessDatDto.getMergedAnalogsCount());
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
//						System.out.println("No already merged events. Events bytes read per samples " + eventBytesToRead);
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
//						System.out.println("Already merged events available. Merged Events bytes read per samples " + mergedEventBytesToRead);
						eventsSpilledOver = (int)(mergedProcessDatDto.getMergedEventsCount() % 16);
//						System.out.println("Odd events to be handled "+eventsSpilledOver);
						mergedEventBytes = new byte[mergedEventBytesToRead];
						binMergedEventsInputStream.read(mergedEventBytes);
						binaryEventsOutStream.write(mergedEventBytes);
						if (!isAnalog)
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
//							System.out.println("BINARY: BEFORE stuffing zeroes: eventBits with spilled over events to be merged with about to read events "+eventBits);
							if (eventBits.length() > eventsSpilledOver)
							{
								eventBits = eventBits.substring(eventBits.length() - eventsSpilledOver);
							}
							else // The extra events will be 0 during the prefault and postfault time and hence it'll be just 0
							{
								leadZero = zeros.substring(0,eventsSpilledOver-eventBits.length());
								eventBits=leadZero.concat(eventBits);
							}
//							System.out.println("BINARY: AFTER stuffing zeroes eventBits with spilled over events to be merged with about to read events "+eventBits);
							//								leadZero = zeros.substring(0,16-eventBits.length());
							//								eventBits=leadZero.concat(eventBits);
							eventByteIndex = 0;
//							System.out.println("BINARY: nofTimesToRead "+nofTimesToRead+" eventByteIndx "+eventByteIndex);
							while (eventByteIndex++ < nofTimesToRead) // Number of bytes to read from the event stream
							{
//								System.out.println("BINARY: eventByteIndx "+eventByteIndex);
								eventData = binaryDataStream.readShort(); // A short consist of 16 events
								eventData = Short.reverseBytes(eventData);
								newEvents = Integer.toBinaryString(eventData);
//								newEvents = new StringBuffer(newEvents).reverse().toString();
//								System.out.println("BINARY: newEvents read before "+newEvents);
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

//								System.out.println("BINARY: newEvents read after "+newEvents);
								j = 0;
//								System.out.println("BINARY: eventsIndexCnt "+eventsIndexCnt +" eventsCnt "+eventsCnt);
								while(j < newEvents.length()) // Total Number of events in this particular DFR
								{
//									System.out.println("BINARY: eventsBits "+eventBits +" j "+j+" NewEvents "+newEvents);
									eventBits = newEvents.substring(j++, j) + eventBits; // Append to the already available events, if any.
									if (eventBits.length() == 16)
									{
										eventHexValue = (short)convertEventToHex(eventBits);
										binaryEventsOutStream.writeShort(Short.reverseBytes(eventHexValue));
										eventBits = "";
									}
								}
//								System.out.println("BINARY: Evnets left to be written "+eventBits);
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
//							System.out.println("Already merged events available but no spill overs. Events bytes to be read per samples " + eventBytesToRead);
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
				System.out.println("ERROR: Incorrect Data format. Quitting the merge process.");
				throw new M9000Exception("Incorrect Data format.");
			}
			if (isAnalog)
			{
				if(mergedProcessDatDto.getMergedAnalogsCount() <= 0)
				{
					System.out.println("BINARY: initialAnalogBytes "+initialAnalogBytes);
					mergedProcessDatDto.setMergedAnalogsCount(initialAnalogBytes);
				}
				else
				{
					System.out.println("BINARY: already available analog cnt "+mergedProcessDatDto.getMergedAnalogsCount()+" Plus the new analog cnt "+analogBytesToRead);
					mergedProcessDatDto.setMergedAnalogsCount(mergedProcessDatDto.getMergedAnalogsCount() + analogBytesToRead);
				}
				mergedProcessDatDto.setMergedBinaryAnalogData(binaryAnalogData);
				System.out.println("BINARY: Merged binary analog cnt "+mergedProcessDatDto.getMergedAnalogsCount());
				System.out.println("BINARY: Merged binary analog size "+binaryAnalogData.size());
			}
			if (isEvents)
			{
				if(mergedProcessDatDto.getMergedEventsCount() <= 0)
				{
					System.out.println("BINARY: initial Event count "+eventsCnt);
					mergedProcessDatDto.setMergedEventsCount(eventsCnt);
				}
				else
				{
					System.out.println("BINARY: already available events cnt "+mergedProcessDatDto.getMergedEventsCount()+" Plus the new events cnt "+eventsCnt);
					mergedProcessDatDto.setMergedEventsCount(mergedProcessDatDto.getMergedEventsCount()+eventsCnt);
				}
				mergedProcessDatDto.setMergedBinaryEventData(binaryEventData);
				System.out.println("BINARY: Merged binary events cnt "+mergedProcessDatDto.getMergedEventsCount());
				System.out.println("BINARY: Merged binary events size "+binaryEventData.size());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occured during analog data update"+e);
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
		
		System.out.println("Returning from updateBinaryData");
	}

	private void updateDummyBinaryData (ProcessDatDTO mergedProcessDatDto) throws Exception
	{
		DataInputStream binMergedAnalogInputStream = null;
		DataInputStream binMergedEventsInputStream = null;
		
		ByteArrayOutputStream binaryAnalogData = new ByteArrayOutputStream();
		ByteArrayOutputStream binaryEventData = new ByteArrayOutputStream();
		DataOutputStream binaryAnalogOutStream = new DataOutputStream(binaryAnalogData);
		DataOutputStream binaryEventsOutStream = new DataOutputStream(binaryEventData);
//		System.out.println("Already mrged analog data avail? "+mergedProcessDatDto.getMergedBinaryAnalogData().size());
//		System.out.println("Already mrged events data avail? "+mergedProcessDatDto.getMergedBinaryEventData().size());
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
		System.out.println("Current dfr "+currentDfr.getDfrId());
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
			System.out.println("Total analogsCnt "+analogsCnt);
			System.out.println("Start Index "+startIndex);
			
			System.out.println("End Index in analog box "+endIndex);
			isAnalog = true;
		}
		else
		{
			eventStartIndex = startIndex;
			System.out.println("End Index in events "+endIndex);
		}

		if (totalEvents > 0)
		{
			isEvents = true;	
			eventsCnt = totalEvents;
		}
		System.out.println("SHOW: Event start index "+eventStartIndex+" Do we have the events this time? "+isEvents+" Events count "+eventsCnt);

		
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
							binaryEventsOutStream.writeShort(dummyEvents);
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
//							System.out.println("DUMMY-BINARY: eventBits spilled over "+eventBits);
							if (eventBits.length() > eventsSpilledOver)
							{
								eventBits = eventBits.substring(eventBits.length() - eventsSpilledOver);
							}
							else // The extra events will be 0 during the prefault and postfault time and hence it'll be just 0
							{
								leadZero = zeros.substring(0,eventsSpilledOver-eventBits.length());
								eventBits=leadZero.concat(eventBits);
							}
//							System.out.println("DUMMY-BINARY: After padding eventBits spilled over "+eventBits);
							int nofTimesToWrite = (int)(Math.ceil((float)eventsCnt/16));
							while(nofTimesToWrite-- > 0) // Total Number of events in this particular DFR
							{
								if (eventBits.length() > 0)
								{
//									eventBits = "0" + eventBits; // Append to the already available events, if any.
									leadZero = zeros.substring(0,eventBits.length());
									eventBits=leadZero.concat(eventBits);
//									System.out.println("DUMMY-BINARY:Before sending to  "+eventBits);
									eventHexValue = (short) convertEventToHex(eventBits);
//									System.out.println("DUMMY-BINARY:Hex value returned  "+eventHexValue);
									binaryEventsOutStream.writeShort(Short.reverseBytes(eventHexValue));
									eventBits = "";
									binaryEventsOutStream.writeShort(dummyEvents);
								}
								else
								{
									binaryEventsOutStream.writeShort(dummyEvents);
								}
							}
						}
						else // No adjustments needed as the events count was divisible by 16
						{
							for (int k = 0; k < eventShortsToWrite; k++) {
								binaryEventsOutStream.writeShort(dummyEvents);
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
					System.out.println("DUMMY-BINARY: initialAnalogBytes "+initialAnalogBytes);
					mergedProcessDatDto.setMergedAnalogsCount(initialAnalogBytes);
				}
				else
				{
					System.out.println("DUMMY-BINARY: already available analog cnt "+mergedProcessDatDto.getMergedAnalogsCount()+" Plus the new analog cnt "+analogBytesToRead);
					mergedProcessDatDto.setMergedAnalogsCount(mergedProcessDatDto.getMergedAnalogsCount() + analogBytesToRead);
				}
				mergedProcessDatDto.setMergedBinaryAnalogData(binaryAnalogData);
			}
			if (isEvents)
			{
				updateDummyEventDetails(mergedProcessDatDto);

				if(mergedProcessDatDto.getMergedEventsCount() <= 0)
				{
					System.out.println("DUMMY-BINARY: initial Event count "+eventsCnt);
					mergedProcessDatDto.setMergedEventsCount(eventsCnt);
				}
				else
				{
					System.out.println("DUMMY-BINARY: already available events cnt "+mergedProcessDatDto.getMergedEventsCount()+" Plus the new events cnt "+eventsCnt);
					mergedProcessDatDto.setMergedEventsCount(mergedProcessDatDto.getMergedEventsCount()+eventsCnt);
				}
				mergedProcessDatDto.setMergedBinaryEventData(binaryEventData);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error occured during analog data update"+e);
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
		System.out.println("Returning from DummyupdateData");
	}
	
	private String getFaultLocationDetails(ProcessDatDTO mergedProcessDatDto) {
		System.out.println("Entered getFaultLocationDetails");
		// Because merged analog count is the total analog bytes to read, we'll over write here to give total analog counts reverse engineering ((analogsCnt *2)+4+4)
		mergedProcessDatDto.setMergedAnalogsCount((mergedProcessDatDto.getMergedAnalogsCount()-4-4)/2);
		System.out.println("Merged analog count "+mergedProcessDatDto.getMergedAnalogsCount());
		System.out.println("Merged events count "+mergedProcessDatDto.getMergedEventsCount());
		
		StringBuffer faultLocationDetails = new StringBuffer();
		Map<LineGroupsAlgorithm, M9kFaultReport> mapFaultReport = null;
		M9kFaultReport faultReport;
		M9kFaultLocation m9kFaultLocation = new M9kFaultLocation(mergedProcessDatDto);
		if (m9kFaultLocation.isContinueProcessing())
		{
			try {
				mapFaultReport = m9kFaultLocation.findFaultLocation(false);
				System.out.println("Returned from findFaultLocation "+mapFaultReport);
				if (mapFaultReport != null)
				{
					for (LineGroupsAlgorithm lineIterator : mapFaultReport.keySet()) {
						faultReport = mapFaultReport.get(lineIterator);
						if (faultReport != null)
						{
							if (faultLocationDetails.length() > 0)
							{
								faultLocationDetails.append("\n");
							}
							faultLocationDetails.append("Line Name: "+lineIterator.getName()+"\n");
							faultLocationDetails.append(faultReport.getFaultReportDetails());
						}
					}
				}
			} catch (M9000Exception e) {
				System.out.println("There is an issue with fault location calculation. "+e);
			} 
			catch (Exception e) {
				System.out.println("There is an issue with fault location calculation. "+e);
			}
		}

		return faultLocationDetails.toString();
	}

	
	private int convertEventToHex(String events)
	{
		int hexVal;
//		System.out.println("BINARY-HEX: Events string to be converted "+events);
		Integer val = Integer.parseInt(events,2);
//		System.out.println("BINARY-HEX: Long value of Events string to be converted "+val);
		String zeros = "0000";
		String hexString = Integer.toHexString(val);
//		System.out.println("BINARY-HEX: before padding Hex value of converted "+hexString);
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
//		System.out.println("BINARY-HEX: After padding Hex value converted "+hexString);
		hexVal = Integer.parseInt(hexString,16);
//		System.out.println("BINARY-HEX: Integer Hex value aafter conversion to be returned "+hexVal);
		return hexVal;
	}

	public int getSignature() {
		return signature;
	}

	public void setSignature(int signature) {
		this.signature = signature;
	}
	
	public String getLocalConfigXml() throws M9000Exception {
		String configXml = "";
		Connection localConnection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
//			localConnection = M9kMySqlDatabase.getInstance().getLocalDBConnection();
			localConnection = M9kStationDBUtil.getLocalConnection();
			System.out.println("Entered getLocalConfigXml in DAO");
			ps = localConnection.prepareStatement("select configXml from station_details where stationId ="+getStationId());
			// Test code
//			ps = localConnection.prepareStatement("select configXml from station_details where stationId = 44");
//			ps = localConnection.prepareStatement("select configXml from station_details where stationId = 666");
//			ps = localConnection.prepareStatement("select configXml from station_details where stationId = 99");
			rs = ps.executeQuery();
			while (rs.next())
			{
				configXml = rs.getString("configXml");
			}
		}
		catch (SQLException e) {
			System.out.println("Failed to get local station config xml. "+e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			System.out.println("Failed to local station config xml "+e);
			throw new M9000Exception(e);
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
					if (localConnection != null)
					{
						localConnection.close();
						localConnection = null;
					}
				} catch (SQLException e) {
					System.out.println("Exception during cleanup"+e);
				}

	}
		return configXml;
	}

	public int getStationId() {
		return stationId;
	}

	public void setStationId(int stationId) {
		this.stationId = stationId;
	}

	public List<DfrDTO> getDFRsFromStationXML() throws Exception
	{
		  int nextStartAnalogIndex = -1;
		  int nextStartDigitalIndex = -1;
		  int analogCnt;
		  int digitalCnt;
		  Analogs analogChannels;
		  Events eventChannels;
//		System.out.println("Substation..."+substation);
		List<DfrDTO> lstDfrs = new ArrayList<DfrDTO>(subStation.getDFRs().sizeOfDFRArray());
		DfrDTO dfrDto;
		DFR[] xmlDfrs = subStation.getDFRs().getDFRArray();
		for (int i = 0; i < xmlDfrs.length; i++) {

			dfrDto = new DfrDTO("DFR"+xmlDfrs[i].getSystem().getDfrId());
			System.out.println("Name: "+dfrDto.getDfrName());
			dfrDto.setDfrId(xmlDfrs[i].getSystem().getDfrId());
			dfrDto.setIpAddress(xmlDfrs[i].getIPAddress());
			dfrDto.setLineFreq(xmlDfrs[i].getSystem().getLineFrequency());
			dfrDto.setSampleRate(xmlDfrs[i].getSystem().getSampleRate());
			System.out.println("IPAddress: "+dfrDto.getIpAddress());
	    	analogChannels = xmlDfrs[i].getDataPool().getChannels().getAnalogs();
			if (analogChannels != null)
			{
				analogCnt = analogChannels.sizeOfAnalogInputArray();
				dfrDto.setAnalogChnlCnt(analogCnt);
				if (nextStartAnalogIndex == -1)
				{
					dfrDto.setAnalogChannelStart(1);
					dfrDto.setAnalogChannelEnd(analogCnt);
					nextStartAnalogIndex = analogCnt+1;
				}
				else 
				{
					dfrDto.setAnalogChannelStart(nextStartAnalogIndex);
					dfrDto.setAnalogChannelEnd(nextStartAnalogIndex + analogCnt - 1);
					nextStartAnalogIndex += analogCnt;
				}
		    	dfrDto.setLstAnalogChannelNames(new ArrayList<String>(analogChannels.sizeOfAnalogInputArray()));
		    	dfrDto.setLstAnalogChannels(new ArrayList<AnalogChannelDTO>(analogChannels.sizeOfAnalogInputArray()));
		    	int start = dfrDto.getAnalogChannelStart();
		    	AnalogChannelDTO analogDto;
		    	for (int j = 0; j < analogChannels.sizeOfAnalogInputArray(); j++) {
		    		dfrDto.getLstAnalogChannelNames().add("A"+(start++)+"-"+analogChannels.getAnalogInputArray(j).getCircuitName());

					analogDto = new AnalogChannelDTO();
					analogDto.setName(analogChannels.getAnalogInputArray(j).getName());
					analogDto.setPhase(analogChannels.getAnalogInputArray(j).getPhase());
					analogDto.setRange(""+analogChannels.getAnalogInputArray(j).getRange());
					analogDto.setPrimaryRatio(""+analogChannels.getAnalogInputArray(j).getTransformerPrimary());
					analogDto.setSecondaryRatio(""+analogChannels.getAnalogInputArray(j).getTransformerSecondary());
					analogDto.setInputType(analogChannels.getAnalogInputArray(j).getInputType());
					analogDto.setChannel("" + (analogChannels.getAnalogInputArray(j).getChannel()+dfrDto.getAnalogChannelStart()-1));
					System.out.println("Export status in xml "+analogChannels.getAnalogInputArray(j).getExport());
					if (analogChannels.getAnalogInputArray(j).getExport() == M9kConstants.ENABLE_ONE)
					{
						analogDto.setExportStatus(true);
					}
					else
					{
						analogDto.setExportStatus(false);
					}
					System.out.println("analog dto added "+analogDto);
					dfrDto.getLstAnalogChannels().add(analogDto);
						    		
				}
	
			}
			else
			{
				dfrDto.setAnalogChnlCnt(0);
			}
	    	eventChannels = xmlDfrs[i].getDataPool().getChannels().getEvents();

			if (eventChannels != null)
			{
				digitalCnt = eventChannels.sizeOfEventInputArray();
				dfrDto.setDigitalChnlCnt(digitalCnt);
				if (nextStartDigitalIndex == -1)
				{
					dfrDto.setDigitalChannelStart(1);
					dfrDto.setDigitalChannelEnd(digitalCnt);
					nextStartDigitalIndex = digitalCnt+1;
				}
				else 
				{
					dfrDto.setDigitalChannelStart(nextStartDigitalIndex);
					dfrDto.setDigitalChannelEnd(nextStartDigitalIndex + digitalCnt - 1);
					nextStartDigitalIndex += digitalCnt;
				}
		    	dfrDto.setLstEventChannelNames(new ArrayList<String>(eventChannels.sizeOfEventInputArray()));
		    	for (int j = 0; j < eventChannels.sizeOfEventInputArray(); j++) {
		    		dfrDto.getLstEventChannelNames().add(eventChannels.getEventInputArray(j).getEventName());
				}

			}
			else
			{
				dfrDto.setDigitalChnlCnt(0);
			}
			System.out.println("Dfrs added to the list..."+dfrDto);
//			if (dfrDto.getAnalogChnlCnt() > 0)
//			{
				lstDfrs.add(dfrDto);
//			}
		}
		return lstDfrs;
	}


	public static void main(String[] args)
	{
		M9kFaultDataMergeTask m9kFaultDataMergeTask = new M9kFaultDataMergeTask(333, 231);		
		m9kFaultDataMergeTask.processFaultRecordsFromDat();
	}
}

