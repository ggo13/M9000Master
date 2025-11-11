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
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.math.BigDecimal;

import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
/**
 * @author Ricardo Mariaca (r.mariaca@dynamicreports.org)
 */
public class SimpleReport_Step01 {

  

 public SimpleReport_Step01() {

    build();

 }

  

 private void build() { 

    try {

       report()//create new report design

         .columns(//add columns

          //            title,     field name     data type

          col.column("Item",       "item",      type.stringType()),

          col.column("Quantity",   "quantity",  type.integerType()),

          col.column("Unit price", "unitprice", type.bigDecimalType()))

         .title(cmp.text("Getting started"))//shows report title

         .pageFooter(cmp.pageXofY())//shows number of page at page footer

         .setDataSource(createDataSource())//set datasource

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

    new SimpleReport_Step01();

 }

}
