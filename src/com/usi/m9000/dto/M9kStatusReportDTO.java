package com.usi.m9000.dto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.usi.m9000.reports.util.Column;

import net.sf.dynamicreports.report.datasource.DRDataSource;

public class M9kStatusReportDTO {
	private int reportId;
	private String reportTitle;
	private String reportDate;
	private String reportTime;
	private String reportHeader;
	private StringBuffer reportBody = new StringBuffer();;
	private StationDTO stationDetails;
	private String reportStatus;
	private String analogSelfTestStatus;
	private String eventsSelfTestStatus;
	private String reportsDir;
	private String reportFileName = null;
	private String pdfReportFileName = null;
	private List<ReportsDTO> lstStationReportDetails;
	
	// Station specific report details
	private int stationId;
	private String stationName;
	private int analogsCount;
	private int digitalsCount;
	private String substationAddress;
	private String makeModelType;
	private int noOfOfflineWarnings;
	private int noOfCommunicationWarnings;
	private int noOFClockSyncWarnings;
	private int noOfPowerSupplyWarnings;
	private int noOfDiskUsageWarnings;
	private int noOfTempWarnings;
	
	private DRDataSource dailyStatusdataSource;
	private String DailyStatusSummaryText;
	private StationReportDTO stationSpecificDetailsDTO;
	/**
	 * 
	 */
	public M9kStatusReportDTO() {
		dailyStatusdataSource = new DRDataSource("SubStation", "Health Status");
	}
	public int getReportId() {
		return reportId;
	}
	public void setReportId(int reportId) {
		this.reportId = reportId;
	}
	public String getReportDate() {
		return reportDate;
	}
	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}
	public String getReportTime() {
		return reportTime;
	}
	public void setReportTime(String reportTime) {
		this.reportTime = reportTime;
	}
	public StationDTO getStationDetails() {
		return stationDetails;
	}
	public void setStationDetails(StationDTO stationDetails) {
		this.stationDetails = stationDetails;
	}
	public String getReportStatus() {
		return reportStatus;
	}
	public void setReportStatus(String reportStatus) {
		this.reportStatus = reportStatus;
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
	public String getAnalogSelfTestStatus() {
		return analogSelfTestStatus;
	}
	public void setAnalogSelfTestStatus(String analogSelfTestStatus) {
		this.analogSelfTestStatus = analogSelfTestStatus;
	}
	public String getEventsSelfTestStatus() {
		return eventsSelfTestStatus;
	}
	public void setEventsSelfTestStatus(String eventsSelfTestStatus) {
		this.eventsSelfTestStatus = eventsSelfTestStatus;
	}
	public String getReportTitle() {
		return reportTitle;
	}
	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}
	public String getReportHeader() {
		return reportHeader;
	}
	public void setReportHeader(String reportHeader) {
		this.reportHeader = reportHeader;
	}
	public StringBuffer getReportBody() {
		return reportBody;
	}
	public void setReportBody(StringBuffer reportBody) {
		this.reportBody = reportBody;
	}
	public String getReportsDir() {
		File destFile = new File(reportsDir);
		destFile.mkdirs();
		destFile.setWritable(true, false);
		destFile.setExecutable(true,false);
		destFile.setReadable(true, false);

		return reportsDir;
	}
	public void setReportsDir(String reportsDir) {
		this.reportsDir = reportsDir;
	}
	public String createReportFileName(String name) {
		if (reportFileName == null)
		{
			reportFileName=getReportId()+"-"+name+"-"+getReportDate()+"-"+getReportTime().replace(":", "")+".txt";
		}
		return reportFileName;
	}
	public String getReportFileName() {
		return reportFileName;
	}
	public void setReportFileName(String reportFileName) {
		this.reportFileName = reportFileName;
	}
	public List<ReportsDTO> getLstStationReportDetails() {
		return lstStationReportDetails;
	}
	public void setLstStationReportDetails(List<ReportsDTO> lstStationReportDetails) {
		this.lstStationReportDetails = lstStationReportDetails;
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
	public int getAnalogsCount() {
		return analogsCount;
	}
	public void setAnalogsCount(int analogsCount) {
		this.analogsCount = analogsCount;
	}
	public int getDigitalsCount() {
		return digitalsCount;
	}
	public void setDigitalsCount(int digitalsCount) {
		this.digitalsCount = digitalsCount;
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
	/**
	 * @return the dailyStatusdataSource
	 */
	public DRDataSource getDailyStatusdataSource() {
		return dailyStatusdataSource;
	}
	/**
	 * @param dailyStatusdataSource the dailyStatusdataSource to set
	 */
	public void setDailyStatusdataSource(DRDataSource dailyStatusdataSource) {
		this.dailyStatusdataSource = dailyStatusdataSource;
	}
	
	public List<Column> getDailyStatusReportColumns() {
		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column("Substation Name","SubStation","string",100));//dataType = "String", "STRING", "java.lang.String", "text"
		columns.add(new Column("Health Status",    "Health Status",  "string",50));//dataType = "Integer", "INTEGER", "java.lang.Integer"
		return columns;
	}
	
	public String createPDFReportFileName(String name) {
		if (pdfReportFileName == null)
		{
			pdfReportFileName=getReportId()+"-"+name+"-"+getReportDate()+"-"+getReportTime().replace(":", "")+".pdf";
		}
		return pdfReportFileName;
	}
	
	public String getAbsolutePDFReportFileName() {
		return getReportsDir()+pdfReportFileName;
	}
	
	public String getPDFReportFileName() {
		if (pdfReportFileName == null)
		{
			pdfReportFileName=getReportId()+"-"+"daily-status-report"+"-"+getReportDate()+"-"+getReportTime().replace(":", "")+".pdf";
		}
		return pdfReportFileName;
	}
	/**
	 * @return the dailyStatusSummaryText
	 */
	public String getDailyStatusSummaryText() {
		return DailyStatusSummaryText;
	}
	/**
	 * @param dailyStatusSummaryText the dailyStatusSummaryText to set
	 */
	public void setDailyStatusSummaryText(String dailyStatusSummaryText) {
		DailyStatusSummaryText = dailyStatusSummaryText;
	}
	/**
	 * @return the stationSpecificDetailsDTO
	 */
	public StationReportDTO getStationSpecificDetailsDTO() {
		return stationSpecificDetailsDTO;
	}
	/**
	 * @param stationSpecificDetailsDTO the stationSpecificDetailsDTO to set
	 */
	public void setStationSpecificDetailsDTO(
			StationReportDTO stationSpecificDetailsDTO) {
		this.stationSpecificDetailsDTO = stationSpecificDetailsDTO;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "M9kStatusReportDTO [reportId=" + reportId + ", reportTitle="
				+ reportTitle + ", reportDate=" + reportDate + ", reportTime="
				+ reportTime + ", reportHeader=" + reportHeader
				+ ", reportBody=" + reportBody + ", stationDetails="
				+ stationDetails + ", reportStatus=" + reportStatus
				+ ", analogSelfTestStatus=" + analogSelfTestStatus
				+ ", eventsSelfTestStatus=" + eventsSelfTestStatus
				+ ", reportsDir=" + reportsDir + ", reportFileName="
				+ reportFileName + ", pdfReportFileName=" + pdfReportFileName
				+ ", lstStationReportDetails=" + lstStationReportDetails
				+ ", stationId=" + stationId + ", stationName=" + stationName
				+ ", analogsCount=" + analogsCount + ", digitalsCount="
				+ digitalsCount + ", substationAddress=" + substationAddress
				+ ", makeModelType=" + makeModelType + ", noOfOfflineWarnings="
				+ noOfOfflineWarnings + ", noOfCommunicationWarnings="
				+ noOfCommunicationWarnings + ", noOFClockSyncWarnings="
				+ noOFClockSyncWarnings + ", noOfPowerSupplyWarnings="
				+ noOfPowerSupplyWarnings + ", noOfDiskUsageWarnings="
				+ noOfDiskUsageWarnings + ", noOfTempWarnings="
				+ noOfTempWarnings + ", dailyStatusdataSource="
				+ dailyStatusdataSource + ", DailyStatusSummaryText="
				+ DailyStatusSummaryText + ", stationSpecificDetailsDTO="
				+ stationSpecificDetailsDTO + "]";
	}

	
}
