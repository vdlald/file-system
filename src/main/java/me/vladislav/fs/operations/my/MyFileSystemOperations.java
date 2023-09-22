package me.vladislav.fs.operations.my;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.ByteBufferUtils;
import me.vladislav.fs.operations.FileSystemOperations;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Builder(toBuilder = true)
public class MyFileSystemOperations implements FileSystemOperations {

    private final BlockAllocatedSpace allocatedSpace;

    @Override
    public void createFile(CreateFileRequest createFileRequest) throws IOException {
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


    private FileDescriptorsBlock firstFilesDescriptorBlock() throws IOException {
        ByteBuffer byteBuffer = allocatedSpace.readBlock(0);
        return FileDescriptorsBlock.from(byteBuffer);
    }


    @ToString
    @EqualsAndHashCode
    public static class FileDescriptorsBlock {

        public static final int NEXT_FILE_DESCRIPTOR_BLOCK_SIZE = 4;
        public static final int DESCRIPTORS_SIZE = 63 * FileDescriptor.TOTAL_SIZE;
        public static final int TOTAL_SIZE = DESCRIPTORS_SIZE + NEXT_FILE_DESCRIPTOR_BLOCK_SIZE;

        private int nextFileDescriptorBlock = 0;
        private final List<FileDescriptor> descriptors = new ArrayList<>();

        public boolean addDescriptor(FileDescriptor descriptor) {
            if (descriptors.size() >= 63) {
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

        public boolean isEmpty() {
            return nextFileDescriptorBlock == 0 && descriptors.isEmpty();
        }
    }

    @ToString
    @EqualsAndHashCode
    @Builder(toBuilder = true)
    public static class FileDescriptor {

        public static final int FILENAME_SIZE = 60;
        public static final int FIRST_BLOCK_INDEX_SIZE = 4;
        public static final int TOTAL_SIZE = FILENAME_SIZE + FIRST_BLOCK_INDEX_SIZE;

        private final String filename;
        private final int firstBlockIndex;

        public static FileDescriptor from(ByteBuffer src) {
            ByteBuffer filenameBytes = ByteBufferUtils.sliceToFirstZero(src.slice(0, FILENAME_SIZE));
            String filename = ByteBufferUtils.readToString(filenameBytes);
            int firstBlockIndex = src.slice(FILENAME_SIZE, FIRST_BLOCK_INDEX_SIZE).asIntBuffer().get();

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
                    .put(filenameBytes)
                    .putInt(firstBlockIndex)
                    .rewind();
        }
    }
}
