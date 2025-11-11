/**
 * 
 */
package com.usi.m9000.test.reports;

/**
 * @author sramasamy
 *
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;

import com.usi.m9000.config.M9kMySqlDatabase;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.exception.DRException;
 
public class SimpleReportExample {
 
  public static void main(String[] args) {
	Connection connection = null;
	try {
		connection = M9kMySqlDatabase.getInstance().getConnection(); 
	}  catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return;
	}
	
 
	JasperReportBuilder report = DynamicReports.report();//a new report
	report
	  .columns(
	      Columns.column("Station Id", "stationId", DataTypes.integerType()),
	      Columns.column("Station Name", "name", DataTypes.stringType()),
	      Columns.column("Analog count", "analogs_count", DataTypes.integerType()),
	      Columns.column("Date", "updated", DataTypes.dateType()))
	  .title(//title of the report
	      Components.text("SimpleReportExample")
		  )
		  .pageFooter(Components.pageXofY())//show page number on the page footer
		  .setDataSource("SELECT stationId, name, analogs_count, updated FROM m9000.station_details", 
                                  connection);
 
	try {
                //show the report
		report.show();
 
                //export the report to a pdf file
		report.toPdf(new FileOutputStream("c:/report.pdf"));
	} catch (DRException e) {
		e.printStackTrace();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
  }
}
