package me.vladislav.fs.blocks.converters;

import lombok.RequiredArgsConstructor;
import me.vladislav.fs.BlockSize;
import me.vladislav.fs.BytesConverter;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import me.vladislav.fs.util.ByteBufferUtils;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
@RequiredArgsConstructor
public class FileDescriptorsBlockBytesConverter implements BytesConverter<FileDescriptorsBlock> {

    private final FileDescriptorBytesConverter fileDescriptorBytesSerializer;

    @Override
    public FileDescriptorsBlock from(ByteBuffer src) {
        return from(src, BlockSize.KB_4);
    }

    public FileDescriptorsBlock from(ByteBuffer src, BlockSize blockSize) {
        FileDescriptorsBlock descriptors = new FileDescriptorsBlock(blockSize);
        int read;
        for (read = 0; read < FileDescriptorsBlock.DESCRIPTORS_SIZE; read += FileDescriptor.TOTAL_SIZE) {
            ByteBuffer descriptorBuffer = src.slice(read, FileDescriptor.TOTAL_SIZE);
            if (ByteBufferUtils.isEmpty(descriptorBuffer)) {
                continue;
            }
            FileDescriptor descriptor = fileDescriptorBytesSerializer.from(descriptorBuffer);
            descriptors.addDescriptor(descriptor);
        }
        descriptors.setNextFileDescriptorBlock(src.slice(read, FileDescriptorsBlock.NEXT_FILE_DESCRIPTOR_BLOCK_SIZE)
                .asIntBuffer()
                .get());
        return descriptors;
    }

    @Override
    public ByteBuffer toByteBuffer(FileDescriptorsBlock object) {
        ByteBuffer buffer = ByteBuffer.allocate(FileDescriptorsBlock.TOTAL_SIZE);

        ByteBuffer descriptorsBuffer = ByteBuffer.allocate(FileDescriptorsBlock.DESCRIPTORS_SIZE);
        for (FileDescriptor descriptor : object.getDescriptors()) {
            descriptorsBuffer.put(fileDescriptorBytesSerializer.toByteBuffer(descriptor));
        }
        buffer.put(descriptorsBuffer.rewind());
        buffer.putInt(object.getNextFileDescriptorBlock());
        return buffer.rewind();
    }
}
