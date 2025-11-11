package com.usi.m9000.test;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.station.commands.M9kCreateContinuousComtradeFilesFromDB;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kConstants;


public class M9kLTRFromDb implements Runnable {
	static boolean booStatus = true;
	private Connection mysqlConn;
	private long ltrStartTime = 0;
	private long ltrStopTime = 0;
	private int exportAnalogBufferTime;
	List<DfrDTO> lstDfrs = null;
//	List<AnalogChannelDTO> lstAnalogChannels;
//	Map<Integer, Boolean> mapAnalogChnlStatus;
	String sqlQuery;
//	private static int iCnt;
	private StationDTO stationDetail;
//	private static long ltrTimeLimit;
	int finalMergedSampleCnt = 0;
	List<Integer> lstMissingExportsId = new ArrayList<Integer>();
	List<Integer> lstAnalogExportsId = new ArrayList<Integer>();
	
	M9kCreateContinuousComtradeFilesFromDB comtradeData;

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kLTRFromDb.class);
	public M9kLTRFromDb(long startTime, long stopTime)
	{
		logger.debug("Entered M9kLTRFromDb constructor with start time "+startTime+" stop time "+stopTime);
		stationDetail = M9kStationDBUtil.getStationDetails();
//		ltrStartTime = startTime-(stationDetail.getPrefaultTime()*1000);
//		ltrStopTime = stopTime+(stationDetail.getPostfaultTime()*1000);
		ltrStartTime = startTime;
		ltrStopTime = stopTime;

		logger.debug("M9kLTRFromDb: After prefault Time start time "+ltrStartTime+" stop time "+ltrStopTime);		
		exportAnalogBufferTime = M9kStationXMLUtil.getExportAnalogBufferTime();
		try {
			lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
			Collections.sort(lstDfrs, new Comparator<DfrDTO>() {

				@Override
				public int compare(DfrDTO o1, DfrDTO o2) {
//					return ((o1.getDfrId() < o2.getDfrId())?0:1);
					return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
				}
			});
			comtradeData = new M9kCreateContinuousComtradeFilesFromDB(stationDetail.getSystemStationId());
//			mapAnalogChnlStatus = getChannelsExportStatus();
		} catch (Exception e) {
			logger.error("Unable to process LTR ",e);
			return;
		}
	}
	
	public M9kLTRFromDb(String startTime, String stopTime)
	{
		try
		{
			logger.debug("Entered M9kLTRFromDb constructor with start time "+startTime+" stop time "+stopTime);
			stationDetail = M9kStationDBUtil.getStationDetailsForId(4);
	//		ltrStartTime = startTime-(stationDetail.getPrefaultTime()*1000);
	//		ltrStopTime = stopTime+(stationDetail.getPostfaultTime()*1000);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			ltrStartTime = sdf.parse(startTime).getTime()*1000;
			ltrStopTime = sdf.parse(stopTime).getTime()*1000;
			logger.debug("M9kLTRFromDb: After prefault Time start time "+ltrStartTime+" stop time "+ltrStopTime);		
			M9kStationXMLUtil.initXml(stationDetail.getConfigXml());
			exportAnalogBufferTime = M9kStationXMLUtil.getExportAnalogBufferTime();
			
			lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
			Collections.sort(lstDfrs, new Comparator<DfrDTO>() {

				@Override
				public int compare(DfrDTO o1, DfrDTO o2) {
//					return ((o1.getDfrId() < o2.getDfrId())?0:1);
					return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
				}
			});
			comtradeData = new M9kCreateContinuousComtradeFilesFromDB(stationDetail.getSystemStationId());
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
//			waitForDataAvailability();
			end = System.currentTimeMillis();
			logger.debug("Total wait time for LTR " + (end-start));
			System.out.println("Total wait time for LTR " + (end-start));
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
		ComtradeDataDTO comtradeDataDTO = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		Map<Integer,ComtradeDataDTO> mapComtradeContDat = new HashMap<Integer, ComtradeDataDTO>();
		double oscSampleCnt = getExportAnalogBufferTime() * stationDetail.getSystemSampleRate();
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
		String sqlQuery = "SELECT expId, name, phase, units, tsLast, sampleRate, sampleCnt, scale, offset, "
			+ "data FROM `m9000`.`contAnalog` where (tsLast >= ? "
			+ "and tsLast <= ?)order by expId, tsLast";

//		String sqlQuery = "insert into long_term_dat SELECT expId, name, phase, units, tsLast, sampleRate, sampleCnt, scale, offset, if ((@start := (sampleCnt-(tsLast - ?))  * sampleRate) >= 0, @start, 0) as startSampleNo, " +
//		"(sampleCnt-(tsLast - ?))  * sampleRate)) as endSampleNo,data FROM `m9000`.`contAnalog` " +
//		"where (((? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast)  or (? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast ) ) " +
//		"or ((tsLast>= ? and ? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? >= tsLast )))";
		try {
			logger.debug("DB-POOL New connection from processMergeAndInsertLongTermData ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("LTR-QUERY: Start time "+ltrStartTime+" Stop time "+ltrStopTime );
			logger.debug("LTR-QUERY: sampleTime.longValue() "+sampleTtimeVal);
			logger.debug("LTR-QUERY: SQL query to be executed "+ sqlQuery);
			ps = mysqlConn.prepareStatement(sqlQuery);
			ps.setLong(++i, (ltrStartTime));
			logger.debug("LTR-QUERY: param "+i+" - "+(ltrStartTime));
			ps.setLong(++i, (ltrStopTime+(getExportAnalogBufferTime() * 1000000)));
			logger.debug("LTR-QUERY: param "+i+" - "+(ltrStopTime+(getExportAnalogBufferTime() * 1000000)));
//			ps.setLong(++i, ltrStartTime+sampleTtimeVal);
//			logger.debug("LTR-QUERY: param "+i+" - "+(ltrStartTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStartTime);
//			logger.debug("LTR-QUERY: param "+i+" - "+(ltrStartTime));
//			ps.setLong(++i, ltrStopTime+sampleTtimeVal);
//			logger.debug("LTR-QUERY: param "+i+" - "+(ltrStopTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStopTime);
//			logger.debug("LTR-QUERY: param "+i+" - "+(ltrStopTime));
//			ps.setLong(++i, ltrStartTime);
//			logger.debug("LTR-QUERY: param "+i+" - "+(ltrStartTime));
//			ps.setLong(++i, ltrStopTime+sampleTtimeVal);
//			logger.debug("LTR-QUERY: param "+i+" - "+(ltrStopTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStopTime);
//			logger.debug("LTR-QUERY: param "+i+" - "+(ltrStopTime));
			rs = ps.executeQuery();

			InputStream is;
			StringBuffer channelInfoBuffer;
//			StringBuffer data;
			InputStream isData;
			double sampleRate;
			int sampleCnt = 0;
			int startSampleNo = -1;
			int endSampleNo = -1;
			int expId;
			long startSampleTime = 0;
			long lastSampleTime = 0;
			Double missingSampleCnt;
			int totalChannelCnt = 1;
			while (rs.next())
			{
//				System.out.println("row cnt..."+rs.getRow());
				sampleCnt = rs.getInt("sampleCnt");
				logger.debug("LTR-QUERY: Sample Cnt "+sampleCnt);
				sampleRate = rs.getDouble("sampleRate");
				logger.debug("LTR-QUERY: sampleRate "+sampleRate);
				expId = rs.getInt("expId");
				logger.debug("LTR-QUERY: expId "+expId);
				is = rs.getBinaryStream("data");
				lastSampleTime = rs.getLong("tsLast");
				logger.debug("LTR-QUERY: lastSampleTime "+lastSampleTime);
//				startSampleNo = rs.getInt("startSampleNo");
				if (lastSampleTime > ltrStartTime)
				{
					startSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - ltrStartTime)*sampleRate)/1000000);
					if (startSampleNo < 0)
					{
						startSampleNo = 1;
					}
				}
				else
				{
					startSampleNo = 1;
				}
				logger.debug("LTR-QUERY: Start sample No "+startSampleNo);
				
//				endSampleNo = rs.getInt("endSampleNo");
				if (lastSampleTime > ltrStopTime)
				{
					endSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - ltrStopTime)*sampleRate)/1000000);
					if (endSampleNo < 0)
					{
						endSampleNo = sampleCnt;
					}
				}
				else
				{
					endSampleNo = sampleCnt;
				}
				logger.debug("LTR-QUERY: End sample No "+endSampleNo);
				
//				startSampleTime = rs.getLong("StartSampleTime");
//				logger.debug("LTR-QUERY: from DB startSampleTime"+startSampleTime);
				startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
				logger.debug("LTR-QUERY: calculated startSampleTime"+startSampleTime);

				if (mapComtradeContDat.get(expId) == null)
				{
//					logger.debug("LTR-DEBUG: Start of the sample....for expId "+expId+" ->" + startSampleNo);
//					if (rs.isLast())
//					{
						sampleCnt=endSampleNo-startSampleNo+1;
//						logger.debug("LTR-DEBUG: Last row to be processed. End Sample..for expId "+expId+" ->" + endSampleNo+" Actual sampleCnt.."+sampleCnt);
//					}
//					iCnt = 1;
					comtradeDataDTO = new ComtradeDataDTO();
					comtradeDataDTO.setTsLast( lastSampleTime);
					if (ltrStartTime < startSampleTime) // Missing data at the start
					{
						// Construct dummy data for initial missing data
						missingSampleCnt = Math.floor(((startSampleTime - ltrStartTime )/1000000)*sampleRate);
						isData = constructDummyContinuousData(missingSampleCnt.intValue());
						comtradeDataDTO.setBinaryDataSteam(isData);
					}
					isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo);
					comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
					comtradeDataDTO.setStationId(stationDetail.getSystemStationId());
					comtradeDataDTO.setStationName(stationDetail.getSystemStationName());
//					channelInfoBuffer = new StringBuffer(totalChannelCnt++ +M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer = new StringBuffer();
					channelInfoBuffer.append(expId+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("phase")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("name")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("units")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getFloat("scale")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getFloat("offset")+M9kStationConstants.COMMA_SEPERATOR);
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
					comtradeDataDTO.setType(M9kStationConstants.ANALOG);
					comtradeDataDTO.setBinaryDataSteam(isData);
					isData.close();
					mapComtradeContDat.put(expId, comtradeDataDTO);
					comtradeDataDTO.setTsPrefault(ltrStartTime);
					comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getSystemPrefaultTime()*1000)+(stationDetail.getSystemLtrPrefaultTime()*1000000));
				}
				else
				{
					comtradeDataDTO = mapComtradeContDat.get(expId);
//					System.out.println("Comtrade Data DTO "+comtradeDataDTO);
//					logger.debug("LTR-DEBUG: start Time "+startSampleTime+" Last time of the previoud record "+comtradeDataDTO.getTsLast()+" previoud Last Time +1 "+ (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)));
					if (startSampleTime > (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)) ) // First sample should be Last sample + 1 of the previous record
					{
						missingSampleCnt = Math.floor(((startSampleTime - comtradeDataDTO.getTsLast() )/1000000)*sampleRate);
						isData = constructDummyContinuousData(missingSampleCnt.intValue());
						comtradeDataDTO.setBinaryDataSteam(isData);
						isData.close();
					}
					comtradeDataDTO.setTsLast(lastSampleTime);
//					startSampleNo = -1;
//					if (rs.isLast())
//					{
//						logger.debug("LTR-DEBUG: processing expId "+expId); 
//						endSampleNo = rs.getInt("endSampleNo");
						System.out.println("LTR-QUERY:  StartSampleNo..."+startSampleNo+" End Sample No "+endSampleNo);
						isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo);
						 
						sampleCnt=(endSampleNo-startSampleNo+1)+comtradeDataDTO.getSampleCnt();
						logger.debug("LTR-QUERY: sample count for expId "+expId+" is "+sampleCnt );
//					}
//					else
//					{
//						logger.debug("LTR-DEBUG: Still processing for expId "+expId); 
//						isData = constructContinuousData(is, sampleRate);
//						sampleCnt+=comtradeDataDTO.getSampleCnt()-1;
//						logger.debug("LTR-DEBUG: sample count as of now for expId "+expId+" is "+sampleCnt );
//					}
					
					comtradeDataDTO.setSampleCnt(sampleCnt);
//					comtradeDataDTO.setBinaryDataSteam(new SequenceInputStream(comtradeDataDTO.getBinaryDataSteam(), isData));
					comtradeDataDTO.setBinaryDataSteam(isData);
//					comtradeDataDTO.setTsPrefault(ltrStartTime);
//					comtradeDataDTO.setTsTrigger(ltrStartTime+stationDetail.getLtrPrefaultTime());
				}
				if (sampleCnt > finalMergedSampleCnt)
				{
					finalMergedSampleCnt = sampleCnt;
					logger.debug("LTr-DEBUG: finalMergedSampleCnt "+finalMergedSampleCnt);
				}
				is.close();
			}
			if(!mapComtradeContDat.isEmpty())
			{
//				createDummyDataForMissingExports(mapComtradeContDat);
				lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContDat.values());
//				insertIntoLtr(lstComtradeDataDtos);
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
							if (o1.getExportId() < o2.getExportId())
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
				comtradeData.createLTRFiles(lstComtradeDataDtos);
			}
			logger.debug("Size of analog cont..."+lstComtradeDataDtos.size());
			getLstMissingExportsId().clear();
			getLstAnalogExportsId().clear();
			lstComtradeDataDtos.clear();
			mapComtradeContDat.clear();
			
		} catch (SQLException e) {
			logger.debug("Error in fetching long term data ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.debug("Error in fetching long term data ", e);
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
				ps.setString(++i, M9kStationConstants.ANALOG);
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
		List<Integer> lstExpIds = new ArrayList<Integer>();
		PreparedStatement ps = null;
		ResultSet rs= null;
		boolean booContinue = true;
		logger.info("Active dfrs "+getLstAnalogExportsId().size());
		logger.debug("LTR stop time "+getLtrStopTime());
		logger.debug("LTR export analog buffer time "+getExportAnalogBufferTime());
		long thresholdTime = 0;
		try {
			thresholdTime = (getLtrStopTime()+ (getExportAnalogBufferTime()*1000000)*2) ;
			
			logger.debug("DB-POOL New connection from waitForDataAvailability ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			ps = mysqlConn.prepareStatement("select expId from contAnalog A where tsLast >= ?  group by expId");
			while (booContinue)
			{

				ps.setLong(1, (getLtrStopTime()+(getExportAnalogBufferTime() * 1000000)));
				rs = ps.executeQuery();
				while (rs.next())
				{
					lstExpIds.add(rs.getInt("expId"));
				}
				rs.close();
				logger.debug("LTR threshold time calculated "+thresholdTime+"(thresholdTime - M9kStationUtil.getIRIG()) "+(thresholdTime - M9kStationUtil.getIRIG()));
				logger.debug("total available exports "+lstExpIds.size());
				//TODO: Get all the channels that are exported and evaluate the count here.
				if (lstExpIds.size() == getLstAnalogExportsId().size() || (thresholdTime - M9kStationUtil.getIRIG()) <= 0)
					//			if ((thresholdTime - M9kStationUtil.getIRIG()) <= 0)
				{
					if (!lstExpIds.isEmpty() && lstExpIds.size() < getLstAnalogExportsId().size())
					{
						updateMissingExports(lstExpIds);
					}
					booContinue = false;
					break;
				}
				else
				{
					lstExpIds.clear();
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
	
	private void updateMissingExports(List<Integer> lstExporsId)
	{
		for (Iterator<Integer> iterator = getLstAnalogExportsId().iterator(); iterator.hasNext();) {
			Integer id =  iterator.next();
			logger.debug("LTR-DEBUG-DUMMY: Id from analog export xml "+id);
			if (!lstExporsId.contains(id))
			{
				logger.debug("Addded to missing list "+id);
				getLstMissingExportsId().add(id);
			}
			
		}
		
		logger.debug("LTR-DEBUG-DUMMY: Missing exports size.."+getLstMissingExportsId().size());
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

    private List<Integer> getExportedAnalogChnlsCount(Map<String, Boolean> mapDfrsStatus2)
    {
    	List<Integer> lstAnalogExports = new ArrayList<Integer>();
    	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
    		DfrDTO dfrDto = iterator.next();
			if (mapDfrsStatus2.get("DFR"+dfrDto.getDfrId()) && dfrDto.getAnalogChnlCnt() > 0)
			{
		    	for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
					AnalogChannelDTO analogChnl  =  iterator1.next();
					logger.debug("DFR ID DFR"+analogChnl.isExportStatus());
					if (analogChnl.isExportStatus())
					{
						lstAnalogExports.add(Integer.parseInt(analogChnl.getChannel()));
					}
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
		logger.debug("Total analogs exported "+lstAnalogExports.size());
    	return lstAnalogExports;
    }

	
	private void createDummyDataForMissingExports(
			Map<Integer, ComtradeDataDTO> mapComtradeContDat) {
		ComtradeDataDTO comtradeDataDTO = mapComtradeContDat.values().iterator().next();
		ComtradeDataDTO dummyComtradeDataDTO = null;
		InputStream isData;
		StringBuffer channelInfoBuffer;
		int expId;
		for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
    		DfrDTO dfrDto = iterator.next();
			if (dfrDto.getAnalogChnlCnt() > 0)
			{
	    		logger.debug("DFR"+dfrDto.getDfrId()+" is down and dummy data is created.");
		    	for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
					AnalogChannelDTO analogChnl  =  iterator1.next();
					logger.debug("DFR ID DFR"+analogChnl.isExportStatus());
					if (analogChnl.isExportStatus())
					{
						expId = Integer.parseInt(analogChnl.getChannel());
						logger.debug("LTR-DEBUG-DUMMY sample Count "+finalMergedSampleCnt+" for export id "+expId);

						if (mapComtradeContDat.get(expId) != null)
						{
							logger.debug("LTR-DEBUG-DUMMY: ExpId exists already "+expId);
							dummyComtradeDataDTO = mapComtradeContDat.get(expId);
							isData = constructDummyContinuousData(dummyComtradeDataDTO);
							dummyComtradeDataDTO.setBinaryDataSteam(isData);
							dummyComtradeDataDTO.setSampleCnt(finalMergedSampleCnt);
						}
						else
						{
							logger.debug("LTR-DEBUG-DUMMY: ExpId doesn't exists already "+expId);
							dummyComtradeDataDTO = new ComtradeDataDTO();
							dummyComtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
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
							dummyComtradeDataDTO.setType(M9kStationConstants.ANALOG);
							isData = constructDummyContinuousData(null);
							dummyComtradeDataDTO.setBinaryDataSteam(isData);
							dummyComtradeDataDTO.setTsPrefault(comtradeDataDTO.getTsPrefault());
							dummyComtradeDataDTO.setTsTrigger(comtradeDataDTO.getTsTrigger());
//							lstComtradeDataDtos.add(dummyComtradeDataDTO);
							mapComtradeContDat.put(expId, dummyComtradeDataDTO);
						}

					}
				}
			}
			else if (dfrDto.getAnalogChnlCnt() > 0) // THere can be scenario where DFR is up and no data exported (e.g. user hasn't exported or a CLUTCH command from M9kDebug)
			{
		    	for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
					AnalogChannelDTO analogChnl  =  iterator1.next();
//					logger.debug("DFR ID DFR"+analogChnl.isExportStatus());
					expId = Integer.parseInt(analogChnl.getChannel());
					if (analogChnl.isExportStatus() && getLstMissingExportsId().contains(expId))
					{
//						logger.debug("LTR-DEBUG-DUMMY sample Count "+finalMergedSampleCnt+" for MISSING export id "+expId);

						if (mapComtradeContDat.get(expId) == null)
						{
//							logger.debug("LTR-DEBUG-DUMMY: DFR is active but no data for expId "+expId);
							dummyComtradeDataDTO = new ComtradeDataDTO();
							dummyComtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
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
							dummyComtradeDataDTO.setType(M9kStationConstants.ASCII);
							isData = constructDummyContinuousData(null);
							dummyComtradeDataDTO.setBinaryDataSteam(isData);
							dummyComtradeDataDTO.setTsPrefault(comtradeDataDTO.getTsPrefault());
							dummyComtradeDataDTO.setTsTrigger(comtradeDataDTO.getTsTrigger());
//							lstComtradeDataDtos.add(dummyComtradeDataDTO);
							mapComtradeContDat.put(expId, dummyComtradeDataDTO);
						}

					}
					
					if (analogChnl.isExportStatus()) // If the data is missing at the end for any dfrs
					{
						if (mapComtradeContDat.get(expId) != null && mapComtradeContDat.get(expId).getSampleCnt() < finalMergedSampleCnt)
						{
//							logger.debug("LTR-DEBUG-DUMMY: Data missing at the end for expId "+expId);
							dummyComtradeDataDTO = mapComtradeContDat.get(expId);
							isData = constructDummyContinuousData(dummyComtradeDataDTO);
							dummyComtradeDataDTO.setBinaryDataSteam(isData);
							dummyComtradeDataDTO.setSampleCnt(finalMergedSampleCnt);
						}
					}
				}

			}
    	}
	}

	public int getExportAnalogBufferTime() {
		return exportAnalogBufferTime;
	}

	public void setExportAnalogBufferTime(int exportAnalogBufferTime) {
		this.exportAnalogBufferTime = exportAnalogBufferTime;
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

	private InputStream constructContinuousData(InputStream is, double sampleRate, int fromSampleNo, int endSampleNo)
	{
		DataInputStream dis = new DataInputStream(is);
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		
		int totalSamplesToRead = endSampleNo - fromSampleNo+1;
//		StringBuffer data = new StringBuffer();
		short sampleData;
		
//		double multFactor = 0.0001;
//		int skipCnt = 0;
//TODO: calculate micro seconds for data prefix	
		try {
		if (fromSampleNo > 0)
		{
			dis.skipBytes((fromSampleNo - 1)*M9kStationConstants.SIZE_OF_SHORT);
		}
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

			sampleData = dis.readShort();
//			System.out.println(calculatedValue);

//			data.append(sampleData+M9kConstants.NEWLINE);
			dos.writeShort(sampleData);
//			skipCnt++;
			totalSamplesToRead--;
			if (totalSamplesToRead == 0)
			{
				break;
			}

		}
		dis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
//		logger.debug("LTR-DEBUG: byte array size "+bais.size());
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
//			logger.debug("LTR-DEBUG-DUMMY: Sample cnt of few available data of inactive dfr "+i);
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
//		logger.debug("LTR-DEBUG-DUMMY: byte array size "+bais.size());
		return new ByteArrayInputStream(bais.toByteArray());

	}

	private InputStream constructDummyContinuousData(int totalSamplesMissing)
	{
//		StringBuffer data = new StringBuffer();
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		Integer iDummyHexData = 0x8000;
		
		try {
			
//		logger.debug("LTR-DEBUG-DUMMY: Sample cnt of those missing "+totalSamplesMissing);
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
//		logger.debug("LTR-DEBUG-DUMMY: byte array size "+bais.size());
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
		String sqlQuery = "insert into long_term_dat (expId, name, phase, units, firstSampleTime, lastSampleTime, sampleRate, sampleCnt, scale, offset, startSampleNo, endSampleNo, data) SELECT expId, name, phase, units, cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) as StartSampleTime ,tsLast, sampleRate, sampleCnt, scale, offset, if ((@tStart := round((sampleCnt -  ((tsLast - ?) /1000000) * sampleRate))) < 0, 0, @tStart) as startSampleNo,"
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
				logger.info("No long term records found for start time "+ltrStartTime + " and stop time "+ ltrStopTime);
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

	public List<Integer> getLstMissingExportsId() {
		return lstMissingExportsId;
	}

	public void setLstMissingExportsId(List<Integer> lstMissingExportsId) {
		this.lstMissingExportsId = lstMissingExportsId;
	}

	public List<Integer> getLstAnalogExportsId() {
		return lstAnalogExportsId;
	}

	public void setLstAnalogExportsId(List<Integer> lstAnalogExportsId) {
		this.lstAnalogExportsId = lstAnalogExportsId;
	}

	public static void main(String args[])
	{
		M9kLTRFromDb m9kLtrFromDb = new M9kLTRFromDb("2014-10-13 15:00:00", "2014-10-13 15:00:05");
		m9kLtrFromDb.processLTR();
	}
}
