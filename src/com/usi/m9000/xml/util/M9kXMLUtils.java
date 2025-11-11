package com.usi.m9000.xml.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlOptions;

import com.usi.AlgorithmsDocument.Algorithms;
import com.usi.AnalogChannelsDocument.AnalogChannels;
import com.usi.AnalogsDocument.Analogs;
import com.usi.DFRDocument.DFR;
import com.usi.EventInputDocument.EventInput;
import com.usi.EventsDocument.Events;
import com.usi.ExportDocument2.Export;
import com.usi.ExportsDocument.Exports;
import com.usi.FrequencyDocument.Frequency;
import com.usi.GlobalLineGroupDocument.GlobalLineGroup;
import com.usi.GlobalLineGroupsDocument.GlobalLineGroups;
import com.usi.LimitsDocument.Limits;
import com.usi.LineGroupDocument.LineGroup;
import com.usi.MagnitudeDocument.Magnitude;
import com.usi.MeasurementDocument.Measurement;
import com.usi.MeasurementsDocument.Measurements;
import com.usi.NegativeSequenceDocument.NegativeSequence;
import com.usi.PhaseDocument.Phase;
import com.usi.PmuInputDocument.PmuInput;
import com.usi.PmuInputsDocument.PmuInputs;
import com.usi.PositiveSequenceDocument.PositiveSequence;
import com.usi.PowerDocument.Power;
import com.usi.RmsDocument.Rms;
import com.usi.SubStationDocument.SubStation;
import com.usi.TriggerDocument.Trigger;
import com.usi.TriggersDocument.Triggers;
import com.usi.ZeroSequenceDocument.ZeroSequence;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.ChannelInfo;
import com.usi.m9000.config.DigitalInfo;
import com.usi.m9000.config.StationInfo;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.LineGroupChannelsDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.TriggerChannelDTO;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.xml.M9000XmlConfig;

public class M9kXMLUtils {
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kXMLUtils.class);
	static Map<String, DFR> mapXMLDfrs;
	private static M9000XmlConfig m9kConfig = null;
	private static ArrayList<LineGroupsAlgorithm> lstLineGroups = null;
	  public static List<DfrDTO> populateDFRsFromXML(SubStation substation)
		{
			  int nextStartAnalogIndex = -1;
			  int nextStartDigitalIndex = -1;
			  int analogCnt;
			  int digitalCnt;
			  Analogs analogChannels;
			  Events eventChannels;
			  int physicalAnalogsCount;
			  int start;
//			logger.info("Substation..."+substation);
			List<DfrDTO> lstDfrs = new ArrayList<DfrDTO>(substation.getDFRs().sizeOfDFRArray());
			DfrDTO dfrDto;
			DFR[] xmlDfrs = substation.getDFRs().getDFRArray();
//			logger.debug("Total DFRs.."+substation.getDFRs().sizeOfDFRArray());
			for (int i = 0; i < xmlDfrs.length; i++) {
				dfrDto = new DfrDTO();
				dfrDto.setDfrId(xmlDfrs[i].getSystem().getDfrId());
				dfrDto.setDfrName("DFR"+xmlDfrs[i].getSystem().getDfrId());
//				logger.debug("Name: "+dfrDto.getDfrName());
				dfrDto.setIpAddress(xmlDfrs[i].getIPAddress());
//				logger.debug("IPAddress: "+dfrDto.getIpAddress());
				dfrDto.setStatus("COMPLETE");
				analogChannels = xmlDfrs[i].getDataPool().getChannels().getAnalogs();
				eventChannels = xmlDfrs[i].getDataPool().getChannels().getEvents();
				
				if (eventChannels != null)
				{
					digitalCnt = eventChannels.sizeOfEventInputArray();
					dfrDto.getChnlConfigDropDownList().setDigitalCnt(digitalCnt);
					// START: 05-Jun-2020 - Mini
					if (digitalCnt % M9kConstants.NO_OF_DIGITAL_CHNLS_PER_BOARD > 0)
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

				if (analogChannels != null)
				{
					physicalAnalogsCount = 0;
					analogCnt = analogChannels.sizeOfAnalogInputArray();
					dfrDto.getChnlConfigDropDownList().setAnalogCnt(analogCnt);
					start = dfrDto.getAnalogChannelStart();
					dfrDto.setLstAnalogChannelNames(new ArrayList<String>(analogCnt));
			    	dfrDto.setLstAnalogChannels(new ArrayList<AnalogChannelDTO>(analogCnt));
					for (int j = 0; j < analogCnt; j++) {
			    		if (analogChannels.getAnalogInputArray(j).isSetVirtual())
			    		{
			    			dfrDto.getLstAnalogChannelNames().add(M9kConstants.VIRTUAL_ANALOG+(start++)+"-"+analogChannels.getAnalogInputArray(j).getCircuitName());
			    		}
			    		else
			    		{
			    			physicalAnalogsCount++;
			    			dfrDto.getLstAnalogChannelNames().add("A"+(start++)+"-"+analogChannels.getAnalogInputArray(j).getCircuitName());
			    		}
					}
					dfrDto.setPhysicalAnalogsCount(physicalAnalogsCount);
					if (nextStartAnalogIndex == -1)
					{
						dfrDto.setAnalogChannelStart(1);
						dfrDto.setAnalogChannelEnd(analogCnt);
						// START: 05-Jun-2020 - Mini
							nextStartAnalogIndex = physicalAnalogsCount+1;
						// END: 18-Jun-2020 - Mini
					}
					else 
					{
						dfrDto.setAnalogChannelStart(nextStartAnalogIndex);
						dfrDto.setAnalogChannelEnd(nextStartAnalogIndex + analogCnt - 1);
						// START: 05-Jun-2020 - Mini
						nextStartAnalogIndex += physicalAnalogsCount;
						// END: 18-Jun-2020 - Mini
					}
					
				}
				else
				{
					dfrDto.getChnlConfigDropDownList().setAnalogCnt(0);
				}
				logger.debug("Dfrs added to the list..."+dfrDto);
				lstDfrs.add(dfrDto);
			}
			return lstDfrs;
		}	  

	  public static List<DfrDTO> getDfrsFromStation(int stationId)
	  {
			M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
			StationDAO mysqlStationDao = m9kDAOFactory.getStationDAO();

		List<DfrDTO> lstScopeDfrs = null;
		  try {
			  String configXml = mysqlStationDao.getConfigXml(stationId);
			  M9000XmlConfig m9kConfig = new M9000XmlConfig(new StringReader(configXml));
			lstScopeDfrs = populateScopeDFRsFromXML(m9kConfig.getSubstation());
		} catch (M9000Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lstScopeDfrs;
	  }

	  
	  public static List<DfrDTO> getDfrsFromStation(URLConnection stationFileUrlConnection)
		  {
			List<DfrDTO> lstScopeDfrs = null;
//			  stationConfigFile =new File("M9kConfig/"+stationId+".xml");
			  
			  logger.debug("Selected Station File in getDfrsFromStation...");
			  try {
				M9000XmlConfig m9kConfig = new M9000XmlConfig(stationFileUrlConnection.getInputStream());
				lstScopeDfrs = populateScopeDFRsFromXML(m9kConfig.getSubstation());
			} catch (M9000Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return lstScopeDfrs;
		  }

		public static List<DfrDTO> populateScopeDFRsFromXML(SubStation substation) throws Exception
		{
			  int nextStartAnalogIndex = -1;
			  int nextStartDigitalIndex = -1;
			  int analogCnt;
			  int digitalCnt;
			  Analogs analogChannels;
			  Events eventChannels;
			  int start;
			List<DfrDTO> lstDfrs = new ArrayList<DfrDTO>(substation.getDFRs().sizeOfDFRArray());
			DfrDTO dfrDto;
			DFR[] xmlDfrs = substation.getDFRs().getDFRArray();
			int physicalAnalogsCount;
			logger.debug("Total DFRs.."+substation.getDFRs().sizeOfDFRArray());
			for (int i = 0; i < xmlDfrs.length; i++) {
				dfrDto = new DfrDTO("DFR"+xmlDfrs[i].getSystem().getDfrId());
				logger.debug("Name: "+dfrDto.getDfrName());
				dfrDto.setDfrId(xmlDfrs[i].getSystem().getDfrId());
				dfrDto.setIpAddress(xmlDfrs[i].getIPAddress());
				dfrDto.setLineFreq(xmlDfrs[i].getSystem().getLineFrequency());
				dfrDto.setSampleRate(xmlDfrs[i].getSystem().getSampleRate());
				logger.debug("IPAddress: "+dfrDto.getIpAddress());
		    	analogChannels = xmlDfrs[i].getDataPool().getChannels().getAnalogs();
		    	eventChannels = xmlDfrs[i].getDataPool().getChannels().getEvents();

				if (eventChannels != null)
				{
					digitalCnt = eventChannels.sizeOfEventInputArray();
					dfrDto.setDigitalChnlCnt(digitalCnt);
					// START: 05-Jun-2020 - Mini
					if (digitalCnt == M9kConstants.NO_OF_DIGITAL_CHNLS_PER_ANEV_BOARD)
					{
						dfrDto.setAnevCard(true);
					}
					// END: 15-Jun-2020 - Mini
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
			    	for (int j = 0; j < eventChannels.sizeOfEventInputArray(); j++) {
			    		dfrDto.getLstEventChannelNames().add(eventChannels.getEventInputArray(j).getEventName());
					}

				}
				else
				{
					dfrDto.setDigitalChnlCnt(0);
				}

				if (analogChannels != null)
				{
					physicalAnalogsCount = 0;
					analogCnt = analogChannels.sizeOfAnalogInputArray();
					dfrDto.setAnalogChnlCnt(analogCnt);
			    	start = dfrDto.getAnalogChannelStart();
			    	for (int j = 0; j < analogChannels.sizeOfAnalogInputArray(); j++) {
			    		if (analogChannels.getAnalogInputArray(j).isSetVirtual())
			    		{
			    			dfrDto.getLstAnalogChannelNames().add(M9kConstants.VIRTUAL_ANALOG+(start++)+"-"+analogChannels.getAnalogInputArray(j).getCircuitName());
			    		}
			    		else
			    		{
			    			physicalAnalogsCount++;
			    			dfrDto.getLstAnalogChannelNames().add("A"+(start++)+"-"+analogChannels.getAnalogInputArray(j).getCircuitName());
			    		}
					}
			    	dfrDto.setPhysicalAnalogsCount(physicalAnalogsCount);
					if (nextStartAnalogIndex == -1)
					{
						dfrDto.setAnalogChannelStart(1);
						dfrDto.setAnalogChannelEnd(analogCnt);
						// START: 05-Jun-2020 - Mini
							nextStartAnalogIndex = physicalAnalogsCount + 1;
						// END: 18-Jun-2020 - Mini
					}
					else 
					{
						dfrDto.setAnalogChannelStart(nextStartAnalogIndex);
						dfrDto.setAnalogChannelEnd(nextStartAnalogIndex + analogCnt - 1);
						// START: 05-Jun-2020 - Mini
							nextStartAnalogIndex += physicalAnalogsCount;
						// END: 15-Jun-2020 - Mini
					}
			    	dfrDto.setLstAnalogChannelNames(new ArrayList<String>(analogChannels.sizeOfAnalogInputArray()));
				}
				else
				{
					dfrDto.setAnalogChnlCnt(0);
				}
				logger.debug("Dfrs added to the list..."+dfrDto);
//				if (dfrDto.getAnalogChnlCnt() > 0)
//				{
					lstDfrs.add(dfrDto);
//				}
			}
			return lstDfrs;
		}

		
		public static StationInfo getStationDetails(String stationId, int dfrId)
		  {
			M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
			StationDAO mysqlStationDao = m9kDAOFactory.getStationDAO();
			StationInfo stationInfo = new StationInfo();
			  File stationConfigFile;
			  stationConfigFile =new File("c:/M9kConfig/"+stationId+".xml");
			  logger.debug("Selected Station File..."+stationConfigFile);
			  try {
				  String configXml = mysqlStationDao.getConfigXml(Integer.parseInt(stationId));
				  M9000XmlConfig m9kConfig = new M9000XmlConfig(new StringReader(configXml));				  
				DFR dfr = getDfr(m9kConfig.getSubstation(), dfrId);
				stationInfo.setStationName(dfr.getSystem().getStationName());
				stationInfo.setRecordingDeviceId(dfr.getSystem().getRecordingDeviceId());
				stationInfo.setComtradeStdRevYear(M9kConstants.COMTRADE_REV_YEAR);
				stationInfo.setAnalogChannelsCount(dfr.getDataPool().getChannels().getAnalogs().sizeOfAnalogInputArray());
				stationInfo.setDigitalChannelsCount(dfr.getDataPool().getChannels().getEvents().sizeOfEventInputArray());
				stationInfo.setChannelsCount(dfr.getDataPool().getChannels().getAnalogs().sizeOfAnalogInputArray() +
						dfr.getDataPool().getChannels().getEvents().sizeOfEventInputArray() );
				stationInfo.setNoOfSamplingRates(M9kConstants.COMTRADE_NO_OF_SAMPLING_RATES);
				
				
			} catch (M9000Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return stationInfo;
		  }		

		public static StationInfo getStationDetails(StationDTO stationDto, int dfrId)
		  {
			StationInfo stationInfo = new StationInfo();
			  try {
				  M9000XmlConfig m9kConfig = new M9000XmlConfig(new URL("http://195.1.1.21:8080/M9000Master/"+stationDto.getStationDisplayName()+".xml").openStream());
				DFR dfr = getDfr(m9kConfig.getSubstation(), dfrId);
				stationInfo.setStationName(dfr.getSystem().getStationName());
				stationInfo.setRecordingDeviceId(dfr.getSystem().getRecordingDeviceId());
				stationInfo.setComtradeStdRevYear(M9kConstants.COMTRADE_REV_YEAR);
				stationInfo.setAnalogChannelsCount(dfr.getDataPool().getChannels().getAnalogs().sizeOfAnalogInputArray());
				stationInfo.setDigitalChannelsCount(dfr.getDataPool().getChannels().getEvents().sizeOfEventInputArray());
				stationInfo.setChannelsCount(dfr.getDataPool().getChannels().getAnalogs().sizeOfAnalogInputArray() +
						dfr.getDataPool().getChannels().getEvents().sizeOfEventInputArray() );
				stationInfo.setNoOfSamplingRates(M9kConstants.COMTRADE_NO_OF_SAMPLING_RATES);
				
				
			} catch (M9000Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return stationInfo;
		  }		

		private static DFR getDfr(SubStation substation, int dfrId)
		{
			DFR dfr = null;
			int i = 0;
			while (i < substation.getDFRs().sizeOfDFRArray())
			{
				if (substation.getDFRs().getDFRArray(i).getSystem().getDfrId() == dfrId)
				{
					dfr = substation.getDFRs().getDFRArray(i);
					break;
				}
				i++;
			}
			return dfr;
		}
		
		
		@SuppressWarnings("unused")
		private static Collection<ChannelInfo> parseDigitalChannelsInfo(StationInfo stationInfo, DFR dfr) throws M9000Exception
		{
			int chnlCnt = 0;
			DigitalInfo digitalChannelInfo;
			EventInput[] eventInputs;
			Collection<ChannelInfo> digitalInfoList = new ArrayList<ChannelInfo>();
			// Process Analog Channels Info
			try {
				eventInputs = dfr.getDataPool().getChannels().getEvents().getEventInputArray();
				while (chnlCnt < stationInfo.getDigitalChannelsCount())
				{
					digitalChannelInfo = new DigitalInfo(eventInputs[chnlCnt].getChannel());
					digitalChannelInfo.setChnlId("E"+eventInputs[chnlCnt].getChannel());
					logger.debug("Digital Channel ID.."+eventInputs[chnlCnt].getChannel());
// TODO: Update XML Parser with Phase and EventName for event channels
					digitalChannelInfo.setPhaseId("");
					digitalChannelInfo.setCircuitName(eventInputs[chnlCnt].getEventName());
					logger.debug("Digital Circuit Name..."+eventInputs[chnlCnt].getEventName());
					digitalChannelInfo.setStatus(Integer.parseInt(eventInputs[chnlCnt].getNormalState()));
					digitalInfoList.add(digitalChannelInfo);
					chnlCnt++;
				}
				stationInfo.setDigitalInfoCollection(digitalInfoList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return digitalInfoList;
		}
		
		public static boolean deleteMeasurement(SubStation currentSubstation, TriggerChannelDTO triggerChannelDTO, Map<String, DFR> mapXMLDfrsSrc)
		{
			boolean deleteStatus = false;
			mapXMLDfrs = mapXMLDfrsSrc;
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			Algorithms algorithms = xmlDfr.getDataPool().getAlgorithms();
			Exports exports = xmlDfr.getDataPool().getExports();
			Export[] arrExport = exports.getExportArray();
			PmuInputs pmuInputs = currentSubstation.getPmus().getPmuArray(0).getPmuInputs();
			PmuInput[] arrPmuInput = pmuInputs.getPmuInputArray();
			Measurements measurements = currentSubstation.getMeasurements();
			Measurement[] arrMeasurement = measurements.getMeasurementArray();
			for (int i = 0; i < arrMeasurement.length; i++) {
				if (arrMeasurement[i].getId() == triggerChannelDTO.getId())
				{
					measurements.removeMeasurement(i);
					deleteStatus = true;
				}
			}
			for (int i = 0; i < arrPmuInput.length; i++) {
				if (arrPmuInput[i].getId() == triggerChannelDTO.getId())
				{
					pmuInputs.removePmuInput(i);
					deleteStatus = true;
				}
			}

			for (int i = 0; i < arrExport.length; i++) {
				if (arrExport[i].getId() == triggerChannelDTO.getId())
				{
					exports.removeExport(i);
					deleteStatus = true;
				}
			}
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
							for (int j = 0; j < arrLimits.length; j++) {
								if (arrLimits[j].getName().equalsIgnoreCase(triggerChannelDTO.getMeasurementName()))
								{
									String inputValue = arrLimits[i].getInput();
									int index = inputValue.indexOf("/")+1;
									String xmlAlgoType = inputValue.substring(index, inputValue.indexOf("/", index));
									if (xmlAlgoType.startsWith(M9kConstants.RMS))
									{
//										triggerChannelDto.setType(xmlAlgoType.substring(0, xmlAlgoType.indexOf("(")));
										deleteRmsFromXml(triggerChannelDTO);
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
									algorithms.removeLimits(j);
									deleteStatus = true;
								}
							}
							triggers.removeTrigger(i);
							deleteStatus = true;
							break;
						}
					}
				}
			}
			
			return deleteStatus;
		}		
		
		
		private static void deleteRmsFromXml(TriggerChannelDTO triggerChannelDTO) {
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

		private static void deleteMagnitudeFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			Magnitude[] arrMagnitude = xmlDfr.getDataPool().getAlgorithms().getMagnitudeArray();
			for (int i = 0; i <arrMagnitude.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrMagnitude[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removeMagnitude(i);
					break;
				}
			}
		}

		private static void deleteFrequencyFromXml(TriggerChannelDTO triggerChannelDTO) {
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
		
		private static void deletePhaseFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			Phase[] arrPhase = xmlDfr.getDataPool().getAlgorithms().getPhaseArray();
			for (int i = 0; i <arrPhase.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrPhase[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removePhase(i);
					break;
				}
			}
		}
		
		private static void deleteZeroSequenceFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			ZeroSequence[] arrZeroSequence = xmlDfr.getDataPool().getAlgorithms().getZeroSequenceArray();
			for (int i = 0; i <arrZeroSequence.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrZeroSequence[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removeZeroSequence(i);
					break;
				}
			}
		}
		
		private static void deletePositiveSequenceFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			PositiveSequence[] arrPositiveSequence = xmlDfr.getDataPool().getAlgorithms().getPositiveSequenceArray();
			for (int i = 0; i <arrPositiveSequence.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrPositiveSequence[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removePositiveSequence(i);
					break;
				}
			}
		}
		
		private static void deleteNegativeSequenceFromXml(TriggerChannelDTO triggerChannelDTO) {
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
		
		private static void deletePowerFromXml(TriggerChannelDTO triggerChannelDTO) {
			DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
			Power[] arrPower = xmlDfr.getDataPool().getAlgorithms().getPowerArray();
			for (int i = 0; i <arrPower.length; i++) {
				if (triggerChannelDTO.getMeasurementName().equals(arrPower[i].getName()))
				{
					xmlDfr.getDataPool().getAlgorithms().removePower(i);
					break;
				}
			}
		}
	
		private static List<LineGroupsAlgorithm> populateLineGroups(String configXml) throws M9000Exception {
			LineGroupsAlgorithm lineGroups;

			String dfrKey= null;
			lstLineGroups = new ArrayList<LineGroupsAlgorithm>();

			if (m9kConfig == null)
			{
				m9kConfig  = new M9000XmlConfig();
				
				m9kConfig.loadStationXmlFile(new StringReader(configXml));
			}

			DFR[] xmlDfrs = m9kConfig.getSubstation().getDFRs().getDFRArray();
//			System.out.println("Entered M9kStationXMLUtil populateLineGroups xmlDfrs.length "+xmlDfrs.length);
			for (int j = 0; j < xmlDfrs.length; j++) {
				if (xmlDfrs[j].getDataPool().getLineGroups() != null) {
					LineGroup xmlLineGroup[] = xmlDfrs[j].getDataPool()
							.getLineGroups().getLineGroupArray();
//					System.out.println("M9kStationXMLUtil: xmlLineGroup.length "+xmlLineGroup.length);
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
						// END: 16-Oct-2019

//						lineGroups.setType(type);
						lineGroups.setLocal(true);
						lineGroups.setChassis(dfrKey);
//						System.out.println("M9kStationXMLUtil: Line Group Name "+xmlLineGroup[i].getLineGroupName());
						lineGroups.setLineGroupName(xmlLineGroup[i].getLineGroupName());
						lstLineGroups.add(lineGroups);
					}
				}
			}
			  GlobalLineGroups globalLineGroups = m9kConfig.getSubstation().getGlobalLineGroups();
			  
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
							if (!arrGlobalLineGroup[i].getDecisionLogic().trim().equalsIgnoreCase("NONE"))
							{
								lineGroups.setDecisionLogic(arrGlobalLineGroup[i].getDecisionLogic());
							}
							else
							{
								lineGroups.setDecisionLogic("");
							}
							// START: 16-Oct-2019 Implemented a comments section for line group where customer can add custom name value pair (Georgia Power)  
							if (arrGlobalLineGroup[i].getComments() != null && !arrGlobalLineGroup[i].getComments().isEmpty())
							{
								lineGroups.setComments(arrGlobalLineGroup[i].getComments());
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
//							populateRMSExportForLineGroupChannels(lgInput);
//							type = getLineGroupType(lgInput);
//							lineGroups.setType(type);
							lineGroups.setLocal(false);
							lstLineGroups.add(lineGroups);					
					  }
//					  logger.debug("Total Line Groups available"+lstLineGroups.size() );
				  }
				  
			  }
			  

			  logger.debug("Returning from  editLineGroups "+lstLineGroups);
			return lstLineGroups;
		}

		/**
		 * @return the lstLineGroups
		 * @throws M9000Exception 
		 */
		public static ArrayList<LineGroupsAlgorithm> getLstLineGroups(String configXml) throws M9000Exception {
			if (lstLineGroups == null)
			{
				populateLineGroups(configXml);
			}
			return lstLineGroups;
		}

		/**
		 * @param lstLineGroups the lstLineGroups to set
		 */
		public static void setLstLineGroups(ArrayList<LineGroupsAlgorithm> lstLineGroups) {
			M9kXMLUtils.lstLineGroups = lstLineGroups;
		}

		public static void reset() {
			lstLineGroups = null;
			m9kConfig = null;
			
		}
		
		public static List<String> getMeasurementTypes() throws M9000Exception
		{
			List<String> lstMeasurementTypes = null;
			String configXml = M9kUtils.getStationDetails().getConfigXml();
			try {
				M9000XmlConfig m9kConfig = new M9000XmlConfig(new StringReader(configXml));
				lstMeasurementTypes = m9kConfig.getLstOfExportedMeasurementNames();
			} catch (M9000Exception e) {
				throw e;
			}
			return lstMeasurementTypes;
		}
		
		public static int getTheMaximumTriggerLimitFromConfig() throws M9000Exception {
			int maxTriggerLimit = 0;
			String configXml = M9kUtils.getStationDetails().getConfigXml();
			try {
				M9000XmlConfig m9kConfig = new M9000XmlConfig(new StringReader(configXml));
				maxTriggerLimit = m9kConfig.getTheMaximumTriggerLimitFromConfig();
				// Since its in milli seconds, let's convert to seconds before we return
				maxTriggerLimit/=1000;
				
			} catch (M9000Exception e) {
				throw e;
			}
			return maxTriggerLimit;
		}
	
		public static XmlOptions getXmlOptions()
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
}
