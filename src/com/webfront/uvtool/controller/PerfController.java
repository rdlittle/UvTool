/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.webfront.uvtool.controller;

import asjava.uniclientlibs.UniDynArray;
import asjava.uniobjects.UniFile;
import asjava.uniobjects.UniFileException;
import asjava.uniobjects.UniSession;
import asjava.uniobjects.UniSessionException;
import com.webfront.app.BaseApp;
import com.webfront.u2.client.UvClient;
import com.webfront.u2.model.Profile;
import com.webfront.u2.util.Config;
import com.webfront.u2.util.Progress;
import com.webfront.util.UvConnection;
import com.webfront.uvtool.app.UvTool;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;
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
public class PerfController implements Controller, Initializable, Progress {

    @FXML
    TabPane tabs;
    @FXML
    Menu fileMenu;
    @FXML
    MenuItem menuOoen;
    @FXML
    TextField txtPerfId;
    @FXML
    Button btnOpen;
    @FXML
    Button btnCancel;
    @FXML
    ComboBox<Profile> cbReadFrom;
    @FXML
    ComboBox<Profile> cbWriteTo;
    @FXML
    Circle sourceLed;
    @FXML
    Circle destLed;

    private HashMap<String, PerfTabController> perfMap;
    private UvClient client;
    private final Config config = Config.getInstance();

    //private Stage stage;
    RadialGradient ledOn;
    RadialGradient ledOff;
    List<Stop> stopsOn;
    List<Stop> stopsOff;
    UniSession readSession;
    UniSession writeSession;
    Profile readProfile;
    Profile writeProfile;
    UniDynArray perfRec;
    boolean isNew;

    ResourceBundle res;
    private final String testPerfId = "AZ263012";

    public PerfController() {
        stopsOn = new ArrayList<>();
        stopsOff = new ArrayList<>();
        cbReadFrom = new ComboBox<>();
        cbWriteTo = new ComboBox<>();

        sourceLed = new Circle();
        stopsOn.add(new Stop(0, Color.web("#26ff6B")));
        stopsOn.add(new Stop(1.0, Color.web("#1e6824")));
        ledOn = new RadialGradient(0, -0.02, 0.51, 0.5, 0.97, true, CycleMethod.NO_CYCLE, stopsOn);

        destLed = new Circle();
        stopsOff.add(new Stop(0, Color.web("#cccccc")));
        stopsOn.add(new Stop(1.0, Color.web("#1e6824")));
        ledOff = new RadialGradient(0, -0.02, 0.51, 0.5, 0.67, true, CycleMethod.NO_CYCLE, stopsOff);

        perfMap = new HashMap<>();
    }

    @FXML
    public void onBtnOpen() {
        launch("viewPerfTab", "titlePerf");
    }

    @Override
    public Button getCancelButton() {
        return btnCancel;
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
            UniDynArray perfRec = getPerf(txtPerfId.getText());
            PerfTabController ctrl = (PerfTabController) viewLoader.getController();
            ctrl.setProfile(cbReadFrom.getValue());
            ctrl.setModel(txtPerfId.getText(), perfRec);
            ctrl.setIsNew(isNew);
            ctrl.getCloseButton().addEventHandler(MOUSE_RELEASED,
                    new TabCloser(txtPerfId.getText()));

            ctrl.getSaveButton().addEventHandler(MOUSE_RELEASED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    Tab t = tabs.getSelectionModel().getSelectedItem();
                    writePerf(t.getText());
                }
            });
            Tab tab = new Tab(txtPerfId.getText());
            tab.setContent(root);
            tabs.getTabs().add(tab);
            tabs.getSelectionModel().select(tab);
            perfMap.put(txtPerfId.getText(), ctrl);
            txtPerfId.setText("");

        } catch (IOException ex) {
            Logger.getLogger(UvToolController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class TabCloser implements EventHandler {

        private String perfId;
        private Alert confirmDlg;

        public TabCloser(String id) {
            String alertText = "The PERF has changed,  Do you want to save?";
            this.confirmDlg = new Alert(AlertType.NONE, alertText,
                    ButtonType.YES,
                    ButtonType.NO,
                    ButtonType.CANCEL
            );
            perfId = id;
            confirmDlg.setTitle("PERF Changed");
        }

        @Override
        public void handle(Event event) {
            PerfTabController c = perfMap.get(perfId);
            c.updateRecord();
            if (c.hasChanged()) {
                String answer = getOK();
                if (answer == "YES") {
                    writePerf(this.perfId);
                } else if (answer == "CANCEL") {
                    return;
                }
            }
            Tab t = tabs.getSelectionModel().getSelectedItem();
            tabs.getTabs().remove(t);
        }

        private String getOK() {
            Optional<ButtonType> result = confirmDlg.showAndWait();
            if (result.get() == ButtonType.YES) {
                return "YES";
            } else if (result.get() == ButtonType.NO) {
                return "NO";
            }
            return "CANCEL";
        }
    }

    @Override
    public void setStage(Stage s) {

    }

    private UniDynArray getPerf(String id) {
        UniDynArray perfRec = new UniDynArray();
        try {
            readSession = UvConnection.newSession(cbReadFrom.getValue());
            readSession.connect();
            writeProfile = cbWriteTo.getValue();
            updateLed(sourceLed, readSession.isActive());
            if (writeProfile != null) {
                String junk = cbWriteTo.getValue().toString();
                writeSession = UvConnection.newSession(cbWriteTo.getValue());
                writeSession.connect();
                updateLed(destLed, writeSession.isActive());
            } else {
                writeSession = readSession;
            }
        } catch (UniSessionException ex) {
            Logger.getLogger(BaseApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            String pid = txtPerfId.getText();
            UniFile readFile = readSession.open("PERF");
            UniFile writeFile = writeSession.open("PERF");
            perfRec = new UniDynArray(readFile.read(pid));
            writeFile.write(pid, perfRec);
            readSession.disconnect();
            writeSession.disconnect();
            updateLed(sourceLed, readSession.isActive());
            updateLed(destLed, writeSession.isActive());
            isNew = false;
        } catch (UniFileException ex) {
            try {
                if (ex.toString().contains("This Record was not found")) {
                    readSession.disconnect();
                    writeSession.disconnect();
                    updateLed(sourceLed, readSession.isActive());
                    updateLed(destLed, writeSession.isActive());
                    perfRec = new UniDynArray();
                    isNew = true;
                }
            } catch (UniSessionException ex1) {
                Logger.getLogger(PerfController.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (UniSessionException ex) {
            Logger.getLogger(PerfController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return perfRec;
    }

    private void writePerf(String perfId) {
        UniFile writeFile;
        try {
            PerfTabController c = perfMap.get(perfId);
            c.updateRecord();
            UniDynArray perfRec = c.getRecord();
            writeSession = UvConnection.newSession(cbWriteTo.getValue());
            writeSession.connect();
            updateLed(destLed, writeSession.isActive());
            writeFile = writeSession.open("PERF");
            try {
                writeFile.write(perfId, perfRec);
                c.setCopy();
            } catch (UniFileException ex) {
                Logger.getLogger(PerfController.class.getName()).log(Level.SEVERE, null, ex);
            }
            writeSession.disconnect();
        } catch (UniSessionException ex) {
            Logger.getLogger(PerfController.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateLed(destLed, writeSession.isActive());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        client = new UvClient(this);
        cbReadFrom.setItems(config.getProfiles());
        cbWriteTo.setItems(config.getProfiles());
        sourceLed.setFill(ledOff);
        destLed.setFill(ledOff);
        res = resources;
        txtPerfId.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                launch("viewPerfTab", "titlePerf");
            }
        });
    }

    @Override
    public void display(String message) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void state(String message) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void updateProgressBar(Double p) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void updateLed(Circle led, boolean onOff) {
        led.setFill(onOff ? ledOn : ledOff);
    }

    @Override
    public void updateLed(String host, boolean onOff) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
