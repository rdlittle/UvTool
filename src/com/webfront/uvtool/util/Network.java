/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.util;

import com.github.cliftonlabs.json_simple.*;
import com.webfront.uvtool.controller.DeployBackupController;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rlittle
 */
public class Network {

    private final String cbHost = "http://corvette";
    private final String cbUser = "release";
    private final String cbPassword = "R31ea$E_@)!(";

    private final String CONFIG_PATH = "/com/webfront/uvtool/util/config.json";
    private JsonObject platforms;

    public Network() {
        platforms = new JsonObject();
        InputStream istream = this.getClass().getResourceAsStream(CONFIG_PATH);
        InputStreamReader reader = new InputStreamReader(istream);
        StringBuilder builder = new StringBuilder();
        try {
            int r = istream.available();
            String msg = "Config size: " + Integer.toString(r);
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.INFO, msg);
            for (;;) {
                int c = reader.read();
                if (c == -1) {
                    break;
                }
                builder.append((char) c);
            }
            platforms = Jsoner.deserialize(builder.toString(), new JsonObject());
            System.out.println(platforms.toString());
        } catch (IOException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JsonObject getPlatforms() {
        return platforms;
    }

    public String sshExec(String host, String path, String cmd) {

        return "";
    }
}
