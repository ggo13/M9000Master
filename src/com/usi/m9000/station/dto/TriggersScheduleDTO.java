package com.usi.m9000.station.dto;

public class TriggersScheduleDTO {
	private int idTrigger;
	private int dfrId;
	private long startTime;
	private long stopTime;
	private int signature;
	
	public int getIdTrigger() {
		return idTrigger;
	}
	public void setIdTrigger(int idTrigger) {
		this.idTrigger = idTrigger;
	}
	public int getDfrId() {
		return dfrId;
	}
	public void setDfrId(int dfrId) {
		this.dfrId = dfrId;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getStopTime() {
		return stopTime;
	}
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}
	public int getSignature() {
		return signature;
	}
	public void setSignature(int signature) {
		this.signature = signature;
	}
	@Override
	public String toString() {
		return "TriggersScheduleDTO [idTrigger=" + idTrigger + ", dfrId="
				+ dfrId + ", startTime=" + startTime + ", stopTime=" + stopTime
				+ ", signature=" + signature + "]";
	}

}
