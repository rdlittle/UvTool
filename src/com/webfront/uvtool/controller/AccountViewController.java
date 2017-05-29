/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.webfront.uvtool.model.Account;
import com.webfront.uvtool.model.Server;
import com.webfront.uvtool.util.AccountConverter;
import com.webfront.uvtool.util.Config;
import com.webfront.uvtool.util.ServerConverter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
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
    TextField txtPath;
    @FXML
    ComboBox<Account> cbAccount;
    @FXML
    ComboBox<Server> cbServers;

    ResourceBundle res;
    private final Config config = Config.getInstance();
    private final FilteredList<Account> filteredAccountList;
    private Account selectedAccount;

    /**
     * Initializes the controller class.
     */
    public AccountViewController() {
        filteredAccountList = new FilteredList<>(config.getAccounts());
        filteredAccountList.setPredicate((e) -> true);
        selectedAccount = new Account();
        
        btnSave = new Button();
        btnCancel = new Button();
        lblStatusMessage = new Label();
        txtPath = new TextField();
        cbAccount = new ComboBox<>();
        cbAccount.converterProperty().set(new AccountConverter());
        
        cbServers = new ComboBox<>();
        cbServers.converterProperty().set(new ServerConverter());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        res = rb;
        cbAccount.setItems(filteredAccountList);
        cbAccount.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                selectedAccount = (Account) newValue;
                txtPath.setText(((Account) newValue).getPath());
            }
        });
        
        cbServers.getItems().addAll(config.getServers());
        cbServers.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                Server s = (Server) newValue;
                String serverName = s.getName();
                filteredAccountList.setPredicate((a) -> a.getServerName().equalsIgnoreCase(serverName));
                selectedAccount = new Account();
                txtPath.setText("");
            }
        });
        
        txtPath.textProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                selectedAccount.setPath(((TextField)newValue).getText());
            }
        });
        
    }

    @FXML
    public void btnSaveOnAction() {
        Server server = cbServers.getValue();
        String name = cbAccount.getValue().getName();
        String path = txtPath.getText();
        if (server == null) {
            lblStatusMessage.setText(res.getString("errServerSelect"));
            return;
        } else if (name.isEmpty()) {
            lblStatusMessage.setText(res.getString("errNoAccountPath"));
            return;
        } else if (path.isEmpty()) {
            lblStatusMessage.setText(res.getString("errNoPath"));
            return;
        }
        for (Account a : config.getAccounts()) {
            if (name.equals(a.getName()) && a.getServerName().equalsIgnoreCase(server.getName())) {
                config.updateAccount(a);
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
