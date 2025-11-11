package com.usi.m9000.config;

public class AnalogInfo extends ChannelInfo{

	private String chnlUnit; // Critical
	private double chnlMultiplier; // Critical
	private double chnlOffset; // Critical
	private double chnlSkew; // Non Critical
	private int rangeMin; // Critical
	private int rangeMax; // Critical
	private double primary; // Critical
	private double secondary; // Critical
	private char scalingFactor; // Critical
	// START: 28-Jan-2020 - Transducer Implementation
	private boolean tranducer = false;
	// END: 28-Jan-2020
	public AnalogInfo(int chnlIndex) {
		super (chnlIndex);
	}

	public String getChnlUnit() {
		return chnlUnit;
	}

	public void setChnlUnit(String chnlUnit) {
		this.chnlUnit = chnlUnit;
	}

	public double getChnlMultiplier() {
		return chnlMultiplier;
	}

	public void setChnlMultiplier(double chnlMultiplier) {
		this.chnlMultiplier = chnlMultiplier;
	}

	public double getChnlOffset() {
		return chnlOffset;
	}

	public void setChnlOffset(double chnlOffset) {
		this.chnlOffset = chnlOffset;
	}

	public double getChnlSkew() {
		return chnlSkew;
	}

	public void setChnlSkew(double chnlSkew) {
		this.chnlSkew = chnlSkew;
	}

	public int getRangeMin() {
		return rangeMin;
	}

	public void setRangeMin(int rangeMin) {
		this.rangeMin = rangeMin;
	}

	public int getRangeMax() {
		return rangeMax;
	}

	public void setRangeMax(int rangeMax) {
		this.rangeMax = rangeMax;
	}

	public double getPrimary() {
		return primary;
	}

	public void setPrimary(double primary) {
		this.primary = primary;
	}

	public double getSecondary() {
		return secondary;
	}

	public void setSecondary(double secondary) {
		this.secondary = secondary;
	}

	public char getScalingFactor() {
		return scalingFactor;
	}

	public void setScalingFactor(char scalingFactor) {
		this.scalingFactor = scalingFactor;
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
		strBuffer.append("; Channel Units:"+getChnlUnit());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Channel Multiplier:"+getChnlMultiplier());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Channel Offset:"+getChnlOffset());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Channel Skew:"+getChnlSkew());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Minimum data value:"+getRangeMin());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Maximum data value:"+getRangeMax());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Primary factor:"+getPrimary());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Secondary factor:"+getSecondary());
		strBuffer.append(System.getProperty("line.separator"));
		strBuffer.append("; Scaling identifier:"+getScalingFactor());
		strBuffer.append(System.getProperty("line.separator"));
		
		return strBuffer.toString();
	}

	public boolean isTranducer() {
		return tranducer;
	}

	public void setTranducer(boolean tranducer) {
		this.tranducer = tranducer;
	}

}
