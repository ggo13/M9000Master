package com.usi.m9000.dto;


/**
 * 
 * @author sramasamy
 * Data transfer object representing the database table reports
 */
public class ReportsDTO {
private int stationId;
private String stationName;
private String report_action;
private String report_time;
private String report;
private String status;
private String status_msg;
private String equipmentName;
private String stationNameForReports; 
private String reportFileName;

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
public String getReport_action() {
	return report_action;
}
public void setReport_action(String report_action) {
	this.report_action = report_action;
}
public String getReport_time() {
	return report_time;
}
public void setReport_time(String report_time) {
	this.report_time = report_time;
}
public String getReport() {
	return report;
}
public void setReport(String report) {
	this.report = report;
}
public String getStatus() {
	return status;
}
public void setStatus(String status) {
	this.status = status;
}
public String getStatus_msg() {
//	status_msg=status_msg.replaceAll(M9kConstants.NEWLINE, " ");
//	status_msg=status_msg.replaceAll("\n\r", " ");
//	status_msg=status_msg.replaceAll("\n", " ");
	return status_msg;
}
public void setStatus_msg(String status_msg) {
	this.status_msg = status_msg;
}
public String getEquipmentName() {
	return equipmentName;
}
public void setEquipmentName(String equipmentName) {
	this.equipmentName = equipmentName;
}
public String getStationNameForReports() {
	return stationNameForReports;
}
public void setStationNameForReports(String stationNameForReports) {
	this.stationNameForReports = stationNameForReports;
}
public String getReportFileName() {
	return reportFileName;
}
public void setReportFileName(String reportFileName) {
	this.reportFileName = reportFileName;
}

}
