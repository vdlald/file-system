package me.vladislav.fs.operations;

import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface FileSystemOperations {

    void createFile(CreateFileRequest createFileRequest) throws IOException;

    ByteBuffer readFile(String fileName);

    void updateFile(UpdateFileRequest updateFileRequest);

    // todo: Q-5 ?
    void deleteFile(String fileName);
}
