package me.vladislav.fs.operations;

import me.vladislav.fs.BlockAllocatedSpace;
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
            BlockAllocatedSpace allocatedSpace,
            FileDescriptorsBlockBytesSerializer fileDescriptorsBlockBytesSerializer
    ) {
        return MyFileSystemOperations.builder()
                .allocatedSpace(allocatedSpace)
                .fileDescriptorsBlockBytesSerializer(fileDescriptorsBlockBytesSerializer)
                .build();
    }
}
