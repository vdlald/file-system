package me.vladislav.fs.operations;

import me.vladislav.fs.AbstractFileSystemTest;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import me.vladislav.fs.operations.my.MyFileSystemOperations;
import me.vladislav.fs.requests.CreateFileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyFileSystemOperationsTest extends AbstractFileSystemTest {

    @Autowired
    private FileDescriptorsBlockBytesSerializer fileDescriptorsBlockBytesSerializer;

    @Test
    @DisplayName("File creation / The descriptor must be written")
    void testCreateFile() throws Exception {
        CreateFileRequest request = CreateFileRequest.builder()
                .filename(UUID.randomUUID().toString())
                .content(Files.newByteChannel(Files.createTempFile("temp", "suff")))
                .build();

        fileSystem.getFileSystemOperations().createFile(request);

        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
        BlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();
        ByteBuffer firstBlock = allocatedSpace.readBlock(0);
        FileDescriptorsBlock descriptors = fileDescriptorsBlockBytesSerializer.from(firstBlock);

        assertEquals(1, descriptors.size());

        FileDescriptor descriptor = descriptors.getDescriptor(0);
        assertEquals(request.getFilename(), descriptor.getFilename());
    }
}
