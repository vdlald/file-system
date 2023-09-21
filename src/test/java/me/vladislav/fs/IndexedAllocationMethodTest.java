package me.vladislav.fs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexedAllocationMethodTest extends AbstractFileSystemTest {

    @Test
    @DisplayName("Метод аллокации / Индексированный метод / Чтение блока")
    void testReadBlock() throws Exception {
        testWithFileSystem(fileSystem -> {
            fileSystem.setStartPosition();

            IndexedAllocationMethod method = IndexedAllocationMethod.builder()
                    .fileSystem(fileSystem)
                    .blockSize(IndexedAllocationMethod.BlockSize.KB_4)
                    .build();

            String expected = UUID.randomUUID().toString();
            SeekableByteChannel content = fileSystem.getContent();
            content.write(ByteBuffer.wrap(expected.getBytes(StandardCharsets.UTF_8)));

            byte[] block = method.readBlock(0).array();
            String actualData = new String(Arrays.copyOfRange(block, 0, expected.length()), StandardCharsets.UTF_8);
            byte[] zeroData = Arrays.copyOfRange(block, expected.length(), block.length);

            assertEquals(expected, actualData);

            byte expectedB = 0;
            for (byte b : zeroData) {
                assertEquals(expectedB, b);
            }
        });
    }
}
