/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import asjava.uniclientlibs.UniString;
import asjava.uniobjects.UniFile;
import asjava.uniobjects.UniFileException;
import asjava.uniobjects.UniSessionException;
import com.webfront.u2.client.UvClient;
import com.webfront.u2.model.Profile;
import com.webfront.u2.util.Progress;
import com.webfront.u2.util.Config;
import com.webfront.util.SysUtils;
import com.webfront.uvtool.util.Path;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
    private Label txtMessages;

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
        txtMessages = new Label();
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
        btnCompare.disableProperty().bind(lvItems.getSelectionModel().selectedItemProperty().isNull().or(cbDestProfile.getSelectionModel().selectedItemProperty().isNull()));
        lvItems.disableProperty().bind(cbDestProfile.valueProperty().isNull());
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
        Platform.runLater(() -> txtMessages.setText("Collecting file names"));
        client.setSourceProfile(cbSourceProfile.getValue());
        sourceFileList.clear();
        cbFiles.disableProperty().set(true);

        String path = "/uvcode";
        boolean isLocal = client.getSourceProfile().getServerName().equalsIgnoreCase("mustang");
        if (isLocal) {
            path = "/usr/local/madev";
        }
        
        SelectorTask selectorTask = new SelectorTask(client.getSourceProfile(), path, "");
        selectorTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                ArrayList<String> list = selectorTask.getValue();
                sourceFileList.addAll(list);
                cbFiles.setItems(sourceFileList);
                cbFiles.disableProperty().set(false);
                stage.getScene().setCursor(Cursor.DEFAULT);
                Platform.runLater(() -> txtMessages.setText(""));
            }
        });
        stage.getScene().setCursor(Cursor.WAIT);
        Thread t = new Thread(selectorTask);
        t.setDaemon(true);
        t.start();
        client.setSourceProfile(cbSourceProfile.getValue());
    }

    private void getFileItems(String fileName) {
        Platform.runLater(() -> txtMessages.setText("Reading " + fileName));
        ArrayList<String> list = new ArrayList<>();
        String filter = null;
        String fileType = fileName;
        if (fileName.endsWith("LIB")) {
            fileType = "webde";
        } else if(fileName.matches(".+\\.uv[fistp]")) {
            fileType = "uvcode";
        }
        String path = new Path().getPath(client.getSourceProfile().getServerName(), fileType);
        if (fileType.equals("DM.BP") || fileType.equals("DM.SR") || fileType.equals("BOBL.BP")) {
            filter = "*";
        } else if(fileType.contains("PADS")) {
            filter = "*";
        } else if(fileType.endsWith("LIB")) {
            filter = ".rbm";
        } else {
            filter = "*.uv*";
        }
        
        SelectorTask selectorTask = new SelectorTask(client.getSourceProfile(), path+fileName, filter);
        selectorTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                ArrayList<String> list = selectorTask.getValue();
                fileItems.clear();
                fileItems.setAll(list);
                lvItems.setItems(fileItems.sorted());
                stage.getScene().setCursor(Cursor.DEFAULT);
                Platform.runLater(() -> txtMessages.setText(""));
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
            fileName = fileName.replace('/', ',');
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
            Platform.runLater(() -> txtMessages.setText(ex.getMessage()));
            Logger.getLogger(SourceCompareController.class
                    .getName()).log(Level.SEVERE, null, ex.getMessage());
            client.doDisconnect();
        } catch (UniSessionException ex) {
            Platform.runLater(() -> txtMessages.setText(ex.getMessage()));
            client.doDisconnect();
        } catch (UniFileException ex) {
            Platform.runLater(() -> txtMessages.setText(ex.getMessage()));
        } catch (InterruptedException ex) {
            client.doDisconnect();
        }
    }

    private boolean checkDestFile(String fileName, String itemId) {
        fileName = fileName.replace('/', ',');
        if (cbDestProfile.getValue() == null) {
            return false;
        }
        try {
            client.doSingleConnect("dest");
            UniFile destFile = client.getDestSession().getSession().open(fileName);
            UniString s = destFile.read(itemId);
            client.doSingleDisconnect("dest");
        } catch (UniSessionException ex) {
            try {
                client.doSingleDisconnect("dest");
                return false;
            } catch (UniSessionException ex1) {
                Logger.getLogger(SourceCompareController.class
                        .getName()).log(Level.SEVERE, null, ex1);
                return false;
            }
        } catch (UniFileException ex) {
            try {
                client.doSingleDisconnect("dest");
                return false;
            } catch (UniSessionException ex1) {
                Logger.getLogger(SourceCompareController.class
                        .getName()).log(Level.SEVERE, null, ex1);
                return false;
            }
        }
        return true;
    }

    @FXML
    public void lvItemsOnMouseRelease() {
        txtMessages.setText("");
        String fileName = cbFiles.getValue();
        String itemId = lvItems.getSelectionModel().getSelectedItem();
        if (!checkDestFile(fileName, itemId)) {
            txtMessages.setText("Cannot read " + fileName + " " + itemId + " on " + client.getDestProfile().getServerName());
            lvItems.getSelectionModel().clearSelection();
        } else {
            txtMessages.setText("");
        }
    }

    @FXML
    public void onDestProfileChanged() {
        client.setDestProfile(cbDestProfile.getValue());
    }

    @Override
    public void display(String message) {
        txtMessages.setText(message);
    }

    @Override
    public void setStage(Stage s) {
        this.stage = s;
    }

    @Override
    public Button getCancelButton() {
        return this.btnCancel;
    }

    @Override
    public void launch(String v, String t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateProgressBar(Double p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void state(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
