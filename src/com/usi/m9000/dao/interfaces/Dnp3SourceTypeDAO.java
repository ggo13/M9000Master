package com.usi.m9000.dao.interfaces;

import java.util.List;

import com.usi.m9000.dto.Dnp3SourceTypeDTO;

public interface Dnp3SourceTypeDAO {
    public List<Dnp3SourceTypeDTO> getSourceTypes();
}