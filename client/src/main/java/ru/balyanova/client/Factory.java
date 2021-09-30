package ru.balyanova.client;

import java.util.Arrays;
import java.util.List;

public class Factory {
    public static NetworkService initializeNetworkService(Callback setButtonsAbleCallback) {
        return Net.initializeNet(setButtonsAbleCallback);
    }

    public static CommandDictionaryService getCommandDictionary() {
        return new ClientCommandDictionaryServiceImpl();
    }

    public static List<CommandService> getCommandServices() {
        return Arrays.asList(new CloudFilesListCommand(), new UploadFileCommand());
    }
}
