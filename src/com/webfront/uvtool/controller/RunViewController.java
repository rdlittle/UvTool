/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.webfront.app.AbstractApp;
import com.webfront.uvtool.app.UvTool;
import com.webfront.uvtool.model.Profile;
import com.webfront.uvtool.model.Program;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author rlittle
 */
public class RunViewController implements Controller, Initializable {

    @FXML
    ComboBox<Profile> cbReadFrom;

    @FXML
    ComboBox<Profile> cbWriteTo;

    @FXML
    ComboBox<Program> cbAppName;

    @FXML
    TextArea txtCriteria;

    @FXML
    Button btnOk;

    @FXML
    Button btnCancel;

    @FXML
    ProgressBar pbProgress;

    private final Config config = Config.getInstance();
    ResourceBundle res;
    private AbstractApp app;

    public RunViewController() {
        btnCancel = new Button();
        btnOk = new Button();
        cbReadFrom = new ComboBox<>();
        cbWriteTo = new ComboBox<>();
        cbAppName = new ComboBox<>();
        pbProgress = new ProgressBar();
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        res = rb;
        cbAppName.setItems(config.getPrograms());
        cbReadFrom.setItems(config.getProfiles());
        cbWriteTo.setItems(config.getProfiles());
    }
    
    @FXML
    public void exec() {
        
    }

    class ButtonHandler implements EventHandler {

        @Override
        public void handle(Event event) {
            Button btn = (Button) event.getSource();
            String id = btn.getId();
            if (id.equalsIgnoreCase("btnCancel")) {
                launch("viewServer", "titleServer");
            } else if (id.equalsIgnoreCase("btnOk")) {
                launch("viewAccount", "titleAccount");
            } else if (id.equalsIgnoreCase("btnAddUser")) {
                launch("viewUser", "titleUser");
            } else if (id.equalsIgnoreCase("btnOptions")) {
                launch("viewWizard", "titleWizard");
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
            VBox root = viewLoader.<VBox>load();
            VirtualBatchWizardController ctrl = viewLoader.getController();
            ctrl.setStage(stage);
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
