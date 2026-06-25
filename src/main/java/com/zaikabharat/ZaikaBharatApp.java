package com.zaikabharat;

import com.zaikabharat.db.DatabaseManager;
import com.zaikabharat.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ZaikaBharatApp extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseManager.initialize();

        MainWindow win = new MainWindow();
        Scene scene = new Scene(win.getRoot(), 1280, 800);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(
            getClass().getResource("/com/zaikabharat/css/glass.css").toExternalForm()
        );

        stage.setTitle("ZaikaBharat — Tales Woven in Flavors");
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseManager.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
