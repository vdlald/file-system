package me.vladislav.fs.operations.my;

import lombok.RequiredArgsConstructor;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.BlockSize;
import me.vladislav.fs.IndexedBlockAllocatedSpace;
import me.vladislav.fs.blocks.components.ChainedFileContentIndexBlockFactory;
import me.vladislav.fs.blocks.components.ChainedFileDescriptorsBlockFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@RequiredArgsConstructor
public class BlockFileSystemOperationsFactory {

    private final ObjectProvider<BlockFileSystemOperations> objectProvider;
    private final ChainedFileDescriptorsBlockFactory chainedFileDescriptorsBlockFactory;
    private final ChainedFileContentIndexBlockFactory chainedFileContentIndexBlockFactory;

    @Nonnull
    public BlockFileSystemOperations create(
            @Nonnull AllocatedSpace allocatedSpace,
            @Nonnull BlockSize blockSize
    ) {
        return objectProvider.getObject(
                new IndexedBlockAllocatedSpace(blockSize, allocatedSpace),
                chainedFileDescriptorsBlockFactory,
                chainedFileContentIndexBlockFactory
        );
    }
}
