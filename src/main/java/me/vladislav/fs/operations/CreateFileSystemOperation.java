package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import me.vladislav.fs.FileSystem;
import me.vladislav.fs.requests.CreateFileSystemRequest;

public interface CreateFileSystemOperation {
    @Nonnull
    FileSystem createFileSystem(@Nonnull CreateFileSystemRequest request);
}
