/**
 * 
 */
package com.usi.m9000.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * @author sramasamy
 *
 */
public class TriggerSourceDTO implements Serializable{
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private String sourceName;
private String sourceType;
private String sourceDisplayName;
private String sourceValue;
private String sourceInputObjName;
private String sourceTriggerName;
private String sourceLineGroupType;
private int sourceLineGroupId;
private String chassis;
//START: 24-Jan-2020 - Implementing Transducer
private boolean transducer=false;
//END: 24-Jan-2020
// Start: 17-Mar-2021 - Implementing Apparent power algorithm
private Map<String, String> mapTriggerInputTypes;
//End: 17-Mar-2021 - Implementing Apparent power algorithm
/**
 * @return the sourceName
 */
public String getSourceName() {
	return sourceName;
}
/**
 * @param sourceName the sourceName to set
 */
public void setSourceName(String sourceName) {
	this.sourceName = sourceName;
}
/**
 * @return the sourceType
 */
public String getSourceType() {
	return sourceType;
}
/**
 * @param sourceType the sourceType to set
 */
public void setSourceType(String sourceType) {
	this.sourceType = sourceType;
}
/**
 * @return the sourceDisplayName
 */
public String getSourceDisplayName() {
	return sourceDisplayName;
}
/**
 * @param sourceDisplayName the sourceDisplayName to set
 */
public void setSourceDisplayName(String sourceDisplayName) {
	this.sourceDisplayName = sourceDisplayName;
}
/**
 * @return the sourceValue
 */
public String getSourceValue() {
	sourceValue = sourceName+"-"+sourceType+"-"+sourceTriggerName; 
	return sourceValue;
}
/**
 * @param sourceValue the sourceValue to set
 */
public void setSourceValue(String sourceValue) {
	int index = sourceValue.indexOf("-");
	this.sourceName = sourceValue.substring(0, index);
	int secondIndex = sourceValue.indexOf("-", index+1);
	this.sourceType = sourceValue.substring(index+1, secondIndex-1);
	this.sourceTriggerName = sourceValue.substring(secondIndex+1);
//	System.out.println("Source Name: "+sourceName+" Source type: "+sourceType);
	
}
public String toString()
{
	StringBuffer strBuff = new StringBuffer();
	strBuff.append(" SourceName: "+getSourceName());
	strBuff.append(" Source Type: "+getSourceType());
	strBuff.append(" Source Value: "+getSourceValue());
	strBuff.append(" Source Display Name: "+getSourceDisplayName());
	return strBuff.toString();
}
/**
 * @return the sourceInputObjName
 */
public String getSourceInputObjName() {
	return sourceInputObjName;
}
/**
 * @param sourceInputObjName the sourceInputObjName to set
 */
public void setSourceInputObjName(String sourceInputObjName) {
	this.sourceInputObjName = sourceInputObjName;
}
/**
 * @return the sourceTriggerName
 */
public String getSourceTriggerName() {
	return sourceTriggerName;
}
/**
 * @param sourceTriggerName the sourceTriggerName to set
 */
public void setSourceTriggerName(String sourceTriggerName) {
	this.sourceTriggerName = sourceTriggerName;
}
/**
 * @return the sourceLineGroupType
 */
public String getSourceLineGroupType() {
	return sourceLineGroupType;
}
/**
 * @param sourceLineGroupType the sourceLineGroupType to set
 */
public void setSourceLineGroupType(String sourceLineGroupType) {
	this.sourceLineGroupType = sourceLineGroupType;
}
public int getSourceLineGroupId() {
	return sourceLineGroupId;
}
public void setSourceLineGroupId(int sourceLineGroupId) {
	this.sourceLineGroupId = sourceLineGroupId;
}
public String getChassis() {
	return chassis;
}
public void setChassis(String chassis) {
	this.chassis = chassis;
}
public boolean isTransducer() {
	return transducer;
}
public void setTransducer(boolean transducer) {
	this.transducer = transducer;
}
public Map<String, String> getMapTriggerInputTypes() {
	return mapTriggerInputTypes;
}
public void setMapTriggerInputTypes(Map<String, String> mapTriggerInputTypes) {
	this.mapTriggerInputTypes = mapTriggerInputTypes;
}
}
