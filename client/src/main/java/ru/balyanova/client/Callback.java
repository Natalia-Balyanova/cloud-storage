package ru.balyanova.client;

import ru.balyanova.Command;

public interface Callback {
    void call(Command command);

}
