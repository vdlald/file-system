package me.vladislav.fs.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

public class ByteBufferUtils {

    public static boolean isEmpty(ByteBuffer buffer) {
        int position = buffer.position();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        for (byte b : bytes) {
            if (b != (byte) 0) {
                buffer.position(position);
                return false;
            }
        }
        buffer.position(position);
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

    public static String parseString(ByteBuffer buffer) {
        buffer = sliceToFirstZero(buffer);
        byte[] b = new byte[buffer.remaining()];
        buffer.get(b);
        return new String(b, StandardCharsets.UTF_8);
    }

    public static ByteBuffer readWholeChannel(SeekableByteChannel seekableByteChannel) throws IOException {
        ByteBuffer allocate = ByteBuffer.allocate((int) seekableByteChannel.size());
        seekableByteChannel.read(allocate);
        return allocate.rewind();
    }

    public static ByteBuffer wrap(int value) {
        ByteBuffer allocate = ByteBuffer.allocate(4);
        return allocate.putInt(value).rewind();
    }
}
