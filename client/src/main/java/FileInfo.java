import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
public class FileInfo {
    public enum FileType {
        FILE("F"), DIRECTORY("D");

        private String name;

        public String getName() {
            return name;
        }

        FileType(String name) {
            this.name = name;
        }
    }

        private String filename;
        private FileType type;
        private long size;
        private LocalDateTime lastModified;

        public FileInfo(Path path) {
            try {
                this.filename = path.getFileName().toString();    //преобразуем к строке имя файла
                this.size = Files.size(path);
                this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
                if(this.type == FileType.DIRECTORY) {
                    this.size = -1L;
                }
                this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(0));
            } catch (IOException ioException) {
                throw new RuntimeException("Unable to create file info from path");
            }
        }
    }