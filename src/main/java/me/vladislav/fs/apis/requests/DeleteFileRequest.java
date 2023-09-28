package me.vladislav.fs.apis.requests;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

@Getter
@Builder
public class DeleteFileRequest {

    /**
     * name of file in fs
     */
    @Nonnull
    private final String filename;

    /**
     * where the file system is stored
     */
    @Nonnull
    private final Path fsPath;

}
