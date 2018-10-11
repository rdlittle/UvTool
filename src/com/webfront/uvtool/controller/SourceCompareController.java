/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import asjava.uniobjects.UniFile;
import asjava.uniobjects.UniFileException;
import asjava.uniobjects.UniSessionException;
import com.webfront.u2.client.UvClient;
import com.webfront.u2.model.Profile;
import com.webfront.u2.util.Progress;
import com.webfront.u2.util.Config;
import com.webfront.util.SysUtils;
import com.webfront.uvtool.util.SelectorTask;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
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
import javafx.scene.control.TextArea;
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
public class SourceCompareController implements Controller, Initializable, Progress {

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
    private Button btnCompare;

    @FXML
    private Button btnCancel;

    @FXML
    private Circle sourceLed;

    @FXML
    private Circle destLed;

    @FXML
    private TextArea txtMessages;

    @FXML
    private ComboBox<Profile> cbSourceProfile;

    @FXML
    private ComboBox<Profile> cbDestProfile;

    @FXML
    private ComboBox<String> cbFiles;

    @FXML
    private ListView<String> lvItems;

    private final Config config;
    private ResourceBundle res;
    private Stage stage;
    private ObservableList<String> sourceFileList;
    private ObservableList<String> fileItems;
    private UvClient client;

    RadialGradient ledOff;
    RadialGradient ledOn;
    List<Stop> stopsOn;
    List<Stop> stopsOff;

    public SourceCompareController() {
        config = Config.getInstance();
        stopsOn = new ArrayList<>();
        stopsOff = new ArrayList<>();
        btnCompare = new Button();
        btnCancel = new Button();
        txtMessages = new TextArea();
        cbSourceProfile = new ComboBox<>();
        cbDestProfile = new ComboBox<>();
        cbFiles = new ComboBox<>();
        lvItems = new ListView<>();

        stopsOn.add(new Stop(0, Color.web("#26ff6B")));
        stopsOn.add(new Stop(1.0, Color.web("#1e6824")));
        ledOn = new RadialGradient(0, -0.02, 0.51, 0.5, 0.97, true, CycleMethod.NO_CYCLE, stopsOn);

        stopsOff.add(new Stop(0, Color.web("#cccccc")));
        stopsOn.add(new Stop(1.0, Color.web("#1e6824")));
        ledOff = new RadialGradient(0, -0.02, 0.51, 0.5, 0.67, true, CycleMethod.NO_CYCLE, stopsOff);

        sourceLed = new Circle();
        destLed = new Circle();
        sourceFileList = FXCollections.<String>observableArrayList();
        fileItems = FXCollections.<String>observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        res = resources;
        client = new UvClient(this);
        cbSourceProfile.setItems(config.getProfiles());
        cbDestProfile.setItems(config.getProfiles());
        cbFiles.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                fileItems.clear();
                if (newValue != null) {
                    String fileName = newValue.toString();
                    cbFiles.setValue(fileName);
                    getFileItems(fileName);
                }
            }
        });
        SimpleListProperty slp = new SimpleListProperty(fileItems);
        btnCompare.disableProperty().bind(slp.emptyProperty().or(cbDestProfile.getSelectionModel().selectedItemProperty().isNull()));
    }

    @Override
    public void updateLed(String host, boolean onOff) {
        if (host.equalsIgnoreCase("source")) {
            Platform.runLater(() -> sourceLed.setFill(onOff ? ledOn : ledOff));
        } else {
            Platform.runLater(() -> destLed.setFill(onOff ? ledOn : ledOff));
        }
    }

    @FXML
    public void onSourceProfileChange() {
        client.setSourceProfile(cbSourceProfile.getValue());
        sourceFileList.clear();
        cbFiles.disableProperty().set(true);
        SelectorTask selectorTask = new SelectorTask(client, stmt);
        selectorTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                ArrayList<String> list = selectorTask.getValue();
                sourceFileList.addAll(list);
                cbFiles.setItems(sourceFileList);
                cbFiles.disableProperty().set(false);
                stage.getScene().setCursor(Cursor.DEFAULT);
            }
        });
        stage.getScene().setCursor(Cursor.WAIT);
        Thread t = new Thread(selectorTask);
        t.setDaemon(true);
        t.start();
        client.setSourceProfile(cbSourceProfile.getValue());
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
                lvItems.setItems(fileItems.sorted());
                stage.getScene().setCursor(Cursor.DEFAULT);
            }
        });
        stage.getScene().setCursor(Cursor.WAIT);
        Thread t = new Thread(selectorTask);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void btnCompareOnClick() {
        try {
            String sourceHost = client.getSourceProfile().getServerName();
            String destHost = client.getDestProfile().getServerName();
            String diffProgram = config.getPreferences().get("diffProgram");
            String tempDir = config.getPreferences().get("tempDir");
            String fileName = cbFiles.getValue();
            String itemName = lvItems.getSelectionModel().getSelectedItem();
            String sourceFileName = tempDir + "/" + sourceHost + "." + itemName;
            String destFileName = tempDir + "/" + destHost + "." + itemName;
            FileWriter sourceOut = new FileWriter(new File(sourceFileName));
            FileWriter destOut = new FileWriter(new File(destFileName));
            if (client.doConnect()) {
                UniFile sourceFile = client.getSourceSession().getSession().open(fileName);
                UniFile destFile = client.getDestSession().getSession().open(fileName);
                String sourceItem = sourceFile.read(itemName).toString();
                String destItem = destFile.read(itemName).toString();

                sourceItem = sourceItem.replaceAll(SysUtils.asString(254), "\n");
                destItem = destItem.replaceAll(SysUtils.asString(254), "\n");

                sourceOut.write(sourceItem);
                destOut.write(destItem);

                sourceOut.flush();
                sourceOut.close();
                destOut.flush();
                destOut.close();
                sourceFile.close();
                destFile.close();
                client.doDisconnect();
                String[] cmd = new String[3];
                cmd[0] = diffProgram;
                cmd[1] = sourceFileName;
                cmd[2] = destFileName;
                Runtime.getRuntime().exec(cmd).waitFor();
            }
        } catch (IOException ex) {
            txtMessages.setText(ex.getMessage());
            Logger.getLogger(SourceCompareController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UniSessionException ex) {
            txtMessages.setText(ex.getMessage());
            Logger.getLogger(SourceCompareController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UniFileException ex) {
            txtMessages.setText(ex.getMessage());
            Logger.getLogger(SourceCompareController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
        }
    }

    @FXML
    public void onDestProfileChanged() {
        client.setDestProfile(cbDestProfile.getValue());
    }

    @Override
    public void updateProgressBar(Double p
    ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void state(String message
    ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void display(String message
    ) {
        txtMessages.setText(message);
    }

    @Override
    public void setStage(Stage s
    ) {
        this.stage = s;
    }

    @Override
    public void launch(String v, String t
    ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Button getCancelButton() {
        return this.btnCancel;
    }

}
