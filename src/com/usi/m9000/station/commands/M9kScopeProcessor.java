package com.usi.m9000.station.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.dto.DfrDTO;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kStationConstants;
import com.usi.m9000.station.util.M9kStationUtil;
import com.usi.m9000.station.util.M9kStationXMLUtil;

public class M9kScopeProcessor {
M9kStationCommandClient commandClient;
static HierarchicalINIConfiguration iniConf;
private String command ;
List<DfrDTO> lstDfrs;
static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kScopeProcessor.class);
public M9kScopeProcessor() {
	try {
		iniConf = new HierarchicalINIConfiguration("station.properties");
//		lstDfrs = new ArrayList<DfrDTO>();
		lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();

	} catch (ConfigurationException e) {
		// TODO Auto-generated catch block
		logger.error("Error in initialization",e);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		logger.error("Error in initialization",e);
	}
}

public Map<String, String> getScopeConfig()
{
	Map<String, String> mapScopeConfig = new HashMap<String, String>();
	DfrDTO dfrDTO = null;
	String dfrName = "";
	StringBuffer dfrConfig;
		for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
			try {
			dfrDTO = iterator.next();
			dfrConfig = new StringBuffer();
//			dfrName = dfrDTO.getDfrName()+" - "+"["+dfrDTO.getAnalogChannelStart()+" - "+dfrDTO.getAnalogChannelEnd()+"]";
			dfrName = dfrDTO.getDfrName();
			dfrConfig.append(dfrDTO.getAnalogChannelStart()+","+dfrDTO.getDigitalChannelStart());
			dfrConfig.append(M9kStationConstants.NEWLINE);
			commandClient = new M9kStationCommandClient(dfrDTO.getIpAddress(), M9kStationUtil.getPort(), "CONFIG");
			dfrConfig.append(commandClient.sendAndReceive());
			logger.debug("DFR Name "+dfrName+" config"+dfrConfig.toString());
			mapScopeConfig.put(dfrName, dfrConfig.toString());
			} catch (M9000Exception e) {
				logger.error("DFR "+ dfrName+" is not accessible", e);
			}
		}
	return mapScopeConfig;
}

/**
 * START: 13-Jul-2020 - Hall Effect implementation - config request for specific dfrs
 * @param dfrId
 * @return configuration as a text
 */
public String getDFRSpecificScopeConfig(int dfrId)
{
	DfrDTO dfrDTO = null;
	StringBuffer dfrConfig =  new StringBuffer();
		for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
			try {
			dfrDTO = iterator.next();
				if (dfrDTO.getDfrId() == dfrId)
				{
					dfrConfig.append(dfrDTO.getAnalogChannelStart()+","+dfrDTO.getDigitalChannelStart());
					dfrConfig.append(M9kStationConstants.NEWLINE);
					commandClient = new M9kStationCommandClient(dfrDTO.getIpAddress(), M9kStationUtil.getPort(), "CONFIG");
					dfrConfig.append(commandClient.sendAndReceive());
					logger.debug("DFR Name "+dfrDTO.getDfrName()+" config"+dfrConfig.toString());
					break;
				}
			} catch (M9000Exception e) {
				logger.error("DFR "+ dfrDTO.getDfrName()+" is not accessible", e);
			}
		}
	return dfrConfig.toString();
}

public byte[] processScope(String command)
{
	M9kStationCommandClient commandClient = null;
//	Map<String,StringBuffer> mapDfrCommands = null;
	byte[] scopeData = null;
//	byte[][] stationScope=null;
		try
		{
			String ipAddress;
			int dfrId;
//			StringBuffer dfrCommand;
//			int i =0;
//			if (getLstDfrs() == null || getLstDfrs().isEmpty())
//			{
//				lstDfrs = M9kStationXMLUtil.getDFRsFromStationXML();
//			}
			logger.debug("Command received "+command);
			ipAddress = command.substring(0,command.indexOf(","));
			logger.debug("Ip address derived "+ipAddress);
			dfrId = Integer.parseInt(ipAddress.substring(ipAddress.lastIndexOf(".")+2));
			
			if (command.indexOf("ch") != -1) // Analogs present
			{
				command = processCommandForChnlOffset(dfrId, command);
			}
			else
			{
				command="SCOPE";
			}
			commandClient = new M9kStationCommandClient(ipAddress, M9kStationUtil.getPort(), command);
			scopeData = commandClient.sendAndReceiveBinary();
// START: 13-APR-2017 SCOPE data to be issued to one chassis. Ip address is received from master. For e.g. 192.168.1.101,scope,samples=480,ch=1			
//			mapDfrCommands = getDfrScopeCommands(command);
//			stationScope = new byte[mapDfrCommands.size()][];
//			for (Iterator<String> dfrIterator = mapDfrCommands.keySet().iterator(); dfrIterator.hasNext();) {
//				ipAddress = dfrIterator.next();
//				dfrCommand = mapDfrCommands.get(ipAddress);
//				commandClient = new M9kStationCommandClient(ipAddress, M9kStationUtil.getPort(), dfrCommand.toString());
//				scopeData = commandClient.sendAndReceiveBinary();
//				// TODO: When we need scope data from more than one DFR we need to merge these data in stationScope variable and return the merged data
//				stationScope[i++]=scopeData;
//			}
			logger.debug("Result after data fetch "+scopeData.length);
		}
		catch (M9000Exception e) {
			logger.error("Exception in fetching scope data ",e);
			scopeData = null;
		} catch (Exception e) {
			logger.error("Exception in fetching scope data ",e);
			scopeData = null;
		}
		return scopeData;
}

private String processCommandForChnlOffset(int dfrId, String command)
{
	String channels[]= null;
	Integer currentChannel=0;
	int newChannelIndex;
	String prefixCommand = null;
	int index =0;
	StringBuffer processedCommand = null;
	command = command.substring(command.indexOf(",")+1); // Remove IPaddress prefix
	index = command.indexOf("ch");
	channels =command.substring(index).split(",");
	prefixCommand=command.substring(0, command.indexOf("ch=")+3);
	logger.debug("Total Channels requested "+channels.length);
	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
		DfrDTO dfrDTO = iterator.next();
		if (dfrDTO.getDfrId() == dfrId)
		{
			if (channels != null)
			{
				for (int i = 0; i < channels.length; i++) {
					currentChannel = Integer.parseInt(channels[i].substring(channels[i].indexOf("=")+1));
//					if (dfrDTO.getAnalogChannelStart() <= currentChannel && dfrDTO.getAnalogChannelEnd() >= currentChannel)
//					{
						newChannelIndex = currentChannel - dfrDTO.getAnalogChannelStart()+1;
						if (processedCommand != null)
						{
							processedCommand.append(",ch="+newChannelIndex);
						}
						else
						{
							processedCommand = new StringBuffer(prefixCommand + newChannelIndex);
						}
//					}
//					else
//					{
//						if (processedCommand != null)
//						{
//							processedCommand.append(",ch="+currentChannel);
//						}
//						else
//						{
//							processedCommand = new StringBuffer(prefixCommand + currentChannel);
//						}
//					}
				}
			}
			break;
		}
	}
	if (processedCommand == null)
	{
		processedCommand = new StringBuffer(command); // To avoid null pointer exception
	}
	logger.debug("The processed SCOPE command to be sent"+processedCommand);
	return processedCommand.toString();
}
@SuppressWarnings("unused")
// May be required when we might implement SCOPE data from more than one dfrs 
private Map<String,StringBuffer> getDfrScopeCommands(String command)
{
	String ipAddress="";
	Integer currentChannel=0;
	String channels[]= null;
	String prefixCommand = null;
	String dfrNameToSend = null;
	Map<String,StringBuffer> mapScopeCmd = new HashMap<String, StringBuffer>();
	int newChannelIndex;
	int index = command.indexOf("ch");
	if (index != -1)
	{
		channels =command.substring(index).split(",");
		prefixCommand=command.substring(0, command.indexOf("ch=")+3);
		logger.debug("Total Channels requested "+channels.length);
	}
	else
	{
		dfrNameToSend = command.substring(command.indexOf(",")+1);
	}
	logger.debug("Actual command "+command +" Dfr name to send "+dfrNameToSend);
	logger.debug("\t\t\t\t\t%&^******************************** Channel no from substring "+command+" channel "+currentChannel);
	for (Iterator<DfrDTO> iterator = lstDfrs.iterator(); iterator.hasNext();) {
		DfrDTO dfrDTO = iterator.next();
		ipAddress = dfrDTO.getIpAddress().trim();
		if (channels != null)
		{
			for (int i = 0; i < channels.length; i++) {
				currentChannel = Integer.parseInt(channels[i].substring(channels[i].indexOf("=")+1));
				if (dfrDTO.getAnalogChannelStart() <= currentChannel && dfrDTO.getAnalogChannelEnd() >= currentChannel)
				{
					newChannelIndex = currentChannel - dfrDTO.getAnalogChannelStart()+1;
					if (mapScopeCmd.containsKey(ipAddress))
					{
						mapScopeCmd.get(ipAddress).append(",ch="+newChannelIndex);
					}
					else
					{
						mapScopeCmd.put(ipAddress, new StringBuffer(prefixCommand + newChannelIndex));
					}
				}				
			}
		}
		else
		{
			
			if (dfrDTO.getDfrName().equalsIgnoreCase(dfrNameToSend))
			{
				mapScopeCmd.put(ipAddress, new StringBuffer("SCOPE"));
			}
		}
	}
	return mapScopeCmd;
}

public String getCommand() {
	return command;
}

public void setCommand(String command) {
	this.command = command;
}


public List<DfrDTO> getLstDfrs() {
	return lstDfrs;
}


public void setLstDfrs(List<DfrDTO> lstDfrs) {
	this.lstDfrs = lstDfrs;
}

}
