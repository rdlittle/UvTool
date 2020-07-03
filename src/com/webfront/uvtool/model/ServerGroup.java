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
public class ServerGroup {

    private final String name;
    private final String codeBase;
    private final Map<String, String> hosts;
    private final Map<String, String> paths;
    private final boolean isSsh;

    public ServerGroup(JsonObject json, String n) {
        this.name = n;
        JsonKey key = Jsoner.mintJsonKey("platforms", new JsonObject());
        JsonKey hostsKey = Jsoner.mintJsonKey("nodes", new JsonObject());
        JsonKey pathKey = Jsoner.mintJsonKey("paths", new JsonObject());
        JsonKey codeBaseKey = Jsoner.mintJsonKey("codebase", new String());
        JsonKey sshKey = Jsoner.mintJsonKey("ssh", true);
        Map<Object, JsonObject> platforms = json.getMap(key);
        
        JsonObject platform = platforms.get(this.name);
        this.codeBase = platform.getString(codeBaseKey);
        this.hosts = platform.getMap(hostsKey);
        this.paths = platform.getMap(pathKey);
        this.isSsh = platform.getMap(sshKey);
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
    
    public boolean isSsh() {
        return this.isSsh;
    }
}
