package ru.balyanova.client;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import ru.balyanova.Command;
import ru.balyanova.CommandType;
import ru.balyanova.FileInfo;

import java.io.*;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class PanelController implements Initializable {

    @FXML
    public TableView<FileInfo> clientsFiles;
    @FXML
    public TextField pathClientField;

    @FXML
    public TableView<FileInfo> cloudFilesOnServer;

    @FXML
    public TextField pathCloudField;

    @FXML
    Button uploadButton;
    @FXML
    Button downloadButton;

    private Net net;
    private NetworkService networkService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeNetworkService();
        makeClientTable();
        makeServerTable();
        createClientListFiles(Paths.get(ClientPropertiesReceiver.getClientDirectory()));
    }

    private void initializeNetworkService() {
        networkService = Factory.initializeNetworkService((s) -> {
            updateClientListFilesOnGUI(Paths.get(pathClientField.getText()));
        });
    }

    private void makeClientTable() {
        clientsFiles.getColumns().addAll(createFileTypeColumn(), createFileNAmeColumn(), createFileSizeColumn());
        moveIntoDirectory(clientsFiles, pathClientField);
    }

    private void makeServerTable() {
        cloudFilesOnServer.getColumns().addAll(createFileTypeColumn(), createFileNAmeColumn(), createFileSizeColumn());
    }

    public void createClientListFiles(Path path) {
        try {
            pathClientField.setText(path.normalize().toAbsolutePath().toString());
            clientsFiles.getItems().clear();
            clientsFiles.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            clientsFiles.sort();
        } catch (IOException e) {
            log.error("ERROR: " + e);
        }
    }

    private void updateClientListFilesOnGUI(Path path) {
        Platform.runLater(() -> createClientListFiles(path));
    }


    private void createServerListFiles(String pathToClientDirectory, List<FileInfo> listOfFiles) {
        pathCloudField.clear();
        pathCloudField.setText(pathToClientDirectory);
        cloudFilesOnServer.getItems().clear();
        cloudFilesOnServer.getItems().addAll(listOfFiles);
        cloudFilesOnServer.sort();
    }

    public void createServerListFilesOnGUI(String pathToClientDirectory, List<FileInfo> listOfFiles) {
        Platform.runLater(() -> createServerListFiles(pathToClientDirectory, listOfFiles));
    }

    public void sendCommand(Command command) {
        net.sendCommand(command);
    }

    private Command uploadCommand() {
        String absolutePathOfUploadFile = getcurrentPath(pathClientField) + "\\" + getSelectedFilename(clientsFiles);
        Long fileSize = clientsFiles.getSelectionModel().getSelectedItem().getSize();
        Object[] commandArgs = {getSelectedFilename(clientsFiles), absolutePathOfUploadFile, fileSize};
        Command command = new Command(CommandType.FILE_MESSAGE.toString(), commandArgs);
        return command;
    }

    public String getcurrentPath(TextField textField) {
        return textField.getText();
    }

    private void moveIntoDirectory(TableView<FileInfo> tableView, TextField textField) {
        tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Path currentPath = Paths.get(textField.getText());
                    Path newPath = currentPath.resolve(tableView.getSelectionModel().getSelectedItem().getFileName());
                    if (Files.isDirectory(newPath)) {
                        createClientListFiles(newPath);
                    }
                }
            }
        });
    }

    private TableColumn<FileInfo,Long> createFileSizeColumn() {
        TableColumn<FileInfo, Long> clientFileSizeColumn = new TableColumn<>("SIZE");
        clientFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        clientFileSizeColumn.setPrefWidth(120);

        clientFileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "DIR";
                        }
                        setText(text);
                    }
                }
            };
        });
        return clientFileSizeColumn;
    }

    private TableColumn<FileInfo,String> createFileNAmeColumn() {
        TableColumn<FileInfo, String> clientFileNameColumn = new TableColumn<>("NAME");
        clientFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        clientFileNameColumn.setPrefWidth(240);
        return clientFileNameColumn;
    }

    private TableColumn<FileInfo,String> createFileTypeColumn() {
        TableColumn<FileInfo, String> clientFileTypeColumn = new TableColumn<>("TYPE");
        clientFileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().getName()));
        clientFileTypeColumn.setPrefWidth(48);
        return clientFileTypeColumn;
    }

    public void updateList(Path path) {
        try {
            pathClientField.setText(path.normalize().toAbsolutePath().toString());
            clientsFiles.getItems().clear();
            clientsFiles.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            clientsFiles.sort();
        } catch (IOException ioException) {
            log.debug("failed to update " + ioException);
        }
    }

    public void btnUP(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathClientField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public String getSelectedFilename(TableView<FileInfo> clientsFiles) {
        if(!this.clientsFiles.isFocused()) {
            return null;
        }
        return this.clientsFiles.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath() {
        return pathClientField.getText();
    }

    public void download(ActionEvent actionEvent) {
        if (!networkService.isConnected()) {
            initializeNetworkService();
        } else {
            networkService.sendCommand(createDownloadCommand());
        }
    }

    private Command createDownloadCommand() {
        Long fileSize = cloudFilesOnServer.getSelectionModel().getSelectedItem().getSize();
        String userDirectoryForDownload = pathClientField.getText();
        Object[] commandArgs = {getSelectedFilename(cloudFilesOnServer), fileSize, userDirectoryForDownload};
        Command command = new Command(CommandType.DOWNLOAD.toString(), commandArgs);
        log.debug(getSelectedFilename(cloudFilesOnServer) + " " + fileSize + " " + userDirectoryForDownload);
        return command;
    }

    public void upload(ActionEvent actionEvent) {
        if (!networkService.isConnected()) {
            initializeNetworkService();
        }
        else {
            networkService.sendCommand(createUploadCommand());
        }
    }

    private Command createUploadCommand() {
        String absolutePathOfUploadFile = getcurrentPath(pathClientField) + "\\" + getSelectedFilename(clientsFiles);
        Long fileSize = clientsFiles.getSelectionModel().getSelectedItem().getSize();
        Object[] commandArgs = {getSelectedFilename(clientsFiles), absolutePathOfUploadFile, fileSize};
        Command command = new Command(CommandType.UPLOAD.toString(), commandArgs);
        log.debug(getSelectedFilename(clientsFiles) + " " + absolutePathOfUploadFile);
        return command;
    }

    public void btnExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void shutdown() {
        net.closeConnection();
    }

    public void sendFile(String absolutePathToUploadFile) {
        networkService.sendFile(absolutePathToUploadFile);
    }
}
