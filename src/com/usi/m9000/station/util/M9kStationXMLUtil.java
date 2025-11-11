package com.usi.m9000.station.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;

import com.usi.AlarmDocument.Alarm;
import com.usi.AlarmsDocument.Alarms;
import com.usi.AnalogChannelsDocument.AnalogChannels;
import com.usi.AnalogInputDocument.AnalogInput;
import com.usi.AnalogsDocument.Analogs;
import com.usi.DFRDocument.DFR;
import com.usi.EventsDocument.Events;
import com.usi.ExportDocument2.Export;
import com.usi.GlobalLineGroupDocument.GlobalLineGroup;
import com.usi.GlobalLineGroupsDocument.GlobalLineGroups;
import com.usi.LineGroupDocument.LineGroup;
import com.usi.MeasurementDocument.Measurement;
import com.usi.StationPropertiesDocument.StationProperties;
import com.usi.SubStationDocument.SubStation;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.DigitalInfo;
import com.usi.m9000.dao.MySqlStationDAO;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.EventChannelDTO;
import com.usi.m9000.dto.LineGroupChannelsDTO;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kXMLConstants;
import com.usi.m9000.xml.M9000XmlConfig;

public class M9kStationXMLUtil {
	private static boolean configUnavailableReported = false;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationXMLUtil.class);
	private static List<AnalogChannelDTO> lstAnalogChannels;
	private static MySqlStationDAO mySqlStationDAO = null;
	static String stationConfigXml;
	private static SubStation subStation;
	private static com.usi.SystemDocument.System system;
	private static DFR[] xmlConfigDfrs = null;
	private static M9000XmlConfig m9kConfig = null;
	private static List<LineGroupsAlgorithm> lstLineGroups;
	private static  Map<Integer, Map<String,Integer>> mapDfrsRmsExport = null;
	public static boolean booXmlInit;
	private static List<DfrDTO> lstCompleteDfrsFromStationXML = null;
	private static List<DfrDTO> lstDfrsWithMinimalInfo = null;
	private static Integer monitorWettingVoltage = 1; // Default enable = 1; For disable it is 0
	private static Map<Integer, Measurement> mapMeasurements = null;
	// 26-May-2022 - Relay mapping info from config xml
	private static Map<LedName, RelayName> mapLedToRelay = null;
	private static Map<RelayName, List<LedName>> mapRelayToLeds = null;
	// Duration of the relays to be set during the trigger event
	private static int triggerDuration = 10;
	
	// START: 31-Aug-2022 - Moving number of days to retain fro cont data to GUI 
	private static int contOscDaysToRetain = 5;
	private static int contMeasurementsDaysToRetain = 30;
	// END: 31-Aug-2022
	
	// START: 09-Jan-2023 - Disturbance alarm implementation
	private static Map<String, Measurement> mapDisturbanceAlarms = null; 
	// END: 09-Jan-2023 - Disturbance alarm implementation
	// START: 09-Jan-2023 - Export time limit for continuous data is made configurable
	private static int contOscExportTimeLimit = 600;
	private static int contMeasurementsExportTimeLimit = 120;
	// END: 09-Jan-2023 - Export time limit for continuous data is made configurable
	// START: 11-Jun-2024 - Enable or Disable event test for incompatible old boards
	private static boolean enableEventTest = true;
	// END: 11-Jun-2024
	// START: 18-Jun-2024 - Set DDR deck time in seconds to be configurable
	private static int ddrDeckTimeLimit = 300; // In Seconds
	// End: 18-Jun-2024	
//	static
//	{
//		logger.debug("About ot initialize XML utility");
//		booXmlInit = initXml(null);
//	}
	public static boolean initXml(String configXml)
	{
		boolean init = true;
		if (configXml == null)
		{
			mySqlStationDAO = new MySqlStationDAO();
			try {
				stationConfigXml = mySqlStationDAO.getLocalConfigXml();
				// START: 06-Feb-2020 - Wait if no config is avaialble
				if (stationConfigXml == null)
				{
					if (configUnavailableReported )
					{
						configUnavailableReported = true;
						logger.info("No configuration available. It will wait until the configuration is available.");
					}
				}
				else
				{
					configUnavailableReported = false;
				}
				// END: 06-Feb-2020
			} catch (M9000Exception e) {
				logger.error("Config XML is not read. "+e);
				stationConfigXml = null;
			}
		}
		else
		{
			stationConfigXml = configXml;
		}
		if (stationConfigXml != null && !stationConfigXml.isEmpty())
		{
			m9kConfig = new M9000XmlConfig();
				try {
					m9kConfig.loadStationXmlFile(new StringReader(stationConfigXml));
					subStation = m9kConfig.getSubstation();
					system = null;
					populateAnalogChannels();
					populateLineGroups();
					mapDfrsRmsExport = null;
					mapMeasurements = null;
					setRelayMappings(null);
					mapDisturbanceAlarms = null;
				} catch (M9000Exception e) {
					e.printStackTrace();
					logger.warn(e.toString());
					init = false;
				}
		}
		else
		{
			init = false;
		}
//		logger.debug("int flag "+init +" M9config "+m9kConfig);
		booXmlInit = init;
		return init;
	}
	
	public static com.usi.SystemDocument.System getSystem()
	{
		if (system == null)
		{
			system = subStation.getDFRs().getDFRArray(0).getSystem();
		}
		
		return system;
	}
	
	private static DFR[] getXmlConfigDfr()
	{
		xmlConfigDfrs = subStation.getDFRs().getDFRArray();
		return xmlConfigDfrs;
	}
	
	public static int getExportAnalogBufferTime()
	{
		return getSystem().getExportAnalogBufferTime();
	}
	
	public static int getExportBufferTime()
	{
		return getSystem().getExportBufferTime();
	}
	
	private static List<AnalogChannelDTO> populateAnalogChannels() {

		int iOffset = 0;
		logger.debug("Total dfrs "+getXmlConfigDfr().length);
		lstAnalogChannels = new ArrayList<AnalogChannelDTO>();
		for (int i = 0; i < getXmlConfigDfr().length; i++) {
			if (getXmlConfigDfr()[i].getDataPool().getChannels().getAnalogs() == null || getXmlConfigDfr()[i].getDataPool().getChannels().getAnalogs().sizeOfAnalogInputArray() == 0)
			{
				continue;
			}
			AnalogInput[] analogInputs = getXmlConfigDfr()[i].getDataPool().getChannels()
			.getAnalogs().getAnalogInputArray();
			AnalogChannelDTO analogDto;
//			logger.debug("populateAnalogDTO: DFR name: " + getXmlConfigDfr()[i].getSystem().getDfrId());
//			logger.debug("Analogs channel count "+analogInputs.length);
			for (int j = 0; j < analogInputs.length; j++) {
				analogDto = new AnalogChannelDTO();
				analogDto.setName(analogInputs[j].getName());
				analogDto.setChannel("" + (analogInputs[j].getChannel()+iOffset));
				analogDto.setCircuitName(analogInputs[j].getCircuitName());
				analogDto.setPhase(analogInputs[j].getPhase());
				analogDto.setInputType(analogInputs[j].getInputType());
//				logger.debug("Export status in xml "+analogInputs[j].getExport());
				if (analogInputs[j].getExport() == M9kConstants.ENABLE_ONE)
				{
					analogDto.setExportStatus(true);
				}
				else
				{
					analogDto.setExportStatus(false);
				}
				
				analogDto.setRange("" + analogInputs[j].getRange());
				analogDto.setPrimaryRatio(""
						+ analogInputs[j].getTransformerPrimary());
				analogDto.setSecondaryRatio(""
						+ analogInputs[j].getTransformerSecondary());
				analogDto.setExtShunt(""+analogInputs[j].getExternalShunt());
				analogDto.setChassis("DFR"+getXmlConfigDfr()[i].getSystem().getDfrId());
//				logger.debug("analog dto added "+analogDto);
				lstAnalogChannels.add(analogDto);
			}
			iOffset+=analogInputs.length;
		}

		return lstAnalogChannels;

	}
	
	public static List<DfrDTO> getDFRsFromStationXML() throws Exception
	{
		  int nextStartAnalogIndex = -1;
		  int nextStartDigitalIndex = -1;
		  int physicalAnalogsCount = 0;
		  int analogCnt;
		  int digitalCnt;
		  Analogs analogChannels;
		  Events eventChannels;
//		logger.info("Substation..."+substation);
		  lstCompleteDfrsFromStationXML = new ArrayList<DfrDTO>(subStation.getDFRs().sizeOfDFRArray());
		DfrDTO dfrDto;
		DFR[] xmlDfrs = getXmlConfigDfr();
		List<AnalogChannelDTO> lstVirtualInputChannels;
		// START: 17-Aug-2020 - LTR Analog dummy data index bug was fixed - Analogs offset logic updated
		int start = 1;
		int dfrChnlsStart = 1;
		// END 09-Oct-2020
		
		// START: 09-Oct-2020 - Set transformer ratios to 1 for virtual channels
		int analogPrimaryRatio = 1;
		int analogSecondaryRatio = 1;
		// END: 09-Oct-2020
		for (int i = 0; i < xmlDfrs.length; i++) {

			dfrDto = new DfrDTO("DFR"+xmlDfrs[i].getSystem().getDfrId());
//			logger.info("Name: "+dfrDto.getDfrName());
			dfrDto.setDfrId(xmlDfrs[i].getSystem().getDfrId());
			dfrDto.setIpAddress(xmlDfrs[i].getIPAddress());
			dfrDto.setLineFreq(xmlDfrs[i].getSystem().getLineFrequency());
			dfrDto.setSampleRate(xmlDfrs[i].getSystem().getSampleRate());
//			logger.info("IPAddress: "+dfrDto.getIpAddress());
	    	analogChannels = xmlDfrs[i].getDataPool().getChannels().getAnalogs();
			if (analogChannels != null)
			{
		    	dfrDto.setLstAnalogChannelNames(new ArrayList<String>(analogChannels.sizeOfAnalogInputArray()));
		    	dfrDto.setLstAnalogChannels(new ArrayList<AnalogChannelDTO>(analogChannels.sizeOfAnalogInputArray()));
		    	// START: 17-Aug-2020 - LTR Analog dummy data index bug was fixed - Analogs offset logic updated
//		    	int start = dfrDto.getAnalogChannelStart();
		    	// END: 17-Aug-2020
		    	AnalogChannelDTO analogDto;
		    	physicalAnalogsCount = 0;
		    	logger.debug("LTR: dfrChnlsStart "+dfrChnlsStart);
		    	for (int j = 0; j < analogChannels.sizeOfAnalogInputArray(); j++) {
		    		if (analogChannels.getAnalogInputArray(j).isSetVirtual() && analogChannels.getAnalogInputArray(j).getVirtual() == 1)
		    		{
		    			// START: 17-Aug-2020 - LTR Analog dummy data index bug was fixed - Analogs offset logic updated
		    			dfrDto.getLstAnalogChannelNames().add(M9kStationConstants.VIRTUAL_PREFIX+(start)+"-"+analogChannels.getAnalogInputArray(j).getCircuitName());
		    			// END: 17-Aug-2020
		    			// START: 09-Oct-2020 - Set transformer ratios to 1 for virtual channels
		    			analogPrimaryRatio = 1;
		    			analogSecondaryRatio = 1;
		    			// END:09-Oct-2020
		    		}
		    		else
		    		{
		    			physicalAnalogsCount++;
		    			// START: 17-Aug-2020 - LTR Analog dummy data index bug was fixed - Analogs offset logic updated
		    			dfrDto.getLstAnalogChannelNames().add("A"+(start)+"-"+analogChannels.getAnalogInputArray(j).getCircuitName());
		    			// END: 17-Aug-2020
		    			// START: 09-Oct-2020 - Set transformer ratios to 1 for virtual channels
		    			analogPrimaryRatio = analogChannels.getAnalogInputArray(j).getTransformerPrimary();
		    			analogSecondaryRatio = analogChannels.getAnalogInputArray(j).getTransformerSecondary();
		    			// END:09-Oct-2020
		    		}

					analogDto = new AnalogChannelDTO();
					analogDto.setChassis(dfrDto.getDfrName());
					analogDto.setName(analogChannels.getAnalogInputArray(j).getName());
					analogDto.setPhase(analogChannels.getAnalogInputArray(j).getPhase());
					analogDto.setRange(""+analogChannels.getAnalogInputArray(j).getRange());
					// START: 09-OCT-2020 - Set transformer ratios to 1 for virtual channels
					analogDto.setPrimaryRatio(""+analogPrimaryRatio);
					analogDto.setSecondaryRatio(""+analogSecondaryRatio);
					// END: 09-OCT-2020
					analogDto.setInputType(analogChannels.getAnalogInputArray(j).getInputType());
					// START: 17-Aug-2020 - LTR Analog dummy data index bug was fixed - Analogs offset logic updated
					logger.debug("LTR-DUMMY-DEBUG: analogChannels.getAnalogInputArray(j).getChannel() "+analogChannels.getAnalogInputArray(j).getChannel()+" primary "+analogChannels.getAnalogInputArray(j).getTransformerPrimary()+" secondary "+analogChannels.getAnalogInputArray(j).getTransformerSecondary());
					logger.debug("LTR-DUMMY-DEBUG: start "+start);
					analogDto.setChannel("" + (analogChannels.getAnalogInputArray(j).getChannel()+dfrChnlsStart-1));
					start++;
					logger.debug("LTR-DUMMY-DEBUG: analog channel index "+analogDto.getChannel());
					// END: 17-Aug-2020
					analogDto.setCircuitName(analogChannels.getAnalogInputArray(j).getCircuitName());
					if (analogChannels.getAnalogInputArray(j).isSetVirtual() && analogChannels.getAnalogInputArray(j).getVirtual() == 1)
					{
						analogDto.setVirtual(true);
			    		lstVirtualInputChannels = new ArrayList<AnalogChannelDTO>();
						analogDto.setVirtualChannelNo(Integer.parseInt(analogChannels.getAnalogInputArray(j).getName().substring(analogChannels.getAnalogInputArray(j).getName().indexOf("VirtualAnalog")+"VirtualAnalog".length())));
						// START: 15-Mar-2018 Subtraction implementation for release v1.0.8.2 
						if (analogChannels.getAnalogInputArray(j).getInputArray() != null && analogChannels.getAnalogInputArray(j).getInputArray().length > 0)
						{
							lstVirtualInputChannels = getAnalogChannelsForNames("DFR"+xmlDfrs[i].getSystem().getDfrId(), analogChannels.getAnalogInputArray(j).getInputArray(), "+");
						}
						if (analogChannels.getAnalogInputArray(j).getInputAddArray() != null && analogChannels.getAnalogInputArray(j).getInputAddArray().length > 0)
						{
							lstVirtualInputChannels = getAnalogChannelsForNames("DFR"+xmlDfrs[i].getSystem().getDfrId(), analogChannels.getAnalogInputArray(j).getInputAddArray(), "+");
						}
						if (analogChannels.getAnalogInputArray(j).getInputSubArray() != null && analogChannels.getAnalogInputArray(j).getInputSubArray().length > 0)
						{
							lstVirtualInputChannels = getAnalogChannelsForNames("DFR"+xmlDfrs[i].getSystem().getDfrId(), analogChannels.getAnalogInputArray(j).getInputSubArray(), "-");
						}
						// END: 15-Mar-2018 Subtraction implementation for release v1.0.8.2

						logger.debug("List of virtual input chnls "+lstVirtualInputChannels);
						analogDto.setLstAnalogsForVirtual(lstVirtualInputChannels);

					}
					
//					logger.debug("Export status in xml "+analogChannels.getAnalogInputArray(j).getExport());
					if (analogChannels.getAnalogInputArray(j).getExport() == M9kConstants.ENABLE_ONE)
					{
						analogDto.setExportStatus(true);
					}
					else
					{
						analogDto.setExportStatus(false);
					}
					
					// For Transducer
					if (analogChannels.getAnalogInputArray(j).isSetTransducer() && analogChannels.getAnalogInputArray(j).getTransducer() == M9kConstants.ENABLE_ONE)
					{
						logger.debug("analogInput xml; "+analogChannels.getAnalogInputArray(j));
						analogDto.setTransducer(true);
						analogDto.setTransducerUnits(analogChannels.getAnalogInputArray(j).getTransducerUnits());
						analogDto.setInP1(analogChannels.getAnalogInputArray(j).getInP1());
						analogDto.setInP2(analogChannels.getAnalogInputArray(j).getInP2());
						analogDto.setOutP1(analogChannels.getAnalogInputArray(j).getOutP1());
						analogDto.setOutP2(analogChannels.getAnalogInputArray(j).getOutP2());
						logger.debug("InP1 and Inp2 from XML P1-> "+analogChannels.getAnalogInputArray(j).getInP1()+" P2 -> "+analogChannels.getAnalogInputArray(j).getInP1());
						logger.debug("InP1 and Inp2 from analogDTO P1-> "+analogDto.getInP1()+" P2 -> "+analogDto.getInP1());
						
					}
					else
					{
						analogDto.setTransducer(false);
					}
					
					dfrDto.getLstAnalogChannels().add(analogDto);
						    		
				}
		    	dfrDto.setPhysicalAnalogsCount(physicalAnalogsCount);
				analogCnt = analogChannels.sizeOfAnalogInputArray();
				dfrDto.setAnalogChnlCnt(analogCnt);
				if (nextStartAnalogIndex == -1)
				{
					dfrDto.setAnalogChannelStart(1);
					dfrDto.setAnalogChannelEnd(physicalAnalogsCount);
					nextStartAnalogIndex = (physicalAnalogsCount)+1;
				}
				else 
				{
					dfrDto.setAnalogChannelStart(nextStartAnalogIndex);
					dfrDto.setAnalogChannelEnd(nextStartAnalogIndex + physicalAnalogsCount -1);
					nextStartAnalogIndex += physicalAnalogsCount;
				}
				// START: 17-Aug-2020 - LTR Analog dummy data index bug was fixed - Analogs offset logic updated
				start = nextStartAnalogIndex; // Start for the next dfr
				dfrChnlsStart = nextStartAnalogIndex;
				// END: 09-Oct-2020

			}
			else
			{
				dfrDto.setAnalogChnlCnt(0);
			}
	    	eventChannels = xmlDfrs[i].getDataPool().getChannels().getEvents();

			if (eventChannels != null)
			{
				digitalCnt = eventChannels.sizeOfEventInputArray();
				dfrDto.setDigitalChnlCnt(digitalCnt);
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
		    	dfrDto.setLstEventChannelNames(new ArrayList<String>(eventChannels.sizeOfEventInputArray()));
		    	// START: 08-Dec-2020 - Get normal status for events for dummy records
		    	dfrDto.setLstEventChannels(new ArrayList<EventChannelDTO>(eventChannels.sizeOfEventInputArray()));
		    	EventChannelDTO eventChannelsDto;
		    	// END: 08-Dec-2020
		    	for (int j = 0; j < eventChannels.sizeOfEventInputArray(); j++) {
		    		dfrDto.getLstEventChannelNames().add(eventChannels.getEventInputArray(j).getEventName());
		    		// START: 08-Dec-2020 - Get normal status for events for dummy records
		    		eventChannelsDto = new EventChannelDTO();
		    		eventChannelsDto.setName(eventChannels.getEventInputArray(j).getName());
		    		eventChannelsDto.setChassis("DFR"+xmlDfrs[i].getId());
		    		eventChannelsDto.setChannel("" + (j+1));
		    		eventChannelsDto.setDescription(eventChannels.getEventInputArray(j).getEventName());
		    		eventChannelsDto.setNormalState(eventChannels.getEventInputArray(j).getNormalState());
		    		dfrDto.getLstEventChannels().add(eventChannelsDto);
		    		// END: 08-Dec-2020
				}		    	

			}
			else
			{
				dfrDto.setDigitalChnlCnt(0);
			}
			logger.debug("Dfrs added to the list..."+dfrDto+" Physical Analog Channels "+dfrDto.getPhysicalAnalogsCount());
//			if (dfrDto.getAnalogChnlCnt() > 0)
//			{
			lstCompleteDfrsFromStationXML.add(dfrDto);
//			}
		}
		return lstCompleteDfrsFromStationXML;
	}

	private static List<AnalogChannelDTO> getAnalogChannelsForNames(String dfrName,
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
	
	public static String getSubstationHostName()
	{
		return subStation.getHost();
	}

	private static List<LineGroupsAlgorithm> populateLineGroups() {
		LineGroupsAlgorithm lineGroups;

		List<AnalogChannelDTO> lgInput;
		String dfrKey= null;
		lstLineGroups = new ArrayList<LineGroupsAlgorithm>();
		DFR[] xmlDfrs = getXmlConfigDfr();
//		System.out.println("Entered M9kStationXMLUtil populateLineGroups xmlDfrs.length "+xmlDfrs.length);
		for (int j = 0; j < xmlDfrs.length; j++) {
			if (xmlDfrs[j].getDataPool().getLineGroups() != null) {
				LineGroup xmlLineGroup[] = xmlDfrs[j].getDataPool()
						.getLineGroups().getLineGroupArray();
//				System.out.println("M9kStationXMLUtil: xmlLineGroup.length "+xmlLineGroup.length);
				for (int i = 0; i < xmlLineGroup.length; i++) {
					lineGroups = new LineGroupsAlgorithm();
					lineGroups.setName(xmlLineGroup[i].getName());
					lineGroups.setId(xmlLineGroup[i].getId());
					lineGroups.setEnableAutoCalc(xmlLineGroup[i].getAutoCalc());
					lineGroups.setPositiveResistance(xmlLineGroup[i].getPositiveResistance());
					lineGroups.setPositiveReactance(xmlLineGroup[i].getPositiveReactance());
					lineGroups.setZeroResistance(xmlLineGroup[i].getZeroResistance());
					lineGroups.setZeroReactance(xmlLineGroup[i].getZeroReactance());
					lineGroups.setLineMiles(xmlLineGroup[i].getLineMiles());
					if (xmlLineGroup[i].getDecisionLogic() != null && !xmlLineGroup[i].getDecisionLogic().trim().equalsIgnoreCase("NONE"))
					{
						lineGroups.setDecisionLogic(xmlLineGroup[i].getDecisionLogic());
					}
					else
					{
						lineGroups.setDecisionLogic("");
					}
					// START: 16-Oct-2019 Implemented a comments section for line group where customer can add custom name value pair (Georgia Power)  
					if (xmlLineGroup[i].getComments() != null && !xmlLineGroup[i].getComments().isEmpty())
					{
						lineGroups.setComments(xmlLineGroup[i].getComments());
					}
					else
					{
						lineGroups.setComments("");
					}
					// END: 16-Oct-2019
					
//					logger.debug("\t\t\t Local line group name "+lineGroups.getName());
					lgInput = getAnalogChannelsForNames("DFR"+xmlDfrs[j].getSystem().getDfrId(), xmlLineGroup[i]
							.getInputArray());
//					type = getLineGroupType(lgInput);
					lineGroups.setInputChannels(lgInput);
//					lineGroups.setType(type);
					lineGroups.setLocal(true);
					lineGroups.setChassis(dfrKey);
//					System.out.println("M9kStationXMLUtil: Line Group Name "+xmlLineGroup[i].getLineGroupName());
					lineGroups.setLineGroupName(xmlLineGroup[i].getLineGroupName());
					lstLineGroups.add(lineGroups);
				}
			}
		}
		  GlobalLineGroups globalLineGroups = subStation.getGlobalLineGroups();
		  
		  if (globalLineGroups != null)
		  {
			  GlobalLineGroup[] arrGlobalLineGroup = globalLineGroups.getGlobalLineGroupArray();
			  if (arrGlobalLineGroup != null && arrGlobalLineGroup.length > 0)
			  {
					List<LineGroupChannelsDTO> lstLineGroupChannelsDTOs;
					LineGroupChannelsDTO lineGroupChannelsDTO;
					AnalogChannels[] analogChannels;
				  for (int i = 0; i < arrGlobalLineGroup.length; i++) {
					  lstLineGroupChannelsDTOs = new ArrayList<LineGroupChannelsDTO>(8);
						lineGroups = new LineGroupsAlgorithm();
						lineGroups.setLocal(false);
						lineGroups.setName(arrGlobalLineGroup[i].getName());
						lineGroups.setLineGroupName(arrGlobalLineGroup[i].getLineGroupName());
						lineGroups.setId(arrGlobalLineGroup[i].getId());
						lineGroups.setEnableAutoCalc(arrGlobalLineGroup[i].getAutoCalc());
						lineGroups.setPositiveResistance(arrGlobalLineGroup[i].getPositiveResistance());
						lineGroups.setPositiveReactance(arrGlobalLineGroup[i].getPositiveReactance());
						lineGroups.setZeroResistance(arrGlobalLineGroup[i].getZeroResistance());
						lineGroups.setZeroReactance(arrGlobalLineGroup[i].getZeroReactance());
						lineGroups.setLineMiles(arrGlobalLineGroup[i].getLineMiles());
						if (!lineGroups.getDecisionLogic().trim().equalsIgnoreCase("NONE"))
						{
							lineGroups.setDecisionLogic(arrGlobalLineGroup[i].getDecisionLogic());
						}
						else
						{
							lineGroups.setDecisionLogic("");
						}
						// START: 16-Oct-2019 Implemented a comments section for line group where customer can add custom name value pair (Georgia Power)  
						if (lineGroups.getComments() != null && !lineGroups.getComments().isEmpty())
						{
							lineGroups.setComments(arrGlobalLineGroup[i].getComments());
						}
						else
						{
							lineGroups.setComments("");
						}
						// END: 16-Oct-2019

						analogChannels = arrGlobalLineGroup[i].getLgAnalogs().getAnalogChannelsArray();
						for (int j = 0; j < analogChannels.length; j++) {
							lineGroupChannelsDTO = new LineGroupChannelsDTO();
							lineGroupChannelsDTO.setName(analogChannels[j].getName());
							lineGroupChannelsDTO.setChannelId(analogChannels[j].getChannelId());
							lineGroupChannelsDTO.setDfrId(analogChannels[j].getRecId());
							lineGroupChannelsDTO.setExportId(analogChannels[j].getExpId());
							lstLineGroupChannelsDTOs.add(lineGroupChannelsDTO);
						}
						lineGroups.setLstLgChannels(lstLineGroupChannelsDTOs);
						lgInput = getAnalogChannelsForLineGroups(lstLineGroupChannelsDTOs);
//						populateRMSExportForLineGroupChannels(lgInput);
//						type = getLineGroupType(lgInput);
						lineGroups.setInputChannels(lgInput);
//						lineGroups.setType(type);
						lineGroups.setLocal(false);
						lstLineGroups.add(lineGroups);					
				  }
//				  logger.debug("Total Line Groups available"+lstLineGroups.size() );
			  }
			  
		  }
		  

		  logger.debug("Returning from  editLineGroups "+lstLineGroups);
		return lstLineGroups;
	}


	private static List<AnalogChannelDTO> getAnalogChannelsForNames(String dfrId,
			String[] lgInputNames) {
		String channelName;
		AnalogChannelDTO analogDto;
		List<AnalogChannelDTO> lgInput = new ArrayList<AnalogChannelDTO>();
		for (int i = 0; i < lgInputNames.length; i++) {
			channelName = lgInputNames[i].substring(
					lgInputNames[i].indexOf("AnalogInput(") + 12,
					lgInputNames[i].lastIndexOf(")"));
			for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels()
					.iterator(); iterator.hasNext();) {
				analogDto = iterator.next();
				if (analogDto.getChassis().equalsIgnoreCase(dfrId) && analogDto.getName().equalsIgnoreCase(channelName)) {
					lgInput.add(analogDto);
//					logger.debug("Added " + analogDto.getName());
					break;
				}
			}
		}
		return lgInput;
	}

	private static List<AnalogChannelDTO> getAnalogChannelsForLineGroups(
			List<LineGroupChannelsDTO> lstLineGroupChannelsDTOs) {
		int channelId;
		AnalogChannelDTO analogDto;
		List<AnalogChannelDTO> lgInput = new ArrayList<AnalogChannelDTO>();
		for (int i = 0; i < lstLineGroupChannelsDTOs.size(); i++) {
			channelId = lstLineGroupChannelsDTOs.get(i).getChannelId();
			for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels()
					.iterator(); iterator.hasNext();) {
				analogDto = iterator.next();
				if (analogDto.getChannel().equalsIgnoreCase(""+channelId)) {
					lgInput.add(analogDto);
					logger.debug("Added " + analogDto.getName());
					break;
				}
			}
		}
		return lgInput;
	}

	public static List<AnalogChannelDTO> getLstAnalogChannels() {
		if (lstAnalogChannels == null)
		{
			lstAnalogChannels = populateAnalogChannels();
		}
		return lstAnalogChannels;
	}

	public static List<LineGroupsAlgorithm> getLstLineGroups() {
		if (lstLineGroups == null)
		{
			lstLineGroups = populateLineGroups();
		}
		return lstLineGroups;
	}

	public static boolean isPrimary()
	{
		boolean booPrimary = false;
		String ps = getSystem().getPs();
		if (ps.trim().equalsIgnoreCase("P"))
		{
			booPrimary = true;
		}
		return booPrimary;
	}
	
	public static boolean isSecondary()
	{
		boolean booSecondary = false;
		String ps = getSystem().getPs();
		if (ps.trim().equalsIgnoreCase("S"))
		{
			booSecondary = true;
		}
		return booSecondary;
	}

	
	public static List<DfrDTO> getListOfDFRs()
	{
		try
		{
			if (lstDfrsWithMinimalInfo == null)
			{
				lstDfrsWithMinimalInfo = new ArrayList<DfrDTO>(subStation.getDFRs().sizeOfDFRArray());
				DfrDTO dfrDto;
				DFR[] xmlDfrs = getXmlConfigDfr();
				for (int i = 0; i < xmlDfrs.length; i++) {
		
					dfrDto = new DfrDTO("DFR"+xmlDfrs[i].getSystem().getDfrId());
					logger.debug("Name: "+dfrDto.getDfrName());
					dfrDto.setDfrId(xmlDfrs[i].getSystem().getDfrId());
					dfrDto.setIpAddress(xmlDfrs[i].getIPAddress());
					dfrDto.setLineFreq(xmlDfrs[i].getSystem().getLineFrequency());
					dfrDto.setSampleRate(xmlDfrs[i].getSystem().getSampleRate());
					logger.debug("IPAddress: "+dfrDto.getIpAddress());
					lstDfrsWithMinimalInfo.add(dfrDto);
				}
			}
		}
		catch (Exception e) {
			logger.error("Unable to get list of dfrs list",e);
			lstDfrsWithMinimalInfo = null;
		}
		return lstDfrsWithMinimalInfo;
	}

	public static Map<Integer, Map<String,Integer>> getMapOfDfrRmsExports()
	{
		if (mapDfrsRmsExport != null && !mapDfrsRmsExport.isEmpty())
		{
			return mapDfrsRmsExport;
		}
		Map<String, Integer> mapRmsExports;
		Export[] arrExport;
		String algorithmType;
		String exportName;
		DFR[] xmlDfrs = getXmlConfigDfr();
		mapDfrsRmsExport = new HashMap<Integer, Map<String,Integer>>(xmlDfrs.length);
		for (int i = 0; i < xmlDfrs.length; i++) {
			if (xmlDfrs[i].getDataPool().getChannels().getAnalogs() == null)
			{
				// All events dfr skip
				continue;
			}
			mapRmsExports = new HashMap<String,Integer>(50);
			arrExport = xmlDfrs[i].getDataPool().getExports().getExportArray();
			for (int j = 0; j < arrExport.length; j++) {
				algorithmType = arrExport[j].getInput().substring(M9kXMLConstants.XML_ALGORITHMS_INPUT.length(), arrExport[j].getInput().indexOf("(",M9kXMLConstants.XML_ALGORITHMS_INPUT.length()));
//				logger.debug("Export "+arrExport[j].getName()+" export input "+arrExport[j].getInput()+" algorith type "+algorithmType);
				exportName = arrExport[j].getName().substring(arrExport[j].getName().lastIndexOf("-")+1);
				if (algorithmType.equalsIgnoreCase(M9kConstants.RMS))
				{
					logger.debug("Export name in rms map "+exportName+" already in map? "+mapRmsExports.get(exportName));
					if (mapRmsExports.get(exportName) == null)
					{
						mapRmsExports.put(exportName, arrExport[j].getId());
					}
				}
			}
			mapDfrsRmsExport.put(xmlDfrs[i].getSystem().getDfrId(), mapRmsExports);
		}
		logger.debug("Lst of rms exports "+mapDfrsRmsExport+" size "+mapDfrsRmsExport.size());
		return mapDfrsRmsExport;
	}
	public static List<Integer> getRmsExportsIdFromConfig()
	{
		List<Integer> lstOfExportIdsFromConfig = new ArrayList<Integer>();
		DFR[] xmlDfrs = getXmlConfigDfr();
		Export[] arrExport;
		String algorithmType;
		for (int i = 0; i < xmlDfrs.length; i++) {
			if (xmlDfrs[i].getDataPool().getChannels().getAnalogs() == null)
			{
				// All events dfr skip
				continue;
			}
			logger.debug("Counting exports in dfr DFR"+xmlDfrs[i].getSystem().getDfrId());
			arrExport = xmlDfrs[i].getDataPool().getExports().getExportArray();
			for (int j = 0; j < arrExport.length; j++) {
				algorithmType = arrExport[j].getInput().substring(M9kXMLConstants.XML_ALGORITHMS_INPUT.length(), arrExport[j].getInput().indexOf("(",M9kXMLConstants.XML_ALGORITHMS_INPUT.length()));
//				logger.debug("Export "+arrExport[j].getName()+" export input "+arrExport[j].getInput()+" algorith type "+algorithmType);
				if (arrExport[j].getName().toUpperCase().startsWith(M9kStationConstants.RMS) || (algorithmType.equalsIgnoreCase(M9kConstants.RMS) && arrExport[j].getEnable() == 1))
				{
					logger.debug(arrExport[j].getId()+" - Export Name "+arrExport[j].getName());
					lstOfExportIdsFromConfig.add(arrExport[j].getId());
				}
			}
		}
		logger.debug("Total no of exports "+lstOfExportIdsFromConfig+" size "+lstOfExportIdsFromConfig.size());
		
		return lstOfExportIdsFromConfig;
	}
	
	public static List<Integer> getExportsIdForDDRFromConfig()
	{
		List<Integer> lstOfExportIdsFromConfig = new ArrayList<Integer>();
		DFR[] xmlDfrs = getXmlConfigDfr();
		Export[] arrExport;
		String algorithmType;
		String ddrStatus = M9kStationConstants.DISABLE;
		for (int i = 0; i < xmlDfrs.length; i++) {
			if (xmlDfrs[i].getDataPool().getChannels().getAnalogs() == null)
			{
				// All events dfr skip
				continue;
			}
			logger.debug("Counting exports in dfr DFR"+xmlDfrs[i].getSystem().getDfrId());
			arrExport = xmlDfrs[i].getDataPool().getExports().getExportArray();
			for (int j = 0; j < arrExport.length; j++) {
				algorithmType = arrExport[j].getInput().substring(M9kXMLConstants.XML_ALGORITHMS_INPUT.length(), arrExport[j].getInput().indexOf("(",M9kXMLConstants.XML_ALGORITHMS_INPUT.length()));
				if (getMapMeasurements() != null && getMapMeasurements().get(arrExport[j].getId()) != null)
				{
					ddrStatus = getMapMeasurements().get(arrExport[j].getId()).getDdrStatus();
				}
				if (arrExport[j].getName().toUpperCase().startsWith(M9kStationConstants.RMS) || 
						(algorithmType.equalsIgnoreCase(M9kConstants.RMS)) || (ddrStatus != null && ddrStatus.equalsIgnoreCase(M9kStationConstants.ENABLE)))
				{
					logger.debug(arrExport[j].getId()+" - Export Name "+arrExport[j].getName());
					lstOfExportIdsFromConfig.add(arrExport[j].getId());
				}
			}
		}
		logger.debug("Total no of exports "+lstOfExportIdsFromConfig+" size "+lstOfExportIdsFromConfig.size());
		
		return lstOfExportIdsFromConfig;
	}
	public static Map<Integer, Measurement> getMapMeasurements()
	{
		if (mapMeasurements == null && subStation.getMeasurements() != null)
		{
			Measurement[] arrMeasurements = subStation.getMeasurements().getMeasurementArray();
			mapMeasurements = new HashMap<Integer, Measurement>(arrMeasurements.length);
			for (int i = 0; i < arrMeasurements.length; i++) {
				mapMeasurements.put(arrMeasurements[i].getId(), arrMeasurements[i]);
			}
		}
		return mapMeasurements;
	}
	
	/**
	 * START: 09-Jan-2023 - Disturbance alarm implementation
	 * @return
	 */
	public static Map<String, Measurement> getMapDisturbanceAlarms()
	{
		if (mapDisturbanceAlarms == null && subStation.getMeasurements() != null)
		{
			Measurement[] arrMeasurements = subStation.getMeasurements().getMeasurementArray();
			mapDisturbanceAlarms = new HashMap<String, Measurement>(arrMeasurements.length);
			logger.debug("Disturbance Alarm: arrMeasurements.length "+arrMeasurements.length);
			for (int i = 0; i < arrMeasurements.length; i++) {
				logger.debug("Disturbance Alarm: arrMeasurements["+i+"].isSetDisturbanceAlarm()? "+arrMeasurements[i].isSetDisturbanceAlarm() +" => "+arrMeasurements[i].getDisturbanceAlarm());
				if (arrMeasurements[i].isSetDisturbanceAlarm() && arrMeasurements[i].getDisturbanceAlarm().equalsIgnoreCase(M9kStationConstants.ENABLE))
				{
					logger.debug("Disturbance Alarm: added "+"T"+arrMeasurements[i].getId());
					mapDisturbanceAlarms.put("T"+arrMeasurements[i].getId(), arrMeasurements[i]);
				}
			}
		}
		logger.debug("Final disturbance alarm map "+mapDisturbanceAlarms);
		return mapDisturbanceAlarms;
	}
	
	public static int getConfigurationSerialNumber()
	{
		return getSystem().getConfigSerialNumber();
	}
	
	public static int getMonitorWettingVoltage(){
		if (getSystem().isSetMonitorWettingVoltage())
		{
			monitorWettingVoltage = getSystem().getMonitorWettingVoltage();
		}
		else
		{
			monitorWettingVoltage = 1;
		}
		return monitorWettingVoltage;
	}
	public static List<String> getMeasurementTypes() throws M9000Exception
	{
		List<String> lstMeasurementTypes = null;
		try {
			M9000XmlConfig m9kConfig = new M9000XmlConfig(new StringReader(stationConfigXml));
			lstMeasurementTypes = m9kConfig.getLstOfExportedMeasurementNames();
		} catch (M9000Exception e) {
			throw e;
		}
		return lstMeasurementTypes;
	}
	
	// Measurement export rate
	public static int getMeasurementSampleRate()
	{
		return getSystem().getExportRate();
	}

	// Analog/Osc export rate
	public static int getOscSampleRate()
	{
		return getSystem().getExportAnalogSampleRate();
	}
	
	public static String getSubstationHealthXmlText(com.usi.health.SubStationDocument.SubStation subStation)
	{
		return subStation.xmlText(getXmlOptions());
	}

	public static String retrieveConfigXmlFromSql(String sqlConfig)
	{
		String configXml = null;
		int beginningIndex = sqlConfig.indexOf("<SubStation>");
		int endingIndex = sqlConfig.indexOf("</SubStation>")+"</SubStation>".length();
		configXml = sqlConfig.substring(beginningIndex, endingIndex);
		return configXml;
	}
	private static XmlOptions getXmlOptions()
	{
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSaveOuter();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setUseDefaultNamespace();
		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("", "http://www.usi.com");
		xmlOptions.setSaveImplicitNamespaces(prefixes);
		return xmlOptions;
	}
	
	/**
	 *  26-May-2022 - Relay mapping info from config xml
	 * @return
	 */
	public static Map<LedName, RelayName> getRelayMappings()
	{
		logger.debug("In getRelayMappings..."+mapLedToRelay);
		if (mapLedToRelay == null)
		{
			mapLedToRelay = new HashMap<LedName, RelayName>(10);
			mapRelayToLeds = null;
			StationProperties stationProperties = subStation.getStationProperties();
			if (stationProperties != null)
			{
				Alarms alarms =subStation.getStationProperties().getAlarms(); 
				if (alarms != null)
				{
					Alarm[] arrAlarm = alarms.getAlarmArray();
					for (int i = 0; i < arrAlarm.length; i++) {
						if (arrAlarm[i].getAlarmName().equalsIgnoreCase(LedName.WETTING_VOLTAGE.name()))
						{
							logger.debug("replacing WETTING_VOLTAGE with "+LedName.DISTURBANCE.name()+" with existing Relay "+arrAlarm[i].getRelay());
							arrAlarm[i].setAlarmName(LedName.DISTURBANCE.name());
//							alarm[i].setRelay(getLstRelays().get(getLstRelays().size()-1));
						}
						else if (arrAlarm[i].getAlarmName().equalsIgnoreCase(LedName.DISTURBANCE.name()+"_ALARM")) // To overcome old name DISTURBANCE_ALARM
						{
							logger.debug("replacing DISTURBANCE_ALARM with "+LedName.DISTURBANCE.name()+" with existing Relay "+arrAlarm[i].getRelay());
							arrAlarm[i].setAlarmName(LedName.DISTURBANCE.name());
//							alarm[i].setRelay(getLstRelays().get(getLstRelays().size()-1));
						}
							mapLedToRelay.put(LedName.valueOf(arrAlarm[i].getAlarmName()), RelayName.valueOf(arrAlarm[i].getRelay()));
					}
				}
			}
			if (mapLedToRelay.isEmpty())
			{
				mapLedToRelay.put(LedName.valueOf("ONLINE"), RelayName.valueOf("RELAY_1"));
				mapLedToRelay.put(LedName.valueOf("TRIGGER"), RelayName.valueOf("RELAY_2"));
				mapLedToRelay.put(LedName.valueOf("CLOCK_SYNC"), RelayName.valueOf("RELAY_3"));
				mapLedToRelay.put(LedName.valueOf("COMMUNICATION"), RelayName.valueOf("RELAY_4"));
				mapLedToRelay.put(LedName.valueOf("POWER"), RelayName.valueOf("RELAY_4"));
				mapLedToRelay.put(LedName.valueOf("DISK"), RelayName.valueOf("RELAY_4"));
				mapLedToRelay.put(LedName.valueOf("TEMPERATURE"), RelayName.valueOf("RELAY_4"));
				mapLedToRelay.put(LedName.valueOf("DISTURBANCE"), RelayName.valueOf("RELAY_4"));
			}
			

		}
		logger.debug("relay mappings "+mapLedToRelay);
		return mapLedToRelay;
	}

	/**
	 * Returns Relay to Leds mapping
	 * @return
	 */
	public static Map<RelayName, List<LedName>> getMapRelayToLeds() {
		if (mapRelayToLeds == null)
		{
			mapRelayToLeds = new HashMap<RelayName, List<LedName>>(10);
			List<RelayName> lstRelayName = new ArrayList<RelayName>(getRelayMappings().values());
			
			RelayName relayName;
			LedName ledName;
			for (Iterator<RelayName> iterator = lstRelayName.iterator(); iterator.hasNext();) {
				relayName = iterator.next();
				if (mapRelayToLeds.get(relayName) == null)
				{
					mapRelayToLeds.put(relayName, new ArrayList<LedName>());
				}
				for (Iterator<LedName> iterator2 = getRelayMappings().keySet().iterator(); iterator2.hasNext();) {
					ledName =  iterator2.next();
					if (getRelayMappings().get(ledName).equals(relayName))
					{
						mapRelayToLeds.get(relayName).add(ledName);
					}
					
				}
			}
		}
		logger.debug("Map relay to Leds "+mapRelayToLeds);
		return mapRelayToLeds;
	}

	public static void setMapRelayToLeds(Map<RelayName, List<LedName>> mapRelayToLeds) {
		M9kStationXMLUtil.mapRelayToLeds = mapRelayToLeds;
	}
	
public static void main(String[] args)
{
	List<AnalogChannelDTO> lstAnalogs =  M9kStationXMLUtil.getLstAnalogChannels();
	for (Iterator<AnalogChannelDTO> iterator = lstAnalogs.iterator(); iterator.hasNext();) {
		AnalogChannelDTO analogChannelDTO = iterator.next();
		System.out.println(analogChannelDTO.getChannel()+" - "+analogChannelDTO.getCircuitName());
		
	}
}

public static int getTriggerDuration() {
	StationProperties stationProperties = subStation.getStationProperties();
	if (stationProperties != null)
	{
		triggerDuration = stationProperties.getTriggerDuration();
	}
	else
	{
		triggerDuration = 10; // Default 10 seconds
	}
	return triggerDuration;
}

/**
 *  Duration of the relays to be set during the trigger event
 * @param triggerDuration
 */
public static void setTriggerDuration(int triggerDuration) {
	M9kStationXMLUtil.triggerDuration = triggerDuration;
}

public static void setRelayMappings(Map<LedName, RelayName> mapLedToRelay) {
	M9kStationXMLUtil.mapLedToRelay = mapLedToRelay;
}

public static int getContOscDaysToRetain() {
	StationProperties stationProperties = subStation.getStationProperties();
	if (stationProperties != null)
	{
		contOscDaysToRetain = stationProperties.getContOscDaysToRetain();
	}
	else
	{
		contOscDaysToRetain = 5; // Default 5 days
	}
	return contOscDaysToRetain;
}

public static void setContOscDaysToRetain(int contOscDaysToRetain) {
	M9kStationXMLUtil.contOscDaysToRetain = contOscDaysToRetain;
}

public static int getContMeasurementsDaysToRetain() {
	StationProperties stationProperties = subStation.getStationProperties();
	if (stationProperties != null)
	{
		contMeasurementsDaysToRetain = stationProperties.getContMeasurementsDaysToRetain();
	}
	else
	{
		contMeasurementsDaysToRetain = 30; // Default 30 days
	}
	return contMeasurementsDaysToRetain;
}

public static void setContMeasurementsDaysToRetain(int contMeasurementsDaysToRetain) {
	M9kStationXMLUtil.contMeasurementsDaysToRetain = contMeasurementsDaysToRetain;
}

public static boolean isDisturbanceOnlyFault(Map<String, DigitalInfo> abnormalEvents) {
	boolean booIsDisturbanceAlarmOnlyFault = true;
	
	try
	{
		logger.debug("isDisturbanceOnlyFault abnormalEvents "+abnormalEvents+"getMapDisturbanceAlarms()  "+getMapDisturbanceAlarms());
		String abnormalId;
		for (Iterator<String> iterator = abnormalEvents.keySet().iterator(); iterator.hasNext();) {
			abnormalId = iterator.next();
//			logger.info("abnormalId Key set "+abnormalId);
			if (getMapDisturbanceAlarms() == null || !getMapDisturbanceAlarms().containsKey(abnormalId))
			{
				booIsDisturbanceAlarmOnlyFault = false;
			}
			else
			{
				if (!getMapDisturbanceAlarms().get(abnormalId).isSetDisturbanceAlarm())
				{
					booIsDisturbanceAlarmOnlyFault = false;
				}
			}
		}
	}
	catch (Exception e) {
		logger.error("Exception occured while checking for disturbance alarm "+e.getMessage());
		booIsDisturbanceAlarmOnlyFault = false;
	}
	logger.debug("Returning Is it disturbanceOnly fault as "+booIsDisturbanceAlarmOnlyFault);
	return booIsDisturbanceAlarmOnlyFault;
	
}

public static boolean isAnyDisturbanceFaultDetected(Map<String, DigitalInfo> abnormalEvents) {
	boolean booIsDisturbanceFault = false;
	
	try
	{
		logger.debug("isDisturbanceOnlyFault abnormalEvents "+abnormalEvents+"getMapDisturbanceAlarms()  "+getMapDisturbanceAlarms());
		String abnormalId;
		for (Iterator<String> iterator = abnormalEvents.keySet().iterator(); iterator.hasNext();) {
			abnormalId = iterator.next();
//			logger.info("abnormalId Key set "+abnormalId);
			if (getMapDisturbanceAlarms() != null && getMapDisturbanceAlarms().containsKey(abnormalId) && getMapDisturbanceAlarms().get(abnormalId).isSetDisturbanceAlarm())
			{
				booIsDisturbanceFault = true;
				break;
			}
		}
	}
	catch (Exception e) {
		logger.error("Exception occured while checking for disturbance alarm "+e.getMessage());
		booIsDisturbanceFault = false;
	}
	logger.debug("Returning Is it disturbanceOnly fault as "+booIsDisturbanceFault);
	return booIsDisturbanceFault;
	
}

/**
 * @return the contOscExportTimeLimit
 */
public static int getContOscExportTimeLimit() {
	StationProperties stationProperties = subStation.getStationProperties();
	if (stationProperties != null)
	{
		contOscExportTimeLimit = stationProperties.getContOscExportTimeLimit();
	}
	else
	{
		contOscExportTimeLimit = 120; // Default 120 seconds / 2 minutes
	}
	return contOscExportTimeLimit;
}

/**
 * @param contOscExportTimeLimit the contOscExportTimeLimit to set
 */
public static void setContOscExportTimeLimit(int contOscExportTimeLimit) {
	M9kStationXMLUtil.contOscExportTimeLimit = contOscExportTimeLimit;
}

/**
 * @return the contMeasurementsExportTimeLimit
 */
public static int getContMeasurementsExportTimeLimit() {
	StationProperties stationProperties = subStation.getStationProperties();
	if (stationProperties != null)
	{
		contMeasurementsExportTimeLimit = stationProperties.getContMeasurementsExportTimeLimit();
	}
	else
	{
		contMeasurementsExportTimeLimit = 600; // Default 600 seconds / 10 minutes
	}
	return contMeasurementsExportTimeLimit;
}

/**
 * @param contMeasurementsExportTimeLimit the contMeasurementsExportTimeLimit to set
 */
public static void setContMeasurementsExportTimeLimit(int contMeasurementsExportTimeLimit) {
	M9kStationXMLUtil.contMeasurementsExportTimeLimit = contMeasurementsExportTimeLimit;
}

public static boolean isEnableEventTest() {
	StationProperties stationProperties = subStation.getStationProperties();
	if (stationProperties != null)
	{
		if (stationProperties.isSetEnableEventTest() && stationProperties.getEnableEventTest() == M9kConstants.DISABLE_ZERO)
		{
			enableEventTest = false;
		}
		else
		{
			enableEventTest = true;
		}
	}
	else
	{
		enableEventTest = true;
	}
	return enableEventTest;
}

public static void setEnableEventTest(boolean enableEventTest) {
	M9kStationXMLUtil.enableEventTest = enableEventTest;
}

public static int getDdrDeckTimeLimit() {
	StationProperties stationProperties = subStation.getStationProperties();
	logger.debug("stationProperties "+stationProperties +" isSetDdrDeckTimeLimit "+stationProperties.isSetDdrDeckTimeLimit());
	if (stationProperties != null && stationProperties.isSetDdrDeckTimeLimit() && stationProperties.getDdrDeckTimeLimit() > 0)
	{
		ddrDeckTimeLimit = stationProperties.getDdrDeckTimeLimit();
	}
	else
	{
		ddrDeckTimeLimit = 300; // Default 300 seconds / 5 minutes
	}
	return ddrDeckTimeLimit;
}

public void setDdrDeckTimeLimit(int ddrDeckTimeLimit) {
	M9kStationXMLUtil.ddrDeckTimeLimit = ddrDeckTimeLimit;
}



}
