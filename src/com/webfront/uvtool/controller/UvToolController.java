/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.couchbase.client.java.Bucket;
import com.webfront.u2.model.Account;
import com.webfront.u2.model.Profile;
import com.webfront.u2.model.Server;
import com.webfront.u2.model.User;
import com.webfront.u2.util.Config;
import com.webfront.u2.util.ServerConverter;
import com.webfront.uvtool.app.UvTool;
import com.webfront.uvtool.util.CBClient;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author rlittle
 */
public class UvToolController implements Initializable {

    private final Config config;
    private final ObservableList<Account> accountList = FXCollections.observableArrayList();
    private final ObservableList<Profile> profileList = FXCollections.observableArrayList();
    private final ObservableList<Server> serverList = FXCollections.<Server>observableArrayList();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private final ObservableList<String> programList = FXCollections.observableArrayList();

    ResourceBundle res;

    @FXML
    ComboBox cbServers;
    @FXML
    ComboBox cbProfiles;

    @FXML
    Label statusMessage;

    @FXML
    Button btnRun;
    @FXML
    Button btnQuery;
    @FXML
    Button btnCompare;
    @FXML
    Button btnPull;
    @FXML
    Button btnCopy;
    @FXML
    Button btnBackups;
    @FXML
    Button btnPeerReview;

    @FXML
    MenuItem fileExit;
    @FXML
    MenuItem mnuFileNewAccount;
    @FXML
    MenuItem mnuFileNewServer;
    @FXML
    MenuItem mnuFileNewProfile;
    @FXML
    MenuItem mnuFileNewProgram;
    @FXML
    MenuItem mnuEditAccount;
    @FXML
    MenuItem mnuEditServer;
    @FXML
    MenuItem mnuEditProfile;
    @FXML
    MenuItem mnuEditApp;

    Image copyImage;
    ImageView copyImageView;
    Image runImage;
    ImageView runImageView;
    Image compareImage;
    ImageView compareImageView;
    Image pullImage;
    ImageView pullImageView;
    Image peerImage;
    ImageView peerImageView;
    Image backupImage;
    ImageView backupImageView;

    public UvToolController() {
        copyImage = new Image(getClass().getResourceAsStream("/com/webfront/uvtool/image/copy.png"), 50, 50, false, false);
        runImage = new Image(getClass().getResourceAsStream("/com/webfront/uvtool/image/gears2.png"), 50, 50, true, false);
        compareImage = new Image(getClass().getResourceAsStream("/com/webfront/uvtool/image/find.png"), 50, 50, false, false);
        pullImage = new Image(getClass().getResourceAsStream("/com/webfront/uvtool/image/arrows.png"), 50, 50, false, false);
        peerImage = new Image(getClass().getResourceAsStream("/com/webfront/uvtool/image/review.png"), 50, 50, false, false);
        backupImage = new Image(getClass().getResourceAsStream("/com/webfront/uvtool/image/backup.png"), 50, 50, false, false);
        
        copyImageView = new ImageView(copyImage);
        runImageView = new ImageView(runImage);
        compareImageView = new ImageView(compareImage);
        pullImageView = new ImageView(pullImage);
        peerImageView = new ImageView(peerImage);
        backupImageView = new ImageView(backupImage);
        
        config = Config.getInstance();
        accountList.setAll(config.getAccounts());
        profileList.setAll(config.getProfiles());
        serverList.setAll(config.getServers());
        serverList.sort(Server.ServerComparator);
        userList.setAll(config.getUsers());
        cbServers = new ComboBox<>();
        cbProfiles = new ComboBox<>();
        btnRun = new Button();
        btnQuery = new Button();
        btnCompare = new Button();
        btnPull = new Button();
        btnBackups = new Button();
        btnPeerReview = new Button();
        mnuFileNewAccount = new MenuItem();
        mnuFileNewServer = new MenuItem();
        mnuFileNewProfile = new MenuItem();
        mnuFileNewProgram = new MenuItem();
        mnuEditAccount = new MenuItem();
        mnuEditServer = new MenuItem();
        mnuEditProfile = new MenuItem();
        mnuEditApp = new MenuItem();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        res = rb;
        cbServers.converterProperty().setValue(new ServerConverter());
        cbServers.getItems().addAll(serverList);
        btnCopy.setGraphic(copyImageView);
        btnRun.setGraphic(runImageView);
        btnPeerReview.setGraphic(peerImageView);
        btnCompare.setGraphic(compareImageView);
        btnBackups.setGraphic(backupImageView);
        btnPull.setGraphic(pullImageView);
        
        btnCopy.setAlignment(Pos.CENTER);

        cbServers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
//                btnCompareServer.disableProperty().set(newValue.toString().equalsIgnoreCase("Select"));
            }
        });

        btnCopy.setOnAction(event -> launch("viewCopy", "titleCopy"));
        btnCopy.addEventHandler(MouseEvent.MOUSE_ENTERED, new MouseOver());
        btnCopy.addEventHandler(MouseEvent.MOUSE_EXITED, new MouseOut());

        btnCompare.setOnAction(event -> launch("viewCompareSource", "titleCompareSource"));
        btnCompare.addEventHandler(MouseEvent.MOUSE_ENTERED, new MouseOver());
        btnCompare.addEventHandler(MouseEvent.MOUSE_EXITED, new MouseOut());

        btnQuery.setOnAction(event -> launch("viewQuery", "titleQuery"));
        btnQuery.addEventHandler(MouseEvent.MOUSE_ENTERED, new MouseOver());
        btnQuery.addEventHandler(MouseEvent.MOUSE_EXITED, new MouseOut());

        btnRun.setOnAction(event -> launch("viewRun", "titleRun"));
        btnRun.addEventHandler(MouseEvent.MOUSE_ENTERED, new MouseOver());
        btnRun.addEventHandler(MouseEvent.MOUSE_EXITED, new MouseOut());

        btnPull.setOnAction(event -> launch("viewPullSource", "titlePullSource"));
        btnPull.addEventHandler(MouseEvent.MOUSE_ENTERED, new MouseOver());
        btnPull.addEventHandler(MouseEvent.MOUSE_EXITED, new MouseOut());

        btnBackups.setOnAction(event -> launch("viewBackups", "titleBackups"));
        btnBackups.addEventHandler(MouseEvent.MOUSE_ENTERED, new MouseOver());
        btnBackups.addEventHandler(MouseEvent.MOUSE_EXITED, new MouseOut());

        btnPeerReview.setOnAction(event -> launch("viewPeer", "titlePeer"));
        btnPeerReview.addEventHandler(MouseEvent.MOUSE_ENTERED, new MouseOver());
        btnPeerReview.addEventHandler(MouseEvent.MOUSE_EXITED, new MouseOut());

    }

    public MenuItem getFileExit() {
        return fileExit;
    }

    private void launch(String view, String title) {
        final FXMLLoader viewLoader = new FXMLLoader();
        String v = res.getString(view);
        String t = res.getString(title);
        URL url = UvTool.class.getResource(v);
        viewLoader.setLocation(url);
        viewLoader.setResources(res);
        try {
            final Pane root = viewLoader.<Pane>load();
            final Stage stage = new Stage();
            final Scene scene = new Scene(root);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            scene.getStylesheets().add(getClass().getResource("/css/runview.css").toExternalForm());
            stage.setTitle(t);
            Controller ctrl = viewLoader.getController();
            if (ctrl.getCancelButton() != null) {
                ctrl.getCancelButton().setOnAction(new EventHandler() {
                    @Override
                    public void handle(Event event) {
                        ctrl.getCancelButton().removeEventHandler(EventType.ROOT, this);
                        stage.close();
                    }
                });
            }
            ctrl.setStage(stage);

            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(UvToolController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    public void displayMessage(String msg) {
        statusMessage.setText(msg);
    }

    @FXML
    public void removeMessage() {
        statusMessage.setText("");
    }

    class MouseOver implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent event) {
            Button src = (Button) event.getSource();
            String id = src.getId();
            String msg = res.getString(id);
            displayMessage(msg);
        }
    }

    class MouseOut implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent event) {
            removeMessage();
        }
    }

    @FXML
    public void onFileNewAccount() {
        launch("viewAccount", "titleAccount");
    }

    @FXML
    public void onFileNewServer() {
        launch("viewServer", "titleServer");
    }

    @FXML
    public void onFileNewProfile() {
        launch("viewProfile", "titleProfile");
    }

    @FXML
    public void onFileNewProgram() {
        launch("viewProgram", "titleProgram");
    }

    @FXML
    public void onEditAccount() {
        launch("viewAccount", "titleAccount");
    }

    @FXML
    public void onEditServer() {
        launch("viewServer", "titleServer");
    }

    @FXML
    public void onEditProfile() {
        launch("viewProfile", "titleProfile");
    }

    @FXML
    public void onEditApp() {
        launch("viewAppEdit", "titleAppEdit");
    }

    @FXML
    public void onEditPreferences() {
        launch("viewPreferences", "titleEditPreferences");
    }

    @FXML
    public void onMnuHelpAbout() {
        launch("viewHelpAbout", "titleHelpAbout");
    }

    @FXML
    void testDb() {
        CBClient cb = new CBClient();
        Bucket bucket = cb.connect("deployBackup");
        cb.testQuery(bucket);
        cb.disconnect(bucket);
    }

}
