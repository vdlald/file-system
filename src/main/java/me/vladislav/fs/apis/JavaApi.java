package me.vladislav.fs.apis;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import me.vladislav.fs.FileSystem;
import me.vladislav.fs.apis.requests.*;
import me.vladislav.fs.operations.CreateFileSystemOperation;
import me.vladislav.fs.operations.OpenFileSystemOperation;
import org.springframework.stereotype.Component;

import java.nio.channels.SeekableByteChannel;

@Component
@RequiredArgsConstructor
public class JavaApi implements ApplicationApi {

    private final OpenFileSystemOperation openFileSystemOperation;
    private final CreateFileSystemOperation createFileSystemOperation;

    public void createFileSystem(@Nonnull CreateFileSystemRequest request) {
        createFileSystemOperation.createFileSystem(me.vladislav.fs.requests.CreateFileSystemRequest.builder()
                .initialSizeInBytes(request.getInitialSizeInBytes())
                .blockSize(request.getBlockSize())
                .whereToStore(request.getWhereToStore())
                .build());
    }

    public void createFile(@Nonnull CreateFileRequest request) {
        FileSystem fileSystem = openFileSystemOperation.open(request.getFsPath());
        fileSystem.getFileSystemOperations().createFile(me.vladislav.fs.requests.CreateFileRequest.builder()
                .filename(request.getFilename())
                .content(request.getContent())
                .build());
    }

    public SeekableByteChannel readFile(@Nonnull ReadFileRequest request) {
        try (FileSystem fileSystem = openFileSystemOperation.open(request.getFsPath())) {
            return fileSystem.getFileSystemOperations().readFile(request.getFilename());
        }
    }

    public void updateFile(@Nonnull UpdateFileRequest request) {
        try (FileSystem fileSystem = openFileSystemOperation.open(request.getFsPath())) {
            fileSystem.getFileSystemOperations().updateFile(me.vladislav.fs.requests.UpdateFileRequest.builder()
                    .filename(request.getFilename())
                    .content(request.getContent())
                    .build());
        }
    }

    public void deleteFile(@Nonnull DeleteFileRequest request) {
        try (FileSystem fileSystem = openFileSystemOperation.open(request.getFsPath())) {
            fileSystem.getFileSystemOperations().deleteFile(request.getFilename());
        }
    }
}
