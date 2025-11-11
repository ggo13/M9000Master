package com.usi.m9000.test;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class TestJSch {
    public static void main(String args[]) {
        JSch jsch = new JSch();
        Session session = null;
        try {
        	System.out.println("Entered...");
            session = jsch.getSession("root", "195.1.1.70", 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword("root");
            session.connect();
            System.out.println("Connected...");
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.get("remotefile.txt", "localfile.txt");
            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }
}
