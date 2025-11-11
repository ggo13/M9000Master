package com.usi.m9000.test;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kStationConstants;

public class TestVisualE extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel lblDfrs = null;
	private JLabel lblCommands = null;
	private JComboBox lstDfrs = null;
	private JComboBox lstCommands = null;
	private JButton btnExecute = null;
	private JLabel lblResult = null;
	private JTextArea taResult = null;
	private JScrollPane jScrollPane = null;
	private JLabel lblTitle = null;

	M9kStationCommandClient commandClient;  //  @jve:decl-index=0:
	public String command;
	Map<String, String> mapDfrs;	
	static HierarchicalINIConfiguration iniConf;
	String dfrId;  //  @jve:decl-index=0:
	String ipAddress;
	String selectedIpAddress;
	String selectedCommand = "HELP";
	int port;
	static String specialCommand = "";
	String finalResult= "";  //  @jve:decl-index=0:

	
	/**
	 * This is the default constructor
	 */
	public TestVisualE() {
		super();
		try {
			iniConf = new HierarchicalINIConfiguration("station.properties");
			mapDfrs = new HashMap<String, String>();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initialize();

	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(965, 664);
		this.setResizable(false);
		this.setContentPane(getJContentPane());
		this.setTitle("JFrame");
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			lblTitle = new JLabel();
			lblTitle.setBounds(new Rectangle(295, 15, 454, 30));
			lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
			lblTitle.setFont(new Font("Dialog", Font.BOLD, 18));
			lblTitle.setText("M9k DFR Debugger");
			lblResult = new JLabel();
			lblResult.setFont(new Font("Dialog", Font.BOLD, 12));
			lblResult.setBounds(new Rectangle(3, 204, 116, 28));
			lblResult.setHorizontalAlignment(SwingConstants.LEFT);
			lblResult.setText("Result");
			lblCommands = new JLabel();
			lblCommands.setFont(new Font("Dialog", Font.BOLD, 12));
			lblCommands.setBounds(new Rectangle(238, 132, 169, 27));
			lblCommands.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			lblCommands.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCommands.setText("Enter or Select a command");
			lblDfrs = new JLabel();
			lblDfrs.setFont(new Font("Dialog", Font.BOLD, 12));
			lblDfrs.setBounds(new Rectangle(291, 86, 113, 30));
			lblDfrs.setHorizontalAlignment(SwingConstants.RIGHT);
			lblDfrs.setText("Select a DFR");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(lblDfrs, null);
			jContentPane.add(lblCommands, null);
			jContentPane.add(getLstDfrs(), null);
			jContentPane.add(getLstCommands(), null);
			jContentPane.add(getBtnExecute(), null);
			jContentPane.add(lblResult, null);
			jContentPane.add(getJScrollPane(), null);
			jContentPane.add(lblTitle, null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes lstDfrs	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getLstDfrs() {
		if (lstDfrs == null) {
			lstDfrs = new JComboBox();
			lstDfrs.setBounds(new Rectangle(435, 88, 175, 29));
			lstDfrs.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
					JComboBox cb = (JComboBox)e.getSource();
			        String dfrId = (String)cb.getSelectedItem();
			        selectedIpAddress = dfrId.split("-")[1];
				}
			});
			Set<String> dfrs = iniConf.getSections();
			for (String dfr : dfrs) {
				if (!dfr.toUpperCase().startsWith("DFR"))
				{
					continue;
				}
				System.out.println("[" + dfr + "]");
				dfrId = iniConf.getString(dfr + ".id");
				ipAddress = iniConf.getString(dfr + ".ip-address");
				port = iniConf.getInt(dfr + ".port");
//				System.out.println("Id " + iniConf.getString(dfr + ".id"));
//				System.out.println("Ip address " + ipAddress);
//				System.out.println("Port " + port);
				lstDfrs.addItem(dfr+" - "+ipAddress);

			}
		}
		return lstDfrs;
	}

	/**
	 * This method initializes lstCommands	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getLstCommands() {
		if (lstCommands == null) {
			lstCommands = new JComboBox();
			lstCommands.setBounds(new Rectangle(435, 136, 309, 25));
			lstCommands.setEditable(true);
			lstCommands.addItem("HELP");
			lstCommands.setSelectedIndex(0);
			lstCommands.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
					JComboBox cb = (JComboBox)e.getSource();
					selectedCommand = (String)cb.getSelectedItem();
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try
					{
						processCommand(selectedCommand);
					}
					catch (Exception ex) {
						taResult.setText(ex.toString());
					}
					finally
					{
						setCursor(Cursor.getDefaultCursor());
					}
				}
			});
		}
		return lstCommands;
	}

	/**
	 * This method initializes btnExecute	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnExecute() {
		if (btnExecute == null) {
			btnExecute = new JButton();
			btnExecute.setBounds(new Rectangle(766, 138, 106, 23));
			btnExecute.setText("Execute");
			btnExecute.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try
					{
						processCommand(selectedCommand);
					}
					catch (Exception ex) {
						taResult.setText(ex.toString());
					}
					finally
					{
						setCursor(Cursor.getDefaultCursor());
					}
				}
			});
		}
		return btnExecute;
	}

	/**
	 * This method initializes taResult	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getTaResult() {
		if (taResult == null) {
			taResult = new JTextArea();
			taResult.setWrapStyleWord(true);
		}
		return taResult;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBounds(new Rectangle(3, 233, 946, 393));
			jScrollPane.setViewportView(getTaResult());
		}
		return jScrollPane;
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                new TestVisualE().setVisible(true);
            }
        });
	}
	
	private void processCommand(String command)
	{
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
			addToListOfCommands(command);
			taResult.setText(finalResult);
			finalResult="";

//			if (!lstHistoryCommands.contains(command) && isCommandSuccessfull())
//			{
//				lstHistoryCommands.add(command);
//			}
		} catch (M9000Exception e) {
//			e.printStackTrace();
			taResult.setText(e.toString());
			finalResult="";
		}

	}
	
	public void executeCommand(String dfrCmd) throws M9000Exception
	{
		String result;

			try
			{
				result = send(selectedIpAddress, port, dfrCmd);
				if (!result.equalsIgnoreCase("Command not found"))
				{
					processResult(result);
				}
				else
				{
					finalResult = result;
				}
			}
			catch (M9000Exception e) {
//				result = "DFR "+ipAddress+" is down";
				throw e;
			}


	}
	private String send(String destIp, int port, String command) throws M9000Exception {
		
		String result;
//		System.out.println("Ip address " + destIp);
//		System.out.println("Port " + port);
//		System.out.println("Command "+command);
		commandClient = new M9kStationCommandClient(destIp, port, command);
		result = commandClient.sendAndReceive();
		
		return result;
	}

	private void processResult(String result)
	{
		StringTokenizer strTok;
			if (selectedCommand.equalsIgnoreCase("boarddetect"))
			{
				finalResult = printBoardDetect(Long.parseLong(result));
			}
			else if (selectedCommand.equalsIgnoreCase("monitor") || selectedCommand.equalsIgnoreCase("dfrinfo"))
			{
				strTok = new StringTokenizer(result, ",");
				while (strTok.hasMoreElements()) {
					String dfrParameter = (String) strTok.nextElement();
					System.out.println(dfrParameter.trim());
					finalResult += dfrParameter.trim()+M9kStationConstants.NEWLINE;
				}
			}
			else
			{
				System.out.println(result);
				finalResult = result;
			}
			
	}

	private String printBoardDetect(long brddet)
	{
		StringBuffer result = new StringBuffer();
		brddet = brddet & 0xffffffffL;
		
		for (int i = 0; i < 4; i++) {
			System.out.println("Analog["+i+"]\t: "+(( brddet & (1 << (26-i))) > 0  ? "present" : "empty" ) );
			result.append("Analog["+i+"]\t: "+(( brddet & (1 << (26-i))) > 0  ? "present" : "empty" ) );
			result.append(M9kStationConstants.NEWLINE);
			
		}
		System.out.println();
		for (int i = 0; i < 4; i++) {
			System.out.println("Event["+i+"]\t: "+(( brddet & (1 << (22-i))) > 0 ? "present" : "empty" ) );
			result.append("Event["+i+"]\t: "+(( brddet & (1 << (22-i))) > 0 ? "present" : "empty" ) );
			result.append(M9kStationConstants.NEWLINE);
		}
		
		return result.toString();
	}


	private void processVerboseCommand()
	{
		String[] strSplit = command.split(" "); 
		if (strSplit.length > 1)
		{
			specialCommand=strSplit[0]+", value="+strSplit[1];
		}
	}
	
	private void addToListOfCommands(Object o) {
		int size = lstCommands.getModel().getSize();
		boolean flgExist = false;
		for (int i = 0; i < size; i++) {
			Object obj = lstCommands.getModel().getElementAt(i);
			if (obj.equals(o)) {
				flgExist = true;
				break;
			}
		}
		if (!flgExist && (!finalResult.equalsIgnoreCase("Command not found")))
		{
			lstCommands.addItem(o);
		}
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

}  //  @jve:decl-index=0:visual-constraint="0,-1"
