/**
 * 
 */
package com.usi.m9000.dto;

import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;

/**
 * @author sramasamy
 *
 */
public class M9kSerDTO {
	private int stationId;
	private int eventNum;
	private String phase;
	private String name;
	private long timeStamp;
	private int normal;
	private int state;
	private int locked;
	private String displayTime;
	private String currentStateAsString;
	private String defaultStateAsString;
	private String status;
	private String lockedAsString;
	private String displayName;
	private String stationName; // To export station name to csv
	private int id; // DB id to delete after pdf export
	/**
	 * @return the eventNum
	 */
	public int getEventNum() {
		return eventNum;
	}
	/**
	 * @param eventNum the eventNum to set
	 */
	public void setEventNum(int eventNum) {
		this.eventNum = eventNum;
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
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}
	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	/**
	 * @return the normal
	 */
	public int getNormal() {
		return normal;
	}
	/**
	 * @param normal the normal to set
	 */
	public void setNormal(int normal) {
		this.normal = normal;
		setDefaultStateAsString(normal);
	}
	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}
	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		this.state = state;
//		setCurrentStateAsString(state);
	}
	/**
	 * @return the locked
	 */
	public int getLocked() {
		return locked;
	}
	/**
	 * @param locked the locked to set
	 */
	public void setLocked(int locked) {
		this.locked = locked;
		setLockedAsString(locked);
	}
	
	/**
	 * @return the displayTime
	 */
	public String getDisplayTime() {
	     String microseconds = (""+getTimeStamp());
	     if (microseconds.length() > 10)
	     {
//	     System.out.println("Get time stamp at DTO "+getTimeStamp());
	     microseconds = microseconds.substring(microseconds.length()-3);
	  // Start: 31-Jan-2013 All date conversion are in M9kUtils class
//	     java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SSS"); 
//		  sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	     java.text.SimpleDateFormat sdf  = M9kUtils.getDateFormat("MM/dd/yyyy - HH:mm:ss.SSS");
		// End: 31-Jan-2013
//	     displayTime = new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SSS").format(new java.util.Date (getTimeStamp()/1000));
		  displayTime = sdf.format(new java.util.Date (getTimeStamp()/1000));
	     displayTime+=microseconds;
//	     System.out.println("Display time tot return "+displayTime);
	     }
		return displayTime;
	}
	/**
	 * @param displayTime the displayTime to set
	 */
	public void setDisplayTime(String displayTime) {
		this.displayTime = displayTime;
	}

	public String toString()
	{
		return getEventNum()+" - "+getName();
	}
	public int getStationId() {
		return stationId;
	}
	public void setStationId(int stationId) {
		this.stationId = stationId;
	}
	/**
	 * @return the currentStateAsString
	 */
	public String getCurrentStateAsString() {
		if ((getNormal()^getState()) == 0)
		{
			currentStateAsString = M9kConstants.OPEN_STATE;
		}
		else
		{
			currentStateAsString = M9kConstants.CLOSE_STATE;
		}
//		if (getState() == 0)
//		{
//			if (getNormal() == 0)
//			{
//				currentStateAsString = M9kConstants.OPEN_STATE;
//			}
//			else
//			{
//				currentStateAsString = M9kConstants.CLOSE_STATE;
//			}
//
//		}
//		else
//		{
//			// Calculate abnormal state based on current state and normal state
//			if (getNormal() == 0)
//			{
//				currentStateAsString = M9kConstants.CLOSE_STATE;
//			}
//			else
//			{
//				currentStateAsString = M9kConstants.OPEN_STATE;
//			}
//		}

		return currentStateAsString;
	}
	/**
	 * @param currentStateAsString the currentStateAsString to set
	 */
	public void setCurrentStateAsString(int currentState) {
		if (currentState == 0)
		{
			currentStateAsString = M9kConstants.OPEN_STATE; 
		}
		else
		{
			currentStateAsString = M9kConstants.CLOSE_STATE;
		}
	}
	/**
	 * @return the normalStateAsString
	 */
	public String getDefaultStateAsString() {
		return defaultStateAsString;
	}
	/**
	 * @param defaultStateAsString the normalStateAsString to set
	 */
	public void setDefaultStateAsString(int normalState) {
		if (normalState == 0)
		{
			defaultStateAsString = M9kConstants.OPEN_STATE; 
		}
		else
		{
			defaultStateAsString = M9kConstants.CLOSE_STATE;
		}

	}
	
	public String getStatus()
	{
		// START: 12-Oct-2015 As DFR is already comparing we just need to interpret already compared state.
//		if (getState() == getNormal())
		if (getState() == 0)
		// End: 12-Oct-2015
		{
			status = M9kConstants.NORMAL;
		}
		else
		{
			status = M9kConstants.ABNORMAL;
		}
		return status;
	}
	/**
	 * @return the lockedAsString
	 */
	public String getLockedAsString() {
		return lockedAsString;
	}
	/**
	 * @param lockedAsString the lockedAsString to set
	 */
	public void setLockedAsString(int locked) {
		if (locked == 1)
		{
			lockedAsString = M9kConstants.YES;
		}
		else
		{
			lockedAsString = M9kConstants.NO;
		}
	}
	public String getDisplayName() {
		displayName = getEventNum()+" - "+getName();
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
}
