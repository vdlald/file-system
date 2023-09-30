package me.vladislav.fs.apis.arguments;

import me.vladislav.fs.AbstractFileSystemTest;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.util.ByteBufferUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.READ;
import static org.junit.jupiter.api.Assertions.*;

public class ArgumentsApiImplTest extends AbstractFileSystemTest {

    @Autowired
    private ArgumentsApiImpl argumentsApi;

    protected Path tempOut;
    protected Path tempErr;

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();

        tempOut = Files.createTempFile("temp", "suff");
        tempErr = Files.createTempFile("temp", "suff");

        PrintStream outPrint = new PrintStream(tempOut.toFile());
        PrintStream errPrint = new PrintStream(tempErr.toFile());

        System.setOut(outPrint);
        System.setErr(errPrint);
    }

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

    @Test
    @DisplayName("There must not be a java exception in the output")
    void testArgumentValidationException() throws Exception {
        Path fsPath = createFSRequest.getWhereToStore();

        ApplicationArguments checksum = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=md5-checksum",
                "--file-name=file"
        );
        argumentsApi.run(checksum);

        SeekableByteChannel out = Files.newByteChannel(tempOut, READ);
        AllocatedSpace allocatedSpace = AllocatedSpace.builder().data(out).build();

        ByteBuffer read = allocatedSpace.read(1000);
        assertTrue(ByteBufferUtils.isEmpty(read));
        allocatedSpace.close();

        out = Files.newByteChannel(tempErr, READ);
        allocatedSpace = AllocatedSpace.builder().data(out).build();

        read = allocatedSpace.read(1000);
        String actual = ByteBufferUtils.readToString(read);
        assertEquals("argument \"filename\" incorrect because it's empty\n", actual);
        assertFalse(ByteBufferUtils.isEmpty(read.rewind()));

        out = Files.newByteChannel(Path.of(ArgumentsApiExceptionHandling.FILE_ST_NAME), READ);
        allocatedSpace = AllocatedSpace.builder().data(out).build();

        read = allocatedSpace.read(1000);
        assertFalse(ByteBufferUtils.isEmpty(read));
    }

    @Test
    @DisplayName("The file must be updated correctly")
    void testUpdate() throws Exception {
        Path fsPath = createFSRequest.getWhereToStore();
        TempFile tempFile = createTempFile("some content");

        ApplicationArguments createFile = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=create-file",
                "--file-in=" + tempFile.path(),
                "--filename=file"
        );
        argumentsApi.run(createFile);

        TempFile updTempFile = createTempFile(readCat5());

        ApplicationArguments updateFile = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=update-file",
                "--file-in=" + updTempFile.path(),
                "--filename=file"
        );
        argumentsApi.run(updateFile);

        TempFile outTemp = createTempFile("");
        ApplicationArguments readFile = new DefaultApplicationArguments(
                "--fs=" + fsPath,
                "--operation=read-file",
                "--file-out=" + outTemp.path(),
                "--filename=file"
        );
        argumentsApi.run(readFile);


        assertChannelsContentEquals(outTemp.content().position(0), Files.newByteChannel(updTempFile.path()));
    }
}
