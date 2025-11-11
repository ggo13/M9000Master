package com.usi.m9000.test;

import com.usi.m9000.util.M9kConstants;

public class TestParseComtradeConf {

	StringBuffer strAnalogsInfo = new StringBuffer(100);
	public TestParseComtradeConf() {
		strAnalogsInfo.append("1,1,N/A,Analog1,V,0.00625019,0,0,-32768,32767,1,1,S");
		strAnalogsInfo.append(M9kConstants.NEWLINE);
		strAnalogsInfo.append("2,2,N/A,Analog2,V,0.00625019,0,0,-32768,32767,1,1,S");
		strAnalogsInfo.append(M9kConstants.NEWLINE);
		strAnalogsInfo.append("3,3,N/A,Analog3,V,0.00625019,0,0,-32768,32767,1,1,S");
		strAnalogsInfo.append(M9kConstants.NEWLINE);
		strAnalogsInfo.append("4,4,N/A,Analog4,V,0.00625019,0,0,-32768,32767,1,1,S");
		strAnalogsInfo.append(M9kConstants.NEWLINE);
		strAnalogsInfo.append("5,5,N/A,Analog5,V,0.00625019,0,0,-32768,32767,1,1,S");
		strAnalogsInfo.append(M9kConstants.NEWLINE);
		strAnalogsInfo.append("6,6,N/A,Analog6,V,0.00625019,0,0,-32768,32767,1,1,S");
		strAnalogsInfo.append(M9kConstants.NEWLINE);
		strAnalogsInfo.append("7,7,N/A,Analog7,V,0.00625019,0,0,-32768,32767,1,1,S");
		strAnalogsInfo.append(M9kConstants.NEWLINE);
		strAnalogsInfo.append("8,8,N/A,Analog8,V,0.00625019,0,0,-32768,32767,1,1,S");
		strAnalogsInfo.append(M9kConstants.NEWLINE);
		strAnalogsInfo.append("9,9,N/A,Analog9,V,0.00625019,0,0,-32768,32767,1,1,S");
		strAnalogsInfo.append(M9kConstants.NEWLINE);
		strAnalogsInfo.append("10,10,N/A,Analog10,V,0.00625019,0,0,-32768,32767,1,1,S");
	}

	public StringBuffer copyColumns(StringBuffer analogInfo, int colSource, int colDest)
	{
		StringBuffer newStrAnalogs = new StringBuffer(100);
		String cols[];
		
		String[] lines = analogInfo.toString().split(M9kConstants.NEWLINE);
		for (int i = 0; i < lines.length; i++) {
			cols = lines[i].split(",");
			for (int j = 0; j < cols.length; j++) {
				if (j != (colDest - 1))
				{
					if (j <(cols.length)-1)
					{
						newStrAnalogs.append(cols[j]+",");
					}
					else
					{
						newStrAnalogs.append(cols[j]);
						newStrAnalogs.append(M9kConstants.NEWLINE);
					}
				}
				else
				{
					if (j <(cols.length)-1)
					{
						newStrAnalogs.append(cols[colSource-1]+",");
					}
					else
					{
						newStrAnalogs.append(cols[colSource-1]);
						newStrAnalogs.append(M9kConstants.NEWLINE);
					}
				}
			}
		}
		return newStrAnalogs;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestParseComtradeConf test = new TestParseComtradeConf();
		System.out.println("Orinal str \n"+test.strAnalogsInfo.toString());
		System.out.println("After replace\n"+test.copyColumns(test.strAnalogsInfo, 4, 2));
		
	}

}
