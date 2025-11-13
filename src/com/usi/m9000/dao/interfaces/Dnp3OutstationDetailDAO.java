package com.usi.m9000.dao.interfaces;

import java.sql.SQLException;

import com.usi.m9000.dto.Dnp3OutstationDetailDTO;

public interface Dnp3OutstationDetailDAO {
    public Dnp3OutstationDetailDTO getDnp3OutstationDetailByStationId(int stationId);
    public void addDnp3OutstationDetail(Dnp3OutstationDetailDTO  dnp3OutstationDetail) throws SQLException;
}
