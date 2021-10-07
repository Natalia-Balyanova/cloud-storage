package ru.balyanova.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.nio.charset.StandardCharsets;

public class FilesOutboundHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        String message = (String) msg;
        ByteBuf buf = ctx.alloc().buffer();;
        buf.writeCharSequence(message, StandardCharsets.UTF_8);
        buf.retain();

        ctx.writeAndFlush(buf);
    }
}
