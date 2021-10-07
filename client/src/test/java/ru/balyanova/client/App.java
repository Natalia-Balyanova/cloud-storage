package ru.balyanova.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.balyanova.client.PanelController;

import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("cloud.fxml"));
        primaryStage.setScene(new Scene(parent, 800, 600));
        primaryStage.setTitle("   C L O U D    S T O R A G E   C L O U D    S T O R A G E");
        primaryStage.setResizable(true);
        primaryStage.show();
    }
}
