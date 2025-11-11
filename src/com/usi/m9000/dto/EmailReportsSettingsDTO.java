/**
 * 
 */
package com.usi.m9000.dto;

import java.io.Serializable;

import com.usi.m9000.util.M9kUtils;

/**
 * @author sramasamy
 *
 */
public class EmailReportsSettingsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private boolean enableDailyStatusEmails=false;
	private String dailyStatusReportTitle="DME Daily Health Report";
	private String stationSpecificStatusReportTitle="DME Alarms Report";
	private int dailyStatusRepeatInterval=1440;// In minutes. Defaults 24Hours/1440Minutes
	private String dailyStatusReportTime="00:00";
	private String dailyStatusReportDir=M9kUtils.getReportsDir();
	private boolean enableSerEmail=false;
	private int serRunFrequency=10;// In minutes Default 10 minutes
	private String serEmailFileType="PDF";// PDF or CSV
	private String serEmailReportsDir=M9kUtils.getSEREmailDir();
	private String serEmailAttachementSizeLimit="2";// In MB. Default 2MB
	private boolean enableFaultEmail=false;
	private boolean enableFaultsBooleanLogicFilter=false;
	private boolean enableFaultsWithAttachment=false;
	private String faultEmailAttachementSizeLimit="2";// In MB. Default 2 MB
	private int faultsEmailDailyLimit=10; // Max 10 emails per day limit
	private boolean masterHealthStatusPoll=false;
	private int masterHealthStatusPollingFrequency=30;// in Seconds
	private boolean masterNotificationListener = false;
	private boolean enableConfigChangeEmail = false;

	/**
	 * 
	 */
	public EmailReportsSettingsDTO() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isEnableDailyStatusEmails() {
		return enableDailyStatusEmails;
	}

	public void setEnableDailyStatusEmails(boolean enableDailyStatusEmails) {
		this.enableDailyStatusEmails = enableDailyStatusEmails;
	}

	public String getDailyStatusReportTitle() {
		return dailyStatusReportTitle;
	}

	public void setDailyStatusReportTitle(String dailyStatusReportTitle) {
		this.dailyStatusReportTitle = dailyStatusReportTitle;
	}

	public String getStationSpecificStatusReportTitle() {
		return stationSpecificStatusReportTitle;
	}

	public void setStationSpecificStatusReportTitle(String stationSpecificStatusReportTitle) {
		this.stationSpecificStatusReportTitle = stationSpecificStatusReportTitle;
	}

	public int getDailyStatusRepeatInterval() {
		return dailyStatusRepeatInterval;
	}

	public void setDailyStatusRepeatInterval(int dailyStatusRepeatInterval) {
		this.dailyStatusRepeatInterval = dailyStatusRepeatInterval;
	}

	public String getDailyStatusReportTime() {
		return dailyStatusReportTime;
	}

	public void setDailyStatusReportTime(String dailyStatusReportTime) {
		this.dailyStatusReportTime = dailyStatusReportTime;
	}

	public String getDailyStatusReportDir() {
		return dailyStatusReportDir;
	}

	public void setDailyStatusReportDir(String dailyStatusReportDir) {
		this.dailyStatusReportDir = dailyStatusReportDir;
	}

	public boolean isEnableSerEmail() {
		return enableSerEmail;
	}

	public void setEnableSerEmail(boolean enableSerEmail) {
		this.enableSerEmail = enableSerEmail;
	}

	public int getSerRunFrequency() {
		return serRunFrequency;
	}

	public void setSerRunFrequency(int serRunFrequency) {
		this.serRunFrequency = serRunFrequency;
	}

	public String getSerEmailFileType() {
		return serEmailFileType;
	}

	public void setSerEmailFileType(String serEmailFileType) {
		this.serEmailFileType = serEmailFileType;
	}

	public String getSerEmailReportsDir() {
		return serEmailReportsDir;
	}

	public void setSerEmailReportsDir(String serEmailReportsDir) {
		this.serEmailReportsDir = serEmailReportsDir;
	}

	public String getSerEmailAttachementSizeLimit() {
		return serEmailAttachementSizeLimit;
	}

	public void setSerEmailAttachementSizeLimit(String serEmailAttachementSizeLimit) {
		this.serEmailAttachementSizeLimit = serEmailAttachementSizeLimit;
	}

	public boolean isEnableFaultEmail() {
		return enableFaultEmail;
	}

	public void setEnableFaultEmail(boolean enableFaultEmail) {
		this.enableFaultEmail = enableFaultEmail;
	}

	public boolean isEnableFaultsBooleanLogicFilter() {
		return enableFaultsBooleanLogicFilter;
	}

	public void setEnableFaultsBooleanLogicFilter(boolean enableFaultsBooleanLogicFilter) {
		this.enableFaultsBooleanLogicFilter = enableFaultsBooleanLogicFilter;
	}

	public boolean isEnableFaultsWithAttachment() {
		return enableFaultsWithAttachment;
	}

	public void setEnableFaultsWithAttachment(boolean enableFaultsWithAttachment) {
		this.enableFaultsWithAttachment = enableFaultsWithAttachment;
	}

	public String getFaultEmailAttachementSizeLimit() {
		return faultEmailAttachementSizeLimit;
	}

	public void setFaultEmailAttachementSizeLimit(String faultEmailAttachementSizeLimit) {
		this.faultEmailAttachementSizeLimit = faultEmailAttachementSizeLimit;
	}

	public int getFaultsEmailDailyLimit() {
		return faultsEmailDailyLimit;
	}

	public void setFaultsEmailDailyLimit(int faultsEmailDailyLimit) {
		this.faultsEmailDailyLimit = faultsEmailDailyLimit;
	}

	public boolean isMasterHealthStatusPoll() {
		return masterHealthStatusPoll;
	}

	public void setMasterHealthStatusPoll(boolean masterHealthStatusPoll) {
		this.masterHealthStatusPoll = masterHealthStatusPoll;
	}

	public int getMasterHealthStatusPollingFrequency() {
		return masterHealthStatusPollingFrequency;
	}

	public void setMasterHealthStatusPollingFrequency(int masterHealthStatusPollingFrequency) {
		this.masterHealthStatusPollingFrequency = masterHealthStatusPollingFrequency;
	}

	public boolean isMasterNotificationListener() {
		return masterNotificationListener;
	}

	public void setMasterNotificationListener(boolean masterNotificationListener) {
		this.masterNotificationListener = masterNotificationListener;
	}

	public boolean isEnableConfigChangeEmail() {
		return enableConfigChangeEmail;
	}

	public void setEnableConfigChangeEmail(boolean enableConfigChangeEmail) {
		this.enableConfigChangeEmail = enableConfigChangeEmail;
	}

	

}
