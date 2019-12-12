/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.webfront.u2.util.Config;
import com.webfront.uvtool.model.Server;
import com.webfront.uvtool.app.UvTool;
import com.webfront.uvtool.util.CBClient;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.w3c.dom.events.EventException;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CaretNode;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.NavigationActions.SelectionPolicy;

/**
 *
 * @author rlittle
 */
public class DeployBackupController implements Controller, Initializable {

    ResourceBundle res;
    private final Config config = Config.getInstance();
    private final String downloadPath;
    private final Network net = new Network();

    public static final int TEXT_WIDTH = 214;
    private HTMLEditor result;

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

    private CodeArea codeArea;

    private final ObservableList<String> itemList;
    private final SimpleStringProperty findTarget;
    private final SimpleStringProperty itemName;
    private ItemType itemType;
    private final HashMap<String, String> resultsMap;
    private String selectedItem;
    private InlineCssTextArea area;

    public DeployBackupController() {
        String path = config.getPreferences().get("downloads");
        if (!path.endsWith("/")) {
            path += "/";
        }
        downloadPath = path;
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        area = new InlineCssTextArea();
        rightVbox = new VBox();
        previewTab = new Tab();
//        previewTab.getChildren().add(new VirtualizedScrollPane<>(codeArea));

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

        result = new HTMLEditor();
        result.setStyle("-fx-font: 12 FreeMono;");
    }

    private void addExtraCaret() {
        CaretNode extraCaret = new CaretNode("another caret", area);
        if (!area.addCaret(extraCaret)) {
            throw new IllegalStateException("caret was not added to area");
        }
        extraCaret.moveTo(100, 8);

        // since the CSS properties are re-set when it applies the CSS from files
        // remove the style class so that properties set below are not overridden by CSS
        extraCaret.getStyleClass().remove("caret");

        extraCaret.setStrokeWidth(10.0);
        extraCaret.setStroke(Color.BROWN);
        extraCaret.setBlinkRate(Duration.millis(200));
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
            previewTab.setContent(new VirtualizedScrollPane<>(codeArea));
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
//        txtPreview.setPrefColumnCount(80);
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

//        txtFind.textProperty().bind(findTarget);
        txtItemName.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                getBackups();
            }
        });
    }

    @FXML
    public void compareDev() {
        Server s = new Server(net.getPlatforms(), "dmc");
        String host = getHostName("dmc", "dev");
        String progName = txtItemName.getText();

        try {
            saveBackup(downloadPath + progName);
        } catch (IOException ex) {
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String libName = getLibName(progName);
        String remotePath = s.getPath(getPathType(libName)) + "/" + libName;

        net.doSftp(host, remotePath, progName, downloadPath,
                progName + "." + host);
        // TODO: make system call to diff program
        String leftFile = downloadPath + selectedItem;
        String rightFile = downloadPath + progName + "." + host;
        doCompare(leftFile, rightFile);
    }

    @FXML
    public void compareStaging() {
        Server s = new Server(net.getPlatforms(), "dmc");
        String host = getHostName("dmc", "staging");
        String progName = txtItemName.getText();

        try {
            saveBackup(downloadPath + progName);
        } catch (IOException ex) {
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String libName = getLibName(progName);
        String remotePath = s.getPath(getPathType(libName)) + "/" + libName;

        net.doSftp(host, remotePath, progName, downloadPath,
                progName + "." + host);
        // TODO: make system call to diff program
        String leftFile = downloadPath + selectedItem;
        String rightFile = downloadPath + progName + "." + host;
        doCompare(leftFile, rightFile);
    }

    @FXML
    public void compareLive() {
        Server s = new Server(net.getPlatforms(), "dmc");
        String host = getHostName("dmc", "live");
        String progName = txtItemName.getText();

        try {
            saveBackup(downloadPath + progName);
        } catch (IOException ex) {
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String libName = getLibName(progName);
        String remotePath = s.getPath(getPathType(libName)) + "/" + libName;

        net.doSftp(host, remotePath, progName, downloadPath,
                progName + "." + host);
        String leftFile = downloadPath + selectedItem;
        String rightFile = downloadPath + progName + "." + host;
        doCompare(leftFile, rightFile);
    }

    @FXML
    public void compareApproved() {
        Server s = new Server(net.getPlatforms(), "dmc");
        String host = getHostName("dmc", "live");
        String progName = txtItemName.getText();

        try {
            saveBackup(downloadPath + selectedItem);
        } catch (IOException ex) {
            Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String libName = getLibName(progName);
        String pt = getPathType(progName);
        String prefix = pt.equals("rbo") ? "RBO" : "DMC";
        String approvedId = prefix + "~" + libName + "~" + progName;
        try {
            getApproved(approvedId);
            String leftFile = downloadPath + selectedItem;
            String rightFile = downloadPath + progName + ".approved";
            doCompare(leftFile, rightFile);
        } catch (FileNotFoundException ex) {
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
                Logger.getLogger(DeployBackupController.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
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

    private String getHostName(String platform, String server) {
        Server s = new Server(net.getPlatforms(), platform);
        return s.getHost(server);
    }

    private String getLibName(String progName) {
        Server s = new Server(net.getPlatforms(), "dmc");
        String path = s.getPath("main");
        String host = s.getHost("dev");
        ByteArrayOutputStream output = net.sshExec(host, path,
                "getDir " + progName);
        String[] result = (output.toString()).split("\n");

        String libName = result[result.length - 1];
        return libName;
    }

    private void getApproved(String approvedId) throws
            FileNotFoundException, EventException {
        Server s = new Server(net.getPlatforms(), "dmc");
        String path = s.getPath("main");
        String host = s.getHost("approved");
        ByteArrayOutputStream output = net.sshExec(host, path,
                "getApproved CODE " + approvedId);
        if (output.size() > 0) {
            throw new EventException((short) -1, "sshExec error");
        }
        String remotePath = s.getPath("deploy") + "/APPROVED.PROGRAMS";
        String item = (approvedId.split("~")[2]) + ".approved";
        net.doSftp(host, remotePath, approvedId, downloadPath, item);
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
    public void find() {
//        matchRegExp();
        findNext();
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
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
        FileWriter file = new FileWriter(progName);
        file.write(backupFile);
        file.close();
    }

    private void setPreview(String item) {
        System.out.println(item);
        String sb = resultsMap.get(item);
        if (sb == null) {
            return;
        }
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
    
}
