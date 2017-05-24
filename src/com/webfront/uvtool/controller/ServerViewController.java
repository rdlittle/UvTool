/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.webfront.uvtool.model.Server;
import com.webfront.uvtool.util.Config;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author rlittle
 */
public class ServerViewController implements Controller, Initializable {

    @FXML
    Button btnSave;
    @FXML
    Button btnCancel;

    @FXML
    TextField txtHostName;
    @FXML
    TextField txtServerName;

    @FXML
    Label lblStatusMessage;

    ResourceBundle res;

    public ServerViewController() {
        btnCancel = new Button();
        btnSave = new Button();
        lblStatusMessage = new Label();
        txtHostName = new TextField();
        txtServerName = new TextField();
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.res = rb;
        txtHostName.setPromptText("Enter host name");
        txtHostName.disableProperty().bind(txtServerName.textProperty().isEmpty());
        this.btnSave.disableProperty().bind(this.txtHostName.textProperty().isEmpty());
    }

    @FXML
    public void btnSaveOnAction() {
        String serverName = txtServerName.getText();
        String hostName = txtHostName.getText();
        if (serverName.isEmpty()) {
            lblStatusMessage.setText(res.getString("errNoServerName"));
            txtServerName.requestFocus();
        } else if (hostName.isEmpty()) {
            lblStatusMessage.setText(res.getString("errNoHostName"));
            txtHostName.requestFocus();
        } else {
            Config cfg = Config.getInstance();
            lblStatusMessage.setText("");
            Server s = new Server();
            s.setHost(hostName);
            s.setName(serverName);
            cfg.addServer(s);
            txtHostName.clear();
            txtServerName.clear();
            btnCancel.fire();
        }
    }

    @Override
    public Button getCancelButton() {
        return btnCancel;
    }

    @Override
    public void launch(String v, String t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
