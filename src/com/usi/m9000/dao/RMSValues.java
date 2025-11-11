package com.usi.m9000.dao;

import java.awt.Paint;
import java.text.DecimalFormat;

public class RMSValues
{
	float value;
	Paint paint;
	String units; 
	/**
	 * @param value
	 * @param paint
	 */
	public RMSValues(float value, Paint paint) {
		super();
		this.value = value;
		this.paint = paint;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		DecimalFormat nf = new DecimalFormat("0.000");
		return nf.format(value);
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(float value) {
		this.value = value;
	}
	/**
	 * @return the paint
	 */
	public Paint getPaint() {
		return paint;
	}
	/**
	 * @param paint the paint to set
	 */
	public void setPaint(Paint paint) {
		this.paint = paint;
	}
	public String toString()
	{
		return "[ " + this.value + " , " + this.paint+" ]";
	}
	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}
	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}
	
}
