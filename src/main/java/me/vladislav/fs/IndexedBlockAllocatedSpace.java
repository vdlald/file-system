package me.vladislav.fs;

import me.vladislav.fs.util.ByteBufferUtils;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * Allows you to work with allocated space in block mode and maintains an index of the availability of these blocks
 */
public class IndexedBlockAllocatedSpace extends BlockAllocatedSpace {

    private final BitSet allocatedIndex;
    private int lastFreeBlockIndex = 0;

    public IndexedBlockAllocatedSpace(BlockSize blockSize, AllocatedSpace allocatedSpace) {
        super(blockSize, allocatedSpace);

        allocatedIndex = new BitSet(blocksAmount);
        for (int i = 0; i < blocksAmount; i++) {
            ByteBuffer block = readBlock(i);
            allocatedIndex.set(i, !ByteBufferUtils.isEmpty(block));
        }
    }

    public static IndexedBlockAllocatedSpace of(SeekableByteChannel file) {
        AllocatedSpace allocatedSpace = AllocatedSpace.builder()
                .data(file)
                .build();
        return new IndexedBlockAllocatedSpace(BlockSize.KB_4, allocatedSpace);
    }

    @Override
    public void writeBlock(int blockIndex, ByteBuffer data) {
        allocatedIndex.set(blockIndex, !ByteBufferUtils.isEmpty(data));
        super.writeBlock(blockIndex, data);
    }

    public void fillBlockZeros(int blockIndex) {
        allocatedIndex.set(blockIndex, false);
        super.fillBlockZeros(blockIndex);
    }

    public int getFreeBlockIndexAndMarkAsAllocated() {
        int freeBlockIndex = getFreeBlockIndex();
        markBlockAsAllocated(freeBlockIndex);
        return freeBlockIndex;
    }

    public int getFreeBlockIndex() {  // todo P-7: может дважды выдать blocksAmount
        int freeBlock = allocatedIndex.nextClearBit(lastFreeBlockIndex);

        if (freeBlock == -1) {
            freeBlock = allocatedIndex.previousClearBit(lastFreeBlockIndex);
        }

        if (freeBlock == -1) {
            freeBlock = blocksAmount;
        }

        lastFreeBlockIndex = freeBlock;

        return freeBlock;
    }

    public boolean isBlockFree(int blockIndex) {
        return !allocatedIndex.get(blockIndex);
    }

    public void markBlockAsAllocated(int blockIndex) {
        allocatedIndex.set(blockIndex, true);
    }

    @SuppressWarnings("unused")
    public IntStream getFreeBlocksIndexStream() {
        return IntStream.generate(this::getFreeBlockIndex);
    }
}
