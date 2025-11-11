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
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kConstants;


public class M9kLtrRmsFromDb implements Runnable {
	static boolean booStatus = true;
	private Connection mysqlConn;
	private long ltrStartTime = 0;
	private long ltrStopTime = 0;
	private int exportBufferTime;
	List<DfrDTO> lstDfrs = null;
//	List<AnalogChannelDTO> lstAnalogChannels;
//	Map<Integer, Boolean> mapAnalogChnlStatus;
	String sqlQuery;
//	private static int iCnt;
	private StationDTO stationDetail;
//	private static long ltrTimeLimit;
	int finalMergedSampleCnt = 0;
	List<Integer> lstMissingExportsId = new ArrayList<Integer>();
	Map<String, Integer> mapAnalogsRange = new HashMap<String, Integer>();

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kLtrRmsFromDb.class);
	static int expCnt;
	M9kCreateContinuousComtradeFilesFromDB comtradeData;
	Map<Integer, Map<String, Integer>> mapDfrRmsExports ;
	public M9kLtrRmsFromDb(long startTime, long stopTime)
	{
		logger.debug("Entered M9kLTRRMSProcesssTask constructor with start time "+startTime+" stop time "+stopTime);
		stationDetail = M9kStationDBUtil.getStationDetails();
//		ltrStartTime = startTime-(stationDetail.getPrefaultTime()*1000);
//		ltrStopTime = stopTime+(stationDetail.getPostfaultTime()*1000);
		ltrStartTime = startTime;
		ltrStopTime = stopTime;
		logger.debug("M9kLTRRMSProcesssTask: After prefault Time start time "+ltrStartTime+" stop time "+ltrStopTime);		
		exportBufferTime = M9kStationXMLUtil.getExportBufferTime();
//		lstAnalogChannels = M9kStationXMLUtil.getLstOfAnalogChannels();
		try {
			lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
			Collections.sort(lstDfrs, new Comparator<DfrDTO>() {

				@Override
				public int compare(DfrDTO o1, DfrDTO o2) {
//					return ((o1.getDfrId() < o2.getDfrId())?0:1);
					return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
				}
			});
			mapAnalogsRange = getAnalogChnlsRanges();
			comtradeData = new M9kCreateContinuousComtradeFilesFromDB(stationDetail.getSystemStationId());
			mapDfrRmsExports = M9kStationXMLUtil.getMapOfDfrRmsExports();
//			mapAnalogChnlStatus = getChannelsExportStatus();
		} catch (Exception e) {
			logger.error("Unable to process LTR ",e);
			return;
		}
	}
	public M9kLtrRmsFromDb(String startTime, String stopTime)
	{
		try
		{
			stationDetail = M9kStationDBUtil.getStationDetailsForId(4);
			logger.debug("Station Name "+stationDetail.getSystemStationName());

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			ltrStartTime = sdf.parse(startTime).getTime()*1000;
			ltrStopTime = sdf.parse(stopTime).getTime()*1000;
			M9kStationXMLUtil.initXml(stationDetail.getConfigXml());
			exportBufferTime = M9kStationXMLUtil.getExportBufferTime();
			
//			lstAnalogChannels = M9kStationXMLUtil.getLstOfAnalogChannels();
				lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
				Collections.sort(lstDfrs, new Comparator<DfrDTO>() {

					@Override
					public int compare(DfrDTO o1, DfrDTO o2) {
//						return ((o1.getDfrId() < o2.getDfrId())?0:1);
						return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
					}
				});
				mapAnalogsRange = getAnalogChnlsRanges();
				comtradeData = new M9kCreateContinuousComtradeFilesFromDB(stationDetail.getSystemStationId());
				mapDfrRmsExports = M9kStationXMLUtil.getMapOfDfrRmsExports();
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
		String sqlQuery = "SELECT recId, expId, name, phase, units, tsLast, sampleRate, sampleCnt, type, "
			+ "data FROM `m9000`.`cont` where (tsLast >= ? "
			+ "and tsLast <= ?) order by expId, tsLast";

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
			ps.setLong(++i, (ltrStopTime+(getExportBufferTime() * 1000000)));
			logger.debug("LTR-QUERY: param "+i+" - "+(ltrStopTime+(getExportBufferTime() * 1000000)));
//			ps.setInt(++i, M9kStationConstants.LTR_RMS_OFFSET);
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
//			int totalChannelCnt = 1;
			float scaleFactor = 0; // scale factor
			String analogChnlName;
			int dfrId;
//			int exportChnlId;
			Integer rmsExpId;
			while (rs.next())
			{
				dfrId = rs.getInt("recId");
				expId = rs.getInt("expId");
				
//				expId = expId - M9kStationConstants.LTR_RMS_OFFSET; // Removing the offset added to export Id during the rms export
				analogChnlName = getAnalogNameStrippingIndexPrefix(rs.getString("name"));
//				logger.debug("LTR-RMS: getMapAnalogRanges() "+getMapAnalogRanges()+ "Dfr Id "+dfrId+ "ExpId "+expId+" Name "+analogChnlName);
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
//				System.out.println("row cnt..."+rs.getRow());
				sampleCnt = rs.getInt("sampleCnt");
//				logger.debug("LTR-QUERY: Sample Cnt "+sampleCnt);
				sampleRate = rs.getDouble("sampleRate");
//				logger.debug("LTR-QUERY: sampleRate "+sampleRate);
				is = rs.getBinaryStream("data");
				lastSampleTime = rs.getLong("tsLast");
//				logger.debug("LTR-QUERY: lastSampleTime "+lastSampleTime);
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
//				logger.debug("LTR-QUERY: Start sample No "+startSampleNo);
				
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
//				logger.debug("LTR-QUERY: End sample No "+endSampleNo);
				
//				startSampleTime = rs.getLong("StartSampleTime");
//				logger.debug("LTR-QUERY: from DB startSampleTime"+startSampleTime);
				startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
//				logger.debug("LTR-QUERY: calculated startSampleTime"+startSampleTime);

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
						logger.debug("Missing data at the start");
						missingSampleCnt = Math.floor(((startSampleTime - ltrStartTime )/1000000)*sampleRate);
						isData = constructDummyContinuousData(missingSampleCnt.intValue());
						comtradeDataDTO.setBinaryDataSteam(isData);
					}
//					scaleFactor = (32767.0f / getMapAnalogRanges().get(expId));
//					exportChnlId =  Integer.parseInt(analogChnlName.substring(M9kStationConstants.ANALOG.length()));
					scaleFactor = (32767.0f / getMapAnalogRanges().get(analogChnlName));
					logger.debug("ExpId "+expId+" scale factor "+scaleFactor);
					comtradeDataDTO.setScaleFactor(scaleFactor);
//					logger.debug("\n\n\t\t Create cont data for expId "+expId);
					expCnt = expId;
					isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo, scaleFactor);
//					logger.debug("\n\n\t\t Done for expId "+expId);
					comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
					comtradeDataDTO.setStationId(stationDetail.getSystemStationId());
					logger.debug("\t\t\t\t\t\t Station Name "+stationDetail.getSystemStationName());
					comtradeDataDTO.setStationName(stationDetail.getSystemStationName());
					comtradeDataDTO.setDfrId(dfrId);
//					comtradeDataDTO.setExportChnlId(exportChnlId);
					// Commented as the sequence index, first column of cfg is added wgile creating cfg file
//					channelInfoBuffer = new StringBuffer(totalChannelCnt++ +M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer = new StringBuffer();
					channelInfoBuffer.append(expId+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("phase")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("name")+M9kStationConstants.COMMA_SEPERATOR);
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
					comtradeDataDTO.setTsPrefault(ltrStartTime);
					if ((ltrStopTime - ltrStartTime) > ((stationDetail.getSystemLtrPrefaultTime()*1000000) + (stationDetail.getSystemLtrPostfaultTime()*1000000)))
					{
						comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getSystemLtrPrefaultTime()*1000000));
					}
					else
					{
						comtradeDataDTO.setTsTrigger(ltrStartTime);
					}
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
//						System.out.println("LTR-QUERY:  StartSampleNo..."+startSampleNo+" End Sample No "+endSampleNo+" scale factor "+comtradeDataDTO.getScaleFactor());
//						logger.debug("\n\n\t\t else part Create cont data for expId "+expId);
						isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo,comtradeDataDTO.getScaleFactor());
//						logger.debug("\n\n\t\t else part create done for expId "+expId);
						 
						sampleCnt=(endSampleNo-startSampleNo+1)+comtradeDataDTO.getSampleCnt();
//						logger.debug("LTR-QUERY: sample count for expId "+expId+" is "+sampleCnt );
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
//							if (o1.getExportChnlId() < o2.getExportChnlId())
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
//						return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
						return returnVal;
					}
				});
				logger.debug("After sort "+lstComtradeDataDtos);
				comtradeData.createLTRFiles(lstComtradeDataDtos);
			}
			logger.debug("Size of analog cont..."+lstComtradeDataDtos.size());
			getLstMissingExportsId().clear();
			getMapAnalogRanges().clear();
			lstComtradeDataDtos.clear();
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
					logger.debug("LTR-RMS: Channel" +analogChnl.getChannel()+" - Range "+analogChnl.getRange());
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
//    	for (Iterator<Boolean> iterator = mapDFrs.values().iterator(); iterator.hasNext();) {
//    		Boolean isActive = iterator.next();
//			if (isActive)
//			{
//				activeCount++;
//			}
//		}
		logger.debug("LTR-RMS: Total analogs exported "+mapAnalogExports.size()+" actual values "+mapAnalogExports);
    	return mapAnalogExports;
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
						channelInfoBuffer = new StringBuffer(expId +M9kStationConstants.COMMA_SEPERATOR);
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
			}
			else if (dfrDto.getAnalogChnlCnt() > 0) // THere can be scenario where DFR is up and no data exported (e.g. user hasn't exported or a CLUTCH command from M9kDebug)
			{
		    	for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
					AnalogChannelDTO analogChnl  =  iterator1.next();
//					logger.debug("DFR ID DFR"+analogChnl.isExportStatus());
					expId = Integer.parseInt(analogChnl.getChannel());
					if (getLstMissingExportsId().contains(expId))
					{
//						logger.debug("LTR-DEBUG-DUMMY sample Count "+finalMergedSampleCnt+" for MISSING export id "+expId);

						if (mapComtradeContDat.get(expId) == null)
						{
//							logger.debug("LTR-DEBUG-DUMMY: DFR is active but no data for expId "+expId);
							dummyComtradeDataDTO = new ComtradeDataDTO();
							dummyComtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
							channelInfoBuffer = new StringBuffer(expId +M9kStationConstants.COMMA_SEPERATOR);
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
					
					// If the data is missing at the end for any dfrs
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

//	public Map<Integer, Boolean> getMapAnalogChnlStatus() {
//		return mapAnalogChnlStatus;
//	}
//
//	public void setMapAnalogChnlStatus(Map<Integer, Boolean> mapAnalogChnlStatus) {
//		this.mapAnalogChnlStatus = mapAnalogChnlStatus;
//	}

//	public int getExportedAnalogs() {
//		return exportedAnalogs;
//	}
//
//	public void setExportedAnalogs(int exportedAnalogs) {
//		this.exportedAnalogs = exportedAnalogs;
//	}

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

	public static void main(String args[])
	{
		M9kLtrRmsFromDb m9kLtrRmsFromDb = new M9kLtrRmsFromDb("2014-11-05 15:50:20", "2014-11-05 15:50:40");
		m9kLtrRmsFromDb.processLTR();
	}
}
