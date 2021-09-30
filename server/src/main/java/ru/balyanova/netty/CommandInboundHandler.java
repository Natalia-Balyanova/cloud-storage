package ru.balyanova.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.balyanova.Command;
import ru.balyanova.CommandType;

import java.util.Arrays;

@Slf4j
public class CommandInboundHandler extends SimpleChannelInboundHandler<Command> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        if(command.getCommandType().startsWith(CommandType.FILE_MESSAGE.toString())) {
            log.debug("From client on server received command FILE_MESSAGE with arguments " + Arrays.asList(command.getArgs()));
            ServerPipelineCheckout.createPipelineForInboundFilesReceiving(ctx, (String) command.getArgs()[0], (Long) command.getArgs()[3]);

        }
    }


}
