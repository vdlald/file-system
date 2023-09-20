package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.CreateFileSystemRequest;
import me.vladislav.fs.FileSystem;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

@Slf4j
@Component
public class CreateFileSystemOperation {

    @Nonnull
    public FileSystem createFileSystem(@Nonnull CreateFileSystemRequest request) throws IOException {
        Path savePlace = request.getWhereToStore().resolve(request.getFileSystemName());

        log.info("creating new FS: {}", savePlace);

        SeekableByteChannel channel = Files.newByteChannel(savePlace, CREATE_NEW, WRITE);
        channel.write(ByteBuffer.allocate(request.getInitialSizeInBytes()));

        channel.position(0);

        log.debug("created new FS");
        return FileSystem.builder()
                .content(channel)
                .whereStored(savePlace)
                .build();
    }
}
