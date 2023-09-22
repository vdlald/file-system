package me.vladislav.fs;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ByteBufferUtils {

    public static boolean isEmpty(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        for (byte b : bytes) {
            if (b != (byte) 0) {
                return false;
            }
        }
        return true;
    }

    public static ByteBuffer sliceToFirstZero(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            if (b == (byte) 0) {
                return buffer.slice(0, buffer.position() - 1);
            }
        }
        return buffer;
    }

    public static String readToString(ByteBuffer buffer) {
        byte[] b = new byte[buffer.remaining()];
        buffer.get(b);
        return new String(b, StandardCharsets.UTF_8);
    }
}
