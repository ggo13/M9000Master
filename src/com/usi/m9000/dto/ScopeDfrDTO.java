/**
 * 
 */
package com.usi.m9000.dto;

/**
 * @author sramasamy
 *
 */
public class ScopeDfrDTO {

	String ipAddress;
	Integer analogChannelStart;
	Integer analogChannelEnd;
	/**
	 * 
	 */
	public ScopeDfrDTO() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}
	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	/**
	 * @return the analogChannelStart
	 */
	public Integer getAnalogChannelStart() {
		return analogChannelStart;
	}
	/**
	 * @param analogChannelStart the analogChannelStart to set
	 */
	public void setAnalogChannelStart(Integer analogChannelStart) {
		this.analogChannelStart = analogChannelStart;
	}
	/**
	 * @return the analogChannelEnd
	 */
	public Integer getAnalogChannelEnd() {
		return analogChannelEnd;
	}
	/**
	 * @param analogChannelEnd the analogChannelEnd to set
	 */
	public void setAnalogChannelEnd(Integer analogChannelEnd) {
		this.analogChannelEnd = analogChannelEnd;
	}
	
	public String toString()
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("Ip Address: "+getIpAddress()+"\n");
		strBuff.append("Starting Channel: "+getAnalogChannelStart()+"\n");
		strBuff.append("Ending Channel: "+getAnalogChannelEnd()+"\n");
		
		return strBuff.toString();
	}

}
