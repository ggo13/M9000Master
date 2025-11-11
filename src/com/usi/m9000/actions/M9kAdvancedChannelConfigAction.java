package com.usi.m9000.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.xmlbeans.XmlOptions;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import com.usi.AlarmDocument.Alarm;
import com.usi.AlarmsDocument.Alarms;
import com.usi.StationPropertiesDocument.StationProperties;
import com.usi.SubStationDocument.SubStation;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.dao.UsersDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.station.util.LedName;
import com.usi.m9000.station.util.RelayName;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kMessagesUtil;
import com.usi.m9000.util.M9kUtils;

public class M9kAdvancedChannelConfigAction extends ActionSupport implements SessionAware, ServletContextListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Object> session;
    private List<UsersDTO> m9kAppUsers;
    private M9kDAOFactory m9kDAOFactory;
    private UsersDAO usersDao;
    private UsersDTO userDto;

    // Save parameters
    private String id;
    private String userName;
    private String password;
    private String role;
    
    private StationDTO stationDetails;
 // 31-May-2022 - Relay mappings from config xml
 	private HashMap<LedName, List<Object>> mapLedToRelay;
 	private List<String> lstAlarmNames;
 	private List<String> lstRelays;
 	private int totalNumberOfRelays=4; // Differs based on Arbor or Moxa. Defaults it to Moxa 4.
    static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kAdvancedSettingsAction.class);

    public M9kAdvancedChannelConfigAction() {
    	m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
    	usersDao = m9kDAOFactory.getUsersDAO();
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
        userDto = (UsersDTO) this.session.get("userDetails");
        stationDetails = (StationDTO) session.get("stationDetails");
    }

    @Override
    @Action(value = "displaySystemSettings", results = {
    @Result(name="success",location="/jsp/createRemoteId.jsp")})
    @SkipValidation
    public String execute() throws Exception {
        return SUCCESS;
    }

    @Actions({@Action(value = "usersList-json", results = {
            @Result(type = "json", params = {"includeProperties", "m9kAppUsers.*"})}
    )
    })
    @SkipValidation
    public String getAllUsers()
    {
    	logger.debug("Get all users gets invoked!!");
        m9kAppUsers = usersDao.getAllUsers();
    	return SUCCESS;
    }
    @Action(value = "saveUsers", interceptorRefs = {@InterceptorRef("jsonValidationWorkflowStack"),@InterceptorRef("defaultStack"),@InterceptorRef("exceptionMappingStack")}, results = {
            @Result(name = SUCCESS, type = "json", params = {"includeProperties", "actionMessages.*,m9kAppUsers.*",
                    "ignoreHierarchy", "false"}),
            @Result(name = ERROR, type = "json", params = {"includeProperties", "actionErrors.*", "ignoreHierarchy",
                    "false", "statusCode", "500"})})
    public String saveUserDetails()
    {
    	String resultString = SUCCESS;
    	
    	try {
            UsersDTO userDto = new UsersDTO();
            userDto.setUserName(getUserName());
            userDto.setPassword(getPassword());
            userDto.setRole(getRole());
            logger.debug("get id "+getId());
            if (getId() != null && !"".equals(getId())) {
                userDto.setId(Integer.parseInt(getId()));
                int updatedCount = usersDao.updateUserDetails(userDto);
                if (updatedCount == 0)
                {
                	logger.error("No user was updated");
                	resultString = ERROR;
                    this.addActionError("No User was updated");
                }
                else
                {
                	logger.info("User details updated for "+getId()+" user "+userDto.getDisplayName());
                }
            } else {
            	usersDao.addUser(userDto);
                logger.debug("Created new user "+getUserName());
            }
        } catch (Exception e) {
        	resultString = ERROR;
            this.addActionError("Error while saving user : " + e.getMessage());
        }
    	return resultString;
    }
    
    @Action(value = "deleteUsers", interceptorRefs = {@InterceptorRef("exceptionMappingStack")}, results = {
            @Result(name = SUCCESS, type = "json", params = {"includeProperties", "actionMessages.*",
                    "ignoreHierarchy", "false"}),
            @Result(name = ERROR, type = "json", params = {"includeProperties", "actionErrors.*", "ignoreHierarchy",
                    "false", "statusCode", "500"})})
    @SkipValidation
    public String deleteUser() throws Exception {
        String resultString = SUCCESS;
        try {
            int removeId = Integer.parseInt(id);
            if (userDto.getId() == removeId)
            {
            	addActionError("You are logged in with the same user that you are trying to delete. You cannot remove yourself.");
            	resultString = ERROR;
            }
            else
            {
	            logger.debug("Delete user " + removeId);
	            int deleteCount = usersDao.removeUser(removeId);
	            if (deleteCount == 0)
	            {
	            	logger.error("No user was deleted");
	            	resultString = ERROR;
	                this.addActionError("No User was deleted");
	            }
            }
        } catch (Exception e) {
        	resultString = ERROR;
            this.addActionError("Error while deleting user : " + e.getMessage());
        }
        return resultString;
    }


	public List<UsersDTO> getM9kAppUsers() {
		return m9kAppUsers;
	}

	public void setM9kAppUsers(List<UsersDTO> m9kAppUsers) {
		this.m9kAppUsers = m9kAppUsers;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@RequiredStringValidator(fieldName = "name", type = ValidatorType.FIELD, message = "User Name is required")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@RequiredStringValidator(fieldName = "password", type = ValidatorType.FIELD, message = "Password is required")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@RequiredStringValidator(fieldName = "role", type = ValidatorType.FIELD, message = "User Role is required")
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	//TODO: Change the logic to look into stationDetails.lstAlarmRelayMappings from advancedSttings jsp to save into xml
	private boolean saveRelayMappingSettingsToXML(SubStation substation)
	{
		boolean booChanged = false;
		StationProperties stationProperties = substation.getStationProperties();
		if (stationProperties != null)
		{
			Alarms alarms = stationProperties.getAlarms();
			logger.debug("populateRelayMappingSettingsFromXML: alarms xml "+alarms+" relays list "+getLstRelays()+" alarm names "+getLstAlarmNames());
			if (alarms != null)
			{
				Alarm alarm[] = alarms.getAlarmArray();
				for (int i = 0; i < alarm.length; i++) {
					logger.debug("Alarms Name "+alarm[i].getAlarmName());
						for (int j = 0; j < getLstAlarmNames().size(); j++) {
							logger.debug("xml alarm name "+alarm[i].getAlarmName()+" from the list of alarm names "+getLstAlarmNames().get(j));
							if (alarm[i].getAlarmName().equalsIgnoreCase(getLstAlarmNames().get(j)))
							{
								if (!alarm[i].getRelay().equalsIgnoreCase(stationDetails.getLstAlarmRelaysMapping().get(j)))
								{
									alarm[i].setRelay(stationDetails.getLstAlarmRelaysMapping().get(j));
									booChanged = true;
								}
							}
						}
									
				}
				if (alarms.sizeOfAlarmArray() < getLstAlarmNames().size()) // Just to add new alarm mappings to already existing alarm mappings. e.g Disturbance alarm added as last minute
					{
					Alarm newAlarm = null;
						int missingMapping = getLstAlarmNames().size() - alarms.sizeOfAlarmArray();
						for (int i = missingMapping; i > 0;  i--)
						{
							newAlarm = alarms.addNewAlarm();
							newAlarm.setAlarmName(getLstAlarmNames().get(getLstAlarmNames().size()-i));
							newAlarm.setRelay(stationDetails.getLstAlarmRelaysMapping().get(stationDetails.getLstAlarmRelaysMapping().size()-i));
						}
					}
			}
			else
			{
				booChanged = true;
				createRelayMappingSettingsInXML(substation);
			}
		}
		else
		{
			booChanged = true;
			createRelayMappingSettingsInXML(substation);
		}
		
		return booChanged;
	}

	/**
	 * 26-May-2022 - Relay mapping information from GUI
	 * @param substation
	 */
	private void createRelayMappingSettingsInXML(SubStation substation)
	{
		Alarms alarmsRoot = substation.addNewStationProperties().addNewAlarms();
		Alarm alarm = null;
//		for (int i=0; i < getLstAlarmNames().size();i++)
		String alarmsRelayMap;
		int i=0;
		for (Iterator<String> iterator = stationDetails.getLstAlarmRelaysMapping().iterator(); iterator.hasNext();)
		{
			alarmsRelayMap = iterator.next();
			logger.debug("populateRelayMappingSettings: Alarms mapping "+alarmsRelayMap);
			alarm = alarmsRoot.addNewAlarm();
			alarm.setAlarmName(getLstAlarmNames().get(i++));
			alarm.setRelay(alarmsRelayMap);
		}
	}

	public HashMap<LedName, List<Object>> getMapLedToRelay() {
		return mapLedToRelay;
	}

	public void setMapLedToRelay(HashMap<LedName, List<Object>> mapLedToRelay) {
		this.mapLedToRelay = mapLedToRelay;
	}

	public List<String> getLstRelays() {
		lstRelays = new ArrayList<String>(8);
		logger.debug("Total number of relays "+getTotalNumberOfRelays());
			for (int i=0; i < getTotalNumberOfRelays();i++)
			{
				lstRelays.add(RelayName.values()[i].name());
			}
		return lstRelays;
	}

	public void setLstRelays(List<String> lstRelays) {
		this.lstRelays = lstRelays;
	}

	public List<String> getLstAlarmNames() {
		if (lstAlarmNames == null)
		{
			lstAlarmNames = new ArrayList<String>(7);
			for (int i=7; i >= 0;i--)
			{
				lstAlarmNames.add(LedName.values()[i].name());
			}
//			lstAlarmNames.add(LedName.WETTING_VOLTAGE.name());
		}
		return lstAlarmNames;
	}

	/**
	 * Returns the advanced settings
	 * @return
	 */
	@Action(value = "displayAdvancedChannelSettings", results = {
	@Result(name="SUCCESS",location="/jsp/advancedChannelConfig.jsp"),
	@Result(name="error",location="/jsp/advancedChannelConfig.jsp"),
	@Result(name="Cancel",location="/jsp/displayDFR.jsp")})
    @SkipValidation
	public String fetchAdvancedSettings()
	{
		logger.debug("fetchAdvancedSettings is invoked!! Is event Test enabled? "+stationDetails.isEnableEventTest());
		String computerType = M9kMessagesUtil.sendSynchMessage(""+stationDetails.getSystemStationId(), "STATION_MASTER_COMPUTER_TYPE", "STATION_MASTER_COMPUTER_TYPE");
		logger.info("In fetchAdvancedSettings computerType "+computerType);
		if (computerType.toUpperCase().startsWith("ERROR"))
		{
			logger.error("Unable to reach station master"+stationDetails.getSystemStationId()+" to get computer type ");
		}
		if (computerType.equalsIgnoreCase("ARBOR"))
		{
			setTotalNumberOfRelays(8);
		}
		else
		{
			setTotalNumberOfRelays(4);
		}
		logger.info("Setting total number of relays to "+getTotalNumberOfRelays());
//		session.put("lstRelays", getLstRelays());
		return SUCCESS;
	}
	
	/**
	 * Returns the advanced settings
	 * @return
	 * @throws M9000Exception 
	 */
	@SkipValidation
	public String saveAdvancedSettings() throws M9000Exception
	{
		SubStation currentSubstation = (SubStation) session.get("selectedSubstation");
		  boolean booChanged = false;
		  booChanged = saveRelayMappingSettingsToXML(currentSubstation);
		  // START: 01-July-2022 - Save Trigger duration
		  StationProperties stationProperties = currentSubstation.getStationProperties();
		  int advancedSettingsChangeTracker = 1;
			if (stationProperties == null)
			{
				stationProperties = currentSubstation.addNewStationProperties();
			}
			else
			{
				advancedSettingsChangeTracker = stationProperties.getAdvancedSettingsChangeTracker()+1;
			}
			stationProperties.setAdvancedSettingsChangeTracker(advancedSettingsChangeTracker);

			stationProperties.setTriggerDuration(stationDetails.getTriggerDuration());
		// END: 01-July-2022
			// START: 11-Sept-2022
			stationProperties.setContOscDaysToRetain(stationDetails.getContOscDaysToRetain());
			stationProperties.setContMeasurementsDaysToRetain(stationDetails.getContMeasurementsDaysToRetain());
			// END: 11-Sept-2022
			// START: 09-Jan-2023 - Moving export time limit for cont. data to GUI
			stationProperties.setContOscExportTimeLimit(stationDetails.getContOscExportTimeLimit());
			stationProperties.setContMeasurementsExportTimeLimit(stationDetails.getContMeasurementsExportTimeLimit());
			// END: 09-Jan-2023
			// START: 11-Jun-2024 - Enable or Disable event test for incompatible old boards
			logger.debug("Is ENable event test "+ stationDetails.isEnableEventTest());
			if (stationDetails.isEnableEventTest())
			{
				stationProperties.setEnableEventTest(M9kConstants.ENABLE_ONE);
			}
			else
			{
				stationProperties.setEnableEventTest(M9kConstants.DISABLE_ZERO);
			}
			// END: 11-Jun-2024
			// START: 18-Jun-2024 - Set DDR deck time in seconds to be configurable
			stationProperties.setDdrDeckTimeLimit(stationDetails.getDdrDeckTimeLimit());
			// END: 18-Jun-2024
			session.put("stationDetails", stationDetails);
			saveAdvancedSettingsIntoXml(currentSubstation);
			M9kUtils.setStationDetails(stationDetails);
		return SUCCESS;
	}
	
	private void saveAdvancedSettingsIntoXml(SubStation substation) throws M9000Exception
	{
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		StationDAO mysqlStationDao = m9kDAOFactory.getStationDAO();
		
		XmlOptions xmlOptions = new XmlOptions(); 
		xmlOptions.setSaveOuter();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setUseDefaultNamespace();
		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("", "http://www.usi.com");
		xmlOptions.setSaveImplicitNamespaces(prefixes);
		stationDetails = (StationDTO) session.get("stationDetails");
		logger.info("After saveAdvancedSettingsIntoXML Is ENable event test "+ stationDetails.isEnableEventTest());
		String xmlText = substation.xmlText(xmlOptions);
		Diff diff = DiffBuilder.compare(Input.fromString(stationDetails.getConfigXml())).withTest(Input.fromString(xmlText))
			     .checkForSimilar()
			     .ignoreWhitespace()
			     .build();

		logger.debug(diff.toString()+" diff has differences? "+diff.hasDifferences());
		int iLineCount = 0;
		int syslogNumLines = M9kUtils.getSyslogTotalLines();
		if (diff.hasDifferences())
		{
			Iterable<Difference> iterDiffs = diff.getDifferences();
			StringBuffer strBufDiff = new StringBuffer();
			for (Difference difference : iterDiffs) {
				strBufDiff.append(difference.toString()+M9kConstants.NEWLINE);
				iLineCount++;
				if((iLineCount % syslogNumLines) == 0)
				{
					logger.info("User "+userDto.getUserName()+" modified the Advanced settings "+stationDetails.getStationDisplayName()+" "+M9kConstants.NEWLINE+strBufDiff.toString());
					strBufDiff.setLength(0);
				}
			}
			if(strBufDiff.length() > 0)
			{
				logger.info("User "+userDto.getUserName()+" modified the Advanced settings  "+stationDetails.getStationDisplayName()+" "+M9kConstants.NEWLINE+strBufDiff.toString());
			}
		}

		stationDetails.setConfigXml(xmlText);
		if (mysqlStationDao.isStationExists(stationDetails.getSystemStationId()))
		{
			mysqlStationDao.updateConfigXml(stationDetails);
		}
		else
		{
			mysqlStationDao.insertStationDetails(stationDetails);
		}
		String currentTime = M9kUtils.getCurrentDateTime();
		String emailSubject = "";
		String emailText = "";
		// Update station master Database in case of remote architecture
		String response = M9kMessagesUtil.sendSynchMessage(""+stationDetails.getSystemStationId(), stationDetails.getConfigXml(),"CONFIG_UPDATE_STATION_DB");
		logger.debug("Response from the JMS client "+response);
		if (response != null && response.toUpperCase().contains("SUCCESSFUL"))
		{
			addActionMessage(response);		
			// 12-Nov-2021 - Send Email for every configuration change
			logger.info("User "+userDto.getUserName()+" succesfully modified avanced settings for station "+stationDetails.getStationDisplayName());
			logger.debug("About to send email...");
			emailSubject = currentTime+" - "+ stationDetails.getStationDisplayName()+" - advanced settings modified by user "+userDto.getUserName();
//			String emailText = "User "+userDto.getUserName()+" applied the configuration at "+currentTime;
			emailText = "User "+userDto.getUserName()+" succesfully modified advanced settings for station "+stationDetails.getStationDisplayName();

		}
		else
		{
			addActionError(response+" Could not apply to the station as Station Master is not reachable. Please ensure it is set to remote architecture");
			logger.error("ERROR!!! User "+userDto.getUserName()+" could not save advanced settings for station "+stationDetails.getStationDisplayName()+" due to "+response);
			logger.debug("About to send email...");
			emailSubject = currentTime+" - "+ stationDetails.getStationDisplayName()+" - advanced settings modified by user "+userDto.getUserName();
//			String emailText = "User "+userDto.getUserName()+" applied the configuration at "+currentTime;
			emailText = "User "+userDto.getUserName()+" modified advanced settings for station "+stationDetails.getStationDisplayName()+" but unable to apply to the station as Station Master is not reachable. Please ensure it is set to remote architecture";
		}
		// START: 23-Jul-2020 Backup config from web master everytime user does a finish or finish and send even without station master
		M9kUtils.masterBackUpConfig(stationDetails);
		// END: 23-Jul-2020
		
		M9kUtils.sendChangeEmail(emailSubject, emailText);

		logger.info("User "+userDto.getUserName()+" succesfully modified advanced settings for station "+stationDetails.getStationDisplayName());
	}

	public void validate()
	{
		logger.debug("Entered user validate method user name "+getUserName()+" user role "+getRole());
		if (getId() != null && getId().isEmpty())
		{
			UsersDTO existingUser = usersDao.getUserDetails(getUserName());
			if (existingUser != null)
			{
				addFieldError("userName", "User Name already exists");
			}
		}
		if (!M9kUtils.isValidUserName(getUserName()))
		{
			addFieldError("userName", "User Name should have no special characters. It should not start with number and should be between 3 to 15 characters");
		}
		if (!M9kUtils.isValidPassword(getPassword()))
		{
			addFieldError("password", "Password should contain at least one lower case, one upper case, one number and one of these special characters !@#$%^&. Length should be between 4 to 20 characters");
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

	public StationDTO getStationDetails() {
		return stationDetails;
	}

	public void setStationDetails(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
	}

	public int getTotalNumberOfRelays() {
		return totalNumberOfRelays;
	}

	public void setTotalNumberOfRelays(int totalNumberOfRelays) {
		this.totalNumberOfRelays = totalNumberOfRelays;
	}
}
