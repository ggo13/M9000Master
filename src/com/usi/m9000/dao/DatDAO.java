package com.usi.m9000.dao;

import java.util.List;

import com.usi.m9000.dto.ComtradeDataDTO;



public interface DatDAO {
	public List<ComtradeDataDTO> getListOfComtradeData(String station);

	public int updateDatStagingWithRemote();
	
	
}
