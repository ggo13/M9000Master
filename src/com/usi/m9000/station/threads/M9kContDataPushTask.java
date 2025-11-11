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


public class M9kContDataPushTask {
	private int runFrequency = 5; // in seconds
	static boolean booStatus = true;
	private Connection mysqlConn;
	 private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	 ScheduledFuture<?> scheduledTask = null;
	private ExecutorService threadHandlerExecutor;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kContDataPushTask.class);
	public M9kContDataPushTask()
	{
		logger.debug("Entered M9kContComtradeDataPushThread constructor");
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
						updateContWithRemote();
//					}
              }
          };
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, getRunFrequency(), TimeUnit.SECONDS);
          threadHandlerExecutor.execute(new M9kThreadExceptionHandler(scheduledTask));
	}
	
	private int updateContWithRemote()
	{
		int totalRecordsInserted = 0;
		List<Integer> lstId = new ArrayList<Integer>();
		int deleteStatus;
		long start;
		long end;
		Statement stmt = null;
		
		try {
//			System.out.println("Connection Request from updateDatStagingWithRemote method....Is Local? "+isLocalDBConnection());
//			logger.debug("Connection obj "+mysqlConn);
			start = System.currentTimeMillis();
			lstId = getIdsToProcess();
			end = System.currentTimeMillis();
			logger.debug("Time to execute getFaultIdToProcess "+(end-start));
			logger.debug("Total ids retrieved from db "+lstId.size());
//			stmt.setFetchSize(Integer.MIN_VALUE);
			if (lstId.size() > 0)
			{
				logger.debug("DB-POOL New connection from updateContWithRemote ");
				mysqlConn = M9kStationDBUtil.getLocalConnection();
				stmt = mysqlConn.createStatement();
				// on duplicate update doesn't work with federated
//				totalRecordsInserted = stmt.executeUpdate("insert into federated_cont(stationId,expId,name,phase,units,tsLast,sampleRate,sampleCnt,type,data) " +
//						"SELECT a.stationId, b.expId, b.name, b.phase, b.units, b.tsLast, b.sampleRate, b.sampleCnt, b.type, b.data FROM station_details a, cont b where b.id IN ("+getIdsToQuery(lstId)+") "+
//						"son duplicate key update stationId=values(stationId),expId=values(expId),name=values(name),phase=values(phase),units=values(units),tsLast=values(tsLast),sampleRate=values(sampleRate),sampleCnt=values(sampleCnt),type=values(type),data=values(data)");
				
				totalRecordsInserted = stmt.executeUpdate("insert ignore into federated_continuous_comtrade_data(stationId,contDataType,startDateTime,endDateTime,fileName) " +
						"SELECT "+M9kStationDBUtil.getStationDetails().getSystemStationId()+", b.contDataType, b.startDateTime, b.endDateTime, b.fileName FROM  continuous_comtrade_data b where b.id IN ("+getIdsToQuery(lstId)+") ");
				SQLWarning sqlWarning = stmt.getWarnings();
				if (sqlWarning != null)
				{
					do
					{
						logger.error("Error in inserting cont. data records to the remote cont table. Error Code"+sqlWarning.getErrorCode()+" Error message "+sqlWarning.getMessage());
						sqlWarning = sqlWarning.getNextWarning();
					}while (sqlWarning != null);
					stmt.clearWarnings();
				}
				else
				{
					deleteStatus = removeProcessedId(lstId);
					logger.debug("Delete status after removing ids("+getIdsToQuery(lstId)+") is "+deleteStatus);
					if (deleteStatus == 0)
					{
	//					System.out.println("Problem in removing a Id from dat_updates. Risk of infinite loop. quitting.  Id "+id);
						logger.error("Error in deleting the ids "+getIdsToQuery(lstId)+ " from the table. ");
	//					scheduledTask.cancel(true);
					}
				}
			}
			
//			System.out.println("Total number of records inserted..."+totalRecordsInserted);
			logger.debug("Total number of records inserted..."+totalRecordsInserted);
		}catch (SQLException sqle) {
			logger.error("SQl Exception occured in updateContWithRemote ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in updateContWithRemote ", e);
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
					logger.debug("DB-POOL Closing connection from updateContWithRemote ");
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
		ResultSet rs = null;
		Statement stmt = null;
		try {
			logger.debug("DB-POOL New connection from getIdsToProcess ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			
			stmt = mysqlConn.createStatement();
			rs = stmt.executeQuery("select id from continuous_comtrade_data_updates limit 1000");
			while (rs.next())
			{
				lstId.add(rs.getInt("id"));
			}
		}catch (SQLException sqle) {
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
				if (mysqlConn != null)
				{
					logger.debug("DB-POOL closing connection from getIdsToProcess ");
					mysqlConn.close();
					mysqlConn = null;
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
		Statement stmt = null;
		try {
//			mysqlConn = getLocalConnection();
			stmt = mysqlConn.createStatement();
			result = stmt.executeUpdate("delete from continuous_comtrade_data_updates where id  IN ("+getIdsToQuery(lstId)+")");
			if (result == 0)
			{
				logger.error("Id "+ getIdsToQuery(lstId)+" is not deleted yet");
			}
		}catch (SQLException sqle) {
			logger.error("SQl Exception occured in removeProcessedId ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in removeProcessedId ", e);
		}
		finally
		{
			try {
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
		logger.debug("IDs to be updated "+strIds.toString());
		return strIds.toString();
	}
	
	public static void main (String args[])
	{
//		M9kContDataPushTask pushFaults = new M9kContDataPushTask();
//		pushFaults.start();
	}
	public int getRunFrequency() {
		return runFrequency;
	}
	public void setRunFrequency(int runFrequency) {
		this.runFrequency = runFrequency;
	}
	
}
