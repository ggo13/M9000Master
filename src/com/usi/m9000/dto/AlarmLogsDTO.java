package com.usi.m9000.dto;

import java.io.Serializable;


public class AlarmLogsDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer alarmId;
	private String ledName; 
	private String ledStatus; // Default value 
	private String relays;
	private StringBuffer alarmDescription;
	private String alarmTime;
	private Integer stationId;
	private String currentStatus; // Normal or Abnormal
	// System details of the config XML
	
	
	public AlarmLogsDTO()
	{
		alarmDescription = new StringBuffer();
	}

	public Integer getAlarmId() {
		return alarmId;
	}

	public void setAlarmId(Integer alarmId) {
		this.alarmId = alarmId;
	}

	public String getLedName() {
		return ledName;
	}

	public void setLedName(String ledName) {
		this.ledName = ledName;
	}

	public String getLedStatus() {
		return ledStatus;
	}

	public void setLedStatus(String ledStatus) {
		this.ledStatus = ledStatus;
	}

	public String getRelays() {
		return relays;
	}

	public void setRelays(String relays) {
		this.relays = relays;
	}

	public String getAlarmDescription() {
		return alarmDescription.toString();
	}

	public void setAlarmDescription(StringBuffer alarmDescription) {
		this.alarmDescription = alarmDescription;
	}

	public String getAlarmTime() {
		return alarmTime;
	}

	public void setAlarmTime(String alarmTime) {
		this.alarmTime = alarmTime;
	}

	@Override
	public String toString() {
		return "AlarmLogsDTO [ledName=" + ledName + ", ledStatus=" + ledStatus
				+ ", relays=" + relays + ", alarmDescription="
				+ alarmDescription + "]";
	}

	/**
	 * @return the stationId
	 */
	public Integer getStationId() {
		return stationId;
	}

	/**
	 * @param stationId the stationId to set
	 */
	public void setStationId(Integer stationId) {
		this.stationId = stationId;
	}

	/**
	 * @return the currentStatus
	 */
	public String getCurrentStatus() {
		return currentStatus;
	}

	/**
	 * @param currentStatus the currentStatus to set
	 */
	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alarmId == null) ? 0 : alarmId.hashCode());
		result = prime * result
				+ ((alarmTime == null) ? 0 : alarmTime.hashCode());
		result = prime * result
				+ ((currentStatus == null) ? 0 : currentStatus.hashCode());
		result = prime * result + ((ledName == null) ? 0 : ledName.hashCode());
		result = prime * result
				+ ((ledStatus == null) ? 0 : ledStatus.hashCode());
		result = prime * result + ((relays == null) ? 0 : relays.hashCode());
		result = prime * result
				+ ((stationId == null) ? 0 : stationId.hashCode());
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
		AlarmLogsDTO other = (AlarmLogsDTO) obj;
		if (alarmId == null) {
			if (other.alarmId != null)
				return false;
		} else if (!alarmId.equals(other.alarmId))
			return false;
		if (alarmTime == null) {
			if (other.alarmTime != null)
				return false;
		} else if (!alarmTime.equals(other.alarmTime))
			return false;
		if (currentStatus == null) {
			if (other.currentStatus != null)
				return false;
		} else if (!currentStatus.equals(other.currentStatus))
			return false;
		if (ledName == null) {
			if (other.ledName != null)
				return false;
		} else if (!ledName.equals(other.ledName))
			return false;
		if (ledStatus == null) {
			if (other.ledStatus != null)
				return false;
		} else if (!ledStatus.equals(other.ledStatus))
			return false;
		if (relays == null) {
			if (other.relays != null)
				return false;
		} else if (!relays.equals(other.relays))
			return false;
		if (stationId == null) {
			if (other.stationId != null)
				return false;
		} else if (!stationId.equals(other.stationId))
			return false;
		return true;
	}


}
