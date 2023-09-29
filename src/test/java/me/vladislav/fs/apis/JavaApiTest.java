package me.vladislav.fs.apis;

import me.vladislav.fs.AbstractFileSystemTest;
import me.vladislav.fs.BlockAllocatedSpace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;

public class JavaApiTest extends AbstractFileSystemTest {

    @Autowired
    private JavaApi javaApi;

    @Test
    @DisplayName("Must be creating and reading a file")
    void testCreateAndRead() throws IOException {
        Path fsPath = createFSRequest.getWhereToStore();

        javaApi.createFile(CrudApplicationApi.CreateFileRequest.builder()
                .fsPath(fsPath)
                .filename("file")
                .content(readFileMd())
                .build());

        SeekableByteChannel actual = javaApi.readFile(CrudApplicationApi.ReadFileRequest.builder()
                .fsPath(fsPath)
                .filename("file")
                .build());

        SeekableByteChannel expected = readFileMd();

        assertAllocatedSpaceEquals(BlockAllocatedSpace.of(expected), BlockAllocatedSpace.of(actual));
    }
}
