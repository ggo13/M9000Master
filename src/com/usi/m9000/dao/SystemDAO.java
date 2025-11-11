package com.usi.m9000.dao;

import com.usi.m9000.Exception.M9000Exception;



public interface SystemDAO {
	public void createMySQLEvents(String eventType) throws M9000Exception;	
	public void dropMySQLEvents(String eventName) throws M9000Exception;
	public boolean isTableExists(String dbName, String tableName) throws M9000Exception;
}
