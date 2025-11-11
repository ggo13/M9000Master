package com.usi.m9000.config;

public class DigitalInfo extends ChannelInfo {

	private int status; // Critical
	public DigitalInfo(int chnlIndex) {
		super(chnlIndex);
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String toString()
	{
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("Channel Index:"+getIndex());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Channel Name:"+getChnlId());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Phase Id:"+getPhaseId());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Circuit Name:"+getCircuitName());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Channel Status:"+getStatus());
		strBuffer.append(System.getProperty("line.separator"));
		
		return strBuffer.toString();
	}


}
