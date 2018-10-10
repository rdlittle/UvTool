/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 *
 * @author rlittle
 */
public interface Controller {
    public Button getCancelButton();
    public void launch(String v,String t);
    public void setStage(Stage s);
}
