package me.vladislav.fs;

import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.With;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * Represents an allocated space somewhere
 */
@Builder
public class AllocatedSpace implements Closeable {

    /**
     * Reserved space at the beginning
     */
    @With
    private final long startOffset;

    /**
     * An open data channel from which the data will be taken
     */
    @Nonnull
    private final SeekableByteChannel data;

    public boolean isOpen() {
        return data.isOpen();
    }

    @Override
    public void close() throws IOException {
        data.close();
    }

    @Nonnull
    @VisibleForTesting
    SeekableByteChannel getData() {
        return data;
    }

    @Nonnull
    public String readInString(int amountOfBytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(amountOfBytes);
        read(buffer);
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }

    public int read(@Nonnull ByteBuffer dst) throws IOException {
        return data.read(dst);
    }

    public int write(@Nonnull ByteBuffer src) throws IOException {
        return data.write(src);
    }

    public long position() throws IOException {
        return data.position() - startOffset;
    }

    @Nonnull
    public SeekableByteChannel position(long newPosition) throws IOException {
        return data.position(startOffset + newPosition);
    }

    public long size() throws IOException {
        return data.size() - startOffset;
    }
}
