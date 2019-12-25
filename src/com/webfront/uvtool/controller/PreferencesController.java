/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.webfront.u2.util.Config;
import com.webfront.u2.util.Progress;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author rlittle
 */
public class PreferencesController implements Controller, Initializable, Progress {

    private ResourceBundle res;
    private final Config config = Config.getInstance();
    private Stage stage;

    @FXML
    private Button btnDownloads;

    @FXML
    private TextField txtDownloads;

    @FXML
    private TextField txtTempLocation;

    @FXML
    private Button btnTempLocation;

    @FXML
    private TextField txtDiffProgram;

    @FXML
    private Button btnDiffProgram;

    @FXML
    private TextField txtEditor;

    @FXML
    private Button btnEditor;

    @FXML
    private Button btnOk;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnProjectHome;
    @FXML
    private Button btnCodeHome;
    @FXML
    private Button btnDataHome;

    @FXML
    private TextField txtProjectHome;
    @FXML
    private TextField txtCodeHome;
    @FXML
    private TextField txtDataHome;
    @FXML
    private CheckBox chkLoadData;

    public PreferencesController() {
        btnDownloads = new Button();
        btnTempLocation = new Button();
        btnDiffProgram = new Button();
        btnOk = new Button();
        btnCancel = new Button();
        btnEditor = new Button();
        btnProjectHome = new Button();
        btnCodeHome = new Button();
        btnDataHome = new Button();
        txtTempLocation = new TextField();
        txtDiffProgram = new TextField();
        txtEditor = new TextField();
        txtDownloads = new TextField();
        txtProjectHome = new TextField();
        txtCodeHome = new TextField();
        txtDataHome = new TextField();
        chkLoadData = new CheckBox();
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        res = rb;
        txtTempLocation.setText(config.getPreferences().get("tempDir"));
        txtDiffProgram.setText(config.getPreferences().get("diffProgram"));
        txtEditor.setText(config.getPreferences().get("editor"));
        txtDownloads.setText(config.getPreferences().get("downloads"));
        txtProjectHome.setText(config.getPreferences().get("projectHome"));
        txtCodeHome.setText(config.getPreferences().get("codeHome"));
        txtDataHome.setText(config.getPreferences().get("dataHome"));
        chkLoadData.selectedProperty().set(config.getPreferences().get("loadData").equals("1"));
    }

    @FXML
    public void btnOkOnClick() {
        config.getPreferences().put("tmpDir", txtTempLocation.getText());
        config.getPreferences().put("diffProgram", txtDiffProgram.getText());
        config.getPreferences().put("editor", txtEditor.getText());
        config.getPreferences().put("projectHome", txtProjectHome.getText());
        config.getPreferences().put("codeHome", txtCodeHome.getText());
        config.getPreferences().put("dataHome", txtDataHome.getText());
        config.getPreferences().put("loadData", chkLoadData.isSelected() ? "1" : "0");
        try {
            config.updatePreferences("tmpDir", txtTempLocation.getText());
            config.updatePreferences("diffProgram", txtDiffProgram.getText());
            config.updatePreferences("editor", txtEditor.getText());
            config.updatePreferences("downloads", txtDownloads.getText());
            config.updatePreferences("projectHome", txtProjectHome.getText());
            config.updatePreferences("codeHome", txtCodeHome.getText());
            config.updatePreferences("dataHome", txtDataHome.getText());
            stage.close();
        } catch (SQLException ex) {
            Logger.getLogger(PreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void btnCodeHomeOnClick() {
        String result = browse(true, "Select location for code downloads");
        if (result == null) {
            return;
        }
        txtCodeHome.setText(result);
    }

    @FXML
    public void btnDataHomeOnClick() {
        String result = browse(true, "Select location for data downloads");
        if (result == null) {
            return;
        }
        txtDataHome.setText(result);
    }

    @FXML
    public void btnTempLocationOnClick() {
        String result = browse(true, "Select location for temporary files");
        if (result == null) {
            return;
        }
        txtTempLocation.setText(result);
    }

    @FXML
    public void btnDiffProgramOnClick() {
        String result = browse(false, "Select diff program");
        if (result == null) {
            return;
        }
        txtDiffProgram.setText(result);
    }

    @FXML
    public void btnProjectHomeOnClick() {
        String result = browse(true, "Select location for projects");
        if (result == null) {
            return;
        }
        txtProjectHome.setText(result);
    }

    @FXML
    void btnDownloadsOnClick() {
        String result = browse(false, "Select location for downloads");
        if (result == null) {
            return;
        }
        txtDownloads.setText(result);
    }

    @FXML
    public void btnEditorOnClick() {
        String result = browse(false, "Select preferred text editor");
        if (result == null) {
            return;
        }
        txtEditor.setText(result);
    }

    @Override
    public void setStage(Stage s) {
        this.stage = s;
    }

    @Override
    public Button getCancelButton() {
        return this.btnCancel;
    }

    private String browse(boolean isDirectory, String title) {
        if (isDirectory) {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle(title);
            File selectedFile = dc.showDialog(stage.getOwner());
            if (selectedFile != null) {
                return selectedFile.getAbsolutePath();
            }
        } else {
            FileChooser fc = new FileChooser();
            fc.setTitle(title);
            File selectedFile = fc.showOpenDialog(stage.getOwner());
            if (selectedFile != null) {
                return selectedFile.getAbsolutePath();
            }
        }
        return null;
    }

    @Override
    public void updateLed(String host, boolean onOff) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateProgressBar(Double p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void state(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void display(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void launch(String v, String t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
