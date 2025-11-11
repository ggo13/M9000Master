package com.usi.m9000.dao;

import java.util.Map;

import com.usi.m9000.dto.CalibrationDTO;
import com.usi.m9000.dto.ReportsDTO;



public interface ReportDAO {
	public void insertIntoReportTable(CalibrationDTO calibrationDTO);
	public String getLastVerifedDate();
	public String getLastCalibratedDate();
	public Map<String, ReportsDTO> getMostRecentReport(String reportAction);
	public void insertIntoReportTable(ReportsDTO reportsDTO);
	/**
	 * @return
	 */
	public String getLastEventTestDate();
}
