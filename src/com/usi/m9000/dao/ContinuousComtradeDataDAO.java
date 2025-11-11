package com.usi.m9000.dao;

import java.util.List;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.ContinuousComtradeDTO;

/**
 * 
 * @author sramasamy
 * Stores the details of the comtrade files created for continuous data exports
 */

public interface ContinuousComtradeDataDAO {
	public void insertContinuousComtradeDetails(String contDataType, String startDateTime, String endDateTime, String fileName) throws M9000Exception;
	public List<ContinuousComtradeDTO> getListOfContinuousComtradeData(String stationId, String contDataType) throws M9000Exception;
	public Integer getNextId(String contDataType) throws M9000Exception;
	public int deleteRecords(int stationId, String commaSeperatedIds)
			throws M9000Exception;
	public int deleteRecordsWithFileNames(String commaSeperatedFileNames) throws M9000Exception; // Expected to call from local station master software	
	public int getTotalLTRCount(int stationId, String contDataType) throws M9000Exception;
	public List<ContinuousComtradeDTO>  searchByCriteria(int stationId, String contDataType, String searchCriteria, String orderCriteria, int from, int to) throws M9000Exception;
	public int  countByCriteria(int stationId, String contDataType, String searchCriteria) throws M9000Exception;
	public String getContinuousDataFileNames(int stationId, String csvIds) throws M9000Exception;
}
