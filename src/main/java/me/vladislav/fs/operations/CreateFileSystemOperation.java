package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.FileSystem;
import me.vladislav.fs.requests.CreateFileSystemRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateFileSystemOperation {

    private final OpenFileSystemOperation openFileSystemOperation;

    @Nonnull
    public FileSystem createFileSystem(@Nonnull CreateFileSystemRequest request) throws IOException {
        Path savePlace = request.getWhereToStore().resolve(request.getFileSystemName());

        log.info("creating new FS: {}", savePlace);

        SeekableByteChannel channel = Files.newByteChannel(savePlace, CREATE_NEW, WRITE);
        channel.write(ByteBuffer.allocate(request.getInitialSizeInBytes()));

        AllocatedSpace allocatedSpaceForUsage = AllocatedSpace.builder()
                .data(channel)
                .startOffset(FileSystem.Metadata.TOTAL_SIZE)
                .build();

        log.debug("saving fs metadata");
        channel.position(0);
        String createdAt = ZonedDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        ByteBuffer createdAtBytes = ByteBuffer.wrap(createdAt.getBytes(UTF_8));
        channel.write(createdAtBytes);


        channel.close();

        return openFileSystemOperation.open(savePlace);
    }
}
