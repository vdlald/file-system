package me.vladislav.fs.apis;

import jakarta.annotation.Nonnull;
import me.vladislav.fs.requests.*;

import java.nio.channels.SeekableByteChannel;

public interface ApplicationApi {

    void createFileSystem(@Nonnull CreateFileSystemRequest request);

    void createFile(@Nonnull CreateFileRequest request);

    SeekableByteChannel readFile(@Nonnull ReadFileRequest request);

    void updateFile(@Nonnull UpdateFileRequest request);

    void deleteFile(@Nonnull DeleteFileRequest request);
}
