package me.vladislav.fs.operations.my;

import lombok.RequiredArgsConstructor;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.BlockSize;
import me.vladislav.fs.IndexedBlockAllocatedSpace;
import me.vladislav.fs.blocks.components.ChainedFileContentIndexBlockFactory;
import me.vladislav.fs.blocks.serializers.FileContentIndexBlockBytesSerializer;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MyFileSystemOperationsFactory {

    private final ObjectProvider<MyFileSystemOperations> objectProvider;
    private final FileContentIndexBlockBytesSerializer indexBlockBytesSerializer;
    private final FileDescriptorsBlockBytesSerializer fileDescriptorsBlockBytesSerializer;
    private final ChainedFileContentIndexBlockFactory chainedFileContentIndexBlockFactory;

    @Nonnull
    public MyFileSystemOperations create(
            @Nonnull AllocatedSpace allocatedSpace,
            @Nonnull BlockSize blockSize
    ) throws IOException {
        return objectProvider.getObject(
                new IndexedBlockAllocatedSpace(blockSize, allocatedSpace),
                indexBlockBytesSerializer,
                fileDescriptorsBlockBytesSerializer,
                chainedFileContentIndexBlockFactory
        );
    }
}
