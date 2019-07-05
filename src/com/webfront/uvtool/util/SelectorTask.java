/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.util;

import com.webfront.u2.model.Profile;
import java.util.ArrayList;
import javafx.concurrent.Task;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

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

    public SelectorTask(Profile client, String remotePath, String filter) {
        this.client = client;
        this.path = remotePath;
        this.filter = filter;
        this.list = new ArrayList<>();
    }

    @Override
    protected ArrayList<String> call() throws Exception {
        FTPClient ftp = new FTPClient();

        ftp.connect(client.getServer().getHost());
        ftp.login(client.getUserName(),
                client.getUserPassword());

        ftp.changeWorkingDirectory(this.path);

        ftp.enterLocalPassiveMode();
        FTPFile[] itemList;

        boolean isUvcode = this.path.startsWith("/usr/local/madev") ||
                !(this.path.endsWith("DM.SR") || this.path.endsWith("DM.BP")
                || (this.path.contains("PADS"))
                || (this.path.contains("pads2.0")));

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
            if (name.matches(".+\\.uv[f,i,s,p,t]\\.O")) {
                continue;
            }
            
            if (isUvcode && !name.matches(".+\\.uv[f,i,s,p,t]")) {
                continue;
            }
            list.add(name);
        }
        if (this.filter.isEmpty()) {
            list.add("DM.BP");
            list.add("DM.SR");
            list.add("PADS.HOOK/SEGMENT");
            list.add("PADS.BP");
        }
        return list;
    }

}
