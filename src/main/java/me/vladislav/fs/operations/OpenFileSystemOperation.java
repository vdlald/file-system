package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import me.vladislav.fs.FileSystem;

import java.nio.file.Path;

public interface OpenFileSystemOperation {
    @Nonnull
    FileSystem open(@Nonnull Path savePlace);
}
