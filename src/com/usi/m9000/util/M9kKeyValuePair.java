/**
 * 
 */
package com.usi.m9000.util;

import java.io.Serializable;

/**
 * @author sramasamy
 *
 */
public class M9kKeyValuePair implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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

	@Override
	public String toString() {
		return "M9kKeyValuePair [key=" + key + ", value=" + value + "]";
	}

	
}
