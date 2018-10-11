/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.util;

import asjava.uniobjects.UniCommand;
import asjava.uniobjects.UniCommandException;
import asjava.uniobjects.UniSelectList;
import asjava.uniobjects.UniSelectListException;
import com.webfront.u2.client.UvClient;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author rlittle
 */
public class SelectorTask extends Task<ArrayList<String>> {

    private UvClient client;
    private String stmt;
    private ArrayList<String> list;

    public SelectorTask(UvClient client, String stmt) {
        this.client = client;
        this.stmt = stmt;
        this.list = new ArrayList<>();
    }

    @Override
    protected ArrayList<String> call() throws Exception {
        client.doSingleConnect("source");
        try {
            UniCommand cmd = client.getSourceSession().getSession().command(stmt);
            cmd.exec();
            UniSelectList selectList = client.getSourceSession().getSession().selectList(0);
            while (!selectList.isLastRecordRead()) {
                String rec = selectList.next().toString();
                if (rec.isEmpty()) {
                    continue;
                }
                list.add(rec);
            }
        } catch (UniCommandException ex) {
            Logger.getLogger(SelectorTask.class.getName()).log(Level.SEVERE, ex.getMessage());
        } catch (UniSelectListException ex) {
            Logger.getLogger(SelectorTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        client.doSingleDisconnect("source");
        return list;
    }

}
