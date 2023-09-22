package me.vladislav.fs.blocks;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.vladislav.fs.util.ByteBufferUtils;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class FileDescriptor {

    public static final int FILENAME_SIZE = 60;
    public static final int FIRST_BLOCK_INDEX_SIZE = 4;
    public static final int TOTAL_SIZE = FILENAME_SIZE + FIRST_BLOCK_INDEX_SIZE;

    @Getter
    @Nonnull
    private final String filename;

    @Getter
    private final int firstBlockIndex;

    @Nonnull
    public static FileDescriptor from(@Nonnull ByteBuffer src) {
        int firstBlockIndex = src.slice(0, FIRST_BLOCK_INDEX_SIZE).asIntBuffer().get();

        ByteBuffer filenameBytes = src.slice(FIRST_BLOCK_INDEX_SIZE, FILENAME_SIZE);
        String filename = ByteBufferUtils.readToString(ByteBufferUtils.sliceToFirstZero(filenameBytes));

        return FileDescriptor.builder()
                .firstBlockIndex(firstBlockIndex)
                .filename(filename)
                .build();
    }

    @Nonnull
    public ByteBuffer toByteBuffer() {
        ByteBuffer filenameBytes = ByteBuffer.allocate(FILENAME_SIZE)
                .put(filename.getBytes(UTF_8))
                .rewind();
        return ByteBuffer.allocate(TOTAL_SIZE)
                .putInt(firstBlockIndex)
                .put(filenameBytes)
                .rewind();
    }
}
