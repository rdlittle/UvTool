/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.util;

import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import com.webfront.u2.util.Config;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rlittle
 */
public class NetworkTopography {

    public static NetworkTopography instance = null;
    private final String JSON_PATH = Config.configPath;
    private JsonObject nodes;
    public static JsonKey hosts;
    public static JsonKey paths;
    public static JsonKey dev;
    public static JsonKey staging;
    public static JsonKey live;
    public static JsonKey codebase;
    private String cbHost;

    protected NetworkTopography() {
        InputStream istream = null;
        try {
            nodes = new JsonObject();
            
            hosts = Jsoner.mintJsonKey("hosts", new JsonObject());
            paths = Jsoner.mintJsonKey("paths", new JsonObject());
            dev = Jsoner.mintJsonKey("dev", new JsonObject());
            staging = Jsoner.mintJsonKey("staging", new JsonObject());
            live = Jsoner.mintJsonKey("live", new JsonObject());
            codebase = Jsoner.mintJsonKey("codebase", new String());
            istream = new FileInputStream(new File(JSON_PATH));
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
                nodes = Jsoner.deserialize(builder.toString(), new JsonObject());
            } catch (IOException ex) {
                Logger.getLogger(NetworkOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            JsonObject cbObject = nodes.getMap(Jsoner.mintJsonKey("couchbase", new JsonObject()));
            JsonObject cbHosts = cbObject.getMap(this.hosts);
            cbHost = cbHosts.getString(this.staging);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetworkTopography.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                istream.close();
            } catch (IOException ex) {
                Logger.getLogger(NetworkTopography.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static NetworkTopography getInstance() {
        if (instance == null) {
            instance = new NetworkTopography();
        }
        return instance;
    }
    
    public JsonObject getNodes() {
        return nodes;
    }
    
    public String getCbHost() {
        return cbHost;
    }
}
