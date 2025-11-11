/**
 * 
 */
package com.usi.m9000.dto;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.usi.m9000.util.M9kConstants;

/**
 * @author sramasamy
 *
 */
public class UsersDTO implements HttpSessionBindingListener, Comparable<UsersDTO>{
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(UsersDTO.class);
private int id;
private String userName;
private String password;
private String role;
private boolean editMode;
private int editedStationId;
private String sessionId;
private String ipAddress;
private String displayName="";
private String errorMessage;

public UsersDTO() {
	super();
}

public UsersDTO(String userName, String password, String role) {
	super();
	this.userName = userName;
	this.password = password;
	this.role = role;
}

public int getId() {
	return id;
}

public void setId(int id) {
	this.id = id;
}

public String getUserName() {
	return userName;
}
public void setUserName(String userName) {
	this.userName = userName;
}
public String getPassword() {
	return password;
}
public void setPassword(String password) {
	this.password = password;
}
public String getRole() {
	return role;
}
public void setRole(String role) {
	this.role = role;
}


/* (non-Javadoc)
 * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
 */
@Override
public void valueBound(HttpSessionBindingEvent event) {
	@SuppressWarnings("unchecked")
	Set<UsersDTO> logins = (Set<UsersDTO>) event.getSession().getServletContext().getAttribute("logins");
	if (logins == null)
	{
		logger.debug("ValueBound method: Logins object from servletContext is null");
		logins = new HashSet<UsersDTO>(10);
	}
	else if (logins.contains(this))
	{
		if (!getRole().equalsIgnoreCase(M9kConstants.ADMIN) && !isEditMode())
		{
			logger.debug("ValueBound method called and the set already contains the user obejct. hence removing"+this);
			// Removed just to ensure updated User object is added
			logins.remove(this);
		}
	}
    logins.add(this);
    event.getSession().getServletContext().setAttribute("logins", logins);
	logger.debug("Inside valueBound method..."+this);
}
/* (non-Javadoc)
 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
 */
@Override
public void valueUnbound(HttpSessionBindingEvent event) {
	@SuppressWarnings("unchecked")
	Set<UsersDTO> logins = (Set<UsersDTO>) event.getSession().getServletContext().getAttribute("logins");
	if (logins != null)
	{
		logger.debug("Removing user "+this);
		logins.remove(this);
	}
	else
	{
		logger.debug("Inside valueUnBound: Logins object from servletContext is null");
	}
    logger.debug("Inside valueUnbound method..."+this);
}
/**
 * @return the editMode
 */
public boolean isEditMode() {
	return editMode;
}
/**
 * @param editMode the editMode to set
 */
public void setEditMode(boolean editMode) {
	this.editMode = editMode;
}
/**
 * @return the editedStationId
 */
public int getEditedStationId() {
	return editedStationId;
}
/**
 * @param editedStationId the editedStationId to set
 */
public void setEditedStationId(int editedStation) {
	this.editedStationId = editedStation;
}
/**
 * @return the sessionId
 */
public String getSessionId() {
	return sessionId;
}
/**
 * @param sessionId the sessionId to set
 */
public void setSessionId(String sessionId) {
	this.sessionId = sessionId;
}
/* (non-Javadoc)
 * @see java.lang.Object#hashCode()
 */
@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
	result = prime * result + ((userName == null) ? 0 : userName.hashCode());
	return result;
}
/* (non-Javadoc)
 * @see java.lang.Object#equals(java.lang.Object)
 */
@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	UsersDTO other = (UsersDTO) obj;
	if (sessionId == null) {
		if (other.sessionId != null)
			return false;
	} else if (!sessionId.equals(other.sessionId))
		return false;
	if (userName == null) {
		if (other.userName != null)
			return false;
	} else if (!userName.equals(other.userName))
		return false;
	return true;
}
/**
 * @return the ipAddress
 */
public String getIpAddress() {
	return ipAddress;
}
/**
 * @param ipAddress the ipAddress to set
 */
public void setIpAddress(String ipAddress) {
	this.ipAddress = ipAddress;
}
public String getDisplayName() {
	return displayName;
}
public void setDisplayName(String displayName) {
	this.displayName = displayName;
}
/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
@Override
public String toString() {
	return "UsersDTO [userName=" + userName 
			+ ", role=" + role + ", editMode=" + editMode
			+ ", editedStationId=" + editedStationId + ", sessionId="
			+ sessionId + ", ipAddress=" + ipAddress + "]";
}
public String getErrorMessage() {
	return errorMessage;
}
public void setErrorMessage(String errorMessage) {
	this.errorMessage = errorMessage;
}
@Override
public int compareTo(UsersDTO o) {
	return this.userName.toLowerCase().compareTo(o.getUserName().toLowerCase());
}

}
