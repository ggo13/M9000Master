/**
 * 
 */
package com.usi.m9000.dto;

import java.io.Serializable;

/**
 * @author sramasamy
 *
 */
public class ContinuousComtradeDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String contDataType;
	private String startDateTime;
	private String endDateTime;
	private String fileName;
	
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
	 * @return the contDataType
	 */
	public String getContDataType() {
		return contDataType;
	}
	/**
	 * @param contDataType the contDataType to set
	 */
	public void setContDataType(String contDataType) {
		this.contDataType = contDataType;
	}
	/**
	 * @return the startDateTime
	 */
	public String getStartDateTime() {
		return startDateTime;
	}
	/**
	 * @param startDateTime the startDateTime to set
	 */
	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}
	/**
	 * @return the endDateTime
	 */
	public String getEndDateTime() {
		return endDateTime;
	}
	/**
	 * @param endDateTime the endDateTime to set
	 */
	public void setEndDateTime(String endDateTime) {
		this.endDateTime = endDateTime;
	}
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ContinuousComtradeDTO [id=" + id + ", contDataType="
				+ contDataType + ", startDateTime=" + startDateTime
				+ ", endDateTime=" + endDateTime + ", fileName=" + fileName
				+ "]";
	}
	
	

}
