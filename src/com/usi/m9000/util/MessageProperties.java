/**
 * 
 */
package com.usi.m9000.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sramasamy
 *
 */
public class MessageProperties {
private Map<String, Object> mapMessageProperties;
public Object getProperty(String keyProperty)
{
	Object value = null; 
	if (mapMessageProperties != null)
	{
		value = mapMessageProperties.get(keyProperty);
	}
	
	return value;
}

public void addProperty(String key, Object value)
{
	if (mapMessageProperties == null)
	{
		mapMessageProperties = new HashMap<String, Object>();
	}
	mapMessageProperties.put(key, value);
}

/**
 * @return the mapMessageProperties
 */
public Map<String, Object> getMapMessageProperties() {
	return mapMessageProperties;
}

}
