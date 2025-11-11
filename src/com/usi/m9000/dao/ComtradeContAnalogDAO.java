package com.usi.m9000.dao;

import java.lang.ref.WeakReference;
import java.util.List;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.ComtradeDataDTO;



public interface ComtradeContAnalogDAO {
	public List<WeakReference<ComtradeDataDTO>> getLstOfContinuousAnalogData(int stationId) throws M9000Exception;
	public List<WeakReference<ComtradeDataDTO>> getQuickSummaryOfContinuousAnalogData(int stationId) throws M9000Exception;
	public List<WeakReference<ComtradeDataDTO>> getLstOfContinuousAnalogData(int stationId, int expId,
			String startDate, String endDate) throws M9000Exception;
	public long getFirstAvailableTime();
	public int getTotalContCount(int stationId, String contDataType) throws M9000Exception;
	public List<ComtradeDataDTO>  searchByCriteria(int stationId, String searchCriteria, String orderCriteria, int from, int to) throws M9000Exception;
	public int  countByCriteria(int stationId, String searchCriteria) throws M9000Exception;	
}
