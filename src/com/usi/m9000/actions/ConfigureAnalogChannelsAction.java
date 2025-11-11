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

import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.AlgorithmsDocument.Algorithms;
import com.usi.AnalogInputDocument;
import com.usi.AnalogInputDocument.AnalogInput;
import com.usi.DFRDocument.DFR;
import com.usi.EventInputDocument.EventInput;
import com.usi.ExportDocument2.Export;
import com.usi.ExportsDocument.Exports;
import com.usi.RmsDocument.Rms;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.algorithms.RMSAlgorithm;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.EventChannelDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.M9kXMLConstants;

public class ConfigureAnalogChannelsAction extends ActionSupport implements
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
//	DfrDTO currentDfrDto = null;
//	DFR xmlConfigDfr;
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
	Map<String, DFR> mapXMLDfrs;
	boolean booAnalog;
	boolean booDigital;
	private boolean lineGroupExists;
	private UsersDTO userDto;
	private String originTab="";
	private int selectedChannelIndex;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ConfigureAnalogChannelsAction.class);

	/**
	 * 
	 */
	public ConfigureAnalogChannelsAction(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
	}

	public ConfigureAnalogChannelsAction() {
//		logger.debug("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is in the Analogs configuration screen");
	}
	
	public String execute()
	{
		String navigateValue = "";
		try
		{
			logger.debug("Inside ConfigureAnalogChannelsAction execute method..."+getLstAnalogChannels()+" originating tab "+getOriginTab());
			logger.debug("sourceTab...."+sourceTab);
			session.put("sourceTab", sourceTab);
			booAnalog = (Boolean)session.get("booAnalog");
			booDigital = (Boolean)session.get("booDigital");
			lineGroupExists = (Boolean)session.get("lineGroupExists");
//			session.put("sourceTab", sourceTab);
			if (getSourceTab() != null && !getSourceTab().isEmpty())
			{
				if (getOriginTab().equalsIgnoreCase("Analogs") && getLstAnalogChannels() != null)
				{
//					createRmsAlgorithmAndExport();
					populateAnalogsIntoXmlConfig();
				}
				if (getSourceTab().equalsIgnoreCase("Measurements"))
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
				else if (getSourceTab().equalsIgnoreCase("VirtualChannels"))
				{
					navigateValue="VirtualChannels";
				}
				// START: 10-Mar-2021 - Virtual Measurement implementation
				else if (getSourceTab().equalsIgnoreCase("VirtualMeasurements"))
				{
					navigateValue="VirtualMeasurements";
				}
				// END: 10-Mar-2021 - Virtual Measurement implementation
			}
			if (getSourceTab() == null || getSourceTab().isEmpty() || getSourceTab().equalsIgnoreCase("Analogs") )
			{
				navigateValue = populateAnalogDTO();
			}
		}
		catch (Exception e) {
			logger.error("Error navigating to LineGroups ",e);
		}
		return navigateValue;
	}

	@SkipValidation
	public String back() {
		String returnValue = "Back";
		logger.debug("Returning from analogs screen "+lstDfrDTO);
		logger.info("user "+getUserDto().getUserName() +" is traversing back to station details page");
		return returnValue;
	}

	@SkipValidation
	public String populateAnalogDTO() throws M9000Exception {
		mapCircuitNames = new HashMap<String, String>();
//		TreeSet<String> keys = new TreeSet<String>(mapXMLDfrs.keySet());
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
		try
		{
			Iterator<String> mapXmlDfrIterator =lstDfrNames.iterator();
			String dfrKey= null;
			DFR xmlDfr;
			AnalogInput[] analogInputs;
	//		AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels()
	//				.getAnalogs().getAnalogInputArray();
			AnalogChannelDTO analogDto;
	//		logger.info("populateAnalogDTO: DFR name: " + xmlConfigDfr.getSystem().getDfrId());
			lstAnalogChannels = new ArrayList<AnalogChannelDTO>(100);
			int analogChnlIndex = 1;
			for (; mapXmlDfrIterator.hasNext();) {
				dfrKey = mapXmlDfrIterator.next();
				xmlDfr = mapXMLDfrs.get(dfrKey);
				if (xmlDfr.getDataPool().getChannels().getAnalogs() == null)
				{
					continue;
				}
				analogInputs = xmlDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();
				for (int i = 0; i < analogInputs.length; i++,analogChnlIndex++) {
					// Skipping virtual channels as only Analog channels are porpulated
					if (analogInputs[i].isSetVirtual() || analogInputs[i].getVirtual() == 1)
					{
						analogChnlIndex--;						
						continue;
					}
					analogDto = new AnalogChannelDTO();
					analogDto.setChassis(dfrKey);
					analogDto.setName(analogInputs[i].getName());
					analogDto.setChannel("" + analogChnlIndex);
					if (analogInputs[i].getExport() == M9kConstants.ENABLE_ONE)
					{
						analogDto.setExportStatus(true);
					}
					else
					{
						analogDto.setExportStatus(false);
					}
	//				logger.info("analogInputs[i].getCircuitName() ... " + i + " --> "
	//						+ analogInputs[i].getCircuitName());
					analogDto.setCircuitName(analogInputs[i].getCircuitName());
					if (analogInputs[i].getCircuitName() != null
							&& !analogInputs[i].getCircuitName().isEmpty()) {
						mapCircuitNames.put("" + i, analogInputs[i].getCircuitName());
					}
					analogDto.setPhase(analogInputs[i].getPhase());
					
					// START: 23-Jan-2020 - Transducer implementation -- For Transducer set the input type To transducer voltage or current
					if (analogInputs[i].isSetTransducer() && analogInputs[i].getTransducer() == M9kConstants.ENABLE_ONE)
					{
						if ((analogInputs[i].getInputType().equalsIgnoreCase(
										M9kConstants.VOLTAGE_DC) ))
						{
							analogDto.setInputType(M9kConstants.TRANSDUCER_VOLTAGE);
						}
						else if (analogInputs[i]
								.getInputType().equalsIgnoreCase(
										M9kConstants.CURRENT_DC_EXTERNAL))
						{
							analogDto.setInputType(M9kConstants.TRANSDUCER_CURRENT);
						}

					}
					else
					{
						analogDto.setInputType(analogInputs[i].getInputType());
					}
	//				logger.info("Input type..." + analogInputs[i].getInputType());
		
					if (analogInputs[i].getInputType() != null
							&& (analogInputs[i].getInputType().equalsIgnoreCase(
									M9kConstants.CURRENT_AC_EXTERNAL) || analogInputs[i]
									.getInputType().equalsIgnoreCase(
											M9kConstants.CURRENT_DC_EXTERNAL) || analogDto
													.getInputType().equalsIgnoreCase(
															M9kConstants.TRANSDUCER_CURRENT)
					// START: 29-Jun-2020 - Hall Effect offset correction implementation
													|| analogDto
													.getInputType().equalsIgnoreCase(
															M9kConstants.CURRENT_AC_HALL_EFFECT_EXTERNAL_SHUNT))) {
					// END: 29-Jun-2020
					// END: 23-Jan-2020
					analogDto.setDisableExtSunt(false);
					} else {
						analogDto.setDisableExtSunt(true);
					}
	//				logger.info("Is Ext shunt Disabled for channel " + i + "? "
	//						+ analogDto.getDisableExtSunt() + " getAccessMode().."
	//						+ getAccessMode());
					if (getAccessMode() == null || getAccessMode().isEmpty()) {
	//					logger.info("Entered If loop ");
						analogDto.setRange("41");
						analogDto.setPrimaryRatio("1");
						analogDto.setSecondaryRatio("1");
					} else {
	//					logger.info("Entered Else loop ");
		//				analogDto.setScale("" + analogInputs[i].getScale());
						analogDto.setRange("" + analogInputs[i].getRange());
		//				analogDto.setOffset("" + analogInputs[i].getOffset());
						analogDto.setPrimaryRatio(""
								+ analogInputs[i].getTransformerPrimary());
						analogDto.setSecondaryRatio(""
								+ analogInputs[i].getTransformerSecondary());
						if (analogInputs[i].isSetExternalShunt())
						{
							analogDto.setExtShunt(""+analogInputs[i].getExternalShunt());
						}
					}
					// START: 23-Jan-2020 - Transducer implementation
					if (analogInputs[i].isSetTransducer() && analogInputs[i].getTransducer() == M9kConstants.ENABLE_ONE) {
						analogDto.setTransducer(true);
						analogDto.setTransducerUnits(analogInputs[i].getTransducerUnits());
						analogDto.setInP1(analogInputs[i].getInP1());
						analogDto.setOutP1(analogInputs[i].getOutP1());
						analogDto.setInP2(analogInputs[i].getInP2());
						analogDto.setOutP2(analogInputs[i].getOutP2());
					}
					// END: 23-Jan-2020
					
					getLstAnalogChannels().add(analogDto);
				}
			}
			session.put("mapCircuitNames", mapCircuitNames);
	//		logger.info("lstAnalogChannels stored in session..."
	//				+ lstAnalogChannels);
	//		for (Iterator<AnalogChannelDTO> iterator = lstAnalogChannels.iterator(); iterator
	//				.hasNext();) {
	//			AnalogChannelDTO type = iterator.next();
	//			logger.info("Channel in the list.... " + type.getChannel());
	//			logger.info("Channel Name in the list.... " + type.getName());
	//			logger
	//					.info("Circuit Name in the list.... "
	//							+ type.getCircuitName());
	//			logger.info("Status of External Shunt in the list.... "
	//					+ type.getDisableExtSunt());
	//		}
			session.remove("lstAnalogChannels");
			session.put("lstAnalogChannels", lstAnalogChannels);
			
//			createRmsAlgorithmAndExport();
		}
		catch (Exception e) {
			logger.error("Exception occured in populateAnalogDTO method",e);
			throw new M9000Exception("Exception in populateAnalogDTO method ",e);
		}

		logger.debug("Returning from populateAnalogDTO method "+lstAnalogChannels.size());
		return SUCCESS;
	}

	public String next()
	{
//		String returnValue = "LineGroups";
		String returnValue = "VirtualChannels";
		if (accessMode == null || !accessMode.equalsIgnoreCase("Edit")) 
		{
			returnValue = "saveAnalogs";
		}
//		createRmsAlgorithmAndExport();
		populateAnalogsIntoXmlConfig();
		
//		if (stationDetails.getSystemDigitalChannelsCount() > 0)
//		{
//			returnValue = "Events";
//		}
//		else
//		{
//			returnValue = "LineGroups";
//		}
		logger.debug("Returning from next() "+returnValue);
		logger.info("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is navigating to virtual channels configuration screen");
		return returnValue;
	}
	
	
	private void populateAnalogsIntoXmlConfig() {
//		TreeSet<String> keys = new TreeSet<String>(mapXMLDfrs.keySet());
//		Iterator<String> mapXmlDfrIterator = keys.iterator();
		Exports exports;
		Export export;
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
		Map<String, AnalogInput[]> mapAnalogInputs = new HashMap<String, AnalogInputDocument.AnalogInput[]>();
		String dfrKey;
		DFR xmlDfr;
		AnalogChannelDTO analogDto;
		for (; mapXmlDfrIterator.hasNext();) {
			dfrKey = mapXmlDfrIterator.next();
			xmlDfr = mapXMLDfrs.get(dfrKey);
			logger.debug("DFR key added "+dfrKey);
			if (xmlDfr.getDataPool().getChannels().getAnalogs() != null)
			{
				mapAnalogInputs.put(dfrKey.trim(), xmlDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray());
			}
		}
//		AnalogInput[] analogInputs = new AnalogInput[dfrObj.getDataPool()
//				.getChannels().getAnalogs().sizeOfAnalogInputArray()];
		AnalogInput[] analogInputs;
		int index = 0;
		String chassis = null;
		String exportName;
			for (int i = 0; i < getLstAnalogChannels().size(); i++) {
				analogDto = getLstAnalogChannels().get(i);
				logger.debug("Analog channel "+(analogDto.getChannel()+"-"+analogDto.getName()));
				if (chassis == null)
				{
					chassis = analogDto.getChassis();
				}
				else
				{
					if(!chassis.equalsIgnoreCase(analogDto.getChassis()))
					{
						index = 0;
						chassis = analogDto.getChassis();
					}
				}
//				logger
//						.info("In populateBackToXmlConfig() analogDto.getName() "
//								+ analogDto.getName()+" Chassis "+analogDto.getChassis());
				analogInputs = mapAnalogInputs.get(analogDto.getChassis().trim());
				analogInputs[index].setCircuitName(analogDto
						.getCircuitName());
//				logger.info("set Circuit name... "
//						+ analogInputs[index].getCircuitName());
				analogInputs[index].setPhase(analogDto
						.getPhase());
//				logger.info("set Phase... " + analogInputs[index].getPhase());
				// START: 23-Jan-2020 - Implementing transducer
				if (analogDto.getInputType()
						.equalsIgnoreCase(M9kConstants.TRANSDUCER_VOLTAGE)
						) {
					analogInputs[index].setInputType(M9kConstants.VOLTAGE_DC);
				}
				else if (analogDto.getInputType()
								.equalsIgnoreCase(
										M9kConstants.TRANSDUCER_CURRENT
						))
				{
					analogInputs[index].setInputType(M9kConstants.CURRENT_DC_EXTERNAL);
				}
				else
				{
					analogInputs[index].setInputType(analogDto
						.getInputType());
				}
				
				if (analogDto.getInputType()
						.equalsIgnoreCase(M9kConstants.CURRENT_AC_EXTERNAL)
						|| analogDto.getInputType()
								.equalsIgnoreCase(
										M9kConstants.CURRENT_DC_EXTERNAL)
								|| analogDto.getInputType()
								.equalsIgnoreCase(
										M9kConstants.TRANSDUCER_CURRENT)
								// START: 29-Jun-2020 - Hall Effect offset correction implementation
								|| analogDto.getInputType()
								.equalsIgnoreCase(
										M9kConstants.CURRENT_AC_HALL_EFFECT_EXTERNAL_SHUNT
								// END: 29-Jun-2020
						)) {
					analogDto.setDisableExtSunt(false);
					analogInputs[index].setExternalShunt(Double.parseDouble(analogDto.getExtShunt()));
				} else {
					analogDto.setDisableExtSunt(true);
					if (analogInputs[index].isSetExternalShunt())
					{
						analogInputs[index].unsetExternalShunt();
					}
				}
				// END: 23-Jan-2020
				
//				logger.info("set Input Type... "
//						+ analogInputs[index].getInputType());
				analogInputs[index].setTransformerPrimary(Integer
						.parseInt(analogDto
								.getPrimaryRatio()));
//				logger.info("set PrimaryRatio... "
//						+ analogInputs[index].getTransformerPrimary());
				analogInputs[index].setTransformerSecondary(Integer
						.parseInt(analogDto
								.getSecondaryRatio()));
//				logger.info("set Secondary Ratio... "
//						+ analogInputs[index].getTransformerSecondary());
				// START: 16-Oct-2019 Full Scale/Range is changed to double
//				analogInputs[index].setRange(Integer
//						.parseInt(analogDto.getRange()));
				analogInputs[index].setRange(Double
						.parseDouble(analogDto.getRange()));
				// END: 16-Oct-2019
//				logger.info("set Range... " + analogInputs[index].getRange());
//				analogInputs[index].setPs(stationDetails.getPs());
				if (analogDto.getStatus())
				{
					analogInputs[index].setExport(M9kConstants.ENABLE_ONE);
				}
				else
				{
					analogInputs[index].setExport(M9kConstants.DISABLE_ZERO);
				}

				// START: 23-Jan-2020 - Implementing transducer
				if (analogDto.getInputType()
						.equalsIgnoreCase(M9kConstants.TRANSDUCER_VOLTAGE)
						|| analogDto.getInputType()
								.equalsIgnoreCase(
										M9kConstants.TRANSDUCER_CURRENT
						)) {
					analogInputs[index].setTransducer(M9kConstants.ENABLE_ONE);
					analogInputs[index].setTransducerUnits(analogDto.getTransducerUnits());
					analogInputs[index].setInP1(analogDto.getInP1());
					analogInputs[index].setOutP1(analogDto.getOutP1());
					analogInputs[index].setInP2(analogDto.getInP2());
					analogInputs[index].setOutP2(analogDto.getOutP2());
				}
				else
				{
					if (analogInputs[index].isSetTransducer())
					{
						analogInputs[index].unsetTransducer();
					}
					if (analogInputs[index].isSetTransducerUnits())
					{
						analogInputs[index].unsetTransducerUnits();
					}
					if (analogInputs[index].isSetInP1())
					{
						analogInputs[index].unsetInP1();
					}
					if (analogInputs[index].isSetInP2())
					{
						analogInputs[index].unsetInP2();
					}
					if (analogInputs[index].isSetOutP1())
					{
						analogInputs[index].unsetOutP1();
					}
					if (analogInputs[index].isSetOutP2())
					{
						analogInputs[index].unsetOutP2();
					}
				}
				// END: 23-Jan-2020

				// Update the exports phase as it doesnt get updated if phase of analog channel changes
				xmlDfr = mapXMLDfrs.get(analogDto.getChassis());
				exports = xmlDfr.getDataPool().getExports();
				if (exports != null)
				{
					exportName = analogDto.getChannel()+"-"+analogDto.getName();
					export = getExportsIfAlreadyExists(exports, exportName);
					logger.debug("exportName to look for "+exportName);
					if (export != null)
					{
	//					logger.debug("Export already exists? "+export);
						export.setPhase(analogDto.getPhase());
						export.setSampleRate(stationDetails.getExportRate());
					}
					exportName = "RMS-"+exportName;
					logger.debug("exportName to look for "+exportName);
					export = getExportsIfAlreadyExists(exports, exportName);
					logger.debug("");
					if (export != null)
					{
	//					logger.debug("Export already exists? "+export);
						export.setPhase(analogDto.getPhase());
						export.setSampleRate(stationDetails.getExportRate());
					}
					
				}

				//				logger.info("set PS... " + stationDetails.getPs());
//				logger.info("in the loop...index " + index);
				index++;
			}

			logger.debug("Returning from populateBackToXmlConfig");

	}


	@SuppressWarnings("unused")
	private void createRmsAlgorithmAndExport()
	{
		logger.debug("In createRmsAlgorithmAndExport to create default RMS and exports for long term rms ");
		DFR xmlDfr = null;
		AnalogChannelDTO analogChannelDTO;
		int expId;
		Exports exports;
		Export export;
		RMSAlgorithm rmsAlgorithm;
		Rms rms;
		Algorithms algorithms ;
		boolean isAlreadyExported = false;
		for (int i = 0; i < getLstAnalogChannels().size(); i++) {
			analogChannelDTO = getLstAnalogChannels().get(i);
			xmlDfr = mapXMLDfrs.get(analogChannelDTO.getChassis());
			rmsAlgorithm = new RMSAlgorithm(xmlDfr, analogChannelDTO);
			rms = rmsAlgorithm.createRmsXmlObj();
//			logger.debug("Rms "+rms.getName()+" Input "+rms.getInput());
			algorithms = xmlDfr.getDataPool().getAlgorithms();
			if (algorithms == null)
			{
				algorithms = xmlDfr.getDataPool().addNewAlgorithms();
				algorithms.setName("Algorithms");
			}
			exports = xmlDfr.getDataPool().getExports();
			String exportName = "RMS-"+analogChannelDTO.getChannel()+"-"+analogChannelDTO.getName();
			if (exports == null)
			{
					exports = xmlDfr.getDataPool().addNewExports();
					exports.setName("Exports");
			}
			isAlreadyExported = isAlreadyExported(exports, exportName.substring(4)); // Compare without RMS- prefix in the export name
			if (isAlreadyExported )
			{
				removeDefaultExportsIfExists(exports, exportName); // Backward compatibility. Remove from already created old configs 
				continue;
			}
			export = getExportsIfAlreadyExists(exports, exportName);
//			logger.debug("Export already exists? "+export);
			if (export == null)
			{
				export = exports.addNewExport();
				export.setName(exportName);
				expId = M9kConstants.LTR_RMS_OFFSET+Integer.parseInt(analogChannelDTO.getChannel());
				export.setId(expId);
			}
			// Start 14-Oct-2013 Introduced exportName attribute to Export in xml
			export.setExportName(analogChannelDTO.getDisplayChannel()+"-"+analogChannelDTO.getCircuitName());
			// End 14-Oct-2013
			
			// Start 08-Jul-2015 Added Measurement type to xml to be added in hte cont table
			export.setMeasurementType(M9kConstants.RMS);
			// End 08-Jul-2015
			
			export.setPhase(analogChannelDTO.getPhase());
//			export.setUnits("V");

			if (analogChannelDTO.getInputType().startsWith(M9kConstants.VOLTAGE))
			{
				export.setUnits("V");
			}
			else
			{
				export.setUnits("A");
			}
			export.setInput(getExportXMLInputString(rms.getName()));
			export.setSampleRate(stationDetails.getExportRate());
		}
//		logger.debug("XML DFR after implicit exports "+xmlDfr.xmlText());
//		algorithms.addNewRms();
//		algorithms.setRmsArray((algorithms.getRmsArray().length-1), rms);
		
	}

	private Export getExportsIfAlreadyExists(Exports exports, String exportName)
	{
//		logger.debug("Export Name to be compared "+exportName);
		Export export = null;
		Export[] arrExport = exports.getExportArray();
		for (int i = 0; i < arrExport.length; i++) {
//			logger.debug("arrExport[i].getName() "+arrExport[i].getName());
			if (arrExport[i].getName().equalsIgnoreCase(exportName))
			{
				export = arrExport[i];
				break;
			}
		}
		return export;
	}
	private void removeDefaultExportsIfExists(Exports exports, String exportName)
	{
//		logger.debug("Export Name to be compared "+exportName);
		Export[] arrExport = exports.getExportArray();
		for (int i = 0; i < arrExport.length; i++) {
//			logger.debug("arrExport[i].getName() "+arrExport[i].getName());
			if (arrExport[i].getName().equalsIgnoreCase(exportName))
			{
				exports.removeExport(i);
				break;
			}
		}
	}
	private boolean isAlreadyExported(Exports exports, String exportName)
	{
//		logger.debug("Export Name to be compared "+exportName);
		boolean isExported = false;
		Export[] arrExport = exports.getExportArray();
		for (int i = 0; i < arrExport.length; i++) {
//			logger.debug("arrExport[i].getName() "+arrExport[i].getName());
			if (arrExport[i].getName().equalsIgnoreCase(exportName)) // Compare without RMS- prefix in the name
			{
				isExported = true;
				break;
			}
		}
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

	  @SkipValidation
	  public String navigateToEvents()
	  {
		  return "Events";
	  }

	  
	  /**
	   * Returns the phase types based on the input type. Set the input type and it should automatically update he phase list in DTO
	   */
	  @SkipValidation
	  public String populatePhaseTypes()
	  {
		  logger.debug("selected Index "+getSelectedIndex()+" is the analogs channels list null? "+getLstAnalogChannels());
		  logger.debug("Phase types "+getLstAnalogChannels().get(getSelectedChannelIndex()).getMapPhaseTypes());
		  return SUCCESS;
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
		logger.debug("ConfigureAnalogChannelsAction: Invoking validate method....");
		int iAnalogCnt = 0;
		for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels().iterator(); iterator.hasNext();) {
			AnalogChannelDTO analogChannelDTO = iterator.next();
			if (analogChannelDTO.getCircuitName() == null || analogChannelDTO.getCircuitName().trim().isEmpty())
			{
				addFieldError("lstAnalogChannels["+iAnalogCnt+"].circuitName", "Channel Description is Required");
			}

// START: Saravanan 11-Sep-2023 Modified below code to allow special characters expect comma as it affect comtrade config			
//			// Saravanan 07-Mar-2013 Validate for special characters
//			else if (analogChannelDTO.getCircuitName().indexOf(",") != -1 || analogChannelDTO.getCircuitName().indexOf("/") != -1 
//					|| analogChannelDTO.getCircuitName().indexOf("[") != -1 || analogChannelDTO.getCircuitName().indexOf("]") != -1)
			else if (analogChannelDTO.getCircuitName().indexOf(",") != -1)
			{
				addFieldError("lstAnalogChannels["+iAnalogCnt+"].circuitName", "Channel Description contains invalid character: , ");
			}
// END: Saravanan 11-Sep-2023			
//			else if (analogChannelDTO.getCircuitName().indexOf("-1") != -1)
//			{
//				analogChannelDTO.setCircuitName(analogChannelDTO.getCircuitName().replaceAll("-1", " - 1"));
////				addFieldError("lstAnalogChannels["+iAnalogCnt+"].circuitName", "Channel Description contains invalid character: / ");
//				
//			}
			
			// Validate primary ratio
			if (analogChannelDTO.getPrimaryRatio() == null || analogChannelDTO.getPrimaryRatio().trim().isEmpty())
			{
				addFieldError("lstAnalogChannels["+iAnalogCnt+"].primaryRatio", "Primary Ratio is Required");
			}
			else if (!M9kUtils.isInteger(analogChannelDTO.getPrimaryRatio()))
			{
				addFieldError("lstAnalogChannels["+iAnalogCnt+"].primaryRatio", "Primary Ratio should be numeric");
			}
			else if (Integer.parseInt(analogChannelDTO.getPrimaryRatio()) < 1)
			{
				addFieldError("lstAnalogChannels["+iAnalogCnt+"].primaryRatio", "Primary Ratio should be greater than zero");
			}
			
			// Secondary ratio
			if (analogChannelDTO.getSecondaryRatio() == null || analogChannelDTO.getSecondaryRatio().trim().isEmpty())
			{
				addFieldError("lstAnalogChannels["+iAnalogCnt+"].secondaryRatio", "Secondary Ratio is Required");
			}
			else if (!M9kUtils.isInteger(analogChannelDTO.getSecondaryRatio()))
			{
				addFieldError("lstAnalogChannels["+iAnalogCnt+"].secondaryRatio", "Secondary Ratio should be numeric");
			}
			else if (Integer.parseInt(analogChannelDTO.getSecondaryRatio()) < 1)
			{
				addFieldError("lstAnalogChannels["+iAnalogCnt+"].secondaryRatio", "Secondary Ratio should be greater than zero");
			}
			
			//TODO: Full scale - Need to be a drop down
			if (analogChannelDTO.getRange() == null || analogChannelDTO.getRange().trim().isEmpty())
			{
				addFieldError("lstAnalogChannels["+iAnalogCnt+"].range", "Full Scale/Range is Required");
			}
			// START: 16-Oct-2019 - Full Scale is changed from integer to Double
			else if (!M9kUtils.isDouble(analogChannelDTO.getRange()))
			{
				addFieldError("lstAnalogChannels["+iAnalogCnt+"].range", "Full Scale/Range should be numeric");
			}
			else if (Double.parseDouble(analogChannelDTO.getRange()) < 0)
			{
				addFieldError("lstAnalogChannels["+iAnalogCnt+"].range", "Full Scale/Range should be greater than zero");
			}
			// END: 16-Oct-2019 - Full Scale is changed from integer to Double
			
//			logger.debug("Input type "+analogChannelDTO.getInputType());
			if (analogChannelDTO.getInputType().toUpperCase().indexOf("EXTERNAL") != -1)
			{
				if (analogChannelDTO.getExtShunt() == null || analogChannelDTO.getExtShunt().trim().isEmpty())
				{
					addFieldError("lstAnalogChannels["+iAnalogCnt+"].extShunt", "External Shunt is Required");
				}
				else if (!M9kUtils.isDouble(analogChannelDTO.getExtShunt()))
				{
					addFieldError("lstAnalogChannels["+iAnalogCnt+"].extShunt", "External Shunt should be numeric");
				}
				else if (Double.parseDouble(analogChannelDTO.getExtShunt()) <= 0.0)
				{
					addFieldError("lstAnalogChannels["+iAnalogCnt+"].extShunt", "External Shunt should be greater than zero");
				}
			}
			if (analogChannelDTO.isTransducer() || analogChannelDTO.getInputType().toUpperCase().indexOf("TRANSDUCER") != -1)
			{
				if (analogChannelDTO.getTransducerUnits() == null || analogChannelDTO.getTransducerUnits().trim().isEmpty())
				{
					addFieldError("lstAnalogChannels["+iAnalogCnt+"].transducerUnits", "Transducer Units is Required");
					logger.debug("Adding field error to "+"lstAnalogChannels["+iAnalogCnt+"].transducerUnits");
				}
				// START: 12-Sep-2023 - Allow all special characters except comma
				//				if (analogChannelDTO.getTransducerUnits().indexOf(",") != -1 || analogChannelDTO.getTransducerUnits().indexOf("/") != -1 || analogChannelDTO.getTransducerUnits().indexOf("[") != -1 || analogChannelDTO.getTransducerUnits().indexOf("]") != -1)
				if (analogChannelDTO.getTransducerUnits().indexOf(",") != -1 )
				{
//					addFieldError("lstAnalogChannels["+iAnalogCnt+"].transducerUnits", "Special Characters not allowed: / [ ] , ");
					addFieldError("lstAnalogChannels["+iAnalogCnt+"].transducerUnits", "Contains invlid character , ");
				}
				// END: 12-Sep-2023
				
				if (analogChannelDTO.getOutP1() == analogChannelDTO.getOutP2())
				{
					addFieldError("lstAnalogChannels["+iAnalogCnt+"].outP1", "In1 cannot be same as In2");
				}
				if (analogChannelDTO.getInP1() == analogChannelDTO.getInP2())
				{
					addFieldError("lstAnalogChannels["+iAnalogCnt+"].inP1", "Out1 cannot be same as Out2");
				}

			}
			iAnalogCnt++;
		}
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
	 * @return the selectedChannelIndex
	 */
	public int getSelectedChannelIndex() {
		return selectedChannelIndex;
	}

	/**
	 * @param selectedChannelIndex the selectedChannelIndex to set
	 */
	public void setSelectedChannelIndex(int selectedChannelIndex) {
		this.selectedChannelIndex = selectedChannelIndex;
	}
}
