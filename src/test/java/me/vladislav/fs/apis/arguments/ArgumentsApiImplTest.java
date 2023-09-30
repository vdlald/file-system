package me.vladislav.fs.apis.arguments;

import me.vladislav.fs.AbstractFileSystemTest;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.util.ByteBufferUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.READ;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    @DisplayName("The correct check amount must be calculated")
    void testChecksum() throws Exception {
        Path fsPath = createFSRequest.getWhereToStore();
        TempFile tempFile = createTempFile(readCat5());

        Path temp = Files.createTempFile("temp", "suff");

        ApplicationArguments createFile = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=create-file",
                "--file-in=" + tempFile.path(),
                "--filename=file"
        );
        argumentsApi.run(createFile);

        PrintStream printStream = new PrintStream(temp.toFile());
        System.setOut(printStream);

        ApplicationArguments checksum = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=md5-checksum",
                "--filename=file"
        );
        argumentsApi.run(checksum);

        printStream.close();

        SeekableByteChannel out = Files.newByteChannel(temp, READ);
        AllocatedSpace allocatedSpace = AllocatedSpace.builder().data(out).build();

        ByteBuffer read = allocatedSpace.read(CAT5_MD5.length());
        assertEquals(CAT5_MD5, ByteBufferUtils.readToString(read));
    }
}
