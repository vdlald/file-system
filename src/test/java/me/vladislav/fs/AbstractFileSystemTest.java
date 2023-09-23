package me.vladislav.fs;

import me.vladislav.fs.operations.CreateFileSystemOperation;
import me.vladislav.fs.operations.OpenFileSystemOperation;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.CreateFileSystemRequest;
import me.vladislav.fs.util.ByteBufferUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AbstractFileSystemTest {

    public static final int KB_2 = 1024 * 2;
    public static final int KB_8 = 1024 * 8;
    public static final int MB_8 = 1048576;

    @Autowired
    protected OpenFileSystemOperation openFileSystemOperation;

    @Autowired
    protected CreateFileSystemOperation createFileSystemOperation;

    protected FileSystem fileSystem;
    protected static Path tempDirectory;

    @BeforeAll
    static void setUpData() throws IOException {
        tempDirectory = Files.createTempDirectory("test-fs-jb_");
    }

    @BeforeEach
    void setUp() throws IOException {
        CreateFileSystemRequest request = getCreateFileSystemRequest();
        fileSystem = createFileSystemOperation.createFileSystem(request);
    }

    @AfterEach
    void cleanUp() throws IOException {
        fileSystem.close();
    }

    @Nonnull
    protected CreateFileSystemRequest getCreateFileSystemRequest() {
        return CreateFileSystemRequest.builder()
                .whereToStore(tempDirectory)
                .fileSystemName("fs_" + UUID.randomUUID())
                .initialSizeInBytes(MB_8)
                .build();
    }

    protected CreateFileRequest createFileRequest(String content) throws IOException {
        return CreateFileRequest.builder()
                .filename(UUID.randomUUID().toString())
                .content(createTempFile(content))
                .build();
    }

    protected SeekableByteChannel createTempFile(String content) throws IOException {
        Path tempFile = Files.createTempFile("temp", "suff");
        SeekableByteChannel seekableByteChannel = Files.newByteChannel(tempFile, READ, WRITE);
        seekableByteChannel.write(ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8)));
        return seekableByteChannel.position(0);
    }

    protected void assertIsEmpty(ByteBuffer buffer) {
        assertTrue(ByteBufferUtils.isEmpty(buffer));
    }

    protected void assertIsNotEmpty(ByteBuffer buffer) {
        assertFalse(ByteBufferUtils.isEmpty(buffer));
    }
}
