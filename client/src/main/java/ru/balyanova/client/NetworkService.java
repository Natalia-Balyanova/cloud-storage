package ru.balyanova.client;

import ru.balyanova.Command;

public interface NetworkService {
    void sendCommand (Command command);
    void closeConnection();
    boolean isConnected();
    void sendFile (String pathToFile);
}
