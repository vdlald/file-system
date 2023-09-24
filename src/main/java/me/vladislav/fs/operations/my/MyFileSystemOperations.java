package me.vladislav.fs.operations.my;

import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.BlockSize;
import me.vladislav.fs.IndexedBlockAllocatedSpace;
import me.vladislav.fs.blocks.FileContentIndexBlock;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import me.vladislav.fs.blocks.serializers.FileContentIndexBlockBytesSerializer;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import me.vladislav.fs.operations.FileSystemOperations;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;
import me.vladislav.fs.util.Pair;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static me.vladislav.fs.util.Utils.avoidException;

@Builder(toBuilder = true)
public class MyFileSystemOperations implements FileSystemOperations {

    public static final String METHOD_NAME = "MYI";

    @Nonnull
    @Getter(onMethod = @__(@VisibleForTesting))
    private final IndexedBlockAllocatedSpace allocatedSpace;
    private final FileContentIndexBlockBytesSerializer indexBlockSerializer;
    private final FileDescriptorsBlockBytesSerializer descriptorsBlockSerializer;

    @Override
    public void createFile(@Nonnull CreateFileRequest createFileRequest) throws IOException {
        var block = getAvailableFileDescriptorsBlock();
        FileDescriptorsBlock descriptorsBlock = block.first();
        int blockIndex = block.second();

        allocatedSpace.markBlockAsAllocated(0);
        int fileIndexBlock = allocatedSpace.getFirstFreeBlockIndex();
        allocatedSpace.markBlockAsAllocated(fileIndexBlock);
        descriptorsBlock.addDescriptor(FileDescriptor.builder()
                .filename(createFileRequest.getFilename())
                .fileBlockIndex(fileIndexBlock)
                .build());
        allocatedSpace.writeBlock(blockIndex, descriptorsBlockSerializer.toByteBuffer(descriptorsBlock));

        FileContentIndexBlock firstIndexBlock = new FileContentIndexBlock();
        AtomicReference<FileContentIndexBlock> currentFileContentIndexBlock = new AtomicReference<>(firstIndexBlock);
        List<FileContentIndexBlock> nextIndexBlocks = new ArrayList<>();

        BlockSize blockSize = allocatedSpace.getBlockSize();
        BlockAllocatedSpace content = new BlockAllocatedSpace(blockSize, AllocatedSpace.builder()
                .data(createFileRequest.getContent())
                .build())
                .block(0);
        AtomicLong written = new AtomicLong();
        allocatedSpace.getFreeBlocksIndexStream()
                .takeWhile(value -> avoidException(() -> content.size() > written.get()))
                .forEach(freeBlockIndex -> avoidException(() -> {
                    written.addAndGet(blockSize.getBlockSizeInBytes());
                    ByteBuffer data = content.readBlock();
                    allocatedSpace.writeBlock(freeBlockIndex, data);

                    if (!currentFileContentIndexBlock.get().addBlockPointer(freeBlockIndex)) {
                        int nextFileBlockIndex = allocatedSpace.getFirstFreeBlockIndex();
                        allocatedSpace.markBlockAsAllocated(nextFileBlockIndex);
                        currentFileContentIndexBlock.get().setNextIndexBlock(nextFileBlockIndex);

                        FileContentIndexBlock newIndexBlock = new FileContentIndexBlock();
                        currentFileContentIndexBlock.set(newIndexBlock);
                        newIndexBlock.addBlockPointer(freeBlockIndex);
                        nextIndexBlocks.add(newIndexBlock);
                    }
                    return null;
                }));

        ByteBuffer firstIndexBytes = indexBlockSerializer.toByteBuffer(firstIndexBlock);
        allocatedSpace.writeBlock(fileIndexBlock, firstIndexBytes);

        int nextFileIndexBlockIndex = currentFileContentIndexBlock.get().getNextIndexBlock();
        for (FileContentIndexBlock nextIndexBlock : nextIndexBlocks) {
            allocatedSpace.writeBlock(nextFileIndexBlockIndex, indexBlockSerializer.toByteBuffer(nextIndexBlock));
            nextFileIndexBlockIndex = nextIndexBlock.getNextIndexBlock();
        }

        createFileRequest.getContent().close();

    }

    @Nonnull
    @Override
    public SeekableByteChannel readFile(@Nonnull String filename) throws IOException {
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();
        FileDescriptor fileDescriptor = getFileDescriptor(filename);

        FileContentIndexBlock indexBlock;
        int indexBlockIndex = fileDescriptor.getFileBlockIndex();
        do {
            ByteBuffer indexBlockBuffer = allocatedSpace.readBlock(indexBlockIndex);
            indexBlock = indexBlockSerializer.from(indexBlockBuffer);

            for (int i = 0; i < indexBlock.getBlockPointers().size(); i++) {
                ByteBuffer buffer = allocatedSpace.readBlock(indexBlock.getBlockPointers().get(i));
                channel.write(buffer);
            }
            indexBlockIndex = indexBlock.getNextIndexBlock();
        } while (indexBlock.containsNextBlock());
        return channel.position(0);

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
        FileDescriptorsBlock block = descriptorsBlockSerializer.from(byteBuffer);
        if (block.isFull()) {
            int nextFileDescriptorBlock = block.getNextFileDescriptorBlock();
            if (nextFileDescriptorBlock == 0) {
                int freeBlockIndex = allocatedSpace.getFirstFreeBlockIndex();

                block.setNextFileDescriptorBlock(freeBlockIndex);
                allocatedSpace.writeBlock(
                        blockIndex, descriptorsBlockSerializer.toByteBuffer(block));

                return Pair.of(new FileDescriptorsBlock(allocatedSpace.getBlockSize()), freeBlockIndex);
            }
            return getAvailableFileDescriptorsBlock(nextFileDescriptorBlock);
        }
        return Pair.of(block, blockIndex);
    }

    @Nonnull
    private FileDescriptor getFileDescriptor(@Nonnull String filename) throws IOException {
        FileDescriptorsBlock descriptors;
        Optional<FileDescriptor> descriptor;
        int nextBlock = 0;
        do {
            ByteBuffer byteBuffer = allocatedSpace.readBlock(nextBlock);
            descriptors = descriptorsBlockSerializer.from(byteBuffer);

            descriptor = descriptors.getDescriptors().stream()
                    .filter(fileDescriptor -> filename.equals(fileDescriptor.getFilename()))
                    .findFirst();
            nextBlock = descriptors.getNextFileDescriptorBlock();
        } while (descriptor.isEmpty() && descriptors.containsNextBlock());

        if (descriptor.isEmpty()) {
            throw new RuntimeException("not found");
        }

        return descriptor.get();
    }
}
