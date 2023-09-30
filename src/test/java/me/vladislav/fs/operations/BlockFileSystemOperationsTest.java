package me.vladislav.fs.operations;

import me.vladislav.fs.*;
import me.vladislav.fs.blocks.FileContentIndexBlock;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import me.vladislav.fs.blocks.converters.FileContentIndexBlockBytesConverter;
import me.vladislav.fs.blocks.converters.FileDescriptorsBlockBytesConverter;
import me.vladislav.fs.exceptions.FileNotFoundException;
import me.vladislav.fs.operations.impl.BlockFileSystemOperations;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.CreateFileSystemRequest;
import me.vladislav.fs.requests.UpdateFileRequest;
import me.vladislav.fs.util.ByteBufferUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import static org.junit.jupiter.api.Assertions.*;

public class BlockFileSystemOperationsTest extends AbstractFileSystemTest {

    @Autowired
    private FileContentIndexBlockBytesConverter indexBlockBytesSerializer;

    @Autowired
    private FileDescriptorsBlockBytesConverter fileDescriptorsBlockBytesSerializer;

    @Test
    @DisplayName("File creation / The descriptor must be written")
    void testCreateFileDescriptor() throws Exception {
        CreateFileRequest request = createFileRequest(" ");

        fileSystem.getFileSystemOperations().createFile(request);

        BlockFileSystemOperations fsOperations = (BlockFileSystemOperations) fileSystem.getFileSystemOperations();
        BlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();
        ByteBuffer firstBlock = allocatedSpace.readBlock(0);
        FileDescriptorsBlock descriptors = fileDescriptorsBlockBytesSerializer.from(firstBlock);

        assertEquals(1, descriptors.size());

        FileDescriptor descriptor = descriptors.getDescriptor(0);
        assertNotNull(descriptor);
        assertEquals(request.getFilename(), descriptor.getFilename());
        assertEquals(1, descriptor.getFileSize());
    }

    @Test
    @DisplayName("File creation / File content must be written")
    void testCreateFileContent() throws Exception {
        String expected = "Some content";
        CreateFileRequest request = createFileRequest(expected);

        fileSystem.getFileSystemOperations().createFile(request);

        BlockFileSystemOperations fsOperations = (BlockFileSystemOperations) fileSystem.getFileSystemOperations();
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

        BlockFileSystemOperations fsOperations = (BlockFileSystemOperations) fileSystem.getFileSystemOperations();
        BlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();
        ByteBuffer firstBlock = allocatedSpace.readBlock(0);

        FileDescriptorsBlock descriptors = fileDescriptorsBlockBytesSerializer.from(firstBlock);

        FileDescriptor descriptor = descriptors.getDescriptor(0);

        assertNotNull(descriptor);
        assertNotEquals(0, descriptor.getFileBlockIndex());

        SeekableByteChannel expected = readCvFile();

        assertChannelsContentEquals(expected, fsOperations.readFile(request.getFilename()));
        expected.close();
    }

    @Test
    @DisplayName("File creation / File content must be written / Big big file")
    void testCreateBigBigFileContent() throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4));

        CreateFileRequest request = createFileRequest(readJbFile());

        fileSystem.getFileSystemOperations().createFile(request);

        BlockFileSystemOperations fsOperations = (BlockFileSystemOperations) fileSystem.getFileSystemOperations();
        BlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();
        ByteBuffer firstBlock = allocatedSpace.readBlock(0);

        FileDescriptorsBlock descriptors = fileDescriptorsBlockBytesSerializer.from(firstBlock);

        FileDescriptor descriptor = descriptors.getDescriptor(0);

        assertNotNull(descriptor);
        assertNotEquals(0, descriptor.getFileBlockIndex());

        SeekableByteChannel expected = readJbFile();
        assertChannelsContentEquals(expected, fsOperations.readFile(request.getFilename()));

        expected.close();
        fileSystem.close();
    }

    @Test
    @DisplayName("File creation / Must save both files")
    void testCreateTwoFilesContent() throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4));

        CreateFileRequest request1 = createFileRequest(readJbFile());
        ExtendedFileSystemOperations fsOperations = fileSystem.getFileSystemOperations();
        fsOperations.createFile(request1);

        CreateFileRequest request2 = createFileRequest(readCvFile());
        fsOperations.createFile(request2);

        assertChannelsContentEquals(readJbFile(), fsOperations.readFile(request1.getFilename()));
        assertChannelsContentEquals(readCvFile(), fsOperations.readFile(request2.getFilename()));
    }

    @Test
    @DisplayName("File read and create / Big big file")
    void testCreateAndRead() throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4));

        CreateFileRequest request = createFileRequest(readJbFile());

        fileSystem.getFileSystemOperations().createFile(request);

        BlockFileSystemOperations fsOperations = (BlockFileSystemOperations) fileSystem.getFileSystemOperations();
        SeekableByteChannel read = fsOperations.readFile(request.getFilename());

        SeekableByteChannel file = readJbFile();

        assertChannelsContentEquals(file, read);
        file.close();
        fileSystem.close();
    }

    @Test
    @DisplayName("File create and checksum / cat5.jpg")
    void testCreateAndChecksum() throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4));

        CreateFileRequest request = createFileRequest(readCat5());

        fileSystem.getFileSystemOperations().createFile(request);

        BlockFileSystemOperations fsOperations = (BlockFileSystemOperations) fileSystem.getFileSystemOperations();
        String actualChecksum = fsOperations.checksum(request.getFilename());

        assertEquals(CAT5_MD5, actualChecksum);

        fileSystem.close();
    }

    @Test
    @DisplayName("File read, and create, and reopen fs / Small file")
    void testCreateAndReadSmallFile() throws Exception {
        CreateFileSystemRequest fsRequest = getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4);
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(fsRequest);

        CreateFileRequest request = createFileRequest(readFileMd());

        fileSystem.getFileSystemOperations().createFile(request);

        SeekableByteChannel file = readFileMd();

        BlockAllocatedSpace expectedBlocks = new BlockAllocatedSpace(BlockSize.KB_4, AllocatedSpace.builder()
                .data(file)
                .build());

        FileSystem open = openFileSystemOperation.open(fsRequest.getWhereToStore());
        SeekableByteChannel read = open.getFileSystemOperations().readFile(request.getFilename());
        BlockAllocatedSpace actualBlocks = new BlockAllocatedSpace(BlockSize.KB_4, AllocatedSpace.builder()
                .data(read)
                .build());

        assertAllocatedSpaceEquals(expectedBlocks, actualBlocks);
        file.close();
        fileSystem.close();
    }

    @Test
    @DisplayName("File create and delete / Big big file")
    void testCreateAndDelete() throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4));

        CreateFileRequest request = createFileRequest(readJbFile());

        fileSystem.getFileSystemOperations().createFile(request);

        BlockFileSystemOperations fsOperations = (BlockFileSystemOperations) fileSystem.getFileSystemOperations();
        fsOperations.deleteFile(request.getFilename());
        assertThrows(FileNotFoundException.class, () -> {
            try (SeekableByteChannel file = fsOperations.readFile(request.getFilename())) {
            }
        });

        fileSystem.close();
    }

    @Test
    @DisplayName("File create and update / Big big file / shrink")
    void testCreateAndUpdateShrink() throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4));

        CreateFileRequest request = createFileRequest(readJbFile());

        ExtendedFileSystemOperations fsOperations = fileSystem.getFileSystemOperations();
        fileSystem.getFileSystemOperations().createFile(request);

        String filename = request.getFilename();
        UpdateFileRequest updateFileRequest = UpdateFileRequest.builder()
                .filename(filename)
                .content(readCvFile())
                .build();

        fsOperations.updateFile(updateFileRequest);

        assertChannelsContentEquals(readCvFile(), fsOperations.readFile(filename));
        fileSystem.close();
    }

    @Test
    @DisplayName("File create and update / Big big file / extend file")
    void testCreateAndUpdateExtend() throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4));

        CreateFileRequest request = createFileRequest(readCvFile());

        ExtendedFileSystemOperations fsOperations = fileSystem.getFileSystemOperations();
        fsOperations.createFile(request);

        String filename = request.getFilename();
        UpdateFileRequest updateFileRequest = UpdateFileRequest.builder()
                .filename(filename)
                .content(readJbFile())
                .build();

        fsOperations.updateFile(updateFileRequest);

        assertChannelsContentEquals(readJbFile(), fsOperations.readFile(filename));
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

    @Test
    @DisplayName("File must be moved")
    void testMoveFile() throws Exception {
        CreateFileRequest request = createFileRequest(readFileMd());

        fileSystem.getFileSystemOperations().createFile(request);
        fileSystem.getFileSystemOperations().moveFile(request.getFilename(), "moved");

        SeekableByteChannel file = fileSystem.getFileSystemOperations().readFile("moved");

        assertAllocatedSpaceEquals(BlockAllocatedSpace.of(readFileMd()), BlockAllocatedSpace.of(file));

        assertThrows(
                FileNotFoundException.class,
                () -> fileSystem.getFileSystemOperations().readFile(request.getFilename())
        );
    }

    @Test
    @DisplayName("Check a series of file creations, deletions, updates and reads")
    void testComplexBehavior() throws IOException {
        ExtendedFileSystemOperations operations = fileSystem.getFileSystemOperations();

        CreateFileRequest createCat1 = createFileRequest(readCat1());
        operations.createFile(createCat1);

        CreateFileRequest createCat2 = createFileRequest(readCat2());
        operations.createFile(createCat2);

        operations.deleteFile(createCat2.getFilename());

        assertThrows(FileNotFoundException.class, () -> operations.readFile(createCat2.getFilename()));

        operations.updateFile(UpdateFileRequest.builder()
                .filename(createCat1.getFilename())
                .content(readCat2())
                .build());

        assertChannelsContentEquals(readCat2(), operations.readFile(createCat1.getFilename()));

        operations.updateFile(UpdateFileRequest.builder()
                .filename(createCat1.getFilename())
                .content(readCat4())
                .build());

        assertChannelsContentEquals(readCat4(), operations.readFile(createCat1.getFilename()));

        operations.updateFile(UpdateFileRequest.builder()
                .filename(createCat1.getFilename())
                .content(readCat3())
                .build());

        assertChannelsContentEquals(readCat3(), operations.readFile(createCat1.getFilename()));

        CreateFileRequest createCat5 = createFileRequest(readCat5());
        operations.createFile(createCat5);

        assertChannelsContentEquals(readCat5(), operations.readFile(createCat5.getFilename()));

        CreateFileRequest createCat4 = createFileRequest(readCat4());
        operations.createFile(createCat4);

        operations.deleteFile(createCat5.getFilename());

        operations.updateFile(UpdateFileRequest.builder()
                .content(readJbFile())
                .filename(createCat4.getFilename())
                .build());

        assertChannelsContentEquals(readJbFile(), operations.readFile(createCat4.getFilename()));
    }
}
