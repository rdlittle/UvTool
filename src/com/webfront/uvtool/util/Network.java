/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rlittle
 */
public class Network {

    private final ConfigProperties platforms = ConfigProperties.getInstance();
    private final String cbHost = platforms.getCbHost();
    private final String cbUser = "release";
    private final String cbPassword = "R31ea$E_@)!(";

    private final HashMap<String, String> sshCommands;

    public Network() {
        sshCommands = new HashMap<>();

        sshCommands.put("getApproved", "getAPPData");
        sshCommands.put("setApproved", "addToApproved");
        sshCommands.put("setFailed", "addToFailed");
        sshCommands.put("getItem", "getFile");
        sshCommands.put("getDir", "getDir");
        sshCommands.put("remove", "rm");
        sshCommands.put("cat", "cat");
        sshCommands.put("compile", "barfyCompile");
    }

    public int doSftp(String remoteHost, String remotePath, String remoteItem,
            String localPath, String localItem) throws JSchException,
            SftpException, IOException {
        String keyPath = "/home/rlittle/sob/nlstest.id_rsa";
        String outPath = localPath + localItem;
        JSch jsch = new JSch();
        FileOutputStream output = null;
        java.util.Vector cmds = new java.util.Vector();
        output = new FileOutputStream(outPath);
        jsch.addIdentity(keyPath);
        Session session = jsch.getSession("release", remoteHost, 22);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword("R31ea$E_@)!(");
        session.connect(1000);
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp c = (ChannelSftp) channel;
        c.cd(remotePath);
        int mode = ChannelSftp.OVERWRITE;
        c.get(remoteItem, output);
        int mtime = c.lstat(remoteItem).getMTime();
        output.close();
        session.disconnect();
        return mtime;
    }

    public ByteArrayOutputStream sshExec(String host, String path, String cmd) {
        try {
            String user = "release";
            String keyPath = "/home/rlittle/sob/nlstest.id_rsa";
            JSch jsch = new JSch();
            String fullCmdPath = path + "/" + cmd;
            jsch.addIdentity(keyPath);
            Session session = jsch.getSession(user, host);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword("R31ea$E_@)!(");
            try {
                session.connect(1000);
            } catch (JSchException e) {
                System.out.println(e.getMessage());
            }
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(cmd);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            InputStream in = channel.getInputStream();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            channel.setOutputStream(output);

            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
//                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }
            channel.disconnect();
            session.disconnect();
            return output;
        } catch (JSchException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
