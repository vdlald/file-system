package me.vladislav.fs.operations.my;

import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.AllocatedSpace;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.BlockSize;
import me.vladislav.fs.IndexedBlockAllocatedSpace;
import me.vladislav.fs.blocks.FileContentIndexBlock;
import me.vladislav.fs.blocks.FileDescriptor;
import me.vladislav.fs.blocks.FileDescriptorsBlock;
import me.vladislav.fs.blocks.components.ChainedFileContentIndexBlock;
import me.vladislav.fs.blocks.components.ChainedFileContentIndexBlockFactory;
import me.vladislav.fs.blocks.serializers.FileContentIndexBlockBytesSerializer;
import me.vladislav.fs.blocks.serializers.FileDescriptorsBlockBytesSerializer;
import me.vladislav.fs.operations.FileSystemOperations;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;
import me.vladislav.fs.util.Pair;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Iterator;
import java.util.Optional;

@Slf4j
@Component
@Scope("prototype")
public class MyFileSystemOperations implements FileSystemOperations {

    public static final String METHOD_NAME = "MYI";
    private static final int FIRST_FILE_DESCRIPTORS_BLOCK_INDEX = 0;

    @Nonnull
    @Getter(onMethod = @__(@VisibleForTesting))
    private final IndexedBlockAllocatedSpace allocatedSpace;
    private final FileContentIndexBlockBytesSerializer indexBlockSerializer;
    private final FileDescriptorsBlockBytesSerializer descriptorsBlockSerializer;
    private final ChainedFileContentIndexBlockFactory chainedFileContentIndexBlockFactory;

    @SuppressWarnings("all")
    public MyFileSystemOperations(
            IndexedBlockAllocatedSpace allocatedSpace,
            FileContentIndexBlockBytesSerializer indexBlockSerializer,
            FileDescriptorsBlockBytesSerializer descriptorsBlockSerializer,
            ChainedFileContentIndexBlockFactory chainedFileContentIndexBlockFactory
    ) {
        this.allocatedSpace = allocatedSpace;
        this.indexBlockSerializer = indexBlockSerializer;
        this.descriptorsBlockSerializer = descriptorsBlockSerializer;
        this.chainedFileContentIndexBlockFactory = chainedFileContentIndexBlockFactory;
    }

    @PostConstruct
    private void init() {
        allocatedSpace.markBlockAsAllocated(FIRST_FILE_DESCRIPTORS_BLOCK_INDEX);
    }

    @Override
    public void createFile(@Nonnull CreateFileRequest createFileRequest) throws IOException {
        log.info("creating file: {}", createFileRequest.getFilename());

        log.debug("find file descriptor block with free space");
        var block = getAvailableFileDescriptorsBlock();
        FileDescriptorsBlock descriptorsBlock = block.first();
        int blockIndex = block.second();

        log.debug("save file descriptor in block: {}", blockIndex);
        int fileIndexBlock = allocatedSpace.getFreeBlockIndexAndMarkAsAllocated();
        FileDescriptor fileDescriptor = FileDescriptor.builder()
                .filename(createFileRequest.getFilename())
                .fileBlockIndex(fileIndexBlock)
                .build();
        descriptorsBlock.addDescriptor(fileDescriptor);
        ByteBuffer descriptorBytes = descriptorsBlockSerializer.toByteBuffer(descriptorsBlock);
        allocatedSpace.writeBlock(blockIndex, descriptorBytes);

        log.debug("create index block for content");
        FileContentIndexBlock firstIndexBlock = new FileContentIndexBlock();

        BlockSize blockSize = allocatedSpace.getBlockSize();
        BlockAllocatedSpace content = new BlockAllocatedSpace(blockSize, AllocatedSpace.builder()
                .data(createFileRequest.getContent())
                .build())
                .block(0);

        ChainedFileContentIndexBlock chainIndex = chainedFileContentIndexBlockFactory.create(
                fileIndexBlock, firstIndexBlock, allocatedSpace);

        log.debug("write file content in fs");
        while (content.hasNextBlock()) {
            chainIndex.appendBlock(content.readBlock());
        }

        chainIndex.close();
        createFileRequest.getContent().close();
    }

    @Nonnull
    @Override
    public SeekableByteChannel readFile(@Nonnull String filename) throws IOException {
        log.info("reading file: {}", filename);
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
    public void updateFile(@Nonnull UpdateFileRequest updateFileRequest) throws IOException {
        log.info("updating file: {}", updateFileRequest.getFilename());
        FileDescriptor fileDescriptor = getFileDescriptor(updateFileRequest.getFilename());
        BlockAllocatedSpace content = new BlockAllocatedSpace(allocatedSpace.getBlockSize(), AllocatedSpace.builder()
                .data(updateFileRequest.getContent())
                .build());

        log.debug("get file index block");
        int indexBlockIndex = fileDescriptor.getFileBlockIndex();
        ByteBuffer indexBlockBuffer = allocatedSpace.readBlock(indexBlockIndex);
        FileContentIndexBlock indexBlock = indexBlockSerializer.from(indexBlockBuffer);

        ChainedFileContentIndexBlock indexChain = chainedFileContentIndexBlockFactory.create(
                fileDescriptor.getFileBlockIndex(), indexBlock, allocatedSpace);

        log.debug("update file content");
        Iterator<ByteBuffer> contentIterator = content.contentIterator();
        indexChain.rewriteBlocks(contentIterator);
        indexChain.close();
    }

    @Override
    public void deleteFile(@Nonnull String filename) throws IOException {
        log.info("deleting file: {}", filename);
        Pair<FileDescriptorsBlock, Integer> pair = getFileDescriptorsBlock(filename);
        FileDescriptorsBlock fileDescriptorsBlock = pair.first();
        int fileDescriptorsBlockIndex = pair.second();

        FileDescriptor fileDescriptor = fileDescriptorsBlock.getDescriptor(filename);
        assert fileDescriptor != null;

        FileContentIndexBlock indexBlock;
        int indexBlockIndex = fileDescriptor.getFileBlockIndex();
        do {
            ByteBuffer indexBlockBuffer = allocatedSpace.readBlock(indexBlockIndex);
            indexBlock = indexBlockSerializer.from(indexBlockBuffer);

            for (int i = 0; i < indexBlock.getBlockPointers().size(); i++) {
                allocatedSpace.fillBlockZeros(indexBlock.getBlockPointers().get(i));
            }
            allocatedSpace.fillBlockZeros(indexBlockIndex);
            indexBlockIndex = indexBlock.getNextIndexBlock();
        } while (indexBlock.containsNextBlock());

        fileDescriptorsBlock.removeDescriptor(filename);
        ByteBuffer descriptorsBlockBuffer = descriptorsBlockSerializer.toByteBuffer(fileDescriptorsBlock);
        allocatedSpace.writeBlock(fileDescriptorsBlockIndex, descriptorsBlockBuffer);
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
        return getFileDescriptorsBlock(filename).first().getDescriptor(filename);
    }

    @Nonnull
    private Pair<FileDescriptorsBlock, Integer> getFileDescriptorsBlock(@Nonnull String filename) throws IOException {
        FileDescriptorsBlock descriptors;
        Optional<FileDescriptor> descriptor;
        int nextBlock = 0;
        do {
            ByteBuffer byteBuffer = allocatedSpace.readBlock(nextBlock);
            descriptors = descriptorsBlockSerializer.from(byteBuffer);

            descriptor = descriptors.getDescriptors().stream()
                    .filter(fileDescriptor -> filename.equals(fileDescriptor.getFilename()))
                    .findFirst();
            if (descriptor.isEmpty()) {
                nextBlock = descriptors.getNextFileDescriptorBlock();
            }
        } while (descriptor.isEmpty() && descriptors.containsNextBlock());

        if (descriptor.isEmpty()) {
            throw new FileNotFoundException();
        }

        return Pair.of(descriptors, nextBlock);
    }
}
