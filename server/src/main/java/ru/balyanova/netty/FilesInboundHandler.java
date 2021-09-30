package ru.balyanova.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import ru.balyanova.Command;
import ru.balyanova.CommandType;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FilesInboundHandler extends ChannelInboundHandlerAdapter {

    private static final Path ROOT = Paths.get("server", "root");

    private final String fileName;
    private final Long fileSize;

    public FilesInboundHandler(String fileName, Long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object chunkedFile) throws Exception {
        ByteBuf byteBuf = (ByteBuf) chunkedFile;
        File newFile = new File(fileName);
        newFile.createNewFile();
        log.debug("Create file " + fileName);
        wrightNewFileContent(fileName, byteBuf);
        createAnswerSuccessUpload(newFile, ctx);
    }

    private void wrightNewFileContent(String absoluteFileName, ByteBuf byteBuf) throws FileNotFoundException {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(absoluteFileName, true))) {
            while (byteBuf.isReadable()) {
                out.write(byteBuf.readByte());
            }
            byteBuf.release();
        } catch (IOException e) {
            log.error("ERROR: " + e);
        }
    }

    private void createAnswerSuccessUpload(File newFile, ChannelHandlerContext ctx) {
        if (newFile.length() == fileSize) {
            log.debug("File is read");
            ServerPipelineCheckout.createBasePipelineAfterUploadForInOutCommandTraffic(ctx);

            String[] args = {fileName};
            ctx.writeAndFlush(new Command(CommandType.LIST_RESPONSE.toString(), args));
            log.debug("На клиент с сервера отправлена команда UPLOAD_FINISHED с аргументами " + args);
        }
    }


}
