package me.vladislav.fs;

import me.vladislav.fs.operations.CreateFileSystemOperation;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class AbstractFileSystemTest {

    public static final int MB_8 = 1048576;

    protected final CreateFileSystemOperation createFileSystemOperation = new CreateFileSystemOperation();

    protected Path tempDirectory;

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("test-fs-jb_");
    }

    protected void testWithFileSystem(FileSystemTestFunction testFunction) throws Exception {
        CreateFileSystemRequest request = CreateFileSystemRequest.builder()
                .whereToStore(tempDirectory)
                .fileSystemName(UUID.randomUUID().toString())
                .initialSizeInBytes(MB_8)
                .build();

        try (FileSystem fileSystem = createFileSystemOperation.createFileSystem(request)) {
            testFunction.test(fileSystem);
        }
    }

    @FunctionalInterface
    protected interface FileSystemTestFunction {

        void test(FileSystem fileSystem) throws Exception;
    }
}
