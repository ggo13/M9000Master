package com.usi.m9000.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.usi.m9000.config.M9kMySqlDatabase;
import com.usi.m9000.dao.interfaces.Dnp3SourceTypeDAO;
import com.usi.m9000.dto.Dnp3SourceTypeDTO;

public class MySqlDnp3SourceTypeDAO implements Dnp3SourceTypeDAO {
    static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MySqlDnp3SourceTypeDAO.class);

    @Override
    public List<Dnp3SourceTypeDTO> getSourceTypes() {
        PreparedStatement ps = null;
        Connection mysqlConn = null;
        ResultSet rs = null;
        List<Dnp3SourceTypeDTO> lstDnp3SourceTypes = new ArrayList<>();

        try {
            mysqlConn = M9kMySqlDatabase.getInstance().getConnection();
            ps = mysqlConn.prepareStatement("SELECT sourceType, variation FROM dnp3SourceTypes");
            rs = ps.executeQuery();

            while (rs.next()) {
                Dnp3SourceTypeDTO dnp3SourceTypeDto = new Dnp3SourceTypeDTO();
                dnp3SourceTypeDto.setSourceType(rs.getString("sourceType"));
                dnp3SourceTypeDto.setVariation(rs.getString("variation"));
                lstDnp3SourceTypes.add(dnp3SourceTypeDto);
            }
        } catch (Exception e) {
            logger.error("Unable to get DNP3 Source Types ", e);
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

        return lstDnp3SourceTypes;
    }
}