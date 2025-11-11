package com.usi.m9000.actions;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.AlarmDocument.Alarm;
import com.usi.AlarmsDocument.Alarms;
import com.usi.AnalogInputDocument.AnalogInput;
import com.usi.DFRDocument.DFR;
import com.usi.ExportsDocument.Exports;
import com.usi.GlobalLineGroupsDocument.GlobalLineGroups;
import com.usi.GroupTriggersDocument.GroupTriggers;
import com.usi.LineGroupsDocument.LineGroups;
import com.usi.MeasurementsDocument.Measurements;
import com.usi.PdcDocument.Pdc;
import com.usi.PmuDocument.Pmu;
import com.usi.PmusDocument.Pmus;
import com.usi.StationPropertiesDocument.StationProperties;
import com.usi.SubStationDocument;
import com.usi.SubStationDocument.SubStation;
import com.usi.SystemDocument.System;
import com.usi.TriggersDocument.Triggers;
import com.usi.UdpServersDocument.UdpServers;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.station.util.LedName;
import com.usi.m9000.station.util.RelayName;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kKeyValuePair;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.xml.M9000XmlConfig;
import com.usi.m9000.xml.util.M9kXMLUtils;

public class CreateChannelsAction extends ActionSupport implements SessionAware, ServletContextListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String btnSubmit;
	private Map<String, Object> session;
	private StationDTO stationDetails;
	private String stationName;
	private List<Object> lstSampleRate;
	private List<Object> lstLtSampleRate;
	Map<String, String> mapLineFrequencies;
	private List<ListValue> lineFreqObjListOld;
	Map<String, String> mapAnalogInputType;
	private List<StationDTO> lstAvailableStations;
	private String accessMode;
	PropertiesConfiguration config;
	PropertiesConfiguration cfgDb;
	private String stationId;
	private List<M9kKeyValuePair> lineFreqObjList;
	private List<Object> lstPmuDataRate;
	private List<Object> lstEventDebounce;
	
	int minimumDFRCount;
	List<DfrDTO> lstDfrDTO;
	String sourceTab;
	boolean booAnalog;
	boolean booDigital;
	private boolean lineGroupExists;
	Map<String, DFR> mapXMLDfrs;
	private M9kDAOFactory m9kDAOFactory;
	private StationDAO stationDao;
	private String customActionError;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(CreateChannelsAction.class);
	private UsersDTO userDto;
	// 16-May-2021 - Delete index of the newly added dfr  
	private int deleteIndex;
	private DfrDTO newlyAddedDfr;
	private int newlyAddedDfrIndex;
	
	// 25-May-2021 - Moved dfrs channels options list from ChannelsListInfo to here
	private List<String> channelsConfList;
	
	// 31-May-2022 - Relay mappings from config xml
	private HashMap<LedName, List<Object>> mapLedToRelay;
	private List<String> lstAlarmNames;
	private List<String> lstRelays;
//	private List<String> lstAlarmRelaysMapping;
	/**
	 * 
	 */
	public CreateChannelsAction(StationDTO stationDetails) {
		this();
		this.stationDetails = stationDetails;
	}
	
	public CreateChannelsAction()
	{
		  setChannelsConfList(M9kUtils.getDfrsChannelsOptionsList());
		logger.debug("Entered constructor of CreateChannelsAction...");
		m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		stationDao = m9kDAOFactory.getStationDAO();
	}

	  public String execute()throws Exception{
		  logger.debug("entered create Channal action.."+getBtnSubmit());
		  
		  setChannelsConfList(M9kUtils.getDfrsChannelsOptionsList());
		  if (getBtnSubmit() != null && getBtnSubmit().equals("Create"))
		  {
			  stationDetails = getStationDetails();
			  stationDetails.setConfigStatus("INCOMPLETE");
			  stationDetails.setTotalTriggersConfigured(0);
			  StationDTO.initializeExportId(0);
			  logger.debug("Station info obje... "+getStationDetails());
			  logger.debug("Station Name... "+getStationDetails().getSystemStationName());
			  logger.debug("Analog channel cnt..."+getStationDetails().getSystemAnalogChannelsCount());
			  logger.debug("PS... "+getStationDetails().getPs());
			  
			  SubStation substation = null;
			  substation = SubStationDocument.Factory.newInstance().addNewSubStation();
			  substation.setId(getStationDetails().getSystemStationId());
			  substation.setName(getStationDetails().getSystemStationName());
//			  substation.setHost(getStationDetails().getIpAddress());
			  createDefaultGolbalPmus(substation);
			  // START: 26-May-2022 - Relay mapping information from GUI
			  createRelayMappingSettingsInXML(substation);
			  // END: 26-May-2022 - Relay mapping information from GUI
			// START: 01-July-2022 - Save Trigger duration
			  StationProperties stationProperties = substation.getStationProperties();
				if (stationProperties == null)
				{
					stationProperties = substation.addNewStationProperties();
				}
				stationProperties.setTriggerDuration(stationDetails.getTriggerDuration());				
			// END: 01-July-2022
				// START: 11-Sept-2022
				stationProperties.setAdvancedSettingsChangeTracker(1);
				stationProperties.setContOscDaysToRetain(stationDetails.getContOscDaysToRetain());
				stationProperties.setContMeasurementsDaysToRetain(stationDetails.getContMeasurementsDaysToRetain());
				// END: 11-Sept-2022
				// START: 09-Jan-2023 - Moving export time limit for cont. data to GUI
				stationProperties.setContOscExportTimeLimit(stationDetails.getContOscExportTimeLimit());
				stationProperties.setContMeasurementsExportTimeLimit(stationDetails.getContMeasurementsExportTimeLimit());
				// END: 09-Jan-2023
				// START: 11-Jun-2024 - Enable or Disable event test for incompatible old boards
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
			  substation.addNewDFRs();
//			  logger.debug("Substation name set..."+substation);
			  minimumDFRCount = calculateMinimumDFRRequired();
			  lstDfrDTO = createDefaultDFRs(minimumDFRCount);
			  logger.debug("Minimum count of DFR: "+minimumDFRCount+" dfr status..."+lstDfrDTO.get(0).getStatus());
			  session.put("stationDetails", stationDetails);
			  session.put("minimumDFRCount", minimumDFRCount);
			  session.put("selectedSubstation", substation);
			  session.put("DFRsList", lstDfrDTO);
//			  logger.debug("######### Initial Global XML content..."+substation);
		  }
		  else if (getBtnSubmit() != null && getBtnSubmit().equalsIgnoreCase("Next"))
		  {
			  return configureChannels();
		  }
		  else if (getBtnSubmit() != null && getBtnSubmit().equalsIgnoreCase("Cancel"))
		  {
			  return cancel();
		  }
		  else if(getBtnSubmit() != null && getBtnSubmit().equalsIgnoreCase("Edit"))
		  {
			  logger.debug("Entering edit...");
			  logger.debug("list from session after delete action... "+lstDfrDTO);
			  session.put("DFRsList", lstDfrDTO);
			  logger.debug("DFR List..."+lstDfrDTO);
			  return SUCCESS;
		  }
		  else 
		  {
			  String navigateValue = "";
				session.put("sourceTab", sourceTab);
				booAnalog = (Boolean)session.get("booAnalog");
				booDigital = (Boolean)session.get("booDigital");
				lineGroupExists = (Boolean)session.get("lineGroupExists");
//				session.put("sourceTab", sourceTab);
				if (getSourceTab() != null && !getSourceTab().isEmpty())
				{
					configureChannels();
					logger.debug("source tab not empty "+getSourceTab());
					if (getSourceTab().equalsIgnoreCase("Analogs"))
					{
						navigateValue = "Analogs";
					}
					else if (getSourceTab().equalsIgnoreCase("Measurements"))
					{
						navigateValue = "Measurements";
					}
					else if (getSourceTab().equalsIgnoreCase("LineGroups"))
					{
						navigateValue="LineGroups";
					}
					else if (getSourceTab().equalsIgnoreCase("Events"))
					{
						logger.debug("Events tab "+getSourceTab());
						// START: 10-May-2022 - For SER only station - Fixing null pointer issue when editing configuration and click on Events menu
						if (isBooAnalog())
						{
							navigateValue="Events";
						}
						else
						{
							navigateValue = "DirectToEvents";
						}
						// END: 10-May-2022
					}
					else if (getSourceTab().equalsIgnoreCase("FaultLocation"))
					{
						navigateValue="FaultLocation";
					}
					else if (getSourceTab().equalsIgnoreCase("VirtualChannels"))
					{
						navigateValue="VirtualChannels";
					}
					// 10-Mar-2021 - Virtual Measurement implementation
					else if (getSourceTab().equalsIgnoreCase("VirtualMeasurements"))
					{
						navigateValue="VirtualMeasurements";
					}
					// 10-Mar-2021 - Virtual Measurement implementation
					return navigateValue;
				}
		  }
			 
	    return SUCCESS;

	  }
	  
	  /**
	 * @param substation
	 */
	private void createDefaultGolbalPmus(SubStation substation) {
		  Pmus pmus = substation.addNewPmus();
		  pmus.setName("PMUs");
		  Pmu pmu = pmus.addNewPmu();
		  pmu.setName(getStationDetails().getSystemStationName());
		  pmu.setPmuName(getStationDetails().getSystemStationName());
		  pmu.setPmuId(getStationDetails().getPmuId());
		  pmu.setPmuPort(getStationDetails().getPmuPort());
		  pmu.setUdpPort(getStationDetails().getPmuUdpPort());
		  pmu.setPmuStreamType(getStationDetails().getPmuStreamType());
		  pmu.setPmuPhasorMode(getStationDetails().getPmuPhasorMode());
		  pmu.setPmuDataRate(getStationDetails().getPmuDataRate());
		// Changed for PDC implementation 4-Oct-2013
//		  if (getStationDetails().getPmuStatus().equalsIgnoreCase(M9kConstants.ENABLE))
		  if (getStationDetails().isPmuEnabled())
		  // End
		  {
			  pmu.setPmuEnable(M9kConstants.ENABLE_ONE);
			  substation = createPdcXmlElement(substation);
		  }
		  else
		  {
			  pmu.setPmuEnable(M9kConstants.DISABLE_ZERO);
		  }

		
	}

	private SubStation createPdcXmlElement(SubStation substation) {
		  Pdc pdc = substation.getPdc();
		  System pdcSystem;
		  if (pdc == null)
		  {
			  pdc = substation.addNewPdc();
			  pdcSystem = pdc.addNewSystem();
		  }
		  else
		  {
			  pdcSystem = pdc.getSystem();
			  if (pdcSystem == null)
			  {
				  pdcSystem = pdc.addNewSystem(); 
			  }
		  }
		pdcSystem.setPdcStationName(getStationDetails().getSystemStationName());
		// Start: 04-Sept-2014 New PMU Id from GUI needs to be set for PDC
		//pdcSystem.setStationId(getStationDetails().getSystemStationId());
		pdcSystem.setStationId(getStationDetails().getPdcId());
		// End
		pdcSystem.setCommandServerPort(getStationDetails().getCommandServerPort());
		pdcSystem.setNetworkProtocol(getStationDetails().getPdcStreamType());
		pdcSystem.setMaxWait(getStationDetails().getPdcMaxWait());
		pdcSystem.setDataRate(getStationDetails().getPmuDataRate());
		pdcSystem.setLineFreq(getStationDetails().getSystemLineFrequency());
		pdcSystem.setVerbose(8);
		pdcSystem.setVerboseConcentrator(8);
		if (getStationDetails().getPdcStreamType().equalsIgnoreCase("tcp"))
		{
			pdcSystem.setServerPort(getStationDetails().getPdcTcpPort());
		}
		else
		{
			if (pdcSystem.getUdpServers() != null)
			{
				pdcSystem.unsetUdpServers();
			}
			logger.debug("list of udp server ports "+getStationDetails().getLstOfUDPserverPorts());
//			int[] udpPortsArray = ArrayUtils.toPrimitive(getStationDetails().getLstOfUDPserverPorts().toArray(new Integer[getStationDetails().getLstOfUDPserverPorts().size()]));
			Integer udpPort;
//			int i = 0;
			UdpServers udpServers = pdcSystem.addNewUdpServers();
			for (Iterator<Integer> iterator = getStationDetails().getLstOfUDPserverPorts().iterator(); iterator.hasNext();) {
				udpPort = iterator.next();
				if (udpPort != null)
				{
					udpServers.addUdpServerPort(udpPort);
				}
			}
		}
		return substation;
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
	@SkipValidation
	  public String updateHeaderDetails()
	  {
		  stationDetails = (StationDTO)session.get("stationDetails");
//		  logger.debug("\t\t\t\tstation id "+((StationDTO)session.get("stationDetails")).getSystemStationId());
		  return SUCCESS;
	  }
	  
	  @SkipValidation
	  public String cancel()
	  {
		  stationDetails = (StationDTO) session.get("stationDetails");
		  String logoutMessageLog = "";
		  logger.debug("Cancel the create action");
		  if (session != null)
		  {
			  ServletContext context = ServletActionContext.getServletContext();
			  Set<UsersDTO> logins = (Set<UsersDTO>) context.getAttribute("logins");
//			  userDto = (UsersDTO) session.get("userDetails");
			  if (logins != null)
			  {
				  logger.debug("Logins not null."+logins.toString()+" Does it contain user ? "+logins.contains(getUserDto())+" Remove user "+getUserDto());
//				  logger.info("User "+getUserDto().getUserName() +" was logged out from the machine with ip address "+getUserDto().getIpAddress());
				  logoutMessageLog= "User "+getUserDto().getUserName() +" was logged out from the machine with ip address "+getUserDto().getIpAddress();
				  logins.remove(getUserDto());
			  }
			  session.clear();
			  if (logins != null)
			  {
				  logger.debug("After session clear. Logins not null."+logins.toString()+" Does it contain user ? "+logins.contains(getUserDto())+" Remove user "+getUserDto());
				  context.setAttribute("logins", logins);
			  }

		  }
		  
		  logger.debug("Session timed out message from webpage "+getCustomActionError()+" User details "+userDto);
		  if (getCustomActionError()!=null && !getCustomActionError().isEmpty())
		  {
			  addActionError(getCustomActionError());
//			  logger.info("User "+(getUserDto() != null? getUserDto().getUserName():"")+" was logged out due to session time out from last logged in station "+(stationDetails!=null?stationDetails.getStationDisplayName():""));
			  logoutMessageLog = "User "+(getUserDto() != null? getUserDto().getUserName():"")+" was logged out due to session time out from last logged in station "+(stationDetails!=null?stationDetails.getStationDisplayName():"");
		  }
		  else
		  {
//			  logger.info("User "+(getUserDto() != null? getUserDto().getUserName():"")+" logged out from last logged in station "+(stationDetails!=null?stationDetails.getStationDisplayName():""));
			  logoutMessageLog = "User "+(getUserDto() != null? getUserDto().getUserName():"")+" logged out from last logged in station "+(stationDetails!=null?stationDetails.getStationDisplayName():"");
		  }
		  logger.info(logoutMessageLog);
		  return "Cancel";
	  }
	  
	  @SkipValidation
	  public String back()
	  {
		  String returnValue;
		  logger.debug("Back to station list ");
		  if (accessMode == null || !accessMode.equals("Edit"))
		  {
			  logger.info("User "+(getUserDto() != null? getUserDto().getUserName():"")+" cancelled new station creation and back in the Home screen");
			  returnValue = "Back";
			  
		  }
		  else
		  {
			  logger.debug("\t\t\t\tstation id "+((StationDTO)session.get("stationDetails")).getSystemStationId());
			  logger.debug("station"+((StationDTO)session.get("stationDetails")));
			  logger.info("User "+(getUserDto() != null? getUserDto().getUserName():"")+" is back in the display DFRs Screen");
			  returnValue = "BackToDisplayDFR";
		  }
		  return returnValue;
	  }
	  
	  @SuppressWarnings("unchecked")
	@SkipValidation
	  public String backTo()
	  {
		  logger.debug("Returngin from backTo method...");
		  mapLineFrequencies = (Map<String, String>) session.get("mapLineFrequencies");
//		  lstSampleRate = (List<String>) session.get("lstSampleRate");
//		  lstLtSampleRate = (List<String>) session.get("lstLtSampleRate");
		  stationDetails = (StationDTO) session.get("stationDetails");
		  return SUCCESS; 
	  }

	  @SkipValidation
	  public String backToConfigure()
	  {
		  stationDetails = (StationDTO) session.get("stationDetails");
		  if (accessMode == null || !accessMode.equals("Edit"))
		  {
			  minimumDFRCount = ((Integer)session.get("minimumDFRCount")).intValue();
		  }
		  logger.debug("Returngin from backToConfigure method...");
		  return SUCCESS; 
	  }

	  public String nextToDisplayDfr()
	  {
		  stationDetails = getStationDetails();
		  
		  logger.debug("accessMode... "+accessMode);
		  logger.debug("Station info obje... "+getStationDetails());
		  logger.debug("Station Name... "+getStationDetails().getSystemStationName());
		  logger.debug("Analog channel cnt..."+getStationDetails().getSystemAnalogChannelsCount());
		  logger.debug("PS... "+getStationDetails().getPs());
		  logger.debug("Station config status "+stationDetails.getConfigStatus());
		  if (accessMode.equalsIgnoreCase("EDIT"))
		  {
			  if (stationDetails.getConfigStatus() == null)
			  {
				  stationDetails.setConfigStatus(((StationDTO)session.get("stationDetails")).getConfigStatus());
			  }
		  }
		  logger.debug("After from session Station config status "+stationDetails.getConfigStatus());
//		  session.put("stationDetails", stationDetails);
		  session.put("DFRsList", lstDfrDTO);
		  return "Next";
		  
	  }
	  
		public String configureChannels() {
			String returnValue = SUCCESS;
			logger.debug("Station details from configureChannels ParentZoneId"+stationDetails.getParentZoneId()+" lst of Alarms Relay Mappings: "+stationDetails.getLstAlarmRelaysMapping());
			saveStationDetailsIntoXml();
			if (stationDetails.getSystemAnalogChannelsCount() > 0) {
				setBooAnalog(true);
				
				returnValue = "Analogs";
				logger.info("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is in the Analogs configuration screen");
			} 
			else
			{
				setBooAnalog(false);
				returnValue = "DirectToEvents";
				logger.info("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is in the Events configuration screen");
			}
			if (stationDetails.getSystemDigitalChannelsCount() > 0)
			{
				setBooDigital(true);
			}
			else
			{
				setBooDigital(false);
			}
//			stationDetails.setConfigXml(((StationDTO) session.get("stationDetails")).getConfigXml());
			logger.debug("StationDTO ParentZoneId..."+stationDetails.getParentZoneId());
			// 10-Nov-2021 - IMplementing hierarchy - Parent zone id update
			M9kUtils.setStationDetails(stationDetails);
			session.put("booAnalog", isBooAnalog());
			session.put("booDigital", isBooDigital());
			session.put("stationDetails", stationDetails);
		logger.debug("Is digital? "+booDigital);
//		logger.debug("station put in session "+stationDetails);
		return returnValue;
		}

		  private String saveStationDetailsIntoXml()
		  {
			String returnValue = SUCCESS;
			mapXMLDfrs = new HashMap<String, DFR>();
			  logger.debug("DFRDTO list after clicking Next.."+getLstDfrDTO()+" alarms "+stationDetails.getLstAlarmRelaysMapping()+" trigger duration "+stationDetails.getTriggerDuration());
			  session.put("DFRsList",lstDfrDTO);
			  if (accessMode.equals("Edit")) // Save in Edit mode
			  {
				  logger.debug("stationDetails config status "+stationDetails.getConfigStatus());
				  SubStation currentSubstation = (SubStation) session.get("selectedSubstation");
				  currentSubstation.setName(stationDetails.getSystemStationName());
				  currentSubstation.setId(stationDetails.getSystemStationId());
//				  currentSubstation.setHost(stationDetails.getIpAddress());
				  if (currentSubstation.getPmus() != null)
				  {
					  Pmu pmu = currentSubstation.getPmus().getPmuArray(0);
					  pmu.setPmuId(stationDetails.getPmuId());
					  pmu.setPmuPort(stationDetails.getPmuPort());
					  pmu.setUdpPort(stationDetails.getPmuUdpPort());
					  pmu.setPmuStreamType(stationDetails.getPmuStreamType());
					  pmu.setPmuPhasorMode(stationDetails.getPmuPhasorMode());
					  pmu.setPmuDataRate(stationDetails.getPmuDataRate());
					// Changed for PDC implementation 4-Oct-2013
	//				  if (stationDetails.getPmuStatus().equalsIgnoreCase(M9kConstants.ENABLE))
					  if (getStationDetails().isPmuEnabled())
					  // End
					  {
						  pmu.setPmuEnable(M9kConstants.ENABLE_ONE);
						  currentSubstation = createPdcXmlElement(currentSubstation);
					  }
					  else
					  {
						  pmu.setPmuEnable(M9kConstants.DISABLE_ZERO);
						  if (currentSubstation.getPdc() != null)
						  {
							  currentSubstation.unsetPdc();
						  }
					  }
				  }
				  else
					{
						initializePmuForChassis();
						createDefaultGolbalPmus(currentSubstation);
					}

				  // START: 29-12-2022 - Moved the code to M9kAdvancedSettingsAction class
				  //				  saveRelayMappingSettingsToXML(currentSubstation);
//				  // START: 01-July-2022 - Save Trigger duration
//				  StationProperties stationProperties = currentSubstation.getStationProperties();
//					if (stationProperties == null)
//					{
//						stationProperties = currentSubstation.addNewStationProperties();
//					}
//					stationProperties.setTriggerDuration(stationDetails.getTriggerDuration());
//				// END: 01-July-2022
//					// START: 11-Sept-2022
//					stationProperties.setContOscDaysToRetain(stationDetails.getContOscDaysToRetain());
//					stationProperties.setContMeasurementsDaysToRetain(stationDetails.getContMeasurementsDaysToRetain());
//					// END: 11-Sept-2022
				  // END: 29-12-2022
				  
				  DFR xmlConfigDfr = null;
				  for (int i = 0; i < getLstDfrDTO().size(); i++) {
					  lstDfrDTO.get(i).setDfrName(getLstDfrDTO().get(i).getDfrName());
					  lstDfrDTO.get(i).setDfrId(getLstDfrDTO().get(i).getDfrId());
					  lstDfrDTO.get(i).setIpAddress(getLstDfrDTO().get(i).getIpAddress());
					  xmlConfigDfr = currentSubstation.getDFRs().getDFRArray(i);
					 logger.debug("adding a new DFR... "+i+" size: "+currentSubstation.getDFRs().sizeOfDFRArray());

						xmlConfigDfr.setId(lstDfrDTO.get(i).getDfrId());
						xmlConfigDfr.setIPAddress(lstDfrDTO.get(i).getIpAddress());
						  com.usi.SystemDocument.System systemProperties = xmlConfigDfr.getSystem();
							systemProperties.setSampleRate(stationDetails.getSystemSampleRate());
							systemProperties.setLineFrequency(stationDetails.getSystemLineFrequency());
							systemProperties.setExportRate(stationDetails.getExportRate());
							systemProperties.setDfrId(getLstDfrDTO().get(i).getDfrId());
							systemProperties.setRecordingDeviceId(stationDetails.getSystemRecordingDeviceId());
							systemProperties.setPrefaultTime(stationDetails.getSystemPrefaultTime());
							systemProperties.setPostfaultTime(stationDetails.getSystemPostfaultTime());
							systemProperties.setLtrPrefaultTime(stationDetails.getSystemLtrPrefaultTime());
							systemProperties.setLtrPostfaultTime(stationDetails.getSystemLtrPostfaultTime());
							systemProperties.setChatterLimit(stationDetails.getChatterLimit());
							systemProperties.setChatterRate(stationDetails.getChatterRate());
							systemProperties.setTriggerLimit(stationDetails.getTriggerLimit());
							systemProperties.setExportAnalogBufferTime(config.getInt("exportAnalogBufferTime", 20));
//							systemProperties.setExportAnalogBufferTime(60);
							systemProperties.setExportAnalogSampleRate(stationDetails.getSystemLongTermSampleRate());
							systemProperties.setExportBufferTime(config.getInt("exportBufferTime",20));
							systemProperties.setAntiAliasFilterDelay(config.getDouble("antiAliasFilterDelay",41.04555));
							systemProperties.setDatabaseDataType(M9kConstants.Binary.toUpperCase());
							systemProperties.setDatabaseConnectString(getDbConnectionString());
							systemProperties.setPs(stationDetails.getPs());
							systemProperties.setSignalTooLowFactor(stationDetails.getSignalTooLowFactor());
							// START: 07-Jul-2016 - Modified to implement event debounce
							if (stationDetails.getEventDebounce() != null)
							{
								systemProperties.setEventDebounce(stationDetails.getEventDebounce());
							}
							else
							{
								systemProperties.setEventDebounce(0.0);
							}
							// END: 07-Jul-2016
							
							// START: 06-MAR-2018 - Configurable wetting voltage
							systemProperties.setMonitorWettingVoltage(stationDetails.getWettingVoltageMonitor());
							// END: 06-Mar-2018
							mapXMLDfrs.put(getLstDfrDTO().get(i).getDfrName(), xmlConfigDfr);
				  }
				  
				  session.put("mapXMLDfrs",mapXMLDfrs);
			  }		  
			return returnValue;
		  }

	  private List<DfrDTO> createDefaultDFRs(int dfrCnt)
	  {
		  List<DfrDTO> lstDfrs = new ArrayList<DfrDTO>(dfrCnt);
		  DfrDTO dfrDto;
		  double totalAnalogCnt = stationDetails.getSystemAnalogChannelsCount();
		  double totalDigitalCnt = stationDetails.getSystemDigitalChannelsCount();
		  int totalBoardsPerDfr = M9kConstants.NO_OF_BOARDS_PER_DFR;
		  int noOfAnalogBoardsReqd;
		  int noOfDigitalBoardsReqd;
		  String dfrIpAddress = config.getString("dfrIpAddressStart", "192.168.1.101");
		  String ipPrefix = dfrIpAddress.substring(0,dfrIpAddress.lastIndexOf(".")+1);
		  int ipAddressCount = Integer.parseInt(dfrIpAddress.substring(dfrIpAddress.lastIndexOf(".")+1));
		  for (int i = 0; i < dfrCnt; i++) {
			  logger.debug("DfrDTO() Called from createDefaultDFRs method..."+i);
			  dfrDto = new DfrDTO();
			  dfrDto.setDfrName("DFR"+(i+1)); 
			  dfrDto.setDfrId(i+1);
			  dfrDto.setIpAddress(ipPrefix+(ipAddressCount+i));
			  dfrDto.setStatus("Incomplete");
			  totalBoardsPerDfr = M9kConstants.NO_OF_BOARDS_PER_DFR;
			  if (totalAnalogCnt > 0 && totalBoardsPerDfr > 0)
			  {
				// START: 02-Jun-2020 - M9kMini - implementation
				  // Check for ANEV
				  if ((totalAnalogCnt % M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD) > 0 || (totalDigitalCnt % M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD) > 0)
				  {
					  logger.debug("M9kMini configuraion found total analog cnt "+totalAnalogCnt +" total digital cnt "+totalDigitalCnt);
					  dfrDto.setAnevCard(true);
					  // Set Analogs for ANEV
					  int totalSlotsUsed = 1;
					  // ANEV board for slot 1
					  int totalAnalogsChannelsConfigured = (int)M9kConstants.NO_OF_ANALOG_CHNLS_PER_ANEV_BOARD;
					  int totalDigitalChannelsConfigured = (int)M9kConstants.NO_OF_DIGITAL_CHNLS_PER_ANEV_BOARD;
					  totalAnalogCnt -= totalAnalogsChannelsConfigured;
					  totalDigitalCnt -= totalDigitalChannelsConfigured;
					  dfrDto.setAnalogChnlCnt(totalAnalogsChannelsConfigured);
					  dfrDto.getChnlConfigDropDownList().setAnalogCnt(totalAnalogsChannelsConfigured);
					  dfrDto.setDigitalChnlCnt(totalDigitalChannelsConfigured);
					  dfrDto.getChnlConfigDropDownList().setDigitalCnt(totalDigitalChannelsConfigured);
					  
					  while ((totalAnalogCnt > 0 || totalDigitalCnt >0) && totalSlotsUsed < 4)
					  {
						  logger.debug("Inside while total slot used "+totalSlotsUsed);
						  logger.debug(" totalAnalogCnt "+totalAnalogCnt+" total analogs configured "+totalAnalogsChannelsConfigured);
						  logger.debug(" totalDigitalsCnt "+totalDigitalCnt+" total digitals configured "+totalDigitalChannelsConfigured);
						  if (totalAnalogCnt >= M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD)
						  {
							  totalAnalogsChannelsConfigured += M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD;
							  totalAnalogCnt -= M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD;
							  dfrDto.setAnalogChnlCnt(totalAnalogsChannelsConfigured);
							  dfrDto.getChnlConfigDropDownList().setAnalogCnt(totalAnalogsChannelsConfigured);
							  logger.debug(" IF CONDN: totalAnalogCnt "+totalAnalogCnt+" total analogs configured "+totalAnalogsChannelsConfigured);
						  }
						  else 
						  {
							  totalDigitalChannelsConfigured += M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD;
							  totalDigitalCnt -= M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD;
							  dfrDto.setDigitalChnlCnt(totalDigitalChannelsConfigured);
							  dfrDto.getChnlConfigDropDownList().setDigitalCnt(totalDigitalChannelsConfigured);
							  logger.debug(" ELSE CONDN: totalDigitalsCnt "+totalDigitalCnt+" total digitals configured "+totalDigitalChannelsConfigured);
						  }
						  totalSlotsUsed++;
					  }
					  logger.debug("MINI: Aanlog count set..."+dfrDto.getChnlConfigDropDownList().getAnalogCnt());
					  logger.debug("MINI: Digital count set..."+dfrDto.getChnlConfigDropDownList().getDigitalCnt());
					  lstDfrs.add(dfrDto);
					  
					  continue;
				  }
				// END: 18-Jun-2020 - M9kMini - implementation

				  noOfAnalogBoardsReqd = (int)Math.ceil(totalAnalogCnt/M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD);
				  if (noOfAnalogBoardsReqd < totalBoardsPerDfr)
				  {
					  logger.debug("noOfAnalogBoardsReqd.."+noOfAnalogBoardsReqd+" totalAnalogCnt..."+totalAnalogCnt);
					  logger.debug("totalAnalogCnt.."+totalAnalogCnt);
					  dfrDto.setAnalogChnlCnt((int) totalAnalogCnt);
					  totalAnalogCnt = 0;
					  totalBoardsPerDfr-=noOfAnalogBoardsReqd;
					  logger.debug("totalBoardsPerDfr.."+totalBoardsPerDfr);					  
				  }
				  else
				  {
					  dfrDto.setAnalogChnlCnt(totalBoardsPerDfr*M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD);
					  totalAnalogCnt-=(totalBoardsPerDfr*M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD);
					  totalBoardsPerDfr=0;	
				  }
				  dfrDto.getChnlConfigDropDownList().setAnalogCnt((M9kConstants.NO_OF_BOARDS_PER_DFR-totalBoardsPerDfr)*M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD);
			  }
			  else
			  {
				  dfrDto.getChnlConfigDropDownList().setAnalogCnt(0);
				  dfrDto.setAnalogChnlCnt(0);
			  }

			  if (totalBoardsPerDfr > 0 && totalDigitalCnt > 0 )
			  {
				  noOfDigitalBoardsReqd = (int)Math.ceil(totalDigitalCnt/M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD);
				  if (noOfDigitalBoardsReqd < totalBoardsPerDfr)
				  {
					  dfrDto.getChnlConfigDropDownList().setDigitalCnt(noOfDigitalBoardsReqd*M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD);
					  dfrDto.setDigitalChnlCnt((int) totalDigitalCnt);
					  totalDigitalCnt=0;
					  totalBoardsPerDfr-=noOfDigitalBoardsReqd;
				  }
				  else
				  {
					  dfrDto.getChnlConfigDropDownList().setDigitalCnt(totalBoardsPerDfr*M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD);
					  dfrDto.setDigitalChnlCnt(totalBoardsPerDfr*M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD);
					  totalDigitalCnt-=(totalBoardsPerDfr*M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD);
					  totalBoardsPerDfr=0;					  
				  }
			  }
			  else
			  {
				  dfrDto.getChnlConfigDropDownList().setDigitalCnt(0);
				  dfrDto.setDigitalChnlCnt(0);
			  }
			  logger.debug("Digital count set..."+dfrDto.getChnlConfigDropDownList().getDigitalCnt());
			  lstDfrs.add(dfrDto); 
		  }
		  return lstDfrs;
	  }
	  
	  @SkipValidation
	  @Action(value = "addNewDfrEdit", results = {
			    @Result(name="success",location="/jsp/addNewDFR.jsp")})
	  public String addNewDFR()
	  {
		  String dfrIpAddress = config.getString("dfrIpAddressStart", "192.168.1.101");
		  String ipPrefix = dfrIpAddress.substring(0,dfrIpAddress.lastIndexOf(".")+1);
		  int ipAddressCount = Integer.parseInt(dfrIpAddress.substring(dfrIpAddress.lastIndexOf(".")+1));

		  stationDetails = (StationDTO) session.get("stationDetails");
		  logger.debug(" DfrDTO() Called from addNewDFR method...");
		  newlyAddedDfr = new DfrDTO();
		  newlyAddedDfr.setDfrName("DFR"+(lstDfrDTO.size()+1)); 
		  newlyAddedDfr.setDfrId((lstDfrDTO.size()+1));
		  newlyAddedDfr.setIpAddress(ipPrefix+(ipAddressCount+lstDfrDTO.size()));
		  newlyAddedDfr.getChnlConfigDropDownList().setSelectedValue("0A-32D");
		  newlyAddedDfr.setNewlyAdded(true);
		  getLstDfrDTO().add(newlyAddedDfr);
		  stationDetails.setDfrAddedOrRemoved(true);
		  logger.debug("total no of dfrs..."+lstDfrDTO);
		  if (lstDfrDTO != null)
		  {
			  int analogCnt = 0;
			  int digitalCnt = 0;
			  for (Iterator<DfrDTO> iterator = lstDfrDTO.iterator(); iterator
					.hasNext();) {
				  DfrDTO dfrDTO = iterator.next();
				  logger.debug("DFR Name... "+dfrDTO.getDfrName());
				  logger.debug("DFR ipAddress... "+dfrDTO.getIpAddress());
				  logger.debug("DFR selected Channels... "+dfrDTO.getChnlConfigDropDownList().getSelectedValue());
				  
				  analogCnt+=dfrDTO.getChnlConfigDropDownList().getAnalogCnt();;
				  digitalCnt+=dfrDTO.getChnlConfigDropDownList().getDigitalCnt();;
			}
			  logger.debug("Total dfrDTO analog cnt..."+analogCnt);
			  logger.debug("Total dfrDTO digital cnt..."+digitalCnt);
		  }
		  if (session.get("minimumDFRCount") != null)
		  {
			  minimumDFRCount = ((Integer)session.get("minimumDFRCount")).intValue();
		  }
		  newlyAddedDfrIndex = getLstDfrDTO().size()-1;;
		  session.put("DFRsList",getLstDfrDTO());
		  return SUCCESS;

	  }
	  
	  @SkipValidation
	  @Action(value = "deleteNewDfrEdit", results = {
			    @Result(name="success",location="/jsp/updatedDfrsList.jsp")})
	  public String deleteNewDFR()
	  {
		  boolean isExistingDfrDeleted = false;
		  DfrDTO deletedDfr;
		  int nextAnalogChannelStart = -1;
		  int nextDigitalChannelStart = -1;
		  logger.debug("delete index after delete action... "+getDeleteIndex());
		  SubStation currentSubstation = (SubStation) session.get("selectedSubstation");
		  deletedDfr = getLstDfrDTO().get(getDeleteIndex());
		  getLstDfrDTO().remove(getDeleteIndex());
		  stationDetails.setSystemAnalogChannelsCount(stationDetails.getSystemAnalogChannelsCount() - deletedDfr.getAnalogChnlCnt());
		  stationDetails.setSystemDigitalChannelsCount(stationDetails.getSystemDigitalChannelsCount() - deletedDfr.getDigitalChnlCnt());
		  // Get the available start analog index and digital index from the deleted chassis
		  if (deletedDfr.getAnalogChannelStart() > 0)
		  {
			  nextAnalogChannelStart = deletedDfr.getAnalogChannelStart();
		  }
		  if (deletedDfr.getDigitalChannelStart() > 0)
		  {
			  nextDigitalChannelStart = deletedDfr.getDigitalChannelStart();
		  }
		  if (getDeleteIndex() == 0)
		  {
			  nextAnalogChannelStart = 1;
			  nextDigitalChannelStart = 1;
		  }
		  else
		  {
			  // Check if it's the last chassis that was deleted
			  int iIndex = getDeleteIndex(); 
			  if (iIndex == getLstDfrDTO().size())  
			  {
				  iIndex = getDeleteIndex() -1 ;
			  }
			  // if we don't have next starting index then Loop back to get the last analog and digital channel counts from the available chassis before the deleted one  
			  if (nextAnalogChannelStart == -1 || nextDigitalChannelStart == -1)
			  {
				  for (int i = iIndex;i >= 0;i--)
				  {
					  if (getLstDfrDTO().get(i).getAnalogChnlCnt() > 0)
					  {
						  if (nextAnalogChannelStart == -1)
						  {
							  nextAnalogChannelStart = getLstDfrDTO().get(i).getAnalogChannelEnd()+1; 
						  }
						  logger.debug("DELETE-DEBUG: nextAnalogChannelStart after delete chassis "+nextAnalogChannelStart);
					  }
					  if (getLstDfrDTO().get(i).getDigitalChnlCnt() > 0)
					  {
						  if (nextDigitalChannelStart == -1)
						  {
							  nextDigitalChannelStart = getLstDfrDTO().get(i).getDigitalChannelEnd()+1;
						  }
						  logger.debug("DELETE-DEBUG: nextDigitalChannelStart after delete chassis "+nextDigitalChannelStart);
					  }
					  if (nextAnalogChannelStart > -1 &&  nextDigitalChannelStart > -1)
					  {
						  break;
					  }
				  }
			  }
			  if (nextAnalogChannelStart == -1)
			  {
				  nextAnalogChannelStart = 1;
			  }
			  if (nextDigitalChannelStart == -1)
			  {
				  nextDigitalChannelStart = 1;
			  }
		  }
		  logger.debug("DELETE-DEBUG: Last analog count for chassis from the deleted chassis "+nextAnalogChannelStart);
		  logger.debug("DELETE-DEBUG: Last digital count for chassis from the deleted chassis "+nextDigitalChannelStart);
		  if (!deletedDfr.isNewlyAdded() && getDeleteIndex() < stationDetails.getTotalDfrsConfigured())
		  {
			  isExistingDfrDeleted = true;
//			  if (!stationDetails.isDfrAddedOrRemoved())
//			  {
				  stationDetails.setDfrAddedOrRemoved(true);
//				  session.remove("stationDetails");
//				  session.put("stationDetails", stationDetails);
//			  }
			  logger.debug("DELETE-DEBUG: Before delete: size of measurements"+currentSubstation.getMeasurements().sizeOfMeasurementArray());
			  currentSubstation.getDFRs().removeDFR(getDeleteIndex());
			  for (int i = 0; i < currentSubstation.getMeasurements().sizeOfMeasurementArray(); i++) {
//				  logger.debug("DELETE-DEBUG: index i "+i+" measurement dfr id "+currentSubstation.getMeasurements().getMeasurementArray(i).getDfr());
				  if (currentSubstation.getMeasurements().getMeasurementArray(i).getDfr() == (getDeleteIndex()+1))
				  {
					  currentSubstation.getMeasurements().removeMeasurement(i);
					  --i;
				  }
			}
			  logger.debug("DELETE-DEBUG: After delete: size of measurements"+currentSubstation.getMeasurements().sizeOfMeasurementArray());
			  logger.debug("DELETE-DEBUG: Before delete: size of PMUs "+currentSubstation.getPmus().getPmuArray(0).getPmuInputs().sizeOfPmuInputArray());
			  logger.debug("DELETE-DEBUG: Before delete: size of PMUs"+currentSubstation.getPmus().getPmuArray(0).xmlText(M9kXMLUtils.getXmlOptions()));
			  for (int i = 0; i < currentSubstation.getPmus().getPmuArray(0).getPmuInputs().sizeOfPmuInputArray(); i++) {
				  logger.debug("DELETE-DEBUG: PMUs DFR  "+currentSubstation.getPmus().getPmuArray(0).getPmuInputs().getPmuInputArray(i).getDfr());
				if (currentSubstation.getPmus().getPmuArray(0).getPmuInputs().getPmuInputArray(i).getDfr() == (getDeleteIndex()+1))
				{
					currentSubstation.getPmus().getPmuArray(0).getPmuInputs().removePmuInput(i);
					--i;
				}
			  }
				logger.debug("DELETE-DEBUG: After delete: size of PMUs"+currentSubstation.getPmus().getPmuArray(0).getPmuInputs().sizeOfPmuInputArray());
		  }
		  for (int i = getDeleteIndex();i < getLstDfrDTO().size();i++)
		  {
			  DfrDTO dfrDto = getLstDfrDTO().get(i);
			  String dfrIpAddress = dfrDto.getIpAddress();
			  String ipPrefix = dfrIpAddress.substring(0,dfrIpAddress.lastIndexOf(".")+1);
			  int ipAddressCount = Integer.parseInt(dfrIpAddress.substring(dfrIpAddress.lastIndexOf(".")+1));

			  dfrDto.setDfrName("DFR"+(i+1)); 
			  dfrDto.setDfrId((i+1));
			  dfrDto.setIpAddress(ipPrefix+(ipAddressCount-1));
			  if (dfrDto.getAnalogChnlCnt() > 0)
			  {
				  dfrDto.setAnalogChannelStart(nextAnalogChannelStart);
				  dfrDto.setAnalogChannelEnd(nextAnalogChannelStart+dfrDto.getAnalogChnlCnt()-1);
				  nextAnalogChannelStart += dfrDto.getAnalogChnlCnt();
			  }
			  if (dfrDto.getDigitalChnlCnt() > 0)
			  {
				  logger.debug("DELETE-DEBUG: nextDigitalChannelStart "+nextDigitalChannelStart);
				  dfrDto.setDigitalChannelStart(nextDigitalChannelStart);
				  dfrDto.setDigitalChannelEnd(nextDigitalChannelStart+dfrDto.getDigitalChnlCnt()-1);
				  nextDigitalChannelStart+=dfrDto.getDigitalChnlCnt();
				  logger.debug("DELETE-DEBUG: New nextDigitalChannelStart "+nextDigitalChannelStart);
			  }
			  // Local PMU reference to dfr updated
			  if (isExistingDfrDeleted)
			  {
				  currentSubstation.getDFRs().getDFRArray(i).getDataPool().getAlgorithms().getPmus().getPmuArray(0).setName(dfrDto.getDfrName());
				  currentSubstation.getDFRs().getDFRArray(i).getDataPool().getAlgorithms().getPmus().getPmuArray(0).setPmuName(dfrDto.getDfrName());
				  currentSubstation.getDFRs().getDFRArray(i).getDataPool().getAlgorithms().getPmus().getPmuArray(0).setPmuId((ipAddressCount-1));
			  }
			  logger.debug("DELETE-DEBUG:from dfrDto Dfr Name "+dfrDto.getDfrName()+" Analog Chanl start "+dfrDto.getAnalogChannelStart()+" Digital chnl start "+dfrDto.getDigitalChannelStart());
			  logger.debug("DELETE-DEBUG:from list Dfr Name "+getLstDfrDTO().get(i).getDfrName()+" Analog Chanl start "+getLstDfrDTO().get(i).getAnalogChannelStart()+" Digital chnl start "+getLstDfrDTO().get(i).getDigitalChannelStart());
		  }
		  // If existing DFR deleted update the measurement and PMU references for the rest of the chassis
		  if (isExistingDfrDeleted)
		  {
			  for (int i = 0; i < currentSubstation.getMeasurements().sizeOfMeasurementArray(); i++) {
				  if (currentSubstation.getMeasurements().getMeasurementArray(i).getDfr() > (getDeleteIndex()+1))
				  {
					  currentSubstation.getMeasurements().getMeasurementArray(i).setDfr(currentSubstation.getMeasurements().getMeasurementArray(i).getDfr()-1);
				  }
			  }
			  for (int i = 0; i < currentSubstation.getPmus().getPmuArray(0).sizeOfPmuInputArray(); i++) {
				  if (currentSubstation.getPmus().getPmuArray(0).getPmuInputArray(i).getDfr() > (getDeleteIndex()+1))
				  {
					  currentSubstation.getPmus().getPmuArray(0).getPmuInputArray(i).setDfr(currentSubstation.getPmus().getPmuArray(0).getPmuInputArray(i).getDfr()-1);
				  }
			 }
			  session.remove("selectedSubstation");
			  session.put("selectedSubstation", currentSubstation);			  
		  }

		  logger.debug("DELETE-DEBUG:Current substation ... "+getLstDfrDTO());
		  session.put("DFRsList",getLstDfrDTO());
		  return SUCCESS;
	  }
	  

	  private void populatePdcFromXML(SubStation substation) {
			Pdc pdc = substation.getPdc();
			if (pdc != null && pdc.getSystem() != null)
			{
				System pdcSystem = pdc.getSystem();
				stationDetails.setPdcId(pdcSystem.getStationId()); // Station Id is used to store the PDC id
				// 16-Nov-2022 - Read the port from m9k-master properties file all the time
//				stationDetails.setCommandServerPort(pdcSystem.getCommandServerPort());
				stationDetails.setPdcStreamType(pdcSystem.getNetworkProtocol());
				stationDetails.setPdcMaxWait(pdcSystem.getMaxWait());
				stationDetails.setPmuDataRate(pdcSystem.getDataRate());
				stationDetails.setPdcTcpPort(pdcSystem.getServerPort());
				UdpServers udpServers = pdcSystem.getUdpServers();
				if (udpServers != null)
				{
					logger.debug("\n\n\n\t\t\t\t\n\n\n list of udp servers "+udpServers.xmlText());
					if (stationDetails.getLstOfUDPserverPorts() == null)
					{
						stationDetails.setLstOfUDPserverPorts(new ArrayList<Integer>(udpServers.sizeOfUdpServerPortArray()));
					}
					logger.debug("\n\n\n\t\t\t\t\n\n\n list of udp servers array size "+udpServers.sizeOfUdpServerPortArray());
					for (int i = 0; i < udpServers.sizeOfUdpServerPortArray(); i++) {
						stationDetails.getLstOfUDPserverPorts().add(udpServers.getUdpServerPortArray(i));
					}
				}
			}
		}
	  
	  @SkipValidation
	  @SuppressWarnings("unchecked")
	public String editStation() throws M9000Exception
	  {
		  String returnValue = SUCCESS;
		  accessMode = "Edit";
		  int totalTriggersConfigured = 0;
		  int totalMeasurements = 0;
		  int totalEventsConfigured = 0;
		  int totalLineGroupsConfigured = 0;
		  List<Integer> triggerIds = new ArrayList<Integer>();
		  List<Integer> measurementIds = new ArrayList<Integer>();
		  // 05-Apr-2021 - Total virtual Measurements
		  int totalVirtualMeasurements = M9kConstants.VIRTUAL_MEASUREMENT_OFFSET; // Let the ids start with 5000 to avoid measurements id conflict
		  List<Integer> virtualMeasurementIds = new ArrayList<Integer>();
		  List<Integer> exportIds = new ArrayList<Integer>();
		  List<Integer> lineGroupIds = new ArrayList<Integer>();
		  DFR dfrs[];
		  Triggers triggers;
		  Exports exports;
		  LineGroups lineGroups;
//		  M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
//		  StationDAO mysqlStationDao = m9kDAOFactory.getStationDAO();
		  logger.debug("Station id before "+Integer.parseInt(getStationId()));
		  UsersDTO userAlreadyEditing = isStationAlreadyInEditMode(Integer.parseInt(getStationId()));
		  stationDetails = (StationDTO) session.get("stationDetails");
		  logger.debug("User dto returned for User already editing "+userAlreadyEditing);
		  if (userAlreadyEditing != null)
		  {
			  addActionMessage("User "+userAlreadyEditing.getUserName()+" is already editing from a machine with IP Address "+userAlreadyEditing.getIpAddress()+". You have read-only access now." );
			  getUserDto().setRole(M9kConstants.GUEST);
			  getUserDto().setEditMode(true);
			  logger.info("User "+getUserDto().getUserName()+" is assigned Read-Only access to edit station "+getStationDetails().getStationDisplayName()+" as User "+userAlreadyEditing.getUserName()+" is already editing from a machine with IP Address "+userAlreadyEditing.getIpAddress()+".");
		  }
		  else
		  {
			  getUserDto().setEditedStationId(Integer.parseInt(getStationId()));
			  if (getUserDto().getRole().equalsIgnoreCase(M9kConstants.ADMIN))
			  {
				  getUserDto().setEditMode(true);
			  }
			  else
			  {
				  getUserDto().setEditMode(false);
			  }
			  session.put("userDetails", getUserDto());
			  logger.debug("User logs in for first time "+getUserDto());
			  logger.info("User "+getUserDto().getUserName()+" is about to edit the station "+getStationDetails().getStationDisplayName());
		  }
		  session.put("accessMode", accessMode);
//		  logger.debug("SESION updated with user details? "+(UsersDTO) session.get("userDetails"));
//		  File stationConfigFile;
//		  int stationId = 0;
//		  if (getStationId() != null && !getStationId().isEmpty())
//		  {
//			  stationId = Integer.parseInt(getStationId());
//		  }
		  
		  lstAvailableStations = (List<StationDTO>) session.get("lstAvailableStations");
//		  stationDetails = lstAvailableStations.get(getStationIndex()-1);
		  // START: 06-Mar-2015 Fetch station details from db instead of memory
//		  stationDetails = getStationDetails(Integer.parseInt(getStationId()));
		  try {
				stationDetails = stationDao.getStationsDetails(Integer.parseInt(getStationId()));
			} catch (NumberFormatException e) {
				logger.error("Exception in fetching station details from database",e);
				throw new M9000Exception(e);
			} catch (M9000Exception e) {
				logger.error("Exception in fetching station details from database",e);
				throw new M9000Exception(e);
				} 
		  // END: 06-Mar-2015
//		  logger.debug("Station Details "+stationDetails);
//		  stationConfigFile =new File("c:/M9kConfig/"+stationDetails.getSystemStationId()+".xml");
//		  logger.debug("Selected Station File..."+stationConfigFile);
		  
		  try {
//			String configXml = mysqlStationDao.getConfigXml(stationId);
			String configXml = stationDetails.getConfigXml();
//			logger.debug("DELETE-DEBUG: ConfigXML from stationDetails after Edit"+configXml);
			M9000XmlConfig m9kConfig = new M9000XmlConfig(new StringReader(configXml));
			logger.debug("DELETE-DEBUG: Substation XML after Edit"+m9kConfig.getSubstation().getPmus().getPmuArray(0).xmlText(M9kXMLUtils.getXmlOptions()));
		    dfrs = m9kConfig.getSubstation().getDFRs().getDFRArray();

			session.put("selectedSubstation", m9kConfig.getSubstation());
//			session.put("configStatus", stationDetails.getConfigStatus());
			com.usi.SystemDocument.System systemDetails = m9kConfig.getSubstation().getDFRs().getDFRArray(0).getSystem();
//			logger.debug("system details from XML..."+systemDetails+" dfrs.length..."+dfrs.length+""+dfrs);
			for (int i = 0; i < dfrs.length; i++) {
//				logger.debug("Dfr count..."+i);
				exports = dfrs[i].getDataPool().getExports();
				if (exports != null && exports.sizeOfExportArray() > 0)
				{
					for (int j = 0; j < exports.sizeOfExportArray(); j++) {
						exportIds.add(exports.getExportArray(j).getId());						
					}
				}

				lineGroups = dfrs[i].getDataPool().getLineGroups();
				if (lineGroups != null && lineGroups.sizeOfLineGroupArray() > 0)
				{
					for (int j = 0; j < lineGroups.sizeOfLineGroupArray(); j++) {
						lineGroupIds.add(lineGroups.getLineGroupArray(j).getId());
//						logger.debug("Line Group Id added "+lineGroups.getLineGroupArray(j).getId()+ " for dfr "+dfrs[i].getSystem().getDfrId());
					}
				}
				if (dfrs[i].getDataPool().getAlgorithms() != null)
				{
					triggers = dfrs[i].getDataPool().getAlgorithms().getTriggers();
					if (triggers != null && triggers.sizeOfTriggerArray() > 0)
					{
						for (int j = 0; j < triggers.sizeOfTriggerArray(); j++) {
							if(triggers.getTriggerArray(j).getType()== null || !triggers.getTriggerArray(j).getType().equalsIgnoreCase("E"))
							{
								triggerIds.add(triggers.getTriggerArray(j).getChanId());
							}
						}
					}
				}
			}
			Measurements measurements = m9kConfig.getSubstation().getMeasurements();
			if (measurements != null && measurements.sizeOfMeasurementArray() > 0)
			{
				for (int i = 0; i < measurements.sizeOfMeasurementArray(); i++) {
					// Count total number of virtual measurements based on the offset
					if ((measurements.getMeasurementArray(i).getId() / M9kConstants.VIRTUAL_MEASUREMENT_OFFSET) > 0)
					{
						virtualMeasurementIds.add(measurements.getMeasurementArray(i).getId());
					}
					else
					{
						measurementIds.add(measurements.getMeasurementArray(i).getId());
					}
				}
				Collections.sort(measurementIds);
				totalMeasurements = measurementIds.get(measurementIds.size() - 1);
				if (virtualMeasurementIds.size() > 0)
				{
					Collections.sort(virtualMeasurementIds);
					totalVirtualMeasurements = virtualMeasurementIds.get(virtualMeasurementIds.size() - 1);
				}
				logger.debug("measurements Id last used "+totalMeasurements+" total virtual measurements used..."+totalVirtualMeasurements);
			}
			GroupTriggers groupTriggers = m9kConfig.getSubstation().getGroupTriggers();
			if (groupTriggers != null && groupTriggers.sizeOfGroupTriggerArray() > 0)
			{
				for (int j = 0; j < groupTriggers.sizeOfGroupTriggerArray(); j++) {
					triggerIds.add(groupTriggers.getGroupTriggerArray(j).getId());
				}
			}
			GlobalLineGroups globalLineGroups = m9kConfig.getSubstation().getGlobalLineGroups();
			if (globalLineGroups != null && globalLineGroups.sizeOfGlobalLineGroupArray() > 0)
			{
				for (int i = 0; i < globalLineGroups.sizeOfGlobalLineGroupArray(); i++) {
					lineGroupIds.add(globalLineGroups.getGlobalLineGroupArray(i).getId());
//					logger.debug("Global Line Group Id added "+globalLineGroups.getGlobalLineGroupArray(i).getId());
				}
			}
			if (!triggerIds.isEmpty())
			{
//				logger.debug("Trigger Ids...."+triggerIds);
				Collections.sort(triggerIds);
//				logger.debug("After sort Trigger Ids...."+triggerIds);
				totalTriggersConfigured = triggerIds.get(triggerIds.size()-1);				
			}
			if (!exportIds.isEmpty())
			{
				Collections.sort(exportIds);
				totalEventsConfigured = exportIds.get(exportIds.size()-1);				
			}
			if (!lineGroupIds.isEmpty())
			{
				Collections.sort(lineGroupIds);
				totalLineGroupsConfigured = lineGroupIds.get((lineGroupIds.size()-1));
				session.put("lineGroupExists", true);
				logger.debug("Line Group exists is set to true"+session.get("lineGroupExists")+" totalLineGroupsConfigured "+totalLineGroupsConfigured);
			}
			else
			{
				session.put("lineGroupExists", false);
			}
			
			
			logger.debug("totalTriggersConfigured in the remote id... "+totalTriggersConfigured);
			logger.debug("Line Groups id... "+totalLineGroupsConfigured);
			
			stationDetails.setTotalDfrsConfigured(m9kConfig.getSubstation().getDFRs().sizeOfDFRArray());
//			stationDetails.setSystemAnalogChannelsCount(systemDetails.getAnalogsCount());
//			stationDetails.setSystemDigitalChannelsCount(systemDetails.getDigitalsCount());
			stationDetails.setSystemSampleRate(systemDetails.getSampleRate());
			stationDetails.setSystemLongTermSampleRate(systemDetails.getExportAnalogSampleRate());
			stationDetails.setSystemLineFrequency(systemDetails.getLineFrequency());
			stationDetails.setExportRate(systemDetails.getExportRate());
			stationDetails.setSystemRecordingDeviceId(systemDetails.getRecordingDeviceId());
			stationDetails.setSystemPrefaultTime(systemDetails.getPrefaultTime());
			stationDetails.setSystemPostfaultTime(systemDetails.getPostfaultTime());
			stationDetails.setSystemLtrPrefaultTime(systemDetails.getLtrPrefaultTime());
			stationDetails.setSystemLtrPostfaultTime(systemDetails.getLtrPostfaultTime());
			stationDetails.setChatterLimit(systemDetails.getChatterLimit());
			stationDetails.setChatterRate(systemDetails.getChatterRate());
			stationDetails.setTriggerLimit(systemDetails.getTriggerLimit());
			// 26-June-2012 - As phase algorithm doesn't have triggers, we'll use measurements to get the last id used
//			stationDetails.setTotalTriggersConfigured(totalTriggersConfigured);
			stationDetails.setTotalTriggersConfigured(totalMeasurements);
			// 05-Apr-2021 - Total virtual measurements
			stationDetails.setTotalVirtualMeasurementsConfigured(totalVirtualMeasurements);
			// 09-Apr-2021 - Moved the initialization of line group id to stationDTO
			stationDetails.setTotalLineGroupsConfigured(totalLineGroupsConfigured);
			
			stationDetails.setPs(systemDetails.getPs());
			if (systemDetails.getSignalTooLowFactor() >= 0 )
			{
				stationDetails.setSignalTooLowFactor(systemDetails.getSignalTooLowFactor());
			}
			else
			{
				stationDetails.setSignalTooLowFactor(1.0);
			}
			
			if (systemDetails.getEventDebounce() >= 0 )
			{
				stationDetails.setEventDebounce(systemDetails.getEventDebounce());
			}
			else
			{
				stationDetails.setEventDebounce(1.0);
			}
			// START: 06-MAR-2018 - Configurable wetting voltage
			if (systemDetails.isSetMonitorWettingVoltage() )
			{
				stationDetails.setWettingVoltageMonitor(systemDetails.getMonitorWettingVoltage());
			}
			else
			{
				// Default enabled
				stationDetails.setWettingVoltageMonitor(1);
			}
			// END: 06-Mar-2018
			
			// Populate relay mappings
			populateRelayMappingSettingsFromXML(m9kConfig.getSubstation());
			
			// Trigger duration
			if (m9kConfig.getSubstation().getStationProperties() == null)
			{
				stationDetails.setTriggerDuration(10); // Default 10 seconds
				// START 13-Sept-2022 - cont data days limit moved to GUI
				stationDetails.setContOscDaysToRetain(5); // Default 5 days to retain OSC
				stationDetails.setContMeasurementsDaysToRetain(30); // Default 30 days to retain measurements
				// END 13-Sept-2022 - cont data days limit moved to GUI
				// START: 09-Jan-2023 - Moving export time limit for cont. data to GUI
				stationDetails.setContOscExportTimeLimit(120); // Default 120 seconds / 2 minutes
				stationDetails.setContMeasurementsExportTimeLimit(600);// Default 600 seconds / 10 minutes
				// END: 09-Jan-2023
				// START: 11-Jun-2024 - Enable or Disable event test for incompatible old boards
				stationDetails.setEnableEventTest(true);
				// END: 11-Jun-2024 
				// START:18-Jun-2024 - Set DDR deck time in seconds to be configurable
				stationDetails.setDdrDeckTimeLimit(300); // Default 300 seconds
				// END: 18-Jun-2024
			}
			else
			{
				stationDetails.setTriggerDuration(m9kConfig.getSubstation().getStationProperties().getTriggerDuration());
				// START 13-Sept-2022 - cont data days limit moved to GUI
				if (m9kConfig.getSubstation().getStationProperties().getContOscDaysToRetain() > 0)
				{
					stationDetails.setContOscDaysToRetain(m9kConfig.getSubstation().getStationProperties().getContOscDaysToRetain()); // Default 5 days to retain OSC
				}
				else
				{
					stationDetails.setContOscDaysToRetain(5);// Default 5
				}
				
				if (m9kConfig.getSubstation().getStationProperties().getContMeasurementsDaysToRetain() > 0)
				{
					stationDetails.setContMeasurementsDaysToRetain(m9kConfig.getSubstation().getStationProperties().getContMeasurementsDaysToRetain()); // Default 30 days to retain measurements
				}
				else
				{
					stationDetails.setContMeasurementsDaysToRetain(30);
				}
				// END 13-Sept-2022 - cont data days limit moved to GUI
				// START: 09-Jan-2023 - Moving export time limit for cont. data to GUI
				if (m9kConfig.getSubstation().getStationProperties().getContOscExportTimeLimit() > 0)
				{
					stationDetails.setContOscExportTimeLimit(m9kConfig.getSubstation().getStationProperties().getContOscExportTimeLimit()); // Default 5 days to retain OSC
				}
				else
				{
					stationDetails.setContOscExportTimeLimit(120);// Default 120 seconds / 2 minutes
				}
				
				if (m9kConfig.getSubstation().getStationProperties().getContMeasurementsExportTimeLimit() > 0)
				{
					stationDetails.setContMeasurementsExportTimeLimit(m9kConfig.getSubstation().getStationProperties().getContMeasurementsExportTimeLimit()); // Default 30 days to retain measurements
				}
				else
				{
					stationDetails.setContMeasurementsExportTimeLimit(600); // Default 600 seconds / 10 minutes
				}
				// END: 09-Jan-2023
				// START: 11-Jun-2024 - Enable or Disable event test for incompatible old boards
				if (m9kConfig.getSubstation().getStationProperties().isSetEnableEventTest() && m9kConfig.getSubstation().getStationProperties().getEnableEventTest() == M9kConstants.DISABLE_ZERO)
				{
					stationDetails.setEnableEventTest(false); // SET TO TRUE if equal to one
				}
				else
				{
					stationDetails.setEnableEventTest(true);
				}
				// END: 11-Jun-2024
				// START: 18-Jun-2024 - Set DDR deck time in seconds to be configurable
				if (m9kConfig.getSubstation().getStationProperties().isSetDdrDeckTimeLimit() && m9kConfig.getSubstation().getStationProperties().getDdrDeckTimeLimit() > 0)
				{
					stationDetails.setDdrDeckTimeLimit(m9kConfig.getSubstation().getStationProperties().getDdrDeckTimeLimit()); // Default 300 seconds
				}
				else
				{
					stationDetails.setDdrDeckTimeLimit(300); // Default 300 seconds
				}
				// END: 18-Jun-2024
			}
			// Populate PMU attributes
			if (m9kConfig.getSubstation().getPmus() != null)
			{
				Pmu pmu = m9kConfig.getSubstation().getPmus().getPmuArray(0);
				stationDetails.setPmuId(pmu.getPmuId());
				stationDetails.setPmuPort(pmu.getPmuPort());
				stationDetails.setPmuUdpPort(pmu.getUdpPort());
				stationDetails.setPmuStreamType(pmu.getPmuStreamType());
//				logger.debug("PMU stream type from XML "+stationDetails.getPmuStreamType());
				stationDetails.setPmuPhasorMode(pmu.getPmuPhasorMode());
				stationDetails.setPmuDataRate(pmu.getPmuDataRate());
				if (pmu.getPmuEnable() == M9kConstants.ENABLE_ONE)
				{
					// Changed for PDC implementation 4-Oct-2013
//					stationDetails.setPmuStatus(M9kConstants.ENABLE);
					stationDetails.setPmuEnabled(true);
					// End
				}
				else
				{
					// Changed for PDC implementation 4-Oct-2013
//					stationDetails.setPmuStatus(M9kConstants.DISABLE);
					stationDetails.setPmuEnabled(false);
					// End
				}
			}
			else
			{
				initializePmuForChassis();
				createDefaultGolbalPmus(m9kConfig.getSubstation());
			}
			populatePdcFromXML(m9kConfig.getSubstation());
			StationDTO.initializeExportId(totalEventsConfigured);
			
			// 09-Apr-2021 - Moved the initializing to stationDTO
//			LineGroupsAlgorithm.initializeLineGroupId(totalLineGroupsConfigured);
//			stationDetails.setConfigXml(configXml);
			
			logger.debug("Station DTO ParentZoneId "+stationDetails.getParentZoneId());
			// START: 18-Mar-2021 - Updating staion details in M9kUtils everytime user edits the station
			M9kUtils.setStationDetails(stationDetails);
			// END: 18-Mar-2021 - Updating staion details in M9kUtils everytime user edits the station
			session.put("stationDetails", stationDetails);
			session.put("editedStationName", stationDetails.getSystemStationName());
//			logger.debug("Station Details after edit..."+stationDetails);
			lstDfrDTO = populateDFRsFromXML(m9kConfig.getSubstation());
//			populateLineFrequency();
//			logger.debug("List of DFRs from XML..."+lstDfrDTO);
			session.put("DFRsList", lstDfrDTO);
//			session.put("lstAlarmNames",getLstAlarmNames());
//			session.put("lstRelays", getLstRelays());
			
		} catch (M9000Exception e) {
			logger.error("Exception occured during edit ",e);
			throw e;
		}
		catch(Exception e)
		{
			logger.error("Exception occured during edit ",e);
			throw new M9000Exception(e);
		}
		  return returnValue;
	  }
	  
	  @SkipValidation
	  public String createNewStation()
	  {
//		  populateLineFrequency();
//		  getSampleRateLst();
		  stationDetails = new StationDTO();
		  stationDetails.setSystemPrefaultTime(100);
		  stationDetails.setSystemPostfaultTime(100);
		  stationDetails.setSystemLtrPrefaultTime(40); // Seconds
		  stationDetails.setSystemLtrPostfaultTime(40); // Seconds
		  stationDetails.setTriggerLimit(10000);
		  stationDetails.setSystemLineFrequency(60);
		  stationDetails.setExportRate(60);
		// Changed for PDC implementation 7-Oct-2013
//		  stationDetails.setPmuPort(4712);
//		  stationDetails.setPmuUdpPort(4713);
		  stationDetails.setSystemRecordingDeviceId("USI_M9000");
		  stationDetails.setChatterLimit(20);
		  stationDetails.setChatterRate(2.0);
		  // START: 16-Dec-2024 - Set default system sampling rate to 4800
		  stationDetails.setSystemSampleRate(4800);
		  stationDetails.setSystemLongTermSampleRate(960);
		  // END: 16-Dec-2024
		  // START: 14-Dec-2015: Signal Too low factor default value set to 1%
		  stationDetails.setSignalTooLowFactor(1);
		  // END
		  // START: 7-Jul-2016 Implementing event Debounce
		  stationDetails.setEventDebounce(1.0);
		// Changed for PDC implementation 4-Oct-2013
//		  stationDetails.setPmuStatus(M9kConstants.DISABLE);
		  stationDetails.setPmuEnabled(false);
		  // Create default Alarms list
//		  session.put("lstAlarmNames",getLstAlarmNames());
//		  session.put("lstRelays", getLstRelays());
		  List<String >lstAlarmRelaysMapping = new ArrayList<String>(8);
			for (int i = 0; i < getLstRelays().size(); i++) {
				logger.debug("populateRelayMappingSettingsFromXML relays "+getLstRelays().get(i));
				lstAlarmRelaysMapping.add(getLstRelays().get(i));
			}
			// NO Seperate relays for wetting voltage as it is combined with Power
			// Default relay for wetting voltage is set to relay_5 like power
//			lstAlarmRelaysMapping.add(getLstRelays().get(4));
			stationDetails.setLstAlarmRelaysMapping(lstAlarmRelaysMapping);
			
			// START: 01-Sept-2022 - Cont. data to retain in GUI - Default 5 days for Osc. and 30 for measurements
			stationDetails.setContOscDaysToRetain(5);
			stationDetails.setContMeasurementsDaysToRetain(30);
			// END: 01-Sept-2022
			// START: 09-Jan-2023 - Export time limit for continuous data is made configurable
			stationDetails.setContOscExportTimeLimit(120); // Default 120 seconds / 2 minutes
			stationDetails.setContMeasurementsExportTimeLimit(600); // Default 600 seconds / 10 minutes
			// END: 09-Jan-2023
			// START: 11-Jun-2024 - Enable or Disable event test for incompatible old boards
			stationDetails.setEnableEventTest(true);
			// END: 11-Jun-2024
			// START: 18-Jun-2024 - Set DDR deck time in seconds to be configurable
			stationDetails.setDdrDeckTimeLimit(300); // In Seconds
			// END: 18-Jun-2024
		  initializePmuForChassis();
		  // Default value for PDC/PMU
		  stationDetails.setPdcId(1);
		  stationDetails.setPmuDataRate(60);
		  stationDetails.setPdcStreamType("udp");
		  stationDetails.setPdcMaxWait(300);
		  stationDetails.setPdcTcpPort(4712);
		  stationDetails.getLstOfUDPserverPorts().add(4713);
		  // end
		  stationDetails.setConfigStatus("INCOMPLETE");
		  session.put("stationDetails", stationDetails);
		  logger.debug("About to return create..." + mapLineFrequencies);
		  session.put("lineGroupExists", false);
		  logger.info("User "+getUserDto().getUserName() +" is creating a new station");
		  return SUCCESS;
	  }
	  
	  private void initializePmuForChassis()
	  {
		  PropertiesConfiguration pmuConfig;
		  int defaultPmuId = 1;
		  String defaultPmuStreamType="tcp";
		  int defaultPmuTcpPort=4712;
		  int defaultPmuUdpPort=4713;
		  String defaultPmuPhasorMode="Rectangular";
		  Integer defaultCommandServerPort = 9978; 
		  
		  try {
			pmuConfig =  new PropertiesConfiguration("/m9k-master.properties");
			stationDetails.setPmuId(pmuConfig.getInt("pmu-id", defaultPmuId));
			stationDetails.setPmuStreamType(pmuConfig.getString("pmu-stream-type",defaultPmuStreamType));
			stationDetails.setPmuPort(pmuConfig.getInt("pmu-tcp-port", defaultPmuTcpPort));
			stationDetails.setPmuUdpPort(pmuConfig.getInt("pmu-udp-port", defaultPmuUdpPort));
			stationDetails.setPmuPhasorMode(pmuConfig.getString("pmu-phasor-mode", defaultPmuPhasorMode));
			stationDetails.setCommandServerPort(pmuConfig.getInt("command-server-port", defaultCommandServerPort));
			stationDetails.setPmuDataRate(10);
		} catch (ConfigurationException e) {
			stationDetails.setPmuId(defaultPmuId);
			stationDetails.setPmuStreamType(defaultPmuStreamType);
			stationDetails.setPmuPort(defaultPmuTcpPort);
			stationDetails.setPmuUdpPort(defaultPmuUdpPort);
			stationDetails.setPmuPhasorMode(defaultPmuPhasorMode);			
			stationDetails.setCommandServerPort(defaultCommandServerPort);
			stationDetails.setPmuDataRate(10);
		}
	  }

	  
	  @SkipValidation
	  public String populateLineFrequency()
	  {
		  List<Object> lstLineFrequency;
		  lstLineFrequency = config.getList("lineFrequency");
		  logger.debug("lstLineFrequency from property file "+lstLineFrequency);
		  lineFreqObjListOld = new ArrayList<ListValue>();
//		  mapLineFrequencies = new LinkedHashMap<String, String>();
		  for (int i = 0; i < lstLineFrequency.size(); i++) {
//			  mapLineFrequencies.put(lstLineFrequency.get(i), lstLineFrequency.get(i)+"Hz");
			  logger.debug("LIne freq read : "+lstLineFrequency.get(i));
			  lineFreqObjListOld.add(new ListValue(lstLineFrequency.get(i).toString(), lstLineFrequency.get(i)+"Hz"));
		  }
		  logger.debug("Line Frequency...."+lineFreqObjListOld);
		  return SUCCESS;
//		  session.put("mapLineFrequencies", mapLineFrequencies);		  
	  }

	private void populateInputType() {
		List<Object> lstAnalogInputType;
		lstAnalogInputType = config.getList("analogInputType");
		mapAnalogInputType = new LinkedHashMap<String, String>();
		String inputTypeDetails[];
		for (int i = 0; i < lstAnalogInputType.size(); i++) {
			inputTypeDetails = lstAnalogInputType.get(i).toString().split(":");
			mapAnalogInputType.put(inputTypeDetails[0], inputTypeDetails[1]
					);
		}
		session.put("mapAnalogInputType", mapAnalogInputType);
	}
	  
		private List<DfrDTO> populateDFRsFromXML(SubStation substation)
		{
			  int nextStartAnalogIndex = -1;
			  int nextStartDigitalIndex = -1;
			  int analogCnt;
			  int digitalCnt;
			
//			logger.debug("Substation..."+substation);
			List<DfrDTO> lstDfrs = new ArrayList<DfrDTO>(substation.getDFRs().sizeOfDFRArray());
			DfrDTO dfrDto;
			DFR[] xmlDfrs = substation.getDFRs().getDFRArray();
			logger.debug("Total DFRs.."+substation.getDFRs().sizeOfDFRArray());
			for (int i = 0; i < xmlDfrs.length; i++) {
				dfrDto = new DfrDTO();
				dfrDto.setDfrName("DFR"+xmlDfrs[i].getSystem().getDfrId());
				dfrDto.setDfrId(xmlDfrs[i].getSystem().getDfrId());
//				logger.debug("Name: "+dfrDto.getDfrName());
				dfrDto.setIpAddress(xmlDfrs[i].getIPAddress());
//				logger.debug("IPAddress: "+dfrDto.getIpAddress());
				dfrDto.setStatus("COMPLETE");
				if (xmlDfrs[i].getDataPool().getChannels().getAnalogs() != null)
				{
					// Subtract the virtual channels count to only report Physical channels
					analogCnt = xmlDfrs[i].getDataPool().getChannels().getAnalogs().sizeOfAnalogInputArray() - getVirtualChannelCount(xmlDfrs[i].getDataPool().getChannels().getAnalogs().getAnalogInputArray()); 
					dfrDto.getChnlConfigDropDownList().setAnalogCnt(analogCnt);
					dfrDto.setAnalogChnlCnt(analogCnt);
					if (nextStartAnalogIndex == -1)
					{
						dfrDto.setAnalogChannelStart(1);
						dfrDto.setAnalogChannelEnd(analogCnt);
						nextStartAnalogIndex = analogCnt+1;
					}
					else 
					{
						dfrDto.setAnalogChannelStart(nextStartAnalogIndex);
						dfrDto.setAnalogChannelEnd(nextStartAnalogIndex + analogCnt - 1);
						nextStartAnalogIndex += analogCnt;
					}
					
				}
				else
				{
					dfrDto.getChnlConfigDropDownList().setAnalogCnt(0);
				}
				if (xmlDfrs[i].getDataPool().getChannels().getEvents() != null)
				{
					digitalCnt = xmlDfrs[i].getDataPool().getChannels().getEvents().sizeOfEventInputArray();
					dfrDto.setDigitalChnlCnt(digitalCnt);
					dfrDto.getChnlConfigDropDownList().setDigitalCnt(digitalCnt);
					if (nextStartDigitalIndex == -1)
					{
						dfrDto.setDigitalChannelStart(1);
						dfrDto.setDigitalChannelEnd(digitalCnt);
						nextStartDigitalIndex = digitalCnt+1;
					}
					else 
					{
						dfrDto.setDigitalChannelStart(nextStartDigitalIndex);
						dfrDto.setDigitalChannelEnd(nextStartDigitalIndex + digitalCnt - 1);
						nextStartDigitalIndex += digitalCnt;
					}

				}
				else
				{
					dfrDto.getChnlConfigDropDownList().setDigitalCnt(0);
				}
//				logger.debug("Dfrs added to the list..."+dfrDto);
				lstDfrs.add(dfrDto);
			}
			return lstDfrs;
		}
	  
	  private int calculateMinimumDFRRequired()
	  {
		  int minDFRCnt = 0;
		  double analogBoard = Math.ceil((double)getStationDetails().getSystemAnalogChannelsCount()/M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD);
		  double digitalBoard = Math.ceil((double)getStationDetails().getSystemDigitalChannelsCount()/M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD);
		  int totalBoards = (int) (analogBoard+digitalBoard);
		  logger.debug(" Analog baords "+analogBoard+" digital board "+digitalBoard+" total boards "+totalBoards);
		  // START: 02-Jun-2020 - M9kMini - implementation 
		  if ((getStationDetails().getSystemAnalogChannelsCount()%M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD) > 0 || (getStationDetails().getSystemDigitalChannelsCount()%M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD) > 0)
		  {
			  totalBoards--; // ANEV has 4A-16D Extra board is not required as it is both combined in one  
			  logger.debug("ANEV configuration. Reduce one board. total board "+totalBoards);
		  }
		  // END: 02-Jun-2020 - M9kMini - implementation 
		  minDFRCnt = (int) Math.ceil((double)totalBoards/M9kConstants.NO_OF_BOARDS_PER_DFR);
		  logger.debug("Minimum dfr count "+minDFRCnt);
		  return minDFRCnt;
	  }

	  private int getVirtualChannelCount(AnalogInput[] arrAnalogInput)
	  {
		  int virtCont = 0;
		  for (int i = 0; i < arrAnalogInput.length; i++) {
			if (arrAnalogInput[i].isSetVirtual() && arrAnalogInput[i].getVirtual() == 1)
			{
				virtCont++;
			}
		  }
		  return virtCont;
	  }
		private String getDbConnectionString()
		{
			StringBuffer dbConn = new StringBuffer();
			logger.debug("DB-CONFIG: Getting db connection string..."+cfgDb);
			if (cfgDb != null)
			{
				dbConn.append("host="+cfgDb.getString("dfr_db_host")+";");
				dbConn.append("user="+cfgDb.getString("mysql_user")+";");
				dbConn.append("password="+cfgDb.getString("mysql_password")+";");
				dbConn.append("db="+cfgDb.getString("db")+";");
				dbConn.append(cfgDb.getString("mysql_conn_attr"));
			}
			else
			{
				dbConn.append("host=192.168.1.1;user=dfr;password=usi;db=m9000;compress=false;auto-reconnect=true");
			}
			logger.debug("DB connection string to be saved..."+dbConn.toString());
			return dbConn.toString();
		}

	@SuppressWarnings("unchecked")
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	  config = (PropertiesConfiguration) session.get("config");
	  cfgDb = (PropertiesConfiguration) session.get("cfgDb");
	  lstDfrDTO = (List<DfrDTO>) session.get("DFRsList");
//		lineFreqObjList = (List<M9kKeyValuePair>) session.get("lineFreqObjList");
//		lstSampleRate =  (List<Object>) session.get("lstSampleRate");
//		lstLtSampleRate =  (List<Object>) session.get("lstLtSampleRate");
//		lstPmuDataRate =  (List<Object>) session.get("lstPmuDataRate");
//		lstEventDebounce = (List<Object>)session.get("lstEventDebounce");
	  accessMode = (String) session.get("accessMode");
	  if (session.get("mapAnalogInputType") == null)
	  {
		  populateInputType();
	  }
	  userDto = (UsersDTO) session.get("userDetails");
	  stationDetails = (StationDTO) session.get("stationDetails");
	}
	
	public Map<String, Object> getSession(){
	    return session;
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
	public void setStationDetails(StationDTO sessionStationDetails) {
		this.stationDetails = sessionStationDetails;
	}

	/**
	 * @return the minimumDFRCount
	 */
	public int getMinimumDFRCount() {
		return minimumDFRCount;
	}

	/**
	 * @param minimumDFRCount the minimumDFRCount to set
	 */
	public void setMinimumDFRCount(int minDfrCnt) {
		this.minimumDFRCount = minDfrCnt;
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
	 * @return the lstSampleRate
	 */
	public List<Object> getLstSampleRate() {
		return lstSampleRate;
	}

	/**
	 * @param lstSampleRate the lstSampleRate to set
	 */
	public void setLstSampleRate(List<Object> lstSampleRate) {
		this.lstSampleRate = lstSampleRate;
	}


	/**
	 * @return the accessMode
	 */
	public String getAccessMode() {
		return accessMode;
	}

	/**
	 * @param accessMode the accessMode to set
	 */
	public void setAccessMode(String accessMode) {
		this.accessMode = accessMode;
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


	/**
	 * @return the mapLineFrequencies
	 */
	public Map<String, String> getMapLineFrequencies() {
		return mapLineFrequencies;
	}

	/**
	 * @param mapLineFrequencies the mapLineFrequencies to set
	 */
	public void setMapLineFrequencies(Map<String, String> mapLineFrequencies) {
		this.mapLineFrequencies = mapLineFrequencies;
	}

	/**
	 * @return the stationName
	 */
	public String getStationName() {
		return stationName;
	}

	/**
	 * @param stationName the stationName to set
	 */
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	
	@SuppressWarnings("unchecked")
	public void validate()
	{
		try
		{
			if (session == null)
			{
				return;
			}
		String editedStationName = (String) session.get("editedStationName");
		lstAvailableStations = (List<StationDTO>) session.get("lstAvailableStations");
//		logger.debug("Station ID test..."+getStationIndex()+getBtnSubmit()+" "+lstAvailableStations);
		logger.debug("Station name "+getStationDetails().getSystemStationName()+" ID "+getStationDetails().getSystemStationId());
		if (getStationDetails().getSystemStationId() == null)
		{
			addFieldError("stationDetails.systemStationId", "Station Id required");
		}
		else if (getStationDetails().getSystemStationName() == null)
		{
			addFieldError("stationDetails.systemStationName", "Station Name is required");
		}
		// START: 21-May-2024 - Check for unwanted special characters. Only allowed special characters are - _
//		if (getStationDetails().getSystemStationName().indexOf(",") != -1)
		if (!M9kUtils.isValidStationName(getStationDetails().getSystemStationName()))
		// END: 21-May-2024 - Check for unwanted special characters. Only allowed special characters are - _
		{
			addFieldError("stationDetails.systemStationName", "'-' '_' '.' and space are the only allowed special characters");
		}

		else
		{
			for (Iterator<StationDTO> iterator = lstAvailableStations.iterator(); iterator.hasNext();) {
				StationDTO station = iterator.next();
				if (editedStationName != null && editedStationName.equalsIgnoreCase(station.getSystemStationName()))
				{
					continue;
				}
				if (station.getSystemStationName().equalsIgnoreCase(getStationDetails().getSystemStationName()))
				{
					addFieldError("stationDetails.systemStationName", "Station Name already exists");
					break;
				}
//				logger.debug("Existing station Id "+station.getSystemStationId()+" entered station id "+getStationDetails().getSystemStationId());
//				logger.debug("Is station id same? "+(station.getSystemStationId() == getStationDetails().getSystemStationId()));
				if (station.getSystemStationId().intValue() == getStationDetails().getSystemStationId().intValue())
				{
					addFieldError("stationDetails.systemStationId", "Station Id already in use");
					break;
				}
				// START: 21-May-2024 - Check for unwanted special characters. Only allowed special characters are - _
//				if (getStationDetails().getSystemStationName().indexOf(",") != -1)
				if (!M9kUtils.isValidStationName(getStationDetails().getSystemStationName()))
				// END: 21-May-2024 - Check for unwanted special characters. Only allowed special characters are - _
				{
					addFieldError("stationDetails.systemStationName", "Only special characters allowed - _ and space");
					break;
				}
			}
		}
		if ((getStationDetails().getSystemAnalogChannelsCount() % M9kConstants.NO_OF_ANALOG_CHNLS_PER_ANEV_BOARD) > 0 )
		{
			addFieldError("stationDetails.systemAnalogChannelsCount", "Analog Channel count should be multiples of 4");
		}
		if ((getStationDetails().getSystemDigitalChannelsCount() % M9kConstants.NO_OF_DIGITAL_CHNLS_PER_ANEV_BOARD) > 0 )
		{
			addFieldError("stationDetails.systemDigitalChannelsCount", "Digital Channel count should be multiples of 16");
		}
		// Start 8-Oct-2013 Changed to implement PDC GUI
//		if (getStationDetails().getPmuPort().equals(getStationDetails().getPmuUdpPort()))
//		{
//			addFieldError("stationDetails.pmuPort", "PMU port should be different from UDP port number");
//			addFieldError("stationDetails.pmuUdpPort", "UDP port should be different from PMU port number");
//		}
		if (getStationDetails().isPmuEnabled())
		{
			if (getStationDetails().getPdcId() == null)
			{
				addFieldError("stationDetails.pdcId", "PMU Id is required");
			}
			else if (!M9kUtils.isInteger(getStationDetails().getPdcId().toString()))
			{
				addFieldError("stationDetails.pdcId", "PMU Id should be numeric");
			}
			else if (Integer.parseInt(getStationDetails().getPdcId().toString()) < 1 || Integer.parseInt(getStationDetails().getPdcId().toString()) > 65534)
			{
				addFieldError("stationDetails.pdcId", "PMU Id should be between 1 and 65534");
			}
			if (getStationDetails().getPdcMaxWait() == null)
			{
				addFieldError("stationDetails.pdcMaxWait", "Max Wait Time is required");
			}
			else if (!M9kUtils.isInteger(getStationDetails().getPdcMaxWait().toString()))
			{
				addFieldError("stationDetails.pdcMaxWait", "Max Wait Time should be numeric");
			}
			else if (Integer.parseInt(getStationDetails().getPdcMaxWait().toString()) < 300 || Integer.parseInt(getStationDetails().getPdcMaxWait().toString()) > 1000)
			{
				addFieldError("stationDetails.pdcMaxWait", "Max Wait Time should be between 300 and 1000");
			}
			if (getStationDetails().getPdcStreamType().equalsIgnoreCase("tcp"))
			{
				if (getStationDetails().getPdcTcpPort() == null)
				{
					addFieldError("stationDetails.pdcTcpPort", "TCP Port is required");
				}
				else if (!M9kUtils.isInteger(getStationDetails().getPdcTcpPort().toString()))
				{
					addFieldError("stationDetails.pdcTcpPort", "TCP Port should be numeric");
				}
			}
			else
			{
				Integer udpPort;
				int i = 0;
				boolean atleastOneUdpPort = false;
				for (Iterator<Integer> iterator = getStationDetails().getLstOfUDPserverPorts().iterator(); iterator.hasNext();) {
					udpPort = iterator.next();
					if (udpPort != null)
					{
						atleastOneUdpPort = true;
						if (!M9kUtils.isInteger(udpPort.toString()))
						{
							addFieldError("stationDetails.lstOfUDPserverPorts["+i+"]", "TCP Port should be numeric");
						}
					}
					
				}
				if (!atleastOneUdpPort)
				{
					addFieldError("stationDetails.lstOfUDPserverPorts[0]", "At least one UDP Port is required");
				}
			}
		}
//		logger.debug("stationDetails"+getStationDetails());
		}
		catch (Exception e) {
			logger.error("Error in validation durong creation ",e);
		}
//		stationDetails = (StationDTO)session.get("stationDetails");
//		if (getStationIndex() == null)
//		{
////			clearFieldErrors();
////			addFieldError("stationIndex", "Select a station to edit");
//		}
	}

	/**
	 * @return the mapAnalogInputType
	 */
	public Map<String, String> getMapAnalogInputType() {
		return mapAnalogInputType;
	}

	/**
	 * @param mapAnalogInputType the mapAnalogInputType to set
	 */
	public void setMapAnalogInputType(Map<String, String> mapAnalogInputType) {
		this.mapAnalogInputType = mapAnalogInputType;
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

	public StationDTO getStationDetails(int stationId)
	{
		StationDTO currentStationDTO = null;
		for (Iterator<StationDTO> iterator = getLstAvailableStations().iterator(); iterator.hasNext();) {
			currentStationDTO = (StationDTO) iterator.next();
			if (currentStationDTO.getId() == stationId)
			{
				break;
			}
		}
		return currentStationDTO;
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

	/**
	 * @return the lstLtSampleRate
	 */
	public List<Object> getLstLtSampleRate() {
		return lstLtSampleRate;
	}

	/**
	 * @param lstLtSampleRate the lstLtSampleRate to set
	 */
	public void setLstLtSampleRate(List<Object> lstLtSampleRate) {
		this.lstLtSampleRate = lstLtSampleRate;
	}
	public class ListValue {
	    private String myKey;
	    private String myValue;

	    public ListValue(String myKey, String myValue) {
	      super();
	      this.myKey = myKey;
	      this.myValue = myValue;
	    }

	    public String getMyKey()
	    {
	      return myKey;
	    }

	    public void setMyKey(String myKey)
	    {
	      this.myKey = myKey;
	    }

	    public String getMyValue()
	    {
	      return myValue;
	    }

	    public void setMyValue(String myValue)
	    {
	      this.myValue = myValue;
	    }
	  }
	
//	   public String getJSON(){
//	    	return populateLineFrequency();
//	    }

	public List<ListValue> getLineFreqObjListOld() {
		return lineFreqObjListOld;
	}

	public void setLineFreqObjListOld(List<ListValue> lineFreqObjList) {
		this.lineFreqObjListOld = lineFreqObjList;
	}

	public String getSourceTab() {
		return sourceTab;
	}

	public void setSourceTab(String sourceTab) {
		this.sourceTab = sourceTab;
	}

	public boolean isBooAnalog() {
		return booAnalog;
	}

	public void setBooAnalog(boolean booAnalog) {
		this.booAnalog = booAnalog;
	}

	public boolean isBooDigital() {
		return booDigital;
	}

	public void setBooDigital(boolean booDigital) {
		this.booDigital = booDigital;
	}

	public boolean isLineGroupExists() {
		return lineGroupExists;
	}

	public void setLineGroupExists(boolean lineGroupExists) {
		this.lineGroupExists = lineGroupExists;
	}

	public Map<String, DFR> getMapXMLDfrs() {
		return mapXMLDfrs;
	}

	public void setMapXMLDfrs(Map<String, DFR> mapXMLDfrs) {
		this.mapXMLDfrs = mapXMLDfrs;
	}

	public List<Object> getLstPmuDataRate() {
		return lstPmuDataRate;
	}

	public void setLstPmuDataRate(List<Object> lstPmuDataRate) {
		this.lstPmuDataRate = lstPmuDataRate;
	}

	public List<M9kKeyValuePair> getLineFreqObjList() {
		return lineFreqObjList;
	}

	public void setLineFreqObjList(List<M9kKeyValuePair> lineFreqObjList) {
		this.lineFreqObjList = lineFreqObjList;
	}
	
	private UsersDTO isStationAlreadyInEditMode(int stationId)
	{
		boolean booUserAlreadyEditing = false;
		UsersDTO usersDTO = null;
		ServletContext context = ServletActionContext.getServletContext();
		@SuppressWarnings("unchecked")
		Set<UsersDTO> logins = (Set<UsersDTO>) context.getAttribute("logins");
		logger.debug("In isStationAlreadyInEditMode method... logins"+logins);
		if (logins != null)
		{
			for (Iterator<UsersDTO> iterator = logins.iterator(); iterator.hasNext();) {
				usersDTO =  iterator.next();
				if (usersDTO.getRole().equalsIgnoreCase(M9kConstants.ADMIN) && usersDTO.isEditMode() && usersDTO.getEditedStationId() == stationId)
				{
					booUserAlreadyEditing = true;
					logger.debug("User already editing "+usersDTO);
					break;
				}
			}
		}
		if (!booUserAlreadyEditing)
		{
			usersDTO = null;
		}
		logger.debug("Returning already editing user "+usersDTO);
		return usersDTO;
	}

	/**
	 * @return the customActionError
	 */
	public String getCustomActionError() {
		return customActionError;
	}

	/**
	 * @param customActionError the customActionError to set
	 */
	public void setCustomActionError(String customActionError) {
		this.customActionError = customActionError;
	}

	/**
	 * @return the lstEventDebounce
	 */
	public List<Object> getLstEventDebounce() {
		return lstEventDebounce;
	}

	/**
	 * @param lstEventDebounce the lstEventDebounce to set
	 */
	public void setLstEventDebounce(List<Object> lstEventDebounce) {
		this.lstEventDebounce = lstEventDebounce;
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

	/**
	 * @return the deleteIndex
	 */
	public int getDeleteIndex() {
		return deleteIndex;
	}

	/**
	 * @param deleteIndex the deleteIndex to set
	 */
	public void setDeleteIndex(int deleteIndex) {
		this.deleteIndex = deleteIndex;
	}

	/**
	 * @return the newlyAddedDfr
	 */
	public DfrDTO getNewlyAddedDfr() {
		return newlyAddedDfr;
	}

	/**
	 * @param newlyAddedDfr the newlyAddedDfr to set
	 */
	public void setNewlyAddedDfr(DfrDTO newlyAddedDfr) {
		this.newlyAddedDfr = newlyAddedDfr;
	}

	/**
	 * @return the newlyAddedDfrIndex
	 */
	public int getNewlyAddedDfrIndex() {
		return newlyAddedDfrIndex;
	}

	/**
	 * @param newlyAddedDfrIndex the newlyAddedDfrIndex to set
	 */
	public void setNewlyAddedDfrIndex(int newlyAddedDfrIndex) {
		this.newlyAddedDfrIndex = newlyAddedDfrIndex;
	}

	/**
	 * @return the channelsConfList
	 */
	public List<String> getChannelsConfList() {
		return channelsConfList;
	}

	/**
	 * @param channelsConfList the channelsConfList to set
	 */
	public void setChannelsConfList(List<String> channelsConfList) {
		this.channelsConfList = channelsConfList;
	}

	public HashMap<LedName, List<Object>> getMapLedToRelay() {
		return mapLedToRelay;
	}

	public void setMapLedToRelay(HashMap<LedName, List<Object>> mapLedToRelay) {
		this.mapLedToRelay = mapLedToRelay;
	}

	public List<String> getLstRelays() {
		if (lstRelays == null)
		{
			lstRelays = new ArrayList<String>(8);
			for (int i=0; i < 4;i++)
			{
				lstRelays.add(RelayName.values()[i].name());
			}
			// As a default assume only 4 relays and set relay 4 for rest of the alarms
			for (int i=0; i < 4;i++)
			{
				lstRelays.add(RelayName.values()[3].name());
			}
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

	private void populateRelayMappingSettingsFromXML(SubStation substation)
	{
		List<String> lstAlarmRelaysMapping = null;
		StationProperties stationProperties = substation.getStationProperties();
		if (stationProperties != null)
		{
			Alarms alarms = substation.getStationProperties().getAlarms();
			logger.debug("populateRelayMappingSettingsFromXML: alarms xml "+alarms+" relays list "+getLstRelays());
			if (alarms != null)
			{
				lstAlarmRelaysMapping = new ArrayList<String>(8);
				Alarm alarm[] = alarms.getAlarmArray();
				for (int i = 0; i < alarm.length; i++) {
					logger.debug("Alarms Name "+alarm[i].getAlarmName());
					// START: 20-Feb-2023 - Replacing Wetting Voltage
					if (alarm[i].getAlarmName().equalsIgnoreCase(LedName.WETTING_VOLTAGE.name()))
					{
						logger.debug("replacing WETTING_VOLTAGE with "+LedName.DISTURBANCE.name()+" with existing Relay "+alarm[i].getRelay());
						alarm[i].setAlarmName(LedName.DISTURBANCE.name());
//						alarm[i].setRelay(getLstRelays().get(getLstRelays().size()-1));
					}
					else if (alarm[i].getAlarmName().equalsIgnoreCase(LedName.DISTURBANCE.name()+"_ALARM")) // To overcome old name DISTURBANCE_ALARM
					{
						logger.debug("replacing DISTURBANCE_ALARM with "+LedName.DISTURBANCE.name()+" with existing Relay "+alarm[i].getRelay());
						alarm[i].setAlarmName(LedName.DISTURBANCE.name());
//						alarm[i].setRelay(getLstRelays().get(getLstRelays().size()-1));
					}
						
					// END: 20-Feb-2023
						for (int j = 0; j < getLstAlarmNames().size(); j++) {
							logger.debug("xml alarm name "+alarm[i].getAlarmName()+" from the list of alarm names "+getLstAlarmNames().get(j));
							if (alarm[i].getAlarmName().equalsIgnoreCase(getLstAlarmNames().get(j)))
							{
								lstAlarmRelaysMapping.add(alarm[i].getRelay());
							}
						}
									
				}
			}
		}
		if (lstAlarmRelaysMapping == null)
		{
			lstAlarmRelaysMapping = new ArrayList<String>(8);
			for (int i = 0; i < getLstRelays().size(); i++) {
				logger.debug("populateRelayMappingSettingsFromXML relays "+getLstRelays().get(i));
				lstAlarmRelaysMapping.add(getLstRelays().get(i));
			}
		}
		else if (lstAlarmRelaysMapping.size() < getLstAlarmNames().size()) // Just to add new alarm mappings to already existing alarm mappings. e.g Disturbance alarm added at last minute
		{
			int missingMapping = getLstAlarmNames().size() - lstAlarmRelaysMapping.size();
			for (int i = missingMapping; i > 0;  i--)
			{
				lstAlarmRelaysMapping.add(getLstRelays().get(getLstRelays().size()-1));
			}
		}

		logger.debug("populateRelayMappingSettingsFromXML: list to be set in stationdetails "+lstAlarmRelaysMapping);
		stationDetails.setLstAlarmRelaysMapping(lstAlarmRelaysMapping);
		logger.debug("populateRelayMappingSettingsFromXML: Alarms Relay Mapping "+stationDetails.getLstAlarmRelaysMapping());
	}
}
