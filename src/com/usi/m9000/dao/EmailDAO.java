package com.usi.m9000.dao;

import java.sql.SQLException;
import java.util.List;

import com.usi.m9000.dto.EmailAddressDTO;
import com.usi.m9000.dto.EmailReportsSettingsDTO;
import com.usi.m9000.dto.EmailSettingsDTO;



public interface EmailDAO {
	public List<EmailAddressDTO> getEmailsList();
	public int removeEmailAddress(EmailAddressDTO emailAddressDTO);
	public int addEmailAddress(EmailAddressDTO emailAddressDTO) throws SQLException;
	public int updateEmailAddressDetails(EmailAddressDTO emailAddressDTO);
	
	public EmailSettingsDTO getEmailSettings();
	public int updateEmailSettings(EmailSettingsDTO emailSettingsDTO);
	
	public EmailReportsSettingsDTO getEmailReportsSettings();
	public int updateEmailReportsSettings(EmailReportsSettingsDTO emailReportsSettingsDTO);
}
