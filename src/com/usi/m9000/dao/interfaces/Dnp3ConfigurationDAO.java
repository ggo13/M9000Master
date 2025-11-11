package com.usi.m9000.dao.interfaces;

import java.sql.SQLException;
import java.util.List;

import com.usi.m9000.dto.Dnp3ConfigurationDTO;

public interface Dnp3ConfigurationDAO {
    public List<Dnp3ConfigurationDTO> getDnp3Configurations();
    public void addDnp3Configuration(List<Dnp3ConfigurationDTO>  dnp3Configurations) throws SQLException;
}