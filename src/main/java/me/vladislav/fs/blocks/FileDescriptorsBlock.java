package me.vladislav.fs.blocks;

import jakarta.annotation.Nonnull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.vladislav.fs.BlockSize;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
public class FileDescriptorsBlock {

    public static final int DESCRIPTORS_LIST_MAX_SIZE = 63;
    public static final int NEXT_FILE_DESCRIPTOR_BLOCK_SIZE = 4;
    public static final int DESCRIPTORS_SIZE = DESCRIPTORS_LIST_MAX_SIZE * FileDescriptor.TOTAL_SIZE;
    public static final int TOTAL_SIZE = DESCRIPTORS_SIZE + NEXT_FILE_DESCRIPTOR_BLOCK_SIZE;

    private final BlockSize blockSize;

    @Setter
    @Getter
    private int nextFileDescriptorBlock = 0;

    @Nonnull
    private final List<FileDescriptor> descriptors = new ArrayList<>();

    public FileDescriptorsBlock(BlockSize blockSize) {
        this.blockSize = blockSize;
    }

    public boolean addDescriptor(FileDescriptor descriptor) {
        if (isFull()) {
            return false;
        }
        descriptors.add(descriptor);
        return true;
    }

    public List<FileDescriptor> getDescriptors() {
        return new ArrayList<>(descriptors);
    }

    public void removeDescriptor(int index) {
        descriptors.remove(index);
    }

    @Nullable
    public FileDescriptor getDescriptor(int index) {
        return descriptors.get(index);
    }

    public int getDescriptorIndex(@Nonnull String filename) {
        for (int i = 0; i < descriptors.size(); i++) {
            if (descriptors.get(i).getFilename().equals(filename)) {
                return i;
            }
        }

        return -1;
    }

    public boolean isEmpty() {
        return nextFileDescriptorBlock == 0 && descriptors.isEmpty();
    }

    public boolean isFull() {
        return descriptors.size() == DESCRIPTORS_LIST_MAX_SIZE;
    }

    public boolean hasNextBlock() {
        return nextFileDescriptorBlock > 0;
    }

    public int size() {
        return descriptors.size();
    }
}
