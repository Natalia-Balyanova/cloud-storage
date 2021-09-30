package ru.balyanova.client;

import ru.balyanova.Command;
import ru.balyanova.CommandType;
import ru.balyanova.FileInfo;

import java.util.List;

public class CloudFilesListCommand implements CommandService{

    @Override
    public void processCommand(Command command) {
        PanelController currentController = (PanelController) App.getActiveController();

        currentController.createServerListFilesOnGUI((String) command.getArgs()[0], (List<FileInfo>) command.getArgs()[1]);
    }

    @Override
    public String getCommand() {
        return CommandType.CLOUD_FILESLIST.toString();
    }
}
