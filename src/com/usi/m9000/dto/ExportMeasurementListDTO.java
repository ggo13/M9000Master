package com.usi.m9000.dto;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "Exports")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExportMeasurementListDTO {

    @XmlElement(name = "Export")
    private List<ExportMeasurementDTO> exports;

    // Getters and setters
    public List<ExportMeasurementDTO> getExports() {
        return exports;
    }

    public void setExports(List<ExportMeasurementDTO> exports) {
        this.exports = exports;
    }
}