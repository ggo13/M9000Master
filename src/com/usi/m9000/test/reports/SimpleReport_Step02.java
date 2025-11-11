/**
 * 
 */
package com.usi.m9000.test.reports;

/**
 * @author sramasamy
 *
 */
/**
02
 * DynamicReports - Free Java reporting library for creating reports dynamically
03
 *
04
 * Copyright (C) 2010 - 2014 Ricardo Mariaca
05
 * http://www.dynamicreports.org
06
 *
07
 * This file is part of DynamicReports.
08
 *
09
 * DynamicReports is free software: you can redistribute it and/or modify
10
 * it under the terms of the GNU Lesser General Public License as published by
11
 * the Free Software Foundation, either version 3 of the License, or
12
 * (at your option) any later version.
13
 *
14
 * DynamicReports is distributed in the hope that it will be useful,
15
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
16
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
17
 * GNU Lesser General Public License for more details.
18
 *
19
 * You should have received a copy of the GNU Lesser General Public License
20
 * along with DynamicReports. If not, see <http://www.gnu.org/licenses/>.
21
 */
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.awt.Color;
import java.awt.Dimension;
import java.math.BigDecimal;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
/**
 * @author Ricardo Mariaca (r.mariaca@dynamicreports.org)
 */
public class SimpleReport_Step02 {

  
JTable tblSample;
JRTableModelDataSource jrTableModelDataSource;

 public SimpleReport_Step02() {

	 String[] columnNames = {"First Name",
             "Last Name",
             "Sport",
             "# of Years",
             "Vegetarian"};
	 Object[][] data = {
			    {"Kathy", "Smith",
			     "Snowboarding", new Integer(5), new Boolean(false)},
			    {"John", "Doe",
			     "Rowing", new Integer(3), new Boolean(true)},
			    {"Sue", "Black",
			     "Knitting", new Integer(2), new Boolean(false)},
			    {"Jane", "White",
			     "Speed reading", new Integer(20), new Boolean(true)},
			    {"Joe", "Brown",
			     "Pool", new Integer(10), new Boolean(false)}
			};
	 tblSample = new JTable(data, columnNames);
	 tblSample.setPreferredScrollableViewportSize(new Dimension(500, 70));
	 tblSample.setFillsViewportHeight(true);
	 JScrollPane scrollPane = new JScrollPane(tblSample);
	 System.out.println("Columns "+tblSample.getModel().getColumnName(0));
	 JPanel pnl = new JPanel();
	 pnl.add(scrollPane);
	 jrTableModelDataSource = new JRTableModelDataSource(tblSample.getModel());
    build();
    JFrame frame = new JFrame("Test");
    frame.setContentPane(pnl);
    frame.pack();
    frame.setVisible(true);

 }

  

 private void build() {

    StyleBuilder boldStyle         = stl.style().bold();

    StyleBuilder boldCenteredStyle = stl.style(boldStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);

    StyleBuilder columnTitleStyle  = stl.style(boldCenteredStyle)

                                        .setBorder(stl.pen1Point())

                                        .setBackgroundColor(Color.LIGHT_GRAY);

    try {

    	System.out.println("data source "+jrTableModelDataSource.toString());
       report()//create new report design

         .setColumnTitleStyle(columnTitleStyle)

         .highlightDetailEvenRows()

         .columns(//add columns

          //            title,     field name     data type

          col.column("First Name",       "First Name",      type.stringType()),

          col.column("Last Name",   "Last Name",  type.stringType()),

          col.column("# of Years", "# of Years", type.integerType()),
          col.column("Vegetarian",   "Vegetarian",  type.booleanType()))

         .title(cmp.text("Getting started").setStyle(boldCenteredStyle))//shows report title

         .pageFooter(cmp.pageXofY().setStyle(boldCenteredStyle))//shows number of page at page footer

         .setDataSource(jrTableModelDataSource)//set datasource
         

         .show();//create and show report

    } catch (DRException e) {

       e.printStackTrace();

    }

 }

  

 private JRDataSource createDataSource() {

    DRDataSource dataSource = new DRDataSource("item", "quantity", "unitprice");

    dataSource.add("Notebook", 1, new BigDecimal(500));

    dataSource.add("DVD", 5, new BigDecimal(30));

    dataSource.add("DVD", 1, new BigDecimal(28));

    dataSource.add("DVD", 5, new BigDecimal(32));

    dataSource.add("Book", 3, new BigDecimal(11));

    dataSource.add("Book", 1, new BigDecimal(15));

    dataSource.add("Book", 5, new BigDecimal(10));

    dataSource.add("Book", 8, new BigDecimal(9));

    return dataSource;

 }

  

 public static void main(String[] args) {

    new SimpleReport_Step02();

 }

}
