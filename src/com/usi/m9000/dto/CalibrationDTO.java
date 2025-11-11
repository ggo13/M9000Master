/**
 * 
 */
package com.usi.m9000.dto;

import java.io.Serializable;

import com.usi.m9000.station.util.M9kStationConstants;

/**
 * @author sramasamy
 *
 */
public class CalibrationDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int calId;
	private String calTime;
	private StringBuffer calReport = new StringBuffer();
	private String calStatus;
	private String calStatusMsg;
	private String calAction = M9kStationConstants.CALIBRATE;
	public int getCalId() {
		return calId;
	}
	public void setCalId(int calId) {
		this.calId = calId;
	}
	public String getCalTime() {
		return calTime;
	}
	public void setCalTime(String calTime) {
		this.calTime = calTime;
	}
	public StringBuffer getCalReport() {
		return calReport;
	}
	public void setCalReport(StringBuffer calReport) {
		this.calReport = calReport;
	}
	public String getCalStatus() {
		return calStatus;
	}
	public void setCalStatus(String calStatus) {
		this.calStatus = calStatus;
	}
	public String getCalStatusMsg() {
		return calStatusMsg;
	}
	public void setCalStatusMsg(String calStatusMsg) {
		this.calStatusMsg = calStatusMsg;
	}
	@Override
	public String toString() {
		return "CalibrationDTO [calId=" + calId + ", calAction=" + calAction + ", calTime=" + calTime
				+ ", calStatus=" + calStatus
				+ ", calStatusMsg=" + calStatusMsg + ", calReport=" + calReport + "]";
	}
	public String getCalAction() {
		return calAction;
	}
	public void setCalAction(String calAction) {
		this.calAction = calAction;
	}
}
