package me.vladislav.fs.operations.my;

import lombok.RequiredArgsConstructor;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.BlockSize;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MyFileSystemOperationsFactory {

    private final ObjectProvider<MyFileSystemOperations> objectProvider;
    private final FileDescriptorsBlockBytesSerializer fileDescriptorsBlockBytesSerializer;

    @Nonnull
    public MyFileSystemOperations create(@Nonnull AllocatedSpace allocatedSpace) throws IOException {
        return objectProvider.getObject(
                new BlockAllocatedSpace(BlockSize.KB_4, allocatedSpace),
                fileDescriptorsBlockBytesSerializer
        );
    }
}
