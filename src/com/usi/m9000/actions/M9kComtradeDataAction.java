package com.usi.m9000.actions;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.util.M9kMessagesUtil;
import com.usi.m9000.util.M9kUtils;

public class M9kComtradeDataAction extends ActionSupport implements SessionAware, ServletContextListener, ServletRequestAware,  ServletResponseAware{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ComtradeDataDTO> lstComtradeDto;
	private Map<String, Object> session;
	PropertiesConfiguration config;
	PropertiesConfiguration cfgComtrade;
	List<DfrDTO> lstDfrDTO;
	private List<StationDTO> lstAvailableStations;
	private StationDTO stationDetails;
	private String stationId;
	private String errorMessage;
	private String infoMessage;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kComtradeDataAction.class);
	protected HttpServletRequest httpServletRequest;
	protected HttpServletResponse httpServletResponse;
	UsersDTO userDto;
	private String fileName;
	private int contAnalogTimeLimit = 120; // In seconds. Defaults to 120 seconds
	private int contDataTimeLimit = 600; // In seconds. Defaults to 600 seconds

	/**
	 * 
	 */
	public M9kComtradeDataAction() {
	}
	
	@SuppressWarnings("unchecked")
	@SkipValidation
	public String getQuickSummary() throws M9000Exception
	{
		logger.debug("Entered Quick Summary in M9kCotmtradeDataAction "+getFileName());
		
//		MySqlComtradeDetailsDAO comtradeDetailsDao = new MySqlComtradeDetailsDAO();
//		//TODO: Station id
//		lstComtradeDto = comtradeDetailsDao.getLstOfComtradeDetails(18);
//		logger.debug("Size of list of quick summary..."+lstComtradeDto.size());
		  lstAvailableStations = (List<StationDTO>) session.get("lstAvailableStations");
		  logger.debug("Station id from getQuickSumary() "+getStationId());
		  if (getStationId() == null || !M9kUtils.isInteger(getStationId()))
		  {
			  logger.info("No Stations Matching Filter Criteria.");
			  return "NoStations";
		  }
		  
//		  if (getStationIndex() != null)
//		  {
//			  stationDetails = lstAvailableStations.get(getStationIndex()-1);
//				logger.debug("station id in the M9kComtradeDataAction..."+stationDetails.getSystemStationId());
//				session.put("stationId", stationDetails.getSystemStationId());
//				  session.put("stationDetails", stationDetails);
//		  }
//		  else 
//		  {
		// START: 06-Mar-2015 Fetch station details from db instead of memory
//			  stationDetails = getStationDetails(Integer.parseInt(getStationId()));
		  try {
//			stationDetails = stationDao.getStationsDetails(Integer.parseInt(getStationId()));
			M9kUtils.setStationId(Integer.parseInt(getStationId()));
			stationDetails = M9kUtils.getStationDetails();
//			logger.info("stationDetails from QuickSummary "+stationDetails);
			if (userDto != null){
				Cookie userCookie = new Cookie(userDto.getUserName(), getStationId());
	            userCookie.setMaxAge(60 * 60 * 24 * 365); // Valid for a year
	            userCookie.setPath("/");
	            httpServletResponse.addCookie(userCookie);
			}
			contAnalogTimeLimit = M9kUtils.getContAnalogTimeLimit();
			contDataTimeLimit = M9kUtils.getContDataTimeLimit();
		} catch (NumberFormatException e) {
			logger.error("Exception in fetching station details from database",e);
			throw new M9000Exception(e);
		} catch (Exception e) {
			logger.error("Exception in fetching station details from database",e);
			throw new M9000Exception(e);
			} 
		// END: 06-Mar-2015 
//		  logger.info("Station id updated into session "+getStationId());
			  session.put("stationId", getStationId());
			  session.put("stationDetails", stationDetails);
			  session.remove("lstEventChannels");
			  session.remove("lstLineGroups");
//			  logger.info("station id read from session "+session.get("stationId"));
//		  }
			  logger.debug("Returning from getQuickSummary method "+SUCCESS);
			  logger.info("User "+userDto.getUserName()+" is now viewing station "+stationDetails.getStationDisplayName());
		return SUCCESS;
	}

	
	@SuppressWarnings("unchecked")
	public String updateQuickSummary()
	{
		stationId = (String) session.get("stationId");
		stationDetails = (StationDTO)session.get("stationDetails");
		lstAvailableStations = (List<StationDTO>) session.get("lstAvailableStations");
		logger.debug("MESSAGE Q: MEssage queue to be sent to "+stationDetails.getSystemStationName());
		String response = M9kMessagesUtil.sendSynchMessage(""+stationDetails.getSystemStationId(), "Test Trigger", "TRIGGER");
		if (response != null && response.equalsIgnoreCase("SUCCESSFUL"))
		{
			addActionMessage("Successfully triggered");	
			setInfoMessage("Successfully triggered");
			logger.info("User "+userDto.getUserName()+" successfully triggered using Trigger Now for station "+stationDetails.getStationDisplayName());
		}
		else
		{
			addActionError("Unable to trigger. Maybe all chassis are down. Please check Station Master Error Logs.");
			setErrorMessage("Unable to trigger. Maybe all chassis are down. Please check Station Master Error Logs.");
			logger.info("User "+userDto.getUserName()+" attempt to 'Trigger Now' was not successful for station "+stationDetails.getStationDisplayName());
		}
		
		logger.debug("updateQuickSummary(): Station Id in the session... "+session.get("stationId"));
		
//		logger.debug("Available stations... "+session.get("lstAvailableStations"));
		return "UPDATE_FAULTS";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		config = (PropertiesConfiguration) session.get("config");
		cfgComtrade = (PropertiesConfiguration) session.get("cfgComtrade");
		lstDfrDTO = (List<DfrDTO>) session.get("DFRsList");
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
	/**
	 * @return the lstComtradeDto
	 */
	public List<ComtradeDataDTO> getLstComtradeDto() {
		return lstComtradeDto;
	}
	/**
	 * @param lstComtradeDto the lstComtradeDto to set
	 */
	public void setLstComtradeDto(List<ComtradeDataDTO> lstComtradeDto) {
		this.lstComtradeDto = lstComtradeDto;
	}
	/**
	 * @return the config
	 */
	public PropertiesConfiguration getConfig() {
		return config;
	}
	/**
	 * @param config the config to set
	 */
	public void setConfig(PropertiesConfiguration config) {
		this.config = config;
	}
	/**
	 * @return the lstDfrDTO
	 */
	public List<DfrDTO> getLstDfrDTO() {
		return lstDfrDTO;
	}
	/**
	 * @param lstDfrDTO the lstDfrDTO to set
	 */
	public void setLstDfrDTO(List<DfrDTO> lstDfrDTO) {
		this.lstDfrDTO = lstDfrDTO;
	}
	/**
	 * @return the session
	 */
	public Map<String, Object> getSession() {
		return session;
	}

	/**
	 * @return the lstAvailableStations
	 */
	public List<StationDTO> getLstAvailableStations() {
		return lstAvailableStations;
	}

	/**
	 * @param lstAvailableStations the lstAvailableStations to set
	 */
	public void setLstAvailableStations(List<StationDTO> lstAvailableStations) {
		this.lstAvailableStations = lstAvailableStations;
	}

//	public StationDTO getStationDetails(int stationId)
//	{
//		StationDTO currentStationDTO = null;
//		for (Iterator<StationDTO> iterator = getLstAvailableStations().iterator(); iterator.hasNext();) {
//			currentStationDTO = (StationDTO) iterator.next();
//			if (currentStationDTO.getId() == stationId)
//			{
//				break;
//			}
//		}
//		return currentStationDTO;
//	}	
	
	/**
	 * @return the stationDetails
	 */
	public StationDTO getStationDetails() {
		return stationDetails;
	}

	/**
	 * @param stationDetails the stationDetails to set
	 */
	public void setStationDetails(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
	}

	/**
	 * @return the stationId
	 */
	public String getStationId() {
		return stationId;
	}

	/**
	 * @param stationId the stationId to set
	 */
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}


	public String getErrorMessage() {
		return errorMessage;
	}


	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


	public String getInfoMessage() {
		return infoMessage;
	}


	public void setInfoMessage(String infoMessage) {
		this.infoMessage = infoMessage;
	}

	/* (non-Javadoc)
	 * @see org.apache.struts2.interceptor.ServletResponseAware#setServletResponse(javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void setServletResponse(HttpServletResponse httpServletResponse) {
		this.httpServletResponse = httpServletResponse;
		
	}

	/* (non-Javadoc)
	 * @see org.apache.struts2.interceptor.ServletRequestAware#setServletRequest(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public void setServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
		
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getContAnalogTimeLimit() {
		return contAnalogTimeLimit;
	}

	public void setContAnalogTimeLimit(int contAnalogTimeLimit) {
		this.contAnalogTimeLimit = contAnalogTimeLimit;
	}

	public int getContDataTimeLimit() {
		return contDataTimeLimit;
	}

	public void setContDataTimeLimit(int contDataTimeLimit) {
		this.contDataTimeLimit = contDataTimeLimit;
	}

	
}
