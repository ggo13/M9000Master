/**
 * 
 */
package com.usi.m9000.triggers;

import com.usi.m9000.util.M9kXMLConstants;

/**
 * @author sramasamy
 *
 */
public class LimitsAlgorithm {
private String name;
private String inputValue;
private String highLimit;
private String lowLimit;
private String rateOfChangeLimit;
//START: 25-Feb-2020 - ROC positive and Negative Limits implementation
private String rateOfChangeLimitPos;
private String rateOfChangeLimitNeg;
//END: 25-Feb-2020
private String duration;
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
 * @return the highLimit
 */
public String getHighLimit() {
	return highLimit;
}
/**
 * @param highLimit the highLimit to set
 */
public void setHighLimit(String highLimit) {
	this.highLimit = highLimit;
}
/**
 * @return the lowLimit
 */
public String getLowLimit() {
	return lowLimit;
}
/**
 * @param lowLimit the lowLimit to set
 */
public void setLowLimit(String lowLimit) {
	this.lowLimit = lowLimit;
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
public void setInputValue(String input) {
	this.inputValue = input;
}
public String getLimitsInput()
{
	StringBuffer inputTag = new StringBuffer(); 
	inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
	inputTag.append(M9kXMLConstants.XML_LIMITS_PREFIX);
	inputTag.append(getName());
	inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
	return inputTag.toString();
}
/**
 * @return the rateOfChangeLimit
 */
public String getRateOfChangeLimit() {
	return rateOfChangeLimit;
}
/**
 * @param rateOfChange the rateOfChangeLimit to set
 */
public void setRateOfChangeLimit(String rateOfChangeLimit) {
	this.rateOfChangeLimit = rateOfChangeLimit;
}
public String getRateOfChangeLimitPos() {
	return rateOfChangeLimitPos;
}
public void setRateOfChangeLimitPos(String rateOfChangeLimitPos) {
	this.rateOfChangeLimitPos = rateOfChangeLimitPos;
}
public String getRateOfChangeLimitNeg() {
	return rateOfChangeLimitNeg;
}
public void setRateOfChangeLimitNeg(String rateOfChangeLimitNeg) {
	this.rateOfChangeLimitNeg = rateOfChangeLimitNeg;
}
/**
 * @return the duration
 */
public String getDuration() {
	return duration;
}
/**
 * @param duration the duration to set
 */
public void setDuration(String duration) {
	this.duration = duration;
}

}
