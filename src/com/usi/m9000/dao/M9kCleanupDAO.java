package com.usi.m9000.dao;

public interface M9kCleanupDAO {
	public void clearStationMasterTables();
	public void clearRemoteMasterTables(int stationId);
}
