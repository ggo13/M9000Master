package com.usi.m9000.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
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
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class MySqlLongTermDataDAO implements LongTermDataDAO {

//	Connection mysqlConn;
	Connection connection = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlLongTermDataDAO.class);
	static int expTestCnt;
	public MySqlLongTermDataDAO()
	{
//		mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
	}
	@Override
	public List<ComtradeDataDTO> getLstOfLongTermAnalogDataStaged(int stationId, int faultId) throws M9000Exception {
		logger.debug("Query LTR for station "+stationId+" for fault "+faultId);
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		Map<Integer,ComtradeDataDTO> mapComtradeContDat = new HashMap<Integer, ComtradeDataDTO>();
		
		InputStream is;
		InputStream isData;
		double sampleRate;
		int sampleCnt = 0;
		int startSampleNo = -1;
		int endSampleNo = -1;
		int expId;
		int i = 0;
		try {
			
//			connection = M9kMySqlDatabase.getInstance().getAppletDBConnection();
			connection = M9kMySqlDatabase.getInstance().getConnection();
//			stmt.setFetchSize(Integer.MIN_VALUE);
			String sqlQuery = "SELECT a.*, b.time_stamp - (a.ltrPreFaultTime*1000000) as ltrPrefault,b.time_stamp as ltrTrigger,c.name , c.recordingDevId,if ((@tStart := round((ltrSampleCnt -  ((tsLast - (b.time_stamp - (a.ltrPreFaultTime*1000000))) /1000000) * ltrSampleRate))) < 0, 1, @tStart) as startSampleNo, " +
					"if(((b.time_stamp +(b.length*1000)+ (a.ltrPostFaultTime*1000000)) > tsLast or (@tstop := round((ltrSampleCnt - ((tsLast - (b.time_stamp +(b.length*1000)+ (a.ltrPostFaultTime*1000000))) / 1000000) * ltrSampleRate )) ) < 0),ltrSampleCnt, @tstop) as endSampleNo " +
					"FROM `m9000`.`long_term_dat_staging` a  , comtrade_details b, station_details c where a.stationId = c.stationId and a.stationId= b.station_id and  a.stationId = ? and b.fault_id = ? and a.type = ? and (((b.time_stamp - (a.ltrPreFaultTime*1000000)) >= a.tsPrefault " +
					"and (b.time_stamp - (a.ltrPreFaultTime*1000000)) <= tsLast) or ((b.time_stamp - (a.ltrPreFaultTime*1000000)) <= a.tsPrefault and (b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) >= tsLast)" +
					"or ((b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) >= a.tsPrefault and (b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) <= tsLast)) order by expId";
//			String sqlQuery = "SELECT a.*, b.time_stamp - (a.ltrPreFaultTime*1000000) as ltrPrefault,b.time_stamp as ltrTrigger,c.name , c.recordingDevId,if ((@tStart := round((ltrSampleCnt -  ((tsLast - (b.time_stamp - (a.ltrPreFaultTime*1000000))) /1000000) * ltrSampleRate))) < 0, 1, @tStart) as startSampleNo, " +
//			"if(((b.time_stamp +(b.length*1000)+ (a.ltrPostFaultTime*1000000)) > tsLast or (@tstop := round((ltrSampleCnt - ((tsLast - (b.time_stamp +(b.length*1000)+ (a.ltrPostFaultTime*1000000))) / 1000000) * ltrSampleRate )) ) < 0),ltrSampleCnt, @tstop) as endSampleNo " +
//			"FROM `m9000`.`long_term_dat_staging` a  , comtrade_details b, station_details c where b.fault_id = ? and a.type = ? and (((b.time_stamp - (a.ltrPreFaultTime*1000000)) >= a.tsPrefault " +
//			"and (b.time_stamp - (a.ltrPreFaultTime*1000000)) <= tsLast) or ((b.time_stamp - (a.ltrPreFaultTime*1000000)) <= a.tsPrefault and (b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) >= tsLast)" +
//			"or ((b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) >= a.tsPrefault and (b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) <= tsLast)) order by expId";
			ps = connection.prepareStatement(sqlQuery);
			i = 0;
			ps.setInt(++i, stationId);
			ps.setLong(++i, faultId);
			ps.setString(++i, M9kConstants.ANALOG);
			
//			rs = stmt.executeQuery("Select a.*, b.name, b.recordingDevId from long_term_dat_staging a, station_details b where a.stationId ="+ stationId +" and a.stationId = b.stationId and a.tsPrefault <= "+tsTrigger+" and (a.tsPrefault+(a.ltrSampleCnt/a.ltrSampleRate)*1000000) >= "+tsTrigger+" order by a.expId");
			rs = ps.executeQuery();
			while (rs.next())
			{
				sampleCnt = rs.getInt("ltrSampleCnt");
				sampleRate = rs.getDouble("ltrSampleRate");
				expId = rs.getInt("expId");
				is = rs.getBinaryStream("dataBlob");
				startSampleNo = rs.getInt("startSampleNo");
				endSampleNo = rs.getInt("endSampleNo");
				if (mapComtradeContDat.get(expId) == null)
				{
					sampleCnt=endSampleNo-startSampleNo+1;
					isData = constructContinuousAnalogData(is, sampleRate,startSampleNo, endSampleNo);
					comtradeDataDTO = new ComtradeDataDTO();
					comtradeDataDTO.setStationId(stationId);
					comtradeDataDTO.setFaultId(faultId);
					comtradeDataDTO.setStationName(rs.getString("name"));
					comtradeDataDTO.setRecordingDevId(rs.getString("recordingDevId"));
					comtradeDataDTO.setExportId(rs.getInt("expId")); // Just a value to use in file name creation
					comtradeDataDTO.setAnalogs(new StringBuffer(rs.getString("Analogs")));
					comtradeDataDTO.setSampleRate(rs.getDouble("ltrSampleRate"));
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.setType(rs.getString("type"));
					comtradeDataDTO.setTsPrefault(rs.getLong("ltrPrefault"));
					comtradeDataDTO.setTsTrigger(rs.getLong("ltrTrigger"));
					comtradeDataDTO.setTsLast(rs.getLong("tsLast"));
					comtradeDataDTO.setStartSampleNo(rs.getInt("startSampleNo"));
					comtradeDataDTO.setEndSampleNo(rs.getInt("endSampleNo"));
					comtradeDataDTO.setBinaryDataSteam(isData);
					mapComtradeContDat.put(expId, comtradeDataDTO);
				}
				else
				{
					comtradeDataDTO = mapComtradeContDat.get(expId);
					logger.debug("Comtrade Data DTO "+comtradeDataDTO);

					logger.debug("LTR-DEBUG:  StartSampleNo..."+startSampleNo+" End Sample No "+endSampleNo);
						isData = constructContinuousAnalogData(is, sampleRate,startSampleNo, endSampleNo);
						 
						sampleCnt=(endSampleNo-startSampleNo+1)+comtradeDataDTO.getSampleCnt();
					
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.setBinaryDataSteam(isData);
				}
			}
			lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContDat.values());			
			Collections.sort(lstComtradeDataDtos, new Comparator<ComtradeDataDTO>() {

				@Override
				public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
//					return ((o1.getExportId() < o2.getExportId())?0:1);
					return ((o1.getExportId() < o2.getExportId())? -1 : (o1.getExportId() == o2.getExportId())?0:1);
				}
			});
		} catch (SQLException e) {
			logger.error("Failed to get list of Long Term data ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get list of Long Term data ", e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (ps != null)
				{
					ps.close();
				}
				if (rs != null)
				{
					rs.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		logger.debug("Size of analog cont..."+lstComtradeDataDtos.size());
		return lstComtradeDataDtos;
	}

	private InputStream constructContinuousAnalogData(InputStream is, double sampleRate, int fromSampleNo, int endSampleNo)
	{
		DataInputStream dis = new DataInputStream(is);
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		
		int totalSamplesToRead = endSampleNo - fromSampleNo+1;
		short sampleData;
		
		try {
		if (fromSampleNo > 0)
		{
			dis.skipBytes((fromSampleNo - 1)*M9kConstants.SIZE_OF_SHORT); // 2 - Size of shorts
		}
		while (dis.available() > 0)
		{

			sampleData = dis.readShort();

			dos.writeShort(sampleData);
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
		return new ByteArrayInputStream(bais.toByteArray());

	}

	@Override
	public int insertIntoLongTermDat(ComtradeDataDTO comtradeDataDTO) throws M9000Exception {
		PreparedStatement ps = null;
		int iCnt;
		int result = 0;
//		stmt.setFetchSize(Integer.MIN_VALUE);
		String sqlQuery = "insert into long_term_dat (stationId, faultId, ltType, tsTrigger, sampleRate, length, preFault, postFault, fileName, comments) values(?,?,?,?,?,?,?,?,?,?)";
		try
		{
//			connection = M9kMySqlDatabase.getInstance().getAppletDBConnection();
//			connection = M9kMySqlDatabase.getInstance().getConnection();
			connection = M9kStationDBUtil.getLocalConnection();	
			ps = connection.prepareStatement(sqlQuery);
			iCnt = 1;
			ps.setInt(iCnt++, comtradeDataDTO.getStationId());
			ps.setInt(iCnt++, comtradeDataDTO.getFaultId());
			ps.setString(iCnt++, comtradeDataDTO.getType());
			ps.setLong(iCnt++, comtradeDataDTO.getTsTrigger());
			ps.setDouble(iCnt++, comtradeDataDTO.getSampleRate());
			ps.setDouble(iCnt++, comtradeDataDTO.getLength());
			ps.setInt(iCnt++, (int) comtradeDataDTO.getPreFault());
			ps.setInt(iCnt++, (int) comtradeDataDTO.getPostFault());
			ps.setString(iCnt++, comtradeDataDTO.getFileName());
			ps.setString(iCnt++, comtradeDataDTO.getUserComments());
			result = ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to insert Long Term data ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to insert Long Term data ", e);
			throw new M9000Exception(e);
		}
		finally {
			try {
				if (ps != null)
				{
					ps.close();
				}
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		}

		return result;
	}
	@Override
	public String getLongTermDataFileName(int stationId, int faultId, String dataType) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int iCnt;
		String ltrFileName = null;
//		stmt.setFetchSize(Integer.MIN_VALUE);
//		String sqlQuery = "select fileName from long_term_dat where stationId= ? and faultId = ? and ltType = ?";
		StringBuffer sqlQuery = new StringBuffer("select fileName from long_term_dat where stationId= ? and id = ? and ");
		if (dataType.equalsIgnoreCase(M9kConstants.ANALOG))
		{
//		String sqlQuery = "select fileName from long_term_dat where stationId= ? and  (tsTrigger-(prefault*1000)) <= ? and (tsTrigger+(length*1000)) >= ? and ltType = ?";
			sqlQuery.append(" and ltType = ?");
		}
		else
		{
			sqlQuery.append(" and (ltType = ? || ltType != '"+M9kConstants.ANALOG+"')");
		}
		try
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();
			ps = connection.prepareStatement(sqlQuery.toString());
			iCnt = 1;
			ps.setInt(iCnt++, stationId);
			ps.setInt(iCnt++, faultId);
			ps.setString(iCnt++, dataType);
			rs = ps.executeQuery();
			if (rs.next())
			{
				ltrFileName = rs.getString("fileName");
			}
		} catch (SQLException e) {
			logger.error("Failed to get Long Term data ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get Long Term data ", e);
			throw new M9000Exception(e);
		}
		finally {
			try {
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		}

		logger.debug("About to return LTR filename "+ltrFileName);
		return ltrFileName;
	}

	@Override
	public String getLongTermDataFileName(int stationId, long tsTrigger, String dataType) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		int iCnt;
		String ltrFileName = null;
//		stmt.setFetchSize(Integer.MIN_VALUE);
		StringBuffer sqlQuery = new StringBuffer("select fileName from long_term_dat where stationId= ? and  (tsTrigger-(prefault*1000)) <= ? and (tsTrigger+(length*1000)) >= ?");
		if (dataType.equalsIgnoreCase(M9kConstants.ANALOG))
		{
//		String sqlQuery = "select fileName from long_term_dat where stationId= ? and  (tsTrigger-(prefault*1000)) <= ? and (tsTrigger+(length*1000)) >= ? and ltType = ?";
			sqlQuery.append(" and ltType = ?");
		}
		else
		{
			sqlQuery.append(" and (ltType = ? || ltType != '"+M9kConstants.ANALOG+"')");
		}
		logger.debug("Query "+sqlQuery);
		try
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();
			ps = connection.prepareStatement(sqlQuery.toString());
			iCnt = 1;
			ps.setInt(iCnt++, stationId);
			logger.debug("param "+iCnt+" - "+stationId);
			ps.setLong(iCnt++, tsTrigger);
			logger.debug("param "+iCnt+" - "+tsTrigger);
			ps.setLong(iCnt++, tsTrigger);
			logger.debug("param "+iCnt+" - "+tsTrigger);
			ps.setString(iCnt++, dataType);
			logger.debug("param "+iCnt+" - "+dataType);
			rs = ps.executeQuery();
			if (rs.next())
			{
				ltrFileName = rs.getString("fileName");
			}
		} catch (SQLException e) {
			logger.error("Failed to get Long Term data ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get Long Term data ", e);
			throw new M9000Exception(e);
		}
		finally {
			try {
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		}

		logger.debug("About to return LTR filename "+ltrFileName);
		return ltrFileName;
	}

	@Override
	public List<ComtradeDataDTO> getLstOfLongTermDataDetails(int stationId) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		int iCnt = 0;
		String sqlQuery = "select * from long_term_dat where stationId= ?";
		try
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();
			ps = connection.prepareStatement(sqlQuery);
			iCnt = 1;
			ps.setInt(iCnt++, stationId);
			rs = ps.executeQuery();
			if (rs.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setStationId(rs.getInt("stationId"));
				comtradeDataDTO.setFaultId(rs.getInt("faultId"));
				comtradeDataDTO.setType(rs.getString("ltType"));
				comtradeDataDTO.setTsTrigger(rs.getLong("tsTrigger"));
				comtradeDataDTO.setSampleRate(rs.getDouble("sampleRate"));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("preFault"));
				comtradeDataDTO.setPostFault(rs.getLong("postFault"));
				comtradeDataDTO.setFileName(rs.getString("fileName"));
				if (rs.getString("comments") != null)
				{
					comtradeDataDTO.setUserComments(rs.getString("comments"));
				}
				else
				{
					comtradeDataDTO.setUserComments("");
				}
				lstComtradeDataDtos.add(comtradeDataDTO);
			}
		} catch (SQLException e) {
			logger.error("Failed to get Long Term data details ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get Long Term data details ", e);
			throw new M9000Exception(e);
		}
		finally {
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
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		}

		return lstComtradeDataDtos;
	}
	@Override
	public List<ComtradeDataDTO> getLstOfLongTermDataDetails(int stationId,
			String startDateTime, String endDateTime, List<ComtradeDataDTO> lstComtradeData, String ltType) throws M9000Exception {
		logger.debug("INside getLstOfLongTermDataDetails "+lstComtradeData.size());
		System.out.println("INside getLstOfLongTermDataDetails "+lstComtradeData.size()+" start "+startDateTime+ " End time "+endDateTime);
		
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		int faultsResultsLimit = 5000; // Read from m9k-applet.properties file
		faultsResultsLimit = M9kUtils.getFaultsResultsLimit();
		
		String commaSeperatedIds = getCommaSeperatedIds (lstComtradeData);
		if (commaSeperatedIds.isEmpty())
		{
			commaSeperatedIds="''";
		}
		logger.debug("commaSeperatedIds "+commaSeperatedIds);
		StringBuffer strQuery = new StringBuffer("Select * from long_term_dat where stationId = ? and ");
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			int i = 0;
			if (startDateTime != null && endDateTime != null)
			{
				strQuery.append(" (from_unixTime(tsTrigger div 1000000) >= ? and from_unixTime(tsTrigger div 1000000) <= ?) and ");
				if (ltType.equalsIgnoreCase(M9kConstants.ANALOG))
				{
					strQuery.append(" ltType = ? ");
				}
				else
				{
					strQuery.append(" (ltType = ? || ltType != '"+M9kConstants.ANALOG+"') ");
				}
				strQuery.append(" order by id desc limit 0, "+faultsResultsLimit);
				
//				ps = connection.prepareStatement("Select * from long_term_dat where stationId = ? and (from_unixTime(tsTrigger div 1000000) >= ? and from_unixTime(tsTrigger div 1000000) <= ?) and ltType = ? order by id desc limit 0, "+faultsResultsLimit);
				ps = connection.prepareStatement(strQuery.toString());
				ps.setInt(++i,stationId);
				ps.setString(++i, startDateTime);
				ps.setString(++i, endDateTime);
				ps.setString(++i, ltType);
//				System.out.println("Select * from long_term_dat where stationId = "+stationId+" and (from_unixTime(tsTrigger div 1000000) >= "+startDateTime+" and from_unixTime(tsTrigger div 1000000) <= "+endDateTime+") and ltType = "+ltType+" order by id desc limit 0, "+faultsResultsLimit);
			}
			else if (startDateTime != null )
			{
				strQuery.append(" from_unixTime(tsTrigger div 1000000) >= ? and ");
				if (ltType.equalsIgnoreCase(M9kConstants.ANALOG))
				{
					strQuery.append(" ltType = ? ");
				}
				else
				{
					strQuery.append(" (ltType = ? || ltType != '"+M9kConstants.ANALOG+"') ");
				}
				strQuery.append(" order by id desc limit 0, "+faultsResultsLimit);
				System.out.println("Query for just start time "+strQuery);
//				ps = connection.prepareStatement("Select * from long_term_dat where stationId = ? and from_unixTime(tsTrigger div 1000000) >= ? and id not in ("+commaSeperatedIds+") and ltType = ? order by id desc limit 0, "+faultsResultsLimit);
				ps = connection.prepareStatement(strQuery.toString());
				ps.setInt(++i,stationId);
				ps.setString(++i, startDateTime);
				ps.setString(++i, ltType);
			}
			else if (endDateTime != null )
			{
				strQuery.append(" from_unixTime(tsTrigger div 1000000) <= ? and ");
				if (ltType.equalsIgnoreCase(M9kConstants.ANALOG))
				{
					strQuery.append(" ltType = ? ");
				}
				else
				{
					strQuery.append(" (ltType = ? || ltType != '"+M9kConstants.ANALOG+"') ");
				}
				strQuery.append(" order by id desc limit 0, "+faultsResultsLimit);
				System.out.println("Query for just end time "+strQuery);
//				ps = connection.prepareStatement("Select * from long_term_dat where stationId = ? and from_unixTime(tsTrigger div 1000000) <= ? and id not in ("+commaSeperatedIds+") and ltType = ? order by id desc limit 0, "+faultsResultsLimit);
				ps = connection.prepareStatement(strQuery.toString());
				ps.setInt(++i,stationId);
				ps.setString(++i, endDateTime);
				ps.setString(++i, ltType);
			}
			else
			{
				strQuery.append(" id not in ("+commaSeperatedIds+") and ");
				if (ltType.equalsIgnoreCase(M9kConstants.ANALOG))
				{
					strQuery.append(" ltType = ? ");
				}
				else
				{
					strQuery.append(" (ltType = ? || ltType != '"+M9kConstants.ANALOG+"') ");
				}
				strQuery.append(" order by id desc limit 0, "+faultsResultsLimit);
				System.out.println("QUery to  be executed "+strQuery);
//				ps = connection.prepareStatement("Select * from long_term_dat where stationId = ? and id not in ("+commaSeperatedIds+") and ltType = ? order by id desc limit 0, "+faultsResultsLimit);
				ps = connection.prepareStatement(strQuery.toString());
				ps.setInt(++i,stationId);
//				System.out.println("Executed the query:\t\t"+"Select * from long_term_dat where stationId = ? and id not in ("+commaSeperatedIds+") and ltType = "+ltType+" order by id desc limit 0, "+faultsResultsLimit);
				ps.setString(++i, ltType);
			}
			rs = ps.executeQuery();
			while (rs.next())
			{
//				System.out.println("Result set loop "+ ++i);
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("id")); // Store the unique id of long_term_dat instead of fault id as it will be -1 for more than one faults
				comtradeDataDTO.setStationId(rs.getInt("stationId"));
				comtradeDataDTO.setType(rs.getString("ltType"));
				comtradeDataDTO.setTsTrigger(rs.getLong("tsTrigger"));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("preFault"));
				comtradeDataDTO.setPostFault(rs.getLong("postFault"));
				comtradeDataDTO.setFileName(rs.getString("fileName"));
				//TODO: Implement header info for the faults by uncommenting below
//				if (rs.getString("header_info") != null)
//				{
//					comtradeDataDTO.setHeaderInfo(rs.getString("header_info"));
//				}
//				else
//				{
//					comtradeDataDTO.setHeaderInfo("");
//				}
				comtradeDataDTO.setHeaderInfo("");
				if (rs.getString("comments") != null)
				{
					comtradeDataDTO.setUserComments(rs.getString("comments"));
				}
				else
				{
					comtradeDataDTO.setUserComments("");
				}
				lstComtradeDataDtos.add(comtradeDataDTO);
//				System.out.println("Fault id...."+rs.getInt("fault_id"));
			}
		} catch (SQLException e) {
			logger.error("Failed to get list of Long Term data details ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get list of Long Term data details ", e);
			throw new M9000Exception(e);
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
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return lstComtradeDataDtos;
	}
	private String getCommaSeperatedIds (List<ComtradeDataDTO> lstComtradeData)
	{
		StringBuffer strValue = new StringBuffer();
		
		ComtradeDataDTO comtradeDataDTO;
    	for (Iterator<ComtradeDataDTO> iterator = lstComtradeData.iterator(); iterator
				.hasNext();) {
    		comtradeDataDTO = iterator.next();
			if (strValue.length() == 0)
			{
				strValue.append(comtradeDataDTO.getFaultId());
			}
			else
			{
				strValue.append(","+comtradeDataDTO.getFaultId());
			}
			
		}
		logger.debug("The search combination string returned "+strValue.toString());
		return strValue.toString();
	}

	
	// Long term RMS data to be created 
	@Override
	public List<ComtradeDataDTO> getLstOfLongTermMeasurementsDataStaged(int stationId, int faultId) throws M9000Exception {
		logger.debug("Query LTR for station "+stationId+" for fault "+faultId);
		boolean available = false;
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		Map<Integer,ComtradeDataDTO> mapComtradeContDat = new HashMap<Integer, ComtradeDataDTO>();
		
		InputStream is;
		InputStream isData;
		double sampleRate;
		int sampleCnt = 0;
		int startSampleNo = -1;
		int endSampleNo = -1;
		int expId;
		int i = 0;
		try {
			
//			connection = M9kMySqlDatabase.getInstance().getAppletDBConnection();
			connection = M9kMySqlDatabase.getInstance().getConnection();
//			stmt.setFetchSize(Integer.MIN_VALUE);
			String sqlQuery = "SELECT a.*, b.time_stamp - (a.ltrPreFaultTime*1000000) as ltrPrefault,b.time_stamp as ltrTrigger,c.name , c.recordingDevId,if ((@tStart := round((ltrSampleCnt -  ((tsLast - (b.time_stamp - (a.ltrPreFaultTime*1000000))) /1000000) * ltrSampleRate))) < 0, 1, @tStart) as startSampleNo, " +
					"if(((b.time_stamp +(b.length*1000)+ (a.ltrPostFaultTime*1000000)) > tsLast or (@tstop := round((ltrSampleCnt - ((tsLast - (b.time_stamp +(b.length*1000)+ (a.ltrPostFaultTime*1000000))) / 1000000) * ltrSampleRate )) ) < 0),ltrSampleCnt, @tstop) as endSampleNo " +
					"FROM `m9000`.`long_term_dat_staging` a  , comtrade_details b, station_details c where a.stationId = c.stationId and a.stationId= b.station_id and  a.stationId = ? and b.fault_id = ? and a.type != ? and (((b.time_stamp - (a.ltrPreFaultTime*1000000)) >= a.tsPrefault " +
					"and (b.time_stamp - (a.ltrPreFaultTime*1000000)) <= tsLast) or ((b.time_stamp - (a.ltrPreFaultTime*1000000)) <= a.tsPrefault and (b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) >= tsLast)" +
					"or ((b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) >= a.tsPrefault and (b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) <= tsLast)) order by expId";
//			String sqlQuery = "SELECT a.*, b.time_stamp - (a.ltrPreFaultTime*1000000) as ltrPrefault,b.time_stamp as ltrTrigger,c.name , c.recordingDevId,if ((@tStart := round((ltrSampleCnt -  ((tsLast - (b.time_stamp - (a.ltrPreFaultTime*1000000))) /1000000) * ltrSampleRate))) < 0, 1, @tStart) as startSampleNo, " +
//			"if(((b.time_stamp +(b.length*1000)+ (a.ltrPostFaultTime*1000000)) > tsLast or (@tstop := round((ltrSampleCnt - ((tsLast - (b.time_stamp +(b.length*1000)+ (a.ltrPostFaultTime*1000000))) / 1000000) * ltrSampleRate )) ) < 0),ltrSampleCnt, @tstop) as endSampleNo " +
//			"FROM `m9000`.`long_term_dat_staging` a  , comtrade_details b, station_details c where b.fault_id = ? and a.type = ? and (((b.time_stamp - (a.ltrPreFaultTime*1000000)) >= a.tsPrefault " +
//			"and (b.time_stamp - (a.ltrPreFaultTime*1000000)) <= tsLast) or ((b.time_stamp - (a.ltrPreFaultTime*1000000)) <= a.tsPrefault and (b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) >= tsLast)" +
//			"or ((b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) >= a.tsPrefault and (b.time_stamp +(b.length*1000)+  (a.ltrPostFaultTime*1000000)) <= tsLast)) order by expId";
			ps = connection.prepareStatement(sqlQuery);
			i = 0;
			ps.setInt(++i, stationId);
			ps.setLong(++i, faultId);
			ps.setString(++i, M9kConstants.ANALOG);
			
//			rs = stmt.executeQuery("Select a.*, b.name, b.recordingDevId from long_term_dat_staging a, station_details b where a.stationId ="+ stationId +" and a.stationId = b.stationId and a.tsPrefault <= "+tsTrigger+" and (a.tsPrefault+(a.ltrSampleCnt/a.ltrSampleRate)*1000000) >= "+tsTrigger+" order by a.expId");
			rs = ps.executeQuery();
//			System.out.println("Executed the query "+sqlQuery);
			while (rs.next())
			{
				available = true;
				sampleCnt = rs.getInt("ltrSampleCnt");
				sampleRate = rs.getDouble("ltrSampleRate");
				expId = rs.getInt("expId");
				is = rs.getBinaryStream("dataBlob");
				startSampleNo = rs.getInt("startSampleNo");
				endSampleNo = rs.getInt("endSampleNo");
				if (mapComtradeContDat.get(expId) == null)
				{
					sampleCnt=endSampleNo-startSampleNo+1;
					expTestCnt = expId;
					isData = constructContinuousRmsData(is, sampleRate,startSampleNo, endSampleNo);
					comtradeDataDTO = new ComtradeDataDTO();
					comtradeDataDTO.setStationId(stationId);
					comtradeDataDTO.setFaultId(faultId);
					comtradeDataDTO.setStationName(rs.getString("name"));
					comtradeDataDTO.setRecordingDevId(rs.getString("recordingDevId"));
					comtradeDataDTO.setExportId(rs.getInt("expId")); // Just a value to use in file name creation
					comtradeDataDTO.setAnalogs(new StringBuffer(rs.getString("Analogs")));
					comtradeDataDTO.setSampleRate(rs.getDouble("ltrSampleRate"));
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.setType(rs.getString("type"));
					comtradeDataDTO.setTsPrefault(rs.getLong("ltrPrefault"));
					comtradeDataDTO.setTsTrigger(rs.getLong("ltrTrigger"));
					comtradeDataDTO.setTsLast(rs.getLong("tsLast"));
					comtradeDataDTO.setStartSampleNo(rs.getInt("startSampleNo"));
					comtradeDataDTO.setEndSampleNo(rs.getInt("endSampleNo"));
					comtradeDataDTO.setBinaryDataSteam(isData);
					mapComtradeContDat.put(expId, comtradeDataDTO);
				}
				else
				{
					comtradeDataDTO = mapComtradeContDat.get(expId);
					logger.debug("Comtrade Data DTO "+comtradeDataDTO);

					logger.debug("LTR-DEBUG:  StartSampleNo..."+startSampleNo+" End Sample No "+endSampleNo);
						isData = constructContinuousRmsData(is, sampleRate,startSampleNo, endSampleNo);
						 
						sampleCnt=(endSampleNo-startSampleNo+1)+comtradeDataDTO.getSampleCnt();
					
					comtradeDataDTO.setSampleCnt(sampleCnt);
					comtradeDataDTO.setBinaryDataSteam(isData);
				}
			}
			logger.debug("LTR RMS Available? "+available);
			if (available)
			{
				lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>( mapComtradeContDat.values());			
				Collections.sort(lstComtradeDataDtos, new Comparator<ComtradeDataDTO>() {
	
					@Override
					public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
//						return ((o1.getExportId() < o2.getExportId())?0:1);
						return ((o1.getExportId() < o2.getExportId())? -1 : (o1.getExportId() == o2.getExportId())?0:1);
					}
				});
				logger.debug("Size of analog cont..."+lstComtradeDataDtos.size());
			}
		} catch (SQLException e) {
			logger.error("Failed to get list of Long Term data ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get list of Long Term data ", e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (ps != null)
				{
					ps.close();
				}
				if (rs != null)
				{
					rs.close();
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

//		System.out.println("Returning "+lstComtradeDataDtos);
		
		return lstComtradeDataDtos;
	}

	@Override
	public int deleteLTRRecords(int stationId, String commaSeperatedFaultIds) throws M9000Exception {
		PreparedStatement ps = null;
		Connection connection = null;
		int numOfFaultsDeleted = 0;
		StringBuffer strbQuery;
		try 
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();		
			System.out.println("CSV of LTR ids "+commaSeperatedFaultIds);
//			System.out.println("Entered insert details...");
			strbQuery = new StringBuffer("delete from long_term_dat where stationid = ? and id in(");
			String[] arrFaultIds = commaSeperatedFaultIds.split(",");
			System.out.println("total faults to be deleted "+arrFaultIds.length);
			for (int i = 0; i < arrFaultIds.length; i++) {
				if (i == 0)
				{
					strbQuery.append("?");
				}
				else
				{
					strbQuery.append(",?");
				}
			}
			strbQuery.append(")");
			ps = connection.prepareStatement(strbQuery.toString());
			int iCnt = 1;
			ps.setInt(iCnt++, stationId);
			for (int i = 0; i < arrFaultIds.length; i++) {
				ps.setString(iCnt++, arrFaultIds[i]);				
			}
			numOfFaultsDeleted = ps.executeUpdate();
				System.out.println("Rows Deleted in comtrade details..."+numOfFaultsDeleted);
				
		}
		catch (SQLException e) {
			logger.error("Failed to delete faults ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to delete faults ",e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return numOfFaultsDeleted;
	}

	/* (non-Javadoc)
	 * @see com.usi.m9000.dao.ComtradeDataDAO#updateComments(int, com.usi.m9000.dto.ComtradeDataDTO)
	 */
	@Override
	public void updateComments(String stationId, ComtradeDataDTO comtradeDataDTO)
			throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();	

			ps = connection.prepareStatement("update long_term_dat set comments=? where stationId = ? and id = ?");
			ps.setString(1, comtradeDataDTO.getUserComments());
			ps.setInt(2,Integer.parseInt(stationId));
			ps.setInt(3, comtradeDataDTO.getFaultId());
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to update comments in long_term_dat for (stationId,id) ("+stationId+","+comtradeDataDTO.getFaultId()+")",e);
			throw new M9000Exception(e);
		}catch (Exception e) {
			logger.error("Failed to update comments in long_term_dat for (stationId,id) ("+stationId+","+comtradeDataDTO.getFaultId()+")",e);
			throw new M9000Exception(e);
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
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.debug("returning from updating comments ");
		
	}

	private InputStream constructContinuousRmsData(InputStream is, double sampleRate, int fromSampleNo, int endSampleNo) throws FileNotFoundException
	{
		DataInputStream dis = new DataInputStream(is);
		ByteArrayOutputStream bais = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bais);
		
		int totalSamplesToRead = endSampleNo - fromSampleNo+1;
		short shortDataVal;
		
		try {
		if (fromSampleNo > 0)
		{
			dis.skipBytes((fromSampleNo - 1)*M9kConstants.SIZE_OF_SHORT); // 4 - Size of Floats
		}

		while (dis.available() > 0)
		{

			shortDataVal = dis.readShort();
			dos.writeShort(shortDataVal);
//			if (expTestCnt == 8 || expTestCnt == 72)
//			{
//				System.out.println(shortDataVal);
//			}
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
		return new ByteArrayInputStream(bais.toByteArray());

	}

	@Override
	public int getTotalDDRCount(int stationId, String ltType) throws M9000Exception {
		logger.debug("Getting total ddr count of ltType "+ltType);
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		int totalFaults = 0;
		try
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();
			StringBuffer strQuery = new StringBuffer("Select count(*) from long_term_dat where stationId = ? and ");
//		System.out.println("About to get total faults from station  "+stationId );
		if (ltType != null && ltType.equalsIgnoreCase(M9kConstants.ANALOG))
		{
			strQuery.append(" ltType = ? ");
		}
		else
		{
			strQuery.append(" (ltType = ? || ltType != '"+M9kConstants.ANALOG+"') ");
		}
		logger.debug("Total ddr count query to be executed "+strQuery);
		ps = connection.prepareStatement(strQuery.toString());
		ps.setInt(1,stationId);
		ps.setString(2,ltType);
		rs = ps.executeQuery();
		if (rs.next())
		{
			totalFaults = rs.getInt(1);
		}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in fetching total DDR "+ ltType+" count for station "+stationId+" Returning value as zero");
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
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return totalFaults;
	}

	@Override
	public List<ComtradeDataDTO> searchByCriteria(int stationId, String ltType, String searchCriteria, String orderCriteria, int from, int size)
			throws M9000Exception {

		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		ComtradeDataDTO comtradeDataDTO;
		logger.debug("Search Criteria of ltType... "+ltType);
		StringBuffer strbufQuery = new StringBuffer("Select * from long_term_dat where stationId = ? ");
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			if (ltType != null && ltType.equalsIgnoreCase(M9kConstants.ANALOG))
			{
				strbufQuery.append(" and ltType = ? ");
			}
			else
			{
				strbufQuery.append(" and (ltType = ? || ltType != '"+M9kConstants.ANALOG+"') ");
			}
			if (searchCriteria != null && !searchCriteria.isEmpty())
			{
				strbufQuery.append(" and "+searchCriteria);
			}
			if (orderCriteria != null && !orderCriteria.isEmpty())
			{
				strbufQuery.append(" "+orderCriteria);
			}
			strbufQuery.append(" limit ? , ? ");
//			ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and fault_id < ? limit "+from+","+size);
			logger.debug("Search criteria query to be executed "+strbufQuery);
			ps = connection.prepareStatement(strbufQuery.toString());
			int iCnt = 1;
			ps.setInt(iCnt++,stationId);
			ps.setString(iCnt++,ltType);
			ps.setInt(iCnt++,from);
			ps.setInt(iCnt++,size);
//			System.out.println("About to process from faultId " +" limited to "+(size)+" query to be executed "+strbufQuery);
			rs = ps.executeQuery();
			while (rs.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("id")); // Store the unique id of long_term_dat instead of fault id as it will be -1 for more than one faults
				comtradeDataDTO.setStationId(rs.getInt("stationId"));
				comtradeDataDTO.setType(rs.getString("ltType"));
				comtradeDataDTO.setTsTrigger(rs.getLong("tsTrigger"));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("preFault"));
				comtradeDataDTO.setPostFault(rs.getLong("postFault"));
				comtradeDataDTO.setFileName(rs.getString("fileName"));
				comtradeDataDTO.setHeaderInfo("");
				if (rs.getString("comments") != null)
				{
					comtradeDataDTO.setUserComments(rs.getString("comments"));
				}
				else
				{
					comtradeDataDTO.setUserComments("");
				}
//				logger.debug("Comtrade rom db "+comtradeDataDTO+" comtrade type "+comtradeDataDTO.getType());
				lstComtradeDataDtos.add(comtradeDataDTO);
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			logger.error("Failed to get list of comtrade details",e);
			throw new M9000Exception(e);
		}catch (Exception e) {
			logger.error("Failed to get list of comtrade details",e);
			throw new M9000Exception(e);
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
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.debug("returning from searchCriteria.. "+lstComtradeDataDtos.size());
		return lstComtradeDataDtos;
	
	}
	@Override
	public int countByCriteria(int stationId, String ltType, String searchCriteria) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		int totalFaults = 0;
		try
		{
			StringBuffer strbufQuery = new StringBuffer("Select count(*) from long_term_dat where stationId = ? ");
			if (ltType != null && ltType.equalsIgnoreCase(M9kConstants.ANALOG))
			{
				strbufQuery.append(" and ltType = ? ");
			}
			else
			{
				strbufQuery.append(" and (ltType = ? || ltType != '"+M9kConstants.ANALOG+"') ");
			}
			if (searchCriteria != null && !searchCriteria.isEmpty())
			{
				strbufQuery.append(" and "+searchCriteria);
			}
			connection = M9kMySqlDatabase.getInstance().getConnection();
		ps = connection.prepareStatement(strbufQuery.toString());
		ps.setInt(1,stationId);
		ps.setString(2,ltType);
//		System.out.println("About to get total faults from station  "+stationId );
		rs = ps.executeQuery();
		if (rs.next())
		{
			totalFaults = rs.getInt(1);
		}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in fetching total faults for station "+stationId+" Returning value as zero");
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
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return totalFaults;

	}
	@Override
	public String getLongTermDataFileNames(int stationId, String csvIds, String ltrType) throws M9000Exception {

		PreparedStatement ps = null;
		ResultSet rs = null;
		int iCnt;
		StringBuffer ltrFileNames = null;
//		stmt.setFetchSize(Integer.MIN_VALUE);
//		String sqlQuery = "select fileName from long_term_dat where stationId= ? and faultId = ? and ltType = ?";
		logger.debug("MySqlLongTermDataDAO: Received ids "+csvIds);
		
		StringBuffer sqlQuery = new StringBuffer("select fileName from long_term_dat where stationId= ? ");
		if (ltrType.equalsIgnoreCase(M9kConstants.ANALOG))
		{
//		String sqlQuery = "select fileName from long_term_dat where stationId= ? and  (tsTrigger-(prefault*1000)) <= ? and (tsTrigger+(length*1000)) >= ? and ltType = ?";
			sqlQuery.append(" and ltType = ?");
		}
		else
		{
			sqlQuery.append(" and (ltType = ? || ltType != '"+M9kConstants.ANALOG+"')");
		}
		sqlQuery.append("and id in (");
		String[] arrIds = csvIds.split(",");
		System.out.println("total contrinuous data to be deleted "+arrIds.length);
		for (int i = 0; i < arrIds.length; i++) {
			if (i == 0)
			{
				sqlQuery.append("?");
			}
			else
			{
				sqlQuery.append(",?");
			}
		}
		sqlQuery.append(")");
		logger.debug("MySqlLongTermDataDAO: SQL to be exexcuted "+sqlQuery);
		try
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();
			ps = connection.prepareStatement(sqlQuery.toString());
			iCnt = 1;
			ps.setInt(iCnt++, stationId);
			ps.setString(iCnt++, ltrType);
			for (int i = 0; i < arrIds.length; i++) {
				ps.setString(iCnt++, arrIds[i]);				
			}
			rs = ps.executeQuery();
			while (rs.next())
			{
				if (ltrFileNames == null)
				{
					ltrFileNames = new StringBuffer(rs.getString("fileName"));
				}
				else
				{
					ltrFileNames.append(";;");
					ltrFileNames.append(rs.getString("fileName"));
				}
				logger.debug("MySqlLongTermDataDAO: ltrFileNames in Loop "+ltrFileNames);
			}
		} catch (SQLException e) {
			logger.error("Failed to get Long Term data ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get Long Term data ", e);
			throw new M9000Exception(e);
		}
		finally {
			try {
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		}

		logger.debug("About to return LTR filename(s) "+ltrFileNames);
		return ltrFileNames.toString();
	
	}
	@Override
	public int deleteLTRRecordsWithFileNames(String commaSeperatedFileNames) throws M9000Exception {
		PreparedStatement ps = null;
		Connection connection = null;
		int numOfLTRDeleted = 0;
		StringBuffer strbQuery;
		try 
		{
			connection = M9kStationDBUtil.getLocalConnection();		
			System.out.println("CSV of LTR fileNames "+commaSeperatedFileNames);
//			System.out.println("Entered insert details...");
			strbQuery = new StringBuffer("delete from long_term_dat where fileName in(");
			String[] arrFileNames = commaSeperatedFileNames.split(";;");
			System.out.println("total faults to be deleted "+arrFileNames.length);
			for (int i = 0; i < arrFileNames.length; i++) {
				if (i == 0)
				{
					strbQuery.append("?");
				}
				else
				{
					strbQuery.append(",?");
				}
			}
			strbQuery.append(")");
			ps = connection.prepareStatement(strbQuery.toString());
			int iCnt = 1;
			for (int i = 0; i < arrFileNames.length; i++) {
				ps.setString(iCnt++, arrFileNames[i]);				
			}
			numOfLTRDeleted = ps.executeUpdate();
				System.out.println("Rows Deleted in Long term dat..."+numOfLTRDeleted);
				
		}
		catch (SQLException e) {
			logger.error("Failed to delete LTR in station master ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to delete LTR in station master ",e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return numOfLTRDeleted;
	}
	@Override
	public List<ComtradeDataDTO> getLstOfOldestLongTermDataDetails(int stationId, int count, int retainMinDays) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		int iCnt = 0;
		String sqlQuery = "select * from long_term_dat where stationId= ? and date(from_unixtime(tsTrigger div 1000000)) < date(DATE_SUB(now(), interval "+retainMinDays+" day)) order by tsTrigger limit "+count;
		try
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();
			ps = connection.prepareStatement(sqlQuery);
			iCnt = 1;
			ps.setInt(iCnt++, stationId);
			rs = ps.executeQuery();
			while (rs.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setStationId(rs.getInt("stationId"));
				comtradeDataDTO.setFaultId(rs.getInt("id")); // Replacing faultId with id as faultId is always -1
				comtradeDataDTO.setType(rs.getString("ltType"));
				comtradeDataDTO.setTsTrigger(rs.getLong("tsTrigger"));
				comtradeDataDTO.setSampleRate(rs.getDouble("sampleRate"));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("preFault"));
				comtradeDataDTO.setPostFault(rs.getLong("postFault"));
				comtradeDataDTO.setFileName(rs.getString("fileName"));
				if (rs.getString("comments") != null)
				{
					comtradeDataDTO.setUserComments(rs.getString("comments"));
				}
				else
				{
					comtradeDataDTO.setUserComments("");
				}
				lstComtradeDataDtos.add(comtradeDataDTO);
			}
		} catch (SQLException e) {
			logger.error("Failed to get Long Term data details ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get Long Term data details ", e);
			throw new M9000Exception(e);
		}
		finally {
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
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		}

		return lstComtradeDataDtos;
	}
}
