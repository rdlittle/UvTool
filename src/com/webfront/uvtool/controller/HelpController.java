/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.webfront.uvtool.app.UvTool;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 *
 * @author rlittle
 */

public class HelpController implements Controller, Initializable {

    private ResourceBundle resources;
    private URL location;

    @FXML
    private Button btnOk;
    
    @FXML
    private WebView webview;
    
    private Stage stage;

    public HelpController() {
        btnOk = new Button();
        webview = new WebView();
    }

    @FXML
    public void initialize(URL url, ResourceBundle rb) {
        location = url;
        resources = rb;
        URL helpUrl = UvTool.class.getResource("resources/help.html");
        webview.getEngine().load(helpUrl.toExternalForm());
    }    
    
    @Override
    public void setStage(Stage s) {
        this.stage = s;
    }


    @Override
    public Button getCancelButton() {
        return btnOk;
    }

    @Override
    public void launch(String v, String t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

