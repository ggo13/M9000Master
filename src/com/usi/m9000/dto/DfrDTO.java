/**
 * 
 */
package com.usi.m9000.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import com.usi.m9000.config.AnalogInfo;
import com.usi.m9000.config.ChannelsListInfo;
import com.usi.m9000.config.DigitalInfo;
import com.usi.m9000.util.M9kConstants;

/**
 * @author sramasamy
 *
 */
@Validations
public class DfrDTO implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int dfrId;
	String dfrName;
	String ipAddress;
	int analogChnlCnt;
	int digitalChnlCnt;
	String status;
	transient ChannelsListInfo chnlConfigDropDownList;
	int analogChannelStart;
	int analogChannelEnd;
	int digitalChannelStart;
	int digitalChannelEnd;

	// For scope
	List<String> lstAnalogChannelNames;
	List<String> lstEventChannelNames;
	private float sampleRate; 
	private int lineFreq;
	List<AnalogInfo> lstAnalogsInfo;
	List<DigitalInfo> lstEventsInfo;
	List<EventChannelDTO> lstEventChannels;
	List<TriggerChannelDTO> lstTriggers;
	
	// For LTR
	List<AnalogChannelDTO> lstAnalogChannels;
	
	// FOR virtuals
	int physicalAnalogsCount;
	// For SCOPE drop down display names
	int physicalAnalogsStart;
	int physicalAnalogsEnd;
	int physicalDigitalsCount;
	int physicalDigitalsStart;
	int physicalDigitalsEnd;
	
	// For new SCOPE select channels list
	Map<Integer, String> mapAnalogChannelNames;
	
	// START: 15-Jun-2020 - M9kMini - implementation
	boolean isAnevCard = false; // Is Anev card part of the stack
	// END: 15-Jun-2020 - M9kMini - implementation
	
	// START: 20-May-2021 - Add or remove chassis
	boolean newlyAdded;
	// END: 20-May-2021
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(DfrDTO.class);
	
	/**
	 * 
	 */
	public DfrDTO() {
//		logger.debug("Entered DFRDTO constructor... ");
		chnlConfigDropDownList = new ChannelsListInfo();
//		if (!chnlConfigDropDownList.getChannelsConfList().isEmpty())
//		{
//			chnlConfigDropDownList.setSelectedValue(chnlConfigDropDownList.getChannelsConfList().get(0).toString());
//		}
	}

	// For scope ChannelsListInfo not required
	public DfrDTO(String name) {
		setDfrName(name);
	}

	/**
	 * @return the dfrName
	 */
	@RequiredStringValidator(type=ValidatorType.FIELD, key="errors.required", message="")
	public String getDfrName() {
		return dfrName;
	}

	/**
	 * @param dfrName the dfrName to set
	 */
	public void setDfrName(String dfrName) {
		this.dfrName = dfrName;
	}

	/**
	 * @return the ipAddress
	 */
	@RequiredStringValidator(type=ValidatorType.FIELD, fieldName="ipAddress", key="errors.required", message="")
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String dfrIp) {
		this.ipAddress = dfrIp;
	}

	/**
	 * @return the analogChnlCnt
	 */
	public int getAnalogChnlCnt() {
		return analogChnlCnt;
	}

	/**
	 * @param analogChnlCnt the analogChnlCnt to set
	 */
	public void setAnalogChnlCnt(int analogChnlCnt) {
		this.analogChnlCnt = analogChnlCnt;
	}

	/**
	 * @return the digitalChnlCnt
	 */
	public int getDigitalChnlCnt() {
		return digitalChnlCnt;
	}

	/**
	 * @param digitalChnlCnt the digitalChnlCnt to set
	 */
	public void setDigitalChnlCnt(int digitalChnlCnt) {
		this.digitalChnlCnt = digitalChnlCnt;
	}
	/**
	 * @return the chnlConfigDropDownList
	 */
	public ChannelsListInfo getChnlConfigDropDownList() {
		return chnlConfigDropDownList;
	}
	/**
	 * @param chnlConfigDropDownList the chnlConfigDropDownList to set
	 */
	public void setChnlConfigDropDownList(ChannelsListInfo chnlConfigList) {
		this.chnlConfigDropDownList = chnlConfigList;
	}
	
	public String getScopeAppletString()
	{
		StringBuffer strbuf = new StringBuffer();
		strbuf.append(getIpAddress()+":"+getAnalogChannelStart()+":"+getAnalogChannelEnd());
		return strbuf.toString();
	}
	public String toString()
	{
		StringBuffer strbuf = null;
		if (getAnalogChnlCnt() > 0)
		{
			strbuf = new StringBuffer();
			strbuf.append(getDfrName() +" - " + "Analogs["+getAnalogChannelStart()+" - "+getAnalogChannelEnd()+"]");				
		}
		if (getDigitalChnlCnt() > 0)
		{
			if (strbuf == null)
			{
				strbuf = new StringBuffer();
				strbuf.append(" - " + "Events["+getDigitalChannelStart()+" - "+getDigitalChannelEnd()+"]");
			}
			else
			{
				strbuf.append(getDfrName() +" - " + "Events["+getDigitalChannelStart()+" - "+getDigitalChannelEnd()+"]");
			}
		}
//		strbuf.append("DFR Name: "+getDfrName());
//		strbuf.append("DFR IP Address: "+getIpAddress());
//		if (getChnlConfigDropDownList() != null)
//		{
//			strbuf.append("DFR Analog Count: "+getChnlConfigDropDownList().getAnalogCnt());
//			strbuf.append("DFR Digital Count: "+getChnlConfigDropDownList().getDigitalCnt());
//			strbuf.append("DFR Selected Value: "+getChnlConfigDropDownList().getSelectedValue());
//		}
//		strbuf.append("DFR Status: "+getStatus());
//		strbuf.append("DFR Analog channels: ("+getAnalogChannelStart()+" , "+getAnalogChannelEnd()+")");
//		strbuf.append("DFR Digital channels: ("+getDigitalChannelStart()+" , "+getDigitalChannelEnd()+")");
		return (strbuf != null?strbuf.toString():"It's NULL");
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the analogChannelStart
	 */
	public int getAnalogChannelStart() {
		return analogChannelStart;
	}
	/**
	 * @param analogChannelStart the analogChannelStart to set
	 */
	public void setAnalogChannelStart(int channelStart) {
		this.analogChannelStart = channelStart;
	}
	/**
	 * @return the analogChannelEnd
	 */
	public int getAnalogChannelEnd() {
		return analogChannelEnd;
	}
	/**
	 * @param analogChannelEnd the analogChannelEnd to set
	 */
	public void setAnalogChannelEnd(int channelEnd) {
		this.analogChannelEnd = channelEnd;
	}
	/**
	 * @return the digitalChannelStart
	 */
	public int getDigitalChannelStart() {
		return digitalChannelStart;
	}
	/**
	 * @param digitalChannelStart the digitalChannelStart to set
	 */
	public void setDigitalChannelStart(int digitalChannelStart) {
		this.digitalChannelStart = digitalChannelStart;
	}
	/**
	 * @return the digitalChannelEnd
	 */
	public int getDigitalChannelEnd() {
		return digitalChannelEnd;
	}
	/**
	 * @param digitalChannelEnd the digitalChannelEnd to set
	 */
	public void setDigitalChannelEnd(int digitalChannelEnd) {
		this.digitalChannelEnd = digitalChannelEnd;
	}
	/**
	 * @return the lstAnalogChannelNames
	 */
	public List<String> getLstAnalogChannelNames() {
		return lstAnalogChannelNames;
	}
	/**
	 * @param lstAnalogChannelNames the lstAnalogChannelNames to set
	 */
	public void setLstAnalogChannelNames(List<String> lstAnalogChannelNames) {
		this.lstAnalogChannelNames = lstAnalogChannelNames;
	}
	/**
	 * @return the lstEventChannelNames
	 */
	public List<String> getLstEventChannelNames() {
		return lstEventChannelNames;
	}
	/**
	 * @param lstEventChannelNames the lstEventChannelNames to set
	 */
	public void setLstEventChannelNames(List<String> lstEventChannelNames) {
		this.lstEventChannelNames = lstEventChannelNames;
	}

	/**
	 * @return the sampleRate
	 */
	public float getSampleRate() {
		return sampleRate;
	}

	/**
	 * @param sampleRate the sampleRate to set
	 */
	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * @return the lineFreq
	 */
	public int getLineFreq() {
		return lineFreq;
	}

	/**
	 * @param lineFreq the lineFreq to set
	 */
	public void setLineFreq(int lineFreq) {
		this.lineFreq = lineFreq;
	}

	public int getDfrId() {
		return dfrId;
	}

	public void setDfrId(int dfrId) {
		this.dfrId = dfrId;
	}

	public List<AnalogChannelDTO> getLstAnalogChannels() {
		return lstAnalogChannels;
	}

	public void setLstAnalogChannels(List<AnalogChannelDTO> lstAnalogChannels) {
		this.lstAnalogChannels = lstAnalogChannels;
	}

	public String toString1() {
		return "DfrDTO [dfrId=" + dfrId + ", dfrName=" + dfrName
				+ ", ipAddress=" + ipAddress + ", analogChnlCnt="
				+ analogChnlCnt + ", digitalChnlCnt=" + digitalChnlCnt
				+ ", status=" + status + ", analogChannelStart="
				+ analogChannelStart + ", analogChannelEnd=" + analogChannelEnd
				+ ", digitalChannelStart=" + digitalChannelStart
				+ ", digitalChannelEnd=" + digitalChannelEnd
				+ ", lstAnalogChannelNames=" + lstAnalogChannelNames
				+ ", lstEventChannelNames=" + lstEventChannelNames
				+ ", sampleRate=" + sampleRate + ", lineFreq=" + lineFreq
				+ ", lstAnalogChannels=" + lstAnalogChannels + "]";
	}

	public List<EventChannelDTO> getLstEventChannels() {
		return lstEventChannels;
	}

	public void setLstEventChannels(List<EventChannelDTO> lstEventChannels) {
		this.lstEventChannels = lstEventChannels;
	}

	public List<TriggerChannelDTO> getLstTriggers() {
		return lstTriggers;
	}

	public void setLstTriggers(List<TriggerChannelDTO> lstTriggers) {
		this.lstTriggers = lstTriggers;
	}

	public List<AnalogInfo> getLstAnalogsInfo() {
		return lstAnalogsInfo;
	}

	public void setLstAnalogsInfo(List<AnalogInfo> lstAnalogsInfo) {
		this.lstAnalogsInfo = lstAnalogsInfo;
	}

	public List<DigitalInfo> getLstEventsInfo() {
		return lstEventsInfo;
	}

	public void setLstEventsInfo(List<DigitalInfo> lstEventsInfo) {
		this.lstEventsInfo = lstEventsInfo;
	}

	/**
	 * @return the physicalAnalogsCount
	 */
	public int getPhysicalAnalogsCount() {
		return physicalAnalogsCount;
	}

	/**
	 * @param physicalAnalogsCount the physicalAnalogsCount to set
	 */
	public void setPhysicalAnalogsCount(int physicalAnalogsCount) {
		this.physicalAnalogsCount = physicalAnalogsCount;
		// START: 05-June-2020 - Mini implementation
		if ( (this.physicalAnalogsCount % M9kConstants.NO_OF_ANALOG_CHNLS_PER_ANEV_BOARD) > 0)
		{
			setAnevCard(true);
		}
		// END: 15-June-2020 - Mini implementation
	}

	public String getScopeDisplayName()
	{
		StringBuffer strbuf = new StringBuffer();
		logger.debug("\t\t\t Analogs start "+ getPhysicalAnalogsStart());
		logger.debug("\t\t\t Analogs End "+ getPhysicalAnalogsEnd());
		logger.debug("\t\t\t Analogs Total "+ getPhysicalAnalogsCount());
		logger.debug("\t\t\t Digitals start "+ getDigitalChannelStart());
		logger.debug("\t\t\t Digitals End "+ getDigitalChannelEnd());
		logger.debug("\t\t\t Digitals Total "+ getPhysicalDigitalsCount());
		
		if (getPhysicalAnalogsCount() > 0)
		{
			strbuf.append(getDfrName() +" - " + "A["+getPhysicalAnalogsStart()+" - "+getPhysicalAnalogsEnd()+"]");
			if (getPhysicalDigitalsCount() > 0)
			{
				strbuf.append(" E["+getPhysicalDigitalsStart()+" - "+getPhysicalDigitalsEnd()+"]");
			}

		}
		else
		{
			strbuf.append(getDfrName() +" - " + "E["+getPhysicalDigitalsStart()+" - "+getPhysicalDigitalsEnd()+"]");
		}
		return strbuf.toString();
	}

	public Map<Integer, String> getMapAnalogChannelNames() {
		return mapAnalogChannelNames;
	}

	public void setMapAnalogChannelNames(Map<Integer, String> mapAnalogChannelNames) {
		this.mapAnalogChannelNames = mapAnalogChannelNames;
	}

	public int getPhysicalDigitalsCount() {
		return physicalDigitalsCount;
	}

	public void setPhysicalDigitalsCount(int physicalDigitalsCount) {
		this.physicalDigitalsCount = physicalDigitalsCount;
		// START: 05-June-2020 - Mini implementation
		if ( (this.physicalDigitalsCount % M9kConstants.NO_OF_DIGITAL_CHNLS_PER_ANEV_BOARD) > 0)
		{
			setAnevCard(true);
		}
		// END: 15-June-2020 - Mini implementation

	}

	public int getPhysicalAnalogsStart() {
		return physicalAnalogsStart;
	}

	public void setPhysicalAnalogsStart(int physicalAnalogsStart) {
		this.physicalAnalogsStart = physicalAnalogsStart;
	}

	public int getPhysicalAnalogsEnd() {
		return physicalAnalogsEnd;
	}

	public void setPhysicalAnalogsEnd(int physicalAnalogsEnd) {
		this.physicalAnalogsEnd = physicalAnalogsEnd;
	}

	public int getPhysicalDigitalsStart() {
		return physicalDigitalsStart;
	}

	public void setPhysicalDigitalsStart(int physicalDigitalsStart) {
		this.physicalDigitalsStart = physicalDigitalsStart;
	}

	public int getPhysicalDigitalsEnd() {
		return physicalDigitalsEnd;
	}

	public void setPhysicalDigitalsEnd(int physicalDigitalsEnd) {
		this.physicalDigitalsEnd = physicalDigitalsEnd;
	}

	public boolean isAnevCard() {
		return isAnevCard;
	}

	public void setAnevCard(boolean isAnevCard) {
		this.isAnevCard = isAnevCard;
	}

	/**
	 * @return the newlyAdded
	 */
	public boolean isNewlyAdded() {
		return newlyAdded;
	}

	/**
	 * @param newlyAdded the newlyAdded to set
	 */
	public void setNewlyAdded(boolean newlyAdded) {
		this.newlyAdded = newlyAdded;
	}

}
