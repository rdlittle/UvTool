/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.github.cliftonlabs.json_simple.Jsoner;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.webfront.u2.util.Config;
import com.webfront.u2.util.Progress;
import com.webfront.uvtool.model.PeerReviewModel;
import com.webfront.uvtool.model.Server;
import com.webfront.uvtool.util.ConfigProperties;
import com.webfront.uvtool.util.Network;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.w3c.dom.events.EventException;

/**
 *
 * @author rlittle
 */
public class PeerReviewController implements Controller, Initializable, Progress {

    private Stage stage;

    @FXML
    Button btnLoad;
    @FXML
    Button btnPassItem;
    @FXML
    Button btnFailItem;
    @FXML
    Button btnPassReview;
    @FXML
    Button btnFailReview;
    @FXML
    CheckBox chkLoadData;
    @FXML
    Label lblPendingCount;
    @FXML
    Label lblDictDataCount;
    @FXML
    Label lblPassedCount;
    @FXML
    Label lblFailedCount;
    @FXML
    Label lblTotalCount;
    @FXML
    Label lblMessage;
    @FXML
    ListView listProjects;
    @FXML
    ListView listPending;
    @FXML
    ListView listFailed;
    @FXML
    ListView<String> listPassed;
    @FXML
    TextField txtReviewId;
    @FXML
    TableColumn itemColumn;
    @FXML
    TableColumn statusColumn;
    @FXML
    TableView<DictDataItem> tblDictData;
    @FXML
    ProgressBar progressBar;

    ResourceBundle res;
    private final Network net = new Network();
    private final ConfigProperties platforms = ConfigProperties.getInstance();
    private final String remotePath = "/uvfs/ma.accounts/deploy/DM.PEER";
    private final String localPath = "/home/rlittle/sob/projects/";
    private final Config systemConfig = Config.getInstance();
    private final String fileSep = System.getProperty("file.separator");
    private final String lineSep = System.getProperty("line.separator");
    private final String pathSep = System.getProperty("path.separator");
    private final String VM = new Character((char) 253).toString();

    private final PeerReviewModel model;
    private boolean loadDictData;

    private final ObservableList<DictDataItem> dictDataItemList;
    private final ObservableList<String> projectList;
    private final SimpleBooleanProperty hasFailed = new SimpleBooleanProperty();
    private final SimpleBooleanProperty hasPending = new SimpleBooleanProperty();
    private final SimpleBooleanProperty hasProject = new SimpleBooleanProperty();

    public PeerReviewController() {
        txtReviewId = new TextField();
        this.model = PeerReviewModel.getInstance();
        loadDictData = com.webfront.u2.util.Config.getInstance().
                getPreferences().get("loadData").equals("1");
        dictDataItemList = FXCollections.observableArrayList();
        projectList = FXCollections.observableArrayList();
        String home = systemConfig.getPreferences().get("projectHome");
        File f = new File(home);
        String[] dir = f.list();
        for (String fileName : dir) {
            if (fileName.endsWith(".json")) {
                projectList.add(fileName);
            }
        }
        hasProject.set(false);
    }

    private boolean deleteLocalFile(String fullPath) {
        File f = new File(fullPath);
        return f.delete();
    }

    private boolean doCompare(String oldItem, String newItem) {
        StringBuilder newContent = new StringBuilder();
        StringBuilder oldContent = new StringBuilder();
        try {
            BufferedReader newFile = new BufferedReader(new FileReader(newItem));
            BufferedReader oldFile = new BufferedReader(new FileReader(oldItem));

            for (;;) {
                String line = newFile.readLine();
                if (line == null) {
                    newFile.close();
                    break;
                }
                newContent.append(line.replaceAll(" ", ""));
            }
            for (;;) {
                String line = oldFile.readLine();
                if (line == null) {
                    oldFile.close();
                    break;
                }
                oldContent.append(line.replaceAll(" ", ""));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return newContent.toString().equals(oldContent.toString());
    }

    @Override
    public Button getCancelButton() {
        return null;
    }

    private int getArtifact(String item) {
        int mtime = 0;
        try {
            String[] segs = item.split("~");
            String platform = segs[0];
            String library = segs[1];
            String program = segs[2];
            if (platform.equals("DMCRBO")) {
                platform = "DMC";
                program = segs[3];
            }
            Server s = new Server(platforms.getPlatforms(), platform.toLowerCase());
            String codebase = s.getCodeBase();
            String pathType = net.getPathType(library);
            String remotePath = s.getPath(pathType);
            String localPath = systemConfig.getPreferences().get("codeHome");
            if (pathType.equals("pads_hook_segment")) {
                library = "";
            }
            if (!remotePath.endsWith("/")) {
                remotePath = remotePath + "/";
            }
            if (!localPath.endsWith(fileSep)) {
                localPath = localPath + fileSep;
            }
            String host = s.getHost("dev");
            mtime = net.doSftpGet(host, remotePath + library, program, localPath, program);
        } catch (JSchException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SftpException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mtime;
    }

    private void getProjectArtifacts() throws IOException {
        int mtime = 0;
        Double recordsDone = 0D;
        updateProgressBar(recordsDone);
        Double totalRecords = Double.valueOf(this.model.getTotalItems().get());
        stage.getScene().setCursor(Cursor.WAIT);
        String path = "";
        String itemToDelete = "";

        for (ArrayList<String> list : this.model.getAllPrograms().values()) {
            for (String item : list) {
                if (item.isEmpty()) {
                    continue;
                }
                if (item.contains("SEGMENT")) {
                    item = item.replaceAll("\\\\", "\\.");
                }
                mtime = getArtifact(item);
                this.model.getTimeStamps().put(item, mtime);
                try {
                    String[] segs = item.split("~");
                    String platform = segs[0];
                    String library = segs[1];
                    String program = segs[2];
                    String codebase = null;
                    if (platform.equals("DMCRBO")) {
                        platform = "DMC";
                        program = segs[3];
                        codebase = "RBO";
                    }
                    Server s = new Server(platforms.getPlatforms(), platform.toLowerCase());
                    if (codebase == null) {
                        codebase = s.getCodeBase();
                    }

                    if (!platform.equalsIgnoreCase(codebase)) {
                        item = String.format("%s~%s~%s", codebase, library, program);
                    }

                    path = systemConfig.getPreferences().get("codeHome");
                    if (!path.endsWith(fileSep)) {
                        path = path + fileSep;
                    }
                    String[] specs = item.split("~");
                    String newItem = path + specs[2];
                    String oldItem = path + specs[2] + ".approved";
                    itemToDelete = oldItem;
                    net.getApproved("CODE", item);

                    boolean isMatch = doCompare(oldItem, newItem);
                    if (!isMatch) {
                        this.model.getPendingList().add(item);
                        deleteLocalFile(oldItem);
                        incrementCounter("pending");
                    } else {
                        deleteLocalFile(oldItem);
                        deleteLocalFile(newItem);
                        this.model.getPassedList().add(item);
                        incrementCounter("passed");
                    }
                    File f = new File(oldItem);
                    f.delete();
                } catch (EventException | JSchException ex) {
                    deleteLocalFile(itemToDelete);
                    Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
                    stage.getScene().setCursor(Cursor.DEFAULT);
                } catch (SftpException ex) {
                    Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
                    display(item + ": " + ex.getMessage());
                    deleteLocalFile(itemToDelete);
                    if (ex.getMessage().contains("No such file")) {
                        this.model.getPendingList().add(item);
                        incrementCounter("pending");
                        continue;
                    }
                    stage.getScene().setCursor(Cursor.DEFAULT);
                } catch (IOException ex) {
                    deleteLocalFile(itemToDelete);
                    if ("No approved version found".equals(ex.getMessage())) {
                        this.model.getPendingList().add(item);
                        incrementCounter("pending");
                        continue;
                    }
                    Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
                    stage.getScene().setCursor(Cursor.DEFAULT);
                }
                recordsDone += 1;
                Double pct = recordsDone / totalRecords;
                updateProgressBar(pct);
            }
        }
        getDictData(recordsDone, totalRecords);
        updateProgressBar(0D);
        stage.getScene().setCursor(Cursor.DEFAULT);
        File f = new File(localPath + txtReviewId.textProperty().getValue() + ".json");
        try (FileWriter out = new FileWriter(f)) {
            out.write(Jsoner.prettyPrint(Jsoner.serialize(this.model.toJson())));
            out.close();
        }
    }

    private void getDictData(Double recordsDone, Double totalRecords) {
        for (ArrayList<String> list : this.model.getAllData().values()) {
            for (String item : list) {
                String itemStatus = "Unchecked";
                if (loadDictData) {
                    boolean isDataOk = net.checkDictData(item);
                    itemStatus = isDataOk ? "Yes" : "No";
                }
                recordsDone += 1;
                Double pct = recordsDone / totalRecords;
                updateProgressBar(pct);
                DictDataItem ddItem = new DictDataItem(item, itemStatus);
                dictDataItemList.add(ddItem);
            }
        }
    }

    private void incrementCounter(String counter) {
        switch (counter) {
            case "pending": {
                Integer i = Integer.parseInt(this.model.getTotalPending().get()) + 1;
                Platform.runLater(() -> this.model.getTotalPending().set(i.toString()));
                break;
            }
            case "passed": {
                Integer i = Integer.parseInt(this.model.getTotalPassed().get()) + 1;
                Platform.runLater(() -> this.model.getTotalPassed().set(i.toString()));
                break;
            }
            case "failed": {
                Integer i = Integer.parseInt(this.model.getTotalFailed().get()) + 1;
                Platform.runLater(() -> this.model.getTotalFailed().set(i.toString()));
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.res = resources;
        listPassed.setItems(this.model.getPassedList());
        listPending.setItems(this.model.getPendingList());
        listFailed.setItems(this.model.getFailedList());
        lblPassedCount.textProperty().bind(this.model.getTotalPassed());
        lblPendingCount.textProperty().bind(this.model.getTotalPending());
        lblFailedCount.textProperty().bind(this.model.getTotalFailed());
        lblMessage.setText("");
        lblDictDataCount.textProperty().bind(this.model.getTotalDictData());
        lblTotalCount.textProperty().bind(this.model.getTotalItems());
        chkLoadData.selectedProperty().set(loadDictData);
        chkLoadData.selectedProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                loadDictData = (boolean) newValue;
            }
        });
        listProjects.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                try {
                    if (newValue != null) {
                        recallProject(newValue.toString());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        txtReviewId.textProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                listProjects.getSelectionModel().clearSelection();
            }
        });
        this.model.getFailedList().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change c) {
                hasFailed.setValue(!c.getList().isEmpty());
            }
        });
        this.model.getPendingList().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change c) {
                hasPending.setValue(!c.getList().isEmpty());
            }
        });
        listPending.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int clicks = event.getClickCount();
                if (clicks == 2) {
                    String item = listPending.getSelectionModel().
                            getSelectedItem().toString();
                    reviewItem(item);
                }
            }
        });

        btnLoad.disableProperty().bind(listProjects.getSelectionModel().selectedItemProperty().isNotNull());
        btnPassReview.disableProperty().bind(hasFailed.or(hasPending).or(hasProject.not()));
        btnFailReview.disableProperty().bind(btnPassReview.disabledProperty().not().or(hasProject.not()));
        btnPassItem.disableProperty().bind(listPending.getSelectionModel().selectedItemProperty().isNull());
        btnFailItem.disableProperty().bind(btnPassItem.disabledProperty());

        itemColumn.setCellValueFactory(new PropertyValueFactory<>("artifactName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("artifactStatus"));
        tblDictData.setItems(dictDataItemList);
        listProjects.setItems(projectList);
    }

    @Override
    public void launch(String v, String t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void recallProject(String project) throws FileNotFoundException, IOException {
        String home = systemConfig.getPreferences().get("projectHome");
        if (!home.endsWith(fileSep)) {
            home = home + fileSep;
        }
        String path = home + project;
        BufferedReader reader = new BufferedReader(new FileReader(path));
        StringBuilder buffer = new StringBuilder();
        for (;;) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            buffer.append(line);
        }
        reader.close();
        dictDataItemList.clear();
        this.model.fromJson(buffer.toString());
        for (ArrayList<String> list : this.model.getAllData().values()) {
            for (String item : list) {
                String itemStatus = "Unchecked";
                if (loadDictData) {
                    boolean isDataOk = net.checkDictData(item);
                    itemStatus = isDataOk ? "Yes" : "No";
                }
                DictDataItem ddItem = new DictDataItem(item, itemStatus);
                dictDataItemList.add(ddItem);
            }
        }
        hasProject.set(true);
    }

    @FXML
    public void onLoadReview() {
        this.model.clear();
        dictDataItemList.clear();
        String item = txtReviewId.getText();
        StringBuilder sb = new StringBuilder();

        try {
            int mtime = net.doSftpGet("nlstest", remotePath, item, localPath, item);
            try (BufferedReader f = new BufferedReader(new FileReader(localPath + item))) {
                while (true) {
                    String line = f.readLine();
                    if (line == null) {
                        break;
                    }
                    sb = sb.append(line).append("\n");
                }
            }
            this.model.init(sb.toString());
            try (FileWriter out = new FileWriter(new File(localPath + item + ".json"))) {
                out.write(Jsoner.prettyPrint(Jsoner.serialize(this.model.toJson())));
            }
            File f2 = new File(localPath + item);
            f2.delete();
            Runnable runner = () -> {
                try {
                    getProjectArtifacts();
                } catch (IOException ex) {
                    Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            };
            Thread backgroundThread = new Thread(runner);
            backgroundThread.setDaemon(true);
            backgroundThread.start();
        } catch (JSchException | SftpException | IOException ex) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.contentTextProperty().set(ex.getMessage());
                alert.showAndWait();
            });
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        }
        projectList.add(txtReviewId.getText() + ".json");
        ConcurrentSkipListSet<String> tempList = new ConcurrentSkipListSet<>(projectList.sorted());
        projectList.clear();
        projectList.addAll(tempList);
        hasProject.set(true);
    }

    private int putArtifact(String item) throws JSchException, SftpException, IOException {
        int mtime = 0;
        String[] segs = item.split("~");
        String platform = segs[0];
        String library = segs[1];
        String program = segs[2];
        if (platform.equals("DMCRBO")) {
            platform = "DMC";
            program = segs[3];
        }
        Server s = new Server(platforms.getPlatforms(), platform.toLowerCase());
        String codebase = s.getCodeBase();
        String pathType = Network.getPathType(library);
        String rPath = s.getPath(pathType);
        String lPath = systemConfig.getPreferences().get("codeHome");
        if (pathType.equals("pads_hook_segment")) {
            library = "";
        }
        if (!rPath.endsWith("/")) {
            rPath = rPath + "/";
        }
        if (!lPath.endsWith(fileSep)) {
            lPath = lPath + fileSep;
        }
        String host = s.getHost("dev");
        mtime = net.doSftpPut(host, rPath + library, program, lPath, program);

        return mtime;
    }

    public void reviewItem(String item) {
        String diff = systemConfig.getPreferences().get("diffProgram");
        String editor = systemConfig.getPreferences().get("editor");
        String path = systemConfig.getPreferences().get("codeHome");
        if (!path.endsWith(fileSep)) {
            path = path + fileSep;
        }
        String[] specs = item.split("~");
        String progName = specs[2];
        String left = path + progName;
        String right = path + progName + ".approved";
        String executable = diff;
        String args = left + " " + right;
        boolean isTwoPart = true;
        File f = new File(right);
        if (!f.exists()) {
            right = path + progName + ".live";
            f = new File(right);
            if (!f.exists()) {
                isTwoPart = false;
                executable = editor;
                args = left;
            }
        }
        f = new File(left);
        try {
            try (LineNumberReader reader = new LineNumberReader(new FileReader(f))) {
                StringBuilder sb = new StringBuilder();
                reader.setLineNumber(0);
                String lineOne = reader.readLine();
                if (!lineOne.contains("!!! Pending")) {
                    sb.append("!!! Pending" + lineSep);
                    for (;;) {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        sb = sb.append(line + lineSep);
                    }
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
                        writer.write(sb.toString());
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String cmd = executable + " " + args;
        Runnable task = () -> {
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (IOException ex) {
                Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    @FXML
    public void onPassItem() {
        String item = listPending.getSelectionModel().getSelectedItem().toString();
        String path = systemConfig.getPreferences().get("codeHome");
        if (!path.endsWith(fileSep)) {
            path = path + fileSep;
        }
        String[] specs = item.split("~");
        String platform = specs[0];
        String library = specs[1];
        String progName = specs[2];
        String pathType = Network.getPathType(library);
        if (platform.equals("DMCRBO")) {
            platform = "DMC";
            progName = specs[3];
        }
        Server s = new Server(platforms.getPlatforms(), platform.toLowerCase());
        StringBuffer sb = new StringBuffer();
        File f = new File(path + progName);

        try (LineNumberReader reader = new LineNumberReader(new FileReader(f))) {
            reader.setLineNumber(0);
            String line = reader.readLine();
            if (line.contains("!!! Pending")) {
                reader.close();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.contentTextProperty().set("Please remove \"!!! Pending\" from the source code");
                alert.showAndWait();
                return;
            } else {
                for (;;) {
                    line = reader.readLine();
                    if (line == null) {
                        reader.close();
                        break;
                    }
                    if (line.contains("!!!")) {
                        reader.close();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.contentTextProperty().set("You have \"!!!\" comments in the source code");
                        alert.showAndWait();
                        return;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            int mtime = putArtifact(item);
            this.model.getPendingList().remove(item);
            this.model.getPassedList().add(item);
            this.model.getTimeStamps().put(item, mtime);
//            net.setApproved("CODE", item);
            path = systemConfig.getPreferences().get("projectHome");
            if (!path.endsWith(fileSep)) {
                path = path + fileSep;
            }
            String id = txtReviewId.getText();
            if(id.isEmpty()) {
                id = listProjects.getSelectionModel().getSelectedItem().toString();
            }
            if (!id.endsWith(".json")) {
                id = id+".json";
            }
            f = new File(path+id);
            try (FileWriter out = new FileWriter(f)) {
                out.write(Jsoner.prettyPrint(Jsoner.serialize(this.model.toJson())));
                out.close();
            }
        } catch (JSchException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SftpException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    public void onFailItem() {
        display("Item failed");
    }

    @FXML
    public void onPassReview() {

    }

    @FXML
    public void onFailReview() {

    }

    private void resetForm() {
        dictDataItemList.clear();
        this.model.clear();
    }

    @Override
    public void setStage(Stage s) {
        this.stage = s;
    }

    @Override
    public void display(String message) {
        Platform.runLater(() -> lblMessage.textProperty().set(message));
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                lblMessage.textProperty().set("");
            }
        }));
        timeline.setCycleCount(3);
        timeline.play();
    }

    @Override
    public void state(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateProgressBar(Double p) {
        Platform.runLater(() -> progressBar.progressProperty().setValue(p));
    }

    @Override
    public void updateLed(String host, boolean onOff) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static class DictDataItem {

        private final SimpleStringProperty artifactName;
        private final SimpleStringProperty artifactStatus;

        private DictDataItem(String itm, String st) {
            this.artifactName = new SimpleStringProperty(itm);
            this.artifactStatus = new SimpleStringProperty(st);
        }

        /**
         * @return the item
         */
        public String getArtifactName() {
            return artifactName.get();
        }

        /**
         * @return the status
         */
        public String getArtifactStatus() {
            return artifactStatus.get();
        }

    }

}
