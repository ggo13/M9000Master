/**
 * 
 */
package com.usi.m9000.algorithms;

import com.usi.AlgorithmsDocument.Algorithms;
import com.usi.DFRDocument.DFR;
import com.usi.RmsDocument.Rms;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.triggers.AbstractTrigger;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kXMLConstants;

/**
 * @author sramasamy
 *
 */
public class RMSAlgorithm extends AbstractTrigger{
	
	DFR xmlDfr;
	AnalogChannelDTO analogChannelDto;
	/**
	 * 
	 */
	public RMSAlgorithm() {
	}

	public RMSAlgorithm(DFR xmlDfr, AnalogChannelDTO analogChannelDto)
	{
		this();
		this.xmlDfr = xmlDfr;
		this.analogChannelDto = analogChannelDto;
	}

	@Override
	public String getInputXMLString() {
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
		inputTag.append(M9kXMLConstants.XML_RMS_PREFIX);
		inputTag.append(getName());
		inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
		return inputTag.toString();
	}
	
	public Rms createRmsXmlObj()
	{
		Rms rms = getRmsIfAlreadyExists(xmlDfr);
		if (rms == null)
		{
			rms = xmlDfr.getDataPool().getAlgorithms().addNewRms();
			rms.setName(M9kConstants.RMS+"_"+analogChannelDto.getName());
			rms.setInput(getInputString());
		}

		return rms;
	}
	private Rms getRmsIfAlreadyExists(DFR xmlDfr)
	{
//		Iterator<String> mapXmlDfrIterator = mapXMLDfrs.keySet().iterator();
//		String dfrKey= null;
//		DFR xmlDfr = mapXMLDfrs.get(analogChannelDto.getChassis());
		Algorithms algorithms;
//		boolean booCont = true;
		Rms rms = null;
//		for (; booCont && mapXmlDfrIterator.hasNext();) {
//			dfrKey = mapXmlDfrIterator.next();
//			xmlDfr = mapXMLDfrs.get(dfrKey);
			algorithms = xmlDfr.getDataPool().getAlgorithms();
			if (algorithms == null)
			{
				algorithms = xmlDfr.getDataPool().addNewAlgorithms();
				return rms;
			}
			for (int i = 0; algorithms.getRmsArray() != null && i < algorithms.getRmsArray().length; i++) {
//				if (getName().equals(algorithms.getRmsArray(i).getName()))
				if (algorithms.getRmsArray(i).getName().endsWith(analogChannelDto.getName()))
				{
					rms = algorithms.getRmsArray(i);
					break;
				}
			}
//		}
		return rms;
	}

	public String getInputString()
	{
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_ANALOG_CHANNELS_INPUT);
		inputTag.append(analogChannelDto.getName());
		inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
		return inputTag.toString();
	}

	public AnalogChannelDTO getAnalogChannelDto() {
		return analogChannelDto;
	}

	public void setAnalogChannlDto(AnalogChannelDTO analogChannelDto) {
		this.analogChannelDto = analogChannelDto;
	}

	public DFR getXmlDfr() {
		return xmlDfr;
	}

	public void setXmlDfr(DFR xmlDfr) {
		this.xmlDfr = xmlDfr;
	}

	public String getName()
	{
		String rmsName = super.getName();
		if (rmsName == null)
		{
			rmsName = M9kConstants.RMS+"_"+analogChannelDto.getName();
		}
		return rmsName ;
	}

	@Override
	public String getPmuInputType() {
		return M9kConstants.PMU_INPUT_TYPE_ANALOG;
	}
}
