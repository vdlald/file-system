package me.vladislav.fs;

import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Builder(toBuilder = true)
public class FileSystem implements Closeable {

    private static final int METADATA_BYTES_SIZE = 46;

    @Getter
    private final Path whereStored;

    private final SeekableByteChannel content;

    public Metadata getMetadata() throws IOException {
        long position = content.position();

        ByteBuffer createdAtRaw = ByteBuffer.allocate(METADATA_BYTES_SIZE);
        content.position(0).read(createdAtRaw);
        String createdAtStr = new String(createdAtRaw.array(), StandardCharsets.UTF_8);

        ZonedDateTime createdAt = ZonedDateTime.parse(createdAtStr, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        Metadata metadata = Metadata.builder()
                .createdAt(createdAt)
                .build();

        content.position(position);
        return metadata;
    }

    public void setStartPosition() throws IOException {
        content.position(METADATA_BYTES_SIZE);
    }

    public long getCurrentPosition() throws IOException {
        return content.position() - METADATA_BYTES_SIZE;
    }

    public void movePosition(long amountOfBytesToMove) throws IOException {
        long position = content.position();
        content.position(position + amountOfBytesToMove);
    }

    public void read(ByteBuffer dst) throws IOException {
        content.read(dst);
    }

    public long getAllocatedSize() throws IOException {
        return content.size();
    }

    public long getAvailableToUseSize() throws IOException {
        return getAllocatedSize() - METADATA_BYTES_SIZE;
    }

    @VisibleForTesting
    SeekableByteChannel getContent() {
        return content;
    }

    @Override
    public void close() throws IOException {
        content.close();
    }

    @Getter
    @Builder
    public static class Metadata {
        private final ZonedDateTime createdAt;
    }
}
