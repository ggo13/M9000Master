package com.usi.m9000.dao;

import java.util.List;
import java.util.Map;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.AlarmLogsDTO;
import com.usi.m9000.dto.StationReportDTO;
import com.usi.m9000.util.M9kKeyValuePair;



public interface AlarmsLogDAO {
	public void insertAlarmDetails(AlarmLogsDTO alarmsDto);
	public Map<String, List<M9kKeyValuePair>> getAllStationAlarmsSummary();
	public Map<String, StationReportDTO> getStationAlarmsDetails();
	public List<AlarmLogsDTO> getAlarmsLogsToDisplay(int stationId, String startDate, String endDate, int alarmsId);
	public Map<String, List<AlarmLogsDTO>> getAbnormalStationAlarmsDetails();  // The alarms that stay abnormal for more than 24 hrs
	public void insertIntoMastersAlarmDetails(AlarmLogsDTO alarmsDto);
	public int getTotalAlarmsCount(int stationId) throws M9000Exception;
	public List<AlarmLogsDTO>  searchByCriteria(int stationId, String searchCriteria, String orderCriteria, int from, int to) throws M9000Exception;
	public int  countByCriteria(int stationId, String searchCriteria) throws M9000Exception;	

}
