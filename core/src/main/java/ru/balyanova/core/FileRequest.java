package ru.balyanova.core;

public class FileRequest extends Command{

    private final String name;

    public FileRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_REQUEST;
    }
}
