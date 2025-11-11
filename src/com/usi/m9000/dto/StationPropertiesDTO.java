/**
 * 
 */
package com.usi.m9000.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sramasamy
 *
 */
class StationPropertiesDTO
{
	List<String> lstAlarmRelaysMapping;
	int triggerDuration = 10; // In Seconds. Default set the trigger relays for 10 seconds.
	List<HealthLimits> lstHealthSettings = new ArrayList<HealthLimits>();
	public StationPropertiesDTO()
	{
		
	}
	public List<String> getLstAlarmRelaysMapping() {
		return lstAlarmRelaysMapping;
	}
	public void setLstAlarmRelaysMapping(List<String> lstAlarmRelaysMapping) {
		this.lstAlarmRelaysMapping = lstAlarmRelaysMapping;
	}
	public int getTriggerDuration() {
		return triggerDuration;
	}
	public void setTriggerDuration(int triggerDuration) {
		this.triggerDuration = triggerDuration;
	}
	public List<HealthLimits> getLstHealthSettings() {
		return lstHealthSettings;
	}
	public void setLstHealthSettings(List<HealthLimits> lstHealthSettings) {
		this.lstHealthSettings = lstHealthSettings;
	}
	class HealthLimits
	{
		int highLimit;
		int lowLimit;
		int diskCleanupLimit;
		int diskStopRecordingLimit;
		int MinDaysOfFaultsToRetain;
	}

}

