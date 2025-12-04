package com.usi.m9000.dto;

public class Dnp3OutstationDetailDTO {
    private int stationId;
    private String transportMethod;
    private int portNumber;
    private int faultLocationTimeLimitInSeconds;
    private String serialPortPath;
    private int baudRate;

    public int getStationId() {
        return stationId;
    }
    public void setStationId(int stationId) {
        this.stationId = stationId;
    }
    public String getTransportMethod() {
        return transportMethod;
    }
    public void setTransportMethod(String transportMethod) {
        this.transportMethod = transportMethod;
    }
    public int getPortNumber() {
        return portNumber;
    }
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }
    public int getFaultLocationTimeLimitInSeconds() {
        return faultLocationTimeLimitInSeconds;
    }
    public void setFaultLocationTimeLimitInSeconds(int faultLocationTimeLimitInSeconds) {
        this.faultLocationTimeLimitInSeconds = faultLocationTimeLimitInSeconds;
    }
    public String getSerialPortPath() {
        return serialPortPath;
    }
    public void setSerialPortPath(String serialPortPath) {
        this.serialPortPath = serialPortPath;
    }
    public int getBaudRate() {
        return baudRate;
    }
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }
    
}
