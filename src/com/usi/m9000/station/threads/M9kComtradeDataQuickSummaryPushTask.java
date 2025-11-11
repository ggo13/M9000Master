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

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.station.Exception.M9kThreadExceptionHandler;
import com.usi.m9000.station.util.M9kSeverityLevels;
import com.usi.m9000.station.util.M9kStationComtradeUtil;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationUtil;


public class M9kComtradeDataQuickSummaryPushTask {
	private int runFrequency = 1; // in seconds
	static boolean booStatus = true;
	private Connection mysqlConn;
	private Statement stmt;
	private ResultSet rs;
	 private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	 ScheduledFuture<?> scheduledTask = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kComtradeDataQuickSummaryPushTask.class);
	ExecutorService threadHandlerExecutor;
	
	public M9kComtradeDataQuickSummaryPushTask()
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
		threadHandlerExecutor = Executors.newSingleThreadExecutor();
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
		ComtradeDataDTO comtradeDataDTO;
		
		
		try {
//			System.out.println("Connection Request from updateDatStagingWithRemote method....Is Local? "+isLocalDBConnection());
//			getLocalConnection();
//			mysqlConn = M9kMySqlDatabase.getInstance().getLocalDBConnection();
			logger.debug("DB-POOL New connection from updateDatStagingWithRemote ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Connection obj "+mysqlConn);
			start = System.currentTimeMillis();
			comtradeDataDTO = getFaultIdToProcess();
			end = System.currentTimeMillis();
			logger.debug("Time to execute getFaultIdToProcess "+(end-start));
			logger.debug("Fault id retrieved from db "+comtradeDataDTO);
//			stmt.setFetchSize(Integer.MIN_VALUE);
			if (comtradeDataDTO != null)
			{
				faultId = comtradeDataDTO.getFaultId();
				stmt = mysqlConn.createStatement();

				// Use of on dupicate key update doesn't work with federated tables
//				totalRecordsInserted = stmt.executeUpdate("insert into federated_dat_staging(stationId, faultId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, data, dataBlob) " +
//						"SELECT a.stationId, b.faultId, b.type, b.tsPrefault, b.tsTrigger, b.lineFreq, b.sampleRate, b.sampleCnt, b.analogs, b.events, b.data,b.dataBlob FROM station_details a, dat_staging b where b.faultId ="+ faultId
//						+" on duplicate key update stationId=values(stationId), faultId=values(faultId), type=values(type), tsPrefault=values(tsPrefault), tsTrigger=values(tsTrigger), lineFreq=values(lineFreq), sampleRate=values(sampleRate), sampleCnt=values(sampleCnt), analogs=values(analogs), events=values(events), data=values(data), dataBlob=values(dataBlob)");

//				totalRecordsInserted = stmt.executeUpdate("insert ignore into federated_dat_staging(stationId, faultId, type, tsPrefault, tsTrigger, lineFreq, sampleRate, sampleCnt, analogs, events, data, dataBlob, faultLocation) " +
//						"SELECT a.stationId, b.faultId, b.type, b.tsPrefault, b.tsTrigger, b.lineFreq, b.sampleRate, b.sampleCnt, b.analogs, b.events, b.data,b.dataBlob,b.faultLocation FROM station_details a, dat_staging b where b.faultId ="+ faultId);
				
				totalRecordsInserted = stmt.executeUpdate("insert ignore into federated_comtrade_details(station_id, fault_id, time_stamp, events, length, pre_fault, post_fault, fault_location, file_name, fault_logic, line_groups, comments) " +
						"SELECT "+M9kStationDBUtil.getStationDetails().getSystemStationId()+", fault_id, time_stamp, events, length, pre_fault, post_fault, fault_location, file_name, fault_logic, line_groups, comments FROM comtrade_details where fault_id ="+ faultId);
				
				SQLWarning sqlWarning = stmt.getWarnings();
				if (sqlWarning != null)
				{
					do
					{
						logger.error("Error in inserting fault records to the remote comtrade_details table. Error Code"+sqlWarning.getErrorCode()+" Error message "+sqlWarning.getMessage());
						sqlWarning = sqlWarning.getNextWarning();
					}while (sqlWarning != null);
					stmt.clearWarnings();
				}
				else
				{
					try {
						if (M9kStationUtil.getTriggerNotify().equalsIgnoreCase(M9kStationConstants.ENABLE))
						{
							StringBuffer faultDetails = new StringBuffer("We have a new fault in the station "+M9kStationDBUtil.getStationTitle());
							faultDetails.append(M9kStationConstants.NEWLINE);
							faultDetails.append(comtradeDataDTO.getFaultDetailsSummary());
//							faultDetails.append(M9kStationConstants.NEWLINE);
//							faultDetails.append("FaultId: "+faultId);
//							faultDetails.append(M9kStationConstants.NEWLINE);
//							faultDetails.append("Date of Occurance: "+comtradeDataDTO.getDisplayTime());
//							faultDetails.append(M9kStationConstants.NEWLINE);
//							faultDetails.append("Abnormal events: "+comtradeDataDTO.getActiveEvents());
//							faultDetails.append(M9kStationConstants.NEWLINE);
//							if (comtradeDataDTO.getFaultLocationDetails() != null && !comtradeDataDTO.getFaultLocationDetails().isEmpty())
//							{
//								faultDetails.append("Fault Location Details: "+comtradeDataDTO.getFaultLocationDetails());
//							}
							M9kStationUtil.sendNotification(M9kSeverityLevels.INFO.name(), faultDetails.toString());
						}
					} catch (M9000Exception e1) {
						logger.error("Unable to notify trigger event to master",e1);
					}

					deleteStatus = removeProcessedFaultId(faultId);
					logger.debug("Delete status after removing faultid "+faultId+ " is "+deleteStatus);
					if (deleteStatus == 0)
					{
						logger.error("Error in deleting the fault id "+faultId+ " from the table. ");
	//					scheduledTask.cancel(true);
					}
				}
			}
			
//			System.out.println("Total number of records inserted..."+totalRecordsInserted);
			logger.debug("Total number of records inserted..."+totalRecordsInserted);
		} catch (SQLException sqle) {
			logger.error("SQl Exception occured in updateDatStagingWithRemote ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in updateDatStagingWithRemote ", e);
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
	
	private ComtradeDataDTO getFaultIdToProcess()
	{
//		int faultId = 0;
		ComtradeDataDTO comtradeDataDTO = null;
		try {
//			mysqlConn = getLocalConnection();
			stmt = mysqlConn.createStatement();
			rs = stmt.executeQuery("SELECT cd.* FROM comtrade_details AS cd " + 
					"LEFT JOIN comtrade_details_updates AS cdu ON cdu.faultId = cd.fault_id " + 
					"WHERE cd.fault_Id = (SELECT faultid FROM comtrade_details_updates order by faultId desc limit 1);");
			if (rs.next())
			{
				comtradeDataDTO = new ComtradeDataDTO();
				comtradeDataDTO.setFaultId(rs.getInt("fault_id"));
				comtradeDataDTO.setStationId(rs.getInt("station_id"));
				comtradeDataDTO.setTsTrigger(rs.getLong("time_stamp"));
				comtradeDataDTO.setEvents(new StringBuffer(rs.getString("events")));
				comtradeDataDTO.setLength(rs.getDouble("length"));
				comtradeDataDTO.setPreFault(rs.getLong("pre_fault"));
				comtradeDataDTO.setPostFault(rs.getLong("post_fault"));
				comtradeDataDTO.setFileName(rs.getString("file_name"));
				comtradeDataDTO.setActiveEvents(rs.getString("events"));
				if (rs.getString("fault_location") != null)
				{
					comtradeDataDTO.setFaultLocationDetails(rs.getString("fault_location"));
				}
				else
				{
					comtradeDataDTO.setFaultLocationDetails("");
				}

				comtradeDataDTO.setFaultLogic(rs.getBoolean("fault_logic"));
				if (rs.getString("comments") != null)
				{
					comtradeDataDTO.setUserComments(rs.getString("comments"));
				}
				else
				{
					comtradeDataDTO.setUserComments("");
				}
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
		return comtradeDataDTO;
	}
	
	private int removeProcessedFaultId(int faultId)
	{
		int result = 0;
		try {
//			mysqlConn = getLocalConnection();
			stmt = mysqlConn.createStatement();
			result = stmt.executeUpdate("delete from comtrade_details_updates where faultId = "+faultId);
			if (result == 0)
			{
				logger.debug("Fault Id "+ faultId+" is not deleted yet");
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
