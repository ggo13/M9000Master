package com.usi.m9000.dao;

import java.util.List;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.ComtradeDataDTO;



public interface LongTermDataDAO {
	public List<ComtradeDataDTO> getLstOfLongTermAnalogDataStaged(int stationId, int faultId) throws M9000Exception;
	public List<ComtradeDataDTO> getLstOfLongTermMeasurementsDataStaged(int stationId, int faultId) throws M9000Exception;
	public int insertIntoLongTermDat(ComtradeDataDTO comtradeDataDTO) throws M9000Exception;
	public String getLongTermDataFileName(int stationId, int faultId, String ltrType) throws M9000Exception;
	public List<ComtradeDataDTO> getLstOfLongTermDataDetails(int stationId) throws M9000Exception;
	public List<ComtradeDataDTO> getLstOfLongTermDataDetails(int stationId, String startDateTime, String endDateTime, List<ComtradeDataDTO> lstComtradeData, String ltrType) throws M9000Exception;
	
	public String getLongTermDataFileName(int stationId, long tsTrigger, String ltrType) throws M9000Exception;
	/**
	 * @param stationId
	 * @param commaSeperatedFaultIds
	 * @return
	 * @throws M9000Exception
	 */
	public int deleteLTRRecords(int stationId, String commaSeperatedFaultIds)
			throws M9000Exception;
	public int deleteLTRRecordsWithFileNames(String commaSeperatedFileNames) throws M9000Exception; // Expected to call from local station master software
	public void updateComments(String stationId, ComtradeDataDTO comtradeDataDTO) throws M9000Exception;
	public int getTotalDDRCount(int stationId, String ltType) throws M9000Exception;
	public List<ComtradeDataDTO>  searchByCriteria(int stationId, String ltType, String searchCriteria, String orderCriteria, int from, int to) throws M9000Exception;
	public int  countByCriteria(int stationId, String ltType, String searchCriteria) throws M9000Exception;
	public String getLongTermDataFileNames(int stationId, String csvIds, String ltrType) throws M9000Exception;
	public List<ComtradeDataDTO> getLstOfOldestLongTermDataDetails(int stationId, int count, int retainMinDays) throws M9000Exception;
}
