/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.webfront.uvtool.controller;

import asjava.uniclientlibs.UniDynArray;
import asjava.uniclientlibs.UniStringException;
import asjava.uniobjects.UniSession;
import asjava.uniobjects.UniSessionException;
import com.webfront.app.BaseApp;
import com.webfront.u2.model.Profile;
import com.webfront.util.UvConnection;
import com.webfront.uvtool.model.PerfModel;
import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author rlittle
 */
public class PerfTabController implements Initializable {

    @FXML
    Button btnSave;
    @FXML
    Button btnClose;
    @FXML
    Button btnProgAdd;
    @FXML
    Button btnProgDel;
    @FXML
    Button btnFileAdd;
    @FXML
    Button btnFileDel;
    @FXML
    Button btnItemAdd;
    @FXML
    Button btnItemDel;
    @FXML
    Button btnActionAdd;
    @FXML
    Button btnActionDel;
    @FXML
    TextArea txtDescription;
    @FXML
    TextField txtCustomer;
    @FXML
    TextField txtModule;
    @FXML
    ListView<String> listPrograms;
    @FXML
    ListView<String> listFiles;

    @FXML
    TableView<PerfModel.OtherItem> tblOtherItems;
    @FXML
    TableColumn<PerfModel.OtherItem, String> fileNameColumn;
    @FXML
    TableColumn<PerfModel.OtherItem, String> itemIdColumn;

    @FXML
    TableView<PerfModel.ActionItem> tblActionItems;
    @FXML
    TableColumn<PerfModel.ActionItem, String> actionColumn;
    @FXML
    TableColumn<PerfModel.ActionItem, String> personColumn;
    @FXML
    TableColumn<PerfModel.ActionItem, String> dateColumn;
    @FXML
    TableColumn<PerfModel.ActionItem, String> memoColumn;

    ResourceBundle res;
    UniDynArray record;
    protected UniDynArray copyOfRecord;
    Profile profile;
    String perfId;
    boolean isNew;
    final int CUST = 6;
    final int MODULE = 8;
    final int DESCRIPTION = 12;
    final int PROGRAMS = 13;
    final int FILES = 16;
    final int ACTION = 20;
    final int DATE = 21;
    final int PERSON = 22;
    final int MEMO = 30;
    final int OTHER_FILE = 102;
    final int OTHER_ITEM = 103;
    PerfModel model;
    boolean hasChanged;

    public PerfTabController() {
    }

    public void setProfile(Profile uv) {
        profile = uv;
    }

    public void setModel(String id, UniDynArray rec) {
        perfId = id;
        record = rec;
        copyOfRecord = new UniDynArray(rec);
        model = new PerfModel();

        fileNameColumn.setCellValueFactory(new PropertyValueFactory("fileName"));
        itemIdColumn.setCellValueFactory(new PropertyValueFactory("itemId"));

        tblOtherItems.getColumns().setAll(fileNameColumn, itemIdColumn);

        actionColumn.setCellValueFactory(new PropertyValueFactory("action"));
        personColumn.setCellValueFactory(new PropertyValueFactory("person"));
        dateColumn.setCellValueFactory(new PropertyValueFactory("date"));
        memoColumn.setCellValueFactory(new PropertyValueFactory("memo"));
        tblActionItems.getColumns().setAll(
                actionColumn, personColumn, dateColumn, memoColumn);

        model.setModule(record.extract(MODULE).toString());
        model.setCustomer(record.extract(CUST).toString());
        model.setDescription(record.extract(DESCRIPTION).toString());
        int fileCount = record.dcount(FILES);
        for (int f = 1; f <= fileCount; f++) {
            model.getFiles().add(record.extract(FILES, f).toString());
        }
        int programCount = record.dcount(PROGRAMS);
        for (int i = 1; i <= programCount; i++) {
            model.getPrograms().add(record.extract(PROGRAMS, i).toString());
        }
        int itemCount = record.dcount(102);
        for (int o = 1; o <= itemCount; o++) {
            String oFile = record.extract(OTHER_FILE, o).toString();
            String oItem = record.extract(OTHER_ITEM, o).toString();
            model.addOtherItem(oFile, oItem);
        }
        int actionCount = record.dcount(ACTION);
        UniSession session = openConnection();

        for (int a = 1; a <= actionCount; a++) {
            String act = record.extract(ACTION, a).toString();
            String date = record.extract(DATE, a).toString();
            String person = record.extract(PERSON, a).toString();
            String memo = record.extract(MEMO, a).toString();
            if (session.isActive()) {
                try {
                    date = session.oconv(date, "D4/").toString();
                } catch (UniStringException ex) {
                    Logger.getLogger(PerfTabController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            model.addActionItem(act, person, date, memo);
        }
        populateForm();
        if (session.isActive()) {
            try {
                session.disconnect();
            } catch (UniSessionException ex) {
                Logger.getLogger(PerfTabController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void populateForm() {
        txtCustomer.setText(model.getCustomer());
        txtModule.setText(model.getModule());
        txtDescription.setText(model.getDescription());
        listPrograms.setItems(model.getPrograms());
        listFiles.setItems(model.getFiles());
        tblOtherItems.setItems(model.otherItems);
        tblActionItems.setItems(model.actionItems);
    }

    public UniDynArray getRecord() {
        return record;
    }
    
    public void updateRecord() {
        record.replace(CUST, txtCustomer.getText());
        record.replace(MODULE, txtModule.getText());
        record.replace(DESCRIPTION, txtDescription.getText());
        int progCount = model.getPrograms().size();
        Iterator<String> it = model.getPrograms().iterator();
        int val = 0;
        while (it.hasNext()) {
            String progName = it.next();
            val++;
            record.replace(PROGRAMS, val++, progName);
        }
    }

    public boolean hasChanged() {
        return perfComparator.compare(record, copyOfRecord) != 0;
    }

    public UniDynArray getCopy() {
        return copyOfRecord;
    }
    
    public void setCopy() {
        copyOfRecord = new UniDynArray(record);
    }

    @FXML
    public void btnCloseOnClick() {
        System.out.println("Close button clicked!");
    }

    private UniSession openConnection() {
        try {
            UniSession session = UvConnection.newSession(profile);
            session.connect();
            return session;
        } catch (UniSessionException ex) {
            Logger.getLogger(BaseApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Button getCloseButton() {
        return btnClose;
    }
    
    public Button getSaveButton() {
        return btnSave;
    }

    public void setIsNew(boolean tf) {
        isNew = tf;
    }

    public static Comparator<UniDynArray> perfComparator = new Comparator<UniDynArray>() {
        @Override
        public int compare(UniDynArray rec1, UniDynArray rec2) {
            String rec1String = rec1.toString();
            String rec2String = rec2.toString();
            return rec1String.compareTo(rec2String);
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
