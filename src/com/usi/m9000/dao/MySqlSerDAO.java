package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.M9kSerDTO;
import com.usi.m9000.util.M9kUtils;

public class MySqlSerDAO implements M9kSerDAO {

//	Connection mysqlConn;
	Connection connection = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlSerDAO.class);
	public MySqlSerDAO()
	{
	}
	@Override
	public List<M9kSerDTO> getLstOfSerData(int stationId, String startDateTime, String endDateTime, String events) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		M9kSerDTO serDTO = null;
		List<M9kSerDTO> lstSerDtos = new ArrayList<M9kSerDTO>();
		logger.debug("Start date Time: "+startDateTime);
		logger.debug("End date Time: "+endDateTime);
		logger.debug("Events to be queried.."+events);
		StringBuffer strQueryBuf;
//		if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
		if (M9kUtils.isRemote())
		{
			strQueryBuf = new StringBuffer("Select * from ser where stationId = ? and ");	
		}
		else
		{
			strQueryBuf = new StringBuffer("Select * from ser where ");
		}
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			int i = 0;
			if (startDateTime != null && endDateTime != null)
			{
				if (events != null && !events.isEmpty())
				{
//					logger.debug("In here..."+ "Query: Select * from ser where (from_unixTime(ts div 1000000) >= ? and from_unixTime(ts div 1000000) <= ? and eventNum in ("+events+")) order by ts desc   ");
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and (from_unixTime(ts div 1000000) >= ? and from_unixTime(ts div 1000000) <= ? and eventNum in ("+events+")) order by ts desc  ");
//					strQueryBuf.append(" (from_unixTime(ts div 1000000) >= ? and from_unixTime(ts div 1000000) <= ? and eventNum in ("+events+")) order by ts desc  ");
					strQueryBuf.append(" ((ts between unix_timestamp(?)*1000000 and unix_timestamp (?)*1000000) and eventNum in ("+events+")) order by ts desc  ");
				}
				else
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and (from_unixTime(ts div 1000000) >= ? and from_unixTime(ts div 1000000) <= ? ) order by ts desc  ");
//					strQueryBuf.append(" (from_unixTime(ts div 1000000) >= ? and from_unixTime(ts div 1000000) <= ? ) order by ts desc  ");
					strQueryBuf.append(" (ts between unix_timestamp(?)*1000000 and unix_timestamp (?)*1000000 ) order by ts desc  ");
				}
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getSerResultsLimit());
//				System.out.println("Query for selected date range "+strQueryBuf.toString());
				ps = connection.prepareStatement(strQueryBuf.toString());
//				if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				if (M9kUtils.isRemote())
				{
					ps.setInt(++i, stationId);
				}
					ps.setString(++i, startDateTime);
					ps.setString(++i, endDateTime);
//					System.out.println("Query for selected date range "+strQueryBuf.toString());
					System.out.println("startDateTime "+startDateTime);
					System.out.println("endDateTime "+endDateTime);

			}
			else if (startDateTime != null )
			{
				if (events != null && !events.isEmpty())
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and  from_unixTime(ts div 1000000) >= ? and eventNum in ("+events+") order by ts desc  ");
//					strQueryBuf.append(" from_unixTime(ts div 1000000) >= ? and eventNum in ("+events+") order by ts desc  ");
					strQueryBuf.append(" ts >= unix_timestamp(?)*1000000 and eventNum in ("+events+") order by ts desc  ");
				}
				else
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and from_unixTime(ts div 1000000) >= ? order by ts desc  ");
//					strQueryBuf.append(" from_unixTime(ts div 1000000) >= ? order by ts desc  ");
					strQueryBuf.append(" ts >= unix_timestamp(?)*1000000  order by ts desc  ");
				}
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getSerResultsLimit());
				ps = connection.prepareStatement(strQueryBuf.toString());
//				if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				if (M9kUtils.isRemote())
				{
					ps.setInt(++i, stationId);
				}
					ps.setString(++i, startDateTime);
			}
			else if (endDateTime != null)
			{
				if (events != null && !events.isEmpty())
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and from_unixTime(ts div 1000000) <= ? and eventNum in ("+events+") order by ts desc  ");
//					strQueryBuf.append(" from_unixTime(ts div 1000000) <= ? and eventNum in ("+events+") order by ts desc  ");
					strQueryBuf.append(" ts <= unix_timestamp(?)*1000000 and eventNum in ("+events+") order by ts desc  ");
				}
				else
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and from_unixTime(ts div 1000000) <= ? order by ts desc  ");
//					strQueryBuf.append(" from_unixTime(ts div 1000000) <= ? order by ts desc  ");
					strQueryBuf.append(" ts <= unix_timestamp(?)*1000000 order by ts desc  ");
				}
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getSerResultsLimit());
//				System.out.println("SER search query "+strQueryBuf);
				ps = connection.prepareStatement(strQueryBuf.toString());
//				if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				if (M9kUtils.isRemote())
				{
					ps.setInt(++i, stationId);
				}
					ps.setString(++i, endDateTime);
			}
			else
			{
				if (events != null && !events.isEmpty())
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and eventNum in ("+events+") order by ts desc  ");
					strQueryBuf.append(" eventNum in ("+events+") order by ts desc  ");
				}
				else
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? order by ts desc  ");
					strQueryBuf.setLength(0);
//					if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
					if (M9kUtils.isRemote())
					{
						// START: 13-Oct-2014 Modified the query to select data for the recent date. To avoid out of memory issue
						strQueryBuf.append("Select * from ser where stationId = ? order by id desc");
//						strQueryBuf.append("SELECT s.* FROM ser s INNER JOIN "); 
//						strQueryBuf.append(" (SELECT stationId, from_unixTime(MAX(ts) div 1000000, \"%Y/%m/%d\") AS MaxTs FROM ser where stationId = ?) qMaxTs "); 
//						strQueryBuf.append(" ON from_unixTime(ts div 1000000, \"%Y/%m/%d\") = qMaxTs.MaxTs and s.stationId = qMaxTs.stationId order by ts desc  ");
						// End
					}
					else
					{
						// START: 13-Oct-2014 Modified the query to select data for the recent date. To avoid out of memory issue
						strQueryBuf.append(" Select * from ser order by id desc  ");
//						strQueryBuf.append("SELECT s.* FROM ser s INNER JOIN "); 
//						strQueryBuf.append(" (SELECT from_unixTime(MAX(ts) div 1000000, \"%Y/%m/%d\") AS MaxTs FROM ser) qMaxTs "); 
//						strQueryBuf.append(" ON from_unixTime(ts div 1000000, \"%Y/%m/%d\") = qMaxTs.MaxTs order by ts desc  ");
						// End
					}
				}
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getSerResultsLimit());
				ps = connection.prepareStatement(strQueryBuf.toString());
				
//				if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				if (M9kUtils.isRemote())
				{
					ps.setInt(++i, stationId);
				}
			}


			rs = ps.executeQuery();
			while (rs.next())
			{
				serDTO =  new M9kSerDTO();
//				if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				if (M9kUtils.isRemote())
				{
					serDTO.setStationId(rs.getInt("stationId"));
				}
				serDTO.setEventNum(rs.getInt("eventNum"));
				serDTO.setPhase(rs.getString("phase"));
				serDTO.setName(rs.getString("name"));
				serDTO.setTimeStamp(rs.getLong("ts"));
				serDTO.setNormal(rs.getInt("normal"));
				serDTO.setState(rs.getInt("state"));
				serDTO.setLocked(rs.getInt("locked"));
				lstSerDtos.add(serDTO);
			}
		} catch (SQLException e) {
			logger.error("SQL Exception in getLstOfSerData ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("SQL Exception in getLstOfSerData ", e);
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
				logger.warn("Exception while clean up ", e);
			}
		}

		logger.debug("Total ser data read..."+lstSerDtos.size());
		return lstSerDtos;
	}

	public static void main(String[] args)
	{
//		MySqlSerDAO test = new MySqlSerDAO();
//		List<String> tstLst = test.getAllAvailableSerDates(123);
//		for (Iterator<String> iterator = tstLst.iterator(); iterator.hasNext();) {
//			String string = iterator.next();
//			System.out.println(string);
//		}
	}
	/**
	 * Get all available dates if limit is set to -1 else limit the result set to given limit
	 */
	@Override
	public List<String> getAllAvailableSerDates(int stationId, int limit) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		List<String> lstAvailableDates = new ArrayList<String>();
		StringBuffer strQueryBuf;

//		if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
		if (M9kUtils.isRemote())
		{
			// START: 02-Dec-2019: to address huge SER data
//			strQueryBuf = new StringBuffer("Select distinct from_unixTime(ts div 1000000, \"%Y/%m/%d\") as ts from ser where stationId = ? order by ts desc");	
			// START: 11-May-2020: to address huge SER data
//			strQueryBuf = new StringBuffer("select distinct from_unixTime (ts, \"%Y/%m/%d\") as ts from (Select distinct (ts div 1000000) as ts from ser where stationId = ? order by ts desc) a");
			strQueryBuf = new StringBuffer("select distinct  ser_date from ser where stationId = ?  order by  ser_date desc ");
			// END: 11-May-2020
			// END: 02-Dec-2019
		}
		else
		{
			// START: 02-Dec-2019: to address huge SER data
//			strQueryBuf = new StringBuffer("Select distinct from_unixTime(ts div 1000000, \"%Y/%m/%d\") as ts from ser order by ts desc");
			
//			strQueryBuf = new StringBuffer("select distinct from_unixTime (ts, \"%Y/%m/%d\") as ts from (Select distinct (ts div 1000000) as ts from ser order by ts desc) a");
			strQueryBuf = new StringBuffer("select distinct ser_date from ser order by  ser_date desc ");
			// END: 11-May-2020
			// END: 02-Dec-2019
		}
		if (limit > 0)
		{
			strQueryBuf.append(" limit 0, "+limit);
		}
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
//			ps = connection.prepareStatement("Select distinct from_unixTime(ts div 1000000, \"%Y/%m/%d\") as ts from ser where stationId = ? order by ts desc");
			ps = connection.prepareStatement(strQueryBuf.toString());
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				ps.setInt(1, stationId);
			}
			
			rs = ps.executeQuery();
			while (rs.next())
			{
				lstAvailableDates.add(rs.getString("ser_date"));
			}
		} catch (SQLException e) {
			logger.error("SQL Exception in getAllAvailableSerDates ", e);
			throw new M9000Exception(e);
		}catch (Exception e) {
			logger.error("SQL Exception in getAllAvailableSerDates ", e);
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
				logger.warn("Exception while clean up ", e);
			}
		}

//		System.out.println("Time taken to fetch all dates "+(end-start));
		logger.debug("Total ser data read..."+lstAvailableDates.size());
		return lstAvailableDates;

	}
	
	@SuppressWarnings("unused")
	private String getDisplayDate(long timeStamp) {
	     String displayDate = new java.text.SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date (timeStamp/1000));
		return displayDate;
	}
	@Override
	public List<M9kSerDTO> getLstOfSerDataForDates(int stationId, String startDate,
			String endDate, String events) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		M9kSerDTO serDTO = null;
		List<M9kSerDTO> lstSerDtos = new ArrayList<M9kSerDTO>();
		logger.debug("Start date: "+startDate);
		logger.debug("End date: "+endDate);
		logger.debug("Events to be queried.."+events);
		int i = 0;
		StringBuffer strQueryBuf;
//		if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
		if (M9kUtils.isRemote())
		{
			strQueryBuf = new StringBuffer("Select * from ser where stationId = ? and ");	
		}
		else
		{
			strQueryBuf = new StringBuffer("Select * from ser where ");
		}
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			if (startDate != null && endDate!= null)
			{
				if (events != null && !events.isEmpty())
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and (from_unixTime(ts div 1000000, \"%Y/%m/%d\") >= ? and from_unixTime(ts div 1000000, \"%Y/%m/%d\") <= ? and eventNum in ("+events+")) order by ts desc  ");
//					strQueryBuf.append(" (from_unixTime(ts div 1000000, \"%Y/%m/%d\") >= ? and from_unixTime(ts div 1000000, \"%Y/%m/%d\") <= ? and eventNum in ("+events+")) order by ts desc  ");
					strQueryBuf.append(" (from_unixTime(ts div 1000000, \"%Y/%m/%d\") >= ? and from_unixTime(ts div 1000000, \"%Y/%m/%d\") <= ? and eventNum in ("+events+")) order by ts desc  ");
				}
				else
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and (from_unixTime(ts div 1000000, \"%Y/%m/%d\") >= ? and from_unixTime(ts div 1000000, \"%Y/%m/%d\") <= ?) order by ts desc  ");
					strQueryBuf.append(" (from_unixTime(ts div 1000000, \"%Y/%m/%d\") >= ? and from_unixTime(ts div 1000000, \"%Y/%m/%d\") <= ?) order by ts desc  ");
				}
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getSerResultsLimit());
				ps = connection.prepareStatement(strQueryBuf.toString());
				ps.setInt(1, stationId);
				ps.setString(2, startDate);
				ps.setString(3, endDate);
			}
			else if (startDate != null )
			{
				if (events != null && !events.isEmpty())
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and from_unixTime(ts div 1000000, \"%Y/%m/%d\") >= ? and eventNum in ("+events+") order by ts desc  ");
					strQueryBuf.append(" from_unixTime(ts div 1000000, \"%Y/%m/%d\") >= ? and eventNum in ("+events+") order by ts desc  ");
				}
				else
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and from_unixTime(ts div 1000000, \"%Y/%m/%d\") >= ? order by ts desc  ");
					strQueryBuf.append(" from_unixTime(ts div 1000000, \"%Y/%m/%d\") >= ? order by ts desc  ");
				}
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getSerResultsLimit());
				ps = connection.prepareStatement(strQueryBuf.toString());
//				if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				if (M9kUtils.isRemote())
				{
					ps.setInt(++i, stationId);
				}
				ps.setString(++i, startDate);
			}
			else if (endDate!= null)
			{
				if (events != null && !events.isEmpty())
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and from_unixTime(ts div 1000000, \"%Y/%m/%d\") <= ? and eventNum in ("+events+") order by ts desc  ");
					strQueryBuf.append(" from_unixTime(ts div 1000000, \"%Y/%m/%d\") <= ? and eventNum in ("+events+") order by ts desc  ");
				}
				else
				{
//					ps = connection.prepareStatement("Select * from ser where stationId = ? and from_unixTime(ts div 1000000, \"%Y/%m/%d\") <= ? order by ts desc  ");
					strQueryBuf.append(" from_unixTime(ts div 1000000, \"%Y/%m/%d\") <= ? order by ts desc  ");
				}
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getSerResultsLimit());
				ps = connection.prepareStatement(strQueryBuf.toString());
//				if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				if (M9kUtils.isRemote())
				{
					ps.setInt(++i, stationId);
				}
				ps.setString(++i, endDate);
			}
			else
			{
//				ps = connection.prepareStatement("Select * from ser where stationId = ? order by ts desc  ");
				strQueryBuf.append(" order by ts desc  ");
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getSerResultsLimit());
				ps = connection.prepareStatement(strQueryBuf.toString());
//				if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				if (M9kUtils.isRemote())
				{
					ps.setInt(1, stationId);
				}
			}

			rs = ps.executeQuery();
			while (rs.next())
			{
				serDTO =  new M9kSerDTO();
//				if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				if (M9kUtils.isRemote())
				{
					serDTO.setStationId(rs.getInt("stationId"));
				}
				serDTO.setEventNum(rs.getInt("eventNum"));
				serDTO.setPhase(rs.getString("phase"));
				serDTO.setName(rs.getString("name"));
				serDTO.setTimeStamp(rs.getLong("ts"));
				serDTO.setNormal(rs.getInt("normal"));
				serDTO.setState(rs.getInt("state"));
				serDTO.setLocked(rs.getInt("locked"));
				lstSerDtos.add(serDTO);
			}
		} catch (SQLException e) {
			logger.error("SQL Exception in getLstOfSerDataForDates ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("SQL Exception in getLstOfSerDataForDates ", e);
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
				logger.warn("Exception while clean up ", e);
			}
		}

		logger.debug("Total ser data read..."+lstSerDtos.size());
		return lstSerDtos;
	}
	@Override
	public List<M9kSerDTO> getAllAvailableEvents(int stationId) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		M9kSerDTO m9kSerDTO;
		List<M9kSerDTO> lstAvailableEvents = new ArrayList<M9kSerDTO>();
		StringBuffer strQueryBuf;
//		if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
		if (M9kUtils.isRemote())
		{
			strQueryBuf = new StringBuffer("Select distinct eventNum, name from ser where stationId = ? order by eventNum asc ");	
		}
		else
		{
			strQueryBuf = new StringBuffer("Select distinct eventNum, name from ser order by eventNum asc ");
		}
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();;
//			ps = connection.prepareStatement("Select distinct eventNum, name from ser where stationId = ? order by ts desc ");
			ps = connection.prepareStatement(strQueryBuf.toString());
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				ps.setInt(1, stationId);
			}
			
			rs = ps.executeQuery();
			while (rs.next())
			{
				m9kSerDTO = new M9kSerDTO();
				m9kSerDTO.setEventNum(rs.getInt("eventNum"));
				m9kSerDTO.setName(rs.getString("name"));
				lstAvailableEvents.add(m9kSerDTO);
			}
		} catch (SQLException e) {
			logger.error("SQL Exception in getAllAvailableEvents ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("SQL Exception in getAllAvailableEvents ", e);
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
				logger.warn("Exception while clean up ", e);
			}
		}

		logger.debug("Total ser events read..."+lstAvailableEvents.size());
		return lstAvailableEvents;

	}
	@Override
	public List<M9kSerDTO> getLstOfSerDataForDates(int stationId, List<String> lstDates,
			String events) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		M9kSerDTO serDTO = null;
		List<M9kSerDTO> lstSerDtos = new ArrayList<M9kSerDTO>();
		logger.debug("Events to be queried.."+events);
		StringBuffer strQueryBuf;
		try {
		connection = M9kMySqlDatabase.getInstance().getConnection();
		if (lstDates != null && !lstDates.isEmpty())
		{
	//		if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
	//			strQueryBuf = new StringBuffer("Select * from ser where stationId = ? and (from_unixTime(ts div 1000000, \"%Y/%m/%d\") in ");	
				strQueryBuf = new StringBuffer("Select * from ser where stationId = ? and ");
			}
			else
			{
	//			strQueryBuf = new StringBuffer("Select * from ser where (from_unixTime(ts div 1000000, \"%Y/%m/%d\") in ");
				strQueryBuf = new StringBuffer("Select * from ser where ");
			}
				String dateToMatch = null;
				for (Iterator<String> iterator = lstDates.iterator(); iterator.hasNext();) {
	//				targetDates.append("'"+iterator.next()+"'");
					if (dateToMatch == null)
					{
						strQueryBuf.append(" (");
					}
					dateToMatch = iterator.next();
					strQueryBuf.append(" (ts between unix_timestamp('"+dateToMatch+" 00:00:00')*1000000 and unix_timestamp('"+dateToMatch+" 23:59:59')*1000000 )");
					if (iterator.hasNext())
					{
	//					targetDates.append(" or ");
						strQueryBuf.append(" or ");
					}
				}
				if (dateToMatch != null)
				{
					strQueryBuf.append(") ");
				}
	//			strQueryBuf.append(" ("+targetDates.toString()+")");
	//			String queryString = "Select * from ser where stationId = ? and (from_unixTime(ts div 1000000, \"%Y/%m/%d\") in ("+targetDates.toString()+")";
				
				if (events != null && !events.isEmpty())
				{
	//				queryString+=" and eventNum in ("+events+")) order by ts desc  ";
					strQueryBuf.append(" and eventNum in ("+events+") order by ts desc  ");
				}
				else
				{
	//				queryString+=") order by ts desc  ";
					strQueryBuf.append(" order by ts desc  ");
				}
			}
		else
		{
			if (M9kUtils.isRemote())
			{
				strQueryBuf = new StringBuffer("Select * from ser a inner join ("
						+ "select max(ts) as maxTs from ser) b on (a.ts between unix_timestamp(from_unixTime(b.maxTs div 1000000, \"%Y/%m/%d 00:00:00\"))*1000000 "
						+ "and unix_timestamp(from_unixTime(b.maxTs div 1000000, \"%Y/%m/%d 23:59:59\"))*1000000 ) and stationId = ? order by a.ts desc ");
			}
			else
			{
				strQueryBuf = new StringBuffer("Select * from ser a inner join ("
						+ "select max(ts) as maxTs from ser) b on (a.ts between unix_timestamp(from_unixTime(b.maxTs div 1000000, \"%Y/%m/%d 00:00:00\"))*1000000 "
						+ "and unix_timestamp(from_unixTime(b.maxTs div 1000000, \"%Y/%m/%d 23:59:59\"))*1000000 ) order by a.ts desc ");
			}
		}
			strQueryBuf.append(" LIMIT 0,"+M9kUtils.getSerResultsLimit());
			logger.debug("Query to be executed..."+strQueryBuf);
//			System.out.println("Query to be executed..."+strQueryBuf);
//			ps = connection.prepareStatement(queryString);
			ps = connection.prepareStatement(strQueryBuf.toString());
//			if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
			if (M9kUtils.isRemote())
			{
				ps.setInt(1, stationId);
			}
			rs = ps.executeQuery();
			while (rs.next())
			{
				serDTO =  new M9kSerDTO();
//				if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				if (M9kUtils.isRemote())
				{
					serDTO.setStationId(rs.getInt("stationId"));
				}
				serDTO.setEventNum(rs.getInt("eventNum"));
				serDTO.setPhase(rs.getString("phase"));
				serDTO.setName(rs.getString("name"));
				serDTO.setTimeStamp(rs.getLong("ts"));
				serDTO.setNormal(rs.getInt("normal"));
				serDTO.setState(rs.getInt("state"));
				serDTO.setLocked(rs.getInt("locked"));
				lstSerDtos.add(serDTO);
			}
		} catch (SQLException e) {
			logger.error("SQL Exception in getLstOfSerDataForDates ", e);
			throw new M9000Exception(e);
		}
		 catch (Exception e) {
				logger.error("SQL Exception in getLstOfSerDataForDates ", e);
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
				logger.warn("Exception while clean up ", e);
			}
		}

		logger.debug("Total ser data read..."+lstSerDtos.size());
		return lstSerDtos;
	}
	@Override
	public int getTotalSerCount(int stationId) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		int totalSerCount = 0;
		try
		{
			StringBuffer strbufQuery = new StringBuffer("Select count(*) from ser ");
			connection = M9kMySqlDatabase.getInstance().getConnection();
			if (M9kUtils.isRemote())
			{
				strbufQuery.append(" where stationId = ? ");
			}
		ps = connection.prepareStatement(strbufQuery.toString());
		if (M9kUtils.isRemote())
		{
			ps.setInt(1,stationId);
		}
//		System.out.println("About to get total faults from station  "+stationId );
		rs = ps.executeQuery();
		if (rs.next())
		{
			totalSerCount = rs.getInt(1);
		}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in fetching total counts of ser for station "+stationId+" Returning value as zero",e);
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

		return totalSerCount;

	}
	@Override
	public List<M9kSerDTO> searchByCriteria(int stationId, String searchCriteria, String orderCriteria, int from,
			int to) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		M9kSerDTO serDTO = null;
		List<M9kSerDTO> lstSerDtos = new ArrayList<M9kSerDTO>();
		StringBuffer strQueryBuf;
//		if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
		logger.debug("search criteria "+searchCriteria +" order criteria "+orderCriteria);
		if (M9kUtils.isRemote())
		{
			strQueryBuf = new StringBuffer("Select * from ser where stationId = ? ");	
		}
		else
		{
			strQueryBuf = new StringBuffer("Select * from ser ");
		}
		
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			int i = 1;

			if (searchCriteria != null && !searchCriteria.isEmpty())
			{
				if (M9kUtils.isRemote())
				{
					strQueryBuf.append(" and "+searchCriteria);
				}
				else
				{
					strQueryBuf.append(" where "+searchCriteria);
				}
			}
			// START: 13-May2020 - Select count of sers in recent date rather than entire table  
			else
			{
				if (M9kUtils.isRemote())
				{
					strQueryBuf.append(" and ser_date > (select date_sub(max(ser_date),interval "+M9kUtils.getSerDatesViewLimit()+" day ) from ser) ");
				}
				else
				{
					strQueryBuf.append(" where ser_date > (select date_sub(max(ser_date),interval "+M9kUtils.getSerDatesViewLimit()+" day ) from ser) ");
				}
			}
			// END: 13-May-2020
			if (orderCriteria != null && !orderCriteria.isEmpty())
			{
				strQueryBuf.append(" "+orderCriteria);
			}
			strQueryBuf.append(" limit ? , ? ");

//			strQueryBuf.append(" LIMIT 0,"+M9kUtils.getSerResultsLimit());
			logger.debug("Query to execute "+strQueryBuf.toString());
			
			ps = connection.prepareStatement(strQueryBuf.toString());
			
			if (M9kUtils.isRemote())
			{
				ps.setInt(i++, stationId);
			}
			ps.setInt(i++,from);
			ps.setInt(i++,to);

			rs = ps.executeQuery();
			while (rs.next())
			{
				serDTO =  new M9kSerDTO();
//				if (M9kUtils.getMasterType().equalsIgnoreCase(M9kStationConstants.MASTER_TYPE_REMOTE))
				if (M9kUtils.isRemote())
				{
					serDTO.setStationId(rs.getInt("stationId"));
				}
				serDTO.setEventNum(rs.getInt("eventNum"));
				serDTO.setPhase(rs.getString("phase"));
				serDTO.setName(rs.getString("name"));
				serDTO.setTimeStamp(rs.getLong("ts"));
				serDTO.setNormal(rs.getInt("normal"));
				serDTO.setState(rs.getInt("state"));
				serDTO.setLocked(rs.getInt("locked"));
				lstSerDtos.add(serDTO);
			}
		} catch (SQLException e) {
			logger.error("SQL Exception in getLstOfSerData ", e);
			throw new M9000Exception(e);
		}
		catch (Exception e) {
			logger.error("SQL Exception in getLstOfSerData ", e);
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
				logger.warn("Exception while clean up ", e);
			}
		}

		logger.debug("Total ser data read..."+lstSerDtos.size());
		return lstSerDtos;

	}
	@Override
	public int countByCriteria(int stationId, String searchCriteria) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		int totalSerCount = 0;
		try
		{
			StringBuffer strbufQuery = new StringBuffer("Select count(*) from ser ");
			connection = M9kMySqlDatabase.getInstance().getConnection();
			if (M9kUtils.isRemote())
			{
				strbufQuery.append(" where stationId = ? ");
			}
		if (searchCriteria != null && !searchCriteria.isEmpty())
		{
			if (!M9kUtils.isRemote())
			{
				strbufQuery.append(" where ");
			}
			else
			{
				strbufQuery.append(" and ");
			}
			strbufQuery.append(" "+searchCriteria);
		}
		// START: 13-May2020 - Select count of sers last 10 days from recent date rather than entire table  
		else
		{			
			if (M9kUtils.isRemote())
			{
				strbufQuery.append(" and ser_date > (select date_sub(max(ser_date),interval "+M9kUtils.getSerDatesViewLimit()+" day ) from ser) ");
			}
			else
			{
				strbufQuery.append(" where ser_date > (select date_sub(max(ser_date),interval "+M9kUtils.getSerDatesViewLimit()+" day ) from ser) ");
			}
		}
		// END: 13-May-2020
		logger.debug("SER countby criteria sql to be executedd "+strbufQuery.toString());
		ps = connection.prepareStatement(strbufQuery.toString());
		if (M9kUtils.isRemote())
		{
			ps.setInt(1,stationId);
		}

//		System.out.println("About to get total faults from station  "+stationId );
		rs = ps.executeQuery();
		if (rs.next())
		{
			totalSerCount = rs.getInt(1);
		}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in fetching total counts of ser for station "+stationId+" Returning value as zero",e);
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

		return totalSerCount;

	}

	@Override
	public List<M9kSerDTO> getAllNewSerEvents(List<Integer> lstNewSerIds) {
		PreparedStatement ps = null;
		ResultSet rs= null;
		M9kSerDTO m9kSerDTO;
		List<M9kSerDTO> lstAvailableEvents = new ArrayList<M9kSerDTO>();
		StringBuffer strQueryBuf;
//		strQueryBuf = new StringBuffer("select * from ser where updated > date_sub(now(), interval "+M9kUtils.getSERListenerRunFrequency()+" second) order by stationId,ts desc,eventNum");	
		if (M9kUtils.isRemote())
		{
			strQueryBuf = new StringBuffer("select a.*,b.name as stationName from ser a inner join station_details b where a.id IN ("); 
			strQueryBuf.append(constructQueryString(lstNewSerIds));
			strQueryBuf.append(") and a.stationId = b.stationId ORDER BY a.stationId,a.ts desc,a.eventNum");
		}
		else
		{
			strQueryBuf = new StringBuffer("select a.*,b.stationId, b.name as stationName from ser a, station_details b where a.id IN ("); 
			strQueryBuf.append(constructQueryString(lstNewSerIds));
			strQueryBuf.append(") ORDER BY a.ts desc,a.eventNum");
		}
		
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();;
//			ps = connection.prepareStatement("Select distinct eventNum, name from ser where stationId = ? order by ts desc ");
			ps = connection.prepareStatement(strQueryBuf.toString());
			int iIndex = 1;
			for (Iterator<Integer> iterator = lstNewSerIds.iterator(); iterator.hasNext();) {
				ps.setInt(iIndex++, iterator.next());
			}
			
			rs = ps.executeQuery();
			while (rs.next())
			{
				m9kSerDTO = new M9kSerDTO();
				m9kSerDTO.setId(rs.getInt("a.id"));
				if (M9kUtils.isRemote())
				{
					m9kSerDTO.setStationId(rs.getInt("a.stationId"));
				}
				else
				{
					m9kSerDTO.setStationId(rs.getInt("b.stationId"));

				}
				m9kSerDTO.setStationName(rs.getString("stationName"));
				m9kSerDTO.setEventNum(rs.getInt("a.eventNum"));
				m9kSerDTO.setPhase(rs.getString("a.phase"));
				m9kSerDTO.setName(rs.getString("a.name"));
				m9kSerDTO.setTimeStamp(rs.getLong("a.ts"));
				m9kSerDTO.setNormal(rs.getInt("a.normal"));
				m9kSerDTO.setState(rs.getInt("a.state"));
				m9kSerDTO.setLocked(rs.getInt("a.locked"));
				lstAvailableEvents.add(m9kSerDTO);
			}
		} catch (SQLException e) {
			logger.error("SQL Exception in getAllNewSerEvents ", e);
			lstAvailableEvents = null;
		}
		catch (Exception e) {
			logger.error("SQL Exception in getAllNewSerEvents ", e);
			lstAvailableEvents = null;
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
				logger.warn("Exception while clean up ", e);
			}
		}
		logger.debug("Total ser events read..."+(lstAvailableEvents != null?lstAvailableEvents.size():null));
		return lstAvailableEvents;

	}

	/**
	 * Called from remote master as a part of SER email functionality
	 * @return
	 */
	@Override
	public  List<Integer> getNewSERIds()
	{
		logger.debug("M9kSerListener: In MysqlSerDAO::getNewSERIds...");
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs= null;
		List<Integer> lstId = new ArrayList<Integer>();
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			StringBuffer strbufQuery = new StringBuffer("select id from ser_updates");
			ps = connection.prepareStatement(strbufQuery.toString());
			rs = ps.executeQuery();
			while (rs.next())
			{
				lstId.add(rs.getInt("id"));
			}
		} catch (SQLException sqle) {
			logger.error("SQl Exception occured in getIdsToProcess ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in getIdsToProcess ", e);
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
					ps= null;
				}
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}
		logger.debug("M9kSerListener: Returning MysqlSerDAO::getNewSERIds...lst of new Ids size "+lstId.size());
		return lstId;
	}
	
	@Override
	public int removeProcessedSerIds(List<Integer> lstId)
	{
		Connection connection = null;
		PreparedStatement ps = null;

		int result = 0;
		try {
			connection = M9kMySqlDatabase.getInstance().getConnection();
			StringBuffer strbufQuery = new StringBuffer("delete from ser_updates where id IN (");
			strbufQuery.append(constructQueryString(lstId));
			strbufQuery.append(")");
			ps = connection.prepareStatement(strbufQuery.toString());
			int iIndex = 1;
			for (Iterator<Integer> iterator = lstId.iterator(); iterator.hasNext();) {
				ps.setInt(iIndex++, iterator.next());
			}
			result = ps.executeUpdate();
			if (result == 0)
			{
				logger.error("Id(s) "+ lstId+" is not deleted yet");
			}
		} catch (SQLException sqle) {
			logger.error("SQl Exception occured in removeProcessedId ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in removeProcessedId ", e);
		}
		finally
		{
			try {
				if (ps != null)
				{
					ps.close();
					ps = null;
				}
			} catch (Exception e) {
				logger.warn("Problem in closing resultset and statment objects ",e);
			}
		}
		return result;
	}
	
	private String constructQueryString(List<Integer> lstId)
	{
		StringBuffer strbufQuery = new StringBuffer();
		for (int i = 0; i < lstId.size(); i++) {
			if (i == 0)
			{
				strbufQuery.append("?");
			}
			else
			{
				strbufQuery.append(",?");
			}
		}
		return strbufQuery.toString();
	}
}
