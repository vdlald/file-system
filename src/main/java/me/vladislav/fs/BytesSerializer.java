package me.vladislav.fs;

import jakarta.annotation.Nonnull;

import java.nio.ByteBuffer;

public interface BytesSerializer<T> {

    @Nonnull
    T from(@Nonnull ByteBuffer src);

    @Nonnull
    ByteBuffer toByteBuffer(@Nonnull T object);
}
