package me.vladislav.fs.blocks;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.ByteBuffer;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class FileContentBlock {

    @Getter
    @Nonnull
    private final ByteBuffer data;
}
