package ru.balyanova.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import ru.balyanova.core.*;

@Slf4j
public class PanelController implements Initializable {

    public ListView<String> clientsFiles;
    public ListView<String> cloudFilesOnServer;
    public TextField pathClientField;
    public TextField pathCloudField;
    public Button uploadButton;
    public Button downloadButton;
    private Path currentDir;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            currentDir = Paths.get("client", "root");

            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            updateClientView();
            addNavigationListeners();

            Thread daemon = new Thread(() -> {
                try {
                    while (true) {
                        Command command = (Command) is.readObject();
                        log.debug("received: {}", command);
                        switch (command.getType()) {
                            case LIST_RESPONSE:
                                ListResponse response = (ListResponse) command;
                                List<String> names = response.getNames();
                                updateServerView(names);
                                break;
                            case PATH_RESPONSE:
                                PathResponse pathResponse = (PathResponse) command;
                                String path = pathResponse.getPath();
                                Platform.runLater(() -> pathCloudField.setText(path));
                                break;
                            case FILE_MESSAGE:
                                FileMessage message = (FileMessage) command;
                                Files.write(currentDir.resolve(message.getName()), message.getBytes());
                                updateClientView();
                                break;
                        }
                    }
                } catch (Exception e) {
                    log.error("exception while read from input stream");
                }
            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException ioException) {
            log.error("ERROR: ", ioException);
        }
    }

    private void updateClientView() throws IOException {
        pathClientField.setText(currentDir.toString());
        List<String> names = Files.list(currentDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(() -> {
            clientsFiles.getItems().clear();
            clientsFiles.getItems().addAll(names);
        });
    }

    private void updateServerView(List<String> names) {
        Platform.runLater(() -> {
            cloudFilesOnServer.getItems().clear();
            cloudFilesOnServer.getItems().addAll(names);
        });
    }

    public void upload(ActionEvent actionEvent) {
        try {
            String fileName = clientsFiles.getSelectionModel().getSelectedItem();
            FileMessage message = new FileMessage(currentDir.resolve(fileName));
            os.writeObject(message);
            os.flush();
        }catch (Exception e) {
            log.debug(e + " .Click on Upload without file");
            Alert alert = new Alert(AlertType.INFORMATION, "File is not selected");
            alert.showAndWait();
        }
    }

    public void download(ActionEvent actionEvent) throws IOException{
            String fileName = cloudFilesOnServer.getSelectionModel().getSelectedItem();
            os.writeObject(new FileRequest(fileName));
            os.flush();
    }

    public void btnClientUP(ActionEvent actionEvent) {
        try {
            if (currentDir.getParent() != null) {
                currentDir = currentDir.getParent();
                pathClientField.setText(currentDir.toString());
                updateClientView();
            }
        } catch (IOException ioException) {
            log.debug("failed to update " + ioException);
        }
    }

    public void btnServerUP(ActionEvent actionEvent) throws IOException {
        os.writeObject(new PathUpRequest());
        os.flush();
    }

    private void addNavigationListeners() {
        clientsFiles.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = clientsFiles.getSelectionModel().getSelectedItem();
                Path newPath = currentDir.resolve(item);
                if (Files.isDirectory(newPath)) {
                    currentDir = newPath;
                    try {
                        updateClientView();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });

        cloudFilesOnServer.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = cloudFilesOnServer.getSelectionModel().getSelectedItem();
                try {
                    os.writeObject(new PathInRequest(item));
                    os.flush();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    public void btnExit(ActionEvent actionEvent) {
        Platform.exit();
    }
}
