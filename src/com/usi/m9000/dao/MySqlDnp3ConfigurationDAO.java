package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dao.interfaces.Dnp3ConfigurationDAO;
import com.usi.m9000.dto.Dnp3ConfigurationDTO;

public class MySqlDnp3ConfigurationDAO implements Dnp3ConfigurationDAO {
    static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MySqlDnp3ConfigurationDAO.class);

    public MySqlDnp3ConfigurationDAO() {
    }

    @Override
    public List<Dnp3ConfigurationDTO> getDnp3Configurations() {
        PreparedStatement ps = null;
        Connection mysqlConn = null;
        ResultSet rs = null;
        List<Dnp3ConfigurationDTO> lstDnp3Configurations = new ArrayList<>();

        try {
            mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
            ps = mysqlConn.prepareStatement(
                    "SELECT dnpIndex, sourceType, dnpChannel FROM dnp3Configurations ORDER BY dnpIndex");
            rs = ps.executeQuery();

            while (rs.next()) {
                Dnp3ConfigurationDTO dnp3ConfigurationDto = new Dnp3ConfigurationDTO();
                dnp3ConfigurationDto.setDnpIndex(rs.getInt("dnpIndex"));
                dnp3ConfigurationDto.setSourceType(rs.getString("sourceType"));
                dnp3ConfigurationDto.setDnpChannel(rs.getInt("dnpChannel"));
                lstDnp3Configurations.add(dnp3ConfigurationDto);
            }
        } catch (Exception e) {
            logger.error("Unable to get DNP3 Configurations ", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
                if (ps != null) {
                    ps.close();
                    ps = null;
                }
                if (mysqlConn != null) {
                    // logger.debug("DB-POOL Close connection from getLastVerifedDate ");
                    mysqlConn.close();
                    mysqlConn = null;
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.error("Error in finally section ", e);
            }
        }

        return lstDnp3Configurations;
    }

    @Override
    public void addDnp3Configuration(List<Dnp3ConfigurationDTO> dnp3Configurations) throws SQLException {
        Connection mysqlConn = null;
        PreparedStatement psInsert = null;

        try {
            mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
            mysqlConn.setAutoCommit(false); // Begin transaction

            if (dnp3Configurations.size() > 0){
                deleteAllDnp3Configurations(mysqlConn); // Will still use the same connection
                psInsert = mysqlConn.prepareStatement("INSERT INTO dnp3Configurations (dnpIndex, sourceType, dnpChannel) VALUES (?, ?, ?)");
                for (Dnp3ConfigurationDTO config : dnp3Configurations) {
                    psInsert.setInt(1, config.getDnpIndex());
                    psInsert.setString(2, config.getSourceType());
                    psInsert.setInt(3, config.getDnpChannel() );
                    psInsert.addBatch();
                }
                
                psInsert.executeBatch();
                mysqlConn.commit(); // Commit transaction
            } else {
                String message = String.format("dnp3 configuration list size is not greater than 0. Size: %s", dnp3Configurations.size());
                throw new IllegalArgumentException(message);
            }
        } catch (Exception e) {
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
                if (psInsert != null)
                    psInsert.close();
                if (mysqlConn != null)
                    mysqlConn.close();
            } catch (SQLException e) {
                logger.error("Error closing resources", e);
            }
        }
    }

    private void deleteAllDnp3Configurations(Connection mysqlConn) {
        PreparedStatement ps = null;
        try {
            ps = mysqlConn.prepareStatement("DELETE FROM dnp3Configurations");
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to delete DNP3 configurations", e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
            } catch (SQLException e) {
                logger.error("Error closing delete statement", e);
            }
        }
    }
}