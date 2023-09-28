package me.vladislav.fs.apis.requests;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

// todo: можно менять контент из вне
@With
@Getter
@Builder(toBuilder = true)
public class CreateFileRequest {

    /**
     * name of file to create
     */
    @Nonnull
    private final String filename;

    /**
     * content of file to create
     */
    @Nonnull
    private final SeekableByteChannel content;

    /**
     * where the file system is stored
     */
    @Nonnull
    private final Path fsPath;

}
