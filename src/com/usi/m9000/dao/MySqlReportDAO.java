package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.CalibrationDTO;
import com.usi.m9000.dto.ReportsDTO;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class MySqlReportDAO implements ReportDAO {

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlReportDAO.class);
	public MySqlReportDAO()
	{

	}
	

	/**
	 * Inserts the report data both locally and remotely
	 */
	@Override
	public void insertIntoReportTable(CalibrationDTO calibrationDTO2) {
		Connection mysqlConn = null;
		PreparedStatement psInsert = null;
//		PreparedStatement psRemoteInsert = null;
		int result = -1;
//		PreparedStatement psDelete = null;
		try {
			logger.debug("DB-POOL New connection from insertIntoCalReportTable ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			
				psInsert = mysqlConn.prepareStatement("insert into reports(report_time, report_action, report, status, status_msg) " +
						"values( ?, ?, ?, ?, ?)" );
			int iIndex = 1;
			psInsert.setString(iIndex++, calibrationDTO2.getCalTime());
			psInsert.setString(iIndex++, calibrationDTO2.getCalAction());
			psInsert.setString(iIndex++, calibrationDTO2.getCalReport().toString());
			psInsert.setString(iIndex++, calibrationDTO2.getCalStatus());
			psInsert.setString(iIndex++, calibrationDTO2.getCalStatusMsg());
			result = psInsert.executeUpdate();
			logger.debug("Total number of cal record inserted "+result);
				
			psInsert.close();
			
//			if (!M9kStationUtil.getReportsDataTransfer().equalsIgnoreCase(M9kStationConstants.DISABLE))
//			{
//				psRemoteInsert = mysqlConn.prepareStatement("insert into federated_reports(stationId, report_time, report_action, report, status, status_msg) " +
//				"values( ?, ?, ?, ?, ?, ?)" );
//				iIndex = 1;
//				psRemoteInsert.setInt(iIndex++, M9kStationDBUtil.getStationDetails().getStationId());
//				psRemoteInsert.setString(iIndex++, calibrationDTO2.getCalTime());
//				psRemoteInsert.setString(iIndex++, calibrationDTO2.getCalAction());
//				psRemoteInsert.setString(iIndex++, calibrationDTO2.getCalReport().toString());
//				psRemoteInsert.setString(iIndex++, calibrationDTO2.getCalStatus());
//				psRemoteInsert.setString(iIndex++, calibrationDTO2.getCalStatusMsg());
//				result = psRemoteInsert.executeUpdate();
//				logger.debug("Total number of cal record inserted "+result);
//	
//				psRemoteInsert.close();
//			}
		} catch (SQLException sqle) {
			logger.error("SQl Exception occured in insertIntoCalReportTable. ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in insertIntoCalReportTable ", e);
			
		}
		finally
		{
			try {
				if (psInsert != null)
				{
					psInsert.close();
					psInsert= null;
				}
//				if (psRemoteInsert != null)
//				{
//					psRemoteInsert.close();
//					psRemoteInsert= null;
//				}
				if (mysqlConn != null)
				{
					logger.debug("DB-POOL Closing the connection from insertIntoCalReportTable ");
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in cleanup", e);
			}
		}
		

		
	}


	@Override
	public String getLastVerifedDate() {
		String lastVerifiedDate = "Not Verified";
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		try
		{
//			logger.debug("DB-POOL New connection from getLastVerifedDate ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Entered getLastVerifedDate in DAO");
			// START: 04-May-2024 - Return Last Calibrated Date, but if the status is not PASS, then return "Calibration verification Failed along with date"
//			ps = mysqlConn.prepareStatement("select max(report_time) as lastVerifiedDate from reports where report_action=? and status=?");
			ps = mysqlConn.prepareStatement("SELECT report_time, status FROM m9000.reports where report_action = ? and report_time = (select max(report_time)  FROM m9000.reports where report_action = ?)");
			// END: 04-May-2024 - Return Last Calibrated Date, but if the status is not PASS, then return "Calibration verification 	Failed along with date"
			int i = 0;
			ps.setString(++i, M9kStationConstants.CALIBRATE_VERIFY);
			// START: 04-May-2024 - Return Last Calibrated Date, but if the status is not PASS, then return "Calibration verification Failed along with date"
//			ps.setString(++i, M9kStationConstants.PASS);
			ps.setString(++i, M9kStationConstants.CALIBRATE_VERIFY);
			// END: 04-May-2024 - Return Last Calibrated Date, but if the status is not PASS, then return "Calibration verification Failed along with date"
			rs = ps.executeQuery();
			while (rs.next())
			{
				// START: 04-May-2024 - Return Last Calibrated Date, but if the status is not PASS, then return "Calibration verification Failed along with date"
//				if (rs.getTimestamp("lastVerifiedDate") != null)
				if (rs.getTimestamp("report_time") != null)
				{
//					lastVerifiedDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("lastVerifiedDate"));
					lastVerifiedDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("report_time"));
					if (rs.getString("status").equals(M9kStationConstants.FAIL))
					{
						lastVerifiedDate = lastVerifiedDate.concat(" STATUS: Calibration Verification Failed ");
					}
				}
				// END: 04-May-2024 - Return Last Calibrated Date, but if the status is not PASS, then return "Calibration verification Failed along with date"
			}
			logger.debug("Last verified date "+lastVerifiedDate);
		}
		catch (SQLException e) {
				logger.error("Error in reading reports table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading reports table ",e);
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
//						logger.debug("DB-POOL Close connection from getLastVerifedDate ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Error in finally section ",e);
				}

	}

		return lastVerifiedDate;
	}
	

	@Override
	public String getLastCalibratedDate() {
		String lastCalibratedDate = "Not Calibrated";
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		try
		{
//			logger.debug("DB-POOL New connection from getLastVerifedDate ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Entered getLastCalibratedDate in DAO");
			// START: 04-May-2024 - Return Last Calibrated Date, but if the status is not PASS, then return "Calibration Failed along with date"
//			ps = mysqlConn.prepareStatement("select max(report_time) as lastCalibratedDate from reports where report_action=? and status=?");
			ps = mysqlConn.prepareStatement("SELECT report_time, status FROM m9000.reports where report_action = ? and report_time = (select max(report_time)  FROM m9000.reports where report_action = ?)");
			// END: 04-May-2024 - Return Last Calibrated Date, but if the status is not PASS, then return "Calibration Failed along with date"
			int i = 0;
			ps.setString(++i, M9kStationConstants.CALIBRATE_APPLY);
			// START: 04-May-2024 - Return Last Calibrated Date, but if the status is not PASS, then return "Calibration Failed along with date"
//			ps.setString(++i, M9kStationConstants.PASS);
			ps.setString(++i, M9kStationConstants.CALIBRATE_APPLY);
			// STOP: 04-May-2024 - Return Last Calibrated Date, but if the status is not PASS, then return "Calibration Failed along with date"
			rs = ps.executeQuery();
			while (rs.next())
			{
				// START: 04-May-2024 - Return Last lastCalibratedDate Date, but if the status is not PASS, then return "Calibration Failed along with date"
//				if (rs.getTimestamp("lastCalibratedDate") != null)
				if (rs.getTimestamp("report_time") != null)
				{
//					lastCalibratedDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("lastCalibratedDate"));
					lastCalibratedDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("report_time"));
					if (rs.getString("status").equals(M9kStationConstants.FAIL))
					{
						lastCalibratedDate = lastCalibratedDate.concat(" STATUS: Calibration Failed ");
					}
				}
				// END: 04-May-2024 - Return Last Calibrated Date, but if the status is not PASS, then return "Calibration Failed along with date"
			}
			logger.debug("Last Calibrated date "+lastCalibratedDate);
		}
		catch (SQLException e) {
				logger.error("Error in reading reports table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading reports table ",e);
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
//						logger.debug("DB-POOL Close connection from getLastVerifedDate ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Error in finally section ",e);
				}

	}

		return lastCalibratedDate;
	}

	@Override
	public String getLastEventTestDate() {
		String lastEventTestDate = "Event Test not run";
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		try
		{
//			logger.debug("DB-POOL New connection from getLastVerifedDate ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Entered getLastEventTestDate in DAO");
			// START: Return Last Event Test Date, but if the status is not PASS, then return "Event Failed along with date"  
//			ps = mysqlConn.prepareStatement("select max(report_time) as lastEventTestDate from reports where report_action=? and status=?");
			ps = mysqlConn.prepareStatement("SELECT report_time, status FROM m9000.reports where report_action = ? and report_time = (select max(report_time)  FROM m9000.reports where report_action = ?)");
			// END: Return Last Event Test Date, but if the status is not PASS, then return "Event Failed along with date"
			int i = 0;
			ps.setString(++i, M9kStationConstants.EVENTTEST);
//			ps.setString(++i, M9kStationConstants.PASS);
			ps.setString(++i, M9kStationConstants.EVENTTEST);
			// START: Return Last Event Test Date, but if the status is not PASS, then return "Event Failed along with date"
//			ps.setString(++i, M9kStationConstants.PASS);
			// STOP: Return Last Event Test Date, but if the status is not PASS, then return "Event Failed along with date"
			rs = ps.executeQuery();
			while (rs.next())
			{
				// START: Return Last Event Test Date, but if the status is not PASS, then return "Event Failed along with date"
//				if (rs.getTimestamp("lastEventTestDate") != null)
				if (rs.getTimestamp("report_time") != null)
				{
//					lastEventTestDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("lastEventTestDate"));
					lastEventTestDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("report_time"));
					if (rs.getString("status").equals(M9kStationConstants.FAIL))
					{
						logger.debug("Before: Last Event Test date "+lastEventTestDate);
						lastEventTestDate = lastEventTestDate.concat(" STATUS: Event Test Failed ");
						logger.debug("After: Last Event Test date "+lastEventTestDate);
					}
				}
				// END: Return Last Event Test Date, but if the status is not PASS, then return "Event Failed along with date"
			}
			logger.debug("Last Event Test date "+lastEventTestDate);
		}
		catch (SQLException e) {
				logger.error("Error in reading reports table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading reports table ",e);
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
//						logger.debug("DB-POOL Close connection from getLastVerifedDate ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Error in finally section ",e);
				}

	}
		logger.debug("Returning Last Event Test date "+lastEventTestDate);
		return lastEventTestDate;
	}

	@Override
	public Map<String, ReportsDTO> getMostRecentReport(String reportAction) {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		Map<String, ReportsDTO> mapStationReports = new HashMap<String, ReportsDTO>();
		String stationKey;
		ReportsDTO reportsDto;
		try
		{
//			logger.debug("DB-POOL New connection from getLastVerifedDate ");
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered getLastVerifedStatus in DAO");
//			ps = mysqlConn.prepareStatement("select a.name as  stationName, max(b.report_time) as reportTime, b.stationId, b.report,b.status,b.status_msg from station_details a, reports b where a.stationId=b.stationId and b.report_action=? group by b.stationId");
			if (M9kUtils.isRemote())
			{
				if (reportAction.equalsIgnoreCase(M9kConstants.CALIBRATE_VERIFY))
				{
//					ps=mysqlConn.prepareStatement("select a.name as  stationName, a.stationId as stationId,reports.reportTime, reports.report,reports.status,reports.status_msg from station_details a " +
//							"left join (select max(b.report_time) as reportTime, b.stationId, b.report,b.status,b.status_msg from station_details a, reports b where b.report_action=? or b.report_action=? group by b.stationId) as reports " +
//							"on a.stationId=reports.stationId order by a.stationId");
					ps=mysqlConn.prepareStatement("select s.name as  stationName,s.stationId as stationId, MAX(sub.report_time) as reportTime, sub.report,sub.status,sub.status_msg from station_details s "+
							" left join "+
							" ( " +
							" SELECT o.* "+
							" FROM `reports` o "+
							" LEFT JOIN `reports` b "+
							" ON o.stationId = b.stationId AND o.report_action = b.report_action AND o.report_time < b.report_time "+
							" WHERE b.report_time is NULL and (o.report_action = ? or b.report_action=?)" + 
							" ) sub on s.stationId = sub.stationId  group by s.stationId,sub.report_time,sub.report, sub.status,sub.status_msg");
				}
				else
				{
//					ps=mysqlConn.prepareStatement("select a.name as  stationName, a.stationId as stationId,reports.reportTime, reports.report,reports.status,reports.status_msg from station_details a " +
//							"left join (select max(b.report_time) as reportTime, b.stationId, b.report,b.status,b.status_msg from station_details a, reports b where b.report_action=? group by b.stationId) as reports " +
//							"on a.stationId=reports.stationId order by a.stationId");
					ps=mysqlConn.prepareStatement("select s.name as  stationName,s.stationId as stationId, sub.report_time as reportTime, sub.report,sub.status,sub.status_msg from station_details s "+
					" left join "+
					" ( " +
					" SELECT o.* "+
					" FROM `reports` o "+
					" LEFT JOIN `reports` b "+
					" ON o.stationId = b.stationId AND o.report_action = b.report_action AND o.report_time < b.report_time "+
					" WHERE b.report_time is NULL and o.report_action = ? " + 
					" ) sub on s.stationId = sub.stationId");
				}
			}
			else
			{
				if (reportAction.equalsIgnoreCase(M9kConstants.CALIBRATE_VERIFY))
				{
//					ps=mysqlConn.prepareStatement("select a.name as stationName, a.stationId as stationId, max(b.report_time) as reportTime, b.report,b.status,b.status_msg from station_details a, reports b where b.report_action=? or b.report_action=?");
					ps=mysqlConn.prepareStatement("select a.name as stationName, a.stationId as stationId, b.report_time as reportTime, b.report_action, b.report,b.status,b.status_msg from station_details a, reports b "
							+ " inner join (select max(report_time) as maxReportTime from reports where report_action=? or report_action=?) maxTime on b.report_time = maxTime.maxReportTime");
					 
					

				}
				else
				{
//					ps=mysqlConn.prepareStatement("select a.name as stationName, a.stationId as stationId, max(b.report_time) as reportTime, b.report,b.status,b.status_msg from station_details a, reports b where b.report_action=?");
					ps=mysqlConn.prepareStatement("select a.name as stationName, a.stationId as stationId, b.report_time as reportTime, b.report_action, b.report,b.status,b.status_msg from station_details a, reports b "
							+ " inner join (select max(report_time) as maxReportTime from reports where report_action=?) maxTime on b.report_time = maxTime.maxReportTime");
//					logger.debug(("select a.name as stationName, a.stationId as stationId, b.report_time as reportTime, b.report_action, b.report,b.status,b.status_msg from station_details a, reports b "
//							+ " inner join (select max(report_time) as maxReportTime from reports where report_action='"+reportAction+"') maxTime on b.report_time = maxTime.maxReportTime"));

				}
			}
			int i = 0;
			ps.setString(++i, reportAction);
			if (reportAction.equalsIgnoreCase(M9kConstants.CALIBRATE_VERIFY))
			{
				ps.setString(++i, M9kConstants.CALIBRATE_APPLY);
			}
			rs = ps.executeQuery();
			while (rs.next())
			{
				reportsDto = new ReportsDTO();
				reportsDto.setStationId(rs.getInt("stationId"));
				reportsDto.setStationName(rs.getString("stationName"));
				reportsDto.setReport_action(reportAction);
				if (rs.getTimestamp("reportTime") != null)
				{
					reportsDto.setReport_time(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("reportTime")));
				}
				reportsDto.setReport(rs.getString("report"));
				reportsDto.setStatus(rs.getString("status"));
				reportsDto.setStatus_msg(rs.getString("status_msg"));
				stationKey = rs.getString("stationId")+"-"+rs.getString("stationName");
				mapStationReports.put(stationKey, reportsDto);
			}
		}
		catch (SQLException e) {
				logger.error("Error in reading reports table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading reports table ",e);
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
//						logger.debug("DB-POOL Close connection from getLastVerifedDate ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Error in finally section ",e);
				}

	}

		return mapStationReports;
	}

	@Override
	public void insertIntoReportTable(ReportsDTO reportsDTO) {
		Connection mysqlConn = null;
		PreparedStatement psInsert = null;
		int result = -1;
//		PreparedStatement psDelete = null;
		try {
			logger.debug("DB-POOL New connection from insertIntoCalReportTable ");
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			
				psInsert = mysqlConn.prepareStatement("insert into reports_health(stationId, report_time, report_type, report,report_file_name, status, status_msg) " +
						"values( ?, ?, ?, ?, ?, ?,?)" );
			int iIndex = 1;
			psInsert.setInt(iIndex++, reportsDTO.getStationId());
			psInsert.setString(iIndex++, reportsDTO.getReport_time());
			psInsert.setString(iIndex++, reportsDTO.getReport_action());
			psInsert.setString(iIndex++, reportsDTO.getReport());
			psInsert.setString(iIndex++, reportsDTO.getReportFileName());
			psInsert.setString(iIndex++, reportsDTO.getStatus());
			psInsert.setString(iIndex++, reportsDTO.getStatus_msg());
			result = psInsert.executeUpdate();
			logger.debug("Total number of cal record inserted "+result);
				
			psInsert.close();
			
		} catch (SQLException sqle) {
			logger.error("SQl Exception occured in insertIntoCalReportTable. ", sqle);
		}
		catch (Exception e) {
			logger.error("Exception occured in insertIntoCalReportTable ", e);
			
		}
		finally
		{
			try {
				if (psInsert != null)
				{
					psInsert.close();
					psInsert= null;
				}
				if (mysqlConn != null)
				{
					logger.debug("DB-POOL Closing the connection from insertIntoCalReportTable ");
					mysqlConn.close();
					mysqlConn = null;
				}
				
			} catch (SQLException e) {
				logger.warn("Error in cleanup", e);
			}
		}
		

		
	}
}
