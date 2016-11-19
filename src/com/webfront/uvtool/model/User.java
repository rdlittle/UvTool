/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.model;

/**
 *
 * @author rlittle
 * create table users (id integer primary key autoincrement, name char(16), password char(256))
 */
public class User {
    private int id;
    private String name;
    private String password;

    public User() {
        id = -1;
        name = "";
        password = "";
    }
    
    public User(int i, String n, String p) {
        id = i;
        name = n;
        password = p;
    }
    
    public User(String n, String p) {
        name = n;
        password = p;
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
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
