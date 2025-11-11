package com.usi.m9000.dao;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.ComtradeDataDTO;



public interface ComtradeContDAO {
	public List<WeakReference<ComtradeDataDTO>> getLstOfContinuousData(int stationId) throws M9000Exception;
	public List<WeakReference<ComtradeDataDTO>> getLstOfContinuousData(int stationId, int expId, String startDate, String endDate) throws M9000Exception;
	public List<WeakReference<ComtradeDataDTO>> getQuickSummaryOfContinuousData(int stationId) throws M9000Exception;
	public ComtradeDataDTO getLstOfContinuousData(int stationId, String expId,
			String startDate, String endDate) throws M9000Exception;
	public Map<String,String> getAvailableMeasurementTypes(int stationId) throws M9000Exception;
	long getFirstAvailableTime();
}
