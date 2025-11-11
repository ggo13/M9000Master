package com.usi.m9000.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.AlgorithmsDocument.Algorithms;
import com.usi.AnalogInputDocument.AnalogInput;
import com.usi.AnalogsDocument.Analogs;
import com.usi.DFRDocument.DFR;
import com.usi.EventInputDocument.EventInput;
import com.usi.ExportDocument2.Export;
import com.usi.ExportsDocument.Exports;
import com.usi.LineGroupDocument.LineGroup;
import com.usi.RmsDocument.Rms;
import com.usi.SubStationDocument.SubStation;
import com.usi.TriggerDocument.Trigger;
import com.usi.TriggersDocument.Triggers;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.EventChannelDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.TriggerChannelDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kXMLConstants;
import com.usi.m9000.xml.util.M9kXMLUtils;

//@ParentPackage(value = "M9000Master")
//@InterceptorRefs({
//    @InterceptorRef("jsonValidationWorkflowStack"),
//    @InterceptorRef("defaultStack")
//})
//@Validations(requiredStrings = {
//    @RequiredStringValidator(fieldName = "circuitName", type = ValidatorType.FIELD, message = "Channel Description is required") 
//})
public class ConfigureVirtualChannelsAction extends ActionSupport implements
		SessionAware, ServletContextListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String selectedChannelIndex;
	private String selectedDfr;
	private String circuitName;
	private AnalogChannelDTO newVirtualChannel = null;
	private AnalogChannelDTO editedVirtualChannel = null;
	private List<AnalogChannelDTO> lstAnalogsForVirtual = new ArrayList<AnalogChannelDTO>();
	List<String> lstSelectedVirtualChannels ;
	private String mode; // New or Edit mode
	private String phase;
	private String inputType;
	List<AnalogChannelDTO> lstRemovedVirtualChannels;
	private String newVirtualChnlIndex;
	private Map<String, Object> session;
	private StationDTO stationDetails;
	private String analogChannelsCount;
	private String eventChannelsCount;
	int minimumDFRCount;
	List<DfrDTO> lstDfrDTO;
	List<DfrDTO> lstAnalogDfrs = new ArrayList<DfrDTO>();
	List<AnalogChannelDTO> lstVirtualChannels;
	List<AnalogChannelDTO> lstAnalogChannels;
	List<AnalogChannelDTO> lstDfrsAnalogChannels;
	List<EventChannelDTO> lstEventChannels;
	List<AnalogChannelDTO> lstAvailableAnalogChannels;
	List<LineGroupsAlgorithm> lstLineGroups;
	int selectedLineGroupIndex;
	EventInput[] eventInput;
	List<String> lstCircuitNames;
	private Map<String, String> json;
	String selectedIndex;
	int reqdIndex;
	List<StationDTO> lstAvailableStations;
	String accessMode;
	String sourceTab;
	String originTab;
	Map<String, DFR> mapXMLDfrs;
	boolean booAnalog;
	boolean booDigital;
	private boolean lineGroupExists;
	private UsersDTO userDto;
	private Map<String,List<AnalogChannelDTO>> mapDfrSpecificChannels = null;
	private SubStation subStation;
	List<String> lstAffectedLineGroups = null; 
	private String virtualLogic; // Logic defined for each virtual channel like A1 + A2 - A3
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ConfigureVirtualChannelsAction.class);
	// START: 23-Feb-2021 - Delta Transformer implementation
	private double virtualScale = 1.0; 
	// END: 23-Feb-2021
	public ConfigureVirtualChannelsAction() {
//		logger.info("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is in the Virtual Analogs configuration screen");
	}
	/**
	 * 
	 */
	public ConfigureVirtualChannelsAction(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
//		, expressions = {
//				  @ExpressionValidator(expression = "(lstVirtualChannels.size() > 0 && ((lstSelectedVirtualChannels != null && (lstSelectedVirtualChannels.size() < 2))) == true)", message = "Selected channels empty. Minimum 2 analog channels required")

	}

	public String execute()
	{
		String navigateValue = "";
		try
		{
			logger.debug("Inside execute method..."+getLstAnalogChannels());
			logger.debug("sourceTab...."+sourceTab);
			session.put("sourceTab", sourceTab);
			booAnalog = (Boolean)session.get("booAnalog");
			booDigital = (Boolean)session.get("booDigital");
			lineGroupExists = (Boolean)session.get("lineGroupExists");
			lstAffectedLineGroups = (List<String>)session.get("linegroupsWarnings");
//			session.put("sourceTab", sourceTab);
			if (getSourceTab() != null && !getSourceTab().isEmpty())
			{
				if (getLstVirtualChannels() != null)
				{
					populateVirtualIntoXmlConfig();
				}
				if (getLstAffectedLineGroups()!=null && getLstAffectedLineGroups().size() > 0)
				{
					navigateValue="LineGroups";
				}
				else if (getSourceTab().equalsIgnoreCase("Analogs"))
				{
					navigateValue = "Back";
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
					navigateValue="Events";
				}
				else if (getSourceTab().equalsIgnoreCase("FaultLocation"))
				{
					logger.info("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is forwarded via Analogs configuration screen");
					navigateValue="FaultLocation";
				}
				else if (getSourceTab().equalsIgnoreCase("StationDetails"))
				{
					navigateValue="StationDetails";
				}
				// 10-Mar-2021 - Virtual Measurement implementation
				else if (getSourceTab().equalsIgnoreCase("VirtualMeasurements"))
				{
					navigateValue="VirtualMeasurements";
				}
				// 10-Mar-2021 - Virtual Measurement implementation
			}
			if (getSourceTab() == null || getSourceTab().isEmpty() || getSourceTab().equalsIgnoreCase("VirtualChannels") )
			{
				navigateValue = populateVirtualChannelsDTO();
			}
			populateMapDfrSpecificChannels();
		}
		catch (Exception e) {
			logger.error("Error navigating to LineGroups ",e);
		}
		return navigateValue;
	}

	@SuppressWarnings("unchecked")
	@SkipValidation
	public String populateVirtualChannelsDTO() throws M9000Exception {
		List<String> lstDfrNames = new ArrayList<String>(mapXMLDfrs.keySet());
		lstVirtualChannels = (List<AnalogChannelDTO>) session.get("lstVirtualChannels");
		logger.debug("In populateVirtualChannelsDTO(), get list from session "+getLstVirtualChannels());
		populateMapDfrSpecificChannels();
		Collections.sort(lstDfrNames, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				int lhs = Integer.parseInt(o1.substring(3));
				int rhs = Integer.parseInt(o2.substring(3));
				return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
			}
		});
		try
		{
			List<AnalogChannelDTO> lstVirtualInputChannels;
			Iterator<String> mapXmlDfrIterator =lstDfrNames.iterator();
			String dfrKey= null;
			DFR xmlDfr;
			AnalogInput[] analogInputs;
			AnalogChannelDTO analogDto;
			if (getLstVirtualChannels() == null)
			{
				lstVirtualChannels = new ArrayList<AnalogChannelDTO>();
				for (; mapXmlDfrIterator.hasNext();) {
					dfrKey = mapXmlDfrIterator.next();
					xmlDfr = mapXMLDfrs.get(dfrKey);
					if (xmlDfr.getDataPool().getChannels().getAnalogs() == null)
					{
						continue;
					}
					analogInputs = xmlDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();
					for (int i = 0; i < analogInputs.length; i++) {
	//					logger.debug("Is virtual set "+analogInputs[i].isSetVirtual());
						if (!analogInputs[i].isSetVirtual() || analogInputs[i].getVirtual() == 0)
						{
							continue;
						}
						analogDto = new AnalogChannelDTO();
						analogDto.setChassis(dfrKey);
						analogDto.setName(analogInputs[i].getName());
						analogDto.setVirtualChannelNo(Integer.parseInt(analogInputs[i].getName().substring(analogInputs[i].getName().indexOf("VirtualAnalog")+"VirtualAnalog".length())));
						analogDto.setChannel("" + (analogInputs[i].getChannel()+getDfrAnalogOffset(dfrKey)));
						if (analogInputs[i].getExport() == M9kConstants.ENABLE_ONE)
						{
							analogDto.setExportStatus(true);
						}
						else
						{
							analogDto.setExportStatus(false);
						}
						analogDto.setCircuitName(analogInputs[i].getCircuitName());
						analogDto.setPhase(analogInputs[i].getPhase());
						analogDto.setInputType(analogInputs[i].getInputType());
						lstVirtualInputChannels = new ArrayList<AnalogChannelDTO>();
						logger.debug("Virtual channels circuit name "+analogInputs[i].getCircuitName());
						if (analogInputs[i].getInputArray() != null && analogInputs[i].getInputArray().length > 0)
						{
							lstVirtualInputChannels.addAll(getAnalogChannelsForNames(dfrKey, analogInputs[i].getInputArray(), "+"));
						}
						if (analogInputs[i].getInputAddArray() != null && analogInputs[i].getInputAddArray().length > 0)
						{
							logger.debug("Input Add array: "+analogInputs[i].getInputAddArray());
							lstVirtualInputChannels.addAll(getAnalogChannelsForNames(dfrKey, analogInputs[i].getInputAddArray(), "+"));
						}
						if (analogInputs[i].getInputSubArray() != null && analogInputs[i].getInputSubArray().length > 0)
						{
							logger.debug("Input Sub array: "+analogInputs[i].getInputSubArray());
							lstVirtualInputChannels.addAll(getAnalogChannelsForNames(dfrKey, analogInputs[i].getInputSubArray(),"-"));
						}
						// START: 23-Feb-2021 - Delta Transformer implementation
						if (analogInputs[i].isSetVirtualScale() && analogInputs[i].getVirtualScale() != 1.0)
						{
							analogDto.setVirtualScale(analogInputs[i].getVirtualScale());
						}
						// END: 23-Feb-2021 - Delta Transformer implementation
						// START: 06-Feb-2020 -- Remove transducer channels from virtual channels
						if (lstVirtualInputChannels.isEmpty())
						{
							// No input channels for virtuals available
							deleteVirtualChannelFromXML(analogDto);
						}
						else
						{
							analogDto.setLstAnalogsForVirtual(lstVirtualInputChannels);
							getLstVirtualChannels().add(analogDto);
						}
					}
				}
				Collections.sort(getLstVirtualChannels(), new Comparator<AnalogChannelDTO>() {

					@Override
					public int compare(AnalogChannelDTO o1, AnalogChannelDTO o2) {
						int lhs = Integer.parseInt(o1.getName().substring(13)); // 13 is the length of string prefix VirtualAnalog
						int rhs = Integer.parseInt(o2.getName().substring(13));
						return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
					}
				});
				session.remove("lstVirtualChannels");
				session.put("lstVirtualChannels", lstVirtualChannels);
			}
			
		}
		catch (Exception e) {
			logger.error("Exception occured in populateVirtualChannelsDTO method",e);
			throw new M9000Exception("Exception in populateVirtualChannelsDTO method ",e);
		}

		logger.debug("Returning from populateVirtualChannelsDTO method "+getLstVirtualChannels());
		return SUCCESS;
	}

	private List<AnalogChannelDTO> getAnalogChannelsForNames(String dfrName,
			String[] virtualChannelsInputNames, String operator) {
		String channelName;
		AnalogChannelDTO analogDto;
		List<AnalogChannelDTO> lgInput = new ArrayList<AnalogChannelDTO>();
		for (int i = 0; i < virtualChannelsInputNames.length; i++) {
			channelName = virtualChannelsInputNames[i].substring(
					virtualChannelsInputNames[i].indexOf("AnalogInput(") + 12,
					virtualChannelsInputNames[i].lastIndexOf(")"));
			for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels()
					.iterator(); iterator.hasNext();) {
				analogDto = iterator.next();
				// START: 06-Feb-2020 -- Remove transducer channels from virtual channels
				if (analogDto.isTransducer())
				{
					continue;
				}
				// END: 06-Feb-2020
				if (analogDto.getChassis().equalsIgnoreCase(dfrName) && analogDto.getName().equalsIgnoreCase(channelName)) {
					analogDto.setOperator(operator);
					try {
						lgInput.add((AnalogChannelDTO) analogDto.clone());
					} catch (CloneNotSupportedException e) {
						logger.warn("Exception occured while cloning analog object in populateVirtualChannelsDTO method",e);
						lgInput.add(analogDto);
					}
					logger.debug("Added " + analogDto.getName());
					break;
				}
			}
		}
		return lgInput;
	}

	public String showNewVirtualChannelDialog()
	{
		DfrDTO dfrDto;
		if (getLstAnalogDfrs() == null )
		{
			setLstAnalogDfrs(new ArrayList<DfrDTO>(getLstDfrDTO().size()));
		}
		if (getLstAnalogDfrs().isEmpty())
		{
			for (Iterator<DfrDTO> iterator = getLstDfrDTO().iterator(); iterator
					.hasNext();) {
				dfrDto = iterator
						.next();
				if (dfrDto.getAnalogChnlCnt() > 0)
				{
					getLstAnalogDfrs().add(dfrDto);
				}
				
			}
		}
		setPhase("A");
		setInputType("I");
		logger.debug("Lst of analog dfrs "+lstAnalogDfrs.size());

		logger.debug("Entered showNewVirtualChannelDialog "+((mapDfrSpecificChannels != null)?mapDfrSpecificChannels.size():"No dfr specific channels list"));
		logger.debug("List of dfr dtos "+getLstDfrDTO()+" Selected Dfrs "+getSelectedDfr());
//		getDfrsSpecificChannels();
		
		return SUCCESS;
	}
	
	public String showEditVirtualChannelDialog()
	{
		logger.debug("Entered showEditVirtualChannelDialog "+getSelectedChannelIndex());
		int currentSelectedIndex = Integer.parseInt(getSelectedChannelIndex());
		setMode("edit");
		editedVirtualChannel = getLstVirtualChannels().get(currentSelectedIndex);
		setSelectedDfr(editedVirtualChannel.getChassis());
		DfrDTO dfrDto;
		if (getLstAnalogDfrs() == null )
		{
			setLstAnalogDfrs(new ArrayList<DfrDTO>(getLstDfrDTO().size()));
		}
		if (getLstAnalogDfrs().isEmpty())
		{
			for (Iterator<DfrDTO> iterator = getLstDfrDTO().iterator(); iterator
					.hasNext();) {
				dfrDto = iterator
						.next();
				if (dfrDto.getAnalogChnlCnt() > 0)
				{
					getLstAnalogDfrs().add(dfrDto);
				}
				
			}
		}
		setLstAnalogsForVirtual(getEditedVirtualChannel().getLstAnalogsForVirtual());
		setPhase(getEditedVirtualChannel().getPhase());
		if (getEditedVirtualChannel().getInputType().startsWith(M9kConstants.VOLTAGE))
		{
			setInputType("V");
		}
		else
		{
			setInputType("I");
		}
		logger.debug("Lst of analog dfrs "+lstAnalogDfrs.size()+" lstAnalogsForVirtual "+getLstAnalogsForVirtual());

		logger.debug("Entered showEditVirtualChannelDialog "+((mapDfrSpecificChannels != null)?mapDfrSpecificChannels.size():"No dfr specific channels list"));
		logger.debug("List of dfr dtos "+getLstDfrDTO()+" Selected Dfrs "+getSelectedDfr());
		logger.debug("Edited virtual channel "+ editedVirtualChannel+" lst of virtual channels "+editedVirtualChannel.getLstAnalogsForVirtual());
		session.put("editedVirtualChannel",getEditedVirtualChannel());
//		getDfrsSpecificChannels();
		return SUCCESS;
	}


	@SuppressWarnings("unused")
	private String getDfrsSpecificChannelsOld()
	{
		AnalogChannelDTO analogChannelDTO;
		logger.debug("Entered getDfrsSpecificChannels "+getSelectedChannelIndex()+" edited channel "+getEditedVirtualChannel()+" get mode "+getMode()+" get Selected dfr "+getSelectedDfr());
		logger.debug("Phase selected "+getPhase()+" InputType Selected "+getInputType());
		if (getMode() != null && getMode().equalsIgnoreCase("edit"))
		{
			editedVirtualChannel = (AnalogChannelDTO) session.get("editedVirtualChannel");
			if ((editedVirtualChannel.getPhase().equalsIgnoreCase(getPhase())) &&
					((getInputType().equalsIgnoreCase("I") && editedVirtualChannel.getInputType().startsWith(M9kConstants.CURRENT)) ||
							(getInputType().equalsIgnoreCase("V") && editedVirtualChannel.getInputType().startsWith(M9kConstants.VOLTAGE)))){
				setLstAnalogsForVirtual(getEditedVirtualChannel().getLstAnalogsForVirtual());
			}
			else
			{
				setLstAnalogsForVirtual(new ArrayList<AnalogChannelDTO>());
				for (Iterator<AnalogChannelDTO> iterator = getEditedVirtualChannel().getLstAnalogsForVirtual().iterator(); iterator
						.hasNext();) {
					analogChannelDTO = iterator
							.next();
					if (analogChannelDTO.getPhase().equalsIgnoreCase(getPhase()) &&
							((getInputType().equalsIgnoreCase("I") && analogChannelDTO.getInputType().startsWith(M9kConstants.CURRENT)) ||
									(getInputType().equalsIgnoreCase("V") && analogChannelDTO.getInputType().startsWith(M9kConstants.VOLTAGE))))
					{
						getLstAnalogsForVirtual().add(analogChannelDTO);
					}
				}
			}
			
			selectedDfr = editedVirtualChannel.getChassis();
		}
		else if (getSelectedDfr() == null || getSelectedDfr().isEmpty())
		{
			selectedDfr = "DFR1";
		}
		
		
		lstDfrsAnalogChannels = new ArrayList<AnalogChannelDTO>();
		for (Iterator<AnalogChannelDTO> iterator = mapDfrSpecificChannels.get(selectedDfr).iterator(); iterator
				.hasNext();) {
			analogChannelDTO = (AnalogChannelDTO) iterator
					.next();
			logger.debug("analog channel DTO "+analogChannelDTO);
			if (getMode()!=null && getMode().equalsIgnoreCase("edit"))
			{
				logger.debug("Edited virtual channel "+ editedVirtualChannel+" lst of virtual channels "+editedVirtualChannel.getLstAnalogsForVirtual());

				logger.debug("virtual channels list "+editedVirtualChannel.getLstAnalogsForVirtual()+" to be compared to analog channel "+analogChannelDTO);
				logger.debug("\t\tAnalogs in the virual list????"+(!editedVirtualChannel.getLstAnalogsForVirtual().contains(analogChannelDTO)));
				logger.debug("analogChannel phase "+analogChannelDTO.getPhase()+" ");
				if (!editedVirtualChannel.getLstAnalogsForVirtual().contains(analogChannelDTO) &&
						(analogChannelDTO.getPhase().equalsIgnoreCase(getPhase()) &&
								((getInputType().equalsIgnoreCase("I") && analogChannelDTO.getInputType().startsWith(M9kConstants.CURRENT)) ||
										(getInputType().equalsIgnoreCase("V") && analogChannelDTO.getInputType().startsWith(M9kConstants.VOLTAGE)))))
				{
					lstDfrsAnalogChannels.add(analogChannelDTO);
				}
			}
			else
			{
				setLstAnalogsForVirtual(new ArrayList<AnalogChannelDTO>());
				logger.debug("phase "+getPhase()+" to compare to "+analogChannelDTO.getPhase()+"getPhase().equalsIgnoreCase(analogChannelDTO.getPhase()) ??? "+getPhase().equalsIgnoreCase(analogChannelDTO.getPhase()));
				logger.debug("input type "+getInputType()+" analogChannelDTO.getInputType() "+analogChannelDTO.getInputType());
				if (getPhase().equalsIgnoreCase(analogChannelDTO.getPhase()) &&
						((getInputType().equalsIgnoreCase("I") && analogChannelDTO.getInputType().startsWith(M9kConstants.CURRENT)) ||
						(getInputType().equalsIgnoreCase("V") && analogChannelDTO.getInputType().startsWith(M9kConstants.VOLTAGE))))
				{
					lstDfrsAnalogChannels.add(analogChannelDTO);
				}
			}
		}
		logger.debug("lst of anologs for virtuals "+getLstAnalogsForVirtual());
		return SUCCESS;
	}
	
	public String getDfrsSpecificChannels()
	{
		AnalogChannelDTO analogChannelDTO;
		populateMapDfrSpecificChannels();

		logger.debug("Entered getDfrsSpecificChannels "+getSelectedChannelIndex()+" edited channel "+getEditedVirtualChannel()+" get mode "+getMode()+" get Selected dfr "+getSelectedDfr());
		logger.debug("Phase selected "+getPhase()+" InputType Selected "+getInputType());
		logger.debug("getVirtualLogic() "+getVirtualLogic());
		if (getMode() != null && getMode().equalsIgnoreCase("edit"))
		{
			editedVirtualChannel = (AnalogChannelDTO) session.get("editedVirtualChannel");
			// START: 13-Feb-2020 - ALlow different phases and different types of channels to be patr of virtual channels
//			if ((editedVirtualChannel.getPhase().equalsIgnoreCase(getPhase())) &&
//					((getInputType().equalsIgnoreCase("I") && editedVirtualChannel.getInputType().startsWith(M9kConstants.CURRENT)) ||
//							(getInputType().equalsIgnoreCase("V") && editedVirtualChannel.getInputType().startsWith(M9kConstants.VOLTAGE)))){
////				setLstAnalogsForVirtual(getEditedVirtualChannel().getLstAnalogsForVirtual());
//				logger.debug("Inside IF "+getEditedVirtualChannel().getVirtualLogic());
			if (getVirtualLogic() == null)
			{
				setVirtualLogic(getEditedVirtualChannel().getVirtualLogic());
			}
//			else
//			{
////				setLstAnalogsForVirtual(new ArrayList<AnalogChannelDTO>());
//				logger.debug("Inside Else ");
//				setVirtualLogic("");
//			}
			// END: 13-Feb-2020
			selectedDfr = editedVirtualChannel.getChassis();
		}
		else if (getSelectedDfr() == null || getSelectedDfr().isEmpty())
		{
			selectedDfr = "DFR1";
		}
		
		
		lstDfrsAnalogChannels = new ArrayList<AnalogChannelDTO>();
		for (Iterator<AnalogChannelDTO> iterator = mapDfrSpecificChannels.get(selectedDfr).iterator(); iterator
				.hasNext();) {
			analogChannelDTO = (AnalogChannelDTO) iterator
					.next();
			logger.debug("analog channel DTO "+analogChannelDTO);
			if (getPhase().equalsIgnoreCase(analogChannelDTO.getPhase()) &&
					((getInputType().equalsIgnoreCase("I") && analogChannelDTO.getInputType().startsWith(M9kConstants.CURRENT)) ||
					(getInputType().equalsIgnoreCase("V") && analogChannelDTO.getInputType().startsWith(M9kConstants.VOLTAGE))))
			{
				lstDfrsAnalogChannels.add(analogChannelDTO);
			}

		}
//		if (getMode()!=null && getMode().equalsIgnoreCase("edit"))
//		{
//			logger.debug("Edited virtual channel "+ editedVirtualChannel+" lst of virtual channels "+editedVirtualChannel.getLstAnalogsForVirtual());
//
//			logger.debug("virtual channels list "+editedVirtualChannel.getLstAnalogsForVirtual()+" Virtual logic "+editedVirtualChannel.getVirtualLogic());
//			setVirtualLogic(editedVirtualChannel.getVirtualLogic());
//		}
//		else
//		{
////			setLstAnalogsForVirtual(new ArrayList<AnalogChannelDTO>());
//			setVirtualLogic("");
//			logger.debug("Clearing virtual logic");
//		}

		logger.debug("lst of anologs for virtuals "+getLstDfrsAnalogChannels());
		return SUCCESS;
	}

	public String fetchLstOfDFrs()
	{
		logger.debug("Fetch list of DFRs "+getLstDfrDTO());
		if (getLstDfrDTO() == null)
		{
			lstDfrDTO = (List<DfrDTO>) session.get("DFRsList");
		}
		DfrDTO dfrDto;
		if (getLstAnalogDfrs() == null )
		{
			setLstAnalogDfrs(new ArrayList<DfrDTO>(getLstDfrDTO().size()));
		}
		else if (getLstAnalogDfrs().isEmpty())
		for (Iterator<DfrDTO> iterator = getLstDfrDTO().iterator(); iterator
				.hasNext();) {
			dfrDto = iterator
					.next();
			if (dfrDto.getAnalogChnlCnt() > 0)
			{
				getLstAnalogDfrs().add(dfrDto);
			}
			
		}

		return SUCCESS;
	}
	
	
	// moved to config analogs screen
	@SuppressWarnings("unused")
	private void populateMapDfrSpecificChannelsOld()
	{
		AnalogChannelDTO analogChannelDTO = null;
		mapDfrSpecificChannels = new HashMap<String, List<AnalogChannelDTO>>();
		for (Iterator<AnalogChannelDTO> iterator = lstAnalogChannels.iterator(); iterator
				.hasNext();) {
			analogChannelDTO = (AnalogChannelDTO) iterator
					.next();
			if (mapDfrSpecificChannels.get(analogChannelDTO.getChassis()) == null)
			{
				mapDfrSpecificChannels.put(analogChannelDTO.getChassis(), new ArrayList<AnalogChannelDTO>());
			}
			// START: 27-Jan-2020 - Transducer Implementation - Cannot be part of virtual channels
			if (!analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE) && !analogChannelDTO.isTransducer())
			// END: 27-Jan-2020
			{
				mapDfrSpecificChannels.get(analogChannelDTO.getChassis()).add(analogChannelDTO);
			}
		}
	}
	public String navigateToLineGroups()
	{
		String returnValue = "LineGroups";
		if (getLstVirtualChannels() != null && !getLstVirtualChannels().isEmpty())
		{
			if (accessMode == null || !accessMode.equalsIgnoreCase("Edit")) 
			{
				returnValue = "saveVirtualsIntoDB";
			}
			populateVirtualIntoXmlConfig();
		}
		logger.debug("Returning from next() "+returnValue);
		logger.info("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is navigating from Virtual Channels configuration to Line Groups configuration screen");
		return returnValue;
	}
	
	
	private void populateVirtualIntoXmlConfig() {
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
		AnalogChannelDTO analogDto;
		String chassis = null;
		AnalogInput analogInput;
			for (int i = 0; i < getLstVirtualChannels().size(); i++) {
				analogDto = getLstVirtualChannels().get(i);
				logger.debug("Analog channel "+(analogDto.getChannel()+"-"+analogDto.getName())+" circuit name "+analogDto.getCircuitName());
				if (chassis == null)
				{
					chassis = analogDto.getChassis();
				}
				else
				{
					if(!chassis.equalsIgnoreCase(analogDto.getChassis()))
					{
						chassis = analogDto.getChassis();
					}
				}
				analogInput = getAnalogInput(analogDto);
				logger.debug(" chassis "+chassis+" analogInput from the method "+analogInput);
				analogInput.setCircuitName(analogDto
						.getCircuitName());
				analogInput.setPhase(analogDto
						.getPhase());
				analogInput.setInputType(analogDto
						.getInputType());
				if (analogDto.getStatus())
				{
					analogInput.setExport(M9kConstants.ENABLE_ONE);
				}
				else
				{
					analogInput.setExport(M9kConstants.DISABLE_ZERO);
				}
				
				// START: 13-Mar-2018 - Subtraction implementation
//				analogInput.setInputArray(analogDto.getLstVirtualInputValues().toArray(new String[analogDto.getLstVirtualInputValues().size()]));
				removeExistingInputs(analogInput);
				StringBuffer inputTag ;
				AnalogChannelDTO inputVal ;
				for (Iterator<AnalogChannelDTO> iterator = analogDto.getLstAnalogsForVirtual().iterator(); iterator.hasNext();) {
					inputVal = iterator.next();
					// START: 27-Jan-2020 - Transducer Implementation - Cannot be part of virtual channels. Skip transducer channels
					if (inputVal.isTransducer())
					{
						continue;
					}
					// END: 27-Jan-2020
					inputTag = new StringBuffer();
					inputTag.append(M9kXMLConstants.XML_ANALOG_CHANNELS_INPUT);
					inputTag.append(inputVal.getName());
					inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
					logger.debug("Input tag to be added "+inputTag+ " for analog chnl "+inputVal.getDisplayChannel()+" with operator "+inputVal.getOperator());
					if (inputVal.getOperator() == null || inputVal.getOperator().trim().equals("+"))
					{
						analogInput.addInputAdd(inputTag.toString());
					}
					else
					{
						analogInput.addInputSub(inputTag.toString());						
					}
				}
				// END: 13-Mar-2018 - Subtraction implementation
				analogInput.setVirtual(M9kConstants.ENABLE_ONE);
				
				// START: 23-Feb-2021 - Delta Transformer implementation
				if (analogDto.getVirtualScale() > 0 && analogDto.getVirtualScale() != 1.0)
				{
					analogInput.setVirtualScale(analogDto.getVirtualScale());
				}
				// END: 23-Feb-2021 - Delta Transformer implementation

				  
				logger.debug("Analog input set "+analogInput.xmlText());
			}

			logger.debug("Returning from populateBackToXmlConfig");

	}

	/**
	 * Removes the existing inputs from the analog input
	 * @param analogInput
	 */
	private void removeExistingInputs(AnalogInput analogInput) {
		// Old software compatibility - Input is considered Add
		while (analogInput.getInputArray().length > 0)
		{
			analogInput.removeInput(0);
		}
		// Remove inputs for Add
		while (analogInput.getInputAddArray().length > 0)
		{
			analogInput.removeInputAdd(0);
		}
		// Removes inputs for Sub
		while (analogInput.getInputSubArray().length > 0)
		{
			analogInput.removeInputSub(0);
		}

	}
	/**
	 * @param analogDto
	 * @return
	 */
	private AnalogInput getAnalogInput(AnalogChannelDTO analogDto) {
		AnalogInput analogInput = null;
		DFR xmlDfr;
		xmlDfr = mapXMLDfrs.get(analogDto.getChassis().trim());
		int currentDfrAnalogOffset = getDfrAnalogOffset(analogDto.getChassis().trim());
		AnalogInput[] arrAnalogInput = xmlDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();
		for (int i = 0; i < arrAnalogInput.length; i++) {
			logger.debug(" src channel "+analogDto.getChannel()+ " xml analog channel "+arrAnalogInput[i].getChannel());
			if (analogDto.getChannel().equalsIgnoreCase(""+(arrAnalogInput[i].getChannel()+currentDfrAnalogOffset)))
			{
				analogInput = arrAnalogInput[i];
				logger.debug("Found analogInput "+analogInput);
				break;
			}
		}
		// If it doesn't exist already
		if (analogInput == null)
		{
			logger.debug("analogInput doesn't exist... Creating new one");
			analogInput = xmlDfr.getDataPool().getChannels().getAnalogs().addNewAnalogInput();
			analogInput.setName(analogDto.getName());
			analogInput.setChannel(Integer.parseInt(analogDto.getChannel())-currentDfrAnalogOffset);
			
		}

		return analogInput;
	}
	public String createVirtualChannel()
	  {
		
		logger.debug("Entered createVirtualChannel LstAnalogsForVirtual "+getLstSelectedVirtualChannels()+" circuitName "+circuitName);
		AnalogChannelDTO analogChannelDTO;
		  String returnValue = SUCCESS;
		  newVirtualChannel = new AnalogChannelDTO();
		  newVirtualChannel.setCircuitName(circuitName);
		  List<AnalogChannelDTO> lstUpdateVirtualChannelDetails = populateNewVirtualChannelDetails();
		  getNewVirtualChannel().setLstAnalogsForVirtual(lstUpdateVirtualChannelDetails);
		  logger.debug("Get New Virtual Channel DTO "+getNewVirtualChannel());
		  newVirtualChannel.setVirtualChannelNo(getNextVirtualChannelNo());
		  newVirtualChannel.setChannel(""+getNextDfrChannelIdForVirtuals());
		  newVirtualChannel.setName("VirtualAnalog"+newVirtualChannel.getVirtualChannelNo());
		  newVirtualChannel.setChassis(selectedDfr);
		// START: 23-Feb-2021 - Delta Transformer implementation
		  newVirtualChannel.setVirtualScale(getVirtualScale());
		// END: 23-Feb-2021 - Delta Transformer implementation
			analogChannelDTO = newVirtualChannel.getLstAnalogsForVirtual().get(0);
			logger.debug("analog channel selected for virtual "+analogChannelDTO);
			logger.debug("analog channel selected input type "+analogChannelDTO.getInputType());
			logger.debug("analog channel selected phase "+analogChannelDTO.getPhase());
//			if (analogChannelDTO.getInputType().startsWith(M9kConstants.VOLTAGE))
//			{
//				newVirtualChannel.setInputType(M9kConstants.VOLTAGE);
//			}
//			else
//			{
//				newVirtualChannel.setInputType(M9kConstants.CURRENT);
//			}
			newVirtualChannel.setInputType(analogChannelDTO.getInputType());
		newVirtualChannel.setPhase(analogChannelDTO.getPhase());
		
		  getLstVirtualChannels().add(newVirtualChannel);
			logger.debug("\n\t\tNAME:newVirtualChannel name set "+newVirtualChannel.getName());
		  setNewVirtualChnlIndex(""+(lstVirtualChannels.size()-1));
		  logger.debug("New virtual channel from the list lstVirtualChannels "+lstVirtualChannels.get(Integer.parseInt(getNewVirtualChnlIndex())));
		  logger.debug("\n\t\tNAME: new virtual name xml name "+lstVirtualChannels.get(Integer.parseInt(getNewVirtualChnlIndex())).getName());
		  logger.debug("Returning value from newLineGroup..."+newVirtualChannel+" new index "+getNewVirtualChnlIndex());
		  logger.info("User "+getUserDto().getUserName()+" created a new Virtual Channel. "+newVirtualChannel.getCircuitName());
		  session.put("lstVirtualChannels", getLstVirtualChannels());
		  return returnValue;

	  }

	// gets the next available virtual channel id for selected dfr
	private int getNextDfrChannelIdForVirtuals()
	  {
		  int virtCont = getCurrentDfrDTO().getAnalogChannelEnd();
		  logger.debug("new virtual chanel id Initialized with getCurrentDfrDTO().getAnalogChannelEnd()"+virtCont + " for selected DFR "+getSelectedDfr());
		DFR xmlDfr;
		Analogs xmlAnalogs;
		AnalogInput[] analogInputs;

		xmlDfr = mapXMLDfrs.get(getSelectedDfr());
		xmlAnalogs = xmlDfr.getDataPool().getChannels().getAnalogs();
		analogInputs = xmlAnalogs.getAnalogInputArray();
		  
		  for (int i = 0; i < analogInputs.length; i++) {
			if (analogInputs[i].isSetVirtual() && analogInputs[i].getVirtual() == 1)
			{
				if (virtCont < analogInputs[i].getChannel())
				{
					virtCont = analogInputs[i].getChannel();
					  logger.debug("new virtual id "+virtCont);
				}
			}
		  }
		  AnalogChannelDTO analogChannelDTO;
		  int chnlIndex;
		  for (Iterator<AnalogChannelDTO> iterator = getLstVirtualChannels().iterator(); iterator
				.hasNext();) {
			analogChannelDTO =  iterator
					.next();
			if (analogChannelDTO.getChassis().equalsIgnoreCase(getSelectedDfr()))
			{
				chnlIndex = Integer.parseInt(analogChannelDTO.getChannel());
				if (virtCont < chnlIndex)
				{
					virtCont = chnlIndex;
					  logger.debug("new virtual id "+virtCont);
				}
			}
			
		}
		  // increment to next channel id
		  virtCont++;
		  
		  logger.debug("Returning new virtual cnt as "+virtCont);
		  return virtCont;
	  }
	
	// gets the next available virtual channel number
	private int getNextVirtualChannelNo()
	  {
		int nextVirtualChannelNo = 0;
		  AnalogChannelDTO analogChannelDTO;
		  int chnlIndex;
		  for (Iterator<AnalogChannelDTO> iterator = getLstVirtualChannels().iterator(); iterator
				.hasNext();) {
			analogChannelDTO =  iterator
					.next();
			chnlIndex = Integer.parseInt(analogChannelDTO.getName().substring(analogChannelDTO.getName().indexOf(M9kConstants.VIRTUAL_ANALOG)+M9kConstants.VIRTUAL_ANALOG.length()));
			if (nextVirtualChannelNo < chnlIndex)
			{
				nextVirtualChannelNo = chnlIndex;
			}
			
		}
		  nextVirtualChannelNo++;
		  logger.debug("Returning new virtual cnt as "+nextVirtualChannelNo);
		  return nextVirtualChannelNo;
	  }

	public String saveVirtualChannelOld()
	  {
		String returnValue = SUCCESS;
		logger.debug("What is in here????"+getSelectedDfr());
		logger.debug("Entered saveVirtualChannel LstAnalogsForVirtual "+getLstSelectedVirtualChannels()+" getSelectedChannelIndex() "+getSelectedChannelIndex());
		editedVirtualChannel = new AnalogChannelDTO();
		editedVirtualChannel.setCircuitName(circuitName);
			logger.debug("edited virtual channels circuit name "+getCircuitName());
			logger.debug("edited virtual channels lstAnalogsForVirtual "+getLstSelectedVirtualChannels());
		AnalogChannelDTO analogChannelDTO;
		  int currentSelectedIndex = Integer.parseInt(getSelectedChannelIndex());
		List<AnalogChannelDTO> lstUpdateVirtualChannelDetails = populateNewVirtualChannelDetails();
		// Remove from XML if the chassis is modified
		getEditedVirtualChannel().setLstAnalogsForVirtual(lstUpdateVirtualChannelDetails);
		
		  logger.debug("Get edited Virtual Channel DTO "+getEditedVirtualChannel()+" list of channels selected "+getEditedVirtualChannel().getLstAnalogsForVirtual());
		  getEditedVirtualChannel().setChassis(getLstVirtualChannels().get(currentSelectedIndex).getChassis());
		  getEditedVirtualChannel().setName(getLstVirtualChannels().get(currentSelectedIndex).getName());
		  getEditedVirtualChannel().setChannel(getLstVirtualChannels().get(currentSelectedIndex).getChannel());
		  getEditedVirtualChannel().setVirtualChannelNo(getLstVirtualChannels().get(currentSelectedIndex).getVirtualChannelNo());;
			analogChannelDTO = getEditedVirtualChannel().getLstAnalogsForVirtual().get(0);
			logger.debug("analog channel selected for virtual "+analogChannelDTO);
			logger.debug("analog channel selected input type "+analogChannelDTO.getInputType());
			logger.debug("analog channel selected phase "+analogChannelDTO.getPhase());
//			if (analogChannelDTO.getInputType().startsWith(M9kConstants.VOLTAGE))
//			{
//				newVirtualChannel.setInputType(M9kConstants.VOLTAGE);
//			}
//			else
//			{
//				newVirtualChannel.setInputType(M9kConstants.CURRENT);
//			}
			getEditedVirtualChannel().setInputType(analogChannelDTO.getInputType());
			getEditedVirtualChannel().setPhase(analogChannelDTO.getPhase());
			getLstVirtualChannels().set(currentSelectedIndex, getEditedVirtualChannel());
		  logger.debug("Edited virtual channel from the list lstVirtualChannels "+lstVirtualChannels.get(currentSelectedIndex));
		  logger.debug("User "+getUserDto().getUserName()+" modified a Virtual channel. "+getEditedVirtualChannel().getChassisChannelNo());
		  logger.debug("updated lst of virtual channels "+getLstVirtualChannels()+"\n Removing the list from session");
		  session.remove("lstVirtualChannels");
		  logger.debug("Storing the updated list back in session ");
		  session.put("lstVirtualChannels", getLstVirtualChannels());
		  logger.debug("Retrieving to test the updated list back from session "+(session.get("lstVirtualChannels")));
		  return returnValue;

	  }
	
	/**
	 * Release v1.0.8.2 - Subtraction logic implemented
	 * @return
	 */
	public String saveVirtualChannel()
	  {
		String returnValue = SUCCESS;
		logger.debug("What is in here????"+getSelectedDfr());
		logger.debug("Phase "+getPhase()+" get Input Type "+getInputType());
		logger.debug("Edited Phase "+editedVirtualChannel.getPhase()+" get edited Input Type "+editedVirtualChannel.getInputType());
		logger.debug(" GetSelectedChannelIndex() "+getSelectedChannelIndex() + " Virtual Logic to be saved "+editedVirtualChannel.getVirtualLogic()+" circuit Name "+editedVirtualChannel.getCircuitName());
//		editedVirtualChannel = new AnalogChannelDTO();
//		editedVirtualChannel.setCircuitName(circuitName);
//		editedVirtualChannel.setVirtualLogic(getVirtualLogic());
//			logger.debug("edited virtual channels circuit name "+getCircuitName());
		AnalogChannelDTO analogChannelDTO;
		  int currentSelectedIndex = Integer.parseInt(getSelectedChannelIndex());
		List<AnalogChannelDTO> lstUpdateVirtualChannelDetails = populateNewVirtualChannelDetails();
		// START: 27-Jan-2020 - IF trasducer channels are selected make it invalid
		if (lstUpdateVirtualChannelDetails == null || lstUpdateVirtualChannelDetails.isEmpty())
		{
			logger.error("List of channels in virtuals are empty "+lstUpdateVirtualChannelDetails);
			addFieldError("lineGroupDetails", "Invalid channels selected");
			return ERROR;
		}
		//END: 27-Jan-2020
//		 Remove from XML if the chassis is modified
		getEditedVirtualChannel().setLstAnalogsForVirtual(lstUpdateVirtualChannelDetails);
		
		  logger.debug("Get edited Virtual Channel DTO "+getEditedVirtualChannel()+" list of channels selected "+getEditedVirtualChannel().getLstAnalogsForVirtual());
		  getEditedVirtualChannel().setChassis(getLstVirtualChannels().get(currentSelectedIndex).getChassis());
		  getEditedVirtualChannel().setName(getLstVirtualChannels().get(currentSelectedIndex).getName());
		  getEditedVirtualChannel().setChannel(getLstVirtualChannels().get(currentSelectedIndex).getChannel());
		  getEditedVirtualChannel().setVirtualChannelNo(getLstVirtualChannels().get(currentSelectedIndex).getVirtualChannelNo());;
			analogChannelDTO = getEditedVirtualChannel().getLstAnalogsForVirtual().get(0);
			logger.debug("analog channel selected for virtual "+analogChannelDTO);
			logger.debug("analog channel selected input type "+analogChannelDTO.getInputType());
			logger.debug("analog channel selected phase "+analogChannelDTO.getPhase());
//			if (analogChannelDTO.getInputType().startsWith(M9kConstants.VOLTAGE))
//			{
//				newVirtualChannel.setInputType(M9kConstants.VOLTAGE);
//			}
//			else
//			{
//				newVirtualChannel.setInputType(M9kConstants.CURRENT);
//			}
			getEditedVirtualChannel().setInputType(analogChannelDTO.getInputType());
			// START: 18-Feb-2020 If virtuals include different phases then set the virtual channel's phase to Neutral
//			getEditedVirtualChannel().setPhase(analogChannelDTO.getPhase());
			getEditedVirtualChannel().setPhase(getPhaseBasedOnSelectedChannels(getEditedVirtualChannel().getLstAnalogsForVirtual()));
			// END: 18-Feb-2020
			
			getLstVirtualChannels().set(currentSelectedIndex, getEditedVirtualChannel());
		  logger.debug("Edited virtual channel from the list lstVirtualChannels "+lstVirtualChannels.get(currentSelectedIndex));
		  logger.debug("User "+getUserDto().getUserName()+" modified a Virtual channel. "+getEditedVirtualChannel().getChassisChannelNo());
		  logger.debug("updated lst of virtual channels "+getLstVirtualChannels()+"\n Removing the list from session");
		  session.remove("lstVirtualChannels");
		  logger.debug("Storing the updated list back in session ");
		  session.put("lstVirtualChannels", getLstVirtualChannels());
		  logger.debug("Retrieving to test the updated list back from session "+(session.get("lstVirtualChannels")));
		  logger.info("Saving edited virtual channel "+getEditedVirtualChannel().getDisplayName());
		  return returnValue;

	  }
	
	/**
	 * 18-Feb-2020 - Virtual channels updated to include different channels with different phases
	 * If virtuals include different phases then set the virtual channel's phase to Neutral
	 * @param lstOfSelectedChannels
	 * @return
	 */
	private String getPhaseBasedOnSelectedChannels(List<AnalogChannelDTO> lstOfSelectedChannels)
	{
		String phase = null;
		AnalogChannelDTO analogChannelDTO;
		for (Iterator<AnalogChannelDTO> iterator = lstOfSelectedChannels.iterator(); iterator.hasNext();) {
			analogChannelDTO = iterator.next();
			if (phase == null)
			{
				phase = analogChannelDTO.getPhase();
			}
			else if (!phase.equalsIgnoreCase(analogChannelDTO.getPhase()))
			{
				phase = M9kConstants.PHASE_NEUTRAL;
				break;
			}
		}
		
		return phase;
	}

	/**
	 * 
	 */
	public String deleteVirtualChannel() {
		logger.debug("selected index "+getSelectedChannelIndex());
		int currentSelectedIndex = Integer.parseInt(getSelectedChannelIndex());
		setEditedVirtualChannel(getLstVirtualChannels().get(currentSelectedIndex));
		logger.debug("SElected virtual channel to delete "+getEditedVirtualChannel());
		deleteVirtualChannelFromXML(getEditedVirtualChannel());
		getLstVirtualChannels().remove(currentSelectedIndex);
		logger.debug("Before session update list "+session.get("lstVirtualChannels")+" lst to be updated "+getLstVirtualChannels());
		session.put("lstVirtualChannels", getLstVirtualChannels());
		logger.debug("after session update list "+session.get("lstVirtualChannels"));

	return SUCCESS;	
	}

	
	/**
	 * 06-Feb-2020 - Delete virtual channel from xml
	 * @param triggerChannelDTO
	 */
	private void deleteVirtualChannelFromXML(AnalogChannelDTO virtualChannelToDelete)
	{
		lstAffectedLineGroups = (List<String>)session.get("linegroupsWarnings");
		String virtualChnlToRemove = null;
		DFR xmlDfr = mapXMLDfrs.get(virtualChannelToDelete.getChassis().trim());
		Analogs xmlAnalogs = xmlDfr.getDataPool().getChannels().getAnalogs();
		AnalogInput[] analogInputs = xmlAnalogs.getAnalogInputArray();
		Algorithms xmlAlgorithm = xmlDfr.getDataPool().getAlgorithms();
		
		for (int i = 0; i < analogInputs.length; i++) {
			logger.debug("Analog input xml "+analogInputs[i].xmlText());
			logger.debug("is virtual set "+analogInputs[i].isSetVirtual() +" analog channel "+analogInputs[i].getChannel());
			logger.debug("edited Virtual channel "+virtualChannelToDelete.getChannel()+" analog channel + dfrOffset "+(analogInputs[i].getChannel()+getDfrAnalogOffset(virtualChannelToDelete.getChassis())));
			logger.debug("IS condition true? "+(analogInputs[i].isSetVirtual() && (virtualChannelToDelete.getChannel().equalsIgnoreCase(""+(analogInputs[i].getChannel()+getDfrAnalogOffset(virtualChannelToDelete.getChassis())))) && analogInputs[i].getCircuitName().equalsIgnoreCase(virtualChannelToDelete.getCircuitName())));
			if (analogInputs[i].isSetVirtual() && (virtualChannelToDelete.getChannel().equalsIgnoreCase(""+(analogInputs[i].getChannel()+getDfrAnalogOffset(virtualChannelToDelete.getChassis())))) && analogInputs[i].getCircuitName().equalsIgnoreCase(virtualChannelToDelete.getCircuitName()))
			{
				virtualChnlToRemove = analogInputs[i].getName(); 
				logger.debug("Removing from config xml "+virtualChnlToRemove);
				xmlAnalogs.removeAnalogInput(i);
				logger.debug("Chassis "+virtualChannelToDelete.getChassis()+" xmlAnalogs "+xmlAnalogs.xmlText());
				break;
			}
		}
		// START: 26-Jun-2024 - If virtualChnlToRemove is null then it's newly created virtual channel. Need not delete anything from xml
		if (virtualChnlToRemove == null)
		{
			return;
		}
		// END: 26-Jun-2024 - If virtualChnlToRemove is null then it's newly created virtual channel. Need not delete anything from xml
		// Remove Trigger
		Triggers xmlTriggers = xmlAlgorithm.getTriggers();
		if (xmlTriggers != null)
		{
			Trigger[] xmlTrigger = xmlTriggers.getTriggerArray();
			for (int i = 0; i < xmlTrigger.length; i++) {
				logger.debug("Trigger xml "+xmlTrigger[i].xmlText()+ " name "+xmlTrigger[i].getName());
				if (xmlTrigger[i].getName().endsWith(virtualChnlToRemove))
				{
					TriggerChannelDTO triggerChannelToDelete = new TriggerChannelDTO();
					triggerChannelToDelete.setId(xmlTrigger[i].getChanId());
					triggerChannelToDelete.setChassis(virtualChannelToDelete.getChassis());
					triggerChannelToDelete.setMeasurementName(xmlTrigger[i].getName());
					logger.debug("Trigger to delete "+triggerChannelToDelete);
					M9kXMLUtils.deleteMeasurement(subStation, triggerChannelToDelete, mapXMLDfrs);
				}
			}
		}
		// In case no measurements added to the virtual channel, we need to remove default rms and exports from the config
		// Remove Exports
		logger.debug("Export Name to be compared "+virtualChnlToRemove);
		Exports exports = xmlDfr.getDataPool().getExports();
		Export[] arrExport = exports .getExportArray();
		for (int i = 0; i < arrExport.length; i++) {
			logger.debug("arrExport[i].getName() "+arrExport[i].getName());
			if (arrExport[i].getName().endsWith(virtualChnlToRemove))
			{
				exports.removeExport(i);
			}
		}
		
		// remove RMS
		Rms[] arrRms = xmlDfr.getDataPool().getAlgorithms().getRmsArray();
		for (int i = 0; i <arrRms.length; i++) {
			logger.debug("arrRms[i].getName() "+arrRms[i].getName());
			if (arrRms[i].getName().endsWith(virtualChnlToRemove))
			{
				xmlDfr.getDataPool().getAlgorithms().removeRms(i);
			}
		}
		
		// Update the associated lineGroups
		if (xmlDfr.getDataPool().isSetLineGroups())
		{
			LineGroup xmlLineGroup[] = xmlDfr.getDataPool()
					.getLineGroups().getLineGroupArray();
			String channelName;
			
			for (int i = 0; i < xmlLineGroup.length; i++) {
				logger.debug("i-> "+i+" - "+"Line Groups Name "+xmlLineGroup[i].getLineGroupName());
				for (int j = 0; j < xmlLineGroup[i].getInputArray().length; j++) {
					logger.debug("Source XML "+xmlLineGroup[i].getInputArray()[j]);
					channelName = xmlLineGroup[i].getInputArray()[j].substring(
							xmlLineGroup[i].getInputArray()[j].indexOf("AnalogInput(") + 12,
							xmlLineGroup[i].getInputArray()[j].lastIndexOf(")"));
					logger.debug("Channel name in the lingroup "+channelName);
					if (channelName.equalsIgnoreCase(virtualChnlToRemove))
					{
						if (lstAffectedLineGroups == null)
						{
							lstAffectedLineGroups = new ArrayList<String>();
						}
						lstAffectedLineGroups.add(xmlLineGroup[i].getId()+"-"+xmlLineGroup[i].getName()+"-"+xmlLineGroup[i].getLineGroupName());
						logger.debug("input array to be deleted "+xmlLineGroup[i].getInputArray()[j]);
						xmlLineGroup[i].removeInput(j);
					}
				}
			}
			logger.debug("Affected line groups in session "+lstAffectedLineGroups);
			if (lstAffectedLineGroups != null)
			{
				session.put("linegroupsWarnings", lstAffectedLineGroups);
			}
		}
	
	}
	@SuppressWarnings("unused")
	private void removeRmsifExists(TriggerChannelDTO triggerChannelDTO) {
		DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
		Rms[] arrRms = xmlDfr.getDataPool().getAlgorithms().getRmsArray();
		for (int i = 0; i <arrRms.length; i++) {
			if (triggerChannelDTO.getMeasurementName().equals(arrRms[i].getName()))
			{
				xmlDfr.getDataPool().getAlgorithms().removeRms(i);
				break;
			}
		}
		
	}
	/**
	 * Populate the Analog channels selected from the list to virtual channel 
	 */
	@SuppressWarnings("unused")
	private List<AnalogChannelDTO>  populateNewVirtualChannelDetailsOld() {
		List<AnalogChannelDTO> lstUpdateVirtualChannelDetails = new ArrayList<AnalogChannelDTO>(getLstSelectedVirtualChannels().size());
		String displayName;
		AnalogChannelDTO analogChannelDTO;
		for (Iterator<String> iterator = getLstSelectedVirtualChannels().iterator(); iterator
		.hasNext();) {
			displayName = iterator.next();
			logger.debug("display name from selected list "+displayName);
			for (Iterator<AnalogChannelDTO> iterator2 = getLstAnalogChannels().iterator(); iterator2.hasNext();) {
				analogChannelDTO = iterator2.next();
				logger.debug("anlog chnl to be compared..."+analogChannelDTO.getDisplayName()+" display name..."+displayName);
				if(analogChannelDTO.getDisplayName().equals(displayName))
				{
					lstUpdateVirtualChannelDetails.add(analogChannelDTO);
					break;
				}
			}
		}
		
		return lstUpdateVirtualChannelDetails;
	}

	private List<AnalogChannelDTO>  populateNewVirtualChannelDetails() {
		List<AnalogChannelDTO> lstUpdateVirtualChannelDetails = new ArrayList<AnalogChannelDTO>();
		String displayChannel;
		AnalogChannelDTO analogChannelDTO;
		String logicArr[];
		// START: 18-Feb-2020 - Allow different phases and different types of channels to be part of virtual channels
//		if (getMode() != null && getMode().equalsIgnoreCase("edit"))
//		{
//			logicArr = getEditedVirtualChannel().getVirtualLogic().split("(?<=[-+*/])|(?=[-+*/])");
//		}
//		else
//		{
		// START: 17-Mar-2020 - Adding virtual scale implementation
		String virtualLogicWithoutScale = getVirtualLogic();
		 	if (getVirtualLogic().indexOf("*") != -1)
		 	{
		 		virtualLogicWithoutScale = getVirtualLogic().substring(getVirtualLogic().indexOf("(")+1, getVirtualLogic().lastIndexOf(")")); 
		 	}
//			logicArr = getVirtualLogic().split("(?<=[-+*/])|(?=[-+*/])");
		 	logicArr = virtualLogicWithoutScale.split("(?<=[-+*/])|(?=[-+*/])");
		// END: 17-Mar-2020 - Adding virtual scale implementation
//		}
		// END: 18-Feb-2020
		String operator;
		int iIndex = 0;
		if (logicArr[0].isEmpty())
		{
			operator = logicArr[1];
			iIndex = 2;
			
		}
		else
		{
			operator = "+";
		}
		for (int i = iIndex; i < logicArr.length; i++) {
//			System.out.println(i+" -> "+logicArr[i]);
			displayChannel = logicArr[i].trim();
			logger.debug("Channel display name "+displayChannel+" operator "+operator);
			for (Iterator<AnalogChannelDTO> iterator2 = getLstAnalogChannels().iterator(); iterator2.hasNext();) {
				analogChannelDTO = iterator2.next();
				logger.debug("anlog chnl to be compared..."+analogChannelDTO.getDisplayChannel()+" display Channel..."+displayChannel);
				// START: 27-Jan-2020 - Transducer Implementation - Cannot be part of virtual channels. Skip transducer channels
				if (analogChannelDTO.isTransducer())
				{
					continue;
				}
				//END: 27-Jan-2020
				if(analogChannelDTO.getDisplayChannel().equals(displayChannel))
				{
					analogChannelDTO.setOperator(operator);
					try {
						lstUpdateVirtualChannelDetails.add((AnalogChannelDTO) analogChannelDTO.clone());
					} catch (CloneNotSupportedException e) {
						logger.warn("Exception occured while cloning ",e);
						lstUpdateVirtualChannelDetails.add(analogChannelDTO);
					}
					break;
				}
			}
			if ((++i) < logicArr.length)
			{
				operator = logicArr[i];
			}
		}
		
		return lstUpdateVirtualChannelDetails;
	}

	private DfrDTO getCurrentDfrDTO()
	{
		logger.debug("Selected dfr "+getSelectedDfr());
		DfrDTO currentDfrDto = lstDfrDTO.get(0);
		for (Iterator<DfrDTO> iterator = lstDfrDTO.iterator(); iterator
				.hasNext();) {
			currentDfrDto = iterator
					.next();
			logger.debug("current dfr "+currentDfrDto.getDfrName()+" selected dfr "+getSelectedDfr());
			if (currentDfrDto.getDfrName().equalsIgnoreCase(getSelectedDfr()))
			{
				break;
			}
		}
		return currentDfrDto;
	}
	 @SkipValidation
	  public String back()
	  {
		  logger.debug("Entered Back method in configure Virtual Channels action class");
		  String returnValue = "Back";

		  return returnValue;
	  }
	 
	  @SkipValidation
	  public String backToDisplayVirtualChannels()
	  {
		  logger.debug("Entered backToDisplayVirtualChannels ");
		  String returnValue = "back";


		  return returnValue;
	  }

		/**
		 * Moved from Analogs configuration action class
		 */
		private void populateMapDfrSpecificChannels()
		{
			logger.info("Populating dfr specific analog channels "+lstAnalogChannels.size());
			AnalogChannelDTO analogChannelDTO = null;
			mapDfrSpecificChannels = new HashMap<String, List<AnalogChannelDTO>>();
			for (Iterator<AnalogChannelDTO> iterator = lstAnalogChannels.iterator(); iterator
					.hasNext();) {
				analogChannelDTO = (AnalogChannelDTO) iterator
						.next();
//				logger.info("Analog Channel "+analogChannelDTO);
//				logger.info("Analog Channel chassis "+analogChannelDTO.getChassis());
//				logger.info("Analog Channel Phase "+analogChannelDTO.getPhase());
				if (mapDfrSpecificChannels.get(analogChannelDTO.getChassis()) == null)
				{
					mapDfrSpecificChannels.put(analogChannelDTO.getChassis(), new ArrayList<AnalogChannelDTO>());
				}
				if (analogChannelDTO.getPhase() != null && !analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE))
				{
					mapDfrSpecificChannels.get(analogChannelDTO.getChassis()).add(analogChannelDTO);
				}
			}
			logger.info("Returning from populateMapDfrSpecificChannels "+mapDfrSpecificChannels);
		}

	 
	@SuppressWarnings("unchecked")
	@Override
	public void setSession(Map<String, Object> session) {
		// TODO Auto-generated method stub
		this.session = session;
		stationDetails = (StationDTO) session.get("stationDetails");
		lstDfrDTO = (List<DfrDTO>) session.get("DFRsList");
		accessMode = (String) session.get("accessMode");
		mapXMLDfrs = (Map<String, DFR>) session.get("mapXMLDfrs");
		userDto = (UsersDTO) session.get("userDetails");
		lstAnalogChannels = (List<AnalogChannelDTO>) session.get("lstAnalogChannels");
		lstVirtualChannels = (List<AnalogChannelDTO>) session.get("lstVirtualChannels");
//		logger.info("From setSession method "+lstVirtualChannels);
		subStation = (SubStation) session.get("selectedSubstation");
		lineGroupExists = (Boolean)session.get("lineGroupExists"); // To make the faultlocation appear after creating virtual channel
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

	public boolean isLineGroupExists() {
		return lineGroupExists;
	}

	public void setLineGroupExists(boolean lineGroupExists) {
		this.lineGroupExists = lineGroupExists;
	}

	public void validate()
	{
		logger.debug("Inside validate method "+getSelectedChannelIndex());
		logger.debug("edited virtual channels "+getSelectedDfr());
		logger.debug("selected analog channels "+getLstSelectedVirtualChannels());
//		if (getLstSelectedVirtualChannels() !=null)
//		{
//			List<AnalogChannelDTO> lstUpdateVirtualChannelDetails = populateNewVirtualChannelDetails();
//			// Remove from XML if the chassis is modified
//			setLstAnalogsForVirtual(lstUpdateVirtualChannelDetails);
//		}
		
//		// New Virtual dialog validation
//		if (getNewVirtualChannel() != null)
//		{
//			if (getNewVirtualChannel().getCircuitName() == null || getNewVirtualChannel().getCircuitName().trim().isEmpty())
//			{
//				addFieldError("newVirtualChannel.circuitName", "Channel Description is Required");
//			}
//			else if (getNewVirtualChannel().getCircuitName().indexOf(",") != -1 || getNewVirtualChannel().getCircuitName().indexOf("/") != -1 || getNewVirtualChannel().getCircuitName().indexOf("(") != -1
//					|| getNewVirtualChannel().getCircuitName().indexOf(")") != -1 || getNewVirtualChannel().getCircuitName().indexOf("[") != -1 || getNewVirtualChannel().getCircuitName().indexOf("]") != -1)
//			{
//				addFieldError("newVirtualChannel.circuitName", "Special Characters not allowed: / ( ) [ ] , ");
//			}
//		}
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
	 * @return the lstVirtualChannels
	 */
	public List<AnalogChannelDTO> getLstVirtualChannels() {
		return lstVirtualChannels;
	}

	/**
	 * @param lstVirtualChannels the lstVirtualChannels to set
	 */
	public void setLstVirtualChannels(List<AnalogChannelDTO> lstVirtualChannels) {
		this.lstVirtualChannels = lstVirtualChannels;
	}

	/**
	 * @return the lstDfrsAnalogChannels
	 */
	public List<AnalogChannelDTO> getLstDfrsAnalogChannels() {
		return lstDfrsAnalogChannels;
	}

	/**
	 * @param lstDfrsAnalogChannels the lstDfrsAnalogChannels to set
	 */
	public void setLstDfrsAnalogChannels(
			List<AnalogChannelDTO> lstDfrsAnalogChannels) {
		this.lstDfrsAnalogChannels = lstDfrsAnalogChannels;
	}

	/**
	 * @return the selectedChannelIndex
	 */
	public String getSelectedChannelIndex() {
		return selectedChannelIndex;
	}

	/**
	 * @param selectedChannelIndex the selectedChannelIndex to set
	 */
	public void setSelectedChannelIndex(String selectedChannelIndex) {
		this.selectedChannelIndex = selectedChannelIndex;
	}

	/**
	 * @return the newVirtualChannel
	 */
	public AnalogChannelDTO getNewVirtualChannel() {
		return newVirtualChannel;
	}

	/**
	 * @param newVirtualChannel the newVirtualChannel to set
	 */
	public void setNewVirtualChannel(AnalogChannelDTO newVirtualChannel) {
		this.newVirtualChannel = newVirtualChannel;
	}

	/**
	 * @return the lstAnalogsForVirtual
	 */
	public List<AnalogChannelDTO> getLstAnalogsForVirtual() {
		return lstAnalogsForVirtual;
	}

	/**
	 * @param lstAnalogsForVirtual the lstAnalogsForVirtual to set
	 */
	public void setLstAnalogsForVirtual(List<AnalogChannelDTO> lstAnalogsForVirtual) {
		this.lstAnalogsForVirtual = lstAnalogsForVirtual;
	}

	/**
	 * @return the lstAnalogDfrs
	 */
	public List<DfrDTO> getLstAnalogDfrs() {
		return lstAnalogDfrs;
	}

	/**
	 * @param lstAnalogDfrs the lstAnalogDfrs to set
	 */
	public void setLstAnalogDfrs(List<DfrDTO> lstAnalogDfrs) {
		this.lstAnalogDfrs = lstAnalogDfrs;
	}

	/**
	 * @return the newVirtualChnlIndex
	 */
	public String getNewVirtualChnlIndex() {
		return newVirtualChnlIndex;
	}

	/**
	 * @param newVirtualChnlIndex the newVirtualChnlIndex to set
	 */
	public void setNewVirtualChnlIndex(String newVirtualChnlIndex) {
		this.newVirtualChnlIndex = newVirtualChnlIndex;
	}

	/**
	 * @return the lstSelectedVirtualChannels
	 */
	public List<String> getLstSelectedVirtualChannels() {
		return lstSelectedVirtualChannels;
	}

	/**
	 * @param lstSelectedVirtualChannels the lstSelectedVirtualChannels to set
	 */
	public void setLstSelectedVirtualChannels(List<String> lstSelectedVirtualChannels) {
		this.lstSelectedVirtualChannels = lstSelectedVirtualChannels;
	}

	/**
	 * @return the editedVirtualChannel
	 */
	public AnalogChannelDTO getEditedVirtualChannel() {
		return editedVirtualChannel;
	}

	/**
	 * @param editedVirtualChannel the editedVirtualChannel to set
	 */
	public void setEditedVirtualChannel(AnalogChannelDTO editedVirtualChannel) {
		this.editedVirtualChannel = editedVirtualChannel;
	}

	/**
	 * @return the lstRemovedVirtualChannels
	 */
	public List<AnalogChannelDTO> getLstRemovedVirtualChannels() {
		return lstRemovedVirtualChannels;
	}

	/**
	 * @param lstRemovedVirtualChannels the lstRemovedVirtualChannels to set
	 */
	public void setLstRemovedVirtualChannels(
			List<AnalogChannelDTO> lstRemovedVirtualChannels) {
		this.lstRemovedVirtualChannels = lstRemovedVirtualChannels;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return the originTab
	 */
	public String getOriginTab() {
		return originTab;
	}

	/**
	 * @param originTab the originTab to set
	 */
	public void setOriginTab(String originTab) {
		this.originTab = originTab;
	}
	/**
	 * @return the circuitName
	 */
	public String getCircuitName() {
		return circuitName;
	}
	/**
	 * @param circuitName the circuitName to set
	 */
	public void setCircuitName(String circuitName) {
		this.circuitName = circuitName;
	}
	/**
	 * @return the phase
	 */
	public String getPhase() {
		return phase;
	}
	/**
	 * @param phase the phase to set
	 */
	public void setPhase(String phase) {
		this.phase = phase;
	}
	/**
	 * @return the inputType
	 */
	public String getInputType() {
		return inputType;
	}
	/**
	 * @param inputType the inputType to set
	 */
	public void setInputType(String inputType) {
		this.inputType = inputType;
	}
	/**
	 * @return the affectedLineGroups
	 */
	public List<String> getLstAffectedLineGroups() {
		return lstAffectedLineGroups;
	}
	/**
	 * @param affectedLineGroups the affectedLineGroups to set
	 */
	public void setLstAffectedLineGroups(List<String> affectedLineGroups) {
		this.lstAffectedLineGroups = affectedLineGroups;
	}
	
	  public int getDfrAnalogOffset(String dfrName)
	  {
		  logger.debug("DFr name to look up "+dfrName);
		  int analogOffset = 0;
		  DfrDTO dfrDTO;
		  for (Iterator<DfrDTO> iterator = lstDfrDTO.iterator(); iterator.hasNext();) {
			  dfrDTO = iterator.next();
			  logger.debug("DFRName from the list to compare "+dfrDTO.getDfrName());
			if (dfrDTO.getDfrName().equalsIgnoreCase(dfrName))
			{
				analogOffset = dfrDTO.getAnalogChannelStart()-1;
				break;
			}
		}
		  logger.debug("Analog offset to be returned "+analogOffset);
		  return analogOffset;
	  }
	public String getVirtualLogic() {
		return virtualLogic;
	}
	public void setVirtualLogic(String virtualLogic) {
		this.virtualLogic = virtualLogic;
	}
	public double getVirtualScale() {
		return virtualScale;
	}
	public void setVirtualScale(double virtualScale) {
		this.virtualScale = virtualScale;
	}
	/**
	 * @return the mapDfrSpecificChannels
	 */
	public Map<String, List<AnalogChannelDTO>> getMapDfrSpecificChannels() {
		return mapDfrSpecificChannels;
	}
	/**
	 * @param mapDfrSpecificChannels the mapDfrSpecificChannels to set
	 */
	public void setMapDfrSpecificChannels(Map<String, List<AnalogChannelDTO>> mapDfrSpecificChannels) {
		this.mapDfrSpecificChannels = mapDfrSpecificChannels;
	}

}
