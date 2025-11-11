/**
 * 
 */
package com.usi.m9000.dto;

import java.io.Serializable;

/**
 * @author sramasamy
 *
 */
public class EmailSettingsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean enableEmail = false;
	private String emailServerHost;
	private String emailSmtpPort="587";
	private String fromEmail;
	private String emailServerPassword;

	/**
	 * 
	 */
	public EmailSettingsDTO() {
		super();
	}

	public EmailSettingsDTO(boolean enableEmail, String emailServerHost, String emailSmtpPort, String fromEmail,
			String emailServerPassword) {
		super();
		this.enableEmail = enableEmail;
		this.emailServerHost = emailServerHost;
		this.emailSmtpPort = emailSmtpPort;
		this.fromEmail = fromEmail;
		this.emailServerPassword = emailServerPassword;
	}

	public boolean isEnableEmail() {
		return enableEmail;
	}

	public void setEnableEmail(boolean enableEmail) {
		this.enableEmail = enableEmail;
	}

	public String getEmailServerHost() {
		return emailServerHost;
	}

	public void setEmailServerHost(String emailServerHost) {
		this.emailServerHost = emailServerHost;
	}

	public String getEmailSmtpPort() {
		return emailSmtpPort;
	}

	public void setEmailSmtpPort(String emailSmtpPort) {
		this.emailSmtpPort = emailSmtpPort;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getEmailServerPassword() {
		return emailServerPassword;
	}

	public void setEmailServerPassword(String emailServerPassword) {
		this.emailServerPassword = emailServerPassword;
	}

	@Override
	public String toString() {
		return "EmailSettingsDTO [enableEmail=" + enableEmail + ", emailServerHost=" + emailServerHost
				+ ", emailSmtpPort=" + emailSmtpPort + ", fromEmail=" + fromEmail + "]";
	}

	
}
