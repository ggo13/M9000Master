package com.usi.m9000.dao;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class MySqlDatDAO {
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlDatDAO.class);
	static int dbConnCount = 0;
	public MySqlDatDAO()
	{
		logger.debug("Entered MySqlDatDAO constructor");
		
	}
	public ComtradeDataDTO getFaultRecordToProcess() {
		ComtradeDataDTO comtradeDataDTO = null;
		Connection localConnection = null;
		int stationId = 0;
		int faultId = 0;
		ResultSet rs = null;		
		ResultSet rsLocal = null;
		PreparedStatement psLocal = null;
		PreparedStatement ps = null;
		try 
		{
//			logger.debug("COMTRADE-DB: New connection from getFaultRecordToProcess "+ ++dbConnCount);
//			localConnection = M9kStationDBUtil.getLocalConnection();
			localConnection = M9kMySqlDatabase.getInstance().getConnection();;
//			System.out.println("Entered getFaultRecordToProcess ");
//			logger.debug("Entered getList in DAO");
			// START : 31-Jan-2023 - To avoid dummy station to stop creating any new faults 
//			psLocal = localConnection.prepareStatement("select stationId, faultId from dat_staging left join comtrade_details on faultId = fault_id and stationId = station_id where fault_id is null and station_id is null LIMIT 1");
			psLocal = localConnection.prepareStatement("select stationId, faultId from dat_staging left join comtrade_details on faultId = fault_id and stationId = station_id where fault_id is null LIMIT 1");
			// END: 31-Jan-2023
//			stmt.setFetchSize(Integer.MIN_VALUE);
//			rs = stmt.executeQuery("SELECT * from dat_staging where stationId = '"+dfr+"' and faultId not in (select fault_id from comtrade_details) LIMIT 1" );
//			rs = stmt.executeQuery("select a.*, c.name, c.recordingDevId from station_details c, federated_dat_staging_test a left join comtrade_details b on a.faultid = b.fault_id and a.stationId = b.station_id where b.fault_id is null and b.station_id is null and a.stationId = c.stationId LIMIT 1");

//			rsLocal = stmt.executeQuery("select stationId, faultId from dat_staging left join comtrade_details on faultId = fault_id and stationId = station_id where fault_id is null and station_id is null LIMIT 1");
			rsLocal = psLocal.executeQuery();
			if (rsLocal.next())
			{
				stationId = rsLocal.getInt("stationId");
				faultId = rsLocal.getInt("faultId");
				
				logger.debug("About to process for station id "+stationId +" and faultId "+faultId);
			}
			
			//Testing purpose need to comment
//			stationId = 13;
//			faultId = 44;
			// Testing 
//			logger.debug("About to close rsLocal");
			rsLocal.close();
			rsLocal= null;
//			logger.debug("About to close psLocal");
			psLocal.close();
			psLocal = null;
			// Working query takes on avg 30 sec
//			rs = stmt.executeQuery("select a.*, c.name, c.recordingDevId from station_details c, dat_staging a left join comtrade_details b on a.faultid = b.fault_id and a.stationId = b.station_id where b.fault_id is null and b.station_id is null and a.stationId = c.stationId LIMIT 1");
			
			// New query
//			rs = stmt.executeQuery("select a.*, c.name, c.recordingDevId from station_details c, dat_staging a  where faultId = (select faultId from dat_staging left join comtrade_details on faultId = fault_id and stationId = station_id where fault_id is null and station_id is null limit 1) and a.stationId = c.stationId");
			// new debug
//			rs = stmt.executeQuery("select a.*, c.name, c.recordingDevId from station_details c, dat_staging a  where (a.stationId, a.faultId) = (select stationId, faultId from dat_staging left join comtrade_details on faultId = fault_id and stationId = station_id where fault_id is null and station_id is null LIMIT 1) and a.stationId = c.stationId");
			ps = localConnection.prepareStatement("select a.*, c.name, c.recordingDevId from station_details c, dat_staging a  where a.stationId = ? and a.faultId=? and a.stationId = c.stationId");
			ps.setInt(1, stationId);
			ps.setInt(2, faultId);
//			logger.debug("Before Query execution...");
			rs = ps.executeQuery();
			
//			logger.debug("After Query execution...");
			long startTime;
			long endTime;
			String faultLocation;
			String userComments; 
			String lineGroups; // Line groups list that passed the logic for this fault
			Reader asciiDataReader = null;
			InputStream binaryDataStream = null;
//			logger.debug("About to start while loop with ts");
			while(rs.next()){
				startTime = System.currentTimeMillis();
				logger.debug("Processing result set");
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("faultId"));
				comtradeDataDTO.setStationId(rs.getInt("stationId"));
				comtradeDataDTO.setStationName(rs.getString("name"));
				comtradeDataDTO.setType(rs.getString("type"));
				comtradeDataDTO.setTsPrefault(rs.getLong("tsPrefault"));
				comtradeDataDTO.setTsTrigger(rs.getLong("tsTrigger"));
				comtradeDataDTO.setLineFreq(rs.getDouble("lineFreq"));
				comtradeDataDTO.setSampleRate(rs.getDouble("sampleRate"));
				comtradeDataDTO.setSampleCnt(rs.getInt("sampleCnt"));
				comtradeDataDTO.setAnalogs(new StringBuffer(rs.getString("analogs")));
				comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				logger.debug("Before data set ComtradeDataDTO "+comtradeDataDTO);
				if (comtradeDataDTO.getType().equalsIgnoreCase(M9kConstants.ASCII) )
				{
					asciiDataReader = rs.getCharacterStream("data");
					comtradeDataDTO.setAsciiDataReader(asciiDataReader);
				}
				else if (comtradeDataDTO.getType().equalsIgnoreCase(M9kConstants.Binary) )
				{
					binaryDataStream = rs.getBinaryStream("dataBlob");
					comtradeDataDTO.setBinaryDataSteam(binaryDataStream);
				}
				logger.debug("Ater Data set ComtradeDataDTO "+comtradeDataDTO);	
//				System.out.println("About to read blob");
//				System.out.println("Reading blob complete...");
				comtradeDataDTO.setRecordingDevId(rs.getString("recordingDevId"));
				
				faultLocation = rs.getString("faultLocation");
//				logger.debug("Before fault location  "+faultLocation);
				if (faultLocation != null)
				{
					comtradeDataDTO.setFaultLocationDetails(faultLocation);
				}
				userComments = rs.getString("comments");
				if (userComments != null)
				{
					comtradeDataDTO.setUserComments(userComments);
				}
				comtradeDataDTO.setFaultLogic(rs.getBoolean("faultLogic"));
				lineGroups = rs.getString("lineGroups");
				logger.debug("Line groups list from dat_staging "+lineGroups);
				if (lineGroups != null)
				{
					comtradeDataDTO.setLineGroups(lineGroups);
				}
//				logger.debug("After fault location  "+comtradeDataDTO);
//				M9kStationCreateComtradeFilesFromDB.createComtradeFiles(comtradeDataDTO);
//				System.out.println("Comtrade File name created..."+comtradeDataDTO.getFileName());
				endTime = System.currentTimeMillis();
				logger.debug("End time..."+endTime);
				
				
				logger.debug("Total time Elapsed..."+(endTime-startTime));
			}
//			logger.debug("After while loop end");
			rs.close();
			rs = null;
			ps.close();
			ps = null;
			localConnection.close();
			localConnection = null;
//			System.out.println("Result set empty..");
//			logger.debug("End of the method. Next to FINALLY...");

		}
		catch (SQLException e) {
			logger.error("Error in fetching records from dat table",e);
		}
		catch (Exception e) {
			logger.error("Error in fetching records from dat table",e);
		}
		finally
		{
//			logger.debug("In the finally block, about to enter try....");
			try {
//				logger.debug("In the finally block1 try");
				if (rsLocal != null && !rsLocal.isClosed())
				{
//					logger.debug("About to close rslocal");
					rsLocal.close();
					rsLocal= null;
//					logger.debug("closed rslocal");
				}
//				logger.debug("In the finally block2");
				if (psLocal != null && !psLocal.isClosed())
				{
//					logger.debug("About to close pslocal");
					psLocal.close();
					psLocal = null;
//					logger.debug("Close pslocal");
				}
//				logger.debug("In the finally block3");
				if (rs != null && !rs.isClosed())
				{
//					logger.debug("About to close rs");
					rs.close();
					rs= null;
//					logger.debug("Closed rs");
				}
//				logger.debug("In the finally block4"+ps);
				if (ps != null && !ps.isClosed())
				{
//					logger.debug("About to close ps");
					ps.close();
					ps= null;
//					logger.debug("Closed ps");
				}
//				logger.debug("In the finally block5");
				if (localConnection != null  && !localConnection.isClosed())
				{
//					logger.debug("COMTRADE-DB: closing connection from getFaultRecordToProcess "+ dbConnCount);
//					logger.debug("About to close localConnection");
					localConnection.close();
					localConnection = null;
//					logger.debug("Closed locaConnection");
				}
//				logger.debug("about to exit finally block");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("Exception in Finally block ",e);
			}
		}
//		logger.debug("Returning from getFaultRecordToProcess "+comtradeDataDTO);
		return comtradeDataDTO;
	}

	public void moveToErrorDat(ComtradeDataDTO comtradeDataDTO) {
		PreparedStatement ps = null;
		Connection localConnection = null;
		try 
		{
//			logger.debug("COMTRADE-DB: New connection from moveToErrorDat "+ ++dbConnCount);
//			localConnection = M9kStationDBUtil.getLocalConnection();	
			if (!M9kUtils.isRemote())
			{
				localConnection =  M9kStationDBUtil.getLocalConnection();
			}
			else
			{
				localConnection = M9kMySqlDatabase.getInstance().getConnection();
			}

//			System.out.println("Entered insert details...");
			ps = localConnection.prepareStatement("insert into error_comtrade select * from dat_staging where faultId = ?");
			long startTime;
			long endTime;
				startTime = System.currentTimeMillis();
				logger.debug("Start time..."+startTime);
				ps.setInt(1, comtradeDataDTO.getFaultId());
				int rowsInserted;
				rowsInserted = ps.executeUpdate();
				if (rowsInserted > 0)
				{
					removeFaultFromDatStaging(comtradeDataDTO.getFaultId());
				}
				logger.debug("Rows inserted in error dat..."+rowsInserted);
				endTime = System.currentTimeMillis();
				logger.debug("End time..."+endTime);
				
				logger.debug("Total time Elapsed..."+(endTime-startTime));
		}
		catch (SQLException e) {
			logger.error("Error in moving unprocessed dat records from dat_staging table to error_comtrade table",e);
		}
		catch (Exception e) {
			logger.error("Error in moving unprocessed dat records from dat_staging table to error_comtrade table",e);
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
					logger.debug("COMTRADE-DB: closing connection from moveToErrorDat  "+ dbConnCount);
					localConnection.close();
					localConnection = null;
				}
			} catch (SQLException e) {
				logger.error("Error in inserting into error table ",e);
				e.printStackTrace();
			}
		}

	}

	public void removeFaultFromDatStaging(int faultId) {
		PreparedStatement ps = null;
		Connection localConnection = null;
		try 
		{
			logger.debug("COMTRADE-DB: New connection from removeErrorFaultId "+ ++dbConnCount);
			localConnection  = M9kStationDBUtil.getLocalConnection();		

//			System.out.println("Entered insert details...");
			ps = localConnection.prepareStatement("delete from dat_staging where faultId = ?");
			long startTime;
			long endTime;
				startTime = System.currentTimeMillis();
				logger.debug("Start time..."+startTime);
				ps.setInt(1, faultId);
				int rowsInserted;
				rowsInserted = ps.executeUpdate();
				logger.debug("Rows inserted in error dat..."+rowsInserted);
				endTime = System.currentTimeMillis();
				logger.debug("End time..."+endTime);
				
				logger.debug("Total time Elapsed..."+(endTime-startTime));
		}
		catch (SQLException e) {
			logger.error("Error in removing fault record "+faultId+" from dat_staging table",e);
		}
		catch (Exception e) {
			logger.error("Error in removing fault record "+faultId+" from dat_staging table",e);
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
					logger.debug("COMTRADE-DB: closing connection removeErrorFaultId "+ dbConnCount);
					localConnection.close();
					localConnection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

//	public int getCurrentSignature() {
//		int maxSignature = 0;
//		ResultSet rs = null;
//		PreparedStatement ps = null;
//		try 
//		{
//			getLocalConnection();
//			logger.debug("Entered getCurrentSignature in DAO");
//			ps = localConnection.prepareStatement("select max(signature) as maxSignature from dat");
//
//			rs = ps.executeQuery();
//			if (rs.next())
//			{
//				maxSignature = rs.getInt("maxSignature");
//				
//			}
//			rs.close();
//		} catch (SQLException e) {
//			logger.error("Error in fetching records from dat table",e);
//		}
//		catch (Exception e) {
//			logger.error("Error in fetching records from dat table",e);
//		}
//		finally
//		{
//			try {
//				if (rs != null)
//				{
//					rs.close();
//					rs = null;
//				}
//				if (ps != null)
//				{
//					ps.close();
//					ps = null;
//				}
//				if (localConnection != null)
//				{
//					logger.debug("COMTRADE-DB: closing connection "+ dbConnCount);
//					localConnection.close();
//					localConnection = null;
//				}
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return maxSignature; 
//	}
	
	// Station Web Master requires to local faults
	public ComtradeDataDTO getLocalFaultRecordToProcess() {
		ComtradeDataDTO comtradeDataDTO = null;
		int faultId = 0;
		ResultSet rsLocal = null;
		PreparedStatement psLocal = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection localConnection = null;
		try 
		{
//			logger.debug("COMTRADE-DB: New connection from getLocalFaultRecordToProcess "+ ++dbConnCount);
			localConnection  = M9kStationDBUtil.getLocalConnection();
//			localConnection = M9kMySqlDatabase.getInstance().getLocalDBConnection();
//			System.out.println("Entered getFaultRecordToProcess ");
//			logger.debug("Entered getList in DAO");
//			stmt = localConnection.createStatement();
			psLocal = localConnection.prepareStatement("select faultId from dat_staging left join comtrade_details on faultId = fault_id where fault_id is null LIMIT 1");
//			stmt.setFetchSize(Integer.MIN_VALUE);
//			rs = stmt.executeQuery("SELECT * from dat_staging where stationId = '"+dfr+"' and faultId not in (select fault_id from comtrade_details) LIMIT 1" );
//			rs = stmt.executeQuery("select a.*, c.name, c.recordingDevId from station_details c, federated_dat_staging_test a left join comtrade_details b on a.faultid = b.fault_id and a.stationId = b.station_id where b.fault_id is null and b.station_id is null and a.stationId = c.stationId LIMIT 1");

//			rsLocal = stmt.executeQuery("select faultId from dat_staging left join comtrade_details on faultId = fault_id where fault_id is null LIMIT 1");
			rsLocal = psLocal.executeQuery();
			if (rsLocal.next())
			{
				faultId = rsLocal.getInt("faultId");
				
				logger.debug("About to process for faultId "+faultId);
			}
			
			//Testing purpose need to comment
//			stationId = 3;
//			faultId = 3485;
			// Testing 
			
			rsLocal.close();
			// Working query takes on avg 30 sec
//			rs = stmt.executeQuery("select a.*, c.name, c.recordingDevId from station_details c, dat_staging a left join comtrade_details b on a.faultid = b.fault_id and a.stationId = b.station_id where b.fault_id is null and b.station_id is null and a.stationId = c.stationId LIMIT 1");
			
			// New query
//			rs = stmt.executeQuery("select a.*, c.name, c.recordingDevId from station_details c, dat_staging a  where faultId = (select faultId from dat_staging left join comtrade_details on faultId = fault_id and stationId = station_id where fault_id is null and station_id is null limit 1) and a.stationId = c.stationId");
			// new debug
//			rs = stmt.executeQuery("select a.*, c.name, c.recordingDevId from station_details c, dat_staging a  where (a.stationId, a.faultId) = (select stationId, faultId from dat_staging left join comtrade_details on faultId = fault_id and stationId = station_id where fault_id is null and station_id is null LIMIT 1) and a.stationId = c.stationId");
			ps = localConnection.prepareStatement("select a.*, b.stationId, b.name, b.recordingDevId from  dat_staging a, station_details b  where a.faultId=? ");
			ps.setInt(1, faultId);
			
			rs = ps.executeQuery();
			
//			logger.debug("Select query executed...");
//			System.out.println("Select query executed...");
			long startTime;
			long endTime;
			String faultLocation;
			String userComments;
			String lineGroups; // Line groups list that passed the logic for this fault
			Reader asciiDataReader = null;
			InputStream binaryDataStream = null;
			while(rs.next()){
				startTime = System.currentTimeMillis();
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("faultId"));
				comtradeDataDTO.setStationId(rs.getInt("stationId"));
				comtradeDataDTO.setStationName(rs.getString("name"));
				comtradeDataDTO.setType(rs.getString("type"));
				comtradeDataDTO.setTsPrefault(rs.getLong("tsPrefault"));
				comtradeDataDTO.setTsTrigger(rs.getLong("tsTrigger"));
				comtradeDataDTO.setLineFreq(rs.getDouble("lineFreq"));
				comtradeDataDTO.setSampleRate(rs.getDouble("sampleRate"));
				comtradeDataDTO.setSampleCnt(rs.getInt("sampleCnt"));
				comtradeDataDTO.setAnalogs(new StringBuffer(rs.getString("analogs")));
				comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				
				if (comtradeDataDTO.getType().equalsIgnoreCase(M9kConstants.ASCII) )
				{
					asciiDataReader = rs.getCharacterStream("data");
					comtradeDataDTO.setAsciiDataReader(asciiDataReader);
				}
				else if (comtradeDataDTO.getType().equalsIgnoreCase(M9kConstants.Binary) )
				{
					binaryDataStream = rs.getBinaryStream("dataBlob");
					comtradeDataDTO.setBinaryDataSteam(binaryDataStream);
				}
					
//				System.out.println("About to read blob");
//				System.out.println("Reading blob complete...");
				comtradeDataDTO.setRecordingDevId(rs.getString("recordingDevId"));
				faultLocation = rs.getString("faultLocation");
				if (faultLocation != null)
				{
					comtradeDataDTO.setFaultLocationDetails(faultLocation);
				}
				userComments = rs.getString("comments");
				if (userComments != null)
				{
					comtradeDataDTO.setUserComments(userComments);
				}
				comtradeDataDTO.setFaultLogic(rs.getBoolean("faultLogic"));
				lineGroups = rs.getString("lineGroups");
				logger.debug("Line groups list from dat_staging "+lineGroups);
				if (lineGroups != null)
				{
					comtradeDataDTO.setLineGroups(lineGroups);
				}

//				M9kStationCreateComtradeFilesFromDB.createComtradeFiles(comtradeDataDTO);
//				System.out.println("Comtrade File name created..."+comtradeDataDTO.getFileName());
				endTime = System.currentTimeMillis();
				logger.debug("End time..."+endTime);
				
				
				logger.debug("Total time Elapsed..."+(endTime-startTime));
			}
//			System.out.println("Result set empty..");
		}
		catch (SQLException e) {
			logger.error("Error in fetching records from dat table",e);
		}
		catch (Exception e) {
			logger.error("Error in fetching records from dat table",e);
		}
		finally
		{
			try {
				if (rsLocal != null)
				{
					rsLocal.close();
					rsLocal= null;
				}
				if (rs != null)
				{
					rs.close();
					rs= null;
				}
				if (ps != null)
				{
					ps.close();
					ps= null;
				}
				if (psLocal != null)
				{
					psLocal.close();
					psLocal = null;
				}
				if (localConnection != null)
				{
					logger.debug("COMTRADE-DB: closing connection from getLocalFaultRecordToProcess "+ dbConnCount);
					localConnection.close();
					localConnection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return comtradeDataDTO;
	}


	// Station Web Master requires to local faults
	public ComtradeDataDTO getLocalFaultRecordForFaultId(int faultId) {
		ComtradeDataDTO comtradeDataDTO = null;
		ResultSet rsLocal = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection localConnection = null;
		try 
		{
			localConnection  = M9kStationDBUtil.getLocalConnection();

			ps = localConnection.prepareStatement("select a.*, b.stationId, b.name, b.recordingDevId from  dat_staging a, station_details b  where a.faultId=? ");
			ps.setInt(1, faultId);
			
			rs = ps.executeQuery();
			
//			logger.debug("Select query executed...");
//			System.out.println("Select query executed...");
			long startTime;
			long endTime;
			String faultLocation;
			String userComments;
			String lineGroups; // Line groups list that passed the logic for this fault
			Reader asciiDataReader = null;
			InputStream binaryDataStream = null;
			while(rs.next()){
				startTime = System.currentTimeMillis();
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("faultId"));
				comtradeDataDTO.setStationId(rs.getInt("stationId"));
				comtradeDataDTO.setStationName(rs.getString("name"));
				comtradeDataDTO.setType(rs.getString("type"));
				comtradeDataDTO.setTsPrefault(rs.getLong("tsPrefault"));
				comtradeDataDTO.setTsTrigger(rs.getLong("tsTrigger"));
				comtradeDataDTO.setLineFreq(rs.getDouble("lineFreq"));
				comtradeDataDTO.setSampleRate(rs.getDouble("sampleRate"));
				comtradeDataDTO.setSampleCnt(rs.getInt("sampleCnt"));
				comtradeDataDTO.setAnalogs(new StringBuffer(rs.getString("analogs")));
				comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				
				if (comtradeDataDTO.getType().equalsIgnoreCase(M9kConstants.ASCII) )
				{
					asciiDataReader = rs.getCharacterStream("data");
					comtradeDataDTO.setAsciiDataReader(asciiDataReader);
				}
				else if (comtradeDataDTO.getType().equalsIgnoreCase(M9kConstants.Binary) )
				{
					binaryDataStream = rs.getBinaryStream("dataBlob");
					comtradeDataDTO.setBinaryDataSteam(binaryDataStream);
				}
					
//				System.out.println("About to read blob");
//				System.out.println("Reading blob complete...");
				comtradeDataDTO.setRecordingDevId(rs.getString("recordingDevId"));
				faultLocation = rs.getString("faultLocation");
				if (faultLocation != null)
				{
					comtradeDataDTO.setFaultLocationDetails(faultLocation);
				}
				userComments = rs.getString("comments");
				if (userComments != null)
				{
					comtradeDataDTO.setUserComments(userComments);
				}
				comtradeDataDTO.setFaultLogic(rs.getBoolean("faultLogic"));
				lineGroups = rs.getString("lineGroups");
				logger.debug("Line groups list from dat_staging "+lineGroups);
				if (lineGroups != null)
				{
					comtradeDataDTO.setLineGroups(lineGroups);
				}

//				M9kStationCreateComtradeFilesFromDB.createComtradeFiles(comtradeDataDTO);
//				System.out.println("Comtrade File name created..."+comtradeDataDTO.getFileName());
				endTime = System.currentTimeMillis();
				logger.debug("End time..."+endTime);
				
				
				logger.debug("Total time Elapsed..."+(endTime-startTime));
			}
//			System.out.println("Result set empty..");
		}
		catch (SQLException e) {
			logger.error("Error in fetching records from dat table",e);
		}
		catch (Exception e) {
			logger.error("Error in fetching records from dat table",e);
		}
		finally
		{
			try {
				if (rsLocal != null)
				{
					rsLocal.close();
					rsLocal= null;
				}
				if (rs != null)
				{
					rs.close();
					rs= null;
				}
				if (ps != null)
				{
					ps.close();
					ps= null;
				}
				if (localConnection != null)
				{
					logger.debug("COMTRADE-DB: closing connection from getLocalFaultRecordToProcess "+ dbConnCount);
					localConnection.close();
					localConnection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return comtradeDataDTO;
	}

	public int deleteFaultsFromDatStaging(String commaSeperatedFaultIds) throws M9000Exception {
		PreparedStatement ps = null;
		Connection localConnection = null;
		int numOfFaultsDeleted = 0;
		StringBuffer strbQuery;
		try 
		{
			localConnection  = M9kStationDBUtil.getLocalConnection();		
			System.out.println("CSV of faults "+commaSeperatedFaultIds);
//			System.out.println("Entered insert details...");
			strbQuery = new StringBuffer("delete from dat_staging where faultId in(");
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
			ps = localConnection.prepareStatement(strbQuery.toString());
			int iCnt = 1;
			for (int i = 0; i < arrFaultIds.length; i++) {
				ps.setString(iCnt++, arrFaultIds[i]);				
			}
			numOfFaultsDeleted = ps.executeUpdate();
				System.out.println("Rows Deleted in dat_staging..."+numOfFaultsDeleted);
				
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
		return numOfFaultsDeleted;
	}

}
