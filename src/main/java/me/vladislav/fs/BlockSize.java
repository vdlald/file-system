package me.vladislav.fs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BlockSize {
    BYTES_512(512), KB_4(4096);

    @Getter
    private final int blockSizeInBytes;
}
