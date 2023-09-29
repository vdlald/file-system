package me.vladislav.fs.operations;

import me.vladislav.fs.*;
import me.vladislav.fs.blocks.FileContentIndexBlock;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import me.vladislav.fs.blocks.serializers.FileContentIndexBlockBytesSerializer;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import me.vladislav.fs.exceptions.FileNotFoundException;
import me.vladislav.fs.operations.my.MyFileSystemOperations;
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

public class MyFileSystemOperationsTest extends AbstractFileSystemTest {

    @Autowired
    private FileContentIndexBlockBytesSerializer indexBlockBytesSerializer;

    @Autowired
    private FileDescriptorsBlockBytesSerializer fileDescriptorsBlockBytesSerializer;

    @Test
    @DisplayName("File creation / The descriptor must be written")
    void testCreateFileDescriptor() throws Exception {
        CreateFileRequest request = createFileRequest(" ");

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

    @Test
    @DisplayName("File creation / File content must be written / Big big file")
    void testCreateBigBigFileContent() throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4));

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

    @Test
    @DisplayName("File creation / Must save both files")
    void testCreateTwoFilesContent() throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4));

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

    @Test
    @DisplayName("File read and create / Big big file")
    void testCreateAndRead() throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4));

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

        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
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

    @Test
    @DisplayName("File create and update / Big big file / extend file")
    void testCreateAndUpdateExtend() throws Exception {
        FileSystem fileSystem = createFileSystemOperation.createFileSystem(getCreateFileSystemRequest()
                .withBlockSize(BlockSize.KB_4));

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

    @Test
    @DisplayName("Check a series of file creations, deletions, updates and reads")
    void testComplexBehavior() throws IOException {
        BlockAllocatedSpace cat2 = BlockAllocatedSpace.of(readCat2());
        BlockAllocatedSpace cat3 = BlockAllocatedSpace.of(readCat3());
        BlockAllocatedSpace cat4 = BlockAllocatedSpace.of(readCat4());
        BlockAllocatedSpace cat5 = BlockAllocatedSpace.of(readCat5());
        BlockAllocatedSpace jb = BlockAllocatedSpace.of(readJbFile());

        FileSystemOperations operations = fileSystem.getFileSystemOperations();

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

        assertAllocatedSpaceEquals(
                cat2,
                BlockAllocatedSpace.of(operations.readFile(createCat1.getFilename()))
        );

        operations.updateFile(UpdateFileRequest.builder()
                .filename(createCat1.getFilename())
                .content(readCat4())
                .build());

        assertAllocatedSpaceEquals(
                cat4,
                BlockAllocatedSpace.of(operations.readFile(createCat1.getFilename()))
        );

        operations.updateFile(UpdateFileRequest.builder()
                .filename(createCat1.getFilename())
                .content(readCat3())
                .build());

        BlockAllocatedSpace file1Actual = BlockAllocatedSpace.of(operations.readFile(createCat1.getFilename()));
        assertAllocatedSpaceEquals(cat3, file1Actual);
        file1Actual.close();

        CreateFileRequest createCat5 = createFileRequest(readCat5());
        operations.createFile(createCat5);

        BlockAllocatedSpace file2Actual = BlockAllocatedSpace.of(operations.readFile(createCat5.getFilename()));
        assertAllocatedSpaceEquals(cat5, file2Actual);

        CreateFileRequest createCat4 = createFileRequest(readCat4());
        operations.createFile(createCat4);

        operations.deleteFile(createCat5.getFilename());

        operations.updateFile(UpdateFileRequest.builder()
                .content(readJbFile())
                .filename(createCat4.getFilename())
                .build());

        BlockAllocatedSpace file3Actual = BlockAllocatedSpace.of(operations.readFile(createCat4.getFilename()));
        assertAllocatedSpaceEquals(jb, file3Actual);
    }
}
