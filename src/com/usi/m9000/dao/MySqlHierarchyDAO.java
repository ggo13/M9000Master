package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.util.M9kKeyValuePair;

public class MySqlHierarchyDAO implements HierarchyDAO {

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlHierarchyDAO.class);
	public MySqlHierarchyDAO()
	{
	}
	
	@Override
	public M9kKeyValuePair getRootZone() {
		M9kKeyValuePair rootZone = null;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			
			logger.debug("Entered getChildZones");
			ps = mysqlConn.prepareStatement("SELECT id, name FROM hierarchy WHERE parent_id IS NULL"); 
			rs = ps.executeQuery();
			while (rs.next())
			{
				rootZone = new M9kKeyValuePair(""+rs.getInt("id"),rs.getString("name"));
			}
		}
		catch (Exception e) {
			logger.error("Error in reading Hierarchy table ",e);
			rootZone = null;
		}
			finally
			{
				try {
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
//						logger.debug("DB-POOL Close connection from getLastVerifedDate ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Error in finally section ",e);
				}

	}

		return rootZone;
	}

	@Override
	public Map<Integer, List<M9kKeyValuePair>> getHierarchyMap()
	{
		Map<Integer, List<M9kKeyValuePair>> maphierarchy = new HashMap<Integer, List<M9kKeyValuePair>>();
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		Integer key;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			
			logger.debug("Entered getHierarchyMap");
			ps = mysqlConn.prepareStatement("WITH RECURSIVE category_path (id, name, lvl) AS\r\n" + 
					"(\r\n" + 
					"  SELECT id, name, 0 lvl\r\n" + 
					"    FROM hierarchy\r\n" + 
					"    WHERE parent_id IS NULL\r\n" + 
					"  UNION ALL\r\n" + 
					"  SELECT c.id, c.name,cp.lvl + 1\r\n" + 
					"    FROM category_path AS cp JOIN hierarchy AS c\r\n" + 
					"      ON cp.id = c.parent_id\r\n" + 
					")\r\n" + 
					"SELECT * FROM category_path\r\n" + 
					"ORDER BY lvl" );
					
			rs = ps.executeQuery();
			while (rs.next())
			{
				key = rs.getInt("lvl");
				if (key == 0)
				{
					// Skipping Root Node
					continue;
				}
				key--;
				if (maphierarchy.get(key) == null)
				{
					maphierarchy.put(key, new ArrayList<M9kKeyValuePair>());
				}
				maphierarchy.get(key).add(new M9kKeyValuePair(""+rs.getInt("id"),rs.getString("name")));
			}
		}
		catch (SQLException e) {
				logger.error("Error in reading Hierarchy table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading Hierarchy table ",e);
		}
			finally
			{
				try {
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
//						logger.debug("DB-POOL Close connection from getLastVerifedDate ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Error in finally section ",e);
				}

	}

		return maphierarchy;
	}

	@Override
	public List<M9kKeyValuePair> getChildZones(int zoneId) {
		List<M9kKeyValuePair> lstChildZones = new ArrayList<M9kKeyValuePair>();
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			
			logger.debug("Entered getChildZones");
			ps = mysqlConn.prepareStatement("SELECT id, name FROM hierarchy WHERE parent_id = ?"); 
			ps.setInt(1, zoneId);
			rs = ps.executeQuery();
			lstChildZones.add(new M9kKeyValuePair("0","ALL"));
			while (rs.next())
			{
				lstChildZones.add(new M9kKeyValuePair(""+rs.getInt("id"),rs.getString("name")));
			}
		}
		catch (SQLException e) {
				logger.error("Error in reading Hierarchy table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading Hierarchy table ",e);
		}
			finally
			{
				try {
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
//						logger.debug("DB-POOL Close connection from getLastVerifedDate ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Error in finally section ",e);
				}

	}

		return lstChildZones;
	}

	@Override
	public int getTotalHierarchyLevel() {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		int totalLevel = 0;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			
			logger.debug("Entered getHierarchyMap");
			ps = mysqlConn.prepareStatement("WITH RECURSIVE category_path (id, name, lvl) AS\r\n" + 
					"(\r\n" + 
					"  SELECT id, name, 0 lvl\r\n" + 
					"    FROM hierarchy\r\n" + 
					"    WHERE parent_id IS NULL\r\n" + 
					"  UNION ALL\r\n" + 
					"  SELECT c.id, c.name,cp.lvl + 1\r\n" + 
					"    FROM category_path AS cp JOIN hierarchy AS c\r\n" + 
					"      ON cp.id = c.parent_id\r\n" + 
					")\r\n" + 
					"SELECT max(lvl) as maxLevel FROM category_path");
					
			rs = ps.executeQuery();
			while (rs.next())
			{
				totalLevel = rs.getInt("maxLevel");
			}
		}
		catch (SQLException e) {
				logger.error("Error in reading Hierarchy table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading Hierarchy table ",e);
		}
			finally
			{
				try {
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
//						logger.debug("DB-POOL Close connection from getLastVerifedDate ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Error in finally section ",e);
				}

	}

		return totalLevel;
	}

	@Override
	public Integer[] getParentPathIds(int stationId) {
		Integer[] parentsIds = new Integer[10];
		Arrays.fill(parentsIds, 0); // Initialize with zeros
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		Integer key;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			
			logger.debug("Entered getHierarchyMap");
			ps = mysqlConn.prepareStatement("WITH RECURSIVE category_path (id, title, parent_id) AS\r\n" + 
					"(\r\n" + 
					"  SELECT id, title, parent_id\r\n" + 
					"    FROM category\r\n" + 
					"    WHERE id = ? \r\n" + 
					"  UNION ALL\r\n" + 
					"  SELECT c.id, c.title, c.parent_id\r\n" + 
					"    FROM category_path AS cp JOIN category AS c\r\n" + 
					"      ON cp.parent_id = c.id\r\n" + 
					")\r\n" + 
					"SELECT * FROM category_path order by id" );
			ps.setInt(1, stationId);
			rs = ps.executeQuery();
			int arrIndex = 0;
			while (rs.next())
			{
				parentsIds[arrIndex++] = rs.getInt("id");
			}
		}
		catch (SQLException e) {
				logger.error("Error in reading Hierarchy table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading Hierarchy table ",e);
		}
			finally
			{
				try {
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
//						logger.debug("DB-POOL Close connection from getLastVerifedDate ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Error in finally section ",e);
				}

	}
		logger.debug("parent ids to be returned "+parentsIds.toString());
		return parentsIds;
	}

	@Override
	public List<M9kKeyValuePair> getLeafZones() {
		List<M9kKeyValuePair> lstLeafZones = new ArrayList<M9kKeyValuePair>();
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			
			logger.debug("Entered getLeafZones");
			ps = mysqlConn.prepareStatement("SELECT c1.id, c1.name FROM hierarchy c1 LEFT JOIN hierarchy c2 ON c2.parent_id = c1.id WHERE c2.id IS NULL;"); 
			rs = ps.executeQuery();
			while (rs.next())
			{
				lstLeafZones.add(new M9kKeyValuePair(""+rs.getInt("id"),rs.getString("name")));
			}
			if (lstLeafZones.isEmpty())
			{
				lstLeafZones.add(new M9kKeyValuePair("9999", "Other"));
			}
		}
		catch (SQLException e) {
				logger.error("Error in reading Hierarchy table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading Hierarchy table ",e);
		}
			finally
			{
				try {
					if (rs != null)
					{
						rs.close();
						rs = null;
					}
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
//						logger.debug("DB-POOL Close connection from getLastVerifedDate ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Error in finally section ",e);
				}

	}

		return lstLeafZones;
	}

}
