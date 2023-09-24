package me.vladislav.fs;

import me.vladislav.fs.requests.CreateFileSystemRequest;
import me.vladislav.fs.util.ByteBufferUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AllocatedSpaceTest extends AbstractFileSystemTest {

    @Test
    @DisplayName("Must write and read")
    void testRW() throws IOException {
        AllocatedSpace allocatedSpace = fileSystem.getAllocatedSpace();

        assertIsEmpty(allocatedSpace.position(0).read(100));

        allocatedSpace.position(0).writeString("some text");

        assertIsNotEmpty(allocatedSpace.position(0).read(100));

        ByteBuffer allocate = ByteBuffer.allocate(10);
        allocatedSpace.position(0).read(allocate);
        assertIsNotEmpty(allocate);

        ByteBuffer wrap = ByteBuffer.wrap("some text".getBytes(StandardCharsets.UTF_8));
        allocatedSpace.position(20).write(wrap);
        assertIsNotEmpty(allocatedSpace.position(20).read(20));
    }

    @Test
    @DisplayName("Must be reading with an offset")
    void testOffset() throws IOException {
        SeekableByteChannel file = createTempFile("some content");

        AllocatedSpace allocatedSpace = AllocatedSpace.builder()
                .data(file)
                .startOffset(5)
                .build();

        String actual = ByteBufferUtils.readToString(allocatedSpace.read(7));
        assertEquals("content", actual);

        allocatedSpace.close();
    }

    @Test
    @DisplayName("Mustn't allow to go outside of the offset")
    void testOffsetBreak() throws IOException {
        SeekableByteChannel file = createTempFile("some content");

        AllocatedSpace allocatedSpace = AllocatedSpace.builder()
                .data(file)
                .startOffset(5)
                .build();

        assertThrows(IndexOutOfBoundsException.class, () -> allocatedSpace.position(-5));

        allocatedSpace.close();
    }

    @Nonnull
    @Override
    protected CreateFileSystemRequest getCreateFileSystemRequest() {
        return super.getCreateFileSystemRequest().toBuilder()
                .initialSizeInBytes(KB_2)
                .build();
    }
}
