/**
 * 
 */
package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.ContinuousComtradeDTO;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

/**
 * @author sramasamy
 *
 */
public class MySqlContinuousComtradeDAO implements ContinuousComtradeDataDAO {

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlContinuousComtradeDAO.class);
	/**
	 * 
	 */
	public MySqlContinuousComtradeDAO() {
	}

	/* (non-Javadoc)
	 * @see com.usi.m9000.dao.ContinuousComtradeDataDAO#insertContinuousComtradeDetails(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void insertContinuousComtradeDetails(String contDataType,
			String startDateTime, String endDateTime, String fileName)
			throws M9000Exception {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		try
		{
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Entered insertContinuousComtradeDetails in DAO");
			int i = 0;
			ps = mysqlConn.prepareStatement("insert into continuous_comtrade_data set contDataType= ?, startDateTime = ? , endDateTime=?, fileName=?");
			ps.setString(++i, contDataType);
			ps.setString(++i, startDateTime);
			ps.setString(++i, endDateTime);
			ps.setString(++i, fileName);
			int rowsUpdated = ps.executeUpdate();
			logger.debug("Inserted the continuous comtrade data with inserted row count "+rowsUpdated);
		}
		catch (SQLException e) {
				logger.error("Error in inserting contiuous comtrade data into continuous_comtrade_data table ",e);
			} 
		catch (Exception e) {
			logger.error("Error in inserting contiuous comtrade data into continuous_comtrade_data table ",e);
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
					e.printStackTrace();
				}

	}


	}

	/* (non-Javadoc)
	 * @see com.usi.m9000.dao.ContinuousComtradeDataDAO#getListOfContinuousComtradeData()
	 * Pass ANALOG as contDataType for continuous analog data and NULL or any other string for continuous export data 
	 * As the query for cont exports data fetches data for contDataType not equal to ANALOG
	 */
	@Override
	public List<ContinuousComtradeDTO> getListOfContinuousComtradeData(String stationId, String contDatType) throws M9000Exception {
		ResultSet rs= null;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		List<ContinuousComtradeDTO> lstContinuousComtrade = new ArrayList<ContinuousComtradeDTO>();
		ContinuousComtradeDTO continuousComtradeDTO;
		logger.debug("Entered getListOfContinuousComtradeData in DAO");
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			if (M9kUtils.isRemote())
			{
				if (contDatType != null && contDatType.equalsIgnoreCase("ANALOG"))
				{
					ps = mysqlConn.prepareStatement("select * from continuous_comtrade_data where stationId = ? and contDataType = ? order by id desc");
				}
				else
				{
					ps = mysqlConn.prepareStatement("select * from continuous_comtrade_data where stationId = ? and contDataType != ? order by id desc");
				}
				ps.setString(1, stationId);
				ps.setString(2, "ANALOG");
			}
			else
			{
				if (contDatType != null && contDatType.equalsIgnoreCase("ANALOG"))
				{
					ps = mysqlConn.prepareStatement("select * from continuous_comtrade_data where contDataType = ? order by id desc");
				}
				else
				{
					ps = mysqlConn.prepareStatement("select * from continuous_comtrade_data where contDataType != ? order by id desc");
				}
				ps.setString(1, "ANALOG");

			}
			
			rs = ps.executeQuery();
			logger.debug("Executed query successfully");
			while (rs.next())
			{
				continuousComtradeDTO = new ContinuousComtradeDTO();
				continuousComtradeDTO.setId(rs.getInt("id"));
				continuousComtradeDTO.setContDataType(rs.getString("contDataType"));
				continuousComtradeDTO.setStartDateTime(rs.getString("startDateTime"));
				continuousComtradeDTO.setEndDateTime(rs.getString("endDateTime"));
				continuousComtradeDTO.setFileName(rs.getString("fileName"));
//				System.out.println("Cont comtrade read "+continuousComtradeDTO.toString());
				lstContinuousComtrade.add(continuousComtradeDTO);
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in fetching contiuous comtrade data from continuous_comtrade_data table ",e);
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
				e.printStackTrace();
			}

		}
		logger.debug("The total number of rows returned "+lstContinuousComtrade.size());
		return lstContinuousComtrade;
	}

	@Override
	public Integer getNextId(String contDatType) throws M9000Exception {
		ResultSet rs= null;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		Integer nextId = 1;
		logger.debug("Entered getNextId in DAO");
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			if (M9kUtils.isRemote())
			{
				if (contDatType != null && contDatType.equalsIgnoreCase("ANALOG"))
				{
					ps = mysqlConn.prepareStatement("select max(id) as maxId from continuous_comtrade_data where stationId = ? and contDataType = ? order by id desc");
				}
				else
				{
					ps = mysqlConn.prepareStatement("select max(id) as maxId from continuous_comtrade_data where stationId = ? and contDataType != ? order by id desc");
				}
				ps.setString(1, ""+M9kUtils.getStationId());
				ps.setString(2, "ANALOG");
			}
			else
			{
				if (contDatType != null && contDatType.equalsIgnoreCase("ANALOG"))
				{
					ps = mysqlConn.prepareStatement("select max(id) as maxId from continuous_comtrade_data where contDataType = ? order by id desc");
				}
				else
				{
					ps = mysqlConn.prepareStatement("select max(id) as maxId from continuous_comtrade_data where contDataType != ? order by id desc");
				}
				ps.setString(1, "ANALOG");

			}
			rs = ps.executeQuery();
			logger.debug("Executed query successfully");
			if (rs.next())
			{
				nextId = rs.getInt("maxId");
				logger.debug("next id from continuous table "+nextId);
				nextId+=1;
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in fetching contiuous comtrade data from continuous_comtrade_data table ",e);
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
				e.printStackTrace();
			}

		}
		logger.debug("NExt id to be returned "+nextId);
		return nextId;

	}

	@Override
	public int deleteRecords(int stationId, String commaSeperatedIds) throws M9000Exception {
		PreparedStatement ps = null;
		Connection connection = null;
		int numOfRecordsDeleted = 0;
		StringBuffer strbQuery;
		try 
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();		
			System.out.println("CSV of LTR ids "+commaSeperatedIds);
//			System.out.println("Entered insert details...");
			if (M9kUtils.isRemote())
			{
				strbQuery = new StringBuffer("delete from continuous_comtrade_data where stationid = ? and id in(");
			}
			else
			{
				strbQuery = new StringBuffer("delete from continuous_comtrade_data where id in(");
			}
			String[] arrIds = commaSeperatedIds.split(",");
			System.out.println("total contrinuous data to be deleted "+arrIds.length);
			for (int i = 0; i < arrIds.length; i++) {
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
			if (M9kUtils.isRemote())
			{
				ps.setInt(iCnt++, stationId);
			}
			for (int i = 0; i < arrIds.length; i++) {
				ps.setString(iCnt++, arrIds[i]);				
			}
			numOfRecordsDeleted = ps.executeUpdate();
				System.out.println("Rows Deleted in continuous_comtrade_data..."+numOfRecordsDeleted);
				
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
		return numOfRecordsDeleted;

	}
	
	@Override
	public int getTotalLTRCount(int stationId, String contDataType) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		int totalFaults = 0;
		StringBuffer strbufQuery = new StringBuffer("Select count(*) from continuous_comtrade_data where ");
		if (M9kUtils.isRemote())
		try
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();
			if (M9kUtils.isRemote())
			{
				if (contDataType != null && contDataType.equalsIgnoreCase(M9kConstants.ANALOG))
				{
					strbufQuery.append(" stationId = ? and contDataType = ? ");
				}
				else
				{
					strbufQuery.append(" stationId = ? and (contDataType != ?) ");
				}
			}
			else
			{
				if (contDataType != null && contDataType.equalsIgnoreCase(M9kConstants.ANALOG))
				{
					strbufQuery.append(" contDataType = ? ");
				}
				else
				{
					strbufQuery.append(" (contDataType != ?) ");
				}
				
			}
		ps = connection.prepareStatement(strbufQuery.toString());
		int iCnt = 1;
		if (M9kUtils.isRemote())
		{
			ps.setInt(iCnt++,stationId);
		}
		ps.setString(iCnt++, M9kConstants.ANALOG);
//		System.out.println("About to get total faults from station  "+stationId );
		rs = ps.executeQuery();
		if (rs.next())
		{
			totalFaults = rs.getInt(1);
		}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in fetching total Cont data  "+ contDataType+" count for station "+stationId+" Returning value as zero");
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
	public List<ContinuousComtradeDTO> searchByCriteria(int stationId, String contDataType, String searchCriteria, String orderCriteria, int from, int size)
			throws M9000Exception {

		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		List<ContinuousComtradeDTO> lstContinuousComtrade = new ArrayList<ContinuousComtradeDTO>();
		ContinuousComtradeDTO continuousComtradeDTO;
		StringBuffer strbufQuery = new StringBuffer("Select * from continuous_comtrade_data where ");
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			if (M9kUtils.isRemote())
			{
				if (contDataType != null && contDataType.equalsIgnoreCase(M9kConstants.ANALOG))
				{
					strbufQuery.append(" stationId = ? and contDataType = ? ");
				}
				else
				{
					strbufQuery.append(" stationId = ? and (contDataType != ?) ");
				}
			}
			else
			{
				if (contDataType != null && contDataType.equalsIgnoreCase(M9kConstants.ANALOG))
				{
					strbufQuery.append(" contDataType = ? ");
				}
				else
				{
					strbufQuery.append(" (contDataType != ?) ");
				}
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
//			ps = connection.prepareStatement("Select * from comtrade_details where stationId = ? and fault_id < ? limit "+from+","+size);
			ps = connection.prepareStatement(strbufQuery.toString());
			int iCnt = 1;
			if (M9kUtils.isRemote())
			{
				ps.setInt(iCnt++,stationId);
			}
			ps.setString(iCnt++,M9kConstants.ANALOG);
			ps.setInt(iCnt++,from);
			ps.setInt(iCnt++,size);
//			System.out.println("About to process from faultId " +" limited to "+(size)+" query to be executed "+strbufQuery);
			rs = ps.executeQuery();
			while (rs.next())
			{
				continuousComtradeDTO = new ContinuousComtradeDTO();
				continuousComtradeDTO.setId(rs.getInt("id"));
				continuousComtradeDTO.setContDataType(rs.getString("contDataType"));
				continuousComtradeDTO.setStartDateTime(rs.getString("startDateTime"));
				continuousComtradeDTO.setEndDateTime(rs.getString("endDateTime"));
				continuousComtradeDTO.setFileName(rs.getString("fileName"));
//				System.out.println("Cont comtrade read "+continuousComtradeDTO.toString());
				lstContinuousComtrade.add(continuousComtradeDTO);
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
		logger.debug("returning from findLesserAsId "+lstContinuousComtrade.size());
		return lstContinuousComtrade;
	
	}
	@Override
	public int countByCriteria(int stationId, String contDataType, String searchCriteria) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		int totalFaults = 0;
		try
		{
			StringBuffer strbufQuery = new StringBuffer("Select count(*) from continuous_comtrade_data where ");
			if (M9kUtils.isRemote())
			{
				if (contDataType != null && contDataType.equalsIgnoreCase(M9kConstants.ANALOG))
				{
					strbufQuery.append(" stationId = ? and contDataType = ? ");
				}
				else
				{
					strbufQuery.append(" stationId = ? and (contDataType != ?) ");
				}
			}
			else
			{
				if (contDataType != null && contDataType.equalsIgnoreCase(M9kConstants.ANALOG))
				{
					strbufQuery.append(" contDataType = ? ");
				}
				else
				{
					strbufQuery.append(" (contDataType != ?) ");
				}
			}
			if (searchCriteria != null && !searchCriteria.isEmpty())
			{
				strbufQuery.append(" and "+searchCriteria);
			}
			connection = M9kMySqlDatabase.getInstance().getConnection();
		ps = connection.prepareStatement(strbufQuery.toString());
		int iCnt = 1;
		if (M9kUtils.isRemote())
		{
			ps.setInt(iCnt++,stationId);
		}
		ps.setString(iCnt++, M9kConstants.ANALOG);
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
	public String getContinuousDataFileNames(int stationId, String csvIds) throws M9000Exception {

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int iCnt;
		StringBuffer ltrFileNames = new StringBuffer();
//		stmt.setFetchSize(Integer.MIN_VALUE);
//		String sqlQuery = "select fileName from long_term_dat where stationId= ? and faultId = ? and ltType = ?";
		StringBuffer sqlQuery;
		if (M9kUtils.isRemote())
		{
			sqlQuery = new StringBuffer("select fileName from continuous_comtrade_data where stationId= ? and id in (");
		}
		else
		{
			sqlQuery = new StringBuffer("select fileName from continuous_comtrade_data where id in (");
		}
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
		try
		{
			connection = M9kMySqlDatabase.getInstance().getConnection();
			ps = connection.prepareStatement(sqlQuery.toString());
			iCnt = 1;
			if (M9kUtils.isRemote())
			{
				ps.setInt(iCnt++, stationId);
			}
			for (int i = 0; i < arrIds.length; i++) {
				ps.setString(iCnt++, arrIds[i]);				
			}
			rs = ps.executeQuery();
			
			while (rs.next())
			{
				if (ltrFileNames.length() > 0)
				{
					ltrFileNames.append(";;");
				}
				ltrFileNames.append(rs.getString("fileName"));
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
	public int deleteRecordsWithFileNames(String commaSeperatedFileNames) throws M9000Exception {
		PreparedStatement ps = null;
		Connection connection = null;
		int numOfRecordsDeleted = 0;
		StringBuffer strbQuery;
		try 
		{
			connection = M9kStationDBUtil.getLocalConnection();		
			System.out.println("CSV of cont file names "+commaSeperatedFileNames);
			strbQuery = new StringBuffer("delete from continuous_comtrade_data where fileName in(");
			String[] arrFileNames = commaSeperatedFileNames.split(";;");
			System.out.println("total contrinuous data to be deleted "+arrFileNames.length);
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
			numOfRecordsDeleted = ps.executeUpdate();
				System.out.println("Rows Deleted in continuous_comtrade_data..."+numOfRecordsDeleted);
				
		}
		catch (SQLException e) {
			logger.error("Failed to delete continuous data ",e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("Failed to delete continuous data ",e);
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
		return numOfRecordsDeleted;
	}

}
