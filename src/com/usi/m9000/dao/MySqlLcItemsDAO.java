package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.LcItemDTO;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.util.M9kUtils;

public class MySqlLcItemsDAO implements LcItemsDAO{
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlLcItemsDAO.class);
	public MySqlLcItemsDAO()
	{
		logger.debug("Entered MySqlM9kCleanupDAO constructor");
		
	}
	@Override
	public void insertLcItem(LcItemDTO lcItemDTO) {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		try
		{
			if (!M9kUtils.isRemote())
			{
				mysqlConn =  M9kStationDBUtil.getLocalConnection();
			}
			else
			{
				mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			}

			logger.debug("Entered insertLcItem in DAO");
			int i = 0;
			ps = mysqlConn.prepareStatement("insert into LcItems set triggerTime = ?, faultInfo = ? , infPath=?");
			ps.setString(++i, lcItemDTO.getTriggerTime());
			ps.setString(++i, lcItemDTO.getFaultInfo());
			ps.setString(++i, lcItemDTO.getInfPath());
			int rowsUpdated = ps.executeUpdate();
			logger.debug("Inserted the LC Item "+lcItemDTO+" with inserted row count "+rowsUpdated);
		}
		catch (SQLException e) {
				logger.error("Error in inserting LC Item ",e);
			} 
		catch (Exception e) {
			logger.error("Error in inserting LC Item ",e);
		}
			finally
			{
				try {
					if (ps != null)
					{
						ps.close();
						ps = null;
					}
					if (mysqlConn != null)
					{
						logger.debug("DB-POOL Close connection from LcItems ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	}

		
	}

}
