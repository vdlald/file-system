package me.vladislav.fs.operations.my;

import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import me.vladislav.fs.operations.FileSystemOperations;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;
import me.vladislav.fs.util.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

@Builder(toBuilder = true)
public class MyFileSystemOperations implements FileSystemOperations {

    @Nonnull
    @Getter(onMethod = @__(@VisibleForTesting))
    private final BlockAllocatedSpace allocatedSpace;
    private final FileDescriptorsBlockBytesSerializer fileDescriptorsBlockBytesSerializer;

    @Override
    public void createFile(@Nonnull CreateFileRequest createFileRequest) throws IOException {
        var block = getAvailableFileDescriptorsBlock();
        FileDescriptorsBlock descriptorsBlock = block.getFirst();
        int blockIndex = Objects.requireNonNull(block.getSecond());

        descriptorsBlock.addDescriptor(FileDescriptor.builder()
                .filename(createFileRequest.getFilename())
                .firstBlockIndex(-1)
                .build());

        allocatedSpace.writeBlock(blockIndex, fileDescriptorsBlockBytesSerializer.toByteBuffer(descriptorsBlock));
    }

    @Nonnull
    @Override
    public ByteBuffer readFile(@Nonnull String fileName) {
        return null;
    }

    @Override
    public void updateFile(@Nonnull UpdateFileRequest updateFileRequest) {

    }

    @Override
    public void deleteFile(@Nonnull String fileName) {

    }

    @Nonnull
    private Pair<FileDescriptorsBlock, Integer> getAvailableFileDescriptorsBlock() throws IOException {
        return getAvailableFileDescriptorsBlock(0);
    }

    @Nonnull
    private Pair<FileDescriptorsBlock, Integer> getAvailableFileDescriptorsBlock(int blockIndex) throws IOException {
        ByteBuffer byteBuffer = allocatedSpace.readBlock(blockIndex);
        FileDescriptorsBlock block = fileDescriptorsBlockBytesSerializer.from(byteBuffer);
        if (block.isFull()) {
            return getAvailableFileDescriptorsBlock(block.getNextFileDescriptorBlock());
        }
        return Pair.of(block, blockIndex);
    }
}
