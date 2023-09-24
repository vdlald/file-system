package me.vladislav.fs.operations;

import me.vladislav.fs.*;
import me.vladislav.fs.blocks.FileContentIndexBlock;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import me.vladislav.fs.blocks.serializers.FileContentIndexBlockBytesSerializer;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import me.vladislav.fs.operations.my.MyFileSystemOperations;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;
import me.vladislav.fs.util.ByteBufferUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static org.junit.jupiter.api.Assertions.*;

public class MyFileSystemOperationsTest extends AbstractFileSystemTest {

    @Autowired
    private FileContentIndexBlockBytesSerializer indexBlockBytesSerializer;

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

        FileContentIndexBlock indexBlock = indexBlockBytesSerializer.from(
                allocatedSpace.readBlock(descriptor.getFileBlockIndex()));

        ByteBuffer contentRaw = allocatedSpace.readBlocks(indexBlock.getBlockPointers());
        String content = ByteBufferUtils.parseString(contentRaw);

        assertEquals(expected, content);
    }

    @Test
    @DisplayName("File creation / File content must be written / Big file")
    void testCreateBigFileContent() throws Exception {
        CreateFileRequest request = createFileRequest(readCvFile());

        fileSystem.getFileSystemOperations().createFile(request);

        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
        BlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();
        ByteBuffer firstBlock = allocatedSpace.readBlock(0);

        FileDescriptorsBlock descriptors = fileDescriptorsBlockBytesSerializer.from(firstBlock);

        FileDescriptor descriptor = descriptors.getDescriptor(0);

        assertNotNull(descriptor);
        assertNotEquals(0, descriptor.getFileBlockIndex());

        FileContentIndexBlock indexBlock = indexBlockBytesSerializer.from(
                allocatedSpace.readBlock(descriptor.getFileBlockIndex()));

        SeekableByteChannel file = readCvFile();

        BlockAllocatedSpace blockAllocatedSpace = new BlockAllocatedSpace(BlockSize.KB_4, AllocatedSpace.builder()
                .data(file)
                .build());

        for (int i = 0; i < indexBlock.getBlockPointers().size(); i++) {
            assertEquals(
                    blockAllocatedSpace.readBlock(i),
                    allocatedSpace.readBlock(indexBlock.getBlockPointers().get(i))
            );
        }
        file.close();
    }

    @ParameterizedTest
    @EnumSource(BlockSize.class)
    @DisplayName("File creation / File content must be written / Big big file")
    void testCreateBigBigFileContent(BlockSize blockSize) throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(blockSize));

        CreateFileRequest request = createFileRequest(readJbFile());

        fileSystem.getFileSystemOperations().createFile(request);

        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
        BlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();
        ByteBuffer firstBlock = allocatedSpace.readBlock(0);

        FileDescriptorsBlock descriptors = fileDescriptorsBlockBytesSerializer.from(firstBlock);

        FileDescriptor descriptor = descriptors.getDescriptor(0);

        assertNotNull(descriptor);
        assertNotEquals(0, descriptor.getFileBlockIndex());

        FileContentIndexBlock indexBlock = indexBlockBytesSerializer.from(
                allocatedSpace.readBlock(descriptor.getFileBlockIndex()));

        SeekableByteChannel file = readJbFile();

        BlockAllocatedSpace blockAllocatedSpace = new BlockAllocatedSpace(BlockSize.KB_4, AllocatedSpace.builder()
                .data(file)
                .build());

        for (int i = 0; i < indexBlock.getBlockPointers().size(); i++) {
            ByteBuffer expected = blockAllocatedSpace.readBlock(i);
            ByteBuffer actual = allocatedSpace.readBlock(indexBlock.getBlockPointers().get(i));
            assertEquals(expected, actual);
        }
        file.close();
        fileSystem.close();
    }

    @ParameterizedTest
    @EnumSource(BlockSize.class)
    @DisplayName("File creation / Must save both files")
    void testCreateTwoFilesContent(BlockSize blockSize) throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(blockSize));

        CreateFileRequest request1 = createFileRequest(readJbFile());
        fileSystem.getFileSystemOperations().createFile(request1);

        CreateFileRequest request2 = createFileRequest(readCvFile());
        fileSystem.getFileSystemOperations().createFile(request2);

        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
        BlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();
        ByteBuffer firstBlock = allocatedSpace.readBlock(0);

        FileDescriptorsBlock descriptors = fileDescriptorsBlockBytesSerializer.from(firstBlock);

        // check first file
        FileDescriptor descriptor = descriptors.getDescriptor(0);

        assertNotNull(descriptor);
        assertNotEquals(0, descriptor.getFileBlockIndex());

        FileContentIndexBlock indexBlock = indexBlockBytesSerializer.from(
                allocatedSpace.readBlock(descriptor.getFileBlockIndex()));

        SeekableByteChannel file = readJbFile();

        BlockAllocatedSpace blockAllocatedSpace = new BlockAllocatedSpace(BlockSize.KB_4, AllocatedSpace.builder()
                .data(file)
                .build());

        for (int i = 0; i < indexBlock.getBlockPointers().size(); i++) {
            assertEquals(
                    blockAllocatedSpace.readBlock(i),
                    allocatedSpace.readBlock(indexBlock.getBlockPointers().get(i))
            );
        }
        file.close();


        // check second file
        descriptor = descriptors.getDescriptor(1);

        assertNotNull(descriptor);
        assertNotEquals(0, descriptor.getFileBlockIndex());

        indexBlock = indexBlockBytesSerializer.from(
                allocatedSpace.readBlock(descriptor.getFileBlockIndex()));

        file = readCvFile();

        blockAllocatedSpace = new BlockAllocatedSpace(BlockSize.KB_4, AllocatedSpace.builder()
                .data(file)
                .build());

        for (int i = 0; i < indexBlock.getBlockPointers().size(); i++) {
            assertEquals(
                    blockAllocatedSpace.readBlock(i),
                    allocatedSpace.readBlock(indexBlock.getBlockPointers().get(i))
            );
        }
        file.close();
        fileSystem.close();
    }

    @ParameterizedTest
    @EnumSource(BlockSize.class)
    @DisplayName("File read and create / Big big file")
    void testCreateAndRead(BlockSize blockSize) throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(blockSize));

        CreateFileRequest request = createFileRequest(readJbFile());

        fileSystem.getFileSystemOperations().createFile(request);

        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
        SeekableByteChannel read = fsOperations.readFile(request.getFilename());

        SeekableByteChannel file = readJbFile();

        BlockAllocatedSpace expectedBlocks = new BlockAllocatedSpace(BlockSize.KB_4, AllocatedSpace.builder()
                .data(file)
                .build());

        BlockAllocatedSpace actualBlocks = new BlockAllocatedSpace(BlockSize.KB_4, AllocatedSpace.builder()
                .data(read)
                .build());

        for (int i = 0; i < expectedBlocks.getBlocksAmount(); i++) {
            ByteBuffer expected = expectedBlocks.readBlock(i);
            ByteBuffer actual = actualBlocks.readBlock(i);
            assertEquals(expected, actual);
        }
        file.close();
        fileSystem.close();
    }

    @ParameterizedTest
    @EnumSource(BlockSize.class)
    @DisplayName("File create and delete / Big big file")
    void testCreateAndDelete(BlockSize blockSize) throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(blockSize));

        CreateFileRequest request = createFileRequest(readJbFile());

        fileSystem.getFileSystemOperations().createFile(request);

        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
        fsOperations.deleteFile(request.getFilename());
        assertThrows(FileNotFoundException.class, () -> {
            try (SeekableByteChannel file = fsOperations.readFile(request.getFilename())) {
            }
        });

        fileSystem.close();
    }

    @ParameterizedTest
    @EnumSource(BlockSize.class)
    @DisplayName("File create and update / Big big file / shrink")
    void testCreateAndUpdateShrink(BlockSize blockSize) throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(blockSize));

        CreateFileRequest request = createFileRequest(readJbFile());

        fileSystem.getFileSystemOperations().createFile(request);

        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();

        String filename = request.getFilename();
        UpdateFileRequest updateFileRequest = UpdateFileRequest.builder()
                .filename(filename)
                .content(readCvFile())
                .build();

        fsOperations.updateFile(updateFileRequest);

        BlockAllocatedSpace actual = BlockAllocatedSpace.of(fsOperations.readFile(filename));
        BlockAllocatedSpace expected = BlockAllocatedSpace.of(readCvFile());

        for (int i = 0; i < expected.getBlocksAmount(); i++) {
            assertEquals(expected.readBlock(i), actual.readBlock(i));
        }

        fileSystem.close();
    }

    @ParameterizedTest
    @EnumSource(BlockSize.class)
    @DisplayName("File create and update / Big big file / extend file")
    void testCreateAndUpdateExtend(BlockSize blockSize) throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(blockSize));

        CreateFileRequest request = createFileRequest(readCvFile());

        fileSystem.getFileSystemOperations().createFile(request);

        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();

        String filename = request.getFilename();
        UpdateFileRequest updateFileRequest = UpdateFileRequest.builder()
                .filename(filename)
                .content(readJbFile())
                .build();

        fsOperations.updateFile(updateFileRequest);

        BlockAllocatedSpace actual = BlockAllocatedSpace.of(fsOperations.readFile(filename));
        BlockAllocatedSpace expected = BlockAllocatedSpace.of(readJbFile());

        for (int i = 0; i < expected.getBlocksAmount(); i++) {
            assertEquals(expected.readBlock(i), actual.readBlock(i));
        }

        fileSystem.close();
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
