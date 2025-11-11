package com.usi.m9000.test;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import com.usi.m9000.util.M9kUtils;

public class TestMicroseconds {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String date1 = "30/08/2010,05:41:11.219333";
		String date2 ="30/08/2010,05:41:10.727333"; 
		long time = 1283161271219L;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy,hh:mm:ss");
		java.util.Date date = null;
		try {
			date = dateFormat.parse(date1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Timestamp dataValueTime = new Timestamp(date.getTime());
		String microSeconds = date1.substring(date1.indexOf(".")+1);
		int multFactor = (int)Math.pow(10.0, (9-microSeconds.length()));
		dataValueTime.setNanos(Integer.parseInt(microSeconds)*multFactor);
		System.out.println("Datavalue: "+dataValueTime.toString());
		System.out.println("Long value: "+(dataValueTime.getTime()));
		long micro = (dataValueTime.getNanos()/1000)+100000;
		System.out.println("Micro: "+micro);
//		dataValueTime = new Timestamp(date.getTime());
		Timestamp ts = new Timestamp(time);
		String displayTime = new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SSS").format(new java.util.Date (date.getTime()+111));
		System.out.println("Time: "+displayTime);
		System.out.println("Time in milli secs before timezone setting \t"+System.currentTimeMillis());
		Calendar calendar = Calendar.getInstance();
		System.out.println("Before .. "+calendar.getTimeZone());
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		System.out.println("After .. "+calendar.getTimeZone());
		System.out.print("Time in milli secs \t\t\t\t"+calendar.getTimeInMillis());
		
		System.out.println(System.nanoTime());
		System.out.println(new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SS").format(new java.util.Date (System.currentTimeMillis())));
		System.out.println(new java.text.SimpleDateFormat("MM/dd/yyyy - HH:mm:ss.SS").format(new java.util.Date (System.currentTimeMillis()+100)));
//		while(true)
//		{
//			System.out.println(System.currentTimeMillis());
//		}
		TestMicroseconds tst = new TestMicroseconds();
		tst.customTime();
		
 		 SimpleDateFormat sdf = M9kUtils.getDateFormat("MM/dd/yyyy - HH:mm:ss.SSS");
	     String microseconds = (""+1362677021618990L);
	     microseconds = microseconds.substring(microseconds.length()-3);
	     displayTime = sdf.format(new java.util.Date (1362677021618990L/1000));
	     displayTime+=microseconds;
	     
		System.out.println("display time "+displayTime);
		
		 TimeZone tz = Calendar.getInstance().getTimeZone();
		 System.out.println("TimeZone: "+tz.getDisplayName());
		 System.out.println("ID: "+tz.getID());
		 System.out.println("Offset "+tz.getRawOffset()/1000/3600);
		 
		 List<String> lstTimestamp = new ArrayList<String>();
		 lstTimestamp.add("02/11/2015 - 10:26:57:004647");
		 lstTimestamp.add("02/11/2015 - 10:26:57:020897");
		 lstTimestamp.add("02/11/2015 - 10:26:57:005272");
		 lstTimestamp.add("02/11/2015 - 10:26:57:025064");
		 Collections.sort(lstTimestamp, Collections.reverseOrder());
		 for (Iterator<String> iterator = lstTimestamp.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
		}
		
		try {
			java.util.Date reverse = sdf.parse(displayTime.substring(0, (displayTime.length()-3)));
			System.out.println("epoch time "+(reverse.getTime()*1000+Integer.parseInt(displayTime.substring((displayTime.length()-3)))));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	private void customTime()
	{
		// Get TimeZone of user
		TimeZone currentTimeZone = Calendar.getInstance().getTimeZone();
		Calendar currentDt = new GregorianCalendar(currentTimeZone);
		// Get the Offset from GMT taking DST into account
		int gmtOffset = currentTimeZone.getOffset(
		    currentDt.get(Calendar.ERA), 
		    currentDt.get(Calendar.YEAR), 
		    currentDt.get(Calendar.MONTH), 
		    currentDt.get(Calendar.DAY_OF_MONTH), 
		    currentDt.get(Calendar.DAY_OF_WEEK), 
		    currentDt.get(Calendar.MILLISECOND));
		// convert to hours
		gmtOffset = gmtOffset / (60*60*1000);
		System.out.println("Current User's TimeZone: " + currentTimeZone.getID());
		System.out.println("Current Offset from GMT (in hrs):" + gmtOffset);

		// Set TS into Calendar
		Calendar issueDate = Calendar.getInstance();
		// Adjust for GMT (note the offset negation)
		issueDate.add(Calendar.HOUR_OF_DAY, gmtOffset);
		System.out.println("Calendar Date converted from TS using GMT and US_EN Locale: "
		    + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
		    .format(issueDate.getTime())+" in Milli secs "+issueDate.getTimeInMillis());
		
		System.out.println("Calendar offset "+Calendar.DST_OFFSET);
	}

}
