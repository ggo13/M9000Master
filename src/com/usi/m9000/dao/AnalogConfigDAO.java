package com.usi.m9000.dao;

import java.util.Map;

import com.usi.m9000.config.AnalogInfo;


public interface AnalogConfigDAO {
	public Map<Integer, AnalogInfo> getAnalogConfigForDfr(int dfr);
	
}
