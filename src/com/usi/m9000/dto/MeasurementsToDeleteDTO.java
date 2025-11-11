/**
 * 
 */
package com.usi.m9000.dto;

import java.io.Serializable;

/**
 * @author sramasamy
 *
 */
public class MeasurementsToDeleteDTO implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(MeasurementsToDeleteDTO.class);
private int id;
private String name;
private String chassis;

public MeasurementsToDeleteDTO() {
	super();
}

public int getId() {
	return id;
}

public void setId(int id) {
	this.id = id;
}

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
 * @return the chassis
 */
public String getChassis() {
	return chassis;
}

/**
 * @param chassis the chassis to set
 */
public void setChassis(String chassis) {
	this.chassis = chassis;
}


}
