package com.usi.m9000.dao;

import java.util.List;
import java.util.Map;

import com.usi.m9000.util.M9kKeyValuePair;



public interface HierarchyDAO {
	public M9kKeyValuePair getRootZone();
	public int getTotalHierarchyLevel();
	public List<M9kKeyValuePair> getChildZones(int zoneId);
	public Map<Integer, List<M9kKeyValuePair>> getHierarchyMap();
	public Integer[] getParentPathIds(int stationId);
	public List<M9kKeyValuePair> getLeafZones();
}
