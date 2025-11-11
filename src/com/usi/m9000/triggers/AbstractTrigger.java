package com.usi.m9000.triggers;

import com.usi.TriggerDocument.Trigger;

public abstract class AbstractTrigger {
	private String name;
	private String inputValue;
	private LimitsAlgorithm limitsAlgorithm;
	private Trigger trigger;
	private Integer average;
	// 18-Mar-2021 - Description added to all algorithms
	private String description;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the inputValue
	 */
	public String getInputValue() {
		return inputValue;
	}
	/**
	 * @param inputValue the inputValue to set
	 */
	public void setInputValue(String inputValue) {
		this.inputValue = inputValue;
	}
	/**
	 * @return the limitsAlgorithm
	 */
	public LimitsAlgorithm getLimitsAlgorithm() {
		return limitsAlgorithm;
	}
	/**
	 * @param limitsAlgorithm the limitsAlgorithm to set
	 */
	public void setLimitsAlgorithm(LimitsAlgorithm limitsAlgorithm) {
		this.limitsAlgorithm = limitsAlgorithm;
	}
	/**
	 * @return the trigger
	 */
	public Trigger getTrigger() {
		return trigger;
	}
	/**
	 * @param trigger the trigger to set
	 */
	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public abstract String getInputXMLString();
	public abstract String getPmuInputType();
	public Integer getAverage() {
		return average;
	}
	public void setAverage(Integer average) {
		this.average = average;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
