package me.vladislav.fs.apis.arguments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vladislav.fs.BlockAllocatedSpace;
import me.vladislav.fs.BlockSize;
import me.vladislav.fs.apis.ApplicationApi;
import me.vladislav.fs.apis.JavaApi;
import me.vladislav.fs.requests.*;
import me.vladislav.fs.util.ResourceUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArgumentsApi implements ApplicationRunner, ApplicationApi {

    public static final String HELP = "help";
    public static final String OPERATION_CREATE_FS = "create-file-system";
    public static final String OPERATION_CREATE_FILE = "create-file";
    public static final String OPERATION_READ_FILE = "read-file";
    public static final String OPERATION_UPDATE_FILE = "update-file";
    public static final String OPERATION_DELETE_FILE = "delete-file";

    private final JavaApi javaApi;
    private final ArgumentsParserFactory argumentsParserFactory;

    @Override
    public void run(ApplicationArguments rawArgs) throws Exception {
        ArgumentsParser arguments = argumentsParserFactory.create(rawArgs);
        String operation = arguments.operation();
        if (operation == null) {
            return;
        }

        switch (operation) {
            case HELP -> {
                System.out.println(ResourceUtils.getResourceAsString("/arguments_help.md"));
            }
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
            case OPERATION_CREATE_FILE -> {
                createFile(
                        CreateFileRequest.builder()
                                .filename(arguments.filename())
                                .content(arguments.fileIn())
                                .build()
                );
            }
            case OPERATION_READ_FILE -> {
                SeekableByteChannel file = readFile(
                        ReadFileRequest.builder()
                                .filename(arguments.filename())
                                .build()
                );
                dumpToSystemOut(file);
            }
            case OPERATION_UPDATE_FILE -> {
                updateFile(
                        UpdateFileRequest.builder()
                                .filename(arguments.filename())
                                .content(arguments.fileIn())
                                .build()
                );
            }
            case OPERATION_DELETE_FILE -> {
                deleteFile(
                        DeleteFileRequest.builder()
                                .filename(arguments.filename())
                                .build()
                );
            }
            default -> {
                throw new RuntimeException();
            }
        }
    }

    private void dumpToSystemOut(SeekableByteChannel channel) {
        try {
            BlockAllocatedSpace allocatedSpace = BlockAllocatedSpace.of(channel);
            while (allocatedSpace.hasNextBlock()) {
                System.out.print(allocatedSpace.readBlock());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
}
