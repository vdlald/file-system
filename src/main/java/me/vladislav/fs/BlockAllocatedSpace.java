package me.vladislav.fs;

import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Allows you to work with the allocated space in a per block manner
 */
public class BlockAllocatedSpace {

    /**
     * Number of available blocks
     */
    private final long blocksAmount;

    /**
     * Size of one block
     */
    @Nonnull
    private final BlockSize blockSize;

    @Nonnull
    private final AllocatedSpace allocatedSpace;

    public BlockAllocatedSpace(
            @Nonnull BlockSize blockSize,
            @Nonnull AllocatedSpace allocatedSpace
    ) throws IOException {
        this.blockSize = blockSize;
        this.allocatedSpace = allocatedSpace;
        blocksAmount = allocatedSpace.size() / blockSize.getBlockSizeInBytes();
    }

    @Nonnull
    public ByteBuffer readBlock(int blockIndex) throws IOException {
        int blockStart = blockStart(blockIndex);

        allocatedSpace.position(blockStart);

        ByteBuffer buffer = ByteBuffer.allocate(blockSize.getBlockSizeInBytes());
        allocatedSpace.read(buffer);

        return buffer;
    }

    public void writeBlock(int blockIndex, @Nonnull ByteBuffer data) throws IOException {
        if (data.array().length > blockSize.getBlockSizeInBytes()) {
            throw new RuntimeException();
        }

        allocatedSpace.position(blockStart(blockIndex));
        allocatedSpace.write(data);
    }

    private int blockStart(int blockIndex) {
        return blockIndex * blockSize.getBlockSizeInBytes();
    }
}
