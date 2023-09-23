package me.vladislav.fs.blocks.serializers;

import me.vladislav.fs.AbstractFileSystemTest;
import me.vladislav.fs.blocks.FileContentIndexBlock;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BytesSerializerTest extends AbstractFileSystemTest {

    @Autowired
    private FileDescriptorBytesSerializer fileDescriptorBytesSerializer;

    @Autowired
    private FileDescriptorsBlockBytesSerializer fileDescriptorsBlockBytesSerializer;

    @Autowired
    private FileContentIndexBlockBytesSerializer fileContentIndexBlockBytesSerializer;

    @Test
    @DisplayName("FileDescriptor / Must converting to bytes and from bytes")
    void testFileDescriptorConvert() {
        String filename = UUID.randomUUID().toString();

        FileDescriptor expected = FileDescriptor.builder()
                .filename(filename)
                .fileBlockIndex(4)
                .build();

        ByteBuffer descriptorRaw = fileDescriptorBytesSerializer.toByteBuffer(expected);

        FileDescriptor actual = fileDescriptorBytesSerializer.from(descriptorRaw);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("FileDescriptorsBlock / Must converting to bytes and from bytes")
    void testFileDescriptorsBlockConvert() {
        FileDescriptorsBlock block = new FileDescriptorsBlock();

        FileDescriptor expected1 = FileDescriptor.builder()
                .filename(UUID.randomUUID().toString())
                .fileBlockIndex(4)
                .build();
        FileDescriptor expected2 = FileDescriptor.builder()
                .filename(UUID.randomUUID().toString())
                .fileBlockIndex(7)
                .build();

        block.addDescriptor(expected1);
        block.addDescriptor(expected2);

        ByteBuffer raw = fileDescriptorsBlockBytesSerializer.toByteBuffer(block);
        FileDescriptorsBlock actual = fileDescriptorsBlockBytesSerializer.from(raw);

        assertEquals(block, actual);
    }

    @Test
    @DisplayName("FileDescriptorsBlock / From empty bytes")
    void testFileDescriptorsBlockConvertEmpty() {
        ByteBuffer allocate = ByteBuffer.allocate(FileDescriptorsBlock.TOTAL_SIZE);
        FileDescriptorsBlock block = fileDescriptorsBlockBytesSerializer.from(allocate);
        assertTrue(block.isEmpty());
    }


    @Test
    @DisplayName("FileContentIndexBlock / Must converting to bytes and from bytes")
    void testFileContentIndexBlockConvert() {
        FileContentIndexBlock expected = new FileContentIndexBlock();
        expected.addBlockPointer(10);
        expected.addBlockPointer(13);
        expected.addBlockPointer(39);
        expected.setNextIndexBlock(12);

        FileContentIndexBlock actual = fileContentIndexBlockBytesSerializer.from(
                fileContentIndexBlockBytesSerializer.toByteBuffer(expected));

        assertEquals(expected, actual);
    }
}
