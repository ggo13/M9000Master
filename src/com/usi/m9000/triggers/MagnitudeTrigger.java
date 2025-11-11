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
public class MagnitudeTrigger extends AbstractTrigger{
	
	private String harmonic;

	/**
	 * 
	 */
	public MagnitudeTrigger() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the harmonic
	 */
	public String getHarmonic() {
		return harmonic;
	}

	/**
	 * @param harmonic the harmonic to set
	 */
	public void setHarmonic(String harmonic) {
		this.harmonic = harmonic;
	}

	@Override
	public String getInputXMLString() {
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
		inputTag.append(M9kXMLConstants.XML_MAGNITUDE_PREFIX);
		inputTag.append(getName());
		inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
		return inputTag.toString();		
	}

	@Override
	public String getPmuInputType() {
		return M9kConstants.PMU_INPUT_TYPE_ANALOG;
	}
}
