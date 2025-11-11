/**
 * 
 */
package com.usi.m9000.station.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sramasamy
 *
 */
public class M9kStationSignatureServer {

	Lock lock;
	private static int SIGNATURE=0;
	private static M9kStationSignatureServer m9kStationSignatureServer = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationSignatureServer.class);
	/**
	 * 
	 */
	private M9kStationSignatureServer() {
		SIGNATURE = getCurrentSignature()+1;
		lock = new ReentrantLock();
	}

	public static M9kStationSignatureServer getInstance()
	{
		if (m9kStationSignatureServer == null)
		{
			m9kStationSignatureServer = new M9kStationSignatureServer();
		}
		return m9kStationSignatureServer;
	}
	
	public int getNewSignature()
	{
		try
		{
			lock.lock();
			SIGNATURE++;
		}
		catch (Exception e) {
			logger.error("Error trying to get lock for new signature. Incrementing signature to be safe ", e);
			SIGNATURE++;
		}
		finally {
			lock.unlock();
		}
		logger.debug("Last signature returned "+SIGNATURE);
		return SIGNATURE;
	}
	
	public int getCurrentSignature() {
		int maxSignature = 0;
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		try 
		{
			logger.debug("DB-POOL New connection from getCurrentSignature ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Entered getCurrentSignature in DAO");
			ps = mysqlConn.prepareStatement("select max(signature) as maxSignature from dat");

			rs = ps.executeQuery();
			if (rs.next())
			{
				maxSignature = rs.getInt("maxSignature");
				
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("Error in getting max signature available from dat table",e);
		}
		catch (Exception e) {
			logger.error("Error in getting max signature available  from dat table",e);
		}
		finally
		{
			try {
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
					logger.debug("DB-POOL Closing connection from getCurrentSignature ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return maxSignature; 
	}

}
