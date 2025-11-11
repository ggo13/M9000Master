package com.usi.m9000.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.apache.struts2.util.Counter;
import org.apache.xmlbeans.XmlOptions;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.AlgorithmsDocument.Algorithms;
import com.usi.DFRDocument.DFR;
import com.usi.EventInputDocument.EventInput;
import com.usi.PdcDocument.Pdc;
import com.usi.PmuDocument.Pmu;
import com.usi.PmuInputDocument.PmuInput;
import com.usi.PmusDocument.Pmus;
import com.usi.SubStationDocument.SubStation;
import com.usi.TriggerDocument.Trigger;
import com.usi.TriggersDocument.Triggers;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.EventChannelDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.TriggerChannelDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.M9kXMLConstants;

public class ConfigureEventChannelsAction extends ActionSupport implements
		SessionAware, ServletContextListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String selectedDfr;
	private Map<String, Object> session;
	private StationDTO stationDetails;
	private String eventChannelsCount;
	int minimumDFRCount;
	private Counter eventChannelCounter;
	private Counter triggerChannelCounter;
//	ChannelsListInfo channelsConfList;
	List<DfrDTO> lstDfrDTO;
//	DFR xmlConfigDfr;
	List<EventChannelDTO> lstEventChannels;
	EventInput[] eventInput;
	List<String> lstEventDescriptions;
	Map<String, String> mapEventDescriptions;
	private Map<String, String> json;
	String selectedIndex;
	int reqdIndex;
	String accessMode;
	PropertiesConfiguration config;
	List<Object> lstDfrStart ;
	List<Object> lstDfrSer ;
	List<Object> lstSerRun ;
	
	List<TriggerChannelDTO> lstEventTriggerChannels;
	// START: 10-Mar-2021 - Virtual Measurements implementation
//	List<TriggerChannelDTO> lstTriggerChannels;
//	List<TriggerSourceDTO> lstTriggerSourceDto;
//	String triggerOutHold;
	// END: 10-Mar-2021 - Virtual Measurements implementation	
	String sourceTab;
	Map<String, DFR> mapXMLDfrs;
	boolean booAnalog;
	boolean booDigital;
	private List<LineGroupsAlgorithm> lstLineGroups;
	private Boolean lineGroupExists = false;
	UsersDTO userDto;
	List<Object> lstAllTriggerTypes;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ConfigureEventChannelsAction.class);

	/**
	 * 
	 */
	public ConfigureEventChannelsAction(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
	}

	public ConfigureEventChannelsAction() {
		logger.debug("Entered constructor of ConfigureEventChannelsAction");
//		logger.info("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is in the Digitals configuration screen");
	}

	public String execute()
	{
		String navigateValue = "";
		booAnalog = (Boolean)session.get("booAnalog");
		booDigital = (Boolean)session.get("booDigital");
		lineGroupExists = (Boolean)session.get("lineGroupExists");
		logger.debug("Is getSourceTab() null? "+getSourceTab());
		if (getSourceTab() == null) 
		{
			sourceTab = (String) session.get("sourceTab");  
		}
		logger.debug("sourceTab...."+sourceTab);
		session.put("sourceTab", sourceTab);
		if (getSourceTab() != null && !getSourceTab().isEmpty())
		{
			if (getLstEventChannels() != null)
			{
				populateEventsIntoXmlConfig();
				session.put("lstEventChannels", lstEventChannels);
			}
			if (getSourceTab().equalsIgnoreCase("Measurements"))
			{
				navigateValue = "Measurements";
			}
			else if (getSourceTab().equalsIgnoreCase("LineGroups"))
			{
				navigateValue="LineGroups";
			}
			else if (getSourceTab().equalsIgnoreCase("Analogs"))
			{
				navigateValue="Analogs";
			}
			else if (getSourceTab().equalsIgnoreCase("FaultLocation"))
			{
				logger.info("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is forwarded via Events configuration screen");
				navigateValue="FaultLocation";
				if (!booDigital)
				{
					return navigateValue;
				}
//				if (getLstEventChannels() != null && !getLstEventChannels().isEmpty())
//				{
//					populateEventsIntoXmlConfig();
//				}
//				else
				logger.debug("is lstEventChannels null "+getLstEventChannels());
				
				if (getLstEventChannels() == null )
				{
					logger.debug("lstEventChannels is null ");
					populateEventDTO();
					populateEventsIntoXmlConfig();
				}
//				populateFaultLocEventTriggers();
//				session.put("lstEventChannels", getLstEventChannels());
			}
			else if (getSourceTab().equalsIgnoreCase("StationDetails"))
			{
				navigateValue="StationDetails";
			}
			else if (getSourceTab().equalsIgnoreCase("VirtualChannels"))
			{
				navigateValue="VirtualChannels";
			}
			// START: 10-Mar-2021 - Virtual Measurement implementation
			else if (getSourceTab().equalsIgnoreCase("VirtualMeasurements"))
			{
				navigateValue="BackToVirtualMeasurements";
			}
			// END: 10-Mar-2021 - Virtual Measurement implementation
			
			
		}
		if (getSourceTab() == null || getSourceTab().equalsIgnoreCase("Events") || getSourceTab().isEmpty())
		{
			navigateValue = populateEventDTO();
		}
		else
		{
			updatePmuDetails();
		}
		return navigateValue;
	}
	
	public String saveEvents() throws M9000Exception
	{
		String returnString = "Done";
		populateEventsIntoXmlConfig();
		session.put("lstEventChannels", lstEventChannels);
		if (stationDetails.isDfrAddedOrRemoved())
		{
			stationDetails.setDfrAddedOrRemoved(false);
		}
		logger.debug("LineGroupExists "+getLineGroupExists()+" "+getLstLineGroups());
		if (getLineGroupExists() || (getLstLineGroups() != null && !getLstLineGroups().isEmpty()))
		{
//			session.put("lstEventChannels", getLstEventChannels());
			logger.debug("LstLineGroups "+getLstLineGroups()+" name of 1st line group "+getLstLineGroups().get(0).getName()+" "+getLstLineGroups().get(0).getLineGroupName() );
			returnString = "FaultLocation";
		}
		else
//		if (getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit"))
		{
			returnString = "Done";
//			SubStation substation = (SubStation)session.get("selectedSubstation");
//			M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
//			StationDAO mysqlStationDao = m9kDAOFactory.getStationDAO();
//			
//			XmlOptions xmlOptions = new XmlOptions(); 
//			xmlOptions.setSaveOuter();
//			xmlOptions.setSavePrettyPrint();
//			xmlOptions.setUseDefaultNamespace();
//			Map<String, String> prefixes = new HashMap<String, String>();
//			prefixes.put("", "http://www.usi.com");
//			xmlOptions.setSaveImplicitNamespaces(prefixes);
//			stationDetails = (StationDTO) session.get("stationDetails");
//			stationDetails.setTotalDfrsConfigured(substation.getDFRs().sizeOfDFRArray());
//			stationDetails.setConfigStatus("COMPLETE");
//			String xmlText = substation.xmlText(xmlOptions);
//			stationDetails.setConfigXml(xmlText);
//			((StationDTO)session.get("stationDetails")).setConfigStatus("COMPLETE");
//			if (mysqlStationDao.isStationExists(stationDetails.getSystemStationId()))
//			{
//				mysqlStationDao.updateConfigXml(stationDetails);
//			}
//			else
//			{
//				mysqlStationDao.insertStationDetails(stationDetails);
//			}
			save();
//			accessMode = "Edit";
//			session.put("accessMode", accessMode);
		}
		return returnString;
	}
	public String saveEventsAndSend () throws M9000Exception
	{
		String returnString = "Send";
		populateEventsIntoXmlConfig();
		save();
		logger.info("User "+userDto.getUserName()+" succesfully sent the saved configuration to station "+stationDetails.getStationDisplayName());
		return returnString;
	}
	private void save() throws M9000Exception
	{

		session.put("lstEventChannels", lstEventChannels);
		SubStation substation = (SubStation)session.get("selectedSubstation");
		
		int configSerialNumber = 1;
		DFR[] dfrs = substation.getDFRs().getDFRArray();
		logger.debug("IS confugSErialNumber set already? "+dfrs[0].getSystem().isSetConfigSerialNumber());
		if (dfrs[0].getSystem().isSetConfigSerialNumber())
		{
			logger.debug("ConfigSEriaNumber already set "+substation.getDFRs().getDFRArray(0).getSystem().getConfigSerialNumber());
			configSerialNumber = substation.getDFRs().getDFRArray(0).getSystem().getConfigSerialNumber()+1;
		}
		for (int i = 0; i < dfrs.length; i++) {
			dfrs[i].getSystem().setConfigSerialNumber(configSerialNumber);
		}

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
		stationDetails.setTotalDfrsConfigured(substation.getDFRs().sizeOfDFRArray());
		stationDetails.setConfigStatus("COMPLETE");
//		// 20-May-2021 - Clear flag for add ot remove chassis
//		stationDetails.setDfrAddedOrRemoved(false);
		String xmlText = substation.xmlText(xmlOptions);
		Diff diff = DiffBuilder.compare(Input.fromString(stationDetails.getConfigXml())).withTest(Input.fromString(xmlText))
			     .checkForSimilar()
			     .ignoreWhitespace()
			     .build();

		logger.debug(diff.toString()+" diff has differences? "+diff.hasDifferences());
		int iLineCount = 0;
		int syslogNumLines = config.getInt("syslogNumLines",1000);
		if (diff.hasDifferences())
		{
			Iterable<Difference> iterDiffs = diff.getDifferences();
			StringBuffer strBufDiff = new StringBuffer();
			for (Difference difference : iterDiffs) {
				strBufDiff.append(difference.toString()+M9kConstants.NEWLINE);
				iLineCount++;
				if((iLineCount % syslogNumLines) == 0)
				{
					logger.info("User "+userDto.getUserName()+" modified the configuration of the station "+stationDetails.getStationDisplayName()+" "+M9kConstants.NEWLINE+strBufDiff.toString());
					strBufDiff.setLength(0);
				}
			}
			if(strBufDiff.length() > 0)
			{
				logger.info("User "+userDto.getUserName()+" modified the configuration of the station "+stationDetails.getStationDisplayName()+" "+M9kConstants.NEWLINE+strBufDiff.toString());
			}
		}

		stationDetails.setConfigXml(xmlText);
		((StationDTO)session.get("stationDetails")).setConfigStatus("COMPLETE");
		if (mysqlStationDao.isStationExists(stationDetails.getSystemStationId()))
		{
			mysqlStationDao.updateConfigXml(stationDetails);
		}
		else
		{
			mysqlStationDao.insertStationDetails(stationDetails);
		}
		// START: 23-Jul-2020 Backup config from web master everytime user does a finish or finish and send even without station master
		M9kUtils.masterBackUpConfig(stationDetails);
		// END: 23-Jul-2020
		logger.info("User "+userDto.getUserName()+" succesfully saved the configuration for station "+stationDetails.getStationDisplayName());
	}
	private void populateEventsIntoXmlConfig() {
//		TreeSet<String> keys = new TreeSet<String>(mapXMLDfrs.keySet());
//		Iterator<String> mapXmlDfrIterator = keys.iterator();
		List<String> lstDfrNames = new ArrayList<String>(mapXMLDfrs.keySet());
		Collections.sort(lstDfrNames, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int lhs = Integer.parseInt(o1.substring(3));
				int rhs = Integer.parseInt(o2.substring(3));
//				return ((lhs < rhs)?0:1);
				return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
			}
		});
		Iterator<String> mapXmlDfrIterator =lstDfrNames.iterator();
		Map<String, EventInput[]> mapEventInputs = new HashMap<String, EventInput[]>();
		String dfrKey;
		DFR xmlDfr;
		EventChannelDTO eventDto;
		for (; mapXmlDfrIterator.hasNext();) {
			dfrKey = mapXmlDfrIterator.next();
			xmlDfr = mapXMLDfrs.get(dfrKey);
			logger.debug("DFR key added "+dfrKey);
			if (xmlDfr.getDataPool().getChannels().getEvents() != null)
			{
				mapEventInputs.put(dfrKey.trim(), xmlDfr.getDataPool().getChannels().getEvents().getEventInputArray());
			}
		}
		
//		EventInput[] eventInputs = new EventInput[dfrObj.getDataPool()
//				.getChannels().getEvents().sizeOfEventInputArray()];
		EventInput[] eventInputs;
		int index = 0;
		int eventChnlTriggerIndex = 0;
		String chassis = null;
		Triggers triggers = null;
		Algorithms algorithms;
//		Algorithms algorithms = dfrObj.getDataPool().getAlgorithms();
//		if (algorithms == null)
//		{
//			algorithms = dfrObj.getDataPool().addNewAlgorithms();
//			algorithms.setName("Algorithms");
//			triggers = dfrObj.getDataPool().getAlgorithms().addNewTriggers();
//			triggers.setName("Global Trigger");
//		}
//		else
//		{
//			triggers = dfrObj.getDataPool().getAlgorithms().getTriggers();
//			if (triggers == null)
//			{
//				triggers = dfrObj.getDataPool().getAlgorithms().addNewTriggers();
//				triggers.setName("Global Trigger");
//			}
//		}
//		if (getLstEventChannels() != null && getLstEventChannels().size() > 0) {
			logger.debug("populateBackToXmlConfig: getLstEventChannels().size() " + getLstEventChannels().size());
			for (int i = 0; i < getLstEventChannels().size(); i++) {
//				eventInputs[index] = EventInputDocument.Factory.newInstance()
//						.addNewEventInput();
				eventDto = getLstEventChannels().get(i);
				logger.debug("populateBackToXmlConfig: Event Name " + eventDto.getName());
				if (chassis == null)
				{
					chassis = eventDto.getChassis();
				}
				else
				{
					if(!chassis.equalsIgnoreCase(eventDto.getChassis()))
					{
						index = 0;
						chassis = eventDto.getChassis();
					}
				}
				eventInputs = mapEventInputs.get(eventDto.getChassis().trim());
//				eventInputs[index].setChannel(Integer
//						.parseInt(eventDto.getChannel()));
//				eventInputs[index]
//						.setName(eventDto.getName());
//				logger
//						.debug("In populateBackToXmlConfig() eventDto.getName() "
//								+ eventDto.getName());
				logger.debug("populateBackToXmlConfig: Event Description " + eventDto.getDescription());
				eventInputs[index].setEventName(eventDto
						.getDescription());
//				logger.debug("set Description... "
//						+ eventInputs[index].getEventName());
				logger.debug("populateBackToXmlConfig: Normal State " + eventDto.getNormalState());
				eventInputs[index].setNormalState(eventDto
						.getNormalState());
//				logger.debug("set Normal State... " + eventInputs[index].getNormalState());
				logger.debug("populateBackToXmlConfig: DFR Start " + eventDto.getDfrStart());
				if (eventDto
						.getDfrStart() != null)
				{
					eventInputs[index].setTriggerModeDfr(eventDto
						.getDfrStart());
				}
				else
				{
					eventInputs[index].setTriggerModeDfr(M9kConstants.DISABLED);
				}
//				if (eventDto
//						.getSerRun() != null)
//				{
//					eventInputs[index].setEnableSer(Integer.parseInt(eventDto
//							.getSerRun()));
//				}
//				else
//				{
//					eventInputs[index].setEnableSer(0);
//				}
				logger.debug("populateBackToXmlConfig: IsSer enabled?  " + eventDto.isSer());
				if (eventDto.isSer())
				{
					eventInputs[index].setEnableSer(M9kConstants.ENABLE_ONE);
				}
				else
				{
					eventInputs[index].setEnableSer(M9kConstants.DISABLE_ZERO);
				}
				logger.debug("populateBackToXmlConfig: Is DFR enabled? " + eventDto.isDfr());
				if (eventDto.isDfr())
				{
					eventInputs[index].setEnableDfr(M9kConstants.ENABLE_ONE);
				}
				else
				{
					eventInputs[index].setEnableDfr(M9kConstants.DISABLE_ZERO);
				}
				
				// set debounce
				if (eventDto.isDebounce())
				{
					eventInputs[index].setDebounceMs(getStationDetails().getEventDebounce());
				}
				else
				{
					eventInputs[index].setDebounceMs(M9kConstants.DISABLE_ZERO);
				}

//				eventInputs[index].setRecordMode(eventDto
//						.getDfrSer());
//				if (eventDto
//						.getDfrSer().equalsIgnoreCase(M9kConstants.DFR))
//				{
//					eventInputs[index].setEnableDfr(M9kConstants.ENABLE_ONE);
//					eventInputs[index].setEnableSer(M9kConstants.DISABLE_ZERO);
//				}
//				else if (eventDto
//						.getDfrSer().equalsIgnoreCase(M9kConstants.SER))
//				{
//					eventInputs[index].setEnableDfr(M9kConstants.DISABLE_ZERO);
//					eventInputs[index].setEnableSer(M9kConstants.ENABLE_ONE);
//				}
//				else // BOTH Dfr and SER
//				{
//					eventInputs[index].setEnableDfr(M9kConstants.ENABLE_ONE);
//					eventInputs[index].setEnableSer(M9kConstants.ENABLE_ONE);
//				}

//				logger.debug("set Dfr Enable... "
//						+ eventInputs[index].getEnableDfr());
//				logger.debug("set SER Enable... "
//						+ eventInputs[index].getEnableSer());
//				if (eventDto.getTriggerStatus())
				// START: 22-Mar-2016 - BUG_FIX - In fault location decision logic, events trigger should appear in the list even when the status is disabled
//				if( (eventInputs[index].getEnableDfr() == M9kConstants.ENABLE_ONE) && eventDto.getDfrStart() != null && !eventDto.getDfrStart().equalsIgnoreCase(M9kConstants.DISABLED))
				if( (eventInputs[index].getEnableDfr() == M9kConstants.ENABLE_ONE) && eventDto.getDfrStart() != null )
				// END: 22-Mar-2016
				{
					xmlDfr = mapXMLDfrs.get(chassis);
					algorithms = xmlDfr.getDataPool().getAlgorithms();
					if (algorithms == null)
					{
						algorithms = xmlDfr.getDataPool().addNewAlgorithms();
						algorithms.setName("Algorithms");
						triggers = xmlDfr.getDataPool().getAlgorithms().addNewTriggers();
						triggers.setName("Global Trigger");
					}
					else
					{
						triggers = xmlDfr.getDataPool().getAlgorithms().getTriggers();
						if (triggers == null)
						{
							triggers = xmlDfr.getDataPool().getAlgorithms().addNewTriggers();
							triggers.setName("Global Trigger");
						}
					}

					populateEventTriggers(triggers, eventDto, (++eventChnlTriggerIndex));
				}
				else
				{
					++eventChnlTriggerIndex;
					if (triggers != null)
					{
						String eventName = "E"+eventChnlTriggerIndex;
						removeEventTriggersIfExists (triggers, eventName, eventChnlTriggerIndex);
					}
				}
				index++;
				createPMUExportForEvent(eventDto);
				if (getStationDetails().isPmuEnabled())
				{
					createPdcPmus();
				}
				
			}
//			dfrObj.getDataPool().getChannels().getEvents()
//					.setEventInputArray(eventInputs);
			logger.debug("Returning from populateBackToXmlConfig"+getLstEventTriggerChannels());
//		}

	}
	
	private void populateEventTriggers(Triggers triggers, EventChannelDTO eventDto, int triggerChnlIndex)
	{
		String eventName = "E"+triggerChnlIndex;
//		for (int i = 0; i < triggers.sizeOfTriggerArray(); i++) {
////			logger.debug("Available triggers "+triggers.getTriggerArray(i).getName());
//			if (triggers.getTriggerArray(i).getName().toUpperCase().startsWith("E")&&(triggers.getTriggerArray(i).getName().equalsIgnoreCase(eventName) || triggers.getTriggerArray(i).getChanId() == triggerChnlIndex))
//			{
////				logger.debug("Trigger to be removed "+triggers.getTriggerArray(i).getName());
//				triggers.removeTrigger(i);
//				break;
//			}
//		}
		logger.debug("event name "+eventName);
		removeEventTriggersIfExists (triggers, eventName, triggerChnlIndex);
		Trigger trigger = triggers.addNewTrigger();
		trigger.setName(eventName );
		trigger.setEnable(1);
		trigger.setChanId(triggerChnlIndex);
		trigger.setType("E");
		trigger.setInput(eventDto.getInputXMLString());
		if (getStationDetails() != null)
		{
			trigger.setChatterLimit(getStationDetails().getChatterLimit());
			trigger.setChatterRate(getStationDetails().getChatterRate());
			trigger.setTriggerLimit(getStationDetails().getTriggerLimit());
		}
		TriggerChannelDTO triggerChannelDTO = new TriggerChannelDTO();
		triggerChannelDTO.setName(eventName);
		triggerChannelDTO.setType("E");
		triggerChannelDTO.setInputChannelName(eventDto.getDescription());
		triggerChannelDTO.setId(triggerChnlIndex);
		if (getLstEventTriggerChannels() == null)
		{
			lstEventTriggerChannels = new ArrayList<TriggerChannelDTO>();
		}
		getLstEventTriggerChannels().add(triggerChannelDTO);
	}

	private void removeEventTriggersIfExists (Triggers triggers, String eventName, int triggerChnlIndex)
	{
		for (int i = 0; i < triggers.sizeOfTriggerArray(); i++) {
//			logger.debug("Available triggers "+triggers.getTriggerArray(i).getName());
			if (triggers.getTriggerArray(i).getName().toUpperCase().startsWith("E")&&(triggers.getTriggerArray(i).getName().equalsIgnoreCase(eventName) || triggers.getTriggerArray(i).getChanId() == triggerChnlIndex))
			{
//				logger.debug("Trigger to be removed "+triggers.getTriggerArray(i).getName());
				triggers.removeTrigger(i);
				break;
			}
		}
	}
	@SuppressWarnings("unchecked")
	public String updateAndGetEventDescription() {
		try {
			mapEventDescriptions = (Map<String, String>) session
					.get("mapEventDescriptions");
			if (mapEventDescriptions == null) {
				mapEventDescriptions = new HashMap<String, String>();
				session.put("mapEventDescriptions", mapEventDescriptions);
			}
			logger
					.info("Entered constructor of updateAndGetCircuitName getLstEventChannels().."
							+ getLstEventChannels()
							+ "  mapEventDescriptions"
							+ mapEventDescriptions);
			EventChannelDTO eventChannelDto;
			logger.debug(" mapEventDescriptions.values()... "
					+ mapEventDescriptions.values());
			lstEventDescriptions = Arrays.asList(mapEventDescriptions.values().toArray(
					new String[0]));
			json = new HashMap<String, String>();
			logger.debug("Entered updateAndGetCircuitName lstEventDescriptions..."
					+ lstEventDescriptions);
			if (selectedIndex != null) {
				reqdIndex = Integer.parseInt(selectedIndex);
				eventChannelDto = getLstEventChannels().get(reqdIndex);
				logger.debug("Length of the list... "
						+ getLstEventDescriptions().size());
				for (Iterator<String> iterator = getLstEventDescriptions()
						.iterator(); iterator.hasNext();) {
					String strCircuitName = iterator.next();
					logger.debug("strCircuiteName..." + strCircuitName);
					logger.debug("eventChannelDto.getDescriptions()..."
							+ eventChannelDto.getDescription());
					logger.debug("eventChannelDto.getDescriptionKey()..."
							+ eventChannelDto.getDescriptionKey());
					if (strCircuitName.toLowerCase().startsWith(
							eventChannelDto.getDescription().toLowerCase())) {
						logger.debug("populating JSON...");
						json.put(strCircuitName, strCircuitName);
					} else if (eventChannelDto.getDescription() != null
							&& !eventChannelDto.getDescription().isEmpty()) {
						mapEventDescriptions.put(selectedIndex, eventChannelDto
								.getDescription());
					}
				}
				if (getLstEventDescriptions() != null
						&& getLstEventDescriptions().isEmpty()
						&& eventChannelDto.getDescription() != null
						&& !eventChannelDto.getDescription().isEmpty()) {
					mapEventDescriptions.put(selectedIndex, eventChannelDto
							.getDescription());
				}
			}
			session.put("mapEventDescriptions", mapEventDescriptions);
			logger.debug("JSON array type??? " + json + " lst of Descriptions"
					+ getLstEventDescriptions() + " mapEventDescriptions.."
					+ mapEventDescriptions);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return SUCCESS;
	}

	private void populateDfrStart()
	  {
//		  lstDfrStart = new ArrayList<String>();
		  lstDfrStart = config.getList("dfrStart");
		  session.put("lstDfrStart", lstDfrStart);		  
	  }
//	private void populateDfrSer()
//	  {
////		  lstDfrSer = new ArrayList<String>();
//		  lstDfrSer = config.getList("dfrSer");
//		  session.put("lstDfrSer", lstDfrSer);		  
//	  }
//	private void populateSerRun()
//	  {
////		  lstSerRun = new ArrayList<String>();
//		  lstSerRun = config.getList("serRun");
//		  session.put("lstSerRun", lstSerRun);		  
//	  }
	public String finish() throws M9000Exception {
		String returnValue = "Finish";
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		StationDAO mysqlStationDao = m9kDAOFactory.getStationDAO();
		SubStation subStation = (SubStation)session.get("selectedSubstation");
		
		int configSerialNumber = 1;
		DFR[] dfrs = subStation.getDFRs().getDFRArray();
		logger.debug("IS confugSErialNumber set already? "+dfrs[0].getSystem().isSetConfigSerialNumber());
		if (dfrs[0].getSystem().isSetConfigSerialNumber())
		{
			logger.debug("ConfigSEriaNumber already set "+subStation.getDFRs().getDFRArray(0).getSystem().getConfigSerialNumber());
			configSerialNumber = subStation.getDFRs().getDFRArray(0).getSystem().getConfigSerialNumber()+1;
		}
		for (int i = 0; i < dfrs.length; i++) {
			dfrs[i].getSystem().setConfigSerialNumber(configSerialNumber);
		}
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSaveOuter();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setUseDefaultNamespace();
		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("", "http://www.usi.com");
		xmlOptions.setSaveImplicitNamespaces(prefixes);
		stationDetails = (StationDTO) session.get("stationDetails");
		String xmlText = subStation.xmlText(xmlOptions);
		Diff diff = DiffBuilder.compare(Input.fromString(stationDetails.getConfigXml())).withTest(Input.fromString(xmlText))
			     .checkForSimilar()
			     .ignoreWhitespace()
			     .build();

//		logger.debug(diff.toString()+" diff has differences? "+diff.hasDifferences());
		int iLineCount = 0;
		int syslogNumLines = config.getInt("syslogNumLines",1000);
		if (diff.hasDifferences())
		{
			Iterable<Difference> iterDiffs = diff.getDifferences();
			StringBuffer strBufDiff = new StringBuffer();
			for (Difference difference : iterDiffs) {
				strBufDiff.append(difference.toString()+M9kConstants.NEWLINE);
				iLineCount++;
				if((iLineCount % syslogNumLines) == 0)
				{
					logger.info("User "+userDto.getUserName()+" modified the configuration of the station "+stationDetails.getStationDisplayName()+" "+M9kConstants.NEWLINE+strBufDiff.toString());
					strBufDiff.setLength(0);
				}
			}
			if(strBufDiff.length() > 0)
			{
				logger.info("User "+userDto.getUserName()+" modified the configuration of the station "+stationDetails.getStationDisplayName()+" "+M9kConstants.NEWLINE+strBufDiff.toString());
			}
		}
		
		stationDetails.setConfigXml(xmlText);
		stationDetails.setConfigStatus("COMPLETE");
		logger.debug("station Details in Finish..." + stationDetails);
		((StationDTO)session.get("stationDetails")).setConfigStatus("COMPLETE");
		if (mysqlStationDao.isStationExists(stationDetails.getSystemStationId()))
		{
			mysqlStationDao.updateConfigXml(stationDetails);
		}
		else
		{
			mysqlStationDao.insertStationDetails(stationDetails);
		}
		// START: 23-Jul-2020 Backup config from web master everytime user does a finish or finish and send even without station master
		M9kUtils.masterBackUpConfig(stationDetails);
		// END: 23-Jul-2020
		  try
		  {
			  session.remove("linegroupsWarnings");	
			// remove measurement warning as well
			  session.remove("LINEGROUP_AFFECTED_MEASUREMENTS");
			  session.remove("MEASUREMENTS_WARNING_MESSAGE");
		  }
		  catch(Exception e)
		  {
			  logger.error("Unable to clear linegroups warning from session ",e);
		  }

		logger.info("User "+userDto.getUserName()+" succesfully saved the configuration for station "+stationDetails.getStationDisplayName());
		return returnValue;
	}
	
	@SkipValidation
	public String back() {
		String returnValue = "Back";
		logger.debug("getLstEventChannels() to be put in session "+getLstEventChannels());
		session.put("lstEventChannels", getLstEventChannels());
		if (booAnalog || stationDetails.getSystemAnalogChannelsCount() > 0)
		{
			returnValue="BackToVirtualMeasurements";
		}
		logger.debug("Returning from events channel " + returnValue);
		return returnValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSession(Map<String, Object> session) {
		// TODO Auto-generated method stub
		this.session = session;
		config = (PropertiesConfiguration) session.get("config");
		stationDetails = (StationDTO) session.get("stationDetails");
		lstDfrDTO = (List<DfrDTO>) session.get("DFRsList");
		accessMode = (String) session.get("accessMode");
		mapXMLDfrs = (Map<String, DFR>) session.get("mapXMLDfrs");
		lstLineGroups = (List<LineGroupsAlgorithm>) session.get("lstLineGroups");
		lstAllTriggerTypes = (List<Object>) session.get("lstAllTriggerTypes");
		userDto = (UsersDTO) session.get("userDetails");
	}
	
	@SuppressWarnings("unchecked")
	@SkipValidation
	public  String populateEventDTO() {
//		xmlConfigDfr = (DFR) session.get("SelectedDfrObj");
//		logger.debug("List of trigger channels from session "+lstTriggerChannels);
		mapEventDescriptions = new HashMap<String, String>();
//		TreeSet<String> keys = new TreeSet<String>(mapXMLDfrs.keySet());
//		Iterator<String> mapXmlDfrIterator = keys.iterator();
		List<String> lstDfrNames = new ArrayList<String>(mapXMLDfrs.keySet());
		Collections.sort(lstDfrNames, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int lhs = Integer.parseInt(o1.substring(3));
				int rhs = Integer.parseInt(o2.substring(3));
//				return ((lhs < rhs)?0:1);
				return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
			}
		});
		Iterator<String> mapXmlDfrIterator =lstDfrNames.iterator();
		String dfrKey= null;
		DFR xmlDfr;
		lstEventChannels =  (List<EventChannelDTO>) session.get("lstEventChannels");
		logger.debug("lstEventChannels in session "+lstEventChannels);
		if (lstEventChannels != null)
		{
			return SUCCESS;
		}
		populateDfrStart();
//		populateSerRun();
//		populateDfrSer();
		
//		EventInput[] eventInputs = xmlConfigDfr.getDataPool().getChannels().getEvents()
//		.getEventInputArray();
		EventInput[] eventInputs;
		EventChannelDTO eventDto;
		lstEventChannels = new ArrayList<EventChannelDTO>(500);
		int eventChnlIndex = 1;
		
		// START: 31-MAR-2015	Enable PMU for Events Version 1.0.7.1
		PmuInput localPmuInput;
		Pmu localPmu = null;
		// END: 31-MAR-2015	Enable PMU for Events Version 1.0.7.1
		
		for (; mapXmlDfrIterator.hasNext();) {
			dfrKey = mapXmlDfrIterator.next();
			xmlDfr = mapXMLDfrs.get(dfrKey);
			if (xmlDfr.getDataPool().getChannels().getEvents() == null)
			{
				continue;
			}
//			logger.debug("\n\n\t\t\tdfr config xml "+xmlDfr.xmlText());
			eventInputs = xmlDfr.getDataPool().getChannels().getEvents().getEventInputArray();
			// START: 31-MAR-2015	Enable PMU for Events Version 1.0.7.1
			localPmu = xmlDfr.getDataPool().getAlgorithms().getPmus().getPmuArray(0);
			// END: 31-MAR-2015	
			for (int i = 0; i < eventInputs.length; i++,eventChnlIndex++) {
				eventDto = new EventChannelDTO();
//				eventDto.setTriggerStatus(true);
				eventDto.setName(eventInputs[i].getName());
				eventDto.setChassis(dfrKey);
				eventDto.setChannel("" + eventChnlIndex);
				eventDto.setDescription(eventInputs[i].getEventName());

//				logger.debug("\t\t\t\teventDto.getName() "+eventDto.getName());
//				logger.debug("\t\t\t\tlocalPmu "+localPmu.getName());
//				logger.debug("\t\t\t\tlocalPmu Pmu inputs size"+localPmu.getPmuInputArray().length);
				localPmuInput = getPmuIfAlreadyExists(localPmu, eventDto.getName());
				if (localPmuInput != null && localPmuInput.getPmuEnable() == M9kConstants.ENABLE_ONE)
				{
					eventDto.setPmu(true);
				}
				else
				{
					eventDto.setPmu(false);
				}
				// END: 31-MAR-2015	Enable PMU for Events Version 1.0.7.1
				
				
				if (eventInputs[i].getEventName() != null
						&& !eventInputs[i].getEventName().isEmpty()) {
					mapEventDescriptions.put("" + i, eventInputs[i].getEventName());
				}
				if (getAccessMode() != null && !getAccessMode().isEmpty()) {
					if (eventInputs[i].getNormalState() != null)
					{
						eventDto.setNormalState(eventInputs[i].getNormalState());
					}
					else
					{
						eventDto.setNormalState("0");
					}
					eventDto.setDfrStart(eventInputs[i].getTriggerModeDfr());
					if (eventInputs[i].getEnableDfr() == M9kConstants.ENABLE_ONE
							&& eventInputs[i].getEnableSer() == M9kConstants.ENABLE_ONE)
					{
//						eventDto.setDfrSer(M9kConstants.BOTH);
						eventDto.setDfr(true);
						eventDto.setSer(true);
						eventDto.setDisableDfrStart(false);
//						eventDto.setDisableSerRun(false);
//						eventDto.setSerRun(""+M9kConstants.ENABLE_ONE);
					}
					else if (eventInputs[i].getEnableDfr() == M9kConstants.ENABLE_ONE
							&& eventInputs[i].getEnableSer() == M9kConstants.DISABLE_ZERO)
					{
//						eventDto.setDfrSer(M9kConstants.DFR);
						eventDto.setDisableDfrStart(false);
//						eventDto.setDisableSerRun(true);
//						eventDto.setSerRun(""+M9kConstants.DISABLE_ZERO);
						eventDto.setDfr(true);
						eventDto.setSer(false);

					}
					else if (eventInputs[i].getEnableDfr() == M9kConstants.DISABLE_ZERO
							&& eventInputs[i].getEnableSer() == M9kConstants.ENABLE_ONE)
					{
//						eventDto.setDfrSer(M9kConstants.SER);
						eventDto.setDisableDfrStart(true);
//						eventDto.setTriggerStatus(false);
//						eventDto.setDisableSerRun(false);
//						eventDto.setSerRun(""+M9kConstants.ENABLE_ONE);
						eventDto.setDfr(false);
						eventDto.setSer(true);

					}
					if (eventInputs[i].getDebounceMs() > M9kConstants.DISABLE_ZERO)
					{
						eventDto.setDebounce(true);
					}
					else
					{
						eventDto.setDebounce(false);
					}
//					if (eventInputs[i].getTriggerModeDfr() != null && eventInputs[i].getTriggerModeDfr().equalsIgnoreCase(M9kConstants.DISABLED))
//					{
//						eventDto.setTriggerStatus(false);
//					}
//					eventDto.setSerRun(""+eventInputs[i].getEnableSer());
				}
				else
				{
					if (eventDto.getDescription() == null || eventDto.getDescription().isEmpty())
					{
						eventDto.setDescription("Event " + eventChnlIndex);
					}
					eventDto.setNormalState("0");
//					eventDto.setDfrSer(M9kConstants.BOTH);
//					eventDto.setNormalState("Open");
//					eventDto.setSerRun("1");
					eventDto.setDisableDfrStart(false);
//					eventDto.setDisableSerRun(false);
					eventDto.setDfrStart("NormalToAbnormal");
					eventDto.setDebounce(true);
				}

				getLstEventChannels().add(eventDto);
			}
		}
		session.put("mapEventDescriptions", mapEventDescriptions);
//		logger.debug("lstEventChannels stored in session..."
//				+ getLstEventChannels());
		session.remove("lstEventChannels");
		session.put("lstEventChannels", lstEventChannels);
		return SUCCESS;
	}
	
	
	@SuppressWarnings("unused")
	private void populateFaultLocEventTriggers()
	{
		logger.debug("Entered populateFaultLocEventTriggers...");
//		mapEventDescriptions = new HashMap<String, String>();
//		TreeSet<String> keys = new TreeSet<String>(mapXMLDfrs.keySet());
//		Iterator<String> mapXmlDfrIterator = keys.iterator();
		List<String> lstDfrNames = new ArrayList<String>(mapXMLDfrs.keySet());
		Collections.sort(lstDfrNames, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int lhs = Integer.parseInt(o1.substring(3));
				int rhs = Integer.parseInt(o2.substring(3));
//				return ((lhs < rhs)?0:1);
				return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
			}
		});
		Iterator<String> mapXmlDfrIterator =lstDfrNames.iterator();
		String dfrKey= null;
		DFR xmlDfr;

		for (; mapXmlDfrIterator.hasNext();) {
			dfrKey = mapXmlDfrIterator.next();
			xmlDfr = mapXMLDfrs.get(dfrKey);
			logger.debug("DFR to be processed DFR"+xmlDfr.getSystem().getDfrId());
			if (xmlDfr.getDataPool().getChannels().getEvents() == null)
			{
				logger.debug("No Events skipping...");
				continue;
			}

		Triggers triggers = xmlDfr.getDataPool().getAlgorithms().getTriggers();
		Trigger[] arrTriggers = null; 
		if (triggers != null)
		{
			arrTriggers = triggers.getTriggerArray();
		}
		logger.debug("Array of triggers "+arrTriggers);
		if (arrTriggers != null && arrTriggers.length > 0)
		{
			logger.debug("Array of triggers length "+arrTriggers.length);
			TriggerChannelDTO triggerChannelDTO;
			for (int i = 0; i < arrTriggers.length; i++) {
				logger.debug("arrTriggers["+i+"] Name "+arrTriggers[i].getName()+arrTriggers[i].getEnable()+" arrTriggers[i]. getType "+arrTriggers[i].getType());
//				if (arrTriggers[i].getEnable() == M9kConstants.ENABLE_ONE && arrTriggers[i].getType() != null && arrTriggers[i].getType().equalsIgnoreCase("E"))
				if (arrTriggers[i].getType() != null && arrTriggers[i].getType().equalsIgnoreCase("E"))
				{
					triggerChannelDTO = new TriggerChannelDTO();
					triggerChannelDTO.setName(arrTriggers[i].getName());
					triggerChannelDTO.setType("E");
					triggerChannelDTO.setId(arrTriggers[i].getChanId());
					triggerChannelDTO.setInputChannelName(getEventTriggerName(arrTriggers[i].getInput()));
					if (getLstEventTriggerChannels() == null)
					{
						lstEventTriggerChannels = new ArrayList<TriggerChannelDTO>();
					}
					logger.debug("adding a event trigger to channels list "+triggerChannelDTO);
					getLstEventTriggerChannels().add(triggerChannelDTO);		
				}
			}
		}
		}
	}
	
	private String getEventTriggerName(String eventsInputChannelXml)
	{
		int startIindex = eventsInputChannelXml.indexOf(M9kXMLConstants.XML_EVENT_CHANNELS_INPUT) + M9kXMLConstants.XML_EVENT_CHANNELS_INPUT.length();
		int endIndex = eventsInputChannelXml.indexOf(M9kXMLConstants.XML_VALUE_INPUT);
//		System.out.println("\n\nEvents Name "+eventsInputChannelXml.substring(startIindex, endIndex));
		return eventsInputChannelXml.substring(startIindex, endIndex);

	}
	public Map<String, Object> getSession() {
		return session;
	}

	/**
	 * @return the stationDetails
	 */
	public StationDTO getStationDetails() {
		return stationDetails;
	}

	/**
	 * @param stationDetails
	 *            the stationDetails to set
	 */
	public void setStationDetails(StationDTO sessionStationDetails) {
		this.stationDetails = sessionStationDetails;
	}

	/**
	 * @return the eventChannelsCount
	 */
	public String getEventChannelsCount() {
		return eventChannelsCount;
	}

	/**
	 * @param eventChannelsCount
	 *            the eventChannelsCount to set
	 */
	public void setEventChannelsCount(String eventChannelsCount) {
		this.eventChannelsCount = eventChannelsCount;
	}

	/**
	 * @return the minimumDFRCount
	 */
	public int getMinimumDFRCount() {
		return minimumDFRCount;
	}

	/**
	 * @param minimumDFRCount
	 *            the minimumDFRCount to set
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
	 * @param lstDfrDTO
	 *            the lstDfrDTO to set
	 */
	public void setLstDfrDTO(List<DfrDTO> lstDfrDTO) {
		this.lstDfrDTO = lstDfrDTO;
	}

	/**
	 * @return the selectedDfr
	 */
	public String getSelectedDfr() {
		return selectedDfr;
	}

	/**
	 * @param selectedDfr
	 *            the selectedDfr to set
	 */
	public void setSelectedDfr(String selectedDfr) {
		this.selectedDfr = selectedDfr;
	}

	/**
	 * @return the eventChannelCounter
	 */
	public Counter getEventChannelCounter() {
		return eventChannelCounter;
	}

	/**
	 * @param eventChannelCounter
	 *            the eventChannelCounter to set
	 */
	public void setEventChannelCounter(Counter eventChannelCounter) {
		this.eventChannelCounter = eventChannelCounter;
	}

	/**
	 * @return the triggerChannelCounter
	 */
	public Counter getTriggerChannelCounter() {
		return triggerChannelCounter;
	}

	/**
	 * @param triggerChannelCounter
	 *            the triggerChannelCounter to set
	 */
	public void setTriggerChannelCounter(Counter triggerChannelCounter) {
		this.triggerChannelCounter = triggerChannelCounter;
	}

	/**
	 * @return the eventInput
	 */
	public EventInput[] getEventInput() {
		return eventInput;
	}

	/**
	 * @param eventInput
	 *            the eventInput to set
	 */
	public void setEventInput(EventInput[] eventinput) {
		this.eventInput = eventinput;
	}
	/**
	 * @return the lstEventDescriptions
	 */
	public List<String> getLstEventDescriptions() {
		return lstEventDescriptions;
	}

	/**
	 * @param lstEventDescriptions
	 *            the lstEventDescriptions to set
	 */
	public void setLstEventDescriptions(List<String> lstCircuitNames) {
		this.lstEventDescriptions = lstCircuitNames;
	}

	/**
	 * @return the selectedIndex
	 */
	public String getSelectedIndex() {
		return selectedIndex;
	}

	/**
	 * @param selectedIndex
	 *            the selectedIndex to set
	 */
	public void setSelectedIndex(String selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	/**
	 * @return the json
	 */
	public Map<String, String> getJson() {
		return json;
	}

	/**
	 * @param json
	 *            the json to set
	 */
	public void setJson(Map<String, String> json) {
		this.json = json;
	}

	/**
	 * @return the accessMode
	 */
	public String getAccessMode() {
		return accessMode;
	}

	/**
	 * @param accessMode
	 *            the accessMode to set
	 */
	public void setAccessMode(String accessMode) {
		this.accessMode = accessMode;
	}

	/**
	 * @return the lstEventChannels
	 */
//	@VisitorFieldValidator(message = "!!! ")
	public List<EventChannelDTO> getLstEventChannels() {
		return lstEventChannels;
	}

	/**
	 * @param lstEventChannels the lstEventChannels to set
	 */
	public void setLstEventChannels(List<EventChannelDTO> lstEventChannels) {
		this.lstEventChannels = lstEventChannels;
	}

	/**
	 * @return the lstDfrStart
	 */
	public List<Object> getLstDfrStart() {
		return lstDfrStart;
	}

	/**
	 * @param lstDfrStart the lstDfrStart to set
	 */
	public void setLstDfrStart(List<Object> lstDfrStart) {
		this.lstDfrStart = lstDfrStart;
	}

	/**
	 * @return the lstDfrSer
	 */
	public List<Object> getLstDfrSer() {
		return lstDfrSer;
	}

	/**
	 * @param lstDfrSer the lstDfrSer to set
	 */
	public void setLstDfrSer(List<Object> lstDfrSer) {
		this.lstDfrSer = lstDfrSer;
	}

	/**
	 * @return the lstSerRun
	 */
	public List<Object> getLstSerRun() {
		return lstSerRun;
	}

	/**
	 * @param lstSerRun the lstSerRun to set
	 */
	public void setLstSerRun(List<Object> lstSerRun) {
		this.lstSerRun = lstSerRun;
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
	 * @return the sourceTab
	 */
	public String getSourceTab() {
		return sourceTab;
	}

	/**
	 * @param sourceTab the sourceTab to set
	 */
	public void setSourceTab(String sourceTab) {
		this.sourceTab = sourceTab;
	}

	public Map<String, DFR> getMapXMLDfrs() {
		return mapXMLDfrs;
	}

	public void setMapXMLDfrs(Map<String, DFR> mapXMLDfrs) {
		this.mapXMLDfrs = mapXMLDfrs;
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

	public List<TriggerChannelDTO> getLstEventTriggerChannels() {
		return lstEventTriggerChannels;
	}

	public void setLstEventTriggerChannels(
			List<TriggerChannelDTO> lstEventTriggerChannels) {
		this.lstEventTriggerChannels = lstEventTriggerChannels;
	}

	public List<LineGroupsAlgorithm> getLstLineGroups() {
		return lstLineGroups;
	}

	public void setLstLineGroups(List<LineGroupsAlgorithm> lstLineGroups) {
		this.lstLineGroups = lstLineGroups;
	}

	public Boolean getLineGroupExists() {
		return lineGroupExists;
	}

	public void setLineGroupExists(Boolean lineGroupExists) {
		this.lineGroupExists = lineGroupExists;
	}
	
	public void validate()
	{
		int iEventCnt = 0;
		if (lstEventChannels != null)
		{
			for (Iterator<EventChannelDTO> iterator = lstEventChannels.iterator(); iterator.hasNext();) {
				EventChannelDTO eventChannelDTO = iterator.next();
				if (eventChannelDTO.getDescription() == null || eventChannelDTO.getDescription().trim().isEmpty())
				{
					addFieldError("lstEventChannels["+iEventCnt+"].description", "Event Description is Required");
				}
				// START: 12-Sept-2023 - Allow all special characters except for comma in description
//				else if (eventChannelDTO.getDescription().indexOf(",") != -1 || eventChannelDTO.getDescription().indexOf("/") != -1 || eventChannelDTO.getDescription().indexOf("(") != -1
//						|| eventChannelDTO.getDescription().indexOf(")") != -1 || eventChannelDTO.getDescription().indexOf("[") != -1 || eventChannelDTO.getDescription().indexOf("]") != -1
//						|| eventChannelDTO.getDescription().indexOf("\\") != -1)
				if (eventChannelDTO.getDescription().indexOf(",") != -1 )
				{
//					addFieldError("lstEventChannels["+iEventCnt+"].description", "Special characters not allowed: / ( ) [ ] ,");
					addFieldError("lstEventChannels["+iEventCnt+"].description", "Event Description contains invalid character: ,");
				}
				// END: 12-Sept-2023
				iEventCnt++;
			}
		}
	}

	public UsersDTO getUserDto() {
		return userDto;
	}

	public void setUserDto(UsersDTO userDto) {
		this.userDto = userDto;
	}

	public List<Object> getLstAllTriggerTypes() {
		return lstAllTriggerTypes;
	}

	public void setLstAllTriggerTypes(List<Object> lstAllTriggerTypes) {
		this.lstAllTriggerTypes = lstAllTriggerTypes;
	}
	
	// START: 31-MAR-2015	Enable PMU for Events Version 1.0.7.1
	private void createPMUExportForEvent(EventChannelDTO eventDto)
	{
		Pmu localPmu = null;
		PmuInput localPmuInput = null;
		
		DFR xmlConfigDfr = mapXMLDfrs.get(eventDto.getChassis());
		
		localPmu = xmlConfigDfr.getDataPool().getAlgorithms().getPmus().getPmuArray(0);
		localPmuInput = getPmuIfAlreadyExists(localPmu, eventDto.getName());
		if (localPmuInput == null)
		{
			localPmuInput = localPmu.addNewPmuInput();
			localPmuInput.setName(eventDto.getName());
			localPmuInput.setInput(eventDto.getInputXMLString());
			localPmuInput.setPmuInputType("Event");
		}
		localPmuInput.setPmuInputName(eventDto.getDescription());
		if (eventDto.isPmu())
		{
			localPmuInput.setPmuEnable(M9kConstants.ENABLE_ONE);
		}
		else
		{
			localPmuInput.setPmuEnable(M9kConstants.DISABLE_ZERO);
		}
	}
	
	private PmuInput getPmuIfAlreadyExists(Pmu localPmu, String pmuName)
	{
//		logger.debug("Pmu Name to be compared "+pmuName);
		PmuInput pmuInput = null;
		PmuInput[] arrPmuInput = localPmu.getPmuInputArray();
		for (int i = 0; i < arrPmuInput.length; i++) {
//			logger.debug("arrPmu[i].getName() "+arrPmuInput[i].getName());
			if (arrPmuInput[i].getName().trim().equalsIgnoreCase(pmuName.trim()))
			{
				pmuInput = arrPmuInput[i];
				break;
			}
		}
		return pmuInput;
	}
	
	private void createPdcPmus()
	{
		List<String> lstDfrNames = new ArrayList<String>(mapXMLDfrs.keySet());
		Collections.sort(lstDfrNames, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int lhs = Integer.parseInt(o1.substring(3));
				int rhs = Integer.parseInt(o2.substring(3));
//				return ((lhs < rhs)?0:1);
				return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
			}
		});
		Iterator<String> mapXmlDfrIterator = lstDfrNames.iterator();
		String dfrKey;
		Algorithms algorithms = null;
		Pmu localPmu = null;
		SubStation currentSubstation = (SubStation)session.get("selectedSubstation");
		Pdc pdc =  currentSubstation.getPdc();
		Pmus pdcPmus = pdc.getPmus();
		if (pdcPmus == null)
		{
			pdcPmus = pdc.addNewPmus();
		}
		Pmu pdcPmu;
		DFR xmlConfigDfr;
		for (; mapXmlDfrIterator.hasNext();) {
			dfrKey = mapXmlDfrIterator.next();
			
			xmlConfigDfr = mapXMLDfrs.get(dfrKey);
			if (getStationDetails().isPmuEnabled())
			{
				boolean isPmuEnabledForAtleastOneChannel = false;
				algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
				localPmu = algorithms.getPmus().getPmuArray(0);
				for (int i = 0; i < localPmu.sizeOfPmuInputArray(); i++) {
					if (localPmu.getPmuInputArray(i).getPmuEnable() == M9kConstants.ENABLE_ONE)
					{
						isPmuEnabledForAtleastOneChannel = true;
						break;
					}
				}
//				  if (localPmu.sizeOfPmuInputArray() > 0)
				if (isPmuEnabledForAtleastOneChannel)
				{
				  localPmu.setPmuEnable(M9kConstants.ENABLE_ONE);
				  pdcPmu = null;
//				  logger.debug("pdcPmus.sizeOfPmuArray() "+pdcPmus.sizeOfPmuArray()+" XML Content "+pdcPmus.xmlText());
				  for (int i = 0; pdcPmus != null && i < pdcPmus.sizeOfPmuArray(); i++) {
//					  logger.debug("PDC Pmu index i= "+i+"Pmu xml content "+pdcPmus.getPmuArray(i).xmlText());
					  logger.debug("DFR ip "+xmlConfigDfr.getIPAddress());
					  logger.debug("Pmu ID "+pdcPmus.getPmuArray(i).getPmuId());
					  if (pdcPmus.getPmuArray(i).getPmuServerIp().equalsIgnoreCase(xmlConfigDfr.getIPAddress()))
					  {
						  pdcPmu = pdcPmus.getPmuArray(i); 
						  break;
					  }
				  }
				  if (pdcPmu == null)
				  {
					  pdcPmu = pdcPmus.addNewPmu();
				  }
				  pdcPmu.setPmuServerIp(xmlConfigDfr.getIPAddress());
				  pdcPmu.setPmuId(Integer.parseInt(xmlConfigDfr.getIPAddress().substring(xmlConfigDfr.getIPAddress().lastIndexOf(".")+1)));
				  pdcPmu.setPmuPort(stationDetails.getPmuPort());
				  pdcPmu.setPmuUdpPort(stationDetails.getPmuUdpPort());
//				  logger.debug("About to set PMU stream type "+stationDetails.getPmuStreamType());
				  pdcPmu.setPmuStreamType(stationDetails.getPmuStreamType());
//				  pdcPmu.setPmuPhasorMode(stationDetails.getPmuPhasorMode());
//				  pdcPmu.setPmuDataRate(stationDetails.getPmuDataRate());
				  pdcPmu.setPmuEnable(M9kConstants.ENABLE_ONE);
				}
				else
				{
					localPmu.setPmuEnable(M9kConstants.DISABLE_ZERO);
				  for (int i = 0; pdcPmus != null && i < pdcPmus.sizeOfPmuArray(); i++) {
					  pdcPmu = pdcPmus.getPmuArray(i);
					  if (pdcPmu.getPmuServerIp().equalsIgnoreCase(xmlConfigDfr.getIPAddress()))
					  {
						  pdcPmus.removePmu(i);
						  break;
					  }
				  }
				}
			}
			else
			{
				if (pdcPmus != null)
				{
					for (int i = 0; pdcPmus != null && i < pdcPmus.sizeOfPmuArray(); i++) {
						  pdcPmu = pdcPmus.getPmuArray(i);
						  if (pdcPmu.getPmuServerIp().equalsIgnoreCase(xmlConfigDfr.getIPAddress()))
						  {
							  pdcPmus.removePmu(i);
							  break;
						  }
					 }
				}
			}
			
		}

	}

	private void updatePmuDetails()
	{
		Pmu localPmu;
		Iterator<String> mapXmlDfrIterator = mapXMLDfrs.keySet().iterator();
		String dfrKey;
		Pmus pmus = null;
		DFR xmlConfigDfr;
		for (; mapXmlDfrIterator.hasNext();) {
			dfrKey = mapXmlDfrIterator.next();
			xmlConfigDfr = mapXMLDfrs.get(dfrKey);
//			logger.debug("DFR id "+xmlConfigDfr.getSystem().getDfrId());
			pmus = (Pmus) xmlConfigDfr.getDataPool().getAlgorithms().getPmus();
			if (pmus != null)
			{
				localPmu = pmus.getPmuArray(0);
				localPmu.setName(dfrKey);
				localPmu.setPmuName(dfrKey);
				localPmu.setPmuId(Integer.parseInt(xmlConfigDfr.getIPAddress().substring(xmlConfigDfr.getIPAddress().lastIndexOf(".")+1)));
				localPmu.setPmuPort(stationDetails.getPmuPort());
				localPmu.setUdpPort(stationDetails.getPmuUdpPort());
//				logger.debug("\n\n\n\n\t\t\t\tSetting UDP port here "+localPmu.getUdpPort());
				localPmu.setPmuStreamType(stationDetails.getPmuStreamType());
				localPmu.setPmuPhasorMode(stationDetails.getPmuPhasorMode());
				localPmu.setPmuDataRate(stationDetails.getPmuDataRate());
				// Changed for PDC implementation 4-Oct-2013
				//				if (stationDetails.getPmuStatus().equalsIgnoreCase(M9kConstants.ENABLE))
				if (getStationDetails().isPmuEnabled())
					// END  
				{
					boolean isPmuEnabledForAtleastOneChannel = false;
					Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
					localPmu = algorithms.getPmus().getPmuArray(0);
					for (int i = 0; i < localPmu.sizeOfPmuInputArray(); i++) {
						if (localPmu.getPmuInputArray(i).getPmuEnable() == M9kConstants.ENABLE_ONE)
						{
							isPmuEnabledForAtleastOneChannel = true;
							break;
						}
					}
					if (isPmuEnabledForAtleastOneChannel)
					{
						localPmu.setPmuEnable(M9kConstants.ENABLE_ONE);
					}
				}
				else
				{
					localPmu.setPmuEnable(M9kConstants.DISABLE_ZERO);
				}
			}
		}
		if (getStationDetails().isPmuEnabled())
		{
			createPdcPmus();
		}
	}

	
	// END: 31-MAR-2015
}
