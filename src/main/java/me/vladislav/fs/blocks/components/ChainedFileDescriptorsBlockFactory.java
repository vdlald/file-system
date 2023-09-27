package me.vladislav.fs.blocks.components;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.IndexedBlockAllocatedSpace;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChainedFileDescriptorsBlockFactory {

    private final ObjectProvider<ChainedFileDescriptorsBlock> objectProvider;
    private final FileDescriptorsBlockBytesSerializer descriptorsBlockSerializer;

    @Nonnull
    public ChainedFileDescriptorsBlock create(
            int firstBlockIndex,
            @Nonnull IndexedBlockAllocatedSpace allocatedSpace
    ) throws IOException {
        log.debug("create chain for: {}", firstBlockIndex);
        ByteBuffer buffer = allocatedSpace.readBlock(firstBlockIndex);
        FileDescriptorsBlock fileDescriptorsBlock = descriptorsBlockSerializer.from(buffer);
        return create(firstBlockIndex, fileDescriptorsBlock, allocatedSpace);
    }

    @Nonnull
    public ChainedFileDescriptorsBlock create(
            int firstBlockIndex,
            @Nonnull FileDescriptorsBlock fileDescriptorsBlock,
            @Nonnull IndexedBlockAllocatedSpace allocatedSpace
    ) {
        log.debug("create chain for: {}", firstBlockIndex);
        return objectProvider.getObject(
                firstBlockIndex, fileDescriptorsBlock, allocatedSpace, descriptorsBlockSerializer
        );
    }
}
