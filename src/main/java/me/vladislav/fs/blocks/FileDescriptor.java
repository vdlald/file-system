package me.vladislav.fs.blocks;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class FileDescriptor {

    public static final int FILENAME_SIZE = 60;
    public static final int FIRST_BLOCK_INDEX_SIZE = 4;
    public static final int TOTAL_SIZE = FILENAME_SIZE + FIRST_BLOCK_INDEX_SIZE;

    @Getter
    @Nonnull
    private final String filename;

    @Getter
    private final int firstBlockIndex;
}
