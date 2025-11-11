package com.usi.m9000.dto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.usi.m9000.reports.util.Column;
import com.usi.m9000.station.util.LedName;
import com.usi.m9000.util.M9kConstants;

import net.sf.dynamicreports.report.datasource.DRDataSource;

public class StationReportDTO {
	private int stationId;
	private String stationName;
	private String stationKey;
	private int analogCount;
	private int digitalCount;
	private String substationAddress;
	private String makeModelType;
	private int noOfOfflineWarnings = 0;
	private int noOfCommunicationWarnings = 0;
	private int noOFClockSyncWarnings = 0;
	private int noOfPowerSupplyWarnings = 0;
	private int noOfDiskUsageWarnings = 0;
	private int noOfTempWarnings = 0;
	List<AlarmLogsDTO> lstOfStationAlarms;
	private StringBuffer analogSelfTestDetails;
	private StringBuffer digitalsSelfTestDetails;
	
	public StationReportDTO()
	{
		analogSelfTestDetails = new StringBuffer();
		digitalsSelfTestDetails = new StringBuffer();
	}
	public int getStationId() {
		return stationId;
	}
	public void setStationId(int stationId) {
		this.stationId = stationId;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public int getAnalogCount() {
		return analogCount;
	}
	public void setAnalogCount(int analogCount) {
		this.analogCount = analogCount;
	}
	public int getDigitalCount() {
		return digitalCount;
	}
	public void setDigitalCount(int digitalCount) {
		this.digitalCount = digitalCount;
	}
	public String getSubstationAddress() {
		return substationAddress;
	}
	public void setSubstationAddress(String substationAddress) {
		this.substationAddress = substationAddress;
	}
	public String getMakeModelType() {
		return makeModelType;
	}
	public void setMakeModelType(String makeModelType) {
		this.makeModelType = makeModelType;
	}
	public int getNoOfOfflineWarnings() {
		return noOfOfflineWarnings;
	}
	public void setNoOfOfflineWarnings(int noOfOfflineWarnings) {
		this.noOfOfflineWarnings = noOfOfflineWarnings;
	}
	public int getNoOfCommunicationWarnings() {
		return noOfCommunicationWarnings;
	}
	public void setNoOfCommunicationWarnings(int noOfCommunicationWarnings) {
		this.noOfCommunicationWarnings = noOfCommunicationWarnings;
	}
	public int getNoOFClockSyncWarnings() {
		return noOFClockSyncWarnings;
	}
	public void setNoOFClockSyncWarnings(int noOFClockSyncWarnings) {
		this.noOFClockSyncWarnings = noOFClockSyncWarnings;
	}
	public int getNoOfPowerSupplyWarnings() {
		return noOfPowerSupplyWarnings;
	}
	public void setNoOfPowerSupplyWarnings(int noOfPowerSupplyWarnings) {
		this.noOfPowerSupplyWarnings = noOfPowerSupplyWarnings;
	}
	public int getNoOfDiskUsageWarnings() {
		return noOfDiskUsageWarnings;
	}
	public void setNoOfDiskUsageWarnings(int noOfDiskUsageWarnings) {
		this.noOfDiskUsageWarnings = noOfDiskUsageWarnings;
	}
	public int getNoOfTempWarnings() {
		return noOfTempWarnings;
	}
	public void setNoOfTempWarnings(int noOfTempWarnings) {
		this.noOfTempWarnings = noOfTempWarnings;
	}
	public List<AlarmLogsDTO> getLstOfStationAlarms() {
		return lstOfStationAlarms;
	}
	public void setLstOfStationAlarms(List<AlarmLogsDTO> lstOfStationAlarms) {
		this.lstOfStationAlarms = lstOfStationAlarms;
		if (lstOfStationAlarms != null && !lstOfStationAlarms.isEmpty())
		{
			AlarmLogsDTO alarmLogsDTO;
			for (Iterator<AlarmLogsDTO> iterator = lstOfStationAlarms.iterator(); iterator
					.hasNext();) {
				alarmLogsDTO = iterator.next();
				incrementWarningsCount(alarmLogsDTO.getLedName());
			}
		}
	}
	public String getStationKey() {
		if (stationKey == null)
		{
			stationKey = getStationId()+"-"+getStationName();
		}
		return stationKey;
	}
	public void setStationKey(String stationKey) {
		this.stationKey = stationKey;
	}
	
	// Based on the led name it increments
	public void incrementWarningsCount(String ledName)
	{
		LedName action = LedName.valueOf(ledName);
		switch(action)
		{
		case ONLINE:
			noOfOfflineWarnings++;
			break;
		case COMMUNICATION:
			noOfCommunicationWarnings++;
			break;
		case CLOCK_SYNC:
			noOFClockSyncWarnings++;
			break;
		case DISK:
			noOfDiskUsageWarnings++;
			break;
		case POWER:
		case WETTING_VOLTAGE:
			noOfPowerSupplyWarnings++;
			break;
		case TEMPERATURE:
			noOfPowerSupplyWarnings++;
			break;
		case STATION_HEALTH:
			noOfCommunicationWarnings++;
			break;
		case DISTURBANCE:
			break;
		case TRIGGER:
			break;
		default:
			break;
		}
	}

	public int getWarningCount(LedName action)
	{
		int count = 0;
		switch(action)
		{
		case ONLINE:
			count = noOfOfflineWarnings;
			break;
		case COMMUNICATION:
			count = noOfCommunicationWarnings;
			break;
		case CLOCK_SYNC:
			count = noOFClockSyncWarnings;
			break;
		case DISK:
			count = noOfDiskUsageWarnings;
			break;
		case POWER:
			count = noOfPowerSupplyWarnings;
			break;
		case TEMPERATURE:
			count = noOfPowerSupplyWarnings;
			break;
		case DISTURBANCE:
			break;
		case STATION_HEALTH:
			break;
		case TRIGGER:
			break;
		default:
			break;
		}
		return count;
	}
	
	public String getWarningSummary()
	{
		StringBuffer strWarningSummary = new StringBuffer();
		strWarningSummary.append(String.format("%1$47s %2$21s%n", "Alarms Summary ","Results"));
		strWarningSummary.append(String.format("%1$50s %2$15d%n", "Offline ",noOfOfflineWarnings)); // 23
		strWarningSummary.append(String.format("%1$56s %2$9d%n", "Communication ",noOfCommunicationWarnings)); //29
		strWarningSummary.append(String.format("%1$53s %2$12d%n", "Clock sync ",noOFClockSyncWarnings)); // 26
		strWarningSummary.append(String.format("%1$61s %2$4d%n", "Power Supply check ",noOfPowerSupplyWarnings)); // 34
		strWarningSummary.append(String.format("%1$53s %2$12d%n", "Disk usage ",noOfDiskUsageWarnings)); // 26
		strWarningSummary.append(String.format("%1$54s %2$11d%n", "Temperature ",noOfTempWarnings)); // 27
		if (getAnalogCount() > 0)
		{
			strWarningSummary.append(String.format("%1$54s %2$12s%n", "Analogs Channels Self Test ",getAnalogsSelfTestResult())); // 27
		}
		if (getDigitalCount() > 0)
		{
			strWarningSummary.append(String.format("%1$55s %2$11s%n", "Digitals Channels Self Test ",getDigitalsSelfTestResult())); // 28
		}
		return strWarningSummary.toString();
	}

	public DRDataSource getWarningSummaryDataSource()
	{
		DRDataSource summaryDataSource = null;
		summaryDataSource = new DRDataSource("Alarms Summary","Results");
		summaryDataSource.add("Offline ",""+noOfOfflineWarnings);
		summaryDataSource.add("Communication ",""+noOfCommunicationWarnings);
		summaryDataSource.add("Clock sync ",""+noOFClockSyncWarnings);
		summaryDataSource.add("Power Supply check ",""+noOfPowerSupplyWarnings);
		summaryDataSource.add("Disk usage ",""+noOfDiskUsageWarnings);
		summaryDataSource.add("Temperature ",""+noOfTempWarnings);
		if (getAnalogCount() > 0)
		{
			summaryDataSource.add("Analogs Channels Self Test ",getAnalogsSelfTestResult());
		}
		if (getDigitalCount() > 0)
		{
			summaryDataSource.add("Digitals Channels Self Test ",getDigitalsSelfTestResult());
		}
		
		return summaryDataSource;
	}

	public List<Column> getWarningSummaryColumns() {
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column("Alarms Summary","Alarms Summary","string",150));//dataType = "String", "STRING", "java.lang.String", "text"
		columns.add(new Column("Results",    "Results",  "string",50));//dataType = "Integer", "INTEGER", "java.lang.Integer"
		return columns;
	}
	
	public List<Column> getStationSpecificDetailsColumn() {
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column("Date-Time","Date-Time","string",110));//dataType = "String", "STRING", "java.lang.String", "text"
		columns.add(new Column("Action",    "Action",  "String",85));//dataType = "Integer", "INTEGER", "java.lang.Integer"
//		columns.add(new Column("Status",  "Status", "string",50));
		columns.add(new Column("Description",  "Description", "string",150));//dataType = "DateYear", "dateyear", "DATEYEAR"
		columns.add(new Column("Current Status",  "CurrentStatus", "string",70));//dataType = "DateYear", "dateyear", "DATEYEAR"
		return columns;
	}
	
	public DRDataSource getStationSpecificDetailsDataSource()
	{
		DRDataSource stationSpecificDetailsDataSource = null;
		stationSpecificDetailsDataSource = new DRDataSource("Date-Time", "Action",  "Description","CurrentStatus");
		AlarmLogsDTO alarmsLogDto;
		if (getLstOfStationAlarms() != null && !getLstOfStationAlarms().isEmpty())
		{
			for (Iterator<AlarmLogsDTO> iterator = getLstOfStationAlarms().iterator(); iterator.hasNext();) {
				alarmsLogDto = iterator.next();
				stationSpecificDetailsDataSource.add(alarmsLogDto.getAlarmTime(), alarmsLogDto.getLedName(), alarmsLogDto.getAlarmDescription().toString(),alarmsLogDto.getCurrentStatus());
			}
		}
		return stationSpecificDetailsDataSource;
	}

	
	private String getAnalogsSelfTestResult() {
		String result;
		if (getAnalogSelfTestDetails().length() == 0)
		{
			result = M9kConstants.PASS;
		}
		else
		{
			result = M9kConstants.FAIL;
		}

		return result;
	}
	
	private String getDigitalsSelfTestResult() {
		String result;
		if (getDigitalsSelfTestDetails().length() == 0)
		{
			result = M9kConstants.PASS;
		}
		else
		{
			result = M9kConstants.FAIL;
		}

		return result;
	}

	public StringBuffer getAnalogSelfTestDetails() {
		return analogSelfTestDetails;
	}
	public void setAnalogSelfTestDetails(StringBuffer analogSelfTestDetails) {
		this.analogSelfTestDetails = analogSelfTestDetails;
	}
	public StringBuffer getDigitalsSelfTestDetails() {
		return digitalsSelfTestDetails;
	}
	public void setDigitalsSelfTestDetails(StringBuffer digitalsSelfTestDetails) {
		this.digitalsSelfTestDetails = digitalsSelfTestDetails;
	}
}
