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

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
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
import com.usi.ExportDocument2.Export;
import com.usi.ExportsDocument.Exports;
import com.usi.LimitsDocument.Limits;
import com.usi.MeasurementDocument.Measurement;
import com.usi.MeasurementsDocument.Measurements;
import com.usi.PmuDocument.Pmu;
import com.usi.PmuInputDocument.PmuInput;
import com.usi.PmuInputsDocument.PmuInputs;
import com.usi.SubStationDocument.SubStation;
import com.usi.TriggerDocument;
import com.usi.TriggerDocument.Trigger;
import com.usi.TriggersDocument.Triggers;
import com.usi.VirtualMeasurementDocument.VirtualMeasurement;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.algorithms.VirtualMeasurementAlgorithm;
import com.usi.m9000.algorithms.VirtualMeasurementAlgorithm.VirtualMeasurementInput;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.TriggerChannelDTO;
import com.usi.m9000.dto.TriggerSourceDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.triggers.LimitsAlgorithm;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.M9kXMLConstants;
import com.usi.m9000.xml.util.M9kXMLUtils;


public class ConfigureVirtualMeasurementsAction extends ActionSupport implements SessionAware, ServletContextListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String selectedDfr;
	private String btnSubmit;
	private Map<String, Object>  session;
	private StationDTO stationDetails;
	private String analogChannelsCount;
	private String digitalChannelsCount;
	int minimumDFRCount;
	private Counter analogChannelCounter;
	private Counter digitalChannelCounter;
	private Counter triggerChannelCounter;
	Map<String, Integer> analogCnt;
	Map<String, Integer> digitalCnt;
	List<Map<String, Integer>> lstAnalog;
	List<Map<String, Integer>> lstDigital;
//	ChannelsListInfo channelsConfList;
	List<DfrDTO> lstDfrDTO;
	SubStation currentSubstation;
	DFR xmlConfigDfr;
	EventInput[] digitals;
	List<TriggerChannelDTO> lstTriggerChannels;
	List<TriggerChannelDTO> lstTriggerChannelsHidden;
	private Map<String,String> json;
	String selectedIndex;
	 int reqdIndex;
	String accessMode;
	PropertiesConfiguration config;
	String sourceTab;
	String originTab;
	private Map<String, DFR> mapXMLDfrs;
	boolean booAnalog;
	boolean booDigital;
	private Boolean lineGroupExists;
//	private Integer freqPmuId; // Used for Freq to be tied to PMU  
	PmuInputs pmuInputs;
	  PmuInput[] pmuInput = null;
	  private boolean newAdded = false;
	  private Integer addedIndex = 0; 
	  UsersDTO userDto;
//	  List<Object> lstAllTriggerTypes;
	  private Map<String, String> mapGlobalTriggerInputTypes;
	  private List<AnalogChannelDTO> lstVirtualChannels;
	  private List<String> lstAffectedLineGroups;
		// START: 03-Mar-2021 - Implementation of virtual measurements for delta transformers
	  private List<TriggerChannelDTO> lstVirtualMeasurements;
	  private List<TriggerChannelDTO> lstPowerMeasurements;
	  private List<TriggerChannelDTO> lstDfrsMeasurements;
	  private List<DfrDTO> lstAnalogDfrs = new ArrayList<DfrDTO>();
	  private String mode; // New or Edit mode
	  private TriggerChannelDTO editedVirtualMeasurement;
	  private Map<String,List<TriggerChannelDTO>> mapDfrSpecificMeasurements = null;
	  private String virtualLogic; // Logic defined for each virtual measurement like A1 + A2 - A3
	  private List<String> lstMeasurementTypes = null;
	  private String measurementType;
	  private String description;
	  private List<LineGroupsAlgorithm> lstLineGroups;
	  private int selectedVirtualIndex;
	  private List<TriggerSourceDTO> lstTriggerSourceDto;
	private List<TriggerChannelDTO> lstDeletedMeasurements;
//	  private String triggerOutHold;
	  
	  // END: 12-Mar-2021 - Implementation of virtual measurements for delta transformers 

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ConfigureVirtualMeasurementsAction.class);
	
	/**
	 * 
	 */
	public ConfigureVirtualMeasurementsAction(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
	}
	
	public ConfigureVirtualMeasurementsAction()
	{
		logger.debug("Entered constructor of ConfigureVirtualChannelsAction req index "+getReqdIndex());
	}

	@Override
	public String execute() throws M9000Exception{
		  String returnValue = SUCCESS;
		try
		{
		  logger.debug("entered ConfigureVirtualMeasurementsAction execute.."+getSourceTab()+" originating tab "+getOriginTab());
			booAnalog = (Boolean)session.get("booAnalog");
			booDigital = (Boolean)session.get("booDigital");
			lineGroupExists = (Boolean)session.get("lineGroupExists");
			logger.debug("DIGITAL: booAnalog "+booAnalog+" booDigital "+booDigital+" lineGroupExists "+lineGroupExists);
			if (getSourceTab() == null) 
			  {
				sourceTab = (String) session.get("sourceTab");  
			  }

			// START: 25-Nov-2024 - Cleanup virtual measurements for all deleted measurements
			if (getOriginTab() != null && !getOriginTab().equalsIgnoreCase("VirtualMeasurements") && getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
			{
				lstDeletedMeasurements = (List<TriggerChannelDTO>) session.get("lstDeletedMeasurements");
				logger.debug("Checking for affected virtual measurements for lstDeletedMeasurements "+lstDeletedMeasurements);
				if (lstDeletedMeasurements != null && lstDeletedMeasurements.size() > 0)
				{
					boolean isVirtualMeasurementAffected = false;
					for (TriggerChannelDTO deletedTriggerChannelDTO : lstDeletedMeasurements)
					{
						logger.debug("Check for virtual measurment inputs..."+deletedTriggerChannelDTO);
						
						// Check for affected and turn the flag to true even if one of them is affected
						if (!isVirtualMeasurementAffected)
							{
								isVirtualMeasurementAffected = checkVirtualMeasurementsForDeletedMeasurement(deletedTriggerChannelDTO);
							}
							else
							{
								checkVirtualMeasurementsForDeletedMeasurement(deletedTriggerChannelDTO);
							}
					}
					addActionMessage("Virtual measurements might be affected due to the deleted measurements. Please verify.");
					setSourceTab(null);
					
				}
			}
			// END: Cleanup virtual measurements for all deleted measurements

		  
		  if (getSourceTab() != null && !getSourceTab().isEmpty() && !getSourceTab().equalsIgnoreCase("VirtualMeasurements"))
		  {
			  save();
			  session.put("sourceTab", sourceTab);
			  if (getSourceTab().equalsIgnoreCase("Measurements"))
				{
				  returnValue = back();
				}
			  else if (getSourceTab().equalsIgnoreCase("Events"))
				{
					session.remove("lstDeletedMeasurements");
				  returnValue = navigateToEvents();
				  return returnValue;
				}
			  else if (getSourceTab().equalsIgnoreCase("FaultLocation"))
				{
				  logger.debug("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is forwarded via Triggers/Measurements configuration screen");
				  returnValue = "FaultLocation";
//				  lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
				  logger.debug("Forwarding to FaultLocation "+((getLstTriggerChannels() != null)?getLstTriggerChannels().size():"lstTriggerChannels is null "));
				  return returnValue;
//				  saveTriggers();
//				  return returnValue;
				}
			  else 
			  {
				  if (getSourceTab().equalsIgnoreCase("LineGroups"))
				  {
					  returnValue = "LineGroups";
				  }
				  else if (getSourceTab().equalsIgnoreCase("Analogs"))
				  {
//					  saveTriggers();
					  returnValue = "Analogs";
					  session.remove("lineGroupDetails");
					  session.remove("flagEdit");
					  
				  }
				  else if (getSourceTab().equalsIgnoreCase("StationDetails"))
					{
//					  saveTriggers();
					  returnValue="StationDetails";
					}
				  else if (getSourceTab().equalsIgnoreCase("VirtualChannels"))
					{
					  returnValue="VirtualChannels";
					}
				  return returnValue;
			  }
		  }
			  

			if (stationDetails.getSystemDigitalChannelsCount() > 0)
			{
				setBooDigital(true);
			}
			else
			{
				setBooDigital(false);
			}
		logger.debug("Is digital? "+booDigital);
		  logger.debug("@#$@#$@#$ lstTriggerChannels.."+getLstTriggerChannels());
		  
		  logger.debug("Mode "+getAccessMode());
		  
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occured in Measurement action ",e);
			throw new M9000Exception(e);
		}
		  return returnValue;

	  }
	  
	@SuppressWarnings("unchecked")
	private boolean checkVirtualMeasurementsForDeletedMeasurement(TriggerChannelDTO triggerChannelDTO) {
		lstVirtualMeasurements = (List<TriggerChannelDTO>) session.get("VIRTUAL_MEASUREMENTS_LIST");
		boolean changed = false;
		boolean deleted = false;
		List<VirtualMeasurementInput> lstVirtualMeasurementInput; 
		VirtualMeasurementInput virtualMeasurementInput;
		VirtualMeasurementAlgorithm virtualMeasurementAlgorithm;
		logger.debug("Inside delete...lstVirtualMeasurements "+lstVirtualMeasurements);
		if (lstVirtualMeasurements != null && !lstVirtualMeasurements.isEmpty())
		{
			TriggerChannelDTO virtTriggerChannelDTO;
			for (Iterator<TriggerChannelDTO> iterator = lstVirtualMeasurements.iterator(); iterator.hasNext();) {
				virtTriggerChannelDTO = iterator.next();
				virtualMeasurementAlgorithm = new VirtualMeasurementAlgorithm(M9kUtils.getDfrsSpecificMeasurements(virtTriggerChannelDTO.getChassis(),getLstTriggerChannels()));
				lstVirtualMeasurementInput = virtualMeasurementAlgorithm.parseVirtualLogic(virtTriggerChannelDTO.getVirtualLogic());
//				deleted = false;
				logger.debug("lstVirtualMeasurementInput "+lstVirtualMeasurementInput);
//				for (Iterator<VirtualMeasurementInput> iterator2 = lstVirtualMeasurementInput.iterator(); iterator2.hasNext();) {
//					virtualMeasurementInput = iterator2.next();
//					logger.debug("Check: virtualMeasurementInput.getInputMeasurement().getId() "+virtualMeasurementInput.getInputMeasurement().getId()+" TriggerChannelDTO deleted "+triggerChannelDTO.getId());
//					if (virtualMeasurementInput.getInputMeasurement().getId().intValue() == triggerChannelDTO.getId().intValue())
//					{
//						logger.debug("Removing the input as measurement is no longer available."+virtualMeasurementInput);
//						iterator2.remove();
//						changed=true; // To update the session objects
//						deleted = true; // 
//					}
//				}
//				if (deleted)
//				{
					virtualMeasurementAlgorithm.setLstVirtualMeasurementInput(lstVirtualMeasurementInput);
					virtTriggerChannelDTO.setVirtualLogic(virtualMeasurementAlgorithm.getVirtualLogic());
					logger.debug("After deleting updated virtual logic "+virtTriggerChannelDTO.getVirtualLogic());
//				}
			}
		}
//		if (changed)
//		{
			session.put("VIRTUAL_MEASUREMENTS_LIST", getLstVirtualMeasurements());
//		}
		return changed;
	}
	public String populateLstVirtualMeasurements()
	{
		String returnValue = SUCCESS;
		logger.debug("In populateLstVirtualMeasurements "+getLstVirtualMeasurements()+" is (getLstVirtualMeasurements() == null || getLstVirtualMeasurements().isEmpty()) logic true? "+(getLstVirtualMeasurements() == null || getLstVirtualMeasurements().isEmpty()));
		if (getLstVirtualMeasurements() == null || getLstVirtualMeasurements().isEmpty())
		{
			setLstVirtualMeasurements(parseLstMeasurementsFromXml());
		}
		TriggerChannelDTO triggerChannelDTO;
		int iCnt=0;
		for (Iterator<TriggerChannelDTO> iterator = getLstVirtualMeasurements().iterator(); iterator.hasNext();) {
			triggerChannelDTO = iterator.next();
			if (triggerChannelDTO.getVirtualLogic() == null 
					|| triggerChannelDTO.getVirtualLogic().trim().isEmpty()
					|| triggerChannelDTO.getVirtualLogic().trim().startsWith("()")
					|| !triggerChannelDTO.getVirtualLogic().trim().startsWith("("))
			{
				logger.debug("Adding error message to virtual logic "+triggerChannelDTO+" added to "+"lstVirtualMeasurements["+iCnt+"].virtualLogic");
				addFieldError("lstVirtualMeasurements["+iCnt+"].virtualLogic", "Virtual logic is incomplete");
			}
			iCnt++;
		}
		return returnValue;
	}
	/**
	 *  03-Mar-2021 - Implementation of virtual measurements for delta transformers 
	 * @return
	 */
	public String saveVirtualMeasurementsAndNext() throws Exception
	{
		String returnValue = SUCCESS;
		session.remove("lstDeletedMeasurements");
		save();
		if (booDigital)
		  {
			  returnValue = "Events";
		  }
		else if ((getLineGroupExists() != null && getLineGroupExists()) || (lstLineGroups!=null && !lstLineGroups.isEmpty()))
		{
			returnValue = "DirectFaultLocation";
		}
		return returnValue;
	}

	@SkipValidation
	public String showNewVirtualMeasurementDialog()
	{
		String returnValue = SUCCESS;
		logger.debug("Inside showNewVirtualMeasurementDialog "+getLstDfrDTO());
		setMeasurementType("All");
		fetchLstOfDFrs();
		fetchMeasurementTypes();
		lstDfrsMeasurements = new ArrayList<TriggerChannelDTO>();
		selectedDfr = getLstAnalogDfrs().get(0).getDfrName();
		logger.debug("Default dfr for new "+selectedDfr);
		logger.debug("List of virtual Measurements "+getLstVirtualMeasurements());
		getDfrsSpecificMeasurements();
		return returnValue;
	}

	@SkipValidation
	public String showEditVirtualMeasurementDialog()
	{
		String returnValue = SUCCESS;
		logger.debug("Inside showNewVirtualMeasurementDialog "+getLstDfrDTO()+" selected virtual Index "+getSelectedVirtualIndex());
		setMeasurementType("All");
		fetchLstOfDFrs();
		fetchMeasurementTypes();
		lstDfrsMeasurements = new ArrayList<TriggerChannelDTO>();
		logger.debug("List of virtual Measurements "+getLstVirtualMeasurements());
		logger.debug("selected dfr "+selectedDfr);
		setSelectedDfr(getLstVirtualMeasurements().get(getSelectedVirtualIndex()).getChassis());
		setDescription(getLstVirtualMeasurements().get(getSelectedVirtualIndex()).getName());
		setVirtualLogic(getLstVirtualMeasurements().get(getSelectedVirtualIndex()).getVirtualLogic());
		getDfrsSpecificMeasurements();
		return returnValue;
	}

	@SuppressWarnings("unchecked")
	private void fetchLstOfDFrs()
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

	}
	
	@SkipValidation
	public String getDfrsSpecificMeasurements()
	{
		logger.debug("Default dfr for new "+selectedDfr+" selected measurement type "+getMeasurementType());
		lstDfrsMeasurements = new ArrayList<TriggerChannelDTO>();
		if (lstTriggerChannels != null)
		{
			TriggerChannelDTO triggerChannelDTO;
			for (Iterator<TriggerChannelDTO> iterator = lstTriggerChannels.iterator(); iterator.hasNext();) {
				triggerChannelDTO = iterator.next();
				if (triggerChannelDTO == null || triggerChannelDTO.getId() == null || triggerChannelDTO.getType() == null || triggerChannelDTO.getType().equalsIgnoreCase("Delete")
						|| !triggerChannelDTO.getChassis().equalsIgnoreCase(selectedDfr) || (getMeasurementType() != null && !getMeasurementType().equalsIgnoreCase("All") && !triggerChannelDTO.getType().equalsIgnoreCase(getMeasurementType()))
//						|| triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.RMS) || triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.FREQUENCY) 
//						|| triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.HARMONIC) || triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.PHASOR)
						)
				{
					continue;
				}
				logger.debug("Adding to the measurements list "+triggerChannelDTO);
				lstDfrsMeasurements.add(triggerChannelDTO);			
			}
		}
		logger.debug("lst of dfr measurements for virtuals "+getLstDfrsMeasurements());
		return SUCCESS;
	}

	public String fetchMeasurementTypes() {
		logger.debug("Inside fetchMeasurementTypes");
    	lstMeasurementTypes = null;
		try {
			lstMeasurementTypes = M9kXMLUtils.getMeasurementTypes();
			if (lstMeasurementTypes == null || lstMeasurementTypes.isEmpty())
			{
				lstMeasurementTypes = new ArrayList<String>(2);
				lstMeasurementTypes.add("All");
				lstMeasurementTypes.add(M9kConstants.RMS);				
			}
			else
			{
				lstMeasurementTypes.add(0, "All");
			}
			logger.debug("Measurement types "+lstMeasurementTypes);
		} catch (Exception e) {
			logger.error("Unable to get measurement names from config xml. Returning default RMS.",e);
			lstMeasurementTypes = new ArrayList<String>(2);
			lstMeasurementTypes.add("All");
			lstMeasurementTypes.add(M9kConstants.RMS);
		}
		logger.debug("About to return measurement types "+lstMeasurementTypes);
		return SUCCESS;
	}
	
	  @SuppressWarnings("unchecked")
	@SkipValidation
	  public String createVirtualMeasurement()
	  {
		  String returnValue = SUCCESS;
		  try
		  {
			  lstVirtualMeasurements = (List<TriggerChannelDTO>)session.get("VIRTUAL_MEASUREMENTS_LIST");
		  
		  logger.debug("@#$@#$@#$ in addNewTriggerChannel lstVirtualMeasurements.."+getLstVirtualMeasurements());
		  

		  if (getLstVirtualMeasurements() == null) 
		  {
			  lstVirtualMeasurements = new ArrayList<TriggerChannelDTO>();
		  }
		  TriggerChannelDTO triggerChannelDto = new TriggerChannelDTO();
		  if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
		  {
			  triggerChannelDto.setId(StationDTO.getNextAvailableVirtualMeasurementId());  
//			  triggerChannelDto.setId(-1);
		  }
		  triggerChannelDto.setType(M9kConstants.VIRTUAL_MEASUREMENT);
		  triggerChannelDto.setName(getDescription());
		  triggerChannelDto.setPhase(M9kConstants.NO_PHASE);
		  triggerChannelDto.setChannel("");
		  triggerChannelDto.setVirtualLogic(getVirtualLogic());
		  triggerChannelDto.setChassis(getSelectedDfr());
		  triggerChannelDto.setDisableHarmonic(true);
		  triggerChannelDto.setGlobalLineGroupId(0);
		  triggerChannelDto.setStart(M9kConstants.NEVER);
		  triggerChannelDto.setDisableTripOver(true);
		  triggerChannelDto.setDisableTripUnder(true);

		  triggerChannelDto.setDisableTripRoc(true);
		  triggerChannelDto.setDisableDuration(true);
		  triggerChannelDto.setStatus(true);
		  triggerChannelDto.setExportStatus(true);
		  triggerChannelDto.setPmuStatus(false);
		  triggerChannelDto.setDdrStatus(true);
		// START: 30-Dec-2022 Disturbance Alarm implementation
		  triggerChannelDto.setDisturbanceAlarm(false);
		  // END: 30-Dec-2022
		  triggerChannelDto.setTriggerStatus(false);
		  triggerChannelDto.setTransducer(false);

		  lstVirtualMeasurements.add(triggerChannelDto);
		  session.put("VIRTUAL_MEASUREMENTS_LIST", getLstVirtualMeasurements());
		  logger.debug("size of lst "+lstVirtualMeasurements.size());
		  }
		  catch (Exception e) {
			logger.error("Error occurred adding a new channel", e);
		}
		  return returnValue;
	  }

	  @SkipValidation
	  public String saveVirtualMeasurements()
	  {
		  session.remove("lstDeletedMeasurements");
		  logger.debug("selected index "+getSelectedVirtualIndex()+" selected dfr "+getSelectedDfr()+" get description "+getDescription()+" virtual logic "+getVirtualLogic());
		  getLstVirtualMeasurements().get(getSelectedVirtualIndex()).setChassis(getSelectedDfr());
		  getLstVirtualMeasurements().get(getSelectedVirtualIndex()).setName(getDescription());
		  getLstVirtualMeasurements().get(getSelectedVirtualIndex()).setVirtualLogic(getVirtualLogic());	
		  TriggerChannelDTO triggerChannelDTO;
			int iCnt=0;
			for (Iterator<TriggerChannelDTO> iterator = getLstVirtualMeasurements().iterator(); iterator.hasNext();) {
				triggerChannelDTO = iterator.next();
				if (triggerChannelDTO.getVirtualLogic() == null 
						|| triggerChannelDTO.getVirtualLogic().trim().isEmpty()
						|| triggerChannelDTO.getVirtualLogic().trim().startsWith("()")
						|| !triggerChannelDTO.getVirtualLogic().trim().startsWith("("))
				{
					logger.debug("Adding error message to virtual logic "+triggerChannelDTO+" added to "+"lstVirtualMeasurements["+iCnt+"].virtualLogic");
					addFieldError("lstVirtualMeasurements["+iCnt+"].virtualLogic", "Virtual logic is incomplete");
				}
				iCnt++;
			}
		  return SUCCESS;
	  }
	  private void  save() throws M9000Exception
	  {
		  logger.debug("In save method");
		  
		  try
		  {
			  currentSubstation = (SubStation) session.get("selectedSubstation");
			  if (getSourceTab() != null && getSourceTab().isEmpty())
				{
					session.remove("sourceTab");
				}
			  
			  logger.debug("Mode "+getAccessMode());
			  
			  logger.debug("\t\t\tlstVirtualMeasurements is null or empty "+((getLstVirtualMeasurements()!=null)?getLstVirtualMeasurements().size():null));
			  if (getLstVirtualMeasurements()!= null && !getLstVirtualMeasurements().isEmpty())
			  {
				  session.put("VIRTUAL_MEASUREMENTS_LIST", getLstVirtualMeasurements());
				  if (getLstVirtualMeasurements().size() > 0)
				  {
//					  getLstTriggerChannels().addAll(getLstVirtualMeasurements());
					  populateVirtualMeasurementsIntoXml();
				  }
			  }
		  } 
		  catch (Exception e) {
			logger.error("Exception occured while saving virtual measurement details ",e);
			throw new M9000Exception(e);
		}
		  
	  }

	  public String finish() throws Exception
	  {
		  save();
		  
		  saveIntoDB();
		  try
		  {
			  session.remove("linegroupsWarnings");		
			  // remove measurement warning as well
			  session.remove("LINEGROUP_AFFECTED_MEASUREMENTS");
			  session.remove("MEASUREMENTS_WARNING_MESSAGE");
			  session.remove("lstDeletedMeasurements");
		  }
		  catch(Exception e)
		  {
			  logger.error("Unable to clear linegroups warning from session ",e);
		  }
		  return "Save";
	  }
	  private void saveIntoDB() throws M9000Exception
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
			int configSerialNumber = 1;
			DFR[] dfrs = currentSubstation.getDFRs().getDFRArray();
			logger.debug("IS confugSErialNumber set already? "+dfrs[0].getSystem().isSetConfigSerialNumber());
			if (dfrs[0].getSystem().isSetConfigSerialNumber())
			{
				logger.debug("ConfigSEriaNumber already set "+currentSubstation.getDFRs().getDFRArray(0).getSystem().getConfigSerialNumber());
				configSerialNumber = currentSubstation.getDFRs().getDFRArray(0).getSystem().getConfigSerialNumber()+1;
			}
			for (int i = 0; i < dfrs.length; i++) {
				dfrs[i].getSystem().setConfigSerialNumber(configSerialNumber);
			}

			((StationDTO)session.get("stationDetails")).setConfigStatus("COMPLETE");
			String xmlText = currentSubstation.xmlText(xmlOptions);
//			logger.debug("Current sub station after save "+xmlText);
			Diff diff = DiffBuilder.compare(Input.fromString(stationDetails.getConfigXml())).withTest(Input.fromString(xmlText))
				     .checkForSimilar()
				     .ignoreWhitespace()
				     .build();

//			logger.debug(diff.toString()+" diff has differences? "+diff.hasDifferences());
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

				logger.info("User "+userDto.getUserName()+" modified the configuration of the station "+stationDetails.getStationDisplayName()+" "+M9kConstants.NEWLINE+strBufDiff.toString());
			}

			stationDetails.setConfigXml(xmlText);
			stationDetails.setTotalDfrsConfigured(currentSubstation.getDFRs().sizeOfDFRArray());
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
		public String saveAndNext() throws Exception
		  {
			  String returnValue = "Save";
			  save();
			  if (getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit"))
			  {
				  saveIntoDB();

			  }
			  logger.debug("DIGITAL: save in progress is booDigital? "+booDigital);
				if (stationDetails.isDfrAddedOrRemoved())
				{
					stationDetails.setDfrAddedOrRemoved(false);
				}

			  // START: 03-Mar-2021 - Implementation of virtual measurements for delta transformers 
			if (booDigital)
			  {
				logger.debug("\t\t\tlstTriggerChannels is null or empty "+((lstTriggerChannels!=null)?lstTriggerChannels.size():null));
				  returnValue = "Events";
			  }
			else if ((getLineGroupExists() != null && getLineGroupExists()) || (getLstLineGroups()!=null && !getLstLineGroups().isEmpty()))
			{
				returnValue = "DirectFaultLocation";
			}
			// END: 03-Mar-2021 - Implementation of virtual measurements for delta transformers
			  return returnValue;
			  
		  }
		
		@SuppressWarnings("unchecked")
		@SkipValidation
		public String back() {
			String returnValue="Back";
			  // For global assistance
			mapGlobalTriggerInputTypes = M9kUtils.convertListToMap(config.getList("allTriggerTypes"));
			lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
			logger.debug("List to be populated into triggers "+lstTriggerChannels);
			if (lstTriggerChannels == null || lstTriggerChannels.isEmpty())
			{
				session.put("sourceTab", "Measurements");
				returnValue = "BackToTriggersVia";
			}
			lstTriggerSourceDto = (List<TriggerSourceDTO>) session.get("lstTriggerSourceDto");
//			triggerOutHold = (String) session.get("triggerOutHold");

			logger.debug("Returning from virtual measurements " + returnValue);
			return returnValue;
		}

	  public String saveAndSend() throws Exception
	  {
		  String returnValue = "Send";
		  save();
		  saveIntoDB(); // To save into the database
		  logger.info("User "+userDto.getUserName()+" succesfully sent the saved configuration to station "+stationDetails.getStationDisplayName());
		  return returnValue;
	  }
	@SkipValidation
	  public String navigateToEvents()
	  {
//		  lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
		  logger.debug("In navigateToEvents "+((lstTriggerChannels != null)?lstTriggerChannels.size():"lstTriggerChannels is null "));
		  return "Events";
	  }
	  
	  // Populating Trigger
		private void populateVirtualMeasurementsIntoXml() throws Exception
		{
			logger.debug("Entered populateVirtualMeasurementsIntoXml..."+getLstVirtualMeasurements());
			Algorithms algorithms = null;
			// Construct exports
			Triggers triggers = null;
			Exports exports = null;
			Export export = null;
			PmuInputs pmuInputs = null;
			PmuInput pmuInput = null;
			Measurements measurements = null;
			Measurement measurement = null;
			Pmu localPmu = null;
			PmuInput localPmuInput;
			Limits limits = null;
			String limitsName;
			TriggerChannelDTO triggerChannelDTO;
			String exportName;
						
			// Initialize the trigger count to zero in case of new station creation
			if (getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit"))
			{
				stationDetails.setTotalVirtualMeasurementsConfigured(M9kConstants.VIRTUAL_MEASUREMENT_OFFSET);
			}

			VirtualMeasurementAlgorithm virtualMeasurementAlgorithm;
			for (int i = 0; i < getLstVirtualMeasurements().size(); i++) {
				triggerChannelDTO = getLstVirtualMeasurements().get(i);
				xmlConfigDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
				algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
				triggers = algorithms.getTriggers();
//				logger.debug("Triggers xml "+triggers.xmlText());
				logger.debug("triggerChannelDTO.getType() "+triggerChannelDTO.getType()+" Virtual logic "+triggerChannelDTO.getVirtualLogic());
				if (!triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.VIRTUAL_MEASUREMENT))
				{
					continue;
				}
				logger.debug("Delete any existing trigger channel before adding it to xml "+triggerChannelDTO);
				if (triggerChannelDTO.getMeasurementName() != null)
				{
					deleteVirtualMeasurementFromXML(triggerChannelDTO);
				}
				logger.debug("About to call populateMeasurement..."+triggerChannelDTO);
				virtualMeasurementAlgorithm = populateMeasurement(triggerChannelDTO);
				logger.debug("After populateMeasurement..." + virtualMeasurementAlgorithm);
				triggerChannelDTO.setUnits("");
				
				VirtualMeasurement virtualMeasurement = algorithms.addNewVirtualMeasurement();
				virtualMeasurement.setName(virtualMeasurementAlgorithm.getName());
				virtualMeasurement.setDescription(virtualMeasurementAlgorithm.getDescription());
				if (triggerChannelDTO.getAverage() != null)
				{
					virtualMeasurement.setAverage(triggerChannelDTO.getAverage());
				}		
				if (virtualMeasurementAlgorithm.getGlobalScale() != 0.0)
				{
					virtualMeasurement.setScale(virtualMeasurementAlgorithm.getGlobalScale());
				}
				int virtMeasurementInputIndex=0;
				com.usi.VirtualMeasurementInputDocument.VirtualMeasurementInput virtualMeasurementInputXml;
				VirtualMeasurementAlgorithm.VirtualMeasurementInput virtualMeasurementInput;
				logger.debug("Virtual measurements array "+ virtualMeasurementAlgorithm.getLstVirtualMeasurementInput());
				for (Iterator<VirtualMeasurementAlgorithm.VirtualMeasurementInput> iterator = virtualMeasurementAlgorithm.getLstVirtualMeasurementInput().iterator(); iterator.hasNext();) {
					 virtualMeasurementInput = iterator.next();
					logger.debug("Virtual measurements..."+virtualMeasurementInput);
					virtualMeasurementInputXml = virtualMeasurement.addNewVirtualMeasurementInput();
					// Name tag virtual measurement input as VMI
					virtualMeasurementInputXml.setName(++virtMeasurementInputIndex+"-"+M9kConstants.VIRTUAL_MEASUREMENT_INPUT_VMI);
					virtualMeasurementInputXml.setScale(virtualMeasurementInput.getScale());
					virtualMeasurementInputXml.setInput(virtualMeasurementInput.getInputXMLString());
				}
			
				// Set trigger object
				triggers.addNewTrigger();
				triggers.setTriggerArray(triggers.sizeOfTriggerArray()-1, virtualMeasurementAlgorithm.getTrigger());
				
				// Constructs Limits
				limitsName = virtualMeasurementAlgorithm.getLimitsAlgorithm().getName();
				limits = algorithms.addNewLimits();
				limits.setName(limitsName);
				limits.setInput(virtualMeasurementAlgorithm.getLimitsAlgorithm().getInputValue());
				if (virtualMeasurementAlgorithm.getLimitsAlgorithm().getHighLimit() != null && !virtualMeasurementAlgorithm.getLimitsAlgorithm().getHighLimit().isEmpty())
				{
					limits.setHighLimit(virtualMeasurementAlgorithm.getLimitsAlgorithm().getHighLimit());
				}
				if (virtualMeasurementAlgorithm.getLimitsAlgorithm().getLowLimit() != null && !virtualMeasurementAlgorithm.getLimitsAlgorithm().getLowLimit().isEmpty())
				{	
					limits.setLowLimit(virtualMeasurementAlgorithm.getLimitsAlgorithm().getLowLimit());
				}
				
				// START: 26-Feb-2020 - ROC positive and Negative Limits implementation
				if (virtualMeasurementAlgorithm.getLimitsAlgorithm().getDuration() != null 
						&& !virtualMeasurementAlgorithm.getLimitsAlgorithm().getDuration().isEmpty() 
						&& Integer.parseInt(virtualMeasurementAlgorithm.getLimitsAlgorithm().getDuration()) > 0)
				{
					limits.setDuration(virtualMeasurementAlgorithm.getLimitsAlgorithm().getDuration());
					limits.setRateOfChangeLimitNeg(virtualMeasurementAlgorithm.getLimitsAlgorithm().getRateOfChangeLimitNeg());
					limits.setRateOfChangeLimitPos(virtualMeasurementAlgorithm.getLimitsAlgorithm().getRateOfChangeLimitPos());
				}
							
				// Construct exports
//				logger.debug("Export status "+triggerChannelDTO.getExportStatus());
				exports = xmlConfigDfr.getDataPool().getExports();
				export = null;
//				exportName = triggerChannelDTO.getName()+"-"+triggerChannelDTO.getType()+"-"+triggerChannelDTO.getInputChannelName();
				exportName = triggerChannelDTO.getMeasurementName();
				if (exports == null)
				{
					exports = xmlConfigDfr.getDataPool().addNewExports();
					exports.setName("Exports");
				}
				else
				{
//					export = getExportsIfAlreadyExists(triggerChannelDTO);
				}
				if (export == null)
				{
					export = exports.addNewExport();
					export.setId(triggerChannelDTO.getId());
				}
				export.setName(exportName);
				export.setExportName(triggerChannelDTO.getName());
				export.setMeasurementType(triggerChannelDTO.getType());			
				export.setPhase(triggerChannelDTO.getPhase());
				export.setUnits(triggerChannelDTO.getUnits());
				export.setInput(triggerChannelDTO.getExportXMLInputString(virtualMeasurementAlgorithm));
				export.setSampleRate(stationDetails.getExportRate());
//				if (triggerChannelDTO.getExportRate() != null)
//				{
//					export.setSampleRate(triggerChannelDTO.getExportRate());
//				}
				export.setSampleRate(stationDetails.getPmuDataRate());
				if (triggerChannelDTO.getStatus() && triggerChannelDTO.isExportStatus())
				{
					export.setEnable(M9kConstants.ENABLE_ONE);
				}
				else
				{
					export.setEnable(M9kConstants.DISABLE_ZERO);
				}
				pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
				pmuInput = null;
				localPmu = xmlConfigDfr.getDataPool().getAlgorithms().getPmus().getPmuArray(0);
				localPmuInput = localPmu.addNewPmuInput();
				localPmuInput.setName(triggerChannelDTO.getMeasurementName());
				localPmuInput.setPmuInputName(triggerChannelDTO.getName());
				localPmuInput.setInput(virtualMeasurementAlgorithm.getInputXMLString().replace(M9kXMLConstants.XML_PHASOR_VALUE_INPUT, M9kXMLConstants.XML_VALUE_INPUT));
				localPmuInput.setPmuInputType(virtualMeasurementAlgorithm.getPmuInputType());
				if (pmuInputs == null)
				{	
					pmuInputs = currentSubstation.getPmus().getPmuArray(0).addNewPmuInputs();
				}
				else
				{
					pmuInput = getPmuIfAlreadyExists(pmuInputs, export.getId());
				}
				if (pmuInput == null)
				{
					pmuInput = pmuInputs.addNewPmuInput();
					pmuInput.setName(triggerChannelDTO.getMeasurementName());
					pmuInput.setId(export.getId());
				}
				pmuInput.setDfr(xmlConfigDfr.getSystem().getDfrId());

//				if(getFreqPmuId() != null && getFreqPmuId() == i)
				if (triggerChannelDTO.isFreqPmuStatus())
				{
					pmuInput.setPmuInputType(M9kConstants.PMU_INPUT_TYPE_FREQ);
					localPmuInput.setPmuInputType(M9kConstants.PMU_INPUT_TYPE_FREQ);
				}
				if (triggerChannelDTO.getStatus() && triggerChannelDTO.isPmuStatus())
				{
					pmuInput.setPmuEnable(M9kConstants.ENABLE_ONE);
					localPmuInput.setPmuEnable(M9kConstants.ENABLE_ONE);
//					localPmu.setPmuEnable(M9kConstants.ENABLE_ONE);
				}
				else
				{
					pmuInput.setPmuEnable(M9kConstants.DISABLE_ZERO);
					localPmuInput.setPmuEnable(M9kConstants.DISABLE_ZERO);
				}
				
				// Meta data storage for Measurement to know whether active or not
				measurement = null;
				measurements = currentSubstation.getMeasurements();
				if (measurements == null)
				{
					measurements = currentSubstation.addNewMeasurements();
				}
				else
				{
					measurement = getMeasurementIfAlreadyExists(measurements, export.getId());
				}
				if (measurement == null)
				{
					measurement = measurements.addNewMeasurement();
					measurement.setId(triggerChannelDTO.getId());
				}
				measurement.setName(triggerChannelDTO.getName());
				measurement.setDfr(xmlConfigDfr.getSystem().getDfrId());
//				logger.debug("Is Measurement enabled? "+triggerChannelDTO.getStatus());
				if (triggerChannelDTO.getStatus())
				{
					measurement.setStatus(M9kConstants.ENABLE);
				}
				else
				{
					measurement.setStatus(M9kConstants.DISABLE);
				}
				if (triggerChannelDTO.isPmuStatus())
				{
					measurement.setPmuStatus(M9kConstants.ENABLE);
				}
				else
				{
					measurement.setPmuStatus(M9kConstants.DISABLE);	
				}
				if (triggerChannelDTO.isExportStatus())
				{
					measurement.setExportStatus(M9kConstants.ENABLE);
				}
				else
				{
					measurement.setExportStatus(M9kConstants.DISABLE);
				}
				// START: 01-May-2018 All exports to be part of DDR instead of just RMS - ReleaseV1.0.8.2
				if (triggerChannelDTO.isDdrStatus())
				{
					measurement.setDdrStatus(M9kConstants.ENABLE);
				}
				else
				{
					measurement.setDdrStatus(M9kConstants.DISABLE);
				}
				// END: 01-May-2018
				
				// START: 30-Dec-2022 Disturbance Alarm implementation
				  if (triggerChannelDTO.isDisturbanceAlarm())
					{
						measurement.setDisturbanceAlarm(M9kConstants.ENABLE);
					}
					else
					{
						measurement.setDisturbanceAlarm(M9kConstants.DISABLE);
					}
				  // END: 30-Dec-2022
			}
			
		}
		
		/**
		 * Populate virtual measurements for the xml
		 * @return
		 */
		private List<TriggerChannelDTO> parseLstMeasurementsFromXml()
		{
			List<TriggerChannelDTO> lstMeasurements = new ArrayList<TriggerChannelDTO>();
			  Exports exports = null;
			  TriggerChannelDTO triggerChannelDto;
			  Measurements measurements = currentSubstation.getMeasurements();
			  Measurement[] arrMeasurement = null;
			  if (measurements != null)
			  {
				  arrMeasurement = measurements.getMeasurementArray();
			  }
			  
			  pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }

			  
			  for (int mCount = 0; mCount < arrMeasurement.length; mCount++) {
				  xmlConfigDfr = mapXMLDfrs.get("DFR"+arrMeasurement[mCount].getDfr());
				  XmlOptions xmlOptions = new XmlOptions(); 
					xmlOptions.setSaveOuter();
					xmlOptions.setSavePrettyPrint();
					xmlOptions.setUseDefaultNamespace();
					Map<String, String> prefixes = new HashMap<String, String>();
					prefixes.put("", "http://www.usi.com");
					xmlOptions.setSaveImplicitNamespaces(prefixes);
				  logger.debug("dfr xml "+xmlConfigDfr.xmlText(xmlOptions));
				  exports = xmlConfigDfr.getDataPool().getExports();
				  Export[] exportXml = exports.getExportArray();
				  logger.debug("Exports length "+exportXml.length);
				  for (int i = 0; i < exportXml.length; i++) {
					  logger.debug("exportXml[i].getMeasurementType() "+exportXml[i].getMeasurementType()+" arrMeasurement[mCount].getId() "+arrMeasurement[mCount].getId());
					  if (exportXml[i].getMeasurementType().equalsIgnoreCase(M9kConstants.VIRTUAL_MEASUREMENT) && exportXml[i].getId() == arrMeasurement[mCount].getId())
					  {
						  triggerChannelDto = parseAndBuildMeasurementsFromXml(exportXml[i]); 
						  triggerChannelDto.setName(arrMeasurement[mCount].getName());
							// Measurement Status
							if (arrMeasurement[mCount].getStatus().equalsIgnoreCase(M9kConstants.ENABLE))
							{
								triggerChannelDto.setStatus(true);
							}
							else
							{
								triggerChannelDto.setStatus(false);
							}
							
							// PMU Status
							if (arrMeasurement[mCount].getPmuStatus().equalsIgnoreCase(M9kConstants.ENABLE))
							{
								triggerChannelDto.setPmuStatus(true);
							}
							else
							{
								triggerChannelDto.setPmuStatus(false);
							}
							
							// START: 01-May-2018 All exports to be part of DDR instead of just RMS - ReleaseV1.0.8.2
							if (triggerChannelDto.isDdrStatus() || arrMeasurement[mCount].getDdrStatus() != null && arrMeasurement[mCount].getDdrStatus().equalsIgnoreCase(M9kConstants.ENABLE))
							{
								triggerChannelDto.setDdrStatus(true);
							}
							else
							{
								triggerChannelDto.setDdrStatus(false);
							}
							// END: 01-May-2018

							// START: 30-Dec-2022 Disturbance Alarm implementation
							if (triggerChannelDto.isDisturbanceAlarm() || arrMeasurement[mCount].getDisturbanceAlarm() != null && arrMeasurement[mCount].getDisturbanceAlarm().equalsIgnoreCase(M9kConstants.ENABLE))
							{
								triggerChannelDto.setDisturbanceAlarm(true);
							}
							else
							{
								triggerChannelDto.setDisturbanceAlarm(false);
							}
							// END: 30-Dec-2022

							// Export Status
							if (arrMeasurement[mCount].getExportStatus().equalsIgnoreCase(M9kConstants.ENABLE))
							{
								triggerChannelDto.setExportStatus(true);
							}
							else
							{
								triggerChannelDto.setExportStatus(false);
							}
							triggerChannelDto.setChassis("DFR"+xmlConfigDfr.getSystem().getDfrId());
							lstMeasurements.add(triggerChannelDto);
						  break;
					  }
				  }

			  }
			  Collections.sort(lstMeasurements, new Comparator<TriggerChannelDTO>() {

					@Override
					public int compare(TriggerChannelDTO o1, TriggerChannelDTO o2) {
//						return ((o1.getId() < o2.getId())?0:1);
						return ((o1.getId() < o2.getId())? -1 : (o1.getId() == o2.getId())?0:1);
					}
				});
			  return lstMeasurements;
		}


		/**
		 * parse and build objects from the exports
		 * @param exportXml
		 * @return
		 */
		  private TriggerChannelDTO parseAndBuildMeasurementsFromXml(Export exportXml)
		  {
			  TriggerChannelDTO triggerChannelDto = null;
			  Trigger[] trigger = xmlConfigDfr.getDataPool().getAlgorithms().getTriggers().getTriggerArray();;
			  for (int i = 0; i < trigger.length; i++) {
					if (trigger[i].getChanId() == exportXml.getId() && (trigger[i].getType() == null || trigger[i].getType().equalsIgnoreCase(M9kConstants.VIRTUAL_MEASUREMENT)))
					{
						triggerChannelDto = constructTriggerFromXml(trigger[i]);
						  triggerChannelDto.setPhase(exportXml.getPhase());
					}
				  }
			  
			  return triggerChannelDto;
		  }

		  /**
		   * 
		   * @param triggerXml
		   * @return
		   */
		  private TriggerChannelDTO constructTriggerFromXml(Trigger triggerXml)
		  {
//			  logger.debug("\t\t\t\tDEBUG: Enteredd constructTriggerFromXml...");
			  TriggerChannelDTO triggerChannelDto = new TriggerChannelDTO();
			  String inputValue = triggerXml.getInput();
			  String algorithmName = "";
			  String limitsName = "";
			  String xmlAlgoType;
			  Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
			  Limits limits[] = algorithms.getLimitsArray();
			  triggerChannelDto.setId(triggerXml.getChanId());
//			  triggerChannelDto.setName(triggerXml.getName().substring(triggerXml.getName().indexOf("T_")+2));
			  triggerChannelDto.setName(triggerXml.getName());
//			  logger.debug("TriggerXml phase...."+triggerXml.getPhase());
//			  triggerChannelDto.setPhase(triggerXml.getPhase());
			  triggerChannelDto.setChatterLimit(""+triggerXml.getChatterLimit());
			  triggerChannelDto.setChatterRate(""+triggerXml.getChatterRate());
			  triggerChannelDto.setTriggerLimit(""+triggerXml.getTriggerLimit());
			  triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_MEASUREMENT);
			  triggerChannelDto.setType(M9kConstants.VIRTUAL_MEASUREMENT);
			  triggerChannelDto.setExportStatus(false);
			  if (triggerXml.getEnable() == M9kConstants.ENABLE_ONE)
			  {
				  triggerChannelDto.setTriggerStatus(true);
			  }
			  else
			  {
				  triggerChannelDto.setTriggerStatus(false);
			  }

//			  logger.debug("\t\t\t\tDEBUG: Input value of the trigger..."+inputValue);
			  int index = inputValue.indexOf("Limits(");
			  if (index != -1)
			  {
				  limitsName = inputValue.substring(index+7,inputValue.lastIndexOf(")"));
//				  logger.debug("\t\t\t\tDEBUG: Limits name: "+limitsName);
				  for (int i = 0; i < limits.length; i++) {
					if (limits[i].getName().equalsIgnoreCase(limitsName))
					{
						triggerChannelDto.setTriggerStatus(true);
						// START: 25-Feb-2020 - ROC positive and Negative Limits implementation
						// ROC settings
//						if(limits[i].getDuration() != null && limits[i].getRateOfChangeLimit() != null)
						if(limits[i].getDuration() != null && !limits[i].getDuration().isEmpty() && !limits[i].getDuration().equals("0"))
						{
							 // START 29-July-2015: ROC implementation  
//							  triggerChannelDto.setDisableTripIfGt(false);
							triggerChannelDto.setDisableTripRoc(false);
							  triggerChannelDto.setDisableDuration(false);
								triggerChannelDto.setDuration(limits[i].getDuration());
							  // END 29-July-2015: ROC implementation
								
								// Old version compatibility - Set the single ROC limit to both positive and negative with sign 
								if (limits[i].getRateOfChangeLimit() != null)
								{
									triggerChannelDto.setTripRocNeg("-"+limits[i].getRateOfChangeLimit()); // negative value
									triggerChannelDto.setTripRocPos(limits[i].getRateOfChangeLimit());									
								}
								else
								{
									triggerChannelDto.setTripRocNeg(limits[i].getRateOfChangeLimitNeg());
									triggerChannelDto.setTripRocPos(limits[i].getRateOfChangeLimitPos());																		
								}

						}
						else
						{
							triggerChannelDto.setDisableTripRoc(true);
//							triggerChannelDto.setDisableDuration(true);
						}
						//END: 25-Feb-2020

						if (limits[i].getHighLimit() != null && limits[i].getLowLimit() != null)
						{
							logger.debug("\t\t\t\tDEBUG: Setting to BOTH");
							triggerChannelDto.setStart(M9kConstants.BOTH);
							triggerChannelDto.setTripIfOver(""+limits[i].getHighLimit());
							triggerChannelDto.setTripIfUnder(""+limits[i].getLowLimit());
							triggerChannelDto.setDisableTripOver(false);
							triggerChannelDto.setDisableTripUnder(false);
						}
						else if (limits[i].getHighLimit() != null)
						{
//							logger.debug("\t\t\t\tDEBUG: Setting to OVER");
							triggerChannelDto.setStart(M9kConstants.OVER);
							triggerChannelDto.setTripIfOver(""+limits[i].getHighLimit());						
							triggerChannelDto.setDisableTripOver(false);
							triggerChannelDto.setDisableTripUnder(true);
						}
						else if (limits[i].getLowLimit() != null)
						{
//							logger.debug("\t\t\t\tDEBUG: Setting to UNDER");
							triggerChannelDto.setStart(M9kConstants.UNDER);
							triggerChannelDto.setTripIfUnder(""+limits[i].getLowLimit());	
							triggerChannelDto.setDisableTripOver(true);
							triggerChannelDto.setDisableTripUnder(false);						
						}
						else
						{
							// If duration and rocLLimit(deprecated) or rocNeg and RocPos exists then enable ROC 
							if(limits[i].getDuration() != null && ((limits[i].getRateOfChangeLimitNeg() != null && limits[i].getRateOfChangeLimitPos() != null) || limits[i].getRateOfChangeLimit() != null))
							{
								logger.debug("\t\t\t\tDEBUG: Setting to ROC");
								 // START 29-July-2015: ROC implementation  
								  triggerChannelDto.setStart(M9kConstants.ROC);
									
									triggerChannelDto.setDisableTripOver(true);
									triggerChannelDto.setDisableTripUnder(true);

								  // END 29-July-2015: ROC implementation
							}
							else
							{
//								logger.debug("\t\t\t\tDEBUG: Setting to NEVER");
								triggerChannelDto.setStart(M9kConstants.NEVER);
								triggerChannelDto.setDisableTripOver(true);
								triggerChannelDto.setDisableTripUnder(true);
								  // START 29-July-2015: ROC implementation  
								  triggerChannelDto.setDisableTripRoc(true);
								  triggerChannelDto.setDisableDuration(true);
								  // END 29-July-2015: ROC implementation
								triggerChannelDto.setTriggerStatus(false);
							}
						}					
//						logger.debug("Trigger if under from XML..."+limits[i].getLowLimit());	
						inputValue = limits[i].getInput();
						index = inputValue.indexOf("/")+1;
						xmlAlgoType = inputValue.substring(index, inputValue.indexOf("/", index));
						algorithmName = xmlAlgoType.substring(xmlAlgoType.lastIndexOf("(")+1, xmlAlgoType.indexOf(")"));
//						logger.debug("Input object of the Limit..."+xmlAlgoType);
//						logger.debug("Type of trigger...."+xmlAlgoType.substring(0, xmlAlgoType.indexOf("(")));
//						logger.debug("Type of trigger...."+xmlAlgoType.substring(0, xmlAlgoType.indexOf("(")));
						triggerChannelDto.setType(xmlAlgoType.substring(0, xmlAlgoType.indexOf("(")));
						
						if (xmlAlgoType.startsWith(M9kConstants.VIRTUAL_MEASUREMENT))
						{
							constructVirtualMeasurementFromXml(algorithmName, triggerChannelDto);
						}

						break;
					}
				}
				  
			  }
			  
			  logger.debug("\n\n\t\t TriggerChannelDTO Constructed..."+triggerChannelDto+"\n\n\n\n");
			  return triggerChannelDto;
		  }
			  
		/**
		 * START: 09-Mar-2020 - Implementing virtual measurements for delta transformers
		 * @param virtualMeasurementName
		 * @param triggerChannelDto
		 */
		private void constructVirtualMeasurementFromXml(String virtualMeasurementName, TriggerChannelDTO triggerChannelDto)
		{
			String inputValue = "";
			int startIndex;
			
			  Exports exports = xmlConfigDfr.getDataPool().getExports();
			  Export export[] = null;
			  if (exports != null)
			  {
				  export = exports.getExportArray();
			  }
			  PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			VirtualMeasurement[] arrVirtualMeasurementXml = xmlConfigDfr.getDataPool().getAlgorithms().getVirtualMeasurementArray();
			com.usi.VirtualMeasurementInputDocument.VirtualMeasurementInput[] arrVirtualMeasurementInputXml;
			triggerChannelDto.setDisableHarmonic(true);
			VirtualMeasurementAlgorithm virtualMeasurementAlgorithm;
			VirtualMeasurementAlgorithm.VirtualMeasurementInput virtualMeasurementInput;
			TriggerChannelDTO inputForVirtualMeasurement;
			
			for (int j = 0; j < arrVirtualMeasurementXml.length; j++) {
				if (arrVirtualMeasurementXml[j].getName().equals(virtualMeasurementName))
				{
					if (arrVirtualMeasurementXml[j].getAverage() > 0)
					{
						triggerChannelDto.setAverage(arrVirtualMeasurementXml[j].getAverage());
					}
					for (int j2 = 0; j2 < arrVirtualMeasurementXml.length; j2++) {
						virtualMeasurementAlgorithm = new VirtualMeasurementAlgorithm();
						if (arrVirtualMeasurementXml[j2].getScale() != 0)
						{
							virtualMeasurementAlgorithm.setGlobalScale(arrVirtualMeasurementXml[j2].getScale());
						}
						if (arrVirtualMeasurementXml[j2].getName().equals(virtualMeasurementName))
						{
							arrVirtualMeasurementInputXml = arrVirtualMeasurementXml[j2].getVirtualMeasurementInputArray();
							for (int i = 0; i < arrVirtualMeasurementInputXml.length; i++) {
								inputForVirtualMeasurement = getInputMeasurement(arrVirtualMeasurementInputXml[i].getInput());
								if (inputForVirtualMeasurement != null)
								{
									virtualMeasurementInput =  virtualMeasurementAlgorithm.new VirtualMeasurementInput(inputForVirtualMeasurement,arrVirtualMeasurementInputXml[i].getScale());
									virtualMeasurementAlgorithm.getLstVirtualMeasurementInput().add(virtualMeasurementInput);
								}
							}
							triggerChannelDto.setVirtualLogic(virtualMeasurementAlgorithm.getVirtualLogic());
							triggerChannelDto.setChannel("");
							break;
						}
					}
					 
					for (int exportCnt=0; (export!=null&&exportCnt < export.length);exportCnt++)
					{
						inputValue = export[exportCnt].getInput();
						startIndex = inputValue.indexOf("Average(");
						if (startIndex == -1)
						{
							continue;
						}
						else
						{
							startIndex+=8;
						}
					}
					break;
				}
			}

		}
		private TriggerChannelDTO getInputMeasurement(String input) {
			TriggerChannelDTO matchedTriggerObject = null;
			// Parsing strings similar to @Algorithms(Algorithms)/Power(29-LG_1)/_Object
			String triggerChannelName = input.substring(input.indexOf("/"),input.lastIndexOf("/"));
			String algorithmType = triggerChannelName.substring(0, triggerChannelName.indexOf("(")); 
			triggerChannelName = triggerChannelName.substring(triggerChannelName.indexOf("(")+1, triggerChannelName.indexOf(")"));
			for (Iterator<TriggerChannelDTO> iterator = getLstTriggerChannels().iterator(); iterator.hasNext();) {
				matchedTriggerObject = iterator.next();
				if (matchedTriggerObject.getType().equalsIgnoreCase(algorithmType) && matchedTriggerObject.getName().equals(triggerChannelName))
				{
					return matchedTriggerObject;
				}
			}
			return null;
		}

		// END: 28-Jan-2020

		private PmuInput getPmuIfAlreadyExists(PmuInputs pmuInputs, Integer exportId)
		{
//			logger.debug("Export Id to be compared "+exportId);
			PmuInput pmuInput = null;
			PmuInput[] arrPmuInput = pmuInputs.getPmuInputArray();
			for (int i = 0; i < arrPmuInput.length; i++) {
//				logger.debug("arrExport[i].getName() "+arrPmuInput[i].getName());
				if (arrPmuInput[i].getId() == exportId)
				{
					pmuInput = arrPmuInput[i];
					if (pmuInput.getPmuInputType() != null && pmuInput.getPmuInputType().equalsIgnoreCase("Freq"))
					{
//						logger.debug("Unsetting PMU inputt type for id "+exportId);
						pmuInput.unsetPmuInputType();
					}
					break;
				}
			}
			return pmuInput;
		}
		private Measurement getMeasurementIfAlreadyExists(Measurements measurements, Integer meaurementId)
		{
//			logger.debug("meaurement Id to be compared "+meaurementId);
			Measurement measurement = null;
			Measurement[] arrMeasurement = measurements.getMeasurementArray();
			for (int i = 0; i < arrMeasurement.length; i++) {
//				logger.debug("arrExport[i].getName() "+arrMeasurement[i].getName());
				if (arrMeasurement[i].getId() == meaurementId)
				{
					measurement = arrMeasurement[i];
					break;
				}
			}
			return measurement;
		}
		
		private VirtualMeasurementAlgorithm populateMeasurement(TriggerChannelDTO triggerChannelDTO)
		{
			logger.debug("\t\t\t\tDEBUG: Inside populate measurement  "+triggerChannelDTO);
			Integer triggerId;
			if ((getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit")) || (triggerChannelDTO == null || triggerChannelDTO.getId() == null || triggerChannelDTO.getId().equals(0) || triggerChannelDTO.getId() == -1))
			{
				triggerId = StationDTO.getNextAvailableVirtualMeasurementId();
				triggerChannelDTO.setId(triggerId);
			}
			else
			{
				triggerId = triggerChannelDTO.getId();
			}
			triggerChannelDTO.setMeasurementName(triggerId+"-VirtualMeasurement");

			VirtualMeasurementAlgorithm virtualMeasurementAlgorithm = null;
			virtualMeasurementAlgorithm = new VirtualMeasurementAlgorithm(M9kUtils.getDfrsSpecificMeasurements(triggerChannelDTO.getChassis(), getLstTriggerChannels()));
			virtualMeasurementAlgorithm.setName(triggerChannelDTO.getMeasurementName());
			virtualMeasurementAlgorithm.setDescription(triggerChannelDTO.getName());
			virtualMeasurementAlgorithm.setVirtualLogic(triggerChannelDTO.getVirtualLogic());
			virtualMeasurementAlgorithm.setInputValue( triggerChannelDTO.getTriggerChannelInput());
			Trigger trigger = TriggerDocument.Factory.newInstance().addNewTrigger();
			trigger.setChanId(triggerId);
//			trigger.setName("T_"+triggerChannelDTO.getName());
			trigger.setName(triggerChannelDTO.getMeasurementName());
			trigger.setTriggerName(triggerChannelDTO.getName());
			LimitsAlgorithm limits = new LimitsAlgorithm();
			limits.setName(triggerChannelDTO.getMeasurementName());
			limits.setInputValue(virtualMeasurementAlgorithm.getInputXMLString());

			if (triggerChannelDTO.getStart() != null && !triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.NEVER))
			{
				if (triggerChannelDTO.getTripIfOver() != null && (triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.BOTH) || triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.OVER)))
				{
					limits.setHighLimit(triggerChannelDTO.getTripIfOver());
				}
				if (triggerChannelDTO.getTripIfUnder() != null && (triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.BOTH) || triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.UNDER)))
				{
					limits.setLowLimit(triggerChannelDTO.getTripIfUnder());
				}
				if (triggerChannelDTO.getDuration() != null && 
						!triggerChannelDTO.getDuration().isEmpty() &&
						!triggerChannelDTO.getDuration().equalsIgnoreCase("0"))
				{
					limits.setDuration(triggerChannelDTO.getDuration());
					limits.setRateOfChangeLimitNeg(triggerChannelDTO.getTripRocNeg());
					limits.setRateOfChangeLimitPos(triggerChannelDTO.getTripRocPos());
				}
				
				logger.debug("Phase to be set in XML... "+triggerChannelDTO.getPhase()+" for channel "+triggerChannelDTO.getDisplayName());
				trigger.setPhase(triggerChannelDTO.getPhase());
				if (triggerChannelDTO.getChatterLimit() != null && !triggerChannelDTO.getChatterLimit().isEmpty())
				{
					trigger.setChatterLimit(Integer.parseInt(triggerChannelDTO.getChatterLimit()));
				}
				if (triggerChannelDTO.getChatterRate() != null && !triggerChannelDTO.getChatterRate().isEmpty())
				{
					trigger.setChatterRate(Double.parseDouble(triggerChannelDTO.getChatterRate()));
				}
				if (triggerChannelDTO.getTriggerLimit() != null && !triggerChannelDTO.getTriggerLimit().isEmpty())
				{
					trigger.setTriggerLimit(Integer.parseInt(triggerChannelDTO.getTriggerLimit()));
				}
				if (triggerChannelDTO.getStatus())
				{
					trigger.setEnable(M9kConstants.ENABLE_ONE);
				}
				else
				{
					trigger.setEnable(M9kConstants.DISABLE_ZERO);
				}
				
			}
			else
			{
				trigger.setEnable(M9kConstants.DISABLE_ZERO);
			}

			virtualMeasurementAlgorithm.setLimitsAlgorithm(limits);
			trigger.setInput(limits.getLimitsInput());

			virtualMeasurementAlgorithm.setTrigger(trigger);
			
			if (triggerChannelDTO.getAverage() != null)
			{
				virtualMeasurementAlgorithm.setAverage(triggerChannelDTO.getAverage());
			}
			
			return virtualMeasurementAlgorithm;
		}
	  
		/**
		 * 
		 */
		@SkipValidation
		public String deleteVirtualMeasurement() {
			logger.debug("Entered deleteVirtualMeasurement selected index to delete "+getSelectedVirtualIndex());
			if (!deleteVirtualMeasurementFromXML(getLstVirtualMeasurements().get(getSelectedVirtualIndex())))
			{
				// New virtual measurement deleted, reuse the id
				if (StationDTO.getCurrentVirtualMeasurementId() > M9kConstants.VIRTUAL_MEASUREMENT_OFFSET)
				{
					StationDTO.decrementVirtualMeasurementId();
				}
			}
			getLstVirtualMeasurements().remove(getSelectedVirtualIndex());
			session.put("VIRTUAL_MEASUREMENTS_LIST", getLstVirtualMeasurements());

		return SUCCESS;	
		}

		
		/**
		 * 06-Feb-2020 - Delete virtual channel from xml
		 * @param triggerChannelDTO
		 */
		private boolean deleteVirtualMeasurementFromXML(TriggerChannelDTO virtualMeasurementToDelete)
		{
			boolean deleteStatus = false;
			logger.debug("About to delete the selected virtual measurement "+virtualMeasurementToDelete);
			DFR xmlDfr = mapXMLDfrs.get(virtualMeasurementToDelete.getChassis().trim());
			
			// Delete measurement xml 
			List<Integer> lstIndicesToDelete = new ArrayList<Integer>();

			Measurements measurements = currentSubstation.getMeasurements();
			if (measurements != null)
			{
				Measurement[] arrMeasurement = measurements.getMeasurementArray();
				if (arrMeasurement != null)
				{
					for (int i = 0; i < arrMeasurement.length; i++) {
						if (arrMeasurement[i].getId() == virtualMeasurementToDelete.getId().intValue())
						{
							logger.debug("About to delete measurements "+arrMeasurement[i].getId());
							lstIndicesToDelete.add(i);
		//					measurements.removeMeasurement(i);
		//					arrMeasurement = measurements.getMeasurementArray();
		//					i=0;
							deleteStatus = true;
						}
					}
					for (Iterator<Integer> iterator = lstIndicesToDelete.iterator(); iterator.hasNext();) {
						measurements.removeMeasurement(iterator.next().intValue());
					}
				}
			}
			lstIndicesToDelete.clear();
			// Delete PMUs xml
			PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
			if (pmuInputs != null)
			{
				PmuInput[] arrPmuInput = pmuInputs.getPmuInputArray();
				if (arrPmuInput != null)
				{
					for (int i = 0; i < arrPmuInput.length; i++) {
						if (arrPmuInput[i].getId() == virtualMeasurementToDelete.getId().intValue())
						{
							logger.debug("About to delete pmu..."+arrPmuInput[i].getId());
							lstIndicesToDelete.add(i);
		//					pmuInputs.removePmuInput(i);
		//					arrPmuInput = pmuInputs.getPmuInputArray();
		//					i=0;
							deleteStatus = true;
						}
					}
					for (Iterator<Integer> iterator = lstIndicesToDelete.iterator(); iterator.hasNext();) {
						pmuInputs.removePmuInput(iterator.next().intValue());
					}
				}
			}			
			lstIndicesToDelete.clear();
			
			Algorithms xmlAlgorithm = xmlDfr.getDataPool().getAlgorithms();

			// Delete PMUs xml from algorithm
			Pmu localPmu = xmlAlgorithm.getPmus().getPmuArray(0);
			if (localPmu != null)
			{
				PmuInput[] arrPmuInput = localPmu.getPmuInputArray();
				if (arrPmuInput != null)
				{
					for (int i = 0; i < arrPmuInput.length; i++) {
						if (arrPmuInput[i].getName() != null && arrPmuInput[i].getName().equalsIgnoreCase(virtualMeasurementToDelete.getMeasurementName()))
						{
							logger.debug("About to delete algorithm pmu ..."+arrPmuInput[i].getId());
							lstIndicesToDelete.add(i);
		//								pmuInputs.removePmuInput(i);
		//								arrPmuInput = pmuInputs.getPmuInputArray();
		//								i=0;
							deleteStatus = true;
						}
					}
					for (Iterator<Integer> iterator = lstIndicesToDelete.iterator(); iterator.hasNext();) {
						localPmu.removePmuInput(iterator.next().intValue());
					}
				}
			}
			lstIndicesToDelete.clear();
			VirtualMeasurement[] arrVirtualMeasurementXml = xmlAlgorithm.getVirtualMeasurementArray();
			if (arrVirtualMeasurementXml != null)
			{
				for (int i = 0; i < arrVirtualMeasurementXml.length; i++) {
					logger.debug("arrVirtualMeasurementXml[i].getName() "+arrVirtualMeasurementXml[i].getName()+" virtualMeasurementToDelete.getName() "+virtualMeasurementToDelete.getName());
					logger.debug("Is it matching? "+(arrVirtualMeasurementXml[i].getName().equalsIgnoreCase(virtualMeasurementToDelete.getName())));
					if (arrVirtualMeasurementXml[i].getName() != null && arrVirtualMeasurementXml[i].getName().equalsIgnoreCase(virtualMeasurementToDelete.getMeasurementName()))
					{
						logger.debug("about to delete virtual measurement "+arrVirtualMeasurementXml[i].getName());
						lstIndicesToDelete.add(i);
	//					xmlAlgorithm.removeVirtualMeasurement(i);
						deleteStatus = true;
					}
				}
				for (Iterator<Integer> iterator = lstIndicesToDelete.iterator(); iterator.hasNext();) {
					xmlAlgorithm.removeVirtualMeasurement(iterator.next().intValue());
				}
			}
			
			// Remove Trigger
			Triggers xmlTriggers = xmlAlgorithm.getTriggers();
			if (xmlTriggers != null)
			{
				Trigger[] xmlTrigger = xmlTriggers.getTriggerArray();
				if (xmlTrigger != null)
				{
					List<TriggerChannelDTO> lstTriggersToDelete = new ArrayList<TriggerChannelDTO>(xmlTrigger.length);
					logger.debug("Delete virtual measurement XML trigger length "+xmlTrigger.length);
					for (int i = 0; i < xmlTrigger.length; i++) {
						logger.debug("Xml trigger obj "+xmlTrigger[i].xmlText()+" xmlTrigger[i].getName() "+xmlTrigger[i].getName());
						if (xmlTrigger[i].getName() != null &&  xmlTrigger[i].getName().endsWith(virtualMeasurementToDelete.getMeasurementName()))
						{
							logger.debug("about to delete Trigger "+xmlTrigger[i].getName());
							TriggerChannelDTO triggerChannelToDelete = new TriggerChannelDTO();
							triggerChannelToDelete.setId(xmlTrigger[i].getChanId());
							triggerChannelToDelete.setChassis(virtualMeasurementToDelete.getChassis());
							triggerChannelToDelete.setMeasurementName(xmlTrigger[i].getName());
							logger.debug("Trigger to delete "+triggerChannelToDelete);
							lstTriggersToDelete.add(triggerChannelToDelete);
	//						M9kXMLUtils.deleteMeasurement(currentSubstation, triggerChannelToDelete, mapXMLDfrs);
							deleteStatus = true;
						}
					}
					for (Iterator<TriggerChannelDTO> iterator = lstTriggersToDelete.iterator(); iterator.hasNext();) {
						TriggerChannelDTO triggerChannelToDelete = iterator.next();
						M9kXMLUtils.deleteMeasurement(currentSubstation, triggerChannelToDelete, mapXMLDfrs);
					}
				}
			}
			
			// Remove exports
			lstIndicesToDelete.clear();
			Exports exports = xmlDfr.getDataPool().getExports();
			if (exports != null)
			{
				Export[] arrExport = exports .getExportArray();
				if (arrExport != null)
				{
					for (int i = 0; i < arrExport.length; i++) {
						if (arrExport[i].getName() != null && arrExport[i].getName().endsWith(virtualMeasurementToDelete.getMeasurementName()))
						{
							logger.debug("About to delete exports "+arrExport[i].getName());
							lstIndicesToDelete.add(i);
		//					exports.removeExport(i);
							deleteStatus = true;
						}
					}
					for (Iterator<Integer> iterator = lstIndicesToDelete.iterator(); iterator.hasNext();) {
						exports.removeExport(iterator.next().intValue());
					}
				}
			}
			return deleteStatus;		
		}
		
	@SuppressWarnings("unchecked")
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		stationDetails = (StationDTO) session.get("stationDetails");
		  lstDfrDTO = (List<DfrDTO>) session.get("DFRsList");
		 config = (PropertiesConfiguration) session.get("config");
		 accessMode = (String) session.get("accessMode");
		 currentSubstation = (SubStation) session.get("selectedSubstation");
		 mapXMLDfrs = (Map<String, DFR>) session.get("mapXMLDfrs");
		 userDto = (UsersDTO) session.get("userDetails");
		 lstVirtualChannels = (List<AnalogChannelDTO>) session.get("lstVirtualChannels");
		 
		lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
		lstVirtualMeasurements = (List<TriggerChannelDTO>)session.get("VIRTUAL_MEASUREMENTS_LIST");
		
		lineGroupExists = (Boolean)session.get("lineGroupExists");
		booAnalog = (Boolean)session.get("booAnalog");
		booDigital = (Boolean)session.get("booDigital");
		lstLineGroups = (List<LineGroupsAlgorithm>) session.get("lstLineGroups");

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
	 * @return the analogChannelsCount
	 */
	public String getAnalogChannelsCount() {
		return analogChannelsCount;
	}

	/**
	 * @param analogChannelsCount the analogChannelsCount to set
	 */
	public void setAnalogChannelsCount(String analogChannelsCount) {
		this.analogChannelsCount = analogChannelsCount;
	}

	/**
	 * @return the digitalChannelsCount
	 */
	public String getDigitalChannelsCount() {
		return digitalChannelsCount;
	}

	/**
	 * @param digitalChannelsCount the digitalChannelsCount to set
	 */
	public void setDigitalChannelsCount(String digitalChannelsCount) {
		this.digitalChannelsCount = digitalChannelsCount;
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
	 * @return the lstAnalog
	 */
	public List<Map<String, Integer>>  getLstAnalog() {
		return lstAnalog;
	}

	/**
	 * @param lstAnalog the lstAnalog to set
	 */
	public void setLstAnalog(List<Map<String, Integer>>  lstAnalog) {
		this.lstAnalog = lstAnalog;
	}

	/**
	 * @return the lstDigital
	 */
	public List<Map<String, Integer>>  getLstDigital() {
		return lstDigital;
	}

	/**
	 * @param lstDigital the lstDigital to set
	 */
	public void setLstDigital(List<Map<String, Integer>>  lstDigital) {
		this.lstDigital = lstDigital;
	}

	/**
	 * @return the analogCnt
	 */
	public Map<String, Integer> getAnalogCnt() {
		return analogCnt;
	}

	/**
	 * @param analogCnt the analogCnt to set
	 */
	public void setAnalogCnt(Map<String, Integer> analogCnt) {
		this.analogCnt = analogCnt;
	}

	/**
	 * @return the eventCnt
	 */
	public Map<String, Integer> getDigitalCnt() {
		return digitalCnt;
	}

	/**
	 * @param eventCnt the eventCnt to set
	 */
	public void setDigitalCnt(Map<String, Integer> digitalCnt) {
		this.digitalCnt = digitalCnt;
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
	 * @return the selectedDfr
	 */
	public String getSelectedDfr() {
		return selectedDfr;
	}

	/**
	 * @param selectedDfr the selectedDfr to set
	 */
	public void setSelectedDfr(String selectedDfr) {
		this.selectedDfr = selectedDfr;
	}

	/**
	 * @return the analogChannelCounter
	 */
	public Counter getAnalogChannelCounter() {
		return analogChannelCounter;
	}

	/**
	 * @param analogChannelCounter the analogChannelCounter to set
	 */
	public void setAnalogChannelCounter(Counter analogChannelCounter) {
		this.analogChannelCounter = analogChannelCounter;
	}

	/**
	 * @return the digitalChannelCounter
	 */
	public Counter getDigitalChannelCounter() {
		return digitalChannelCounter;
	}

	/**
	 * @param digitalChannelCounter the digitalChannelCounter to set
	 */
	public void setDigitalChannelCounter(Counter digitalChannelCounter) {
		this.digitalChannelCounter = digitalChannelCounter;
	}

	/**
	 * @return the triggerChannelCounter
	 */
	public Counter getTriggerChannelCounter() {
		return triggerChannelCounter;
	}

	/**
	 * @param triggerChannelCounter the triggerChannelCounter to set
	 */
	public void setTriggerChannelCounter(Counter triggerChannelCounter) {
		this.triggerChannelCounter = triggerChannelCounter;
	}

	/**
	 * @return the eventInput
	 */
	public EventInput[] getDigitals() {
		return digitals;
	}

	/**
	 * @param eventInput the eventInput to set
	 */
	public void setDigitals(EventInput[] digitals) {
		this.digitals = digitals;
	}

	/**
	 * @return the lstTriggerChannels
	 */
	public List<TriggerChannelDTO> getLstTriggerChannels() {
		return lstTriggerChannels;
	}

	/**
	 * @param lstTriggerChannels the lstTriggerChannels to set
	 */
	public void setLstTriggerChannels(List<TriggerChannelDTO> lstTriggerChannels) {
		this.lstTriggerChannels = lstTriggerChannels;
	}

	/**
	 * @return the selectedIndex
	 */
	public String getSelectedIndex() {
		return selectedIndex;
	}

	/**
	 * @param selectedIndex the selectedIndex to set
	 */
	public void setSelectedIndex(String selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	/**
	 * @return the reqdIndex
	 */
	public int getReqdIndex() {
		return reqdIndex;
	}

	/**
	 * @param reqdIndex the reqdIndex to set
	 */
	public void setReqdIndex(int reqdIndex) {
		this.reqdIndex = reqdIndex;
	}

	/**
	 * @return the json
	 */
	public Map<String, String> getJson() {
		return json;
	}

	/**
	 * @param json the json to set
	 */
	public void setJson(Map<String, String> json) {
		this.json = json;
	}

	/**
	 * @return the lstTriggerChannelsHidden
	 */
	public List<TriggerChannelDTO> getLstTriggerChannelsHidden() {
		return lstTriggerChannelsHidden;
	}

	/**
	 * @param lstTriggerChannelsHidden the lstTriggerChannelsHidden to set
	 */
	public void setLstTriggerChannelsHidden(
			List<TriggerChannelDTO> lstTriggerChannelsHidden) {
		this.lstTriggerChannelsHidden = lstTriggerChannelsHidden;
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

	public void validate()
	{
		 try{
		if (getLstVirtualMeasurements() != null && !getLstVirtualMeasurements().isEmpty())
		{
				
			int iTriggerCnt = 0;
			TriggerChannelDTO triggerChannelDTO;
			for (Iterator<TriggerChannelDTO> triggerIterator = getLstVirtualMeasurements().iterator(); triggerIterator.hasNext();) {
				triggerChannelDTO = triggerIterator.next();
				if (!triggerChannelDTO.getStatus())
				{
					iTriggerCnt++;
					continue;
				}
				if (triggerChannelDTO.getVirtualLogic() == null 
						|| triggerChannelDTO.getVirtualLogic().trim().isEmpty()
						|| triggerChannelDTO.getVirtualLogic().trim().startsWith("()")
						|| !triggerChannelDTO.getVirtualLogic().trim().startsWith("("))
				{
					logger.debug("Adding error message to virtual logic "+triggerChannelDTO.getVirtualLogic()+" adding it to "+"lstVirtualMeasurements["+iTriggerCnt+"].virtualLogic");
					addFieldError("lstVirtualMeasurements["+iTriggerCnt+"].virtualLogic", "Virtual logic is incomplete");
				}
				// START: 12-Sep-2023 - Allow all special characters except ,
//					if (triggerChannelDTO.getName().indexOf("/") != -1 || triggerChannelDTO.getName().indexOf("(") != -1 || triggerChannelDTO.getName().indexOf(")") != -1
//							|| triggerChannelDTO.getName().indexOf("[") != -1 || triggerChannelDTO.getName().indexOf("]") != -1 || triggerChannelDTO.getName().indexOf(",") != -1)
					if (triggerChannelDTO.getName().indexOf(",") != -1)
					{
//						addFieldError("lstTriggerChannels["+iTriggerCnt+"].name","Special characters not allowed: / ( ) [ ] ,");
						addFieldError("lstTriggerChannels["+iTriggerCnt+"].name","Contains invlid character ,");
					}
					// END: 12-Sep-2023 - Allow all special characters except ,
					if (triggerChannelDTO.getStart() != null)
					{
						
						if (triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.BOTH))
						{
							if (triggerChannelDTO.getTripIfOver() == null || triggerChannelDTO.getTripIfOver().isEmpty())
							{
//								logger.debug("About to add field Error..."+"lstTriggerChannels["+iTriggerCnt+"].tripIfOver");
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfOver", "TripIfOver is Required");
//								logger.debug("Field Errors list..."+getFieldErrors());
							}
							else if (!M9kUtils.isDouble(triggerChannelDTO.getTripIfOver())) 
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfOver", "TripIfOver should be numeric");
							}
							if (triggerChannelDTO.getTripIfUnder() == null || triggerChannelDTO.getTripIfUnder().isEmpty())
							{
//								logger.debug("About to add field Error..."+"lstTriggerChannels["+iTriggerCnt+"].tripIfUnder");
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfUnder", "TripIfUnder is Required");
//								logger.debug("Field Errors list..."+getFieldErrors());
							}
							else if (!M9kUtils.isDouble(triggerChannelDTO.getTripIfUnder())) 
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfUnder", "TripIfUnder should be numeric");
							}
							
	
						}
						else if (triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.OVER))
						{
							if (triggerChannelDTO.getTripIfOver() == null || triggerChannelDTO.getTripIfOver().isEmpty())
							{
//								logger.debug("About to add field Error..."+"lstTriggerChannels["+iTriggerCnt+"].tripIfOver");
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfOver", "TripIfOver is Required");
//								logger.debug("Field Errors list..."+getFieldErrors());
							}
							else if (!M9kUtils.isDouble(triggerChannelDTO.getTripIfOver())) 
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfOver", "TripIfOver should be numeric");
							}
						} 
						else if (triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.UNDER))
						{
							if (triggerChannelDTO.getTripIfUnder() == null || triggerChannelDTO.getTripIfUnder().isEmpty())
							{
//								logger.debug("About to add field Error..."+"lstTriggerChannels["+iTriggerCnt+"].tripIfUnder");
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfUnder", "TripIfUnder is Required");
//								logger.debug("Field Errors list..."+getFieldErrors());
							}
							else if (!M9kUtils.isDouble(triggerChannelDTO.getTripIfUnder())) 
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfUnder", "TripIfUnder should be numeric");
							}
						} 
						
						if (!triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.NEVER))
						{
							if (triggerChannelDTO.getChatterLimit() == null || triggerChannelDTO.getChatterLimit().isEmpty())
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].chatterLimit", "Chatter Limit is Required");
							}
							else if (triggerChannelDTO.getChatterLimit() != null && !M9kUtils.isInteger(triggerChannelDTO.getChatterLimit())) 
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].chatterLimit", "Chatter Limit should be numeric");
							}
							if (triggerChannelDTO.getChatterRate() == null || triggerChannelDTO.getChatterRate().isEmpty())
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].chatterRate", "Chatter Rate is Required");
							}
							else if (triggerChannelDTO.getChatterRate() != null && !M9kUtils.isDouble(triggerChannelDTO.getChatterRate())) 
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].chatterRate", "Chatter Rate should be numeric");
							}
							if (triggerChannelDTO.getTriggerLimit() == null || triggerChannelDTO.getTriggerLimit().isEmpty())
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].triggerLimit", "Trigger Limit is Required");
							}
							else if (triggerChannelDTO.getTriggerLimit() != null && !M9kUtils.isInteger(triggerChannelDTO.getTriggerLimit())) 
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].triggerLimit", "Trigger Limit should be numeric");
							}
						}
					}
//					logger.debug("Trigger cnt lstTriggerChannels["+iTriggerCnt+"].duration"+"ROC "+triggerChannelDTO.getTripIfGt() +", Duration "+triggerChannelDTO.getDuration() );
					// START: 25-Feb-2020 - ROC positive and Negative Limits implementation
					// Validate ROC details based on duration
					if (triggerChannelDTO.getDuration() != null && !triggerChannelDTO.getDuration().isEmpty() && !triggerChannelDTO.getDuration().equalsIgnoreCase("0"))
					{
						if(!M9kUtils.isInteger(triggerChannelDTO.getDuration()))
						{
							addFieldError("lstTriggerChannels["+iTriggerCnt+"].duration", "Duration should be numeric");
						}
						else if(Integer.parseInt(triggerChannelDTO.getDuration()) < 0)
						{
							addFieldError("lstTriggerChannels["+iTriggerCnt+"].duration", "Duration cannot be negative number (zero disables ROC)");
						}
						else if(Integer.parseInt(triggerChannelDTO.getDuration()) > 512)
						{
							addFieldError("lstTriggerChannels["+iTriggerCnt+"].duration", "Duration cannot be greater than 512");
						}
						else
						{
							if (triggerChannelDTO.getTripRocNeg() == null || triggerChannelDTO.getTripRocNeg().isEmpty())
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripRocNeg", "ROC Negative Limit cannot be blank");
							}
							else if (!M9kUtils.isDouble(triggerChannelDTO.getTripRocNeg()))
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripRocNeg", "ROC Negative Limit should be numeric");
							}
							else if (Double.parseDouble(triggerChannelDTO.getTripRocNeg()) >= 0)
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripRocNeg", "ROC Negative Limit should be a negative value");
							}
							
							if (triggerChannelDTO.getTripRocPos() == null || triggerChannelDTO.getTripRocPos().isEmpty())
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripRocPos", "ROC Positive Limit cannot be blank");
							}
							else if (!M9kUtils.isDouble(triggerChannelDTO.getTripRocPos()))
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripRocPos", "ROC Positive Limit should be numeric");
							}
							else if (Double.parseDouble(triggerChannelDTO.getTripRocPos()) <= 0)
							{
								addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripRocPos", "ROC Positive Limit should be a positive value");
							}
						}
						
						
					}

					if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.HARMONIC))
					{
						triggerChannelDTO.setDisableHarmonic(false);
						if (triggerChannelDTO.getHarmonic() == null || triggerChannelDTO.getHarmonic().isEmpty())
						{
							addFieldError("lstTriggerChannels["+iTriggerCnt+"].harmonic", "Harmonic is Required");
						}
						else if (!M9kUtils.isInteger(triggerChannelDTO.getHarmonic())) 
						{
							addFieldError("lstTriggerChannels["+iTriggerCnt+"].harmonic", "harmonic should be numeric");
						}
					}
					
					if (triggerChannelDTO.getAverage() != null && triggerChannelDTO.getAverage() < 1)
					{
						addFieldError("lstTriggerChannels["+iTriggerCnt+"].average", "Average value should be greater than zero");
					}
					
				iTriggerCnt++;	
			}
		}
		 }
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception during validation ",e);
		}
	}

	
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
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

	public Boolean getLineGroupExists() {
		return lineGroupExists;
	}

	public void setLineGroupExists(Boolean lineGroupExists) {
		this.lineGroupExists = lineGroupExists;
	}

	public boolean isNewAdded() {
		return newAdded;
	}

	public void setNewAdded(boolean newAdded) {
		this.newAdded = newAdded;
	}

	public Integer getAddedIndex() {
		return addedIndex;
	}

	public void setAddedIndex(Integer addedIndex) {
		this.addedIndex = addedIndex;
	}

	public UsersDTO getUserDto() {
		return userDto;
	}

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
	 * @return the lstAffectedLineGroups
	 */
	public List<String> getLstAffectedLineGroups() {
		return lstAffectedLineGroups;
	}

	/**
	 * @param lstAffectedLineGroups the lstAffectedLineGroups to set
	 */
	public void setLstAffectedLineGroups(List<String> lstAffectedLineGroups) {
		this.lstAffectedLineGroups = lstAffectedLineGroups;
	}


	public List<TriggerChannelDTO> getLstVirtualMeasurements() {
		return lstVirtualMeasurements;
	}

	public void setLstVirtualMeasurements(List<TriggerChannelDTO> lstVirtualMeasurements) {
		this.lstVirtualMeasurements = lstVirtualMeasurements;
	}

	public List<TriggerChannelDTO> getLstPowerMeasurements() {
		return lstPowerMeasurements;
	}

	public void setLstPowerMeasurements(List<TriggerChannelDTO> lstPowerMeasurements) {
		this.lstPowerMeasurements = lstPowerMeasurements;
	}

	public List<DfrDTO> getLstAnalogDfrs() {
		return lstAnalogDfrs;
	}

	public void setLstAnalogDfrs(List<DfrDTO> lstAnalogDfrs) {
		this.lstAnalogDfrs = lstAnalogDfrs;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public TriggerChannelDTO getEditedVirtualMeasurement() {
		return editedVirtualMeasurement;
	}

	public void setEditedVirtualMeasurement(TriggerChannelDTO editedVirtualMeasurement) {
		this.editedVirtualMeasurement = editedVirtualMeasurement;
	}

	public Map<String, List<TriggerChannelDTO>> getMapDfrSpecificMeasurements() {
		return mapDfrSpecificMeasurements;
	}

	public void setMapDfrSpecificMeasurements(Map<String, List<TriggerChannelDTO>> mapDfrSpecificMeasurements) {
		this.mapDfrSpecificMeasurements = mapDfrSpecificMeasurements;
	}

	public List<TriggerChannelDTO> getLstDfrsMeasurements() {
		return lstDfrsMeasurements;
	}

	public void setLstDfrsMeasurements(List<TriggerChannelDTO> lstDfrsMeasurements) {
		this.lstDfrsMeasurements = lstDfrsMeasurements;
	}

	public String getVirtualLogic() {
		return virtualLogic;
	}

	public void setVirtualLogic(String virtualLogic) {
		this.virtualLogic = virtualLogic;
	}

	public List<String> getLstMeasurementTypes() {
		return lstMeasurementTypes;
	}

	public void setLstMeasurementTypes(List<String> lstMeasurementTypes) {
		this.lstMeasurementTypes = lstMeasurementTypes;
	}

	public String getMeasurementType() {
		return measurementType;
	}

	public void setMeasurementType(String measurementType) {
		this.measurementType = measurementType;
	}

	public String getDescription() {
		return StringEscapeUtils.unescapeXml(description);
	}

	public void setDescription(String description) {
		this.description = StringEscapeUtils.escapeXml(description);
	}

	public List<LineGroupsAlgorithm> getLstLineGroups() {
		return lstLineGroups;
	}

	public void setLstLineGroups(List<LineGroupsAlgorithm> lstLineGroups) {
		this.lstLineGroups = lstLineGroups;
	}

	public int getSelectedVirtualIndex() {
		return selectedVirtualIndex;
	}

	public void setSelectedVirtualIndex(int selectedVirtualIndex) {
		this.selectedVirtualIndex = selectedVirtualIndex;
	}

	public List<TriggerSourceDTO> getLstTriggerSourceDto() {
		return lstTriggerSourceDto;
	}

	public void setLstTriggerSourceDto(List<TriggerSourceDTO> lstTriggerSourceDto) {
		this.lstTriggerSourceDto = lstTriggerSourceDto;
	}

//	public String getTriggerOutHold() {
//		return triggerOutHold;
//	}
//
//	public void setTriggerOutHold(String triggerOutHold) {
//		this.triggerOutHold = triggerOutHold;
//	}

	/**
	 * @return the mapGlobalTriggerInputTypes
	 */
	public Map<String, String> getMapGlobalTriggerInputTypes() {
		return mapGlobalTriggerInputTypes;
	}

	/**
	 * @param mapGlobalTriggerInputTypes the mapGlobalTriggerInputTypes to set
	 */
	public void setMapGlobalTriggerInputTypes(Map<String, String> mapGlobalTriggerInputTypes) {
		this.mapGlobalTriggerInputTypes = mapGlobalTriggerInputTypes;
	}
}
