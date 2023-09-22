package me.vladislav.fs;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;
import me.vladislav.fs.operations.FileSystemOperations;

import java.io.Closeable;
import java.io.IOException;
import java.time.ZonedDateTime;

@Builder(toBuilder = true)
public class FileSystem implements Closeable {

    @Getter
    private final Metadata metadata;

    @Getter(onMethod = @__(@VisibleForTesting))
    private final AllocatedSpace allocatedSpace;

    private final FileSystemOperations fileSystemOperations;

    public AllocatedSpace getUsableAllocatedSpace() {
        return allocatedSpace.withStartOffset(Metadata.TOTAL_SIZE);
    }

    @Override
    public void close() throws IOException {
        allocatedSpace.close();
    }

    @Getter
    @Builder
    public static class Metadata {
        public static final int FAM_SIZE = 3;
        public static final int CREATED_AT_SIZE = 25;
        public static final int TOTAL_SIZE = CREATED_AT_SIZE + FAM_SIZE;

        private final ZonedDateTime createdAt;
        private final String fileAllocationMethod;
    }
}
