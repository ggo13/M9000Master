package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;

public class MySqlDfrDetailsDAO implements DfrDetailsDAO {

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlDfrDetailsDAO.class);
	public MySqlDfrDetailsDAO()
	{

	}
	
	public static void main(String[] args)
	{
		MySqlDfrDetailsDAO test = new MySqlDfrDetailsDAO();
		test.createOrUpdateDfrDetails();
	}

	
	@Override
	public Map<String, Integer> getDfrAnalogsOffset() {
		Map<String, Integer> mapDfrsAnalogOffset = null;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		try
		{
			mapDfrsAnalogOffset = new HashMap<String, Integer> ();
			logger.debug("DB-POOL New connection from getDfrAnalogsOffset ");
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Entered getDfrAnalogsOffset in DAO");
			ps = mysqlConn.prepareStatement("select dfrId, analog_offset from dfr_details");
			rs = ps.executeQuery();
			while (rs.next())
			{
				mapDfrsAnalogOffset.put("DFR"+rs.getInt("dfrId"),rs.getInt("analog_offset"));
			}
			logger.debug("Map of analog offset "+mapDfrsAnalogOffset);
		}
		catch (SQLException e) {
				logger.error("Error in reading dfr_details table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading dfr_details table ",e);
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
						logger.debug("DB-POOL Close connection from insertAlarmDetails ");
						mysqlConn.close();
						mysqlConn = null;
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Error in finally section ",e);
				}

	}
		return mapDfrsAnalogOffset;
	}

	@Override
	public void createOrUpdateDfrDetails() {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		try
		{
			List<DfrDTO> lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
			Collections.sort(lstDfrs, new Comparator<DfrDTO>() {
	
				@Override
				public int compare(DfrDTO o1, DfrDTO o2) {
	//				return ((o1.getDfrId() < o2.getDfrId())?0:1);
					return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
				}
			});
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			ps = mysqlConn.prepareStatement("insert into dfr_details set dfrId=?, ipaddress=?, analog_count=?, digital_count=?, analog_offset=?, digital_offset=? " +
					"on duplicate key update dfrId=values(dfrId), ipaddress=values(ipaddress), analog_count=values(analog_count), digital_count=values(digital_count), analog_offset=values(analog_offset), digital_offset=values(digital_offset)");
			int iCnt = 0;
			int resultRowCnt;
			DfrDTO dfrDTO;
			for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
				dfrDTO =  iterator.next();
				iCnt = 0;
				ps.setInt(++iCnt, dfrDTO.getDfrId());
				ps.setString(++iCnt, dfrDTO.getIpAddress());
				ps.setInt(++iCnt, dfrDTO.getAnalogChnlCnt());
				ps.setInt(++iCnt, dfrDTO.getDigitalChnlCnt());
				if (dfrDTO.getAnalogChannelStart() > 0)
				{
					ps.setInt(++iCnt, dfrDTO.getAnalogChannelStart()-1);
				}
				else
				{
					ps.setInt(++iCnt, dfrDTO.getAnalogChannelStart());
				}
				if (dfrDTO.getDigitalChannelStart() > 0)
				{
					ps.setInt(++iCnt, dfrDTO.getDigitalChannelStart() - 1);
				}
				else
				{
					ps.setInt(++iCnt, dfrDTO.getDigitalChannelStart());
				}
				resultRowCnt = ps.executeUpdate();
				logger.debug("Updated dfr_details table "+resultRowCnt);
			}
		}
		catch (SQLException e) {
			logger.error("Error in updating dfr_details table",e);
		} 
		catch (Exception e) {
			logger.error("Error in updating dfr_details table ",e);
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
					logger.debug("DB-POOL Close connection from insertAlarmDetails ");
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Error in finally section ",e);
			}
		}
	}

}
