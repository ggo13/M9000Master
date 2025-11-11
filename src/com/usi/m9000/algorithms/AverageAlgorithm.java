/**
 * 
 */
package com.usi.m9000.algorithms;

import com.usi.AlgorithmsDocument.Algorithms;
import com.usi.AverageDocument.Average;
import com.usi.DFRDocument.DFR;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.triggers.AbstractTrigger;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kXMLConstants;

/**
 * @author sramasamy
 *
 */
public class AverageAlgorithm extends AbstractTrigger{
	
	DFR xmlDfr;
	AnalogChannelDTO analogChannelDto;
	/**
	 * 
	 */
	public AverageAlgorithm() {
	}

	public AverageAlgorithm(DFR xmlDfr, AnalogChannelDTO analogChannelDto)
	{
		this();
		this.xmlDfr = xmlDfr;
		this.analogChannelDto = analogChannelDto;
	}

	@Override
	public String getInputXMLString() {
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
		inputTag.append(M9kXMLConstants.XML_AVERAGE_PREFIX);
		inputTag.append(getName());
		inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
		return inputTag.toString();
	}
	
	public Average createAverageXmlObj()
	{
		Average average = getAverageIfAlreadyExists(xmlDfr);
		if (average == null)
		{
			average = xmlDfr.getDataPool().getAlgorithms().addNewAverage();
			average.setName(M9kConstants.AVERAGE+"_"+analogChannelDto.getName());
			average.setInput(getInputString());
		}

		return average;
	}
	private Average getAverageIfAlreadyExists(DFR xmlDfr)
	{
//		Iterator<String> mapXmlDfrIterator = mapXMLDfrs.keySet().iterator();
//		String dfrKey= null;
//		DFR xmlDfr = mapXMLDfrs.get(analogChannelDto.getChassis());
		Algorithms algorithms;
//		boolean booCont = true;
		Average average = null;
//		for (; booCont && mapXmlDfrIterator.hasNext();) {
//			dfrKey = mapXmlDfrIterator.next();
//			xmlDfr = mapXMLDfrs.get(dfrKey);
			algorithms = xmlDfr.getDataPool().getAlgorithms();
			if (algorithms == null)
			{
				algorithms = xmlDfr.getDataPool().addNewAlgorithms();
				return average;
			}
			
			for (int i = 0; algorithms.getAverageArray() != null && i < algorithms.getAverageArray().length; i++) {
				if (getName().equals(algorithms.getAverageArray(i).getName()))
				{
					average = algorithms.getAverageArray(i);
					break;
				}
			}
//		}
		return average;
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
		String averageName = super.getName();
		if (averageName == null)
		{
			averageName = M9kConstants.AVERAGE+"_"+analogChannelDto.getName();
		}
		return averageName ;
	}

	@Override
	public String getPmuInputType() {
		return M9kConstants.PMU_INPUT_TYPE_ANALOG;
	}
}
