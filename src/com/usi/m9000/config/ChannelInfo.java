package com.usi.m9000.config;

public abstract class ChannelInfo {

	private int index; // Critical
	private String chnlId; // Non Critical
	private String phaseId; // Non Critical
	private String circuitName;  // Non Critical
	
	public ChannelInfo()
	{
		
	}
	public ChannelInfo(int chnlIndex) {
		this.index = chnlIndex;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getChnlId() {
		return chnlId;
	}
	public void setChnlId(String chnlId) {
		this.chnlId = chnlId;
	}
	public String getPhaseId() {
		return phaseId;
	}
	public void setPhaseId(String phaseId) {
		this.phaseId = phaseId;
	}
	public String getCircuitName() {
		return circuitName;
	}
	public void setCircuitName(String circuitName) {
		this.circuitName = circuitName;
	}

}
