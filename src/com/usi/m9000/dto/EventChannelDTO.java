package com.usi.m9000.dto;

import java.io.Serializable;

import org.apache.commons.lang.StringEscapeUtils;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import com.usi.m9000.util.M9kXMLConstants;
@Validations
public class EventChannelDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String channel;
	private String description;
	private String descriptionKey; // For auto complete functionality
	private String normalState;
	private String dfrStart;
//	private String dfrSer;
//	private String serRun;
	private String displayChannel;
	private boolean disableDfrStart;
//	private boolean disableSerRun;
//	private boolean triggerStatus;
	private String chassis;
	private String altDescription;
	private boolean dfr;
	private boolean ser;
	private boolean pmu;
	private boolean debounce;

	/**
	 * 
	 */
	public EventChannelDTO() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}
	/**
	 * @return the description
	 */
	@RequiredStringValidator(type=ValidatorType.FIELD, fieldName="Event Description", key="errors.required", message="")
	public String getDescription() {
		return StringEscapeUtils.unescapeXml(description);
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = StringEscapeUtils.escapeXml(description);
	}
	/**
	 * @return the normalState
	 */
	public String getNormalState() {
		return normalState;
	}
	/**
	 * @param normalState the normalState to set
	 */
	public void setNormalState(String normalState) {
		this.normalState = normalState;
	}
	/**
	 * @return the dfrStart
	 */
	public String getDfrStart() {
		return dfrStart;
	}
	/**
	 * @param dfrStart the dfrStart to set
	 */
	public void setDfrStart(String dfrStart) {
		this.dfrStart = dfrStart;
	}
//	/**
//	 * @return the dfrSer
//	 */
//	public String getDfrSer() {
//		return dfrSer;
//	}
//	/**
//	 * @param dfrSer the dfrSer to set
//	 */
//	public void setDfrSer(String dfrSer) {
//		this.dfrSer = dfrSer;
//	}
//	/**
//	 * @return the serRun
//	 */
//	public String getSerRun() {
//		return serRun;
//	}
//	/**
//	 * @param serRun the serRun to set
//	 */
//	public void setSerRun(String serRun) {
//		this.serRun = serRun;
//	}
	/**
	 * @return the displayChannel
	 */
	public String getDisplayChannel() {
		displayChannel = "E"+getChannel();
		return displayChannel;
	}
	/**
	 * @param displayChannel the displayChannel to set
	 */
	public void setDisplayChannel(String displayChannel) {
		this.displayChannel = displayChannel;
	}
	/**
	 * @return the descriptionKey
	 */
	public String getDescriptionKey() {
		return descriptionKey;
	}
	/**
	 * @param descriptionKey the descriptionKey to set
	 */
	public void setDescriptionKey(String descriptionKey) {
		this.descriptionKey = descriptionKey;
	}
	/**
	 * @return the disableDfrStart
	 */
	public boolean getDisableDfrStart() {
		return disableDfrStart;
	}
	/**
	 * @param disableDfrStart the disableDfrStart to set
	 */
	public void setDisableDfrStart(boolean disableDfrStart) {
		this.disableDfrStart = disableDfrStart;
	}
//	/**
//	 * @return the disableSerRun
//	 */
//	public boolean getDisableSerRun() {
//		return disableSerRun;
//	}
//	/**
//	 * @param disableSerRun the disableSerRun to set
//	 */
//	public void setDisableSerRun(boolean disableSerRun) {
//		this.disableSerRun = disableSerRun;
//	}
//	/**
//	 * @return the triggerStatus
//	 */
//	public boolean isTriggerStatus() {
//		return triggerStatus;
//	}
//	/**
//	 * @return the triggerStatus
//	 */
//	public boolean getTriggerStatus() {
//		return triggerStatus;
//	}
//	/**
//	 * @param triggerStatus the triggerStatus to set
//	 */
//	public void setTriggerStatus(boolean triggerStatus) {
//		this.triggerStatus = triggerStatus;
//	}
	
	public String getInputXMLString() {
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_EVENT_CHANNELS_INPUT);
		inputTag.append(getName());
		inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
		return inputTag.toString();
	}
	public String getChassis() {
		return chassis;
	}
	public void setChassis(String chassis) {
		this.chassis = chassis;
	}
	public String getAltDescription() {
		return altDescription;
	}
	public void setAltDescription(String altDescription) {
		this.altDescription = altDescription;
	}
	public boolean isDfr() {
		return dfr;
	}
	public void setDfr(boolean dfr) {
		this.dfr = dfr;
	}
	public boolean isSer() {
		return ser;
	}
	public void setSer(boolean ser) {
		this.ser = ser;
	}
	/**
	 * @return the pmu
	 */
	public boolean isPmu() {
		return pmu;
	}
	/**
	 * @param pmu the pmu to set
	 */
	public void setPmu(boolean pmu) {
		this.pmu = pmu;
	}
	/**
	 * @return the debounce
	 */
	public boolean isDebounce() {
		return debounce;
	}
	/**
	 * @param debounce the debounce to set
	 */
	public void setDebounce(boolean debounce) {
		this.debounce = debounce;
	}
}
