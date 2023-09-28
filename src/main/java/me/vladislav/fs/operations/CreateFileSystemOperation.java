package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.FileSystem;
import me.vladislav.fs.requests.CreateFileSystemRequest;
import me.vladislav.fs.serializers.FileSystemMetadataBytesSerializer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;
import static me.vladislav.fs.operations.my.MyFileSystemOperations.METHOD_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateFileSystemOperation {

    private final OpenFileSystemOperation openFileSystemOperation;
    private final FileSystemMetadataBytesSerializer metadataBytesSerializer;

    @Nonnull
    public FileSystem createFileSystem(@Nonnull CreateFileSystemRequest request) throws IOException {
        Path savePlace = request.getWhereToStore();
        if (Files.isDirectory(savePlace)) {
            throw new IllegalArgumentException("You need to specify where to save the file system");
        }

        log.info("creating new FS: {}", savePlace);

        SeekableByteChannel channel = Files.newByteChannel(savePlace, CREATE_NEW, WRITE);
        channel.write(ByteBuffer.allocate(request.getInitialSizeInBytes()));

        log.debug("saving fs metadata");
        ByteBuffer metadataBytes = metadataBytesSerializer.toByteBuffer(FileSystem.Metadata.builder()
                .createdAt(ZonedDateTime.now())
                .blockSize(request.getBlockSize())
                .fileAllocationMethod(METHOD_NAME)
                .build());
        channel.position(0)
                .write(metadataBytes);

        channel.close();

        return openFileSystemOperation.open(savePlace);
    }
}
