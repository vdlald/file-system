package me.vladislav.fs;

import jakarta.annotation.Nonnull;
import lombok.Builder;

import java.io.ByteArrayInputStream;

@Builder(toBuilder = true)
public class UpdateFileRequest {

    // content of file to update
    @Nonnull
    private final ByteArrayInputStream content;

    // name of file to update
    @Nonnull
    private final String name;
}
