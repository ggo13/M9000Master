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
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kConstants;


public class M9kLTRProcesssTask implements Runnable {
	static boolean booStatus = true;
	private Connection mysqlConn;
	private long ltrStartTime = 0;
	private long ltrStopTime = 0;
	private int exportAnalogBufferTime;
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
	List<Integer> lstAnalogExportsId = new ArrayList<Integer>();
	
	M9kCreateContinuousComtradeFilesFromDB comtradeData;

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kLTRProcesssTask.class);
	public M9kLTRProcesssTask(long startTime, long stopTime)
	{
		logger.debug("Entered M9kLTRProcesssTask constructor with start time "+startTime+" stop time "+stopTime);
		stationDetail = M9kStationDBUtil.getStationDetails();
//		ltrStartTime = startTime-(stationDetail.getSystemSystemLtrPrefaultTime()*1000);
//		ltrStopTime = stopTime+(stationDetail.getSystemSystemLtrPostfaultTime()*1000);
		ltrStartTime = startTime;
		ltrStopTime = stopTime;
		logger.debug("M9kLTRProcesssTask: After prefault Time start time "+ltrStartTime+" stop time "+ltrStopTime);		
		exportAnalogBufferTime = M9kStationXMLUtil.getExportAnalogBufferTime();
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
			lstAnalogExportsId = getExportedAnalogChnlsCount(mapDfrsStatus);
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
		ComtradeDataDTO comtradeDataDTO = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		Map<Integer,ComtradeDataDTO> mapComtradeContDat = null;
		Map<Integer,Map<Integer,ComtradeDataDTO>> mapDfrComtradeContDat = new HashMap<Integer, Map<Integer,ComtradeDataDTO>>();
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
		String sqlQuery = "SELECT recId,expId, name, phase, units, tsLast, sampleRate, sampleCnt, scale, offset, "
			+ "data FROM `m9000`.`contAnalog` where (tsLast >= ? "
			+ "and tsLast <= ?)  order by expId, tsLast";

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
			ps.setLong(++i, (ltrStartTime-(getExportAnalogBufferTime() * 1000000))); 
			logger.debug("LTR-QUERY: param "+i+" - "+(ltrStartTime-(getExportAnalogBufferTime() * 1000000)));
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
			int dfrId;
			double startSampleTime = 0;
			long lastSampleTime = 0;
			Long missingSampleCnt;
			double tus;
			double tusDivBy2;
			// START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
			AnalogChannelDTO analogChannelDto;
			// END: 16-Jul-2020
			while (rs.next())
			{
//				System.out.println("row cnt..."+rs.getRow());
				sampleCnt = rs.getInt("sampleCnt");
				logger.debug("LTR-QUERY: Sample Cnt "+sampleCnt);
				sampleRate = rs.getDouble("sampleRate");
				logger.debug("LTR-QUERY: sampleRate "+sampleRate);
				dfrId= rs.getInt("recId");
				logger.debug("LTR-QUERY: dfrId "+dfrId);
				expId = rs.getInt("expId");
				logger.debug("LTR-QUERY: expId "+expId);
				is = rs.getBinaryStream("data");
				lastSampleTime = rs.getLong("tsLast");
				logger.debug("LTR-QUERY: lastSampleTime "+lastSampleTime);
//				startSampleNo = rs.getInt("startSampleNo");
				// START: 04-Dec-2019 Todd's logic
				tus = 1e6/sampleRate;
				tusDivBy2 = tus / 2;

//				startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
//				logger.debug("LTR-QUERY: calculated startSampleTime"+startSampleTime);
//				
//				if (ltrStartTime > (lastSampleTime+((1/sampleRate) * 1000000)) || ltrStopTime < startSampleTime ) // OUt of range records
//				{
//					logger.debug("Skipping the current record with tsLast "+lastSampleTime+" It is less than ltrStartTime "+ ltrStartTime);
//					continue;
//				}
//				
//				if (ltrStartTime >= lastSampleTime) // ltrStartTime falls between tsLast and next tsStart
//				{
//					startSampleNo = sampleCnt;
//				}
//				else
//				if (ltrStartTime < startSampleTime)
//				{
//					startSampleNo = 1;
//				}
////				if (lastSampleTime > ltrStartTime)
//				else
//				{
//					startSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - ltrStartTime)*sampleRate)/1000000);
//					if (startSampleNo < 0)
//					{
//						startSampleNo = 1;
//					}
//				}
////				else
////				{
////					startSampleNo = 1;
////				}
//				logger.debug("LTR-QUERY: Start sample No "+startSampleNo);
//				
////				endSampleNo = rs.getInt("endSampleNo");
//				if (ltrStopTime > lastSampleTime) // Not the last record end sample no always last
//				{
//					endSampleNo = sampleCnt;
//				}
////				if (lastSampleTime > ltrStopTime)
//				else
//				{
//					endSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - ltrStopTime)*sampleRate)/1000000);
//					if (endSampleNo < 0)
//					{
//						endSampleNo = sampleCnt;
//					}
//				}
////				else
////				{
////					endSampleNo = sampleCnt;
////				}
//				logger.debug("LTR-QUERY: End sample No "+endSampleNo);
				
//				startSampleTime = rs.getLong("StartSampleTime");
//				logger.debug("LTR-QUERY: from DB startSampleTime"+startSampleTime);
				
				startSampleTime = (lastSampleTime - (sampleCnt-1) * tus);
				if(ltrStopTime < (startSampleTime - tusDivBy2) || ltrStartTime > (lastSampleTime + tusDivBy2))
				{
					logger.debug("TODD: contAnalog - Skipping the blob...");
					continue;
				}
				
				startSampleNo = M9kStationUtil.toIndex(ltrStartTime - startSampleTime, tus, sampleCnt);
				logger.debug("TODD: Start sample no "+startSampleNo);
				endSampleNo = M9kStationUtil.toIndex(ltrStopTime - startSampleTime, tus, sampleCnt);
				logger.debug("TODD: End sample no "+endSampleNo);
				// END: 04-Dec-2019 
				
				mapComtradeContDat = mapDfrComtradeContDat.get(dfrId);
				if (mapComtradeContDat == null)
				{
					mapComtradeContDat = new HashMap<Integer, ComtradeDataDTO>();
					mapDfrComtradeContDat.put(dfrId,mapComtradeContDat);
				}
				// START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
				logger.debug("LTR: about to invoke getAnalogChannelForDfr with dfrId "+dfrId+" with expId "+expId);
				analogChannelDto = getAnalogChannelForDfr(dfrId,expId);
				// END: 16-Jul-2020
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
						missingSampleCnt = Math.round((((startSampleTime - ltrStartTime )/1000000.0)*sampleRate));
						if (missingSampleCnt == 0)
						{
							logger.debug("Missing sample count seems to be zero "+missingSampleCnt+" before round of value "+(((startSampleTime - ltrStartTime )/1000000.0)*sampleRate));
							missingSampleCnt = 1L;
						}
						logger.debug("Missing sample count calcuated "+missingSampleCnt);
						isData = constructDummyContinuousData(missingSampleCnt.intValue());
						comtradeDataDTO.setBinaryDataSteam(isData);
					}
					isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo);
					comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
					comtradeDataDTO.setStationId(stationDetail.getSystemStationId());
					comtradeDataDTO.setStationName(stationDetail.getSystemStationName());
					comtradeDataDTO.setRecordingDevId(stationDetail.getSystemRecordingDeviceId());
					// 21-May-2013: Commented as the sequence index, first column of cfg is added wgile creating cfg file
//					channelInfoBuffer = new StringBuffer(totalChannelCnt++ +M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer = new StringBuffer();
					// START: 18-Mar-2018 Prefix A or VIRTUAL_ANALOG prefix in cfg files
					if (expId <= M9kStationXMLUtil.getSystem().getAnalogsCount())
					{
						channelInfoBuffer.append("A"+expId+M9kStationConstants.COMMA_SEPERATOR);
					}
					else
					{
						channelInfoBuffer.append(M9kStationConstants.VIRTUAL_PREFIX+expId+M9kStationConstants.COMMA_SEPERATOR);
					}
					
//					channelInfoBuffer.append(expId+M9kStationConstants.COMMA_SEPERATOR);
					
					// END: 18-Mar-2018 Prefix A or VIRTUAL_ANALOG prefix in cfg files

					channelInfoBuffer.append(rs.getString("phase")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("name")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("units")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getFloat("scale")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getFloat("offset")+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_CHNL_SKEW+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MIN+M9kStationConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MAX+M9kStationConstants.COMMA_SEPERATOR);
					// START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
					comtradeDataDTO.setDfrId(dfrId); // Help in sorting in case of virtual
					if (analogChannelDto == null)
					{
						channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(M9kConstants.COMTRADE_SECONDARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
					}
					else
					{
						channelInfoBuffer.append(analogChannelDto.getPrimaryRatio()+M9kStationConstants.COMMA_SEPERATOR);
						channelInfoBuffer.append(analogChannelDto.getSecondaryRatio()+M9kStationConstants.COMMA_SEPERATOR);
					}
					// END: 15-Jul-2020
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
//					comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getSystemPrefaultTime()*1000)+(stationDetail.getSystemLtrPrefaultTime()*1000000));
					comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getSystemLtrPrefaultTime()*1000000));
				}
				else
				{
					comtradeDataDTO = mapComtradeContDat.get(expId);
//					System.out.println("Comtrade Data DTO "+comtradeDataDTO);
					logger.debug("LTR-DEBUG: start Time "+startSampleTime+" Last time of the previoud record "+comtradeDataDTO.getTsLast()+" previoud Last Time +1 "+ (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)));
					if (startSampleTime > (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)) ) // First sample should be Last sample + 1 of the previous record
					{
						missingSampleCnt = Math.round((((startSampleTime - comtradeDataDTO.getTsLast() )/1000000.0)*sampleRate));
						if (missingSampleCnt == 0)
						{
							logger.debug("Missing sample count in the middle seems to be zero "+missingSampleCnt+" before round of value "+(((startSampleTime - comtradeDataDTO.getTsLast() )/1000000.0)*sampleRate));
							missingSampleCnt = 1L;
						}
						logger.debug("LTR-DEBUG: Missing sample count in between "+missingSampleCnt);
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
						logger.debug("LTR-QUERY:  StartSampleNo..."+startSampleNo+" End Sample No "+endSampleNo);
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
//					comtradeDataDTO.setTsTrigger(ltrStartTime+stationDetail.getSystemLtrPrefaultTime());
				}
				if (sampleCnt > finalMergedSampleCnt)
				{
					finalMergedSampleCnt = sampleCnt;
					logger.debug("LTr-DEBUG: finalMergedSampleCnt "+finalMergedSampleCnt);
				}
				is.close();
			}
			if(!mapDfrComtradeContDat.isEmpty())
			{
				createDummyDataForMissingExports(mapDfrComtradeContDat);
				lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
				for (Iterator<Integer> iterator = mapDfrComtradeContDat.keySet().iterator(); iterator
						.hasNext();) {
					dfrId = iterator.next();
					lstComtradeDataDtos.addAll(mapDfrComtradeContDat.get(dfrId).values());
				}
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

				logger.debug("Size of analog cont..."+lstComtradeDataDtos.size());

				comtradeData.createLTRFiles(lstComtradeDataDtos);
			}
			getLstMissingExportsId().clear();
			getLstAnalogExportsId().clear();
			lstComtradeDataDtos.clear();
			if (mapComtradeContDat != null && !mapComtradeContDat.isEmpty())
			{
				mapComtradeContDat.clear();
			}
			
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
		logger.debug("Active dfrs "+getLstAnalogExportsId().size());
		logger.debug("LTR stop time "+getLtrStopTime()+" IRIG time "+M9kStationUtil.getIRIG());
		logger.debug("LTR export analog buffer time "+getExportAnalogBufferTime()+" LTR prefault time "+stationDetail.getSystemLtrPrefaultTime());
		long thresholdTime = 0;
		try {
			
			// Instead of exportAnalogBufferTime seconds from ltr stop time, start waiting exportAnalogBufferTime secoonds from current irig time
			thresholdTime = (getLtrStopTime()+ (stationDetail.getSystemLtrPrefaultTime() * 1000000) + (getExportAnalogBufferTime()*1000000)*2) ;
			
			logger.debug("DB-POOL New connection from waitForDataAvailability . Threshold Time "+thresholdTime);
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			ps = mysqlConn.prepareStatement("select expId from contAnalog A where tsLast >= ?  group by recId,expId");
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
//				logger.debug("Closing the connection... "+mysqlConn);
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
		logger.debug("Total analogs exported "+lstAnalogExports.size()+" values "+lstAnalogExports);
    	return lstAnalogExports;
    }

	
	private void createDummyDataForMissingExports(
			Map<Integer, Map<Integer, ComtradeDataDTO>> mapDfrComtradeContDat) {
		
		Map<Integer, ComtradeDataDTO> mapComtradeContDat;
		ComtradeDataDTO dummyComtradeDataDTO = null;
		InputStream isData;
		StringBuffer channelInfoBuffer;
		int expId;

		for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
    		DfrDTO dfrDto = iterator.next();
    		mapComtradeContDat = mapDfrComtradeContDat.get(dfrDto.getDfrId());
			if (mapComtradeContDat == null)
			{
				mapComtradeContDat = new HashMap<Integer, ComtradeDataDTO>();
				mapDfrComtradeContDat.put(dfrDto.getDfrId(), mapComtradeContDat);
			}
			if (!getMapDfrsStatus().get("DFR"+dfrDto.getDfrId()) && dfrDto.getAnalogChnlCnt() > 0)
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
							dummyComtradeDataDTO.setStationId(stationDetail.getSystemStationId());
							dummyComtradeDataDTO.setRecordingDevId(stationDetail.getSystemRecordingDeviceId());
							// START: 13-Nov-2020 - Missing name and dfr id set
							dummyComtradeDataDTO.setStationName(stationDetail.getSystemStationName());
							dummyComtradeDataDTO.setDfrId(dfrDto.getDfrId());
							// END: 13-Nov-2020
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
							// START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
//							channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
//							channelInfoBuffer.append(M9kConstants.COMTRADE_SECONDARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(analogChnl.getPrimaryRatio()+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(analogChnl.getSecondaryRatio()+M9kStationConstants.COMMA_SEPERATOR);
							// END: 15-Jul-2020

							channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY);
							channelInfoBuffer.append(M9kStationConstants.NEWLINE);
							dummyComtradeDataDTO.setAnalogs(channelInfoBuffer);
							// START: 13-Nov-2020 - Long term sample rate to be set instead
//							dummyComtradeDataDTO.setSampleRate(dfrDto.getSampleRate());
							dummyComtradeDataDTO.setSampleRate(stationDetail.getSystemLongTermSampleRate());
							// END: 13-Nov-2020
							dummyComtradeDataDTO.setSampleCnt(finalMergedSampleCnt);
							dummyComtradeDataDTO.setType(M9kStationConstants.ANALOG);
							isData = constructDummyContinuousData(null);
							dummyComtradeDataDTO.setBinaryDataSteam(isData);
							dummyComtradeDataDTO.setTsPrefault(ltrStartTime);
							dummyComtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getSystemLtrPrefaultTime()*1000000));
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

						if (mapDfrComtradeContDat.get(expId) == null)
						{
//							logger.debug("LTR-DEBUG-DUMMY: DFR is active but no data for expId "+expId);
							dummyComtradeDataDTO = new ComtradeDataDTO();
							dummyComtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
							dummyComtradeDataDTO.setStationId(stationDetail.getSystemStationId());
							dummyComtradeDataDTO.setRecordingDevId(stationDetail.getSystemRecordingDeviceId());
							// START: 13-Nov-2020 - Missing name and dfr id set
							dummyComtradeDataDTO.setStationName(stationDetail.getSystemStationName());
							dummyComtradeDataDTO.setDfrId(dfrDto.getDfrId());
							// END: 13-Nov-2020
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
							// START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
//							channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
//							channelInfoBuffer.append(M9kConstants.COMTRADE_SECONDARY_RATIO+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(analogChnl.getPrimaryRatio()+M9kStationConstants.COMMA_SEPERATOR);
							channelInfoBuffer.append(analogChnl.getSecondaryRatio()+M9kStationConstants.COMMA_SEPERATOR);
							// END: 15-Jul-2020

							channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY);
							channelInfoBuffer.append(M9kStationConstants.NEWLINE);
							dummyComtradeDataDTO.setAnalogs(channelInfoBuffer);
							// START: 13-Nov-2020 - Long term sample rate to be set instead
//							dummyComtradeDataDTO.setSampleRate(dfrDto.getSampleRate());
							dummyComtradeDataDTO.setSampleRate(stationDetail.getSystemLongTermSampleRate());
							// END: 13-Nov-2020
							dummyComtradeDataDTO.setSampleCnt(finalMergedSampleCnt);
							dummyComtradeDataDTO.setType(M9kStationConstants.ANALOG);
							isData = constructDummyContinuousData(null);
							dummyComtradeDataDTO.setBinaryDataSteam(isData);
							dummyComtradeDataDTO.setTsPrefault(ltrStartTime);
							dummyComtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getSystemLtrPrefaultTime()*1000000));
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
		if (fromSampleNo > 1)
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
		logger.debug("LTR-DEBUG: Merged data and the byte array size "+bais.size());
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
		logger.debug("LTR-DEBUG-DUMMY: byte array size "+bais.size());
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

	public List<Integer> getLstAnalogExportsId() {
		return lstAnalogExportsId;
	}

	public void setLstAnalogExportsId(List<Integer> lstAnalogExportsId) {
		this.lstAnalogExportsId = lstAnalogExportsId;
	}

	/**
	 * START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
	 * @param dfrId
	 * @param expId
	 * @return
	 */
	private AnalogChannelDTO getAnalogChannelForDfr(int dfrId, int expId)
	{
		DfrDTO dfrDto;
		AnalogChannelDTO analogChannelDTO = null;
		for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
    		dfrDto = iterator.next();
    		logger.debug("LTR: dfrDto getDfrId "+dfrDto.getDfrId() + "Is it true (dfrDto.getDfrId() == dfrId) ? "+(dfrDto.getDfrId() == dfrId));
    		if (dfrDto.getDfrId() == dfrId)
    		{
    			for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
    				analogChannelDTO = iterator1.next();
    				logger.debug("LTR: "+analogChannelDTO.getChannel()+" ExpId "+expId+" Is it true analogChannelDTO.getChannel().equals(expId) "+analogChannelDTO.getChannel().equals(""+expId));
    				if (analogChannelDTO.getChannel().equals(""+expId))
    				{
    					logger.debug("Analog channel to return for dfrId "+dfrId+" with expId "+expId+" is "+analogChannelDTO+" primary ratio "+analogChannelDTO.getPrimaryRatio()+" Secondary Ratio "+analogChannelDTO.getSecondaryRatio());
    					return analogChannelDTO;
    				}
    			}
    		}
		}
		return null;
	}
}
