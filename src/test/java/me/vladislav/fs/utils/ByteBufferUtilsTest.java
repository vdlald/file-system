package me.vladislav.fs.utils;

import me.vladislav.fs.util.ByteBufferUtils;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ByteBufferUtilsTest {

    @Test
    void testIsEmpty() {
        ByteBuffer buffer = ByteBuffer.allocate(10)
                .putInt(10)
                .rewind();
        assertFalse(ByteBufferUtils.isEmpty(buffer));
        assertTrue(ByteBufferUtils.isEmpty(ByteBuffer.allocate(10)));
    }
}
