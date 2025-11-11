package com.usi.m9000.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.usi.m9000.station.util.M9kBackupUtil;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationUtil;


public class TestFileNames {

	public void createFile(){
		String fileName = "C:/M9kConfig/Comtrade/100806,063526117,Central Station,Utility Systems M9000 SN 1.cfg" ;
		fileName="/4-Station Name/R04F1_130418,12141000,-5t,Station Name,usi_m9000,USI.cfg";
		fileName="C:/data/m9k/faults/R01-Sandy Bog/R01F1338_30_04_2013,114834.116,-5t,Sandy Bog,USI-M9000,USI.dat";
//		
		System.out.println("Filename: "+fileName);
		File file = new File(fileName);
		System.out.println("file exists? "+file.exists());
		System.out.println("Last index "+fileName.lastIndexOf("/"));
		System.out.println("File name stripped prefix "+fileName.substring(fileName.lastIndexOf("/")+1));
		String destZipfilePath = M9kStationUtil.getDataDir()+fileName.substring(0, fileName.lastIndexOf("/",1)+1)+"compressed"+File.separator+"";
		System.out.println("Zip File path "+destZipfilePath);
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		File destDir = new File(destZipfilePath);
//		if (!destDir.exists())
//		{
//			destDir.mkdir();
//		}
//		File zipFile = new File(destDir,(fileName+".zip"));
//		System.out.println("zip file path "+zipFile);
		
//		System.out.println("file path: "+file.getPath().replace("\\", "/"));
//		System.out.println("File name "+file.getName());
//		String strComma = "a,b,c";
//		System.out.println(strComma.replaceAll(",", M9kConstants.NEWLINE));
//		System.out.println("new line "+M9kConstants.NEWLINE);
//		try {
//			file.createNewFile();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			System.out.println("File Name: "+fileName);
//			e.printStackTrace();
//		}
//		System.out.println("Exists ? " +file.exists());
//		String dat = "1,0,-7,-10,-9,-12,9,-8,-7339,7,3,-3,0,3,6,0,9,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1";
//		int startIndex = findNthIndexOf(dat,",",18);
//		int analogIndex = 18;
//		String datArr[] = dat.split(",");
//		System.out.println("Actual dat "+dat);
//		System.out.println("Start index "+startIndex);
//		System.out.println("analog index "+analogIndex);
//		System.out.println(dat.substring(4, startIndex));
//		for (int i = 2; i < analogIndex; i++) {
//			System.out.print(datArr[i]+",");
//		}
	}
	
	public static int findNthIndexOf (String str, String needle, int occurence)
    throws IndexOutOfBoundsException {
		int index = -1;
		Pattern p = Pattern.compile(needle, Pattern.MULTILINE);
		Matcher m = p.matcher(str);
		while(m.find()) {
		if (--occurence == 0) {
		    index = m.start();
		    break;
		}
		}
		if (index < 0) throw new IndexOutOfBoundsException();
		return index;
}
	// R[0-9]+F[0-9]+
	
	public void checkForFaultID()
	{
		String oldFileName="/4-Station Name/R04F971_130418,12141000,-5t,Station Name,usi_m9000,USI.cfg";
		String newFileName = "180117,143032116,-6t,St Lawrence 25_26,USI_M9000,USI,R07F23456.inf";
		int index = -1;
		String strPatt = "R[0-9]+F[0-9]+";
		String strSubPatt = "F[0-9]+";
		String matchedString;
		Pattern searchPattern = Pattern.compile(strPatt);
		Matcher m = searchPattern.matcher(newFileName);
		if(m.find()) {
			System.out.println("MAtch found. Start "+m.start() +" end "+m.end()+" toString "+m.group());
			matchedString = m.group();
			System.out.println("Matched String "+matchedString);
			searchPattern = Pattern.compile(strSubPatt);
			m = searchPattern.matcher(matchedString);
			m.find();
			System.out.println("Fault id "+m.group().substring(1));
				
		}
		else
		{
			System.out.println("No Match Found :(");
		}
	}
	public void getFileNameForFaultID(int faultId)
	{
		String oldFileName="/4-Station Name/R04F971_130418,12141000,-5t,Station Name,usi_m9000,USI.cfg";
		String newFileName = "180117,143032116,-6t,St Lawrence 25_26,USI_M9000,USI,R07F23456.inf";
		String arrFileNames[] = new String[12];
		arrFileNames[0] = "180117,143032116,-6t,St Lawrence 25_26,USI_M9000,USI,R07F23456.inf";
		arrFileNames[1] = "180117,143032116,-6t,St Lawrence 25_26,USI_M9000,USI,R07F23456.cfg";
		arrFileNames[2] = "180117,143032116,-6t,St Lawrence 25_26,USI_M9000,USI,R07F23456.dat";
		arrFileNames[3] = "180117,143032116,-6t,St Lawrence 25_26,USI_M9000,USI,R07F23457.inf";
		arrFileNames[4] = "180117,143032116,-6t,St Lawrence 25_26,USI_M9000,USI,R07F23457.cfg";
		arrFileNames[5] = "180117,143032116,-6t,St Lawrence 25_26,USI_M9000,USI,R07F23457.dat";
		arrFileNames[6] = "180117,143032116,-6t,St Lawrence 25_26,USI_M9000,USI,R07F234.inf";
		arrFileNames[7] = "180117,143032116,-6t,St Lawrence 25_26,USI_M9000,USI,R07F234.cfg";
		arrFileNames[8] = "180117,143032116,-6t,St Lawrence 25_26,USI_M9000,USI,R07F234.dat";
		arrFileNames[9] = "R04F971_130418,12141000,-5t,Station Name,usi_m9000,USI.cfg";
		arrFileNames[10] = "R04F971_130418,12141000,-5t,Station Name,usi_m9000,USI.dat";
		arrFileNames[11] = "R04F971_130418,12141000,-5t,Station Name,usi_m9000,USI.inf";
		String strPatt = "R[0-9]+F"+faultId+"[._]";
		System.out.println("PAttern "+strPatt);
		Pattern searchPattern = Pattern.compile(strPatt);
		Matcher m;
		System.out.println("Array length "+arrFileNames.length);
		for (int i = 0; i < arrFileNames.length; i++) {
			m = searchPattern.matcher(arrFileNames[i]);
			if(m.find()) {
				System.out.println("File Name found "+arrFileNames[i]);	
			}
			else
			{
				System.out.println("No Match Found :(");
			}
			
		}
	}
	
	public void getLatestDateFromFilename()
	{
		File srcFolder = new File("C:\\data\\m9k\\auto-exports\\");
		if (srcFolder.exists())
		{
			File[] fileList = srcFolder.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.isDirectory();
				}
			});
			System.out.println("File list "+fileList.length);
			Arrays.sort(fileList);
			for (int i = 0; i < fileList.length; i++) {
				System.out.println("file name "+fileList[i].getName());
			}
			if (fileList.length > 0) 
			{
				String latestFolderName = fileList[fileList.length-1].getName();
				System.out.println("\n\tLatest folder name "+latestFolderName);
				File[] srcFiles = fileList[fileList.length-1].listFiles();
				String latestFileName = srcFiles[srcFiles.length-1].getName();
				System.out.println("\n\tLatest File name "+latestFileName);
	//			String dateFromFile = latestFileName.substring(0, latestFileName.indexOf(",",latestFileName.indexOf(",")+1));
				String dateFromFile = latestFileName.substring(0, latestFileName.indexOf(","));
				System.out.println("Time portion of file name "+dateFromFile);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
				try {
					Date latestTime = sdf.parse(dateFromFile);
					System.out.println("Formatted Date "+latestTime);
					Date availEndTime = new Date(latestTime.getTime()+60000);
					System.out.println("Available end time "+availEndTime);
					System.out.println("time in milli last time "+availEndTime.getTime());
					System.out.println("converted "+M9kBackupUtil.getConvertedEPOCHTime(availEndTime.getTime()));
					Date nowDate = new Date();
					System.out.println(nowDate.getTime() - availEndTime.getTime());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			try
			{
				System.out.println("getCurrentDateFolderForAutoExport() "+M9kBackupUtil.getCurrentDateFolderForAutoExport());
				File[] exportDateDirs = M9kBackupUtil.getAutoExportDateFolders("/data/m9k/auto-exports");
				System.out.println("Exported Dirs "+exportDateDirs);
				System.out.println(M9kBackupUtil.getConvertedEPOCHTime(1560257999000L));
				System.out.println((((1*60) * 1000000)-104));
				System.out.println(M9kBackupUtil.getConvertedEPOCHTime(1560257999000L)  + (((1*60) * 1000000)));
			}
			catch (Exception e) {
				e.printStackTrace();
			}


		}
	}

	public static void main(String args[]) throws IOException
	{
		TestFileNames test = new TestFileNames();
		String oldFileName="/4-Station Name/R04F971_130418,12141000,-5t,Station Name,usi_m9000,USI.cfg";
		System.out.println("Second character in a string "+oldFileName.substring(1,2));
		test.getLatestDateFromFilename();
		String fileName = "R11-Comanche-Peak/continuous/20190911-215700,-4t,Comanche-Peak,USI_M9000,USI,R11CONT1,Osc";
		fileName = fileName.replaceAll("/", Matcher.quoteReplacement(File.separator));
		System.out.println("index of / "+fileName.indexOf("/")+" replaced file name "+fileName);
		System.out.println("File seperator "+File.separator+" last index "+(fileName.lastIndexOf(File.separator)+1));
		System.out.println(fileName.substring(0, fileName.lastIndexOf(File.separator)+1));
//		test.checkForFaultID();
//		test.getFileNameForFaultID(23456);
		String dataDir = "/data/m9k/faults";
		
		System.out.println("dir.. "+dataDir.substring(0, dataDir.lastIndexOf("/")));
		File datFile = new File(dataDir);
		datFile = new File(datFile.getParent()+File.separator+"config-files");
		
		System.out.println("datFile "+datFile.getPath());
		System.out.println("datFile parent "+datFile.getParent());
//		test.deleteFilesFromDirectory("C:/m9k/test-delete-folder");
		

			Path start = Paths.get("D:\\M9k\\Troubleshooting\\auto-exports\\");
			try (Stream<Path> stream = Files.walk(start, 2)) {
			    List<Path> collect = stream
			        .map(Path::getFileName)
			        .sorted()
//			        .filter(line -> line.indexOf(M9kStationConstants.MEASUREMENTS) != -1)
			        .filter(path -> path.getFileName().toString().indexOf(M9kStationConstants.MEASUREMENTS) != -1)
			        .collect(Collectors.toList());
			    
			    collect.forEach(System.out::println);
//			    System.out.println("last file "+collect.get(collect.size()-1).toString());
			    System.out.println("collection? "+collect);
			}
			
			
		
	}
	private  void deleteFilesFromDirectory(String dirPath)
	{
		File sourceDir = new File(dirPath);
		for(File file: sourceDir.listFiles()) 
		{
			System.out.println("Deleting "+file.getAbsolutePath());
			if (file.isDirectory())
			{
				deleteFilesFromDirectory(file.getAbsolutePath());
			}
			file.delete();
		}
	}
}
