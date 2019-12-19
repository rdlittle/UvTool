/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.model;

import com.github.cliftonlabs.json_simple.JsonArray;
import java.util.Arrays;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.github.cliftonlabs.json_simple.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
/**
 *
 * @author rlittle
 */
public class PeerReviewModel {

    private final SimpleStringProperty id;
    private final SimpleStringProperty totalItems;
    private final SimpleStringProperty totalPending;
    private final SimpleStringProperty totalPassed;
    private final SimpleStringProperty totalFailed;
    private final SimpleStringProperty totalDictData;

    private final ObservableList<String> itemList;
    private final ObservableList<String> passedList;
    private final ObservableList<String> failedList;
    private final ObservableList<String> pendingList;
    private final ObservableList<String> dictDataList;

    private String[] rawData;
    private final String VM = new Character((char) 253).toString();
    private final String SVM = new Character((char) 252).toString();
    
    private final ArrayList<String> wrappersList;
    private final ArrayList<String> padsAppsList;
    private final ArrayList<String> padsProgramsList;
    private final ArrayList<String> webDeList;
    private final ArrayList<String> programsList;
    private final ArrayList<String> dictsList;
    private final ArrayList<String> dataList;
    private final ArrayList<String> missingList;
    private final HashMap<String, Integer> timeStamps;
    
    private static PeerReviewModel instance = null;

    protected PeerReviewModel() {
        id = new SimpleStringProperty();
        totalItems = new SimpleStringProperty("0");
        totalPending = new SimpleStringProperty("0");
        totalPassed = new SimpleStringProperty("0");
        totalFailed = new SimpleStringProperty("0");
        totalDictData = new SimpleStringProperty("0");

        itemList = FXCollections.observableArrayList();
        passedList = FXCollections.observableArrayList();
        failedList = FXCollections.observableArrayList();
        pendingList = FXCollections.observableArrayList();
        dictDataList = FXCollections.observableArrayList();
        
        padsAppsList = new ArrayList<>();
        padsProgramsList = new ArrayList<>();
        webDeList = new ArrayList<>();
        wrappersList = new ArrayList<>();
        programsList = new ArrayList<>();
        dictsList = new ArrayList<>();
        dataList = new ArrayList<>();
        missingList = new ArrayList<>();
        timeStamps = new HashMap<>();
    }
    
    public void init(String s) {
        this.rawData = s.split("\\n");
        decodeProject();
    }
    
    public static PeerReviewModel getInstance() {
        if (PeerReviewModel.instance == null) {
            PeerReviewModel.instance = new PeerReviewModel();
        }
        return PeerReviewModel.instance;
    }

    private String decodeMv(String mvString) {
        mvString = mvString.replaceAll(SVM, "~");
        mvString = mvString.replaceAll(" ", "~");
        mvString = mvString.replaceAll(":", "~");
        return mvString;
    }

    private void decodeProject() {
        id.set(this.rawData[0]);
        String[] wrappers = decodeMv(this.rawData[3]).split(VM);
        String[] padsPrograms = decodeMv(this.rawData[4]).split(VM);
        String[] padsApps = decodeMv(this.rawData[5]).split(VM);
        String[] webDe = decodeMv(this.rawData[6]).split(VM);
        String[] programs = decodeMv(this.rawData[7]).split(VM);
        String[] dicts = decodeMv(this.rawData[10]).split(VM);
        String[] data = decodeMv(this.rawData[11]).split(VM);

        if (wrappers.length > 1) {
            itemList.addAll(Arrays.asList(wrappers));
            wrappersList.addAll(Arrays.asList(wrappers));
        }
        if (padsPrograms.length > 1) {
            itemList.addAll(Arrays.asList(padsPrograms));
            padsProgramsList.addAll(Arrays.asList(padsPrograms));
        }
        if (padsApps.length > 1) {
            itemList.addAll(Arrays.asList(padsApps));
            padsAppsList.addAll(Arrays.asList(padsApps));
        }
        if(webDe.length > 1) {
            itemList.addAll(Arrays.asList(webDe));
            webDeList.addAll(Arrays.asList(webDe));
        }
        if(programs.length > 1) {
            itemList.addAll(Arrays.asList(programs));
            programsList.addAll(Arrays.asList(programs));
        }
        if(dicts.length > 1) {
            itemList.addAll(Arrays.asList(dicts));
            dictsList.addAll(Arrays.asList(dicts));
        }
        if (data.length > 1) {
            itemList.addAll(Arrays.asList(data));
            dataList.addAll(Arrays.asList(data));
        }
        totalItems.set(Integer.toString(itemList.size()));
    }

    /**
     * @return the id
     */
    public SimpleStringProperty getId() {
        return id;
    }

    /**
     * @return the totalItems
     */
    public SimpleStringProperty getTotalItems() {
        return totalItems;
    }

    /**
     * @return the totalPending
     */
    public SimpleStringProperty getTotalPending() {
        return totalPending;
    }

    /**
     * @return the totalPassed
     */
    public SimpleStringProperty getTotalPassed() {
        return totalPassed;
    }

    /**
     * @return the totalFailed
     */
    public SimpleStringProperty getTotalFailed() {
        return totalFailed;
    }

    /**
     * @return the totalDictData
     */
    public SimpleStringProperty getTotalDictData() {
        return totalDictData;
    }

    /**
     * @return the itemList
     */
    public ObservableList<String> getItemList() {
        return itemList;
    }

    /**
     * @return the passedList
     */
    public ObservableList<String> getPassedList() {
        return passedList;
    }

    /**
     * @return the failedList
     */
    public ObservableList<String> getFailedList() {
        return failedList;
    }

    /**
     * @return the pendingList
     */
    public ObservableList<String> getPendingList() {
        return pendingList;
    }

    /**
     * @return the dictDataList
     */
    public ObservableList<String> getDictDataList() {
        return dictDataList;
    }
    
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.put("id", id.getValue());
        json.put("item_count", totalItems.getValue());
        json.put("data", new JsonArray(dataList));
        json.put("dicts", new JsonArray(dictsList));        
        json.put("pads_programs", new JsonArray(padsProgramsList));                
        json.put("pads_apps", new JsonArray(padsAppsList));
        json.put("wrappers", new JsonArray(wrappersList));        
        json.put("failed", new JsonArray(failedList.sorted()));
        json.put("passed", new JsonArray(passedList.sorted()));
        json.put("webde", new JsonArray(webDeList));
        JsonObject jo = new JsonObject();
        jo.putAll(timeStamps);
        json.put("timestamps", jo);
        json.put("missing", new JsonArray().addAll(missingList));
        
        return json;
    }

}
