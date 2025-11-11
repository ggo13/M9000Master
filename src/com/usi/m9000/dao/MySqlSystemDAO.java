package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;

public class MySqlSystemDAO implements SystemDAO {

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlSystemDAO.class);
	public MySqlSystemDAO()
	{

	}
	
	@Override
	public void createMySQLEvents(String eventType) {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		String sqlQuery;
		try
		{
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			if (eventType != null && !eventType.isEmpty())
			{
				int iCnt = 1;
				if (eventType.equalsIgnoreCase(M9kStationConstants.MYSQL_EVENT_NAME_DROP_CONT_MEASUREMENTS))
				{
					dropMySQLEvents("drop_cont_partitions");
					sqlQuery = "CREATE EVENT drop_cont_partitions " + 
							"ON SCHEDULE EVERY 1 day " + 
							//"STARTS str_to_date( date_format(now(), '%Y%m%d "+M9kStationUtil.getTimeToRunDropContOscEvent()+"'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY " +
							"STARTS str_to_date( date_format(now(), '%Y%m%d "+M9kStationUtil.getTimeToRunDropContOscEvent()+"'), '%Y%m%d %H%i' ) " + 
							"DO  call DropPartitions('m9000', ?, DATE(now()), ?)";
					ps = mysqlConn.prepareStatement(sqlQuery);
					ps.setString(iCnt++, "cont");
					ps.setInt(iCnt++, M9kStationXMLUtil.getContMeasurementsDaysToRetain());
					ps.executeUpdate();
				}
				else if (eventType.equalsIgnoreCase(M9kStationConstants.MYSQL_EVENT_NAME_DROP_CONT_OSC))
				{
					dropMySQLEvents("drop_contAnalog_partitions");
					sqlQuery = "CREATE EVENT drop_contAnalog_partitions " + 
							"ON SCHEDULE EVERY 1 day " + 
//							"STARTS str_to_date( date_format(now(), '%Y%m%d "+M9kStationUtil.getTimeToRunDropContMeasurementEvent()+"'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY " + 
							"STARTS str_to_date( date_format(now(), '%Y%m%d "+M9kStationUtil.getTimeToRunDropContMeasurementEvent()+"'), '%Y%m%d %H%i' ) " +
							"DO  call DropPartitions('m9000', ?, DATE(now()), ?)";
					ps = mysqlConn.prepareStatement(sqlQuery);
					ps.setString(iCnt++, "contAnalog");
					ps.setInt(iCnt++, M9kStationXMLUtil.getContOscDaysToRetain());
					ps.executeUpdate();
				}
				else if (eventType.equalsIgnoreCase(M9kStationConstants.MYSQL_EVENT_NAME_CREATE_CONT_MEASUREMENTS))
				{
					dropMySQLEvents("create_cont_partitions");
					sqlQuery = "CREATE EVENT create_cont_partitions " + 
							"ON SCHEDULE EVERY 8 hour " + 
							"STARTS str_to_date( date_format(now(), '%Y%m%d "+M9kStationUtil.getTimeToRunCreateContOscEvent()+"'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY " + 
							"DO  call CreatePartitions('m9000', ?, DATE(now()), ?)";
					ps = mysqlConn.prepareStatement(sqlQuery);
					ps.setString(iCnt++, "cont");
					ps.setInt(iCnt++, 5);
					ps.executeUpdate();
				}
				else if (eventType.equalsIgnoreCase(M9kStationConstants.MYSQL_EVENT_NAME_CREATE_CONT_OSC))
				{
					dropMySQLEvents("create_contAnalog_partitions");
					sqlQuery = "CREATE EVENT create_contAnalog_partitions " + 
							"ON SCHEDULE EVERY 8 hour " + 
							"STARTS str_to_date( date_format(now(), '%Y%m%d "+M9kStationUtil.getTimeToRunDropContMeasurementEvent()+"'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY " + 
							"DO  call CreatePartitions('m9000', ?, DATE(now()), ?)";
					ps = mysqlConn.prepareStatement(sqlQuery);
					ps.setString(iCnt++, "contAnalog");
					ps.setInt(iCnt++, 5);
					ps.executeUpdate();
				}
				else if (eventType.equalsIgnoreCase(M9kStationConstants.MYSQL_EVENT_NAME_REMOVE_OLD_SER))
				{
					dropMySQLEvents("remove_old_ser");
					sqlQuery = "CREATE EVENT remove_old_ser " + 
							"ON SCHEDULE EVERY 24 hour " + 
							"STARTS str_to_date( date_format(now(), '%Y%m%d "+M9kStationUtil.getTimeToRunRemoveOldSerEvent()+"'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY " + 
							"DO  call m9k_remove_old_data('m9000',?,?);";
					ps = mysqlConn.prepareStatement(sqlQuery);
					ps.setString(iCnt++, "ser");
					ps.setInt(iCnt++, M9kStationUtil.getNoOfYearsToRetainSer());
					ps.executeUpdate();
				}
				else if (eventType.equalsIgnoreCase(M9kStationConstants.MYSQL_EVENT_NAME_REMOVE_OLD_DFRLOG))
				{
					dropMySQLEvents("remove_old_dfrlog");
					sqlQuery = "CREATE EVENT remove_old_dfrlog " + 
							"ON SCHEDULE EVERY 24 hour " + 
							"STARTS str_to_date( date_format(now(), '%Y%m%d "+M9kStationUtil.getTimeToRunRemoveOldDfrlogEvent()+"'), '%Y%m%d %H%i' ) + INTERVAL 1 DAY " + 
							"DO  call m9k_remove_old_data('m9000',?,?);";
					ps = mysqlConn.prepareStatement(sqlQuery);
					ps.setString(iCnt++, "dfrlog");
					ps.setInt(iCnt++, M9kStationUtil.getNoOfYearsToRetainDfrlog());
					ps.executeUpdate();
				}
				
			}
		}
		catch (SQLException e) {
				logger.error("Error in creating mysql events ",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading creating mysql events ",e);
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
						logger.debug("DB-POOL Close connection from create events ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					logger.error("Error in finally section ",e);
				}

	}
		
	}

	@Override
	public void dropMySQLEvents(String dbEventName) throws M9000Exception {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		try
		{
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			ps = mysqlConn.prepareStatement("DROP EVENT IF EXISTS "+ dbEventName);
			ps.executeUpdate();
		}
		catch (SQLException e) {
			logger.error("Error in dropping mysql event "+dbEventName,e);
			throw new M9000Exception("Error in dropping mysql event "+dbEventName,e);
		} 
		catch (Exception e) {
			logger.error("Error in dropping mysql event "+dbEventName,e);
			throw new M9000Exception("Error in dropping mysql event "+dbEventName,e);
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
					logger.debug("DB-POOL Close connection from dropMySQLEvents ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				logger.error("Error in finally section ",e);
			}
		}
	}

	@Override
	public boolean isTableExists(String dbName, String tableName) throws M9000Exception {
		Connection mysqlConn = null;
		PreparedStatement ps = null;
		boolean tableExists = false;
			try {
				logger.debug("Inside isTableExists");
				try
				{
					mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
				}
				catch(Exception ce)
				{
					ce.printStackTrace();
	//				mysqlConn = M9kMySqlDatabase.getInstance().getAppletDBConnection();
				}
				ps = mysqlConn.prepareStatement("SELECT * FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?");
				ps.setString(1, dbName);
				ps.setString(2, tableName);
				ResultSet rs = ps.executeQuery();
				tableExists = false;
				while (rs.next())
				{
					tableExists = true;
					break;
				}
	
			} catch (Exception e) {
				logger.error("Exception occured in determining remote or local table ", e);
				tableExists = false;
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
					logger.error("Error in checking for tables existance "+ e);
				}
	
			}
		return tableExists;
	}

}
