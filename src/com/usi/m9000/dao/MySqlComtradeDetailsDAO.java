package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.station.faultLocation.M9kLineLogicFilter;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.xml.util.M9kXMLUtils;

public class MySqlComtradeDetailsDAO implements ComtradeDataDAO {

	Connection mysqlConn;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlComtradeDetailsDAO.class);
	M9kStationComtradeUtil m9kStationComtradeUtil;
	M9kLineLogicFilter m9kLineLogicFilter;

	public MySqlComtradeDetailsDAO()
	{
//		logger.debug("In the constructor of MySqlComtradeDetailsDAO");
		m9kStationComtradeUtil = new M9kStationComtradeUtil();
		m9kLineLogicFilter = new M9kLineLogicFilter();
	}
	@Override
	public void insertComtradeDetails(ComtradeDataDTO comtradeDataDTO) throws M9000Exception{
		PreparedStatement ps = null;
		Connection connection = null;
//		org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlComtradeDetailsDAO.class);
//		logger.debug("Entered insertComtradeDetails of MySqlComtradeDetailsDAO...");
		try 
		{
			try
			{
//				logger.debug("About to c all isRemote");
				logger.debug("COMTRADE-DB: New connection from insertComtradeDetails. Is remote? " + M9kUtils.isRemote());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
//			logger.debug("After isRemote call");
			if (!M9kUtils.isRemote())
			{
				connection =  M9kStationDBUtil.getLocalConnection();
			}
			else
			{
				connection = M9kMySqlDatabase.getInstance().getConnection();
			}

//			System.out.println("Entered insert details...");
			ps = connection.prepareStatement("insert into comtrade_details(fault_id,station_id,time_stamp,events,length,pre_fault,post_fault,fault_location,fault_logic,line_groups,comments, file_name) values (?,?,?,?,?,?,?,?,?,?,?,?)");
			long startTime;
			long endTime;
			int iIndex = 1;
				startTime = System.currentTimeMillis();
//				logger.debug("Start time..."+startTime);
				ps.setInt(iIndex++, comtradeDataDTO.getFaultId());
				ps.setInt(iIndex++, comtradeDataDTO.getStationId());
				ps.setLong(iIndex++, comtradeDataDTO.getTsTrigger());
				ps.setString(iIndex++, comtradeDataDTO.getActiveEvents());
				ps.setDouble(iIndex++, comtradeDataDTO.getLength());
				ps.setDouble(iIndex++, comtradeDataDTO.getPreFault());
				ps.setDouble(iIndex++, comtradeDataDTO.getPostFault());
				ps.setString(iIndex++, comtradeDataDTO.getFaultLocationDetails());
				ps.setBoolean(iIndex++, comtradeDataDTO.isFaultLogic());
				ps.setString(iIndex++, comtradeDataDTO.getLineGroups());
				logger.debug("Line groups to be inserted into the comtrade_details "+comtradeDataDTO.getLineGroups());
				ps.setString(iIndex++, comtradeDataDTO.getUserComments());
				ps.setString(iIndex++, comtradeDataDTO.getFileName());
				int rowsInserted;
				rowsInserted = ps.executeUpdate();
				logger.debug("Rows inserted in comtrade details..."+rowsInserted);
				endTime = System.currentTimeMillis();
//				logger.debug("End time..."+endTime);
				
				logger.debug("Total time Elapsed..."+(endTime-startTime));
		}
		catch (SQLException e) {
			logger.error("Failed to insert comtrade details",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to insert comtrade details",e);
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
//					logger.info("COMTRADE-DB: closing connection from MySqlComtradeDetailsDAO ");
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public List<ComtradeDataDTO> getLstOfComtradeDetails(int stationID) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		Connection connection = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();	

			if (!M9kUtils.isFaultFilterEnabled())
			{
				ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? order by fault_id desc");
				ps.setInt(1,stationID);
			}
			else
			{
				ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and fault_logic = ? order by fault_id desc");
				ps.setInt(1,stationID);
				ps.setBoolean(2,true);
			}
			
			rs = ps.executeQuery();
			while (rs.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
				comtradeDataDTO.setStationId(rs.getInt("station_id"));
				comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
				comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
				comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
				comtradeDataDTO.setFileName(rs.getString("file_name"));
				comtradeDataDTO.setActiveEvents(rs.getString("events"));
				if (rs.getString("fault_location") != null)
				{
					comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
				}
				else
				{
					comtradeDataDTO.setFaultLocationDetails("");
				}

				comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
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
		logger.debug("returning from getLstOfComtradeDetails"+lstComtradeDataDtos.size());
		return lstComtradeDataDtos;
	}

	@Override
	public List<ComtradeDataDTO> getLstOfComtradeDetails(int stationId, String startDateTime,
			String endDateTime, List<ComtradeDataDTO> lstComtradeData, String lineGroupsToSearch) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		Connection connection = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		int maxFaultId = 0;
		String searchQueryForLineGroups = "";
//		String commaSeperatedIds = getCommaSeperatedIds (lstComtradeData);
//		if (commaSeperatedIds.isEmpty())
//		{
//			commaSeperatedIds="''";
//		}
		if (lstComtradeData != null && !lstComtradeData.isEmpty())
		{
			maxFaultId = getMaxFaultId(lstComtradeData);
//			System.out.println("MAx fault id "+maxFaultId);
		}
		if (lineGroupsToSearch != null && !lineGroupsToSearch.isEmpty())
		{
			searchQueryForLineGroups = buildSearchQueryForLineGroups(lineGroupsToSearch);
		}
//		System.out.println("commaSeperatedIds "+commaSeperatedIds);
		try {
			logger.debug("DATABASE-CONNECTION: Getting new Connection from getLstOfComtradeDetails");
			connection = M9kMySqlDatabase.getInstance().getConnection();
			int faultsResultsLimit = 5000; // Read from m9k-applet.properties file
			faultsResultsLimit = M9kUtils.getFaultsResultsLimit();
			StringBuffer queryToExecute = new StringBuffer();
			if (startDateTime != null && endDateTime != null)
			{
				queryToExecute.append("Select * from comtrade_details where station_id = ? and (from_unixTime(time_stamp div 1000000) >= ? and from_unixTime(time_stamp div 1000000) <= ?) and fault_id > ? and fault_logic like ? ");
				if (!searchQueryForLineGroups.isEmpty())
				{
					queryToExecute.append(searchQueryForLineGroups);
				}
				queryToExecute.append("order by fault_id desc limit 0, "+faultsResultsLimit);
//				ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and (from_unixTime(time_stamp div 1000000) >= ? and from_unixTime(time_stamp div 1000000) <= ?) and fault_id not in ("+commaSeperatedIds+") order by fault_id desc limit 0, "+faultsResultsLimit);
//				ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and (from_unixTime(time_stamp div 1000000) >= ? and from_unixTime(time_stamp div 1000000) <= ?) and fault_id > ? and fault_logic like ? order by fault_id desc limit 0, "+faultsResultsLimit);
				ps = connection.prepareStatement(queryToExecute.toString());
				logger.debug("Executed the query 1"+queryToExecute);
//				System.out.println("Start Date Time "+startDateTime);
//				System.out.println("End Date Time "+endDateTime);
				ps.setInt(1,stationId);
				ps.setString(2, startDateTime);
				ps.setString(3, endDateTime);
				ps.setInt(4, maxFaultId);
				ps.setString(5, (M9kUtils.isFaultFilterEnabled()?"1":"%")); // Boolean column used as String for convenience to search 
			}
			else if (startDateTime != null )
			{
				queryToExecute.append("Select * from comtrade_details where station_id = ? and from_unixTime(time_stamp div 1000000) >= ? and fault_id > ? and fault_logic like ? ");
				if (!searchQueryForLineGroups.isEmpty())
				{
					queryToExecute.append(searchQueryForLineGroups);
				}
				queryToExecute.append("order by fault_id desc limit 0, "+faultsResultsLimit);
//				ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and from_unixTime(time_stamp div 1000000) >= ? and fault_id not in ("+commaSeperatedIds+") order by fault_id desc limit 0, "+faultsResultsLimit);
//				ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and from_unixTime(time_stamp div 1000000) >= ? and fault_id > ? and fault_logic like ?  order by fault_id desc limit 0, "+faultsResultsLimit);
				ps = connection.prepareStatement(queryToExecute.toString());
				ps.setInt(1,stationId);
				ps.setString(2, startDateTime);
				ps.setInt(3, maxFaultId);
				ps.setString(4, (M9kUtils.isFaultFilterEnabled()?"1":"%")); // Boolean column used as String for convenience to search
				logger.debug("Executed the query 2"+queryToExecute);
			}
			else if (endDateTime != null )
			{
				queryToExecute.append("Select * from comtrade_details where station_id = ? and from_unixTime(time_stamp div 1000000) <= ?  and fault_id > ? and fault_logic like ?  ");
				if (!searchQueryForLineGroups.isEmpty())
				{
					queryToExecute.append(searchQueryForLineGroups);
				}
				queryToExecute.append("order by fault_id desc limit 0, "+faultsResultsLimit);
//				ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and from_unixTime(time_stamp div 1000000) <= ? and fault_id not in ("+commaSeperatedIds+") order by fault_id desc limit 0, "+faultsResultsLimit);
//				ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and from_unixTime(time_stamp div 1000000) <= ?  and fault_id > ? and fault_logic like ?  order by fault_id desc limit 0, "+faultsResultsLimit);
				ps = connection.prepareStatement(queryToExecute.toString());
				ps.setInt(1,stationId);
				ps.setString(2, endDateTime);
				ps.setInt(3, maxFaultId);
				ps.setString(4, (M9kUtils.isFaultFilterEnabled()?"1":"%")); // Boolean column used as String for convenience to search
				logger.debug("Executed the query 3");
			}
			else
			{
				queryToExecute.append("Select * from comtrade_details where station_id = ? and fault_id > ? and fault_logic like ? ");
				if (!searchQueryForLineGroups.isEmpty())
				{
					queryToExecute.append(searchQueryForLineGroups);
				}
				queryToExecute.append("order by fault_id desc limit 0, "+faultsResultsLimit);
//				ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and fault_id not in ("+commaSeperatedIds+") order by fault_id desc limit 0, "+faultsResultsLimit);
//				System.out.println("Select * from comtrade_details where station_id = ? and fault_id > ? order by fault_id desc limit 0, "+faultsResultsLimit);
//				ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and fault_id > ? and fault_logic like ?  order by fault_id desc limit 0, "+faultsResultsLimit);
				ps = connection.prepareStatement(queryToExecute.toString());
				ps.setInt(1,stationId);
				ps.setInt(2, maxFaultId);
				ps.setString(3, (M9kUtils.isFaultFilterEnabled()?"1":"%")); // Boolean column used as String for convenience to search
//				System.out.println("Fault id "+maxFaultId);
//				System.out.println("Executed the query 4 station id "+ stationId+" query: "+queryToExecute);
				
			}
			rs = ps.executeQuery();
			int i = 0;
			while (rs.next())
			{
//				System.out.println("Result set loop "+ ++i);
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
				comtradeDataDTO.setStationId(rs.getInt("station_id"));
				comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
				if (rs.getString("events") != null)
				{
					comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				}
				else
				{
					comtradeDataDTO.setEvents(new StringBuffer(""));
				}
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
				comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
				comtradeDataDTO.setFileName(rs.getString("file_name"));
				comtradeDataDTO.setActiveEvents(rs.getString("events"));
				if (rs.getString("fault_location") != null)
				{
					comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
				}
				else
				{
					comtradeDataDTO.setFaultLocationDetails("");
				}
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
				comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
				if (rs.getString("comments") != null)
				{
					comtradeDataDTO.setUserComments(rs.getString("comments"));
				}
				else
				{
					comtradeDataDTO.setUserComments("");
				}

				lstComtradeDataDtos.add(comtradeDataDTO);
//				if (rs.getInt("fault_id") == 3518)
//				{
//					System.out.println("\n\n\t\t\tFault id...."+rs.getInt("fault_id")+" fault deetails "+comtradeDataDTO.getFaultLocationDetails());
//				}
			}
		} catch (SQLException e) {
			logger.error("Error in getLstOfComtradeDetails method ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
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
					logger.debug("DATABASE-CONNECTION: closing Connection from getLstOfComtradeDetails ");
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return lstComtradeDataDtos;
	}
	
	/**
	 * @param lineGroupsToSearch
	 * @return
	 */
	private String buildSearchQueryForLineGroups(String lineGroupsToSearch) {
		StringBuffer queryString = null;
		String[] lstOfLgs = lineGroupsToSearch.split(",");
		String logicRadioButton = M9kUtils.getLogicRadioButton();
		for (int i = 0; i < lstOfLgs.length; i++) {
			if (queryString == null)
			{
				queryString = new StringBuffer(" and ( line_groups like '%"+lstOfLgs[i]+"%' ");
			}
			else
			{
				queryString.append(" "+logicRadioButton+" line_groups like '%"+lstOfLgs[i]+"%' ");
			}
		}
		if (queryString != null)
		{
			queryString.append(" ) ");
		}
		System.out.println("Search query for linegroups "+queryString);
		return queryString.toString();
	}
	@Override
	public void insertErrorComtradeDetails(ComtradeDataDTO comtradeDataDTO) throws M9000Exception {
		PreparedStatement ps = null;
		Connection connection = null;
		try 
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();		

//			System.out.println("Entered insert details...");
			ps = connection.prepareStatement("insert into comtrade_details values (?,?,?,?,?,?,?,?)");
			long startTime;
			long endTime;
				startTime = System.currentTimeMillis();
				logger.debug("Start time..."+startTime);
				ps.setInt(1, comtradeDataDTO.getFaultId());
				ps.setInt(2, comtradeDataDTO.getStationId());
				ps.setLong(3, comtradeDataDTO.getTsTrigger());
				ps.setString(4, comtradeDataDTO.getActiveEvents());
				ps.setDouble(5, comtradeDataDTO.getLength());
				ps.setDouble(6, comtradeDataDTO.getPreFault());
				ps.setDouble(7, comtradeDataDTO.getPostFault());
				ps.setString(8, comtradeDataDTO.getFileName());
				int rowsInserted;
				rowsInserted = ps.executeUpdate();
				logger.debug("Rows inserted in comtrade details..."+rowsInserted);
				endTime = System.currentTimeMillis();
				logger.debug("End time..."+endTime);
				
				logger.debug("Total time Elapsed..."+(endTime-startTime));
		}
		catch (SQLException e) {
			logger.error("Failed to insert into error comtrade ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to insert into error comtrade ",e);
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

	}

	@Override
	public int deleteFaults(int stationId, String commaSeperatedFaultIds) throws M9000Exception {
		PreparedStatement ps = null;
		Connection connection = null;
		int numOfFaultsDeleted = 0;
		StringBuffer strbQuery;
		try 
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();		
			System.out.println("CSV of faults "+commaSeperatedFaultIds);
//			System.out.println("Entered insert details...");
			strbQuery = new StringBuffer("delete from comtrade_details where station_id = ? and fault_id in(");
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

	/**
	 * @return the mysqlConn
	 */
	public Connection getMysqlConn() {
		return mysqlConn;
	}
	/**
	 * @param mysqlConn the mysqlConn to set
	 */
	public void setMysqlConn(Connection mysqlConn) {
		this.mysqlConn = mysqlConn;
	}

	private String buildCommaSeperatedIds (List<String> lstFaults)
	{
		StringBuffer strValue = new StringBuffer();
		String faultId;
    	for (Iterator<String> iterator = lstFaults.iterator(); iterator
				.hasNext();) {
    		faultId = iterator.next();
			if (strValue.length() == 0)
			{
				strValue.append(faultId);
			}
			else
			{
				strValue.append(","+faultId);
			}
			
		}
		
		return strValue.toString();
	}
	
	private int getMaxFaultId (List<ComtradeDataDTO> lstComtradeData)
	{
		int maxFaultId = 0;
//		System.out.println("Fault id before sort "+lstComtradeData.get(0).getFaultId());
		Collections.sort(lstComtradeData, new Comparator<ComtradeDataDTO>() {

			@Override
			public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
				return ((o1.getFaultId() > o2.getFaultId())? -1 : (o1.getFaultId() == o2.getFaultId())?0:1);
			}
		});
		maxFaultId = lstComtradeData.get(0).getFaultId();
//		System.out.println("Fault id to bse sent after sort "+maxFaultId);
		return maxFaultId;
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

			ps = connection.prepareStatement("update comtrade_details set comments=? where station_id = ? and fault_id = ?");
			ps.setString(1, comtradeDataDTO.getUserComments());
			ps.setInt(2,Integer.parseInt(stationId));
			ps.setInt(3, comtradeDataDTO.getFaultId());
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Failed to update comments in comtrade details for (stationId,faultId) ("+stationId+","+comtradeDataDTO.getFaultId()+")",e);
			throw new M9000Exception(e);
		}catch (Exception e) {
			logger.error("Failed to update comments in comtrade details for (stationId,faultId) ("+stationId+","+comtradeDataDTO.getFaultId()+")",e);
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
	/* (non-Javadoc)
	 * @see com.usi.m9000.dao.ComtradeDataDAO#getLstOfFaultsFrom(int)
	 * Fetches faults in batches from the specified faultId
	 */
	@Override
	public int updateFaultsBooleanLogic(int stationId)
			throws M9000Exception {
		int numberOfFaultsUpdated = 0;
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		Connection connection = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		List<String> lstActiveEvents;
		String associatedLineGroups = "";
		boolean booFautLogic = false;
		int faultId = 1;
		int iIndex = 1;
		
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();	
			int faultsResultsLimit = M9kUtils.getFaultsResultsLimit();
			
			while(true)
			{
				lstComtradeDataDtos.clear();
				ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and fault_id >= ? order by fault_id limit 0,"+faultsResultsLimit);
				ps.setInt(1,stationId);
				ps.setInt(2,faultId);
//				System.out.println("About to process from faultId "+faultId +" limited to "+(faultId+faultsResultsLimit-1));
				rs = ps.executeQuery();
				while (rs.next())
				{
					comtradeDataDTO = new ComtradeDataDTO();
					comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
					comtradeDataDTO.setStationId(rs.getInt("station_id"));
					comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
					comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
					comtradeDataDTO.setLength(rs.getDouble("length"));
					comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
					comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
					comtradeDataDTO.setFileName(rs.getString("file_name"));
					comtradeDataDTO.setActiveEvents(rs.getString("events"));
					if (rs.getString("fault_location") != null)
					{
						comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
					}
					else
					{
						comtradeDataDTO.setFaultLocationDetails("");
					}
	
					comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
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
				rs.close();
				ps.close();
				if (lstComtradeDataDtos != null && !lstComtradeDataDtos.isEmpty())
				{
					connection.setAutoCommit(false);  
					ps = connection.prepareStatement("update comtrade_details set fault_logic = ? , line_groups = ? where station_id = ? and fault_id = ?");
					for (Iterator<ComtradeDataDTO> iterator = lstComtradeDataDtos.iterator(); iterator
							.hasNext();) {
						comtradeDataDTO = iterator
								.next();
						if (comtradeDataDTO.getActiveEvents() == null || comtradeDataDTO.getActiveEvents().isEmpty())
						{
							iterator.remove();
//							System.out.println("Removed from list as there is no active events "+comtradeDataDTO);
							continue;
						}
//						System.out.println("Process for fault Id "+comtradeDataDTO.getFaultId());
						lstActiveEvents = getActiveEventsIndex(comtradeDataDTO.getActiveEvents());
						associatedLineGroups = checkLogic(lstActiveEvents); 
//						System.out.println("Associated line groups "+associatedLineGroups);
						booFautLogic = ((associatedLineGroups == null)?false:true); 
//						System.out.println("LOgic for fault Id "+comtradeDataDTO.getFaultId()+" is "+booFautLogic);
						ps.setBoolean(iIndex++, booFautLogic);
						if (associatedLineGroups == null)
						{
							associatedLineGroups = "";
						}
						ps.setString(iIndex++, associatedLineGroups);
						ps.setString(iIndex++,""+stationId);
						ps.setInt(iIndex++,comtradeDataDTO.getFaultId());
						ps.addBatch();
						iIndex = 1;
					}
					faultId+=faultsResultsLimit-1;

					int resultsUpdates[] = ps.executeBatch();
					System.out.println("Results updated length "+resultsUpdates.length);
					ps.close();
					for (int i = 0; i < resultsUpdates.length; i++) {
						if (resultsUpdates[i] == Statement.EXECUTE_FAILED)
						{
							System.out.println("Update failed at "+i);
						}
						else
						{
							numberOfFaultsUpdated+=resultsUpdates[i];
						}
					}
					 connection.commit();
				}
				else
				{
					System.out.println("Faults List is empty. Quitting.");
					connection.close();
					break;
				}
		}
			
		} catch (SQLException e) {
			logger.error("Failed to update list of comtrade details",e);
			throw new M9000Exception(e);
		}catch (Exception e) {
			logger.error("Failed to update list of comtrade details",e);
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

		System.out.println("Number of faults updated  "+numberOfFaultsUpdated);
		return numberOfFaultsUpdated;
	}

	private List<String> getActiveEventsIndex(String activeEvents)
	{
		List<String> lstActiveEvents = null;
		String[] strActiveEvents;
		int index = -1;
		if (activeEvents != null && !activeEvents.isEmpty())
		{
//			System.out.println("INdex of newline? "+activeEvents.indexOf("\\r?\\n"));
			strActiveEvents = activeEvents.split("\\r?\\n");
//			System.out.println("String array length "+strActiveEvents.length);
			lstActiveEvents = new ArrayList<String>(strActiveEvents.length);
			for (int i = 0; i < strActiveEvents.length; i++) {
//				System.out.println("Events string: "+strActiveEvents[i]);
				index = strActiveEvents[i].indexOf("-");
				lstActiveEvents.add(strActiveEvents[i].substring(0, index-1).trim());
			}
		}
//		System.out.println("List of event strings "+lstActiveEvents);
		return lstActiveEvents;
	}
	
	private List<String> splitActiveEvents(String activeEvents)
	{
		List<String> lstActiveEvents = new ArrayList<String>();
		String[] strActiveEvents;
		if (activeEvents != null && !activeEvents.isEmpty())
		{
			strActiveEvents = activeEvents.split("\\r?\\n");
			for (int i = 0; i < strActiveEvents.length; i++) {
				logger.debug("Split Events string: "+strActiveEvents[i]);
				lstActiveEvents.add(strActiveEvents[i]);
			}
		}
		logger.debug("List of event strings "+lstActiveEvents);
		return lstActiveEvents;
	}
	private String checkLogic(List<String> lstActiveEvents)
	{
		boolean booLogic;
		String strTrueLinegroups = null;
		LineGroupsAlgorithm lineGroupsAlgorithm;
		List<LineGroupsAlgorithm> lstLineGroups;
		
		try
		{
			lstLineGroups = M9kXMLUtils.getLstLineGroups(M9kUtils.getStationDetails().getConfigXml());
			for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator.hasNext();) {
				lineGroupsAlgorithm = iterator.next();
//				System.out.println("About to check Logic for linegroup name "+lineGroupsAlgorithm.getLineGroupName());
				if (lineGroupsAlgorithm.getDecisionLogic() != null && !lineGroupsAlgorithm.getDecisionLogic().isEmpty() && !lineGroupsAlgorithm.getDecisionLogic().equalsIgnoreCase("NONE"))
				{
//					System.out.println("LineGroup decision logic "+lineGroupsAlgorithm.getDecisionLogic());
					booLogic = m9kLineLogicFilter.isLogicTrue(lstActiveEvents, lineGroupsAlgorithm.getDecisionLogic());
					if (booLogic)
					{
						if (strTrueLinegroups == null)
						{
							strTrueLinegroups = lineGroupsAlgorithm.getName();
						}
						else
						{
							strTrueLinegroups = strTrueLinegroups.concat(", "+lineGroupsAlgorithm.getName());
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception in updating comtrade details table "+e.getMessage());
		}
//		logger.info("Line groups list "+strTrueLinegroups);
		return strTrueLinegroups;
	}
	@Override
	public int getTotalFaultsCount(int stationId) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		int totalFaults = 0;
		try
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();
		ps = connection.prepareStatement("Select count(*) from comtrade_details where station_id = ?");
		ps.setInt(1,stationId);
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
	public ComtradeDataDTO findById(int stationId, int faultId) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		ComtradeDataDTO comtradeDataDTO = null;
		try
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();
		ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and fault_id = ?");
		ps.setInt(1,stationId);
		ps.setInt(2,faultId);
//		System.out.println("About to get total faults from station  "+stationId );
		rs = ps.executeQuery();
		if (rs.next())
		{
			comtradeDataDTO = new ComtradeDataDTO();
			comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
			comtradeDataDTO.setStationId(rs.getInt("station_id"));
			comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
			comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
			comtradeDataDTO.setLength(rs.getDouble("length"));
			comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
			comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
			comtradeDataDTO.setFileName(rs.getString("file_name"));
			comtradeDataDTO.setActiveEvents(rs.getString("events"));
			if (rs.getString("fault_location") != null)
			{
				comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
			}
			else
			{
				comtradeDataDTO.setFaultLocationDetails("");
			}

			comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
			if (rs.getString("comments") != null)
			{
				comtradeDataDTO.setUserComments(rs.getString("comments"));
			}
			else
			{
				comtradeDataDTO.setUserComments("");
			}
			
		}
		rs.close();
		ps.close();

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

		return comtradeDataDTO;
	}
	@Override
	public List<ComtradeDataDTO> findLesserAsId(int stationId, int faultId, int from, int size) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		Connection connection = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();	
			ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and fault_id < ? limit "+from+","+size);
			ps.setInt(1,stationId);
			ps.setInt(2,faultId);
//			System.out.println("About to process from faultId "+faultId +" limited to "+(size));
			rs = ps.executeQuery();
			while (rs.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
				comtradeDataDTO.setStationId(rs.getInt("station_id"));
				comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
				comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
				comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
				comtradeDataDTO.setFileName(rs.getString("file_name"));
				comtradeDataDTO.setActiveEvents(rs.getString("events"));
				if (rs.getString("fault_location") != null)
				{
					comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
				}
				else
				{
					comtradeDataDTO.setFaultLocationDetails("");
				}

				comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
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
		logger.debug("returning from findLesserAsId "+lstComtradeDataDtos.size());
		return lstComtradeDataDtos;
	}
	@Override
	public List<ComtradeDataDTO> findGreaterAsId(int stationId, int faultId, int from, int size) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		Connection connection = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();	
			ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and fault_id > ? limit "+from+","+size);
			ps.setInt(1,stationId);
			ps.setInt(2,faultId);
//			System.out.println("About to process from faultId "+faultId +" limited to "+(size));
			rs = ps.executeQuery();
			while (rs.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
				comtradeDataDTO.setStationId(rs.getInt("station_id"));
				comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
				comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
				comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
				comtradeDataDTO.setFileName(rs.getString("file_name"));
				comtradeDataDTO.setActiveEvents(rs.getString("events"));
				if (rs.getString("fault_location") != null)
				{
					comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
				}
				else
				{
					comtradeDataDTO.setFaultLocationDetails("");
				}

				comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
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
		logger.debug("returning from findGreaterAsId "+lstComtradeDataDtos.size());
		return lstComtradeDataDtos;
	}
	@Override
	public List<ComtradeDataDTO> getFaults(int stationId, int from, int size) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		Connection connection = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();	
			ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? limit "+from+","+size);
			ps.setInt(1,stationId);
//			System.out.println("About to process from faultId "+from +" limited to "+(size));
			rs = ps.executeQuery();
			while (rs.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
				comtradeDataDTO.setStationId(rs.getInt("station_id"));
				comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
				comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
				comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
				comtradeDataDTO.setFileName(rs.getString("file_name"));
				comtradeDataDTO.setActiveEvents(rs.getString("events"));
				if (rs.getString("fault_location") != null)
				{
					comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
				}
				else
				{
					comtradeDataDTO.setFaultLocationDetails("");
				}

				comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
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
		logger.debug("returning from getFaults "+lstComtradeDataDtos.size());
		return lstComtradeDataDtos;
	}
	@Override
	public List<ComtradeDataDTO> searchByCriteria(int stationId, String searchCriteria, String orderCriteria, int from, int size)
			throws M9000Exception {

		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		Connection connection = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		StringBuffer strbufQuery = new StringBuffer("Select * from comtrade_details where station_id = ? ");
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
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
			logger.debug("searchByCriteria: About to execute sql "+strbufQuery.toString());
			ps = connection.prepareStatement(strbufQuery.toString());
			int iCnt = 1;
			ps.setInt(iCnt++,stationId);
			ps.setInt(iCnt++,from);
			ps.setInt(iCnt++,size);
//			System.out.println("About to process from faultId " +" limited to "+(size)+" query to be executed "+strbufQuery);
			rs = ps.executeQuery();
			while (rs.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
				comtradeDataDTO.setStationId(rs.getInt("station_id"));
				comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
				comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
				comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
				comtradeDataDTO.setFileName(rs.getString("file_name"));
				comtradeDataDTO.setActiveEvents(rs.getString("events"));
				if (rs.getString("fault_location") != null)
				{
					comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
				}
				else
				{
					comtradeDataDTO.setFaultLocationDetails("");
				}

				comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
				if (rs.getString("comments") != null)
				{
					comtradeDataDTO.setUserComments(rs.getString("comments"));
				}
				else
				{
					comtradeDataDTO.setUserComments("");
				}
				
				if (rs.getString("line_groups") != null)
				{
					comtradeDataDTO.setLineGroups(rs.getString("line_groups"));
				}
				else
				{
					comtradeDataDTO.setLineGroups("");
				}
				lstComtradeDataDtos.add(comtradeDataDTO);
//				System.out.println("Fault id...."+rs.getInt("fault_id"));
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
		logger.debug("returning from findLesserAsId "+lstComtradeDataDtos.size());
		return lstComtradeDataDtos;
	
	}
	@Override
	public int countByCriteria(int stationId, String searchCriteria) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		int totalFaults = 0;
		try
		{
			StringBuffer strbufQuery = new StringBuffer("Select count(*) from comtrade_details where station_id = ? ");
			if (searchCriteria != null && !searchCriteria.isEmpty())
			{
				strbufQuery.append(" and "+searchCriteria);
			}
			connection = M9kMySqlDatabase.getInstance().getConnection();
		ps = connection.prepareStatement(strbufQuery.toString());
		ps.setInt(1,stationId);
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
	public List<ComtradeDataDTO> getFaultFileNames(int stationId, String csvFaultIds) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		Connection connection = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();	
			ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and fault_id in (?)");
			ps.setInt(1,stationId);
			ps.setString(2,csvFaultIds);
//			System.out.println("About to process from faultId "+faultId +" limited to "+(size));
			rs = ps.executeQuery();
			while (rs.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
				comtradeDataDTO.setStationId(rs.getInt("station_id"));
				comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
				comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
				comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
				comtradeDataDTO.setFileName(rs.getString("file_name"));
				comtradeDataDTO.setActiveEvents(rs.getString("events"));
				if (rs.getString("fault_location") != null)
				{
					comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
				}
				else
				{
					comtradeDataDTO.setFaultLocationDetails("");
				}

				comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
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
		logger.debug("returning from findGreaterAsId "+lstComtradeDataDtos.size());
		return lstComtradeDataDtos;

	}
	@Override
	public List<String> getDistinctActiveEvents(int stationId) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		Set<String> linkedHashSet = new LinkedHashSet<String>(); // For unique list
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();	
			ps = connection.prepareStatement("Select distinct events from comtrade_details where station_id = ? ");
			ps.setInt(1,stationId);
//			System.out.println("About to process from faultId "+faultId +" limited to "+(size));
			rs = ps.executeQuery();
			String events;
			List<String> lstSplitActiveEvents;
			while (rs.next())
			{
				events = rs.getString("events");
				logger.debug("events from db "+events);
				lstSplitActiveEvents = splitActiveEvents(events);
				linkedHashSet.addAll(lstSplitActiveEvents);
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
		logger.debug("Returning distinct active events "+linkedHashSet);
		return new LinkedList<String>(linkedHashSet);

	}

	/**
	 * 21-Feb-2020 - Check if fault already exists before creating a fault. Implemented for local with remote push architecture
	 */
	@Override
	public ComtradeDataDTO getLocalFaultById(int stationId, int faultId) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		ComtradeDataDTO comtradeDataDTO = null;
		try
		{
			connection = M9kStationDBUtil.getLocalConnection();
		ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and fault_id = ?");
		ps.setInt(1,stationId);
		ps.setInt(2,faultId);
//		System.out.println("About to get total faults from station  "+stationId );
		rs = ps.executeQuery();
		if (rs.next())
		{
			comtradeDataDTO = new ComtradeDataDTO();
			comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
			comtradeDataDTO.setStationId(rs.getInt("station_id"));
			comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
			comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
			comtradeDataDTO.setLength(rs.getDouble("length"));
			comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
			comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
			comtradeDataDTO.setFileName(rs.getString("file_name"));
			comtradeDataDTO.setActiveEvents(rs.getString("events"));
			if (rs.getString("fault_location") != null)
			{
				comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
			}
			else
			{
				comtradeDataDTO.setFaultLocationDetails("");
			}

			comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
			if (rs.getString("comments") != null)
			{
				comtradeDataDTO.setUserComments(rs.getString("comments"));
			}
			else
			{
				comtradeDataDTO.setUserComments("");
			}
			
		}
		rs.close();
		ps.close();

		}
		catch (Exception e) {
			comtradeDataDTO = null;
			throw new M9000Exception("Error in getting local fault with faultId "+ faultId+" for station "+stationId+" Returning null", e);
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

		return comtradeDataDTO;

	}
	
	public List<ComtradeDataDTO> getLstOfOldestOfFaultsToDelete(int stationId, int count, int retainMinDays) throws M9000Exception
	{
		PreparedStatement ps = null;
		ResultSet rs= null;
		ComtradeDataDTO comtradeDataDTO = null;
		Connection connection = null;
		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
		
		try {
			connection = M9kStationDBUtil.getLocalConnection();	
			ps = connection.prepareStatement("Select * from comtrade_details where station_id = ? and date(from_unixtime(time_stamp div 1000000)) < date(DATE_SUB(now(), interval "+retainMinDays+" day)) order by time_stamp limit "+count);
			ps.setInt(1,stationId);
//			System.out.println("About to process from faultId "+from +" limited to "+(size));
			rs = ps.executeQuery();
			while (rs.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
				comtradeDataDTO.setStationId(rs.getInt("station_id"));
				comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
				comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
				comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
				comtradeDataDTO.setFileName(rs.getString("file_name"));
				comtradeDataDTO.setActiveEvents(rs.getString("events"));
				if (rs.getString("fault_location") != null)
				{
					comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
				}
				else
				{
					comtradeDataDTO.setFaultLocationDetails("");
				}

				comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
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
			rs.close();
			ps.close();
		} catch (SQLException e) {
			logger.error("Failed to get list of oldest comtrade details",e);
			throw new M9000Exception(e);
		}catch (Exception e) {
			logger.error("Failed to get list of oldest comtrade details",e);
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
		logger.debug("returning from getoldestFaults "+lstComtradeDataDtos.size());
		return lstComtradeDataDtos;
	}
}
