package me.vladislav.fs.apis;

import jakarta.annotation.Nonnull;
import me.vladislav.fs.requests.*;
import org.springframework.stereotype.Component;

import java.nio.channels.SeekableByteChannel;

@Component
public class JavaApi implements ApplicationApi {

    public void createFileSystem(@Nonnull CreateFileSystemRequest request) {
        throw new RuntimeException("unsupported");
    }

    public void createFile(@Nonnull CreateFileRequest request) {
        throw new RuntimeException("unsupported");
    }

    public SeekableByteChannel readFile(@Nonnull ReadFileRequest request) {
        throw new RuntimeException("unsupported");
    }

    public void updateFile(@Nonnull UpdateFileRequest request) {
        throw new RuntimeException("unsupported");
    }

    public void deleteFile(@Nonnull DeleteFileRequest request) {
        throw new RuntimeException("unsupported");
    }
}
