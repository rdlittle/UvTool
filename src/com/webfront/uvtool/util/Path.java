/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.util;

import java.util.HashMap;

/**
 *
 * @author rlittle
 */
public class Path {

    private static final HashMap<String, String> serverMap = new HashMap<>();
    private static final HashMap<String, HashMap<String, String>> codeMap = new HashMap<>();

    public Path() {
        serverMap.put("mustang", "/usr/local/madev");
        serverMap.put("dev", "/uvfs/ma.accounts/");
        serverMap.put("staging", "/uvfs/ma.accounts/");
        serverMap.put("dmc", "/uvfs/ma.accounts/");
        serverMap.put("nlsdev", "/uvfs/ma.accounts/");
        serverMap.put("nlstest", "/uvfs/ma.accounts/");
        serverMap.put("nls", "/uvfs/ma.accounts/");

        for (String server : serverMap.keySet()) {
            codeMap.put(server, new HashMap<>());
        }

        codeMap.get("mustang").put("PADS.BP", "/usr/local/pads2.0/PADS.BP/");
        codeMap.get("mustang").put("PADS.INCLUDE", "/usr/local/pads2.0/PADS.BP/");
        codeMap.get("mustang").put("PADS.HOOK", "/home/uvuser/PADS.HOOK/");
        codeMap.get("mustang").put("PADS.HOOK/SEGMENT", "/home/uvuser/");
        codeMap.get("mustang").put("DM.SR", "/usr/local/madev/");
        codeMap.get("mustang").put("DM.SR", "/usr/local/madev/");
        codeMap.get("mustang").put("DM.INCLUDE", "/usr/local/madev/");
        codeMap.get("mustang").put("uvcode", "/usr/local/madev/");
        codeMap.get("mustang").put("webde", "/usr/local/wde/");

        codeMap.get("dev").put("PADS.BP", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("dev").put("PADS.INCLUDE", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("dev").put("PADS.HOOK", "/uvfs/ma.accounts/dmc/");
        codeMap.get("dev").put("PADS.HOOK/SEGMENT", "/uvfs/ma.accounts/dmc/");
        codeMap.get("dev").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("dev").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("dev").put("DM.INCLUDE", "/uvfs/ma.accounts/dmc/");
        codeMap.get("dev").put("uvcode", "/uvcode/");
        codeMap.get("dev").put("webde", "/uvfs/ma.accounts/dmcrbo/");

        codeMap.get("staging").put("PADS.BP", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("staging").put("PADS.INCLUDE", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("staging").put("PADS.HOOK", "/uvfs/ma.accounts/PADS.HOOK/PADS.HOOK/");
        codeMap.get("staging").put("PADS.HOOK/SEGMENT", "/uvfs/ma.accounts/PADS.HOOK/SEGMENT/");
        codeMap.get("staging").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("staging").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("staging").put("DM.INCLUDE", "/uvfs/ma.accounts/dmc/");
        codeMap.get("staging").put("uvcode", "/uvcode/");
        codeMap.get("staging").put("webde", "/uvfs/ma.accounts/dmcrbo/");

        codeMap.get("dmc").put("PADS.BP", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("dmc").put("PADS.INCLUDE", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("dmc").put("PADS.HOOK", "/uvfs/ma.accounts/PADS.HOOK/PADS.HOOK/");
        codeMap.get("dmc").put("PADS.HOOK/SEGMENT", "/uvfs/ma.accounts/PADS.HOOK/SEGMENT/");
        codeMap.get("dmc").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("dmc").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("dmc").put("DM.INCLUDE", "/uvfs/ma.accounts/dmc/");
        codeMap.get("dmc").put("uvcode", "/uvcode/");
        codeMap.get("dmc").put("webde", "/uvfs/ma.accounts/dmcrbo/");
        
        codeMap.get("nlsdev").put("PADS.BP", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("nlsdev").put("PADS.INCLUDE", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("nlsdev").put("PADS.HOOK", "/uvfs/ma.accounts/PADS.HOOK/PADS.HOOK/");
        codeMap.get("nlsdev").put("PADS.HOOK/SEGMENT", "/uvfs/ma.accounts/PADS.HOOK/SEGMENT/");
        codeMap.get("nlsdev").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("nlsdev").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("nlsdev").put("DM.INCLUDE", "/uvfs/ma.accounts/dmc/");
        codeMap.get("nlsdev").put("uvcode", "/uvcode/");
        codeMap.get("nlsdev").put("webde", "/uvfs/ma.accounts/dmcrbo/");

        codeMap.get("nlstest").put("PADS.BP", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("nlstest").put("PADS.INCLUDE", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("nlstest").put("PADS.HOOK", "/uvfs/ma.accounts/PADS.HOOK/PADS.HOOK/");
        codeMap.get("nlstest").put("PADS.HOOK/SEGMENT", "/uvfs/ma.accounts/PADS.HOOK/SEGMENT/");
        codeMap.get("nlstest").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("nlstest").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("nlstest").put("DM.INCLUDE", "/uvfs/ma.accounts/dmc/");
        codeMap.get("nlstest").put("uvcode", "/uvcode/");
        codeMap.get("nlstest").put("webde", "/uvfs/ma.accounts/dmcrbo/");

        codeMap.get("nls").put("PADS.BP", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("nls").put("PADS.INCLUDE", "/uvfs/ma.accounts/pads2.0/PADS.BP/");
        codeMap.get("nls").put("PADS.HOOK", "/uvfs/ma.accounts/PADS.HOOK/PADS.HOOK/");
        codeMap.get("nls").put("PADS.HOOK/SEGMENT", "/uvfs/ma.accounts/PADS.HOOK/SEGMENT/");
        codeMap.get("nls").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("nls").put("DM.SR", "/uvfs/ma.accounts/dmc/");
        codeMap.get("nls").put("DM.INCLUDE", "/uvfs/ma.accounts/dmc/");
        codeMap.get("nls").put("uvcode", "/uvcode/");
        codeMap.get("nls").put("webde", "/uvfs/ma.accounts/dmcrbo/");
        

    }

    public static String getPath(String server, String fileName) {
        String cm = codeMap.get(server).get(fileName);
        return codeMap.get(server).get(fileName);
    }

}
