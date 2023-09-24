package me.vladislav.fs;

import jakarta.annotation.Nonnull;
import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.List;

/**
 * Allows you to work with the allocated space in a per block manner
 */
public class BlockAllocatedSpace {

    /**
     * Number of available blocks
     */
    @Getter
    protected int blocksAmount;

    /**
     * Size of one block
     */
    @Getter
    @Nonnull
    protected final BlockSize blockSize;

    @Nonnull
    private final AllocatedSpace allocatedSpace;

    public BlockAllocatedSpace(
            @Nonnull BlockSize blockSize,
            @Nonnull AllocatedSpace allocatedSpace
    ) throws IOException {
        this.blockSize = blockSize;
        this.allocatedSpace = allocatedSpace;
        blocksAmount = (int) (allocatedSpace.size() / blockSize.getBlockSizeInBytes());
    }

    public static BlockAllocatedSpace of(SeekableByteChannel channel) throws IOException {
        AllocatedSpace allocatedSpace = AllocatedSpace.builder()
                .data(channel)
                .build();
        return new BlockAllocatedSpace(BlockSize.KB_4, allocatedSpace);
    }

    @Nonnull
    public ByteBuffer readBlock(int blockIndex) throws IOException {
        return allocatedSpace.position(blockStart(blockIndex))
                .read(blockSize.getBlockSizeInBytes());
    }

    @Nonnull
    public ByteBuffer readBlocks(List<Integer> blockIndexes) throws IOException {
        ByteBuffer allocate = ByteBuffer.allocate(blockIndexes.size() * blockSize.getBlockSizeInBytes()); //
        for (Integer blockIndex : blockIndexes) {
            allocate.put(readBlock(blockIndex));
        }
        return allocate.rewind();
    }

    public ByteBuffer readBlock() throws IOException {
        return allocatedSpace.read(blockSize.getBlockSizeInBytes());
    }

    protected void fillBlockZeros(int blockIndex) throws IOException {
        writeBlock(blockIndex, ByteBuffer.allocate(blockSize.getBlockSizeInBytes()));
    }

    public void writeBlock(int blockIndex, @Nonnull ByteBuffer data) throws IOException {
        if (data.array().length > blockSize.getBlockSizeInBytes()) {
            throw new RuntimeException();
        }

        allocatedSpace.position(blockStart(blockIndex))
                .write(data);
    }

    public boolean containsNextBlock() throws IOException {
        return !allocatedSpace.isCurrentPositionMoreOrEqualsSizeOfChannel();
    }

    public BlockAllocatedSpace block(int blockIndex) throws IOException {
        allocatedSpace.position(blockStart(blockIndex));
        return this;
    }

    public long size() throws IOException {
        return allocatedSpace.size();
    }

    private int blockStart(int blockIndex) {
        return blockIndex * blockSize.getBlockSizeInBytes();
    }
}
