package com.usi.m9000.dto;

public class Dnp3ConfigurationDTO {
    private int dnpIndex;
    private String sourceType;
    private int dnpChannel;

    public int getDnpIndex() {
        return dnpIndex;
    }

    public void setDnpIndex(int dnpIndex) {
        this.dnpIndex = dnpIndex;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public int getDnpChannel() {
        return dnpChannel;
    }

    public void setDnpChannel(int dnpChannel) {
        this.dnpChannel = dnpChannel;
    }
}