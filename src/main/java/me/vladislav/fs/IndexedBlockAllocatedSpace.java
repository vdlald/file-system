package me.vladislav.fs;

import me.vladislav.fs.util.ByteBufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.BitSet;
import java.util.PrimitiveIterator;
import java.util.stream.IntStream;

/**
 * Allows you to work with allocated space in block mode and maintains an index of the availability of these blocks
 */
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

    public static IndexedBlockAllocatedSpace of(SeekableByteChannel file) throws IOException {
        AllocatedSpace allocatedSpace = AllocatedSpace.builder()
                .data(file)
                .build();
        return new IndexedBlockAllocatedSpace(BlockSize.KB_4, allocatedSpace);
    }

    @Override
    public void writeBlock(int blockIndex, ByteBuffer data) throws IOException {
        index.set(blockIndex, ByteBufferUtils.isEmpty(data));
        super.writeBlock(blockIndex, data);
    }

    public void fillBlockZeros(int blockIndex) throws IOException {
        index.set(blockIndex, true);
        super.fillBlockZeros(blockIndex);
    }

    public int getFreeBlockIndexAndMarkAsAllocated() {
        int freeBlockIndex = getFreeBlockIndex();
        markBlockAsAllocated(freeBlockIndex);
        return freeBlockIndex;
    }

    public int getFreeBlockIndex() {
        int freeBlock = index.nextSetBit(lastFreeBlockIndex);

        if (freeBlock == -1) {
            freeBlock = index.previousSetBit(lastFreeBlockIndex);
        }

        if (freeBlock == -1) {
            freeBlock = blocksAmount;
        }

        return freeBlock;
    }

    public boolean isBlockFree(int blockIndex) {
        return index.get(blockIndex);
    }

    public void markBlockAsAllocated(int blockIndex) {
        index.set(blockIndex, false);
    }

    public PrimitiveIterator.OfInt getFreeBlocksIndexIterator() {
        return getFreeBlocksIndexStream().iterator();
    }

    public IntStream getFreeBlocksIndexStream() {
        return IntStream.generate(this::getFreeBlockIndex);
    }
}
