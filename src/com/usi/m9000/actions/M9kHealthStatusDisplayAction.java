package com.usi.m9000.actions;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.SystemDocument.System;
import com.usi.health.SubStationDocument;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.util.M9kMessagesUtil;
import com.usi.m9000.xml.M9000XmlConfig;

public class M9kHealthStatusDisplayAction extends ActionSupport implements SessionAware, ServletContextListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Map<String, Object> session;
	String stationHealthStatus;
	StationDTO stationDetails;
	XmlOptions xmlOptions;
	com.usi.health.SubStationDocument.SubStation substationHlth;
	int currentConfigSerialNumber = 0;
    System systemDetails = null;

	M9000XmlConfig m9kConfig;
	
	UsersDTO userDto;
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kHealthStatusDisplayAction.class);
	/**
	 * 
	 */
	public M9kHealthStatusDisplayAction()
	{
		xmlOptions = new XmlOptions();
		xmlOptions.setLoadUseDefaultResolver();
		xmlOptions.setUseDefaultNamespace();
		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("", "http://www.usi.com/health");
		xmlOptions.setLoadSubstituteNamespaces(prefixes);
	}

	  public String execute()throws Exception{
		  String returnStatus = SUCCESS;
		  logger.debug("Entered execute method..."+getStationDetails());
		  logger.info("User "+userDto.getUserName() +" is requesting health status for Heath Monitor");

//		  logger.info("System details "+systemDetails);
		  return returnStatus;
	  }

	@SkipValidation
	public String getHealthStatus()
	  {
		
		  String returnValue = SUCCESS;
		  StationDTO stationDetails = (StationDTO)session.get("stationDetails");
		  stationHealthStatus = M9kMessagesUtil.sendSynchMessage(""+stationDetails.getSystemStationId(), "MONITOR", "MONITOR");
		  try {
			  if (!stationHealthStatus.toUpperCase().startsWith("ERROR"))
			  {
				  // START: version 1.2 Explicit cast after it failed due to updated xmlbeans package
				SubStationDocument subStationDoc = (SubStationDocument) SubStationDocument.Factory.parse(stationHealthStatus, xmlOptions);
				substationHlth = subStationDoc.getSubStation();
			  }
			  else
			  {
				  substationHlth = null; 
			  }
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  return returnValue;
	  }

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		stationDetails = (StationDTO) session.get("stationDetails");
		userDto = (UsersDTO) session.get("userDetails");
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
//		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//	    LogFactory.release(contextClassLoader);
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public String getStationHealthStatus() {
		return stationHealthStatus;
	}

	public void setStationHealthStatus(String stationHealthStatus) {
		this.stationHealthStatus = stationHealthStatus;
	}

	public com.usi.health.SubStationDocument.SubStation getSubstationHlth() {
		return substationHlth;
	}

	public void setSubstationHlth(
			com.usi.health.SubStationDocument.SubStation substationHlth) {
		this.substationHlth = substationHlth;
	}

	public StationDTO getStationDetails() {
		return stationDetails;
	}

	public void setStationDetails(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
	}

	/**
	 * @return the currentConfigSerialNumber
	 */
	public int getCurrentConfigSerialNumber() {

		if (systemDetails == null)
		{
			try {
				if (getStationDetails() != null && getStationDetails().getConfigXml() != null)
				{
					m9kConfig = new M9000XmlConfig(new StringReader(getStationDetails().getConfigXml()));
					systemDetails = m9kConfig.getSubstation().getDFRs().getDFRArray(0).getSystem();
				}
			} catch (M9000Exception e) {
				logger.warn("Exception reading configSerialNumber from stationDetails. Defaulting it to 0.",e);
			}
		}

		logger.debug("System details "+systemDetails);
		if (systemDetails!= null && systemDetails.isSetConfigSerialNumber())
		{
			currentConfigSerialNumber = systemDetails.getConfigSerialNumber();
		}
		logger.debug("Current configSerialNumber "+currentConfigSerialNumber);
		return currentConfigSerialNumber;
	}

	/**
	 * @param currentConfigSerialNumber the currentConfigSerialNumber to set
	 */
	public void setCurrentConfigSerialNumber(int currentConfigSerialNumber) {
		this.currentConfigSerialNumber = currentConfigSerialNumber;
	}

	/**
	 * @return the userDto
	 */
	public UsersDTO getUserDto() {
		return userDto;
	}

	/**
	 * @param userDto the userDto to set
	 */
	public void setUserDto(UsersDTO userDto) {
		this.userDto = userDto;
	}


}
