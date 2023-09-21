package me.vladislav.fs.operations;

import me.vladislav.fs.AbstractFileSystemTest;
import me.vladislav.fs.CreateFileSystemRequest;
import me.vladislav.fs.FileSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class CreateFileSystemOperationTest extends AbstractFileSystemTest {

    @ParameterizedTest
    @ValueSource(ints = {MB_8, MB_8 * 2})
    @DisplayName("Файловая система / Должен создаться файл под файловую систему")
    void testCreateFileSystemFile(int size) throws IOException {
        CreateFileSystemRequest request = CreateFileSystemRequest.builder()
                .whereToStore(tempDirectory)
                .fileSystemName("test file system")
                .initialSizeInBytes(size)
                .build();

        try (FileSystem fileSystem = createFileSystemOperation.createFileSystem(request)) {
            assertTrue(Files.exists(fileSystem.getWhereStored()));
            assertEquals(MB_8, Files.size(fileSystem.getWhereStored()));
        }
    }

    @Test
    @DisplayName("Файловая система / При создании ФС должна быть сохранена метадата")
    void testCreateFileSystemFileWithMetadata() throws Exception {
        testWithFileSystem(fileSystem -> {
            FileSystem.Metadata metadata = fileSystem.getMetadata();
            assertNotNull(metadata.getCreatedAt());
        });
    }
}
