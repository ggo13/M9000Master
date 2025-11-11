package com.usi.m9000.dto;

import java.io.Serializable;

import org.apache.commons.lang.StringEscapeUtils;
public class LineGroupChannelsDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private Integer channelId;
	private Integer dfrId;
	private Integer exportId;
	/**
	 * 
	 */
	public LineGroupChannelsDTO() {
		// TODO Auto-generated constructor stub
	}
	public String getName() {
		return StringEscapeUtils.unescapeXml(name);
	}
	public void setName(String name) {
		this.name = StringEscapeUtils.escapeXml(name);
	}
	public Integer getChannelId() {
		return channelId;
	}
	public void setChannelId(Integer channelId) {
		this.channelId = channelId;
	}
	public Integer getDfrId() {
		return dfrId;
	}
	public void setDfrId(Integer dfrId) {
		this.dfrId = dfrId;
	}
	public Integer getExportId() {
		return exportId;
	}
	public void setExportId(Integer exportId) {
		this.exportId = exportId;
	}
	
}
