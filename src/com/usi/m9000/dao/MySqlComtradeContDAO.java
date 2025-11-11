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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.station.util.M9kBackupUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class MySqlComtradeContDAO implements ComtradeContDAO {

//	Connection mysqlConn;
	private int iCnt;
	private Map<Integer, Map<Long, Float>> mapData;
	private Map<Long, Float> currentDataMap;
	private int totalSampleCnt;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlComtradeContDAO.class);
	
	public MySqlComtradeContDAO()
	{
	}
	@Override
	public List<WeakReference<ComtradeDataDTO>> getLstOfContinuousData(int stationId) throws M9000Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
//		DataInputStream dis = null;
		List<WeakReference<ComtradeDataDTO>> lstComtradeDataDtos;
		WeakHashMap<Integer,WeakReference<ComtradeDataDTO>> mapComtradeContDat = new WeakHashMap<Integer, WeakReference<ComtradeDataDTO>>();
		mapData = new HashMap<Integer, Map<Long,Float>>();
		StringBuffer strQueryBuf;
//		if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
		if (M9kUtils.isRemote())
		{
			strQueryBuf = new StringBuffer("Select * from cont_view where stationId = ? ");	
		}
		else
		{
			strQueryBuf = new StringBuffer("Select * from cont_view ");
		}
		strQueryBuf.append(" order by expId ");
		try {
			
//			ps = mysqlConn.prepareStatement("Select * from cont order by expId ");
			connection = M9kMySqlDatabase.getInstance().getConnection();

//			stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
//		              java.sql.ResultSet.CONCUR_READ_ONLY);
//			stmt.setFetchSize(Integer.MIN_VALUE);
			
//			rs = stmt.executeQuery("Select * from cont_view where stationId = "+ stationId +" order by expId ");
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
			double multFactor = 0.0001;
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
					comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
					comtradeDataDTO.setRecordingDevId(M9kUtils.getStationDetails().getSystemRecordingDeviceId());
					comtradeDataDTO.setStationId(stationId);
					channelInfoBuffer = new StringBuffer(iRowCnt +M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getInt("expId")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("phase")+M9kConstants.COMMA_SEPERATOR);
					// START: 15-Mar-2018 DDR Rms cfg file needs to have description instead of name of the channel
//					channelInfoBuffer.append(rs.getString("name")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("description")+M9kConstants.COMMA_SEPERATOR);
					// END: 15-Mar-2018 DDR Rms cfg file needs to have description instead of name of the channel
					channelInfoBuffer.append(rs.getString("units")+M9kConstants.COMMA_SEPERATOR);
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
					comtradeDataDTO.setDataType(rs.getString("type"));
					comtradeDataDTO.setType(M9kConstants.ASCII);
					comtradeDataDTO.setData(data);
//					System.out.println("Length of the data Before append... "+comtradeDataDTO.getData().length());
					mapComtradeContDat.put(expId, new WeakReference<ComtradeDataDTO>(comtradeDataDTO));
				}
				else
				{
					data = constructContinuousData(is, sampleRate);
					sampleCnt+=comtradeDataDTO.getSampleCnt();
					comtradeDataDTO.getData().append(data);
//					System.out.println("Length of the data after append... "+comtradeDataDTO.getData().length());
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.setTsPrefault(rs.getLong("tsLast"));
					comtradeDataDTO.setTsTrigger(rs.getLong("tsLast"));
//					comtradeDataDTO.setData(data);
				}
//				System.out.println("Export id...."+rs.getInt("expId"));
//				System.out.println("Name...."+rs.getString("name"));
//				System.out.println("TS ...."+rs.getLong("tsLast"));
//				System.out.println("Sample Rate...."+rs.getDouble("sampleRate"));
//				System.out.println("Sample Cnt...."+rs.getInt("sampleCnt"));
//				System.out.println("Type...."+rs.getString("type"));
//				ByteArrayOutputStream bos = new ByteArrayOutputStream();
//				dis = new DataInputStream(is);
//				data = new StringBuffer();
//				iCnt = 1;
//				float realData;
//				sampleRate = rs.getDouble("sampleRate");
//				System.out.println("\n\n\n\t\t\t************************* MicroSeconds..."+(rs.getRow() / rs.getDouble("sampleRate"))+"\n\n\n\t\t\t");
//// TODO: calculate micro seconds for data prefix	
//				FileWriter fw = new FileWriter(new File("C://M9kConfig/TestContinuous.dat"));
//				while (dis.available() > 0)
//				{
//					data.append((iCnt)+","+(int)((iCnt / sampleRate)*1000)+",");
//					iCnt++;
//					realData = dis.readFloat();
//					calculatedValue = (int)(realData/multFactor);
//					System.out.println(calculatedValue);
//					data.append(calculatedValue+M9kConstants.NEWLINE);
//					fw.write(""+realData);
//					fw.write(M9kConstants.NEWLINE);
//				}
//				fw.close();
//				dis.close();
//				comtradeDataDTO.setData(data);
//				lstComtradeDataDtos.add(comtradeDataDTO);
//				System.out.println();
			}
		} catch (SQLException e) {
			logger.error("Failed to get list of cont data ",e);
			throw new M9000Exception(e);
		}catch (Exception e) {
			logger.error("Failed to get list of cont data ",e);
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
			 catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		lstComtradeDataDtos = new ArrayList<WeakReference<ComtradeDataDTO>>( mapComtradeContDat.values());
		return lstComtradeDataDtos;
	}

	private StringBuffer constructContinuousData(InputStream is, double sampleRate)
	{
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataInputStream dis = new DataInputStream(is);
		StringBuffer data = new StringBuffer();
		int calculatedValue;
		float realData;
		double multFactor = 0.0001;
//TODO: calculate micro seconds for data prefix	
		try {
		
		while (dis.available() > 0)
		{
			data.append((iCnt)+","+(int)((iCnt / sampleRate)*1000)+",");
			iCnt++;
			realData = dis.readFloat();
			calculatedValue = (int)(realData/multFactor);
//			System.out.println(calculatedValue);
			data.append(calculatedValue+M9kConstants.NEWLINE);
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
	
	private StringBuffer constructContinuousData(InputStream is, double sampleRate, int fromSampleNo, int endSampleNo, long timestamp)
	{
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataInputStream dis = new DataInputStream(is);
		StringBuffer data = new StringBuffer();
//		int calculatedValue;
		float realData;
//		double multFactor = 0.0001;
		int actualSampleCnt = 0;
		int skipCnt = 0;
		long currentSampleTime = (long)(timestamp + (((actualSampleCnt-1)/sampleRate)*1000000));
//TODO: calculate micro seconds for data prefix	
		try {
		while (dis.available() > 0)
		{
			if (fromSampleNo > 0)
			{
				while (skipCnt < fromSampleNo)
				{
					realData = dis.readFloat();
					skipCnt++;
				}
			}
			if(endSampleNo != -1 && skipCnt > endSampleNo)
			{
				break;
			} 
			data.append((iCnt)+","+(int)((iCnt / sampleRate)*1000)+",");
			iCnt++;
//			currentSampleTime = (long)((timestamp/1000000.0 + ((actualSampleCnt-1)/sampleRate))*1000000);
			currentSampleTime = (long)(timestamp + (((actualSampleCnt-1)/sampleRate)*1000000));
//			if (actualSampleCnt == 0)
//			{
//				System.out.println("\t\t\tTimestamp..."+timestamp);
//				System.out.println("\t\t\tcurrentSampleTime ... "+currentSampleTime);
//			}
			actualSampleCnt++;
//			System.out.print(currentSampleTime+" , ");
			realData = dis.readFloat();
//			System.out.println("\t\tDebug: Real data "+realData);
//			calculatedValue = (int)(realData/multFactor);
//			System.out.println("\t\tDebug: calculated value "+calculatedValue);
//			currentDataMap.put(currentSampleTime, calculatedValue);
			currentDataMap.put(currentSampleTime, realData);
//			data.append(calculatedValue+M9kConstants.NEWLINE);
			data.append(realData+M9kConstants.NEWLINE);
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
//		System.out.println("actualSampleCnt..."+(actualSampleCnt-1));
		return data;

	}

	public static void main(String[] args)
	{
//		MySqlComtradeContDAO test = new MySqlComtradeContDAO();
//		ComtradeDataDTO comtradeDataDTO = test.getLstOfContinuousData(123, "1,2,6", "2010-10-25 7:47:36", "2010-10-25 7:47:45");
//		System.out.println(comtradeDataDTO.getAnalogs());
//		System.out.println("Done...");
//		System.exit(0);
	}
	@Override
	public List<WeakReference<ComtradeDataDTO>> getQuickSummaryOfContinuousData(int stationId) throws M9000Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs= null;
		PreparedStatement psInit = null;
		ResultSet rsInit= null;
		ComtradeDataDTO comtradeDataDTO = null;
		List<WeakReference<ComtradeDataDTO>> lstComtradeDataDtos = new ArrayList<WeakReference<ComtradeDataDTO>>();
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
			strInitQuery = new StringBuffer("select expId,name, sampleCnt,sampleRate, type from cont where stationId = ? group by expId ");
		}
		else
		{
			strInitQuery = new StringBuffer("select expId,name, sampleCnt,sampleRate, type from cont group by expId ");
		}
//		if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
		if (M9kUtils.isRemote())
		{
//			strQueryBuf = new StringBuffer("select expId, name, convert_tz(from_unixtime(MIN(tsLast)/1000000 - ((sampleCnt-1)/sampleRate)),@@global.time_zone,'+00:00') as StartTime, convert_tz(from_unixtime(MAX(tsLast) div 1000000),@@global.time_zone,'+00:00') as EndTime from cont_view where stationId = ? ");
			strQueryBuf = new StringBuffer("select expId, MIN(tsLast) as StartTime, MAX(tsLast) as EndTime from cont where stationId = ?");
		}
		else
		{
//			strQueryBuf = new StringBuffer("select expId, name, convert_tz(from_unixtime(MIN(tsLast)/1000000 - ((sampleCnt-1)/sampleRate)),@@global.time_zone,'+00:00') as StartTime, convert_tz(from_unixtime(MAX(tsLast) div 1000000),@@global.time_zone,'+00:00') as EndTime from cont_view ");
			strQueryBuf = new StringBuffer("select expId, MIN(tsLast) as StartTime, MAX(tsLast) as EndTime from cont");
		}
		strQueryBuf.append(" group by expId");
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			
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
				comtradeDataDTO.setDataType(rsInit.getString("type"));
				mapComtradeDataDtos.put(rsInit.getInt("expId"), new WeakReference<ComtradeDataDTO>(comtradeDataDTO));
			}
			rsInit.close();
			psInit.close();

			
//			ps = connection.prepareStatement("select expId, name, convert_tz(from_unixtime(MIN(tsLast)/1000000 - ((sampleCnt-1)/sampleRate)),@@global.time_zone,'+00:00') as StartTime, convert_tz(from_unixtime(MAX(tsLast) div 1000000),@@global.time_zone,'+00:00') as EndTime from cont_view where stationId = "+stationId+" group by expId");
			ps = connection.prepareStatement(strQueryBuf.toString());
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				ps.setInt(1, stationId);
			}
			rs = ps.executeQuery();
			while (rs.next())
			{
//				comtradeDataDTO = new ComtradeDataDTO();
//				comtradeDataDTO.setStationId(stationId);
//				comtradeDataDTO.setExportId(rs.getInt("expId")); 
//				comtradeDataDTO.setExportName(rs.getString("name"));
//				comtradeDataDTO.setExportStartTime(rs.getString("StartTime"));
//				comtradeDataDTO.setExportEndTime(rs.getString("EndTime"));
//				lstComtradeDataDtos.add(new WeakReference<ComtradeDataDTO>(comtradeDataDTO));
				comtradeDataDTO = mapComtradeDataDtos.get(rs.getInt("expId")).get();
				startDateVal = rs.getLong("StartTime") - (((comtradeDataDTO.getSampleCnt()-1) / (int)comtradeDataDTO.getSampleRate())*1000000);
				comtradeDataDTO.setExportStartTime(sdf.format(startDateVal/1000));
				comtradeDataDTO.setExportEndTime(sdf.format(rs.getLong("EndTime")/1000));
				lstComtradeDataDtos.add(new WeakReference<ComtradeDataDTO>(comtradeDataDTO));
			}
		} catch (SQLException e) {
			logger.error("Failed to get quick summary of cont data ",e);
			throw new M9000Exception(e);
		}catch (Exception e) {
			logger.error("Failed to get quick summary of cont data ",e);
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
				if (ps != null)
				{
					ps.close();
				}
				if (psInit != null)
				{
					psInit.close();
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

		return lstComtradeDataDtos;

	}
	
	
	@Override
	public List<WeakReference<ComtradeDataDTO>> getLstOfContinuousData(int stationId, int expId, String startDate,
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
			
//			rs = stmt.executeQuery("SELECT sampleCnt, sampleRate, phase, name, units, type, tsLast, (sampleCnt-((cast((tsLast/1000000) as unsigned) - unix_timestamp('"+startDate+"'))  * sampleRate)) as startSampleNo, " +
//					"(sampleCnt-((cast((tsLast/1000000) as unsigned) - unix_timestamp('"+endDate+"'))  * sampleRate)) as endSampleNo,data FROM `m9000`.`cont_view` " +
//					"where stationId="+stationId+" and expId="+ expId +" and ((('"+startDate+"' >= firstSampleTime and '"+startDate+"' <= lastSampleTime)  or ('"+endDate+"' >= firstSampleTime and '"+endDate+"' <= lastSampleTime ) ) " +
//					"or ((lastSampleTime >= '"+startDate+"' and '"+endDate+"' >= firstSampleTime and '"+endDate+"' >= lastSampleTime )))");
			strQueryBuf = new StringBuffer("SELECT sampleCnt, sampleRate, phase, name, units, type, tsLast, (sampleCnt-((cast((tsLast/1000000) as unsigned) - unix_timestamp(?))  * sampleRate)) as startSampleNo, ");
			strQueryBuf.append(" (sampleCnt-((cast((tsLast/1000000) as unsigned) - unix_timestamp(?))  * sampleRate)) as endSampleNo,data FROM `m9000`.`cont_view` where ");
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				strQueryBuf.append(" stationId= ? and ");
			}
			strQueryBuf.append(" expId=? and (((? >= firstSampleTime and ? <= lastSampleTime)  or (? >= firstSampleTime and ? <= lastSampleTime ) ) ");
			strQueryBuf.append(" or ((lastSampleTime >= ? and ? >= firstSampleTime and ? >= lastSampleTime )))");

			logger.debug("Query to be executed "+strQueryBuf.toString());
			ps = connection.prepareStatement(strQueryBuf.toString());
			ps.setString(++i, startDate);
			logger.debug("param " +i+" - "+startDate);
			ps.setString(++i, endDate);
			logger.debug("param " +i+" - "+endDate);
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				ps.setInt(++i, stationId);
			}
			ps.setInt(++i, expId);
			logger.debug("param " +i+" - "+expId);
			ps.setString(++i, startDate);
			logger.debug("param " +i+" - "+startDate);
			ps.setString(++i, startDate);
			logger.debug("param " +i+" - "+startDate);
			ps.setString(++i, endDate);
			logger.debug("param " +i+" - "+endDate);
			ps.setString(++i, endDate);
			logger.debug("param " +i+" - "+endDate);
			ps.setString(++i, startDate);
			logger.debug("param " +i+" - "+startDate);
			ps.setString(++i, endDate);
			logger.debug("param " +i+" - "+endDate);
			ps.setString(++i, endDate);
			logger.debug("param " +i+" - "+endDate);
//			rs = ps.executeQuery();
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
			long timestamp;

//			System.out.println("Received startdate..."+startDate);
			// Start: 31-Jan-2013 All date conversion are in M9kUtils class
//			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
//			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			java.text.SimpleDateFormat sdf  = M9kUtils.getDateFormat("yyyy-MM-dd HH:mm:ss");
			// End: 31-Jan-2013
			
			startSampleTimeEpoch = sdf.parse(startDate).getTime()*1000;
//			System.out.println("Start date in epoch format..."+startSampleTimeEpoch);
			rs = ps.executeQuery();
			while (rs.next())
			{
//				System.out.println("row cnt..."+rs.getRow());
				timestamp = rs.getLong("tsLast");
				sampleCnt = rs.getInt("sampleCnt");
				
				sampleRate = rs.getDouble("sampleRate");
				is = rs.getBinaryStream("data");
				if ((comtradeDataDTO = mapComtradeContDat.get(expId).get()) == null)
				{
					startSampleNo = rs.getInt("startSampleNo");
					if (startSampleNo < 0)
					{
						startSampleNo = 0;
					}
//					System.out.println("Start of the sample...."+startSampleNo);
					if (rs.isLast())
					{
						endSampleNo = rs.getInt("endSampleNo");
						sampleCnt=endSampleNo+1;
//						System.out.println("Last row to be processed. End Sample.."+endSampleNo+" Actual sampleCnt.."+sampleCnt);
					}
					iCnt = 1;
					data = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo, timestamp);
					comtradeDataDTO = new ComtradeDataDTO();
					comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
					comtradeDataDTO.setRecordingDevId(M9kUtils.getStationDetails().getSystemRecordingDeviceId());
					comtradeDataDTO.setStationId(stationId);
					channelInfoBuffer = new StringBuffer(iRowCnt +M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(expId+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("phase")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("name")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("units")+M9kConstants.COMMA_SEPERATOR);
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
//					System.out.println("1) Sample cnt..."+(sampleCnt-startSampleNo));
					comtradeDataDTO.setSampleCnt(sampleCnt-startSampleNo);
					comtradeDataDTO.setDataType(rs.getString("type"));
					comtradeDataDTO.setType(M9kConstants.ASCII);
					comtradeDataDTO.setData(data);
					comtradeDataDTO.setTsPrefault(startSampleTimeEpoch);
					comtradeDataDTO.setTsTrigger(startSampleTimeEpoch);
					mapComtradeContDat.put(expId, new WeakReference<ComtradeDataDTO>(comtradeDataDTO));
				}
				else
				{
//					startSampleNo = -1;
					if (rs.isLast())
					{
						endSampleNo = (int)rs.getDouble("endSampleNo");
						if (endSampleNo < 0)
						{
							endSampleNo = rs.getInt("sampleCnt");
						}
//						System.out.println("Last row to be processed..."+endSampleNo);
						data = constructContinuousData(is, sampleRate,-1, endSampleNo, timestamp);
//						 System.out.println("Ssample cnt from db..."+sampleCnt);
						sampleCnt=endSampleNo+comtradeDataDTO.getSampleCnt()+1;
					}
					else
					{
						data = constructContinuousData(is, sampleRate, -1,-1,timestamp);
						sampleCnt+=comtradeDataDTO.getSampleCnt();
					}
//					System.out.println("Sample cnt..."+(sampleCnt));
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.getData().append(data);
					comtradeDataDTO.setTsPrefault(startSampleTimeEpoch);
					comtradeDataDTO.setTsTrigger(startSampleTimeEpoch);
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to get list of cont data ",e);
			throw new M9000Exception(e);
		} catch (ParseException e) {
			logger.error("Failed to get list of cont data ",e);
			throw new M9000Exception(e);
		}catch (Exception e) {
			logger.error("Failed to get list of cont data ",e);
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
	public ComtradeDataDTO getLstOfContinuousData(int stationId, String expIds, String startDate,
			String endDate) throws M9000Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		boolean isDataAvailable = false;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
//		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
//		Map<Integer,ComtradeDataDTO> mapComtradeContDat = new HashMap<Integer, ComtradeDataDTO>();
		List<Integer> lstExpIds = new ArrayList<Integer>();
		mapData = new HashMap<Integer, Map<Long,Float>>();
		StringBuffer data;
		StringBuffer strQueryBuf;
		int i = 0;
		try {
//			System.out.println("Export ids list "+expIds);
			connection = M9kMySqlDatabase.getInstance().getConnection();

//			stmt = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
//			ps = connection.prepareStatement("SELECT sampleCnt, sampleRate, phase, name, units, type, tsLast, ((cast((tsLast/1000000) as unsigned) - unix_timestamp('"+startDate+"'))  * sampleRate) as startSampleNo, " +
//					"((cast((tsLast/1000000) as unsigned) - unix_timestamp('"+endDate+"'))  * sampleRate) as endSampleNo,data FROM `m9000`.`cont_view` " +
//					"where expId="+ expId +" and ((('"+startDate+"' >= firstSampleTime and '"+startDate+"' <= lastSampleTime)  or ('"+endDate+"' >= firstSampleTime and '"+endDate+"' <= lastSampleTime ) ) " +
//					"or ((lastSampleTime >= '"+startDate+"' and '"+endDate+"' >= firstSampleTime and '"+endDate+"' >= lastSampleTime )))");
//			String queryToExecute = "SELECT expId, sampleCnt, sampleRate, phase, name, units, type, tsLast, (sampleCnt-((cast((tsLast/1000000) as unsigned) - unix_timestamp('"+startDate+"'))  * sampleRate)) as startSampleNo, " +
//			"(sampleCnt-((tsLast div 1000000) - unix_timestamp('"+endDate+"'))  * sampleRate) as endSampleNo,data FROM `m9000`.`cont_view` " +
//			"where stationId =" + stationId + " and expId in ("+ expIds +") and ((('"+startDate+"' >= firstSampleTime and '"+startDate+"' <= lastSampleTime)  or ('"+endDate+"' >= firstSampleTime and '"+endDate+"' <= lastSampleTime ) ) " +
//			"or ((lastSampleTime >= '"+startDate+"' and '"+endDate+"' >= firstSampleTime and '"+endDate+"' >= lastSampleTime ))) order by expId";
//			System.out.println("Query to be executed "+queryToExecute);
//			strQueryBuf = new StringBuffer("SELECT expId, sampleCnt, sampleRate, phase, name, units, type, tsLast, if ((@start := (sampleCnt-(((tsLast/1000000) - unix_timestamp(?))  * sampleRate))) >= 0, @start, 0) as startSampleNo, ");
//			strQueryBuf.append(" if ((@stop := (sampleCnt-(((tsLast/1000000) - unix_timestamp(?))  * sampleRate))) >= 0, @stop, 0) as endSampleNo,data FROM `m9000`.`cont_view` where " );
			strQueryBuf = new StringBuffer("SELECT expId, sampleCnt, sampleRate, phase, name, units, type, tsLast, data FROM `m9000`.`cont_view` where ");
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				strQueryBuf.append(" stationId=? and ");
			} 
			strQueryBuf.append(" expId in (?) and (((? >= firstSampleTime and ? <= lastSampleTime)  or (? >= firstSampleTime and ? <= lastSampleTime ) ) ");
			strQueryBuf.append(" or ((lastSampleTime >= ? and ? >= firstSampleTime and ? >= lastSampleTime ))) order by expId, tsLast");
			logger.debug("Query to be executed "+strQueryBuf.toString());
			ps = connection.prepareStatement(strQueryBuf.toString());
//			ps.setString(++i, startDate);
//			logger.debug("param " +i+" - "+startDate);
//			ps.setString(++i, endDate);
//			logger.debug("param " +i+" - "+endDate);
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				ps.setInt(++i, stationId);
			}
			ps.setString(++i, expIds);
			logger.debug("param " +i+" - "+expIds);
			ps.setString(++i, startDate);
			logger.debug("param " +i+" - "+startDate);
			ps.setString(++i, startDate);
			logger.debug("param " +i+" - "+startDate);
			ps.setString(++i, endDate);
			logger.debug("param " +i+" - "+endDate);
			ps.setString(++i, endDate);
			logger.debug("param " +i+" - "+endDate);
			ps.setString(++i, startDate);
			logger.debug("param " +i+" - "+startDate);
			ps.setString(++i, endDate);
			logger.debug("param " +i+" - "+endDate);
			ps.setString(++i, endDate);
			logger.debug("param " +i+" - "+endDate);
//			rs = stmt.executeQuery(queryToExecute);
//			rs = ps.executeQuery();
			InputStream is;
			StringBuffer channelInfoBuffer;
			double sampleRate;
			double multFactor = 0.0001;
			int sampleCnt = 0;
			int startSampleNo = -1;
			int endSampleNo = -1;
			int iRowCnt = 1;
			long startSampleTimeEpoch;
			long endSampleTimeEpoch;
			long displayStartSampleTimeEpoch;
			int expId;
			long timestamp;
			long startSampleTimestamp;
//			System.out.println("Received startdate..."+startDate+"\n\n\n");
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
//			Date actualDate = sdf.parse(startDate);
//			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//			System.out.println("Start date in epoch format..."+startSampleTimeEpoch);
			comtradeDataDTO = new ComtradeDataDTO();
			comtradeDataDTO.setStationId(stationId);
			comtradeDataDTO.setSampleRate(-1.0);
			comtradeDataDTO.setData(null);
			startSampleTimeEpoch = sdf.parse(startDate).getTime()/1000;
//			logger.debug("startSampleTimeEpoch "+startSampleTimeEpoch);
			endSampleTimeEpoch = sdf.parse(endDate).getTime()/1000;
//			logger.debug("endSampleTimeEpoch "+endSampleTimeEpoch);
			displayStartSampleTimeEpoch = sdf.parse(M9kUtils.convertDateToDisplay(startDate)).getTime()*1000;
			rs = ps.executeQuery();
			
			while (rs.next())
			{
//				System.out.println("row cnt..."+rs.getRow());
				expId = rs.getInt("expId");
//				System.out.println("ExpId "+expId);
				timestamp = rs.getLong("tsLast");
//				System.out.println("Timestamp from DB..."+timestamp);
				sampleCnt = rs.getInt("sampleCnt");
				
				sampleRate = rs.getDouble("sampleRate");
				startSampleTimestamp = (long)(timestamp - (((sampleCnt-1)/sampleRate)*1000000));
//				System.out.println("Calculated timestamp..."+timestamp +" sample Cnt "+sampleCnt+" sample rate "+sampleRate);
				is = rs.getBinaryStream("data");
				if (!lstExpIds.contains(expId)) // Enter for the first time for each expIds
				{
//					System.out.println("New export id..."+expId);
					currentDataMap = new HashMap<Long, Float>();
//					startSampleNo = rs.getInt("startSampleNo");
					startSampleNo = sampleCnt - (int)(((timestamp/1000000) - startSampleTimeEpoch)*sampleRate);
//					System.out.println("IF part Start of the sample...."+startSampleNo);
					if (startSampleNo < 0)
					{
						startSampleNo = 0;
					}
					logger.debug("Start of the sample...."+startSampleNo);
//					if (rs.getDouble("endSampleNo") >= 0)
//					{
//						endSampleNo = rs.getInt("endSampleNo");
						endSampleNo = sampleCnt - (int) (((timestamp/1000000) - endSampleTimeEpoch) * sampleRate);
//						System.out.println("IF part end of the sample...."+endSampleNo);
						if (endSampleNo < 0)
						{
							endSampleNo = -1;
						}
						logger.debug("End sample no "+endSampleNo);
						sampleCnt=endSampleNo+1;
//						System.out.println("Last row to be processed. End Sample.."+endSampleNo+" Actual sampleCnt.."+sampleCnt);
//					}
//					else
//					{
//						endSampleNo = -1;
//					}
					iCnt = 1;
//					comtradeDataDTO.setExportId(expId); // Just a value to use in file name creation
					if (comtradeDataDTO.getAnalogs() == null )
					{
						channelInfoBuffer = new StringBuffer();
					}
					else
					{
						channelInfoBuffer = comtradeDataDTO.getAnalogs();
					}
					channelInfoBuffer.append(iRowCnt++ +M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(expId+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("phase")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("name")+M9kConstants.COMMA_SEPERATOR);
					channelInfoBuffer.append(rs.getString("units")+M9kConstants.COMMA_SEPERATOR);
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
					data = constructContinuousData(is, sampleRate,startSampleNo, endSampleNo, startSampleTimestamp);
//					System.out.println("comtradeDataDTO.getSampleRate()... "+comtradeDataDTO.getSampleRate());
//					System.out.println("Sample Rate..."+sampleRate);
					if (comtradeDataDTO.getSampleRate() != -1.0 && comtradeDataDTO.getSampleRate() != sampleRate)
					{
						comtradeDataDTO.setSampleRate(0.0);
					}
					else
					{
						comtradeDataDTO.setSampleRate(sampleRate);
					}
//					System.out.println("1) Sample cnt..."+(sampleCnt-startSampleNo));
					comtradeDataDTO.setSampleCnt(sampleCnt-startSampleNo);
					comtradeDataDTO.setDataType(rs.getString("type"));
					comtradeDataDTO.setType(M9kConstants.ASCII);
//					comtradeDataDTO.setData(data);
					comtradeDataDTO.setTsPrefault(displayStartSampleTimeEpoch);
					comtradeDataDTO.setTsTrigger(displayStartSampleTimeEpoch);
					lstExpIds.add(expId);
					mapData.put(expId, currentDataMap);
				}
				else
				{
//					startSampleNo = -1;
//					if (rs.getDouble("endSampleNo") >= 0)
//					{
//						endSampleNo = (int)rs.getDouble("endSampleNo");
//						if (endSampleNo < 0)
//						{
//							endSampleNo = rs.getInt("sampleCnt");
//						}
					
//					System.out.println("ELse part timestamp...."+timestamp+" endSampleTimeEpoch "+endSampleTimeEpoch+" sampleRate "+sampleRate);
//					System.out.println("ELSE part sampleCnt "+sampleCnt);
						endSampleNo = sampleCnt - (int) (((timestamp/1000000) - endSampleTimeEpoch) * sampleRate);
//						System.out.println("ELse part end of the sample(sampleCnt - (int) (((timestamp/1000000) - endSampleTimeEpoch) * sampleRate))...."+endSampleNo);
						if (endSampleNo < 0)
						{
							sampleCnt+=comtradeDataDTO.getSampleCnt();
							timestamp = (long)(timestamp - (((sampleCnt-1)/sampleRate)*1000000));
							data = constructContinuousData(is, sampleRate, -1, -1, timestamp);
						}
						else
						{
	//						System.out.println("Last row to be processed..."+endSampleNo);
							sampleCnt=endSampleNo+comtradeDataDTO.getSampleCnt()+1;
							timestamp = (long)(timestamp - (((sampleCnt-1)/sampleRate)*1000000));
							data = constructContinuousData(is, sampleRate,-1, endSampleNo, timestamp);
						}
//						 System.out.println("Ssample cnt from db..."+sampleCnt);
//					}
//					else
//					{
//						sampleCnt+=comtradeDataDTO.getSampleCnt();
//						timestamp = (long)(timestamp - (((sampleCnt-1)/sampleRate)*1000000));
//						data = constructContinuousData(is, sampleRate, -1, -1, timestamp);
//					}
//					System.out.println("Sample cnt..."+(sampleCnt));
					comtradeDataDTO.setSampleCnt(sampleCnt);
//					comtradeDataDTO.getData().append(data);
					comtradeDataDTO.setTsPrefault(displayStartSampleTimeEpoch);
					comtradeDataDTO.setTsTrigger(displayStartSampleTimeEpoch);
				}
				isDataAvailable = true;
			}
		} catch (SQLException e) {
			logger.error("Failed to get list of cont data ",e);
			throw new M9000Exception(e);
		} catch (ParseException e) {
			logger.error("Failed to get list of cont data ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get list of cont data ",e);
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
		if (isDataAvailable)
		{
			data = createDataStringBuffer();
			comtradeDataDTO.setData(data);
			comtradeDataDTO.setSampleCnt(getTotalSampleCnt());
		}
//		System.out.println("list of comtrade Size..."+lstComtradeDataDtos.size());
		return comtradeDataDTO;
	}
	
	
	private StringBuffer createDataStringBuffer()
	{
		StringBuffer dataBuffer = new StringBuffer();
		Iterator<Integer> iterator = mapData.keySet().iterator();
		Iterator<Long> iteratorTs;
		int key;
		int sampleCnt = 0;
		long ts;
		List<Long> lstTimestamps = new ArrayList<Long>();
		while (iterator.hasNext())
		{
			key = iterator.next();
			
//			System.out.println("Export Id..."+key);
			currentDataMap = mapData.get(key);
//			System.out.println("Size of each Map...."+currentDataMap.size());
			iteratorTs = currentDataMap.keySet().iterator();
			while(iteratorTs.hasNext())
			{
				ts = iteratorTs.next();
				if (!lstTimestamps.contains(ts))
				{
					lstTimestamps.add(ts);
				}
			}
		}
		Collections.sort(lstTimestamps);
		setTotalSampleCnt(lstTimestamps.size());
//		System.out.println("Size of the ts list "+lstTimestamps.size());
//		System.out.println("last ts in the list..."+lstTimestamps.get((lstTimestamps.size()-1)));
		long temp = 0;
		for (Iterator<Long> iterator2 = lstTimestamps.iterator(); iterator2
				.hasNext();) {
			Long long1 = iterator2.next();
			if (temp == 0)
			{
				temp = long1;
//				System.out.println("First time stamp..."+temp);
			}
			dataBuffer.append(++sampleCnt+","+((long1/1000) - (temp/1000))+",");
			iterator = mapData.keySet().iterator();
			while (iterator.hasNext())
			{
				key = iterator.next();
				
//				System.out.println("Export Id..."+key);
				currentDataMap = mapData.get(key);
				if (currentDataMap.get(long1) != null)
				{
					dataBuffer.append(""+currentDataMap.get(long1));
				}
				else
				{
					dataBuffer.append("99999");
				}
				if (iterator.hasNext())
				{
					dataBuffer.append(",");
				}
			}
			dataBuffer.append(M9kConstants.NEWLINE);
//			System.out.println((long1/1000) - (temp/1000));
			
			
		}
//		System.out.println(dataBuffer.toString());
		return dataBuffer;
	}
	/**
	 * @return the totalSampleCnt
	 */
	public int getTotalSampleCnt() {
		return totalSampleCnt;
	}
	/**
	 * @param totalSampleCnt the totalSampleCnt to set
	 */
	public void setTotalSampleCnt(int totalSampleCnt) {
		this.totalSampleCnt = totalSampleCnt;
	}
	/* (non-Javadoc)
	 * @see com.usi.m9000.dao.ComtradeContDAO#getAvailableMeasurementTypes()
	 * Returns map of all measurement types along with its available start and stop date
	 */
	@Override
	public Map<String,String> getAvailableMeasurementTypes(int stationId) throws M9000Exception {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs= null;
		StringBuffer strQueryBuf;
		String measurementType;
		String strStartDate;
		String strEndDate;
		Map<String,String> mapMeasurementTypesWithDates = new HashMap<String, String>();
		if (M9kUtils.isRemote())
		{
			strQueryBuf = new StringBuffer("select measurementType,from_unixtime(min(tsLast) div 1000000) as startDate, from_unixtime(max(tsLast) div 1000000) as endDate from cont where stationId = ? group by measurementType ");	
		}
		else
		{
			strQueryBuf = new StringBuffer("select measurementType,from_unixtime(min(tsLast) div 1000000) as startDate, from_unixtime(max(tsLast) div 1000000) as endDate from cont group by measurementType");
		}
		try {
			
			connection = M9kMySqlDatabase.getInstance().getConnection();
			ps = connection.prepareStatement(strQueryBuf.toString());
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				ps.setInt(1, stationId);
			}
			rs = ps.executeQuery();
			while (rs.next())
			{
				measurementType = rs.getString("measurementType");
				strStartDate = rs.getString("startDate");
				strEndDate = rs.getString("endDate");
				if (!measurementType.trim().isEmpty())
				{
					mapMeasurementTypesWithDates.put(measurementType, ("From "+strStartDate+" to "+strEndDate));
					
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to get list of Measurement types ",e);
			throw new M9000Exception(e);
		}catch (Exception e) {
			logger.error("Failed to get list of Measurement types ",e);
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
			 catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		return mapMeasurementTypesWithDates;
	}
	
	@Override
	public long getFirstAvailableTime() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuffer strQueryBuf;
		long firstAvailableTime = 0L;
//		strQueryBuf = new StringBuffer("select min(tsLast) as firstAvailableTime from cont where date(from_unixtime(tsLast div 1000000)) = CURDATE()");
		strQueryBuf = new StringBuffer("select min(tsLast) as firstAvailableTime from cont");
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			ps = connection.prepareStatement(strQueryBuf.toString());
			rs = ps.executeQuery();
			if (rs.next()) {
				firstAvailableTime = rs.getLong("firstAvailableTime");
			}
		} catch (Exception e) {
			logger.error("Failed to get first available time of cont data ", e);
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
				e.printStackTrace();
			}
		}
		if (firstAvailableTime == 0L) {
			try {
				logger.info("cont table has no data and hence no first available time. Setting it to current hour.");
				firstAvailableTime = M9kBackupUtil.getCurrentWholeHourTime();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return firstAvailableTime;
	}
	
}
