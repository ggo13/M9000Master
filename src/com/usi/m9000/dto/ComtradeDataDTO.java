package com.usi.m9000.dto;

import java.io.InputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

public class ComtradeDataDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String fileName;
	private int stationId;
	private String stationName;
	private int faultId;
	private int dfrId;
	private String type;
	private long tsPrefault;
	private long tsTrigger;
	private double lineFreq;
	private double sampleRate;
	private int sampleCnt;
	private StringBuffer analogs;
	private StringBuffer events;
	private StringBuffer data;
	private String dataType;
	private String activeEvents;
	private long preFault;
	private long postFault;
	private double length;
	private String displayTime;
	private String headerInfo;
	private int exportId;
	private String exportName;
	private String exportStartTime;
	private String exportEndTime;
	private long tsLast;
	private String recordingDevId;
	private List<InputStream> lstBinaryDataSteam = null;
	private int startSampleNo;
	private int endSampleNo;
	private Reader asciiDataReader;
	private String faultLocationDetails;
	private java.text.SimpleDateFormat sdf = null;
	private float scaleFactor;
//	private int exportChnlId;
	private boolean faultLogic = true;
	private String userComments = ""; 
	private String lineGroups = ""; // Line groups list that passed the logic
	private String lineGroupDetailsForFaultLoc; // To be stored in INF files of all the faults that helps calculating fault location
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(ComtradeDataDTO.class);
	/**
	 * 
	 */
	public ComtradeDataDTO() {
		lineFreq = 60.0; // Default value
		faultId = -1;
		// Start: 31-Jan-2013 All date conversion are in M9kUtils class
//	     sdf = new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SSS");
//	     sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		// End: 31-Jan-2013

	}
	
	/**
	 * @return the dfrId
	 */
	public int getDfrId() {
		return dfrId;
	}
	/**
	 * @param dfrId the dfrId to set
	 */
	public void setDfrId(int dfrId) {
		this.dfrId = dfrId;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the lineFreq
	 */
	public double getLineFreq() {
		return lineFreq;
	}
	/**
	 * @param lineFreq the lineFreq to set
	 */
	public void setLineFreq(double lineFreq) {
		this.lineFreq = lineFreq;
	}
	/**
	 * @return the sampleRate
	 */
	public double getSampleRate() {
		return sampleRate;
	}
	/**
	 * @param sampleRate the sampleRate to set
	 */
	public void setSampleRate(double sampleRate) {
		this.sampleRate = sampleRate;
	}
	/**
	 * @return the sampleCnt
	 */
	public int getSampleCnt() {
		return sampleCnt;
	}
	/**
	 * @param sampleCnt the sampleCnt to set
	 */
	public void setSampleCnt(int sampleCnt) {
		this.sampleCnt = sampleCnt;
	}
	/**
	 * @return the analogs
	 */
	public StringBuffer getAnalogs() {
		return analogs;
	}
	/**
	 * @param analogs the analogs to set
	 */
	public void setAnalogs(StringBuffer analogs) {
		this.analogs = analogs;
	}
	/**
	 * @return the events
	 */
	public StringBuffer getEvents() {
		return events;
	}
	/**
	 * @param events the events to set
	 */
	public void setEvents(StringBuffer events) {
		this.events = events;
	}
	/**
	 * @return the data
	 */
	public StringBuffer getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(StringBuffer data) {
		this.data = data;
	}
	/**
	 * @return the tsPrefault
	 */
	public long getTsPrefault() {
		return tsPrefault;
	}
	/**
	 * @param tsPrefault the tsPrefault to set
	 */
	public void setTsPrefault(long tsPrefault) {
		this.tsPrefault = tsPrefault;
	}
	/**
	 * @return the tsTrigger
	 */
	public long getTsTrigger() {
		return tsTrigger;
	}
	/**
	 * @param tsTrigger the tsTrigger to set
	 */
	public void setTsTrigger(long tsTrigger) {
		this.tsTrigger = tsTrigger;
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
	 * @return the stationId
	 */
	public int getStationId() {
		return stationId;
	}
	/**
	 * @param stationId the stationId to set
	 */
	public void setStationId(int stationId) {
		this.stationId = stationId;
	}
	/**
	 * @return the faultId
	 */
	public int getFaultId() {
		return faultId;
	}
	/**
	 * @param faultId the faultId to set
	 */
	public void setFaultId(int faultId) {
		this.faultId = faultId;
	}
	/**
	 * @return the activeEvents
	 */
	public String getActiveEvents() {
		return activeEvents;
	}
	/**
	 * @param activeEvents the activeEvents to set
	 */
	public void setActiveEvents(String activeTriggers) {
		this.activeEvents = activeTriggers;
	}
	/**
	 * @return the preFault
	 */
	public long getPreFault() {
		return preFault;
	}
	/**
	 * @param preFault the preFault to set
	 */
	public void setPreFault(long preFault) {
		this.preFault = preFault;
	}
	/**
	 * @return the postFault
	 */
	public long getPostFault() {
		return postFault;
	}
	/**
	 * @param postFault the postFault to set
	 */
	public void setPostFault(long postFault) {
		this.postFault = postFault;
	}
	/**
	 * @return the length
	 */
	public double getLength() {
		return length;
	}
	/**
	 * @param length the length to set
	 */
	public void setLength(double length) {
		this.length = length;
	}
	/**
	 * @return the displayTime
	 */
	public String getDisplayTime() {
		if (sdf == null)
		{
			// Start: 31-Jan-2013 All date conversion are in M9kUtils class
		     sdf  = M9kUtils.getDateFormat("MM/dd/yyyy - HH:mm:ss.SSS");
			// End: 31-Jan-2013

		}
	     String microseconds = (""+getTsTrigger());
	     microseconds = microseconds.substring(microseconds.length()-3);
	     displayTime = sdf.format(new java.util.Date (getTsTrigger()/1000));
	     displayTime+=microseconds;
		return displayTime;
	}
	/**
	 * @param displayTime the displayTime to set
	 */
	public void setDisplayTime(String displayTime) {
		this.displayTime = displayTime;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the headerInfo
	 */
	public String getHeaderInfo() {
		return headerInfo;
	}

	/**
	 * @param headerInfo the headerInfo to set
	 */
	public void setHeaderInfo(String headerInfo) {
		this.headerInfo = headerInfo;
	}

	/**
	 * @return the exportId
	 */
	public int getExportId() {
		return exportId;
	}

	/**
	 * @param exportId the exportId to set
	 */
	public void setExportId(int exportId) {
		this.exportId = exportId;
	}

	/**
	 * @return the exportName
	 */
	public String getExportName() {
		return exportName;
	}

	/**
	 * @param exportName the exportName to set
	 */
	public void setExportName(String exportName) {
		this.exportName = exportName;
	}

	/**
	 * @return the exportStartTime
	 */
	public String getExportStartTime() {
		return exportStartTime;
	}

	/**
	 * @param exportStartTime the exportStartTime to set
	 */
	public void setExportStartTime(String exportStartTime) {
		this.exportStartTime = exportStartTime;
	}

	/**
	 * @return the exportEndTime
	 */
	public String getExportEndTime() {
		return exportEndTime;
	}

	/**
	 * @param exportEndTime the exportEndTime to set
	 */
	public void setExportEndTime(String exportEndTime) {
		this.exportEndTime = exportEndTime;
	}

	/**
	 * @return the tsLast
	 */
	public long getTsLast() {
		return tsLast;
	}

	/**
	 * @param tsLast the tsLast to set
	 */
	public void setTsLast(long tsLast) {
		this.tsLast = tsLast;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public String getRecordingDevId() {
		return recordingDevId;
	}

	public void setRecordingDevId(String recordingDevId) {
		this.recordingDevId = recordingDevId;
	}

	public InputStream getBinaryDataSteam() {
		SequenceInputStream sip = null;
		if (lstBinaryDataSteam != null)
		{
//			logger.debug("lstBinaryDataSteam size "+lstBinaryDataSteam.size());
			Enumeration<InputStream> enumInputStream = Collections.enumeration(lstBinaryDataSteam);
			sip = new SequenceInputStream(enumInputStream);
		}
		return sip;
	}

	public void setBinaryDataSteam(InputStream binaryDataSteam) {
		if (this.lstBinaryDataSteam == null)
		{
			this.lstBinaryDataSteam = new ArrayList<InputStream>();
		}
		this.lstBinaryDataSteam.add(binaryDataSteam);
//		logger.debug("Added to lstBinaryDataSteam size "+lstBinaryDataSteam.size());
	}

	@Override
	public String toString() {
		return "ComtradeDataDTO [fileName=" + fileName + ", stationId="
				+ stationId + ", stationName=" + stationName + ", faultId="
				+ faultId + ", dfrId=" + dfrId + ", type=" + type
				+ ", tsPrefault=" + tsPrefault + ", tsTrigger=" + tsTrigger
				+ ", lineFreq=" + lineFreq + ", sampleRate=" + sampleRate
				+ ", sampleCnt=" + sampleCnt + ", analogs=" + analogs
				+ ", events=" + events + ", data=" + data + ", dataType="
				+ dataType + ", activeEvents=" + activeEvents + ", preFault="
				+ preFault + ", postFault=" + postFault + ", length=" + length
				+ ", displayTime=" + displayTime + ", headerInfo=" + headerInfo
//				+ ", exportId=" + exportId + ", exportChnlId=" + exportChnlId+ ", exportName=" + exportName
				+ ", exportId=" + exportId + ", exportName=" + exportName
				+ ", exportStartTime=" + exportStartTime + ", exportEndTime="
				+ exportEndTime + ", tsLast=" + tsLast
				+ ", recordingDevId=" + recordingDevId
				+ ", lstBinaryDataSteam=" + lstBinaryDataSteam + "]";
	}

//	@Override
//	public String toString() {
//		return "ComtradeDataDTO ["+", dfrId=" + dfrId + ", exportId=" + exportId + ", exportChnlId=" + exportChnlId +"]";
//	}

	public int getStartSampleNo() {
		return startSampleNo;
	}

	public void setStartSampleNo(int startSampleNo) {
		this.startSampleNo = startSampleNo;
	}

	public int getEndSampleNo() {
		return endSampleNo;
	}

	public void setEndSampleNo(int endSampleNo) {
		this.endSampleNo = endSampleNo;
	}

	public Reader getAsciiDataReader() {
		return asciiDataReader;
	}

	public void setAsciiDataReader(Reader asciiDataReader) {
		this.asciiDataReader = asciiDataReader;
	}

	public String getFaultLocationDetails() {
		return faultLocationDetails;
	}

	public void setFaultLocationDetails(String faultLocationDetails) {
		this.faultLocationDetails = faultLocationDetails;
	}

	public float getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

//	public int getExportChnlId() {
//		return exportChnlId;
//	}
//
//	public void setExportChnlId(int exportChnlId) {
//		this.exportChnlId = exportChnlId;
//	}

	/**
	 * @return the faultLogic
	 */
	public boolean isFaultLogic() {
		return faultLogic;
	}

	/**
	 * @param faultLogic the faultLogic to set
	 */
	public void setFaultLogic(boolean faultLogic) {
		this.faultLogic = faultLogic;
	}

	/**
	 * @return the userComments
	 */
	public String getUserComments() {
		return userComments;
	}

	/**
	 * @param userComments the userComments to set
	 */
	public void setUserComments(String userComments) {
		this.userComments = userComments;
	}

	/**
	 * @return the lineGroups
	 */
	public String getLineGroups() {
		return lineGroups;
	}

	/**
	 * @param lineGroups the lineGroups to set
	 */
	public void setLineGroups(String lineGroups) {
		this.lineGroups = lineGroups;
	}
	
	// Used as a email body when sending out an email
	public String getFaultDetailsSummary()
	{
		StringBuffer faultDetailsSummary = new StringBuffer();
		faultDetailsSummary.append("Fault id: "+getFaultId());
		faultDetailsSummary.append(M9kConstants.NEWLINE);
		faultDetailsSummary.append("Date of occurence: "+getDisplayTime());
		if (getActiveEvents() != null && !getActiveEvents().isEmpty())
		{
			faultDetailsSummary.append(M9kConstants.NEWLINE);
			faultDetailsSummary.append("List of abnormal events: ");
			faultDetailsSummary.append(M9kConstants.NEWLINE);
			faultDetailsSummary.append(getActiveEvents());
			if (getFaultLocationDetails() != null && !getFaultLocationDetails().isEmpty())
			{
				faultDetailsSummary.append(M9kConstants.NEWLINE);
				faultDetailsSummary.append("Fault Location Details: ");
				faultDetailsSummary.append(M9kConstants.NEWLINE);
				faultDetailsSummary.append(getFaultLocationDetails());
			}
		}
		return faultDetailsSummary.toString();
	}
	
	public String getStationTitle()
	{
		String stationTitle = getStationId()+" - "+getStationName();
		return stationTitle;
	}

	public String getLineGroupDetailsForFaultLoc() {
		return lineGroupDetailsForFaultLoc;
	}

	public void setLineGroupDetailsForFaultLoc(String lineGroupDetailsForFaultLoc) {
		this.lineGroupDetailsForFaultLoc = lineGroupDetailsForFaultLoc;
	}
}
