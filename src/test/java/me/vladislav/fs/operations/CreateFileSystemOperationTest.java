package me.vladislav.fs.operations;

import me.vladislav.fs.AbstractFileSystemTest;
import me.vladislav.fs.FileSystem;
import me.vladislav.fs.requests.CreateFileSystemRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CreateFileSystemOperationTest extends AbstractFileSystemTest {

    @ParameterizedTest
    @ValueSource(ints = {MB_8, MB_8 * 2})
    @DisplayName("File system / Must create a file for the file system")
    void testCreateFileSystemFile(int size) throws IOException {
        String fsName = "test file system" + UUID.randomUUID();
        CreateFileSystemRequest request = CreateFileSystemRequest.builder()
                .whereToStore(tempDirectory)
                .fileSystemName(fsName)
                .initialSizeInBytes(size)
                .build();

        try (FileSystem ignored = createFileSystemOperation.createFileSystem(request)) {
            Path pathToFs = tempDirectory.resolve(fsName);
            assertTrue(Files.exists(pathToFs));
            assertEquals(size, Files.size(pathToFs));
        }
    }

    @Test
    @DisplayName("File System / When creating an FS, a metadata must be saved")
    void testCreateFileSystemFileWithMetadata() {
        FileSystem.Metadata metadata = fileSystem.getMetadata();
        assertNotNull(metadata.getCreatedAt());
    }
}
