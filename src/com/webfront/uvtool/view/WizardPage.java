/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.view;

import com.webfront.uvtool.app.UvTool;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

/**
 *
 * @author rlittle
 */
public abstract class WizardPage extends VBox implements Initializable {
    private FXMLLoader loader;
    private URL location;
    private ResourceBundle resources;
    
    private String view;
    
    public WizardPage(String v) {
        view = v;
    }

    @Override
    public void initialize(URL loc, ResourceBundle rb) {
        location = loc;
        resources = rb;
        loader = new FXMLLoader();
        loader.setLocation(UvTool.class.getResource(resources.getString(view)));
        loader.setResources(resources);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(WizardPage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
