/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.webfront.u2.model.Program;
import com.webfront.u2.model.UvFile;
import com.webfront.u2.util.Config;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author rlittle
 */
public class ProgramController implements Controller, Initializable {

    @FXML
    Button btnCancel;

    @FXML
    Button btnDelete;

    @FXML
    Button btnSave;

    @FXML
    ComboBox<Program> cbAppSelector;

    @FXML
    TextArea txtLocalFiles;

    @FXML
    TextArea txtRemoteFiles;

    @FXML
    TextField txtAppName;

    @FXML
    TextField txtPackage;

    private final Config config = Config.getInstance();

    public ProgramController() {
        cbAppSelector = new ComboBox<>();
        txtAppName = new TextField();
    }

    @Override
    public void launch(String v, String t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Button getCancelButton() {
        return btnCancel;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbAppSelector.setItems(config.getPrograms());
        cbAppSelector.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cbAppSelector.editableProperty().set(false);
                event.consume();
            } else {
                if (event.getCode() == KeyCode.ENTER) {
                    Program p = cbAppSelector.getValue();
                    event.consume();
                }
            };
        });
        cbAppSelector.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            int clicks = event.getClickCount();
            if (clicks == 2) {
                if (!cbAppSelector.editableProperty().get()) {
                    cbAppSelector.setEditable(true);
                }
            }
        });

    }
    
    @FXML
    public void onBtnDelete() {
        Program p = cbAppSelector.getValue();
        if (p == null) {
            return;
        }
        config.deleteProgram(p);
    }

    @FXML
    public void onBtnSave() {
        Object o = cbAppSelector.getValue();
        boolean isNew = true;
        if (o == null) {
            return;
        }
        Program p;
        if (o instanceof String) {
            p = new Program();
            int appId = config.addProgram(p);
            if (appId == -1) {
                return;
            }
            p.setId(appId);
            p.setName((String) o);
        } else {
            p = cbAppSelector.getValue();
            isNew = false;
        }
        String[] rfiles = txtRemoteFiles.getText().split("\n");
        String[] lfiles = txtLocalFiles.getText().split("\n");
        if (!txtAppName.getText().isEmpty()) {
            p.setName(txtAppName.getText());
        }
        p.setClassName(txtPackage.getText());

        ArrayList<UvFile> fileList = new ArrayList<>();
        if (rfiles.length > 0) {
            for (String s : rfiles) {
                if (s.isEmpty()) {
                    continue;
                }
                fileList.add(new UvFile(p.getId(), s, true, false));
            }
        }
        if (lfiles.length > 0) {
            for (String s : lfiles) {
                if (s.isEmpty()) {
                    continue;
                }
                fileList.add(new UvFile(p.getId(), s, false, true));
            }
        }

        p.setFileList(fileList);
        
        if (isNew) {
             config.addProgram(p);
            if (fileList.size() > 0) {
                config.addFiles(fileList);
            }
        } else {
            config.updateProgram(p);
        }
        cbAppSelector.setEditable(false);
    }

    @FXML
    public void onAppSelect() {
        Object o = cbAppSelector.getValue();
        if (o instanceof String) {
            return;
        }
        Program p = cbAppSelector.getValue();
        if(p==null) {
            return;
        }
        txtPackage.setText(p.getClassName());
        txtLocalFiles.clear();
        txtRemoteFiles.clear();

        for (UvFile uvf : p.getFileList()) {
            String fname = uvf.getFileName();
            if (uvf.isLocal()) {
                txtLocalFiles.appendText(fname + "\n");
            }
            if (uvf.isRemote()) {
                txtRemoteFiles.appendText(fname + "\n");
            }
        }
    }

}
