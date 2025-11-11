package com.usi.m9000.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.apache.xmlbeans.XmlOptions;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.AnalogInputDocument;
import com.usi.AnalogInputDocument.AnalogInput;
import com.usi.DFRDocument.DFR;
import com.usi.EventInputDocument.EventInput;
import com.usi.LineGroupDocument.LineGroup;
import com.usi.LineGroupsDocument.LineGroups;
import com.usi.SubStationDocument.SubStation;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.common.email.SendMailUSI;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.EmailReportsSettingsDTO;
import com.usi.m9000.dto.EmailSettingsDTO;
import com.usi.m9000.dto.EventChannelDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kMessagesUtil;
import com.usi.m9000.util.M9kReportUtil;
import com.usi.m9000.util.M9kUtils;

public class ConfigureChannelsAction extends ActionSupport implements
		SessionAware, ServletContextListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String selectedDfr;
	private Map<String, Object> session;
	private StationDTO stationDetails;
	private String analogChannelsCount;
	private String eventChannelsCount;
	int minimumDFRCount;
//	private Counter analogChannelCounter;
//	private Counter eventChannelCounter;
//	private Counter triggerChannelCounter;
//	ChannelsListInfo channelsConfList;
	List<DfrDTO> lstDfrDTO;
	SubStation currentSubstation;
	DfrDTO currentDfrDto = null;
	DFR xmlConfigDfr;
	List<AnalogChannelDTO> lstAnalogChannels;
	List<EventChannelDTO> lstEventChannels;
	List<AnalogChannelDTO> lstAvailableAnalogChannels;
	List<LineGroupsAlgorithm> lstLineGroups;
	int selectedLineGroupIndex;
	EventInput[] eventInput;
	List<String> lstCircuitNames;
	Map<String, String> mapCircuitNames;
	Map<String, String> mapEventDescriptions;
	private Map<String, String> json;
	String selectedIndex;
	int reqdIndex;
	List<StationDTO> lstAvailableStations;
	String accessMode;
	String sourceTab;
	boolean booAnalog;
	boolean booDigital;
	private boolean lineGroupExists;
	UsersDTO userDto;
	// 25-May-2021 - Moved dfrs channels options list from ChannelsListInfo to here
	private List<String> channelsConfList;
		
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ConfigureChannelsAction.class);

	/**
	 * 
	 */
	public ConfigureChannelsAction(StationDTO stationDetails) {
		this();
		this.stationDetails = stationDetails;
	}

	public ConfigureChannelsAction() {
//		logger.info("User "+getUserDto()!=null?getUserDto().getUserName():""+" is in the Analogs configuration screen");
		logger.debug("Entered constructor of ConfigureChannelsAction");
		setChannelsConfList(M9kUtils.getDfrsChannelsOptionsList());
	}
	
	public String execute()
	{
		String navigateValue = "";
		try
		{
			logger.debug("Inside execute method..."+getLstAnalogChannels());
			logger.debug("sourceTab...."+sourceTab);
			session.put("sourceTab", sourceTab);
			if (getSourceTab().equalsIgnoreCase("LineGroups"))
			{
				navigateValue = configureLineGroups();
			}
			else
			{
				configureLineGroups();
				navigateValue="Measurements";
			}
			
		}
		catch (Exception e) {
			logger.error("Error navigating to LineGroups ",e);
		}
		return navigateValue;
	}

	private List<LineGroupsAlgorithm> createLineGroups() {
		List<LineGroupsAlgorithm> lstLineGroups = new ArrayList<LineGroupsAlgorithm>();
		LineGroupsAlgorithm lineGroups;
		AnalogChannelDTO analogDto;
		List<AnalogChannelDTO> lstChannelsGroup;
		Map<String, List<AnalogChannelDTO>> mapLineGroups = new HashMap<String, List<AnalogChannelDTO>>();
		for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels()
				.iterator(); iterator.hasNext();) {
			analogDto = iterator.next();
			if (analogDto.getCircuitName() != null
					&& !analogDto.getCircuitName().isEmpty()) {
				if (mapLineGroups.get(analogDto.getCircuitName()) != null) {
					mapLineGroups.get(analogDto.getCircuitName())
							.add(analogDto);
				} else {
					lstChannelsGroup = new ArrayList<AnalogChannelDTO>();
					lstChannelsGroup.add(analogDto);

					mapLineGroups.put(analogDto.getCircuitName(),
							lstChannelsGroup);
				}
			}
		}
		String key = "";
		String type;
		for (Iterator<String> iterator = mapLineGroups.keySet().iterator(); iterator
				.hasNext();) {
			key = iterator.next();
			List<AnalogChannelDTO> inputValues = (List<AnalogChannelDTO>) mapLineGroups
					.get(key);
			if (inputValues.size() > 1) {
				type = getLineGroupType(inputValues);
				lineGroups = new LineGroupsAlgorithm();
				lineGroups.setName(key);
				lineGroups.setInputChannels(inputValues);
				lineGroups.setType(type);
				lstLineGroups.add(lineGroups);
			}
		}
		return lstLineGroups;
	}

	private String getLineGroupType(List<AnalogChannelDTO> linegroupChannels)
	{
		String type = "";
		int typeVoltage = 0;
		int typeCurrent = 0;
		int phaseA_Volts = 0;
		int phaseB_Volts = 0;
		int phaseC_Volts = 0;
		int phaseA_Amps = 0;
		int phaseB_Amps = 0;
		int phaseC_Amps = 0;
		
		for (Iterator<AnalogChannelDTO> iterator = linegroupChannels.iterator(); iterator
				.hasNext();) {
			AnalogChannelDTO analogChannelDTO =  iterator
					.next();
//			logger.info("Input Type..."+analogChannelDTO.getInputType());
			if (analogChannelDTO.getInputType().startsWith(M9kConstants.VOLTAGE_AC))
			{
				typeVoltage++;
				if (analogChannelDTO.getPhase().equalsIgnoreCase("A"))
				{
					phaseA_Volts++;
				}
				else if (analogChannelDTO.getPhase().equalsIgnoreCase("B"))
				{
					phaseB_Volts++;
				}
				else if (analogChannelDTO.getPhase().equalsIgnoreCase("C"))
				{
					phaseC_Volts++;
				}
			}
			else if (analogChannelDTO.getInputType().startsWith(M9kConstants.CURRENT_AC_INTERNAL) 
					|| analogChannelDTO.getInputType().startsWith(M9kConstants.CURRENT_AC_EXTERNAL)
					// START: 29-Jun-2020 - Hall Effect offset correction implementation
					|| analogChannelDTO.getInputType().equalsIgnoreCase(M9kConstants.CURRENT_AC_HALL_EFFECT_EXTERNAL_SHUNT)
					// END: 29-Jun-2020 
					)
			{
				typeCurrent++;
				if (analogChannelDTO.getPhase().equalsIgnoreCase("A"))
				{
					phaseA_Amps++;
				}
				else if (analogChannelDTO.getPhase().equalsIgnoreCase("B"))
				{
					phaseB_Amps++;
				}
				else if (analogChannelDTO.getPhase().equalsIgnoreCase("C"))
				{
					phaseC_Amps++;
				}
			}
			
		}
//		logger.info("typeVoltage..."+typeVoltage);
//		logger.info("typeCurrent..."+typeCurrent);
//		logger.info("phaseA Volts..."+phaseA_Volts);
//		logger.info("phaseA Amps..."+phaseA_Amps);
//		logger.info("phaseB Volts..."+phaseB_Volts);
//		logger.info("phaseB Amps..."+phaseB_Amps);
//		logger.info("phaseC Volts..."+phaseC_Volts);
//		logger.info("phaseC Amps..."+phaseC_Amps);

		if (typeVoltage > 3 && typeCurrent == 0)
		{
			
			if (phaseA_Volts > 0 && phaseB_Volts > 0 && phaseC_Volts > 0)
			{
				type = M9kConstants.SEQ_VOLTAGE;
			}
		}
		else if (typeVoltage == 0 && typeCurrent > 3)
		{
			if (phaseA_Amps > 0 && phaseB_Amps > 0 && phaseC_Amps > 0)
			{
				type = M9kConstants.SEQ_CURRENT;
			}
		}
		else if (typeVoltage > 2 && typeCurrent > 2)
		{
			if (phaseA_Volts > 0 && phaseB_Volts > 0 && phaseC_Volts > 0
					&& phaseA_Amps > 0 && phaseB_Amps > 0 && phaseC_Amps > 0)
			{
				type = M9kConstants.ALL_PHASE;
			}
		}
		else if (typeVoltage > 0 && typeCurrent > 0)
		{
			if ((phaseA_Volts + phaseA_Amps) > 1)
			{
				type = M9kConstants.A_PHASE;
				if ((phaseB_Volts + phaseB_Amps) > 1)
				{
					type = M9kConstants.A_B_PHASE;
				}
				else if ((phaseC_Volts + phaseC_Amps) > 1)
				{
					type = M9kConstants.A_C_PHASE;
				}
			}
			else if ((phaseB_Volts + phaseB_Amps) > 1)
			{
				type = M9kConstants.B_PHASE;
				if ((phaseC_Volts + phaseC_Amps) > 1)
				{
					type = M9kConstants.B_C_PHASE;
				}
			}
			else if ((phaseC_Volts + phaseC_Amps) > 1)
			{
				type = M9kConstants.C_PHASE;
			}
		}
		return type;
	}
	private List<LineGroupsAlgorithm> editLineGroups(DFR xmlConfigDfr) {
		lstLineGroups = new ArrayList<LineGroupsAlgorithm>();
		LineGroupsAlgorithm lineGroups;

		List<AnalogChannelDTO> lgInput;
		if (xmlConfigDfr.getDataPool().getLineGroups() != null) {
			String type;
			LineGroup xmlLineGroup[] = xmlConfigDfr.getDataPool()
					.getLineGroups().getLineGroupArray();
			for (int i = 0; i < xmlLineGroup.length; i++) {
				lineGroups = new LineGroupsAlgorithm();
				lineGroups.setName(xmlLineGroup[i].getName());
				lgInput = getAnalogChannelsForNames(xmlLineGroup[i]
						.getInputArray());
				type = getLineGroupType(lgInput);
				lineGroups.setInputChannels(lgInput);
				lineGroups.setType(type);
				lstLineGroups.add(lineGroups);
			}
		}
		return lstLineGroups;
	}

	private List<AnalogChannelDTO> getAnalogChannelsForNames(
			String[] lgInputNames) {
		String channelName;
		AnalogChannelDTO analogDto;
		List<AnalogChannelDTO> lgInput = new ArrayList<AnalogChannelDTO>();
		for (int i = 0; i < lgInputNames.length; i++) {
			channelName = lgInputNames[i].substring(lgInputNames[i]
					.indexOf("AnalogInput(") + 12, lgInputNames[i]
					.lastIndexOf(")"));
			for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels()
					.iterator(); iterator.hasNext();) {
				analogDto = iterator.next();
				if (analogDto.getName().equalsIgnoreCase(channelName)) {
					lgInput.add(analogDto);
					logger.debug("Added " + analogDto.getName());
				}
			}
		}

		return lgInput;
	}

	
//	private void populateAnalogDTO() {
//		mapCircuitNames = new HashMap<String, String>();
//
//		AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels()
//				.getAnalogs().getAnalogInputArray();
//		AnalogChannelDTO analogDto;
//		logger.info("populateAnalogDTO: DFR name: " + xmlConfigDfr.getSystem().getDfrId());
//		lstAnalogChannels = new ArrayList<AnalogChannelDTO>(analogInputs.length);
//		for (int i = 0; i < analogInputs.length; i++) {
//			analogDto = new AnalogChannelDTO();
//			analogDto.setName(analogInputs[i].getName());
//			analogDto.setChannel("" + (analogInputs[i].getChannel()+currentDfrDto.getAnalogChannelStart()-1));
//			if (analogInputs[i].getExport() == M9kConstants.ENABLE_ONE)
//			{
//				analogDto.setExportStatus(true);
//			}
//			else
//			{
//				analogDto.setExportStatus(false);
//			}
//			logger.info("analogInputs[i].getCircuitName() ... " + i + " --> "
//					+ analogInputs[i].getCircuitName());
//			analogDto.setCircuitName(analogInputs[i].getCircuitName());
//			if (analogInputs[i].getCircuitName() != null
//					&& !analogInputs[i].getCircuitName().isEmpty()) {
//				mapCircuitNames.put("" + i, analogInputs[i].getCircuitName());
//			}
//			analogDto.setPhase(analogInputs[i].getPhase());
//			analogDto.setInputType(analogInputs[i].getInputType());
//			logger.info("Input type..." + analogInputs[i].getInputType());
//
//			if (analogInputs[i].getInputType() != null
//					&& (analogInputs[i].getInputType().equalsIgnoreCase(
//							M9kConstants.CURRENT_AC_EXTERNAL) || analogInputs[i]
//							.getInputType().equalsIgnoreCase(
//									M9kConstants.CURRENT_DC_EXTERNAL))) {
//				analogDto.setDisableExtSunt(false);
//			} else {
//				analogDto.setDisableExtSunt(true);
//			}
//			logger.info("Is Ext shunt Disabled for channel " + i + "? "
//					+ analogDto.getDisableExtSunt() + " getAccessMode().."
//					+ getAccessMode());
//			if (getAccessMode() == null || getAccessMode().isEmpty()) {
//				logger.info("Entered If loop ");
//				analogDto.setRange("5");
//				analogDto.setPrimaryRatio("1");
//				analogDto.setSecondaryRatio("100");
//			} else {
//				logger.info("Entered Else loop ");
////				analogDto.setScale("" + analogInputs[i].getScale());
//				analogDto.setRange("" + analogInputs[i].getRange());
////				analogDto.setOffset("" + analogInputs[i].getOffset());
//				analogDto.setPrimaryRatio(""
//						+ analogInputs[i].getTransformerPrimary());
//				analogDto.setSecondaryRatio(""
//						+ analogInputs[i].getTransformerSecondary());
//				analogDto.setExtShunt(""+analogInputs[i].getExternalShunt());
//			}
//			getLstAnalogChannels().add(analogDto);
//		}
//		session.put("mapCircuitNames", mapCircuitNames);
//		logger.info("lstAnalogChannels stored in session..."
//				+ lstAnalogChannels);
////		for (Iterator<AnalogChannelDTO> iterator = lstAnalogChannels.iterator(); iterator
////				.hasNext();) {
////			AnalogChannelDTO type = iterator.next();
////			logger.info("Channel in the list.... " + type.getChannel());
////			logger.info("Channel Name in the list.... " + type.getName());
////			logger
////					.info("Circuit Name in the list.... "
////							+ type.getCircuitName());
////			logger.info("Status of External Shunt in the list.... "
////					+ type.getDisableExtSunt());
////		}
//		session.remove("lstAnalogChannels");
//		session.put("lstAnalogChannels", lstAnalogChannels);
//	}

	private void populateAnalogsIntoXmlConfig(DFR dfrObj) {
		AnalogInput[] analogInputs = new AnalogInput[dfrObj.getDataPool()
				.getChannels().getAnalogs().sizeOfAnalogInputArray()];

		currentDfrDto = (DfrDTO) session.get("SelectedDfrToConfigure");
		if (getLstAnalogChannels() != null && getLstAnalogChannels().size() > 0) {
			logger.debug("populateBackToXmlConfig: DFR name: " + dfrObj.getSystem().getDfrId());
			for (int i = 0; i < getLstAnalogChannels().size(); i++) {
				analogInputs[i] = AnalogInputDocument.Factory.newInstance()
						.addNewAnalogInput();
				analogInputs[i].setChannel((Integer
						.parseInt(getLstAnalogChannels().get(i).getChannel())) - currentDfrDto.getAnalogChannelStart()+1);
				analogInputs[i]
						.setName(getLstAnalogChannels().get(i).getName());
				logger
						.debug("In populateBackToXmlConfig() getLstAnalogChannels().get(i).getName() "
								+ getLstAnalogChannels().get(i).getName());
				analogInputs[i].setCircuitName(getLstAnalogChannels().get(i)
						.getCircuitName());
				logger.debug("set Circuit name... "
						+ analogInputs[i].getCircuitName());
				analogInputs[i].setPhase(getLstAnalogChannels().get(i)
						.getPhase());
				logger.debug("set Phase... " + analogInputs[i].getPhase());
				analogInputs[i].setInputType(getLstAnalogChannels().get(i)
						.getInputType());
				if (getLstAnalogChannels().get(i).getInputType()
						.equalsIgnoreCase(M9kConstants.CURRENT_AC_EXTERNAL)
						|| getLstAnalogChannels().get(i).getInputType()
								.equalsIgnoreCase(
										M9kConstants.CURRENT_DC_EXTERNAL)
								// START: 29-Jun-2020 - Hall Effect offset correction implementation
								|| getLstAnalogChannels().get(i).getInputType()
								.equalsIgnoreCase(
										M9kConstants.CURRENT_AC_HALL_EFFECT_EXTERNAL_SHUNT)
								// END: 29-Jun-2020
								) {
					getLstAnalogChannels().get(i).setDisableExtSunt(false);
					analogInputs[i].setExternalShunt(Double.parseDouble(getLstAnalogChannels().get(i).getExtShunt()));
				} else {
					getLstAnalogChannels().get(i).setDisableExtSunt(true);
				}
				
				logger.debug("set Input Type... "
						+ analogInputs[i].getInputType());
				analogInputs[i].setTransformerPrimary(Integer
						.parseInt(getLstAnalogChannels().get(i)
								.getPrimaryRatio()));
				logger.debug("set PrimaryRatio... "
						+ analogInputs[i].getTransformerPrimary());
				analogInputs[i].setTransformerSecondary(Integer
						.parseInt(getLstAnalogChannels().get(i)
								.getSecondaryRatio()));
				logger.debug("set Secondary Ratio... "
						+ analogInputs[i].getTransformerSecondary());
				// START: 16-Oct-2019 Full Scale/Range is changed to double
//				analogInputs[i].setRange(Integer
//						.parseInt(getLstAnalogChannels().get(i).getRange()));
				analogInputs[i].setRange(Double
						.parseDouble(getLstAnalogChannels().get(i).getRange()));
				logger.debug("set Range... " + analogInputs[i].getRange());
				// END: 16-Oct-2019
//				analogInputs[i].setPs(stationDetails.getPs());
				if (getLstAnalogChannels().get(i).getStatus())
				{
					analogInputs[i].setExport(M9kConstants.ENABLE_ONE);
				}
				else
				{
					analogInputs[i].setExport(M9kConstants.DISABLE_ZERO);
				}
//				logger.info("set PS... " + stationDetails.getPs());
//				logger.info("in the loop...i " + i);
			}
			dfrObj.getDataPool().getChannels().getAnalogs()
					.setAnalogInputArray(analogInputs);
			logger.debug("Returning from populateBackToXmlConfig");
		}

	}

	private void populateLineGroups(DFR dfrObj) {
		logger.debug("Entered populateLineGroups method ");
		LineGroups xmlLineGroups = dfrObj.getDataPool().getLineGroups();
		if (xmlLineGroups == null) {
			xmlLineGroups = dfrObj.getDataPool().addNewLineGroups();
			xmlLineGroups.setName("LineGroups");
		}
		clearLineGroupDetails(xmlLineGroups);
		LineGroup xmlLineGroup;
		LineGroupsAlgorithm lineGroups;
		for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator
				.hasNext();) {
			lineGroups = iterator.next();
			xmlLineGroup = xmlLineGroups.addNewLineGroup();
			xmlLineGroup.setName(lineGroups.getName());
			if (lineGroups.getLineGroupName() != null)
			{
				xmlLineGroup.setLineGroupName(lineGroups.getLineGroupName());
			}
			else
			{
				xmlLineGroup.setLineGroupName(lineGroups.getName());
			}
			for (Iterator<String> iteratorInputTags = lineGroups
					.getLstInputTagValues().iterator(); iteratorInputTags
					.hasNext();) {
				String inputTagValue = iteratorInputTags.next();
				xmlLineGroup.addInput(inputTagValue);
			}

		}
	}

	private void clearLineGroupDetails(LineGroups lineGroups) {
		while (lineGroups.sizeOfLineGroupArray() > 0) {
			lineGroups.removeLineGroup(0);
		}
	}

	public String configure() {
		String returnValue = SUCCESS;
		userDto = (UsersDTO) session.get("userDetails");
		session.remove("lstTriggerChannels");
//		logger.debug("Lst Trigger Channel after remove..."
//				+ session.get("lstTriggerChannels"));
//		logger.debug("stationDetails.getSystemDigitalChannelsCount() "+stationDetails.getSystemDigitalChannelsCount());
//		session.remove("triggerOutHold");
		session.remove("lstTriggerSourceDto");

//		logger.debug("Station details from session "+stationDetails);
		if (stationDetails.getSystemAnalogChannelsCount() > 0) {
			setBooAnalog(true);
		}
		else
		{
			setBooAnalog(false);
		}
		if (stationDetails.getSystemDigitalChannelsCount() > 0)
		{
			setBooDigital(true);
		}
		else
		{
			setBooDigital(false);
		}
		session.put("booAnalog", isBooAnalog());
		session.put("booDigital", isBooDigital());
		if (session.get("lineGroupExists") != null)
		{
			lineGroupExists = (Boolean) session.get("lineGroupExists");
		}
		logger.debug("Station details alarms mapping "+getStationDetails().getLstAlarmRelaysMapping());
		logger.info("User "+userDto.getUserName()+" is editing the configuration for station "+stationDetails.getStationDisplayName());
		logger.debug("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is in the Station Details screen");
		return returnValue;
	}

	public String configureChannels() {
		String returnValue = SUCCESS;
		logger.debug("Entered configureChannels method");
//		session.remove("lstTriggerChannels");
//		logger.info("Lst Trigger Channel after remove..."
//				+ session.get("lstTriggerChannels"));
//		session.remove("triggerOutHold");
//		session.remove("lstTriggerSourceDto");

//		session.put("selectedDfr", getSelectedDfr());
//		StationDTO stationDetails = (StationDTO) session.get("stationDetails");
//		logger.debug("Station details from session "+stationDetails);
//		currentSubstation = (SubStation) session.get("selectedSubstation");
//		int index;
//		for (index = 0; index < lstDfrDTO.size(); index++) {
//			currentDfrDto = lstDfrDTO.get(index);
//			logger.debug("DFR Name: " + currentDfrDto.getDfrName());
//			if (currentDfrDto != null
//					&& currentDfrDto.getDfrName().equalsIgnoreCase(getSelectedDfr())) {
//				break;
//			}
//		}
//		session.put("SelectedDfrToConfigure", currentDfrDto);
//		if (currentDfrDto.getChnlConfigDropDownList().getDigitalCnt() > 0)
//		{
//			session.put("isDigital", true);
//		}
//		if (currentDfrDto.getChnlConfigDropDownList().getAnalogCnt() > 0)
//		{
//			session.put("isAnalog", true);
//		}

//		xmlConfigDfr = currentSubstation.getDFRs().getDFRArray(index);
//		logger.info("Configure method! selected dfr obj from session.."
//				+ xmlConfigDfr);
//		session.put("SelectedDfrObj", xmlConfigDfr);
//		if (xmlConfigDfr.getDataPool().getChannels().getAnalogs() != null && xmlConfigDfr.getDataPool().getChannels().getAnalogs()
//				.getAnalogInputArray() != null
//				&& xmlConfigDfr.getDataPool().getChannels().getAnalogs()
//						.getAnalogInputArray().length > 0) {
//			populateAnalogDTO();
//			returnValue = "Analogs";
//
//		} else if (xmlConfigDfr.getDataPool().getChannels().getEvents()
//				.getEventInputArray() != null
//				&& xmlConfigDfr.getDataPool().getChannels().getEvents()
//						.getEventInputArray().length > 0) {
//			returnValue = "Events";
//		}

		if (stationDetails.getSystemAnalogChannelsCount() > 0) {
//			populateAnalogDTO();
			setBooAnalog(true);
			returnValue = "Analogs";

		} else if (stationDetails.getSystemDigitalChannelsCount() > 0) {
			if (stationDetails.isDfrAddedOrRemoved())
			{
				stationDetails.setDfrAddedOrRemoved(false);
			}

			returnValue = "Events";
		}

		if (getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit")) {
			if (stationDetails.getSystemDigitalChannelsCount() > 0)
			{
				setBooDigital(true);
			}
			else
			{
				setBooDigital(false);
			}
			session.put("booAnalog", isBooAnalog());
			session.put("booDigital", isBooDigital());
		}
	logger.debug("Is digital? "+booDigital);

//		if (currentDfrDto != null) {
//			int analogLastCount = currentDfrDto.getChnlConfigDropDownList()
//					.getAnalogCnt();
//			if (analogLastCount > 0) {
//				analogLastCount--;
//				analogChannelCounter = new Counter();
//				analogChannelCounter.setFirst(0);
//				analogChannelCounter.setLast(analogLastCount);
//
//				triggerChannelCounter = new Counter();
//				triggerChannelCounter.setFirst(0);
//				triggerChannelCounter.setLast(analogLastCount);
//
//				logger.info("dfrDto.getAnalogChnlCnt() " + analogLastCount);
//			}
//
//			int eventLastCount = currentDfrDto.getChnlConfigDropDownList()
//					.getDigitalCnt();
//			if (eventLastCount > 0) {
//				eventLastCount--;
//				eventChannelCounter = new Counter();
//				eventChannelCounter.setFirst(0);
//				eventChannelCounter.setLast(eventLastCount);
//				logger.info("dfrDto.getDigitalChnlCnt() "
//						+ currentDfrDto.getChnlConfigDropDownList().getDigitalCnt());
//			}
//		}
//		logger.info("Selected XML Config DFR... " + xmlConfigDfr.getSystem().getDfrId()
	logger.debug(" Return from configureChannels method with return value" + returnValue);
		return returnValue;
	}
	
	@SuppressWarnings("unchecked")
	public String configureLineGroups() {
		String returnValue = "LineGroups";
		logger.debug("Did it enter here in Save method configureLineGroups()? ");
		logger.debug("AnalogInput " + getLstAnalogChannels());
		if (getLstAnalogChannels() == null) {
			lstAnalogChannels = (List<AnalogChannelDTO>) session
					.get("lstAnalogChannels");
		}
		logger.debug("Analog from session..." + lstAnalogChannels);
		xmlConfigDfr = (DFR) session.get("SelectedDfrObj");
		stationDetails = (StationDTO) session.get("stationDetails");
		
		populateAnalogsIntoXmlConfig(xmlConfigDfr);
		if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit")) {
			lstLineGroups = editLineGroups(xmlConfigDfr);
			if (lstLineGroups !=null && !lstLineGroups.isEmpty())
			{
				checkAndUpdateLineGroups();
			}
			else
			{
				lstLineGroups = createLineGroups();
			}
//			if ((lstLineGroups == null || lstLineGroups.isEmpty())
//					&& getLstAnalogChannels() != null) {
//				lstLineGroups = createLineGroups();
//			}
		} else {
			lstLineGroups = createLineGroups();
		}
		if ((lstLineGroups == null || lstLineGroups.isEmpty())
				&& getLstAnalogChannels() != null) {
			session.put("lstAnalogChannels", getLstAnalogChannels());
		} else {
//			if (getAccessMode() == null
//					|| !getAccessMode().equalsIgnoreCase("Edit")) {
				populateLineGroups(xmlConfigDfr);
//			}
			session.put("lstAnalogChannels", getLstAnalogChannels());
			session.put("lstLineGroups", lstLineGroups);
			session.put("selectedLineGroupIndex", 0);
			selectedLineGroupIndex = 0;
//			previousSelectedLineGroupIndex = 0;
//			session.put("previousSelectedLineGroupIndex", 0);
//			session.put("lstAvailableAnalogChannels",
//					getLstAvailableAnalogChannels());
		}

		logger.debug("Returning value..." + returnValue);
		return returnValue;

	}
	
	private void checkAndUpdateLineGroups()
	{
		List<LineGroupsAlgorithm> lstNewLineGroups = new ArrayList<LineGroupsAlgorithm>();
		lstNewLineGroups = createLineGroups();
		
		for (Iterator<LineGroupsAlgorithm> iterator = lstNewLineGroups.iterator(); iterator
				.hasNext();) {
			LineGroupsAlgorithm lineGroupsAlgorithm = iterator
					.next();
			if (!lstLineGroups.contains(lineGroupsAlgorithm))
			{
				lstLineGroups.add(lineGroupsAlgorithm);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public String updateAndGetCircuitName() {
		if (accessMode == null || !accessMode.equalsIgnoreCase("Edit")) {
			session.remove("lstLineGroups");
		}
		try {
			mapCircuitNames = (Map<String, String>) session
					.get("mapCircuitNames");
			if (mapCircuitNames == null) {
				mapCircuitNames = new HashMap<String, String>();
				session.put("mapCircuitNames", mapCircuitNames);
			}
			logger
					.info("Entered constructor of updateAndGetCircuitName getLstAnalogChannels().."
							+ getLstAnalogChannels()
							+ "  mapCircuitNames"
							+ mapCircuitNames);
			AnalogChannelDTO analogChannelDto;
			logger.debug(" mapCircuitNames.values()... "
					+ mapCircuitNames.values());
			lstCircuitNames = Arrays.asList(mapCircuitNames.values().toArray(
					new String[0]));
			json = new HashMap<String, String>();
			logger.debug("Entered updateAndGetCircuitName lstEventDescriptions..."
					+ lstCircuitNames);
			if (selectedIndex != null) {
				reqdIndex = Integer.parseInt(selectedIndex);
				analogChannelDto = getLstAnalogChannels().get(reqdIndex);
				logger.debug("Length of the list... "
						+ getLstCircuitNames().size());
				for (Iterator<String> iterator = getLstCircuitNames()
						.iterator(); iterator.hasNext();) {
					String strCircuitName = iterator.next();
					logger.debug("strCircuiteName..." + strCircuitName);
					logger.debug("analogChannelDto.getCircuitNames()..."
							+ analogChannelDto.getCircuitName());
					logger.debug("analogChannelDto.getCircuitNamesKey()..."
							+ analogChannelDto.getCircuitNameKey());
					if (strCircuitName.toLowerCase().startsWith(
							analogChannelDto.getCircuitName().toLowerCase())) {
						logger.debug("populating JSON...");
						json.put(strCircuitName, strCircuitName);
					} else if (analogChannelDto.getCircuitName() != null
							&& !analogChannelDto.getCircuitName().isEmpty()) {
						mapCircuitNames.put(selectedIndex, analogChannelDto
								.getCircuitName());
					}
				}
				if (getLstCircuitNames() != null
						&& getLstCircuitNames().isEmpty()
						&& analogChannelDto.getCircuitName() != null
						&& !analogChannelDto.getCircuitName().isEmpty()) {
					mapCircuitNames.put(selectedIndex, analogChannelDto
							.getCircuitName());
				}
			}
			session.put("mapCircuitNames", mapCircuitNames);
			logger.debug("JSON array type??? " + json + " lst of circuitNames"
					+ getLstCircuitNames() + " mapCircuitNames.."
					+ mapCircuitNames);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return SUCCESS;
	}

//	public String editConfiguration() {
//		String returnValue = SUCCESS;
//		accessMode = "Edit";
//		session.put("accessMode", accessMode);
//		String previouslySelectedDfr = (String) session.get("selectedDfr");
//		if (previouslySelectedDfr != null
//				&& !previouslySelectedDfr.equals(getSelectedDfr())) {
//			session.remove("lstTriggerChannels");
//			logger.info("Lst Trigger Channel after remove..."
//					+ session.get("lstTriggerChannels"));
//			session.remove("triggerOutHold");
//			session.remove("lstTriggerSourceDto");
//		}
//		session.put("selectedDfr", getSelectedDfr());
//		
//
//		currentSubstation = (SubStation) session.get("selectedSubstation");
//		int index;
//		for (index = 0; index < lstDfrDTO.size(); index++) {
//			currentDfrDto = lstDfrDTO.get(index);
//			logger.debug("DFR Name: " + currentDfrDto.getDfrName());
//			if (currentDfrDto != null
//					&& currentDfrDto.getDfrName().equalsIgnoreCase(getSelectedDfr())) {
//				break;
//			}
//		}
//		session.put("SelectedDfrToConfigure", currentDfrDto);
//		logger.info("Is digital? "+currentDfrDto.getChnlConfigDropDownList().getDigitalCnt());
////		if (currentDfrDto.getChnlConfigDropDownList().getDigitalCnt() > 0)
////		{
////			session.put("isDigital", true);
////		}
////		if (currentDfrDto.getChnlConfigDropDownList().getAnalogCnt() > 0)
////		{
////			session.put("isAnalog", true);
////		}
//		xmlConfigDfr = currentSubstation.getDFRs().getDFRArray(index);
//		logger.info("editConfiguration method! selected dfr obj from session.."
//				+ xmlConfigDfr);
//		session.put("SelectedDfrObj", xmlConfigDfr);
//		if (xmlConfigDfr.getDataPool().getChannels().getAnalogs() != null &&
//				xmlConfigDfr.getDataPool().getChannels().getAnalogs()
//				.getAnalogInputArray() != null
//				&& xmlConfigDfr.getDataPool().getChannels().getAnalogs()
//						.getAnalogInputArray().length > 0) {
//			populateAnalogDTO();
//			returnValue = "Analogs";
//
//		} else if (xmlConfigDfr.getDataPool().getChannels().getEvents()
//				.getEventInputArray() != null
//				&& xmlConfigDfr.getDataPool().getChannels().getEvents()
//						.getEventInputArray().length > 0) {
//			returnValue = "Events";
//		}
//
//		logger.info("Selected XML Config DFR... " + xmlConfigDfr.getSystem().getDfrId()
//				+ " Return value..." + returnValue);
//
//		
//		return returnValue;
//	}

	public String sendConfiguration()
	{
		String returnValue = SUCCESS;
//		StringWriter configXml = new StringWriter();
//		DfrDTO dfrDto = null;
//		SubStation currentSubstation = (SubStation) session.get("selectedSubstation");
//		XmlOptions xmlOptions = new XmlOptions();
//		xmlOptions.setSaveOuter();
//		xmlOptions.setSavePrettyPrint();
//		xmlOptions.setUseDefaultNamespace();
//		xmlOptions.setSaveNoXmlDecl();
//		Map<String, String> prefixes = new HashMap<String, String>();
//		prefixes.put("", "http://www.usi.com");
//		xmlOptions.setSaveImplicitNamespaces(prefixes);
		try {
			// Saravanan 11-Mar-2013 - fix for finish and send doesn't save event triggers check box
//			finish();
//			currentSubstation.save(configXml,xmlOptions);
//			logger.debug("ConfigXML to be queued..."+stationDetails.getConfigXml());
//			M9kMessagesUtil.sendMessage("MyQueue", configXml.toString());
			logger.debug("MESSAGE Q: Message queue Name to be sent to "+ stationDetails.getSystemStationName());
			String response = M9kMessagesUtil.sendSynchMessage(""+stationDetails.getSystemStationId(), stationDetails.getConfigXml(),"CONFIG");
			logger.debug("Response from the JMS client "+response);
			//TODO: response cannot be null. To be checked
			if (response != null && response.equalsIgnoreCase("SUCCESSFUL"))
			{
				addActionMessage("Configuration send successful");		
				// 12-Nov-2021 - Send Email for every configuration change
				sendConfigChangeEmail();

				logger.info("User "+userDto.getUserName()+" succesfully sent the saved configuration to station "+stationDetails.getStationDisplayName());
			}
			else
			{
				addActionError(response);
				logger.error("ERROR!!! User "+userDto.getUserName()+" could not send the saved configuration to station "+stationDetails.getStationDisplayName()+" due to "+response);
			}
		} 
//		catch (M9000Exception e) {
//			addActionError("Failed to save the config xml");
//			logger.error(e);
//		}
		catch (Exception e) {
			addActionError("Failed to send the config xml");
			logger.error("Failed to send the configuration.",e);
		}
		return returnValue;
	}
	public String finish() throws M9000Exception{
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		StationDAO mysqlStationDao = m9kDAOFactory.getStationDAO();
		logger.debug("Entered Finish...");
		String returnValue = "Finish";
		SubStation subStation = (SubStation) session.get("selectedSubstation");
		int version = 1;
		DFR[] dfrs = subStation.getDFRs().getDFRArray();
		logger.debug("IS confugSErialNumber set already? "+dfrs[0].getSystem().isSetConfigSerialNumber());
		if (dfrs[0].getSystem().isSetConfigSerialNumber())
		{
			logger.debug("ConfigSEriaNumber already set "+subStation.getDFRs().getDFRArray(0).getSystem().getConfigSerialNumber());
			version = subStation.getDFRs().getDFRArray(0).getSystem().getConfigSerialNumber()+1;
		}
		for (int i = 0; i < dfrs.length; i++) {
			dfrs[i].getSystem().setConfigSerialNumber(version);
		}
		
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSaveOuter();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setUseDefaultNamespace();
		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("", "http://www.usi.com");
		xmlOptions.setSaveImplicitNamespaces(prefixes);
		stationDetails = (StationDTO) session.get("stationDetails");
		try {
//			logger.debug("Station Details.."+stationDetails);
//			logger.debug("XML System Details.."+subStation.getDFRs().getDFRArray(0).getSystem());
//			logger.debug("Station Details sample Rate.."+subStation.getDFRs().getDFRArray(0).getSystem().getSampleRate());
				String xmlText = subStation.xmlText(xmlOptions);
//				logger.info("XML content after finish "+xmlText);
//				Diff diff = DiffBuilder.compare(Input.fromString(stationDetails.getConfigXml())).withTest(Input.fromString(xmlText))
//					     .checkForSimilar()
//					     .ignoreWhitespace()
//					     .build();
//
//				logger.debug(diff.toString()+" diff has differences? "+diff.hasDifferences());
//				if (diff.hasDifferences())
//				{
//					logger.info("User "+userDto.getUserName()+" modified the configuration of the station "+stationDetails.getStationDisplayName()+" "+M9kConstants.NEWLINE+diff.toString());
//				}

				stationDetails.setConfigXml(xmlText);
				if (stationDetails.getConfigStatus() == null)
				{
					stationDetails.setConfigStatus("INCOMPLETE");
				}
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
		} catch (Exception e) {
				logger.error("Error in saving station details in the database. ",e);
				throw new M9000Exception("Saving failed!",e);
			}
		return returnValue;
	}

	private void sendConfigChangeEmail()
	{
		try
		{
			EmailSettingsDTO emailSettingsDTO;
				emailSettingsDTO = M9kReportUtil.getEmailSettingsDTO();
				logger.debug("Email settings "+emailSettingsDTO);
				EmailReportsSettingsDTO emailReportsSettingsDTO = M9kReportUtil.getEmailReportsSettingsDTO();
				logger.debug("Email report settings "+emailReportsSettingsDTO);
//					if (M9kUtils.isEmailNotificationEnabled() && getEmailNotificationForFaults().equalsIgnoreCase(M9kConstants.ENABLE))
				if (emailSettingsDTO.isEnableEmail() && emailReportsSettingsDTO.isEnableConfigChangeEmail())
				{
					String currentTime = M9kUtils.getCurrentDateTime();
					logger.debug("About to send email...");
					String emailSubject = currentTime+" - "+ stationDetails.getStationDisplayName()+" - Configuration applied to all Chassis by user "+userDto.getUserName();
					String emailText = "User "+userDto.getUserName()+" applied the configuration at "+currentTime;
					SendMailUSI.sendEmail(emailSubject, emailText.toString());
				}
		
		} catch (Exception e)
		{
			logger.error("Email couldn't be sent for configuration change", e);
		}

	}
	@SkipValidation
	public String back() {
		String returnValue = "Back";
		logger.debug("Returning " + lstDfrDTO);
		logger.info("user "+getUserDto().getUserName() +" is traversing back to station details page");
		return returnValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSession(Map<String, Object> session) {
		// TODO Auto-generated method stub
		this.session = session;
		stationDetails = (StationDTO) session.get("stationDetails");
//		logger.debug("In SetSession() Station details from session "+stationDetails);
		lstDfrDTO = (List<DfrDTO>) session.get("DFRsList");
		accessMode = (String) session.get("accessMode");
		userDto = (UsersDTO) session.get("userDetails");
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
	 * @return the analogChannelsCount
	 */
	public String getAnalogChannelsCount() {
		return analogChannelsCount;
	}

	/**
	 * @param analogChannelsCount
	 *            the analogChannelsCount to set
	 */
	public void setAnalogChannelsCount(String analogChannelsCount) {
		this.analogChannelsCount = analogChannelsCount;
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
	 * @return the lstAnalogChannels
	 */
//	@VisitorFieldValidator(message = "! ")
	public List<AnalogChannelDTO> getLstAnalogChannels() {
		return lstAnalogChannels;
	}

	/**
	 * @param lstAnalogChannels
	 *            the lstAnalogChannels to set
	 */
	public void setLstAnalogChannels(List<AnalogChannelDTO> analogs) {
		this.lstAnalogChannels = analogs;
	}

	/**
	 * @return the lstLineGroups
	 */
	public List<LineGroupsAlgorithm> getLstLineGroups() {
		return lstLineGroups;
	}

	/**
	 * @param lstLineGroups
	 *            the lstLineGroups to set
	 */
	public void setLstLineGroups(List<LineGroupsAlgorithm> lstLineGroups) {
		this.lstLineGroups = lstLineGroups;
	}

	/**
	 * @param lstAvailableAnalogChannels
	 *            the lstAvailableAnalogChannels to set
	 */
	public void setLstAvailableAnalogChannels(
			List<AnalogChannelDTO> lstAvailableAnalogChannels) {
		this.lstAvailableAnalogChannels = lstAvailableAnalogChannels;
	}

	public List<AnalogChannelDTO> getLstAvailableAnalogChannels() {
		lstAvailableAnalogChannels = new ArrayList<AnalogChannelDTO>();
		LineGroupsAlgorithm lineGroupsAlgorithm = getLstLineGroups().get(0);
		for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels()
				.iterator(); iterator.hasNext();) {
			AnalogChannelDTO analogChannelDTO = iterator.next();
			if (!lineGroupsAlgorithm.getInputChannels().contains(
					analogChannelDTO)) {
				lstAvailableAnalogChannels.add(analogChannelDTO);
			}
		}
		return lstAvailableAnalogChannels;
	}

	/**
	 * @return the selectedLineGroupIndex
	 */
	public int getSelectedLineGroupIndex() {
		return selectedLineGroupIndex;
	}

	/**
	 * @param selectedLineGroupIndex
	 *            the selectedLineGroupIndex to set
	 */
	public void setSelectedLineGroupIndex(int selectedLineGroupIndex) {
		this.selectedLineGroupIndex = selectedLineGroupIndex;
	}

//	/**
//	 * @return the previousSelectedLineGroupIndex
//	 */
//	public int getPreviousSelectedLineGroupIndex() {
//		return previousSelectedLineGroupIndex;
//	}
//
//	/**
//	 * @param previousSelectedLineGroupIndex
//	 *            the previousSelectedLineGroupIndex to set
//	 */
//	public void setPreviousSelectedLineGroupIndex(
//			int previousSelectedLineGroupIndex) {
//		this.previousSelectedLineGroupIndex = previousSelectedLineGroupIndex;
//	}

	/**
	 * @return the lstEventDescriptions
	 */
	public List<String> getLstCircuitNames() {
		return lstCircuitNames;
	}

	/**
	 * @param lstEventDescriptions
	 *            the lstEventDescriptions to set
	 */
	public void setLstCircuitNames(List<String> lstCircuitNames) {
		this.lstCircuitNames = lstCircuitNames;
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
	 * @return the lstAvailableStations
	 */
	public List<StationDTO> getLstAvailableStations() {
		return lstAvailableStations;
	}

	/**
	 * @param lstAvailableStations
	 *            the lstAvailableStations to set
	 */
	public void setLstAvailableStations(
			List<StationDTO> lstAvailableStations) {
		this.lstAvailableStations = lstAvailableStations;
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
	public List<EventChannelDTO> getLstEventChannels() {
		return lstEventChannels;
	}

	/**
	 * @param lstEventChannels the lstEventChannels to set
	 */
	public void setLstEventChannels(List<EventChannelDTO> lstEventChannels) {
		this.lstEventChannels = lstEventChannels;
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.debug("Entered destroy method...");
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


	public boolean isBooDigital() {
		return booDigital;
	}

	public void setBooDigital(boolean booDigital) {
		this.booDigital = booDigital;
	}

	public boolean isBooAnalog() {
		return booAnalog;
	}

	public void setBooAnalog(boolean booAnalog) {
		this.booAnalog = booAnalog;
	}

	public boolean isLineGroupExists() {
		return lineGroupExists;
	}

	public void setLineGroupExists(boolean lineGroupExists) {
		this.lineGroupExists = lineGroupExists;
	}

	public UsersDTO getUserDto() {
		return userDto;
	}

	public void setUserDto(UsersDTO userDto) {
		this.userDto = userDto;
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

}
