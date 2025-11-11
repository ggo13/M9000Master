package com.usi.m9000.dto;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.usi.m9000.algorithms.AverageAlgorithm;
import com.usi.m9000.algorithms.RMSAlgorithm;
import com.usi.m9000.triggers.AbstractTrigger;
import com.usi.m9000.triggers.FrequencyTrigger;
import com.usi.m9000.triggers.MagnitudeTrigger;
import com.usi.m9000.triggers.PhasorTrigger;
import com.usi.m9000.triggers.PowerTrigger;
import com.usi.m9000.triggers.SequenceTrigger;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.util.M9kXMLConstants;

public class TriggerChannelDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;
	private String inputChannelName;
	private String channel;
	private String phase;
	private String type;
	private String start;
	private String duration="0";
	private String tripIfOver;
	private String tripIfUnder;
	// START: 25-Feb-2020 - ROC positive and Negative Limits implementation
//	private String tripIfGt="0";
	private String tripRocPos="";
	private String tripRocNeg="";
	// END: 25-Feb-2020
	private String priority;
	private String chatterLimit;
	private String chatterRate;
	private String triggerLimit;
	private String harmonic;
	private boolean disableHarmonic;
	private boolean disableTripOver;
	private boolean disableTripUnder;
	private boolean status;
	private String inputType;
	private boolean exportStatus;
	private Integer exportRate;
	private String units; 
	private Integer average = null;
	private String chassis;
	private Integer globalLineGroupId; 
	private String displayName;
	private String triggerKey; // used for Fault location decision logic
	private boolean pmuStatus; // Used for PMU
	private boolean triggerStatus; // New implentation for PMU
	private boolean freqPmuStatus = false;
	private String measurementName;
	private String powerType;
	// START: 25-Feb-2020 - ROC positive and Negative Limits implementation
//	private boolean disableTripIfGt;
	private boolean disableTripRoc; // Disables both ROC Positive and Negative Limit 
	// END: 25-Feb-2020
	private boolean disableDuration;
	// START: 01-May-2018 All exports to be part of DDR instead of just RMS - ReleaseV1.0.8.2
	private boolean ddrStatus; 
	// END: 01-May-2018 
	//START: 24-Jan-2020 - Implementing Transducer
	private boolean transducer=false;
	//END: 24-Jan-2020
	
	// START: 03-Mar-2021 - Implementation of virtual measurements for delta transformers 
	private String virtualLogic; // Logic defined for each virtual measurement like A1 + A2 - A3
	// 17-Mar-2021 - apparent power implementation - list to Map
	private Map<String, String> mapTriggerInputTypes;
  // END: 03-Mar-2021 - Implementation of virtual measurements for delta transformers
	
	// START: 30-Dec-2022 - Disturbance alarm implementation
	private boolean disturbanceAlarm = false;
	// END: 30-Dec-2022 - Disturbance alarm implementation
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(TriggerChannelDTO.class);
	/**
	 * 
	 */
	public TriggerChannelDTO() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return StringEscapeUtils.unescapeXml(name);
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = StringEscapeUtils.escapeXml(name);
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
//		int index = channel.indexOf("-");
		this.channel = channel;//.substring(0, index);
//		this.inputType = channel.substring(index+1);
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the start
	 */
	public String getStart() {
		return start;
	}
	/**
	 * @param start the start to set
	 */
	public void setStart(String start) {
		this.start = start;
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
	/**
	 * @return the tripIfOver
	 */
	public String getTripIfOver() {
		return tripIfOver;
	}
	/**
	 * @param tripIfOver the tripIfOver to set
	 */
	public void setTripIfOver(String tripIfOver) {
		this.tripIfOver = tripIfOver;
	}
	/**
	 * @return the tripIfUnder
	 */
	public String getTripIfUnder() {
		return tripIfUnder;
	}
	/**
	 * @param tripIfUnder the tripIfUnder to set
	 */
	public void setTripIfUnder(String tripIfUnder) {
		this.tripIfUnder = tripIfUnder;
	}
//	/**
//	 * @return the tripIfGt
//	 */
//	public String getTripIfGt() {
//		return tripIfGt;
//	}
//	/**
//	 * @param tripIfGt the tripIfGt to set
//	 */
//	public void setTripIfGt(String tripIfGt) {
//		this.tripIfGt = tripIfGt;
//	}
	public String getTripRocPos() {
		return tripRocPos;
	}
	public void setTripRocPos(String tripRocPos) {
		this.tripRocPos = tripRocPos;
	}
	public String getTripRocNeg() {
		return tripRocNeg;
	}
	public void setTripRocNeg(String tripRocNeg) {
		this.tripRocNeg = tripRocNeg;
	}
	/**
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}
	/**
	 * @param priority the priority to set
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}
	/**
	 * @return the chatterLimit
	 */
	public String getChatterLimit() {
		return chatterLimit;
	}
	/**
	 * @param chatterLimit the chatterLimit to set
	 */
	public void setChatterLimit(String chatterLimit) {
		this.chatterLimit = chatterLimit;
	}
	/**
	 * @return the chatterRate
	 */
	public String getChatterRate() {
		return chatterRate;
	}
	/**
	 * @param chatterRate the chatterRate to set
	 */
	public void setChatterRate(String chatterRate) {
		this.chatterRate = chatterRate;
	}
	/**
	 * @return the triggerLimit
	 */
	public String getTriggerLimit() {
		return triggerLimit;
	}
	/**
	 * @param triggerLimit the triggerLimit to set
	 */
	public void setTriggerLimit(String triggerLimit) {
		this.triggerLimit = triggerLimit;
	}
	/**
	 * @return the phase
	 */
	public String getPhase() {
		return phase;
	}
	/**
	 * @param phase the phase to set
	 */
	public void setPhase(String phase) {
		this.phase = phase;
	}
	/**
	 * @return the disableHarmonic
	 */
	public boolean getDisableHarmonic() {
		return disableHarmonic;
	}
	/**
	 * @param disableHarmonic the disableHarmonic to set
	 */
	public void setDisableHarmonic(boolean disableHarmonic) {
		this.disableHarmonic = disableHarmonic;
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
	
	public String getUniqueName()
	{
		return getChannel()+"_"+getName();
	}
	/**
	 * @return the status
	 */
	public boolean isStatus() {
		return status;
	}
	
	/**
	 * @return the status
	 */
	public boolean getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getTriggerChannelInput()
	{
		StringBuffer inputTag = new StringBuffer();
//		logger.info("Input Type: "+getInputType());
		if (getInputType() != null && getInputType().equalsIgnoreCase(M9kConstants.TRIGGER_INPUT_TYPE_ANALOG))
		{
			inputTag.append(M9kXMLConstants.XML_ANALOG_CHANNELS_INPUT);
		}
		else
		{
			inputTag.append(M9kXMLConstants.XML_LINEGROUP_CHANNELS_INPUT);
		}
		inputTag.append(getInputChannelName());
		inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
		return inputTag.toString();
	}
	public String toString()
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("Id:\t\t"+getId());
		strBuff.append("Name:\t\t"+getName());
		strBuff.append("Analog Channel Name:\t\t"+getInputChannelName());
		strBuff.append("Trigger Input Type:\t\t"+getInputType());
		strBuff.append("Channel:\t\t"+getChannel());
		strBuff.append("Trigger Channel Input:\t\t"+getTriggerChannelInput());
		strBuff.append("Type:\t\t"+getType());
		strBuff.append("Phase:\t\t"+getPhase());
		strBuff.append("TriggerIfUnder:\t"+getTripIfUnder());
		strBuff.append("triggerIfOver:\t"+getTripIfOver());
		strBuff.append("Chatter Limit:\t"+getChatterLimit());
		strBuff.append("Chatter Rate:\t"+getChatterRate());
		strBuff.append("Trigger Limit:\t"+getTriggerLimit());
		strBuff.append("Chassis:\t"+getChassis());
		strBuff.append("Trigger input Types:\t"+getMapTriggerInputTypes());
		strBuff.append("Trigger Export status :\t"+getExportStatus());
//		strBuff.append("Trigger Export rate :\t"+getExportRate());
		strBuff.append("Measurement Name :\t"+getMeasurementName());
		strBuff.append("Virtual logic :\t\t"+getVirtualLogic());
		return strBuff.toString();
	}
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the inputChannelName
	 */
	public String getInputChannelName() {
		return inputChannelName;
	}
	/**
	 * @param inputChannelName the inputChannelName to set
	 */
	public void setInputChannelName(String inputChannelName) {
		this.inputChannelName = inputChannelName;
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
	 * @return the disableTripOver
	 */
	public boolean getDisableTripOver() {
		return disableTripOver;
	}
	/**
	 * @param disableTripOver the disableTripOver to set
	 */
	public void setDisableTripOver(boolean disableTripOver) {
		this.disableTripOver = disableTripOver;
	}
	/**
	 * @return the disableTripUnder
	 */
	public boolean getDisableTripUnder() {
		return disableTripUnder;
	}
	/**
	 * @param disableTripUnder the disableTripUnder to set
	 */
	public void setDisableTripUnder(boolean disableTripUnder) {
		this.disableTripUnder = disableTripUnder;
	}
	/**
	 * @return the exportStatus
	 */
	public boolean isExportStatus() {
		return exportStatus;
	}
	/**
	 * @param exportStatus the exportStatus to set
	 */
	public void setExportStatus(boolean exportStatus) {
		this.exportStatus = exportStatus;
	}
	
	/**
	 * @return the exportStatus
	 */
	public boolean getExportStatus() {
		return exportStatus;
	}
	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}
	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	public String getExportXMLInputString(AbstractTrigger genericTrigger)
	{
		StringBuffer inputTag = new StringBuffer();
		inputTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
		// START: 26-Jan-2020 - Transducer implementation
		// 23-Jul-2020 - Bug Fix: Units value messes the algorithm
		if(isTransducer())
		{
			inputTag.append(M9kXMLConstants.XML_AVERAGE_PREFIX);
		}
		// END: 23-Jul-2020
		else if (genericTrigger instanceof SequenceTrigger)
		{
			inputTag.append(((SequenceTrigger) genericTrigger).getSequenceType()+"(");
		}
		else if (getType().equalsIgnoreCase(M9kConstants.PHASOR))
		{
			inputTag.append(M9kConstants.PHASE+"(");
		}
		else if (getType().equalsIgnoreCase(M9kConstants.HARMONIC))
		{
			inputTag.append(M9kXMLConstants.XML_MAGNITUDE_PREFIX);
		}
		else if (getType().endsWith(M9kConstants.WATTS) || getType().endsWith(M9kConstants.VARS)
				// START: 17-Mar-2021 - Apparent power implementation
				|| getType().endsWith(M9kConstants.VA))
			// END: 17-Mar-2021 - Apparent power implementation
		{
			inputTag.append(M9kXMLConstants.XML_POWER_PREFIX);
		}
		else
		{
			inputTag.append(getType()+"(");
		}
		inputTag.append(genericTrigger.getName());
		if (getType()!= null && getType().equalsIgnoreCase(M9kConstants.PHASOR))
		{
			inputTag.append(M9kXMLConstants.XML_PHASOR_VALUE_INPUT);
		}
		else
		{
			inputTag.append(M9kXMLConstants.EXPORT_XML_VALUE_INPUT);
		}
		
		return inputTag.toString();
	}
	/**
	 * @return the exportRate
	 */
	public Integer getExportRate() {
		return exportRate;
	}
	/**
	 * @param exportRate the exportRate to set
	 */
	public void setExportRate(Integer exportRate) {
		this.exportRate = exportRate;
	}
	public Integer getAverage() {
		return average;
	}
	public void setAverage(Integer average) {
		this.average = average;
	}
	public String getChassis() {
		return chassis;
	}
	public void setChassis(String chassis) {
		this.chassis = chassis;
	}
	public Integer getGlobalLineGroupId() {
		return globalLineGroupId;
	}
	public void setGlobalLineGroupId(Integer globalLineGroupId) {
		this.globalLineGroupId = globalLineGroupId;
	}
	public void setGlobalLineGroupId(String globalLineGroupId) {
		logger.warn("No line group id");
	}
	public String getDisplayName() {
		if (getType()!= null && getType().equalsIgnoreCase("E"))
		{
			displayName = "["+M9kUtils.rightpad(getName(), 5)+"]    "+getInputChannelName();
		}
		else
		{
			displayName = "[T"+M9kUtils.rightpad(""+getId(), 4)+"]    "+getName();
		}
		return displayName;
	}
	
	/**
	 * Get the list prefixed with M for virtual measurements
	 * @return
	 */
	public String getDisplayNameForVirtualMeasurments() {
		if (getType()!= null && getType().equalsIgnoreCase("E"))
		{
			displayName = getName()+"-"+getInputChannelName();
		}
		else
		{
			displayName = "[M"+M9kUtils.rightpad(""+getId(), 4)+"]    "+getName();
		}
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getTriggerKey() {
		if (getType()!= null && getType().equalsIgnoreCase("E"))
		{
			triggerKey = "E"+getId();
		}
		else
		{
			triggerKey = "T"+getId();
		}
		return triggerKey;
	}
/**
 * Return measurement key with prefix M for virtual measurements
 * @param triggerKey
 */
	public String getMeasurementKey() {
		if (getType()!= null && getType().equalsIgnoreCase("E"))
		{
			triggerKey = "E"+getId();
		}
		else
		{
			triggerKey = "M"+getId();
		}
		return triggerKey;
	}

	public void setTriggerKey(String triggerKey) {
		this.triggerKey = triggerKey;
	}
	public boolean isPmuStatus() {
		return pmuStatus;
	}
	public void setPmuStatus(boolean pmuStatus) {
		this.pmuStatus = pmuStatus;
	}
	public boolean isTriggerStatus() {
		return triggerStatus;
	}
	public void setTriggerStatus(boolean triggerStatus) {
		this.triggerStatus = triggerStatus;
	}
	public boolean isFreqPmuStatus() {
		return freqPmuStatus;
	}
	public void setFreqPmuStatus(boolean freqPmuStatus) {
		this.freqPmuStatus = freqPmuStatus;
	}
	public String getMeasurementName() {
		return measurementName;
	}
	public void setMeasurementName(String measurementName) {
		this.measurementName = measurementName;
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
//	/**
//	 * @return the disableTripIfGt
//	 */
//	public boolean isDisableTripIfGt() {
//		return disableTripIfGt;
//	}
//	/**
//	 * @param disableTripIfGt the disableTripIfGt to set
//	 */
//	public void setDisableTripIfGt(boolean disableTripIfGt) {
//		this.disableTripIfGt = disableTripIfGt;
//	}
	public boolean isDisableTripRoc() {
		return disableTripRoc;
	}
	public void setDisableTripRoc(boolean disableTripRoc) {
		this.disableTripRoc = disableTripRoc;
	}
	/**
	 * @return the disableDuration
	 */
	public boolean isDisableDuration() {
		return disableDuration;
	}
	/**
	 * @param disableDuration the disableDuration to set
	 */
	public void setDisableDuration(boolean disableDuration) {
		this.disableDuration = disableDuration;
	}
	public boolean isDdrStatus() {
		return ddrStatus;
	}
	public void setDdrStatus(boolean ddrStatus) {
		this.ddrStatus = ddrStatus;
	}
	public boolean isTransducer() {
		return transducer;
	}
	public void setTransducer(boolean transducer) {
		this.transducer = transducer;
	}
	public String getVirtualLogic() {
		return virtualLogic;
	}
	public void setVirtualLogic(String virtualLogic) {
		this.virtualLogic = virtualLogic;
	}
	
	/**
	 * 06-Mar-2021 - Virtual measurements for delta transformer 
	 * Returns the XML input string of the corresponding algorithm type
	 */
	public String getAlgoritmInputXMLString()
	{
			AbstractTrigger genericTrigger = null;
			if (getType().equalsIgnoreCase(M9kConstants.AVERAGE) || isTransducer())
			{
				logger.debug("Inside "+M9kConstants.AVERAGE);
				genericTrigger = new AverageAlgorithm();
//				genericTrigger.setName(M9kConstants.RMS+"_"+triggerChannelDTO.getId());
			}
			//END: 23-Jul-2020

			else if (getType().equalsIgnoreCase(M9kConstants.RMS))
			{
				logger.debug("Inside "+M9kConstants.RMS);
				genericTrigger = new RMSAlgorithm();
//				genericTrigger.setName(M9kConstants.RMS+"_"+getId());
			}
			else if (getType().equals(M9kConstants.HARMONIC))
			{
				logger.debug("Inside "+M9kConstants.HARMONIC);
				genericTrigger = new MagnitudeTrigger();
//				genericTrigger.setName(M9kConstants.MAGNITUDE+"_"+triggerChannelDTO.getId());
				((MagnitudeTrigger)genericTrigger).setHarmonic(getHarmonic());
			}
			else if (getType().equals(M9kConstants.FREQUENCY))
			{
				logger.debug("Inside "+M9kConstants.FREQUENCY);
				genericTrigger = new FrequencyTrigger();
//				genericTrigger.setName(M9kConstants.FREQUENCY+"_"+getId());
//				((FrequencyTrigger)genericTrigger).setPhaseName(M9kConstants.PHASE+"_"+getInputChannelName());
			}
			else if (getType().equals(M9kConstants.ZERO_SEQUENCE_V) ||
					getType().equals(M9kConstants.ZERO_SEQUENCE_I))
			{
				logger.debug("Inside "+M9kConstants.ZERO_SEQUENCE);
				genericTrigger = new SequenceTrigger(M9kConstants.ZERO_SEQUENCE);
				if (getType().indexOf(M9kConstants.VOLTAGE) != -1)
				{
//					genericTrigger.setName(M9kConstants.ZSV+getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.VOLTAGE);
				}
				else
				{
//					genericTrigger.setName(M9kConstants.ZSI+getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.CURRENT);
				}

			}
			else if (getType().equals(M9kConstants.POSITIVE_SEQUENCE_V) || 
					getType().equals(M9kConstants.POSITIVE_SEQUENCE_I))
			{
				logger.debug("Inside "+M9kConstants.POSITIVE_SEQUENCE);
				genericTrigger = new SequenceTrigger(M9kConstants.POSITIVE_SEQUENCE);
				if (getType().indexOf(M9kConstants.VOLTAGE) != -1)
				{
//					genericTrigger.setName(M9kConstants.PSV+getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.VOLTAGE);
				}
				else
				{
//					genericTrigger.setName(M9kConstants.PSI+getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.CURRENT);
				}

			}
			else if (getType().startsWith(M9kConstants.NEGATIVE_SEQUENCE_V) ||
					getType().startsWith(M9kConstants.NEGATIVE_SEQUENCE_I))
			{
				logger.debug("Inside "+M9kConstants.NEGATIVE_SEQUENCE);
				genericTrigger = new SequenceTrigger(M9kConstants.NEGATIVE_SEQUENCE);
				if (getType().indexOf(M9kConstants.VOLTAGE) != -1)
				{
//					genericTrigger.setName(M9kConstants.NSV+getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.VOLTAGE);
				}
				else
				{
//					genericTrigger.setName(M9kConstants.NSI+getId());
					((SequenceTrigger)genericTrigger).setInputType(M9kConstants.CURRENT);
				}

			}
			else if (getType().equalsIgnoreCase(M9kConstants.PHASOR))
			{
				genericTrigger = new PhasorTrigger();
//				genericTrigger.setName(M9kConstants.PHASE+getId());
			}
			else if (getType().indexOf(M9kConstants.WATTS) != -1)
			{
				genericTrigger = new PowerTrigger(M9kConstants.WATTS_REAL);
			}
			else if (getType().indexOf(M9kConstants.VARS) != -1)
			{
				genericTrigger = new PowerTrigger(M9kConstants.VARS_REACTIVE);
			}
			// START: 17-Mar-2021 - Apparent power implementation
			else if (getType().toLowerCase().indexOf(M9kConstants.VA_APPARENT.toLowerCase()) != -1)
			{
				genericTrigger = new PowerTrigger(M9kConstants.VA_APPARENT);
			}
			// END: 17-Mar-2021 - Apparent power implementation
			logger.debug("VIRTUALMEASUREMENTINPUT: getMeasurementName() "+getMeasurementName());
			logger.debug("VIRTUALMEASUREMENTINPUT:  getName() "+getName());
			genericTrigger.setName(getMeasurementName());
			return genericTrigger.getInputXMLString();
	}
	public Map<String, String> getMapTriggerInputTypes() {
		return mapTriggerInputTypes;
	}
	public void setMapTriggerInputTypes(Map<String, String> mapTriggerInputTypes) {
		this.mapTriggerInputTypes = mapTriggerInputTypes;
	}
	public boolean isDisturbanceAlarm() {
		return disturbanceAlarm;
	}
	public void setDisturbanceAlarm(boolean disturbanceAlarm) {
		this.disturbanceAlarm = disturbanceAlarm;
	}
}