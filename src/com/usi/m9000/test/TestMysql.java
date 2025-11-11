package com.usi.m9000.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.usi.m9000.station.util.M9kStationDBUtil;

public class TestMysql {
	// Creates triggers and tracking table to push comtrade_details and clears triggers in dat_staging
	public void setUpSerDataPush() {
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
			stmt.executeUpdate("DROP TABLE IF EXISTS ser_updates");
			stmt.executeUpdate("CREATE TABLE ser_updates (   id 	INT(11)	NOT NULL,   updated 	DATETIME,   PRIMARY KEY (id) ) ENGINE=MyISAM");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS new_ser");
			stmt.executeUpdate("CREATE DEFINER='dfr'@'%' TRIGGER m9000.new_ser AFTER INSERT ON m9000.ser FOR EACH ROW BEGIN INSERT INTO ser_updates (id, updated) VALUES (NEW.id, NOW()); END");
			
		} catch (Exception e) {
			System.out.println("Exception occured in setting up comtrade_details table for data transfer "+ e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				System.out.println("Error in setUpFaultsPull cleanup"+e);
			}

		}
	}
	
	private void disableSerDataTransfer()
	{
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
			stmt.executeUpdate("DROP TABLE IF EXISTS ser_updates");
			stmt.executeUpdate("DROP TRIGGER IF EXISTS new_ser");
			
		} catch (Exception e) {
			System.out.println("Exception occured in setting up comtrade_details table for data transfer "+ e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				System.out.println("Error in setUpFaultsPull cleanup"+ e);
			}

		}

	}

	private void determineRemoteOrLocal() {
		Connection mysqlConn = null;
		Statement stmt = null;
		try {
//			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'm9000' AND TABLE_NAME = 'ser' AND COLUMN_NAME = 'stationId'");
			boolean remote = false;
			while (rs.next())
			{
				remote = true;
			}
			System.out.println("is remote? "+remote);
		} catch (Exception e) {
			System.out.println("Exception occured in setting up comtrade_details table for data transfer "+ e);
		}
		finally
		{
			try {
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
				if (mysqlConn != null)
				{
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				System.out.println("Error in setUpFaultsPull cleanup"+ e);
			}

		}
}
	public static void main(String[] args)
	{
		TestMysql test = new TestMysql();
		test.determineRemoteOrLocal();
	}
}
