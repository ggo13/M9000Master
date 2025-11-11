package com.usi.m9000.test;

import java.sql.Connection;
import java.sql.Statement;

import com.usi.m9000.station.util.M9kStationDBUtil;

public class DBTestFederated {

	private Connection mysqlConn;
	private Statement stmt;
	public void testDbConnection()
	{
		try {
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			stmt = mysqlConn.createStatement();
			int totalRecordsInserted = stmt.executeUpdate("insert ignore into federated_contAnalog(stationId) values(111)");
			System.out.println("Total records inserted");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
