package me.vladislav.fs.operations;

import me.vladislav.fs.CreateFileSystemRequest;
import me.vladislav.fs.FileSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateFileSystemOperationTest {

    private static final int MB_8 = 1048576;

    private final CreateFileSystemOperation createFileSystemOperation = new CreateFileSystemOperation();

    private Path tempDirectory;

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("test-fs-jb_");
    }

    @Test
    @DisplayName("Файловая система / Должен создаться файл под файловую систему")
    void testCreateFileSystemOperation() throws IOException {
        CreateFileSystemRequest request = CreateFileSystemRequest.builder()
                .whereToStore(tempDirectory)
                .fileSystemName("test file system")
                .initialSizeInBytes(MB_8)
                .build();

        try (FileSystem fileSystem = createFileSystemOperation.createFileSystem(request)) {
            assertTrue(Files.exists(fileSystem.getWhereStored()));
            assertEquals(MB_8, Files.size(fileSystem.getWhereStored()));
        }
    }
}
