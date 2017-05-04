/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.model;

import java.util.HashMap;

/**
 *
 * @author rlittle
 */
public class Program {
    private HashMap<Integer,String> iList;
    private HashMap<Integer,String> oList;
    private HashMap<Integer,String> eList;
    private HashMap<String,Integer> iListProperties;
    private HashMap<String,Integer> oListProperties;
    private HashMap<String,Integer> eListProperties;
    
    /**
     * @return the iList
     */
    public HashMap<Integer,String> getiList() {
        return iList;
    }

    /**
     * @param iList the iList to set
     */
    public void setiList(HashMap<Integer,String> iList) {
        this.iList = iList;
    }

    /**
     * @return the oList
     */
    public HashMap<Integer,String> getoList() {
        return oList;
    }

    /**
     * @param oList the oList to set
     */
    public void setoList(HashMap<Integer,String> oList) {
        this.oList = oList;
    }

    /**
     * @return the eList
     */
    public HashMap<Integer,String> geteList() {
        return eList;
    }

    /**
     * @param eList the eList to set
     */
    public void seteList(HashMap<Integer,String> eList) {
        this.eList = eList;
    }

    /**
     * @return the iListProperties
     */
    public HashMap<String,Integer> getiListProperties() {
        return iListProperties;
    }

    /**
     * @param iListProperties the iListProperties to set
     */
    public void setiListProperties(HashMap<String,Integer> iListProperties) {
        this.iListProperties = iListProperties;
    }

    /**
     * @return the oListProperties
     */
    public HashMap<String,Integer> getoListProperties() {
        return oListProperties;
    }

    /**
     * @param oListProperties the oListProperties to set
     */
    public void setoListProperties(HashMap<String,Integer> oListProperties) {
        this.oListProperties = oListProperties;
    }

    /**
     * @return the eListProperties
     */
    public HashMap<String,Integer> geteListProperties() {
        return eListProperties;
    }

    /**
     * @param eListProperties the eListProperties to set
     */
    public void seteListProperties(HashMap<String,Integer> eListProperties) {
        this.eListProperties = eListProperties;
    }

}
