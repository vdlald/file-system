package me.vladislav.fs;

import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.IOException;
import java.nio.ByteBuffer;

@Builder(toBuilder = true)
public class IndexedAllocationMethod {

    private final BlockSize blockSize;
    private final FileSystem fileSystem;

    public ByteBuffer readBlock(int blockIndex) throws IOException {
        int blockStart = blockStart(blockIndex);

        long currentPosition = fileSystem.getCurrentPosition();
        fileSystem.movePosition(blockStart - currentPosition);

        ByteBuffer buffer = ByteBuffer.allocate(blockSize.blockSizeInBytes);
        fileSystem.read(buffer);

        return buffer;
    }

    @VisibleForTesting
    protected void writeBlock(int blockIndex, ByteBuffer data) throws IOException {
        if (data.array().length > blockSize.blockSizeInBytes) {
            throw new RuntimeException();
        }

        fileSystem.setPosition(blockStart(blockIndex));
        fileSystem.write(data);
    }

    private int blockStart(int blockIndex) {
        return blockIndex * blockSize.blockSizeInBytes;
    }

    @AllArgsConstructor
    public enum BlockSize {
        BYTES_512(512), KB_4(4096);

        private final int blockSizeInBytes;
    }
}
