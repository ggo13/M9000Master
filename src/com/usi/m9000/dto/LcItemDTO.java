package com.usi.m9000.dto;

import java.io.Serializable;

public class LcItemDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String triggerTime;
	private String faultInfo;
	private String infPath;
	private String hasCorrelation;
	private String correlationResult;
	private boolean InfUpdated;
	private boolean correlationReceived;
	private boolean serverNotified;
	private boolean infBroadcasted;
	/**
	 * @return the triggerTime
	 */
	public String getTriggerTime() {
		return triggerTime;
	}
	/**
	 * @param triggerTime the triggerTime to set
	 */
	public void setTriggerTime(String triggerTime) {
		this.triggerTime = triggerTime;
	}
	/**
	 * @return the faultInfo
	 */
	public String getFaultInfo() {
		return faultInfo;
	}
	/**
	 * @param faultInfo the faultInfo to set
	 */
	public void setFaultInfo(String faultInfo) {
		this.faultInfo = faultInfo;
	}
	/**
	 * @return the infPath
	 */
	public String getInfPath() {
		return infPath;
	}
	/**
	 * @param infPath the infPath to set
	 */
	public void setInfPath(String infPath) {
		this.infPath = infPath;
	}
	/**
	 * @return the hasCorrelation
	 */
	public String getHasCorrelation() {
		return hasCorrelation;
	}
	/**
	 * @param hasCorrelation the hasCorrelation to set
	 */
	public void setHasCorrelation(String hasCorrelation) {
		this.hasCorrelation = hasCorrelation;
	}
	/**
	 * @return the correlationResult
	 */
	public String getCorrelationResult() {
		return correlationResult;
	}
	/**
	 * @param correlationResult the correlationResult to set
	 */
	public void setCorrelationResult(String correlationResult) {
		this.correlationResult = correlationResult;
	}
	/**
	 * @return the infUpdated
	 */
	public boolean isInfUpdated() {
		return InfUpdated;
	}
	/**
	 * @param infUpdated the infUpdated to set
	 */
	public void setInfUpdated(boolean infUpdated) {
		InfUpdated = infUpdated;
	}
	/**
	 * @return the correlationReceived
	 */
	public boolean isCorrelationReceived() {
		return correlationReceived;
	}
	/**
	 * @param correlationReceived the correlationReceived to set
	 */
	public void setCorrelationReceived(boolean correlationReceived) {
		this.correlationReceived = correlationReceived;
	}
	/**
	 * @return the serverNotified
	 */
	public boolean isServerNotified() {
		return serverNotified;
	}
	/**
	 * @param serverNotified the serverNotified to set
	 */
	public void setServerNotified(boolean serverNotified) {
		this.serverNotified = serverNotified;
	}
	/**
	 * @return the infBroadcasted
	 */
	public boolean isInfBroadcasted() {
		return infBroadcasted;
	}
	/**
	 * @param infBroadcasted the infBroadcasted to set
	 */
	public void setInfBroadcasted(boolean infBroadcasted) {
		this.infBroadcasted = infBroadcasted;
	}
	
}
