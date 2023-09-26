package me.vladislav.fs.blocks.components;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.IndexedBlockAllocatedSpace;
import me.vladislav.fs.blocks.FileContentIndexBlock;
import me.vladislav.fs.blocks.serializers.FileContentIndexBlockBytesSerializer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

@Slf4j
@Component
@Scope("prototype")
public class ChainedFileContentIndexBlock implements Closeable {

    @Getter
    private final int firstBlockIndex;

    @Nonnull
    private final FileContentIndexBlock firstBlock;

    private int currentBlockIndex;

    @Nonnull
    private FileContentIndexBlock currentBlock;

    @Nonnull
    private final IndexedBlockAllocatedSpace allocatedSpace;

    @Nonnull
    private final FileContentIndexBlockBytesSerializer indexBlockSerializer;

    @SuppressWarnings("all")
    public ChainedFileContentIndexBlock(
            int firstBlockIndex,
            FileContentIndexBlock firstBlock,
            IndexedBlockAllocatedSpace allocatedSpace,
            FileContentIndexBlockBytesSerializer indexBlockSerializer
    ) {
        this.firstBlock = firstBlock;
        this.allocatedSpace = allocatedSpace;
        this.firstBlockIndex = firstBlockIndex;
        this.indexBlockSerializer = indexBlockSerializer;

        currentBlock = firstBlock;
        currentBlockIndex = firstBlockIndex;
    }

    public void appendBlock(@Nonnull ByteBuffer data) throws IOException {
        if (data.remaining() > allocatedSpace.getBlockSize().getBlockSizeInBytes()) {
            throw new RuntimeException();
        }

        int freeBlock = allocatedSpace.getFreeBlockIndexAndMarkAsAllocated();
        while (!currentBlock.addBlockPointer(freeBlock)) {
            nextIndexBlock();
        }

        allocatedSpace.writeBlock(freeBlock, data);
    }

    public void rewriteBlocks(@Nonnull Iterator<ByteBuffer> blocks) throws IOException {
        resetToFirstBlock();

        int i = 0;
        while (blocks.hasNext()) {
            ByteBuffer block = blocks.next();

            int blockPointer;
            if (i >= currentBlock.size()) {
                blockPointer = allocatedSpace.getFreeBlockIndex();
                currentBlock.addBlockPointer(blockPointer);
            } else {
                blockPointer = currentBlock.getBlockPointers().get(i);
            }
            allocatedSpace.writeBlock(blockPointer, block);
            i++;

            if (i == currentBlock.getMaxBlockPointers()) {
                nextIndexBlock();
                i = 0;
            }
        }

        while (i != currentBlock.size()) {
            Integer blockPointer = currentBlock.getBlockPointers().get(i);
            currentBlock.removeBlockPointer(blockPointer);
            allocatedSpace.fillBlockZeros(blockPointer);
        }

        int nextBlock = currentBlock.getNextIndexBlock();
        while (nextBlock != 0) {
            ByteBuffer nextBlockBytes = allocatedSpace.readBlock(currentBlock.getNextIndexBlock());
            FileContentIndexBlock indexBlock = indexBlockSerializer.from(nextBlockBytes);

            for (Integer blockPointer : indexBlock.getBlockPointers()) {
                allocatedSpace.fillBlockZeros(blockPointer);
            }
            allocatedSpace.fillBlockZeros(nextBlock);

            nextBlock = indexBlock.getNextIndexBlock();
        }
        allocatedSpace.writeBlock(currentBlockIndex, indexBlockSerializer.toByteBuffer(currentBlock));

        resetToFirstBlock();
    }

    public void resetToFirstBlock() {
        log.debug("reset to first block");
        currentBlock = firstBlock;
    }

    private FileContentIndexBlock nextIndexBlock() throws IOException {
        if (currentBlock.getNextIndexBlock() > 0) {
            ByteBuffer buffer = indexBlockSerializer.toByteBuffer(currentBlock);
            allocatedSpace.writeBlock(currentBlockIndex, buffer);

            int nextBlockIndex = currentBlock.getNextIndexBlock();
            ByteBuffer nextBlockBytes = allocatedSpace.readBlock(nextBlockIndex);
            currentBlockIndex = nextBlockIndex;
            currentBlock = indexBlockSerializer.from(nextBlockBytes);
        } else {
            int allocatedBlock = allocatedSpace.getFreeBlockIndex();
            currentBlock.setNextIndexBlock(allocatedBlock);
            ByteBuffer buffer = indexBlockSerializer.toByteBuffer(currentBlock);
            allocatedSpace.writeBlock(currentBlockIndex, buffer);

            FileContentIndexBlock indexBlock = new FileContentIndexBlock();
            currentBlockIndex = allocatedBlock;
            currentBlock = indexBlock;
        }
        return currentBlock;
    }

    @Override
    public void close() throws IOException {
        allocatedSpace.writeBlock(firstBlockIndex, indexBlockSerializer.toByteBuffer(firstBlock));
        allocatedSpace.writeBlock(currentBlockIndex, indexBlockSerializer.toByteBuffer(currentBlock));
    }
}
