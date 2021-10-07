package ru.balyanova.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import ru.balyanova.core.Command;
import ru.balyanova.core.CommandType;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FilesInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object chunkedFile) throws Exception {
        ByteBuf byteBuf = (ByteBuf) chunkedFile;
        log.debug("buf: {}", byteBuf);
        StringBuilder s = new StringBuilder();
        while (byteBuf.isReadable()) {
            s.append((char) byteBuf.readByte());
        }
        log.debug("recived: {}", s);
        ctx.fireChannelRead(s.toString());
    }
}

