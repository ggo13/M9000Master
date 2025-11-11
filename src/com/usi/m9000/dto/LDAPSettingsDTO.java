/**
 * 
 */
package com.usi.m9000.dto;

import java.io.Serializable;

/**
 * @author sramasamy
 *
 */
public class LDAPSettingsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean enableLdap = false;
	private String ldapServerHost;
	private String ldapPort="389";
	private String ldapDefaultSearchBase;
	private String ldapAdminGroup;
	private String ldapGuestGroup;

	/**
	 * 
	 */
	public LDAPSettingsDTO() {
		super();
	}

	public LDAPSettingsDTO(boolean enableLdap, String ldapServerHost, String ldapPort, String ldapDefaultSearchBase,
			String ldapAdminGroup, String ldapGuestGroup) {
		super();
		this.enableLdap = enableLdap;
		this.ldapServerHost = ldapServerHost;
		this.ldapPort = ldapPort;
		this.ldapDefaultSearchBase = ldapDefaultSearchBase;
		this.ldapAdminGroup = ldapAdminGroup;
		this.ldapGuestGroup = ldapGuestGroup;
	}

	/**
	 * @return the enableLdap
	 */
	public boolean isEnableLdap() {
		return enableLdap;
	}

	/**
	 * @param enableLdap the enableLdap to set
	 */
	public void setEnableLdap(boolean enableLdap) {
		this.enableLdap = enableLdap;
	}

	/**
	 * @return the ldapServerHost
	 */
	public String getLdapServerHost() {
		return ldapServerHost;
	}

	/**
	 * @param ldapServerHost the ldapServerHost to set
	 */
	public void setLdapServerHost(String ldapServerHost) {
		this.ldapServerHost = ldapServerHost;
	}

	/**
	 * @return the ldapPort
	 */
	public String getLdapPort() {
		return ldapPort;
	}

	/**
	 * @param ldapPort the ldapPort to set
	 */
	public void setLdapPort(String ldapPort) {
		this.ldapPort = ldapPort;
	}

	/**
	 * @return the ldapDefaultSearchBase
	 */
	public String getLdapDefaultSearchBase() {
		return ldapDefaultSearchBase;
	}

	/**
	 * @param ldapDefaultSearchBase the ldapDefaultSearchBase to set
	 */
	public void setLdapDefaultSearchBase(String ldapDefaultSearchBase) {
		this.ldapDefaultSearchBase = ldapDefaultSearchBase;
	}

	/**
	 * @return the ldapAdminGroup
	 */
	public String getLdapAdminGroup() {
		return ldapAdminGroup;
	}

	/**
	 * @param ldapAdminGroup the ldapAdminGroup to set
	 */
	public void setLdapAdminGroup(String ldapAdminGroup) {
		this.ldapAdminGroup = ldapAdminGroup;
	}

	/**
	 * @return the ldapGuestGroup
	 */
	public String getLdapGuestGroup() {
		return ldapGuestGroup;
	}

	/**
	 * @param ldapGuestGroup the ldapGuestGroup to set
	 */
	public void setLdapGuestGroup(String ldapGuestGroup) {
		this.ldapGuestGroup = ldapGuestGroup;
	}


	
}
