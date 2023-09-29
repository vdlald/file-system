package me.vladislav.fs.apis.arguments;

import me.vladislav.fs.AbstractFileSystemTest;
import me.vladislav.fs.BlockAllocatedSpace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.nio.file.Files;
import java.nio.file.Path;

public class ArgumentsApiImplTest extends AbstractFileSystemTest {

    @Autowired
    private ArgumentsApiImpl argumentsApi;

    @Test
    @DisplayName("Must be creating and reading a file")
    void testCreateAndRead() throws Exception {
        Path fsPath = createFSRequest.getWhereToStore();
        TempFile tempFile = createTempFile("some content");

        ApplicationArguments createFile = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=create-file",
                "--file-in=" + tempFile.path(),
                "--filename=file"
        );
        argumentsApi.run(createFile);

        TempFile outTemp = createTempFile("");
        ApplicationArguments readFile = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=read-file",
                "--file-out=" + outTemp.path(),
                "--filename=file"
        );
        argumentsApi.run(readFile);

        BlockAllocatedSpace expected = BlockAllocatedSpace.of(Files.newByteChannel(tempFile.path()));

        assertAllocatedSpaceEquals(expected, BlockAllocatedSpace.of(outTemp.content()));
    }

    @Test
    @DisplayName("File must be moved")
    void testMoveFile() throws Exception {
        Path fsPath = createFSRequest.getWhereToStore();
        TempFile tempFile = createTempFile("some content");

        ApplicationArguments createFile = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=create-file",
                "--file-in=" + tempFile.path(),
                "--filename=file"
        );
        argumentsApi.run(createFile);

        ApplicationArguments moveFile = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=move-file",
                "--filename=file",
                "--new-filename=moved"
        );
        argumentsApi.run(moveFile);

        TempFile outTemp = createTempFile("");
        ApplicationArguments readFile = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=read-file",
                "--file-out=" + outTemp.path(),
                "--filename=moved"
        );
        argumentsApi.run(readFile);

        BlockAllocatedSpace expected = BlockAllocatedSpace.of(Files.newByteChannel(tempFile.path()));

        assertAllocatedSpaceEquals(expected, BlockAllocatedSpace.of(outTemp.content()));
    }
}
