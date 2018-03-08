/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.webfront.u2.model.Program;
import com.webfront.u2.model.Prompt;
import com.webfront.u2.model.UvFile;
import com.webfront.u2.util.Config;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
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
    Button btnAddRow;

    @FXML
    Button btnCancel;

    @FXML
    Button btnDelete;

    @FXML
    Button btnDeleteRow;

    @FXML
    Button btnSave;

    @FXML
    CheckBox chkIsSubroutine;

    @FXML
    ComboBox<Program> cbAppSelector;

    @FXML
    TableView<Prompt> tblInputs;

    @FXML
    TableColumn tblColInputNumber;

    @FXML
    TableColumn tblColPrompt;

    @FXML
    TextArea txtDescription;

    @FXML
    TextArea txtReadFiles;

    @FXML
    TextArea txtWriteFiles;

    @FXML
    TextField txtAppName;

    @FXML
    TextField txtPackage;

    private final Config config = Config.getInstance();

    public ProgramController() {
        chkIsSubroutine = new CheckBox();
        cbAppSelector = new ComboBox<>();
        txtAppName = new TextField();
        txtDescription = new TextArea();
        txtPackage = new TextField();
        txtReadFiles = new TextArea();
        txtWriteFiles = new TextArea();
        tblInputs = new TableView<>();
        tblColInputNumber = new TableColumn();
        tblColPrompt = new TableColumn();
        tblColPrompt.setCellFactory(TextFieldTableCell.forTableColumn());
        tblColPrompt.setOnEditCommit(new EventHandler<CellEditEvent<Prompt, String>>() {
            @Override
            public void handle(CellEditEvent<Prompt, String> t) {
                System.out.println(t.getSource().toString());
            }
        });
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
        tblColInputNumber.setCellValueFactory(new PropertyValueFactory<>("num"));
        tblColPrompt.setCellValueFactory(new PropertyValueFactory<>("message"));
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

        Program p;
        if (o == null || o instanceof String) {
            String desc = txtDescription.getText();
            p = new Program();
            p.setName(txtAppName.getText());
            p.setClassName(txtPackage.getText());
            p.setDescription(desc == null ? "" : txtDescription.getText());
            p.setSubroutine(chkIsSubroutine.isSelected());
            int appId = config.addProgram(p);
            if (appId == -1) {
                return;
            }
            p.setId(appId);
        } else {
            p = cbAppSelector.getValue();
            isNew = false;
        }

        String[] rdFiles = txtReadFiles.getText().split("\n");
        String[] wrFiles = txtWriteFiles.getText().split("\n");

        if (!txtAppName.getText().isEmpty()) {
            p.setName(txtAppName.getText());
        }
        p.setClassName(txtPackage.getText());
        p.setDescription(txtDescription.getText());
        p.setSubroutine(chkIsSubroutine.isSelected());
        ArrayList<UvFile> fileList = new ArrayList<>();

        if (rdFiles.length > 0) {
            for (String s : rdFiles) {
                if (s.isEmpty()) {
                    continue;
                }
                fileList.add(new UvFile(p.getId(), s, true, false));
            }
        }
        if (wrFiles.length > 0) {
            for (String s : wrFiles) {
                if (s.isEmpty()) {
                    continue;
                }
                fileList.add(new UvFile(p.getId(), s, false, true));
            }
        }

        p.setFileList(fileList);
        p.getPrompts().clear();
        for (Prompt prp : tblInputs.getItems()) {
            int pNum = prp.getNum();
            String msg = prp.getMessage();
            p.getPrompts().put(pNum, prp);
        }

        if (isNew) {
//            config.addProgram(p);
            if (fileList.size() > 0) {
                config.addFiles(fileList);
            }
        } else {
            config.updateProgram(p);
        }
        cbAppSelector.setEditable(false);
    }

    @FXML
    public void onAddRow() {
        Object o = cbAppSelector.getValue();
        Prompt p = new Prompt();
        int nextPrompt = 1;
        if (o instanceof Program) {
            Program prog = (Program) o;
            nextPrompt = prog.getPrompts().size() + 1;
        }
        p.setNum(nextPrompt);
        tblInputs.getItems().add(p);
    }

    @FXML
    public void onDeleteRow() {

    }

    @FXML
    public void onAppSelect() {
        Object o = cbAppSelector.getValue();
        if (o instanceof String) {
            return;
        }
        Program p = cbAppSelector.getValue();
        if (p == null) {
            return;
        }
        txtPackage.setText(p.getClassName());
        txtDescription.setText(p.getDescription());
        txtReadFiles.clear();
        txtWriteFiles.clear();

        for (UvFile uvf : p.getFileList()) {
            String fname = uvf.getFileName();
            if (uvf.isRead()) {
                txtReadFiles.appendText(fname + "\n");
            }
            if (uvf.isWrite()) {
                txtWriteFiles.appendText(fname + "\n");
            }
        }
        tblInputs.setItems(p.getPromptList());
        chkIsSubroutine.selectedProperty().set(p.isSubroutine());
    }

}
