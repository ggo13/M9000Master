package com.usi.m9000.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.usi.m9000.station.util.M9kBackupUtil;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.util.M9kUtils;

public class TestEpochTime {
	long epoch =1345646259999834L;
//	String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date (1280480467100333*1000));
	public static void main (String args[])
	{
		TestEpochTime tet = new TestEpochTime();
		try {
//		      Statement stmt;
//		      ResultSet rs;
//		      String startDate = "2010-10-06 7:30:20";
//		      try {
//				long epoch = new java.text.SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").parse(startDate).getTime();
				System.out.println("Epoch..."+tet.epoch);
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				 String date = sdf.format(tet.epoch/1000);
			     System.out.println("\ttest_id= " + tet.epoch +"\n\t Date: " + date);
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		      //Register the JDBC driver for MySQL.
//		      Class.forName("com.mysql.jdbc.Driver");
//
//		      //Define URL of database server for
//		      // database named JunkDB on the localhost
//		      // with the default port number 3306.
//		      String url =
//		            "jdbc:mysql://195.1.1.85:3306/m9000";
//		      Connection con =
//                  DriverManager.getConnection(
//                     url,"dfr", "usi");
//		    stmt = con.createStatement();
//		    //Query the database, storing the result
//		   // in an object of type ResultSet
//		    System.out.println("Abput to execute");
//		    stmt.executeUpdate("insert into dat values(23, 'ASCII', unix_timestamp(now()), unix_timestamp(now()), 60, 6000, 200, 'testAnalog', 'TestEvents','testDat')");
			     String startDateTime="2012-07-31 07:50:14";
			     Calendar localCal = Calendar.getInstance();
			     Calendar utcCal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			     utcCal.set(Calendar.HOUR_OF_DAY, 7);            // 0..23
			     utcCal.set(Calendar.MINUTE, 50);
			     utcCal.set(Calendar.SECOND, 14);
			     System.out.println("UTC convert "+utcCal.getTime());
			     SimpleDateFormat sdfLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			     SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			     System.out.println("\nEPOCH: "+sdfLocal.parse("2012-08-20 10:15:15").getTime());
//			     sdfLocal.setTimeZone(TimeZone.getTimeZone("EST"));
			     sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
				  Date inptdate = null;
				  Date localDate;
				  Date utcDate;
			     try {
				        inptdate = sdfLocal.parse(startDateTime);
				        localDate = sdfLocal.parse(startDateTime);
				        utcDate = sdfUTC.parse(startDateTime);
				        utcCal.setTime(inptdate);
				        localCal.setTime(inptdate);
				        inptdate = sdfUTC.parse(startDateTime);
				        System.out.println("\t\tConverted to utc "+utcDate+" "+sdfLocal.format(localDate));
				        System.out.println("\t\tConverted to local "+localDate+" "+sdfUTC.format(localDate));
				        localCal.setTimeInMillis(utcCal.getTimeInMillis());
				    } catch (ParseException pe) {pe.printStackTrace();}
				    
				    startDateTime = sdfLocal.format(inptdate);
				    System.out.println("CONVERT: date converted Start "+startDateTime);
				    System.out.println("Local cal "+localCal.get(Calendar.HOUR));
				    System.out.println("sdfLocal.parse(startDateTime) "+sdfLocal.parse(startDateTime));
				    utcCal.setTimeInMillis(sdfLocal.parse(startDateTime).getTime());
//				    localCal.setTime(utcCal.getTime());
				    localCal.set(Calendar.YEAR, utcCal.get(Calendar.YEAR));
				    localCal.set(Calendar.MONTH, utcCal.get(Calendar.MONTH));
				    localCal.set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH));
				    localCal.set(Calendar.HOUR_OF_DAY, utcCal.get(Calendar.HOUR_OF_DAY));
				    localCal.set(Calendar.MINUTE, utcCal.get(Calendar.MINUTE));
				    localCal.set(Calendar.SECOND, utcCal.get(Calendar.SECOND));
				    System.out.println("Time in epoch "+localCal.getTimeInMillis());
				    System.out.println("Time in date "+localCal.getTime());
				    long dateEpoch = 1345646259999834L;
				    Date cvtDate = new Date(dateEpoch);
				    System.out.println("converted date "+cvtDate);
				    System.out.println("date to display "+M9kUtils.convertDateToDisplay(cvtDate));
				    
				    System.out.println("Current time "+Calendar.getInstance().getTime());
				    
				    java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("dd/MM/yyyy,hh:mm:ss");
				    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy,HH:mm:ss");
				    String strDate = "20/09/2012,12:07:33";
				    System.out.println("Formatted "+dateFormat.parse(strDate));;
				    
				    String dateStr = new java.text.SimpleDateFormat("yyMMdd,HHmmss").format(System.currentTimeMillis());
				    System.out.println("Date str "+dateStr);
				    
				    Date zero = new Date(0L);  
				    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z (Z)");  
			        System.out.println(df.format(zero));  
			  
			        
			        df.setTimeZone(TimeZone.getTimeZone("GMT"));  
			        System.out.println(df.format(zero));  
			  
			        df.setTimeZone(TimeZone.getTimeZone("UTC"));  
			        System.out.println(df.format(zero)); 
				    
			        SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
//			        fileDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//					fileDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//					destFileName = M9kStationConstants.CONT_ANALOG_DATA_TYPE+"-"+fileDateFormat.format(startDate)+"_"+fileDateFormat.format(endDate);
					SimpleDateFormat sdfForFileName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					sdfForFileName.setTimeZone(TimeZone.getTimeZone("UTC"));
					System.out.println(fileDateFormat.format(sdfForFileName.parse("2014-11-05 11:39:05")));
					System.out.println("Converted Date test "+tet.getRequiredDataFormat("yyyy-MM-dd HH:mm:ss",sdfForFileName.parse("2014-11-05 11:39:05")));
					
//					List<ComtradeDataDTO> lstComtradePhaseDataDtos = new ArrayList<ComtradeDataDTO>( );
//					ComtradeDataDTO comtradeDataDTO2 = new ComtradeDataDTO();
//					comtradeDataDTO2.setDfrId(2);
//					comtradeDataDTO2.setExportChnlId(2);
//					lstComtradePhaseDataDtos.add(comtradeDataDTO2);
//					ComtradeDataDTO comtradeDataDTO3 = new ComtradeDataDTO();
//					comtradeDataDTO3.setDfrId(3);
//					comtradeDataDTO3.setExportChnlId(3);
//					lstComtradePhaseDataDtos.add(comtradeDataDTO3);
//					ComtradeDataDTO comtradeDataDTO1 = new ComtradeDataDTO();
//					comtradeDataDTO1.setDfrId(1);
//					comtradeDataDTO1.setExportChnlId(1);
//					lstComtradePhaseDataDtos.add(comtradeDataDTO1);
//					System.out.println("BEfore sort "+lstComtradePhaseDataDtos);
//					Collections.sort(lstComtradePhaseDataDtos, new Comparator<ComtradeDataDTO>() {
//
//						@Override
//						public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
//							int returnVal;
//							if ((o1.getDfrId() < o2.getDfrId()))
//							{
//								returnVal= -1;
//							}
//							else if (o1.getDfrId() == o2.getDfrId())
//							{
//								if (o1.getExportChnlId() < o2.getExportChnlId())
//								{
//									returnVal =  -1;
//								}
//								else
//								{
//									returnVal = 1;
//								}
//							}
//							else
//							{
//								returnVal = 1 ;
//							}
//							return returnVal;
//						}
//					});
//					System.out.println("After sort "+lstComtradePhaseDataDtos);
					
					
					Short magShortDataVal = new Double(Math.hypot(-17.512697, 0.16779408)).shortValue();
					Short phaseShortDataVal =  new Double(Math.toDegrees(Math.atan2(0.16779408, -17.512697))).shortValue();
					System.out.println("Magnitude "+magShortDataVal+" Phase "+phaseShortDataVal);
					
					magShortDataVal = new Double(Math.hypot(12.9547, 11.7866) * (32767.0f/41.0)).shortValue();
					phaseShortDataVal =  new Double(Math.toDegrees(Math.atan2(11.7866, 12.9547)) * (32767.0f/360.0)).shortValue();
					System.out.println("Magnitude "+magShortDataVal+" Phase "+phaseShortDataVal);
					System.out.println("First version "+magShortDataVal/(32767f/41.0)+", "+phaseShortDataVal/(32767.0f/360.0f));

					magShortDataVal = new Double(Math.hypot(12.9547, 11.7866)).shortValue();
					magShortDataVal = (short) (magShortDataVal * (32767.0f/41.0));
					phaseShortDataVal =  new Double(Math.toDegrees(Math.atan2(11.7866, 12.9547))).shortValue();
					phaseShortDataVal =  (short)(phaseShortDataVal * (32767.0f/360.0)); 
					System.out.println("Magnitude "+magShortDataVal+" Phase "+phaseShortDataVal);
					System.out.println("Second version "+magShortDataVal/(32767f/41.0)+", "+phaseShortDataVal/(32767.0f/360.0f));

					System.out.println("Magnitude "+Math.hypot(12.9547, 11.7866)+" Phase "+Math.toDegrees(Math.atan2(11.7866, 12.9547)));

					String sT = "2014-11-18 19:00:00";
					String eT = "2014-11-19 19:00:00";
					Date startDateObj = M9kUtils.convertStringToDate(sT);
					System.out.println("SEARCH date "+M9kUtils.convertDateToSearch(sT));
					Date stopDateObj = M9kUtils.convertStringToDate(eT);
					long diff = stopDateObj.getTime() - startDateObj.getTime();
					System.out.println("diff "+diff+" "+(120/60));
					System.out.println("diff in seconds "+(long)(diff/1000)/60);
					System.out.println("dfr1-ip-addresss".substring("dfr1-ip-addresss".lastIndexOf("-")+1));
					
					System.out.println("Date string "+new SimpleDateFormat("MMddyyyy_HHmmss'.ser'").format(new Date()));
					String dt = "01/07/2015 - 17:04:06.017564";
					String microseconds = dt.substring(dt.indexOf("."));

					SimpleDateFormat srcDateFormat = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss");
//					srcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
					System.out.println("Date "+srcDateFormat.parse(dt));
					SimpleDateFormat destDateFormat = new SimpleDateFormat("MMddyyyyHHmmss");
//					destDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
					System.out.println("Date Str "+destDateFormat.format(srcDateFormat.parse(dt))+microseconds);
				
					long result = (long)((952.0/9600.0)*1000000);
					System.out.println("Timestamp comtrade "+result);
					
					System.out.println("\n\n\t\tGet dispplay time "+tet.getDisplayTime());
					System.out.println("\t\tDisplay replaced with , "+tet.getDisplayTime().replace(" -", ",")+"\n\n");
					
					System.out.println(M9kStationUtil.getDateFormat("yyyy-MM-dd HH:mm:ss").format(1434426287633605L/1000));
					System.out.println("current date "+sdfLocal.format(new Date()));
					String tst = "8";
					String tst2 = "A8";
					System.out.println("Is it true? "+("A"+tst).equalsIgnoreCase(tst2));
					
					sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
					System.out.println(""+sdf.format(1344419977070083L/1000));
					
					System.out.println("File Name date format "+M9kStationUtil.getDateFormat("yyMMdd,HHmmssSSS").format(new java.util.Date (1344419977070083L/1000)));
					
					System.out.println("Time from system "+System.currentTimeMillis());
					destDateFormat = M9kStationUtil.getDateFormat("yyyy-MM-dd HH:mm:ss");
//					destDateFormat.setTimeZone(TimeZone.getTimeZone("est"));
					System.out.println("Date/Time from system "+destDateFormat.format(System.currentTimeMillis()));
					System.out.println("Time zone offset "+TimeZone.getDefault().getOffset(1478100802283333L));
					System.out.println(1478100802283333L+TimeZone.getDefault().getOffset(1478100802283333L));
					System.out.println("Time zone "+TimeZone.getDefault());
					String startT = "2017-02-27 05:15:00";
					SimpleDateFormat sdfcUtc = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					SimpleDateFormat sdfclocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					sdfcUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
					System.out.println(sdfcUtc.format(sdfclocal.parse(startT).getTime()));
					Calendar cal = Calendar.getInstance();
					String timeCode = (cal.get(Calendar.ZONE_OFFSET)+cal.get(Calendar.DST_OFFSET))/1000/3600 +"t";
					System.out.println(timeCode);
					
					long localUTC = 1508948513100000L;
					long micro = localUTC % 1000000;
					String strLocalUTC = sdfcUtc.format(localUTC/1000);
					System.out.println("local UTC "+strLocalUTC);
					long convertedUTC = sdfclocal.parse(strLocalUTC).getTime()*1000L;
					System.out.println("LOcalUTC "+localUTC+" convertedUTC "+convertedUTC);
					String strconvertedUTC = sdfcUtc.format(convertedUTC/1000);
					System.out.println("Converted UTC "+strconvertedUTC);
					long reqUTC = sdfcUtc.parse(strconvertedUTC).getTime()/1000L;
					reqUTC= (reqUTC*1000000)+micro;
					System.out.println("Reequired UTC"+reqUTC);
					
					SimpleDateFormat sdfDfrTime = new SimpleDateFormat("MMM d yyyy - HH:mm:ss");
					String dfrTime = "Feb 16 2018 - 11:54:54.446939";
					Date minDate = sdfDfrTime.parse(dfrTime);
					System.out.println("PArsed DFR TIME minDate "+minDate);
					
					sdfDfrTime = new SimpleDateFormat("MMM d yyyy - HH:mm:ss");
					dfrTime = "Feb 16 2018 - 11:56:04.446939";
					Date maxDate = sdfDfrTime.parse(dfrTime);
					System.out.println("PArsed DFR TIME maxDate "+maxDate);
					
					System.out.println("Difference in dates "+((maxDate.getTime() - minDate.getTime())/1000));
					System.out.println("Date "+M9kUtils.getCurrentDateWithoutTime());
					
					tet.printBoardDetect(234881024);
					SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyyMMddHHmmss");
					System.out.println(sdfFormat.format(sdfclocal.parse(startT).getTime()));
					String i = "ad";
					System.out.println("is i numeric? "+i.matches("-?\\d+"));
					
					long originalContStartTime = sdf.parse("2018-06-04 00:00:00").getTime();
					long actualContStopTime = originalContStartTime + 3599000;
					
					System.out.println("\n\n\t\tStart Time "+new Date(originalContStartTime) +" End Time "+new Date(actualContStopTime));
					DateFormat dateFileFormat = new SimpleDateFormat("yyyyMMdd");
					System.out.println(dateFileFormat.format(new Date()));
					
					   Calendar c = Calendar.getInstance();
				        c.setTime(new Date());
				        c.add(Calendar.HOUR, -1);
				        c.set(Calendar.MINUTE, 0);
				        c.set(Calendar.SECOND, 0);
				        c.set(Calendar.MILLISECOND, 0);

				       System.out.println(" Time in milliseconds "+ c.getTimeInMillis()*1000);
				       
				       long start = 1559674800000L;
				       long endTime = (1559674800000L + (1 * 59000));
				       long endTimeSeconds = endTime - 1000;
				       
				       System.out.println("start time "+new Date(start)+"Add 1 minute "+ new Date (endTime)+ " sub 1 second "+new Date(endTimeSeconds));
				       System.out.println("date from milli "+new Date(c.getTimeInMillis()));
				       System.out.println(sdfUTC.parse(sdfclocal.format(new Date(c.getTimeInMillis()))).getTime());
				       System.out.println(M9kBackupUtil.getLastHourToAutoExport());
				       
				       
				   	Date startDate = new Date(1559728741000L);
				   	System.out.println("actual date "+startDate);
				   	System.out.println("Local formatted "+sdfclocal.format(startDate));
				   	System.out.println("Local formatted get millis "+sdfUTC.parse(sdfclocal.format(startDate)).getTime());
				   	System.out.println("Local formatted get millis date "+new Date(sdfclocal.parse(sdfUTC.format(startDate)).getTime()));
				   	System.out.println("UTC Parse "+sdfclocal.parse(sdfUTC.format(startDate)));

						try {
							startDate = new Date(M9kBackupUtil.getConvertedEPOCHTime(1559728741L));
							
						} catch (ParseException e1) {
						}
						System.out.println( tet.getRequiredDataFormat("yyyyMMdd-HHmmss",startDate));
						
					     sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
						startDate = new Date(sdfLocal.parse(sdfLocal.format(1559728741000000L/1000)).getTime());
						System.out.println(tet.getRequiredDataFormat("yyyyMMdd-HHmmss",startDate));
						startDate = new Date(1559728741000000L/1000);
						System.out.println(tet.getRequiredDataFormat("yyyyMMdd-HHmmss",startDate));
						
						System.out.println(M9kBackupUtil.getStartTimeOFTheDay());
						System.out.println("delete day "+M9kBackupUtil.getDateFolderToDelete(1));
						System.out.println(""+M9kBackupUtil.getCurrentDateFolderForAutoExport());
						
						System.out.println("time in milliseconds to the previous whole hour "+(M9kBackupUtil.getMillSecondsFromStartOfHourToRun(5)));
						
						tet.getSearchString("2020-04-24");
						
						System.out.println("test conversion: "+M9kUtils.convertDateFormatString("2020-05-15 00:00:00.0", "yyyy-MM-dd HH:mm:ss", "MM/dd/yyyy"));
						
						int id = 5004;
						System.out.println("Id mod 5000 "+(id / 5000));
					
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
//		String ip = "192.168.1.101";
//		System.out.println(ip.substring(11));
//   //Display URL and connection information
//   System.out.println("URL: " + url);
//   System.out.println("Connection: " + con);
//
//   //Get a Statement object
//   stmt = con.createStatement();
//   //Query the database, storing the result
//   // in an object of type ResultSet
//   rs = stmt.executeQuery("SELECT ts " +
//             "from dat");
//
//   //Use the methods of class ResultSet in a
//   // loop to display all of the data in the
//   // database.
//   System.out.println("Display all results:");
//   long theInt=0L;
//   String microseconds;
//   while(rs.next()){
//     theInt= rs.getLong("ts");
//     microseconds = (""+theInt);
//     microseconds = microseconds.substring(microseconds.length()-3);
//     String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS").format(new java.util.Date (theInt/1000));
//     System.out.println("\ttest_id= " + theInt +"\n\t Date: " + date+microseconds);
//   }//end while loop
//
//		}catch( Exception e ) {
//		      e.printStackTrace();
//	    }//end catch
	}
	
	private String getRequiredDataFormat(String requiredFormat, Date dateToFormat)
	{
		String convertedDateFormat = null;
		SimpleDateFormat requiredDateFormat = new SimpleDateFormat(requiredFormat);
		requiredDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		convertedDateFormat = requiredDateFormat.format(dateToFormat);
		return convertedDateFormat;
	}
	
	public String getDisplayTime() {
//	     String microseconds = (""+getTimeStamp());
//	     microseconds = microseconds.substring(microseconds.length()-3);
	  // Start: 31-Jan-2013 All date conversion are in M9kUtils class
//	     java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SSS"); 
//		  sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	     java.text.SimpleDateFormat sdf  = M9kUtils.getDateFormat("MM/dd/yyyy - HH:mm:ss.SSS");
		// End: 31-Jan-2013
//	     displayTime = new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SSS").format(new java.util.Date (getTimeStamp()/1000));
		  String displayTime = sdf.format(new java.util.Date (getTimeStamp()/1000));
//	     displayTime+=microseconds;
		return displayTime;
	}

	/**
	 * @return
	 */
	private long getTimeStamp() {
		// TODO Auto-generated method stub
		// 1434426287633605L
		// 1434426287633605L
		return 1434426287633605L;
	}
	private void printBoardDetect(long brddet)
	{
		brddet = brddet & 0xffffffffL;
		
		for (int i = 0; i < 4; i++) {
			System.out.println("Analog["+i+"]\t: "+(( brddet & (1 << (27-i))) > 0  ? "present" : "empty" ) );
			
		}
		System.out.println();
		for (int i = 0; i < 4; i++) {
			System.out.println("Event["+i+"]\t: "+(( brddet & (1 << (23-i))) > 0 ? "present" : "empty" ) );
			
		}
	}
	
	private void getSearchString(String dateToSearch) throws ParseException
	{
		String queryDate = "(ts between unix_timestamp('START')*1000000 and unix_timestamp ('END')*1000000)";
		DateFormat searchDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal  = Calendar.getInstance();
		cal.setTime(searchDateFormat.parse(dateToSearch));
		
		queryDate = queryDate.replace("START", (searchDateFormat.format(cal.getTime())+" 00:00:00"));
        cal.add(Calendar.DATE, 1);
        queryDate = queryDate.replace("END", (searchDateFormat.format(cal.getTime())+" 00:00:00"));
        
        System.out.println("Query string to be returned..."+queryDate);
	}

}
