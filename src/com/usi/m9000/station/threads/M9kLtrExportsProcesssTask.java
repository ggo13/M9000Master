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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.station.commands.M9kCreateContinuousComtradeFilesFromDB;
import com.usi.m9000.station.commands.M9kDFRHealthStatusProcessor;
import com.usi.m9000.station.util.M9kKeyValuePair;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kConstants;


public class M9kLtrExportsProcesssTask implements Runnable {
	static boolean booStatus = true;
	private Connection mysqlConn;
	private long ltrStartTime = 0;
	private long ltrStopTime = 0;
	private int exportBufferTime;
	Map<String, Boolean> mapDfrsStatus = null;
	M9kDFRHealthStatusProcessor m9kDFRHealthStatusProcessor;
	List<DfrDTO> lstDfrs = null;
//	List<AnalogChannelDTO> lstAnalogChannels;
//	Map<Integer, Boolean> mapAnalogChnlStatus;
	String sqlQuery;
//	private static int iCnt;
	private StationDTO stationDetail;
//	private static long ltrTimeLimit;
	int finalMergedSampleCnt = 0;
	List<Integer> lstMissingExportsId = new ArrayList<Integer>();
	Map<String, Double> mapAnalogsRange = new HashMap<String, Double>();
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kLtrExportsProcesssTask.class);
	static int expCnt;
	M9kCreateContinuousComtradeFilesFromDB comtradeData;
	Map<Integer, Map<String, Integer>> mapDfrRmsExports ;
	private List<Integer> lstOfExportIdsForDDRFromConfig;
	private Map<Integer,ComtradeDataDTO> mapComtradeContDat;
	Map<Integer,Object> mapMaxValues = new HashMap<Integer, Object>();
//	private int currentExpIdProcessed; // for debugging
	private Map<Integer,ComtradeDataDTO> mapComtradeContMagnitudeDat;
	private Map<Integer,ComtradeDataDTO> mapComtradeContPhaseDat;

	public M9kLtrExportsProcesssTask(long startTime, long stopTime)
	{
		logger.debug("Entered M9kLTRRMSProcesssTask constructor with start time "+startTime+" stop time "+stopTime);
		mapComtradeContDat = new HashMap<Integer, ComtradeDataDTO>();
		mapComtradeContMagnitudeDat = new HashMap<Integer, ComtradeDataDTO>();
		mapComtradeContPhaseDat = new HashMap<Integer, ComtradeDataDTO>();
		stationDetail = M9kStationDBUtil.getStationDetails();
//		ltrStartTime = startTime-(stationDetail.getPrefaultTime()*1000);
//		ltrStopTime = stopTime+(stationDetail.getPostfaultTime()*1000);
		ltrStartTime = startTime;
		ltrStopTime = stopTime;
		logger.debug("M9kLTRRMSProcesssTask: After prefault Time start time "+ltrStartTime+" stop time "+ltrStopTime);		
		exportBufferTime = M9kStationXMLUtil.getExportBufferTime();
		m9kDFRHealthStatusProcessor = new M9kDFRHealthStatusProcessor();
//		lstAnalogChannels = M9kStationXMLUtil.getLstOfAnalogChannels();
		mapDfrsStatus = m9kDFRHealthStatusProcessor.getDFRsStatus();
		logger.debug(" mapDfrsStatus "+mapDfrsStatus);
		try {
			lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
			Collections.sort(lstDfrs, new Comparator<DfrDTO>() {

				@Override
				public int compare(DfrDTO o1, DfrDTO o2) {
//					return ((o1.getDfrId() < o2.getDfrId())?0:1);
					return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
				}
			});
			mapAnalogsRange = getAnalogChnlsRanges(mapDfrsStatus);
			comtradeData = new M9kCreateContinuousComtradeFilesFromDB(stationDetail.getSystemStationId());
			mapDfrRmsExports = M9kStationXMLUtil.getMapOfDfrRmsExports();
			lstOfExportIdsForDDRFromConfig = M9kStationXMLUtil.getExportsIdForDDRFromConfig();
//			mapAnalogChnlStatus = getChannelsExportStatus();
		} catch (Exception e) {
			logger.error("Unable to process LTR ",e);
			return;
		}
	}
	
	public void run()
	{
		processLTR();
	}
	private void processLTR()
	{
		long start = System.currentTimeMillis();
		long end;
	
		try {
//			System.out.println("Connection Request from updateDatStagingWithRemote method....Is Local? "+isLocalDBConnection());
			waitForDataAvailability();
			end = System.currentTimeMillis();
			logger.debug("Total wait time for LTR " + (end-start));
//			System.out.println("Total wait time for LTR " + (end-start));
//			logger.debug("Connection obj "+mysqlConn);
			
			
			// For debugging. Comment it later
//			prepareAndUpdateLongTermData();

			processMergeAndInsertLongTermData();
			
		
		} catch (Exception e) {
			logger.error("Exception occured in processLTR ", e);
		}
	}
	

	
	private void processMergeAndInsertLongTermData() throws M9000Exception
	{
		PreparedStatement ps = null;
		ResultSet rs= null;
		int i = 0;
		List<ComtradeDataDTO> lstComtradeDataDtos = null;
		double oscSampleCnt = getExportBufferTime() * stationDetail.getSystemSampleRate();
		logger.debug("Oscillography sample count "+oscSampleCnt);
		Double sampleTime = (((oscSampleCnt - 1)/stationDetail.getSystemSampleRate()) * 1000000);
		logger.debug("Sample time in double "+sampleTime);
		long sampleTtimeVal = sampleTime.longValue(); 
		logger.debug("SampleTimeVal long "+sampleTtimeVal);
		finalMergedSampleCnt = 0;

		// query to select the matching rows
//		String sqlQuery = "SELECT expId, name, phase, units, cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) as StartSampleTime ,tsLast, sampleRate, sampleCnt, scale, offset, if ((@tStart := round((sampleCnt -  ((CAST(tsLast  AS SIGNED) - ?) /1000000) * sampleRate))) < 0, 1, @tStart) as startSampleNo,"
//				+ "if((@tstop := round((sampleCnt - ((CAST(tsLast  AS SIGNED) - ?) / 1000000) * sampleRate )) ) < 0,sampleCnt, @tstop) as endSampleNo, data FROM `m9000`.`contAnalog` where (((? >= cast(CAST(tsLast  AS SIGNED) -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) "
//				+ "and ? <= tsLast)  or (? >= cast(CAST(tsLast  AS SIGNED) -(((sampleCnt - 1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast ) ) or ((tsLast>= ? and (? >= cast((CAST(tsLast  AS SIGNED) -(((sampleCnt - 1)/sampleRate) * 1000000)) as SIGNED)) and ? >= tsLast ))) order by expId, tsLast";

// Commented by Saravanan on 25-June-2012 - Start		
//		String sqlQuery = "SELECT expId, name, phase, units, cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) as StartSampleTime ,tsLast, sampleRate, sampleCnt, scale, offset, if ((@tStart := round((sampleCnt - ((CAST(tsLast  AS SIGNED) - ?) /1000000) * sampleRate))) < 0, 1, @tStart) as startSampleNo,"
//			+ "if((@tstop := round((sampleCnt - ((CAST(tsLast  AS SIGNED) - ?) / 1000000) * sampleRate )) ) < 0,sampleCnt, @tstop) as endSampleNo, data FROM `m9000`.`contAnalog` where (((tsLast <= ? "
//			+ "and tsLast >= ?)  or (tsLast <= ?  and tsLast >= ? ) ) or ((tsLast>= ? and tsLast <= ? and tsLast <= ?))) order by expId, tsLast";
		
//		String sqlQuery = "SELECT expId, name, phase, units, cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) as StartSampleTime ,tsLast, sampleRate, sampleCnt, scale, offset, if ((@tStart := round((sampleCnt - ((tsLast - ?) /1000000) * sampleRate))) < 0, 1, round(sampleCnt-@tStart)) as startSampleNo,"
//			+ "if((@tstop := (((CAST(tsLast  AS SIGNED) - ?) / 1000000) * sampleRate ) ) < 0,sampleCnt, round(sampleCnt-@tstop)) as endSampleNo, data FROM `m9000`.`contAnalog` where (((tsLast <= ? "
//			+ "and tsLast >= ?)  or (tsLast <= ?  and tsLast >= ? ) ) or ((tsLast>= ? and tsLast <= ? and tsLast <= ?))) order by expId, tsLast";
// End 25-June-2012
		
//		String sqlQuery = "SELECT expId, name, phase, units, cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) as StartSampleTime ,tsLast, sampleRate, sampleCnt, scale, offset, "
//			+ "data FROM `m9000`.`contAnalog` where (((tsLast <= ? "
//			+ "and tsLast >= ?)  or (tsLast <= ?  and tsLast >= ? ) ) or ((tsLast>= ? and tsLast <= ? and tsLast <= ?))) order by expId, tsLast";

		// START: 01-May-2018 - Release v1.0.8.2 - Create LTR for all exports instead of just RMS. DDR exports instead of DDR Rms.
//		String sqlQuery = "SELECT recId, expId, name, description, phase, units, tsLast, sampleRate, sampleCnt, type, "
//			+ "data FROM `m9000`.`cont` where (tsLast >= ? "
//			+ "and tsLast <= ?) and measurementType=? order by expId, tsLast";

		String commaSeperatedExportIds = getCommaSeperatedIds (lstOfExportIdsForDDRFromConfig);
		if (commaSeperatedExportIds.isEmpty())
		{
			commaSeperatedExportIds="''";
		}
		logger.debug("commaSeperatedIds "+commaSeperatedExportIds);
		String sqlQuery = "SELECT recId, expId, measurementType, name, description, phase, units, tsLast, sampleRate, sampleCnt, type, "
				+ "data FROM `m9000`.`cont` where (tsLast >= ? "
				+ "and tsLast <= ?) and expId in ("+commaSeperatedExportIds+") order by expId, tsLast";

		// END: 01-May-2018
		try {
			logger.debug("DB-POOL New connection from processMergeAndInsertLongTermData ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("LTR-RMS-DEBUG:  Start time "+ltrStartTime+" Stop time "+ltrStopTime );
			logger.debug("LTR-RMS-DEBUG:  sampleTime.longValue() "+sampleTtimeVal);
			logger.debug("LTR-RMS-DEBUG:  SQL query to be executed "+ sqlQuery);
			// START: 28-Sept-2023 - Fix for the error "java.sql.SQLException: Operation not allowed for a result set of type ResultSet.TYPE_FORWARD_ONLY."
//			ps = mysqlConn.prepareStatement(sqlQuery);
			ps = mysqlConn.prepareStatement(sqlQuery, ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			// END: 28-Sept-2023
			ps.setLong(++i, (ltrStartTime-(getExportBufferTime() * 1000000)));
			logger.debug("LTR-RMS-DEBUG:  param "+i+" - "+(ltrStartTime-(getExportBufferTime() * 1000000)));
			ps.setLong(++i, (ltrStopTime+(getExportBufferTime() * 1000000)));
			logger.debug("LTR-RMS-DEBUG:  param "+i+" - "+(ltrStopTime+(getExportBufferTime() * 1000000)));
			rs = ps.executeQuery();
			mapMaxValues = getMaxValue(rs);
			int expId;
			String analogChnlName;
			int dfrId;
			Integer rmsExpId;
			String measurementType;
			String dataType;
			int sampleCnt = 0;
			double sampleRate;
			double startSampleTime = 0;
			long lastSampleTime = 0;
			double tus;
			double tusDivBy2;
			logger.debug("mapDfrRmsExports "+mapDfrRmsExports);
			while (rs.next())
			{
				dfrId = rs.getInt("recId");
				expId = rs.getInt("expId");
				dataType = rs.getString("type");
				measurementType = rs.getString("measurementType");
				sampleCnt = rs.getInt("sampleCnt");
				sampleRate = rs.getDouble("sampleRate");
				lastSampleTime = rs.getLong("tsLast");

				tus = 1e6/sampleRate;
				tusDivBy2 = tus / 2;

				startSampleTime = (lastSampleTime - (sampleCnt-1) * tus);
				if(ltrStopTime < (startSampleTime - tusDivBy2) || ltrStartTime > (lastSampleTime + tusDivBy2))
				{
					logger.debug("TODD: LTR Exports Skipping the blob...");
					continue;
				}
				analogChnlName = getAnalogNameStrippingIndexPrefix(rs.getString("name"));
				logger.debug("LTR-RMS: Dfr Id "+dfrId+ "ExpId "+expId+" Name "+analogChnlName);
				logger.debug("dfr Id "+dfrId+"\tExport Id "+expId);
				if (mapDfrRmsExports.get(dfrId) == null)
				{
					// No dfrId found in the config
					logger.warn("Dfr Id not configured "+dfrId);
					continue;
				}
				if (measurementType.equalsIgnoreCase(M9kStationConstants.RMS))
				{
					rmsExpId = mapDfrRmsExports.get(dfrId).get(analogChnlName);
					logger.debug(" rmsExpId for analogchannelaName "+rmsExpId+" channel name "+analogChnlName+" mapDfrRmsExports.get(dfrId) "+mapDfrRmsExports.get(dfrId));
					logger.debug("Is Logic true?? "+(rmsExpId == null || rmsExpId.intValue() != expId));
					if (rmsExpId == null || rmsExpId.intValue() != expId)
					{
						// May be a duplicate export
						logger.debug("rmsExpId "+((rmsExpId!=null)?("RmsExpId "+rmsExpId.intValue()+" expId "+expId):("rmsExpId is NULL "+expId)));
						continue;
					}
				}

//				System.out.println("row cnt..."+rs.getRow());
				if (dataType != null && dataType.equalsIgnoreCase(M9kStationConstants.COMPLEX))
				{
					processComplexDataType(rs);
				}
				else // Assuming FLOAT data
				{
					processFloatDataType(rs);
				}
			}
			
			if(!mapComtradeContDat.isEmpty())
			{
				createDummyDataForMissingExports(mapComtradeContDat);
				lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContDat.values());
			}
			// Start Complex data to list
			if(!mapComtradeContMagnitudeDat.isEmpty())
			{
				createDummyComplexDataForMissingExports();
				if (lstComtradeDataDtos == null)
				{
					lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContMagnitudeDat.values());
					lstComtradeDataDtos.addAll(mapComtradeContPhaseDat.values());
				}
				else
				{
					lstComtradeDataDtos.addAll(mapComtradeContMagnitudeDat.values());
					lstComtradeDataDtos.addAll(mapComtradeContPhaseDat.values());
				}
			}

			// End complex data to list
			if(lstComtradeDataDtos != null && !lstComtradeDataDtos.isEmpty())
			{
				
//				logger.debug("Before sort "+lstComtradeDataDtos);
				Collections.sort(lstComtradeDataDtos, new Comparator<ComtradeDataDTO>() {

					@Override
					public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
						int returnVal;
						if ((o1.getDfrId() < o2.getDfrId()))
						{
							returnVal= -1;
						}
						else if (o1.getDfrId() == o2.getDfrId())
						{
							// START: 12-FEB-2015 - Trying to sort based on export Id instead of Export ChnlId
//							if (o1.getExportChnlId() < o2.getExportChnlId())
							// START: 18-Nov-2019 - To sort the channels based on the actual channel number including default RMS exports
//							if (o1.getExportId() <= o2.getExportId())
//							if ((o1.getExportId() % (o1.getDfrId() * M9kConstants.LTR_RMS_OFFSET)) <= (o2.getExportId() % (o1.getDfrId() * M9kConstants.LTR_RMS_OFFSET)))
							if (o1.getExportId() <= o2.getExportId())
							// END: 18-Nov-2019
							// END: 12-FEB-2015
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
//						return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
						return returnVal;
					}
				});
//				logger.debug("After sort "+lstComtradeDataDtos);
				comtradeData.createLTRFiles(lstComtradeDataDtos);
			}
			if(lstComtradeDataDtos != null && !lstComtradeDataDtos.isEmpty())
			{
				logger.debug("Size of analog cont..."+lstComtradeDataDtos.size());
				lstComtradeDataDtos.clear();
			}				
			getLstMissingExportsId().clear();
			getMapAnalogRanges().clear();
			mapComtradeContDat.clear();
			
		} catch (SQLException e) {
			logger.error("Error in fetching long term data ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Error in fetching long term data ", e);
			throw new M9000Exception(e);
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
//		int totalChannelCnt = 1;
		float scaleFactor = 0; // scale factor
		String analogChnlName;
		int dfrId;
//		int exportChnlId;
//		String measurementType;
		double tus;
		dfrId = rs.getInt("recId");
		expId = rs.getInt("expId");
//		measurementType = rs.getString("measurementType");
		analogChnlName = getAnalogNameStrippingIndexPrefix(rs.getString("name"));
		
//		System.out.println("row cnt..."+rs.getRow());
		sampleCnt = rs.getInt("sampleCnt");
//		logger.debug("Cont-Data-Log: Sample Cnt "+sampleCnt);
		sampleRate = rs.getDouble("sampleRate");
//		logger.debug("Cont-Data-Log: sampleRate "+sampleRate);
		is = rs.getBinaryStream("data");
		lastSampleTime = rs.getLong("tsLast");
//		logger.debug("Cont-Data-Log: lastSampleTime "+lastSampleTime);
//		startSampleNo = rs.getInt("startSampleNo");

		// START: 05-Dec-2019 Todd's logic
		tus = 1e6/sampleRate;
		
//		startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
//		logger.debug("LTR-QUERY: calculated startSampleTime"+startSampleTime);
//		
//		if (ltrStartTime > (lastSampleTime+((1/sampleRate) * 1000000)) || ltrStopTime < startSampleTime ) // OUt of range records
//		{
//			logger.debug("Skipping the current record with tsLast "+lastSampleTime+" It is less than ltrStartTime "+ ltrStartTime);
//			return;
//		}
//		
//		if (ltrStartTime >= lastSampleTime) // ltrStartTime falls between tsLast and next tsStart
//		{
//			startSampleNo = sampleCnt;
//		}
//		else
//		if (ltrStartTime < startSampleTime)
//		{
//			startSampleNo = 1;
//		}
////		if (lastSampleTime > ltrStartTime)
//		else
//		{
//			startSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - ltrStartTime)*sampleRate)/1000000);
//			if (startSampleNo < 0)
//			{
//				startSampleNo = 1;
//			}
//		}
//		logger.debug("Cont-Data-Log: Start sample No "+startSampleNo);
//		
//		if (lastSampleTime > ltrStopTime)
//		{
//			endSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - ltrStopTime)*sampleRate)/1000000);
//			if (endSampleNo < 0)
//			{
//				endSampleNo = sampleCnt;
//			}
//		}
//		else
//		{
//			endSampleNo = sampleCnt;
//		}
		
		startSampleTime = (lastSampleTime - (sampleCnt-1) * tus);
		
		startSampleNo = M9kStationUtil.toIndex(ltrStartTime - startSampleTime, tus, sampleCnt);
		logger.debug("TODD: Start sample no "+startSampleNo);
		endSampleNo = M9kStationUtil.toIndex(ltrStopTime - startSampleTime, tus, sampleCnt);
		logger.debug("TODD: End sample no "+endSampleNo);
		// END: 05-Dec-2019 
		
		logger.debug("expId to be processed "+expId);
		if (mapComtradeContDat.get(expId) == null)
		{
//			logger.debug("Cont Data-DEBUG: Start of the sample....for expId "+expId+" ->" + startSampleNo);
//			if (rs.isLast())
//			{
				sampleCnt=endSampleNo-startSampleNo+1;
//				logger.debug("Cont Data-DEBUG: Last row to be processed. End Sample..for expId "+expId+" ->" + endSampleNo+" Actual sampleCnt.."+sampleCnt);
//			}
//			iCnt = 1;
			comtradeDataDTO = new ComtradeDataDTO();
			comtradeDataDTO.setTsLast( lastSampleTime);
			if (ltrStartTime < startSampleTime) // Missing data at the start
			{
				// Construct dummy data for initial missing data
				logger.debug("Missing data at the start");
				missingSampleCnt = Math.floor(((startSampleTime - ltrStartTime )/1000000.0)*sampleRate);
				isData = constructDummyContinuousData(missingSampleCnt.intValue());
				comtradeDataDTO.setBinaryDataSteam(isData);
			}
//			scaleFactor = (32767.0f / getMapAnalogRanges().get(expId));
//			if (dataType.endsWith(M9kStationConstants.WATTS) || dataType.endsWith(M9kStationConstants.VARS) || dataType.toLowerCase().indexOf("sequence") > -1)
//			// For watts, vars, sequence and virtual measurement is either linegroup or another measurement so we can use the actual expId as exportChnlId
//			if (measurementType.endsWith(M9kStationConstants.WATTS) || measurementType.endsWith(M9kStationConstants.VARS)
//					|| measurementType.endsWith(M9kStationConstants.SEQ_VOLTAGE)
//					|| measurementType.endsWith(M9kStationConstants.SEQ_CURRENT)
//					|| measurementType.equalsIgnoreCase(M9kStationConstants.VIRTUAL_MEASUREMENT))
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
				scaleFactor = (32767.0f / getMapAnalogRanges().get(analogChnlName).floatValue());
//				logger.debug("ExpId "+exportChnlId+" scale factor "+scaleFactor);
			}
			else
			{
				scaleFactor = (32767.0f / ((Float)mapMaxValues.get(expId)).floatValue());
//				logger.debug("ExpId "+exportChnlId+" scale factor "+scaleFactor);
			}
			comtradeDataDTO.setScaleFactor(scaleFactor);
//			logger.debug("\n\n\t\t Create cont data for expId "+expId);
			expCnt = expId;
			isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo, scaleFactor);
//			logger.debug("\n\n\t\t Done for expId "+expId);
			comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
			comtradeDataDTO.setStationId(stationDetail.getSystemStationId());
			logger.debug("\t\t\t\t\t\t Station Name "+stationDetail.getSystemStationName());
			comtradeDataDTO.setStationName(stationDetail.getSystemStationName());
			comtradeDataDTO.setDfrId(dfrId);
//			comtradeDataDTO.setExportChnlId(exportChnlId);
			comtradeDataDTO.setRecordingDevId(stationDetail.getSystemRecordingDeviceId());
			// Commented as the sequence index, first column of cfg is added wgile creating cfg file
//			channelInfoBuffer = new StringBuffer(totalChannelCnt++ +M9kStationConstants.COMMA_SEPERATOR);
			channelInfoBuffer = new StringBuffer();
			// START: 18-Mar-2018 Prefix A or VIRTUAL_ANALOG prefix in cfg files
			// START: 18-Nov-2019 Change prefix only to virtual not to default RMS exports
//			if (expId <= M9kStationXMLUtil.getSystem().getAnalogsCount())
			if (analogChnlName.toUpperCase().indexOf(M9kStationConstants.VIRTUAL_ANALOG.toUpperCase()) == -1)
			// END: 18-Nov-2019
			{
				channelInfoBuffer.append("A"+expId+M9kStationConstants.COMMA_SEPERATOR);
			}
			else
			{
				channelInfoBuffer.append(M9kStationConstants.VIRTUAL_PREFIX+expId+M9kStationConstants.COMMA_SEPERATOR);
			}

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
//			comtradeDataDTO.setType(M9kStationConstants.RMS);
			comtradeDataDTO.setType(M9kStationConstants.MEASUREMENTS);
			comtradeDataDTO.setBinaryDataSteam(isData);
			isData.close();
			mapComtradeContDat.put(expId, comtradeDataDTO);
			comtradeDataDTO.setTsPrefault(ltrStartTime);
//			if ((ltrStopTime - ltrStartTime) > ((stationDetail.getSystemLtrPrefaultTime()*1000000) + (stationDetail.getSystemLtrPostfaultTime()*1000000)))
//			{
//				comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getSystemLtrPrefaultTime()*1000000));
//			}
//			else
//			{
				comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getSystemLtrPrefaultTime()*1000000));
//			}
		}
		else
		{
			comtradeDataDTO = mapComtradeContDat.get(expId);
//			System.out.println("Comtrade Data DTO "+comtradeDataDTO);
//			logger.debug("Cont Data-DEBUG: start Time "+startSampleTime+" Last time of the previoud record "+comtradeDataDTO.getTsLast()+" previoud Last Time +1 "+ (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)));
			if (startSampleTime > (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)) ) // First sample should be Last sample + 1 of the previous record
			{
				missingSampleCnt = Math.floor(((startSampleTime - comtradeDataDTO.getTsLast() )/1000000.0)*sampleRate);
				isData = constructDummyContinuousData(missingSampleCnt.intValue());
				comtradeDataDTO.setBinaryDataSteam(isData);
				isData.close();
			}
			comtradeDataDTO.setTsLast(lastSampleTime);
//			startSampleNo = -1;
//			if (rs.isLast())
//			{
//				logger.debug("Cont Data-DEBUG: processing expId "+expId); 
//				endSampleNo = rs.getInt("endSampleNo");
//				System.out.println("Cont-Data-Log:  StartSampleNo..."+startSampleNo+" End Sample No "+endSampleNo+" scale factor "+comtradeDataDTO.getScaleFactor());
//				logger.debug("\n\n\t\t else part Create cont data for expId "+expId);
				isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo,comtradeDataDTO.getScaleFactor());
//				logger.debug("\n\n\t\t else part create done for expId "+expId);
				 
				sampleCnt=(endSampleNo-startSampleNo+1)+comtradeDataDTO.getSampleCnt();
//				logger.debug("Cont-Data-Log: sample count for expId "+expId+" is "+sampleCnt );
//			}
//			else
//			{
//				logger.debug("Cont Data-DEBUG: Still processing for expId "+expId); 
//				isData = constructContinuousData(is, sampleRate);
//				sampleCnt+=comtradeDataDTO.getSampleCnt()-1;
//				logger.debug("Cont Data-DEBUG: sample count as of now for expId "+expId+" is "+sampleCnt );
//			}
			
			comtradeDataDTO.setSampleCnt(sampleCnt);
//			comtradeDataDTO.setBinaryDataSteam(new SequenceInputStream(comtradeDataDTO.getBinaryDataSteam(), isData));
			comtradeDataDTO.setBinaryDataSteam(isData);
//			comtradeDataDTO.setTsPrefault(ltrStartTime);
//			comtradeDataDTO.setTsTrigger(ltrStartTime+stationDetail.getSystemLtrPrefaultTime());
		}
		logger.debug("LTRMERGE-DEBUG: FLOAT expId "+expId+" total sample count "+sampleCnt+" finalMergedSampleCnt "+finalMergedSampleCnt);
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
		
//		expId = expId - M9kStationConstants.LTR_RMS_OFFSET; // Removing the offset added to export Id during the rms export
		analogChnlName = getAnalogNameStrippingIndexPrefix(rs.getString("name"));
//		logger.debug("Cont-Data-Debug: getMapAnalogRanges() "+getMapAnalogRanges()+ "Dfr Id "+dfrId+ "ExpId "+expId+" Name "+analogChnlName);
//		if (dataUnits.equalsIgnoreCase("V"))
//		{
//			if (mapDfrRmsExports.get(dfrId) == null)
//			{
//				// No dfrId found in the config
//				logger.warn("Dfr Id not configured "+dfrId);
//				continue;
//			}
//			rmsExpId = mapDfrRmsExports.get(dfrId).get(analogChnlName); 
//			if (rmsExpId == null || rmsExpId != expId)
//			{
//				// Not a rms export
//				continue;
//			}
//		}
//		System.out.println("row cnt..."+rs.getRow());
		sampleCnt = rs.getInt("sampleCnt");
//		logger.debug("Cont-Data-Log: Sample Cnt "+sampleCnt);
		sampleRate = rs.getDouble("sampleRate");
//		logger.debug("Cont-Data-Log: sampleRate "+sampleRate);
		is = rs.getBinaryStream("data");
		lastSampleTime = rs.getLong("tsLast");
//		logger.debug("Cont-Data-Log: lastSampleTime "+lastSampleTime);
//		startSampleNo = rs.getInt("startSampleNo");

		// START: 05-Dec-2019 Todd's logic
		tus = 1e6/sampleRate;
//		startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
//		logger.debug("LTR-QUERY: calculated startSampleTime"+startSampleTime);
//		
//		if (ltrStartTime > (lastSampleTime+((1/sampleRate) * 1000000)) || ltrStopTime < startSampleTime ) // OUt of range records
//		{
//			logger.debug("Skipping the current record with tsLast "+lastSampleTime+" It is less than ltrStartTime "+ ltrStartTime);
//			return;
//		}
//		
//		if (ltrStartTime >= lastSampleTime) // ltrStartTime falls between tsLast and next tsStart
//		{
//			startSampleNo = sampleCnt;
//		}
//		else
//		if (ltrStartTime < startSampleTime)
//		{
//			startSampleNo = 1;
//		}
////		if (lastSampleTime > ltrStartTime)
//		else
//		{
//			startSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - ltrStartTime)*sampleRate)/1000000);
//			if (startSampleNo < 0)
//			{
//				startSampleNo = 1;
//			}
//		}

//		if (lastSampleTime > ltrStartTime)
//		{
//			startSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - ltrStartTime)*sampleRate)/1000000);
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
		
//		endSampleNo = rs.getInt("endSampleNo");
//		if (lastSampleTime > ltrStopTime)
//		{
//			endSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - ltrStopTime)*sampleRate)/1000000);
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
		
//		startSampleTime = rs.getLong("StartSampleTime");
//		logger.debug("Cont-Data-Log: from DB startSampleTime"+startSampleTime);
//		startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
//		logger.debug("CONT-PHASOR: calculated startSampleTime"+startSampleTime);
		startSampleTime = (lastSampleTime - (sampleCnt-1) * tus);
		
		startSampleNo = M9kStationUtil.toIndex(ltrStartTime - startSampleTime, tus, sampleCnt);
		logger.debug("TODD: Start sample no "+startSampleNo);
		endSampleNo = M9kStationUtil.toIndex(ltrStopTime - startSampleTime, tus, sampleCnt);
		logger.debug("TODD: End sample no "+endSampleNo);
		// END: 05-Dec-2019 
		
		if (mapComtradeContMagnitudeDat.get(expId) == null)
		{
			logger.debug("CONT-PHASOR: Start of the sample....for expId "+expId+" ->" + startSampleNo);
//			if (rs.isLast())
//			{
				sampleCnt=endSampleNo-startSampleNo+1;
				logger.debug("CONT-PHASOR: Last row to be processed. End Sample..for expId "+expId+" ->" + endSampleNo+" Actual sampleCnt.."+sampleCnt);
//			}
//			iCnt = 1;
			comtradeMagnitudeDataDTO = new ComtradeDataDTO();
			comtradePhaseDataDTO = new ComtradeDataDTO();
			comtradeMagnitudeDataDTO.setTsLast( lastSampleTime);
			comtradePhaseDataDTO.setTsLast( lastSampleTime);
			if (ltrStartTime < startSampleTime) // Missing data at the start
			{
				// Construct dummy data for initial missing data
				logger.debug("Missing data at the start");
				missingSampleCnt = Math.floor(((startSampleTime - ltrStartTime )/1000000.0)*sampleRate);
				isData = constructDummyContinuousComplexData(missingSampleCnt.intValue());
				comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
				comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
				isData[0].close();
				isData[1].close();
			}
//			scaleFactor = (32767.0f / getMapAnalogRanges().get(expId));
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
				magScaleFactor = (32767.0f / getMapAnalogRanges().get(analogChnlName).floatValue());
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
//			logger.debug("\n\n\t\t Create cont data for expId "+expId);
			expCnt = expId;
//			logger.debug("\n\n\t\t Done for expId "+expId);
			comtradeMagnitudeDataDTO.setExportId(expId); // Just a value to use in file name creation
			comtradePhaseDataDTO.setExportId(expId); // Just a value to use in file name creation
			comtradeMagnitudeDataDTO.setStationId(stationDetail.getSystemStationId());
			comtradePhaseDataDTO.setStationId(stationDetail.getSystemStationId());
			logger.debug("\t\t\t\t\t\t Station Name "+stationDetail.getSystemStationName());
			comtradeMagnitudeDataDTO.setStationName(stationDetail.getSystemStationName());
			comtradePhaseDataDTO.setStationName(stationDetail.getSystemStationName());
			comtradeMagnitudeDataDTO.setDfrId(dfrId);
			comtradePhaseDataDTO.setDfrId(dfrId);
//			comtradeMagnitudeDataDTO.setExportChnlId(exportChnlId);
//			comtradePhaseDataDTO.setExportChnlId(exportChnlId);
			comtradeMagnitudeDataDTO.setRecordingDevId(stationDetail.getSystemRecordingDeviceId());
			comtradePhaseDataDTO.setRecordingDevId(stationDetail.getSystemRecordingDeviceId());
			// Commented as the sequence index, first column of cfg is added wgile creating cfg file
//			channelInfoBuffer = new StringBuffer(totalChannelCnt++ +M9kStationConstants.COMMA_SEPERATOR);
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
//			comtradeMagnitudeDataDTO.setType(M9kStationConstants.MAGNITUDE);
//			comtradePhaseDataDTO.setType(M9kStationConstants.PHASE);
			comtradeMagnitudeDataDTO.setType(M9kStationConstants.MEASUREMENTS);
			comtradePhaseDataDTO.setType(M9kStationConstants.MEASUREMENTS);

			comtradeMagnitudeDataDTO.setTsPrefault(ltrStartTime);
			comtradePhaseDataDTO.setTsPrefault(ltrStartTime+(stationDetail.getSystemLtrPrefaultTime()*1000000));
			comtradeMagnitudeDataDTO.setTsTrigger(ltrStartTime);
			comtradePhaseDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getSystemLtrPrefaultTime()*1000000));
			
			// Phase data
			
			isData = constructContinuousComplexData(is, sampleRate,startSampleNo, endSampleNo, magScaleFactor, phaseScaleFactor);
			comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
			comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
			isData[0].close();
			isData[1].close();
			mapComtradeContMagnitudeDat.put(expId, comtradeMagnitudeDataDTO);
			mapComtradeContPhaseDat.put(expId, comtradePhaseDataDTO);
//			if ((ltrStopTime - ltrStartTime) > ((stationDetail.getSystemLtrPrefaultTime()*1000000) + (stationDetail.getSystemLtrPostfaultTime()*1000000)))
//			{
//				comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getSystemLtrPrefaultTime()*1000000));
//			}
//			else
//			{
//			}
		}
		else
		{
			comtradeMagnitudeDataDTO = mapComtradeContMagnitudeDat.get(expId);
			comtradePhaseDataDTO = mapComtradeContPhaseDat.get(expId);
//			System.out.println("Comtrade Data DTO "+comtradeDataDTO);
//			logger.debug("CONT-PHASOR: start Time "+startSampleTime+" Last time of the previoud record "+comtradeDataDTO.getTsLast()+" previoud Last Time +1 "+ (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)));
			if (startSampleTime > (comtradeMagnitudeDataDTO.getTsLast() +((1/sampleRate)*1000000)) ) // First sample should be Last sample + 1 of the previous record
			{
				missingSampleCnt = Math.floor(((startSampleTime - comtradeMagnitudeDataDTO.getTsLast() )/1000000.0)*sampleRate);
				isData = constructDummyContinuousComplexData(missingSampleCnt.intValue());
				comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
				comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
				isData[0].close();
				isData[1].close();
			}
			comtradeMagnitudeDataDTO.setTsLast(lastSampleTime);
			comtradePhaseDataDTO.setTsLast(lastSampleTime);
//			startSampleNo = -1;
//			if (rs.isLast())
//			{
//				logger.debug("CONT-PHASOR: processing expId "+expId); 
//				endSampleNo = rs.getInt("endSampleNo");
//				System.out.println("Cont-Data-Log:  StartSampleNo..."+startSampleNo+" End Sample No "+endSampleNo+" scale factor "+comtradeDataDTO.getScaleFactor());
//				logger.debug("\n\n\t\t else part Create cont data for expId "+expId);
				isData = constructContinuousComplexData(is, sampleRate,startSampleNo, endSampleNo,comtradeMagnitudeDataDTO.getScaleFactor(), comtradePhaseDataDTO.getScaleFactor());
//				logger.debug("\n\n\t\t else part create done for expId "+expId);
				 
				sampleCnt=(endSampleNo-startSampleNo+1)+comtradeMagnitudeDataDTO.getSampleCnt();
				logger.debug("Total complex Sample count "+sampleCnt);
//				logger.debug("Cont-Data-Log: sample count for expId "+expId+" is "+sampleCnt );
//			}
//			else
//			{
//				logger.debug("CONT-PHASOR: Still processing for expId "+expId); 
//				isData = constructContinuousData(is, sampleRate);
//				sampleCnt+=comtradeDataDTO.getSampleCnt()-1;
//				logger.debug("CONT-PHASOR: sample count as of now for expId "+expId+" is "+sampleCnt );
//			}
			
			comtradeMagnitudeDataDTO.setSampleCnt(sampleCnt);
			comtradePhaseDataDTO.setSampleCnt(sampleCnt);
//			comtradeDataDTO.setBinaryDataSteam(new SequenceInputStream(comtradeDataDTO.getBinaryDataSteam(), isData));
			comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
			comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
			isData[0].close();
			isData[1].close();
//			comtradeDataDTO.setTsPrefault(ltrStartTime);
//			comtradeDataDTO.setTsTrigger(ltrStartTime+stationDetail.getSystemLtrPrefaultTime());
		}
		logger.debug("LTRMERGE-DEBUG: COMPLEX expId "+expId+" total sample count "+sampleCnt+" finalMergedSampleCnt "+finalMergedSampleCnt);
		if (sampleCnt > finalMergedSampleCnt)
		{
			finalMergedSampleCnt = sampleCnt;
			logger.debug("CONT-PHASOR: finalMergedSampleCnt "+finalMergedSampleCnt);
		}
		is.close();
	
	}

	@SuppressWarnings("unused")
	private boolean insertIntoLtr(List<ComtradeDataDTO> lstComtradeDataDtos)
	{
		boolean status = true;
		PreparedStatement ps = null;
		ResultSet rs= null;
		int i = 0;
		String sqlQuery = "insert into long_term_dat_staging (stationId, expId, type, tsPrefault, tsTrigger, ltrPreFaultTime, ltrPostFaultTime, lineFreq, ltrSampleRate, ltrSampleCnt, analogs, data, dataBlob) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try
		{
			ps = mysqlConn.prepareStatement(sqlQuery);
			for (Iterator<ComtradeDataDTO> iterator = lstComtradeDataDtos.iterator(); iterator
					.hasNext();) {
				ComtradeDataDTO comtradeDto = iterator
						.next();
				i = 0;
				ps.setInt(++i, stationDetail.getSystemStationId());
				ps.setInt(++i, comtradeDto.getExportId());
				ps.setString(++i, M9kStationConstants.RMS);
				ps.setLong(++i,comtradeDto.getTsPrefault()); 
				ps.setLong(++i,comtradeDto.getTsTrigger());
				ps.setInt(++i, stationDetail.getSystemLtrPrefaultTime());
				ps.setInt(++i, stationDetail.getSystemLtrPostfaultTime());
				ps.setInt(++i, stationDetail.getSystemLineFrequency());
				ps.setDouble(++i, comtradeDto.getSampleRate());
				ps.setInt(++i, comtradeDto.getSampleCnt());
				ps.setString(++i, comtradeDto.getAnalogs().toString());
				ps.setString(++i, null);
				ps.setBinaryStream(++i, comtradeDto.getBinaryDataSteam());
				
				int insertStatus = ps.executeUpdate();
				
				logger.debug("Insert status "+insertStatus);
			}
		}
		catch (SQLException e) {
			logger.error("Insert into long_term_dat_staging failed",e);
		}
		catch (Exception e) {
			// TODO: handle exception
			logger.error("Insert into long_term_dat_staging failed",e);
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
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}
		return status;
	}
	private void waitForDataAvailability()
	{
//		List<M9kKeyValuePair> lstExpIds = new ArrayList<M9kKeyValuePair>();
		Map<String,String> mapExpIds = new HashMap<String, String>();
		PreparedStatement ps = null;
		ResultSet rs= null;
		boolean booContinue = true;
		logger.debug("Active dfrs "+getMapAnalogRanges().size());
		logger.debug("LTR stop time "+getLtrStopTime()+" IRIG time "+M9kStationUtil.getIRIG());
		logger.debug("LTR export analog buffer time "+getExportBufferTime()+" LTR prefault time "+stationDetail.getSystemLtrPrefaultTime());
		long thresholdTime = 0;
		try {
			thresholdTime = (getLtrStopTime()+ (stationDetail.getSystemLtrPrefaultTime() * 1000000)+ (getExportBufferTime()*1000000)*2) ;
			
			logger.debug("DB-POOL New connection from waitForDataAvailability . Threshold Time "+thresholdTime);
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			ps = mysqlConn.prepareStatement("select expId,name,recId from cont A where tsLast >= ? and measurementType = ? group by expId,name,recId");
			while (booContinue)
			{

				ps.setLong(1, (getLtrStopTime()+(getExportBufferTime() * 1000000)));
				ps.setString(2, M9kStationConstants.RMS);
				rs = ps.executeQuery();
				while (rs.next())
				{
//					lstExpIds.add(new M9kKeyValuePair(""+rs.getInt("expId"), rs.getInt("recId")+"-"+getAnalogNameStrippingIndexPrefix(rs.getString("name"))));
					mapExpIds.put(""+rs.getInt("expId"), rs.getInt("recId")+"-"+getAnalogNameStrippingIndexPrefix(rs.getString("name")));
				}
				rs.close();
				logger.debug("List of expIds from db "+mapExpIds);
				logger.debug("List of expIds from config "+lstOfExportIdsForDDRFromConfig);

				logger.debug("LTR threshold time calculated "+thresholdTime+"(thresholdTime - M9kStationUtil.getIRIG()) "+(thresholdTime - M9kStationUtil.getIRIG()));
				logger.debug("total available exports "+mapExpIds.size() + " rms Exports count "+lstOfExportIdsForDDRFromConfig.size());
				//TODO: Get all the channels that are exported and evaluate the count here.
				if (mapExpIds.size() == lstOfExportIdsForDDRFromConfig.size() || (thresholdTime - M9kStationUtil.getIRIG()) <= 0)
					//			if ((thresholdTime - M9kStationUtil.getIRIG()) <= 0)
				{
					if (!mapExpIds.isEmpty() && mapExpIds.size() < lstOfExportIdsForDDRFromConfig.size())
					{
						updateMissingExports(mapExpIds);
					}
					booContinue = false;
					break;
				}
				else
				{
					mapExpIds.clear();
					try {
						//					logger.debug("Delay the spawning to avoid rush.");
						Thread.sleep(5000); // Wait for the latency time before reading again
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			ps.close();
	} catch (SQLException e) {
		logger.error("Error occured during waitForDataAvailability  ", e);
	} catch (Exception e) {
		logger.error("Error occured during waitForDataAvailability  ", e);
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
//				logger.info("Closing the connection... "+mysqlConn);
				logger.debug("DB-POOL Closing connection from waitForDataAvailability ");
				mysqlConn.close();
				mysqlConn = null;
			}
		} catch (Exception e) {
			logger.warn("Error in cleanup", e);
		}
	}
		
	}
	
	private void updateMissingExports(Map<String,String> mapExportsId)
	{
//		for (Iterator<Integer> iterator = getMapAnalogRanges().keySet().iterator(); iterator.hasNext();) {
//			Integer id =  iterator.next();
//			logger.debug("LTR-RMS-DEBUG-DUMMY: Id from analog export xml "+id);
//			if (!lstExporsId.contains(id))
//			{
//				logger.debug("Addded to missing list "+id);
//				getLstMissingExportsId().add(id);
//			}
//			
//		}
		Integer expId;
		for (Iterator<String> iterator = mapExportsId.keySet().iterator(); iterator.hasNext();) {
			expId = Integer.parseInt(iterator.next());  
			logger.debug("ExpId "+expId);
			if (getMapAnalogRanges().get(mapExportsId.get(""+expId)) == null)
			{
				logger.debug("Addded to missing list "+expId);
				getLstMissingExportsId().add(expId);
			}
		}
		
		logger.debug("LTR-RMS-DEBUG-DUMMY: Missing exports size.."+getLstMissingExportsId().size());
	}
	
//	private Map<Integer, Boolean> getChannelsExportStatus()
//	{
//		Map<Integer, Boolean> mapChnlExpStatus = new HashMap<Integer, Boolean>();
//    	for (Iterator<AnalogChannelDTO> iterator = lstAnalogChannels.iterator(); iterator.hasNext();) {
//			AnalogChannelDTO analogChnl  =  iterator.next();
//			logger.debug("DFR ID DFR"+analogChnl.isExportStatus());
//			mapChnlExpStatus.put(Integer.parseInt(analogChnl.getChannel()), analogChnl.isExportStatus());
//		}
//		return mapChnlExpStatus;
//	}

    private Map<String, Double> getAnalogChnlsRanges(Map<String, Boolean> mapDfrsStatus2)
    {
    	Map<String, Double> mapAnalogRanges = new HashMap<String, Double>();
    	Double range;
    	Integer txRatio;
		AnalogChannelDTO analogChnl;
		AnalogChannelDTO virtAnalogChnls;

    	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
    		DfrDTO dfrDto = iterator.next();
			if (mapDfrsStatus2.get("DFR"+dfrDto.getDfrId()) && dfrDto.getAnalogChnlCnt() > 0)
			{
		    	for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
					analogChnl  =  iterator1.next();
					if ( (analogChnl.getLstAnalogsForVirtual() != null && !analogChnl.getLstAnalogsForVirtual().isEmpty()))
					{
						continue; // process the virtual channel in the below for loop 
					}
					logger.debug("LTR-RMS: Channel" +analogChnl.getChannel()+" - Range "+analogChnl.getRange());
					range = Double.parseDouble(analogChnl.getRange());
					if (M9kStationXMLUtil.isSecondary())
					{
						txRatio = Integer.parseInt(analogChnl.getPrimaryRatio()) / Integer.parseInt(analogChnl.getSecondaryRatio());
						range = range * txRatio;
						logger.debug("It is secondary. TxRatio "+txRatio+" Range "+range);
					}
					mapAnalogRanges.put(dfrDto.getDfrId()+"-"+getAnalogNameStrippingIndexPrefix(analogChnl.getName()), range);
				}
		    	logger.debug("Map ANalog exports "+mapAnalogRanges);
		    	for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
					analogChnl  =  iterator1.next();
					logger.debug("LTR-RMS: Channel" +analogChnl.getChannel()+" - Range "+analogChnl.getRange());
					range = Double.parseDouble(analogChnl.getRange());
					if ( (analogChnl.getLstAnalogsForVirtual() == null || analogChnl.getLstAnalogsForVirtual().isEmpty()))
					{
						continue;// Skip physical channels as it is processed in the above for loop
					}
					range = 0.0;
					logger.debug(" Virtual channels  "+analogChnl.getLstAnalogsForVirtual());
					for (Iterator<AnalogChannelDTO> iterator2 = analogChnl.getLstAnalogsForVirtual().iterator(); iterator2
							.hasNext();) {
						virtAnalogChnls = iterator2.next();
						logger.debug("Virtual channel range "+virtAnalogChnls+" virtAnalogChnls.getName() "+virtAnalogChnls.getName());
						range = (range + mapAnalogRanges.get(dfrDto.getDfrId()+"-"+getAnalogNameStrippingIndexPrefix(virtAnalogChnls.getName())).intValue());
						logger.debug(" range after intValue "+range);
					}
					mapAnalogRanges.put(dfrDto.getDfrId()+"-"+getAnalogNameStrippingIndexPrefix(analogChnl.getName()), range);
				}
			}
    	}
//    	for (Iterator<Boolean> iterator = mapDFrs.values().iterator(); iterator.hasNext();) {
//    		Boolean isActive = iterator.next();
//			if (isActive)
//			{
//				activeCount++;
//			}
//		}
		logger.debug("LTR-RMS: Total analogs exported "+mapAnalogRanges.size()+" actual values "+mapAnalogRanges);
    	return mapAnalogRanges;
    }

	
	private void createDummyDataForMissingExports(
			Map<Integer, ComtradeDataDTO> mapComtradeContDat) {
		ComtradeDataDTO comtradeDataDTO = mapComtradeContDat.values().iterator().next();
		ComtradeDataDTO dummyComtradeDataDTO = null;
		InputStream isData;
		StringBuffer channelInfoBuffer;
		int expId;
		for (Iterator<Integer> iteratorExpIds = mapComtradeContDat.keySet().iterator(); iteratorExpIds.hasNext();) {
			expId = iteratorExpIds.next();
			// If the data is missing at the end for any dfrs
			if (mapComtradeContDat.get(expId) != null && mapComtradeContDat.get(expId).getSampleCnt() < finalMergedSampleCnt)
			{
				logger.debug("LTR-RMS-DEBUG-DUMMY: Data missing at the end for expId "+expId+" existing sample count "+mapComtradeContDat.get(expId).getSampleCnt());
				dummyComtradeDataDTO = mapComtradeContDat.get(expId);
				isData = constructDummyContinuousData(dummyComtradeDataDTO);
				dummyComtradeDataDTO.setBinaryDataSteam(isData);
				dummyComtradeDataDTO.setSampleCnt(finalMergedSampleCnt);
			}

		}
		for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
    		DfrDTO dfrDto = iterator.next();
			if (!getMapDfrsStatus().get("DFR"+dfrDto.getDfrId()) && dfrDto.getAnalogChnlCnt() > 0)
			{
	    		logger.debug("DFR"+dfrDto.getDfrId()+" is down and dummy data is created.");
		    	for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
					AnalogChannelDTO analogChnl  =  iterator1.next();
					logger.debug("DFR ID DFR"+analogChnl.isExportStatus());
					expId = Integer.parseInt(analogChnl.getChannel());
					logger.debug("LTR-RMS-DEBUG-DUMMY sample Count "+finalMergedSampleCnt+" for export id "+expId);

					if (mapComtradeContDat.get(expId) != null)
					{
						logger.debug("LTR-RMS-DEBUG-DUMMY: ExpId exists already "+expId);
						dummyComtradeDataDTO = mapComtradeContDat.get(expId);
						isData = constructDummyContinuousData(dummyComtradeDataDTO);
						dummyComtradeDataDTO.setBinaryDataSteam(isData);
						dummyComtradeDataDTO.setSampleCnt(finalMergedSampleCnt);
					}
					else
					{
						logger.debug("LTR-RMS-DEBUG-DUMMY: ExpId doesn't exists already "+expId);
						dummyComtradeDataDTO = new ComtradeDataDTO();
						dummyComtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
						dummyComtradeDataDTO.setStationId(stationDetail.getSystemStationId());
//						dummyComtradeDataDTO.setExportChnlId(expId);
						dummyComtradeDataDTO.setDfrId(dfrDto.getDfrId());
						dummyComtradeDataDTO.setRecordingDevId(stationDetail.getSystemRecordingDeviceId());
//						channelInfoBuffer = new StringBuffer(expId +M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer = new StringBuffer();
						channelInfoBuffer.append(expId+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(analogChnl.getPhase()+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(analogChnl.getName()+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(analogChnl.getInputType()+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(M9kConstants.COMTRADE_CHNL_MULT+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(M9kConstants.COMTRADE_CHNL_OFFSET+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_CHNL_SKEW+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MIN+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MAX+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(M9kConstants.COMTRADE_SECONDARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY);
						channelInfoBuffer.append(M9kStationConstants.NEWLINE);
						dummyComtradeDataDTO.setAnalogs(channelInfoBuffer);
						dummyComtradeDataDTO.setSampleRate(comtradeDataDTO.getSampleRate());
						dummyComtradeDataDTO.setSampleCnt(finalMergedSampleCnt);
						dummyComtradeDataDTO.setType(M9kStationConstants.RMS);
						isData = constructDummyContinuousData(null);
						dummyComtradeDataDTO.setBinaryDataSteam(isData);
						dummyComtradeDataDTO.setTsPrefault(comtradeDataDTO.getTsPrefault());
						dummyComtradeDataDTO.setTsTrigger(comtradeDataDTO.getTsTrigger());
//							lstComtradeDataDtos.add(dummyComtradeDataDTO);
						mapComtradeContDat.put(expId, dummyComtradeDataDTO);
					}

				}
			}
			else if (dfrDto.getAnalogChnlCnt() > 0) // THere can be scenario where DFR is up and no data exported (e.g. user hasn't exported or a CLUTCH command from M9kDebug)
			{
		    	for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
					AnalogChannelDTO analogChnl  =  iterator1.next();
//					logger.debug("DFR ID DFR"+analogChnl.isExportStatus());
					expId = Integer.parseInt(analogChnl.getChannel());
					if (getLstMissingExportsId().contains(expId))
					{
						logger.debug("LTR-RMS-DEBUG-DUMMY sample Count "+finalMergedSampleCnt+" for MISSING export id "+expId);

						if (mapComtradeContDat.get(expId) == null)
						{
//							logger.debug("LTR-RMS-DEBUG-DUMMY: DFR is active but no data for expId "+expId);
							dummyComtradeDataDTO = new ComtradeDataDTO();
							dummyComtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
							dummyComtradeDataDTO.setStationId(stationDetail.getSystemStationId());
//							dummyComtradeDataDTO.setExportChnlId(expId);
							dummyComtradeDataDTO.setDfrId(dfrDto.getDfrId());
//							channelInfoBuffer = new StringBuffer(expId +M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer = new StringBuffer();
							channelInfoBuffer.append(expId+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(analogChnl.getPhase()+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(analogChnl.getName()+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(analogChnl.getInputType()+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(M9kConstants.COMTRADE_CHNL_MULT+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(M9kConstants.COMTRADE_CHNL_OFFSET+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_CHNL_SKEW+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MIN+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MAX+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(M9kConstants.COMTRADE_SECONDARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY);
							channelInfoBuffer.append(M9kStationConstants.NEWLINE);
							dummyComtradeDataDTO.setAnalogs(channelInfoBuffer);
							dummyComtradeDataDTO.setSampleRate(comtradeDataDTO.getSampleRate());
							dummyComtradeDataDTO.setSampleCnt(finalMergedSampleCnt);
							dummyComtradeDataDTO.setType(M9kStationConstants.RMS);
							isData = constructDummyContinuousData(null);
							dummyComtradeDataDTO.setBinaryDataSteam(isData);
							dummyComtradeDataDTO.setTsPrefault(comtradeDataDTO.getTsPrefault());
							dummyComtradeDataDTO.setTsTrigger(comtradeDataDTO.getTsTrigger());
//							lstComtradeDataDtos.add(dummyComtradeDataDTO);
							mapComtradeContDat.put(expId, dummyComtradeDataDTO);
						}

					}
					
//					// If the data is missing at the end for any dfrs
//						if (mapComtradeContDat.get(expId) != null && mapComtradeContDat.get(expId).getSampleCnt() < finalMergedSampleCnt)
//						{
//							logger.debug("LTR-RMS-DEBUG-DUMMY: Data missing at the end for expId "+expId+" existing sample count "+mapComtradeContDat.get(expId).getSampleCnt());
//							dummyComtradeDataDTO = mapComtradeContDat.get(expId);
//							isData = constructDummyContinuousData(dummyComtradeDataDTO);
//							dummyComtradeDataDTO.setBinaryDataSteam(isData);
//							dummyComtradeDataDTO.setSampleCnt(finalMergedSampleCnt);
//						}
				}

			}
    	}
	}

	public int getExportBufferTime() {
		return exportBufferTime;
	}

	public void setExportBufferTime(int exportBufferTime) {
		this.exportBufferTime = exportBufferTime;
	}


	public long getLtrStartTime() {
		return ltrStartTime;
	}


	public void setLtrStartTime(long ltrStartTime) {
		this.ltrStartTime = ltrStartTime;
	}


	public long getLtrStopTime() {
		return ltrStopTime;
	}


	public void setLtrStopTime(long ltrStopTime) {
		this.ltrStopTime = ltrStopTime;
	}

	private InputStream constructContinuousData(InputStream is, double sampleRate, int fromSampleNo, int endSampleNo, float scaleFactor) throws M9000Exception
	{
//		PrintWriter pwShort = null;
//		PrintWriter pwTstScopeData = null;
		DataInputStream dis = new DataInputStream(is);
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		
		int totalSamplesToRead = endSampleNo - fromSampleNo+1;
//		StringBuffer data = new StringBuffer();
		float sampleData;
		short shortDataVal;
		
//		int skipCnt = 0;
//TODO: calculate micro seconds for data prefix	
		try {
		if (fromSampleNo > 1)
		{
			dis.skipBytes((fromSampleNo - 1)*M9kStationConstants.SIZE_OF_FLOAT);
		}
//		logger.debug("LTR-RMS-DEBUG: ExpId "+expCnt);
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

			sampleData = dis.readFloat();
//			logger.debug("Sample data read as float "+sampleData);
//			System.out.println(calculatedValue);
			shortDataVal = (short) (sampleData * scaleFactor);
//			if (sampleData <= 0 || shortDataVal == -32678)
//			{
//				logger.debug("LTR-RMS-DEBUG: sample data "+sampleData+ " scale factor "+scaleFactor+" converted short "+shortDataVal);
//			}
//			if (expCnt == 8 || expCnt == 24 || expCnt == 41 || expCnt == 73)
//			{
//				pwShort.print(shortDataVal);
//				pwShort.println();
//				
//				pwTstScopeData.print(sampleData);
//				pwTstScopeData.println();
//
//			}
//			logger.debug("sample data copnverted to short "+ shortDataVal);
//			data.append(sampleData+M9kConstants.NEWLINE);
			dos.writeShort(shortDataVal);
//			skipCnt++;
			totalSamplesToRead--;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new M9000Exception("Failed to construct rms data",e);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new M9000Exception("Failed to construct rms data",e);
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
		logger.debug("LTR-RMS-DEBUG: Merged actual data and  byte array size "+bais.size());
		return new ByteArrayInputStream(bais.toByteArray());

	}

	// If the inactive dfr has few data in the start then the dummy data has to be appended to the existing data. If there is no data the first parameter will be NULL 
	private InputStream constructDummyContinuousData(ComtradeDataDTO dummyComtradeDto)
	{
//		StringBuffer data = new StringBuffer();
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		Integer iDummyHexData = 0x8000;
		
		int i = 0;
		try {
			
		if (dummyComtradeDto != null)
		{
			i = dummyComtradeDto.getSampleCnt();
//			logger.debug("LTR-RMS-DEBUG-DUMMY: Sample cnt of few available data of inactive dfr "+i);
		}
		for (; i < finalMergedSampleCnt; i++)
				
		{
			dos.writeShort(iDummyHexData);
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		logger.debug("LTR-RMS-DEBUG-DUMMY:Constructed dummy for sampleCount "+finalMergedSampleCnt+" byte array size "+bais.size());
		return new ByteArrayInputStream(bais.toByteArray());

	}

	private InputStream constructDummyContinuousData(int totalSamplesMissing)
	{
//		StringBuffer data = new StringBuffer();
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		Integer iDummyHexData = 0x8000;
		
		try {
			
//		logger.debug("LTR-RMS-DEBUG-DUMMY: Sample cnt of those missing "+totalSamplesMissing);
		for (int i = 0; i < totalSamplesMissing; i++)
				
		{
			dos.writeShort(iDummyHexData);
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		logger.debug("LTR-RMS-DEBUG-DUMMY: Constructed dummy for sampleCount "+totalSamplesMissing+" byte array size "+bais.size());
		return new ByteArrayInputStream(bais.toByteArray());

	}
	// Used for Testing
	@SuppressWarnings("unused")
	private void prepareAndUpdateLongTermData()
	{
		PreparedStatement ps = null;
		ResultSet rs= null;
		int i = 0;
		
		// query to select the matching rows
		String sqlQuery = "insert into long_term_dat (expId, name, phase, units, firstSampleTime, lastSampleTime, sampleRate, sampleCnt, scale, offset, startSampleNo, endSampleNo, data) SELECT expId, name, phase, units, cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) as StartSampleTime ,tsLast, sampleRate, sampleCnt, scale, offset, if ((@tStart := round((sampleCnt -  ((tsLast - ?) /1000000.0) * sampleRate))) < 0, 0, @tStart) as startSampleNo,"
				+ "if((@tstop := round((sampleCnt - ((tsLast - ?) / 1000000) * sampleRate )) ) < 0,sampleCnt, @tstop) as endSampleNo, data FROM `m9000`.`contAnalog` where (((? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) "
				+ "and ? <= tsLast)  or (? >= cast(tsLast -(((sampleCnt - 1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast ) ) or ((tsLast>= ? and (? >= cast((tsLast -(((sampleCnt - 1)/sampleRate) * 1000000)) as SIGNED)) and ? >= tsLast )));";
		
//		String sqlQuery = "insert into long_term_dat SELECT expId, name, phase, units, tsLast, sampleRate, sampleCnt, scale, offset, if ((@start := (sampleCnt-(tsLast - ?))  * sampleRate) >= 0, @start, 0) as startSampleNo, " +
//		"(sampleCnt-(tsLast - ?))  * sampleRate)) as endSampleNo,data FROM `m9000`.`contAnalog` " +
//		"where (((? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast)  or (? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast ) ) " +
//		"or ((tsLast>= ? and ? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? >= tsLast )))";
		try {
			logger.debug("DB-POOL New connection from prepareAndUpdateLongTermData ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Start time "+ltrStartTime+" Stop time "+ltrStopTime );
			logger.debug("SQL query to be executed "+ sqlQuery);
			ps = mysqlConn.prepareStatement(sqlQuery);
			ps.setLong(++i, ltrStartTime);
			ps.setLong(++i, ltrStopTime);
			ps.setLong(++i, ltrStartTime);
			ps.setLong(++i, ltrStartTime);
			ps.setLong(++i, ltrStopTime);
			ps.setLong(++i, ltrStopTime);
			ps.setLong(++i, ltrStartTime);
			ps.setLong(++i, ltrStopTime);
			ps.setLong(++i, ltrStopTime);
			int rowsInserted = ps.executeUpdate();
			if (rowsInserted <= 0)
			{
				logger.warn("No long term records found for start time "+ltrStartTime + " and stop time "+ ltrStopTime);
			}
			logger.debug("Total number of records inserted as part of long term "+rowsInserted);
			
		} catch (SQLException e) {
			logger.error("Error occured during prepareAndUpdateLongTermData  ", e);
		} catch (Exception e) {
			logger.error("Error occured during prepareAndUpdateLongTermData  ", e);
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
					logger.debug("DB-POOL closing connection from prepareAndUpdateLongTermData ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new M9000Exception("Failed to construct rms data",e);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new M9000Exception("Failed to construct rms data",e);
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

	// If the inactive dfr has few data in the start then the dummy data has to be appended to the existing data. If there is no data the first parameter will be NULL 
	private InputStream[] constructDummyContinuousComplexData(ComtradeDataDTO dummyComtradeDto)
	{
		InputStream[] dummyStreamsArray = new InputStream[2];
//		StringBuffer data = new StringBuffer();
		ByteArrayOutputStream baisMagnitude = new ByteArrayOutputStream();
		DataOutputStream dosMagnitude = new DataOutputStream(baisMagnitude);
		
		ByteArrayOutputStream baisPhase = new ByteArrayOutputStream();
		DataOutputStream dosPhase = new DataOutputStream(baisPhase);
		Integer iDummyHexData = 0x8000;
		
		int i = 0;
		try {
			
		if (dummyComtradeDto != null)
		{
			i = dummyComtradeDto.getSampleCnt();
//			logger.debug("LTR-RMS-DEBUG-DUMMY: Sample cnt of few available data of inactive dfr "+i);
		}
		for (; i < finalMergedSampleCnt; i++)
				
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

		logger.debug("LTR-RMS-DEBUG-DUMMY:Constructed dummy for sampleCount "+finalMergedSampleCnt+" dummy expid  "+dummyComtradeDto.getExportId()+" sample cnt "+dummyComtradeDto.getSampleCnt());
		return dummyStreamsArray;

	}

	private void createDummyComplexDataForMissingExports() {
		ComtradeDataDTO dummyComtradeDataDTO = null;
		InputStream[] isData = null;
		int expId;
		try
		{
			for (Iterator<Integer> iteratorExpIds = mapComtradeContMagnitudeDat.keySet().iterator(); iteratorExpIds.hasNext();) {
				expId = iteratorExpIds.next();
				// If the data is missing at the end for any dfrs
				if (mapComtradeContMagnitudeDat.get(expId).getSampleCnt() < finalMergedSampleCnt)
				{
					logger.debug("LTR-RMS-DEBUG-DUMMY: Data missing at the end for expId "+expId+" existing sample count "+mapComtradeContMagnitudeDat.get(expId).getSampleCnt());
					dummyComtradeDataDTO = mapComtradeContMagnitudeDat.get(expId);
					isData = constructDummyContinuousComplexData(dummyComtradeDataDTO);
					mapComtradeContMagnitudeDat.get(expId).setBinaryDataSteam(isData[0]);
					mapComtradeContPhaseDat.get(expId).setBinaryDataSteam(isData[1]);
					mapComtradeContMagnitudeDat.get(expId).setSampleCnt(finalMergedSampleCnt);
					mapComtradeContPhaseDat.get(expId).setSampleCnt(finalMergedSampleCnt);
//					isData[0].close();
//					isData[1].close();
//					isData = null;
				}
				
			}
		}
		catch(Exception e)
		{
			logger.error("Unable to create dummy for certain coplex data exports",e);
		}
		finally
		{
//			if (isData != null)
//			{
//				try {
//					isData[0].close();
//					isData[1].close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
		}
	}


	// Utility function to strip prefix and get the name e.g. 1-Analog1 will return Analog1
	private String getAnalogNameStrippingIndexPrefix(String analogName)
	{
		return analogName.substring(analogName.lastIndexOf("-")+1);
	}
	public Map<String, Boolean> getMapDfrsStatus() {
		return mapDfrsStatus;
	}

	public void setMapDfrsStatus(Map<String, Boolean> mapDfrsStatus) {
		this.mapDfrsStatus = mapDfrsStatus;
	}

	public List<Integer> getLstMissingExportsId() {
		return lstMissingExportsId;
	}

	public void setLstMissingExportsId(List<Integer> lstMissingExportsId) {
		this.lstMissingExportsId = lstMissingExportsId;
	}

	public Map<String, Double> getMapAnalogRanges() {
		return mapAnalogsRange;
	}

	public void setMapAnalogRanges(Map<String, Double> mapAnalogsRange) {
		this.mapAnalogsRange = mapAnalogsRange;
	}
	
	private Map<String, Double> getAnalogChnlsRanges()
    {
    	Map<String, Double> mapAnalogExports = new HashMap<String, Double>();
    	Double range;
    	Integer txRatio;
    	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
    		DfrDTO dfrDto = iterator.next();
			if (dfrDto.getAnalogChnlCnt() > 0)
			{
		    	for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
					AnalogChannelDTO analogChnl  =  iterator1.next();
					logger.debug("Cont-Data-Debug: Channel" +analogChnl.getChannel()+" - Range "+analogChnl.getRange());
					range = Double.parseDouble(analogChnl.getRange());
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

	private String getCommaSeperatedIds (List<Integer> lstExportIds)
	{
		StringBuffer strValue = new StringBuffer();
		int expId;
    	for (Iterator<Integer> iterator = lstExportIds.iterator(); iterator
				.hasNext();) {
    		expId = iterator.next().intValue();
			if (strValue.length() == 0)
			{
				strValue.append(expId);
			}
			else
			{
				strValue.append(","+expId);
			}
			
		}
		logger.debug("The search combination string returned "+strValue.toString());
		return strValue.toString();
	}

	
}
