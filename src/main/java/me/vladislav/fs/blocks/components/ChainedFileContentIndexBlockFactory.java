package me.vladislav.fs.blocks.components;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.IndexedBlockAllocatedSpace;
import me.vladislav.fs.blocks.FileContentIndexBlock;
import me.vladislav.fs.blocks.serializers.FileContentIndexBlockBytesSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChainedFileContentIndexBlockFactory {

    private final FileContentIndexBlockBytesSerializer indexBlockSerializer;
    private final ObjectProvider<ChainedFileContentIndexBlock> objectProvider;

    @Nonnull
    public ChainedFileContentIndexBlock create(
            int firstBlockIndex,
            @Nonnull FileContentIndexBlock firstBlock,
            @Nonnull IndexedBlockAllocatedSpace allocatedSpace
    ) {
        log.debug("create chain for: {}", firstBlockIndex);
        return objectProvider.getObject(firstBlockIndex, firstBlock, allocatedSpace, indexBlockSerializer);
    }
}
