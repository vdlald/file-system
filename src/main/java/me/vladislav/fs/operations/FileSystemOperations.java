package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

public interface FileSystemOperations {

    void createFile(@Nonnull CreateFileRequest createFileRequest) throws IOException;

    @Nonnull
    SeekableByteChannel readFile(@Nonnull String fileName) throws IOException;

    void updateFile(@Nonnull UpdateFileRequest updateFileRequest);

    // todo: Q-5 ?
    void deleteFile(@Nonnull String fileName);
}
