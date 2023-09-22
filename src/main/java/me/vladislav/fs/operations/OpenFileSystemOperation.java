package me.vladislav.fs.operations;

import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.BlockSize;
import me.vladislav.fs.FileSystem;
import me.vladislav.fs.operations.my.MyFileSystemOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static me.vladislav.fs.FileSystem.Metadata.*;

@Slf4j
@Component
public class OpenFileSystemOperation {

    public FileSystem open(Path savePlace) throws IOException {
        log.info("opening fs");
        AllocatedSpace allocatedFsSpace = AllocatedSpace.builder()
                .data(Files.newByteChannel(savePlace, READ))
                .build();
        AllocatedSpace allocatedFilesSpace = AllocatedSpace.builder()
                .data(Files.newByteChannel(savePlace, READ, WRITE))
                .startOffset(TOTAL_SIZE)
                .build();

        log.info("loading metadata");
        allocatedFsSpace.position(0);

        String createdAtRaw = allocatedFsSpace.readInString(CREATED_AT_SIZE);
        ZonedDateTime createdAt = ZonedDateTime.parse(createdAtRaw, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String fileAllocationMethod = allocatedFsSpace.readInString(FAM_SIZE);

        allocatedFsSpace.close();

        FileSystem.Metadata metadata = FileSystem.Metadata.builder()
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
