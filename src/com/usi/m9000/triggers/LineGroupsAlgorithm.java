package com.usi.m9000.triggers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.LineGroupChannelsDTO;
import com.usi.m9000.station.util.LineChannels;
import com.usi.m9000.util.M9kXMLConstants;

public class LineGroupsAlgorithm implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private List<AnalogChannelDTO> inputChannels;
	private String type;
	private boolean local;
	private String chassis;
	private Integer id = 0;
	private String enableAutoCalc = "no";
	private double positiveResistance = 0.0;
	private double positiveReactance = 0.0;
	private double zeroResistance = 0.0;
	private double zeroReactance = 0.0;
	private double lineMiles;
	private String decisionLogic="";
	private double lattitude;
	private double longitude;
	private List<LineGroupChannelsDTO> lstLgChannels;
//	private static int nextAvailableId = 0;
	private Map<Integer, LineChannels> mapLineChannels = null;
	private String lineGroupName; // To store user entered descriptions
	// START: 16-Oct-2019 Implemented a comments section for line group where customer can add custom name value pair (Georgia Power)
	private String Comments;
	private boolean mixedVoltageChannels; // If line-neutral and line-line are mixed in the line group
	private boolean invalidCombination; // Linegroup shouldn't have more than one channel of same phase
	private boolean missingAVoltageChannel; // Missing voltage channel will be automatically calculated
	private boolean missingACurrentChannel; // Missing voltage channel will be automatically calculated
	private boolean missingTwoCurrentChannels; // Missing voltage channel will be automatically calculated
	private Map<String, String> mapLinegroupWarningMessages = new HashMap<String, String>(); // Warning messages for each line group
	
	public LineGroupsAlgorithm() {
	}

	public List<String> getLstInputTagValues()
	{
		StringBuffer inputTag ;
		AnalogChannelDTO inputVal ;
		List<String> lstInputTagValues = new ArrayList<String>(getInputChannels().size());
		for (Iterator<AnalogChannelDTO> iterator = getInputChannels().iterator(); iterator.hasNext();) {
			inputVal = iterator.next();
			inputTag = new StringBuffer();
			inputTag.append(M9kXMLConstants.XML_ANALOG_CHANNELS_INPUT);
			inputTag.append(inputVal.getName());
			inputTag.append(M9kXMLConstants.XML_VALUE_INPUT);
			lstInputTagValues.add(inputTag.toString());
		}
		return lstInputTagValues;
	}
	
//	public static int getNextAvailableId()
//	{
//		return (++nextAvailableId);
//	}
//	
//	public static void initializeLineGroupId(int linegroupId)
//	{
//		nextAvailableId = linegroupId;
//	}
//	public static void decrementId()
//	{
//		--nextAvailableId;
//	}

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
	 * @return the inputChannels
	 */
	public List<AnalogChannelDTO> getInputChannels() {
		return inputChannels;
	}

	/**
	 * @param inputChannels the inputChannels to set
	 */
	public void setInputChannels(List<AnalogChannelDTO> inputValues) {
		this.inputChannels = inputValues;
		if (!this.inputChannels.isEmpty())
		{
			classifyLineChannels();
		}
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

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	public String getEnableAutoCalc() {
		return enableAutoCalc;
	}

	public void setEnableAutoCalc(String enableAutoCalc) {
		this.enableAutoCalc = enableAutoCalc;
	}

	public double getPositiveResistance() {
		return positiveResistance;
	}

	public void setPositiveResistance(double positiveResistance) {
		this.positiveResistance = positiveResistance;
	}

	public double getPositiveReactance() {
		return positiveReactance;
	}

	public void setPositiveReactance(double positiveReactance) {
		this.positiveReactance = positiveReactance;
	}

	public double getZeroResistance() {
		return zeroResistance;
	}

	public void setZeroResistance(double zeroResistance) {
		this.zeroResistance = zeroResistance;
	}

	public double getZeroReactance() {
		return zeroReactance;
	}

	public void setZeroReactance(double zeroReactance) {
		this.zeroReactance = zeroReactance;
	}

	public String getDecisionLogic() {
		return decisionLogic;
	}

	public void setDecisionLogic(String decisionLogic) {
		this.decisionLogic = decisionLogic;
	}

	public List<LineGroupChannelsDTO> getLstLgChannels() {
		return lstLgChannels;
	}

	public void setLstLgChannels(List<LineGroupChannelsDTO> lstLgChannels) {
		this.lstLgChannels = lstLgChannels;
	}

	public String getChassis() {
		return chassis;
	}

	public void setChassis(String chassis) {
		this.chassis = chassis;
	}

	public double getLineMiles() {
		return lineMiles;
	}

	public void setLineMiles(double lineMiles) {
		this.lineMiles = lineMiles;
	}

	public double getLattitude() {
		return lattitude;
	}

	public void setLattitude(double lattitude) {
		this.lattitude = lattitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public Map<Integer, LineChannels> getMapLineChannels() {
		return mapLineChannels;
	}

	public void setMapLineChannels(Map<Integer, LineChannels> mapLineChannels) {
		this.mapLineChannels = mapLineChannels;
	}
	
	private void classifyLineChannels()
	{
		if (inputChannels != null && !inputChannels.isEmpty() && (inputChannels.size() != 7 || inputChannels.size() != 8))
		{
			mapLineChannels = new HashMap<Integer, LineChannels>(8);
			AnalogChannelDTO analogChannelDTO;
			for (Iterator<AnalogChannelDTO> iterator = inputChannels.iterator(); iterator.hasNext();) {
				analogChannelDTO =  iterator.next();
				if (mapLineChannels.get(analogChannelDTO.getLineChannel().getChannelId()) != null)
				{
					mapLineChannels.clear();
					break;
				}
				else
				{
					mapLineChannels.put(Integer.parseInt(analogChannelDTO.getChannel()), analogChannelDTO.getLineChannel());
				}
			}
		}
	}
	public boolean isFaultLocEligible()
	{
		boolean isEligible = false;
		if (mapLineChannels != null && !mapLineChannels.isEmpty())
		{
			isEligible = true;
		}
		return isEligible;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

	/**
	 * @return the lineGroupName
	 */
	public String getLineGroupName() {
		return lineGroupName;
	}

	/**
	 * @param lineGroupName the lineGroupName to set
	 */
	public void setLineGroupName(String lineGroupName) {
		this.lineGroupName = lineGroupName;
	}

	/**
	 * @return the displayName
	 * To diplay name on the drop down
	 */
	public String getDisplayName() {
		return getName()+" - "+getLineGroupName();
	}

	public String getComments() {
		return Comments;
	}

	public void setComments(String comments) {
		Comments = comments;
	}

	/**
	 * @return the mixedVoltageChannels
	 */
	public boolean isMixedVoltageChannels() {
		return mixedVoltageChannels;
	}

	/**
	 * @param mixedVoltageChannels the mixedVoltageChannels to set
	 */
	public void setMixedVoltageChannels(boolean mixedVoltageChannels) {
		this.mixedVoltageChannels = mixedVoltageChannels;
	}

	/**
	 * @return the mapLinegroupWarningMessages
	 */
	public Map<String, String> getMapLinegroupWarningMessages() {
		return mapLinegroupWarningMessages;
	}

	/**
	 * @param mapLinegroupWarningMessages the mapLinegroupWarningMessages to set
	 */
	public void setMapLinegroupWarningMessages(Map<String, String> mapLinegroupWarningMessages) {
		this.mapLinegroupWarningMessages = mapLinegroupWarningMessages;
	}

	/**
	 * @return the invalidCombination
	 */
	public boolean isInvalidCombination() {
		return invalidCombination;
	}

	/**
	 * @param invalidCombination the invalidCombination to set
	 */
	public void setInvalidCombination(boolean invalidCombination) {
		this.invalidCombination = invalidCombination;
	}

	/**
	 * @return the missingAVoltageChannel
	 */
	public boolean isMissingAVoltageChannel() {
		return missingAVoltageChannel;
	}

	/**
	 * @param missingAVoltageChannel the missingAVoltageChannel to set
	 */
	public void setMissingAVoltageChannel(boolean missingAVoltageChannel) {
		this.missingAVoltageChannel = missingAVoltageChannel;
	}

	/**
	 * @return the missingACurrentChannel
	 */
	public boolean isMissingACurrentChannel() {
		return missingACurrentChannel;
	}

	/**
	 * @param missingACurrentChannel the missingACurrentChannel to set
	 */
	public void setMissingACurrentChannel(boolean missingACurrentChannel) {
		this.missingACurrentChannel = missingACurrentChannel;
	}

	/**
	 * @return the missingTwoCurrentChannels
	 */
	public boolean isMissingTwoCurrentChannels() {
		return missingTwoCurrentChannels;
	}

	/**
	 * @param missingTwoCurrentChannels the missingTwoCurrentChannels to set
	 */
	public void setMissingTwoCurrentChannels(boolean missingTwoCurrentChannels) {
		this.missingTwoCurrentChannels = missingTwoCurrentChannels;
	}
	
	
	
}
