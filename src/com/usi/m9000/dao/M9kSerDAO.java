package com.usi.m9000.dao;

import java.util.List;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.M9kSerDTO;



public interface M9kSerDAO {
	public List<M9kSerDTO> getLstOfSerData(int stationID, String startDateTime, String endDateTime, String events) throws M9000Exception;
	public List<M9kSerDTO> getLstOfSerDataForDates(int stationID, String startDate, String endDate, String events) throws M9000Exception;
	public List<M9kSerDTO> getLstOfSerDataForDates(int stationID, List<String>dates, String events) throws M9000Exception;
	public List<String> getAllAvailableSerDates(int stationID, int limit) throws M9000Exception;
	public List<M9kSerDTO> getAllAvailableEvents(int stationID) throws M9000Exception;
	public int getTotalSerCount(int stationId) throws M9000Exception;
	public List<M9kSerDTO>  searchByCriteria(int stationId, String searchCriteria, String orderCriteria, int from, int to) throws M9000Exception;
	public int  countByCriteria(int stationId, String searchCriteria) throws M9000Exception;
	public List<M9kSerDTO> getAllNewSerEvents(List<Integer> lstNewSerIds);
	public List<Integer> getNewSERIds();
	public int removeProcessedSerIds(List<Integer> lstId);
}
