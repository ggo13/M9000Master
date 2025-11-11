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
import com.usi.m9000.station.util.M9kKeyValuePair;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kConstants;


public class M9kContDataFromDb implements Runnable {
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

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kContDataFromDb.class);
	static int expCnt;
	M9kCreateContinuousComtradeFilesFromDB comtradeData;
	Map<Integer, Map<String, Integer>> mapDfrRmsExports ;
	Message receivedMessage;
	TextMessage response = null;
	TextMessage errorResponse = null;
	MessageProducer replyProducer;
	Session session;
	String destFileName;
//	String dataUnits = "%";
	String dataType = "RMS";
	
	private String strStartTime;
	private String strStopTime;
	private Date startDate;
	private Date endDate;
	private static MySqlContinuousComtradeDAO mySqlContinuousComtradeDAO;
	Map<Integer,ComtradeDataDTO> mapComtradeContDat = new HashMap<Integer, ComtradeDataDTO>();
	Map<Integer,Object> mapMaxValues = new HashMap<Integer, Object>();
	Map<Integer,ComtradeDataDTO> mapComtradeContMagnitudeDat = new HashMap<Integer, ComtradeDataDTO>();
	Map<Integer,ComtradeDataDTO> mapComtradeContPhaseDat = new HashMap<Integer, ComtradeDataDTO>();
	
//	private String dataTypeAbbreviated;

	public M9kContDataFromDb(long startTime, long stopTime)
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
//			mapAnalogsRange = getAnalogChnlsRanges();
			comtradeData = new M9kCreateContinuousComtradeFilesFromDB(stationDetail.getSystemStationId());
			mapDfrRmsExports = M9kStationXMLUtil.getMapOfDfrRmsExports();
//			mapAnalogChnlStatus = getChannelsExportStatus();
		} catch (Exception e) {
			logger.error("Unable to process Cont Data ",e);
			return;
		}
	}
	public M9kContDataFromDb(String startTime, String stopTime)
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
//				mapAnalogsRange = getAnalogChnlsRanges();
				comtradeData = new M9kCreateContinuousComtradeFilesFromDB(stationDetail.getSystemStationId());
				mapDfrRmsExports = M9kStationXMLUtil.getMapOfDfrRmsExports();
		} catch (Exception e) {
			logger.error("Unable to process Cont Data ",e);
			return;
		}
	}
	
	public M9kContDataFromDb(Session session, MessageProducer replyProducer, Message receivedMessage)
	{
		this.session = session;
		this.replyProducer = replyProducer;
		this.receivedMessage = receivedMessage;
		TextMessage textMessage = (TextMessage) receivedMessage;

		try {
			mySqlContinuousComtradeDAO = new MySqlContinuousComtradeDAO();
			strStartTime = textMessage.getStringProperty("FROM_DATETIME");
			strStopTime = textMessage.getStringProperty("TO_DATETIME");
			dataType = textMessage.getStringProperty("DATA_EXPORT_TYPE");
//			dataTypeAbbreviated = getAbbreviatedDataType(dataType);
//			dataUnits = M9kStationUtil.getContDataUnits(dataType);
//			logger.debug("Data export type "+dataType+" Abbreviated Form "+dataTypeAbbreviated);
			logger.debug("Entered M9kContDataFromDb constructor with start time "+strStartTime+" stop time "+strStopTime);
			response = session.createTextMessage();
			errorResponse = session.createTextMessage();
			stationDetail = M9kStationDBUtil.getStationDetails();
			logger.debug("Station Name "+stationDetail.getSystemStationName());

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			startDate =  sdf.parse(strStartTime);
			endDate =  sdf.parse(strStopTime);
			ltrStartTime = startDate.getTime()*1000;
			ltrStopTime = endDate.getTime()*1000;
//			SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
//			fileDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//			SimpleDateFormat sdfForFileName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			destFileName = M9kStationConstants.CONT_DATA_TYPE+"-"+dataType+"-"+getRequiredDataFormat("yyyyMMdd-HHmmss",startDate)+"_"+getRequiredDataFormat("yyyyMMdd-HHmmss",endDate);
//			destFileName = getRequiredDataFormat("yyyyMMdd-HHmmss",startDate);
			destFileName = createFileName();

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
//				mapAnalogsRange = getAnalogChnlsRanges();
				comtradeData = new M9kCreateContinuousComtradeFilesFromDB(stationDetail.getSystemStationId());
				// START: 03-Mar-2020 - Add exported Measurements types list to INF file in case of continuous measurements export
				// Set the exported measurement types by converting comma separated into newline 
				logger.debug("dataType "+dataType);
				comtradeData.setExportedMeasurementTypes(dataType);
				// END: 03-Mar-2020

				if (dataType.toUpperCase().indexOf(M9kStationConstants.RMS) != -1)
				{
					mapDfrRmsExports = M9kStationXMLUtil.getMapOfDfrRmsExports();
				}
		} catch (Exception e) {
			logger.error("Unable to create continuous data ",e);
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
			logger.debug("Total wait time for Cont Data " + (end-start));
//			System.out.println("Total wait time for Cont Data " + (end-start));
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
//		ComtradeDataDTO comtradeDataDTO = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
//		Map<Integer,ComtradeDataDTO> mapComtradeContDat = new HashMap<Integer, ComtradeDataDTO>();
//		double oscSampleCnt = getExportBufferTime() * stationDetail.getSampleRate();
//		logger.debug("Oscillography sample count "+oscSampleCnt);
//		Double sampleTime = (((oscSampleCnt - 1)/stationDetail.getSampleRate()) * 1000000);
//		logger.debug("Sample time in double "+sampleTime);
//		long sampleTtimeVal = sampleTime.longValue(); 
//		logger.debug("SampleTimeVal long "+sampleTtimeVal);
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
		String sqlQuery = "SELECT recId, expId, name, description, phase, units, tsLast, sampleRate, sampleCnt, type, measurementType, "
			+ "data FROM `m9000`.`cont` where (tsLast >= ? "
			+ "and tsLast <= ? and measurementType in(";
		StringBuffer strbQuery = new StringBuffer(sqlQuery);
		String[] arrMeasurementTypes = dataType.split(",");
		System.out.println("total measurements to be queried for "+arrMeasurementTypes.length);
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

//		String sqlQuery = "insert into long_term_dat SELECT expId, name, phase, units, tsLast, sampleRate, sampleCnt, scale, offset, if ((@start := (sampleCnt-(tsLast - ?))  * sampleRate) >= 0, @start, 0) as startSampleNo, " +
//		"(sampleCnt-(tsLast - ?))  * sampleRate)) as endSampleNo,data FROM `m9000`.`contAnalog` " +
//		"where (((? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast)  or (? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast ) ) " +
//		"or ((tsLast>= ? and ? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? >= tsLast )))";
		try {
			logger.debug("DB-POOL New connection from processMergeAndInsertLongTermData ");
//			Map<Integer,Float> mapMaxValue = getMaxValue(sqlQuery);

			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Cont-Data-Log: Start time "+ltrStartTime+" Stop time "+ltrStopTime );
//			logger.debug("Cont-Data-Log: sampleTime.longValue() "+sampleTtimeVal);
			logger.debug("Cont-Data-Log: SQL query to be executed "+ strbQuery);
			// START: 10-Oct-2023 - Fix for the error "java.sql.SQLException: Operation not allowed for a result set of type ResultSet.TYPE_FORWARD_ONLY."
//			ps = mysqlConn.prepareStatement(strbQuery.toString());
			ps = mysqlConn.prepareStatement(strbQuery.toString(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			// END: 10-Oct-2023 - Fix for the error "java.sql.SQLException: Operation not allowed for a result set of type ResultSet.TYPE_FORWARD_ONLY."
			ps.setLong(++i, (ltrStartTime));
			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStartTime));
			ps.setLong(++i, (ltrStopTime+(getExportBufferTime() * 1000000)));
			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStopTime+(getExportBufferTime() * 1000000)));
			for (int j = 0; j < arrMeasurementTypes.length; j++) {
				ps.setString(++i, arrMeasurementTypes[j]);				
				logger.debug("Cont-Data-Log: param "+i+" - "+arrMeasurementTypes[j]);
			}
//			ps.setInt(++i, M9kStationConstants.LTR_RMS_OFFSET);
//			ps.setLong(++i, ltrStartTime+sampleTtimeVal);
//			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStartTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStartTime);
//			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStartTime));
//			ps.setLong(++i, ltrStopTime+sampleTtimeVal);
//			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStopTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStopTime);
//			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStopTime));
//			ps.setLong(++i, ltrStartTime);
//			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStartTime));
//			ps.setLong(++i, ltrStopTime+sampleTtimeVal);
//			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStopTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStopTime);
//			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStopTime));
			rs = ps.executeQuery();
//			Map<Integer,Object> mapMaxValue = getMaxValue(rs);
//			InputStream is;
//			StringBuffer channelInfoBuffer;
//			StringBuffer data;
//			InputStream isData;
			double sampleRate;
			int sampleCnt = 0;
//			int startSampleNo = -1;
//			int endSampleNo = -1;
			int expId;
			double startSampleTime = 0;
			long lastSampleTime = 0;
//			Double missingSampleCnt;
//			int totalChannelCnt = 1;
//			float scaleFactor = 0; // scale factor
			String analogChnlName;
			int dfrId;
//			int exportChnlId;
			Integer rmsExpId;
			String type = null;
			String measurementType = null;
//			rs.first();
			double tus;
			double tusDivBy2;

			mapMaxValues = getMaxValue(rs);

			while (rs.next())
			{
				dfrId = rs.getInt("recId");
				expId = rs.getInt("expId");
				type = rs.getString("type");
				measurementType = rs.getString("measurementType");
//				expId = expId - M9kStationConstants.LTR_RMS_OFFSET; // Removing the offset added to export Id during the rms export
				sampleCnt = rs.getInt("sampleCnt");
				sampleRate = rs.getDouble("sampleRate");
				lastSampleTime = rs.getLong("tsLast");

				tus = 1e6/sampleRate;
				tusDivBy2 = tus / 2;

				startSampleTime = (lastSampleTime - (sampleCnt-1) * tus);
				if(ltrStopTime < (startSampleTime - tusDivBy2) || ltrStartTime > (lastSampleTime + tusDivBy2))
				{
					logger.debug("TODD: Cont Data Exports Skipping the blob...");
					continue;
				}
				
				analogChnlName = getAnalogNameStrippingIndexPrefix(rs.getString("name"));
//				logger.debug("Cont-Data-Debug: getMapAnalogRanges() "+getMapAnalogRanges()+ "Dfr Id "+dfrId+ "ExpId "+expId+" Name "+analogChnlName);
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
			}
			if(!mapComtradeContMagnitudeDat.isEmpty())
			{
//				createDummyDataForMissingExports(mapComtradeContDat);
				List<ComtradeDataDTO> lstComtradeMagnitudeDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContMagnitudeDat.values());
//				insertIntoLtr(lstComtradeDataDtos);
				
//				logger.debug("Before sort "+lstComtradeDataDtos);
				Collections.sort(lstComtradeMagnitudeDataDtos, new Comparator<ComtradeDataDTO>() {

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
//				logger.debug("After sort "+lstComtradeDataDtos);
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
						return returnVal;
					}
				});
				
				lstComtradeDataDtos.addAll(lstComtradePhaseDataDtos);
			}
			if (lstComtradeDataDtos != null && !lstComtradeDataDtos.isEmpty())
			{
				comtradeData.createContinuousDataFiles(destFileName, lstComtradeDataDtos);
//				try
//	          	  {
//						ExecutorService executorFileProcessor = Executors.newSingleThreadExecutor();
//						Message textMessageForFile = session.createTextMessage();
//						textMessageForFile.setStringProperty("MESSAGE_TYPE","CONT_FILE_RETRIEVE");
//						logger.debug("Dest. cont dest File name "+comtradeData.getContDataFileName());
//						((TextMessage) textMessageForFile).setText(comtradeData.getContDataFileName());
//						textMessageForFile.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
//						textMessageForFile.setJMSReplyTo(receivedMessage.getJMSReplyTo());
//	          		  executorFileProcessor.execute(new M9kFileRequestProcessor(session,replyProducer,textMessageForFile));
//	          		  logger.debug("Invoked successfully and now acknowledging the message");
//	          		  executorFileProcessor.shutdown();
//	          	  }
//	          	  catch (Exception e) {
//						logger.error( "File transfer process resulted in a exception ",e);
//					}

				mySqlContinuousComtradeDAO.insertContinuousComtradeDetails(dataType, getRequiredDataFormat("yyyy-MM-dd HH:mm:ss",startDate), getRequiredDataFormat("yyyy-MM-dd HH:mm:ss",endDate), comtradeData.getContDataFileName());
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
			getMapAnalogRanges().clear();
			lstComtradeDataDtos.clear();
			mapComtradeContDat.clear();
			
		} 
		catch (Exception e) {
			logger.error("Error in fetching Continuous data ", e);
			try
			{
				TextMessage response = session.createTextMessage();
				response.setText("ERROR: No Matching Data available");
	            response.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
	            this.replyProducer.send(receivedMessage.getJMSReplyTo(), response);
			}
			catch (Exception e1)
			{
				logger.error("Failed to send response to master ", e1);
			}
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
		double tus;
		// START: 25-Feb-2020 - Cont MEASUREMENTS export with VARS and WATTS
//		String measurementType;
//		measurementType = rs.getString("measurementType");
		// END: 25-Feb-2020
		dfrId = rs.getInt("recId");
		expId = rs.getInt("expId");

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
////		logger.debug("Cont-Data-Log: End sample No "+endSampleNo);
//		
////		startSampleTime = rs.getLong("StartSampleTime");
////		logger.debug("Cont-Data-Log: from DB startSampleTime"+startSampleTime);
//		startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
////		logger.debug("Cont-Data-Log: calculated startSampleTime"+startSampleTime);

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
			// START: 25-Feb-2020 - Cont MEASUREMENTS export with VARS and WATTS
//			if (dataType.endsWith(M9kStationConstants.WATTS) || dataType.endsWith(M9kStationConstants.VARS))
//			if (measurementType.endsWith(M9kStationConstants.WATTS) || measurementType.endsWith(M9kStationConstants.VARS))
//			// END: 25-Feb-2020
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
			comtradeDataDTO.setTsPrefault(ltrStartTime);
//			if ((ltrStopTime - ltrStartTime) > ((stationDetail.getLtrPrefaultTime()*1000000) + (stationDetail.getLtrPostfaultTime()*1000000)))
//			{
//				comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getLtrPrefaultTime()*1000000));
//			}
//			else
//			{
				comtradeDataDTO.setTsTrigger(ltrStartTime);
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
//			comtradeDataDTO.setTsTrigger(ltrStartTime+stationDetail.getLtrPrefaultTime());
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
//		
////		endSampleNo = rs.getInt("endSampleNo");
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
//		
////		startSampleTime = rs.getLong("StartSampleTime");
////		logger.debug("Cont-Data-Log: from DB startSampleTime"+startSampleTime);
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
			comtradeMagnitudeDataDTO.setType(M9kStationConstants.MAGNITUDE);
			comtradePhaseDataDTO.setType(M9kStationConstants.PHASE);
			comtradeMagnitudeDataDTO.setTsPrefault(ltrStartTime);
			comtradePhaseDataDTO.setTsPrefault(ltrStartTime);
			comtradeMagnitudeDataDTO.setTsTrigger(ltrStartTime);
			comtradePhaseDataDTO.setTsTrigger(ltrStartTime);
			
			// Phase data
			
			isData = constructContinuousComplexData(is, sampleRate,startSampleNo, endSampleNo, magScaleFactor, phaseScaleFactor);
			comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
			comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
			isData[0].close();
			isData[1].close();
			mapComtradeContMagnitudeDat.put(expId, comtradeMagnitudeDataDTO);
			mapComtradeContPhaseDat.put(expId, comtradePhaseDataDTO);
//			if ((ltrStopTime - ltrStartTime) > ((stationDetail.getLtrPrefaultTime()*1000000) + (stationDetail.getLtrPostfaultTime()*1000000)))
//			{
//				comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getLtrPrefaultTime()*1000000));
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
//			comtradeDataDTO.setTsTrigger(ltrStartTime+stationDetail.getLtrPrefaultTime());
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
//    	for (Iterator<Boolean> iterator = mapDFrs.values().iterator(); iterator.hasNext();) {
//    		Boolean isActive = iterator.next();
//			if (isActive)
//			{
//				activeCount++;
//			}
//		}
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

			sampleData = dis.readFloat();
//			System.out.println(calculatedValue);
			shortDataVal = (short) (sampleData * scaleFactor);
//			logger.debug("\t\t\tSample data read as float "+sampleData+" scaleFactor "+scaleFactor+" converted short value "+shortDataVal);
//			if (sampleData <= 0 || shortDataVal == -32678)
//			{
//				logger.debug("Cont-Data-Debug-DEBUG: sample data "+sampleData+ " scale factor "+scaleFactor+" converted short "+shortDataVal);
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
//		logger.debug("Cont Data-DEBUG: byte array size "+bais.size());
		return new ByteArrayInputStream(bais.toByteArray());

	}

	// If the inactive dfr has few data in the start then the dummy data has to be appended to the existing data. If there is no data the first parameter will be NULL 
	@SuppressWarnings("unused")
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
//			logger.debug("Cont Data-DEBUG-DUMMY: Sample cnt of few available data of inactive dfr "+i);
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
//		logger.debug("Cont Data-DEBUG-DUMMY: byte array size "+bais.size());
		return new ByteArrayInputStream(bais.toByteArray());

	}

	private InputStream constructDummyContinuousData(int totalSamplesMissing)
	{
//		StringBuffer data = new StringBuffer();
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		Integer iDummyHexData = 0x8000;
		
		try {
			
//		logger.debug("Cont Data-DEBUG-DUMMY: Sample cnt of those missing "+totalSamplesMissing);
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
//		logger.debug("Cont Data-DEBUG-DUMMY: byte array size "+bais.size());
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
		M9kContDataFromDb m9kLtrRmsFromDb = new M9kContDataFromDb("2013-05-21 10:33:00", "2013-05-21 10:35:00");
		m9kLtrRmsFromDb.processLTR();
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
	
	private String createFileName()
	{
		String ctFileName;
		String timeCode;
//	    String date = new java.text.SimpleDateFormat("yyMMdd,HHmmssSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
//		String date =M9kStationUtil.getDateFormat("yyMMdd,HHmmssSSS").format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
		String date = getRequiredDataFormat("yyyyMMdd-HHmmss",startDate);
//	    String filePrefix = "R";
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
		
		// START: 03-Mar-2020 - File Name too long for multiple exports
//	    ctFileName = date + ","+timeCode+","+stationDetail.getSystemStationName()+","+stationDetail.getSystemRecordingDeviceId()+","+M9kStationComtradeUtil.getComtradeProperty("company")+","+filePrefix+nextId+","+dataTypeAbbreviated;
		ctFileName = date + ","+timeCode+","+stationDetail.getSystemStationName()+","+stationDetail.getSystemRecordingDeviceId()+","+M9kStationComtradeUtil.getComtradeProperty("company")+","+filePrefix+nextId+",MeasuredExports";
		// END: 03-Mar-2020
		
	    // END: 20-May-2016
	    ctFileName = ctFileName.replace("/", "_");
	    ctFileName = ctFileName.replace("\\", "_");
	    return ctFileName;
	}
	
	@SuppressWarnings("unused")
	private String getAbbreviatedDataType(String dataTypeInFull)
	{
		StringBuffer abbreviatedDataType = new StringBuffer();
		if (dataTypeInFull != null)
		{
			String[] arrMeasurementTypes = dataTypeInFull.split(",");
			for (int j = 0; j < arrMeasurementTypes.length; j++) {
				// Checking for All Sequence algorithms
				if (arrMeasurementTypes[j].equalsIgnoreCase(M9kStationConstants.RMS))
				{
					if (j == 0)
					{
						abbreviatedDataType.append(M9kStationConstants.RMS);
					}
					else
					{
						abbreviatedDataType.append("_"+M9kStationConstants.RMS);
					}
				}
				else if (arrMeasurementTypes[j].equalsIgnoreCase(M9kStationConstants.FREQUENCY))
				{
					if (j == 0)
					{
						abbreviatedDataType.append(M9kStationConstants.FREQUENCY_SHORT);
					}
					else
					{
						abbreviatedDataType.append("_"+M9kStationConstants.FREQUENCY_SHORT);
					}
				}
				else if (arrMeasurementTypes[j].equalsIgnoreCase(M9kStationConstants.PHASOR))
				{
					if (j == 0)
					{
						abbreviatedDataType.append(M9kStationConstants.PHASOR_SHORT);
					}
					else
					{
						abbreviatedDataType.append("_"+M9kStationConstants.PHASOR_SHORT);
					}
				}
				else if (abbreviatedDataType.indexOf(M9kStationConstants.HARMONIC_SHORT) == -1 && arrMeasurementTypes[j].toUpperCase().startsWith(M9kStationConstants.HARMONIC))
				{
					if (j == 0)
					{
						abbreviatedDataType.append(M9kStationConstants.HARMONIC_SHORT);
					}
					else
					{
						abbreviatedDataType.append("_"+M9kStationConstants.HARMONIC_SHORT);
					}
				}
				else if (abbreviatedDataType.indexOf(M9kStationConstants.SEQUENCE) == -1 && arrMeasurementTypes[j].toLowerCase().indexOf(M9kStationConstants.SEQUENCE.toLowerCase()) != -1)
				{
					if (j == 0)
					{
						abbreviatedDataType.append(M9kStationConstants.SEQUENCE);
					}
					else
					{
						abbreviatedDataType.append("_"+M9kStationConstants.SEQUENCE);
					}
					
				}
				else if (abbreviatedDataType.indexOf(M9kStationConstants.VARS_REACTIVE) == -1 && arrMeasurementTypes[j].toLowerCase().endsWith(M9kStationConstants.VARS.toLowerCase())) 
				{
					if (j == 0)
					{
						abbreviatedDataType.append(M9kStationConstants.VARS_REACTIVE);
					}
					else
					{
						abbreviatedDataType.append("_"+M9kStationConstants.VARS_REACTIVE);
					}
					
				}
				else if (abbreviatedDataType.indexOf(M9kStationConstants.WATTS_REAL) == -1 && arrMeasurementTypes[j].toLowerCase().endsWith(M9kStationConstants.WATTS.toLowerCase())) 
				{
					if (j == 0)
					{
						abbreviatedDataType.append(M9kStationConstants.WATTS_REAL);
					}
					else
					{
						abbreviatedDataType.append("_"+M9kStationConstants.WATTS_REAL);
					}
					
				}
//				else if (arrMeasurementTypes[j].toLowerCase().endsWith(M9kStationConstants.SEQ_VOLTAGE.toLowerCase())) 
//				{
//					if (arrMeasurementTypes[j].toLowerCase().startsWith(M9kStationConstants.POSITIVE.toLowerCase()))
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.POSITIVE_SEQ_VOLTAGE_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.POSITIVE_SEQ_VOLTAGE_SHORT);
//						}						
//					}
//					else if (arrMeasurementTypes[j].toLowerCase().startsWith(M9kStationConstants.NEGATIVE.toLowerCase()))
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.NEGATIVE_SEQ_VOLTAGE_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.NEGATIVE_SEQ_VOLTAGE_SHORT);
//						}						
//					}
//					else
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.ZERO_SEQ_VOLTAGE_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.ZERO_SEQ_VOLTAGE_SHORT);
//						}						
//						
//					}
//						
//				}
//				else if (arrMeasurementTypes[j].toLowerCase().endsWith(M9kStationConstants.SEQ_CURRENT.toLowerCase())) 
//				{
//					if (arrMeasurementTypes[j].toLowerCase().startsWith(M9kStationConstants.POSITIVE.toLowerCase()))
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.POSITIVE_SEQ_CURRENT_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.POSITIVE_SEQ_CURRENT_SHORT);
//						}						
//					}
//					else if (arrMeasurementTypes[j].toLowerCase().startsWith(M9kStationConstants.NEGATIVE.toLowerCase()))
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.NEGATIVE_SEQ_CURRENT_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.NEGATIVE_SEQ_CURRENT_SHORT);
//						}						
//					}
//					else
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.ZERO_SEQ_CURRENT_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.ZERO_SEQ_CURRENT_SHORT);
//						}						
//						
//					}
//						
//				}
//				else if (arrMeasurementTypes[j].toLowerCase().endsWith(M9kStationConstants.VARS.toLowerCase())) 
//				{
//					if (arrMeasurementTypes[j].equalsIgnoreCase(M9kStationConstants.ALL_PHASE_VARS))
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.ALL_PHASE_VARS_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.ALL_PHASE_VARS_SHORT);
//						}						
//					}
//					else if (arrMeasurementTypes[j].equalsIgnoreCase(M9kStationConstants.A_PHASE_VARS))
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.A_PHASE_VARS_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.A_PHASE_VARS_SHORT);
//						}						
//					}
//					else if (arrMeasurementTypes[j].equalsIgnoreCase(M9kStationConstants.B_PHASE_VARS))
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.B_PHASE_VARS_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.B_PHASE_VARS_SHORT);
//						}						
//					}
//					else 
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.C_PHASE_VARS_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.C_PHASE_VARS_SHORT);
//						}						
//						
//					}
//						
//				}
//				else if (arrMeasurementTypes[j].toLowerCase().endsWith(M9kStationConstants.WATTS.toLowerCase())) 
//				{
//					if (arrMeasurementTypes[j].equalsIgnoreCase(M9kStationConstants.ALL_PHASE_WATTS))
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.ALL_PHASE_WATTS_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.ALL_PHASE_WATTS_SHORT);
//						}						
//					}
//					else if (arrMeasurementTypes[j].equalsIgnoreCase(M9kStationConstants.A_PHASE_WATTS))
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.A_PHASE_WATTS_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.A_PHASE_WATTS_SHORT);
//						}						
//					}
//					else if (arrMeasurementTypes[j].equalsIgnoreCase(M9kStationConstants.B_PHASE_WATTS))
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.B_PHASE_WATTS_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.B_PHASE_WATTS_SHORT);
//						}						
//					}
//					else 
//					{
//						if (j == 0)
//						{
//							abbreviatedDataType = new StringBuffer();
//							abbreviatedDataType.append(M9kStationConstants.C_PHASE_WATTS_SHORT);
//						}
//						else
//						{
//							abbreviatedDataType.append("_"+M9kStationConstants.C_PHASE_WATTS_SHORT);
//						}						
//						
//					}
//						
//				}
				else if (arrMeasurementTypes[j].toLowerCase().indexOf(M9kStationConstants.SEQUENCE.toLowerCase()) == -1
						&& arrMeasurementTypes[j].toLowerCase().indexOf(M9kStationConstants.VARS.toLowerCase()) == -1
						&& arrMeasurementTypes[j].toLowerCase().indexOf(M9kStationConstants.WATTS.toLowerCase()) == -1
						&& arrMeasurementTypes[j].toLowerCase().indexOf(M9kStationConstants.HARMONIC.toLowerCase()) == -1)
				{
					if (j == 0)
					{
						abbreviatedDataType = new StringBuffer();
						abbreviatedDataType.append(arrMeasurementTypes[j]);
					}
					else
					{
						abbreviatedDataType.append("_"+arrMeasurementTypes[j]);
					}
				}
			}
		}
		return abbreviatedDataType.toString();
	}


}
