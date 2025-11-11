package com.usi.m9000.station.commands;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.net.M9kStationCommandClient;
import com.usi.m9000.station.util.M9kStationConstants;

public class M9kStationDebugTestDFR extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	int delay = 1000; //milliseconds
	int scriptDelay = 20000;
	private JPanel jContentPane = null;
	private JLabel lblDfrs = null;
	private JLabel lblCommands = null;
	private JLabel lblFileCommands = null;
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
	private JPanel pnlFileCommands;
	private JPanel pnlResult;
	private JPanel pnlFileResult;
	private JPanel pnlNorth;

	private JPanel pnlSelectFile;
	private JButton btnSelectFile;
	private JButton btnLoadFile;
	
	private JTextArea taSelectedFileName;
	private JButton btnExecuteFile;
	private JButton btnTestContRun;
	private JFileChooser fc;
	private static File selectedFile; 
	private static String fileContent;
	JSplitPane splitPane;
	
	private JMenuBar menuBar;
	JMenu menuFile;
	JMenuItem fileOpenMenuItem;
	JMenuItem fileSaveMenuItem;
	JMenuItem fileSaveAsMenuItem;
	JMenuItem fileExitMenuItem;
	
	JMenu menuEdit;
	JRadioButtonMenuItem rbDebugMenuItem;
	JRadioButtonMenuItem rbTestMenuItem;

	private JTextArea taFileCommands = null;
	private JScrollPane jspFileCommands = null;

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
	String userHome;
	Dimension screenDimension;
	static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kStationDebugTestDFR.class);
	
	ActionListener taskPerformer = null;
	ActionListener tstRunTaskPerformer = null;
	Timer timer = null;
	private StringBuffer stationProperty;
	private static int noOfDfrs = -1;
	/**
	 * This is the default constructor
	 */
	public M9kStationDebugTestDFR() {
		super("M9k Dfr Debugger");
		try {
			if (noOfDfrs >0)
			{
				createNewStationProperty();
			}
			screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
			iniConf = new HierarchicalINIConfiguration("station-debug.properties");
			mapDfrs = new HashMap<String, String>();
			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
			hostName = localMachine.getHostName();
			hostIp = localMachine.getHostAddress();
			userHome = System.getProperty( "user.home" );
			logger.debug("userHome "+userHome);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error in reading station-debug.properties file "+e);
//			System.exit(ERROR);
			hostName="unknown";
			hostIp = "127.0.0.1";
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Network unavailable");
			logger.error("Network unavailable");
			e.printStackTrace();
//			System.exit(ERROR);
			hostName="unknown";
			hostIp = "127.0.0.1";
		}
		catch(Exception e)
		{
			System.out.println("Network unavailable");
			e.printStackTrace();
			hostName="unknown";
			hostIp = "127.0.0.1";
		}
		initialize();

	}

	private void createNewStationProperty() {
		int ipAddressCountStart=101;
		stationProperty = new StringBuffer("[PowerSupply]");
		stationProperty.append(M9kStationConstants.NEWLINE);
		stationProperty.append("id=1");
		stationProperty.append(M9kStationConstants.NEWLINE);
		stationProperty.append("ip-address=192.168.1.100");
		stationProperty.append(M9kStationConstants.NEWLINE);
		stationProperty.append("port=9978");
		stationProperty.append(M9kStationConstants.NEWLINE);
		
		for (int i = 0; i < M9kStationDebugTestDFR.noOfDfrs; i++) {
			stationProperty.append("[DFR"+(i+1) +"]");
			stationProperty.append(M9kStationConstants.NEWLINE);
			stationProperty.append("id="+(i+2));// power supply id is already 1
			stationProperty.append(M9kStationConstants.NEWLINE);
			stationProperty.append("ip-address=192.168.1."+ipAddressCountStart++);
			stationProperty.append(M9kStationConstants.NEWLINE);
			stationProperty.append("port=9978");
			stationProperty.append(M9kStationConstants.NEWLINE);		
		}
		stationProperty.append("[localhost]");
		stationProperty.append(M9kStationConstants.NEWLINE);
		stationProperty.append("id="+(M9kStationDebugTestDFR.noOfDfrs+2));
		stationProperty.append(M9kStationConstants.NEWLINE);
		stationProperty.append("ip-address=localhost");
		stationProperty.append(M9kStationConstants.NEWLINE);
		stationProperty.append("port=9978");
		stationProperty.append(M9kStationConstants.NEWLINE);
		System.out.println("Station property constructed "+stationProperty);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File("../lib/station-debug.properties")));
			bw.write(stationProperty.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
			try
			{
				bw = new BufferedWriter(new FileWriter(new File("station-debug.properties")));
				bw.write(stationProperty.toString());
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
			System.out.println("Unable to create station.property file. Create manually in /home/dfr/m9k/M9kDfrDebug/lib/station-debug.properties");
		}
		finally{
			if (bw != null)
			{
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
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

		menuBar = new JMenuBar();
		createAndAddMenu();
		fc = new JFileChooser(userHome+"/M9K");
//		File selectedFile = null;
		fc.setFileFilter(new TestFileFilter());
		fc.setAcceptAllFileFilterUsed(false);
		
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
				System.out.println("Closing...");
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
			pnlResult.setPreferredSize(new Dimension(200,500));
			pnlResult.setMinimumSize(new Dimension((int)screenDimension.getWidth(),(int)screenDimension.getHeight()/4));
			pnlFileResult = new JPanel();
			pnlFileResult.setLayout(new BorderLayout());
			splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,getJspFileCommands(), pnlResult);
			splitPane.setResizeWeight(0.5);
			splitPane.setOneTouchExpandable(true);
			splitPane.setPreferredSize(new Dimension(500,1000));
//			splitPane.setPreferredSize(new Dimension(200, 700));
			pnlFileResult.add(splitPane, BorderLayout.CENTER);
			
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
			
			pnlFileCommands = new JPanel();
			pnlFileCommands.setLayout(new BorderLayout());
			lblFileCommands = new JLabel();
			lblFileCommands.setFont(new Font("Dialog", Font.BOLD, 12));
			lblFileCommands.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			lblFileCommands.setHorizontalAlignment(SwingConstants.LEFT);
			lblFileCommands.setText("Select a File ");
			
			btnSelectFile = new JButton("Browse");
			btnSelectFile.addActionListener(this);

			btnLoadFile = new JButton("Load");
			btnLoadFile.addActionListener(this);


			taSelectedFileName = new JTextArea();
			taSelectedFileName.setText("Type and load a file or browse...");
			taSelectedFileName.setSelectionStart(0);
			taSelectedFileName.setSelectionEnd(taSelectedFileName.getText().length());
			taSelectedFileName.requestFocus();
			taSelectedFileName.setPreferredSize(new Dimension(250, 25));
			taSelectedFileName.setAlignmentY(CENTER_ALIGNMENT);
			
			pnlSelectFile = new JPanel();
			pnlSelectFile.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
			pnlSelectFile.add(lblFileCommands);
			pnlSelectFile.add(taSelectedFileName);
			
			btnExecuteFile = new JButton("Run");
			btnExecuteFile.setEnabled(false);
			btnExecuteFile.addActionListener(this);
			

			pnlSelectFile.add(btnLoadFile);
			pnlSelectFile.add(btnSelectFile);
			pnlSelectFile.add(btnExecuteFile);
			pnlSelectFile.add(getBtnContTestRun());
			
			pnlFileCommands.add(pnlSelectFile, BorderLayout.NORTH);
//			pnlFileCommands.add(getJspFileCommands(),BorderLayout.CENTER);
			
			pnlSelectDfrs = new JPanel();
			pnlSelectDfrs.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
			lblDfrs = new JLabel();
			lblDfrs.setFont(new Font("Dialog", Font.BOLD, 12));
			lblDfrs.setHorizontalAlignment(SwingConstants.RIGHT);
			lblDfrs.setText("Select a DFR");
			pnlSelectDfrs.add(lblDfrs);
			pnlSelectDfrs.add(getLstDfrs());
			pnlNorth = new JPanel();
			pnlNorth.setLayout(new BorderLayout());
			pnlNorth.add(pnlTitle, BorderLayout.NORTH);
			pnlNorth.add(pnlSelectDfrs, BorderLayout.CENTER);
//			pnlNorth.add(pnlCommands, BorderLayout.SOUTH);
//			pnlNorth.add(pnlFileCommands, BorderLayout.SOUTH);
			
			JPanel pnlBoth = new JPanel();
			pnlBoth.setLayout(new BorderLayout());
			pnlBoth.add(pnlCommands, BorderLayout.NORTH);
			pnlBoth.add(pnlFileCommands, BorderLayout.CENTER);
			pnlNorth.add(pnlBoth, BorderLayout.SOUTH);
			
			
			pnlCommands.setVisible(true);
			pnlFileCommands.setVisible(false);
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(pnlNorth, BorderLayout.NORTH);
//			jContentPane.add(pnlResult, BorderLayout.CENTER);
			jContentPane.add(pnlFileResult, BorderLayout.CENTER);
			pnlFileResult.setVisible(true);
			jspFileCommands.setVisible(false);
//			pnlResult.setVisible(true);
			
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
					JComboBox cb = (JComboBox)e.getSource();
			        String dfrId = (String)cb.getSelectedItem();
//			        selectedIpAddress = dfrId.split("-")[1];
			        selectedIpAddress = dfrId.substring(dfrId.lastIndexOf("-")+1);
				}
			});
			Set<String> dfrs = iniConf.getSections();
			for (String dfr : dfrs) {
//				if (!dfr.toUpperCase().startsWith("DFR") && !dfr.equalsIgnoreCase("PowerSupply"))
//				{
//					continue;
//				}
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
				System.out.println("\tEntered action performed "+e.getActionCommand()+" Selected command "+selectedCommand);
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

	private JButton getBtnContTestRun() {
		if (btnTestContRun == null) {
			btnTestContRun = new JButton("Cont. Run");
			btnTestContRun.setEnabled(false);
//			btnContExecute.setBounds(new Rectangle(766, 84, 106, 23));
			btnTestContRun.setPreferredSize(new Dimension(100,25));
			btnTestContRun.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
//				System.out.println("\tEntered action performed "+e.getActionCommand());
//				System.out.println("(e.getModifiers() & InputEvent.BUTTON1_DOWN_MASK) "+ e.getModifiers() +" get source "+e.toString()+" param string "+e.paramString());
					if (e.getActionCommand().equalsIgnoreCase("STOP"))
					{
						timer.stop();
						btnTestContRun.setText("Continuous");
						
						return;
					}
					try
					{
						btnTestContRun.setText("STOP");
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						timer = new Timer(scriptDelay, tstRunTaskPerformer);
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
			
			tstRunTaskPerformer = new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
						try
						{
							executeScript();
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
			btnTestContRun.addKeyListener(new KeyListener() {
				
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
							btnTestContRun.setText("Repeated Execute");
							return;
						}
					try
					{
						btnTestContRun.setText("STOP");
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						timer = new Timer(delay, tstRunTaskPerformer);
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
		return btnTestContRun;
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

	private JTextArea getTaFileCommands() {
		if (taFileCommands == null) {
			taFileCommands = new JTextArea();
			taFileCommands.setWrapStyleWord(true);
			taFileCommands.setFont(new Font("Courier", Font.PLAIN, 12));
			taFileCommands.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent arg0) {
					if (taFileCommands.getText().isEmpty())
					{
						btnExecuteFile.setEnabled(false);
						btnTestContRun.setEnabled(false);
					}
					else
					{
						btnExecuteFile.setEnabled(true);
						btnTestContRun.setEnabled(true);
					}
					
				}
			});
		}
		return taFileCommands;
	}

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
//			jScrollPane.setMinimumSize(new Dimension(200,400));
			jScrollPane.setViewportView(getTaResult());
		}
		return jScrollPane;
	}

	private JScrollPane getJspFileCommands() {
		if (jspFileCommands == null) {
			jspFileCommands = new JScrollPane();
//			jScrollPane.setBounds(new Rectangle(3, 150, 946, 793));
			jspFileCommands.setPreferredSize(new Dimension(200,500));
			jspFileCommands.setMinimumSize(new Dimension((int)screenDimension.getWidth(),(int)screenDimension.getHeight()/4));
			jspFileCommands.setViewportView(getTaFileCommands());
		}
		return jspFileCommands;
	}

	public static void main(String[] args)
	{
		if (args.length == 1)
		{
			try
			{
				M9kStationDebugTestDFR.noOfDfrs = Integer.parseInt(args[0]);
			}
			catch (Exception e) {
				
				System.out.println("Invalid argument. Starting debugger as default");
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                M9kStationDebugTestDFR m9kdebug = new M9kStationDebugTestDFR();
                m9kdebug.setVisible(true);
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
				logger.debug("About to executre normal cmd "+command);
				executeCommand(command);
			}
			else
			{
				logger.debug("About to execute command "+specialCommand);
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
//			if (selectedCommand.equalsIgnoreCase("boarddetect"))
//			{
////				finalResult = printBoardDetect(Long.parseLong(result));
//			}
			if (selectedCommand.equalsIgnoreCase("monitor") || selectedCommand.equalsIgnoreCase("dfrinfo"))
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

	private void createAndAddMenu()
	{
		menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuFile.getAccessibleContext().setAccessibleDescription(
		        "Basic functionalities to start with");
		menuBar.add(menuFile);
		createFileMenuItems();
		
		menuEdit = new JMenu("Edit");
		menuEdit.setMnemonic(KeyEvent.VK_E);
		menuEdit.getAccessibleContext().setAccessibleDescription(
		        "some edit functionalities");
		menuBar.add(menuEdit);
		createEditMenuItems();
		this.setJMenuBar(menuBar);
		hideMenuItems();
	}
	
	private void hideMenuItems()
	{
		fileOpenMenuItem.setVisible(false);
		fileSaveMenuItem.setVisible(false);
		fileSaveAsMenuItem.setVisible(false);
	}

	private void showMenuItems()
	{
		fileOpenMenuItem.setVisible(true);
		fileSaveMenuItem.setVisible(true);
		fileSaveAsMenuItem.setVisible(true);
	}

	
	private void createFileMenuItems()
	{
		// Open Menu
		fileOpenMenuItem = new JMenuItem("Open");
		fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		fileOpenMenuItem.getAccessibleContext().setAccessibleDescription(
		        "Save the Script");
		fileOpenMenuItem.addActionListener(this);
		menuFile.add(fileOpenMenuItem);

		// Save Menu
		fileSaveMenuItem = new JMenuItem("Save");
		fileSaveMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		fileSaveMenuItem.getAccessibleContext().setAccessibleDescription(
		        "Save the Script");
		fileSaveMenuItem.addActionListener(this);
		menuFile.add(fileSaveMenuItem);

		
		// Save As Menu
		fileSaveAsMenuItem = new JMenuItem("Save As");
		fileSaveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		fileSaveAsMenuItem.getAccessibleContext().setAccessibleDescription(
		        "Exit the application");
		fileSaveAsMenuItem.addActionListener(this);
		menuFile.add(fileSaveAsMenuItem);

		// Exit Menu
		fileExitMenuItem = new JMenuItem("Exit");
		fileExitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		fileExitMenuItem.getAccessibleContext().setAccessibleDescription(
		        "Exit the application");
		fileExitMenuItem.addActionListener(this);
		menuFile.add(fileExitMenuItem);
	}
	
	private void createEditMenuItems()
	{
		ButtonGroup group = new ButtonGroup();
		rbDebugMenuItem = new JRadioButtonMenuItem("Command Mode");
		rbDebugMenuItem.setSelected(true);
		rbDebugMenuItem.setMnemonic(KeyEvent.VK_D);
		rbDebugMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		group.add(rbDebugMenuItem);
		menuEdit.add(rbDebugMenuItem);
		rbDebugMenuItem.addActionListener(this);
		
		rbTestMenuItem = new JRadioButtonMenuItem("Script Mode");
		rbTestMenuItem.setMnemonic(KeyEvent.VK_T);
		rbTestMenuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		group.add(rbTestMenuItem);
		menuEdit.add(rbTestMenuItem);
		rbTestMenuItem.addActionListener(this);
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

	@Override
	public void actionPerformed(ActionEvent ae) {
		String source = ae.getActionCommand();
		if (source.equalsIgnoreCase("Command Mode"))
		{
			hideMenuItems();
			System.out.println("Enable Command Mode");
			pnlCommands.setVisible(true);
			pnlFileCommands.setVisible(false);
			jspFileCommands.setVisible(false);

			taResult.setText("");
		}
		else if (source.equalsIgnoreCase("Script Mode"))
		{
			System.out.println("Enable Script Mode");
			showMenuItems();
			jspFileCommands.setPreferredSize(new Dimension(200,500));
			pnlCommands.setVisible(false);
			pnlFileCommands.setVisible(true);
			jspFileCommands.setVisible(true);

			taResult.setText("");
			taSelectedFileName.requestFocus();
		}
		else if (source.equalsIgnoreCase("Exit"))
		{
			System.exit(0);
		}
		else if (source.equalsIgnoreCase("Run"))
		{
			executeScript();
		}
		else if (source.equalsIgnoreCase("Browse") || source.equalsIgnoreCase("Open"))
		{
			int returnVal = 0;
			try
			{
				if (!taSelectedFileName.getText().isEmpty())
				{
					File file = new File(taSelectedFileName.getText());
					if (file.isDirectory())
					{
						fc.setCurrentDirectory(file);
					}
					else if(file.isFile())
					{
						fc.setCurrentDirectory(file.getParentFile());
					}
				}
				
			}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			returnVal = fc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
				    selectedFile = fc.getSelectedFile();
				    System.out.println("file selected..."+selectedFile.getName());
				    taSelectedFileName.setText(selectedFile.getName());
				    
				    fileContent = getFileContent();
				    taFileCommands.setText(fileContent);
				    btnExecuteFile.setEnabled(true);
				    btnTestContRun.setEnabled(true);
				    
				} else {
				    System.out.println("Cancelled...");
				}
			
		}
		else if (source.equalsIgnoreCase("Load"))
		{
			if (!taSelectedFileName.getText().isEmpty())
			{
				if (taSelectedFileName.getText().indexOf("/") != -1)
				{
					selectedFile = new File(taSelectedFileName.getText());
				}
				else
				{
					selectedFile = new File(fc.getCurrentDirectory()+"/"+taSelectedFileName.getText());
				}
				fileContent = getFileContent();
			    taFileCommands.setText(fileContent);
			    btnExecuteFile.setEnabled(true);
			    btnTestContRun.setEnabled(true);
			}
		}
		else if (source.equalsIgnoreCase("Save"))
		{
			if (taSelectedFileName.getText().isEmpty())
			{
				JOptionPane.showMessageDialog(this, "No File Name Specified", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (taSelectedFileName.getText().indexOf("/") != -1)
			{
				selectedFile = new File(taSelectedFileName.getText());
			}
			else
			{
				selectedFile = new File(fc.getCurrentDirectory()+"/"+taSelectedFileName.getText());
			}
			fileContent = taFileCommands.getText();
			save(fileContent);
		}
		else if (source.equalsIgnoreCase("Save As"))
		{
			int returnVal = 0;
			returnVal = fc.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				selectedFile = fc.getSelectedFile();
				taSelectedFileName.setText(selectedFile.getName());
				fileContent = taFileCommands.getText();
				save(fileContent);
			}
		}

		
	}

	private void executeScript() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		String latestContent = taFileCommands.getText();
		String delay; 
		String[] commands = latestContent.split(M9kStationConstants.NEWLINE);
		StringBuffer results = new StringBuffer();
		for (int i = 0; i < commands.length; i++) {
			try {
				System.out.println("Command to be execured "+commands[i]);
				if (commands[i].toUpperCase().startsWith(M9kStationConstants.SCRIPT_DELAY))
				{
					delay = commands[i].substring(commands[i].indexOf("=")+1);
					Thread.sleep(Integer.parseInt(delay));
					continue;
				}
				executeCommand(commands[i]);
				results.append(finalResult);
				results.append(M9kStationConstants.NEWLINE);
			} catch (M9000Exception e) {
				finalResult = e.getCause().toString();
				System.out.println("Error text "+finalResult);
				e.printStackTrace();
				logger.error("Error Occured"+e);
				taResult.setText(finalResult);
				finalResult="";
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.error("Error Occured"+e);
				finalResult = e.getCause().toString();
				taResult.setText(finalResult);
				finalResult="";
				return;
			}
			finally
			{
				setCursor(Cursor.getDefaultCursor());
			}
			
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		taResult.setText(results.toString());
		saveOutputToFile(results.toString());
		results=null;
		setCursor(Cursor.getDefaultCursor());

		
	}

	private String getFileContent()
	{
		BufferedReader readFile;
		StringBuffer strFileContent = new StringBuffer();
		String line = null;
		
		try {
			readFile = new BufferedReader(new FileReader(selectedFile));
			
			while ((line = readFile.readLine()) != null)
			{
				strFileContent.append(line);
				strFileContent.append(M9kStationConstants.NEWLINE);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strFileContent.toString();
	}
	
	private void save(String fileContent)
	{
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(selectedFile));
			bw.write(fileContent);
			JOptionPane.showMessageDialog(this, "SuccessFully Saved", "Info", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if (bw != null)
			{
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bw = null;
			}
		}
	}
	
	private void saveOutputToFile(String output)
	{
		BufferedWriter bw = null;
		String currentFileName = taSelectedFileName.getText();
		currentFileName = currentFileName.substring(0, currentFileName.indexOf(".")+1)+"out";
		if (currentFileName.indexOf("/") == -1)
		{
			currentFileName = fc.getCurrentDirectory()+"/"+currentFileName;
		}
		
		try {
			FileWriter fw = new FileWriter(currentFileName, true);
			bw = new BufferedWriter(fw);
			
			bw.write(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(System.currentTimeMillis()));
			bw.newLine();
			bw.write(output);
			bw.newLine();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			if (bw != null)
			{
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				bw = null;
			}
		}
	}
	public JPanel getPnlCommands() {
		return pnlCommands;
	}

	public void setPnlCommands(JPanel pnlCommands) {
		this.pnlCommands = pnlCommands;
	}

	public JPanel getPnlFileCommands() {
		return pnlFileCommands;
	}

	public void setPnlFileCommands(JPanel pnlFileCommands) {
		this.pnlFileCommands = pnlFileCommands;
	}

	public JPanel getPnlNorth() {
		return pnlNorth;
	}

	public void setPnlNorth(JPanel pnlNorth) {
		this.pnlNorth = pnlNorth;
	}
	
	public class TestFileFilter extends FileFilter {

	    //Accept all directories and all gif, jpg, tiff, or png files.
	    public boolean accept(File file) {
	        if (file.isDirectory()) {
	            return true;
	        }

	        int i = file.getName().lastIndexOf(".");
	        String fileExtension;
	        if (i > 0 && i < (file.getName().length() - 1))
	        {
	        	fileExtension = file.getName().substring(i+1);
	        	if (fileExtension.equalsIgnoreCase("usi"))
	        	{
	        		return true;
	        	}
	        }
	        return false;
	    }

	    //The description of this filter
	    public String getDescription() {
	        return "*.usi";
	    }
	}


}  //  @jve:decl-index=0:visual-constraint="0,-1"

