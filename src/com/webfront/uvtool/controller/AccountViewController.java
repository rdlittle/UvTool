/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.webfront.uvtool.model.Account;
import com.webfront.uvtool.model.Server;
import com.webfront.uvtool.util.Config;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author rlittle
 */
public class AccountViewController implements Controller, Initializable {

    @FXML
    Button btnSave;
    @FXML
    Button btnCancel;
    @FXML
    Button btnDelete;
    @FXML
    Label lblStatusMessage;
    @FXML
    TextField txtName;
    @FXML
    TextField txtPath;
    @FXML
    ComboBox cbServers;

    ResourceBundle res;
    private final Config config = Config.getInstance();

    /**
     * Initializes the controller class.
     */

    public AccountViewController() {
        btnSave = new Button();
        btnCancel = new Button();
        lblStatusMessage = new Label();
        txtName = new TextField();
        txtPath = new TextField();
        cbServers = new ComboBox();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        res = rb;
        cbServers.getItems().addAll(config.getServers());
    }
    
    @FXML
    public void btnSaveOnAction() {
        Server server = (Server) cbServers.getSelectionModel().getSelectedItem();
        String name = txtName.getText();
        String path = txtPath.getText();
        if(server==null) {
            lblStatusMessage.setText(res.getString("errServerSelect"));
            return;
        } else if(name.isEmpty()) {
            lblStatusMessage.setText(res.getString("errNoAccountPath"));
            return;
        } else if(path.isEmpty()) {
            lblStatusMessage.setText(res.getString("errNoPath"));
            return;
        }
        for(Account a : config.getAccounts()) {
            if(name.equals(a.getName()) && a.getServerName().equalsIgnoreCase(server.getName())) {
                lblStatusMessage.setText(res.getString("errAccountExist"));
                return;
            }
        }
        Account account = new Account();
        account.setServerName(server.getName());
        account.setName(name);
        account.setPath(path);
        config.addAccount(account);
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
