package ru.balyanova.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class Server {
    private ServerSocketChannel serverChannel;
    private Selector selector; //classified events
    private ByteBuffer buffer;
    private static Path ROOT = Paths.get("server-gb", "root");

    public Server() throws Exception {
        buffer = ByteBuffer.allocate(256);
        serverChannel = ServerSocketChannel.open();
        selector = Selector.open();
        serverChannel.bind(new InetSocketAddress(8189));
        log.debug("Server started... ");
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (serverChannel.isOpen()) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws Exception {
        SocketChannel channel = (SocketChannel) key.channel();
        buffer.clear();
        int read = 0;
        StringBuilder msg = new StringBuilder();
        while (true) {
            if (read == -1) { //разрыв соединения
                channel.close();
                log.debug("-1 Client disconnected!");
                return;
            }
            read = channel.read(buffer);
            if (read == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                msg.append((char) buffer.get());
            }
            buffer.clear();
        }
        String message = msg.toString().trim(); //обрезка
        log.debug("received: {}", message);

        if (message.equals("ls")) {
            channel.write(ByteBuffer.wrap(getFilesInfo().getBytes(StandardCharsets.UTF_8)));
        } else if (message.startsWith("cat")) {
            try {
                String fileName = message.split(" ")[1]; //нумерация с 0
                channel.write(ByteBuffer.wrap(getFileDataString(fileName).getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                channel.write(ByteBuffer.wrap("Command 'cat' should be have only two args or such file don't exists\n".getBytes(StandardCharsets.UTF_8)));
            }
        } else {
            channel.write(ByteBuffer.wrap("Wrong Command. Use 'cat fileName(.file's format)' or 'ls'\n".getBytes(StandardCharsets.UTF_8)));
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        log.debug("Client accepted... ");
    }

    private String getFileDataString(String fileName) throws IOException {
        if (Files.isDirectory(ROOT.resolve(fileName))) {
            return "[ERROR] Command 'cat' can not be applied to " + fileName + "\n";
        } else {
            return new String(Files.readAllBytes(ROOT.resolve(fileName))) + "\n";
        }
    }

    private String getFilesInfo() throws Exception {
        return Files.list(ROOT).map(this::resolveFileType).collect(Collectors.joining("\n")) + "\n";
    }

    private String resolveFileType(Path path) {
        if (Files.isDirectory(path)) {
            return String.format("%s%s\n", path.getFileName().toString(), "[DIR]");
        } else {
            return String.format("%s%s\n", path.getFileName().toString(), "[FILE]");
        }
    }

    public static void main(String[] args) throws Exception {
        new Server();
    }
}
