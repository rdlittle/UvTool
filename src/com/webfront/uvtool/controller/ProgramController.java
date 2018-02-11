/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author rlittle
 */
public class ProgramController implements Controller, Initializable {
    
    @FXML
    Button btnCancel;
    
    @FXML
    Button btnDelete;
    
    @FXML
    Button btnSave;
    
    @FXML
    TextArea txtLocalFiles;
    
    @FXML
    TextArea txtRemoreFiles;
    
    @FXML
    TextField txtAppName;
    
    @FXML
    TextField txtPackage;
        
    
    @Override
    public void launch(String v, String t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Button getCancelButton() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
