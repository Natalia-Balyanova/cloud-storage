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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class PanelController implements Initializable {

    private static String ROOT_DIR = "client/Dir1";
    private static byte[] buffer = new byte[1024];

    @FXML
    TableView<FileInfo> filesTable;
    @FXML
    ComboBox<String> disksBox;
    @FXML
    TextField pathField;
    @FXML
    public ListView<String> listView;
    @FXML
    public TextField input;
    private DataInputStream is;
    private DataOutputStream os;

    public void send(ActionEvent actionEvent) throws Exception {
        String fileName = input.getText();
        input.clear();
        sendFile(fileName);
    }

    private void sendFile(String fileName) throws IOException {
        Path file = Paths.get(ROOT_DIR, fileName);
        if (Files.exists(file)) {
            long size = Files.size(file);
            os.writeUTF(fileName);
            os.writeLong(size);

            InputStream fileStream = Files.newInputStream(file);
            int read;
            while ((read = fileStream.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        }else {
            os.writeUTF(fileName);
            os.flush();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            fillFilesInCurrentDir();
            createFileList();
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Runnable target;
            Thread daemon = new Thread(() ->{
                try {
                    while (true) {
                        String msg = is.readUTF();
                        log.debug("received: {}", msg);
                        Platform.runLater(() -> input.setText(msg));
                    }
                }catch (Exception e) {
                    log.error("exception while read from input stream");
                }
            });
            daemon.setDaemon(true);
            daemon.start();
        } catch (IOException ioException) {
            log.error("e=", ioException);
        }
    }

    private void fillFilesInCurrentDir() throws IOException {
        listView.getItems().clear();
        listView.getItems().addAll(
                Files.list(Paths.get(ROOT_DIR))
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList())
        );
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = listView.getSelectionModel().getSelectedItem();
                input.setText(item);
            }
        });
    }

    public void createFileList() {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("File name");
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if(item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%, d bytes", item);
                        if(item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120);

        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Date");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(120);

        filesTable.getColumns().addAll(fileTypeColumn, filenameColumn, fileSizeColumn, fileDateColumn);
        filesTable.getSortOrder().add(fileTypeColumn);

        disksBox.getItems().clear();
        for(Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(0);

        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() == 2) {
                    Path path = Paths.get(pathField.getText()).resolve(filesTable.getSelectionModel().getSelectedItem().getFilename());
                    if(Files.isDirectory(path)) {
                        updateList(path);
                    }
                }
            }
        });

        updateList(Paths.get("."));

    }

    public void updateList(Path path) {
        try {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException ioException) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "failed to update", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnUP(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void selectDisk(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFilename() {
        if(!filesTable.isFocused()) {
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getCurrentPath() {
        return pathField.getText();
    }

    public void btnExit(ActionEvent actionEvent) {
        Platform.exit();
    }
}
