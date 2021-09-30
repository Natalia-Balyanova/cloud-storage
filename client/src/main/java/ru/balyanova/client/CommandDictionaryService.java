package ru.balyanova.client;

import ru.balyanova.Command;

public interface CommandDictionaryService {
    void processCommand(Command command);
}
