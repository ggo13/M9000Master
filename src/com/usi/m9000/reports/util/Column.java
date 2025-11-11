/**
 * 
 */
package com.usi.m9000.reports.util;

import net.sf.dynamicreports.report.builder.datatype.BigDecimalType;

/**
 * @author sramasamy
 *
 */
public class Column {
	private String title;
	private String field;
	private String dataType;
	private BigDecimalType reportDataType;
	private int columnLength = 0;

	public Column(String title, String field, String dataType) {
		this.title = title;
		this.field = field;
		this.dataType = dataType;
	}
	
	public Column(String title, String field, String dataType, int columnLength)
	{
		this(title, field, dataType);
		this.columnLength = columnLength;
	}

	public Column(String title, String field, BigDecimalType dataType, int columnLength)
	{
		this.title = title;
		this.field = field;
		this.reportDataType = dataType;
		this.columnLength = columnLength;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * @param field the field to set
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the columnLength
	 */
	public int getColumnLength() {
		return columnLength;
	}

	/**
	 * @param columnLength the columnLength to set
	 */
	public void setColumnLength(int columnLength) {
		this.columnLength = columnLength;
	}

	public BigDecimalType getReportDataType() {
		return reportDataType;
	}

	public void setReportDataType(BigDecimalType reportDataType) {
		this.reportDataType = reportDataType;
	}
}
