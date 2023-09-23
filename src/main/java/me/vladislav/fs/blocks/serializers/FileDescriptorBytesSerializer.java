package me.vladislav.fs.blocks.serializers;

import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.util.ByteBufferUtils;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class FileDescriptorBytesSerializer implements BytesSerializer<FileDescriptor> {

    @Override
    public FileDescriptor from(ByteBuffer src) {
        int firstBlockIndex = src.slice(0, FileDescriptor.FIRST_BLOCK_INDEX_SIZE).asIntBuffer().get();

        ByteBuffer filenameBytes = src.slice(FileDescriptor.FIRST_BLOCK_INDEX_SIZE, FileDescriptor.FILENAME_SIZE);
        String filename = ByteBufferUtils.readToString(ByteBufferUtils.sliceToFirstZero(filenameBytes));

        return FileDescriptor.builder()
                .firstBlockIndex(firstBlockIndex)
                .filename(filename)
                .build();
    }

    @Override
    public ByteBuffer toByteBuffer(FileDescriptor object) {
        ByteBuffer filenameBytes = ByteBuffer.allocate(FileDescriptor.FILENAME_SIZE)
                .put(object.getFilename().getBytes(UTF_8))
                .rewind();
        return ByteBuffer.allocate(FileDescriptor.TOTAL_SIZE)
                .putInt(object.getFirstBlockIndex())
                .put(filenameBytes)
                .rewind();
    }
}
