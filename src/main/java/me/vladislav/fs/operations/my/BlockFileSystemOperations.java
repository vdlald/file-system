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
import me.vladislav.fs.blocks.components.ChainedFileContentIndexBlock;
import me.vladislav.fs.blocks.components.ChainedFileContentIndexBlockFactory;
import me.vladislav.fs.blocks.components.ChainedFileDescriptorsBlock;
import me.vladislav.fs.blocks.components.ChainedFileDescriptorsBlockFactory;
import me.vladislav.fs.exceptions.FileAlreadyExistsException;
import me.vladislav.fs.exceptions.FileNotFoundException;
import me.vladislav.fs.operations.ExtendedFileSystemOperations;
import me.vladislav.fs.requests.CreateFileRequest;
import me.vladislav.fs.requests.UpdateFileRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.BoundedSeekableByteChannelInputStream;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope("prototype")
public class BlockFileSystemOperations implements ExtendedFileSystemOperations {

    public static final String METHOD_NAME = "MYI";
    private static final int FIRST_FILE_DESCRIPTORS_BLOCK_INDEX = 0;

    @Nonnull
    @Getter(onMethod = @__(@VisibleForTesting))
    private final IndexedBlockAllocatedSpace allocatedSpace;
    private final ChainedFileDescriptorsBlockFactory chainedFileDescriptorsBlockFactory;
    private final ChainedFileContentIndexBlockFactory chainedFileContentIndexBlockFactory;

    @SuppressWarnings("all")
    public BlockFileSystemOperations(
            IndexedBlockAllocatedSpace allocatedSpace,
            ChainedFileDescriptorsBlockFactory chainedFileDescriptorsBlockFactory,
            ChainedFileContentIndexBlockFactory chainedFileContentIndexBlockFactory
    ) {
        this.allocatedSpace = allocatedSpace;
        this.chainedFileDescriptorsBlockFactory = chainedFileDescriptorsBlockFactory;
        this.chainedFileContentIndexBlockFactory = chainedFileContentIndexBlockFactory;
    }

    @PostConstruct
    private void init() {
        allocatedSpace.markBlockAsAllocated(FIRST_FILE_DESCRIPTORS_BLOCK_INDEX);
    }

    @Override
    public void createFile(@Nonnull CreateFileRequest createFileRequest) {
        String filename = createFileRequest.getFilename();
        log.info("creating file: {}", filename);

        ChainedFileDescriptorsBlock descriptorChain = chainedFileDescriptorsBlockFactory.create(
                FIRST_FILE_DESCRIPTORS_BLOCK_INDEX, allocatedSpace);

        if (descriptorChain.getFileDescriptor(filename) != null) {
            throw new FileAlreadyExistsException(filename);
        }
        descriptorChain.resetToFirstBlock();

        log.debug("find file descriptor block with free space");

        long fileSize;
        try {
            fileSize = createFileRequest.getContent().size();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int fileIndexBlock = allocatedSpace.getFreeBlockIndexAndMarkAsAllocated();
        FileDescriptor fileDescriptor = FileDescriptor.builder()
                .filename(filename)
                .fileBlockIndex(fileIndexBlock)
                .fileSize(fileSize)
                .build();
        descriptorChain.addFileDescriptor(fileDescriptor);
        log.debug("save file descriptor in block: {}", descriptorChain.getCurrentBlockIndex());

        log.debug("create index block for content");
        FileContentIndexBlock firstIndexBlock = new FileContentIndexBlock();

        BlockSize blockSize = allocatedSpace.getBlockSize();
        BlockAllocatedSpace content = new BlockAllocatedSpace(blockSize, AllocatedSpace.builder()
                .data(createFileRequest.getContent())
                .build())
                .block(0);

        ChainedFileContentIndexBlock contentChain = chainedFileContentIndexBlockFactory.create(
                fileSize, fileIndexBlock, firstIndexBlock, allocatedSpace);

        log.debug("write file content in fs");
        Iterator<ByteBuffer> contentIterator = content.contentIterator();
        contentChain.rewriteBlocks(contentIterator);

        contentChain.close();
        try {
            createFileRequest.getContent().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    @Override
    public SeekableByteChannel readFile(@Nonnull String filename) {
        log.info("reading file: {}", filename);
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();

        ChainedFileDescriptorsBlock descriptorChain = chainedFileDescriptorsBlockFactory.create(
                FIRST_FILE_DESCRIPTORS_BLOCK_INDEX, allocatedSpace);

        FileDescriptor fileDescriptor = descriptorChain.getFileDescriptor(filename);
        if (fileDescriptor == null) {
            throw new FileNotFoundException(filename);
        }

        ChainedFileContentIndexBlock contentChain = chainedFileContentIndexBlockFactory.create(
                fileDescriptor, allocatedSpace);

        contentChain.readAllBlocks(channel);
        try {
            return channel.position(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateFile(@Nonnull UpdateFileRequest updateFileRequest) {
        String filename = updateFileRequest.getFilename();
        log.info("updating file: {}", filename);

        ChainedFileDescriptorsBlock descriptorChain = chainedFileDescriptorsBlockFactory.create(
                FIRST_FILE_DESCRIPTORS_BLOCK_INDEX, allocatedSpace);

        FileDescriptor fileDescriptor = descriptorChain.getFileDescriptor(filename);
        if (fileDescriptor == null) {
            throw new FileNotFoundException(filename);
        }

        BlockAllocatedSpace content = new BlockAllocatedSpace(allocatedSpace.getBlockSize(), AllocatedSpace.builder()
                .data(updateFileRequest.getContent())
                .build());

        log.debug("get file index block");
        ChainedFileContentIndexBlock contentChain = chainedFileContentIndexBlockFactory.create(
                fileDescriptor, allocatedSpace);

        log.debug("update file content");
        Iterator<ByteBuffer> contentIterator = content.contentIterator();
        contentChain.rewriteBlocks(contentIterator);
        contentChain.close();
    }

    @Override
    public void deleteFile(@Nonnull String filename) {
        log.info("deleting file: {}", filename);

        ChainedFileDescriptorsBlock descriptorChain = chainedFileDescriptorsBlockFactory.create(
                FIRST_FILE_DESCRIPTORS_BLOCK_INDEX, allocatedSpace);

        if (!descriptorChain.removeFileDescriptor(filename)) {
            throw new FileNotFoundException(filename);
        }
    }

    @Override
    public List<String> listFiles() {
        log.info("listing files");

        ChainedFileDescriptorsBlock descriptorChain = chainedFileDescriptorsBlockFactory.create(
                FIRST_FILE_DESCRIPTORS_BLOCK_INDEX, allocatedSpace);

        return descriptorChain.getAllDescriptors().stream()
                .map(FileDescriptor::getFilename)
                .collect(Collectors.toList());
    }

    @Override
    public void moveFile(@Nonnull String filename, @Nonnull String newFilename) {
        ChainedFileDescriptorsBlock descriptorChain = chainedFileDescriptorsBlockFactory.create(
                FIRST_FILE_DESCRIPTORS_BLOCK_INDEX, allocatedSpace);

        if (descriptorChain.getFileDescriptor(newFilename) != null) {
            throw new FileAlreadyExistsException(newFilename);
        }

        FileDescriptor fileDescriptor = descriptorChain.getFileDescriptor(filename);
        if (fileDescriptor == null) {
            throw new FileNotFoundException(filename);
        }

        fileDescriptor.setFilename(newFilename);
        descriptorChain.saveCurrentBlock();
    }

    @Override
    public String checksum(@Nonnull String filename) {
        SeekableByteChannel file = readFile(filename);

        try {
            return DigestUtils.md5Hex(new BoundedSeekableByteChannelInputStream(0, file.size(), file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
