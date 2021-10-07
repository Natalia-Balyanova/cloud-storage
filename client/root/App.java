package ru.balyanova.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("panel.fxml"));
        Parent parent = loader.load();
        primaryStage.setScene(new Scene(parent, 800, 600));
        primaryStage.setTitle("     C L O U D    S T O R A G E");
        primaryStage.setResizable(true);
        primaryStage.show();
    }
}
