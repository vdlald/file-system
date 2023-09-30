package me.vladislav.fs.blocks.components;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.IndexedBlockAllocatedSpace;
import me.vladislav.fs.blocks.FileContentIndexBlock;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.converters.FileContentIndexBlockBytesConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChainedFileContentIndexBlockFactory {

    private final FileContentIndexBlockBytesConverter indexBlockSerializer;
    private final ObjectProvider<ChainedFileContentIndexBlock> objectProvider;

    @Nonnull
    public ChainedFileContentIndexBlock create(
            FileDescriptor descriptor,
            @Nonnull IndexedBlockAllocatedSpace allocatedSpace
    ) {
        log.debug("create chain for: {}", descriptor);
        ByteBuffer buffer = allocatedSpace.readBlock(descriptor.getFileBlockIndex());
        FileContentIndexBlock firstBlock = indexBlockSerializer.from(buffer);
        return create(descriptor.getFileSize(), descriptor.getFileBlockIndex(), firstBlock, allocatedSpace);
    }

    @Nonnull
    public ChainedFileContentIndexBlock create(
            long fileSize,
            int firstBlockIndex,
            @Nonnull FileContentIndexBlock firstBlock,
            @Nonnull IndexedBlockAllocatedSpace allocatedSpace
    ) {
        log.debug("create chain for: {}", firstBlockIndex);
        return objectProvider.getObject(fileSize, firstBlockIndex, firstBlock, allocatedSpace, indexBlockSerializer);
    }
}
