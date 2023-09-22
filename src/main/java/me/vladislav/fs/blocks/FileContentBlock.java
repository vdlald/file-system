package me.vladislav.fs.blocks;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.nio.ByteBuffer;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class FileContentBlock {

    @Nonnull
    private final ByteBuffer data;

    @Nonnull
    public ByteBuffer toByteBuffer() {
        return data.duplicate();
    }

    @Nonnull
    public FileContentBlock from(@Nonnull ByteBuffer buffer) {
        return new FileContentBlock(buffer.duplicate());
    }
}
