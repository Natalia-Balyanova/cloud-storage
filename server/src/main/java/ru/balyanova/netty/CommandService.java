package ru.balyanova.netty;

import ru.balyanova.Command;

public interface CommandService {
    Object processCommand(Command command);

    String getCommand();
}
