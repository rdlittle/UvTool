/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.webfront.u2.util.Config;

import com.webfront.u2.model.Profile;
import com.webfront.u2.util.Progress;
import com.webfront.uvtool.app.UvTool;
//import com.webfront.uvtool.util.Progress;
import com.webfront.uvtool.util.UvClient;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author rlittle
 */
public class PullSourceCodeController implements Controller, Initializable, Progress {

    public final String stmt = "SSELECT VOC WITH F1 = \"F]\" AND WITH @ID = \"DM.SR\" \"DM.BP\" \"[.uv]\" AND WITH @ID # \"[.O\"";
    
    @FXML
    private Button btnCancel;

    @FXML
    private Button btnOk;

    @FXML
    Circle sourceLed;

    @FXML
    Circle destLed;

    @FXML
    private ComboBox<Profile> cbSourceProfile;

    @FXML
    private ComboBox<Profile> cbDestProfile;
    
    @FXML
    private ComboBox<String> cbFromFile;
    
    @FXML
    private ComboBox<String> cbToFile;

    @FXML
    private ListView<String> lvSourceItems;

    @FXML
    private ListView<String> lvDestItems;

    @FXML
    ProgressBar progressBar;

    RadialGradient ledOff;
    RadialGradient ledOn;
    List<Stop> stopsOn;
    List<Stop> stopsOff;

    ResourceBundle res;

    SimpleObjectProperty<Profile> sourceProfileProperty;
    SimpleObjectProperty<Profile> destProfileProperty;

    private final Config config;
    
    UvClient client;

    public PullSourceCodeController() {
        config = Config.getInstance();
        stopsOn = new ArrayList<>();
        stopsOff = new ArrayList<>();
        btnCancel = new Button();
        btnOk = new Button();
        cbFromFile = new ComboBox<>();
        cbToFile = new ComboBox<>();
        cbSourceProfile = new ComboBox<>();
        cbDestProfile = new ComboBox<>();
        
        sourceProfileProperty = new SimpleObjectProperty<>();
        destProfileProperty = new SimpleObjectProperty<>();
        stopsOn.add(new Stop(0, Color.web("#26ff6B")));
        stopsOn.add(new Stop(1.0, Color.web("#1e6824")));
        ledOn = new RadialGradient(0, -0.02, 0.51, 0.5, 0.97, true, CycleMethod.NO_CYCLE, stopsOn);

        stopsOff.add(new Stop(0, Color.web("#cccccc")));
        stopsOn.add(new Stop(1.0, Color.web("#1e6824")));
        ledOff = new RadialGradient(0, -0.02, 0.51, 0.5, 0.67, true, CycleMethod.NO_CYCLE, stopsOff);

        progressBar = new ProgressBar();
        sourceLed = new Circle();
        destLed = new Circle();

        lvSourceItems = new ListView<>();
        lvDestItems = new ListView<>();
        
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        res = rb;
        cbSourceProfile.setItems(config.getProfiles());
        cbDestProfile.setItems(config.getProfiles());
        client = new UvClient(this);
    }

    @Override
    public void updateLed(String host, boolean onOff) {
        if (host.equalsIgnoreCase("source")) {
            sourceLed.setFill(onOff ? ledOn : ledOff);
        } else {
            destLed.setFill(onOff ? ledOn : ledOff);
        }
    }

    @Override
    public void updateProgressBar(Double p) {
        Platform.runLater(() -> progressBar.progressProperty().setValue(p));
    }

    @Override
    public void state(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void display(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void launch(String view, String title) {
        FXMLLoader viewLoader = new FXMLLoader();
        String v = res.getString(view);
        String t = res.getString(title);
        URL url = UvTool.class.getResource(v);
        viewLoader.setLocation(url);
        viewLoader.setResources(res);
        try {
            Pane root = viewLoader.<Pane>load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle(t);
            Controller ctrl = viewLoader.getController();
            ctrl.getCancelButton().setOnAction(new EventHandler() {
                @Override
                public void handle(Event event) {
                    ctrl.getCancelButton().removeEventHandler(EventType.ROOT, this);
                    stage.close();
                }
            });

            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(UvToolController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    public void onOK() {
        
    }

    @Override
    public Button getCancelButton() {
        return this.btnCancel;
    }
    
    @FXML
    public void onSourceProfileChange() {
        client.setSourceProfile(cbSourceProfile.getValue());
    }
    
    @FXML
    public void onDestProfileChange() {
        
    }

}
