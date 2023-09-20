package me.vladislav.fs;

import jakarta.annotation.Nonnull;
import lombok.Builder;

import java.io.ByteArrayInputStream;

// todo: можно менять контент из вне
// todo: Q-5 ?
@Builder(toBuilder = true)
public class CreateFileRequest {

    // content of file to create
    @Nonnull
    private final ByteArrayInputStream content;

    // name of file to create
    @Nonnull
    private final String name;
}
