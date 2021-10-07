package ru.balyanova.netty;

import java.util.Arrays;
import java.util.List;

public class Factory {

    public static ServerService getServerService() {
        return new NettyEchoServer2();
    }
}
