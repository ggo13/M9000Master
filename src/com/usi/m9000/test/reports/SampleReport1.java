/**
 * 
 */
package com.usi.m9000.test.reports;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.field;
import static net.sf.dynamicreports.report.builder.DynamicReports.margin;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.jasper.builder.export.JasperPdfExporterBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.FieldBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.Evaluation;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.VerticalAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 *
 * @author pichansr
 */
public class SampleReport1 {

  public SampleReport1() {

  }
  final StyleBuilder courierNewStyle = stl.style(Templates.columnStyle)
          .setFontName("Courier New").setFontSize(12).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP).setBold(Boolean.FALSE);

  public static void main(String[] args) {

    SampleReport1 sample = new SampleReport1();
    sample.generate();
  }
  int[] columnWidths = {10, 1, 25, 9, 10, 1, 5};
  DRDataSource dataSource = new DRDataSource("firstName", "lastName", "age");
  final JasperReportBuilder report = report();

  private List<Person> persons = new ArrayList<Person>();
  FieldBuilder<String> firstName1 = field("firstName", type.stringType());
  FieldBuilder<String> lastName = field("lastName", type.stringType());
  FieldBuilder<String> age = field("age", type.stringType());

  private Map<Integer, Person> dataForReport = null;

  public Map<Integer, Person> getDataForReport() {
    return dataForReport;
  }

  public void setDataForReport(Map<Integer, Person> dataForReport) {
    this.dataForReport = dataForReport;
  }

  public List<Person> getPersons() {
    return persons;
  }

  public void setPersons(List<Person> persons) {
    this.persons = persons;
  }

  private void createDataSource(List<Person> persons) {

    Map<Integer, Person> datasource = new LinkedHashMap<Integer, Person>();
    int rowNumber = 0;
    if (persons != null && !persons.isEmpty()) {
      for (Person entry : persons) {
        datasource.put(rowNumber++, entry);

      }
    }
    setDataForReport(datasource);
  }

  private JRDataSource createDataSourceSize(List<Person> persons) {
    int size = 0;
    if (persons != null) {
      size = persons.size();
    }
    return new JREmptyDataSource(size);
  }

  private class PageDynamicHead extends AbstractSimpleExpression<String> {

    @Override
    public String evaluate(ReportParameters reportParameters) {
      int masterRowNumber = reportParameters.getReportRowNumber();
      Person person = getDataForReport().get(masterRowNumber - 1);
      if (person != null) {
        return person.getFirstName() + ", " + person.getLastName();
      }
      return "";
    }

  }

  private class PageDynamicFooter extends AbstractSimpleExpression<String> {

    @Override
    public String evaluate(ReportParameters reportParameters) {
      return "";
    }

  }

  private class SubreportDataSourceExpression extends AbstractSimpleExpression<JRDataSource> {

    private static final long serialVersionUID = 1L;

    @Override
    public JRDataSource evaluate(ReportParameters reportParameters) {
      int masterRowNumber = reportParameters.getReportRowNumber();

      List<Person> datasource = new ArrayList<Person>();

      datasource.add(getDataForReport().get(masterRowNumber - 1));
      return new JRBeanCollectionDataSource(datasource);

    }
  }

  private class SubreportExpression extends AbstractSimpleExpression<JasperReportBuilder> {

    private static final long serialVersionUID = 1L;

    @Override
    public JasperReportBuilder evaluate(ReportParameters reportParameters) {
      int masterRowNumber = reportParameters.getReportRowNumber();
      Person person = getDataForReport().get(masterRowNumber - 1);

      JasperReportBuilder report = report();

      HorizontalListBuilder hb = cmp.horizontalList();
      hb.add(cmp.text("First Name").setWidth(columnWidths[0]).setFixedRows(1),
              cmp.text(":").setWidth(columnWidths[1]).setFixedRows(1),
              cmp.text(person.getFirstName()).setWidth(columnWidths[2]).setFixedRows(1).setHorizontalAlignment(HorizontalAlignment.LEFT));
      hb.newRow();
      hb.add(cmp.text("Last Name").setWidth(columnWidths[0]),
              cmp.text(":").setWidth(columnWidths[1]).setFixedRows(1).setFixedRows(1),
              cmp.text(person.getLastName()).setWidth(columnWidths[2]).setFixedRows(1));
      hb.newRow();
      hb.add(cmp.text("Age").setWidth(columnWidths[0]).setFixedRows(1),
              cmp.text(":").setWidth(columnWidths[1]).setFixedRows(1),
              cmp.text(person.getAge()).setWidth(columnWidths[2]).setFixedRows(1));
      hb.newRow();
      hb.add(cmp.text("Address").setWidth(columnWidths[0]).setFixedRows(1),
              cmp.text(":").setWidth(columnWidths[1]).setFixedRows(1),
              cmp.text("XYZ Street").setWidth(columnWidths[2]).setFixedRows(1));
      hb.newRow();

      hb.add(cmp.pageBreak());
      report.detail(hb);
      return report;
    }
  }

  public void generate() {
    prepopulatePersons();
    createDataSource(persons);

    SubreportBuilder subreport = cmp.subreport(new SubreportExpression())
            .setDataSource(new SubreportDataSourceExpression());

    report.setPageFormat(PageType.A4, PageOrientation.PORTRAIT);
    report.setTemplate(Templates.reportTemplate);
    report.setPageMargin(margin(30).setBottom(20));
    report.pageHeader(cmp.text(new PageDynamicHead()).setEvaluationTime(Evaluation.PAGE)
            .setHorizontalAlignment(HorizontalAlignment.RIGHT).setStyle(courierNewStyle));
    report.detail(subreport);
    report.setDataSource(createDataSourceSize(persons));
    report.pageFooter(cmp.pageXslashY().setHorizontalAlignment(HorizontalAlignment.CENTER));
    report.addProperty("net.sf.jasperreports.export.pdf.force.linebreak.policy", "true");
    pdfFileExporter();
  }

  private void pdfFileExporter() {
    Random rn = new Random();
    report.addProperty("net.sf.jasperreports.export.pdf.force.linebreak.policy", "true");
    String fileName = "/data/TestFile" + rn.nextInt() + ".pdf";
    try {
        JasperPdfExporterBuilder pdfExporter = DynamicReports.export.pdfExporter(fileName);
        report.setPageMargin(margin(30).setBottom(20));
    	System.out.println("About to export pdf "+fileName);
      report.toPdf(pdfExporter);
    } catch (DRException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
    	e.printStackTrace();
	}
//    SampleReport1.openFile(fileName);
  }

  private void prepopulatePersons() {

    Person p1 = new Person("firstName1", "lastName1", 1);
    Person p2 = new Person("firstName2", "lastName2", 2);
    Person p3 = new Person("firstName3", "lastName3", 3);
    Person p4 = new Person("firstName4", "lastName4", 4);
    Person p5 = new Person("firstName5", "lastName5", 5);
    Person p6 = new Person("firstName6", "lastName6", 6);
    Person p7 = new Person("firstName7", "lastName7", 7);
    persons.add(p1);
    persons.add(p2);
    persons.add(p3);
    persons.add(p4);
    persons.add(p5);
    persons.add(p6);
    persons.add(p7);

  }

  public class Person {

    public Person() {

    }

    public Person(String firstName, String lastName, int age) {
      this.firstName = firstName;
      this.lastName = lastName;
      this.age = age;
    }
    private String firstName;
    private String lastName;
    private int age;

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }

  }

  public static void openFile(String fileNameWithPath) {
    Desktop desktop = Desktop.getDesktop();
    File file = new File(fileNameWithPath);
    try {
      desktop.open(file);
    } catch (IOException io) {
      io.printStackTrace();
    }
    file.deleteOnExit();
  }
}

