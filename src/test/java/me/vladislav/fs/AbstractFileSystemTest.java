package me.vladislav.fs;

import me.vladislav.fs.operations.CreateFileSystemOperation;
import me.vladislav.fs.operations.OpenFileSystemOperation;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.CreateFileSystemRequest;
import me.vladislav.fs.util.ByteBufferUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AbstractFileSystemTest {

    public static final int KB_2 = 1024 * 2;
    public static final int KB_8 = 1024 * 8;
    public static final int MB_1 = 1048576;
    public static final int MB_8 = MB_1 * 8;

    public static final String CAT5_MD5 = "86adc15f5f2aec4fab91b7ff19128e65";

    @Autowired
    protected OpenFileSystemOperation openFileSystemOperation;

    @Autowired
    protected CreateFileSystemOperation createFileSystemOperation;

    protected FileSystem fileSystem;
    protected CreateFileSystemRequest createFSRequest;
    protected static Path tempDirectory;

    @BeforeAll
    static void setUpData() throws IOException {
        tempDirectory = Files.createTempDirectory("test-fs-jb_");
    }

    @BeforeEach
    protected void setUp() throws IOException {
        createFSRequest = getCreateFileSystemRequest();
        fileSystem = createFileSystemOperation.createFileSystem(createFSRequest);
    }

    @AfterEach
    protected void cleanUp() throws IOException {
        fileSystem.close();
    }

    @Nonnull
    protected CreateFileSystemRequest getCreateFileSystemRequest() {
        return CreateFileSystemRequest.builder()
                .whereToStore(tempDirectory.resolve("fs_" + UUID.randomUUID()))
                .initialSizeInBytes(MB_1)
                .build();
    }

    protected CreateFileRequest createFileRequest(String content) throws IOException {
        return CreateFileRequest.builder()
                .filename(UUID.randomUUID().toString())
                .content(createTempFile(content).content())
                .build();
    }

    protected CreateFileRequest createFileRequest(SeekableByteChannel file) throws IOException {
        return CreateFileRequest.builder()
                .filename(UUID.randomUUID().toString())
                .content(file)
                .build();
    }

    protected TempFile createTempFile(String content) throws IOException {
        Path tempFile = Files.createTempFile("temp", "suff");
        SeekableByteChannel seekableByteChannel = Files.newByteChannel(tempFile, READ, WRITE);
        seekableByteChannel.write(ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8)));
        return new TempFile(tempFile, seekableByteChannel.position(0));
    }

    protected TempFile createTempFile(SeekableByteChannel fromChannel) throws IOException {
        Path tempFile = Files.createTempFile("temp", "suff");
        SeekableByteChannel seekableByteChannel = Files.newByteChannel(tempFile, READ, WRITE);

        Iterator<ByteBuffer> iterator = BlockAllocatedSpace.of(fromChannel).contentIterator();
        while (iterator.hasNext()) {
            seekableByteChannel.write(iterator.next());
        }

        return new TempFile(tempFile, seekableByteChannel.position(0));
    }

    protected void assertIsEmpty(ByteBuffer buffer) {
        assertTrue(ByteBufferUtils.isEmpty(buffer));
    }

    protected void assertIsNotEmpty(ByteBuffer buffer) {
        assertFalse(ByteBufferUtils.isEmpty(buffer));
    }

    protected void assertAllocatedSpaceEquals(BlockAllocatedSpace expected, BlockAllocatedSpace actual) {
        assertEquals(expected.blocksAmount, actual.blocksAmount);
        while (expected.hasNextBlock()) {
            ByteBuffer e = expected.readBlock();
            ByteBuffer a = actual.readBlock();
            assertEquals(e, a,
                    () -> "expected: %s\nactual: %s".formatted(
                            ByteBufferUtils.readToString(e), ByteBufferUtils.readToString(a)));
        }
    }

    protected void assertChannelsContentEquals(
            SeekableByteChannel expected,
            SeekableByteChannel actual
    ) throws IOException {
        int read = 0;
        do {
            ByteBuffer buffer1 = ByteBuffer.allocate(KB_8);
            ByteBuffer buffer2 = ByteBuffer.allocate(KB_8);
            assertEquals(expected.read(buffer1), actual.read(buffer2));
            assertEquals(buffer1.rewind(), buffer2.rewind());
            read += KB_8;
        } while (expected.size() > read);
    }

    protected SeekableByteChannel readCat1() throws IOException {
        return readFile("/files/cat1.webp");
    }

    protected SeekableByteChannel readCat2() throws IOException {
        return readFile("/files/cat2.webp");
    }

    protected SeekableByteChannel readCat3() throws IOException {
        return readFile("/files/cat3.jpeg");
    }

    protected SeekableByteChannel readCat4() throws IOException {
        return readFile("/files/cat4.webp");
    }

    protected SeekableByteChannel readCat5() throws IOException {
        return readFile("/files/cat5.jpg");
    }

    protected SeekableByteChannel readCvFile() throws IOException {
        return readFile("/files/vladislav_golubinov_cv_for_jb.pdf");
    }

    protected SeekableByteChannel readJbFile() throws IOException {
        return readFile("/files/jb.png");
    }

    protected SeekableByteChannel readFileMd() throws IOException {
        return readFile("/files/file.md");
    }

    protected SeekableByteChannel readFile(String name) throws IOException {
        InputStream input = AbstractFileSystemTest.class.getResourceAsStream(name);
        return new SeekableInMemoryByteChannel(IOUtils.toByteArray(input));
    }

    protected record TempFile(
            Path path,
            SeekableByteChannel content
    ) {
    }
}
