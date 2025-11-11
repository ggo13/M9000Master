package com.usi.m9000.dto;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class ExportMeasurementDTO {

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "_ExportName")
    private String exportName;

    @XmlElement(name = "_MeasurementType")
    private String measurementType;

    @XmlElement(name = "_Enable")
    private int enable;

    @XmlElement(name = "_Id")
    private int id;

    @XmlElement(name = "_Phase")
    private String phase;

    @XmlElement(name = "_Units")
    private String units;

    @XmlElement(name = "_Input")
    private String input;

    @XmlElement(name = "_SampleRate")
    private int sampleRate;

    // Getters and setters...

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExportName() {
        return exportName;
    }

    public void setExportName(String exportName) {
        this.exportName = exportName;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
}
