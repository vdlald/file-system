package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.CreateFileSystemRequest;
import me.vladislav.fs.FileSystem;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.StandardOpenOption.*;

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

        log.debug("saving metadata");
        String createdAt = ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        ByteBuffer createdAtBytes = ByteBuffer.wrap(createdAt.getBytes(StandardCharsets.UTF_8));
        channel.write(createdAtBytes);
        channel.close();

        log.debug("created new FS");
        return FileSystem.builder()
                .content(Files.newByteChannel(savePlace, READ, WRITE))
                .whereStored(savePlace)
                .build();
    }
}
