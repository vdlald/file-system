package me.vladislav.fs;

import me.vladislav.fs.operations.CreateFileSystemOperation;
import me.vladislav.fs.operations.OpenFileSystemOperation;
import me.vladislav.fs.requests.CreateFileSystemRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@SpringBootTest
public class AbstractFileSystemTest {

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
                .fileSystemName(UUID.randomUUID().toString())
                .initialSizeInBytes(MB_8)
                .build();
    }
}
