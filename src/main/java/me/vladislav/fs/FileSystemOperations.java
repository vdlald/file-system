package me.vladislav.fs;

import java.io.ByteArrayOutputStream;

public interface FileSystemOperations {

    void createFile(CreateFileRequest createFileRequest);

    ByteArrayOutputStream readFile(String fileName);

    void updateFile(UpdateFileRequest updateFileRequest);

    // todo: Q-5 ?
    void deleteFile(String fileName);
}
