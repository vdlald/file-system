package me.vladislav.fs;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

import static me.vladislav.fs.FileSystem.Metadata.CREATED_AT_SIZE;

@Builder(toBuilder = true)
public class AllocatedSpace implements Closeable {

    private final long startOffset;

    private final SeekableByteChannel data;

    public boolean isOpen() {
        return data.isOpen();
    }

    @Override
    public void close() throws IOException {
        data.close();
    }

    @VisibleForTesting
    SeekableByteChannel getData() {
        return data;
    }

    public String readInString(long amountOfBytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(CREATED_AT_SIZE);
        read(buffer);
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }

    public int read(ByteBuffer dst) throws IOException {
        return data.read(dst);
    }

    public int write(ByteBuffer src) throws IOException {
        return data.write(src);
    }

    public long position() throws IOException {
        return data.position() - startOffset;
    }

    public SeekableByteChannel position(long newPosition) throws IOException {
        return data.position(startOffset + newPosition);
    }

    public long size() throws IOException {
        return data.size() - startOffset;
    }
}
