package ru.balyanova.netty;

public class Factory {

    public static ServerService getServerService() {
        return (ServerService) new NettyEchoServer2();
    }

    public static CommandDictionaryService getCommandDictionaryService() {
        return (CommandDictionaryService) new CommandDictionaryServiceImpl();
    }

}
