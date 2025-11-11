package com.usi.m9000.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.usi.AlgorithmsDocument.Algorithms;
import com.usi.AnalogInputDocument.AnalogInput;
import com.usi.AnalogsDocument.Analogs;
import com.usi.ChannelsDocument.Channels;
import com.usi.DFRDocument.DFR;
import com.usi.DFRsDocument.DFRs;
import com.usi.DataPoolDocument.DataPool;
import com.usi.EventInputDocument.EventInput;
import com.usi.EventsDocument.Events;
import com.usi.PmuDocument.Pmu;
import com.usi.PmusDocument.Pmus;
import com.usi.SubStationDocument.SubStation;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;
@Validations
public class SaveDFRAction extends ActionSupport implements SessionAware, ServletContextListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Map<String, Object> session;
	String btnSubmit;
	List<String> lstBtnDelete;
	List<DfrDTO> lstDfrDTO;
	private SubStation currentSubstation;
	int minimumDFRCount;
	StationDTO stationDetails;
	String accessMode;
	private Map<String, String> mapLineFrequencies;
	private List<Object> lstSampleRate;
	Integer dfrIndex;
	PropertiesConfiguration cfgDb;
	PropertiesConfiguration cfgM9000Properties;
	Map<String, DFR> mapXMLDfrs;
	private UsersDTO userDto;
	
	// 25-May-2021 - Moved dfrs channels options list from ChannelsListInfo to here
	private List<String> channelsConfList;
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(SaveDFRAction.class);
	/**
	 * 
	 */
	public SaveDFRAction()
	{
		setChannelsConfList(M9kUtils.getDfrsChannelsOptionsList());
	}

	@SkipValidation
	  public String execute()throws Exception{
		  logger.debug("Entered execute method of SaveDFRAction... ");
		  setChannelsConfList(M9kUtils.getDfrsChannelsOptionsList());
		  String returnValue = SUCCESS;
		  if (getLstBtnDelete() != null )
		  {
			  logger.debug("Btns list... "+getLstBtnDelete());
			  stationDetails = (StationDTO) session.get("stationDetails");
			  int index;
			  for (index = 0; index < getLstBtnDelete().size(); index++) {
				if (getLstBtnDelete().get(index) != null)
					break;
				
			}
			  logger.debug("index to be deleted.. "+index);
			  logger.debug("list before delete.. "+getLstDfrDTO());
			  getLstDfrDTO().remove(index);
			  // START: 19-Nov-2020 - Option to delete dfr while creating new stations needs index updated 
			  updateDfrsListAfterDelete();
			  // END: 19-Nov-2020
			  logger.debug("list after delete.. "+getLstDfrDTO());
			  session.remove("DFRsList");
			  session.put("btnSubmit","delete");
			  session.put("DFRsList",getLstDfrDTO());
			  logger.debug("list from session after delete.. "+session.get("DFRsList"));
			  returnValue = "Delete";
		  }
		  return returnValue;
	  }

	/**
	 * Util method to update dfr index after deleting dfrs while creating new station
	 * START: 19-Nov-2020 - Option to delete dfr while creating new stations needs index updated
	 */
	private void updateDfrsListAfterDelete()
	{
		String dfrIpAddress = cfgM9000Properties.getString("dfrIpAddressStart", "192.168.1.101");
		  String ipPrefix = dfrIpAddress.substring(0,dfrIpAddress.lastIndexOf(".")+1);
		  int ipAddressCount = Integer.parseInt(dfrIpAddress.substring(dfrIpAddress.lastIndexOf(".")+1));
		  
		for (int i = 0; i < getLstDfrDTO().size(); i++) {
			getLstDfrDTO().get(i).setDfrName("DFR"+(i+1)); 
			getLstDfrDTO().get(i).setDfrId(i+1);
			getLstDfrDTO().get(i).setIpAddress(ipPrefix+(ipAddressCount+i));
		} 
	}
	  private void UpdateXMLConfigDFRs()
	  {
		 
		  DFR xmlConfigDfr;
		  DfrDTO dfrDto;
		  DataPool dataPool;
		  Channels channels; 
		  Analogs analogs = null;
		  Algorithms algorithms;
		  Events events = null;
		  AnalogInput analogInput;
		  EventInput eventInput;
		  int tstI = 0;
		  DFRs dfrs = currentSubstation.getDFRs();
		  if (dfrs == null)
		  {
			  dfrs = currentSubstation.addNewDFRs();
		  }
		  logger.debug("Before adding new DFR... "+currentSubstation.getDFRs().sizeOfDFRArray() +" getChatterLimit value selected... "+stationDetails.getChatterLimit());
		  for (Iterator<DfrDTO> iterator = getLstDfrDTO().iterator(); iterator.hasNext();) {
			dfrDto = iterator.next();
			// 16-Nay-2021 - Saving the newly added dfrs.Hence skipping already created dfrs
			if (!dfrDto.isNewlyAdded() && (mapXMLDfrs.get(dfrDto.getDfrName())) != null)
			{
				mapXMLDfrs.get(dfrDto.getDfrName()).getSystem().setAnalogsCount(stationDetails.getSystemAnalogChannelsCount());
				mapXMLDfrs.get(dfrDto.getDfrName()).getSystem().setDigitalsCount(stationDetails.getSystemDigitalChannelsCount());
				continue;
			}
			xmlConfigDfr = dfrs.addNewDFR();
			logger.debug("adding a new DFR... "+tstI+" size: "+dfrs.sizeOfDFRArray()+" dfr id "+dfrDto.getDfrId());

			logger.debug("DIGITAL: digital channels count "+stationDetails.getSystemDigitalChannelsCount());
			xmlConfigDfr.setId(dfrDto.getDfrId());
			xmlConfigDfr.setIPAddress(dfrDto.getIpAddress());
			com.usi.SystemDocument.System systemProperties = xmlConfigDfr.addNewSystem();
			systemProperties.setDfrId(dfrDto.getDfrId());
			systemProperties.setStationName(stationDetails.getSystemStationName());
			systemProperties.setAnalogsCount(stationDetails.getSystemAnalogChannelsCount());
			systemProperties.setDigitalsCount(stationDetails.getSystemDigitalChannelsCount());
			systemProperties.setSampleRate(stationDetails.getSystemSampleRate());
			systemProperties.setLineFrequency(stationDetails.getSystemLineFrequency());
			systemProperties.setExportRate(stationDetails.getExportRate());
			systemProperties.setRecordingDeviceId(stationDetails.getSystemRecordingDeviceId());
//			systemProperties.setRecordingDeviceName(stationDetails.getSystemRecordingDeviceName());
			systemProperties.setPrefaultTime(stationDetails.getSystemPrefaultTime());
			systemProperties.setPostfaultTime(stationDetails.getSystemPostfaultTime());
			systemProperties.setLtrPrefaultTime(stationDetails.getSystemLtrPrefaultTime());
			systemProperties.setLtrPostfaultTime(stationDetails.getSystemLtrPostfaultTime());
			systemProperties.setChatterLimit(stationDetails.getChatterLimit());
			systemProperties.setChatterRate(stationDetails.getChatterRate());
			systemProperties.setTriggerLimit(stationDetails.getTriggerLimit());
			systemProperties.setExportAnalogBufferTime(cfgM9000Properties.getInt("exportAnalogBufferTime",20));
//			systemProperties.setExportAnalogBufferTime(60);
			systemProperties.setExportAnalogSampleRate(stationDetails.getSystemLongTermSampleRate());
			systemProperties.setExportBufferTime(cfgM9000Properties.getInt("exportBufferTime",20));
			systemProperties.setAntiAliasFilterDelay(cfgM9000Properties.getDouble("antiAliasFilterDelay",41.04555));
			systemProperties.setDatabaseDataType(M9kConstants.Binary.toUpperCase());
			systemProperties.setDatabaseConnectString(getDbConnectionString());
			systemProperties.setPs(stationDetails.getPs());
			systemProperties.setSignalTooLowFactor(stationDetails.getSignalTooLowFactor());
			systemProperties.setEventDebounce(stationDetails.getEventDebounce());
			// START: 06-MAR-2018 - Configurable wetting voltage
			systemProperties.setMonitorWettingVoltage(stationDetails.getWettingVoltageMonitor());
			// END: 06-Mar-2018
//			initialiazeComtradeConfig(xmlConfigDfr);
			dataPool = xmlConfigDfr.addNewDataPool();
			dataPool.setName("DataPool");
			channels = dataPool.addNewChannels();
			channels.setName("Channels");
			algorithms = dataPool.addNewAlgorithms();
			algorithms.setName("Algorithms");
			Pmus localPmus = algorithms.addNewPmus();
			localPmus.setName("PMUs");
			Pmu localPmu = localPmus.addNewPmu();
			// Start 11-Oct-2013 PMU name in PDC changed to reflect the dfr name instead of station name for all chassis
//			localPmu.setName(stationDetails.getSystemStationName());
			localPmu.setName(dfrDto.getDfrName());
			localPmu.setPmuName(dfrDto.getDfrName());
			// End 11-Oct-2013
			localPmu.setPmuId(stationDetails.getPmuId());
			localPmu.setPmuPort(stationDetails.getPmuPort());
			localPmu.setUdpPort(stationDetails.getPmuUdpPort());
			localPmu.setPmuStreamType(stationDetails.getPmuStreamType());
			localPmu.setPmuPhasorMode(stationDetails.getPmuPhasorMode());
			localPmu.setPmuDataRate(stationDetails.getPmuDataRate());
//			if (stationDetails.getPmuStatus().equalsIgnoreCase(M9kConstants.ENABLE))
//			{
//				localPmu.setPmuEnable(M9kConstants.ENABLE_ONE);
//			}
//			else
//			{
				localPmu.setPmuEnable(M9kConstants.DISABLE_ZERO);
//			}
			if (dfrDto.getChnlConfigDropDownList().getAnalogCnt() > 0)
			{
				analogs = channels.addNewAnalogs();
				analogs.setName("Analogs");
			}
			if (dfrDto.getChnlConfigDropDownList().getDigitalCnt() > 0)
			{
				events = channels.addNewEvents();
				events.setName("Events");
			}
			 logger.debug("Entered UpdateXMLConfigDFRs DFR"+(++tstI)+" ... "+dfrDto.getChnlConfigDropDownList().getAnalogCnt());
//			 int nextAvailableChannelIndex;
//			nextAvailableChannelIndex = dfrDto.getAnalogChannelStart();
			for (int i = 0; i < dfrDto.getChnlConfigDropDownList().getAnalogCnt(); i++) {
				analogInput = analogs.addNewAnalogInput();
				// Saravanan 24-Mar-11 start
//				analogInput.setName("Analog"+nextAvailableChannelIndex);
//				analogInput.setChannel(nextAvailableChannelIndex++);
				analogInput.setName("Analog"+(i+1));
				analogInput.setChannel(i+1);
				analogInput.setCircuitName("Analog"+(i+dfrDto.getAnalogChannelStart()));
				// Saravanan 24-Mar-11 end
				// 05-Feb-2020 - Default export of Oscillography
				analogInput.setExport(M9kConstants.ENABLE_ONE);
				// 05-Feb-2020
				analogInput.setRange(41);
				analogInput.setTransformerPrimary(1);
				analogInput.setTransformerSecondary(1);

//				analogInput.setPs(stationDetails.getPs());
			}
//			nextAvailableChannelIndex = dfrDto.getDigitalChannelStart();
			for (int i = 0; i < dfrDto.getChnlConfigDropDownList().getDigitalCnt(); i++) {
				eventInput = events.addNewEventInput();
				// Saravanan 24-Mar-11 start
				eventInput.setName("Event"+(i+1));
//				eventInput.setChannel(nextAvailableChannelIndex++);
				eventInput.setEventName("Event "+(i+dfrDto.getDigitalChannelStart()));
				eventInput.setChannel(i+1);
				// Saravanan 24-Mar-11 end
			}
			mapXMLDfrs.put(dfrDto.getDfrName(), xmlConfigDfr);
		  }
	  }
	  
//	  private void initialiazeComtradeConfig(DFR xmlConfigDfr)
//	  {
//		  Comtrade comtradeConfig;
//		  comtradeConfig = xmlConfigDfr.addNewComtrade();
//		  comtradeConfig.addNewHeaderFile();
//		  comtradeConfig.addNewInformationFile();
//		  ConfigurationFile ctConfigFile = comtradeConfig.addNewConfigurationFile();
//		  General ctGeneral = ctConfigFile.addNewGeneral();
//		  ctGeneral.setRevision(1999);
//		  ctGeneral.setFileType("ASCII");
//		  ctGeneral.setTimeStampMult(1);
//	  }

	@SkipValidation
	  public String finishAndReturn()
	  {
		  logger.debug("Returning from back method with list.."+lstDfrDTO);
		  return SUCCESS;
	  }
	  
	  @SuppressWarnings("unchecked")
	@SkipValidation
	  public String back()
	  {
		  logger.debug("Entered backFromConfigDfr....");
		  mapLineFrequencies = (Map<String, String>) session.get("mapLineFrequencies");
//		  lstSampleRate = (List<String>) session.get("lstSampleRate");
		  return SUCCESS;
	  }

	  @SkipValidation
	  public String addNewDfr()
	  {
		  logger.debug("Entered addNewDfr....");
		  String returnValue=  "Add";
//		  session.put("DFRsList",getLstDfrDTO());
		  return returnValue;
	  }

	  @SkipValidation
	  public String deleteDfr()
	  {
		  String returnValue = "Delete";
		  logger.debug("Dfr index..."+getDfrIndex());
		  logger.debug("index to be deleted.. "+getDfrIndex());
		  logger.debug("list before delete.. "+getLstDfrDTO());
		  getLstDfrDTO().remove(getDfrIndex().intValue());
		  logger.debug("list after delete.. "+getLstDfrDTO());
		  session.remove("DFRsList");
		  session.put("btnSubmit","delete");
		  session.put("DFRsList",lstDfrDTO);
		  
		  return returnValue;
	  }
	  
	  public String next()
	  {
		String returnValue = SUCCESS;
		StationDTO stationDetails = (StationDTO) session.get("stationDetails");
		mapXMLDfrs = new HashMap<String, DFR>();
//		  logger.debug("DFRDTO list after clicking Next.."+getLstDfrDTO());
		  session.put("DFRsList",lstDfrDTO);
		  if (accessMode.equals("Edit")) // Save in Edit mode
		  {
//			  logger.debug("stationDetails config status "+stationDetails.getConfigStatus());
			  currentSubstation = (SubStation) session.get("selectedSubstation");
			  DFR xmlConfigDfr = null;
			  for (int i = 0; i < getLstDfrDTO().size(); i++) {
				  lstDfrDTO.get(i).setDfrName(getLstDfrDTO().get(i).getDfrName());
				  lstDfrDTO.get(i).setDfrId(getLstDfrDTO().get(i).getDfrId());
				  lstDfrDTO.get(i).setIpAddress(getLstDfrDTO().get(i).getIpAddress());
				  xmlConfigDfr = currentSubstation.getDFRs().getDFRArray(i);
//				 logger.debug("adding a new DFR... "+i+" size: "+currentSubstation.getDFRs().sizeOfDFRArray());

					xmlConfigDfr.setId(lstDfrDTO.get(i).getDfrId());
					xmlConfigDfr.setIPAddress(lstDfrDTO.get(i).getIpAddress());
					  com.usi.SystemDocument.System systemProperties = xmlConfigDfr.getSystem();
						systemProperties.setSampleRate(stationDetails.getSystemSampleRate());
						systemProperties.setLineFrequency(stationDetails.getSystemLineFrequency());
						systemProperties.setExportRate(stationDetails.getExportRate());
						systemProperties.setStationName(stationDetails.getSystemStationName());
						systemProperties.setDfrId(getLstDfrDTO().get(i).getDfrId());
						systemProperties.setRecordingDeviceId(stationDetails.getSystemRecordingDeviceId());
						systemProperties.setPrefaultTime(stationDetails.getSystemPrefaultTime());
						systemProperties.setPostfaultTime(stationDetails.getSystemPostfaultTime());
						systemProperties.setLtrPrefaultTime(stationDetails.getSystemLtrPrefaultTime());
						systemProperties.setLtrPostfaultTime(stationDetails.getSystemLtrPostfaultTime());
						systemProperties.setChatterLimit(stationDetails.getChatterLimit());
						systemProperties.setChatterRate(stationDetails.getChatterRate());
						systemProperties.setTriggerLimit(stationDetails.getTriggerLimit());
						systemProperties.setExportAnalogBufferTime(cfgM9000Properties.getInt("exportAnalogBufferTime",20));
//						systemProperties.setExportAnalogBufferTime(60);
						systemProperties.setExportAnalogSampleRate(stationDetails.getSystemLongTermSampleRate());
						systemProperties.setExportBufferTime(cfgM9000Properties.getInt("exportBufferTime",20));
						systemProperties.setAntiAliasFilterDelay(cfgM9000Properties.getDouble("antiAliasFilterDelay",41.04555));
						systemProperties.setDatabaseDataType(M9kConstants.Binary.toUpperCase());
						systemProperties.setDatabaseConnectString(getDbConnectionString());
						systemProperties.setPs(stationDetails.getPs());
						systemProperties.setSignalTooLowFactor(stationDetails.getSignalTooLowFactor());
						systemProperties.setEventDebounce(stationDetails.getEventDebounce());
						// START: 06-MAR-2018 - Configurable wetting voltage
						systemProperties.setMonitorWettingVoltage(stationDetails.getWettingVoltageMonitor());
						// END: 06-Mar-2018
						mapXMLDfrs.put(getLstDfrDTO().get(i).getDfrName(), xmlConfigDfr);
			  }
			  
			  session.put("mapXMLDfrs",mapXMLDfrs);
		  }		  
		  logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" is in the DFRs display page");
		return returnValue;
	  }
	  public String saveDfrConfig()
	  {
		  logger.info("Entered dfr Config..."+getLstDfrDTO());
		  String returnValue = SUCCESS;
		  mapXMLDfrs = new HashMap<String, DFR>();

		  if (accessMode == null || !accessMode.equals("Edit"))// Save in create mode
		  {
			  setChannelsConfList(M9kUtils.getDfrsChannelsOptionsList());
			  returnValue = "Save";
			  int nextStartAnalogIndex = -1;
			  int nextStartDigitalIndex = -1;
			  int totalAnalogs = 0;
			  int totalDigitals = 0;
			  int analogCnt;
			  int digitalCnt;
			  for (Iterator<DfrDTO> iterator = getLstDfrDTO().iterator(); iterator.hasNext();) {
				DfrDTO dfrDto = iterator.next();
				analogCnt = dfrDto.getChnlConfigDropDownList().getAnalogCnt(); 
				dfrDto.setAnalogChnlCnt(analogCnt);
				logger.debug("Analog count "+analogCnt);
				if ( analogCnt > 0)
				{
					totalAnalogs+=analogCnt;
					if (nextStartAnalogIndex == -1)
					{
						dfrDto.setAnalogChannelStart(1);
						dfrDto.setAnalogChannelEnd(analogCnt);
						// START: 05-June-2020 - Mini implementation
//						nextStartAnalogIndex = (analogCnt-(analogCnt%8))+1;
						nextStartAnalogIndex = analogCnt + 1;
						// END: 05-June-2020 - Mini implementation
						logger.debug("dfr set: "+dfrDto);
					}
					else 
					{
						dfrDto.setAnalogChannelStart(nextStartAnalogIndex);
						dfrDto.setAnalogChannelEnd(nextStartAnalogIndex + analogCnt - 1);
						// START: 05-June-2020 - Mini implementation
//						nextStartAnalogIndex += (analogCnt-(analogCnt%8));
						nextStartAnalogIndex += analogCnt;
						// END: 05-June-2020 - Mini implementation
					}
					logger.debug("\t\tMINI: Next start Analog index "+nextStartAnalogIndex);
				}
				digitalCnt = dfrDto.getChnlConfigDropDownList().getDigitalCnt();
				dfrDto.setDigitalChnlCnt(digitalCnt);
				logger.debug("digitalCnt count "+digitalCnt);
				if (digitalCnt > 0)
				{
					totalDigitals += digitalCnt;
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
				logger.debug("dfr Details: "+dfrDto);
			}
			  
			  session.put("DFRsList",getLstDfrDTO());
			  currentSubstation = (SubStation) session.get("selectedSubstation");
			  stationDetails = (StationDTO) session.get("stationDetails");
			  stationDetails.setSystemAnalogChannelsCount(totalAnalogs);
			  stationDetails.setSystemDigitalChannelsCount(totalDigitals);
			  logger.debug("currentSubstation.getDFRArray()... "+currentSubstation.getDFRs().getDFRArray());
			  logger.debug("currentSubstation.sizeOfDFRArray()... "+currentSubstation.getDFRs().sizeOfDFRArray());
			  logger.debug("currentSubstation.getDFRArray().length... "+currentSubstation.getDFRs().getDFRArray().length);
			  int i = 0;
			  while(currentSubstation.getDFRs().sizeOfDFRArray() > 0) {
				  logger.debug("Removing... DFR"+(i++)+" currentSubstation.sizeOfDFRArray() "+currentSubstation.getDFRs().sizeOfDFRArray()+" currentSubstation.getDFRArray().length" + currentSubstation.getDFRs().getDFRArray().length);
				  currentSubstation.getDFRs().removeDFR(0);
			  } 
			  minimumDFRCount = ((Integer)session.get("minimumDFRCount")).intValue();
			  UpdateXMLConfigDFRs();
			  logger.debug("######SaveDFRACtion DFR Cnt in XML COnfig... "+currentSubstation.getDFRs().sizeOfDFRArray());
			  logger.debug("######SaveDFRACtion getLstDfrDTO() size... "+getLstDfrDTO().size());
		  }
		  else if (accessMode.equals("Edit")) // Save in Edit mode
		  {
			  returnValue = SUCCESS;
			  currentSubstation = (SubStation) session.get("selectedSubstation");
			  
			  DFR xmlConfigDfr;
			  for (int i = 0; i < getLstDfrDTO().size(); i++) {
				  lstDfrDTO.get(i).setDfrName(getLstDfrDTO().get(i).getDfrName());
				  lstDfrDTO.get(i).setDfrId(getLstDfrDTO().get(i).getDfrId());
				  lstDfrDTO.get(i).setIpAddress(getLstDfrDTO().get(i).getIpAddress());
				  xmlConfigDfr = currentSubstation.getDFRs().getDFRArray(i);
				 logger.debug("adding a new DFR... "+i+" size: "+currentSubstation.getDFRs().sizeOfDFRArray());

					xmlConfigDfr.setId(lstDfrDTO.get(i).getDfrId());
					xmlConfigDfr.setIPAddress(lstDfrDTO.get(i).getIpAddress());
					mapXMLDfrs.put(getLstDfrDTO().get(i).getDfrName(), xmlConfigDfr);
			  }
		  }
		  logger.info("User "+(getUserDto() != null?getUserDto().getUserName():"")+" saved the required chassis configuration for the station");
		  session.put("mapXMLDfrs",mapXMLDfrs);
		  return returnValue;
	  }
	  
	  /**
	   * 16-May-2021 Saving the newly added dfrs
	   * @return
	   */
	@Action(value = "saveNewlyAddedDfrsAction", results = {
		@Result(name = SUCCESS, type = "chain", params = {"actionName", "editStationDetails"})})
		  @SkipValidation
	  public String saveNewlyAddedDfrs()
	  {
		  String returnStatus = SUCCESS;
		  int analogCnt;
		  int digitalCnt;
		  int nextStartAnalogIndex = stationDetails.getSystemAnalogChannelsCount();
		  int nextStartDigitalIndex = stationDetails.getSystemDigitalChannelsCount();
		  int icntDfrList = getLstDfrDTO().size()-1;
		  mapXMLDfrs = new HashMap<String, DFR>();
		  for (Iterator<DfrDTO> iterator = getLstDfrDTO().iterator(); iterator.hasNext();) {
				DfrDTO dfrDto = iterator.next();
				logger.debug("DELETE-DEBUG: dfrDto name "+dfrDto.getDfrName()+" dfrID "+dfrDto.getDfrId()+" is newly added "+dfrDto.isNewlyAdded()+" size of xml drs "+currentSubstation.getDFRs().sizeOfDFRArray());
				logger.debug("DELETE-DEBUG: "+dfrDto.getChnlConfigDropDownList().getAnalogCnt()+" digital "+dfrDto.getChnlConfigDropDownList().getDigitalCnt());
				if (!dfrDto.isNewlyAdded())
				{
					mapXMLDfrs.put(dfrDto.getDfrName(), currentSubstation.getDFRs().getDFRArray(dfrDto.getDfrId()-1));
					continue;
				}
				analogCnt = dfrDto.getChnlConfigDropDownList().getAnalogCnt(); 
				dfrDto.setAnalogChnlCnt(analogCnt);
				logger.debug("Analog count "+analogCnt);
				if ( analogCnt > 0)
				{
					dfrDto.setAnalogChannelStart(nextStartAnalogIndex+1);
					dfrDto.setAnalogChannelEnd(nextStartAnalogIndex + analogCnt - 1);
					// START: 05-June-2020 - Mini implementation
//						nextStartAnalogIndex += (analogCnt-(analogCnt%8));
					nextStartAnalogIndex += analogCnt;
					// END: 05-June-2020 - Mini implementation
					logger.debug("\t\tMINI: Next start Analog index "+nextStartAnalogIndex);
				}
				digitalCnt = dfrDto.getChnlConfigDropDownList().getDigitalCnt();
				dfrDto.setDigitalChnlCnt(digitalCnt);
				logger.debug("digitalCnt count "+digitalCnt);
				if (digitalCnt > 0)
				{
						dfrDto.setDigitalChannelStart(nextStartDigitalIndex+1);
						dfrDto.setDigitalChannelEnd(nextStartDigitalIndex + digitalCnt - 1);
						nextStartDigitalIndex += digitalCnt;
				}
				logger.debug("dfr Details: "+dfrDto);
			}
		  stationDetails.setSystemAnalogChannelsCount(nextStartAnalogIndex);
		  stationDetails.setSystemDigitalChannelsCount(nextStartDigitalIndex);
		  logger.debug("Last dfrs analog and digitals end AnalogsEnd "+getLstDfrDTO().get(icntDfrList).getAnalogChannelEnd()+" digitalsEnd "+getLstDfrDTO().get(icntDfrList).getDigitalChannelEnd());
		  UpdateXMLConfigDFRs();
		  stationDetails.setTotalDfrsConfigured(currentSubstation.getDFRs().sizeOfDFRArray());
		  session.put("stationDetails", stationDetails);
		  session.put("mapXMLDfrs",mapXMLDfrs);
		  return returnStatus;
	  }
	  @SuppressWarnings("unchecked")
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		currentSubstation = (SubStation) session.get("selectedSubstation");
		lstDfrDTO = (List<DfrDTO>) session.get("DFRsList");
		logger.debug("LstDfrDto size "+getLstDfrDTO().size()+" first dfrs analog and digital count "+getLstDfrDTO().get(0).getChnlConfigDropDownList().getAnalogCnt() +" digital "+lstDfrDTO.get(0).getChnlConfigDropDownList().getDigitalCnt());
		stationDetails = (StationDTO) session.get("stationDetails");
		cfgDb = (PropertiesConfiguration) session.get("cfgDb");
		cfgM9000Properties = (PropertiesConfiguration) session.get("config");
		logger.debug("cfgDb ..."+cfgDb);
		logger.debug("Export Analog Buffer Time "+cfgM9000Properties.getInt("exportAnalogBufferTime"));
	  accessMode = (String) session.get("accessMode"); 

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
	 * @return the lstDfrDTO
	 */
//	@VisitorFieldValidator(message="!!! ")
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
	 * @return the lstBtnDelete
	 */
	public List<String> getLstBtnDelete() {
		return lstBtnDelete;
	}

	/**
	 * @param lstBtnDelete the lstBtnDelete to set
	 */
	public void setLstBtnDelete(List<String> lstBtnDelete) {
		this.lstBtnDelete = lstBtnDelete;
	}

	/**
	 * @return the currentSubstation
	 */
	public SubStation getCurrentSubstation() {
		return currentSubstation;
	}

	/**
	 * @param currentSubstation the currentSubstation to set
	 */
	public void setCurrentSubstation(SubStation currentSubstation) {
		this.currentSubstation = currentSubstation;
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
	public void setMinimumDFRCount(int minimumDFRCount) {
		this.minimumDFRCount = minimumDFRCount;
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
	 * @return the dfrIndex
	 */
	public Integer getDfrIndex() {
		return dfrIndex;
	}

	/**
	 * @param dfrIndex the dfrIndex to set
	 */
	public void setDfrIndex(String dfrIndex) {
		this.dfrIndex = Integer.parseInt(dfrIndex);
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

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
//		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//	    LogFactory.release(contextClassLoader);
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
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

	public Map<String, DFR> getMapXMLDfrs() {
		return mapXMLDfrs;
	}

	public void setMapXMLDfrs(Map<String, DFR> mapXMLDfrs) {
		this.mapXMLDfrs = mapXMLDfrs;
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
