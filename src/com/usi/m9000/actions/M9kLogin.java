package com.usi.m9000.actions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.dao.UsersDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.HierarchyDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.queues.M9kMasterNotificationsListener;
import com.usi.m9000.util.LDAPUtils;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kKeyValuePair;
import com.usi.m9000.util.M9kUtils;
/**
 * <p> Validate a user login. </p>
 */

public class M9kLogin extends ActionSupport implements SessionAware, ServletContextListener, ServletRequestAware,  ServletResponseAware{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kLogin.class);
	 private String userName = null;
	 String btnSubmit;
	 Map<String, Object> session;
	 StationDTO stationDetails;
	 List<StationDTO> lstAvailableStations;
	 PropertiesConfiguration config;
	 PropertiesConfiguration cfgComtrade;
	 PropertiesConfiguration cfgDb;
	 M9kDAOFactory m9kDAOFactory;
	 StationDAO stationDao;
	 UsersDAO userDao;
	 UsersDTO userDto;
	 ExecutorService executor;
	 Set<UsersDTO> logins;
	 private String sessionId;
	 protected HttpServletRequest httpServletRequest;
	 protected HttpServletResponse httpServletResponse;
	 private Cookie userCookie = null;
	 private int sessionTimeOut = 1800; // 3 Minutes
	 // 04-Nov-2021 - Hierarchy implementation - list of child zones
	 private List<M9kKeyValuePair> lstChildZones;
	 private int parentZoneLevel;
	 private List<Integer> zoneIds = null;
	 private List<StationDTO> lstFilteredStations;
	 private List<Integer> lstAllSubzoneChildrenIds = null;

	 // END: 07-Nov-2021
	 public M9kLogin()
	 {
			m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
			stationDao = m9kDAOFactory.getStationDAO();
			userDao = m9kDAOFactory.getUsersDAO();
			executor = Executors.newCachedThreadPool();
//			session = new HashMap<String, Object>();
	 }
	@SuppressWarnings("unchecked")
	public String execute(){
		String result = SUCCESS;
		logger.debug("Validating login");
		try {
			logger.debug("User Name: "+getUserName());
			if (getBtnSubmit() != null && (getBtnSubmit().equalsIgnoreCase("Back")))
			{
				
				stationDetails = (StationDTO) session.get("stationDetails");
				result = SUCCESS;
			}
			
			else if (getUserName() == null || getUserName().isEmpty() || getPassword() == null || getPassword().isEmpty())
			{
//				addFieldError("password", "Password cannot be blank");
				addActionError("User name or password cannot be blank");
				result = ERROR;
			}
			else
			{
				if (!LDAPUtils.isLDAPEnabled())
				{
					userDto = userDao.getUserDetails(getUserName(), getPassword());
				}
				else
				{
					userDto = LDAPUtils.isUserAuthenticated(getUserName(), getPassword());
				}
				
	//			if(!getUserName().equalsIgnoreCase("usi") || !getPassword().equals("usi")){
				if(userDto == null){
					addActionError("Invalid user name or password! Please try again!");
					result = ERROR;
				}
				else if (userDto != null )
				{
					if (userDto.getErrorMessage() != null  && !userDto.getErrorMessage().isEmpty())
					{
						addActionError(userDto.getErrorMessage());
						result = ERROR;
					}
					else if (userDto.getRole() == null )
					{
						addActionError("User is not authorized");
						result = ERROR;
					}
					else{
						ServletContext context = ServletActionContext.getServletContext();
						
						logins = (Set<UsersDTO>) context.getAttribute("logins");
						logger.debug("logins from servlet context..." +logins+" userDTO "+userDto);
						userCookie = getUserCookiesIfExist();
	//					if (logins != null)
	//					{
	//						UsersDTO usersDTOFromSession = doesUserExist(logins);
	//						if (usersDTOFromSession != null)
	//						{
	//							if (usersDTOFromSession.getIpAddress() != null && !usersDTOFromSession.getIpAddress().isEmpty())
	//							{
	//								logger.info("User "+usersDTOFromSession.getUserName()+" has already logged in a machine with ipaddress "+usersDTOFromSession.getIpAddress());
	////								addActionMessage("User "+usersDTOFromSession.getUserName()+" has already logged in a machine with ipaddress "+usersDTOFromSession.getIpAddress());
	//							}
	//							else
	//							{
	//								logger.info("User "+usersDTOFromSession.getUserName()+" has already logged in a machine. Ipaddress not known.");
	////								addActionMessage("User "+usersDTOFromSession.getUserName()+" has already logged in a machine. Ipaddress not known");
	//							}
	//						}
	//					}
						// Set the editMode as false
						 String ipAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");  
	//					 System.out.println("Session Time out interval "+getSessionTimeOut());
						 logger.info("Session Time out interval set to "+getSessionTimeOut());
						 logger.debug("Ip address from header "+ipAddress);
						   if (ipAddress == null) {  
							   ipAddress = httpServletRequest.getRemoteAddr();
							   logger.debug("Ip address from get Remote addr "+ipAddress);
						   }
						   if (ipAddress.equalsIgnoreCase("0:0:0:0:0:0:0:1")) // Logged in from the localhost
						   {
							   userDto.setIpAddress("localhost");
						   }
						   else
						   {
							   userDto.setIpAddress(ipAddress);
						   }
						userDto.setEditMode(false);
						userDto.setSessionId(getSessionId());
						// 03-Nov-2021 - Hierarchy implementation - Set root zone info if available
						checkForHierarchy();
						logger.info("User "+userDto.getUserName()+" logged in from "+userDto.getIpAddress()+" with "+userDto.getRole()+" role");
						if (session != null)
						{
	//						session.clear();
							if (userDto.getDisplayName() != null && !userDto.getDisplayName().isEmpty())
							{
								session.put("userName", userDto.getDisplayName());
							}
							else
							{
								session.put("userName", userName);
							}
							session.put("userDetails", userDto);
						}
						M9kUtils.setUsersDto(userDto);
	
						initializeProperties();
	//					logger.debug("Before invoking station list");
						try
						{
							lstAvailableStations = stationDao.getStationsList();
	//						logger.debug("list returned..."+lstAvailableStations);
						}
						catch(Exception e)
						{
							logger.error("Exeption occured ",e);
						}
						
						if (userCookie != null && !userCookie.getValue().isEmpty() && (Integer.parseInt(userCookie.getValue()) > 0) && !lstAvailableStations.isEmpty())
						{
	//						stationDetails = stationDao.getStationsDetails(Integer.parseInt(userCookie.getValue()));
							int staionIdFromCookie = Integer.parseInt(userCookie.getValue());
							boolean stationIdFound = false;
							StationDTO stationDTO;
							for (Iterator<StationDTO> iterator = lstAvailableStations.iterator(); iterator.hasNext();) {
								stationDTO = iterator.next();
								if (staionIdFromCookie == stationDTO.getId())
								{
									stationIdFound = true;
									break;
								}
							}
							if (stationIdFound)
							{
								M9kUtils.setStationId(staionIdFromCookie);
								stationDetails = M9kUtils.getStationDetails();
							}
							else
							{
								userCookie = null;
							}
						}
						else
						{
							userCookie = null;
						}
						
	//					createNotificationListeners(lstAvailableStations);
						if (userCookie == null && !lstAvailableStations.isEmpty())
						{
							logger.debug("List is not empty with size "+lstAvailableStations.size());
							// START: 06-Mar-2015 Fetch station details from db instead of memory
	//						stationDetails = lstAvailableStations.get(0);
							M9kUtils.setStationId(lstAvailableStations.get(0).getSystemStationId());
							stationDetails = M9kUtils.getStationDetails();
	//						stationDetails = stationDao.getStationsDetails(lstAvailableStations.get(0).getSystemStationId());
							// END: 06-Mar-2015
							userCookie = new Cookie(userDto.getUserName(), "0");
		                    userCookie.setMaxAge(60 * 60 * 24 * 365); // Valid for a year
		                    userCookie.setPath("/");
		                    httpServletResponse.addCookie(userCookie);
							
						}
	//					logger.debug("list returned..."+lstAvailableStations);
	//					logger.debug("Station details after login "+stationDetails);
						if (stationDetails != null)
						{
							userDto.setEditedStationId(stationDetails.getSystemStationId());
							session.put("stationDetails",stationDetails);
							session.put("lstAvailableStations", lstAvailableStations);
							logger.info("User "+userDto.getUserName()+" logged in succesfully into station "+stationDetails.getStationDisplayName());
						}
						if (M9kUtils.getWebMasterSoftwareVersion() != null)
						{
							session.put("version", M9kUtils.getWebMasterSoftwareVersion());
						}
						// START: 14-July-2021 - Setting architecture type in session to access it from jsp 
						if (M9kUtils.isRemote() != null && M9kUtils.isRemote())
						{
							session.put("ARCH_TYPE", "REMOTE");
							logger.info("Architecture is set to remote");
						}
						// END: 14-July-2021
						result = SUCCESS;
		
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error occured...",e);
			addActionError("Login Unsuccessful!! "+e.getCause());
			result = ERROR;
		}
		return result;
}
	
	private void checkForHierarchy() {
		HierarchyDTO hierarchyDTO = M9kUtils.getHierarchyInfo();
		if (hierarchyDTO != null)
		{
			session.put("HIERARCHY_INFO", hierarchyDTO);
		}
		
	}
	/**
	 * @return
	 */
	private Cookie getUserCookiesIfExist() {
		Cookie cookie = null;
		Cookie[] allCookies = httpServletRequest.getCookies();
		logger.debug("Cookies from session "+allCookies);
		for (int i = 0; allCookies != null && i < allCookies.length; i++) {
			if (allCookies[i].getName().equalsIgnoreCase(userDto.getUserName()))
			{
				cookie = allCookies[i];
				logger.debug("matching Cookie value "+cookie.getValue());
			}
		}
		logger.debug("Cookie to be returned "+cookie);
		return cookie;
	}
	/**
	 * @param logins2
	 * @param userDto2
	 * @return
	 */
	@SuppressWarnings("unused")
	private UsersDTO doesUserExist(Set<UsersDTO> logins2) {
		boolean booExists = false;
		UsersDTO usersDTOFromSession = null;
		for (Iterator<UsersDTO> iterator = logins2.iterator(); iterator.hasNext();) {
			usersDTOFromSession = iterator.next();
			if (usersDTOFromSession.getUserName().equalsIgnoreCase(userDto.getUserName()))
			{
				booExists = true;
				logger.debug("User "+userDto.getUserName()+" already logged in from "+userDto.getIpAddress());
				break;
			}
		}
		if (booExists == false)
		{
			usersDTOFromSession = null;
		}
		return usersDTOFromSession;
	}
	@SuppressWarnings("unused")
	private void createNotificationListeners(
			List<StationDTO> lstAvailableStations2) {
		for (Iterator<StationDTO> iterator = lstAvailableStations2.iterator(); iterator
				.hasNext();) {
			StationDTO stationDTO = iterator.next();
			if (stationDTO.getSystemStationName() != null && !stationDTO.getSystemStationName().isEmpty())
			{
				new M9kMasterNotificationsListener(stationDTO.getSystemStationName()+"-NotificationQ");
			}
			
		}
		
	}
	@SkipValidation
	public String showLogin()
	{
//		logger.debug("In Show Login actionErrors? "+hasActionErrors()+" has action messages? "+hasActionMessages()+" has errors? "+hasErrors()+ " has field errors? "+hasFieldErrors());
//		if (session != null)
//		{
//			session.clear();
//		}
		return SUCCESS;
	}
	
	@SkipValidation
	public String back() throws M9000Exception
	{
		try
		{
			userDto = (UsersDTO) session.get("userDetails");
			userDto.setEditedStationId(0);
			logger.info("User "+userDto.getUserName()+" is back in the home page");
			if (userDto.isEditMode() && userDto.getRole().equalsIgnoreCase(M9kConstants.GUEST))
			{
				userDto.setRole(M9kConstants.ADMIN);
			}
			userDto.setEditMode(false);
			userName = (String) session.get("userName");
			stationDetails = (StationDTO)session.get("stationDetails");
			logger.debug("station details "+(stationDetails != null? stationDetails.getStationDisplayName():"StationDetails is null"));
			config = (PropertiesConfiguration) session.get("config");
			cfgComtrade = (PropertiesConfiguration) session.get("cfgComtrade");
			logins = (Set<UsersDTO>) session.get("logins");
			logger.debug("logins from servlet context... "+logins);
//			logger.debug("From M9kLogin backTo()..."+getStationDetails());
//			logger.debug("Session before clear..."+session.get("stationDetails"));
			
			session.clear();
	//		stationDetails = null;
			
			lstAvailableStations = stationDao.getStationsList();
			initializeProperties();
//			logger.debug("After availableStations()..."+getStationDetails());
//			if (logins != null)
//			{
//				session.put("logins", logins);
//			}
			session.put("userName", userName);
			session.put("userDetails", userDto);
			// 07-Nov-2021 - Hierarchy implementation - Look for hierarchy structure
			checkForHierarchy();
//			session.put("config", config);
//			session.put("cfgComtrade", cfgComtrade);
			// START: 30-July-2021 Fetch station details from db instead of memory
			if (stationDetails == null && !lstAvailableStations.isEmpty())
			{
				logger.debug("List is not empty with size "+lstAvailableStations.size());
				M9kUtils.setStationId(lstAvailableStations.get(0).getSystemStationId());
				stationDetails = M9kUtils.getStationDetails();
				if (stationDetails != null)
				{
					userDto.setEditedStationId(stationDetails.getSystemStationId());
					session.put("stationDetails",stationDetails);
				}
			}
			// 22-Aug-2022 - Release 1.0.9.12 - Added logic to avoid exception when you click back after you have clicked New button to create station 
			else if (stationDetails != null && stationDetails.getSystemStationId() != null && !lstAvailableStations.isEmpty()) 
			{
				M9kUtils.setStationId(stationDetails.getSystemStationId());
				userDto.setEditedStationId(stationDetails.getSystemStationId());
			}
			// END: 30-July-2021
//					logger.debug("list returned..."+lstAvailableStations);
//					logger.debug("Station details after login "+stationDetails);
			session.put("lstAvailableStations", lstAvailableStations);
			if (M9kUtils.getWebMasterSoftwareVersion() != null)
			{
				session.put("version", M9kUtils.getWebMasterSoftwareVersion());
			}
			// START: 14-July-2021 - Setting architecture type in session to access it from jsp 
			if (M9kUtils.isRemote() != null && M9kUtils.isRemote())
			{
				session.put("ARCH_TYPE", "REMOTE");
				logger.info("Architecture is set to remote");
			}
//			session.put("session-timeout-secs", getSessionTimeOut());
//			session.remove("accessMode");
			//TODO: Temporary Fix for an error
			clearFieldErrors();
		}
		catch (Exception e)
		{
			logger.error("Exception occured returning back to stations list page "+e);
			throw new M9000Exception(e);
		}
		return SUCCESS;
	}
	
	private void initializeProperties() throws M9000Exception
	{
		
		logger.debug("Entered PopulateChannelsList...");
		try {
			config = new PropertiesConfiguration("/M9000.properties");
			cfgComtrade = new PropertiesConfiguration("/M9K_COMTRADE.properties");
			cfgDb = new PropertiesConfiguration("/db.properties");
			logger.debug("CfgDB obj..."+cfgDb);
			logger.debug("\t\t host: "+cfgDb.getString("dfr_db_host"));
			logger.debug("\t\t host: "+cfgDb.getString("host"));
			session.put("config", config);
			session.put("cfgComtrade", cfgComtrade);
			session.put("cfgDb", cfgDb);
			
			// Read session timeout web.xml
			
			session.put("session-timeout-secs", getSessionTimeOut());
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Reading Property file failed!",e);
			throw new M9000Exception(e);
		}		
	}
	
	@Override
	public void setSession(Map<String, Object> session) {
//		logger.debug("Setting session "+session);
		this.session = session;
		
	}


	/**
	 * Show admin screen
	 */
	public String showAdmin()
	{
		logger.debug("About to forward to administration screen...");
		
		return SUCCESS;
	}
/**
 * <p>Provide User username.</p>
 *
 * @return Returns the User username.
 */
public String getUserName() {
	return userName;
}
/**
 * <p>Store new User username</p>
 *
 * @param value The username to set.
 */
public void setUserName(String value) {
	userName = value;
}
// ---- Username property ----
/**
 * <p>Field to store User password.</p>
 * <p/>
38
 */
private String password = null;
/**
 * <p>Provide User password.</p>
 *
 * @return Returns the User password.
 */
public String getPassword() {
	return password;
}
/**
 * <p>Store new User password</p>
 *
 * @param value The password to set.
 */
public void setPassword(String value) {
	password = value;
}
/**
 * @return the btnSubmit
 */
public String getBtnSubmit() {
	return btnSubmit;
}
/**
 * @param btnSubmit the btnSubmit to set
 */
public void setBtnSubmit(String btnSubmit) {
	this.btnSubmit = btnSubmit;
}
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

@Override
public void contextDestroyed(ServletContextEvent arg0) {
//	ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//    LogFactory.release(contextClassLoader);
	
}

@Override
public void contextInitialized(ServletContextEvent arg0) {
	// TODO Auto-generated method stub
	
}
public UsersDTO getUserDto() {
	return userDto;
}
public void setUserDto(UsersDTO userDto) {
	this.userDto = userDto;
}
/* (non-Javadoc)
 * @see org.apache.struts2.interceptor.ServletRequestAware#setServletRequest(javax.servlet.http.HttpServletRequest)
 */
@Override
public void setServletRequest(HttpServletRequest httpServletRequest) {
	setSessionId(httpServletRequest.getSession().getId());
	this.httpServletRequest = httpServletRequest;
}
/**
 * @return the sessionId
 */
public String getSessionId() {
	return sessionId;
}
/**
 * @param sessionId the sessionId to set
 */
public void setSessionId(String sessionId) {
	this.sessionId = sessionId;
}
/* (non-Javadoc)
 * @see org.apache.struts2.interceptor.ServletResponseAware#setServletResponse(javax.servlet.http.HttpServletResponse)
 */
@Override
public void setServletResponse(HttpServletResponse httpServletResponse) {
	this.httpServletResponse = httpServletResponse;
	
}
/**
 * @return the sessionTimeOut
 */
public int getSessionTimeOut() {
//	sessionTimeOut = config.getInt("session-timeout-secs",1800);
	sessionTimeOut= httpServletRequest.getSession().getMaxInactiveInterval();
	if (sessionTimeOut >= 300)
	{
		sessionTimeOut -= 120;
	}
	else // If session Time out is set < 2 minutes, we'll default it to 30 minutes
	{
		logger.debug("setting to 30 minutes as the session time out is set to "+sessionTimeOut);
		httpServletRequest.getSession().setMaxInactiveInterval(1800);
		sessionTimeOut = 1800 - 120;
		
	}
	return sessionTimeOut;
}
/**
 * @param sessionTimeOut the sessionTimeOut to set
 */
public void setSessionTimeOut(int sessionTimeOut) {
	this.sessionTimeOut = sessionTimeOut;
}

public String getJSONChildZones(){
	logger.debug("Entered getJSON method..."+getZoneIds()+" parent zone level "+getParentZoneLevel());
	getLstChildZones();
	return SUCCESS;
}
public String getJSONFilteredStationsList() throws M9000Exception {

	logger.debug("Entered getFilteredStationsList..."+lstAvailableStations);
	int selectedZoneId = getLastSelectedZoneId();
	logger.debug("Selected zone id..."+selectedZoneId);
	if (selectedZoneId == 0)
	{
		lstFilteredStations = stationDao.getStationsList();
	}
	else
	{
		lstAllSubzoneChildrenIds = null;
		List<Integer> lstChildZones =  getChildrenOfAllSubzoneIds(selectedZoneId);
		StationDTO stationDTO;
		lstFilteredStations = new ArrayList<StationDTO>();
		if (lstChildZones == null)
		{
			for (Iterator<StationDTO> iterator = stationDao.getStationsList().iterator(); iterator.hasNext();) {
				stationDTO = iterator.next();
				if (stationDTO.getParentZoneId() == selectedZoneId)
				{
					lstFilteredStations.add(stationDTO);
				}
			}
		}
		else
		{
			for (Iterator<StationDTO> iterator = stationDao.getStationsList().iterator(); iterator.hasNext();) {
				stationDTO = iterator.next();
				if (lstChildZones.contains(stationDTO.getParentZoneId()))
				{
					lstFilteredStations.add(stationDTO);
				}
			}
		}
	}
	logger.debug("lstFilteredStations..."+lstFilteredStations);
	return SUCCESS;
	
}
private int getLastSelectedZoneId() {
	int selectedZoneId = 0;
	if (getZoneIds() != null)
	{
		Integer iZoneId = 0;
		for (Iterator<Integer> iterator = getZoneIds().iterator(); iterator.hasNext();) {
			iZoneId = iterator.next();
			if (iZoneId != null && iZoneId.intValue() > 0)
			{
				selectedZoneId = iZoneId;
			}
			
		}
	}
	return selectedZoneId;
}
private List<Integer> getChildrenOfAllSubzoneIds(int zoneId)
{
	List<M9kKeyValuePair> lstChildren = M9kUtils.getChildrenZone(zoneId);
	logger.debug("RECURSIVE: lstChildren ..."+lstChildren);
	if (lstChildren != null && lstChildren.size() > 1)
	{
		for (Iterator<M9kKeyValuePair> iterator = lstChildren.iterator(); iterator.hasNext();) {
			M9kKeyValuePair m9kKeyValuePair = (M9kKeyValuePair) iterator.next();
			getChildrenOfAllSubzoneIds(Integer.parseInt(m9kKeyValuePair.getKey()));
		}
		if (lstAllSubzoneChildrenIds == null)
		{
			lstAllSubzoneChildrenIds = new ArrayList<Integer>();
		}
		for (Iterator<M9kKeyValuePair> iterator = lstChildren.iterator(); iterator.hasNext();) {
			M9kKeyValuePair m9kKeyValuePair =  iterator.next();
			lstAllSubzoneChildrenIds.add(Integer.parseInt(m9kKeyValuePair.getKey()));
			
		}
	}
	logger.debug("All subzone children returned "+lstAllSubzoneChildrenIds);
	return lstAllSubzoneChildrenIds;
}
public List<M9kKeyValuePair> getLstChildZones() {
	logger.debug("Entered getLstChildZones method..."+getZoneIds()+" parent zone level "+getParentZoneLevel());
	HierarchyDTO hierarchyDTO = M9kUtils.getHierarchyInfo();
	if (getParentZoneLevel() == 0 )
	{
		logger.debug("INside If part...getParentZoneLevel .. "+getParentZoneLevel());
		lstChildZones =  M9kUtils.getChildrenZone(hierarchyDTO.getRootZoneId());
	}
	else
	{
		logger.debug("INside Else part..."+getZoneIds()+" getParentZoneLevel .. "+getParentZoneLevel());
		if (getZoneIds() == null || getZoneIds().get(getParentZoneLevel()-1) == null|| getZoneIds().get(getParentZoneLevel()-1) == 0)
		{
			logger.debug("Inside Else IF "+hierarchyDTO.getMapHierarchyLevels());
			lstChildZones =  hierarchyDTO.getMapHierarchyLevels().get(getParentZoneLevel());
			lstChildZones.add(0,new M9kKeyValuePair("0","ALL"));
		}
		else
		{
			logger.debug("Inside Else ELSE");
			logger.debug("Parent zone name "+getZoneIds().get(getParentZoneLevel()));
			lstChildZones =  M9kUtils.getChildrenZone(getZoneIds().get(getParentZoneLevel()-1));
		}
	}

	
	return lstChildZones;
}

public void setLstChildZones(List<M9kKeyValuePair> lstChildZones) {
	this.lstChildZones = lstChildZones;
}
public int getParentZoneLevel() {
	return parentZoneLevel;
}
public void setParentZoneLevel(int parentZoneLevel) {
	this.parentZoneLevel = parentZoneLevel;
}
public List<Integer> getZoneIds() {
	return zoneIds;
}
public void setZoneIds(List<Integer> zoneIds) {
	this.zoneIds = zoneIds;
}
public List<StationDTO> getLstFilteredStations() {
	return lstFilteredStations;
}
public void setLstFilteredStations(List<StationDTO> lstFilteredStations) {
	this.lstFilteredStations = lstFilteredStations;
}
}