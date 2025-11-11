/**
 * 
 */
package com.usi.m9000.station.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.M9kCleanupDAO;
import com.usi.m9000.dao.MySqlComtradeContDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.util.M9kConstants;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author sramasamy
 *
 */
public class M9kStationDBUtil {
	private static ResourceBundle resourceBundle;
//	private static JDCConnectionDriver jdcConnectionDriver;
	private static StationDTO stationDetails;	
	static HikariDataSource connectionPool = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationDBUtil.class);
	public static boolean booDBInit =  initDB();
	private static String stationTitle = null;

public static StationDTO getStationDetails()
{
	if (stationDetails == null)
	{
		stationDetails = getLocalStationDetails();
	}
	
	return stationDetails;
}

public static void setStationDetails(StationDTO srcStationDetails)
{
	stationDetails = srcStationDetails;
}
public static StationDTO getLocalStationDetails()
{
	Connection mysqlConn = null;

	PreparedStatement ps = null;
	ResultSet rs= null;
	StationDTO stationDTO = null;
//	stationDTO.setPrefaultTime(0);
//	stationDTO.setPostfaultTime(0);
//	stationDTO.setLtrPrefaultTime(0);
//	stationDTO.setLtrPostfaultTime(0);
	try {
		mysqlConn = getLocalConnection();
		ps = mysqlConn.prepareStatement("select * from station_details");
		rs = ps.executeQuery();
		while (rs.next())
		{
			stationDTO = new StationDTO();
			stationDTO.setSystemStationId(rs.getInt("stationId"));
			stationDTO.setSystemStationName(rs.getString("name"));
//			stationDTO.setIpAddress(rs.getString("ipAddress"));
			stationDTO.setSystemRecordingDeviceId(rs.getString("recordingDevId"));
			stationDTO.setSystemPrefaultTime(rs.getInt("preFaultTime"));
			stationDTO.setSystemPostfaultTime(rs.getInt("postFaultTime"));
			stationDTO.setSystemLtrPrefaultTime(rs.getInt("ltrPreFaultTime"));
			stationDTO.setSystemLtrPostfaultTime(rs.getInt("ltrPostFaultTime"));
//			logger.debug("Sample Rate from database "+rs.getDouble("sampleRate"));
			stationDTO.setSystemSampleRate(rs.getInt("sampleRate"));
			stationDTO.setSystemLongTermSampleRate(rs.getInt("longTermSampleRate"));
			stationDTO.setSystemLineFrequency(rs.getInt("lineFreq"));
			stationDTO.setSystemAnalogChannelsCount(rs.getInt("analogs_count"));
			stationDTO.setSystemDigitalChannelsCount(rs.getInt("digitals_count"));
			stationDTO.setConfigXml(rs.getString("configXml"));
			stationDTO.setConfigStatus(rs.getString("status"));
			stationDTO.setCreated(rs.getString("created"));
			break;
		}
	}catch (SQLException e) {
		logger.error("Unable to get local station details from database",e);
		
	} catch (Exception e) {
		// TODO Auto-generated catch block
		logger.error("Unable to get local station details from database",e);
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
				logger.debug("Closing the connection... "+mysqlConn);
				mysqlConn.close();
				mysqlConn = null;
			}
		} catch (Exception e) {
			logger.warn("Error in cleanup", e);
		}
	}
	return stationDTO;
}

public static StationDTO getStationDetailsForId(int stationId)
{
	Connection mysqlConn = null;

	PreparedStatement ps = null;
	ResultSet rs= null;
	StationDTO stationDTO = null;
//	stationDTO.setPrefaultTime(0);
//	stationDTO.setPostfaultTime(0);
//	stationDTO.setLtrPrefaultTime(0);
//	stationDTO.setLtrPostfaultTime(0);
	try {
		mysqlConn = getLocalConnection();
		ps = mysqlConn.prepareStatement("select * from station_details where stationId = "+stationId);
		rs = ps.executeQuery();
		while (rs.next())
		{
			stationDTO = new StationDTO();
			stationDTO.setSystemStationId(rs.getInt("stationId"));
			stationDTO.setSystemStationName(rs.getString("name"));
//			stationDTO.setSystemIpAddress(rs.getString("ipAddress"));
			stationDTO.setSystemRecordingDeviceId(rs.getString("recordingDevId"));
			stationDTO.setSystemPrefaultTime(rs.getInt("preFaultTime"));
			stationDTO.setSystemPostfaultTime(rs.getInt("postFaultTime"));
			stationDTO.setSystemLtrPrefaultTime(rs.getInt("ltrPreFaultTime"));
			stationDTO.setSystemLtrPostfaultTime(rs.getInt("ltrPostFaultTime"));
//			logger.debug("Sample Rate from database "+rs.getDouble("sampleRate"));
			stationDTO.setSystemSampleRate(rs.getInt("sampleRate"));
			stationDTO.setSystemLongTermSampleRate(rs.getInt("longTermSampleRate"));
			stationDTO.setSystemLineFrequency(rs.getInt("lineFreq"));
			stationDTO.setConfigXml(rs.getString("configXml"));
			stationDTO.setSystemAnalogChannelsCount(rs.getInt("analogs_count"));
			stationDTO.setSystemDigitalChannelsCount(rs.getInt("digitals_count"));
			break;
		}
	}catch (SQLException e) {
		logger.error("Unable to get local station details from database",e);
		
	} catch (Exception e) {
		// TODO Auto-generated catch block
		logger.error("Unable to get local station details from database",e);
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
				logger.debug("Closing the connection... "+mysqlConn);
				mysqlConn.close();
				mysqlConn = null;
			}
		} catch (Exception e) {
			logger.warn("Error in cleanup", e);
		}
	}
	return stationDTO;
}

//public synchronized static JDCConnection getLocalConnectionOriginal() throws SQLException
//{
//	JDCConnection mysqlConn;
//	long start;
//	long end;
//	start = System.currentTimeMillis();
//
//	try
//	{
//		logger.info("Getting connecction");
//		mysqlConn = (JDCConnection) jdcConnectionDriver.getConnection();
//	}
//	catch(Exception e)
//	{
//		try {
//			logger.info("Getting DB connection failed once. Trying again.");
//			mysqlConn = (JDCConnection) jdcConnectionDriver.getConnection();
//		} catch (SQLException e1) {
//			logger.error(e1.getCause());
//			throw e1;
//		}
//	}
//	end = System.currentTimeMillis();
//	
//	logger.info("Total time to get connection each time... "+(end - start));
//	
//	return mysqlConn;
//}

public synchronized static Connection getLocalConnection() throws Exception
{
	Connection mysqlConn = null;
	long start;
	long end;
	start = System.currentTimeMillis();

	try
	{
//		logger.debug("Getting connecction booInit: "+booDBInit +" connection pool "+connectionPool);
		if (!booDBInit){
			booDBInit = initDB();
		}
		
		if (booDBInit && connectionPool != null)
		{
//			logger.debug("COMTRADE-DB-POOL: Before New connection Size of connection pool "+connectionPool.getTotalCreatedConnections());
			mysqlConn = connectionPool.getConnection();
//			logger.debug("COMTRADE-DB-POOL: After New connection Size of connection pool "+connectionPool.getTotalCreatedConnections());
		}
		else
		{
			throw new M9000Exception("Database is not initialized yet. Either MySQL is down or not reachable.");
		}
	}
	catch(Exception e)
	{
		logger.warn("Getting DB connection failed once. Trying again.");
		throw e;
	}
	end = System.currentTimeMillis();
	
//	logger.debug("Total time to get connection each time... "+(end - start));
	
	return mysqlConn;
}

public static void close() {
	connectionPool.close();
	logger.debug("Closing connection pool ");
	
}

private static boolean initDB()
{
	boolean booInit = false;
	  resourceBundle = ResourceBundle.getBundle("db");
//      try{
//    	  jdcConnectionDriver = new JDCConnectionDriver(
//    			  resourceBundle.getString("mysql_driver"), 
//    			  resourceBundle.getString("local_mysql_connection_url"),
//    			  resourceBundle.getString("mysql_user"), resourceBundle.getString("mysql_password"));
//         }catch(Exception e){
//	    	  try {
//	    		  if (jdcConnectionDriver == null)
//	    		  {
//	    			  jdcConnectionDriver = new JDCConnectionDriver(
//	    	    			  resourceBundle.getString("mysql_driver"), 
//	    	    			  resourceBundle.getString("local_mysql_connection_url"),
//	    	    			  resourceBundle.getString("mysql_user"), resourceBundle.getString("mysql_password"));
//	    		  }
//			} catch (ClassNotFoundException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (InstantiationException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (IllegalAccessException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (SQLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//          }
         
         try {
			Class.forName(resourceBundle.getString("mysql_driver"));
			HikariConfig  config = new HikariConfig ();	// create a new configuration object
//	      	logger.debug("Attempting mysql connection to "+resourceBundle.getString("local_mysql_connection_url"));
	      	logger.debug("Attempting mysql connection to "+resourceBundle.getString("local_mysql_connection_url"));
	      	config.setJdbcUrl(resourceBundle.getString("local_mysql_connection_url"));	// set the JDBC url
	     	config.setUsername(resourceBundle.getString("mysql_user"));			// set the username
	     	config.setPassword(resourceBundle.getString("mysql_password"));		
	     	config.addDataSourceProperty( "cachePrepStmts" , "true" );
	        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
	        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
	        config.addDataSourceProperty( "connectionTestQuery" , "select * from information_schema.tables limit 1" );
	        config.addDataSourceProperty( "connectionTimeout" , "10000" );
// set the password
//	     	config.setConnectionTestStatement("SELECT 1");
//	     	config.setQueryExecuteTimeLimitInMs(10000);
//	     	config.setMaxConnectionsPerPartition(50);
//	     	config.setMinConnectionsPerPartition(5);
//	     	config.setIdleMaxAgeInMinutes(2);
//	     	config.setIdleConnectionTestPeriodInMinutes(1);
//	     	config.setPoolAvailabilityThreshold(0);
//	     	config.setPartitionCount(2);
//	     	config.setLazyInit(true);
//	     	config.setAcquireIncrement(1);
//	     	config.setReleaseHelperThreads(2);
//	     	config.setCloseConnectionWatch(true);
	     	
	//     	config.setd;				// (other config options here)
	     	logger.debug("CONNECTION-POOL: Setting up new connection pool");
	     	connectionPool = new HikariDataSource (config); 	// setup the connection pool
	 		booInit = true;
 		}
         catch (Exception e) {
  			logger.error("Database connection issue ",e);
 		} 	// load the DB driver
         
//        catch (ClassNotFoundException e) {
// 			logger.error("Database connection issue ",e);
//		} 	// load the DB driver
// 		catch (SQLException e) {
// 			logger.error("Database connection issue ",e);
// 		}

		
		return booInit;

}

public static String getStationTitle()
{
	if (stationTitle == null)
	{
		stationTitle = getStationDetails().getSystemStationId()+" - "+getStationDetails().getSystemStationName();
	}
	return stationTitle;
}

public static String getStationFolderName() {
	String folderName = "R"+String.format("%02d", getStationDetails().getSystemStationId())+"-"+getStationDetails().getSystemStationName();
	return folderName;
}

public static Map<String,String>  getMeasurementTypes() throws M9000Exception
{
	MySqlComtradeContDAO mySqlComtradeContDAO = new MySqlComtradeContDAO();
	return mySqlComtradeContDAO.getAvailableMeasurementTypes(getStationDetails().getSystemStationId());
}
/**
 * Database cleanup for import config and remove stations
 */
public static void m9kStationDbCleanup() throws Exception
{
	M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
	M9kCleanupDAO mySqlM9kCleanupDAO =  m9kDAOFactory.getM9kCleanupDAO();
	mySqlM9kCleanupDAO.clearStationMasterTables();
}
}
