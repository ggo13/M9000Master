package com.usi.m9000.util;

/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.commons.io.IOUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import com.usi.m9000.Exception.M9000Exception;
import com.usi.m9000.station.net.M9kStationCommandClient;

public class M9kScp implements Runnable{
private JSch jsch;
private String user;
private String host;
private int port;
private String configXml;
private Channel channel;
private Session session;

private String backupStationConfig;
private volatile boolean failed = false;
private String tempNewConfigFile = "/etc/m9000/newconfig.xml";
static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(M9kScp.class);
	public M9kScp(String host, int port, String userName)
	{
		String knownHostsFilename = "/home/dfr/.ssh/known_hosts";
		String pubkeyfile="/home/dfr/.ssh/id_rsa";
		
		try {
			if (host != null && !host.isEmpty())
			{
				this.host = host;
			}
			else
			{
				throw new M9000Exception("Invalid Host");
			}
			this.port = port;
			this.user = userName;
			jsch=new JSch();
			jsch.addIdentity(pubkeyfile);
	      jsch.setKnownHosts(knownHostsFilename);      
	      logger.debug("User:" + user +" Host..."+host+" port: "+ port);
	      session=jsch.getSession(user, host, port);
	      session.setPassword("root");
	      java.util.Properties config = new java.util.Properties();
          config.put("StrictHostKeyChecking", "no");
           session.setConfig(config);



	      
		} catch(Exception e){
	          logger.error("Error in M9kSCP", e);
	          e.printStackTrace();
	        }
		
	}

	// To make it parallel copy
	public M9kScp(String host, int port, String userName, String configXml, String backupStationConfig)
	{
		this.configXml = configXml;
		this.backupStationConfig = backupStationConfig;
//		this.configFile = configFile;
		String knownHostsFilename = "/home/dfr/.ssh/known_hosts";
		String pubkeyfile="/home/dfr/.ssh/id_rsa";
		
		try {
			if (host != null && !host.isEmpty())
			{
				this.host = host;
			}
			else
			{
				throw new M9000Exception("Invalid Host");
			}
			this.port = port;
			this.user = userName;
			jsch=new JSch();
			jsch.addIdentity(pubkeyfile);
	      jsch.setKnownHosts(knownHostsFilename);      
	      logger.debug("User:" + user +" Host..."+host+" port: "+ port);
	      session=jsch.getSession(user, host, port);
//	      session.setPassword("root");
	      java.util.Properties config = new java.util.Properties();
          config.put("StrictHostKeyChecking", "no");
           session.setConfig(config);



	      
		} catch(Exception e){
	          logger.error("Error in M9kSCP", e);
	          e.printStackTrace();
	        }
		
	}

	// Used for testing purpose 
	private M9kScp(String host, int port, String userName,boolean booTest)
	{
		String knownHostsFilename = "C:/M9k/ssh/known_hosts";
		String pubkeyfile="C:/M9k/ssh/id_rsa.ppk";

		
		try {
			if (host != null && !host.isEmpty())
			{
				this.host = host;
			}
			else
			{
				throw new M9000Exception("Invalid Host");
			}
			this.port = port;
			this.user = userName;
			jsch=new JSch();
//			jsch.addIdentity(pubkeyfile);
//		      jsch.setKnownHosts(knownHostsFilename);      

	      logger.debug("User:" + user +" Host..."+host+" port: "+ port);
	      session=jsch.getSession(user, host, port);
//	      UserInfo ui=new MyUserInfo();
//	      session.setUserInfo(ui);
	      session.setPassword("root");
	      java.util.Properties config = new java.util.Properties();
          config.put("StrictHostKeyChecking", "no");
           session.setConfig(config);



	      
		} catch(Exception e){
	          logger.error("Error in M9kSCP", e);
	          e.printStackTrace();
	        }
		
	}
	public void run() {
		// TODO Auto-generated method stub
		try {
			logger.debug("RUN: Invoking sendConfig ");
			sendConfig(configXml, tempNewConfigFile , backupStationConfig);
		} catch (M9000Exception e) {
			logger.error("Error in M9kSCP..."+host, e);
			logger.debug("Failed flag is set to true..."+failed);
			failed = true;
		}
	}

	
	// Unused as we are using new method to parallel send the Config
	public void sendConfig(String configFile, String backupStationConfig) throws M9000Exception
	{
	    FileInputStream fis=null;
	    OutputStream out = null;
	    InputStream in = null;
//	    boolean booSuccess = true;
	      String remoteFile = configFile;
	      try {
//		      // username and password will be given via UserInfo interface.
//		      UserInfo ui=new MyUserInfo();
//		      session.setUserInfo(ui);
//		      session.setPassword("root");
		      session.connect(10000);
		      logger.debug("Successfully Connected..");
	      if (configFile.indexOf("/") > -1)
		      {
		    	  remoteFile = configFile.substring(configFile.lastIndexOf("/")+1);
		      }
//	      logger.debug("Remote file "+configFile+" index of slash "+configFile.indexOf("/") );
		      // exec 'scp -t rfile' remotely
	      logger.debug("Setting up channels to send config");
		      String command="scp -p -t /etc/m9000/"+remoteFile;
		      channel=session.openChannel("exec");
		      ((ChannelExec)channel).setCommand(command);
		      // get I/O streams for remote scp
		       out=channel.getOutputStream();
		      in=channel.getInputStream();
	
		      channel.connect();
	
		      if(checkAck(in)!=0){
		    	  throw new M9000Exception("Send Failed");
		      }
		      // send "C0644 filesize filename", where filename should not include '/'
		      long filesize=(new File(configFile)).length();
		      command="C0644 "+filesize+" ";
		      if(configFile.lastIndexOf('/')>0){
		        command+=configFile.substring(configFile.lastIndexOf('/')+1);
		      }
		      else{
		        command+=configFile;
		      }
		      command+="\n";
		      
		      out.write(command.getBytes()); out.flush();
		      if(checkAck(in)!=0){
	//	    	  System.exit(0);
		    	  throw new M9000Exception("Send Failed");
		      }
	
		      // send a content of lfile
		      logger.debug("Sending the content of the config file");
		      fis=new FileInputStream(configFile);
		      byte[] buf=new byte[1024];
		      while(true){
		        int len=fis.read(buf, 0, buf.length);
			if(len<=0) break;
		        out.write(buf, 0, len); //out.flush();
		      }
		      fis.close();
		      fis=null;
		      
		      // send '\0'
		      buf[0]=0; out.write(buf, 0, 1); out.flush();
		      logger.debug("Sending complete...");
		      logger.debug("Checking ack");
		      if(checkAck(in)!=0){
		    	  throw new M9000Exception("Send Failed");
		      }
		      logger.debug("ACK complete");
		      out.close();
		      in.close();
		      channel.disconnect();
		      logger.debug("About to send go");
		      sendFinishStatus();
		      try
		      {
				// START: 27-May-2021 - Backup entire station configuration in each of the chassis
				if (backupStationConfig != null)
				{
					logger.debug("backed up config "+backupStationConfig);
					backupStationConfig(backupStationConfig);
				}
				// END: 27-May-2021
		      }
		      catch (Exception e) {
				logger.error("Unable to back up the station config sql file. Reason "+e.getMessage(),e);
			}

		      // START:09-May-2024 - Restart after sending config
		    try
		    {
		    	M9kStationCommandClient commandClient = new M9kStationCommandClient(host, port, "RESTART");
				commandClient.sendAndReceive();
		    }
		    catch (Exception e) {
		    	logger.error("Unable to issue RESTART command to chassis "+host+". Reason "+e.getMessage(),e);
		    }
		    // END:09-May-2024
	      }
	      catch (Exception e) {
//	    	  booSuccess = false;
	    	  logger.error("Unable to send the config to the chassis"+host+". Reason "+e.getMessage(),e);
			throw new M9000Exception("Send Failed!", e);
		}
	      finally
	      {
	    	  
	    	  try{
	    		  cleanUp();
	    		  if(fis!=null)
	    		  {
	    			  fis.close();
	    			  fis = null;
	    		  }
	    		  if(out!=null)
	    		  {
	    			  out.close();
	    			  out = null;
	    		  }
	    		  if(in!=null)
	    		  {
	    			  in.close();
	    			  in = null;
	    		  }
	    	  }catch(Exception ee){}
	      }
		
	}

	public void sendConfig(String configXml, String remoteFilePath, String backupStationConfig) throws M9000Exception
	{
	      try {
		      session.connect(10000);
		      logger.debug("Successfully Connected..");
		      logger.debug("Setting up channels to send config");
//		      String command = "echo '" + configXml + "' > " + remoteFilePath; 
		   // Create SSH channel (SFTP) for file transfer
	            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
	            channel.connect();

	            try {
	                // Convert text content to input stream
	                byte[] textBytes = configXml.getBytes(StandardCharsets.UTF_8);
	                ByteArrayInputStream inputStream = new ByteArrayInputStream(textBytes);

	                // Transfer text content to remote file
	                channel.put(inputStream, remoteFilePath,ChannelSftp.OVERWRITE);

	            } finally {
	                // Disconnect SSH channel
	                channel.disconnect();
	            }
		      logger.debug("About to send go");
		      try
		      {
				// START: 27-May-2021 - Backup entire station configuration in each of the chassis
				if (backupStationConfig != null)
				{
					logger.debug("backed up config "+backupStationConfig);
					backupStationConfig(backupStationConfig);
				}
				// END: 27-May-2021
		      }
		      catch (Exception e) {
				logger.error("Unable to back up the station config sql file. Reason "+e.getMessage(),e);
			}
		      sendFinishStatus();

		      // START:09-May-2024 - Restart after sending config
		    try
		    {
		    	M9kStationCommandClient commandClient = new M9kStationCommandClient(host, port, "RESTART");
				commandClient.sendAndReceive();
		    }
		    catch (Exception e) {
		    	logger.error("Unable to issue RESTART command to chassis "+host+". Reason "+e.getMessage(),e);
		    }
		    // END:09-May-2024
	      }
	      catch (Exception e) {
//	    	  booSuccess = false;
	    	  logger.error("Unable to send the config to the chassis"+host+". Reason "+e.getMessage(),e);
			throw new M9000Exception("Send Failed!", e);
		}
	      finally
	      {
	    	  
	    	  try{
	    		  cleanUp();
	    	  }catch(Exception ee){}
	      }
		
	}

	private void sendFinishStatusOld() throws M9000Exception
	{
	      String remoteFile = "go";
		    OutputStream out = null;
		    InputStream in = null;

	      try {
//		      session.connect(10000);

		      String command="scp -p -t /etc/m9000/"+remoteFile;
		      channel=session.openChannel("exec");
		      ((ChannelExec)channel).setCommand(command);
		      out=channel.getOutputStream();
		      in=channel.getInputStream();
	
		      channel.connect();
	
		      if(checkAck(in)!=0){
		    	  throw new M9000Exception("Send Failed");
		      }
		      // send "C0644 filesize filename", where filename should not include '/'
		      long filesize=0L;
		      command="C0644 "+filesize+" go\n";
		      
		      out.write(command.getBytes()); out.flush();
		      if(checkAck(in)!=0){
	//	    	  System.exit(0);
		    	  throw new M9000Exception("Send Failed");
		      }
	
		      // send '\0'
		      byte[] buf=new byte[1024];
		      buf[0]=0; out.write(buf, 0, 1); out.flush();
		      logger.debug("Sending go complete");
		      if(checkAck(in)!=0){
		    	  throw new M9000Exception("Send Failed");
		      }
		      out.close();
	
		      
	      }
	      catch (Exception e) {
//	    	  booSuccess = false;
			throw new M9000Exception("Send Failed!", e);
		}
	      finally
	      {
	    	  try{
	    		  if(out!=null)
	    		  {
	    			  out.close();
	    			  out = null;
	    		  }
	    		  if(in!=null)
	    		  {
	    			  in.close();
	    			  in = null;
	    		  }

	    	  }catch(Exception ee){}
	    	  
	      }
		
	}

	private void sendFinishStatus() throws M9000Exception
	{
	      try {
		      logger.debug("Successfully Connected..");
		      logger.debug("Setting up channels to send config");
		      String remoteFilePath="/etc/m9000/go";
		      String touchCommand = "touch " + remoteFilePath; 
		      channel=session.openChannel("exec");
		      ((ChannelExec)channel).setCommand(touchCommand);
		      channel.connect();
		      channel.disconnect();
	      }
	      catch (Exception e) {
//	    	  booSuccess = false;
	    	  logger.error("Unable to send the config to the chassis"+host+". Reason "+e.getMessage(),e);
			throw new M9000Exception("Send Failed!", e);
		}
	      finally
	      {
	    	  
	    	  try{
	    		  cleanUp();
	    	  }catch(Exception ee){}
	      }
		
	}

	public void cleanUp()
	{
		if (channel != null && !channel.isClosed())
		{
	      channel.disconnect();
	      channel = null;
		}
		if (session != null && session.isConnected())
		{
			session.disconnect();
			session = null;
		}
	}
	public void getComtradeFiles(String remoteFile, String localDir) throws M9000Exception
	{
		
	    FileOutputStream fos=null;
	      try {
//		      // username and password will be given via UserInfo interface.
//		      UserInfo ui=new MyUserInfo();
//		      session.setUserInfo(ui);
//		      session.setPassword("user");
		      session.connect(10000);
		      logger.debug("Successfully Connected..");
	    	  File fileLocaldir = new File(localDir);
	    	  if (!fileLocaldir.exists())
	    	  {
	    		  fileLocaldir.mkdirs();
	    	  }
		      // exec 'scp -t rfile' remotely
		      String command="scp -f "+remoteFile;
		      channel=session.openChannel("exec");
		      ((ChannelExec)channel).setCommand(command);
		      
		      // get I/O streams for remote scp
		      OutputStream out=channel.getOutputStream();
		      InputStream in=channel.getInputStream();
		      
//		      logger.debug("About to connect Channel...");
		      channel.connect();
//		      logger.debug("connected to Channel...");
		      byte[] buf=new byte[1024];

		      // send '\0'
		      buf[0]=0; out.write(buf, 0, 1); out.flush();

		      while(true){
			int c=checkAck(in);
		        if(c!='C'){
			  break;
			}

		        // read '0644 '
		        in.read(buf, 0, 5);

		        long filesize=0L;
		        while(true){
		          if(in.read(buf, 0, 1)<0){
		            // error
		            break; 
		          }
		          if(buf[0]==' ')break;
		          filesize=filesize*10L+(long)(buf[0]-'0');
		        }

		        String file=null;
		        for(int i=0;;i++){
		          in.read(buf, i, 1);
		          if(buf[i]==(byte)0x0a){
		            file=new String(buf, 0, i);
		            break;
		  	  }
		        }

//			logger.debug("filesize="+filesize+", file="+file);

		        // send '\0'
		        buf[0]=0; out.write(buf, 0, 1); out.flush();

		        // read a content of lfile
		        fos=new FileOutputStream(localDir+file);
		        int foo;
		        while(true){
		          if(buf.length<filesize) foo=buf.length;
			  else foo=(int)filesize;
		          foo=in.read(buf, 0, foo);
		          if(foo<0){
		            // error 
		            break;
		          }
		          fos.write(buf, 0, foo);
		          filesize-=foo;
		          if(filesize==0L) break;
		        }
		        fos.close();
		        fos=null;

			if(checkAck(in)!=0){
			  System.exit(0);
			}

		        // send '\0'
		        buf[0]=0; out.write(buf, 0, 1); out.flush();
		      }

		      session.disconnect();

		    }
		    catch(Exception e){
		      logger.error(e.toString());
		      throw new M9000Exception("Copy Failed!", e);
		    }
	      finally
	      {
	    	  try{if(fos!=null)fos.close();}catch(Exception ee){}
	      }
		
	}

	public File getActiveConfigurationFile(String remoteFile, String localDir) throws M9000Exception
	{
		File activeConfigFile = null;
	    FileOutputStream fos=null;
	      try {
//		      // username and password will be given via UserInfo interface.
//		      UserInfo ui=new MyUserInfo();
//		      session.setUserInfo(ui);
//		      session.setPassword("user");
		      session.connect(10000);
		      logger.debug("Successfully Connected.."+remoteFile);
	    	  File fileLocaldir = new File(localDir);
	    	  if (!fileLocaldir.exists())
	    	  {
	    		  fileLocaldir.mkdirs();
	    	  }
	    	  
		      // exec 'scp -f rfile' remotely
		      String command="scp -f /hd/dat/oldConfigs/"+remoteFile;
		      channel=session.openChannel("exec");
		      ((ChannelExec)channel).setCommand(command);
		      
		      // get I/O streams for remote scp
		      OutputStream out=channel.getOutputStream();
		      InputStream in=channel.getInputStream();
		      
		      logger.debug("About to connect Channel...command "+command);
		      channel.connect();
		      logger.debug("connected to Channel...");
		      byte[] buf=new byte[1024];

		      // send '\0'
		      buf[0]=0; out.write(buf, 0, 1); out.flush();

		      while(true){
			int c=checkAck(in);
		        if(c!='C'){
			  break;
			}

		        // read '0644 '
		        in.read(buf, 0, 5);

		        long filesize=0L;
		        while(true){
		          if(in.read(buf, 0, 1)<0){
		            // error
		            break; 
		          }
		          if(buf[0]==' ')break;
		          filesize=filesize*10L+(long)(buf[0]-'0');
		        }

		        String file=null;
		        for(int i=0;;i++){
		          in.read(buf, i, 1);
		          if(buf[i]==(byte)0x0a){
		            file=new String(buf, 0, i);
		            break;
		  	  }
		        }

			logger.debug("filesize="+filesize+", file="+file);

		        // send '\0'
		        buf[0]=0; out.write(buf, 0, 1); out.flush();
		          activeConfigFile = new File(localDir+file);
		          logger.debug("active Config file path "+activeConfigFile.getAbsolutePath()+" name "+activeConfigFile.getAbsoluteFile());
			    	
		        // Write content of lfile
		        fos=new FileOutputStream(activeConfigFile);
		        int foo;
		        while(true){
		          if(buf.length<filesize) foo=buf.length;
			  else foo=(int)filesize;
		          foo=in.read(buf, 0, foo);
		          if(foo<0){
		            // error 
		            break;
		          }
		          fos.write(buf, 0, foo);
		          filesize-=foo;
		          if(filesize==0L) break;
		        }
		        fos.close();
		        fos=null;

			if(checkAck(in)!=0){
			  System.exit(0);
			}

		        // send '\0'
		        buf[0]=0; out.write(buf, 0, 1); out.flush();
		      }

	          activeConfigFile.setWritable(true, false);
	          activeConfigFile.setExecutable(true,false);
	          activeConfigFile.setReadable(true, false);
		      session.disconnect();

		    }
		    catch(Exception e){
		      logger.error("Failed to copy the active config file..."+e);
		      throw new M9000Exception("Copy Failed!", e);
		    }
	      finally
	      {
	    	  try{if(fos!=null)fos.close(); session.disconnect();}catch(Exception ee){}
	      }
	      logger.debug("About to return activeConfigFile..."+activeConfigFile);
	      return activeConfigFile;
		
	}

  static int checkAck(InputStream in) throws IOException{
    int b=in.read();
    // b may be 0 for success,
    //          1 for error,
    //          2 for fatal error,
    //          -1
    if(b==0) return b;
    if(b==-1) return b;

    if(b==1 || b==2){
      StringBuffer sb=new StringBuffer();
      int c;
      do {
	c=in.read();
	sb.append((char)c);
      }
      while(c!='\n');
      if(b==1){ // error
	System.out.print(sb.toString());
      }
      if(b==2){ // fatal error
	System.out.print(sb.toString());
      }
    }
    return b;
  }

  public static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
    public String getPassword(){ return passwd; }
    public boolean promptYesNo(String str){
      Object[] options={ "yes", "no" };
      int foo=JOptionPane.showOptionDialog(null, 
             str,
             "Warning", 
             JOptionPane.DEFAULT_OPTION, 
             JOptionPane.WARNING_MESSAGE,
             null, options, options[0]);
       return foo==0;
    }
  
    String passwd;
    JTextField passwordField=(JTextField)new JPasswordField(20);

    public String getPassphrase(){ return null; }
    public boolean promptPassphrase(String message){ return true; }
    public boolean promptPassword(String message){
      Object[] ob={passwordField}; 
      int result=
	  JOptionPane.showConfirmDialog(null, ob, message,
					JOptionPane.OK_CANCEL_OPTION);
      if(result==JOptionPane.OK_OPTION){
	passwd=passwordField.getText();
	return true;
      }
      else{ return false; }
    }
    public void showMessage(String message){
      JOptionPane.showMessageDialog(null, message);
    }
    final GridBagConstraints gbc = 
      new GridBagConstraints(0,0,1,1,1,1,
                             GridBagConstraints.NORTHWEST,
                             GridBagConstraints.NONE,
                             new Insets(0,0,0,0),0,0);
    private Container panel;
    public void showConfirmation(String message){
        JOptionPane.showMessageDialog(null, message);
      }
    public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo){
      panel = new JPanel();
      panel.setLayout(new GridBagLayout());

      gbc.weightx = 1.0;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbc.gridx = 0;
      panel.add(new JLabel(instruction), gbc);
      gbc.gridy++;

      gbc.gridwidth = GridBagConstraints.RELATIVE;

      JTextField[] texts=new JTextField[prompt.length];
      for(int i=0; i<prompt.length; i++){
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.weightx = 1;
        panel.add(new JLabel(prompt[i]),gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;
        if(echo[i]){
          texts[i]=new JTextField(20);
        }
        else{
          texts[i]=new JPasswordField(20);
        }
        panel.add(texts[i], gbc);
        gbc.gridy++;
      }

      if(JOptionPane.showConfirmDialog(null, panel, 
                                       destination+": "+name,
                                       JOptionPane.OK_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE)
         ==JOptionPane.OK_OPTION){
        String[] response=new String[prompt.length];
        for(int i=0; i<prompt.length; i++){
          response[i]=texts[i].getText();
        }
	return response;
      }
      else{
        return null;  // cancel
      }
    }
  }

  /**
   * 
   * @param configFile
   * @throws M9000Exception
   */
  public void backupStationConfig(String configFile) throws M9000Exception
	{
	    FileInputStream fis=null;
	    OutputStream out = null;
	    InputStream in = null;
//	    boolean booSuccess = true;
	      try {
//		      // username and password will be given via UserInfo interface.
//		      UserInfo ui=new MyUserInfo();
//		      session.setUserInfo(ui);
//		      session.setPassword("root");
//		      session.connect(10000);
		      logger.debug("In backupStationConfig Successfully Connected.."+configFile);

	      logger.debug("Remote file "+configFile+" index of slash "+configFile.indexOf("/") );
		      // exec 'scp -t rfile' remotely
	      logger.debug("Setting up channels to send config");
		      String command="scp -p -t /hd/dat/oldConfigs/"+M9kConstants.ACTIVE_CONFIG_SQL;
		      channel=session.openChannel("exec");
		      ((ChannelExec)channel).setCommand(command);
		      // get I/O streams for remote scp
		       out=channel.getOutputStream();
		      in=channel.getInputStream();
	
		      channel.connect();
	
		      if(checkAck(in)!=0){
		    	  throw new M9000Exception("Send Failed");
		      }
		      // send "C0644 filesize filename", where filename should not include '/'
		      long filesize=(new File(configFile)).length();
		      command="C0644 "+filesize+" ";
		      if(configFile.lastIndexOf('/')>0){
		        command+=configFile.substring(configFile.lastIndexOf('/')+1);
		      }
		      else{
		        command+=configFile;
		      }
		      command+=M9kConstants.NEWLINE;
		      
		      out.write(command.getBytes()); out.flush();
		      if(checkAck(in)!=0){
	//	    	  System.exit(0);
		    	  throw new M9000Exception("Send Failed");
		      }
	
		      // send a content of lfile
		      logger.debug("Sending the content of the config file");
		      fis=new FileInputStream(configFile);
		      byte[] buf=new byte[1024];
		      while(true){
		        int len=fis.read(buf, 0, buf.length);
			if(len<=0) break;
		        out.write(buf, 0, len); //out.flush();
		      }
		      fis.close();
		      fis=null;
		      
		      // send '\0'
		      buf[0]=0; out.write(buf, 0, 1); out.flush();
		      logger.debug("Sending complete...");
		      logger.debug("Checking ack");
		      if(checkAck(in)!=0){
		    	  throw new M9000Exception("Send Failed");
		      }
		      logger.debug("ACK complete");
		      out.close();
		      in.close();
		      channel.disconnect();
		      logger.debug("About to send go");
	      }
	      catch (Exception e) {
//	    	  booSuccess = false;
			throw new M9000Exception("Send Failed!", e);
		}
	      finally
	      {
	    	  
	    	  try{
//	    		  cleanUp();
	    		  if(fis!=null)
	    		  {
	    			  fis.close();
	    			  fis = null;
	    		  }
	    		  if(out!=null)
	    		  {
	    			  out.close();
	    			  out = null;
	    		  }
	    		  if(in!=null)
	    		  {
	    			  in.close();
	    			  in = null;
	    		  }
	    	  }catch(Exception ee){}
	      }
		
	}
/**
 * @return the user
 */
public String getUser() {
	return user;
}
/**
 * @param user the user to set
 */
public void setUser(String user) {
	this.user = user;
}
/**
 * @return the host
 */
public String getHost() {
	return host;
}
/**
 * @param host the host to set
 */
public void setHost(String host) {
	this.host = host;
}
/**
 * @return the port
 */
public int getPort() {
	return port;
}
/**
 * @param port the port to set
 */
public void setPort(int port) {
	this.port = port;
}

public static void lsTest() {
    String username = "dfr";
    String host = "10.10.0.144";
    String privateKeyFile = "C:/M9k/trial-error/ssh/id_dsa.ppk";
    String knownHostsFile = "known_hosts";
    String dirName = "/data/m9k/config-files/";
    int port = 22;

    JSch jsch = new JSch();
    Session session = null;
    Channel channel = null;
    try {
        jsch.addIdentity(privateKeyFile);
        jsch.setKnownHosts(knownHostsFile);
        session = jsch.getSession(username, host, port);
        session.setTimeout(20 * 1000);
        session.connect();

        channel = session.openChannel("exec");
        ChannelExec channelExec = (ChannelExec) channel;

        System.out.println("Executing ls");
//        channelExec.setCommand("ls " + dirName);
        channelExec.setCommand("find " + dirName + " -maxdepth 1 -type f -printf '%f\\n'");
        channel.connect();

        InputStream is = channelExec.getInputStream();
        IOUtils.copy(is, System.out);
        is.close();

        System.out.println("Exit status: " + channel.getExitStatus());


    } catch (JSchException exception) {
        exception.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }
}
public boolean isFailed() {
    return failed;
}
public static void main(String[] args)
{
	M9kScp.lsTest();
//	try {
//		int cnt = 0;
//		while (cnt++ < 10)
//		{
//			M9kScp m9kScp = new M9kScp("195.1.1.72",22,"root", true);
//	//		m9kScp.getComtradeFiles("test.dat", "c:/comtradeFiles/");
//			m9kScp.sendConfig("c:/M9kConfig/newconfig.xml", "c:/M9kConfig/newconfig.xml");
//	//		m9kScp.getComtradeFiles("/root/*.cfg", "c:/comtradeFiles/");
//	//		m9kScp.getComtradeFiles("*.inf", "e:/comtradeFiles");
////			Thread.sleep(1000);
//			System.out.println("Cnt "+cnt);
//		}
//	} catch (Exception e) {
//		// TODO: handle exception
//		e.printStackTrace();
//	}
	
}
}
