/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.uvtool.app;

import com.webfront.uvtool.controller.UvToolController;
import com.webfront.uvtool.util.Config;
import java.awt.AWTException;
import java.awt.Font;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;

/**
 *
 * @author rlittle
 */
public class UvTool extends Application {

    private final String fxml = "/com/webfront/uvtool/fxml/UvToolView.fxml";
    private final String propertyString = "com.webfront.uvtool.util.UvTool";
    private static final String iconLoc = "image/U2_24x24.png";
    private Config config;
    Scene scene;
    Stage stage;
    SystemTray sysTray;
    TrayIcon trayIcon;
    ResourceBundle res;

    @Override
    public void start(Stage stg) throws Exception {
        config = Config.getInstance();
        stage = stg;
        Platform.setImplicitExit(false);
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(UvTool.class.getResource(fxml));
        res = ResourceBundle.getBundle(propertyString, Locale.getDefault());
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
        toggleStage();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void toggleStage() {
        if (stage != null) {
            if (stage.isShowing()) {
                Platform.runLater(() -> stage.hide());
            } else {
                Platform.runLater(() -> {
                    stage.show();
                    stage.toFront();
                });
            }
        }
    }

    private class WindowHandler implements EventHandler {

        @Override
        public void handle(Event event) {
            config.setWindowLocation((int) scene.getWindow().getX(), (int) scene.getWindow().getY());
            config.setWindowSize((int) scene.getWindow().getWidth(), (int) scene.getWindow().getHeight());
            config.setConfig();
            config.shutdown();
            sysTray.remove(trayIcon);
            Platform.exit();
        }
    }

    public void addAppToTray() {
        Toolkit.getDefaultToolkit();
        if (!SystemTray.isSupported()) {
            return;
        }
        try {
            sysTray = SystemTray.getSystemTray();
            InputStream is = UvTool.class.getResourceAsStream("/U2_24x24.png");
            Image image = ImageIO.read(is);
            trayIcon = new TrayIcon(image);
            trayIcon.addMouseListener(new MouseListener() {
                @Override
                public void mouseExited(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    e.consume();
                    toggleStage();
                }
            });

            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
//                config.setWindowLocation((int) scene.getWindow().getX(), (int) scene.getWindow().getY());
//                config.setWindowSize((int) scene.getWindow().getWidth(), (int) scene.getWindow().getHeight());
//                config.setConfig();
//                config.shutdown();
//                Platform.exit();
                sysTray.remove(trayIcon);
                Platform.runLater(()->stage.fireEvent(new Event(WindowEvent.WINDOW_CLOSE_REQUEST)));
            });

            java.awt.PopupMenu popup = new java.awt.PopupMenu();
            Font defaultFont = Font.decode(null);
            Font boldFont = defaultFont.deriveFont(Font.BOLD,12);
            exitItem.setFont(boldFont);
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
            sysTray.add(trayIcon);

        } catch (MalformedURLException ex) {
            Logger.getLogger(UvTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UvTool.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AWTException ex) {
            Logger.getLogger(UvTool.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
