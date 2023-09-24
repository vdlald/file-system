package me.vladislav.fs;

import me.vladislav.fs.util.ByteBufferUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockAllocatedSpaceTest extends AbstractFileSystemTest {

    @Test
    @DisplayName("Read block")
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
    @DisplayName("Write block")
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

    @Test
    @DisplayName("Must write and read")
    void testRW() throws IOException {
        SeekableByteChannel tempFile = createTempFile("");
        BlockAllocatedSpace allocatedSpace = BlockAllocatedSpace.of(tempFile);

        ByteBuffer expected = ByteBuffer.allocate(BlockSize.KB_4.getBlockSizeInBytes())
                .put("some content".getBytes(UTF_8))
                .rewind();

        allocatedSpace.writeBlock(1, expected);

        ByteBuffer actual = allocatedSpace.readBlock(1);

        assertEquals(expected.rewind(), actual);
    }

    @Test
    @DisplayName("The selected block must be filled with zeros")
    void testFillBlockZeros() throws IOException {
        SeekableByteChannel cv = readCvFile();

        BlockAllocatedSpace allocatedSpace = BlockAllocatedSpace.of(cv);

        allocatedSpace.fillBlockZeros(0);

        AllocatedSpace secondAllocated = AllocatedSpace.builder().data(cv).build();
        ByteBufferUtils.isEmpty(secondAllocated.read(BlockSize.KB_4.getBlockSizeInBytes()));
    }


    @Test
    @DisplayName("The number of blocks should increase with expansion")
    void testBlocksAmount() throws IOException {
        SeekableByteChannel tempFile = createTempFile("");
        BlockAllocatedSpace allocatedSpace = BlockAllocatedSpace.of(tempFile);

        assertEquals(0, allocatedSpace.getBlocksAmount());

        ByteBuffer expected = ByteBuffer.allocate(BlockSize.KB_4.getBlockSizeInBytes())
                .put("some content".getBytes(UTF_8))
                .rewind();

        allocatedSpace.writeBlock(1, expected);

        assertEquals(2, allocatedSpace.getBlocksAmount());
    }
}
