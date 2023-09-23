package me.vladislav.fs;

import me.vladislav.fs.util.ByteBufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.stream.IntStream;

public class IndexedBlockAllocatedSpace extends BlockAllocatedSpace {

    private final BitSet index;
    private int lastFreeBlockIndex = 0;

    public IndexedBlockAllocatedSpace(BlockSize blockSize, AllocatedSpace allocatedSpace) throws IOException {
        super(blockSize, allocatedSpace);

        index = new BitSet(blocksAmount);
        index.set(0, blocksAmount, true);
        for (int i = 0; i < blocksAmount; i++) {
            ByteBuffer block = readBlock(i);
            if (!ByteBufferUtils.isEmpty(block)) {
                index.set(i, false);
            }
        }
    }

    @Override
    public void writeBlock(int blockIndex, ByteBuffer data) throws IOException {
        if (!ByteBufferUtils.isEmpty(data)) {
            index.set(blockIndex, false);
        }
        super.writeBlock(blockIndex, data);
    }

    public int getFirstFreeBlockIndex() {
        for (int i = lastFreeBlockIndex; i < index.size(); i++) {
            if (isBlockFree(i)) {
                lastFreeBlockIndex = i;
                return i;
            }
        }
        return -1;
    }

    public boolean isBlockFree(int blockIndex) {
        return index.get(blockIndex);
    }

    public void markBlockAsAllocated(int blockIndex) {
        index.set(blockIndex, false);
    }

    public IntStream getFreeBlocksIndexStream() {
        return IntStream.generate(this::getFirstFreeBlockIndex);
    }
}
