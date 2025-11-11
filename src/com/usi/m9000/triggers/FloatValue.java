package com.usi.m9000.triggers;

import com.usi.m9000.util.M9kXMLConstants;

public class FloatValue {
private String name;
private String value;
/**
 * @return the name
 */
public String getName() {
	return name;
}
/**
 * @param name the name to set
 */
public void setName(String name) {
	this.name = name;
}
/**
 * @return the value
 */
public String getValue() {
	return value;
}
/**
 * @param value the value to set
 */
public void setValue(String value) {
	this.value = value;
}

public String getFloatValueInput()
{
	StringBuffer floatValueTag = new StringBuffer(); 
	floatValueTag.append(M9kXMLConstants.XML_ALGORITHMS_INPUT);
	floatValueTag.append(M9kXMLConstants.XML_FLOAT_PREFIX);
	floatValueTag.append(getName());
	floatValueTag.append(M9kXMLConstants.XML_VALUE_INPUT);
	return floatValueTag.toString();	
}
}
