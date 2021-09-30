package ru.balyanova.client;

import ru.balyanova.Command;
import ru.balyanova.CommandType;

public class UploadFileCommand implements CommandService {

    @Override
    public void processCommand(Command command) {
        ru.balyanova.client.PanelController currentController = (PanelController) App.getActiveController();
        currentController.sendFile((String) command.getArgs()[1]);
    }

    @Override
    public String getCommand() {
        return CommandType.READY_TO_UPLOAD.toString();
    }
}
