package com.usi.m9000.station.threads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.station.Exception.M9kThreadExceptionHandler;
import com.usi.m9000.station.commands.M9kCreateContinuousComtradeFilesFromDB;
import com.usi.m9000.station.util.M9kBackupUtil;
import com.usi.m9000.station.util.M9kKeyValuePair;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kConstants;


public class M9kExportContMeasurementsComtrades{
	private int runFrequency = 1; // in Hour
	static boolean booStatus = true;
	private Connection mysqlConn;
	private long contStartTime = 0;
	private long contStopTime = 0;
	private int exportBufferTime;
	List<DfrDTO> lstDfrs = null;
	String sqlQuery;
	private StationDTO stationDetails;
	int finalMergedSampleCnt = 0;
	List<Integer> lstMissingExportsId = new ArrayList<Integer>();
	Map<String, Integer> mapAnalogsRange = new HashMap<String, Integer>();

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kExportContMeasurementsComtrades.class);
	static int expCnt;
	M9kCreateContinuousComtradeFilesFromDB comtradeData;
	Map<Integer, Map<String, Integer>> mapDfrRmsExports ;
	String destFileName;
	
	Map<Integer,ComtradeDataDTO> mapComtradeContDat = null;
	Map<Integer,Object> mapMaxValues = null;
	Map<Integer,ComtradeDataDTO> mapComtradeContMagnitudeDat = null;
	Map<Integer,ComtradeDataDTO> mapComtradeContPhaseDat = null;
	
	private long oldestDataStartHour = 0;
	private long lastExportedTime = 0;
	private long singleSampleTime;
	private int sizeInMinutes = 0; // COMTRADE Size to be created
	
	private int contComtradeNoOfDaysToRetain = 1; // COMTRADE Size to be created
	private String destExportsDirectory;
	 private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	 ScheduledFuture<?> scheduledTask = null;
	 private int errorLogCount = 0;
	private ExecutorService threadHandlerExecutor;

	public M9kExportContMeasurementsComtrades(long startTime, int sizeInMinutes, String destExportsDirectory, int contComtradeNoOfDaysToRetain)
	{
		this.sizeInMinutes = sizeInMinutes;
		this.destExportsDirectory = destExportsDirectory;
		this.contComtradeNoOfDaysToRetain = contComtradeNoOfDaysToRetain;
		threadHandlerExecutor = Executors.newSingleThreadExecutor();
		errorLogCount = 0;
		logger.debug("AutoExport: Entered M9kExportContMeasurementsComtrades constructor with start time "+startTime+" COMTRADE size in Minutes "+sizeInMinutes);
		try {
			M9kStationXMLUtil.initXml(getStationDetails().getConfigXml());
			logger.debug("M9kStationXMLUtil.getMeasurementSampleRate() "+M9kStationXMLUtil.getMeasurementSampleRate());
			singleSampleTime = (1/M9kStationXMLUtil.getMeasurementSampleRate())*1000000;
			logger.debug("Auto Export: Single Sample time "+singleSampleTime);

			contStartTime = startTime;
			contStopTime = startTime + ((sizeInMinutes*60) * 1000000) - singleSampleTime;
			// START: 08-Dec-2023 - Instead of start of the day start from first available time of the current day
//			startOfDay = M9kBackupUtil.getStartTimeOFTheDay();
			oldestDataStartHour = M9kBackupUtil.getFirstAvailableHourForContData(M9kConstants.CONTINUOUS_DATA);
			// END: 08-Dec-2023 - Instead of start of the day start from first available time of the current day
			lastExportedTime = calculateContStartTime();
			logger.debug("Measurements: contStartTime "+contStartTime+" contStopTime "+contStopTime+"lastExportedTime "+lastExportedTime+" (contStartTime - singleSampleTime) "+(contStartTime - singleSampleTime));
			if (lastExportedTime != -1 && (lastExportedTime < (contStartTime - singleSampleTime) || lastExportedTime < contStartTime))
			{
				logger.debug("LastExportedTime is less so starting time is replaced with it. LastExportedTime "+lastExportedTime+" current Start Time "+contStartTime);
				if (lastExportedTime == oldestDataStartHour)
				{
					contStartTime = lastExportedTime ;
				}
				else
				{
					contStartTime = lastExportedTime + singleSampleTime;
				}
				contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000)- singleSampleTime;
			}
			logger.debug("Entered M9kContDataFromDb constructor with start time "+contStartTime+" stop time "+contStopTime);
			logger.debug("Station Name "+getStationDetails().getSystemStationName());


			exportBufferTime = M9kStationXMLUtil.getExportBufferTime();
			
				lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
				Collections.sort(lstDfrs, new Comparator<DfrDTO>() {

					@Override
					public int compare(DfrDTO o1, DfrDTO o2) {
						return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
					}
				});
				comtradeData = new M9kCreateContinuousComtradeFilesFromDB(getStationDetails().getSystemStationId());
				mapDfrRmsExports = M9kStationXMLUtil.getMapOfDfrRmsExports();
		} catch (Exception e) {
			logger.error("Unable to create continuous data ",e);
		}

	}

	// Testing purpose. Not intended for any other functionality
	private M9kExportContMeasurementsComtrades(long startTime, int sizeInMinutes, String destExportsDirectory)
	{
		this.sizeInMinutes = sizeInMinutes;
		this.destExportsDirectory = destExportsDirectory;
		threadHandlerExecutor = Executors.newSingleThreadExecutor();
		errorLogCount = 0;
		logger.debug("AutoExport: Entered M9kExportContMeasurementsComtrades constructor with start time "+startTime+" COMTRADE size in Minutes "+sizeInMinutes);
		try {
			M9kStationXMLUtil.initXml(getStationDetails().getConfigXml());
			singleSampleTime = (long) ((1/getStationDetails().getSystemSampleRate())*1000000);
			logger.debug("Auto Export: Single Sample time "+singleSampleTime);

			contStartTime = startTime;
			contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000) - singleSampleTime;
			logger.debug("Entered M9kContDataFromDb constructor with start time "+contStartTime+" stop time "+contStopTime);
			logger.debug("Station Name "+getStationDetails().getSystemStationName());


			exportBufferTime = M9kStationXMLUtil.getExportBufferTime();
			
				lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
				Collections.sort(lstDfrs, new Comparator<DfrDTO>() {

					@Override
					public int compare(DfrDTO o1, DfrDTO o2) {
						return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
					}
				});
				comtradeData = new M9kCreateContinuousComtradeFilesFromDB(getStationDetails().getSystemStationId());
				mapDfrRmsExports = M9kStationXMLUtil.getMapOfDfrRmsExports();
		} catch (Exception e) {
			logger.error("Unable to create continuous data ",e);
		}

	}

	public void scheduleCreateContComtrades()
	{
		// Inital run
		try
	  	  {
	  		  exportContMeasurementsComtrades();
	  	  }
	  	  catch (Exception e) {
				logger.error("Encounted error while creating the comtrade files around the time "+contStartTime,e);
	  	  }

		  final Runnable taskPerformer = new Runnable() {
              public void run() {
// Commented as it is handled in contAnalog exports
//            	  try
//            	  {
//            		  logger.debug("About to delete old folder");
//            		  removeOldContComtrades(); -- To be expected to happen in Cont Analog Export
//            	  }
//            	  catch (Exception e) {
//					logger.error("Encounted error while removing old comtrade files ",e);
//            	  }
            	  try
            	  {
            		  exportContMeasurementsComtrades();
            	  }
            	  catch (Exception e) {
					logger.error("Encounted error while creating the comtrade files around the time "+contStartTime,e);
            	  }

              }
          };
          // Delay the next run so that it starts exactly at start of the hour
       // START: 23-Jan-2023 - Schedule the task to run at start of the hour. It fixes the issue of time drifting caused due to scheduleWithFixedDelay.
//          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, M9kBackupUtil.calculateSecondsUntilNextHour(), getRunFrequency()*3600, TimeUnit.SECONDS);
          scheduledTask = scheduler.scheduleAtFixedRate(taskPerformer, M9kBackupUtil.calculateSecondsUntilNextHour(), getRunFrequency()*3600, TimeUnit.SECONDS);
          // END: 23-Jan-2023
          threadHandlerExecutor.execute(new M9kThreadExceptionHandler(scheduledTask));
	}

	private void exportContMeasurementsComtrades()
	{
		long start = System.currentTimeMillis();
		long end;
	
		try {
//			long actualContStopTime = contStartTime + 3599000000L; // end time of hr:59:59 so that next hour starts from 00
			long actualContStopTime = M9kBackupUtil.getCurrentWholeHourTime(); // end time of hr:59:59 so that next hour starts from 00
			logger.debug("AutoExport: Current whole hour time/actualContStopTime " + actualContStopTime);
			logger.debug("AutoExport: Start Time "+contStartTime+" stop time "+contStopTime+" Last exported time "+lastExportedTime);
			// START: 12-Dec-2023 - Check if stop Time is already exported until last hour
//			if (contStartTime < lastExportedTime)
			if (actualContStopTime <= lastExportedTime)
			// END: 12-Dec-2023
			{
				logger.debug("Already exported until last hour. Skipping this run.");
				contStartTime = lastExportedTime + singleSampleTime;
				contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000) - singleSampleTime;
			}
			else
			{
				do {
					destFileName = createFileName();
					processMergeAndCreateComtrade();
					contStartTime = contStopTime + singleSampleTime;
					contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000) - singleSampleTime;
					logger.debug("AutoExport: contStartTime "+contStartTime+"AutoExport: contStopTime "+contStopTime+" actualContStopTime "+actualContStopTime);
				} while ( contStopTime <= actualContStopTime);
				if (contStartTime < actualContStopTime) //// If size in minutes is not evenly distributed within hour like 7 minutes file then we need to get the last few odd minutes of the hour 
				{
					logger.debug("Measurement: Overflow due to unevenly distributed minutes contStopTime "+contStopTime+" actualContStopTime "+actualContStopTime);
					contStopTime = actualContStopTime;
					destFileName = createFileName();
					processMergeAndCreateComtrade(); 
					contStartTime = contStopTime + singleSampleTime;
					contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000) - singleSampleTime;
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured in process Cont Measurements data with start time "+contStartTime+" and stop time "+contStopTime, e);
			contStartTime = contStopTime + singleSampleTime;
			contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000) - singleSampleTime;

		}
		end = System.currentTimeMillis();
		logger.debug("Total time for creating ContMeasurements Data " + (end-start));

	}
	

	
	private void processMergeAndCreateComtrade() throws M9000Exception
	{
		mapComtradeContDat = new HashMap<Integer, ComtradeDataDTO>();
		mapComtradeContMagnitudeDat = new HashMap<Integer, ComtradeDataDTO>();
		mapComtradeContPhaseDat = new HashMap<Integer, ComtradeDataDTO>();
		mapMaxValues = null;

		double startSampleTime = 0;
		long lastSampleTime = 0;
		int sampleCnt = 0;
		double sampleRate;
		double tus;
		double tusDivBy2;

		PreparedStatement ps = null;
		ResultSet rs= null;
		int i = 0;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		finalMergedSampleCnt = 0;

		String sqlQuery = "SELECT recId, expId, name, description, phase, units, tsLast, sampleRate, sampleCnt, type, measurementType, "
			+ "data FROM `m9000`.`cont` where (tsLast >= ? "
			+ "and tsLast <= ? and measurementType in(";
		StringBuffer strbQuery = new StringBuffer(sqlQuery);
		String[] arrMeasurementTypes = getMeasurementTypes();
		for (int j = 0; j < arrMeasurementTypes.length; j++) {
			if (j == 0)
			{
				strbQuery.append("?");
			}
			else
			{
				strbQuery.append(",?");
			}
		}
		strbQuery.append(")) order by measurementType desc, expId, tsLast");

		try {
			logger.debug("DB-POOL New connection from processMergeAndInsertLongTermData ");

			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Cont-Data-Log: Start time "+contStartTime+" Stop time "+contStopTime );
			logger.debug("Cont-Data-Log: SQL query to be executed "+ strbQuery);
			// START: 10-Oct-2023 - Fix for the error "java.sql.SQLException: Operation not allowed for a result set of type ResultSet.TYPE_FORWARD_ONLY."
//			ps = mysqlConn.prepareStatement(strbQuery.toString());
			ps = mysqlConn.prepareStatement(strbQuery.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			// END: 10-Oct-2023 - Fix for the error "java.sql.SQLException: Operation not allowed for a result set of type ResultSet.TYPE_FORWARD_ONLY."
			ps.setLong(++i, (contStartTime));
			logger.debug("Cont-Data-Log: param "+i+" - "+(contStartTime));
			ps.setLong(++i, (contStopTime+(getExportBufferTime() * 1000000)));
			logger.debug("Cont-Data-Log: param "+i+" - "+(contStopTime+(getExportBufferTime() * 1000000)));
			for (int j = 0; j < arrMeasurementTypes.length; j++) {
				ps.setString(++i, arrMeasurementTypes[j]);				
				logger.debug("Cont-Data-Log: param "+i+" - "+arrMeasurementTypes[j]);
			}
			rs = ps.executeQuery();
			int expId;
			String analogChnlName;
			int dfrId;
			Integer rmsExpId;
			String type = null;
			String measurementType = null;
			mapMaxValues = getMaxValue(rs);

			while (rs.next())
			{
				dfrId = rs.getInt("recId");
				expId = rs.getInt("expId");
				type = rs.getString("type");
				measurementType = rs.getString("measurementType");
				sampleCnt = rs.getInt("sampleCnt");
				sampleRate = rs.getDouble("sampleRate");
				lastSampleTime = rs.getLong("tsLast");

				tus = 1e6/sampleRate;
				tusDivBy2 = tus / 2;

				startSampleTime = (lastSampleTime - (sampleCnt-1) * tus);
				if(contStopTime < (startSampleTime - tusDivBy2) || contStartTime > (lastSampleTime + tusDivBy2))
				{
					logger.debug("TODD: ExportContMeasurements Skipping the blob...");
					continue;
				}
				analogChnlName = getAnalogNameStrippingIndexPrefix(rs.getString("name"));

				if (measurementType.equalsIgnoreCase(M9kStationConstants.RMS))
				{
					if (mapDfrRmsExports.get(dfrId) == null)
					{
						// No dfrId found in the config
						logger.warn("Dfr Id not configured "+dfrId);
						continue;
					}
					rmsExpId = mapDfrRmsExports.get(dfrId).get(analogChnlName); 
					if (rmsExpId == null || rmsExpId != expId)
					{
						// Not a rms export
						continue;
					}
				}
				logger.debug("\n\n\t\tType from database "+type + " for exp Id " + expId+"\n\n");
				if (type!= null && type.equalsIgnoreCase(M9kStationConstants.COMPLEX))
				{
					processComplexDataType(rs);
				}
				else
				{
					processFloatDataType(rs);
				}
			}
			if(!mapComtradeContDat.isEmpty())
			{
				lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContDat.values());
				
				logger.debug("Before sort "+lstComtradeDataDtos);
				Collections.sort(lstComtradeDataDtos, new Comparator<ComtradeDataDTO>() {

					@Override
					public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
//						return ((o1.getDfrId() < o2.getDfrId())?0:1);
						int returnVal;
						if ((o1.getDfrId() < o2.getDfrId()))
						{
							returnVal= -1;
						}
						else if (o1.getDfrId() == o2.getDfrId())
						{
							if (o1.getExportId() <= o2.getExportId())
							{
								returnVal =  -1;
							}
							else
							{
								returnVal = 1;
							}
						}
						else
						{
							returnVal = 1 ;
						}
						return returnVal;
					}
				});
				logger.debug("After sort "+lstComtradeDataDtos);
			}
			if(!mapComtradeContMagnitudeDat.isEmpty())
			{
				List<ComtradeDataDTO> lstComtradeMagnitudeDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContMagnitudeDat.values());
				
				Collections.sort(lstComtradeMagnitudeDataDtos, new Comparator<ComtradeDataDTO>() {

					@Override
					public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
						int returnVal;
						if ((o1.getDfrId() < o2.getDfrId()))
						{
							returnVal= -1;
						}
						else if (o1.getDfrId() == o2.getDfrId())
						{
//							if (o1.getExportChnlId() <= o2.getExportChnlId())
							if (o1.getExportId() <= o2.getExportId())
							{
								returnVal =  -1;
							}
							else
							{
								returnVal = 1;
							}
						}
						else
						{
							returnVal = 1 ;
						}
						return returnVal;
					}
				});
				lstComtradeDataDtos.addAll(lstComtradeMagnitudeDataDtos);
				// Phase object sorting
				List<ComtradeDataDTO> lstComtradePhaseDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContPhaseDat.values());
				
				Collections.sort(lstComtradePhaseDataDtos, new Comparator<ComtradeDataDTO>() {

					@Override
					public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
						int returnVal;
						if ((o1.getDfrId() < o2.getDfrId()))
						{
							returnVal= -1;
						}
						else if (o1.getDfrId() == o2.getDfrId())
						{
//							if (o1.getExportChnlId() <= o2.getExportChnlId())
							if (o1.getExportId() <= o2.getExportId())
							{
								returnVal =  -1;
							}
							else
							{
								returnVal = 1;
							}
						}
						else
						{
							returnVal = 1 ;
						}
						return returnVal;
					}
				});
				
				lstComtradeDataDtos.addAll(lstComtradePhaseDataDtos);
			}
			if (lstComtradeDataDtos != null && !lstComtradeDataDtos.isEmpty())
			{
				if (errorLogCount > 0)
				{
					errorLogCount = 0;
					logger.info("Data match found after errors at previous attempts. Error logging for no match found will be renewed.");
				}
				comtradeData.createContinuousDataFiles(destExportsDirectory, destFileName, lstComtradeDataDtos);
			}
			else
			{
				if (errorLogCount < 5)
				{
					logger.error("ERROR: No Matching Data available from start time "+contStartTime+" to end time "+contStopTime);
					if (errorLogCount == 4)
					{
						logger.error("Error logging for no matching data will be suspended temporaroly to avoid too many errors and it will resume when it recovers.");
					}
					errorLogCount++;
				}
			}
			logger.debug("Size of Measurements cont..."+lstComtradeDataDtos.size());
			getLstMissingExportsId().clear();
			getMapAnalogRanges().clear();
			lstComtradeDataDtos.clear();
			mapComtradeContDat.clear();
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
				logger.debug("DB-POOL Closing connection from processMergeAndInsertLongTermData ");
				mysqlConn.close();
				mysqlConn = null;
			}
		} 
		catch (Exception e) {
			logger.error("Error in fetching Continuous Measurements data ", e);
//			throw new M9000Exception(e);
		}
		finally
		{
			try {
				System.gc();
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
					logger.debug("DB-POOL Closing connection from processMergeAndInsertLongTermData ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}
		
		
	}
	
	private void processFloatDataType(ResultSet rs) throws Exception{
		ComtradeDataDTO comtradeDataDTO = null;
		InputStream isData;
		InputStream is;
		StringBuffer channelInfoBuffer;
		double sampleRate;
		int sampleCnt = 0;
		int startSampleNo = -1;
		int endSampleNo = -1;
		int expId;
		double startSampleTime = 0;
		long lastSampleTime = 0;
		Double missingSampleCnt;
		float scaleFactor = 0; // scale factor
		String analogChnlName;
		int dfrId;
//		int exportChnlId;
//		String dataType;
		double tus;
		dfrId = rs.getInt("recId");
		expId = rs.getInt("expId");

		analogChnlName = getAnalogNameStrippingIndexPrefix(rs.getString("name"));
		
		sampleCnt = rs.getInt("sampleCnt");
		sampleRate = rs.getDouble("sampleRate");
		is = rs.getBinaryStream("data");
		lastSampleTime = rs.getLong("tsLast");
//		dataType = rs.getString("measurementType");
		tus = 1e6/sampleRate;
//		if (lastSampleTime > contStartTime)
//		{
//			startSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - contStartTime)*sampleRate)/1000000) + 1;
//			if (startSampleNo < 0)
//			{
//				startSampleNo = 1;
//			}
//		}
//		else
//		{
//			startSampleNo = 1;
//		}
//		if (lastSampleTime > contStopTime)
//		{
//			endSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - contStopTime)*sampleRate)/1000000);
//			if (endSampleNo < 0)
//			{
//				endSampleNo = sampleCnt;
//			}
//		}
//		else
//		{
//			endSampleNo = sampleCnt;
//		}
//		startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
		startSampleTime = (lastSampleTime - (sampleCnt-1) * tus);
		startSampleNo = M9kStationUtil.toIndex(contStartTime - startSampleTime, tus, sampleCnt);
		logger.debug("TODD: Start sample no "+startSampleNo);
		endSampleNo = M9kStationUtil.toIndex(contStopTime - startSampleTime, tus, sampleCnt);
		logger.debug("TODD: End sample no "+endSampleNo);
		
		logger.debug("expId to be processed "+expId);
		if (mapComtradeContDat.get(expId) == null)
		{
				sampleCnt=endSampleNo-startSampleNo+1;
			comtradeDataDTO = new ComtradeDataDTO();
			comtradeDataDTO.setTsLast( lastSampleTime);
			if (contStartTime < startSampleTime) // Missing data at the start
			{
				// Construct dummy data for initial missing data
				logger.debug("Missing data at the start");
				
				missingSampleCnt = Math.floor(((startSampleTime - contStartTime )/1000000.0)*sampleRate);
				logger.info("Constructing dummy data for missing samples count of "+missingSampleCnt+" data Start time "+contStartTime+" actual start sample time "+startSampleTime+" for expId "+expId);
				isData = constructDummyContinuousData(missingSampleCnt.intValue());
				comtradeDataDTO.setBinaryDataSteam(isData);
				isData.close();
			}
//			if (dataType.endsWith(M9kStationConstants.WATTS) || dataType.endsWith(M9kStationConstants.VARS))
//			{
//				exportChnlId = expId;
//			}
//			else
//			{
//				if (analogChnlName.toUpperCase().indexOf(M9kStationConstants.VIRTUAL_ANALOG.toUpperCase()) != -1)
//				{
//					exportChnlId =  Integer.parseInt(analogChnlName.substring(M9kStationConstants.VIRTUAL_ANALOG.length()));
//				}
//				else
//				{
//					exportChnlId =  Integer.parseInt(analogChnlName.substring(M9kStationConstants.ANALOG.length()));
//				}
//			}
//			logger.debug("exportChnlId derived  "+exportChnlId);
			if (mapMaxValues == null)
			{
				if (getMapAnalogRanges() == null || getMapAnalogRanges().isEmpty())
				{
					mapAnalogsRange = getAnalogChnlsRanges();
				}
				scaleFactor = (32767.0f / getMapAnalogRanges().get(analogChnlName));
//				logger.debug("ExpId "+exportChnlId+" scale factor "+scaleFactor);
			}
			else
			{
				scaleFactor = (32767.0f / ((Float)mapMaxValues.get(expId)).floatValue());
//				logger.debug("ExpId "+exportChnlId+" scale factor "+scaleFactor);
			}
			comtradeDataDTO.setScaleFactor(scaleFactor);
			expCnt = expId;
			isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo, scaleFactor);
			comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
			comtradeDataDTO.setStationId(getStationDetails().getSystemStationId());
			logger.debug("\t\t\t\t\t\t Station Name "+getStationDetails().getSystemStationName());
			comtradeDataDTO.setStationName(getStationDetails().getSystemStationName());
			comtradeDataDTO.setDfrId(dfrId);
//			comtradeDataDTO.setExportChnlId(exportChnlId);
			// Commented as the sequence index, first column of cfg is added wgile creating cfg file
//			channelInfoBuffer = new StringBuffer(totalChannelCnt++ +M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer = new StringBuffer();
			// START: 18-Mar-2018 Prefix A or VIRTUAL_ANALOG prefix in cfg files
//			if (expId <= M9kStationXMLUtil.getSystem().getAnalogsCount())
//			{
				channelInfoBuffer.append("M"+expId+M9kStationConstants.COMMA_SEPERATOR);
//			}
//			else
//			{
//				channelInfoBuffer.append(M9kStationConstants.VIRTUAL_PREFIX+expId+M9kStationConstants.COMMA_SEPERATOR);
//			}

//			channelInfoBuffer.append(expId+M9kStationConstants.COMMA_SEPERATOR);
			// END: 18-Mar-2018 Prefix A or VIRTUAL_ANALOG prefix in cfg files
			channelInfoBuffer.append(rs.getString("phase")+M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer.append(rs.getString("description")+M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer.append(rs.getString("units")+M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer.append((1/scaleFactor)+M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer.append(M9kStationConstants.COMTRADE_CHNL_OFFSET+M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_CHNL_SKEW+M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MIN+M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MAX+M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer.append(M9kConstants.COMTRADE_SECONDARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY);
			channelInfoBuffer.append(M9kStationConstants.NEWLINE);
			comtradeDataDTO.setAnalogs(channelInfoBuffer);
			comtradeDataDTO.setSampleRate(sampleRate);
			comtradeDataDTO.setSampleCnt(sampleCnt);
			comtradeDataDTO.setType(M9kStationConstants.RMS);
			comtradeDataDTO.setBinaryDataSteam(isData);
			isData.close();
			mapComtradeContDat.put(expId, comtradeDataDTO);
			comtradeDataDTO.setTsPrefault(contStartTime);
			comtradeDataDTO.setTsTrigger(contStartTime);
		}
		else
		{
			comtradeDataDTO = mapComtradeContDat.get(expId);
			if (startSampleTime > (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)) ) // First sample should be Last sample + 1 of the previous record
			{
				missingSampleCnt = Math.floor(((startSampleTime - comtradeDataDTO.getTsLast() )/1000000.0)*sampleRate);
				logger.info("Constructing dummy data for missing samples count of "+missingSampleCnt+" last data end time ((comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000))) "+(comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000))+" actual start sample time "+startSampleTime+" for expId "+expId);
				isData = constructDummyContinuousData(missingSampleCnt.intValue());
				comtradeDataDTO.setBinaryDataSteam(isData);
				isData.close();
			}
			comtradeDataDTO.setTsLast(lastSampleTime);
				isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo,comtradeDataDTO.getScaleFactor());
				 
				sampleCnt=(endSampleNo-startSampleNo+1)+comtradeDataDTO.getSampleCnt();
			
			comtradeDataDTO.setSampleCnt(sampleCnt);
			comtradeDataDTO.setBinaryDataSteam(isData);
			isData.close();
		}
		if (sampleCnt > finalMergedSampleCnt)
		{
			finalMergedSampleCnt = sampleCnt;
			logger.debug("Cont Data-DEBUG: finalMergedSampleCnt "+finalMergedSampleCnt);
		}
		is.close();
		
	}
	
	private void processComplexDataType(ResultSet rs) throws Exception{

		InputStream is;
		StringBuffer magnitudeChannelInfoBuffer;
		StringBuffer phaseChannelInfoBuffer;
		InputStream[] isData;
		double sampleRate;
		int sampleCnt = 0;
		int startSampleNo = -1;
		int endSampleNo = -1;
		int expId;
		double startSampleTime = 0;
		long lastSampleTime = 0;
		Double missingSampleCnt;
		float magScaleFactor = 0; // scale factor for magnitude
		float phaseScaleFactor = 0; // scale factor for phase
		String analogChnlName;
		int dfrId;
//		int exportChnlId;
		double tus;

		ComtradeDataDTO comtradeMagnitudeDataDTO = null;
		ComtradeDataDTO comtradePhaseDataDTO = null;

		
		dfrId = rs.getInt("recId");
		expId = rs.getInt("expId");
		
		analogChnlName = getAnalogNameStrippingIndexPrefix(rs.getString("name"));
		sampleCnt = rs.getInt("sampleCnt");
		sampleRate = rs.getDouble("sampleRate");
		is = rs.getBinaryStream("data");
		lastSampleTime = rs.getLong("tsLast");
		tus = 1e6/sampleRate;
//		if (lastSampleTime > contStartTime)
//		{
//			startSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - contStartTime)*sampleRate)/1000000);
//			if (startSampleNo < 0)
//			{
//				startSampleNo = 1;
//			}
//		}
//		else
//		{
//			startSampleNo = 1;
//		}
//		logger.debug("CONT-PHASOR: Start sample No "+startSampleNo);
//		
//		if (lastSampleTime > contStopTime)
//		{
//			endSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - contStopTime)*sampleRate)/1000000);
//			if (endSampleNo < 0)
//			{
//				endSampleNo = sampleCnt;
//			}
//		}
//		else
//		{
//			endSampleNo = sampleCnt;
//		}
//		logger.debug("CONT-PHASOR: End sample No "+endSampleNo);
//		
//		startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
//		logger.debug("CONT-PHASOR: calculated startSampleTime"+startSampleTime);
		startSampleTime = (lastSampleTime - (sampleCnt-1) * tus);
		startSampleNo = M9kStationUtil.toIndex(contStartTime - startSampleTime, tus, sampleCnt);
		logger.debug("TODD: Start sample no "+startSampleNo);
		endSampleNo = M9kStationUtil.toIndex(contStopTime - startSampleTime, tus, sampleCnt);
		logger.debug("TODD: End sample no "+endSampleNo);

		if (mapComtradeContMagnitudeDat.get(expId) == null)
		{
			logger.debug("CONT-PHASOR: Start of the sample....for expId "+expId+" ->" + startSampleNo);
			sampleCnt=endSampleNo-startSampleNo+1;
			logger.debug("CONT-PHASOR: Last row to be processed. End Sample..for expId "+expId+" ->" + endSampleNo+" Actual sampleCnt.."+sampleCnt);
			comtradeMagnitudeDataDTO = new ComtradeDataDTO();
			comtradePhaseDataDTO = new ComtradeDataDTO();
			comtradeMagnitudeDataDTO.setTsLast( lastSampleTime);
			comtradePhaseDataDTO.setTsLast( lastSampleTime);
			if (contStartTime < startSampleTime) // Missing data at the start
			{
				// Construct dummy data for initial missing data
				logger.debug("Missing data at the start");
				missingSampleCnt = Math.floor(((startSampleTime - contStartTime )/1000000.0)*sampleRate);
				logger.info("Construncting dummy complex data for missing samples count of "+missingSampleCnt+" data Start time "+contStartTime+" actual start sample time "+startSampleTime);
				isData = constructDummyContinuousComplexData(missingSampleCnt.intValue());
				comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
				comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
				isData[0].close();
				isData[1].close();
			}
			logger.debug("cont-db-export: Channel name "+analogChnlName);
			logger.debug("cont-db-export: index of "+M9kStationConstants.VIRTUAL_ANALOG+" = "+analogChnlName.indexOf(M9kStationConstants.VIRTUAL_ANALOG));
			logger.debug("cont-db-export: index of "+M9kStationConstants.ANALOG+" = "+analogChnlName.indexOf(M9kStationConstants.ANALOG));
//			if (analogChnlName.toUpperCase().indexOf(M9kStationConstants.VIRTUAL_ANALOG.toUpperCase()) != -1)
//			{
//				exportChnlId =  Integer.parseInt(analogChnlName.substring(M9kStationConstants.VIRTUAL_ANALOG.length()));
//			}
//			else if (analogChnlName.toUpperCase().indexOf(M9kStationConstants.ANALOG.toUpperCase()) != -1)
//			{
//				exportChnlId =  Integer.parseInt(analogChnlName.substring(M9kStationConstants.ANALOG.length()));
//			}
//			else
//			{
//				exportChnlId =  Integer.parseInt(analogChnlName.substring(M9kStationConstants.LG_PREFIX.length()));
//			}
			if (mapMaxValues == null)
			{
				if (mapAnalogsRange == null && mapAnalogsRange.isEmpty())
				{
					mapAnalogsRange = getAnalogChnlsRanges();
				}
				magScaleFactor = (32767.0f / getMapAnalogRanges().get(analogChnlName));
				phaseScaleFactor = (32767.0f / 360);
			}
			else
			{
				magScaleFactor = (float) (32767.0f / Double.parseDouble(((M9kKeyValuePair)mapMaxValues.get(expId)).getKey()));
				phaseScaleFactor = (float) (32767.0f / Double.parseDouble(((M9kKeyValuePair)mapMaxValues.get(expId)).getValue()));
			}
			logger.debug("ExpId "+expId+" mag scale factor "+magScaleFactor+" phase scale factor "+phaseScaleFactor);
			comtradeMagnitudeDataDTO.setScaleFactor(magScaleFactor);
			comtradePhaseDataDTO.setScaleFactor(phaseScaleFactor);
			expCnt = expId;
			comtradeMagnitudeDataDTO.setExportId(expId); // Just a value to use in file name creation
			comtradePhaseDataDTO.setExportId(expId); // Just a value to use in file name creation
			comtradeMagnitudeDataDTO.setStationId(getStationDetails().getSystemStationId());
			comtradePhaseDataDTO.setStationId(getStationDetails().getSystemStationId());
			logger.debug("\t\t\t\t\t\t Station Name "+getStationDetails().getSystemStationName());
			comtradeMagnitudeDataDTO.setStationName(getStationDetails().getSystemStationName());
			comtradePhaseDataDTO.setStationName(getStationDetails().getSystemStationName());
			comtradeMagnitudeDataDTO.setDfrId(dfrId);
			comtradePhaseDataDTO.setDfrId(dfrId);
//			comtradeMagnitudeDataDTO.setExportChnlId(exportChnlId);
//			comtradePhaseDataDTO.setExportChnlId(exportChnlId);
			comtradeMagnitudeDataDTO.setRecordingDevId(getStationDetails().getSystemRecordingDeviceId());
			comtradePhaseDataDTO.setRecordingDevId(getStationDetails().getSystemRecordingDeviceId());

			// Commented as the sequence index, first column of cfg is added wgile creating cfg file
			magnitudeChannelInfoBuffer = new StringBuffer();
			magnitudeChannelInfoBuffer.append("A"+expId+M9kStationConstants.COMMA_SEPERATOR);
			magnitudeChannelInfoBuffer.append(rs.getString("phase")+M9kStationConstants.COMMA_SEPERATOR);
			magnitudeChannelInfoBuffer.append(rs.getString("description")+M9kStationConstants.COMMA_SEPERATOR);
			magnitudeChannelInfoBuffer.append(rs.getString("units")+M9kStationConstants.COMMA_SEPERATOR);
			magnitudeChannelInfoBuffer.append((1/magScaleFactor)+M9kStationConstants.COMMA_SEPERATOR);
			magnitudeChannelInfoBuffer.append(M9kStationConstants.COMTRADE_CHNL_OFFSET+M9kStationConstants.COMMA_SEPERATOR);
			magnitudeChannelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_CHNL_SKEW+M9kStationConstants.COMMA_SEPERATOR);
			magnitudeChannelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MIN+M9kStationConstants.COMMA_SEPERATOR);
			magnitudeChannelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MAX+M9kStationConstants.COMMA_SEPERATOR);
			magnitudeChannelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
			magnitudeChannelInfoBuffer.append(M9kConstants.COMTRADE_SECONDARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
			magnitudeChannelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY);
			magnitudeChannelInfoBuffer.append(M9kStationConstants.NEWLINE);
			
			// Phase conf string
			phaseChannelInfoBuffer = new StringBuffer();
			phaseChannelInfoBuffer.append("A"+expId+M9kStationConstants.COMMA_SEPERATOR);
			phaseChannelInfoBuffer.append(rs.getString("phase")+M9kStationConstants.COMMA_SEPERATOR);
			phaseChannelInfoBuffer.append("Phase for "+rs.getString("description")+M9kStationConstants.COMMA_SEPERATOR);
			phaseChannelInfoBuffer.append("Deg"+M9kStationConstants.COMMA_SEPERATOR);
			phaseChannelInfoBuffer.append((1/phaseScaleFactor)+M9kStationConstants.COMMA_SEPERATOR);
			phaseChannelInfoBuffer.append(M9kStationConstants.COMTRADE_CHNL_OFFSET+M9kStationConstants.COMMA_SEPERATOR);
			phaseChannelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_CHNL_SKEW+M9kStationConstants.COMMA_SEPERATOR);
			phaseChannelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MIN+M9kStationConstants.COMMA_SEPERATOR);
			phaseChannelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MAX+M9kStationConstants.COMMA_SEPERATOR);
			phaseChannelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
			phaseChannelInfoBuffer.append(M9kConstants.COMTRADE_SECONDARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
			phaseChannelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY);
			phaseChannelInfoBuffer.append(M9kStationConstants.NEWLINE);
			
			comtradeMagnitudeDataDTO.setAnalogs(magnitudeChannelInfoBuffer);
			comtradePhaseDataDTO.setAnalogs(phaseChannelInfoBuffer);
			comtradeMagnitudeDataDTO.setSampleRate(sampleRate);
			comtradePhaseDataDTO.setSampleRate(sampleRate);
			comtradeMagnitudeDataDTO.setSampleCnt(sampleCnt);
			comtradePhaseDataDTO.setSampleCnt(sampleCnt);
			comtradeMagnitudeDataDTO.setType(M9kStationConstants.MAGNITUDE);
			comtradePhaseDataDTO.setType(M9kStationConstants.PHASE);
			comtradeMagnitudeDataDTO.setTsPrefault(contStartTime);
			comtradePhaseDataDTO.setTsPrefault(contStartTime);
			comtradeMagnitudeDataDTO.setTsTrigger(contStartTime);
			comtradePhaseDataDTO.setTsTrigger(contStartTime);
			
			// Phase data
			
			isData = constructContinuousComplexData(is, sampleRate,startSampleNo, endSampleNo, magScaleFactor, phaseScaleFactor);
			comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
			comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
			isData[0].close();
			isData[1].close();
			mapComtradeContMagnitudeDat.put(expId, comtradeMagnitudeDataDTO);
			mapComtradeContPhaseDat.put(expId, comtradePhaseDataDTO);
		}
		else
		{
			comtradeMagnitudeDataDTO = mapComtradeContMagnitudeDat.get(expId);
			comtradePhaseDataDTO = mapComtradeContPhaseDat.get(expId);
			if (startSampleTime > (comtradeMagnitudeDataDTO.getTsLast() +((1/sampleRate)*1000000)) ) // First sample should be Last sample + 1 of the previous record
			{
				missingSampleCnt = Math.floor(((startSampleTime - comtradeMagnitudeDataDTO.getTsLast() )/1000000.0)*sampleRate);
				logger.info("Construncting dummy complex data for missing samples count of "+missingSampleCnt+" data Start time "+contStartTime+" actual start sample time "+startSampleTime);
				isData = constructDummyContinuousComplexData(missingSampleCnt.intValue());
				comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
				comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
				isData[0].close();
				isData[1].close();
			}
			comtradeMagnitudeDataDTO.setTsLast(lastSampleTime);
			comtradePhaseDataDTO.setTsLast(lastSampleTime);
				isData = constructContinuousComplexData(is, sampleRate,startSampleNo, endSampleNo,comtradeMagnitudeDataDTO.getScaleFactor(), comtradePhaseDataDTO.getScaleFactor());
				 
				sampleCnt=(endSampleNo-startSampleNo+1)+comtradeMagnitudeDataDTO.getSampleCnt();
			
			comtradeMagnitudeDataDTO.setSampleCnt(sampleCnt);
			comtradePhaseDataDTO.setSampleCnt(sampleCnt);
			comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
			comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
			isData[0].close();
			isData[1].close();
		}
		if (sampleCnt > finalMergedSampleCnt)
		{
			finalMergedSampleCnt = sampleCnt;
			logger.debug("CONT-PHASOR: finalMergedSampleCnt "+finalMergedSampleCnt);
		}
		is.close();
	
	}
	private Map<String, Integer> getAnalogChnlsRanges()
    {
    	Map<String, Integer> mapAnalogExports = new HashMap<String, Integer>();
    	Integer range;
    	Integer txRatio;
    	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
    		DfrDTO dfrDto = iterator.next();
			if (dfrDto.getAnalogChnlCnt() > 0)
			{
		    	for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
					AnalogChannelDTO analogChnl  =  iterator1.next();
					logger.debug("Cont-Data-Debug: Channel" +analogChnl.getChannel()+" - Range "+analogChnl.getRange());
					range = Integer.parseInt(analogChnl.getRange());
					if (M9kStationXMLUtil.isSecondary())
					{
						txRatio = Integer.parseInt(analogChnl.getPrimaryRatio()) / Integer.parseInt(analogChnl.getSecondaryRatio());
						range = range * txRatio;
						logger.debug("It is secondary. TxRatio "+txRatio+" Range "+range);
					}
					mapAnalogExports.put(getAnalogNameStrippingIndexPrefix(analogChnl.getName()), range);
				}
			}
    	}
		logger.debug("Cont-Data-Debug: Total analogs exported "+mapAnalogExports.size()+" actual values "+mapAnalogExports);
    	return mapAnalogExports;
    }

	

	public int getExportBufferTime() {
		return exportBufferTime;
	}

	public void setExportBufferTime(int exportBufferTime) {
		this.exportBufferTime = exportBufferTime;
	}


	public long getLtrStartTime() {
		return contStartTime;
	}


	public void setLtrStartTime(long ltrStartTime) {
		this.contStartTime = ltrStartTime;
	}


	public long getLtrStopTime() {
		return contStopTime;
	}


	public void setLtrStopTime(long ltrStopTime) {
		this.contStopTime = ltrStopTime;
	}

	private InputStream constructContinuousData(InputStream is, double sampleRate, int fromSampleNo, int endSampleNo, float scaleFactor) throws M9000Exception
	{
		DataInputStream dis = new DataInputStream(is);
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		
		int totalSamplesToRead = endSampleNo - fromSampleNo+1;
		float sampleData;
		short shortDataVal;
		
		try {
		if (fromSampleNo > 1)
		{
			dis.skipBytes((fromSampleNo - 1)*M9kStationConstants.SIZE_OF_FLOAT);
		}
		while (dis.available() > 0)
		{
			sampleData = dis.readFloat();
			shortDataVal = (short) (sampleData * scaleFactor);
			dos.writeShort(shortDataVal);
			totalSamplesToRead--;
			if (totalSamplesToRead == 0)
			{
				break;
			}

		}
		dis.close();
		} catch (IOException e) {
			logger.error("Failed to construct rms data",e);
		}
		catch (Exception e) {
			logger.error("Failed to construct rms data",e);
		}
		finally {
			if (dis != null)
			{
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return new ByteArrayInputStream(bais.toByteArray());

	}

	private InputStream constructDummyContinuousData(int totalSamplesMissing)
	{
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		Integer iDummyHexData = 0x8000;
		
		try {
			
		for (int i = 0; i < totalSamplesMissing; i++)
				
		{
			dos.writeShort(iDummyHexData);
		}
		} catch (IOException e) {
			logger.error("Error occured while creating dummy data for measurements.",e);
		}
		finally {
			if (dos != null)
			{
				try {
					dos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		return new ByteArrayInputStream(bais.toByteArray());

	}

	// Utility function to strip prefix and get the name e.g. 1-Analog1 will return Analog1
	private String getAnalogNameStrippingIndexPrefix(String analogName)
	{
		return analogName.substring(analogName.lastIndexOf("-")+1);
	}
	public List<Integer> getLstMissingExportsId() {
		return lstMissingExportsId;
	}

	public void setLstMissingExportsId(List<Integer> lstMissingExportsId) {
		this.lstMissingExportsId = lstMissingExportsId;
	}

	public Map<String, Integer> getMapAnalogRanges() {
		return mapAnalogsRange;
	}

	public void setMapAnalogRanges(Map<String, Integer> mapAnalogsRange) {
		this.mapAnalogsRange = mapAnalogsRange;
	}

	private String getRequiredDataFormat(String requiredFormat, Date dateToFormat)
	{
		String convertedDateFormat = null;
		SimpleDateFormat requiredDateFormat = new SimpleDateFormat(requiredFormat);
		requiredDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		convertedDateFormat = requiredDateFormat.format(dateToFormat.getTime());
		return convertedDateFormat;
	}
	
    // Calculated max value for scale factor calculation
    private Map<Integer, Object> getMaxValue(ResultSet rs) 
    {
    	InputStream is;
    	DataInputStream dis = null;
    	float dataVal;
		int expId = 0;
		String type = null; 
    	float real;
    	float imaginary;
		double magVal;
		double phaseVal;
		double maxMagVal = 1.0f;
		double maxPhaseVal = 1.0f;
		mapMaxValues = new HashMap<Integer, Object>();
    	try
    	{
	    	while (rs.next())
			{
				is = rs.getBinaryStream("data");
				dis = new DataInputStream(is);
				expId = rs.getInt("expId");
				type = rs.getString("type");
//				logger.debug("ExpId "+expId+" type "+type);
				if (type != null && !type.equalsIgnoreCase(M9kStationConstants.COMPLEX))
				{
					if (mapMaxValues.get(expId) == null)
					{
						mapMaxValues.put(expId, 1.0f);
					}
					while (dis.available() > 0)
					{
						dataVal = Math.abs(dis.readFloat());
						
						if (((Float)mapMaxValues.get(expId)).floatValue() < dataVal)
						{
							mapMaxValues.put(expId, Float.valueOf(dataVal));
	
						}
	
	
					}
//					logger.debug("expId "+expId+" Max value  "+((Float)mapMaxValues.get(expId)).floatValue()+" regular Float obejct "+mapMaxValues.get(expId));
					dis.close();
				}
				else
				{
					if (mapMaxValues.get(expId) == null)
					{
						mapMaxValues.put(expId, new M9kKeyValuePair("1", "1"));
					}
					while (dis.available() > 0)
					{
						real = dis.readFloat();
						imaginary = dis.readFloat();
						maxMagVal = Double.parseDouble(((M9kKeyValuePair)mapMaxValues.get(expId)).getKey());
						magVal = Math.hypot(real, imaginary) ;
						if (maxMagVal < magVal)
						{
							((M9kKeyValuePair)mapMaxValues.get(expId)).setKey(""+magVal);
						}
						maxPhaseVal = Double.parseDouble(((M9kKeyValuePair)mapMaxValues.get(expId)).getValue());
						phaseVal =  Math.toDegrees(Math.atan2(imaginary, real));
						if (maxPhaseVal < phaseVal)
						{
							((M9kKeyValuePair)mapMaxValues.get(expId)).setValue(""+phaseVal);
						}
						
					}
//					logger.debug("Max value map "+mapMaxValues);
					dis.close();

				}

			}
	    	rs.beforeFirst();
    	}
    	catch(Exception e)
    	{
    		logger.error("Error in finding max value",e);
    		mapMaxValues = null;
    	}
    	finally
    	{
    		if (dis != null)
    		{
    			try {
					dis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    		try {
				System.gc();
    		} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
    	}
    	logger.debug("Max value to return  "+mapMaxValues);
    	return mapMaxValues;
    }

	private InputStream[] constructContinuousComplexData(InputStream is, double sampleRate, int fromSampleNo, int endSampleNo, double magScaleFactor, float phaseScaleFactor) throws M9000Exception
	{
		InputStream[] byteInputStreamArr;
//		PrintWriter pwShort = null;
//		PrintWriter pwTstScopeData = null;
		DataInputStream dis = new DataInputStream(is);
		ByteArrayOutputStream baisMagnitude = new ByteArrayOutputStream();
		DataOutputStream dosMagnitude = new DataOutputStream(baisMagnitude);

		ByteArrayOutputStream baisPhase = new ByteArrayOutputStream();
		DataOutputStream dosPhase = new DataOutputStream(baisPhase);
		byteInputStreamArr = new InputStream[2];
		int totalSamplesToRead = endSampleNo - fromSampleNo+1;
//		StringBuffer data = new StringBuffer();
//		float sampleData;
		float real;
		float imaginary;
		Double magShortDataVal;
		Double phaseShortDataVal;
		
//		int skipCnt = 0;
//TODO: calculate micro seconds for data prefix	
		try {
		if (fromSampleNo > 1)
		{
			dis.skipBytes((fromSampleNo - 1)*(M9kStationConstants.SIZE_OF_FLOAT*2));
		}
//		logger.debug("Cont-Data-Debug-DEBUG: ExpId "+expCnt);
//		if (expCnt == 8 || expCnt == 24 || expCnt == 41 || expCnt == 73)
//		{
//			pwShort = new PrintWriter(new FileWriter(new File("/home/dfr/m9k/LTR_RMS_"+expCnt+"_short.txt"), true));
//			pwTstScopeData = new PrintWriter(new FileWriter(new File("/home/dfr/m9k/LTR_RMS_"+expCnt+"_Float.txt"), true));
//
//		}
		while (dis.available() > 0)
		{
//			if (fromSampleNo > 0)
//			{
//				while (skipCnt < fromSampleNo)
//				{
//					sampleData = dis.readShort();
//					skipCnt++;
//				}
//			}
////			if(endSampleNo != -1 && skipCnt > endSampleNo)
//			if(skipCnt > endSampleNo)
//			{
//				break;
//			} 
//			dos.writeInt(Integer.reverseBytes(iCnt));
//			dos.writeInt(Integer.reverseBytes((int)((iCnt / sampleRate)*1000)));
//			data.append((iCnt)+","+(int)((iCnt / sampleRate)*1000)+",");
//			iCnt++;

//			sampleData = dis.readFloat();
			real = dis.readFloat();
			imaginary = dis.readFloat();
			
//			logger.debug("Sample data read as float "+sampleData);
//			System.out.println(calculatedValue);
//			shortDataVal = (short) (sampleData * scaleFactor);
//			shortDataVal = getMagnitude(real, imaginary).shortValue();
			magShortDataVal = Math.hypot(real, imaginary) * magScaleFactor;
			dosMagnitude.writeShort(magShortDataVal.shortValue());

//			shortDataVal = getAngle(real, imaginary).shortValue();
			phaseShortDataVal =  Math.toDegrees(Math.atan2(imaginary, real)) * phaseScaleFactor;
			dosPhase.writeShort(phaseShortDataVal.shortValue());

			totalSamplesToRead--;
//			if (currentExpIdProcessed == 257 || currentExpIdProcessed == 258 )
//			{
//				logger.debug("CONT-PHASE-DATA: expId "+currentExpIdProcessed+" \tphasor= ("+real+","+imaginary+") Mag: "+(magShortDataVal/magScaleFactor)+" Ang: "+(phaseShortDataVal/phaseScaleFactor));
//			}
			if (totalSamplesToRead == 0)
			{
				break;
			}

		}
		dis.close();
//		if (pwShort != null)
//		{
//			pwShort.close();
//		}
//		if (pwTstScopeData != null)
//		{
//			pwTstScopeData.close();
//		}
		} catch (IOException e) {
			logger.error("Failed to construct rms data",e);
		}
		catch (Exception e) {
			logger.error("Failed to construct rms data",e);
		}
		finally {
			if (dis != null)
			{
				try {
					dis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
//		System.out.println("Data returned..."+data);
//		logger.debug("CONT-PHASOR: byte array size "+bais.size());
		byteInputStreamArr[0] = new ByteArrayInputStream(baisMagnitude.toByteArray());
		byteInputStreamArr[1] = new ByteArrayInputStream(baisPhase.toByteArray());
		return byteInputStreamArr;

	}


	private InputStream[] constructDummyContinuousComplexData(int totalSamplesMissing)
	{
		InputStream[] dummyStreamsArray = new InputStream[2];
//		StringBuffer data = new StringBuffer();
		ByteArrayOutputStream baisMagnitude = new ByteArrayOutputStream();
		DataOutputStream dosMagnitude = new DataOutputStream(baisMagnitude);
		
		ByteArrayOutputStream baisPhase = new ByteArrayOutputStream();
		DataOutputStream dosPhase = new DataOutputStream(baisMagnitude);
		
		Integer iDummyHexData = 0x8000;
		
		try {
			
//		logger.debug("CONT-PHASOR-DUMMY: Sample cnt of those missing "+totalSamplesMissing);
		for (int i = 0; i < totalSamplesMissing; i++)
				
		{
			dosMagnitude.writeShort(iDummyHexData);
			dosPhase.writeShort(iDummyHexData);
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (dosMagnitude != null)
			{
				try {
					dosMagnitude.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (dosPhase != null)
			{
				try {
					dosPhase.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}


		}
		dummyStreamsArray[0] = new ByteArrayInputStream(baisMagnitude.toByteArray()); 
		dummyStreamsArray[1] = new ByteArrayInputStream(baisPhase.toByteArray());
//		logger.debug("CONT-PHASOR-DUMMY: byte array size "+bais.size());
		return dummyStreamsArray;

	}
	
	private String createFileName()
	{
		String ctFileName;
		String timeCode;
		Date startDate;
//	    String date = new java.text.SimpleDateFormat("yyMMdd,HHmmssSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
//		String date =M9kBackupUtil.getDateFormat("yyMMdd,HHmmssSSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
		startDate = new Date(contStartTime/1000);
		String date = getRequiredDataFormat(M9kStationConstants.COMTRADE_FILE_DATE_FORMAT,startDate);

//	    String filePrefix = "R";
		String filePrefix = "R"+String.format("%02d", getStationDetails().getSystemStationId());
//	    if (getStationDetails().getSystemStationId() < 10)
//	    {
//	    	filePrefix += "0"+getStationDetails().getSystemStationId();
//	    }
//	    else
//	    {
//	    	filePrefix += getStationDetails().getSystemStationId();
//	    }
	    filePrefix+="CONT"; // To match the old model 2002 statndard
		try
		{
			// Start: Modified the timezone calculation logic 02-Dec-2013
//			TimeZone tz = Calendar.getInstance().getTimeZone();
//			timeCode = (tz.getRawOffset()/1000/3600)+"t";
			Calendar cal = Calendar.getInstance();
			timeCode = (cal.get(Calendar.ZONE_OFFSET)+cal.get(Calendar.DST_OFFSET))/1000/3600 +"t";
			// End
		}
		catch (Exception e) {
			logger.error("Cannot get TimeZone and rawoffset for timecode. Attempting to read from M9K_COMTRADE",e);
			timeCode = M9kStationComtradeUtil.getComtradeProperty("timeCode");

		}

//	    ctFileName = filePrefix+comtradeDataDTO.getFaultId()+"_"+date + ","+bundle.getString("timeCode")+","+stationName+","+recDevId+","+bundle.getString("company");
	    // START: 20-May-2016 Changed to IEEE C37.232-2011 COMNAME naming convention 
//	    ctFileName = filePrefix+comtradeDataDTO.getFaultId()+"_"+date + ","+timeCode+","+stationName+","+recDevId+","+bundle.getString("company");
		
	    ctFileName = date + ","+timeCode+","+getStationDetails().getSystemStationName()+","+getStationDetails().getSystemRecordingDeviceId()+","+M9kStationComtradeUtil.getComtradeProperty("company")+","+filePrefix+","+M9kStationConstants.MEASUREMENTS;
	    // END: 20-May-2016
	    ctFileName = ctFileName.replace("/", "_");
	    ctFileName = ctFileName.replace("\\", "_");
	    return ctFileName;
	}
	
	@SuppressWarnings("unused")
	private long calculateContStartTimeOld()
	{
		long calculatedStartTime = -1; // return the actual start time i.e. an hour before by default
		try {
				File[] exportDateDirs = M9kBackupUtil.getAutoExportDateFolders(destExportsDirectory);
				if (exportDateDirs != null && exportDateDirs.length > 0)
				{
					Arrays.sort(exportDateDirs);
					String latestFolderName = exportDateDirs[exportDateDirs.length-1].getName();
					logger.debug("\n\tLatest folder name "+latestFolderName);
					File[] exportedFiles = exportDateDirs[exportDateDirs.length-1].listFiles(new FileFilter() {
						public boolean accept(File file) {
							return (file.getName().indexOf(M9kStationConstants.MEASUREMENTS) != -1);
						}
					});
					if (exportedFiles != null && exportedFiles.length > 0)
					{
						Arrays.sort(exportedFiles);
						String latestFileName = exportedFiles[exportedFiles.length-1].getName();
						logger.debug("\n\tLatest File name "+latestFileName + " size of folder "+exportedFiles.length);
		//				String dateFromFile = latestFileName.substring(0, latestFileName.indexOf(",",latestFileName.indexOf(",")+1));
						String lastExportedTime = latestFileName.substring(0, latestFileName.indexOf(",",latestFileName.indexOf(",")+1));
						logger.debug("Time portion of file name "+lastExportedTime);
						// START: 12-Dec-2023 - Updating date parsing logic to Java 8
						DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern(M9kStationConstants.COMTRADE_FILE_DATE_FORMAT);
						LocalDateTime lastExportedLocalDateTime = LocalDateTime.parse(lastExportedTime, dateformatter);
						long latestTimeFromFile = (lastExportedLocalDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()*1000) + (((sizeInMinutes*60) * 1000000) - singleSampleTime);
						
//						SimpleDateFormat sdf = new SimpleDateFormat(M9kStationConstants.COMTRADE_FILE_DATE_FORMAT);
//						Date latestTime = sdf.parse(lastExportedTime);
//						logger.debug("Formatted Date "+latestTime);
//						Date latestContStartTime = new Date(latestTime.getTime());
//						logger.debug("Available end time "+latestContStartTime);
//						logger.debug("time in milli last time "+latestContStartTime.getTime());
//						logger.debug(" Measurement singleSampleTime "+singleSampleTime+" sizeInMinutes "+sizeInMinutes+" (((sizeInMinutes*60) * 1000000) - singleSampleTime "+(((sizeInMinutes*60) * 1000000) - singleSampleTime));
//						long latestTimeFromFile = M9kBackupUtil.getConvertedEPOCHTime(latestContStartTime.getTime()) + (((sizeInMinutes*60) * 1000000) - singleSampleTime);
//						logger.debug("Osc: Latest exported time after adjusting to UTC "+latestTimeFromFile+ " Start Time of the day "+startOfDay );
						// END: 12-Dec-2023
						if (latestTimeFromFile < oldestDataStartHour) 
						{
							calculatedStartTime = oldestDataStartHour;
						}
						else 
						{
							calculatedStartTime = latestTimeFromFile;
						}
					}
					else
					{
						calculatedStartTime = oldestDataStartHour;
					}
				}
				else
				{
					calculatedStartTime = oldestDataStartHour;
				}
				
			} catch (Exception e) {
				
				logger.error("An error occurred calculating the last exported time. Exports will start from last hour from default. ",e);
			}

		logger.debug("Calculated start time "+ calculatedStartTime);
		return calculatedStartTime;
	}
	
	// START: 12-Dec-2023 - Updated logic to Java 8
	private long calculateContStartTime()
	{
		long calculatedStartTime = -1; // return the actual start time i.e. an hour before by default
		try {
			
			String latestFileName = M9kBackupUtil.getLatestFilename(destExportsDirectory, M9kStationConstants.MEASUREMENTS);
			if (latestFileName != null && latestFileName.length() > 0)
			{
				String lastExportedTime = latestFileName.substring(0, latestFileName.indexOf(",",latestFileName.indexOf(",")+1));
				logger.debug("Time portion of file name "+lastExportedTime);
				DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern(M9kStationConstants.COMTRADE_FILE_DATE_FORMAT);
				LocalDateTime lastExportedLocalDateTime = LocalDateTime.parse(lastExportedTime, dateformatter);
				long latestTimeFromFile = (lastExportedLocalDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()*1000) + (((sizeInMinutes*60) * 1000000) - singleSampleTime);
				
				if (latestTimeFromFile < oldestDataStartHour) 
				{
					calculatedStartTime = oldestDataStartHour;
				}
				else 
				{
					calculatedStartTime = latestTimeFromFile;
				}
			}
			else
			{
				calculatedStartTime = oldestDataStartHour;
			}
				
			} catch (Exception e) {
				
				logger.error("An error occurred calculating the last exported time. Exports will start from last hour from default. ",e);
			}

		logger.debug("Calculated start time "+ calculatedStartTime);
		return calculatedStartTime;
	}

	public void removeOldContComtrades()
	{
		String dateTodelete = M9kBackupUtil.getDateFolderToDelete(contComtradeNoOfDaysToRetain);
		File folderToDelete;
		logger.debug("Cont Osc export folder to be deleted "+dateTodelete);
		
		try
		{
			File[] exportedDateFolders = M9kBackupUtil.getAutoExportDateFolders(destExportsDirectory);
			if (exportedDateFolders != null)
			{
				for (int j = 0; j < exportedDateFolders.length; j++) {
					folderToDelete = exportedDateFolders[j];
					if (folderToDelete.getName().compareTo(dateTodelete) <= 0)
					{
						logger.info("Cont Osc export folder to be deleted "+dateTodelete);
						File[] exportedFiles = folderToDelete.listFiles();
						for (int i = 0; i < exportedFiles.length; i++) {
							if (exportedFiles[i].delete())
							{
								logger.debug("Succesffully deleted "+exportedFiles[i]);
							}
							else
							{
								logger.debug("Filed to delete "+exportedFiles[i]);
							}
							
						}
						folderToDelete.delete();
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Unable to delete the folder "+dateTodelete,e);
		}
	}

	public String[] getMeasurementTypes() {
		logger.debug("Inside getMeasurementTypes");
    	String[] arrMeasurementTypes = null;
		try {
			List<String> lstMeasurementTypes = M9kStationXMLUtil.getMeasurementTypes();
			if (lstMeasurementTypes == null || lstMeasurementTypes.isEmpty())
			{
				arrMeasurementTypes = new String[1];
				arrMeasurementTypes[0] = M9kConstants.RMS;				
			}
			arrMeasurementTypes = lstMeasurementTypes.toArray(new String[0]);
		} catch (Exception e) {
			logger.error("Unable to get measurement names from config xml. Returning default RMS.",e);
			arrMeasurementTypes = new String[1];
			arrMeasurementTypes[0] = M9kConstants.RMS;
		}
		logger.debug("Measurement types "+Arrays.toString(arrMeasurementTypes));
		return arrMeasurementTypes;
	}
	
	public int getRunFrequency() {
		return runFrequency;
	}
	public void setRunFrequency(int runFrequency) {
		this.runFrequency = runFrequency;
	}

	public static void main(String args[])
	{
		M9kExportContMeasurementsComtrades m9kLtrRmsFromDb = new M9kExportContMeasurementsComtrades(1561093800000000L, 1, "C:/data/m9k/");
		m9kLtrRmsFromDb.testCreateExportFiles(7);
	}


	// Test code. Not intended for any functionality
	private void testCreateExportFiles(int numberOfFilesToCreate)
	{
		long start = System.currentTimeMillis();
		long end;
	
		try {
				do {
					destFileName = createFileName();
					processMergeAndCreateComtrade();
					contStartTime = contStopTime + singleSampleTime;
					contStopTime = contStartTime + ((sizeInMinutes*60) * 1000000) - singleSampleTime;
					logger.debug("AutoExport: contStartTime "+contStartTime+"AutoExport: contStopTime "+contStopTime);
				} while ( --numberOfFilesToCreate > 0);
		} catch (Exception e) {
		}
		end = System.currentTimeMillis();
		logger.debug("Total time for creating ContMeasurements Data " + (end-start));

	}
	public StationDTO getStationDetails() {
		stationDetails = M9kStationDBUtil.getStationDetails();
		return stationDetails;
	}
}
