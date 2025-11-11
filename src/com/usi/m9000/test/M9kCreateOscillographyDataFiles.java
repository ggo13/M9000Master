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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.station.commands.M9kCreateContinuousComtradeFilesFromDB;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;
import com.usi.m9000.util.M9kConstants;


public class M9kCreateOscillographyDataFiles implements Runnable {
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

	String destFileName;
	int dataLengthInSeconds = 60; // for Utility purpose that uses this to create continuous files in loops.
	public M9kCreateOscillographyDataFiles(long startTime, long stopTime)
	{
		System.out.println("Entered M9kContAnalogFromDb constructor with start time "+startTime+" stop time "+stopTime);
		stationDetail = M9kStationDBUtil.getStationDetails();
//		ltrStartTime = startTime-(stationDetail.getPrefaultTime()*1000);
//		ltrStopTime = stopTime+(stationDetail.getPostfaultTime()*1000);
		ltrStartTime = startTime;
		ltrStopTime = stopTime;
		System.out.println("M9kContAnalogFromDb: After prefault Time start time "+ltrStartTime+" stop time "+ltrStopTime);
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
			System.out.println("Unable to process ContAnalog Data "+ e);
			return;
		}
	}

	public M9kCreateOscillographyDataFiles(String startTime, String stopTime)
	{
		try
		{
			System.out.println("Entered M9kContAnalogFromDb constructor with start time "+startTime+" stop time "+stopTime);
			stationDetail = M9kStationDBUtil.getStationDetails();
	//		ltrStartTime = startTime-(stationDetail.getSystemPrefaultTime()*1000);
	//		ltrStopTime = stopTime+(stationDetail.getSystemPostfaultTime()*1000);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			ltrStartTime = sdf.parse(startTime).getTime()*1000;
			ltrStopTime = sdf.parse(stopTime).getTime()*1000;
			System.out.println("M9kContAnalogFromDb: After prefault Time start time "+ltrStartTime+" stop time "+ltrStopTime);
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
			System.out.println("Unable to process ContAnalog Data "+ e);
			return;
		}
	}

	public M9kCreateOscillographyDataFiles(String startTime, String stopTime, int dataLengthInSeconds)
	{
		this(startTime, stopTime);
		this.dataLengthInSeconds = dataLengthInSeconds;
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
			System.out.println("Total wait time for ContAnalog Data " + (end-start));
//			System.out.println("Total wait time for ContAnalog Data " + (end-start));
//			System.out.println("Connection obj "+mysqlConn);


			// For debugging. Comment it later
//			prepareAndUpdateLongTermData();

			processMergeAndInsertLongTermData();


		} catch (Exception e) {
			System.out.println("Exception occured in processLTR "+ e);
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
		System.out.println("Oscillography sample count "+oscSampleCnt);
		Double sampleTime = (((oscSampleCnt - 1)/stationDetail.getSystemSampleRate()) * 1000000);
		System.out.println("Sample time in double "+sampleTime);
		long sampleTtimeVal = sampleTime.longValue();
		System.out.println("SampleTimeVal long "+sampleTtimeVal);
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
			System.out.println("DB-POOL New connection from processMergeAndInsertLongTermData ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			System.out.println("ContAnalog-Data-Debug: Start time "+ltrStartTime+" Stop time "+ltrStopTime );
			System.out.println("ContAnalog-Data-Debug: sampleTime.longValue() "+sampleTtimeVal);
			System.out.println("ContAnalog-Data-Debug: SQL query to be executed "+ sqlQuery);
			ps = mysqlConn.prepareStatement(sqlQuery);
			ps.setLong(++i, (ltrStartTime));
			System.out.println("ContAnalog-Data-Debug: param "+i+" - "+(ltrStartTime));
			ps.setLong(++i, (ltrStopTime+(getExportAnalogBufferTime() * 1000000)));
			System.out.println("ContAnalog-Data-Debug: param "+i+" - "+(ltrStopTime+(getExportAnalogBufferTime() * 1000000)));
//			ps.setLong(++i, ltrStartTime+sampleTtimeVal);
//			System.out.println("ContAnalog-Data-Debug: param "+i+" - "+(ltrStartTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStartTime);
//			System.out.println("ContAnalog-Data-Debug: param "+i+" - "+(ltrStartTime));
//			ps.setLong(++i, ltrStopTime+sampleTtimeVal);
//			System.out.println("ContAnalog-Data-Debug: param "+i+" - "+(ltrStopTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStopTime);
//			System.out.println("ContAnalog-Data-Debug: param "+i+" - "+(ltrStopTime));
//			ps.setLong(++i, ltrStartTime);
//			System.out.println("ContAnalog-Data-Debug: param "+i+" - "+(ltrStartTime));
//			ps.setLong(++i, ltrStopTime+sampleTtimeVal);
//			System.out.println("ContAnalog-Data-Debug: param "+i+" - "+(ltrStopTime+sampleTtimeVal));
//			ps.setLong(++i, ltrStopTime);
//			System.out.println("ContAnalog-Data-Debug: param "+i+" - "+(ltrStopTime));
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
			while (rs.next())
			{
//				System.out.println("row cnt..."+rs.getRow());
				sampleCnt = rs.getInt("sampleCnt");
				System.out.println("ContAnalog-Data-Debug: Sample Cnt "+sampleCnt);
				sampleRate = rs.getDouble("sampleRate");
				System.out.println("ContAnalog-Data-Debug: sampleRate "+sampleRate);
				expId = rs.getInt("expId");
				System.out.println("ContAnalog-Data-Debug: expId "+expId);
				is = rs.getBinaryStream("data");
				lastSampleTime = rs.getLong("tsLast");
				System.out.println("ContAnalog-Data-Debug: lastSampleTime "+lastSampleTime);
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
				System.out.println("ContAnalog-Data-Debug: Start sample No "+startSampleNo);

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
				System.out.println("ContAnalog-Data-Debug: End sample No "+endSampleNo);

//				startSampleTime = rs.getLong("StartSampleTime");
//				System.out.println("ContAnalog-Data-Debug: from DB startSampleTime"+startSampleTime);
				startSampleTime = (long) (lastSampleTime -(((sampleCnt-1)/sampleRate) * 1000000));
				System.out.println("ContAnalog-Data-Debug: calculated startSampleTime"+startSampleTime);

				if (mapComtradeContDat.get(expId) == null)
				{
//					System.out.println("ContAnalog-Data-Debug: Start of the sample....for expId "+expId+" ->" + startSampleNo);
//					if (rs.isLast())
//					{
						sampleCnt=endSampleNo-startSampleNo+1;
//						System.out.println("ContAnalog-Data-Debug: Last row to be processed. End Sample..for expId "+expId+" ->" + endSampleNo+" Actual sampleCnt.."+sampleCnt);
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
//					comtradeDataDTO.setTsTrigger(ltrStartTime+(stationDetail.getPrefaultTime()*1000)+(stationDetail.getLtrPrefaultTime()*1000000));
					comtradeDataDTO.setTsTrigger(ltrStartTime);
				}
				else
				{
					comtradeDataDTO = mapComtradeContDat.get(expId);
//					System.out.println("Comtrade Data DTO "+comtradeDataDTO);
//					System.out.println("ContAnalog-Data-Debug: start Time "+startSampleTime+" Last time of the previoud record "+comtradeDataDTO.getTsLast()+" previoud Last Time +1 "+ (comtradeDataDTO.getTsLast() +((1/sampleRate)*1000000)));
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
//						System.out.println("ContAnalog-Data-Debug: processing expId "+expId);
//						endSampleNo = rs.getInt("endSampleNo");
					System.out.println("ContAnalog-Data-Debug:  StartSampleNo..."+startSampleNo+" End Sample No "+endSampleNo);
						isData = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo);

						sampleCnt=(endSampleNo-startSampleNo+1)+comtradeDataDTO.getSampleCnt();
						System.out.println("ContAnalog-Data-Debug: sample count for expId "+expId+" is "+sampleCnt );
//					}
//					else
//					{
//						System.out.println("ContAnalog-Data-Debug: Still processing for expId "+expId);
//						isData = constructContinuousData(is, sampleRate);
//						sampleCnt+=comtradeDataDTO.getSampleCnt()-1;
//						System.out.println("ContAnalog-Data-Debug: sample count as of now for expId "+expId+" is "+sampleCnt );
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
					System.out.println("ContAnalog-Data-Debug: finalMergedSampleCnt "+finalMergedSampleCnt);
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
				comtradeData.createContinuousDataFiles(destFileName, lstComtradeDataDtos);
			}
			System.out.println("Size of analog cont..."+lstComtradeDataDtos.size());
			getLstMissingExportsId().clear();
			getLstAnalogExportsId().clear();
			lstComtradeDataDtos.clear();
			mapComtradeContDat.clear();

		} catch (SQLException e) {
			System.out.println("Error in fetching continuous Analog data "+ e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			System.out.println("Error in fetching continuous Analog data "+ e);
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
					System.out.println("DB-POOL Closing connection from processMergeAndInsertLongTermData ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (Exception e) {
				System.out.println("Error in cleanup"+ e);
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
//		System.out.println("ContAnalog-Data-Debug: byte array size "+bais.size());
		return new ByteArrayInputStream(bais.toByteArray());

	}

	private InputStream constructDummyContinuousData(int totalSamplesMissing)
	{
//		StringBuffer data = new StringBuffer();
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		Integer iDummyHexData = 0x8000;

		try {

//		System.out.println("ContAnalog-Data-Debug-DUMMY: Sample cnt of those missing "+totalSamplesMissing);
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
//		System.out.println("ContAnalog-Data-Debug-DUMMY: byte array size "+bais.size());
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
		String startTime;
		String stopTime;
		int sizeInSeconds;
		Date dateStartTime;
		Date intermediateDateStopTime;
		Date dateStopTime;
		boolean toContinue = true;
		M9kCreateOscillographyDataFiles M9kContAnalogFromDb;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (args.length < 2)
		{
			System.out.println("Invalid arguments. Enter start and stop timestamp in the format like 2014-09-15 15:00:00");
			return;
		}
		try {
			dateStartTime = sdf.parse(args[0]);
			dateStopTime = sdf.parse(args[1]);
			startTime = sdf.format(dateStartTime);
			stopTime = sdf.format(dateStopTime);
		} catch (ParseException e) {
			System.out.println("Invalid arguments. Enter start and stop timestamp in the format like 2014-09-15 15:00:00");
			e.printStackTrace();
			return;
		}

		if (args.length == 2)
		{
			sizeInSeconds = 60;
			System.out.println("dateStartTime "+dateStartTime);
			System.out.println("dateStopTime "+dateStopTime);
			System.out.println("startTime "+startTime);
			System.out.println("stopTime "+stopTime);


//			M9kContAnalogFromDb = new M9kContAnalogFromDb("2014-09-15 15:00:00", "2014-09-15 15:00:05");
//			M9kContAnalogFromDb = new M9kContAnalogFromDb(args[0], args[1]);
		}
		else
		{
//			M9kContAnalogFromDb = new M9kContAnalogFromDb(args[0], args[1],Integer.parseInt(args[2]));
			sizeInSeconds = Integer.parseInt(args[2]);
		}
		Calendar calendar=Calendar.getInstance();
//		System.out.println("Added 60 seconds "+intermediateDateStopTime);
		while (true)
		{
			System.out.println("StartTime "+startTime);
			calendar.setTime(dateStartTime);
			calendar.add(Calendar.SECOND,sizeInSeconds-1);
			intermediateDateStopTime = calendar.getTime();
			if (intermediateDateStopTime.after(dateStopTime))
			{
				System.out.println("Adding "+sizeInSeconds+" seconds exceeds stop time. Stopped at stop time");
				intermediateDateStopTime = dateStopTime;
				toContinue = false;
			}
			stopTime = sdf.format(intermediateDateStopTime);
			System.out.println("stopTime "+stopTime);
			M9kContAnalogFromDb = new M9kCreateOscillographyDataFiles(startTime, stopTime);
			M9kContAnalogFromDb.processLTR();
			if (!toContinue)
			{
				System.out.println("Done.");
				break;
			}
			calendar.add(Calendar.SECOND, 1);
			dateStartTime = calendar.getTime();
			if (dateStartTime.after(dateStopTime) || dateStartTime.equals(dateStopTime))
			{
				System.out.println("Done");
				break;
			}
			startTime = sdf.format(dateStartTime);
		}
//		M9kContAnalogFromDb.processLTR();
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
}
