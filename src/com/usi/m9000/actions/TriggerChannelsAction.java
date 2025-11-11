package com.usi.m9000.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import com.usi.AnalogInputDocument.AnalogInput;
import com.usi.AverageDocument.Average;
import com.usi.DFRDocument.DFR;
import com.usi.DataPoolDocument.DataPool;
import com.usi.EventInputDocument.EventInput;
import com.usi.ExportDocument2.Export;
import com.usi.ExportsDocument.Exports;
import com.usi.FrequencyDocument.Frequency;
import com.usi.GroupTriggerDocument.GroupTrigger;
import com.usi.GroupTriggersDocument.GroupTriggers;
import com.usi.LimitsDocument.Limits;
import com.usi.LineGroupDocument.LineGroup;
import com.usi.MagnitudeDocument.Magnitude;
import com.usi.MeasurementDocument.Measurement;
import com.usi.MeasurementsDocument.Measurements;
import com.usi.NegativeSequenceDocument.NegativeSequence;
import com.usi.PdcDocument.Pdc;
import com.usi.PhaseDocument.Phase;
import com.usi.PmuDocument.Pmu;
import com.usi.PmuInputDocument.PmuInput;
import com.usi.PmuInputsDocument.PmuInputs;
import com.usi.PmusDocument.Pmus;
import com.usi.PositiveSequenceDocument.PositiveSequence;
import com.usi.PowerDocument.Power;
import com.usi.RmsDocument.Rms;
import com.usi.SubStationDocument.SubStation;
import com.usi.TriggerDocument;
import com.usi.TriggerDocument.Trigger;
import com.usi.TriggersDocument.Triggers;
import com.usi.VirtualMeasurementDocument.VirtualMeasurement;
import com.usi.ZeroSequenceDocument.ZeroSequence;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.algorithms.AverageAlgorithm;
import com.usi.m9000.algorithms.RMSAlgorithm;
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
import com.usi.m9000.triggers.AbstractTrigger;
import com.usi.m9000.triggers.FrequencyTrigger;
import com.usi.m9000.triggers.LimitsAlgorithm;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.triggers.MagnitudeTrigger;
import com.usi.m9000.triggers.PhasorTrigger;
import com.usi.m9000.triggers.PowerTrigger;
import com.usi.m9000.triggers.SequenceTrigger;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kErrorConstants;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.M9kXMLConstants;
import com.usi.m9000.xml.util.M9kXMLUtils;


public class TriggerChannelsAction extends ActionSupport implements SessionAware, ServletContextListener{

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
	List<AnalogChannelDTO> lstAnalogChannels;
	List<TriggerChannelDTO> lstTriggerChannels;
	List<TriggerChannelDTO> lstTriggerChannelsHidden;
	private List<LineGroupsAlgorithm> lstLineGroups;
	List<String> lstCircuitNames;
	private Map<String,String> json;
	String magnitudeTriggerStatus;
	List<String> channelMagnitudeTriggerIndex;
	String selectedIndex;
	 int reqdIndex;
//	String triggerOutHold;
	String accessMode;
	PropertiesConfiguration config;
//	List<String> lstTriggerInputTypes;
	List<TriggerSourceDTO> lstTriggerSourceDto;
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
	  private List<TriggerChannelDTO> lstVirtualMeasurements = new ArrayList<TriggerChannelDTO>();
	  private Map<String, String> mapTriggerInputTypes;
		// 31-Mar-2021 Track measurements associated whenever line group is modified
	private List<TriggerChannelDTO> lstAssociatedMeasurements;
	private List<String> lstLinegroupsModified;

	// END: 17-Mar-2021 - Implementation of virtual measurements for delta transformers 
	
	// START: 17-Oct-2024 - Implementation of adding multiple measurements at once
	private List<TriggerChannelDTO> lstNewMeasurements = null;
	private int newMeasurementsCount=1;
	private int totalMeasurementsCount;
	private Map<String, String> mapAnalogTriggerInputTypes = null;
	private String newMeasurementsType="Rms";
	private Boolean booReorderMeasurementIds = false;
	private TriggerChannelDTO measurementToDelete = null;
	private List<TriggerChannelDTO> selectedMeasurementsToDelete;
	private List<TriggerChannelDTO> lstDeletedMeasurements = null; // 09-Nov-2024 - List of measurements to be deleted
	// END: 17-Oct-2024 - Implementation of adding multiple measurements at once
	  
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(TriggerChannelsAction.class);
	
	/**
	 * 
	 */
	public TriggerChannelsAction(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
	}
	
	public TriggerChannelsAction()
	{
		logger.debug("Entered constructor of TriggerChannelsAction req index "+getReqdIndex());
	}

	@Override
	@SuppressWarnings("unchecked")
	  public String execute() throws M9000Exception{
		  String returnValue = SUCCESS;
		try
		{
		  
		  lstAnalogChannels = (List<AnalogChannelDTO>) session.get("lstAnalogChannels");
			booAnalog = (Boolean)session.get("booAnalog");
			booDigital = (Boolean)session.get("booDigital");
			lineGroupExists = (Boolean)session.get("lineGroupExists");
			logger.debug("DIGITAL: booAnalog "+booAnalog+" booDigital "+booDigital+" lineGroupExists "+lineGroupExists);
		  if (getSourceTab() == null) 
		  {
			sourceTab = (String) session.get("sourceTab");  
		  }
		  logger.debug("entered TriggerChannelsAction execute.."+getSourceTab()+" originating tab "+getOriginTab());

		  if (getSourceTab() != null && !getSourceTab().isEmpty() && !getSourceTab().equalsIgnoreCase("Measurements"))
		  {
			  updatePmuDetails();
			  logger.debug("getLstTriggerChannels while clicking link "+((getLstTriggerChannels() != null)?getLstTriggerChannels().size():"lstTriggerChannels is null "));
			  // START: 16-Nov-2020 - Check for analog channels before updating tranducer properties
//			  if ((getLstTriggerChannels()== null || getLstTriggerChannels().isEmpty()))
			  logger.debug("Originated from originTab "+getOriginTab());

			  if (getOriginTab() != null && getOriginTab().equalsIgnoreCase("Analogs") && booAnalog && (getLstTriggerChannels()== null || getLstTriggerChannels().isEmpty()))
			  // END: 16-Nov-2020 
			  {
				  logger.debug("Originated from Analogs. Updating transducer properties... ");
				  // START: 29-Jan-2020 - Transducer implementation
				  updateTranducerPropertiesinExports();
				  // END: 29-Jan-2020
			  }
			  else if (getOriginTab() != null && getOriginTab().equalsIgnoreCase("Measurements"))
			  {
				  returnValue = saveTriggers();
			  }
			  else
			  {
				  if (getLstTriggerChannels() ==null)
				  {
					  populateInputTriggerChannels();
					populateLstMeasurementsFromXml();
				  }  
			  }
			  // START: 27-Nov-2024 - Force forward to virtual measurements if any trigger channel is deleted
			  List<TriggerChannelDTO> lstDeletedMeasurements = (List<TriggerChannelDTO>) session.get("lstDeletedMeasurements");
			  logger.debug("lstDeletedMeasurements "+lstDeletedMeasurements);
			  // if the trigger channel is deleted and affects the virtual measurements then take the user to Virtual measurements regardless of what the user selected 
//			  if (getSourceTab().equalsIgnoreCase("VirtualMeasurements") || returnValue.equalsIgnoreCase("VirtualMeasurements")) 
			  if (getSourceTab().equalsIgnoreCase("VirtualMeasurements") || returnValue.equalsIgnoreCase("VirtualMeasurements") || (lstDeletedMeasurements != null && !lstDeletedMeasurements.isEmpty())) 
			  {
			  // END: 27-Nov-2024 - Force forward to virtual measurements if any trigger channel is deleted
				  // By populating triggers we are populating virtual measurements
				  logger.debug("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is forwarded to VirtualMeasurements screen");
				  setSourceTab("VirtualMeasurements");
				  session.remove("sourceTab");
				  return "VirtualMeasurements";
			  }
			  session.put("sourceTab", sourceTab);
			  if (getSourceTab().equalsIgnoreCase("Events"))
				{
				  if (getOriginTab() != null && getOriginTab().equalsIgnoreCase("Virtuals"))
				  {
					  recreateLtrRmsAlgorithmAndExportForVirtualsOnly();
				  }

				  returnValue = navigateToEvents();
//				  if (stationDetails.getSystemAnalogChannelsCount() == 0 || (getLstTriggerChannels()!= null && !getLstTriggerChannels().isEmpty()))
//				  {
					  return returnValue;
//				  }
				}
			  else if (getSourceTab().equalsIgnoreCase("FaultLocation"))
				{
				  if (getOriginTab() != null && getOriginTab().equalsIgnoreCase("Virtuals"))
				  {
					  recreateLtrRmsAlgorithmAndExportForVirtualsOnly();
				  }
				  logger.debug("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is forwarded via Triggers/Measurements configuration screen");
				  returnValue = "FaultLocation";
//				  lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
				  logger.debug("Forwarding to FaultLocation "+((getLstTriggerChannels() != null)?getLstTriggerChannels().size():"lstTriggerChannels is null "));
//				  if (getLstTriggerChannels()!= null && !getLstTriggerChannels().isEmpty())
//				  {
					  return returnValue;
//				  }
//				  saveTriggers();
//				  return returnValue;
				}
			  else 
			  {
				  if (getSourceTab().equalsIgnoreCase("LineGroups"))
				  {
//					  returnValue = "LineGroups";
					  returnValue = back();
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
					// START: 10-Mar-2021 - Virtual Measurement implementation
					else if (getSourceTab().equalsIgnoreCase("VirtualMeasurements"))
					{
						returnValue="VirtualMeasurements";
					}
					// END: 10-Mar-2021 - Virtual Measurement implementation
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
		  logger.debug("@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());
		  logger.debug("@#$@#$@#$ lstTriggerChannels.."+getLstTriggerChannels());
		  logger.debug("@#$@#$@#$ lstLineGroups.."+getLstLineGroups());
		  
		  logger.debug("Mode "+getAccessMode());
		  
		  if (getLstLineGroups() == null)
		  {
			  lstLineGroups = (List<LineGroupsAlgorithm>) session.get("lstLineGroups");
		  }
//		  if (getTriggerOutHold() == null)
//		  {
//			  triggerOutHold = (String) session.get("triggerOutHold");
//		  }
		  lstTriggerSourceDto = (List<TriggerSourceDTO>) session.get("lstTriggerSourceDto");
		
		  // For global assistance
		  mapGlobalTriggerInputTypes = M9kUtils.convertListToMap(config.getList("allTriggerTypes"));
		  logger.debug("In TriggerChannelsAction execute method mapGlobalTriggerInputTypes "+mapGlobalTriggerInputTypes);
		  // START: 11-Dec-2024 - Remove any unwanted measurements
		  removeZombieMeasurementsAndPmus();
		  populateInputTriggerChannels();
		  prepareMeasuementData();
		  populateUserMessagesForAffectedMeasurements();
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occured in Measurement action ",e);
			throw new M9000Exception(e);
		}
		  return returnValue;

	  }
	  

	  private void populateUserMessagesForAffectedMeasurements() {
//		  logger.debug("IN1 populateUserMessagesForAffectedMeasurements getLstAssociatedMeasurements() "+getLstAssociatedMeasurements());
//		  logger.debug("IN2 populateUserMessagesForAffectedMeasurements getLstLinegroupsModified()() "+getLstLinegroupsModified());
			clearFieldErrors();
		  if (getLstAssociatedMeasurements() != null && !getLstAssociatedMeasurements().isEmpty())
		  {
			  TriggerChannelDTO triggerChannelDTO;
			  TriggerChannelDTO srcTriggerChannelDTO;
			  int iTriggerCnt = 0;
			  int iAffectedIndex = 0;
			  for (Iterator<TriggerChannelDTO> iterator = getLstTriggerChannels().iterator(); iterator.hasNext();) {
				  srcTriggerChannelDTO = iterator.next();
				  // Skip if it is not a linegroup based measurement
				  if (!srcTriggerChannelDTO.getInputType().equalsIgnoreCase(M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP))
				  {
					  iTriggerCnt++;
					  continue;
				  }
				for (Iterator<TriggerChannelDTO> iterator2 = getLstAssociatedMeasurements().iterator(); iterator2.hasNext();) {
					triggerChannelDTO = iterator2.next();
					if (triggerChannelDTO.getId().intValue() == srcTriggerChannelDTO.getId().intValue())
					{
						addFieldError("lstTriggerChannels["+iTriggerCnt+"].channel", M9kErrorConstants.ERROR_MEASUREMENT_WARNING);
						iAffectedIndex++;
						if (iAffectedIndex == getLstAssociatedMeasurements().size())
						{
							logger.debug("All affected measurements have been update ");
							break;
						}
					}
				}
				iTriggerCnt++;
			}
		  }
		  else if (getLstLinegroupsModified() != null && !getLstLinegroupsModified().isEmpty())
		  {
			  String linegroupName;
			  TriggerChannelDTO srcTriggerChannelDTO;
			  int iTriggerCnt = 0;
			  int iAffectedIndex = 0;
			  for (Iterator<TriggerChannelDTO> iterator = getLstTriggerChannels().iterator(); iterator.hasNext();) {
				  srcTriggerChannelDTO = iterator.next();
				  // Skip if it is not a linegroup based measurement
				  if (!srcTriggerChannelDTO.getInputType().equalsIgnoreCase(M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP))
				  {
					  iTriggerCnt++;
					  continue;
				  }
				  for (Iterator<String> iterator2 = getLstLinegroupsModified().iterator(); iterator2.hasNext();) {
					  linegroupName = iterator2.next();
					  logger.debug("About to add field error linegroupName "+linegroupName+" src trigger name "+srcTriggerChannelDTO.getInputChannelName()+" srcTriggerChannelDTO.getInputType() "+srcTriggerChannelDTO.getInputType()+" iTriggerCnt "+iTriggerCnt);
					if (linegroupName.equalsIgnoreCase(srcTriggerChannelDTO.getInputChannelName()))
					{
						logger.debug("Match found ... "+linegroupName);
						if (getLstAssociatedMeasurements() == null)
						{
							lstAssociatedMeasurements = new ArrayList<TriggerChannelDTO>();
						}
						lstAssociatedMeasurements.add(srcTriggerChannelDTO);
						addFieldError("lstTriggerChannels["+iTriggerCnt+"].channel", M9kErrorConstants.ERROR_MEASUREMENT_WARNING);
						iAffectedIndex++;
						if (iAffectedIndex == getLstLinegroupsModified().size())
						{
							logger.debug("All affected measurements have been updated ");
							break;
						}
					}
				}
				iTriggerCnt++;
			}
		  }
	}

	@SuppressWarnings("unchecked")
	public String saveTriggers() throws M9000Exception
	  {
		  logger.debug("In saveTriggers method");
		  String returnValue = "Save";
		  
		  try
		  {
			  currentSubstation = (SubStation) session.get("selectedSubstation");
	//		  logger.debug("Substation from session in saveTriggers() "+currentSubstation);
			  if (getSourceTab() != null && getSourceTab().isEmpty())
				{
					session.remove("sourceTab");
				}
			  lstAnalogChannels = (List<AnalogChannelDTO>) session.get("lstAnalogChannels");
//			  logger.debug("@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());
//			  logger.debug("@#$@#$@#$ lstTriggerChannels.."+getLstTriggerChannels());
			  logger.debug("@#$@#$@#$ lstLineGroups.."+getLstLineGroups());
			  
			  logger.debug("Mode "+getAccessMode());
			  
//			  if  (getLstTriggerChannels() == null)
//			  {
//				  lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
//			  }
			  if (getLstLineGroups() == null)
			  {
				  lstLineGroups = (List<LineGroupsAlgorithm>) session.get("lstLineGroups");
			  }
//			  if (getTriggerOutHold() == null)
//			  {
//				  triggerOutHold = (String) session.get("triggerOutHold");
//			  }
			  logger.debug("\t\t\tlstTriggerChannels is null or empty "+((lstTriggerChannels!=null)?lstTriggerChannels.size():null));
	//		  DfrDTO dfrDTO = (DfrDTO) session.get("SelectedDfrToConfigure");
	//		  dfrDTO.setStatus("COMPLETE");
	//		  xmlConfigDfr = (DFR) session.get("SelectedDfrObj");
			  if (getLstTriggerChannels()!= null && !getLstTriggerChannels().isEmpty())
			  {
//				  logger.debug("Inside Submit Click lstAnalogChannels.."+getLstAnalogChannels());
//				  logger.debug("Inside Submit Click lstTriggerChannels.."+getLstTriggerChannels());
				  logger.debug("In SaveTrigger: measurements list in session: before updating trigger types "+lstTriggerChannels);
				  // Update the iput triggers list so that it gets populated when loading the triggers page again
				  TriggerChannelDTO triggerChannelDTOToBePopulated = null;
				  for (Iterator<TriggerChannelDTO> iterator = getLstTriggerChannels().iterator(); iterator.hasNext();) {
					  triggerChannelDTOToBePopulated = iterator.next();
					  if (triggerChannelDTOToBePopulated != null)
					  {
							populateTriggerTypes(triggerChannelDTOToBePopulated);											  
					  }
				  }
				  logger.debug("In SaveTrigger: measurements list in session: after updating trigger types  "+lstTriggerChannels);
				  session.put("lstTriggerChannels", getLstTriggerChannels());
//				  logger.debug("test value..."+getTriggerOutHold());
//				  session.put("triggerOutHold", triggerOutHold);
				  if (getLstTriggerChannels().size() > 0)
				  {
					  if (populateTriggerChannelsIntoXml())
					  {
						  returnValue = "VirtualMeasurements";
					  }
				  }
			  }
	//		  logger.debug("After populateTriggerChannels() Substation from session in saveTriggers() "+currentSubstation);
	//		  logger.debug("Digital count with dfrDTO.getDigitalChnlCnt() ..."+dfrDTO.getDigitalChnlCnt());
	//		  logger.debug("Digital count with dfrDTO.getChnlConfigDropDownList().getDigitalCnt() ..."+dfrDTO.getChnlConfigDropDownList().getDigitalCnt());
	//		  if (dfrDTO.getChnlConfigDropDownList().getDigitalCnt() == 0)
	//		  {
			  
	//			logger.debug("Is Digital? "+booDigital);
	//		if (booDigital)
	//		  {
	////			lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
	//			  returnValue = "Events";
	//		  }
	//		else if (getLineGroupExists() || (getLstLineGroups()!=null && !getLstLineGroups().isEmpty()))
	//		{
	//			returnValue = "DirectFaultLocation";
	//		}
	//		else
	//		{
	//			accessMode="Edit";
	//			session.put("accessMode", accessMode);
	//		}
		  } 
		  catch (Exception e) {
			logger.error("Exception occured while saving measurement details ",e);
			throw new M9000Exception(e);
		}
		  return returnValue;
		  
	  }

	  public String finish() throws Exception
	  {
		  saveTriggers();
		  
		  saveIntoDB();
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
		public String saveTriggersAndNext() throws Exception
		  {
			  String returnValue = "Save";
			  saveTriggers();
			  if (getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit"))
			  {
				  saveIntoDB();

			  }
			  logger.debug("DIGITAL: save in progress is booDigital? "+booDigital);
			  // START: 03-Mar-2021 - Implementation of virtual measurements for delta transformers 
//			if (booDigital)
//			  {
//				logger.debug("\t\t\tlstTriggerChannels is null or empty "+((lstTriggerChannels!=null)?lstTriggerChannels.size():null));
//				  returnValue = "Events";
//			  }
//			else if ((getLineGroupExists() != null && getLineGroupExists()) || (getLstLineGroups()!=null && !getLstLineGroups().isEmpty()))
//			{
//				returnValue = "DirectFaultLocation";
//			}
			  returnValue = "VirtualMeasurements";
			// END: 03-Mar-2021 - Implementation of virtual measurements for delta transformers
			  return returnValue;
			  
		  }
		
	  public String saveTriggersAndSend() throws Exception
	  {
		  String returnValue = "Send";
		  saveTriggers();
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
	  @SuppressWarnings("unchecked")
	  @SkipValidation
	  public String populateTriggerTypes()
	  {
		  logger.debug("Entered populateTriggerTypes get Reqd INdex..."+getReqdIndex());
		  lstTriggerSourceDto = (List<TriggerSourceDTO>) session.get("lstTriggerSourceDto");
		  logger.debug("lstTriggerSourceDto is it null? "+(lstTriggerSourceDto == null));
//		  logger.debug("Lst of trigger channels..."+getLstTriggerChannels());
//		  logger.debug("lstTriggerSourceDto..."+lstTriggerSourceDto);
//		  logger.debug("Access Mode set..."+getAccessMode());
		  int triggerIndex = getReqdIndex();
//		  if (getLstTriggerChannels() == null || getLstTriggerChannels().isEmpty()|| getLstTriggerChannels().get(triggerIndex).getInputChannelName() == null || getLstTriggerChannels().get(triggerIndex).getType() == null)
//		  {
////			  lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
//			  logger.debug("Lst of trigger channels from session..."+lstTriggerChannels);
//		  }
//		  else
//		  {
//			  logger.debug("\t\t\tlstTriggerChannels is null or empty "+((lstTriggerChannels!=null)?lstTriggerChannels.size():null));
//		  }
//		  logger.debug("getLstTriggerChannels().get(triggerIndex). "+getLstTriggerChannels().get(triggerIndex));
//		  logger.debug("Input Channel selected..."+getLstTriggerChannels().get(triggerIndex).getChannel());
		  TriggerSourceDTO triggerSourceDto;
		  for (Iterator<TriggerSourceDTO> iterator = lstTriggerSourceDto.iterator(); iterator.hasNext();) {
			triggerSourceDto =  iterator.next();
			logger.debug("\t\t\t#TRIGGER-SOURCEInside trigger source list "+ triggerSourceDto.getSourceName()+" To be compared with trigger channel "+getLstTriggerChannels().get(triggerIndex).getChannel());
			logger.debug("triggerSourceDto.getSourceLineGroupType() "+triggerSourceDto.getSourceLineGroupType());
			if ((triggerSourceDto.getSourceValue().equalsIgnoreCase(getLstTriggerChannels().get(triggerIndex).getChannel())) 
					|| (triggerSourceDto.getSourceName().equalsIgnoreCase(getLstTriggerChannels().get(triggerIndex).getChannel())))
			{
				getLstTriggerChannels().get(triggerIndex).setInputChannelName(triggerSourceDto.getSourceInputObjName());
				getLstTriggerChannels().get(triggerIndex).setInputType(triggerSourceDto.getSourceType());
				getLstTriggerChannels().get(triggerIndex).setChassis(triggerSourceDto.getChassis());
				logger.debug("triggerSourceDto.getSourceInputObjName() "+triggerSourceDto.getSourceInputObjName()+" Trigger source dto name..."+triggerSourceDto.getSourceType()+" Chassis "+triggerSourceDto.getChassis());
//				logger.debug("Trigger Line Group source Type..."+triggerSourceDto.getSourceLineGroupType());
				getLstTriggerChannels().get(triggerIndex).setMapTriggerInputTypes(triggerSourceDto.getMapTriggerInputTypes());
				setMapTriggerInputTypes(triggerSourceDto.getMapTriggerInputTypes());
				logger.debug("IS transducer source? "+triggerSourceDto.isTransducer());
				getLstTriggerChannels().get(triggerIndex).setTransducer(triggerSourceDto.isTransducer());
//				lstTriggerInputTypes = triggerSourceDto.getLstTriggerInputTypes();
				break;
			}
		
		  }
		  
			logger.debug("AFter mapTriggerInputTypes "+getLstTriggerChannels().get(triggerIndex).getMapTriggerInputTypes());
			if (getLstTriggerChannels().get(triggerIndex).getMapTriggerInputTypes() == null || getLstTriggerChannels().get(triggerIndex).getMapTriggerInputTypes().isEmpty())
			{
				getLstTriggerChannels().get(triggerIndex).setMapTriggerInputTypes(new LinkedHashMap<String, String>());
				getLstTriggerChannels().get(triggerIndex).getMapTriggerInputTypes().put(M9kConstants.DELETE, M9kConstants.DELETE);
			}
//			session.put("lstTriggerChannels", lstTriggerChannels);
//		  logger.debug("Returning from populateTriggerTypes with the session update Trigger List..."+lstTriggerChannels);
		  logger.debug("Returning from populateTriggerTypes with the list..."+getLstTriggerChannels().get(triggerIndex).getMapTriggerInputTypes());
		  return "TriggerTypes";
	  }
	  
	  
	  /**
	   * Modified on 17-Mar-2021 - Implemented Apparent power and changed list to map for display
	   * @param triggerSourceDto
	   * @return
	   */
	  private Map<String,String> getTriggerTypes(TriggerSourceDTO triggerSourceDto)
	  {
		  Map<String, String> mapTriggerTypes = new LinkedHashMap<String, String>();
		  List<Object> lstTriggerTypes = new ArrayList<Object>(1);
		  try
		  {
			  if (triggerSourceDto.getSourceType().equalsIgnoreCase(M9kConstants.TRIGGER_INPUT_TYPE_ANALOG))
			  {
				  lstTriggerTypes = config.getList("analogInputTriggerType");
			  }
			  else if (triggerSourceDto.getSourceLineGroupType() == null || triggerSourceDto.getSourceLineGroupType().isEmpty())
			  {
				  lstTriggerTypes.add(M9kConstants.DELETE+":"+M9kConstants.DELETE);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.ALL_PHASE))
			  {
				  lstTriggerTypes = config.getList("lineGroupInputTriggerType");
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.SEQ_VOLTAGE))
			  {
				  lstTriggerTypes = config.getList("voltageTriggerType");
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.SEQ_CURRENT))
			  {
				  lstTriggerTypes = config.getList("currentTriggerType");
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.A_PHASE))
			  {
				  lstTriggerTypes = config.getList("phaseATriggerType");
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.B_PHASE))
			  {
				  lstTriggerTypes = config.getList("phaseBTriggerType");
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.C_PHASE))
			  {
				  lstTriggerTypes = config.getList("phaseCTriggerType");
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.A_B_PHASE))
			  {
				  lstTriggerTypes = config.getList("phaseABTriggerType");
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.A_C_PHASE))
			  {
				  lstTriggerTypes = config.getList("phaseACTriggerType");
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.B_C_PHASE))
			  {
				  lstTriggerTypes = config.getList("phaseBCTriggerType");
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.ALL_PHASE_MISSING_PHASE_A))
			  {
				  lstTriggerTypes = config.getList("allPowerTriggerType");
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_A);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.ALL_PHASE_MISSING_PHASE_B))
			  {
				  lstTriggerTypes = config.getList("allPowerTriggerType");
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_B);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.ALL_PHASE_MISSING_PHASE_C))
			  {
				  lstTriggerTypes = config.getList("allPowerTriggerType");
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_C);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.CURRENT_ALL_PHASE)) // Remove sequence voltage but keep sequence current 
			  {
				  lstTriggerTypes = config.getList("lineGroupInputTriggerType");
				  removeSequenceAlgorithm(lstTriggerTypes, M9kConstants.VOLTAGE);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.VOLTAGE_ALL_PHASE_MISSING_PHASE_A))
			  {
				  lstTriggerTypes = config.getList("lineGroupInputTriggerType");
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_A);
				  removeSequenceAlgorithm(lstTriggerTypes, M9kConstants.CURRENT);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.VOLTAGE_ALL_PHASE_MISSING_PHASE_B))
			  {
				  lstTriggerTypes = config.getList("lineGroupInputTriggerType");
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_B);
				  removeSequenceAlgorithm(lstTriggerTypes, M9kConstants.CURRENT);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.VOLTAGE_ALL_PHASE_MISSING_PHASE_C))
			  {
				  lstTriggerTypes = config.getList("lineGroupInputTriggerType");
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_C);
				  removeSequenceAlgorithm(lstTriggerTypes, M9kConstants.CURRENT);
			  }			  
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.ALL_PHASE_MISSING_PHASE_A_B))
			  {
				  lstTriggerTypes = config.getList("allPowerTriggerType");
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_A_B);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.ALL_PHASE_MISSING_PHASE_B_C))
			  {
				  lstTriggerTypes = config.getList("allPowerTriggerType");
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_B_C);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.ALL_PHASE_MISSING_PHASE_C_A))
			  {
				  lstTriggerTypes = config.getList("allPowerTriggerType");
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_C_A);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.ALL_PHASE_MISSING_PHASE_A_B_AND_SEQ_VOLTAGE))
			  {
				  lstTriggerTypes = config.getList("voltageTriggerType");
				  lstTriggerTypes.addAll(lstTriggerTypes.size()-1,config.getList("allPowerTriggerType"));
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_A_B);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.ALL_PHASE_MISSING_PHASE_B_C_AND_SEQ_VOLTAGE))
			  {
				  lstTriggerTypes = config.getList("voltageTriggerType");
				  lstTriggerTypes.addAll(lstTriggerTypes.size()-1,config.getList("allPowerTriggerType"));
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_B_C);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.ALL_PHASE_MISSING_PHASE_C_A_AND_SEQ_VOLTAGE))
			  {
				  lstTriggerTypes = config.getList("voltageTriggerType");
				  lstTriggerTypes.addAll(lstTriggerTypes.size()-1,config.getList("allPowerTriggerType"));
				  removePhaseFromList(lstTriggerTypes, M9kConstants.PHASE_C_A);
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.A_PHASE_AND_SEQ_CURRENT))
			  {
				  lstTriggerTypes = config.getList("currentTriggerType");
				  lstTriggerTypes.addAll(lstTriggerTypes.size()-1,config.getList("phaseATriggerType"));
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.B_PHASE_AND_SEQ_CURRENT))
			  {
				  lstTriggerTypes = config.getList("currentTriggerType");
				  lstTriggerTypes.addAll(lstTriggerTypes.size()-1,config.getList("phaseBTriggerType"));
			  }
			  else if (triggerSourceDto.getSourceLineGroupType().equalsIgnoreCase(M9kConstants.C_PHASE_AND_SEQ_CURRENT))
			  {
				  lstTriggerTypes = config.getList("currentTriggerType");
				  lstTriggerTypes.addAll(lstTriggerTypes.size()-1,config.getList("phaseCTriggerType"));
			  }

			  logger.debug("before invoking convertListToMap lstTriggerTypes "+lstTriggerTypes);
			  mapTriggerTypes = M9kUtils.convertListToMap(lstTriggerTypes);
		  }
		  catch (Exception e) {
			  logger.error("Error in getting trigger types ",e);
			  lstTriggerTypes.add(M9kConstants.DELETE);
		  }
		  return mapTriggerTypes;
	  }
	  
	  /**
	   * Removes the specified type of sequence algorithms 
	   * @param current
	   */
	  private void removeSequenceAlgorithm(List<Object> lstTriggerTypes, String type) {
		  String algorithmsStrings;
			for (Iterator<Object> iterator = lstTriggerTypes.iterator(); iterator.hasNext();) {
				algorithmsStrings = iterator.next().toString();
				logger.debug("Seq. algorithms starts like "+algorithmsStrings);
				if (type.equalsIgnoreCase(M9kConstants.VOLTAGE) && algorithmsStrings.indexOf("Sequence_Voltage") != -1
						|| type.equalsIgnoreCase(M9kConstants.CURRENT) && algorithmsStrings.indexOf("Sequence_Current") != -1)
				{
					logger.debug("Seq. Algorithm type to be removed..."+algorithmsStrings);
					iterator.remove();
				}
			}
		
	}

	/**
	   * Remove the phases for which the Measurements can't be calculated
	 * @param lstTriggerTypes 
	   * @param phaseA
	   */
	  private void removePhaseFromList(List<Object> lstTriggerTypes, String phase) {
		  String algorithmsStrings;
		for (Iterator<Object> iterator = lstTriggerTypes.iterator(); iterator.hasNext();) {
			algorithmsStrings = iterator.next().toString();
			logger.debug("algorithms starts like "+algorithmsStrings.toUpperCase());
			if ((phase.equalsIgnoreCase(M9kConstants.PHASE_A) && algorithmsStrings.toUpperCase().startsWith("A_PHASE"))
					|| (phase.equalsIgnoreCase(M9kConstants.PHASE_B) && algorithmsStrings.toUpperCase().startsWith("B_PHASE"))
					|| (phase.equalsIgnoreCase(M9kConstants.PHASE_C) && algorithmsStrings.toUpperCase().startsWith("C_PHASE"))
					||(phase.equalsIgnoreCase(M9kConstants.PHASE_A_B) && (algorithmsStrings.toUpperCase().startsWith("A_PHASE") || algorithmsStrings.toUpperCase().startsWith("B_PHASE")))
					||(phase.equalsIgnoreCase(M9kConstants.PHASE_B_C) && (algorithmsStrings.toUpperCase().startsWith("B_PHASE") || algorithmsStrings.toUpperCase().startsWith("C_PHASE")))
					||(phase.equalsIgnoreCase(M9kConstants.PHASE_C_A) && (algorithmsStrings.toUpperCase().startsWith("C_PHASE") || algorithmsStrings.toUpperCase().startsWith("A_PHASE")))
					)
			{
				logger.debug("Algorithm type to be removed..."+algorithmsStrings);
				iterator.remove();
			}
		}
		
	}

	@SuppressWarnings("unchecked")
//	@SkipValidation
	  public String back() throws Exception
	  {
//		  saveTriggers();
//		  session.put("triggerOutHold",getTriggerOutHold()); 
		  lstLineGroups = (List<LineGroupsAlgorithm>) session.get("lstLineGroups");
//		  logger.debug("Entered Back session triggerOutHold value... "+getTriggerOutHold());
		  String returnValue = "Back";
		  saveTriggers();
		  return returnValue;
	  }
	  
	  @SuppressWarnings("unchecked")
	  @SkipValidation
	  public String addNewTriggerChannel()
	  {
		  String returnValue = "AddNewMeasurement";
		  try
		  {
		  
		  lstAnalogChannels = (List<AnalogChannelDTO>) session.get("lstAnalogChannels");
		  if (getLstLineGroups() == null)
		  {
			  lstLineGroups = (List<LineGroupsAlgorithm>) session.get("lstLineGroups");
		  }
//		  if (getTriggerOutHold() == null)
//		  {
//			  triggerOutHold = (String) session.get("triggerOutHold");
//		  }
//		  if (getTriggerOutHold() == null || getTriggerOutHold().isEmpty())
//		  {
//			  setTriggerOutHold(M9kConstants.DEFAULT_TRIGGER_OUT_HOLD);
//		  }
//		  if (getAccessMode() == null)
//		  {
//			  lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
//		  }
		  logger.debug("@#$@#$@#$ in addNewTriggerChannel lstTriggerChannels.."+getLstTriggerChannels());
		  
		  logger.debug("MULTIPLE NEW MEASUREMENTS - newMeasurementsCount "+getNewMeasurementsCount());
		  
		  if (getLstTriggerChannels() == null) // Edit mode with partially saved DFR
		  {
			  lstTriggerChannels = new ArrayList<TriggerChannelDTO>();
		  }
//		  totalMeasurementsCount = lstTriggerChannels.size();
		  logger.debug("Total Measurement Count "+totalMeasurementsCount);
		  logger.debug("New Measurement Type "+getNewMeasurementsType());
//		  if (lstTriggerSourceDto)
//		  populateInputTriggerChannels();
		  TriggerChannelDTO triggerChannelDto;
		  lstNewMeasurements = new ArrayList<TriggerChannelDTO>();
		  // START: 17-Oct-2024 - Adding multiple measurements at once
		  int modIndex = 0;
		  for (int i = 0; i < getNewMeasurementsCount(); i++)
		  {
			  modIndex = i % lstAnalogChannels.size();
			  logger.debug("MULTIPLE NEW MEASUREMENTS - modIndex "+modIndex+" lstAnalogChannels.size() "+lstAnalogChannels.size()+" i "+i);
			  triggerChannelDto = new TriggerChannelDTO();
		  // END: 17-Oct-2024 - Adding multiple measurements at once
			  if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
			  {
				  triggerChannelDto.setId(StationDTO.getNextAvailableTriggerId());  
	//			  triggerChannelDto.setId(-1);
			  }
			  else
			  {
				  triggerChannelDto.setId(totalMeasurementsCount+i);
			  }
			  String sourceDisplayName = lstAnalogChannels.get(modIndex).getUnalignedDisplayName().substring(lstAnalogChannels.get(modIndex).getUnalignedDisplayName().indexOf("-")+1);
//			  triggerChannelDto.setName(sourceDisplayName+"- RMS");
			  triggerChannelDto.setName(sourceDisplayName+"- "+getNewMeasurementsType());
			  triggerChannelDto.setPhase(M9kConstants.NO_PHASE);
			// START: 17-Oct-2024 - Adding multiple measurements at once
//			  triggerChannelDto.setChannel(lstAnalogChannels.get(modIndex).getChannel());
			  // END: 17-Oct-2024 - Adding multiple measurements at once
			  triggerChannelDto.setChassis(lstAnalogChannels.get(modIndex).getChassis());
			  triggerChannelDto.setDisableHarmonic(true);
			  triggerChannelDto.setGlobalLineGroupId(0);
			  triggerChannelDto.setStart(M9kConstants.NEVER);
			  triggerChannelDto.setDisableTripOver(true);
			  triggerChannelDto.setDisableTripUnder(true);
	
			  // START 29-July-2015: ROC implementation  
			  // START: 25-Feb-2020 - ROC positive and Negative Limits implementation
	//		  triggerChannelDto.setDisableTripIfGt(true);
			  triggerChannelDto.setDisableTripRoc(true);
			  // END: 25-Feb-2020
			  triggerChannelDto.setDisableDuration(true);
			  // END 29-July-2015: ROC implementation
			  triggerChannelDto.setStatus(true);
			// START: 17-Mar-2020 - Get the triggers input types for transducers
	//		  triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_ANALOG);
	//		  triggerChannelDto.setInputChannelName(lstAnalogChannels.get(0).getName());
			  // END: 17-Mar-2020
	//		  triggerChannelDto.setChatterLimit(""+stationDetails.getChatterLimit());
	//		  triggerChannelDto.setChatterRate(""+stationDetails.getChatterRate());
	//		  triggerChannelDto.setTriggerLimit(""+stationDetails.getTriggerLimit());
			  triggerChannelDto.setExportStatus(true);
			  triggerChannelDto.setPmuStatus(false);
			  // START: 01-May-2018 All exports to be part of DDR instead of just RMS - ReleaseV1.0.8.2
			  triggerChannelDto.setDdrStatus(true);
			  // END: 01-May-2018
			  // START: 3-Dec-2022 - Disturbance alarm implementation
			  triggerChannelDto.setDisturbanceAlarm(false);
			  // END: 3-Dec-2022 - Disturbance alarm implementation
			  triggerChannelDto.setTriggerStatus(false);
			  // START: 17-Mar-2020 - Get the triggers input types for transducers
	//		  triggerChannelDto.setLstTriggerInputTypes(config.getList("analogInputTriggerType"));
			  TriggerSourceDTO triggerSourceDto = lstTriggerSourceDto.get(modIndex);
				triggerChannelDto.setInputChannelName(triggerSourceDto.getSourceInputObjName());
				triggerChannelDto.setInputType(triggerSourceDto.getSourceType());
				triggerChannelDto.setMapTriggerInputTypes(triggerSourceDto.getMapTriggerInputTypes());
				setMapTriggerInputTypes(triggerSourceDto.getMapTriggerInputTypes());
				logger.debug("IS transducer source? "+triggerSourceDto.isTransducer());
				triggerChannelDto.setTransducer(triggerSourceDto.isTransducer());
			  // END: 17-Mar-2020
	
			  logger.debug("\t\t\tNew trigger types "+triggerChannelDto.getMapTriggerInputTypes());
			// START: 17-Oct-2024 - Adding multiple measurements at once
			  triggerChannelDto.setChannel(triggerSourceDto.getSourceValue());
			  triggerChannelDto.setType(getNewMeasurementsType());
			  lstNewMeasurements.add(triggerChannelDto);
			  if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
			  {
//				  lstTriggerChannels.add(triggerChannelDto);
				  saveMeasurementsIntoXml(triggerChannelDto);				  
				  logger.debug("size of lst "+lstTriggerChannels.size());
					//		  if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
					//		  {
								  setNewAdded(true);
//								  // START: 25-Jan-2013: Saravanan: Issue when adding new measurement after deleting an existing measurement
//					//			  setAddedIndex(triggerChannelDto.getId()-1);
//								  setAddedIndex(lstTriggerChannels.size()-1);
//								  // END:25-Jan-2013: Saravanan
//								  logger.debug("Newly added id "+lstTriggerChannels.get(getAddedIndex()).getId());
//								  logger.debug("Newly added channel "+lstTriggerChannels.get(getAddedIndex()).getChannel());
//								  logger.debug("Newly added list of trigger types "+lstTriggerChannels.get(getAddedIndex()).getMapTriggerInputTypes());
//								  logger.debug("Newly added name "+lstTriggerChannels.get(getAddedIndex()).getName());
			  }
			// END: 17-Oct-2024 - Adding multiple measurements at once
	//		  }
	//		  session.put("lstTriggerChannels", lstTriggerChannels);
		// START: 17-Oct-2024 - Adding multiple measurements at once
		  }
//		  if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
//		  {
//			  setTotalMeasurementsCount(lstTriggerChannels.size());
//		  }
		  logger.debug("lstNewMeasurements "+lstNewMeasurements);
		  // END: 17-Oct-2024 - Adding multiple measurements at once
		  }
		  catch (Exception e) {
			logger.error("Error occurred adding a new channel", e);
		}
		  return returnValue;
	  }

	  public String refreshMeasurements()
	  {
		  logger.debug("Updated triggers channel size "+getLstTriggerChannels().size());
		  return SUCCESS;
	  }
//	  public String getJSON(){
//		  populateTriggerTypes();
//		return SUCCESS;  
//	  }
	  private void populateTriggerTypes(TriggerChannelDTO triggerChannelDto)
	  {
		  TriggerSourceDTO triggerSourceDto;
//		logger.debug("To be compared to "+triggerChannelDto.getChannel()+" lstTriggerSourceDto "+lstTriggerSourceDto);
		logger.debug("Received TriggerChannelDTO "+triggerChannelDto);
		  for (Iterator<TriggerSourceDTO> iterator = lstTriggerSourceDto.iterator(); iterator.hasNext();) {
			triggerSourceDto =  iterator.next();
			logger.debug("DELETE-DEBUG: get map triggers from source"+" triggerChannelDto.getChannel() "+triggerChannelDto.getChannel()+ " Source Value  "+triggerSourceDto.getSourceValue()+"triggerSourceDto.getSourceName() "+triggerSourceDto.getSourceName() );
			if ((triggerSourceDto.getSourceValue().equalsIgnoreCase(triggerChannelDto.getChannel())) 
					|| (triggerSourceDto.getSourceName().equalsIgnoreCase(triggerChannelDto.getChannel())))
			{
				triggerChannelDto.setInputChannelName(triggerSourceDto.getSourceInputObjName());
				triggerChannelDto.setInputType(triggerSourceDto.getSourceType());
				triggerChannelDto.setChassis(triggerSourceDto.getChassis());
				logger.debug("Trigger source dto name..."+triggerSourceDto.getSourceType()+" Chassis "+triggerSourceDto.getChassis());
				logger.debug("Trigger source trigger types..."+triggerSourceDto.getMapTriggerInputTypes()+" Chassis "+triggerSourceDto.getChassis());
				logger.debug("Trigger Line Group source Type..."+triggerSourceDto.getSourceLineGroupType());
				triggerChannelDto.setMapTriggerInputTypes(triggerSourceDto.getMapTriggerInputTypes());
				// START: 04-Feb-2020 - Tranducer implentation 
				logger.debug("Is transducer channel? "+triggerChannelDto.isTransducer()+" source DTO transducer? "+triggerSourceDto.isTransducer());
				if (triggerChannelDto.isTransducer() && !triggerSourceDto.isTransducer())
				{
					// Changed from transducer to normal channel. Default RMS exports and DDR should be exported and disabled
					logger.debug("Changed from transducer to normal channel. Default RMS exports and DDR should be exported and disabled");
					triggerChannelDto.setType(M9kConstants.RMS);
				}

				triggerChannelDto.setTransducer(triggerSourceDto.isTransducer());
				// END: 04-Feb-2020
				break;
			}
		
		  }
		  
	  }

	  // Populating Trigger
		private boolean populateTriggerChannelsIntoXml() throws Exception
		{
			boolean isVirtualMeasurementAffected = false;
			logger.debug("Entered populateTriggerChannels..."+getLstTriggerChannels());
//			logger.debug("Entered populateTriggerChannels with substation "+currentSubstation);
			Algorithms algorithms = null;
			// Construct exports
			Triggers triggers = null;
			Exports exports = null;
			Export export = null;
			PmuInputs pmuInputs = null;
			PmuInput pmuInput = null;
			Measurements measurements = null;
			Measurement measurement = null;
			Pmus localPmus;
			Pmu localPmu = null;
//			PmuInputs localPmuInputs;
			PmuInput localPmuInput;
//			DFR xmlDfr = null;
//			Iterator<String> mapXmlDfrIterator = mapXMLDfrs.keySet().iterator();
//			String dfrKey;
//			for (; mapXmlDfrIterator.hasNext();) {
//				dfrKey = mapXmlDfrIterator.next();
//				xmlDfr = mapXMLDfrs.get(dfrKey);
//				clearAlgorithms(xmlDfr);
//			}

			boolean booTriggersExist = false;
			TriggerChannelDTO triggerChannelDTO;
			DataPool datapool;
			String exportName;
			Limits limits = null;
			String limitsName;
			GroupTriggers groupTriggers = currentSubstation.getGroupTriggers();
			
			// START: 10-Apr-2015: Events for PMU
			int sizeOfArray;
			// END: 10-Apr-2015

			while (groupTriggers != null && groupTriggers.sizeOfGroupTriggerArray() > 0)
			{
				groupTriggers.removeGroupTrigger(0);
			}
			
			
			Iterator<String> mapXmlDfrIterator = mapXMLDfrs.keySet().iterator();
			String dfrKey;
			for (; mapXmlDfrIterator.hasNext();) {
				dfrKey = mapXmlDfrIterator.next();
				xmlConfigDfr = mapXMLDfrs.get(dfrKey);
//				logger.debug("DFR id "+xmlConfigDfr.getSystem().getDfrId());
//				logger.debug("\t\t DEBUG-UNSET: XML config \t"+xmlConfigDfr.xmlText());
				Pmus pmus = null;
				if (xmlConfigDfr.getDataPool().getAlgorithms().getPmus() != null)
				{
					pmus = (Pmus) xmlConfigDfr.getDataPool().getAlgorithms().getPmus().copy();
				}
				if (xmlConfigDfr.getDataPool().getAlgorithms() != null)
				{
					xmlConfigDfr.getDataPool().unsetAlgorithms();
				}
				if (xmlConfigDfr.getDataPool().getExports() != null)
				{
					xmlConfigDfr.getDataPool().unsetExports();
				}
				algorithms = xmlConfigDfr.getDataPool().addNewAlgorithms();
				algorithms.setName("Algorithms");
				if (pmus != null)
				{
					localPmu = pmus.getPmuArray(0);
					// Start: 10-Apr-2015: Skip events PMUs from getting removed
//					while (localPmu.sizeOfPmuInputArray() > 0)
//					{
//						localPmu.removePmuInput(0);
//					}
					sizeOfArray = localPmu.sizeOfPmuInputArray();
					logger.debug("sizeOfArray "+sizeOfArray+" for pmu name "+localPmu.getName());
					for (int i = 0;i<sizeOfArray;) {
						if (localPmu.getPmuInputArray(i).getPmuInputType().equalsIgnoreCase("Event"))
						{
//							logger.debug("Skipping event pmu "+i+" Pmu input name "+localPmu.getPmuInputArray(i).getName());
							i++;
						}
						else
						{
//							logger.debug("Removing pmu at "+i+" Pmu input name "+localPmu.getPmuInputArray(i).getName());
							localPmu.removePmuInput(i);
							sizeOfArray = localPmu.sizeOfPmuInputArray();
						}
						
					}
					//END: 10-Apr-2015
					
					algorithms.setPmus(pmus);
					localPmu = algorithms.getPmus().getPmuArray(0);
				}
				else
				{
					localPmus = algorithms.addNewPmus();
					localPmus.setName("PMUs");
					localPmu = localPmus.addNewPmu();
//					localPmu.setPmuEnable(M9kConstants.DISABLE_ZERO);
				}
				// Start 11-Oct-2013 PMU name in PDC changed to reflect the dfr name instead of station name for all chassis
//				localPmu.setName(stationDetails.getSystemStationName());
				localPmu.setName(dfrKey);
				localPmu.setPmuName(dfrKey);
				// End 11-Oct-2013
				
				localPmu.setPmuId(Integer.parseInt(xmlConfigDfr.getIPAddress().substring(xmlConfigDfr.getIPAddress().lastIndexOf(".")+1)));
				localPmu.setPmuPort(stationDetails.getPmuPort());
				localPmu.setUdpPort(stationDetails.getPmuUdpPort());
//				logger.debug("\n\n\n\n\t\t\t\tSetting UDP port here "+localPmu.getUdpPort());
				localPmu.setPmuStreamType(stationDetails.getPmuStreamType());
				localPmu.setPmuPhasorMode(stationDetails.getPmuPhasorMode());
				localPmu.setPmuDataRate(stationDetails.getPmuDataRate());
				// Changed for PDC implementation 4-Oct-2013
//				  if (stationDetails.getPmuStatus().equalsIgnoreCase(M9kConstants.ENABLE))
//				if (getStationDetails().isPmuEnabled())
//				// End 
//				{
//					  if (localPmu.sizeOfPmuInputArray() > 0)
//						{
//						  localPmu.setPmuEnable(M9kConstants.ENABLE_ONE);
//						}
//				  }
//				  else
//				  {
//					  localPmu.setPmuEnable(M9kConstants.DISABLE_ZERO);
//				  }
				localPmu.setPmuEnable(M9kConstants.DISABLE_ZERO); // Set disable as default and later in createPdcPmus method it applies logic to enable PMU for each chassis 
			}
			
			// Initialize the trigger count to zero in case of new station creation
			if (getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit"))
			{
				stationDetails.setTotalTriggersConfigured(0);
			}

			
			for (int i = 0; i < getLstTriggerChannels().size(); i++) {
				triggerChannelDTO = getLstTriggerChannels().get(i);
//				// START: 03-Mar-2021 - Implementation of virtual measurements for delta transformers
				// Skip virtual measurement handling as it is handled separately
				if (triggerChannelDTO == null || triggerChannelDTO.getType() == null || triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.VIRTUAL_MEASUREMENT))
				{
					logger.debug("Skipping Virtual measurements ");
					continue;
				}
				// END: 03-Mar-2021 - Implementation of virtual measurements for delta transformers
//				logger.debug("Inside for loop "+triggerChannelDTO);
//				logger.debug("\n\n\n\t\t\t\t\t Freq Status: "+freqPmuId+"\n\n\n");
//				logger.debug("Type of input for trigger  "+triggerChannelDTO.getInputType());
//				logger.debug("triggerChannelDTO status...i ("+i+") --> "+triggerChannelDTO.getStatus());
//				logger.debug("Name to"+triggerChannelDTO.getName());
//				logger.debug("Trigger object... "+triggerChannelDTO);
//				logger.debug("triggerChannelDTO.getType() in populateTriggerChannels..."+triggerChannelDTO.getType());
				if (triggerChannelDTO.getType() == null 
						|| triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.DELETE) 
						|| triggerChannelDTO.getType().equalsIgnoreCase("E")
						)
				{
					if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
					{
						logger.debug("About to delete..."+triggerChannelDTO);
						deleteMeasurement(triggerChannelDTO);
//						logger.debug("Decrementing Trigger ID");
//						if (!deleteMeasurement(triggerChannelDTO))
//						{
//							StationDTO.decrementTriggerId(); // To be decremented only if no measurements is saved earlier
//						}
//						// 30-Mar-2021 - Check for measurement to be deleted in virtual measurement inputs
//						else
//						{
//							logger.debug("Check for virtual measurment inputs..."+triggerChannelDTO);
//							// Check for affected and turn the flag to true even if one of them is affected
//							if (!isVirtualMeasurementAffected)
//							{
//								isVirtualMeasurementAffected = checkVirtualMeasurementsForDeletedMeasurement(triggerChannelDTO);
//							}
//							else
//							{
//								checkVirtualMeasurementsForDeletedMeasurement(triggerChannelDTO);
//							}
//						}
//						// 30-Mar-2021
					}
					continue;
				}
				logger.debug("DFR chassis "+triggerChannelDTO.getChassis());
//				logger.debug("About to process trigger type "+triggerChannelDTO.getInputType());
//				if (triggerChannelDTO.getInputType().equalsIgnoreCase(M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP))
//				{
//					processLineGroupTriggers(triggerChannelDTO);
//					continue;
//				}
//				else
//				{
					logger.debug("In else part DFR map "+Arrays.toString(mapXMLDfrs.keySet().toArray(new String[0])));
					xmlConfigDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
//					logger.debug("xmlConfigDfr "+xmlConfigDfr);
//				}
				if (xmlConfigDfr.getDataPool() == null)
				{
					datapool = xmlConfigDfr.addNewDataPool();
					datapool.setName("Datapool");
					logger.debug("Creating Algorithm");
					algorithms = datapool.addNewAlgorithms();
					algorithms.setName("Algorithms");
					triggers = algorithms.addNewTriggers();
					triggers.setName("Global Trigger");
				}
				else
				{
					algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
					if (algorithms == null)
					{
							logger.debug("Creating Algorithm");
							algorithms = xmlConfigDfr.getDataPool().addNewAlgorithms();
							algorithms.setName("Algorithms");
							triggers = algorithms.addNewTriggers();
							triggers.setName("Global Trigger");
					}
					else
					{
						triggers = algorithms.getTriggers();
						if (triggers == null)
						{
							triggers = algorithms.addNewTriggers();
							triggers.setName("Global Trigger");							
						}
					}
				}
				
//				if (getTriggerOutHold() != null && !getTriggerOutHold().isEmpty())
//				{
//					triggers.setTriggerOutHold(Integer.parseInt(getTriggerOutHold()));
//				}
//				else
//				{
//					triggers.setTriggerOutHold(Integer.parseInt(getTriggerOutHold()));
//				}
				AbstractTrigger genericTrigger = populateTrigger(triggerChannelDTO);
				if (genericTrigger == null)
				{
					logger.error("This particular measurement is skipped as it is not supported yet"+triggerChannelDTO);
					continue;
				}
//				if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
//				{
//					triggerChannelDTO.setChassis("DFR"+xmlConfigDfr.getSystem().getDfrId());
//					deleteMeasurement(triggerChannelDTO);
//				}
				
				//START: 24-Jan-2020 - Implementing Transducer 
				// 23-Jul-2020 - Bug Fix: Units value messes the algorithm
				if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.AVERAGE) || triggerChannelDTO.isTransducer())
				{
					logger.debug("Is transducer set ? "+triggerChannelDTO.isTransducer());
					triggerChannelDTO.setUnits(getUnitsForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
					Average average = getAverageIfAlreadyExists(genericTrigger);
					if (average == null)
					{
						average = algorithms.addNewAverage();
					}
					average.setName(genericTrigger.getName());
					average.setInput(genericTrigger.getInputValue());
					if (triggerChannelDTO.getAverage() != null)
					{
						average.setAverage(triggerChannelDTO.getAverage());
					}			
					// START: 18-Mar-2021 - Set description for all algorithm
					average.setDescription(triggerChannelDTO.getName());
					// END: 18-Mar-2021 - Set description for all algorithm

				}
				//END 23-Jul-2020

				else if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.RMS))
				{
//					triggerChannelDTO.setUnits("V");
					triggerChannelDTO.setUnits(getUnitsForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
					Rms rms = getRmsIfAlreadyExists(genericTrigger);
					if (rms == null)
					{
						rms = algorithms.addNewRms();
					}
					rms.setName(genericTrigger.getName());
					rms.setInput(genericTrigger.getInputValue());
					if (triggerChannelDTO.getAverage() != null)
					{
						rms.setAverage(triggerChannelDTO.getAverage());
					}
					// START: 18-Mar-2021 - Set description for all algorithm
					rms.setDescription(triggerChannelDTO.getName());
					// END: 18-Mar-2021 - Set description for all algorithm
				}
				else if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.HARMONIC))
				{
//					triggerChannelDTO.setUnits("V");
					triggerChannelDTO.setUnits(getUnitsForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
					triggerChannelDTO.setPhase(getPhaseForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
					if (triggerChannelDTO.getStatus())
					{
						triggerChannelDTO.setDisableHarmonic(false);
					}
					else
					{
						triggerChannelDTO.setDisableHarmonic(true);
					}
					Magnitude magnitude = getMagnitudeIfAlreadyExists(genericTrigger);
					if (magnitude == null)
					{
						magnitude = algorithms.addNewMagnitude();
					}
					magnitude.setName(genericTrigger.getName());
					magnitude.setInput(triggerChannelDTO.getTriggerChannelInput());
					if (((MagnitudeTrigger)genericTrigger).getHarmonic() != null)
					{
						magnitude.setHarmonic(Integer.parseInt(((MagnitudeTrigger)genericTrigger).getHarmonic()));
					}
					else
					{
						magnitude.setHarmonic(1);
					}
					if (triggerChannelDTO.getAverage() != null)
					{
						magnitude.setAverage(triggerChannelDTO.getAverage());
					}
					// START: 18-Mar-2021 - Set description for all algorithm
					magnitude.setDescription(triggerChannelDTO.getName());
					// END: 18-Mar-2021 - Set description for all algorithm
				}
				else if (triggerChannelDTO.getType().equals(M9kConstants.ZERO_SEQUENCE_V) ||
						triggerChannelDTO.getType().equals(M9kConstants.ZERO_SEQUENCE_I))
				{
					if (triggerChannelDTO.getType().equals(M9kConstants.ZERO_SEQUENCE_V))
					{
						triggerChannelDTO.setUnits("V");
					}
					else
					{
						triggerChannelDTO.setUnits("A");
					}
					ZeroSequence zeroSequence = getZeroSequenceIfAlreadyExists(genericTrigger);
					if (zeroSequence == null)
					{
						zeroSequence = algorithms.addNewZeroSequence();
					}
					zeroSequence.setName(genericTrigger.getName());
					zeroSequence.setInput(genericTrigger.getInputValue());
					zeroSequence.setInputType(((SequenceTrigger)genericTrigger).getInputType());
					if (triggerChannelDTO.getAverage() != null)
					{
						zeroSequence.setAverage(triggerChannelDTO.getAverage());
					}
					// START: 18-Mar-2021 - Set description for all algorithm
					zeroSequence.setDescription(triggerChannelDTO.getName());
					// END: 18-Mar-2021 - Set description for all algorithm
				}
				else if (triggerChannelDTO.getType().startsWith(M9kConstants.POSITIVE_SEQUENCE_V) || 
						triggerChannelDTO.getType().startsWith(M9kConstants.POSITIVE_SEQUENCE_I))
				{
					if (triggerChannelDTO.getType().equals(M9kConstants.POSITIVE_SEQUENCE_V))
					{
						triggerChannelDTO.setUnits("V");
					}
					else
					{
						triggerChannelDTO.setUnits("A");
					}
					PositiveSequence positiveSequence = getPositiveSequenceIfAlreadyExists(genericTrigger);
					if (positiveSequence == null)
					{
						positiveSequence = algorithms.addNewPositiveSequence();
					}
					positiveSequence.setName(genericTrigger.getName());
					positiveSequence.setInput(genericTrigger.getInputValue());
					positiveSequence.setInputType(((SequenceTrigger)genericTrigger).getInputType());
					if (triggerChannelDTO.getAverage() != null)
					{
						positiveSequence.setAverage(triggerChannelDTO.getAverage());
					}
					// START: 18-Mar-2021 - Set description for all algorithm
					positiveSequence.setDescription(triggerChannelDTO.getName());
					// END: 18-Mar-2021 - Set description for all algorithm
				}
				else if (triggerChannelDTO.getType().startsWith(M9kConstants.NEGATIVE_SEQUENCE_V) || 
						triggerChannelDTO.getType().startsWith(M9kConstants.NEGATIVE_SEQUENCE_I))
				{
					if (triggerChannelDTO.getType().equals(M9kConstants.NEGATIVE_SEQUENCE_V))
					{
						triggerChannelDTO.setUnits("V");
					}
					else
					{
						triggerChannelDTO.setUnits("A");
					}
					NegativeSequence negativeSequence = getNegativeSequenceIfAlreadyExists(genericTrigger);
					if (negativeSequence == null)
					{
						negativeSequence = algorithms.addNewNegativeSequence();
					}
					negativeSequence.setName(genericTrigger.getName());
					negativeSequence.setInput(genericTrigger.getInputValue());
					negativeSequence.setInputType(((SequenceTrigger)genericTrigger).getInputType());
					if (triggerChannelDTO.getAverage() != null)
					{
						negativeSequence.setAverage(triggerChannelDTO.getAverage());
					}
					// START: 18-Mar-2021 - Set description for all algorithm
					negativeSequence.setDescription(triggerChannelDTO.getName());
					// END: 18-Mar-2021 - Set description for all algorithm
				}
				else if (triggerChannelDTO.getType().indexOf(M9kConstants.WATTS) != -1 ||
						(triggerChannelDTO.getType().indexOf(M9kConstants.VARS) != -1)
						// START: 17-Mar-2021 - Apparent power implementation
						|| (triggerChannelDTO.getType().toLowerCase().indexOf(M9kConstants.VA_APPARENT.toLowerCase()) != -1)
						// END: 17-Mar-2021 - Apparent power implementation
						)
				{
					// START: 17-Mar-2021 - Apparent power implementation
					if (triggerChannelDTO.getType().indexOf(M9kConstants.WATTS) != -1)
					{
						triggerChannelDTO.setUnits(M9kConstants.WATTS_UNITS);
					}
					else if (triggerChannelDTO.getType().indexOf(M9kConstants.VARS) != -1)
					{
						triggerChannelDTO.setUnits(M9kConstants.VARS_UNITS);
					}
					else 
					{
						triggerChannelDTO.setUnits(M9kConstants.VA_UNITS);
					}
					Power power = getPowerIfAlreadyExists(genericTrigger);
					if (power == null)
					{
						power = algorithms.addNewPower();
					}
					if (triggerChannelDTO.getType().equals(M9kConstants.A_PHASE_WATTS) ||
							(triggerChannelDTO.getType().equals(M9kConstants.A_PHASE_VARS))
							|| (triggerChannelDTO.getType().equals(M9kConstants.A_PHASE_APPARENT)))
					{
						power.setInputs("A");
					}
					else if (triggerChannelDTO.getType().equals(M9kConstants.B_PHASE_WATTS) ||
							(triggerChannelDTO.getType().equals(M9kConstants.B_PHASE_VARS))
							|| (triggerChannelDTO.getType().equals(M9kConstants.B_PHASE_APPARENT)))
					{
						power.setInputs("B");
					}
					else if (triggerChannelDTO.getType().equals(M9kConstants.C_PHASE_WATTS) ||
							(triggerChannelDTO.getType().equals(M9kConstants.C_PHASE_VARS))
							|| (triggerChannelDTO.getType().equals(M9kConstants.C_PHASE_APPARENT)))
					{
						power.setInputs("C");
					}
					else 
					{
						power.setInputs("ALL");
					}
					// END: 17-Mar-2021 - Apparent power implementation
					power.setName(genericTrigger.getName());
					power.setPowerType(((PowerTrigger)genericTrigger).getPowerType());
					power.setInput(genericTrigger.getInputValue());
					// START: 18-Mar-2021 - Set description for all algorithm
					power.setDescription(triggerChannelDTO.getName());
					// END: 18-Mar-2021 - Set description for all algorithm
				}
				else if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.FREQUENCY))
				{
					triggerChannelDTO.setUnits("Hz");
					triggerChannelDTO.setPhase(getPhaseForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
//					if (triggerChannelDTO.getStatus())
//					{
//						triggerChannelDTO.setDisableHarmonic(false);
//					}
//					else
//					{
//						triggerChannelDTO.setDisableHarmonic(true);
//					}
//					Phasor phasor = getPhasorIfAlreadyExists(genericTrigger);
//					if (phasor == null)
//					{
//						phasor = algorithms.addNewPhasor();
//						phasor.setName(((FrequencyTrigger)genericTrigger).getPhaseName());
//						phasor.setInput(triggerChannelDTO.getTriggerChannelInput());
					Frequency frequency = getFrequencyIfAlreadyExists(genericTrigger);
					if (frequency == null)
					{
						frequency = algorithms.addNewFrequency();
					}
					frequency.setName(genericTrigger.getName());
					frequency.setInput(triggerChannelDTO.getTriggerChannelInput());
					if (triggerChannelDTO.getAverage() != null)
					{
						frequency.setAverage(triggerChannelDTO.getAverage());
					}
					else
					{
						logger.debug("TESTAverage: Average is null "+triggerChannelDTO);
					}
					// START: 18-Mar-2021 - Set description for all algorithm
					frequency.setDescription(triggerChannelDTO.getName());
					// END: 18-Mar-2021 - Set description for all algorithm
//						frequency.setInput(((FrequencyTrigger)genericTrigger).getFrequencyInputXMLString(((FrequencyTrigger)genericTrigger).getPhaseName()));
//					}
				}
				else if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.PHASOR))
				{
					triggerChannelDTO.setUnits(M9kConstants.PHASOR);
					triggerChannelDTO.setPhase(getPhaseForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
					Phase phase = getPhaseIfAlreadyExists(genericTrigger);
					if (phase == null)
					{
						phase = algorithms.addNewPhase();
					}
					phase.setName(genericTrigger.getName());
					phase.setInput(triggerChannelDTO.getTriggerChannelInput());
					if (triggerChannelDTO.getAverage() != null)
					{
						phase.setAverage(triggerChannelDTO.getAverage());
					}
					// START: 18-Mar-2021 - Set description for all algorithm
					phase.setDescription(triggerChannelDTO.getName());
					// END: 18-Mar-2021 - Set description for all algorithm
				}

				limits = null;
				limitsName = genericTrigger.getLimitsAlgorithm().getName();
				for (int j = 0; j < algorithms.sizeOfLimitsArray(); j++) {
					if (algorithms.getLimitsArray(j).getName().equalsIgnoreCase(limitsName)){
//						limits = algorithms.getLimitsArray(j);
						algorithms.removeLimits(j);
						break;
					}
				}
				if (!triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.PHASOR))
				{
					if (limits == null)
					{
						limits = algorithms.addNewLimits();
						limits.setName(limitsName);
					}
					limits.setInput(genericTrigger.getLimitsAlgorithm().getInputValue());
					if (genericTrigger.getLimitsAlgorithm().getHighLimit() != null && !genericTrigger.getLimitsAlgorithm().getHighLimit().isEmpty())
					{
						limits.setHighLimit(genericTrigger.getLimitsAlgorithm().getHighLimit());
					}
					if (genericTrigger.getLimitsAlgorithm().getLowLimit() != null && !genericTrigger.getLimitsAlgorithm().getLowLimit().isEmpty())
					{	
						limits.setLowLimit(genericTrigger.getLimitsAlgorithm().getLowLimit());
					}
					
					// START: 26-Feb-2020 - ROC positive and Negative Limits implementation
					if (genericTrigger.getLimitsAlgorithm().getDuration() != null 
							&& !genericTrigger.getLimitsAlgorithm().getDuration().isEmpty() 
							&& Integer.parseInt(genericTrigger.getLimitsAlgorithm().getDuration()) > 0)
					{
						limits.setDuration(genericTrigger.getLimitsAlgorithm().getDuration());
						limits.setRateOfChangeLimitNeg(genericTrigger.getLimitsAlgorithm().getRateOfChangeLimitNeg());
						limits.setRateOfChangeLimitPos(genericTrigger.getLimitsAlgorithm().getRateOfChangeLimitPos());
					}
					
					// START: 18-Mar-2021 - Set description for all algorithm
					limits.setDescription(triggerChannelDTO.getName());
					// END: 18-Mar-2021 - Set description for all algorithm
					// 28-July-2015 START: Implementation of rate of change trigger
//					if (genericTrigger.getLimitsAlgorithm().getRateOfChangeLimit() != null && !genericTrigger.getLimitsAlgorithm().getRateOfChangeLimit().isEmpty() )
//					{
//						limits.setRateOfChangeLimit(genericTrigger.getLimitsAlgorithm().getRateOfChangeLimit());
//					}
//					if (genericTrigger.getLimitsAlgorithm().getDuration() != null && !genericTrigger.getLimitsAlgorithm().getDuration().isEmpty() )
//					{
//						limits.setDuration(genericTrigger.getLimitsAlgorithm().getDuration());
//					}
					// 28-July-2015 END: Implementation of rate of change trigger
					// END: 26-Feb-2020
					
	//				logger.debug("trigger to be set..."+genericTrigger.getTrigger()+" Input..."+genericTrigger.getTrigger().getInput());
					int j = 0;
					for (; j < triggers.sizeOfTriggerArray(); j++) {
						if(triggers.getTriggerArray(j).getName().equalsIgnoreCase(genericTrigger.getTrigger().getName()))
						{
							break;
						}
					}
					if (j == triggers.sizeOfTriggerArray())
					{
						triggers.addNewTrigger();
					}
					triggers.setTriggerArray(j, genericTrigger.getTrigger());
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
				// Start 14-Oct-2013 Introduced exportName attribute to Export in xml
				export.setExportName(triggerChannelDTO.getName());
				// End 14-Oct-2013

				// Start 08-Jul-2015 Added Measurement type to xml to be added in hte cont table
				if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.HARMONIC))
				{
//					System.out.println("Algorithm type "+triggerChannelDTO.getType());
					if (((MagnitudeTrigger)genericTrigger).getHarmonic() != null)
					{
//						System.out.println("Harmonic number "+Integer.parseInt(((MagnitudeTrigger)genericTrigger).getHarmonic()));
						export.setMeasurementType(triggerChannelDTO.getType()+"-"+Integer.parseInt(((MagnitudeTrigger)genericTrigger).getHarmonic()));
					}
					else
					{
						export.setMeasurementType(triggerChannelDTO.getType());
					}
					
				}
				else
				{
					export.setMeasurementType(triggerChannelDTO.getType());
				}
				// End 08-Jul-2015
				
				export.setPhase(triggerChannelDTO.getPhase());
				export.setUnits(triggerChannelDTO.getUnits());
				export.setInput(triggerChannelDTO.getExportXMLInputString(genericTrigger));
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
				localPmuInput.setInput(genericTrigger.getInputXMLString().replace(M9kXMLConstants.XML_PHASOR_VALUE_INPUT, M9kXMLConstants.XML_VALUE_INPUT));
				localPmuInput.setPmuInputType(genericTrigger.getPmuInputType());
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
			
			if (getStationDetails().isPmuEnabled())
			{
				createPdcPmus();
			}
//			if (booTriggersExist)
//			{
//				triggers.setTriggerOutHold(Integer.parseInt(getTriggerOutHold()));
//			}
//			else
//			{
				session.remove("TriggerOutHold");
//			}
			logger.debug("Returning populateTriggerChannels..."+booTriggersExist+" isVirtualMeasurementAffected "+isVirtualMeasurementAffected);
//			logger.debug("Returning from populateTriggerChannels with substation "+currentSubstation);
			
//			// START: 25-Nov-2024 - Cleanup virtual measurements for all deleted measurements
//			if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
//			{
//				lstDeletedMeasurements = (List<TriggerChannelDTO>) session.get("lstDeletedMeasurements");
//				if (lstDeletedMeasurements != null && lstDeletedMeasurements.size() > 0)
//				for (TriggerChannelDTO deletedTriggerChannelDTO : lstDeletedMeasurements)
//				{
//					logger.debug("Check for virtual measurment inputs..."+deletedTriggerChannelDTO);
//					// Check for affected and turn the flag to true even if one of them is affected
//					if (!isVirtualMeasurementAffected)
//					{
//						isVirtualMeasurementAffected = checkVirtualMeasurementsForDeletedMeasurement(deletedTriggerChannelDTO);
//					}
//					else
//					{
//						checkVirtualMeasurementsForDeletedMeasurement(deletedTriggerChannelDTO);
//					}
//				}
//			}
//			// END: Cleanup virtual measurements for all deleted measurements
			// As by default implicitly created RMS and its exports were also cleared..we'll recreate
			recreateLtrRmsAlgorithmAndExport();

			return isVirtualMeasurementAffected;
		}

		/**
		 * START: 4-Nov-2024 - Remove zombie measurements if any
		 * 
		 * 
		 */		
		private void removeZombieMeasurementsAndPmus() {
			 if (getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit"))
			  {
				 return;
			  }
			
			  Measurements measurements = currentSubstation.getMeasurements();
			  Measurement[] arrMeasurement = null;
			  Exports exports = null;
			  Map<String, Export> mapExportsXml = new HashMap<String, Export>();
			  pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
			  logger.debug("mapXMLDfrs size "+mapXMLDfrs.size());
			  for (int dfrCnt = 1; dfrCnt <= mapXMLDfrs.size(); dfrCnt++) {
				  xmlConfigDfr = mapXMLDfrs.get("DFR"+dfrCnt);
				  logger.debug("xmlConfigDfr "+xmlConfigDfr.getId());
				  exports = xmlConfigDfr.getDataPool().getExports();
				  if (exports == null)
				  {
					 continue;
				  }
				  Export[] arrExportXml = exports.getExportArray();
				  logger.debug("ExportsXml size "+arrExportXml.length);
				  for (int i = 0; i < arrExportXml.length; i++) {
					  mapExportsXml.put(arrExportXml[i].getId() + "", arrExportXml[i]);
				  }
				  logger.debug("Inside Loop mapExportsXml "+mapExportsXml.size());
			  }
			  logger.debug("Final mapExportsXml "+mapExportsXml.size());
			  arrMeasurement = measurements.getMeasurementArray();
			  ArrayList<Measurement> lstMeasurement = new ArrayList<Measurement>(Arrays.asList(arrMeasurement));
			  Export exportXmlObj = null;
			  Iterator<Measurement> it = lstMeasurement.iterator();
			  Measurement measurement = null;
			  while (it.hasNext()) {
				  measurement = it.next();
				  exportXmlObj = mapExportsXml.get(measurement.getId() + "");
				  if (exportXmlObj == null)
				  {
					  logger.info("exportXmlObj is null for measurement id "+measurement.getId()+". Removing measurement "+measurement.getName());
					  it.remove();
				  }
			  }
			  measurements.setMeasurementArray((Measurement[])lstMeasurement.toArray(new Measurement[lstMeasurement.size()]));
			  logger.debug("measurements size "+measurements.getMeasurementArray().length);
			  
			  // START: 12-Dec-2024 - Remove zombie PMU elements as well
			  // Remove zombie PMUs along with Measurements
				PmuInput[] arrPmuInput = pmuInputs.getPmuInputArray();
				ArrayList<PmuInput> lstPmuInputs = new ArrayList<PmuInput>(Arrays.asList(arrPmuInput));
				Iterator<PmuInput> itPmuInput= lstPmuInputs.iterator();
				PmuInput pmuInput = null;
				while (itPmuInput.hasNext())
				{
					pmuInput = itPmuInput.next();
					exportXmlObj = mapExportsXml.get(pmuInput.getId() + "");
					if (exportXmlObj == null)
					  {
						  logger.info("exportXmlObj is null for PMU id "+pmuInput.getId()+". Removing PMU "+pmuInput.getName());
						  itPmuInput.remove();
					  }
				}
				pmuInputs.setPmuInputArray((PmuInput[]) lstPmuInputs.toArray(new PmuInput[lstPmuInputs.size()]));
				
			  logger.debug("Pmu size "+pmuInputs.getPmuInputArray().length);
		}

		

		private void createPdcPmus()
		{
			List<String> lstDfrNames = new ArrayList<String>(mapXMLDfrs.keySet());
			Collections.sort(lstDfrNames, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					int lhs = Integer.parseInt(o1.substring(3));
					int rhs = Integer.parseInt(o2.substring(3));
//					return ((lhs < rhs)?0:1);
					return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
				}
			});
			Iterator<String> mapXmlDfrIterator = lstDfrNames.iterator();
			String dfrKey;
			Algorithms algorithms = null;
			Pmu localPmu = null;
			Pdc pdc =  currentSubstation.getPdc();
			Pmus pdcPmus = pdc.getPmus();
			if (pdcPmus == null)
			{
				pdcPmus = pdc.addNewPmus();
			}
			Pmu pdcPmu;

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
//					  logger.debug("pdcPmus.sizeOfPmuArray() "+pdcPmus.sizeOfPmuArray()+" XML Content "+pdcPmus.xmlText());
					  for (int i = 0; pdcPmus != null && i < pdcPmus.sizeOfPmuArray(); i++) {
//						  logger.debug("PDC Pmu index i= "+i+"Pmu xml content "+pdcPmus.getPmuArray(i).xmlText());
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
//					  logger.debug("About to set PMU stream type "+stationDetails.getPmuStreamType());
					  pdcPmu.setPmuStreamType(stationDetails.getPmuStreamType());
//					  pdcPmu.setPmuPhasorMode(stationDetails.getPmuPhasorMode());
//					  pdcPmu.setPmuDataRate(stationDetails.getPmuDataRate());
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
		private void recreateLtrRmsAlgorithmAndExport()
		{
			logger.debug("In createRmsAlgorithmAndExport to create default RMS and exports for long term rms "+lstVirtualChannels);
			DFR xmlDfr = null;
			AnalogChannelDTO analogChannelDTO;
			int expId;
			Exports exports;
			Export export;
			RMSAlgorithm rmsAlgorithm;
			Rms rms;
			boolean isAlreadyExported = false;
			// Merging analogs and virtuals to recreate RMS exports
			List<AnalogChannelDTO> lstEveryAnalogs = new ArrayList<AnalogChannelDTO>(lstAnalogChannels);
			if (lstVirtualChannels != null)
			{
				lstEveryAnalogs.addAll(lstVirtualChannels);
			}
			for (int i = 0; i < lstEveryAnalogs.size(); i++) {
				analogChannelDTO = lstEveryAnalogs.get(i);
				// START: 27-Jan-2020 - Transducer implementation - Do not create RMS for transducer channels to the 
				if (analogChannelDTO.isTransducer())
				{
					continue;
				}
				// END: 27-Jan-2020
				xmlDfr = mapXMLDfrs.get(analogChannelDTO.getChassis());
				exports = xmlDfr.getDataPool().getExports();
				// START: 05-Feb-2020 - Transducer implementation - Do not create RMS if already exists
				rms = getRmsIfAlreadyExists(analogChannelDTO); 
				if (rms != null)
				{
					String exportName = "RMS-"+analogChannelDTO.getChannel()+"-"+analogChannelDTO.getName();
					removeDefaultExportsIfExists(exports, exportName); // Remove any default RMS export as it is already exported
					continue;
				}
				// END: 05-Feb-2020
				rmsAlgorithm = new RMSAlgorithm(xmlDfr, analogChannelDTO);
				rms = rmsAlgorithm.createRmsXmlObj();
//				logger.debug("Rms "+rms.getName()+" Input "+rms.getInput());
				String exportName = "RMS-"+analogChannelDTO.getChannel()+"-"+analogChannelDTO.getName();
				if (exports == null)
				{
						exports = xmlDfr.getDataPool().addNewExports();
						exports.setName("Exports");
				}
				isAlreadyExported = isAlreadyExported(exports, analogChannelDTO.getName()); // Compare just the analog name as prefix channel id can be diffrerent
				if (isAlreadyExported )
				{
					removeDefaultExportsIfExists(exports, exportName); // Backward compatibility. Remove from already created old configs 
					continue;
				}

				export = exports.addNewExport();
				export.setName(exportName);
				// START 16-Mar-2018 Remove the prefix and just display circuit name
				// Start 14-Oct-2013 Introduced exportName attribute to Export in xml
//				export.setExportName(analogChannelDTO.getDisplayChannel()+"-"+analogChannelDTO.getCircuitName());
				// End 14-Oct-2013
				export.setExportName(analogChannelDTO.getCircuitName());
				// End 16-Mar-2018 Remove the prefix and just display circuit name
				
				// Start 08-Jul-2015 Added Measurement type to xml to be added in hte cont table
				export.setMeasurementType(M9kConstants.RMS);
				// End 08-Jul-2015
				
				if (analogChannelDTO.getInputType().startsWith(M9kConstants.VOLTAGE))
				{
					export.setUnits("V");
				}
				else
				{
					export.setUnits("A");
				}
				expId = (xmlDfr.getId()*M9kConstants.LTR_RMS_OFFSET)+Integer.parseInt(analogChannelDTO.getChannel());
				logger.debug("DEFAULT LTR RMS expId "+expId);
				export.setId(expId);
				export.setPhase(analogChannelDTO.getPhase());
				export.setInput(getExportXMLInputString(rms.getName()));
				export.setSampleRate(stationDetails.getExportRate());
				logger.debug("Export created "+export.xmlText());
			}
//			logger.debug("XML DFR after implicit exports "+xmlDfr.xmlText());
//			algorithms.addNewRms();
//			algorithms.setRmsArray((algorithms.getRmsArray().length-1), rms);
			
		}
		
		// This is required if user creates or modifies a virtual channel and then goes ahead and does finish and send without visiting triggers page
		private void recreateLtrRmsAlgorithmAndExportForVirtualsOnly()
		{
			logger.debug("In recreateLtrRmsAlgorithmAndExportForVirtualsOnly to create default RMS and exports for long term rms "+lstVirtualChannels);
			DFR xmlDfr = null;
			AnalogChannelDTO analogChannelDTO;
			int expId;
			Exports exports;
			Export export;
			RMSAlgorithm rmsAlgorithm;
			Rms rms;
			boolean isAlreadyExported = false;
			// Merging analogs and virtuals to recreate RMS exports
			for (int i = 0; i < lstVirtualChannels.size(); i++) {
				analogChannelDTO = lstVirtualChannels.get(i);
				xmlDfr = mapXMLDfrs.get(analogChannelDTO.getChassis());
				exports = xmlDfr.getDataPool().getExports();
				// START: 05-Feb-2020 - Transducer implementation - Do not create RMS if already exists
				rms = getRmsIfAlreadyExists(analogChannelDTO); 
				if (rms != null)
				{
					String exportName = "RMS-"+analogChannelDTO.getChannel()+"-"+analogChannelDTO.getName();
					removeDefaultExportsIfExists(exports, exportName); // Remove any default RMS export as it is already exported
					continue;
				}
				// END: 05-Feb-2020
				
				rmsAlgorithm = new RMSAlgorithm(xmlDfr, analogChannelDTO);
				rms = rmsAlgorithm.createRmsXmlObj();
//				logger.debug("Rms "+rms.getName()+" Input "+rms.getInput());
				exports = xmlDfr.getDataPool().getExports();
				String exportName = "RMS-"+analogChannelDTO.getChannel()+"-"+analogChannelDTO.getName();
				if (exports == null)
				{
						exports = xmlDfr.getDataPool().addNewExports();
						exports.setName("Exports");
				}
				logger.debug("Exports in the loop "+exports.xmlText());
				isAlreadyExported = isAlreadyExported(exports, analogChannelDTO.getName()); // Compare just the analog name as prefix channel id can be diffrerent
				if (isAlreadyExported )
				{
					continue;
				}

				export = exports.addNewExport();
				export.setName(exportName);
				// START 16-Mar-2018 Remove the prefix and just display circuit name
				// Start 14-Oct-2013 Introduced exportName attribute to Export in xml
//				export.setExportName(analogChannelDTO.getDisplayChannel()+"-"+analogChannelDTO.getCircuitName());
				// End 14-Oct-2013
				export.setExportName(analogChannelDTO.getCircuitName());
				// END 16-Mar-2018 Remove the prefix and just display circuit name
				// Start 08-Jul-2015 Added Measurement type to xml to be added in hte cont table
				export.setMeasurementType(M9kConstants.RMS);
				// End 08-Jul-2015
				
				if (analogChannelDTO.getInputType().startsWith(M9kConstants.VOLTAGE))
				{
					export.setUnits("V");
				}
				else
				{
					export.setUnits("A");
				}
				expId = (xmlDfr.getId()*M9kConstants.LTR_RMS_OFFSET)+Integer.parseInt(analogChannelDTO.getChannel());
				export.setId(expId);
				export.setPhase(analogChannelDTO.getPhase());
				export.setInput(getExportXMLInputString(rms.getName()));
				export.setSampleRate(stationDetails.getExportRate());
				logger.debug("Export object created "+export.xmlText());
			}
//			logger.debug("XML DFR after implicit exports "+xmlDfr.xmlText());
//			algorithms.addNewRms();
//			algorithms.setRmsArray((algorithms.getRmsArray().length-1), rms);
			
		}

		private void removeDefaultExportsIfExists(Exports exports, String exportName)
		{
			logger.debug("Export Name to be compared "+exportName);
			Export[] arrExport = exports.getExportArray();
			for (int i = 0; i < arrExport.length; i++) {
//				logger.debug("arrExport[i].getName() "+arrExport[i].getName());
				if (arrExport[i].getName().equalsIgnoreCase(exportName))
				{
					exports.removeExport(i);
					break;
				}
			}
		}
		private boolean isAlreadyExported(Exports exports, String exportName)
		{
			logger.debug("Export Name to be compared "+exportName+" xml content "+exports.xmlText());
			boolean isExported = false;
			String algorithmType;
			Export[] arrExport = exports.getExportArray();
			for (int i = 0; i < arrExport.length; i++) {
//				logger.debug("arrExport[i].getName() "+arrExport[i].getName()+" if ocndition "+arrExport[i].getName().substring(arrExport[i].getName().lastIndexOf("-")+1));
				if (arrExport[i].getName().substring(arrExport[i].getName().lastIndexOf("-")+1).equalsIgnoreCase(exportName)) // Compare without RMS- prefix in the name
				{
					logger.debug(arrExport[i].getInput());
					algorithmType = arrExport[i].getInput().substring(M9kXMLConstants.XML_ALGORITHMS_INPUT.length(), arrExport[i].getInput().indexOf("(",M9kXMLConstants.XML_ALGORITHMS_INPUT.length()));
					if (algorithmType.equalsIgnoreCase(M9kConstants.RMS))
					{
						isExported = true;
						break;						
					}
				}
			}
			logger.debug(exportName +" exported already??? "+isExported);
			return isExported;
		}

		private  String getExportXMLInputString(String name)
		{
			StringBuffer inputTag = new StringBuffer();
			inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
			inputTag.append(M9kConstants.RMS+"(");
			inputTag.append(name);
			inputTag.append(M9kXMLConstants.EXPORT_XML_VALUE_INPUT);
			return inputTag.toString();
		}

		@SuppressWarnings("unused")
		private Export getExportsIfAlreadyExists(TriggerChannelDTO triggerChannel)
		{
			Collection<DFR> lstXmlDfrs = mapXMLDfrs.values();
			DFR xmlDfr = null;
			Exports exports1;
			Export export = null;
			for (Iterator<DFR> iterator = lstXmlDfrs.iterator(); iterator.hasNext();) {
				xmlDfr = iterator.next();
				if (xmlDfr.getDataPool() == null)
				{
					continue;
				}
				exports1 = xmlDfr.getDataPool().getExports();
				if (exports1 == null)
				{
					continue;
				}
//				logger.debug("Export Id to be compared "+triggerChannel.getId());
				Export[] arrExport = exports1.getExportArray();
				for (int i = 0; i < arrExport.length; i++) {
//					logger.debug("arrExport[i].getId() "+arrExport[i].getId()+"arrExport[i].getName() "+arrExport[i].getName()+" new measurement name "+triggerChannel.getMeasurementName());
					if ((arrExport[i].getId() == triggerChannel.getId()) || (arrExport[i].getName().equalsIgnoreCase("RMS-"+triggerChannel.getMeasurementName())))
					{
						if (triggerChannel.getChassis().equalsIgnoreCase("DFR"+xmlDfr.getSystem().getDfrId()))
						{
							
							if (arrExport[i].getName().equalsIgnoreCase(triggerChannel.getMeasurementName()))
							{
								export = arrExport[i];
							}
							else if (arrExport[i].getName().equalsIgnoreCase("RMS-"+triggerChannel.getMeasurementName()))
							{
								export = arrExport[i];
							}
							else
							{
								export = null;
								deleteMeasurementUsingExport(xmlDfr, arrExport[i]);
							}
						}
						else
						{
							export = null;
							deleteMeasurementUsingExport(xmlDfr, arrExport[i]);
						}
						break;
					}
				}
			}
			return export;
		}
		
		

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
		
		
		@SuppressWarnings("unused")
		// To support global triggers in the future
		private void processLineGroupTriggers(TriggerChannelDTO triggerChannelDTO) {
			GroupTriggers groupTriggers = currentSubstation.getGroupTriggers();
			if (groupTriggers == null)
			{
				groupTriggers = currentSubstation.addNewGroupTriggers();
				groupTriggers.setName("GroupTriggers");
			}
//			groupTriggers.setTriggerOutHold(Integer.parseInt(getTriggerOutHold()));
			GroupTrigger groupTrigger = groupTriggers.addNewGroupTrigger();
			groupTrigger.setName("LGT_"+triggerChannelDTO.getMeasurementName());
			Integer triggerId;
			if ((getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit")) || triggerChannelDTO == null || triggerChannelDTO.getId() == null || triggerChannelDTO.getId() == -1)
			{
				triggerId = StationDTO.getNextAvailableTriggerId();
				triggerChannelDTO.setId(triggerId);
			}
			else
			{
				triggerId = triggerChannelDTO.getId();
			}
			groupTrigger.setId(triggerChannelDTO.getId());
			if (triggerChannelDTO.getType().indexOf(M9kConstants.VOLTAGE) > -1)
			{
				triggerChannelDTO.setUnits("V");
				groupTrigger.setInputType(M9kConstants.VOLTAGE);
			}
			else
			{
				triggerChannelDTO.setUnits("A");
				groupTrigger.setInputType(M9kConstants.CURRENT);
			}
			groupTrigger.setType(triggerChannelDTO.getType());
			if (triggerChannelDTO.getGlobalLineGroupId() != null)
			{
				groupTrigger.setLineGroupId(triggerChannelDTO.getGlobalLineGroupId());
			}
			else
			{
				
			}
			if (triggerChannelDTO.getStatus())
			{
				groupTrigger.setEnable(M9kConstants.ENABLE_ONE);
			}
			else
			{
				groupTrigger.setEnable(M9kConstants.DISABLE_ZERO);
			}
			groupTrigger.setLineGroupName(triggerChannelDTO.getMeasurementName());
			groupTrigger.setChatterLimit(Integer.parseInt(triggerChannelDTO.getChatterLimit()));
			groupTrigger.setChatterRate(Double.parseDouble(triggerChannelDTO.getChatterRate()));
			if (triggerChannelDTO.getTripIfOver() != null && (triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.BOTH) || triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.OVER)))
			{
				groupTrigger.setHighLimit(triggerChannelDTO.getTripIfOver());
			}
			if (triggerChannelDTO.getTripIfUnder() != null && (triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.BOTH) || triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.UNDER)))
			{
				groupTrigger.setLowLimit(triggerChannelDTO.getTripIfUnder());
			}
			
			if (triggerChannelDTO.getExportStatus())
			{
				groupTrigger.setExport(M9kConstants.ENABLE_ONE);
//				groupTrigger.setExportRate(triggerChannelDTO.getExportRate());
				groupTrigger.setExportRate(stationDetails.getPmuDataRate());
			}
			else
			{
				groupTrigger.setExport(M9kConstants.DISABLE_ZERO);
			}
			logger.debug("Created group trigger and continuing");
			
		}

		private void deleteMeasurementUsingExport(DFR xmlDfr, Export export) {
			TriggerChannelDTO triggerChannelToDelete = new TriggerChannelDTO();
			triggerChannelToDelete.setId(export.getId());
			triggerChannelToDelete.setChassis("DFR"+xmlDfr.getSystem().getDfrId());
			triggerChannelToDelete.setMeasurementName(export.getName());
			deleteMeasurement(triggerChannelToDelete);
		}
		private boolean deleteMeasurement(TriggerChannelDTO triggerChannelDTO)
		{
			boolean deleteStatus = false;
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
//			logger.debug("BEFORE: Triggers size..."+mapXMLDfrs.get(triggerChannelDTO.getChassis()).getDataPool().getAlgorithms().getTriggers().sizeOfTriggerArray());
//			logger.debug("DFR XML content in deleteMeasurement method "+xmlDfr.xmlText());
			Algorithms algorithms = xmlDfr.getDataPool().getAlgorithms();
//			Exports exports = xmlDfr.getDataPool().getExports();
//			Export[] arrExport = exports.getExportArray();
//			PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
//			PmuInput[] arrPmuInput = pmuInputs.getPmuInputArray();
			Measurements measurements = currentSubstation.getMeasurements();
			Measurement[] arrMeasurement = measurements.getMeasurementArray();
			logger.debug("arrMeasurement size..."+arrMeasurement.length);
			for (int i = 0; i < arrMeasurement.length; i++) {
				logger.debug("arrMeasurement[i].getId() "+arrMeasurement[i].getId()+" triggerChannelDTO.getId() "+triggerChannelDTO.getId());
				if (arrMeasurement[i].getId() == triggerChannelDTO.getId())
				{
					measurements.removeMeasurement(i);
					arrMeasurement = measurements.getMeasurementArray();
					i=0;
					deleteStatus = true;
				}
			}
//			for (int i = 0; i < arrPmuInput.length; i++) {
//				if (arrPmuInput[i].getId() == triggerChannelDTO.getId())
//				{
//					pmuInputs.removePmuInput(i);
//					arrPmuInput = pmuInputs.getPmuInputArray();
//					i = 0;
//					deleteStatus = true;
//				}
//			}
//
//			for (int i = 0; i < arrExport.length; i++) {
//				if (arrExport[i].getId() == triggerChannelDTO.getId())
//				{
//					exports.removeExport(i);
//					arrExport = exports.getExportArray();
//					i=0;
//					deleteStatus = true;
//				}
//			}
			Triggers triggers = algorithms.getTriggers();
			logger.debug("About to delete Trigger "+triggerChannelDTO.getId()+" name: "+triggerChannelDTO.getMeasurementName());
			if (triggers != null )
			{
				Trigger[] arrTrigger = triggers.getTriggerArray(); 
				if(arrTrigger != null && arrTrigger.length > 0)
				{
					for (int i = 0; i < arrTrigger.length; i++) {
//						logger.debug("To be compared to "+arrTrigger[i].getChanId()+" name "+arrTrigger[i].getName());
						if (triggerChannelDTO.getId().equals(arrTrigger[i].getChanId()))
						{
							logger.debug("Removed trigger with index "+i+" with chanId "+triggerChannelDTO.getId());
							Limits[] arrLimits = algorithms.getLimitsArray();
							logger.debug("Limits size..."+arrLimits.length);
							for (int j = 0; j < arrLimits.length; j++) {
								logger.debug("arrLimits[j].getName() "+arrLimits[j].getName()+" triggerChannelDTO.getMeasurementName() "+triggerChannelDTO.getMeasurementName());
								if (arrLimits[j].getName().equalsIgnoreCase(triggerChannelDTO.getMeasurementName()))
								{
									String inputValue = arrLimits[j].getInput();
									int index = inputValue.indexOf("/")+1;
									String xmlAlgoType = inputValue.substring(index, inputValue.indexOf("/", index));
									if (xmlAlgoType.startsWith(M9kConstants.RMS))
									{
//										triggerChannelDto.setType(xmlAlgoType.substring(0, xmlAlgoType.indexOf("(")));
										deleteRmsFromXml(triggerChannelDTO);
										logger.debug("Triggers size..."+mapXMLDfrs.get(triggerChannelDTO.getChassis()).getDataPool().getAlgorithms().getTriggers().sizeOfTriggerArray());
									}
									else if (xmlAlgoType.startsWith(M9kConstants.MAGNITUDE))
									{
										deleteMagnitudeFromXml(triggerChannelDTO);
									}
									else if (xmlAlgoType.startsWith(M9kConstants.FREQUENCY))
									{
										deleteFrequencyFromXml(triggerChannelDTO);
									}
									else if (xmlAlgoType.startsWith(M9kConstants.ZERO_SEQUENCE))
									{
										deleteZeroSequenceFromXml( triggerChannelDTO);
									}
									else if (xmlAlgoType.startsWith(M9kConstants.POSITIVE_SEQUENCE))
									{
										deletePositiveSequenceFromXml(triggerChannelDTO);
									}
									else if (xmlAlgoType.startsWith(M9kConstants.NEGATIVE_SEQUENCE))
									{
										deleteNegativeSequenceFromXml(triggerChannelDTO);
									}
									else if (xmlAlgoType.startsWith(M9kConstants.PHASE))
									{
										deletePhaseFromXml(triggerChannelDTO);
									}
									else if (xmlAlgoType.startsWith(M9kConstants.POWER))
									{
										deletePowerFromXml(triggerChannelDTO);
									}
									// START: 28-Jan-2020 - Transducer implementation - Average algorithm
									else if (xmlAlgoType.startsWith(M9kConstants.AVERAGE))
									{
										deleteAverageFromXml(triggerChannelDTO);
									}
									// END: 28-Jan-2020
									algorithms.removeLimits(j);
									deleteStatus = true;
								}
							}
							triggers.removeTrigger(i);
							mapXMLDfrs.put(triggerChannelDTO.getChassis(), xmlDfr);
							deleteStatus = true;
							break;
						}
					}
				}
			}
			logger.debug("AFTER: Triggers size..."+mapXMLDfrs.get(triggerChannelDTO.getChassis()).getDataPool().getAlgorithms().getTriggers().sizeOfTriggerArray());
			logger.debug("AFTER: Triggers size..."+xmlDfr.getDataPool().getAlgorithms().getTriggers().sizeOfTriggerArray());
			session.put("mapXMLDfrs",mapXMLDfrs);
			return deleteStatus;
		}		
		
		
		private void deleteRmsFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			Rms[] arrRms = xmlDfr.getDataPool().getAlgorithms().getRmsArray();
			logger.debug("Before: Rms deleted from xml"+xmlDfr.getDataPool().getAlgorithms().getRmsArray().length);
			for (int i = 0; i <arrRms.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrRms[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removeRms(i);
					break;
				}
			}
			logger.debug("After: Rms deleted from xml"+xmlDfr.getDataPool().getAlgorithms().getRmsArray().length);
			mapXMLDfrs.put(triggerChannelDTO.getChassis(), xmlDfr);
			
		}

		private void deleteMagnitudeFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			Magnitude[] arrMagnitude = xmlDfr.getDataPool().getAlgorithms().getMagnitudeArray();
			for (int i = 0; i <arrMagnitude.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrMagnitude[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removeMagnitude(i);
					break;
				}
			}
			mapXMLDfrs.put(triggerChannelDTO.getChassis(), xmlDfr);
		}

		private void deleteFrequencyFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			Frequency[] arrFrequency = xmlDfr.getDataPool().getAlgorithms().getFrequencyArray();
			for (int i = 0; i <arrFrequency.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrFrequency[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removeFrequency(i);
					break;
				}
			}
		}
		
		private void deletePhaseFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			Phase[] arrPhase = xmlDfr.getDataPool().getAlgorithms().getPhaseArray();
			for (int i = 0; i <arrPhase.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrPhase[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removePhase(i);
					break;
				}
			}
			mapXMLDfrs.put(triggerChannelDTO.getChassis(), xmlDfr);
		}
		
		private void deleteZeroSequenceFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			ZeroSequence[] arrZeroSequence = xmlDfr.getDataPool().getAlgorithms().getZeroSequenceArray();
			for (int i = 0; i <arrZeroSequence.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrZeroSequence[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removeZeroSequence(i);
					break;
				}
			}
			mapXMLDfrs.put(triggerChannelDTO.getChassis(), xmlDfr);
		}
		
		private void deletePositiveSequenceFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			PositiveSequence[] arrPositiveSequence = xmlDfr.getDataPool().getAlgorithms().getPositiveSequenceArray();
			for (int i = 0; i <arrPositiveSequence.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrPositiveSequence[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removePositiveSequence(i);
					break;
				}
			}
			mapXMLDfrs.put(triggerChannelDTO.getChassis(), xmlDfr);
		}
		
		private void deleteNegativeSequenceFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			NegativeSequence[] arrNegativeSequence = xmlDfr.getDataPool().getAlgorithms().getNegativeSequenceArray();
			for (int i = 0; i <arrNegativeSequence.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrNegativeSequence[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removeNegativeSequence(i);
					break;
				}
			}
		}
		
		private void deletePowerFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			Power[] arrPower = xmlDfr.getDataPool().getAlgorithms().getPowerArray();
			for (int i = 0; i <arrPower.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrPower[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removePower(i);
					break;
				}
			}
			mapXMLDfrs.put(triggerChannelDTO.getChassis(), xmlDfr);
		}
		
		// START: 28-Jan-2020 - Transducer implementation - Average algorithm
		private void deleteAverageFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			Average[] arrAverage = xmlDfr.getDataPool().getAlgorithms().getAverageArray();
			for (int i = 0; i <arrAverage.length; i++) {
				logger.debug("Comparing average algo to delete "+triggerChannelDTO.getMeasurementName()+" average name "+arrAverage[i].getName());
				if (triggerChannelDTO.getMeasurementName().equals(arrAverage[i].getName()))
				{
					logger.debug("Before delete "+xmlDfr.getDataPool().getAlgorithms());
					xmlDfr.getDataPool().getAlgorithms().removeAverage(i);
					logger.debug("After delete "+xmlDfr.getDataPool().getAlgorithms());
					break;
				}
			}
			mapXMLDfrs.put(triggerChannelDTO.getChassis(), xmlDfr);
			
		}
		// END: 28-Jan-2020

		private Rms getRmsIfAlreadyExists(AbstractTrigger rmsTrigger)
		{
			Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
			Rms rms = null;
			for (int i = 0; i < algorithms.getRmsArray().length; i++) {
				if (rmsTrigger.getName().equals(algorithms.getRmsArray(i).getName()))
				{
					rms = algorithms.getRmsArray(i);
					break;
				}
			}
			return rms;
		}

		private Frequency getFrequencyIfAlreadyExists(AbstractTrigger frequencyTrigger)
		{
			Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
			Frequency frequency= null;
			for (int i = 0; i < algorithms.getFrequencyArray().length; i++) {
				if (frequencyTrigger.getName().equals(algorithms.getFrequencyArray(i).getName()))
				{
					frequency = algorithms.getFrequencyArray(i);
					break;
				}
			}
			return frequency;
		}
		
		
		private Magnitude getMagnitudeIfAlreadyExists(AbstractTrigger magnitudeTrigger)
		{
			Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
			Magnitude magnitude = null;
			for (int i = 0; i < algorithms.getMagnitudeArray().length; i++) {
				if (magnitudeTrigger.getName().equals(algorithms.getMagnitudeArray(i).getName()))
				{
					magnitude = algorithms.getMagnitudeArray(i);
					break;
				}
			}
			return magnitude;
		}

		

		private Phase getPhaseIfAlreadyExists(AbstractTrigger phasorTrigger)
		{
			Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
			Phase phasor = null;
			for (int i = 0; i < algorithms.getPhaseArray().length; i++) {
				if (phasorTrigger.getName().equals(algorithms.getPhaseArray(i).getName()))
				{
					phasor = algorithms.getPhaseArray(i);
					break;
				}
			}
			return phasor;
		}

		private ZeroSequence getZeroSequenceIfAlreadyExists(AbstractTrigger zeroSequenceTrigger)
		{
			Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
			ZeroSequence zeroSequence = null;
			for (int i = 0; i < algorithms.getZeroSequenceArray().length; i++) {
				if (zeroSequenceTrigger.getName().equals(algorithms.getZeroSequenceArray(i).getName()))
				{
					zeroSequence = algorithms.getZeroSequenceArray(i);
					break;
				}
			}
			return zeroSequence;
		}

		private PositiveSequence getPositiveSequenceIfAlreadyExists(AbstractTrigger positiveSequenceTrigger)
		{
			Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
			PositiveSequence positiveSequence = null;
			for (int i = 0; i < algorithms.getPositiveSequenceArray().length; i++) {
				if (positiveSequenceTrigger.getName().equals(algorithms.getPositiveSequenceArray(i).getName()))
				{
					positiveSequence = algorithms.getPositiveSequenceArray(i);
					break;
				}
			}
			return positiveSequence;
		}
		private NegativeSequence getNegativeSequenceIfAlreadyExists(AbstractTrigger negativeSequenceTrigger)
		{
			Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
			NegativeSequence negativeSequence = null;
			for (int i = 0; i < algorithms.getNegativeSequenceArray().length; i++) {
				if (negativeSequenceTrigger.getName().equals(algorithms.getNegativeSequenceArray(i).getName()))
				{
					negativeSequence = algorithms.getNegativeSequenceArray(i);
					break;
				}
			}
			return negativeSequence;
		}

		private Power getPowerIfAlreadyExists(AbstractTrigger powerTrigger)
		{
			Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
			Power power = null;
			for (int i = 0; i < algorithms.getPowerArray().length; i++) {
				if (powerTrigger.getName().equals(algorithms.getPowerArray(i).getName()))
				{
					power = algorithms.getPowerArray(i);
					break;
				}
			}
			return power;
		}

		// START: 28-Jan-2020 - Transducer implementation - Average algorithm
		private Average getAverageIfAlreadyExists(AbstractTrigger averageTrigger)
		{
			Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
			Average average = null;
			for (int i = 0; i < algorithms.getRmsArray().length; i++) {
				if (averageTrigger.getName().equals(algorithms.getRmsArray(i).getName()))
				{
					average = algorithms.getAverageArray(i);
					break;
				}
			}
			return average;
		}
		//END: 28-Jan-2020
		@SuppressWarnings("unused")
		private void clearAlgorithms(DFR dfrObj)
		{
			Algorithms algorithms = dfrObj.getDataPool().getAlgorithms();
			if (algorithms == null)
			{
				return;
			}
			Triggers triggers = algorithms.getTriggers();
			Exports exports = dfrObj.getDataPool().getExports();
//			// Clear Exports
//			while(exports != null && exports.sizeOfExportArray() > 0)
//			{
//				exports.removeExport(0);
//			}
			// Clear trigger objects
			while(triggers != null && triggers.sizeOfTriggerArray() > 0)
			{
				triggers.removeTrigger(0);
			}
			
//			// Clear Rms objects
//			while(algorithms.sizeOfRmsArray() > 0)
//			{
//				algorithms.removeRms(0);
//			}

			// Clear Frequency objects
			while(algorithms.sizeOfFrequencyArray() > 0)
			{
				algorithms.removeFrequency(0);
			}
			
			// Clear And objects
			while(algorithms.sizeOfAndArray() > 0)
			{
				algorithms.removeAnd(0);
			}
			
			// Clear Average objects
			while(algorithms.sizeOfAverageArray() > 0)
			{
				algorithms.removeAverage(0);
			}
			
//			// Clear Float objects
//			while(algorithms.sizeOfFloatArray() > 0)
//			{
//				algorithms.removeFloat(0);
//			}

			// Clear Limits objects
			while(algorithms.sizeOfLimitsArray() > 0)
			{
				algorithms.removeLimits(0);
			}

			// Clear Magnitude objects
			while(algorithms.sizeOfMagnitudeArray() > 0)
			{
				algorithms.removeMagnitude(0);
			}
			
			// Clear Output objects
			while(algorithms.sizeOfOutputArray() > 0)
			{
				algorithms.removeOutput(0);
			}

			// Clear Phasor objects
			while(algorithms.sizeOfPhaseArray() > 0)
			{
				algorithms.removePhase(0);
			}

			// Clear ZS objects
			while(algorithms.sizeOfZeroSequenceArray() > 0)
			{
				algorithms.removeZeroSequence(0);
			}
			// Clear NS objects
			while(algorithms.sizeOfNegativeSequenceArray() > 0)
			{
				algorithms.removeNegativeSequence(0);
			}
			// Clear PS objects
			while(algorithms.sizeOfPositiveSequenceArray() > 0)
			{
				algorithms.removePositiveSequence(0);
			}
			if (exports != null)
			{
				dfrObj.getDataPool().unsetExports();
			}
//			dfrObj.getDataPool().unsetAlgorithms();
			if (dfrObj.getDataPool().getLineGroups() != null)
			{
				dfrObj.getDataPool().unsetLineGroups();
			}
//			algorithms.unsetPmus();
//			if (algorithms.getTriggers() != null && algorithms.getTriggers().sizeOfTriggerArray() == 0)
//			{
//				algorithms.unsetTriggers();
//			}
		}
		
		private AbstractTrigger populateTrigger(TriggerChannelDTO triggerChannelDTO)
		{
			logger.debug("\t\t\t\tDEBUG: Inside populate Trigger "+triggerChannelDTO);
			Integer triggerId;
			if ((getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit")) || (triggerChannelDTO == null || triggerChannelDTO.getId() == null || triggerChannelDTO.getId().equals(0) || triggerChannelDTO.getId() == -1))
			{
				triggerId = StationDTO.getNextAvailableTriggerId();
				triggerChannelDTO.setId(triggerId);
//				triggerChannelDTO.setMeasurementName(triggerId+"-"+triggerChannelDTO.getInputChannelName());
			}
			else
			{
				triggerId = triggerChannelDTO.getId();
			}
//			triggerChannelDTO.setName(triggerId+"-"+triggerChannelDTO.getInputChannelName());
			triggerChannelDTO.setMeasurementName(triggerId+"-"+triggerChannelDTO.getInputChannelName());

			AbstractTrigger genericTrigger = null;
//			logger.debug("Entered populate Trigger..."+triggerChannelDTO.getType());
			logger.debug("Getting generic trigger is tranducer set? "+triggerChannelDTO.isTransducer());
			//START: 28-Jan-2020 - Implementing Transducer
			// 23-Jul-2020 - Bug Fix: Units value messes the algorithm
			if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.AVERAGE) || triggerChannelDTO.isTransducer())
			{
				logger.debug("Inside "+M9kConstants.AVERAGE);
				genericTrigger = new AverageAlgorithm();
//				genericTrigger.setName(M9kConstants.RMS+"_"+triggerChannelDTO.getId());
			}
			//END: 23-Jul-2020

			else if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.RMS))
			{
				logger.debug("Inside "+M9kConstants.RMS);
				genericTrigger = new RMSAlgorithm();
//				genericTrigger.setName(M9kConstants.RMS+"_"+triggerChannelDTO.getId());
			}
			else if (triggerChannelDTO.getType().equals(M9kConstants.HARMONIC))
			{
				logger.debug("Inside "+M9kConstants.HARMONIC);
				genericTrigger = new MagnitudeTrigger();
//				genericTrigger.setName(M9kConstants.MAGNITUDE+"_"+triggerChannelDTO.getId());
				((MagnitudeTrigger)genericTrigger).setHarmonic(triggerChannelDTO.getHarmonic());
			}
			else if (triggerChannelDTO.getType().equals(M9kConstants.FREQUENCY))
			{
				logger.debug("Inside "+M9kConstants.FREQUENCY);
				genericTrigger = new FrequencyTrigger();
//				genericTrigger.setName(M9kConstants.FREQUENCY+"_"+triggerChannelDTO.getId());
//				((FrequencyTrigger)genericTrigger).setPhaseName(M9kConstants.PHASE+"_"+triggerChannelDTO.getInputChannelName());
			}
			else if (triggerChannelDTO.getType().equals(M9kConstants.ZERO_SEQUENCE_V) ||
					triggerChannelDTO.getType().equals(M9kConstants.ZERO_SEQUENCE_I))
			{
				logger.debug("Inside "+M9kConstants.ZERO_SEQUENCE);
				genericTrigger = new SequenceTrigger(M9kConstants.ZERO_SEQUENCE);
				if (triggerChannelDTO.getType().indexOf(M9kConstants.VOLTAGE) != -1)
				{
//					genericTrigger.setName(M9kConstants.ZSV+triggerChannelDTO.getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.VOLTAGE);
				}
				else
				{
//					genericTrigger.setName(M9kConstants.ZSI+triggerChannelDTO.getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.CURRENT);
				}

			}
			else if (triggerChannelDTO.getType().equals(M9kConstants.POSITIVE_SEQUENCE_V) || 
					triggerChannelDTO.getType().equals(M9kConstants.POSITIVE_SEQUENCE_I))
			{
				logger.debug("Inside "+M9kConstants.POSITIVE_SEQUENCE);
				genericTrigger = new SequenceTrigger(M9kConstants.POSITIVE_SEQUENCE);
				if (triggerChannelDTO.getType().indexOf(M9kConstants.VOLTAGE) != -1)
				{
//					genericTrigger.setName(M9kConstants.PSV+triggerChannelDTO.getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.VOLTAGE);
				}
				else
				{
//					genericTrigger.setName(M9kConstants.PSI+triggerChannelDTO.getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.CURRENT);
				}

			}
			else if (triggerChannelDTO.getType().startsWith(M9kConstants.NEGATIVE_SEQUENCE_V) ||
					triggerChannelDTO.getType().startsWith(M9kConstants.NEGATIVE_SEQUENCE_I))
			{
				logger.debug("Inside "+M9kConstants.NEGATIVE_SEQUENCE);
				genericTrigger = new SequenceTrigger(M9kConstants.NEGATIVE_SEQUENCE);
				if (triggerChannelDTO.getType().indexOf(M9kConstants.VOLTAGE) != -1)
				{
//					genericTrigger.setName(M9kConstants.NSV+triggerChannelDTO.getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.VOLTAGE);
				}
				else
				{
//					genericTrigger.setName(M9kConstants.NSI+triggerChannelDTO.getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.CURRENT);
				}

			}
			else if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.PHASOR))
			{
				genericTrigger = new PhasorTrigger();
//				genericTrigger.setName(M9kConstants.PHASE+triggerChannelDTO.getId());
			}
			else if (triggerChannelDTO.getType().indexOf(M9kConstants.WATTS) != -1)
			{
				genericTrigger = new PowerTrigger(M9kConstants.WATTS_REAL);
			}
			else if (triggerChannelDTO.getType().indexOf(M9kConstants.VARS) != -1)
			{
				genericTrigger = new PowerTrigger(M9kConstants.VARS_REACTIVE);
			}
			// START: 17-Mar-2021 - Apparent power implementation
			else if (triggerChannelDTO.getType().indexOf(M9kConstants.VA_APPARENT) != -1)
			{
				genericTrigger = new PowerTrigger(M9kConstants.VA_APPARENT);
			}
			// END: 17-Mar-2021 - Apparent power implementation
			else
			{
				logger.error(triggerChannelDTO.getType()+" is not yet implemented");
				return null;
			}
			genericTrigger.setName(triggerChannelDTO.getMeasurementName());
			genericTrigger.setInputValue( triggerChannelDTO.getTriggerChannelInput());
			Trigger trigger = TriggerDocument.Factory.newInstance().addNewTrigger();
			trigger.setChanId(triggerId);
//			trigger.setName("T_"+triggerChannelDTO.getName());
			trigger.setName(triggerChannelDTO.getMeasurementName());
			trigger.setTriggerName(triggerChannelDTO.getName());
			LimitsAlgorithm limits = new LimitsAlgorithm();
			limits.setName(triggerChannelDTO.getMeasurementName());
			if (genericTrigger instanceof SequenceTrigger)
			{
				limits.setInputValue(((SequenceTrigger)genericTrigger).getInputXMLString(M9kXMLConstants.XML_VALUE_INPUT));
			}
			else
			{
				limits.setInputValue(genericTrigger.getInputXMLString());
			}

			// START: 26-Mar-2020 - Hot Fix to address editing configuration in IE issue
//			if (!triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.NEVER))
			if (triggerChannelDTO.getStart() != null && !triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.NEVER))
			// STOP: 26-Mar-2020
			{
				logger.debug("TriggerIfOver from Populate Trigger..."+triggerChannelDTO.getTripIfOver()+" is disabled? "+triggerChannelDTO.getDisableTripOver());
				logger.debug("TriggerIfUnder from Populate Trigger..."+triggerChannelDTO.getTripIfUnder()+" is disabled? "+triggerChannelDTO.getDisableTripUnder());
				
				if (triggerChannelDTO.getTripIfOver() != null && (triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.BOTH) || triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.OVER)))
				{
					limits.setHighLimit(triggerChannelDTO.getTripIfOver());
				}
				if (triggerChannelDTO.getTripIfUnder() != null && (triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.BOTH) || triggerChannelDTO.getStart().equalsIgnoreCase(M9kConstants.UNDER)))
				{
					limits.setLowLimit(triggerChannelDTO.getTripIfUnder());
				}
	//			limits.setHighLimit(triggerChannelDTO.getTripIfOver());
	//			limits.setLowLimit(triggerChannelDTO.getTripIfUnder());
				// START: 25-Feb-2020 - ROC positive and Negative Limits implementation
//				if (triggerChannelDTO.getTripIfGt() != null)
//				{
//					limits.setRateOfChangeLimit(triggerChannelDTO.getTripIfGt());
//				}
//				if (triggerChannelDTO.getDuration() != null)
				if (triggerChannelDTO.getDuration() != null && 
						!triggerChannelDTO.getDuration().isEmpty() &&
						!triggerChannelDTO.getDuration().equalsIgnoreCase("0"))
				{
					limits.setDuration(triggerChannelDTO.getDuration());
					limits.setRateOfChangeLimitNeg(triggerChannelDTO.getTripRocNeg());
					limits.setRateOfChangeLimitPos(triggerChannelDTO.getTripRocPos());
				}
				// END: 25-Feb-2020
				
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
//				if (triggerChannelDTO.getTripIfGt() != null && !triggerChannelDTO.getTripIfGt().isEmpty())
//				{
//					// TODO: To be implemented. Still not part of XML
//					logger.debug("Tigger if greater than certain seconds for certain durations not implemented");
//				}
				
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

			genericTrigger.setLimitsAlgorithm(limits);
			trigger.setInput(limits.getLimitsInput());

			genericTrigger.setTrigger(trigger);
			
			if (triggerChannelDTO.getAverage() != null)
			{
				genericTrigger.setAverage(triggerChannelDTO.getAverage());
			}
//			logger.debug("Returning populateRMSTrigger...");
			
			return genericTrigger;
		}
	  
		  @SuppressWarnings("unused")
		  // To support global triggers in the future
		private TriggerChannelDTO constructGroupTriggerFromXml(GroupTrigger groupTrigger)
		  {
			  TriggerChannelDTO triggerChannelDto = new TriggerChannelDTO();
			  triggerChannelDto.setChannel(groupTrigger.getId()+"-"+M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP+"-"+groupTrigger.getLineGroupName());
			  logger.debug("\t\t\t#LineGroup: LineGroup Id "+groupTrigger.getLineGroupId() +" LineGroup Name "+groupTrigger.getLineGroupName()+" Channel set "+triggerChannelDto.getChannel());
			  triggerChannelDto.setGlobalLineGroupId(groupTrigger.getLineGroupId());
			  triggerChannelDto.setId(groupTrigger.getId());
			  triggerChannelDto.setName(groupTrigger.getName().substring(groupTrigger.getName().indexOf("LGT_")+4));
			  triggerChannelDto.setPhase(M9kConstants.NO_PHASE);
			  triggerChannelDto.setChatterLimit(""+groupTrigger.getChatterLimit());
			  triggerChannelDto.setChatterRate(""+groupTrigger.getChatterRate());
			  triggerChannelDto.setTriggerLimit(""+groupTrigger.getTriggerLimit());
			  triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP);
			  triggerChannelDto.setType(groupTrigger.getType());
			  if (groupTrigger.getHighLimit() != null && groupTrigger.getLowLimit() != null)
				{
					triggerChannelDto.setStart(M9kConstants.BOTH);
					triggerChannelDto.setTripIfOver(""+groupTrigger.getHighLimit());
					triggerChannelDto.setTripIfUnder(""+groupTrigger.getLowLimit());
					triggerChannelDto.setDisableTripOver(false);
					triggerChannelDto.setDisableTripUnder(false);
				}
				else if (groupTrigger.getHighLimit() != null)
				{
					triggerChannelDto.setStart(M9kConstants.OVER);
					triggerChannelDto.setTripIfOver(""+groupTrigger.getHighLimit());						
					triggerChannelDto.setDisableTripOver(false);
					triggerChannelDto.setDisableTripUnder(true);
				}
				else if (groupTrigger.getLowLimit() != null)
				{
					triggerChannelDto.setStart(M9kConstants.UNDER);
					triggerChannelDto.setTripIfUnder(""+groupTrigger.getLowLimit());	
					triggerChannelDto.setDisableTripOver(true);
					triggerChannelDto.setDisableTripUnder(false);						
				}			  
			  if (groupTrigger.getExport() == 0)
			  {
				  triggerChannelDto.setExportStatus(false);
			  }
			  else
			  {
				  triggerChannelDto.setExportStatus(true);
//				  triggerChannelDto.setExportRate(groupTrigger.getExportRate());
			  }
			  if (groupTrigger.getEnable() == 0)
			  {
				  triggerChannelDto.setStatus(false);
			  }
			  else
			  {
				  triggerChannelDto.setStatus(true);
			  }
			  triggerChannelDto.setDisableHarmonic(true);
			  return triggerChannelDto;
		  }
	  private TriggerChannelDTO constructTriggerFromXml(Trigger triggerXml)
	  {
//		  logger.debug("\t\t\t\tDEBUG: Enteredd constructTriggerFromXml...");
		  TriggerChannelDTO triggerChannelDto = new TriggerChannelDTO();
		  String inputValue = triggerXml.getInput();
		  String algorithmName = "";
		  String limitsName = "";
		  String xmlAlgoType;
		  Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
		  Limits limits[] = algorithms.getLimitsArray();
		  triggerChannelDto.setId(triggerXml.getChanId());
//		  triggerChannelDto.setName(triggerXml.getName().substring(triggerXml.getName().indexOf("T_")+2));
		  triggerChannelDto.setName(triggerXml.getName());
//		  logger.debug("TriggerXml phase...."+triggerXml.getPhase());
//		  triggerChannelDto.setPhase(triggerXml.getPhase());
		  triggerChannelDto.setChatterLimit(""+triggerXml.getChatterLimit());
		  triggerChannelDto.setChatterRate(""+triggerXml.getChatterRate());
		  triggerChannelDto.setTriggerLimit(""+triggerXml.getTriggerLimit());
		  triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_ANALOG);
		  triggerChannelDto.setExportStatus(false);
		  if (triggerXml.getEnable() == M9kConstants.ENABLE_ONE)
		  {
			  triggerChannelDto.setTriggerStatus(true);
		  }
		  else
		  {
			  triggerChannelDto.setTriggerStatus(false);
		  }

//		  logger.debug("\t\t\t\tDEBUG: Input value of the trigger..."+inputValue);
		  int index = inputValue.indexOf("Limits(");
		  if (index != -1)
		  {
			  limitsName = inputValue.substring(index+7,inputValue.lastIndexOf(")"));
//			  logger.debug("\t\t\t\tDEBUG: Limits name: "+limitsName);
			  for (int i = 0; i < limits.length; i++) {
				if (limits[i].getName().equalsIgnoreCase(limitsName))
				{
					triggerChannelDto.setTriggerStatus(true);
					// START: 25-Feb-2020 - ROC positive and Negative Limits implementation
					// ROC settings
//					if(limits[i].getDuration() != null && limits[i].getRateOfChangeLimit() != null)
					if(limits[i].getDuration() != null && !limits[i].getDuration().isEmpty() && !limits[i].getDuration().equals("0"))
					{
						 // START 29-July-2015: ROC implementation  
//						  triggerChannelDto.setDisableTripIfGt(false);
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
//						triggerChannelDto.setDisableDuration(true);
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
//						logger.debug("\t\t\t\tDEBUG: Setting to OVER");
						triggerChannelDto.setStart(M9kConstants.OVER);
						triggerChannelDto.setTripIfOver(""+limits[i].getHighLimit());						
						triggerChannelDto.setDisableTripOver(false);
						triggerChannelDto.setDisableTripUnder(true);
					}
					else if (limits[i].getLowLimit() != null)
					{
//						logger.debug("\t\t\t\tDEBUG: Setting to UNDER");
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
//							logger.debug("\t\t\t\tDEBUG: Setting to NEVER");
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
//					logger.debug("Trigger if under from XML..."+limits[i].getLowLimit());	
					inputValue = limits[i].getInput();
					index = inputValue.indexOf("/")+1;
					xmlAlgoType = inputValue.substring(index, inputValue.indexOf("/", index));
					algorithmName = xmlAlgoType.substring(xmlAlgoType.lastIndexOf("(")+1, xmlAlgoType.indexOf(")"));
//					logger.debug("Input object of the Limit..."+xmlAlgoType);
//					logger.debug("Type of trigger...."+xmlAlgoType.substring(0, xmlAlgoType.indexOf("(")));
//					logger.debug("Type of trigger...."+xmlAlgoType.substring(0, xmlAlgoType.indexOf("(")));
					triggerChannelDto.setType(xmlAlgoType.substring(0, xmlAlgoType.indexOf("(")));
					
					if (xmlAlgoType.startsWith(M9kConstants.RMS))
					{
//						triggerChannelDto.setType(xmlAlgoType.substring(0, xmlAlgoType.indexOf("(")));
						constructRmsFromXml(algorithmName, triggerChannelDto);
					}
					else if (xmlAlgoType.startsWith(M9kConstants.MAGNITUDE))
					{
						//START: 03-June-2015 Magnitude changed to Harmonic in algorithm type drop down
						triggerChannelDto.setType(M9kConstants.HARMONIC);
						//END
						constructMagnitudeFromXml(algorithmName, triggerChannelDto);
					}
					else if (xmlAlgoType.startsWith(M9kConstants.FREQUENCY))
					{
						constructFrequencyFromXml(algorithmName, triggerChannelDto);
					}
					else if (xmlAlgoType.startsWith(M9kConstants.ZERO_SEQUENCE))
					{
						constructZeroSequenceFromXml(algorithmName, triggerChannelDto);
					}
					else if (xmlAlgoType.startsWith(M9kConstants.POSITIVE_SEQUENCE))
					{
						constructPositiveSequenceFromXml(algorithmName, triggerChannelDto);
					}
					else if (xmlAlgoType.startsWith(M9kConstants.NEGATIVE_SEQUENCE))
					{
						logger.debug("\t\t\t\tDEBUG: about to invoke constructNegativeSequenceFromXml");
						constructNegativeSequenceFromXml(algorithmName, triggerChannelDto);
					}
					else if (xmlAlgoType.startsWith(M9kConstants.POWER))
					{
						constructPowerFromXml(algorithmName, triggerChannelDto);
					}
					// START: 28-Jan-2020 - Transducer Implementation
					else if (xmlAlgoType.startsWith(M9kConstants.AVERAGE))
					{
						constructAverageFromXml(algorithmName, triggerChannelDto);
					}
					// START: 09-Mar-2021 - Transducer Implementation
					else if (xmlAlgoType.startsWith(M9kConstants.VIRTUAL_MEASUREMENT))
					{
						
						constructVirtualMeasurementFromXml(algorithmName, triggerChannelDto);
						  triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_MEASUREMENT);
						  triggerChannelDto.setType(M9kConstants.VIRTUAL_MEASUREMENT);
					}

					// END: 28-Jan-2020
//					else if (xmlAlgoType.startsWith(M9kConstants.PHASE))
//					{
//						constructPhaseFromXml(algorithmName, triggerChannelDto);
//					}
					break;
				}
			}
			  
		  }
		  
		  logger.debug("\n\n\t\t TriggerChannelDTO Constructed..."+triggerChannelDto+"\n\n\n\n");
		  return triggerChannelDto;
	  }

	  private TriggerChannelDTO parseAndBuildMeasurementsFromXml(Export exportXml)
	  {
		  TriggerChannelDTO triggerChannelDto = null;
//		  logger.debug("export input "+exportXml.getInput());
//		  logger.debug("export Name "+exportXml.getName());
//		  logger.debug("export id "+exportXml.getId());
		  String inputValue = exportXml.getInput();
		  String algorithmName = "";
		  String xmlAlgoType;
		  Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
		  Triggers triggers = algorithms.getTriggers();
		  Trigger[] trigger = null;
		  if (triggers != null)
		  {
			  trigger = triggers.getTriggerArray();
//			  if (triggerOutHold.isEmpty())
//			  {
//				  triggerOutHold = ""+triggers.getTriggerOutHold();
//				  session.put("triggerOutHold", triggerOutHold);
//				  logger.debug("triggerOutHold in edit mode "+triggerOutHold);
//			  }
			  
		  }
		  int index = inputValue.indexOf("Phase(");
		  if (index != -1)
		  {
			  triggerChannelDto = new TriggerChannelDTO();
			  triggerChannelDto.setId(exportXml.getId());
			  triggerChannelDto.setName(exportXml.getName());
//			  logger.debug("TriggerXml phase...."+exportXml.getPhase());
			  triggerChannelDto.setPhase(exportXml.getPhase());
			  index = inputValue.indexOf("/")+1;
			  xmlAlgoType = inputValue.substring(index, inputValue.indexOf("/", index));
			  algorithmName = xmlAlgoType.substring(xmlAlgoType.lastIndexOf("(")+1, xmlAlgoType.indexOf(")"));
			  triggerChannelDto.setStart(M9kConstants.NEVER);
			  // START: 26-Mar-2020 - Hot Fix to address editing configuration in IE issue
			  triggerChannelDto.setType(M9kConstants.PHASOR);
			  // END: 26-Mar-2020
			  triggerChannelDto.setDisableTripOver(true);
			  triggerChannelDto.setDisableTripUnder(true);
			  // START 29-July-2015: ROC implementation  
			  triggerChannelDto.setDisableTripRoc(true);
			  triggerChannelDto.setDisableDuration(true);
			  // END 29-July-2015: ROC implementation
			  triggerChannelDto.setTriggerStatus(false);
			  
//				triggerChannelDto.setExportRate(exportXml.getSampleRate());
//			  if (exportXml.getEnable() == M9kConstants.ENABLE_ONE)
//			  {
//				// Set PMU Status  
//				for (int pmuCnt=0; pmuInput != null && pmuCnt < pmuInput.length;pmuCnt++)
//				{
//					if (pmuInput[pmuCnt].getDfr() == xmlConfigDfr.getSystem().getDfrId() && pmuInput[pmuCnt].getId() == exportXml.getId())
//					{
//						if (pmuInput[pmuCnt].getPmuEnable() == M9kConstants.ENABLE_ONE)
//						{
//							triggerChannelDto.setPmuStatus(true);
//						}
//						else
//						{
//							triggerChannelDto.setPmuStatus(false);
//						}
//						break;
//					}
//				}
//
//			  }
//			  else
//			  {
//				  triggerChannelDto.setExportStatus(false);
//			  }
			  constructPhaseFromXml(algorithmName, triggerChannelDto);
		  }
		  else
		  {
			  for (int i = 0; i < trigger.length; i++) {
				if (trigger[i].getChanId() == exportXml.getId() && (trigger[i].getType() == null || !trigger[i].getType().equalsIgnoreCase("E")))
				{
					triggerChannelDto = constructTriggerFromXml(trigger[i]);
					  triggerChannelDto.setPhase(exportXml.getPhase());
				}
			  }
		  }

		  
//		  logger.debug("\n\n\t\t TriggerChannelDTO Contructed..."+triggerChannelDto+"\n\n\n\n");
		  return triggerChannelDto;
	  }
	  
		private void constructRmsFromXml(String rmsName, TriggerChannelDTO triggerChannelDto)
		{
			String inputValue = "";
			String analogName= "";
//			String exportName = "";
//			int startIndex;
			
			AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
			
//			  Exports exports = xmlConfigDfr.getDataPool().getExports();
//			  Export export[] = null;
//			  if (exports != null)
//			  {
//				  export = exports.getExportArray();
//			  }
			  PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
//			  PmuInput[] pmuInput = null;
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			Rms rms[] = xmlConfigDfr.getDataPool().getAlgorithms().getRmsArray();		
			triggerChannelDto.setDisableHarmonic(true);
			// START: 01-May-2018 All exports to be part of DDR instead of just RMS - ReleaseV1.0.8.2
			triggerChannelDto.setDdrStatus(true);
			// END: 01-May-2018
//			rmsName = xmlAlgoType.substring(xmlAlgoType.indexOf("(")+1, xmlAlgoType.indexOf(")"));
//			logger.debug("Rms Name: "+rmsName);
			int analogOffset = getDfrAnalogOffset("DFR"+xmlConfigDfr.getSystem().getDfrId());
			for (int j = 0; j < rms.length; j++) {
//				logger.debug("rms[j].getName()..."+rms[j].getName());
				if (rms[j].getName().equals(rmsName))
				{
					inputValue = rms[j].getInput();
					if (rms[j].getAverage() > 0)
					{
						triggerChannelDto.setAverage(rms[j].getAverage());
					}
//					logger.debug("RMS input string..."+inputValue);
					analogName = inputValue.substring(inputValue.indexOf("AnalogInput(")+12,inputValue.lastIndexOf(")"));
//					logger.debug("analogName input for RMS..."+analogName);
					for (int j2 = 0; j2 < analogInputs.length; j2++) {
//						logger.debug("analogInputs[j2].getName()..."+analogInputs[j2].getName());
						if (analogInputs[j2].getName().equals(analogName))
						{
							triggerChannelDto.setInputChannelName(analogInputs[j2].getName());
							triggerChannelDto.setChannel(""+(analogInputs[j2].getChannel()+analogOffset)+"-"+M9kConstants.TRIGGER_INPUT_TYPE_ANALOG+"-"+analogInputs[j2].getCircuitName());
							// START: 28-Jan-2020 - Transducer Implementation - IF the channel is modified to transducer during editing then set the value to true
							if (analogInputs[j2].isSetTransducer() && analogInputs[j2].getTransducer() == 1)
							{
								triggerChannelDto.setTransducer(true);
								triggerChannelDto.setUnits(analogInputs[j2].getTransducerUnits());
								triggerChannelDto.setType(analogInputs[j2].getTransducerUnits()); // Type is same as Transducer Units
							}
							// END: 28-Jan-2020 
							if (analogInputs[j2].getInputType().startsWith(M9kConstants.VOLTAGE))
							{
								triggerChannelDto.setUnits("V");
							}
							else
							{
								triggerChannelDto.setUnits("A");
							}
//							logger.debug("Analog INput Channel..."+(analogInputs[j2].getChannel()+analogOffset));
							break;
						}
					}
					 
//					for (int exportCnt=0; (export!=null&&exportCnt < export.length);exportCnt++)
//					{
//						inputValue = export[exportCnt].getInput();
//						startIndex = inputValue.indexOf("Rms(");
//						if (startIndex == -1)
//						{
//							continue;
//						}
//						else
//						{
//							startIndex+=4;
//						}
//						exportName = inputValue.substring(startIndex,inputValue.lastIndexOf(")"));
//						if (exportName.equals(rmsName))
//						{
//							if (export[exportCnt].getSampleRate() > 0)
//							{
//								triggerChannelDto.setExportRate(export[exportCnt].getSampleRate());
//							}
//							for (int pmuCnt=0; pmuInput != null && pmuCnt < pmuInput.length;pmuCnt++)
//							{
//								if (pmuInput[pmuCnt].getDfr() == xmlConfigDfr.getSystem().getDfrId() && pmuInput[pmuCnt].getId() == export[exportCnt].getId())
//								{
//									if (pmuInput[pmuCnt].getPmuEnable() == M9kConstants.ENABLE_ONE)
//									{
//										triggerChannelDto.setPmuStatus(true);
//									}
//									else
//									{
//										triggerChannelDto.setPmuStatus(false);
//									}
//									break;
//								}
//							}
//							break;
//						}
//					}
					break;
				}
			}

		}
		
		private void constructMagnitudeFromXml(String magnitudeName, TriggerChannelDTO triggerChannelDto)
		{
			String inputValue = "";
			String analogName= "";
//			String exportName = "";
//			int startIndex;
//			  Exports exports = xmlConfigDfr.getDataPool().getExports();
//			  Export export[] = null;
//			  if (exports != null)
//			  {
//				  export = exports.getExportArray();
//			  }
			  PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
//			  PmuInput[] pmuInput = null;
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
			Magnitude magnitude[] = xmlConfigDfr.getDataPool().getAlgorithms().getMagnitudeArray();
			triggerChannelDto.setDisableHarmonic(false);
//			magnitudeName = xmlAlgoType.substring(xmlAlgoType.indexOf("(")+1, xmlAlgoType.indexOf(")"));
//			logger.debug("Magnitude Name: "+magnitudeName);
			int analogOffset = getDfrAnalogOffset("DFR"+xmlConfigDfr.getSystem().getDfrId());
			for (int j = 0; j < magnitude.length; j++) {
				if (magnitude[j].getName().equals(magnitudeName))
				{
					inputValue = magnitude[j].getInput();
					if (magnitude[j].getAverage() > 0)
					{
						triggerChannelDto.setAverage(magnitude[j].getAverage());
					}
					triggerChannelDto.setHarmonic(""+magnitude[j].getHarmonic());
					analogName = inputValue.substring(inputValue.indexOf("AnalogInput(")+12,inputValue.lastIndexOf(")"));
					for (int j2 = 0; j2 < analogInputs.length; j2++) {
						if (analogInputs[j2].getName().equals(analogName))
						{
//							logger.debug("Analog Channel..."+(analogInputs[j2].getChannel()+analogOffset)+" For name... "+analogInputs[j2].getName());
							  triggerChannelDto.setInputChannelName(analogInputs[j2].getName());

							triggerChannelDto.setChannel(""+(analogInputs[j2].getChannel()+analogOffset)+"-"+M9kConstants.TRIGGER_INPUT_TYPE_ANALOG+"-"+analogInputs[j2].getCircuitName());
							// START: 28-Jan-2020 - Transducer Implementation - IF the channel is modified to transducer during editing then set the value to true
							if (analogInputs[j2].isSetTransducer() && analogInputs[j2].getTransducer() == 1)
							{
								triggerChannelDto.setTransducer(true);
								triggerChannelDto.setUnits(analogInputs[j2].getTransducerUnits());
								triggerChannelDto.setType(analogInputs[j2].getTransducerUnits()); // Type is same as Transducer Units
							}
							// END: 28-Jan-2020 

							if (analogInputs[j2].getInputType().startsWith(M9kConstants.VOLTAGE))
							{
								triggerChannelDto.setUnits("V");
							}
							else
							{
								triggerChannelDto.setUnits("A");
							}
							break;
						}
					}
//					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
//					{
//						inputValue = export[exportCnt].getInput();
//						startIndex = inputValue.indexOf("Magnitude(");
//						if (startIndex == -1)
//						{
//							continue;
//						}
//						else
//						{
//							startIndex+=10;
//						}
//						exportName = inputValue.substring(startIndex,inputValue.lastIndexOf(")"));
//						if (exportName.equals(magnitudeName))
//						{
//							if (export[exportCnt].getSampleRate() > 0)
//							{
//								triggerChannelDto.setExportRate(export[exportCnt].getSampleRate());
//							}
//							for (int pmuCnt=0; pmuInput != null && pmuCnt < pmuInput.length;pmuCnt++)
//							{
//								if (pmuInput[pmuCnt].getDfr() == xmlConfigDfr.getSystem().getDfrId() && pmuInput[pmuCnt].getId() == export[exportCnt].getId())
//								{
//									if (pmuInput[pmuCnt].getPmuEnable() == M9kConstants.ENABLE_ONE)
//									{
//										triggerChannelDto.setPmuStatus(true);
//									}
//									else
//									{
//										triggerChannelDto.setPmuStatus(false);
//									}
//									break;
//								}
//							}
//							break;
//						}
//					}
					break;
				}
			}		
		}

		private void constructFrequencyFromXml(String frequencyName, TriggerChannelDTO triggerChannelDto)
		{
			String inputValue = "";
			String analogName= "";
			String exportName = "";
			int startIndex;
			  Exports exports = xmlConfigDfr.getDataPool().getExports();
			  Export export[] = null;
			  if (exports != null)
			  {
				  export = exports.getExportArray();
			  }
			  PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
			  PmuInput[] pmuInput = null;
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
			Frequency frequency[] = xmlConfigDfr.getDataPool().getAlgorithms().getFrequencyArray();		
			triggerChannelDto.setDisableHarmonic(true);
//			rmsName = xmlAlgoType.substring(xmlAlgoType.indexOf("(")+1, xmlAlgoType.indexOf(")"));
//			logger.debug("Freq Name: "+frequencyName);
			int analogOffset = getDfrAnalogOffset("DFR"+xmlConfigDfr.getSystem().getDfrId());
			for (int i = 0; i < frequency.length; i++) {
				if (frequency[i].getName().equals(frequencyName))
				{
					inputValue = frequency[i].getInput();
					if (frequency[i].getAverage() > 0)
					{
						triggerChannelDto.setAverage(frequency[i].getAverage());
					}
//					logger.debug("frequency input string..."+inputValue);
					analogName = inputValue.substring(inputValue.indexOf("AnalogInput(")+12,inputValue.lastIndexOf(")"));
//					logger.debug("analogName input for frequency..."+analogName);
				
					for (int j2 = 0; j2 < analogInputs.length; j2++) {
//						logger.debug("analogInputs[j2].getName()..."+analogInputs[j2].getName());
						if (analogInputs[j2].getName().equals(analogName))
						{
							triggerChannelDto.setInputChannelName(analogInputs[j2].getName());
							triggerChannelDto.setChannel(""+(analogInputs[j2].getChannel()+analogOffset)+"-"+M9kConstants.TRIGGER_INPUT_TYPE_ANALOG+"-"+analogInputs[j2].getCircuitName());
							// START: 28-Jan-2020 - Transducer Implementation - IF the channel is modified to transducer during editing then set the value to true
							if (analogInputs[j2].isSetTransducer() && analogInputs[j2].getTransducer() == 1)
							{
								triggerChannelDto.setTransducer(true);
								triggerChannelDto.setUnits(analogInputs[j2].getTransducerUnits());
								triggerChannelDto.setType(analogInputs[j2].getTransducerUnits()); // Type is same as Transducer Units
							}
							// END: 28-Jan-2020 

//							logger.debug("Analog INput Channel..."+(analogInputs[j2].getChannel()+analogOffset));
							break;
						}
					}
					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
					{
						inputValue = export[exportCnt].getInput();
						startIndex = inputValue.indexOf("Frequency(");
						if (startIndex == -1)
						{
							continue;
						}
						else
						{
							startIndex+=10;
						}
						exportName = inputValue.substring(startIndex,inputValue.lastIndexOf(")"));
						if (exportName.equals(frequencyName))
						{
//							if (export[exportCnt].getSampleRate() > 0)
//							{
//								triggerChannelDto.setExportRate(export[exportCnt].getSampleRate());
//							}
							for (int pmuCnt=0; pmuInput != null && pmuCnt < pmuInput.length;pmuCnt++)
							{
								if (pmuInput[pmuCnt].getDfr() == xmlConfigDfr.getSystem().getDfrId() && pmuInput[pmuCnt].getId() == export[exportCnt].getId())
								{
//									if (pmuInput[pmuCnt].getPmuEnable() == M9kConstants.ENABLE_ONE)
//									{
//										triggerChannelDto.setPmuStatus(true);
//									}
//									else
//									{
//										triggerChannelDto.setPmuStatus(false);
//									}
									if (pmuInput[pmuCnt].getPmuInputType() != null && pmuInput[pmuCnt].getPmuInputType().equalsIgnoreCase("Freq"))
									{
										triggerChannelDto.setFreqPmuStatus(true);
									}
									break;
								}
							}
							break;
						}
					}
					break;
				}
			}
		}

		private void constructPhaseFromXml(String phaseName, TriggerChannelDTO triggerChannelDto)
		{
			String inputValue = "";
			String analogName= "";
//			String exportName = "";
//			int startIndex;
//			  Exports exports = xmlConfigDfr.getDataPool().getExports();
//			  Export export[] = null;
//			  if (exports != null)
//			  {
//				  export = exports.getExportArray();
//			  }
			  PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
//			  PmuInput[] pmuInput = null;
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
			Phase phase[] = xmlConfigDfr.getDataPool().getAlgorithms().getPhaseArray();		
			triggerChannelDto.setDisableHarmonic(true);
			triggerChannelDto.setType(M9kConstants.PHASOR);
//			rmsName = xmlAlgoType.substring(xmlAlgoType.indexOf("(")+1, xmlAlgoType.indexOf(")"));
//			logger.debug("Phase Name: "+phaseName);
			int analogOffset = getDfrAnalogOffset("DFR"+xmlConfigDfr.getSystem().getDfrId());
			for (int i = 0; i < phase.length; i++) {
				if (phase[i].getName().equals(phaseName))
				{
					inputValue = phase[i].getInput();
					if (phase[i].getAverage() > 0)
					{
						triggerChannelDto.setAverage(phase[i].getAverage());
					}
//					logger.debug("phase input string..."+inputValue);
					analogName = inputValue.substring(inputValue.indexOf("AnalogInput(")+12,inputValue.lastIndexOf(")"));
//					logger.debug("analogName input for phase..."+analogName);
				
					for (int j2 = 0; j2 < analogInputs.length; j2++) {
//						logger.debug("analogInputs[j2].getName()..."+analogInputs[j2].getName());
						if (analogInputs[j2].getName().equals(analogName))
						{
							triggerChannelDto.setInputChannelName(analogInputs[j2].getName());
							triggerChannelDto.setChannel(""+(analogInputs[j2].getChannel()+analogOffset)+"-"+M9kConstants.TRIGGER_INPUT_TYPE_ANALOG+"-"+analogInputs[j2].getCircuitName());
							// START: 28-Jan-2020 - Transducer Implementation - IF the channel is modified to transducer during editing then set the value to true
							if (analogInputs[j2].isSetTransducer() && analogInputs[j2].getTransducer() == 1)
							{
								triggerChannelDto.setTransducer(true);
								triggerChannelDto.setUnits(analogInputs[j2].getTransducerUnits());
								triggerChannelDto.setType(analogInputs[j2].getTransducerUnits()); // Type is same as Transducer Units
							}
							// END: 28-Jan-2020 

//							logger.debug("Analog INput Channel..."+(analogInputs[j2].getChannel()+analogOffset));
							break;
						}
					}
//					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
//					{
//						inputValue = export[exportCnt].getInput();
//						startIndex = inputValue.indexOf("Phase(");
//						if (startIndex == -1)
//						{
//							continue;
//						}
//						else
//						{
//							startIndex+=10;
//						}
//						exportName = inputValue.substring(startIndex,inputValue.lastIndexOf(")"));
//						if (exportName.equals(phaseName))
//						{
//							triggerChannelDto.setExportStatus(true);
//							triggerChannelDto.setExportRate(export[exportCnt].getSampleRate());
//							for (int pmuCnt=0; pmuInput != null && pmuCnt < pmuInput.length;pmuCnt++)
//							{
//								if (pmuInput[pmuCnt].getDfr() == xmlConfigDfr.getSystem().getDfrId() && pmuInput[pmuCnt].getId() == export[exportCnt].getId())
//								{
//									triggerChannelDto.setPmuStatus(true);
//									break;
//								}
//							}
//							break;
//						}
//					}
					break;
				}
			}
		}
		
		private void constructZeroSequenceFromXml(String zeroSequenceName, TriggerChannelDTO triggerChannelDto)
		{
			String lineGroupName = "";
			String inputValue = "";
			String inputType="";
//			String exportName = "";
//			int startIndex;
		    ZeroSequence zeroSequence[] = xmlConfigDfr.getDataPool().getAlgorithms().getZeroSequenceArray();
		    LineGroup[] lineGroups = xmlConfigDfr.getDataPool().getLineGroups().getLineGroupArray();
//			  Exports exports = xmlConfigDfr.getDataPool().getExports();
//			  Export export[] = null;
//			  if (exports != null)
//			  {
//				  export = exports.getExportArray();
//			  }
			
			  PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
//			  PmuInput[] pmuInput = null;
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			triggerChannelDto.setDisableHarmonic(true);
			logger.debug("ZS Name: "+zeroSequenceName);
			for (int j = 0; j < zeroSequence.length; j++) {
				if (zeroSequence[j].getName().equals(zeroSequenceName))
				{
						inputValue = zeroSequence[j].getInput();
						inputType = zeroSequence[j].getInputType();
						if (zeroSequence[j].getAverage() > 0)
						{
							triggerChannelDto.setAverage(zeroSequence[j].getAverage());
						}
						logger.debug("ZS input Value: "+inputValue+" input Type: "+inputType);
						if (inputType.equalsIgnoreCase(M9kConstants.VOLTAGE))
						{
							triggerChannelDto.setType(M9kConstants.ZERO_SEQUENCE_V);
						}
						else
						{
							triggerChannelDto.setType(M9kConstants.ZERO_SEQUENCE_I);
						}
					triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP);
					lineGroupName = inputValue.substring(inputValue.indexOf("LineGroup(")+10,inputValue.lastIndexOf(")"));
					for (int j2 = 0; j2 < lineGroups.length; j2++) {
						if (lineGroups[j2].getName().equals(lineGroupName) || (lineGroups[j2].getLineGroupName() != null && (lineGroups[j2].getLineGroupName().equals(lineGroupName))))
						{
							triggerChannelDto.setInputChannelName(lineGroups[j2].getName());
							if (lineGroups[j2].getLineGroupName() != null)
							{
								triggerChannelDto.setChannel(""+lineGroups[j2].getId()+"-"+M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP+"-"+lineGroups[j2].getLineGroupName());
							}
							else
							{
								triggerChannelDto.setChannel(""+lineGroups[j2].getId()+"-"+M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP+"-"+lineGroups[j2].getName());
							}

							logger.debug("ZS: LineGroup INput Channel..."+triggerChannelDto.getChannel());
							break;
						}
					}
//					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
//					{
//						inputValue = export[exportCnt].getInput();
//						startIndex = inputValue.indexOf(triggerChannelDto.getType());
//						if (startIndex == -1)
//						{
//							continue;
//						}
//						else
//						{
//							startIndex+=triggerChannelDto.getType().length()+1;
//						}
//						exportName = inputValue.substring(startIndex,inputValue.lastIndexOf(")"));
//						if (exportName.equals(zeroSequenceName))
//						{
//							if (export[exportCnt].getSampleRate() > 0)
//							{
//								triggerChannelDto.setExportRate(export[exportCnt].getSampleRate());
//							}
//							for (int pmuCnt=0; pmuInput != null && pmuCnt < pmuInput.length;pmuCnt++)
//							{
//								if (pmuInput[pmuCnt].getDfr() == xmlConfigDfr.getSystem().getDfrId() && pmuInput[pmuCnt].getId() == export[exportCnt].getId())
//								{
//									if (pmuInput[pmuCnt].getPmuEnable() == M9kConstants.ENABLE_ONE)
//									{
//										triggerChannelDto.setPmuStatus(true);
//									}
//									else
//									{
//										triggerChannelDto.setPmuStatus(false);
//									}
//									break;
//								}
//							}
//							break;
//						}
//					}

					break;
				}
			}
			
		}
		
		private void constructPositiveSequenceFromXml(String positiveSequenceName, TriggerChannelDTO triggerChannelDto)
		{
			String lineGroupName = "";
			String inputValue = "";
			String inputType="";
//			String exportName = "";
//			int startIndex;
		    PositiveSequence positiveSequence[] = xmlConfigDfr.getDataPool().getAlgorithms().getPositiveSequenceArray();
		    LineGroup[] lineGroups = xmlConfigDfr.getDataPool().getLineGroups().getLineGroupArray();
//			  Exports exports = xmlConfigDfr.getDataPool().getExports();
//			  Export export[] = null;
//			  if (exports != null)
//			  {
//				  export = exports.getExportArray();
//			  }
			  PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
//			  PmuInput[] pmuInput = null;
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			triggerChannelDto.setDisableHarmonic(true);
			logger.debug("ZS Name: "+positiveSequenceName);
			for (int j = 0; j < positiveSequence.length; j++) {
				if (positiveSequence[j].getName().equals(positiveSequenceName))
				{
						inputValue = positiveSequence[j].getInput();
						inputType = positiveSequence[j].getInputType();
						if (positiveSequence[j].getAverage() > 0)
						{
							triggerChannelDto.setAverage(positiveSequence[j].getAverage());
						}
						logger.debug("ZS input Value: "+inputValue+" input Type: "+inputType);
						if (inputType.equalsIgnoreCase(M9kConstants.VOLTAGE))
						{
							triggerChannelDto.setType(M9kConstants.POSITIVE_SEQUENCE_V);
						}
						else
						{
							triggerChannelDto.setType(M9kConstants.POSITIVE_SEQUENCE_I);
						}
					triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP);
					lineGroupName = inputValue.substring(inputValue.indexOf("LineGroup(")+10,inputValue.lastIndexOf(")"));
					
					for (int j2 = 0; j2 < lineGroups.length; j2++) {
						if (lineGroups[j2].getName().equals(lineGroupName) || (lineGroups[j2].getLineGroupName() != null && (lineGroups[j2].getLineGroupName().equals(lineGroupName))))
						{
							triggerChannelDto.setInputChannelName(lineGroups[j2].getName());
							if (lineGroups[j2].getLineGroupName() != null)
							{
								triggerChannelDto.setChannel(""+lineGroups[j2].getId()+"-"+M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP+"-"+lineGroups[j2].getLineGroupName());
							}
							else
							{
								triggerChannelDto.setChannel(""+lineGroups[j2].getId()+"-"+M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP+"-"+lineGroups[j2].getName());
							}
							logger.debug("PS: LineGroup INput Channel..."+triggerChannelDto.getChannel());
							break;
						}
					}
//					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
//					{
//						inputValue = export[exportCnt].getInput();
//						startIndex = inputValue.indexOf(triggerChannelDto.getType());
//						if (startIndex == -1)
//						{
//							continue;
//						}
//						else
//						{
//							startIndex+=triggerChannelDto.getType().length()+1;
//						}
//						exportName = inputValue.substring(startIndex,inputValue.lastIndexOf(")"));
//						if (exportName.equals(positiveSequenceName))
//						{
//							if (export[exportCnt].getSampleRate() > 0)
//							{
//								triggerChannelDto.setExportRate(export[exportCnt].getSampleRate());
//							}
//							for (int pmuCnt=0; pmuInput != null && pmuCnt < pmuInput.length;pmuCnt++)
//							{
//								if (pmuInput[pmuCnt].getDfr() == xmlConfigDfr.getSystem().getDfrId() && pmuInput[pmuCnt].getId() == export[exportCnt].getId())
//								{
//									if (pmuInput[pmuCnt].getPmuEnable() == M9kConstants.ENABLE_ONE)
//									{
//										triggerChannelDto.setPmuStatus(true);
//									}
//									else
//									{
//										triggerChannelDto.setPmuStatus(false);
//									}
//									break;
//								}
//							}
//							break;
//						}
//					}
					break;
				}
			}
			
		}

		private void constructNegativeSequenceFromXml(String negativeSequenceName, TriggerChannelDTO triggerChannelDto)
		{
			String lineGroupName = "";
			String inputValue = "";
			String inputType="";
//			String exportName = "";
//			int startIndex;
			
		    NegativeSequence negativeSequence[] = xmlConfigDfr.getDataPool().getAlgorithms().getNegativeSequenceArray();
		    LineGroup[] lineGroups = xmlConfigDfr.getDataPool().getLineGroups().getLineGroupArray();
//			  Exports exports = xmlConfigDfr.getDataPool().getExports();
//			  Export export[] = null;
//			  if (exports != null)
//			  {
//				  export = exports.getExportArray();
//			  }
			  PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
//			  PmuInput[] pmuInput = null;
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			triggerChannelDto.setDisableHarmonic(true);
			logger.debug("ZS Name: "+negativeSequenceName);
			for (int j = 0; j < negativeSequence.length; j++) {
				if (negativeSequence[j].getName().equals(negativeSequenceName))
				{
						inputValue = negativeSequence[j].getInput();
						inputType = negativeSequence[j].getInputType();
						if (negativeSequence[j].getAverage() > 0)
						{
							triggerChannelDto.setAverage(negativeSequence[j].getAverage());
						}
						logger.debug("ZS input Value: "+inputValue+" input Type: "+inputType);
						if (inputType.equalsIgnoreCase(M9kConstants.VOLTAGE))
						{
							triggerChannelDto.setType(M9kConstants.NEGATIVE_SEQUENCE_V);
						}
						else
						{
							triggerChannelDto.setType(M9kConstants.NEGATIVE_SEQUENCE_I);
						}
					triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP);
					lineGroupName = inputValue.substring(inputValue.indexOf("LineGroup(")+10,inputValue.lastIndexOf(")"));
					for (int j2 = 0; j2 < lineGroups.length; j2++) {
						if (lineGroups[j2].getName().equals(lineGroupName) || (lineGroups[j2].getLineGroupName() != null && (lineGroups[j2].getLineGroupName().equals(lineGroupName))))
						{
							triggerChannelDto.setInputChannelName(lineGroups[j2].getName());
							if (lineGroups[j2].getLineGroupName() != null)
							{
								triggerChannelDto.setChannel(""+lineGroups[j2].getId()+"-"+M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP+"-"+lineGroups[j2].getLineGroupName());
							}
							else
							{
								triggerChannelDto.setChannel(""+lineGroups[j2].getId()+"-"+M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP+"-"+lineGroups[j2].getName());
							}

							logger.debug("NS: LineGroup INput Channel..."+triggerChannelDto.getChannel());
							break;
						}
					}
//					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
//					{
//						inputValue = export[exportCnt].getInput();
//						startIndex = inputValue.indexOf(triggerChannelDto.getType());
//						if (startIndex == -1)
//						{
//							continue;
//						}
//						else
//						{
//							startIndex+=triggerChannelDto.getType().length()+1;
//						}
//						exportName = inputValue.substring(startIndex,inputValue.lastIndexOf(")"));
//						if (exportName.equals(negativeSequenceName))
//						{
//							if (export[exportCnt].getSampleRate() > 0)
//							{
//								triggerChannelDto.setExportRate(export[exportCnt].getSampleRate());
//							}
//							for (int pmuCnt=0; pmuInput != null && pmuCnt < pmuInput.length;pmuCnt++)
//							{
//								if (pmuInput[pmuCnt].getDfr() == xmlConfigDfr.getSystem().getDfrId() && pmuInput[pmuCnt].getId() == export[exportCnt].getId())
//								{
//									if (pmuInput[pmuCnt].getPmuEnable() == M9kConstants.ENABLE_ONE)
//									{
//										triggerChannelDto.setPmuStatus(true);
//									}
//									else
//									{
//										triggerChannelDto.setPmuStatus(false);
//									}
//									break;
//								}
//							}
//							break;
//						}
//					}
					break;
				}
			}
			
		}

		private void constructPowerFromXml(String powerName, TriggerChannelDTO triggerChannelDto)
		{
			String lineGroupName = "";
			String inputValue = "";
			String inputs="";
			String powerType="";
//			String exportName = "";
//			int startIndex;
			
		    Power power[] = xmlConfigDfr.getDataPool().getAlgorithms().getPowerArray();
		    LineGroup[] lineGroups = xmlConfigDfr.getDataPool().getLineGroups().getLineGroupArray();
//			  Exports exports = xmlConfigDfr.getDataPool().getExports();
//			  Export export[] = null;
//			  if (exports != null)
//			  {
//				  export = exports.getExportArray();
//			  }
//			  PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
//			  PmuInput[] pmuInput = null;
//			  if (pmuInputs != null)
//			  {
//				  pmuInput = pmuInputs.getPmuInputArray();
//			  }
			triggerChannelDto.setDisableHarmonic(true);
			logger.debug("Power Name: "+powerName);
			for (int j = 0; j < power.length; j++) {
				if (power[j].getName().equals(powerName))
				{
						inputValue = power[j].getInput();
						inputs = power[j].getInputs();
						powerType = power[j].getPowerType();
						triggerChannelDto.setType(powerType);
						if (powerType.equalsIgnoreCase(M9kConstants.WATTS_REAL))
						{
							if (inputs.equalsIgnoreCase("A"))
							{
								triggerChannelDto.setType(M9kConstants.A_PHASE_WATTS);
							}
							else if (inputs.equalsIgnoreCase("B"))
							{
								triggerChannelDto.setType(M9kConstants.B_PHASE_WATTS);
							} 
							else if (inputs.equalsIgnoreCase("C"))
							{
								triggerChannelDto.setType(M9kConstants.C_PHASE_WATTS);
							} 
							else
							{
								triggerChannelDto.setType(M9kConstants.ALL_PHASE_WATTS);
							}
						}
						else if (powerType.equalsIgnoreCase(M9kConstants.VARS_REACTIVE))
						{
							if (inputs.equalsIgnoreCase("A"))
							{
								triggerChannelDto.setType(M9kConstants.A_PHASE_VARS);
							}
							else if (inputs.equalsIgnoreCase("B"))
							{
								triggerChannelDto.setType(M9kConstants.B_PHASE_VARS);
							} 
							else if (inputs.equalsIgnoreCase("C"))
							{
								triggerChannelDto.setType(M9kConstants.C_PHASE_VARS);
							} 
							else
							{
								triggerChannelDto.setType(M9kConstants.ALL_PHASE_VARS);
							}
						}
						// START: 17-Mar-2021 - Apparent power implementation
						else 
						{
							if (inputs.equalsIgnoreCase("A"))
							{
								triggerChannelDto.setType(M9kConstants.A_PHASE_APPARENT);
							}
							else if (inputs.equalsIgnoreCase("B"))
							{
								triggerChannelDto.setType(M9kConstants.B_PHASE_APPARENT);
							} 
							else if (inputs.equalsIgnoreCase("C"))
							{
								triggerChannelDto.setType(M9kConstants.C_PHASE_APPARENT);
							} 
							else
							{
								triggerChannelDto.setType(M9kConstants.ALL_PHASE_APPARENT);
							}
						}
						// END: 17-Mar-2021 - Apparent power implementation
					triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP);
					lineGroupName = inputValue.substring(inputValue.indexOf("LineGroup(")+10,inputValue.lastIndexOf(")"));
					for (int j2 = 0; j2 < lineGroups.length; j2++) {
						if (lineGroups[j2].getName().equals(lineGroupName) || (lineGroups[j2].getLineGroupName() != null && (lineGroups[j2].getLineGroupName().equals(lineGroupName))))
						{
							triggerChannelDto.setInputChannelName(lineGroups[j2].getName());
							if (lineGroups[j2].getLineGroupName() != null)
							{
								triggerChannelDto.setChannel(""+lineGroups[j2].getId()+"-"+M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP+"-"+lineGroups[j2].getLineGroupName());
							}
							else
							{
								triggerChannelDto.setChannel(""+lineGroups[j2].getId()+"-"+M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP+"-"+lineGroups[j2].getName());
							}

							logger.debug("Power: LineGroup INput Channel..."+triggerChannelDto.getChannel());
							break;
						}
					}
//					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
//					{
//						inputValue = export[exportCnt].getInput();
//						startIndex = inputValue.indexOf(triggerChannelDto.getType());
//						if (startIndex == -1)
//						{
//							continue;
//						}
//						else
//						{
//							startIndex+=triggerChannelDto.getType().length()+1;
//						}
//						exportName = inputValue.substring(startIndex,inputValue.lastIndexOf(")"));
//						if (exportName.equals(powerName))
//						{
//							if (export[exportCnt].getSampleRate() > 0)
//							{
//								triggerChannelDto.setExportRate(export[exportCnt].getSampleRate());
//							}
//							for (int pmuCnt=0; pmuInput != null && pmuCnt < pmuInput.length;pmuCnt++)
//							{
//								if (pmuInput[pmuCnt].getDfr() == xmlConfigDfr.getSystem().getDfrId() && pmuInput[pmuCnt].getId() == export[exportCnt].getId())
//								{
//									if (pmuInput[pmuCnt].getPmuEnable() == M9kConstants.ENABLE_ONE)
//									{
//										triggerChannelDto.setPmuStatus(true);
//									}
//									else
//									{
//										triggerChannelDto.setPmuStatus(false);
//									}
//									break;
//								}
//							}
//							break;
//						}
//					}
					break;
				}
			}
			
		}
		
		// START: 28-Jan-2020 - Implementing Transducer - Average algorithm
		private void constructAverageFromXml(String rmsName, TriggerChannelDTO triggerChannelDto)
		{
			String inputValue = "";
			String analogName= "";
//			int startIndex;
			
			AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
			
//			  Exports exports = xmlConfigDfr.getDataPool().getExports();
//			  Export export[] = null;
//			  if (exports != null)
//			  {
//				  export = exports.getExportArray();
//			  }
			  PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			Average average[] = xmlConfigDfr.getDataPool().getAlgorithms().getAverageArray();		
			triggerChannelDto.setDisableHarmonic(true);
			int analogOffset = getDfrAnalogOffset("DFR"+xmlConfigDfr.getSystem().getDfrId());
			for (int j = 0; j < average.length; j++) {
				if (average[j].getName().equals(rmsName))
				{
					inputValue = average[j].getInput();
					if (average[j].getAverage() > 0)
					{
						triggerChannelDto.setAverage(average[j].getAverage());
					}
					analogName = inputValue.substring(inputValue.indexOf("AnalogInput(")+12,inputValue.lastIndexOf(")"));
					for (int j2 = 0; j2 < analogInputs.length; j2++) {
						if (analogInputs[j2].getName().equals(analogName))
						{
							triggerChannelDto.setInputChannelName(analogInputs[j2].getName());
							triggerChannelDto.setChannel(""+(analogInputs[j2].getChannel()+analogOffset)+"-"+M9kConstants.TRIGGER_INPUT_TYPE_ANALOG+"-"+analogInputs[j2].getCircuitName());
							// START: 04-Feb-2020 - Tranducer - To fix an issue of disabling exports and ddr for RMS if transducer channel changed back to normal channe;
//							if (analogInputs[j2].isSetTransducer() && analogInputs[j2].getTransducer() == 1)
							// END: 04-Feb-2020
							{
								triggerChannelDto.setTransducer(true);
								triggerChannelDto.setUnits(analogInputs[j2].getTransducerUnits());
								triggerChannelDto.setType(analogInputs[j2].getTransducerUnits()); // Type is same as Transducer Units
							}
							
//							logger.debug("Analog INput Channel..."+(analogInputs[j2].getChannel()+analogOffset));
							break;
						}
					}
					 
//					for (int exportCnt=0; (export!=null&&exportCnt < export.length);exportCnt++)
//					{
//						inputValue = export[exportCnt].getInput();
//						startIndex = inputValue.indexOf("Average(");
//						if (startIndex == -1)
//						{
//							continue;
//						}
//						else
//						{
//							startIndex+=8;
//						}
//					}
					break;
				}
			}

		}
		// END: 28-Jan-2020

		/**
		 * START: 09-Mar-2020 - Implementing virtual measurements for delta transformers
		 * @param virtualMeasurementName
		 * @param triggerChannelDto
		 */
		private void constructVirtualMeasurementFromXmlOld(String virtualMeasurementName, TriggerChannelDTO triggerChannelDto)
		{
			String inputValue = "";
			String analogName= "";
			
			AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
			
			  PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			Average average[] = xmlConfigDfr.getDataPool().getAlgorithms().getAverageArray();		
			triggerChannelDto.setDisableHarmonic(true);
			int analogOffset = getDfrAnalogOffset("DFR"+xmlConfigDfr.getSystem().getDfrId());
			for (int j = 0; j < average.length; j++) {
				if (average[j].getName().equals(virtualMeasurementName))
				{
					inputValue = average[j].getInput();
					if (average[j].getAverage() > 0)
					{
						triggerChannelDto.setAverage(average[j].getAverage());
					}
					analogName = inputValue.substring(inputValue.indexOf("AnalogInput(")+12,inputValue.lastIndexOf(")"));
					for (int j2 = 0; j2 < analogInputs.length; j2++) {
						if (analogInputs[j2].getName().equals(analogName))
						{
							triggerChannelDto.setInputChannelName(analogInputs[j2].getName());
							triggerChannelDto.setChannel(""+(analogInputs[j2].getChannel()+analogOffset)+"-"+M9kConstants.TRIGGER_INPUT_TYPE_ANALOG+"-"+analogInputs[j2].getCircuitName());
							// START: 04-Feb-2020 - Tranducer - To fix an issue of disabling exports and ddr for RMS if transducer channel changed back to normal channe;
//							if (analogInputs[j2].isSetTransducer() && analogInputs[j2].getTransducer() == 1)
							// END: 04-Feb-2020
							{
								triggerChannelDto.setTransducer(true);
								triggerChannelDto.setUnits(analogInputs[j2].getTransducerUnits());
								triggerChannelDto.setType(analogInputs[j2].getTransducerUnits()); // Type is same as Transducer Units
							}
							
//							logger.debug("Analog INput Channel..."+(analogInputs[j2].getChannel()+analogOffset));
							break;
						}
					}
					 
					break;
				}
			}

		}
		// END: 28-Jan-2020

		
		private void populateInputTriggerChannels()
	  {
		  logger.debug("Entered populateInputTriggerChannels..."+getLstVirtualChannels());
		  TriggerSourceDTO triggerSourceDTO;
//		  String sourceDisplayName;
		  lstTriggerSourceDto = new ArrayList<TriggerSourceDTO>();
		  for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels().iterator(); iterator.hasNext();) {
			AnalogChannelDTO analogChannelDTO = iterator.next();
			triggerSourceDTO = new TriggerSourceDTO();
			triggerSourceDTO.setSourceName(analogChannelDTO.getChannel());
			triggerSourceDTO.setSourceTriggerName(analogChannelDTO.getCircuitName());
			triggerSourceDTO.setSourceInputObjName(analogChannelDTO.getName());
			triggerSourceDTO.setSourceType(M9kConstants.TRIGGER_INPUT_TYPE_ANALOG);
			logger.debug("DELETE-DEBUG: in populateInputTriggerChannels sourceName "+analogChannelDTO.getChannel()+" SourceTriggerName "+analogChannelDTO.getCircuitName()+" SourceType "+M9kConstants.TRIGGER_INPUT_TYPE_ANALOG+" SourceDisplayName "+analogChannelDTO.getDisplayName()+" Chassis "+analogChannelDTO.getChassis());
//			sourceDisplayName = analogChannelDTO.getDisplayName().substring(analogChannelDTO.getDisplayName().indexOf("-")+1);
			triggerSourceDTO.setSourceDisplayName(analogChannelDTO.getDisplayName());
			triggerSourceDTO.setChassis(analogChannelDTO.getChassis());
			// START: 24-Jan-2020  - Implementing Transducer- Trigger algorithm is set to Transduce Units entered by the user
			logger.debug("Is transducer set? "+analogChannelDTO.isTransducer());
			if (!analogChannelDTO.isTransducer())
			{
				// START: 17-Mar-2021 - Implementing Apparent power. Modifying list to Map
//				triggerSourceDTO.setLstTriggerInputTypes(getTriggerTypes(triggerSourceDTO));
				triggerSourceDTO.setMapTriggerInputTypes(getTriggerTypes(triggerSourceDTO));
				triggerSourceDTO.setTransducer(false);
			}
			else
			{
				// Add transducer units as the only trigger option
//				triggerSourceDTO.setLstTriggerInputTypes(new ArrayList<Object>(Arrays.asList(analogChannelDTO.getTransducerUnits(),M9kConstants.DELETE)));
				Map<String, String> mapTransducerUnits = new LinkedHashMap<String, String>(1);
				mapTransducerUnits.put(analogChannelDTO.getTransducerUnits(), analogChannelDTO.getTransducerUnits());
				mapTransducerUnits.put(M9kConstants.DELETE, M9kConstants.DELETE);
				triggerSourceDTO.setMapTriggerInputTypes(mapTransducerUnits);
				// END 17-Mar-2021 - Apparent power implementation. Modifying list to map
				triggerSourceDTO.setTransducer(true);
			}
			logger.debug("DELETE-DEBUG: map types set for trigger channel..."+triggerSourceDTO.getMapTriggerInputTypes());
			// END: 24-Jan-2020
			lstTriggerSourceDto.add(triggerSourceDTO);
		  }
		  // For Virtual channels
		  if (getLstVirtualChannels() != null && !getLstVirtualChannels().isEmpty())
		  {
			  for (Iterator<AnalogChannelDTO> iterator = getLstVirtualChannels().iterator(); iterator.hasNext();) {
					AnalogChannelDTO analogChannelDTO = iterator.next();
					triggerSourceDTO = new TriggerSourceDTO();
					triggerSourceDTO.setSourceName(analogChannelDTO.getChannel());
					triggerSourceDTO.setSourceTriggerName(analogChannelDTO.getCircuitName());
					triggerSourceDTO.setSourceInputObjName(analogChannelDTO.getName());
					triggerSourceDTO.setSourceType(M9kConstants.TRIGGER_INPUT_TYPE_ANALOG);
//					sourceDisplayName = analogChannelDTO.getDisplayName().substring(analogChannelDTO.getDisplayName().indexOf("-")+1);
					triggerSourceDTO.setSourceDisplayName(analogChannelDTO.getDisplayName());
					triggerSourceDTO.setChassis(analogChannelDTO.getChassis());
					// START: 17-Mar-2021 - Implementing Apparent power. Modifying list to Map
//					triggerSourceDTO.setLstTriggerInputTypes(getTriggerTypes(triggerSourceDTO));
					triggerSourceDTO.setMapTriggerInputTypes(getTriggerTypes(triggerSourceDTO));
					lstTriggerSourceDto.add(triggerSourceDTO);
				  }
		  }
		  
		  if (getLstLineGroups() != null)
		  {
			  for (Iterator<LineGroupsAlgorithm> iterator = getLstLineGroups().iterator(); iterator.hasNext();) {
				  LineGroupsAlgorithm lineGroup = iterator.next();
//				  logger.debug("\t\t\tLine group "+lineGroup.getName()+" chassis "+lineGroup.getChassis()+" Is Local? "+lineGroup.isLocal());
				  if (lineGroup.isLocal() && !lineGroup.isInvalidCombination())
				  {
					triggerSourceDTO = new TriggerSourceDTO();
					triggerSourceDTO.setSourceLineGroupId(lineGroup.getId());
					triggerSourceDTO.setSourceName(""+lineGroup.getId());
					if (lineGroup.getLineGroupName() != null && !lineGroup.getLineGroupName().isEmpty())
					{
						triggerSourceDTO.setSourceTriggerName(lineGroup.getLineGroupName());
						// START: 10-Mar-2020 - Display name of line groups to be included with description instad of name
//						triggerSourceDTO.setSourceDisplayName(lineGroup.getChassis()+" - "+lineGroup.getDisplayName());
						// 18-Mar-2021 - Padded with space
						triggerSourceDTO.setSourceDisplayName("["+M9kUtils.rightpad(lineGroup.getChassis(),6)+M9kUtils.rightpad(lineGroup.getName(),5)+"] "+lineGroup.getLineGroupName());
						// END: 10-Mar-2020
					}
					else
					{
						triggerSourceDTO.setSourceTriggerName(lineGroup.getName());
						// START: 10-Mar-2020 - Display name of line groups to be included with description instad of name
//						triggerSourceDTO.setSourceDisplayName(lineGroup.getChassis()+" - "+lineGroup.getName());
						// 18-Mar-2021 - Padded with space
						triggerSourceDTO.setSourceDisplayName("["+M9kUtils.rightpad(lineGroup.getChassis(),6)+M9kUtils.rightpad(lineGroup.getName(),5)+"] ");
						// END: 10-Mar-2020
					}
					triggerSourceDTO.setSourceInputObjName(lineGroup.getName());
					triggerSourceDTO.setSourceType(M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP);
					// START: 10-Mar-2020 - Display name of line groups to be included with description instad of name
//					triggerSourceDTO.setSourceDisplayName(lineGroup.getChassis()+" - "+lineGroup.getName());
					// END: 10-Mar-2020
					triggerSourceDTO.setSourceLineGroupType(lineGroup.getType());
					triggerSourceDTO.setChassis(lineGroup.getChassis());
					// START: 17-Mar-2021 - Implementing Apparent power. Modifying list to Map
//					triggerSourceDTO.setLstTriggerInputTypes(getTriggerTypes(triggerSourceDTO));
					triggerSourceDTO.setMapTriggerInputTypes(getTriggerTypes(triggerSourceDTO));
					lstTriggerSourceDto.add(triggerSourceDTO);
				  }
			  }
		  }
		  logger.debug("Putting in session lstTriggerSourceDto "+lstTriggerSourceDto);
		 session.put("lstTriggerSourceDto", lstTriggerSourceDto); 
	  }

	  private int getDfrAnalogOffset(String dfrName)
	  {
//		  logger.debug("DFr name to look up "+dfrName);
		  int analogOffset = 0;
		  DfrDTO dfrDTO;
		  for (Iterator<DfrDTO> iterator = lstDfrDTO.iterator(); iterator.hasNext();) {
			  dfrDTO = iterator.next();
			if (dfrDTO.getDfrName().equalsIgnoreCase(dfrName))
			{
				analogOffset = dfrDTO.getAnalogChannelStart()-1;
				break;
			}
		}
		  logger.debug("Analog offset to be returned "+analogOffset);
		  return analogOffset;
	  }
	@SuppressWarnings("unchecked")
	@Override
	public void setSession(Map<String, Object> session) {
		logger.debug("In TriggerChannelsAction setSession method...");
		this.session = session;
//		logger.debug("Current substation in setSession method of triggerChannelsAction "+session.get("selectedSubstation"));
		stationDetails = (StationDTO) session.get("stationDetails");
//		logger.debug("Station Details from the session "+stationDetails);
		  lstDfrDTO = (List<DfrDTO>) session.get("DFRsList");
		 config = (PropertiesConfiguration) session.get("config");

		 accessMode = (String) session.get("accessMode");
		 if (getLstTriggerSourceDto() == null)
		 {
			 lstTriggerSourceDto = (List<TriggerSourceDTO>) session.get("lstTriggerSourceDto");
		 }
		 currentSubstation = (SubStation) session.get("selectedSubstation");
		 mapXMLDfrs = (Map<String, DFR>) session.get("mapXMLDfrs");
		 userDto = (UsersDTO) session.get("userDetails");
		 lstVirtualChannels = (List<AnalogChannelDTO>) session.get("lstVirtualChannels");
		 lstAffectedLineGroups = (List<String>) session.get("linegroupsWarnings");
		 lstAssociatedMeasurements = (List<TriggerChannelDTO>) session.get("LINEGROUP_AFFECTED_MEASUREMENTS");
		 lstLinegroupsModified = (List<String>) session.get("MEASUREMENTS_WARNING_MESSAGE");
		 lstLineGroups = (List<LineGroupsAlgorithm>) session.get("lstLineGroups");
//		 booDigital = (Boolean)session.get("booDigital");
//		 logger.debug("Req Index... "+getReqdIndex());
//			logger.debug("Is Digital? "+booDigital);
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
	 * @return the lstAnalogChannels
	 */
	public List<AnalogChannelDTO> getLstAnalogChannels() {
		return lstAnalogChannels;
	}

	/**
	 * @param lstAnalogChannels the lstAnalogChannels to set
	 */
	public void setLstAnalogChannels(List<AnalogChannelDTO> lstAnalogChannels) {
		this.lstAnalogChannels = lstAnalogChannels;
	}

	/**
	 * @return the magnitudeTriggerStatus
	 */
	public String getMagnitudeTriggerStatus() {
		return magnitudeTriggerStatus;
	}

	/**
	 * @param magnitudeTriggerStatus the magnitudeTriggerStatus to set
	 */
	public void setMagnitudeTriggerStatus(String magnitudeTriggerStatus) {
		this.magnitudeTriggerStatus = magnitudeTriggerStatus;
	}

	/**
	 * @return the channelMagnitudeTriggerIndex
	 */
	public List<String> getChannelMagnitudeTriggerIndex() {
		return channelMagnitudeTriggerIndex;
	}

	/**
	 * @param channelMagnitudeTriggerIndex the channelMagnitudeTriggerIndex to set
	 */
	public void setChannelMagnitudeTriggerIndex(
			List<String> channelMagnitudeTriggerIndex) {
		this.channelMagnitudeTriggerIndex = channelMagnitudeTriggerIndex;
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
	 * @return the lstEventDescriptions
	 */
	public List<String> getLstCircuitNames() {
		return lstCircuitNames;
	}

	/**
	 * @param lstEventDescriptions the lstEventDescriptions to set
	 */
	public void setLstCircuitNames(List<String> lstCircuitNames) {
		this.lstCircuitNames = lstCircuitNames;
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
	 * @return the lstLineGroups
	 */
	public List<LineGroupsAlgorithm> getLstLineGroups() {
		return lstLineGroups;
	}

	/**
	 * @param lstLineGroups the lstLineGroups to set
	 */
	public void setLstLineGroups(List<LineGroupsAlgorithm> lstLineGroups) {
		this.lstLineGroups = lstLineGroups;
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

//	/**
//	 * @return the triggerOutHold
//	 */
//	public String getTriggerOutHold() {
//		return triggerOutHold;
//	}
//
//	/**
//	 * @param triggerOutHold the triggerOutHold to set
//	 */
//	public void setTriggerOutHold(String triggerOutHold) {
//		this.triggerOutHold = triggerOutHold;
//	}

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
	 * @return the lstTriggerSourceDto
	 */
	public List<TriggerSourceDTO> getLstTriggerSourceDto() {
		return lstTriggerSourceDto;
	}

	/**
	 * @param lstTriggerSourceDto the lstTriggerSourceDto to set
	 */
	public void setLstTriggerSourceDto(List<TriggerSourceDTO> lstTriggerSourceDto) {
		this.lstTriggerSourceDto = lstTriggerSourceDto;
	}

//	/**
//	 * @return the lstTriggerInputTypes
//	 */
//	public List<String> getLstTriggerInputTypes() {
//		return lstTriggerInputTypes;
//	}
//
//	/**
//	 * @param lstTriggerInputTypes the lstTriggerInputTypes to set
//	 */
//	public void setLstTriggerInputTypes(List<String> lstTriggerInputTypes) {
//		this.lstTriggerInputTypes = lstTriggerInputTypes;
//	}
	
	public void validate()
	{

//		logger.debug("During validation..."+getLstTriggerChannels());
//		 lstLineGroups = (List<LineGroupsAlgorithm>) session.get("lstLineGroups");
//		 logger.debug("lstlinegroups from session "+lstLineGroups);
		 try{
		if (getLstTriggerChannels() != null && !getLstTriggerChannels().isEmpty())
		{
//			if (getTriggerOutHold() == null || getTriggerOutHold().isEmpty() )
//			{
////				logger.debug("1-trigger out hold in validation "+getTriggerOutHold());
//				addFieldError("triggerOutHold", "TriggerOutHold is Required");
//			}
//			else if (!M9kUtils.isInteger(getTriggerOutHold()))
//			{
////				logger.debug("2-trigger out hold in validation "+getTriggerOutHold());
//				addFieldError("triggerOutHold", "TriggerOutHold should be numeric");
//			}
				
			int iTriggerCnt = 0;
			TriggerChannelDTO triggerChannelDTO;
			for (Iterator<TriggerChannelDTO> triggerIterator = getLstTriggerChannels().iterator(); triggerIterator.hasNext();) {
				triggerChannelDTO = triggerIterator.next();
//				logger.debug("Status of Measurement "+triggerChannelDTO.getStatus()+" type "+triggerChannelDTO.getType());
				if (triggerChannelDTO == null || !triggerChannelDTO.getStatus())
				{
					iTriggerCnt++;
					continue;
				}
				if (triggerChannelDTO.getType() != null && !triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.DELETE))
				{
//					logger.debug("triggerChannelDTO name..."+triggerChannelDTO.getName());
//					logger.debug("Value of status and trigger Status..."+triggerChannelDTO.getStatus()+" trig stat "+triggerChannelDTO.isTriggerStatus());
					// START: Saravanan 12-Sep-2023 - Allow all special characters except ,
					// Saravanan 07-Mar-2013 - Additional Check for special characters
//					if (triggerChannelDTO.getName().indexOf("/") != -1 || triggerChannelDTO.getName().indexOf("(") != -1 || triggerChannelDTO.getName().indexOf(")") != -1
//							|| triggerChannelDTO.getName().indexOf("[") != -1 || triggerChannelDTO.getName().indexOf("]") != -1 || triggerChannelDTO.getName().indexOf(",") != -1)
					if (triggerChannelDTO.getName().indexOf(",") != -1 )
					{
//						addFieldError("lstTriggerChannels["+iTriggerCnt+"].name","Special characters not allowed: / ( ) [ ] ,");
						addFieldError("lstTriggerChannels["+iTriggerCnt+"].name","Contains invalid character ,");
					}
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
//					boolean booRocDurationCheck = false;
//					if (triggerChannelDTO.getTripIfGt() != null ) 
//					{
//						if (!M9kUtils.isDouble(triggerChannelDTO.getTripIfGt()))
//						{
//							addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfGt", "Trigger if > x/s field should be numeric");
//						}
//						else if (Double.parseDouble(triggerChannelDTO.getTripIfGt()) > 0)
//						{
//							if (triggerChannelDTO.getDuration() != null )
//							{
//								if(!M9kUtils.isInteger(triggerChannelDTO.getDuration()))
//								{
//									addFieldError("lstTriggerChannels["+iTriggerCnt+"].duration", "Duration should be numeric");
//									booRocDurationCheck = true;
//								}
//								else if(Integer.parseInt(triggerChannelDTO.getDuration()) <= 0)
//								{
//									addFieldError("lstTriggerChannels["+iTriggerCnt+"].duration", "Duration should be greater than zero if trigger > x/s value is set to greater than zero");
//									booRocDurationCheck = true;
//								}
//								else if(Integer.parseInt(triggerChannelDTO.getDuration()) > 512)
//								{
//									addFieldError("lstTriggerChannels["+iTriggerCnt+"].duration", "Duration cannot be greater than 512");
//									booRocDurationCheck = true;
//								} 
//							}
//							else
//							{
//								addFieldError("lstTriggerChannels["+iTriggerCnt+"].duration", "Duration is required if trigger > x/s specified");
//								booRocDurationCheck = true;
//							}
//						}
//					}
//
//					if (!booRocDurationCheck && triggerChannelDTO.getDuration() != null) 
//					{
//						if (!M9kUtils.isInteger(triggerChannelDTO.getDuration()))
//						{
//							addFieldError("lstTriggerChannels["+iTriggerCnt+"].duration", "Duration should be numeric");
//						}
//						else if(Integer.parseInt(triggerChannelDTO.getDuration()) > 0)
//						{
//							if (Integer.parseInt(triggerChannelDTO.getDuration()) > 512)
//							{
//								addFieldError("lstTriggerChannels["+iTriggerCnt+"].duration", "Duration cannot be greater than 512");
//							}
//							else
//							{
//								if (triggerChannelDTO.getTripIfGt() != null )
//								{
//									if(!M9kUtils.isDouble(triggerChannelDTO.getTripIfGt()))
//									{
//										addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfGt", "Trigger > x/s should be numeric");
//									}
//									else if(Double.parseDouble(triggerChannelDTO.getTripIfGt()) <= 0)
//									{
//										addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfGt", "Trigger > x/s should be greater than zero if duration is set to greater than zero");
//									}
//								}
//								else
//								{
//									addFieldError("lstTriggerChannels["+iTriggerCnt+"].tripIfGt", "Trigger > x/s is required if duration is specified");
//								}
//							}
//						}
//					}

					// END:25-Feb-2020

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
					
//					if (triggerChannelDTO.getExportStatus())
//					{
						// Commented out as export rate is global now 13-July-2012
//						if (triggerChannelDTO.getExportRate() == null)
//						{
//							addFieldError("lstTriggerChannels["+iTriggerCnt+"].exportRate", "Export Rate is missing");
//						}
//						else if (triggerChannelDTO.getExportRate() < 1)
//						{
//							addFieldError("lstTriggerChannels["+iTriggerCnt+"].exportRate", "Export Rate should be greater than zero");
//						}
//					}
					if (triggerChannelDTO.getAverage() != null && triggerChannelDTO.getAverage() < 1)
					{
						addFieldError("lstTriggerChannels["+iTriggerCnt+"].average", "Average value should be greater than zero");
					}
					
				}
				iTriggerCnt++;	
			}
//			session.put("lstTriggerChannels", getLstTriggerChannels());
		}
		if (hasFieldErrors() || hasErrors())
		{
			// To fix issue when validation fails
			  mapGlobalTriggerInputTypes = M9kUtils.convertListToMap(config.getList("allTriggerTypes"));
			  TriggerChannelDTO triggerChannelDTO = null;
			  for (Iterator<TriggerChannelDTO> iterator = getLstTriggerChannels().iterator(); iterator.hasNext();) {
				  triggerChannelDTO = iterator.next();
				  if (triggerChannelDTO != null)
				  {
					  populateTriggerTypes(triggerChannelDTO);
				  }
				  }
		}

		 }
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception during validation ",e);
		}
	}

	private void updatePmuDetails()
	{
		Pmu localPmu;
		Iterator<String> mapXmlDfrIterator = mapXMLDfrs.keySet().iterator();
		String dfrKey;
		Pmus pmus = null;
		for (; mapXmlDfrIterator.hasNext();) {
			dfrKey = mapXmlDfrIterator.next();
			xmlConfigDfr = mapXMLDfrs.get(dfrKey);
//			logger.debug("DFR id "+xmlConfigDfr.getSystem().getDfrId());
			pmus = (Pmus) xmlConfigDfr.getDataPool().getAlgorithms().getPmus();
			if (pmus != null)
			{
				localPmu = pmus.getPmuArray(0);
				// Start 11-Oct-2013 PMU name in PDC changed to reflect the dfr name instead of station name for all chassis
//				localPmu.setName(stationDetails.getSystemStationName());
				localPmu.setName(dfrKey);
				localPmu.setPmuName(dfrKey);
				// End 11-Oct-2013
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
	
	// Get Units for algorithms RMS and Magnitude
	private String getUnitsForAlgorithm(String analogName)
	{
		String units = "";
//		logger.debug("Analog name to get the units "+analogName);

		AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
		
		for (int j2 = 0; j2 < analogInputs.length; j2++) {
//			logger.debug("analogInputs[j2].getName()..."+analogInputs[j2].getName());
			if (analogInputs[j2].getName().equals(analogName))
			{
				//START: 24-Jan-2020 - Implementing Transducer
				if (analogInputs[j2].isSetTransducer() && analogInputs[j2].getTransducer() == 1)
				{
					units = analogInputs[j2].getTransducerUnits();
				}
				//END: 24-Jan-2020
				else if (analogInputs[j2].getInputType().startsWith(M9kConstants.VOLTAGE))
				{
					units = "V";
				}
				else
				{
					units = "A";
				}
//				logger.debug("Analog INput Channel..."+(analogInputs[j2].getChannel()+analogOffset));
				break;
			}
		}
		 

		return units;
	}

	// Get Units for algorithms RMS and Magnitude
	private String getPhaseForAlgorithm(String analogName)
	{
		String phase = "";
		logger.debug("Analog name to get the phase "+analogName);

		AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
		
		for (int j2 = 0; j2 < analogInputs.length; j2++) {
			logger.debug("analogInputs[j2].getName()..."+analogInputs[j2].getName());
			if (analogInputs[j2].getName().equals(analogName))
			{
				phase = analogInputs[j2].getPhase();
				break;
			}
		}
		 

		return phase;
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

//	public Integer getFreqPmuId() {
//		return freqPmuId;
//	}
//
//	public void setFreqPmuId(Integer freqPmuId) {
//		this.freqPmuId = freqPmuId;
//	}

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

	private void prepareMeasuementData()
	{
		  logger.debug("Inside prepareMeasuementData ");

		  if (getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit"))
		  {
			  
			  lstTriggerChannels = initializeNewMeasurements();
				  logger.debug("Transducer_DEBUG: IF Part Size of Trigger channels list "+lstTriggerChannels.size());
		  }
		  else
		  {
			  if (currentSubstation.getMeasurements() != null)
			  {
				  populateLstMeasurementsFromXml();
					// START: 22-Oct-2024 - Manage Trigger/Measurement IDs sequence
					stationDetails.setTotalTriggersConfigured(lstTriggerChannels.get(lstTriggerChannels.size() - 1).getId());
					// END: 22-Oct-2024 - Manage Trigger/Measurement IDs sequence

			  }
//		        List<TriggerChannelDTO> data = this.applyFilter(lstTriggerChannels, this.request.getSearch().getValue());
//		        int to = this.request.getStart().intValue() + this.request.getLength() > data.size() ? data.size()
//		                : this.request.getStart().intValue() + this.request.getLength();
//		        this.applySort(data);
//		        this.response.setData(data.subList(this.request.getStart().intValue(), to));
//		        this.response.setRecordsTotal((long) lstTriggerChannels.size());
//		        this.response.setRecordsFiltered(data.size() != lstTriggerChannels.size() ? (long) data.size() : (long) lstTriggerChannels.size());

			  logger.debug("In the edit mode ");

//			  logger.debug("List of trigger objects "+lstTriggerChannels);

//			  GroupTriggers groupTriggers = currentSubstation.getGroupTriggers();
//			  if (groupTriggers != null)
//			  {
//				  GroupTrigger[] groupTrigger = groupTriggers.getGroupTriggerArray();
//				  for (int i = 0; i < groupTrigger.length; i++) {
//					triggerChannelDto = constructGroupTriggerFromXml(groupTrigger[i]);
//					lstTriggerChannels.add(triggerChannelDto);
//				}
//				  setTriggerOutHold(""+groupTriggers.getTriggerOutHold());
//			  }
		  }

	}

	private void populateLstMeasurementsFromXml()
	{
		setLstTriggerChannels(new ArrayList<TriggerChannelDTO>());
		  Exports exports = null;
		  TriggerChannelDTO triggerChannelDto;
//		  triggerOutHold = "";
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

		  Map<String, Export> mapExportsXml = new HashMap<String, Export>();
		  logger.debug("mapXMLDfrs size "+mapXMLDfrs.size());
		  for (int dfrCnt = 1; dfrCnt <= mapXMLDfrs.size(); dfrCnt++) {
			  xmlConfigDfr = mapXMLDfrs.get("DFR"+dfrCnt);
			  logger.debug("xmlConfigDfr "+xmlConfigDfr.getId());
			  exports = xmlConfigDfr.getDataPool().getExports();
			  if (exports == null)
			  {
				  continue;
			  }
			  Export[] arrExportXml = exports.getExportArray();
			  logger.debug("ExportsXml size "+arrExportXml.length);
			  for (int i = 0; i < arrExportXml.length; i++) {
				  mapExportsXml.put(arrExportXml[i].getId() + "", arrExportXml[i]);
			  }
			  logger.debug("Inside Loop mapExportsXml "+mapExportsXml.size());
		  }
		  logger.debug("Final mapExportsXml "+mapExportsXml.size());
		  Export exportXmlObj = null;  
		  for (int mCount = 0; mCount < arrMeasurement.length; mCount++) {
			  xmlConfigDfr = mapXMLDfrs.get("DFR"+arrMeasurement[mCount].getDfr());
			  // START: 04-Nov-2024 - Move exports looping out of for loop for efficiency
//			  exports = xmlConfigDfr.getDataPool().getExports();
//			  Export[] exportXml = exports.getExportArray();
			  exportXmlObj = mapExportsXml.get(arrMeasurement[mCount].getId() + "");
			  if (exportXmlObj == null)
			  {
				  logger.debug("exportXmlObj is null for measurement id "+arrMeasurement[mCount].getId());
				  continue;
			  }
			  logger.debug("mCount "+mCount+" arrMeasurement[mCount].getId() "+arrMeasurement[mCount].getId());
//			  for (int i = 0; i < exportXml.length; i++) {
				  logger.debug("exportXml[i].getId() "+exportXmlObj.getId());
//				  if (exportXml[i].getId() == arrMeasurement[mCount].getId())
//				  {
				  // END: 04-Nov-2024 - Move exports looping out of for loop for efficiency
					  triggerChannelDto = parseAndBuildMeasurementsFromXml(exportXmlObj); 
					  logger.debug("isReorderMeasurementIds() "+getBooReorderMeasurementIds());
					  if (getBooReorderMeasurementIds())
					  {
						  logger.debug("IsReorderMeasurementIds() "+getBooReorderMeasurementIds()+" mcount "+mCount);
						  triggerChannelDto.setId(mCount + 1);
						  logger.debug("Trigger Channel Id "+triggerChannelDto.getId());
					  }
					  triggerChannelDto.setName(arrMeasurement[mCount].getName());
					  // Expected to set measurement name that corresponds to XML name attribute
					  triggerChannelDto.setMeasurementName(exportXmlObj.getName());
					  
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
						if (arrMeasurement[mCount].getDisturbanceAlarm() != null && arrMeasurement[mCount].getDisturbanceAlarm().equalsIgnoreCase(M9kConstants.ENABLE))
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
						
						logger.debug("Trigger channel to be added to the list "+triggerChannelDto);
						if ((triggerChannelDto.getType() != null && triggerChannelDto.getType().equalsIgnoreCase(M9kConstants.VIRTUAL_MEASUREMENT)) 
								|| (triggerChannelDto.getInputType() != null && triggerChannelDto.getInputType().equalsIgnoreCase(M9kConstants.TRIGGER_INPUT_TYPE_MEASUREMENT)))
						{
							logger.debug("Adding it to Virtual Measurement list "+triggerChannelDto);
							getLstVirtualMeasurements().add(triggerChannelDto);
						}
						else
						{
							populateTriggerTypes(triggerChannelDto);
							getLstTriggerChannels().add(triggerChannelDto);
							logger.debug("MapTriggerInputType set to trigger object "+triggerChannelDto);

						}
//					  break;
//				  }
//			  }

		  }
		  Collections.sort(getLstTriggerChannels(), new Comparator<TriggerChannelDTO>() {

				@Override
				public int compare(TriggerChannelDTO o1, TriggerChannelDTO o2) {
//					return ((o1.getId() < o2.getId())?0:1);
					return ((o1.getId() < o2.getId())? -1 : (o1.getId() == o2.getId())?0:1);
				}
			});
			// START: 08-Mar-2021 - Virtual Measurement implementation for delta transformer
		  logger.debug("Virtual Measurements to be put in session "+getLstVirtualMeasurements());
		  if (getLstVirtualMeasurements() != null && !getLstVirtualMeasurements().isEmpty())
		  {
			  session.put("VIRTUAL_MEASUREMENTS_LIST", getLstVirtualMeasurements());
		  }
		  // START: 17-May-2021 - Add default measurements for newly added dfrs if any
//		  if (currentSubstation.getDFRs().sizeOfDFRArray() > getStationDetails().getTotalDfrsConfigured())
		  {
			  getLstTriggerChannels().addAll(initializeNewMeasurements());
		  }
		  // END: 17-May-2021
		  session.put("lstTriggerChannels", getLstTriggerChannels());
		  logger.debug("measurements list in session "+getLstTriggerChannels().size());
			// END: 08-Mar-2021 - Virtual Measurement implementation for delta transformer

	}
	
	private List<TriggerChannelDTO> initializeNewMeasurements()
	{
		List<TriggerChannelDTO> lstMeasurements = new ArrayList<TriggerChannelDTO>();
		 String sourceDisplayName;
		AnalogChannelDTO analogChannelDto;
		  TriggerChannelDTO triggerChannelDto;
		  int triggerSourceChnlCnt = 0;
		  int dfrId;
		  int measurementId = 1;
		  TriggerSourceDTO triggerSourceDto;
		  if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
		  {
			  measurementId = stationDetails.getTotalTriggersConfigured()+1;
		  }
		  else
		  {
			  measurementId = 1;
		  }
		  if (getLstAnalogChannels() == null || getLstAnalogChannels().isEmpty())
		  {
			  lstAnalogChannels = (List<AnalogChannelDTO>) session.get("lstAnalogChannels");
		  }
		  for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels().iterator(); iterator.hasNext();) {
			  analogChannelDto = iterator.next();
			  // 17-May-2021 - Handle newly added dfrs
			  if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
			  {
				  // Get dfrId from chassis name 
				  dfrId = M9kUtils.getDfrIdfromChassisName(analogChannelDto.getChassis());
				  logger.debug("dfr id "+dfrId);
				  if (!lstDfrDTO.get(dfrId-1).isNewlyAdded())
				  {
					  logger.debug("Skipping old dfrs "+dfrId);
					  triggerSourceChnlCnt++;
					  continue;
				  }
			  }
			  logger.debug("triggerSourceChnlCnt after skipping "+triggerSourceChnlCnt);
			  // END: 17-May-2021			  
			  triggerSourceDto = lstTriggerSourceDto.get(triggerSourceChnlCnt);
			  logger.debug("DELETE-DEBUG: lstTriggerSourceDto size "+lstTriggerSourceDto);
			  logger.debug("DELETE-DEBUG: Trigger Source dto "+triggerSourceDto.getMapTriggerInputTypes());
			  triggerChannelDto = new TriggerChannelDTO();
			  // START: 11-Nov-2025 - To address delete issue when creating new station
//			  if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
//			  {
				  triggerChannelDto.setId(measurementId++);
//			  }
				  // END: 11-Nov-2025
//			  triggerChannelDto.setInputChannelName(analogChannelDto.getName());
//			  triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_ANALOG);
//			  logger.debug("Name of the analog channel to set..."+analogChannelDto.getName());	
//			  logger.debug("Channel to be set..."+lstTriggerSourceDto.get(triggerChnlCnt).getSourceValue());
			  triggerChannelDto.setChannel(triggerSourceDto.getSourceValue());
			  sourceDisplayName = analogChannelDto.getUnalignedDisplayName().substring(analogChannelDto.getUnalignedDisplayName().indexOf("-")+1);
			  triggerChannelDto.setName(sourceDisplayName+"-RMS");
			  triggerChannelDto.setPhase(analogChannelDto.getPhase());
			  triggerChannelDto.setDisableHarmonic(true);
			  triggerChannelDto.setStart(M9kConstants.NEVER);
			  triggerChannelDto.setDisableTripOver(false);
			  triggerChannelDto.setDisableTripUnder(true);
			// START 29-July-2015: ROC implementation  
			  triggerChannelDto.setDisableTripRoc(true);
			  triggerChannelDto.setDisableDuration(true);
			  // END 29-July-2015: ROC implementation
			  triggerChannelDto.setStatus(true);
//			  triggerChannelDto.setChatterLimit(""+stationDetails.getChatterLimit());
//			  triggerChannelDto.setChatterRate(""+stationDetails.getChatterRate());
//			  triggerChannelDto.setTriggerLimit(""+stationDetails.getTriggerLimit());
			  triggerChannelDto.setExportStatus(true);
			  // START: 01-May-2018 All exports to be part of DDR instead of just RMS - ReleaseV1.0.8.2
			  triggerChannelDto.setDdrStatus(true);
			  // END: 01-May-2018
			// START: 30-Dec-2022 Disturbance Alarm implementation
			  triggerChannelDto.setDisturbanceAlarm(false);
			  // END: 30-Dec-2022
//			  logger.debug("Chassis info in analog channel "+analogChannelDto.getChassis());
			  triggerChannelDto.setChassis(analogChannelDto.getChassis());
//			  logger.debug("triggerChannelDto after set..."+triggerChannelDto);
			  triggerChannelDto.setInputChannelName(triggerSourceDto.getSourceInputObjName());
			  triggerChannelDto.setInputType(triggerSourceDto.getSourceType());
			  triggerChannelDto.setMapTriggerInputTypes(triggerSourceDto.getMapTriggerInputTypes());
//			  logger.debug("\t\t\tLst of trigger input types "+triggerChannelDto.getLstTriggerInputTypes());
			  // START: 24-Jan-2020 - Transducer Implementation
			  if (analogChannelDto.isTransducer())
			  {
				  triggerChannelDto.setUnits(analogChannelDto.getTransducerUnits());
				  triggerChannelDto.setTransducer(true);
			  }
			  //END: 24-Jan-2020
			  else if (analogChannelDto.getInputType().startsWith(M9kConstants.VOLTAGE))
			  {
				  triggerChannelDto.setUnits("V");
			  }
			  else
			  {
				  triggerChannelDto.setUnits("A");
			  }
			  lstMeasurements.add(triggerChannelDto);
			  triggerSourceChnlCnt++;
		  }
		  if (getLstLineGroups() != null)
		  {
			  for (Iterator<LineGroupsAlgorithm> iterator = getLstLineGroups().iterator(); iterator
					.hasNext();) {
				LineGroupsAlgorithm lineGroup = iterator.next();
				// skip the global line group
				if (!lineGroup.isLocal())
				{
					continue;
				}
				// START: 17-May-2021 - Handle newly added dfrs
				  if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
				  {
					// Get dfrId from chassis name 
					  dfrId = M9kUtils.getDfrIdfromChassisName(lineGroup.getChassis());
					  logger.debug("Line group dfr id "+dfrId);
					  if (!lstDfrDTO.get(dfrId-1).isNewlyAdded())
					  {
						  logger.debug("Skipping old dfrs "+dfrId);
						  continue;
					  }
				  }
				  // END: 17-May-2021
				if (lineGroup.isLocal())
				{
					triggerSourceDto = lstTriggerSourceDto.get(triggerSourceChnlCnt);
					triggerChannelDto = new TriggerChannelDTO();
				  triggerChannelDto.setInputChannelName(lineGroup.getName());
				  triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_LINEGROUP);
				  triggerChannelDto.setGlobalLineGroupId(lineGroup.getId());
//				  logger.debug("Name of the LineGroup channel to set..."+lineGroup.getName());	
//				  logger.debug("Line Group Source display Name: "+lstTriggerSourceDto.get(triggerChnlCnt).getSourceValue());
				  triggerChannelDto.setChannel(triggerSourceDto.getSourceValue());
				  if (lineGroup.getLineGroupName() != null && !lineGroup.getLineGroupName().isEmpty())
				  {
					  triggerChannelDto.setName(lineGroup.getLineGroupName());
				  }
				  else
				  {
					  triggerChannelDto.setName(lineGroup.getName());
				  }
				  triggerChannelDto.setChassis(lineGroup.getChassis());
				  triggerChannelDto.setPhase(M9kConstants.NO_PHASE);
				  triggerChannelDto.setDisableHarmonic(true);
				  triggerChannelDto.setStart(M9kConstants.NEVER);
				  triggerChannelDto.setDisableTripOver(false);
				  triggerChannelDto.setDisableTripUnder(true);
				// START 29-July-2015: ROC implementation  
				  triggerChannelDto.setDisableTripRoc(true);
				  triggerChannelDto.setDisableDuration(true);
				  // END 29-July-2015: ROC implementation
				  triggerChannelDto.setStatus(false);
//				  triggerChannelDto.setChatterLimit(""+stationDetails.getChatterLimit());
//				  triggerChannelDto.setChatterRate(""+stationDetails.getChatterRate());
//				  triggerChannelDto.setTriggerLimit(""+stationDetails.getTriggerLimit());
				  triggerChannelDto.setExportStatus(false);
				  triggerChannelDto.setInputChannelName(triggerSourceDto.getSourceInputObjName());
				  triggerChannelDto.setInputType(triggerSourceDto.getSourceType());
				  triggerChannelDto.setMapTriggerInputTypes(triggerSourceDto.getMapTriggerInputTypes());
				  logger.debug("\t\t\tLst of trigger input types "+triggerChannelDto.getMapTriggerInputTypes());
//				  logger.debug("triggerChannelDto after set..."+triggerChannelDto);
				  lstMeasurements.add(triggerChannelDto);
				  triggerSourceChnlCnt++;
				}
			}
		  }
//		  setTriggerOutHold(M9kConstants.DEFAULT_TRIGGER_OUT_HOLD);
		  logger.debug("DELETE-DEBUG: lst of new measurments added "+lstMeasurements.size()+" actual list "+lstMeasurements);
		  return lstMeasurements;

	}
	
	/** START: 29-Jan-2020 - Transducer implementation - Update exports if transducer unit is changed
	 * Scenario: User modifies transducer units and then goes directly to events or faults Loc page to finish and send
	 * @throws Exception 
	 * **/
	private void updateTranducerPropertiesinExports() throws Exception
	{
		logger.debug("In updateTranducerPropertiesinExports to update transducer properties ");
		DFR xmlDfr = null;
		AnalogChannelDTO analogChannelDTO;
		Exports exports;
		List<TriggerChannelDTO> lstRmsTriggersForAvg = new ArrayList<TriggerChannelDTO>();
		for (int i = 0; i < lstAnalogChannels.size(); i++) {
			analogChannelDTO = lstAnalogChannels.get(i);
			logger.debug("Is the channel A"+analogChannelDTO.getChannel()+" set to transducer? "+analogChannelDTO.isTransducer());
			xmlDfr = mapXMLDfrs.get(analogChannelDTO.getChassis());
			exports = xmlDfr.getDataPool().getExports();
			lstRmsTriggersForAvg.addAll(getUpdateLstRMSTriggerDTO(exports, analogChannelDTO)); // Compare just the analog name as prefix channel id can be different
			logger.debug("Global boolean list of Trigger channels "+lstRmsTriggersForAvg.size());
		}
		logger.debug("Final list of updated rms triggers "+lstRmsTriggersForAvg);
		// WHen changing a normal channel to Transducer while editing and then click directly on the last page either events or faultLocation it doesn't create required algorithms like Average,Limits etc..
		logger.debug("New Transducer channel ");
		 populateInputTriggerChannels();
		 populateLstMeasurementsFromXml();
		if (lstRmsTriggersForAvg.size() > 0)
		{
			 lstTriggerChannels.addAll(lstRmsTriggersForAvg);
			 Collections.sort(lstTriggerChannels, new Comparator<TriggerChannelDTO>() {

					@Override
					public int compare(TriggerChannelDTO o1, TriggerChannelDTO o2) {
//						return ((o1.getId() < o2.getId())?0:1);
						return ((o1.getId() < o2.getId())? -1 : (o1.getId() == o2.getId())?0:1);
					}
				});
		}
		 populateTriggerChannelsIntoXml();

	}

	/** START: 29-Jan-2020 - Transducer implementation - Update all algorithm elements if transducer unit is changed
	 * Scenario: User modifies transducer units and then goes directly to events or faults Loc page to finish and send
	 * @throws Exception 
	 * **/
	private List<TriggerChannelDTO> getUpdateLstRMSTriggerDTO(Exports exports, AnalogChannelDTO analogChannelDTO) throws Exception
	{
		List<TriggerChannelDTO> lstRmsTriggersForAvg = new ArrayList<TriggerChannelDTO>();
		TriggerChannelDTO replacedTriggerDTO;
//		List<Integer> lstExportsToDelete = new ArrayList<Integer>();
		
//		logger.debug("Export Name to be compared "+analogChannelDTO+" xml content "+exports.xmlText());
		String algorithmType;
		Export[] arrExport = exports.getExportArray();
		for (int i = arrExport.length-1; i >=0;i--) {
			if (arrExport[i].getName().substring(arrExport[i].getName().lastIndexOf("-")+1).equalsIgnoreCase(analogChannelDTO.getName())) // Compare without RMS- prefix in the name
			{
				logger.debug("Is the selected analogDTO a transducer channel? "+analogChannelDTO.isTransducer());
				logger.debug("Export Input "+arrExport[i].getInput());
				algorithmType = arrExport[i].getInput().substring(M9kXMLConstants.XML_ALGORITHMS_INPUT.length(), arrExport[i].getInput().indexOf("(",M9kXMLConstants.XML_ALGORITHMS_INPUT.length()));
				if (algorithmType.equalsIgnoreCase(M9kConstants.AVERAGE))
				{
					if (analogChannelDTO.isTransducer())
					{
						arrExport[i].setMeasurementType(analogChannelDTO.getTransducerUnits());
						arrExport[i].setUnits(analogChannelDTO.getTransducerUnits());
					}
					else
					{
						replacedTriggerDTO = getRMSMeasurementForTransducerAverageMeasurement(arrExport[i], analogChannelDTO);
						if (replacedTriggerDTO != null)
						{
							lstRmsTriggersForAvg.add(replacedTriggerDTO);
						}
						TriggerChannelDTO deleteTriggerChannelDTO = new TriggerChannelDTO();
						deleteTriggerChannelDTO.setChassis(analogChannelDTO.getChassis());
						deleteTriggerChannelDTO.setMeasurementName(arrExport[i].getName());
//						lstExportsToDelete.add(i);
//						deleteAverageFromXml(deleteTriggerChannelDTO);
						deleteMeasurementUsingExport(mapXMLDfrs.get(analogChannelDTO.getChassis()),arrExport[i]);
					}
				}
			}
		}
//		logger.debug("Deleting the exports "+lstExportsToDelete.size());
//		for (int i = 0; i < lstExportsToDelete.size(); i++) {
//			exports.removeExport(lstExportsToDelete.get(i));
//		}
		logger.debug("Did the config updated here ? Returning lst of triggers size "+lstRmsTriggersForAvg.size());
		return lstRmsTriggersForAvg;

	}

	/** START: 05-Feb-2020 - Transducer implementation - Update exports if transducer unit is changed
	 * Avoid recreating default RMS if RMS algorithm is already configured
	 * 
	 * **/
	private Rms getRmsIfAlreadyExists(AnalogChannelDTO analogChannelDTO)
	{
		DFR xmlDfr = mapXMLDfrs.get(analogChannelDTO.getChassis());
		Algorithms algorithms = xmlDfr.getDataPool().getAlgorithms();
		Rms rms = null;
		String rmsNameToSearch = analogChannelDTO.getChannel()+"-"+analogChannelDTO.getName();
		for (int i = 0; i < algorithms.getRmsArray().length; i++) {
			logger.debug("RMS Name to search "+rmsNameToSearch+" rms name to compare "+algorithms.getRmsArray(i).getName());
			if (rmsNameToSearch.equals(algorithms.getRmsArray(i).getName()))
			{
				rms = algorithms.getRmsArray(i);
				break;
			}
		}
		return rms;
	}

	/**
	 * 
	 */
	 private TriggerChannelDTO getRMSMeasurementForTransducerAverageMeasurement(Export export, AnalogChannelDTO analogChannelDTO)
	  {
		  TriggerChannelDTO triggerChannelDto = new TriggerChannelDTO();
		  try
		  {
		  triggerChannelDto.setId(export.getId());
		  triggerChannelDto.setType(M9kConstants.RMS);
		  triggerChannelDto.setMeasurementName(export.getExportName());
		  String sourceDisplayName = analogChannelDTO.getUnalignedDisplayName().substring(analogChannelDTO.getUnalignedDisplayName().indexOf("-")+1);
		  triggerChannelDto.setName(sourceDisplayName+"-RMS");
		  triggerChannelDto.setPhase(M9kConstants.NO_PHASE);
		  triggerChannelDto.setChannel(analogChannelDTO.getChannel());
		  triggerChannelDto.setChassis(analogChannelDTO.getChassis());
		  triggerChannelDto.setDisableHarmonic(true);
		  triggerChannelDto.setGlobalLineGroupId(0);
		  triggerChannelDto.setStart(M9kConstants.NEVER);
		  triggerChannelDto.setDisableTripOver(true);
		  triggerChannelDto.setDisableTripUnder(true);

		  triggerChannelDto.setDisableTripRoc(true);
		  triggerChannelDto.setDisableDuration(true);
		  triggerChannelDto.setStatus(true);
		  triggerChannelDto.setInputType(M9kConstants.TRIGGER_INPUT_TYPE_ANALOG);
		  triggerChannelDto.setInputChannelName(analogChannelDTO.getName());
		  triggerChannelDto.setExportStatus(true);
		  triggerChannelDto.setPmuStatus(false);
		  triggerChannelDto.setDdrStatus(true);
		// START: 30-Dec-2022 Disturbance Alarm implementation
		  triggerChannelDto.setDisturbanceAlarm(false);
		  // END: 30-Dec-2022
		  triggerChannelDto.setTriggerStatus(false);
		  triggerChannelDto.setMapTriggerInputTypes(M9kUtils.convertListToMap(config.getList("analogInputTriggerType")));

		  }
		  catch (Exception e) {
			logger.error("Error occurred replacing transducer average algorithm ", e);
			triggerChannelDto = null;
		}
		  return triggerChannelDto;
	  }

		/**
		 * START: 09-Mar-2020 - Implementing virtual measurements for delta transformers
		 * @param virtualMeasurementName
		 * @param triggerChannelDto
		 */
		private void constructVirtualMeasurementFromXml(String virtualMeasurementName, TriggerChannelDTO triggerChannelDto)
		{
			logger.debug("EnteredconstructVirtualMeasurementFromXml virtualMeasurementName "+virtualMeasurementName+" triggerChannelDto "+triggerChannelDto);
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
					virtualMeasurementAlgorithm = new VirtualMeasurementAlgorithm();
					if (arrVirtualMeasurementXml[j].getScale() != 0)
					{
						virtualMeasurementAlgorithm.setGlobalScale(arrVirtualMeasurementXml[j].getScale());
					}
					arrVirtualMeasurementInputXml = arrVirtualMeasurementXml[j].getVirtualMeasurementInputArray();
					logger.debug("virtual measurement input array "+arrVirtualMeasurementInputXml.length);
					for (int i = 0; i < arrVirtualMeasurementInputXml.length; i++) {
						logger.debug("arrVirtualMeasurementInputXml[i].getInput() "+arrVirtualMeasurementInputXml[i].getInput());
						inputForVirtualMeasurement = getInputMeasurement(arrVirtualMeasurementInputXml[i].getInput());
						logger.debug("Input measurement object returned "+inputForVirtualMeasurement);
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

		}
		private TriggerChannelDTO getInputMeasurement(String input) {
			TriggerChannelDTO matchedTriggerObject = null;
			// Parsing strings similar to @Algorithms(Algorithms)/Power(29-LG_1)/_Object
			String triggerChannelName = input.substring(input.indexOf("/")+1,input.lastIndexOf("/"));
			String algorithmType = triggerChannelName.substring(0, triggerChannelName.indexOf("(")); 
			triggerChannelName = triggerChannelName.substring(triggerChannelName.indexOf("(")+1, triggerChannelName.indexOf(")"));
			logger.debug("Inside getInputMeasurement algorithmType "+algorithmType+" triggerChannelName "+triggerChannelName);
			for (Iterator<TriggerChannelDTO> iterator = getLstTriggerChannels().iterator(); iterator.hasNext();) {
				matchedTriggerObject = iterator.next();
				logger.debug("matchedTriggerObject.getName "+matchedTriggerObject.getName()+"get measurement name "+matchedTriggerObject.getMeasurementName()+" triggerChannelName "+triggerChannelName);
				if (matchedTriggerObject.getMeasurementName().equals(triggerChannelName))
				{
					logger.debug("Returning matched trigger object "+matchedTriggerObject);
					return matchedTriggerObject;
				}
			}
			return null;
		}
		
		public String deleteMeasurementRequest()
		{
			logger.debug("Inside deleteMeasurementAction triggerChannelDto "+getMeasurementToDelete());
			if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
			{
				deleteMeasurement(getMeasurementToDelete());
				lstDeletedMeasurements = (List<TriggerChannelDTO>) session.get("lstDeletedMeasurements");
				if (lstDeletedMeasurements == null)
				{
					lstDeletedMeasurements = new ArrayList<TriggerChannelDTO>();
				}
				lstDeletedMeasurements.add(getMeasurementToDelete());
				session.put("lstDeletedMeasurements", lstDeletedMeasurements);
			}

			logger.debug("AFTER: Inside deleteMeasurementAction lstTriggerChannels "+getLstTriggerChannels());
			
			return SUCCESS;
		}
		
		public String deleteSelectedMeasurementsRequest()
		{
			TriggerChannelDTO deletedMeasurement = null;
			logger.debug("Inside deleteSelectedMeasurementsRequest triggerChannelDto "+getSelectedMeasurementsToDelete());
			if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
			{
				lstDeletedMeasurements = (List<TriggerChannelDTO>) session.get("lstDeletedMeasurements");
				for (Iterator<TriggerChannelDTO> iterator = getSelectedMeasurementsToDelete().iterator(); iterator.hasNext();) {
					deletedMeasurement = iterator.next();
					deleteMeasurement(deletedMeasurement);
					if (lstDeletedMeasurements == null)
					{
						lstDeletedMeasurements = new ArrayList<TriggerChannelDTO>();
					}
					logger.debug("inside for loop Added deletedMeasurement "+deletedMeasurement);
					lstDeletedMeasurements.add(deletedMeasurement);
				}
				session.put("lstDeletedMeasurements", lstDeletedMeasurements);
			}
			logger.debug("AFTER: Inside deleteMeasurementAction lstDeletedMeasurements "+lstDeletedMeasurements);

			return SUCCESS;
		}
		
		/**
		 * 
		 * @return
		 */
		@SuppressWarnings("unchecked")
		private void saveMeasurementsIntoXml(TriggerChannelDTO triggerChannelDTO)
		{
			if (mapXMLDfrs == null)
			{
				mapXMLDfrs = (Map<String, DFR>) session.get("mapXMLDfrs");
			}
			xmlConfigDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			Algorithms algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
			Triggers triggers = algorithms.getTriggers();
			DataPool datapool;
			Exports exports = null;
			Export export = null;
			PmuInputs pmuInputs = null;
			PmuInput pmuInput = null;
			Measurements measurements = null;
			Measurement measurement = null;
			String exportName;
			Limits limits = null;
			String limitsName;
			Pmu localPmu = null;
			PmuInput localPmuInput;
			
			if (xmlConfigDfr.getDataPool() == null)
			{
				datapool = xmlConfigDfr.addNewDataPool();
				datapool.setName("Datapool");
				logger.debug("Creating Algorithm");
				algorithms = datapool.addNewAlgorithms();
				algorithms.setName("Algorithms");
				triggers = algorithms.addNewTriggers();
				triggers.setName("Global Trigger");
			}
			else
			{
				algorithms = xmlConfigDfr.getDataPool().getAlgorithms();
				if (algorithms == null)
				{
						logger.debug("Creating Algorithm");
						algorithms = xmlConfigDfr.getDataPool().addNewAlgorithms();
						algorithms.setName("Algorithms");
						triggers = algorithms.addNewTriggers();
						triggers.setName("Global Trigger");
				}
				else
				{
					triggers = algorithms.getTriggers();
					if (triggers == null)
					{
						triggers = algorithms.addNewTriggers();
						triggers.setName("Global Trigger");							
					}
				}
			}
			AbstractTrigger genericTrigger = populateTrigger(triggerChannelDTO);
			if (genericTrigger == null)
			{
				logger.error("This particular measurement is skipped as it is not supported yet"+triggerChannelDTO);
				return;
			}
//			if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit"))
//			{
//				triggerChannelDTO.setChassis("DFR"+xmlConfigDfr.getSystem().getDfrId());
//				deleteMeasurement(triggerChannelDTO);
//			}
			
			//START: 24-Jan-2020 - Implementing Transducer 
			// 23-Jul-2020 - Bug Fix: Units value messes the algorithm
			if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.AVERAGE) || triggerChannelDTO.isTransducer())
			{
				logger.debug("Is transducer set ? "+triggerChannelDTO.isTransducer());
				triggerChannelDTO.setUnits(getUnitsForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
				Average average = getAverageIfAlreadyExists(genericTrigger);
				if (average == null)
				{
					average = algorithms.addNewAverage();
				}
				average.setName(genericTrigger.getName());
				average.setInput(genericTrigger.getInputValue());
				if (triggerChannelDTO.getAverage() != null)
				{
					average.setAverage(triggerChannelDTO.getAverage());
				}			
				// START: 18-Mar-2021 - Set description for all algorithm
				average.setDescription(triggerChannelDTO.getName());
				// END: 18-Mar-2021 - Set description for all algorithm

			}
			//END 23-Jul-2020

			else if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.RMS))
			{
//				triggerChannelDTO.setUnits("V");
				triggerChannelDTO.setUnits(getUnitsForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
				Rms rms = getRmsIfAlreadyExists(genericTrigger);
				if (rms == null)
				{
					rms = algorithms.addNewRms();
				}
				rms.setName(genericTrigger.getName());
				rms.setInput(genericTrigger.getInputValue());
				if (triggerChannelDTO.getAverage() != null)
				{
					rms.setAverage(triggerChannelDTO.getAverage());
				}
				// START: 18-Mar-2021 - Set description for all algorithm
				rms.setDescription(triggerChannelDTO.getName());
				// END: 18-Mar-2021 - Set description for all algorithm
			}
			else if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.HARMONIC))
			{
//				triggerChannelDTO.setUnits("V");
				triggerChannelDTO.setUnits(getUnitsForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
				triggerChannelDTO.setPhase(getPhaseForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
				if (triggerChannelDTO.getStatus())
				{
					triggerChannelDTO.setDisableHarmonic(false);
				}
				else
				{
					triggerChannelDTO.setDisableHarmonic(true);
				}
				Magnitude magnitude = getMagnitudeIfAlreadyExists(genericTrigger);
				if (magnitude == null)
				{
					magnitude = algorithms.addNewMagnitude();
				}
				magnitude.setName(genericTrigger.getName());
				magnitude.setInput(triggerChannelDTO.getTriggerChannelInput());
				if (((MagnitudeTrigger)genericTrigger).getHarmonic() != null)
				{
					magnitude.setHarmonic(Integer.parseInt(((MagnitudeTrigger)genericTrigger).getHarmonic()));
				}
				else
				{
					magnitude.setHarmonic(1);
				}
				if (triggerChannelDTO.getAverage() != null)
				{
					magnitude.setAverage(triggerChannelDTO.getAverage());
				}
				// START: 18-Mar-2021 - Set description for all algorithm
				magnitude.setDescription(triggerChannelDTO.getName());
				// END: 18-Mar-2021 - Set description for all algorithm
			}
			else if (triggerChannelDTO.getType().equals(M9kConstants.ZERO_SEQUENCE_V) ||
					triggerChannelDTO.getType().equals(M9kConstants.ZERO_SEQUENCE_I))
			{
				if (triggerChannelDTO.getType().equals(M9kConstants.ZERO_SEQUENCE_V))
				{
					triggerChannelDTO.setUnits("V");
				}
				else
				{
					triggerChannelDTO.setUnits("A");
				}
				ZeroSequence zeroSequence = getZeroSequenceIfAlreadyExists(genericTrigger);
				if (zeroSequence == null)
				{
					zeroSequence = algorithms.addNewZeroSequence();
				}
				zeroSequence.setName(genericTrigger.getName());
				zeroSequence.setInput(genericTrigger.getInputValue());
				zeroSequence.setInputType(((SequenceTrigger)genericTrigger).getInputType());
				if (triggerChannelDTO.getAverage() != null)
				{
					zeroSequence.setAverage(triggerChannelDTO.getAverage());
				}
				// START: 18-Mar-2021 - Set description for all algorithm
				zeroSequence.setDescription(triggerChannelDTO.getName());
				// END: 18-Mar-2021 - Set description for all algorithm
			}
			else if (triggerChannelDTO.getType().startsWith(M9kConstants.POSITIVE_SEQUENCE_V) || 
					triggerChannelDTO.getType().startsWith(M9kConstants.POSITIVE_SEQUENCE_I))
			{
				if (triggerChannelDTO.getType().equals(M9kConstants.POSITIVE_SEQUENCE_V))
				{
					triggerChannelDTO.setUnits("V");
				}
				else
				{
					triggerChannelDTO.setUnits("A");
				}
				PositiveSequence positiveSequence = getPositiveSequenceIfAlreadyExists(genericTrigger);
				if (positiveSequence == null)
				{
					positiveSequence = algorithms.addNewPositiveSequence();
				}
				positiveSequence.setName(genericTrigger.getName());
				positiveSequence.setInput(genericTrigger.getInputValue());
				positiveSequence.setInputType(((SequenceTrigger)genericTrigger).getInputType());
				if (triggerChannelDTO.getAverage() != null)
				{
					positiveSequence.setAverage(triggerChannelDTO.getAverage());
				}
				// START: 18-Mar-2021 - Set description for all algorithm
				positiveSequence.setDescription(triggerChannelDTO.getName());
				// END: 18-Mar-2021 - Set description for all algorithm
			}
			else if (triggerChannelDTO.getType().startsWith(M9kConstants.NEGATIVE_SEQUENCE_V) || 
					triggerChannelDTO.getType().startsWith(M9kConstants.NEGATIVE_SEQUENCE_I))
			{
				if (triggerChannelDTO.getType().equals(M9kConstants.NEGATIVE_SEQUENCE_V))
				{
					triggerChannelDTO.setUnits("V");
				}
				else
				{
					triggerChannelDTO.setUnits("A");
				}
				NegativeSequence negativeSequence = getNegativeSequenceIfAlreadyExists(genericTrigger);
				if (negativeSequence == null)
				{
					negativeSequence = algorithms.addNewNegativeSequence();
				}
				negativeSequence.setName(genericTrigger.getName());
				negativeSequence.setInput(genericTrigger.getInputValue());
				negativeSequence.setInputType(((SequenceTrigger)genericTrigger).getInputType());
				if (triggerChannelDTO.getAverage() != null)
				{
					negativeSequence.setAverage(triggerChannelDTO.getAverage());
				}
				// START: 18-Mar-2021 - Set description for all algorithm
				negativeSequence.setDescription(triggerChannelDTO.getName());
				// END: 18-Mar-2021 - Set description for all algorithm
			}
			else if (triggerChannelDTO.getType().indexOf(M9kConstants.WATTS) != -1 ||
					(triggerChannelDTO.getType().indexOf(M9kConstants.VARS) != -1)
					// START: 17-Mar-2021 - Apparent power implementation
					|| (triggerChannelDTO.getType().toLowerCase().indexOf(M9kConstants.VA_APPARENT.toLowerCase()) != -1)
					// END: 17-Mar-2021 - Apparent power implementation
					)
			{
				// START: 17-Mar-2021 - Apparent power implementation
				if (triggerChannelDTO.getType().indexOf(M9kConstants.WATTS) != -1)
				{
					triggerChannelDTO.setUnits(M9kConstants.WATTS_UNITS);
				}
				else if (triggerChannelDTO.getType().indexOf(M9kConstants.VARS) != -1)
				{
					triggerChannelDTO.setUnits(M9kConstants.VARS_UNITS);
				}
				else 
				{
					triggerChannelDTO.setUnits(M9kConstants.VA_UNITS);
				}
				Power power = getPowerIfAlreadyExists(genericTrigger);
				if (power == null)
				{
					power = algorithms.addNewPower();
				}
				if (triggerChannelDTO.getType().equals(M9kConstants.A_PHASE_WATTS) ||
						(triggerChannelDTO.getType().equals(M9kConstants.A_PHASE_VARS))
						|| (triggerChannelDTO.getType().equals(M9kConstants.A_PHASE_APPARENT)))
				{
					power.setInputs("A");
				}
				else if (triggerChannelDTO.getType().equals(M9kConstants.B_PHASE_WATTS) ||
						(triggerChannelDTO.getType().equals(M9kConstants.B_PHASE_VARS))
						|| (triggerChannelDTO.getType().equals(M9kConstants.B_PHASE_APPARENT)))
				{
					power.setInputs("B");
				}
				else if (triggerChannelDTO.getType().equals(M9kConstants.C_PHASE_WATTS) ||
						(triggerChannelDTO.getType().equals(M9kConstants.C_PHASE_VARS))
						|| (triggerChannelDTO.getType().equals(M9kConstants.C_PHASE_APPARENT)))
				{
					power.setInputs("C");
				}
				else 
				{
					power.setInputs("ALL");
				}
				// END: 17-Mar-2021 - Apparent power implementation
				power.setName(genericTrigger.getName());
				power.setPowerType(((PowerTrigger)genericTrigger).getPowerType());
				power.setInput(genericTrigger.getInputValue());
				// START: 18-Mar-2021 - Set description for all algorithm
				power.setDescription(triggerChannelDTO.getName());
				// END: 18-Mar-2021 - Set description for all algorithm
			}
			else if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.FREQUENCY))
			{
				triggerChannelDTO.setUnits("Hz");
				triggerChannelDTO.setPhase(getPhaseForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
//				if (triggerChannelDTO.getStatus())
//				{
//					triggerChannelDTO.setDisableHarmonic(false);
//				}
//				else
//				{
//					triggerChannelDTO.setDisableHarmonic(true);
//				}
//				Phasor phasor = getPhasorIfAlreadyExists(genericTrigger);
//				if (phasor == null)
//				{
//					phasor = algorithms.addNewPhasor();
//					phasor.setName(((FrequencyTrigger)genericTrigger).getPhaseName());
//					phasor.setInput(triggerChannelDTO.getTriggerChannelInput());
				Frequency frequency = getFrequencyIfAlreadyExists(genericTrigger);
				if (frequency == null)
				{
					frequency = algorithms.addNewFrequency();
				}
				frequency.setName(genericTrigger.getName());
				frequency.setInput(triggerChannelDTO.getTriggerChannelInput());
				if (triggerChannelDTO.getAverage() != null)
				{
					frequency.setAverage(triggerChannelDTO.getAverage());
				}
				else
				{
					logger.debug("TESTAverage: Average is null "+triggerChannelDTO);
				}
				// START: 18-Mar-2021 - Set description for all algorithm
				frequency.setDescription(triggerChannelDTO.getName());
				// END: 18-Mar-2021 - Set description for all algorithm
//					frequency.setInput(((FrequencyTrigger)genericTrigger).getFrequencyInputXMLString(((FrequencyTrigger)genericTrigger).getPhaseName()));
//				}
			}
			else if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.PHASOR))
			{
				triggerChannelDTO.setUnits(M9kConstants.PHASOR);
				triggerChannelDTO.setPhase(getPhaseForAlgorithm(genericTrigger.getName().substring(genericTrigger.getName().lastIndexOf("-")+1)));
				Phase phase = getPhaseIfAlreadyExists(genericTrigger);
				if (phase == null)
				{
					phase = algorithms.addNewPhase();
				}
				phase.setName(genericTrigger.getName());
				phase.setInput(triggerChannelDTO.getTriggerChannelInput());
				if (triggerChannelDTO.getAverage() != null)
				{
					phase.setAverage(triggerChannelDTO.getAverage());
				}
				// START: 18-Mar-2021 - Set description for all algorithm
				phase.setDescription(triggerChannelDTO.getName());
				// END: 18-Mar-2021 - Set description for all algorithm
			}

			limits = null;
			limitsName = genericTrigger.getLimitsAlgorithm().getName();
			for (int j = 0; j < algorithms.sizeOfLimitsArray(); j++) {
				if (algorithms.getLimitsArray(j).getName().equalsIgnoreCase(limitsName)){
//					limits = algorithms.getLimitsArray(j);
					algorithms.removeLimits(j);
					break;
				}
			}
			if (!triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.PHASOR))
			{
				if (limits == null)
				{
					limits = algorithms.addNewLimits();
					limits.setName(limitsName);
				}
				limits.setInput(genericTrigger.getLimitsAlgorithm().getInputValue());
				if (genericTrigger.getLimitsAlgorithm().getHighLimit() != null && !genericTrigger.getLimitsAlgorithm().getHighLimit().isEmpty())
				{
					limits.setHighLimit(genericTrigger.getLimitsAlgorithm().getHighLimit());
				}
				if (genericTrigger.getLimitsAlgorithm().getLowLimit() != null && !genericTrigger.getLimitsAlgorithm().getLowLimit().isEmpty())
				{	
					limits.setLowLimit(genericTrigger.getLimitsAlgorithm().getLowLimit());
				}
				
				// START: 26-Feb-2020 - ROC positive and Negative Limits implementation
				if (genericTrigger.getLimitsAlgorithm().getDuration() != null 
						&& !genericTrigger.getLimitsAlgorithm().getDuration().isEmpty() 
						&& Integer.parseInt(genericTrigger.getLimitsAlgorithm().getDuration()) > 0)
				{
					limits.setDuration(genericTrigger.getLimitsAlgorithm().getDuration());
					limits.setRateOfChangeLimitNeg(genericTrigger.getLimitsAlgorithm().getRateOfChangeLimitNeg());
					limits.setRateOfChangeLimitPos(genericTrigger.getLimitsAlgorithm().getRateOfChangeLimitPos());
				}
				
				// START: 18-Mar-2021 - Set description for all algorithm
				limits.setDescription(triggerChannelDTO.getName());
				// END: 18-Mar-2021 - Set description for all algorithm
				// 28-July-2015 START: Implementation of rate of change trigger
//				if (genericTrigger.getLimitsAlgorithm().getRateOfChangeLimit() != null && !genericTrigger.getLimitsAlgorithm().getRateOfChangeLimit().isEmpty() )
//				{
//					limits.setRateOfChangeLimit(genericTrigger.getLimitsAlgorithm().getRateOfChangeLimit());
//				}
//				if (genericTrigger.getLimitsAlgorithm().getDuration() != null && !genericTrigger.getLimitsAlgorithm().getDuration().isEmpty() )
//				{
//					limits.setDuration(genericTrigger.getLimitsAlgorithm().getDuration());
//				}
				// 28-July-2015 END: Implementation of rate of change trigger
				// END: 26-Feb-2020
				
//				logger.debug("trigger to be set..."+genericTrigger.getTrigger()+" Input..."+genericTrigger.getTrigger().getInput());
				int j = 0;
				for (; j < triggers.sizeOfTriggerArray(); j++) {
					if(triggers.getTriggerArray(j).getName().equalsIgnoreCase(genericTrigger.getTrigger().getName()))
					{
						break;
					}
				}
				if (j == triggers.sizeOfTriggerArray())
				{
					triggers.addNewTrigger();
				}
				triggers.setTriggerArray(j, genericTrigger.getTrigger());
			}
		
			// Construct exports
//			logger.debug("Export status "+triggerChannelDTO.getExportStatus());
			exports = xmlConfigDfr.getDataPool().getExports();
			if (exports == null)
			{
				exports = xmlConfigDfr.getDataPool().addNewExports();
				exports.setName("Exports");	
			}
			export = null;
//			exportName = triggerChannelDTO.getName()+"-"+triggerChannelDTO.getType()+"-"+triggerChannelDTO.getInputChannelName();
			exportName = triggerChannelDTO.getMeasurementName();
			export = exports.addNewExport();
			export.setId(triggerChannelDTO.getId());
			export.setName(exportName);
			// Start 14-Oct-2013 Introduced exportName attribute to Export in xml
			export.setExportName(triggerChannelDTO.getName());
			// End 14-Oct-2013

			// Start 08-Jul-2015 Added Measurement type to xml to be added in hte cont table
			if (triggerChannelDTO.getType().equalsIgnoreCase(M9kConstants.HARMONIC))
			{
//				System.out.println("Algorithm type "+triggerChannelDTO.getType());
				if (((MagnitudeTrigger)genericTrigger).getHarmonic() != null)
				{
//					System.out.println("Harmonic number "+Integer.parseInt(((MagnitudeTrigger)genericTrigger).getHarmonic()));
					export.setMeasurementType(triggerChannelDTO.getType()+"-"+Integer.parseInt(((MagnitudeTrigger)genericTrigger).getHarmonic()));
				}
				else
				{
					export.setMeasurementType(triggerChannelDTO.getType());
				}
				
			}
			else
			{
				export.setMeasurementType(triggerChannelDTO.getType());
			}
			// End 08-Jul-2015
			
			export.setPhase(triggerChannelDTO.getPhase());
			export.setUnits(triggerChannelDTO.getUnits());
			export.setInput(triggerChannelDTO.getExportXMLInputString(genericTrigger));
			export.setSampleRate(stationDetails.getExportRate());
//			if (triggerChannelDTO.getExportRate() != null)
//			{
//				export.setSampleRate(triggerChannelDTO.getExportRate());
//			}
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
			localPmuInput.setInput(genericTrigger.getInputXMLString().replace(M9kXMLConstants.XML_PHASOR_VALUE_INPUT, M9kXMLConstants.XML_VALUE_INPUT));
			localPmuInput.setPmuInputType(genericTrigger.getPmuInputType());
			if (pmuInputs == null)
			{	
				pmuInputs = currentSubstation.getPmus().getPmuArray(0).addNewPmuInputs();
			}
			pmuInput = pmuInputs.addNewPmuInput();
			pmuInput.setName(triggerChannelDTO.getMeasurementName());
			pmuInput.setId(export.getId());
			pmuInput.setDfr(xmlConfigDfr.getSystem().getDfrId());

//			if(getFreqPmuId() != null && getFreqPmuId() == i)
			if (triggerChannelDTO.isFreqPmuStatus())
			{
				pmuInput.setPmuInputType(M9kConstants.PMU_INPUT_TYPE_FREQ);
				localPmuInput.setPmuInputType(M9kConstants.PMU_INPUT_TYPE_FREQ);
			}
			if (triggerChannelDTO.getStatus() && triggerChannelDTO.isPmuStatus())
			{
				pmuInput.setPmuEnable(M9kConstants.ENABLE_ONE);
				localPmuInput.setPmuEnable(M9kConstants.ENABLE_ONE);
//				localPmu.setPmuEnable(M9kConstants.ENABLE_ONE);
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
			measurement = measurements.addNewMeasurement();
			measurement.setId(triggerChannelDTO.getId());
			measurement.setName(triggerChannelDTO.getName());
			measurement.setDfr(xmlConfigDfr.getSystem().getDfrId());
//			logger.debug("Is Measurement enabled? "+triggerChannelDTO.getStatus());
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

			logger.debug("Exports from xml returning "+exports.xmlText(M9kXMLUtils.getXmlOptions()));
		}
	 
		public String showAddMeasurementsDialog()
		{
			logger.debug("Show add measurements dialog"+getLstTriggerChannels());
			setTotalMeasurementsCount(getLstTriggerChannels().size());
			return SUCCESS;
		}
	public List<TriggerChannelDTO> getLstVirtualMeasurements() {
		return lstVirtualMeasurements;
	}

	public void setLstVirtualMeasurements(List<TriggerChannelDTO> lstVirtualMeasurements) {
		this.lstVirtualMeasurements = lstVirtualMeasurements;
	}

	public Map<String, String> getMapTriggerInputTypes() {
		return mapTriggerInputTypes;
	}

	public void setMapTriggerInputTypes(Map<String, String> mapTriggerInputTypes) {
		this.mapTriggerInputTypes = mapTriggerInputTypes;
	}

	/**
	 * @return the lstAssociatedMeasurements
	 */
	public List<TriggerChannelDTO> getLstAssociatedMeasurements() {
		return lstAssociatedMeasurements;
	}

	/**
	 * @param lstAssociatedMeasurements the lstAssociatedMeasurements to set
	 */
	public void setLstAssociatedMeasurements(List<TriggerChannelDTO> lstAssociatedMeasurements) {
		this.lstAssociatedMeasurements = lstAssociatedMeasurements;
	}

	/**
	 * @return the lstLinegroupsModified
	 */
	public List<String> getLstLinegroupsModified() {
		return lstLinegroupsModified;
	}

	/**
	 * @param lstLinegroupsModified the lstLinegroupsModified to set
	 */
	public void setLstLinegroupsModified(List<String> lstLinegroupsModified) {
		this.lstLinegroupsModified = lstLinegroupsModified;
	}

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

	/**
	 * @return the newMeasurementsCount
	 */
	public int getNewMeasurementsCount() {
		return newMeasurementsCount;
	}

	/**
	 * @param newMeasurementsCount the newMeasurementsCount to set
	 */
	public void setNewMeasurementsCount(int newMeasurementsCount) {
		this.newMeasurementsCount = newMeasurementsCount;
	}

	/**
	 * @return the lstNewMeasurements
	 */
	public List<TriggerChannelDTO> getLstNewMeasurements() {
		return lstNewMeasurements;
	}

	/**
	 * @param lstNewMeasurements the lstNewMeasurements to set
	 */
	public void setLstNewMeasurements(List<TriggerChannelDTO> lstNewMeasurements) {
		this.lstNewMeasurements = lstNewMeasurements;
	}

	/**
	 * @return the totalMeasurementsCount
	 */
	public int getTotalMeasurementsCount() {
		return totalMeasurementsCount;
	}

	/**
	 * @param totalMeasurementsCount the totalMeasurementsCount to set
	 */
	public void setTotalMeasurementsCount(int totalMeasurementsCount) {
		this.totalMeasurementsCount = totalMeasurementsCount;
	}

	/**
	 * @return the mapAnalogTriggerInputTypes
	 */
	public Map<String, String> getMapAnalogTriggerInputTypes() {
		if (mapAnalogTriggerInputTypes == null)
		{
			mapAnalogTriggerInputTypes = M9kUtils.convertListToMap(config.getList("analogInputTriggerType"));
			mapAnalogTriggerInputTypes.remove("Delete");
			mapAnalogTriggerInputTypes.remove("THD");
		}
		logger.debug("mapAnalogTriggerInputTypes "+mapAnalogTriggerInputTypes);
		return mapAnalogTriggerInputTypes;
	}

	/**
	 * @param mapAnalogTriggerInputTypes the mapAnalogTriggerInputTypes to set
	 */
	public void setMapAnalogTriggerInputTypes(Map<String, String> mapAnalogTriggerInputTypes) {
		this.mapAnalogTriggerInputTypes = mapAnalogTriggerInputTypes;
	}

	/**
	 * @return the newMeasurementsType
	 */
	public String getNewMeasurementsType() {
		return newMeasurementsType;
	}

	/**
	 * @param newMeasurementsType the newMeasurementsType to set
	 */
	public void setNewMeasurementsType(String newMeasurementsType) {
		this.newMeasurementsType = newMeasurementsType;
	}

	/**
	 * @return the booReorderMeasurementIds
	 */
	public Boolean getBooReorderMeasurementIds() {
		return booReorderMeasurementIds;
	}

	/**
	 * @param booReorderMeasurementIds the booReorderMeasurementIds to set
	 */
	public void setBooReorderMeasurementIds(Boolean booReorderMeasurementIds) {
		this.booReorderMeasurementIds = booReorderMeasurementIds;
	}

	/**
	 * @return the measurementToDelete
	 */
	public TriggerChannelDTO getMeasurementToDelete() {
		return measurementToDelete;
	}

	/**
	 * @param measurementToDelete the measurementToDelete to set
	 */
	public void setMeasurementToDelete(TriggerChannelDTO measurementToDelete) {
		this.measurementToDelete = measurementToDelete;
	}

	/**
	 * @return the selectedMeasurementsToDelete
	 */
	public List<TriggerChannelDTO> getSelectedMeasurementsToDelete() {
		return selectedMeasurementsToDelete;
	}

	/**
	 * @param selectedMeasurementsToDelete the selectedMeasurementsToDelete to set
	 */
	public void setSelectedMeasurementsToDelete(List<TriggerChannelDTO> selectedMeasurementsToDelete) {
		this.selectedMeasurementsToDelete = selectedMeasurementsToDelete;
	}

	/**
	 * @return the lstDeletedMeasurements
	 */
	public List<TriggerChannelDTO> getLstDeletedMeasurements() {
		return lstDeletedMeasurements;
	}

	/**
	 * @param lstDeletedMeasurements the lstDeletedMeasurements to set
	 */
	public void setLstDeletedMeasurements(List<TriggerChannelDTO> lstDeletedMeasurements) {
		this.lstDeletedMeasurements = lstDeletedMeasurements;
	}
}
