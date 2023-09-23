package me.vladislav.fs;

import me.vladislav.fs.requests.CreateFileSystemRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AllocatedSpaceTest extends AbstractFileSystemTest {

    @Test
    @DisplayName("AllocatedSpace / Must write and read")
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

    @Nonnull
    @Override
    protected CreateFileSystemRequest getCreateFileSystemRequest() {
        return super.getCreateFileSystemRequest().toBuilder()
                .initialSizeInBytes(KB_2)
                .build();
    }
}
