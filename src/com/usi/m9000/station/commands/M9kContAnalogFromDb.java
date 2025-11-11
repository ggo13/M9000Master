package com.usi.m9000.station.commands;

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

import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.MySqlContinuousComtradeDAO;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kConstants;


public class M9kContAnalogFromDb implements Runnable {
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

	Message receivedMessage;
	String destFileName;
	MessageProducer replyProducer;
	Session session;
	private String strStartTime;
	private String strStopTime;
	private Date startDate;
	private Date endDate;
	private static MySqlContinuousComtradeDAO mySqlContinuousComtradeDAO;
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kContAnalogFromDb.class);
	public M9kContAnalogFromDb(long startTime, long stopTime)
	{
		logger.debug("Entered M9kContAnalogFromDb constructor with start time "+startTime+" stop time "+stopTime);
		stationDetail = M9kStationDBUtil.getStationDetails();
//		ltrStartTime = startTime-(stationDetail.getPrefaultTime()*1000);
//		ltrStopTime = stopTime+(stationDetail.getPostfaultTime()*1000);
		ltrStartTime = startTime;
		ltrStopTime = stopTime;
		logger.debug("M9kContAnalogFromDb: After prefault Time start time "+ltrStartTime+" stop time "+ltrStopTime);		
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
			logger.error("Unable to process ContAnalog Data ",e);
			return;
		}
	}
	
	public M9kContAnalogFromDb(String startTime, String stopTime)
	{
		try
		{
			logger.debug("Entered M9kContAnalogFromDb constructor with start time "+startTime+" stop time "+stopTime);
			stationDetail = M9kStationDBUtil.getStationDetails();
	//		ltrStartTime = startTime-(stationDetail.getPrefaultTime()*1000);
	//		ltrStopTime = stopTime+(stationDetail.getPostfaultTime()*1000);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			ltrStartTime = sdf.parse(startTime).getTime()*1000;
			ltrStopTime = sdf.parse(stopTime).getTime()*1000;
			logger.debug("M9kContAnalogFromDb: After prefault Time start time "+ltrStartTime+" stop time "+ltrStopTime);		
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
			logger.error("Unable to process ContAnalog Data ",e);
			return;
		}
	}

	public M9kContAnalogFromDb(Session session, MessageProducer replyProducer, Message receivedMessage)
	{
		this.session = session;
		this.replyProducer = replyProducer;
		this.receivedMessage = receivedMessage;
		TextMessage textMessage = (TextMessage) receivedMessage;
		
		try
		{
			mySqlContinuousComtradeDAO = new MySqlContinuousComtradeDAO();
			strStartTime = textMessage.getStringProperty("FROM_DATETIME");
			strStopTime = textMessage.getStringProperty("TO_DATETIME");
			logger.debug("Entered M9kContAnalogFromDb constructor with start time "+strStartTime+" stop time "+strStopTime);
			stationDetail = M9kStationDBUtil.getStationDetails();
	//		ltrStartTime = startTime-(stationDetail.getPrefaultTime()*1000);
	//		ltrStopTime = stopTime+(stationDetail.getPostfaultTime()*1000);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			startDate =  sdf.parse(strStartTime);
			endDate =  sdf.parse(strStopTime);
			ltrStartTime = startDate.getTime()*1000;
			ltrStopTime = endDate.getTime()*1000;
//			SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
//			fileDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//			destFileName = M9kStationConstants.CONT_ANALOG_DATA_TYPE+"-"+fileDateFormat.format(startDate)+"_"+fileDateFormat.format(endDate);
//			SimpleDateFormat sdfForFileName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			destFileName = M9kStationConstants.CONT_ANALOG_DATA_TYPE+"-"+getRequiredDataFormat("yyyyMMdd-HHmmss",startDate)+"_"+getRequiredDataFormat("yyyyMMdd-HHmmss",endDate);
			destFileName = createFileName();

			logger.debug("M9kContAnalogFromDb: After prefault Time start time "+ltrStartTime+" stop time "+ltrStopTime);		
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
			logger.error("Unable to process ContAnalog Data ",e);
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
			logger.debug("Total wait time for ContAnalog Data " + (end-start));
//			System.out.println("Total wait time for ContAnalog Data " + (end-start));
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
		// START: 03-Dec-2020 - cont. Analog handling virtual channels 
		Map<Integer,Map<Integer,ComtradeDataDTO>> mapDfrComtradeContDat = new HashMap<Integer, Map<Integer,ComtradeDataDTO>>();
		// END: 03-Dec-2020
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
			+ "and tsLast <= ?)order by expId, tsLast";

//		String sqlQuery = "insert into long_term_dat SELECT expId, name, phase, units, tsLast, sampleRate, sampleCnt, scale, offset, if ((@start := (sampleCnt-(tsLast - ?))  * sampleRate) >= 0, @start, 0) as startSampleNo, " +
//		"(sampleCnt-(tsLast - ?))  * sampleRate)) as endSampleNo,data FROM `m9000`.`contAnalog` " +
//		"where (((? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast)  or (? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast ) ) " +
//		"or ((tsLast>= ? and ? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? >= tsLast )))";
		try {
			logger.debug("DB-POOL New connection from processMergeAndInsertLongTermData ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("ContAnalog-Data-Debug: Start time "+ltrStartTime+" Stop time "+ltrStopTime );
			logger.debug("ContAnalog-Data-Debug: sampleTime.longValue() "+sampleTtimeVal);
			logger.debug("ContAnalog-Data-Debug: SQL query to be executed "+ sqlQuery);
			ps = mysqlConn.prepareStatement(sqlQuery);
			ps.setLong(++i, (ltrStartTime));
			logger.debug("ContAnalog-Data-Debug: param "+i+" - "+(ltrStartTime));
			ps.setLong(++i, (ltrStopTime+(getExportAnalogBufferTime() * 1000000)));
			logger.debug("ContAnalog-Data-Debug: param "+i+" - "+(ltrStopTime+(getExportAnalogBufferTime() * 1000000)));
//			ps.setLong(++i, ltrStartTime+sampleTtimeVal);
//			logger.debug("ContAnalog-Data-Debug: param "+i+" - "+(ltrStartTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStartTime);
//			logger.debug("ContAnalog-Data-Debug: param "+i+" - "+(ltrStartTime));
//			ps.setLong(++i, ltrStopTime+sampleTtimeVal);
//			logger.debug("ContAnalog-Data-Debug: param "+i+" - "+(ltrStopTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStopTime);
//			logger.debug("ContAnalog-Data-Debug: param "+i+" - "+(ltrStopTime));
//			ps.setLong(++i, ltrStartTime);
//			logger.debug("ContAnalog-Data-Debug: param "+i+" - "+(ltrStartTime));
//			ps.setLong(++i, ltrStopTime+sampleTtimeVal);
//			logger.debug("ContAnalog-Data-Debug: param "+i+" - "+(ltrStopTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStopTime);
//			logger.debug("ContAnalog-Data-Debug: param "+i+" - "+(ltrStopTime));
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
			double startSampleTime = 0;
			long lastSampleTime = 0;
			Double missingSampleCnt;
			double tus;
			double tusDivBy2;
			// START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
			int dfrId;
			AnalogChannelDTO analogChannelDto;
			// END: 16-Jul-2020
			while (rs.next())
			{
//				System.out.println("row cnt..."+rs.getRow());
				sampleCnt = rs.getInt("sampleCnt");
				logger.debug("ContAnalog-Data-Debug: Sample Cnt "+sampleCnt);
				sampleRate = rs.getDouble("sampleRate");
				logger.debug("ContAnalog-Data-Debug: sampleRate "+sampleRate);
				dfrId= rs.getInt("recId");
				logger.debug("LTR-QUERY: dfrId "+dfrId);
				
				expId = rs.getInt("expId");
				logger.debug("ContAnalog-Data-Debug: expId "+expId);
				is = rs.getBinaryStream("data");
				lastSampleTime = rs.getLong("tsLast");
				logger.debug("ContAnalog-Data-Debug: lastSampleTime "+lastSampleTime);
//				startSampleNo = rs.getInt("startSampleNo");
				// START: 06-Dec-2019 Todd's logic
				tus = 1e6/sampleRate;
				tusDivBy2 = tus / 2;
				
//				if (lastSampleTime > ltrStartTime)
//				{
//					startSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - ltrStartTime)*sampleRate)/1000000);
//					if (startSampleNo < 0)
//					{
//						startSampleNo = 1;
//					}
//				}
//				else
//				{
//					startSampleNo = 1;
//				}
//				logger.debug("ContAnalog-Data-Debug: Start sample No "+startSampleNo);
//				
////				endSampleNo = rs.getInt("endSampleNo");
//				if (lastSampleTime > ltrStopTime)
//				{
//					endSampleNo = sampleCnt - (int)Math.round(((lastSampleTime - ltrStopTime)*sampleRate)/1000000);
//					if (endSampleNo < 0)
//					{
//						endSampleNo = sampleCnt;
//					}
//				}
//				else
//				{
//					endSampleNo = sampleCnt;
//				}
//				logger.debug("ContAnalog-Data-Debug: End sample No "+endSampleNo);
//				
////				startSampleTime = rs.getLong("StartSampleTime");
////				logger.debug("ContAnalog-Data-Debug: from DB startSampleTime"+startSampleTime);
//				startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
//				logger.debug("ContAnalog-Data-Debug: calculated startSampleTime"+startSampleTime);
				startSampleTime = (lastSampleTime - (sampleCnt-1) * tus);
				if(ltrStopTime < (startSampleTime - tusDivBy2) || ltrStartTime > (lastSampleTime + tusDivBy2))
				{
					logger.debug("TODD: contAnalog - Skipping the blob...");
					continue;
				}
				
				startSampleNo = M9kStationUtil.toIndex(ltrStartTime - startSampleTime, tus, sampleCnt);
				logger.debug("TODD: ContAnalog data Start sample no "+startSampleNo);
				endSampleNo = M9kStationUtil.toIndex(ltrStopTime - startSampleTime, tus, sampleCnt);
				logger.debug("TODD: ContAnalog data  End sample no "+endSampleNo);
				// END: 04-Dec-2019 

				// START: 03-Dec-2020 - cont. Analog handling virtual channels
				mapComtradeContDat = mapDfrComtradeContDat.get(dfrId);
				if (mapComtradeContDat == null)
				{
					mapComtradeContDat = new HashMap<Integer, ComtradeDataDTO>();
					mapDfrComtradeContDat.put(dfrId,mapComtradeContDat);
				}
				// END: 03-Dec-2020
				
				// START: 15-Jul-2020 - LTR Analog COMTRADE config file need to include primary and secondary values
				analogChannelDto = getAnalogChannelForDfr(dfrId,expId);
				// END: 16-Jul-2020

				if (mapComtradeContDat.get(expId) == null)
				{
//					logger.debug("ContAnalog-Data-Debug: Start of the sample....for expId "+expId+" ->" + startSampleNo);
//					if (rs.isLast())
//					{
						sampleCnt=endSampleNo-startSampleNo+1;
//						logger.debug("ContAnalog-Data-Debug: Last row to be processed. End Sample..for expId "+expId+" ->" + endSampleNo+" Actual sampleCnt.."+sampleCnt);
//					}
//					iCnt = 1;
					comtradeDataDTO = new ComtradeDataDTO();
					comtradeDataDTO.setTsLast( lastSampleTime);
					if (ltrStartTime < startSampleTime) // Missing data at the start
					{
						// Construct dummy data for initial missing data
						missingSampleCnt = Math.floor(((startSampleTime - ltrStartTime )/1000000.0)*sampleRate);
						isData = constructDummyContinuousData(missingSampleCnt.intValue());
						comtradeDataDTO.setBinaryDataSteam(isData);
					}
					isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo);
					// START: 03-Dec-2020 - cont. Analog handling virtual channels
					comtradeDataDTO.setDfrId(dfrId);
					// END: 03-Dec-2020
					comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
					comtradeDataDTO.setStationId(stationDetail.getSystemStationId());
					comtradeDataDTO.setStationName(stationDetail.getSystemStationName());
					comtradeDataDTO.setRecordingDevId(stationDetail.getSystemRecordingDeviceId());
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
//					comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getPrefaultTime()*1000)+(stationDetail.getLtrPrefaultTime()*1000000));
					comtradeDataDTO.setTsTrigger(ltrStartTime);
				}
				else
				{
					comtradeDataDTO = mapComtradeContDat.get(expId);
//					System.out.println("Comtrade Data DTO "+comtradeDataDTO);
//					logger.debug("ContAnalog-Data-Debug: start Time "+startSampleTime+" Last time of the previoud record "+comtradeDataDTO.getTsLast()+" previoud Last Time +1 "+ (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)));
					if (startSampleTime > (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)) ) // First sample should be Last sample + 1 of the previous record
					{
						missingSampleCnt = Math.floor(((startSampleTime - comtradeDataDTO.getTsLast() )/1000000.0)*sampleRate);
						isData = constructDummyContinuousData(missingSampleCnt.intValue());
						comtradeDataDTO.setBinaryDataSteam(isData);
						isData.close();
					}
					comtradeDataDTO.setTsLast(lastSampleTime);
//					startSampleNo = -1;
//					if (rs.isLast())
//					{
//						logger.debug("ContAnalog-Data-Debug: processing expId "+expId); 
//						endSampleNo = rs.getInt("endSampleNo");
					logger.debug("ContAnalog-Data-Debug:  StartSampleNo..."+startSampleNo+" End Sample No "+endSampleNo);
						isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo);
						 
						sampleCnt=(endSampleNo-startSampleNo+1)+comtradeDataDTO.getSampleCnt();
						logger.debug("ContAnalog-Data-Debug: sample count for expId "+expId+" is "+sampleCnt );
//					}
//					else
//					{
//						logger.debug("ContAnalog-Data-Debug: Still processing for expId "+expId); 
//						isData = constructContinuousData(is, sampleRate);
//						sampleCnt+=comtradeDataDTO.getSampleCnt()-1;
//						logger.debug("ContAnalog-Data-Debug: sample count as of now for expId "+expId+" is "+sampleCnt );
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
					logger.debug("ContAnalog-Data-Debug: finalMergedSampleCnt "+finalMergedSampleCnt);
				}
				is.close();
			}
			// START: 03-Dec-2020 - cont. Analog handling virtual channels 
//			if(!mapComtradeContDat.isEmpty())
			if(!mapDfrComtradeContDat.isEmpty())
			{
//				createDummyDataForMissingExports(mapComtradeContDat);
//				lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContDat.values());
				lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
				for (Iterator<Integer> iterator = mapDfrComtradeContDat.keySet().iterator(); iterator
						.hasNext();) {
					dfrId = iterator.next();
					lstComtradeDataDtos.addAll(mapDfrComtradeContDat.get(dfrId).values());
				}
				// END: 03-Dec-2020
//				insertIntoLtr(lstComtradeDataDtos);
				Collections.sort(lstComtradeDataDtos, new Comparator<ComtradeDataDTO>() {

					@Override
					public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
//						return ((o1.getDfrId() < o2.getDfrId())?0:1);
						int returnVal = ((Integer)o1.getDfrId()).compareTo(o2.getDfrId());
						if (returnVal == 0)
						{
							returnVal = ((Integer)o1.getExportId()).compareTo(o2.getExportId());
						}
//						if ((o1.getDfrId() < o2.getDfrId()))
//						{
//							returnVal= -1;
//						}
//						else if (o1.getDfrId() == o2.getDfrId())
//						{
//							if (o1.getExportId() < o2.getExportId())
//							{
//								returnVal =  -1;
//							}
//							else
//							{
//								returnVal = 1;
//							}
//						}
//						else
//						{
//							returnVal = 1 ;
//						}
//						return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
						return returnVal;
					}
				});
				comtradeData.createContinuousDataFiles(destFileName, lstComtradeDataDtos);
//				try
//          	  {
//					ExecutorService executorFileProcessor = Executors.newSingleThreadExecutor();
//					Message textMessageForFile = session.createTextMessage();
//					textMessageForFile.setStringProperty("MESSAGE_TYPE","CONT_FILE_RETRIEVE");
//					logger.debug("Dest. cont dest File name "+comtradeData.getContDataFileName());
//					((TextMessage) textMessageForFile).setText(comtradeData.getContDataFileName());
//					textMessageForFile.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
//					textMessageForFile.setJMSReplyTo(receivedMessage.getJMSReplyTo());
//          		  executorFileProcessor.execute(new M9kFileRequestProcessor(session,replyProducer,textMessageForFile));
//          		  logger.debug("Invoked successfully and now acknowledging the message");
//          		  executorFileProcessor.shutdown();
//          	  }
//          	  catch (Exception e) {
//					logger.error( "File transfer process resulted in a exception ",e);
//				}
				mySqlContinuousComtradeDAO.insertContinuousComtradeDetails(M9kStationConstants.ANALOG, getRequiredDataFormat("yyyy-MM-dd HH:mm:ss",startDate), getRequiredDataFormat("yyyy-MM-dd HH:mm:ss",endDate), comtradeData.getContDataFileName());
				logger.debug("Cont Data file created. sending response filename to messageQ "+destFileName);
				TextMessage response = session.createTextMessage();
				response.setText(comtradeData.getContDataFileName());
                response.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
                this.replyProducer.send(receivedMessage.getJMSReplyTo(), response);
			}
			else
			{
				TextMessage response = session.createTextMessage();
				response.setText("ERROR: No Matching Data available");
                response.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
                this.replyProducer.send(receivedMessage.getJMSReplyTo(), response);
				
			}
			logger.debug("Size of analog cont..."+lstComtradeDataDtos.size());
			getLstMissingExportsId().clear();
			getLstAnalogExportsId().clear();
			lstComtradeDataDtos.clear();
			mapComtradeContDat.clear();
			
		} catch (SQLException e) {
			logger.debug("Error in fetching continuous Analog data ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.debug("Error in fetching continuous Analog data ", e);
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
//		logger.debug("ContAnalog-Data-Debug: byte array size "+bais.size());
		return new ByteArrayInputStream(bais.toByteArray());

	}

	private InputStream constructDummyContinuousData(int totalSamplesMissing)
	{
//		StringBuffer data = new StringBuffer();
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		Integer iDummyHexData = 0x8000;
		
		try {
			
//		logger.debug("ContAnalog-Data-Debug-DUMMY: Sample cnt of those missing "+totalSamplesMissing);
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
//		logger.debug("ContAnalog-Data-Debug-DUMMY: byte array size "+bais.size());
		return new ByteArrayInputStream(bais.toByteArray());

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
		M9kContAnalogFromDb M9kContAnalogFromDb = new M9kContAnalogFromDb("2014-09-15 15:00:00", "2014-09-15 15:00:05");
		M9kContAnalogFromDb.processLTR();
	}
	private String getRequiredDataFormat(String requiredFormat, Date dateToFormat)
	{
		String convertedDateFormat = null;
//		System.out.println("date format for date "+dateToFormat);
		SimpleDateFormat requiredDateFormat = new SimpleDateFormat(requiredFormat);
		requiredDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		convertedDateFormat = requiredDateFormat.format(dateToFormat.getTime());
//		System.out.println("Converted date format "+convertedDateFormat);
		return convertedDateFormat;
	}
	
	private String createFileName()
	{
		String ctFileName;
		String timeCode;
//	    String date = new java.text.SimpleDateFormat("yyMMdd,HHmmssSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
//		String date =M9kStationUtil.getDateFormat("yyMMdd,HHmmssSSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
		String date = getRequiredDataFormat("yyyyMMdd-HHmmss",startDate);
	    String filePrefix = "R"+String.format("%02d", stationDetail.getSystemStationId());
	    int nextId = -1;
//	    if (stationDetail.getSystemStationId() < 10)
//	    {
//	    	filePrefix += "0"+stationDetail.getSystemStationId();
//	    }
//	    else
//	    {
//	    	filePrefix += stationDetail.getSystemStationId();
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
		try {
			nextId = mySqlContinuousComtradeDAO.getNextId(null);
		} catch (M9000Exception e) {
			logger.error("Unable to get the next id from continuous comtrade table. Using -1",e);
		}
		
	    ctFileName = date + ","+timeCode+","+stationDetail.getSystemStationName()+","+stationDetail.getSystemRecordingDeviceId()+","+M9kStationComtradeUtil.getComtradeProperty("company")+","+filePrefix+nextId+","+M9kStationConstants.OSC;
	    // END: 20-May-2016
	    ctFileName = ctFileName.replace("/", "_");
	    ctFileName = ctFileName.replace("\\", "_");
	    return ctFileName;
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
    		if (dfrDto.getDfrId() == dfrId)
    		{
    			for (Iterator<AnalogChannelDTO> iterator1 = dfrDto.getLstAnalogChannels().iterator(); iterator1.hasNext();) {
    				analogChannelDTO = iterator1.next();
    				if (analogChannelDTO.getChannel().equals(""+expId))
    				{
    					break;
    				}
    			}
    		}
		}
		logger.debug("Analog channel to return for dfrId "+dfrId+" with expId "+expId+" is "+analogChannelDTO);
		return analogChannelDTO;
	}
}
