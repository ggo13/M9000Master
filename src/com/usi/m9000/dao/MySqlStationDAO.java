package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.usi.SubStationDocument.SubStation;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.StationReportDTO;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.util.M9kUtils;

public class MySqlStationDAO implements StationDAO {

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlStationDAO.class);
	public MySqlStationDAO()
	{

	}
	@Override
	public List<StationDTO> getStationsList() throws M9000Exception {
		List<StationDTO> lstStationDto = new ArrayList<StationDTO>();
		StationDTO stationDTO = null;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		
		try 
		{
//			logger.debug("About to get connection from local database");
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered getStationsList in DAO....");
//			stmt = mysqlConn.createStatement();
			ps = mysqlConn.prepareStatement("SELECT * from station_details order by stationId" );
//			rs = stmt.executeQuery("SELECT * from station_details order by stationId" );
			rs = ps.executeQuery();
			logger.debug("Select query executed..."+"SELECT * from station_details order by stationId" );
//			long startTime;
//			long endTime;
			while(rs.next()){
//				startTime = System.currentTimeMillis();
//				logger.debug("Start time..."+startTime);
				stationDTO = new StationDTO();
				stationDTO.setSystemStationId(rs.getInt("stationId"));
				stationDTO.setId(rs.getInt("stationId"));
				stationDTO.setSystemStationName(rs.getString("name"));
				stationDTO.setSystemRecordingDeviceId(rs.getString("recordingDevId"));
				stationDTO.setSystemAnalogChannelsCount(rs.getInt("analogs_count"));
				stationDTO.setSystemDigitalChannelsCount(rs.getInt("digitals_count"));
//				stationDTO.setSystemRecordingDeviceName(rs.getString("recordingDevName"));
//				stationDTO.setIpAddress(rs.getString("ipAddress"));
				stationDTO.setSystemPrefaultTime(rs.getInt("preFaultTime"));
				stationDTO.setSystemPostfaultTime(rs.getInt("PostFaultTime"));
				stationDTO.setSystemLtrPrefaultTime(rs.getInt("ltrPreFaultTime"));
				stationDTO.setSystemLtrPostfaultTime(rs.getInt("ltrPostFaultTime"));
				stationDTO.setSystemLineFrequency(rs.getInt("lineFreq"));
				stationDTO.setSystemSampleRate(rs.getInt("sampleRate"));
				stationDTO.setSystemLongTermSampleRate(rs.getInt("longTermSampleRate"));
//				stationDTO.setConfigXml(rs.getString("configXml"));
				stationDTO.setConfigStatus(rs.getString("status"));
				// 09-Nov-2021 - Hierarchy implementation
				stationDTO.setParentZoneId(rs.getInt("parentZoneId"));
//				logger.debug("Station details status "+stationDTO.getConfigStatus());
//				logger.debug("Display Name: "+stationDTO.getStationDisplayName());
				lstStationDto.add(stationDTO);
//				M9kStationCreateComtradeFilesFromDB.createComtradeFiles(comtradeDataDTO);
//				System.out.println("Comtrade File name created..."+comtradeDataDTO.getFileName());
//				endTime = System.currentTimeMillis();
//				logger.debug("End time..."+endTime);
//				
//				logger.debug("Total time Elapsed for getStationsList()..."+(endTime-startTime));
			}
		}
		catch (SQLException e) {
//			e.printStackTrace();
			logger.error("Failed to get Station list",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
//			e.printStackTrace();
			logger.error("Failed to get Station list",e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs= null;
				}
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.warn("Exception during clean up", e);
			}
		}
		logger.debug("Total No of stations.."+lstStationDto.size());
		return lstStationDto;
	}

	@Override
	public List<StationDTO> getManagedStations() throws M9000Exception {
		List<StationDTO> lstStationDto = new ArrayList<StationDTO>();
		StationDTO stationDTO = null;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		
		try 
		{
//			logger.debug("About to get connection from local database");
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered getStationsList in DAO");
//			stmt = mysqlConn.createStatement();
			ps = mysqlConn.prepareStatement("SELECT * from station_details order by stationId" );
//			rs = stmt.executeQuery("SELECT * from station_details order by stationId" );
			rs = ps.executeQuery();
//			logger.debug("Select query executed...");
//			long startTime;
//			long endTime;
			while(rs.next()){
//				startTime = System.currentTimeMillis();
//				logger.debug("Start time..."+startTime);
//				System.out.println("Total Sample Cnt..."+rs.getInt("sampleCnt"));
				stationDTO = new StationDTO();
				stationDTO.setSystemStationId(rs.getInt("stationId"));
				stationDTO.setId(rs.getInt("stationId"));
				stationDTO.setSystemStationName(rs.getString("name"));
				stationDTO.setSystemRecordingDeviceId(rs.getString("recordingDevId"));
				stationDTO.setSystemAnalogChannelsCount(rs.getInt("analogs_count"));
				stationDTO.setSystemDigitalChannelsCount(rs.getInt("digitals_count"));
//				stationDTO.setSystemRecordingDeviceName(rs.getString("recordingDevName"));
//				stationDTO.setIpAddress(rs.getString("ipAddress"));
				stationDTO.setSystemPrefaultTime(rs.getInt("preFaultTime"));
				stationDTO.setSystemPostfaultTime(rs.getInt("PostFaultTime"));
				stationDTO.setSystemLtrPrefaultTime(rs.getInt("ltrPreFaultTime"));
				stationDTO.setSystemLtrPostfaultTime(rs.getInt("ltrPostFaultTime"));
				stationDTO.setSystemLineFrequency(rs.getInt("lineFreq"));
				stationDTO.setSystemSampleRate(rs.getInt("sampleRate"));
				stationDTO.setSystemLongTermSampleRate(rs.getInt("longTermSampleRate"));
				stationDTO.setConfigXml(rs.getString("configXml"));
				stationDTO.setConfigStatus(rs.getString("status"));
//				logger.debug("Station details status "+stationDTO.getConfigStatus());
//				logger.debug("Display Name: "+stationDTO.getStationDisplayName());
				lstStationDto.add(stationDTO);
//				M9kStationCreateComtradeFilesFromDB.createComtradeFiles(comtradeDataDTO);
//				System.out.println("Comtrade File name created..."+comtradeDataDTO.getFileName());
//				endTime = System.currentTimeMillis();
//				logger.debug("End time..."+endTime);
				
//				logger.info("Total time Elapsed for getStationsList()..."+(endTime-startTime));
			}
		}
		catch (SQLException e) {
			logger.error("Failed to get Station list",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get Station list",e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs= null;
				}
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.warn("Exception during clean up", e);
			}
		}
		logger.debug("No of staTIONS.."+lstStationDto.size());
		return lstStationDto;
	}

	@Override
	public StationDTO getStationsDetails(int stationId) throws M9000Exception {
		StationDTO stationDTO = null;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		try 
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
//			logger.debug("Entered getStationsDetails in DAO");
			logger.debug("Station ID be look out for "+stationId);
			ps = mysqlConn.prepareStatement("select * from station_details where stationId = ?");
			ps.setInt(1, stationId);
			
			rs = ps.executeQuery();
			
//			logger.debug("Select query executed...");
//			long startTime;
//			long endTime;
			while(rs.next()){
//				startTime = System.currentTimeMillis();
//				logger.debug("Start time..."+startTime);
//				System.out.println("Total Sample Cnt..."+rs.getInt("sampleCnt"));
				stationDTO = new StationDTO();
				stationDTO.setSystemStationId(rs.getInt("stationId"));
				stationDTO.setId(rs.getInt("stationId"));
				stationDTO.setSystemStationName(rs.getString("name"));
				stationDTO.setSystemRecordingDeviceId(rs.getString("recordingDevId"));
//				stationDTO.setSystemRecordingDeviceName(rs.getString("recordingDevName"));
//				stationDTO.setIpAddress(rs.getString("ipAddress"));
				stationDTO.setSystemAnalogChannelsCount(rs.getInt("analogs_count"));
				stationDTO.setSystemDigitalChannelsCount(rs.getInt("digitals_count"));
				stationDTO.setSystemPrefaultTime(rs.getInt("preFaultTime"));
				stationDTO.setSystemPostfaultTime(rs.getInt("PostFaultTime"));
				stationDTO.setSystemLtrPrefaultTime(rs.getInt("ltrPreFaultTime"));
				stationDTO.setSystemLtrPostfaultTime(rs.getInt("ltrPostFaultTime"));
				stationDTO.setSystemLineFrequency(rs.getInt("lineFreq"));
				stationDTO.setSystemSampleRate(rs.getInt("sampleRate"));
				stationDTO.setSystemLongTermSampleRate(rs.getInt("longTermSampleRate"));
				stationDTO.setConfigXml(rs.getString("configXml"));
				stationDTO.setConfigStatus(rs.getString("status"));
				stationDTO.setCreated(rs.getString("created"));
				// 09-Nov-2021 - Hierarchy implementation
				stationDTO.setParentZoneId(rs.getInt("parentZoneId"));
				logger.debug("Station details status "+stationDTO.getConfigStatus());
				logger.debug("Display Name: "+stationDTO.getStationDisplayName());
//				M9kStationCreateComtradeFilesFromDB.createComtradeFiles(comtradeDataDTO);
//				System.out.println("Comtrade File name created..."+comtradeDataDTO.getFileName());
//				endTime = System.currentTimeMillis();
//				logger.debug("End time..."+endTime);
				
//				logger.info("Total time Elapsed for getStationsList()..."+(endTime-startTime));
			}
		}
		catch (SQLException e) {
			logger.error("Failed to get Station details. ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get Station details. ",e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs= null;
				}
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.warn("Exception during clean up", e);
			}
		}
		if (stationDTO != null)
		{
			logger.debug("Station details from db... "+stationDTO.getStationDisplayName());
		}
		return stationDTO;
	}
	
	public List<Integer> getListOfStationIds() throws M9000Exception
	{
		List<Integer> lstStationIds = new ArrayList<Integer>();
		Connection localConnection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			localConnection = M9kMySqlDatabase.getInstance().getConnection();
//			stmt = localConnection.createStatement();
			ps = localConnection.prepareStatement("SELECT stationId FROM station_details");
//			rs = stmt.executeQuery("SELECT stationId FROM station_details" );
			rs = ps.executeQuery();
			while (rs.next())
			{
				lstStationIds.add(rs.getInt("stationId"));
			}
		}
		catch (SQLException e) {
			logger.error("Failed to get list of Station Ids. ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get list of Station Ids. ",e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs= null;
				}
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (localConnection != null)
				{
					localConnection.close();
					localConnection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return lstStationIds;
	}
//	/**
//	 * @return the mysqlConn
//	 */
//	public Connection getMysqlConn() {
//		return mysqlConn;
//	}
//	/**
//	 * @param mysqlConn the mysqlConn to set
//	 */
//	public void setMysqlConn(Connection mysqlConn) {
//		this.mysqlConn = mysqlConn;
//	}
	
	public static void main(String[] args)
	{
//		MySqlStationDAO test = new MySqlStationDAO();
//		test.getStationsList();
	}
	@Override
	public void insertStationDetails(StationDTO stationDto) throws M9000Exception {
		Connection mysqlConn = null;
		PreparedStatement ps = null;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered insertStationDetails in DAO");
			int i = 0;
			ps = mysqlConn.prepareStatement("insert into station_details set recordingDevId=?, stationId= ?, name = ? ,  analogs_count=?, digitals_count=?, preFaultTime=?, postFaultTime=?,ltrPreFaultTime=?, ltrPostFaultTime=?," +
				"lineFreq=?, sampleRate=?, longTermSampleRate=?, configXml=?, status=?,parentZoneId=?");
			ps.setString(++i, stationDto.getSystemRecordingDeviceId());
			ps.setInt(++i, stationDto.getSystemStationId());
			ps.setString(++i, stationDto.getSystemStationName());
//			ps.setString(4, stationDto.getSystemRecordingDeviceName());
//			ps.setString(++i, stationDto.getIpAddress());
			ps.setInt(++i, stationDto.getSystemAnalogChannelsCount());
			ps.setInt(++i, stationDto.getSystemDigitalChannelsCount());
			ps.setInt(++i, stationDto.getSystemPrefaultTime());
			ps.setInt(++i, stationDto.getSystemPostfaultTime());
			ps.setInt(++i, stationDto.getSystemLtrPrefaultTime());
			ps.setInt(++i, stationDto.getSystemLtrPostfaultTime());
			ps.setInt(++i, stationDto.getSystemLineFrequency());
			ps.setInt(++i, stationDto.getSystemSampleRate());
			ps.setInt(++i, stationDto.getSystemLongTermSampleRate());
			ps.setString(++i, stationDto.getConfigXml());
			ps.setString(++i, stationDto.getConfigStatus());
			ps.setInt(++i, stationDto.getParentZoneId());
			int rowsUpdated = ps.executeUpdate();
//			logger.info("Inserted the station details "+stationDto+" with inserted row count "+rowsUpdated);
		}
		catch (SQLException e) {
			logger.error("Failed to insert station details. ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to insert station details. ",e);
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
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}
		
	}
	@Override
	public void updateConfigXml(StationDTO stationDto) throws M9000Exception {
		Connection mysqlConn = null;
		PreparedStatement ps = null;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered updateConfigXml in DAO..Parent Zone Id..."+stationDto.getParentZoneId());
			int i = 0;
//			ps = mysqlConn.prepareStatement("update station_details set configXml = ?, status=? where stationId = ?");
			ps = mysqlConn.prepareStatement("update station_details set recordingDevId=?,  name = ? , analogs_count=?, digitals_count=?, preFaultTime=?, postFaultTime=?,ltrPreFaultTime=?, ltrPostFaultTime=?," +
				"lineFreq=?, sampleRate=?, longTermSampleRate=?, configXml=?, status=?,parentZoneId=? where stationId = ?");
			ps.setString(++i, stationDto.getSystemRecordingDeviceId());
			ps.setString(++i, stationDto.getSystemStationName());
//			ps.setString(++i, stationDto.getIpAddress());
			ps.setInt(++i, stationDto.getSystemAnalogChannelsCount());
			ps.setInt(++i, stationDto.getSystemDigitalChannelsCount());
			ps.setInt(++i, stationDto.getSystemPrefaultTime());
			ps.setInt(++i, stationDto.getSystemPostfaultTime());
			ps.setInt(++i, stationDto.getSystemLtrPrefaultTime());
			ps.setInt(++i, stationDto.getSystemLtrPostfaultTime());
			ps.setInt(++i, stationDto.getSystemLineFrequency());
			ps.setInt(++i, stationDto.getSystemSampleRate());
			ps.setInt(++i, stationDto.getSystemLongTermSampleRate());
			ps.setString(++i, stationDto.getConfigXml());
			ps.setString(++i, stationDto.getConfigStatus());
			ps.setInt(++i, stationDto.getParentZoneId());
			ps.setInt(++i, stationDto.getSystemStationId());
			int rowsUpdated = ps.executeUpdate();
			logger.debug("Update the station details "+stationDto+" with inserted row count "+rowsUpdated);
		}
		catch (SQLException e) {
			logger.error("Failed to update config xml. ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to  update config xml. ",e);
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
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					logger.warn("Exception during cleanup ",e);
				}

	}
	}
	@Override
	public boolean isStationExists(int stationId) throws M9000Exception {
		boolean stationExists = false;
		Connection mysqlConn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered isStationExists in DAO");
			ps = mysqlConn.prepareStatement("select * from station_details where stationId = ?");
			ps.setInt(1, stationId);
			
			rs = ps.executeQuery();
			int cnt = 0;
			while (rs.next())
			{
				cnt++;
			}
			if (cnt > 0)
			{
				stationExists = true;
			}
		}
		catch (SQLException e) {
			logger.error("Failed to get station status. ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get station status. ",e);
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
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					logger.warn("Exception during cleanup",e);
				}

	}
		return stationExists;
	}
	@Override
	public String getConfigXml(int stationId) throws M9000Exception {
		String configXml = "";
		Connection localConnection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			localConnection = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered getConfigXml in DAO");
			ps = localConnection.prepareStatement("select configXml from station_details where stationId = ?");
			ps.setInt(1, stationId);
			
			rs = ps.executeQuery();
			while (rs.next())
			{
				configXml = rs.getString("configXml");
			}
		}
		catch (SQLException e) {
			logger.error("Failed to get station config xml. ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to station config xml ",e);
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
					if (localConnection != null)
					{
						localConnection.close();
						localConnection = null;
					}
				} catch (SQLException e) {
					logger.warn("Exception during cleanup",e);
				}

	}
		return configXml;
	}
	@Override
	public String getLocalConfigXml() throws M9000Exception {
		String configXml = null;
		Connection localConnection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
//			localConnection = M9kMySqlDatabase.getInstance().getLocalDBConnection();
			localConnection = M9kStationDBUtil.getLocalConnection();
//			logger.debug("Entered getLocalConfigXml in DAO");
			ps = localConnection.prepareStatement("select configXml from station_details");
			// Test code
//			ps = localConnection.prepareStatement("select configXml from station_details where stationId = 14"); // Pleasantville
//			ps = localConnection.prepareStatement("select configXml from station_details where stationId = 16"); // Buchanan
//			ps = localConnection.prepareStatement("select configXml from station_details where stationId = 7"); // Millwood
//			ps = localConnection.prepareStatement("select configXml from station_details where stationId = 6"); // Millwood
			rs = ps.executeQuery();
			while (rs.next())
			{
				configXml = rs.getString("configXml");
			}
		}
		catch (SQLException e) {
			logger.error("Failed to get local station config xml. ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to get local station config xml ",e);
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
					if (localConnection != null)
					{
						localConnection.close();
						localConnection = null;
					}
				} catch (SQLException e) {
					logger.warn("Exception during cleanup",e);
				}

	}
		return configXml;
	}
	
	public boolean updateLocalStationDetails(SubStation substation, String configXml) throws M9000Exception
	{
		boolean status = false;
		Connection localConnection = null;
		PreparedStatement ps = null;
//		PreparedStatement psConfig = null;
		com.usi.SystemDocument.System xmlStationInfo = substation.getDFRs().getDFRArray(0).getSystem();
		try
		{
//			localConnection = M9kMySqlDatabase.getInstance().getLocalDBConnection();
			localConnection = M9kStationDBUtil.getLocalConnection();
			logger.debug("Entered getLocalConfigXml in DAO"+configXml);
			logger.debug("substation.getId() "+substation.getId());
//			ps = localConnection.prepareStatement("select configXml from station_details ");
//			rs = ps.executeQuery();
//			if (rs.next())
//			{
//				update = true;
//			}
//			rs.close();
//			ps.close();
			
//			if (update)
//			{
//				psConfig = localConnection.prepareStatement("insert into station_details where configXml = ?");
//				psConfig.setString(1, configXml);
//				psConfig.executeUpdate();
//				status = true;
//			}
//			else
//			{
//				//TODO: code for new station insert goes in here
////				localConnection.prepareStatement("insert into station_details where config");
//			}
			
			int i =0;
			
			ps = localConnection.prepareStatement("insert into station_details set recordingDevId=?, stationId= ?, name = ? ,  analogs_count=?, digitals_count=?,preFaultTime=?, postFaultTime=?,ltrPreFaultTime=?, ltrPostFaultTime=?," +
			"lineFreq=?, sampleRate=?, longTermSampleRate=?, configXml=?, parentZoneId=? on duplicate key update recordingDevId=values(recordingDevId), name = values(name) ,  analogs_count=values(analogs_count), digitals_count=values(digitals_count), preFaultTime=values(preFaultTime), postFaultTime=values(postFaultTime)," +
			"ltrPreFaultTime=values(ltrPreFaultTime), ltrPostFaultTime=values(ltrPostFaultTime),lineFreq=values(lineFreq), sampleRate=values(sampleRate), longTermSampleRate=values(longTermSampleRate), configXml=values(configXml), parentZoneId=values(parentZoneId)");
		ps.setString(++i, xmlStationInfo.getRecordingDeviceId());
		ps.setInt(++i, substation.getId());
		ps.setString(++i, substation.getName());
//		ps.setString(4, stationDto.getSystemRecordingDeviceName());
//		ps.setString(++i, substation.getHost());
		ps.setInt(++i, xmlStationInfo.getAnalogsCount());
		ps.setInt(++i, xmlStationInfo.getDigitalsCount());
		ps.setInt(++i, xmlStationInfo.getPrefaultTime());
		ps.setInt(++i, xmlStationInfo.getPostfaultTime());
		ps.setInt(++i, xmlStationInfo.getLtrPrefaultTime()); //TODO: should update the XML structure to include long term prefault
		ps.setInt(++i, xmlStationInfo.getLtrPostfaultTime()); //TODO: should update the XML structure to include long term postfault
		ps.setInt(++i, xmlStationInfo.getLineFrequency());
		ps.setInt(++i, xmlStationInfo.getSampleRate());
		ps.setInt(++i, xmlStationInfo.getExportAnalogSampleRate()); 
		ps.setString(++i, configXml);
		ps.setInt(++i, M9kUtils.getStationDetails().getParentZoneId());
		int insertOrUpdatestatus = ps.executeUpdate();
		status = true;
		logger.debug("insertOrUpdatestatus after query execution "+insertOrUpdatestatus);
		}
		catch (SQLException e) {
				logger.error("Exception during insert or update station details",e);
				throw new M9000Exception(e);
			} catch (Exception e) {
				logger.error("Exception during insert or update station details",e);
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
					if (localConnection != null)
					{
						localConnection.close();
						localConnection = null;
					}
				} catch (SQLException e) {
					logger.warn("Exception during cleanup",e);
				}

	}
		return status;
	}
	
	@Override
	public void updateFaultLocationDetails(int stationId, int faultId, String faultLocationDetails) throws M9000Exception {
		Connection mysqlConn = null;
		PreparedStatement ps = null;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
//			logger.debug("Entered updateConfigXml in DAO"+stationDto);
			int i = 0;
//			ps = mysqlConn.prepareStatement("update station_details set configXml = ?, status=? where stationId = ?");
			ps = mysqlConn.prepareStatement("update comtrade_details set fault_location=? where station_id=? and fault_id=?" +
				"");
			ps.setString(++i, faultLocationDetails);
			ps.setInt(++i, stationId);
			ps.setInt(++i, faultId);
			int rowsUpdated = ps.executeUpdate();

			logger.debug("Updatec the station details "+stationId+" with inserted row count "+rowsUpdated);
		}
		catch (SQLException e) {
			logger.error("Failed to update fault Location. ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to  update faultLocation. ",e);
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
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					logger.warn("Exception during cleanup ",e);
				}

	}
	}
	/* (non-Javadoc)
	 * @see com.usi.m9000.dao.StationDAO#getLstStationForReports()
	 */
	@Override
	public List<StationReportDTO> getLstStationForReports()
			throws M9000Exception {

		List<StationReportDTO> lstStationReportDto = new ArrayList<StationReportDTO>();
		StationReportDTO stationReportDTO = null;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		
		try 
		{
//			logger.debug("About to get connection from local database");
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered getLstStationForReports in DAO....");
//			stmt = mysqlConn.createStatement();
			ps = mysqlConn.prepareStatement("SELECT * from station_details order by stationId" );
//			rs = stmt.executeQuery("SELECT * from station_details order by stationId" );
			rs = ps.executeQuery();
			logger.debug("Select query executed..."+"SELECT * from station_details order by stationId" );
//			long startTime;
//			long endTime;
			while(rs.next()){
//				startTime = System.currentTimeMillis();
//				logger.debug("Start time..."+startTime);
				stationReportDTO = new StationReportDTO();
				stationReportDTO.setStationId(rs.getInt("stationId"));
				stationReportDTO.setStationName(rs.getString("name"));
				stationReportDTO.setAnalogCount(rs.getInt("analogs_count"));
				stationReportDTO.setDigitalCount(rs.getInt("digitals_count"));
				lstStationReportDto.add(stationReportDTO);
			}
		}
		catch (SQLException e) {
//			e.printStackTrace();
			logger.error("Failed to get Station list for reports",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
//			e.printStackTrace();
			logger.error("Failed to get Station list for reports",e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs= null;
				}
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.warn("Exception during clean up", e);
			}
		}
		logger.debug("Total No of stations for reports.."+lstStationReportDto.size());
		return lstStationReportDto;
	
	}
	@Override
	public void removeStation(int stationId) throws M9000Exception {
		Connection mysqlConn = null;
		PreparedStatement ps = null;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			int i = 0;
			ps = mysqlConn.prepareStatement("delete from station_details where stationId = ?");
			ps.setInt(++i, stationId);
			int rowsUpdated = ps.executeUpdate();
			logger.debug("Removed the station "+stationId);
			
		}
		catch (SQLException e) {
			logger.error("Failed to remove station with station Id "+stationId,e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to remove station with station Id "+stationId,e);
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
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					logger.warn("Exception during cleanup ",e);
				}

	}

		
	}
	@Override
	public void importStationConfig(int stationId, String sqlContent) throws M9000Exception {
		Connection mysqlConn = null;
		PreparedStatement ps = null;
		try
		{
			removeStation(stationId);
		}
		catch (Exception e) {
			logger.error("Error in removing station config before importing. Continuing to import.",e);
		}
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
//			logger.debug("Entered updateConfigXml in DAO"+stationDto);
//			ps = mysqlConn.prepareStatement("update station_details set configXml = ?, status=? where stationId = ?");
			ps = mysqlConn.prepareStatement(sqlContent);
			int rowsUpdated = ps.executeUpdate();
			logger.debug("Imported the station "+stationId);
		}
		catch (SQLException e) {
			logger.error("Failed to import config",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to  import config",e);
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
					if (mysqlConn != null)
					{
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					logger.warn("Exception during cleanup ",e);
				}

	}
	}
	@Override
	public void removeAllStationsForLocal() throws M9000Exception {
		Connection localConnection = null;
		PreparedStatement ps = null;
		try
		{
			localConnection = M9kStationDBUtil.getLocalConnection();
			logger.debug("Entered removeAllStations");
			
			ps = localConnection.prepareStatement("truncate station_details");
			ps.executeUpdate();
		}
		catch (SQLException e) {
				logger.error("Exception during removing all stations",e);
				throw new M9000Exception(e);
			} catch (Exception e) {
				logger.error("Exception during removing all stations",e);
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
					if (localConnection != null)
					{
						localConnection.close();
						localConnection = null;
					}
				} catch (SQLException e) {
					logger.warn("Exception during cleanup",e);
				}

	}
	}
	@Override
	public List<StationDTO> getZonesStationsList(int zoneId) throws M9000Exception {
		List<StationDTO> lstStationDto = new ArrayList<StationDTO>();
		StationDTO stationDTO = null;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		
		try 
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered getStationsList in DAO....");
			ps = mysqlConn.prepareStatement("SELECT * from station_details where parentZoneId = ? order by stationId" );
			ps.setInt(1, zoneId);
			rs = ps.executeQuery();
			while(rs.next()){
				stationDTO = new StationDTO();
				stationDTO.setSystemStationId(rs.getInt("stationId"));
				stationDTO.setId(rs.getInt("stationId"));
				stationDTO.setSystemStationName(rs.getString("name"));
				stationDTO.setSystemRecordingDeviceId(rs.getString("recordingDevId"));
				stationDTO.setSystemAnalogChannelsCount(rs.getInt("analogs_count"));
				stationDTO.setSystemDigitalChannelsCount(rs.getInt("digitals_count"));
				stationDTO.setSystemPrefaultTime(rs.getInt("preFaultTime"));
				stationDTO.setSystemPostfaultTime(rs.getInt("PostFaultTime"));
				stationDTO.setSystemLtrPrefaultTime(rs.getInt("ltrPreFaultTime"));
				stationDTO.setSystemLtrPostfaultTime(rs.getInt("ltrPostFaultTime"));
				stationDTO.setSystemLineFrequency(rs.getInt("lineFreq"));
				stationDTO.setSystemSampleRate(rs.getInt("sampleRate"));
				stationDTO.setSystemLongTermSampleRate(rs.getInt("longTermSampleRate"));
				stationDTO.setConfigStatus(rs.getString("status"));
				stationDTO.setParentZoneId(rs.getInt("parentZoneId"));
				lstStationDto.add(stationDTO);
			}
		}
		catch (SQLException e) {
//			e.printStackTrace();
			logger.error("Failed to get Station list",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
//			e.printStackTrace();
			logger.error("Failed to get Station list",e);
			throw new M9000Exception(e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs= null;
				}
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.warn("Exception during clean up", e);
			}
		}
		logger.debug("Total No of stations.."+lstStationDto.size());
		return lstStationDto;
	}

}