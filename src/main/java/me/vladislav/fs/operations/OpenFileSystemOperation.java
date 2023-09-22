package me.vladislav.fs.operations;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.BlockSize;
import me.vladislav.fs.FileSystem;
import me.vladislav.fs.FileSystem.Metadata;
import me.vladislav.fs.operations.my.MyFileSystemOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static me.vladislav.fs.FileSystem.Metadata.CREATED_AT_SIZE;
import static me.vladislav.fs.FileSystem.Metadata.FAM_SIZE;

@Slf4j
@Component
public class OpenFileSystemOperation {

    @Nonnull
    public FileSystem open(@Nonnull Path savePlace) throws IOException {
        log.info("opening fs");
        AllocatedSpace allocatedFsSpace = AllocatedSpace.builder()
                .data(Files.newByteChannel(savePlace, READ))
                .build();
        AllocatedSpace allocatedFilesSpace = AllocatedSpace.builder()
                .data(Files.newByteChannel(savePlace, READ, WRITE))
                .startOffset(Metadata.TOTAL_SIZE)
                .build();

        log.info("loading metadata");
        allocatedFsSpace.position(0);

        String createdAtRaw = allocatedFsSpace.readInString(CREATED_AT_SIZE);
        ZonedDateTime createdAt = ZonedDateTime.parse(createdAtRaw, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String fileAllocationMethod = allocatedFsSpace.readInString(FAM_SIZE);

        allocatedFsSpace.close();

        Metadata metadata = Metadata.builder()
                .createdAt(createdAt)
                .fileAllocationMethod(fileAllocationMethod)
                .build();

        return FileSystem.builder()
                .metadata(metadata)
                .allocatedSpace(allocatedFilesSpace)
                .fileSystemOperations(MyFileSystemOperations.builder()
                        .allocatedSpace(new BlockAllocatedSpace(BlockSize.KB_4, allocatedFilesSpace))
                        .build())
                .build();
    }
}
