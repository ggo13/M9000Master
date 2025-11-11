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
import org.apache.xmlbeans.XmlOptions;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.AlgorithmsDocument.Algorithms;
import com.usi.AnalogChannelsDocument.AnalogChannels;
import com.usi.AnalogInputDocument.AnalogInput;
import com.usi.DFRDocument.DFR;
import com.usi.ExportDocument2.Export;
import com.usi.ExportsDocument.Exports;
import com.usi.GlobalLineGroupDocument.GlobalLineGroup;
import com.usi.GlobalLineGroupsDocument.GlobalLineGroups;
import com.usi.LgAnalogsDocument.LgAnalogs;
import com.usi.LimitsDocument.Limits;
import com.usi.LineGroupDocument.LineGroup;
import com.usi.LineGroupsDocument.LineGroups;
import com.usi.MeasurementDocument.Measurement;
import com.usi.MeasurementsDocument.Measurements;
import com.usi.NegativeSequenceDocument.NegativeSequence;
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
import com.usi.m9000.algorithms.RMSAlgorithm;
import com.usi.m9000.dao.StationDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.EventChannelDTO;
import com.usi.m9000.dto.LineGroupChannelsDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.TriggerChannelDTO;
import com.usi.m9000.dto.TriggerSourceDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.M9kXMLConstants;

public class ManageLineGroupsAction extends ActionSupport implements SessionAware, ServletContextListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StationDTO stationDetails;
	private String selectedLineGroup;
//	private String lineGroupName;
	private String btnSubmit;
	private String btnEdit;
	private Map<String, Object>  session;
	private List<LineGroupsAlgorithm> lstLineGroups;
	List<String> lstChannelsInLineGroup;
	List<AnalogChannelDTO> lstAnalogChannels;
	List<AnalogChannelDTO> lstAvailableAnalogChannels;
	List<TriggerChannelDTO> lstTriggerChannels;
	List<TriggerSourceDTO> lstTriggerSourceDto;
	List<EventChannelDTO> lstEventChannels;
	List<DfrDTO> lstDfrDTO;
	List<String> availableChannels;
	int selectedLineGroupIndex;
	int currentLineGroupIndex;
//	int previousSelectedLineGroupIndex;
	List<String> lineGroupDetails;
//	DFR xmlConfigDfr;
	String accessMode;
//	String triggerOutHold;
	String sourceTab;
	Map<String, DFR> mapXMLDfrs;
	SubStation currentSubstation;
	String actionType = "NEW";
	boolean booAnalog;
	boolean booDigital;
	Boolean decisionLogic = null;
	List<TriggerChannelDTO> lstEventTriggerChannels;
	List<TriggerChannelDTO> lstAllTriggerChannels;
	LineGroupsAlgorithm editedLineGroup; // used for editing line group details 
	private Boolean lineGroupExists;
	 UsersDTO userDto;
	 List<Object> lstAllTriggerTypes;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ManageLineGroupsAction.class);
	PropertiesConfiguration config;
	List<AnalogChannelDTO> lstVirtualChannels;
	private List<String> lstAffectedLineGroups;
	List<AnalogChannelDTO> lstExistingAnalogChannels;
	// 31-Mar-2021 Track measurements associated whenever line group is modified
	List<TriggerChannelDTO> lstAssociatedMeasurements;
	private List<String> lstLinegroupsModified;
	public ManageLineGroupsAction()
	{
	}

	  public String execute()throws Exception{
		  String returnValue = "ManageLineGroups";
		  logger.debug("lstLineGroups after entering managelinegroups..."+lstLineGroups);
		  logger.debug("LineGroupDetails "+lineGroupDetails);
		  logger.debug("@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());
		  logger.debug("getBtnSubmit()..."+getBtnSubmit());
		  logger.debug("IS there any associated measurements "+getLstAssociatedMeasurements());
			booAnalog = (Boolean)session.get("booAnalog");
			booDigital = (Boolean)session.get("booDigital");
			lineGroupExists = (Boolean)session.get("lineGroupExists");
		  if (getSourceTab() == null) 
		  {
			sourceTab = (String) session.get("sourceTab");  
		  }
		  if (getSourceTab() != null && !getSourceTab().isEmpty())
		  {
			  logger.debug("decisionLogic inside ManageLineGroupsAction "+isBooAnalog());
			  if (isDecisionLogic() != null)
			  {
				  if (isDecisionLogic())
				  {
					  logger.debug("Traversed from Decision Logic");
				  }
				  else
				  {
					if (lstLineGroups != null) {
						updateLineGroupDetailsBackToXMLConfig();
					}
				  }
			  }
			  logger.debug("In Execute method....lstLineGroups "+lstLineGroups);
			  session.put("sourceTab", sourceTab);
			  if (getSourceTab().equalsIgnoreCase("StationDetails"))
				{
				  returnValue="StationDetails";
				}
			  else if (getSourceTab().equalsIgnoreCase("Analogs"))
			  {
				  returnValue = "Analogs";
			  }
			  else if (getSourceTab().equalsIgnoreCase("VirtualChannels"))
				{
				  returnValue=back();
				}
			  
			  else
			  {
				  lstLineGroups = editLineGroups();
				  if (lstLineGroups != null && !lstLineGroups.isEmpty())
				  {
					  session.put("lstLineGroups", lstLineGroups);
				  }
			  
				  if (getSourceTab().equalsIgnoreCase("Measurements") 
						  || (getLstAssociatedMeasurements() != null && !getLstAssociatedMeasurements().isEmpty())
						  || (getLstLinegroupsModified() != null && !getLstLinegroupsModified().isEmpty()))
				  {
					  logger.debug("Measurements affected due to save or delete of line group");
					  session.put("sourceTab", "Measurements");
					  returnValue = configureTriggers();
				  }// Anything forwarded beyond linegroups should be validated for measurements 
				// START: 10-Mar-2021 - Virtual Measurement implementation
				  else if (getSourceTab().equalsIgnoreCase("VirtualMeasurements"))
				{
					returnValue="VirtualMeasurements";
				}
				// END: 10-Mar-2021 - Virtual Measurement implementation
				  else if (getSourceTab().equalsIgnoreCase("Events"))
					{
					  returnValue = "Events";
					}
				  else if (getSourceTab().equalsIgnoreCase("FaultLocation"))
				  {
					  logger.info("User "+(getUserDto()!=null?getUserDto().getUserName():"")+" is forwarded via Line groups screen");
					  returnValue = "FaultLocation";
				  }
			  }
		  }
		  
		  
//		  if (getLstLineGroups() != null)
//		  {
//			  if (session.get("previousSelectedLineGroupIndex") != null)
//			  {
//				  previousSelectedLineGroupIndex = Integer.parseInt(session.get("previousSelectedLineGroupIndex").toString());
//				  logger.debug("previousSelectedLineGroupIndex from session..."+previousSelectedLineGroupIndex);
//			  }
//			  if(getLineGroupDetails() != null && !getLineGroupDetails().isEmpty())
//			  {
//				  logger.debug("previousSelectedLineGroupIndex before update..."+previousSelectedLineGroupIndex);
//				  LineGroupsAlgorithm lineGroupsAlgorithm = getLstLineGroups().get(previousSelectedLineGroupIndex);
//				  updateLineGroupChannel(lineGroupsAlgorithm);
//			  }
//			  selectedLineGroupIndex = calculateSelectedLineGroupIndex();
////			  previousSelectedLineGroupIndex = selectedLineGroupIndex;
////			  session.put("previousSelectedLineGroupIndex", previousSelectedLineGroupIndex);
//			  logger.debug("Entered ManageLineGroupsAction"+getSelectedLineGroup());
//			  logger.debug("lineGroupDetails..."+getLineGroupDetails());
//			  logger.debug("availableChannels..."+getAvailableChannels());
//			  logger.debug("lstLine groups..."+getLstLineGroups());
//			  lstAvailableAnalogChannels = getLstAvailableAnalogChannels();
//			  logger.debug("selectedLineGroupIndex..."+selectedLineGroupIndex);
//			  session.put("selectedLineGroupIndex", selectedLineGroupIndex);
//			  session.put("lstAvailableAnalogChannels", getLstAvailableAnalogChannels());
//		  }
		  logger.debug("Returning value..."+returnValue);
		  
		  return returnValue;

	  }

	  
		public String configureLineGroups() {
			String returnValue = SUCCESS;
			logger.debug("Did it enter here in Save method configureLineGroups()? ");
//			logger.info("AnalogInput " + getLstAnalogChannels());
//			logger.info("Analog from session..." + lstAnalogChannels);
//			xmlConfigDfr = (DFR) session.get("SelectedDfrObj");
			stationDetails = (StationDTO) session.get("stationDetails");
			
			logger.debug("Access Mode "+getAccessMode());
			if (getAccessMode() != null && getAccessMode().equalsIgnoreCase("Edit")) {
				if (getLstLineGroups() == null || getLstLineGroups().isEmpty())
				{
					lstLineGroups = editLineGroups();
				}
				if (getLstAffectedLineGroups() != null && !getLstAffectedLineGroups().isEmpty())
				{
					setSelectedLineGroup(getLstAffectedLineGroups().get(0).substring(getLstAffectedLineGroups().get(0).indexOf("-")+1,getLstAffectedLineGroups().get(0).lastIndexOf("-")));
					logger.debug("Set selected linegroup to first in the list of affected linegroups");
				}
//				if (lstLineGroups !=null && !lstLineGroups.isEmpty())
//				{
//					checkAndUpdateLineGroups();
//				}
//				else
//				{
//					lstLineGroups = createLineGroups();
//				}
//				if ((lstLineGroups == null || lstLineGroups.isEmpty())
//						&& getLstAnalogChannels() != null) {
//					lstLineGroups = createLineGroups();
//				}
			} else {
				lstLineGroups = createLineGroups();
			}
			if ((lstLineGroups == null || lstLineGroups.isEmpty())
					&& getLstAnalogChannels() != null) {
				session.put("lstAnalogChannels", getLstAnalogChannels());
			} else {
//				if (getAccessMode() == null
//						|| !getAccessMode().equalsIgnoreCase("Edit")) {
//				GlobalLineGroups globalLineGroups = currentSubstation.getGlobalLineGroups();
//				while (globalLineGroups != null && globalLineGroups.sizeOfGlobalLineGroupArray() > 0) {
//					globalLineGroups.removeGlobalLineGroup(0);
//				}
//
//					populateLineGroups();
//				}
				session.put("lstAnalogChannels", getLstAnalogChannels());
				session.put("lstLineGroups", lstLineGroups);
//				session.put("selectedLineGroupIndex", 0);
				selectedLineGroupIndex = 0;
				session.put("lstAvailableAnalogChannels",
						getLstAvailableAnalogChannels());
				
			}

			logger.debug("Returning value..." + returnValue);
			return returnValue;

		}
	  
	  public String showLineGroupDetails() throws Exception
	  {
		  String returnValue = "LineGroupDetail";
		  String flagEdit = (String) session.get("flagEdit");
		  editedLineGroup = (LineGroupsAlgorithm) session.get("editedLineGroup");
		  String newLineGroup = (String) session.get("NewLineGroup");
		  logger.debug("Decision logic "+lstLineGroups.get(selectedLineGroupIndex).getDecisionLogic());
//		  logger.debug("@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());
		  logger.debug("Edited line group Name: "+getSelectedLineGroup());
//		  logger.debug("After session...@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());
		  logger.debug("newLineGroup in showLineGroupDetails()... "+newLineGroup);
		  if (newLineGroup != null && newLineGroup.equalsIgnoreCase("true"))
		  {
			  returnValue = "NewLineGroupDetail";
			  selectedLineGroupIndex = lstLineGroups.size()-1;
//			  selectedLineGroupIndex = Integer.parseInt(session.get("selectedLineGroupIndex").toString());
			  logger.debug("selectedLineGroupIndex..."+selectedLineGroupIndex);
			  logger.debug("lineGroupDetails updated in the sesssion"+lineGroupDetails);
//			  return returnValue;
		  }
		  else if (flagEdit != null && flagEdit.equalsIgnoreCase("true"))
		  {
			  returnValue = "EditLineGroupDetail";
			  logger.debug("Edited line group..."+getEditedLineGroup());
//			  selectedLineGroupIndex = calculateSelectedLineGroupIndex();
			  selectedLineGroupIndex = Integer.parseInt(session.get("selectedLineGroupIndex").toString());
			  logger.debug("selectedLineGroupIndex..."+selectedLineGroupIndex);
//			  return returnValue;
		  }
		  else
		  {
			  selectedLineGroupIndex = calculateSelectedLineGroupIndex();
		  }
		  // Before updating line group channels populate existing linegroup details so that it can be updated for latest changes from analogs and virtuals
		  lineGroupDetails = parseLinegroupDetailsFromExistingLinegroup(getLstLineGroups().get(selectedLineGroupIndex));
		  // look for mixed channels or local channels
		  boolean isLocal = updateLineGroupChannel(getLstLineGroups().get(selectedLineGroupIndex));
		  getLstLineGroups().get(selectedLineGroupIndex).setLocal(isLocal);
		  if (!isLocal)
		  {
			  // 24-Mar-2021 - set linegroup user information to be displayed to the user
//			  addActionError("Current version does not support Line Group components across chassis.Select components from a single chassis.");
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().put(M9kConstants.KEY_MULTIPLE_CHASSIS, M9kConstants.MSG_MULTIPLE_CHASSIS);
//			  returnValue = ERROR;
//			  return returnValue;
		  }
		  else
		  {
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MULTIPLE_CHASSIS);
		  }
		  // 22-Mar-2021 - Warn the user if there are mixed voltage channels - Line-line and line-neutral channels are mixed
		  logger.debug("Is mixed voltage channels "+getLstLineGroups().get(selectedLineGroupIndex).isMixedVoltageChannels());
		  if (getLstLineGroups().get(selectedLineGroupIndex).isMixedVoltageChannels())
		  {
			  // 24-Mar-2021 - set linegroup user information to be displayed to the user
//			  addActionMessage("Line Group has both line-line and line-neutral voltage channels. You will not be able to calculate any measurements on this.");
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().put(M9kConstants.KEY_MIXED_VOLTAGE_CHANNELS, M9kConstants.MSG_MIXED_VOLTAGE_CHANNELS);				  
		  }
		  else
		  {
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MIXED_VOLTAGE_CHANNELS);
		  }
		  
		  // 06-Apr-2021 - Linegroup has more than one channel with same phase
		  if (getLstLineGroups().get(selectedLineGroupIndex).isInvalidCombination())
		  {
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().put(M9kConstants.KEY_INVALID_CHANNEL_COMBINATION, M9kConstants.MSG_INVALID_CHANNEL_COMBINATION);				  
		  }
		  else
		  {
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().remove(M9kConstants.KEY_INVALID_CHANNEL_COMBINATION);
		  }
		  logger.debug("lstLineGroups after entering managelinegroups..."+lstLineGroups);
		  logger.debug("Entered showLinegroupDetails..."+getLineGroupDetails());
		  logger.debug("selectedLineGroup..."+getSelectedLineGroup());

		  String type = getLineGroupType(getLstLineGroups().get(selectedLineGroupIndex));
		  getLstLineGroups().get(selectedLineGroupIndex).setType(type);
		// 06-Apr-2021 - Linegroup has one channel missing and it'll be automatically calculated
		  if (getLstLineGroups().get(selectedLineGroupIndex).isMissingAVoltageChannel())
		  {
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().put(M9kConstants.KEY_MISSING_ONE_VOLTAGE_CHANNEL, M9kConstants.MSG_MISSING_ONE_VOLTAGE_CHANNEL);				  
		  }
		  else
		  {
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MISSING_ONE_VOLTAGE_CHANNEL);
		  }
		  if (getLstLineGroups().get(selectedLineGroupIndex).isMissingACurrentChannel())
		  {
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().put(M9kConstants.KEY_MISSING_ONE_CURRENT_CHANNEL, M9kConstants.MSG_MISSING_ONE_CURRENT_CHANNEL);				  
		  }
		  else
		  {
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MISSING_ONE_CURRENT_CHANNEL);
		  }
		  if (getLstLineGroups().get(selectedLineGroupIndex).isMissingTwoCurrentChannels())
		  {
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().put(M9kConstants.KEY_MISSING_TWO_CURRENT_CHANNELS, M9kConstants.MSG_MISSING_TWO_CURRENT_CHANNELS);				  
		  }
		  else
		  {
			  getLstLineGroups().get(selectedLineGroupIndex).getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MISSING_TWO_CURRENT_CHANNELS);
		  }

//		  if (session.get("previousSelectedLineGroupIndex") != null)
//		  {
//			  previousSelectedLineGroupIndex = Integer.parseInt(session.get("previousSelectedLineGroupIndex").toString());
//			  logger.debug("previousSelectedLineGroupIndex from session..."+previousSelectedLineGroupIndex);
//			  logger.debug("Edited LG name after change of selection..."+getEditSelectedLineGroup());
//		  }
//		  if(getLineGroupDetails() != null && !getLineGroupDetails().isEmpty() && flagEdit != null && flagEdit.equalsIgnoreCase("true"))
//		  {
//			  logger.debug("previousSelectedLineGroupIndex before update..."+previousSelectedLineGroupIndex);
//			  LineGroupsAlgorithm lineGroupsAlgorithm = getLstLineGroups().get(previousSelectedLineGroupIndex);
//			  logger.debug("Edited LG name after change of selection..."+getEditSelectedLineGroup());
//			  lineGroupsAlgorithm.setName(getEditSelectedLineGroup());
//			  updateLineGroupChannel(lineGroupsAlgorithm);
//		  }
//		  selectedLineGroupIndex = calculateSelectedLineGroupIndex();
//		  previousSelectedLineGroupIndex = selectedLineGroupIndex;
//		  session.put("previousSelectedLineGroupIndex", previousSelectedLineGroupIndex);
		  logger.debug("Entered ManageLineGroupsAction"+getSelectedLineGroup());
		  logger.debug("lineGroupDetails..."+getLineGroupDetails());
		  logger.debug("availableChannels..."+getAvailableChannels());
		  logger.debug("lstLine groups..."+getLstLineGroups());
		  logger.debug("selectedLineGroupIndex..."+selectedLineGroupIndex);
		  currentLineGroupIndex = selectedLineGroupIndex;
		  session.put("currentLineGroupIndex", currentLineGroupIndex);
//		  session.put("selectedLineGroupIndex", selectedLineGroupIndex);
//		  session.put("selectedLineGroup", getSelectedLineGroup());
		  logger.debug("Returning value..."+returnValue+" with currentLineGroupIndex "+currentLineGroupIndex);
		  return returnValue;

	  }

	  /**
	   * Everytime before displaying linegroups to update the analog channels and virtual channels part of the line group, we need the existing names 
	   * @param lineGroupsAlgorithm
	   * @return
	   */
	  private List<String> parseLinegroupDetailsFromExistingLinegroup(LineGroupsAlgorithm lineGroupsAlgorithm) {
		  List<String> lstExistingAnalogDisplayChannels = new ArrayList<String>(lineGroupsAlgorithm.getInputChannels().size());
		  AnalogChannelDTO analogChannelDTO;
		  for (Iterator<AnalogChannelDTO> iterator = lineGroupsAlgorithm.getInputChannels().iterator(); iterator.hasNext();) {
			  analogChannelDTO =  iterator.next();
			  logger.debug("Analog to be added "+analogChannelDTO.getDisplayChannel());
			  lstExistingAnalogDisplayChannels.add(analogChannelDTO.getDisplayChannel());
		}
		return lstExistingAnalogDisplayChannels;
	}

	public String showDecisionLogic() throws Exception
	  {
		  String returnValue = SUCCESS;
//		  lstAllTriggerChannels = (List<TriggerChannelDTO>) session.get("lstAllTriggerChannels");
		  logger.debug("Entered Show Decion Logic "+getSelectedLineGroup());
		  selectedLineGroupIndex = calculateSelectedLineGroupIndex();
		  logger.debug("Decision logic "+lstLineGroups.get(selectedLineGroupIndex).getDecisionLogic());
		  logger.debug("selectedLineGroupIndex..."+selectedLineGroupIndex);
//		  logger.debug("lstAllTriggerChannels..."+lstAllTriggerChannels);
		  currentLineGroupIndex = selectedLineGroupIndex;
		  logger.debug("Returning value..."+returnValue+ " with currentLineGroupIndex "+currentLineGroupIndex);
		  session.put("currentLineGroupIndex", currentLineGroupIndex);
		  return returnValue;

	  }

	  public String editLineGroupDetails() throws Exception
	  {
		  actionType = "EDIT";
		  String returnValue = "EditLineGroup";
		  logger.debug("lstLineGroups after entering managelinegroups..."+lstLineGroups);
		  session.put("flagEdit", "true");
//		  selectedLineGroupIndex = Integer.parseInt(session.get("selectedLineGroupIndex").toString());
//		  backUpLineGroups();
		  selectedLineGroupIndex = getIndexOfSelectedLineGroup(getSelectedLineGroup());
		  session.put("selectedLineGroupIndex", selectedLineGroupIndex );
		  editedLineGroup = getLstLineGroups().get(selectedLineGroupIndex);
		  logger.debug("@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());
		  logger.debug("selectedLineGroup..."+getSelectedLineGroup());
		  logger.debug("After session...@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());

		  logger.debug("Entered showLinegroupDetails..."+getLineGroupDetails());
//		  previousSelectedLineGroupIndex = selectedLineGroupIndex;
//		  session.put("previousSelectedLineGroupIndex", previousSelectedLineGroupIndex);
		  logger.debug("Entered ManageLineGroupsAction"+getSelectedLineGroup());
		  logger.debug("lineGroupDetails..."+getLineGroupDetails());
		  logger.debug("availableChannels..."+getAvailableChannels());
		  logger.debug("lstLine groups..."+getLstLineGroups());
		  lstAvailableAnalogChannels = getLstAvailableAnalogChannels();
		  logger.debug("selectedLineGroupIndex..."+selectedLineGroupIndex);
//		  session.put("selectedLineGroupIndex", selectedLineGroupIndex);
		  session.put("lstAvailableAnalogChannels", lstAvailableAnalogChannels);
		  session.put("editedLineGroup", editedLineGroup);
		  logger.debug("Returning value..."+returnValue+" with edited line Group"+editedLineGroup );
		  logger.info("User "+getUserDto().getUserName()+" is editing the line group "+editedLineGroup.getDisplayName());
		  return returnValue;

	  }

	  private int getIndexOfSelectedLineGroup(String selectedLineGroupName) {
		  int selectedIndex = 0;
		  for (Iterator<LineGroupsAlgorithm> iterator = getLstLineGroups().iterator(); iterator.hasNext();) {
			  LineGroupsAlgorithm lineGroupsAlgorithm = iterator.next();
			  if (lineGroupsAlgorithm.getName().equalsIgnoreCase(selectedLineGroupName))
			  {
				  break;
			  }
			  selectedIndex++;
		}
		  return selectedIndex;
	}

	public String deleteLineGroup() throws Exception
	  {
		  String returnValue = SUCCESS;
		  logger.debug("lstLineGroups after entering managelinegroups..."+lstLineGroups);
		  selectedLineGroupIndex = calculateSelectedLineGroupIndex();
//		  selectedLineGroupIndex = Integer.parseInt(session.get("selectedLineGroupIndex").toString());
		  GlobalLineGroups globalLineGroups = currentSubstation.getGlobalLineGroups();
		  LineGroupsAlgorithm lineGroupToDelete;
		  // START: 16-Nov-2020 - BUG - Delete did not work when there was just one line group - Fixed 
//		  if (lstLineGroups.size() > 1)
		  if (lstLineGroups.size() > 0)
		  // END: 16-Nov-2020
		  {
//			  deleteLocalLineGroupFromXml(lstLineGroups.get(selectedLineGroupIndex));
				lineGroupToDelete = lstLineGroups.get(selectedLineGroupIndex); 
				logger.debug("LG_BUG:line group to be deleted "+lineGroupToDelete+" lineGroup.getChassis() "+lineGroupToDelete.getChassis()+" mapXMLDfrs "+mapXMLDfrs);
				// START: 11-Dec-2019 - Delete global line group fix
				if (lineGroupToDelete.getChassis() == null)
				{
					if (globalLineGroups != null)
					  {
						  deleteGlobalLineGroupFromXml(currentSubstation.getGlobalLineGroups(), lineGroupToDelete);
					  }
				}
				else
				{
				// END: 11-Dec-2019
					DFR xmlDfr = mapXMLDfrs.get(lineGroupToDelete.getChassis());
					logger.debug("LG_BUG:DFR Id "+xmlDfr.getId());
					LineGroups xmlLineGroups = xmlDfr.getDataPool().getLineGroups();
					logger.debug("LG_BUG:Xml Line groups "+xmlLineGroups);
					if (xmlLineGroups != null)
					{
						LineGroup[] arrLineGroup = xmlLineGroups.getLineGroupArray();
						logger.debug("LG_BUG: Line Group array "+arrLineGroup);
						if (arrLineGroup != null)
						{
							for (int i = 0; i < arrLineGroup.length; i++) {
								logger.debug("To compare lineGroup.getName() "+lineGroupToDelete.getName()+" lineGroup.getId() "+lineGroupToDelete.getId());
								logger.debug("\t\tComparing with "+arrLineGroup[i].getId()+" - "+arrLineGroup[i].getName());
								if (arrLineGroup[i].getName().equalsIgnoreCase(lineGroupToDelete.getName()) || arrLineGroup[i].getId() == lineGroupToDelete.getId())
								{
									logger.debug("LG_BUG:ABout to delete linegroup xmlLineGroups["+i+"]--> "+xmlLineGroups.getLineGroupArray(i));
									xmlLineGroups.removeLineGroup(i);
									break;
								}
							}
						}
						logger.debug("LG_BUG: about to remove Associated MEasurements...");
						removeAssociatedMeasurements(xmlDfr, lineGroupToDelete);
						logger.debug("LG_BUG: After removing Associated MEasurements...");
					}
				}
			  lstLineGroups.remove(selectedLineGroupIndex);
			  if (selectedLineGroupIndex > 0)
			  {
				  selectedLineGroupIndex--;
			  }
			  // Check for associated measurements for the deleted linegroup 
			  checkForAssociatedMeasurements(lineGroupToDelete);
		  }
//		  else
//		  {
//			  if (globalLineGroups != null)
//			  {
//				  deleteGlobalLineGroupFromXml(currentSubstation.getGlobalLineGroups(), lstLineGroups.get(selectedLineGroupIndex));
//			  }
//			  lstLineGroups.clear();
//		  }
		  logger.debug("selectedLineGroup..."+getSelectedLineGroup());
		  logger.debug("After session...@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());

		  logger.debug("Entered showLinegroupDetails..."+getLineGroupDetails());
//		  previousSelectedLineGroupIndex = selectedLineGroupIndex;
//		  session.put("previousSelectedLineGroupIndex", previousSelectedLineGroupIndex);
		  logger.debug("lstLine groups..."+getLstLineGroups());
		  logger.debug("selectedLineGroupIndex..."+selectedLineGroupIndex);
//		  session.put("selectedLineGroupIndex", selectedLineGroupIndex);
		  session.put("lstLineGroups",lstLineGroups);
		// START: 16-Nov-2020 - BUG - Delete did not work when there was just one line group - Fixed
		  if (lstLineGroups.size() == 0)
		  {
			  setLineGroupExists(false);
			  session.put("lineGroupExists",false);
		  }
		// END: 16-Nov-2020
		  
		  logger.debug("Returning value..."+returnValue+" lstAssociatedMeasurements "+lstAssociatedMeasurements);
		  logger.info("User "+getUserDto().getUserName()+" deleted the line group "+(getSelectedLineGroup()!=null?getSelectedLineGroup():"")+" and the associated measurements");
		  return returnValue;

	  }
	  
	  /**
	 * @param xmlDfr 
	 * @param lineGroup
	 */
	private void removeAssociatedMeasurements(DFR xmlDfr, LineGroupsAlgorithm lineGroup) {
		Exports exports = xmlDfr.getDataPool().getExports();
		Export[] arrExport = exports.getExportArray();
		
		for (int i = 0; i < arrExport.length; i++) {
//			logger.debug("arrExport[i].getName() "+arrExport[i].getName());
			if (arrExport[i].getName().endsWith(lineGroup.getName()))
			{
				deleteMeasurementUsingExport(xmlDfr,arrExport[i]);
			}
		}
		
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
//					logger.debug("To be compared to "+arrTrigger[i].getChanId()+" name "+arrTrigger[i].getName());
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
								if (xmlAlgoType.startsWith(M9kConstants.ZERO_SEQUENCE))
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


	private void deleteZeroSequenceFromXml(TriggerChannelDTO triggerChannelDTO) {
		DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
		ZeroSequence[] arrZeroSequence = xmlDfr.getDataPool().getAlgorithms().getZeroSequenceArray();
		for (int i = 0; i <arrZeroSequence.length; i++) {
			if (triggerChannelDTO.getMeasurementName().equals(arrZeroSequence[i].getName()))
			{
				 logger.info("User "+getUserDto().getUserName()+" deleted the zero Sequence algorithm  "+arrZeroSequence[i].getName() +" associated with the line group "+(getSelectedLineGroup()!=null?getSelectedLineGroup():""));
				xmlDfr.getDataPool().getAlgorithms().removeZeroSequence(i);
				break;
			}
		}
	}
	
	private void deletePositiveSequenceFromXml(TriggerChannelDTO triggerChannelDTO) {
		DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
		PositiveSequence[] arrPositiveSequence = xmlDfr.getDataPool().getAlgorithms().getPositiveSequenceArray();
		for (int i = 0; i <arrPositiveSequence.length; i++) {
			if (triggerChannelDTO.getMeasurementName().equals(arrPositiveSequence[i].getName()))
			{
				logger.info("User "+getUserDto().getUserName()+" deleted the positive Sequence algorithm  "+arrPositiveSequence[i].getName() +" associated with the line group "+(getSelectedLineGroup()!=null?getSelectedLineGroup():""));
				xmlDfr.getDataPool().getAlgorithms().removePositiveSequence(i);
				break;
			}
		}
	}
	
	private void deleteNegativeSequenceFromXml(TriggerChannelDTO triggerChannelDTO) {
		DFR xmlDfr = mapXMLDfrs.get(triggerChannelDTO.getChassis());
		NegativeSequence[] arrNegativeSequence = xmlDfr.getDataPool().getAlgorithms().getNegativeSequenceArray();
		for (int i = 0; i <arrNegativeSequence.length; i++) {
			if (triggerChannelDTO.getMeasurementName().equals(arrNegativeSequence[i].getName()))
			{
				logger.info("User "+getUserDto().getUserName()+" deleted the negative Sequence algorithm  "+arrNegativeSequence[i].getName() +" associated with the line group "+(getSelectedLineGroup()!=null?getSelectedLineGroup():""));
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
				logger.info("User "+getUserDto().getUserName()+" deleted the power algorithm  "+arrPower[i].getName() +" associated with the line group "+(getSelectedLineGroup()!=null?getSelectedLineGroup():""));
				xmlDfr.getDataPool().getAlgorithms().removePower(i);
				break;
			}
		}
	}

	public String newLineGroup() throws Exception
	  {
		  String returnValue = "NewLineGroup";
		  actionType = "NEW";
		  AnalogChannelDTO analogChannelDTO;
		  if (getLstLineGroups() == null)
		  {
			  lstLineGroups = new ArrayList<LineGroupsAlgorithm>();
			  selectedLineGroupIndex = 0;
		  }
		  // Get all available data
		  lstAvailableAnalogChannels = new ArrayList<AnalogChannelDTO>(getLstAnalogChannels().size());
		  for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels().iterator(); iterator.hasNext();) {
				analogChannelDTO = iterator.next();
//				logger.debug("Analog Channel display Name: "+analogChannelDTO.getDisplayName());
				if (!analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE))
				{
					lstAvailableAnalogChannels.add(analogChannelDTO);
				}
			}
		  if (getLstVirtualChannels() != null && !getLstVirtualChannels().isEmpty())
			{
			  // Add Virtual channels too the available list
			  for (Iterator<AnalogChannelDTO> iterator = getLstVirtualChannels().iterator(); iterator.hasNext();) {
					analogChannelDTO = iterator.next();
					logger.debug("Virtual Analog Channel display Name: "+analogChannelDTO.getDisplayName());
					if (!analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE))
					{
						lstAvailableAnalogChannels.add(analogChannelDTO);
					}
				}
			}
		  if (lstAvailableAnalogChannels != null && lstAvailableAnalogChannels.isEmpty())
		  {
			  returnValue = ERROR;
			  addActionError("There is no valid Analog channels to create line group.");
		  }
		  else
		  {
//			  logger.debug("lstLineGroups after entering newLineGroup..."+lstLineGroups);
			  session.put("NewLineGroup", "true");
			  
//			  logger.debug("@#$@#$@#$ lstAnalogChannels in newLineGroup.."+getLstAnalogChannels());
//			  logger.debug("selectedLineGroup in newLineGroup..."+getSelectedLineGroup());
//			  logger.debug("After session...@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());

			  LineGroupsAlgorithm lineGroupsAlgorithm = new LineGroupsAlgorithm();
			  lineGroupsAlgorithm.setInputChannels(new ArrayList<AnalogChannelDTO>());
			  // 09-Apr-2021 - Line group id generation moved to stationDTO
//			  lineGroupsAlgorithm.setId(LineGroupsAlgorithm.getNextAvailableId());
			  logger.debug("total line groups configured "+getStationDetails().getTotalLineGroupsConfigured());
			  logger.debug("Current linegroup id "+getStationDetails().getCurrentLineGroupId());
			  lineGroupsAlgorithm.setId(getStationDetails().getNextAvailableLineGroupId());
			  setSelectedLineGroup("NewLineGroup"+lineGroupsAlgorithm.getId());
			  lstLineGroups.add(lineGroupsAlgorithm);
			  lineGroupsAlgorithm.setName("LG_"+lineGroupsAlgorithm.getId());
//			  setSelectedLineGroup(getSelectedLineGroup());
//			  previousSelectedLineGroupIndex = selectedLineGroupIndex;
			  selectedLineGroupIndex = lstLineGroups.size()-1;
//			  logger.debug("selectedLineGroupIndex..."+selectedLineGroupIndex);
			  session.put("lstAvailableAnalogChannels", lstAvailableAnalogChannels);
			  lineGroupDetails = new ArrayList<String>();
			  session.put("lstLineGroups",lstLineGroups);
//			  logger.debug("lineGroupDetails updated in the sesssion"+lstLineGroups);
//			  logger.debug("lstAvailableAnalogChannels about to be updated in the sesssion" + lstAvailableAnalogChannels);
//			  
//			  logger.debug("lstAvailableAnalogChannels from session..."+session.get("lstAvailableAnalogChannels"));
		  }
		  logger.debug("Returning value from newLineGroup..."+returnValue);
		  logger.info("User "+getUserDto().getUserName()+" is creating a new line group");
		  return returnValue;

	  }

	  public String saveLineGroupDetails() 
	  {
		  logger.debug("Entered saveLineGroupDetails...."+getSelectedLineGroup());
		  String returnValue = "LineGroupDetail";
		  String newLineGroup = (String) session.get("NewLineGroup");

//		  selectedLineGroup = (String) session.get("selectedLineGroup");
		  logger.debug("Edited line group "+getEditedLineGroup());
		  if (getEditedLineGroup() == null)
		  {
			  editedLineGroup = (LineGroupsAlgorithm) session.get("editedLineGroup");
		  }
		  logger.debug("In saveLineGroupDetails line group name: "+getSelectedLineGroup());
		  logger.debug("@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());
		  logger.debug("selectedLineGroup..."+getSelectedLineGroup());
		  logger.debug("saveLineGroupDetails getLineGroupDetails() "+getLineGroupDetails());
		  String type;

		  try
		  {
		  if (newLineGroup == null || !newLineGroup.equals("true"))
		  {
			  selectedLineGroupIndex = Integer.parseInt(session.get("selectedLineGroupIndex").toString());
			  LineGroupsAlgorithm linegrouptoSave = null;
			  for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator.hasNext();) {
				  linegrouptoSave = iterator.next();
				  if (linegrouptoSave.getId() == getEditedLineGroup().getId())
				  {
					  logger.debug("matched with linegroup "+linegrouptoSave);
					  break;
				  }
				  else
				  {
					  linegrouptoSave=null;
				  }
			  }
				logger.debug("In Edit Validation "+getEditedLineGroup());
//				if (getEditedLineGroup().getName() == null || getEditedLineGroup().getName().isEmpty())
				if (getEditedLineGroup().getLineGroupName() == null || getEditedLineGroup().getLineGroupName().isEmpty())
				{
//					addFieldError("lineGroupName","Name cannot be blank");
					addActionError("Line Group Name cannot be blank");
					returnValue= ERROR;
				}
				// START: Saravanan 12-Sep-2023 - Allow all special characters except ,
//				else if(getEditedLineGroup().getName().indexOf("/") != -1 || getEditedLineGroup().getName().indexOf(",") != -1 ||  getEditedLineGroup().getName().indexOf("(") != -1
//						|| getEditedLineGroup().getName().indexOf(")") != -1 || getEditedLineGroup().getName().indexOf("[") != -1 || getEditedLineGroup().getName().indexOf("]") != -1)
				else if (getEditedLineGroup().getName().indexOf(",") != -1 )
				{
//					addActionError("Special characters not allowed in Line Groups name: / ( ) [ ] ,");
					addActionError("Contains invalid character ,");
					returnValue= ERROR;	
				}
				// END: Saravanan 12-Sep-2023 - Allow all special characters except ,
				else
				{
//					int index = 0;
					for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator.hasNext();) {
						LineGroupsAlgorithm lineGroupsAlgorithm =  iterator.next();
//						if (getEditedLineGroup().getName().equalsIgnoreCase(lineGroupsAlgorithm.getName()) && selectedLineGroupIndex != index)
//						if (getEditedLineGroup().getLineGroupName().equalsIgnoreCase(lineGroupsAlgorithm.getLineGroupName()) && selectedLineGroupIndex != index)
						if (getEditedLineGroup().getLineGroupName().equalsIgnoreCase(lineGroupsAlgorithm.getLineGroupName()) && getEditedLineGroup().getId() != lineGroupsAlgorithm.getId())
						{
//							addFieldError("lineGroupName","Name already exists");
							addActionError("Line Group Name already exists");
							returnValue =  ERROR;
							break;
						}
//						index++;
					}
				}
				
				if (getLineGroupDetails() == null || getLineGroupDetails().isEmpty())
				{
					getEditedLineGroup().setInputChannels(new ArrayList<AnalogChannelDTO>());
//							addFieldError("lineGroupDetails","No channels selected");
					addActionError("Selected channels List cannot be empty");
					returnValue = ERROR;
				}
				if (returnValue == ERROR)
				{
					logger.debug("Returning with Error");
					return returnValue;
				}
//				// Saravanan 07-Mar-2013 : FIX to avoid -1 in name
//				if(getEditedLineGroup().getName().indexOf("-1") != -1)
//				{
////					getEditedLineGroup().setName(getEditedLineGroup().getName().replaceAll("-1", "- 1"));
//					if (getEditedLineGroup().getLineGroupName() != null)
//					{
//						getEditedLineGroup().setName("LG_"+getEditedLineGroup().getId());
//						getEditedLineGroup().setLineGroupName(getEditedLineGroup().getLineGroupName().replaceAll("-1", "- 1"));
//					}
//					else
//					{
//						getEditedLineGroup().setLineGroupName(getEditedLineGroup().getName().replaceAll("-1", "- 1"));
//					}
//				}
			  logger.debug("After session...@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());
			  logger.debug("lstLineGroups after entering managelinegroups..."+lstLineGroups);
	
			  logger.debug("Entered showLinegroupDetails..."+getLineGroupDetails());
//			  selectedLineGroupIndex = calculateSelectedLineGroupIndex();
				logger.debug("In validation linegroup details "+lineGroupDetails+" selectedLineGroupIndex "+selectedLineGroupIndex);
				logger.debug("The list of selected channels "+lstLineGroups.get(selectedLineGroupIndex).getInputChannels().size());
//			  previousSelectedLineGroupIndex = selectedLineGroupIndex;
	//		  updateLineGroupChannel();
			  logger.debug("Selected line group name: "+lstLineGroups.get(selectedLineGroupIndex));
//			  LineGroupsAlgorithm lineGroupEdited = lstLineGroups.get(selectedLineGroupIndex);
//			  getEditedLineGroup().setName(getSelectedLineGroup());
			  
			  getEditedLineGroup().getMapLinegroupWarningMessages().clear();// Clear already stored messages
			  
			selectedLineGroup = getEditedLineGroup().getName();
			  if(getLineGroupDetails() != null && !getLineGroupDetails().isEmpty())
			  {
				  logger.debug("lineGroupsAlgorithm to be updated name..."+getEditedLineGroup().getName());
				  logger.debug("lineGroupsAlgorithm to be updated circuitNames..."+getEditedLineGroup().getInputChannels());
				  boolean isLocal = updateLineGroupChannel(getEditedLineGroup());
				  getEditedLineGroup().setLocal(isLocal);
				  if (!isLocal)
				  {
					  // 24-Mar-2021 - set linegroup user information to be displayed to the user
//					  addActionError("Current version does not support Line Group components across chassis.Select components from a single chassis.");
					  getEditedLineGroup().getMapLinegroupWarningMessages().put(M9kConstants.KEY_MULTIPLE_CHASSIS, M9kConstants.MSG_MULTIPLE_CHASSIS);
//					  returnValue = ERROR;
//					  return returnValue;
				  }
				  else
				  {
					  getEditedLineGroup().getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MULTIPLE_CHASSIS);
				  }
				  // 22-Mar-2021 - Warn the user if there are mixed voltage channels - Line-line and line-neutral channels are mixed
				  logger.debug("Is mixed voltage channels "+getEditedLineGroup().isMixedVoltageChannels());
				  if (getEditedLineGroup().isMixedVoltageChannels())
				  {
					  // 24-Mar-2021 - set linegroup user information to be displayed to the user
//					  addActionMessage("Line Group has both line-line and line-neutral voltage channels. You will not be able to calculate any measurements on this.");
					  getEditedLineGroup().getMapLinegroupWarningMessages().put(M9kConstants.KEY_MIXED_VOLTAGE_CHANNELS, M9kConstants.MSG_MIXED_VOLTAGE_CHANNELS);				  
				  }
				  else
				  {
					  getEditedLineGroup().getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MIXED_VOLTAGE_CHANNELS);
				  }
				  
				  // 06-Apr-2021 - Linegroup has more than one channel with same phase
				  if (getEditedLineGroup().isInvalidCombination())
				  {
					  getEditedLineGroup().getMapLinegroupWarningMessages().put(M9kConstants.KEY_INVALID_CHANNEL_COMBINATION, M9kConstants.MSG_INVALID_CHANNEL_COMBINATION);				  
				  }
				  else
				  {
					  getEditedLineGroup().getMapLinegroupWarningMessages().remove(M9kConstants.KEY_INVALID_CHANNEL_COMBINATION);
				  }

			  }
			  // Changed the signature
//			  type = getLineGroupType(getEditedLineGroup().getInputChannels());
			  type = getLineGroupType(getEditedLineGroup());
			  getEditedLineGroup().setType(type);
			// 06-Apr-2021 - Linegroup has one channel missing and it'll be automatically calculated
			  if (getEditedLineGroup().isMissingAVoltageChannel())
			  {
				  getEditedLineGroup().getMapLinegroupWarningMessages().put(M9kConstants.KEY_MISSING_ONE_VOLTAGE_CHANNEL, M9kConstants.MSG_MISSING_ONE_VOLTAGE_CHANNEL);				  
			  }
			  else
			  {
				  getEditedLineGroup().getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MISSING_ONE_VOLTAGE_CHANNEL);
			  }
			  if (getEditedLineGroup().isMissingACurrentChannel())
			  {
				  getEditedLineGroup().getMapLinegroupWarningMessages().put(M9kConstants.KEY_MISSING_ONE_CURRENT_CHANNEL, M9kConstants.MSG_MISSING_ONE_CURRENT_CHANNEL);				  
			  }
			  else
			  {
				  getEditedLineGroup().getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MISSING_ONE_CURRENT_CHANNEL);
			  }
			  if (getEditedLineGroup().isMissingTwoCurrentChannels())
			  {
				  getEditedLineGroup().getMapLinegroupWarningMessages().put(M9kConstants.KEY_MISSING_TWO_CURRENT_CHANNELS, M9kConstants.MSG_MISSING_TWO_CURRENT_CHANNELS);				  
			  }
			  else
			  {
				  getEditedLineGroup().getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MISSING_TWO_CURRENT_CHANNELS);
			  }
//			  lstLineGroups.remove(selectedLineGroupIndex);
			  lstLineGroups.set(selectedLineGroupIndex, getEditedLineGroup());
			  Collections.sort(lstLineGroups, new Comparator<LineGroupsAlgorithm>() {

					@Override
					public int compare(LineGroupsAlgorithm o1, LineGroupsAlgorithm o2) {
//						return ((o1.getId() < o2.getId())?0:1);
						return ((o1.getId() < o2.getId())? -1 : (o1.getId() == o2.getId())?0:1);
					}
				});
//			  selectedLineGroup = lineGroupName;
			  logger.debug("\t\t\t\tEdited line group after setting local and type : "+getEditedLineGroup());
			  logger.debug("Selected line group name After Edit: "+lstLineGroups.get(selectedLineGroupIndex));
//			  session.put("previousSelectedLineGroupIndex", previousSelectedLineGroupIndex);
			  logger.debug("Entered ManageLineGroupsAction"+getSelectedLineGroup());
			  logger.debug("lineGroupDetails..."+getLineGroupDetails());
			  logger.debug("availableChannels..."+getAvailableChannels());
			  lstAvailableAnalogChannels = getLstAvailableAnalogChannels();
			  logger.debug("selectedLineGroupIndex..."+selectedLineGroupIndex);
//			  session.put("selectedLineGroupIndex", selectedLineGroupIndex);
//			  session.put("selectedLineGroup", selectedLineGroup);
			  session.put("lstAvailableAnalogChannels", lstAvailableAnalogChannels);
			  session.put("lstLineGroups",getLstLineGroups());
			  logger.debug("Back to Session lstLine groups..."+getLstLineGroups());
			  logger.debug("Returning value..."+returnValue);
			  session.remove("flagEdit");
			  session.remove("editedLineGroup");
			  session.remove("selectedLineGroupIndex");
			  logger.info("User "+getUserDto().getUserName()+" edited and saved the linegroup "+getEditedLineGroup().getDisplayName());
			// Check for associated measurements for the modified and saved linegroup
			  checkForAssociatedMeasurements(getEditedLineGroup());
		  }
		  else if (newLineGroup != null && newLineGroup.equals("true"))
		  {
				logger.debug("In Validation "+getSelectedLineGroup());
				if (getSelectedLineGroup() == null || getSelectedLineGroup().isEmpty())
				{
					logger.debug("Line Group Name cannot be blank");
//					addFieldError("lineGroupName","Name cannot be blank");
					addActionError("Line Group Name cannot be blank");
					returnValue= ERROR;
				}
				// START: Saravanan 12-Sep-2023 - Allow all special characters except ,
//				else if(getSelectedLineGroup().indexOf("/") != -1 || getSelectedLineGroup().indexOf(",") != -1 ||  getSelectedLineGroup().indexOf("(") != -1
//						|| getSelectedLineGroup().indexOf(")") != -1 || getSelectedLineGroup().indexOf("[") != -1 || getSelectedLineGroup().indexOf("]") != -1)
				else if (getSelectedLineGroup().indexOf(",") != -1 )
				{
//					logger.debug("Special characters not allowed in Line Groups name: / ( ) [ ] ,");
//					addActionError("Special characters not allowed in Line Groups name: / ( ) [ ] , ");
					addActionError("Contains invalid character ,");
					returnValue= ERROR;	
				}
				// END: Saravanan 12-Sep-2023 - Allow all special characters except ,
				else
				{
					logger.debug("In else part");
					for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator.hasNext();) {
						LineGroupsAlgorithm lineGroupsAlgorithm =  iterator.next();
//						if (getSelectedLineGroup().equalsIgnoreCase(lineGroupsAlgorithm.getName()) && !getSelectedLineGroup().equalsIgnoreCase(getSelectedLineGroup()))
						if (getSelectedLineGroup().equalsIgnoreCase(lineGroupsAlgorithm.getName()))
						{
//							addFieldError("lineGroupName","Name already exists");
							logger.debug("Line Group Name already exists");
							addActionError("Line Group Name already exists");
							returnValue =  ERROR;
							break;
						}
					}
				}
				if (returnValue == ERROR)
				{
					logger.debug("About to return "+returnValue);
					logger.debug("Returning with Error");
					return returnValue;
				}
				// Saravanan 07-Mar-2013 : FIX to avoid -1 in name
//				if(getSelectedLineGroup().indexOf("-1") != -1)
//				{
//					setSelectedLineGroup(getSelectedLineGroup().replaceAll("-1", "- 1"));
//				}
			  logger.debug("lineGroupName after save..."+getSelectedLineGroup());
			  logger.debug("lstLineGroups in the session..."+lstLineGroups);
			  selectedLineGroupIndex = lstLineGroups.size()-1;
//			  selectedLineGroupIndex = Integer.parseInt(session.get("selectedLineGroupIndex").toString());
			  logger.debug("selectedLineGroupIndex in saveDetails()... "+selectedLineGroupIndex);
			  LineGroupsAlgorithm newLineGroupsAlgorithm = getLstLineGroups().get(selectedLineGroupIndex);
			  if(getLineGroupDetails() != null && !getLineGroupDetails().isEmpty())
			  {
				  logger.debug("lineGroupsAlgorithm object name..."+newLineGroupsAlgorithm.getName());
				  logger.debug("lineGroupsAlgorithm to be updated name..."+newLineGroupsAlgorithm.getLineGroupName());
				  logger.debug("lineGroupsAlgorithm Enable auto calc? "+newLineGroupsAlgorithm.getEnableAutoCalc());
				  logger.debug("lineGroupsAlgorithm Positive reactance..."+newLineGroupsAlgorithm.getPositiveReactance());
				  logger.debug("lineGroupsAlgorithm to be updated circuitNames..."+newLineGroupsAlgorithm.getInputChannels());
				  
				  boolean isLocal = updateLineGroupChannel(newLineGroupsAlgorithm);
				  if (!isLocal)
				  {
//					  addActionError("Current version does not support Line Group components across chassis.Select components from a single chassis.");
//					  addActionMessage("Line Group has components from multiple chassis. You will not be able to trigger on this.");
					  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().put(M9kConstants.KEY_MULTIPLE_CHASSIS, M9kConstants.MSG_MULTIPLE_CHASSIS);
//					  returnValue = ERROR;
//					  return returnValue;
				  }
				  else
				  {
					  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MULTIPLE_CHASSIS);
				  }
				  if (newLineGroupsAlgorithm.isMixedVoltageChannels())
				  {
					  // 24-Mar-2021 - set linegroup user information to be displayed to the user
//					  addActionMessage("Line Group has both line-line and line-neutral voltage channels. You will not be able to calculate any measurements on this.");
					  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().put(M9kConstants.KEY_MIXED_VOLTAGE_CHANNELS, M9kConstants.MSG_MIXED_VOLTAGE_CHANNELS);				  
				  }
				  else
				  {
					  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MIXED_VOLTAGE_CHANNELS);
				  }
			  	
				// 06-Apr-2021 - Linegroup has more than one channel with same phase
				  if (newLineGroupsAlgorithm.isInvalidCombination())
				  {
					  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().put(M9kConstants.KEY_INVALID_CHANNEL_COMBINATION, M9kConstants.MSG_INVALID_CHANNEL_COMBINATION);				  
				  }
				  else
				  {
					  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().remove(M9kConstants.KEY_INVALID_CHANNEL_COMBINATION);
				  }
			  }
			 
				logger.debug("In validation linegroup details "+lineGroupDetails+" selectedLineGroupIndex "+selectedLineGroupIndex);
				logger.debug("The list of selected channels "+lstLineGroups.get(selectedLineGroupIndex).getInputChannels().size());
				if (newLineGroupsAlgorithm.getInputChannels() == null || newLineGroupsAlgorithm.getInputChannels().isEmpty())
				{
//					addFieldError("lineGroupDetails","No channels selected");
					addActionError("channels List is empty for this Line Group");
					return ERROR;
				}
			  logger.debug("newLineGroupsAlgorithm from lstLineGroups..."+newLineGroupsAlgorithm);
			  logger.debug("newLineGroupsAlgorithm Enable auto calc? "+newLineGroupsAlgorithm.getEnableAutoCalc());
			  logger.debug("newLineGroupsAlgorithm Positive reactance..."+newLineGroupsAlgorithm.getPositiveReactance());
			  newLineGroupsAlgorithm.setLineGroupName(getSelectedLineGroup());
//			  newLineGroupsAlgorithm.setId(LineGroupsAlgorithm.getNextAvailableId());
			  newLineGroupsAlgorithm.setName("LG_"+newLineGroupsAlgorithm.getId());

//			  if(getLineGroupDetails() != null && !getLineGroupDetails().isEmpty())
//			  {
//				  logger.debug("lineGroupsAlgorithm to be updated name..."+newLineGroupsAlgorithm.getLineGroupName());
//				  logger.debug("lineGroupsAlgorithm to be updated circuitNames..."+newLineGroupsAlgorithm.getInputChannels());
//				  
//				  boolean isLocal = updateLineGroupChannel(newLineGroupsAlgorithm);
//				  if (!isLocal)
//				  {
////					  addActionError("Current version does not support Line Group components across chassis.Select components from a single chassis.");
////					  addActionMessage("Line Group has components from multiple chassis. You will not be able to trigger on this.");
//					  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().put(M9kConstants.KEY_MULTIPLE_CHASSIS, M9kConstants.MSG_MULTIPLE_CHASSIS);
////					  returnValue = ERROR;
////					  return returnValue;
//				  }
//				  if (newLineGroupsAlgorithm.isMixedVoltageChannels())
//				  {
//					  // 24-Mar-2021 - set linegroup user information to be displayed to the user
////					  addActionMessage("Line Group has both line-line and line-neutral voltage channels. You will not be able to calculate any measurements on this.");
//					  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().put(M9kConstants.KEY_MIXED_VOLTAGE_CHANNELS, M9kConstants.MSG_MIXED_VOLTAGE_CHANNELS);				  
//				  }
//			  }
			  // Changing the signature of the message to send entire object
//			  type = getLineGroupType(newLineGroupsAlgorithm.getInputChannels());
			  type = getLineGroupType(newLineGroupsAlgorithm);
			  logger.debug("\t\tLine Group type returned "+type);
			  newLineGroupsAlgorithm.setType(type);
			// 06-Apr-2021 - Linegroup has one channel missing and it'll be automatically calculated
			  if (newLineGroupsAlgorithm.isMissingAVoltageChannel())
			  {
				  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().put(M9kConstants.KEY_MISSING_ONE_VOLTAGE_CHANNEL, M9kConstants.MSG_MISSING_ONE_VOLTAGE_CHANNEL);				  
			  }
			  else
			  {
				  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MISSING_ONE_VOLTAGE_CHANNEL);
			  }
			  if (newLineGroupsAlgorithm.isMissingACurrentChannel())
			  {
				  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().put(M9kConstants.KEY_MISSING_ONE_CURRENT_CHANNEL, M9kConstants.MSG_MISSING_ONE_CURRENT_CHANNEL);				  
			  }
			  else
			  {
				  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MISSING_ONE_CURRENT_CHANNEL);
			  }
			  if (newLineGroupsAlgorithm.isMissingTwoCurrentChannels())
			  {
				  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().put(M9kConstants.KEY_MISSING_TWO_CURRENT_CHANNELS, M9kConstants.MSG_MISSING_TWO_CURRENT_CHANNELS);				  
			  }
			  else
			  {
				  newLineGroupsAlgorithm.getMapLinegroupWarningMessages().remove(M9kConstants.KEY_MISSING_TWO_CURRENT_CHANNELS);
			  }
			  logger.debug("Name: "+newLineGroupsAlgorithm.getName());
			  logger.debug("input Channels... "+newLineGroupsAlgorithm.getInputChannels());
			  selectedLineGroup = newLineGroupsAlgorithm.getName();
			  session.put("lstLineGroups",lstLineGroups);
//			  session.put("selectedLineGroup", selectedLineGroup);
//			  session.put("selectedLineGroupIndex", selectedLineGroupIndex);
			  session.remove("NewLineGroup");
			  logger.info("User "+getUserDto().getUserName()+" created a new linegroup "+newLineGroupsAlgorithm.getDisplayName());
		  }
		  }
		  catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception occuired while saving ",e);
			addActionError("Error occured. "+e.getMessage());
		}
		  return returnValue;

	  }
	  
	  /**
	   * 01-Apr-2021 - Checks and creates a list of associated measurements
	   * @param sourceLinegroup
	   */
	  @SuppressWarnings("unchecked")
	private void checkForAssociatedMeasurements(LineGroupsAlgorithm sourceLinegroup) {
		  lstAssociatedMeasurements = (List<TriggerChannelDTO>) session.get("LINEGROUP_AFFECTED_MEASUREMENTS");
		// 31-Mar-2021 - Track measurements associated with the deleted lingroup
			lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
			logger.debug("List of measurements from session "+lstTriggerChannels+" getLstTriggerChannels() "+getLstTriggerChannels());
			if (getLstTriggerChannels() != null && !getLstTriggerChannels().isEmpty())
			{
			  lstAssociatedMeasurements = (List<TriggerChannelDTO>) session.get("LINEGROUP_AFFECTED_MEASUREMENTS");
			  logger.debug("About to call getLstOfMeasurementsAssociatedWithThisLinegroup "+getLstTriggerChannels());
			  List<TriggerChannelDTO> lstMeasurementsWithThisLinegroup =  getLstOfMeasurementsAssociatedWithThisLinegroup(sourceLinegroup);
			  if (lstMeasurementsWithThisLinegroup != null && !lstMeasurementsWithThisLinegroup.isEmpty())
			  {
				  if (lstAssociatedMeasurements == null)
				  {
					  lstAssociatedMeasurements = new ArrayList<TriggerChannelDTO>();
				  }
				lstAssociatedMeasurements.addAll(lstMeasurementsWithThisLinegroup);
				session.put("LINEGROUP_AFFECTED_MEASUREMENTS", lstAssociatedMeasurements);
			  }
			}
			else
			{
				lstLinegroupsModified = (List<String>) session.get("MEASUREMENTS_WARNING_MESSAGE");
				if (getLstLinegroupsModified() == null)
				{
					lstLinegroupsModified = new ArrayList<String>();
				}
				lstLinegroupsModified.add(sourceLinegroup.getName());
				session.put("MEASUREMENTS_WARNING_MESSAGE", lstLinegroupsModified);
			}
		
	}

	  /**
	   * 01-Apr-2021 - Checks and creates a list of associated measurements
	   * @param sourceLinegroup
	   */
//	  @SuppressWarnings("unchecked")
//	private void checkForAssociatedMeasurements() {
//		  logger.debug("Entered checkForAssociatedMeasurements...");
//		// 31-Mar-2021 - Track measurements associated with the deleted lingroup
//			  LineGroupsAlgorithm lineGroupsAlgorithm;
//			  for (Iterator<LineGroupsAlgorithm> iterator = getLstLineGroups().iterator(); iterator.hasNext();) {
//				  lineGroupsAlgorithm = iterator.next();
//				  if (lineGroupsAlgorithm.getType().isEmpty())
//				  {
//						lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
//						if (getLstTriggerChannels() != null && !getLstTriggerChannels().isEmpty())
//						{
//						  lstAssociatedMeasurements = (List<TriggerChannelDTO>) session.get("LINEGROUP_AFFECTED_MEASUREMENTS");
//						  logger.debug("About to call getLstOfMeasurementsAssociatedWithThisLinegroup "+getLstTriggerChannels());
//						  List<TriggerChannelDTO> lstMeasurementsWithThisLinegroup =  getLstOfMeasurementsAssociatedWithThisLinegroup(lineGroupsAlgorithm);
//						  if (lstMeasurementsWithThisLinegroup != null && !lstMeasurementsWithThisLinegroup.isEmpty())
//						  {
//							  if (lstAssociatedMeasurements == null)
//							  {
//								  lstAssociatedMeasurements = new ArrayList<TriggerChannelDTO>();
//							  }
//							lstAssociatedMeasurements.addAll(lstMeasurementsWithThisLinegroup);
//							session.put("LINEGROUP_AFFECTED_MEASUREMENTS", lstAssociatedMeasurements);
//						  }
//						}
//						else
//						{
//							lstLinegroupsModified = (List<String>) session.get("MEASUREMENTS_WARNING_MESSAGE");
//							if (getLstLinegroupsModified() == null)
//							{
//								lstLinegroupsModified = new ArrayList<String>();
//							}
//							lstLinegroupsModified.add(lineGroupsAlgorithm.getName());
//							session.put("MEASUREMENTS_WARNING_MESSAGE", lstLinegroupsModified);
//						}
//				  }
//			  }
//			}

	  /**
	   * To look for affected measurements because of the change in the line group
	   * @param editedLineGroup2
	   */
	  private List<TriggerChannelDTO> getLstOfMeasurementsAssociatedWithThisLinegroup(LineGroupsAlgorithm editedLineGroup) {
		List<TriggerChannelDTO> lstAssociatedMeasurements = new ArrayList<TriggerChannelDTO>();
		TriggerChannelDTO triggerChannelDTO;
		
		logger.debug("Entered getLstOfMeasurementsAssociatedWithThisLinegroup "+editedLineGroup+" list of measurements? "+getLstTriggerChannels());
		for (Iterator<TriggerChannelDTO> iterator = getLstTriggerChannels().iterator(); iterator.hasNext();) {
			triggerChannelDTO = iterator.next();
			logger.debug("Inside FOR loop: triggerChannelDTO.getInputChannelName() "+triggerChannelDTO.getInputChannelName()+" editedLineGroup.getName() "+editedLineGroup.getName());
			if (triggerChannelDTO.getInputChannelName().equalsIgnoreCase(editedLineGroup.getName()))
			{
				logger.debug("Measurement "+triggerChannelDTO.getDisplayName()+"is associated with linegroup "+editedLineGroup);
				lstAssociatedMeasurements.add(triggerChannelDTO);
			}
		}
		return lstAssociatedMeasurements;		
	}

	public String cancelEdit()
	  {
		  String newLineGroup = (String) session.get("NewLineGroup");
		  logger.debug("Entering cancelEdit function..."+getSelectedLineGroup());
		  String returnValue = "Cancel";
//		  restoreLineGroupChannel();
		  if (newLineGroup != null && newLineGroup.equals("true"))
		  {
			  lstLineGroups.remove(lstLineGroups.size()-1);
			  // S
//			  LineGroupsAlgorithm.decrementId();
			  getStationDetails().decrementLineGroupId();
			  logger.info("User "+userDto.getUserName()+" cancelled the new Line Group create action");
		  }
		  else
		  {
			  logger.info("User "+userDto.getUserName()+" cancelled editing Line Group "+getSelectedLineGroup());
		  }
		  session.remove("flagEdit");
		  session.remove("NewLineGroup");
		  
		  return returnValue;
	  }
	  public String updateLineGroupDetails()
	  {
		  logger.debug("In updateLineGroupDetails "+getEditedLineGroup());
		  String returnValue = "EditLineGroupDetail";
		  String newLineGroup = (String) session.get("NewLineGroup");
//		  session.put("lineGroupDetails", getLineGroupDetails());
		  logger.debug("@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());
		  logger.debug("selectedLineGroup..."+getSelectedLineGroup());
		  logger.debug("lstLineGroups after entering managelinegroups..."+lstLineGroups);
		  logger.debug("Line group details after entering managelinegroups..."+getLineGroupDetails());

		  logger.debug("Entered showLinegroupDetails..."+getLineGroupDetails());
		  logger.debug("After session...@#$@#$@#$ lstAnalogChannels.."+getLstAnalogChannels());
//		  selectedLineGroupIndex = Integer.parseInt(session.get("selectedLineGroupIndex").toString());
		  if (newLineGroup == null || !newLineGroup.equals("true"))
		  {
//			  selectedLineGroupIndex = calculateSelectedLineGroupIndex();
			  selectedLineGroupIndex = Integer.parseInt(session.get("selectedLineGroupIndex").toString());
			  logger.debug("In updateLineGroupDetails lineGroupDetails..."+getLineGroupDetails());
			  if(getLineGroupDetails() != null && !getLineGroupDetails().isEmpty())
			  {
//				  logger.debug("previousSelectedLineGroupIndex before update..."+previousSelectedLineGroupIndex +" getSelectedLineGroupIndex()"+selectedLineGroupIndex);
//				  LineGroupsAlgorithm lineGroupsAlgorithm = getLstLineGroups().get(selectedLineGroupIndex);
				  
				  boolean isLocal = updateLineGroupChannel(editedLineGroup);
				  if (!isLocal)
				  {
					  addActionMessage("Line Group has components from multiple chassis. You will not be able to trigger on this.");				  }
			  }
			session.put("editedLineGroup", editedLineGroup);
		  logger.info("User "+userDto.getUserName()+" is editing the line group "+getEditedLineGroup().getDisplayName());

		  }
		  else if (newLineGroup != null && newLineGroup.equals("true"))
		  {
			  returnValue = "NewLineGroup";
			  selectedLineGroupIndex = lstLineGroups.size()-1;
			  logger.debug("selectedLineGroupIndex in updateDetails...."+selectedLineGroupIndex);
			  logger.debug("getLstLineGroups() to be updated for new line group..."+getLstLineGroups());
			  if(getLineGroupDetails() != null && !getLineGroupDetails().isEmpty())
			  {
				  LineGroupsAlgorithm lineGroupsAlgorithm = getLstLineGroups().get(selectedLineGroupIndex);
				  logger.debug("lineGroupsAlgorithm object name..."+lineGroupsAlgorithm.getName());
				  logger.debug("lineGroupsAlgorithm to be updated name..."+lineGroupsAlgorithm.getLineGroupName());
				  logger.debug("lineGroupsAlgorithm Enable auto calc? "+lineGroupsAlgorithm.getEnableAutoCalc());
				  logger.debug("lineGroupsAlgorithm Positive reactance..."+lineGroupsAlgorithm.getPositiveReactance());
				  logger.debug("lineGroupsAlgorithm to be updated circuitNames..."+lineGroupsAlgorithm.getInputChannels());
				  
				  boolean isLocal = updateLineGroupChannel(lineGroupsAlgorithm);
				  if (!isLocal)
				  {
					  addActionMessage("Line Group has components from multiple chassis. You will not be able to trigger on this.");				  
				  }
			  	
			  }
		  }
		  return returnValue;
	  }

	  public String getUpdatedLineGroupsList()
	  {
//		  selectedLineGroup = (String) session.get("selectedLineGroup");
		  logger.debug("getUpdatedLineGroupsList() selectedLineGroup..."+getSelectedLineGroup());
		  session.remove("flagEdit");
		  session.remove("NewLineGroup");
		  return "updatedLineGroupList";
	  }
	  @SuppressWarnings("unchecked")
	@SkipValidation
		public String configureTriggers()
		{
			logger.debug("Entered configureTriggers..."+getSourceTab());
			logger.debug("Soource tab from session..."+session.get("sourceTab"));
			lstAnalogChannels = (List<AnalogChannelDTO>) session.get("lstAnalogChannels");
			if (getSourceTab() != null && getSourceTab().isEmpty())
			{
				session.remove("sourceTab");
			}
			String returnValue = "Measurements";
			
//			logger.debug("Line group size "+lstLineGroups.size()+" actual line groups "+lstLineGroups);
			if (lstLineGroups != null && isDecisionLogic() != null && !isDecisionLogic())
			{
				try
				{
					updateLineGroupDetailsBackToXMLConfig();
				}
				catch (Exception e) {
					e.printStackTrace();
					logger.error("Exception while updating details back to xml config", e);
				}
			}
			lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
			lstTriggerSourceDto = (List<TriggerSourceDTO>) session.get("lstTriggerSourceDto");
//			triggerOutHold = (String) session.get("triggerOutHold");
			 lstAllTriggerTypes = (List<Object>) session.get("lstAllTriggerTypes");
			 
			if (getAccessMode() == null || !getAccessMode().equalsIgnoreCase("Edit")) {
				returnValue = "SaveLineGroups";
			}
			if (lstLineGroups != null && !lstLineGroups.isEmpty())
			{
				session.put("lineGroupExists", true);
			}
			else
			{
				session.put("lineGroupExists", false);
			}
			return returnValue;
		}

	  @SkipValidation
	  public String back()
	  {
		  logger.debug("Entered Back ");
		  String returnValue = "back";

		  session.remove("lineGroupDetails");
		  session.remove("flagEdit");

		  return returnValue;
	  }

	  @SuppressWarnings("unchecked")
		@SkipValidation
		// called from fault location
		public String backFromFaultLoc()
		  {
		  	String returnValue; 
		  	if (isBooDigital())
		  	{
//		  		lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
		  		lstEventChannels = (List<EventChannelDTO>) session.get("lstEventChannels");
		  		returnValue = "BackToEvents";
		  	}
		  	else 
			{
					returnValue="BackToTriggers";
					lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
					lstTriggerSourceDto = (List<TriggerSourceDTO>) session.get("lstTriggerSourceDto");
//					triggerOutHold = (String) session.get("triggerOutHold");
					 lstAllTriggerTypes = (List<Object>) session.get("lstAllTriggerTypes");
			}
			  
			  return returnValue;
		  }
		private int calculateSelectedLineGroupIndex()
		{
			logger.debug("Entered calculateSelectedLineGroupIndex...."+getSelectedLineGroup());
			int selectedIndex = 0;
			if (getSelectedLineGroup() != null && !getSelectedLineGroup().isEmpty() && getLstLineGroups() != null)
			{
				for (Iterator<LineGroupsAlgorithm> iterator = getLstLineGroups().iterator(); iterator.hasNext();) {
					LineGroupsAlgorithm lineGroupsAlgorithm = iterator.next();
					logger.debug("lineGroupsAlgorithm.getName()...."+lineGroupsAlgorithm.getName()+ " linegroup name "+lineGroupsAlgorithm.getLineGroupName());
					if (lineGroupsAlgorithm.getName().equals(getSelectedLineGroup()))
					{
						logger.debug("lineGroupsAlgorithm.getName()..."+lineGroupsAlgorithm.getLineGroupName()+" with index..."+selectedIndex);	
						break;
					}
					selectedIndex++;
				}
			}
			else
			{
				selectedIndex = 0;
			}
			logger.debug("returning with index.."+selectedIndex);
			return selectedIndex;
		}
		/**
		 * @return the lstAvailableAnalogChannels
		 */
		public List<AnalogChannelDTO> getLstAvailableAnalogChannels() {
			AnalogChannelDTO analogChannelDTO = null;
			lstAvailableAnalogChannels = new ArrayList<AnalogChannelDTO>();
			logger.debug("Lst line groups "+getLstLineGroups());
			logger.debug("edited line group "+getEditedLineGroup());
			if (getLstLineGroups() == null || getLstLineGroups().isEmpty())
			{
				for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels().iterator(); iterator.hasNext();) {
					analogChannelDTO = iterator.next();
//					logger.debug("Analog Channel display Name: "+analogChannelDTO.getDisplayName());
					// START: 27-Jan-2020 - Transducer implementation - Do not populate transducer channels to the line groups list
					if (!analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE) && !analogChannelDTO.isTransducer())
					// END: 27-Jan-2020
					{
						lstAvailableAnalogChannels.add(analogChannelDTO);
					}
				}
				// Add Virtual channels too the available list
				if (getLstVirtualChannels() != null && !getLstVirtualChannels().isEmpty())
				{
				  for (Iterator<AnalogChannelDTO> iterator = getLstVirtualChannels().iterator(); iterator.hasNext();) {
						analogChannelDTO = iterator.next();
						logger.debug("Virtual Analog Channel display Name: "+analogChannelDTO.getDisplayName());
						if (!analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE))
						{
							lstAvailableAnalogChannels.add(analogChannelDTO);
						}
					}
				}
			}
			else
			{
				LineGroupsAlgorithm lineGroupsAlgorithm = getEditedLineGroup();
				
				if (lineGroupsAlgorithm == null)
				{
					lineGroupsAlgorithm = getLstLineGroups().get(getSelectedLineGroupIndex());
				}
//				logger.debug("getSelectedLineGroupIndex().."+getSelectedLineGroupIndex());
//				logger.debug("getLstLineGroups()..."+getLstLineGroups());
				for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels().iterator(); iterator.hasNext();) {
					analogChannelDTO = iterator.next();
//					logger.info("Analog Channel display Name: "+analogChannelDTO.getDisplayName());
					// START: 27-Jan-2020 - Transducer implementation - Do not populate transducer channels to the line groups list
					if (!analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE) && !lineGroupsAlgorithm.getInputChannels().contains(analogChannelDTO) && !analogChannelDTO.isTransducer())
					// END: 27-Jan-2020
					{
//						logger.debug("Doesn't contains the object.."+analogChannelDTO.getDisplayName());
						lstAvailableAnalogChannels.add(analogChannelDTO);
					}
					// END: 27-Jan-2020
				}
				// Add Virtual channels too the available list
				if (getLstVirtualChannels() != null && !getLstVirtualChannels().isEmpty())
				{
				  for (Iterator<AnalogChannelDTO> iterator = getLstVirtualChannels().iterator(); iterator.hasNext();) {
						analogChannelDTO = iterator.next();
//						logger.debug("Virtual Analog Channel display Name: "+analogChannelDTO.getDisplayName());
						if (!analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE) && !lineGroupsAlgorithm.getInputChannels().contains(analogChannelDTO))
						{
							lstAvailableAnalogChannels.add(analogChannelDTO);
						}
					}
				}
			}
			logger.debug("lstAvailableAnalogChannels returned from getLstAvailableAnalogChannels()... "+lstAvailableAnalogChannels);
			return lstAvailableAnalogChannels;
		}

		public boolean updateLineGroupChannel(LineGroupsAlgorithm lineGroupsAlgorithm)
		{
//			logger.debug("getPreviouslySelectedLineGroupIndex().."+getpreviousSelectedLineGroupIndex());
			logger.debug("Entered updateLineGroupChannel "+getEditedLineGroup());
//			editedLineGroup = (LineGroupsAlgorithm) session.get("editedLineGroup");
			String chassis = null;
			boolean isLocal = true;
			int line_line_count = 0;
			int line_neutral_count = 0;
			int phaseIA_count=0;
			int phaseIB_count=0;
			int phaseIC_count=0;
			int phaseVA_count=0;
			int phaseVB_count=0;
			int phaseVC_count=0;
			int phaseAB_count=0;
			int phaseBC_count=0;
			int phaseCA_count=0;
			List<AnalogChannelDTO> lstUpdateLineGroupDetails = new ArrayList<AnalogChannelDTO>(getLineGroupDetails().size());
			for (Iterator<String> iterator = getLineGroupDetails().iterator(); iterator
			.hasNext();) {
				String displayChannel = iterator.next();
				for (Iterator<AnalogChannelDTO> iterator2 = getLstAnalogChannels().iterator(); iterator2.hasNext();) {
					AnalogChannelDTO analogChannelDTO = iterator2.next();
					// START: 27-Jan-2020 - Transducer implementation - Do not populate transducer channels to the line groups list
					if (analogChannelDTO.isTransducer())
					{
						continue;
					}
					// END: 27-Jan-2020
//					logger.debug("anlog chnl to be compared..."+analogChannelDTO.getDisplayName()+" display name..."+displayName);
					
					if(analogChannelDTO.getDisplayChannel().equals(displayChannel))
					{
						logger.debug("Channel added..."+displayChannel);
						if (chassis == null)
						{
							chassis = analogChannelDTO.getChassis();
						}
						else if (!chassis.equalsIgnoreCase(analogChannelDTO.getChassis()))
						{
							isLocal = false;
						}
						lstUpdateLineGroupDetails.add(analogChannelDTO);
						// 22-Mar-2021 - Check for any mixed voltage channels (line-line and line-neutral) 
						logger.debug("Check for input type? "+analogChannelDTO.getInputType());
						if (!analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_NEUTRAL))
						{
							if (analogChannelDTO.getInputType().indexOf(M9kConstants.VOLTAGE) != -1)
							{
								logger.debug("Phase of the channel..."+analogChannelDTO.getPhase());
								if ((analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_AB) || analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_BC) || analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_CA)))
								{
									line_line_count++;
									if (analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_AB))
									{
										phaseAB_count++;
									}
									else if (analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_BC))
									{
										phaseBC_count++;
									}
									else
									{
										phaseCA_count++;
									}
								}
								else
								{
									line_neutral_count++;
									if (analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_A))
									{
										phaseVA_count++;
									}
									else if (analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_B))
									{
										phaseVB_count++;
									}
									else
									{
										phaseVC_count++;
									}
								}
							}
							else // Count current channels
							{
								if (analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_A))
								{
									phaseIA_count++;
								}
								else if (analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_B))
								{
									phaseIB_count++;
								}
								else
								{
									phaseIC_count++;
								}
	
							}
						}
						break;
					}
				}
				if (getLstVirtualChannels() != null && !getLstVirtualChannels().isEmpty())
				{
					for (Iterator<AnalogChannelDTO> iterator2 = getLstVirtualChannels().iterator(); iterator2.hasNext();) {
						AnalogChannelDTO analogChannelDTO = iterator2.next();
	//					logger.debug("anlog chnl to be compared..."+analogChannelDTO.getDisplayName()+" display name..."+displayName);
						if(analogChannelDTO.getDisplayChannel().equals(displayChannel))
						{
							logger.debug("Channel added..."+displayChannel);
							if (chassis == null)
							{
								chassis = analogChannelDTO.getChassis();
							}
							else if (!chassis.equalsIgnoreCase(analogChannelDTO.getChassis()))
							{
								isLocal = false;
							}
							lstUpdateLineGroupDetails.add(analogChannelDTO);
							if (!analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_NEUTRAL))
							{
								if (analogChannelDTO.getInputType().indexOf(M9kConstants.VOLTAGE) != -1)
								{
									line_neutral_count++;
									if (analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_A))
									{
										phaseVA_count++;
									}
									else if (analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_B))
									{
										phaseVB_count++;
									}
									else
									{
										phaseVC_count++;
									}
								}
								else // Count current channels
								{
									if (analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_A))
									{
										phaseIA_count++;
									}
									else if (analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_B))
									{
										phaseIB_count++;
									}
									else
									{
										phaseIC_count++;
									}
		
								}
							}

							break;
						}
					}
				}
			}
			lineGroupsAlgorithm.setLocal(isLocal);
			if (isLocal)
			{
				lineGroupsAlgorithm.setChassis(chassis);
			}
			
			// 22-Mar-2021 - Set to true if line-neutral and line-line voltage channels are mixed
			if (line_line_count > 0 && line_neutral_count > 0)
			{
				logger.debug("Setting mixed to true as both line-line and line-neutral voltage channels are present");
				lineGroupsAlgorithm.setMixedVoltageChannels(true);
			}
			else
			{
				lineGroupsAlgorithm.setMixedVoltageChannels(false);
			}
			
			if (phaseIA_count > 1 || phaseIB_count > 1 || phaseIC_count > 1
					|| phaseVA_count > 1 || phaseVB_count > 1 || phaseVC_count > 1
					|| phaseAB_count > 1 || phaseBC_count > 1 || phaseCA_count > 1)
			{
				lineGroupsAlgorithm.setInvalidCombination(true);
			}
			else
			{
				lineGroupsAlgorithm.setInvalidCombination(false);
			}
				
			lineGroupsAlgorithm.setInputChannels(lstUpdateLineGroupDetails);
			
			return isLocal;
		}
		
//		public void backUpLineGroups()
//		{
//			List<LineGroupsAlgorithm> lstBackupLineGroups = new ArrayList<LineGroupsAlgorithm>(getLstLineGroups().size());
////			lstBackupLineGroups = (List) ((ArrayList)getLstLineGroups()).clone();
//			LineGroupsAlgorithm backupLineGroupsAlgorithm = null;
//			for (Iterator<LineGroupsAlgorithm> iterator = getLstLineGroups().iterator(); iterator
//					.hasNext();) {
//				LineGroupsAlgorithm lineGroupsAlgorithm = iterator
//						.next();
//				backupLineGroupsAlgorithm = new LineGroupsAlgorithm();
//				backupLineGroupsAlgorithm.setInputChannels(lineGroupsAlgorithm.getInputChannels());
//				backupLineGroupsAlgorithm.setName(lineGroupsAlgorithm.getName());
//				lstBackupLineGroups.add(backupLineGroupsAlgorithm);
//			}
//			
//			if (lstBackupLineGroups.size() > 0)
//			{
//				LineGroupsAlgorithm lineGroupsAlgorithm = lstBackupLineGroups.get(selectedLineGroupIndex);
//				for (Iterator<AnalogChannelDTO> iterator = lineGroupsAlgorithm.getInputChannels().iterator(); iterator.hasNext();) {
//					AnalogChannelDTO type = iterator.next();
//					logger.debug("Line Groups to be backed up..."+type.getDisplayName());
//				}
//				  session.put("originalLstLineGroups", lstBackupLineGroups);
//				  session.put("backupSelectedLineGroupIndex", selectedLineGroupIndex);
//				  session.put("backupSelectedLineGroup", lstBackupLineGroups.get(selectedLineGroupIndex).getName());
//			}
//
//		}

//		@SuppressWarnings("unchecked")
//		public void restoreLineGroupChannel()
//		{
//			lstLineGroups = (List<LineGroupsAlgorithm>) session.get("originalLstLineGroups");
//			selectedLineGroupIndex = Integer.parseInt(session.get("backupSelectedLineGroupIndex").toString());
//			selectedLineGroup = (String) session.get("backupSelectedLineGroup");
////			previousSelectedLineGroupIndex = selectedLineGroupIndex;
////			session.put("selectedLineGroup", selectedLineGroup);
////			session.put("selectedLineGroupIndex", selectedLineGroupIndex);
////			session.put("previousSelectedLineGroupIndex", previousSelectedLineGroupIndex);
//			logger.debug("Entered restoreLineGroupChannel...lstLineGroups: "+lstLineGroups +" selectedLIneGroupIndex: "+selectedLineGroupIndex);
//			LineGroupsAlgorithm lineGroupsAlgorithm = lstLineGroups.get(selectedLineGroupIndex);
//			
//			lineGroupDetails = new ArrayList<String>();
//			for (Iterator<AnalogChannelDTO> iterator = lineGroupsAlgorithm.getInputChannels().iterator(); iterator.hasNext();) {
//				AnalogChannelDTO type =  iterator.next();
//				logger.debug("Restore line group details..."+type.getDisplayName());
//				lineGroupDetails.add(type.getDisplayName());
//			}
//			session.put("lstLineGroups", lstLineGroups);
////			session.put("lineGroupDetails", lineGroupDetails);
//			session.remove("originalLstLineGroups");
//			session.remove("backupSelectedLineGroupIndex");
//		}

		public void updateLineGroupDetailsBackToXMLConfig()
		{
			LineGroupsAlgorithm lineGroup;
			// Clear all exising line group and create again
			for (Iterator<String> iterator = mapXMLDfrs.keySet().iterator(); iterator.hasNext();) {
				String dfrId = iterator.next();
				DFR xmlDfr = mapXMLDfrs.get(dfrId);
				//			LineGroups xmlLineGroups = xmlDfr.getDataPool().getLineGroups();
				if (xmlDfr.getDataPool().isSetLineGroups())
				{
					xmlDfr.getDataPool().unsetLineGroups();
				}
//				logger.debug("In UpdateLineGroupDetailsBackToXMLConfig method "+xmlLineGroups+" lstLineGroup "+lstLineGroups);
				
			}

			for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator
					.hasNext();) {
				lineGroup = iterator.next();
//				logger.debug("Line Group name "+lineGroup.getName()+" Chassis "+lineGroup.getChassis() +" is local? "+lineGroup.isLocal());
				if (lineGroup.isLocal() && lineGroup.getChassis() != null)
				{
//					logger.debug("Local line group "+lineGroup.getName());
					updateLocalLineGroupToXMLConfig(lineGroup);
				}
				else
				{
//					logger.debug("Global line group "+lineGroup.getName());
					updateGlobalLineGroupToXMLConfig(lineGroup);
				}
			}
		}

		private void updateGlobalLineGroupToXMLConfig(LineGroupsAlgorithm lineGroup)
		{
			boolean booNew = false;
			SubStation currentSubstation = (SubStation) session.get("selectedSubstation");
			GlobalLineGroups globalLineGroups = currentSubstation.getGlobalLineGroups();
//			logger.debug("In UpdateLineGroupDetailsBackToXMLConfig method "+globalLineGroups+" lstLineGroup "+lstLineGroups);
			if (globalLineGroups == null)
			{
				globalLineGroups = currentSubstation.addNewGlobalLineGroups();
				globalLineGroups.setName("Global Line Groups");
				booNew = true;
			}
			else
			{
//				while (globalLineGroups.sizeOfGlobalLineGroupArray() > 0) {
//					globalLineGroups.removeGlobalLineGroup(0);
//				}
			}
			GlobalLineGroup globalLineGroup;
			LgAnalogs lgAnalogs;
			LineGroupChannelsDTO lineGroupChannelsDTO;
			AnalogChannels analogChannels;
			AnalogChannelDTO lgAnalogChannels ;
			List<AnalogChannelDTO> lstLineGroupAnalogs;
			globalLineGroup = null;
			if (!booNew)
			{
				globalLineGroup = getGlobalLineGroupAlreadyAvailable(globalLineGroups, lineGroup);
			}
			if (globalLineGroup == null)
			{
				globalLineGroup = globalLineGroups.addNewGlobalLineGroup();					
				lgAnalogs = globalLineGroup.addNewLgAnalogs();
				lgAnalogs.setName("Linegroup Analog Input");
			}
			else
			{
				lgAnalogs = globalLineGroup.getLgAnalogs();
				while (lgAnalogs.sizeOfAnalogChannelsArray() > 0)
				{
					lgAnalogs.removeAnalogChannels(0);
				}
			}
			globalLineGroup.setId(lineGroup.getId());
			globalLineGroup.setName(lineGroup.getName());
			if (lineGroup.getLineGroupName()!= null && !lineGroup.getLineGroupName().isEmpty())
			{
				globalLineGroup.setLineGroupName(lineGroup.getLineGroupName());
			}
			else
			{
				globalLineGroup.setLineGroupName(lineGroup.getName());
			}
			globalLineGroup.setAutoCalc(lineGroup.getEnableAutoCalc());
			globalLineGroup.setPositiveResistance(lineGroup.getPositiveResistance());
			globalLineGroup.setPositiveReactance(lineGroup.getPositiveReactance());
			globalLineGroup.setZeroResistance(lineGroup.getZeroResistance());
			globalLineGroup.setZeroReactance(lineGroup.getZeroReactance());
			globalLineGroup.setLineMiles(lineGroup.getLineMiles());
			if (lineGroup.getDecisionLogic() != null && !lineGroup.getDecisionLogic().isEmpty())
			{
				globalLineGroup.setDecisionLogic(lineGroup.getDecisionLogic());
			}
			lstLineGroupAnalogs = lineGroup.getInputChannels();
//			logger.debug("lineGroup.getInputChannels() size "+lstLineGroupAnalogs.size());
			for (Iterator<AnalogChannelDTO> iteratorAnalogs = lstLineGroupAnalogs.iterator(); iteratorAnalogs
					.hasNext();) {
				lgAnalogChannels = iteratorAnalogs.next();
				// START: 27-Jan-2020 - Transducer implementation - Do not populate transducer channels to the line groups list
				if (lgAnalogChannels.isTransducer())
				{
					continue;
				}
				// END: 27-Jan-2020
				lineGroupChannelsDTO = createRmsAlgorithmAndExport(lineGroup, lgAnalogChannels);
				analogChannels = lgAnalogs.addNewAnalogChannels();
				analogChannels.setName("lg_"+lgAnalogChannels.getName());
				analogChannels.setChannelId(lineGroupChannelsDTO.getChannelId());
				analogChannels.setRecId(lineGroupChannelsDTO.getDfrId());
				analogChannels.setExpId(lineGroupChannelsDTO.getExportId());
			}
			// START: 16-Oct-2019 Implemented a comments section for line group where customer can add custom name value pair (Georgia Power)
			if (lineGroup.getComments() != null && !lineGroup.getComments().isEmpty())
			{
				globalLineGroup.setComments(lineGroup.getComments());
			}
			// END: 16-Oct-2016

		}

		private void updateLocalLineGroupToXMLConfig(LineGroupsAlgorithm lineGroup)
		{
			DFR xmlDfr = mapXMLDfrs.get(lineGroup.getChassis());
			LineGroups xmlLineGroups = xmlDfr.getDataPool().getLineGroups();
			if (xmlLineGroups == null)
			{
				xmlLineGroups = xmlDfr.getDataPool().addNewLineGroups();
				xmlLineGroups.setName("LineGroups");
			}
//			xmlDfr.getDataPool().unsetLineGroups();
//			logger.debug("In UpdateLineGroupDetailsBackToXMLConfig method "+xmlLineGroups+" lstLineGroup "+lstLineGroups);
//				xmlLineGroups = xmlDfr.getDataPool().addNewLineGroups();
//				xmlLineGroups.setName("Line Groups");
			LineGroup xmlLineGroup = null;
//			xmlLineGroup = getLocalLineGroupAlreadyAvailable(xmlLineGroups, lineGroup);
			xmlLineGroup = xmlLineGroups.addNewLineGroup();		
			xmlLineGroup.setName(lineGroup.getName());
			// To store user entered name 23-July-2015
			if (lineGroup.getLineGroupName() != null && !lineGroup.getLineGroupName().isEmpty())
			{
				xmlLineGroup.setLineGroupName(lineGroup.getLineGroupName());
			}
			else
			{
				xmlLineGroup.setLineGroupName(lineGroup.getName());
			}
			
			xmlLineGroup.setId(lineGroup.getId());
			xmlLineGroup.setAutoCalc(lineGroup.getEnableAutoCalc());
			xmlLineGroup.setPositiveResistance(lineGroup.getPositiveResistance());
			xmlLineGroup.setPositiveReactance(lineGroup.getPositiveReactance());
			xmlLineGroup.setZeroResistance(lineGroup.getZeroResistance());
			xmlLineGroup.setZeroReactance(lineGroup.getZeroReactance());
			xmlLineGroup.setLineMiles(lineGroup.getLineMiles());

			// START: 16-Feb-2017 - Save the decision logic even if it is blank
			if (lineGroup.getDecisionLogic() != null && !lineGroup.getDecisionLogic().isEmpty())
			{
				xmlLineGroup.setDecisionLogic(lineGroup.getDecisionLogic());
			}
			else
			{
				xmlLineGroup.setDecisionLogic("NONE");
			}
			// END: 16-Feb-2017
			for (Iterator<String> iteratorInputTags = lineGroup.getLstInputTagValues().iterator(); iteratorInputTags.hasNext();) {
				String inputTagValue = iteratorInputTags.next();
				xmlLineGroup.addInput(inputTagValue);
			}

			// START: 16-Oct-2019 Implemented a comments section for line group where customer can add custom name value pair (Georgia Power)  
			if (lineGroup.getComments() != null && !lineGroup.getComments().isEmpty())
			{
				logger.debug("DELETE-DEBUG: lineGroup comments "+lineGroup.getComments());
				xmlLineGroup.setComments(lineGroup.getComments());
			}
			// END: 16-Oct-2019

				
		}
		
		private GlobalLineGroup getGlobalLineGroupAlreadyAvailable(
			GlobalLineGroups globalLineGroups, LineGroupsAlgorithm lineGroup) {
			GlobalLineGroup reqdGlobalLineGroup = null;
			logger.debug("Is line group available? id "+lineGroup.getId()+" - "+lineGroup.getName()+" - "+lineGroup.getLineGroupName());
			GlobalLineGroup[] arrGlobalLineGroup = globalLineGroups.getGlobalLineGroupArray();
			if (arrGlobalLineGroup != null)
			{
				for (int i = 0; i < arrGlobalLineGroup.length; i++) {
//					logger.debug("Comparing with "+arrGlobalLineGroup[i].getId()+" - "+arrGlobalLineGroup[i].getName());
					if (arrGlobalLineGroup[i].getId() == lineGroup.getId())
					{
						reqdGlobalLineGroup = arrGlobalLineGroup[i]; 
						break;
					}
				}
			}
//			logger.debug("Is line group "+lineGroup.getId()+" - "+lineGroup.getName()+"already available? "+reqdGlobalLineGroup);
			return reqdGlobalLineGroup;
		}

		@SuppressWarnings("unused")
		private LineGroup getLocalLineGroupAlreadyAvailable(
				LineGroups lineGroups, LineGroupsAlgorithm lineGroup) {
				LineGroup reqdLineGroup = null;
				logger.debug("Is line group available? id "+lineGroup.getId()+" - "+lineGroup.getName());
				LineGroup[] arrLineGroup = lineGroups.getLineGroupArray();
				if (arrLineGroup != null)
				{
					for (int i = 0; i < arrLineGroup.length; i++) {
//						logger.debug("Comparing with "+arrGlobalLineGroup[i].getId()+" - "+arrGlobalLineGroup[i].getName());
						if (arrLineGroup[i].getName().equalsIgnoreCase(lineGroup.getName()))
						{
							reqdLineGroup = arrLineGroup[i]; 
							break;
						}
					}
				}
//				logger.debug("Is line group "+lineGroup.getId()+" - "+lineGroup.getName()+"already available? "+reqdGlobalLineGroup);
				return reqdLineGroup;
			}
		
		private void deleteGlobalLineGroupFromXml(
				GlobalLineGroups globalLineGroups, LineGroupsAlgorithm lineGroup) {
				logger.debug("Is line group available? id "+lineGroup.getId()+" - "+lineGroup.getName()+" - "+lineGroup.getLineGroupName());
				GlobalLineGroup[] arrGlobalLineGroup = globalLineGroups.getGlobalLineGroupArray();
				if (arrGlobalLineGroup != null)
				{
					for (int i = 0; i < arrGlobalLineGroup.length; i++) {
						logger.debug("Comparing with "+arrGlobalLineGroup[i].getId()+" - "+arrGlobalLineGroup[i].getName());
						if (arrGlobalLineGroup[i].getId() == lineGroup.getId())
						{
							globalLineGroups.removeGlobalLineGroup(i);
							break;
						}
					}
				}
			}

		@SuppressWarnings("unused")
		private void clearLinegroups(LineGroups lineGroup)
		{
			while(lineGroup.sizeOfLineGroupArray()> 0)
			{
				lineGroup.removeLineGroup(0);
			}
		}

	private List<LineGroupsAlgorithm> editLineGroups() {
		LineGroupsAlgorithm lineGroups;

		List<AnalogChannelDTO> lgInput;
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
		lstLineGroups = new ArrayList<LineGroupsAlgorithm>();
		for (; mapXmlDfrIterator.hasNext();) {
			dfrKey = mapXmlDfrIterator.next();
			xmlDfr = mapXMLDfrs.get(dfrKey);
			if (xmlDfr.getDataPool().getLineGroups() != null) {
				String type;
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
//					logger.debug("xmlLineGroup[i].getName() "+xmlLineGroup[i].getName());
					lineGroups.setId(xmlLineGroup[i].getId());
					lineGroups.setName("LG_"+xmlLineGroup[i].getId());
					lineGroups.setEnableAutoCalc(xmlLineGroup[i].getAutoCalc());
					lineGroups.setPositiveResistance(xmlLineGroup[i].getPositiveResistance());
					lineGroups.setPositiveReactance(xmlLineGroup[i].getPositiveReactance());
					lineGroups.setZeroResistance(xmlLineGroup[i].getZeroResistance());
					lineGroups.setZeroReactance(xmlLineGroup[i].getZeroReactance());
					lineGroups.setLineMiles(xmlLineGroup[i].getLineMiles());
//					logger.debug("Decision logic populated "+xmlLineGroup[i].getDecisionLogic());
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
						logger.debug("DELETE-DEBUG: Before Setting Comments from xml..."+xmlLineGroup[i].getComments());
						lineGroups.setComments(xmlLineGroup[i].getComments());
						logger.debug("DELETE-DEBUG: After Setting Comments from xml..."+lineGroups.getComments());
					}
					// END: 16-Oct-2019
//					logger.debug("\t\t\t Local line group name "+lineGroups.getName());
					lgInput = getAnalogChannelsForNames(xmlLineGroup[i], dfrKey);
					
					// getLineGroupType signature changed to send entire object instead of just list of input channels 
					lineGroups.setInputChannels(lgInput);
					type = getLineGroupType(lineGroups);
					// 22-Mar-2021 - look for mixed channels (line-line and line-neutral voltages)
					// type will be empty if mixed lines
					if (type.isEmpty())
					{
						lineGroups.setMixedVoltageChannels(true);
					}
					else
					{
						lineGroups.setMixedVoltageChannels(false);
					}
					// 22-Mar-2021
					
					lineGroups.setType(type);
					lineGroups.setLocal(true);
					lineGroups.setChassis(dfrKey);
					lstLineGroups.add(lineGroups);
				}
			}
		}
//		if (lstLineGroups != null && !lstLineGroups.isEmpty())
//		{
//			// Check for all linegroups that are invalid but associated with one or more measurements
//			logger.debug("About to check for associated measurements checkForAssociatedMeasurements..lstLineGroups size? "+lstLineGroups.size());
//			 checkForAssociatedMeasurements();
//		}
		  GlobalLineGroups globalLineGroups = currentSubstation.getGlobalLineGroups();
		  
		  if (globalLineGroups != null)
		  {
			  GlobalLineGroup[] arrGlobalLineGroup = globalLineGroups.getGlobalLineGroupArray();
			  if (arrGlobalLineGroup != null && arrGlobalLineGroup.length > 0)
			  {
				  if (lstLineGroups == null)
				  {
					  lstLineGroups = new ArrayList<LineGroupsAlgorithm>();
				  }
					List<LineGroupChannelsDTO> lstLineGroupChannelsDTOs;
					LineGroupChannelsDTO lineGroupChannelsDTO;
					AnalogChannels[] analogChannels;
					String type;
//					logger.debug("Total Global line groups "+arrGlobalLineGroup.length);
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
						lgInput = getAnalogChannelsForLineGroups(lstLineGroupChannelsDTOs);
//						populateRMSExportForLineGroupChannels(lgInput);
						lineGroups.setInputChannels(lgInput);
						// Changed the method signature to send entire object
//						type = getLineGroupType(lgInput);
						type = getLineGroupType(lineGroups);
						lineGroups.setType(type);
						lineGroups.getMapLinegroupWarningMessages().put(M9kConstants.KEY_MULTIPLE_CHASSIS, M9kConstants.MSG_MULTIPLE_CHASSIS);
						lineGroups.setLocal(false);
						lstLineGroups.add(lineGroups);					
				  }
//				  logger.debug("Total Line Groups available"+lstLineGroups.size() );
			  }
			  
		  }
		  

		  logger.debug("Returning from  editLineGroups "+lstLineGroups);
		return lstLineGroups;
	}

	
	@SuppressWarnings("unused")
	private void populateRMSExportForLineGroupChannels(
			List<AnalogChannelDTO> lgInput) {
		AnalogChannelDTO analogChannelDTO;
		for (Iterator<AnalogChannelDTO> iterator = lgInput.iterator(); iterator.hasNext();) {
			analogChannelDTO = (AnalogChannelDTO) iterator
					.next();
			constructLineGroupRmsExport(analogChannelDTO );
			
		}
	}
	
	@SuppressWarnings("unused")
	private void constructLineGroupRmsExport(AnalogChannelDTO analogChannelDTO)
	{
		DFR xmlConfigDfr;
		String rmsName;
		String inputValue = "";
		String analogName= "";
		String exportName = "";
		xmlConfigDfr = mapXMLDfrs.get(analogChannelDTO.getChassis());
		rmsName = M9kConstants.RMS+"_"+analogChannelDTO.getName();
		AnalogInput[] analogInputs = xmlConfigDfr.getDataPool().getChannels().getAnalogs().getAnalogInputArray();		
		
		  Exports exports = xmlConfigDfr.getDataPool().getExports();
		  Export export[] = null;
		  if (exports != null)
		  {
			  export = exports.getExportArray();
		  }
		Rms rms[] = xmlConfigDfr.getDataPool().getAlgorithms().getRmsArray();		
//		rmsName = xmlAlgoType.substring(xmlAlgoType.indexOf("(")+1, xmlAlgoType.indexOf(")"));
		logger.debug("Rms Name: "+rmsName);

	}
	

	private List<AnalogChannelDTO> getAnalogChannelsForNames(
			LineGroup srcLineGroup, String chassis) {
		String channelName;
		AnalogChannelDTO analogDto;
		List<AnalogChannelDTO> lgInput = new ArrayList<AnalogChannelDTO>();
		String[] lgInputNames = srcLineGroup
				.getInputArray();
		for (int i = 0; i < lgInputNames.length; i++) {
			channelName = lgInputNames[i].substring(
					lgInputNames[i].indexOf("AnalogInput(") + 12,
					lgInputNames[i].lastIndexOf(")"));
			for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels()
					.iterator(); iterator.hasNext();) {
				analogDto = iterator.next();
				if (analogDto.getChassis().equalsIgnoreCase(chassis) && analogDto.getName().equalsIgnoreCase(channelName)) 
				{
					if (!analogDto.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE))
					{
						lgInput.add(analogDto);
					}
					else
					{
						if (lstAffectedLineGroups == null)
						{
							lstAffectedLineGroups = new ArrayList<String>();
						}
						lstAffectedLineGroups.add(srcLineGroup.getId()+"-"+srcLineGroup.getName()+"-"+srcLineGroup.getLineGroupName());						
					}
//					logger.info("Added " + analogDto.getName());
					break;
				}
			}
			if (getLstVirtualChannels() != null && !getLstVirtualChannels().isEmpty())
			{
				// loop thru virtual channels list
				for (Iterator<AnalogChannelDTO> iterator = getLstVirtualChannels()
						.iterator(); iterator.hasNext();) {
					analogDto = iterator.next();
					if (analogDto.getChassis().equalsIgnoreCase(chassis) && analogDto.getName().equalsIgnoreCase(channelName)) 
					{
						if (!analogDto.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE))
						{
							lgInput.add(analogDto);
						}
						else
						{
							if (lstAffectedLineGroups == null)
							{
								lstAffectedLineGroups = new ArrayList<String>();
							}
							lstAffectedLineGroups.add(srcLineGroup.getId()+"-"+srcLineGroup.getName()+"-"+srcLineGroup.getLineGroupName());						
						}
						break;
					}
				}
			}

		}
		if (lstAffectedLineGroups != null)
		{
			session.put("linegroupsWarnings", lstAffectedLineGroups);
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
				if (analogDto.getChannel().equalsIgnoreCase(""+channelId)
						&& !analogDto.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE) // 05-Apr-2021 - Remove channels with NO phase
						) {
					lgInput.add(analogDto);
					logger.debug("Added " + analogDto.getName());
					break;
				}
			}
		}
		return lgInput;
	}

	
	private String getLineGroupType(LineGroupsAlgorithm lineGroupsAlgorithm) {
		String type = "";
		int typeVoltage = 0;
		int typeCurrent = 0;
		int phaseA_Volts = 0;
		int phaseB_Volts = 0;
		int phaseC_Volts = 0;
		int phaseA_Amps = 0;
		int phaseB_Amps = 0;
		int phaseC_Amps = 0;
		// START: 20-Mar-2021 - Delta Transformer - Added more phase types
		int phaseAB_Volts = 0;
		int phaseBC_Volts = 0;
		int phaseCA_Volts = 0;
		boolean isLineToLine = false;
		boolean isLineToNeutral = false;
		
		lineGroupsAlgorithm.setMissingAVoltageChannel(false);
		lineGroupsAlgorithm.setMissingACurrentChannel(false);
		lineGroupsAlgorithm.setMissingTwoCurrentChannels(false);

		// END: 20-Mar-2021 - Delta Transformer - Added more phase types		
		List<AnalogChannelDTO> linegroupChannels = lineGroupsAlgorithm.getInputChannels();
		for (Iterator<AnalogChannelDTO> iterator = linegroupChannels.iterator(); iterator
				.hasNext();) {
			AnalogChannelDTO analogChannelDTO = iterator.next();
			if (analogChannelDTO.getPhase().equalsIgnoreCase(M9kConstants.PHASE_NEUTRAL))
			{
				logger.debug("Skipping Neutral channels...");
				continue;
			}
//			 logger.info("Input Type..."+analogChannelDTO.getInputType());
			if (analogChannelDTO.getInputType().startsWith(
					M9kConstants.VOLTAGE_AC)) {
				typeVoltage++;
				if (analogChannelDTO.getPhase().equalsIgnoreCase("A")) {
					phaseA_Volts++;
					isLineToNeutral=true;
				} else if (analogChannelDTO.getPhase().equalsIgnoreCase("B")) {
					phaseB_Volts++;
					isLineToNeutral=true;
				} else if (analogChannelDTO.getPhase().equalsIgnoreCase("C")) {
					phaseC_Volts++;
					isLineToNeutral=true;
				}else if (analogChannelDTO.getPhase().equalsIgnoreCase("AB")) {
					phaseAB_Volts++;
					isLineToLine=true;
				}else if (analogChannelDTO.getPhase().equalsIgnoreCase("BC")) {
					phaseBC_Volts++;
					isLineToLine=true;
				}else if (analogChannelDTO.getPhase().equalsIgnoreCase("CA")) {
					phaseCA_Volts++;
					isLineToLine=true;
				}
				// Check if the line group has both line-line and line-neutral and no measurements can be calculated, if it is
				// Also, if there are more than one channel with same phase then no measurements can be calculated
				if ((isLineToLine && isLineToNeutral))
				{
					lineGroupsAlgorithm.setMixedVoltageChannels(true);
					if ((phaseA_Volts > 1) || (phaseB_Volts > 1) || (phaseC_Volts > 1)
							|| (phaseAB_Volts > 1) || (phaseBC_Volts > 1) || (phaseCA_Volts > 1))
					{
						lineGroupsAlgorithm.setInvalidCombination(true);
					}
					return "";
				}
			} else if (analogChannelDTO.getInputType().startsWith(
					M9kConstants.CURRENT_AC_INTERNAL)
					|| analogChannelDTO.getInputType().startsWith(
							M9kConstants.CURRENT_AC_EXTERNAL)
					// START: 29-Jun-2020 - Hall Effect offset correction implementation
					|| analogChannelDTO.getInputType().startsWith(
							M9kConstants.CURRENT_AC_HALL_EFFECT_EXTERNAL_SHUNT)
					// END: 29-Jun-2020
					) {
				typeCurrent++;
				if (analogChannelDTO.getPhase().equalsIgnoreCase("A")) {
					phaseA_Amps++;
				} else if (analogChannelDTO.getPhase().equalsIgnoreCase("B")) {
					phaseB_Amps++;
				} else if (analogChannelDTO.getPhase().equalsIgnoreCase("C")) {
					phaseC_Amps++;
				}
			}
			if ((phaseA_Amps > 1) || (phaseB_Amps > 1) || (phaseC_Amps > 1)) // If there are more than one current channel with same phase then no measurements can be calculated
			{
				lineGroupsAlgorithm.setInvalidCombination(true);
				return "";
			}

		}
		lineGroupsAlgorithm.setMixedVoltageChannels(false);
		lineGroupsAlgorithm.setInvalidCombination(false);
		// logger.info("typeVoltage..."+typeVoltage);
		// logger.info("typeCurrent..."+typeCurrent);
		// logger.info("phaseA Volts..."+phaseA_Volts);
		// logger.info("phaseA Amps..."+phaseA_Amps);
		// logger.info("phaseB Volts..."+phaseB_Volts);
		// logger.info("phaseB Amps..."+phaseB_Amps);
		// logger.info("phaseC Volts..."+phaseC_Volts);
		// logger.info("phaseC Amps..."+phaseC_Amps);

		if (typeVoltage == 3 && typeCurrent == 0) { // All voltage phases and no currents

			// 20-Mar-2021 - Either all line-line or all line-neutral only. We cannot mix both
			if (((phaseA_Volts == 1 && phaseB_Volts == 1 && phaseC_Volts == 1)
					&& (phaseAB_Volts == 0 && phaseBC_Volts == 0 && phaseCA_Volts == 0))
					|| (phaseA_Volts == 0 && phaseB_Volts == 0 && phaseC_Volts == 0)
					&& (phaseAB_Volts == 1 && phaseBC_Volts == 1 && phaseCA_Volts == 1)) {
				type = M9kConstants.SEQ_VOLTAGE;
			}
		} else if (typeVoltage == 0 && typeCurrent == 3) { // All current phases and no voltages  
			if (phaseA_Amps == 1 && phaseB_Amps == 1 && phaseC_Amps == 1) {
				type = M9kConstants.SEQ_CURRENT;
			}
		}
		else if (typeVoltage == 3 && typeCurrent == 3) { // All 3 voltage channels (line-line or line-neutral) and all 3 currents
			if (phaseA_Amps == 1 && phaseB_Amps == 1 && phaseC_Amps == 1 && 
					((phaseA_Volts  == 1 && phaseB_Volts == 1  && phaseC_Volts == 1) 
					|| (phaseAB_Volts  == 1 && phaseBC_Volts == 1 && phaseCA_Volts == 1)))
			{
				type = M9kConstants.ALL_PHASE;
			}
		}
		else if (typeVoltage == 2 && typeCurrent == 3) { // 2 voltage channels (line-line or line-neutral) and all 3 currents
			if (phaseA_Amps == 1 && phaseB_Amps == 1 && phaseC_Amps == 1 && 
					((phaseA_Volts + phaseB_Volts) == 2 || (phaseB_Volts +phaseC_Volts) == 2  || (phaseA_Volts +phaseC_Volts) == 2) 
					|| (phaseAB_Volts + phaseBC_Volts) == 2 || (phaseBC_Volts + phaseCA_Volts) == 2 || (phaseCA_Volts + phaseAB_Volts) == 2)
			{
				type = M9kConstants.CURRENT_ALL_PHASE;
				lineGroupsAlgorithm.setMissingAVoltageChannel(true);
			}
		}
		else if (typeVoltage == 3 && typeCurrent == 2) { // All 3 voltage channels (line-line or line-neutral) and 2 current channels
			if (( (phaseA_Volts == 1 && phaseB_Volts == 1 && phaseC_Volts == 1)
					|| (phaseAB_Volts == 1 && phaseBC_Volts == 1 && phaseCA_Volts == 1)))
			{
				if ((phaseA_Amps + phaseB_Amps) == 2)
				{
					type = M9kConstants.VOLTAGE_ALL_PHASE_MISSING_PHASE_C;
				}
				else if ((phaseB_Amps +phaseC_Amps) == 2)
				{
					type = M9kConstants.VOLTAGE_ALL_PHASE_MISSING_PHASE_A;
				}
				if ((phaseA_Amps +phaseC_Amps) == 2)
				{
					type = M9kConstants.VOLTAGE_ALL_PHASE_MISSING_PHASE_B;
				}
				lineGroupsAlgorithm.setMissingACurrentChannel(true);
			}
		}
		else if (typeVoltage == 2 && typeCurrent == 2) { // Two voltage channels and two current channels
			if ((phaseA_Volts + phaseB_Volts) == 2 || (phaseB_Volts + phaseC_Volts) == 2 || (phaseA_Volts + phaseC_Volts) == 2
					|| (phaseAB_Volts + phaseBC_Volts) == 2 || (phaseBC_Volts + phaseCA_Volts) == 2 || (phaseAB_Volts + phaseCA_Volts) == 2) // Check for unique voltage pairs
			{
				if ((phaseA_Amps + phaseB_Amps) == 2)
				{
					type = M9kConstants.ALL_PHASE_MISSING_PHASE_C;
				}
				else if ((phaseB_Amps +phaseC_Amps) == 2)
				{
					type = M9kConstants.ALL_PHASE_MISSING_PHASE_A;
				}
				if ((phaseA_Amps +phaseC_Amps) == 2)
				{
					type = M9kConstants.ALL_PHASE_MISSING_PHASE_B;
				}
				lineGroupsAlgorithm.setMissingAVoltageChannel(true);
				lineGroupsAlgorithm.setMissingACurrentChannel(true);
			}
		}
		else if (typeVoltage == 1 && typeCurrent == 1) {
			if ((phaseA_Volts + phaseA_Amps) == 2) {
				type = M9kConstants.A_PHASE;
			} else if ((phaseB_Volts + phaseB_Amps) == 2) {
				type = M9kConstants.B_PHASE;
			} else if ((phaseC_Volts + phaseC_Amps) == 2) {
				type = M9kConstants.C_PHASE;
			}
		}
		else if (typeCurrent == 1)
		{
			lineGroupsAlgorithm.setMissingTwoCurrentChannels(true);
			// Check for unique voltage channels. If 3 voltage channels then we should allow Sequence voltage calculation
			if (((phaseA_Volts == 1 && phaseB_Volts == 1 && phaseC_Volts == 1) // If we have 3 voltagechannels
					|| (phaseAB_Volts == 1 && phaseBC_Volts == 1 && phaseCA_Volts == 1))) 
			{
				if (phaseA_Amps == 1)
				{
					type = M9kConstants.ALL_PHASE_MISSING_PHASE_B_C_AND_SEQ_VOLTAGE;
				}
				else if (phaseB_Amps == 1)
				{
					type = M9kConstants.ALL_PHASE_MISSING_PHASE_C_A_AND_SEQ_VOLTAGE;
				}
				else if (phaseC_Amps == 1)
				{
					type = M9kConstants.ALL_PHASE_MISSING_PHASE_A_B_AND_SEQ_VOLTAGE;
				}
			}
			else
//					((phaseA_Volts + phaseB_Volts) == 2 || (phaseB_Volts + phaseC_Volts) == 2 || (phaseA_Volts + phaseC_Volts) == 2 // If we have 2 voltage channels
//					|| (phaseAB_Volts + phaseBC_Volts) == 2 || (phaseBC_Volts + phaseCA_Volts) == 2 || (phaseAB_Volts + phaseCA_Volts) == 2)
			{
				if (phaseA_Amps == 1)
				{
					type = M9kConstants.ALL_PHASE_MISSING_PHASE_B_C;
				}
				else if (phaseB_Amps == 1)
				{
					type = M9kConstants.ALL_PHASE_MISSING_PHASE_C_A;
				}
				else if (phaseC_Amps == 1)
				{
					type = M9kConstants.ALL_PHASE_MISSING_PHASE_A_B;
				}
				lineGroupsAlgorithm.setMissingAVoltageChannel(true);
			}
		}
		else if (typeVoltage == 1)
		{
			// Check for unique voltage channels. If 3 voltage channels then we should allow Sequence voltage calculation
			if (phaseA_Amps == 1 && phaseA_Amps == 1 && phaseA_Amps == 1) // If we have 3 voltagechannels
			{
				if (phaseA_Volts == 1)
				{
					type = M9kConstants.A_PHASE_AND_SEQ_CURRENT;
				}
				else if (phaseB_Volts == 1)
				{
					type = M9kConstants.B_PHASE_AND_SEQ_CURRENT;
				}
				else if (phaseC_Volts == 1)
				{
					type = M9kConstants.C_PHASE_AND_SEQ_CURRENT;
				}
			}
			else
//					((phaseA_Volts + phaseB_Volts) == 2 || (phaseB_Volts + phaseC_Volts) == 2 || (phaseA_Volts + phaseC_Volts) == 2 // If we have 2 voltage channels
//					|| (phaseAB_Volts + phaseBC_Volts) == 2 || (phaseBC_Volts + phaseCA_Volts) == 2 || (phaseAB_Volts + phaseCA_Volts) == 2)
			{
				if (phaseA_Amps+phaseA_Volts == 2)
				{
					type = M9kConstants.A_PHASE;
				}
				else if (phaseB_Amps+phaseB_Volts == 2)
				{
					type = M9kConstants.B_PHASE;
				}
				else if (phaseC_Amps+phaseC_Volts == 2)
				{
					type = M9kConstants.C_PHASE;
				}
			}
		}
		
		logger.debug("Returning Line group "+lineGroupsAlgorithm+" type "+type);
		return type;

	}

	/**
	 * To find whether linegroup spans across different dfrs (Global) or within one dfr(local)
	 * @param linegroupChannels
	 * @return
	 */
	private String getChassisIfLocal(List<AnalogChannelDTO> linegroupChannels) {
		String chassis = null;
		AnalogChannelDTO analogChannelDTO;
		for (Iterator<AnalogChannelDTO> iterator = linegroupChannels.iterator(); iterator
				.hasNext();) {
			analogChannelDTO =  iterator
					.next();
			logger.debug("\t\t\t\t\t\tAnalog name "+analogChannelDTO.getName()+" chassis "+analogChannelDTO.getChassis());
			if (chassis == null)
			{
				chassis = analogChannelDTO.getChassis();
			}
			else
			{
				if (!chassis.equalsIgnoreCase(analogChannelDTO.getChassis()))
				{
					chassis = null;
					break;
				}
			}
			
		}
		return chassis;
	}	
	@SuppressWarnings("unused")
	private void checkAndUpdateLineGroups() {
		List<LineGroupsAlgorithm> lstNewLineGroups = new ArrayList<LineGroupsAlgorithm>();
		lstNewLineGroups = createLineGroups();

		for (Iterator<LineGroupsAlgorithm> iterator = lstNewLineGroups
				.iterator(); iterator.hasNext();) {
			LineGroupsAlgorithm lineGroupsAlgorithm = iterator.next();
			if (!lstLineGroups.contains(lineGroupsAlgorithm)) {
				lstLineGroups.add(lineGroupsAlgorithm);
			}
		}
	}			

	private List<LineGroupsAlgorithm> createLineGroups() {
		logger.debug("Entered createLineGroups()");
		List<LineGroupsAlgorithm> lstLineGroups = new ArrayList<LineGroupsAlgorithm>();
		LineGroupsAlgorithm lineGroups;
		AnalogChannelDTO analogDto;
		List<AnalogChannelDTO> lstChannelsGroup;
		Map<String, List<AnalogChannelDTO>> mapLineGroups = new HashMap<String, List<AnalogChannelDTO>>();
		for (Iterator<AnalogChannelDTO> iterator = getLstAnalogChannels()
				.iterator(); iterator.hasNext();) {
			analogDto = iterator.next();
			if (analogDto.getCircuitName() != null
					&& !analogDto.getCircuitName().isEmpty() && !analogDto.getPhase().equalsIgnoreCase(M9kConstants.NO_PHASE)) {
				if (mapLineGroups.get(analogDto.getCircuitName()) != null) {
					mapLineGroups.get(analogDto.getCircuitName())
							.add(analogDto);
//					logger.debug("Adding Rest of the analogs "+analogDto);
				} else {
					lstChannelsGroup = new ArrayList<AnalogChannelDTO>();
					lstChannelsGroup.add(analogDto);
					
					mapLineGroups.put(analogDto.getCircuitName(),
							lstChannelsGroup);
//					logger.debug("Adding first analog "+analogDto);
				}
			}
		}
		String key = "";
		String type;
		String chassis;
		for (Iterator<String> iterator = mapLineGroups.keySet().iterator(); iterator
				.hasNext();) {
			key = iterator.next();
			List<AnalogChannelDTO> inputValues = (List<AnalogChannelDTO>) mapLineGroups
					.get(key);
			logger.debug("Total analogs "+inputValues.size()+" anlogs"+inputValues);
			if (inputValues.size() > 1) {
				// 24-Mar-2021 - Changed the method signature to send the entire object instead of just list of input channels
				lineGroups = new LineGroupsAlgorithm();
				lineGroups.setInputChannels(inputValues);
				type = getLineGroupType(lineGroups);
				if (!type.isEmpty())
				{
//					lineGroups = new LineGroupsAlgorithm();
					// 09-Apr-2021 - Line group id generation moved to staionDTO
//					lineGroups.setId(LineGroupsAlgorithm.getNextAvailableId());
					lineGroups.setId(getStationDetails().getNextAvailableLineGroupId());
					lineGroups.setName("LG_"+lineGroups.getId());
					lineGroups.setLineGroupName(key);
					lineGroups.setInputChannels(inputValues);
					lineGroups.setType(type);
					chassis = getChassisIfLocal(inputValues);
					if (chassis != null)
					{
						lineGroups.setLocal(true);
						lineGroups.setChassis(chassis);
					}
					else
					{
						lineGroups.setLocal(false);
					}
//					logger.debug("DefaultCreateLineGroups: Line group name "+lineGroups.getName()+" is Local? "+lineGroups.isLocal()+" chassis "+lineGroups.getChassis());
					logger.debug("Input chnannels set "+lineGroups.getInputChannels());
					lstLineGroups.add(lineGroups);
				}
				else
				{
					
				}
			}
		}
		logger.debug("Returning from createLineGroups "+lstLineGroups);
		return lstLineGroups;
	}

	@SuppressWarnings("unused")
	private void populateLineGroups() {
		logger.debug("Entered populateLineGroups method ");
		SubStation currentSubstation = (SubStation) session.get("selectedSubstation");
//		Iterator<String> mapXmlDfrIterator = mapXMLDfrs.keySet().iterator();
//		String dfrKey= null;
//		DFR xmlDfr;
//		for (; mapXmlDfrIterator.hasNext();) {
//			dfrKey = mapXmlDfrIterator.next();
//			xmlDfr = mapXMLDfrs.get(dfrKey);
//		}		
		GlobalLineGroups globalLineGroups = currentSubstation.getGlobalLineGroups();
		if (globalLineGroups == null)
		{
			globalLineGroups = currentSubstation.addNewGlobalLineGroups();
			globalLineGroups.setName("GlobalLineGroups");
		}
		GlobalLineGroup globalLineGroup;
		LineGroupsAlgorithm lineGroup;
		LineGroupChannelsDTO lineGroupChannelsDTO;
		AnalogChannels analogChannels = null;
		AnalogChannelDTO lgAnalogChannelDto; 
		List<AnalogChannelDTO> lstLineGroupAnalogs;
		for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator
				.hasNext();) {
			lineGroup = iterator.next();
			globalLineGroup = globalLineGroups.addNewGlobalLineGroup();
			logger.debug("Line group id to be set "+lineGroup.getId());
			globalLineGroup.setId(lineGroup.getId());
			globalLineGroup.setName(lineGroup.getName());
			globalLineGroup.setAutoCalc(lineGroup.getEnableAutoCalc());
			globalLineGroup.setPositiveResistance(lineGroup.getPositiveResistance());
			globalLineGroup.setPositiveReactance(lineGroup.getPositiveReactance());
			globalLineGroup.setZeroResistance(lineGroup.getZeroResistance());
			globalLineGroup.setZeroReactance(lineGroup.getZeroReactance());
			globalLineGroup.setLineMiles(lineGroup.getLineMiles());
			if (lineGroup.getDecisionLogic() != null && !lineGroup.getDecisionLogic().isEmpty())
			{
				globalLineGroup.setDecisionLogic(lineGroup.getDecisionLogic());
			}
			// START: 16-Oct-2019 Implemented a comments section for line group where customer can add custom name value pair (Georgia Power)
			if (lineGroup.getComments() != null && !lineGroup.getComments().isEmpty())
			{
				globalLineGroup.setComments(lineGroup.getComments());
			}
			// END: 16-Oct-2016
			LgAnalogs lgAnalogs = globalLineGroup.addNewLgAnalogs();
			lgAnalogs.setName("Linegroup Analog Input");
			lstLineGroupAnalogs = lineGroup.getInputChannels();
			logger.debug("lineGroup.getInputChannels() size "+lstLineGroupAnalogs.size());
			for (Iterator<AnalogChannelDTO> iteratorAnalogs = lstLineGroupAnalogs.iterator(); iteratorAnalogs
					.hasNext();) {
				lgAnalogChannelDto = iteratorAnalogs.next();
				logger.debug("Analog channel in the list "+lgAnalogChannelDto);
				lineGroupChannelsDTO = createRmsAlgorithmAndExport(lineGroup, lgAnalogChannelDto);
//				logger.debug("Dfr after update "+mapXMLDfrs.get(lgAnalogChannelDto.getChassis()));
				analogChannels = lgAnalogs.addNewAnalogChannels();
				analogChannels.setName("lg_"+lgAnalogChannelDto.getName());
				analogChannels.setChannelId(lineGroupChannelsDTO.getChannelId());
				analogChannels.setRecId(lineGroupChannelsDTO.getDfrId());
				analogChannels.setExpId(lineGroupChannelsDTO.getExportId());
				
			}
//			for (Iterator<AnalogChannelDTO> iteratorAnalogs = lineGroup.getInputChannels().iterator(); iterator.hasNext();) {
//				lgAnalogChannelDto = iteratorAnalogs.next();
//				logger.debug("Analog channel in the list "+lgAnalogChannelDto);
//				lineGroupChannelsDTO = createRmsAlgorithmAndExport(lgAnalogChannelDto);
//				analogChannels = lgAnalogs.addNewAnalogChannels();
//				analogChannels.setName("lg_"+lgAnalogChannelDto.getName());
//				analogChannels.setChannelId(lineGroupChannelsDTO.getChannelId());
//				analogChannels.setRecId(lineGroupChannelsDTO.getDfrId());
//				analogChannels.setExpId(lineGroupChannelsDTO.getExportId());
//			}
			
			logger.debug("After the For loop. ");

		}
		
//		LineGroups xmlLineGroups = dfrObj.getDataPool().getLineGroups();
//		if (xmlLineGroups == null) {
//			xmlLineGroups = dfrObj.getDataPool().addNewLineGroups();
//			xmlLineGroups.setName("LineGroups");
//		}
//		clearLineGroupDetails(xmlLineGroups);
//		LineGroup xmlLineGroup;
//		LineGroupsAlgorithm lineGroups;
//		for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator
//				.hasNext();) {
//			lineGroups = iterator.next();
//			xmlLineGroup = xmlLineGroups.addNewLineGroup();
//			xmlLineGroup.setName(lineGroups.getName());
//			for (Iterator<String> iteratorInputTags = lineGroups
//					.getLstInputTagValues().iterator(); iteratorInputTags
//					.hasNext();) {
//				String inputTagValue = iteratorInputTags.next();
//				xmlLineGroup.addInput(inputTagValue);
//			}
//
//		}
		
		logger.debug("Returning from populateLineGroups");
	}

	@SuppressWarnings("unused")
	private void clearLineGroupDetails(LineGroups lineGroups) {
		while (lineGroups.sizeOfLineGroupArray() > 0) {
			lineGroups.removeLineGroup(0);
		}
	}

	private LineGroupChannelsDTO createRmsAlgorithmAndExport(LineGroupsAlgorithm lineGroup, AnalogChannelDTO lgAnalogChannels)
	{
		logger.debug("in createRmsAlgorithmAndExport "+lgAnalogChannels);
		LineGroupChannelsDTO lineGroupChannelsDTO = new LineGroupChannelsDTO();
		int expId;
		Exports exports;
		Export export;
		DFR xmlDfr = mapXMLDfrs.get(lgAnalogChannels.getChassis());
		RMSAlgorithm rmsAlgorithm = new RMSAlgorithm(xmlDfr, lgAnalogChannels);
		Rms rms = rmsAlgorithm.createRmsXmlObj();
		logger.debug("Rms "+rms.getName()+" Input "+rms.getInput());
		Algorithms algorithms = xmlDfr.getDataPool().getAlgorithms();
		if (algorithms == null)
		{
			algorithms = xmlDfr.getDataPool().addNewAlgorithms();
			algorithms.setName("Algorithms");
		}
//		algorithms.addNewRms();
//		algorithms.setRmsArray((algorithms.getRmsArray().length-1), rms);
		exports = xmlDfr.getDataPool().getExports();
		String exportName = "LG-"+lineGroup.getId()+"-"+lgAnalogChannels.getName()+"-RMS";
		if (exports == null)
		{
				exports = xmlDfr.getDataPool().addNewExports();
				exports.setName("Exports");
		}
		export = getExportsIfAlreadyExists(exports, exportName);
		if (export == null)
		{
			export = exports.addNewExport();
			export.setName(exportName);
			expId = StationDTO.getNextAvailableExportId();
			export.setId(expId);
		}
		else
		{
			expId = export.getId();
		}
		// Start 14-Oct-2013 Introduced exportName attribute to Export in xml
		export.setExportName(lgAnalogChannels.getDisplayChannel()+"-"+lgAnalogChannels.getCircuitName());
		// End 14-Oct-2013
		
		// Start 08-Jul-2015 Added Measurement type to xml to be added in hte cont table
		export.setMeasurementType(M9kConstants.RMS);
		// End 08-Jul-2015
		
		export.setPhase(lgAnalogChannels.getPhase());
//		export.setUnits("V");
		if (lgAnalogChannels.getInputType().startsWith(M9kConstants.VOLTAGE))
		{
			export.setUnits("V");
		}
		else
		{
			export.setUnits("A");
		}
		export.setInput(getExportXMLInputString(rms.getName()));
		export.setSampleRate(stationDetails.getExportRate());
		
		lineGroupChannelsDTO.setChannelId(Integer.parseInt(lgAnalogChannels.getChannel()));
		lineGroupChannelsDTO.setDfrId(xmlDfr.getSystem().getDfrId());
		lineGroupChannelsDTO.setExportId(expId);
		
		return lineGroupChannelsDTO;
	}
	
	private Export getExportsIfAlreadyExists(Exports exports, String exportName)
	{
		logger.debug("Export Name to be compared "+exportName);
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
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		stationDetails = (StationDTO) session.get("stationDetails");
		accessMode = (String) session.get("accessMode");
	  lstLineGroups = (List<LineGroupsAlgorithm>) session.get("lstLineGroups");
//	  logger.debug("In ManageLineGroupAction lstLineGroups in session "+lstLineGroups);
	  lstAnalogChannels = (List<AnalogChannelDTO>) session.get("lstAnalogChannels");
	  lstVirtualChannels = (List<AnalogChannelDTO>) session.get("lstVirtualChannels");
	  lstDfrDTO = (List<DfrDTO>) session.get("DFRsList");
//	  if (getLineGroupDetails() == null)
//	  {
//		  lineGroupDetails = (List<String>) session.get("lineGroupDetails");
//	  }
		mapXMLDfrs = (Map<String, DFR>) session.get("mapXMLDfrs");
		currentSubstation = (SubStation) session.get("selectedSubstation");
		userDto = (UsersDTO) session.get("userDetails");
		config = (PropertiesConfiguration) session.get("config");
		lstAffectedLineGroups = (List<String>) session.get("linegroupsWarnings");
		lstAssociatedMeasurements = (List<TriggerChannelDTO>) session.get("LINEGROUP_AFFECTED_MEASUREMENTS");
		lstLinegroupsModified = (List<String>) session.get("MEASUREMENTS_WARNING_MESSAGE");
	}
	
	public String saveDecisionLogic() throws M9000Exception
	{
		String returnValue = "Done";
		updateDecisionLogic(lstLineGroups);
		SubStation substation = (SubStation)session.get("selectedSubstation");
		M9kDAOFactory m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		StationDAO mysqlStationDao = m9kDAOFactory.getStationDAO();
		
		int configSerialNumber = 1;
		DFR[] dfrs = substation.getDFRs().getDFRArray();
		logger.debug("IS configSerialNumber set already? "+dfrs[0].getSystem().isSetConfigSerialNumber());
		if (dfrs[0].getSystem().isSetConfigSerialNumber())
		{
			logger.debug("ConfigSeriaNumber already set "+substation.getDFRs().getDFRArray(0).getSystem().getConfigSerialNumber());
			configSerialNumber = substation.getDFRs().getDFRArray(0).getSystem().getConfigSerialNumber()+1;
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
		stationDetails.setConfigStatus("COMPLETE");
		String xmlText = substation.xmlText(xmlOptions);
		logger.debug("configXml "+stationDetails.getConfigXml()+M9kConstants.NEWLINE + M9kConstants.NEWLINE+" xmml Text "+xmlText);
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

			logger.info("User "+userDto.getUserName()+" modified the configuration of the station "+stationDetails.getStationDisplayName()+" "+M9kConstants.NEWLINE+strBufDiff.toString());
		}

		stationDetails.setConfigXml(xmlText);
		stationDetails.setTotalDfrsConfigured(substation.getDFRs().sizeOfDFRArray());
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
		accessMode="Edit";
		session.put("accessMode", accessMode);
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
	
	public String saveDecisionLogicAndSend() throws M9000Exception
	{
		String returnValue = "Send";
		saveDecisionLogic();
		logger.info("User "+userDto.getUserName()+" succesfully sent the saved configuration to station "+stationDetails.getStationDisplayName());
		return returnValue;
	}
	private void updateDecisionLogic(List<LineGroupsAlgorithm> lstLineGroups) {
		LineGroupsAlgorithm lineGroup;
		for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator
				.hasNext();) {
			lineGroup = iterator.next();
//			logger.debug("Line Group name "+lineGroup.getName()+" Chassis "+lineGroup.getChassis() +" is local? "+lineGroup.isLocal());
			if (lineGroup.isLocal() && lineGroup.getChassis() != null)
			{
//				logger.debug("Local line group "+lineGroup.getName());
				updateDecisionLogicForLocalLineGroups(lineGroup);
			}
			else
			{
//				logger.debug("Global line group "+lineGroup.getName());
				updateDecisionLogicForGlobalLineGroup(lineGroup);
			}
		}
		
	}

	private void updateDecisionLogicForLocalLineGroups(LineGroupsAlgorithm lineGroup) {
		DFR xmlDfr = mapXMLDfrs.get(lineGroup.getChassis());
		LineGroups xmlLineGroups = xmlDfr.getDataPool().getLineGroups();
		LineGroup[] arrLineGroup = xmlLineGroups.getLineGroupArray();
		if (arrLineGroup != null)
		{
			for (int i = 0; i < arrLineGroup.length; i++) {
				logger.debug("Comparing with "+arrLineGroup[i].getId()+" - "+arrLineGroup[i].getName());
				if (arrLineGroup[i].getName().equalsIgnoreCase(lineGroup.getName()))
				{
					// START: 16-Feb-2017 - Save the decision logic even if it is blank
					if (lineGroup.getDecisionLogic() != null && !lineGroup.getDecisionLogic().isEmpty())
					{
						arrLineGroup[i].setDecisionLogic(lineGroup.getDecisionLogic());
					}
					else
					{
						arrLineGroup[i].setDecisionLogic("NONE");
					}
					// END: 16-Feb-2017
					break;
				}
			}
		}

		
	}

	private void updateDecisionLogicForGlobalLineGroup(
			LineGroupsAlgorithm lineGroup) {
		SubStation currentSubstation = (SubStation) session.get("selectedSubstation");
		GlobalLineGroups globalLineGroups = currentSubstation.getGlobalLineGroups();
		logger.debug("Is line group available? id "+lineGroup.getId()+" - "+lineGroup.getName()+" - "+lineGroup.getLineGroupName());
		GlobalLineGroup[] arrGlobalLineGroup = globalLineGroups.getGlobalLineGroupArray();
		if (arrGlobalLineGroup != null)
		{
			for (int i = 0; i < arrGlobalLineGroup.length; i++) {
//				logger.debug("Comparing with "+arrGlobalLineGroup[i].getId()+" - "+arrGlobalLineGroup[i].getName());
				if (arrGlobalLineGroup[i].getId() == lineGroup.getId())
				{
					if (lineGroup.getDecisionLogic() != null && !lineGroup.getDecisionLogic().isEmpty())
					{
						arrGlobalLineGroup[i].setDecisionLogic(lineGroup.getDecisionLogic()); 
					}
					break;
				}
			}
		}
		
	}

	
	/**
	 * @return the selectedLineGroup
	 */
	public String getSelectedLineGroup() {
		return StringEscapeUtils.unescapeXml(selectedLineGroup);
	}

	/**
	 * @param selectedLineGroup the selectedLineGroup to set
	 */
	public void setSelectedLineGroup(String selectedLineGroup) {
		this.selectedLineGroup = StringEscapeUtils.escapeXml(selectedLineGroup);
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
	 * @return the lstChannelsInLineGroup
	 */
	public List<String> getLstChannelsInLineGroup() {
		return lstChannelsInLineGroup;
	}

	/**
	 * @param lstChannelsInLineGroup the lstChannelsInLineGroup to set
	 */
	public void setLstChannelsInLineGroup(List<String> lstChannelsInLineGroup) {
		this.lstChannelsInLineGroup = lstChannelsInLineGroup;
	}

	/**
	 * @param selectedLineGroupIndex the selectedLineGroupIndex to set
	 */
	public void setSelectedLineGroupIndex(int selectedLineGroupIndex) {
		this.selectedLineGroupIndex = selectedLineGroupIndex;
	}


	/**
	 * @param lstAvailableAnalogChannels the lstAvailableAnalogChannels to set
	 */
	public void setLstAvailableAnalogChannels(
			List<AnalogChannelDTO> lstAvailableAnalogChannels) {
		this.lstAvailableAnalogChannels = lstAvailableAnalogChannels;
	}

	/**
	 * @return the lineGroupDetails
	 */
	public List<String> getLineGroupDetails() {
		return lineGroupDetails;
	}

	/**
	 * @param lineGroupDetails the lineGroupDetails to set
	 */
	public void setLineGroupDetails(List<String> lineGroupDetails) {
		this.lineGroupDetails = lineGroupDetails;
	}

	/**
	 * @return the availableChannels
	 */
	public List<String> getAvailableChannels() {
		return availableChannels;
	}

	/**
	 * @param availableChannels the availableChannels to set
	 */
	public void setAvailableChannels(List<String> availableChannels) {
		this.availableChannels = availableChannels;
	}
	

//	/**
//	 * @return the previousSelectedLineGroupIndex
//	 */
//	public int getpreviousSelectedLineGroupIndex() {
//		return previousSelectedLineGroupIndex;
//	}
//
//	/**
//	 * @param previousSelectedLineGroupIndex the previousSelectedLineGroupIndex to set
//	 */
//	public void setpreviousSelectedLineGroupIndex(int previousSelectedLineGroupIndex) {
//		this.previousSelectedLineGroupIndex = previousSelectedLineGroupIndex;
//	}

	/**
	 * @return the selectedLineGroupIndex
	 */
	public int getSelectedLineGroupIndex() {
		return selectedLineGroupIndex;
	}
	
	/**
	 * @return the btnEdit
	 */
	public String getBtnEdit() {
		return btnEdit;
	}

	/**
	 * @param btnEdit the btnEdit to set
	 */
	public void setBtnEdit(String btnEdit) {
		this.btnEdit = btnEdit;
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

//	/**
//	 * @return the lineGroupName
//	 */
//	public String getLineGroupName() {
//		return lineGroupName;
//	}
//
//	/**
//	 * @param lineGroupName the lineGroupName to set
//	 */
//	public void setLineGroupName(String lineGroupName) {
//		this.lineGroupName = lineGroupName;
//	}

	public List<TriggerSourceDTO> getLstTriggerSourceDto() {
		return lstTriggerSourceDto;
	}

	public void setLstTriggerSourceDto(List<TriggerSourceDTO> lstTriggerSourceDto) {
		this.lstTriggerSourceDto = lstTriggerSourceDto;
	}

	public List<EventChannelDTO> getLstEventChannels() {
		return lstEventChannels;
	}

	public void setLstEventChannels(List<EventChannelDTO> lstEventChannels) {
		this.lstEventChannels = lstEventChannels;
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
	 * @return the triggerOutHold
	 */
//	public String getTriggerOutHold() {
//		return triggerOutHold;
//	}

	/**
	 * @param triggerOutHold the triggerOutHold to set
	 */
//	public void setTriggerOutHold(String triggerOutHold) {
//		this.triggerOutHold = triggerOutHold;
//	}

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

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
		
	public void updateLineGroupDetailsBackToXMLConfigBackUp()
	{
		boolean booNew = false;
		SubStation currentSubstation = (SubStation) session.get("selectedSubstation");
		GlobalLineGroups globalLineGroups = currentSubstation.getGlobalLineGroups();
		logger.debug("In UpdateLineGroupDetailsBackToXMLConfig method "+globalLineGroups+" lstLineGroup "+lstLineGroups);
		if (globalLineGroups == null)
		{
			globalLineGroups = currentSubstation.addNewGlobalLineGroups();
			globalLineGroups.setName("Global Line Groups");
			booNew = true;
		}
		else
		{
//			while (globalLineGroups.sizeOfGlobalLineGroupArray() > 0) {
//				globalLineGroups.removeGlobalLineGroup(0);
//			}
		}
		GlobalLineGroup globalLineGroup;
		LgAnalogs lgAnalogs;
		LineGroupsAlgorithm lineGroup;
		LineGroupChannelsDTO lineGroupChannelsDTO;
		AnalogChannels analogChannels;
		AnalogChannelDTO lgAnalogChannels ;
		List<AnalogChannelDTO> lstLineGroupAnalogs;
		for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator
				.hasNext();) {
			lineGroup = iterator.next();
			globalLineGroup = null;
			if (!booNew)
			{
				globalLineGroup = getGlobalLineGroupAlreadyAvailable(globalLineGroups, lineGroup);
			}
			if (globalLineGroup == null)
			{
				globalLineGroup = globalLineGroups.addNewGlobalLineGroup();					
				lgAnalogs = globalLineGroup.addNewLgAnalogs();
				lgAnalogs.setName("Linegroup Analog Input");
			}
			else
			{
				lgAnalogs = globalLineGroup.getLgAnalogs();
				while (lgAnalogs.sizeOfAnalogChannelsArray() > 0)
				{
					lgAnalogs.removeAnalogChannels(0);
				}
			}
			globalLineGroup.setId(lineGroup.getId());
			globalLineGroup.setName(lineGroup.getName());
			if (lineGroup.getLineGroupName()!= null && !lineGroup.getLineGroupName().isEmpty())
			{
				globalLineGroup.setLineGroupName(lineGroup.getLineGroupName());
			}
			else
			{
				globalLineGroup.setLineGroupName(lineGroup.getName());
			}
			globalLineGroup.setAutoCalc(lineGroup.getEnableAutoCalc());
			globalLineGroup.setPositiveResistance(lineGroup.getPositiveResistance());
			globalLineGroup.setPositiveReactance(lineGroup.getPositiveReactance());
			globalLineGroup.setZeroResistance(lineGroup.getZeroResistance());
			globalLineGroup.setZeroReactance(lineGroup.getZeroReactance());
			globalLineGroup.setLineMiles(lineGroup.getLineMiles());
			if (lineGroup.getDecisionLogic() != null && !lineGroup.getDecisionLogic().isEmpty())
			{
				globalLineGroup.setDecisionLogic(lineGroup.getDecisionLogic());
			}
			// START: 16-Oct-2019 Implemented a comments section for line group where customer can add custom name value pair (Georgia Power)
			if (lineGroup.getComments() != null && !lineGroup.getComments().isEmpty())
			{
				globalLineGroup.setComments(lineGroup.getComments());
			}
			// END: 16-Oct-2016
			lstLineGroupAnalogs = lineGroup.getInputChannels();
			logger.debug("lineGroup.getInputChannels() size "+lstLineGroupAnalogs.size());
			for (Iterator<AnalogChannelDTO> iteratorAnalogs = lstLineGroupAnalogs.iterator(); iteratorAnalogs
					.hasNext();) {
				lgAnalogChannels = iteratorAnalogs.next();
				lineGroupChannelsDTO = createRmsAlgorithmAndExport(lineGroup, lgAnalogChannels);
				analogChannels = lgAnalogs.addNewAnalogChannels();
				analogChannels.setName("lg_"+lgAnalogChannels.getName());
				analogChannels.setChannelId(lineGroupChannelsDTO.getChannelId());
				analogChannels.setRecId(lineGroupChannelsDTO.getDfrId());
				analogChannels.setExpId(lineGroupChannelsDTO.getExportId());
			}

		}

//		LineGroups xmlLineGroups = selectedDfr.getDataPool().getLineGroups();
//		logger.debug("In UpdateLineGroupDetailsBackToXMLConfig method "+xmlLineGroups+" lstLineGroup "+lstLineGroups);
//		if (xmlLineGroups == null)
//		{
//			xmlLineGroups = selectedDfr.getDataPool().addNewLineGroups();
//			xmlLineGroups.setName("Line Groups");
//		}
//		else
//		{
//			clearLinegroups(xmlLineGroups);
//		}
//		LineGroup xmlLineGroup;
//		LineGroupsAlgorithm lineGroups;
//		for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator.hasNext();) {
//			lineGroups = iterator.next();
//			xmlLineGroup = xmlLineGroups.addNewLineGroup();
//			xmlLineGroup.setName(lineGroups.getName());
//			for (Iterator<String> iteratorInputTags = lineGroups.getLstInputTagValues().iterator(); iteratorInputTags.hasNext();) {
//				String inputTagValue = iteratorInputTags.next();
//				xmlLineGroup.addInput(inputTagValue);
//			}
//			
//		}		
	}

	
	@SuppressWarnings("unchecked")
	public String faultLocationDecisionLogic()
	{
		String returnValue = "FaultLocation";
		lstAllTriggerChannels = new ArrayList<TriggerChannelDTO>();
		if (lstTriggerChannels == null)
		{
			lstTriggerChannels = (List<TriggerChannelDTO>) session.get("lstTriggerChannels");
		}
		if (lstTriggerChannels != null)
		{
			TriggerChannelDTO triggerChannelDTO;
			for (Iterator<TriggerChannelDTO> iterator = lstTriggerChannels.iterator(); iterator.hasNext();) {
				triggerChannelDTO = iterator.next();
				logger.debug("ADD MULTIPLE: triggerChannelDTO "+triggerChannelDTO);
				// START: 26-Mar-2020 - HOT FIX for IE editing configuration issue that crashes during edit
//				if (triggerChannelDTO.getId() == null || triggerChannelDTO.getType().equalsIgnoreCase("Delete"))
				if (triggerChannelDTO == null || triggerChannelDTO.getId() == null || triggerChannelDTO.getType() == null || triggerChannelDTO.getType().equalsIgnoreCase("Delete"))
				// END: 26-Mar-2020
				{
					continue;
				}
				lstAllTriggerChannels.add(triggerChannelDTO);			}
		}
		if(lstEventTriggerChannels != null)
		{
			lstAllTriggerChannels.addAll(lstEventTriggerChannels);
		}
//		session.put("lstAllTriggerChannels", lstAllTriggerChannels);
//		logger.debug("selectedLineGroupIndex in the faultLocationDecisionLogic "+selectedLineGroupIndex);
		selectedLineGroupIndex = calculateSelectedLineGroupIndex();
		session.put("currentLineGroupIndex", selectedLineGroupIndex);
		return returnValue;
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

	public Boolean isDecisionLogic() {
		return decisionLogic;
	}

	public void setDecisionLogic(Boolean decisionLogic) {
		this.decisionLogic = decisionLogic;
	}

	public List<TriggerChannelDTO> getLstAllTriggerChannels() {
		return lstAllTriggerChannels;
	}

	public void setLstAllTriggerChannels(
			List<TriggerChannelDTO> lstAllTriggerChannels) {
		this.lstAllTriggerChannels = lstAllTriggerChannels;
	}

	public List<TriggerChannelDTO> getLstEventTriggerChannels() {
		return lstEventTriggerChannels;
	}

	public void setLstEventTriggerChannels(
			List<TriggerChannelDTO> lstEventTriggerChannels) {
		this.lstEventTriggerChannels = lstEventTriggerChannels;
	}

	public List<DfrDTO> getLstDfrDTO() {
		return lstDfrDTO;
	}

	public void setLstDfrDTO(List<DfrDTO> lstDfrDTO) {
		this.lstDfrDTO = lstDfrDTO;
	}

	public LineGroupsAlgorithm getEditedLineGroup() {
		return editedLineGroup;
	}

	public void setEditedLineGroup(LineGroupsAlgorithm editedLineGroup) {
		this.editedLineGroup = editedLineGroup;
	}

	public int getCurrentLineGroupIndex() {
		return currentLineGroupIndex;
	}

	public void setCurrentLineGroupIndex(int currentLineGroupIndex) {
		this.currentLineGroupIndex = currentLineGroupIndex;
	}

	public Boolean getLineGroupExists() {
		return lineGroupExists;
	}

	public void setLineGroupExists(Boolean lineGroupExists) {
		this.lineGroupExists = lineGroupExists;
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

	/**
	 * @return the lstExistingAnalogChannels
	 */
	public List<AnalogChannelDTO> getLstExistingAnalogChannels() {
		return lstExistingAnalogChannels;
	}

	/**
	 * @param lstExistingAnalogChannels the lstExistingAnalogChannels to set
	 */
	public void setLstExistingAnalogChannels(List<AnalogChannelDTO> lstExistingAnalogChannels) {
		this.lstExistingAnalogChannels = lstExistingAnalogChannels;
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

}
