package com.usi.m9000.actions;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.EventInputDocument.EventInput;
import com.usi.SubStationDocument.SubStation;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kScp;

public class M9kComtradeAction extends ActionSupport implements SessionAware, ServletContextListener{
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
	String fileName;
	String faultId;
	private StationDTO stationDetails;
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kComtradeAction.class);
	/**
	 * 
	 */
	public M9kComtradeAction()
	{
	}

	  public String execute()throws Exception{
		  String returnStatus = SUCCESS;
		  return returnStatus;
	  }

	  @SkipValidation
	  public String viewComtrade()
	  {
		  String returnValue = SUCCESS;
		  String accessMode = "comtrade";
		  logger.debug("Start of view Scope:: SubStation from session..."+session.get("selectedSubstation"));
		  session.put("accessMode", accessMode);
		logger.debug("File Name.."+getFileName());
		logger.debug("File Name.."+getFaultId());
		session.put("FileName", getFileName());
		session.put("FaultId", getFaultId());
		logger.debug("Fault Id: "+getFaultId());
		logger.debug("File Name: "+getFileName());
//		fetchComtradeFiles();
		clearActionErrors();
		  return returnValue;
	  }
	  
	  public String fetchComtradeFiles() 
	  {
			try {
				M9kScp m9kScp = new M9kScp(M9kConstants.DFR_IP,22,"user");
				m9kScp.getComtradeFiles("/root/*.dat", "c:/comtradeFiles/");
				m9kScp = new M9kScp(M9kConstants.DFR_IP,22,"user");
				m9kScp.getComtradeFiles("/root/*.cfg", "c:/comtradeFiles/");
				m9kScp = new M9kScp(M9kConstants.DFR_IP,22,"user");
				m9kScp.getComtradeFiles("/root/*.inf", "c:/comtradeFiles/");
			} catch (M9000Exception e) {
				// TODO: handle exception
				addActionError("Fetch Failed");
			}
		  
		  return SUCCESS;
	  }

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
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
  
	@SuppressWarnings("unchecked")
	public void validate()
	{
		lstAvailableStations = (List<StationDTO>) session.get("lstAvailableStations");
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
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the faultId
	 */
	public String getFaultId() {
		return faultId;
	}

	/**
	 * @param faultId the faultId to set
	 */
	public void setFaultId(String faultId) {
		this.faultId = faultId;
	}

}
