/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.webfront.uvtool.app.UvTool;
import com.webfront.uvtool.model.Profile;
import com.webfront.uvtool.util.Config;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author rlittle
 */
public class RunViewController implements Controller, Initializable {

    @FXML
    ComboBox<Profile> cbProfile;
    
    @FXML
    Label statusMessage;
    
    @FXML
    Button btnRun;
    
    @FXML
    Button btnCancel;
    
    @FXML
    TextField txtProgram;
    
    @FXML
    TextField txtLibrary;
    
    @FXML
    TextArea txtResponse;
    
    @FXML
    ToggleGroup progType;
    
    @FXML
    RadioButton rbProgram;
    
    @FXML
    RadioButton rbSubroutine;
    
    @FXML
    Pane optionPane;
            
    private final Config config = Config.getInstance();
    ResourceBundle res;
    
    public RunViewController() {
        btnCancel = new Button();
        btnRun = new Button();
        cbProfile = new ComboBox<>();
    }
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        res = rb;
        cbProfile.setItems(config.getProfiles());
    } 
    
    class ButtonHandler implements EventHandler {

        @Override
        public void handle(Event event) {
            Button btn = (Button) event.getSource();
            String id = btn.getId();
            if(id.equalsIgnoreCase("btnAddServer")) {
                launch("viewServer","titleServer");
            } else if(id.equalsIgnoreCase("btnAddAccount")) {
                launch("viewAccount","titleAccount");
            } else if(id.equalsIgnoreCase("btnAddUser")) {
                launch("viewUser","titleUser");
            }
        }
    }
    
    @Override
    public void launch(String view, String title) {
        FXMLLoader viewLoader = new FXMLLoader();
        viewLoader.setLocation(UvTool.class.getResource(res.getString(view)));
        viewLoader.setResources(res);
        try {
            Stage stage = new Stage();
            stage.setTitle(res.getString(title));
            AnchorPane root = viewLoader.<AnchorPane>load();
            Controller ctrl = viewLoader.getController();
            
            ctrl.getCancelButton().setOnAction(new EventHandler() {
                @Override
                public void handle(Event event) {
                   stage.close();
                }
            });
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(UvToolController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Override
    public Button getCancelButton() {
        return btnCancel;
    }
    
}
