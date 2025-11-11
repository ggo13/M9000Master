/**
 * 
 */
package com.usi.m9000.actions;

import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.dto.UsersDTO;

/**
 * @author sramasamy
 *
 */
public class M9kJSONSessionCheck extends ActionSupport implements SessionAware,
		ServletContextListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2634964690478153432L;
	private Map<String, Object> session;
	PropertiesConfiguration config;

	private String currentSessionStatus = "active";
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kJSONSessionCheck.class);

	
	public M9kJSONSessionCheck() {
		
	}

		@SkipValidation
		  public String getSessionStatus()
		  {
			logger.debug("Entered getSessionStatus...session ");
		  if (session != null)
		  {
				UsersDTO userDetails = (UsersDTO) session.get("userDetails");
				  logger.debug("Entered getSessionStatus...userDetails "+userDetails);
				if (userDetails!=null )
				{
					setCurrentSessionStatus("active");
				}
				else
				{
					setCurrentSessionStatus("expired");
				}
		  }
		  else
		  {
			  setCurrentSessionStatus("expired");
		  }
//			  logger.debug("LIst Export date  rate "+lstExportRate);
			  return SUCCESS;
		  }	  
		
		
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.struts2.interceptor.SessionAware#setSession(java.util.Map)
	 */
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		config = (PropertiesConfiguration) this.session.get("config");

	}

	/**
	 * @return the currentSessionStatus
	 */
	public String getCurrentSessionStatus() {
		return currentSessionStatus;
	}

	/**
	 * @param currentSessionStatus the currentSessionStatus to set
	 */
	public void setCurrentSessionStatus(String currentSessionStatus) {
		this.currentSessionStatus = currentSessionStatus;
	}
	


}
