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
public class SequenceTrigger extends AbstractTrigger{
	private String sequenceType;
	private String inputType;

	/**
	 * 
	 */
	public SequenceTrigger(String sequenceType) {
		this.sequenceType = sequenceType;
	}

	/**
	 * @return the inputType
	 */
	public String getInputType() {
		return inputType;
	}



	/**
	 * @param inputType the inputType to set
	 */
	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	@Override
	public String getInputXMLString() {
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
		inputTag.append(getSequenceTypePrefix());
		inputTag.append(getName());
		inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
		return inputTag.toString();		
	}

	public String getInputXMLString(String value) {
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
		inputTag.append(getSequenceTypePrefix());
		inputTag.append(getName());
		inputTag.append(value);
		return inputTag.toString();		
	}

	/**
	 * @return the sequenceType
	 */
	public String getSequenceType() {
		return sequenceType;
	}

	/**
	 * @param sequenceType the sequenceType to set
	 */
	public void setSequenceType(String sequenceType) {
		this.sequenceType = sequenceType;
	}
	
	private String getSequenceTypePrefix()
	{
		String prefix = "";
		if (getSequenceType() == null)
		{
			prefix = M9kXMLConstants.XML_ZEROSEQUENCE_PREFIX;
		}
		else if (getSequenceType().equals(M9kConstants.ZERO_SEQUENCE))
		{
			prefix = M9kXMLConstants.XML_ZEROSEQUENCE_PREFIX;
		}
		else if (getSequenceType().equals(M9kConstants.POSITIVE_SEQUENCE))
		{
			prefix = M9kXMLConstants.XML_POSITIVESEQUENCE_PREFIX;
		}
		else if (getSequenceType().equals(M9kConstants.NEGATIVE_SEQUENCE))
		{
			prefix = M9kXMLConstants.XML_NEGATIVESEQUENCE_PREFIX;
		}
		return prefix;
	}
	@Override
	public String getPmuInputType() {
		return M9kConstants.PMU_INPUT_TYPE_PHASOR;
	}

}
