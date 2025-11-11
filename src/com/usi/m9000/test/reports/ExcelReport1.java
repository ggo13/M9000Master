/**
 * 
 */
package com.usi.m9000.test.reports;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.export;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.math.BigDecimal;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.jasper.constant.JasperProperty;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
/**
 * @author Ricardo Mariaca (r.mariaca@dynamicreports.org)
 */
public class ExcelReport1 {



 public ExcelReport1() {

    build();

 }



 private void build() {

    try {

       JasperXlsExporterBuilder xlsExporter = export.xlsExporter("c:/data/report.xls")

                                                    .setDetectCellType(true)

                                                    .setIgnorePageMargins(true)

                                                    .setWhitePageBackground(false)

                                                    .setRemoveEmptySpaceBetweenColumns(true);


       JasperReportBuilder report = DynamicReports.report();//a new report
       report

         .setColumnTitleStyle(Templates.columnTitleStyle)

         .addProperty(JasperProperty.EXPORT_XLS_FREEZE_ROW, "2")

         .ignorePageWidth()

         .ignorePagination()

         .columns(

          col.column("Item",       "item",      type.stringType()),

          col.column("Quantity",   "quantity",  type.integerType()),

          col.column("Unit price", "unitprice", type.bigDecimalType()))

         .setDataSource(createDataSource())
         
         .toXls(xlsExporter);
       try {
           //show the report
	report.show();

} catch (DRException e) {
	e.printStackTrace();
} 
       

    } catch (DRException e) {

       e.printStackTrace();

    }

 }



 private JRDataSource createDataSource() {

    DRDataSource dataSource = new DRDataSource("item", "quantity", "unitprice");

    for (int i = 0; i < 50; i++) {

       dataSource.add("Book", (int) (Math.random() * 10) + 1, new BigDecimal(Math.random() * 100 + 1));

    }

    return dataSource;

 }



 public static void main(String[] args) {

    new ExcelReport1();

 }

}
