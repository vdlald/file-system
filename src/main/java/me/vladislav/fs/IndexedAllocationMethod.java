package me.vladislav.fs;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;

import java.io.IOException;
import java.nio.ByteBuffer;

@Builder(toBuilder = true)
public class IndexedAllocationMethod {

    public static final String CODE = "IAM";

    private final BlockSize blockSize;
    private final AllocatedSpace allocatedSpace;

    public ByteBuffer readBlock(int blockIndex) throws IOException {
        int blockStart = blockStart(blockIndex);

        allocatedSpace.position(blockStart);

        ByteBuffer buffer = ByteBuffer.allocate(blockSize.getBlockSizeInBytes());
        allocatedSpace.read(buffer);

        return buffer;
    }

    @VisibleForTesting
    protected void writeBlock(int blockIndex, ByteBuffer data) throws IOException {
        if (data.array().length > blockSize.getBlockSizeInBytes()) {
            throw new RuntimeException();
        }

        allocatedSpace.position(blockStart(blockIndex));
        allocatedSpace.write(data);
    }

    private int blockStart(int blockIndex) {
        return blockIndex * blockSize.getBlockSizeInBytes();
    }
}
