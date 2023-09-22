package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface FileSystemOperations {

    void createFile(@Nonnull CreateFileRequest createFileRequest) throws IOException;

    @Nonnull
    ByteBuffer readFile(@Nonnull String fileName);

    void updateFile(@Nonnull UpdateFileRequest updateFileRequest);

    // todo: Q-5 ?
    void deleteFile(@Nonnull String fileName);
}
