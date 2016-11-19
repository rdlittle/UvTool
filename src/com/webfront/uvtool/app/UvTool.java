/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.app;

import com.webfront.uvtool.controller.UvToolController;
import com.webfront.uvtool.util.Config;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author rlittle
 */
public class UvTool extends Application {
    private final String fxml = "/com/webfront/uvtool/fxml/UvToolView.fxml";
    private final String propertyString = "com.webfront.uvtool.util.UvTool";
    private Config config;
    Scene scene;
    ResourceBundle res;
    
    @Override
    public void start(Stage stage) throws Exception {
        config = Config.getInstance();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(UvTool.class.getResource(fxml));
        res = ResourceBundle.getBundle(propertyString,Locale.getDefault());
        loader.setResources(res);
        AnchorPane root = loader.<AnchorPane>load();
        UvToolController controller = loader.getController();
        scene = new Scene(root);
        controller.getFileExit().setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                stage.fireEvent(new Event(WindowEvent.WINDOW_CLOSE_REQUEST));
            }
        });
        stage.setTitle(res.getString("titleUvTool"));
        stage.setX((double) config.getWindowLocation().x);
        stage.setY((double) config.getWindowLocation().y);
        stage.setScene(scene);
        stage.setOnCloseRequest(new WindowHandler());
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    private class WindowHandler implements EventHandler {

        @Override
        public void handle(Event event) {
            config.setWindowLocation((int)scene.getWindow().getX(), (int)scene.getWindow().getY());
            config.setWindowSize((int)scene.getWindow().getWidth(), (int)scene.getWindow().getHeight());
            config.setConfig();
            config.shutdown();
            Platform.exit();
        }
    }
    
}