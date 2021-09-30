package ru.balyanova.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Initializable activeController;

    public static Initializable getActiveController() { // геттер, чтобы к контроллеру обращаться.
        return activeController;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/panel.fxml"));
        Parent parent = loader.load();
        primaryStage.setScene(new Scene(parent, 800, 600));
        primaryStage.setTitle("     C L O U D    S T O R A G E");
        primaryStage.setResizable(true);

        PanelController controller = loader.getController();
        activeController = controller;
        primaryStage.setOnCloseRequest((event) -> controller.shutdown());
        primaryStage.show();
    }
}
