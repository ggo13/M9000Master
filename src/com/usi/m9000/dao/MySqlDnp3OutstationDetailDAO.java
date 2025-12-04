package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dao.interfaces.Dnp3OutstationDetailDAO;
import com.usi.m9000.dto.Dnp3OutstationDetailDTO;

public class MySqlDnp3OutstationDetailDAO implements Dnp3OutstationDetailDAO {
    static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MySqlDnp3OutstationDetailDAO.class);

    @Override
    public Dnp3OutstationDetailDTO getDnp3OutstationDetailByStationId(int stationId) {
        Dnp3OutstationDetailDTO dnp3OutstationDetailDto = new Dnp3OutstationDetailDTO();

        PreparedStatement ps = null;
        Connection mysqlConn = null;
        ResultSet rs = null;

        try {
            mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
            ps = mysqlConn.prepareStatement(
                    "SELECT stationId, transportMethod, portNumber, faultLocationTimeLimitInSeconds, serialPortPath, baudRate FROM dnp3OutstationDetails WHERE stationId = ?");
            ps.setInt(1, stationId);
            rs = ps.executeQuery();

            if (rs.next()) {
                dnp3OutstationDetailDto.setStationId(rs.getInt("stationId"));
                dnp3OutstationDetailDto.setTransportMethod(rs.getString("transportMethod"));
                dnp3OutstationDetailDto.setPortNumber(rs.getInt("portNumber"));
                dnp3OutstationDetailDto.setFaultLocationTimeLimitInSeconds(
                        rs.getInt("faultLocationTimeLimitInSeconds"));
                dnp3OutstationDetailDto.setSerialPortPath(rs.getString("serialPortPath"));
                dnp3OutstationDetailDto.setBaudRate(rs.getInt("baudRate"));
            }

        } catch (M9000Exception | SQLException e) {
            logger.error("Unable to get DNP3 Outstation Details for stationId=" + stationId, e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (mysqlConn != null)
                    mysqlConn.close();
            } catch (SQLException e) {
                logger.error("Error closing DB resources in getDnp3OutstationDetailByStationId", e);
            }
        }

        return dnp3OutstationDetailDto;
    }

    @Override
    public void addDnp3OutstationDetail(Dnp3OutstationDetailDTO dnp3OutstationDetail) throws SQLException {
        Connection mysqlConn = null;
        PreparedStatement ps = null;
        try {
            String sql = "INSERT INTO dnp3OutstationDetails " +
                    "(stationId, transportMethod, portNumber, faultLocationTimeLimitInSeconds, serialPortPath, baudRate) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "transportMethod = VALUES(transportMethod), " +
                    "portNumber = VALUES(portNumber), " +
                    "faultLocationTimeLimitInSeconds = VALUES(faultLocationTimeLimitInSeconds), " +
                    "serialPortPath = VALUES(serialPortPath), " +
                    "baudRate = VALUES(baudRate)";

            mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
            ps = mysqlConn.prepareStatement(sql);
            ps.setInt(1, dnp3OutstationDetail.getStationId());
            ps.setString(2, dnp3OutstationDetail.getTransportMethod());
            ps.setInt(3, dnp3OutstationDetail.getPortNumber());
            ps.setInt(4, dnp3OutstationDetail.getFaultLocationTimeLimitInSeconds());
            ps.setString(5, dnp3OutstationDetail.getSerialPortPath());
            ps.setInt(6, dnp3OutstationDetail.getBaudRate());
            ps.executeUpdate();

        } catch (M9000Exception | SQLException e) {
            if (mysqlConn != null) {
                try {
                    mysqlConn.rollback(); // Rollback on error
                    logger.error("Error saving DNP3 Config", e);
                } catch (SQLException rollbackEx) {
                    logger.error("Rollback failed", rollbackEx);
                }
            }
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (mysqlConn != null)
                    mysqlConn.close();
            } catch (SQLException e) {
                logger.error("Error closing resources", e);
            }
        }
    }
}
