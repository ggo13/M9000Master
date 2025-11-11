/**
 * 
 */
package com.usi.m9000.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.interceptor.validation.SkipValidation;

import com.opensymphony.xwork2.ActionSupport;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.util.M9kKeyValuePair;
import com.usi.m9000.util.M9kUtils;

/**
 * @author sramasamy
 *
 */
public class M9kJSON extends ActionSupport implements SessionAware,
		ServletContextListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2634964690478153432L;
	private Map<String, Object> session;
	PropertiesConfiguration config;
	private List<M9kKeyValuePair> lineFreqObjList;
	private List<Object> lstSampleRate;
	private List<Object> lstLtSampleRate;
	private List<Object> lstPmuDataRate;
	private List<Object> lstExportRate;
	private List<Object> lstEventDebounce;
	
	private StationDTO stationDetails;

	private List<M9kKeyValuePair> lstLeafZones;

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kJSON.class);

	
	public M9kJSON() {
		
	}
	public String execute()
	{
		String result = SUCCESS;
		
		  
		return result;
	}

	public String getLineFreqList()
	{
		String result = SUCCESS;
		
		// Populate Line Frequency
		  List<Object> lstLineFrequency;
//		  logger.debug("In Execute method... ");
		  lstLineFrequency = config.getList("lineFrequency");
//		  logger.debug("lstLineFrequency from property file "+lstLineFrequency);
		  lineFreqObjList = new ArrayList<M9kKeyValuePair>();
		  for (int i = 0; i < lstLineFrequency.size(); i++) {
//			  logger.debug("LIne freq read : "+lstLineFrequency.get(i));
			  lineFreqObjList.add(new M9kKeyValuePair(lstLineFrequency.get(i).toString(), lstLineFrequency.get(i)+"Hz"));
		  }
		  logger.debug("Line Frequency...."+lineFreqObjList);
//		  getSampleRateLst();
//		  getLtrSampleRateLst();
		  return result;
	}
	  public String getSampleRateLst()
	  {
		  logger.debug("Entered getSampleRateLst..."+getStationDetails());
		  if (getStationDetails() != null && getStationDetails().getSystemLineFrequency() != null && getStationDetails().getSystemLineFrequency().intValue() == 50)
		  {
			  lstSampleRate= config.getList("sampleRate50");
		  }
		  else //if (getStationDetails().getSystemLineFrequency().equals("60"))
		  {
			  lstSampleRate= config.getList("sampleRate60");
		  }
		  logger.debug("LIst sample rate "+lstSampleRate);
		  return SUCCESS;
	  }

	  @SuppressWarnings("unchecked")
		@SkipValidation
		  public String getLtrSampleRateLst()
		  {
		  
			  logger.debug("Entered getLtrSampleRateLsttt..."+getStationDetails());
			  
			  List<Object> lstAvailableLtrSampleRates =  new ArrayList<Object>();
			  if (getStationDetails() != null && getStationDetails().getSystemLineFrequency() != null && getStationDetails().getSystemLineFrequency().intValue() == 50)
			  {
				  lstAvailableLtrSampleRates= config.getList("ltrSampleRate50");
			  }
			  else //if (getStationDetails().getSystemLineFrequency().equals("60"))
			  {
				  lstAvailableLtrSampleRates= config.getList("ltrSampleRate60");
			  }
			  int sampleRate = 0;
			  lstLtSampleRate = new ArrayList<Object>();
			  lstLtSampleRate.add("0");
			  if (getStationDetails() != null && getStationDetails().getSystemSampleRate() != null)
			  {
				  sampleRate = getStationDetails().getSystemSampleRate();
				  for (Iterator<Object> iterator = lstAvailableLtrSampleRates.iterator(); iterator.hasNext();) {
					String ltrSampleRate = iterator.next().toString();
					if (!ltrSampleRate.trim().equalsIgnoreCase("0") && (sampleRate % Integer.parseInt(ltrSampleRate)) == 0)
					{
						lstLtSampleRate.add(ltrSampleRate);
					}
				  }
			  }
			  else
			  {
				  lstLtSampleRate =  lstAvailableLtrSampleRates;
			  }
			  return SUCCESS;
		  }

		@SkipValidation
		  public String getPmuDataRateLst()
		  {
		  
//			  logger.debug("Entered getPmuDataRateLst..."+getStationDetails());
			  
			  if (getStationDetails() != null && getStationDetails().getSystemLineFrequency() != null && getStationDetails().getSystemLineFrequency().intValue() == 50)
			  {
				  lstPmuDataRate= config.getList("pmuDataRate50");
			  }
			  else //if (getStationDetails().getSystemLineFrequency().equals("60"))
			  {
				  lstPmuDataRate= config.getList("pmuDataRate60");
			  }
//			  logger.debug("LIst PMU date  rate "+lstPmuDataRate);
			  return SUCCESS;
		  }	  

		@SkipValidation
		  public String getGlobalExportRateLst()
		  {
		  
//			  logger.debug("Entered getGlobalExportRateLst..."+getStationDetails());
			  
			  if (getStationDetails() != null && getStationDetails().getSystemLineFrequency() != null && getStationDetails().getSystemLineFrequency().intValue() == 50)
			  {
				  lstExportRate= config.getList("exportRate50");
			  }
			  else //if (getStationDetails().getSystemLineFrequency().equals("60"))
			  {
				  lstExportRate= config.getList("exportRate60");
			  }
//			  logger.debug("LIst Export date  rate "+lstExportRate);
			  return SUCCESS;
		  }	  

		@SkipValidation
		  public String getEventDebounceLst()
		  {
		  
//			  logger.debug("Entered getEventDebounceLst... getStationDetails().getSystemSampleRate() "+getStationDetails().getSystemSampleRate());
			  
			  if (getStationDetails() != null && (getStationDetails().getSystemSampleRate() != null))
			  {
				  if (getStationDetails().getSystemSampleRate().intValue() == 24000)
				  {
					  lstEventDebounce= config.getList("eventDebounce24k");
				  }
				  else if (getStationDetails().getSystemSampleRate().intValue() == 19200 || getStationDetails().getSystemSampleRate().intValue() == 20000)
				  {
					  lstEventDebounce= config.getList("eventDebounce19-20K");
				  }
				  else if (getStationDetails().getSystemSampleRate().intValue() == 15000 || getStationDetails().getSystemSampleRate().intValue() == 16000)
				  {
					  lstEventDebounce= config.getList("eventDebounce15-16K");
				  }
				  else
				  {
					  lstEventDebounce= config.getList("eventDebounceOthers");
				  }
			  }
			  else //if (getStationDetails().getSystemLineFrequency().equals("60"))
			  {
				  lstEventDebounce= config.getList("eventDebounce24k");
			  }
			  logger.debug("LIst debounce value "+lstEventDebounce);
			  return SUCCESS;
		  }	  

		public String getJSON(){

			logger.debug("getJSON: Fetching Line frequency ");
			getLineFreqList();
			logger.debug("getJSON: Fetching sample rate");
			getSampleRateLst();
			logger.debug("getJSON: Fetching long term sample rate");
			getLtrSampleRateLst();
			logger.debug("getJSON: Fetching Global export rate");
			getGlobalExportRateLst();
			logger.debug("getJSON: Fetching Event debounce list");
			getEventDebounceLst();
			logger.debug("getJSON: Fetching PMU data rate");
			getPmuDataRateLst();
			
	        return SUCCESS;
	    }
		
	public String getHierarchyLeafZones() {
		if (M9kUtils.getHierarchyInfo() != null)
		{
			lstLeafZones = M9kUtils.getHierarchyInfo().getLstLeafZones();
		}
		return SUCCESS;
		}
	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.struts2.interceptor.SessionAware#setSession(java.util.Map)
	 */
	@Override
	public void setSession(Map<String, Object> session) {
		logger.debug("In set session...");
		this.session = session;
		config = (PropertiesConfiguration) this.session.get("config");

	}
	
	public List<M9kKeyValuePair> getLineFreqObjList() {
		return lineFreqObjList;
	}
	public StationDTO getStationDetails() {
		return stationDetails;
	}
	public void setStationDetails(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
	}
	public List<Object> getLstSampleRate() {
		return lstSampleRate;
	}
	public void setLstSampleRate(List<Object> lstSampleRate) {
		this.lstSampleRate = lstSampleRate;
	}
	public List<Object> getLstLtSampleRate() {
		return lstLtSampleRate;
	}
	public List<Object> getLstPmuDataRate() {
		return lstPmuDataRate;
	}
	public void setLstPmuDataRate(List<Object> lstPmuDataRate) {
		this.lstPmuDataRate = lstPmuDataRate;
	}
	public List<Object> getLstExportRate() {
		return lstExportRate;
	}
	public void setLstExportRate(List<Object> lstExportRate) {
		this.lstExportRate = lstExportRate;
	}
	/**
	 * @return the lstEventDebounce
	 */
	public List<Object> getLstEventDebounce() {
		return lstEventDebounce;
	}
	/**
	 * @param lstEventDebounce the lstEventDebounce to set
	 */
	public void setLstEventDebounce(List<Object> lstEventDebounce) {
		this.lstEventDebounce = lstEventDebounce;
	}
	public List<M9kKeyValuePair> getLstLeafZones() {
		return lstLeafZones;
	}
	public void setLstLeafZones(List<M9kKeyValuePair> LstLeafZones) {
		this.lstLeafZones = LstLeafZones;
	}

}
