package ru.balyanova;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Setter
public class FileInfo implements Serializable {
    public FileType getFileType() {
        return type;
    }

    public enum FileType implements Serializable {
        FILE("F"), DIRECTORY("D");

        private String name;

        public String getName() {
            return name;
        }

        FileType(String name) {
            this.name = name;
        }
    }

    private String fileName;
    private FileType type;
    private long size;

    public FileInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString();    //преобразуем к строке имя файла
            this.size = Files.size(path);
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if(this.type == FileType.DIRECTORY) {
                this.size = -1L;
            }
        } catch (IOException ioException) {
            throw new RuntimeException("Unable to create file info from path");
        }
    }
}
