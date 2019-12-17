/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.github.cliftonlabs.json_simple.Jsoner;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.webfront.uvtool.model.PeerReviewModel;
import com.webfront.uvtool.model.Server;
import com.webfront.uvtool.util.ConfigProperties;
import com.webfront.uvtool.util.Network;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.w3c.dom.events.EventException;

/**
 *
 * @author rlittle
 */
public class PeerReviewController implements Controller, Initializable {

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
    ListView listItems;
    @FXML
    ListView listPending;
    @FXML
    ListView listFailed;
    @FXML
    ListView listDictData;
    @FXML
    ListView listPassed;
    @FXML
    TextField txtReviewId;

    private final SimpleStringProperty reviewId;
    private final SimpleStringProperty totalItems;
    private final SimpleStringProperty totalPending;
    private final SimpleStringProperty totalPassed;
    private final SimpleStringProperty totalFailed;
    private final SimpleStringProperty totalDictData;

    private final ObservableList<String> itemList;
    private final ObservableList<String> passedList;
    private final ObservableList<String> failedList;
    private final ObservableList<String> pendingList;
    private final ObservableList<String> dictDataList;

    ResourceBundle res;
    private final Network net = new Network();
    private final ConfigProperties platforms = ConfigProperties.getInstance();
    private final String remotePath = "/uvfs/ma.accounts/deploy/DM.PEER";
    private final String localPath = "/home/rlittle/sob/projects/";

    private PeerReviewModel model;

    public PeerReviewController() {

        reviewId = new SimpleStringProperty();
        totalItems = new SimpleStringProperty();
        totalPending = new SimpleStringProperty();
        totalPassed = new SimpleStringProperty();
        totalFailed = new SimpleStringProperty();
        totalDictData = new SimpleStringProperty();

        itemList = FXCollections.observableArrayList();
        passedList = FXCollections.observableArrayList();
        failedList = FXCollections.observableArrayList();
        pendingList = FXCollections.observableArrayList();
        dictDataList = FXCollections.observableArrayList();

        txtReviewId = new TextField();
        this.model = null;
    }

    @Override
    public Button getCancelButton() {
        return null;
    }

    private void getArtifact(String item) {
        String[] segs = item.split("`");
        String platform = segs[0];
        String library = segs[1];
        String program = segs[2];
        Server s = new Server(platforms.getPlatforms(), platform);
        String pathType = net.getPathType(library);
        String remotePath = s.getPath(pathType);
    }



    private void getProjectArtifacts() {
        for (String item : this.model.getItemList()) {

        }
    }

    @Override
    public void launch(String v, String t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setStage(Stage s) {
        this.stage = s;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.res = resources;
    }

    @FXML
    public void onLoadReview() {
        String item = txtReviewId.getText();
        StringBuilder sb = new StringBuilder();
        String vm = new Character((char) 253).toString();
        try {
            int mtime = net.doSftp("nlstest", remotePath, item, localPath, item);
            try (BufferedReader f = new BufferedReader(new FileReader(localPath + item))) {
                while (true) {
                    String line = f.readLine();
                    if (line == null) {
                        break;
                    }
                    if (line.isEmpty()) {
                        sb.append("\n");
                    } else {
                        sb.append(line);
                    }
                }
            }
            this.model = new PeerReviewModel(sb.toString());
            try (FileWriter out = new FileWriter(new File(localPath + item + ".json"))) {
                out.write(Jsoner.prettyPrint(Jsoner.serialize(this.model.toJson())));
            }
            File f2 = new File(localPath + item);
            f2.delete();
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

    }

    @FXML
    public void onFailItem() {

    }

    @FXML
    public void onPassReview() {

    }

    @FXML
    public void onFailReview() {

    }
}
