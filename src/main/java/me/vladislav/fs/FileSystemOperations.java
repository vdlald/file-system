package me.vladislav.fs;

import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.CreateFileSystemRequest;
import me.vladislav.fs.requests.UpdateFileRequest;

import java.io.ByteArrayOutputStream;

public interface FileSystemOperations {

    void createFileSystem(CreateFileSystemRequest createFileSystemRequest);

    void createFile(CreateFileRequest createFileRequest);

    ByteArrayOutputStream readFile(String fileName);

    void updateFile(UpdateFileRequest updateFileRequest);

    // todo: Q-5 ?
    void deleteFile(String fileName);
}
