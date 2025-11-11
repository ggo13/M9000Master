/**
 * 
 */
package com.usi.m9000.algorithms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.usi.m9000.dto.TriggerChannelDTO;
import com.usi.m9000.triggers.AbstractTrigger;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kXMLConstants;

/**
 * @author sramasamy
 *
 */
public class VirtualMeasurementAlgorithm extends AbstractTrigger{
//	private M9000XmlConfig m9kConfig;
	private String description;
	private double globalScale;
	private List<VirtualMeasurementInput> lstVirtualMeasurementInput = new ArrayList<VirtualMeasurementInput>();
	private String virtualLogic = null;
	private List<TriggerChannelDTO> lstDfrSpecificMeasurementChannels;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(VirtualMeasurementAlgorithm.class);
	
	public class VirtualMeasurementInput
	{
		private TriggerChannelDTO inputMeasurement;
		private double scale;
		
		public VirtualMeasurementInput(TriggerChannelDTO inputMeasurement, double scale) {
			super();
			this.inputMeasurement = inputMeasurement;
			this.scale = scale;
		}
		public TriggerChannelDTO getInputMeasurement() {
			return inputMeasurement;
		}
		public void setInputMeasurement(TriggerChannelDTO inputMeasurement) {
			this.inputMeasurement = inputMeasurement;
		}
		public double getScale() {
			return scale;
		}
		public void setScale(double scale) {
			this.scale = scale;
		}
		
		public String getInputXMLString()
		{
			return inputMeasurement.getAlgoritmInputXMLString();
		}
		@Override
		public String toString() {
			return "VirtualMeasurementInput [inputMeasurement=" + inputMeasurement + ", scale=" + scale + "]";
		}
		
		
	}
	/**
	 * Called to construct VirtualAlgorithm from xml
	 * 
	 */
	public VirtualMeasurementAlgorithm()
	{
		
	}
	/**
	 * Called when needed to save the virtualAlgorithm into xml
	 */
	public VirtualMeasurementAlgorithm(List<TriggerChannelDTO> lstDfrSpecificMeasurementChannels) {
//		m9kConfig = new M9000XmlConfig();
////		 m9kConfig.setSubstation(currentSubstation);
//		try {
//			m9kConfig.loadStationXmlFile(new StringReader(M9kUtils.getStationDetails().getConfigXml()));
//		} catch (M9000Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		lstAllMeasurementChannels  = m9kConfig.getLstOfMeasurements();
		super();
		setLstDfrSpecificMeasurementChannels(lstDfrSpecificMeasurementChannels);
	}

	
	
	@Override
	public String getInputXMLString() {
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
		inputTag.append(M9kXMLConstants.XML_VIRTUALMEASUREMENT_PREFIX);
		inputTag.append(getName());
		inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
		return inputTag.toString();
	}
	
	@Override
	public String getPmuInputType() {
		return M9kConstants.PMU_INPUT_TYPE_ANALOG;
	}

	public double getGlobalScale() {
		return globalScale;
	}

	public void setGlobalScale(double globalScale) {
		this.globalScale = globalScale;
	}

	public String getVirtualLogic() {
		logger.debug("Is getVirtualLogic() null?? "+virtualLogic); 
		if (virtualLogic == null)
		{
			virtualLogic = "";
			VirtualMeasurementInput virtualMeasurementInput;
			for (Iterator<VirtualMeasurementInput> iterator = getLstVirtualMeasurementInput().iterator(); iterator.hasNext();) {
				virtualMeasurementInput =  iterator.next();
				if (virtualLogic.isEmpty())
				{
					virtualLogic = "((";
				}
				else
				{
					virtualLogic += " + (";
				}
				virtualLogic+=virtualMeasurementInput.getScale()+" * "+virtualMeasurementInput.getInputMeasurement().getMeasurementKey()+"))";
			}
			if (globalScale != 0)
			{
				virtualLogic = virtualLogic+" * "+globalScale;
			}
		}
		logger.debug("Virtual logic derived..."+virtualLogic);
		return virtualLogic;
	}

	public void setVirtualLogic(String virtualLogic) {
		this.virtualLogic = virtualLogic;
		setLstVirtualMeasurementInput(parseVirtualLogic(virtualLogic));
	}



	public List<VirtualMeasurementInput> getLstVirtualMeasurementInput() {
		logger.debug("list of virtual measurements "+lstVirtualMeasurementInput+" virtual logic "+virtualLogic);
		if (lstVirtualMeasurementInput == null || lstVirtualMeasurementInput.isEmpty())
		{
			if (virtualLogic != null &&  virtualLogic.isEmpty())
			{
				setLstVirtualMeasurementInput(parseVirtualLogic(virtualLogic));
			}
		}
		return lstVirtualMeasurementInput;
	}



	public void setLstVirtualMeasurementInput(List<VirtualMeasurementInput> lstVirtualMeasurementInput) {
		this.lstVirtualMeasurementInput = lstVirtualMeasurementInput;
	}
	
	public List<VirtualMeasurementInput> parseVirtualLogic(String virtualLogic)
	{
		logger.debug("About to parse virtual logic "+virtualLogic);
		String parseAlgorithm;
		List<VirtualMeasurementInput> lstParsedInputs = new ArrayList<VirtualMeasurementInput>();
		if (virtualLogic != null && !virtualLogic.isEmpty() && virtualLogic.indexOf("(") != -1) 
		{
			Matcher matcher = Pattern.compile("\\((.*?)\\)",Pattern.DOTALL).matcher(virtualLogic.substring(virtualLogic.indexOf("(")+1, virtualLogic.lastIndexOf(")")));
			VirtualMeasurementInput virtualMeasurementInput;
			String inputMeasurementKey;
			double scale;
			if (!virtualLogic.trim().endsWith(")"))
			{
				setGlobalScale(Double.parseDouble(virtualLogic.trim().substring(virtualLogic.lastIndexOf("*")+1).trim())); 
			}
			TriggerChannelDTO inputMeasurementChannel;
			while(matcher.find())
			{
				parseAlgorithm = matcher.group(1);
			    logger.debug("First match in algorithm "+parseAlgorithm);
			    scale = Double.parseDouble(parseAlgorithm.substring(0, parseAlgorithm.indexOf("*")).trim());
			    inputMeasurementKey = parseAlgorithm.substring(parseAlgorithm.indexOf("*")+1).trim();
			    inputMeasurementChannel = getInputMeasurement(inputMeasurementKey);
			    if (inputMeasurementChannel != null)
			    {
			    	virtualMeasurementInput = new VirtualMeasurementInput(inputMeasurementChannel, scale);
			    	lstParsedInputs.add(virtualMeasurementInput);
			    }
			}
		}
		return lstParsedInputs;
	}
	
	private TriggerChannelDTO getInputMeasurement(String inputMeasurementName)
	{
		TriggerChannelDTO matchedInputName = null;
		if (getLstDfrSpecificMeasurementChannels() != null && !getLstDfrSpecificMeasurementChannels().isEmpty())
		{
			for (Iterator<TriggerChannelDTO> iterator = getLstDfrSpecificMeasurementChannels().iterator(); iterator.hasNext();) {
				matchedInputName = iterator.next();
				logger.debug("inputMeasurementName "+inputMeasurementName+" Key to compare to "+matchedInputName.getMeasurementKey());
				if (matchedInputName.getMeasurementKey().equalsIgnoreCase(inputMeasurementName) ||
						matchedInputName.getTriggerKey().equalsIgnoreCase(inputMeasurementName))
				{
					logger.debug("Match found "+matchedInputName);
					return matchedInputName;
				}
			}
		}
		return null;
	}



	public List<TriggerChannelDTO> getLstDfrSpecificMeasurementChannels() {
		return lstDfrSpecificMeasurementChannels;
	}



	public void setLstDfrSpecificMeasurementChannels(List<TriggerChannelDTO> lstDfrSpecificMeasurementChannels) {
		this.lstDfrSpecificMeasurementChannels = lstDfrSpecificMeasurementChannels;
	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	@Override
	public String toString() {
		return "VirtualMeasurementAlgorithm [description=" + description + ", globalScale=" + globalScale
				+ ", lstVirtualMeasurementInput=" + lstVirtualMeasurementInput + ", virtualLogic=" + virtualLogic + "]";
	}

}
