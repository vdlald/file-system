package me.vladislav.fs.blocks.components;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.IndexedBlockAllocatedSpace;
import me.vladislav.fs.blocks.FileContentIndexBlock;
import me.vladislav.fs.blocks.serializers.FileContentIndexBlockBytesSerializer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Iterator;

/**
 * Represents linked index blocks of a file's content
 * Encapsulates the handling of index blocks when writing data
 * Allows you to stop thinking about allocating and reading the following blocks
 */
@Slf4j
@Component
@Scope("prototype")
public class ChainedFileContentIndexBlock implements Closeable {

    @Getter
    private final int firstBlockIndex;

    @Nonnull
    private final FileContentIndexBlock firstBlock;

    @Getter
    private final long fileSize;

    private int currentBlockIndex;

    @Nonnull
    private FileContentIndexBlock currentBlock;

    @Nonnull
    private final IndexedBlockAllocatedSpace allocatedSpace;

    @Nonnull
    private final FileContentIndexBlockBytesSerializer indexBlockSerializer;

    @SuppressWarnings("all")
    public ChainedFileContentIndexBlock(
            long fileSize,
            int firstBlockIndex,
            FileContentIndexBlock firstBlock,
            IndexedBlockAllocatedSpace allocatedSpace,
            FileContentIndexBlockBytesSerializer indexBlockSerializer
    ) {
        this.fileSize = fileSize;
        this.firstBlock = firstBlock;
        this.allocatedSpace = allocatedSpace;
        this.firstBlockIndex = firstBlockIndex;
        this.indexBlockSerializer = indexBlockSerializer;

        currentBlock = firstBlock;
        currentBlockIndex = firstBlockIndex;
    }

    public void readAllBlocks(SeekableByteChannel outChannel) {
        resetToFirstBlock();
        AllocatedSpace outSpace = AllocatedSpace.builder().data(outChannel).build();

        while (true) {
            for (int blockPointer : currentBlock.getBlockPointers()) {
                ByteBuffer buffer = allocatedSpace.readBlock(blockPointer);
                outSpace.write(buffer);
            }
            if (currentBlock.hasNextBlock()) {
                nextIndexBlock();
            } else {
                break;
            }
        }

        outSpace.truncate(fileSize);
    }

    public void rewriteBlocks(@Nonnull Iterator<ByteBuffer> blocks) {
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
        currentBlockIndex = firstBlockIndex;
    }

    private void nextIndexBlock() {
        ByteBuffer buffer = indexBlockSerializer.toByteBuffer(currentBlock);
        allocatedSpace.writeBlock(currentBlockIndex, buffer);

        if (currentBlock.getNextIndexBlock() > 0) {
            int nextBlockIndex = currentBlock.getNextIndexBlock();
            ByteBuffer nextBlockBytes = allocatedSpace.readBlock(nextBlockIndex);

            currentBlockIndex = nextBlockIndex;
            currentBlock = indexBlockSerializer.from(nextBlockBytes);
        } else {
            int allocatedBlock = allocatedSpace.getFreeBlockIndexAndMarkAsAllocated();
            currentBlock.setNextIndexBlock(allocatedBlock);

            currentBlockIndex = allocatedBlock;
            currentBlock = new FileContentIndexBlock();
        }
    }

    @Override
    public void close() {
        allocatedSpace.writeBlock(firstBlockIndex, indexBlockSerializer.toByteBuffer(firstBlock));
        allocatedSpace.writeBlock(currentBlockIndex, indexBlockSerializer.toByteBuffer(currentBlock));
    }
}
