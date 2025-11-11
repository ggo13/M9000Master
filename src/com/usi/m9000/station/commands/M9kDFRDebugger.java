package com.usi.m9000.station.commands;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.UnknownHostException;
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
import javax.swing.Timer;
import javax.swing.UIManager;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kStationConstants;

public class M9kDFRDebugger extends JFrame {

	private static final long serialVersionUID = 1L;
	int delay = 1000; //milliseconds
	private JPanel jContentPane = null;
	private JLabel lblDfrs = null;
	private JLabel lblCommands = null;
//	private JLabel lblFileCommands = null;
	private JComboBox lstDfrs = null;
	private JComboBox lstCommands = null;
	private JButton btnExecute = null;
	private JButton btnContExecute = null;
	private JLabel lblResult = null;
	private JTextArea taResult = null;
	private JScrollPane jScrollPane = null;
	private JLabel lblTitle = null;
	private JPanel pnlTitle;
	private JPanel pnlSelectDfrs;
	private JPanel pnlCommands;
//	private JPanel pnlFileCommands;
	private JPanel pnlResult;

//	private JTextArea taFileCommands = null;
//	private JScrollPane jspFileCommands = null;

	M9kStationCommandClient commandClient;  //  @jve:decl-index=0:
//	public String command;
	Map<String, String> mapDfrs;	
	static HierarchicalINIConfiguration iniConf;
	String dfrId;  //  @jve:decl-index=0:
	String ipAddress;
	String selectedIpAddress;
	String selectedCommand = "HELP";  //  @jve:decl-index=0:
	int port;
	static String specialCommand = "";  //  @jve:decl-index=0:
	String finalResult= "";  //  @jve:decl-index=0:
	String action = ""; 
	String hostName;
	String hostIp;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kDFRDebugger.class);
	
	ActionListener taskPerformer = null;
	static Timer timer = null;
	/**
	 * This is the default constructor
	 */
	public M9kDFRDebugger() {
		super("M9k Dfr Debugger");
		try {
			iniConf = new HierarchicalINIConfiguration("station.properties");
			mapDfrs = new HashMap<String, String>();
			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
			hostName = localMachine.getHostName();
			hostIp = localMachine.getHostAddress();

		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in reading station.properties file "+e);
		} catch (UnknownHostException e) {
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
		this.setSize(965, 1000);
		this.setResizable(true);
		this.setContentPane(getJContentPane());
		this.setTitle("M9k DFR Debugger @ "+hostName +"("+hostIp+" ) by "+System.getProperty("user.name"));
		this.addWindowListener(new  WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
//				System.out.println("Closing...");
				logger.debug("Exiting the application.");
				System.exit(0);
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	/**
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	@SuppressWarnings("unused")
	private JPanel getBackupJContentPane() {
		if (jContentPane == null) {
			lblTitle = new JLabel();
			lblTitle.setBounds(new Rectangle(295, 15, 454, 30));
			lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
			lblTitle.setFont(new Font("Dialog", Font.BOLD, 18));
			lblTitle.setText("M9k DFR Debugger");
			lblResult = new JLabel();
			lblResult.setFont(new Font("Dialog", Font.BOLD, 12));
			lblResult.setBounds(new Rectangle(3, 116, 116, 28));
			lblResult.setHorizontalAlignment(SwingConstants.LEFT);
			lblResult.setText("Result");
			lblCommands = new JLabel();
			lblCommands.setFont(new Font("Dialog", Font.BOLD, 12));
			lblCommands.setBounds(new Rectangle(238, 84, 169, 27));
			lblCommands.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			lblCommands.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCommands.setText("Enter or Select a command");
			lblDfrs = new JLabel();
			lblDfrs.setFont(new Font("Dialog", Font.BOLD, 12));
			lblDfrs.setBounds(new Rectangle(291, 50, 113, 30));
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
	 * This method initializes jContentPane	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			pnlTitle = new JPanel();
			lblTitle = new JLabel();
//			lblTitle.setPreferredSize(new Dimension(15, 30));
			lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
			lblTitle.setFont(new Font("Dialog", Font.BOLD, 18));
			lblTitle.setText("M9k DFR Debugger");
			pnlTitle.add(lblTitle);
			
			pnlResult = new JPanel();
			pnlResult.setLayout(new BorderLayout());
			lblResult = new JLabel();
			lblResult.setFont(new Font("Dialog", Font.BOLD, 12));
			lblResult.setHorizontalAlignment(SwingConstants.LEFT);
			lblResult.setText("Result");
			pnlResult.add(lblResult, BorderLayout.NORTH);
			pnlResult.add(getJScrollPane(), BorderLayout.CENTER);
			
			pnlCommands = new JPanel();
			pnlCommands.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
			lblCommands = new JLabel();
			lblCommands.setFont(new Font("Dialog", Font.BOLD, 12));
			lblCommands.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			lblCommands.setHorizontalAlignment(SwingConstants.RIGHT);
			lblCommands.setText("Enter or Select a command");
			pnlCommands.add(lblCommands);
			pnlCommands.add(getLstCommands());
			pnlCommands.add(getBtnExecute());
			pnlCommands.add(getContBtnExecute());
			
//			pnlFileCommands = new JPanel();
//			pnlFileCommands.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
//			lblFileCommands = new JLabel();
//			lblFileCommands.setFont(new Font("Dialog", Font.BOLD, 12));
//			lblFileCommands.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
//			lblFileCommands.setHorizontalAlignment(SwingConstants.RIGHT);
//			lblFileCommands.setText("Select a File ");
//			pnlFileCommands.add(lblCommands);
//			pnlFileCommands.add(getJspFileCommands());
			
			pnlSelectDfrs = new JPanel();
			pnlSelectDfrs.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
			lblDfrs = new JLabel();
			lblDfrs.setFont(new Font("Dialog", Font.BOLD, 12));
			lblDfrs.setHorizontalAlignment(SwingConstants.RIGHT);
			lblDfrs.setText("Select a DFR");
			pnlSelectDfrs.add(lblDfrs);
			pnlSelectDfrs.add(getLstDfrs());
			JPanel pnlNorth = new JPanel();
			pnlNorth.setLayout(new BorderLayout());
			pnlNorth.add(pnlTitle, BorderLayout.NORTH);
			pnlNorth.add(pnlSelectDfrs, BorderLayout.CENTER);
			pnlNorth.add(pnlCommands, BorderLayout.SOUTH);
			
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(pnlNorth, BorderLayout.NORTH);
			jContentPane.add(pnlResult, BorderLayout.CENTER);
			
//			jContentPane.add(lblDfrs, null);
//			jContentPane.add(lblCommands, null);
//			jContentPane.add(getLstDfrs(), null);
//			jContentPane.add(getLstCommands(), null);
//			jContentPane.add(getBtnExecute(), null);
//			jContentPane.add(lblResult, null);
//			jContentPane.add(getJScrollPane(), null);
//			jContentPane.add(lblTitle, null);
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
//			lstDfrs.setBounds(new Rectangle(435, 50, 175, 29));
			lstDfrs.setPreferredSize(new Dimension(250,30));
			lstDfrs.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
//					System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
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
//				System.out.println("[" + dfr + "]");
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
//			lstCommands.setBounds(new Rectangle(435, 84, 309, 25));
			lstCommands.setPreferredSize(new Dimension(250, 25));
			lstCommands.setEditable(true);
			// The following code helps to avoid execution of command for UP/DOWN arrows
			lstCommands.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
			lstCommands.addItem("HELP");
			lstCommands.setSelectedIndex(0);
			lstCommands.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
//						System.out.println("About to change it to wait cursor "+e.getID());
//					System.out.println("(e.getModifiers() & InputEvent.BUTTON1_DOWN_MASK) "+ e.getModifiers() +" get source "+e.toString()+" param string "+e.paramString());
					if (e.getActionCommand().equalsIgnoreCase("comboBoxEdited") || (e.getActionCommand().equalsIgnoreCase("comboBoxChanged") && e.getModifiers() != 0))
					{
					action = e.getActionCommand();
						JComboBox cb = (JComboBox)e.getSource();
						selectedCommand = (String)cb.getSelectedItem();
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						cb.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						try
						{
							processCommand(selectedCommand);
//							System.out.println("Selected command "+selectedCommand);
						}
						catch (Exception ex) {
							taResult.setText(ex.getCause().toString());
							logger.error("Error in processing the command "+selectedCommand,ex);
							return;
						}
						finally
						{
							cb.setCursor(Cursor.getDefaultCursor());
							setCursor(Cursor.getDefaultCursor());
						}
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
//			btnExecute.setBounds(new Rectangle(766, 84, 106, 23));
			btnExecute.setPreferredSize(new Dimension(100,25));
			btnExecute.setText("Execute");
			btnExecute.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
//				System.out.println("\tEntered action performed ");
//				System.out.println("(e.getModifiers() & InputEvent.BUTTON1_DOWN_MASK) "+ e.getModifiers() +" get source "+e.toString()+" param string "+e.paramString());
				if (action.equalsIgnoreCase("comboBoxEdited") && e.getModifiers() != 0)
				{
//					System.out.println("no action taken");
					action = "";
					return;
				}
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try
					{
						processCommand(selectedCommand);
					}
					catch (Exception ex) {
						taResult.setText(ex.getCause().toString());
						logger.error("Error in processing the command "+selectedCommand,ex);
						return;
					}
					finally
					{
						setCursor(Cursor.getDefaultCursor());
					}
				}
			});
			
			btnExecute.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER )
					{
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try
					{
						processCommand(selectedCommand);
					}
					catch (Exception ex) {
						taResult.setText(ex.getCause().toString());
						logger.error("Error in processing the command "+selectedCommand,ex);
						return;
					}
					finally
					{
						setCursor(Cursor.getDefaultCursor());
					}
					}
					
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
		}
		return btnExecute;
	}

	private JButton getContBtnExecute() {
		if (btnContExecute == null) {
			btnContExecute = new JButton();
//			btnContExecute.setBounds(new Rectangle(766, 84, 106, 23));
			btnContExecute.setPreferredSize(new Dimension(100,25));
			btnContExecute.setText("Continuous");
			btnContExecute.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
//				System.out.println("\tEntered action performed "+e.getActionCommand());
//				System.out.println("(e.getModifiers() & InputEvent.BUTTON1_DOWN_MASK) "+ e.getModifiers() +" get source "+e.toString()+" param string "+e.paramString());
					if (e.getActionCommand().equalsIgnoreCase("STOP"))
					{
						timer.stop();
						btnContExecute.setText("Continuous");
						
						return;
					}
					try
					{
						btnContExecute.setText("STOP");
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						timer = new Timer(delay, taskPerformer);
						timer.start();
					}
					catch (Exception ex) {
						taResult.setText(ex.getCause().toString());
						logger.error("Error in starting a timer ",ex);
						return;
					}
					finally
					{
						setCursor(Cursor.getDefaultCursor());
					}
				}
			});
			
			 taskPerformer = new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
						try
						{
								setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
								processCommand(selectedCommand);
								setCursor(Cursor.getDefaultCursor());
						}
						catch (Exception ex) {
							taResult.setText(ex.getCause().toString());
							logger.error("Error in processing the command "+selectedCommand,ex);
							return;
						}
						finally
						{
							setCursor(Cursor.getDefaultCursor());
						}
					}

			  };
			btnContExecute.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER )
					{
						if (((JButton)e.getSource()).getActionCommand().equalsIgnoreCase("STOP"))
						{
							timer.stop();
							btnContExecute.setText("Repeated Execute");
							return;
						}
					try
					{
						btnContExecute.setText("STOP");
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						timer = new Timer(delay, taskPerformer);
						timer.start();
					}
					catch (Exception ex) {
						taResult.setText(ex.getCause().toString());
						logger.error("Error in Starting the timer ",ex);
						return;
					}
					finally
					{
						setCursor(Cursor.getDefaultCursor());
					}
					}
					
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
			});
		}
		return btnContExecute;
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
			taResult.setFont(new Font("Courier", Font.PLAIN, 12));
		}
		return taResult;
	}

//	private JTextArea getTaFileCommands() {
//		if (taFileCommands == null) {
//			taFileCommands = new JTextArea();
//			taFileCommands.setWrapStyleWord(true);
//			taFileCommands.setFont(new Font("Courier", Font.PLAIN, 12));
//		}
//		return taResult;
//	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
//			jScrollPane.setBounds(new Rectangle(3, 150, 946, 793));
			jScrollPane.setPreferredSize(new Dimension(200,700));
			jScrollPane.setViewportView(getTaResult());
		}
		return jScrollPane;
	}

//	private JScrollPane getJspFileCommands() {
//		if (jspFileCommands == null) {
//			jspFileCommands = new JScrollPane();
////			jScrollPane.setBounds(new Rectangle(3, 150, 946, 793));
//			jspFileCommands.setPreferredSize(new Dimension(200,700));
//			jspFileCommands.setViewportView(getTaFileCommands());
//		}
//		return jScrollPane;
//	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                new M9kDFRDebugger().setVisible(true);
            }
        });
	}
	
	private void processCommand(String command)
	{
		logger.debug("Command to process "+command);
		specialCommand = "";
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
			taResult.setText("Calibrating...");
			logger.debug("Debug Calibrating..."+specialCommand+ " or normal command "+command);
			if (timer!= null && timer.isRunning())
			{
				timer.stop();
				btnContExecute.setText("Continuous");
			}
		} 
		else if (command.toUpperCase().startsWith("RESTART"))
		{
			if (timer!= null && timer.isRunning())
			{
				timer.stop();
				btnContExecute.setText("Continuous");
			}

		}
		try {
			if (specialCommand.isEmpty())
			{
				logger.debug("About to execute normal cmd "+command);
				executeCommand(command);
			}
			else
			{
				logger.debug("About to execute special command "+specialCommand);
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
			taResult.setText(e.getCause().toString());
			logger.error("Error in processing the command ",e);
			finalResult="";
		}

	}
	
	public void executeCommand(String dfrCmd) throws M9000Exception
	{
		String result;

			try
			{
				logger.debug("Executing cmd "+dfrCmd);
				result = send(selectedIpAddress, port, dfrCmd);
				logger.debug("result "+result);
				if (result != null && !result.equalsIgnoreCase("Command not found"))
				{
					processResult(result);
				}
				else
				{
					if (result != null)
					{
						finalResult = result;
					}
					else
					{
						finalResult = "No response from the dfr";
					}
				}
			}
			catch (M9000Exception e) {
//				result = "DFR "+ipAddress+" is down";
				logger.error("Error Occured"+e);
				finalResult = e.getCause().toString();
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
//				finalResult = printBoardDetect(Long.parseLong(result));
			}
			else if (selectedCommand.equalsIgnoreCase("monitor") || selectedCommand.equalsIgnoreCase("dfrinfo"))
			{
				strTok = new StringTokenizer(result, ",");
				while (strTok.hasMoreElements()) {
					String dfrParameter = (String) strTok.nextElement();
					if (dfrParameter.toUpperCase().indexOf("DFRUPTIME") != -1)
					{
						String dfrUpTime[] = dfrParameter.split("=");
						long upTime = Long.parseLong(dfrUpTime[1]);
						int days = (int)(upTime/(24*3600));
						int hours = (int)(upTime/3600) - (days * 24*3600);
						int minutes = (int)(upTime/60)- (hours * 60);
						int seconds = (int)(upTime % 60);
						String calculatedUpTime = "";
//						System.out.println((int)(upTime/(24*3600))+" Days " + (int)(upTime/3600)+" Hours " +(int)(upTime/60)+" Minutes "+(int)(upTime % 60)+" Seconds");
						if (days > 0)
						{
							calculatedUpTime=days+" Days ";
						}
						if (hours > 0)
						{
							calculatedUpTime+=hours+" Hours ";
						}
						if (minutes > 0)
						{
							calculatedUpTime+=minutes+" Minutes ";
						}
						calculatedUpTime+=seconds+" Seconds ";
						dfrParameter="DfrUpTime="+calculatedUpTime+ "[Total seconds = "+ upTime +"]";
					}
//					System.out.println(dfrParameter.trim());
					finalResult += dfrParameter.trim()+M9kStationConstants.NEWLINE;
				}
			}
			else
			{
//				System.out.println(result);
				finalResult = result;
			}
		System.out.println(result);	
	}

	private String printBoardDetect(long brddet)
	{
		StringBuffer result = new StringBuffer();
		brddet = brddet & 0xffffffffL;
		
		for (int i = 0; i < 4; i++) {
//			System.out.println("Analog["+i+"]\t: "+(( brddet & (1 << (26-i))) > 0  ? "present" : "empty" ) );
			result.append("Analog["+i+"]\t: "+(( brddet & (1 << (27-i))) > 0  ? "present" : "empty" ) );
			result.append(M9kStationConstants.NEWLINE);
			
		}
		System.out.println();
		for (int i = 0; i < 4; i++) {
//			System.out.println("Event["+i+"]\t: "+(( brddet & (1 << (22-i))) > 0 ? "present" : "empty" ) );
			result.append("Event["+i+"]\t: "+(( brddet & (1 << (23-i))) > 0 ? "present" : "empty" ) );
			result.append(M9kStationConstants.NEWLINE);
		}
		
		return result.toString();
	}


	private void processVerboseCommand()
	{
		String[] strSplit = selectedCommand.split(" "); 
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
			if (obj.toString().equalsIgnoreCase(o.toString())) {
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
