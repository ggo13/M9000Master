package com.usi.m9000.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.usi.m9000.Exception.M9000Exception;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class M9kMySqlDatabase {

	private static M9kMySqlDatabase m9kMySqlDatabase;
	private Connection mysqlConnection = null;
	private DataSource ds = null;
	private Context initContext = null;
	static ResourceBundle resourceBundle;
//	static JDCConnectionDriver jdcAppletConnectionDriver;
//	static JDCConnectionDriver jdcLocalhostConnectionDriver;
//	private JDCConnection appletJdcConnection = null;
//	private JDCConnection localJdcConnection = null;
//	static BoneCP localConnectionPool = null;
	static HikariDataSource  remoteConnectionPool = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kMySqlDatabase.class);
	public static boolean booDBInit =  initDB();
	
//	static {
//	      try{
//	    	  resourceBundle = ResourceBundle.getBundle("db");
//	    	  jdcAppletConnectionDriver = new JDCConnectionDriver(
//	    			  resourceBundle.getString("mysql_driver"), 
//	    			  resourceBundle.getString("mysql_web_connection_url"),
//	    			  resourceBundle.getString("mysql_user"), resourceBundle.getString("mysql_password"));
//	          }catch(Exception e){
//	        	  e.printStackTrace();
//		    	  try {
//		    		  if (jdcAppletConnectionDriver == null)
//		    		  {
//		    			  jdcAppletConnectionDriver = new JDCConnectionDriver(
//		    	    			  resourceBundle.getString("mysql_driver"), 
//		    	    			  resourceBundle.getString("mysql_web_connection_url"),
//		    	    			  resourceBundle.getString("mysql_user"), resourceBundle.getString("mysql_password"));
//		    		  }
//				} catch (ClassNotFoundException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (InstantiationException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (IllegalAccessException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (SQLException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//	        	  
//	          }
	          
//	          // DB connection to Localhost
//		      try{
//		    	  jdcLocalhostConnectionDriver = new JDCConnectionDriver(
//		    			  resourceBundle.getString("mysql_driver"), 
//		    			  resourceBundle.getString("local_mysql_connection_url"),
//		    			  resourceBundle.getString("mysql_user"), resourceBundle.getString("mysql_password"));
//		          }catch(Exception e){
//		        	  e.printStackTrace();
//			    	  try {
//			    		  if (jdcLocalhostConnectionDriver == null)
//			    		  {
//			    			  jdcLocalhostConnectionDriver = new JDCConnectionDriver(
//			    	    			  resourceBundle.getString("mysql_driver"), 
//			    	    			  resourceBundle.getString("local_mysql_connection_url"),
//			    	    			  resourceBundle.getString("mysql_user"), resourceBundle.getString("mysql_password"));
//			    		  }
//					} catch (ClassNotFoundException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					} catch (InstantiationException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					} catch (IllegalAccessException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					} catch (SQLException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//		        	  
//		          }


//		          try {
//			        	 logger.debug("About to get localconnection pool " + resourceBundle.getString("local_mysql_connection_url"));
//			        	 System.out.println("About to get localconnection pool "+resourceBundle.getString("local_mysql_connection_url"));
//							
//					      	BoneCPConfig config = new BoneCPConfig();	// create a new configuration object
//					      	logger.debug("BoneCPCOnfogi constructor invoked");
//					      	config.setJdbcUrl(resourceBundle.getString("local_mysql_connection_url"));	// set the JDBC url
//					     	config.setUsername(resourceBundle.getString("mysql_user"));			// set the username
//					     	config.setPassword(resourceBundle.getString("mysql_password"));				// set the password
//					     	config.setConnectionTestStatement("SELECT 1");
//					     	config.setQueryExecuteTimeLimitInMs(10000);
//					     	config.setMaxConnectionsPerPartition(20);
//					     	config.setPartitionCount(4);
//					     	config.setLazyInit(true);
//					     	config.setAcquireIncrement(5);
//					     	config.setCloseConnectionWatch(true);
//					     	logger.debug("BoneCPCOnfogi constructed");
//					//     	config.setd;				// (other config options here)
//					     	
//					     	localConnectionPool = new BoneCP(config); 	// setup the connection pool
//					     	logger.debug("localConnectionPool constructed");
//				 		} catch (SQLException e) {
//				 			logger.error("Database connection issue ",e);
//				 		}
//				 		catch (Exception e) {
//				 			logger.error("Database connection issue ",e);
//				 			System.out.println("Error in database connection "+e.getMessage());
//						}
				 		
				 		// Applet/remote connection pool

		          
//	    }

	private M9kMySqlDatabase(){
		try {
			initContext = new InitialContext();
			logger.debug("About to lookup");
			ds = (DataSource)initContext.lookup("java:comp/env/jdbc/mysql");
			
			
		} catch( NamingException ne ) {
			ne.printStackTrace();
			logger.warn("Assuming Stand alone app.",ne);
		} 
	}

	private static boolean initDB() {
		boolean booInit = false;
		try {
			resourceBundle = ResourceBundle.getBundle("db");
			Class.forName(resourceBundle.getString("mysql_driver"));
//			logger.debug("About to get remoteconnection pool " + M9kUtils.getAppletDBConnectionString());
//      	 System.out.println("About to get remoteconnection pool "+M9kUtils.getAppletDBConnectionString());
//			Class.forName(resourceBundle.getString("mysql_driver"));
			logger.debug("After class.forName " + resourceBundle.getString("mysql_driver"));
			HikariConfig  config = new HikariConfig ();	// create a new configuration object
//	      	config.setJdbcUrl(M9kUtils.getAppletDBConnectionString());	// set the JDBC url
	      	config.setJdbcUrl(resourceBundle.getString("local_mysql_connection_url"));	// set the JDBC url
	     	config.setUsername(resourceBundle.getString("mysql_user"));			// set the username
	     	config.setPassword(resourceBundle.getString("mysql_password"));				// set the password
	     	config.addDataSourceProperty( "cachePrepStmts" , "true" );
	        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
	        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
	        config.addDataSourceProperty( "connectionTestQuery" , "select * from information_schema.tables limit 1" );
	        config.addDataSourceProperty( "connectionTimeout" , "10000" );
//	     	config.setConnectionTestStatement("SELECT 1");
//	     	config.setQueryExecuteTimeLimitInMs(10000);
//	     	config.setMaxConnectionsPerPartition(10);
//	     	config.setMinConnectionsPerPartition(2);
//	     	config.setIdleMaxAgeInMinutes(2);
//	     	config.setIdleConnectionTestPeriodInMinutes(1);
//	     	config.setPoolAvailabilityThreshold(0);
//	     	config.setPartitionCount(1);
//	     	config.setLazyInit(true);
//	     	config.setAcquireIncrement(1);
//	     	config.setReleaseHelperThreads(0);
//	     	config.setCloseConnectionWatch(true);
	     	
	//     	config.setd;				// (other config options here)
	     	logger.debug("BoneCPCOnfogi constructed");
	     	remoteConnectionPool = new HikariDataSource(config); 	// setup the connection pool
	     	booInit = true;
	     	logger.debug("BoneCPCOnfogi constructed");
		} 
//		catch (SQLException e) {
//			logger.error("Database connection issue ",e);
//		}
//		catch (ClassNotFoundException e1) {
//			logger.error("Error in look up ", e1);
//			System.out.println("Error "+e1.getMessage());
//		}
		catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.error("Error in database configuration ", e1);
			System.out.println("Error "+e1.getMessage());
		}
		return booInit;

	}

	public static M9kMySqlDatabase getInstance()
	{
//		System.out.println("Getting instance of m9kMySqlDatabase "+m9kMySqlDatabase);
//		logger.debug("Getting instance of m9kMySqlDatabase "+m9kMySqlDatabase);
		if (m9kMySqlDatabase == null)
		{
			m9kMySqlDatabase = new M9kMySqlDatabase();
		}
//		logger.debug("Returning from getInstance "+m9kMySqlDatabase);
//		System.out.println("Returning from getInstance "+m9kMySqlDatabase);
		return m9kMySqlDatabase;
	}
	
	public Connection getConnection() throws M9000Exception
	{
//		logger.debug("Is DataSource empty? "+(ds == null));
//		System.out.println("Is DataSource empty? "+(ds == null));
    	if (ds != null)
		{
			try {
//	    		long startTime;
//	    		long endTime;
//	    		startTime = System.currentTimeMillis();
				mysqlConnection = ds.getConnection();
//				endTime = System.currentTimeMillis();
//				System.out.println("Total Time for DS getConnection() ..."+(endTime-startTime));
			} catch (SQLException e) {
				logger.error("Error in getting connection ", e);
				throw new M9000Exception(e);
			}
			catch (Exception e) {
				logger.error("Error in getting connection ", e);
				throw new M9000Exception(e);
			}
		}
		else
		{
			try
			{
				mysqlConnection = getAppletDBConnection();
			}
			catch (Exception e) {
				logger.error("Error in getting connection ", e);
				throw new M9000Exception(e);
			}
		}


		return mysqlConnection;
	}

//    public JDCConnection getAppletDBConnectionOld()
//    {
//    	long start;
//		long end;
//		start = System.currentTimeMillis();
//
//		try
//		{
//			logger.info("Getting connecction to "+resourceBundle.getString("mysql_web_connection_url"));
//			appletJdcConnection = (JDCConnection) jdcAppletConnectionDriver.getConnection();
//		}
//		catch(Exception e)
//		{
//			try {
//				logger.error("Unable to get Connection. Attempting again.");
//				appletJdcConnection = (JDCConnection) jdcAppletConnectionDriver.getConnection();
//			} catch (SQLException e1) {
//				logger.error("Failed to get database connection"+e1.getCause());
//			}
//		}
//		end = System.currentTimeMillis();
//		
//		logger.info("Total time to get connection each time... "+(end - start)); 	
//	      return appletJdcConnection;
//    }

//    public JDCConnection getLocalDBConnectionOld()
//    {
////    	long start;
////		long end;
////		start = System.currentTimeMillis();
//
//		try
//		{
////			logger.info("Getting connecction to "+resourceBundle.getString("local_mysql_connection_url"));
//			localJdcConnection = (JDCConnection) jdcLocalhostConnectionDriver.getConnection();
//		}
//		catch(Exception e)
//		{
//			try {
//				logger.error("Unable to get Connection. Attempting again.");
//				localJdcConnection = (JDCConnection) jdcLocalhostConnectionDriver.getConnection();
//			} catch (SQLException e1) {
//				logger.error("Failed to get database connection"+e1.getCause());
//			}
//		}
////		end = System.currentTimeMillis();
//		
////		logger.info("Total time to get connection each time... "+(end - start)); 	
//	      return localJdcConnection;
//    }

	public void closeConnection()
	{
		System.out.println("In Close connection method ");
		if (mysqlConnection != null)
		{
			try {
				mysqlConnection.close();
				System.out.println("Connection Closed");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (remoteConnectionPool != null)
		{
			logger.debug("Closing remoteConnectionPool ");
			remoteConnectionPool.close();
			remoteConnectionPool = null;
			booDBInit = false;
		}
	}

//    public synchronized Connection getLocalDBConnection() throws Exception
//    {
//    	Connection mysqlConn = null;
//    	long start;
//    	long end;
//    	start = System.currentTimeMillis();
//
//    	try
//    	{
//    		
//   			mysqlConn = localConnectionPool.getConnection();
//    	}
//    	catch(Exception e)
//    	{
//    		logger.info("Getting DB connection failed once. Trying again.");
//    		throw e;
//    	}
//    	end = System.currentTimeMillis();
//    	
//    	logger.info("Total time to get connection each time... "+(end - start));
//    	
//    	return mysqlConn;
//    }
    
    public synchronized Connection getAppletDBConnection() throws Exception
    {
    	Connection mysqlConn = null;
//    	long start;
//    	long end;
//    	start = System.currentTimeMillis();

    	try
    	{
//    		logger.debug("DATABASE-CONNECTION: About to get remote connection");
    		if (!booDBInit || remoteConnectionPool == null){
    			booDBInit = initDB();
    		}
    		if (booDBInit && remoteConnectionPool != null)
    		{
    			mysqlConn = remoteConnectionPool.getConnection();
    		}
    		else
    		{
    			throw new M9000Exception("Error in database connection");
    		}
    	}
    	catch(Exception e)
    	{
    		logger.error("Getting DB connection failed once. Trying again.");
    		throw e;
    	}
//    	end = System.currentTimeMillis();
    	
//    	logger.info("Total time to get connection each time... "+(end - start));
    	
    	return mysqlConn;
    }

}
