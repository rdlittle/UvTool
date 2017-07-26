/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.controller;

import com.webfront.uvtool.view.WizPage1;
import com.webfront.uvtool.view.WizPage2;
import com.webfront.uvtool.view.WizardPage;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Stack;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author rlittle
 */
public class VirtualBatchWizardController extends StackPane implements Initializable, Controller {

    @FXML
    Button btnCancel;
    @FXML
    Button btnNext;
    @FXML
    Button btnPrev;

    @FXML
    RadioButton rbProgram;
    @FXML
    RadioButton rbSubroutine;

    @FXML
    StackPane stackPane;

    @FXML
    ToggleGroup tgProgtype;

    private static final int UNDEFINED = -1;
    private ObservableList<WizardPage> pageList;
    private int currentPage;
    private final Stack<Integer> history = new Stack<>();
    private Stage stage;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentPage = UNDEFINED;
        btnCancel = new Button();
        btnNext = new Button();
        btnPrev = new Button();
        stackPane = new StackPane();
        pageList = FXCollections.<WizardPage>observableArrayList();
        String view1;
        String view2;
        view1 = rb.getString("viewWizPage1");
        view2 = rb.getString("viewWizPage2");
        pageList.addAll(new WizPage1(view1),new WizPage2(view2));
        getChildren().clear();
        getChildren().add(pageList.get(0));
    }
    
    public void setStage(Stage s) {
        stage = s;
    }
    
    public Stage getStage() {
        return stage;
    }

    @Override
    public void launch(String v, String t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Button getCancelButton() {
        return btnCancel;
    }

    @FXML
    void onBtnNext() {
    }

    @FXML
    void onBtnPrev() {

    }

    boolean hasNextPage() {
        return (currentPage < pageList.size() - 1);
    }
    
    boolean hasPrevPage() {
        return !history.isEmpty();
    }
}
