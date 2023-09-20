package me.vladislav.fs;

import lombok.Builder;
import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.file.Path;

@Builder(toBuilder = true)
public class FileSystem implements Closeable {

    @Getter
    private final Path whereStored;

    private final Channel content;

    @Override
    public void close() throws IOException {
        content.close();
    }
}
