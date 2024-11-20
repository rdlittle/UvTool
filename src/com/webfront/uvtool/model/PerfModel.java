/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.webfront.uvtool.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author rlittle
 */
public class PerfModel {

    /**
     * @return the otherItems
     */
    public ObservableList<OtherItem> getOtherItems() {
        return otherItems;
    }

    /**
     * @return the programs
     */
    public ObservableList<String> getPrograms() {
        return programs;
    }

    /**
     * @return the files
     */
    public ObservableList<String> getFiles() {
        return files;
    }

    public final ObservableList<OtherItem> otherItems;
    public final ObservableList<ActionItem> actionItems;
    private final ObservableList<String> programs;
    private final ObservableList<String> files;

    private String perfId;
    private String description;
    private String customer;
    private String module;
    private String action;
    private String person;
    private String date;
    private String memo;

    public PerfModel() {
        otherItems = FXCollections.observableArrayList();
        files = FXCollections.observableArrayList();
        programs = FXCollections.observableArrayList();
        actionItems = FXCollections.observableArrayList();
    }

    public void addOtherItem(String f, String n) {
        OtherItem item = new OtherItem();
        item.setFileName(f);
        item.setItemId(n);
        otherItems.add(item);
    }

    public void addActionItem(String a, String p, String d, String m) {
        ActionItem item = new ActionItem();
        item.setAction(a);
        item.setPerson(p);
        item.setDate(d);
        item.setMemo(m);
        actionItems.add(item);
    }

    /**
     * @return the perfId
     */
    public String getPerfId() {
        return perfId;
    }

    /**
     * @param perfId the perfId to set
     */
    public void setPerfId(String perfId) {
        this.perfId = perfId;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the customer
     */
    public String getCustomer() {
        return customer;
    }

    /**
     * @param customer the customer to set
     */
    public void setCustomer(String customer) {
        this.customer = customer;
    }

    /**
     * @return the module
     */
    public String getModule() {
        return module;
    }

    /**
     * @param module the module to set
     */
    public void setModule(String module) {
        this.module = module;
    }

    public class ActionItem {

        private StringProperty action;
        private StringProperty person;
        private StringProperty date;
        private StringProperty memo;

        public String getAction() {
            return actionProperty().get();
        }

        public void setAction(String value) {
            actionProperty().set(value);
        }

        public StringProperty actionProperty() {
            if (action == null) {
                action = new SimpleStringProperty(this, "action");
            }
            return action;
        }

        public StringProperty personProperty() {
            if (person == null) {
                person = new SimpleStringProperty(this, "Person");
            }
            return person;
        }

        public String getPerson() {
            return personProperty().get();
        }

        public void setPerson(String value) {
            personProperty().set(value);
        }

        public StringProperty dateProperty() {
            if (date == null) {
                date = new SimpleStringProperty(this, "Date");
            }
            return date;
        }

        public String getDate() {
            return dateProperty().get();
        }

        public void setDate(String value) {
            dateProperty().set(value);
        }

        public StringProperty memoProperty() {
            if (memo == null) {
                memo = new SimpleStringProperty(this, "Memo");
            }
            return person;
        }

        public String getMemo() {
            return memoProperty().get();
        }

        public void setMemo(String value) {
            memoProperty().set(value);
        }
    }

    public class OtherItem {

        private StringProperty fileName;
        private StringProperty itemId;

        public void setFileName(String value) {
            fileNameProperty().set(value);
        }

        public String getFileName() {
            //System.out.println(fileNameProperty().get());
            return fileNameProperty().get();
        }

        public StringProperty fileNameProperty() {
            if (fileName == null) {
                fileName = new SimpleStringProperty(this, "fileName");
            }
            return fileName;
        }

        public void setItemId(String value) {
            itemIdProperty().set(value);
        }

        public String getItemId() {
            return itemIdProperty().get();
        }

        public StringProperty itemIdProperty() {
            if (itemId == null) {
                itemId = new SimpleStringProperty(this, "itemId");
            }
            return itemId;
        }

    }

}
