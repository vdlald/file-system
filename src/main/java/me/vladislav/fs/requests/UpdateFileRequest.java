package me.vladislav.fs.requests;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;

import java.nio.channels.SeekableByteChannel;

@Getter
@Builder(toBuilder = true)
public class UpdateFileRequest {

    // content of file to update
    @Nonnull
    private final SeekableByteChannel content;

    // name of file to update
    @Nonnull
    private final String filename;
}
