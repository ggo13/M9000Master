/**
 * 
 */
package com.usi.m9000.dto;

import java.util.List;

/**
 * @author sramasamy
 *
 */
public class M9kStationHealthStatusDTO {
private int stationId;
private String status;
private List<M9kDfrHealthDTO> lstDfrHealthDTO;
public int getStationId() {
	return stationId;
}
public void setStationId(int stationId) {
	this.stationId = stationId;
}
public List<M9kDfrHealthDTO> getLstDfrHealthDTO() {
	return lstDfrHealthDTO;
}
public void setLstDfrHealthDTO(List<M9kDfrHealthDTO> lstDfrHealthDTO) {
	this.lstDfrHealthDTO = lstDfrHealthDTO;
}
public String getStatus() {
	return status;
}
public void setStatus(String status) {
	this.status = status;
}
}
