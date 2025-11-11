package com.usi.m9000.dao;

import java.awt.Paint;

public class LegendValues implements Comparable<LegendValues>
{
	String title;
	Paint paint;
	/**
	 * @param value
	 * @param paint
	 */
	public LegendValues(String title, Paint paint) {
		super();
		this.title = title;
		this.paint = paint;
	}
	/**
	 * @return the paint
	 */
	public Paint getPaint() {
		return paint;
	}
	/**
	 * @param paint the paint to set
	 */
	public void setPaint(Paint paint) {
		this.paint = paint;
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
	public String toString()
	{
		return "[ " + this.title + " , " + this.paint+" ]";
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(LegendValues o) {
		int srcChnlId = Integer.parseInt(this.title.substring(1, this.title.indexOf("-")-1).trim());
		int toCompareChanlId = Integer.parseInt(o.getTitle().substring(1, o.getTitle().indexOf("-")-1).trim());
		if (srcChnlId <= toCompareChanlId)
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}
	
}
