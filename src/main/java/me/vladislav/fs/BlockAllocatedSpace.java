package me.vladislav.fs;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BlockAllocatedSpace {

    private final long blocksAmount;
    private final BlockSize blockSize;
    private final AllocatedSpace allocatedSpace;

    public BlockAllocatedSpace(BlockSize blockSize, AllocatedSpace allocatedSpace) throws IOException {
        this.blockSize = blockSize;
        this.allocatedSpace = allocatedSpace;
        blocksAmount = allocatedSpace.size() / blockSize.getBlockSizeInBytes();
    }

    public ByteBuffer readBlock(int blockIndex) throws IOException {
        int blockStart = blockStart(blockIndex);

        allocatedSpace.position(blockStart);

        ByteBuffer buffer = ByteBuffer.allocate(blockSize.getBlockSizeInBytes());
        allocatedSpace.read(buffer);

        return buffer;
    }

    public void writeBlock(int blockIndex, ByteBuffer data) throws IOException {
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
