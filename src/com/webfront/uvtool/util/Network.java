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
import com.webfront.uvtool.model.Server;
import com.webfront.u2.util.Config;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.events.EventException;

/**
 *
 * @author rlittle
 */
public class Network {

    private final Config systemConfig = Config.getInstance();
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

    public int doSftpGet(String remoteHost, String remotePath, String remoteItem,
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
    
    public int doSftpPut(String remoteHost, String remotePath, String remoteItem,
            String localPath, String localItem) throws JSchException,
            SftpException, IOException {
        String keyPath = "/home/rlittle/sob/nlstest.id_rsa";
        String inputPath = localPath + localItem;
        JSch jsch = new JSch();
        FileInputStream inputStream = new FileInputStream(inputPath);
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
        c.put(inputStream, remoteItem);
        int mtime = c.lstat(remoteItem).getMTime();
        inputStream.close();
        session.disconnect();
        return mtime;
    }    

    public ByteArrayOutputStream sshExec(String host, String path, String cmd) {
        try {
            String user = "release";
            String keyPath = "/home/rlittle/sob/nlstest.id_rsa";
            String multiCmd = String.format("cd %s && ./%s", path, cmd);
            if(cmd.startsWith("cat")) {
                multiCmd = String.format("cd %s && %s", path, cmd);
            }
            JSch jsch = new JSch();
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
            ((ChannelExec) channel).setCommand(multiCmd);
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

    public void getApproved(String itemType, String approvedId) throws
            FileNotFoundException, EventException, JSchException, SftpException,
            IOException {
        Server s = new Server(platforms.getPlatforms(), "dmc");
        String path = s.getPath("deploy");

        String host = s.getHost("approved");
        String cmd = sshCommands.get("getApproved");
        ByteArrayOutputStream output = sshExec(host, path,
                cmd + " "+ itemType + " " + approvedId);
        if (output.size() == 0) {
            throw new EventException((short) -1, "sshExec error");
        }
        String downloadPath = systemConfig.getPreferences().get("downloads");
        if ("CODE".equals(itemType)) {
            downloadPath = systemConfig.getPreferences().get("codeHome");
        } else if ("DATA".equals(itemType)) {
            downloadPath = systemConfig.getPreferences().get("dataHome");
        }
        if (!downloadPath.endsWith("/")) {
            downloadPath = downloadPath + "/";
        }
        String remotePath = s.getPath("deploy") + "/APPROVED.PROGRAMS";
        String item = (approvedId.split("~")[2]) + ".approved";
        doSftpGet(host, remotePath, approvedId, downloadPath, item);
        StringBuilder fileOutput;
        try (BufferedReader f = new BufferedReader(new FileReader(downloadPath + item))) {
            fileOutput = new StringBuilder();
            while (true) {
                String line = f.readLine();
                if (line == null) {
                    break;
                }
                fileOutput.append(line);
            }
        }
        if (fileOutput.indexOf("no APPROVED.PROGRAMS code found!") > 0) {
            File aFile = new File(downloadPath + approvedId);
            if (aFile.exists()) {
                aFile.delete();
            }
            throw new FileNotFoundException("No approved version found");
        }
    }
    
    public void setApproved(String itemType, String approvedId) throws
            FileNotFoundException, EventException, JSchException, SftpException,
            IOException {
        Server s = new Server(platforms.getPlatforms(), "dmc");
        String path = s.getPath("deploy");

        String host = s.getHost("approved");
        String cmd = sshCommands.get("setApproved");
        ByteArrayOutputStream output = sshExec(host, path,
                cmd + " "+ itemType + " " + approvedId);
        if (output.size() == 0) {
            throw new EventException((short) -1, "sshExec error");
        }
    }    
    
    public boolean checkDictData(String item) {
        String[] specs = item.split("~");
        String cmd = sshCommands.get("getApproved");
        String downloadPath = systemConfig.getPreferences().get("dataHome");
        String platform = specs[0];
        String dataFile = specs[1].replaceAll("\\\\", "~");;
        String itemId = specs[2];
        
        item = String.format("%s~%s~%s", platform, dataFile, itemId);
        
        Server s = new Server(platforms.getPlatforms(), "dmc");
        String remotePath = s.getPath("deploy");
        if (!remotePath.endsWith("/")) {
            remotePath = remotePath + "/";
        }
        cmd = cmd + " DATA "+item;
        ByteArrayOutputStream output = 
                sshExec("nlstest", remotePath, cmd);
        remotePath = s.getPath("dev_data");
        cmd = sshCommands.get("cat");
        cmd = cmd + " " + item;
        output = sshExec("nlstest", remotePath, cmd);
        if (output.size()==0) {
            return false;
        }
        return true;
    }

    public static String getPathType(String library) {
        Pattern pattern = Pattern.compile(".+\\.uv[f, i, p, s, t]");
        Matcher matcher = pattern.matcher(library);
        if (library.startsWith("DM.")) {
            return "main";
        }
        if (matcher.matches()) {
            return "uvcode";
        }
        if (library.endsWith("LIB")) {
            return "rbo";
        }
        if(library.endsWith("SEGMENT")) {
            return "pads_hook_segment";
        }
        if(library.equals("PADS.HOOK")) {
            return "pads_hook";
        }
        if(library.equals("PADS.BP")) {
            return "pads_bp";
        }
        if(library.equals("PADS.APP")) {
            return "main";
        }
        
        return null;
    }
    
    public static String sayHi() {
        return "Hi";
    }
}
