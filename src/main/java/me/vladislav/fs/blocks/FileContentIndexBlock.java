package me.vladislav.fs.blocks;

import jakarta.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
public class FileContentIndexBlock {

    public static final int MAX_BLOCK_POINTERS = 1023;
    public static final int NEXT_INDEX_BLOCK_SIZE = 4;
    public static final int BLOCK_POINTERS_SIZE = MAX_BLOCK_POINTERS * 4;
    public static final int TOTAL_SIZE = BLOCK_POINTERS_SIZE + NEXT_INDEX_BLOCK_SIZE;

    @Setter
    private int nextIndexBlock = -1;

    @Nonnull
    private final List<Integer> blockPointers = new ArrayList<>(MAX_BLOCK_POINTERS);

    @Nonnull
    public static FileContentIndexBlock from(@Nonnull ByteBuffer src) {
        FileContentIndexBlock block = new FileContentIndexBlock();

        block.nextIndexBlock = src.slice(0, NEXT_INDEX_BLOCK_SIZE).asIntBuffer().get();

        IntBuffer blockPointersBuffer = src.slice(NEXT_INDEX_BLOCK_SIZE, BLOCK_POINTERS_SIZE).asIntBuffer();
        while (blockPointersBuffer.hasRemaining()) {
            int pointer = blockPointersBuffer.get();
            if (pointer == 0) {
                continue;
            }
            block.blockPointers.add(pointer);
        }

        return block;
    }

    @Nonnull
    public ByteBuffer toByteBuffer() {
        ByteBuffer blockPointersBuffer = ByteBuffer.allocate(BLOCK_POINTERS_SIZE);
        for (int blockPointer : blockPointers) {
            blockPointersBuffer.putInt(blockPointer);
        }

        return ByteBuffer.allocate(TOTAL_SIZE)
                .put(ByteBuffer.allocate(NEXT_INDEX_BLOCK_SIZE)
                        .putInt(nextIndexBlock)
                        .rewind())
                .put(blockPointersBuffer.rewind());
    }

    public boolean addBlockPointer(int blockIndex) {
        if (isFull()) {
            return false;
        }
        blockPointers.add(blockIndex);
        return true;
    }

    public boolean isFull() {
        return blockPointers.size() >= MAX_BLOCK_POINTERS;
    }
}
