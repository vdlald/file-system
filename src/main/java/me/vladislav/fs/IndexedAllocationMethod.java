package me.vladislav.fs;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.IOException;
import java.nio.ByteBuffer;

@Builder(toBuilder = true)
public class IndexedAllocationMethod {

    private final BlockSize blockSize;
    private final FileSystem fileSystem;

    public void getFileDescriptors() {
    }

    public ByteBuffer readBlock(int blockIndex) throws IOException {
        int blockStart = blockIndex * blockSize.blockSizeInBytes;

        long currentPosition = fileSystem.getCurrentPosition();
        fileSystem.movePosition(blockStart - currentPosition);

        ByteBuffer buffer = ByteBuffer.allocate(blockSize.blockSizeInBytes);
        fileSystem.read(buffer);

        return buffer;
    }

    @AllArgsConstructor
    public enum BlockSize {
        BYTES_512(512), KB_4(4096);

        private final int blockSizeInBytes;
    }
}
