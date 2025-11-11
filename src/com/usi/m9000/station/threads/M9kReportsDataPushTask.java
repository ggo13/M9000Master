package com.usi.m9000.station.threads;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.usi.m9000.station.Exception.M9kThreadExceptionHandler;
import com.usi.m9000.station.util.M9kStationDBUtil;


public class M9kReportsDataPushTask {
	private int runFrequency = 5; // in seconds
	static boolean booStatus = true;
	private Connection mysqlConn;
	private Statement stmt;
	private ResultSet rs;
	 private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	 ScheduledFuture<?> scheduledTask = null;
	private ExecutorService threadHandlerExecutor;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kReportsDataPushTask.class);
	public M9kReportsDataPushTask()
	{
		logger.debug("Entered M9kReportsDataPushTask constructor");
		threadHandlerExecutor = Executors.newSingleThreadExecutor();
  	  
	}
	
	public void scheduleReportsPush()
	{
		  final Runnable taskPerformer = new Runnable() {
              public void run() {
//            	  if (!isMasterStationReachable())
//					{
//						logger.error("No connections established to Master yet...");
//					}
//					else
//					{
						updateReportsDataToRemote();
//					}
              }
          };
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, getRunFrequency(), TimeUnit.SECONDS);
          threadHandlerExecutor.execute(new M9kThreadExceptionHandler(scheduledTask));
	}
	
	private int updateReportsDataToRemote()
	{
		int totalRecordsInserted = 0;
		List<Integer> lstId = new ArrayList<Integer>();
		int deleteStatus;
		long start;
		long end;
		
		
		try {
//			System.out.println("Connection Request from updateDatStagingWithRemote method....Is Local? "+isLocalDBConnection());
			logger.debug("DB-POOL New connection from updateReportsDataToRemote ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Connection obj "+mysqlConn);
			start = System.currentTimeMillis();
			lstId = getIdsToProcess();
			end = System.currentTimeMillis();
			logger.debug("Time to execute getFaultIdToProcess "+(end-start));
			logger.debug("Total ids retrieved from db "+lstId.size());
//			stmt.setFetchSize(Integer.MIN_VALUE);
			if (lstId.size() > 0)
			{
				stmt = mysqlConn.createStatement();
				totalRecordsInserted = stmt.executeUpdate("insert ignore into federated_reports(stationId, report_time, report_action, report, status, status_msg, updated) " +
						"select "+M9kStationDBUtil.getStationDetails().getSystemStationId()+", report_time, report_action, report, status, status_msg, updated from  reports  where idreports in ("+getIdsToQuery(lstId)+") ");
				SQLWarning sqlWarning = stmt.getWarnings();
				if (sqlWarning != null)
				{
					do
					{
						logger.error("Error in inserting ser records to the remote alarm table. Error Code"+sqlWarning.getErrorCode()+" Error message "+sqlWarning.getMessage());
						sqlWarning = sqlWarning.getNextWarning();
					}while (sqlWarning != null);
					stmt.clearWarnings();
				}
				else
				{

					deleteStatus = removeProcessedId(lstId);
					logger.debug("Delete status after removing ids is "+deleteStatus);
					if (deleteStatus == 0)
					{
	//					System.out.println("Problem in removing a Id from dat_updates. Risk of infinite loop. quitting. Id "+getIdsToQuery(lstId));
						logger.error("Error in deleting the ids "+getIdsToQuery(lstId)+ " from the table. ");
	//					scheduledTask.cancel(true);
					}
				}
			}
			
			logger.debug("Total number of records inserted..."+totalRecordsInserted);
		} catch (SQLException sqle) {
			logger.error("SQl Exception occured in updatealarmToRemote ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in updatealarmToRemote ", e);
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
					logger.debug("DB-POOL Closing connection from updatealarmToRemote ");
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (Exception e) {
				logger.warn("Error in cleanup", e);
			}
		}
		return totalRecordsInserted;
	}
	
	private List<Integer> getIdsToProcess()
	{
		List<Integer> lstId = new ArrayList<Integer>();
		try {
//			mysqlConn = getLocalConnection();
			stmt = mysqlConn.createStatement();
			rs = stmt.executeQuery("select id from reports_updates");
			while (rs.next())
			{
				lstId.add(rs.getInt("id"));
			}
		} catch (SQLException sqle) {
			logger.error("SQl Exception occured in getIdsToProcess ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in getIdsToProcess ", e);
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
		return lstId;
	}
	
	private int removeProcessedId(List<Integer> lstId)
	{
		int result = 0;
		try {
//			mysqlConn = getLocalConnection();
			stmt = mysqlConn.createStatement();
			result = stmt.executeUpdate("delete from reports_updates where id IN ("+getIdsToQuery(lstId)+")");
			if (result == 0)
			{
				logger.error("Id "+ getIdsToQuery(lstId)+" is not deleted yet");
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
				// TODO Auto-generated catch block
				logger.warn("Problem in closing resultset and statment objects ",e);
			}
		}
		return result;
	}
	
	private String getIdsToQuery(List<Integer> lstId)
	{
		StringBuffer strIds = new StringBuffer();
		for (Iterator<Integer> iterator = lstId.iterator(); iterator.hasNext();) {
			int id = iterator.next();
			if (strIds.length() == 0)
			{
				strIds.append(id);
			}
			else
			{
				strIds.append(","+id);
			}
		}
		logger.debug("reports id to query "+strIds.toString());
		return strIds.toString();
	}
	public static void main (String args[])
	{
//		M9kSERDataPushTask pushFaults = new M9kSERDataPushTask();
//		pushFaults.start();
	}
	public int getRunFrequency() {
		return runFrequency;
	}
	public void setRunFrequency(int runFrequency) {
		this.runFrequency = runFrequency;
	}
	
	
}
