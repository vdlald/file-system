package me.vladislav.fs.blocks;

import jakarta.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.vladislav.fs.util.ByteBufferUtils;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
public class FileDescriptorsBlock {

    public static final int DESCRIPTORS_LIST_MAX_SIZE = 63;
    public static final int NEXT_FILE_DESCRIPTOR_BLOCK_SIZE = 4;
    public static final int DESCRIPTORS_SIZE = DESCRIPTORS_LIST_MAX_SIZE * FileDescriptor.TOTAL_SIZE;
    public static final int TOTAL_SIZE = DESCRIPTORS_SIZE + NEXT_FILE_DESCRIPTOR_BLOCK_SIZE;

    @Getter
    private int nextFileDescriptorBlock = -1;

    @Nonnull
    private final List<FileDescriptor> descriptors = new ArrayList<>();

    public boolean addDescriptor(FileDescriptor descriptor) {
        if (isFull()) {
            return false;
        }
        descriptors.add(descriptor);
        return true;
    }

    @Nonnull
    public static FileDescriptorsBlock from(@Nonnull ByteBuffer src) {
        FileDescriptorsBlock descriptors = new FileDescriptorsBlock();
        int read;
        for (read = 0; read < DESCRIPTORS_SIZE; read += FileDescriptor.TOTAL_SIZE) {
            ByteBuffer descriptorBuffer = src.slice(read, FileDescriptor.TOTAL_SIZE);
            if (ByteBufferUtils.isEmpty(descriptorBuffer)) {
                continue;
            }
            FileDescriptor descriptor = FileDescriptor.from(descriptorBuffer);
            descriptors.descriptors.add(descriptor);
        }
        descriptors.nextFileDescriptorBlock = src.slice(read, NEXT_FILE_DESCRIPTOR_BLOCK_SIZE)
                .asIntBuffer()
                .get();
        return descriptors;
    }

    @Nonnull
    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(TOTAL_SIZE);

        ByteBuffer descriptorsBuffer = ByteBuffer.allocate(DESCRIPTORS_SIZE);
        for (FileDescriptor descriptor : descriptors) {
            descriptorsBuffer.put(descriptor.toByteBuffer());
        }
        buffer.put(descriptorsBuffer.rewind());
        buffer.putInt(nextFileDescriptorBlock);
        return buffer.rewind();
    }

    @Nullable
    public FileDescriptor getDescriptor(int index) {
        return descriptors.get(index);
    }

    public boolean isEmpty() {
        return nextFileDescriptorBlock == -1 && descriptors.isEmpty();
    }

    public boolean isFull() {
        return descriptors.size() == DESCRIPTORS_LIST_MAX_SIZE;
    }

    public int size() {
        return descriptors.size();
    }
}
