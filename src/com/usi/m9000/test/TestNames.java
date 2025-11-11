package com.usi.m9000.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TestNames {

	String sourceString = "ShravanthiSrRdhek";
	List<String> rules;
	List<String> startRules;
	List<String> endRules;
	String src1 = "abc";
	String src2 = "def";
	long permutations = 1;
	List<List<String>> names;
	int totalNames = 0;
	int nameLength = 0;
	/**
	 * 
	 */
	/**
	 * 
	 */
	public TestNames()
	{
		rules = new ArrayList<String>();
		rules.add("z");
		rules.add("x");
		rules.add("f");
		rules.add("q");
		
		rules.add("ae");
		rules.add("sb");
		rules.add("sc");
		rules.add("sd");
		rules.add("sg");
		rules.add("sj");
		rules.add("sm");
		rules.add("sn");
		rules.add("sp");
		rules.add("ss");
		rules.add("sv");
		rules.add("rb");
		rules.add("rc");
		rules.add("rd");
		rules.add("rg");
		rules.add("rh");
		rules.add("rk");
		rules.add("rl");
		rules.add("rm");
		rules.add("rn");
		rules.add("rp");
		rules.add("rr");
		rules.add("rt");
		rules.add("rv");
		rules.add("rw");
		rules.add("rz");
		rules.add("ao");
		rules.add("bb");
		rules.add("bc");
		rules.add("bd");
		rules.add("bg");
		rules.add("bj");
		rules.add("bk");
		rules.add("bl");
		rules.add("bm");
		rules.add("bn");
		rules.add("bp");
		rules.add("bs");
		rules.add("bt");
		rules.add("bu");
		rules.add("bv");
		rules.add("bw");
		rules.add("cb");
		rules.add("cc");
		rules.add("cd");
		rules.add("cg");
		rules.add("cj");
		rules.add("ck");
		rules.add("cm");
		rules.add("cn");
		rules.add("cp");
		rules.add("cr");
		rules.add("cs");
		rules.add("ct");
		rules.add("cu");
		rules.add("cv");
		rules.add("cw");
		rules.add("db");
		rules.add("dc");
		rules.add("dd");
		rules.add("dg");
		rules.add("dj");
		rules.add("dk");
		rules.add("dl");
		rules.add("dm");
		rules.add("dn");
		rules.add("dp");
		rules.add("ds");
		rules.add("dt");
		rules.add("dv");
		rules.add("eb");
		rules.add("ec");
		rules.add("ed");
		rules.add("eg");
		rules.add("eo");
		rules.add("eu");
		rules.add("ew");
		rules.add("gb");
		rules.add("gc");
		rules.add("gd");
		rules.add("gg");
		rules.add("gj");
		rules.add("gk");
		rules.add("gl");
		rules.add("gm");
		rules.add("gn");
		rules.add("gp");
		rules.add("gs");
		rules.add("gt");
		rules.add("gv");
		rules.add("gw");
		rules.add("gy");
		rules.add("hb");
		rules.add("hc");
		rules.add("hd");
		rules.add("hg");
		rules.add("hh");
		rules.add("hj");
		rules.add("hk");
		rules.add("hl");
		rules.add("hm");
		rules.add("hn");
		rules.add("hp");
		rules.add("hs");
		rules.add("ht");
		rules.add("hv");
		rules.add("hw");
		rules.add("hy");
		rules.add("ib");
		rules.add("ic");
		rules.add("ie");
		rules.add("ig");
		rules.add("ih");
		rules.add("ii");
		rules.add("io");
		rules.add("ir");
		rules.add("iu");
		rules.add("iw");
		rules.add("jb");
		rules.add("jc");
		rules.add("jd");
		rules.add("jg");
		rules.add("jj");
		rules.add("jk");
		rules.add("jl");
		rules.add("jm");
		rules.add("jn");
		rules.add("jp");
		rules.add("jr");
		rules.add("js");
		rules.add("jt");
		rules.add("jv");
		rules.add("jw");
		rules.add("kb");
		rules.add("kc");
		rules.add("kd");
		rules.add("kg");
		rules.add("kh");
		rules.add("kj");
		rules.add("kl");
		rules.add("km");
		rules.add("kn");
		rules.add("kp");
		rules.add("ks");
		rules.add("kv");
		rules.add("kw");
		rules.add("lb");
		rules.add("lc");
		rules.add("ld");
		rules.add("lg");
		rules.add("lh");
		rules.add("li");
		rules.add("lj");
		rules.add("lk");
		rules.add("ll");
		rules.add("lm");
		rules.add("ln");
		rules.add("lp");
		rules.add("lr");
		rules.add("ls");
		rules.add("lt");
		rules.add("lu");
		rules.add("lv");
		rules.add("lw");
		rules.add("ly");
		rules.add("mb");
		rules.add("md");
		rules.add("mg");
		rules.add("mh");
		rules.add("mj");
		rules.add("mk");
		rules.add("ml");
		rules.add("mm");
		rules.add("mn");
		rules.add("mp");
		rules.add("ms");
		rules.add("mt");
		rules.add("mv");
		rules.add("mw");
		rules.add("nb");
		rules.add("ng");
		rules.add("nh");
		rules.add("nk");
		rules.add("nl");
		rules.add("nm");
		rules.add("nn");
		rules.add("np");
		rules.add("nr");
		rules.add("ns");
		rules.add("nt");
		rules.add("nv");
		rules.add("nw");
		rules.add("oi");
		rules.add("ol");
		rules.add("pb");
		rules.add("pc");
		rules.add("pd");
		rules.add("pg");
		rules.add("pj");
		rules.add("pk");
		rules.add("pl");
		rules.add("pm");
		rules.add("pn");
		rules.add("pp");
		rules.add("ps");
		rules.add("pt");
		rules.add("pv");
		rules.add("pw");
		rules.add("py");
		rules.add("rn");
		rules.add("sn");
		rules.add("sv");
		rules.add("tb");
		rules.add("tc");
		rules.add("td");
		rules.add("tg");
		rules.add("tj");
		rules.add("tk");
		rules.add("tl");
		rules.add("tm");
		rules.add("tn");
		rules.add("tp");
		rules.add("ts");
		rules.add("tv");
		rules.add("tw");
		rules.add("ua");
		rules.add("ue");
		rules.add("ul");
		rules.add("uo");
		rules.add("uu");
		rules.add("uw");
		rules.add("uy");
		rules.add("vb");
		rules.add("vc");
		rules.add("vd");
		rules.add("vg");
		rules.add("vh");
		rules.add("vj");
		rules.add("vk");
		rules.add("vl");
		rules.add("vm");
		rules.add("vn");
		rules.add("vp");
		rules.add("vr");
		rules.add("vs");
		rules.add("vt");
		rules.add("vv");
		rules.add("vw");
		rules.add("wb");
		rules.add("wc");
		rules.add("wd");
		rules.add("wg");
		rules.add("wj");
		rules.add("wk");
		rules.add("wl");
		rules.add("wm");
		rules.add("wn");
		rules.add("wp");
		rules.add("ws");
		rules.add("wt");
		rules.add("wv");
		rules.add("ww");
		rules.add("yb");
		rules.add("yc");
		rules.add("yd");
		rules.add("yg");
		rules.add("yh");
		rules.add("yi");
		rules.add("yj");
		rules.add("ym");
		rules.add("yn");
		rules.add("yp");
		rules.add("yr");
		rules.add("ys");
		rules.add("yt");
		rules.add("yv");
		rules.add("yw");
		rules.add("yy");
		rules.add("aid");
		rules.add("ass");
		rules.add("ei");
		rules.add("trsh");
		rules.add("trs");
		rules.add("thah");
		rules.add("hrs");
		rules.add("rst");
		rules.add("ahr");
		rules.add("ahah");
		rules.add("aarS");
		rules.add("tiaa");
		rules.add("tiaah");
		rules.add("haahi");
		rules.add("trash");
		rules.add("hahi");
		rules.add("trs");
		rules.add("thrs");
		rules.add("vaai");
		rules.add("tair");
		rules.add("thair");
		rules.add("harass");
		rules.add("hasth");
		rules.add("hain");
		rules.add("traai");
		rules.add("haha");
		
		startRules = new ArrayList<String>();
		startRules.add("e");
		startRules.add("d");
		startRules.add("h");
		startRules.add("i");
		startRules.add("o");
		startRules.add("t");
		startRules.add("u");
		startRules.add("w");
		startRules.add("y");
		startRules.add("ae");
		startRules.add("rt");
		startRules.add("nd");
		startRules.add("rv");
		startRules.add("rs");
		startRules.add("sk");
		startRules.add("rit");
		startRules.add("nv");
		startRules.add("nit");
		startRules.add("hi");
		startRules.add("hr");
		startRules.add("st");
		startRules.add("kt");
		startRules.add("kr");
		startRules.add("kk");
		startRules.add("as");
		startRules.add("are");
		startRules.add("adr");
		startRules.add("ak");
		startRules.add("ai");
		startRules.add("sia");
		startRules.add("sra");
		startRules.add("thr");
		startRules.add("kia");
		startRules.add("hasth");
		startRules.add("hava");
		startRules.add("this");
		startRules.add("nathr");
		startRules.add("nashr");
		startRules.add("nath");
		startRules.add("hav");
		startRules.add("vath");
		startRules.add("han");
		startRules.add("nasth");
		startRules.add("naa");
		startRules.add("nia");
		startRules.add("naha");
		startRules.add("hi");
		startRules.add("hai");
		startRules.add("nai");
		startRules.add("str");
		
		
		endRules = new ArrayList<String>();
		endRules.add("ah");
		endRules.add("br");
		endRules.add("eh");
		endRules.add("cr");
		endRules.add("dr");
		endRules.add("hr");
		endRules.add("sr");
		endRules.add("sk");
		endRules.add("kt");
		endRules.add("kr");
		endRules.add("tr");
		endRules.add("vr");
		endRules.add("ik");
		endRules.add("shr");
		endRules.add("sth");
		endRules.add("rah");
		endRules.add("hin");
		endRules.add("vah");
		endRules.add("tria");
		endRules.add("tra");
		endRules.add("thra");
		endRules.add("tara");
		endRules.add("shia");
		endRules.add("shit");
		endRules.add("shra");
		endRules.add("kad");
		endRules.add("y");
		endRules.add("i");
		endRules.add("ia");
		endRules.add("ina");
		endRules.add("ta");
		endRules.add("tha");
		endRules.add("aha");
		endRules.add("sha");
		endRules.add("kth");
		endRules.add("krs");

	}
	public void permuteNames(int nameLength)
	{
		totalNames = 0;
		this.nameLength = nameLength;
		int length = sourceString.length();
		names = new ArrayList<List<String>>(length);
		for (int i = 0; i < length; i++) {
			names.add(new ArrayList<String>());
		}
		List<String> lstNames = new ArrayList<String>();
		permutations = 1;
		int nr = 1;
		int x = 0;
		System.out.println("length.."+length);
		for (int i=length;i>0;i--)
		{
			permutations*=i;
		}
		System.out.println("permutations..."+permutations);
		length = length-nameLength;
		for (int i=length;i>0;i--)
		{
			nr*=i;
		}
		permutations = permutations/nr;
		System.out.println("Total possiblities.."+permutations);
		StringBuffer strBuff = null;
		List<Integer> lstRand = null;
		int formatLine = 0;
		int permutationCnt = 0;
		List<List<Integer>> lstRandomDups = new ArrayList<List<Integer>>();
		while(true)
		{
			lstRand = new ArrayList<Integer>();
			strBuff = new StringBuffer();
			Random rand = new Random();
			for (int i = 0; strBuff.length() < nameLength; i++) {
//				x = (int)(Math.random()*sourceString.length());
				x = rand.nextInt((sourceString.length())) ;
				if (!lstRand.contains(x))
				{
					lstRand.add(x);
//					System.out.println("X value.."+x);
					strBuff.append(sourceString.charAt(x));					
				}
			}
//			System.out.println(lstRandomDups);
			if (!isEligible(strBuff.toString()))
			{
				permutationCnt++;
				continue;
			}
			if (!lstRandomDups.contains(lstRand))
			{
//				System.out.println("Lst added"+lstRand);
				lstRandomDups.add(lstRand);
			}
			else if (lstNames.size() < permutations && permutationCnt < permutations)
			{
//				System.out.println("Skipped.."+lstRand);
				continue;
			}
			if (!lstNames.contains(strBuff.toString()))
			{
//				if ((strBuff.toString().endsWith("ra") && (strBuff.toString().startsWith("sr")||(strBuff.toString().startsWith("")))))
//				if (strBuff.toString().startsWith("a"))
				{
//					System.out.print(strBuff+" , ");
					formatLine++;
					if (formatLine % 10 == 0)
					{
//						System.out.println();
					}
					permutationCnt++;
				}
//				System.out.println("Permutations.."+lstNames.size());
				System.out.println(strBuff.toString());
				for (int i = 0; i < sourceString.length(); i++) {
					if (strBuff.toString().startsWith(""+sourceString.charAt(i)))
					{
						names.get(i).add(strBuff.toString());
						break;
					}
					
				}
				lstNames.add(strBuff.toString());
				totalNames++;
			}
			else
			{
				permutationCnt++;				
			}
			if (lstNames.size() >= permutations || permutationCnt > permutations)
			{
				break;
			}
//			if (permutationCnt < 100000)
//			{
//				break;
//			}
		}
	}
	private boolean isEligible(String name)
	{
		boolean booFlag = true;
		for (Iterator<String> iterator = rules.iterator(); iterator.hasNext();) {
			String rule = iterator.next();
			if (name.toLowerCase().indexOf(rule) != -1)
			{
				booFlag = false;
				break;
			}
		}
		if (booFlag)
		{
			for (Iterator<String> iterator = startRules.iterator(); iterator.hasNext();) {
				String startRule = iterator.next();
				if (name.toLowerCase().startsWith(startRule))
				{
					booFlag = false;
					break;
				}
			}
		}
		if (booFlag)
		{
			for (Iterator<String> iterator = endRules.iterator(); iterator.hasNext();) {
				String endRule = iterator.next();
				if (name.toLowerCase().endsWith(endRule))
				{
					booFlag = false;
					break;
				}
			}
		}
		return booFlag;
	}
	private void printNames()
	{
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(new File ("c:/personal/Radhe/names-"+sourceString+"-"+nameLength+".txt")));
			int format = 0;
			System.out.println("Total names "+totalNames);
			for (int i = 0; i < names.size(); i++) {
//				System.out.println("********************************************************");
				bw.write("********************************************************");
				bw.newLine();
//				System.out.println("\t\t\t Names starting with: "+sourceString.charAt(i));
				bw.write("\t\t\t Names starting with: "+sourceString.charAt(i));
				bw.newLine();
//				System.out.println("********************************************************");
				bw.write("********************************************************");
				bw.newLine();
				format = 0;
				Collections.sort(names.get(i));
				for (int j = 0; j < names.get(i).size(); j++) {
//					if (isEligible(names.get(i).get(j)))
//					{
						format++;
//						System.out.print(names.get(i).get(j)+" , ");
						bw.write(names.get(i).get(j)+" , ");
						if (format % 15 == 0)
						{
//							System.out.println();
							bw.newLine();
						}
//					}
//					else
//					{
//						System.out.print(names.get(i).get(j)+" , ");
//						if (j % 15 == 0)
//						{
//							System.out.println();
//						}
//
//					}
				}
				bw.newLine();
				bw.newLine();
//				System.out.println();
//				System.out.println();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		// TODO Auto-generated method stub
		TestNames test = new TestNames();
		test.permuteNames(6);
		System.out.println("About to print names...");
		test.printNames();
		File file = new File("c:/");
//		double totalSpace = file.getTotalSpace()/ Math.pow(2, 30);
//		double usedSpace = (file.getTotalSpace()-file.getFreeSpace())/ Math.pow(2, 30);
//		System.out.println("Total space "+file.getTotalSpace()+" bytes which is " +totalSpace +" in gb");
//		System.out.println("Used space "+usedSpace);
////		System.out.println("Used space "+file.getUsedSpace());
//		System.out.println("% used "+(usedSpace/totalSpace)*100);
		
//		float totalSpace = Math.round((float)((file.getTotalSpace()/ Math.pow(2, 30))+0.5));
////		logger.debug("DISK: Total space "+totalSpace);
////		logger.debug("DISK: Free space "+file.getFreeSpace());
//		float usedSpace = Math.round((float)(((file.getTotalSpace()-file.getFreeSpace())/ Math.pow(2, 30)+0.5)));
////		logger.debug("DISK: Used space "+usedSpace);
//		int percentage = Math.round(((usedSpace / totalSpace)*100)+0.5f);
//		int diskWarningLimit = (int) Math.floor((90.0 - ((5.0/100)*90.0f)));
//		int altWarn = (int) Math.floor(((90.0/100)*6.0));
//		System.out.println("warning limit "+diskWarningLimit+" alt limit "+altWarn);
//		
//		String strFilname = "//195.1.1.128/M9k/Data/1-College Station/1_120329,080333466,-5t,College Station,USI_M9000,USI.dat";
//		String configFile = strFilname.substring(0, strFilname.lastIndexOf(".")+1)+"cfg";
//		System.out.println("config file "+configFile);
//		File cfile = new File(configFile);
//		System.out.println("is exists? "+cfile.exists());
		
//		 Pattern pattern = Pattern.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");   
//		  
//	        String ipAddress = "192.168.1.1";  
//	        Matcher matcher = pattern.matcher(ipAddress);  
////	        System.out.println(matcher.find() && matcher.group().equals(ipAddress));  
//	        System.out.println(matcher.matches());
//		
//	        System.out.println(Arrays.asList(TimeZone.getDefault()));
//        		System.out.println("OS name.. "+ System.getProperty("os.name"));
//        		
//        		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        		Calendar cal = Calendar.getInstance();
//        		System.out.println(dateFormat.format(cal.getTime()));
//        		
////        		System.out.println("1234 md5 text "+test.getMD5Text("1234"));
//        		Map<String,String> mapTst = new HashMap<String, String>();
//        		mapTst.put("DFR1","1");
//        		mapTst.put("DFR3","3");
//        		mapTst.put("DFR5","5");
//        		mapTst.put("DFR2","2");
//        		mapTst.put("DFR4","4");
//        		mapTst.put("DFR10","10");
//        		TreeSet<String> keys = new TreeSet<String>(mapTst.keySet());
//        		Iterator<String> mapXmlDfrIterator =keys.iterator();
//        		for (; mapXmlDfrIterator.hasNext();) {
//        			System.out.println("key "+mapXmlDfrIterator.next());
//        		}
//        		String dfrName = "DFR2"; 
//        		int dfrId = Integer.parseInt(dfrName.substring("DFR".length()));
//        		System.out.println("dfr id "+dfrId);
        		
//		double lo = 0.0;
//		List<String> lstRelayName = new ArrayList<String>(1);
//		lstRelayName.add("RELAY_1");
//		System.out.println("lst of relay name "+lstRelayName);
//		System.out.println("Online "+LedName.ONLINE.name().equalsIgnoreCase("Online"));
//		System.out.println("Is zero? "+(lo == 0));
//		M9kLED.init();
//		Set<LedName> set=M9kLED.mapLedToRelay.keySet();
//		for (Iterator iterator = set.iterator(); iterator.hasNext();) {
//			LedName ledName = (LedName) iterator.next();
//			System.out.println("Led Name "+ledName);
//			lstRelayName = (List<String>)(List<?>)M9kLED.mapLedToRelay.get(ledName);
//			System.out.println("lst of relay name "+lstRelayName);
//		}
//		long start = System.nanoTime();
//		System.out.println("start "+start);
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		long end = System.nanoTime();
//		System.out.println("end "+end);
//		System.out.println("Time elapsed "+(end - start)/1000000);
		
//		CInt tst = new CInt();
//		tst.printA();
		
//		int value = 125;
//		System.out.println(" set relay 1 to green "+(value |= (1 << (7))));
        		
        		
//        		Double sampleTime = (((28800.0 - 1)/960) * 1000000);
//        		System.out.println("Sample time "+sampleTime);
//        		System.out.println("Sample time "+sampleTime.longValue());
//        		
//        		String str = "ABC DEF GHI"+M9kXMLConstants.XML_PHASOR_VALUE_INPUT;
//        		System.out.println(str.replace(M9kXMLConstants.XML_PHASOR_VALUE_INPUT, M9kXMLConstants.XML_VALUE_INPUT));
        		
//        		StringBuffer strEvents = new StringBuffer();
//        		int cnt = 0;
//        		StringBuffer[] strArr = new StringBuffer[10];
//        		while (cnt < 10)
//        		{
//        			strEvents.append("cnt - "+cnt);
//        			strArr[cnt] = new StringBuffer(strEvents.toString());
//        			strEvents.setLength(0);
//        			cnt++;
//        		}
//        		cnt = 0;
//        		while (cnt < 10)
//        		{
//        			System.out.println(cnt+" - "+strArr[cnt]);
//        			cnt++;
//        		}
        		
//        		System.out.println("DFR Time "+ M9kStationUtil.getDfrTime());
//        		System.out.println("System time "+System.currentTimeMillis());
//        		System.out.println("System time in seconds "+(System.currentTimeMillis()/1000));
//        		System.out.println("Calendar "+Calendar.getInstance().getTimeInMillis());
//        		System.out.println("Calendar UTC"+Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
//        		
//        		String name = "dfr11";
//        		System.out.println(name.substring(3));
//		ComtradeDataDTO comtradeDataDTO;
//		List<ComtradeDataDTO> lstComtradeDataDtos=new ArrayList<ComtradeDataDTO>(); 
//        		for (int i = 0; i < 100000; i++) {
//        			comtradeDataDTO = new ComtradeDataDTO();
//        			comtradeDataDTO.setFaultId(i);
//    				comtradeDataDTO.setStationId(i);
//    				comtradeDataDTO.setTsTrigger(System.currentTimeMillis());
//        			comtradeDataDTO.setEvents(new StringBuffer("ABC "+i));
//        			comtradeDataDTO.setLength(i*1234.99);
//    				comtradeDataDTO.setPreFault(100);
//    				comtradeDataDTO.setPostFault(100);
//    				comtradeDataDTO.setFileName("ABCDEF");
//    				comtradeDataDTO.setActiveEvents("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
//    				lstComtradeDataDtos.add(comtradeDataDTO);
//				}
//        		System.out.println("Array size "+lstComtradeDataDtos.size());
		
//		String date = sdf.format(new java.util.Date (comtradeDataDTO.getTsTrigger()/1000));
//		bw.write("Date="+date.substring(0,date.indexOf(",")));
		System.out.println("Java Version "+System.getProperty("java.version"));
	}
	public String getMD5Text(String password)
	{
		 /* Create MD5 from string input */
	    MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				m = MessageDigest.getInstance("SHA1");
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	    m.reset();
	    m.update(password.getBytes());
	    byte[] digest = m.digest();
	    BigInteger bigInt = new BigInteger(1,digest);
	    String hashtext = bigInt.toString(16);
	    while(hashtext.length() < 32 ){
	      hashtext = "0"+hashtext;
	    }
	    /* Create MD5 from string input */
		return hashtext;
	}
	
}

class A
{
	public void printA()
	{
		System.out.println("In A"); 
	}
}

class B
{
	public void printB() {
		System.out.println("In B"); 
		
	}
}
class CInt extends C
{
	
}

class C extends A 
{

	
}
