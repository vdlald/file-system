package me.vladislav.fs.apis.arguments;

import me.vladislav.fs.AbstractFileSystemTest;
import me.vladislav.fs.BlockAllocatedSpace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArgumentsApiTest extends AbstractFileSystemTest {

    @Autowired
    private ArgumentsApi argumentsApi;

    @Test
    @DisplayName("Must be creating and reading a file")
    void testCreateAndRead() throws IOException {
        Path fsPath = createFSRequest.getWhereToStore();
        TempFile tempFile = createTempFile("some content");

        ApplicationArguments createFile = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=create-file",
                "--file_in=" + tempFile.path(),
                "--filename=file"
        );
        argumentsApi.run(createFile);

        TempFile outTemp = createTempFile("");
        ApplicationArguments readFile = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=read-file",
                "--file_out=" + outTemp.path(),
                "--filename=file"
        );
        argumentsApi.run(readFile);

        BlockAllocatedSpace expected = BlockAllocatedSpace.of(Files.newByteChannel(tempFile.path()));

        assertAllocatedSpaceEquals(expected, BlockAllocatedSpace.of(outTemp.content()));
    }
}
