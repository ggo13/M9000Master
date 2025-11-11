package com.usi.m9000.config;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.usi.m9000.util.Pair;

public class StationInfo {

	private String userName;
	private String stationName; // Non Critical
	private String recordingDeviceId; // Non Critical
	private int comtradeStdRevYear; // Critical
	private int channelsCount; // Critical
	private int analogChannelsCount; // Critical
	private int digitalChannelsCount; // Critical
	private Collection<ChannelInfo> analogInfoCollection;
	private Collection<ChannelInfo> digitalInfoCollection;
	private double lineFrequency; // Non Critical
	private int noOfSamplingRates; // Critical
	private List<Pair<Double, Integer>> sampleRateConf;
	private Timestamp timeOfFirstDataVal; // Non Critical
	private Timestamp timeOfTriggerPoint; // Non Critical
	private String fileType; // Critical
	private double timeMult; // Critical

	// System details of the config XML
	private Integer systemAnalogChannelsCount; 
	private Integer systemDigitalChannelsCount; 
	private Integer systemSampleRate;
	private Integer systemPrefaultTime;
	private Integer systemPostfaultTime;
	private String systemStationName; 
	private String systemRecordingDeviceId; 
	private Integer systemLineFrequency; 
	private String ps;

	// System details of the config XML
	
	
	public StationInfo()
	{
	}
	public StationInfo(String stationName) {
		this.stationName = stationName;
		sampleRateConf = new ArrayList<Pair<Double, Integer>>();
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	public String getRecordingDeviceId() {
		return recordingDeviceId;
	}

	public void setRecordingDeviceId(String recordingDeviceId) {
		this.recordingDeviceId = recordingDeviceId;
	}

	public int getComtradeStdRevYear() {
		return comtradeStdRevYear;
	}

	public void setComtradeStdRevYear(int comtradeStdRevYear) {
		this.comtradeStdRevYear = comtradeStdRevYear;
	}

	public int getChannelsCount() {
		channelsCount = getAnalogChannelsCount() + getDigitalChannelsCount();
		return channelsCount;
	}

	public void setChannelsCount(int noOfChannels) {
		this.channelsCount = noOfChannels;
	}

	public int getAnalogChannelsCount() {
		return analogChannelsCount;
	}

	public void setAnalogChannelsCount(int noOfAnalogChannels) {
		this.analogChannelsCount = noOfAnalogChannels;
	}

	public int getDigitalChannelsCount() {
		return digitalChannelsCount;
	}

	public void setDigitalChannelsCount(int noOfDigitalChannels) {
		this.digitalChannelsCount = noOfDigitalChannels;
	}

	public Collection<ChannelInfo> getAnalogInfoCollection() {
		return analogInfoCollection;
	}

	public void setAnalogInfoCollection(Collection<ChannelInfo> analogInfoCollection) {
		this.analogInfoCollection = analogInfoCollection;
	}

	public Collection<ChannelInfo> getDigitalInfoCollection() {
		return digitalInfoCollection;
	}

	public void setDigitalInfoCollection(
			Collection<ChannelInfo> digitalInfoCollection) {
		this.digitalInfoCollection = digitalInfoCollection;
	}

	public double getLineFrequency() {
		return lineFrequency;
	}

	public void setLineFrequency(double lineFrequency) {
		this.lineFrequency = lineFrequency;
	}

	public int getNoOfSamplingRates() {
		return noOfSamplingRates;
	}

	public void setNoOfSamplingRates(int noOfSamplingRates) {
		this.noOfSamplingRates = noOfSamplingRates;
	}

	public Timestamp getTimeOfFirstDataVal() {
		return timeOfFirstDataVal;
	}

	public void setTimeOfFirstDataVal(Timestamp timeOfFirstDataVal) {
		this.timeOfFirstDataVal = timeOfFirstDataVal;
	}

	public Timestamp getTimeOfTriggerPoint() {
		return timeOfTriggerPoint;
	}

	public void setTimeOfTriggerPoint(Timestamp timeOfTriggerPoint) {
		this.timeOfTriggerPoint = timeOfTriggerPoint;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public double getTimeMult() {
		return timeMult;
	}

	public void setTimeMult(double timeMult) {
		this.timeMult = timeMult;
	}

	public String toString()
	{
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("User Name:\t"+getUserName());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Station Name:\t"+getStationName());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Recording Dev Id:\t"+getRecordingDeviceId());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("COMTRADE Standard Revision Year:\t"+getComtradeStdRevYear());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Total Number of Channels:\t"+getChannelsCount());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Analog Channels count:\t"+getAnalogChannelsCount());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Digital Channels Count:\t"+getDigitalChannelsCount());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Analog Channels info:\t"+getAnalogInfoCollection());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Digital Channels info:\t"+getDigitalInfoCollection());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Line Frequency:\t"+getLineFrequency());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Number of sampling rates:\t"+getNoOfSamplingRates());
		strBuffer.append(System.getProperty("line.separator"));
		for (int i = 0; i < getNoOfSamplingRates(); i++) {
			strBuffer.append("Sample Rate in hertz:\t"+getSampleRate(i));
			strBuffer.append(System.getProperty("line.separator"));
			strBuffer.append("Total Samples for each Sample Rate:\t"+getTotalNumberOfSamples(i));
		}
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("First Data point time:\t"+getTimeOfFirstDataVal());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Trigger point time:\t"+getTimeOfTriggerPoint());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Data File Type:\t"+getFileType());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Time Stamp Multiplication Factor:\t" + getTimeMult());
		return strBuffer.toString();
	}

	public List<Pair<Double, Integer>> getSampleRateConf() {
		return sampleRateConf;
	}

	public void setSampleRateConf(List<Pair<Double, Integer>> sampleRateConf) {
		this.sampleRateConf = sampleRateConf;
	}
	
	public void addSampleRateCfg(Double samp, Integer endSamp)
	{
		this.sampleRateConf.add(new Pair<Double, Integer>(samp, endSamp));
	}
	public int getTotalNumberOfSamples(int index)
	{
		Pair<Double, Integer> pair = this.sampleRateConf.get(index);
		return pair.getSecond().intValue();
	}
	
	public double getSampleRate(int index)
	{
		Pair<Double, Integer> pair = this.sampleRateConf.get(index);
		return pair.getFirst();
	}
	
	public String[] getAnalogChannelTitles()
	{
		String[] titles = new String[getAnalogChannelsCount()];
		for (int j = 0; j < getAnalogChannelsCount(); j++) {
			titles[j] = getAnalogInfo(j).getChnlId()+" - "+getAnalogInfo(j).getCircuitName();
		}
		return titles;
	}

	public String[] getSelectedAnalogChannelTitles(List<Integer> selectedChannels)
	{
		String[] titles = new String[selectedChannels.size()];
		int selectedIndex = 0;
		for (int j = 0; j < selectedChannels.size(); j++) {
			selectedIndex = ((Integer)selectedChannels.get(selectedIndex)).intValue();
			titles[j] = getAnalogInfo(selectedIndex).getChnlId()+" - "+getAnalogInfo(selectedIndex).getCircuitName();
		}
		return titles;
	}

	public String[] getDigitalChannelTitles()
	{
		String[] titles = new String[getDigitalChannelsCount()];
		for (int j = 0; j < getDigitalChannelsCount(); j++) {
			titles[j] = getDigitalInfo(j).getChnlId()+" - "+getDigitalInfo(j).getCircuitName();
		}
		return titles;
	}
	
	public String[] getSelectedDigitalChannelTitles(Integer[] selectedChannels)
	{
		String[] digitalChannelTitles = new String[selectedChannels.length+1];
		int selectedIndex = 0;
		int plotIndex = 0;
		digitalChannelTitles[plotIndex++] = "";
		for (int j = 0; j < selectedChannels.length;  j++) {
			selectedIndex = selectedChannels[j];
			digitalChannelTitles[plotIndex++] = getDigitalInfo(selectedIndex).getCircuitName()+" - "+getDigitalInfo(selectedIndex).getChnlId();
		}
		return digitalChannelTitles;
	}

	public String[] getSelectedDigitalChannelTitlesForLegends(Integer[] selectedChannels)
	{
		String[] digitalChannelTitles = new String[selectedChannels.length+1];
		int selectedIndex = 0;
//		int plotIndex = selectedChannels.length-1;
//		digitalChannelTitles[plotIndex++] = "";
		for (int j = 0; j < selectedChannels.length;  j++) {
			selectedIndex = selectedChannels[j];
			digitalChannelTitles[j] = getDigitalInfo(selectedIndex).getCircuitName()+" - "+getDigitalInfo(selectedIndex).getChnlId();
//			System.out.println("Digital Channel Titles..."+digitalChannelTitles[j]);
		}
		return digitalChannelTitles;
	}
	
	
	public String[] getSelectedDigitalChannelIds(Integer[] selectedChannels)
	{
		String[] digitalChannelIds = new String[selectedChannels.length+1];
		int selectedIndex = 0;
		int plotIndex = 0;
		digitalChannelIds[plotIndex++] = "";
		for (int j = 0; j < selectedChannels.length;  j++) {
			selectedIndex = selectedChannels[j];
			digitalChannelIds[plotIndex++] = getDigitalInfo(selectedIndex).getChnlId();
//			System.out.println("selected index "+selectedIndex+" Update Y-Axis.."+digitalChannelIds[plotIndex-1]);
		}
		return digitalChannelIds;
	}
	private AnalogInfo getAnalogInfo(int Channel)
	{
		AnalogInfo analogInfo = (AnalogInfo) ((ArrayList<ChannelInfo>)getAnalogInfoCollection()).get(Channel);
		
		return analogInfo;
	}
	
	public DigitalInfo getDigitalInfo(int Channel)
	{
		DigitalInfo digitalInfo = (DigitalInfo) ((ArrayList<ChannelInfo>)getDigitalInfoCollection()).get(Channel);
		
		return digitalInfo;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
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

}
