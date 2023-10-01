package me.vladislav.fs;

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

    @SuppressWarnings("unused")
    public boolean isOpen() {
        return data.isOpen();
    }

    @Override
    public void close() {
        try {
            data.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    @Nonnull
    public String readInString(int amountOfBytes) {
        ByteBuffer buffer = ByteBuffer.allocate(amountOfBytes);
        read(buffer);
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }

    @Nonnull
    public ByteBuffer read(int amountOfBytes) {
        try {
            ByteBuffer allocate = ByteBuffer.allocate(amountOfBytes);
            int read = data.read(allocate);
            if (read > 0) {
                allocate.limit(read);
            }
            return allocate.rewind();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int read(@Nonnull ByteBuffer dst) {
        int read;
        try {
            read = data.read(dst);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dst.rewind();
        return read;
    }

    public int writeString(@Nonnull String str) {
        ByteBuffer allocate = ByteBuffer.allocate(str.length())
                .put(str.getBytes(StandardCharsets.UTF_8))
                .rewind();
        try {
            return data.write(allocate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int write(@Nonnull ByteBuffer src) {
        try {
            return data.write(src);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public long position() {
        try {
            return data.position() - startOffset;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public AllocatedSpace position(long newPosition) {
        long actualPosition = startOffset + newPosition;
        if (actualPosition < startOffset) {
            throw new IndexOutOfBoundsException(
                    "startOffset: %s, actualPosition: %s".formatted(startOffset, actualPosition)
            );
        }
        try {
            data.position(actualPosition);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void truncate(long size) {
        try {
            data.truncate(size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
