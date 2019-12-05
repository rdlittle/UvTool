/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.webfront.uvtool.model.Server;
import com.webfront.uvtool.app.UvTool;
import com.webfront.uvtool.util.CBClient;
import com.webfront.uvtool.util.Network;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author rlittle
 */
public class DeployBackupController implements Controller, Initializable {

    ResourceBundle res;

    private static enum ItemType {
        CODE, DICT, DATA;
    }

    @FXML
    Button btnGetBackups;

    @FXML
    Button btnNewSearch;

    @FXML
    Button btnPrevious;

    @FXML
    Button btnNext;

    @FXML
    Button btnCompareDev;

    @FXML
    Button btnCompareStaging;

    @FXML
    Button btnCompareLive;

    @FXML
    Button btnCompareApproved;

    @FXML
    ListView<String> listItems;

    @FXML
    RadioButton rbItemTypeCode;

    @FXML
    RadioButton rbItemTypeDict;

    @FXML
    RadioButton rbItemTypeData;

    @FXML
    ToggleGroup tgItemType;

    @FXML
    TextArea txtPreview;

    @FXML
    TextField txtItemName;

    @FXML
    TextField txtFind;

    @FXML
    ScrollPane scroller;

    private ObservableList<String> itemList;
    private SimpleStringProperty findTarget;
    private SimpleStringProperty itemName;
    private ItemType itemType;
    private HashMap<String, String> resultsMap;
    private String selectedItem;

    public DeployBackupController() {

        itemList = FXCollections.<String>observableArrayList();
        resultsMap = new HashMap<>();

        btnGetBackups = new Button();
        btnNewSearch = new Button();
        btnPrevious = new Button();
        btnNext = new Button();
        btnCompareDev = new Button();
        btnCompareStaging = new Button();
        btnCompareLive = new Button();
        btnCompareApproved = new Button();

        listItems = new ListView<>(itemList);

        rbItemTypeCode = new RadioButton();
        rbItemTypeDict = new RadioButton();
        rbItemTypeData = new RadioButton();

        tgItemType = new ToggleGroup();

        txtPreview = new TextArea();

        txtItemName = new TextField();
        txtFind = new TextField();

        itemName = new SimpleStringProperty();
        itemType = ItemType.CODE;
        findTarget = new SimpleStringProperty();
        scroller = new ScrollPane(txtPreview);

        selectedItem = new String();

    }

    @Override
    public Button getCancelButton() {
        return new Button();
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

    @Override
    public void setStage(Stage s) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        res = resources;
        txtPreview.setPrefColumnCount(80);
        tgItemType.selectedToggleProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (((RadioButton) newValue).getId().equals("rbItemTypeDict")) {
                    itemType = ItemType.DICT;
                } else if (((RadioButton) newValue).getId().equals("rbItemTypeData")) {
                    itemType = ItemType.DATA;
                } else {
                    itemType = ItemType.CODE;
                }
            }
        });

        listItems.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                selectedItem = newValue;
                setPreview(newValue);
            }
        });

        txtFind.textProperty().bind(findTarget);
        txtItemName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                getBackups();
            }
        });
    }

    @FXML
    public void compareDev() {
        String downloadPath = "/home/rlittle/sob/download/";
        String backupFile = txtPreview.getText();
        String progName = txtItemName.getText();
        
        try {
            FileWriter file = new FileWriter(downloadPath + selectedItem);
            file.write(backupFile);
            file.close();
        } catch (IOException ex) {
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
        }

        Network net = new Network();
        Server s = new Server(net.getPlatforms(), "dmc");
        String path = s.getPath("main");
        String host = s.getHost("dev");
        ByteArrayOutputStream output = net.sshExec(host, path, 
                "getDir "+ progName);
        String[] result = (output.toString()).split("\n");
        
        String libName = result[result.length - 1];
        String remotePath = s.getPath(getPathType(libName)) + "/" + libName;

        net.doSftp("dmcdev", remotePath, progName, downloadPath, 
                progName + "."+ host);
        // TODO: make system call to diff program
        String leftFile = downloadPath + selectedItem;
        String rightFile = downloadPath + progName + "."+ host;
    }

    @FXML
    public void compareStaging() {

    }

    @FXML
    public void compareLive() {

    }

    @FXML
    public void compareApproved() {

    }
    
    private void doCompare(String left, String right) {
        
    }

    @FXML
    public void getBackups() {
        Logger.getLogger(DeployBackupController.class.getName()).log(Level.INFO,
                txtItemName.textProperty().getValue());
        String query = "SELECT name, program FROM deployBackup where name like ";
        query += "\"" + txtItemName.textProperty().get() + "%\" ORDER BY name";
        Logger.getLogger(DeployBackupController.class.getName()).log(Level.INFO, query);
        CBClient cb = new CBClient();
        Bucket bucket = cb.connect("deployBackup");
        N1qlQueryResult result = cb.doQuery(bucket, query);
        itemList.clear();
        resultsMap.clear();
        for (N1qlQueryRow row : result) {
            com.couchbase.client.java.document.json.JsonObject doc = row.value();
            resultsMap.put(doc.getString("name"), doc.getString("program"));
            itemList.add(doc.getString("name"));
        }
        listItems.itemsProperty().set(itemList);
    }

    @FXML
    public void find() {

    }

    @FXML
    public void findNext() {

    }

    @FXML
    public void findPrevious() {

    }

    @FXML
    public void newSearch() {

    }

    private void setPreview(String item) {
        txtPreview.textProperty().set(resultsMap.get(item));
    }

    private String getPathType(String library) {
        Pattern pattern = Pattern.compile(".+\\.uv[f, i, p, s]");
        Matcher matcher = pattern.matcher(library);
        if (library.equals("DM.SR") || library.equals("DM.BP")) {
            return "main";
        }
        if (matcher.matches()) {
            return "uvcode";
        }
        if (library.endsWith("LIB")) {
            return "rbo";
        }
        return null;
    }
}
