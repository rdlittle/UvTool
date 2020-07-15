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
public class NetworkNode {

    private final String name;
    private final String codeBase;
    private final Map<String, JsonObject> hosts;
    private final Map<String, String> paths;
    private final JsonKey hostNameKey;
    private final JsonKey sshKey;

    public NetworkNode(JsonObject networkNodes, String n) {
        this.name = n;
        JsonKey hostsKey = Jsoner.mintJsonKey("hosts", new JsonObject());
        JsonKey pathKey = Jsoner.mintJsonKey("paths", new JsonObject());
        JsonKey codeBaseKey = Jsoner.mintJsonKey("codebase", new String());
        JsonObject node = (JsonObject)networkNodes.get(this.name);
        this.codeBase = node.getString(codeBaseKey);
        this.hosts = node.getMap(hostsKey);
        this.paths = node.getMap(pathKey);
        this.hostNameKey = Jsoner.mintJsonKey("name", new JsonObject());
        this.sshKey = Jsoner.mintJsonKey("ssh", true);
    }

    /**
     * @return the codeBase
     */
    public String getCodeBase() {
        return codeBase;
    }
    
    public String getHost(String platform) {
        JsonObject node = hosts.get(platform);
        String hostName = node.getString(this.hostNameKey);
        return hostName;
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
    
    public boolean isSsh(String platform) {
        JsonObject node = hosts.get(platform);
        return node.getBoolean(this.sshKey);
    }
}
