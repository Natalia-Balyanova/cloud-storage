package ru.balyanova.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ServerPipelineCheckout {
    public static void createPipelineForInboundFilesReceiving(ChannelHandlerContext ctx, String fileName, Long fileSize) {
        ctx.pipeline().addLast(new ChunkedWriteHandler());
        ctx.pipeline().addLast(new FilesInboundHandler(fileName, fileSize));
        ctx.pipeline().remove(ObjectDecoder.class);
        ctx.pipeline().remove(CommandInboundHandler.class);
    }

    public static void createPipelineForOutboundFilesSending(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(ObjectEncoder.class);
        ctx.pipeline().addLast(new ChunkedWriteHandler());
    }

    public static void createBasePipelineAfterDownloadForInOutCommandTraffic(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(ChunkedWriteHandler.class);
        ctx.pipeline().remove(ObjectDecoder.class);
        ctx.pipeline().addFirst(new ObjectEncoder());
        ctx.pipeline().addFirst(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
    }

    public static void createBasePipelineAfterUploadForInOutCommandTraffic(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(ChunkedWriteHandler.class);
        ctx.pipeline().addFirst(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        ctx.pipeline().addLast(new CommandInboundHandler());
        ctx.pipeline().remove(FilesInboundHandler.class);
    }
}
