/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.view;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;

/**
 *
 * @author rlittle
 */
public class WizPage1 extends WizardPageImpl {
    @FXML
    ComboBox cbProfile;
    @FXML
    Pane pane;

    public WizPage1(String v) {
        super(v);
        cbProfile = new ComboBox();
    }
    
}
