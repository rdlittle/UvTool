/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.util;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rlittle
 */
public class ConfigProperties {

    public static ConfigProperties instance = null;
    private final String JSON_PATH = "/com/webfront/uvtool/util/config.json";
    private JsonObject platforms;
    private String cbHost;

    protected ConfigProperties() {
        platforms = new JsonObject();
        InputStream istream = this.getClass().getResourceAsStream(JSON_PATH);
        InputStreamReader reader = new InputStreamReader(istream);
        StringBuilder builder = new StringBuilder();
        try {
            int r = istream.available();
            for (;;) {
                int c = reader.read();
                if (c == -1) {
                    break;
                }
                builder.append((char) c);
            }
            platforms = Jsoner.deserialize(builder.toString(), new JsonObject());
        } catch (IOException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        }
        cbHost = platforms.get("cb_host").toString();
    }
    
    public static ConfigProperties getInstance() {
        if (instance == null) {
            instance = new ConfigProperties();
        }
        return instance;
    }
    
    public JsonObject getPlatforms() {
        return platforms;
    }
    
    public String getCbHost() {
        return cbHost;
    }
}
