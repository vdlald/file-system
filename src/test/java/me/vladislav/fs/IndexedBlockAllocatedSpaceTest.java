package me.vladislav.fs;

import me.vladislav.fs.operations.my.MyFileSystemOperations;
import me.vladislav.fs.util.ByteBufferUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexedBlockAllocatedSpaceTest extends AbstractFileSystemTest {

    @Test
    @DisplayName("Must write and read block")
    void testWrite() throws IOException {
        MyFileSystemOperations fsOperations = (MyFileSystemOperations) fileSystem.getFileSystemOperations();
        IndexedBlockAllocatedSpace allocatedSpace = fsOperations.getAllocatedSpace();

        assertTrue(ByteBufferUtils.isEmpty(allocatedSpace.readBlock(0)));

        allocatedSpace.writeBlock(0, ByteBuffer.wrap("some data".getBytes(StandardCharsets.UTF_8)));
        assertFalse(ByteBufferUtils.isEmpty(allocatedSpace.readBlock(0)));
    }

    @Test
    @DisplayName("When a block is deleted, it should be marked as free")
    void testFillBLockZerosMarkIndex() throws IOException {
        SeekableByteChannel file = createTempFile("some content").content();
        IndexedBlockAllocatedSpace allocatedSpace = IndexedBlockAllocatedSpace.of(file);

        assertFalse(allocatedSpace.isBlockFree(0));
        allocatedSpace.fillBlockZeros(0);
        assertTrue(allocatedSpace.isBlockFree(0));
    }

    @Test
    @DisplayName("When data is written to a block, it must be marked busy")
    void testWriteBlockMarkIndex() throws IOException {
        SeekableByteChannel file = createTempFile("").content();
        IndexedBlockAllocatedSpace allocatedSpace = IndexedBlockAllocatedSpace.of(file);

        allocatedSpace.fillBlockZeros(0);
        assertTrue(allocatedSpace.isBlockFree(0));

        allocatedSpace.writeBlock(0, ByteBuffer.wrap("some".getBytes(StandardCharsets.UTF_8)));
        assertFalse(allocatedSpace.isBlockFree(0));
    }

    @Test
    @DisplayName("When creating an object of a class, memory is scanned for free space")
    void testWhenCreate() throws IOException {
        SeekableByteChannel file = createTempFile("some content").content();
        IndexedBlockAllocatedSpace allocatedSpace = IndexedBlockAllocatedSpace.of(file);

        assertFalse(allocatedSpace.isBlockFree(0));
    }
}
