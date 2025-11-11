package com.usi.m9000.station.threads;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.usi.m9000.common.M9kCreateComtradeFilesFromDB;
import com.usi.m9000.dao.MySqlDatDAO;
import com.usi.m9000.station.Exception.M9kThreadExceptionHandler;
import com.usi.m9000.station.commands.M9kStationCreateComtradeFilesFromDB;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.station.util.M9kStationDBUtil;


public class M9kComtradeDataPushTask {
	private int runFrequency = 1; // in seconds
	static boolean booStatus = true;
	private Connection mysqlConn;
	private Statement stmt;
	private ResultSet rs;
	 private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	 ScheduledFuture<?> scheduledTask = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kComtradeDataPushTask.class);
	ExecutorService threadHandlerExecutor;
	private M9kCreateComtradeFilesFromDB m9kCreateComtradeFilesFromDB;
	private static MySqlDatDAO mySqlDataDAO = null;
	
	public M9kComtradeDataPushTask()
	{
		logger.debug("Entered M9kComtradeDataPushThread constructor");
//  	  resourceBundle = ResourceBundle.getBundle("db");
//      try{
////    	  System.out.println("resourceBundle values "+resourceBundle.getString("local_mysql_connection_url")+" "+resourceBundle.getString("mysql_conn_attr"));
//    	  logger.debug("Resource bundle read "+resourceBundle.getString("local_mysql_connection_url"));
////    	  System.out.println("Before static init..."+jdcAppletConnectionDriver+" conn str "+M9kConstants.STATION_MYSQL_CONNECTION_URL);
//    	  jdcConnectionDriver = new JDCConnectionDriver(
//    			  resourceBundle.getString("mysql_driver"), 
//    			  resourceBundle.getString("local_mysql_connection_url"),
//    			  resourceBundle.getString("mysql_user"), resourceBundle.getString("mysql_password"));
//         }catch(Exception e){
//        	 logger.warn("JDBC connection exception. Attempting again");
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
		String runFreqproperty = M9kStationComtradeUtil.getComtradeProperty("run-frequency");
		if (runFreqproperty == null || runFreqproperty.isEmpty())
		{
			runFrequency = 5;
		}
		else
		{
			runFrequency = Integer.parseInt(runFreqproperty);
		}
		 
		if (mySqlDataDAO == null)
		{
			mySqlDataDAO =new MySqlDatDAO();
		}
		
		threadHandlerExecutor = Executors.newSingleThreadExecutor();
		m9kCreateComtradeFilesFromDB = new M9kStationCreateComtradeFilesFromDB();
	}
	
	public void schedulePush()
	{
		  final Runnable taskPerformer = new Runnable() {
              public void run() {
//            	  if (!isMasterStationReachable())
//					{
//						logger.error("No connections established to Master yet...");
//					}
//					else
//					{
						updateDatStagingWithRemote();
//					}
              }
          };
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, getRunFrequency(), TimeUnit.SECONDS);
          threadHandlerExecutor.execute(new M9kThreadExceptionHandler(scheduledTask));
	}
	
	private int updateDatStagingWithRemote()
	{
		int totalRecordsInserted = 0;
		int faultId = 0;
		int deleteStatus;
		long start;
		long end;
		
		
		try {
//			System.out.println("Connection Request from updateDatStagingWithRemote method....Is Local? "+isLocalDBConnection());
//			getLocalConnection();
//			mysqlConn = M9kMySqlDatabase.getInstance().getLocalDBConnection();
			logger.debug("DB-POOL New connection from updateDatStagingWithRemote ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Connection obj "+mysqlConn);
			start = System.currentTimeMillis();
			faultId = getFaultIdToProcess();
			end = System.currentTimeMillis();
			logger.debug("Time to execute getFaultIdToProcess "+(end-start));
			logger.debug("Fault id retrieved from db "+faultId);
//			stmt.setFetchSize(Integer.MIN_VALUE);
			if (faultId > 0)
			{

				// START: 11-Feb-2020 - Create the COMTRADE file locally takes priority before pushing to the remote.
				try
				{
					m9kCreateComtradeFilesFromDB.createComtradeFileForFaultId(faultId);
				}
				catch (Exception e)
				{
					logger.error("Error while creating COMTRADE files locally for Fault id "+faultId,e);
				}
				// END: 11-Feb-2020

				stmt = mysqlConn.createStatement();

				// Use of on dupicate key update doesn't work with federated tables
//				totalRecordsInserted = stmt.executeUpdate("insert into federated_dat_staging(stationId, faultId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, data, dataBlob) " +
//						"SELECT a.stationId, b.faultId, b.type, b.tsPrefault, b.tsTrigger, b.lineFreq, b.sampleRate, b.sampleCnt, b.analogs, b.events, b.data,b.dataBlob FROM station_details a, dat_staging b where b.faultId ="+ faultId
//						+" on duplicate key update stationId=values(stationId), faultId=values(faultId), type=values(type), tsPrefault=values(tsPrefault), tsTrigger=values(tsTrigger), lineFreq=values(lineFreq), sampleRate=values(sampleRate), sampleCnt=values(sampleCnt), analogs=values(analogs), events=values(events), data=values(data), dataBlob=values(dataBlob)");

				logger.info("About to push fault "+faultId+" to the remote master");
				totalRecordsInserted = stmt.executeUpdate("insert ignore into federated_dat_staging(stationId, faultId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, data, dataBlob, faultLocation, faultLogic, lineGroups, comments) " +
						"SELECT a.stationId, b.faultId, b.type, b.tsPrefault, b.tsTrigger, b.lineFreq, b.sampleRate, b.sampleCnt, b.analogs, b.events, b.data,b.dataBlob,b.faultLocation, b.faultLogic, b.lineGroups, b.comments FROM station_details a, dat_staging b where b.faultId ="+ faultId);
				
				
				SQLWarning sqlWarning = stmt.getWarnings();
				if (sqlWarning != null)
				{
					do
					{
						logger.info("Unable to push the fault "+faultId+" due to errors. It will keep trying to push in future");
						logger.error("Error in inserting fault record"+faultId+" to the remote dat_staging table. Error Code"+sqlWarning.getErrorCode()+" Error message "+sqlWarning.getMessage());
						sqlWarning = sqlWarning.getNextWarning();
					}while (sqlWarning != null);
					stmt.clearWarnings();
					// This helps processing the next fault when the remote master is unavailable
					moveFaultIdToError(faultId);
				}
				else
				{
					logger.info("Successfully pushed fault "+faultId+" to the remote master");
					// START: 11-Feb-2020 - Moved the code further up as Creating COMTRADE file locally takes priority before pushing to the remote. 
//					try
//					{
//						m9kCreateComtradeFilesFromDB.createComtradeFileForFaultId(faultId);
//					}
//					catch (Exception e)
//					{
//						logger.error("Fault id "+faultId+" is successfully pushed to the master but error while creating COMTRADE files locally.",e);
//					}

					// Moved the logic from create comtrade to help pushing error 
					removeFaultFromDatStaging(faultId);

					// END: 11-Feb-2020
					
					deleteStatus = removeProcessedFaultId(faultId);
					logger.debug("Delete status after removing faultid "+faultId+ " is "+deleteStatus);
					if (deleteStatus == 0)
					{
						logger.error("Error in deleting the fault id "+faultId+ " from the table. ");
	//					scheduledTask.cancel(true);
					}
					// START: 11-Feb-2020 - Moved the code further up as Creating COMTRADE file locally takes priority before pushing to the remote. 
					else
					{
						// If the push is successful then try processing the old fault ids that had errors earlier
						int reprocessStatus = reprocessFromErrorDatUpdates();
						if (reprocessStatus > 0)
						{
							clearErrorDatUpdates();
						}
						
					}
					// END: 11-Feb-2020
				}
			}
			// START: 11-Feb-2020 - Moved the code further up as Creating COMTRADE file locally takes priority before pushing to the remote. 
			else
			{
				// If the there are any left over old faults try to push it here
				int reprocessStatus = reprocessFromErrorDatUpdates();
				if (reprocessStatus > 0)
				{
					clearErrorDatUpdates();
				}
				
			}
			// END: 04-Sept-2020
			
//			System.out.println("Total number of records inserted..."+totalRecordsInserted);
			logger.debug("Total number of records inserted..."+totalRecordsInserted);
		} 
		catch (Exception e) {
			logger.info("Unable to push the fault "+faultId+" due to errors. It will keep trying to push in future");
			moveFaultIdToError(faultId);
			logger.error("Unable to process fault id "+faultId, e);
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
					logger.debug("DB-POOL Closing the connection updateDatStagingWithRemote ");
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in cleanup", e);
			}
		}
		return totalRecordsInserted;
	}
	
	private int getFaultIdToProcess()
	{
		int faultId = 0;
		try {
//			mysqlConn = getLocalConnection();
			stmt = mysqlConn.createStatement();
			rs = stmt.executeQuery("select faultId from dat_updates LIMIT 1");
			if (rs.next())
			{
				faultId = rs.getInt("faultId");
			}
		}catch (SQLException sqle) {
			logger.error("SQl Exception occured in getFaultIdToProcess ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in getFaultIdToProcess ", e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				logger.warn("Error in cleanup", e);
			}
		}
		return faultId;
	}
	
	private int removeProcessedFaultId(int faultId)
	{
		int result = 0;
		try {
//			mysqlConn = getLocalConnection();
			stmt = mysqlConn.createStatement();
			result = stmt.executeUpdate("delete from dat_updates where faultId = "+faultId);
			if (result == 0)
			{
				logger.debug("Fault Id "+ faultId+" is not deleted from dat_updates yet");
			}

//			// Delete from dat_staging after it's been pushed to the remote master
//			result = stmt.executeUpdate("delete from dat_staging where faultId = "+faultId);
//			if (result == 0)
//			{
//				logger.error("Fault Id "+ faultId+" is not deleted from dat_staging yet");
//			}

		}catch (SQLException sqle) {
			logger.error("SQl Exception occured in removeProcessedFaultId ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in removeProcessedFaultId ", e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				logger.warn("Error in cleanup", e);
			}
		}
		return result;
	}
	
	private int moveFaultIdToError(int faultId)
	{
		int result = 0;
		try {
//			mysqlConn = getLocalConnection();
			stmt = mysqlConn.createStatement();
			result = stmt.executeUpdate("delete from dat_updates where faultId = "+faultId);
			if (result == 0)
			{
				logger.debug("Fault Id "+ faultId+" is not deleted yet");
			}
			result = stmt.executeUpdate("insert into error_dat_updates set faultId = "+faultId+", updated = now()");
			if (result == 0)
			{
				logger.debug("Fault Id "+ faultId+" is not deleted yet");
			}
			else
			{
				logger.warn("Fault id "+faultId+" is moved to error_dat_updates as there was a warning or error while pushing faults to remote federated table");
			}

		}catch (SQLException sqle) {
			logger.error("SQl Exception occured in removeProcessedFaultId ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in removeProcessedFaultId ", e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				logger.warn("Error in cleanup", e);
			}
		}
		return result;
	}
	
	private int reprocessFromErrorDatUpdates()
	{
		int result = 0;
		try {
			stmt = mysqlConn.createStatement();
			result = stmt.executeUpdate("insert ignore into dat_updates select * from error_dat_updates");
			if (result == 0)
			{
				logger.debug("Unable to reprocess faults that had errors earlier");
			}
			else
			{
				logger.info("Attempting to reprocess the faults that had errors earlier while pushing.");
			}

		}catch (SQLException sqle) {
			logger.error("SQl Exception occured in removeProcessedFaultId ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in removeProcessedFaultId ", e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				logger.warn("Error in cleanup", e);
			}
		}
		return result;
	}
	
	private int clearErrorDatUpdates()
	{
		int result = 0;
		try {
			stmt = mysqlConn.createStatement();
			result = stmt.executeUpdate("truncate error_dat_updates");
			if (result == 0)
			{
				logger.debug("Unable to clear faults from error_dat_updates");
			}
			else
			{
				logger.info("Clearing error_dat_updates table");
			}

		}catch (SQLException sqle) {
			logger.error("SQl Exception occured in removeProcessedFaultId ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in removeProcessedFaultId ", e);
		}
		finally
		{
			try {
				if (rs != null)
				{
					rs.close();
					rs = null;
				}
				if (stmt != null)
				{
					stmt.close();
					stmt = null;
				}
			} catch (SQLException e) {
				logger.warn("Error in cleanup", e);
			}
		}
		return result;
	}
	private void removeFaultFromDatStaging(int faultIdToRemoveFromDatStaging) throws Exception
	{
		// START: 06-FEB-2017	Removes the staging data from MySQL table 'dat_staging' after the successful creation of the COMTRADE file 
		mySqlDataDAO.removeFaultFromDatStaging(faultIdToRemoveFromDatStaging);
		// END: 06-FEB-2017

	}
	public static void main (String args[])
	{
//		M9kComtradeDataPushTask pushFaults = new M9kComtradeDataPushTask();
//		pushFaults.start();
	}
	public int getRunFrequency() {
		return runFrequency;
	}
	public void setRunFrequency(int runFrequency) {
		this.runFrequency = runFrequency;
	}
	
//    private void getLocalConnection() throws SQLException
//    {
//		long start;
//		long end;
//		start = System.currentTimeMillis();
//
//		try
//		{
//			logger.info("Getting connecction");
//			mysqlConn = (JDCConnection) jdcConnectionDriver.getConnection();
//		}
//		catch(Exception e)
//		{
//			try {
//				logger.info("Getting DB connection failed once. Trying again.");
//				mysqlConn = (JDCConnection) jdcConnectionDriver.getConnection();
//			} catch (SQLException e1) {
//				logger.error(e1.getCause());
//				throw e1;
//			}
//		}
//		end = System.currentTimeMillis();
//		
//		logger.info("Total time to get connection each time... "+(end - start));
//    }
	
//    private boolean isMasterStationReachable()
//    {
//    	boolean booConnection = true;
//    	
//		try {
//			getLocalConnection();
//			stmt = mysqlConn.createStatement();
//			stmt.executeQuery("select 1 from federated_dat_staging");
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			logger.info("Station Master is not connected to master or Master Station MySql server is down");
//			booConnection = false;
//		}
//		finally
//		{
//			try {
//				if (stmt != null)
//				{
//					stmt.close();
//					stmt = null;
//				}
//				if (mysqlConn != null)
//				{
//					logger.info("Closing the connection... "+mysqlConn);
//					mysqlConn.close();
//					mysqlConn = null;
//				}
//			} catch (Exception e) {
//				logger.warn("Error in cleanup", e);
//			}
//		}
//
//    	return booConnection;
//    }
}
