package me.vladislav.fs.operations;

import me.vladislav.fs.AbstractFileSystemTest;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import me.vladislav.fs.operations.my.MyFileSystemOperations;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.util.ByteBufferUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class MyFileSystemOperationsTest extends AbstractFileSystemTest {

    @Autowired
    private FileDescriptorsBlockBytesSerializer fileDescriptorsBlockBytesSerializer;

    @Test
    @DisplayName("File creation / The descriptor must be written")
    void testCreateFileDescriptor() throws Exception {
        CreateFileRequest request = createFileRequest("");

        fileSystem.getFileSystemOperations().createFile(request);

        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
        BlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();
        ByteBuffer firstBlock = allocatedSpace.readBlock(0);
        FileDescriptorsBlock descriptors = fileDescriptorsBlockBytesSerializer.from(firstBlock);

        assertEquals(1, descriptors.size());

        FileDescriptor descriptor = descriptors.getDescriptor(0);
        assertNotNull(descriptor);
        assertEquals(request.getFilename(), descriptor.getFilename());
    }

    @Test
    @DisplayName("File creation / File content must be written")
    void testCreateFileContent() throws Exception {
        String expected = "Some content";
        CreateFileRequest request = createFileRequest(expected);

        fileSystem.getFileSystemOperations().createFile(request);

        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
        BlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();
        ByteBuffer firstBlock = allocatedSpace.readBlock(0);

        FileDescriptorsBlock descriptors = fileDescriptorsBlockBytesSerializer.from(firstBlock);

        FileDescriptor descriptor = descriptors.getDescriptor(0);

        assertNotNull(descriptor);
        assertNotEquals(0, descriptor.getFileBlockIndex());

        ByteBuffer contentRaw = allocatedSpace.readBlock(descriptor.getFileBlockIndex());
        String content = ByteBufferUtils.parseString(contentRaw);

        assertEquals(expected, content);
    }

    @Test
    @DisplayName("File creation / File is closed after writing")
    void testCreateFileClose() throws Exception {
        String expected = "Some content";
        CreateFileRequest request = createFileRequest(expected);

        fileSystem.getFileSystemOperations().createFile(request);

        assertFalse(request.getContent().isOpen());
    }

}
