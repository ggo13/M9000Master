package com.usi.m9000.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.usi.m9000.util.M9kConstants;


public class StationDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int nextAvailableTriggerId = 0;
	private static int nextAvailableExportId = 0;
	private Integer id;
	// System details of the config XML
	private Integer systemAnalogChannelsCount; 
	private Integer systemDigitalChannelsCount; 
	private Integer systemSampleRate;
	private Integer systemLongTermSampleRate;
	private Integer systemPrefaultTime;
	private Integer systemPostfaultTime;
	private Integer systemLtrPrefaultTime;
	private Integer systemLtrPostfaultTime;
	private Integer systemStationId;
	private String systemStationName; 
	private String systemRecordingDeviceId; // Default value 
//	private String systemRecordingDeviceName; 
	private Integer systemLineFrequency; 
	private String ps;
	private Integer totalDfrsConfigured;
	private Integer totalTriggersConfigured;
	private Integer chatterLimit;
	private Double chatterRate;
	private Integer triggerLimit;
	private String stationDisplayName;
	private String ipAddress;
	private String configXml;
	private String configStatus;
	private Integer exportRate; // Global export rate
	private Double signalTooLowFactor = 0.0; // Percentage of full-scale signal required to enable phase and frequency measurements
	private Double eventDebounce; // Time in ms to check for event debounce
	private Integer wettingVoltageMonitor = 0; // Configurable wetting voltage monitoring Yes = 1, No = 0 
	// PMU details
	private Integer pmuId;
	private Integer pmuPort;
	private Integer pmuUdpPort;
	private String pmuStreamType;
	private String pmuPhasorMode;
	private Integer pmuDataRate;
	private String pmuStatus;
	private boolean pmuEnabled;
	private Integer commandServerPort;
	// PDC details
	private Integer pdcId;
	private Integer pdcTcpPort;
	private Integer pdcMaxWait;
	private String pdcStreamType;
	private List<Integer> lstOfUDPserverPorts;

	// System details of the config XML
	private String created;
	
	// 05-Apr-2021 - Virtual measurements count
	private Integer totalVirtualMeasurementsConfigured;
	private static int nextAvailableVirtualMeasurementId = M9kConstants.VIRTUAL_MEASUREMENT_OFFSET;
	
	private Integer totalLineGroupsConfigured;
	private int nextAvailableLineGroupId;
	
	// 20-May-2021 - Add or Delete chassis implementation
	private boolean dfrAddedOrRemoved = false; // If a chassis is deleted or added

	// 02-Nov-2021 - Hierarchy implementation
	// Define zone OTHER with id 9999 as default 
	private int parentZoneId=9999;
	
	private List<String> lstAlarmRelaysMapping;
	private int triggerDuration = 10; // In Seconds. Default set the trigger relays for 10 seconds.
	
	// START: 31-Aug-2022 - Moving number of days to retain for cont data to GUI 
	private int contOscDaysToRetain = 5;
	private int contMeasurementsDaysToRetain = 30;
	// END: 31-Aug-2022
	// START: 09-Jan-2023 - Export time limit for continuous data is made configurable
	private int contOscExportTimeLimit = 120;
	private int contMeasurementsExportTimeLimit = 600;
	// END: 09-Jan-2023

	// START: 11-Jun-2024 - Enable or Disable event test for incompatible old boards
	private boolean enableEventTest = true;
	// End: 11-Jun-2024
	
	// START: 18-Jun-2024 - Set DDR deck time in seconds to be configurable
	private int ddrDeckTimeLimit = 300; // In Seconds
	// End: 18-Jun-2024
	public StationDTO()
	{
		lstOfUDPserverPorts = new ArrayList<Integer>();
	}

	
	
	public static Integer getNextAvailableTriggerId()
	{
		return ++nextAvailableTriggerId;
	}
	
	public static Integer getNextAvailableExportId()
	{
		return ++nextAvailableExportId;
	}
	
	public static void initializeExportId(int exportId)
	{
		nextAvailableExportId = exportId;
	}
	public static void decrementExportId()
	{
		nextAvailableExportId--;
	}
	
	public static void decrementTriggerId()
	{
		nextAvailableTriggerId--;
	}

	/**
	 * @return the systemSampleRate
	 */
	public Integer getSystemSampleRate() {
		return systemSampleRate;
	}
	/**
	 * @param systemSampleRate the systemSampleRate to set
	 */
	public void setSystemSampleRate(Integer systemSampleRate) {
		this.systemSampleRate = systemSampleRate;
	}
	/**
	 * @return the systemPrefaultTime
	 */
	public Integer getSystemPrefaultTime() {
		return systemPrefaultTime;
	}
	/**
	 * @param systemPrefaultTime the systemPrefaultTime to set
	 */
	public void setSystemPrefaultTime(Integer systemPrefaultTime) {
		this.systemPrefaultTime = systemPrefaultTime;
	}
	/**
	 * @return the systemPostfaultTime
	 */
	public Integer getSystemPostfaultTime() {
		return systemPostfaultTime;
	}
	/**
	 * @param systemPostfaultTime the systemPostfaultTime to set
	 */
	public void setSystemPostfaultTime(Integer systemPostfaultTime) {
		this.systemPostfaultTime = systemPostfaultTime;
	}
	public Integer getSystemLtrPrefaultTime() {
		return systemLtrPrefaultTime;
	}



	public void setSystemLtrPrefaultTime(Integer systemLtrPrefaultTime) {
		this.systemLtrPrefaultTime = systemLtrPrefaultTime;
	}



	public Integer getSystemLtrPostfaultTime() {
		return systemLtrPostfaultTime;
	}



	public void setSystemLtrPostfaultTime(Integer systemLtrPostfaultTime) {
		this.systemLtrPostfaultTime = systemLtrPostfaultTime;
	}



	/**
	 * @return the systemStationName
	 */
	public String getSystemStationName() {
		return systemStationName;
	}
	/**
	 * @param systemStationName the systemStationName to set
	 */
	public void setSystemStationName(String systemStationName) {
		this.systemStationName = systemStationName;
	}
	/**
	 * @return the systemRecordingDeviceId
	 */
	public String getSystemRecordingDeviceId() {
		return systemRecordingDeviceId;
	}
	/**
	 * @param systemRecordingDeviceId the systemRecordingDeviceId to set
	 */
	public void setSystemRecordingDeviceId(String systemRecordingDeviceId) {
		this.systemRecordingDeviceId = systemRecordingDeviceId;
	}
	/**
	 * @return the systemLineFrequency
	 */
	public Integer getSystemLineFrequency() {
		return systemLineFrequency;
	}
	/**
	 * @param systemLineFrequency the systemLineFrequency to set
	 */
	public void setSystemLineFrequency(Integer systemLineFrequency) {
		this.systemLineFrequency = systemLineFrequency;
	}
	/**
	 * @return the ps
	 */
	public String getPs() {
		return ps;
	}
	/**
	 * @param ps the ps to set
	 */
	public void setPs(String ps) {
		this.ps = ps;
	}
	/**
	 * @return the systemAnalogChannelsCount
	 */
	public Integer getSystemAnalogChannelsCount() {
		return systemAnalogChannelsCount;
	}
	/**
	 * @param systemAnalogChannelsCount the systemAnalogChannelsCount to set
	 */
	public void setSystemAnalogChannelsCount(Integer systemAnalogChannelsCount) {
		this.systemAnalogChannelsCount = systemAnalogChannelsCount;
	}
	/**
	 * @return the systemDigitalChannelsCount
	 */
	public Integer getSystemDigitalChannelsCount() {
		return systemDigitalChannelsCount;
	}
	/**
	 * @param systemDigitalChannelsCount the systemDigitalChannelsCount to set
	 */
	public void setSystemDigitalChannelsCount(Integer systemDigitalChannelsCount) {
		this.systemDigitalChannelsCount = systemDigitalChannelsCount;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the totalDfrsConfigured
	 */
	public Integer getTotalDfrsConfigured() {
		return totalDfrsConfigured;
	}

	/**
	 * @param totalDfrsConfigured the totalDfrsConfigured to set
	 */
	public void setTotalDfrsConfigured(Integer totalDfrsConfigured) {
		this.totalDfrsConfigured = totalDfrsConfigured;
	}

	/**
	 * @return the totalTriggersConfigured
	 */
	public Integer getTotalTriggersConfigured() {
		return totalTriggersConfigured;
	}

	/**
	 * @param totalTriggersConfigured the totalTriggersConfigured to set
	 */
	public void setTotalTriggersConfigured(Integer totalTriggersConfigured) {
		this.totalTriggersConfigured = totalTriggersConfigured;
		nextAvailableTriggerId = totalTriggersConfigured;
	}

	/**
	 * @return the chatterLimit
	 */
	public Integer getChatterLimit() {
		return chatterLimit;
	}

	/**
	 * @param chatterLimit the chatterLimit to set
	 */
	public void setChatterLimit(Integer chatterLimit) {
		this.chatterLimit = chatterLimit;
	}

	/**
	 * @return the chatterRate
	 */
	public Double getChatterRate() {
		return chatterRate;
	}

	/**
	 * @param chatterRate the chatterRate to set
	 */
	public void setChatterRate(Double chatterRate) {
		this.chatterRate = chatterRate;
	}

	/**
	 * @return the triggerLimit
	 */
	public Integer getTriggerLimit() {
		return triggerLimit;
	}

	/**
	 * @param triggerLimit the triggerLimit to set
	 */
	public void setTriggerLimit(Integer triggerLimit) {
		this.triggerLimit = triggerLimit;
	}

	/**
	 * @return the systemStationId
	 */
	public Integer getSystemStationId() {
		return systemStationId;
	}

	/**
	 * @param systemStationId the systemStationId to set
	 */
	public void setSystemStationId(Integer systemStationId) {
		this.systemStationId = systemStationId;
	}

	/**
	 * @return the stationDisplayName
	 */
	public String getStationDisplayName() {
		stationDisplayName = getSystemStationId()+"-"+getSystemStationName();
		return stationDisplayName;
	}

	/**
	 * @param stationDisplayName the stationDisplayName to set
	 */
	public void setStationDisplayName(String stationDisplayName) {
		this.stationDisplayName = stationDisplayName;
	}

	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the configXml
	 */
	public String getConfigXml() {
		// START: 10-APR-2020 - Escaping single quotes for export config functionality
		// 10-Sept-2020 - Commented again so that it stays exclusively for export functionality
//		if (configXml != null && !configXml.isEmpty())
//		{
//			configXml = configXml.replace("'", "\\'");
//		}
		// END: 10-SEP-2020
		return configXml;
	}

	/**
	 * @param configXml the configXml to set
	 */
	public void setConfigXml(String configXml) {
		this.configXml = configXml;
	}

//	/**
//	 * @return the systemRecordingDeviceName
//	 */
//	public String getSystemRecordingDeviceName() {
//		return systemRecordingDeviceName;
//	}
//
//	/**
//	 * @param systemRecordingDeviceName the systemRecordingDeviceName to set
//	 */
//	public void setSystemRecordingDeviceName(String systemRecordingDeviceName) {
//		this.systemRecordingDeviceName = systemRecordingDeviceName;
//	}

	/**
	 * @return the systemLongTermSampleRate
	 */
	public Integer getSystemLongTermSampleRate() {
		return systemLongTermSampleRate;
	}

	/**
	 * @param systemLongTermSampleRate the systemLongTermSampleRate to set
	 */
	public void setSystemLongTermSampleRate(Integer systemLongTermSampleRate) {
		this.systemLongTermSampleRate = systemLongTermSampleRate;
	}

	/**
	 * @return the configStatus
	 */
	public String getConfigStatus() {
		return configStatus;
	}

	/**
	 * @param configStatus the configStatus to set
	 */
	public void setConfigStatus(String configStatus) {
		this.configStatus = configStatus;
	}

	public Integer getPmuId() {
		return pmuId;
	}



	public void setPmuId(Integer pmuId) {
		this.pmuId = pmuId;
	}



	public Integer getPmuPort() {
		return pmuPort;
	}



	public void setPmuPort(Integer pmuPort) {
		this.pmuPort = pmuPort;
	}



	public String getPmuPhasorMode() {
		return pmuPhasorMode;
	}



	public void setPmuPhasorMode(String pmuPhasorMode) {
		this.pmuPhasorMode = pmuPhasorMode;
	}



	public Integer getPmuDataRate() {
		return pmuDataRate;
	}



	public void setPmuDataRate(Integer pmuDataRate) {
		this.pmuDataRate = pmuDataRate;
	}


	public String getPmuStatus() {
		return pmuStatus;
	}



	public void setPmuStatus(String pmuStatus) {
		this.pmuStatus = pmuStatus;
	}



	public Integer getExportRate() {
		return exportRate;
	}



	public void setExportRate(Integer exportRate) {
		this.exportRate = exportRate;
	}



	public Integer getPmuUdpPort() {
		return pmuUdpPort;
	}



	public void setPmuUdpPort(Integer pmuUdpPort) {
		this.pmuUdpPort = pmuUdpPort;
	}



	public String getPmuStreamType() {
		return pmuStreamType;
	}



	public void setPmuStreamType(String pmuStreamType) {
		this.pmuStreamType = pmuStreamType;
	}



	@Override
	public String toString() {
		return "StationDTO [id=" + id + ", systemAnalogChannelsCount="
				+ systemAnalogChannelsCount + ", systemDigitalChannelsCount="
				+ systemDigitalChannelsCount +", ParentZoneId="+parentZoneId+ ", systemSampleRate="
				+ systemSampleRate + ", systemLongTermSampleRate="
				+ systemLongTermSampleRate + ", systemPrefaultTime="
				+ systemPrefaultTime + ", systemPostfaultTime="
				+ systemPostfaultTime + ", systemLtrPrefaultTime="
				+ systemLtrPrefaultTime + ", systemLtrPostfaultTime="
				+ systemLtrPostfaultTime + ", systemStationId="
				+ systemStationId + ", systemStationName=" + systemStationName
				+ ", systemRecordingDeviceId=" + systemRecordingDeviceId
				+ ", systemLineFrequency=" + systemLineFrequency + ", ps=" + ps
				+ ", totalDfrsConfigured=" + totalDfrsConfigured
				+ ", totalTriggersConfigured=" + totalTriggersConfigured
				+ ", totalLineGroupsConfigured=" + totalLineGroupsConfigured
				+ ", chatterLimit=" + chatterLimit + ", chatterRate="
				+ chatterRate + ", triggerLimit=" + triggerLimit
				+ ", stationDisplayName=" + stationDisplayName + ", configXml="
				+ configXml + ", configStatus=" + configStatus
				+ ", exportRate=" + exportRate + ", pmuId=" + pmuId
				+ ", pmuPort=" + pmuPort + ", pmuUdpPort=" + pmuUdpPort
				+ ", pmuStreamType=" + pmuStreamType + ", pmuPhasorMode="
				+ pmuPhasorMode + ", pmuDataRate=" + pmuDataRate
				+ ", pmuStatus=" + pmuStatus + "]";
	}



	public Integer getPdcTcpPort() {
		return pdcTcpPort;
	}



	public void setPdcTcpPort(Integer pdcTcpPort) {
		this.pdcTcpPort = pdcTcpPort;
	}



	public Integer getPdcMaxWait() {
		return pdcMaxWait;
	}



	public void setPdcMaxWait(Integer pdcMaxWait) {
		this.pdcMaxWait = pdcMaxWait;
	}



	public String getPdcStreamType() {
		return pdcStreamType;
	}



	public void setPdcStreamType(String pdcStreamType) {
		this.pdcStreamType = pdcStreamType;
	}


	public List<Integer> getLstOfUDPserverPorts() {
		return lstOfUDPserverPorts;
	}



	public void setLstOfUDPserverPorts(List<Integer> lstOfUDPserverPorts) {
		this.lstOfUDPserverPorts = lstOfUDPserverPorts;
	}



	public boolean isPmuEnabled() {
		return pmuEnabled;
	}



	public void setPmuEnabled(boolean pmuEnabled) {
		this.pmuEnabled = pmuEnabled;
	}



	/**
	 * 16-Nov-2022 - read PDC port from properties files
	 * @return
	 */
	public Integer getCommandServerPort() {
//		if (commandServerPort == null)
//		{
		PropertiesConfiguration pmuConfig;
		 try {
				pmuConfig =  new PropertiesConfiguration("/m9k-master.properties");
				commandServerPort = pmuConfig.getInt("command-server-port", 9979);
			} catch (ConfigurationException e) {
				commandServerPort = 9979;
			}
//		}
		return commandServerPort;
	}



	public void setCommandServerPort(Integer commandServerPort) {
		this.commandServerPort = commandServerPort;
	}
	
	public String getStationDetailsAsSQLStmt()
	{
		StringBuffer sqlStmtBuffer = new StringBuffer();
		sqlStmtBuffer.append("INSERT INTO station_details (stationId,name,recordingDevId,analogs_count,digitals_count,preFaultTime,postFaultTime,");
		sqlStmtBuffer.append("ltrPreFaultTime,ltrPostFaultTime,lineFreq,sampleRate,longTermSampleRate,configXml,status,created) VALUES (");
		sqlStmtBuffer.append(getSystemStationId());
		sqlStmtBuffer.append(",\'");
		sqlStmtBuffer.append(getSystemStationName());
		sqlStmtBuffer.append("\',\'");
		sqlStmtBuffer.append(getSystemRecordingDeviceId());
		sqlStmtBuffer.append("\',");
		sqlStmtBuffer.append(getSystemAnalogChannelsCount());
		sqlStmtBuffer.append(",");
		sqlStmtBuffer.append(getSystemDigitalChannelsCount());
		sqlStmtBuffer.append(",");
		sqlStmtBuffer.append(getSystemPrefaultTime());
		sqlStmtBuffer.append(",");
		sqlStmtBuffer.append(getSystemPostfaultTime());
		sqlStmtBuffer.append(",");
		sqlStmtBuffer.append(getSystemLtrPrefaultTime());
		sqlStmtBuffer.append(",");
		sqlStmtBuffer.append(getSystemLtrPostfaultTime());
		sqlStmtBuffer.append(",");
		sqlStmtBuffer.append(getSystemLineFrequency());
		sqlStmtBuffer.append(",");
		sqlStmtBuffer.append(getSystemSampleRate());
		sqlStmtBuffer.append(",");
		sqlStmtBuffer.append(getSystemLongTermSampleRate());
		sqlStmtBuffer.append(",\'");
		// START: 10-SEP-2020 - Export config as sql should escape single quotes as it impacts import functionality
		if (getConfigXml() == null)
		{
			sqlStmtBuffer.append(getConfigXml());
		}
		else
		{
			sqlStmtBuffer.append(getConfigXml().replace("'", "\\'"));
		}
		// END: 10-SEP-2020
		sqlStmtBuffer.append("\',\'");
		sqlStmtBuffer.append(getConfigStatus());
		sqlStmtBuffer.append("\','");
		sqlStmtBuffer.append(getCreated());
		sqlStmtBuffer.append("');");

		return sqlStmtBuffer.toString();
	}



	/**
	 * @return the created
	 */
	public String getCreated() {
		return created;
	}



	/**
	 * @param created the created to set
	 */
	public void setCreated(String created) {
		this.created = created;
	}



	/**
	 * @return the pdcId
	 */
	public Integer getPdcId() {
		return pdcId;
	}



	/**
	 * @param pdcId the pdcId to set
	 */
	public void setPdcId(Integer pdcId) {
		this.pdcId = pdcId;
	}



	/**
	 * @return the signalTooLowFactor
	 */
	public double getSignalTooLowFactor() {
//		System.out.println("is it here????? "+signalTooLowFactor);
		return signalTooLowFactor;
	}



	/**
	 * @param signalTooLowFactor the signalTooLowFactor to set
	 */
	public void setSignalTooLowFactor(double signalTooLowFactor) {
		this.signalTooLowFactor = signalTooLowFactor;
	}



	/**
	 * @return the eventDebounce
	 */
	public Double getEventDebounce() {
		return eventDebounce;
	}



	/**
	 * @param eventDebounce the eventDebounce to set
	 */
	public void setEventDebounce(Double eventDebounce) {
		this.eventDebounce = eventDebounce;
	}



	public Integer getWettingVoltageMonitor() {
		return wettingVoltageMonitor;
	}



	public void setWettingVoltageMonitor(Integer wettingVoltageMonitor) {
		this.wettingVoltageMonitor = wettingVoltageMonitor;
	}

	/**
	 * @return the folder name created in the data directory
	 */
	public String getStationFolderName() {
		return "R"+String.format("%02d", getSystemStationId())+"-"+getSystemStationName();
	}



	/**
	 * @return the totalVirtualMeasurementsConfigured
	 */
	public Integer getTotalVirtualMeasurementsConfigured() {
		return totalVirtualMeasurementsConfigured;
	}



	/**
	 * @param totalVirtualMeasurementsConfigured the totalVirtualMeasurementsConfigured to set
	 */
	public void setTotalVirtualMeasurementsConfigured(Integer totalVirtualMeasurementsConfigured) {
		this.totalVirtualMeasurementsConfigured = totalVirtualMeasurementsConfigured;
		nextAvailableVirtualMeasurementId = totalVirtualMeasurementsConfigured;
	}



	/**
	 * @return the nextAvailableVirtualMeasurementId
	 */
	public static int getNextAvailableVirtualMeasurementId() {
		return ++nextAvailableVirtualMeasurementId;
	}

	/**
	 * Returns the current virtual measuremet id
	 * @return
	 */
	public static int getCurrentVirtualMeasurementId() {
		return nextAvailableVirtualMeasurementId;
	}


	/**
	 * @param nextAvailableVirtualMeasurementId the nextAvailableVirtualMeasurementId to set
	 */
	public static void setNextAvailableVirtualMeasurementId(int nextAvailableVirtualMeasurementId) {
		StationDTO.nextAvailableVirtualMeasurementId = nextAvailableVirtualMeasurementId;
	}

	public static void decrementVirtualMeasurementId()
	{
		--nextAvailableVirtualMeasurementId;
	}



	/**
	 * @return the totalLineGroupsConfigured
	 */
	public Integer getTotalLineGroupsConfigured() {
		return totalLineGroupsConfigured;
	}



	/**
	 * @param totalLineGroupsConfigured the totalLineGroupsConfigured to set
	 */
	public void setTotalLineGroupsConfigured(Integer totalLineGroupsConfigured) {
		this.totalLineGroupsConfigured = totalLineGroupsConfigured;
		nextAvailableLineGroupId = totalLineGroupsConfigured;
	}
	
	public int getNextAvailableLineGroupId()
	{
		return ++nextAvailableLineGroupId;
	}
	
	public void decrementLineGroupId()
	{
		--nextAvailableLineGroupId;
	}
	public int getCurrentLineGroupId() {
		return nextAvailableLineGroupId;
	}



	/**
	 * @return the dfrAddedOrRemoved
	 */
	public boolean isDfrAddedOrRemoved() {
		return dfrAddedOrRemoved;
	}



	/**
	 * @param dfrAddedOrRemoved the dfrAddedOrRemoved to set
	 */
	public void setDfrAddedOrRemoved(boolean dfrAddedOrRemoved) {
		this.dfrAddedOrRemoved = dfrAddedOrRemoved;
	}



	public int getParentZoneId() {
		return parentZoneId;
	}



	public void setParentZoneId(int parentZoneId) {
		this.parentZoneId = parentZoneId;
	}



	public List<String> getLstAlarmRelaysMapping() {
		return lstAlarmRelaysMapping;
	}



	public void setLstAlarmRelaysMapping(List<String> lstAlarmRelaysMapping) {
		this.lstAlarmRelaysMapping = lstAlarmRelaysMapping;
	}



	public int getTriggerDuration() {
		return triggerDuration;
	}



	public void setTriggerDuration(int triggerDuration) {
		this.triggerDuration = triggerDuration;
	}



	public int getContOscDaysToRetain() {
		return contOscDaysToRetain;
	}



	public void setContOscDaysToRetain(int contOscDaysToRetain) {
		this.contOscDaysToRetain = contOscDaysToRetain;
	}



	public int getContMeasurementsDaysToRetain() {
		return contMeasurementsDaysToRetain;
	}



	public void setContMeasurementsDaysToRetain(int contMeasurementsDaysToRetain) {
		this.contMeasurementsDaysToRetain = contMeasurementsDaysToRetain;
	}



	/**
	 * @return the contOscExportTimeLimit
	 */
	public int getContOscExportTimeLimit() {
		return contOscExportTimeLimit;
	}



	/**
	 * @param contOscExportTimeLimit the contOscExportTimeLimit to set
	 */
	public void setContOscExportTimeLimit(int contOscExportTimeLimit) {
		this.contOscExportTimeLimit = contOscExportTimeLimit;
	}



	/**
	 * @return the contMeasurementsExportTimeLimit
	 */
	public int getContMeasurementsExportTimeLimit() {
		return contMeasurementsExportTimeLimit;
	}



	/**
	 * @param contMeasurementsExportTimeLimit the contMeasurementsExportTimeLimit to set
	 */
	public void setContMeasurementsExportTimeLimit(int contMeasurementsExportTimeLimit) {
		this.contMeasurementsExportTimeLimit = contMeasurementsExportTimeLimit;
	}



	public boolean isEnableEventTest() {
		return enableEventTest;
	}



	public void setEnableEventTest(boolean enableEventTest) {
		this.enableEventTest = enableEventTest;
	}



	public int getDdrDeckTimeLimit() {
		return ddrDeckTimeLimit;
	}



	public void setDdrDeckTimeLimit(int ddrDeckTimeLimit) {
		this.ddrDeckTimeLimit = ddrDeckTimeLimit;
	}
	
}
