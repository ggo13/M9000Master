package com.usi.m9000.dao;

import java.util.List;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.ComtradeDataDTO;



public interface ComtradeDataDAO {
	public void insertComtradeDetails(ComtradeDataDTO comtradeDataDTO) throws M9000Exception;
	public List<ComtradeDataDTO> getLstOfComtradeDetails(int stationId) throws M9000Exception;
	public List<ComtradeDataDTO> getLstOfComtradeDetails(int stationId, String startDate, String endDate, List<ComtradeDataDTO> lstComtradeDataDto, String lineGroupsToSearch) throws M9000Exception;
	public void insertErrorComtradeDetails(ComtradeDataDTO comtradeDataDTO) throws M9000Exception;
	public void updateComments(String stationId, ComtradeDataDTO comtradeDataDTO) throws M9000Exception;
	public int updateFaultsBooleanLogic(int stationId) throws M9000Exception; 
	public int deleteFaults(int stationId, String commaSeperatedFaultIds) throws M9000Exception;
	public int getTotalFaultsCount(int stationId) throws M9000Exception;
	public List<ComtradeDataDTO>  searchByCriteria(int stationId, String searchCriteria, String orderCriteria, int from, int to) throws M9000Exception;
	public int  countByCriteria(int stationId, String searchCriteria) throws M9000Exception;
	public ComtradeDataDTO  findById(int stationId, int faultId) throws M9000Exception;
	public List<ComtradeDataDTO>  findLesserAsId(int stationId, int faultId, int from, int to) throws M9000Exception;
	public List<ComtradeDataDTO>  findGreaterAsId(int stationId, int faultId, int from, int to) throws M9000Exception;
	public List<ComtradeDataDTO>  getFaults(int stationId, int from, int to) throws M9000Exception;
	public List<ComtradeDataDTO>  getFaultFileNames(int stationId, String csvFaultIds) throws M9000Exception;
	public List<String> getDistinctActiveEvents(int stationId) throws M9000Exception;
	public ComtradeDataDTO getLocalFaultById(int stationId, int faultId) throws M9000Exception; // Invoked from station master before creating a fault 
	public List<ComtradeDataDTO> getLstOfOldestOfFaultsToDelete(int stationId, int count, int retainMinDays) throws M9000Exception;
}
