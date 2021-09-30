package ru.balyanova.netty;

import ru.balyanova.Command;

public interface CommandDictionaryService {
    Object processCommand(Command command);
}
