package me.vladislav.fs;

import me.vladislav.fs.operations.my.MyFileSystemOperations;
import me.vladislav.fs.util.ByteBufferUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexedBlockAllocatedSpaceTest extends AbstractFileSystemTest {

    @Test
    @DisplayName("IndexedBlockAllocatedSpace / Must write and read block")
    void testWrite() throws IOException {
        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
        IndexedBlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();

        assertTrue(ByteBufferUtils.isEmpty(allocatedSpace.readBlock(0)));

        allocatedSpace.writeBlock(0, ByteBuffer.wrap("some data".getBytes(StandardCharsets.UTF_8)));
        assertFalse(ByteBufferUtils.isEmpty(allocatedSpace.readBlock(0)));
    }
}
