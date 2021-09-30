package ru.balyanova.client;

import ru.balyanova.Command;

public interface CommandService {
    void processCommand(Command command);
    String getCommand();
}
