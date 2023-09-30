package me.vladislav.fs.blocks;

import jakarta.annotation.Nonnull;
import lombok.*;

@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class FileDescriptor {

    public static final int FILENAME_SIZE = 52;
    public static final int FILE_SIZE = 8;
    public static final int FILE_BLOCK_INDEX_SIZE = 4;
    public static final int TOTAL_SIZE = FILENAME_SIZE + FILE_BLOCK_INDEX_SIZE + FILE_SIZE;

    @Setter
    @Getter
    @Nonnull
    private String filename;

    @Getter
    @Setter
    private long fileSize;

    @Getter
    private final int fileBlockIndex;

}
