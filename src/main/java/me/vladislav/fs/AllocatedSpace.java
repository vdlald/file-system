package me.vladislav.fs;

import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Nonnull;
import lombok.Builder;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * Represents an allocated space in channel
 */
public class AllocatedSpace implements Closeable {

    /**
     * Reserved space at the beginning of data
     */
    private final long startOffset;

    /**
     * An open data channel from which the data will be taken
     */
    @Nonnull
    private final SeekableByteChannel data;

    @Builder(toBuilder = true)
    public AllocatedSpace(long startOffset, SeekableByteChannel data) {
        this.startOffset = startOffset;
        this.data = data;
        try {
            if (data.position() < startOffset) {
                data.position(startOffset);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

    @Nonnull
    public ByteBuffer read(int amountOfBytes) {
        try {
            ByteBuffer allocate = ByteBuffer.allocate(amountOfBytes);
            data.read(allocate);
            return allocate.rewind();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int read(@Nonnull ByteBuffer dst) throws IOException {
        int read = data.read(dst);
        dst.rewind();
        return read;
    }

    public int writeString(@Nonnull String str) throws IOException {
        ByteBuffer allocate = ByteBuffer.allocate(str.length())
                .put(str.getBytes(StandardCharsets.UTF_8))
                .rewind();
        return data.write(allocate);
    }

    public int write(@Nonnull ByteBuffer src) throws IOException {
        return data.write(src);
    }

    public long position() throws IOException {
        return data.position() - startOffset;
    }

    @Nonnull
    public AllocatedSpace position(long newPosition) throws IOException {
        long actualPosition = startOffset + newPosition;
        if (actualPosition < startOffset) {
            throw new IndexOutOfBoundsException(
                    "startOffset: %s, actualPosition: %s".formatted(startOffset, actualPosition)
            );
        }
        data.position(actualPosition);
        return this;
    }

    /**
     * Checks if the position in the channel is outside the channel boundary
     */
    public boolean isOutsideOfSpace() {
        try {
            return data.position() >= data.size();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long size() {
        try {
            return data.size() - startOffset;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AllocatedSpace withStartOffset(long startOffset) {
        return this.startOffset == startOffset ? this : new AllocatedSpace(this.startOffset + startOffset, this.data);
    }
}
