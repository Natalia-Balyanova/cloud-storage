package ru.balyanova.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.balyanova.Command;

@Slf4j
public class ClientInboundCommandHandler extends SimpleChannelInboundHandler<Command> {

    private final Callback callback;

    public ClientInboundCommandHandler(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        log.debug("received: {}", command);
        callback.call(command);
    }
}
