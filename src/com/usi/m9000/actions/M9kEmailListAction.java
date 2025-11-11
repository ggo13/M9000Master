package com.usi.m9000.actions;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.dao.EmailDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.EmailAddressDTO;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class M9kEmailListAction extends ActionSupport implements SessionAware, ServletContextListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Object> session;
    private M9kDAOFactory m9kDAOFactory;
    private EmailDAO emailDAO;
    private List<EmailAddressDTO> lstEmailAddresses;
    private EmailAddressDTO emailAddressDTO;
    
    static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kEmailListAction.class);

    public M9kEmailListAction() {
    	m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
    	emailDAO = m9kDAOFactory.getEmailDAO();
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    @Override
    @Action(value = "displayEmailSettings", results = {
    @Result(name="success",location="/jsp/emailSettingsTab.jsp")})
    @SkipValidation
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Action(value = "displayEmailsList", results = {
    @Result(name="success",location="/jsp/emailsList.jsp")})
    @SkipValidation
    public String displayEmailsList() throws Exception {
        return SUCCESS;
    }
    
    @Actions({@Action(value = "emailsList-json", results = {
            @Result(type = "json", params = {"includeProperties", "lstEmailAddresses.*"})}
    )
    })
    @SkipValidation
    public String getAllEmailList()
    {
        lstEmailAddresses = emailDAO.getEmailsList();
    	return SUCCESS;
    }
    
    @Action(value = "saveEmailDetails", interceptorRefs = {@InterceptorRef("jsonValidationWorkflowStack"),@InterceptorRef("defaultStack"),@InterceptorRef("exceptionMappingStack")}, results = {
            @Result(name = SUCCESS, type = "json", params = {"includeProperties", "actionMessages.*,lstEmailAddresses.*",
                    "ignoreHierarchy", "false"}),
            @Result(name = ERROR, type = "json", params = {"includeProperties", "actionErrors.*", "ignoreHierarchy",
                    "false", "statusCode", "500"})})
    public String saveEmailDetails()
    {
    	String resultString = SUCCESS;
    	
    	try {
            logger.debug("Email address to be saved..."+getEmailAddressDTO());
            if (getEmailAddressDTO().getId() > 0) {
                int updatedCount = emailDAO.updateEmailAddressDetails(getEmailAddressDTO());
                if (updatedCount == 0)
                {
                	logger.error("No Email Details Were Updated");
                	resultString = ERROR;
                    this.addActionError("No Email Details Were Updated");
                }
                else
                {
                	logger.info("Email details updated for "+getEmailAddressDTO());
                }
            } else {
           		emailDAO.addEmailAddress(getEmailAddressDTO());
            	logger.info("New Email details Added "+getEmailAddressDTO());
            }
        }
    	catch (SQLException e) {
        	resultString = ERROR;
		    if (e instanceof SQLIntegrityConstraintViolationException) {
		    	this.addActionError("Email Address is already subscribed");
		    } else {
		    	this.addActionError("Unable to add email address "+e.getMessage());
		    }
		}

    	catch (Exception e) {
        	resultString = ERROR;
            this.addActionError("Error while saving email : " + e.getMessage());
        }
    	return resultString;
    }
    
    @Action(value = "deleteEmail", interceptorRefs = {@InterceptorRef("exceptionMappingStack")}, results = {
            @Result(name = SUCCESS, type = "json", params = {"includeProperties", "actionMessages.*",
                    "ignoreHierarchy", "false"}),
            @Result(name = ERROR, type = "json", params = {"includeProperties", "actionErrors.*", "ignoreHierarchy",
                    "false", "statusCode", "500"})})
    @SkipValidation
    public String deleteEmail() throws Exception {
        String resultString = SUCCESS;
        try {
            logger.debug("Delete email " + getEmailAddressDTO());
            int deleteCount = emailDAO.removeEmailAddress(getEmailAddressDTO());
            if (deleteCount == 0)
            {
            	logger.error("No Email Address Was Deleted "+getEmailAddressDTO());
            	resultString = ERROR;
                this.addActionError("No Email Address Was Deleted");
            }
        } catch (Exception e) {
        	resultString = ERROR;
            this.addActionError("Error while deleting email : "+getEmailAddressDTO() + e.getMessage());
        }
        return resultString;
    }
	

	public List<EmailAddressDTO> getLstEmailAddresses() {
		return lstEmailAddresses;
	}

	public void setLstEmailAddresses(List<EmailAddressDTO> lstEmailAddresses) {
		this.lstEmailAddresses = lstEmailAddresses;
	}

	public EmailAddressDTO getEmailAddressDTO() {
		return emailAddressDTO;
	}

	public void setEmailAddressDTO(EmailAddressDTO emailAddressDTO) {
		this.emailAddressDTO = emailAddressDTO;
	}

	public void validate()
	{
		if (emailAddressDTO.getFirstName() == null || emailAddressDTO.getFirstName().isEmpty())
		{
			addFieldError("emailAddressDTO.firstName", "First Name is Required");
		}
		if (emailAddressDTO.getLastName() == null || emailAddressDTO.getLastName().isEmpty())
		{
			addFieldError("emailAddressDTO.lastName", "Last Name is Required");
		}
		if (emailAddressDTO.getEmailAddress() == null || emailAddressDTO.getEmailAddress().isEmpty())
		{
			addFieldError("emailAddressDTO.emailAddress", "Email Address is Required");
		}
		else if (!M9kUtils.isValidEmailAddress(emailAddressDTO.getEmailAddress() ))
		{
			addFieldError("emailAddressDTO.emailAddress", "Enter a valid Email Address");
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
