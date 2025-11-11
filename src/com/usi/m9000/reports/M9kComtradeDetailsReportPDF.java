/**
 * 
 */
package com.usi.m9000.reports;

/**
 * @author sramasamy
 *
 */
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.awt.Color;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;

import com.usi.SubStationDocument.SubStation;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.EventChannelDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.dto.TriggerChannelDTO;
import com.usi.m9000.reports.util.Column;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kUtils;
import com.usi.m9000.xml.M9000XmlConfig;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.component.MultiPageListBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;

public class M9kComtradeDetailsReportPDF {

  
JTable tblSample;
JRDataSource dataSource;
String reportTitle = "Station Configuration Report";
List<Column> columnsList;
private StationDTO stationDto;
private String stationId = "Unkown";
private String stationName = "Unknown";

StyleBuilder boldStyle;
StyleBuilder boldCenteredStyle;
StyleBuilder columnTitleStyle;
StyleBuilder titleStyle;
StyleBuilder subTitleStyle;
StyleBuilder boldRtAlignedStyle ;
StyleBuilder boldLtAlignedStyle;
StyleBuilder borderStyle;
StyleBuilder innerBorderStyle;

private SubStation subStation;
private M9000XmlConfig m9kConfig;
private MultiPageListBuilder multiPageListBuilder;
private boolean transducerEnabled = false;
static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kComtradeDetailsReportPDF.class);

 public M9kComtradeDetailsReportPDF() {
	
	 boldStyle         = stl.style().bold();
	 
	 boldCenteredStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
			 			.setFontSize(10);
	 columnTitleStyle  = stl.style(boldCenteredStyle)
			 				.setBorder(stl.pen2Point())
			 				.setBackgroundColor(Color.LIGHT_GRAY);
	 titleStyle = stl.style(boldCenteredStyle)
             .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)  
             .setFontSize(13);
	 subTitleStyle = stl.style(boldStyle)
             .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)  
             .setFontSize(12);
	 boldRtAlignedStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
				.setFontSize(10);
	 boldLtAlignedStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
				.setFontSize(10);
	 borderStyle = stl.style().setBorder(stl.pen1Point().setLineWidth(2.0f));
	 innerBorderStyle = stl.style().setBorder(stl.pen1Point().setLineWidth(1.0f));
	 
	 

 }

 public M9kComtradeDetailsReportPDF(String reportTitle) {
  this();
  this.reportTitle = reportTitle;
 }
 
 public M9kComtradeDetailsReportPDF(StationDTO stationDto, String reportTitle) {
	  this();
	  this.stationDto = stationDto;
	  this.reportTitle = reportTitle;
	  logger.debug("Entered M9kComtradeDetailsReportPDF with station Name "+this.stationDto.getSystemStationName()+" report title "+reportTitle);
	 }

public  JasperReportBuilder buildPDF() throws Exception
 {
	JasperReportBuilder stationConfigReport = report();
	// START: Fails to export as PDF for huge station with LANDSCAPE 
//	stationConfigReport.setPageFormat(PageType.A4, PageOrientation.LANDSCAPE);
	// END: Fails to export as PDF for huge station with LANDSCAPE
	multiPageListBuilder = cmp.multiPageList();
	multiPageListBuilder.setSplitType(SplitType.PREVENT);
	 if (stationDto != null)
	 {
		 stationId = stationDto.getSystemStationId().toString();
		 stationName = stationDto.getSystemStationName();
	 }

	 m9kConfig = new M9000XmlConfig();
	 m9kConfig.loadStationXmlFile(new StringReader(stationDto.getConfigXml()));
	 stationDto = m9kConfig.getStationDto();
		subStation = m9kConfig.getSubstation();
		try {
//			for (Column column : getColumnsList()) {
//				if (column.getColumnLength() > 0)
//				{
////					System.out.println("Column "+column.getTitle());
//					report.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) .setWidth(column.getColumnLength()));
//				}
//				else
//				{
//					report.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) .setWidth(40));
//				}
//			}
			int analogChannelsCount;
			analogChannelsCount = getStationDto().getSystemAnalogChannelsCount();
	         multiPageListBuilder
    		 //TODO: Add company name when implemented
    		 .add(cmp.text("").setStyle(boldLtAlignedStyle), cmp.text(reportTitle).setStyle(titleStyle).setFixedWidth(250),cmp.text(new SimpleDateFormat("MM/dd/yyyy").format(new Date())).setStyle(boldRtAlignedStyle))//shows report title
//    		 .newRow()
    		 .add(cmp.filler().setHeight(20))
//    		 .newRow()
    		 .add(cmp.horizontalList().add(cmp.verticalList()
    				 .add(cmp.verticalGap(10),cmp.horizontalList(cmp.text("  Station Name: ").setStyle(boldLtAlignedStyle), cmp.text(stationName).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))).setFixedWidth(200),
    					  cmp.verticalGap(10),
    					  cmp.horizontalList().add(cmp.horizontalList().add(cmp.text("  Station Id: ").setStyle(boldLtAlignedStyle), cmp.text(stationId)).setFixedWidth(120)
    							  ,cmp.text("  # of Units : ").setStyle(boldLtAlignedStyle), cmp.text(subStation.getDFRs().sizeOfDFRArray())
         						 ).setFixedWidth(240),
         				cmp.verticalGap(10),
         				(analogChannelsCount > 0)?
  					  cmp.horizontalList().add(cmp.text("  #Analogs: ").setStyle(boldLtAlignedStyle), cmp.text(analogChannelsCount)
							  ,cmp.horizontalList().add(cmp.text("  #Measurements : ").setStyle(boldLtAlignedStyle), cmp.text(subStation.getMeasurements().sizeOfMeasurementArray())).setFixedWidth(180)
     						 ).setFixedWidth(300) : cmp.horizontalList().add(cmp.text("  #Analogs: ").setStyle(boldLtAlignedStyle), cmp.text(analogChannelsCount)).setFixedWidth(120)
     						 ,
     				cmp.verticalGap(10),
     				cmp.horizontalList().add(cmp.text("  #Events: ").setStyle(boldLtAlignedStyle), cmp.text(getStationDto().getSystemDigitalChannelsCount())
   						 ).setFixedWidth(120),
   				    cmp.verticalGap(10)
     				)
     				,cmp.verticalList()
     				.add(cmp.verticalGap(10),
     						cmp.horizontalList()
     						.add(
     						cmp.verticalList()
     						.add( cmp.horizontalList(cmp.horizontalGap(50),
     						cmp.text("  TRANSIENT ").setStyle(boldLtAlignedStyle), 
     						cmp.text(" LONGTERM ").setStyle(boldLtAlignedStyle)).setFixedWidth(200),
     						cmp.horizontalList().add(cmp.horizontalList().add(cmp.text("  Frequency: ").setStyle(boldLtAlignedStyle), cmp.text(getStationDto().getSystemSampleRate())).setFixedWidth(120)
        							  ,cmp.horizontalGap(10), cmp.text(getStationDto().getSystemLongTermSampleRate())),
        					cmp.horizontalList().add(cmp.horizontalList().add(cmp.text("  Prefault: ").setStyle(boldLtAlignedStyle), cmp.text(getStationDto().getSystemPrefaultTime()+" ms")).setFixedWidth(120)
                							  ,cmp.horizontalGap(10), cmp.text(getStationDto().getSystemLtrPrefaultTime()+" s")),
           					cmp.horizontalList().add(cmp.horizontalList().add(cmp.text("  Postfault: ").setStyle(boldLtAlignedStyle), cmp.text(getStationDto().getSystemPostfaultTime()+" ms")).setFixedWidth(120)
                      							  ,cmp.horizontalGap(10), cmp.text(getStationDto().getSystemLtrPostfaultTime()+" s")),
                      		cmp.horizontalList().add(cmp.text("  Trigger Limit: ").setStyle(boldLtAlignedStyle), cmp.text(subStation.getDFRs().getDFRArray(0).getSystem().getTriggerLimit()+" ms")).setFixedWidth(150)
                      		
           						 ).setStyle(innerBorderStyle),
           				cmp.horizontalGap(10))
           				,cmp.verticalGap(10)
     				)
     				)
    				 .setStyle(borderStyle)
    				 )
//    		.newRow()
//    		.add(cmp.horizontalList().
//    				add((cmp.text("  Total No. Of Events:").setStyle(boldLtAlignedStyle)),cmp.text(totalNoOfEvents).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT))).setFixedWidth(250))
    		.add(cmp.filler().setHeight(25));

			stationConfigReport
			 .setColumnTitleStyle(columnTitleStyle)
	         .highlightDetailEvenRows()
//			  .title(cmp.text(reportTitle).setStyle(titleStyle))
//	                  .title(
//        		 cmp.multiPageList()
//        		 )

        		
			  .pageFooter(cmp.pageXofY().setStyle(boldCenteredStyle));
//			  .setPageFormat(PageType.A4, PageOrientation.LANDSCAPE);
//			  .setPageMargin(margin(20))
//			  .setPageMargin(margin().setLeft(50).setRight(20).setTop(50).setBottom(50));
			logger.debug("Checking PMU status to generate report "+getStationDto().isPmuEnabled());
			if (getStationDto().isPmuEnabled())
			{
				Integer pdcUdpPort;
				int i = 0;
			    List<Integer> lstPmuUdpPorts = getStationDto().getLstOfUDPserverPorts();
			    VerticalListBuilder vListUdpPorts = cmp.verticalList();
			    for (Iterator<Integer> iterator = lstPmuUdpPorts.iterator(); iterator
						.hasNext();) {
			    	i++;
					pdcUdpPort = iterator.next();
					vListUdpPorts.add(cmp.horizontalList().add(cmp.text("  PMU UDP Port "+i+" : ").setStyle(boldLtAlignedStyle), cmp.text(pdcUdpPort)).setFixedWidth(220));
				}
			    multiPageListBuilder.add( cmp.text("  PMU Details ").setStyle(subTitleStyle));
			    multiPageListBuilder.add(         				
						cmp.verticalGap(10),
 						cmp.horizontalList()
 						.add(
 						cmp.verticalList()
 						.add( 
 						cmp.horizontalList().add(cmp.horizontalList().add(cmp.text("  PMU Id: ").setStyle(boldLtAlignedStyle), cmp.text(getStationDto().getPdcId())).setFixedWidth(220)
    							  ,cmp.horizontalGap(10), cmp.text("  PMU Stream Type: ").setStyle(boldLtAlignedStyle), cmp.text(getStationDto().getPdcStreamType())),
 						cmp.horizontalList().add(cmp.horizontalList().add(cmp.text("  PMU Data Rate: ").setStyle(boldLtAlignedStyle), cmp.text(getStationDto().getPmuDataRate())).setFixedWidth(220)
    		    							  ,cmp.horizontalGap(10), cmp.text("  PMU Max Wait: ").setStyle(boldLtAlignedStyle), cmp.text(getStationDto().getPdcMaxWait()+" ms")),
    					cmp.horizontalList().add(cmp.horizontalList().add(cmp.text("  PMU TCP Port: ").setStyle(boldLtAlignedStyle), cmp.text(getStationDto().getPdcTcpPort())).setFixedWidth(220)),
    					cmp.verticalList().add(vListUdpPorts)
                  		
       						 ).setStyle(innerBorderStyle),
       				cmp.horizontalGap(10))
       				,cmp.verticalGap(10)
						
						);
			}
			if (getStationDto().getSystemAnalogChannelsCount() > 0)
			{
//				System.out.println("Analog channels count "+getStationDto().getSystemAnalogChannelsCount());
				JasperReportBuilder analogReport = buildAnalogReport();
				multiPageListBuilder.add(cmp.subreport(analogReport));
//				stationConfigReport.addTitle(cmp.pageBreak());
				logger.debug("Analog report complete. Now is Transducer enabled? " + isTransducerEnabled());
				// START: 03-Feb-2020 - Transducer implementation - include it in the report
				if (isTransducerEnabled())
				{
					JasperReportBuilder transducerChannelsReport = buildTranducerChannelsReport();
					multiPageListBuilder.add(cmp.subreport(transducerChannelsReport));
					logger.debug("Transducer Channels report complete");
				}
				// END: 03-Feb-2020
				if (m9kConfig.getLstVirtualChannels() != null && !m9kConfig.getLstVirtualChannels().isEmpty())
				{
					JasperReportBuilder virtualChannelsReport = buildVirtualChannelsReport();
					multiPageListBuilder.add(cmp.subreport(virtualChannelsReport));
					logger.debug("Virtual Channels report complete");
				}
				if (m9kConfig.isLineGroupsConfigured())
				{
					JasperReportBuilder lineGroupReport = buildLineGroupsReport();
//					stationConfigReport.title(cmp.verticalList(cmp.subreport(lineGroupReport)));
					multiPageListBuilder.add(cmp.subreport(lineGroupReport));
//					stationConfigReport.addTitle(cmp.pageBreak());
					logger.debug("Line Grooup report complete");
				}
//				report.detailFooter(cmp.horizontalList(cmp.horizontalGap(150), cmp.subreport(analogReport), cmp.horizontalGap(150)),cmp.line());
				if (getStationDto().getTotalTriggersConfigured() > 0)
				{
					JasperReportBuilder measurementsReport = buildMeasurementsReport();
					multiPageListBuilder.add(cmp.subreport(measurementsReport));
//					stationConfigReport.title(cmp.verticalList(cmp.subreport(measurementsReport)));
//					stationConfigReport.addTitle(cmp.pageBreak());
					logger.debug("Measurement report complete");
				}
				
			}
			if (getStationDto().getSystemDigitalChannelsCount() > 0)
			{
				JasperReportBuilder eventsReport = buildEventsReport();
				multiPageListBuilder.add(cmp.subreport(eventsReport));
//				stationConfigReport.title(cmp.verticalList(cmp.subreport(eventsReport)));
				logger.debug("Events report complete");
			}
//			  .setDataSource(getDataSource());
			// COde to print directly
//			final JRPrintServiceExporter exporter = new JRPrintServiceExporter();
//			exporter.setParameter(JRExporterParameter.JASPER_PRINT, report.toJasperPrint());
//			exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
//			exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.TRUE);
//			exporter.exportReport();
//			report.setPageFormat(PageType.LETTER, PageOrientation.PORTRAIT);


//			JasperViewer jasperViewer = new JasperViewer(stationConfigReport.toJasperPrint(), false, Locale.ENGLISH);
//			jasperViewer.setTitle("Station Configuration Print Preview");
//			jasperViewer.setZoomRatio(new Float(0.8949)); //the frame fit ratio
//			jasperViewer.setVisible(true);
//			stationConfigReport.print(true);
//			JasperPdfExporterBuilder pdfExporter = DynamicReports.export.pdfExporter("/data/m9k/stationConfigReport");
//			stationConfigReport.toPdf(pdfExporter);
			stationConfigReport.summary(multiPageListBuilder);
			stationConfigReport.summaryWithPageHeaderAndFooter();
		} catch (DRException e) {
			e.printStackTrace();
			stationConfigReport = null;
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			stationConfigReport = null;
			throw e;
		}

		return stationConfigReport;
 }


/**
 * @param report
 * @throws Exception 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
private JasperReportBuilder buildAnalogReport() throws Exception {
	JasperReportBuilder analogReport = report();
		for (Column column : createAnalogsColumnData()) {
	if (column.getColumnLength() > 0)
	{
//		System.out.println("Column "+column.getTitle());
		analogReport.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) 
				.setWidth(column.getColumnLength())
				.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER));
	}
	else
	{
		analogReport.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) .setWidth(40));
	}
}
	
	analogReport
	 .setColumnTitleStyle(columnTitleStyle)

    .highlightDetailEvenRows()
     .title( cmp.horizontalList().
        				add((cmp.text("  Analog Channels").setStyle(subTitleStyle))).setFixedWidth(250)
        				.add(cmp.filler().setHeight(25)))
	.setDataSource(createAnalogDataSource());
//	JasperViewer jasperViewer = new JasperViewer(analogReport.toJasperPrint(), false, Locale.ENGLISH);
//			jasperViewer.setTitle("Print Preview");
//			jasperViewer.setZoomRatio(new Float(0.8949)); //the frame fit ratio
//			jasperViewer.setVisible(true);
	return analogReport;
}

@SuppressWarnings({ "unchecked", "rawtypes" })
private JasperReportBuilder buildMeasurementsReport() throws Exception {
	JasperReportBuilder measurementsReport = report();
//	measurementsReport.setPageFormat(PageType.A4, PageOrientation.LANDSCAPE);
		for (Column column : createMeasurementsColumnData()) {
			if (column.getColumnLength() > 0)
			{
				measurementsReport.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) 
						.setWidth(column.getColumnLength())
						.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER));
			}
			else
			{
				measurementsReport.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) .setWidth(40));
			}
		}
	
	measurementsReport
	 .setColumnTitleStyle(columnTitleStyle)
	 
	   .title( cmp.horizontalList().
        				add((cmp.text("  Measurements/Trigger Channels").setStyle(subTitleStyle))).setFixedWidth(250)
        				.add(cmp.filler().setHeight(25)))
    .highlightDetailEvenRows()
	.setDataSource(createMeasurementsDataSource());
	return measurementsReport;
}


private JasperReportBuilder buildLineGroupsReport() throws Exception {
	JasperReportBuilder lineGroupsReport = report();
	lineGroupsReport.title(cmp.verticalList().add(cmp.verticalGap(20),cmp.text("Line Groups").setStyle(subTitleStyle)));
	List<LineGroupsAlgorithm> lstLineGroups = m9kConfig.getListOfAllLinegroupsForReport();
	LineGroupsAlgorithm lineGroupsAlgorithm;
	List<AnalogChannelDTO> listAnalogInputs;
	AnalogChannelDTO analogChannelDTO;
	String inputChannelType;
	for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator.hasNext();) {
		 lineGroupsAlgorithm =  iterator
				.next();
		listAnalogInputs = lineGroupsAlgorithm.getInputChannels();
		VerticalListBuilder vListAnalogChannels = cmp.verticalList()
				.add(cmp.horizontalList(
					cmp.text("  Channels ").setStyle(boldLtAlignedStyle), 
					cmp.text(" IN# ").setStyle(boldLtAlignedStyle),
					cmp.text(" Description ").setStyle(boldLtAlignedStyle)
				).setFixedWidth(300));
		for (Iterator<AnalogChannelDTO> iterator2 = listAnalogInputs.iterator(); iterator2
				.hasNext();) {
			analogChannelDTO = iterator2
					.next();
			if (analogChannelDTO.getInputType().startsWith(
					M9kConstants.VOLTAGE_AC))
			{
				inputChannelType = "V"+analogChannelDTO.getPhase().toLowerCase();
			}
			else
			{
				inputChannelType = "I"+analogChannelDTO.getPhase().toLowerCase();
			}
			vListAnalogChannels
					.add(
							cmp.horizontalList(cmp.text(inputChannelType).setStyle(boldLtAlignedStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER), cmp.text(analogChannelDTO.getDisplayChannel()),cmp.text(analogChannelDTO.getCircuitName())).setFixedWidth(300)
							  )
							  
							;
		}
//		lineGroupsReport.title(cmp.verticalList().add(cmp.verticalGap(25)));	
	lineGroupsReport.title(
			cmp.filler().setHeight(20),
   		 cmp.multiPageList()
   		 .add(cmp.verticalGap(20),cmp.horizontalList(cmp.text("  Line Group Name: ").setStyle(boldLtAlignedStyle) .setFixedWidth(100), cmp.text(lineGroupsAlgorithm.getLineGroupName()).setStyle(stl.style())
   				 																																					.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)))
   		 .add(cmp.verticalList(cmp.horizontalList().add(cmp.verticalList()
   				 .add(
   					  cmp.horizontalList().add(cmp.horizontalList().add(cmp.text("  Auto Cal. Fault Loc.: ").setStyle(boldLtAlignedStyle), cmp.text(lineGroupsAlgorithm.getEnableAutoCalc()))),
        				cmp.verticalGap(5),
 					  cmp.horizontalList().add(cmp.text("  Positive Resistance: ").setStyle(boldLtAlignedStyle), cmp.text(lineGroupsAlgorithm.getPositiveResistance()+" Ohm")),
    				cmp.verticalGap(5),
    				cmp.horizontalList().add(cmp.text("  Positive Reactance: ").setStyle(boldLtAlignedStyle), cmp.text(lineGroupsAlgorithm.getPositiveReactance()+" Ohm")),
  				    cmp.verticalGap(5),
  				    cmp.horizontalList().add(cmp.text("  Zero Resistance: ").setStyle(boldLtAlignedStyle), cmp.text(lineGroupsAlgorithm.getZeroResistance()+" Ohm")),
  				    cmp.verticalGap(5),
  				    cmp.horizontalList().add(cmp.text("  Zero Reactance: ").setStyle(boldLtAlignedStyle), cmp.text(lineGroupsAlgorithm.getZeroReactance()+" Ohm")),
  				    cmp.verticalGap(5),
  				    cmp.horizontalList().add(cmp.text("  Line Length: ").setStyle(boldLtAlignedStyle), cmp.text(lineGroupsAlgorithm.getLineMiles()+" Miles")),
				    cmp.verticalGap(5),
  				    cmp.horizontalList().add(cmp.text("  Comments: ").setStyle(boldLtAlignedStyle), cmp.text(lineGroupsAlgorithm.getComments())),
				    cmp.verticalGap(5)
    				)
    				,cmp.verticalList()
    				.add(cmp.verticalGap(5),
    						vListAnalogChannels.setStyle(innerBorderStyle).setFixedWidth(300)
          				,cmp.verticalGap(5)
    				)
    				)
    				,cmp.horizontalList().add(cmp.text("  Fault Line Decision Logic: ").setStyle(boldLtAlignedStyle).setFixedWidth(150), cmp.text(lineGroupsAlgorithm.getDecisionLogic())),
				    cmp.verticalGap(5)
    				)
   				 .setStyle(borderStyle)
   				 )
   		);
   		 
	}
	
//	JasperViewer jasperViewer = new JasperViewer(lineGroupsReport.toJasperPrint(), false, Locale.ENGLISH);
//	jasperViewer.setTitle("Print Preview");
//	jasperViewer.setZoomRatio(new Float(0.8949)); //the frame fit ratio
//	jasperViewer.setVisible(true);

	return lineGroupsReport;
}


@SuppressWarnings({ "unchecked", "rawtypes" })
private JasperReportBuilder buildEventsReport() throws Exception {
	JasperReportBuilder eventsReport = report();
//	eventsReport.setPageFormat(PageType.A4, PageOrientation.PORTRAIT);

		for (Column column : createEventsColumnData()) {
	if (column.getColumnLength() > 0)
	{
//		System.out.println("Column "+column.getTitle());
		eventsReport.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) 
				.setWidth(column.getColumnLength())
				.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER));
	}
	else
	{
		eventsReport.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) .setWidth(40));
	}
}
	
	eventsReport
	 .setColumnTitleStyle(columnTitleStyle)
    .highlightDetailEvenRows()
     .title( cmp.horizontalList().
        				add((cmp.text("  Event Channels").setStyle(subTitleStyle))).setFixedWidth(250)
        				.add(cmp.filler().setHeight(25)))
	.setDataSource(createEventDataSource());
//	JasperViewer jasperViewer = new JasperViewer(analogReport.toJasperPrint(), false, Locale.ENGLISH);
//			jasperViewer.setTitle("Print Preview");
//			jasperViewer.setZoomRatio(new Float(0.8949)); //the frame fit ratio
//			jasperViewer.setVisible(true);
	return eventsReport;
}

@SuppressWarnings({ "unchecked", "rawtypes" })
private JasperReportBuilder buildVirtualChannelsReport() throws Exception {
	JasperReportBuilder virtualChannelsReport = report();
		for (Column column : createVirtualChannelsColumnData()) {
	if (column.getColumnLength() > 0)
	{
//		System.out.println("Column "+column.getTitle());
		virtualChannelsReport.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) 
				.setWidth(column.getColumnLength())
				.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER));
	}
	else
	{
		virtualChannelsReport.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) .setWidth(40));
	}
}
	
	virtualChannelsReport
	 .setColumnTitleStyle(columnTitleStyle)

    .highlightDetailEvenRows()
     .title( cmp.horizontalList().
        				add((cmp.text("  Virtual Channels").setStyle(subTitleStyle))).setFixedWidth(250)
        				.add(cmp.filler().setHeight(25)))
	.setDataSource(createVirtualChannelDataSource());
//	JasperViewer jasperViewer = new JasperViewer(analogReport.toJasperPrint(), false, Locale.ENGLISH);
//			jasperViewer.setTitle("Print Preview");
//			jasperViewer.setZoomRatio(new Float(0.8949)); //the frame fit ratio
//			jasperViewer.setVisible(true);
	return virtualChannelsReport;
}

/**
 * START: 03-Feb-2020 - Transducer implementation
 * */
@SuppressWarnings({ "unchecked", "rawtypes" })
private JasperReportBuilder buildTranducerChannelsReport() throws Exception {
	JasperReportBuilder transducerChannelsReport = report();
		for (Column column : createTransducerChannelsColumnData()) {
	if (column.getColumnLength() > 0)
	{
//		System.out.println("Column "+column.getTitle());
		transducerChannelsReport.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) 
				.setWidth(column.getColumnLength())
				.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER));
	}
	else
	{
		transducerChannelsReport.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) .setWidth(40));
	}
}
	
	transducerChannelsReport
	 .setColumnTitleStyle(columnTitleStyle)

    .highlightDetailEvenRows()
     .title( cmp.horizontalList().
        				add((cmp.text("  Transducer Properties ").setStyle(subTitleStyle))).setFixedWidth(250)
        				.add(cmp.filler().setHeight(25)))
	.setDataSource(createTransducerChannelDataSource());
//	JasperViewer jasperViewer = new JasperViewer(analogReport.toJasperPrint(), false, Locale.ENGLISH);
//			jasperViewer.setTitle("Print Preview");
//			jasperViewer.setZoomRatio(new Float(0.8949)); //the frame fit ratio
//			jasperViewer.setVisible(true);
	return transducerChannelsReport;
}


/**
 * @return the dataSource
 */
public JRDataSource getDataSource() {
	return dataSource;
}


/**
 * @param dataSource the dataSource to set
 */
public void setDataSource(JRDataSource dataSource) {
	this.dataSource = dataSource;
}


/**
 * @return the reportTitle
 */
public String getReportTitle() {
	return reportTitle;
}


/**
 * @param reportTitle the reportTitle to set
 */
public void setReportTitle(String reportTitle) {
	this.reportTitle = reportTitle;
}


/**
 * @return the columnsList
 */
public List<Column> getColumnsList() {
	return columnsList;
}


/**
 * @param columnsList the columnsList to set
 */
public void setColumnsList(List<Column> columnsList) {
	this.columnsList = columnsList;
}

/**
 * @return the stationDto
 */
public StationDTO getStationDto() {

	if (stationDto == null)
	{
		stationDto = M9kUtils.getStationDetails();
	}

	return stationDto;
}

/**
 * @param stationDto the stationDto to set
 */
public void setStationDto(StationDTO stationDto) {
	this.stationDto = stationDto;
}

private List<Column> createAnalogsColumnData() {
	List<Column> columns = new ArrayList<Column>();
	columns.add(new Column("IN#","IN#","string",50));
	columns.add(new Column("Analog Channel Description",    "Analog Channel Description",  "String",150));
	// START: 06-Sept-2024 - Add Phase to Analog Channel Description page 
	columns.add(new Column("Phase",  "Phase", "string", 50));
	columns.add(new Column("Type",  "Type", "string", 100));
	columns.add(new Column("Primary",  "Primary", "string",50));
	columns.add(new Column("Secondary",  "Secondary", "string",60));
	columns.add(new Column("Full Scale",  "Full Scale", "string",50));
	columns.add(new Column("External Shunt",  "External Shunt", "string",50));
	columns.add(new Column("Exported",  "Exported", "string",50));
	return columns;

	
}

private List<Column> createEventsColumnData() {
	List<Column> columns = new ArrayList<Column>();
	columns.add(new Column("Event#","Event#","string",50));
	columns.add(new Column("Event Channel Description",    "Event Channel Description",  "String",150));
	columns.add(new Column("Normal",  "Normal", "string", 50));
	columns.add(new Column("DFR",  "DFR", "string",50));
	columns.add(new Column("DFR Trigger",  "DFR Trigger", "string",60));
	columns.add(new Column("SER",  "SER", "string",50));
	if (getStationDto().isPmuEnabled())
	{
		columns.add(new Column("PMU",  "PMU", "string",50));
	}
	return columns;

	
}
private List<Column> createMeasurementsColumnData() {
	List<Column> columns = new ArrayList<Column>();
	columns.add(new Column("Enabled","Enabled","string",50));
	columns.add(new Column("TR#","TR#","string",50));
	columns.add(new Column("Trigger Channel Description", "Trigger Channel Description",  "String",150));
	columns.add(new Column("Type",  "Type", "string", 70));
	columns.add(new Column("Average",  "Average", "string",50));
	columns.add(new Column("Harmonic",  "Harmonic", "string",60));
	if (getStationDto().isPmuEnabled())
	{
//		System.out.println("Adding 2 columns for PMU");
		columns.add(new Column("PMU",  "PMU", "string",50));
		columns.add(new Column("Freq",  "Freq", "string",50));
	}
	columns.add(new Column("Trigger",  "Trigger", "string",50));
	columns.add(new Column("Trigger If Over",  "Trigger If Over", "string",50));
	columns.add(new Column("Trigger If Under",  "Trigger If Under", "string",50));
	columns.add(new Column("Chatter Limit",  "Chatter Limit", "string",50));
	columns.add(new Column("Chatter Rate",  "Chatter Rate", "string",50));
	columns.add(new Column("Trigger Limit (ms)",  "Trigger Limit (ms)", "string",50));
	// START: 26-Feb-2020 - ROC positive and Negative Limits implementation
//	columns.add(new Column("Trigger If > x/s",  "Trigger If > x/s", "string",50));
//	columns.add(new Column("Duration (cycles)",  "Duration (cycles)", "string",50));
	columns.add(new Column("ROC Duration (cycles)",  "Duration (cycles)", "string",50));
	columns.add(new Column("ROC -",  "ROCNeg", "string",50));
	columns.add(new Column("ROC +",  "ROCPos", "string",50));
	// END: 26-Feb-2020
	columns.add(new Column("Exported",  "Exported", "string",50));
	columns.add(new Column("DDR",  "DDR", "string",50));
	columns.add(new Column("Disturbance Alarm",  "Disturbance Alarm", "string",50));
	return columns;

	
}

private List<Column> createVirtualChannelsColumnData() {
	List<Column> columns = new ArrayList<Column>();
	columns.add(new Column("IN#","IN#","string",50));
	columns.add(new Column("Virtual Channel Description",    "Virtual Channel Description",  "String",150));
	columns.add(new Column("Virtual Logic",  "Virtual Logic", "string", 100));
	columns.add(new Column("Phase",  "Phase", "string",50));
	columns.add(new Column("Input Type",  "Input Type", "string",100));
	columns.add(new Column("Exported",  "Exported", "string",50));
	return columns;

	
}
private DRDataSource  createAnalogDataSource()
{
	AnalogChannelDTO analogChannelDTO = null;
	DRDataSource dataSource = new DRDataSource("IN#", "Analog Channel Description", "Phase", "Type", "Primary","Secondary","Full Scale","External Shunt","Exported");
	List<AnalogChannelDTO> lstAnalogChannels  = m9kConfig.getLstAnalogChannels();
	String externalShunt = M9kConstants.NOT_APPLICABLE;
	String isExported = M9kConstants.NO;
	String analogInputType = null;
	Map<String,String> mapAnalogInutTypeForReport = M9kUtils.getMapAnalogChannelInputType();
//	for (Iterator<AnalogChannelDTO> iterator = lstAnalogChannels.iterator(); iterator.hasNext();) {
	logger.debug("PDF-EXPORT-ISSUE: Inside createAnalogDataSource() "+lstAnalogChannels.size());
	for (int i = 0; i < lstAnalogChannels.size(); i++) {
		analogChannelDTO = lstAnalogChannels.get(i);
		if (analogChannelDTO.isVirtual())
		{
			continue;
		}
		// START: 03-Feb-2020 - Transducer implementation
		if (analogChannelDTO.isTransducer())
		{
			transducerEnabled = true;
		}
		// END: 03-Feb-2020
		logger.debug("PDF-EXPORT-ISSUE: "+analogChannelDTO.getInputType()+" i--> "+i+" analogChannelDTO--> "+analogChannelDTO);
		if (analogChannelDTO.getInputType().toLowerCase().contains("external"))
		{
			externalShunt = analogChannelDTO.getExtShunt();
		}
		else
		{
			externalShunt = M9kConstants.NOT_APPLICABLE;
		}
		if (analogChannelDTO.isExportStatus())
		{
			isExported = M9kConstants.YES;
		}
		else
		{
			isExported = M9kConstants.NO;
		}
		analogInputType = analogChannelDTO.getInputType(); 
		if (mapAnalogInutTypeForReport != null && mapAnalogInutTypeForReport.containsKey(analogInputType))
		{
			analogInputType = mapAnalogInutTypeForReport.get(analogInputType);
			if (analogInputType == null || analogInputType.trim().isEmpty())
			{
				analogInputType = analogChannelDTO.getInputType(); 
			}
		}
//		System.out.println("channel "+analogChannelDTO.getDisplayChannel()+" analog desc "+analogChannelDTO.getCircuitName()+"Input Type "+analogChannelDTO.getInputType()+ 
//				"  Primary "+analogChannelDTO.getPrimaryRatio()+" Secondary "+analogChannelDTO.getSecondaryRatio()+"Full scale "+analogChannelDTO.getRange()+" external Shunt "+externalShunt);
		dataSource.add(analogChannelDTO.getDisplayChannel(), analogChannelDTO.getCircuitName(), analogChannelDTO.getPhase(), analogInputType, analogChannelDTO.getPrimaryRatio(), analogChannelDTO.getSecondaryRatio(), analogChannelDTO.getRange(), externalShunt,isExported);
	}
	return dataSource;
}

private DRDataSource  createEventDataSource()
{
	EventChannelDTO eventChannelDTO = null;
	DRDataSource dataSource;
	
	if (getStationDto().isPmuEnabled())
	{
		dataSource = new DRDataSource("Event#", "Event Channel Description", "Normal", "DFR","DFR Trigger","SER","PMU");
	}
	else
	{
		dataSource = new DRDataSource("Event#", "Event Channel Description", "Normal", "DFR","DFR Trigger","SER");
	}
	List<EventChannelDTO> lstEventChannels  = m9kConfig.getLstEventChannels();
	String normalState = M9kConstants.OPEN_STATE; // Default open
	String dfrStatus = M9kConstants.DISABLED;
	String serStatus = M9kConstants.DISABLED;
	String pmuStatus = M9kConstants.DISABLED;
	for (Iterator<EventChannelDTO> iterator = lstEventChannels.iterator(); iterator.hasNext();) {
		eventChannelDTO = iterator.next();
		if (eventChannelDTO.getNormalState() != null && eventChannelDTO.getNormalState().equalsIgnoreCase("1"))
		{
			normalState = M9kConstants.CLOSE_STATE;
		}
		else
		{
			normalState = M9kConstants.OPEN_STATE;
		}
		if (eventChannelDTO.isDfr())
		{
			dfrStatus = M9kConstants.ENABLED;
		}
		else
		{
			dfrStatus = M9kConstants.DISABLED;
		}
		if (eventChannelDTO.isSer())
		{
			serStatus = M9kConstants.ENABLED;
		}
		else
		{
			serStatus = M9kConstants.DISABLED;
		}
		if (getStationDto().isPmuEnabled())
		{
			
			if (eventChannelDTO.isPmu())
			{
				pmuStatus = M9kConstants.ENABLED;
			}
			else
			{
				pmuStatus = M9kConstants.DISABLED;
			}
			dataSource.add(eventChannelDTO.getDisplayChannel(), eventChannelDTO.getDescription(), normalState, dfrStatus, eventChannelDTO.getDfrStart(), serStatus, pmuStatus);
		}
		else
		{
			dataSource.add(eventChannelDTO.getDisplayChannel(), eventChannelDTO.getDescription(), normalState, dfrStatus, eventChannelDTO.getDfrStart(), serStatus);
		}
	}
	return dataSource;
}


private DRDataSource  createMeasurementsDataSource()
{
	TriggerChannelDTO triggerChannelDTO = null;
	String harmonic;
	String triggerStart;
	String tripIfOver;
	String tripIfUnder;
	String chatterLimit;
	String chatterRate;
	String triggerLimit;
	// START: 26-Feb-2020 - ROC positive and Negative Limits implementation
//	String tripIfGt;
	String tripRocPos="";
	String tripRocNeg="";
	// END: 26-Feb-2020
	String duration;
	String exportStatus = M9kConstants.YES;
	String average = "1";
	String enableStatus; // Enabled or Disabled
	String ddrStatus = M9kConstants.NO;
	// START: 30-Dec-2022 Disturbance Alarm implementation
	String disturbanceAlarm = M9kConstants.NO;
	// END: 30-Dec-2022
	DRDataSource dataSource;
	List<TriggerChannelDTO> lstTriggerChannels  = m9kConfig.getLstOfMeasurements();
	// START: 26-Feb-2020 - ROC positive and Negative Limits implementation
	if (getStationDto().isPmuEnabled())
	{
//		dataSource = new DRDataSource("Enabled", "TR#", "Trigger Channel Description", "Type", "Average", "Harmonic","PMU", "Freq", "Trigger","Trigger If Over","Trigger If Under","Chatter Limit","Chatter Rate","Trigger Limit (ms)","Trigger If > x/s","Duration (cycles)","Exported","DDR");
		dataSource = new DRDataSource("Enabled", "TR#", "Trigger Channel Description", "Type", "Average", "Harmonic","PMU", "Freq", "Trigger","Trigger If Over","Trigger If Under","Chatter Limit","Chatter Rate","Trigger Limit (ms)","Duration (cycles)","ROCNeg","ROCPos","Exported","DDR","Disturbance Alarm");
	}
	else
	{
//		dataSource = new DRDataSource("Enabled", "TR#", "Trigger Channel Description", "Type", "Average", "Harmonic","Trigger","Trigger If Over","Trigger If Under","Chatter Limit","Chatter Rate","Trigger Limit (ms)","Trigger If > x/s","Duration (cycles)","Exported","DDR");
		dataSource = new DRDataSource("Enabled", "TR#", "Trigger Channel Description", "Type", "Average", "Harmonic","Trigger","Trigger If Over","Trigger If Under","Chatter Limit","Chatter Rate","Trigger Limit (ms)","Duration (cycles)","ROCNeg","ROCPos","Exported","DDR","Disturbance Alarm");
	}
	// END: 26-Feb-2020
	for (Iterator<TriggerChannelDTO> iterator = lstTriggerChannels.iterator(); iterator.hasNext();) {
		triggerChannelDTO = iterator.next();
		if (triggerChannelDTO.getExportStatus())
		{
			exportStatus = M9kConstants.YES;
		}
		else
		{
			exportStatus = M9kConstants.NO;
		}
		if (triggerChannelDTO.isDdrStatus())
		{
			ddrStatus = M9kConstants.YES;
		}
		else
		{
			ddrStatus = M9kConstants.NO;
		}
		
		// START: 30-Dec-2022 Disturbance Alarm implementation
		if (triggerChannelDTO.isDisturbanceAlarm())
		{
			disturbanceAlarm = M9kConstants.YES;
		}
		else
		{
			disturbanceAlarm = M9kConstants.NO;
		}
		// END: 30-Dec-2022
		if (triggerChannelDTO.getStatus())
		{
			enableStatus = M9kConstants.YES;
		}
		else
		{
			enableStatus = M9kConstants.NO;
		}
		harmonic = triggerChannelDTO.getHarmonic();
		if (harmonic == null || harmonic.isEmpty())
		{
			harmonic = M9kConstants.NOT_APPLICABLE;
		}
		triggerStart = triggerChannelDTO.getStart();
		if (triggerStart == null || triggerStart.isEmpty())
		{
			triggerStart = M9kConstants.NOT_APPLICABLE;
		}
		
		tripIfOver = triggerChannelDTO.getTripIfOver();
		if (tripIfOver == null || tripIfOver.isEmpty())
		{
			tripIfOver = M9kConstants.NOT_APPLICABLE;
		}

		tripIfUnder = triggerChannelDTO.getTripIfUnder();
		if (tripIfUnder == null || tripIfUnder.isEmpty())
		{
			tripIfUnder = M9kConstants.NOT_APPLICABLE;
		}
		
		chatterLimit = triggerChannelDTO.getChatterLimit();
		if (chatterLimit == null || chatterLimit.isEmpty())
		{
			chatterLimit = M9kConstants.NOT_APPLICABLE;
		}
		
		chatterRate = triggerChannelDTO.getChatterRate();
		if (chatterRate == null || chatterRate.isEmpty())
		{
			chatterRate = M9kConstants.NOT_APPLICABLE;
		}
		
		triggerLimit = triggerChannelDTO.getTriggerLimit();
		if (triggerLimit == null || triggerLimit.isEmpty())
		{
			triggerLimit = M9kConstants.NOT_APPLICABLE;
		}

//		tripIfGt = triggerChannelDTO.getTripIfGt();
//		if (tripIfGt == null || tripIfGt.isEmpty())
//		{
//			tripIfGt = M9kConstants.NOT_APPLICABLE;
//		}
		
		duration = triggerChannelDTO.getDuration();
		if (duration == null || duration.isEmpty() || duration.equalsIgnoreCase("0"))
		{
			duration = M9kConstants.NOT_APPLICABLE;
			tripRocNeg = M9kConstants.NOT_APPLICABLE;
			tripRocPos = M9kConstants.NOT_APPLICABLE;
		}
		else
		{
			tripRocNeg = triggerChannelDTO.getTripRocNeg();
			tripRocPos = triggerChannelDTO.getTripRocPos();
		}
		
		if (triggerChannelDTO.getAverage() == null )
		{
			average = "";
		}
		else
		{
			average = ""+triggerChannelDTO.getAverage();
		}
		if (getStationDto().isPmuEnabled())
		{
			String pmuStatus;
			String freqPmuStatus;
			if (triggerChannelDTO.isPmuStatus())
			{
				pmuStatus = M9kConstants.YES;
			}
			else
			{
				pmuStatus = M9kConstants.NO;
			}

			if (triggerChannelDTO.isFreqPmuStatus())
			{
				freqPmuStatus = M9kConstants.YES;
			}
			else
			{
				freqPmuStatus = M9kConstants.NO;
			}

			// START: 26-Feb-2020 - ROC positive and Negative Limits implementation
//			dataSource.add(enableStatus, "T"+triggerChannelDTO.getId(), triggerChannelDTO.getName(), triggerChannelDTO.getType(),average, harmonic, pmuStatus, freqPmuStatus, triggerStart, tripIfOver, 
//				tripIfUnder, chatterLimit, chatterRate,triggerLimit, tripIfGt, duration, exportStatus, ddrStatus);
			dataSource.add(enableStatus, "T"+triggerChannelDTO.getId(), triggerChannelDTO.getName(), triggerChannelDTO.getType(),average, harmonic, pmuStatus, freqPmuStatus, triggerStart, tripIfOver, 
					tripIfUnder, chatterLimit, chatterRate,triggerLimit, duration, tripRocNeg,tripRocPos, exportStatus, ddrStatus,disturbanceAlarm);
			// END: 26-Feb-2020
		}
		else
		{
			// START: 26-Feb-2020 - ROC positive and Negative Limits implementation
//			dataSource.add(enableStatus, "T"+triggerChannelDTO.getId(), triggerChannelDTO.getName(), triggerChannelDTO.getType(),average, harmonic, triggerStart, tripIfOver, 
//					tripIfUnder, chatterLimit, chatterRate,triggerLimit, tripIfGt, duration, exportStatus, ddrStatus);
			dataSource.add(enableStatus, "T"+triggerChannelDTO.getId(), triggerChannelDTO.getName(), triggerChannelDTO.getType(),average, harmonic, triggerStart, tripIfOver, 
					tripIfUnder, chatterLimit, chatterRate,triggerLimit, duration, tripRocNeg,tripRocPos, exportStatus, ddrStatus,disturbanceAlarm);			
			// END: 26-Feb-2020
		}
	}
	return dataSource;
}

private DRDataSource  createVirtualChannelDataSource()
{
	AnalogChannelDTO analogChannelDTO = null;
	DRDataSource dataSource = new DRDataSource("IN#", "Virtual Channel Description", "Virtual Logic", "Phase", "Input Type", "Exported");
	List<AnalogChannelDTO> lstVirtualChannels  = m9kConfig.getLstVirtualChannels();
	String isExported = M9kConstants.NO;
	String analogInputType = null;
	Map<String,String> mapAnalogInutTypeForReport = M9kUtils.getMapAnalogChannelInputType();
	for (Iterator<AnalogChannelDTO> iterator = lstVirtualChannels.iterator(); iterator.hasNext();) {
		analogChannelDTO = iterator.next();
		
		if (analogChannelDTO.isExportStatus())
		{
			isExported = M9kConstants.YES;
		}
		else
		{
			isExported = M9kConstants.NO;
		}
		analogInputType = analogChannelDTO.getInputType(); 
		if (mapAnalogInutTypeForReport != null && mapAnalogInutTypeForReport.containsKey(analogInputType))
		{
			analogInputType = mapAnalogInutTypeForReport.get(analogInputType);
			if (analogInputType == null || analogInputType.trim().isEmpty())
			{
				analogInputType = analogChannelDTO.getInputType(); 
			}
		}
//		System.out.println("channel "+analogChannelDTO.getDisplayChannel()+" analog desc "+analogChannelDTO.getCircuitName()+"Input Type "+analogChannelDTO.getInputType()+ 
//				"  Primary "+analogChannelDTO.getPrimaryRatio()+" Secondary "+analogChannelDTO.getSecondaryRatio()+"Full scale "+analogChannelDTO.getRange()+" external Shunt "+externalShunt);
		dataSource.add(analogChannelDTO.getDisplayChannel(), analogChannelDTO.getCircuitName(), analogChannelDTO.getVirtualLogic(), analogChannelDTO.getPhase(), analogInputType, isExported);
	}
	return dataSource;
}

/**
 * START: 03-Feb-2020 - Transducer implementation
 * @return
 */

private List<Column> createTransducerChannelsColumnData() {
	List<Column> columns = new ArrayList<Column>();
	columns.add(new Column("IN#","IN#","string",50));
	columns.add(new Column("Analog Channel Description","Analog Channel Description","string",150));
	columns.add(new Column("Input   (Low)","Input Low","string",60));
	columns.add(new Column("Input  (High)","Input High","string",60));
	columns.add(new Column("Output (Low)","Output Low","string",60));
	columns.add(new Column("Output (High)","Output High","string",60));
	columns.add(new Column("Units","Units","string", 150));
	return columns;

	
}

/**
 * START: 03-Feb-2020 - Transducer implementation
 * @return
 */
private DRDataSource  createTransducerChannelDataSource()
{
	AnalogChannelDTO analogChannelDTO = null;
	DRDataSource dataSource = new DRDataSource("IN#", "Analog Channel Description","Input Low", "Input High", "Output Low", "Output High", "Units");
	List<AnalogChannelDTO> lstAnalogChannels  = m9kConfig.getLstAnalogChannels();
	String inputUnits;
	for (Iterator<AnalogChannelDTO> iterator = lstAnalogChannels.iterator(); iterator.hasNext();) {
		analogChannelDTO = iterator.next();
		
		if (!analogChannelDTO.isTransducer())
		{
			continue;
		}
		if (analogChannelDTO.getInputType().toLowerCase().indexOf("current") != -1)
		{
			inputUnits = " A";
		}
		else
		{
			inputUnits = " V";
		}
			
		logger.debug("InP1 and Inp2 from analogDTO InP1-> "+analogChannelDTO.getInP1()+" InP2 -> "+analogChannelDTO.getInP2()+" OutP1-> "+analogChannelDTO.getOutP1()+" outP2 -> "+analogChannelDTO.getOutP2());
		dataSource.add(analogChannelDTO.getDisplayChannel(), analogChannelDTO.getCircuitName(), ""+analogChannelDTO.getOutP1(), ""+analogChannelDTO.getOutP2(), analogChannelDTO.getInP1()+inputUnits, analogChannelDTO.getInP2()+inputUnits, analogChannelDTO.getTransducerUnits());
	}
	return dataSource;
}
// END 03-Feb-2020
public static void main(String[] args)
{
	M9kUtils.setAppletWebHost("localhost");
	StationDTO stationDTO = M9kUtils.getDebugStationDetails(99201);
	M9kComtradeDetailsReportPDF m9kComtradeDetailsReportPDF = new M9kComtradeDetailsReportPDF(stationDTO, "Station Configuration report");
//	stationDTO.setSystemStationName("TEst Station");
//	stationDTO.setSystemStationId(1);
//	stationDTO.setSystemAnalogChannelsCount(8);
//	stationDTO.setSystemDigitalChannelsCount(32);
//	stationDTO.setTotalDfrsConfigured(1);
//	System.out.println(stationDTO);
	stationDTO.setTotalTriggersConfigured(7);
	m9kComtradeDetailsReportPDF.setStationDto(stationDTO);
	try {
//		JasperReportBuilder stationConfigReport = m9kComtradeDetailsReportPDF.buildPDF();
//		if (stationConfigReport != null)
//    	{
//	    	File testFile = new File("C:/data/m9k/Cross-config.pdf");
//	    	DataOutputStream baos = new DataOutputStream(new FileOutputStream(testFile));
//	    	stationConfigReport.toPdf(baos);
//    	}
//    	else
//        {
//        	logger.error("PDF turned out to be null. ");
//			throw new M9000Exception("No Report generated");
//        }
		
		JasperReportBuilder stationConfigReport = m9kComtradeDetailsReportPDF.buildAnalogsWitTransducersReport();
		if (stationConfigReport != null)
    	{
	    	File testFile = new File("C:/data/m9k/Transducer-config.pdf");
			System.out.println(" Station config report "+stationConfigReport);
//			JasperViewer jasperViewer = new JasperViewer(analogsChannelsReport.toJasperPrint(), false, Locale.ENGLISH);
//			jasperViewer.setTitle("Print Preview");
//			jasperViewer.setZoomRatio(new Float(0.8949)); //the frame fit ratio
//			jasperViewer.setVisible(true);

			DataOutputStream baos = new DataOutputStream(new FileOutputStream(testFile));
	    	stationConfigReport.toPdf(baos);
			System.out.println(" After pdf  ");
			baos.close();
    	}
    	else
        {
        	System.out.println("PDF turned out to be null. ");
			throw new M9000Exception("No Report generated");
        }

	} catch (Exception e) {
		e.printStackTrace();
	}
	System.exit(0);
}

private JasperReportBuilder buildAnalogsWitTransducersReport() throws Exception {
	JasperReportBuilder analogsChannelsReport = report();
	
	AnalogChannelDTO analogChannelDTO;
	 stationDto = M9kUtils.getDebugStationDetails(99201);
	 m9kConfig = new M9000XmlConfig();
	 m9kConfig.loadStationXmlFile(new StringReader(stationDto.getConfigXml()));
//	 stationDto = m9kConfig.getStationDto();
	List<AnalogChannelDTO> lstAnalogChannels  = m9kConfig.getLstAnalogChannels();
	String externalShunt = M9kConstants.NOT_APPLICABLE;
	String isExported = M9kConstants.NO;
	String analogInputType = null;
	Map<String,String> mapAnalogInutTypeForReport = M9kUtils.getMapAnalogChannelInputType();
	System.out.println("Analog list size "+lstAnalogChannels.size());
	MultiPageListBuilder vListAnalogChannels;
	vListAnalogChannels = cmp.multiPageList();
	vListAnalogChannels.setSplitType(SplitType.PREVENT);

	vListAnalogChannels.add(cmp.verticalList()
			.add(cmp.horizontalList(
				cmp.text("  IN# ").setStyle(boldLtAlignedStyle), 
				cmp.text(" Analog Channel Description ").setStyle(boldLtAlignedStyle).setFixedWidth(200),
				cmp.text(" Type ").setStyle(boldLtAlignedStyle), 
				cmp.text(" Primary ").setStyle(boldLtAlignedStyle),
				cmp.text(" Secondary ").setStyle(boldLtAlignedStyle), 
				cmp.text(" Full Scale ").setStyle(boldLtAlignedStyle),
				cmp.text(" External Shunt ").setStyle(boldLtAlignedStyle),
				cmp.text(" Exported ").setStyle(boldLtAlignedStyle)
			).setFixedWidth(800)));
	for (int i = 0; i < lstAnalogChannels.size(); i++) {

		analogChannelDTO = lstAnalogChannels.get(i);
		if (analogChannelDTO.isVirtual())
		{
			continue;
		}
		if (analogChannelDTO.getInputType().toLowerCase().contains("external"))
		{
			externalShunt = analogChannelDTO.getExtShunt();
		}
		else
		{
			externalShunt = M9kConstants.NOT_APPLICABLE;
		}
		if (analogChannelDTO.isExportStatus())
		{
			isExported = M9kConstants.YES;
		}
		else
		{
			isExported = M9kConstants.NO;
		}
		analogInputType = analogChannelDTO.getInputType(); 
		if (mapAnalogInutTypeForReport != null && mapAnalogInutTypeForReport.containsKey(analogInputType))
		{
			analogInputType = mapAnalogInutTypeForReport.get(analogInputType);
			if (analogInputType == null || analogInputType.trim().isEmpty())
			{
				analogInputType = analogChannelDTO.getInputType(); 
			}
		}

			vListAnalogChannels
					.add(
							cmp.horizontalList(cmp.text(analogChannelDTO.getDisplayChannel()),cmp.text(analogChannelDTO.getCircuitName())).setFixedWidth(300),
							cmp.text(analogInputType).setStyle(boldLtAlignedStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER), 
							cmp.text(analogChannelDTO.getPrimaryRatio()).setStyle(boldLtAlignedStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER), 
							cmp.text(analogChannelDTO.getSecondaryRatio()).setStyle(boldLtAlignedStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER),
							cmp.text(analogChannelDTO.getRange()).setStyle(boldLtAlignedStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER), 
							cmp.text(externalShunt).setStyle(boldLtAlignedStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER), 
							cmp.text(isExported).setStyle(boldLtAlignedStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)							
							  )
							  
							;
			
			System.out.println(analogChannelDTO.getDisplayChannel()+" - " + analogChannelDTO.getCircuitName()+" - " + analogInputType+" - " + analogChannelDTO.getPrimaryRatio()+" - " + analogChannelDTO.getSecondaryRatio()+" - " + analogChannelDTO.getRange()+" - " + externalShunt+" - " +isExported);
			analogsChannelsReport.title(cmp.verticalList().add(cmp.verticalGap(20),cmp.text("Analog Channels").setStyle(subTitleStyle))
					.add(vListAnalogChannels));
   		 
	}
	
System.out.println("Returning from transducer analog report testing ");
	return analogsChannelsReport;
}

public boolean isTransducerEnabled() {
	return transducerEnabled;
}

public void setTransducerEnabled(boolean transducerEnabled) {
	this.transducerEnabled = transducerEnabled;
}
}

