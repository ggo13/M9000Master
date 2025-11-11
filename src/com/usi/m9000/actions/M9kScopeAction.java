package com.usi.m9000.actions;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.jfree.chart.JFreeChart;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.EventInputDocument.EventInput;
import com.usi.SubStationDocument.SubStation;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.chart.applet.M9kChart;
import com.usi.m9000.config.AnalogInfo;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.CalibrationDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.UsersDTO;
import com.usi.m9000.station.util.M9kKeyValuePair;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kMessagesUtil;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.MessageProperties;

public class M9kScopeAction extends ActionSupport implements SessionAware, ServletContextListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Map<String, Object> session;
	String btnSubmit;
	List<DfrDTO> lstDfrDTO;
	List<AnalogChannelDTO> lstAnalogChannels; 
	EventInput[] digitals;
	private SubStation currentSubstation;
	int minimumDFRCount;
	private String selectedDfr;
	private List<StationDTO> lstAvailableStations;
	transient private StationDTO stationDetails;
	private String stationId;
	private UsersDTO userDto;
	private JFreeChart chart;
	private M9kChart m9kChart;
	private Object scopeConfig;
	private List<DfrDTO> lstScopeDfrs;
	private DfrDTO selectedDfrDto = null;
	private String[] selectedChannels;
	private String lastCalVerifiedDate = "No Info";
	private String lastCalibratedDate = "No Info";
	private String lastEventTestDate = "No Info";

	private List<Map<Integer, Double>> lstOfMapDataSetForJQ;
	private String[] scopeRmsValues;
	private String primaryOrSecondaryDisplay = M9kConstants.PRIMARY;
	private Map<String, M9kKeyValuePair> mapDigitalsDisplayInfo; // Key - channel Id and value is M9kKeyValuePair(Tooltip, Color of the status))
	private String strSelectedChannels; // Selected channels as a comma separated values
	private Boolean chartChanged = true;
	private Boolean showHideDataPoints = false;
	private String requestedCycles = "5";
	private Boolean eventTestTransient = false;
	private String eventTestResult;
	private Integer eventTestStatus; // Idea is to update progress bar using this variable while event test
	private Integer calibrationStatus; // Idea is to update progress bar using this variable while calibration and verification
	private String internalCalibrationResult;
	private String applyCalFactorsResult;
	private String verifyCalibrationResult;
	private String externalCalibrationResult;
	private Boolean enableTransient = false;
	private Boolean offsetCorrection = false;
	private Boolean extCalReset = false;
	private String desiredValue = null;
	private String errorMessage;
	private boolean updateScopeConfig = false; // Request and Update the scope configuration after external calibration
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kScopeAction.class);
	/**
	 * 
	 */
	public M9kScopeAction()
	{
		logger.debug("Inside M9kScopeAction constructor with station Id "+getStationId());
	}

	  public String execute()throws Exception{
		  logger.debug("Inside execute...");
		  String returnStatus = SUCCESS;
		  return returnStatus;
	  }
	  @SkipValidation
	  @SuppressWarnings("unchecked")
	public String viewScope()
	  {
		  String returnValue = SUCCESS;
		  String accessMode = "scope";
		  logger.debug("Sttaion ID: "+getStationId());
		  session.put("accessMode", accessMode);
			stationDetails = (StationDTO)session.get("stationDetails");
			logger.debug("Station Details from session "+stationDetails);
			if (getLstAvailableStations() == null)
			{
				lstAvailableStations = (List<StationDTO>) session.get("lstAvailableStations");
			}
			
		session.put("StationId", ""+stationDetails.getSystemStationId());
		logger.debug("About to return to scope with result "+returnValue);
		return returnValue;
	  }
	  
	  @SuppressWarnings("unchecked")
	@SkipValidation
	  public String backFromScope()
	  {
		  logger.debug("Entered backFromScope method...");
		  String returnStatus = "Back";
		  session.remove("accessMode");
		  session.remove("stationDetails");
		  lstDfrDTO = (List<DfrDTO>) session.get("DFRsList"); 
		  return returnStatus;
	  }

	  public String displaySCOPETab() throws Exception
		{
		  String result = SUCCESS;
			logger.debug("Inside display SCOPE Tab ");
			result = initJQScopeChart();
			logger.debug("strSelectedChannels set to "+strSelectedChannels+ " result of init "+result);
			return result;
			
		}
	  
	  public String loadSCOPETab() throws Exception
		{
			logger.debug("Inside Load SCOPE Tab ");
			return SUCCESS;
			
		}
	  public String displaySCOPETabJF() throws Exception
		{
			logger.debug("Inside display SCOPE Tab ");
			if (m9kChart == null)
			  {
				  initScopeChart();
			  }
//			  else
//			  {
//				  updateChart();
//			  }
			return SUCCESS;
			
		}
	  
	  
	  public String updateScopeChart()
	  {
		  try {
			  logger.debug("selectedDfrDto "+getSelectedDfrDto());
			  if (getSelectedDfrDto() == null)
			  {
				  selectedDfrDto = (DfrDTO) session.get("selectedScopeDfr");
			  }
//			  updateChart();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error in updating chart ",e);
		}
		  return SUCCESS;
	  }
	  public String getUpdatedScopeChart() throws Exception 
	  {
		  logger.debug("Inside getUpdatedScopeChart " + m9kChart);
			m9kChart = (M9kChart) session.get("m9kScopeChart");
			logger.debug("m9kchart from session" + m9kChart);
			
			if (m9kChart == null)
			  {
				logger.debug("Initializing the chart...");
				  initScopeChart();
			  }
			  else
			  {
				  logger.debug("Updating the chart..."+getSelectedDfrDto());
				  if (getSelectedDfrDto() == null)
				  {
					  selectedDfrDto = (DfrDTO) session.get("selectedScopeDfr");
				  }
				  updateChart();
			  }
		  return SUCCESS;
	  }
	  
	  public String initScopeChart() 
	  {
//		  System.out.println("Inside Execute: Start");
//
//
//	        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
//	        dataSet.setValue(0, "01-04-2014", "Channel1");
//	        dataSet.setValue(15000, "01-04-2014", "Channel2");
//
//	        dataSet.setValue(9000, "01-05-2014", "Channel1");
//	        dataSet.setValue(1500, "01-05-2014", "Channel2");
//
//	        dataSet.setValue(10000, "01-06-2014", "Channel1");
//	        dataSet.setValue(8000, "01-06-2014", "Channel2");
//
//	        chart = ChartFactory.createBarChart(
//	                "Demo Bar Chart", //Chart title
//	                "Mobile Manufacturer", //Domain axis label
//	                "TRANSACTIONS", //Range axis label
//	                dataSet, //Chart Data
//	                PlotOrientation.VERTICAL, // orientation
//	                true, // include legend?
//	                true, // include tooltips?
//	                false // include URLs?
//	        );
//
//	        chart.setBorderVisible(true);
//	        System.out.println("Inside Execute: End");
//		  stationDetails = (StationDTO)session.get("stationDetails");
//		  M9kUtils.setStationDetails(stationDetails);
		  logger.debug("Station Details is null ? "+(getStationDetails() == null));
		  
		  m9kChart = new M9kChart(getStationDetails());
		  try {
			updateScopeConfig();
			m9kChart.setSelectedDfr(getSelectedDfrDto());
			m9kChart.synthesizeDataForFilteredRMS();
			
			m9kChart.setPrimary(true);
			session.put("m9kScopeChart", m9kChart);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  return SUCCESS;
	  }

	@SuppressWarnings("unchecked")
	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
		lstScopeDfrs = (List<DfrDTO>)session.get("scopeDfrsList");
		stationDetails = (StationDTO) session.get("stationDetails");
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
	 * @return the lstAvailableStations
	 */
	public List<StationDTO> getLstAvailableStations() {
		return lstAvailableStations;
	}

	/**
	 * @param lstAvailableStations the lstAvailableStations to set
	 */
	public void setLstAvailableStations(List<StationDTO> lstAvailableStations) {
		this.lstAvailableStations = lstAvailableStations;
	}


	/**
	 * @return the stationDetails
	 * 
	 */
	// To fix external calibration exception, renamed the
	public StationDTO getStationDetailsOld() {
		if (stationDetails == null)
		{
			if (getStationId() != null)
			{
				M9kUtils.setStationId(Integer.parseInt(getStationId()));
				stationDetails = M9kUtils.getStationDetails();
			}
			else
			{
				stationDetails = (StationDTO) session.get("stationDetails");
			}
		}
		return stationDetails;
	}

	public StationDTO getStationDetails() {
		return stationDetails;
	}
	/**
	 * @param stationDetails the stationDetails to set
	 */
	public void setStationDetails(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
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
	 * @return the stationId
	 */
	public String getStationId() {
		return stationId;
	}

	/**
	 * @param stationId the stationId to set
	 */
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

//	public StationDTO getStationDetails(int stationId)
//	{
//		StationDTO currentStationDTO = null;
//		for (Iterator<StationDTO> iterator = getLstAvailableStations().iterator(); iterator.hasNext();) {
//			currentStationDTO = (StationDTO) iterator.next();
//			if (currentStationDTO.getId() == stationId)
//			{
//				break;
//			}
//		}
//		return currentStationDTO;
//	}

	public UsersDTO getUserDto() {
		return userDto;
	}

	public void setUserDto(UsersDTO userDto) {
		this.userDto = userDto;
	}

	public JFreeChart getChart() {
		return chart;
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void updateScopeConfig() throws Exception{
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.addProperty("MESSAGE_TYPE", "SCOPECONFIG");
		messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
	scopeConfig = M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "SCOPECONFIG", messageProperties);
	logger.debug("SCOPE-CONFIG: request scope config complete...");
//	System.out.println("List of dfrDto "+lstScopeDfrs);
	if (scopeConfig != null && scopeConfig instanceof List<?>)
	{
		lstScopeDfrs = (List<DfrDTO>) scopeConfig;
//		System.out.println("BEFORE SORT: List of dfrDto "+lstScopeDfrs);
		Collections.sort(lstScopeDfrs, new Comparator<DfrDTO>() {

			@Override
			public int compare(DfrDTO o1, DfrDTO o2) {
//				return ((o1.getDfrId() < o2.getDfrId())?0:1);
				return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
			}
		});
		logger.debug("List of scope dfrs "+lstScopeDfrs);
//		for (Iterator<DfrDTO> iterator = lstScopeDfrs.iterator(); iterator.hasNext();) {
//			logger.debug("Dfr Ids from list "+iterator.next().getDfrId());
//			
//		}
		session.put("scopeDfrsList", lstScopeDfrs);
		if (selectedDfrDto == null || selectedDfrDto.getDfrId() == 0)
		{
			selectedDfrDto = lstScopeDfrs.get(0);
			logger.debug("selected dfrDto "+selectedDfrDto.getDfrId());
	//		System.out.println("AFTER SORT: List of dfrDto "+lstScopeDfrs);
//			LAST_CAL_VERIFIED_DATE = getLastCalVerifiedDate();
//			LAST_CALIBRATED_DATE = getLastCalibratedDate();
//			LAST_EVENTTEST_DATE = getLastEventTestDate();
		}
		else
		{
			DfrDTO dfrDTO;
			for (Iterator<DfrDTO> iterator = lstScopeDfrs.iterator(); iterator
					.hasNext();) {
				 dfrDTO = iterator.next();
				 logger.debug("dfrDto from list "+dfrDTO.getDfrId()+" selected DfrDto "+selectedDfrDto.getDfrId());
				 if (dfrDTO.getDfrId() == selectedDfrDto.getDfrId())
				 {
					 selectedDfrDto = dfrDTO;
					 break;
				 }
				
			} 
		}
		selectedDfr = ""+selectedDfrDto.getDfrId();
		session.put("alreadySelectedDfr", selectedDfr);
		session.put("selectedScopeDfr", selectedDfrDto);
	}
	else
	{
		throw new M9000Exception("Unable to get the SCOPE configuration. Please Check whether atleast one of the DFRs is connected to the station master");
	}
	setUpdateScopeConfig(false);
	logger.debug("Returning from updateScopeConfig..."+selectedDfr);
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public synchronized void updateDFRSpecificScopeConfig() throws Exception{
		DfrDTO requestedDfr = null;
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.addProperty("MESSAGE_TYPE", "DFR_SPECIFIC_SCOPECONFIG");
		messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
		int destDfrId = 1;
		if (selectedDfrDto == null || selectedDfrDto.getDfrId() == 0)
		{
			destDfrId = 1;
		}
		else
		{
			destDfrId = selectedDfrDto.getDfrId();
		}
		messageProperties.addProperty("DFR_ID", destDfrId);
		try
		{
			requestedDfr = (DfrDTO)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "DFR_SPECIFIC_SCOPECONFIG", messageProperties);
		}
		catch (Exception e)
		{
			logger.error("Unable to get dfr specific config. Reason: ",e);
			requestedDfr = null;
		}
	logger.debug("SCOPE-CONFIG: request scope config complete...");
//	System.out.println("List of dfrDto "+lstScopeDfrs);
	if (requestedDfr != null)
	{
		DfrDTO dfrDtoLoop;
		for (Iterator<DfrDTO> iterator = lstScopeDfrs.iterator(); iterator.hasNext();) {
			dfrDtoLoop = iterator.next();
			logger.debug("Dfr Ids from list "+dfrDtoLoop);
			if (dfrDtoLoop.getDfrId() == selectedDfrDto.getDfrId())
			{
				logger.debug("index to be replaced "+lstScopeDfrs.indexOf(dfrDtoLoop));
				lstScopeDfrs.set(lstScopeDfrs.indexOf(dfrDtoLoop), requestedDfr);
				break;
			}
			
		}
		session.put("scopeDfrsList", lstScopeDfrs);
		if (selectedDfrDto == null || selectedDfrDto.getDfrId() == 0)
		{
			selectedDfrDto = lstScopeDfrs.get(0);
			logger.debug("selected dfrDto "+selectedDfrDto.getDfrId());
	//		System.out.println("AFTER SORT: List of dfrDto "+lstScopeDfrs);
//			LAST_CAL_VERIFIED_DATE = getLastCalVerifiedDate();
//			LAST_CALIBRATED_DATE = getLastCalibratedDate();
//			LAST_EVENTTEST_DATE = getLastEventTestDate();
		}
		else
		{
			DfrDTO dfrDTO;
			for (Iterator<DfrDTO> iterator = lstScopeDfrs.iterator(); iterator
					.hasNext();) {
				 dfrDTO = iterator.next();
				 logger.debug("dfrDto from list "+dfrDTO.getDfrId()+" selected DfrDto "+selectedDfrDto.getDfrId());
				 if (dfrDTO.getDfrId() == selectedDfrDto.getDfrId())
				 {
					 selectedDfrDto = dfrDTO;
					 break;
				 }
				
			} 
		}
		selectedDfr = ""+selectedDfrDto.getDfrId();
		session.put("alreadySelectedDfr", selectedDfr);
		session.put("selectedScopeDfr", selectedDfrDto);
	}
	else
	{
		throw new M9000Exception("Unable to get the SCOPE configuration. Please Check whether atleast one of the DFRs is connected to the station master");
	}
	setUpdateScopeConfig(false);
	logger.debug("Returning from updateScopeConfig..."+selectedDfr);
	}

//	public String getLastCalVerifiedDate() {
//		String lastVerifiedDate = "No Info";
//		try {
//			MessageProperties messageProperties = new MessageProperties();
//			messageProperties.addProperty("MESSAGE_TYPE", "FETCH_LAST_CAL_VERIFY_DATE");
//			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
//
//			lastVerifiedDate = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "FETCH_LAST_CAL_VERIFY_DATE", messageProperties);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return lastVerifiedDate;
//	}
//	
//	public String getLastCalibratedDate() {
//		String lastCalibratedDate = "No Info";
//		try {
//			MessageProperties messageProperties = new MessageProperties();
//			messageProperties.addProperty("MESSAGE_TYPE", "FETCH_LAST_CAL_DATE");
//			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
//
//			lastCalibratedDate = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "FETCH_LAST_CAL_DATE", messageProperties);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return lastCalibratedDate;
//	}
//	
//	public String getLastEventTestDate() {
//		String lastEventTestDate = "No Info";
//		try {
//			MessageProperties messageProperties = new MessageProperties();
//			messageProperties.addProperty("MESSAGE_TYPE", "FETCH_LAST_EVENTTEST_DATE");
//			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
//			lastEventTestDate = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "FETCH_LAST_EVENTTEST_DATE", messageProperties);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return lastEventTestDate;
//	}

	public DfrDTO getSelectedDfrDto() {
		return selectedDfrDto;
	}

	public void setSelectedDfrDto(DfrDTO selectedDfrDto) {
		this.selectedDfrDto = selectedDfrDto;
	}

	private int getSamplesPerCycle()
	{
		logger.debug("Samples per cycle "+(int)(m9kChart.getSampleRate()/m9kChart.getLineFreq()));
		logger.debug("Samples cnt "+m9kChart.getSamplesCnt());
		return (int)(m9kChart.getSampleRate()/m9kChart.getLineFreq());
	}
	private String constructScopeCommand(int cycles)
	{
		// Modified to send the selected chassis IP Address from master instead of deriving at station master
		logger.debug("selected Dfr ip adress "+selectedDfrDto.getIpAddress());
		String scopeCmd = selectedDfrDto.getIpAddress()+",scope, samples="+((cycles+1) * getSamplesPerCycle())+",ch=";
		if (getSelectedChannels() == null)
		{
			scopeCmd+=1;
		}
		else
		{
			int startIndex=1;
			for (int i = 0; i < getSelectedChannels().length; i++) {
//				logger.debug("index "+getSelectedChannels()[i]);
				if (getSelectedChannels()[i].startsWith(M9kConstants.VIRTUAL_PREFIX))
				{
//					startIndex = 3;
					startIndex = M9kConstants.VIRTUAL_PREFIX.length();
				}
				
				if (i == 0)
				{
					scopeCmd+=getSelectedChannels()[i].substring(startIndex, selectedChannels[i].indexOf("-"));
				}
				else
				{
					scopeCmd+=",ch="+(getSelectedChannels()[i].substring(startIndex, selectedChannels[i].indexOf("-")));
				}
			}
		}
		logger.debug("Constructed cmd "+scopeCmd);
		return scopeCmd;
	}

	public String[] getSelectedChannels() {
		return selectedChannels;
	}

	public void setSelectedChannels(String[] selectedChannels) {
		this.selectedChannels = selectedChannels;
	}
	
	private void updateChart() throws Exception
	{
		logger.debug("Inside updateChart..."+selectedDfrDto);
		if (selectedDfrDto.getAnalogChnlCnt() > 0)
		{
				m9kChart.getChannelDataList().removeBinaryChannels();
				chart = m9kChart.constructChart();
		}
		else
		{
			m9kChart.parseEventsData();
//			getEventsPanel().updateUI();
//			btnPanel.setVisible(false);
//			enableComponents(btnPanel,false);
		}
	}

	
	 public String initJQScopeChart() throws Exception 
	  {
		 String returnStatus = SUCCESS;
//		  stationDetails = (StationDTO)session.get("stationDetails");
//		  M9kUtils.setStationDetails(stationDetails);
		  logger.debug("initJQScopeChart Station Details is null ? "+(getStationDetails() == null));
		  
//		  m9kChart = new M9kChart(getStationDetails());
		  try {
			updateScopeConfig();
			  m9kChart = new M9kChart(getStationDetails());
			m9kChart.setSelectedDfr(getSelectedDfrDto());
			m9kChart.synthesizeDataForFilteredRMS();
			
			m9kChart.setPrimary(true);
			if (selectedDfrDto.getAnalogChnlCnt() > 0)
			{
				m9kChart.setChannelCnt(1);
				selectedChannels = new String[1];
				selectedChannels[0] = selectedDfrDto.getLstAnalogChannelNames().get(0); 
				strSelectedChannels = getSelectedChannels()[0].substring(1, selectedChannels[0].indexOf("-"));
				m9kChart.setChannelsToDisplay(getChannelsForChart(new String[]{getSelectedChannels()[0].substring(1, selectedChannels[0].indexOf("-"))}));
			}
			m9kChart.setLineFreq(selectedDfrDto.getLineFreq());
			m9kChart.setSampleRate(selectedDfrDto.getSampleRate());
			m9kChart.getChannelDataList().removeBinaryChannels();
			m9kChart.setSamplesCnt(getRequestedCycles() * getSamplesPerCycle());
			m9kChart.setScopeCommand(constructScopeCommand(getRequestedCycles()));
			
//			chart.setBackgroundPaint(java.awt.Color.white);
			session.put("m9kScopeChart", m9kChart);
			
		} catch (Exception e) {
			logger.error("Error in initializing SCOPE",e);
			returnStatus = ERROR;
			errorMessage = "Error in initializing SCOPE. Reason: "+e.getMessage();
		}
		  return returnStatus;
	  }

	 public String getUpdatedJQScopeChart() throws Exception 
	  {
		 String result = SUCCESS;
		 String sessionDfrId = ""+((DfrDTO) session.get("selectedScopeDfr")).getDfrId();
		
		 if (session.get("alreadySelectedDfr") != null)
		 {
			 String alreadySelectedDfr = (String) session.get("alreadySelectedDfr");
				 logger.debug("Already Selected Dfr  "+alreadySelectedDfr+" from the request "+selectedDfrDto.getDfrId());
				 if (!alreadySelectedDfr.equalsIgnoreCase(sessionDfrId))
				 {
					 logger.debug("DIFFERENT DFR selected. Setting chart change flag to true. Already Selected Dfr  "+alreadySelectedDfr+" from the request "+selectedDfrDto.getDfrId());
					 selectedDfr = ""+selectedDfrDto.getDfrId();
						session.put("alreadySelectedDfr", selectedDfr);
					 setChartChanged(true);
				 }
		 }
		 logger.debug("getUpdatedJQScopeChart: Selected dfr id "+getSelectedDfr()+" dfrDto "+getSelectedDfrDto());
		  logger.debug("Inside getUpdatedJQScopeChart " + m9kChart+" selected channels list "+getStrSelectedChannels()+" is chart changed? "+isChartChanged());
			selectedDfrDto = (DfrDTO) session.get("selectedScopeDfr");
			logger.debug("Selected scope dfr from session "+selectedDfr);
			m9kChart = (M9kChart) session.get("m9kScopeChart");
			// START: 07-July-2020 - Hall Effect implementation - queries config every time
		  logger.debug("SCOPE-CONFIG: Updating the scope config for Hall Effect implementation");
		  updateDFRSpecificScopeConfig();
			// END: 09-Jul-2020
			if (isChartChanged())
			{
			  logger.debug("List of dfrs "+getLstDfrDTO()+" is update config set to true? "+isUpdateScopeConfig());
			// START: 07-July-2020 - Hall Effect implementation - queries config every time
//			  if (isUpdateScopeConfig())
//			  {
//				  logger.debug("SCOPE-CONFIG: Updating the scope config. Mostly for external calibration only");
//				  updateScopeConfig();
//			  }
			// END: 09-Jul-2020
			  m9kChart.setSelectedDfr(getSelectedDfrDto());
	//			updateSelectedDfr();
				updatePrimaryOrSecondary();
				if (selectedDfrDto.getAnalogChnlCnt() > 0)
				{
					  if (strSelectedChannels != null && !strSelectedChannels.isEmpty()) 
					  {
						  logger.debug("strSelectedChannels not Empty "+strSelectedChannels);
						  populateSelectedChannels();
					  }
					  else
					  {
							selectedChannels = new String[1];
							selectedChannels[0] = selectedDfrDto.getLstAnalogChannelNames().get(0);
							strSelectedChannels = getSelectedChannels()[0].substring(1, selectedChannels[0].indexOf("-"));
							logger.debug("strSelectedChannels Empty "+strSelectedChannels);
					  }
					m9kChart.setChannelCnt(selectedChannels.length);
					logger.debug("selected channel "+getSelectedChannels()[0]);
					logger.debug("Channel id to display "+getSelectedChannels()[0].substring(1, selectedChannels[0].indexOf("-")));
					if (strSelectedChannels != null && !strSelectedChannels.isEmpty())
					{
						 logger.debug("setChannelsToDisplay: strSelectedChannels not Empty "+strSelectedChannels);
						m9kChart.setChannelsToDisplay(getChannelsForChart(strSelectedChannels.split(",")));
					}
					else
					{
						m9kChart.setChannelsToDisplay(getChannelsForChart(new String[]{getSelectedChannels()[0].substring(1, selectedChannels[0].indexOf("-"))}));
						 logger.debug("setChannelsToDisplay: strSelectedChannels not Empty "+m9kChart.getChannelsToDisplay());
					}
					m9kChart.setLineFreq(selectedDfrDto.getLineFreq());
					m9kChart.setSampleRate(selectedDfrDto.getSampleRate());
		//			m9kChart.getChannelDataList().removeBinaryChannels();
					m9kChart.setSamplesCnt(getRequestedCycles() * getSamplesPerCycle());
					m9kChart.setScopeCommand(constructScopeCommand(getRequestedCycles()));
				}
	//			lstOfMapDataSetForJQ = m9kChart.getDataForJQ();
	//			scopeRmsValues=m9kChart.getScopeRmsValues();
	//			logger.debug("init jq chart scopeRmsValues "+scopeRmsValues[0]);
				
	//				chart.setBackgroundPaint(java.awt.Color.white);
				m9kChart.setLineFreq(selectedDfrDto.getLineFreq());
				logger.debug("chart constructed. Returning from action");
				session.put("m9kScopeChart", m9kChart);
	
					  logger.debug("Updating the JQ chart..."+getSelectedDfrDto());
					  setChartChanged(false);
			}
			try
			{
				updateJQChart();
			}
			catch(Exception e)
			{
				logger.error("Unable to get the SCOPE data. May be station master or DFR is down.",e);
				errorMessage = "Unable to get the SCOPE data. May be station master or DFR is down. Reason: "+e.getMessage();
				result = ERROR;
			}
			logger.debug("Returning from getUpdatedJQScopeChart() with return value "+result);
		  return result;
	  }
	 
	 public String getUpdatedJQDigitalOnlyScopeChart() throws Exception 
	 {
		 String result = SUCCESS;
		 try
			{
			 selectedDfrDto = (DfrDTO) session.get("selectedScopeDfr");
				logger.debug("Selected scope dfr from session "+selectedDfr);
				m9kChart = (M9kChart) session.get("m9kScopeChart");
				 m9kChart.setSelectedDfr(getSelectedDfrDto());
				 m9kChart.setLineFreq(selectedDfrDto.getLineFreq());
				logger.debug("chart constructed. Returning from action");
				session.put("m9kScopeChart", m9kChart);
				updateJQChart();
			}
			catch(Exception e)
			{
				logger.debug("Unable to get the SCOPE data for Digital Only Chassis. May be station master or DFR is down.",e);
				errorMessage = "Unable to get the SCOPE data for Digital Only Chassis. May be station master or DFR is down. Reason: "+e.getMessage();
				result = ERROR;
			}
			logger.debug("Returning from getUpdatedJQScopeChart() with return value "+result);
		  return result; 
	 }
	 private void populateSelectedChannels() {
		 String[] selectedChannelIds = getStrSelectedChannels().split(",");
		 logger.debug("Selected channels "+getStrSelectedChannels());
		 selectedChannels = new String[selectedChannelIds.length];
		 for (int i = 0; i < selectedChannelIds.length; i++) {
			 selectedChannels[i] = selectedDfrDto.getMapAnalogChannelNames().get(Integer.valueOf(selectedChannelIds[i]));			
		}
		
	}

	private void updateJQChart() throws Exception
		{
			logger.debug("Inside updateJQChart...."+selectedDfrDto);
			if (selectedDfrDto.getAnalogChnlCnt() > 0)
			{
				m9kChart.getChannelDataList().removeBinaryChannels();
				lstOfMapDataSetForJQ = m9kChart.getDataForJQ();
//				logger.debug("list of map "+lstOfMapDataSetForJQ);
				updateDigitalChannels();
				scopeRmsValues=m9kChart.getScopeRmsValues();
				logger.debug("jq chart scopeRmsValues "+scopeRmsValues);
			}
			else
			{
				m9kChart.parseEventsData();
				updateDigitalChannels();
//				getEventsPanel().updateUI();
//				btnPanel.setVisible(false);
//				enableComponents(btnPanel,false);
			}
		}
	 
		private String[] getChannelsForChart(String[] selectedChannels)
		{
			String[] strDefaultChannel = new String[selectedChannels.length];
			int chnl;
			if (getSelectedDfrDto() != null)
			{
				for (int i = 0; i < selectedChannels.length; i++) {
//					chnl = Integer.parseInt(selectedChannels[i]) - selectedDfrDto.getAnalogChannelStart() + 1;
					chnl = Integer.parseInt(selectedChannels[i]);
					strDefaultChannel[i] = ""+chnl;		
					logger.debug("Calculated channel no.... "+chnl);
				}
				
			}
			else
			{
				for (int i = 0; i < selectedChannels.length; i++) {
					strDefaultChannel[i] = selectedChannels[i].substring(1, selectedChannels[i].indexOf("-"));			
				}
			}
			return strDefaultChannel;
		}	 
		
		public String updateSelectedDfr()
		{

			logger.debug("About to update selected Dfr "+selectedDfrDto);
			 if (selectedDfrDto == null || selectedDfrDto.getDfrId() <= 1)
			 {
				 selectedDfrDto = lstScopeDfrs.get(0);
				 m9kChart.setSelectedDfr(selectedDfrDto);
			 }
			 else
			 {
				DfrDTO dfrDTO;
				for (Iterator<DfrDTO> iterator = lstScopeDfrs.iterator(); iterator
						.hasNext();) {
					 dfrDTO = iterator.next();
					 if (selectedDfrDto.getDfrId() == dfrDTO.getDfrId())
					 {
						 selectedDfrDto = dfrDTO;
						 m9kChart.setSelectedDfr(selectedDfrDto);
						 break;
					 }
					
				}
			 }
			 session.put("selectedScopeDfr", selectedDfrDto);
			 logger.debug("updated selected Dfr "+m9kChart.getSelectedDfr());
			 return SUCCESS;
		}
		
		public String updatePrimaryOrSecondary()
		{
			if (getPrimaryOrSecondaryDisplay() == null || getPrimaryOrSecondaryDisplay().isEmpty() || getPrimaryOrSecondaryDisplay().trim().equalsIgnoreCase(M9kConstants.PRIMARY))
			{
				m9kChart.setPrimary(true);
			}
			else
			{
				m9kChart.setPrimary(false);
			}
			logger.debug("Is primary display set? "+m9kChart.isPrimary());
			return SUCCESS;
		}
		
		private void updateDigitalChannels()
		{
			logger.debug("Entered updateDigitalChannels... "+selectedDfrDto);
			mapDigitalsDisplayInfo = new LinkedHashMap<String, M9kKeyValuePair>(selectedDfrDto.getLstEventChannelNames().size());
			String[] channelIds = new String[selectedDfrDto.getLstEventChannelNames().size()];
			String channelName;
			String colorName;
			int i = 0;
			for (Iterator<String> iterator = selectedDfrDto.getLstEventChannelNames().iterator(); iterator.hasNext();) {
				channelName = iterator.next();
				channelIds[i++] = channelName.substring(0, channelName.indexOf("-"));
			}
			for (int j = 0; j < channelIds.length; j++) {
				if (m9kChart.getLstEventChannelStatus().get(j).equals(Color.RED))
				{
					colorName="RED";
				}
				else
				{
					colorName="GREEN";
				}
//				logger.debug("Digital channel status Chnl Id: "+channelIds[j]+" status : "+colorName);
				mapDigitalsDisplayInfo.put(channelIds[j], new M9kKeyValuePair(selectedDfrDto.getLstEventChannelNames().get(j), colorName));

		        }
			session.put("scopeDigitalInfo", mapDigitalsDisplayInfo);
			logger.debug("Returning from updateDigitalChannels "+mapDigitalsDisplayInfo);
		}

	public String getUpdatedChartDisplay()
	{
		String returnValue="Both"; // Default display both analogs and digitals
		logger.debug("Entered getUpdatedChartDisplay m9kchart "+m9kChart+" data points show "+getShowHideDataPoints());
		
		if (getShowHideDataPoints() == null)
		{
			setShowHideDataPoints(false);
			logger.debug(" data points show was null "+getShowHideDataPoints());
		}
		m9kChart = (M9kChart) session.get("m9kScopeChart");
		updateSelectedDfr();
		logger.debug("selected dfr dto "+selectedDfrDto+" analog "+selectedDfrDto.getAnalogChnlCnt()+" digitals "+ selectedDfrDto.getDigitalChnlCnt() );
		
		if (selectedDfrDto.getAnalogChnlCnt() > 0)
		{
			if (selectedDfrDto.getDigitalChnlCnt() > 0)
			{
				returnValue = "Both";
			}
			else
			{
				returnValue = "AnalogOnly";
			}
			selectedChannels = new String[1];
			selectedChannels[0] = selectedDfrDto.getLstAnalogChannelNames().get(0); 
			strSelectedChannels = getSelectedChannels()[0].substring(1, selectedChannels[0].indexOf("-"));
			setRequestedCycles("5");
		}
		else
		{
			returnValue = "DigitalOnly";
		}
		setChartChanged(true);
		logger.debug("Return value "+returnValue);
		return returnValue;
	}
	
	public String performEventTest()
	{

    	CalibrationDTO calibResult = null;
    	try
		{
    		setEventTestStatus(3);
			String commandToSend = "EVENTTEST,transient="+(getEventTestTransient()?"1":"0")+",test=1";// TODO: No of times is hardcoded default to 1 May be shud get from user at later point 
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.addProperty("MESSAGE_TYPE", "EVENTTEST");
			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());

			calibResult = (CalibrationDTO)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), commandToSend, messageProperties);
			System.out.println("calibResult received "+calibResult);

			if (calibResult == null)
        	{
					    setEventTestResult("Event Test failed."); 
        	}
        	else if (calibResult.getCalStatus().toUpperCase().equalsIgnoreCase("PASS") )
			{
        		eventTestResult = "EVENT TEST COMPLETED SUCCESSFULLY."+M9kConstants.NEWLINE+M9kConstants.NEWLINE+calibResult.getCalReport().toString();  
			}
			else
			{
				eventTestResult = "EVENT TEST FAILED."+M9kConstants.NEWLINE+M9kConstants.NEWLINE+calibResult.getCalReport().toString();
			}
			setEventTestStatus(100);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			eventTestResult = ex.getMessage();
		}
		finally{
			}
    
		return SUCCESS;
	}
	
	public String performInternalCalibration()
	{
    	CalibrationDTO calibResult = null;
    	try
		{
    		setCalibrationStatus(1);
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.addProperty("MESSAGE_TYPE", "CALIBRATE");
			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());

			calibResult = (CalibrationDTO)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "CALIBRATE", messageProperties);
			logger.debug("calibResult received "+calibResult);
			
        	if (calibResult == null)
        	{
        		setInternalCalibrationResult("Calibration failed.");
        	}
        	else if (calibResult.getCalStatus().toUpperCase().equalsIgnoreCase("PASS") )
			{
        		setInternalCalibrationResult("CALIBRATION COMPLETED SUCCESSFULLY."+M9kConstants.NEWLINE+M9kConstants.NEWLINE+calibResult.getCalReport().toString());
			}
			else
			{
				setInternalCalibrationResult("CALIBRATION FAILED "+calibResult.getCalStatusMsg()+M9kConstants.NEWLINE+M9kConstants.NEWLINE+calibResult.getCalReport().toString());
			}
        	setCalibrationStatus(100);
		}
		catch (Exception ex) {
			
			internalCalibrationResult = ex.getMessage();
		}
		return SUCCESS;
	}

	public String applyCalFactors()
	{
    	try
		{
    		setCalibrationStatus(1);
    		MessageProperties messageProperties = new MessageProperties();
			messageProperties.addProperty("MESSAGE_TYPE", "APPLY_CAL_FACTORS");
			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());

			String applyStatus = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "calfactors,apply=1", messageProperties);
			logger.debug("After sending applycalgfactors command: "+applyStatus);
			if (applyStatus.toUpperCase().indexOf("ERROR") == -1)
			{
				setApplyCalFactorsResult("Successfully applied the calibration factors");
			}
			else
			{
				setApplyCalFactorsResult("Failed to apply the calibration factors");
			}
        	setCalibrationStatus(100);
		}
		catch (Exception ex) {
			
			setApplyCalFactorsResult(ex.getMessage());
		}
		return SUCCESS;
	}
	
	public String verifyCalibration()
	{
		try
		{
			setCalibrationStatus(1);
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.addProperty("MESSAGE_TYPE", "VERIFY_CAL_FACTORS");
			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
			logger.debug("About to send calfactors,verify=1 message to activemq");
			CalibrationDTO calVerificationResult = (CalibrationDTO)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "calfactors,verify=1", messageProperties);
			logger.debug("Returned from ActiveMQ "+calVerificationResult);
			if (calVerificationResult!= null && calVerificationResult.getCalStatus().toUpperCase().equalsIgnoreCase("PASS"))
			{
				setVerifyCalibrationResult(calVerificationResult.getCalReport().toString());
			}
			else
			{
				if (calVerificationResult == null)
				{
					setVerifyCalibrationResult("No report received. Error in verification");
				}
				else
				{
					setVerifyCalibrationResult(calVerificationResult.getCalReport().toString());
				}
			}
			setCalibrationStatus(100);
		}
		catch (Exception ex) {
			
			setVerifyCalibrationResult(ex.getMessage());
		}

		return SUCCESS;
	}
	public String updateLastCalVerifiedDate() {
		try {
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.addProperty("MESSAGE_TYPE", "FETCH_LAST_CAL_VERIFY_DATE");
			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());

			lastCalVerifiedDate = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "FETCH_LAST_CAL_VERIFY_DATE", messageProperties);
			logger.info("lastCalVerifiedDate from SCOPE "+lastCalVerifiedDate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lastCalVerifiedDate = e.getMessage();
		}
		return SUCCESS;
	}
	
	public String updateLastCalibratedDate() {
		try {
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.addProperty("MESSAGE_TYPE", "FETCH_LAST_CAL_DATE");
			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());

			lastCalibratedDate = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "FETCH_LAST_CAL_DATE", messageProperties);
			logger.info("lastCalibratedDate from SCOPE "+lastCalibratedDate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lastCalibratedDate = e.getMessage();
		}
		return SUCCESS;
	}
	
	public String updateLastEventTestDate() {
		try {
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.addProperty("MESSAGE_TYPE", "FETCH_LAST_EVENTTEST_DATE");
			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
			lastEventTestDate = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), "FETCH_LAST_EVENTTEST_DATE", messageProperties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lastEventTestDate = e.getMessage();
		}
		return SUCCESS;
	}

	private String getExternalCalibrateCommand() {
		logger.debug("\t\tEXTERNAL_CAL: Is external reset flag set? "+getExtCalReset());
		logger.debug("\\t\\tEXTERNAL_CAL: Desired Value "+getDesiredValue());
		logger.debug("\\t\\tEXTERNAL_CAL: Offset correction checked? "+getOffsetCorrection());
		logger.debug("\\t\\tEXTERNAL_CAL: Selected channels string "+getStrSelectedChannels());
        selectedDfrDto = (DfrDTO) session.get("selectedScopeDfr");
		StringBuffer command = new StringBuffer(selectedDfrDto.getIpAddress()+",extcal");
//		for (int i = 0; i < selectedIndices.length; i++) {
//			command.append(", ch="+selectedIndices[i]);
//		}
//		selectedDfrDto.getLstAnalogChannels().get(index)
		int startIndex=1;
		populateSelectedChannels();
		for (int i = 0; i < getSelectedChannels().length; i++) {
//			logger.debug("index "+getSelectedChannels()[i]);
			if (getSelectedChannels()[i].startsWith(M9kConstants.VIRTUAL_PREFIX))
			{
//				startIndex = 3;
				startIndex = M9kConstants.VIRTUAL_PREFIX.length();
			}
			else
			{
				startIndex = 1;
			}
				command.append(",ch="+(getSelectedChannels()[i].substring(startIndex, selectedChannels[i].indexOf("-"))));
		}
		if (getExtCalReset())
		{
			command.append(",reset=1");
		}
		else if (getDesiredValue() != null && !getDesiredValue().isEmpty())
		{
			double calculatedDesiredValue = calculateDesiredValue(); // In case of Transducer channel and primary selected do desired value * slope + intercept
			logger.debug("Actual desired value "+getDesiredValue()+" calculated for Transducer if any "+calculatedDesiredValue);
			if (getOffsetCorrection())
			{
				command.append(",desired="+calculatedDesiredValue+",OffsetCorrection=1");
			}
			else
			{
				command.append(",desired="+calculatedDesiredValue);
			}
		}
		else
		{
			command.append(",OffsetCorrection=1");
		}
		if (getPrimaryOrSecondaryDisplay() == null || getPrimaryOrSecondaryDisplay().isEmpty() || getPrimaryOrSecondaryDisplay().trim().equalsIgnoreCase(M9kConstants.PRIMARY))
		{
			command.append(",ps=p");
		}
		else
		{
			command.append(",ps=s");
		}
		command.append(",user=machine");
		logger.debug("External calibration command to be sent "+command.toString());
		return command.toString();
	}

	public String processExternalCalibrationRequest() throws Exception {
		String calibResult = null;
		try
		{
			logger.debug("\t\tEXTERNAL_CAL1: Is external reset flag set? "+getExtCalReset());
			logger.debug("\\t\\tEXTERNAL_CAL1: Desired Value "+getDesiredValue());
			logger.debug("\\t\\tEXTERNAL_CAL1: Offset correction checked? "+getOffsetCorrection());
			logger.debug("\\t\\tEXTERNAL_CAL1: Selected channels string "+getStrSelectedChannels());
			
			MessageProperties messageProperties = new MessageProperties();
			messageProperties.addProperty("MESSAGE_TYPE", "EXTERNAL-CALIBRATE");
			messageProperties.addProperty("USER_NAME", M9kUtils.getUsersDto().getUserName());
			calibResult = (String)M9kMessagesUtil.sendSynchMessageFromApplet(""+getStationDetails().getSystemStationId(), getExternalCalibrateCommand(), messageProperties);
		}
		catch (M9000Exception e) {
			logger.error("Error in External calibration process ",e);
			throw e;
		}
		if (calibResult != null && calibResult.toUpperCase().contains("FAIL"))
		{
			setExternalCalibrationResult(calibResult);
		}
		else
		{
			if (getExtCalReset())
			{
				setExternalCalibrationResult("Reset External Calibration Successful.");
			}
			else
			{
				setExternalCalibrationResult("External Calibration Successful.");
			}
		}
		
		return SUCCESS;
	}

	 private double calculateDesiredValue() {
		 double desiredValueForTransducer = Double.parseDouble(getDesiredValue());
		 String[] selectedChannelIds = getStrSelectedChannels().split(",");
		 AnalogInfo analogInfo;
		 int chnlIndex = 0;
		 logger.debug("isTransducerChannelSelected: Selected channels "+getStrSelectedChannels());
		 selectedChannels = new String[selectedChannelIds.length];
		 for (int i = 0; i < selectedChannelIds.length; i++) {
			 chnlIndex = Integer.valueOf(selectedChannelIds[i]) - 1;
			 logger.debug("isTransducerChannelSelected: Analog channel Info for selected channel "+selectedDfrDto.getLstAnalogsInfo().get(chnlIndex));
			 analogInfo = selectedDfrDto.getLstAnalogsInfo().get(chnlIndex);
			 if (analogInfo.isTranducer() && getPrimaryOrSecondaryDisplay() != null && !getPrimaryOrSecondaryDisplay().isEmpty() && !getPrimaryOrSecondaryDisplay().trim().equalsIgnoreCase(M9kConstants.PRIMARY))
			 {
				 logger.debug("isTransducerChannelSelected: It is transducer channel Desired Value "+desiredValueForTransducer+" primary "+analogInfo.getPrimary()+" secondary "+analogInfo.getSecondary());
				 desiredValueForTransducer = (desiredValueForTransducer * analogInfo.getPrimary()) + analogInfo.getSecondary(); // DesiredValue * slope + Intercept for Transducer e.g. Current to Hz
				 break;
			 }
		}
		return desiredValueForTransducer;
	}
	 
	@SuppressWarnings("unchecked")
	public String getUpdatedScopeDigitalInfo()
	{
		mapDigitalsDisplayInfo = (Map<String, M9kKeyValuePair>) session.get("scopeDigitalInfo");
		logger.debug("Inside getUpdatedScopeDigitalInfo "+((getMapDigitalsDisplayInfo() != null)?getMapDigitalsDisplayInfo().size():getMapDigitalsDisplayInfo()));
		return SUCCESS;
	}
	public List<Map<Integer, Double>> getLstOfMapDataSetForJQ() {
		return lstOfMapDataSetForJQ;
	}

	public void setLstOfMapDataSetForJQ(List<Map<Integer, Double>> lstOfMapDataSetForJQ) {
		this.lstOfMapDataSetForJQ = lstOfMapDataSetForJQ;
	}


	public String[] getScopeRmsValues() {
		return scopeRmsValues;
	}

	public void setScopeRmsValues(String[] scopeRmsValues) {
		this.scopeRmsValues = scopeRmsValues;
	}

	public List<DfrDTO> getLstScopeDfrs() {
		return lstScopeDfrs;
	}

	public void setLstScopeDfrs(List<DfrDTO> lstScopeDfrs) {
		this.lstScopeDfrs = lstScopeDfrs;
	}

	public String getPrimaryOrSecondaryDisplay() {
		return primaryOrSecondaryDisplay;
	}

	public void setPrimaryOrSecondaryDisplay(String primaryOrSecondaryDisplay) {
		this.primaryOrSecondaryDisplay = primaryOrSecondaryDisplay;
	}

	public Map<String, M9kKeyValuePair> getMapDigitalsDisplayInfo() {
		return mapDigitalsDisplayInfo;
	}

	public void setMapDigitalsDisplayInfo(Map<String, M9kKeyValuePair> mapDigitalsDisplayInfo) {
		this.mapDigitalsDisplayInfo = mapDigitalsDisplayInfo;
	}

	public String getStrSelectedChannels() {
		return strSelectedChannels;
	}

	public void setStrSelectedChannels(String strSelectedChannels) {
		this.strSelectedChannels = strSelectedChannels;
	}

	public Boolean isChartChanged() {
		return chartChanged;
	}

	public void setChartChanged(Boolean chartChanged) {
		this.chartChanged = chartChanged;
	}

	public Boolean getShowHideDataPoints() {
		return showHideDataPoints;
	}

	public void setShowHideDataPoints(Boolean showHideDataPoints) {
		this.showHideDataPoints = showHideDataPoints;
	}

	public Integer getRequestedCycles() {
		int validatedCycles = 5;
		if (requestedCycles != null && requestedCycles.matches("-?\\d+"))
		{
			try
			{
				validatedCycles = Integer.parseInt(requestedCycles);
			}
			catch(Exception e)
			{
				logger.error("Non Numeric value foung in requested cycles field. Setting default cycles to 5.",e);
				validatedCycles = 5;
			}
		}
		return validatedCycles;
	}

	public void setRequestedCycles(String requestedCycles) {
		this.requestedCycles = requestedCycles;
	}

	public Boolean getEventTestTransient() {
		return eventTestTransient;
	}

	public void setEventTestTransient(Boolean eventTestTransient) {
		this.eventTestTransient = eventTestTransient;
	}

	public String getEventTestResult() {
		return eventTestResult;
	}

	public void setEventTestResult(String eventTestResult) {
		this.eventTestResult = eventTestResult;
	}

	public Integer getEventTestStatus() {
		logger.debug("Inside to get the event test  status "+eventTestStatus);
		if (eventTestStatus != null)
		{
			eventTestStatus += 3;
		}
		else
		{
			eventTestStatus = 5;
		}
		return eventTestStatus;
	}

	public void setEventTestStatus(Integer eventTestStatus) {
		this.eventTestStatus = eventTestStatus;
	}

	public String getInternalCalibrationResult() {
		return internalCalibrationResult;
	}

	public void setInternalCalibrationResult(String internalCalibrationResult) {
		this.internalCalibrationResult = internalCalibrationResult;
	}

	public Integer getCalibrationStatus() {
		logger.debug("Inside to get the Calibration status "+calibrationStatus);
		if (calibrationStatus != null)
		{
			calibrationStatus += 1;
		}
		else
		{
			calibrationStatus = 1;
		}
		
		return calibrationStatus;
	}

	public void setCalibrationStatus(Integer calibrationStatus) {
		this.calibrationStatus = calibrationStatus;
	}

	public String getVerifyCalibrationResult() {
		return verifyCalibrationResult;
	}

	public void setVerifyCalibrationResult(String verifyCalibrationResult) {
		this.verifyCalibrationResult = verifyCalibrationResult;
	}

	public String getApplyCalFactorsResult() {
		return applyCalFactorsResult;
	}

	public void setApplyCalFactorsResult(String applyCalFactorsResult) {
		this.applyCalFactorsResult = applyCalFactorsResult;
	}

	public void setLastCalVerifiedDate(String lastCalVerifiedDate) {
		this.lastCalVerifiedDate = lastCalVerifiedDate;
	}

	public void setLastCalibratedDate(String lastCalibratedDate) {
		this.lastCalibratedDate = lastCalibratedDate;
	}

	public void setLastEventTestDate(String lastEventTestDate) {
		this.lastEventTestDate = lastEventTestDate;
	}

	public String getLastCalVerifiedDate() {
		return lastCalVerifiedDate;
	}

	public String getLastCalibratedDate() {
		return lastCalibratedDate;
	}

	public String getLastEventTestDate() {
		return lastEventTestDate;
	}

	public Boolean getEnableTransient() {
		return enableTransient;
	}

	public void setEnableTransient(Boolean enableTransient) {
		this.enableTransient = enableTransient;
	}

	public Boolean getOffsetCorrection() {
		return offsetCorrection;
	}

	public void setOffsetCorrection(Boolean offsetCorrection) {
		this.offsetCorrection = offsetCorrection;
	}

	public Boolean getExtCalReset() {
		return extCalReset;
	}

	public void setExtCalReset(Boolean extCalReset) {
		this.extCalReset = extCalReset;
	}

	public String getDesiredValue() {
		return desiredValue;
	}

	public void setDesiredValue(String desiredValue) {
		this.desiredValue = desiredValue;
	}

	public String getExternalCalibrationResult() {
		return externalCalibrationResult;
	}

	public void setExternalCalibrationResult(String externalCalibrationResult) {
		this.externalCalibrationResult = externalCalibrationResult;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean isUpdateScopeConfig() {
		return updateScopeConfig;
	}

	public void setUpdateScopeConfig(boolean updateScopeConfig) {
		this.updateScopeConfig = updateScopeConfig;
	}

	}
