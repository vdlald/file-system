package me.vladislav.fs.operations.impl;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.FileSystem;
import me.vladislav.fs.FileSystem.Metadata;
import me.vladislav.fs.converters.FileSystemMetadataBytesConverter;
import me.vladislav.fs.exceptions.OpenFileSystemException;
import me.vladislav.fs.operations.OpenFileSystemOperation;
import me.vladislav.fs.operations.my.BlockFileSystemOperations;
import me.vladislav.fs.operations.my.BlockFileSystemOperationsFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenFileSystemOperationImpl implements OpenFileSystemOperation {

    private final FileSystemMetadataBytesConverter metadataBytesSerializer;
    private final BlockFileSystemOperationsFactory blockFileSystemOperationsFactory;

    @Nonnull
    @Override
    public FileSystem open(@Nonnull Path savePlace) {
        log.info("opening fs");

        AllocatedSpace allocatedFsSpace;
        try {
            allocatedFsSpace = AllocatedSpace.builder()
                    .data(Files.newByteChannel(savePlace, READ))
                    .build();
        } catch (IOException e) {
            throw new OpenFileSystemException(e);
        }

        log.info("loading metadata");
        ByteBuffer metadataBytes = allocatedFsSpace.position(0).read(Metadata.TOTAL_SIZE);
        Metadata metadata = metadataBytesSerializer.from(metadataBytes);
        log.info("loaded metadata: {}", metadata);

        allocatedFsSpace.close();

        AllocatedSpace allocatedFilesSpace;
        try {
            allocatedFilesSpace = AllocatedSpace.builder()
                    .data(Files.newByteChannel(savePlace, READ, WRITE))
                    .startOffset(Metadata.TOTAL_SIZE)
                    .build();
        } catch (IOException e) {
            throw new OpenFileSystemException(e);
        }

        BlockFileSystemOperations fileSystemOperations = blockFileSystemOperationsFactory.create(
                allocatedFilesSpace, metadata.getBlockSize());
        return FileSystem.builder()
                .metadata(metadata)
                .allocatedSpace(allocatedFilesSpace)
                .fileSystemOperations(fileSystemOperations)
                .build();
    }
}
