package com.usi.m9000.util;

import java.util.Formatter;
import java.util.List;

import com.usi.m9000.dao.EmailDAO;
import com.usi.m9000.data.M9kDAOFactory;
import com.usi.m9000.dto.EmailAddressDTO;
import com.usi.m9000.dto.EmailReportsSettingsDTO;
import com.usi.m9000.dto.EmailSettingsDTO;
import com.usi.m9000.dto.M9kStatusReportDTO;
import com.usi.m9000.dto.StationReportDTO;

public class M9kReportUtil {

	private static M9kDAOFactory m9kDAOFactory;
	private static EmailDAO emailDAO;
	private static EmailSettingsDTO emailSettingsDTO;
	private static EmailReportsSettingsDTO emailReportsSettingsDTO;
	private static List<EmailAddressDTO> lstOfSubscribedEmails;

	static
	{
		m9kDAOFactory = M9kDAOFactory.getDAOFactory(M9kConstants.MYSQL);
		emailDAO = m9kDAOFactory.getEmailDAO();
	}
	public static StringBuffer generateDailyStatusReport(M9kStatusReportDTO m9kStatusReportDTO) throws Exception
	{
//		File reportsFile = new File(m9kReportsDTO.getReportsDir()+m9kReportsDTO.getReportFileName());
//		BufferedWriter bw = new BufferedWriter(new FileWriter(reportsFile));
		StringBuffer m9kReportContent = new StringBuffer();
		 Formatter formatter = new Formatter(m9kReportContent);
//		 m9kReportContent.append("\t\t\t\t\t"+m9kStatusReportDTO.getReportTitle());
//		m9kReportContent.append(M9kConstants.NEWLINE);
//		m9kReportContent.append(M9kConstants.NEWLINE);

		 formatter.format("%1$100s%n%n",m9kStatusReportDTO.getReportTitle());
//		m9kReportContent.append("Date of Report:\t"+m9kStatusReportDTO.getReportDate());
//		m9kReportContent.append(M9kConstants.NEWLINE);
		formatter.format("%1$-20s %2$s%n%n","Report#:",m9kStatusReportDTO.getReportId());
		 formatter.format("%1$-20s %2$s%n","Date of Report:",m9kStatusReportDTO.getReportDate());

//		m9kReportContent.append("Time of Report:\t"+m9kStatusReportDTO.getReportTime());
//		m9kReportContent.append(M9kConstants.NEWLINE);
		formatter.format("%1$-20s %2$s%n","Time of Report:",m9kStatusReportDTO.getReportTime());

//		m9kReportContent.append("Report#:\t"+m9kStatusReportDTO.getReportId());
//		m9kReportContent.append(M9kConstants.NEWLINE);
//		m9kReportContent.append(M9kConstants.NEWLINE);
//		formatter.format("%1$-20s %2$s%n%n","Report#:",m9kStatusReportDTO.getReportId());
		
//		m9kReportContent.append(m9kStatusReportDTO.getReportHeader());
//		m9kReportContent.append(M9kConstants.NEWLINE);
		formatter.format("%1$s %n",m9kStatusReportDTO.getReportHeader());
		
//		m9kReportContent.append(m9kStatusReportDTO.getReportBody());
		formatter.format("%1$s %n",m9kStatusReportDTO.getReportBody());
		return m9kReportContent;
	}
	
	public static StringBuffer generateStationSpecificReport(M9kStatusReportDTO m9kStatusReportDTO, StationReportDTO stationReportDTO) throws Exception
	{
//		File reportsFile = new File(m9kReportsDTO.getReportsDir()+m9kReportsDTO.getReportFileName());
//		BufferedWriter bw = new BufferedWriter(new FileWriter(reportsFile));
		StringBuffer m9kReportContent = new StringBuffer();
		 Formatter formatter = new Formatter(m9kReportContent);
//		 m9kReportContent.append("\t\t\t\t\t"+m9kStatusReportDTO.getReportTitle());
//		m9kReportContent.append(M9kConstants.NEWLINE);
//		m9kReportContent.append(M9kConstants.NEWLINE);
		 formatter.format("%1$100s%n%n",m9kStatusReportDTO.getReportTitle());
//		m9kReportContent.append("Date of Report:\t"+m9kStatusReportDTO.getReportDate());
//		m9kReportContent.append(M9kConstants.NEWLINE);
		 formatter.format("%1$-15s %2$s%n","Date of Report:",m9kStatusReportDTO.getReportDate());

//		m9kReportContent.append("Time of Report:\t"+m9kStatusReportDTO.getReportTime());
//		m9kReportContent.append(M9kConstants.NEWLINE);
		formatter.format("%1$-15s %2$s%n","Time of Report:",m9kStatusReportDTO.getReportTime());
		formatter.format("%1$-20s %2$s%n","Report#:",m9kStatusReportDTO.getReportId());
		
		formatter.format("%n%1$-15s %2$20s %3$25s %4$s %5$25s %6$s %n%n","Substation:",stationReportDTO.getStationKey(),"# of Analog Channels",stationReportDTO.getAnalogCount(),"# of Digital Channels",stationReportDTO.getDigitalCount());

//		formatter.format("%1$s%n", stationReportDTO.getWarningSummary());
		m9kReportContent.append(stationReportDTO.getWarningSummary());
//		m9kReportContent.append("Report#:\t"+m9kStatusReportDTO.getReportId());
//		m9kReportContent.append(M9kConstants.NEWLINE);
//		m9kReportContent.append(M9kConstants.NEWLINE);
//		formatter.format("%1$-20s %2$s%n%n","Report#:",m9kStatusReportDTO.getReportId());
		
//		m9kReportContent.append(m9kStatusReportDTO.getReportHeader());
//		m9kReportContent.append(M9kConstants.NEWLINE);
//		formatter.format("%1$s %n",m9kStatusReportDTO.getReportHeader());
		
//		m9kReportContent.append(m9kStatusReportDTO.getReportBody());
		formatter.format("%1$s %n",m9kStatusReportDTO.getReportBody());
		return m9kReportContent;
	}

	public static EmailSettingsDTO getEmailSettingsDTO() {
		emailSettingsDTO = emailDAO.getEmailSettings();
		return emailSettingsDTO;
	}

	public static EmailReportsSettingsDTO getEmailReportsSettingsDTO() {
		emailReportsSettingsDTO = emailDAO.getEmailReportsSettings();
		return emailReportsSettingsDTO;
	}

	public static List<EmailAddressDTO> getLstOfSubscribedEmails() {
		return emailDAO.getEmailsList();
	}

}
