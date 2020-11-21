/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.util;

import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.webfront.u2.model.Profile;
import java.util.ArrayList;
import javafx.concurrent.Task;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author rlittle
 */
public class SelectorTask extends Task<ArrayList<String>> {

    //private final UvClient client;
    private final Profile client;
    private final String path;
    private final String filter;
    private final ArrayList<String> list;
    private final ArrayList<String> exclude;
    private final NetworkTopography networkTopography;

    public SelectorTask(Profile client, String remotePath, String filter) {
        this.client = client;
        this.path = remotePath;
        this.filter = filter;
        this.list = new ArrayList<>();
        this.exclude = new ArrayList<>();
        this.exclude.add("AE_SCRATCH");
        this.exclude.add("AE_COMS");
        this.exclude.add("AE.TEMP");
        this.exclude.add("LC_ASSOC");
        this.exclude.add("LC_COLUMNS");
        this.exclude.add("LC_FKEY");
        this.exclude.add("LC_TABLES");
        this.exclude.add("U2XfrNLS");
        this.exclude.add("New folder");
        this.exclude.add("TJD.BP");
        this.exclude.add("VOCLIB");
        this.exclude.add("VOC");
        this.exclude.add("TRAININGLIB");
        this.exclude.add("PARAMS");
        this.networkTopography = NetworkTopography.getInstance();
    }

    @Override
    protected ArrayList<String> call() throws Exception {
        String nodeName = client.getServer().getName();
        System.out.println(nodeName);
        String acct = client.getAccountName();

        JsonKey hostKey = Jsoner.mintJsonKey(nodeName, new JsonObject());
        JsonObject node = this.networkTopography.getNode(acct);
        JsonObject hosts = node.getMap(NetworkTopography.hostsKey);
        JsonObject host = hosts.getMap(hostKey);
        boolean isSsh = host.getInteger(Jsoner.mintJsonKey("ssh", null)).equals(1);
        
        if (isSsh) {
            return scall();
        }
        FTPClient ftp = new FTPClient();

        ftp.connect(client.getServer().getHost());
        ftp.login(client.getUserName(),
                client.getUserPassword());

        boolean cwdResult = ftp.changeWorkingDirectory(this.path);

        ftp.enterLocalPassiveMode();
        FTPFile[] itemList;

        String pathSpec[] = this.path.split("/");
        String lastChild = pathSpec[pathSpec.length - 1];

        boolean isUvcode = lastChild.matches(".+\\.uv[fistp]");
        boolean isLocal = lastChild.matches("madev");

        if (filter.isEmpty()) {
            itemList = ftp.listDirectories();
        } else {
            itemList = ftp.listFiles(this.filter);
        }

        for (FTPFile file : itemList) {
            String name = file.getName().trim();
            if (name.startsWith("&")) {
                continue;
            }
            if (name.startsWith("D_")) {
                continue;
            }
            if (name.matches(".+\\.uv[fispt]\\.O")) {
                continue;
            }
            if (name.matches(".+\\.\\..+")) {
                continue;
            }
            if (name.startsWith(".") || name.endsWith(".")) {
                continue;
            }
            if (name.contains("junk")) {
                continue;
            }
            if (this.exclude.contains(name)) {
                continue;
            }
            if (this.filter.isEmpty() && isLocal) {
                if (!name.matches(".+\\.uv[fispt]")) {
                    continue;
                }
            }
            if (isUvcode && !name.matches(".+\\.uv[fispt]")) {
                continue;
            }
            list.add(name);
        }
        if (this.filter.isEmpty()) {
            if (nodeName.equalsIgnoreCase("mustang") || nodeName.equalsIgnoreCase("dev")) {
                list.add("BOBL.BP");
            }
            list.add("DM.BP");
            list.add("DM.SR");
            list.add("PADS.HOOK/SEGMENT");
            list.add("PADS.BP");
        }
        return list;
    }

    protected ArrayList<String> scall() throws Exception {
        // 
        JSch jsch = new JSch();
        Session session = jsch.getSession("release", "dmcdev");
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword("R31ea$E_@)!(");
        try {
            session.connect(1000);
        } catch (JSchException e) {
            System.out.println(e.getMessage());
        }
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp = (ChannelSftp) channel;

        String pathSpec[] = this.path.split("/");
        String lastChild = pathSpec[pathSpec.length - 1];

        boolean isUvcode = lastChild.matches(".+\\.uv[fistp]");
        boolean isLocal = lastChild.matches("madev");
        Vector<ChannelSftp.LsEntry> itemList = new Vector<>();
        sftp.cd(this.path);
        boolean isDirList = filter.isEmpty();
        if (isDirList) {
            itemList = sftp.ls(".");
        } else {
            itemList = sftp.ls(this.filter);
        }

        for (ChannelSftp.LsEntry item : itemList) {
            String name = item.getFilename();
            if (name.startsWith("&")) {
                continue;
            }
            if (name.startsWith("D_")) {
                continue;
            }
            if (name.matches(".+\\.uv[fispt]\\.O")) {
                continue;
            }
            if (name.matches(".+\\.\\..+")) {
                continue;
            }
            if (name.startsWith(".") || name.endsWith(".")) {
                continue;
            }
            if (this.exclude.contains(name)) {
                continue;
            }
            if (name.contains("junk")) {
                continue;
            }
            if (this.filter.isEmpty() && isLocal) {
                if (!name.matches(".+\\.uv[fispt]")) {
                    continue;
                }
            }
            if (isUvcode && !name.matches(".+\\.uv[fispt]")) {
                continue;
            }
            list.add(name);
        }
        if (isDirList) {
            if (this.client.getServerName().equalsIgnoreCase("dev")) {
                list.add("BOBL.BP");
            }
            list.add("DM.BP");
            list.add("DM.SR");
            list.add("PADS.HOOK/SEGMENT");
            list.add("PADS.BP");
        }

        return list;
    }

}
