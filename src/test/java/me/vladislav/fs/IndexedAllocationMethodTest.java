package me.vladislav.fs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
            content.write(ByteBuffer.wrap(expected.getBytes(UTF_8)));

            byte[] block = method.readBlock(0).array();
            String actualData = new String(Arrays.copyOfRange(block, 0, expected.length()), UTF_8);
            byte[] zeroData = Arrays.copyOfRange(block, expected.length(), block.length);

            assertEquals(expected, actualData);

            byte expectedB = 0;
            for (byte b : zeroData) {
                assertEquals(expectedB, b);
            }
        });
    }

    @Test
    @DisplayName("Метод аллокации / Индексированный метод / Запись в блок")
    void testWriteBlock() throws Exception {
        testWithFileSystem(fileSystem -> {
            fileSystem.setStartPosition();

            IndexedAllocationMethod method = IndexedAllocationMethod.builder()
                    .fileSystem(fileSystem)
                    .blockSize(IndexedAllocationMethod.BlockSize.KB_4)
                    .build();

            String expected = UUID.randomUUID().toString();
            method.writeBlock(1, ByteBuffer.wrap(expected.getBytes(UTF_8)));
            ByteBuffer actualRaw = method.readBlock(1);

            String actual = new String(Arrays.copyOfRange(actualRaw.array(), 0, expected.length()), UTF_8);
            assertEquals(expected, actual);

            byte[] zeroData = Arrays.copyOfRange(actualRaw.array(), expected.length(), actualRaw.array().length);

            byte expectedB = 0;
            for (byte b : zeroData) {
                assertEquals(expectedB, b);
            }
        });
    }
}
