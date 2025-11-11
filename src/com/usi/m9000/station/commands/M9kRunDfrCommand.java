package com.usi.m9000.station.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.net.M9kStationCommandClient;

public class M9kRunDfrCommand  extends Thread{
	M9kStationCommandClient commandClient;
	public String command;
	Map<String, String> mapDfrs;	
	static HierarchicalINIConfiguration iniConf;
	String dfrId;
	String ipAddress;
	int port;
	static String previousCommand = "";
	static String specialCommand = "";
	static List<String> lstCommands; 
public M9kRunDfrCommand() {
		super();
		try {
//			ResourceBundle.getBundle("station.properties");
			iniConf = new HierarchicalINIConfiguration("station.properties");
			mapDfrs = new HashMap<String, String>();
			lstCommands = new ArrayList<String>();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

public void run()
{
//	Scanner in = new Scanner(System.in);
	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	System.out.println("Enter Help for usage info");
	System.out.println("Enter a command(type quit or q to exit): ");
	try {
		while ((command = in.readLine()) != null)
		{
//			System.out.println("cmd entered "+command);
			if (command.equalsIgnoreCase("quit") || command.equalsIgnoreCase("q"))
			{
				System.out.println("bye");
				break;
			}
			else if (command.equalsIgnoreCase("history") || command.startsWith("hist"))
			{
				processHistory();
				continue;
			}
			else if (isInteger(command))
			{
				int index = Integer.parseInt(command);
				if (lstCommands.size() >= index)
				{
					command = lstCommands.get(index-1);
//					if (command.toUpperCase().startsWith("VERBOSE"))
//					{
//						processVerboseCommand();
//					}
				}
				if (command.equalsIgnoreCase("history") || command.startsWith("hist"))
				{
					processHistory();
					continue;
				}
			}
			else if(command.isEmpty())
			{
				if (previousCommand.isEmpty())
				{
					command = "Help";
				}
				else
				{
					command = previousCommand;
				}
			}
			if (command.toUpperCase().startsWith("VERBOSE"))
			{
				processVerboseCommand();
			}
			else if (command.toUpperCase().startsWith("CALIBRATE"))
			{
				String[] strSplit = command.split(" "); 
				if (strSplit.length > 1)
				{
					specialCommand=strSplit[0]+", times="+strSplit[1];
				}
				System.out.println("Calibrating...");

			} 
			try {
				if (specialCommand.isEmpty())
				{
					executeCommand(command);
				}
				else
				{
					executeCommand(specialCommand);
					specialCommand = "";
				}
				if (!lstCommands.contains(command) && isCommandSuccessfull())
				{
					lstCommands.add(command);
				}
			} catch (M9000Exception e) {
				e.printStackTrace();
			}
			previousCommand = command;
			System.out.println("\nEnter a command(type quit or q to exit): ");
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
public static void main(String args[])
{
	M9kRunDfrCommand runCommand = new M9kRunDfrCommand();
	runCommand.run();
//	try {
//		if (args.length == 0)
//		{
//			System.out.println("Usage: M9kTestCommands <commands>");
//			return;
//		}
//		command = args[0];
//		int i =1;
//		while (i < args.length)
//		{
//			command+=", "+args[i++];
//		}
//		runCommand.executeCommand(command);
////		M9kLineGroups dfrInfo = new M9kLineGroups();
////		dfrInfo.getLineGroupsInfo();
//	} catch (M9000Exception e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
}

public void executeCommand(String dfrCmd) throws M9000Exception
{
//	System.out.println("Command to be executed "+dfrCmd);
//	String result = send("195.1.1.72", 9978, dfrCmd);
//	System.out.println("Result: "+result);
	
	Set<String> dfrs = iniConf.getSections();
//	String ipAddress = null;
//	int port = 0;
	String result;
	for (String dfr : dfrs) {
		if (!dfr.toUpperCase().startsWith("DFR"))
		{
			continue;
		}
//		System.out.println("[" + dfr + "]");
		ipAddress = iniConf.getString(dfr + ".ip-address");
		port = iniConf.getInt(dfr + ".port");
//		System.out.println("Id " + iniConf.getString(dfr + ".id"));
//		System.out.println("Ip address " + ipAddress);
//		System.out.println("Port " + port);

		try
		{
			result = send(ipAddress, port, dfrCmd);
		}
		catch (M9000Exception e) {
			result = "DFR "+ipAddress+" is down";
		}
		mapDfrs.put(dfr, result);

	}
	processResult();

}
private String send(String destIp, int port, String command) throws M9000Exception {
	
	String result;
//	System.out.println("Ip address " + destIp);
//	System.out.println("Port " + port);
//	System.out.println("Command "+command);
	commandClient = new M9kStationCommandClient(destIp, port, command);
	result = commandClient.sendAndReceive();
	
	return result;
}

private void processResult()
{
	String dfr;
	StringTokenizer strTok;
	for (Iterator<String> iterator = mapDfrs.keySet().iterator(); iterator.hasNext();) {
		dfr = iterator.next();
		System.out.println("DFR Name: "+dfr + " IP Address: "+getIpAddress()+" Port: "+getPort());
		if (command.equalsIgnoreCase("boarddetect"))
		{
			printBoardDetect(Long.parseLong(mapDfrs.get(dfr)));
		}
		else if (command.equalsIgnoreCase("monitor") || command.equalsIgnoreCase("dfrinfo"))
		{
			strTok = new StringTokenizer(mapDfrs.get(dfr), ",");
			while (strTok.hasMoreElements()) {
				String dfrParameter = (String) strTok.nextElement();
				System.out.println(dfrParameter.trim());
				
			}
		}
		else
		{
			System.out.println(mapDfrs.get(dfr));
		}
	}		
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

private void processHistory()
{
	int i = 0;
	for (Iterator<String> iterator = lstCommands.iterator(); iterator
			.hasNext();) {
		String type =  iterator.next();
		System.out.println(++i+". "+ type);
	}
	if (!lstCommands.contains(command) )
	{
		lstCommands.add(command);
	}

}

private void processVerboseCommand()
{
	String[] strSplit = command.split(" "); 
	if (strSplit.length > 1)
	{
		specialCommand=strSplit[0]+", value="+strSplit[1];
	}
}
private boolean isInteger( String input )  
{  
   try  
   {  
      Integer.parseInt( input );  
      return true;  
   }  
   catch( Exception e)  
   {  
      return false;  
   }  
}  

private boolean isCommandSuccessfull()
{
	boolean flgCommand = true;
	String dfr;
	for (Iterator<String> iterator = mapDfrs.keySet().iterator(); iterator.hasNext();) {
		dfr = iterator.next();
		if (mapDfrs.get(dfr).equalsIgnoreCase("Command not found"))
		{
			flgCommand = false;
			break;
		}
	}		 
	return flgCommand;
}

public String getDfrId() {
	return dfrId;
}

public void setDfrId(String dfrId) {
	this.dfrId = dfrId;
}

public String getIpAddress() {
	return ipAddress;
}

public void setIpAddress(String ipAddress) {
	this.ipAddress = ipAddress;
}

public int getPort() {
	return port;
}

public void setPort(int port) {
	this.port = port;
}

}
