package com.usi.m9000.dto;

import com.fazecast.jSerialComm.SerialPort;

public class SerialPortDTO {

    private String path; // full path: /dev/ttyUSB0
    private String friendlyName; // descriptive name

    public SerialPortDTO(SerialPort port) {
        this.path = port.getSystemPortPath(); // full path
        this.friendlyName = port.getDescriptivePortName();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }


}