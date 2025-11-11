package com.usi.m9000.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.triggers.LineGroupsAlgorithm;

public class TestMergeComtradeFiles {

	String fileName;
	public TestMergeComtradeFiles(String fileName)
	{
		this.fileName = fileName;
	}
	public static void main(String[] args)
	{
//		TestMergeComtradeFiles tst = new TestMergeComtradeFiles("C:/USI 2002/Comtrade Data Files/040715,172956109,R83F0235,-5s,KETTLE CREEK,USI_2002,GTC");
//		tst.createMergedDatFile();
		TestMergeComtradeFiles.parse2002LineGroupFile("C:/USI 2002/Build Output/R83Lines.inf");
	}
	public void createMergedDatFile()
	{
		int i=0;
		String extn = ".d0"+i;
		File datFile = new File(fileName+".dat");
		File readFile = new File(fileName+extn);
		RandomAccessFile raf;
		BufferedInputStream dis;
		try {
			 raf = new RandomAccessFile(datFile, "rw");
		while (readFile.exists())
		{
			dis = new BufferedInputStream(new FileInputStream(readFile));
			byte[] buf = new byte[3000];
			int read = 0;
			raf.seek(datFile.length());
			while ((read = dis.read(buf)) > 0) {
				raf.write(buf,0, read);
	        }
			dis.close();
			extn = ".d0"+ ++i;
			System.out.println("File to read "+(fileName+extn));
			readFile = new File(fileName+extn);
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<LineGroupsAlgorithm> parse2002LineGroupFile(String lineGroupFileName)
	{
		HierarchicalINIConfiguration iniConf = null;
		List<LineGroupsAlgorithm> lstLineGroups = new ArrayList<LineGroupsAlgorithm>();
		
//		BufferedInputStream bis;
		
		try {
			iniConf = new HierarchicalINIConfiguration(lineGroupFileName);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<String> dfrs = iniConf.getSections();
		LineGroupsAlgorithm lineGroupsAlgorithm;
		for (String dfr : dfrs) {
			System.out.println("[ "+dfr+" ]");
			if (dfr.equalsIgnoreCase("General Parameters"))
			{
				continue;
			}
			lineGroupsAlgorithm = new LineGroupsAlgorithm();
			lineGroupsAlgorithm.setName(iniConf.getString(dfr + "LineName"));
			lineGroupsAlgorithm.setId(iniConf.getInt(dfr+"LineID"));
			lineGroupsAlgorithm.setLattitude(iniConf.getDouble(dfr+"DfrLon"));
			lineGroupsAlgorithm.setLongitude(iniConf.getDouble(dfr+"DfrLat"));
			lineGroupsAlgorithm.setDecisionLogic(iniConf.getString(dfr + "LineFaultLogic"));
			lineGroupsAlgorithm.setEnableAutoCalc("Yes");
			
		}
//			bis = new BufferedInputStream(new FileInputStream(new File(lineGroupFileName)));
		
		return lstLineGroups;
	}
}
