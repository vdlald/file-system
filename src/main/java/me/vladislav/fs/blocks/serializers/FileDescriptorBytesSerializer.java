package me.vladislav.fs.blocks.serializers;

import me.vladislav.fs.BytesSerializer;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.util.ByteBufferUtils;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static me.vladislav.fs.blocks.FileDescriptor.*;

@Component
public class FileDescriptorBytesSerializer implements BytesSerializer<FileDescriptor> {

    @Override
    public FileDescriptor from(ByteBuffer src) {
        int firstBlockIndex = src.slice(0, FILE_BLOCK_INDEX_SIZE).asIntBuffer().get();
        long fileSize = src.slice(FILE_BLOCK_INDEX_SIZE, FILE_SIZE).asLongBuffer().get();
        ByteBuffer filenameBytes = src.slice(FILE_BLOCK_INDEX_SIZE + FILE_SIZE, FILENAME_SIZE);
        String filename = ByteBufferUtils.readToString(ByteBufferUtils.sliceToFirstZero(filenameBytes));

        return FileDescriptor.builder()
                .fileBlockIndex(firstBlockIndex)
                .fileSize(fileSize)
                .filename(filename)
                .build();
    }

    @Override
    public ByteBuffer toByteBuffer(FileDescriptor object) {
        ByteBuffer filenameBytes = ByteBuffer.allocate(FILENAME_SIZE)
                .put(object.getFilename().getBytes(UTF_8))
                .rewind();
        return ByteBuffer.allocate(FileDescriptor.TOTAL_SIZE)
                .putInt(object.getFileBlockIndex())
                .putLong(object.getFileSize())
                .put(filenameBytes)
                .rewind();
    }
}
