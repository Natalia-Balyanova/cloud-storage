package ru.balyanova.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedFile;
import lombok.extern.slf4j.Slf4j;
import ru.balyanova.Command;

import java.io.File;

@Slf4j
public class Net implements NetworkService{ //singleton

    private static Net INSTANCE;

//    private final Callback callback;
    private static SocketChannel channel;

//    public static Net getINSTANCE(Callback callback) {
//        if(INSTANCE == null) {
//            INSTANCE = new Net(callback);
//        }
//        return INSTANCE;
//    }

    private Net() {

    }

    public static Net initializeNet(Callback updateFilesListCallback) {
        Net net = new Net();
        initializeNetwork(updateFilesListCallback);
        return net;
    }



    private static void initializeNetwork(Callback updateFilesListCallback) {
        Thread thread = new Thread(() -> {
            EventLoopGroup group = new NioEventLoopGroup();

            try {
                Bootstrap bootstrap = new Bootstrap();

                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel c)  {
                                channel = c;
                                channel.pipeline().addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new ClientInboundCommandHandler(updateFilesListCallback)
                                );
//                                channel.pipeline().addLast(
//                                        new StringEncoder(),
//                                        new StringDecoder(),
//                                        new ClientStringHandler(callback)
//                                );
                            }
                        });
                ChannelFuture future = bootstrap.connect("localhost", 8189).sync();
                log.debug("Client connected... ");
                future.channel().closeFuture().sync(); //block
            }catch (Exception e) {
                log.error("", e);
            } finally {
                group.shutdownGracefully();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


//    private Net(Callback callback) {
//        this.callback = callback;
//    }



//    public void sendMessage(String msg) {
//        channel.writeAndFlush(msg);
//    }

    public void sendCommand(Command command) {
        channel.writeAndFlush(command);
        log.debug("Received command: " + command.getCommandType());
    }

    public void sendFile(String pathToFile) {
        try {
            ChannelFuture future = channel.writeAndFlush(new ChunkedFile(new File(pathToFile)));
            log.debug("Start ransfer file on the path " + pathToFile);
            future.addListener((ChannelFutureListener) channelFuture -> log.debug("File transferred"));
        } catch (Exception e) {
            log.error("Error: " + e);
        }
    }

    public boolean isConnected() {
        return channel != null && !channel.isShutdown();
    }

    public void closeConnection() {
        try {
            if(isConnected()) {
                channel.close().sync();
            }
        } catch (InterruptedException e) {
            log.error("Error " + e);
        }
    }
}
