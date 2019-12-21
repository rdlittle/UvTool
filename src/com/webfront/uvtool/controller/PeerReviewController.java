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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
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
    ListView listItems;
    @FXML
    ListView listPending;
    @FXML
    ListView listFailed;
    @FXML
    ListView listDictData;
    @FXML
    ListView<String> listPassed;
    @FXML
    TextField txtReviewId;
    @FXML
    ProgressBar progressBar;

    ResourceBundle res;
    private final Network net = new Network();
    private final ConfigProperties platforms = ConfigProperties.getInstance();
    private final String remotePath = "/uvfs/ma.accounts/deploy/DM.PEER";
    private final String localPath = "/home/rlittle/sob/projects/";
    private final Config systemConfig = Config.getInstance();
    private final String pathSep = System.getProperty("file.separator");
    private final String VM = new Character((char) 253).toString();

    private PeerReviewModel model;

    public PeerReviewController() {
        txtReviewId = new TextField();
        this.model = PeerReviewModel.getInstance();
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
            if(platform.equals("DMCRBO")) {
                platform = "DMC";
                program = segs[3];
            }
            Server s = new Server(platforms.getPlatforms(), platform.toLowerCase());
            String codebase = s.getCodeBase();
            String pathType = net.getPathType(library);
            String remotePath = s.getPath(pathType);
            String localPath = systemConfig.getPreferences().get("codeHome");
            if(pathType.equals("pads_hook_segment")) {
                library = "";
            }
            if (!remotePath.endsWith("/")) {
                remotePath = remotePath + "/";
            }
            if (!localPath.endsWith(pathSep)) {
                localPath = localPath + pathSep;
            }
            String host = s.getHost("dev");
            mtime = net.doSftp(host, remotePath + library, program, localPath, program);
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

        for (ArrayList<String> list : this.model.getAllPrograms().values()) {
            for (String item : list) {
                if (item.isEmpty()) {
                    continue;
                }
                if(item.contains("SEGMENT")) {
                    item = item.replaceAll("\\\\", "\\.");
                }
                mtime = getArtifact(item);
                this.model.getTimeStamps().put(item, mtime);
                try {
                    String[] segs = item.split("~");
                    String platform = segs[0];
                    String library = segs[1];
                    String program = segs[2];
                    String codebase=null;
                    if(platform.equals("DMCRBO")) {
                        platform = "DMC";
                        program = segs[3];
                        codebase = "RBO";
                    }
                    Server s = new Server(platforms.getPlatforms(), platform.toLowerCase());
                    if(codebase == null) {
                        codebase = s.getCodeBase();
                    }
                    
                    if (!platform.equalsIgnoreCase(codebase)) {
                        item = String.format("%s~%s~%s", codebase, library, program);
                    }
                    if(item.contains("SEGMENT")) {
                        System.out.println("Ready");
                    }
                    net.getApproved("CODE", item);
                    String path = systemConfig.getPreferences().get("codeHome");

                    if (!path.endsWith(pathSep)) {
                        path = path + pathSep;
                    }
                    String[] specs = item.split("~");
                    String newItem = path + specs[2];
                    String oldItem = path + specs[2] + ".approved";
                    boolean isMatch = doCompare(oldItem, newItem);
                    if (!isMatch) {
                        this.model.getPendingList().add(item);
                        incrementCounter("pending");
                    } else {
                        this.model.getPassedList().add(item);
                        incrementCounter("passed");
                    }
                    File f = new File(oldItem);
                    f.delete();
                } catch (EventException | JSchException ex) {
                    Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
                    stage.getScene().setCursor(Cursor.DEFAULT);
                } catch (SftpException ex) {
                    Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
                    display(item + ": " + ex.getMessage());
                    if(ex.getMessage().contains("No such file")) {
                        this.model.getPendingList().add(item);
                        incrementCounter("pending");
                        continue;
                    }
                    stage.getScene().setCursor(Cursor.DEFAULT);
                } catch (IOException ex) {
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
        updateProgressBar(0D);
        stage.getScene().setCursor(Cursor.DEFAULT);
        File f = new File(localPath + txtReviewId.textProperty().getValue() + ".json");
        try (FileWriter out = new FileWriter(f)) {
            out.write(Jsoner.prettyPrint(Jsoner.serialize(this.model.toJson())));
            out.close();
        }
    }

    private void incrementCounter(String counter) {
        if (counter.equals("pending")) {
            Integer i = Integer.parseInt(this.model.getTotalPending().get()) + 1;
            Platform.runLater(() -> this.model.getTotalPending().set(i.toString()));
        } else if (counter.equals("passed")) {
            Integer i = Integer.parseInt(this.model.getTotalPassed().get()) + 1;
            Platform.runLater(() -> this.model.getTotalPassed().set(i.toString()));
        } else if (counter.equals("failed")) {
            Integer i = Integer.parseInt(this.model.getTotalFailed().get()) + 1;
            Platform.runLater(() -> this.model.getTotalFailed().set(i.toString()));
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.res = resources;
        listPassed.setItems(this.model.getPassedList());
        listPending.setItems(this.model.getPendingList());
        listFailed.setItems(this.model.getFailedList());
        listDictData.setItems(this.model.getDictDataList());
        lblPassedCount.textProperty().bind(this.model.getTotalPassed());
        lblPendingCount.textProperty().bind(this.model.getTotalPending());
        lblFailedCount.textProperty().bind(this.model.getTotalFailed());
        lblMessage.setText("");
        lblDictDataCount.textProperty().bind(this.model.getTotalDictData());
        lblTotalCount.textProperty().bind(this.model.getTotalItems());
    }

    @Override
    public void launch(String v, String t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @FXML
    public void onLoadReview() {
        String item = txtReviewId.getText();
        StringBuilder sb = new StringBuilder();

        try {
            int mtime = net.doSftp("nlstest", remotePath, item, localPath, item);
            try (BufferedReader f = new BufferedReader(new FileReader(localPath + item))) {
                while (true) {
                    String line = f.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line + "\n");
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

        } catch (JSchException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SftpException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PeerReviewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void onPassItem() {
        display("Item passed");
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

}
