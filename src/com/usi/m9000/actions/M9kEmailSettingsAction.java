package com.usi.m9000.actions;

import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.Master.M9kReportListener;
import com.usi.m9000.dao.EmailDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.EmailAddressDTO;
import com.usi.m9000.dto.EmailReportsSettingsDTO;
import com.usi.m9000.dto.EmailSettingsDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class M9kEmailSettingsAction extends ActionSupport implements SessionAware, ServletContextListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Object> session;
    private M9kDAOFactory m9kDAOFactory;
    private EmailDAO emailDAO;
    private EmailReportsSettingsDTO emailReportsSettingsDTO;
    private EmailSettingsDTO emailSettingsDTO;
    private EmailAddressDTO emailAddressDTO;
	private UsersDTO userDto;
    
    static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kEmailSettingsAction.class);

    public M9kEmailSettingsAction() {
    	m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
    	emailDAO = m9kDAOFactory.getEmailDAO();
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
        userDto = (UsersDTO) session.get("userDetails");
    }

    @Override
    @Action(value = "displayEmailSettings", results = {
    @Result(name="success",location="/jsp/emailSettingsTab.jsp")})
    @SkipValidation
    public String execute() throws Exception {
        return SUCCESS;
    }

	
    @Action(value = "displayEmailProperties", results = {
	@Result(name="success",location="/jsp/emailConfiguration.jsp")})
    @SkipValidation
    public String getEmailProperties()
    {
    	emailSettingsDTO = emailDAO.getEmailSettings();
    	logger.debug("In getEmailProperties email settings "+emailSettingsDTO);
    	emailReportsSettingsDTO = emailDAO.getEmailReportsSettings();
    	return SUCCESS;
    }
    
    @Action(value = "saveEmailConfigurations", interceptorRefs = {@InterceptorRef("jsonValidationWorkflowStack"),@InterceptorRef("defaultStack"),@InterceptorRef("exceptionMappingStack")}, results = {
            @Result(name = SUCCESS, type = "json", params = {"includeProperties", "actionMessages.*,emailSettingsDTO.*,emailReportsSettingsDTO.*",
                    "ignoreHierarchy", "false"}),
            @Result(name = ERROR, type = "json", params = {"includeProperties", "actionErrors.*", "ignoreHierarchy",
                    "false", "statusCode", "500"})})
    public String saveEmailProperties()
    {
    	logger.info("User "+userDto.getUserName()+" saving email settings changes.");
    	emailDAO.updateEmailSettings(getEmailSettingsDTO());
    	emailDAO.updateEmailReportsSettings(getEmailReportsSettingsDTO());
    	// START: 23-Jul-2024 - after saving email settings, reschedule run at hour task
    	logger.debug("Email settings updated successfully. About reschedule run at hour task.");
    	new M9kReportListener().reScheduleRunAtHour();
    	logger.debug("Reschedule run at hour task completed.");
    	// END: 23-Jul-2024 - after saving email settings, reschedule run at hour task
    	return SUCCESS;
    }
    
	public EmailReportsSettingsDTO getEmailReportsSettingsDTO() {
		return emailReportsSettingsDTO;
	}

	public void setEmailReportsSettingsDTO(EmailReportsSettingsDTO emailReportsSettingsDTO) {
		this.emailReportsSettingsDTO = emailReportsSettingsDTO;
	}

	public EmailSettingsDTO getEmailSettingsDTO() {
		return emailSettingsDTO;
	}

	public void setEmailSettingsDTO(EmailSettingsDTO emailSettingsDTO) {
		this.emailSettingsDTO = emailSettingsDTO;
	}

	public EmailAddressDTO getEmailAddressDTO() {
		return emailAddressDTO;
	}

	public void setEmailAddressDTO(EmailAddressDTO emailAddressDTO) {
		this.emailAddressDTO = emailAddressDTO;
	}

	public void validate()
	{
		if (emailSettingsDTO.isEnableEmail())
		{
			if (emailSettingsDTO.getEmailServerHost() == null || emailSettingsDTO.getEmailServerHost().isEmpty())
			{
				addFieldError("emailSettingsDTO.emailServerHost", "Email Server Host is Required");
			}
			if (emailSettingsDTO.getEmailSmtpPort() == null|| emailSettingsDTO.getEmailSmtpPort().isEmpty())
			{
				addFieldError("emailSettingsDTO.emailSmtpPort", "Email smtp Port number is required");
			}
			else if(!M9kUtils.isInteger(emailSettingsDTO.getEmailSmtpPort()))
			{
				addFieldError("emailSettingsDTO.emailSmtpPort", "Email smtp port should be numeric");
			}
			else if (Integer.parseInt(emailSettingsDTO.getEmailSmtpPort()) <=0 || Integer.parseInt(emailSettingsDTO.getEmailSmtpPort()) > 65535)
			{
				addFieldError("emailSettingsDTO.emailSmtpPort", "Email smtp Port number should be between 0 and 65535");
			}
			if (emailSettingsDTO.getFromEmail() == null || emailSettingsDTO.getFromEmail().isEmpty())
			{
				addFieldError("emailSettingsDTO.fromEmail", "From email address is required");
			}
			else if (!M9kUtils.isValidEmailAddress(emailSettingsDTO.getFromEmail()))
			{
				addFieldError("emailSettingsDTO.fromEmail", "Enter a valid email address");
			}
		}
		if (emailReportsSettingsDTO.isEnableDailyStatusEmails())
		{
			if (emailReportsSettingsDTO.getDailyStatusReportTitle() == null || emailReportsSettingsDTO.getDailyStatusReportTitle().isEmpty())
			{
				addFieldError("emailReportsSettingsDTO.dailyStatusReportTitle", "Report title is required");
			}
			if (emailReportsSettingsDTO.getStationSpecificStatusReportTitle() == null || emailReportsSettingsDTO.getStationSpecificStatusReportTitle().isEmpty())
			{
				addFieldError("emailReportsSettingsDTO.stationSpecificStatusReportTitle", "Station specific alarm report title is required");
			}
			if (emailReportsSettingsDTO.getDailyStatusRepeatInterval() <= 0 )
			{
				addFieldError("emailReportsSettingsDTO.dailyStatusRepeatInterval", "Run Frequency should be greater than zero");
			}
		}
		if (emailReportsSettingsDTO.isEnableSerEmail())
		{
			if (emailReportsSettingsDTO.getSerEmailAttachementSizeLimit() == null|| emailReportsSettingsDTO.getSerEmailAttachementSizeLimit().isEmpty())
			{
				addFieldError("emailReportsSettingsDTO.serEmailAttachementSizeLimit", "Email attachment size is required");
			}
			else if(!M9kUtils.isInteger(emailReportsSettingsDTO.getSerEmailAttachementSizeLimit()))
			{
				addFieldError("emailReportsSettingsDTO.serEmailAttachementSizeLimit", "Email attachment size should be numeric");
			}
			else if (Integer.parseInt(emailReportsSettingsDTO.getSerEmailAttachementSizeLimit()) <= 0 || Integer.parseInt(emailReportsSettingsDTO.getSerEmailAttachementSizeLimit()) > 10)
			{
				addFieldError("emailReportsSettingsDTO.serEmailAttachementSizeLimit", "Size in mb should be between 0 and 10");
			}
			// SER run frequency validation
			if (emailReportsSettingsDTO.getSerRunFrequency() <= 0 )
			{
				addFieldError("emailReportsSettingsDTO.serRunFrequency", "Run Frequency should be greater than zero");
			}
		}
		if (emailReportsSettingsDTO.isEnableFaultEmail())
		{
			if (emailReportsSettingsDTO.getFaultEmailAttachementSizeLimit() == null|| emailReportsSettingsDTO.getFaultEmailAttachementSizeLimit().isEmpty())
			{
				addFieldError("emailReportsSettingsDTO.faultEmailAttachementSizeLimit", "Email attachment size is required");
			}
			else if(!M9kUtils.isInteger(emailReportsSettingsDTO.getFaultEmailAttachementSizeLimit()))
			{
				addFieldError("emailReportsSettingsDTO.faultEmailAttachementSizeLimit", "Email attachment size should be numeric");
			}
			else if (Integer.parseInt(emailReportsSettingsDTO.getFaultEmailAttachementSizeLimit()) <= 0 || Integer.parseInt(emailReportsSettingsDTO.getFaultEmailAttachementSizeLimit()) > 10)
			{
				addFieldError("emailReportsSettingsDTO.faultEmailAttachementSizeLimit", "Size in mb should be between 0 and 10");
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
