package com.usi.m9000.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;
import com.usi.m9000.station.util.LineChannels;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.M9kXMLConstants;
@Validations
public class AnalogChannelDTO implements Serializable,Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String channel;
	private String circuitName;
	private String circuitNameKey;
	private String range;
	private String scale;
	private String offset;
	private String inputType;
	private String phase; //TODO: Change this to char
	private String primaryRatio = "1";
	private String secondaryRatio = "1";
	private String ctpt;
	private String extShunt;
	private String displayName;
	private String displayChannel;
	private boolean disableExtSunt;
	private boolean exportStatus;
	private String chassis;
	// For virtual channels implementation
	private List<AnalogChannelDTO> lstAnalogsForVirtual = new ArrayList<AnalogChannelDTO>();
	private String chassisChannelNo;
	private int virtualChannelNo;
	private String operator; // Arithmetic operator add or subtract
	private String virtualLogic; // If virtual then logic involved e.g. A1 + A2 -A3
	private String virtualLogicToolTip;
	private boolean virtual; // set to true if it is virtual
	private boolean transducer = false;
	private String transducerUnits;
	private double inP1=0.0;
	private double outP1=0.0;
	private double inP2=0.0;
	private double outP2=0.0;
	// START: 23-Feb-2021 - Delta Transformer implementation
	private double virtualScale = 1.0;
	
	// New Phase options like AB, BC, CA
	private Map<String, String> mapPhaseTypes;
	// END: 19-Mar-2021
	
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(AnalogChannelDTO.class);
	/**
	 * 
	 */
	public AnalogChannelDTO() {
		// TODO Auto-generated constructor stub
	}
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
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}
	/**
	 * @return the circuitName
	 */
	@RequiredStringValidator(type=ValidatorType.FIELD, fieldName="circuitName", key="errors.required", message="")
	public String getCircuitName() {
		return StringEscapeUtils.unescapeXml(circuitName);
	}
	/**
	 * @param circuitName the circuitName to set
	 */
	public void setCircuitName(String circuitName) {
		this.circuitName = StringEscapeUtils.escapeXml(circuitName);
	}
	/**
	 * @return the range
	 */
	public String getRange() {
		return range;
	}
	/**
	 * @param range the range to set
	 */
	public void setRange(String range) {
		this.range = range;
	}
	/**
	 * @return the scale
	 */
	public String getScale() {
		return scale;
	}
	/**
	 * @param scale the scale to set
	 */
	public void setScale(String scale) {
		this.scale = scale;
	}
	/**
	 * @return the offset
	 */
	public String getOffset() {
		return offset;
	}
	/**
	 * @param offset the offset to set
	 */
	public void setOffset(String offset) {
		this.offset = offset;
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
	/**
	 * @return the phase
	 */
	public String getPhase() {
		// Saravanan: 07-Mar-2013 To change N/A to NA
		if (phase != null && phase.indexOf("/") != -1){
			phase = M9kConstants.NO_PHASE;
		}
		return phase;
	}
	/**
	 * @param phase the phase to set
	 */
	public void setPhase(String phase) {
		// Saravanan: 07-Mar-2013 To change N/A to NA
		if (phase != null && phase.indexOf("/") != -1){
			phase = M9kConstants.NO_PHASE;
		}
		this.phase = phase;
	}
	/**
	 * @return the primaryRatio
	 */
	public String getPrimaryRatio() {
		return primaryRatio;
	}
	/**
	 * @param primaryRatio the primaryRatio to set
	 */
	public void setPrimaryRatio(String primaryRatio) {
		this.primaryRatio = primaryRatio;
	}
	/**
	 * @return the secondaryRatio
	 */
	public String getSecondaryRatio() {
		return secondaryRatio;
	}
	/**
	 * @param secondaryRatio the secondaryRatio to set
	 */
	public void setSecondaryRatio(String secondaryRatio) {
		this.secondaryRatio = secondaryRatio;
	}
	/**
	 * @return the ctpt
	 */
	public String getCtpt() {
		return ctpt;
	}
	/**
	 * @param ctpt the ctpt to set
	 */
	public void setCtpt(String ctpt) {
		this.ctpt = ctpt;
	}
	/**
	 * @return the circuitNameKey
	 */
	public String getCircuitNameKey() {
		return circuitNameKey;
	}
	/**
	 * @param circuitNameKey the circuitNameKey to set
	 */
	public void setCircuitNameKey(String circuitNameKey) {
		this.circuitNameKey = circuitNameKey;
	}
	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		
		String units;
		if (getInputType() != null && getInputType().startsWith(M9kConstants.VOLTAGE))
		{
			units = "V";
		}
		else
		{
			units = "I";
		}
		// START: 15-Mar-2021 - To improve User readability suggested by Todd!
		
//		displayName = getChassis()+"-"+getDisplayChannel()+" - "+getCircuitName()+" -"+units+getPhase();
		displayName = "["+M9kUtils.rightpadChassisName(getChassis())+M9kUtils.leftpadAnalogChannel(getDisplayChannel())+M9kUtils.leftpad(units+getPhase(),4)+"]    "+getCircuitName();
		
//		logger.debug("Display Name to be returned for analog "+displayName+" |");
		// END: 15-Mar-2021 - To improve User readability suggested by Todd!
		return displayName;
	}
	
	public String getUnalignedDisplayName() {
		
		String units;
		if (getInputType() != null && getInputType().startsWith(M9kConstants.VOLTAGE))
		{
			units = "V";
		}
		else
		{
			units = "I";
		}
		// START: 15-Mar-2021 - To improve User readability suggested by Todd!
		
		displayName = getChassis()+"-"+getDisplayChannel()+" - "+getCircuitName()+" -"+units+getPhase();
//		displayName = "["+M9kUtils.rightpad(getChassis(),6)+M9kUtils.leftpad(getDisplayChannel(),7)+M9kUtils.leftpad(units+getPhase(),3)+"]    "+getCircuitName();
		
		// END: 15-Mar-2021 - To improve User readability suggested by Todd!
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String toString()
	{
		return getDisplayName();
	}
	/**
	 * @return the displayChannel
	 */
	public String getDisplayChannel() {
		if (getLstAnalogsForVirtual() == null || getLstAnalogsForVirtual().isEmpty())
		{
			displayChannel = "A"+getChannel();
		}
		else
		{
			displayChannel = M9kConstants.VIRTUAL_PREFIX+getVirtualChannelNo();
		}
		return displayChannel;
	}
	/**
	 * @param displayChannel the displayChannel to set
	 */
	public void setDisplayChannel(String displayChannel) {
		this.displayChannel = displayChannel;
	}
	/**
	 * @return the extShunt
	 */
	public String getExtShunt() {
		return extShunt;
	}
	/**
	 * @param extShunt the extShunt to set
	 */
	public void setExtShunt(String extShunt) {
		this.extShunt = extShunt;
	}
	/**
	 * @return the disableExtSunt
	 */
	public boolean isDisableExtSunt() {
		return disableExtSunt;
	}
	/**
	 * @return the disableExtSunt
	 */
	public boolean getDisableExtSunt() {
		return disableExtSunt;
	}

	/**
	 * @param disableExtSunt the disableExtSunt to set
	 */
	public void setDisableExtSunt(boolean disableExtSunt) {
		this.disableExtSunt = disableExtSunt;
	}
	
	/**
	 * @return the status
	 */
	public boolean isExportStatus() {
		return exportStatus;
	}
	
	/**
	 * @return the status
	 */
	public boolean getStatus() {
		return exportStatus;
	}

	/**
	 * @param status the status to set
	 */
	public void setExportStatus(boolean status) {
		this.exportStatus = status;
	}
	public String getChassis() {
		return chassis;
	}
	public void setChassis(String chassis) {
		this.chassis = chassis;
	}
	
	public LineChannels getLineChannel()
	{
		if (getInputType().startsWith(M9kStationConstants.VOLTAGE))
		{
			if (getPhase().equalsIgnoreCase("A"))
			{
				return LineChannels.CHAN_VA;
			}
			else if (getPhase().equalsIgnoreCase("B"))
			{
				return LineChannels.CHAN_VB;
			}
			else if (getPhase().equalsIgnoreCase("C"))
			{
				return LineChannels.CHAN_VC;
			}
			else if (getPhase().equalsIgnoreCase("N"))
			{
				return LineChannels.CHAN_IN;
			}
			else
			{
				return LineChannels.CHAN_REF;
			}
		}
		else
		{
			if (getPhase().equalsIgnoreCase("A"))
			{
				return LineChannels.CHAN_IA;
			}
			else if (getPhase().equalsIgnoreCase("B"))
			{
				return LineChannels.CHAN_IB;
			}
			else if (getPhase().equalsIgnoreCase("C"))
			{
				return LineChannels.CHAN_IC;
			}
			else if (getPhase().equalsIgnoreCase("N"))
			{
				return LineChannels.CHAN_IN;
			}
			else
			{
				return LineChannels.CHAN_REF;
			}
		}
	}
	/**
	 * @return the lstAnalogsForVirtual
	 */
	public List<AnalogChannelDTO> getLstAnalogsForVirtual() {
		return lstAnalogsForVirtual;
	}
	/**
	 * @param lstAnalogsForVirtual the lstAnalogsForVirtual to set
	 */
	public void setLstAnalogsForVirtual(List<AnalogChannelDTO> lstAnalogsForVirtual) {
		this.lstAnalogsForVirtual = lstAnalogsForVirtual;
	}
	
	public List<String> getLstVirtualInputValues()
	{
		StringBuffer inputTag ;
		AnalogChannelDTO inputVal ;
		List<String> lstInputTagValues = new ArrayList<String>(getLstAnalogsForVirtual().size());
		for (Iterator<AnalogChannelDTO> iterator = getLstAnalogsForVirtual().iterator(); iterator.hasNext();) {
			inputVal = iterator.next();
			inputTag = new StringBuffer();
			inputTag.append(M9kXMLConstants.XML_ANALOG_CHANNELS_INPUT);
			inputTag.append(inputVal.getName());
			inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
			lstInputTagValues.add(inputTag.toString());
		}
		return lstInputTagValues;
	}
	/**
	 * Display channel details for virtual channels 
	 * @return the chassisChannelNo
	 */
	public String getChassisChannelNo() {
		chassisChannelNo = chassis+" - "+M9kConstants.VIRTUAL_PREFIX+"-"+getChannel();
		return chassisChannelNo;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((displayName == null) ? 0 : displayName.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnalogChannelDTO other = (AnalogChannelDTO) obj;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		return true;
	}
	/**
	 * @return the virtualChannelNo
	 */
	public int getVirtualChannelNo() {
		return virtualChannelNo;
	}
	/**
	 * @param virtualChannelNo the virtualChannelNo to set
	 */
	public void setVirtualChannelNo(int virtualChannelNo) {
		this.virtualChannelNo = virtualChannelNo;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	/**
	 * @return the virtualLogic
	 */
	public String getVirtualLogic() {
		if(virtualLogic == null)
		{
			if (getLstAnalogsForVirtual() != null && !getLstAnalogsForVirtual().isEmpty())
			{
				StringBuffer virtLogToolTip = new StringBuffer();
				AnalogChannelDTO analogChannelDTO;
				StringBuffer virtLogicBuf = null;
				for (Iterator<AnalogChannelDTO> iterator = lstAnalogsForVirtual.iterator(); iterator.hasNext();) {
					 analogChannelDTO = iterator.next();
					 logger.debug("In getVirtualLogic... analogChannelDTO "+analogChannelDTO+" opertaor "+analogChannelDTO.getOperator());
					 if(virtLogicBuf == null)
					 {
						 virtLogicBuf = new StringBuffer();
						 if (analogChannelDTO.getOperator() != null && analogChannelDTO.getOperator().equalsIgnoreCase("-"))
						 {
							 virtLogicBuf.append(" "+analogChannelDTO.getOperator()+" ");
						 }
					 }
					 else
					 {
						 if (analogChannelDTO.getOperator() != null )
						 {
							 virtLogicBuf.append(" "+analogChannelDTO.getOperator()+" ");
						 }
						 else
						 {
							 virtLogicBuf.append(" + ");
						 }
					 }
					 virtLogicBuf.append(analogChannelDTO.getDisplayChannel());
					 virtLogToolTip.append(analogChannelDTO.getDisplayName());
					 virtLogToolTip.append(M9kConstants.NEWLINE);
					 
				}
				// START: 17-Mar-2021 - Adding scale to virtual logic
				if (getVirtualScale() != 1.0)
				{
					virtLogicBuf.insert(0, "(");
					virtLogicBuf.append(") * "+getVirtualScale());
				}
				// END: 17-Mar-2021 - Adding scale to virtual logic
				if (virtLogicBuf != null && virtLogicBuf.length() > 0)
				{
					virtualLogic = virtLogicBuf.toString();
					setVirtualLogicToolTip(virtLogToolTip.toString());
				}
			}
		}
		return virtualLogic;
	}
	/**
	 * @param virtualLogic the virtualLogic to set
	 */
	public void setVirtualLogic(String virtualLogic) {
		this.virtualLogic = virtualLogic;
	}
	
	public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
	/**
	 * @return the virtualLogicToolTip
	 */
	public String getVirtualLogicToolTip() {
		if (virtualLogicToolTip == null || virtualLogicToolTip.isEmpty())
		{
			updateVirtualLogicToolTip();
		}
		return virtualLogicToolTip;
	}
	private void updateVirtualLogicToolTip() {
		getVirtualLogic();
		
	}
	/**
	 * @param virtualLogicToolTip the virtualLogicToolTip to set
	 */
	public void setVirtualLogicToolTip(String virtualLogicToolTip) {
		this.virtualLogicToolTip = virtualLogicToolTip;
	}
	public boolean isVirtual() {
		return virtual;
	}
	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}
	public boolean isTransducer() {
		return transducer;
	}
	public void setTransducer(boolean transducer) {
		this.transducer = transducer;
	}
	public String getTransducerUnits() {
		return transducerUnits;
	}
	public void setTransducerUnits(String transducerUnits) {
		this.transducerUnits = transducerUnits;
	}
	public double getInP1() {
		return inP1;
	}
	public void setInP1(double inP1) {
		this.inP1 = inP1;
	}
	public double getInP2() {
		return inP2;
	}
	public void setInP2(double inP2) {
		this.inP2 = inP2;
	}
	public double getOutP1() {
		return outP1;
	}
	public void setOutP1(double outP1) {
		this.outP1 = outP1;
	}
	public double getOutP2() {
		return outP2;
	}
	public void setOutP2(double outP2) {
		this.outP2 = outP2;
	}
	public double getVirtualScale() {
		return virtualScale;
	}
	public void setVirtualScale(double virtualScale) {
		this.virtualScale = virtualScale;
	}
	/**
	 * @return the mapPhaseTypes
	 */
	public Map<String, String> getMapPhaseTypes() {
		if (getInputType() == null || getInputType().indexOf(M9kConstants.VOLTAGE) != -1)
		{
			mapPhaseTypes = M9kUtils.getMapAnalogVoltagePhaseTypes();
		}
		else
		{
			mapPhaseTypes = M9kUtils.getMapAnalogCurrentPhaseTypes();
		}
		return mapPhaseTypes;
	}
	/**
	 * @param mapPhaseTypes the mapPhaseTypes to set
	 */
	public void setMapPhaseTypes(Map<String, String> mapPhaseTypes) {
		this.mapPhaseTypes = mapPhaseTypes;
	}

}
