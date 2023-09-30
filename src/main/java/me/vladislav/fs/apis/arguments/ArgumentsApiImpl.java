package me.vladislav.fs.apis.arguments;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.BlockSize;
import me.vladislav.fs.apis.ArgumentsApi;
import me.vladislav.fs.apis.JavaApi;
import me.vladislav.fs.apis.UnknownOperationException;
import me.vladislav.fs.util.ResourceUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArgumentsApiImpl implements ArgumentsApi {

    private final JavaApi javaApi;
    private final ArgumentsParserFactory argumentsParserFactory;

    @Override
    public void run(ApplicationArguments rawArgs) throws IOException {
        ArgumentsParser arguments = argumentsParserFactory.create(rawArgs);
        String operation = arguments.operation();
        if (operation == null) {
            return;
        }

        switch (operation) {
            case HELP -> System.out.println(ResourceUtils.getResourceAsString("/arguments_help.md"));
            case OPERATION_CREATE_FS -> {
                var request = CreateFileSystemRequest.builder()
                        .whereToStore(arguments.fileSystemPath())
                        .blockSize(BlockSize.KB_4);
                Integer initSize = arguments.fsInitSize();
                if (initSize != null) {
                    request.initialSizeInBytes(initSize);
                }
                createFileSystem(request.build());
            }
            case OPERATION_CREATE_FILE -> createFile(
                    CreateFileRequest.builder()
                            .filename(arguments.filename())
                            .content(arguments.fileIn())
                            .fsPath(arguments.fileSystemPath())
                            .build()
            );
            case OPERATION_READ_FILE -> {
                SeekableByteChannel out = arguments.fileOut();

                SeekableByteChannel file = readFile(
                        ReadFileRequest.builder()
                                .filename(arguments.filename())
                                .fsPath(arguments.fileSystemPath())
                                .build()
                );

                if (out == null) {
                    dumpToSystemOut(file);
                } else {
                    dumpToChannel(file, out);
                    out.close();
                }
            }
            case OPERATION_UPDATE_FILE -> updateFile(
                    UpdateFileRequest.builder()
                            .filename(arguments.filename())
                            .content(arguments.fileIn())
                            .fsPath(arguments.fileSystemPath())
                            .build()
            );
            case OPERATION_DELETE_FILE -> deleteFile(
                    DeleteFileRequest.builder()
                            .filename(arguments.filename())
                            .fsPath(arguments.fileSystemPath())
                            .build()
            );
            case OPERATION_LIST_FILES -> System.out.println(listFiles(ListFilesRequest.builder()
                    .fsPath(arguments.fileSystemPath())
                    .build()));
            case OPERATION_MOVE_FILE -> moveFile(MoveFileRequest.builder()
                    .fsPath(arguments.fileSystemPath())
                    .filename(arguments.filename())
                    .newFilename(arguments.newFilename())
                    .build());
            case OPERATION_MD5_CHECKSUM -> {
                String checksum = md5ChecksumFile(Md5ChecksumFileRequest.builder()
                        .fsPath(arguments.fileSystemPath())
                        .filename(arguments.filename())
                        .build());
                System.out.println(checksum);
            }
            default -> throw new UnknownOperationException(operation);
        }
    }

    private void dumpToSystemOut(SeekableByteChannel channel) {
        BlockAllocatedSpace allocatedSpace = BlockAllocatedSpace.of(channel);
        while (allocatedSpace.hasNextBlock()) {
            ByteBuffer obj = allocatedSpace.readBlock();
            String content = new String(obj.array(), StandardCharsets.UTF_8);
            System.out.print(content);
        }
    }

    private void dumpToChannel(SeekableByteChannel file, SeekableByteChannel outFile) throws IOException {
        BlockAllocatedSpace in = BlockAllocatedSpace.of(file);
        while (in.hasNextBlock()) {
            outFile.write(in.readBlock());
        }
    }

    @Override
    public void createFileSystem(CreateFileSystemRequest request) {
        javaApi.createFileSystem(request);
    }

    @Override
    public void createFile(CreateFileRequest request) {
        javaApi.createFile(request);
    }

    @Override
    public SeekableByteChannel readFile(ReadFileRequest request) {
        return javaApi.readFile(request);
    }

    @Override
    public void updateFile(UpdateFileRequest request) {
        javaApi.updateFile(request);
    }

    @Override
    public void deleteFile(DeleteFileRequest request) {
        javaApi.deleteFile(request);
    }

    @Override
    public List<String> listFiles(ListFilesRequest request) {
        return javaApi.listFiles(request);
    }

    @Override
    public void moveFile(@Nonnull MoveFileRequest request) {
        javaApi.moveFile(request);
    }

    @Override
    public String md5ChecksumFile(@Nonnull Md5ChecksumFileRequest request) {
        return javaApi.md5ChecksumFile(request);
    }
}
