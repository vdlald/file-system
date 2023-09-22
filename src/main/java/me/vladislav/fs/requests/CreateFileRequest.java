package me.vladislav.fs.requests;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;

import java.nio.channels.SeekableByteChannel;

// todo: можно менять контент из вне
// todo: Q-5 ?
@Getter
@Builder(toBuilder = true)
public class CreateFileRequest {

    // content of file to create
    @Nonnull
    private final SeekableByteChannel content;

    // name of file to create
    @Nonnull
    private final String filename;
}
