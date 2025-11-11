package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.station.util.M9kStationDBUtil;

public class MySqlM9kCleanupDAO implements M9kCleanupDAO{
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlM9kCleanupDAO.class);
	static int dbConnCount = 0;
	public MySqlM9kCleanupDAO()
	{
		logger.debug("Entered MySqlM9kCleanupDAO constructor");
		
	}
	public void clearStationMasterTables() {
		PreparedStatement ps = null;
		Connection localConnection = null;
		ResultSet rs = null;
		Statement localStatment;
		try 
		{
			localConnection =  M9kStationDBUtil.getLocalConnection();
			localStatment=localConnection.createStatement();
			localStatment.executeUpdate("SET FOREIGN_KEY_CHECKS=0");
			localStatment.close();
			ps = localConnection.prepareStatement("SELECT Concat('TRUNCATE TABLE ',table_schema,'.',TABLE_NAME, ';') AS TRUNCATE_TABLE_LIST FROM INFORMATION_SCHEMA.TABLES where  table_schema in ('m9000') and table_name not like ('users') and table_name not like ('dfr_details') and table_name not like ('email%') and table_name not like ('%federated%') and table_name not like ('%remote%') and table_name not like ('station_details') and table_name not like ('%_view')");
			rs = ps.executeQuery();
			while (rs.next())
			{
				localStatment=localConnection.createStatement();
				localStatment.executeUpdate(rs.getString("TRUNCATE_TABLE_LIST"));
				localStatment.close();
			}
			rs.close();
			rs=null;
			ps.close();
			ps = null;
			localStatment=localConnection.createStatement();
			localStatment.executeUpdate("SET FOREIGN_KEY_CHECKS=1");
			localStatment.close();
			logger.info("Database data cleanup successful in station master");
		}
		catch (Exception e) {
			logger.error("Error in clearing all data",e);
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

	@Override
	public void clearRemoteMasterTables(int stationId) {
		PreparedStatement ps = null;
		Connection connection = null;
		ResultSet rs = null;
		Statement statment;
		try 
		{
		connection = M9kMySqlDatabase.getInstance().getConnection();
		ps = connection.prepareStatement("select CONCAT('DELETE FROM ',table_name,' where ', column_name,' = ','"+stationId+"') as DELETE_QUERY from INFORMATION_SCHEMA.columns where table_name IN ('alarms_log','comtrade_details','continuous_comtrade_data','dat_staging','error_comtrade','error_dat','long_term_dat','reports','reports_health','ser') and column_name like 'station%';");
		rs = ps.executeQuery();
		while (rs.next())
		{
			statment=connection.createStatement();
			statment.executeUpdate(rs.getString("DELETE_QUERY"));
			statment.close();
		}
		rs.close();
		rs=null;
		ps.close();
		ps = null;
		logger.info("Database data cleanup successful in web master for station "+stationId);
	}
	catch (Exception e) {
		logger.error("Error in clearing all data for station id "+stationId,e);
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
				logger.debug("COMTRADE-DB: closing connection from moveToErrorDat  "+ dbConnCount);
				connection.close();
				connection = null;
			}
		} catch (SQLException e) {
			logger.error("Error in clean up in Finally ",e);
			e.printStackTrace();
		}
	}

	}

}
