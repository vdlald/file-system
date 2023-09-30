package me.vladislav.fs.converters;

import me.vladislav.fs.BlockSize;
import me.vladislav.fs.BytesConverter;
import me.vladislav.fs.FileSystem;
import me.vladislav.fs.util.ByteBufferUtils;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static me.vladislav.fs.FileSystem.Metadata.*;

@Component
public class FileSystemMetadataBytesConverter implements BytesConverter<FileSystem.Metadata> {

    private static final DateTimeFormatter CREATED_AT_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public FileSystem.Metadata from(ByteBuffer src) {
        byte[] createdAtBytes = new byte[CREATED_AT_SIZE];

        src.get(createdAtBytes);
        String createdAtRaw = new String(createdAtBytes, UTF_8);
        ZonedDateTime createdAt = ZonedDateTime.parse(createdAtRaw, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        byte[] fam = new byte[FAM_SIZE];
        src.get(fam);
        String fileAllocationMethod = new String(fam, UTF_8);

        byte[] blockBytes = new byte[BLOCK_SIZE];
        src.get(blockBytes);
        int blockSize = ByteBuffer.wrap(blockBytes).asIntBuffer().get();

        return FileSystem.Metadata.builder()
                .createdAt(createdAt)
                .fileAllocationMethod(fileAllocationMethod)
                .blockSize(BlockSize.fromBytes(blockSize))
                .build();
    }

    @Override
    public ByteBuffer toByteBuffer(FileSystem.Metadata object) {
        ByteBuffer allocate = ByteBuffer.allocate(FileSystem.Metadata.TOTAL_SIZE);

        String createdAt = object.getCreatedAt()
                .truncatedTo(ChronoUnit.SECONDS)
                .format(CREATED_AT_FORMAT);

        return allocate.put(ByteBuffer.wrap(createdAt.getBytes(UTF_8)))
                .put(ByteBuffer.wrap(object.getFileAllocationMethod().getBytes(UTF_8)))
                .put(ByteBufferUtils.wrap(object.getBlockSize().getBlockSizeInBytes()))
                .rewind();
    }
}
