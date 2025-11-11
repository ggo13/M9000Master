package com.usi.m9000.test;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.lang.StringEscapeUtils;

import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.StationReportDTO;
import com.usi.m9000.station.util.M9kBackupUtil;
import com.usi.m9000.util.M9kConstants;
import com.usi.m9000.util.M9kKeyValuePair;


public class TestStringBuffer {
	static HierarchicalINIConfiguration iniConf;
	Map<String, Integer> mapTest = new HashMap<String, Integer>(10);
	public static void main(String[] args) throws Exception
	{
//		try
//		{
//		StringBuffer test = new StringBuffer();
//		test.append("Test1");
//		System.out.println("Before "+test.toString());
//		test.setLength(0);
//		System.out.println("After "+test.toString()+" Is empty "+test.toString().isEmpty());
//		int event = (int)(2 * Math.ceil((float)33/16));
//		int eventsSpilledOver = (int)(2 * 192 % 16);
//		int mergedEventBytesToRead = (int)(2 * Math.floor((float)193/16));
//		System.out.println("Event bytes to read "+event);
//		System.out.println("Merged Event bytes to read "+mergedEventBytesToRead);
//		System.out.println("Event remaining to read "+eventsSpilledOver);
		//		
//		System.out.println("Test string..."+test.toString());
//		StringReader sbi = new StringReader(test.toString());
			
//			short[][] data = new short[2][4];
//			data[0][0]=1;
//			data[0][1]=2;
//			data[0][2]=3;
//			data[1][0]=1;
//			data[1][1]=2;
//			data[1][2]=3;
//			System.out.println("size of dat : "+data[0].length);
//		String str = "SCOPE,samples=2400,ch=1,ch=2";
//		System.out.println("Prefix command "+str.substring(0,str.indexOf("ch=")+3));
//			iniConf = new HierarchicalINIConfiguration("station.properties");
//			System.out.println("Host name: "+iniConf.getString("station.master_host")+" interval "+iniConf.getInt("station.health_check_frequency"));
			TestStringBuffer tst = new TestStringBuffer();
//			StringBuffer tstEvents = new StringBuffer();
//			tstEvents.append("E193 - Trigger(T_A1-Kettle Creek 220kV)");
//			tstEvents.append(M9kConstants.NEWLINE);
//			tstEvents.append("E194 - Trigger(T_A17-a)");
//			tstEvents.append(M9kConstants.NEWLINE);
//			tstEvents.append("E227 - Trigger(T_A49-i)");
//			tstEvents.append(M9kConstants.NEWLINE);
//			int[] events = tst.getActiveEventsIndex(tstEvents.toString());
//			for (int i = 0; i < events.length; i++) {
//				System.out.println("Events : "+events[i]);
//			}
			
//			String dfrIpAddress = "192.168.1.101";
//			  String ipPrefix = dfrIpAddress.substring(0,dfrIpAddress.lastIndexOf(".")+1);
//			  int ipAddressCount = Integer.parseInt(dfrIpAddress.substring(dfrIpAddress.lastIndexOf(".")+1));
//			  
//			  System.out.println("Prefix "+ipPrefix);
//			  System.out.println("Ip count "+ipAddressCount);
			  
			  
//			  for (int i = 0; i < 10; i++) {
//				  System.out.println("Value of i -> "+i);
//				for (int j = 0; j < 10; j++) {
//					System.out.println("Value of j -> "+j);
//					if (i == 2&& j == 5)
//					{
//						break;
//					}
//				}
//			}
//			tst.mapTest.put("One",1);
//			tst.mapTest.put("Two",2);
//			System.out.println("Size of map "+tst.mapTest.size());
			
			
			// to replace -1 with - 1
//			String strLine = "BP-1 ch -1 re-1 -1-1-1";
//			System.out.println("Actual string "+strLine);
//			if (strLine.indexOf("-1") != -1)
//			{
//				strLine = strLine.replaceAll("-1", "- 1");
//				System.out.println("Changed string "+strLine);
//			}
//			
//			// date format util test
//			System.out.println(M9kStationUtil.getDateFormat("yyMMdd,HHmmssSS").format(new java.util.Date (System.currentTimeMillis())));
//			
//			String analogName = "RMS-1-Analog22";
//			System.out.println("Analog name "+analogName.substring(analogName.lastIndexOf("-")+1));
//		}catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//		
//		
//		String eventsXml = "@Channels(Channels)/Events(Events)/EventInput(Event1)/_Object";
//		int startIindex = eventsXml.indexOf(M9kXMLConstants.XML_EVENT_CHANNELS_INPUT) + M9kXMLConstants.XML_EVENT_CHANNELS_INPUT.length();
//		int endIndex = eventsXml.indexOf(M9kXMLConstants.XML_VALUE_INPUT);
//		System.out.println("\n\nEvents Name "+eventsXml.substring(startIindex, endIndex));
//		
//		String rmsstr= "@Algorithms(Algorithms)/Rms(1-Analog1)/_Value";
//		System.out.println("Rms str "+rmsstr.substring(M9kXMLConstants.XML_ALGORITHMS_INPUT.length(), rmsstr.indexOf("(", M9kXMLConstants.XML_ALGORITHMS_INPUT.length())));
//		rmsstr = "Rms-1-Analog1";
//		System.out.println("name: "+rmsstr.substring(rmsstr.lastIndexOf("-")+1));
//		
//		Map<String, Integer> mapTest = new HashMap<String, Integer>();
//		mapTest.put("Analog1",1);
//		mapTest.put("Analog2",2);
//		
//		System.out.println("Map test "+mapTest);
		
//		String analog = "Analog2";
//		System.out.println(analog.substring("Analog".length()));
//		List<ComtradeDataDTO> lstComtradeDataDtos = new ArrayList<ComtradeDataDTO>();
//		ComtradeDataDTO test1 = new ComtradeDataDTO();
//		test1.setDfrId(2);
//		test1.setExportId(4);
//		lstComtradeDataDtos.add(test1);
//		ComtradeDataDTO test2 = new ComtradeDataDTO();
//		test2.setDfrId(1);
//		test2.setExportId(5);
//		lstComtradeDataDtos.add(test2);
//		ComtradeDataDTO test3 = new ComtradeDataDTO();
//		test3.setDfrId(1);
//		test3.setExportId(4);
//		lstComtradeDataDtos.add(test3);
//		ComtradeDataDTO test4 = new ComtradeDataDTO();
//		test4.setDfrId(2);
//		test4.setExportId(5);
//		lstComtradeDataDtos.add(test4);
//		ComtradeDataDTO test5 = new ComtradeDataDTO();
//		test5.setDfrId(1);
//		test5.setExportId(1);
//		lstComtradeDataDtos.add(test5);
//		ComtradeDataDTO test6 = new ComtradeDataDTO();
//		test6.setDfrId(2);
//		test6.setExportId(1);
//		lstComtradeDataDtos.add(test6);
//		ComtradeDataDTO test7 = new ComtradeDataDTO();
//		test7.setDfrId(1);
//		test7.setExportId(3);
//		lstComtradeDataDtos.add(test7);
//		ComtradeDataDTO test8 = new ComtradeDataDTO();
//		test8.setDfrId(2);
//		test8.setExportId(3);
//		lstComtradeDataDtos.add(test8);
//		ComtradeDataDTO test9 = new ComtradeDataDTO();
//		test9.setDfrId(1);
//		test9.setExportId(2);
//		lstComtradeDataDtos.add(test9);
//		ComtradeDataDTO test10 = new ComtradeDataDTO();
//		test10.setDfrId(2);
//		test10.setExportId(2);
//		lstComtradeDataDtos.add(test10);
//		System.out.println("Before sort "+lstComtradeDataDtos);
//		Collections.sort(lstComtradeDataDtos, new Comparator<ComtradeDataDTO>() {
//
//			@Override
//			public int compare(ComtradeDataDTO o1, ComtradeDataDTO o2) {
////				return ((o1.getDfrId() < o2.getDfrId())?0:1);
//				int returnVal;
//				if ((o1.getDfrId() < o2.getDfrId()))
//				{
//					returnVal= -1;
//				}
//				else if (o1.getDfrId() == o2.getDfrId())
//				{
//					if (o1.getExportId() < o2.getExportId())
//					{
//						returnVal =  -1;
//					}
//					else
//					{
//						returnVal = 1;
//					}
//				}
//				else
//				{
//					returnVal = 1 ;
//				}
////				return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
//				return returnVal;
//			}
//		});
//		System.out.println("After sort "+lstComtradeDataDtos);
		
//		int stationId = 9;
//		System.out.println(String.format("%02d", stationId));
			String arg0 = "4 - stationName";
			List<String> lstDownStaionsList = new ArrayList<String>();
			lstDownStaionsList.add(arg0);
			lstDownStaionsList.add("1 - Test");
			if (lstDownStaionsList.contains(arg0))
			{
//				System.out.println("Yes contains "+arg0);
			}
			else
			{
//				System.out.println("No it doesn;t contain "+arg0);
			}
			int lhsKey = Integer.parseInt(arg0.substring(0, arg0.indexOf("-")).trim());
//			System.out.println("int key : "+lhsKey);
			StringBuffer strBuff = new StringBuffer();
			 Formatter formatter = new Formatter(strBuff);
//			 System.out.println();
//			 formatter.format("%1$-25s %2$-25s %3$-25s %4$-25s %n","stationName","equipment Name","Health Check Result","status");
			 formatter.format("%1$180s  %n%n","stationName equipment Name Health Check Result status");
//			 formatter.format("%1$25s%n%2$10d","stationName equipment Name Health Check Result status",25);
//			strBuff.append("This is a test");
//			strBuff.setLength(0);
//			System.out.println("<Font face=\"courier\"><pre>"+" xTesttttttt "+strBuff.toString()+"</pre></Font>");
			StationReportDTO tstDto = new StationReportDTO();
			StringBuffer strBuff2 = new StringBuffer();
			
			strBuff2.append(tstDto.getWarningSummary());
//			System.out.println(String.format("%1$25s%n",tstDto.getWarningSummary()));
//			System.out.println("\n secondary \n"+strBuff2);
			
			String ip = "192.168.1.103";
//			System.out.println("PMU ID "+ip.substring(ip.lastIndexOf(".")+1));
			
			TimeZone tz = Calendar.getInstance().getTimeZone();
//			System.out.println((tz.getRawOffset()/1000/3600)+"t");
			Calendar cal = Calendar.getInstance();
//			System.out.println("cal.get(Calendar.ZONE_OFFSET) "+cal.get(Calendar.ZONE_OFFSET));
//			System.out.println("cal.get(Calendar.DST_OFFSET) "+cal.get(Calendar.DST_OFFSET));
//			
//			System.out.println("Zone offset: "+(cal.get(Calendar.ZONE_OFFSET)+cal.get(Calendar.DST_OFFSET))/1000/3600);
			
			Calendar christmas = new GregorianCalendar(2013, Calendar.OCTOBER, 25);
			tz = christmas.getTimeZone();
//			System.out.println("cal.get(Calendar.ZONE_OFFSET) "+christmas.get(Calendar.ZONE_OFFSET));
//			System.out.println("cal.get(Calendar.DST_OFFSET) "+christmas.get(Calendar.DST_OFFSET));
//			System.out.println("Zone offset: "+(christmas.get(Calendar.ZONE_OFFSET)+christmas.get(Calendar.DST_OFFSET))/1000/3600);
//			System.out.println((tz.getRawOffset()/1000/3600)+"t");
//			System.out.println(tz.getOffset(christmas.getTimeInMillis()) / 1000 / 3600 );
//			
//			System.out.println(Integer.parseInt("-4t".substring(1, 2)));
//			System.out.println("Timer offset  "+M9kUtils.getTimeOffset());
//			System.out.println("60*60*4 "+(60L*60*4*1000000));
//			System.out.println("Timer offset to add to adjust UTC "+((long)(M9kUtils.getTimeOffset()*60*60*1000000)));
//			
//			System.out.println(String.format("%1$-25s %2$-25s %3$-25s %4$-25s%n","No Date Info", "Analog Channels Self Test",M9kConstants.FAIL," Analog Channel Self Test run record not found. "));
//			System.out.println(("\"No Date Info\", \"Analog Channels Self Test\",\""+M9kConstants.FAIL+"\" ,\"Analog Channel Self Test run record not found.\""));
			
//			int reportFreq = M9kUtils.getReportFrequency()/60;
//			System.out.println("Report frequency "+reportFreq);
			
			String v = "";
			System.out.println("Is v null "+(v == null));
			System.out.println("IS it true? "+("vvv".contains(v)));
			
			System.out.println("Ipaddress prefix "+"192.168.1.100".substring(0, "192.168.1.100".lastIndexOf(".")+1));
			System.out.println(Integer.parseInt("192.168.1.100".substring("192.168.1.100".lastIndexOf(".")+1)));
			System.out.println("DFR10".substring(3));
			
			String uuid = UUID.randomUUID().toString();
			System.out.println("uuid = " + uuid);
			
			System.out.println(System.getProperty("java.library.path"));
			
//			System.out.println("Difference "+StringUtils.difference("<abc>"
//					+ "abc"
//					+ "</abc>", "<abc>"
//							+ "def"
//							+ "</abc>"));
			v = "Edisto 115-12kv 135-220kv";
			System.out.println("V before replacement "+v);
			System.out.println("Index of -1 "+v.indexOf("-1"));
			if (v.indexOf("-1") > -1)
			{
				v = v.replaceAll("-1", " - 1");
			}
			System.out.println("V replaced "+v);
			try
			{
//				String srcFileStr = tst.readFile("C:/Users/sramasamy/Downloads/file1.xml");
//				String dstFileStr = tst.readFile("C:/Users/sramasamy/Downloads/file2.xml");
						
//				Diff diff = DiffBuilder.compare(Input.fromString("<abc>"
//						+ "abc"
//						+ "</abc>")).withTest(Input.fromString("<abc>"
//								+ "def"
//								+ "</abc>"))
//					     .checkForSimilar()
//					     .ignoreWhitespace()
//					     .build();
//				System.out.println(diff.toString()+" diff has differences? "+diff.hasDifferences());
//				
//				Diff diff1 = DiffBuilder.compare(Input.fromString(srcFileStr)).withTest(Input.fromString(dstFileStr))
//					     .checkForSimilar()
//					     .ignoreWhitespace()
//					     .build();

//				System.out.println(diff1.toString()+" diff has differences? "+diff1.hasDifferences());
//				Iterable<Difference> iterDiffs = diff1.getDifferences();
//				StringBuffer strBuf = new StringBuffer();
//				int iCnt =0;
//				for (Difference difference : iterDiffs) {
//					iCnt++;
//					strBuf.append(difference.toString(new DefaultComparisonFormatter())+M9kConstants.NEWLINE);
//				}
//				System.out.println("Final differences "+strBuf+" total differnces "+iCnt);
//				BufferedWriter bfw = new BufferedWriter(new FileWriter(new File("c:/m9k/troubleshoot/diff-result.txt")));
//				bfw.write(strBuf.toString());
//				bfw.close();
//				int iLine = 0;
//				for (int i = 1; i < 10001; i++) {
//					if((i % 1000) == 0)
//					{
//						System.out.println("LIne "+ ++iLine+" i "+i);
//					}
//				}
				System.out.println("DFR23".substring("DFR".length()));
				String command = "192.168.1.101,scope, samples=480,ch=1,ch=2";
				System.out.println("ipaddress = "+command.substring(0,command.indexOf(",")));
				command = command.substring(command.indexOf(",")+1);
				System.out.println("Command "+command);
				System.out.println("SUM index "+"SUM152-".substring(3,"SUM152-".indexOf("-")));
				System.out.println("index "+("VirtualAnalog1".indexOf("VirtualAnalog")+("VirtualAnalog".length())));
				System.out.println("virt no after parsing "+Integer.parseInt("VirtualAnalog112".substring(("VirtualAnalog112".indexOf("VirtualAnalog")+("VirtualAnalog".length())))));
				
				int analogChnlsCount = 29;
				System.out.println("physical chnl count "+(analogChnlsCount - (analogChnlsCount%8)));
				long startSampleTime = 1500280619524166L;
				long comtradeDataDTOTsLast = 1500280604150000L;
				double sampleRate = 60.0;
				Long missingSampleCnt = Math.round(((((startSampleTime - comtradeDataDTOTsLast )*sampleRate)/1000000)));
				System.out.println("Missing count "+missingSampleCnt+" int value "+missingSampleCnt.intValue());
				System.out.println("Is valid string "+isValid("(T1 OR T2)"));
				
				StringBuffer strTest = new StringBuffer("T3 - A3 - Analog3 -VNA-RMS- Ph C");

				strTest.append("T6 - A6 - Analog6 -VNA-RMS- Ph B");
				strTest.append("\n");
				strTest.append("T7 - A7 - Analog7 -VNA-RMS- Ph C");
				System.out.println("str Test "+strTest);
				System.out.println("Index "+strTest.toString().split("\\r?\\n").length);
				List<Integer> lstNumbers = Arrays.asList(1,2,3,4,5,6);

				String arith = "-a1 + a2 - a1";
//				String arithArr[] = arith.split("(?<=[-+*/])|(?=[-+*/])"); // Working
				String arithArr[] = arith.split("(?<=[-+*/])|(?=[-+*/])");
				AnalogChannelDTO temp = null;
				String operator;
				int iIndex = 0;
				if (arithArr[0].isEmpty())
				{
					operator = arithArr[1];
					iIndex = 2;
					
				}
				else
				{
					operator = "+";
				}
				for (int i = iIndex; i < arithArr.length; i++) {
//					System.out.println(i+" -> "+arithArr[i]);
					System.out.println("Channel display name "+arithArr[i]+" operator "+operator);
					if ((++i) < arithArr.length)
					{
						operator = arithArr[i];
					}
				}
				System.out.println("\n\t Splittingggggg... "+Arrays.toString(arith.split("(?<=[-+*/])|(?=[-+*/])")));
				System.out.println(Arrays.toString(arith.split("(?<=[-+*/])")));
				System.out.println(Arrays.toString(arith.split("(?=[-+*/])")));
				
//				Pattern p = Pattern.compile("^[?:\\s*[+-]\\s*][a-zA-Z]\\d+(?:\\s*[+-]\\s*[a-zA-Z]\\d+)+$");
				Pattern p = Pattern.compile("^(([a-zA-Z]\\d+)|(?:\\s*[+-]*\\s*))(?:\\s*[+-]\\s*[a-zA-Z]\\d+)+$");
//				Pattern p = Pattern.compile("(\\s[+-]\\s)");
				Matcher m = p.matcher(arith);
				if (m.matches())
				{
					System.out.println("Match Successful");
				}
				else
				{
					System.out.println("Match Unsuccessful");
				}
//				List<String> tokens = new LinkedList<String>();
//				while(m.find())
//				{
//				  String token = m.group( 1 ); //group 0 is always the entire match   
////				  tokens.add(token);
//				  System.out.println("Token "+token);
//				}
				System.out.println("Arith string "+arith);
				System.out.println("Split string "+arith.split("\\+").length);
				arith = null;
				System.out.println(((arith == null)?null:arith.length()));
				
				StringBuffer testAppend = new StringBuffer();
				testAppend.append("A "+"[0 - 9]");
				testAppend.append("E "+"[0 - 32]");
				System.out.println(testAppend.toString());
				System.out.println("color to string "+(Color.RED == Color.RED));
				
				String datestr = "03/12/2019 - 15:48:03.466667";
				datestr = datestr.replaceAll("[:\\/-]", "");
				datestr = datestr.replaceAll("[.]", "");
				datestr = datestr.replaceAll("[ ]", "");
				System.out.println(datestr);
				
				System.out.println(0^0);
				System.out.println(0^1);
				System.out.println(1^0);
				System.out.println(1^1);
				 Set<String> linkedHashSet = new LinkedHashSet<String>();
				 linkedHashSet.add("3");
				 linkedHashSet.add("1");
				 linkedHashSet.add("2");
				 linkedHashSet.add("3");
				 linkedHashSet.add("1");
				 linkedHashSet.add("2");
				 List<String> lstArray = new LinkedList<String>(linkedHashSet);
				 for (String i : lstArray) {
				     System.out.println(i);
				 }
				 String event = "E68 - NYPA";
					int index = event.indexOf("-");
					String key = event.substring(0, index-1).trim();
					String value = event.substring(index+1).trim();
					System.out.println("Key: "+key+" value: "+value);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			boolean config = true;
			config &= true;
			System.out.println("value of boolean "+config);
			String testExports = "'RMS'";
			System.out.println("REplaced string "+testExports.replace("'", "\\'"));
			
			System.out.println("Ceil... Analogs "+Math.ceil(12/8.0)+" Digitals "+Math.ceil(16/32.0));
			String strDfr = "32A-64D";
			System.out.println("Test "+strDfr.substring(0, strDfr.lastIndexOf("-")));
			System.out.println("index of A "+strDfr.substring(0, strDfr.indexOf("A"))+" "+strDfr.substring(strDfr.indexOf("A")+2, strDfr.indexOf("D")));
			System.out.println("");
			StringBuffer buf = new StringBuffer("First Line");
			buf.append(M9kConstants.NEWLINE);
			buf.append("Second Line");
			buf.append(M9kConstants.NEWLINE);
			buf.append("abcdessssssssssssssssssssssssssssssssssssssssssssssss");
			System.out.println("Before "+buf);
			System.out.println("after "+buf.toString().replace(M9kConstants.NEWLINE,";;"));
			
			tst.testListSort();
//			String str = "((1.0 * T1) + (1.0 * T3) + (1.0 * T7)) * -0.333";
			String str = "(3.0 * M28)";
			Pattern p = Pattern.compile("\\((.*?)\\)",Pattern.DOTALL);
			Matcher m = p.matcher(str.substring(str.indexOf("(")+1, str.lastIndexOf(")")));
			String parseAlgorithm;
			if (!m.matches()) {
				p = Pattern.compile("\\((.*?)\\)",Pattern.DOTALL);
				m = p.matcher(str.substring(str.indexOf("(")+1, str.lastIndexOf(")")));
			}
			while(m.find())
			{
				parseAlgorithm = m.group(1);
			    System.out.println("found match:"+parseAlgorithm);
			    System.out.println("input measurement: "+parseAlgorithm.substring(parseAlgorithm.indexOf("*")+1));
			    System.out.println("scale "+Double.parseDouble(parseAlgorithm.substring(0, parseAlgorithm.indexOf("*")).trim()));
			}
			
			String input = "@Algorithms(Algorithms)/Power(29-LG_1)/_Object";
			String triggerChannelName = input.substring(input.indexOf("/"),input.lastIndexOf("/"));
			triggerChannelName = triggerChannelName.substring(triggerChannelName.indexOf("(")+1, triggerChannelName.indexOf(")"));
			System.out.println("Name of trigger "+triggerChannelName);
			
			String oldFilename1 = "LTR_Measurements_190909,175851983,-4t,Guinea,USI_M9000,USI";
			String oldFilename2 = "LTR_ANALOG_191024,141731400,-4t,Demo,USI_M9000,USI";
			String oldFilename3 = "R01F1_150420,101941566,-4t,Linden,USI_M9000,USI";
			String newFilename = "201118,095358600,-5t,Guinea,Test_COMNAME_Company,USI,R04F135";
			Pattern p1 = Pattern.compile("\\d{6},\\d{9}",Pattern.DOTALL);
			m = p1.matcher(oldFilename1);
			while(m.find())
			{
				System.out.println("Is matched? "+m.group(0)); 
			}
			File[] oldFaultFiles = new File("C:\\Users\\sramasamy.USI\\Downloads\\").listFiles();
//			M9kStationUtil.setMinDaysOfFaultsToRetain(365);
//			for (int i = 0; i < oldFaultFiles.length; i++) {
//				if (!M9kStationUtil.isFileOldEnoughToDelete(oldFaultFiles[i].getName()))
//				{
//					System.out.println("File "+oldFaultFiles[i].getName()+" is not old enough");
//				}
//			}
			System.out.println("Is valid email address?"+TestStringBuffer.isValidEmailAddress("123@abc.com"));
			
			Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");
			input = "Chassis123";
			Matcher matcher = lastIntPattern.matcher(input);
			if (matcher.find()) {
			    String someNumberStr = matcher.group(1);
			    int lastNumberInt = Integer.parseInt(someNumberStr);
			    System.out.println("Last Number "+lastNumberInt);
			}
			Pattern firstInt = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
			String backedUp = "/data/m9k/config-files/2021-05-26/05-26-2021,14_43_09,-4t,R01-Eversource Demo-config.sql";
			matcher = firstInt.matcher(backedUp);
			String backedupFileName;
			if (matcher.find()) {
				backedupFileName = backedUp.substring(backedUp.lastIndexOf( matcher.group()));
			}
			else
			{
				backedupFileName = backedUp.substring(backedUp.lastIndexOf("/"));
			}
		    System.out.println("backedupFileName "+backedupFileName);

			System.out.println("backed up path "+backedUp.substring(backedUp.indexOf("config-files/")+"config-files/".length()));
			
			String strbuf = null;
			System.out.println("strbuf null "+(strbuf != null?strbuf.toString():"It's NULL"));
			
			str = "     Geeks     for Geeks A1 + A2 * A3 * A4 + A5 "+String.format("%n")+" New line 1"+String.format("%n")+" New line 2";
			System.out.println(str);
			 
	        // Call the replaceAll() method
	        str = str.replace(" ", "");
	        str = str.replaceAll("\\+", "|");
	        str = str.replaceAll("\\*", "&");
	        str = str.replace(String.format("%n"),";;");
	 
	        System.out.println(str);
	        
	        isExistEvent();
	        
	        testArrays();
	        
	        str = "LG1,LG2,LG3";
	        String[] tstArr=str.split(",");
	        System.out.println("Size of arr "+tstArr.length);
	        
	        testEscapeXMLText();
	        
			 long currentTimeMillis = System.currentTimeMillis();

		        // Calculate the delay until the start of the next hour
		        long delayToNextHour = 3600000 - (currentTimeMillis % 3600000);
		        System.out.println("Delay to next hour: " + delayToNextHour);
		        
		        long singleSampleTime = (1/1200)*1000000;
		        System.out.println("Auto Export Osc: Single Sample time "+singleSampleTime);
		        long startTime = M9kBackupUtil.getLastHourToAutoExport();
		        System.out.println("Start time "+startTime);
				long contStopTime = startTime + ((1*60) * 1000000)- singleSampleTime;
		        System.out.println("stop time "+ contStopTime);
		        
		        DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		        Instant instant = Instant.ofEpochMilli(1702312260000L);
		        ZoneId zoneId = ZoneId.systemDefault(); // Use the system default time zone
		        System.out.println("zoneId "+zoneId);
		        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
		        System.out.println("localDateTime "+localDateTime);
		        System.out.println(localDateTime.format(dateformatter));
		        
//		        LocalDateTime roundFloor =  localDateTime.truncatedTo(ChronoUnit.MINUTES);
//		        LocalDateTime roundCeiling =  localDateTime.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
		        LocalDateTime roundCeiling =  localDateTime.plusMinutes(5).withSecond(0).withNano(0);
		        System.out.println( "round Ceiling "+roundCeiling);
		        System.out.println(roundCeiling.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()*1000);
		        System.out.println(roundCeiling.format(dateformatter));
		        LocalDateTime roundFloor =  localDateTime.withMinute(0).withSecond(0).withNano(0).truncatedTo(ChronoUnit.HOURS);
		        System.out.println(roundFloor.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()*1000);
		        System.out.println(roundFloor.format(dateformatter));
		        System.out.println("Converted hout"+M9kBackupUtil.convertToStartOfTheHour(1702312260000L));

		        System.out.println(M9kBackupUtil.calculateSecondsUntilNextHour());
		        
		        System.out.println(1702461600000000L + ((1*60) * 1000000));
		        
		        System.out.println(Instant.ofEpochMilli(1702396800000L).until(Instant.ofEpochMilli(1702569600000L), ChronoUnit.DAYS));

	}
	
	public static boolean isValidEmailAddress(String email) {
		   boolean result = true;
		   try {
		      InternetAddress emailAddr = new InternetAddress(email);
		      emailAddr.validate();
		   } catch (AddressException ex) {
		      result = false;
		   }
		   return result;
		}
	private void testListSort()
	{
		List<M9kKeyValuePair> lstTest = new ArrayList<M9kKeyValuePair>();
		lstTest.add(new M9kKeyValuePair("DFR1", "17"));
		lstTest.add(new M9kKeyValuePair("DFR2", "18"));
		lstTest.add(new M9kKeyValuePair("DFR2", "17"));
		lstTest.add(new M9kKeyValuePair("DFR1", "18"));
		lstTest.add(new M9kKeyValuePair("DFR1", "19"));
		lstTest.add(new M9kKeyValuePair("DFR1", "20"));
		lstTest.add(new M9kKeyValuePair("DFR2", "19"));
		lstTest.add(new M9kKeyValuePair("DFR2", "20"));
		System.out.println("Before sort "+lstTest.toString());
		Collections.sort(lstTest, new Comparator<M9kKeyValuePair>() {

			@Override
			public int compare(M9kKeyValuePair o1, M9kKeyValuePair o2) {
//				return ((o1.getDfrId() < o2.getDfrId())?0:1);
				int returnVal = o1.getKey().compareTo(o2.getKey());
				if (returnVal == 0)
				{
					returnVal = o1.getValue().compareTo(o2.getValue());
				}
//				if ((o1.getDfrId() < o2.getDfrId()))
//				{
//					returnVal= -1;
//				}
//				else if (o1.getDfrId() == o2.getDfrId())
//				{
//					if (o1.getExportId() < o2.getExportId())
//					{
//						returnVal =  -1;
//					}
//					else
//					{
//						returnVal = 1;
//					}
//				}
//				else
//				{
//					returnVal = 1 ;
//				}
//				return ((o1.getDfrId() < o2.getDfrId())? -1 : (o1.getDfrId() == o2.getDfrId())?0:1);
				return returnVal;
			}
		});
		System.out.println("After sort "+lstTest.toString());
	}
	private int[] getActiveEventsIndex(String activeEvents)
	{
		String[] strActiveEvents = activeEvents.split(M9kConstants.NEWLINE);
		int eventsIndex[] = new int[strActiveEvents.length];
		int index = -1;
		for (int i = 0; i < strActiveEvents.length; i++) {
			index = strActiveEvents[i].indexOf("-");
			eventsIndex[i] = Integer.parseInt(strActiveEvents[i].substring(1, index-1).trim());
		}
		return eventsIndex;
	}
	
	private String readFile(String pathname) throws IOException {

	    File file = new File(pathname);
	    StringBuilder fileContents = new StringBuilder((int)file.length());
	    Scanner scanner = new Scanner(file);
	    String lineSeparator = System.getProperty("line.separator");

	    try {
	        while(scanner.hasNextLine()) {
	            fileContents.append(scanner.nextLine() + lineSeparator);
	        }
	        return fileContents.toString();
	    } finally {
	        scanner.close();
	    }
	}

	public static boolean isValid(String s) {
		HashMap<Character, Character> map = new HashMap<Character, Character>();
		map.put('(', ')');
		map.put('[', ']');
		map.put('{', '}');
	 
		Stack<Character> stack = new Stack<Character>();
	 
		for (int i = 0; i < s.length(); i++) {
			char curr = s.charAt(i);
	 
			if (map.keySet().contains(curr)) {
				stack.push(curr);
			} else if (map.values().contains(curr)) {
				if (!stack.empty() && map.get(stack.peek()) == curr) {
					stack.pop();
				} else {
					return false;
				}
			}
		}
	 
		return stack.empty();
	}
	
	public static void isExistEvent()
	{
		
		String event = "T61F54";
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(event);
		if (m.find())
		{
			System.out.println("Number matched "+m.group());
		}
	}
	
	public static void testArrays()
	{
		Integer[] activeDigitalChannels = new Integer[10];
		activeDigitalChannels[0] = 0;
		activeDigitalChannels[1] = 1;
		activeDigitalChannels[2] = 2;
		System.out.println("Size of array "+activeDigitalChannels.length);
		int someIndex = Arrays.asList(activeDigitalChannels).indexOf(null);
		System.out.println("Index "+someIndex);
		System.out.println("null? "+activeDigitalChannels[4]);
		activeDigitalChannels = Arrays.copyOfRange(activeDigitalChannels,0,someIndex);
		System.out.println("After Size of array "+activeDigitalChannels.length);
	}
	public static void testEscapeXMLText()
	{
		String xmlStr = "<desc>!<>@#$%^&Test4567)(*&^</desc>";
		xmlStr="1-ANALOG-Analog1-4th Col~`!@#$%^&*()_+-={}[]|\":;'<>?./";
		System.out.println("ACtual String "+xmlStr);
		String escapedStr = StringEscapeUtils.escapeXml(xmlStr);
		System.out.println("Escaped String \t\t\t"+escapedStr+" escapr html "+StringEscapeUtils.escapeHtml(xmlStr));
		System.out.println("Escaping escaped string \t"+StringEscapeUtils.unescapeXml(escapedStr));
	}
}
