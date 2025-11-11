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
import static net.sf.dynamicreports.report.builder.DynamicReports.margin;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTable;

import com.usi.m9000.dto.M9kSerDTO;
import com.usi.m9000.dto.StationDTO;
import com.usi.m9000.reports.util.Column;
import com.usi.m9000.util.M9kUtils;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;

public class M9kSERReportPDF {

  
JTable tblSample;
JRDataSource dataSource;
String reportTitle;
List<Column> columnsList;
private StationDTO stationDto;
private String stationId = "Unkown";
private String stationName = "Unknown";
private List<M9kSerDTO> lstSerDtos;
StyleBuilder boldStyle;
StyleBuilder boldCenteredStyle;
StyleBuilder columnTitleStyle;
StyleBuilder titleStyle;
StyleBuilder boldRtAlignedStyle ;
StyleBuilder boldLtAlignedStyle;
StyleBuilder borderStyle;

private int totalNoOfEvents;
static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kSERReportPDF.class);

 public M9kSERReportPDF() {
	 boldStyle         = stl.style().bold();
	 boldCenteredStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
			 			.setFontSize(10);
	 columnTitleStyle  = stl.style(boldCenteredStyle)
			 				.setBorder(stl.pen2Point())
			 				.setBackgroundColor(Color.LIGHT_GRAY);
	 titleStyle = stl.style(boldCenteredStyle)
             .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
             .setFontSize(13);
	 boldRtAlignedStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
				.setFontSize(10);
	 boldLtAlignedStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
				.setFontSize(10);
	 borderStyle = stl.style().setBorder(stl.pen1Point().setLineWidth(2.0f));
	 
	 logger.debug("Getting station Details....");
	 stationDto = M9kUtils.getStationDetails();
	 if (stationDto != null)
	 {
		 stationId = stationDto.getSystemStationId().toString();
		 stationName = stationDto.getSystemStationName();
	 }
	 
 }

 public M9kSERReportPDF(String reportTitle, List<Column> columnsList, List<M9kSerDTO> lstSerDtos) {
	 this();
	 this.reportTitle =reportTitle;
	 this.columnsList = columnsList;
	 this.lstSerDtos = lstSerDtos;
	 totalNoOfEvents = lstSerDtos.size();
 }
 /**
  * Called only from SER listener to send out emails
  * @param reportTitle
  * @param columnsList
  * @param lstSerDtos
  */
 
 public M9kSERReportPDF(String reportTitle, List<Column> columnsList, List<M9kSerDTO> lstSerDtos, int stationId, String stationName) {
	 logger.debug("Inside SER email M9kSERReportPDF constructor..."+stationId+" name  "+stationName);
	 boldStyle         = stl.style().bold();
	 boldCenteredStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)
			 			.setFontSize(10);
	 columnTitleStyle  = stl.style(boldCenteredStyle)
			 				.setBorder(stl.pen2Point())
			 				.setBackgroundColor(Color.LIGHT_GRAY);
	 titleStyle = stl.style(boldCenteredStyle)
             .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE)
             .setFontSize(13);
	 boldRtAlignedStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)
				.setFontSize(10);
	 boldLtAlignedStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)
				.setFontSize(10);
	 borderStyle = stl.style().setBorder(stl.pen1Point().setLineWidth(2.0f));
	 this.reportTitle =reportTitle;
	 this.columnsList = columnsList;
	 this.lstSerDtos = lstSerDtos;
	 totalNoOfEvents = lstSerDtos.size();
	 this.stationId = ""+stationId;
	 this.stationName = stationName;
	 logger.debug("Inside M9kSERReportPDF constructor...");
 }

 
 @SuppressWarnings({ "unchecked", "rawtypes" })
public  JasperReportBuilder buildPDF() throws Exception
 {
	 logger.debug("Inside M9kSERReportPDF buildPDF...");
	 JasperReportBuilder report = null;
		try {
			report = report();
			for (Column column : getColumnsList()) {
				if (column.getColumnLength() > 0)
				{
//					System.out.println("Column "+column.getTitle());
					report.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) .setWidth(column.getColumnLength()));
				}
				else
				{
					report.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())) .setWidth(40));
				}
			}
			report
			 .setColumnTitleStyle(columnTitleStyle)
	         .highlightDetailEvenRows()
//			  .title(cmp.text(reportTitle).setStyle(titleStyle))
	                  .title(
        		 cmp.horizontalList()
        		 //TODO: Add company name when implemented
        		 .add(cmp.text("").setStyle(boldLtAlignedStyle), cmp.text(reportTitle).setStyle(titleStyle).setFixedWidth(250),cmp.text(new SimpleDateFormat("MM/dd/yyyy").format(new Date())).setStyle(boldRtAlignedStyle))//shows report title
        		 .newRow()
        		 .add(cmp.filler().setHeight(20))
        		 .newRow()
        		 .add(cmp.verticalList()
        				 .add(cmp.verticalGap(10),cmp.horizontalList(cmp.text("  Station Name: ").setStyle(boldLtAlignedStyle), cmp.text(stationName).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))).setFixedWidth(150),
        					  cmp.verticalGap(10),
        					  cmp.horizontalList().add(cmp.text("  Station Id: ").setStyle(boldLtAlignedStyle), cmp.text(stationId)).setFixedWidth(145),cmp.verticalGap(10))
        				 .setStyle(borderStyle)
        				 )
        		.newRow()
//        		.add(cmp.horizontalList().
//        				add((cmp.text("  Total No. Of Events:").setStyle(boldLtAlignedStyle)),cmp.text(totalNoOfEvents).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))).setFixedWidth(250))
        		.add(cmp.filler().setHeight(25))
        		 )

        		 .title( cmp.horizontalList().
        				add((cmp.text("  Total No. Of Events:").setStyle(boldLtAlignedStyle)),cmp.text(totalNoOfEvents).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))).setFixedWidth(250)
        				.add(cmp.filler().setHeight(25)))
			  .pageFooter(cmp.pageXofY().setStyle(boldCenteredStyle))
			  .setPageFormat(PageType.A4, PageOrientation.PORTRAIT)
//			  .setPageMargin(margin(20))
			  .setPageMargin(margin().setLeft(50).setRight(20).setTop(50).setBottom(50))

//			  .setDataSource(getDataSource());
			  .setDataSource(createSerDataSource());
			// COde to print directly
//			final JRPrintServiceExporter exporter = new JRPrintServiceExporter();
//			exporter.setParameter(JRExporterParameter.JASPER_PRINT, report.toJasperPrint());
//			exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
//			exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.TRUE);
//			exporter.exportReport();
//			report.setPageFormat(PageType.LETTER, PageOrientation.PORTRAIT);

//			JasperViewer jasperViewer = new JasperViewer(report.toJasperPrint(), false, Locale.ENGLISH);
//			jasperViewer.setTitle("Print Preview");
//			jasperViewer.setZoomRatio(new Float(0.8949)); //the frame fit ratio
//			jasperViewer.setVisible(true);
//			report.print(true);
//			JasperPdfExporterBuilder pdfExporter = DynamicReports.export.pdfExporter("c:/data/test-pdf.pdf");
//			report.toPdf(pdfExporter);
		} catch (DRException e) {
			logger.error("Exception occured while building pdf ",e);
			throw e;
		} catch (Exception e) {
			logger.error("Exception occured while building pdf ",e);
			throw e;
		}
		logger.debug("Returing from buildPDF ");
		return report;
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

private DRDataSource  createSerDataSource()
{
	DRDataSource dataSource;
	M9kSerDTO m9kSerDTO;
	dataSource = new DRDataSource("Date-Time", "Event", "Current", "Status","Sync","Description");
	for (Iterator<M9kSerDTO> iterator = getLstSerDtos().iterator(); iterator.hasNext();) {
		m9kSerDTO = iterator.next();
		dataSource.add(m9kSerDTO.getDisplayTime(), m9kSerDTO.getEventNum(), m9kSerDTO.getCurrentStateAsString(), m9kSerDTO.getStatus(),m9kSerDTO.getLockedAsString(),  m9kSerDTO.getName());
		logger.debug("Inside createSerDataSource "+m9kSerDTO);
	}
	return dataSource;
}

public List<M9kSerDTO> getLstSerDtos() {
	return lstSerDtos;
}

public void setLstSerDtos(List<M9kSerDTO> lstSerDtos) {
	this.lstSerDtos = lstSerDtos;
}


}
