/**
 * 
 */
package com.usi.m9000.triggers;

import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kXMLConstants;

/**
 * @author sramasamy
 *
 */
public class PowerTrigger extends AbstractTrigger{
	private String powerType;
	private String inputs;

	/**
	 * 
	 */
	public PowerTrigger(String powerType) {
		this.powerType = powerType;
	}

	/**
	 * @return the powerType
	 */
	public String getPowerType() {
		return powerType;
	}



	/**
	 * @param powerType the powerType to set
	 */
	public void setPowerType(String powerType) {
		this.powerType = powerType;
	}

	@Override
	public String getInputXMLString() {
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
		inputTag.append(M9kXMLConstants.XML_POWER_PREFIX);
		inputTag.append(getName());
		inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
		return inputTag.toString();		
	}

	public String getInputXMLString(String value) {
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
		inputTag.append(M9kXMLConstants.XML_POWER_PREFIX);
		inputTag.append(getName());
		inputTag.append(value);
		return inputTag.toString();		
	}

	/**
	 * @return the inputs
	 */
	public String getInputs() {
		return inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(String inputs) {
		this.inputs = inputs;
	}
	
	@Override
	public String getPmuInputType() {
		// START: 20-NOV-2017 PMU Input type changed to Analog
//		return M9kConstants.PMU_INPUT_TYPE_PHASOR;
		return M9kConstants.PMU_INPUT_TYPE_ANALOG;
		// END: 20-NOV-2017
	}

}
