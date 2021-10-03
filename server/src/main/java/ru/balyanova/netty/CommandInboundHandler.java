package ru.balyanova.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.stream.ChunkedFile;
import lombok.extern.slf4j.Slf4j;
import ru.balyanova.core.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CommandInboundHandler extends SimpleChannelInboundHandler<Command> {

    private static Path currentPath;

    public CommandInboundHandler() throws IOException {
        currentPath = Paths.get("server", "root");
        if (!Files.exists(currentPath)) {
            Files.createDirectory(currentPath);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListResponse(currentPath));
        ctx.writeAndFlush(new PathResponse(currentPath.toString()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        log.debug("received: {}", command.getType() );
        switch(command.getType()) {
            case FILE_REQUEST:
                FileRequest fileRequest = (FileRequest) command;
                FileMessage msg = new FileMessage(currentPath.resolve(fileRequest.getName()));
                ctx.writeAndFlush(msg);
                break;
            case FILE_MESSAGE:
                FileMessage message = (FileMessage) command;
                Files.write(currentPath.resolve(message.getName()), message.getBytes());
                ctx.writeAndFlush(new ListResponse(currentPath));
                break;
            case LIST_REQUEST:
                ctx.writeAndFlush(new ListResponse(currentPath));
                break;
            case PATH_UP_REQUEST:
                if (currentPath.getParent() != null) {
                    currentPath = currentPath.getParent();
                }
                ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                ctx.writeAndFlush(new ListResponse(currentPath));
                break;
            case PATH_IN_REQUEST:
                PathInRequest request = (PathInRequest) command;
                Path newPath = currentPath.resolve(request.getDir());
                if (Files.isDirectory(newPath)) {
                    currentPath = newPath;
                    ctx.writeAndFlush(new PathResponse(currentPath.toString()));
                    ctx.writeAndFlush(new ListResponse(currentPath));
                }
                break;
        }

    }

//    private Object createListOfClientFilesInCloud(Command command) {
//        log.debug("From client FILESLIST");
//        //List<FileInfo> resultListOfFiles = (List<FileInfo>) dictionaryService.processCommand(command);
//        //String pathToClientDirectory = command.getArgs()[0] + ":\\";
//        String pathToClientDirectory = root.toString();
//        Object[] args = new Object[]{pathToClientDirectory, resultListOfFiles};
//        return new Command(CommandType.CLOUD_FILESLIST);
//    }

//    private Object createReadyToDownloadAccept(Command command) {
//        log.debug("DOWNLOAD " );
//        //Path pathToFile = ROOT.resolve((String) command.getArgs()[1]).resolve((String) command.getArgs()[0]);
//
//        Path pathToFile = currentPath;
//        String absolutePathOfDownloadFile = pathToFile.toString();
//
//        log.debug("READY_TO_DOWNLOAD " );
//        return new Command(CommandType.READY_TO_DOWNLOAD);
//    }




}
