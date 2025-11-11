package com.usi.m9000.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.usi.m9000.util.M9kKeyValuePair;


public class HierarchyDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer totalLevels;
	private String rootZoneName = null;
	private Integer rootZoneId = 1;
	private Integer otherZoneLeafId=9999;
	private String otherZoneName="Other";
	private Map<Integer, List<M9kKeyValuePair>> mapHierarchyLevels;
	private List<M9kKeyValuePair> lstLeafZones;
	
	public HierarchyDTO()
	{
	}

	public Integer getTotalLevels() {
		return totalLevels;
	}

	public void setTotalLevels(Integer totalLevels) {
		this.totalLevels = totalLevels;
	}

	public String getRootZoneName() {
		return rootZoneName;
	}

	public void setRootZoneName(String rootZoneName) {
		this.rootZoneName = rootZoneName;
	}

	public Integer getRootZoneId() {
		return rootZoneId;
	}

	public void setRootZoneId(Integer rootZoneId) {
		this.rootZoneId = rootZoneId;
	}

	public Integer getOtherZoneLeafId() {
		return otherZoneLeafId;
	}

	public void setOtherZoneLeafId(Integer otherZoneLeafId) {
		this.otherZoneLeafId = otherZoneLeafId;
	}

	public String getOtherZoneName() {
		return otherZoneName;
	}

	public void setOtherZoneName(String otherZoneName) {
		this.otherZoneName = otherZoneName;
	}

	public Map<Integer, List<M9kKeyValuePair>> getMapHierarchyLevels() {
		return mapHierarchyLevels;
	}

	public void setMapHierarchyLevels(Map<Integer, List<M9kKeyValuePair>> mapHierarchyLevels) {
		this.mapHierarchyLevels = mapHierarchyLevels;
	}

	public List<M9kKeyValuePair> getLstLeafZones() {
		return lstLeafZones;
	}

	public void setLstLeafZones(List<M9kKeyValuePair> LstLeafZones) {
		this.lstLeafZones = LstLeafZones;
	}


}
