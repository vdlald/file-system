package me.vladislav.fs.blocks.components;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.IndexedBlockAllocatedSpace;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Represents linked blocks of file descriptors
 * Encapsulates working with a series of descriptor blocks in the file system
 * Allows you to stop thinking about allocating and reading the following blocks
 */
@Slf4j
@Component
@Scope("prototype")
public class ChainedFileDescriptorsBlock {

    @Getter
    private final int firstBlockIndex;

    @Getter
    private int currentBlockIndex;
    private FileDescriptorsBlock currentBlock;

    private final IndexedBlockAllocatedSpace allocatedSpace;
    private final FileDescriptorsBlockBytesSerializer descriptorsBlockSerializer;

    @SuppressWarnings("all")
    public ChainedFileDescriptorsBlock(
            int firstBlockIndex,
            FileDescriptorsBlock fileDescriptorsBlock,
            IndexedBlockAllocatedSpace allocatedSpace,
            FileDescriptorsBlockBytesSerializer descriptorsBlockSerializer
    ) {
        this.allocatedSpace = allocatedSpace;
        this.firstBlockIndex = firstBlockIndex;
        this.currentBlock = fileDescriptorsBlock;
        this.descriptorsBlockSerializer = descriptorsBlockSerializer;

        currentBlockIndex = firstBlockIndex;
    }

    public void addFileDescriptor(@Nonnull FileDescriptor fileDescriptor) throws IOException {
        while (!currentBlock.addDescriptor(fileDescriptor)) {
            nextBlock();
        }

        ByteBuffer buffer = descriptorsBlockSerializer.toByteBuffer(currentBlock);
        allocatedSpace.writeBlock(currentBlockIndex, buffer);
    }

    @Nullable
    public FileDescriptor getFileDescriptor(@Nonnull String filename) throws IOException {
        resetToFirstBlock();

        int descriptorIndex;
        do {
            descriptorIndex = currentBlock.getDescriptorIndex(filename);
            if (descriptorIndex == -1 && currentBlock.hasNextBlock()) {
                nextBlock();
            } else {
                break;
            }
        } while (true);

        if (descriptorIndex == -1) {
            return null;
        }

        return currentBlock.getDescriptors().get(descriptorIndex);
    }

    public boolean removeFileDescriptor(@Nonnull String filename) throws IOException {
        resetToFirstBlock();

        int descriptorIndex;
        do {
            descriptorIndex = currentBlock.getDescriptorIndex(filename);
            if (descriptorIndex == -1 && currentBlock.hasNextBlock()) {
                nextBlock();
            } else {
                break;
            }
        } while (true);

        if (descriptorIndex == -1) {
            return false;
        }

        currentBlock.removeDescriptor(descriptorIndex);
        allocatedSpace.writeBlock(currentBlockIndex, descriptorsBlockSerializer.toByteBuffer(currentBlock));
        return true;
    }

    public void resetToFirstBlock() throws IOException {
        log.debug("reset to first block");
        currentBlock = descriptorsBlockSerializer.from(allocatedSpace.readBlock(firstBlockIndex));
        currentBlockIndex = firstBlockIndex;
    }

    private void nextBlock() throws IOException {
        ByteBuffer buffer = descriptorsBlockSerializer.toByteBuffer(currentBlock);
        allocatedSpace.writeBlock(currentBlockIndex, buffer);

        if (currentBlock.hasNextBlock()) {
            int nextBlockIndex = currentBlock.getNextFileDescriptorBlock();
            ByteBuffer next = allocatedSpace.readBlock(nextBlockIndex);

            currentBlockIndex = nextBlockIndex;
            currentBlock = descriptorsBlockSerializer.from(next);
        } else {
            int allocatedBlock = allocatedSpace.getFreeBlockIndexAndMarkAsAllocated();
            currentBlock.setNextFileDescriptorBlock(allocatedBlock);

            currentBlockIndex = allocatedBlock;
            currentBlock = new FileDescriptorsBlock(allocatedSpace.getBlockSize());
        }
    }
}
