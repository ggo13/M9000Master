/**
 * 
 */
package com.usi.m9000.station.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.config.AnalogInfo;
import com.usi.m9000.config.ChannelInfo;
import com.usi.m9000.config.DigitalInfo;
import com.usi.m9000.dto.AnalogChannelDTO;
import com.usi.m9000.dto.ComtradeDataDTO;
import com.usi.m9000.station.dto.ProcessDatDTO;
import com.usi.m9000.station.faultLocation.M9kLineLogicFilter;
import com.usi.m9000.triggers.LineGroupsAlgorithm;
import com.usi.m9000.util.M9kConstants;

/**
 * @author sramasamy
 *
 */
public class M9kStationComtradeUtil {

	int abnormalDigiChnl[];
	int totalAbnormalChannels = 0;
	List<ChannelInfo> analogInfoList;
	 List<ChannelInfo> digitalInfoList;
	 private static ResourceBundle bundle;
	 private static String dataDir = null;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationComtradeUtil.class);
	static
	{
		try
		{
			bundle = ResourceBundle.getBundle("M9K_COMTRADE");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.error("Unable to read cofiguration", e1);
		}
	}
	/**
	 * 
	 */
	public M9kStationComtradeUtil() {
		// TODO Auto-generated constructor stub
	}

	public static String getDataDir()
	{
		if (dataDir == null)
		{
			try
			{
				dataDir = bundle.getString("dataDir");
				if (dataDir != null)
				{
					if (!dataDir.endsWith(File.separator))
					{
						dataDir = dataDir+File.separator;
					}
				}
				else
				{
					logger.warn("dataDir from M9K_COMTRADE is null. Using default path /data/m9k/faults");
					dataDir = "/data/m9k/faults/";
				}
			}
			catch (Exception e)
			{
				logger.warn("There was trouble in reading dataDir from M9K_COMTRADE. Using default path /data/m9k/faults",e);
				dataDir = "/data/m9k/faults/";
			}
		}
		return dataDir;
	}
	
	/**
	 * Generic method to get value for attributes from M9k_COMTRADE propery file
	 * @param attribute
	 * @return
	 */
	public static String getComtradeProperty(String property)
	{
		String value = "";
		try
		{
			value = bundle.getString(property);
		}
		catch (Exception e) {
			logger.warn("Exception occured while reading "+property+" from M9K_COMTRADE property file");
		}
		return value;
	}

	
	public Map<String, DigitalInfo> getActiveEventsId(ProcessDatDTO mergedComtradeData) throws M9000Exception
	{
		parseComtradeDataForEvents(mergedComtradeData);
		Map<String, DigitalInfo> mapAbnormalEvents = new HashMap<String, DigitalInfo>();
		int[] sortedDigitalChnl = (int[])abnormalDigiChnl.clone();
		Arrays.sort(sortedDigitalChnl);

		Integer[] activeDigitalChannels = new Integer[totalAbnormalChannels];
		int indexToBeWritten = 0;
		for (int i = sortedDigitalChnl.length - 1; i >= 0; i--) {
			for (int j = 0; j < abnormalDigiChnl.length; j++) {
				if (sortedDigitalChnl[i] > 0 && sortedDigitalChnl[i] == abnormalDigiChnl[j])
				{
					activeDigitalChannels[indexToBeWritten] = j;
					abnormalDigiChnl[j] = 0;
					indexToBeWritten++;
					break;
				}						
			}
		}
		int nullIndex = Arrays.asList(activeDigitalChannels).indexOf(null);
		if (nullIndex >= 0)
		{
			activeDigitalChannels = Arrays.copyOfRange(activeDigitalChannels,0,nullIndex);			
		}
		DigitalInfo digitalInfo;
//		logger.info("Lst Digital Info  "+digitalInfoList);
//		logger.info("activeDigitalChannelse "+activeDigitalChannels);

		for (int j = activeDigitalChannels.length-1; j>=0 ;j--) {
			logger.debug("activeDigitalChannels activeDigitalChannels["+j+"] -> "+activeDigitalChannels[j]);
			logger.debug("digitalInfoList "+digitalInfoList.get(activeDigitalChannels[j]));
			digitalInfo = (DigitalInfo) digitalInfoList.get(activeDigitalChannels[j]);
//			if (events.length() > 0)
//			{
//				events.append(M9kConstants.NEWLINE+digitalInfo.getChnlId()+" - "+digitalInfo.getCircuitName());
//			}
//			else
//			{
//				events.append(digitalInfo.getChnlId()+" - "+digitalInfo.getCircuitName());
//			}
			mapAbnormalEvents.put(digitalInfo.getChnlId(), digitalInfo);
		}

//		logger.debug("Active events id.."+mapAbnormalEvents);
		return mapAbnormalEvents;
	}
	
	private void parseComtradeDataForEvents(ProcessDatDTO mergedComtradeData) throws M9000Exception
	{
		long end = 0;
		long start = System.currentTimeMillis();
//		short sData;
		DataInputStream dis = null;
		digitalInfoList =mergedComtradeData.getLstDigitalInfo(); 
		if (digitalInfoList == null)
		{
			parseDigitalChannelsInfo(mergedComtradeData);
		}

		if (mergedComtradeData.getBinaryData() != null)
		{
			logger.debug("Binary file..."+mergedComtradeData.getBinaryData().size());
			try {
				dis = new DataInputStream(new ByteArrayInputStream(mergedComtradeData.getBinaryData().toByteArray()));
				int eventChannelCnt = mergedComtradeData.getMergedEventsCount();
				logger.debug("Merged Analog count "+mergedComtradeData.getMergedAnalogsCount());
				logger.debug("Merged events count "+eventChannelCnt);
				int totalBytesPerSample = (int) ((mergedComtradeData.getMergedAnalogsCount() * 2) + (2 * Math.ceil((float)eventChannelCnt/16))+4+4);
				logger.debug("Total bytes per sample "+totalBytesPerSample);
				byte skipAnalogBytes[] = new byte[(int)((mergedComtradeData.getMergedAnalogsCount() * 2) +4+4)];
				ByteBuffer roBuff;
				int i = 0;
				int totalNumberOfSamples = mergedComtradeData.getSampleCnt();
				logger.debug("Total sample count "+totalNumberOfSamples);
				byte[] bytes = new byte[totalBytesPerSample*totalNumberOfSamples];
				logger.debug("Total size to read "+(totalBytesPerSample*totalNumberOfSamples));
//				analogData = new double[totalNumberOfSamples][stationDetails.getAnalogChannelsCount()];
//				digitalData = new short[totalNumberOfSamples][eventChannelCnt];
				short chnlValue;
				abnormalDigiChnl = new int [eventChannelCnt];
				short digitalChnl = 0;
				short numOfDigitalBytes = (short)(Math.ceil((float)eventChannelCnt/16)); 
				String digitalBits;
//				char[] charDigitalBits;
				int iBitCnt = 0;
				int ichnlCnt = 0;
				int totalNoOfDigitalChnl = 0;
//				double chnlMultiplier;
//				double chnlOffset;
//				ArrayList<ChannelInfo> digitalChnlConfList;
//				digitalChnlConfList = (ArrayList<ChannelInfo>)stationDetails.getDigitalInfoCollection();
				int[] eventChnlStatus = getChannelConfigurationStatus(mergedComtradeData);
				String zeros = "0000000000000000";
				String leadZero;

				dis.readFully(bytes);
				roBuff = ByteBuffer.wrap(bytes);
				roBuff.order(ByteOrder.LITTLE_ENDIAN);
				
				while (i<totalNumberOfSamples)
				{
//					System.out.println("Sample cnt "+i);
//					roBuff.getInt();

//					dataTimestamps[i] = roBuff.getInt();
					
					ichnlCnt = 0;
//					while (ichnlCnt < stationDetails.getAnalogChannelsCount())
//					{
//						chnlMultiplier = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(ichnlCnt)).getChnlMultiplier();
//						chnlOffset = ((AnalogInfo)((ArrayList<ChannelInfo>)stationDetails.getAnalogInfoCollection()).get(ichnlCnt)).getChnlOffset();
//						sData = roBuff.getShort();
//						if (sData == M9kConstants.BINARY_MISSING_DATA.shortValue())
//						{
//							analogData[i][ichnlCnt] = 0;
//						}
//						else
//						{
//							analogData[i][ichnlCnt] = (chnlMultiplier*sData)+chnlOffset;
//						}
//						ichnlCnt++;
//					}
					roBuff.get(skipAnalogBytes);
					ichnlCnt = 0;
					totalNoOfDigitalChnl = 0;
					while (ichnlCnt++ < numOfDigitalBytes)
					{
						digitalChnl = roBuff.getShort();//Short.reverseBytes(roBuff.getShort());
						digitalBits = Integer.toBinaryString(digitalChnl);
						if (digitalBits.length() > 16)
						{
							digitalBits = digitalBits.substring(digitalBits.length() - 16);
							
						}
						else
						{
//							digitalBits = stuffLeadingzeroes(digitalBits);
							leadZero = zeros.substring(0,16-digitalBits.length());
							digitalBits=leadZero.concat(digitalBits);
						}
						iBitCnt = 16; // 16 channels for 2 bytes
//						charDigitalBits = digitalBits.toCharArray();
						while (iBitCnt > 0 && totalNoOfDigitalChnl < eventChannelCnt)
						{
							chnlValue = Short.parseShort(digitalBits.substring(iBitCnt-1, iBitCnt));
//							if (totalNoOfDigitalChnl > 128)
//							{
//								System.out.println("Channel value "+chnlValue);
//							}
//							chnlValue = Short.parseShort(""+charDigitalBits[iBitCnt]);
								if (eventChnlStatus[totalNoOfDigitalChnl] == 0) // y = 0 indicates OPEN in config file
								{
									if (chnlValue == 1 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
									{
										abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
										totalAbnormalChannels++;
									}
								}
								else if (eventChnlStatus[totalNoOfDigitalChnl] == 1) // y = 0 indicates OPEN in config file
								{
									if (chnlValue == 0 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
									{
										abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
										totalAbnormalChannels++;
									}
								}
								
								totalNoOfDigitalChnl++;
							iBitCnt--;
						}
					}
					i++;
				}
				logger.debug("Total abnormal channels..."+totalAbnormalChannels);
			} catch (FileNotFoundException fnfe) {
				throw new M9000Exception(fnfe);
			} catch (IOException ioe) {
				throw new M9000Exception(ioe);
			}
			finally
			{
				end = System.currentTimeMillis();
				logger.debug("Time taken for ParseComtradeData binary in M9kParser "+(end-start));
				try {
					if (dis != null)
					{
						dis.close();
						dis = null;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}
		}
		else if (mergedComtradeData.getData() != null)
		{
			logger.debug("ASCII File...");
			BufferedReader bfr = null;
			try
			{
			bfr = new BufferedReader(new StringReader(mergedComtradeData.getData().toString()));
			int totalColumns = (int) (mergedComtradeData.getMergedAnalogsCount() + mergedComtradeData.getMergedEventsCount()+2);
//			logger.debug("Total cols: "+totalColumns+" analog cnt: "+stationDetails.getAnalogChannelsCount()+" Digital cnt: "+stationDetails.getDigitalChannelsCount());
			String[] data = new String[totalColumns];
			String strLine = "";
			int i = 0;
			int totalNumberOfSamples = mergedComtradeData.getSampleCnt();
			logger.debug("totalNumberOfSamples..."+totalNumberOfSamples);
//			analogData = new double[totalNumberOfSamples][stationDetails.getAnalogChannelsCount()];
			short[][] digitalData = new short[totalNumberOfSamples][mergedComtradeData.getMergedEventsCount()];
			abnormalDigiChnl = new int [mergedComtradeData.getMergedEventsCount()];
			
			int totalNoOfDigitalChnl = 0;
//			digitalChnlConfList = (ArrayList<ChannelInfo>)stationDetails.getDigitalInfoCollection();
			int[] eventChnlStatus = getChannelConfigurationStatus(mergedComtradeData);
			int columnsCnt = 0;
			int analogCnt = mergedComtradeData.getMergedAnalogsCount();
			int digitalCnt = mergedComtradeData.getMergedEventsCount();
			logger.debug("Analog Channels Count..."+analogCnt);
			logger.debug("Digital Channels Count..."+digitalCnt);
//			System.out.println("Analog Channels Count..."+analogCnt);
//			System.out.println("Digital Channels Count..."+digitalCnt);
//			m9kUtils.startTimer("\n\n\t\t\tParsing Dat file");
			// START: 21-Feb-2016: Removed ready() function as it might cause failure like in Dunwoodie, where fault did not open
//			while (bfr.ready() && i<totalNumberOfSamples)
			while (i<totalNumberOfSamples)
			// END: 21-Feb-2016
			{
				strLine = bfr.readLine();
				strLine = strLine.substring(nthOccurrence(strLine,',',(analogCnt+1))+1);
				data = strLine.split(",");
				//test
				columnsCnt=0;
//				logger.debug("Lines read..."+i + " \n Data read: "+strLine);
//				System.out.println("Lines read..."+i + " \n Data read: "+strLine);

//				dataTimestamps[i] = Integer.parseInt(data[columnsCnt++]);
//				columnsCnt++;
				
//				ichnlCnt = 0;
//				while (ichnlCnt < analogCnt)
//				{
//					columnsCnt++;
//					
////					System.out.print(analogData[i][ichnlCnt]+" , ");
//					ichnlCnt++;
//				}
//				logger.debug("Columns Cnt..."+columnsCnt);
//				ichnlCnt = 0;
				totalNoOfDigitalChnl = 0;
//				System.out.println("\n\n\t\t\t\t Column after analog..."+columnsCnt);
				while (totalNoOfDigitalChnl < digitalCnt)
				{
//					logger.debug("Digital Columns Cnt..."+columnsCnt+" i-> "+i + " totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
//					System.out.println("Digital Columns Cnt..."+columnsCnt+" i-> "+i + " totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
//					System.out.println("Digital Columns Cnt..."+columnsCnt+" i-> "+i + " totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
						digitalData[i][totalNoOfDigitalChnl] = Short.parseShort(data[columnsCnt++]);
							if ( eventChnlStatus[totalNoOfDigitalChnl] == 0) // y = 0 indicates OPEN in config file
							{
								if (digitalData[i][totalNoOfDigitalChnl] == 1 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
								{
//									System.out.println("\t\t\t\t\tabnormal index i "+i+" totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
									abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
									totalAbnormalChannels++;
								}
							}
							else // y = 0 indicates OPEN in config file
							{
//								System.out.println("\n\t\t\t\tStatus "+((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getStatus()+" chnl Id "+((DigitalInfo)digitalChnlConfList.get(totalNoOfDigitalChnl)).getChnlId());
//								System.out.println("\n\t\t\t\t Data value... "+digitalData[i][totalNoOfDigitalChnl]);
								if (digitalData[i][totalNoOfDigitalChnl] == 0 && abnormalDigiChnl[totalNoOfDigitalChnl] == 0)
								{
//									System.out.println("\t\t\tabnormal2 index i "+i+" totalNoOfDigitalChnl "+totalNoOfDigitalChnl);
									abnormalDigiChnl[totalNoOfDigitalChnl] = totalNoOfDigitalChnl+1;
									totalAbnormalChannels++;
								}
							}
							totalNoOfDigitalChnl++;
				}
				i++;
				
			}
//			m9kUtils.endTimer("\n\n\t\t\tParsing Dat file");
//			logger.debug("analogData..."+analogData.length);
			logger.debug("digitalData..."+digitalData.length);
			
		} catch (FileNotFoundException fnfe) {
			throw new M9000Exception(fnfe);
		} catch (IOException ioe) {
			throw new M9000Exception(ioe);
		}catch (Exception e) {
			throw new M9000Exception(e);
		}
		finally
		{
			end = System.currentTimeMillis();
			logger.debug("Time taken for ParseComtradeData ASCII in M9kParser "+(end-start));
			logger.debug("1Returing from the parseComtradeData function...");

			try {
				logger.debug("2Returing from the parseComtradeData function...");
				if (bfr != null)
				{
					logger.debug("3Returing from the parseComtradeData function...");
					bfr.close();
					bfr = null;
				}
				logger.debug("4Returing from the parseComtradeData function...");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.debug("5Returing from the parseComtradeData function...");
			}
			logger.debug("6Returing from the parseComtradeData function...");
		}
		logger.debug("7Returing from the parseComtradeData function...");
		}
	}	

	
	// Parse Analog Data
	public Map<LineChannels, List<Double>> getAnalogDataForSelectedChannels(ProcessDatDTO mergedComtradeData, Map<Integer, LineChannels> mapSelectedChannels) throws M9000Exception
	{
		short sData;
		double chnlMultiplier;
		double chnlOffset;
		
		BufferedInputStream dis = null;
		analogInfoList =mergedComtradeData.getLstAnalogInfo();
		if (analogInfoList == null)
		{
			parseAnalogChannelsInfo(mergedComtradeData);
		}
		Map<LineChannels, List<Double>> mapAnalogData = new HashMap<LineChannels, List<Double>>(mapSelectedChannels.size());
		if (mergedComtradeData.getBinaryData() != null)
		{
			logger.debug("Binary file..."+mapSelectedChannels);
			try {
				for (LineChannels lineChannel : LineChannels.values()) {
					if (mapSelectedChannels.containsValue(lineChannel))
					{
						mapAnalogData.put(lineChannel,  new ArrayList<Double>(mergedComtradeData.getSampleCnt()));
					}
				  }
//				for (int i = 0; i < selectedChannels.length; i++) {
//					mapAnalogData.put(selectedChannels[i], new ArrayList<Double>(mergedComtradeData.getSampleCnt()));
//				}
				dis = new BufferedInputStream(new ByteArrayInputStream(mergedComtradeData.getBinaryData().toByteArray()));
				int totalBytesPerSample = (int) ((mergedComtradeData.getMergedAnalogsCount() * 2) + (2 * Math.ceil((float)mergedComtradeData.getMergedEventsCount()/16))+4+4);
//				byte skipEventBytes[] = new byte[(int)(2 * Math.ceil((float)mergedComtradeData.getMergedEventsCount()/16))];
				int i = 0;
				int totalNumberOfSamples = mergedComtradeData.getSampleCnt();
				ByteBuffer roBuff ;
				byte[] bytes = new byte[totalBytesPerSample];
//				double[][] analogData = new double[mergedComtradeData.getMergedAnalogsCount()][totalNumberOfSamples];
				int[] dataTimestamps = new int[totalNumberOfSamples];
				int ichnlCnt = 0;
				LineChannels selectedLineChannel;
				while (i<totalNumberOfSamples)
				{
					dis.read(bytes);
					roBuff = ByteBuffer.wrap(bytes);
					roBuff.order(ByteOrder.LITTLE_ENDIAN);
					roBuff.getInt();

					dataTimestamps[i] = roBuff.getInt();
					
					ichnlCnt = 0;
					while (ichnlCnt < mergedComtradeData.getMergedAnalogsCount())
					{
						chnlMultiplier = ((AnalogInfo)(analogInfoList.get(ichnlCnt))).getChnlMultiplier();
						chnlOffset = ((AnalogInfo)(analogInfoList.get(ichnlCnt))).getChnlOffset();
						sData = roBuff.getShort();
						selectedLineChannel = mapSelectedChannels.get(ichnlCnt+1);
						if (selectedLineChannel != null)
						{
							if (sData == M9kConstants.BINARY_MISSING_DATA.shortValue())
							{
//								analogData[ichnlCnt][i] = 0;
								mapAnalogData.get(selectedLineChannel).add(0.0);
							}
							else
							{
//								analogData[ichnlCnt][i] = (chnlMultiplier*sData)+chnlOffset;
								mapAnalogData.get(selectedLineChannel).add((chnlMultiplier*sData)+chnlOffset);
							}
//							logger.debug("Selected channel "+selectedLineChannel+" channel cnt "+ichnlCnt+" first data "+mapAnalogData.get(selectedLineChannel).get(0)+" chnlMultiplier "+chnlMultiplier);
						}
						ichnlCnt++;
					}
					i++;
				}
				bytes = null;
				roBuff = null;

			} catch (FileNotFoundException fnfe) {
				throw new M9000Exception(fnfe);
			} catch (IOException ioe) {
				throw new M9000Exception(ioe);
			}
			catch (Exception ioe) {
				throw new M9000Exception(ioe);
			}
			finally
			{
				try {
					if (dis != null)
					{
						dis.close();
						dis = null;
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}
		}
		return mapAnalogData;
	}
//	private int[] getChannelConfigurationStatus(ProcessDatDTO mergedComtradeData)
//	{
//		int[] status = new int[mergedComtradeData.getMergedEventsCount()];
//		String eventDetails[] = mergedComtradeData.getEvents().toString().split(M9kStationConstants.NEWLINE);
//		for (int i = 0; i < mergedComtradeData.getMergedEventsCount(); i++) {
//			status[i] = Integer.parseInt(eventDetails[i].substring(eventDetails[i].length()-1)); 
//		}
//		
//		return status;
//	}
	
	private int[] getChannelConfigurationStatus(ProcessDatDTO mergedComtradeData)
	{
		int[] status = new int[mergedComtradeData.getMergedEventsCount()];
		for (int i = 0; i < mergedComtradeData.getMergedEventsCount(); i++) {
			status[i] = ((DigitalInfo)digitalInfoList.get(i)).getStatus(); 
//			System.out.println("Status of channel "+(i+1)+" "+status[i]);
		}
		
		return status;
	}
	private static int nthOccurrence(String str, char c, int n) {
	    int pos = str.indexOf(c, 0);
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(c, pos+1);
	    return pos;
	}
	
	public static void main (String[] args)
	{
		M9kStationXMLUtil.initXml(null);
		M9kStationComtradeUtil m9kStationComtradeUtil = new M9kStationComtradeUtil();
		m9kStationComtradeUtil.getLineGroupDetailsForFaultLoc();
//		MySqlDatDAO mySqlDataDAO = null;
//		mySqlDataDAO =new MySqlDatDAO();
//		
//		ComtradeDataDTO comtradeDataDTO  = mySqlDataDAO.getFaultRecordToProcess();
//		System.out.println("Comtrade data "+comtradeDataDTO);
//		ProcessDatDTO processDatDTO = m9kStationComtradeUtil.convertToProcessDataDTO(comtradeDataDTO);
//		try {
//			System.out.println("Active events list "+m9kStationComtradeUtil.getActiveEventsId(processDatDTO));
//			
//			Map<Integer, LineChannels> selectedChannels = new HashMap<Integer, LineChannels>();
//			selectedChannels.put(1, LineChannels.CHAN_IA);
//			selectedChannels.put(3, LineChannels.CHAN_IB);
//			selectedChannels.put(5, LineChannels.CHAN_IC);
//			 Map<LineChannels, List<Double>> mapResult = m9kStationComtradeUtil.getAnalogDataForSelectedChannels(processDatDTO, selectedChannels);
//			 Iterator<LineChannels> iterateMapResult = mapResult.keySet().iterator();
//			 LineChannels chnlId;
//			 while(iterateMapResult.hasNext())
//			 {
//				 chnlId = iterateMapResult.next();
//				 System.out.println("Channel id "+chnlId+" total samples cnt "+mapResult.get(chnlId).size());
//			 }
//		} catch (M9000Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private ProcessDatDTO convertToProcessDataDTO(ComtradeDataDTO comtradeDataDTO)
	{
		ProcessDatDTO processDatDTO = new ProcessDatDTO();
		processDatDTO.setMergedAnalogsCount(comtradeDataDTO.getAnalogs().toString().split("\n").length);
		processDatDTO.setMergedEventsCount(comtradeDataDTO.getEvents().toString().split("\n").length);
		processDatDTO.setAnalogs(comtradeDataDTO.getAnalogs());
		processDatDTO.setEvents(comtradeDataDTO.getEvents());
		processDatDTO.setSampleCnt(comtradeDataDTO.getSampleCnt());
		ByteArrayOutputStream binaryData = new ByteArrayOutputStream();
		DataOutputStream binaryOutStream = new DataOutputStream(binaryData);
		byte[] buf = new byte[3000];
        int read = 0;
        try
        {
			while ((read = comtradeDataDTO.getBinaryDataSteam().read(buf)) > 0) {
				binaryOutStream.write(buf, 0, read);
	        }
			binaryOutStream.close();
			comtradeDataDTO.getBinaryDataSteam().close();
        }
        catch (Exception e) {
			// TODO: handle exception
		}
        finally
        {
        	if (binaryOutStream != null)
        	{
        		try {
					binaryOutStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	if (comtradeDataDTO.getBinaryDataSteam() != null)
        	{
        		try {
        			comtradeDataDTO.getBinaryDataSteam().close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
		processDatDTO.setBinaryData(binaryData);
		return processDatDTO;
	}

	
	private void parseAnalogChannelsInfo(ProcessDatDTO mergedComtradeData) throws M9000Exception
	{
		int chnlCnt = 0;
		AnalogInfo analogChannelInfo;
		String strTokens[] = null;
		String[] analogs = mergedComtradeData.getAnalogs().toString().split("\n");
		analogInfoList = new ArrayList<ChannelInfo>();
		// Process Analog Channels Info
		try {
			for (int i = 0; i < analogs.length; i++)
			{
				strTokens = analogs[i].split(",");
				analogChannelInfo = new AnalogInfo(Integer.parseInt(strTokens[0]));
				analogChannelInfo.setChnlId(strTokens[1]);
				analogChannelInfo.setPhaseId(strTokens[2]);
				analogChannelInfo.setCircuitName(strTokens[3]);
				analogChannelInfo.setChnlUnit(strTokens[4]);
				analogChannelInfo.setChnlMultiplier(Double.parseDouble(strTokens[5]));
				analogChannelInfo.setChnlOffset(Double.parseDouble(strTokens[6]));
				analogChannelInfo.setChnlSkew(Double.parseDouble(strTokens[7]));
				analogChannelInfo.setRangeMin(Integer.parseInt(strTokens[8]));
				analogChannelInfo.setRangeMax(Integer.parseInt(strTokens[9]));
				analogChannelInfo.setPrimary(Double.parseDouble(strTokens[10]));
				analogChannelInfo.setSecondary(Double.parseDouble(strTokens[11]));
				analogChannelInfo.setScalingFactor(strTokens[12].charAt(0));
				analogInfoList.add(analogChannelInfo);
				chnlCnt++;
			}
			logger.debug("Total analog channels list "+analogInfoList.size());

		} catch (Exception e) {
			logger.error("Error occured during parseAnalogChannelsInfo",e);
		}
		
	}
	
	private void parseDigitalChannelsInfo(ProcessDatDTO mergedComtradeData) throws M9000Exception
	{
		int chnlCnt = 0;
		DigitalInfo digitalChannelInfo;
		String strTokens[] = null;
		String[] events = mergedComtradeData.getEvents().toString().split("\n");
//		System.out.println("Events length "+events.length);
		digitalInfoList = new ArrayList<ChannelInfo>();
		// Process Analog Channels Info
		try {
//			while (chnlCnt < stationDetails.getDigitalChannelsCount() && brConfig.ready())
			for (int i = 0; i < events.length; i++) 
			{
//				System.out.println("\t\t\tEvents: "+i+" -> "+events[i]);
				strTokens = events[i].split(",");
				digitalChannelInfo = new DigitalInfo(Integer.parseInt(strTokens[0]));
				digitalChannelInfo.setChnlId(strTokens[1]);
//				logger.debug("Digital Channel ID.."+strTokens[1]);
				digitalChannelInfo.setPhaseId(strTokens[2]);
				digitalChannelInfo.setCircuitName(strTokens[3]);
//				logger.debug("Digital Circuit Name..."+strTokens[3]);
				digitalChannelInfo.setStatus(Integer.parseInt(strTokens[4]));
				digitalInfoList.add(digitalChannelInfo);
				chnlCnt++;
			}

		}catch (Exception e) {
			logger.error("Error occured during parseDigitalChannelsInfo",e);
		}
		

	}

	// Return comma seperated linegroups name if true or null if it is false
	public String getFaultLogicStatus(ProcessDatDTO mergedComtradeData)
	{
		boolean booLogic = false;
		String strTrueLinegroups = null;
		Map<String, DigitalInfo> abnormalEvents;
		List<LineGroupsAlgorithm> lstLineGroups;
		LineGroupsAlgorithm lineGroupsAlgorithm;
		try {
			abnormalEvents = getActiveEventsId(mergedComtradeData);
			if (abnormalEvents == null || abnormalEvents.isEmpty())
			{
				logger.info("No abnormal events. Fault logic is set to false");
				booLogic = false;
			}
			else
			{
				lstLineGroups = M9kStationXMLUtil.getLstLineGroups();
				
				if (lstLineGroups == null || lstLineGroups.isEmpty())
				{
					logger.info("No Line Groups defined. Fault logic is set to false");
					booLogic = false;
				}
				else
				{
					M9kLineLogicFilter m9kLineLogicFilter = new M9kLineLogicFilter();
					for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator.hasNext();) {
						lineGroupsAlgorithm = iterator.next();
//						System.out.println("About to check Logic for linegroup name "+lineGroupsAlgorithm.getLineGroupName());
						if (lineGroupsAlgorithm.getDecisionLogic() != null && !lineGroupsAlgorithm.getDecisionLogic().isEmpty())
						{
							booLogic = m9kLineLogicFilter.isLogicTrue(abnormalEvents, lineGroupsAlgorithm.getDecisionLogic());
							if (booLogic)
							{
								logger.info("The logic is true for the linegroup "+lineGroupsAlgorithm.getDisplayName()+" with decision logic "+lineGroupsAlgorithm.getDecisionLogic());
								if (strTrueLinegroups == null)
								{
									strTrueLinegroups = lineGroupsAlgorithm.getName();
								}
								else
								{
									strTrueLinegroups = strTrueLinegroups.concat(","+lineGroupsAlgorithm.getName());
								}
							}
						}
					}
				}
			}
			
		} catch (M9000Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("Fault logic true list "+strTrueLinegroups);
		return strTrueLinegroups;
	}
	
	// Called from the remote master with appropriate station master 
	public String getLineGroupDetailsForFaultLoc(String configXml)
	{
		M9kStationXMLUtil.initXml(configXml);
		return getLineGroupDetailsForFaultLoc();
	}
	public String getLineGroupDetailsForFaultLoc()
	{
		StringBuffer strLineGroupDetails = new StringBuffer();
		List<LineGroupsAlgorithm> lstLineGroups;
		LineGroupsAlgorithm lineGroups;
		

		int lineGroupCount = 1;
		try {
			lstLineGroups = M9kStationXMLUtil.getLstLineGroups();
			if (lstLineGroups != null && lstLineGroups.size() > 0)
			{
				strLineGroupDetails.append(M9kConstants.NEWLINE);
			}
//				System.out.println("Entered M9kStationXMLUtil populateLineGroups xmlDfrs.length "+xmlDfrs.length);
			for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator.hasNext();) {
				lineGroups =  iterator.next();
			
				strLineGroupDetails.append("[USI Line#"+lineGroupCount++ +"]");
				strLineGroupDetails.append(M9kConstants.NEWLINE);
				strLineGroupDetails.append("LineName="+lineGroups.getId()+" - "+lineGroups.getLineGroupName());
				strLineGroupDetails.append(M9kConstants.NEWLINE);
				strLineGroupDetails.append("LineId="+lineGroups.getId());
				strLineGroupDetails.append(M9kConstants.NEWLINE);
				if (lineGroups.getDecisionLogic() != null && !lineGroups.getDecisionLogic().trim().equalsIgnoreCase("NONE"))
				{
					strLineGroupDetails.append("LineFaultLogic="+lineGroups.getDecisionLogic());
				}
				else
				{
					strLineGroupDetails.append("LineFaultLogic=0");
				}
				strLineGroupDetails.append(M9kConstants.NEWLINE);
				strLineGroupDetails.append("AutoCalFaultLoc="+(lineGroups.getEnableAutoCalc().equalsIgnoreCase(M9kConstants.YES)?M9kConstants.ENABLE_ONE:M9kConstants.DISABLE_ZERO));
				strLineGroupDetails.append(M9kConstants.NEWLINE);
				strLineGroupDetails.append(getLineGroupInputChannelList(lineGroups.getInputChannels()));
				strLineGroupDetails.append("PosSeqR="+lineGroups.getPositiveResistance());
				strLineGroupDetails.append(M9kConstants.NEWLINE);
				strLineGroupDetails.append("PosSeqX="+lineGroups.getPositiveReactance());
				strLineGroupDetails.append(M9kConstants.NEWLINE);
				strLineGroupDetails.append("ZeroSeqR="+lineGroups.getZeroResistance());
				strLineGroupDetails.append(M9kConstants.NEWLINE);
				strLineGroupDetails.append("ZeroSeqX="+lineGroups.getZeroReactance());
				strLineGroupDetails.append(M9kConstants.NEWLINE);
				strLineGroupDetails.append("LineMiles="+lineGroups.getLineMiles());
				strLineGroupDetails.append(M9kConstants.NEWLINE);
				// START: 17-Nov-2020 - Implemented for For lightning correlation 
//				strLineGroupDetails.append(lineGroups.getComments());
				strLineGroupDetails.append("LcLineId="+lineGroups.getComments().replace(M9kConstants.NEWLINE,";;"));
				// END: 17-Nov-2020
				strLineGroupDetails.append(M9kConstants.NEWLINE);
				}
					  
		} catch (Exception e) {
			logger.error("Unable to get the line group details for fault location ",e);
		}
		logger.debug("Line groups list for fault loc "+strLineGroupDetails);
		System.out.println("Line groups list for fault loc "+strLineGroupDetails);
		return strLineGroupDetails.toString();
	}

	/**
	 * Get fault info for lightning correlation from line groups
	 * @return
	 */
	public Map<String, String> getFaultInfoForLC(List<String> associatedLinegroups)
	{
		Map<String, String> mapFaultInfoForLC = null;
		List<LineGroupsAlgorithm> lstLineGroups;
		LineGroupsAlgorithm lineGroups;
		
		try {
			lstLineGroups = M9kStationXMLUtil.getLstLineGroups();
			logger.info("In getFaultInfoForLC lstLineGroups.size() "+lstLineGroups.size());
			for (Iterator<LineGroupsAlgorithm> iterator = lstLineGroups.iterator(); iterator.hasNext();) {
				lineGroups =  iterator.next();
				logger.info("In getFaultInfoForLC Check for line group name "+lineGroups.getName()+" with associated list "+associatedLinegroups);
				logger.info("In getFaultInfoForLC does it contain? "+(associatedLinegroups.contains(lineGroups.getName())));
				if (associatedLinegroups.contains(lineGroups.getName().trim()))
				{
					if (mapFaultInfoForLC == null)
					{
						mapFaultInfoForLC = new HashMap<String,String> (associatedLinegroups.size());
					}
					mapFaultInfoForLC.put(lineGroups.getName().trim(), lineGroups.getComments().replace(M9kConstants.NEWLINE,";;"));
				}
			}
					  
		} catch (Exception e) {
			logger.error("Unable to get the line group details for fault location ",e);
		}
		logger.debug("Line groups list for fault loc "+mapFaultInfoForLC);
		return mapFaultInfoForLC;
	}

	private String getLineGroupInputChannelList(List<AnalogChannelDTO> lineGroupInputChannels) {
		Map<LineChannels,String> mapChannelTypes;
		StringBuffer strLineGroupInputChannels = new StringBuffer(); 
		mapChannelTypes = new HashMap<LineChannels, String>(lineGroupInputChannels.size());
		for (int i = 0; i < lineGroupInputChannels.size(); i++) {
			mapChannelTypes.put(lineGroupInputChannels.get(i).getLineChannel(), lineGroupInputChannels.get(i).getChannel());
		}
		if (mapChannelTypes.containsKey(LineChannels.CHAN_VA))
		{
			strLineGroupInputChannels.append("VaChan="+mapChannelTypes.get(LineChannels.CHAN_VA));
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		else
		{
			strLineGroupInputChannels.append("VaChan=0");
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		if (mapChannelTypes.containsKey(LineChannels.CHAN_VB))
		{
			strLineGroupInputChannels.append("VbChan="+mapChannelTypes.get(LineChannels.CHAN_VB));
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		else
		{
			strLineGroupInputChannels.append("VbChan=0");
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		if (mapChannelTypes.containsKey(LineChannels.CHAN_VC))
		{
			strLineGroupInputChannels.append("VcChan="+mapChannelTypes.get(LineChannels.CHAN_VC));
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		else
		{
			strLineGroupInputChannels.append("VcChan=0");
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		if (mapChannelTypes.containsKey(LineChannels.CHAN_IA))
		{
			strLineGroupInputChannels.append("IaChan="+mapChannelTypes.get(LineChannels.CHAN_IA));
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		else
		{
			strLineGroupInputChannels.append("IaChan=0");
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		if (mapChannelTypes.containsKey(LineChannels.CHAN_IB))
		{
			strLineGroupInputChannels.append("IbChan="+mapChannelTypes.get(LineChannels.CHAN_IB));
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		else
		{
			strLineGroupInputChannels.append("IbChan=0");
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		if (mapChannelTypes.containsKey(LineChannels.CHAN_IC))
		{
			strLineGroupInputChannels.append("IcChan="+mapChannelTypes.get(LineChannels.CHAN_IC));
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		else
		{
			strLineGroupInputChannels.append("IcChan=0");
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		if (mapChannelTypes.containsKey(LineChannels.CHAN_IN))
		{
			strLineGroupInputChannels.append("InChan="+mapChannelTypes.get(LineChannels.CHAN_IN));
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		else
		{
			strLineGroupInputChannels.append("InChan=0");
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		if (mapChannelTypes.containsKey(LineChannels.CHAN_REF))
		{
			strLineGroupInputChannels.append("RefChan="+mapChannelTypes.get(LineChannels.CHAN_REF));
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}
		else
		{
			strLineGroupInputChannels.append("RefChan=0");
			strLineGroupInputChannels.append(M9kConstants.NEWLINE);
		}

		logger.debug("Line groups details for fault location "+strLineGroupInputChannels);
	return strLineGroupInputChannels.toString();	
	}

}
