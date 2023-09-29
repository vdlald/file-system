package me.vladislav.fs;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents different sizes of a single block in the file system
 */
@AllArgsConstructor
public enum BlockSize {
    BYTES_128(128), BYTES_512(512), KB_4(4096);

    @Getter
    private final int blockSizeInBytes;

    private static final Map<Integer, BlockSize> map = Arrays.stream(BlockSize.values())
            .collect(Collectors.toMap(BlockSize::getBlockSizeInBytes, Function.identity()));

    public static BlockSize fromBytes(int blockSize) {
        if (map.containsKey(blockSize)) {
            return map.get(blockSize);
        }
        throw new IllegalArgumentException("unknown block size %s".formatted(blockSize));
    }
}
