package com.usi.m9000.dao;

import java.util.Map;



public interface DfrDetailsDAO {
	public Map<String,Integer> getDfrAnalogsOffset();
	public void createOrUpdateDfrDetails();
}
