package me.vladislav.fs.requests;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import me.vladislav.fs.BlockSize;

import java.nio.file.Path;

@With
@Getter
@Builder(toBuilder = true)
public class CreateFileSystemRequest {

    /**
     * Where to create a file system
     */
    @Nonnull
    private final Path whereToStore;

    /**
     * How much space to allocate to the file system beforehand
     */
    @Builder.Default
    private final int initialSizeInBytes = BlockSize.KB_4.getBlockSizeInBytes();

    @Builder.Default
    private final BlockSize blockSize = BlockSize.KB_4;
}
