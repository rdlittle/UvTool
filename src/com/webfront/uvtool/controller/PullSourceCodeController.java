/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import asjava.uniclientlibs.UniDynArray;
import asjava.uniobjects.UniFile;
import asjava.uniobjects.UniFileException;
import asjava.uniobjects.UniSessionException;
import com.webfront.u2.util.Config;

import com.webfront.u2.model.Profile;
import com.webfront.u2.util.Progress;
import com.webfront.u2.client.UvClient;
import com.webfront.uvtool.util.SelectorTask;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 *
 * @author rlittle
 */
public class PullSourceCodeController implements Controller, Initializable, Progress {

    public final String stmt = "SSELECT VOC "
            + "WITH F1 = \"F]\" "
            + "AND WITH @ID = \"DM.SR\" \"DM.BP\" \"[.uv]\" "
            + "AND WITH @ID # \"[.O\" AND WITH @ID # \"[.L\" "
            + "AND WITH @ID # \"DMC.]\" "
            + "AND WITH @ID # \"DMCLIVE.]\" "
            + "AND WITH @ID # \"DMCTEST.]\" "
            + "AND WITH @ID # \"NLSDEV.]\" "
            + "AND WITH @ID # \"MA.]\" ";

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnOk;

    @FXML
    private Button btnOneLeft;

    @FXML
    private Button btnAllLeft;

    @FXML
    private Button btnOneRight;

    @FXML
    private Button btnAllRight;

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
    private TextField txtStatus;

    @FXML
    ProgressBar progressBar;

    RadialGradient ledOff;
    RadialGradient ledOn;
    List<Stop> stopsOn;
    List<Stop> stopsOff;

    ResourceBundle res;

    SimpleObjectProperty<Profile> sourceProfileProperty;
    SimpleObjectProperty<Profile> destProfileProperty;

    private ObservableList<String> sourceFileList;
    private ObservableList<String> fileItems;

    private final Config config;
    private Stage stage;

    UvClient client;

    public PullSourceCodeController() {
        config = Config.getInstance();
        stopsOn = new ArrayList<>();
        stopsOff = new ArrayList<>();
        btnCancel = new Button();
        btnOk = new Button();
        btnOneLeft = new Button();
        btnOneRight = new Button();
        btnAllLeft = new Button();
        btnAllRight = new Button();
        cbFromFile = new ComboBox<>();
        cbToFile = new ComboBox<>();
        cbSourceProfile = new ComboBox<>();
        cbDestProfile = new ComboBox<>();

        sourceProfileProperty = new SimpleObjectProperty<>();
        destProfileProperty = new SimpleObjectProperty<>();
        sourceFileList = FXCollections.observableArrayList();
        fileItems = FXCollections.observableArrayList();
        stopsOn.add(new Stop(0, Color.web("#26ff6B")));
        stopsOn.add(new Stop(1.0, Color.web("#1e6824")));
        ledOn = new RadialGradient(0, -0.02, 0.51, 0.5, 0.97, true, CycleMethod.NO_CYCLE, stopsOn);

        stopsOff.add(new Stop(0, Color.web("#cccccc")));
        stopsOn.add(new Stop(1.0, Color.web("#1e6824")));
        ledOff = new RadialGradient(0, -0.02, 0.51, 0.5, 0.67, true, CycleMethod.NO_CYCLE, stopsOff);

        progressBar = new ProgressBar();
        sourceLed = new Circle();
        destLed = new Circle();

        lvSourceItems = new ListView<>(fileItems);
        lvDestItems = new ListView<>();

        txtStatus = new TextField();
    }

    @Override
    public void setStage(Stage s) {
        this.stage = s;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        res = rb;
        lvSourceItems.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        cbSourceProfile.setItems(config.getProfiles());
        for (Profile p : config.getProfiles()) {
            String svrName = p.getServer().getName();
            if (svrName.equals("dmc") || svrName.equals("staging") || svrName.equals("dmctw") || svrName.endsWith("test")) {
                continue;
            }
            cbDestProfile.getItems().add(p);
        }
        client = new UvClient(this);

        cbFromFile.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                fileItems.clear();
                if (newValue != null) {
                    String fileName = newValue.toString();
                    cbToFile.setValue(fileName);
                    getFileItems(fileName);
                }
            }
        });

        SimpleListProperty slp = new SimpleListProperty(fileItems);
        SimpleListProperty dlp = new SimpleListProperty(lvDestItems.getItems());

        btnOk.disableProperty().bind(dlp.emptyProperty().or(cbDestProfile.getSelectionModel().selectedItemProperty().isNull()));

        btnAllLeft.disableProperty().bind(dlp.emptyProperty());
        btnAllRight.disableProperty().bind(slp.emptyProperty());
        btnOneLeft.disableProperty().bind(lvDestItems.getSelectionModel().selectedItemProperty().isNull());
        btnOneRight.disableProperty().bind(lvSourceItems.getSelectionModel().selectedItemProperty().isNull());
    }

    @Override
    public void updateLed(String host, boolean onOff) {
        if (host.equalsIgnoreCase("source")) {
            Platform.runLater(() -> sourceLed.setFill(onOff ? ledOn : ledOff));
        } else {
            Platform.runLater(() -> destLed.setFill(onOff ? ledOn : ledOff));
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
        txtStatus.setText(message);
    }

    @Override
    public void launch(String view, String title) {
    }

    @FXML
    public void onOk() {
        try {
            if (cbDestProfile.getValue() == null) {
                txtStatus.setText("Please select a destination");
                return;
            }
            if (cbDestProfile.getValue() == cbSourceProfile.getValue()) {
                txtStatus.setText("Source and destination profiles must be different");
                return;
            }

            txtStatus.clear();
            String fileName = cbFromFile.getValue();
            if (client.doConnect()) {
                UniFile sourceFile = client.getSourceSession().getSession().openFile(fileName);
                UniFile destFile = client.getDestSession().getSession().openFile(fileName);
                ObservableList<String> items = FXCollections.<String>observableArrayList();
                items.setAll(lvDestItems.getItems());
                for (String itemId : items) {
                    UniDynArray uda = new UniDynArray(sourceFile.read(itemId));
                    destFile.write(itemId, uda);
                    lvDestItems.getItems().remove(itemId);
                }
                sourceFile.close();
                destFile.close();
                client.doDisconnect();
            }
        } catch (UniSessionException ex) {
            Logger.getLogger(PullSourceCodeController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UniFileException ex) {
            Logger.getLogger(PullSourceCodeController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Button getCancelButton() {
        return this.btnCancel;
    }

    @FXML
    public void onSourceProfileChange() {
        sourceFileList.clear();
        cbFromFile.disableProperty().set(true);
        SelectorTask selectorTask = new SelectorTask(client, stmt);
        selectorTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                ArrayList<String> list = selectorTask.getValue();
                sourceFileList.addAll(list);
                cbFromFile.setItems(sourceFileList);
                cbFromFile.disableProperty().set(false);
                stage.getScene().setCursor(Cursor.DEFAULT);
            }
        });
        stage.getScene().setCursor(Cursor.WAIT);
        Thread t = new Thread(selectorTask);
        t.setDaemon(true);
        t.start();
        client.setSourceProfile(cbSourceProfile.getValue());
    }

    @FXML
    public void onDestProfileChange() {
        client.setDestProfile(cbDestProfile.getValue());
    }

    private void getFileItems(String fileName) {
        ArrayList<String> list = new ArrayList<>();
        SelectorTask selectorTask = new SelectorTask(client, "SELECT " + fileName);
        selectorTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                ArrayList<String> list = selectorTask.getValue();
                fileItems.clear();
                fileItems.setAll(list);
                lvSourceItems.setItems(fileItems.sorted());
                stage.getScene().setCursor(Cursor.DEFAULT);
            }
        });
        stage.getScene().setCursor(Cursor.WAIT);
        Thread t = new Thread(selectorTask);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void moveOneRight() {
        String item = lvSourceItems.getSelectionModel().getSelectedItem();
        if (!lvDestItems.getItems().contains(item)) {
            lvDestItems.getItems().add(item);
        }
    }

    @FXML
    public void moveAllRight() {
        lvDestItems.getItems().setAll(lvSourceItems.getItems());
    }

    @FXML
    public void moveOneLeft() {
        String item = lvDestItems.getSelectionModel().getSelectedItem();
        lvDestItems.getItems().remove(item);
    }

    @FXML
    public void moveAllLeft() {
        lvDestItems.getItems().clear();
    }

}
