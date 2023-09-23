package me.vladislav.fs.operations;

import me.vladislav.fs.IndexedBlockAllocatedSpace;
import me.vladislav.fs.blocks.serializers.FileContentIndexBlockBytesSerializer;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import me.vladislav.fs.operations.my.MyFileSystemOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class OperationsConfig {

    @Bean
    @Scope("prototype")
    @SuppressWarnings("all")
    public MyFileSystemOperations myFileSystemOperations(
            IndexedBlockAllocatedSpace allocatedSpace,
            FileContentIndexBlockBytesSerializer indexBlockBytesSerializer,
            FileDescriptorsBlockBytesSerializer fileDescriptorsBlockBytesSerializer
    ) {
        return MyFileSystemOperations.builder()
                .allocatedSpace(allocatedSpace)
                .indexBlockSerializer(indexBlockBytesSerializer)
                .descriptorsBlockSerializer(fileDescriptorsBlockBytesSerializer)
                .build();
    }
}
