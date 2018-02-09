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
    private int id;
    private char type;
    private String name;
    private String className;
    private char selectType;
    private String selectCriteria;
    private int readProfile;
    private int writeProfile;
    
    public Program() {
        
    }
    
    public Program(int id, char type, String name, String className, char selectType, String criteria, int readProf, int writeProf) {
        this.id=id;
        this.type=type;
        this.name=name;
        this.className=className;
        this.selectType=selectType;
        this.selectCriteria=criteria;
        this.readProfile=readProf;
        this.writeProfile=writeProf;
    }

    @Override
    public String toString() {
        return name;
    }
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

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the type
     */
    public char getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(char type) {
        this.type = type;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the selectType
     */
    public char getSelectType() {
        return selectType;
    }

    /**
     * @param selectType the selectType to set
     */
    public void setSelectType(char selectType) {
        this.selectType = selectType;
    }

    /**
     * @return the selectCriteria
     */
    public String getSelectCriteria() {
        return selectCriteria;
    }

    /**
     * @param selectCriteria the selectCriteria to set
     */
    public void setSelectCriteria(String selectCriteria) {
        this.selectCriteria = selectCriteria;
    }

    /**
     * @return the readProfile
     */
    public int getReadProfile() {
        return readProfile;
    }

    /**
     * @param readProfile the readProfile to set
     */
    public void setReadProfile(int readProfile) {
        this.readProfile = readProfile;
    }

    /**
     * @return the writeProfile
     */
    public int getWriteProfile() {
        return writeProfile;
    }

    /**
     * @param writeProfile the writeProfile to set
     */
    public void setWriteProfile(int writeProfile) {
        this.writeProfile = writeProfile;
    }

}
