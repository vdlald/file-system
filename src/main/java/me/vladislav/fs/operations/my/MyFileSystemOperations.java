package me.vladislav.fs.operations.my;

import com.google.common.annotations.VisibleForTesting;
import lombok.*;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.ByteBufferUtils;
import me.vladislav.fs.Pair;
import me.vladislav.fs.operations.FileSystemOperations;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Builder(toBuilder = true)
public class MyFileSystemOperations implements FileSystemOperations {

    @Getter(onMethod = @__(@VisibleForTesting))
    private final BlockAllocatedSpace allocatedSpace;

    @Override
    public void createFile(CreateFileRequest createFileRequest) throws IOException {
        var block = getAvailableFileDescriptorsBlock();
        FileDescriptorsBlock descriptorsBlock = block.getFirst();
        Integer blockIndex = block.getSecond();

        descriptorsBlock.descriptors.add(FileDescriptor.builder()
                .filename(createFileRequest.getFilename())
                .firstBlockIndex(-1)
                .build());

        allocatedSpace.writeBlock(blockIndex, descriptorsBlock.toByteBuffer());
    }

    @Override
    public ByteBuffer readFile(String fileName) {
        return null;
    }

    @Override
    public void updateFile(UpdateFileRequest updateFileRequest) {

    }

    @Override
    public void deleteFile(String fileName) {

    }

    private Pair<FileDescriptorsBlock, Integer> getAvailableFileDescriptorsBlock() throws IOException {
        return getAvailableFileDescriptorsBlock(0);
    }

    private Pair<FileDescriptorsBlock, Integer> getAvailableFileDescriptorsBlock(int blockIndex) throws IOException {
        ByteBuffer byteBuffer = allocatedSpace.readBlock(blockIndex);
        FileDescriptorsBlock block = FileDescriptorsBlock.from(byteBuffer);
        if (block.isFull()) {
            return getAvailableFileDescriptorsBlock(block.nextFileDescriptorBlock);
        }
        return Pair.of(block, blockIndex);
    }


    @ToString
    @EqualsAndHashCode
    public static class FileDescriptorsBlock {

        public static final int DESCRIPTORS_LIST_MAX_SIZE = 63;
        public static final int NEXT_FILE_DESCRIPTOR_BLOCK_SIZE = 4;
        public static final int DESCRIPTORS_SIZE = DESCRIPTORS_LIST_MAX_SIZE * FileDescriptor.TOTAL_SIZE;
        public static final int TOTAL_SIZE = DESCRIPTORS_SIZE + NEXT_FILE_DESCRIPTOR_BLOCK_SIZE;

        private int nextFileDescriptorBlock = -1;
        private final List<FileDescriptor> descriptors = new ArrayList<>();

        public boolean addDescriptor(FileDescriptor descriptor) {
            if (isFull()) {
                return false;
            }
            descriptors.add(descriptor);
            return true;
        }

        public static FileDescriptorsBlock from(ByteBuffer src) {
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

    @ToString
    @EqualsAndHashCode
    @Builder(toBuilder = true)
    public static class FileDescriptor {

        public static final int FILENAME_SIZE = 60;
        public static final int FIRST_BLOCK_INDEX_SIZE = 4;
        public static final int TOTAL_SIZE = FILENAME_SIZE + FIRST_BLOCK_INDEX_SIZE;

        @Getter
        private final String filename;

        @Getter
        private final int firstBlockIndex;

        public static FileDescriptor from(ByteBuffer src) {
            int firstBlockIndex = src.slice(0, FIRST_BLOCK_INDEX_SIZE).asIntBuffer().get();

            ByteBuffer filenameBytes = src.slice(FIRST_BLOCK_INDEX_SIZE, FILENAME_SIZE);
            String filename = ByteBufferUtils.readToString(ByteBufferUtils.sliceToFirstZero(filenameBytes));

            return FileDescriptor.builder()
                    .firstBlockIndex(firstBlockIndex)
                    .filename(filename)
                    .build();
        }

        public ByteBuffer toByteBuffer() {
            ByteBuffer filenameBytes = ByteBuffer.allocate(FILENAME_SIZE)
                    .put(filename.getBytes(UTF_8))
                    .rewind();
            return ByteBuffer.allocate(TOTAL_SIZE)
                    .putInt(firstBlockIndex)
                    .put(filenameBytes)
                    .rewind();
        }
    }

    @ToString
    @EqualsAndHashCode
    public static class FileContentIndexBlock {

        public static final int MAX_BLOCK_POINTERS = 1023;
        public static final int NEXT_INDEX_BLOCK_SIZE = 4;
        public static final int BLOCK_POINTERS_SIZE = MAX_BLOCK_POINTERS * 4;
        public static final int TOTAL_SIZE = BLOCK_POINTERS_SIZE + NEXT_INDEX_BLOCK_SIZE;

        @Setter
        private int nextIndexBlock = -1;

        private final List<Integer> blockPointers = new ArrayList<>(MAX_BLOCK_POINTERS);

        public static FileContentIndexBlock from(ByteBuffer src) {
            FileContentIndexBlock block = new FileContentIndexBlock();

            block.nextIndexBlock = src.slice(0, NEXT_INDEX_BLOCK_SIZE).asIntBuffer().get();

            IntBuffer blockPointersBuffer = src.slice(NEXT_INDEX_BLOCK_SIZE, BLOCK_POINTERS_SIZE).asIntBuffer();
            while (blockPointersBuffer.hasRemaining()) {
                int pointer = blockPointersBuffer.get();
                if (pointer == 0) {
                    continue;
                }
                block.blockPointers.add(pointer);
            }

            return block;
        }

        public ByteBuffer toByteBuffer() {
            ByteBuffer blockPointersBuffer = ByteBuffer.allocate(BLOCK_POINTERS_SIZE);
            for (int blockPointer : blockPointers) {
                blockPointersBuffer.putInt(blockPointer);
            }

            return ByteBuffer.allocate(TOTAL_SIZE)
                    .put(ByteBuffer.allocate(NEXT_INDEX_BLOCK_SIZE)
                            .putInt(nextIndexBlock)
                            .rewind())
                    .put(blockPointersBuffer.rewind());
        }

        public boolean addBlockPointer(int blockIndex) {
            if (isFull()) {
                return false;
            }
            blockPointers.add(blockIndex);
            return true;
        }

        public boolean isFull() {
            return blockPointers.size() >= MAX_BLOCK_POINTERS;
        }
    }

    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class FileContentBlock {
        private final ByteBuffer data;

        public ByteBuffer toByteBuffer() {
            return data.duplicate();
        }

        public FileContentBlock from(ByteBuffer buffer) {
            return new FileContentBlock(buffer.duplicate());
        }
    }
}
