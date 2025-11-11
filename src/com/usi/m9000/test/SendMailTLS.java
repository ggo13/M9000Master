package com.usi.m9000.test;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
 
public class SendMailTLS {
 
	public static void sendEmail(String from, String to, String sub, String body)
	{
		final String username = "vrsarav@gmail.com";
		final String password = "@ttitude100%";
 
		Properties props = new Properties();
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.starttls.enable", "true");
//		props.put("mail.smtp.host", "smtp.gmail.com");
//		props.put("mail.smtp.port", "465");
		
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
//		props.put("mail.smtp.connectiontimeout", "5000");
//	    props.put("mail.smtp.timeout", "5000");
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
 
		try {
			System.out.println("Auth successfull");
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
			message.addRecipients(Message.RecipientType.TO,
					InternetAddress.parse("vrs_rama@yahoo.com"));
			message.setSubject(sub);
			message.setText(body);
			
			System.out.println("About to send...");
			Transport.send(message);
 
			System.out.println("Done");
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		SendMailTLS.sendEmail("vrsarav@gmail.com", "sramasamy@faultrecorder.com", "Testing", "Worked? ");
 
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
}