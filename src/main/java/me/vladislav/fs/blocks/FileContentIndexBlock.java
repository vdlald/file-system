package me.vladislav.fs.blocks;

import jakarta.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
    @Getter
    private int nextIndexBlock = 0;

    @Nonnull
    private final List<Integer> blockPointers = new ArrayList<>(MAX_BLOCK_POINTERS);

    public List<Integer> getBlockPointers() {
        return new ArrayList<>(blockPointers);
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
