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
    private final String AM = new Character((char) 254).toString();
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
    private final ArrayList<String> inProgressList;
    private final HashMap<String, Integer> timeStamps;
    private final HashMap<String, ArrayList> allPrograms;
    private final HashMap<String, ArrayList> allData;

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
        inProgressList = new ArrayList<>();
        timeStamps = new HashMap<>();
        allPrograms = new HashMap<>();
        allData = new HashMap<>();
    }

    public void init(String s) {
        this.rawData = s.split("\n");
        getAllPrograms().clear();
        getAllData().clear();
        decodeProject();
    }

    public static PeerReviewModel getInstance() {
        if (PeerReviewModel.instance == null) {
            PeerReviewModel.instance = new PeerReviewModel();
        }
        return PeerReviewModel.instance;
    }

    public void clear() {
        getAllPrograms().clear();
        getAllData().clear();
        getId().set("");
        getTotalItems().set("0");
        getTotalPending().set("0");
        getTotalPassed().set("0");
        getTotalFailed().set("0");
        getTotalDictData().set("0");
        getItemList().clear();
        getPassedList().clear();
        getFailedList().clear();
        getPendingList().clear();
        getWrappersList().clear();
        getPadsAppsList().clear();
        getPadsProgramsList().clear();
        getWebDeList().clear();
        getProgramsList().clear();
        getDictsList().clear();
        getDataList().clear();
        getMissingList().clear();
        getInProgressList().clear();
        getTimeStamps().clear();
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

        if (wrappers.length > 0) {
            if (!wrappers[0].isEmpty()) {
                itemList.addAll(Arrays.asList(wrappers));
            }
            getWrappersList().addAll(Arrays.asList(wrappers));
            getAllPrograms().put("wrappers", getWrappersList());
        }
        if (padsPrograms.length > 0) {
            if (!padsPrograms[0].isEmpty()) {
                itemList.addAll(Arrays.asList(padsPrograms));
            }
            getPadsProgramsList().addAll(Arrays.asList(padsPrograms));
            getAllPrograms().put("pads", getPadsProgramsList());
        }
        if (padsApps.length > 0) {
            if (!padsApps[0].isEmpty()) {
                itemList.addAll(Arrays.asList(padsApps));
            }
            getPadsAppsList().addAll(Arrays.asList(padsApps));
            getAllData().put("padsApps", getPadsAppsList());
            dictDataList.addAll(getPadsAppsList());
        }
        if (webDe.length > 0) {
            if (!webDe[0].isEmpty()) {
                itemList.addAll(Arrays.asList(webDe));
            }
            getWebDeList().addAll(Arrays.asList(webDe));
            getAllPrograms().put("webde", getWebDeList());
        }
        if (programs.length > 0) {
            if (!programs[0].isEmpty()) {
                itemList.addAll(Arrays.asList(programs));
            }
            getProgramsList().addAll(Arrays.asList(programs));
            getAllPrograms().put("programs", getProgramsList());
        }
        if (dicts.length > 0) {
            if (!dicts[0].isEmpty()) {
                itemList.addAll(Arrays.asList(dicts));
            }
            getDictsList().addAll(Arrays.asList(dicts));
            getAllData().put("dicts", getDictsList());
            dictDataList.addAll(getDictsList());
        }
        if (data.length > 0) {
            if (!data[0].isEmpty()) {
                itemList.addAll(Arrays.asList(data));
            }
            getDataList().addAll(Arrays.asList(data));
            getAllData().put("data", getDataList());
            dictDataList.addAll(getDataList());
        }

        totalItems.set(Integer.toString(itemList.size()));
        totalDictData.set(Integer.toString(dictDataList.size()));
    }

    /**
     * @return the id
     */
    public SimpleStringProperty getId() {
        return id;
    }

    /**
     * @return the timeStamps
     */
    public HashMap<String, Integer> getTimeStamps() {
        return timeStamps;
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
        json.put("data", new JsonArray(getDataList()));
        json.put("dicts", new JsonArray(getDictsList()));
        json.put("pads_programs", new JsonArray(getPadsProgramsList()));
        json.put("pads_apps", new JsonArray(getPadsAppsList()));
        json.put("wrappers", new JsonArray(getWrappersList()));
        json.put("failed", new JsonArray(failedList.sorted()));
        json.put("passed", new JsonArray(passedList.sorted()));
        json.put("webde", new JsonArray(getWebDeList()));
        JsonObject ts = new JsonObject();
        ts.putAll(getTimeStamps());
        json.put("timestamps", ts);
        json.put("in_progress", new JsonArray(getInProgressList()));
        json.put("missing", new JsonArray(getMissingList()));

        return json;
    }

    /**
     * @return the allPrograms
     */
    public HashMap<String, ArrayList> getAllPrograms() {
        return allPrograms;
    }

    /**
     * @return the allData
     */
    public HashMap<String, ArrayList> getAllData() {
        return allData;
    }

    /**
     * @return the wrappersList
     */
    public ArrayList<String> getWrappersList() {
        return wrappersList;
    }

    /**
     * @return the padsAppsList
     */
    public ArrayList<String> getPadsAppsList() {
        return padsAppsList;
    }

    /**
     * @return the webDeList
     */
    public ArrayList<String> getWebDeList() {
        return webDeList;
    }

    /**
     * @return the programsList
     */
    public ArrayList<String> getProgramsList() {
        return programsList;
    }

    /**
     * @return the dictsList
     */
    public ArrayList<String> getDictsList() {
        return dictsList;
    }

    /**
     * @return the dataList
     */
    public ArrayList<String> getDataList() {
        return dataList;
    }

    /**
     * @return the missingList
     */
    public ArrayList<String> getMissingList() {
        return missingList;
    }

    /**
     * @return the inProgressList
     */
    public ArrayList<String> getInProgressList() {
        return inProgressList;
    }

    /**
     * @return the padsProgramsList
     */
    public ArrayList<String> getPadsProgramsList() {
        return padsProgramsList;
    }

}