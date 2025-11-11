package com.usi.m9000.dao;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.station.util.M9kBackupUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class MySqlComtradeContAnalogDAO implements ComtradeContAnalogDAO {

//	Connection mysqlConn;
	private static int iCnt;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlComtradeContAnalogDAO.class);
	public MySqlComtradeContAnalogDAO()
	{
//		mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
	}
	@Override
	public List<WeakReference<ComtradeDataDTO>> getLstOfContinuousAnalogData(int stationId) throws M9000Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		List<WeakReference<ComtradeDataDTO>> lstComtradeDataDtos = new ArrayList<WeakReference<ComtradeDataDTO>>();
		WeakHashMap<Integer,WeakReference<ComtradeDataDTO>> mapComtradeContDat = new WeakHashMap<Integer, WeakReference<ComtradeDataDTO>>();
		StringBuffer strQueryBuf;
//		if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
		if (M9kUtils.isRemote())
		{
			strQueryBuf = new StringBuffer("Select * from contAnalog where stationId = ? ");	
		}
		else
		{
			strQueryBuf = new StringBuffer("Select * from contAnalog ");
		}
		try {
			
			connection = M9kMySqlDatabase.getInstance().getConnection();
//			stmt = connection.createStatement();
//			stmt.setFetchSize(Integer.MIN_VALUE);
			
//			rs = stmt.executeQuery("Select * from contAnalog where stationId ="+ stationId +" order by expId LIMIT 1");
			strQueryBuf.append("  order by expId LIMIT 1");
			ps = connection.prepareStatement(strQueryBuf.toString());
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				ps.setInt(1, stationId);
			}
			InputStream is;
			StringBuffer channelInfoBuffer;
			StringBuffer data;
			double sampleRate;
			Integer expId;
			int sampleCnt = 0;
			int iRowCnt = 1;
//			int calculatedValue;
			rs = ps.executeQuery();
			while (rs.next())
			{
				expId = rs.getInt("expId");
				sampleCnt = rs.getInt("sampleCnt");
				sampleRate = rs.getDouble("sampleRate");
				is = rs.getBinaryStream("data");
				if ((comtradeDataDTO = mapComtradeContDat.get(expId).get()) == null)
				{
					iCnt = 1;
					data = constructContinuousData(is, sampleRate);
					comtradeDataDTO = new ComtradeDataDTO();
					comtradeDataDTO.setStationId(stationId);
					comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
					comtradeDataDTO.setRecordingDevId(M9kUtils.getStationDetails().getSystemRecordingDeviceId());
					channelInfoBuffer = new StringBuffer(iRowCnt++ +M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getInt("expId")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("phase")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("name")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("units")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getFloat("scale")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getFloat("offset")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_CHNL_SKEW+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MIN+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MAX+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY_RATIO+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_SECONDARY_RATIO+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY);
					channelInfoBuffer.append(M9kConstants.NEWLINE);
					comtradeDataDTO.setAnalogs(channelInfoBuffer);
					comtradeDataDTO.setSampleRate(sampleRate);
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.setType(M9kConstants.ASCII);
					comtradeDataDTO.setTsPrefault(rs.getLong("tsLast"));
					comtradeDataDTO.setTsTrigger(rs.getLong("tsLast"));
					comtradeDataDTO.setData(data);
					mapComtradeContDat.put(expId, new WeakReference<ComtradeDataDTO>(comtradeDataDTO));
				}
				else
				{
					data = constructContinuousData(is, sampleRate);
					sampleCnt+=comtradeDataDTO.getSampleCnt();
					comtradeDataDTO.getData().append(data);
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.setTsPrefault(rs.getLong("tsLast"));
					comtradeDataDTO.setTsTrigger(rs.getLong("tsLast"));
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to get list of cont analog data ",e);
			throw new M9000Exception(e);
		} catch (Exception e) {
			logger.error("Failed to get list of cont analog data ",e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
				}
				if (ps != null)
				{
					ps.close();
				}
				if (connection != null)
				{
					connection.close();
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		lstComtradeDataDtos = new ArrayList<WeakReference<ComtradeDataDTO>>( mapComtradeContDat.values());
//		System.out.println("Size of analog cont..."+lstComtradeDataDtos.size());
		return lstComtradeDataDtos;
	}

	private StringBuffer constructContinuousData(InputStream is, double sampleRate)
	{
		DataInputStream dis = new DataInputStream(is);
		StringBuffer data = new StringBuffer();
		int realData;
//TODO: calculate micro seconds for data prefix	
		try {
		
		while (dis.available() > 0)
		{
			data.append((iCnt)+","+(int)((iCnt / sampleRate)*1000)+",");
			iCnt++;
			realData = dis.readShort();
//			System.out.println(calculatedValue);
			data.append(realData+M9kConstants.NEWLINE);
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
		return data;

	}
	
	@Override
	public List<WeakReference<ComtradeDataDTO>> getLstOfContinuousAnalogData(int stationId, int expId, String startDate,
			String endDate) throws M9000Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		List<WeakReference<ComtradeDataDTO>> lstComtradeDataDtos = new ArrayList<WeakReference<ComtradeDataDTO>>();
		WeakHashMap<Integer,WeakReference<ComtradeDataDTO>> mapComtradeContDat = new WeakHashMap<Integer, WeakReference<ComtradeDataDTO>>();
		StringBuffer strQueryBuf;
		int i = 0;
		try {
			
			connection = M9kMySqlDatabase.getInstance().getConnection();

//			stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
//			ps = connection.prepareStatement("SELECT sampleCnt, sampleRate, phase, name, units, type, tsLast, ((cast((tsLast/1000000) as unsigned) - unix_timestamp('"+startDate+"'))  * sampleRate) as startSampleNo, " +
//					"((cast((tsLast/1000000) as unsigned) - unix_timestamp('"+endDate+"'))  * sampleRate) as endSampleNo,data FROM `m9000`.`cont_view` " +
//					"where expId="+ expId +" and ((('"+startDate+"' >= firstSampleTime and '"+startDate+"' <= lastSampleTime)  or ('"+endDate+"' >= firstSampleTime and '"+endDate+"' <= lastSampleTime ) ) " +
//					"or ((lastSampleTime >= '"+startDate+"' and '"+endDate+"' >= firstSampleTime and '"+endDate+"' >= lastSampleTime )))");
			
//			String sqlQuery = "SELECT sampleCnt, sampleRate, phase, name, units, tsLast, scale, offset, (sampleCnt-((cast((tsLast/1000000) as unsigned) - unix_timestamp('"+startDate+"'))  * sampleRate)) as startSampleNo, " +
//			"(sampleCnt-((cast((tsLast/1000000) as unsigned) - unix_timestamp('"+endDate+"'))  * sampleRate)) as endSampleNo,data FROM `m9000`.`cont_analog_view` " +
//			"where stationId="+ stationId +" and expId="+ expId +" and ((('"+startDate+"' >= firstSampleTime and '"+startDate+"' <= lastSampleTime)  or ('"+endDate+"' >= firstSampleTime and '"+endDate+"' <= lastSampleTime ) ) " +
//			"or ((lastSampleTime >= '"+startDate+"' and '"+endDate+"' >= firstSampleTime and '"+endDate+"' >= lastSampleTime )))";
//			String sqlQuery = "SELECT sampleCnt, sampleRate, phase, name, units, tsLast, scale, offset, if ((@start := (sampleCnt-((cast((tsLast/1000000) as unsigned) - unix_timestamp('"+startDate+"'))  * sampleRate))) >= 0, @start, 0) as startSampleNo, " +
//			"(sampleCnt-((cast((tsLast/1000000) as unsigned) - unix_timestamp('"+endDate+"'))  * sampleRate)) as endSampleNo,data FROM `m9000`.`cont_analog_view` " +
//			"where stationId="+ stationId +" and expId="+ expId +" and ((('"+startDate+"' >= firstSampleTime and '"+startDate+"' <= lastSampleTime)  or ('"+endDate+"' >= firstSampleTime and '"+endDate+"' <= lastSampleTime ) ) " +
//			"or ((lastSampleTime >= '"+startDate+"' and '"+endDate+"' >= firstSampleTime and '"+endDate+"' >= lastSampleTime )))";
//			strQueryBuf = new StringBuffer("SELECT sampleCnt, sampleRate, phase, name, units, tsLast, scale, offset, if ((@start := (sampleCnt-((cast((tsLast/1000000) as unsigned) - unix_timestamp(?))  * sampleRate))) >= 0, @start, 0) as startSampleNo, ");
//			strQueryBuf.append("(sampleCnt-((cast((tsLast/1000000) as unsigned) - unix_timestamp(?))  * sampleRate)) as endSampleNo,data FROM `m9000`.`cont_analog_view` where ");
			strQueryBuf = new StringBuffer("SELECT sampleCnt, sampleRate, phase, name, units, tsLast, scale, offset,data FROM `m9000`.`cont_analog_view` where ");
			
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				strQueryBuf.append(" stationId=? and ");
			}
			strQueryBuf.append(" expId=? and (((? >= firstSampleTime and ? <= lastSampleTime)  or (? >= firstSampleTime and ? <= lastSampleTime ) ) ");
			strQueryBuf.append(" or ((lastSampleTime >= ? and ? >= firstSampleTime and ? >= lastSampleTime )))");
//			System.out.println("Sql query to be executed "+strQueryBuf.toString());
			ps = connection.prepareStatement(strQueryBuf.toString());
//			ps.setString(++i, startDate);
//			System.out.println("Start "+i+" - "+startDate);
//			ps.setString(++i, endDate);
//			System.out.println("End "+i+" - "+endDate);
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				ps.setInt(++i, stationId);
			}
			ps.setInt(++i, expId);
//			System.out.println("expId "+i+" - "+expId);
			ps.setString(++i, startDate);
//			System.out.println("Start "+i+" - "+startDate);
			ps.setString(++i, startDate);
//			System.out.println("Start "+i+" - "+startDate);
			ps.setString(++i, endDate);
//			System.out.println("End "+i+" - "+endDate);
			ps.setString(++i, endDate);
//			System.out.println("End "+i+" - "+endDate);
			ps.setString(++i, startDate);
//			System.out.println("Start "+i+" - "+startDate);
			ps.setString(++i, endDate);
//			System.out.println("End "+i+" - "+endDate);
			ps.setString(++i, endDate);
//			System.out.println("End "+i+" - "+endDate);
//			rs = stmt.executeQuery(sqlQuery);
			rs = ps.executeQuery();
			InputStream is;
			StringBuffer channelInfoBuffer;
			StringBuffer data;
			double sampleRate;
			double multFactor = 0.0001;
			int sampleCnt = 0;
			int startSampleNo = -1;
			int endSampleNo = -1;
			int iRowCnt = 1;
			long startSampleTimeEpoch;
			long endSampleTimeEpoch;
			long displayStartSampleTimeEpoch;
			long tsLast;

			logger.debug("Received startdate..."+startDate);
			logger.debug("Received startdate..."+endDate);
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
//			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			startSampleTimeEpoch = sdf.parse(startDate).getTime()/1000;
			logger.debug("startSampleTimeEpoch "+startSampleTimeEpoch);
			endSampleTimeEpoch = sdf.parse(endDate).getTime()/1000;
			displayStartSampleTimeEpoch = sdf.parse(M9kUtils.convertDateToDisplay(startDate)).getTime()*1000;
			logger.debug("Display Start ..."+M9kUtils.convertDateToDisplay(startDate));

			while (rs.next())
			{
				sampleCnt = rs.getInt("sampleCnt");
				sampleRate = rs.getDouble("sampleRate");
				tsLast = rs.getLong("tsLast");
				is = rs.getBinaryStream("data");
				if (mapComtradeContDat.get(expId) == null)
				{
//					startSampleNo = rs.getInt("startSampleNo");
//					if (tsLast > startSampleTimeEpoch)
//					{
						startSampleNo = sampleCnt - (int)(((tsLast/1000000) - startSampleTimeEpoch)*sampleRate);
						if (startSampleNo < 0)
						{
							startSampleNo = 0;
						}
//					}
//					else
//					{
//						startSampleNo = 0;
//					}
						logger.debug("Start of the sample...."+startSampleNo);
					if (rs.isLast())
					{
//						endSampleNo = rs.getInt("endSampleNo");
//						if (tsLast > endSampleTimeEpoch)
//						{
							endSampleNo = sampleCnt - (int) (((tsLast/1000000) - endSampleTimeEpoch) * sampleRate);
							if (endSampleNo < 0)
							{
								endSampleNo = sampleCnt;
							}
//						}
//						else
//						{
//							endSampleNo = sampleCnt;
//						}
							logger.debug("End of the sample...."+endSampleNo);
						sampleCnt=endSampleNo-startSampleNo+1;
//						System.out.println("Last row to be processed. End Sample.."+endSampleNo+" Actual sampleCnt.."+sampleCnt);
					}
					iCnt = 1;
					data = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo);
					comtradeDataDTO = new ComtradeDataDTO();
					comtradeDataDTO.setStationId(stationId);
					comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
					comtradeDataDTO.setRecordingDevId(M9kUtils.getStationDetails().getSystemRecordingDeviceId());
					channelInfoBuffer = new StringBuffer(iRowCnt++ +M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(expId+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("phase")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("name")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("units")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getFloat("scale")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getFloat("offset")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(multFactor+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_CHNL_OFFSET+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_CHNL_SKEW+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MIN+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_ANALOG_DATA_RANGE_MAX+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY_RATIO+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_SECONDARY_RATIO+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(M9kConstants.COMTRADE_PRIMARY);
					channelInfoBuffer.append(M9kConstants.NEWLINE);
					comtradeDataDTO.setAnalogs(channelInfoBuffer);
					comtradeDataDTO.setSampleRate(sampleRate);
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.setType(M9kConstants.ASCII);
					comtradeDataDTO.setData(data);
					mapComtradeContDat.put(expId, new WeakReference<ComtradeDataDTO>(comtradeDataDTO));
					comtradeDataDTO.setTsPrefault(displayStartSampleTimeEpoch);
					comtradeDataDTO.setTsTrigger(displayStartSampleTimeEpoch);
				}
				else
				{
					comtradeDataDTO = mapComtradeContDat.get(expId).get();

//					startSampleNo = -1;
					if (rs.isLast())
					{
//						endSampleNo = rs.getInt("endSampleNo");
//						System.out.println("Last row to be processed..."+endSampleNo);
//						if (tsLast > endSampleTimeEpoch)
//						{
							endSampleNo = sampleCnt - (int) ((tsLast/1000000 - endSampleTimeEpoch) * sampleRate);
							if (endSampleNo < 0)
							{
								endSampleNo = sampleCnt;
							}
//						}
//						else
//						{
//							endSampleNo = sampleCnt;
//						}
						data = constructContinuousData(is, sampleRate,-1, endSampleNo);
						 
						sampleCnt=endSampleNo-startSampleNo+comtradeDataDTO.getSampleCnt()+1;
					}
					else
					{
						data = constructContinuousData(is, sampleRate);
						sampleCnt+=comtradeDataDTO.getSampleCnt();
					}
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.getData().append(data);
					comtradeDataDTO.setTsPrefault(displayStartSampleTimeEpoch);
					comtradeDataDTO.setTsTrigger(displayStartSampleTimeEpoch);
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to get list of cont analog data ",e);
			throw new M9000Exception(e);
		} catch (ParseException e) {
			logger.error("Failed to get list of cont analog data ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get list of cont analog data ",e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
				}
				if (ps != null)
				{
					ps.close();
				}
				if (connection != null)
				{
					connection.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		lstComtradeDataDtos = new ArrayList<WeakReference<ComtradeDataDTO>>( mapComtradeContDat.values());
//		System.out.println("list of comtrade Size..."+lstComtradeDataDtos.size());
		return lstComtradeDataDtos;
	}

	@Override
	public List<WeakReference<ComtradeDataDTO>> getQuickSummaryOfContinuousAnalogData(int stationId) throws M9000Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs= null;
		PreparedStatement psInit = null;
		ResultSet rsInit= null;
		
		ComtradeDataDTO comtradeDataDTO = null;
		long startTime;
		long endTime;
		startTime = System.currentTimeMillis();
		List<WeakReference<ComtradeDataDTO>> lstComtradeDataDtos = new ArrayList<WeakReference<ComtradeDataDTO>>(100);
		Map<Integer, WeakReference<ComtradeDataDTO>> mapComtradeDataDtos = new HashMap<Integer, WeakReference<ComtradeDataDTO>>(100);
		StringBuffer strQueryBuf;
		StringBuffer strInitQuery;
		// Start: 31-Jan-2013 All date conversion are in M9kUtils class
//		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
//		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		java.text.SimpleDateFormat sdf  = M9kUtils.getDateFormat("yyyy-MM-dd HH:mm:ss");
		// End: 31-Jan-2013
		long startDateVal;
//		if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
		if (M9kUtils.isRemote())
		{
			strInitQuery = new StringBuffer("select expId,name, sampleCnt,sampleRate from contAnalog where stationId = ? group by expId ");
		}
		else
		{
			strInitQuery = new StringBuffer("select expId,name, sampleCnt,sampleRate from contAnalog group by expId ");
		}
		
		// Query split for performance
//		if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
		if (M9kUtils.isRemote())
		{
//			strQueryBuf = new StringBuffer("select expId, name, convert_tz(from_unixtime(MIN(tsLast)/1000000 - ((sampleCnt-1)/sampleRate)) ,@@global.time_zone,'+00:00')  as StartTime, convert_tz(from_unixtime(MAX(tsLast) div 1000000) ,@@global.time_zone,'+00:00')  as EndTime from contAnalog where stationId = ?");	
			strQueryBuf = new StringBuffer("select expId, MIN(tsLast) as StartTime, MAX(tsLast) as EndTime from contAnalog where stationId = ?");
		}
		else
		{
//			strQueryBuf = new StringBuffer("select expId, name, convert_tz(from_unixtime(MIN(tsLast)/1000000 - ((sampleCnt-1)/sampleRate)) ,@@global.time_zone,'+00:00')  as StartTime, convert_tz(from_unixtime(MAX(tsLast) div 1000000) ,@@global.time_zone,'+00:00')  as EndTime from contAnalog ");
			strQueryBuf = new StringBuffer("select expId, MIN(tsLast) as StartTime, MAX(tsLast) as EndTime from contAnalog");
		}
		strQueryBuf.append(" group by expId");
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			
			endTime = System.currentTimeMillis();
//			System.out.println("Total Time for DB connection..."+(endTime-startTime));
			psInit = connection.prepareStatement(strInitQuery.toString());
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				psInit.setInt(1, stationId);
			}
			rsInit = psInit.executeQuery();
			while (rsInit.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setStationId(stationId);
				comtradeDataDTO.setExportId(rsInit.getInt("expId")); 
				comtradeDataDTO.setExportName(rsInit.getString("name"));
				comtradeDataDTO.setSampleCnt(rsInit.getInt("sampleCnt"));
				comtradeDataDTO.setSampleRate(rsInit.getDouble("sampleRate"));
				mapComtradeDataDtos.put(rsInit.getInt("expId"), new WeakReference<ComtradeDataDTO>(comtradeDataDTO));
			}
			rsInit.close();
			psInit.close();
			// Execution of the second part of the query
			long startTime1 = System.currentTimeMillis();
//			ps = connection.prepareStatement("select expId, name, convert_tz(from_unixtime(MIN(tsLast)/1000000 - ((sampleCnt-1)/sampleRate)) ,@@global.time_zone,'+00:00')  as StartTime, convert_tz(from_unixtime(MAX(tsLast) div 1000000) ,@@global.time_zone,'+00:00')  as EndTime from contAnalog where stationId = "+stationId+" group by expId");
			ps = connection.prepareStatement(strQueryBuf.toString());
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				ps.setInt(1, stationId);
			}
//			ps = connection.prepareStatement("select A.expId, A.name, from_unixtime(A.tsLast/1000000 - (A.sampleCnt/A.sampleRate)) as StartTime, from_unixtime(B.tsLast div 1000000) as EndTime from contAnalog A, contAnalog B" +
//					"	where A.stationId = "+stationId+" and A.tsLast = (select MIN(tsLast) as StartTime from contAnalog where stationId = "+stationId+" ) and B.tsLast = (select MAX(tsLast) as StartTime from contAnalog where stationId = "+stationId+" ) group by A.expId");
			endTime = System.currentTimeMillis();
			logger.debug("Total Time for prepare statement ..."+(endTime-startTime1));
			
			startTime1 = System.currentTimeMillis();
			rs = ps.executeQuery();
			
			endTime = System.currentTimeMillis();
			logger.debug("Total Time for execution query..."+(endTime-startTime1));
			
			startTime1 = System.currentTimeMillis();
			while (rs.next())
			{
//				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO = mapComtradeDataDtos.get(rs.getInt("expId")).get();
//				comtradeDataDTO.setStationId(stationId);
//				comtradeDataDTO.setExportId(rs.getInt("expId"));
				startDateVal = rs.getLong("StartTime") - (((comtradeDataDTO.getSampleCnt()-1) / (int)comtradeDataDTO.getSampleRate())*1000000);
//				comtradeDataDTO.setExportName(rs.getString("name"));
				comtradeDataDTO.setExportStartTime(sdf.format(startDateVal/1000));
				comtradeDataDTO.setExportEndTime(sdf.format(rs.getLong("EndTime")/1000));
				lstComtradeDataDtos.add(new WeakReference<ComtradeDataDTO>(comtradeDataDTO));
			}
			mapComtradeDataDtos.clear();
			endTime = System.currentTimeMillis();
			logger.debug("Total Time for while loop..."+(endTime-startTime1));
		} catch (SQLException e) {
			logger.error("Failed to get quick summary of cont analog data ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get quick summary of cont analog data ",e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (rsInit != null)
				{
					rsInit.close();
				}
				if (rs != null)
				{
					rs.close();
				}
				if (psInit != null)
				{
					psInit.close();
				}
				if (ps != null)
				{
					ps.close();
				}
				if (connection != null)
				{
					connection.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		endTime = System.currentTimeMillis();
		logger.debug("Total Time for getQuickSummaryOfContinuousAnalogData..."+(endTime-startTime));
		return lstComtradeDataDtos;

	}

	private StringBuffer constructContinuousData(InputStream is, double sampleRate, int fromSampleNo, int endSampleNo)
	{
		DataInputStream dis = new DataInputStream(is);
		StringBuffer data = new StringBuffer();
		short sampleData;
//		double multFactor = 0.0001;
		int skipCnt = 0;
//TODO: calculate micro seconds for data prefix	
		try {
		
		while (dis.available() > 0)
		{
			if (fromSampleNo > 0)
			{
				while (skipCnt < fromSampleNo)
				{
					sampleData = dis.readShort();
					skipCnt++;
				}
			}
			if(endSampleNo != -1 && skipCnt > endSampleNo)
			{
				break;
			} 
			data.append((iCnt)+","+(int)((iCnt / sampleRate)*1000)+",");
			iCnt++;

			sampleData = dis.readShort();
//			System.out.println(calculatedValue);
			data.append(sampleData+M9kConstants.NEWLINE);
			skipCnt++;
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
		return data;

	}
	
	public static void main(String[] args)
	{
//		MySqlComtradeContAnalogDAO test = new MySqlComtradeContAnalogDAO();
//		test.getLstOfContinuousAnalogData(123);
//		test.getLstOfContinuousAnalogData(123,1, "2010-10-06 7:30:0", "2010-10-06 7:40:0");
	}
	@Override
	public int getTotalContCount(int stationId, String contDataType) throws M9000Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public List<ComtradeDataDTO> searchByCriteria(int stationId, String searchCriteria, String orderCriteria, int from,
			int to) throws M9000Exception {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int countByCriteria(int stationId, String searchCriteria) throws M9000Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public long getFirstAvailableTime() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuffer strQueryBuf;
		long firstAvailableTime = 0L;
//		strQueryBuf = new StringBuffer("select min(tsLast) as firstAvailableTime from contAnalog where date(from_unixtime(tsLast div 1000000)) = CURDATE()");
		strQueryBuf = new StringBuffer("select min(tsLast) as firstAvailableTime from contAnalog");
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			ps = connection.prepareStatement(strQueryBuf.toString());
			rs = ps.executeQuery();
			if (rs.next()) {
				firstAvailableTime = rs.getLong("firstAvailableTime");
			}
		} catch (Exception e) {
			logger.error("Failed to get first available time of cont analog data ", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (connection != null) {
					connection.close();
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (firstAvailableTime == 0L) {
			try {
				logger.info(
						"contAnalog table has no data and hence no first available time. Setting it to current hour.");
				firstAvailableTime = M9kBackupUtil.getCurrentWholeHourTime();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return firstAvailableTime;
	}

}
