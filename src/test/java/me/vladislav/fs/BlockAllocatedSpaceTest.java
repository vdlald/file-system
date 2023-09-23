package me.vladislav.fs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockAllocatedSpaceTest extends AbstractFileSystemTest {

    @Test
    @DisplayName("BlockAllocatedSpace / Read block")
    void testReadBlock() throws Exception {
        AllocatedSpace allocatedSpace = fileSystem.getAllocatedSpace()
                .withStartOffset(FileSystem.Metadata.TOTAL_SIZE);

        BlockAllocatedSpace blockAllocatedSpace = new BlockAllocatedSpace(BlockSize.KB_4, allocatedSpace);

        String expected = UUID.randomUUID().toString();
        allocatedSpace.position(0);
        allocatedSpace.write(ByteBuffer.wrap(expected.getBytes(UTF_8)));

        byte[] block = blockAllocatedSpace.readBlock(0).array();
        String actualData = new String(Arrays.copyOfRange(block, 0, expected.length()), UTF_8);
        byte[] zeroData = Arrays.copyOfRange(block, expected.length(), block.length);

        assertEquals(expected, actualData);

        byte expectedB = 0;
        for (byte b : zeroData) {
            assertEquals(expectedB, b);
        }
    }

    @Test
    @DisplayName("BlockAllocatedSpace / Write block")
    void testWriteBlock() throws Exception {
        AllocatedSpace allocatedSpace = fileSystem.getAllocatedSpace()
                .withStartOffset(FileSystem.Metadata.TOTAL_SIZE);

        BlockAllocatedSpace blockAllocatedSpace = new BlockAllocatedSpace(BlockSize.KB_4, allocatedSpace);

        String expected = UUID.randomUUID().toString();
        blockAllocatedSpace.writeBlock(1, ByteBuffer.wrap(expected.getBytes(UTF_8)));
        ByteBuffer actualRaw = blockAllocatedSpace.readBlock(1);

        String actual = new String(Arrays.copyOfRange(actualRaw.array(), 0, expected.length()), UTF_8);
        assertEquals(expected, actual);

        byte[] zeroData = Arrays.copyOfRange(actualRaw.array(), expected.length(), actualRaw.array().length);

        byte expectedB = 0;
        for (byte b : zeroData) {
            assertEquals(expectedB, b);
        }
    }
}
