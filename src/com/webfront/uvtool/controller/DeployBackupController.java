/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.webfront.u2.util.Config;
import com.webfront.uvtool.model.ServerGroup;
import com.webfront.uvtool.app.UvTool;
import com.webfront.uvtool.util.CBClient;
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
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
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
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.events.EventException;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.NavigationActions.SelectionPolicy;

/**
 *
 * @author rlittle
 */
public class DeployBackupController implements Controller, Initializable {

    ResourceBundle res;
    private final Config config = Config.getInstance();
    private final ConfigProperties platforms = ConfigProperties.getInstance();
    private final String downloadPath;
    private final Network net = new Network();

    public static final int TEXT_WIDTH = 214;

    private static enum ItemType {
        CODE, DICT, DATA;
    }

    @FXML
    Label lblStatus;

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
    TextField txtItemName;

    @FXML
    TextField txtFind;

    @FXML
    ScrollPane scroller;

    @FXML
    VBox rightVbox;

    @FXML
    Tab previewTab;

    @FXML
    TabPane tabPane;

    private final CodeArea codeArea;

    private final ObservableList<String> itemList;
    private final SimpleStringProperty findTarget;
    private final SimpleStringProperty itemName;
    private ItemType itemType;
    private final HashMap<String, String> resultsMap;
    private String selectedItem;
    private Stage stage;
    private final boolean ON = true;
    private final boolean OFF = false;

    public DeployBackupController() {
        String path = config.getPreferences().get("downloads");
        if (!path.endsWith("/")) {
            path += "/";
        }
        downloadPath = path;
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        rightVbox = new VBox();
        previewTab = new Tab();

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

        txtItemName = new TextField();
        txtFind = new TextField();

        itemName = new SimpleStringProperty();
        itemType = ItemType.CODE;
        findTarget = new SimpleStringProperty();

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
            setStage(new Stage());
            getStage().initModality(Modality.APPLICATION_MODAL);
            getStage().setScene(new Scene(root));
            previewTab.setContent(new VirtualizedScrollPane<>(codeArea));
            getStage().setTitle(t);
            Controller ctrl = viewLoader.getController();
            ctrl.getCancelButton().setOnAction(new EventHandler() {
                @Override
                public void handle(Event event) {
                    ctrl.getCancelButton().removeEventHandler(EventType.ROOT, this);
                    stage.close();
                }
            });

            getStage().showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(UvToolController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setStage(Stage s) {
        this.stage = s;
    }

    private Stage getStage() {
        return this.stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        res = resources;
        tgItemType.selectedToggleProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                switch (((RadioButton) newValue).getId()) {
                    case "rbItemTypeDict":
                        itemType = ItemType.DICT;
                        break;
                    case "rbItemTypeData":
                        itemType = ItemType.DATA;
                        break;
                    default:
                        itemType = ItemType.CODE;
                        break;
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

        txtItemName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                getBackups();
            }
        });
    }

    private void compare(String host) {
        String hostName = getHostName("dmc", host);
        String progName = txtItemName.getText();
        doDownload(hostName, progName);

        String leftFile = downloadPath + selectedItem;
        String rightFile = downloadPath + progName + "." + hostName;
        doCompare(leftFile, rightFile);
    }

    @FXML
    public void compareDev() {
        compare("dev");
    }

    @FXML
    public void compareStaging() {
        compare("staging");
    }

    @FXML
    public void compareLive() {
        compare("live");
    }

    @FXML
    public void compareApproved() {
        ServerGroup s = new ServerGroup(platforms.getPlatforms(), "dmc");
        String host = getHostName("dmc", "live");
        String progName = txtItemName.getText();

        try {
            saveBackup(downloadPath + selectedItem);
        } catch (IOException ex) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.contentTextProperty().set(ex.getMessage());
                alert.showAndWait();
            });
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String libName;
        try {
            libName = getLibName(progName);
            String pt = getPathType(progName);
            String prefix = pt.equals("rbo") ? "RBO" : "DMC";
            String approvedId = prefix + "~" + libName + "~" + progName;
            getApproved(approvedId);
            String leftFile = downloadPath + selectedItem;
            String rightFile = downloadPath + progName + ".approved";
            doCompare(leftFile, rightFile);
        } catch (JSchException | FileNotFoundException | EventException ex) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.contentTextProperty().set(ex.getMessage());
                alert.showAndWait();
            });
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void doCompare(String left, String right) {
        String diff = config.getPreferences().get("diffProgram");
        String cmd = diff + " " + left + " " + right;
        Runnable task = () -> {
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.contentTextProperty().set(ex.getMessage());
                    alert.showAndWait();
                });
                Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void doDownload(String host, String progName) {
        ServerGroup s = new ServerGroup(platforms.getPlatforms(), "dmc");
        Thread t = new Thread(() -> {
            try {
                saveBackup(downloadPath + selectedItem);
            } catch (IOException ex) {
                Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                String libName = getLibName(progName);
                String remotePath = s.getPath(getPathType(libName)) + "/" + libName;
                net.doSftpGet(host, remotePath, progName, downloadPath,
                        progName + "." + host);
            } catch (JSchException | SftpException | IOException ex) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.contentTextProperty().set(ex.getMessage());
                    alert.showAndWait();
                });
                Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
            }

        });
        t.start();
        while (t.isAlive()) {
        };
    }

    @FXML
    public void getBackups() {
        toggleCursor(ON);
        Thread t = new Thread(() -> {
            String query = "SELECT name, program FROM deployBackup where name like ";
            query += "\"" + txtItemName.textProperty().get() + "%\" ORDER BY name";
            CBClient cb = new CBClient();
            Bucket bucket = cb.connect("deployBackup");
            N1qlQueryResult result = cb.doQuery(bucket, query);
            itemList.clear();
            resultsMap.clear();

            if (result.allRows().isEmpty()) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.contentTextProperty().set(txtItemName.getText() + " not found");
                    alert.showAndWait();
                });
            } else {
                for (N1qlQueryRow row : result) {
                    com.couchbase.client.java.document.json.JsonObject doc = row.value();
                    resultsMap.put(doc.getString("name"), doc.getString("program"));
                    itemList.add(doc.getString("name"));
                }
                listItems.itemsProperty().set(itemList);
            }
            toggleCursor(OFF);
        });
        t.start();
    }

    private String getHostName(String platform, String server) {
        ServerGroup s = new ServerGroup(platforms.getPlatforms(), platform);
        return s.getHost(server);
    }

    private String getLibName(String progName) throws JSchException {
        ServerGroup s = new ServerGroup(platforms.getPlatforms(), "dmc");
        String path = s.getPath("main");
        String host = s.getHost("dev");

        ByteArrayOutputStream output = net.sshExec(host, path,
                "getDir " + progName);
        if (output.toString().isEmpty()) {
            return "";
        }
        String[] result = (output.toString()).split("\n");

        String libName = result[result.length - 1];
        return libName;
    }

    private void getApproved(String approvedId) throws
            FileNotFoundException, EventException {
        ServerGroup s = new ServerGroup(platforms.getPlatforms(), "dmc");
        String path = s.getPath("main");
        String host = s.getHost("approved");
        ByteArrayOutputStream output;
        try {
            output = net.sshExec(host, path,
                    "getApproved CODE " + approvedId);
        } catch (JSchException ex) {
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String remotePath = s.getPath("deploy") + "/APPROVED.PROGRAMS";
        String item = (approvedId.split("~")[2]) + ".approved";
        try {
            net.doSftpGet(host, remotePath, approvedId, downloadPath, item);
        } catch (JSchException ex) {
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SftpException ex) {
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedReader f = new BufferedReader(new FileReader(downloadPath + item));
        StringBuilder fileOutput = new StringBuilder();
        try {
            while (true) {
                String line = f.readLine();
                if (line == null) {
                    break;
                }
                fileOutput.append(line);
            }
            f.close();
            if (fileOutput.indexOf("no APPROVED.PROGRAMS code found!") > 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.showAndWait();
                File aFile = new File(downloadPath + item);
                if (aFile.exists()) {
                    aFile.delete();
                }
                throw new FileNotFoundException("No approved version found");
            }
        } catch (IOException ex) {

        }
    }

    @FXML
    public void findNext() {
        String s = codeArea.getText();
        String t = txtFind.getText();
        int pos = s.indexOf(t, codeArea.getSelection().getEnd());
        if (pos < 0) {
            pos = s.indexOf(t);
        }
        if (pos < 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.contentTextProperty().set(txtFind.getText() + " not found");
            alert.showAndWait();
        }
        int cp = codeArea.getCaretPosition();
        codeArea.selectRange(pos, pos + t.length());
        IndexRange origin = codeArea.getSelection();

        codeArea.moveTo(origin.getStart());
        codeArea.requestFollowCaret();
        codeArea.lineStart(SelectionPolicy.CLEAR);
        int start = codeArea.getCaretPosition();

        codeArea.moveTo(origin.getEnd());
        codeArea.lineEnd(SelectionPolicy.CLEAR);
        int end = codeArea.getCaretPosition();

        codeArea.selectRange(start, end);
    }

    @FXML
    public void findPrevious() {
        String text = codeArea.getText();
        String searchText = txtFind.getText();
        if (searchText == null || searchText.isEmpty()) {
            return;
        }
        int previousIndex = text.lastIndexOf(searchText, codeArea.getSelection().getStart() - 1);
        if (previousIndex < 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.contentTextProperty().set("No previous match found");
            alert.showAndWait();
            return;
        }

        codeArea.selectRange(previousIndex, previousIndex + searchText.length());
        IndexRange origin = codeArea.getSelection();

        codeArea.moveTo(origin.getStart());
        codeArea.requestFollowCaret();
        codeArea.lineStart(SelectionPolicy.CLEAR);
        int start = codeArea.getCaretPosition();

        codeArea.moveTo(origin.getEnd());
        codeArea.lineEnd(SelectionPolicy.CLEAR);
        int end = codeArea.getCaretPosition();

        codeArea.selectRange(start, end);
    }

    @FXML
    public void newSearch() {
        txtFind.textProperty().set("");
        codeArea.clear();
        itemList.clear();
        txtItemName.textProperty().set("");
        previewTab.setText("");
        txtItemName.requestFocus();
    }

    private void saveBackup(String progName) throws IOException {
        String backupFile = resultsMap.get(selectedItem);
        backupFile = backupFile.replaceAll("\\|", "\n");
        try (FileWriter file = new FileWriter(progName)) {
            file.write(backupFile);
            file.flush();
            file.close();
        }
    }

    private void setPreview(String item) {
        String sb = resultsMap.get(item);
        if (sb == null) {
            return;
        }
        sb = sb.replaceAll("\\|", "\n");
        codeArea.clear();
        codeArea.replaceText(0, 0, sb);
        previewTab.setText(item);
        previewTab.setContent(new VirtualizedScrollPane<>(codeArea));
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

    private void toggleCursor(boolean turnItOn) {
        if (turnItOn) {
            Platform.runLater(() -> getStage().getScene().getRoot().setCursor(Cursor.WAIT));
        } else {
            Platform.runLater(() -> getStage().getScene().getRoot().setCursor(Cursor.DEFAULT));
        }
    }

}
