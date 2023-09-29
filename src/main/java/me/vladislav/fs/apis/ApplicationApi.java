package me.vladislav.fs.apis;

import jakarta.annotation.Nonnull;
import me.vladislav.fs.apis.requests.*;

import java.nio.channels.SeekableByteChannel;
import java.util.List;

public interface ApplicationApi {

    void createFileSystem(@Nonnull CreateFileSystemRequest request);

    void createFile(@Nonnull CreateFileRequest request);

    SeekableByteChannel readFile(@Nonnull ReadFileRequest request);

    void updateFile(@Nonnull UpdateFileRequest request);

    void deleteFile(@Nonnull DeleteFileRequest request);

    List<String> listFiles(@Nonnull ListFilesRequest request);
}
