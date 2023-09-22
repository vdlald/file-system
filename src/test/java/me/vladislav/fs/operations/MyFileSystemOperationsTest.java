package me.vladislav.fs.operations;

import me.vladislav.fs.AbstractFileSystemTest;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.operations.my.MyFileSystemOperations;
import me.vladislav.fs.operations.my.MyFileSystemOperations.FileContentIndexBlock;
import me.vladislav.fs.operations.my.MyFileSystemOperations.FileDescriptor;
import me.vladislav.fs.operations.my.MyFileSystemOperations.FileDescriptorsBlock;
import me.vladislav.fs.requests.CreateFileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyFileSystemOperationsTest extends AbstractFileSystemTest {

    @Test
    @DisplayName("MyFileSystemOperations / FileDescriptor / Must converting to bytes and from bytes")
    void testFileDescriptorConvert() {
        String filename = UUID.randomUUID().toString();

        FileDescriptor expected = FileDescriptor.builder()
                .filename(filename)
                .firstBlockIndex(4)
                .build();

        ByteBuffer descriptorRaw = expected.toByteBuffer();

        FileDescriptor actual = FileDescriptor.from(descriptorRaw);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("MyFileSystemOperations / FileDescriptorsBlock / Must converting to bytes and from bytes")
    void testFileDescriptorsBlockConvert() {
        FileDescriptorsBlock block = new FileDescriptorsBlock();

        FileDescriptor expected1 = FileDescriptor.builder()
                .filename(UUID.randomUUID().toString())
                .firstBlockIndex(4)
                .build();
        FileDescriptor expected2 = FileDescriptor.builder()
                .filename(UUID.randomUUID().toString())
                .firstBlockIndex(7)
                .build();

        block.addDescriptor(expected1);
        block.addDescriptor(expected2);

        ByteBuffer raw = block.toByteBuffer();
        FileDescriptorsBlock actual = FileDescriptorsBlock.from(raw);

        assertEquals(block, actual);
    }

    @Test
    @DisplayName("MyFileSystemOperations / FileDescriptorsBlock / From empty bytes")
    void testFileDescriptorsBlockConvertEmpty() {
        ByteBuffer allocate = ByteBuffer.allocate(FileDescriptorsBlock.TOTAL_SIZE);
        FileDescriptorsBlock block = FileDescriptorsBlock.from(allocate);
        assertTrue(block.isEmpty());
    }

    @Test
    @DisplayName("MyFileSystemOperations / File creation / Write descriptor")
    void testCreateFile() throws Exception {
        testWithFileSystem(fileSystem -> {
            CreateFileRequest request = CreateFileRequest.builder()
                    .filename(UUID.randomUUID().toString())
                    .content(Files.newByteChannel(Files.createTempFile("temp", "suff")))
                    .build();

            fileSystem.getFileSystemOperations().createFile(request);

            MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
            BlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();
            ByteBuffer firstBlock = allocatedSpace.readBlock(0);
            FileDescriptorsBlock descriptors = FileDescriptorsBlock.from(firstBlock);

            assertEquals(1, descriptors.size());

            FileDescriptor descriptor = descriptors.getDescriptor(0);
            assertEquals(request.getFilename(), descriptor.getFilename());

        });
    }

    @Test
    @DisplayName("MyFileSystemOperations / FileContentIndexBlock / Must converting to bytes and from bytes")
    void testFileContentIndexBlockConvert() {
        FileContentIndexBlock expected = new FileContentIndexBlock();
        expected.addBlockPointer(10);
        expected.addBlockPointer(13);
        expected.addBlockPointer(39);
        expected.setNextIndexBlock(12);

        FileContentIndexBlock actual = FileContentIndexBlock.from(expected.toByteBuffer());

        assertEquals(expected, actual);
    }
}
