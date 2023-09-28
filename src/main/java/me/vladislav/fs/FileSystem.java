package me.vladislav.fs;

import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import me.vladislav.fs.operations.FileSystemOperations;

import java.io.Closeable;
import java.time.ZonedDateTime;

/**
 * Represents an open file system
 */
@Builder
public class FileSystem implements Closeable {

    /**
     * Metadata of the file system
     */
    @Getter
    @Nonnull
    private final Metadata metadata;

    /**
     * All available space in the file system
     */
    @Nonnull
    @Getter(onMethod = @__(@VisibleForTesting))
    private final AllocatedSpace allocatedSpace;

    /**
     * Operations available on this file system
     */
    @Getter
    @Nonnull
    private final FileSystemOperations fileSystemOperations;

    /**
     * Get the available space, which does not include file system metadata
     */
    public AllocatedSpace getUsableAllocatedSpace() {
        return allocatedSpace.withStartOffset(Metadata.TOTAL_SIZE);
    }

    @Override
    public void close() {
        allocatedSpace.close();
    }

    @Getter
    @Builder
    @ToString
    public static class Metadata {
        public static final int FAM_SIZE = 3;
        public static final int CREATED_AT_SIZE = 25;
        public static final int BLOCK_SIZE = 4;
        public static final int TOTAL_SIZE = CREATED_AT_SIZE + FAM_SIZE + BLOCK_SIZE;

        @Nonnull
        private final ZonedDateTime createdAt;

        @Nonnull
        private final String fileAllocationMethod;

        @Nonnull
        private final BlockSize blockSize;
    }
}
