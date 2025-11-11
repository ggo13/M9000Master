/**
 * 
 */
package com.usi.m9000.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import com.usi.AlgorithmsDocument.Algorithms;
import com.usi.AnalogChannelsDocument.AnalogChannels;
import com.usi.AnalogInputDocument.AnalogInput;
import com.usi.AverageDocument.Average;
import com.usi.DFRDocument.DFR;
import com.usi.EventInputDocument.EventInput;
import com.usi.ExportDocument2.Export;
import com.usi.ExportsDocument.Exports;
import com.usi.FrequencyDocument.Frequency;
import com.usi.GlobalLineGroupDocument.GlobalLineGroup;
import com.usi.GlobalLineGroupsDocument.GlobalLineGroups;
import com.usi.GroupTriggersDocument.GroupTriggers;
import com.usi.LimitsDocument.Limits;
import com.usi.LineGroupDocument.LineGroup;
import com.usi.LineGroupsDocument.LineGroups;
import com.usi.MagnitudeDocument.Magnitude;
import com.usi.MeasurementDocument.Measurement;
import com.usi.MeasurementsDocument.Measurements;
import com.usi.NegativeSequenceDocument.NegativeSequence;
import com.usi.PdcDocument.Pdc;
import com.usi.PhaseDocument.Phase;
import com.usi.PmuDocument.Pmu;
import com.usi.PmuInputDocument.PmuInput;
import com.usi.PmuInputsDocument.PmuInputs;
import com.usi.PositiveSequenceDocument.PositiveSequence;
import com.usi.PowerDocument.Power;
import com.usi.RmsDocument.Rms;
import com.usi.SubStationDocument;
import com.usi.SubStationDocument.SubStation;
import com.usi.TriggerDocument.Trigger;
import com.usi.TriggersDocument.Triggers;
import com.usi.UdpServersDocument.UdpServers;
import com.usi.ZeroSequenceDocument.ZeroSequence;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.EventChannelDTO;
import com.usi.m9000.dto.LineGroupChannelsDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.TriggerChannelDTO;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.xml.util.M9kXMLUtils;

/**
 * @author sramasamy
 *
 */
public class M9000XmlConfig {

	private XmlOptions xmlOptions;
	private SubStation substation = null;
	private DFR xmlConfigDfr;
//	private String triggerOutHold;
	private StationDTO stationDto;
	private List<AnalogChannelDTO> lstAnalogChannels ;
	private List<EventChannelDTO> lstEventChannels;
	PmuInputs pmuInputs;
	  PmuInput[] pmuInput = null;
	 private boolean lineGroupsConfigured = false;
	private List<DfrDTO> lstDfrDTO;
	private ArrayList<AnalogChannelDTO> lstVirtualChannels;

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9000XmlConfig.class);
	/**
	 * 
	 */
	public M9000XmlConfig() {
		xmlOptions = new XmlOptions();
		xmlOptions.setLoadUseDefaultResolver();
		xmlOptions.setUseDefaultNamespace();
		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("", "http://www.usi.com");
		xmlOptions.setLoadSubstituteNamespaces(prefixes);
		
	}

	/**
	 * @throws M9000Exception 
	 * 
	 */
	public M9000XmlConfig(String xmlFileName) throws M9000Exception {
		this();
		loadXmlFile(xmlFileName);
	}
	
	public M9000XmlConfig(File xmlFileName) throws M9000Exception {
		this();
		loadXmlFile(xmlFileName);
	}
	
	public M9000XmlConfig(InputStream xmlFileStream) throws M9000Exception {
		this();
		loadXmlFile(xmlFileStream);
	}
	
	public M9000XmlConfig(Reader xmlReader) throws M9000Exception {
		this();
		loadXmlFile(xmlReader);
	}

	/**
	 * @param xmlFile the xmlFile to set
	 * @throws M9000Exception 
	 */
	public void loadXmlFile(String xmlFileName) throws M9000Exception {
		try {
			File xmlFile = new File(xmlFileName);
			SubStationDocument substationDoc = SubStationDocument.Factory.parse(xmlFile, xmlOptions);
			substation = substationDoc.getSubStation();
		} catch (XmlException e) {
			e.printStackTrace();
			throw new M9000Exception("XML Parsing failed!", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new M9000Exception("Unable to access XML file!", e);
		}
	}

	public void loadXmlFile(File xmlFile) throws M9000Exception {
		try {
			SubStationDocument substationDoc = SubStationDocument.Factory.parse(xmlFile, xmlOptions);

			substation = substationDoc.getSubStation();
		} catch (XmlException e) {
			e.printStackTrace();
			throw new M9000Exception("XML Parsing failed!", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new M9000Exception("Unable to access XML file!", e);
		}
	}
	
	public void loadXmlFile(InputStream xmlFileStream) throws M9000Exception {
		try {
			SubStationDocument substationDoc = SubStationDocument.Factory.parse(xmlFileStream, xmlOptions);

			substation = substationDoc.getSubStation();
		} catch (XmlException e) {
			e.printStackTrace();
			throw new M9000Exception("XML Parsing failed!", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new M9000Exception("Unable to access XML file!", e);
		}
	}
	
	public void loadXmlFile(Reader xmlReader) throws M9000Exception {
		try {
			SubStationDocument substationDoc = SubStationDocument.Factory.parse(xmlReader, xmlOptions);

			substation = substationDoc.getSubStation();
		} catch (XmlException e) {
			e.printStackTrace();
			throw new M9000Exception("XML Parsing failed!", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new M9000Exception("Unable to access XML file!", e);
		}
	}

	public void loadStationXmlFile(Reader xmlReader) throws M9000Exception {
		try {
			SubStationDocument stationDoc = SubStationDocument.Factory.parse(xmlReader, xmlOptions);

			substation = stationDoc.getSubStation();
		} catch (XmlException e) {
			e.printStackTrace();
			throw new M9000Exception("XML Parsing failed!", e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new M9000Exception("Unable to access XML file!", e);
		}
	}
	
	public SubStation getSubstation() {
		return substation;
	}

	public void setSubstation(SubStation substation) {
		this.substation = substation;
	}
	
	public List<AnalogChannelDTO> populateAnalogChannels() {

		int iOffset = 0;
		DFR[] arrDfrs = getSubstation().getDFRs().getDFRArray();
		logger.debug("Total dfrs "+arrDfrs.length);
		lstAnalogChannels = new ArrayList<AnalogChannelDTO>();
		int virtualChannelsCount = 0;
		for (int i = 0; i < arrDfrs.length; i++) {
			if (arrDfrs[i].getDataPool().getChannels().getAnalogs() == null || arrDfrs[i].getDataPool().getChannels().getAnalogs().sizeOfAnalogInputArray() == 0)
			{
				continue;
			}
			AnalogInput[] analogInputs = arrDfrs[i].getDataPool().getChannels()
			.getAnalogs().getAnalogInputArray();
			AnalogChannelDTO analogDto;
			virtualChannelsCount = 0;
			logger.debug("populateAnalogDTO: DFR name: " + arrDfrs[i].getSystem().getDfrId());
			logger.debug("Analogs channel count "+analogInputs.length);
			for (int j = 0; j < analogInputs.length; j++) {
				analogDto = new AnalogChannelDTO();
				analogDto.setName(analogInputs[j].getName());
				analogDto.setChannel("" + (analogInputs[j].getChannel()+iOffset));
				analogDto.setCircuitName(analogInputs[j].getCircuitName());
				analogDto.setPhase(analogInputs[j].getPhase());
				analogDto.setInputType(analogInputs[j].getInputType());
				logger.debug("Export status in xml "+analogInputs[j].getExport());
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
				analogDto.setChassis("DFR"+arrDfrs[i].getSystem().getDfrId());
				if (analogInputs[j].isSetVirtual() && analogInputs[j].getVirtual() == 1)
				{
					virtualChannelsCount++;
					analogDto.setVirtual(true);
				}
				else
				{
					analogDto.setVirtual(false);
				}
				logger.debug("analog dto added "+analogDto+" Input type "+analogInputs[j].getInputType()+" analogDto input type "+analogDto.getInputType());
				// START: 03-Feb-2020 - Tranducer implementation
				logger.debug("IS transducer set ? "+(analogInputs[j].isSetTransducer() && analogInputs[j].getTransducer() == M9kConstants.ENABLE_ONE));
				if (analogInputs[j].isSetTransducer() && analogInputs[j].getTransducer() == M9kConstants.ENABLE_ONE)
				{
					logger.debug("analogInput xml; "+analogInputs[j]);
					analogDto.setTransducer(true);
					analogDto.setTransducerUnits(analogInputs[j].getTransducerUnits());
					analogDto.setInP1(analogInputs[j].getInP1());
					analogDto.setInP2(analogInputs[j].getInP2());
					analogDto.setOutP1(analogInputs[j].getOutP1());
					analogDto.setOutP2(analogInputs[j].getOutP2());
					logger.debug("InP1 and Inp2 from XML P1-> "+analogInputs[j].getInP1()+" P2 -> "+analogInputs[j].getInP1());
					logger.debug("InP1 and Inp2 from analogDTO P1-> "+analogDto.getInP1()+" P2 -> "+analogDto.getInP1());
					
				}
				else
				{
					analogDto.setTransducer(false);
				}
				// END: 03-Feb-2020
				lstAnalogChannels.add(analogDto);
			}
			iOffset+=(analogInputs.length-virtualChannelsCount);
		}

		return lstAnalogChannels;

	}


	public List<TriggerChannelDTO> getLstOfMeasurements()
	{
		List<TriggerChannelDTO> lstTriggerChannels = new ArrayList<TriggerChannelDTO>();
		DFR[] arrDfrs = getSubstation().getDFRs().getDFRArray();
		  List<DFR> lstXmlDfrs = new ArrayList<DFR> (Arrays.asList(arrDfrs));
//		  logger.debug("lstXmlDfrs before sort "+lstXmlDfrs);
		  Collections.sort(lstXmlDfrs, new Comparator<DFR>() {

				@Override
				public int compare(DFR o1, DFR o2) {
//					return ((o1.getId() < o2.getId())?0:1);
					return ((o1.getId() < o2.getId())? -1 : (o1.getId() == o2.getId())?0:1);
				}
			});
//		  logger.debug("lstXmlDfrs after sort "+lstXmlDfrs);
//		  Triggers triggers = null;
		  Exports exports = null;
		  TriggerChannelDTO triggerChannelDto;
		  Measurements measurements = getSubstation().getMeasurements();
		  Measurement[] arrMeasurement = null;
		  if (measurements != null)
		  {
			  arrMeasurement = measurements.getMeasurementArray();
		  }
		  
		  pmuInputs = getSubstation().getPmus().getPmuArray(0).getPmuInputs();
		  if (pmuInputs != null)
		  {
			  pmuInput = pmuInputs.getPmuInputArray();
		  }

		  for (Iterator<DFR> iterator = lstXmlDfrs.iterator(); iterator.hasNext();) {
			  xmlConfigDfr = iterator.next();
//			  logger.debug("xmlConfigDfr ID: "+xmlConfigDfr.getSystem().getDfrId());
//			  if (xmlConfigDfr.getDataPool().getAlgorithms() != null && xmlConfigDfr.getDataPool().getAlgorithms().getTriggers() != null)
//			  {
//				  triggers = xmlConfigDfr.getDataPool().getAlgorithms().getTriggers();
////				  triggerOutHold = ""+triggers.getTriggerOutHold();
////				  session.put("triggerOutHold", triggerOutHold);
////				  logger.debug("triggerOutHold in edit mode "+triggerOutHold);
//				  Trigger[] triggerXml = triggers.getTriggerArray();
////				  lstTriggerChannels = new ArrayList<TriggerChannelDTO>();
//				  for (int i = 0; i < triggerXml.length; i++) {
////					  logger.debug("triggerXml[i].getType()..."+triggerXml[i].getType());
//					  if (triggerXml[i].getType() != null && triggerXml[i].getType().equalsIgnoreCase("E"))
//					  {
//						  continue;
//					  }
//					  triggerChannelDto = constructTriggerFromXml(triggerXml[i]);
//					  triggerChannelDto.setChassis("DFR"+xmlConfigDfr.getSystem().getDfrId());
//					  populateTriggerTypes(triggerChannelDto);
//					  logger.debug("Triggers type for the trigger "+triggerChannelDto.getLstTriggerInputTypes());
//					  logger.debug("Triggers Added to  list "+triggerChannelDto);
//					  if (triggerOutHold.isEmpty())
//					  {
//						  triggerOutHold = ""+triggers.getTriggerOutHold();
//						  session.put("triggerOutHold", triggerOutHold);
//						  logger.debug("triggerOutHold in edit mode "+triggerOutHold);
//					  }
//					  lstTriggerChannels.add(triggerChannelDto);
//				
//				  }
//			  }
			  if (xmlConfigDfr.getSystem().getAnalogsCount() > 0 && xmlConfigDfr.getDataPool().getAlgorithms() != null && xmlConfigDfr.getDataPool().getExports() != null)
			  {
				  exports = xmlConfigDfr.getDataPool().getExports();
				  Export[] exportXml = exports.getExportArray();
//				  System.out.println("DFR Id "+xmlConfigDfr.getSystem().getDfrId());
//				  System.out.println("xml content "+exports);
				  for (int i = 0; i < exportXml.length; i++) {
					  if(exportXml[i].getName().startsWith("LG") || exportXml[i].getName().startsWith("RMS"))
					  {
						  continue; // Ignoring the exports created as part of global line group, which is not used now
					  }
					  triggerChannelDto = parseAndBuildMeasurementsFromXml(exportXml[i]);
					  triggerChannelDto.setMeasurementName(exportXml[i].getName());

					  // Check whether the status is enabled
					  if (arrMeasurement != null && arrMeasurement.length > 0)
					  {
						  for (int mCount = 0; mCount < arrMeasurement.length; mCount++) {
//							  logger.debug("mcount "+mCount);
							if (arrMeasurement[mCount].getDfr() == xmlConfigDfr.getSystem().getDfrId() && arrMeasurement[mCount].getId() == triggerChannelDto.getId())
							{
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
								if (arrMeasurement[mCount].getDdrStatus() != null && arrMeasurement[mCount].getDdrStatus().equalsIgnoreCase(M9kConstants.ENABLE))
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
							}
						} 
					  }
					  
					  triggerChannelDto.setChassis("DFR"+xmlConfigDfr.getSystem().getDfrId());
//					  populateTriggerTypes(triggerChannelDto);
//					  logger.debug("Triggers type for the trigger "+triggerChannelDto.getLstTriggerInputTypes());
//					  logger.debug("Triggers Added to  list "+triggerChannelDto);
//					  if (triggerOutHold.isEmpty())
//					  {
//						  triggerOutHold = ""+triggers.getTriggerOutHold();
//						  session.put("triggerOutHold", triggerOutHold);
//						  logger.debug("triggerOutHold in edit mode "+triggerOutHold);
//					  }
					  lstTriggerChannels.add(triggerChannelDto);
				
				  }
			  }
		  }
		  Collections.sort(lstTriggerChannels, new Comparator<TriggerChannelDTO>() {

				@Override
				public int compare(TriggerChannelDTO o1, TriggerChannelDTO o2) {
//					return ((o1.getId() < o2.getId())?0:1);
					return ((o1.getId() < o2.getId())? -1 : (o1.getId() == o2.getId())?0:1);
				}
			});
		  
		  return lstTriggerChannels;
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
//			  if (triggerOutHold == null || triggerOutHold.isEmpty())
//			  {
//				  triggerOutHold = ""+triggers.getTriggerOutHold();
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
			  triggerChannelDto.setDisableTripOver(true);
			  triggerChannelDto.setDisableTripUnder(true);
			  // START 29-July-2015: ROC implementation  
			  triggerChannelDto.setDisableTripRoc(true);
			  triggerChannelDto.setDisableDuration(true);
			  // END 29-July-2015: ROC implementation
			  triggerChannelDto.setTriggerStatus(false);
			  // START: 26-Mar-2020 - Hot Fix to address editing configuration in IE issue
			  triggerChannelDto.setStart(M9kConstants.NEVER);
			  triggerChannelDto.setType(M9kConstants.PHASOR);
			  // END: 26-Mar-2020
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

	  
	  private TriggerChannelDTO constructTriggerFromXml(Trigger triggerXml)
	  {
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

//		  logger.debug("Input value of the trigger..."+inputValue);
		  int index = inputValue.indexOf("Limits(");
		  if (index != -1)
		  {
			  limitsName = inputValue.substring(index+7,inputValue.lastIndexOf(")"));
//			  logger.debug("Limits name: "+limitsName);
			  for (int i = 0; i < limits.length; i++) {
				if (limits[i].getName().equalsIgnoreCase(limitsName))
				{
					triggerChannelDto.setTriggerStatus(true);
					// START: 25-Feb-2020 - ROC positive and Negative Limits implementation
					// ROC settings
					if(limits[i].getDuration() != null && !limits[i].getDuration().isEmpty() && !limits[i].getDuration().equals("0"))
					{
						 // START 29-July-2015: ROC implementation  
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
						triggerChannelDto.setDisableDuration(true);
					}
					//END: 25-Feb-2020

					if (limits[i].getHighLimit() != null && limits[i].getLowLimit() != null)
					{
						triggerChannelDto.setStart(M9kConstants.BOTH);
						triggerChannelDto.setTripIfOver(""+limits[i].getHighLimit());
						triggerChannelDto.setTripIfUnder(""+limits[i].getLowLimit());
						triggerChannelDto.setDisableTripOver(false);
						triggerChannelDto.setDisableTripUnder(false);
					}
					else if (limits[i].getHighLimit() != null)
					{
						triggerChannelDto.setStart(M9kConstants.OVER);
						triggerChannelDto.setTripIfOver(""+limits[i].getHighLimit());						
						triggerChannelDto.setDisableTripOver(false);
						triggerChannelDto.setDisableTripUnder(true);
					}
					else if (limits[i].getLowLimit() != null)
					{
						triggerChannelDto.setStart(M9kConstants.UNDER);
						triggerChannelDto.setTripIfUnder(""+limits[i].getLowLimit());	
						triggerChannelDto.setDisableTripOver(true);
						triggerChannelDto.setDisableTripUnder(false);						
					}
					else
					{
						if(limits[i].getDuration() != null && limits[i].getRateOfChangeLimit() != null)
						{
							 // START 29-July-2015: ROC implementation  
							  triggerChannelDto.setStart(M9kConstants.ROC);
								
								triggerChannelDto.setDisableTripOver(true);
								triggerChannelDto.setDisableTripUnder(true);

							  // END 29-July-2015: ROC implementation
						}
						else
						{
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
					break;
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
			int startIndex;
			
			AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
			
			  Exports exports = xmlConfigDfr.getDataPool().getExports();
			  Export export[] = null;
			  if (exports != null)
			  {
				  export = exports.getExportArray();
			  }
			  pmuInputs = getSubstation().getPmus().getPmuArray(0).getPmuInputs();
			  pmuInput = null;
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			Rms rms[] = xmlConfigDfr.getDataPool().getAlgorithms().getRmsArray();		
			triggerChannelDto.setDisableHarmonic(true);
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
					 
					for (int exportCnt=0; (export!=null&&exportCnt < export.length);exportCnt++)
					{
						inputValue = export[exportCnt].getInput();
						startIndex = inputValue.indexOf("Rms(");
						if (startIndex == -1)
						{
							continue;
						}
						else
						{
							startIndex+=4;
						}
					}
					break;
				}
			}

		}
		
		private void constructMagnitudeFromXml(String magnitudeName, TriggerChannelDTO triggerChannelDto)
		{
			String inputValue = "";
			String analogName= "";
//			String exportName = "";
			int startIndex;
			  Exports exports = xmlConfigDfr.getDataPool().getExports();
			  Export export[] = null;
			  if (exports != null)
			  {
				  export = exports.getExportArray();
			  }
			  pmuInputs = getSubstation().getPmus().getPmuArray(0).getPmuInputs();
			  pmuInput = null;
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
					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
					{
						inputValue = export[exportCnt].getInput();
						startIndex = inputValue.indexOf("Magnitude(");
						if (startIndex == -1)
						{
							continue;
						}
						else
						{
							startIndex+=10;
						}
					}
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
			  pmuInputs = getSubstation().getPmus().getPmuArray(0).getPmuInputs();
			  pmuInput = null;
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
			Frequency frequency[] = xmlConfigDfr.getDataPool().getAlgorithms().getFrequencyArray();		
			triggerChannelDto.setDisableHarmonic(true);
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
							for (int pmuCnt=0; pmuInput != null && pmuCnt < pmuInput.length;pmuCnt++)
							{
								if (pmuInput[pmuCnt].getDfr() == xmlConfigDfr.getSystem().getDfrId() && pmuInput[pmuCnt].getId() == export[exportCnt].getId())
								{
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
			  pmuInputs = getSubstation().getPmus().getPmuArray(0).getPmuInputs();
			  pmuInput = null;
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
//							logger.debug("Analog INput Channel..."+(analogInputs[j2].getChannel()+analogOffset));
							break;
						}
					}
					break;
				}
			}
		}
		
		private void constructZeroSequenceFromXml(String zeroSequenceName, TriggerChannelDTO triggerChannelDto)
		{
			String lineGroupName = "";
			String inputValue = "";
			String inputType="";
			int startIndex;
		    ZeroSequence zeroSequence[] = xmlConfigDfr.getDataPool().getAlgorithms().getZeroSequenceArray();
		    LineGroup[] lineGroups = xmlConfigDfr.getDataPool().getLineGroups().getLineGroupArray();
			  Exports exports = xmlConfigDfr.getDataPool().getExports();
			  Export export[] = null;
			  if (exports != null)
			  {
				  export = exports.getExportArray();
			  }
			
			  pmuInputs = getSubstation().getPmus().getPmuArray(0).getPmuInputs();
			  pmuInput = null;
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
					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
					{
						inputValue = export[exportCnt].getInput();
						startIndex = inputValue.indexOf(triggerChannelDto.getType());
						if (startIndex == -1)
						{
							continue;
						}
						else
						{
							startIndex+=triggerChannelDto.getType().length()+1;
						}
					}

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
			int startIndex;
		    PositiveSequence positiveSequence[] = xmlConfigDfr.getDataPool().getAlgorithms().getPositiveSequenceArray();
		    LineGroup[] lineGroups = xmlConfigDfr.getDataPool().getLineGroups().getLineGroupArray();
			  Exports exports = xmlConfigDfr.getDataPool().getExports();
			  Export export[] = null;
			  if (exports != null)
			  {
				  export = exports.getExportArray();
			  }
			  pmuInputs = getSubstation().getPmus().getPmuArray(0).getPmuInputs();
			  pmuInput = null;
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			triggerChannelDto.setDisableHarmonic(true);
//			logger.debug("ZS Name: "+positiveSequenceName);
			for (int j = 0; j < positiveSequence.length; j++) {
				if (positiveSequence[j].getName().equals(positiveSequenceName))
				{
						inputValue = positiveSequence[j].getInput();
						inputType = positiveSequence[j].getInputType();
						if (positiveSequence[j].getAverage() > 0)
						{
							triggerChannelDto.setAverage(positiveSequence[j].getAverage());
						}
//						logger.debug("ZS input Value: "+inputValue+" input Type: "+inputType);
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
					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
					{
						inputValue = export[exportCnt].getInput();
						startIndex = inputValue.indexOf(triggerChannelDto.getType());
						if (startIndex == -1)
						{
							continue;
						}
						else
						{
							startIndex+=triggerChannelDto.getType().length()+1;
						}
					}
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
			int startIndex;
			
		    NegativeSequence negativeSequence[] = xmlConfigDfr.getDataPool().getAlgorithms().getNegativeSequenceArray();
		    LineGroup[] lineGroups = xmlConfigDfr.getDataPool().getLineGroups().getLineGroupArray();
			  Exports exports = xmlConfigDfr.getDataPool().getExports();
			  Export export[] = null;
			  if (exports != null)
			  {
				  export = exports.getExportArray();
			  }
			  pmuInputs = getSubstation().getPmus().getPmuArray(0).getPmuInputs();
			  pmuInput = null;
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
					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
					{
						inputValue = export[exportCnt].getInput();
						startIndex = inputValue.indexOf(triggerChannelDto.getType());
						if (startIndex == -1)
						{
							continue;
						}
						else
						{
							startIndex+=triggerChannelDto.getType().length()+1;
						}
					}
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
			int startIndex;
			
		    Power power[] = xmlConfigDfr.getDataPool().getAlgorithms().getPowerArray();
		    LineGroup[] lineGroups = xmlConfigDfr.getDataPool().getLineGroups().getLineGroupArray();
			  Exports exports = xmlConfigDfr.getDataPool().getExports();
			  Export export[] = null;
			  if (exports != null)
			  {
				  export = exports.getExportArray();
			  }
			  pmuInputs = getSubstation().getPmus().getPmuArray(0).getPmuInputs();
			  pmuInput = null;
			  if (pmuInputs != null)
			  {
				  pmuInput = pmuInputs.getPmuInputArray();
			  }
			triggerChannelDto.setDisableHarmonic(true);
//			logger.debug("Power Name: "+powerName);
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

//							logger.debug("Power: LineGroup INput Channel..."+triggerChannelDto.getChannel());
							break;
						}
					}
					for (int exportCnt=0; export != null && exportCnt < export.length;exportCnt++)
					{
						inputValue = export[exportCnt].getInput();
						startIndex = inputValue.indexOf(triggerChannelDto.getType());
						if (startIndex == -1)
						{
							continue;
						}
						else
						{
							startIndex+=triggerChannelDto.getType().length()+1;
						}
					}
					break;
				}
			}
			
		}
		
		// START: 28-Jan-2020 - Implementing Transducer - Average algorithm
		private void constructAverageFromXml(String rmsName, TriggerChannelDTO triggerChannelDto)
		{
			String inputValue = "";
			String analogName= "";
			int startIndex;
			
			AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
			
			  Exports exports = xmlConfigDfr.getDataPool().getExports();
			  Export export[] = null;
			  if (exports != null)
			  {
				  export = exports.getExportArray();
			  }
			  PmuInputs pmuInputs = getSubstation().getPmus().getPmuArray(0).getPmuInputs();
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
							if (analogInputs[j2].isSetTransducer() && analogInputs[j2].getTransducer() == 1)
							{
								triggerChannelDto.setTransducer(true);
								triggerChannelDto.setUnits(analogInputs[j2].getTransducerUnits());
								triggerChannelDto.setType(analogInputs[j2].getTransducerUnits()); // Type is same as Transducer Units
							}
							
//							logger.debug("Analog INput Channel..."+(analogInputs[j2].getChannel()+analogOffset));
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
		// END: 28-Jan-2020

		  public int getDfrAnalogOffset(String dfrName)
		  {
//			  logger.debug("DFr name to look up "+dfrName);
			  int analogOffset = 0;
			  DfrDTO dfrDTO;
			  if (lstDfrDTO == null)
			  {
				// START: 05-Jun-2020 - Mini implementation
//				  lstDfrDTO = populateDFRsFromXML(getSubstation());
				  lstDfrDTO = M9kXMLUtils.populateDFRsFromXML(getSubstation());
				// STOP: 05-Jun-2020 - Mini implementation
			  }
			  for (Iterator<DfrDTO> iterator = lstDfrDTO.iterator(); iterator.hasNext();) {
				  dfrDTO = iterator.next();
				if (dfrDTO.getDfrName().equalsIgnoreCase(dfrName))
				{
					analogOffset = dfrDTO.getAnalogChannelStart()-1;
					break;
				}
			}
//			  logger.debug("Analog offset to be returned "+analogOffset);
			  return analogOffset;
		  }


		  
			@SuppressWarnings("unused")
			// Used the method from M9kXMLUtil utility class
			private List<DfrDTO> populateDFRsFromXMLOld(SubStation substation)
			{
				  int nextStartAnalogIndex = -1;
				  int nextStartDigitalIndex = -1;
				  int analogCnt;
				  int digitalCnt;
				
//				logger.debug("Substation..."+substation);
				List<DfrDTO> lstDfrs = new ArrayList<DfrDTO>(substation.getDFRs().sizeOfDFRArray());
				DfrDTO dfrDto;
				DFR[] xmlDfrs = substation.getDFRs().getDFRArray();
				logger.debug("Total DFRs.."+substation.getDFRs().sizeOfDFRArray());
				for (int i = 0; i < xmlDfrs.length; i++) {
					dfrDto = new DfrDTO();
					dfrDto.setDfrName("DFR"+xmlDfrs[i].getSystem().getDfrId());
					dfrDto.setDfrId(xmlDfrs[i].getSystem().getDfrId());
//					logger.debug("Name: "+dfrDto.getDfrName());
					dfrDto.setIpAddress(xmlDfrs[i].getIPAddress());
//					logger.debug("IPAddress: "+dfrDto.getIpAddress());
					dfrDto.setStatus("COMPLETE");
					if (xmlDfrs[i].getDataPool().getChannels().getEvents() != null)
					{
						digitalCnt = xmlDfrs[i].getDataPool().getChannels().getEvents().sizeOfEventInputArray();
						dfrDto.getChnlConfigDropDownList().setDigitalCnt(digitalCnt);
						// START: 05-Jun-2020 - Mini
						if (digitalCnt == M9kConstants.NO_OF_DIGITAL_CHNLS_PER_ANEV_BOARD)
						{
							dfrDto.setAnevCard(true);
						}
						// END: 05-Jun-2020 - Mini
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
					if (xmlDfrs[i].getDataPool().getChannels().getAnalogs() != null)
					{
						analogCnt = xmlDfrs[i].getDataPool().getChannels().getAnalogs().sizeOfAnalogInputArray();
						dfrDto.getChnlConfigDropDownList().setAnalogCnt(analogCnt);
						if (nextStartAnalogIndex == -1)
						{
							dfrDto.setAnalogChannelStart(1);
							dfrDto.setAnalogChannelEnd(analogCnt);
							// START: 05-Jun-2020 - Mini
							if (dfrDto.isAnevCard())
							{
								nextStartAnalogIndex = (analogCnt-(analogCnt%M9kConstants.NO_OF_ANALOG_CHNLS_PER_ANEV_BOARD))+1;
							}
							else
							{
								nextStartAnalogIndex = (analogCnt-(analogCnt%M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD))+1;
							}
							// END: 15-Jun-2020 - Mini
						}
						else 
						{
							dfrDto.setAnalogChannelStart(nextStartAnalogIndex);
							dfrDto.setAnalogChannelEnd(nextStartAnalogIndex + analogCnt - 1);
							// START: 05-Jun-2020 - Mini
							if (dfrDto.isAnevCard())
							{
								nextStartAnalogIndex = (analogCnt-(analogCnt%M9kConstants.NO_OF_ANALOG_CHNLS_PER_ANEV_BOARD));
							}
							else
							{
								nextStartAnalogIndex += (analogCnt-(analogCnt%M9kConstants.NO_OF_ANALOG_CHNLS_PER_BOARD));
							}
							// END: 15-Jun-2020 - Mini
							
						}
						
					}
					else
					{
						dfrDto.getChnlConfigDropDownList().setAnalogCnt(0);
					}
//					logger.debug("Dfrs added to the list..."+dfrDto);
					lstDfrs.add(dfrDto);
				}
				return lstDfrs;
			}

			/**
			 * 
			 */
			private void populateStationDetails() {
				stationDto = new StationDTO();
				  try {
					  com.usi.SystemDocument.System systemDetails = getSubstation().getDFRs().getDFRArray(0).getSystem();
					  DFR[] dfrs = getSubstation().getDFRs().getDFRArray();
						List<Integer> lineGroupIds = new ArrayList<Integer>();
						List<Integer> triggerIds = new ArrayList<Integer>();
						List<Integer> exportIds = new ArrayList<Integer>();
						List<Integer> measurementIds = new ArrayList<Integer>();

						Integer totalMeasurements = 0;
						Integer totalEventsConfigured = 0;
						
						for (int i = 0; i < dfrs.length; i++) {
//							logger.debug("Dfr count..."+i);
							Exports exports = dfrs[i].getDataPool().getExports();
							if (exports != null && exports.sizeOfExportArray() > 0)
							{
								for (int j = 0; j < exports.sizeOfExportArray(); j++) {
									exportIds.add(exports.getExportArray(j).getId());						
								}
							}

							LineGroups lineGroups = dfrs[i].getDataPool().getLineGroups();
							if (lineGroups != null && lineGroups.sizeOfLineGroupArray() > 0)
							{
								for (int j = 0; j < lineGroups.sizeOfLineGroupArray(); j++) {
									lineGroupIds.add(lineGroups.getLineGroupArray(j).getId());
//									logger.debug("Line Group Id added "+lineGroups.getLineGroupArray(j).getId()+ " for dfr "+dfrs[i].getSystem().getDfrId());
								}
							}
							if (dfrs[i].getDataPool().getAlgorithms() != null)
							{
								Triggers triggers = dfrs[i].getDataPool().getAlgorithms().getTriggers();
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
						Measurements measurements = getSubstation().getMeasurements();
						if (measurements != null && measurements.sizeOfMeasurementArray() > 0)
						{
							for (int i = 0; i < measurements.sizeOfMeasurementArray(); i++) {
								measurementIds.add(measurements.getMeasurementArray(i).getId());
							}
							Collections.sort(measurementIds);
							totalMeasurements = measurementIds.get(measurementIds.size() - 1);
						}
						GroupTriggers groupTriggers = getSubstation().getGroupTriggers();
						if (groupTriggers != null && groupTriggers.sizeOfGroupTriggerArray() > 0)
						{
							for (int j = 0; j < groupTriggers.sizeOfGroupTriggerArray(); j++) {
								triggerIds.add(groupTriggers.getGroupTriggerArray(j).getId());
							}
						}
						GlobalLineGroups globalLineGroups = getSubstation().getGlobalLineGroups();
						if (globalLineGroups != null && globalLineGroups.sizeOfGlobalLineGroupArray() > 0)
						{
							for (int i = 0; i < globalLineGroups.sizeOfGlobalLineGroupArray(); i++) {
								lineGroupIds.add(globalLineGroups.getGlobalLineGroupArray(i).getId());
//								logger.debug("Global Line Group Id added "+globalLineGroups.getGlobalLineGroupArray(i).getId());
							}
						}
						if (!exportIds.isEmpty())
						{
							Collections.sort(exportIds);
							totalEventsConfigured = exportIds.get(exportIds.size()-1);				
						}
						if (!lineGroupIds.isEmpty())
						{
							setLineGroupsConfigured(true);
						}
						

						stationDto.setTotalDfrsConfigured(getSubstation().getDFRs().sizeOfDFRArray());
						stationDto.setSystemAnalogChannelsCount(systemDetails.getAnalogsCount());
						stationDto.setSystemDigitalChannelsCount(systemDetails.getDigitalsCount());
						stationDto.setSystemSampleRate(systemDetails.getSampleRate());
						stationDto.setSystemLongTermSampleRate(systemDetails.getExportAnalogSampleRate());
						stationDto.setSystemLineFrequency(systemDetails.getLineFrequency());
						stationDto.setExportRate(systemDetails.getExportRate());
						stationDto.setSystemRecordingDeviceId(systemDetails.getRecordingDeviceId());
						stationDto.setSystemPrefaultTime(systemDetails.getPrefaultTime());
						stationDto.setSystemPostfaultTime(systemDetails.getPostfaultTime());
						stationDto.setSystemLtrPrefaultTime(systemDetails.getLtrPrefaultTime());
						stationDto.setSystemLtrPostfaultTime(systemDetails.getLtrPostfaultTime());
						stationDto.setChatterLimit(systemDetails.getChatterLimit());
						stationDto.setChatterRate(systemDetails.getChatterRate());
						stationDto.setTriggerLimit(systemDetails.getTriggerLimit());
						// 26-June-2012 - As phase algorithm doesn't have triggers, we'll use measurements to get the last id used
//						stationDto.setTotalTriggersConfigured(totalTriggersConfigured);
						stationDto.setTotalTriggersConfigured(totalMeasurements);
						stationDto.setPs(systemDetails.getPs());
						if (systemDetails.getSignalTooLowFactor() >= 0 )
						{
							stationDto.setSignalTooLowFactor(systemDetails.getSignalTooLowFactor());
						}
						else
						{
							stationDto.setSignalTooLowFactor(1.0);
						}
						
						if (systemDetails.getEventDebounce() >= 0 )
						{
							stationDto.setEventDebounce(systemDetails.getEventDebounce());
						}
						else
						{
							stationDto.setEventDebounce(1.0);
						}
						
						// START: 06-MAR-2018 - Configurable wetting voltage
						if (systemDetails.isSetMonitorWettingVoltage() )
						{
							stationDto.setWettingVoltageMonitor(systemDetails.getMonitorWettingVoltage());
						}
						else
						{
							// Default enabled
							stationDto.setWettingVoltageMonitor(1);
						}
						// END: 06-Mar-2018
						
						// Trigger duration
						if (getSubstation().getStationProperties() == null)
						{
							stationDto.setTriggerDuration(10); // Default 10 seconds
							// START 13-Sept-2022 - cont data days limit moved to GUI
							stationDto.setContOscDaysToRetain(5); // Default 5 days to retain OSC
							stationDto.setContMeasurementsDaysToRetain(30); // Default 30 days to retain measurements
							// END 13-Sept-2022 - cont data days limit moved to GUI
							// START: 09-Jan-2023 - Moving export time limit for cont. data to GUI
							stationDto.setContOscExportTimeLimit(120); // Default 120 seconds / 2 minutes
							stationDto.setContMeasurementsExportTimeLimit(600);// Default 600 seconds / 10 minutes
							// END: 09-Jan-2023
						}
						else
						{
							stationDto.setTriggerDuration(getSubstation().getStationProperties().getTriggerDuration());
							// START 13-Sept-2022 - cont data days limit moved to GUI
							if (getSubstation().getStationProperties().getContOscDaysToRetain() > 0)
							{
								stationDto.setContOscDaysToRetain(getSubstation().getStationProperties().getContOscDaysToRetain()); // Default 5 days to retain OSC
							}
							else
							{
								stationDto.setContOscDaysToRetain(5);// Default 5
							}
							
							if (getSubstation().getStationProperties().getContMeasurementsDaysToRetain() > 0)
							{
								stationDto.setContMeasurementsDaysToRetain(getSubstation().getStationProperties().getContMeasurementsDaysToRetain()); // Default 30 days to retain measurements
							}
							else
							{
								stationDto.setContMeasurementsDaysToRetain(30);
							}
							// END 13-Sept-2022 - cont data days limit moved to GUI
							// START: 09-Jan-2023 - Moving export time limit for cont. data to GUI
							if (getSubstation().getStationProperties().getContOscExportTimeLimit() > 0)
							{
								stationDto.setContOscExportTimeLimit(getSubstation().getStationProperties().getContOscExportTimeLimit()); // Default 5 days to retain OSC
							}
							else
							{
								stationDto.setContOscExportTimeLimit(120);// Default 120 seconds / 2 minutes
							}
							
							if (getSubstation().getStationProperties().getContMeasurementsExportTimeLimit() > 0)
							{
								stationDto.setContMeasurementsExportTimeLimit(getSubstation().getStationProperties().getContMeasurementsExportTimeLimit()); // Default 30 days to retain measurements
							}
							else
							{
								stationDto.setContMeasurementsExportTimeLimit(600); // Default 600 seconds / 10 minutes
							}
							// END: 09-Jan-2023
						}
							
						// Populate PMU attributes
						if (getSubstation().getPmus() != null)
						{
							Pmu pmu = getSubstation().getPmus().getPmuArray(0);
							stationDto.setPmuId(pmu.getPmuId());
							stationDto.setPmuPort(pmu.getPmuPort());
							stationDto.setPmuUdpPort(pmu.getUdpPort());
							stationDto.setPmuStreamType(pmu.getPmuStreamType());
//							logger.debug("PMU stream type from XML "+stationDto.getPmuStreamType());
							stationDto.setPmuPhasorMode(pmu.getPmuPhasorMode());
							stationDto.setPmuDataRate(pmu.getPmuDataRate());
							if (pmu.getPmuEnable() == M9kConstants.ENABLE_ONE)
							{
								// Changed for PDC implementation 4-Oct-2013
//								stationDto.setPmuStatus(M9kConstants.ENABLE);
								stationDto.setPmuEnabled(true);
								// End
							}
							else
							{
								// Changed for PDC implementation 4-Oct-2013
//								stationDto.setPmuStatus(M9kConstants.DISABLE);
								stationDto.setPmuEnabled(false);
								// End
							}
						}
						populatePdcFromXML();
//						stationDto.setConfigXml(configXml);
						
					} 
					catch(Exception e)
					{
						System.out.println("Exception occured during edit "+e);
						e.printStackTrace();;
					}

				
			}

			private void populatePdcFromXML() {
				Pdc pdc = substation.getPdc();
				if (pdc != null && pdc.getSystem() != null)
				{
					com.usi.SystemDocument.System pdcSystem = pdc.getSystem();
					stationDto.setPdcId(pdcSystem.getStationId()); // Station Id is used to store the PDC id
					stationDto.setCommandServerPort(pdcSystem.getCommandServerPort());
					stationDto.setPdcStreamType(pdcSystem.getNetworkProtocol());
					stationDto.setPdcMaxWait(pdcSystem.getMaxWait());
					stationDto.setPmuDataRate(pdcSystem.getDataRate());
					stationDto.setPdcTcpPort(pdcSystem.getServerPort());
					UdpServers udpServers = pdcSystem.getUdpServers();
					if (udpServers != null)
					{
//						java.lang.System.out.println("\n\n\n\t\t\t\t\n\n\n list of udp servers "+udpServers.xmlText());
						if (stationDto.getLstOfUDPserverPorts() == null)
						{
							stationDto.setLstOfUDPserverPorts(new ArrayList<Integer>(udpServers.sizeOfUdpServerPortArray()));
						}
//						System.out.println("\n\n\n\t\t\t\t\n\n\n list of udp servers array size "+udpServers.sizeOfUdpServerPortArray());
						for (int i = 0; i < udpServers.sizeOfUdpServerPortArray(); i++) {
							stationDto.getLstOfUDPserverPorts().add(udpServers.getUdpServerPortArray(i));
						}
					}
				}
			}

			
			// Return all local and global linegroups to populate the report
			public List<LineGroupsAlgorithm> getListOfAllLinegroupsForReport()
			{
				List<LineGroupsAlgorithm> lstLineGroups = new ArrayList<LineGroupsAlgorithm>();
				LineGroupsAlgorithm lineGroups;

				List<AnalogChannelDTO> lgInput;
				DFR[] dfrs = getSubstation().getDFRs().getDFRArray();
				DFR xmlDfr;
				for (int j = 0; j < dfrs.length; j++) {
					xmlDfr = dfrs[j];
					if (xmlDfr.getDataPool().getLineGroups() != null) {
//						String type;
						LineGroup xmlLineGroup[] = xmlDfr.getDataPool()
								.getLineGroups().getLineGroupArray();
						for (int i = 0; i < xmlLineGroup.length; i++) {
							lineGroups = new LineGroupsAlgorithm();
							if (xmlLineGroup[i].getLineGroupName() != null)
							{
								lineGroups.setLineGroupName(xmlLineGroup[i].getLineGroupName());
							}
							else
							{
								lineGroups.setLineGroupName(xmlLineGroup[i].getName());
							}
//							logger.debug("xmlLineGroup[i].getName() "+xmlLineGroup[i].getName());
							lineGroups.setId(xmlLineGroup[i].getId());
							lineGroups.setName("LG_"+xmlLineGroup[i].getId());
							lineGroups.setEnableAutoCalc(xmlLineGroup[i].getAutoCalc());
							lineGroups.setPositiveResistance(xmlLineGroup[i].getPositiveResistance());
							lineGroups.setPositiveReactance(xmlLineGroup[i].getPositiveReactance());
							lineGroups.setZeroResistance(xmlLineGroup[i].getZeroResistance());
							lineGroups.setZeroReactance(xmlLineGroup[i].getZeroReactance());
							lineGroups.setLineMiles(xmlLineGroup[i].getLineMiles());
//							logger.debug("Decision logic populated "+xmlLineGroup[i].getDecisionLogic());
							if (xmlLineGroup[i].getDecisionLogic() != null && !xmlLineGroup[i].getDecisionLogic().isEmpty()
									&& !xmlLineGroup[i].getDecisionLogic().equalsIgnoreCase("NONE"))
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

//							logger.debug("\t\t\t Local line group name "+lineGroups.getName());
							lgInput = getAnalogChannelsForNames(xmlLineGroup[i]
									.getInputArray(), "DFR"+xmlDfr.getId());
//							type = getLineGroupType(lgInput);
							lineGroups.setInputChannels(lgInput);
//							lineGroups.setType(type);
							lineGroups.setLocal(true);
							lineGroups.setChassis("DFR"+xmlDfr.getId());
							lstLineGroups.add(lineGroups);
						}
					}
				}
				  GlobalLineGroups globalLineGroups = substation.getGlobalLineGroups();
				  
				  if (globalLineGroups != null)
				  {
					  GlobalLineGroup[] arrGlobalLineGroup = globalLineGroups.getGlobalLineGroupArray();
					  if (arrGlobalLineGroup != null && arrGlobalLineGroup.length > 0)
					  {
							List<LineGroupChannelsDTO> lstLineGroupChannelsDTOs;
							LineGroupChannelsDTO lineGroupChannelsDTO;
							AnalogChannels[] analogChannels;
//							String type;
//							logger.debug("Total Global line groups "+arrGlobalLineGroup.length);
						  for (int i = 0; i < arrGlobalLineGroup.length; i++) {
							  lstLineGroupChannelsDTOs = new ArrayList<LineGroupChannelsDTO>(8);
								lineGroups = new LineGroupsAlgorithm();
								lineGroups.setLocal(false);
								lineGroups.setId(arrGlobalLineGroup[i].getId());
								lineGroups.setName("LG_"+arrGlobalLineGroup[i].getId());
								if (arrGlobalLineGroup[i].getLineGroupName() != null)
								{
									lineGroups.setLineGroupName(arrGlobalLineGroup[i].getLineGroupName());
								}
								else
								{
									lineGroups.setLineGroupName(arrGlobalLineGroup[i].getName());
								}
								lineGroups.setEnableAutoCalc(arrGlobalLineGroup[i].getAutoCalc());
								lineGroups.setPositiveResistance(arrGlobalLineGroup[i].getPositiveResistance());
								lineGroups.setPositiveReactance(arrGlobalLineGroup[i].getPositiveReactance());
								lineGroups.setZeroResistance(arrGlobalLineGroup[i].getZeroResistance());
								lineGroups.setZeroReactance(arrGlobalLineGroup[i].getZeroReactance());
								lineGroups.setLineMiles(arrGlobalLineGroup[i].getLineMiles());
								if (arrGlobalLineGroup[i].getDecisionLogic() != null && !arrGlobalLineGroup[i].getDecisionLogic().isEmpty()
										&& !arrGlobalLineGroup[i].getDecisionLogic().equalsIgnoreCase("NONE"))
								{
									lineGroups.setDecisionLogic(arrGlobalLineGroup[i].getDecisionLogic());
								}
								else
								{
									lineGroups.setDecisionLogic("");
								}
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
//								populateRMSExportForLineGroupChannels(lgInput);
//								type = getLineGroupType(lgInput);
								lineGroups.setInputChannels(lgInput);
//								lineGroups.setType(type);
								lineGroups.setLocal(false);
								lstLineGroups.add(lineGroups);					
						  }
//						  logger.debug("Total Line Groups available"+lstLineGroups.size() );
					  }
					  
				  }
				  

				  logger.debug("Returning from  editLineGroups "+lstLineGroups);
				return lstLineGroups;
			}
			
			private  void  populateEventDTO() {
				EventInput[] eventInputs;
				EventChannelDTO eventDto;
				lstEventChannels = new ArrayList<EventChannelDTO>(500);
				int eventChnlIndex = 1;
				
				// START: 31-MAR-2015	Enable PMU for Events Version 1.0.7.1
				PmuInput localPmuInput;
				Pmu localPmu = null;
				// END: 31-MAR-2015	Enable PMU for Events Version 1.0.7.1
				
				DFR[] dfrs = getSubstation().getDFRs().getDFRArray();
				DFR xmlDfr;
//				int counter = 0;
				for (int j = 0; j < dfrs.length; j++) {
					xmlDfr = dfrs[j];
//					System.out.println("DFR "+xmlDfr.getId());
					if (xmlDfr.getDataPool().getChannels().getEvents() == null)
					{
						continue;
					}
//					logger.debug("\n\n\t\t\tdfr config xml "+xmlDfr.xmlText());
					eventInputs = xmlDfr.getDataPool().getChannels().getEvents().getEventInputArray();
//					System.out.println("DFR "+xmlDfr.getId()+" has events " + eventInputs.length);
					// START: 31-MAR-2015	Enable PMU for Events Version 1.0.7.1
					localPmu = xmlDfr.getDataPool().getAlgorithms().getPmus().getPmuArray(0);
					// END: 31-MAR-2015	
					for (int i = 0; i < eventInputs.length; i++,eventChnlIndex++) {
						eventDto = new EventChannelDTO();
//						eventDto.setTriggerStatus(true);
						eventDto.setName(eventInputs[i].getName());
						eventDto.setChassis("DFR"+xmlDfr.getId());
						eventDto.setChannel("" + eventChnlIndex);
						eventDto.setDescription(eventInputs[i].getEventName());

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
//								eventDto.setDfrSer(M9kConstants.BOTH);
								eventDto.setDfr(true);
								eventDto.setSer(true);
								eventDto.setDisableDfrStart(false);
//								eventDto.setDisableSerRun(false);
//								eventDto.setSerRun(""+M9kConstants.ENABLE_ONE);
							}
							else if (eventInputs[i].getEnableDfr() == M9kConstants.ENABLE_ONE
									&& eventInputs[i].getEnableSer() == M9kConstants.DISABLE_ZERO)
							{
//								eventDto.setDfrSer(M9kConstants.DFR);
								eventDto.setDisableDfrStart(false);
//								eventDto.setDisableSerRun(true);
//								eventDto.setSerRun(""+M9kConstants.DISABLE_ZERO);
								eventDto.setDfr(true);
								eventDto.setSer(false);

							}
							else if (eventInputs[i].getEnableDfr() == M9kConstants.DISABLE_ZERO
									&& eventInputs[i].getEnableSer() == M9kConstants.ENABLE_ONE)
							{
//								eventDto.setDfrSer(M9kConstants.SER);
								eventDto.setDisableDfrStart(true);
//								eventDto.setTriggerStatus(false);
//								eventDto.setDisableSerRun(false);
//								eventDto.setSerRun(""+M9kConstants.ENABLE_ONE);
								eventDto.setDfr(false);
								eventDto.setSer(true);

							}
//							if (eventInputs[i].getTriggerModeDfr() != null && eventInputs[i].getTriggerModeDfr().equalsIgnoreCase(M9kConstants.DISABLED))
//							{
//								eventDto.setTriggerStatus(false);
//							}
//							eventDto.setSerRun(""+eventInputs[i].getEnableSer());
							
							lstEventChannels.add(eventDto);
					}
				}
			}
			
			private void populateVirtualChannelsDTO() throws M9000Exception {
				List<AnalogChannelDTO> lstVirtualInputChannels;

				try
				{
					String dfrKey= null;
					DFR xmlDfr;
					AnalogInput[] analogInputs;
					AnalogChannelDTO analogDto;
					
					DFR[] dfrs = getSubstation().getDFRs().getDFRArray();
					lstVirtualChannels = new ArrayList<AnalogChannelDTO>();
					for (int j = 0; j < dfrs.length; j++) {
						dfrKey = "DFR"+dfrs[j].getId();
						xmlDfr = dfrs[j];
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
							if (analogInputs[i].getInputArray() != null && analogInputs[i].getInputArray().length > 0)
							{
								lstVirtualInputChannels.addAll(getAnalogChannelsForNames(dfrKey, analogInputs[i].getInputArray(), "+"));
							}
							if (analogInputs[i].getInputAddArray() != null && analogInputs[i].getInputAddArray().length > 0)
							{
								lstVirtualInputChannels.addAll(getAnalogChannelsForNames(dfrKey, analogInputs[i].getInputAddArray(), "+"));
							}
							if (analogInputs[i].getInputSubArray() != null && analogInputs[i].getInputSubArray().length > 0)
							{
								lstVirtualInputChannels.addAll(getAnalogChannelsForNames(dfrKey, analogInputs[i].getInputSubArray(),"-"));
							}
							analogDto.setLstAnalogsForVirtual(lstVirtualInputChannels);
							lstVirtualChannels.add(analogDto);
						}
					}
					Collections.sort(lstVirtualChannels, new Comparator<AnalogChannelDTO>() {

						@Override
						public int compare(AnalogChannelDTO o1, AnalogChannelDTO o2) {
							int lhs = Integer.parseInt(o1.getName().substring(13)); // 13 is the length of string prefix VirtualAnalog
							int rhs = Integer.parseInt(o2.getName().substring(13));
							return ((lhs < rhs)? -1 : (lhs == rhs)?0:1);
						}
					});
					
				}
				catch (Exception e) {
					logger.error("Exception occured in populateVirtualChannelsDTO method",e);
					throw new M9000Exception("Exception in populateVirtualChannelsDTO method ",e);
				}

				logger.debug("Returning from populateVirtualChannelsDTO method "+lstVirtualChannels);
			}
			
			private List<AnalogChannelDTO> getAnalogChannelsForNames(
					String[] lgInputNames, String chassis) {
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
						if (analogDto.getChassis().equalsIgnoreCase(chassis) && analogDto.getName().equalsIgnoreCase(channelName)) {
							lgInput.add(analogDto);
//							logger.info("Added " + analogDto.getName());
							break;
						}
					}
				}
				return lgInput;
			}

			private List<AnalogChannelDTO> getAnalogChannelsForLineGroups(
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
			
			private PmuInput getPmuIfAlreadyExists(Pmu localPmu, String pmuName)
			{
//				logger.debug("Pmu Name to be compared "+pmuName);
				PmuInput pmuInput = null;
				PmuInput[] arrPmuInput = localPmu.getPmuInputArray();
				for (int i = 0; i < arrPmuInput.length; i++) {
//					logger.debug("arrPmu[i].getName() "+arrPmuInput[i].getName());
					if (arrPmuInput[i].getName().trim().equalsIgnoreCase(pmuName.trim()))
					{
						pmuInput = arrPmuInput[i];
						break;
					}
				}
				return pmuInput;
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
			
			/**
			 * Returns of the distinct exported measurements by parsing the xml. Invoked from Continuous Measurement Tab to display all exported measurements
			 * @return
			 */
			public List<String> getLstOfExportedMeasurementNames()
			{
				List<String> lstMeasurementNames = new LinkedList<String>();
				DFR[] arrDfrs = getSubstation().getDFRs().getDFRArray();
				  List<DFR> lstXmlDfrs = new ArrayList<DFR> (Arrays.asList(arrDfrs));
				  Collections.sort(lstXmlDfrs, new Comparator<DFR>() {

						@Override
						public int compare(DFR o1, DFR o2) {
							return ((o1.getId() < o2.getId())? -1 : (o1.getId() == o2.getId())?0:1);
						}
					});
				  for (Iterator<DFR> iterator = lstXmlDfrs.iterator(); iterator.hasNext();) {
					  xmlConfigDfr = iterator.next();
					  if (xmlConfigDfr.getSystem().getAnalogsCount() > 0 && xmlConfigDfr.getDataPool().getAlgorithms() != null && xmlConfigDfr.getDataPool().getExports() != null)
					  {
						  Export[] exportXml = xmlConfigDfr.getDataPool().getExports().getExportArray();
						  for (int i = 0; i < exportXml.length; i++) {
							  if (!lstMeasurementNames.contains(exportXml[i].getMeasurementType()))
							  {
								  lstMeasurementNames.add(exportXml[i].getMeasurementType());
							  }
						  }
					  }
				  }
				  
				  return lstMeasurementNames;
			}
			
			/**
			 * the maximum triggerlimit set among measurements in all DFRs
			 * @return
			 */
			public int getTheMaximumTriggerLimitFromConfig()
			{
				logger.debug("In getTheMaximumTriggerLimitFromConfig ");
				int maxTriggerLimit = 0;
				DFR[] arrDfrs = getSubstation().getDFRs().getDFRArray();
				maxTriggerLimit = arrDfrs[0].getSystem().getTriggerLimit();
				  List<DFR> lstXmlDfrs = new ArrayList<DFR> (Arrays.asList(arrDfrs));
				  Collections.sort(lstXmlDfrs, new Comparator<DFR>() {

						@Override
						public int compare(DFR o1, DFR o2) {
							return ((o1.getId() < o2.getId())? -1 : (o1.getId() == o2.getId())?0:1);
						}
					});
				  for (Iterator<DFR> iterator = lstXmlDfrs.iterator(); iterator.hasNext();) {
					  xmlConfigDfr = iterator.next();
					  if (xmlConfigDfr.getSystem().getAnalogsCount() > 0 && xmlConfigDfr.getDataPool().getAlgorithms() != null)
					  {
						  logger.debug("Analog systems ");
						  Trigger[] trigger = xmlConfigDfr.getDataPool().getAlgorithms().getTriggers().getTriggerArray();
						  logger.debug("Trigger size "+trigger.length);
						  for (int i = 0; i < trigger.length; i++) {
							  
							  logger.debug("trigger[i].getType() "+trigger[i].getType()+" trigger[i].getTriggerLimit() "+trigger[i].getTriggerLimit()+" maxTriggerLimit "+maxTriggerLimit);
								if ((trigger[i].getType() != null && !trigger[i].getType().equalsIgnoreCase("E")) || (trigger[i].getTriggerLimit() > 0))
								{
									if (trigger[i].getTriggerLimit() > maxTriggerLimit)
									{
										maxTriggerLimit = trigger[i].getTriggerLimit();
										logger.debug("maxTriggerLimit calculated "+maxTriggerLimit);
									}
								}
							  }
					  }
				  }
				  logger.debug("Returning maxTriggerLimit "+maxTriggerLimit);
				  return maxTriggerLimit;
			}

			/**
			 * @return the stationDto
			 */
			public StationDTO getStationDto() {
				if (stationDto == null)
				{
					populateStationDetails();
				}
				return stationDto;
			}

			/**
			 * @param stationDto the stationDto to set
			 */
			public void setStationDto(StationDTO stationDto) {
				this.stationDto = stationDto;
			}

			/**
			 * @return the lstAnalogChannels
			 */
			public List<AnalogChannelDTO> getLstAnalogChannels() {
				if (lstAnalogChannels == null || lstAnalogChannels.isEmpty())
				{
					lstAnalogChannels = populateAnalogChannels();
				}

				return lstAnalogChannels;
			}

			/**
			 * @param lstAnalogChannels the lstAnalogChannels to set
			 */
			public void setLstAnalogChannels(List<AnalogChannelDTO> lstAnalogChannels) {
				this.lstAnalogChannels = lstAnalogChannels;
			}


			/**
			 * @return the lineGroupsConfigured
			 */
			public boolean isLineGroupsConfigured() {
				return lineGroupsConfigured;
			}

			/**
			 * @param lineGroupsConfigured the lineGroupsConfigured to set
			 */
			public void setLineGroupsConfigured(boolean lineGroupsConfigured) {
				this.lineGroupsConfigured = lineGroupsConfigured;
			}

			/**
			 * @return the lstEventChannels
			 */
			public List<EventChannelDTO> getLstEventChannels() {
				if (lstEventChannels == null || !lstEventChannels.isEmpty())
				{
					populateEventDTO();
				}
				return lstEventChannels;
			}

			/**
			 * @param lstEventChannels the lstEventChannels to set
			 */
			public void setLstEventChannels(List<EventChannelDTO> lstEventChannels) {
				this.lstEventChannels = lstEventChannels;
			}

			public ArrayList<AnalogChannelDTO> getLstVirtualChannels() {
				if (lstVirtualChannels == null || lstVirtualChannels.isEmpty())
				{
					try {
						populateVirtualChannelsDTO();
					} catch (M9000Exception e) {
						lstVirtualChannels = null;
						logger.error("Error in getting virtual channel details ",e);
					}
				}
				return lstVirtualChannels;
			}

			public void setLstVirtualChannels(ArrayList<AnalogChannelDTO> lstVirtualChannels) {
				this.lstVirtualChannels = lstVirtualChannels;
			}
}
