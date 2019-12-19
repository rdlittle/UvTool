/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.model;

import com.github.cliftonlabs.json_simple.JsonKey;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import java.util.Map;

/**
 *
 * @author rlittle
 */
public class Server {

    private final String name;
    private final String codeBase;
    private final Map<String, String> hosts;
    private final Map<String, String> paths;

    public Server(JsonObject json, String n) {
        this.name = n;
        JsonKey key = Jsoner.mintJsonKey("platforms", new JsonObject());
        JsonKey serverKey = Jsoner.mintJsonKey("servers", new JsonObject());
        JsonKey pathKey = Jsoner.mintJsonKey("paths", new JsonObject());
        JsonKey codeBaseKey = Jsoner.mintJsonKey("codebase", new String());
        Map<Object, JsonObject> platforms = json.getMap(key);
        
        JsonObject platform = platforms.get(this.name);
        this.codeBase = platform.getString(codeBaseKey);
        this.hosts = platform.getMap(serverKey);
        this.paths = platform.getMap(pathKey);
    }

    /**
     * @return the codeBase
     */
    public String getCodeBase() {
        return codeBase;
    }
    
    public String getHost(String h) {
        return hosts.get(h);
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }    

    public String getPath(String p) {
        return paths.get(p);
    }
}
