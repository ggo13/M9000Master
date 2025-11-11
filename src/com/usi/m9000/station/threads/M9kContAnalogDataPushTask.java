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


public class M9kContAnalogDataPushTask {
	private int runFrequency = 5; // in seconds
	static boolean booStatus = true;
	private Connection mysqlConn;
	private Statement stmt;
	private ResultSet rs;
	 private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	 ScheduledFuture<?> scheduledTask = null;
	private ExecutorService threadHandlerExecutor;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kContAnalogDataPushTask.class);
	public M9kContAnalogDataPushTask()
	{
		logger.debug("Entered M9kComtradeDataPushThread constructor");
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
						updateContAnalogWithRemote();
//					}
              }
          };
          scheduledTask = scheduler.scheduleWithFixedDelay(taskPerformer, 0, getRunFrequency(), TimeUnit.SECONDS);
          threadHandlerExecutor.execute(new M9kThreadExceptionHandler(scheduledTask));
	}
	
	private int updateContAnalogWithRemote()
	{
		int totalRecordsInserted = 0;
		int deleteStatus;
		long start;
		long end;
		
		
		try {
//			System.out.println("Connection Request from updateDatStagingWithRemote method....Is Local? "+isLocalDBConnection());
			logger.debug("DB-POOL New connection from updateContAnalogWithRemote ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Connection obj "+mysqlConn);
			start = System.currentTimeMillis();
			String strIds = getIdToProcess();
			end = System.currentTimeMillis();
			logger.debug("Time to execute getIdToProcess "+(end-start));
			logger.debug("Id retrieved from db "+strIds);
//			stmt.setFetchSize(Integer.MIN_VALUE);
			if (!strIds.isEmpty())
			{
				stmt = mysqlConn.createStatement();

				// On duplicate key doesn't work with federated
//				totalRecordsInserted = stmt.executeUpdate("insert into federated_contAnalog(stationId,expId,name,phase,units,tsLast,sampleRate,sampleCnt,scale,offset,data) " +
//						"SELECT a.stationId, b.expId, b.name, b.phase, b.units, b.tsLast, b.sampleRate, b.sampleCnt, b.scale, b.offset, b.data FROM station_details a, contAnalog b where b.id in( "+ strIds+" )"+
//						" on duplicate key update stationId=values(stationId),expId=values(expId),name=values(name),phase=values(phase),units=values(units),tsLast=values(tsLast),sampleRate=values(sampleRate),sampleCnt=values(sampleCnt),scale=values(scale),offset=values(offset),data=values(data)");

				totalRecordsInserted = stmt.executeUpdate("insert ignore into federated_contAnalog(stationId,expId,name,phase,units,tsLast,sampleRate,sampleCnt,scale,offset,data) " +
						"SELECT "+M9kStationDBUtil.getStationDetails().getSystemStationId()+", b.expId, b.name, b.phase, b.units, b.tsLast, b.sampleRate, b.sampleCnt, b.scale, b.offset, b.data FROM contAnalog b where b.id in( "+ strIds+" )");
				SQLWarning sqlWarning = stmt.getWarnings();
				if (sqlWarning != null)
				{
					do
					{
						logger.error("Error in inserting cont. analog records to the remote contAnalog table. Error Code"+sqlWarning.getErrorCode()+" Error message "+sqlWarning.getMessage());
						sqlWarning = sqlWarning.getNextWarning();
					}while (sqlWarning != null);
					stmt.clearWarnings();
				}
				else
				{

					deleteStatus = removeProcessedId(strIds);
					logger.debug("Delete status after removing ids "+strIds+ " are "+deleteStatus);
					if (deleteStatus == 0)
					{
//						logger.debug("Problem in removing a id from dat_updates. Risk of infinite loop. quitting. Ids "+strIds);
						logger.error("Error in deleting  "+strIds+ " from the table. ");
	//					scheduledTask.cancel(true);
					}
				}
			}
			
//			System.out.println("Total number of records inserted..."+totalRecordsInserted);
			logger.debug("Total number of records inserted..."+totalRecordsInserted);
		} catch (SQLException sqle) {
			logger.error("SQl Exception occured in updateContAnalogWithRemote ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in updateContAnalogWithRemote ", e);
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
					logger.debug("DB-POOL closing connection from updateContAnalogWithRemote ");
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
			rs = stmt.executeQuery("select id from contAnalog_updates LIMIT 100");
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
			result = stmt.executeUpdate("delete from contAnalog_updates where id in( "+strIds+" )");
			if (result == 0)
			{
				logger.debug("Ida "+ strIds+" is not deleted yet");
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
	public static void main (String args[])
	{
//		M9kContAnalogDataPushTask pushContAnalog = new M9kContAnalogDataPushTask();
//		pushContAnalog.start();
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
//			getLocalConnection();
//			stmt = mysqlConn.createStatement();
//			stmt.executeQuery("select 1 from federated_contAnalog");
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
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//    	return booConnection;
//    }
}
