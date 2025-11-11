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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import javax.swing.JTable;

import com.usi.m9000.common.email.SendMailUSI;
import com.usi.m9000.dto.M9kStatusReportDTO;
import com.usi.m9000.reports.util.Column;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.datatype.DRIDataType;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;

public class M9kStationSpecificDailyStatusReportPDF {

  
JTable tblSample;
StyleBuilder boldStyle;
StyleBuilder boldCenteredStyle;
StyleBuilder columnTitleStyle;
StyleBuilder titleStyle;
StyleBuilder boldRtAlignedStyle ;
StyleBuilder boldLtAlignedStyle;
StyleBuilder borderStyle;
private M9kStatusReportDTO m9kStatusReportDTO;
static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationSpecificDailyStatusReportPDF.class);
 public M9kStationSpecificDailyStatusReportPDF() {

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

 }

 public M9kStationSpecificDailyStatusReportPDF(M9kStatusReportDTO m9kStatusReportDTO) {
	 this();
	 logger.debug("Inside M9kStationSpecificDailyStatusReportPDF constructor"+m9kStatusReportDTO);
	 this.m9kStatusReportDTO =m9kStatusReportDTO;
 }

 public  void buildPDF() throws Exception
 {
	 ByteArrayOutputStream pdfBuffer = null;
	 java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
	 java.text.SimpleDateFormat reportDateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy");

		try {
			Date date = dateFormat.parse(m9kStatusReportDTO.getReportDate());
			String reportDateInReqFormat = reportDateFormat.format(date);
			SubreportBuilder subreport = cmp.subreport(new SubreportExpression())
					                                      .setDataSource(new SubreportDataSourceExpression());

			JasperReportBuilder report = report();
//			int iColCnt = 0;
//			for (Column column : m9kStatusReportDTO.getStationSpecificDetailsDTO().getWarningSummaryColumns()) {
//				if (iColCnt == 0)
//				{
//					report.addColumn(col.emptyColumn(false, false).setFixedWidth(50)); //empty column
//				}
//				if (column.getColumnLength() > 0)
//				{
////					System.out.println("Column "+column.getTitle());
//					report.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())));
//				}
//				else
//				{
//					report.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType) type.detectType(column.getDataType())));
//				}
//				iColCnt++;
//			}
//			report.addColumn(col.emptyColumn(false, false).setFixedWidth(50)); //empty column

			report
			 .setColumnTitleStyle(columnTitleStyle)
	         .highlightDetailEvenRows()
//			  .title(cmp.text(reportTitle).setStyle(titleStyle))
	                  .title(
        		 cmp.horizontalList()
        		 //TODO: Add company name when implemented
        		 .add(cmp.text(m9kStatusReportDTO.getReportTitle()).setStyle(titleStyle))//shows report title
        		 .newRow()
        		 .add(cmp.filler().setHeight(20))
        		 .newRow()
        		 .add(cmp.verticalList()
        				 .add(cmp.verticalGap(10),cmp.horizontalList(cmp.text("  Report#: ").setStyle(boldLtAlignedStyle), cmp.text(m9kStatusReportDTO.getReportId()).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))).setFixedWidth(250),
        				      cmp.verticalGap(5),
           					  cmp.horizontalList().add(cmp.text("  Date of Report: ").setStyle(boldLtAlignedStyle), cmp.text(reportDateInReqFormat).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))).setFixedWidth(250),cmp.verticalGap(5),
           					  cmp.horizontalList().add(cmp.text("  Time of Report: ").setStyle(boldLtAlignedStyle), cmp.text(m9kStatusReportDTO.getReportTime()).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))).setFixedWidth(250),cmp.verticalGap(20),
           					cmp.horizontalList().add(cmp.text("  Substation: ").setStyle(boldLtAlignedStyle), cmp.text(m9kStatusReportDTO.getStationSpecificDetailsDTO().getStationKey()).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))).setFixedWidth(250),cmp.verticalGap(10),
           					cmp.horizontalList().add(cmp.text("  Analogs Count: ").setStyle(boldLtAlignedStyle), cmp.text(m9kStatusReportDTO.getStationSpecificDetailsDTO().getAnalogCount()).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))).setFixedWidth(250),cmp.verticalGap(10),
           					cmp.horizontalList().add(cmp.text("  Digitals Count: ").setStyle(boldLtAlignedStyle), cmp.text(m9kStatusReportDTO.getStationSpecificDetailsDTO().getDigitalCount()).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))).setFixedWidth(250),cmp.verticalGap(10))
        				 .setStyle(borderStyle)
        				 )
        		.newRow()
//        		.add(cmp.horizontalList().
//        				add((cmp.text("  Substation Name:").setStyle(boldLtAlignedStyle)),cmp.text(m9kStatusReportDTO.getStationSpecificDetailsDTO().getStationKey()).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))).setFixedWidth(250))
        		.add(cmp.filler().setHeight(25))
        		 )
        		 .detail(
		            subreport,
		            cmp.verticalGap(20))

			  .pageFooter(cmp.pageXofY().setStyle(boldCenteredStyle))
			  .setPageFormat(PageType.A4, PageOrientation.PORTRAIT)
//			  .setPageMargin(margin(20))
			  .setPageMargin(margin().setLeft(50).setRight(20).setTop(50).setBottom(50))
			  .setDataSource(createDataSource());
			// COde to print directly
			String fileName = m9kStatusReportDTO.getReportsDir()+m9kStatusReportDTO.createPDFReportFileName(m9kStatusReportDTO.getStationSpecificDetailsDTO().getStationKey()+"-report");
			pdfBuffer = new ByteArrayOutputStream();
			logger.debug("About to create pdf "+fileName);
			JasperPdfExporterBuilder pdfExporter = DynamicReports.export.pdfExporter(pdfBuffer);
			logger.debug("After pdfExporter");
			report.toPdf(pdfExporter);
			logger.debug("After toPDF method");
			pdfBuffer.writeTo(new FileOutputStream (fileName));
			pdfBuffer.close();
			logger.debug("PDF creation done "+fileName);
		} catch (DRException e) {
			e.printStackTrace();
			logger.error("Error in creating pdf ",e);
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in creating pdf ",e);
			throw e;
		}
		finally
		{
			if (pdfBuffer != null)
			{
				pdfBuffer.close();
				pdfBuffer = null;
			}
		}

 }
 
	private JRDataSource createDataSource() {
		return new JREmptyDataSource(2);
	}

	private class SubreportExpression extends
			AbstractSimpleExpression<JasperReportBuilder> {
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unchecked")
		@Override
		public JasperReportBuilder evaluate(ReportParameters reportParameters) {
			int masterRowNumber = reportParameters.getReportRowNumber();

			JasperReportBuilder report = report()
			 .setColumnTitleStyle(columnTitleStyle)
	         .highlightDetailEvenRows();

			try {

				if (masterRowNumber == 1)
				{
					report.addColumn(col.emptyColumn(false, false).setFixedWidth(50)); //empty column
					for (Column column : m9kStatusReportDTO.getStationSpecificDetailsDTO().getWarningSummaryColumns()) {
							report.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType<? super Object, Object>) type.detectType(column.getDataType()))
									.setWidth(column.getColumnLength())
									.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER));
					}
					report.addColumn(col.emptyColumn(false, false).setFixedWidth(50)); //empty column
				}
				else
				{
					for (Column column : m9kStatusReportDTO.getStationSpecificDetailsDTO().getStationSpecificDetailsColumn()) {
						report.addColumn(col.column(column.getTitle(), column.getField(), (DRIDataType<? super Object, Object>) type.detectType(column.getDataType()))
								.setWidth(column.getColumnLength())
								.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER));
					}					
				}
			} catch (DRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return report;

		}

	}

	private class SubreportDataSourceExpression extends
			AbstractSimpleExpression<JRDataSource> {

		private static final long serialVersionUID = 1L;

		@Override
		public JRDataSource evaluate(ReportParameters reportParameters) {

			int masterRowNumber = reportParameters.getReportRowNumber();
			JRDataSource dataSource = null;

			if (masterRowNumber == 1)
			{
				dataSource = m9kStatusReportDTO.getStationSpecificDetailsDTO().getWarningSummaryDataSource();
			}
			else
			{
				dataSource = m9kStatusReportDTO.getStationSpecificDetailsDTO().getStationSpecificDetailsDataSource();
			}

			return dataSource;

		}

	}

	/**
 * @param emailMsg 
 * 
 */
public void sendEmail() {
	File pdfReportFile = new File (m9kStatusReportDTO.getAbsolutePDFReportFileName());
	logger.debug("About to check the pdf file to send email "+m9kStatusReportDTO.getAbsolutePDFReportFileName()+" is file exists "+pdfReportFile.exists());
	if (pdfReportFile.exists())
	{
		SendMailUSI.sendEmailWithAttachment(m9kStatusReportDTO.getStationSpecificDetailsDTO().getStationKey()+" - Alarms Report "+m9kStatusReportDTO.getReportDate()+" "+m9kStatusReportDTO.getReportTime(), "Please find Alarms Report for Station "+m9kStatusReportDTO.getStationSpecificDetailsDTO().getStationKey()+" attached as a PDF document.",pdfReportFile);
	}

	
}





}
