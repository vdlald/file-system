package me.vladislav.fs.requests;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;

@Builder
public class ReadFileRequest {

    @Getter
    @Nonnull
    private final String filename;
}
