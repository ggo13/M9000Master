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

import com.usi.m9000.station.Exception.M9kThreadExceptionHandler;
import com.usi.m9000.station.util.M9kStationDBUtil;


public class M9kLTRDataPushTask {
	private int runFrequency = 5; // in seconds
	private Connection mysqlConn;
	private Statement stmt;
	private ResultSet rs;
	 private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	 ScheduledFuture<?> scheduledTask = null;
	private ExecutorService threadHandlerExecutor;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kLTRDataPushTask.class);
	public M9kLTRDataPushTask()
	{
		logger.debug("Entered M9kLTRDataPushTask constructor");
		threadHandlerExecutor = Executors.newSingleThreadExecutor();
	}
	
	public void schedulePush()
	{
		logger.debug("LTR_DEBUG: About to schedule push for every "+getRunFrequency()+" seconds");
		  final Runnable taskPerformer = new Runnable() {
			  @Override
              public void run() {
//            	  if (!isMasterStationReachable())
//					{
//						logger.error("No connections established to Master yet...");
//					}
//					else
//					{
						updateLTRDataToMaster();
//					}
              }
          };
          logger.debug("LTR_DEBUG: Just beffore scheduling...");
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, getRunFrequency(), TimeUnit.SECONDS);
          threadHandlerExecutor.execute(new M9kThreadExceptionHandler(scheduledTask));
	}
	
	private int updateLTRDataToMaster()
	{
		logger.debug("Debug-Entered updateLTRDataToMaster method" + System.currentTimeMillis());
//		logger.info("Info-Entered updateLTRDataToMaster method");
		int totalRecordsInserted = 0;
		int deleteStatus;
		long start;
		long end;
		
		
		try {
//			System.out.println("Connection Request from updateDatStagingWithRemote method....Is Local? "+isLocalDBConnection());
			logger.debug("DB-POOL New connection from updateLTRDataToMaster ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			start = System.currentTimeMillis();
			String strIds = getIdToProcess();
			end = System.currentTimeMillis();
			logger.debug("Time to execute getIdToProcess "+(end-start));
			logger.debug("Ids retrieved from db "+strIds);
			start = System.currentTimeMillis();
//			stmt.setFetchSize(Integer.MIN_VALUE);
			if (!strIds.isEmpty())
			{
				stmt = mysqlConn.createStatement();
				// on duplicate key update doesn't work with federated
//				totalRecordsInserted = stmt.executeUpdate("insert into federated_long_term_dat_staging(stationId,expId, type, tsPrefault, tsTrigger, ltrPreFaultTime, ltrPostFaultTime, lineFreq, ltrSampleRate, ltrSampleCnt, analogs,  data, dataBlob) " +
//						"SELECT a.stationId, b.expId, b.type, b.tsPrefault, b.tsTrigger, b.ltrPreFaultTime, b.ltrPostFaultTime, b.lineFreq, b.ltrSampleRate, b.ltrSampleCnt, b.analogs,  b.data, b.dataBlob FROM station_details a, long_term_dat_staging b where b.id in( "+ strIds+" )" +
//						" on duplicate key update stationId=values(stationId),expId=values(expId),type=values(type),tsPrefault=values(tsPrefault),tsTrigger=values(tsTrigger),ltrPreFaultTime=values(ltrPreFaultTime),ltrPostFaultTime=values(ltrPostFaultTime),lineFreq=values(lineFreq),ltrSampleRate=values(ltrSampleRate),ltrSampleCnt=values(ltrSampleCnt),analogs=values(analogs),data=values(data),dataBlob=values(dataBlob)");
//				totalRecordsInserted = stmt.executeUpdate("insert ignore into federated_long_term_dat_staging(stationId,expId, type, tsPrefault, tsTrigger, tsLast, ltrPreFaultTime, ltrPostFaultTime, lineFreq, ltrSampleRate, ltrSampleCnt, analogs,  data, dataBlob) " +
//						"SELECT a.stationId, b.expId, b.type, b.tsPrefault, b.tsTrigger, b.tsLast, b.ltrPreFaultTime, b.ltrPostFaultTime, b.lineFreq, b.ltrSampleRate, b.ltrSampleCnt, b.analogs,  b.data, b.dataBlob FROM station_details a, long_term_dat_staging b where b.id in( "+ strIds+" )" );

				totalRecordsInserted = stmt.executeUpdate("insert ignore into federated_long_term_dat(stationId,id, faultId, ltType, tsTrigger, sampleRate, length, preFault,  postFault, fileName) " +
						"SELECT "+M9kStationDBUtil.getStationDetails().getSystemStationId()+", id, faultId, ltType, tsTrigger, sampleRate, length, preFault,  postFault, fileName FROM long_term_dat where id in( "+ strIds+" )" );

				end = System.currentTimeMillis();
				logger.debug("Time to insert into federated_long_term_dat "+(end-start));
				SQLWarning sqlWarning = stmt.getWarnings();
				if (sqlWarning != null)
				{
					do
					{
						logger.error("Error in inserting LTR records to the remote long_term_dat table. Error Code"+sqlWarning.getErrorCode()+" Error message "+sqlWarning.getMessage());
						sqlWarning = sqlWarning.getNextWarning();
					}while (sqlWarning != null);
					stmt.clearWarnings();
				}
				else
				{
					start = System.currentTimeMillis();
					deleteStatus = removeProcessedId(strIds);
					end = System.currentTimeMillis();
					logger.debug("Time to remove the processed Ids "+(end-start));
					logger.debug("Delete status after removing id "+strIds+ " is "+deleteStatus);
					if (deleteStatus == 0)
					{
						logger.error("Problem in removing a id from ltr_updates. Risk of infinite loop. quitting. Id "+strIds);
						logger.error("Error in deleting the id "+strIds+ " from the table. ");
	//					scheduledTask.cancel(true);
					}
				}
			}
			
//			System.out.println("Total number of records inserted..."+totalRecordsInserted);
			logger.debug("Total number of records inserted into federated_long_term_dat..."+totalRecordsInserted);
		} catch (SQLException sqle) {
			logger.error("SQl Exception occured in updateLTRDataToMaster ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in updateLTRDataToMaster ", e);
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
					logger.debug("DB-POOL Closing connection from updateLTRDataToMaster ");
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}
		return totalRecordsInserted;
	}
	
	private String getIdToProcess()
	{
		StringBuffer strIds = new StringBuffer();
		try {
//			mysqlConn = getLocalConnection();
			stmt = mysqlConn.createStatement();
			rs = stmt.executeQuery("select id from ltr_updates LIMIT 50");
			while (rs.next())
			{
				if (strIds.length() == 0)
				{
					strIds.append(rs.getInt("id"));
				}
				else
				{
					strIds.append(","+rs.getInt("id"));
				}

			}
		} catch (SQLException sqle) {
			logger.error("SQl Exception occured in getIdToProcess ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in getIdToProcess ", e);
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
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}
		logger.debug("Returning Ids from getIdToProcess "+strIds.toString());
		return strIds.toString().trim();
	}
	
	private int removeProcessedId(String strIds)
	{
		int result = 0;
		try {
//			mysqlConn = getLocalConnection();
			stmt = mysqlConn.createStatement();
			result = stmt.executeUpdate("delete from ltr_updates where id in( "+strIds+" )");
			if (result == 0)
			{
				logger.debug("Id "+ strIds+" are not deleted yet");
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
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}
		return result;
	}
	public int getRunFrequency() {
		return runFrequency;
	}
	public void setRunFrequency(int runFrequency) {
		this.runFrequency = runFrequency;
	}
	
//    private boolean isMasterStationReachable()
//    {
//    	boolean booConnection = true;
//    	
//		try {
//			mysqlConn = M9kStationDBUtil.getLocalConnection();
//			stmt = mysqlConn.createStatement();
//			stmt.executeQuery("select 1 from federated_long_term_dat_staging");
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
////					logger.info("Closing the connection... "+mysqlConn);
//					mysqlConn.close();
//					mysqlConn = null;
//				}
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//    	return booConnection;
//    }
}
