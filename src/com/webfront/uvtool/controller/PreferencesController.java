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
    private TextField txtTempLocation;

    @FXML
    private Button btnTempLocation;

    @FXML
    private TextField txtDiffProgram;

    @FXML
    private Button btnDiffProgram;

    @FXML
    private Button btnOk;

    @FXML
    private Button btnCancel;

    public PreferencesController() {
        btnTempLocation = new Button();
        btnDiffProgram = new Button();
        btnOk = new Button();
        btnCancel = new Button();
        txtTempLocation = new TextField();
        txtDiffProgram = new TextField();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        res = rb;
        txtTempLocation.setText(config.getPreferences().get("tempDir"));
        txtDiffProgram.setText(config.getPreferences().get("diffProgram"));
    }

    @FXML
    public void btnOkOnClick() {
        config.getPreferences().put("tmpDir", txtTempLocation.getText());
        config.getPreferences().put("diffProgram", txtDiffProgram.getText());
        try {
            config.updatePreferences("tmpDir", txtTempLocation.getText());
            config.updatePreferences("diffProgram", txtDiffProgram.getText());
            stage.close();
        } catch (SQLException ex) {
            Logger.getLogger(PreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    @Override
    public void updateLed(String host, boolean onOff) {
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

    @Override
    public void display(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setStage(Stage s) {
        this.stage = s;
    }

    @Override
    public void launch(String v, String t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
}
