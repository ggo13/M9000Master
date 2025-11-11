package com.usi.m9000.common.email;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.usi.m9000.dto.EmailAddressDTO;
import com.usi.m9000.dto.EmailReportsSettingsDTO;
import com.usi.m9000.dto.EmailSettingsDTO;
import com.usi.m9000.util.M9kReportUtil;
 
public class SendMailUSI {
//	private static String emailHost;
//	private static int emailPort;
//	private static String fromEmail;
//	private static String emailPwd;
//	private static String[] emailIds;
//	private static PropertiesConfiguration config = null;
	private static EmailSettingsDTO emailSettingsDTO;
	private static EmailReportsSettingsDTO emailReportsSettingsDTO;

	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(SendMailUSI.class);
//	static
//	{
//		try {
//			config = new PropertiesConfiguration("m9k-master.properties");
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			logger.error("Error in reading m9k-master.properties files. Enabling health poll by default.",e);
//			config = null;
//		}
//
//	}
	public static void testClassAvailability()
	{
		logger.debug("Yes! SendMailUSI is available in station master");
	}
	public static void sendEmail(String sub, String body)
	{
		emailSettingsDTO = M9kReportUtil.getEmailSettingsDTO();
		emailReportsSettingsDTO = M9kReportUtil.getEmailReportsSettingsDTO();
//		final String username = "alerts@faultrecorder.com";
//		final String password = "Usi#1Dfr";
//		final String username = "sramasamy@faultrecorder.com";
//		final String password = "S7aspeP";
//		final String username = "vrsarav@gmail.com";
//		final String password = "@ttitude100%";
//		try {
//			config = new PropertiesConfiguration("m9k-master.properties");
//			String emailNotify = config.getString("email-notification");
//			if (emailNotify == null || !emailNotify.equalsIgnoreCase(M9kConstants.ENABLE) )
//			if (!M9kUtils.isEmailNotificationEnabled())
		if (!emailSettingsDTO.isEnableEmail())
			{
				logger.info("Email notification is not enabled");
				return;
			}
//		} catch (ConfigurationException e) {
//			logger.error("Error in reading m9k-master.properties files. No Email server details. Quitting",e);
////			config = null;
//			return;
//		}
 
 
		try {
			logger.debug("USI Auth successfull");
			Message message = getEmailMessage();
			message.setSubject(sub);
			message.setText(body);
			
			logger.debug("USI About to send...");
			Transport.send(message);
 
			logger.debug("USI Done");
 
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception while sending email",e);
		}
	}

	
	public static void sendEmailWithAttachment(String sub, String body,File pdfFileToAttach)
	{
		logger.debug("In sendEmailWithAttachment ...");
		emailSettingsDTO = M9kReportUtil.getEmailSettingsDTO();
		logger.debug("emailSettingsDTO "+emailSettingsDTO);
		emailReportsSettingsDTO = M9kReportUtil.getEmailReportsSettingsDTO();
		logger.debug("emailReportsSettingsDTO "+emailReportsSettingsDTO);
//		final String username = "alerts@faultrecorder.com";
//		final String password = "Usi#1Dfr";
//		final String username = "sramasamy@faultrecorder.com";
//		final String password = "S7aspeP";
//		final String username = "vrsarav@gmail.com";
//		final String password = "@ttitude100%";
//		try {
			logger.debug("In the SendMailUSI for sendEmailWithAttachment");
//			config = new PropertiesConfiguration("m9k-master.properties");
			logger.debug("After config init ");
//			String emailNotify = config.getString("email-notification");
//			logger.debug("Email notify from config "+emailNotify);
//			if (emailNotify == null || !emailNotify.equalsIgnoreCase(M9kConstants.ENABLE) )
//			if (!M9kUtils.isEmailNotificationEnabled())
			if (!emailSettingsDTO.isEnableEmail())
			{
				logger.info("Email notification is not enabled");
				return;
			}
//		} catch (ConfigurationException e) {
//			logger.error("Error in reading m9k-master.properties files. No Email server details. Quitting",e);
//			config = null;
//			return;
//		}
 
		try {
			logger.debug("USI Auth successfull");
			Message message = getEmailMessage();
			message.setSubject(sub);
//			message.setText(body);
			
			// Create the message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			 // Part two is attachment

//	         messageBodyPart.setText(body);
	         messageBodyPart.setContent("<pre><p style=\"font-family:arial\">"+body+"</p></pre>", "text/html");
	      // Create a multipar message
	         Multipart multipart = new MimeMultipart();

	         // Set text message part
	         multipart.addBodyPart(messageBodyPart);

			messageBodyPart = new MimeBodyPart();
	         messageBodyPart.attachFile(pdfFileToAttach);
//	         messageBodyPart.setFileName(pdfFileToAttach.getName());
//	         messageBodyPart.setFileName(pdfFileToAttach.getName());
	         multipart.addBodyPart(messageBodyPart);

	      // Send the complete message parts
	         message.setContent(multipart);

			logger.debug("USI About to send...");
			Transport.send(message);
 
			logger.debug("USI Done");
 
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception while sending email",e);
		}
	}

	// Not used anywhere except locally to test the functionality
	public static void sendEmail(String from, String to, String sub, String body)
	{
		emailSettingsDTO = M9kReportUtil.getEmailSettingsDTO();
		emailReportsSettingsDTO = M9kReportUtil.getEmailReportsSettingsDTO();
//		final String username = "alerts@faultrecorder.com";
//		final String password = "Usi#1Dfr";
//		final String username = "sramasamy@faultrecorder.com";
//		final String password = "";
//		final String username = "vrsarav@gmail.com";
//		final String password = "";
//		try {
//			config = new PropertiesConfiguration("m9k-master.properties");
//			String emailNotify = config.getString("email-notification");
//			if (emailNotify == null || !emailNotify.equalsIgnoreCase(M9kConstants.ENABLE) )
		if (!emailSettingsDTO.isEnableEmail())
			{
				logger.info("Email notification is not enabled in m9k-master properties file. Hence logging to Station-Info.log/OS logs file.");
				logger.info("Email: \nFrom "+from+"\nTo "+to+ "\nSubject: "+sub+"\n Message: \n\t"+body);
//				return;
			}
			
//		} catch (ConfigurationException e) {
//			logger.error("Error in reading m9k-master.properties files. No Email server details. Quitting",e);
//			config = null;
//			return;
//		}
 
		try {
			logger.debug("USI Auth successfull");
			Message message = getEmailMessage();
			message.setSubject(sub);
			message.setContent("<pre><p style=\"font-family:arial\">"+body+"</p></pre>", "text/html");
			
			logger.debug("USI About to send...");
			Transport.send(message);
 
			logger.debug("USI Done");
 
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception while sending email",e);
		}
	}

	private static Properties getEmailProperties()
	{
		Properties props = new Properties();
		props.put("mail.smtp.host", emailSettingsDTO.getEmailServerHost());
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", emailSettingsDTO.getEmailSmtpPort());
		props.put("mail.smtp.ssl.trust", emailSettingsDTO.getEmailServerHost());
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		return props;
	}

	private static Message getEmailMessage() throws Exception
	{
		Message emailMessage;
		Session session = Session.getInstance(getEmailProperties(),
				  new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
//						return new PasswordAuthentication(M9kUtils.getFromEmail(), M9kUtils.getEmailPwd());
						return new PasswordAuthentication(emailSettingsDTO.getFromEmail(), emailSettingsDTO.getEmailServerPassword());
					}
				  });
		emailMessage = new MimeMessage(session);
		emailMessage.setFrom(new InternetAddress(emailSettingsDTO.getFromEmail()));
		for (Iterator<EmailAddressDTO> iterator = M9kReportUtil.getLstOfSubscribedEmails().iterator(); iterator.hasNext();) {
			EmailAddressDTO emailAddressDTO = iterator.next();
			
			logger.debug("To address to be sent "+emailAddressDTO.getEmailAddress());
			emailMessage.addRecipients(Message.RecipientType.TO,
					InternetAddress.parse(emailAddressDTO.getEmailAddress()));				
		}
		return emailMessage;
	}
	public static void main(String[] args) {
//		SendMailUSI.sendEmail("sramasamy@faultrecorder.com", "vrsarav@gmail.com",  "Testing", "Worked? ");
//		try {
////			config = new PropertiesConfiguration("m9k-master.properties");
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			logger.error("Error in reading m9k-master.properties files. Enabling health poll by default.",e);
//			config = null;
//		}
		SendMailUSI.sendEmail("alerts@faultrecorder.com","sramasamy@faultrecorder.com",  "Testing", "Worked? ");
 
//		final String username = "vrsarav@gmail.com";
//		final String password = "@ttitude100%";
// 
//		Properties props = new Properties();
////		props.put("mail.smtp.auth", "true");
////		props.put("mail.smtp.starttls.enable", "true");
////		props.put("mail.smtp.host", "smtp.gmail.com");
////		props.put("mail.smtp.port", "465");
//		
//		props.put("mail.smtp.host", "smtp.gmail.com");
//		props.put("mail.smtp.socketFactory.port", "465");
//		props.put("mail.smtp.socketFactory.class",
//				"javax.net.ssl.SSLSocketFactory");
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.port", "465");
////		props.put("mail.smtp.connectiontimeout", "5000");
////	    props.put("mail.smtp.timeout", "5000");
// 
//		Session session = Session.getInstance(props,
//		  new javax.mail.Authenticator() {
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication(username, password);
//			}
//		  });
// 
//		try {
//			System.out.println("Auth successfull");
//			Message message = new MimeMessage(session);
//			message.setFrom(new InternetAddress("vrsarav@gmail.com"));
//			message.setRecipients(Message.RecipientType.TO,
//				InternetAddress.parse("sramasamy@faultrecorder.com"));
//			message.setSubject("Testing Subject");
//			message.setText("IS this working?,"
//				+ "\n\n No spam to my email, please!");
//			
//			System.out.println("About to send...");
//			Transport.send(message);
// 
//			System.out.println("Done");
// 
//		} catch (MessagingException e) {
//			throw new RuntimeException(e);
//		}
	}

//	public static String getEmailHost() {
//		emailHost=config.getString("mail-server-host");
//		return emailHost;
//	}
//
//	public static void setEmailHost(String emailHost) {
//		SendMailUSI.emailHost = emailHost;
//	}
//
//	public static int getEmailPort() {
//		emailPort=config.getInt("mail-server-smtp-port",587);
//		return emailPort;
//	}
//
//	public static void setEmailPort(int emailPort) {
//		SendMailUSI.emailPort = emailPort;
//	}
//
//	public static String getFromEmail() {
//		fromEmail=config.getString("from-email");
//		return fromEmail;
//	}
//
//	public static void setFromEmail(String fromEmail) {
//		SendMailUSI.fromEmail = fromEmail;
//	}
//
//	public static String getEmailPwd() {
//		emailPwd=config.getString("email-pwd");
//		return emailPwd;
//	}
//
//	public static void setEmailPwd(String emailPwd) {
//		SendMailUSI.emailPwd = emailPwd;
//	}
//
//	public static String[] getEmailIds() {
//		emailIds=config.getStringArray("email-ids");
//		return emailIds;
//	}
//
//	public static void setEmailIds(String[] emailIds) {
//		SendMailUSI.emailIds = emailIds;
//	}
}