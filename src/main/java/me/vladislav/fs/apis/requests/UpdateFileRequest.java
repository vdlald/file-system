package me.vladislav.fs.apis.requests;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;

import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

@Getter
@Builder(toBuilder = true)
public class UpdateFileRequest {

    /**
     * name of file to update
     */
    @Nonnull
    private final String filename;

    /**
     * content of file to update
     */
    @Nonnull
    private final SeekableByteChannel content;

    /**
     * where the file system is stored
     */
    @Nonnull
    private final Path fsPath;
}
