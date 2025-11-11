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
public class PhasorTrigger extends AbstractTrigger{
	
	private String name;
	/**
	 * 
	 */
	public PhasorTrigger() {
		// TODO Auto-generated constructor stub
	}


	@Override
	public String getInputXMLString() {
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
		inputTag.append(M9kXMLConstants.XML_PHASOR_PREFIX);
		inputTag.append(getName());
		inputTag.append(M9kXMLConstants.XML_PHASOR_VALUE_INPUT);
		return inputTag.toString();
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getPmuInputType() {
		return M9kConstants.PMU_INPUT_TYPE_PHASOR;
	}

}
