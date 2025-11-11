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

import javax.jms.JMSException;
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
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kConstants;


public class M9kContPhasorDataFromDb implements Runnable {
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

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kContPhasorDataFromDb.class);
	static int expCnt;
	M9kCreateContinuousComtradeFilesFromDB comtradeData;
//	Map<Integer, Map<String, Integer>> mapDfrRmsExports ;
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
	int currentExpIdProcessed; // for debugging
	private static MySqlContinuousComtradeDAO mySqlContinuousComtradeDAO;
	public M9kContPhasorDataFromDb(long startTime, long stopTime)
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
//			mapDfrRmsExports = M9kStationXMLUtil.getMapOfDfrRmsExports();
//			mapAnalogChnlStatus = getChannelsExportStatus();
		} catch (Exception e) {
			logger.error("Unable to process Cont Data ",e);
			return;
		}
	}
	public M9kContPhasorDataFromDb(String startTime, String stopTime)
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
//				mapDfrRmsExports = M9kStationXMLUtil.getMapOfDfrRmsExports();
		} catch (Exception e) {
			logger.error("Unable to process Cont Data ",e);
			return;
		}
	}
	
	public M9kContPhasorDataFromDb(Session session, MessageProducer replyProducer, Message receivedMessage)
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
//			dataUnits = M9kStationUtil.getContDataUnits(dataType);
//			dataUnits = M9kStationConstants.PHASOR;
			logger.debug("Data export type "+dataType);
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
			destFileName = M9kStationConstants.CONT_DATA_TYPE+"-"+dataType+"-"+getRequiredDataFormat("yyyyMMdd-HHmmss",startDate)+"_"+getRequiredDataFormat("yyyyMMdd-HHmmss",endDate);

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
//				if (dataType.equalsIgnoreCase("V"))
//				{
//					mapDfrRmsExports = M9kStationXMLUtil.getMapOfDfrRmsExports();
//				}
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
		ComtradeDataDTO comtradeMagnitudeDataDTO = null;
		ComtradeDataDTO comtradePhaseDataDTO = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		Map<Integer,ComtradeDataDTO> mapComtradeContMagnitudeDat = new HashMap<Integer, ComtradeDataDTO>();
		Map<Integer,ComtradeDataDTO> mapComtradeContPhaseDat = new HashMap<Integer, ComtradeDataDTO>();
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
			+ "and tsLast <= ? and measurementType = ?) order by expId, tsLast";

//		String sqlQuery = "insert into long_term_dat SELECT expId, name, phase, units, tsLast, sampleRate, sampleCnt, scale, offset, if ((@start := (sampleCnt-(tsLast - ?))  * sampleRate) >= 0, @start, 0) as startSampleNo, " +
//		"(sampleCnt-(tsLast - ?))  * sampleRate)) as endSampleNo,data FROM `m9000`.`contAnalog` " +
//		"where (((? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast)  or (? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast ) ) " +
//		"or ((tsLast>= ? and ? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? >= tsLast )))";
		try {
			Map<Integer, M9kKeyValuePair> mapMaxValues = getMaxValue(sqlQuery);

			logger.debug("DB-POOL New connection from processMergeAndInsertLongTermData ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Cont-Data-Log: Start time "+ltrStartTime+" Stop time "+ltrStopTime );
			logger.debug("Cont-Data-Log: sampleTime.longValue() "+sampleTtimeVal);
			logger.debug("Cont-Data-Log: SQL query to be executed "+ sqlQuery);
			ps = mysqlConn.prepareStatement(sqlQuery);
			ps.setLong(++i, (ltrStartTime));
			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStartTime));
			ps.setLong(++i, (ltrStopTime+(getExportBufferTime() * 1000000)));
			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStopTime+(getExportBufferTime() * 1000000)));
			ps.setString(++i, dataType);
			rs = ps.executeQuery();

			InputStream is;
			StringBuffer magnitudeChannelInfoBuffer;
			StringBuffer phaseChannelInfoBuffer;
//			StringBuffer data;
			InputStream[] isData;
			double sampleRate;
			int sampleCnt = 0;
			int startSampleNo = -1;
			int endSampleNo = -1;
			int expId;
			long startSampleTime = 0;
			long lastSampleTime = 0;
			Double missingSampleCnt;
			float magScaleFactor = 0; // scale factor for magnitude
			float phaseScaleFactor = 0; // scale factor for phase
			String analogChnlName;
			int dfrId;
//			int exportChnlId;
//			Integer rmsExpId;
//			rs.first();
			while (rs.next())
			{
				dfrId = rs.getInt("recId");
				expId = rs.getInt("expId");
				
//				expId = expId - M9kStationConstants.LTR_RMS_OFFSET; // Removing the offset added to export Id during the rms export
				analogChnlName = getAnalogNameStrippingIndexPrefix(rs.getString("name"));
//				logger.debug("Cont-Data-Debug: getMapAnalogRanges() "+getMapAnalogRanges()+ "Dfr Id "+dfrId+ "ExpId "+expId+" Name "+analogChnlName);
//				if (dataUnits.equalsIgnoreCase("V"))
//				{
//					if (mapDfrRmsExports.get(dfrId) == null)
//					{
//						// No dfrId found in the config
//						logger.warn("Dfr Id not configured "+dfrId);
//						continue;
//					}
//					rmsExpId = mapDfrRmsExports.get(dfrId).get(analogChnlName); 
//					if (rmsExpId == null || rmsExpId != expId)
//					{
//						// Not a rms export
//						continue;
//					}
//				}
//				System.out.println("row cnt..."+rs.getRow());
				sampleCnt = rs.getInt("sampleCnt");
//				logger.debug("Cont-Data-Log: Sample Cnt "+sampleCnt);
				sampleRate = rs.getDouble("sampleRate");
//				logger.debug("Cont-Data-Log: sampleRate "+sampleRate);
				is = rs.getBinaryStream("data");
				lastSampleTime = rs.getLong("tsLast");
//				logger.debug("Cont-Data-Log: lastSampleTime "+lastSampleTime);
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
				logger.debug("CONT-PHASOR: Start sample No "+startSampleNo);
				
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
				logger.debug("CONT-PHASOR: End sample No "+endSampleNo);
				
//				startSampleTime = rs.getLong("StartSampleTime");
//				logger.debug("Cont-Data-Log: from DB startSampleTime"+startSampleTime);
				startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
				logger.debug("CONT-PHASOR: calculated startSampleTime"+startSampleTime);
				currentExpIdProcessed = expId;
				if (mapComtradeContMagnitudeDat.get(expId) == null)
				{
					logger.debug("CONT-PHASOR: Start of the sample....for expId "+expId+" ->" + startSampleNo);
//					if (rs.isLast())
//					{
						sampleCnt=endSampleNo-startSampleNo+1;
						logger.debug("CONT-PHASOR: Last row to be processed. End Sample..for expId "+expId+" ->" + endSampleNo+" Actual sampleCnt.."+sampleCnt);
//					}
//					iCnt = 1;
					comtradeMagnitudeDataDTO = new ComtradeDataDTO();
					comtradePhaseDataDTO = new ComtradeDataDTO();
					comtradeMagnitudeDataDTO.setTsLast( lastSampleTime);
					comtradePhaseDataDTO.setTsLast( lastSampleTime);
					if (ltrStartTime < startSampleTime) // Missing data at the start
					{
						// Construct dummy data for initial missing data
						logger.debug("Missing data at the start");
						missingSampleCnt = Math.floor(((startSampleTime - ltrStartTime )/1000000.0)*sampleRate);
						isData = constructDummyContinuousData(missingSampleCnt.intValue());
						comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
						comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
						isData[0].close();
						isData[1].close();
					}
//					scaleFactor = (32767.0f / getMapAnalogRanges().get(expId));
//					if (analogChnlName.indexOf(M9kStationConstants.VIRTUAL_ANALOG) != -1)
//					{
//						exportChnlId =  Integer.parseInt(analogChnlName.substring(M9kStationConstants.VIRTUAL_ANALOG.length()));
//					}
//					else if (analogChnlName.indexOf(M9kStationConstants.ANALOG) != -1)
//					{
//						exportChnlId =  Integer.parseInt(analogChnlName.substring(M9kStationConstants.ANALOG.length()));
//					}
//					else
//					{
//						exportChnlId =  Integer.parseInt(analogChnlName.substring(M9kStationConstants.LG_PREFIX.length()+1));
//					}
					if (mapMaxValues == null)
					{
						magScaleFactor = (32767.0f / getMapAnalogRanges().get(analogChnlName));
						phaseScaleFactor = (32767.0f / 360);
					}
					else
					{
						if (mapAnalogsRange == null && mapAnalogsRange.isEmpty())
						{
							mapAnalogsRange = getAnalogChnlsRanges();
						}
						magScaleFactor = (float) (32767.0f / Double.parseDouble(mapMaxValues.get(expId).getKey()));
						phaseScaleFactor = (float) (32767.0f / Double.parseDouble(mapMaxValues.get(expId).getValue()));
					}
					logger.debug("ExpId "+expId+" mag scale factor "+magScaleFactor+" phase scale factor "+phaseScaleFactor);
					comtradeMagnitudeDataDTO.setScaleFactor(magScaleFactor);
					comtradePhaseDataDTO.setScaleFactor(phaseScaleFactor);
//					logger.debug("\n\n\t\t Create cont data for expId "+expId);
					expCnt = expId;
//					logger.debug("\n\n\t\t Done for expId "+expId);
					comtradeMagnitudeDataDTO.setExportId(expId); // Just a value to use in file name creation
					comtradePhaseDataDTO.setExportId(expId); // Just a value to use in file name creation
					comtradeMagnitudeDataDTO.setStationId(stationDetail.getSystemStationId());
					comtradePhaseDataDTO.setStationId(stationDetail.getSystemStationId());
					logger.debug("\t\t\t\t\t\t Station Name "+stationDetail.getSystemStationName());
					comtradeMagnitudeDataDTO.setStationName(stationDetail.getSystemStationName());
					comtradePhaseDataDTO.setStationName(stationDetail.getSystemStationName());
					comtradeMagnitudeDataDTO.setDfrId(dfrId);
					comtradePhaseDataDTO.setDfrId(dfrId);
//					comtradeMagnitudeDataDTO.setExportChnlId(exportChnlId);
//					comtradePhaseDataDTO.setExportChnlId(exportChnlId);
					comtradeMagnitudeDataDTO.setRecordingDevId(stationDetail.getSystemRecordingDeviceId());
					comtradePhaseDataDTO.setRecordingDevId(stationDetail.getSystemRecordingDeviceId());
					// Commented as the sequence index, first column of cfg is added wgile creating cfg file
//					channelInfoBuffer = new StringBuffer(totalChannelCnt++ +M9kStationConstants.COMMA_SEPERATOR);
					magnitudeChannelInfoBuffer = new StringBuffer();
					magnitudeChannelInfoBuffer.append("A"+expId+M9kStationConstants.COMMA_SEPERATOR);
					magnitudeChannelInfoBuffer.append(rs.getString("phase")+M9kStationConstants.COMMA_SEPERATOR);
					magnitudeChannelInfoBuffer.append(rs.getString("name")+M9kStationConstants.COMMA_SEPERATOR);
					magnitudeChannelInfoBuffer.append("V"+M9kStationConstants.COMMA_SEPERATOR);
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
					phaseChannelInfoBuffer.append("Phase for "+rs.getString("name")+M9kStationConstants.COMMA_SEPERATOR);
					phaseChannelInfoBuffer.append("V"+M9kStationConstants.COMMA_SEPERATOR);
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
					
					isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo, magScaleFactor, phaseScaleFactor);
					comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
					comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
					isData[0].close();
					isData[1].close();
					mapComtradeContMagnitudeDat.put(expId, comtradeMagnitudeDataDTO);
					mapComtradeContPhaseDat.put(expId, comtradePhaseDataDTO);
//					if ((ltrStopTime - ltrStartTime) > ((stationDetail.getLtrPrefaultTime()*1000000) + (stationDetail.getLtrPostfaultTime()*1000000)))
//					{
//						comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getLtrPrefaultTime()*1000000));
//					}
//					else
//					{
//					}
				}
				else
				{
					comtradeMagnitudeDataDTO = mapComtradeContMagnitudeDat.get(expId);
					comtradePhaseDataDTO = mapComtradeContPhaseDat.get(expId);
//					System.out.println("Comtrade Data DTO "+comtradeDataDTO);
//					logger.debug("CONT-PHASOR: start Time "+startSampleTime+" Last time of the previoud record "+comtradeDataDTO.getTsLast()+" previoud Last Time +1 "+ (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)));
					if (startSampleTime > (comtradeMagnitudeDataDTO.getTsLast() +((1/sampleRate)*1000000)) ) // First sample should be Last sample + 1 of the previous record
					{
						missingSampleCnt = Math.floor(((startSampleTime - comtradeMagnitudeDataDTO.getTsLast() )/1000000.0)*sampleRate);
						isData = constructDummyContinuousData(missingSampleCnt.intValue());
						comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
						comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
						isData[0].close();
						isData[1].close();
					}
					comtradeMagnitudeDataDTO.setTsLast(lastSampleTime);
					comtradePhaseDataDTO.setTsLast(lastSampleTime);
//					startSampleNo = -1;
//					if (rs.isLast())
//					{
//						logger.debug("CONT-PHASOR: processing expId "+expId); 
//						endSampleNo = rs.getInt("endSampleNo");
//						System.out.println("Cont-Data-Log:  StartSampleNo..."+startSampleNo+" End Sample No "+endSampleNo+" scale factor "+comtradeDataDTO.getScaleFactor());
//						logger.debug("\n\n\t\t else part Create cont data for expId "+expId);
						isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo,comtradeMagnitudeDataDTO.getScaleFactor(), comtradePhaseDataDTO.getScaleFactor());
//						logger.debug("\n\n\t\t else part create done for expId "+expId);
						 
						sampleCnt=(endSampleNo-startSampleNo+1)+comtradeMagnitudeDataDTO.getSampleCnt();
//						logger.debug("Cont-Data-Log: sample count for expId "+expId+" is "+sampleCnt );
//					}
//					else
//					{
//						logger.debug("CONT-PHASOR: Still processing for expId "+expId); 
//						isData = constructContinuousData(is, sampleRate);
//						sampleCnt+=comtradeDataDTO.getSampleCnt()-1;
//						logger.debug("CONT-PHASOR: sample count as of now for expId "+expId+" is "+sampleCnt );
//					}
					
					comtradeMagnitudeDataDTO.setSampleCnt(sampleCnt);
					comtradePhaseDataDTO.setSampleCnt(sampleCnt);
//					comtradeDataDTO.setBinaryDataSteam(new SequenceInputStream(comtradeDataDTO.getBinaryDataSteam(), isData));
					comtradeMagnitudeDataDTO.setBinaryDataSteam(isData[0]);
					comtradePhaseDataDTO.setBinaryDataSteam(isData[1]);
					isData[0].close();
					isData[1].close();
//					comtradeDataDTO.setTsPrefault(ltrStartTime);
//					comtradeDataDTO.setTsTrigger(ltrStartTime+stationDetail.getLtrPrefaultTime());
				}
				if (sampleCnt > finalMergedSampleCnt)
				{
					finalMergedSampleCnt = sampleCnt;
					logger.debug("CONT-PHASOR: finalMergedSampleCnt "+finalMergedSampleCnt);
				}
				is.close();
			}
			if(!mapComtradeContMagnitudeDat.isEmpty())
			{
//				createDummyDataForMissingExports(mapComtradeContDat);
				lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContMagnitudeDat.values());
//				insertIntoLtr(lstComtradeDataDtos);
				
//				logger.debug("Before sort "+lstComtradeDataDtos);
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
//				logger.debug("After sort "+lstComtradeDataDtos);
				
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
				logger.debug("Final list of phasor data dtos "+lstComtradeDataDtos);
				TextMessage response = session.createTextMessage();
				try {
					comtradeData.createContinuousDataFiles(destFileName, lstComtradeDataDtos);
					response.setText(comtradeData.getContDataFileName());
	                response.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
	                this.replyProducer.send(receivedMessage.getJMSReplyTo(), response);
				} catch (Exception e) {
					logger.error("Error in fetching Continuous data ", e);
					response.setText("ERROR: Failed to create a comtrade record due to invalid data");
	                response.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
	                this.replyProducer.send(receivedMessage.getJMSReplyTo(), response);
					throw new M9000Exception(e);
				}
				mySqlContinuousComtradeDAO.insertContinuousComtradeDetails(dataType, getRequiredDataFormat("yyyy-MM-dd HH:mm:ss",startDate), getRequiredDataFormat("yyyy-MM-dd HH:mm:ss",endDate), comtradeData.getContDataFileName());
			}
			else
			{
				response.setText("ERROR: The list appears to be empty");
                response.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
                this.replyProducer.send(receivedMessage.getJMSReplyTo(), response);
			}
			logger.debug("Size of analog cont..."+lstComtradeDataDtos.size());
			getLstMissingExportsId().clear();
			getMapAnalogRanges().clear();
			lstComtradeDataDtos.clear();
			mapComtradeContMagnitudeDat.clear();
			
		} catch (SQLException e) {
			logger.error("Error in fetching Continuous data ", e);
			TextMessage response;
			try {
				response = session.createTextMessage();
				response.setText("ERROR: Failed to create a comtrade record due to invalid data. Reason: "+e.getMessage());
	            response.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
	            this.replyProducer.send(receivedMessage.getJMSReplyTo(), response);
			} catch (JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Error in fetching Continuous data ", e);
			try
			{
			TextMessage response = session.createTextMessage();
			response.setText("ERROR: Failed to create a comtrade record due to invalid data. Reason: "+e.getMessage());
            response.setJMSCorrelationID(receivedMessage.getJMSCorrelationID());
            this.replyProducer.send(receivedMessage.getJMSReplyTo(), response);
			throw new M9000Exception(e);
			} catch (JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

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

	private InputStream[] constructContinuousData(InputStream is, double sampleRate, int fromSampleNo, int endSampleNo, double magScaleFactor, float phaseScaleFactor) throws M9000Exception
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


	private InputStream[] constructDummyContinuousData(int totalSamplesMissing)
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
		M9kContPhasorDataFromDb m9kLtrRmsFromDb = new M9kContPhasorDataFromDb("2013-05-21 10:33:00", "2013-05-21 10:35:00");
		m9kLtrRmsFromDb.processLTR();
	}
	private String getRequiredDataFormat(String requiredFormat, Date dateToFormat)
	{
		String convertedDateFormat = null;
		SimpleDateFormat requiredDateFormat = new SimpleDateFormat(requiredFormat);
		requiredDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		convertedDateFormat = requiredDateFormat.format(dateToFormat);
		return convertedDateFormat;
	}
	
	// Get angle.
    public Double getAngle(float re, float im){
        double X = Math.toDegrees(Math.atan( im / re));
        if(re >= 0 && im > 0){
            return X;
        }else if((re < 0 && im < 0) || (re < 0 && im > 0)){
            return X + 180;
        }else{
            return X + 360;
        }
    }
 
    // Calculate magnitude.
    public Double getMagnitude(float re, float im	){
        return Math.sqrt(Math.pow(re, 2) + Math.pow(im, 2));
    }
    
    // Calculated max value for scale factor calculation
    private Map<Integer, M9kKeyValuePair> getMaxValue(String sqlQuery) 
    {
    	Map<Integer, M9kKeyValuePair> mapMaxValues= new HashMap<Integer, M9kKeyValuePair>();
    	InputStream is;
		PreparedStatement ps = null;
		ResultSet rs= null;
		int i = 0;

    	DataInputStream dis = null;
    	float real;
    	float imaginary;
		double magVal;
		double phaseVal;
		double maxMagVal = 1.0f;
		double maxPhaseVal = 1.0f;
		int expId = 0;

    	try
    	{
    		mysqlConn = M9kStationDBUtil.getLocalConnection();
    		ps = mysqlConn.prepareStatement(sqlQuery);
			ps.setLong(++i, (ltrStartTime));
			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStartTime));
			ps.setLong(++i, (ltrStopTime+(getExportBufferTime() * 1000000)));
			logger.debug("Cont-Data-Log: param "+i+" - "+(ltrStopTime+(getExportBufferTime() * 1000000)));
			ps.setString(++i, dataType);
			rs = ps.executeQuery();
	    	while (rs.next())
			{
				is = rs.getBinaryStream("data");
				dis = new DataInputStream(is);
				expId = rs.getInt("expId");
				if (mapMaxValues.get(expId) == null)
				{
					mapMaxValues.put(expId, new M9kKeyValuePair("1", "1"));
				}
				while (dis.available() > 0)
				{
					real = dis.readFloat();
					imaginary = dis.readFloat();
					maxMagVal = Double.parseDouble(mapMaxValues.get(expId).getKey());
					magVal = Math.hypot(real, imaginary) ;
					if (maxMagVal < magVal)
					{
						mapMaxValues.get(expId).setKey(""+magVal);
					}
					maxPhaseVal = Double.parseDouble(mapMaxValues.get(expId).getValue());
					phaseVal =  Math.toDegrees(Math.atan2(imaginary, real));
					if (maxPhaseVal < phaseVal)
					{
						mapMaxValues.get(expId).setValue(""+phaseVal);
					}
					
				}
				logger.debug("Max value map "+mapMaxValues);
				dis.close();

			}
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
					logger.debug("DB-POOL Closing connection from max values from complex continuous ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}

    	}
    	return mapMaxValues;
    }
}
