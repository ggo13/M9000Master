package com.usi.m9000.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.usi.m9000.util.M9kUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class TestDatabase {
	long epoch =1345646259999834L;
//	String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date (1280480467100333*1000));
	public static void main (String args[])
	{
		TestDatabase tet = new TestDatabase();
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection mysqlConn = null;
		String dataType = "Phasor,RMs";
		long ltrStartTime = 1522022400000000L;
		long ltrStopTime = 1522022422000000L;
		int i =0;
		int expId;
		String type;
		String sqlQuery = "SELECT recId, expId, name, description, phase, units, tsLast, sampleRate, sampleCnt, type, "
				+ "data FROM `m9000`.`cont` where (tsLast >= ? "
				+ "and tsLast <= ? and measurementType in(";
			StringBuffer strbQuery = new StringBuffer(sqlQuery);
			String[] arrMeasurementTypes = dataType.split(",");
			System.out.println("total measurements to be queried for "+arrMeasurementTypes.length);
			for (int j = 0; j < arrMeasurementTypes.length; j++) {
				if (j == 0)
				{
					strbQuery.append("?");
				}
				else
				{
					strbQuery.append(",?");
				}
			}
			strbQuery.append(")) order by measurementType desc, expId, tsLast");

//			String sqlQuery = "insert into long_term_dat SELECT expId, name, phase, units, tsLast, sampleRate, sampleCnt, scale, offset, if ((@start := (sampleCnt-(tsLast - ?))  * sampleRate) >= 0, @start, 0) as startSampleNo, " +
//			"(sampleCnt-(tsLast - ?))  * sampleRate)) as endSampleNo,data FROM `m9000`.`contAnalog` " +
//			"where (((? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast)  or (? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? <= tsLast ) ) " +
//			"or ((tsLast>= ? and ? >= cast(tsLast -(((sampleCnt-1)/sampleRate) * 1000000) as SIGNED) and ? >= tsLast )))";
			try {
				System.out.println("DB-POOL New connection from processMergeAndInsertLongTermData ");
//				Map<Integer,Float> mapMaxValue = getMaxValue(sqlQuery);

				mysqlConn = getLocalConnection();
				System.out.println("Cont-Data-Log: Start time "+ltrStartTime+" Stop time "+ltrStopTime );
//				System.out.println("Cont-Data-Log: sampleTime.longValue() "+sampleTtimeVal);
				System.out.println("Cont-Data-Log: SQL query to be executed "+ strbQuery);
				ps = mysqlConn.prepareStatement(strbQuery.toString());
				ps.setLong(++i, (ltrStartTime));
				System.out.println("Cont-Data-Log: param "+i+" - "+(ltrStartTime));
				ps.setLong(++i, (ltrStopTime));
				System.out.println("Cont-Data-Log: param "+i+" - "+(ltrStopTime));
				for (int j = 0; j < arrMeasurementTypes.length; j++) {
					ps.setString(++i, arrMeasurementTypes[j]);				
					System.out.println("Cont-Data-Log: param "+i+" - "+arrMeasurementTypes[j]);
				}
				
				rs = ps.executeQuery();
				while(rs.next())
				{
					expId = rs.getInt("expId");
					type = rs.getString("type");
					System.out.println("ExpId "+expId+" type "+type);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally
	    	{
				try {
					System.gc();
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
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}

	}
	
	private static Connection getLocalConnection() {
		HikariDataSource  connectionPool = null;
    	Connection connection = null;

        try {
			Class.forName("com.mysql.jdbc.Driver");
			HikariConfig  config = new HikariConfig ();	// create a new configuration object
//	      	logger.debug("Attempting mysql connection to "+resourceBundle.getString("local_mysql_connection_url"));
	      	config.setJdbcUrl("jdbc:mysql://195.1.1.89:3306/m9000?autoReconnect=true&amp;validationQuery=Select 1");	// set the JDBC url
	     	config.setUsername("dfr");			// set the username
	     	config.setPassword("usi");				// set the password
//	     	config.setConnectionTestStatement("SELECT 1");
//	     	config.setQueryExecuteTimeLimitInMs(10000);
//	     	config.setMaxConnectionsPerPartition(50);
//	     	config.setMinConnectionsPerPartition(5);
//	     	config.setIdleMaxAgeInMinutes(2);
//	     	config.setIdleConnectionTestPeriodInMinutes(1);
////	     	config.setPoolAvailabilityThreshold(0);
//	     	config.setPartitionCount(2);
////	     	config.setLazyInit(true);
//	     	config.setAcquireIncrement(1);
//	     	config.setReleaseHelperThreads(2);
//	     	config.setCloseConnectionWatch(true);
	     	
	//     	config.setd;				// (other config options here)
	     	connectionPool = new HikariDataSource (config); 	// setup the connection pool
	     	 connection = connectionPool.getConnection();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 	// load the DB driver
		catch (SQLException e) {
			e.printStackTrace();
		}

		return connection;
	}

	private String getRequiredDataFormat(String requiredFormat, Date dateToFormat)
	{
		String convertedDateFormat = null;
		SimpleDateFormat requiredDateFormat = new SimpleDateFormat(requiredFormat);
		requiredDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		convertedDateFormat = requiredDateFormat.format(dateToFormat);
		return convertedDateFormat;
	}
	
	public String getDisplayTime() {
	     String microseconds = (""+getTimeStamp());
	     microseconds = microseconds.substring(microseconds.length()-3);
	  // Start: 31-Jan-2013 All date conversion are in M9kUtils class
//	     java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SSS"); 
//		  sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	     java.text.SimpleDateFormat sdf  = M9kUtils.getDateFormat("MM/dd/yyyy - HH:mm:ss.SSS");
		// End: 31-Jan-2013
//	     displayTime = new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SSS").format(new java.util.Date (getTimeStamp()/1000));
		  String displayTime = sdf.format(new java.util.Date (getTimeStamp()/1000));
	     displayTime+=microseconds;
		return displayTime;
	}

	/**
	 * @return
	 */
	private long getTimeStamp() {
		// TODO Auto-generated method stub
		// 1434426287633605L
		// 1434426287633605L
		return 1434426287633605L;
	}

}
