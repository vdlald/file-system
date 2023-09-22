package me.vladislav.fs.requests;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@Builder(toBuilder = true)
public class CreateFileSystemRequest {

    /**
     * Where to create a file system
     */
    @Nonnull
    private final Path whereToStore;

    /**
     * How to name a file system
     */
    @Nonnull
    private final String fileSystemName;

    /**
     * How much space to allocate to the file system beforehand
     */
    private final int initialSizeInBytes;
}
