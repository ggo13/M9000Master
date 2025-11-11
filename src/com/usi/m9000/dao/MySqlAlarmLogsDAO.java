package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dto.AlarmLogsDTO;
import com.usi.m9000.dto.StationReportDTO;
import com.usi.m9000.station.util.LedName;
import com.usi.m9000.station.util.LedState;
import com.usi.m9000.station.util.M9kStationDBUtil;
import com.usi.m9000.station.util.RelayName;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kKeyValuePair;
import com.usi.m9000.util.M9kUtils;

public class MySqlAlarmLogsDAO implements AlarmsLogDAO {

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MySqlAlarmLogsDAO.class);
	private int reportFreq = 24; //24 hours 
	public MySqlAlarmLogsDAO()
	{
	}
	
	public static void main(String[] args)
	{
		MySqlAlarmLogsDAO test = new MySqlAlarmLogsDAO();
		List<Object> lst = new ArrayList<Object>();
		lst.add(RelayName.RELAY_1);
		lst.add(RelayName.RELAY_2);
		AlarmLogsDTO alarmDto = new AlarmLogsDTO();
		alarmDto.setLedName(LedName.ONLINE.name());
		alarmDto.setLedStatus(LedState.GREEN.name());
		alarmDto.setRelays(lst.toString());
		test.insertAlarmDetails(alarmDto);
	}
	@Override
	public void insertAlarmDetails(AlarmLogsDTO alarmDto) {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		try
		{
			logger.debug("DB-POOL New connection from insertAlarmDetails ");
			
			mysqlConn = M9kStationDBUtil.getLocalConnection();
			logger.debug("Entered insertAlarmDetails in DAO");
			int i = 0;
			ps = mysqlConn.prepareStatement("insert into alarms_log set led_name= ?, led_status = ? , relays=?, description=?, updated=?");
			ps.setString(++i, alarmDto.getLedName());
			ps.setString(++i, alarmDto.getLedStatus());
			ps.setString(++i, alarmDto.getRelays());
			ps.setString(++i, alarmDto.getAlarmDescription().toString());
			ps.setString(++i, alarmDto.getAlarmTime());
			int rowsUpdated = ps.executeUpdate();
			logger.debug("Inserted the alarm details "+alarmDto+" with inserted row count "+rowsUpdated);
		}
		catch (SQLException e) {
				logger.error("Error in inserting alarm details into log table ",e);
			} 
		catch (Exception e) {
			logger.error("Error in inserting alarm details into log table ",e);
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
					e.printStackTrace();
				}

	}
		
	}
	public Map<String, List<M9kKeyValuePair>> getAllStationAlarmsSummary()
	{
		Map<String, List<M9kKeyValuePair>> mapStationAlarmSummary = new HashMap<String, List<M9kKeyValuePair>>();
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		String actionName;
		String key;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			
			logger.debug("Entered getNoOFOfflineWarnings");
//			if (ledName.equalsIgnoreCase(LedName.ONLINE.name())) // Check for all ONLINE status that were RED in last 24 hours 
//			{
////				ps = mysqlConn.prepareStatement("select b.stationId, b.name, count(*) as count from alarms_log a,station_details b where a.led_name=? and a.led_status = ? and a.updated > DATE_SUB(now(), interval 24 hour)and a.stationId = b.stationId group by stationId");
//				ps = mysqlConn.prepareStatement("select a.stationId, a.name, alarms_count.led_name, ifnull(alarms_count.count,0) as count from station_details a left join (select b.stationId, count(*) as count, b.led_name from alarms_log b where b.led_name=? and b.led_status = ? and b.updated > DATE_SUB(now(), interval 24 hour) group by b.stationId) as alarms_count ON a.stationId = alarms_count.stationId order by a.stationId");
//				ledStatus = LedState.RED.name();
//			}
//			else // Check for all status other than ONLINe that were not GREEN in last 24 hours
//			{
////				ps = mysqlConn.prepareStatement("select b.stationId, b.name, count(*) as count from alarms_log a,station_details b where a.led_name=? and a.led_status != ? and a.updated > DATE_SUB(now(), interval 24 hour)and a.stationId = b.stationId group by stationId");
//				ps = mysqlConn.prepareStatement("select a.stationId, a.name, ifnull(alarms_count.count,0) as count from station_details a left join (select b.stationId, count(*) as count from alarms_log b where b.led_name!=? and b.led_status = ? and b.updated > DATE_SUB(now(), interval 24 hour) group by b.stationId) as alarms_count ON a.stationId = alarms_count.stationId order by a.stationId");
//				ledStatus = LedState.GREEN.name();
//			}
			if (M9kUtils.isRemote())
			{
				ps = mysqlConn.prepareStatement("select a.stationId, a.name, alarms_count.led_name as actionName, ifnull(alarms_count.count,0) as count from station_details a" +
						"  left join (select b.stationId, count(*) as count,b.led_name from alarms_log b where b.led_name != ? and b.led_name != ? and b.led_status != ? and ((b.led_name = ? and b.led_status = ?) or (b.led_name != ? and b.led_status != ?)) and b.updated > DATE_SUB(now(), interval 24 hour) group by b.stationId,b.led_name) as alarms_count " +
						" ON a.stationId = alarms_count.stationId order by a.stationId");
			}
			else
			{
				ps = mysqlConn.prepareStatement("select a.stationId, a.name, alarms_count.led_name as actionName, ifnull(alarms_count.count,0) as count from station_details a" +
						"  left join (select c.stationId, count(*) as count,b.led_name from alarms_log b, station_details c where b.led_name != ? and b.led_name != ? and b.led_status != ? and ((b.led_name = ? and b.led_status = ?) or (b.led_name != ? and b.led_status != ?)) and b.updated > DATE_SUB(now(), interval 24 hour) group by b.led_name) as alarms_count " +
						" ON a.stationId = alarms_count.stationId order by a.stationId ");
			}
					
			int i = 0;
			ps.setString(++i, LedName.TRIGGER.name());
			ps.setString(++i, LedName.DISTURBANCE.name());
			ps.setString(++i, LedState.OFF.name());
			ps.setString(++i, LedName.ONLINE.name());
			ps.setString(++i, LedState.RED.name());
			ps.setString(++i, LedName.ONLINE.name());
			ps.setString(++i, LedState.GREEN.name());
			rs = ps.executeQuery();
			while (rs.next())
			{
				actionName = rs.getString("actionName");
				key = rs.getInt("stationId")+"-"+rs.getString("name");
				if (mapStationAlarmSummary.get(key) == null)
				{
					mapStationAlarmSummary.put(key, new ArrayList<M9kKeyValuePair>());
				}
				if (actionName != null)
				{
					mapStationAlarmSummary.get(key).add(new M9kKeyValuePair(actionName, ""+rs.getInt("count")));
				}
			}
		}
		catch (SQLException e) {
				logger.error("Error in reading reports table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading reports table ",e);
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

		return mapStationAlarmSummary;
	}

	@Override
	public Map<String, StationReportDTO> getStationAlarmsDetails() {
		Map<String, StationReportDTO> mapStationAlarmDetails = new HashMap<String, StationReportDTO>();
		StationReportDTO stationReportDTO;
		AlarmLogsDTO alarmLogsDTO;
		AlarmLogsDTO abnormalAlarmLogsDTO;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		String key = null;
		String ledName;
		SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			Map<String, List<AlarmLogsDTO>> mapLstOfAbnormalAlarms = getAbnormalStationAlarmsDetails();
			
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			
			logger.debug("Entered getStationAlarmsDetails");
//			if (ledName.equalsIgnoreCase(LedName.ONLINE.name())) // Check for all ONLINE status that were RED in last 24 hours 
//			{
////				ps = mysqlConn.prepareStatement("select b.stationId, b.name, count(*) as count from alarms_log a,station_details b where a.led_name=? and a.led_status = ? and a.updated > DATE_SUB(now(), interval 24 hour)and a.stationId = b.stationId group by stationId");
//				ps = mysqlConn.prepareStatement("select a.stationId, a.name, alarms_count.led_name, ifnull(alarms_count.count,0) as count from station_details a left join (select b.stationId, count(*) as count, b.led_name from alarms_log b where b.led_name=? and b.led_status = ? and b.updated > DATE_SUB(now(), interval 24 hour) group by b.stationId) as alarms_count ON a.stationId = alarms_count.stationId order by a.stationId");
//				ledStatus = LedState.RED.name();
//			}
//			else // Check for all status other than ONLINe that were not GREEN in last 24 hours
//			{
////				ps = mysqlConn.prepareStatement("select b.stationId, b.name, count(*) as count from alarms_log a,station_details b where a.led_name=? and a.led_status != ? and a.updated > DATE_SUB(now(), interval 24 hour)and a.stationId = b.stationId group by stationId");
//				ps = mysqlConn.prepareStatement("select a.stationId, a.name, ifnull(alarms_count.count,0) as count from station_details a left join (select b.stationId, count(*) as count from alarms_log b where b.led_name!=? and b.led_status = ? and b.updated > DATE_SUB(now(), interval 24 hour) group by b.stationId) as alarms_count ON a.stationId = alarms_count.stationId order by a.stationId");
//				ledStatus = LedState.GREEN.name();
//			}
			if (M9kUtils.isRemote())
			{
				logger.debug("It is remote architecture");
				ps = mysqlConn.prepareStatement("select a.stationId, a.name,a.analogs_count,a.digitals_count, alarms_log.idalarms_log, alarms_log.led_name, alarms_log.led_status, alarms_log.description, alarms_log.relays,alarms_log.updated as alarmTime from station_details a" +
						" left join (select b.idalarms_log, b.stationId, b.led_name,b.led_status, b.description,b.relays,b.updated from alarms_log b where b.led_name != ? and b.led_name != ? and b.led_status != ? and ((b.led_name = ? and (b.led_status = ? or b.led_status = ?)) or (b.led_name != ? and b.led_status != ?)) and b.updated > DATE_SUB(?, interval "+reportFreq+" hour)) as alarms_log" +
								" ON a.stationId = alarms_log.stationId order by a.stationId");
//				logger.info("Query to be executed...  "+"select a.stationId, a.name,a.analogs_count,a.digitals_count, alarms_log.idalarms_log, alarms_log.led_name, alarms_log.led_status, alarms_log.description, alarms_log.relays,alarms_log.updated as alarmTime from station_details a" +
//						" left join (select b.idalarms_log, b.stationId, b.led_name,b.led_status, b.description,b.relays,b.updated from alarms_log b where b.led_name != ? and b.led_name != ? and b.led_status != ? and ((b.led_name = ? and (b.led_status = ? or b.led_status = ?)) or (b.led_name != ? and b.led_status != ?)) and b.updated > DATE_SUB(now(), interval "+reportFreq+" hour)) as alarms_log" +
//						" ON a.stationId = alarms_log.stationId order by a.stationId");
			}
			else
			{
				logger.debug("It is local architecture");
				ps = mysqlConn.prepareStatement("select a.stationId,a.name, a.analogs_count,a.digitals_count,b.idalarms_log, b.led_name,b.led_status, b.description,b.relays,b.updated as alarmTime from station_details a, alarms_log b " +
						" where b.led_name != ? and b.led_name != ? and b.led_status != ? and ((b.led_name = ? and (b.led_status = ? or b.led_status = ?)) or (b.led_name != ? and b.led_status != ?)) " +
								" and b.updated > DATE_SUB(?, interval "+reportFreq+" hour)");
//				logger.debug("select a.stationId,a.name, a.analogs_count,a.digitals_count,b.idalarms_log, b.led_name,b.led_status, b.description,b.relays,b.updated as alarmTime from station_details a, alarms_log b " +
//						" where b.led_name != '"+LedName.TRIGGER.name()+"' and b.led_name != '"+LedName.FAN.name()+"' and b.led_status != '"+LedState.OFF.name()+"' and ((b.led_name = '"+LedName.ONLINE.name()+"' and (b.led_status = '"+LedState.RED.name()+"' or b.led_status = '"+LedState.YELLOW.name()+"')) or (b.led_name != '"+LedName.ONLINE.name()+"' and b.led_status != '"+LedState.YELLOW.name()+"')) " +
//						" and b.updated > DATE_SUB('"+sdfLocal.format(new Date())+"', interval "+reportFreq+" hour)");
			}
					
			int i = 0;
			ps.setString(++i, LedName.TRIGGER.name());
			ps.setString(++i, LedName.DISTURBANCE.name());
			ps.setString(++i, LedState.OFF.name());
			ps.setString(++i, LedName.ONLINE.name());
			ps.setString(++i, LedState.RED.name());
			ps.setString(++i, LedState.YELLOW.name());
			ps.setString(++i, LedName.ONLINE.name());
			ps.setString(++i, LedState.GREEN.name());
			ps.setString(++i, sdfLocal.format(new Date()));
			rs = ps.executeQuery();
			while (rs.next())
			{
				key = rs.getInt("stationId")+"-"+rs.getString("name");
				logger.debug("key "+key);
				stationReportDTO = mapStationAlarmDetails.get(key);
				if (stationReportDTO == null)
				{
					stationReportDTO = new StationReportDTO();
				}
				stationReportDTO.setStationId(rs.getInt("stationId"));
				stationReportDTO.setStationName(rs.getString("name"));
				stationReportDTO.setAnalogCount(rs.getInt("analogs_count"));
				stationReportDTO.setDigitalCount(rs.getInt("digitals_count"));
				
				ledName = rs.getString("led_name");  
				logger.debug("led Name "+ledName);
				if (ledName != null)
				{
					stationReportDTO.incrementWarningsCount(ledName);
					alarmLogsDTO = new AlarmLogsDTO();
					alarmLogsDTO.setAlarmId(rs.getInt("idalarms_log"));
					alarmLogsDTO.setAlarmTime(rs.getString("alarmTime"));
					alarmLogsDTO.setLedName(ledName);
					alarmLogsDTO.setLedStatus(rs.getString("led_status"));
					alarmLogsDTO.setRelays(rs.getString("relays"));
					alarmLogsDTO.setAlarmDescription(new StringBuffer(rs.getString("description")));
					alarmLogsDTO.setCurrentStatus(M9kConstants.RESOLVED);
					if (mapLstOfAbnormalAlarms != null && mapLstOfAbnormalAlarms.get(key) != null)
					{
						int iIndex = 0;
						for (Iterator<AlarmLogsDTO> iterator = mapLstOfAbnormalAlarms.get(key).iterator(); iterator
								.hasNext();) {
							abnormalAlarmLogsDTO = iterator.next();
							logger.debug("Abnormal alarms "+abnormalAlarmLogsDTO+"\n\n Alarms Dto "+alarmLogsDTO);
							logger.debug("Abnormal alarms Id "+abnormalAlarmLogsDTO.getAlarmId()+"\n\n Alarms Dto ID "+alarmLogsDTO.getAlarmId());
							if (abnormalAlarmLogsDTO.getAlarmId().intValue() == alarmLogsDTO.getAlarmId().intValue())
							{
								logger.debug("Same alarms id. About to be removed..");
								alarmLogsDTO.setCurrentStatus(M9kConstants.UNRESOLVED);
								logger.debug("List before removal "+mapLstOfAbnormalAlarms.get(key));
								mapLstOfAbnormalAlarms.get(key).remove(iIndex);
								logger.debug("List after removal "+mapLstOfAbnormalAlarms.get(key));
								break;
							}
							else if (abnormalAlarmLogsDTO.getLedName().equalsIgnoreCase(ledName))
							{
								alarmLogsDTO.setCurrentStatus(M9kConstants.UNRESOLVED);
								break;
							}
							iIndex++;
						}
					}
					if (stationReportDTO.getLstOfStationAlarms() == null)
					{
						stationReportDTO.setLstOfStationAlarms(new ArrayList<AlarmLogsDTO>());
					}
					logger.debug("Adding alarms details to the list");
					stationReportDTO.getLstOfStationAlarms().add(alarmLogsDTO);
				}
				
				mapStationAlarmDetails.put(key, stationReportDTO);

			}
			if (mapLstOfAbnormalAlarms != null && mapLstOfAbnormalAlarms.size() > 0)
			{
				List<AlarmLogsDTO> lstAlarmLogsDTO;
				for (Iterator<String> iterator = mapStationAlarmDetails.keySet().iterator(); iterator.hasNext();) {
					key = iterator.next();
					if (mapLstOfAbnormalAlarms.get(key) != null && !mapLstOfAbnormalAlarms.get(key).isEmpty())
					{
						if (mapStationAlarmDetails.get(key).getLstOfStationAlarms() == null)
						{
							mapStationAlarmDetails.get(key).setLstOfStationAlarms(mapLstOfAbnormalAlarms.get(key));
						}
						else
						{
							stationReportDTO = mapStationAlarmDetails.get(key);
							lstAlarmLogsDTO = mapLstOfAbnormalAlarms.get(key);

							stationReportDTO.getLstOfStationAlarms().addAll(lstAlarmLogsDTO);

							// Increment the alarms warning count for abnormal events out of reportInterval time 
							for (Iterator<AlarmLogsDTO> iterator2 = lstAlarmLogsDTO.iterator(); iterator2
									.hasNext();) {
								alarmLogsDTO = (AlarmLogsDTO) iterator2
										.next();
								stationReportDTO.incrementWarningsCount(alarmLogsDTO.getLedName());
							}
						}
					}
					
				}
			}
		}
		catch (SQLException e) {
				logger.error("Error in reading reports table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading reports table ",e);
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

		logger.debug("REturning maps of alarms reports "+mapStationAlarmDetails);
		return mapStationAlarmDetails;
	}

	/* (non-Javadoc)
	 * @see com.usi.m9000.dao.AlarmsLogDAO#getAlarmsLogsToDisplay()
	 */
	@Override
	public List<AlarmLogsDTO> getAlarmsLogsToDisplay(int stationId, String startDateTime, String endDateTime, int alarmsId) {
		List<AlarmLogsDTO> lstAlarmLogsDTO = new ArrayList<AlarmLogsDTO>();
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		AlarmLogsDTO alarmLogsDTO;
		StringBuffer strQueryBuf;
		int i = 0;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("station id "+stationId);
			logger.debug("Alarms id "+alarmsId);
//			ps = mysqlConn.prepareStatement("Select * from alarms_log where stationId = ? and idalarms_log > alarmsId and date(updated) > DATE_SUB(CURDATE(), INTERVAL 1 DAY) order by updated desc");
//			ps.setInt(1,stationId);
			
			if (M9kUtils.isRemote())
			{
				strQueryBuf = new StringBuffer("Select * from alarms_log where stationId = ? and ");	
			}
			else
			{
				strQueryBuf = new StringBuffer("Select * from alarms_log where ");
			}
			
			if (startDateTime != null && endDateTime != null)
			{
				strQueryBuf.append(" (updated >= ? and updated <= ?)  order by updated desc ");
//				ps = mysqlConn.prepareStatement("Select * from alarms_log where stationId = ? and (updated >= ? and updated <= ?)  order by updated desc");
				logger.debug("Executed the query 1");
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getAlarmsLogPnlResultsLimit());

				ps = mysqlConn.prepareStatement(strQueryBuf.toString());
				if (M9kUtils.isRemote())
				{
					ps.setInt(++i,stationId);
				}
				ps.setString(++i, startDateTime);
				ps.setString(++i, endDateTime);
			}
			else if (startDateTime != null )
			{
				strQueryBuf.append(" updated >= ?   order by updated desc ");
//				ps = mysqlConn.prepareStatement("Select * from alarms_log where stationId = ? and updated >= ?  order by updated desc");
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getAlarmsLogPnlResultsLimit());
				ps = mysqlConn.prepareStatement(strQueryBuf.toString());
				if (M9kUtils.isRemote())
				{
					ps.setInt(++i,stationId);
				}
				ps.setString(++i, startDateTime);
				logger.debug("Executed the query 2");
			}
			else if (endDateTime != null )
			{
				strQueryBuf.append(" updated <= ?   order by updated desc ");
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getAlarmsLogPnlResultsLimit());

//				ps = mysqlConn.prepareStatement("Select * from alarms_log where stationId = ? and updated <= ? order by updated desc");
				ps = mysqlConn.prepareStatement(strQueryBuf.toString());
				if (M9kUtils.isRemote())
				{
					ps.setInt(++i,stationId);
				}
				ps.setString(++i, endDateTime);
				logger.debug("Executed the query 3");
			}
			else
			{
//				strQueryBuf.append(" idalarms_log > "+alarmsId +" and date(updated) > DATE_SUB(CURDATE(), INTERVAL 2 DAY) order by updated desc");
				strQueryBuf.append(" idalarms_log > "+alarmsId +" order by updated desc ");
				strQueryBuf.append(" LIMIT 0,"+M9kUtils.getAlarmsLogPnlResultsLimit());
				ps = mysqlConn.prepareStatement(strQueryBuf.toString());
//				ps = mysqlConn.prepareStatement("Select * from alarms_log where stationId = ? and idalarms_log > alarmsId and date(updated) > DATE_SUB(CURDATE(), INTERVAL 1 DAY) order by updated desc");
				if (M9kUtils.isRemote())
				{
					ps.setInt(++i,stationId);
				}
			}
			rs = ps.executeQuery();
			while (rs.next())
			{
				alarmLogsDTO = new AlarmLogsDTO();
				alarmLogsDTO.setAlarmId(rs.getInt("idalarms_log"));
				alarmLogsDTO.setStationId(stationId);
				alarmLogsDTO.setAlarmTime(rs.getString("updated"));
				alarmLogsDTO.setLedName(rs.getString("led_name"));
				alarmLogsDTO.setLedStatus(rs.getString("led_status"));
				alarmLogsDTO.setRelays(rs.getString("relays"));
				alarmLogsDTO.setAlarmDescription(new StringBuffer(rs.getString("description")));
				lstAlarmLogsDTO.add(alarmLogsDTO);
			}
		}
		catch (SQLException e) {
			logger.error("Error in reading alarms_log table",e);
		} 
		catch (Exception e) {
			logger.error("Error in reading alarms_log table ",e);
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
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Error in finally section ",e);
			}

		}

		return lstAlarmLogsDTO;
	}

	/* (non-Javadoc)
	 * @see com.usi.m9000.dao.AlarmsLogDAO#getAbnormalStationAlarmsDetails()
	 */
	@Override
	public Map<String, List<AlarmLogsDTO>> getAbnormalStationAlarmsDetails() {
		Map<String, List<AlarmLogsDTO>> mapAbnormalAlarmDetails = new HashMap<String, List<AlarmLogsDTO>>();
		List<AlarmLogsDTO> lstAbnormalAlarms = null;
		AlarmLogsDTO alarmLogsDTO;
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		String key = null;
		String ledName;
		String ledStatus;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			
			logger.debug("Entered getAbnormalStationAlarmsDetails");
			if (M9kUtils.isRemote())
			{
				ps = mysqlConn.prepareStatement("select a.stationId, name, a.idalarms_log, a.led_name, a.led_status, a.relays, a.description, a.updated from alarms_log a "
						+ "inner join (select al.stationId, name, led_name,max(al.updated) as updated from alarms_log al, station_details sd where al.stationId = sd.stationId group by stationId,led_name) b "
						+ "on a.led_name = b.led_name and a.updated = b.updated and a.stationId = b.stationId order by a.idalarms_log desc");
			}
			else
			{
				ps = mysqlConn.prepareStatement("select st.stationId,st.name,a.idalarms_log, a.led_name, a.led_status, a.relays, a.description, a.updated from station_details st,alarms_log a inner join "
						+ "(select led_name,max(updated) as updated from alarms_log group by led_name) b on a.led_name = b.led_name and a.updated = b.updated order by a.idalarms_log desc");
			}
					
			rs = ps.executeQuery();
			while (rs.next())
			{
				key = rs.getInt("stationId")+"-"+rs.getString("name");
				lstAbnormalAlarms = mapAbnormalAlarmDetails.get(key);
				ledName = rs.getString("led_name");  
				ledStatus = rs.getString("led_status");
				if ( ledStatus != null &&  ledStatus.equalsIgnoreCase(LedState.RED.name())  )
				{
					alarmLogsDTO = new AlarmLogsDTO();
					alarmLogsDTO.setAlarmId(rs.getInt("idalarms_log"));
					alarmLogsDTO.setAlarmTime(rs.getString("updated"));
					alarmLogsDTO.setLedName(ledName);
					alarmLogsDTO.setLedStatus(ledStatus);
					alarmLogsDTO.setRelays(rs.getString("relays"));
					alarmLogsDTO.setAlarmDescription(new StringBuffer(rs.getString("description")));
					alarmLogsDTO.setCurrentStatus(M9kConstants.UNRESOLVED);
					if (lstAbnormalAlarms == null)
					{
						lstAbnormalAlarms = new ArrayList<AlarmLogsDTO>();
						mapAbnormalAlarmDetails.put(key, lstAbnormalAlarms);
					}
					logger.debug("Adding alarms details to the list"+alarmLogsDTO);
					mapAbnormalAlarmDetails.get(key).add(alarmLogsDTO);
				}
				

			}
		}
		catch (SQLException e) {
				logger.error("Error in reading reports table",e);
			} 
		catch (Exception e) {
			logger.error("Error in reading reports table ",e);
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

		logger.debug("Returning abnormal alarmDetails map "+mapAbnormalAlarmDetails);
		return mapAbnormalAlarmDetails;

	}

	@Override
	public void insertIntoMastersAlarmDetails(AlarmLogsDTO alarmDto) {
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		try
		{
			logger.debug("DB-POOL New connection from insertAlarmDetails ");
			
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("Entered insertAlarmDetails in DAO");
			int i = 0;
			if (M9kUtils.isRemote())
			{
				ps = mysqlConn.prepareStatement("insert into alarms_log set stationId = ?, led_name= ?, led_status = ? , relays=?, description=?, updated=?");
				ps.setInt(++i, alarmDto.getStationId());
			}
			else
			{
				ps = mysqlConn.prepareStatement("insert into alarms_log set led_name= ?, led_status = ? , relays=?, description=?, updated=?");
			}
			ps.setString(++i, alarmDto.getLedName());
			ps.setString(++i, alarmDto.getLedStatus());
			ps.setString(++i, alarmDto.getRelays());
			ps.setString(++i, alarmDto.getAlarmDescription().toString());
			ps.setString(++i, alarmDto.getAlarmTime());
			int rowsUpdated = ps.executeUpdate();
			logger.debug("Inserted the alarm details "+alarmDto+" with inserted row count "+rowsUpdated);
		}
		catch (SQLException e) {
				logger.error("Error in inserting alarm details into log table ",e);
			} 
		catch (Exception e) {
			logger.error("Error in inserting alarm details into log table ",e);
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
					e.printStackTrace();
				}

	}
		
	}

	/**
	 * @return the reportFreq
	 */
	public int getReportFreq() {
		return reportFreq;
	}

	/**
	 * @param reportFreq the reportFreq to set
	 */
	public void setReportFreq(int reportFreq) {
		this.reportFreq = reportFreq;
	}

	@Override
	public int getTotalAlarmsCount(int stationId) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		int totalAlarmsCount = 0;
		try
		{
			StringBuffer strbufQuery = new StringBuffer("Select count(*) from alarms_log ");
			connection = M9kMySqlDatabase.getInstance().getConnection();
			if (M9kUtils.isRemote())
			{
				strbufQuery.append(" where stationId = ? ");
			}
		ps = connection.prepareStatement(strbufQuery.toString());
		if (M9kUtils.isRemote())
		{
			ps.setInt(1,stationId);
		}
//		System.out.println("About to get total faults from station  "+stationId );
		rs = ps.executeQuery();
		if (rs.next())
		{
			totalAlarmsCount = rs.getInt(1);
		}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in fetching total counts of alarms for station "+stationId+" Returning value as zero",e);
			throw new M9000Exception(e);
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
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return totalAlarmsCount;

	}

	@Override
	public List<AlarmLogsDTO> searchByCriteria(int stationId, String searchCriteria, String orderCriteria, int from,
			int to) throws M9000Exception {
		List<AlarmLogsDTO> lstAlarmLogsDTO = new ArrayList<AlarmLogsDTO>();
		PreparedStatement ps = null;
		Connection mysqlConn = null;
		ResultSet rs = null;
		AlarmLogsDTO alarmLogsDTO;
		StringBuffer strQueryBuf;
		try
		{
			mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
			logger.debug("station id "+stationId);
//			ps = mysqlConn.prepareStatement("Select * from alarms_log where stationId = ? and idalarms_log > alarmsId and date(updated) > DATE_SUB(CURDATE(), INTERVAL 1 DAY) order by updated desc");
//			ps.setInt(1,stationId);
			
			if (M9kUtils.isRemote())
			{
				strQueryBuf = new StringBuffer("Select * from alarms_log where stationId = ? ");	
			}
			else
			{
				strQueryBuf = new StringBuffer("Select * from alarms_log  ");
			}
			
			if (searchCriteria != null && !searchCriteria.isEmpty())
			{
				if (M9kUtils.isRemote())
				{
					strQueryBuf.append(" and "+searchCriteria);
				}
				else
				{
					strQueryBuf.append(" where "+searchCriteria);
				}
			}
			if (orderCriteria != null && !orderCriteria.isEmpty())
			{
				strQueryBuf.append(" "+orderCriteria);
			}
			strQueryBuf.append(" limit ? , ? ");
			logger.debug("AlarmsLog sql to execute "+strQueryBuf.toString());
			ps = mysqlConn.prepareStatement(strQueryBuf.toString());
			int iCnt = 1;
			if (M9kUtils.isRemote())
			{
				ps.setInt(iCnt++,stationId);
			}
			ps.setInt(iCnt++,from);
			ps.setInt(iCnt++,to);
			rs = ps.executeQuery();
			while (rs.next())
			{
				alarmLogsDTO = new AlarmLogsDTO();
				alarmLogsDTO.setAlarmId(rs.getInt("idalarms_log"));
				alarmLogsDTO.setStationId(stationId);
				alarmLogsDTO.setAlarmTime(rs.getString("updated"));
				alarmLogsDTO.setLedName(rs.getString("led_name"));
				alarmLogsDTO.setLedStatus(rs.getString("led_status"));
				alarmLogsDTO.setRelays(rs.getString("relays"));
				alarmLogsDTO.setAlarmDescription(new StringBuffer(rs.getString("description")));
				lstAlarmLogsDTO.add(alarmLogsDTO);
			}
		}
		catch (SQLException e) {
			logger.error("Error in reading alarms_log table",e);
		} 
		catch (Exception e) {
			logger.error("Error in reading alarms_log table ",e);
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
					mysqlConn.close();
					mysqlConn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Error in finally section ",e);
			}

		}

		return lstAlarmLogsDTO;

	}

	@Override
	public int countByCriteria(int stationId, String searchCriteria) throws M9000Exception {
		PreparedStatement ps = null;
		ResultSet rs= null;
		Connection connection = null;
		int totalAlarmsCount = 0;
		try
		{
			StringBuffer strbufQuery = new StringBuffer("Select count(*) from alarms_log ");
			connection = M9kMySqlDatabase.getInstance().getConnection();
			if (M9kUtils.isRemote())
			{
				strbufQuery.append(" where stationId = ? ");
			}
		if (searchCriteria != null && !searchCriteria.isEmpty())
		{
			if (!M9kUtils.isRemote())
			{
				strbufQuery.append(" where ");
			}
			strbufQuery.append(" "+searchCriteria);
		}
		ps = connection.prepareStatement(strbufQuery.toString());
		if (M9kUtils.isRemote())
		{
			ps.setInt(1,stationId);
		}

//		System.out.println("About to get total faults from station  "+stationId );
		rs = ps.executeQuery();
		if (rs.next())
		{
			totalAlarmsCount = rs.getInt(1);
		}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in fetching total counts of alarms for station "+stationId+" Returning value as zero",e);
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
				if (connection != null)
				{
					connection.close();
					connection = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return totalAlarmsCount;
	}

}
