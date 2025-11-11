/**
 * 
 */
package com.usi.m9000.station.util;

/**
 * @author sramasamy
 *
 */
public class M9kKeyValuePair {

	private String key;
	private String value;

	/**
	 * 
	 */
	public M9kKeyValuePair(String key, String value) {
		this.key = key;
		this.value = value;
	}

	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return key+"-"+value;
	}


}
