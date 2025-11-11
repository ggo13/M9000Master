package com.usi.m9000.dao;

import java.util.List;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.StationReportDTO;



public interface StationDAO {
	public List<StationDTO> getStationsList() throws M9000Exception;
	public List<StationDTO> getManagedStations() throws M9000Exception;
	public void updateConfigXml(StationDTO stationDto) throws M9000Exception;
	public void insertStationDetails(StationDTO stationDto) throws M9000Exception;
	public boolean isStationExists(int stationId) throws M9000Exception;
	public String getConfigXml(int stationId) throws M9000Exception;
	public String getLocalConfigXml() throws M9000Exception;
	public StationDTO getStationsDetails(int stationId) throws M9000Exception;
	public void updateFaultLocationDetails(int stationId, int faultId, String faultLocationDetails) throws M9000Exception;
	public List<StationReportDTO> getLstStationForReports() throws M9000Exception;
	public void removeStation(int stationId) throws M9000Exception;
	public void importStationConfig (int stationId, String sqlContent) throws M9000Exception;
	public void removeAllStationsForLocal()  throws M9000Exception;
	// 03-Nov-2021 - Hierarchy implementation - Get the list of stations for a given zone
	public List<StationDTO> getZonesStationsList(int zoneId) throws M9000Exception;
}
