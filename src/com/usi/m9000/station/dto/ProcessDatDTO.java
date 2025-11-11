package com.usi.m9000.station.dto;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import com.usi.m9000.config.ChannelInfo;

public class ProcessDatDTO {

	private int datId;
	private int dfrId;
	private String type;
	private long tsPrefault;
	private long tsTrigger;
	private double lineFreq;
	private double sampleRate;
	private int sampleCnt;
	StringBuffer analogs;
	StringBuffer events;
	StringBuffer data;
	private int isTransient;
	private String ipAddress;
	private int port;
	private int analogOffset;
	private int eventsOffset;
	private String prefixDataStr;
	private StringBuffer mergedAnalogData;
	private StringBuffer mergedEventData;
//	private List<InputStream> lstBinaryDataSteam = null;
	private InputStream binaryDataStream;
	private StringBuffer binaryEventsString; 
	private ByteArrayOutputStream mergedBinaryAnalogData;
	private ByteArrayOutputStream mergedBinaryEventData;
	private ByteArrayOutputStream binaryData;
	private int mergedAnalogsCount = 0;
	private int mergedEventsCount = 0;

	// Fault Location
	private List<ChannelInfo> lstAnalogInfo;
	private List<ChannelInfo> lstDigitalInfo;

	public ProcessDatDTO() {
		super();
		mergedAnalogData = null;
		mergedEventData = null;
		mergedBinaryAnalogData = null;
		mergedBinaryEventData = null;
		events = null;
	}

	public int getDatId() {
		return datId;
	}

	public void setDatId(int datId) {
		this.datId = datId;
	}

	public int getDfrId() {
		return dfrId;
	}

	public void setDfrId(int dfrId) {
		this.dfrId = dfrId;
	}

	public long getTsPrefault() {
		return tsPrefault;
	}

	public void setTsPrefault(long tsPrefault) {
		this.tsPrefault = tsPrefault;
	}

	public long getTsTrigger() {
		return tsTrigger;
	}

	public void setTsTrigger(long tsTrigger) {
		this.tsTrigger = tsTrigger;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getStartTime()
	{
		return tsPrefault;
	}
	
	public long getEndTime()
	{
		long endTime;
		// Last sample time calculated is the end time
		endTime = (long)((getTsPrefault()/1000000.0 + (getSampleCnt()/getSampleRate()))*1000000);
		
		return endTime;
	}

	public double getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(double sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getSampleCnt() {
		return sampleCnt;
	}

	public void setSampleCnt(int sampleCnt) {
		this.sampleCnt = sampleCnt;
	}

	public double getSamplesPerCycle()
	{
		return (getSampleRate() / getLineFreq());
	}
	public double getLineFreq() {
		return lineFreq;
	}

	public void setLineFreq(double lineFreq) {
		this.lineFreq = lineFreq;
	}

	public StringBuffer getAnalogs() {
		return analogs;
	}

	public void setAnalogs(StringBuffer analogs) {
		this.analogs = analogs;
	}

	public StringBuffer getEvents() {
		return events;
	}

	public void setEvents(StringBuffer events) {
		this.events = events;
	}

	public StringBuffer getData() {
		return data;
	}

	public void setData(StringBuffer data) {
		this.data = data;
	}

	public int getAnalogOffset() {
		return analogOffset;
	}

	public void setAnalogOffset(int analogOffset) {
		this.analogOffset = analogOffset;
	}

	public int getEventsOffset() {
		return eventsOffset;
	}

	public void setEventsOffset(int eventsOffset) {
		this.eventsOffset = eventsOffset;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public StringBuffer getMergedAnalogData() {
		return mergedAnalogData;
	}

	public void setMergedAnalogData(StringBuffer mergedAnalogData) {
		this.mergedAnalogData = mergedAnalogData;
	}

	public StringBuffer getMergedEventData() {
		return mergedEventData;
	}

	public void setMergedEventData(StringBuffer mergedEventData) {
		this.mergedEventData = mergedEventData;
	}

	public String getPrefixDataStr() {
		return prefixDataStr;
	}

	public void setPrefixDataStr(String prefixDataStr) {
		this.prefixDataStr = prefixDataStr;
	}

	@Override
	public String toString() {
		return "ProcessDatDTO [datId=" + datId + ", dfrId=" + dfrId + ", type="
				+ type + ", tsPrefault=" + tsPrefault + ", tsTrigger="
				+ tsTrigger + ", lineFreq=" + lineFreq + ", sampleRate="
				+ sampleRate + ", sampleCnt=" + sampleCnt + ", analogOffset="
				+ analogOffset + ", eventsOffset=" + eventsOffset + "]";
	}

	public int getIsTransient() {
		return isTransient;
	}

	public void setIsTransient(int isTransient) {
		this.isTransient = isTransient;
	}
	
//	public InputStream getBinaryDataSteam() {
//		SequenceInputStream sip = null;
//		if (lstBinaryDataSteam != null)
//		{
//			Enumeration<InputStream> enumInputStream = Collections.enumeration(lstBinaryDataSteam);
//			sip = new SequenceInputStream(enumInputStream);
//		}
//		return sip;
//	}
//
//	public void setBinaryDataSteam(InputStream binaryDataSteam) {
//		if (this.lstBinaryDataSteam == null)
//		{
//			this.lstBinaryDataSteam = new ArrayList<InputStream>();
//		}
//		this.lstBinaryDataSteam.add(binaryDataSteam);
//	}

	public StringBuffer getBinaryEventsString() {
		return binaryEventsString;
	}

	public void setBinaryEventsString(StringBuffer binaryEventsString) {
		this.binaryEventsString = binaryEventsString;
	}
	
//	public void writeBinaryEventData(String event)
//	{
//		try {
//		if (binaryEventsString == null)
//		{
//			binaryEventsString = new StringBuffer();
//		}
//		else
//		{
//			binaryEventsString.insert(0, event.trim());
//			if (binaryEventsString.length() == 16)
//			{
//				eventsOutStream.writeShort(convertEventToHex(binaryEventsString.toString()));
//				binaryEventsString = null;
//			}
//		}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public void writeBinaryAnalogData(String analogData)
	{
		
	}
//	public ByteArrayOutputStream getBinaryEvents() {
//		try
//		{
//		if (binaryEventsString != null && binaryEventsString.length() > 0)
//		{
//			while (binaryEventsString.length() < 16)
//			{
//				binaryEventsString.insert(0, "0");
//			}
//			eventsOutStream.writeShort(convertEventToHex(binaryEventsString.toString()));
//		}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		finally
//		{
//			if (eventsOutStream != null)
//			{
//				try {
//					eventsOutStream.close();
//				} catch (IOException e) {
//					
//				}
//				eventsOutStream = null;
//			}
//		}
//		return binaryEvents;
//	}

//	public void setBinaryEvents(ByteArrayOutputStream binaryEvents) {
//		this.binaryEvents = binaryEvents;
//	}
	
	private int convertEventToHex(String events)
	{
		int hexVal;
		long val = Long.parseLong(events,2);
		String zeros = "0000";
		String hexString = Long.toHexString(Long.reverseBytes(val));
		if (hexString.length() > 4)
		{
			hexString = hexString.substring(0,4);
		}
		else
		{
			String leadZero = zeros.substring(0,4-hexString.length());
			hexString=leadZero.concat(hexString);			
		}
		hexVal = Integer.parseInt(hexString,16);
		return hexVal;
	}


	public InputStream getBinaryDataStream() {
		return binaryDataStream;
	}

	public void setBinaryDataStream(InputStream binaryEventDataStream) {
		this.binaryDataStream = binaryEventDataStream;
	}

	public ByteArrayOutputStream getMergedBinaryAnalogData() {
		return mergedBinaryAnalogData;
	}

	public void setMergedBinaryAnalogData(
			ByteArrayOutputStream mergedBinaryAnalogData) {
		this.mergedBinaryAnalogData = mergedBinaryAnalogData;
	}

	public ByteArrayOutputStream getMergedBinaryEventData() {
		return mergedBinaryEventData;
	}

	public void setMergedBinaryEventData(ByteArrayOutputStream mergedBinaryEventData) {
		this.mergedBinaryEventData = mergedBinaryEventData;
	}

	public int getMergedAnalogsCount() {
		return mergedAnalogsCount;
	}

	public void setMergedAnalogsCount(int mergedAnalogsCount) {
		this.mergedAnalogsCount = mergedAnalogsCount;
	}

	public int getMergedEventsCount() {
		return mergedEventsCount;
	}

	public void setMergedEventsCount(int mergedEventsCount) {
		this.mergedEventsCount = mergedEventsCount;
	}

	public ByteArrayOutputStream getBinaryData() {
		return binaryData;
	}

	public void setBinaryData(ByteArrayOutputStream binaryData) {
		this.binaryData = binaryData;
	}

	public List<ChannelInfo> getLstAnalogInfo() {
		return lstAnalogInfo;
	}

	public void setLstAnalogInfo(List<ChannelInfo> lstAnalogInfo) {
		this.lstAnalogInfo = lstAnalogInfo;
	}

	public List<ChannelInfo> getLstDigitalInfo() {
		return lstDigitalInfo;
	}

	public void setLstDigitalInfo(List<ChannelInfo> lstDigitalInfo) {
		this.lstDigitalInfo = lstDigitalInfo;
	}


//	public ByteArrayOutputStream getBinaryAnalogs() {
//		return binaryAnalogs;
//	}
//
//	public void setBinaryAnalogs(ByteArrayOutputStream binaryAnalogs) {
//		this.binaryAnalogs = binaryAnalogs;
//	}

}
