package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.EmailAddressDTO;
import com.usi.m9000.dto.EmailReportsSettingsDTO;
import com.usi.m9000.dto.EmailSettingsDTO;
import com.usi.m9000.util.M9kConstants;

public class MySqlEmailDAO implements EmailDAO {

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlEmailDAO.class);
	public MySqlEmailDAO()
	{
	}
	@Override
	public List<EmailAddressDTO> getEmailsList() {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		EmailAddressDTO emailAddressDTO;
		List<EmailAddressDTO> lstEmailAddresses = new ArrayList<EmailAddressDTO>();
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			ps = mysqlConn.prepareStatement("select * from emailsSubscriptionList order by id");
			rs = ps.executeQuery();
			while (rs.next())
			{
				emailAddressDTO = new EmailAddressDTO();
				emailAddressDTO.setId(rs.getInt("id"));
				emailAddressDTO.setFirstName(rs.getString("firstName"));
				emailAddressDTO.setLastName(rs.getString("lastName"));
				emailAddressDTO.setEmailAddress(rs.getString("emailAddress"));
				lstEmailAddresses.add(emailAddressDTO);
			}
		}
		catch (Exception e) {
			logger.error("Unable to get emails list ",e);
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
//					logger.debug("DB-POOL Close connection from getLastVerifedDate ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Error in finally section ",e);
			}
		}
		return lstEmailAddresses;
	}
	@Override
	public int removeEmailAddress(EmailAddressDTO emailAddressDTO) {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		int deleteCount = 0;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			ps = mysqlConn.prepareStatement("delete from emailsSubscriptionList where id = ?");
			ps.setInt(1, emailAddressDTO.getId());
			deleteCount = ps.executeUpdate();
		}
		catch (Exception e) {
			logger.error("Unable to delete email address "+emailAddressDTO,e);
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
//					logger.debug("DB-POOL Close connection from getLastVerifedDate ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Error in finally section ",e);
			}
		}
		return deleteCount;
	}
	@Override
	public int addEmailAddress(EmailAddressDTO emailAddressDTO) throws SQLException{
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		int insertCount = 0;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			int i = 0;
			ps = mysqlConn.prepareStatement("insert into emailsSubscriptionList set firstName= ?, lastName = ?, emailAddress = ?");
			ps.setString(++i, emailAddressDTO.getFirstName());
			ps.setString(++i, emailAddressDTO.getLastName());
			ps.setString(++i, emailAddressDTO.getEmailAddress());
			insertCount = ps.executeUpdate();
		}
	 catch (SQLException e) {
		 e.printStackTrace();
		 logger.debug("Is it true? "+(e instanceof SQLIntegrityConstraintViolationException));
		    if (e instanceof SQLIntegrityConstraintViolationException) {
		        throw e;
		    } else {
		    	logger.error("Unable to add email address ",e);
		    }
		}
		catch (Exception e) {
			logger.error("Unable to add list ",e);
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
//					logger.debug("DB-POOL Close connection from getLastVerifedDate ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Error in finally section ",e);
			}
		}
		return insertCount;

	}
	
	@Override
	public int updateEmailAddressDetails(EmailAddressDTO emailAddressDTO) {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		int updateCount = 0;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			int i = 0;
			ps = mysqlConn.prepareStatement("update emailsSubscriptionList set firstName= ?, lastName = ?, emailAddress = ? where id = ?");
			ps.setString(++i, emailAddressDTO.getFirstName());
			ps.setString(++i, emailAddressDTO.getLastName());
			ps.setString(++i, emailAddressDTO.getEmailAddress());
			ps.setInt(++i, emailAddressDTO.getId());
			updateCount = ps.executeUpdate();
		}
		catch (Exception e) {
			logger.error("Unable to get emails list ",e);
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
//					logger.debug("DB-POOL Close connection from getLastVerifedDate ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Error in finally section ",e);
			}
		}
		return updateCount;

	}
	@Override
	public EmailSettingsDTO getEmailSettings() {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		EmailSettingsDTO emailSettingsDTO = new EmailSettingsDTO();
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			ps = mysqlConn.prepareStatement("select * from emailSettings");
			rs = ps.executeQuery();
			if (rs.next())
			{
				if (rs.getInt("enableEmail") > 0)
				{
					emailSettingsDTO.setEnableEmail(true);
				}
				else
				{
					emailSettingsDTO.setEnableEmail(false);
				}
				emailSettingsDTO.setEmailServerHost(rs.getString("emailServerHost"));
				emailSettingsDTO.setEmailSmtpPort(""+rs.getInt("emailSmtpPort"));
				emailSettingsDTO.setFromEmail(rs.getString("fromEmail"));
				emailSettingsDTO.setEmailServerPassword(rs.getString("emailServerPassword"));
			}
		}
		catch (Exception e) {
			logger.error("Unable to get emails list ",e);
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
//					logger.debug("DB-POOL Close connection from getLastVerifedDate ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Error in finally section ",e);
			}
		}
		logger.debug("Email settings to be returned "+emailSettingsDTO);
		return emailSettingsDTO;
	}
	@Override
	public int updateEmailSettings(EmailSettingsDTO emailSettingsDTO) {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		int updateCount = 0;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			ps = mysqlConn.prepareStatement("insert into emailSettings set enableEmail=?, emailServerHost=?,emailSmtpPort=?,fromEmail=?,emailServerPassword=? on duplicate key update enableEmail=values(enableEmail)," + 
					" emailServerHost=values(emailServerHost),emailServerHost=values(emailServerHost),emailSmtpPort=values(emailSmtpPort),fromEmail=values(fromEmail),emailServerPassword=values(emailServerPassword)");
			int i = 0;
			if (emailSettingsDTO.isEnableEmail())
			{
				ps.setInt(++i, M9kConstants.TRUE);
			}
			else
			{
				ps.setInt(++i, M9kConstants.FALSE);
			}
			ps.setString(++i, emailSettingsDTO.getEmailServerHost());
			ps.setInt(++i, Integer.parseInt(emailSettingsDTO.getEmailSmtpPort()));
			ps.setString(++i, emailSettingsDTO.getFromEmail());
			ps.setString(++i, emailSettingsDTO.getEmailServerPassword());
			updateCount = ps.executeUpdate();
			if (updateCount == 0)
			{
				logger.error("Email settings couldn't be updated");
			}
		}
		catch (Exception e) {
			logger.error("Unable to get emails list ",e);
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
//					logger.debug("DB-POOL Close connection from getLastVerifedDate ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Error in finally section ",e);
			}
		}
		return updateCount;
	}
	@Override
	public EmailReportsSettingsDTO getEmailReportsSettings() {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		EmailReportsSettingsDTO emailReportsSettingsDTO = new EmailReportsSettingsDTO();
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			ps = mysqlConn.prepareStatement("select * from emailReportsSettings");
			rs = ps.executeQuery();
			if (rs.next())
			{
				emailReportsSettingsDTO.setId(rs.getInt("id"));
				if (rs.getInt("enableDailyStatusEmails") > 0)
				{
					emailReportsSettingsDTO.setEnableDailyStatusEmails(true);
				}
				else
				{
					emailReportsSettingsDTO.setEnableDailyStatusEmails(false);
				}
				emailReportsSettingsDTO.setDailyStatusReportTitle(rs.getString("dailyStatusReportTitle"));
				emailReportsSettingsDTO.setStationSpecificStatusReportTitle(rs.getString("stationSpecificStatusReportTitle"));
				emailReportsSettingsDTO.setDailyStatusRepeatInterval(rs.getInt("dailyStatusRepeatInterval"));
				emailReportsSettingsDTO.setDailyStatusReportTime(rs.getString("dailyStatusReportTime"));
				if (rs.getInt("enableSerEmail") > 0)
				{
					emailReportsSettingsDTO.setEnableSerEmail(true);
				}
				else
				{
					emailReportsSettingsDTO.setEnableSerEmail(false);
				}
				emailReportsSettingsDTO.setSerRunFrequency(rs.getInt("serRunFrequency"));
				emailReportsSettingsDTO.setSerEmailFileType(rs.getString("serEmailFileType"));
				emailReportsSettingsDTO.setSerEmailAttachementSizeLimit(""+rs.getInt("serEmailAttachementSizeLimit"));
				if (rs.getInt("enableFaultEmail") > 0)
				{
					emailReportsSettingsDTO.setEnableFaultEmail(true);
				}
				else
				{
					emailReportsSettingsDTO.setEnableFaultEmail(false);
				}
				if (rs.getInt("enableFaultsBooleanLogicFilter") > 0)
				{
					emailReportsSettingsDTO.setEnableFaultsBooleanLogicFilter(true);
				}
				else
				{
					emailReportsSettingsDTO.setEnableFaultsBooleanLogicFilter(false);
				}
				if (rs.getInt("enableFaultsWithAttachment") > 0)
				{
					emailReportsSettingsDTO.setEnableFaultsWithAttachment(true);
				}
				else
				{
					emailReportsSettingsDTO.setEnableFaultsWithAttachment(false);
				}
				emailReportsSettingsDTO.setFaultEmailAttachementSizeLimit(""+rs.getInt("faultEmailAttachementSizeLimit"));
				emailReportsSettingsDTO.setFaultsEmailDailyLimit(rs.getInt("faultsEmailDailyLimit"));
				if (rs.getInt("masterHealthStatusPoll") > 0)
				{
					emailReportsSettingsDTO.setMasterHealthStatusPoll(true);
				}
				else
				{
					emailReportsSettingsDTO.setMasterHealthStatusPoll(false);
				}
				emailReportsSettingsDTO.setFaultsEmailDailyLimit(rs.getInt("faultsEmailDailyLimit"));
				if (rs.getInt("masterHealthStatusPoll") > 0)
				{
					emailReportsSettingsDTO.setMasterHealthStatusPoll(true);
				}
				else
				{
					emailReportsSettingsDTO.setMasterHealthStatusPoll(false);
				}
				
				// 12-Nov-2021 - Config change email setting
				if (rs.getInt("enableConfigChangeEmail") > 0)
				{
					emailReportsSettingsDTO.setEnableConfigChangeEmail(true);
				}
				else
				{
					emailReportsSettingsDTO.setEnableConfigChangeEmail(false);
				}

			}
		}
		catch (Exception e) {
			logger.error("Unable to get emails list ",e);
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
//					logger.debug("DB-POOL Close connection from getLastVerifedDate ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Error in finally section ",e);
			}
		}
		return emailReportsSettingsDTO;
	}
	@Override
	public int updateEmailReportsSettings(EmailReportsSettingsDTO emailReportsSettingsDTO) {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		int updateCount = 0;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			ps = mysqlConn.prepareStatement("insert into emailReportsSettings set enableDailyStatusEmails=?, dailyStatusReportTitle=?, stationSpecificStatusReportTitle=?,dailyStatusRepeatInterval=?,dailyStatusReportTime=?,"
					+ "enableSerEmail=?,serRunFrequency=?,serEmailFileType=?,serEmailAttachementSizeLimit=?,enableFaultEmail=?,enableFaultsBooleanLogicFilter=?,"
					+ "enableFaultsWithAttachment=?,faultEmailAttachementSizeLimit=?,faultsEmailDailyLimit=?,masterHealthStatusPoll=?,masterHealthStatusPollingFrequency=?,masterNotificationListener=?,enableConfigChangeEmail=? on duplicate key update enableDailyStatusEmails=values(enableDailyStatusEmails)," + 
					" dailyStatusReportTitle=values(dailyStatusReportTitle),stationSpecificStatusReportTitle=values(stationSpecificStatusReportTitle),dailyStatusRepeatInterval=values(dailyStatusRepeatInterval),dailyStatusReportTime=values(dailyStatusReportTime),"
					+ "enableSerEmail=values(enableSerEmail),serRunFrequency=values(serRunFrequency),serEmailFileType=values(serEmailFileType),"
					+ "serEmailAttachementSizeLimit=values(serEmailAttachementSizeLimit),enableFaultEmail=values(enableFaultEmail),"
					+ "enableFaultsBooleanLogicFilter=values(enableFaultsBooleanLogicFilter),enableFaultsWithAttachment=values(enableFaultsWithAttachment),"
					+ "faultEmailAttachementSizeLimit=values(faultEmailAttachementSizeLimit),faultsEmailDailyLimit=values(faultsEmailDailyLimit),"
					+ "masterHealthStatusPoll=values(masterHealthStatusPoll),masterHealthStatusPollingFrequency=values(masterHealthStatusPollingFrequency),masterNotificationListener=values(masterNotificationListener),enableConfigChangeEmail=values(enableConfigChangeEmail)");
			int i = 0;
			if (emailReportsSettingsDTO.isEnableDailyStatusEmails())
			{
				ps.setInt(++i, M9kConstants.TRUE);
			}
			else
			{
				ps.setInt(++i, M9kConstants.FALSE);
			}
			ps.setString(++i, emailReportsSettingsDTO.getDailyStatusReportTitle());
			ps.setString(++i, emailReportsSettingsDTO.getStationSpecificStatusReportTitle());
			ps.setInt(++i, emailReportsSettingsDTO.getDailyStatusRepeatInterval());
			ps.setString(++i, emailReportsSettingsDTO.getDailyStatusReportTime());
			if (emailReportsSettingsDTO.isEnableSerEmail())
			{
				ps.setInt(++i, M9kConstants.TRUE);
			}
			else
			{
				ps.setInt(++i, M9kConstants.FALSE);
			}
			ps.setInt(++i, emailReportsSettingsDTO.getSerRunFrequency());
			ps.setString(++i, emailReportsSettingsDTO.getSerEmailFileType());
			ps.setInt(++i, Integer.parseInt(emailReportsSettingsDTO.getSerEmailAttachementSizeLimit()));
			if (emailReportsSettingsDTO.isEnableFaultEmail())
			{
				ps.setInt(++i, M9kConstants.TRUE);
			}
			else
			{
				ps.setInt(++i, M9kConstants.FALSE);
			}
			if (emailReportsSettingsDTO.isEnableFaultsBooleanLogicFilter())
			{
				ps.setInt(++i, M9kConstants.TRUE);
			}
			else
			{
				ps.setInt(++i, M9kConstants.FALSE);
			}
			if (emailReportsSettingsDTO.isEnableFaultsWithAttachment())
			{
				ps.setInt(++i, M9kConstants.TRUE);
			}
			else
			{
				ps.setInt(++i, M9kConstants.FALSE);
			}
			ps.setInt(++i, Integer.parseInt(emailReportsSettingsDTO.getFaultEmailAttachementSizeLimit()));
			ps.setInt(++i, emailReportsSettingsDTO.getFaultsEmailDailyLimit());
			if (emailReportsSettingsDTO.isMasterHealthStatusPoll())
			{
				ps.setInt(++i, M9kConstants.TRUE);
			}
			else
			{
				ps.setInt(++i, M9kConstants.FALSE);
			}
			ps.setInt(++i, emailReportsSettingsDTO.getMasterHealthStatusPollingFrequency());
			if (emailReportsSettingsDTO.isMasterNotificationListener())
			{
				ps.setInt(++i, M9kConstants.TRUE);
			}
			else
			{
				ps.setInt(++i, M9kConstants.FALSE);
			}
			// 12-Nov-2021 - Config change email
			if (emailReportsSettingsDTO.isEnableConfigChangeEmail())
			{
				ps.setInt(++i, M9kConstants.TRUE);
			}
			else
			{
				ps.setInt(++i, M9kConstants.FALSE);
			}

			updateCount = ps.executeUpdate();
			if (updateCount == 0)
			{
				logger.error("Email reports settings couldn't be updated");
			}
		}
		catch (Exception e) {
			logger.error("Unable to get emails list ",e);
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
//					logger.debug("DB-POOL Close connection from getLastVerifedDate ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Error in finally section ",e);
			}
		}
		return updateCount;

	}

}
